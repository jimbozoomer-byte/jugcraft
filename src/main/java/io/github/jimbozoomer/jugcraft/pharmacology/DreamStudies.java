package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.dreaming.Dreaming;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauScrying;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import java.util.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;

/** Authored, paid Overworld lessons. No world search, private inventory escrow, or dimension travel. */
public final class DreamStudies {
	public static final int MAX_SITES = 16;
	public static Block PLINTH;
	public static Item FOLIO;
	public static MenuType<DreamStudyMenu> MENU;
	private static final String SITES = "jugcraft_dream_sites_v1";
	private DreamStudies() {}
	public static void register() {
		DreamCue.register();
		MENU = Registry.register(BuiltInRegistries.MENU, Jugcraft.id("dream_study"), new MenuType<>(DreamStudyMenu::new, FeatureFlags.DEFAULT_FLAGS));
		var key = ResourceKey.create(Registries.BLOCK, Jugcraft.id("dream_study_plinth"));
		PLINTH = Registry.register(BuiltInRegistries.BLOCK, key, new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).setId(key).noOcclusion()) {
			@Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
				return net.minecraft.world.phys.shapes.Shapes.or(Block.box(1, 0, 1, 15, 3, 15), Block.box(4, 3, 4, 12, 9, 12),
						Block.box(1, 9, 1, 15, 12.5, 15), Block.box(6, 12, 12, 10, 15, 15));
			}
			@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player who, BlockHitResult hit) {
				if (who instanceof ServerPlayer player && RateGate.allow(player, "dream_study_open", 4)) {
					if (!available(player) || !visible(player, pos, 6) || folio(player).isEmpty()) {
						player.sendOverlayMessage(Component.translatable("message.jugcraft.dream_study.folio"));
						return InteractionResult.FAIL;
					}
					discover(player, pos);
					player.openMenu(new SimpleMenuProvider((id, inv, p) -> new DreamStudyMenu(id, inv, pos), Component.translatable("block.jugcraft.dream_study_plinth")));
				}
				return InteractionResult.SUCCESS;
			}
		});
		JugcraftRegistry.item("dream_study_plinth", p -> new BlockItem(PLINTH, p.useBlockDescriptionPrefix()));
		FOLIO = JugcraftRegistry.item("dream_folio", p -> new DescribedItem(p.stacksTo(1)));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> { e.accept(PLINTH); e.accept(FOLIO); });
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 == 0) for (var player : server.getPlayerList().getPlayers()) perceive(player);
		});
	}
	public static boolean available(ServerPlayer player) {
		return player.isAlive() && !player.isSpectator() && !player.isSleeping() && !player.isPassenger()
				&& player.level().dimension().equals(Level.OVERWORLD) && !Dreaming.dreaming(player) && !NouveauScrying.active(player);
	}
	public static ItemStack folio(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			var stack = player.getInventory().getItem(slot);
			if (stack.is(FOLIO) && stack.getCount() == 1) return stack;
		}
		return ItemStack.EMPTY;
	}
	public static List<BlockPos> sites(ItemStack folio) {
		if (!folio.is(FOLIO)) return List.of();
		long[] saved = folio.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLongArray(SITES).orElseGet(() -> new long[0]);
		return Arrays.stream(saved).limit(MAX_SITES).distinct().mapToObj(BlockPos::of).toList();
	}
	/** Only a physically reached, visible authored block can enter the carried folio. */
	public static boolean discover(ServerPlayer player, BlockPos pos) {
		var folio = folio(player);
		if (folio.isEmpty() || !available(player) || !visible(player, pos, 6)) return false;
		var sites = new ArrayList<>(sites(folio));
		sites.remove(pos); sites.addFirst(pos.immutable());
		long[] packed = sites.stream().limit(MAX_SITES).mapToLong(BlockPos::asLong).toArray();
		CustomData.update(DataComponents.CUSTOM_DATA, folio, tag -> tag.putLongArray(SITES, packed));
		player.getInventory().setChanged();
		return true;
	}
	public static boolean visible(ServerPlayer player, BlockPos pos, int range) {
		var level = player.level();
		if (range < 1 || range > 24 || !level.hasChunkAt(pos) || !level.getBlockState(pos).is(PLINTH)
				|| player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > (double) range * range) return false;
		// clip() reads along the whole ray. A loaded destination alone does not guarantee a loaded path.
		int fromX = player.blockPosition().getX() >> 4, fromZ = player.blockPosition().getZ() >> 4;
		for (int x = Math.min(fromX, pos.getX() >> 4); x <= Math.max(fromX, pos.getX() >> 4); x++)
			for (int z = Math.min(fromZ, pos.getZ() >> 4); z <= Math.max(fromZ, pos.getZ() >> 4); z++)
				if (!level.hasChunkAt(new BlockPos(x << 4, pos.getY(), z << 4))) return false;
		var hit = level.clip(new ClipContext(player.getEyePosition(), Vec3.atCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
	}
	public static Optional<BlockPos> resonance(ServerPlayer player) {
		if (!available(player) || !Pharmacology.activeProfile(player, PsychedelicPharmacy.MESCALINE)) return Optional.empty();
		return sites(folio(player)).stream().filter(pos -> player.level().hasChunkAt(pos)
				&& player.level().getBlockState(pos).is(PLINTH) && player.distanceToSqr(Vec3.atCenterOf(pos)) <= 96 * 96)
				.min(Comparator.comparingDouble(pos -> player.distanceToSqr(Vec3.atCenterOf(pos))));
	}
	/** Only bounded carried-folio entries are considered; visibility checks preflight the loaded ray path. */
	public static DreamCue cue(ServerPlayer player) {
		if(!available(player))return DreamCue.EMPTY;
		int sight=PsychedelicPharmacy.sight(player);
		int mode=sight>0?sight:Pharmacology.activeProfile(player,PsychedelicPharmacy.MESCALINE)?4:0;
		if(mode==0)return DreamCue.EMPTY;
		var folio=folio(player);if(folio.isEmpty())return new DreamCue(mode,false,List.of());
		if(mode==4)return new DreamCue(mode,true,resonance(player).stream().toList());
		var visible=sites(folio).stream().filter(pos->visible(player,pos,sight>=2?24:16))
				.sorted(Comparator.comparingDouble(pos->player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)))).limit(4).toList();
		return new DreamCue(mode,true,visible);
	}
	private static void perceive(ServerPlayer player) {
		DreamCue.sync(player,cue(player));
		if(!available(player))return;
		if (player.containerMenu == player.inventoryMenu) resonance(player).ifPresent(pos -> {
			var delta = Vec3.atCenterOf(pos).subtract(player.position());
			String direction = Math.abs(delta.x) > Math.abs(delta.z) ? delta.x > 0 ? "east" : "west" : delta.z > 0 ? "south" : "north";
			player.sendOverlayMessage(Component.translatable("message.jugcraft.dream_study.resonance", Component.translatable("message.jugcraft.dream_study.direction_" + direction), (int) Math.ceil(delta.length())));
		});
	}
}
