package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FlamingPumpkin;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.agriculture.HorsemanRider;
import io.github.jimbozoomer.jugcraft.agriculture.HorsemanSightings;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Game tests for the Headless Horseman's sightings in the Halloween season (docs/features/horseman-sightings.md): when a
 * player is due one; that he rides at a player from in front of them, comes close, swings round them, flings his
 * pumpkin, rides off and is gone, without hurting them, and that nothing hurts him; that his pumpkin only frightens; that
 * everyone near has seen him and nobody far; and that a player just come into the game is left to settle.
 *
 * <p>Sightings come on their own only on passing nights in the season; these tests send him, so the server's date and
 * clock do not matter. The ride runs in a field-sized arena of air over a floor the test lays.
 */
public class HorsemanSightingsGameTests {
	/** An arena of air 69 by 76 blocks: room for his whole ride. */
	private static final String FIELD = "jugcraft-test:crowfield_plain";

	private static ServerPlayer player(GameTestHelper helper, double x, double z, float yaw) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos at = helper.absolutePos(BlockPos.ZERO);
		player.snapTo(at.getX() + x, at.getY() + 1, at.getZ() + z, yaw, 0.0F);
		return player;
	}

	/** A floor of dirt under the arena, its top at the arena's floor. */
	private static void floor(GameTestHelper helper, int width, int depth) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < width; x++) {
			for (int z = 0; z < depth; z++) {
				level.setBlock(helper.absolutePos(new BlockPos(x, 0, z)), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	private static boolean earned(ServerPlayer player, String id) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(id));
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	/** On their first night, every two or three days after, and on Halloween if not yet that day; never twice a day. */
	@GameTest
	public void aSightingComesTheFirstNightThenEveryFewDaysAndOnHalloween(GameTestHelper helper) {
		UUID player = UUID.randomUUID();
		long today = 20_000L;
		long halloween = today + 10;
		helper.assertTrue(HorsemanSightings.due(HorsemanSightings.NEVER, today, halloween, player), "A player who has never seen him is due");
		helper.assertFalse(HorsemanSightings.due(today, today, halloween, player), "Not twice in a day");
		int gap = HorsemanSightings.gap(player, today);
		helper.assertTrue(gap >= HorsemanSightings.MIN_GAP_DAYS && gap <= HorsemanSightings.MAX_GAP_DAYS, "The gap is two or three days: " + gap);
		helper.assertFalse(HorsemanSightings.due(today, today + gap - 1, halloween, player), "Not before the gap is out");
		helper.assertTrue(HorsemanSightings.due(today, today + gap, halloween, player), "Due when it is");
		helper.assertTrue(HorsemanSightings.due(halloween - 1, halloween, halloween, player), "On Halloween, a day after the last");
		helper.assertFalse(HorsemanSightings.due(halloween, halloween, halloween, player), "but only once that day");
		helper.assertTrue(HorsemanSightings.due(today + 5, today, halloween, player), "and again if the clock was set back");
		Set<Integer> gaps = new HashSet<>();
		for (int i = 0; i < 64; i++) {
			gaps.add(HorsemanSightings.gap(UUID.randomUUID(), today + i));
		}
		helper.assertTrue(gaps.equals(Set.of(HorsemanSightings.MIN_GAP_DAYS, HorsemanSightings.MAX_GAP_DAYS)), "Both gaps come up: " + gaps);
		helper.succeed();
	}

	/**
	 * Sent at a player, he comes from {@value HorsemanSightings#START_DISTANCE} blocks in front of them, gallops close,
	 * swings round them, flings his pumpkin and rides off, gone before {@value HorsemanRider#MAX_TICKS} ticks; the player
	 * is never hurt, has seen him and earns Sleepy Hollow, and nothing hurts him.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void theHorsemanRidesAtAPlayerAndOnlyFrightensThem(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 69, 76);
		// He comes only from ticking chunks, as a real player's own are: the arena's are forced for the ride, and the
		// test waits for them to start ticking before he is sent.
		List<ChunkPos> arena = new ArrayList<>();
		ChunkPos from = ChunkPos.containing(helper.absolutePos(BlockPos.ZERO));
		ChunkPos to = ChunkPos.containing(helper.absolutePos(new BlockPos(68, 0, 75)));
		for (int cx = Math.min(from.x(), to.x()); cx <= Math.max(from.x(), to.x()); cx++) {
			for (int cz = Math.min(from.z(), to.z()); cz <= Math.max(from.z(), to.z()); cz++) {
				level.setChunkForced(cx, cz, true);
				arena.add(new ChunkPos(cx, cz));
			}
		}
		ServerPlayer player = player(helper, 34.5, 40.5, 0.0F);
		AtomicReference<HorsemanRider> sent = new AtomicReference<>();
		double[] closest = {Double.MAX_VALUE};
		boolean[] hurt = {false};
		float health = player.getHealth();
		helper.runAfterDelay(5, () -> {
			HorsemanRider rider = HorsemanSightings.send(level, player);
			helper.assertTrue(rider != null, "He comes");
			double start = rider.distanceTo(player);
			helper.assertTrue(start >= HorsemanSightings.START_DISTANCE - 2.0, "from far off: " + start);
			helper.assertTrue(rider.getZ() > player.getZ(), "in front of the player, who looks south");
			helper.assertFalse(rider.hurtServer(level, level.damageSources().generic(), 10.0F), "Nothing hurts him");
			helper.assertFalse(rider.isPickable(), "nor can he be struck");
			sent.set(rider);
		});
		helper.onEachTick(() -> {
			HorsemanRider rider = sent.get();
			if (rider != null && !rider.isRemoved()) {
				closest[0] = Math.min(closest[0], Math.hypot(rider.getX() - player.getX(), rider.getZ() - player.getZ()));
			}
			hurt[0] |= player.getHealth() < health;
		});
		long today = HalloweenSeason.date().toEpochDay();
		helper.succeedWhen(() -> {
			HorsemanRider rider = sent.get();
			helper.assertTrue(rider != null, "He comes");
			helper.assertTrue(rider.thrown(), "He flings his pumpkin");
			helper.assertTrue(rider.isRemoved(), "and rides off, gone: " + rider.phase());
			helper.assertTrue(closest[0] <= HorsemanRider.CIRCLE_RADIUS + 1.5, "having come close: " + closest[0]);
			helper.assertFalse(hurt[0], "The player is never hurt");
			helper.assertTrue(HorsemanSightings.seen(player) == today, "and has seen him today: " + HorsemanSightings.seen(player));
			helper.assertTrue(earned(player, HorsemanSightings.ADVANCEMENT), "Sleepy Hollow");
			for (ChunkPos chunk : arena) {
				level.setChunkForced(chunk.x(), chunk.z(), false);
			}
		});
	}

	/** His pumpkin bursts as the summoned Horseman's does, but only blinds and pushes back a player; it hurts and burns nobody. */
	@GameTest
	public void hisPumpkinOnlyFrightens(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 8, 8);
		ServerPlayer player = player(helper, 3.5, 3.5, 0.0F);
		var cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(4, 1, 4));
		HorsemanRider rider = helper.spawnWithNoFreeWill(JugcraftAgriculture.HORSEMAN_RIDER, new BlockPos(1, 1, 1));
		float playerHealth = player.getHealth();
		float cowHealth = cow.getHealth();
		FlamingPumpkin pumpkin = FlamingPumpkin.harmless(level, rider);
		helper.assertTrue(pumpkin.isHarmless(), "His pumpkin is the harmless kind");
		pumpkin.burst(level, player.position().add(-0.5, 0.0, 0.0));
		helper.assertTrue(player.getHealth() == playerHealth && !player.isOnFire(), "The player is not hurt or alight");
		helper.assertTrue(player.hasEffect(MobEffects.BLINDNESS), "but blinded for a moment");
		helper.assertTrue(player.getDeltaMovement().x > 0.0, "and thrown back from the burst: " + player.getDeltaMovement());
		helper.assertTrue(cow.getHealth() == cowHealth && !cow.isOnFire(), "The cow is not hurt or alight either");
		rider.discard();
		helper.succeed();
	}

	/** Everyone within {@value HorsemanSightings#WITNESS_RANGE} blocks has seen him; a player farther off has not. */
	@GameTest
	public void everyoneNearHasSeenHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer near = player(helper, 3.5, 3.5, 0.0F);
		ServerPlayer far = player(helper, 3.5 + HorsemanSightings.WITNESS_RANGE + 12.0, 3.5, 0.0F);
		HorsemanSightings.witnessed(level, near.position().add(10.0, 0.0, 0.0));
		long today = HalloweenSeason.date().toEpochDay();
		helper.assertTrue(HorsemanSightings.seen(near) == today && earned(near, HorsemanSightings.ADVANCEMENT), "The player near has seen him");
		helper.assertTrue(HorsemanSightings.seen(far) == HorsemanSightings.NEVER && !earned(far, HorsemanSightings.ADVANCEMENT),
				"the one far off has not");
		helper.succeed();
	}

	/** A player just come into the game is left to settle; one in the game a while, out under the sky, is ready. */
	@GameTest(skyAccess = true)
	public void aPlayerJustComeIntoTheGameIsLeftToSettle(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// A player who can become a spectator (the vanilla mock player always reports creative).
		ServerPlayer player = JimtaRobotGameTests.player(helper);
		BlockPos at = helper.absolutePos(BlockPos.ZERO);
		player.snapTo(at.getX() + 3.5, at.getY() + 1, at.getZ() + 3.5, 0.0F, 0.0F);
		helper.assertFalse(HorsemanSightings.ready(level, player), "Not straight away");
		player.tickCount = HorsemanSightings.SETTLE_TICKS;
		helper.assertTrue(level.canSeeSky(player.blockPosition()) == HorsemanSightings.ready(level, player),
				"After " + HorsemanSightings.SETTLE_TICKS + " ticks, out under the sky, yes");
		player.setGameMode(GameType.SPECTATOR);
		helper.assertFalse(HorsemanSightings.ready(level, player), "Never a spectator");
		helper.succeed();
	}
}
