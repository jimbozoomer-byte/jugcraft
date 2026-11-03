package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.collectionMillis;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.collections;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.count;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.half;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.kib;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.mib;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.ms;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.retained;
import static io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.slope;

import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.garden.Canopies;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AmdusiasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BathinEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BifronsEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.CanopyShelter;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ForasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenReach;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GraftKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HabitatReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.MaraxEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactAccords;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RootRunning;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasRegard;
import io.github.jimbozoomer.jugcraft.concordance.spirits.VerdanceCarrying;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.Measure;
import io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.Stats;
import io.github.jimbozoomer.jugcraft.test.ConcordanceWorkshopLoadGameTests.Traffic;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SystemReport;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-10 (GC-S17, GC-P141): the garden's load card, on the pact hall's card's model
 * (ConcordancePactHallLoadGameTests), sharing Chunk C's workshop card's Measure, Traffic, Stats and static helpers. The same
 * world is measured twice: first with its two players standing where they will stand, then with the garden built round
 * them. The garden: two survival holders, each keeping all six garden pacts and Stolas's (fourteen pacts at fourteen Pact
 * Perches they placed, sealed under the shipped offers as the worker tests seal them, the compact left out, one offering in
 * each escrow) over a garden of their own: 24 awake beds each (48), 20 with organism crops of the four kinds (some ripe) and 4
 * fallow and rich, strain and nutrients seeded so every service has work; a Verdant Heart, a Gleaner and a Ley Pylon each
 * (holder 0's Gleaner beyond the Heart's reach of 4 and their pylon beside it, its own road; holder 1's Gleaner within 4 and
 * their pylon apart); a Mulch Maw (holder 0); eight disturbing blocks; one oak tree beside holder 0's Amdusias; a chest of
 * Dewmoss touching each Foras perch. Holder 0's Bathin is answered "take from it" and both Foras "graft every host"; Bifrons
 * and Amdusias are left unanswered. While measured each holder asks Marax for a reading every READ_TICKS through the kept
 * page's Recall, and every SCAFFOLD_TICKS the test puts back what the services used (two beds re-strained, the Hearts and
 * fallow beds refilled, the Gleaners and pylons emptied, the chests topped up, grafts cleared): that and the readings are the
 * test's own work, timed apart. GC-P141's rules are sampled from the services' own memories every THINK_TICKS while
 * measured, and the Bound Wills, escrows and the ground every AUDIT ticks. After the spans, in one unmeasured tick, it times
 * each service's think, a GardenReach refresh, a canopy lookup, a reading and a graft's transaction call by call, and
 * reports all of it as "Garden load card | ...".
 * <p>
 * Time of day (unit 7, decided by the session): Buer serves by day and Bifrons by night, so no held hour serves both, and
 * each pact's quota is counted by game day (Pacts.done reads game time, which no test can set). Time therefore runs in both
 * spans; each span opens at world time MEASURED_FROM (dusk falls inside it), and the garden's warm-up waits for a game day's
 * turn, so the garden span opens with every quota fresh. It measures only when Fabric's filter selects it and nothing else
 * (docs/TESTING.md); in every other run it passes at once. In CI it runs after the pact hall's card in the same opted-in job.
 */
public class ConcordanceGardenLoadGameTests {
	/** Fabric's id for loadCard: the test mod's id, then the class and method in snake case (TestAnnotationLocator). */
	public static final String ID = "jugcraft-test:concordance_garden_load_game_tests_load_card";
	/** Fabric API's selection property (GameTestSystemProperties.FILTER, an impl class not imported here). */
	static final String FILTER = "fabric-api.gametest.filter";
	/** The workshop card's: its Measure is sized by its own two, which these equal (check_garden_load_card). */
	static final int WINDOW = 100;
	static final int MEASURED = 6_000;
	static final int BASE_WARMUP = 2_000;
	static final int GARDEN_WARMUP = 1_500;
	/** A warm-up also waits until the loaded chunks have held steady this long. */
	static final int SETTLE = 400;
	static final int WARMUP_CAP = 10_000;
	/** The garden's warm-up also waits for a game day's turn (Pacts.DAY, asserted), so each quota is fresh when measuring begins. */
	static final int DAY_WAIT = 24_000;
	static final int AUDIT = 1_000;
	/** Room for two warm-ups at their cap, the wait for a day's turn and two measured spans. */
	static final int MAX_TICKS = 60_000;
	/** The yard: a stone floor from OFF to OFF + SIZE in x and z, barriers round its edge. */
	static final int SIZE = 47;
	static final int OFF = 10;
	/** GC-P141's garden: two holders, each keeping seven pacts (the six garden spirits and Stolas). */
	static final int HOLDERS = 2;
	static final int PACTS = 7;
	/** In all, both holders' together. */
	static final int BEDS = 48;
	static final int HEARTS = 2;
	static final int GLEANERS = 2;
	static final int PYLONS = 2;
	static final int MAWS = 1;
	static final int DISTURBING = 8;
	static final int TREES = 1;
	/** Offerings each pact's escrow starts with: one each (its definition's count of items). */
	static final int ESCROW = 1;
	/** Each holder asks Marax for a reading this often while measured: two a span, his day's quota (asserted against it). */
	static final int READ_TICKS = 3_000;
	/** The test puts back what the services used this often (scaffolding: timed apart, recorded in the report). */
	static final int SCAFFOLD_TICKS = 200;
	/** World time each span opens at: Buer's daylight runs to 12,000 and Bifrons's night from 13,000, both inside a span. */
	static final long MEASURED_FROM = 10_000L;
	/** Dewmoss each Foras chest is kept at; Verdance a Heart is refilled to; nutrients a fallow bed is refilled to. */
	static final int DONORS = 64;
	static final long HEART_FILL = 16L;
	static final int RICH = 30;
	/** Calls timed one by one after the spans: thinks, refreshes and lookups CALLS each, readings and transactions READS each, after as many untimed. */
	static final int CALLS = 1_000;
	static final int READS = 200;
	/** GC-P141, in C-P19's form, proposed: reported here, not asserted. */
	static final double BUDGET_MEDIAN_MS = 1.0;
	static final double BUDGET_P95_MS = 2.5;
	static final double BUDGET_RETAINED_MIB = 4.0;
	/** What is asserted: five times the budget (GC-P141; the pact hall's card asserts C's 64 MiB for memory instead). */
	static final double LIMIT_MEDIAN_MS = 5.0;
	static final double LIMIT_P95_MS = 12.5;
	static final double LIMIT_RETAINED_MIB = 20.0;
	/** The pattern each holder has observed (Stolas's missing step). */
	static final String PATTERN = "jugcraft:full_moon";
	/** Bathin's pylon branch (understood, as a holder who built one would). */
	static final String CIRCLE_LORE = "jugcraft:circle_lore";
	/** The seven counterparts in perch order along each holder's row. */
	static final List<String> COUNTERPARTS = List.of(StolasEntity.COUNTERPART, BuerEntity.COUNTERPART, BathinEntity.COUNTERPART,
			MaraxEntity.COUNTERPART, ForasEntity.COUNTERPART, BifronsEntity.COUNTERPART, AmdusiasEntity.COUNTERPART);
	static final int STOLAS = 0;
	static final int BUER = 1;
	static final int BATHIN = 2;
	static final int MARAX = 3;
	static final int FORAS = 4;
	static final int BIFRONS = 5;
	static final int AMDUSIAS = 6;
	/** The bed indices left fallow (Bifrons's sources), one at each corner of a holder's beds. */
	static final Set<Integer> FALLOW = Set.of(0, 5, 18, 23);

	/** A yard position: x and z from the yard's corner, y as given. */
	static BlockPos p(int x, int y, int z) {
		return new BlockPos(OFF + x, y, OFF + z);
	}

	/** Holder {@code h}'s half of the yard: x from its own corner (holder 0 west, holder 1 east, 24 apart), y and z as given. */
	static BlockPos at(int h, int x, int y, int z) {
		return p(3 + 24 * h + x, y, z);
	}

	/** Holder {@code h}'s perch {@code k} (COUNTERPARTS' order), in a row two blocks apart, south of the beds. */
	static BlockPos perchAt(int h, int k) {
		return at(h, 1 + 2 * k, 2, 17);
	}

	static BlockPos standAt(int h) {
		return at(h, 7, 2, 21);
	}

	/** Holder {@code h}'s bed {@code i} (0 to 23): six columns, rows at z 9, 10, 12 and 13, a water row between at z 11. */
	static BlockPos bedAt(int h, int i) {
		int[] rows = {9, 10, 12, 13};
		return at(h, 4 + i % 6, 1, rows[i / 6]);
	}

	/** The Heart stands on stone (no bed: it makes no Verdance of its own); the pylon beside it for holder 0, two apart for holder 1. */
	static BlockPos heartAt(int h) {
		return at(h, 0, 2, 8);
	}

	static BlockPos pylonAt(int h) {
		return h == 0 ? at(h, 0, 2, 7) : at(h, 2, 2, 7);
	}

	/** Holder 0's Gleaner six from the Heart (beyond its own reach of 4: Bathin carries); holder 1's three (it draws itself). Both beyond a bed's reach. */
	static BlockPos gleanerAt(int h) {
		return h == 0 ? at(h, 0, 2, 14) : at(h, 0, 2, 11);
	}

	static BlockPos chestAt(int h) {
		return perchAt(h, FORAS).south();
	}

	static BlockPos mawAt() {
		return at(0, 11, 2, 11);
	}

	/** Holder 0's oak, two south of their Amdusias's perch (within CanopyShelter.TREE_REACH). */
	static BlockPos treeAt() {
		return perchAt(0, AMDUSIAS).south(2);
	}

	static boolean selected() {
		return ID.equals(System.getProperty(FILTER));
	}

	@GameTest(maxTicks = MAX_TICKS, skyAccess = true)
	public void loadCard(GameTestHelper helper) {
		if (!selected()) {
			helper.succeed();
			return;
		}
		new Card(helper).begin();
	}

	private enum Phase {
		BASE_WARMUP, BASE, GARDEN_WARMUP, GARDEN
	}

	/** The test's own actions, timed apart while measured. */
	private enum Action {
		READ, SCAFFOLD, SAMPLE
	}

	/** The run: one driver callback a tick (runAfterDelay), baseline first, then the garden: the pact hall card's rhythm, for this yard and its two players. */
	private static final class Card {
		final GameTestHelper helper;
		final ServerLevel level;
		final MinecraftServer server;
		/** The test's own nanoseconds in each tick of the ring, by tick % WINDOW, taken out of the server's figure. */
		final long[] own = new long[WINDOW];
		final List<Signs.Shown> shown = new ArrayList<>();
		final List<String> runner = new ArrayList<>();
		final List<ChunkPos> forced = new ArrayList<>();
		/** The two holders (holder 0 is the observed player); each one's channel at the same index. */
		final List<ServerPlayer> players = new ArrayList<>();
		final List<EmbeddedChannel> channels = new ArrayList<>();
		/** Messages holder 1 was sent while measured: in the baseline, in the garden. */
		final long[] others = new long[2];
		int yardChunks;
		int forcedCount;
		StreamCodec<ByteBuf, Packet<? super ClientGamePacketListener>> codec;
		AutoCloseable signs;
		Phase phase = Phase.BASE_WARMUP;
		int phaseTicks;
		int expected = -1;
		int chunks = -1;
		int steadySince;
		Measure base;
		Measure garden;
		Measure current;
		Yard work;
		boolean advanceTime;
		boolean advanceWeather;
		boolean spawnMobs;
		boolean spawnTraders;

		Card(GameTestHelper helper) {
			this.helper = helper;
			this.level = helper.getLevel();
			this.server = level.getServer();
		}

		void begin() {
			helper.assertTrue(server.getPlayerList().getPlayerCount() == 0, "The load card runs alone: no other test's player is here");
			helper.assertTrue(Workers.enabled() && Garden.enabled(), "The Concordance and the garden are on");
			helper.assertTrue(COUNTERPARTS.stream().allMatch(counterpart -> PactPerchBlockEntity.definition(counterpart) != null),
					"Each of the seven offers is loaded: " + COUNTERPARTS);
			helper.assertTrue(DAY_WAIT == Pacts.DAY, "DAY_WAIT is a game day (Pacts.DAY): " + Pacts.DAY);
			describeRunner();
			hold();
			force();
			ground();
			join();
			signs = Signs.observe(sign -> {
				if (current != null && sign.level() == level) {
					shown.add(sign);
				}
			});
			helper.runAfterDelay(1, this::tick);
		}

		void describeRunner() {
			String report = new SystemReport().toLineSeparatedString();
			for (String key : List.of("Minecraft Version", "Operating System", "Java Version", "Java VM Version", "Processor Vendor",
					"Processor Name", "Frequency (GHz)", "Number of physical packages", "Number of physical CPUs",
					"Number of logical CPUs", "Memory", "JVM Flags")) {
				report.lines().filter(line -> line.startsWith(key + ": ")).findFirst().ifPresent(runner::add);
			}
			runner.add("max heap " + mib(Runtime.getRuntime().maxMemory()) + " MiB; collectors " + ManagementFactory.getGarbageCollectorMXBeans()
					.stream().map(GarbageCollectorMXBean::getName).collect(Collectors.joining(", ")));
			runner.add("JVM arguments: " + String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
		}

		/** Time runs (day and night services both serve: see the class comment), weather held, no mobs or wandering traders. Given back at the end. */
		void hold() {
			GameRules rules = level.getGameRules();
			advanceTime = rules.get(GameRules.ADVANCE_TIME);
			advanceWeather = rules.get(GameRules.ADVANCE_WEATHER);
			spawnMobs = rules.get(GameRules.SPAWN_MOBS);
			spawnTraders = rules.get(GameRules.SPAWN_WANDERING_TRADERS);
			rules.set(GameRules.ADVANCE_TIME, true, server);
			rules.set(GameRules.ADVANCE_WEATHER, false, server);
			rules.set(GameRules.SPAWN_MOBS, false, server);
			rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
		}

		void release() {
			GameRules rules = level.getGameRules();
			rules.set(GameRules.ADVANCE_TIME, advanceTime, server);
			rules.set(GameRules.ADVANCE_WEATHER, advanceWeather, server);
			rules.set(GameRules.SPAWN_MOBS, spawnMobs, server);
			rules.set(GameRules.SPAWN_WANDERING_TRADERS, spawnTraders, server);
		}

		/** Every chunk under the yard ticks (the game test server's simulation distance is 0). */
		void force() {
			BlockPos low = helper.absolutePos(p(0, 0, 0));
			BlockPos high = helper.absolutePos(p(SIZE, 0, SIZE));
			for (int cx = Math.min(low.getX(), high.getX()) >> 4; cx <= Math.max(low.getX(), high.getX()) >> 4; cx++) {
				for (int cz = Math.min(low.getZ(), high.getZ()) >> 4; cz <= Math.max(low.getZ(), high.getZ()) >> 4; cz++) {
					ChunkPos chunk = new ChunkPos(cx, cz);
					yardChunks++;
					if (!level.getChunkSource().getForceLoadedChunks().contains(chunk.pack())) {
						level.setChunkForced(cx, cz, true);
						forced.add(chunk);
					}
				}
			}
			forcedCount = forced.size();
		}

		void unforce() {
			for (ChunkPos chunk : forced) {
				level.setChunkForced(chunk.x(), chunk.z(), false);
			}
			forced.clear();
		}

		void ground() {
			for (int x = 0; x <= SIZE; x++) {
				for (int z = 0; z <= SIZE; z++) {
					boolean edge = x == 0 || z == 0 || x == SIZE || z == SIZE;
					helper.setBlock(p(x, 1, z), edge ? Blocks.BARRIER : Blocks.STONE);
					if (edge) {
						helper.setBlock(p(x, 2, z), Blocks.BARRIER);
						helper.setBlock(p(x, 3, z), Blocks.BARRIER);
					}
				}
			}
		}

		/**
		 * The two holders, in both spans: joined as the pact hall's players are, in survival, each channel kept and drained every
		 * tick; each understands The Garden's Regard (so the Binding Arts and Verdant Husbandry), Circle Lore and Stolas's Regard,
		 * and has observed the full moon.
		 */
		void join() {
			for (int h = 0; h < HOLDERS; h++) {
				ServerPlayer player = player("garden-holder-" + h, standAt(h));
				ConcordanceWorkerGameTests.understand(player, GardenPacts.RESEARCH);
				ConcordanceWorkerGameTests.understand(player, CIRCLE_LORE);
				ConcordanceWorkerGameTests.understand(player, StolasRegard.RESEARCH);
				AstralClaims.of(server).observe(player.getUUID(), PATTERN, 0L);
				RateGate.forget(player.getUUID());
			}
			codec = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(level.registryAccess())).codec();
		}

		ServerPlayer player(String name, BlockPos at) {
			CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
			ServerPlayer player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
			Connection connection = new Connection(PacketFlow.SERVERBOUND);
			channels.add(new EmbeddedChannel(new ChannelHandler[] {connection}));
			server.getPlayerList().placeNewPlayer(connection, player, cookie);
			player.setGameMode(GameType.SURVIVAL);
			BlockPos to = helper.absolutePos(at);
			player.snapTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5, 0.0F, 0.0F);
			level.getChunkSource().move(player);
			helper.assertTrue(!player.isCreative() && !player.hasInfiniteMaterials(), "Each player is an ordinary survival player: " + name);
			players.add(player);
			return player;
		}

		void tick() {
			long began = System.nanoTime();
			int now = server.getTickCount();
			helper.assertTrue(expected < 0 || now == expected, "The driver ran every tick, so the windows line up: " + now + " after " + (expected - 1));
			expected = now + 1;
			for (ServerPlayer player : players) {
				player.connection.chunkSender.onChunkBatchReceivedByClient(64.0F);
			}
			drain();
			if (work != null) {
				work.drive(now);
			}
			phaseTicks++;
			if (!advance(now)) {
				helper.runAfterDelay(1, this::tick);
			}
			own[Math.floorMod(now, WINDOW)] = System.nanoTime() - began;
		}

		/** Every player's channel emptied; what holder 0 is sent is sized (C's Traffic), holder 1's messages only counted. */
		void drain() {
			Traffic traffic = current == null ? null : current.traffic;
			for (int i = 0; i < channels.size(); i++) {
				EmbeddedChannel channel = channels.get(i);
				Object message;
				while ((message = channel.readOutbound()) != null) {
					if (traffic == null || !(message instanceof Packet<?> packet)) {
						continue;
					}
					if (i == 0) {
						traffic.add(packet, codec);
					} else {
						others[current == base ? 0 : 1]++;
					}
				}
			}
			for (Signs.Shown sign : shown) {
				if (traffic != null && sign.at().distanceToSqr(players.get(0).position()) <= Presentation.FAR * Presentation.FAR) {
					traffic.sign(sign, codec);
				}
			}
			shown.clear();
		}

		boolean advance(int now) {
			switch (phase) {
				case BASE_WARMUP -> {
					if (settled(BASE_WARMUP, WARMUP_CAP)) {
						base = open("baseline", now);
						phase = Phase.BASE;
					}
				}
				case BASE -> {
					if (window(base, now) && base.windows == MEASURED / WINDOW) {
						close(base);
						work = new Yard(this);
						work.build();
						phase = Phase.GARDEN_WARMUP;
						phaseTicks = 0;
						chunks = -1;
					}
				}
				case GARDEN_WARMUP -> {
					// The garden span opens on a game day's turn (its first tick's game time a whole day), so every quota is fresh.
					if (settled(GARDEN_WARMUP, WARMUP_CAP + DAY_WAIT) && Math.floorMod((long) level.getGameTime() + 1, (long) DAY_WAIT) == 0) {
						garden = open("garden", now);
						work.mark(now + 1);
						phase = Phase.GARDEN;
					}
				}
				case GARDEN -> {
					if (window(garden, now) && garden.windows == MEASURED / WINDOW) {
						close(garden);
						finish();
						return true;
					}
				}
			}
			return false;
		}

		boolean settled(int least, int cap) {
			int loaded = level.getChunkSource().getLoadedChunksCount();
			if (loaded != chunks) {
				chunks = loaded;
				steadySince = phaseTicks;
			}
			helper.assertTrue(phaseTicks <= cap, "The world settled within " + cap + " ticks (" + loaded + " chunks, changing)");
			return phaseTicks >= least && phaseTicks - steadySince >= SETTLE;
		}

		/** Opens a span after this tick, which is not measured: a full collection first, for the retained heap; the clock set to MEASURED_FROM. */
		Measure open(String name, int now) {
			helper.setTime(MEASURED_FROM);
			Measure measure = new Measure(name);
			measure.retainedBefore = retained();
			measure.collections = -collections();
			measure.collectionMillis = -collectionMillis();
			measure.chunksBefore = level.getChunkSource().getLoadedChunksCount();
			measure.warmup = phaseTicks;
			measure.start = now + 1;
			current = measure;
			return measure;
		}

		/** At each window's end, the ring's 100 ticks: what the server measured, and that less the test's own work. */
		boolean window(Measure measure, int now) {
			int since = now - measure.start;
			if (since <= 0 || since % WINDOW != 0) {
				return false;
			}
			long[] ring = server.getTickTimesNanos();
			for (int tick = now - WINDOW; tick < now; tick++) {
				int slot = Math.floorMod(tick, WINDOW);
				int index = tick - measure.start;
				measure.raw[index] = ring[slot];
				measure.net[index] = Math.max(0L, ring[slot] - own[slot]);
				measure.own += own[slot];
			}
			measure.heap[measure.windows++] = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
			return true;
		}

		void close(Measure measure) {
			measure.collections += collections();
			measure.collectionMillis += collectionMillis();
			measure.chunksAfter = level.getChunkSource().getLoadedChunksCount();
			measure.retainedAfter = retained();
			current = null;
		}

		/** Its players leave the game (PlayerList.remove, as a logout). */
		void leave() {
			for (ServerPlayer player : players) {
				if (server.getPlayerList().getPlayer(player.getUUID()) == player) {
					server.getPlayerList().remove(player);
				}
			}
		}

		void finish() {
			try {
				signs.close();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			work.audit("the end");
			Yard.Calls calls = work.timeCalls();
			release();
			unforce();
			Stats b = Stats.of(base.net);
			Stats g = Stats.of(garden.net);
			double deltaMedian = g.median() - b.median();
			double deltaP95 = g.p95() - b.p95();
			boolean memory = base.retainedBefore >= 0 && base.retainedAfter >= 0 && garden.retainedBefore >= 0 && garden.retainedAfter >= 0;
			double retained = memory ? mib(garden.retainedAfter - garden.retainedBefore) - mib(base.retainedAfter - base.retainedBefore) : 0;
			report(b, g, calls, deltaMedian, deltaP95, memory, retained);
			leave();
			helper.assertTrue(work.problems.isEmpty(), "GC-P141's rules held: " + work.problems);
			helper.assertTrue(work.eased > 0 && work.moves > 0 && work.reads > 0 && work.grafts > 0 && work.runs > 0 && work.covered > 0,
					"Each garden service did its work while measured: Buer eased " + work.eased + ", Bathin moved " + work.moves + ", Marax read "
							+ work.reads + ", Foras grafted " + work.grafts + ", Bifrons ran " + work.runs + ", Amdusias covered " + work.covered + " beds");
			helper.assertTrue(deltaMedian <= LIMIT_MEDIAN_MS && deltaP95 <= LIMIT_P95_MS,
					String.format(Locale.ROOT, "Within the asserted limits: median %+.3f ms (%.1f), p95 %+.3f ms (%.1f)", deltaMedian, LIMIT_MEDIAN_MS, deltaP95, LIMIT_P95_MS));
			helper.assertTrue(!memory || retained <= LIMIT_RETAINED_MIB, "Retained heap within " + LIMIT_RETAINED_MIB + " MiB of the baseline's: " + retained);
			helper.succeed();
		}

		void report(Stats b, Stats g, Yard.Calls calls, double deltaMedian, double deltaP95, boolean memory, double retained) {
			Traffic tb = base.traffic;
			Traffic tg = garden.traffic;
			double seconds = MEASURED / 20.0;
			List<String> lines = new ArrayList<>();
			lines.add("===== Concordance garden cohort load card (GC-10, GC-P141) =====");
			lines.add("build: Jugcraft " + FabricLoader.getInstance().getModContainer("jugcraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?")
					+ "; Jade " + (FabricLoader.getInstance().isModLoaded("jade") ? "loaded" : "absent"));
			for (String line : runner) {
				lines.add("runner: " + line);
			}
			lines.add("world: game test server (flat world, ticks run back to back); time running in both spans, each opened at world time " + MEASURED_FROM
					+ " (Buer's daylight to 12000, Bifrons's night from 13000), the garden span on a game day's turn (each quota fresh); weather held, "
					+ "mobs and wandering traders off, random tick speed " + level.getGameRules().get(GameRules.RANDOM_TICK_SPEED) + "; player list view "
					+ server.getPlayerList().getViewDistance() + " and simulation " + server.getPlayerList().getSimulationDistance() + "; the yard's "
					+ yardChunks + " chunks force-loaded (" + forcedCount + " by the card); players " + players.size() + " in both spans (" + HOLDERS
					+ " holders: server-side test players, no client)");
			lines.add(String.format(Locale.ROOT, "spans: baseline from tick %d, garden from tick %d (game time %d); each %d ticks in %d windows of %d after warm-ups of %d "
					+ "and %d ticks (at least %d and %d, then until the loaded chunk count held %d ticks, the garden's also until a game day's turn); tick times "
					+ "net of the test's own work (readings, scaffolding and sampling, timed apart below)", base.start, garden.start, work.markedAt, MEASURED,
					MEASURED / WINDOW, WINDOW, base.warmup, garden.warmup, BASE_WARMUP, GARDEN_WARMUP, SETTLE));
			lines.add("tick ms, net of the test's own work | median p95 mean max over-50ms | 100-tick window means: median p95");
			for (Measure measure : List.of(base, garden)) {
				Stats s = measure == base ? b : g;
				lines.add(String.format(Locale.ROOT, "%s %.3f %.3f %.3f %.1f %d | %.3f %.3f", measure.name, s.median(), s.p95(), s.mean(), s.max(), s.over50(),
						s.windowMedian(), s.windowP95()));
			}
			lines.add(String.format(Locale.ROOT, "raw medians %.3f and %.3f ms; the test's own work %.3f and %.3f ms a tick",
					Stats.of(base.raw).median(), Stats.of(garden.raw).median(), ms((double) base.own / MEASURED), ms((double) garden.own / MEASURED)));
			lines.add(String.format(Locale.ROOT, "delta: median %+.3f ms, p95 %+.3f ms; baseline drift (second half less first) %+.3f ms",
					deltaMedian, deltaP95, half(base.net, true) - half(base.net, false)));
			lines.add(memory ? String.format(Locale.ROOT, "memory: retained after a full collection: baseline %d -> %d MiB, garden %d -> %d MiB (%+.0f MiB beyond the baseline's change); "
					+ "heap in use %+.2f and %+.2f MiB per 1,000 ticks; collections %d (%d ms) and %d (%d ms)", mib(base.retainedBefore), mib(base.retainedAfter),
					mib(garden.retainedBefore), mib(garden.retainedAfter), retained, slope(base.heap), slope(garden.heap), base.collections, base.collectionMillis,
					garden.collections, garden.collectionMillis) : "memory: retained heap not measured (the JVM ignored System.gc())");
			lines.add("chunks loaded: baseline " + base.chunksBefore + " -> " + base.chunksAfter + ", garden " + garden.chunksBefore + " -> " + garden.chunksAfter);
			lines.add("counts: " + String.join(", ", work.counts));
			lines.add("level at the end: " + level.getWatchdogStats());
			for (String counterpart : COUNTERPARTS) {
				PactDefinition d = PactPerchBlockEntity.definition(counterpart);
				lines.add(String.format(Locale.ROOT, "active terms: %s's pact %s (radius %d, hours %d to %d, quota %d; upkeep %d of #%s every %d ticks, grace %d, up to %d ahead)",
						counterpart, d.work(), d.radius(), d.from(), d.to(), d.quota(), d.count(), d.offering(), d.period(), d.grace(), d.cap()));
			}
			lines.add(String.format(Locale.ROOT, "the garden's rules sampled while measured: every %d ticks (one think of each spirit between samples; %d samples) no "
					+ "Verdance minted by a move (a pylon's Ley at most 2 for 3), no nutrient minted by a run (at most half reaches), one Dewmoss "
					+ "a graft (each chest's fall equals the new grafts), at most one task counted a pact a think; every %d ticks (%d samples) one "
					+ "presence a pact, one canopy a holder at its Amdusias's perch and no bed's damping above it (canopies seen %d); around each "
					+ "reading no area sample taken (Garden.budget); every %d ticks the Bound Wills' own check, each escrow and the ground. "
					+ "Spirits' statuses at the window samples: %s", WorkerEntity.THINK_TICKS, work.thinkSamples, WINDOW, work.windowSamples,
					work.canopies, AUDIT, work.states));
			lines.add(String.format(Locale.ROOT, "work while measured: Buer eased %d beds, Bathin made %d moves (%d Verdance, %d Ley Charge), Marax gave %d readings (%d "
					+ "answered, reasons %s), Foras grafted %d, Bifrons ran %d roots (%d nutrients taken, %d given), Amdusias covered at most %d beds",
					work.eased, work.moves, work.verdance, work.ley, work.reads, work.answered, work.reasons, work.grafts, work.runs, work.taken,
					work.given, work.covered));
			lines.add(String.format(Locale.ROOT, "the test's own actions, timed apart (not in the tick figures above): readings %s, scaffolding %s (every %d ticks: two "
					+ "beds a holder re-strained to 2, the Hearts refilled to %d Verdance, the Gleaners and pylons emptied, the fallow beds refilled to %d "
					+ "nutrients, each Foras chest topped up to %d Dewmoss, grafts cleared), sampling %s; %.3f ms a tick in all", work.timing(Action.READ),
					work.timing(Action.SCAFFOLD), SCAFFOLD_TICKS, HEART_FILL, RICH, DONORS, work.timing(Action.SAMPLE), work.actedMs() / MEASURED));
			lines.add(String.format(Locale.ROOT, "calls timed after the spans (one unmeasured tick, holder 0's spirits; %d thinks, refreshes and lookups, %d readings "
					+ "and transactions, each after as many untimed; the thinks may change the garden within each quota): Buer's think %.2f us, Bathin's %.2f us, "
					+ "Marax's %.2f us, Foras's %.2f us, Bifrons's %.2f us, Amdusias's %.2f us; a GardenReach refresh (a new index's first read) %.2f us; a canopy "
					+ "lookup (Canopies.damping, one a habitat build) %.2f us; a reading (HabitatReading.read, as the page's Recall calls it; answered %s) %.2f us; "
					+ "a graft's transaction (one Dewmoss extracted in an outer transaction, rolled back) %.2f us", CALLS, READS, calls.thinks()[BUER],
					calls.thinks()[BATHIN], calls.thinks()[MARAX], calls.thinks()[FORAS], calls.thinks()[BIFRONS], calls.thinks()[AMDUSIAS], calls.refresh(),
					calls.canopy(), calls.reason(), calls.reading(), calls.graft()));
			lines.add("escrow and ground: " + work.inventoryLine() + "; problems " + (work.problems.isEmpty() ? "none" : work.problems));
			lines.add(String.format(Locale.ROOT, "network to holder 0, the observed player (uncompressed, unframed): baseline %d packets %.1f KiB (%.2f KiB/s), "
					+ "garden %d packets %.1f KiB (%.2f KiB/s); unsized %d and %d; signs in reach %d and %d (%.1f KiB, an upper bound); pact states a client "
					+ "that declared them would be sent %d (%.1f KiB); holder 1 was sent %d and %d messages (counted, not sized)", tb.packets, kib(tb.bytes),
					kib(tb.bytes) / seconds, tg.packets, kib(tg.bytes), kib(tg.bytes) / seconds, tb.unsized, tg.unsized, tb.signs, tg.signs, kib(tg.signBytes),
					tg.payloads, kib(tg.payloadBytes), others[0], others[1]));
			lines.add("network, garden, holder 0 by kind: " + tg.top(12));
			lines.add("not measured: Jade's replies (they answer a client's request); compression and framing; client frame time (no client here)");
			lines.add("not in the workload: an open kept page left open, the roster, Star-reckoning, an offering falling due, a suspension, ending a pact, "
					+ "a party, a stranger's beds, holder 1's Amdusias at a tree (none beside his perch: he waits for one), an unloading chunk");
			lines.add(String.format(Locale.ROOT, "budget (GC-P141 in C-P19's form, proposed; the owner decides at merge review): median +%.1f ms: %s; p95 +%.1f ms: %s; "
					+ "retained +%.0f MiB beyond the baseline's: %s. Asserted here: +%.1f, +%.1f ms, +%.0f MiB", BUDGET_MEDIAN_MS,
					deltaMedian <= BUDGET_MEDIAN_MS ? "within" : "over", BUDGET_P95_MS, deltaP95 <= BUDGET_P95_MS ? "within" : "over", BUDGET_RETAINED_MIB,
					!memory ? "not measured" : retained <= BUDGET_RETAINED_MIB ? "within" : "over", LIMIT_MEDIAN_MS, LIMIT_P95_MS, LIMIT_RETAINED_MIB));
			lines.add("===== end =====");
			for (String line : lines) {
				Jugcraft.LOGGER.info("Garden load card | {}", line);
			}
		}
	}

	/** The garden: built in one tick, driven while measured, its rules sampled from the services' own memories. */
	private static final class Yard {
		/** Per-call costs timed after the spans, in microseconds; the reading's answer. */
		record Calls(double[] thinks, double refresh, double canopy, double reading, String reason, double graft) {
		}

		final Card card;
		final GameTestHelper helper;
		final ServerLevel level;
		final MinecraftServer server;
		final Set<String> problems = new LinkedHashSet<>();
		final List<String> counts = new ArrayList<>();
		final Map<String, Integer> states = new TreeMap<>();
		final Map<String, Integer> reasons = new TreeMap<>();
		final PactPerchBlockEntity[][] perches = new PactPerchBlockEntity[HOLDERS][PACTS];
		final UUID[][] pacts = new UUID[HOLDERS][PACTS];
		final List<List<VerdantBedBlockEntity>> beds = new ArrayList<>();
		final VerdantHeartBlockEntity[] hearts = new VerdantHeartBlockEntity[HOLDERS];
		final GleanerBlockEntity[] gleaners = new GleanerBlockEntity[HOLDERS];
		final LeyPylonBlockEntity[] pylons = new LeyPylonBlockEntity[HOLDERS];
		final Container[] chests = new Container[HOLDERS];
		/** What the previous think sample saw, per holder and pact: the day's count, each service's last record, each chest's Dewmoss. */
		final int[][] done = new int[HOLDERS][PACTS];
		final Object[][] last = new Object[HOLDERS][PACTS];
		final int[] dewmoss = new int[HOLDERS];
		/** The test's own actions while measured: nanoseconds and count, by Action. */
		final long[][] acted = new long[Action.values().length][2];
		int markedAt = -1;
		int thinkSamples;
		int windowSamples;
		int canopies;
		int eased;
		int moves;
		long verdance;
		long ley;
		int reads;
		int answered;
		int grafts;
		int runs;
		int taken;
		int given;
		int covered;
		int restrain;

		Yard(Card card) {
			this.card = card;
			this.helper = card.helper;
			this.level = card.level;
			this.server = card.server;
		}

		void build() {
			long now = level.getGameTime();
			PactDefinition marax = PactPerchBlockEntity.definition(MaraxEntity.COUNTERPART);
			helper.assertTrue((MEASURED + READ_TICKS - 1) / READ_TICKS <= marax.quota(),
					"Marax's quota permits a reading every " + READ_TICKS + " ticks while measured: " + marax.quota() + " a game day");
			Block[] crops = {Garden.SUNPETAL_CROP, Garden.DEWMOSS_CROP, Garden.GLOAMCAP_CROP, Garden.MENDVETCH_CROP};
			for (int h = 0; h < HOLDERS; h++) {
				ServerPlayer holder = card.players.get(h);
				UUID keeper = holder.getUUID();
				// The beds: a water row between, so they stay wet; 20 planted (four kinds, every fifth ripe), 4 fallow and rich.
				for (int x = 4; x <= 9; x++) {
					helper.setBlock(at(h, x, 1, 11), Blocks.WATER);
				}
				List<VerdantBedBlockEntity> own = new ArrayList<>();
				for (int i = 0; i < BEDS / HOLDERS; i++) {
					BlockPos pos = bedAt(h, i);
					helper.setBlock(pos, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
					VerdantBedBlockEntity bed = helper.getBlockEntity(pos, VerdantBedBlockEntity.class);
					bed.awaken(keeper);
					if (FALLOW.contains(i)) {
						bed.setNutrients(RICH);
					} else {
						OrganismCropBlock crop = (OrganismCropBlock) crops[i % crops.length];
						helper.setBlock(pos.above(), crop.getStateForAge(i % 5 == 0 ? OrganismCropBlock.MAX_AGE : (i * 3) % OrganismCropBlock.MAX_AGE));
						bed.setNutrients(2 + (i * 7) % 29);
					}
					bed.setStrain(i % 4);
					own.add(bed);
				}
				beds.add(own);
				// A Heart with Verdance on stone, a Gleaner and a pylon of theirs (holder 0's pylon its Heart's own road).
				helper.setBlock(heartAt(h), Garden.VERDANT_HEART);
				hearts[h] = helper.getBlockEntity(heartAt(h), VerdantHeartBlockEntity.class);
				hearts[h].awaken(keeper);
				hearts[h].setVerdance(HEART_FILL);
				helper.setBlock(gleanerAt(h), Garden.GLEANER);
				gleaners[h] = helper.getBlockEntity(gleanerAt(h), GleanerBlockEntity.class);
				gleaners[h].awaken(keeper);
				gleaners[h].setVerdance(0);
				helper.setBlock(pylonAt(h), JugcraftConcordance.LEY_PYLON);
				pylons[h] = helper.getBlockEntity(pylonAt(h), LeyPylonBlockEntity.class);
				pylons[h].setOwner(keeper);
				pylons[h].setLey(level, 0);
				// Four disturbing blocks beside the beds (concordance/disturbance: budding amethyst gives 1).
				for (int z : new int[] {9, 10, 12, 13}) {
					helper.setBlock(at(h, 3, 2, z), Blocks.BUDDING_AMETHYST);
				}
				// Seven perches in a row, each sealed under its shipped offer (the compact left out) with one offering in escrow.
				for (int k = 0; k < PACTS; k++) {
					PactDefinition d = PactPerchBlockEntity.definition(COUNTERPARTS.get(k));
					PactPerchBlockEntity perch = place(perchAt(h, k), holder);
					AgreementHolder.Sealed sealed = perch.holder().seal(level, holder, PactPerchBlockEntity.sealing(d, null, null), now);
					helper.assertTrue(sealed.outcome() == Pacts.Outcome.DONE, "Holder " + h + "'s pact with " + d.id() + " is sealed: " + sealed.outcome());
					perches[h][k] = perch;
					pacts[h][k] = perch.holder().agreement().record();
					Item offering = ConcordanceWorkerGameTests.offeringOf(d);
					holder.getInventory().add(new ItemStack(offering, d.count() * ESCROW));
					int paid = PactKeeping.deposit(level, holder, perch.holder(), offering, now);
					helper.assertTrue(paid == d.count() * ESCROW, "Holder " + h + " pays one offering into " + d.id() + "'s escrow: " + paid);
				}
				// A chest of Dewmoss touching the Foras perch (his store).
				helper.setBlock(chestAt(h), Blocks.CHEST);
				chests[h] = (Container) level.getBlockEntity(helper.absolutePos(chestAt(h)));
				chests[h].setItem(0, new ItemStack(Garden.DEWMOSS, DONORS));
				// The answers: holder 0's Bathin takes from the Heart with its own road; both Foras graft every host.
				if (h == 0) {
					resolve(holder, perches[h][BATHIN], VerdanceCarrying.WORK, VerdanceCarrying.CLAIM, now);
				}
				resolve(holder, perches[h][FORAS], GraftKeeping.WORK, GraftKeeping.ALWAYS, now);
			}
			// Holder 0's Mulch Maw and oak (logs, and persistent leaves so none decays and drops).
			helper.setBlock(mawAt(), Garden.MULCH_MAW);
			for (int y = 0; y < 3; y++) {
				helper.setBlock(treeAt().above(y), Blocks.OAK_LOG);
			}
			BlockPos crown = treeAt().above(3);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					helper.setBlock(crown.offset(dx, 0, dz), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
				}
			}
			helper.setBlock(crown.above(), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
			int present = 0;
			for (int h = 0; h < HOLDERS; h++) {
				for (int k = 0; k < PACTS; k++) {
					present += presence(h, k) != null ? 1 : 0;
				}
			}
			helper.assertTrue(present == HOLDERS * PACTS, "Each pact has its presence at its perch: " + present);
			counts.addAll(List.of("holders " + HOLDERS + " (each keeping " + PACTS + " pacts: the six garden spirits and Stolas)",
					"Pact Perches " + HOLDERS * PACTS, "Bound Wills " + HOLDERS * PACTS, "escrow one offering a pact",
					"Verdant Beds " + BEDS + " (awake, wet; " + (BEDS - HOLDERS * FALLOW.size()) + " planted, " + HOLDERS * FALLOW.size() + " fallow)",
					"Verdant Hearts " + HEARTS, "Gleaners " + GLEANERS + " (holder 0's beyond its Heart's reach, holder 1's within)",
					"Ley Pylons " + PYLONS + " (holder 0's beside its Heart, holder 1's apart)", "Mulch Maws " + MAWS, "disturbing blocks " + DISTURBING,
					"trees " + TREES + " (beside holder 0's Amdusias)", "chests " + HOLDERS + " (" + DONORS + " Dewmoss, touching each Foras perch)",
					"readings every " + READ_TICKS + " ticks a holder while measured"));
		}

		/** A Pact Perch at {@code at}, placed by {@code owner} (its block's setPlacedBy records them, as a player's placing does). */
		PactPerchBlockEntity place(BlockPos at, ServerPlayer owner) {
			helper.setBlock(at, Workers.PACT_PERCH);
			BlockPos absolute = helper.absolutePos(at);
			Workers.PACT_PERCH.setPlacedBy(level, absolute, level.getBlockState(absolute), owner, new ItemStack(Workers.PACT_PERCH_ITEM));
			return helper.getBlockEntity(at, PactPerchBlockEntity.class);
		}

		/** The holder answers a matter at the perch: PactAccords.resolve, the call the kept page's RESOLVE makes (it asks no matter raised first). */
		void resolve(ServerPlayer holder, PactPerchBlockEntity perch, String work, String value, long now) {
			helper.assertTrue(PactAccords.resolve(level, holder, perch, work, value, now), "The perch keeps the answer " + value + " for " + work);
		}

		@Nullable PactSpiritEntity<?> presence(int h, int k) {
			PactSpiritEntity<?> spirit = perches[h][k].presence(level);
			return spirit != null && pacts[h][k].equals(spirit.record()) && card.players.get(h).getUUID().equals(spirit.owner()) ? spirit : null;
		}

		@Nullable BoundWill record(int h, int k) {
			return BoundWills.of(server).ledger().get(pacts[h][k]);
		}

		int doneToday(int h, int k, long gameTime) {
			BoundWill record = record(h, k);
			return record == null || record.pact() == null ? -1 : Pacts.done(record.pact(), gameTime);
		}

		/** The service memory's last record of each service that keeps one (Bathin's move, Foras's graft, Bifrons's run), or null. */
		@Nullable Object lastOf(int h, int k) {
			PactSpiritEntity<?> spirit = presence(h, k);
			if (spirit == null) {
				return null;
			}
			return switch (k) {
				case BATHIN -> spirit.serviceMemory(VerdanceCarrying.Memory.class, VerdanceCarrying.Memory::new).last();
				case FORAS -> spirit.serviceMemory(GraftKeeping.Memory.class, GraftKeeping.Memory::new).last();
				case BIFRONS -> spirit.serviceMemory(RootRunning.Memory.class, RootRunning.Memory::new).last();
				default -> null;
			};
		}

		AABB yard() {
			return helper.absoluteAABB(new AABB(OFF, 0, OFF, OFF + SIZE + 1, 16, OFF + SIZE + 1));
		}

		void drive(int now) {
			if (card.current == null || markedAt < 0) {
				return;
			}
			long gameTime = level.getGameTime();
			if (now % WorkerEntity.THINK_TICKS == 0) {
				long started = System.nanoTime();
				sampleThinks(gameTime);
				if (now % WINDOW == 0) {
					sampleWindow(gameTime);
				}
				acted(Action.SAMPLE, started);
			}
			if (now % SCAFFOLD_TICKS == 0) {
				long started = System.nanoTime();
				scaffold(gameTime);
				acted(Action.SCAFFOLD, started);
			}
			for (int h = 0; h < HOLDERS; h++) {
				int since = now - markedAt - 50 * h;
				if (since >= 0 && since % READ_TICKS == 0) {
					long started = System.nanoTime();
					read(h, gameTime);
					acted(Action.READ, started);
				}
			}
			if (now % AUDIT == 0) {
				audit("tick " + now);
			}
		}

		void acted(Action action, long started) {
			acted[action.ordinal()][0] += System.nanoTime() - started;
			acted[action.ordinal()][1]++;
		}

		/**
		 * Holder {@code h} asks Marax for a reading as the kept page does: the perch opened, Recall sent with his one choice, the
		 * screen closed. No area sample may be taken by it (Garden.budget's count unchanged across the call, in this one tick).
		 */
		void read(int h, long gameTime) {
			ServerPlayer holder = card.players.get(h);
			RateGate.forget(holder.getUUID());
			PactMenu.open(holder, level, perches[h][MARAX]);
			if (!(holder.containerMenu instanceof PactMenu menu)) {
				problems.add(gameTime + ": holder " + h + "'s Marax perch did not open");
				return;
			}
			PactStatePayload last = menu.lastSent();
			int sampledBefore = Garden.budget(level).used(gameTime);
			PactStatePayload state = PactMenu.receive(holder, new PactRequestPayload(menu.containerId, menu.lastSequence() + 1, PactRequestPayload.RECALL, 0,
					last == null ? 0 : last.revision()));
			int sampledAfter = Garden.budget(level).used(gameTime);
			holder.closeContainer();
			if (sampledAfter != sampledBefore) {
				problems.add(gameTime + ": holder " + h + "'s reading took an area sample (" + sampledBefore + " -> " + sampledAfter + ")");
			}
			if (state == null || state.result().isEmpty()) {
				problems.add(gameTime + ": holder " + h + "'s reading was not answered");
				return;
			}
			String reason = state.result().get().reason();
			reasons.merge(reason, 1, Integer::sum);
			answered++;
			if (HabitatReading.READ.equals(reason)) {
				reads++;
			}
			if (h == 0) {
				card.current.traffic.payload(state, card.codec);
			}
			// The reading's own count is the test's: the next think sample starts from it.
			done[h][MARAX] = doneToday(h, MARAX, gameTime);
		}

		/** The test's scaffolding: what the services used is put back, so each keeps work for the whole span. */
		void scaffold(long gameTime) {
			for (int h = 0; h < HOLDERS; h++) {
				List<VerdantBedBlockEntity> own = beds.get(h);
				for (int n = 0; n < 2; n++) {
					int i = restrain++ % own.size();
					own.get(i).setStrain(2);
				}
				for (int i = 0; i < own.size(); i++) {
					VerdantBedBlockEntity bed = own.get(i);
					if (FALLOW.contains(i)) {
						bed.setNutrients(RICH);
					}
					if (!bed.graft().isEmpty()) {
						bed.setGraft("");
					}
				}
				hearts[h].setVerdance(HEART_FILL);
				gleaners[h].setVerdance(0);
				pylons[h].setLey(level, 0);
				chests[h].clearContent();
				chests[h].setItem(0, new ItemStack(Garden.DEWMOSS, DONORS));
				dewmoss[h] = DONORS;
			}
		}

		/** The measured span begins at {@code start} (its first tick): the samples start from what stands now. */
		void mark(int start) {
			markedAt = start;
			long gameTime = level.getGameTime() + 1;
			// The stocks the warm-up used are put back first, as the first measured scaffolding would (this tick is not measured).
			scaffold(gameTime);
			for (int h = 0; h < HOLDERS; h++) {
				for (int k = 0; k < PACTS; k++) {
					done[h][k] = doneToday(h, k, gameTime);
					last[h][k] = lastOf(h, k);
				}
				dewmoss[h] = count(chests[h], Garden.DEWMOSS);
			}
		}

		/**
		 * Every THINK_TICKS while measured (one think of each spirit between samples): each new move, run and graft by its
		 * service's own record (no Verdance or nutrient minted; one Dewmoss a graft), and each pact's day's count risen by at
		 * most one (one change a think).
		 */
		void sampleThinks(long gameTime) {
			thinkSamples++;
			for (int h = 0; h < HOLDERS; h++) {
				int newGrafts = 0;
				for (int k = 0; k < PACTS; k++) {
					int today = doneToday(h, k, gameTime);
					if (today - done[h][k] > 1) {
						problems.add(gameTime + ": holder " + h + "'s " + COUNTERPARTS.get(k) + " counted " + (today - done[h][k]) + " tasks in one think");
					}
					if (k == BUER && today > done[h][k]) {
						eased += today - done[h][k];
					}
					done[h][k] = today;
					Object now = lastOf(h, k);
					if (now == null || Objects.equals(now, last[h][k])) {
						continue;
					}
					last[h][k] = now;
					if (now instanceof VerdanceCarrying.Move move) {
						moves++;
						verdance += move.verdance();
						ley += move.ley();
						if (move.pylon() ? move.ley() * 3 > move.verdance() * 2 : move.ley() != 0 || move.verdance() > VerdanceCarrying.MOVE_MAX) {
							problems.add(gameTime + ": holder " + h + "'s Bathin minted: " + move);
						}
					} else if (now instanceof RootRunning.Run run) {
						runs++;
						taken += run.taken();
						given += run.given();
						if (run.given() * 2 > run.taken() || run.lost() < run.given()) {
							problems.add(gameTime + ": holder " + h + "'s Bifrons minted nutrients: " + run);
						}
					} else if (now instanceof GraftKeeping.Grafted) {
						grafts++;
						newGrafts++;
					}
				}
				int chest = count(chests[h], Garden.DEWMOSS);
				if (dewmoss[h] - chest != newGrafts) {
					problems.add(gameTime + ": holder " + h + "'s chest gave " + (dewmoss[h] - chest) + " Dewmoss for " + newGrafts + " grafts");
				}
				dewmoss[h] = chest;
			}
		}

		/** Every WINDOW ticks while measured: one presence a pact, one canopy a holder at its Amdusias's perch, no damping above it. */
		void sampleWindow(long gameTime) {
			windowSamples++;
			for (int h = 0; h < HOLDERS; h++) {
				UUID holder = card.players.get(h).getUUID();
				for (int k = 0; k < PACTS; k++) {
					PactSpiritEntity<?> spirit = presence(h, k);
					if (spirit == null) {
						problems.add(gameTime + ": holder " + h + "'s pact with " + COUNTERPARTS.get(k) + " has no presence of its own at its perch");
						continue;
					}
					states.merge(COUNTERPARTS.get(k).replace("jugcraft:", "") + " " + spirit.status().id, 1, Integer::sum);
					if (k == AMDUSIAS) {
						covered = Math.max(covered, spirit.serviceMemory(CanopyShelter.Memory.class, CanopyShelter.Memory::new).covered().size());
					}
				}
				Canopies.Canopy canopy = Canopies.of(level, holder, gameTime);
				if (canopy == null) {
					continue;
				}
				canopies++;
				if (!canopy.perch().equals(helper.absolutePos(perchAt(h, AMDUSIAS)))) {
					problems.add(gameTime + ": holder " + h + "'s canopy stands at " + canopy.perch().toShortString() + ", not their Amdusias's perch");
				}
				for (BlockPos bed : canopy.beds()) {
					int damping = Canopies.damping(level, bed, false, gameTime);
					if (damping > canopy.damping()) {
						problems.add(gameTime + ": the bed at " + bed.toShortString() + " reads damping " + damping + " above its canopy's " + canopy.damping());
					}
				}
			}
		}

		/** Each pact's escrow untouched (no period falls due), the Bound Wills' own check, and nothing on the ground. */
		void audit(String when) {
			for (int h = 0; h < HOLDERS; h++) {
				for (int k = 0; k < PACTS; k++) {
					PactDefinition d = PactPerchBlockEntity.definition(COUNTERPARTS.get(k));
					int kept = BoundWills.of(server).escrow(pacts[h][k]).stream().mapToInt(ItemStack::getCount).sum();
					if (kept != d.count() * ESCROW) {
						problems.add(when + ": holder " + h + "'s escrow with " + d.id() + " holds " + kept + ", not " + d.count() * ESCROW);
					}
				}
			}
			List<ItemEntity> ground = level.getEntitiesOfClass(ItemEntity.class, yard(), ItemEntity::isAlive);
			if (!ground.isEmpty()) {
				problems.add(when + ": items on the ground: " + ground.stream().map(item -> item.getItem().toString()).toList());
			}
			List<String> bound = BoundWills.of(server).problems();
			if (!bound.isEmpty()) {
				problems.add(when + ": the Bound Wills: " + bound);
			}
		}

		String timing(Action action) {
			long[] row = acted[action.ordinal()];
			return String.format(Locale.ROOT, "%d at %.1f us", row[1], row[1] == 0 ? 0.0 : row[0] / 1000.0 / row[1]);
		}

		double actedMs() {
			long nanos = 0;
			for (long[] row : acted) {
				nanos += row[0];
			}
			return ms((double) nanos);
		}

		/**
		 * After the spans, in one unmeasured tick, holder 0's: each garden spirit's think, a GardenReach refresh (a new index of
		 * Buer's pact read once: what a refresh costs), a canopy lookup on their first bed (Canopies.damping, the call each habitat
		 * build makes under a canopy), a reading (HabitatReading.read, the call the page's Recall makes) and a graft's transaction
		 * (GraftKeeping.spend opens its own outer transaction, so the same extraction is timed in one rolled back), each after as
		 * many untimed calls. Thinks may change the garden within each quota; nothing here is measured as a tick.
		 */
		Calls timeCalls() {
			long gameTime = level.getGameTime();
			long time = level.getOverworldClockTime();
			double[] thinks = new double[PACTS];
			for (int k = BUER; k < PACTS; k++) {
				PactSpiritEntity<?> spirit = presence(0, k);
				if (spirit == null) {
					problems.add("the timed calls found no presence for holder 0's " + COUNTERPARTS.get(k));
					continue;
				}
				long nanos = 0;
				for (int pass = 0; pass < 2; pass++) {
					long start = System.nanoTime();
					for (int i = 0; i < CALLS; i++) {
						spirit.think(level, time, gameTime);
					}
					nanos = System.nanoTime() - start;
				}
				thinks[k] = nanos / 1000.0 / CALLS;
			}
			BoundWill buer = record(0, BUER);
			BlockPos bed = beds.get(0).getFirst().getBlockPos();
			ServerPlayer reader = card.players.get(0);
			Storage<ItemVariant> store = ContainerStorage.of(chests[0], null);
			ItemVariant donor = ItemVariant.of(Garden.DEWMOSS);
			long refresh = 0;
			long canopy = 0;
			long reading = 0;
			long graft = 0;
			String reason = "?";
			int indexed = 0;
			for (int pass = 0; pass < 2; pass++) {
				long start = System.nanoTime();
				for (int i = 0; i < CALLS && buer != null; i++) {
					indexed += GardenReach.of(buer).beds(level, gameTime).size();
				}
				refresh = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 0; i < CALLS; i++) {
					indexed += Canopies.damping(level, bed, false, gameTime);
				}
				canopy = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 0; i < READS; i++) {
					reason = HabitatReading.read(level, reader, perches[0][MARAX].holder(), time, gameTime).reason();
				}
				reading = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 0; i < READS; i++) {
					try (Transaction transaction = Transaction.openOuter()) {
						indexed += (int) store.extract(donor, 1, transaction);
					}
				}
				graft = System.nanoTime() - start;
			}
			if (indexed == 0) {
				problems.add("the timed calls read nothing");
			}
			return new Calls(thinks, refresh / 1000.0 / CALLS, canopy / 1000.0 / CALLS, reading / 1000.0 / READS, reason, graft / 1000.0 / READS);
		}

		String inventoryLine() {
			int escrow = 0;
			int held = 0;
			for (int h = 0; h < HOLDERS; h++) {
				for (int k = 0; k < PACTS; k++) {
					escrow += BoundWills.of(server).escrow(pacts[h][k]).stream().mapToInt(ItemStack::getCount).sum();
				}
				held += BoundWills.of(server).ledger().heldBy(card.players.get(h).getUUID()).size();
			}
			return String.format(Locale.ROOT, "escrow %d items in %d pacts (one offering each, none taken: no period fell due); the compacts not paid (sealed as "
					+ "the worker tests seal); Bound Wills held %d; Dewmoss in the chests %d and %d; ground and the Bound Wills checked", escrow, HOLDERS * PACTS,
					held, count(chests[0], Garden.DEWMOSS), count(chests[1], Garden.DEWMOSS));
		}
	}
}
