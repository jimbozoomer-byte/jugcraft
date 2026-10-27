package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.creatures.herobrine.Herobrine;
import io.github.jimbozoomer.jugcraft.creatures.herobrine.HerobrineDirector;
import io.github.jimbozoomer.jugcraft.creatures.herobrine.HerobrineEncounters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Herobrine's encounters (docs/features/herobrine.md): he goes when someone comes close, stares him down or hits him,
 * and hurts nobody; a close encounter only darkens the screen; he is never saved; only attentive players who really see
 * him are reported as witnesses; a player has one encounter at a time, and their first close one must follow a distant
 * one; a distant encounter is placed where the player could see him, or reported as failed with nothing placed; a
 * follower walks the player's own path and stops when they stop; a second look finds him gone or moved; a tunnel's
 * eyes wait at the far end of a tunnel behind the player; and a shared sighting credits everyone who really saw him.
 * The schedule is not tested here: it is a separate {@link HerobrineDirector}, and these tests listen through one that
 * passes everything on to whichever director was installed.
 */
public class HerobrineGameTests {
	private static final String ARENA = "jugcraft-test:tatterlace_arena";

	/** What the director heard about one encounter or request. */
	private record Event(String what, UUID player, UUID encounter, Object detail) {
	}

	/** Records every report, and passes everything on to the director that was installed before it. */
	private static final class Recorder implements HerobrineDirector {
		private final HerobrineDirector next;
		private final List<Event> events = new ArrayList<>();

		private Recorder(HerobrineDirector next) {
			this.next = next;
		}

		@Override
		public List<Request> due(MinecraftServer server, List<Candidate> candidates) {
			return next.due(server, candidates);
		}

		@Override
		public void placed(Request request, UUID encounter) {
			events.add(new Event("placed", request.player(), encounter, request.kind()));
			next.placed(request, encounter);
		}

		@Override
		public void failed(Request request, Failure failure) {
			events.add(new Event("failed", request.player(), null, failure));
			next.failed(request, failure);
		}

		@Override
		public void seen(Request request, UUID encounter, UUID player) {
			events.add(new Event("seen", request.player(), encounter, player));
			next.seen(request, encounter, player);
		}

		@Override
		public void ended(Request request, UUID encounter, Herobrine.Outcome outcome, Set<UUID> witnesses) {
			events.add(new Event("ended", request.player(), encounter, List.of(outcome, Set.copyOf(witnesses))));
			next.ended(request, encounter, outcome, witnesses);
		}

		List<Event> about(UUID encounter) {
			return events.stream().filter(e -> encounter.equals(e.encounter())).toList();
		}

		List<Event> failuresFor(UUID player) {
			return events.stream().filter(e -> e.what().equals("failed") && e.player().equals(player)).toList();
		}
	}

	private static Recorder recorder() {
		if (!(HerobrineEncounters.director() instanceof Recorder recorder)) {
			recorder = new Recorder(HerobrineEncounters.director());
			HerobrineEncounters.setDirector(recorder);
		}
		return recorder;
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 48; x++) {
			for (int z = 0; z < 48; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	/** A mock player standing at {@code at} (relative), facing {@code yaw} (0 is south, +z), looking level. */
	private static ServerPlayer player(GameTestHelper helper, BlockPos at, float yaw) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		BlockPos pos = helper.absolutePos(at);
		player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
		player.setYHeadRot(yaw);
		return player;
	}

	private static Herobrine place(GameTestHelper helper, ServerPlayer player, BlockPos at, Herobrine.Kind kind) {
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attemptAt(player,
				new HerobrineDirector.Request(player.getUUID(), kind, HerobrineDirector.FORCED), Vec3.atBottomCenterOf(helper.absolutePos(at)));
		helper.assertTrue(attempt.placed(), "He could not be placed: " + attempt.failure());
		return attempt.herobrine();
	}

	private static Object ending(Recorder recorder, Herobrine herobrine) {
		return recorder.about(herobrine.encounter()).stream().filter(e -> e.what().equals("ended")).map(Event::detail)
				.findFirst().orElse(null);
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 120)
	public void heVanishesWhenSomeoneComesClose(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 4), 180.0F);
		Herobrine herobrine = place(helper, player, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(herobrine.isAlive(), "He should still stand while nobody is near");
			BlockPos near = helper.absolutePos(new BlockPos(24, 1, 32));
			player.snapTo(near.getX() + 0.5, near.getY(), near.getZ() + 0.5, 180.0F, 0.0F);
			helper.runAfterDelay(5, () -> {
				helper.assertTrue(herobrine.isRemoved(), "He should be gone once someone is within " + Herobrine.VANISH_DISTANCE + " blocks");
				helper.assertTrue(List.of(Herobrine.Outcome.APPROACHED, Set.of()).equals(ending(recorder, herobrine)),
						"The encounter should end approached and unseen, not " + ending(recorder, herobrine));
				helper.succeed();
			});
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 120)
	public void staringHimDownEndsTheSightingAndCountsAsSeen(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 4), 0.0F);
		Herobrine herobrine = place(helper, player, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		helper.runAfterDelay(Herobrine.STARE_TICKS + 10, () -> {
			helper.assertTrue(herobrine.isRemoved(), "He should go once stared at for " + Herobrine.STARE_TICKS + " ticks");
			helper.assertTrue(List.of(Herobrine.Outcome.STARED_DOWN, Set.of(player.getUUID())).equals(ending(recorder, herobrine)),
					"The encounter should end stared down, seen by the player, not " + ending(recorder, herobrine));
			helper.assertTrue(recorder.about(herobrine.encounter()).stream().filter(e -> e.what().equals("seen")).count() == 1,
					"The player should be reported as seeing him once");
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 60)
	public void aBlowMakesHimVanishAndHurtsNobody(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 20), 0.0F);
		Herobrine herobrine = place(helper, player, new BlockPos(24, 1, 22), Herobrine.Kind.CLOSE);
		helper.assertFalse(herobrine.hurtServer(level, level.damageSources().playerAttack(player), 10.0F), "A blow should not land");
		helper.assertTrue(herobrine.isRemoved(), "He should be gone when struck");
		helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Nobody should be hurt");
		helper.assertTrue(List.of(Herobrine.Outcome.HIT, Set.of(player.getUUID())).equals(ending(recorder, herobrine)),
				"Whoever struck him saw him: " + ending(recorder, herobrine));
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = Herobrine.CLOSE_TICKS + 40)
	public void aCloseEncounterDarkensButNeverHurtsAndUnseenIsNotCredited(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 24), 0.0F);
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attempt(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.CLOSE, HerobrineDirector.FORCED));
		helper.assertTrue(attempt.placed(), "A forced close encounter should be placed: " + attempt.failure());
		Herobrine herobrine = attempt.herobrine();
		helper.assertTrue(herobrine.distanceTo(player) < 2.5 && herobrine.getZ() < player.getZ(), "He should stand right behind the player");
		helper.assertTrue(player.hasEffect(MobEffects.DARKNESS), "The player should be given a moment of Darkness");
		helper.runAfterDelay(Herobrine.CLOSE_TICKS + 10, () -> {
			helper.assertTrue(herobrine.isRemoved(), "He should be gone after " + Herobrine.CLOSE_TICKS + " ticks");
			helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Nobody should be hurt");
			helper.assertTrue(List.of(Herobrine.Outcome.EXPIRED, Set.of()).equals(ending(recorder, herobrine)),
					"Behind a player who never turned round, he should go unseen: " + ending(recorder, herobrine));
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 40)
	public void aCloseEncounterMustFollowADistantOneAndOneAtATime(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 4), 180.0F);
		HerobrineEncounters.Attempt close = HerobrineEncounters.attempt(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.CLOSE, 7L));
		helper.assertTrue(!close.placed() && close.failure() == HerobrineDirector.Failure.TARGET_UNSAFE,
				"A first encounter should never be a close one: " + close.failure());
		Herobrine herobrine = place(helper, player, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		HerobrineEncounters.Attempt second = HerobrineEncounters.attempt(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.DISTANT, 8L));
		helper.assertTrue(!second.placed() && second.failure() == HerobrineDirector.Failure.ALREADY_HAUNTED,
				"A second encounter at once should fail: " + second.failure());
		helper.assertTrue(recorder.failuresFor(player.getUUID()).size() == 2, "Both failures should be reported to the director");
		herobrine.discard();
		helper.assertTrue(List.of(Herobrine.Outcome.REMOVED, Set.of()).equals(ending(recorder, herobrine)),
				"Removing him should end the encounter: " + ending(recorder, herobrine));
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 80)
	public void onlyAttentiveWitnessesAreCredited(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer target = player(helper, new BlockPos(20, 1, 4), 0.0F);
		ServerPlayer hurt = player(helper, new BlockPos(28, 1, 4), 0.0F);
		hurt.setHealth(4.0F);
		Herobrine herobrine = place(helper, target, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		helper.runAfterDelay(Herobrine.SEEN_TICKS + 10, () -> {
			helper.assertTrue(herobrine.witnesses().equals(Set.of(target.getUUID())),
					"Only the attentive player should have seen him, not " + herobrine.witnesses());
			helper.assertTrue(!HerobrineEncounters.attentive(hurt, helper.getLevel().getServer().getTickCount()),
					"A badly hurt player is not attentive");
			herobrine.discard();
			helper.assertTrue(recorder.about(herobrine.encounter()).stream().noneMatch(e -> e.what().equals("seen")
					&& hurt.getUUID().equals(e.detail())), "The hurt player should never be reported as seeing him");
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void aSightingStandsWhereThePlayerCouldSeeHim(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 6), 0.0F);
		Vec3 spot = HerobrineEncounters.spot(level, player, 14, 20, false);
		helper.assertTrue(spot != null, "There should be a place to stand in an open arena");
		double distance = Math.sqrt(Math.pow(spot.x - player.getX(), 2) + Math.pow(spot.z - player.getZ(), 2));
		helper.assertTrue(distance >= 13.0 && distance <= 21.5, "He should stand 14 to 20 blocks away, not " + distance);
		helper.assertTrue(spot.z > player.getZ(), "He should stand ahead of the player, not behind");
		helper.assertTrue(HerobrineEncounters.standable(level, BlockPos.containing(spot)), "He should stand on solid ground with room");
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void aWalledInPlayerGetsAFailureAndNothingIsPlaced(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		for (int x = 22; x <= 26; x++) {
			for (int y = 1; y <= 4; y++) {
				for (int z = 22; z <= 26; z++) {
					boolean inside = x > 22 && x < 26 && z > 22 && z < 26 && y < 4;
					helper.setBlock(new BlockPos(x, y, z), inside ? Blocks.AIR : Blocks.STONE);
				}
			}
		}
		ServerPlayer player = player(helper, new BlockPos(24, 1, 24), 0.0F);
		int before = HerobrineEncounters.live();
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attempt(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.DISTANT, 9L));
		helper.assertTrue(!attempt.placed() && attempt.failure() == HerobrineDirector.Failure.NO_VISIBLE_SPOT,
				"A walled-in player cannot see him anywhere: " + attempt.failure());
		helper.assertTrue(HerobrineEncounters.live() == before, "Nothing should be placed");
		helper.assertTrue(recorder.failuresFor(player.getUUID()).size() == 1, "The failure should be reported to the director");
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void heIsNeverSavedAndMockPlayersAreNotActive(GameTestHelper helper) {
		helper.assertTrue(!HerobrineEncounters.HEROBRINE.canSerialize(), "Herobrine should never be saved with the world");
		ServerPlayer player = player(helper, new BlockPos(24, 1, 24), 0.0F);
		helper.assertTrue(!HerobrineEncounters.active(player, helper.getLevel().getServer().getTickCount()),
				"A player who has not been in the world a minute is not active");
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 60)
	public void aFollowerWalksThePlayersPathAndStopsWhenTheyStop(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = player(helper, new BlockPos(24, 1, 30), 0.0F);
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attemptVariant(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.DISTANT, HerobrineDirector.FORCED), Herobrine.Variant.FOLLOWER);
		helper.assertTrue(attempt.placed(), "A follower should find a place behind the player: " + attempt.failure());
		Herobrine herobrine = attempt.herobrine();
		Vec3 start = herobrine.position();
		double keep = start.distanceTo(player.position());
		helper.assertTrue(start.z < player.getZ() && keep >= HerobrineEncounters.FOLLOW_NEAR - 1.0, "He should start behind, not at " + start);
		for (int tick = 1; tick <= 16; tick++) {
			double z = player.getZ() + tick * 0.5;
			helper.runAfterDelay(tick, () -> player.snapTo(player.getX(), player.getY(), z, 0.0F, 0.0F));
		}
		helper.runAfterDelay(22, () -> {
			Vec3 now = herobrine.position();
			helper.assertTrue(herobrine.isAlive() && now.z > start.z + 4.0, "He should have followed, not stayed at " + now);
			helper.assertTrue(Math.abs(now.distanceTo(player.position()) - keep) < 1.5, "He should keep his distance, "
					+ keep + ", not " + now.distanceTo(player.position()));
			helper.runAfterDelay(15, () -> {
				helper.assertTrue(herobrine.position().distanceTo(now) < 0.01, "He should stop when the player stops");
				herobrine.discard();
				helper.succeed();
			});
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 80)
	public void aSecondLookFindsHimGoneOrMoved(GameTestHelper helper) {
		floor(helper);
		Recorder recorder = recorder();
		ServerPlayer player = player(helper, new BlockPos(24, 1, 4), 0.0F);
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attemptAt(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.DISTANT, HerobrineDirector.FORCED),
				Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(24, 1, 30))), Herobrine.Variant.SECOND_LOOK);
		helper.assertTrue(attempt.placed(), "He could not be placed: " + attempt.failure());
		Herobrine herobrine = attempt.herobrine();
		Vec3 first = herobrine.position();
		helper.runAfterDelay(Herobrine.SEEN_TICKS + 4, () -> {
			helper.assertTrue(herobrine.witnesses().contains(player.getUUID()), "The player should have seen him first");
			player.snapTo(player.getX(), player.getY(), player.getZ(), 180.0F, 0.0F);
			player.setYHeadRot(180.0F);
			helper.runAfterDelay(Herobrine.LOOK_AWAY_TICKS + 5, () -> {
				helper.assertTrue(herobrine.isRemoved() || herobrine.position().distanceTo(first) >= 6.0,
						"Looking back, he should be gone or somewhere else, not still at " + herobrine.position());
				herobrine.discard();
				helper.assertTrue(recorder.about(herobrine.encounter()).stream().anyMatch(e -> e.what().equals("ended")),
						"The encounter should have ended");
				helper.succeed();
			});
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void tunnelEyesWaitAtTheFarEndOfATunnelBehind(GameTestHelper helper) {
		floor(helper);
		for (int z = 1; z <= 31; z++) {
			for (int y = 1; y <= 3; y++) {
				for (int x = 23; x <= 25; x++) {
					boolean inside = x == 24 && y < 3 && z > 1;
					helper.setBlock(new BlockPos(x, y, z), inside ? Blocks.AIR : Blocks.STONE);
				}
			}
		}
		ServerPlayer player = player(helper, new BlockPos(24, 1, 30), 0.0F);
		HerobrineEncounters.Attempt attempt = HerobrineEncounters.attemptVariant(player,
				new HerobrineDirector.Request(player.getUUID(), Herobrine.Kind.DISTANT, HerobrineDirector.FORCED), Herobrine.Variant.TUNNEL);
		helper.assertTrue(attempt.placed(), "He should wait down the tunnel: " + attempt.failure());
		Herobrine herobrine = attempt.herobrine();
		BlockPos end = helper.absolutePos(new BlockPos(24, 1, 2));
		helper.assertTrue(herobrine.blockPosition().equals(end), "He should stand at the tunnel's far end, " + end + ", not "
				+ herobrine.blockPosition());
		herobrine.discard();
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 60)
	public void aSharedSightingCreditsEveryoneWhoSawHim(GameTestHelper helper) {
		floor(helper);
		ServerPlayer target = player(helper, new BlockPos(20, 1, 4), 0.0F);
		ServerPlayer friend = player(helper, new BlockPos(28, 1, 4), 0.0F);
		ServerPlayer elsewhere = player(helper, new BlockPos(24, 1, 8), 180.0F);
		Herobrine herobrine = place(helper, target, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		helper.runAfterDelay(Herobrine.SEEN_TICKS + 10, () -> {
			helper.assertTrue(herobrine.witnesses().equals(Set.of(target.getUUID(), friend.getUUID())),
					"Both players who saw him should be witnesses, and the one looking away should not: " + herobrine.witnesses());
			helper.assertTrue(!herobrine.witnesses().contains(elsewhere.getUUID()), "Facing away is not seeing");
			herobrine.discard();
			helper.succeed();
		});
	}

	/** Regression: a player's count of ticks seeing him restarts whenever they look away, so glimpses never add up. */
	@GameTest(structure = ARENA, padding = 48, maxTicks = 80)
	public void scatteredGlimpsesDoNotAddUpToASighting(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = player(helper, new BlockPos(24, 1, 4), 0.0F);
		Herobrine herobrine = place(helper, player, new BlockPos(24, 1, 40), Herobrine.Kind.DISTANT);
		// Three rounds of 6 ticks looking and 3 looking away: 18 ticks of glimpses, never 10 in a row.
		for (int tick = 1; tick <= 27; tick++) {
			float yaw = (tick - 1) % 9 < 6 ? 0.0F : 180.0F;
			helper.runAfterDelay(tick, () -> {
				player.snapTo(player.getX(), player.getY(), player.getZ(), yaw, 0.0F);
				player.setYHeadRot(yaw);
			});
		}
		helper.runAfterDelay(29, () -> {
			helper.assertTrue(herobrine.isAlive() && herobrine.witnesses().isEmpty(),
					"Scattered glimpses should not count as seeing him: " + herobrine.witnesses());
			player.snapTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
			player.setYHeadRot(0.0F);
			helper.runAfterDelay(Herobrine.SEEN_TICKS + 3, () -> {
				helper.assertTrue(herobrine.witnesses().contains(player.getUUID()), "A steady look should count as seeing him");
				herobrine.discard();
				helper.succeed();
			});
		});
	}

	/** Regression: a line of sight is only cast over loaded chunks, and checking one never loads a chunk. */
	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void aLineOfSightNeverLoadsAChunk(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Vec3 here = helper.absoluteVec(new Vec3(4.5, 2.0, 4.5));
		Vec3 across = helper.absoluteVec(new Vec3(43.5, 2.0, 43.5));
		helper.assertTrue(HerobrineEncounters.corridorLoaded(level, here, across), "A line across the loaded arena should be clear to cast");
		Vec3 far = here.add(30000.0, 0.0, 70.0);
		BlockPos farChunk = BlockPos.containing(far);
		helper.assertTrue(!level.hasChunkAt(farChunk), "The far point should start unloaded");
		helper.assertTrue(!HerobrineEncounters.corridorLoaded(level, here, far), "A line into unloaded land should not be cast");
		helper.assertTrue(!level.hasChunkAt(farChunk), "Checking the line should not have loaded the far chunk");
		helper.succeed();
	}
}
