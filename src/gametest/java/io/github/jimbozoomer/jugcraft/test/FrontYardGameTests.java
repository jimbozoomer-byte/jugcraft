package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.AutumnWreathBlock;
import io.github.jimbozoomer.jugcraft.agriculture.BatWreathBlock;
import io.github.jimbozoomer.jugcraft.agriculture.BewareDoormatBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CrashedWitchBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.DoorWreath;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GargoyleKnockerBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinPathLightBlock;
import io.github.jimbozoomer.jugcraft.agriculture.RisingZombieBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SkeletonAnimalBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SkeletonAnimalBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Front-Yard Haunt Scenes, parts 1 and 2 (decor set 7; docs/features/front-yard-haunt.md): the Haunted Mailbox stands two
 * blocks high, facing the player, its flag down; an empty hand raises its flag on both blocks and lowers it again;
 * broken at its box it comes away whole and drops once. Its haunting is worked out the same everywhere: with someone near,
 * its door falls open {@value HauntedMailboxBlock#OPEN} degrees, its eyes showing; with no one, it shuts and then only
 * rattles open a crack now and then, its eyes hidden. The Pumpkin Path Light stands two blocks high, placed lit, its
 * lower block alone giving light {@value PumpkinPathLightBlock#LIGHT}; an empty hand puts it out and lights it again;
 * broken, it drops once; its lantern sways a little, to and fro. Part 2: the Skeleton Dog, Cat and Crow each face
 * whoever places them, keep their moving part's block entity, rattle for an empty hand without changing, and drop
 * themselves; the dog's tail wags only while someone is near, the cat's sways, each cat in its own time, and the crow's
 * head turns toward whoever it watches as far as it can, glancing about with no one near, in quick snaps. The Rising
 * Zombie stands two blocks high and groans for an empty hand without changing; broken, it drops once; its arms claw out of
 * step and reach higher the nearer anyone comes. Part 3: the Gargoyle Door Knocker hangs on a wall facing out and raps
 * for an empty hand, its ring swinging and its eyes glowing while it raps, then still; on a door its knock neither opens
 * the door nor lets a second knock in while it raps, and with a Candy Bag it knocks for trick-or-treat. The Bat Wreath
 * hangs on a wall and a door, takes no mums and drops as itself. The Beware Doormat lies facing whoever places it and
 * goes with what is under it. Part 4: the Crashed Witch hangs on a wall facing out; an empty hand makes her cackle once
 * until she has done; her legs kick only in their bursts, the whole time with someone near and after a cackle, out of
 * step; with the wall gone she goes, dropping herself. Their recipes and loot load (two lights a craft).
 */
public class FrontYardGameTests {
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

	/** Whether both blocks of the two-high piece at {@code master} are {@code block}, in their parts, and pass {@code test}. */
	private static boolean both(ServerLevel level, MultiDecorationBlock block, BlockPos master, Predicate<BlockState> test) {
		for (int part = 0; part < block.cells().length; part++) {
			BlockState state = level.getBlockState(block.partPos(master, Direction.NORTH, part));
			if (!state.is(block) || block.part(state) != part || !test.test(state)) {
				return false;
			}
		}
		return true;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theHauntedMailboxRaisesItsFlag(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		HauntedMailboxBlock mailbox = (HauntedMailboxBlock) JugcraftAgriculture.block("haunted_mailbox");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("haunted_mailbox")));
		helper.assertTrue(both(level, mailbox, master, state -> state.getValue(MultiDecorationBlock.FACING) == Direction.NORTH
				&& !state.getValue(HauntedMailboxBlock.FLAG)), "It stands two blocks high, facing the player, its flag down");
		helper.assertTrue(level.getBlockEntity(master) instanceof HauntedMailboxBlockEntity, "Its first block keeps its haunting");
		use(player, level, master.above(), ItemStack.EMPTY);
		helper.assertTrue(both(level, mailbox, master, state -> state.getValue(HauntedMailboxBlock.FLAG)), "An empty hand raises its flag, on both blocks");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(both(level, mailbox, master, state -> !state.getValue(HauntedMailboxBlock.FLAG)), "and lowers it again");
		level.destroyBlock(master.above(), true);
		helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir(), "Broken at its box, it comes away whole");
		helper.assertTrue(dropped(helper, item("haunted_mailbox")) == 1, "and drops once, not " + dropped(helper, item("haunted_mailbox")));
		helper.succeed();
	}

	@GameTest
	public void theMailboxsDoorFallsOpenForWhoeverComesNear(GameTestHelper helper) {
		float open = HauntedMailboxBlock.lid(true, HauntedMailboxBlock.SWING_TICKS, 1000.0F, 0);
		helper.assertTrue(Math.abs(open - HauntedMailboxBlock.OPEN) < 0.01F, "With someone near, its door falls open " + HauntedMailboxBlock.OPEN + ": " + open);
		helper.assertTrue(HauntedMailboxBlock.lid(true, 0.0F, 1000.0F, 0) == 0.0F, "starting shut");
		helper.assertTrue(HauntedMailboxBlock.eyes(open, 1000.0F, 0), "and its eyes stare out");
		float gone = HauntedMailboxBlock.lid(false, HauntedMailboxBlock.SWING_TICKS, 100.0F, 0);
		helper.assertTrue(gone == 0.0F && !HauntedMailboxBlock.eyes(gone, 100.0F, 0), "With no one near, it shuts, its eyes hidden: " + gone);
		float rattle = HauntedMailboxBlock.lid(false, 1000.0F, HauntedMailboxBlock.RATTLE_TICKS * 10 + 5.0F, 0);
		helper.assertTrue(rattle > 0.0F && rattle < 30.0F && !HauntedMailboxBlock.eyes(rattle, 5.0F, 0),
				"and now and then rattles open a crack, its eyes hidden: " + rattle);
		helper.assertTrue(HauntedMailboxBlock.lid(false, 1000.0F, HauntedMailboxBlock.RATTLE_TICKS * 10 + 5.0F, 80) == 0.0F,
				"each box in its own time");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void thePumpkinPathLightLightsThePath(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PumpkinPathLightBlock light = (PumpkinPathLightBlock) JugcraftAgriculture.block("pumpkin_path_light");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("pumpkin_path_light")));
		helper.assertTrue(both(level, light, master, state -> state.getValue(MultiDecorationBlock.LIT)), "It stands two blocks high, placed lit");
		helper.assertTrue(level.getBlockState(master).getLightEmission() == PumpkinPathLightBlock.LIGHT
				&& level.getBlockState(master.above()).getLightEmission() == 0, "its lower block alone giving light " + PumpkinPathLightBlock.LIGHT);
		use(player, level, master.above(), ItemStack.EMPTY);
		helper.assertTrue(both(level, light, master, state -> !state.getValue(MultiDecorationBlock.LIT))
				&& level.getBlockState(master).getLightEmission() == 0, "An empty hand puts it out");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(both(level, light, master, state -> state.getValue(MultiDecorationBlock.LIT)), "and lights it again");
		float most = 0.0F;
		for (int t = 0; t < PumpkinPathLightBlock.PERIOD; t++) {
			most = Math.max(most, Math.abs(PumpkinPathLightBlock.sway(t, 7, false)));
		}
		helper.assertTrue(most > 1.0F && most <= PumpkinPathLightBlock.SWAY + 0.001F, "Its lantern sways, up to " + PumpkinPathLightBlock.SWAY + ": " + most);
		level.destroyBlock(master, true);
		helper.assertTrue(level.getBlockState(master.above()).isAir(), "Broken, it comes away whole");
		helper.assertTrue(dropped(helper, item("pumpkin_path_light")) == 1, "and drops once, not " + dropped(helper, item("pumpkin_path_light")));
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theSkeletonAnimalsFaceWhoeverPlacesThem(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		String[] animals = {"skeleton_dog", "skeleton_cat", "skeleton_crow"};
		SkeletonAnimalBlock.Kind[] kinds = {SkeletonAnimalBlock.Kind.DOG, SkeletonAnimalBlock.Kind.CAT, SkeletonAnimalBlock.Kind.CROW};
		for (int k = 0; k < animals.length; k++) {
			BlockPos pos = new BlockPos(2 + 2 * k, 2, 4);
			BlockPos at = helper.absolutePos(pos);
			place(player, helper, pos, new ItemStack(item(animals[k])));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.getBlock() instanceof SkeletonAnimalBlock animal && animal.kind() == kinds[k]
					&& placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH && placed.getValue(SkeletonAnimalBlock.RUNNER) == 0,
					animals[k] + " stands facing the player who placed it");
			helper.assertTrue(level.getBlockEntity(at) instanceof SkeletonAnimalBlockEntity, animals[k] + " keeps its moving part");
			use(player, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at) == placed, "An empty hand rattles " + animals[k] + "'s bones, changing nothing");
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(animals[k])) == 1, "Broken, " + animals[k] + " drops itself");
		}
		helper.succeed();
	}

	@GameTest
	public void theSkeletonAnimalsMove(GameTestHelper helper) {
		float wagging = 0.0F;
		float still = 0.0F;
		float swaying = 0.0F;
		boolean apart = false;
		for (int t = 0; t < SkeletonAnimalBlock.SWAY_TICKS; t++) {
			wagging = Math.max(wagging, Math.abs(SkeletonAnimalBlock.wag(true, 100.0F, t + 0.5F)));
			still = Math.max(still, Math.abs(SkeletonAnimalBlock.wag(false, 100.0F, t + 0.5F)));
			swaying = Math.max(swaying, Math.abs(SkeletonAnimalBlock.sway(t, 0)));
			apart |= Math.abs(SkeletonAnimalBlock.sway(t, 0) - SkeletonAnimalBlock.sway(t, 17)) > 1.0F;
		}
		helper.assertTrue(wagging > SkeletonAnimalBlock.WAG * 0.9F && wagging <= SkeletonAnimalBlock.WAG + 0.001F,
				"With someone near, the dog's tail wags " + SkeletonAnimalBlock.WAG + " degrees: " + wagging);
		helper.assertTrue(still == 0.0F, "With no one near, it lies still: " + still);
		helper.assertTrue(Math.abs(SkeletonAnimalBlock.wag(true, 0.0F, 2.5F)) == 0.0F, "starting from still");
		helper.assertTrue(swaying > SkeletonAnimalBlock.SWAY * 0.9F && swaying <= SkeletonAnimalBlock.SWAY + 0.001F && apart,
				"The cat's tail sways " + SkeletonAnimalBlock.SWAY + " degrees, each cat in its own time: " + swaying);
		helper.assertTrue(SkeletonAnimalBlock.watch(true, 30.0F, 0, 0) == 30.0F, "The crow's head turns toward whoever it watches");
		helper.assertTrue(SkeletonAnimalBlock.watch(true, 120.0F, 0, 0) == SkeletonAnimalBlock.TURN
				&& SkeletonAnimalBlock.watch(true, -120.0F, 0, 0) == -SkeletonAnimalBlock.TURN, "as far as it can turn");
		Set<Float> glances = new HashSet<>();
		for (long snap = 0; snap < 20; snap++) {
			float glance = SkeletonAnimalBlock.watch(false, 120.0F, snap, 5);
			helper.assertTrue(Math.abs(glance) <= 40.0F, "With no one near, it only glances about: " + glance);
			glances.add(glance);
		}
		helper.assertTrue(glances.size() >= 3, "now this way, now that: " + glances);
		helper.assertTrue(SkeletonAnimalBlock.snapping(0.0F, 60.0F, 0.0F) == 0.0F
				&& SkeletonAnimalBlock.snapping(0.0F, 60.0F, SkeletonAnimalBlock.SNAP_LENGTH) == 60.0F, "in a quick snap");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theRisingZombieClawsAndGroans(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		RisingZombieBlock zombie = (RisingZombieBlock) JugcraftAgriculture.block("rising_zombie");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("rising_zombie")));
		helper.assertTrue(both(level, zombie, master, state -> state.getValue(MultiDecorationBlock.FACING) == Direction.NORTH
				&& state.getValue(RisingZombieBlock.RUNNER) == 0), "It stands two blocks high, facing the player");
		helper.assertTrue(level.getBlockEntity(master) instanceof DecorationBlockEntity && level.getBlockEntity(master.above()) == null,
				"Its first block keeps its arms");
		BlockState before = level.getBlockState(master);
		use(player, level, master.above(), ItemStack.EMPTY);
		helper.assertTrue(level.getBlockState(master) == before && !before.getValue(MultiDecorationBlock.LIT),
				"An empty hand makes it groan, changing nothing");
		float most = 0.0F;
		boolean opposed = true;
		for (int t = 0; t < RisingZombieBlock.CLAW_TICKS; t++) {
			float left = RisingZombieBlock.claw(false, t, 9);
			most = Math.max(most, Math.abs(left));
			opposed &= Math.abs(left + RisingZombieBlock.claw(true, t, 9)) < 0.01F;
		}
		helper.assertTrue(most > RisingZombieBlock.CLAW * 0.9F && most <= RisingZombieBlock.CLAW + 0.001F && opposed,
				"Its arms claw " + RisingZombieBlock.CLAW + " degrees, out of step: " + most);
		float half = RisingZombieBlock.lurch(RisingZombieBlock.REACH / 2);
		helper.assertTrue(RisingZombieBlock.lurch(RisingZombieBlock.REACH + 1.0) == 0.0F && RisingZombieBlock.lurch(0.0) == RisingZombieBlock.LURCH
				&& half > 0.0F && half < RisingZombieBlock.LURCH, "and reach higher the nearer anyone comes: " + half);
		level.destroyBlock(master.above(), true);
		helper.assertTrue(level.getBlockState(master).isAir(), "Broken, it comes away whole");
		helper.assertTrue(dropped(helper, item("rising_zombie")) == 1, "and drops once, not " + dropped(helper, item("rising_zombie")));
		helper.succeed();
	}

	/** Uses {@code held} on the {@code face} side of the block at {@code absolute}, as a player placing or using it there. */
	private static void useOnFace(ServerPlayer player, BlockPos absolute, Direction face, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(face, 0.5), face, absolute, false)));
	}

	private static void door(ServerLevel level, BlockPos base) {
		level.setBlock(base.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(base, Blocks.OAK_DOOR.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(base.above(), Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
	}

	@GameTest(maxTicks = 60)
	public void theGargoyleKnockerRapsOnAWallAndADoor(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos wall = helper.absolutePos(new BlockPos(2, 2, 5));
		level.setBlock(wall, Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = standing(helper, new BlockPos(2, 2, 2));
		useOnFace(player, wall, Direction.NORTH, new ItemStack(item("gargoyle_knocker")));
		BlockPos at = wall.north();
		BlockState hung = level.getBlockState(at);
		helper.assertTrue(hung.getBlock() instanceof GargoyleKnockerBlock && hung.getValue(AutumnWreathBlock.FACING) == Direction.NORTH
				&& level.getBlockEntity(at) instanceof DecorationBlockEntity, "It hangs on the wall's face, facing out, with its block entity");
		use(player, level, at, ItemStack.EMPTY);
		float lifted = GargoyleKnockerBlock.ring(GargoyleKnockerBlock.RAP_TICKS / 2.0F);
		helper.assertTrue(Math.abs(lifted - GargoyleKnockerBlock.SWING) < 0.01F && GargoyleKnockerBlock.ring(0.0F) == 0.0F
				&& GargoyleKnockerBlock.ring(GargoyleKnockerBlock.RAP_TICKS) < 0.01F, "Its ring lifts " + GargoyleKnockerBlock.SWING + " and strikes: " + lifted);
		helper.assertTrue(GargoyleKnockerBlock.eyes(1.0F) && !GargoyleKnockerBlock.eyes(GargoyleKnockerBlock.RAPS * GargoyleKnockerBlock.RAP_TICKS
				+ GargoyleKnockerBlock.GLOW_AFTER), "its eyes glowing while it raps, then not");
		BlockPos base = helper.absolutePos(new BlockPos(5, 2, 4));
		door(level, base);
		DoorWreath ornament = DoorWreath.attach(level, base.above(), true, hung.getBlock().defaultBlockState());
		helper.assertTrue(ornament != null, "It hangs on a door too");
		helper.runAfterDelay(1, () -> {
			helper.assertTrue(level.getBlockEntity(at) instanceof DecorationBlockEntity watch && watch.marked() != Long.MIN_VALUE,
					"An empty hand knocks on the wall's knocker");
			ornament.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
			long knocked = ornament.knockedAt();
			helper.assertTrue(knocked != Long.MIN_VALUE && !level.getBlockState(base).getValue(DoorBlock.OPEN),
					"On a door, an empty hand knocks rather than opening it");
			ornament.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
			helper.assertTrue(ornament.knockedAt() == knocked, "and a second knock waits while it raps");
			ItemStack bag = new ItemStack(item("candy_bag"));
			player.setItemInHand(InteractionHand.MAIN_HAND, bag);
			helper.runAfterDelay(GargoyleKnockerBlock.RAPS * GargoyleKnockerBlock.RAP_TICKS + 2, () -> {
				ornament.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
				helper.assertTrue(ornament.knockedAt() > knocked && player.getCooldowns().isOnCooldown(bag),
						"With a Candy Bag it knocks again, for trick-or-treat at its door");
				level.destroyBlock(at, true);
				helper.assertTrue(dropped(helper, item("gargoyle_knocker")) == 1, "Broken on the wall, it drops itself");
				helper.succeed();
			});
		});
	}

	@GameTest(maxTicks = 40)
	public void theBatWreathHangsOnWallsAndDoors(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos wall = helper.absolutePos(new BlockPos(2, 2, 5));
		level.setBlock(wall, Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = standing(helper, new BlockPos(2, 2, 2));
		useOnFace(player, wall, Direction.NORTH, new ItemStack(item("bat_wreath")));
		BlockState hung = level.getBlockState(wall.north());
		helper.assertTrue(hung.getBlock() instanceof BatWreathBlock && hung.getValue(AutumnWreathBlock.FACING) == Direction.NORTH,
				"It hangs on the wall's face, facing out");
		ItemStack mums = new ItemStack(item("red_mum"), 2);
		var result = hung.useItemOn(mums, level, player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(wall.north()), Direction.NORTH, wall.north(), false));
		helper.assertTrue(result == net.minecraft.world.InteractionResult.PASS && level.getBlockState(wall.north()) == hung && mums.getCount() == 2,
				"It takes no mums");
		BlockPos base = helper.absolutePos(new BlockPos(5, 2, 4));
		door(level, base);
		useOnFace(player, base.above(), Direction.NORTH, new ItemStack(item("bat_wreath")));
		var hanging = level.getEntitiesOfClass(DoorWreath.class, new AABB(base.above()).inflate(1), e -> e.getPos().equals(base.above()));
		helper.assertTrue(hanging.size() == 1 && hanging.get(0).wreathState().getBlock() instanceof BatWreathBlock, "It hangs on a door, as the other wreaths do");
		ItemStack picked = hanging.get(0).getPickResult();
		helper.assertTrue(picked.is(item("bat_wreath")) && !picked.has(DataComponents.BLOCK_STATE), "and comes away as itself, with no mums");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theBewareDoormatLiesAtTheDoor(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos at = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("beware_doormat")));
		BlockState mat = level.getBlockState(at);
		helper.assertTrue(mat.getBlock() instanceof BewareDoormatBlock && mat.getValue(FacingDecorationBlock.FACING) == Direction.NORTH,
				"It lies facing whoever places it, its device read the right way up by them");
		level.setBlock(at.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item("beware_doormat")) == 1, "With nothing under it, it goes, dropping itself");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theCrashedWitchKicksAndCackles(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 5));
		level.setBlock(wall, Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 2));
		useOnFace(player, wall, Direction.NORTH, new ItemStack(item("crashed_witch")));
		BlockPos at = wall.north();
		BlockState hung = level.getBlockState(at);
		helper.assertTrue(hung.getBlock() instanceof CrashedWitchBlock && hung.getValue(WallDecorationBlock.FACING) == Direction.NORTH
				&& level.getBlockEntity(at) instanceof DecorationBlockEntity, "She hangs out of the tree's face, facing out, with her block entity");
		use(player, level, at, ItemStack.EMPTY);
		float far = Float.MAX_VALUE;
		helper.assertTrue(CrashedWitchBlock.kick(false, 63.0F, 0, far, -1.0F) == 0.0F, "Far off and between her bursts, she lies still");
		float burst = CrashedWitchBlock.kick(false, 21.0F, 0, far, -1.0F);
		helper.assertTrue(Math.abs(burst) > 20.0F && Math.abs(burst) <= CrashedWitchBlock.KICK, "In a burst she kicks: " + burst);
		helper.assertTrue(Math.abs(CrashedWitchBlock.kick(false, 63.0F, 0, 2.0, -1.0F) - CrashedWitchBlock.KICK) < 0.01F,
				"With someone near she kicks the whole time");
		helper.assertTrue(Math.abs(CrashedWitchBlock.kick(false, 63.0F, 0, far, CrashedWitchBlock.BURST / 2.0F)) > 20.0F, "and after she cackles");
		helper.assertTrue(Math.abs(CrashedWitchBlock.kick(false, 21.0F, 0, far, -1.0F) + CrashedWitchBlock.kick(true, 21.0F, 0, far, -1.0F)) < 0.01F,
				"her legs out of step");
		helper.runAfterDelay(1, () -> {
			long cackled = level.getBlockEntity(at) instanceof DecorationBlockEntity watch ? watch.marked() : Long.MIN_VALUE;
			helper.assertTrue(cackled != Long.MIN_VALUE, "An empty hand makes her cackle");
			use(player, level, at, ItemStack.EMPTY);
			helper.runAfterDelay(1, () -> {
				helper.assertTrue(level.getBlockEntity(at) instanceof DecorationBlockEntity watch && watch.marked() == cackled,
						"and not again until she has done");
				level.setBlock(wall, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item("crashed_witch")) == 1, "With the tree gone she goes, dropping herself");
				helper.succeed();
			});
		});
	}

	@GameTest
	public void frontYardDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : new String[] {"haunted_mailbox", "pumpkin_path_light", "skeleton_dog", "skeleton_cat", "skeleton_crow", "rising_zombie",
				"gargoyle_knocker", "bat_wreath", "beware_doormat", "crashed_witch"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}
