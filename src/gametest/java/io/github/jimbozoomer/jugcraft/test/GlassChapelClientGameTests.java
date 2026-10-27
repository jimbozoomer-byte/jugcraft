package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.GlassAccordRite;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.HowlingTotemRite;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.LairRules;
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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for the Glass Chapel and the Glass Accord (docs/features/glass-chapel.md), in a real world, which has
 * the lair dimensions: first the Accord from end to end with the one player in survival ({@link #endToEnd}); then
 * pictures: the Grey Mist risen where the Accord was held up, and, in the chapel, the narthex from the arrival, the nave
 * from the rood screen, the apse with the lectern under the rose window, the east mirror at rest, marked and veiled, the
 * east and south mirrors set for their pair seen from the Abbot's place, the lancets down the west wall and the Grey Mist
 * in the south door; last, the game's own block light on every open cell of the chapel. CI job {@code client}.
 */
public class GlassChapelClientGameTests implements FabricClientGameTest {
	private static final Lair CHAPEL = Lair.GLASS_CHAPEL;
	/**
	 * The least block light on any open cell of the chapel, and how many open cells it has (tools/glass_chapel.py
	 * NAVE_LIGHT and its open cells, checked by the audit).
	 */
	private static final int NAVE_LIGHT = 8;
	private static final int CHAPEL_CELLS = 1146;
	/** The height the test's pad stands at, high over the spawn, open to the sky. */
	private static final int HIGH = 130;

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
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("difficulty peaceful");
			server.runCommand("weather clear");
			server.runCommand("time set 6000");
			// A stone pad high over the spawn, open to the sky: a skylight of glass over one spot, a stone roof over glass
			// over another, and a spot with nothing over it.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 10, HIGH - 2, z - 10, x + 10, HIGH + 30, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 10, HIGH - 1, z - 10, x + 10, HIGH - 1, z + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:glass".formatted(x, HIGH + 5, z - 5, x + 4, HIGH + 5, z - 1));
			server.runCommand("fill %d %d %d %d %d %d minecraft:glass".formatted(x - 9, HIGH + 4, z - 9, x - 5, HIGH + 4, z - 5));
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 9, HIGH + 7, z - 9, x - 5, HIGH + 7, z - 5));
			context.waitTicks(20);

			BlockPos spot = new BlockPos(x + 2, HIGH, z - 3);
			BlockPos open = new BlockPos(x - 6, HIGH, z + 4);
			BlockPos shaded = new BlockPos(x - 7, HIGH, z - 7);
			endToEnd(context, server, spot, open, shaded);

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
					player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.GLASS_ACCORD));
					GlassAccordRite.Holding holding = GlassAccordRite.hold(overworld, player, player.getMainHandItem());
					check(holding.missing() == null, "The Accord opened no chapel for the pictures: " + holding);
					instance.set(holding.instance());
				});
				context.waitTicks(40);
				// The mist on the pad (the camera leaves the chapel by command, which ends the visit; the mist stays).
				shootAt(context, singleplayer, "minecraft:overworld", spot.getX() + 3.5, HIGH, spot.getZ() + 4.5, spot.getX() + 0.5, HIGH + 1.0,
						spot.getZ() + 0.5, "jugcraft_glass_accord_mist");
				server.runOnServer(minecraft -> check(Lairs.enter(player(minecraft), instance.get()), "The camera could not go back in"));
				context.waitTicks(40);
				singleplayer.getConnection().waitForChunksRender();
				BlockPos o = CHAPEL.origin(0);
				String dim = CHAPEL.dimension.identifier().toString();
				int stand = GlassChapel.STAND;
				shoot(context, singleplayer, dim, o.getX() + 17.5, o.getY() + stand, o.getZ() + 42.5, 180, -4, "jugcraft_glass_chapel_arrival");
				shoot(context, singleplayer, dim, o.getX() + 17.5, o.getY() + stand + 1, o.getZ() + 37.5, 180, -6, "jugcraft_glass_chapel_nave");
				shootAt(context, singleplayer, dim, o.getX() + 17.5, o.getY() + stand + 1, o.getZ() + 15.5, o.getX() + 17.5, o.getY() + GlassChapel.PAVE + 7.5,
						o.getZ() + 1.5, "jugcraft_glass_chapel_apse");
				BlockPos east = Mirror.EAST.base(o);
				shootAt(context, singleplayer, dim, o.getX() + 25.5, o.getY() + stand, o.getZ() + 23.5, east.getX() + 0.5, east.getY() + 1.0,
						east.getZ() + 0.5, "jugcraft_glass_chapel_mirror");
				// Marked, then veiled, held so for the pictures.
				server.runOnServer(minecraft -> ChapelMirrorBlock.setMarked(minecraft.getLevel(CHAPEL.dimension), east, true));
				shootAt(context, singleplayer, dim, o.getX() + 25.5, o.getY() + stand, o.getZ() + 23.5, east.getX() + 0.5, east.getY() + 1.0,
						east.getZ() + 0.5, "jugcraft_glass_chapel_mirror_marked");
				server.runOnServer(minecraft -> ChapelMirrorBlock.setVeiled(minecraft.getLevel(CHAPEL.dimension), east, true));
				shootAt(context, singleplayer, dim, o.getX() + 25.5, o.getY() + stand, o.getZ() + 23.5, east.getX() + 0.5, east.getY() + 1.0,
						east.getZ() + 0.5, "jugcraft_glass_chapel_mirror_veiled");
				// The east and south mirrors set for their pair and marked, seen from over the Abbot's place.
				server.runOnServer(minecraft -> {
					ServerLevel lair = minecraft.getLevel(CHAPEL.dimension);
					ChapelMirrorBlock.reset(lair, o);
					for (Mirror mirror : new Mirror[] {Mirror.EAST, Mirror.SOUTH}) {
						Mirror other = mirror == Mirror.EAST ? Mirror.SOUTH : Mirror.EAST;
						ChapelMirrorBlock.setStop(lair, mirror.base(o), mirror.stopFor(other));
						ChapelMirrorBlock.setMarked(lair, mirror.base(o), true);
					}
					check(GlassChapel.comesHome(GlassChapel.stops(lair, o), Mirror.EAST, Mirror.SOUTH),
							"The east and south mirrors set from their places do not send the beam home");
				});
				shootAt(context, singleplayer, dim, o.getX() + GlassChapel.ABBOT_X, o.getY() + stand + 5, o.getZ() + GlassChapel.ABBOT_Z + 1.0,
						o.getX() + 20.5, o.getY() + stand, o.getZ() + 24.5, "jugcraft_glass_chapel_mirrors_set");
				server.runOnServer(minecraft -> ChapelMirrorBlock.reset(minecraft.getLevel(CHAPEL.dimension), o));
				shootAt(context, singleplayer, dim, o.getX() + 7.5, o.getY() + stand, o.getZ() + 36.5, o.getX() + 1.5, o.getY() + stand + 4.0,
						o.getZ() + 22.5, "jugcraft_glass_chapel_lancets");
				shoot(context, singleplayer, dim, o.getX() + 17.5, o.getY() + stand, o.getZ() + 40.5, 0, 0, "jugcraft_glass_chapel_exit");

				// The game's own light on every open cell of the chapel, where the Abbot fights.
				server.runOnServer(minecraft -> {
					ServerLevel lair = minecraft.getLevel(CHAPEL.dimension);
					int least = 15;
					int cells = 0;
					BlockPos darkest = null;
					for (int dx = 0; dx < CHAPEL.width; dx++) {
						for (int dz = 0; dz < CHAPEL.length; dz++) {
							if (!GlassChapel.inside(dx + 0.5, dz + 0.5)) {
								continue;
							}
							BlockPos cell = o.offset(dx, stand, dz);
							if (lair.getBlockState(cell.below()).isAir() || !standable(lair, cell) || !standable(lair, cell.above())) {
								continue;
							}
							int light = lair.getBrightness(LightLayer.BLOCK, cell);
							cells++;
							if (light < least) {
								least = light;
								darkest = cell.subtract(o);
							}
						}
					}
					Jugcraft.LOGGER.info("[lairs] the Glass Chapel: {} open cells, the darkest at block light {} ({})", cells, least, darkest);
					check(cells == CHAPEL_CELLS && least >= NAVE_LIGHT, "The chapel has " + cells + " open cells (not " + CHAPEL_CELLS
							+ "), the darkest at block light " + least + " (" + darkest + "), under " + NAVE_LIGHT);
				});
			} finally {
				JugcraftConfig.setTextOption("lairs.gate_seconds", gateSeconds);
				server.runOnServer(minecraft -> Lairs.reset());
				server.runCommand("weather clear");
				server.runCommand("gamerule minecraft:advance_time true");
			}
		}
	}

	/**
	 * The Glass Accord from end to end, step by step, with the player in survival (the instances are the whole server's, so
	 * this one test owns them):
	 * <ol>
	 * <li>held up at night under the skylight the Accord is refused and kept; in the rain, the same; by day in clear
	 * weather with nothing over the holder, and under glass roofed over with stone, the same;</li>
	 * <li>held up by the player's hand by day under the skylight, it opens an instance in the chapel's own dimension,
	 * places the chapel fresh (the narthex under the arrival, the Grey Mist in the south door, each mirror whole and at
	 * rest, the lectern before the Abbot's place), is used up and rested, raises the Grey Mist where it was held, and brings
	 * the player into the narthex, unable to build;</li>
	 * <li>in the chapel a mirror is a fixture a player turns with a use, and the lectern is not; a player in the nave is
	 * where they may be; falling out of the chapel throws them back to the narthex for the mist's toll;</li>
	 * <li>leaving takes the player back to where they stood; while the mist is open a second Accord held up there is
	 * refused and kept, and using the mist takes them back into the same chapel;</li>
	 * <li>when the instance closes the player goes home and the mist is gone.</li>
	 * </ol>
	 */
	private static void endToEnd(ClientGameTestContext context, TestServerContext server, BlockPos spot, BlockPos open, BlockPos shaded) {
		server.runCommand("gamemode survival @a");
		server.runCommand("time set 18000");
		context.waitTicks(5);
		AtomicReference<LairInstance> first = new AtomicReference<>();
		AtomicReference<Vec3> stood = new AtomicReference<>();

		// 1. Refused at night, in the rain, with nothing over the holder and under a roof.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			player.teleportTo(overworld, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.GLASS_ACCORD, 2));
			GlassAccordRite.Holding holding = GlassAccordRite.hold(overworld, player, player.getMainHandItem());
			check(holding.missing() == GlassAccordRite.Missing.NIGHT && player.getMainHandItem().getCount() == 2,
					"At night, the Accord was not refused, or was used up: " + holding + ", " + player.getMainHandItem());
		});
		server.runCommand("time set 6000");
		server.runCommand("weather rain");
		context.waitTicks(100);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			check(overworld.isRaining(), "It is not raining");
			GlassAccordRite.Holding holding = GlassAccordRite.hold(overworld, player, player.getMainHandItem());
			check(holding.missing() == GlassAccordRite.Missing.CLOUDED && player.getMainHandItem().getCount() == 2,
					"In the rain, the Accord was not refused, or was used up: " + holding + ", " + player.getMainHandItem());
		});
		server.runCommand("weather clear");
		context.waitTicks(200);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			check(!overworld.isRaining() && overworld.isBrightOutside(), "It is not a clear day");
			player.teleportTo(overworld, open.getX() + 0.5, open.getY(), open.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
			GlassAccordRite.Holding holding = GlassAccordRite.hold(overworld, player, player.getMainHandItem());
			check(holding.missing() == GlassAccordRite.Missing.NO_GLASS && player.getMainHandItem().getCount() == 2,
					"With nothing over the holder, the Accord was not refused, or was used up: " + holding);
			player.teleportTo(overworld, shaded.getX() + 0.5, shaded.getY(), shaded.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
			holding = GlassAccordRite.hold(overworld, player, player.getMainHandItem());
			check(holding.missing() == GlassAccordRite.Missing.SHADED && player.getMainHandItem().getCount() == 2,
					"Under glass roofed with stone, the Accord was not refused, or was used up: " + holding);
			player.teleportTo(overworld, spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, Set.of(), 180.0F, 0.0F, true);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.GLASS_ACCORD));
		});
		context.waitTicks(50);

		// 2. By day under the skylight, the Accord opens the chapel and the light takes the player.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			stood.set(player.position());
			GlassAccordRite.Missing ready = GlassAccordRite.check(overworld, HowlingTotemRite.feet(player), overworld.isBrightOutside(),
					!overworld.isRaining());
			check(ready == null, "The Accord could not open the chapel here: " + ready);
			InteractionResult used = player.gameMode.useItem(player, overworld, player.getMainHandItem(), InteractionHand.MAIN_HAND);
			check(used.consumesAction(), "Holding up the Accord did not succeed: " + used);
		});
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			LairInstance instance = Lairs.open(CHAPEL).isEmpty() ? null : Lairs.open(CHAPEL).getFirst();
			check(instance != null, "The Accord opened no instance");
			first.set(instance);
			check(player.getMainHandItem().isEmpty(), "The Accord was not used up: " + player.getMainHandItem());
			check(player.getCooldowns().isOnCooldown(new ItemStack(JugcraftLairs.GLASS_ACCORD)), "The Accord was not rested after its use");
			ServerLevel lair = minecraft.getLevel(CHAPEL.dimension);
			check(lair != null && lair.getMinY() == 0, "The Glass Chapel's dimension: from y 0");
			BlockPos o = instance.origin();
			BlockPos underArrival = BlockPos.containing(CHAPEL.arrival).below().offset(o);
			check(lair.getBlockState(underArrival).isFaceSturdy(lair, underArrival, Direction.UP), "No narthex floor under the arrival");
			for (BlockPos mist : GlassChapel.exitCells()) {
				check(lair.getBlockState(o.offset(mist)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist in the south door at " + mist);
			}
			for (Mirror mirror : Mirror.values()) {
				for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
					BlockPos at = half == DoubleBlockHalf.LOWER ? mirror.base(o) : mirror.base(o).above();
					BlockState state = lair.getBlockState(at);
					check(state.is(JugcraftLairs.CHAPEL_MIRROR) && state.getValue(ChapelMirrorBlock.HALF) == half
							&& state.getValue(ChapelMirrorBlock.MIRROR) == mirror && state.getValue(ChapelMirrorBlock.STOP) == Mirror.REST
							&& !state.getValue(ChapelMirrorBlock.MARKED) && !state.getValue(ChapelMirrorBlock.VEILED),
							"No " + half + " of the " + mirror.getSerializedName() + " mirror at rest at " + at.subtract(o) + ": " + state);
				}
			}
			check(lair.getBlockState(o.offset(GlassChapel.LECTERN)).is(JugcraftLairs.ABBOT_LECTERN), "No lectern before the Abbot's place");
			check(player.level() == lair && player.position().distanceTo(instance.arrival()) < 0.5,
					"The player did not come into the narthex: " + player.level().dimension() + " " + player.position());
			check(!player.getAbilities().mayBuild, "In a lair a player cannot build");
			MistGateEntity mist = GlassAccordRite.mist(overworld, spot);
			check(mist != null && mist.instance() == instance && mist.lair() == CHAPEL, "No mist risen where the Accord was held up");
		});
		context.waitTicks(40);

		// 3. A mirror is a fixture that turns with a use; the lectern is not one; the nave is in bounds; a fall throws back.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel lair = minecraft.getLevel(CHAPEL.dimension);
			BlockPos o = first.get().origin();
			BlockPos upper = Mirror.EAST.base(o).above();
			player.teleportTo(o.getX() + 27.5, o.getY() + GlassChapel.STAND, o.getZ() + 20.5);
			check(LairRules.useBlock(player, lair, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(upper), Direction.WEST, upper, false))
					!= InteractionResult.FAIL, "A Chapel Mirror cannot be used in the chapel");
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(upper), Direction.WEST, upper, false);
			player.gameMode.useItemOn(player, lair, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
			check(ChapelMirrorBlock.stop(lair, Mirror.EAST.base(o)) == 1, "Used with an empty hand, the east mirror did not turn: "
					+ ChapelMirrorBlock.stop(lair, Mirror.EAST.base(o)));
			BlockPos lectern = o.offset(GlassChapel.LECTERN);
			check(LairRules.useBlock(player, lair, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(lectern), Direction.SOUTH, lectern, false))
					== InteractionResult.FAIL, "The Abbot's Lectern can be used");
			ChapelMirrorBlock.reset(lair, o);
			player.teleportTo(o.getX() + GlassChapel.CENTRE_X, o.getY() + GlassChapel.STAND, o.getZ() + GlassChapel.CENTRE_Z);
			Vec3 inNave = player.position();
			check(!Lairs.edges(player, first.get()) && player.position().distanceTo(inNave) < 1.0E-6, "A player in the nave was thrown back");
			Vec3 arrival = first.get().arrival();
			player.teleportTo(o.getX() + GlassChapel.CENTRE_X, o.getY() + CHAPEL.floor - 2.0, o.getZ() + GlassChapel.CENTRE_Z);
			player.setHealth(20.0F);
			check(Lairs.edges(player, first.get()) && player.position().distanceTo(arrival) < 0.5
					&& player.getHealth() == 20.0F - Lairs.EDGE_DAMAGE, "Falling out of the chapel did not end in the narthex");
			player.setHealth(20.0F);
		});

		// 4. Out, a second Accord refused at the open mist, and back in through the mist.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			check(Lairs.leave(player) && player.level() == minecraft.overworld() && player.position().distanceTo(stood.get()) < 1.0E-6,
					"The player did not come back to where they stood: " + player.position());
		});
		context.waitTicks(40);
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			ServerLevel overworld = minecraft.overworld();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.GLASS_ACCORD));
			check(GlassAccordRite.hold(overworld, player, player.getMainHandItem()).missing() == GlassAccordRite.Missing.MISTING
					&& player.getMainHandItem().getCount() == 1, "A second Accord at the open mist was not refused, or was used up");
			MistGateEntity mist = GlassAccordRite.mist(overworld, spot);
			check(mist != null, "The mist closed early");
			mist.use(player, InteractionHand.MAIN_HAND);
			check(player.level() == minecraft.getLevel(CHAPEL.dimension) && Lairs.instance(player.getAttached(Lairs.VISIT)) == first.get(),
					"The mist did not take the player back into the same chapel");
		});
		context.waitTicks(40);

		// 5. Closed, the player goes home and the mist is gone.
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			Lairs.close(minecraft, first.get());
			check(player.level() == minecraft.overworld(), "Closing the chapel did not send the player home");
		});
		context.waitTicks(50);
		server.runOnServer(minecraft -> {
			ServerLevel overworld = minecraft.overworld();
			check(GlassAccordRite.mist(overworld, spot) == null
					&& overworld.getEntitiesOfClass(MistGateEntity.class, new AABB(spot).inflate(3)).isEmpty(), "The mist outlived its chapel");
			player(minecraft).setHealth(20.0F);
			Lairs.reset();
		});
		context.waitTicks(20);
		Jugcraft.LOGGER.info("[lairs] client game test: the Glass Accord passed end to end");
	}

	/** Whether a player could stand in {@code cell}: nothing in it, or nothing to collide with. */
	private static boolean standable(ServerLevel level, BlockPos cell) {
		BlockState state = level.getBlockState(cell);
		return state.isAir() || state.is(Blocks.LIGHT) || state.getCollisionShape(level, cell).isEmpty();
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
