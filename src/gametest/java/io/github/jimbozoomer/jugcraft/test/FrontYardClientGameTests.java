package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.AutumnWreathBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DoorWreath;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client game test for Front-Yard Haunt Scenes, parts 1 to 4 (decor set 7): a front path of gravel across a lawn, a
 * Haunted Mailbox at the street end with its flag up, and three Pumpkin Path Lights along the path, lit, their lanterns
 * swaying (drawn by client/PumpkinPathLightRenderer). By the mailbox a Skeleton Dog sits and a Skeleton Crow perches on a
 * fence post; on the lawn a Skeleton Cat arches and a Rising Zombie claws its way out of the ground (their moving parts
 * drawn by client/SkeletonAnimalRenderer and client/RisingZombieRenderer). At the path's end a house front of dark oak
 * has a Gargoyle Door Knocker on its door (drawn by client/DoorWreathRenderer), a Bat Wreath on its wall and a Beware
 * Doormat at its step; by the path a tree trunk with a Crashed Witch out to her hips in it (client/CrashedWitchRenderer).
 * Photographed from along the path, from far
 * enough that the mailbox stays shut; from the street close by the mailbox, near enough that its door falls open and its
 * eyes stare out (client/HauntedMailboxRenderer), the dog wags and the crow watches the camera; from the lawn close by the
 * zombie, near enough that it reaches higher; from the path before the door, the knocker rapping; from the path by the
 * witch, near enough that she kicks the whole time; and by night (CI job
 * {@code client}). They go into
 * docs/features/front-yard-haunt.md.
 */
public class FrontYardClientGameTests implements FabricClientGameTest {
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
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 1, z - 8, x + 6, y - 1, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 8, x + 6, y + 5, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:gravel".formatted(x, y - 1, z - 8, x + 1, y - 1, z + 12));
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			shoot(context, singleplayer, x + 4, y + 1, z + 11, 160, 15, "jugcraft_front_yard_path");
			// From the street, three blocks from the mailbox: near enough for its door to fall open on the camera.
			shoot(context, singleplayer, x - 1, y + 1, z - 6, 0, 15, "jugcraft_front_yard_mailbox");
			// From the lawn, two blocks from the zombie and the cat: near enough for it to reach higher.
			shoot(context, singleplayer, x - 1, y + 1, z + 8, 135, 20, "jugcraft_front_yard_bones");
			// Before the door, four blocks from it, the knocker set rapping three ticks before the shot.
			server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y, z + 8));
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y + 1, z + 8.5, 0, 10));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			server.runOnServer(minecraft -> knock(minecraft.overworld(), origin));
			context.waitTicks(3);
			context.takeScreenshot("jugcraft_front_yard_door");
			// On the path, three blocks from the witch: near enough that she kicks the whole time.
			shoot(context, singleplayer, x + 1, y + 1, z + 3, 270, 10, "jugcraft_front_yard_witch");
			server.runCommand("time set midnight");
			shoot(context, singleplayer, x + 4, y + 1, z + 11, 160, 15, "jugcraft_front_yard_night");
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

	/** Stands a two-high piece at {@code at} facing {@code facing}, each of its blocks set as {@code dressed} says. */
	private static void stand(ServerLevel level, MultiDecorationBlock block, BlockPos at, Direction facing,
			java.util.function.UnaryOperator<BlockState> dressed) {
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, facing);
		level.setBlock(at, state, Block.UPDATE_ALL);
		block.setPlacedBy(level, at, state, null, new ItemStack(block));
		for (int part = 0; part < block.cells().length; part++) {
			BlockPos there = block.partPos(at, facing, part);
			level.setBlock(there, dressed.apply(level.getBlockState(there)), Block.UPDATE_ALL);
		}
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// The mailbox at the street end of the path (to the north), its door to the street, its flag up.
		stand(level, (MultiDecorationBlock) JugcraftAgriculture.block("haunted_mailbox"), origin.offset(-1, 0, -3), Direction.NORTH,
				state -> state.setValue(HauntedMailboxBlock.FLAG, true));
		// Three path lights down the path's east side, their crooks over it, lit.
		MultiDecorationBlock light = (MultiDecorationBlock) JugcraftAgriculture.block("pumpkin_path_light");
		for (int k = 0; k < 3; k++) {
			stand(level, light, origin.offset(2, 0, 1 + 4 * k), Direction.WEST, state -> state.setValue(MultiDecorationBlock.LIT, true));
		}
		// The dog sits by the mailbox, facing the street; the crow perches on a fence post beside it.
		animal(level, "skeleton_dog", origin.offset(-2, 0, -2), Direction.NORTH);
		level.setBlock(origin.offset(-3, 0, -3), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
		animal(level, "skeleton_crow", origin.offset(-3, 1, -3), Direction.NORTH);
		// On the lawn the cat arches at the path, and the zombie rises facing it.
		animal(level, "skeleton_cat", origin.offset(-2, 0, 3), Direction.EAST);
		stand(level, (MultiDecorationBlock) JugcraftAgriculture.block("rising_zombie"), origin.offset(-3, 0, 6), Direction.EAST, state -> state);
		doorway(level, origin);
		// A tree trunk east of the path, the witch out to her hips in its west face, kicking toward the path.
		for (int dy = 0; dy <= 3; dy++) {
			level.setBlock(origin.offset(5, dy, 3), Blocks.OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
		}
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				level.setBlock(origin.offset(5 + dx, 4, 3 + dz), Blocks.OAK_LEAVES.defaultBlockState()
						.setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
			}
		}
		level.setBlock(origin.offset(4, 1, 3), JugcraftAgriculture.block("crashed_witch").defaultBlockState()
				.setValue(io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
	}

	/** The house front at the path's end: its door, the knocker on its door, the wreath on its wall, the mat at its step. */
	private static void doorway(ServerLevel level, BlockPos origin) {
		for (int dx = -2; dx <= 3; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				level.setBlock(origin.offset(dx, dy, 12), Blocks.DARK_OAK_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		BlockPos door = origin.offset(0, 0, 12);
		BlockState lower = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
		level.setBlock(door, lower, Block.UPDATE_ALL);
		level.setBlock(door.above(), lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		DoorWreath.attach(level, door.above(), true, JugcraftAgriculture.block("gargoyle_knocker").defaultBlockState());
		level.setBlock(origin.offset(2, 1, 11), JugcraftAgriculture.block("bat_wreath").defaultBlockState().setValue(AutumnWreathBlock.FACING,
				Direction.NORTH), Block.UPDATE_ALL);
		animal(level, "beware_doormat", origin.offset(0, 0, 11), Direction.NORTH);
	}

	private static void knock(ServerLevel level, BlockPos origin) {
		BlockPos door = origin.offset(0, 1, 12);
		for (DoorWreath ornament : level.getEntitiesOfClass(DoorWreath.class, new AABB(door).inflate(1.0), e -> e.getPos().equals(door))) {
			ornament.knock();
		}
	}

	private static void animal(ServerLevel level, String id, BlockPos at, Direction facing) {
		level.setBlock(at, JugcraftAgriculture.block(id).defaultBlockState().setValue(FacingDecorationBlock.FACING, facing), Block.UPDATE_ALL);
	}
}
