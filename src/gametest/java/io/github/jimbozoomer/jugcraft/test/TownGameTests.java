package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.AtmMenu;
import io.github.jimbozoomer.jugcraft.town.Jugs;
import io.github.jimbozoomer.jugcraft.town.ShopMenu;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownBuilder;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownDecor;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.TownsfolkCare;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The walled town on a test server: its data reads whole, the shops make sense, Jugs stay in bounds and move between
 * players only as allowed, and a test town (built far from every other test) is built block for block from its data,
 * brings out its townsfolk, keeps its blocks from players, explosions and fire, and changes its decor by theme.
 */
public class TownGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-town-tests");
	/** The test town's corner: far from every other test's area. */
	private static final int FAR = 48_000;

	@GameTest
	public void townDataReadsWhole(GameTestHelper helper) {
		TownData data = TownData.get();
		helper.assertTrue(data.unknownStates() == 0, "Every block state in the town's palette is a real one, " + data.unknownStates() + " are not");
		helper.assertTrue(data.size == 192 && data.sites.size() > 250 && data.spots.size() >= 30 && data.atms.size() == 4,
				"The town has its sites, places and ATMs: " + data.sites.size() + " sites, " + data.spots.size() + " places, "
						+ data.atms.size() + " ATMs");
		Set<String> kinds = new HashSet<>();
		for (TownData.Site site : data.sites) {
			if (!kinds.add(site.kind())) {
				continue;
			}
			for (String theme : data.themes) {
				if (site.kind().equals("centerpiece")) {
					long filled = 0;
					for (int[] b : site.blocks()) {
						if (!TownDecor.stateFor(data, site, theme, b[3]).isAir()) {
							filled++;
						}
					}
					helper.assertTrue(filled > 10, "The square's centrepiece shows something for " + theme + ": " + filled + " blocks");
				} else {
					BlockState state = TownDecor.stateFor(data, site, theme, site.blocks()[0][3]);
					helper.assertTrue(!state.isAir(), "A " + site.kind() + " site shows something for " + theme);
				}
			}
		}
		LOGGER.info("Town data: {} palette entries, {} sites of kinds {}, {} places, themes {}", data.paletteText.size(), data.sites.size(),
				kinds, data.spots.size(), data.themes);
		helper.succeed();
	}

	@GameTest
	public void shopsMakeSense(GameTestHelper helper) {
		TownShops shops = TownShops.get();
		Set<String> sold = new HashSet<>();
		Set<String> bought = new HashSet<>();
		for (TownShops.Shop shop : shops.shops.values()) {
			for (TownShops.Offer offer : shop.sells()) {
				helper.assertTrue(offer.itemType() != Items.AIR && offer.price() > 0 && offer.count() > 0, shop.id() + " sells a real item: " + offer);
				sold.add(offer.item());
			}
			for (TownShops.Offer offer : shop.buys()) {
				helper.assertTrue(offer.itemType() != Items.AIR && offer.price() > 0 && offer.count() > 0, shop.id() + " buys a real item: " + offer);
				bought.add(offer.item());
			}
		}
		sold.retainAll(bought);
		helper.assertTrue(sold.isEmpty(), "No item is both sold and bought by the town: " + sold);
		helper.succeed();
	}

	@GameTest
	public void jugsStayInBounds(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		UUID a = UUID.randomUUID();
		UUID b = UUID.randomUUID();
		Jugs.add(server, a, 50);
		helper.assertTrue(Jugs.balance(server, a) == 50, "50 Jugs added");
		helper.assertTrue(!Jugs.take(server, a, 60) && Jugs.balance(server, a) == 50, "Can't take more than a player has");
		helper.assertTrue(Jugs.take(server, a, 20) && Jugs.balance(server, a) == 30, "Taking 20 leaves 30");
		helper.assertTrue(!Jugs.transfer(server, a, b, 40), "Can't send more than one has");
		helper.assertTrue(!Jugs.transfer(server, a, a, 10), "Can't send to oneself");
		helper.assertTrue(!Jugs.transfer(server, a, b, 0) && !Jugs.transfer(server, a, b, -5), "Can't send nothing or less");
		helper.assertTrue(Jugs.transfer(server, a, b, 30) && Jugs.balance(server, a) == 0 && Jugs.balance(server, b) == 30, "30 sent");
		long max = TownShops.get().maxBalance;
		Jugs.add(server, b, max);
		helper.assertTrue(Jugs.balance(server, b) == max, "A balance stops at the maximum");
		helper.succeed();
	}

	@GameTest
	public void shopTradesCheckEverything(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		MinecraftServer server = helper.getLevel().getServer();
		TownShops.Shop general = TownShops.get().shop("general");
		TownShops.Offer torches = general.sells().stream().filter(o -> o.item().equals("minecraft:torch")).findFirst().orElseThrow();
		TownShops.Offer wheat = general.buys().stream().filter(o -> o.item().equals("minecraft:wheat")).findFirst().orElseThrow();
		helper.assertTrue(!ShopMenu.buy(player, torches), "No Jugs, no torches");
		Jugs.add(server, player.getUUID(), torches.price() + 1);
		helper.assertTrue(ShopMenu.buy(player, torches), "Torches bought");
		helper.assertTrue(player.getInventory().countItem(Items.TORCH) == torches.count() && Jugs.balance(server, player.getUUID()) == 1,
				"The torches are in the inventory and the price is paid: " + player.getInventory().countItem(Items.TORCH) + " torches, "
						+ Jugs.balance(server, player.getUUID()) + " Jugs left");
		helper.assertTrue(!ShopMenu.sell(player, wheat), "No wheat, nothing to sell");
		player.getInventory().add(new ItemStack(Items.WHEAT, wheat.count() + 3));
		helper.assertTrue(ShopMenu.sell(player, wheat), "Wheat sold");
		helper.assertTrue(player.getInventory().countItem(Items.WHEAT) == 3 && Jugs.balance(server, player.getUUID()) == 1 + wheat.price(),
				"The wheat is taken and paid for");
		// A seasonal offer is only for sale in its themes.
		TownShops.Offer cobweb = TownShops.get().shop("seasonal").sells().stream().filter(o -> o.item().equals("minecraft:cobweb"))
				.findFirst().orElseThrow();
		Jugs.add(server, player.getUUID(), 100);
		TownDecor.force("spring");
		boolean outOfSeason = ShopMenu.buy(player, cobweb);
		TownDecor.force("halloween");
		boolean inSeason = ShopMenu.buy(player, cobweb);
		TownDecor.force(null);
		helper.assertTrue(!outOfSeason && inSeason, "Cobwebs sell at Halloween only");
		helper.succeed();
	}

	@GameTest
	public void atmSendsJugsBetweenPlayers(GameTestHelper helper) {
		ServerPlayer from = helper.makeMockServerPlayerInLevel();
		ServerPlayer to = helper.makeMockServerPlayerInLevel();
		MinecraftServer server = helper.getLevel().getServer();
		Jugs.add(server, from.getUUID(), 100);
		helper.assertTrue(AtmMenu.sendForTest(from, to, 40), "40 Jugs sent");
		helper.assertTrue(Jugs.balance(server, from.getUUID()) == 60 && Jugs.balance(server, to.getUUID()) == 40, "Balances after sending");
		helper.assertTrue(!AtmMenu.sendForTest(from, to, 61), "Can't send more than one holds");
		helper.assertTrue(!AtmMenu.sendForTest(from, from, 10), "Can't send to oneself");
		helper.assertTrue(!AtmMenu.sendForTest(from, null, 10), "Can't send to nobody");
		helper.succeed();
	}

	/**
	 * A test town far away: its square's chunks are built straight from the data and checked block by block, its
	 * townsfolk come out, a survival player can neither break nor place there, an explosion and a fire leave it as it
	 * was, townsfolk shrug off a player's blows, and a lamp changes with the theme.
	 */
	@GameTest(maxTicks = 4000)
	public void testTownIsBuiltAndKept(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		TownState state = TownState.get(level);
		helper.assertTrue(state.origin() == null, "The test world has no town of its own (it is flat)");
		BlockPos origin = new BlockPos(FAR, level.getMinY() + 3, FAR);
		state.place(origin);
		TownData data = TownData.get();
		List<ChunkPos> chunks = new ArrayList<>();
		for (int cx = (origin.getX() + 72) >> 4; cx <= (origin.getX() + 120) >> 4; cx++) {
			for (int cz = (origin.getZ() + 86) >> 4; cz <= (origin.getZ() + 112) >> 4; cz++) {
				chunks.add(new ChunkPos(cx, cz));
			}
		}
		for (ChunkPos pos : chunks) {
			level.setChunkForced(pos.x(), pos.z(), true);
			LevelChunk chunk = level.getChunk(pos.x(), pos.z());
			TownBuilder.build(level, chunk, origin);
			state.markBuilt(pos.pack());
			TownDecor.chunkBuilt(level, pos);
			TownsfolkCare.chunkBuilt(level, pos);
		}
		// Block for block: every block the data sets in these chunks is there (decor sites aside).
		Set<Long> sites = new HashSet<>();
		for (TownData.Site site : data.sites) {
			for (int[] b : site.blocks()) {
				sites.add(BlockPos.asLong(b[0], b[1], b[2]));
			}
		}
		int checked = 0;
		int wrong = 0;
		String firstWrong = null;
		for (ChunkPos pos : chunks) {
			for (int wx = pos.x() << 4; wx < (pos.x() << 4) + 16; wx++) {
				for (int wz = pos.z() << 4; wz < (pos.z() << 4) + 16; wz++) {
					int x = wx - origin.getX();
					int z = wz - origin.getZ();
					for (int y = data.yMin; y < data.yMin + data.height; y++) {
						int index = data.index(x, y, z);
						if (index == TownData.KEEP || sites.contains(BlockPos.asLong(x, y, z))) {
							continue;
						}
						checked++;
						BlockState found = level.getBlockState(origin.offset(x, y, z));
						if (found != data.state(index)) {
							wrong++;
							if (firstWrong == null) {
								firstWrong = x + " " + y + " " + z + ": " + found + " instead of " + data.state(index);
							}
						}
					}
				}
			}
		}
		LOGGER.info("Test town: {} chunks round the square built, {} blocks checked, {} differ{}", chunks.size(), checked, wrong,
				firstWrong == null ? "" : " (first: " + firstWrong + ")");
		helper.assertTrue(checked > 10_000 && wrong == 0, "The square is built as designed: " + wrong + " of " + checked + " blocks differ"
				+ (firstWrong == null ? "" : ", first " + firstWrong));
		// The town has brought out one townsperson for each place in these chunks (they are looked up once the chunks'
		// entities are loaded, below: a chunk forced this tick shows its entities only from a later tick).
		List<TownData.Spot> places = data.spots.stream().filter(s -> chunks.contains(ChunkPos.containing(origin.offset(s.pos())))).toList();
		long recorded = places.stream().filter(s -> state.townsperson(s.index()) != null).count();
		helper.assertTrue(!places.isEmpty() && recorded == places.size(),
				"A townsperson brought out for each place by the square: " + recorded + " of " + places.size());
		// A survival player can't break or place, or pour water.
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos paving = origin.offset(96, 0, 100);
		BlockState pavingState = level.getBlockState(paving);
		player.snapTo(paving.getX() + 0.5, paving.getY() + 1, paving.getZ() + 0.5, 0.0F, 0.0F);
		helper.assertTrue(Town.isProtected(level, paving) && Town.isInside(level, paving), "The square is protected");
		boolean broke = player.gameMode.destroyBlock(paving);
		helper.assertTrue(!broke && level.getBlockState(paving) == pavingState, "A survival player can't break the town's paving");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COBBLESTONE, 4));
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(paving).add(0, 0.5, 0), Direction.UP, paving, false));
		helper.assertTrue(level.getBlockState(paving.above()).isAir(), "A survival player can't place a block on the square");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
		player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
		helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "A survival player can't empty a bucket in the town");
		// An explosion in the square leaves it as it was.
		BlockPos fountain = origin.offset(88, 1, 104);
		BlockState fountainState = level.getBlockState(fountain);
		level.explode(null, fountain.getX() + 3.5, fountain.getY() + 1.0, fountain.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
		helper.assertTrue(level.getBlockState(fountain) == fountainState && level.getBlockState(paving) == pavingState,
				"An explosion leaves the town's blocks");
		// A lamp shows a soul lantern at Halloween and a lantern in summer.
		TownData.Site lamp = data.sites.stream().filter(s -> s.kind().equals("lamp")
				&& chunks.contains(ChunkPos.containing(origin.offset(s.anchor())))).findFirst().orElseThrow();
		BlockPos lampPos = origin.offset(lamp.anchor());
		TownDecor.force("halloween");
		state.setTheme("halloween", level.getGameTime());
		TownDecor.apply(level, lamp.index());
		boolean soul = level.getBlockState(lampPos).is(Blocks.SOUL_LANTERN);
		TownDecor.force("summer");
		state.setTheme("summer", level.getGameTime());
		TownDecor.apply(level, lamp.index());
		boolean plain = level.getBlockState(lampPos).is(Blocks.LANTERN);
		TownDecor.force(null);
		helper.assertTrue(soul && plain, "The lamp changes with the theme (soul lantern " + soul + ", lantern " + plain + ")");
		// Fire on the square goes out before it can burn anything.
		BlockPos fire = paving.offset(2, 1, 0);
		level.setBlock(fire, Blocks.FIRE.defaultBlockState(), 3);
		helper.runAfterDelay(80, () -> {
			boolean out = !level.getBlockState(fire).is(Blocks.FIRE);
			// The townsfolk are in the world, each the one the town recorded for its place. A chunk ticks (and shows) its
			// entities only once the chunks round it are generated, which happens off the server thread while the test
			// server races through ticks, so wait for that rather than for a number of ticks.
			whenEntitiesTick(helper, level, chunks, 0, () -> {
				List<Townsfolk> people = recordedTownsfolk(level, state, places);
				if (people.size() != places.size()) {
					logTownsfolk(level, state, origin, places);
				}
				LOGGER.info("Test town: {} townsfolk by the square for {} places: {}", people.size(), places.size(),
						people.stream().map(p -> p.getName().getString() + " (" + p.role() + (p.shop().isEmpty() ? "" : ", " + p.shop()) + ")").toList());
				// Townsfolk shrug off a player's blows.
				boolean unhurt = false;
				if (!people.isEmpty()) {
					Townsfolk someone = people.get(0);
					boolean hurt = someone.hurtServer(level, level.damageSources().playerAttack(player), 100.0F);
					unhurt = !hurt && someone.getHealth() == someone.getMaxHealth() && someone.isAlive();
				}
				state.forget();
				for (ChunkPos pos : chunks) {
					level.setChunkForced(pos.x(), pos.z(), false);
				}
				helper.assertTrue(people.size() == places.size(), "One townsperson in the world for each place by the square: "
						+ people.size() + " of " + places.size());
				helper.assertTrue(unhurt, "A townsperson can't be hurt by a player");
				helper.assertTrue(out, "Fire in the town goes out");
				helper.succeed();
			});
		});
	}

	/**
	 * Runs {@code then} once every one of {@code chunks} ticks its entities (checked every 20 ticks), or, failing that,
	 * after waiting 3,600 ticks (the test allows 4,000).
	 */
	private static void whenEntitiesTick(GameTestHelper helper, ServerLevel level, List<ChunkPos> chunks, int waited, Runnable then) {
		boolean ticking = chunks.stream().allMatch(c -> level.isPositionEntityTicking(new BlockPos(c.x() << 4, level.getMinY() + 4, c.z() << 4)));
		if (ticking || waited >= 3600) {
			LOGGER.info("Test town: the square's chunks tick their entities after waiting {} ticks: {}", waited, ticking);
			then.run();
		} else {
			helper.runAfterDelay(20, () -> whenEntitiesTick(helper, level, chunks, waited + 20, then));
		}
	}

	/** The townsfolk the town recorded for these places that are in the world now. */
	private static List<Townsfolk> recordedTownsfolk(ServerLevel level, TownState state, List<TownData.Spot> places) {
		return places.stream().map(s -> state.townsperson(s.index())).filter(id -> id != null).map(level::getEntity)
				.filter(e -> e instanceof Townsfolk).map(e -> (Townsfolk) e).toList();
	}

	/** Logs each place (its chunk, whether that ticks entities, its recorded townsperson) and every townsperson in the level. */
	private static void logTownsfolk(ServerLevel level, TownState state, BlockPos origin, List<TownData.Spot> places) {
		for (TownData.Spot s : places) {
			BlockPos at = origin.offset(s.pos());
			UUID id = state.townsperson(s.index());
			LOGGER.info("Test town: place {} {} ({}) at {} chunk {} entity-ticking {}: recorded {} found {}", s.index(), s.name(),
					s.role(), s.pos().toShortString(), ChunkPos.containing(at), level.isPositionEntityTicking(at), id,
					id != null && level.getEntity(id) != null);
		}
		List<String> all = level.getEntities(EntityTypeTest.forClass(Townsfolk.class), t -> true).stream()
				.map(t -> t.getName().getString() + " spot " + t.spot() + " at " + t.blockPosition().subtract(origin).toShortString()
						+ (t.isAlive() ? "" : " (dead: " + t.getRemovalReason() + ")"))
				.toList();
		LOGGER.info("Test town: {} townsfolk in the level: {}", all.size(), all);
	}
}
