package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.CandyBagItem;
import io.github.jimbozoomer.jugcraft.agriculture.CandyCornDispenserBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CandyCornDispenserBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.PartyServingBlock;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Halloween Party Table, part 1 (decor set 8; docs/features/halloween-party-table.md): the Candy Corn Dispenser stands
 * facing whoever places it, empty, with its block entity; an empty one only rattles for an empty hand; candy corn pours
 * in, as much as fits, up to {@value CandyCornDispenserBlock#CAPACITY}, its globe filling by level; full, it takes no more.
 * An empty hand sets its crank turning, and a second use while it turns does nothing; as the crank comes round one piece
 * drops out of its chute. Its crank turns once round, easing in and out, and its flap swings out and back once it has.
 * Broken, it drops itself and all it held. Part 2: the Spider Cupcake Tower and the Ghost Meringue Platter each stand
 * facing whoever places it, empty and off any table; their food goes on one at a time up to their capacity, anything else
 * changing nothing; an empty hand takes one off into the player's inventory; broken, each drops itself and what it held.
 * On a Feast Table each is set down onto its top, and stands back up when the table goes. The Ghost Meringue is candy, so
 * a Candy Bag takes it. Part 3: the Mummy Sausage Tray, the Witch-Finger Cauldron and the Eyeball Jelly Mould serve
 * their foods the same way. Their recipes and loot load.
 */
public class PartyTableGameTests {
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

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 60)
	public void theCandyCornDispenserFillsAndTurns(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item("candy_corn_dispenser")));
		BlockState placed = level.getBlockState(at);
		if (!(placed.getBlock() instanceof CandyCornDispenserBlock) || !(level.getBlockEntity(at) instanceof CandyCornDispenserBlockEntity dispenser)) {
			throw helper.assertionException("The dispenser stands where it was placed, with its block entity");
		}
		helper.assertTrue(placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH && placed.getValue(CandyCornDispenserBlock.FILL) == 0,
				"It faces the player, empty");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(dispenser.marked() == Long.MIN_VALUE, "An empty one only rattles");
		use(player, level, at, new ItemStack(item("candy_corn"), 20));
		helper.assertTrue(player.getMainHandItem().isEmpty() && dispenser.count() == 20
				&& level.getBlockState(at).getValue(CandyCornDispenserBlock.FILL) == CandyCornDispenserBlock.fill(20), "Candy corn pours in, its globe filling");
		use(player, level, at, new ItemStack(item("candy_corn"), 60));
		int over = 80 - CandyCornDispenserBlock.CAPACITY;
		helper.assertTrue(player.getMainHandItem().getCount() == over && dispenser.count() == CandyCornDispenserBlock.CAPACITY
				&& level.getBlockState(at).getValue(CandyCornDispenserBlock.FILL) == CandyCornDispenserBlock.LEVELS,
				"as much as fits, up to " + CandyCornDispenserBlock.CAPACITY + ": " + dispenser.count());
		use(player, level, at, player.getMainHandItem());
		helper.assertTrue(player.getMainHandItem().getCount() == over, "Full, it takes no more");
		use(player, level, at, ItemStack.EMPTY);
		long turned = dispenser.marked();
		helper.assertTrue(turned != Long.MIN_VALUE, "An empty hand sets its crank turning");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(dispenser.marked() == turned && dispenser.count() == CandyCornDispenserBlock.CAPACITY,
				"and a second use while it turns does nothing");
		helper.runAfterDelay(CandyCornDispenserBlock.CRANK_TICKS + 2, () -> {
			helper.assertTrue(dispenser.count() == CandyCornDispenserBlock.CAPACITY - 1 && dropped(helper, item("candy_corn")) == 1,
					"As the crank comes round, one piece drops out of its chute, not " + dropped(helper, item("candy_corn")));
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item("candy_corn_dispenser")) == 1
					&& dropped(helper, item("candy_corn")) == CandyCornDispenserBlock.CAPACITY, "Broken, it drops itself and all it held");
			helper.succeed();
		});
	}

	@GameTest
	public void theDispensersCrankTurnsOnceRound(GameTestHelper helper) {
		int crank = CandyCornDispenserBlock.CRANK_TICKS;
		helper.assertTrue(CandyCornDispenserBlock.crank(-1.0F) == 0.0F && CandyCornDispenserBlock.crank(0.0F) == 0.0F
				&& Math.abs(CandyCornDispenserBlock.crank(crank / 2.0F) - 180.0F) < 0.01F && CandyCornDispenserBlock.crank(crank) == 0.0F,
				"Its crank turns once round, half way at half time, and rests");
		helper.assertTrue(CandyCornDispenserBlock.crank(1.0F) < 360.0F / crank, "easing in");
		float open = CandyCornDispenserBlock.flap(crank + CandyCornDispenserBlock.FLAP_TICKS / 2.0F);
		helper.assertTrue(CandyCornDispenserBlock.flap(crank - 1.0F) == 0.0F && Math.abs(open - CandyCornDispenserBlock.FLAP) < 0.01F
				&& CandyCornDispenserBlock.flap(crank + CandyCornDispenserBlock.FLAP_TICKS) == 0.0F,
				"Its flap stays shut while the crank turns, then swings out " + CandyCornDispenserBlock.FLAP + " degrees and back: " + open);
		helper.assertTrue(CandyCornDispenserBlock.fill(0) == 0 && CandyCornDispenserBlock.fill(1) == 1
				&& CandyCornDispenserBlock.fill(CandyCornDispenserBlock.CAPACITY) == CandyCornDispenserBlock.LEVELS, "Its globe shows how full it is");
		BlockPos pos = new BlockPos(0, 64, 0);
		helper.assertTrue(CandyCornDispenserBlock.chute(pos, Direction.NORTH).z < 0.5 && CandyCornDispenserBlock.chute(pos, Direction.EAST).x > 0.5,
				"Its chute lets pieces out at its front");
		helper.succeed();
	}

	private static int carried(ServerPlayer player, Item item) {
		int count = 0;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(item)) {
				count += player.getInventory().getItem(slot).getCount();
			}
		}
		return count;
	}

	@GameTest(maxTicks = 40)
	public void theCakeStandsServeTheirFood(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		String[][] stands = {{"spider_cupcake_tower", "spiderweb_cupcake"}, {"ghost_meringue_platter", "ghost_meringue"},
				{"mummy_sausage_tray", "mummy_sausage"}, {"witch_finger_cauldron", "witch_finger"}, {"eyeball_jelly_mould", "eyeball_jelly"}};
		for (int k = 0; k < stands.length; k++) {
			BlockPos rel = new BlockPos(1 + k, 2, 3);
			BlockPos at = helper.absolutePos(rel);
			Item food = item(stands[k][1]);
			place(player, helper, rel, new ItemStack(item(stands[k][0])));
			BlockState placed = level.getBlockState(at);
			if (!(placed.getBlock() instanceof PartyServingBlock stand)) {
				throw helper.assertionException(stands[k][0] + " stands where it was placed");
			}
			int capacity = stand.capacity();
			helper.assertTrue(placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH && placed.getValue(stand.servings()) == 0
					&& !placed.getValue(PartyServingBlock.ON_TABLE), stands[k][0] + " faces the player, empty, off any table");
			use(player, level, at, new ItemStack(item("candy_corn"), 4));
			helper.assertTrue(level.getBlockState(at).getValue(stand.servings()) == 0 && player.getMainHandItem().getCount() == 4,
					"Anything but its food changes nothing");
			use(player, level, at, new ItemStack(food, capacity + 3));
			for (int n = 1; n < capacity + 2; n++) {
				use(player, level, at, player.getMainHandItem());
			}
			helper.assertTrue(level.getBlockState(at).getValue(stand.servings()) == capacity && player.getMainHandItem().getCount() == 3,
					"Its food goes on one at a time, up to " + capacity + ": " + level.getBlockState(at).getValue(stand.servings()));
			player.getInventory().clearContent();
			use(player, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at).getValue(stand.servings()) == capacity - 1 && carried(player, food) == 1,
					"An empty hand takes one off, into the player's inventory");
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(stands[k][0])) == 1 && dropped(helper, food) == capacity - 1,
					"Broken, " + stands[k][0] + " drops itself and what it held, not " + dropped(helper, food));
		}
		helper.assertTrue(new ItemStack(item("ghost_meringue")).is(CandyBagItem.TREATS), "The Ghost Meringue is candy, so a Candy Bag takes it");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theCakeStandsStandOnAFeastTable(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		helper.setBlock(new BlockPos(3, 2, 3), JugcraftAgriculture.block("feast_table"));
		BlockPos at = helper.absolutePos(new BlockPos(3, 3, 3));
		place(player, helper, new BlockPos(3, 3, 3), new ItemStack(item("ghost_meringue_platter")));
		BlockState placed = level.getBlockState(at);
		helper.assertTrue(placed.getBlock() instanceof PartyServingBlock && placed.getValue(PartyServingBlock.ON_TABLE)
				&& placed.getShape(level, at).min(Direction.Axis.Y) < 0.0, "On a Feast Table it is set down onto the table's top");
		helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
		helper.assertTrue(!level.getBlockState(at).getValue(PartyServingBlock.ON_TABLE) && level.getBlockState(at).getShape(level, at).min(Direction.Axis.Y) == 0.0,
				"and stands back up when the table goes");
		helper.succeed();
	}

	@GameTest
	public void partyTableDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id("ghost_meringue"))).isPresent(),
				"ghost_meringue has a recipe");
		for (String food : new String[] {"mummy_sausage", "witch_finger", "eyeball_jelly"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(food))).isPresent(), food + " has a recipe");
		}
		for (String id : new String[] {"candy_corn_dispenser", "spider_cupcake_tower", "ghost_meringue_platter", "mummy_sausage_tray",
				"witch_finger_cauldron", "eyeball_jelly_mould"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}
