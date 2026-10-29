package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.AnimatronicWerewolfBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkFigure;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkFigureBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkRavenBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FlyingCrankGhostBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.OrganGrinderSkeletonBlock;
import io.github.jimbozoomer.jugcraft.agriculture.OrganGrinderSkeletonBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinDrummerBlock;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlock;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlockEntity;
import io.github.jimbozoomer.jugcraft.kinetic.JugcraftKinetics;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Game tests for the Clockwork Animatronics (decor set 9; docs/features/clockwork-animatronics.md), part 1: the
 * Organ-Grinder Skeleton is placed two blocks tall facing the player; it takes no more than its need of kinetic energy a
 * tick, runs only on all of it, plays while it runs and runs down after its power stops; a hand crank beside it drives
 * it; broken, it comes down whole and drops once; and its recipe and loot load. Part 2: the Animatronic Werewolf runs on
 * its own need, its howl rises, holds and falls within its cycle, and a hand crank drives it. Part 3: the Flying Crank
 * Ghost runs on its own need, from any side, and runs down; a hand crank behind it drives it; and it breaks whole. Part
 * 4: the Clockwork Raven runs on its own need, its caws, beak and wings keep time in its cycle, and a hand crank drives it.
 * Part 5: the Pumpkin-Headed Drummer runs on his own need, his mallets' strokes land on their beats and his head dips in
 * time, and a hand crank drives him.
 */
public class ClockworkGameTests {
	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	/** Places the figure from the player's hand on the block below {@code pos} (relative), and returns its lower block. */
	private static BlockPos placed(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		return placed(helper, player, pos, "organ_grinder_skeleton");
	}

	private static BlockPos placed(GameTestHelper helper, ServerPlayer player, BlockPos pos, String id) {
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftAgriculture.item(id)));
		BlockPos below = helper.absolutePos(pos.below());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
		return helper.absolutePos(pos);
	}

	private static OrganGrinderSkeletonBlockEntity clockwork(GameTestHelper helper, BlockPos lower) {
		if (helper.getLevel().getBlockEntity(lower) instanceof OrganGrinderSkeletonBlockEntity figure) {
			return figure;
		}
		throw helper.assertionException("The figure's clockwork is in its lower block");
	}

	private static boolean running(ServerLevel level, BlockPos lower) {
		BlockState below = level.getBlockState(lower);
		BlockState above = level.getBlockState(lower.above());
		return below.getValue(ClockworkFigure.RUNNING) && above.getValue(ClockworkFigure.RUNNING);
	}

	@GameTest(maxTicks = 80)
	public void theOrganGrinderRunsOnItsClockwork(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3));
		BlockState state = level.getBlockState(lower);
		helper.assertTrue(state.getBlock() instanceof OrganGrinderSkeletonBlock && state.getValue(OrganGrinderSkeletonBlock.HALF) == DoubleBlockHalf.LOWER
				&& level.getBlockState(lower.above()).getValue(OrganGrinderSkeletonBlock.HALF) == DoubleBlockHalf.UPPER
				&& state.getValue(OrganGrinderSkeletonBlock.FACING) == Direction.NORTH && !state.getValue(OrganGrinderSkeletonBlock.RUNNING),
				"It stands two blocks tall, facing the player, at rest");
		OrganGrinderSkeletonBlockEntity figure = clockwork(helper, lower);
		long first = figure.acceptKinetic(Direction.SOUTH, OrganGrinderSkeletonBlock.NEED - 1);
		helper.runAtTickTime(3, () -> helper.assertTrue(first == OrganGrinderSkeletonBlock.NEED - 1 && !running(level, lower),
				"Short of its need, it doesn't run"));
		// From tick 5 to 24 it gets its fill each tick, offered more than it needs.
		long[] took = new long[2];
		helper.onEachTick(() -> {
			long now = helper.getTick();
			if (now >= 5 && now < 25) {
				took[0] = figure.acceptKinetic(Direction.SOUTH, 16);
				took[1] = figure.acceptKinetic(Direction.EAST, 16);
			}
		});
		helper.runAtTickTime(20, () -> helper.assertTrue(took[0] == OrganGrinderSkeletonBlock.NEED && took[1] == 0 && running(level, lower)
				&& figure.played() > OrganGrinderSkeletonBlock.STEP_TICKS,
				"Fed, it takes its need a tick and no more, from any side, and runs: took " + took[0] + " then " + took[1]));
		helper.runAtTickTime(25 + OrganGrinderSkeletonBlock.SPIN_DOWN + 5, () -> {
			helper.assertTrue(!running(level, lower) && figure.played() == 0, "Its power stopped, it runs down and stops");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 60)
	public void aHandCrankDrivesTheOrganGrinder(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3));
		helper.setBlock(new BlockPos(4, 2, 3), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.WEST));
		if (!(level.getBlockEntity(helper.absolutePos(new BlockPos(4, 2, 3))) instanceof HandCrankBlockEntity crank)) {
			throw helper.assertionException("A hand crank stands beside it");
		}
		crank.addTurns(HandCrankBlockEntity.TICKS_PER_CRANK);
		helper.runAtTickTime(20, () -> {
			helper.assertTrue(running(level, lower), "A hand crank turning beside its lower block drives it");
			helper.succeed();
		});
	}

	@GameTest
	public void theOrganGrinderBreaksWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3));
		level.destroyBlock(lower.above(), true);
		helper.succeedWhen(() -> {
			int dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(lower).inflate(3.0),
					drop -> drop.getItem().is(JugcraftAgriculture.item("organ_grinder_skeleton"))).stream().mapToInt(drop -> drop.getItem().getCount()).sum();
			helper.assertTrue(level.getBlockState(lower).isAir() && level.getBlockState(lower.above()).isAir() && dropped == 1,
					"Broken at its upper half, it comes down whole and drops once: " + dropped);
		});
	}

	/** Part 2: the Animatronic Werewolf runs on its own need, howls in its cycle, and a hand crank beside it drives it. */
	@GameTest(maxTicks = 60)
	public void theWerewolfHowlsOnItsClockwork(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3), "animatronic_werewolf");
		BlockState state = level.getBlockState(lower);
		helper.assertTrue(state.getBlock() instanceof AnimatronicWerewolfBlock && state.getValue(AnimatronicWerewolfBlock.FACING) == Direction.NORTH
				&& !state.getValue(ClockworkFigure.RUNNING) && level.getBlockEntity(lower) instanceof ClockworkFigureBlockEntity,
				"It stands two blocks tall on its turntable, facing the player, at rest");
		ClockworkFigureBlockEntity figure = (ClockworkFigureBlockEntity) level.getBlockEntity(lower);
		long took = figure.acceptKinetic(Direction.SOUTH, 64);
		helper.assertTrue(took == AnimatronicWerewolfBlock.NEED && figure.acceptKinetic(Direction.SOUTH, 64) == 0,
				"It takes its need a tick and no more: " + took);
		int start = AnimatronicWerewolfBlock.howlStart();
		int top = start + AnimatronicWerewolfBlock.RAISE_TICKS;
		helper.assertTrue(AnimatronicWerewolfBlock.howl(0) == 0.0F && AnimatronicWerewolfBlock.howl(start) == 0.0F
				&& AnimatronicWerewolfBlock.howl(top) == 1.0F && AnimatronicWerewolfBlock.howl(top + AnimatronicWerewolfBlock.HOLD_TICKS) == 1.0F
				&& AnimatronicWerewolfBlock.howl(AnimatronicWerewolfBlock.HOWL_EVERY) == 0.0F
				&& AnimatronicWerewolfBlock.howl(start + AnimatronicWerewolfBlock.RAISE_TICKS / 2.0) > 0.0F,
				"Its howl rises, holds and falls within its cycle");
		helper.setBlock(new BlockPos(4, 2, 3), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.WEST));
		if (level.getBlockEntity(helper.absolutePos(new BlockPos(4, 2, 3))) instanceof HandCrankBlockEntity crank) {
			crank.addTurns(HandCrankBlockEntity.TICKS_PER_CRANK);
		}
		helper.runAtTickTime(20, () -> {
			helper.assertTrue(running(level, lower), "A hand crank turning beside its lower block drives it");
			helper.succeed();
		});
	}

	/** Part 3: the Flying Crank Ghost runs on its own need, runs down after it, and a hand crank behind it drives it. */
	@GameTest(maxTicks = 80)
	public void theCrankGhostFliesOnItsClockwork(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3), "flying_crank_ghost");
		BlockState state = level.getBlockState(lower);
		helper.assertTrue(state.getBlock() instanceof FlyingCrankGhostBlock && state.getValue(FlyingCrankGhostBlock.FACING) == Direction.NORTH
				&& level.getBlockState(lower.above()).getValue(FlyingCrankGhostBlock.HALF) == DoubleBlockHalf.UPPER
				&& !state.getValue(ClockworkFigure.RUNNING) && level.getBlockEntity(lower) instanceof ClockworkFigureBlockEntity,
				"It stands two blocks tall on its post, facing the player, at rest");
		ClockworkFigureBlockEntity figure = (ClockworkFigureBlockEntity) level.getBlockEntity(lower);
		long[] took = new long[2];
		helper.onEachTick(() -> {
			long now = helper.getTick();
			if (now >= 2 && now < 20) {
				took[0] = figure.acceptKinetic(Direction.WEST, 64);
				took[1] = figure.acceptKinetic(Direction.EAST, 64);
			}
		});
		helper.runAtTickTime(15, () -> helper.assertTrue(took[0] == FlyingCrankGhostBlock.NEED && took[1] == 0 && running(level, lower),
				"Fed, it takes its need a tick and no more, from any side, and runs: took " + took[0] + " then " + took[1]));
		helper.runAtTickTime(20 + FlyingCrankGhostBlock.SPIN_DOWN + 5, () -> {
			helper.assertTrue(!running(level, lower) && figure.played() == 0, "Its power stopped, it runs down and stops");
			// A hand crank behind its lower block, facing into it, as its gear housing is.
			helper.setBlock(new BlockPos(3, 2, 4), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.NORTH));
			if (level.getBlockEntity(helper.absolutePos(new BlockPos(3, 2, 4))) instanceof HandCrankBlockEntity crank) {
				crank.addTurns(HandCrankBlockEntity.TICKS_PER_CRANK);
			}
		});
		helper.runAtTickTime(20 + FlyingCrankGhostBlock.SPIN_DOWN + 25, () -> {
			helper.assertTrue(running(level, lower), "A hand crank turning behind its lower block drives it");
			level.destroyBlock(lower.above(), true);
		});
		helper.runAtTickTime(20 + FlyingCrankGhostBlock.SPIN_DOWN + 35, () -> {
			int dropped = level.getEntitiesOfClass(ItemEntity.class, new AABB(lower).inflate(3.0),
					drop -> drop.getItem().is(JugcraftAgriculture.item("flying_crank_ghost"))).stream().mapToInt(drop -> drop.getItem().getCount()).sum();
			helper.assertTrue(level.getBlockState(lower).isAir() && level.getBlockState(lower.above()).isAir() && dropped == 1,
					"Broken at its upper half, it comes down whole and drops once: " + dropped);
			helper.succeed();
		});
	}

	/** Part 4: the Clockwork Raven runs on its own need, caws in its cycle with its beak and wings, and a hand crank drives it. */
	@GameTest(maxTicks = 60)
	public void theRavenCawsOnItsClockwork(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3), "clockwork_raven");
		BlockState state = level.getBlockState(lower);
		helper.assertTrue(state.getBlock() instanceof ClockworkRavenBlock && state.getValue(ClockworkRavenBlock.FACING) == Direction.NORTH
				&& level.getBlockState(lower.above()).getValue(ClockworkRavenBlock.HALF) == DoubleBlockHalf.UPPER
				&& !state.getValue(ClockworkFigure.RUNNING) && level.getBlockEntity(lower) instanceof ClockworkFigureBlockEntity,
				"It stands two blocks tall on its stand, facing the player, at rest");
		ClockworkFigureBlockEntity figure = (ClockworkFigureBlockEntity) level.getBlockEntity(lower);
		long took = figure.acceptKinetic(Direction.SOUTH, 64);
		helper.assertTrue(took == ClockworkRavenBlock.NEED && figure.acceptKinetic(Direction.SOUTH, 64) == 0,
				"It takes its need a tick and no more: " + took);
		int burst = ClockworkRavenBlock.BURST;
		int every = ClockworkRavenBlock.CAW_EVERY;
		int last = burst + (ClockworkRavenBlock.CAWS - 1) * every;
		helper.assertTrue(ClockworkRavenBlock.cawsAt(burst) && ClockworkRavenBlock.cawsAt(last) && !ClockworkRavenBlock.cawsAt(burst - 1)
				&& !ClockworkRavenBlock.cawsAt(burst + 1) && !ClockworkRavenBlock.cawsAt(last + every) && !ClockworkRavenBlock.cawsAt(0),
				"It caws its burst's caws, and only those");
		helper.assertTrue(ClockworkRavenBlock.beak(0) == 0.0F && ClockworkRavenBlock.beak(burst) == 1.0F
				&& ClockworkRavenBlock.beak(burst + every / 2.0 + ClockworkRavenBlock.BEAK_HOLD / 2.0) == 0.0F && ClockworkRavenBlock.beak(last) == 1.0F,
				"Its beak opens for each caw and shuts between them");
		helper.assertTrue(ClockworkRavenBlock.wings(0) == 0.0F && Math.abs(ClockworkRavenBlock.wings(burst) - 1.0F) < 1.0E-6F
				&& ClockworkRavenBlock.wings(burst + every / 2.0) < 1.0E-6F && Math.abs(ClockworkRavenBlock.wings(last) - 1.0F) < 1.0E-6F
				&& ClockworkRavenBlock.wings(ClockworkRavenBlock.CYCLE - 1) == 0.0F,
				"Its wings beat once a caw, at the top on each, and fold between bursts");
		helper.setBlock(new BlockPos(4, 2, 3), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.WEST));
		if (level.getBlockEntity(helper.absolutePos(new BlockPos(4, 2, 3))) instanceof HandCrankBlockEntity crank) {
			crank.addTurns(HandCrankBlockEntity.TICKS_PER_CRANK);
		}
		helper.runAtTickTime(20, () -> {
			helper.assertTrue(running(level, lower), "A hand crank turning beside its lower block drives it");
			helper.succeed();
		});
	}

	/** Part 5: the Pumpkin-Headed Drummer runs on his own need, beats his march in time, and a hand crank drives him. */
	@GameTest(maxTicks = 60)
	public void theDrummerBeatsOnHisClockwork(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos lower = placed(helper, player, new BlockPos(3, 2, 3), "pumpkin_drummer");
		BlockState state = level.getBlockState(lower);
		helper.assertTrue(state.getBlock() instanceof PumpkinDrummerBlock && state.getValue(PumpkinDrummerBlock.FACING) == Direction.NORTH
				&& level.getBlockState(lower.above()).getValue(PumpkinDrummerBlock.HALF) == DoubleBlockHalf.UPPER
				&& !state.getValue(ClockworkFigure.RUNNING) && level.getBlockEntity(lower) instanceof ClockworkFigureBlockEntity,
				"He stands two blocks tall on his plinth, facing the player, at rest");
		ClockworkFigureBlockEntity figure = (ClockworkFigureBlockEntity) level.getBlockEntity(lower);
		long took = figure.acceptKinetic(Direction.SOUTH, 64);
		helper.assertTrue(took == PumpkinDrummerBlock.NEED && figure.acceptKinetic(Direction.SOUTH, 64) == 0,
				"He takes his need a tick and no more: " + took);
		int step = PumpkinDrummerBlock.STEP_TICKS;
		int half = PumpkinDrummerBlock.BACK_TICKS / 2;
		int right = PumpkinDrummerBlock.PATTERN.indexOf('R', 1) * step;
		int left = PumpkinDrummerBlock.PATTERN.indexOf('L') * step;
		helper.assertTrue(PumpkinDrummerBlock.beat(0) == 'R' && PumpkinDrummerBlock.beat(left / step) == 'L' && PumpkinDrummerBlock.beat(1) == '.'
				&& PumpkinDrummerBlock.beat(PumpkinDrummerBlock.PATTERN.length()) == 'R', "His march has its beats and rests, over and over");
		helper.assertTrue(PumpkinDrummerBlock.stroke(right, 'R') == 0.0F && Math.abs(PumpkinDrummerBlock.stroke(right - half, 'R') - 1.0F) < 1.0E-6F
				&& PumpkinDrummerBlock.stroke(left, 'L') == 0.0F && Math.abs(PumpkinDrummerBlock.stroke(left - half, 'L') - 1.0F) < 1.0E-6F
				&& PumpkinDrummerBlock.stroke(left - half, 'R') == 0.0F && PumpkinDrummerBlock.stroke(right + 1, 'R') == 0.0F,
				"Each mallet swings back before its own beats and lands on them");
		helper.assertTrue(PumpkinDrummerBlock.nod(0) == 1.0F && PumpkinDrummerBlock.nod(PumpkinDrummerBlock.NOD_STEPS * step / 2.0) < 1.0E-6F,
				"His head dips on the beat and lifts between");
		helper.setBlock(new BlockPos(4, 2, 3), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.WEST));
		if (level.getBlockEntity(helper.absolutePos(new BlockPos(4, 2, 3))) instanceof HandCrankBlockEntity crank) {
			crank.addTurns(HandCrankBlockEntity.TICKS_PER_CRANK);
		}
		helper.runAtTickTime(20, () -> {
			helper.assertTrue(running(level, lower), "A hand crank turning beside his lower block drives him");
			helper.succeed();
		});
	}

	@GameTest
	public void clockworkDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : new String[] {"organ_grinder_skeleton", "animatronic_werewolf", "flying_crank_ghost", "clockwork_raven", "pumpkin_drummer"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}
