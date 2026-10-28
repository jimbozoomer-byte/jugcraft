package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinPathLightBlock;
import java.util.function.Predicate;
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
 * Front-Yard Haunt Scenes, part 1 (decor set 7; docs/features/front-yard-haunt.md): the Haunted Mailbox stands two
 * blocks high, facing the player, its flag down; an empty hand raises its flag on both blocks and lowers it again;
 * broken at its box it comes away whole and drops once. Its haunting is worked out the same everywhere: with someone near,
 * its door falls open {@value HauntedMailboxBlock#OPEN} degrees, its eyes showing; with no one, it shuts and then only
 * rattles open a crack now and then, its eyes hidden. The Pumpkin Path Light stands two blocks high, placed lit, its
 * lower block alone giving light {@value PumpkinPathLightBlock#LIGHT}; an empty hand puts it out and lights it again;
 * broken, it drops once; its lantern sways a little, to and fro. Their recipes and loot load (two lights a craft).
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

	@GameTest
	public void frontYardDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : new String[] {"haunted_mailbox", "pumpkin_path_light"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}
