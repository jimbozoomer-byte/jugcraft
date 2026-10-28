package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CottonCandyCartBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GhostTrainBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GhostTrainBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PopcornWagonBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.TicketBoothBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WhackAZombieBlock;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Haunted Midway, part 1 (decor set 6; docs/features/haunted-midway.md): the Cotton-Candy Cart stands two blocks high,
 * facing the player, switched off; sugar isn't spun while it is off; an empty hand switches it on, on both blocks; three
 * sugars are spun into floss and a fourth isn't; Paper winds a serving onto a cone, a Cotton Candy, two times; switched
 * off, broken at its canopy it comes away whole, giving back itself and a sugar for the serving left. The Caramel-Corn
 * Wagon stands two blocks high, out; a kernel isn't taken while it is out; flint and steel lights both blocks, wearing the
 * flint, its upper block alone giving light {@value PopcornWagonBlock#LIGHT}; a kernel goes in the kettle and a second
 * isn't taken while it pops; after {@value PopcornWagonBlock#POP_TICKS} ticks it has popped into the case, the kettle's
 * spill marked; an empty hand takes the Popcorn; Caramel isn't spent on fewer than two; two more kernels pop and Caramel
 * coats the two into a Caramel Popcorn; a shovel puts it out. Broken while popping with three in its case, it gives back
 * itself, the three Popcorn and the kernel.
 * Part 2: the Ticket Booth won't print with an empty roll; three sheets load six tickets; an empty hand prints one, and
 * the crank must come to rest before the next; broken with four left, it gives back itself and two sheets. Whack-a-Zombie
 * stands out; an empty hand starts nothing; a ticket starts a round and a second isn't taken while it runs; a head pops
 * up; another player's whack doesn't count; a whack on the wrong hole doesn't count; the player who paid knocks each head
 * down as it comes, lighting a lamp each, and all eight win a prize from the midway's prize table and end the round.
 * Left alone, a round's time runs out: no head is up, no lamp lit and no prize given.
 * Part 3: the Shooting Gallery stands three blocks across and two up, out; a ticket starts a round; a shot from too close,
 * another player's shot and a shot between the targets knock nothing down; the player who paid knocks each target down
 * as it comes into sight, and all eight down win a prize and end the round, every target standing again. Left alone, a
 * round's time runs out with no prize.
 * Part 4: the Ghost Train stands five blocks across and four up, its two doorways left free (a rail laid in one stays);
 * a minecart in the IN doorway opens its doors and springs its ghost, the OUT doors staying shut; gone, the doors shut
 * again; an empty hand lights its windows, its window blocks giving light; broken, it comes away whole and the rail
 * in its doorway stays.
 */
public class HauntedMidwayGameTests {
	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

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

	/** Uses {@code held} on the block at {@code absolute} (an absolute position) from its front. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
	}

	/** Places {@code held} from the player's hand on the top of the block below {@code pos} (relative). */
	private static void place(ServerPlayer player, GameTestHelper helper, BlockPos pos, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos below = helper.absolutePos(pos.below());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
	}

	/** Whether both blocks of the two-high stall at {@code master} are {@code block}, in their parts, and pass {@code test}. */
	private static boolean both(ServerLevel level, MultiDecorationBlock block, BlockPos master, Predicate<BlockState> test) {
		for (int part = 0; part < block.cells().length; part++) {
			BlockState state = level.getBlockState(block.partPos(master, Direction.NORTH, part));
			if (!state.is(block) || block.part(state) != part || !test.test(state)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Takes the Midway Tickets out of the player's 36 inventory slots and returns how many there were. The tests' empty
	 * hand is put in by overwriting the selected slot, where a printed ticket lands when that slot is empty, so a ticket
	 * left there would be lost.
	 */
	private static int takeTickets(ServerPlayer player) {
		int taken = 0;
		for (int slot = 0; slot < 36; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(item("midway_ticket"))) {
				taken += stack.getCount();
				player.getInventory().setItem(slot, ItemStack.EMPTY);
			}
		}
		return taken;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theCottonCandyCartSpinsSugarIntoCandy(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		CottonCandyCartBlock cart = (CottonCandyCartBlock) JugcraftAgriculture.block("cotton_candy_cart");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("cotton_candy_cart")));
		helper.assertTrue(both(level, cart, master, state -> state.getValue(MultiDecorationBlock.FACING) == Direction.NORTH
				&& !state.getValue(MultiDecorationBlock.LIT) && state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == 0),
				"It stands two blocks high, facing the player, switched off and empty");
		ItemStack sugar = new ItemStack(Items.SUGAR, 4);
		use(player, level, master, sugar);
		helper.assertTrue(sugar.getCount() == 4, "Sugar isn't spun while it is off");
		use(player, level, master.above(), ItemStack.EMPTY);
		helper.assertTrue(both(level, cart, master, state -> state.getValue(MultiDecorationBlock.LIT)), "An empty hand switches it on");
		for (int i = 0; i < 4; i++) {
			use(player, level, master, sugar);
		}
		helper.assertTrue(both(level, cart, master, state -> state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == CottonCandyCartBlock.FLOSS)
				&& sugar.getCount() == 1, "Three sugars are spun into floss and a fourth isn't");
		ItemStack paper = new ItemStack(Items.PAPER, 2);
		use(player, level, master, paper);
		use(player, level, master.above(), paper);
		helper.assertTrue(both(level, cart, master, state -> state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == 1) && paper.isEmpty()
				&& player.getInventory().countItem(item("cotton_candy")) == 2, "Paper winds a serving onto a cone, two times");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(both(level, cart, master, state -> !state.getValue(MultiDecorationBlock.LIT)), "An empty hand switches it off");
		level.destroyBlock(master.above(), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir()
					&& dropped(helper, item("cotton_candy_cart")) == 1 && dropped(helper, Items.SUGAR) == 1,
					"Broken at its canopy it comes away whole, giving back itself and a sugar for the serving left");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 260)
	public void thePopcornWagonPopsKernels(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PopcornWagonBlock wagon = (PopcornWagonBlock) JugcraftAgriculture.block("popcorn_wagon");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("popcorn_wagon")));
		helper.assertTrue(both(level, wagon, master, state -> !state.getValue(MultiDecorationBlock.LIT) && state.getValue(PopcornWagonBlock.POPCORN) == 0),
				"It stands two blocks high, out and empty");
		ItemStack kernels = new ItemStack(item("corn_kernels"), 4);
		use(player, level, master, kernels);
		helper.assertTrue(kernels.getCount() == 4, "A kernel isn't taken while it is out");
		ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
		use(player, level, master.above(), flint);
		helper.assertTrue(both(level, wagon, master, state -> state.getValue(MultiDecorationBlock.LIT)) && flint.getDamageValue() == 1,
				"Flint and steel lights both blocks, wearing the flint");
		helper.assertTrue(level.getBlockState(master).getLightEmission() == 0
				&& level.getBlockState(master.above()).getLightEmission() == PopcornWagonBlock.LIGHT, "Its upper block alone gives light");
		use(player, level, master, kernels);
		use(player, level, master, kernels);
		helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPPING)) && kernels.getCount() == 3,
				"A kernel goes in the kettle, and a second isn't taken while it pops");
		ItemStack caramel = new ItemStack(item("caramel"), 2);
		helper.runAfterDelay(PopcornWagonBlock.POP_TICKS + 2, () -> {
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 1
					&& !state.getValue(PopcornWagonBlock.POPPING)), "It has popped into the case");
			helper.assertTrue(level.getBlockEntity(master) instanceof DecorationBlockEntity spill && spill.marked() != Long.MIN_VALUE,
					"The kettle's spill is marked");
			use(player, level, master.above(), ItemStack.EMPTY);
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 0)
					&& player.getInventory().countItem(item("popcorn")) == 1, "An empty hand takes the Popcorn");
			use(player, level, master, caramel);
			helper.assertTrue(caramel.getCount() == 2, "Caramel isn't spent on fewer than two");
			use(player, level, master, kernels);
		});
		helper.runAfterDelay(2 * (PopcornWagonBlock.POP_TICKS + 2), () -> use(player, level, master, kernels));
		helper.runAfterDelay(3 * (PopcornWagonBlock.POP_TICKS + 2), () -> {
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 2) && kernels.getCount() == 1,
					"Two more kernels have popped");
			use(player, level, master, caramel);
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 0) && caramel.getCount() == 1
					&& player.getInventory().countItem(item("caramel_popcorn")) == 1, "Caramel coats the two into a Caramel Popcorn");
			use(player, level, master, new ItemStack(Items.IRON_SHOVEL));
			helper.assertTrue(both(level, wagon, master, state -> !state.getValue(MultiDecorationBlock.LIT)), "A shovel puts it out");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void thePopcornWagonGivesBackWhatItHolds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PopcornWagonBlock wagon = (PopcornWagonBlock) JugcraftAgriculture.block("popcorn_wagon");
		BlockPos master = helper.absolutePos(new BlockPos(3, 2, 4));
		BlockState placed = wagon.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		wagon.setPlacedBy(level, master, placed, null, new ItemStack(wagon));
		for (int part = 0; part < wagon.cells().length; part++) {
			BlockPos at = wagon.partPos(master, Direction.NORTH, part);
			level.setBlock(at, level.getBlockState(at).setValue(PopcornWagonBlock.POPCORN, 3).setValue(PopcornWagonBlock.POPPING, true),
					Block.UPDATE_CLIENTS);
		}
		level.destroyBlock(master, true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir()
					&& dropped(helper, item("popcorn_wagon")) == 1 && dropped(helper, item("popcorn")) == 3
					&& dropped(helper, item("corn_kernels")) == 1,
					"Broken while popping with three in its case, it gives back itself, the three Popcorn and the kernel");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 2

	private static int prizes(ServerPlayer player) {
		int count = 0;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.isEmpty() && !stack.is(item("midway_ticket")) && !stack.is(item("carnival_mallet"))) {
				count += stack.getCount();
			}
		}
		return count;
	}

	@GameTest(maxTicks = 60)
	public void theTicketBoothPrintsTickets(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		TicketBoothBlock booth = (TicketBoothBlock) JugcraftAgriculture.block("ticket_booth");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("ticket_booth")));
		helper.assertTrue(both(level, booth, master, state -> state.getValue(TicketBoothBlock.ROLL_LEFT) == 0), "It stands two blocks high, its roll empty");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(player.getInventory().countItem(item("midway_ticket")) == 0, "With an empty roll it prints nothing");
		ItemStack paper = new ItemStack(Items.PAPER, 3);
		for (int i = 0; i < 3; i++) {
			use(player, level, master.above(), paper);
		}
		helper.assertTrue(both(level, booth, master, state -> state.getValue(TicketBoothBlock.ROLL_LEFT) == 6) && paper.isEmpty(),
				"Three sheets load six tickets");
		// Each ticket is taken as it is printed, before the next empty hand is put in.
		int[] printed = {0};
		use(player, level, master, ItemStack.EMPTY);
		printed[0] += takeTickets(player);
		use(player, level, master, ItemStack.EMPTY);
		printed[0] += takeTickets(player);
		helper.assertTrue(printed[0] == 1 && level.getBlockEntity(master) instanceof DecorationBlockEntity crank && crank.marked() != Long.MIN_VALUE,
				"An empty hand prints a ticket and turns the crank, which must come to rest before the next: " + printed[0] + " printed");
		helper.runAfterDelay(TicketBoothBlock.CRANK_TICKS + 1, () -> {
			use(player, level, master, ItemStack.EMPTY);
			printed[0] += takeTickets(player);
			helper.assertTrue(printed[0] == 2 && both(level, booth, master, state -> state.getValue(TicketBoothBlock.ROLL_LEFT) == 4),
					"At rest, it prints the next: " + printed[0] + " printed");
			level.destroyBlock(master, true);
		});
		helper.runAfterDelay(TicketBoothBlock.CRANK_TICKS + 4, () -> {
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir()
					&& dropped(helper, item("ticket_booth")) == 1 && dropped(helper, Items.PAPER) == 2,
					"Broken with four tickets left, it gives back itself and two sheets");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 420)
	public void whackAZombieWinsAPrize(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		WhackAZombieBlock whack = (WhackAZombieBlock) JugcraftAgriculture.block("whack_a_zombie");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 2));
		ServerPlayer other = standing(helper, new BlockPos(4, 2, 2));
		place(player, helper, pos, new ItemStack(item("whack_a_zombie")));
		helper.assertTrue(both(level, whack, master, state -> !state.getValue(MultiDecorationBlock.LIT) && state.getValue(WhackAZombieBlock.HEAD) == 0),
				"It stands two blocks high, no round on");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(!level.getBlockState(master).getValue(MultiDecorationBlock.LIT), "An empty hand starts nothing");
		ItemStack tickets = new ItemStack(item("midway_ticket"), 2);
		use(player, level, master, tickets);
		use(player, level, master, tickets);
		helper.assertTrue(both(level, whack, master, state -> state.getValue(MultiDecorationBlock.LIT) && state.getValue(WhackAZombieBlock.SCORE) == 0)
				&& tickets.getCount() == 1, "A ticket starts a round, and a second isn't taken while it runs");
		int before = prizes(player);
		boolean[] checked = new boolean[1];
		for (int tick = WhackAZombieBlock.GAP_TICKS + 1; tick < 380; tick += 2) {
			helper.runAfterDelay(tick, () -> {
				BlockState state = level.getBlockState(master);
				if (!state.is(whack) || !state.getValue(MultiDecorationBlock.LIT) || state.getValue(WhackAZombieBlock.HEAD) == 0) {
					return;
				}
				int up = state.getValue(WhackAZombieBlock.HEAD) - 1;
				Vec3 top = WhackAZombieBlock.headTop(master, Direction.NORTH, up);
				if (!checked[0]) {
					checked[0] = true;
					helper.assertTrue(!whack.whack(level, master, state, other, top), "Another player's whack doesn't count");
					Vec3 wrong = WhackAZombieBlock.headTop(master, Direction.NORTH, (up + 2) % WhackAZombieBlock.HOLES_X.length);
					helper.assertTrue(!whack.whack(level, master, state, player, wrong) && level.getBlockState(master).getValue(WhackAZombieBlock.SCORE) == 0,
							"A whack on the wrong hole doesn't count");
				}
				int score = state.getValue(WhackAZombieBlock.SCORE);
				helper.assertTrue(whack.whack(level, master, state, player, top), "A whack on the head that's up is a hit");
				BlockState after = level.getBlockState(master);
				helper.assertTrue(after.getValue(WhackAZombieBlock.HEAD) == 0 && after.getValue(WhackAZombieBlock.SCORE) == Math.min(WhackAZombieBlock.LAMPS, score + 1),
						"The head is knocked down and a lamp lit");
			});
		}
		helper.runAfterDelay(385, () -> {
			helper.assertTrue(both(level, whack, master, state -> !state.getValue(MultiDecorationBlock.LIT)
					&& state.getValue(WhackAZombieBlock.SCORE) == WhackAZombieBlock.LAMPS && state.getValue(WhackAZombieBlock.HEAD) == 0),
					"All eight lamps lit, the round is over");
			helper.assertTrue(prizes(player) > before, "Its player won a prize from the midway's prize table");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 460)
	public void whackAZombieRunsOutOfTime(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		WhackAZombieBlock whack = (WhackAZombieBlock) JugcraftAgriculture.block("whack_a_zombie");
		BlockPos master = helper.absolutePos(new BlockPos(3, 2, 4));
		BlockState placed = whack.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		whack.setPlacedBy(level, master, placed, null, new ItemStack(whack));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 2));
		use(player, level, master, new ItemStack(item("midway_ticket")));
		int before = prizes(player);
		boolean[] popped = new boolean[1];
		for (int tick = WhackAZombieBlock.GAP_TICKS + 2; tick < WhackAZombieBlock.ROUND_TICKS; tick += 5) {
			helper.runAfterDelay(tick, () -> popped[0] |= level.getBlockState(master).getValue(WhackAZombieBlock.HEAD) > 0);
		}
		helper.runAfterDelay(WhackAZombieBlock.ROUND_TICKS + WhackAZombieBlock.UP_TICKS + WhackAZombieBlock.GAP_TICKS + 5, () -> {
			helper.assertTrue(popped[0], "Heads popped up during the round");
			helper.assertTrue(both(level, whack, master, state -> !state.getValue(MultiDecorationBlock.LIT)
					&& state.getValue(WhackAZombieBlock.HEAD) == 0 && state.getValue(WhackAZombieBlock.SCORE) == 0) && prizes(player) == before,
					"Left alone, its time runs out: no head up, no lamp lit, no prize");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 3

	/** A point on the gallery's back wall (facing north, first block at {@code master}) at frame {@code x} and height {@code y}. */
	private static Vec3 onBackWall(BlockPos master, double x, double y) {
		return new Vec3(master.getX() + (x - 32.0) / 16.0, master.getY() + y / 16.0, master.getZ() + ShootingGalleryBlock.BOARD / 16.0);
	}

	private static ShootingGalleryBlock gallery(GameTestHelper helper, BlockPos master) {
		ServerLevel level = helper.getLevel();
		ShootingGalleryBlock gallery = (ShootingGalleryBlock) JugcraftAgriculture.block("shooting_gallery");
		BlockState placed = gallery.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		gallery.setPlacedBy(level, master, placed, null, new ItemStack(gallery));
		return gallery;
	}

	@GameTest(maxTicks = 300)
	public void theShootingGalleryKnocksDownTargets(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = helper.absolutePos(new BlockPos(5, 2, 5));
		ShootingGalleryBlock gallery = gallery(helper, master);
		helper.assertTrue(both(level, gallery, master, state -> !state.getValue(MultiDecorationBlock.LIT)),
				"It stands three blocks across and two up, no round on");
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		ServerPlayer other = standing(helper, new BlockPos(3, 2, 1));
		use(player, level, master, new ItemStack(item("midway_ticket")));
		helper.assertTrue(both(level, gallery, master, state -> state.getValue(MultiDecorationBlock.LIT)), "A ticket starts a round");
		ShootingGalleryBlockEntity round = (ShootingGalleryBlockEntity) level.getBlockEntity(master);
		Vec3 back = new Vec3(0.0, 0.0, 1.0);
		BlockState state = level.getBlockState(master);
		// A ghost in sight now, for the refusals.
		long now = level.getGameTime();
		int ghost = 0;
		for (int k = 0; k < ShootingGalleryBlock.PER_RAIL; k++) {
			float x = ShootingGalleryBlock.targetX(1, k, now, 0.0F);
			if (x > ShootingGalleryBlock.SEEN_FROM + 3.0F && x < ShootingGalleryBlock.SEEN_TO - 3.0F) {
				ghost = k;
			}
		}
		Vec3 aim = onBackWall(master, ShootingGalleryBlock.targetX(1, ghost, now, 0.0F), ShootingGalleryBlock.RAIL_Y[1] + 3.0);
		helper.assertTrue(gallery.shot(level, master, state, other, aim, back) < 0 && round.down() == 0, "Another player's shot knocks nothing down");
		Vec3 stand = player.position();
		BlockPos counter = helper.absolutePos(new BlockPos(4, 2, 5));
		player.setPos(counter.getX() + 0.5, counter.getY(), counter.getZ() + 0.5);
		helper.assertTrue(gallery.shot(level, master, state, player, aim, back) < 0 && round.down() == 0, "A shot from too close knocks nothing down");
		player.setPos(stand.x, stand.y, stand.z);
		double gap = ShootingGalleryBlock.targetX(1, ghost, now, 0.0F) + 5.5;
		helper.assertTrue(gallery.shot(level, master, state, player, onBackWall(master, gap, ShootingGalleryBlock.RAIL_Y[1] + 3.0), back) < 0
				&& round.down() == 0, "A shot between the targets knocks nothing down");
		int before = prizes(player);
		for (int tick = 1; tick < 260; tick++) {
			helper.runAfterDelay(tick, () -> {
				BlockState here = level.getBlockState(master);
				if (!here.getValue(MultiDecorationBlock.LIT)) {
					return;
				}
				long time = level.getGameTime();
				for (int target = 0; target < ShootingGalleryBlock.TARGETS; target++) {
					int rail = target / ShootingGalleryBlock.PER_RAIL;
					float x = ShootingGalleryBlock.targetX(rail, target % ShootingGalleryBlock.PER_RAIL, time, 0.0F);
					if (round.isDown(target) || x < ShootingGalleryBlock.SEEN_FROM + 3.0F || x > ShootingGalleryBlock.SEEN_TO - 3.0F) {
						continue;
					}
					Vec3 at = onBackWall(master, x, ShootingGalleryBlock.RAIL_Y[rail] + 0.5 + ShootingGalleryBlock.TARGET_H[rail] / 2.0);
					int down = round.down();
					int hit = gallery.shot(level, master, here, player, at, back);
					helper.assertTrue(hit == target || round.down() == 0 && down == ShootingGalleryBlock.TARGETS - 1,
							"A shot at target " + target + " in sight knocks it down, not " + hit);
					if (!level.getBlockState(master).getValue(MultiDecorationBlock.LIT)) {
						return;
					}
				}
			});
		}
		helper.runAfterDelay(265, () -> {
			helper.assertTrue(both(level, gallery, master, state2 -> !state2.getValue(MultiDecorationBlock.LIT)) && round.down() == 0,
					"All eight down, the round is over and they stand again");
			helper.assertTrue(prizes(player) > before, "Its player won a prize from the midway's prize table");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 660)
	public void theShootingGalleryRunsOutOfTime(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = helper.absolutePos(new BlockPos(5, 2, 5));
		ShootingGalleryBlock gallery = gallery(helper, master);
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		use(player, level, master, new ItemStack(item("midway_ticket")));
		ShootingGalleryBlockEntity round = (ShootingGalleryBlockEntity) level.getBlockEntity(master);
		// One target down, then left alone.
		long now = level.getGameTime();
		for (int k = 0; k < ShootingGalleryBlock.PER_RAIL && round.down() == 0; k++) {
			float x = ShootingGalleryBlock.targetX(0, k, now, 0.0F);
			if (x > ShootingGalleryBlock.SEEN_FROM + 3.0F && x < ShootingGalleryBlock.SEEN_TO - 3.0F) {
				gallery.shot(level, master, level.getBlockState(master), player, onBackWall(master, x, ShootingGalleryBlock.RAIL_Y[0] + 2.0),
						new Vec3(0.0, 0.0, 1.0));
			}
		}
		helper.assertTrue(round.down() == 1, "A target is knocked down");
		int before = prizes(player);
		helper.runAfterDelay(ShootingGalleryBlock.ROUND_TICKS + 5, () -> {
			helper.assertTrue(both(level, gallery, master, state -> !state.getValue(MultiDecorationBlock.LIT)) && round.down() == 0
					&& prizes(player) == before, "Its time run out, the round is lost: they stand again, and no prize");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 4

	@GameTest(maxTicks = 100)
	public void theGhostTrainOpensForACart(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		GhostTrainBlock train = (GhostTrainBlock) JugcraftAgriculture.block("ghost_train");
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		BlockState placed = train.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		train.setPlacedBy(level, master, placed, null, new ItemStack(train));
		helper.assertTrue(both(level, train, master, state -> !state.getValue(GhostTrainBlock.IN_OPEN) && !state.getValue(GhostTrainBlock.OUT_OPEN)),
				"It stands five blocks across and four up, its doors shut");
		BlockPos in = GhostTrainBlock.doorway(master, Direction.NORTH, 0);
		BlockPos out = GhostTrainBlock.doorway(master, Direction.NORTH, 1);
		helper.assertTrue(level.getBlockState(in).isAir() && level.getBlockState(in.above()).isAir() && level.getBlockState(out).isAir(),
				"Its doorways are left free");
		level.setBlockAndUpdate(in, Blocks.RAIL.defaultBlockState());
		Minecart cart = helper.spawn(EntityType.MINECART, helper.relativePos(in));
		helper.runAfterDelay(GhostTrainBlock.LOOK_TICKS + 2, () -> {
			helper.assertTrue(both(level, train, master, state -> state.getValue(GhostTrainBlock.IN_OPEN) && !state.getValue(GhostTrainBlock.OUT_OPEN)),
					"A minecart in the IN doorway opens its doors, the OUT doors staying shut");
			helper.assertTrue(level.getBlockEntity(master) instanceof GhostTrainBlockEntity watch && watch.marked() != Long.MIN_VALUE
					&& watch.changedAt(0) != Long.MIN_VALUE, "Its ghost springs up as they open");
			cart.discard();
		});
		helper.runAfterDelay(GhostTrainBlock.LOOK_TICKS * 2 + GhostTrainBlock.HOLD_TICKS + 6, () -> {
			helper.assertTrue(both(level, train, master, state -> !state.getValue(GhostTrainBlock.IN_OPEN)), "The cart gone, its doors shut again");
			ServerPlayer player = standing(helper, new BlockPos(6, 2, 1));
			use(player, level, master.above(2), ItemStack.EMPTY);
			int lit = 0;
			for (int part = 0; part < train.cells().length; part++) {
				lit += level.getBlockState(train.partPos(master, Direction.NORTH, part)).getLightEmission() > 0 ? 1 : 0;
			}
			helper.assertTrue(both(level, train, master, state -> state.getValue(MultiDecorationBlock.LIT)) && lit == 3
					&& level.getBlockState(master.above(2)).getLightEmission() == GhostTrainBlock.LIGHT, "An empty hand lights its three windows");
			level.destroyBlock(master, true);
		});
		helper.runAfterDelay(GhostTrainBlock.LOOK_TICKS * 2 + GhostTrainBlock.HOLD_TICKS + 10, () -> {
			for (int part = 0; part < train.cells().length; part++) {
				helper.assertTrue(level.getBlockState(train.partPos(master, Direction.NORTH, part)).isAir(), "Its block " + part + " is gone");
			}
			helper.assertTrue(level.getBlockState(in).is(Blocks.RAIL) && dropped(helper, item("ghost_train")) == 1,
					"Broken, it comes away whole, dropping itself once, and the rail in its doorway stays");
			helper.succeed();
		});
	}
}
