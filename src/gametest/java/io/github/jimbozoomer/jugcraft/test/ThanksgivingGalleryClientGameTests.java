package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CandleCenterpieceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToyCornTossBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FestivalToySpinningTopBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.ThanksgivingDishes;
import io.github.jimbozoomer.jugcraft.agriculture.WoodlandRushMatBlock;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Client gallery of the Thanksgiving package's new and refined pieces (groups A to D: docs/features/thanksgiving-homestead.md,
 * thanksgiving-woodland-crafts.md, thanksgiving-town-details.md and thanksgiving-toys.md): four small showrooms on a
 * flat lawn, every piece facing south towards the cameras, photographed at noon from the front and three quarters, then
 * the lit pieces at midnight (CI job {@code client}). It only builds and photographs: what each piece does is covered by
 * the groups' server game tests.
 *
 * <ul>
 * <li>Homestead (A): the High-Backed Oak Settle and the Carved Dish Dresser (two by two, every part set), the Pilgrim Peg
 * Rail and the Embroidered Harvest Sampler on a plank wall, the Pewter Pitcher and Basin, the Pewter Chamberstick lit and
 * unlit, and the refined Quilt Rail and Butter Churn.</li>
 * <li>Woodland (B): the seven floor crafts in a row and a three by three Rush Floor Mat, its sides joined.</li>
 * <li>Town (C): the Timber Harvest Arch (all eleven parts) over a path that runs through its empty opening, the Hanging
 * Crock and Herb Rack under a plank beam, the Pumpkin Carving Bench, the Folded Linen Stack, the Copper Chafing Dish lit
 * and unlit, and a Window Box holding an orange mum and a Warty Gourd.</li>
 * <li>Toys (D): the Harvest Photo Frame Backdrop (all ten parts, its bulbs on, then off) and the seven toys in a row.</li>
 * <li>Feast (batch 2; docs/features/thanksgiving-dishes.md and thanksgiving-toys.md): the six side dishes on oak plank
 * tables, each in its one set-down state; before them the remodelled Spinning Top at rest upright, mid-spin and wobbling,
 * and the Tabletop Corn Toss Set fresh, mid-round and with every bag on the board. The server's ticks are frozen while
 * they are photographed, so the tops hold their frames. The toys' row is also shot close and from behind, and the
 * backdrop with its bulbs off at noon.</li>
 * </ul>
 * Multi-block pieces are set part by part, as the Haunted Archway is in Decor9ClientGameTests.
 */
public class ThanksgivingGalleryClientGameTests implements FabricClientGameTest {
	/** Each showroom's first column, east of the spawn point (blocks). */
	private static final int HOMESTEAD = 0;
	private static final int WOODLAND = 22;
	private static final int TOWN = 44;
	private static final int TOYS = 68;
	private static final int FEAST = 90;

	@Override
	public void runTest(ClientGameTestContext context) {
		boolean hudHidden = context.computeOnClient(client -> client.gui.hud.isHidden());
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
			server.runCommand("gamerule minecraft:advance_weather false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			// A flat lawn, cleared above, in four slices (a fill is limited in size).
			for (int from = -6; from <= 84; from += 30) {
				server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x + from, y - 3, z - 12, x + from + 29, y - 1, z + 16));
				server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x + from, y, z - 12, x + from + 29, y + 12, z + 16));
			}
			context.waitTicks(10);
			server.runOnServer(minecraft -> build(minecraft.overworld(), origin));
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) client.gui.hud.toggle();
			});
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();

			// Noon, front and three quarters, group by group.
			shoot(context, singleplayer, x + HOMESTEAD + 7, y + 2, z + 10, 180, 8, "jugcraft_thanksgiving_a_homestead_front");
			shoot(context, singleplayer, x + HOMESTEAD + 15, y + 3, z + 8, 135, 15, "jugcraft_thanksgiving_a_homestead_three_quarter");
			shoot(context, singleplayer, x + HOMESTEAD + 3, y + 1, z + 4, 180, 10, "jugcraft_thanksgiving_a_settle_and_dresser");
			shoot(context, singleplayer, x + WOODLAND + 6, y + 2, z + 10, 180, 12, "jugcraft_thanksgiving_b_woodland_front");
			shoot(context, singleplayer, x + WOODLAND + 14, y + 4, z + 8, 135, 22, "jugcraft_thanksgiving_b_woodland_three_quarter");
			shoot(context, singleplayer, x + TOWN + 2, y + 1, z + 9, 180, 5, "jugcraft_thanksgiving_c_arch_front_through_opening");
			shoot(context, singleplayer, x + TOWN + 2, y + 1, z - 9, 0, 5, "jugcraft_thanksgiving_c_arch_back");
			shoot(context, singleplayer, x + TOWN + 8, y + 4, z + 7, 135, 15, "jugcraft_thanksgiving_c_town_three_quarter");
			shoot(context, singleplayer, x + TOWN + 12, y + 2, z + 6, 180, 15, "jugcraft_thanksgiving_c_town_details_front");
			shoot(context, singleplayer, x + TOYS + 6, y + 2, z + 10, 180, 8, "jugcraft_thanksgiving_d_toys_front");
			shoot(context, singleplayer, x + TOYS + 14, y + 4, z + 8, 135, 18, "jugcraft_thanksgiving_d_toys_three_quarter");
			shoot(context, singleplayer, x + TOYS + 2, y + 1, z + 5, 180, 5, "jugcraft_thanksgiving_d_backdrop_bulbs_on");
			shoot(context, singleplayer, x + TOYS + 5, y + 1, z + 5, 180, 20, "jugcraft_thanksgiving_d_toys_close");
			shoot(context, singleplayer, x + TOYS + 8, y + 2, z - 5, 0, 20, "jugcraft_thanksgiving_d_toys_back");

			// The feast, built with the server's ticks frozen so the tops hold their frames: the dishes, then the toy states.
			server.runOnServer(minecraft -> minecraft.tickRateManager().setFrozen(true));
			try {
				server.runOnServer(minecraft -> feast(minecraft.overworld(), origin));
				context.waitTicks(10);
				singleplayer.getConnection().waitForChunksRender();
				shoot(context, singleplayer, x + FEAST + 5, y + 2, z + 5, 180, 22, "jugcraft_thanksgiving_e_feast_front");
				shoot(context, singleplayer, x + FEAST + 5, y + 1, z + 7, 180, 10, "jugcraft_thanksgiving_e_feast_normal_distance");
				shoot(context, singleplayer, x + FEAST + 2, y + 1, z + 1, 180, 32, "jugcraft_thanksgiving_e_dishes_close_first_three");
				shoot(context, singleplayer, x + FEAST + 8, y + 1, z + 1, 180, 32, "jugcraft_thanksgiving_e_dishes_close_last_three");
				shoot(context, singleplayer, x + FEAST + 13, y + 3, z + 4, 135, 25, "jugcraft_thanksgiving_e_feast_three_quarter");
				shoot(context, singleplayer, x + FEAST + 2, y + 1, z + 5, 180, 25, "jugcraft_thanksgiving_e_top_rest_spin_wobble");
				shoot(context, singleplayer, x + FEAST + 8, y + 1, z + 5, 180, 25, "jugcraft_thanksgiving_e_corn_toss_states");
			} finally {
				server.runOnServer(minecraft -> minecraft.tickRateManager().setFrozen(false));
			}

			// Normal-distance overviews cannot resolve the small crafts' handles,
			// rims, bindings and toys. Keep dedicated close-ups for visual review.
			shoot(context, singleplayer, x + HOMESTEAD + 8, y, z + 3, 180, -2, "jugcraft_thanksgiving_a_wall_crafts_close");
			shoot(context, singleplayer, x + WOODLAND + 1, y, z + 3, 180, 15, "jugcraft_thanksgiving_b_baskets_close");
			shoot(context, singleplayer, x + WOODLAND + 5, y, z + 3, 180, 15, "jugcraft_thanksgiving_b_box_and_bowls_close");
			shoot(context, singleplayer, x + WOODLAND + 9, y, z + 3, 180, 15, "jugcraft_thanksgiving_b_pottery_and_corn_close");
			shoot(context, singleplayer, x + WOODLAND + 12, y, z + 2, 180, 20, "jugcraft_thanksgiving_b_mortar_close");
			shoot(context, singleplayer, x + TOWN + 9, y + 1, z + 3, 180, 10, "jugcraft_thanksgiving_c_crock_rack_close");
			shoot(context, singleplayer, x + TOWN + 17, y, z + 3, 180, 0, "jugcraft_thanksgiving_c_planter_close");
			shoot(context, singleplayer, x + TOYS + 1, y, z + 4, 180, 20, "jugcraft_thanksgiving_d_dolls_and_turkey_close");
			shoot(context, singleplayer, x + TOYS + 5, y, z + 4, 180, 20, "jugcraft_thanksgiving_d_top_and_ship_close");
			shoot(context, singleplayer, x + TOYS + 10, y, z + 4, 180, 20, "jugcraft_thanksgiving_d_fair_games_close");

			// Midnight: the lit pieces beside their unlit twins, and the backdrop's bulbs on, then off.
			server.runCommand("time set midnight");
			shoot(context, singleplayer, x + HOMESTEAD + 10, y + 1, z + 3, 180, 20, "jugcraft_thanksgiving_a_chamberstick_lit_and_unlit_night");
			shoot(context, singleplayer, x + TOWN + 14, y + 1, z + 3, 180, 20, "jugcraft_thanksgiving_c_chafing_dish_lit_and_unlit_night");
			shoot(context, singleplayer, x + TOYS + 2, y + 1, z + 5, 180, 5, "jugcraft_thanksgiving_d_backdrop_bulbs_on_night");
			server.runOnServer(minecraft -> lit(minecraft.overworld(), origin.offset(TOYS, 0, -1), "harvest_photo_backdrop", false));
			context.waitTicks(10);
			shoot(context, singleplayer, x + TOYS + 2, y + 1, z + 5, 180, 5, "jugcraft_thanksgiving_d_backdrop_bulbs_off_night");

			// Noon again: the backdrop with its bulbs off, beside the noon shot with them on.
			server.runCommand("time set noon");
			shoot(context, singleplayer, x + TOYS + 2, y + 1, z + 5, 180, 5, "jugcraft_thanksgiving_d_backdrop_bulbs_off");
		} finally {
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden() != hudHidden) client.gui.hud.toggle();
			});
		}
	}

	/** Stands the camera at (x, y, z) looking along yaw and pitch, and waits for the world to draw. */
	private static void place(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
		context.waitTicks(20);
		singleplayer.getConnection().waitForChunksRender();
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z, int yaw, int pitch,
			String name) {
		place(context, singleplayer, x, y, z, yaw, pitch);
		context.waitTicks(20);
		context.takeScreenshot(name);
	}

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_ALL);
	}

	/** A one-block piece facing south (towards the cameras). */
	private static BlockState south(String id) {
		BlockState state = JugcraftAgriculture.block(id).defaultBlockState();
		return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH) : state;
	}

	/** Every block of a multi-block piece facing south, its first block at {@code master}. */
	private static void prop(ServerLevel level, BlockPos master, String id) {
		MultiDecorationBlock block = (MultiDecorationBlock) JugcraftAgriculture.block(id);
		BlockState state = block.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.SOUTH);
		for (int part = 0; part < block.cells().length; part++) {
			set(level, block.partPos(master, Direction.SOUTH, part), state.setValue(block.partProperty(), part));
		}
	}

	/** Lights or puts out every block of a multi-block piece facing south, its first block at {@code master}. */
	private static void lit(ServerLevel level, BlockPos master, String id, boolean lit) {
		MultiDecorationBlock block = (MultiDecorationBlock) JugcraftAgriculture.block(id);
		for (int part = 0; part < block.cells().length; part++) {
			BlockPos pos = block.partPos(master, Direction.SOUTH, part);
			set(level, pos, level.getBlockState(pos).setValue(MultiDecorationBlock.LIT, lit));
		}
	}

	private static void planter(ServerLevel level, BlockPos at, Item... plants) {
		set(level, at, JugcraftAgriculture.block("window_box").defaultBlockState().setValue(PlanterBlock.FACING, Direction.SOUTH));
		if (level.getBlockEntity(at) instanceof PlanterBlockEntity planted) {
			for (Item plant : plants) {
				planted.plant(new ItemStack(plant));
			}
		}
	}

	private static void wall(ServerLevel level, BlockPos origin, int from, int to, int height, int z, Block block) {
		for (int across = from; across <= to; across++) {
			for (int up = 0; up < height; up++) {
				set(level, origin.offset(across, up, z), block.defaultBlockState());
			}
		}
	}

	/**
	 * E. The feast (batch 2): the six side dishes on oak plank tables against the back, facing south; before them the
	 * Spinning Top at rest (upright on its tip), mid-spin and wobbling, and the Corn Toss Set before a round, mid-round
	 * (three bags of each colour tossed, two of each on the board) and with every bag on the board.
	 */
	private static void feast(ServerLevel level, BlockPos origin) {
		int e = FEAST;
		for (int i = 0; i < ThanksgivingDishes.DISHES.size(); i++) {
			set(level, origin.offset(e + 2 * i, 0, -1), Blocks.OAK_PLANKS.defaultBlockState());
			set(level, origin.offset(e + 2 * i, 1, -1), south(ThanksgivingDishes.DISHES.get(i).id()));
		}
		int[] spins = {0, 20, 4};
		for (int i = 0; i < spins.length; i++) {
			set(level, origin.offset(e + 2 * i, 0, 2), south("spinning_top").setValue(FestivalToySpinningTopBlock.SPIN, spins[i]));
		}
		int[][] tosses = {{0, 0, 0}, {6, 2, 2}, {8, 4, 4}};
		for (int i = 0; i < tosses.length; i++) {
			set(level, origin.offset(e + 6 + 2 * i, 0, 2), south("corn_toss_set").setValue(FestivalToyCornTossBlock.TOSSED, tosses[i][0])
					.setValue(FestivalToyCornTossBlock.RUST_BOARD, tosses[i][1]).setValue(FestivalToyCornTossBlock.CREAM_BOARD, tosses[i][2]));
		}
	}

	private static void build(ServerLevel level, BlockPos origin) {
		// A. The homestead: a plank wall at the back, the settle and the dresser against it, the peg rail and the sampler
		// on it; before it the pitcher and basin, the chamberstick lit and unlit, the quilt rail and the butter churn.
		int a = HOMESTEAD;
		wall(level, origin, a, a + 14, 3, -2, Blocks.OAK_PLANKS);
		prop(level, origin.offset(a + 1, 0, -1), "oak_settle");
		prop(level, origin.offset(a + 4, 0, -1), "carved_dish_dresser");
		set(level, origin.offset(a + 7, 1, -1), south("pilgrim_peg_rail"));
		set(level, origin.offset(a + 9, 1, -1), south("harvest_sampler"));
		set(level, origin.offset(a + 7, 0, 0), south("pewter_pitcher_and_basin"));
		set(level, origin.offset(a + 9, 0, 0), south("pewter_chamberstick").setValue(CandleCenterpieceBlock.LIT, true));
		set(level, origin.offset(a + 10, 0, 0), south("pewter_chamberstick").setValue(CandleCenterpieceBlock.LIT, false));
		set(level, origin.offset(a + 12, 0, 0), south("quilt_rail"));
		set(level, origin.offset(a + 13, 0, 0), south("butter_churn"));

		// B. The woodland crafts: a low log wall at the back, the seven floor crafts a block apart before it, and a three by
		// three Rush Floor Mat in front, every side joined to its neighbour.
		int b = WOODLAND;
		wall(level, origin, b - 1, b + 13, 1, -2, Blocks.SPRUCE_LOG);
		String[] crafts = {"lidded_twined_basket", "twined_pack_basket", "birch_bark_box", "carved_bowls_and_spoons", "incised_earthenware_pot",
				"corn_drying_frame", "wooden_corn_mortar"};
		for (int i = 0; i < crafts.length; i++) {
			set(level, origin.offset(b + 2 * i, 0, -1), south(crafts[i]));
		}
		for (int across = 0; across < 3; across++) {
			for (int deep = 0; deep < 3; deep++) {
				set(level, origin.offset(b + 4 + across, 0, 1 + deep), JugcraftAgriculture.block("rush_floor_mat").defaultBlockState()
						.setValue(WoodlandRushMatBlock.AXIS, Direction.Axis.Z).setValue(WoodlandRushMatBlock.NORTH, deep > 0)
						.setValue(WoodlandRushMatBlock.SOUTH, deep < 2).setValue(WoodlandRushMatBlock.WEST, across > 0)
						.setValue(WoodlandRushMatBlock.EAST, across < 2));
			}
		}

		// C. The town: the arch over a path through its opening; a plank beam on two log posts with the crock rack hung
		// under it, the carving bench and the linen stack below; the chafing dish lit and unlit; a window box on a plank
		// wall holding an orange mum and a Warty Gourd.
		int c = TOWN;
		for (int across = 1; across <= 3; across++) {
			for (int deep = -6; deep <= 6; deep++) {
				set(level, origin.offset(c + across, -1, deep), Blocks.DIRT_PATH.defaultBlockState());
			}
		}
		prop(level, origin.offset(c, 0, 0), "timber_harvest_arch");
		for (int up = 0; up < 3; up++) {
			set(level, origin.offset(c + 7, up, 0), Blocks.OAK_LOG.defaultBlockState());
			set(level, origin.offset(c + 11, up, 0), Blocks.OAK_LOG.defaultBlockState());
		}
		for (int across = 7; across <= 11; across++) {
			set(level, origin.offset(c + across, 3, 0), Blocks.OAK_PLANKS.defaultBlockState());
		}
		set(level, origin.offset(c + 9, 2, 0), south("hanging_crock_rack"));
		set(level, origin.offset(c + 8, 0, 0), south("pumpkin_carving_bench"));
		set(level, origin.offset(c + 10, 0, 0), south("folded_linen_stack"));
		set(level, origin.offset(c + 14, 0, 0), south("copper_chafing_dish").setValue(CandleCenterpieceBlock.LIT, true));
		set(level, origin.offset(c + 15, 0, 0), south("copper_chafing_dish").setValue(CandleCenterpieceBlock.LIT, false));
		wall(level, origin, c + 17, c + 18, 3, -1, Blocks.OAK_PLANKS);
		planter(level, origin.offset(c + 17, 1, 0), JugcraftAgriculture.item("orange_mum"), JugcraftAgriculture.item("warty_gourd"));

		// D. The toys: the photo backdrop against the back, its bulbs on; the seven toys in a row before it.
		int d = TOYS;
		prop(level, origin.offset(d, 0, -1), "harvest_photo_backdrop");
		String[] toys = {"corn_husk_dolls", "turkey_pull_toy", "spinning_top", "mayflower_model_ship", "corn_toss_set", "prize_pumpkin_display",
				"harvest_percussion_set"};
		for (int i = 0; i < toys.length; i++) {
			set(level, origin.offset(d + 2 * i, 0, 1), south(toys[i]));
		}
	}
}
