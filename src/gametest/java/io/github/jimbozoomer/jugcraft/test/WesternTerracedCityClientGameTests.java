package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.stream.JsonReader;
import io.github.jimbozoomer.jugcraft.town.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.world.level.storage.LevelResource;
import java.util.zip.GZIPInputStream;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Architecture review only in a new, disposable world. Does not exercise a live installer. */
public final class WesternTerracedCityClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        int distance=context.computeOnClient(c->c.options.renderDistance().get());
        context.runOnClient(c->c.options.renderDistance().set(12));
        String reference=System.getenv("JUGCRAFT_WESTERN_TERRAIN_REFERENCE");
        BlockPos origin=reference==null?new BlockPos(400,114,400):new BlockPos(1296,114,400);
        try(var world=reviewWorld(context,reference)) {
            var server=world.getServer();
            server.runCommand("gamemode spectator @p"); server.runCommand("time set noon");
            server.runCommand("weather clear"); server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("tp @p "+(origin.getX()-100)+" 130 495");
            server.runCommand("forceload add "+(origin.getX()-224)+" 400 "+(origin.getX()+31)+" 591");
            server.runOnServer(s->{
                TownState.get(s).place(new BlockPos(2000,100,2000)); // Keep unrelated town care out of this fixture.
            });
            context.waitTicks(80);
            var preserved=server.computeOnServer(s->{
                var result=new java.util.HashMap<BlockPos,net.minecraft.nbt.CompoundTag>();
                if(reference!=null)for(int cx=(origin.getX()-224)>>4;cx<=(origin.getX()+31)>>4;cx++)for(int cz=25;cz<=36;cz++)
                    for(var be:s.overworld().getChunk(cx,cz).getBlockEntities().values())
                        if(be instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)
                            result.put(be.getBlockPos().immutable(),be.saveWithoutMetadata(s.registryAccess()));
                return result;
            });
            var dryUnderground=new it.unimi.dsi.fastutil.longs.LongArrayList();
            try(var stream=getClass().getResourceAsStream("/data/jugcraft/test/western_terraced_review.json.gz")) {
                if(stream==null) throw new AssertionError("Regenerate the committed terraced review fixture");
                try(var in=new JsonReader(new InputStreamReader(new GZIPInputStream(stream),StandardCharsets.UTF_8))) {
                    var palette=new ArrayList<BlockState>(); int total=0;
                    in.beginObject();
                    while(in.hasNext()) switch(in.nextName()) {
                        case "palette" -> {
                            in.beginArray(); while(in.hasNext()) {
                                String state=in.nextString();palette.add(server.computeOnServer(s->WesternDistrictPlan.parse(state)));
                            } in.endArray();
                        }
                        case "cells" -> {
                            in.beginArray();
                            while(in.hasNext()) {
                                int[][] batch=new int[4096][4];int count=0;
                                while(count<batch.length && in.hasNext()) {
                                    in.beginArray();for(int j=0;j<4;j++)batch[count][j]=in.nextInt();in.endArray();count++;
                                }
                                for(int i=0;i<count;i++)if(batch[i][1]<-49 && palette.get(batch[i][3]).isAir())
                                    dryUnderground.add(origin.offset(batch[i][0],batch[i][1],batch[i][2]).asLong());
                                final int n=count;
                                server.runOnServer(s->{for(int i=0;i<n;i++) {
                                    int[] cell=batch[i];TownBuilder.set(s.overworld(),origin.offset(cell[0],cell[1],cell[2]),palette.get(cell[3]));
                                }});
                                total+=count; context.waitTicks(1);
                            }in.endArray();
                        }
                        default -> in.skipValue();
                    }
                    in.endObject();if(total==0)throw new AssertionError("Incomplete terraced fixture");
                }
            } catch(java.io.IOException e) { throw new AssertionError(e); }
            // The site-reference chest remains in an unbuilt pocket; this test
            // stand-in is clearly synthetic and does not contain player belongings.
            if(reference==null)server.runOnServer(s->{
                for(int x=-169;x<=-154;x++)for(int z=108;z<=125;z++)
                    TownBuilder.set(s.overworld(),origin.offset(x,-50,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());
                TownBuilder.set(s.overworld(),origin.offset(-162,-49,115),net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());
            });
            // Exercise boundary fluids, not just a screenshot before old aquifer
            // water receives an update. The synthetic review remains dry as well.
            server.runOnServer(s->{
                var scheduled=new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
                for(long packed:dryUnderground)for(var direction:net.minecraft.core.Direction.values()) {
                    var p=BlockPos.of(packed).relative(direction);var fluid=s.overworld().getFluidState(p);
                    if(!fluid.isEmpty() && scheduled.add(p.asLong()))s.overworld().scheduleTick(p,fluid.getType(),1);
                }
            });
            context.waitTicks(200);
            server.runOnServer(s->{
                for(int x=-118;x<=-108;x++)for(int z=73;z<=83;z++)if(x==-118 || x==-108 || z==73 || z==83)
                    for(int y=-29;y<=-25;y++)if(!s.overworld().getFluidState(origin.offset(x,y,z)).isEmpty())
                        throw new AssertionError("Courtyard fountain escaped its basin at "+new BlockPos(x,y,z));
            });
            server.runOnServer(s->{
                for(var entry:preserved.entrySet()) {
                    var be=s.overworld().getBlockEntity(entry.getKey());
                    if(be==null || !entry.getValue().equals(be.saveWithoutMetadata(s.registryAccess())))
                        throw new AssertionError("Copied-site container changed at "+entry.getKey());
                }
            });
            context.runOnClient(c->{if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
            int[][] views={{-102,42,180,180,28},{-177,-46,84,180,-14},{-119,-28,84,180,-14},
                {-60,-10,93,180,-14},{-183,-46,96,-90,-12},{-102,-71,126,180,-12},
                {-102,-57,117,180,5},{-173,-71,51,210,0},{-173,-42,50,20,8},{-122,-28,87,220,8},{-119,-28,133,0,0},{-40,113,224,160,49},{-7,16,138,167,14},{14,2,96,90,4},{-94,124,98,90,86},{-8,194,128,180,82},{-7,21,152,180,15}};
            String[] names={"overview","quay_apartments","middle_apartments","upper_apartments","stair_avenue","rotunda","gallery","service_hall","room","courtyard","market","ring_overview","facing_gates","gate_crossing","ring_outline","both_rings","bridge_profile"};
            for(int i=0;i<views.length;i++) {
                if (names[i].equals("both_rings")) {
                    server.runOnServer(s -> s.getPlayerList().setViewDistance(16));
                    context.runOnClient(c -> c.options.renderDistance().set(16));
                }
                var a=views[i];server.runCommand(String.format(Locale.ROOT,"tp @p %.1f %d %.1f %d %d",
                    origin.getX()+a[0]+.5,origin.getY()+a[1],origin.getZ()+a[2]+.5,a[3],a[4]));
                context.waitTicks(80);world.getConnection().waitForChunksRender(false,1200);
                context.takeScreenshot((reference==null?"western_terraced_":"western_site_")+names[i]);
            }
            context.runOnClient(c->{if(c.gui.hud.isHidden())c.gui.hud.toggle();});
            server.runOnServer(s->{
                int wet=0;BlockPos first=null;
                for(long packed:dryUnderground) {
                    var p=BlockPos.of(packed);
                    if(!s.overworld().getFluidState(p).isEmpty()){wet++;if(first==null)first=p;}
                }
                if(wet!=0)throw new AssertionError("Undercity dry space flooded: "+wet+" cells; first "+first);
            });
        } finally { context.runOnClient(c->c.options.renderDistance().set(distance)); }
    }
    static TestSingleplayerContext reviewWorld(ClientGameTestContext context,String reference) {
        var builder=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
        });
        if(reference==null)return builder.create();
        final TestWorldSave save;
        final Path target;
        // Always create a new test-owned world. Never open the supplied directory as a save.
        try(var blank=builder.create()) {
            save=blank.getWorldSave();
            target=blank.getServer().computeOnServer(s->s.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize());
            blank.getServer().runOnServer(s->TownState.get(s).place(new BlockPos(2000,100,2000)));
        }
        try {
            Path source=Path.of(reference).toRealPath();
            Path regions=target.resolve("dimensions/minecraft/overworld/region");
            if(!Files.isDirectory(regions))throw new AssertionError("Unexpected disposable world dimension layout");
            for(String name:new String[]{"r.2.0.mca","r.2.1.mca"}) {
                Path destination=regions.resolve(name);
                if(Files.exists(destination))throw new AssertionError("Reference must only populate absent test regions");
                Files.copy(source.resolve(name),destination);
            }
        } catch(java.io.IOException e) {throw new AssertionError(e);}
        return save.open();
    }
}
