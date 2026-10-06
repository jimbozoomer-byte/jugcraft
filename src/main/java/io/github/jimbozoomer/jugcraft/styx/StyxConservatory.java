package io.github.jimbozoomer.jugcraft.styx;

import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownBuilder;
import io.github.jimbozoomer.jugcraft.town.TownData;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Explicit, previewed operator placement. Natural world generation is a later stage of this feature. */
public final class StyxConservatory {
	public static final int WIDTH=27, HEIGHT=28, DEPTH=21, BLOCKS_PER_TICK=128;
	public record Placement(BlockPos offset, BlockState state) { }
	private record Preview(BlockPos origin, long expires) { }
	private static final Map<UUID,Preview> PREVIEWS=new HashMap<>();
	private static List<Placement> placements;
	private StyxConservatory() { }
	public static List<Placement> placements() {
		if(placements==null) {
			try(var stream=StyxConservatory.class.getResourceAsStream("/data/jugcraft/styx/conservatory.json")) {
				if(stream==null) throw new IllegalStateException("Missing conservatory blueprint");
				var json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
				var palette=new ArrayList<BlockState>();
				for(var s:json.getAsJsonArray("palette")) palette.add(TownData.parse(s.getAsString()));
				var list=new ArrayList<Placement>();
				for(var entry:json.getAsJsonArray("blocks")) {
					var b=entry.getAsJsonArray();
					BlockPos pos=new BlockPos(b.get(0).getAsInt(),b.get(1).getAsInt(),b.get(2).getAsInt());
					if(pos.getX()<0 || pos.getX()>=WIDTH || pos.getY()<0 || pos.getY()>=HEIGHT || pos.getZ()<0 || pos.getZ()>=DEPTH) throw new IllegalStateException("Conservatory block out of bounds");
					list.add(new Placement(pos,palette.get(b.get(3).getAsInt())));
				}
				placements=List.copyOf(list);
			} catch(Exception e) { throw new IllegalStateException("Cannot load conservatory",e); }
		}
		return placements;
	}
	public static boolean loaded(ServerLevel level,BlockPos origin) {
		for(int x=origin.getX()>>4;x<=(origin.getX()+WIDTH-1)>>4;x++)
			for(int z=origin.getZ()>>4;z<=(origin.getZ()+DEPTH-1)>>4;z++)
				if(level.getChunkSource().getChunkNow(x,z)==null) return false;
		return true;
	}
	/** Reads loaded chunks only. Rejects homes, block entities, solid obstacles and the protected town. */
	public static String canPlace(ServerLevel level,BlockPos origin) {
		if(level.dimension()!=Level.OVERWORLD) return "The conservatory belongs in the Overworld.";
		if(StyxState.get(level).origin.isPresent()) return "This world already has a conservatory.";
		if(origin.getY()<level.getMinY() || origin.getY()+HEIGHT>=level.getMaxY()) return "Not enough vertical room.";
		if(!loaded(level,origin)) return "Load the whole preview area first; no chunks will be forced.";
		for(int x=0;x<WIDTH;x++) for(int z=0;z<DEPTH;z++) for(int y=0;y<HEIGHT;y++) {
			BlockPos p=origin.offset(x,y,z); BlockState state=level.getBlockState(p);
			if(Town.isInside(level,p) || level.getBlockEntity(p)!=null) return "The footprint overlaps a protected town or a block entity.";
			if(!state.isAir() && !state.canBeReplaced() && !(y==0 && (state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)))) return "Clear a level 27 x 21 area with 28 blocks of headroom first. Obstruction: "+BuiltInRegistries.BLOCK.getKey(state.getBlock())+" at "+p.toShortString()+" (floor "+origin.getY()+").";
		}
		return "";
	}
	public static boolean begin(ServerLevel level,BlockPos origin) {
		if(!canPlace(level,origin).isEmpty()) return false;
		StyxState state=StyxState.get(level);state.origin=Optional.of(origin.immutable());state.placed=0;state.setDirty();return true;
	}
	public static void tick(ServerLevel level) {
		StyxState state=StyxState.get(level);
		if(state.origin.isEmpty() || state.resident.isPresent()) return;
		BlockPos origin=state.origin.get();
		if(!loaded(level,origin)) return;
		List<Placement> blocks=placements();
		int end=Math.min(blocks.size(),state.placed+BLOCKS_PER_TICK);
		state.setDirty(); // Retain progress even if a later entry in this batch is obstructed.
		for(;state.placed<end;state.placed++) {
			Placement p=blocks.get(state.placed);BlockPos at=origin.offset(p.offset());
			BlockState current=level.getBlockState(at);
			// Pause rather than overwrite a new obstruction introduced after preview or while unloaded.
			if(current!=p.state() && !current.isAir() && !current.canBeReplaced() && !(p.offset().getY()==0 && (current.is(BlockTags.DIRT) || current.is(Blocks.GRASS_BLOCK)))) return;
			TownBuilder.set(level,at,p.state());
		}
		state.setDirty();
		if(state.placed==blocks.size()) {
			Styxhexenhammer wizard=JugcraftStyx.WIZARD.create(level,EntitySpawnReason.STRUCTURE);
			if(wizard!=null) {
				wizard.setHome(origin);wizard.snapTo(origin.getX()+8.5,origin.getY()+1,origin.getZ()+10.5,0,0);
				if(level.addFreshEntity(wizard)) {state.resident=Optional.of(wizard.getUUID());state.setDirty();}
			}
		}
	}
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick(server.overworld()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> PREVIEWS.clear());
		CommandRegistrationCallback.EVENT.register((dispatcher,access,env) -> dispatcher.register(Commands.literal("jugcraft")
				.then(Commands.literal("styx")
					.executes(c -> {var s=StyxState.get(c.getSource().getServer().overworld());c.getSource().sendSuccess(() -> Component.literal(s.origin.map(p -> "Nightglass Conservatory: "+p.toShortString()+"; placed "+s.placed+" blocks").orElse("No conservatory. Operators: /jugcraft styx preview, then /jugcraft styx place.")),false);return 1;})
					.then(Commands.literal("preview").requires(s -> Commands.LEVEL_GAMEMASTERS.check(s.permissions())).executes(c -> {
						var player=c.getSource().getPlayerOrException();BlockPos origin=player.blockPosition().offset(-13,-1,-10);
						String error=canPlace(player.level(),origin);
						if(!error.isEmpty()) {c.getSource().sendFailure(Component.literal(error));return 0;}
						PREVIEWS.put(player.getUUID(),new Preview(origin,player.level().getGameTime()+1200));
						c.getSource().sendSuccess(() -> Component.literal("Conservatory footprint "+origin.toShortString()+" to "+origin.offset(WIDTH-1,HEIGHT-1,DEPTH-1).toShortString()+". /jugcraft styx place confirms this area within 60 seconds."),false);return 1;
					}))
					.then(Commands.literal("place").requires(s -> Commands.LEVEL_GAMEMASTERS.check(s.permissions())).executes(c -> {
						var player=c.getSource().getPlayerOrException();Preview preview=PREVIEWS.remove(player.getUUID());
						if(preview==null || preview.expires()<player.level().getGameTime() || player.blockPosition().distSqr(preview.origin())>4096 || !begin(player.level(),preview.origin())) {
							c.getSource().sendFailure(Component.literal("Preview expired, moved out of reach, or footprint changed. Run /jugcraft styx preview again."));return 0;
						}
						c.getSource().sendSuccess(() -> Component.literal("Building the conservatory in bounded steps; Styxhexenhammer will arrive when it is complete."),true);return 1;
					})))));
	}
}
