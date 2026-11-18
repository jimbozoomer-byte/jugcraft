package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.raiders.Raider;
import io.github.jimbozoomer.jugcraft.raiders.RaiderBlimp;
import io.github.jimbozoomer.jugcraft.raiders.RaiderRaids;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.town.CityGuardVantage;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.town.TownBuilder;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownDecor;
import io.github.jimbozoomer.jugcraft.town.TownDefense;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.TownsfolkCare;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reproducible raid on the real town (docs/features/raiders.md, "City approaches and visible fights"): the east half of
 * the town is built from its blueprint far from every other test, its residents come out (gate guards, square guards, a
 * guard walker, the two eastern flak crews), every guard is given the watch ({@link CityGuardVantage}) as the Townsfolk
 * hook would, and a level-3 raid (infantry, a crewed walker and a crewed blimp) comes at it from the east, gathering
 * past the Styx estate's corner. Over {@value #MEASURE_TICKS} ticks it measures and logs one line:
 *
 * <pre>[city-raid-scenario] engagedAfter=T firstHurtAfter=T maxInSight=N meanInSight=F raiders=N fallen=N stuck=N
 * targetsGivenUp=N approachesGivenUp=N holding=N towerPileup=N wallFoot=N guards=N vantageMoves=N</pre>
 *
 * <ul>
 * <li>{@code engagedAfter}: ticks from the raid's start until a guard first takes on a raider or a raider takes on a
 * guard in reach and in sight (-1: never).</li>
 * <li>{@code maxInSight}/{@code meanInSight}: raiders a guard has a clear line to, within the guard's sight (a foot
 * guard's 48 blocks, a guard walker's 96, a flak crew's 250), sampled every 20 ticks.</li>
 * <li>{@code stuck}: targets plus approaches the raiders gave up (their bounded recovery), with {@code holding} those
 * that gave their march up for good.</li>
 * <li>{@code towerPileup}: the most raiders on the ground at one sample within 8 blocks of a corner tower;
 * {@code wallFoot}: the most within 3 blocks outside the wall's face away from the gate.</li>
 * </ul>
 *
 * It asserts the raid became a fight the guards could see: an engagement, at least two raiders in a guard's sight at
 * once, nobody piled under a tower. Its own test environment ({@code jugcraft-test:city_raid}) keeps it in a batch of its
 * own, so placing the town cannot collide with TownGameTests' test town. Run alone with the game-test filter for
 * {@code cityraidscenariogametests}. Not deterministic to the tick (mobs choose their own steps), but the layout,
 * party, gathering point and seed are fixed.
 */
public class CityRaidScenarioGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-city-raid");
	/** The scenario town's corner, chunk-aligned and far from every other test (TownGameTests uses 48 000). */
	static final int FAR = 56_000;
	/** Ticks the raid is watched for: under the raid's abandon time, as no player stands near. */
	static final int MEASURE_TICKS = 1800;
	static final int SAMPLE_TICKS = 20;
	/** The raid's level: a crewed walker and a crewed blimp come too. */
	static final int RAID_LEVEL = 3;

	@GameTest(maxTicks = 7000, environment = "jugcraft-test:city_raid")
	public void cityRaidOnTheEastGateIsAVisibleFight(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		TownState state = TownState.get(level);
		h.assertTrue(state.origin() == null, "The scenario needs a world without a town of its own (the flat test world)");
		// The pinned test world's surface is not minY + 3. A town placed there
		// sits 64 blocks below its raiders, inside an artificial excavation.
		level.getChunk(FAR >> 4, FAR >> 4);
		int ground = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FAR, FAR) - 1;
		BlockPos origin = new BlockPos(FAR, ground, FAR);
		state.place(origin);
		TownData data = TownData.get();
		// The east half round the east gate (local x 96..191, z 64..127), the open ground east of it to the gathering
		// point, and the chunks of the two eastern flak towers.
		List<ChunkPos> chunks = new ArrayList<>();
		for (int cx = (origin.getX() + 96) >> 4; cx <= (origin.getX() + 239) >> 4; cx++) {
			for (int cz = (origin.getZ() + 64) >> 4; cz <= (origin.getZ() + 127) >> 4; cz++) {
				chunks.add(new ChunkPos(cx, cz));
			}
		}
		List<BlockPos> towers = new ArrayList<>();
		for (TownData.Spot spot : data.spots) {
			if (spot.role().equals("flak_guard") && spot.pos().getX() > 96) {
				BlockPos at = origin.offset(spot.pos());
				towers.add(at);
				ChunkPos chunk = ChunkPos.containing(at);
				if (!chunks.contains(chunk)) {
					chunks.add(chunk);
				}
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
		BlockPos centre = Town.centre(origin);
		BlockPos gather = origin.offset(214, 1, 112);
		AABB area = new AABB(Vec3.atLowerCornerOf(origin.offset(80, -8, 30)), Vec3.atLowerCornerOf(origin.offset(250, 60, 165)));
		Metrics m = new Metrics();
		RaiderRaids.Raid[] raid = {null};
		int[] tick = {0};
		int[] started = {-1};
		h.onEachTick(() -> {
			tick[0]++;
			if (m.finished) {
				return;
			}
			if (raid[0] == null) {
				// Wait for the chunks to tick their entities (generation runs off the server thread), then start.
				boolean ticking = chunks.stream().allMatch(c -> level.isPositionEntityTicking(new BlockPos(c.x() << 4, origin.getY() + 2, c.z() << 4)));
				if (!ticking && tick[0] < 3600) {
					return;
				}
				for (Townsfolk guard : guards(level, area)) {
					CityGuardVantage.install(guard);
				}
				raid[0] = RaiderRaids.start(level, centre, gather, RAID_LEVEL, RandomSource.create(1907));
				if (raid[0] == null) {
					finish(h, state, chunks, m, null);
					h.assertTrue(JugcraftSeasons.thanksgivingTruce(), "The city raid should start (outside the Thanksgiving truce)");
					h.succeed();
					return;
				}
				started[0] = tick[0];
				m.raiders = raid[0].total();
				LOGGER.info("[city-raid-scenario] raid of {} started at tick {} from {} (local {}), after {} ticks of loading; {} guards",
						m.raiders, tick[0], gather, gather.subtract(origin), tick[0], guards(level, area).size());
				return;
			}
			int since = tick[0] - started[0];
			if (since == 1 || since % 600 == 0) {
				for (UUID member : raid[0].members()) {
					if (level.getEntity(member) instanceof Mob mob) LOGGER.info("[city-raid-detail] t={} type={} pos={} age={} target={} hp={}", since,
							mob.getType(), mob.position().subtract(Vec3.atLowerCornerOf(origin)), mob.tickCount,
							mob.getTarget() == null ? "none" : mob.getTarget().getType(), mob.getHealth());
				}
				for (Townsfolk guard : guards(level, area)) LOGGER.info("[city-guard-detail] t={} role={} pos={} age={} target={}", since,
						guard.role(), guard.position().subtract(Vec3.atLowerCornerOf(origin)), guard.tickCount,
						guard.getTarget() == null ? "none" : guard.getTarget().getType());
			}
			sample(level, data, origin, raid[0], towers, area, m, since);
			if (since >= MEASURE_TICKS || !RaiderRaids.active(level, raid[0].id())) {
				m.finished = true;
				finish(h, state, chunks, m, raid[0]);
				report(m, raid[0], guards(level, area));
				h.assertTrue(m.engagedAfter >= 0, "The guards and raiders should come to blows within " + MEASURE_TICKS + " ticks");
				h.assertTrue(m.maxInSight >= 2, "At least two raiders should be in a guard's sight at once, not " + m.maxInSight);
				h.assertTrue(m.towerPileup == 0, m.towerPileup + " raiders piled up under a corner tower");
				h.succeed();
			}
		});
	}

	/** What the scenario measures. */
	static final class Metrics {
		boolean finished;
		int raiders;
		int engagedAfter = -1;
		int firstHurtAfter = -1;
		int maxInSight;
		long sumInSight;
		int samples;
		int towerPileup;
		int wallFoot;
		/** Each raider's last known recovery counts: [targets given up, approaches given up, holding]. */
		final Map<UUID, int[]> recovery = new HashMap<>();
	}

	private static void sample(ServerLevel level, TownData data, BlockPos origin, RaiderRaids.Raid raid, List<BlockPos> towers, AABB area,
			Metrics m, int since) {
		List<Townsfolk> guards = guards(level, area);
		List<Mob> raiders = new ArrayList<>();
		for (UUID id : raid.members()) {
			if (level.getEntity(id) instanceof Mob mob && mob.isAlive()) {
				raiders.add(mob);
				var engagement = ((Raider) mob).engagement();
				m.recovery.put(id, new int[] {engagement.targetsGivenUp(), engagement.approachesGivenUp(), engagement.holding() ? 1 : 0});
				if (m.firstHurtAfter < 0 && mob.getHealth() < mob.getMaxHealth()) {
					m.firstHurtAfter = since;
				}
				if (m.engagedAfter < 0 && mob.getTarget() instanceof Townsfolk target && engagement.engaging(target)) {
					m.engagedAfter = since;
				}
			}
		}
		for (Townsfolk guard : guards) {
			if (m.engagedAfter < 0 && guard.getTarget() != null && guard.getTarget() instanceof Raider) {
				m.engagedAfter = since;
			}
		}
		if (since % SAMPLE_TICKS != 0) {
			return;
		}
		int inSight = 0;
		int underTowers = 0;
		int atWallFoot = 0;
		for (Mob raider : raiders) {
			Vec3 aim = raider.getBoundingBox().getCenter();
			for (Townsfolk guard : guards) {
				double sight = guard.role().equals("flak_guard") ? TownDefense.RANGE : CityGuardVantage.sight(guard);
				if (guard.getEyePosition().distanceTo(aim) <= sight && TownDefense.visible(level, guard, guard.getEyePosition(), aim)) {
					inSight++;
					break;
				}
			}
			if (raider instanceof RaiderBlimp || raider.isPassenger()) {
				continue;
			}
			for (BlockPos tower : towers) {
				if (raider.position().subtract(Vec3.atBottomCenterOf(tower)).horizontalDistance() < 8.0) {
					underTowers++;
					break;
				}
			}
			// Outside the wall, within three blocks of its face, and not on the east gate's road.
			int lx = raider.getBlockX() - origin.getX();
			int lz = raider.getBlockZ() - origin.getZ();
			if (data.mask(lx, lz) != TownData.INSIDE && Math.abs(lz - 96) > 6 && nearInside(data, lx, lz, 3)) {
				atWallFoot++;
			}
		}
		m.maxInSight = Math.max(m.maxInSight, inSight);
		m.sumInSight += inSight;
		m.samples++;
		m.towerPileup = Math.max(m.towerPileup, underTowers);
		m.wallFoot = Math.max(m.wallFoot, atWallFoot);
	}

	private static boolean nearInside(TownData data, int x, int z, int r) {
		for (int dx = -r; dx <= r; dx++) {
			for (int dz = -r; dz <= r; dz++) {
				if (data.mask(x + dx, z + dz) == TownData.INSIDE) {
					return true;
				}
			}
		}
		return false;
	}

	/** The town's guards on foot, guard walkers and flak crews in the built area (loaded only). */
	private static List<Townsfolk> guards(ServerLevel level, AABB area) {
		return level.getEntitiesOfClass(Townsfolk.class, area, t -> t.isAlive() && t.isGuard());
	}

	private static void report(Metrics m, RaiderRaids.Raid raid, List<Townsfolk> guards) {
		int given = 0;
		int approaches = 0;
		int holding = 0;
		for (int[] r : m.recovery.values()) {
			given += r[0];
			approaches += r[1];
			holding += r[2];
		}
		int moves = 0;
		for (Townsfolk guard : guards) {
			CityGuardVantage.WatchGoal watch = CityGuardVantage.of(guard);
			if (watch != null) {
				moves += watch.moves();
			}
		}
		LOGGER.info("[city-raid-scenario] engagedAfter={} firstHurtAfter={} maxInSight={} meanInSight={} raiders={} fallen={} stuck={} "
				+ "targetsGivenUp={} approachesGivenUp={} holding={} towerPileup={} wallFoot={} guards={} vantageMoves={}",
				m.engagedAfter, m.firstHurtAfter, m.maxInSight, m.samples == 0 ? 0 : String.format("%.2f", m.sumInSight / (double) m.samples),
				m.raiders, m.raiders - raid.members().size(), given + approaches, given, approaches, holding, m.towerPileup, m.wallFoot,
				guards.size(), moves);
	}

	/** Ends the scenario: the raid withdraws, the town is forgotten (its blocks stay, far away) and the chunks are let go. */
	private static void finish(GameTestHelper h, TownState state, List<ChunkPos> chunks, Metrics m, RaiderRaids.@Nullable Raid raid) {
		ServerLevel level = h.getLevel();
		if (raid != null && RaiderRaids.active(level, raid.id())) {
			RaiderRaids.withdraw(level, raid);
		}
		state.forget();
		for (ChunkPos pos : chunks) {
			level.setChunkForced(pos.x(), pos.z(), false);
		}
		m.finished = true;
	}
}
