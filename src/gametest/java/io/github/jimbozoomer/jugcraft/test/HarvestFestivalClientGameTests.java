package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GiantCornucopiaBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.HayWagonBlock;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PrizeRibbonBoardBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SteamTractionEngineBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SteamTractionEngineBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightHookBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightHookBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightsItem;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * Client game test for the Harvest Festival Grounds (decor set 10): part 1's Giant Cornucopia on the grass, facing the
 * camera, photographed by day from the front, from the west into its mouth and the harvest spilling out of it, and from
 * the north-east over its tail; then, in its place, part 2's show: the Giant Cabbage, Marrow, Turnip and Carrot in a row
 * facing the camera, the Prize Ribbon Board behind them with all three ribbons hung, from the front and then close on
 * the board; then part 3's Steam Traction Engine, fired, its motion running, with the Hay Wagon behind it, from its
 * flywheel side, from ahead of it, close over its motion, and along the wagon; then part 4's Harvest Bunting strung
 * between two hooks on spruce posts, and an armor stand under it in a Sack-Race Sack (CI job {@code client}). They go
 * into docs/features/harvest-festival-grounds.md.
 */
public class HarvestFestivalClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 7, y - 1, z - 12, x + 7, y - 1, z + 6));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 7, y, z - 12, x + 7, y + 5, z + 6));
			server.runOnServer(minecraft -> cornucopia(minecraft.overworld(), origin.offset(1, 0, 1)));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x, y + 1, z - 4, 0, 20, "jugcraft_harvest_festival_cornucopia");
			shoot(context, singleplayer, x - 5, y, z + 2, -90, 15, "jugcraft_harvest_festival_cornucopia_mouth");
			shoot(context, singleplayer, x + 4, y + 2, z - 3, 45, 30, "jugcraft_harvest_festival_cornucopia_tail");
			// Part 2: the show, where the cornucopia was.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 7, y, z - 6, x + 6, y + 5, z + 6));
			server.runOnServer(minecraft -> show(minecraft.overworld(), origin));
			context.waitTicks(20);
			shoot(context, singleplayer, x, y + 1, z - 5, 0, 15, "jugcraft_harvest_festival_show");
			shoot(context, singleplayer, x, y + 1, z, 0, 5, "jugcraft_harvest_festival_ribbon_board");
			// Part 3: the engine, fired, and the wagon behind it, where the show was.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 7, y, z - 12, x + 7, y + 5, z + 6));
			server.runOnServer(minecraft -> engine(minecraft.overworld(), origin.offset(1, 0, -6), origin.offset(1, 0, -1)));
			context.waitTicks(60);
			shoot(context, singleplayer, x + 6, y + 1, z - 3, 90, 10, "jugcraft_harvest_festival_traction_engine");
			shoot(context, singleplayer, x + 5, y + 2, z - 10, 30, 15, "jugcraft_harvest_festival_traction_engine_front");
			shoot(context, singleplayer, x + 3, y + 3, z - 4, 90, 35, "jugcraft_harvest_festival_traction_engine_motion");
			shoot(context, singleplayer, x + 6, y + 1, z + 1, 90, 10, "jugcraft_harvest_festival_hay_wagon");
			// Part 4: the bunting and a sack, where the engine was.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 7, y, z - 12, x + 7, y + 5, z + 6));
			server.runOnServer(minecraft -> bunting(minecraft.overworld(), origin));
			context.waitTicks(20);
			shoot(context, singleplayer, x, y + 1, z - 4, 0, 5, "jugcraft_harvest_festival_bunting");
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

	/**
	 * The show: the four vegetables in a row along {@code origin}'s row, facing north, and the Prize Ribbon Board two
	 * blocks behind the middle of them with its three ribbons hung.
	 */
	private static void show(ServerLevel level, BlockPos origin) {
		String[] ids = {"giant_cabbage", "giant_marrow", "giant_turnip", "giant_carrot"};
		for (int i = 0; i < ids.length; i++) {
			level.setBlock(origin.offset(3 - 2 * i, 0, 0), JugcraftAgriculture.block(ids[i]).defaultBlockState()
					.setValue(FacingDecorationBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
		}
		PrizeRibbonBoardBlock board = (PrizeRibbonBoardBlock) JugcraftAgriculture.block("prize_ribbon_board");
		BlockState hung = board.defaultBlockState().setValue(PrizeRibbonBoardBlock.FACING, Direction.NORTH).setValue(PrizeRibbonBoardBlock.FIRST, true)
				.setValue(PrizeRibbonBoardBlock.SECOND, true).setValue(PrizeRibbonBoardBlock.THIRD, true);
		BlockPos at = origin.offset(0, 0, 2);
		level.setBlock(at, hung, Block.UPDATE_ALL);
		board.setPlacedBy(level, at, hung, null, new ItemStack(board));
	}

	/**
	 * The Steam Traction Engine, its first block at {@code engineAt} facing north, fired with its firebox full; and the
	 * Hay Wagon behind it facing the same way, its first block at {@code wagonAt}, its drawbar at the engine's hook.
	 */
	private static void engine(ServerLevel level, BlockPos engineAt, BlockPos wagonAt) {
		SteamTractionEngineBlock engine = (SteamTractionEngineBlock) JugcraftAgriculture.block("steam_traction_engine");
		BlockState state = engine.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlock(engineAt, state, Block.UPDATE_ALL);
		engine.setPlacedBy(level, engineAt, state, null, new ItemStack(engine));
		if (level.getBlockEntity(engineAt) instanceof SteamTractionEngineBlockEntity firebox) {
			firebox.setFuel(SteamTractionEngineBlock.MAX_FUEL);
		}
		engine.setLit(level, engineAt, true);
		HayWagonBlock wagon = (HayWagonBlock) JugcraftAgriculture.block("hay_wagon");
		BlockState hay = wagon.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlock(wagonAt, hay, Block.UPDATE_ALL);
		wagon.setPlacedBy(level, wagonAt, hay, null, new ItemStack(wagon));
	}

	/**
	 * Harvest Bunting strung between two hooks on spruce posts two high, six blocks apart either side of the middle,
	 * and an armor stand under it in a Sack-Race Sack, facing north.
	 */
	private static void bunting(ServerLevel level, BlockPos origin) {
		for (int across : new int[] {-3, 3}) {
			for (int up = 0; up < 2; up++) {
				level.setBlock(origin.offset(across, up, 0), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), Block.UPDATE_ALL);
			}
			level.setBlock(origin.offset(across, 2, 0), JugcraftAgriculture.block("string_light_hook").defaultBlockState()
					.setValue(StringLightHookBlock.FACE, AttachFace.FLOOR).setValue(StringLightHookBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
		}
		StringLightsItem.string(level, origin.offset(-3, 2, 0), origin.offset(3, 2, 0), StringLightHookBlockEntity.Strand.HARVEST);
		ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
		if (stand != null) {
			stand.snapTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, 180.0F, 0.0F);
			stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(JugcraftAgriculture.item("sack_race_sack")));
			level.addFreshEntity(stand);
		}
	}

	/** The Giant Cornucopia, its first block (its tail's) at {@code master}, facing north: its mouth to the west. */
	private static void cornucopia(ServerLevel level, BlockPos master) {
		GiantCornucopiaBlock block = (GiantCornucopiaBlock) JugcraftAgriculture.block("giant_cornucopia");
		BlockState state = block.defaultBlockState().setValue(GiantCornucopiaBlock.FACING, Direction.NORTH);
		level.setBlock(master, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, master, state, null, new ItemStack(block));
	}
}
