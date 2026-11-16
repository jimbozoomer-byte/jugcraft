package io.github.jimbozoomer.jugcraft.test;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.block.Rotation;

/**
 * Real mock-player joins with isolated completion listeners; no dependence on test order or a later batch. Since
 * 12 November 2026 also the fixtures that join players themselves, and absent fake players' trackers
 * (docs/features/server-load-benchmark.md, "Full-suite memory").
 */
@SuppressWarnings("removal")
public class TestPlayersGameTests {
	@GameTest(maxTicks = 30)
	public void rawHelperPlayersStayUntilTheirTestCompletes(GameTestHelper helper) {
		Scope scope = scope(helper);
		ServerPlayer player = scope.helper().makeMockServerPlayerInLevel();
		ServerPlayer other = helper.makeMockServerPlayerInLevel();
		helper.assertTrue(online(player) && online(other), "Both test scopes have online players");
		helper.runAfterDelay(3, () -> {
			try {
				helper.assertTrue(online(player), "The raw helper's player survives while its test is active");
				scope.complete(true);
				helper.assertTrue(!online(player), "A passed test logs out its raw-helper player");
				helper.assertTrue(helper.getLevel().getPlayerByUUID(player.getUUID()) == null,
						"Completed fixture no longer remains in the world player list");
				helper.assertTrue(online(other), "Completing one test does not log out another test's player");
				helper.succeed();
			} finally {
				scope.complete(false);
			}
		});
	}

	@GameTest(maxTicks = 20)
	public void wrapperRegistrationAndFailureCleanupAreIdempotent(GameTestHelper helper) {
		Scope scope = scope(helper);
		try {
			ServerPlayer player = TestPlayers.mock(scope.helper());
			TestPlayers.track(scope.helper(), player);
			TestPlayers.track(scope.helper(), player);
			helper.assertTrue(tracked(scope.helper()) == 1, "Wrapper plus raw-helper hook retains one reference per player");
			helper.assertTrue(scope.info().getListeners().count() == 1, "One test has one cleanup listener");
			scope.complete(false);
			helper.assertTrue(!online(player), "A failed test also logs out its player");
			helper.assertTrue(tracked(scope.helper()) == 0, "Completion releases tracked player references");
			scope.complete(false);
			scope.complete(true);
			helper.assertTrue(!online(player), "Repeated completion cannot restore or remove a player twice");
			helper.succeed();
		} finally {
			scope.complete(false);
		}
	}

	@GameTest(maxTicks = 20)
	public void oldFixtureCleanupDoesNotDisconnectRejoinedIdentity(GameTestHelper helper) {
		Scope oldScope = scope(helper);
		Scope newScope = scope(helper);
		try {
			ServerPlayer old = TestPlayers.survivor(oldScope.helper());
			TestPlayers.play(old, 123);
			TestPlayers.leave(old);
			ServerPlayer rejoined = TestPlayers.again(newScope.helper(), old.getUUID());
			helper.assertTrue(old != rejoined && online(rejoined), "The same UUID rejoins as a new live player");
			helper.assertTrue(TestPlayers.played(rejoined) == 123, "Leaving/rejoining preserves the fixture's saved statistics");
			oldScope.complete(true);
			helper.assertTrue(online(rejoined), "Old fixture cleanup checks object identity before removing the UUID");
			newScope.complete(false);
			helper.assertTrue(!online(rejoined), "The rejoined survivor still belongs to its new test's cleanup");
			helper.succeed();
		} finally {
			oldScope.complete(false);
			newScope.complete(false);
		}
	}

	@GameTest(maxTicks = 20)
	public void immediateCompletionClosesAndReleasesMockTransport(GameTestHelper helper) {
		Scope scope = scope(helper);
		ServerPlayer player = scope.helper().makeMockServerPlayerInLevel();
		try {
			Field channelField = MockPlayerChannels.class.getDeclaredField("CHANNEL");
			Field connectionField = MockPlayerChannels.class.getDeclaredField("CONNECTION");
			channelField.setAccessible(true);
			connectionField.setAccessible(true);
			Object connection = ((Field) connectionField.get(null)).get(player.connection);
			io.netty.channel.embedded.EmbeddedChannel channel =
					(io.netty.channel.embedded.EmbeddedChannel) ((Field) channelField.get(null)).get(connection);
			io.netty.buffer.ByteBuf queued = io.netty.buffer.Unpooled.buffer().writeByte(1);
			channel.outboundMessages().add(queued);
			helper.assertTrue(channel.isOpen() && !channel.outboundMessages().isEmpty(), "Mock has queued login data before any tick");
			scope.complete(true);
			helper.assertTrue(!channel.isOpen() && channel.outboundMessages().isEmpty() && channel.inboundMessages().isEmpty(),
					"Immediate completion closes and empties the fake transport");
			helper.assertTrue(queued.refCnt() == 0, "Reference-counted queued data is released");
			scope.complete(false);
			helper.succeed();
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Cannot inspect mock transport", e);
		} finally {
			scope.complete(false);
		}
	}

	@GameTest(maxTicks = 20)
	public void nonMockPlayersAreNotRegistered(GameTestHelper helper) {
		Scope scope = scope(helper);
		ServerPlayer ordinary = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "not-a-mock"), ClientInformation.createDefault());
		helper.assertTrue(TestPlayers.track(scope.helper(), ordinary) == ordinary, "Tracking preserves the returned player");
		helper.assertTrue(scope.info().getListeners().count() == 0 && tracked(scope.helper()) == 0,
				"No embedded mock connection means no fixture cleanup ownership");
		helper.succeed();
	}

	/**
	 * Fixtures that join their own players instead of through TestPlayers.mock (here the Jimta robot owner and the
	 * connected alchemist, which about a hundred tests share) leave with their test, and their transports are closed so
	 * Fabric ends their network sessions. Before, they stayed in the game for the rest of the run.
	 */
	@GameTest(maxTicks = 20)
	public void directJoinFixturesLeaveWithTheirTest(GameTestHelper helper) {
		Scope scope = scope(helper);
		try {
			ServerPlayer robotOwner = JimtaRobotGameTests.player(scope.helper());
			ServerPlayer alchemist = ConnectedAlchemyGameTests.player(scope.helper());
			helper.assertTrue(online(robotOwner) && online(alchemist), "Both direct-join fixtures are in the game during their test");
			helper.assertTrue(tracked(scope.helper()) == 2, "Both belong to their test's cleanup: " + tracked(scope.helper()));
			scope.complete(true);
			helper.assertTrue(!online(robotOwner) && !online(alchemist), "Both leave when their test ends");
			helper.assertTrue(!MockPlayerChannels.transportOpen(robotOwner) && !MockPlayerChannels.transportOpen(alchemist),
					"Their fake transports are closed");
			helper.succeed();
		} finally {
			scope.complete(false);
		}
	}

	/** A fixture that logs its player out by hand (as the robot and alchemy tests do) still has the transport closed at the end. */
	@GameTest(maxTicks = 20)
	public void handRemovedFixturePlayersAreStillDisposed(GameTestHelper helper) {
		Scope scope = scope(helper);
		try {
			ServerPlayer player = JimtaRobotGameTests.player(scope.helper());
			helper.getLevel().getServer().getPlayerList().remove(player);
			helper.assertTrue(!online(player) && MockPlayerChannels.transportOpen(player), "Logged out by hand, its transport is still open");
			scope.complete(true);
			helper.assertTrue(!MockPlayerChannels.transportOpen(player), "Its test's end closes it");
			helper.succeed();
		} finally {
			scope.complete(false);
		}
	}

	/**
	 * A fake player for an identity that is not in the game (a stand-in for an absent owner) leaves an advancement tracker
	 * in the player list; the sweep lets it go. A stand-in for a present owner shares the owner's, which stays.
	 */
	@GameTest(maxTicks = 20)
	public void absentStandInTrackersAreReleased(GameTestHelper helper) {
		Scope scope = scope(helper);
		try {
			MinecraftServer server = helper.getLevel().getServer();
			PlayerList list = server.getPlayerList();
			ServerPlayer owner = TestPlayers.mock(scope.helper());
			FakePlayer present = FakePlayer.get(helper.getLevel(), new GameProfile(owner.getUUID(), "[Present stand-in]"));
			UUID away = UUID.randomUUID();
			FakePlayer absent = FakePlayer.get(helper.getLevel(), new GameProfile(away, "[Absent stand-in]"));
			Map<?, ?> trackers = MockPlayerChannels.trackers(list);
			helper.assertTrue(trackers != null, "The player list keeps its trackers by identity");
			helper.assertTrue(present.getAdvancements() == owner.getAdvancements(), "A present owner's stand-in shares the owner's tracker");
			helper.assertTrue(trackers.get(away) == absent.getAdvancements(), "The absent identity's stand-in left a tracker behind");
			int released = MockPlayerChannels.releaseAbsentStandIns(server);
			helper.assertTrue(released >= 1 && !trackers.containsKey(away), "The absent stand-in's tracker is let go (" + released + ")");
			helper.assertTrue(trackers.get(owner.getUUID()) == owner.getAdvancements() && online(owner), "The present owner keeps theirs");
			helper.assertTrue(absent.getAdvancements() != null, "The fake player still has its own tracker to ask");
			helper.succeed();
		} finally {
			scope.complete(false);
		}
	}

	private static boolean online(ServerPlayer player) {
		return player.level().getServer().getPlayerList().getPlayer(player.getUUID()) == player;
	}

	private static Scope scope(GameTestHelper parent) {
		try {
			Field field = GameTestHelper.class.getDeclaredField("testInfo");
			field.setAccessible(true);
			GameTestInfo source = (GameTestInfo) field.get(parent);
			GameTestInfo info = new GameTestInfo(source.getTestHolder(), Rotation.NONE, parent.getLevel(), RetryOptions.noRetries()) {
				@Override
				public BlockPos getTestOrigin() {
					return parent.absolutePos(BlockPos.ZERO);
				}
			};
			return new Scope(info, new GameTestHelper(info));
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Cannot create isolated fixture lifetime", e);
		}
	}

	private static int tracked(GameTestHelper helper) {
		try {
			Field field = TestPlayers.class.getDeclaredField("MADE");
			field.setAccessible(true);
			Map<?, ?> made = (Map<?, ?>) field.get(null);
			Object players = made.get(helper);
			return players instanceof List<?> list ? list.size() : 0;
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Cannot inspect retained fixture references", e);
		}
	}

	private record Scope(GameTestInfo info, GameTestHelper helper) {
		/** Dispatch the framework's completion notification without deliberately failing the outer regression test. */
		void complete(boolean passed) {
			info.getListeners().forEach(listener -> {
				if (passed) {
					listener.testPassed(info, null);
				} else {
					listener.testFailed(info, null);
				}
			});
		}
	}
}
