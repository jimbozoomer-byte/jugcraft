package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A multi-block prop ({@link MultiDecorationBlock}) reaching across a claim edge is protected as a whole: a stranger
 * cannot place one into someone's claim, nor break one, in survival or in creative, from the block outside the claim
 * (breaking it would bring down the blocks inside); an explosion outside the claim leaves it whole; its owner places
 * and breaks it as before, and it drops once. Every registered prop that can reach across a claim edge refuses a
 * stranger's break. Written for the server game tests; not run here (no Minecraft toolchain in the environment that
 * wrote them).
 */
public class MultiblockClaimGameTests {

	@GameTest(maxTicks = 60)
	public void bucketsCannotWashAwayThinArchParts(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = TestPlayers.survivor(helper);
		BlockPos master = helper.absolutePos(new BlockPos(6, 2, 4));
		for (String id : new String[] {"timber_harvest_arch", "haunted_archway"}) {
			MultiDecorationBlock arch = (MultiDecorationBlock) JugcraftAgriculture.block(id);
			BlockPos[] cells = new BlockPos[arch.cells().length];
			for (int part = 0; part < cells.length; part++) {
				cells[part] = arch.partPos(master, Direction.NORTH, part);
				level.setBlock(cells[part], arch.defaultBlockState().setValue(arch.partProperty(), part), Block.UPDATE_CLIENTS);
			}
			for (BlockPos cell : cells) {
				helper.assertTrue(level.getBlockState(cell).isSolid(), id + " has fluid-resistant structural parts");
				helper.assertFalse(((BucketItem) Items.WATER_BUCKET).emptyContents(player, level, cell, null), id + " refuses water replacement");
				helper.assertFalse(((BucketItem) Items.LAVA_BUCKET).emptyContents(player, level, cell, null), id + " refuses lava replacement");
			}
			helper.assertTrue(whole(level, arch, cells), id + " remains whole after every bucket attempt");
			level.removeBlock(master, false);
		}
		helper.succeed();
	}
	@GameTest(maxTicks = 60)
	public void aPropAcrossAClaimEdgeIsProtectedWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// Far from every other test, as the claim tests do. The archway's first block is just east of the claimed chunk;
		// placed facing north, its other blocks go west, into the claim.
		BlockPos far = helper.absolutePos(new BlockPos(12288, 3, 12288));
		BlockPos master = new BlockPos(Math.floorDiv(far.getX(), 16) * 16 + 16, far.getY(), Math.floorDiv(far.getZ(), 16) * 16 + 8);
		MultiDecorationBlock archway = (MultiDecorationBlock) JugcraftAgriculture.block("haunted_archway");
		Item item = JugcraftAgriculture.item("haunted_archway");
		ServerPlayer owner = TestPlayers.survivor(helper);
		ServerPlayer stranger = TestPlayers.survivor(helper);
		owner.setGameMode(GameType.SURVIVAL);
		stranger.setGameMode(GameType.SURVIVAL);
		// Both look south, so the archway faces north; the owner stands well clear of the explosion below.
		owner.snapTo(Vec3.atBottomCenterOf(master.south(12)));
		stranger.snapTo(Vec3.atBottomCenterOf(master.south(4).east(3)));
		var land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(master.west());
		BlockPos[] cells = new BlockPos[archway.cells().length];
		for (int part = 0; part < cells.length; part++) {
			cells[part] = archway.partPos(master, Direction.NORTH, part);
		}
		// Remote claim fixtures need entity-ticking chunks before testing item drops.
		int cx = Math.floorDiv(master.getX(), 16), cz = Math.floorDiv(master.getZ(), 16);
		for (int dx = -1; dx <= 1; dx++) level.setChunkForced(cx + dx, cz, true);
		helper.runAtTickTime(10, () -> {
		try {
			land.assign(book, key, book.account(owner.getUUID(), "PropClaimOwner"), 0);
			helper.assertTrue(LandRegistry.keyOf(master) != key && LandRegistry.keyOf(cells[cells.length - 1]) == key,
					"The archway's first block is outside the claim and its far pillar inside");
			for (int x = -4; x <= 4; x++) {
				for (int z = -2; z <= 2; z++) {
					level.setBlockAndUpdate(master.offset(x, -1, z), Blocks.STONE.defaultBlockState());
					for (int y = 0; y <= 3; y++) {
						level.setBlockAndUpdate(master.offset(x, y, z), Blocks.AIR.defaultBlockState());
					}
				}
			}
			placeAt(stranger, master.below(), new ItemStack(item));
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(cells[cells.length - 1]).isAir(),
					"A stranger cannot place a prop that would reach into someone's claim");
			helper.assertTrue(stranger.getMainHandItem().is(item), "and keeps it");
			placeAt(owner, master.below(), new ItemStack(item));
			for (BlockPos cell : cells) {
				helper.assertTrue(level.getBlockState(cell).is(archway), "The claim's owner places it across the edge: " + cell);
			}
			stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			BlockHitResult use = new BlockHitResult(Vec3.atCenterOf(master), Direction.NORTH, master, false);
			stranger.gameMode.useItemOn(stranger, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, use);
			for (BlockPos cell : cells) {
				helper.assertTrue(level.getBlockState(cell).getValue(MultiDecorationBlock.LIT), "An unclaimed corner cannot toggle private lights");
			}
			owner.gameMode.useItemOn(owner, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, use);
			for (BlockPos cell : cells) {
				helper.assertFalse(level.getBlockState(cell).getValue(MultiDecorationBlock.LIT), "The owner can still toggle every part");
			}
			helper.assertFalse(stranger.gameMode.destroyBlock(master), "A stranger in survival cannot break its block outside the claim");
			helper.assertTrue(whole(level, archway, cells), "and it stands whole");
			stranger.setGameMode(GameType.CREATIVE);
			helper.assertFalse(stranger.gameMode.destroyBlock(master), "nor can a stranger in creative who is not an operator");
			helper.assertTrue(whole(level, archway, cells), "and it still stands whole");
			// An explosion just outside the claim, beside its unclaimed block.
			level.explode(null, master.getX() + 1.5, master.getY() + 0.5, master.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
			helper.assertTrue(whole(level, archway, cells), "An explosion outside the claim leaves it whole");
			helper.assertTrue(dropped(level, master, item) == 0, "and drops nothing");
			helper.assertTrue(owner.gameMode.destroyBlock(master), "Its owner can break it");
			for (BlockPos cell : cells) {
				helper.assertTrue(!level.getBlockState(cell).is(archway), "Broken by its owner, it all comes down: " + cell);
			}
			helper.assertTrue(dropped(level, master, item) == 1, "and drops once");
		} finally {
			land.abandon(owner, book, key, true, 0);
			for (int x = -4; x <= 4; x++) {
				for (int z = -2; z <= 2; z++) {
					for (int y = -1; y <= 3; y++) {
						level.setBlockAndUpdate(master.offset(x, y, z), Blocks.AIR.defaultBlockState());
					}
				}
			}
			level.getEntitiesOfClass(ItemEntity.class, new AABB(master).inflate(8.0)).forEach(ItemEntity::discard);
			for (int dx = -1; dx <= 1; dx++) level.setChunkForced(cx + dx, cz, false);
		}
		helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void everyPropAcrossAClaimEdgeRefusesAStrangersBreak(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos far = helper.absolutePos(new BlockPos(14336, 3, 14336));
		BlockPos master = new BlockPos(Math.floorDiv(far.getX(), 16) * 16 + 16, far.getY(), Math.floorDiv(far.getZ(), 16) * 16 + 8);
		ServerPlayer owner = helper.makeMockServerPlayerInLevel();
		ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
		owner.setGameMode(GameType.SURVIVAL);
		stranger.setGameMode(GameType.CREATIVE);
		var land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(master.west());
		int checked = 0;
		try {
			land.assign(book, key, book.account(owner.getUUID(), "EveryPropClaimOwner"), 0);
			for (Block block : BuiltInRegistries.BLOCK) {
				if (!(block instanceof MultiDecorationBlock prop)) {
					continue;
				}
				for (Direction facing : Direction.Plane.HORIZONTAL) {
					boolean across = false;
					for (int part = 0; part < prop.cells().length; part++) {
						across |= LandRegistry.keyOf(prop.partPos(master, facing, part)) == key;
					}
					if (!across) {
						continue; // In this facing it stays outside the claim (one block across, or built away from it).
					}
					BlockState state = prop.defaultBlockState().setValue(MultiDecorationBlock.FACING, facing);
					String name = BuiltInRegistries.BLOCK.getKey(block) + " facing " + facing;
					helper.assertFalse(prop.mayChangeWhole(level, master, facing, stranger), name + ": a stranger may not change all of it");
					helper.assertFalse(PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, stranger, master, state, null),
							name + ": a stranger cannot break it at its block outside the claim");
					helper.assertTrue(prop.mayChangeWhole(level, master, facing, owner)
									&& PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, owner, master, state, null),
							name + ": the claim's owner can");
					checked++;
					break;
				}
			}
		} finally {
			land.abandon(owner, book, key, true, 0);
		}
		helper.assertTrue(checked > 0, "Some prop reaches across a claim edge (" + checked + " checked)");
		helper.succeed();
	}

	private static boolean whole(Level level, MultiDecorationBlock prop, BlockPos[] cells) {
		for (int part = 0; part < cells.length; part++) {
			BlockState state = level.getBlockState(cells[part]);
			if (!state.is(prop) || prop.part(state) != part) {
				return false;
			}
		}
		return true;
	}

	/** Places {@code held} from the player's hand on the top of the block at {@code below} (absolute). */
	private static void placeAt(ServerPlayer player, BlockPos below, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
	}

	private static int dropped(Level level, BlockPos at, Item item) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(8.0), entity -> entity.getItem().is(item)).stream()
				.mapToInt(entity -> entity.getItem().getCount()).sum();
	}
}
