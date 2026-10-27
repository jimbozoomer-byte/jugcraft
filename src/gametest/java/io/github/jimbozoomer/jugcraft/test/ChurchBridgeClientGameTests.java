package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge.Brazier;
import io.github.jimbozoomer.jugcraft.lair.HessiansBridleRite;
import io.github.jimbozoomer.jugcraft.lair.HowlingTotemRite;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairBrazierBlock;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.MistGateEntity;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the Church Bridge and the Hessian's Bridle (docs/features/church-bridge.md), in a real world, which
 * has the lair dimensions: first the bridle from end to end with the one player in survival ({@link #endToEnd}); then
 * pictures: the Grey Mist risen by the lantern post, and, in the hollow, the wood road from the arrival, the hollow from
 * its south side, a brazier, the covered bridge, the church and its yard, the whole hollow from over the road, and the
 * Grey Mist at the road's end. The Halloween season is switched off for the test (the hollow opens only out of it) and
 * back to its settings after; time stands at midnight. CI job {@code client}.
 */
public class ChurchBridgeClientGameTests implements FabricClientGameTest {
	private static final Lair HOLLOW = Lair.CHURCH_BRIDGE;
	private static final long MIDNIGHT = 18000;
	private static final long NOON = 6000;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("difficulty peaceful");
			server.runCommand("weather clear");
			// A stone pad over the spawn, open to the sky: a jack-o'-lantern on a fence post, one on bare stone, and one on a
			// post under a roof.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 10, y, z - 10, x + 10, y + 30, z + 10));
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 10, y - 1, z - 10, x + 10, y - 1, z + 10));
			BlockPos post = new BlockPos(x + 2, y, z - 3);
			BlockPos bare = new BlockPos(x + 6, y, z - 3);
			BlockPos roofedPost = new BlockPos(x - 6, y, z - 3);
			server.runCommand("setblock %d %d %d minecraft:spruce_fence".formatted(post.getX(), post.getY(), post.getZ()));
			server.runCommand("setblock %d %d %d minecraft:jack_o_lantern".formatted(post.getX(), post.getY() + 1, post.getZ()));
			server.runCommand("setblock %d %d %d minecraft:jack_o_lantern".formatted(bare.getX(), bare.getY(), bare.getZ()));
			server.runCommand("setblock %d %d %d minecraft:cobblestone_wall".formatted(roofedPost.getX(), roofedPost.getY(), roofedPost.getZ()));
			server.runCommand("setblock %d %d %d minecraft:jack_o_lantern".formatted(roofedPost.getX(), roofedPost.getY() + 1, roofedPost.getZ()));
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(roofedPost.getX() - 1, y + 4, roofedPost.getZ() - 1,
					roofedPost.getX() + 1, y + 4, roofedPost.getZ() + 1));
			server.runCommand("time set " + MIDNIGHT);
			context.waitTicks(10);

			BlockPos lantern = post.above();
			BlockPos spot = new BlockPos(x + 2, y, z);
			try {
				endToEnd(context, server, lantern, bare, roofedPost.above(), spot);

				// The pictures, in creative, the mist held open long enough to take them all.
				server.runCommand("gamemode creative @a");
				String gateSeconds = JugcraftConfig.textOption("lairs.gate_seconds");
				try {
					JugcraftConfig.setTextOption("lairs.gate_seconds", "600");
					AtomicReference<LairInstance> instance = new AtomicReference<>();
					server.runOnServer(minecraft -> {
						ServerPlayer player = player(minecraft);
						ServerLevel overworld = minecraft.overworld();
						player.teleportTo(overworld, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
						player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.HESSIANS_BRIDLE));
						HessiansBridleRite.Bridling bridling = HessiansBridleRite.bridle(overworld, player, lantern, player.getMainHandItem());
						check(bridling.missing() == null, "The bridle opened no hollow for the pictures: " + bridling);
						instance.set(bridling.instance());
					});
					context.waitTicks(40);
					// The mist by the post (the camera leaves the hollow by command, which ends the visit; the mist stays).
					shootAt(context, singleplayer, "minecraft:overworld", spot.getX() + 3.5, y, spot.getZ() + 4.5, spot.getX() + 0.5, y + 1.0,
							spot.getZ() - 1.0, "jugcraft_hessians_bridle_mist");
					server.runOnServer(minecraft -> check(Lairs.enter(player(minecraft), instance.get()), "The camera could not go back in"));
					context.waitTicks(40);
					singleplayer.getConnection().waitForChunksRender();
					BlockPos o = HOLLOW.origin(0);
					String dim = HOLLOW.dimension.identifier().toString();
					int stand = ChurchBridge.STAND;
					shoot(context, singleplayer, dim, o.getX() + 36.5, o.getY() + stand, o.getZ() + 77.5, 180, -4, "jugcraft_church_bridge_arrival");
					shoot(context, singleplayer, dim, o.getX() + 36.5, o.getY() + stand, o.getZ() + 64.5, 180, -6, "jugcraft_church_bridge_hollow");
					BlockPos brazier = Brazier.NORTH_EAST.pos(o);
					shootAt(context, singleplayer, dim, brazier.getX() + 3.5, o.getY() + stand, brazier.getZ() + 3.5, brazier.getX() + 0.5,
							brazier.getY() + 0.5, brazier.getZ() + 0.5, "jugcraft_church_bridge_brazier");
					shootAt(context, singleplayer, dim, o.getX() + 30.5, o.getY() + stand, o.getZ() + 30.5, o.getX() + 36.5, o.getY() + stand + 2.0,
							o.getZ() + 25.5, "jugcraft_church_bridge_bridge");
					shootAt(context, singleplayer, dim, o.getX() + 46.5, o.getY() + stand, o.getZ() + 19.5, o.getX() + 36.5, o.getY() + stand + 6.0,
							o.getZ() + 12.5, "jugcraft_church_bridge_church");
					shoot(context, singleplayer, dim, o.getX() + 36.5, o.getY() + stand + 20, o.getZ() + 84.5, 180, 32, "jugcraft_church_bridge_above");
					shoot(context, singleplayer, dim, o.getX() + 36.5, o.getY() + stand, o.getZ() + 77.5, 0, 0, "jugcraft_church_bridge_exit");
				} finally {
					JugcraftConfig.setTextOption("lairs.gate_seconds", gateSeconds);
					server.runOnServer(minecraft -> Lairs.reset());
				}
			} finally {
				HalloweenSeason.reset();
				server.runCommand("gamerule minecraft:advance_time true");
			}
		}
	}

	/**
	 * The Hessian's Bridle from end to end, step by step, with the player in survival (the instances are the whole server's,
	 * so this one test owns them):
	 * <ol>
	 * <li>in the Halloween season the bridle is refused and kept; out of it, by day, the same; at midnight on a lantern on
	 * bare stone, and on one on a post under a roof, the same;</li>
	 * <li>used by the player's hand on the jack-o'-lantern on its fence post, at midnight out of the season, it opens an
	 * instance in the hollow's own dimension, places the hollow fresh (the road under the arrival, the Grey Mist at its
	 * end, every brazier lit, the brook's water and the bridge's deck over it), is used up and rested, raises the Grey Mist
	 * where the player stood, leaves the lantern on its post, and brings the player to on the road, unable to build;</li>
	 * <li>in the hollow a brazier gone dark is lit again by using it;</li>
	 * <li>leaving takes the player back to where they stood; while the mist is open a second bridle there is refused and
	 * kept, and using the mist takes them back into the same hollow;</li>
	 * <li>when the instance closes the player goes home and the mist is gone.</li>
	 * </ol>
	 */
	private static void endToEnd(ClientGameTestContext context, TestServerContext server, BlockPos lantern, BlockPos bare, BlockPos roofed,
			BlockPos spot) {
		server.runCommand("gamemode survival @a");
		context.waitTicks(5);
		AtomicReference<LairInstance> first = new AtomicReference<>();
		AtomicReference<Vec3> stood = new AtomicReference<>();

		// 1. Refused in the season, by day, on bare stone and under a roof; kept each time.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			player.teleportTo(overworld, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.HESSIANS_BRIDLE, 2));
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			HessiansBridleRite.Bridling bridling = HessiansBridleRite.bridle(overworld, player, lantern, player.getMainHandItem());
			check(bridling.missing() == HessiansBridleRite.Missing.IN_SEASON && player.getMainHandItem().getCount() == 2,
					"In the season, the bridle was not refused, or was used up: " + bridling + ", " + player.getMainHandItem());
			HalloweenSeason.setMode(HalloweenSeason.Mode.OFF);
		});
		server.runCommand("time set " + NOON);
		context.waitTicks(5);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			HessiansBridleRite.Bridling bridling = HessiansBridleRite.bridle(overworld, player, lantern, player.getMainHandItem());
			check(bridling.missing() == HessiansBridleRite.Missing.NOT_NIGHT && player.getMainHandItem().getCount() == 2,
					"By day, the bridle was not refused, or was used up: " + bridling);
		});
		server.runCommand("time set " + MIDNIGHT);
		context.waitTicks(5);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			HessiansBridleRite.Bridling bridling = HessiansBridleRite.bridle(overworld, player, bare, player.getMainHandItem());
			check(bridling.missing() == HessiansBridleRite.Missing.NOT_LANTERN && player.getMainHandItem().getCount() == 2,
					"On a lantern on bare stone, the bridle was not refused, or was used up: " + bridling);
			bridling = HessiansBridleRite.bridle(overworld, player, roofed, player.getMainHandItem());
			check(bridling.missing() == HessiansBridleRite.Missing.ROOFED && player.getMainHandItem().getCount() == 2,
					"On a lantern under a roof, the bridle was not refused, or was used up: " + bridling);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.HESSIANS_BRIDLE));
		});
		context.waitTicks(50);

		// 2. At midnight out of the season, on the lantern on its post, the bridle opens the hollow.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			stood.set(player.position());
			HessiansBridleRite.Missing ready = HessiansBridleRite.check(overworld, lantern, HowlingTotemRite.feet(player), !overworld.isBrightOutside(),
					HalloweenSeason.active());
			check(ready == null, "The bridle could not open the hollow here: " + ready);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(lantern), Direction.SOUTH, lantern, false);
			InteractionResult used = player.gameMode.useItemOn(player, overworld, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
			check(used.consumesAction(), "Using the bridle on the lantern did not succeed: " + used);
		});
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			LairInstance instance = Lairs.open(HOLLOW).isEmpty() ? null : Lairs.open(HOLLOW).getFirst();
			check(instance != null, "The bridle opened no instance");
			first.set(instance);
			check(player.getMainHandItem().isEmpty(), "The bridle was not used up: " + player.getMainHandItem());
			check(player.getCooldowns().isOnCooldown(new ItemStack(JugcraftLairs.HESSIANS_BRIDLE)), "The bridle was not rested after its use");
			check(overworld.getBlockState(lantern).is(Blocks.JACK_O_LANTERN) && overworld.getBlockState(lantern.below()).is(Blocks.SPRUCE_FENCE),
					"The lantern or its post was taken");
			ServerLevel lair = minecraft.getLevel(HOLLOW.dimension);
			check(lair != null && lair.getMinY() == 0, "The Church Bridge's dimension: from y 0");
			BlockPos o = instance.origin();
			BlockPos underArrival = BlockPos.containing(HOLLOW.arrival).below().offset(o);
			check(lair.getBlockState(underArrival).is(Blocks.DIRT_PATH), "No road under the arrival: " + lair.getBlockState(underArrival));
			for (BlockPos mist : ChurchBridge.exitCells()) {
				check(lair.getBlockState(o.offset(mist)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist at the road's end at " + mist);
			}
			check(ChurchBridge.lit(lair, o).size() == Brazier.values().length, "Not every brazier burns: " + ChurchBridge.lit(lair, o));
			check(lair.getBlockState(o.offset(30, ChurchBridge.SOIL, ChurchBridge.BROOK_NORTH + 1)).is(Blocks.WATER), "No water in the brook");
			check(lair.getBlockState(o.offset(ChurchBridge.ROAD_WEST + 1, ChurchBridge.SOIL, ChurchBridge.BROOK_NORTH + 1)).is(Blocks.SPRUCE_PLANKS),
					"No bridge deck over the brook");
			check(player.level() == lair && player.position().distanceTo(instance.arrival()) < 0.5,
					"The player did not come to on the road: " + player.level().dimension() + " " + player.position());
			check(!player.getAbilities().mayBuild, "In a lair a player cannot build");
			MistGateEntity mist = HessiansBridleRite.mist(overworld, BlockPos.containing(stood.get()));
			check(mist != null && mist.instance() == instance && mist.lair() == HOLLOW, "No mist rose where the bridle was used");
		});
		context.waitTicks(40);

		// 3. A brazier gone dark is lit again by using it.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel lair = minecraft.getLevel(HOLLOW.dimension);
			BlockPos o = first.get().origin();
			BlockPos at = Brazier.SOUTH_EAST.pos(o);
			lair.setBlock(at, lair.getBlockState(at).setValue(LairBrazierBlock.LIT, false), Block.UPDATE_ALL);
			check(!ChurchBridge.lit(lair, o, Brazier.SOUTH_EAST), "The brazier did not go dark");
			player.teleportTo(at.getX() + 0.5, at.getY(), at.getZ() + 2.5);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(at), Direction.SOUTH, at, false);
			player.gameMode.useItemOn(player, lair, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
			check(ChurchBridge.lit(lair, o, Brazier.SOUTH_EAST), "Using a dark brazier did not light it");
		});

		// 4. Out, a second bridle refused at the open mist, and back in through the mist.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			check(Lairs.leave(player) && player.level() == minecraft.overworld() && player.position().distanceTo(stood.get()) < 1.0E-6,
					"The player did not come back to where they stood: " + player.position());
		});
		context.waitTicks(40);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.HESSIANS_BRIDLE));
			check(HessiansBridleRite.bridle(overworld, player, lantern, player.getMainHandItem()).missing() == HessiansBridleRite.Missing.MISTING
					&& player.getMainHandItem().getCount() == 1, "A second bridle at the open mist was not refused, or was used up");
			MistGateEntity mist = HessiansBridleRite.mist(overworld, BlockPos.containing(stood.get()));
			check(mist != null, "The mist closed early");
			mist.use(player, InteractionHand.MAIN_HAND);
			check(player.level() == minecraft.getLevel(HOLLOW.dimension) && Lairs.instance(player.getAttached(Lairs.VISIT)) == first.get(),
					"The mist did not take the player back into the same hollow");
		});
		context.waitTicks(40);

		// 5. Closed, the player goes home and the mist is gone.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			Lairs.close(minecraft, first.get());
			check(player.level() == minecraft.overworld(), "Closing the hollow did not send the player home");
		});
		context.waitTicks(50);
		server.runOnServer(minecraft -> {
			ServerLevel overworld = minecraft.overworld();
			BlockPos at = BlockPos.containing(stood.get());
			check(HessiansBridleRite.mist(overworld, at) == null
					&& overworld.getEntitiesOfClass(MistGateEntity.class, new AABB(at).inflate(3)).isEmpty(), "The mist outlived its hollow");
			Lairs.reset();
		});
		context.waitTicks(20);
		Jugcraft.LOGGER.info("[lairs] client game test: the Hessian's Bridle passed end to end");
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().get(0);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	/** Stands the camera at (x, y, z) in {@code dimension} (its feet), looking at (tx, ty, tz), and shoots. */
	private static void shootAt(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, double x, double y,
			double z, double tx, double ty, double tz, String name) {
		double dx = tx - x;
		double dy = ty - (y + 1.62);
		double dz = tz - z;
		int yaw = (int) Math.round(Math.toDegrees(Math.atan2(-dx, dz)));
		int pitch = (int) Math.round(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
		shoot(context, singleplayer, dimension, x, y, z, yaw, pitch, name);
	}

	/** Stands the camera at (x, y, z) in {@code dimension}, looking along yaw and pitch, waits for the world to draw, and shoots. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, double x, double y,
			double z, int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runOnServer(minecraft -> {
			// Flying, so the camera stays where it is put (set on the server, which tells the client).
			ServerPlayer player = player(minecraft);
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		});
		server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.2f %.2f %.2f %d %d", dimension, x, y, z, yaw, pitch));
		context.waitTicks(30);
		singleplayer.getConnection().waitForChunksRender();
		context.waitTicks(10);
		context.takeScreenshot(name);
	}
}
