package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.GraveyardRising;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.agriculture.HorsemanSummoning;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Graveyard Rising (docs/features/graveyard-rising.md). Times of day are passed in rather than set on the shared world
 * clock, and the season is switched on only inside each step and switched back before it returns. A rising rung up is
 * stopped in the same tick; the waves are run by stepping a rising by hand, and every one of the risen is gone before
 * the test method returns, so none wanders into another test.
 */
public final class GraveyardRisingGameTests {
	private static final long MIDNIGHT = HorsemanSummoning.MIDNIGHT;
	private static final long NOON = 6000;

	private static Block mound() {
		return JugcraftAgriculture.block("grave_mound");
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer player(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos at = helper.absolutePos(new BlockPos(4, 2, 1));
		player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		return player;
	}

	/** Six graves in a row, with room in front of each; returns where they are. */
	private static List<BlockPos> graves(GameTestHelper helper) {
		List<BlockPos> graves = new ArrayList<>();
		for (int x = 1; x <= 6; x++) {
			BlockPos grave = new BlockPos(x, 2, 5);
			helper.setBlock(grave, mound());
			graves.add(helper.absolutePos(grave));
		}
		return graves;
	}

	/** Lays every one of the risen to rest (removes them). */
	private static void clear(ServerLevel level, GraveyardRising.Rising rising) {
		for (UUID id : rising.risen()) {
			Entity mob = level.getEntity(id);
			if (mob != null) {
				mob.discard();
			}
		}
	}

	/** Ringing a bell by a graveyard: nothing out of season, too few graves, by day, then a stir, then the dead rise. */
	@GameTest(maxTicks = 200)
	public void ringingTwiceByAGraveyardWakesTheDead(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper);
		helper.setBlock(new BlockPos(0, 2, 0), Blocks.BELL);
		BlockPos bell = helper.absolutePos(new BlockPos(0, 2, 0));
		HalloweenSeason.setMode(HalloweenSeason.Mode.OFF);
		try {
			helper.assertTrue(GraveyardRising.ring(player, bell, MIDNIGHT) == GraveyardRising.Result.OUT_OF_SEASON, "Nothing out of season");
		} finally {
			HalloweenSeason.reset();
		}
		// Graves until one short of enough (graves of a neighbouring test may be in reach already).
		for (int x = 1; x <= 7 && GraveyardRising.graves(level, bell).size() < GraveyardRising.MIN_GRAVES - 1; x++) {
			helper.setBlock(new BlockPos(x, 2, 6), mound());
		}
		boolean few = GraveyardRising.graves(level, bell).size() == GraveyardRising.MIN_GRAVES - 1;
		HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
		try {
			GraveyardRising.Result first = GraveyardRising.ring(player, bell, MIDNIGHT);
			helper.assertTrue(!few || first == GraveyardRising.Result.TOO_FEW_GRAVES, "Too few graves to wake: " + first);
			helper.assertTrue(GraveyardRising.ring(player, bell, MIDNIGHT) == GraveyardRising.Result.WAIT, "A player looks for graves once a second");
		} finally {
			HalloweenSeason.reset();
		}
		graves(helper);
		helper.runAfterDelay(GraveyardRising.RING_COOLDOWN + 2, () -> {
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			try {
				helper.assertTrue(GraveyardRising.graves(level, bell).size() >= GraveyardRising.MIN_GRAVES, "Enough graves now");
				helper.assertTrue(GraveyardRising.ring(player, bell, NOON) == GraveyardRising.Result.DAYTIME, "The dead sleep by day");
			} finally {
				HalloweenSeason.reset();
			}
		});
		helper.runAfterDelay(2L * (GraveyardRising.RING_COOLDOWN + 2), () -> {
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			try {
				helper.assertTrue(GraveyardRising.ring(player, bell, MIDNIGHT) == GraveyardRising.Result.STIRRED, "The first ring stirs the graves");
			} finally {
				HalloweenSeason.reset();
			}
		});
		helper.runAfterDelay(3L * (GraveyardRising.RING_COOLDOWN + 2), () -> {
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			try {
				GraveyardRising.Result result = GraveyardRising.ring(player, bell, MIDNIGHT);
				GraveyardRising.Rising rising = GraveyardRising.risings().stream().filter(r -> r.bell().equals(bell)).findFirst().orElse(null);
				if (rising != null) {
					rising.stop(level);
				}
				helper.assertTrue(result == GraveyardRising.Result.RISEN, "The second ring wakes the dead: " + result);
				helper.assertTrue(rising != null && rising.risen().isEmpty(), "A rising is under way at the bell, and stops cleanly");
			} finally {
				HalloweenSeason.reset();
			}
		});
		helper.runAfterDelay(4L * (GraveyardRising.RING_COOLDOWN + 2), () -> {
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			try {
				helper.assertTrue(GraveyardRising.ring(player, bell, MIDNIGHT) == GraveyardRising.Result.ALREADY_RISEN, "A graveyard rises once a night");
			} finally {
				HalloweenSeason.reset();
			}
			helper.succeed();
		});
	}

	/** The three waves climb out of the graves, archers with bows; clear them and the party is rewarded once a night. */
	@GameTest
	public void wavesRiseAndTheGraveyardRests(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper);
		List<BlockPos> graves = graves(helper);
		BlockPos bell = helper.absolutePos(new BlockPos(0, 2, 0));
		long now = level.getGameTime();
		GraveyardRising.Rising rising = GraveyardRising.begin(level, bell, graves, now, MIDNIGHT);
		try {
			helper.assertTrue(rising.step(level, now, MIDNIGHT) == GraveyardRising.State.RISING && rising.risen().isEmpty(),
					"Nothing climbs out at once");
			long at = now + GraveyardRising.FIRST_WAVE_TICKS;
			for (int wave = 0; wave < GraveyardRising.WAVES.size(); wave++) {
				helper.assertTrue(rising.step(level, at, MIDNIGHT) == GraveyardRising.State.RISING, "Still rising at wave " + (wave + 1));
				List<UUID> risen = rising.risen();
				int least = GraveyardRising.count(wave, 1);
				helper.assertTrue(risen.size() >= least && risen.size() <= least + GraveyardRising.MAX_EXTRA,
						"Wave " + (wave + 1) + " brings " + least + " (more with more players): " + risen.size());
				for (UUID id : risen) {
					Entity mob = level.getEntity(id);
					helper.assertTrue(mob != null && mob.entityTags().contains(GraveyardRising.TAG), "Each of the risen is marked");
					helper.assertTrue(graves.stream().anyMatch(grave -> grave.closerThan(mob.blockPosition(), 2.5)), "and climbs out beside a grave");
					if (mob.getType() == EntityTypes.SKELETON || mob.getType() == EntityTypes.STRAY) {
						helper.assertTrue(((net.minecraft.world.entity.Mob) mob).getItemBySlot(EquipmentSlot.MAINHAND).is(Items.BOW), "Archers carry bows");
					}
				}
				helper.assertTrue(wave == 0 || risen.stream().anyMatch(id -> level.getEntity(id).getType() == EntityTypes.SKELETON),
						"Skeletons rise from the second wave");
				clear(level, rising);
				at += 1;
				GraveyardRising.State state = rising.step(level, at, MIDNIGHT);
				if (wave + 1 < GraveyardRising.WAVES.size()) {
					helper.assertTrue(state == GraveyardRising.State.RISING && rising.wave() == wave + 1, "On to the next wave");
					helper.assertTrue(rising.step(level, at + GraveyardRising.WAVE_GAP_TICKS - 1, MIDNIGHT) == GraveyardRising.State.RISING
							&& rising.risen().isEmpty(), "A pause between waves");
					at += GraveyardRising.WAVE_GAP_TICKS;
				} else {
					helper.assertTrue(state == GraveyardRising.State.WON, "The last wave cleared, the graveyard rests");
				}
			}
			long night = GraveyardRising.tonight(MIDNIGHT);
			helper.assertTrue(GraveyardRising.rested(player) == night, "The player is rewarded for tonight");
			int items = player.getInventory().getContainerSize() - (int) java.util.stream.IntStream.range(0, player.getInventory().getContainerSize())
					.filter(slot -> player.getInventory().getItem(slot).isEmpty()).count();
			helper.assertTrue(items > 0, "The graves' thanks are in the player's inventory");
			AdvancementHolder shift = level.getServer().getAdvancements().get(Jugcraft.id(GraveyardRising.ADVANCEMENT));
			helper.assertTrue(shift != null && player.getAdvancements().getOrStartProgress(shift).isDone(), "and Graveyard Shift is earned");

			// A second rising the same night rewards nobody twice.
			GraveyardRising.Rising again = GraveyardRising.begin(level, bell, graves, at, MIDNIGHT);
			long t = at + GraveyardRising.FIRST_WAVE_TICKS;
			GraveyardRising.State state = GraveyardRising.State.RISING;
			for (int step = 0; step < 3 * GraveyardRising.WAVES.size() && state == GraveyardRising.State.RISING; step++) {
				state = again.step(level, t, MIDNIGHT);
				clear(level, again);
				t += GraveyardRising.WAVE_GAP_TICKS;
			}
			int after = player.getInventory().getContainerSize() - (int) java.util.stream.IntStream.range(0, player.getInventory().getContainerSize())
					.filter(slot -> player.getInventory().getItem(slot).isEmpty()).count();
			helper.assertTrue(state == GraveyardRising.State.WON && after == items, "Won again, but no second reward tonight");
		} finally {
			clear(level, rising);
			rising.stop(level);
		}
		helper.succeed();
	}

	/** Dawn sends the dead back, and so do graves with no room to climb out of. */
	@GameTest
	public void theDeadSinkBackAtDawnOrWhenTheGravesAreBlocked(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		player(helper);
		List<BlockPos> graves = graves(helper);
		BlockPos bell = helper.absolutePos(new BlockPos(0, 2, 0));
		long now = level.getGameTime();
		GraveyardRising.Rising rising = GraveyardRising.begin(level, bell, graves, now, MIDNIGHT);
		try {
			rising.step(level, now + GraveyardRising.FIRST_WAVE_TICKS, MIDNIGHT);
			List<UUID> risen = rising.risen();
			helper.assertTrue(!risen.isEmpty(), "The first wave climbs out");
			helper.assertTrue(rising.step(level, now + GraveyardRising.FIRST_WAVE_TICKS + 1, NOON) == GraveyardRising.State.FAILED,
					"Dawn ends the rising");
			helper.assertTrue(risen.stream().allMatch(id -> level.getEntity(id) == null) && rising.risen().isEmpty(),
					"and every one of the risen sinks back");
		} finally {
			clear(level, rising);
			rising.stop(level);
		}

		// Graves walled in, with no room to climb out: a wave cannot rise, and the rising fails.
		for (BlockPos stone : BlockPos.betweenClosed(new BlockPos(2, 3, 2), new BlockPos(4, 7, 4))) {
			helper.setBlock(stone, Blocks.STONE);
		}
		List<BlockPos> buried = List.of(helper.absolutePos(new BlockPos(3, 5, 3)));
		GraveyardRising.Rising blocked = GraveyardRising.begin(level, bell, buried, now, MIDNIGHT);
		try {
			helper.assertTrue(blocked.step(level, now + GraveyardRising.FIRST_WAVE_TICKS, MIDNIGHT) == GraveyardRising.State.FAILED,
					"Graves with no room fail the rising");
		} finally {
			clear(level, blocked);
			blocked.stop(level);
		}
		helper.succeed();
	}
}
