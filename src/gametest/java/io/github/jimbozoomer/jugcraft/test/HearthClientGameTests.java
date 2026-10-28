package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.Knitting;
import io.github.jimbozoomer.jugcraft.agriculture.KnittingBasketBlock;
import io.github.jimbozoomer.jugcraft.agriculture.KnittingBasketBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.BlanketLadderBlock;
import io.github.jimbozoomer.jugcraft.agriculture.BlanketLadderBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FirewoodRackBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FirewoodRackBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.StoneFireplaceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StovetopBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WoodstoveBlock;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client game test for Cozy Fall Hearth, part 1 (decor set 5): a room's corner of spruce floor and a plank back wall, a
 * Stone Fireplace against it, lit and dressed with its Mantel Garland, and a second beside it out and bare; and part 2's
 * two Woodstoves lit along the wall, a Tea Kettle boiling on one (its lid drawn by client/TeaKettleRenderer) and a
 * Simmer Pot simmering on the other; and part 3's Firewood Rack full of logs beside them and a Chopping Block before it
 * (its hatchet drawn by client/ChoppingBlockRenderer); and part 4's Blanket Ladder against the wall west of the
 * fireplaces with three throws on it and a Knitting Basket of four coloured balls and its needles before it (the throws
 * and the yarn drawn by client/BlanketLadderRenderer and client/KnittingBasketRenderer); built in a real client and
 * photographed from the room, close by, at the stoves, at the woodpile, at the comforts and by night (CI job
 * {@code client}). The fire is its own full-bright model in an
 * animated texture, so the pictures show it burns in its firebox and behind the stoves' mica. They go into
 * docs/features/cozy-fall-hearth.md.
 */
public class HearthClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(x - 6, y - 1, z - 3, x + 8, y - 1, z + 10));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 3, x + 8, y + 6, z + 10));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 1, y + 1, z + 7, 180, 10, "jugcraft_hearth");
			shoot(context, singleplayer, x + 1, y, z + 3, 180, 15, "jugcraft_hearth_close");
			shoot(context, singleplayer, x + 5, y + 1, z + 3, 180, 30, "jugcraft_hearth_stoves");
			shoot(context, singleplayer, x + 7, y + 1, z + 5, 180, 20, "jugcraft_hearth_woodpile");
			shoot(context, singleplayer, x - 5, y + 1, z + 4, 180, 20, "jugcraft_hearth_comforts");
			server.runCommand("time set midnight");
			shoot(context, singleplayer, x + 1, y, z + 5, 180, 5, "jugcraft_hearth_night");
		}
	}

	/** Stands the player at a spot (on an invisible barrier) and takes a screenshot. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z,
			int yaw, int pitch, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}

	private static void fireplace(ServerLevel level, BlockPos at, boolean lit, boolean garland) {
		StoneFireplaceBlock fireplace = (StoneFireplaceBlock) JugcraftAgriculture.block("stone_fireplace");
		BlockState state = fireplace.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH)
				.setValue(MultiDecorationBlock.LIT, lit).setValue(StoneFireplaceBlock.GARLAND, garland);
		level.setBlock(at, state, Block.UPDATE_ALL);
		fireplace.setPlacedBy(level, at, state, null, new ItemStack(fireplace));
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// A plank back wall to the north, three blocks high, across the room.
		for (int across = -5; across <= 7; across++) {
			for (int up = 0; up < 4; up++) {
				level.setBlock(origin.offset(across, up, -1), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		// The fireplace in the middle, facing the room (its blocks run east from its first), lit and dressed; a second to the
		// west, out and bare.
		fireplace(level, origin, true, true);
		fireplace(level, origin.offset(-4, 0, 0), false, false);
		// Part 2: two woodstoves lit along the wall to the east, a kettle on one and a simmer pot on the other, both heated.
		for (int across : new int[] {4, 6}) {
			level.setBlock(origin.offset(across, 0, 0), JugcraftAgriculture.block("woodstove").defaultBlockState()
					.setValue(FacingDecorationBlock.FACING, Direction.SOUTH).setValue(WoodstoveBlock.LIT, true), Block.UPDATE_ALL);
		}
		level.setBlock(origin.offset(4, 1, 0), JugcraftAgriculture.block("tea_kettle").defaultBlockState()
				.setValue(FacingDecorationBlock.FACING, Direction.SOUTH).setValue(StovetopBlock.HEATED, true), Block.UPDATE_ALL);
		level.setBlock(origin.offset(6, 1, 0), JugcraftAgriculture.block("simmer_pot").defaultBlockState()
				.setValue(FacingDecorationBlock.FACING, Direction.SOUTH).setValue(StovetopBlock.HEATED, true), Block.UPDATE_ALL);
		// Part 3: a full firewood rack against the wall beyond the stoves, and a chopping block before it.
		BlockPos rack = origin.offset(7, 0, 0);
		level.setBlock(rack, JugcraftAgriculture.block("firewood_rack").defaultBlockState().setValue(FacingDecorationBlock.FACING, Direction.SOUTH)
				.setValue(FirewoodRackBlock.LOGS, FirewoodRackBlock.CAPACITY), Block.UPDATE_ALL);
		if (level.getBlockEntity(rack) instanceof FirewoodRackBlockEntity logs) {
			for (int i = 0; i < FirewoodRackBlock.CAPACITY; i++) {
				logs.stack(new ItemStack(i % 3 == 0 ? Items.BIRCH_LOG : Items.OAK_LOG));
			}
		}
		level.setBlock(origin.offset(7, 0, 2), JugcraftAgriculture.block("chopping_block").defaultBlockState()
				.setValue(FacingDecorationBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		// Part 4: a blanket ladder leant on the wall at its west end with three throws on it, and a knitting basket before it.
		BlanketLadderBlock ladder = (BlanketLadderBlock) JugcraftAgriculture.block("blanket_ladder");
		BlockPos foot = origin.offset(-5, 0, 0);
		BlockState leant = ladder.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		level.setBlock(foot, leant, Block.UPDATE_ALL);
		ladder.setPlacedBy(level, foot, leant, null, new ItemStack(ladder));
		if (level.getBlockEntity(foot) instanceof BlanketLadderBlockEntity throwsOn) {
			for (ItemStack carpet : new ItemStack[] {new ItemStack(Items.RED_CARPET), new ItemStack(Items.YELLOW_CARPET), new ItemStack(Items.GREEN_CARPET)}) {
				throwsOn.hang(carpet);
			}
		}
		BlockPos basket = origin.offset(-5, 0, 2);
		level.setBlock(basket, JugcraftAgriculture.block("knitting_basket").defaultBlockState().setValue(FacingDecorationBlock.FACING, Direction.SOUTH)
				.setValue(KnittingBasketBlock.NEEDLES, true), Block.UPDATE_ALL);
		if (level.getBlockEntity(basket) instanceof KnittingBasketBlockEntity yarn) {
			for (int rgb : new int[] {0xF0E6D2, 0xB02E26, 0x3C44AA, 0xF0AF15}) {
				yarn.put(Knitting.yarn(rgb, 1));
			}
			yarn.put(new ItemStack(JugcraftAgriculture.item("knitting_needles")));
		}
	}
}
