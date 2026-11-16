package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToyBackdropBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToyCornTossBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToyCornTossBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToyPercussionBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToySpinningTopBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToys;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Thanksgiving festival toys (tools/thanksgiving_toys.py; docs/features/thanksgiving-toys.md): every one-block toy is
 * placed facing whoever placed it and, broken, drops itself once; the Spinning Top spins on a use, steps down every
 * frame to lying still in a bounded time and ignores a use while it spins; the Harvest Percussion Set plays what is
 * aimed at and then rests for its cooldown; the Tabletop Corn Toss Set scores tosses by turns from the server's own
 * view of the thrower, refuses tosses too close, too far, from behind, too soon or after the round, and a sneak-use
 * gathers the bags; the Harvest Photo Frame Backdrop places all ten blocks round an open middle, refuses blocked or
 * foreign ground, switches its bulbs, and breaks as one with one drop; in claimed land no outsider can use or break
 * any of them; and every recipe and loot table loads. A claim on any block of the backdrop, even one made after it was
 * placed, keeps all ten: an outsider can neither break an unclaimed block of it (in survival or in creative, which
 * ignores destroy progress) nor blow one up to bring the claimed rest down; water and buckets cannot wash its thin rail or
 * sill away; pistons cannot move it; and breaking and placing it again in one tick drops it once per break. The corn
 * toss is a public festival game in towns (#jugcraft:town_usable): a visitor tosses and gathers the bags but cannot break
 * it, while in a private claim an outsider still can do none of that.
 *
 * <p>Written for the headless server game tests; they have not been run here (no Minecraft or Fabric toolchain in the
 * environment these were written in). Each scheduled step is its own Runnable (GameTestInfo keys scheduled callbacks
 * by instance).
 */
public class FestivalToyGameTests {
	/** The toys of one block, in the table's order. */
	private static final List<String> SINGLE = List.of(FestivalToys.CORN_HUSK_DOLLS, FestivalToys.TURKEY_PULL_TOY, FestivalToys.SPINNING_TOP,
			FestivalToys.MAYFLOWER_MODEL_SHIP, FestivalToys.CORN_TOSS_SET, FestivalToys.PRIZE_PUMPKIN_DISPLAY, FestivalToys.HARVEST_PERCUSSION_SET);

	@GameTest(maxTicks = 40)
	public void everySingleBlockToyFacesItsPlacerAndDropsItself(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (String id : SINGLE) {
			place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(id)));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.is(block(id)) && placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH,
					id + " stands where it was placed, facing the player: " + placed);
			helper.assertTrue(player.getMainHandItem().isEmpty(), id + " is used up placing it");
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(id)) == 1, id + ", broken, drops itself once");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 200)
	public void theTopSpinsWindsDownAndLiesStill(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(FestivalToys.SPINNING_TOP)));
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToySpinningTopBlock.SPIN) == 0, "It is placed lying still");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToySpinningTopBlock.SPIN) == FestivalToySpinningTopBlock.STEPS,
				"A use spins it from the top step");
		helper.assertTrue(level.getBlockTicks().hasScheduledTick(at, block(FestivalToys.SPINNING_TOP)), "Its next step is scheduled");
		long start = level.getGameTime();
		List<Integer> seen = new ArrayList<>();
		seen.add(FestivalToySpinningTopBlock.STEPS);
		helper.onEachTick(() -> {
			BlockState state = level.getBlockState(at);
			if (state.is(block(FestivalToys.SPINNING_TOP))) {
				int spin = state.getValue(FestivalToySpinningTopBlock.SPIN);
				if (spin != seen.get(seen.size() - 1)) {
					seen.add(spin);
				}
			}
		});
		helper.runAfterDelay(5, () -> {
			int spin = level.getBlockState(at).getValue(FestivalToySpinningTopBlock.SPIN);
			helper.assertTrue(spin > 0 && spin < FestivalToySpinningTopBlock.STEPS, "Five ticks on it is still spinning, a few steps down: " + spin);
			use(player, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToySpinningTopBlock.SPIN) == spin, "A use while it spins changes nothing");
			helper.assertTrue(!FestivalToySpinningTopBlock.spin(level, at, level.getBlockState(at), player), "Nor does starting it again");
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToySpinningTopBlock.SPIN) == 0, "It winds down to lying still");
			long took = level.getGameTime() - start;
			helper.assertTrue(took >= FestivalToySpinningTopBlock.spinTicks() - 2 && took <= FestivalToySpinningTopBlock.spinTicks() + 2,
					"A spin takes its fixed time: " + took + " ticks, not " + FestivalToySpinningTopBlock.spinTicks());
			for (int i = 0; i < seen.size(); i++) {
				helper.assertTrue(seen.get(i) == FestivalToySpinningTopBlock.STEPS - i, "It steps down one frame at a time: " + seen);
			}
			helper.assertTrue(!level.getBlockTicks().hasScheduledTick(at, block(FestivalToys.SPINNING_TOP)), "Lying still, nothing more is scheduled");
		});
	}

	@GameTest(maxTicks = 60)
	public void thePercussionSetPlaysWhatIsAimedAtThenRests(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(FestivalToys.HARVEST_PERCUSSION_SET)));
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.NONE,
				"It is placed at rest");
		aimAt(player, level, at, 4.75, 9.0, 8.5);
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.DRUM,
				"Aimed at the drum, the drum plays");
		aimAt(player, level, at, 12.0, 8.0, 8.5);
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.DRUM,
				"Within its cooldown nothing else plays");
		helper.assertTrue(!FestivalToyPercussionBlock.play(level, at, level.getBlockState(at), FestivalToyPercussionBlock.Instrument.WASHBOARD),
				"Nor can it be played directly");
		helper.runAfterDelay(FestivalToyPercussionBlock.COOLDOWN + 2, () -> {
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.NONE,
					"After its cooldown it rests");
			aimAt(player, level, at, 12.0, 8.0, 8.5);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.WASHBOARD,
					"Aimed at the washboard, the washboard plays");
		});
		helper.runAfterDelay(2 * FestivalToyPercussionBlock.COOLDOWN + 6, () -> {
			aimAt(player, level, at, 7.0, 2.0, 2.0);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.SHAKER,
					"Aimed low at the front, the gourd shaker plays");
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(level.getGameTime() > 0 && level.getBlockState(at).getValue(FestivalToyPercussionBlock.STRUCK)
					== FestivalToyPercussionBlock.Instrument.NONE && !level.getBlockTicks().hasScheduledTick(at, block(FestivalToys.HARVEST_PERCUSSION_SET))
					&& helper.getTick() > 2 * FestivalToyPercussionBlock.COOLDOWN + 6, "And it comes to rest again, nothing left scheduled");
		});
	}

	@GameTest(maxTicks = 20)
	public void theAimTurnsWithTheSet(GameTestHelper helper) {
		BlockPos pos = new BlockPos(0, 64, 0);
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			// The drum's middle (4.75, 9, 8.5) in the set's own frame, turned to this facing as the blockstate turns its model.
			double[][] points = {{4.75, 9.0, 8.5}, {12.0, 8.0, 8.5}, {7.0, 2.0, 2.0}};
			FestivalToyPercussionBlock.Instrument[] wanted = {FestivalToyPercussionBlock.Instrument.DRUM,
					FestivalToyPercussionBlock.Instrument.WASHBOARD, FestivalToyPercussionBlock.Instrument.SHAKER};
			for (int i = 0; i < points.length; i++) {
				Vec3 world = world(pos, facing, points[i]);
				double[] back = FestivalToyPercussionBlock.local(pos, facing, world);
				helper.assertTrue(Math.abs(back[0] - points[i][0]) < 1.0E-6 && Math.abs(back[2] - points[i][2]) < 1.0E-6,
						"The set's frame turns back to itself facing " + facing);
				helper.assertTrue(FestivalToyPercussionBlock.aimed(pos, facing, world) == wanted[i], "Facing " + facing + " it plays the " + wanted[i]);
			}
		}
		helper.assertTrue(FestivalToyCornTossBlock.landing(FestivalToyCornTossBlock.HOLE_X, FestivalToyCornTossBlock.HOLE_Z)
				== FestivalToyCornTossBlock.Toss.HOLE, "The hole's middle is in the hole");
		helper.assertTrue(FestivalToyCornTossBlock.landing(5.0, 5.0) == FestivalToyCornTossBlock.Toss.BOARD, "The board away from the hole is on it");
		helper.assertTrue(FestivalToyCornTossBlock.landing(1.0, 1.0) == FestivalToyCornTossBlock.Toss.MISS, "Off the board is a miss");
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void theCornTossScoresTossesInTurnAndGathersTheBags(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 5));
		ServerPlayer rust = standing(helper, new BlockPos(3, 2, 3));
		place(rust, helper, new BlockPos(3, 2, 5), new ItemStack(item(FestivalToys.CORN_TOSS_SET)));
		FestivalToyCornTossBlockEntity game = (FestivalToyCornTossBlockEntity) level.getBlockEntity(at);
		helper.assertTrue(game != null && level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 0, "A new set has its bags racked");
		// Rust tosses from 3 blocks in front, looking at the hole's middle: in the hole.
		ServerPlayer thrower = standing(helper, new BlockPos(3, 2, 2));
		look(thrower, at, FestivalToyCornTossBlock.HOLE_X, FestivalToyCornTossBlock.AIM_HEIGHT, FestivalToyCornTossBlock.HOLE_Z);
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, level.getBlockState(at), thrower, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.HOLE,
				"Aimed at the hole from three blocks off, it goes in");
		BlockState once = level.getBlockState(at);
		helper.assertTrue(once.getValue(FestivalToyCornTossBlock.TOSSED) == 1 && game.rust() == FestivalToyCornTossBlock.HOLE_POINTS
				&& game.cream() == 0 && once.getValue(FestivalToyCornTossBlock.RUST_BOARD) == 0, "Rust scores three, the bag gone into the hole");
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, once, thrower, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.COOLING_DOWN
				&& level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 1, "A second toss in the same moment is refused");
		helper.runAfterDelay(FestivalToyCornTossBlock.COOLDOWN + 1, () -> {
			look(thrower, at, 5.0, FestivalToyCornTossBlock.AIM_HEIGHT, 5.0);
			helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, level.getBlockState(at), thrower, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.BOARD,
					"Aimed at the board, it lands on it");
			BlockState twice = level.getBlockState(at);
			helper.assertTrue(twice.getValue(FestivalToyCornTossBlock.TOSSED) == 2 && twice.getValue(FestivalToyCornTossBlock.CREAM_BOARD) == 1
					&& game.cream() == FestivalToyCornTossBlock.BOARD_POINTS && game.rust() == FestivalToyCornTossBlock.HOLE_POINTS,
					"It was cream's turn: cream scores one and its bag lies on the board");
			thrower.setShiftKeyDown(true);
			use(thrower, level, at, ItemStack.EMPTY);
			BlockState gathered = level.getBlockState(at);
			helper.assertTrue(gathered.getValue(FestivalToyCornTossBlock.TOSSED) == 0 && gathered.getValue(FestivalToyCornTossBlock.CREAM_BOARD) == 0
					&& game.rust() == 0 && game.cream() == 0, "A sneak-use gathers the bags and clears the score");
			helper.assertTrue(thrower.getInventory().isEmpty(), "Nothing is ever given");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theCornTossRefusesBadTosses(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 5));
		ServerPlayer placer = standing(helper, new BlockPos(3, 2, 3));
		place(placer, helper, new BlockPos(3, 2, 5), new ItemStack(item(FestivalToys.CORN_TOSS_SET)));
		BlockState state = level.getBlockState(at);
		ServerPlayer close = standing(helper, new BlockPos(3, 2, 4));
		look(close, at, 8.0, 5.7, 10.5);
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, state, close, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.TOO_CLOSE, "One block off is too close");
		ServerPlayer far = standing(helper, new BlockPos(3, 2, -1));
		look(far, at, 8.0, 5.7, 10.5);
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, state, far, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.TOO_FAR, "Six blocks off is too far");
		ServerPlayer behind = standing(helper, new BlockPos(3, 2, 8));
		look(behind, at, 8.0, 5.7, 10.5);
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, state, behind, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.NOT_IN_FRONT, "From behind is refused");
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 0, "None of them tossed a bag");
		level.setBlock(at, state.setValue(FestivalToyCornTossBlock.TOSSED, FestivalToyCornTossBlock.TOSSES), Block.UPDATE_ALL);
		ServerPlayer fair = standing(helper, new BlockPos(3, 2, 2));
		look(fair, at, 8.0, 5.7, 10.5);
		helper.assertTrue(FestivalToyCornTossBlock.toss(level, at, level.getBlockState(at), fair, 0.0, 0.0) == FestivalToyCornTossBlock.Toss.ROUND_OVER,
				"After the round nothing more is tossed until the bags are gathered");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theCornTossUseTossesOneBagAtATime(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 5));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 2));
		place(player, helper, new BlockPos(3, 2, 5), new ItemStack(item(FestivalToys.CORN_TOSS_SET)));
		look(player, at, 8.0, 5.7, 10.5);
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 1, "A use from three blocks in front tosses a bag");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 1, "A second use within the cooldown tosses none");
		helper.runAfterDelay(FestivalToyCornTossBlock.COOLDOWN + 1, () -> {
			use(player, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 2, "After the cooldown the next bag goes");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theBackdropStandsRoundAnOpenMiddleAndComesDownAsOne(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		Block backdrop = block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		MultiDecorationBlock frame = (MultiDecorationBlock) backdrop;
		ServerPlayer player = standing(helper, new BlockPos(6, 2, 1));
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		helper.assertTrue(player.getMainHandItem().isEmpty(), "Placing it uses the item");
		for (int part = 0; part < FestivalToyBackdropBlock.PARTS; part++) {
			BlockState state = level.getBlockState(frame.partPos(master, Direction.NORTH, part));
			helper.assertTrue(state.is(backdrop) && state.getValue(FestivalToyBackdropBlock.PART) == part
					&& state.getValue(FestivalToyBackdropBlock.FACING) == Direction.NORTH, "Part " + part + " stands in its place: " + state);
		}
		// The open middle: one and two blocks to the placer's right, one up, are not part of it.
		for (int right = 1; right <= 2; right++) {
			BlockPos open = master.relative(Direction.WEST, right).above();
			helper.assertTrue(level.getBlockState(open).isAir(), "The middle is open at " + open);
			BlockPos sill = master.relative(Direction.WEST, right);
			helper.assertTrue(level.getBlockState(sill).getCollisionShape(level, sill).max(Direction.Axis.Y) <= 2.0 / 16 + 1.0E-6,
					"The sill under it is only its own two pixels high");
		}
		BlockPos rail = frame.partPos(master, Direction.NORTH, FestivalToyBackdropBlock.BULBS_LEFT);
		helper.assertTrue(level.getBlockState(rail).getLightEmission() == FestivalToyBackdropBlock.LIGHT, "Its bulbs are lit when placed");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(!level.getBlockState(rail).getValue(FestivalToyBackdropBlock.LIT) && level.getBlockState(rail).getLightEmission() == 0
				&& !level.getBlockState(master).getValue(FestivalToyBackdropBlock.LIT), "A use switches every part's bulbs off");
		level.destroyBlock(rail, true);
		for (int part = 0; part < FestivalToyBackdropBlock.PARTS; part++) {
			helper.assertTrue(level.getBlockState(frame.partPos(master, Direction.NORTH, part)).isAir(), "Breaking the top rail takes down part " + part);
		}
		helper.assertTrue(dropped(helper, item(FestivalToys.HARVEST_PHOTO_BACKDROP)) == 1, "It drops itself once");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theBackdropRefusesBlockedGround(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		MultiDecorationBlock frame = (MultiDecorationBlock) block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		BlockPos blocked = frame.partPos(master, Direction.NORTH, 5);
		level.setBlockAndUpdate(blocked, Blocks.OAK_PLANKS.defaultBlockState());
		ServerPlayer player = standing(helper, new BlockPos(6, 2, 1));
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
		helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(blocked).is(Blocks.OAK_PLANKS),
				"With a block where it would go, nothing is placed and nothing replaced");
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "The item is kept");
		for (int part = 1; part < FestivalToyBackdropBlock.PARTS; part++) {
			BlockPos at = frame.partPos(master, Direction.NORTH, part);
			helper.assertTrue(at.equals(blocked) || level.getBlockState(at).isAir(), "No orphan part is left at " + at);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void claimsKeepTheToysToTheirMembers(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = survivor(helper);
		ServerPlayer stranger = survivor(helper);
		BlockPos base = helper.absolutePos(new BlockPos(20480, 3, 20480));
		BlockPos origin = new BlockPos(Math.floorDiv(base.getX(), 16) * 16 + 4, base.getY(), Math.floorDiv(base.getZ(), 16) * 16 + 4);
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(origin);
		try {
			book.account(stranger.getUUID(), "ToyVisitor");
			land.assign(book, key, book.account(owner.getUUID(), "ToyOwner"), 0);
			for (int x = 0; x <= 8; x++) {
				for (int z = -3; z <= 3; z++) {
					level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
				}
			}
			BlockPos top = origin, drum = origin.east(3), toss = origin.east(6);
			level.setBlockAndUpdate(top, block(FestivalToys.SPINNING_TOP).defaultBlockState());
			level.setBlockAndUpdate(drum, block(FestivalToys.HARVEST_PERCUSSION_SET).defaultBlockState());
			level.setBlockAndUpdate(toss, block(FestivalToys.CORN_TOSS_SET).defaultBlockState());
			stranger.snapTo(Vec3.atCenterOf(origin.north(3)));
			use(stranger, level, top, ItemStack.EMPTY);
			use(stranger, level, drum, ItemStack.EMPTY);
			use(stranger, level, toss, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(top).getValue(FestivalToySpinningTopBlock.SPIN) == 0
					&& level.getBlockState(drum).getValue(FestivalToyPercussionBlock.STRUCK) == FestivalToyPercussionBlock.Instrument.NONE
					&& level.getBlockState(toss).getValue(FestivalToyCornTossBlock.TOSSED) == 0, "An outsider cannot spin, play or toss on claimed land");
			for (BlockPos at : List.of(top, drum, toss)) {
				helper.assertTrue(!stranger.gameMode.destroyBlock(at) && !level.getBlockState(at).isAir(), "Nor break what stands there: " + at);
			}
			use(owner, level, top, ItemStack.EMPTY);
			helper.assertTrue(FestivalToySpinningTopBlock.spinning(level.getBlockState(top)), "Its owner spins the top");
			helper.assertTrue(owner.gameMode.destroyBlock(drum) && level.getBlockState(drum).isAir(), "Its owner can take the percussion set away");
			// In a town a toy without a block entity is a public interaction; the corn toss, whose block entity keeps only
			// scores, is public through #jugcraft:town_usable. Public in a town is not public in a claim (above).
			helper.assertTrue(TownProtection.publicBlock(level, top, level.getBlockState(top)), "Spinning a top is public in a town");
			helper.assertTrue(TownProtection.publicBlock(level, toss, level.getBlockState(toss)), "So is the corn toss");
		} finally {
			land.abandon(owner, book, key, true, 0);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aTownVisitorTossesAndGathersTheCornTossButCannotBreakIt(GameTestHelper helper) throws Exception {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 5));
		place(standing(helper, new BlockPos(3, 2, 3)), helper, new BlockPos(3, 2, 5), new ItemStack(item(FestivalToys.CORN_TOSS_SET)));
		BlockState set = level.getBlockState(at);
		helper.assertTrue(set.is(block(FestivalToys.CORN_TOSS_SET)) && set.getValue(FacingDecorationBlock.FACING) == Direction.NORTH, "The set stands, facing north");
		helper.assertTrue(set.is(TagKey.create(Registries.BLOCK, Jugcraft.id("town_usable"))), "The corn toss is in #jugcraft:town_usable");
		FestivalToyCornTossBlockEntity game = (FestivalToyCornTossBlockEntity) level.getBlockEntity(at);
		helper.assertTrue(!(game instanceof net.minecraft.world.Container) && TownProtection.publicBlock(level, at, set),
				"Its block entity holds no inventory, so the tag makes it public");
		// Protected public ground, as the operators' /protect regions make it: they share TownProtection's rules with towns
		// (denies, publicBlock, the public use path), without placing the world's one town.
		var server = level.getServer();
		var dispatch = server.getCommands().getDispatcher();
		ServerPlayer warden = TestPlayers.mock(helper);
		var op = server.createCommandSourceStack().withEntity(warden).withSuppressedOutput();
		String region = "toss_" + warden.getUUID().toString().replace("-", "");
		BlockPos low = helper.absolutePos(new BlockPos(0, 1, 0));
		BlockPos high = helper.absolutePos(new BlockPos(7, 5, 7));
		warden.snapTo(low.getX(), low.getY(), low.getZ(), 0, 0);
		dispatch.execute("protect pos1", op);
		warden.snapTo(high.getX(), high.getY(), high.getZ(), 0, 0);
		dispatch.execute("protect pos2", op);
		dispatch.execute("protect create " + region, op);
		ServerPlayer visitor = TestPlayers.survivor(helper);
		try {
			visitor.snapTo(Vec3.atBottomCenterOf(at.north(3)));
			helper.assertTrue(TownProtection.denies(visitor, level, at) && TownProtection.shieldsBlock(level, at),
					"Fixture: the visitor may not build where the set stands");
			look(visitor, at, FestivalToyCornTossBlock.HOLE_X, FestivalToyCornTossBlock.AIM_HEIGHT, FestivalToyCornTossBlock.HOLE_Z);
			use(visitor, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 1, "A town visitor tosses a bag");
			visitor.setShiftKeyDown(true);
			use(visitor, level, at, ItemStack.EMPTY);
			BlockState gathered = level.getBlockState(at);
			helper.assertTrue(gathered.getValue(FestivalToyCornTossBlock.TOSSED) == 0 && game.rust() == 0 && game.cream() == 0,
					"and gathers the bags with a sneak-use");
			visitor.setShiftKeyDown(false);
			helper.assertTrue(!visitor.gameMode.destroyBlock(at) && level.getBlockState(at).is(block(FestivalToys.CORN_TOSS_SET)),
					"Playing it gives no right to break it");
			helper.assertTrue(visitor.getInventory().isEmpty(), "Nothing is given or taken");
		} finally {
			visitor.setShiftKeyDown(false);
			dispatch.execute("protect remove " + region, op);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aStrangerInAPrivateClaimStillCannotTossGatherOrBreakTheCornToss(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = TestPlayers.survivor(helper);
		ServerPlayer stranger = TestPlayers.survivor(helper);
		BlockPos base = helper.absolutePos(new BlockPos(28672, 3, 28672));
		BlockPos at = new BlockPos(Math.floorDiv(base.getX(), 16) * 16 + 8, base.getY(), Math.floorDiv(base.getZ(), 16) * 16 + 8);
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(at);
		try {
			book.account(stranger.getUUID(), "TossStranger");
			land.assign(book, key, book.account(owner.getUUID(), "TossOwner"), 0);
			for (int dz = -4; dz <= 1; dz++) {
				for (int dy = -1; dy <= 2; dy++) {
					level.setBlockAndUpdate(at.offset(0, dy, dz), dy < 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
				}
			}
			// A round under way: three bags thrown, rust three points up, the last toss long past.
			level.setBlockAndUpdate(at, block(FestivalToys.CORN_TOSS_SET).defaultBlockState().setValue(FacingDecorationBlock.FACING, Direction.NORTH)
					.setValue(FestivalToyCornTossBlock.TOSSED, 3));
			FestivalToyCornTossBlockEntity game = (FestivalToyCornTossBlockEntity) level.getBlockEntity(at);
			game.scored(true, FestivalToyCornTossBlock.HOLE_POINTS, level.getGameTime() - 100);
			helper.assertTrue(TownProtection.publicBlock(level, at, level.getBlockState(at)), "Fixture: the set is a public town game");
			stranger.snapTo(Vec3.atBottomCenterOf(at.north(3)));
			look(stranger, at, FestivalToyCornTossBlock.HOLE_X, FestivalToyCornTossBlock.AIM_HEIGHT, FestivalToyCornTossBlock.HOLE_Z);
			use(stranger, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 3 && game.rust() == FestivalToyCornTossBlock.HOLE_POINTS,
					"A stranger in a private claim cannot toss");
			stranger.setShiftKeyDown(true);
			use(stranger, level, at, ItemStack.EMPTY);
			stranger.setShiftKeyDown(false);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 3 && game.rust() == FestivalToyCornTossBlock.HOLE_POINTS,
					"nor gather the bags and clear the score");
			helper.assertTrue(!stranger.gameMode.destroyBlock(at) && level.getBlockState(at).is(block(FestivalToys.CORN_TOSS_SET)), "nor break it");
			// From the same place, the owner's toss counts: the stranger was refused for the claim, not for the throw.
			owner.snapTo(Vec3.atBottomCenterOf(at.north(3)));
			look(owner, at, FestivalToyCornTossBlock.HOLE_X, FestivalToyCornTossBlock.AIM_HEIGHT, FestivalToyCornTossBlock.HOLE_Z);
			use(owner, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(FestivalToyCornTossBlock.TOSSED) == 4, "Its owner tosses the next bag");
		} finally {
			land.abandon(owner, book, key, true, 0);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theBackdropNeverReachesIntoAnotherClaim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = survivor(helper);
		ServerPlayer neighbour = survivor(helper);
		BlockPos base = helper.absolutePos(new BlockPos(22528, 3, 22528));
		int chunkX = Math.floorDiv(base.getX(), 16) * 16;
		int z = Math.floorDiv(base.getZ(), 16) * 16 + 8;
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long mine = LandRegistry.keyOf(new BlockPos(chunkX + 8, base.getY(), z));
		long theirs = LandRegistry.keyOf(new BlockPos(chunkX - 8, base.getY(), z));
		Block backdrop = block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		MultiDecorationBlock frame = (MultiDecorationBlock) backdrop;
		try {
			land.assign(book, mine, book.account(owner.getUUID(), "FrameOwner"), 0);
			land.assign(book, theirs, book.account(neighbour.getUUID(), "FrameNeighbour"), 0);
			for (int x = chunkX - 6; x <= chunkX + 10; x++) {
				for (int dy = 0; dy <= 3; dy++) {
					level.setBlockAndUpdate(new BlockPos(x, base.getY() + dy, z), dy == 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
				}
			}
			// Facing north, the frame runs from the block aimed at to the placer's right, the west: from x + 1, into the
			// neighbour's chunk.
			BlockPos edge = new BlockPos(chunkX + 1, base.getY() + 1, z);
			owner.snapTo(Vec3.atBottomCenterOf(edge.north(3)));
			owner.setYRot(0.0F);
			placeFrom(owner, level, edge, new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
			helper.assertTrue(level.getBlockState(edge).isAir() && level.getBlockState(edge.west(2)).isAir(),
					"A frame that would reach into someone else's claim is not placed");
			BlockPos inside = new BlockPos(chunkX + 6, base.getY() + 1, z);
			owner.snapTo(Vec3.atBottomCenterOf(inside.north(3)));
			owner.setYRot(0.0F);
			placeFrom(owner, level, inside, new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
			helper.assertTrue(level.getBlockState(inside).is(backdrop) && level.getBlockState(frame.partPos(inside, Direction.NORTH, 7)).is(backdrop),
					"Wholly in its owner's chunk it is placed");
			helper.assertTrue(!neighbour.gameMode.destroyBlock(inside) && level.getBlockState(inside).is(backdrop), "An outsider cannot break it");
		} finally {
			land.abandon(owner, book, mine, true, 0);
			land.abandon(neighbour, book, theirs, true, 0);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aClaimOnAnyBlockOfTheBackdropKeepsItWholeFromBreaking(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = TestPlayers.survivor(helper);
		ServerPlayer stranger = TestPlayers.survivor(helper);
		MultiDecorationBlock frame = (MultiDecorationBlock) block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		BlockPos master = straddling(helper, new BlockPos(24576, 3, 24576));
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long claim = LandRegistry.keyOf(master);
		// Remote claim fixtures need entity-ticking chunks before testing item drops.
		int cx = Math.floorDiv(master.getX(), 16), cz = Math.floorDiv(master.getZ(), 16);
		for (int dx = -1; dx <= 1; dx++) level.setChunkForced(cx + dx, cz, true);
		helper.runAtTickTime(10, () -> {
		try {
			placeStraddling(owner, level, master);
			helper.assertTrue(whole(level, frame, master), "Placed on unclaimed ground, the frame stands whole");
			// The owner claims the master's chunk after placing it; its two right-hand columns stay on unclaimed land.
			land.assign(book, claim, book.account(owner.getUUID(), "BackdropOwner"), 0);
			BlockPos post = frame.partPos(master, Direction.NORTH, 7);
			helper.assertTrue(LandRegistry.isClaimed(level, master) && !LandRegistry.isClaimed(level, post),
					"Fixture: the master's chunk is claimed and the far post's is not");
			stranger.snapTo(Vec3.atBottomCenterOf(post.north(2)));
			helper.assertTrue(level.getBlockState(post).getDestroyProgress(stranger, level, post) == 0.0F,
					"An outsider makes no progress breaking the unclaimed post");
			helper.assertTrue(!stranger.gameMode.destroyBlock(post) && whole(level, frame, master),
					"An outsider in survival cannot break the unclaimed post (and so the claimed frame)");
			stranger.setGameMode(GameType.CREATIVE);
			helper.assertTrue(stranger.isCreative() && !TownProtection.exempt(stranger), "Fixture: an ordinary player in creative, not an operator");
			helper.assertTrue(!stranger.gameMode.destroyBlock(post) && whole(level, frame, master),
					"Nor can an ordinary player in creative, whose instant break never asks for destroy progress");
			helper.assertTrue(droppedNear(level, master, item(FestivalToys.HARVEST_PHOTO_BACKDROP)) == 0, "Nothing dropped while it stood");
			helper.assertTrue(level.getBlockState(post).getDestroyProgress(owner, level, post) > 0.0F, "Its owner makes progress breaking it");
			helper.assertTrue(owner.gameMode.destroyBlock(post) && gone(level, frame, master), "Its owner takes the whole frame down, no part left");
			helper.assertTrue(droppedNear(level, master, item(FestivalToys.HARVEST_PHOTO_BACKDROP)) == 1, "It drops itself once");
		} finally {
			land.abandon(owner, book, claim, true, 0);
			clearDropsNear(level, master);
			for (int dx = -1; dx <= 1; dx++) level.setChunkForced(cx + dx, cz, false);
		}
		helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void anExplosionLeavesTheBackdropWhileAnyBlockOfItIsClaimed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = TestPlayers.survivor(helper);
		MultiDecorationBlock frame = (MultiDecorationBlock) block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		Item item = item(FestivalToys.HARVEST_PHOTO_BACKDROP);
		BlockPos master = straddling(helper, new BlockPos(26624, 3, 26624));
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long claim = LandRegistry.keyOf(master);
		boolean claimed = false;
		try {
			placeStraddling(owner, level, master);
			land.assign(book, claim, book.account(owner.getUUID(), "BackdropOwner"), 0);
			claimed = true;
			// The open middle's right-hand block, on the unclaimed side: the blast reaches the unclaimed post, sill and rail.
			BlockPos middle = master.relative(Direction.WEST, 2).above();
			helper.assertTrue(!LandRegistry.isClaimed(level, middle), "Fixture: the blast is on unclaimed land");
			level.explode(null, middle.getX() + 0.5, middle.getY() + 0.5, middle.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
			helper.assertTrue(whole(level, frame, master), "With one block claimed, an explosion takes no block of it");
			helper.assertTrue(droppedNear(level, master, item) == 0, "and drops nothing");
			land.abandon(owner, book, claim, true, 0);
			claimed = false;
			level.explode(null, middle.getX() + 0.5, middle.getY() + 0.5, middle.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
			helper.assertTrue(gone(level, frame, master), "Unclaimed, the explosion takes it all, no part left standing");
			helper.assertTrue(droppedNear(level, master, item) <= 1, "and it drops at most once");
		} finally {
			if (claimed) {
				land.abandon(owner, book, claim, true, 0);
			}
			clearDropsNear(level, master);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void nothingWashesOrPushesABlockOfTheBackdropAway(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		MultiDecorationBlock frame = (MultiDecorationBlock) block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		ServerPlayer player = TestPlayers.survivor(helper);
		BlockPos stand = helper.absolutePos(new BlockPos(6, 2, 1));
		player.snapTo(Vec3.atBottomCenterOf(stand));
		player.setYRot(0.0F);
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		helper.assertTrue(whole(level, frame, master), "The frame stands");
		BlockPos rail = frame.partPos(master, Direction.NORTH, FestivalToyBackdropBlock.BULBS_LEFT);
		BlockPos sill = frame.partPos(master, Direction.NORTH, 8);
		for (int part = 0; part < FestivalToyBackdropBlock.PARTS; part++) {
			BlockPos at = frame.partPos(master, Direction.NORTH, part);
			helper.assertTrue(level.getBlockState(at).isSolid(), "Part " + part + " counts as solid, so no fluid takes its place");
			helper.assertTrue(!PistonBaseBlock.isPushable(level.getBlockState(at), level, at, Direction.EAST, false, Direction.EAST),
					"No piston moves part " + part);
		}
		helper.assertTrue(!((BucketItem) Items.WATER_BUCKET).emptyContents(player, level, sill, null) && whole(level, frame, master),
				"Water emptied onto the sill does not replace it");
		helper.assertTrue(!((BucketItem) Items.LAVA_BUCKET).emptyContents(player, level, rail, null) && whole(level, frame, master),
				"Nor does lava emptied onto the top rail");
		// Water in the open middle, over the sill, and over the top rail: it flows round, never into, the frame.
		BlockPos overSill = sill.above(), overRail = rail.above();
		for (BlockPos source : List.of(overSill, overRail)) {
			level.setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
			Fluids.WATER.tick(level, source, level.getBlockState(source), level.getFluidState(source));
		}
		helper.assertTrue(whole(level, frame, master) && level.getFluidState(sill).isEmpty() && level.getFluidState(rail).isEmpty(),
				"Flowing water leaves the sill and the rail as they were");
		for (BlockPos source : List.of(overSill, overRail)) {
			level.setBlockAndUpdate(source, Blocks.AIR.defaultBlockState());
		}
		helper.assertTrue(droppedNear(level, master, item(FestivalToys.HARVEST_PHOTO_BACKDROP)) == 0, "Nothing dropped");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theBackdropBrokenAndPlacedAgainInOneTickDropsOncePerBreak(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		MultiDecorationBlock frame = (MultiDecorationBlock) block(FestivalToys.HARVEST_PHOTO_BACKDROP);
		Item item = item(FestivalToys.HARVEST_PHOTO_BACKDROP);
		ServerPlayer player = TestPlayers.survivor(helper);
		BlockPos stand = helper.absolutePos(new BlockPos(6, 2, 1));
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		player.snapTo(Vec3.atBottomCenterOf(stand));
		player.setYRot(0.0F);
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item));
		helper.assertTrue(whole(level, frame, master) && player.getMainHandItem().isEmpty(), "Placed, using the item");
		helper.assertTrue(player.gameMode.destroyBlock(frame.partPos(master, Direction.NORTH, 4)) && gone(level, frame, master),
				"Broken at its rail, it all comes down at once");
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item));
		helper.assertTrue(whole(level, frame, master), "In the same tick a new one stands in its place");
		helper.assertTrue(player.gameMode.destroyBlock(master) && gone(level, frame, master), "Broken at its master, it all comes down");
		helper.assertTrue(droppedNear(level, master, item) == 2, "Two breaks, two drops: nothing more");
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void everyRecipeAndLootTableLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : FestivalToys.ALL) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer survivor(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		return player;
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = survivor(helper);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setYRot(0.0F);
		return player;
	}

	/** Uses {@code held} on the block at {@code absolute} from its north face, through the server's normal use path. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
	}

	/** Uses an empty hand on the set at {@code absolute}, aimed at {x, y, z} pixels in its frame (it faces north here). */
	private static void aimAt(ServerPlayer player, ServerLevel level, BlockPos absolute, double x, double y, double z) {
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		Vec3 hit = new Vec3(absolute.getX() + x / 16.0, absolute.getY() + y / 16.0, absolute.getZ() + z / 16.0);
		player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.NORTH, absolute, false));
	}

	/** Turns {@code player} to look at {x, y, z} pixels in the frame of the block at {@code absolute} (facing north). */
	private static void look(ServerPlayer player, BlockPos absolute, double x, double y, double z) {
		Vec3 aim = new Vec3(absolute.getX() + x / 16.0, absolute.getY() + y / 16.0, absolute.getZ() + z / 16.0).subtract(player.getEyePosition());
		player.setYRot((float) Math.toDegrees(Math.atan2(-aim.x, aim.z)));
		player.setYHeadRot(player.getYRot());
		player.setXRot((float) Math.toDegrees(Math.atan2(-aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z))));
	}

	/** A point given in pixels in the frame of a block facing north, turned to {@code facing} as its blockstate turns its model. */
	private static Vec3 world(BlockPos pos, Direction facing, double[] p) {
		double x = p[0], z = p[2];
		double wx = switch (facing) {
			case EAST -> 16.0 - z;
			case SOUTH -> 16.0 - x;
			case WEST -> z;
			default -> x;
		};
		double wz = switch (facing) {
			case EAST -> x;
			case SOUTH -> 16.0 - z;
			case WEST -> 16.0 - x;
			default -> z;
		};
		return new Vec3(pos.getX() + wx / 16.0, pos.getY() + p[1] / 16.0, pos.getZ() + wz / 16.0);
	}

	/** Places {@code held} from the player's hand on the top of the block below {@code pos} (relative). */
	private static void place(ServerPlayer player, GameTestHelper helper, BlockPos pos, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos below = helper.absolutePos(pos.below());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
	}

	/** Places {@code held} on top of the block below {@code absolute} through the server's normal use path (claims apply). */
	private static void placeFrom(ServerPlayer player, ServerLevel level, BlockPos absolute, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos below = absolute.below();
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false));
	}

	/**
	 * A master position for a frame facing north that straddles a chunk edge, near {@code far} (relative): the master
	 * and its first right-hand column stand in one chunk, its other two columns in the chunk to the west. The ground under
	 * it is stone and the room for it and its placer is cleared.
	 */
	private static BlockPos straddling(GameTestHelper helper, BlockPos far) {
		ServerLevel level = helper.getLevel();
		BlockPos base = helper.absolutePos(far);
		int chunkX = Math.floorDiv(base.getX(), 16) * 16;
		int z = Math.floorDiv(base.getZ(), 16) * 16 + 8;
		for (int x = chunkX - 6; x <= chunkX + 6; x++) {
			for (int dz = -4; dz <= 2; dz++) {
				for (int dy = 0; dy <= 5; dy++) {
					level.setBlockAndUpdate(new BlockPos(x, base.getY() + dy, z + dz), dy == 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
				}
			}
		}
		return new BlockPos(chunkX + 1, base.getY() + 1, z);
	}

	/** {@code player} places a frame with its master at {@code master}, facing north, through the server's use path. */
	private static void placeStraddling(ServerPlayer player, ServerLevel level, BlockPos master) {
		player.snapTo(Vec3.atBottomCenterOf(master.north(3)));
		player.setYRot(0.0F);
		placeFrom(player, level, master, new ItemStack(item(FestivalToys.HARVEST_PHOTO_BACKDROP)));
	}

	/** Whether every block of the frame facing north with its master at {@code master} stands in its place. */
	private static boolean whole(ServerLevel level, MultiDecorationBlock frame, BlockPos master) {
		for (int part = 0; part < FestivalToyBackdropBlock.PARTS; part++) {
			BlockState state = level.getBlockState(frame.partPos(master, Direction.NORTH, part));
			if (!state.is(frame) || state.getValue(FestivalToyBackdropBlock.PART) != part || state.getValue(FestivalToyBackdropBlock.FACING) != Direction.NORTH) {
				return false;
			}
		}
		return true;
	}

	/** Whether no block of that frame is left: none of its places holds a frame block. */
	private static boolean gone(ServerLevel level, MultiDecorationBlock frame, BlockPos master) {
		for (int part = 0; part < FestivalToyBackdropBlock.PARTS; part++) {
			if (level.getBlockState(frame.partPos(master, Direction.NORTH, part)).is(frame)) {
				return false;
			}
		}
		return true;
	}

	private static int droppedNear(ServerLevel level, BlockPos at, Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(6.0), entity -> entity.getItem().is(item)).stream()
				.mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clearDropsNear(ServerLevel level, BlockPos at) {
		level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(6.0)).forEach(ItemEntity::discard);
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(6.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clearDrops(GameTestHelper helper) {
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(6.0))
				.forEach(ItemEntity::discard);
	}
}
