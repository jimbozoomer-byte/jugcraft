package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jspecify.annotations.Nullable;

/**
 * Mock players for the Concordance's server game tests, logged out once their test is over. The game's own
 * {@code makeMockServerPlayerInLevel} logs a player in for the rest of the run: each one keeps its chunks loaded and its
 * inventory synced, and by Concordance Chunk C the suite's 1,428 left the game test server out of heap (CI run
 * 40003960812). A player made here leaves the game ({@code PlayerList.remove}, as the town and alchemy tests do by hand)
 * when its test passes or fails: the test framework tells its listeners on the tick after the test is done, never during
 * it. A player its test has already logged out (to stand for one who is away) is left alone.
 */
public final class TestPlayers {
	private static final @Nullable Field TEST_INFO = testInfoField();
	private static final Map<GameTestHelper, List<ServerPlayer>> MADE = Collections.synchronizedMap(new WeakHashMap<>());

	private TestPlayers() {
	}

	/** A mock player in the test's level, as {@code makeMockServerPlayerInLevel} makes it, that leaves when the test ends. */
	@SuppressWarnings("removal")
	public static ServerPlayer mock(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		List<ServerPlayer> made = MADE.get(helper);
		if (made == null) {
			made = new ArrayList<>();
			MADE.put(helper, made);
			GameTestInfo info = info(helper);
			if (info != null) {
				List<ServerPlayer> players = made;
				info.addListener(new GameTestListener() {
					@Override
					public void testStructureLoaded(GameTestInfo testInfo) {
					}

					@Override
					public void testPassed(GameTestInfo testInfo, GameTestRunner runner) {
						logOut(players);
					}

					@Override
					public void testFailed(GameTestInfo testInfo, GameTestRunner runner) {
						logOut(players);
					}

					@Override
					public void testAddedForRerun(GameTestInfo testInfo, GameTestInfo rerun, GameTestRunner runner) {
					}
				});
			}
		}
		made.add(player);
		return player;
	}

	private static void logOut(List<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			PlayerList list = player.level().getServer().getPlayerList();
			if (list.getPlayer(player.getUUID()) == player) {
				list.remove(player);
			}
		}
		players.clear();
	}

	private static @Nullable GameTestInfo info(GameTestHelper helper) {
		if (TEST_INFO == null) {
			return null;
		}
		try {
			return (GameTestInfo) TEST_INFO.get(helper);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	/** GameTestHelper keeps its test private; without it a player made here simply stays, as before. */
	private static @Nullable Field testInfoField() {
		try {
			Field field = GameTestHelper.class.getDeclaredField("testInfo");
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException | RuntimeException e) {
			Jugcraft.LOGGER.warn("[test players] GameTestHelper.testInfo not found; mock players will stay logged in", e);
			return null;
		}
	}
}
