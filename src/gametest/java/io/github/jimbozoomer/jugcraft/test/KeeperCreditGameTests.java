package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import io.github.jimbozoomer.jugcraft.concordance.effect.Cause;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectKind;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import io.github.jimbozoomer.jugcraft.concordance.effect.Intent;
import io.github.jimbozoomer.jugcraft.concordance.effect.Ledger;
import io.github.jimbozoomer.jugcraft.lair.CryptArchive;
import io.github.jimbozoomer.jugcraft.lair.CydonianSanctum;
import io.github.jimbozoomer.jugcraft.lair.Encounter;
import io.github.jimbozoomer.jugcraft.lair.FoundryTrench;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.MoonlitRing;
import io.github.jimbozoomer.jugcraft.lair.alpha.AlphaWerewolfEntity;
import io.github.jimbozoomer.jugcraft.lair.alpha.AlphaWerewolfLoot;
import io.github.jimbozoomer.jugcraft.lair.dreadnought.IronDreadnoughtEntity;
import io.github.jimbozoomer.jugcraft.lair.dreadnought.IronDreadnoughtLoot;
import io.github.jimbozoomer.jugcraft.lair.hag.MireHagEntity;
import io.github.jimbozoomer.jugcraft.lair.hag.MireHagLoot;
import io.github.jimbozoomer.jugcraft.lair.lich.CryptLichEntity;
import io.github.jimbozoomer.jugcraft.lair.lich.CryptLichLoot;
import io.github.jimbozoomer.jugcraft.lair.sentinel.CydonianSentinelEntity;
import io.github.jimbozoomer.jugcraft.lair.sentinel.SentinelLoot;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Concordance Chunk E, E4 (docs/features/lair-encounter-credit.md): the five keepers that paid only who hurt them (the
 * Mire Hag, the Crypt Lich, the Iron Dreadnought, the Alpha Werewolf and the Cydonian Sentinel) pay their support as the
 * first four keepers do. In each one's arena (its own test structure, laid out as its own tests lay it): a healer who
 * mended 4 of what the keeper's blow took qualifies and is paid when it falls; a striker still down as it falls has their
 * share kept under the keeper's saved name and handed over once on their feet; the one it only hurt is not paid; and its
 * fall reported again pays no one twice. Death is marked as Chunk A's tests mark it. Each keeper's objectives, now deeds,
 * are each one's own tests' (a seal broken, a name read back, a discharge routed, a bell rung: "taking part").
 */
public class KeeperCreditGameTests {
	/** A new player cannot be hurt until its client has loaded the world, or for 60 ticks (see LairEncounterGameTests). */
	private static final int LOADING_TICKS = 80;

	/** {@code healer} mends {@code patient} 4 through the Concordance's effect boundary, as the Mend word does. */
	private static void mend(ServerLevel level, ServerPlayer healer, ServerPlayer patient) {
		Cause cause = Cause.of(healer.getUUID(), Cause.Origin.SPELL, "test:mend", ConcordanceEffects.nextSerial());
		ConcordanceEffects.apply(new ConcordanceEffects.Context(level, cause, healer, new Ledger(new Ledger.Limits(16, 256, 0)), "mend",
				healer.position()), EffectSpec.of(EffectKind.RESTORATION, Intent.HELPFUL, 4, 0), patient);
	}

	/** A survival player standing {@code dx}, {@code dz} from {@code at}. */
	private static ServerPlayer player(GameTestHelper helper, Vec3 at, double dx, double dz) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.snapTo(at.x + dx, at.y, at.z + dz, 0.0F, 0.0F);
		return player;
	}

	/**
	 * The support scenario, for any keeper: woken before the players can be hurt, its blow hurts the patient, the healer
	 * mends it, a striker is marked down, and it falls; then its fall is reported again.
	 */
	private static void supportIsPaid(GameTestHelper helper, Mob keeper, Encounter fight, Vec3 stand, int wakeTicks, Runnable wake,
			BiConsumer<ServerPlayer, Float> took, Predicate<ServerPlayer> firstKill, String name) {
		ServerLevel level = helper.getLevel();
		ServerPlayer patient = player(helper, stand, 1.0, 0.0);
		ServerPlayer healer = player(helper, stand, 3.0, 0.0);
		ServerPlayer fallen = player(helper, stand, -3.0, 0.0);
		List<ServerPlayer> players = List.of(patient, healer, fallen);
		players.forEach(player -> player.setGameMode(GameType.CREATIVE));  // it pays them no heed while they load
		helper.runAfterDelay(LOADING_TICKS - wakeTicks - 2, wake);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			players.forEach(player -> player.setGameMode(GameType.SURVIVAL));
			try {
				UUID receipt = fight.fight();
				helper.assertTrue(fight.underway(), "Awake, " + name + "'s fight is not on");
				// The keeper's blow: a creature's, so the fight's hurt.
				patient.hurtServer(level, level.damageSources().mobAttack(keeper), 6.0F);
				float lost = patient.getMaxHealth() - patient.getHealth();
				mend(level, healer, patient);
				Encounter.Share mended = fight.share(healer);
				helper.assertTrue(lost >= Encounter.HEALING && mended != null && mended.healing() == Encounter.HEALING && mended.qualifies(),
						"Mending 4 of " + name + "'s blow (" + lost + ") did not qualify the healer: " + (mended == null ? "nothing" : mended.healing()));
				took.accept(fallen, 2.0F);
				fight.excuse(fallen.getUUID());  // as dying in its reach does (Encounter's AFTER_DEATH)
				fallen.setHealth(0.0F);  // still down as it falls (on their feet again below, in this tick)
				List<UUID> qualified = fight.qualified();
				helper.assertTrue(qualified.containsAll(List.of(healer.getUUID(), fallen.getUUID())) && !qualified.contains(patient.getUUID()),
						"The healer and the striker should qualify, the one it only hurt not: " + qualified);
				int experience = healer.totalExperience;
				keeper.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				helper.assertTrue(!keeper.isAlive() && !fight.underway(), name + " did not fall");
				helper.assertTrue(!firstKill.test(healer) && healer.totalExperience > experience, "The healer was not paid by " + name);
				helper.assertTrue(firstKill.test(patient), "The one " + name + " only hurt was paid");
				LairRewards.Owed owed = fallen.getAttached(LairRewards.OWED);
				helper.assertTrue(owed != null && owed.due().size() == 1 && owed.due().getFirst().boss().equals(name)
						&& owed.due().getFirst().fight().equals(receipt) && firstKill.test(fallen),
						"The share of one still down as " + name + " fell was not kept for them: " + owed);
				// Back on their feet (AFTER_RESPAWN calls handOver), paid by its own payer.
				fallen.setHealth(fallen.getMaxHealth());
				helper.assertTrue(LairRewards.handOver(fallen) && !fallen.hasAttached(LairRewards.OWED) && !firstKill.test(fallen),
						"Back on their feet, the fallen striker was not handed their share of " + name);
				int healerExperience = healer.totalExperience;
				int fallenExperience = fallen.totalExperience;
				keeper.die(level.damageSources().genericKill());
				helper.assertTrue(healer.totalExperience == healerExperience && fallen.totalExperience == fallenExperience
						&& !fallen.hasAttached(LairRewards.OWED), name + "'s fall reported again paid someone twice");
			} finally {
				keeper.discard();
				players.forEach(ServerPlayer::discard);
			}
			helper.succeed();
		});
	}

	@GameTest(structure = "jugcraft-test:fen_marsh", padding = 8, maxTicks = 160)
	public void theMireHagPaysHerSupport(GameTestHelper helper) {
		MireHagEntity hag = MireHagEntity.summon(helper.getLevel(), helper.absolutePos(new BlockPos(0, -7, 0)), null);
		supportIsPaid(helper, hag, hag.encounter(), hag.centre(), MireHagEntity.WAKE_TICKS, () -> hag.wake(helper.getLevel()), hag::took,
				MireHagLoot::firstKill, MireHagLoot.BOSS);
	}

	@GameTest(structure = "jugcraft-test:crypt_hall", padding = 8, maxTicks = 160)
	public void theCryptLichPaysHisSupport(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(6, -3, 4));
		CryptLichEntity lich = CryptLichEntity.summon(helper.getLevel(), origin, null);
		Vec3 stand = new Vec3(lich.centre().x, origin.getY() + CryptArchive.STAND, lich.centre().z);
		supportIsPaid(helper, lich, lich.encounter(), stand, CryptLichEntity.WAKE_TICKS, () -> lich.wake(helper.getLevel()), lich::took,
				CryptLichLoot::firstKill, CryptLichLoot.BOSS);
	}

	@GameTest(structure = "jugcraft-test:foundry_pit", padding = 8, maxTicks = 160)
	public void theIronDreadnoughtPaysHisSupport(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(3, -3, 2));
		IronDreadnoughtEntity dreadnought = IronDreadnoughtEntity.summon(helper.getLevel(), origin, null);
		Vec3 stand = new Vec3(dreadnought.centre().x, origin.getY() + FoundryTrench.STAND, dreadnought.centre().z);
		supportIsPaid(helper, dreadnought, dreadnought.encounter(), stand, IronDreadnoughtEntity.WAKE_TICKS,
				() -> dreadnought.wake(helper.getLevel()), dreadnought::took, IronDreadnoughtLoot::firstKill, IronDreadnoughtLoot.BOSS);
	}

	@GameTest(structure = "jugcraft-test:moonlit_glade", padding = 8, maxTicks = 160)
	public void theAlphaWerewolfPaysHisSupport(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1 - MoonlitRing.STAND, 0));
		AlphaWerewolfEntity alpha = AlphaWerewolfEntity.summon(helper.getLevel(), origin, null);
		Vec3 stand = new Vec3(alpha.centre().x, origin.getY() + MoonlitRing.STAND, alpha.centre().z);
		supportIsPaid(helper, alpha, alpha.encounter(), stand, AlphaWerewolfEntity.WAKE_TICKS, () -> alpha.wake(helper.getLevel()), alpha::took,
				AlphaWerewolfLoot::firstKill, AlphaWerewolfLoot.BOSS);
	}

	@GameTest(structure = "jugcraft-test:crypt_hall", padding = 8, maxTicks = 160)
	public void theCydonianSentinelPaysItsSupport(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(-5, 2 - CydonianSanctum.STAND, -14));
		CydonianSentinelEntity sentinel = CydonianSentinelEntity.summon(helper.getLevel(), origin, null);
		Vec3 stand = new Vec3(sentinel.centre().x, origin.getY() + CydonianSanctum.STAND, sentinel.centre().z);
		supportIsPaid(helper, sentinel, sentinel.encounter(), stand, CydonianSentinelEntity.WAKE_TICKS, () -> sentinel.wake(helper.getLevel()),
				sentinel::took, SentinelLoot::firstKill, SentinelLoot.BOSS);
	}
}
