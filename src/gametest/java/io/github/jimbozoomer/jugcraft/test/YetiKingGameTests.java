package io.github.jimbozoomer.jugcraft.test;

import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.agriculture.TrickOrTreat;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import io.github.jimbozoomer.jugcraft.concordance.effect.Cause;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectKind;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import io.github.jimbozoomer.jugcraft.concordance.effect.Intent;
import io.github.jimbozoomer.jugcraft.concordance.effect.Ledger;
import io.github.jimbozoomer.jugcraft.lair.Encounter;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.yeti.FallingIcicleEntity;
import io.github.jimbozoomer.jugcraft.lair.yeti.GlacialSpikeEntity;
import io.github.jimbozoomer.jugcraft.lair.yeti.GlacierHall;
import io.github.jimbozoomer.jugcraft.lair.yeti.JugcraftYeti;
import io.github.jimbozoomer.jugcraft.lair.yeti.YetiKingEntity;
import io.github.jimbozoomer.jugcraft.lair.yeti.YetiKingLoot;
import io.github.jimbozoomer.jugcraft.lair.yeti.YetiWhelpEntity;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The Yeti King (docs/features/yeti-king.md), as far as a game-test server can show him: one put down on his own (with no
 * lair instance, over a hall laid out from the corner of an arena the hall's size, so his throne, his lake and its dens
 * are all loaded) waits on his throne until a player's blow wakes him, and leaps down onto his lake; his health grows
 * with the party, and the Fury of the Peaks shortens his cooldowns and widens his slam; Maul Swipe strikes in front and
 * not behind; his Ground Slam bares the drift snow round where he lands and never the trampled, and the snow drifts
 * back; an Avalanche Charge into an ice column stuns him, and stunned he takes a third more damage; his kin come out of
 * the dens, never more than four at once, and flee when he is left alone; his frost, and the Yeti Mitten against it; a
 * glacial spike throws a player up and an icicle shatters on its mark; and his loot is each participant's own. His fight
 * in the Glacier Hall itself runs in a real world, in {@link YetiKingClientGameTests}. Since Concordance Chunk B (B-01)
 * his fight's credit is the lairs' shared one: support and the absent share his loot once, a killing blow alone is paid,
 * a tamed creature's blow is its owner's, a charge baited into a column is each baiter's deed but no idle late
 * arrival's, four players are each paid once, and a fight loaded awake keeps a fresh record. Since B-02 his loot is what
 * his JEI pages say.
 *
 * <p>He fights any player within 38 blocks of his lake's centre; the arena keeps every other test's players farther off
 * than that. Each test sends him away before it ends, with his whelps, and lets the snow drift back.
 */
public class YetiKingGameTests {
	/** An arena of air the hall's size (80 x 88), so everything of his fight is loaded and ticking. */
	private static final String HALL = "jugcraft-test:yeti_hall";
	/**
	 * A new player cannot be hurt until its client has loaded the world, or for 60 ticks; a mock player has no client, so
	 * a test that hurts one waits this long first.
	 */
	private static final int LOADING_TICKS = 80;
	/** How many shares a page test rolls: each count then lies far inside five standard deviations of its chance's mean. */
	private static final int SHARES = 400;

	/** Whether {@code count} of {@value #SHARES} shares lies within five standard deviations of {@code chance}'s mean. */
	private static boolean within(int count, double chance) {
		double mean = SHARES * chance;
		double deviation = 5.0 * Math.sqrt(SHARES * chance * (1.0 - chance));
		return count >= mean - deviation && count <= mean + deviation;
	}

	/** The origin of a hall laid out from the arena's corner, its lake's surface a block over the arena's floor. */
	private static BlockPos origin(GameTestHelper helper) {
		return helper.absolutePos(new BlockPos(0, -GlacierHall.LAKE, 0));
	}

	/** He waits on the throne of the hall laid out from {@code origin}. */
	private static YetiKingEntity waiting(GameTestHelper helper, BlockPos origin) {
		return YetiKingEntity.summon(helper.getLevel(), origin, null);
	}

	/** Stone at the lake's layer under the template's cells {@code x0..x1}, {@code z0..z1}, for players to stand on. */
	private static void floor(ServerLevel level, BlockPos origin, int x0, int z0, int x1, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				level.setBlockAndUpdate(origin.offset(x, GlacierHall.LAKE, z), Blocks.STONE.defaultBlockState());
			}
		}
	}

	/** A survival player standing {@code dx}, {@code dz} from his lake's centre, on it. */
	private static ServerPlayer player(GameTestHelper helper, YetiKingEntity king, double dx, double dz) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 at = king.centre().add(dx, 0.0, dz);
		player.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		return player;
	}

	/** Checks {@code body}, then sends him away whether it passed or failed, so a failed test leaves no king fighting on. */
	private static void ending(ServerLevel level, YetiKingEntity king, Runnable body) {
		try {
			body.run();
		} finally {
			for (UUID id : king.whelps()) {
				if (level.getEntity(id) instanceof YetiWhelpEntity whelp) {
					whelp.discard();
				}
			}
			king.driftAll(level);
			king.discard();
		}
	}

	/** {@code healer} mends {@code patient} 4 through the Concordance's effect boundary, as the Mend word does. */
	private static void mend(ServerLevel level, ServerPlayer healer, ServerPlayer patient) {
		Cause cause = Cause.of(healer.getUUID(), Cause.Origin.SPELL, "test:mend", ConcordanceEffects.nextSerial());
		ConcordanceEffects.apply(new ConcordanceEffects.Context(level, cause, healer, new Ledger(new Ledger.Limits(16, 256, 0)), "mend",
				healer.position()), EffectSpec.of(EffectKind.RESTORATION, Intent.HELPFUL, 4, 0), patient);
	}

	/** How many of his trophies {@code player} holds. */
	private static int trophies(ServerPlayer player) {
		return player.getInventory().countItem(JugcraftYeti.glacierMaul()) + player.getInventory().countItem(JugcraftYeti.rimeclaw());
	}

	/** {@code player}'s deeds in {@code fight} (none without a part). */
	private static int deeds(Encounter fight, ServerPlayer player) {
		Encounter.Share share = fight.share(player);
		return share == null ? 0 : share.deeds();
	}

	/** A survival player north of the north-west column (26.5, 23.5), {@code x} across the hall (floor 24..29, 10..14). */
	private static ServerPlayer northOfTheColumn(GameTestHelper helper, YetiKingEntity king, BlockPos origin, double x) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.snapTo(origin.getX() + x, king.floorY(), origin.getZ() + 12.0, 0.0F, 0.0F);
		return player;
	}

	/** Puts him 9.5 blocks south of the north-west column and sets him charging at {@code foe}, beyond it (as a charge test does). */
	private static void chargeAt(ServerLevel level, YetiKingEntity king, BlockPos origin, ServerPlayer foe) {
		king.teleportTo(origin.getX() + 26.5, king.floorY(), origin.getZ() + 33.0);
		king.begin(level, YetiKingEntity.Attack.AVALANCHE_CHARGE, foe);
	}

	/**
	 * Waiting on his throne he takes no harm; a player's blow wakes him at full health, and over {@value
	 * YetiKingEntity#WAKE_TICKS} ticks he roars and leaps down onto his lake below the dais, to hunt.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 120)
	public void aBlowWakesHimAndHeLeapsDown(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 37, 34, 42, 39);
		ServerPlayer player = player(helper, king, 0.5, 0.5);
		boolean throned = king.phase() == YetiKingEntity.Phase.WAITING && king.position().distanceTo(king.throne()) < 0.01;
		boolean harmless = !king.hurtServer(level, level.damageSources().playerAttack(player), 10.0F);
		boolean woken = king.phase() == YetiKingEntity.Phase.WAKING && king.getHealth() == king.getMaxHealth();
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 3, () -> {
			ending(level, king, () -> {
				player.discard();
				Vec3 below = GlacierHall.below().add(origin.getX(), origin.getY(), origin.getZ());
				helper.assertTrue(throned, "He did not wait on his throne");
				helper.assertTrue(harmless, "Waiting, he was harmed");
				helper.assertTrue(woken, "A blow did not wake him at full health");
				helper.assertTrue(king.phase() == YetiKingEntity.Phase.HUNT, "He did not come down to hunt: " + king.phase());
				helper.assertTrue(Math.abs(king.getY() - king.floorY()) < 0.1, "He is not on his lake but at " + king.getY());
				helper.assertTrue(Math.hypot(king.getX() - below.x, king.getZ() - below.z) < 2.0, "He did not land below his dais but at " + king.position());
			});
			helper.succeed();
		});
	}

	/**
	 * His health grows by half for each player after the first, at most two and a half times; the Fury of the Peaks makes
	 * every cooldown a third shorter and his slam bare a wider ring.
	 */
	@GameTest(structure = HALL, padding = 8)
	public void hisHealthAndHisFury(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		YetiKingEntity king = waiting(helper, origin(helper));
		ending(level, king, () -> {
			helper.assertTrue(YetiKingEntity.partyScale(1) == 1.0 && YetiKingEntity.partyScale(2) == 1.5 && YetiKingEntity.partyScale(4) == 2.5
					&& YetiKingEntity.partyScale(9) == 2.5, "His health scales with the party, at most 2.5 times");
			helper.assertTrue(king.cooldown(240) == 240 && king.ring() == YetiKingEntity.RING, "Calm, his cooldowns and his ring are their own");
			king.startFury(level);
			helper.assertTrue(king.furious() && king.phase() == YetiKingEntity.Phase.FURY, "No Fury of the Peaks");
			helper.assertTrue(king.cooldown(240) == 160 && king.cooldown(YetiKingEntity.GLOBAL_COOLDOWN) == 13,
					"In his fury his cooldowns are not a third shorter: " + king.cooldown(240));
			helper.assertTrue(king.ring() == YetiKingEntity.FURY_RING, "In his fury his slam does not bare a wider ring");
		});
		helper.succeed();
	}

	/**
	 * Maul Swipe strikes a player in front of him and not one directly behind. The two wait out their loading in
	 * creative, where he pays them no heed.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 180)
	public void hisSwipeStrikesInFrontNotBehind(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 35, 31, 44, 42);
		ServerPlayer front = player(helper, king, 0.5, 3.0);
		ServerPlayer behind = player(helper, king, 0.5, -3.0);
		front.setGameMode(GameType.CREATIVE);
		behind.setGameMode(GameType.CREATIVE);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			front.setGameMode(GameType.SURVIVAL);
			behind.setGameMode(GameType.SURVIVAL);
			ending(level, king, () -> {
				Vec3 centre = king.centre();
				king.teleportTo(centre.x, king.floorY(), centre.z);
				king.setYRot(0.0F);  // facing south, towards the one in front
				int hit = king.swipe(level);
				helper.assertTrue(hit == 1, "The swipe struck " + hit + " players, not 1");
				helper.assertTrue(front.getHealth() < front.getMaxHealth(), "The one in front is not struck");
				helper.assertTrue(behind.getHealth() == behind.getMaxHealth(), "The one behind him is not spared");
			});
			helper.succeed();
		});
	}

	/**
	 * His Ground Slam blasts the drift snow bare to glare ice within {@value YetiKingEntity#RING} blocks of where he lands,
	 * never the trampled snow nor the snow beyond; {@value YetiKingEntity#GLARE_TICKS} ticks later it has drifted back.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 300)
	public void hisSlamBaresTheDriftSnowAndItDriftsBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		YetiKingEntity king = waiting(helper, origin(helper));
		Vec3 at = king.centre();
		BlockPos middle = BlockPos.containing(at).below();
		// Drift snow round the lake's centre, a row of trampled snow across it.
		for (int x = -9; x <= 9; x++) {
			for (int z = -9; z <= 9; z++) {
				level.setBlockAndUpdate(middle.offset(x, 0, z), (z == 0 ? JugcraftLairs.TRAMPLED_SNOW : JugcraftLairs.DRIFT_SNOW).defaultBlockState());
			}
		}
		List<BlockPos> cells = king.bare(level, at, YetiKingEntity.RING);
		boolean bared = !cells.isEmpty() && cells.stream().allMatch(cell -> level.getBlockState(cell).is(JugcraftLairs.GLARE_ICE));
		boolean inRing = cells.stream().allMatch(cell -> Math.hypot(cell.getX() + 0.5 - at.x, cell.getZ() + 0.5 - at.z) <= YetiKingEntity.RING);
		boolean trampledKept = IntStream.rangeClosed(-9, 9).allMatch(x -> level.getBlockState(middle.offset(x, 0, 0)).is(JugcraftLairs.TRAMPLED_SNOW));
		boolean beyondKept = level.getBlockState(middle.offset(9, 0, 9)).is(JugcraftLairs.DRIFT_SNOW);
		int patches = king.baredPatches();
		helper.runAfterDelay(YetiKingEntity.GLARE_TICKS + 5, () -> {
			ending(level, king, () -> {
				helper.assertTrue(bared, "The drift snow round where he lands was not bared to glare ice");
				helper.assertTrue(inRing, "He bared snow outside his ring");
				helper.assertTrue(trampledKept, "He bared the trampled snow");
				helper.assertTrue(beyondKept, "He bared the snow beyond his ring");
				helper.assertTrue(patches == 1, patches + " bare patches, not 1");
				helper.assertTrue(cells.stream().allMatch(cell -> level.getBlockState(cell).is(JugcraftLairs.DRIFT_SNOW)),
						"The snow did not drift back over the bare ice");
				helper.assertTrue(king.baredPatches() == 0, "A patch is still bare");
			});
			helper.succeed();
		});
	}

	/**
	 * An Avalanche Charge at a foe beyond an ice column stops at the column: he is stunned for {@value
	 * YetiKingEntity#STUN_TICKS} ticks, and meanwhile takes a third more damage.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 160)
	public void aChargeIntoAColumnStunsHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		// The north-west column stands at (26.5, 23.5): he charges north at a foe beyond it.
		floor(level, origin, 24, 10, 29, 14);
		ServerPlayer foe = helper.makeMockServerPlayerInLevel();
		foe.setGameMode(GameType.SURVIVAL);
		foe.snapTo(origin.getX() + 26.5, king.floorY(), origin.getZ() + 12.0, 0.0F, 0.0F);
		king.wake(level);
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 2, () -> {
			YetiKingEntity.Phase hunting = king.phase();
			king.teleportTo(origin.getX() + 26.5, king.floorY(), origin.getZ() + 33.0);
			king.begin(level, YetiKingEntity.Attack.AVALANCHE_CHARGE, foe);
			helper.runAfterDelay(YetiKingEntity.Attack.AVALANCHE_CHARGE.windup + 12, () -> {
				ending(level, king, () -> {
					helper.assertTrue(hunting == YetiKingEntity.Phase.HUNT, "He was not hunting: " + hunting);
					helper.assertTrue(king.stunnedTicks() > 0 && king.action() == YetiKingEntity.Action.STUNNED,
							"Charging into the column did not stun him: " + king.action());
					double fromColumn = Math.hypot(king.getX() - origin.getX() - 26.5, king.getZ() - origin.getZ() - 23.5);
					helper.assertTrue(fromColumn > GlacierHall.COLUMN_RADIUS && king.getZ() - origin.getZ() > 23.5,
							"He ran through the column: " + fromColumn + " from it");
					float before = king.getHealth();
					king.hurtServer(level, level.damageSources().indirectMagic(foe, foe), 6.0F);
					float taken = before - king.getHealth();
					helper.assertTrue(Math.abs(taken - 6.0 * YetiKingEntity.STUN_TAKEN) < 0.01, "Stunned, he took " + taken + " of 6, not a third more");
					foe.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * His kin climb out of the dens in the side walls, never more than {@value YetiKingEntity#MAX_WHELPS} at once; left
	 * alone he climbs back onto his throne, healed, and they flee into the snow.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 60)
	public void hisKinComeFromTheDensAndFleeWhenHeIsLeftAlone(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 3, 34, 8, 39);
		floor(level, origin, 71, 34, 76, 39);
		king.wake(level);
		int called = king.callKin(level, YetiKingEntity.KIN);
		int more = king.callKin(level, 6);
		List<UUID> kin = king.whelps();
		List<Vec3> dens = GlacierHall.dens().stream().map(den -> den.add(origin.getX(), origin.getY(), origin.getZ())).toList();
		boolean atDens = kin.stream().allMatch(id -> level.getEntity(id) instanceof YetiWhelpEntity whelp
				&& dens.stream().anyMatch(den -> whelp.position().distanceTo(den) < 1.5));
		king.setHealth(king.getMaxHealth() * 0.5F);
		king.reset(level);
		helper.runAfterDelay(3, () -> {
			ending(level, king, () -> {
				helper.assertTrue(called == YetiKingEntity.KIN, called + " whelps answered his call, not " + YetiKingEntity.KIN);
				helper.assertTrue(more == YetiKingEntity.MAX_WHELPS - YetiKingEntity.KIN && kin.size() == YetiKingEntity.MAX_WHELPS,
						"More than " + YetiKingEntity.MAX_WHELPS + " whelps came at once: " + kin.size());
				helper.assertTrue(atDens, "A whelp did not come out of a den");
				for (UUID id : kin) {
					helper.assertTrue(!(level.getEntity(id) instanceof YetiWhelpEntity whelp) || whelp.isRemoved(), "A whelp did not flee");
				}
				helper.assertTrue(king.phase() == YetiKingEntity.Phase.WAITING && king.getHealth() == king.getMaxHealth(),
						"Left alone he is not back on his throne, healed");
				helper.assertTrue(king.position().distanceTo(king.throne()) < 0.1, "He is not on his throne but at " + king.position());
			});
			helper.succeed();
		});
	}

	/**
	 * His frost: the King's Roar's blizzard chills a player to just short of freezing them through, his Frost Breath
	 * stacks past it; a Yeti Mitten in the offhand keeps its wearer warm, whatever frost they gather.
	 */
	@GameTest(maxTicks = 20)
	public void hisFrostAndTheYetiMitten(GameTestHelper helper) {
		ServerPlayer bare = helper.makeMockServerPlayerInLevel();
		ServerPlayer warm = helper.makeMockServerPlayerInLevel();
		bare.setGameMode(GameType.SURVIVAL);
		warm.setGameMode(GameType.SURVIVAL);
		warm.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftYeti.YETI_MITTEN));
		YetiKingEntity.chill(bare, YetiKingEntity.BLIZZARD_FROST, false);
		YetiKingEntity.chill(warm, YetiKingEntity.BLIZZARD_FROST, false);
		int blizzard = bare.getTicksFrozen();
		boolean shortOfFreezing = !bare.isFullyFrozen();
		int warmBlizzard = warm.getTicksFrozen();
		YetiKingEntity.chill(bare, bare.getTicksRequiredToFreeze(), true);
		boolean frozenThrough = bare.isFullyFrozen();
		warm.setTicksFrozen(100);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(blizzard == YetiKingEntity.BLIZZARD_FROST && shortOfFreezing, "The blizzard did not chill to just short of freezing: " + blizzard);
			helper.assertTrue(frozenThrough, "His breath's frost did not stack past freezing");
			helper.assertTrue(warmBlizzard == 0, "The mitten's wearer was chilled");
			helper.assertTrue(warm.getTicksFrozen() == 0, "The mitten did not keep its wearer warm");
			bare.discard();
			warm.discard();
			helper.succeed();
		});
	}

	/**
	 * A glacial spike bursting up under a player strikes them and throws them up; an icicle shaken from the vault falls
	 * onto its mark and shatters, striking the player standing there.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 140)
	public void aSpikeThrowsAPlayerUpAndAnIcicleShatters(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 32, 34, 42, 39);
		ServerPlayer struck = player(helper, king, 0.5, 0.5);
		ServerPlayer under = player(helper, king, -4.5, 0.5);
		// They wait out their loading in creative, where he pays them no heed; once they can be struck he may wake, but
		// waking he strikes no one until the icicle has long since landed.
		struck.setGameMode(GameType.CREATIVE);
		under.setGameMode(GameType.CREATIVE);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			struck.setGameMode(GameType.SURVIVAL);
			under.setGameMode(GameType.SURVIVAL);
			float before = struck.getHealth();
			GlacialSpikeEntity.burst(level, king, struck.position());
			boolean thrown = struck.getHealth() < before && struck.getDeltaMovement().y > 0.0;
			float underBefore = under.getHealth();
			FallingIcicleEntity icicle = FallingIcicleEntity.drop(level, king, under.position());
			helper.runAfterDelay((int) Math.ceil(FallingIcicleEntity.HEIGHT / FallingIcicleEntity.FALL_SPEED) + 4, () -> {
				ending(level, king, () -> {
					helper.assertTrue(thrown, "The spike did not strike the player and throw them up");
					helper.assertTrue(icicle.isRemoved(), "The icicle did not shatter");
					helper.assertTrue(under.getHealth() < underBefore, "The icicle did not strike the player under it");
					struck.discard();
					under.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * His loot is each participant's own: everyone who struck him rolls for themselves (Yeti Fur, and on a first kill
	 * always one of his trophies) into their own inventory, and a bystander who never struck him gets nothing. Afterwards
	 * Abominable is theirs, and a later roll no longer counts as a first kill.
	 */
	@GameTest(structure = HALL, padding = 8)
	public void hisLootIsEachParticipantsOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 36, 34, 43, 41);
		ServerPlayer first = player(helper, king, 0.5, 3.0);
		ServerPlayer second = player(helper, king, 2.5, 3.0);
		ServerPlayer bystander = player(helper, king, -1.5, 3.0);
		king.took(first, 10.0F);
		king.took(second, 4.0F);
		ending(level, king, () -> {
			List<ServerPlayer> participants = king.participants(level);
			helper.assertTrue(participants.contains(first) && participants.contains(second), "Both who struck him take part");
			helper.assertFalse(participants.contains(bystander), "A bystander who never struck him does not");
			helper.assertTrue(YetiKingLoot.firstKill(first), "A newcomer has not yet won Abominable");
			List<ItemStack> loot = YetiKingLoot.roll(level, king, first);
			int fur = loot.stream().filter(stack -> stack.is(JugcraftYeti.YETI_FUR)).mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(fur >= 4 && fur <= 8, fur + " Yeti Fur, not 4 to 8");
			helper.assertTrue(loot.stream().filter(YetiKingLoot::trophy).count() == 1, "A first kill always brings one of his trophies");
			YetiKingLoot.reward(level, king, participants);
			for (ServerPlayer player : List.of(first, second)) {
				helper.assertTrue(player.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4, "Each participant's loot went into their own inventory");
				int trophies = player.getInventory().countItem(JugcraftYeti.glacierMaul()) + player.getInventory().countItem(JugcraftYeti.rimeclaw());
				helper.assertTrue(trophies == 1, "Each first kill brought its own trophy, not " + trophies);
				helper.assertFalse(YetiKingLoot.firstKill(player), "Abominable is theirs now");
			}
			helper.assertTrue(bystander.getInventory().countItem(JugcraftYeti.YETI_FUR) == 0, "The bystander got nothing");
			helper.assertTrue(YetiKingLoot.firstKill(bystander), "The bystander has not won Abominable");
			for (ServerPlayer player : List.of(first, second, bystander)) {
				player.discard();
			}
		});
		helper.succeed();
	}

	/**
	 * Concordance Chunk B: support shares his loot. A healer who mended 4 of what his fight did is paid; a striker who
	 * died in it is kept their share and handed it on their feet; one who left the game in it has it kept in the world
	 * and handed over on joining; one who walked away alive, a bystander and the one he only hurt are not paid; the
	 * experience is shared among those paid, now or later; and his fall reported again pays no one twice.
	 */
	@GameTest(structure = HALL, padding = 8)
	public void supportSharesHisLootOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 34, 34, 45, 41);
		ServerPlayer fighter = player(helper, king, 0.5, 3.0);
		ServerPlayer healer = player(helper, king, 2.5, 3.0);
		ServerPlayer bystander = player(helper, king, -1.5, 3.0);
		ServerPlayer wanderer = player(helper, king, 4.5, 3.0);
		ServerPlayer fallen = player(helper, king, -3.5, 3.0);
		// Away from the game: a player the server's list never held, as one who has left is not held.
		FakePlayer away = FakePlayer.get(level, new GameProfile(UUID.randomUUID(), "yeti_away"));
		Encounter fight = king.encounter();
		fight.begin();
		UUID receipt = fight.fight();
		LairRewards.Data data = LairRewards.Data.of(level.getServer());
		ending(level, king, () -> {
			try {
				// His fight hurt the fighter 6 (as his blow would have been measured); the healer mends 4 of it.
				fighter.setHealth(fighter.getMaxHealth() - 6.0F);
				fight.hurt(fighter, 6.0F);
				mend(level, healer, fighter);
				king.took(wanderer, 2.0F);
				king.took(fallen, 2.0F);
				king.took(away, 2.0F);
				// A real death: Fabric's AFTER_DEATH reaches Encounter's listener (the killing blow itself is not real).
				fallen.setHealth(0.0F);
				fallen.die(level.damageSources().genericKill());
				// Leaving is marked as Fabric's LEAVE would mark it: a test player cannot disconnect.
				fight.left(away.getUUID());
				wanderer.snapTo(wanderer.getX() + 100.0, wanderer.getY(), wanderer.getZ(), 0.0F, 0.0F);
				helper.assertTrue(fight.excused(fallen.getUUID()), "Dying in his fight did not excuse the fallen striker");
				List<UUID> qualified = fight.qualified();
				helper.assertTrue(qualified.containsAll(List.of(healer.getUUID(), wanderer.getUUID(), fallen.getUUID(), away.getUUID()))
						&& !qualified.contains(fighter.getUUID()) && !qualified.contains(bystander.getUUID()),
						"The healer and the three strikers should qualify, the one he only hurt and the bystander not: " + qualified);
				int experience = healer.totalExperience;
				king.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				int fur = healer.getInventory().countItem(JugcraftYeti.YETI_FUR);
				int share = YetiKingEntity.EXPERIENCE / 3;
				helper.assertTrue(fur >= 4 && trophies(healer) == 1, "The healer was not paid, first-kill trophy and all: " + fur + " Yeti Fur");
				helper.assertTrue(healer.totalExperience - experience == share,
						"300 was not shared among the three paid: " + (healer.totalExperience - experience));
				LairRewards.Owed owed = fallen.getAttached(LairRewards.OWED);
				helper.assertTrue(owed != null && owed.due().size() == 1 && owed.due().getFirst().boss().equals(YetiKingLoot.BOSS)
						&& owed.due().getFirst().fight().equals(receipt) && fallen.getInventory().countItem(JugcraftYeti.YETI_FUR) == 0,
						"The fallen striker's share was not kept for them: " + owed);
				List<LairRewards.Due> kept = data.kept(away.getUUID());
				helper.assertTrue(kept.size() == 1 && kept.getFirst().boss().equals(YetiKingLoot.BOSS) && kept.getFirst().fight().equals(receipt)
						&& kept.getFirst().experience() == share, "The share of one who left the game was not kept in the world: " + kept);
				for (ServerPlayer unpaid : List.of(wanderer, bystander, fighter)) {
					helper.assertTrue(unpaid.getInventory().countItem(JugcraftYeti.YETI_FUR) == 0,
							"Paid: the one who walked away, the bystander or the one he only hurt");
				}
				// Back on their feet far off (AFTER_RESPAWN calls handOver); joining (JOIN calls welcome).
				fallen.snapTo(fallen.getX() - 100.0, fallen.getY(), fallen.getZ(), 0.0F, 0.0F);
				fallen.setHealth(fallen.getMaxHealth());
				helper.assertTrue(LairRewards.handOver(fallen) && fallen.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4
						&& !fallen.hasAttached(LairRewards.OWED), "Back on their feet, the fallen striker was not handed their share by his payer");
				LairRewards.welcome(away);
				helper.assertTrue(away.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4 && data.kept(away.getUUID()).isEmpty(),
						"Joining again, the one who left was not handed their share, or the world still keeps it");
				int fallenFur = fallen.getInventory().countItem(JugcraftYeti.YETI_FUR);
				king.die(level.damageSources().genericKill());
				helper.assertTrue(healer.getInventory().countItem(JugcraftYeti.YETI_FUR) == fur && trophies(healer) == 1
						&& fallen.getInventory().countItem(JugcraftYeti.YETI_FUR) == fallenFur && !fallen.hasAttached(LairRewards.OWED)
						&& data.kept(away.getUUID()).isEmpty(), "His fall reported again paid someone twice");
			} finally {
				data.take(away.getUUID());
				List.of(fighter, healer, bystander, wanderer, fallen).forEach(ServerPlayer::discard);
			}
		});
		helper.succeed();
	}

	/**
	 * Concordance Chunk B: a player whose only part in his fight is the blow that fells him has taken part before his
	 * fall is paid, and is paid: Yeti Fur, a first kill's trophy and Abominable.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 120)
	public void aKillingBlowAloneIsPaid(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 34, 34, 45, 41);
		king.wake(level);
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 2, () -> {
			ServerPlayer striker = player(helper, king, 0.5, 3.0);
			Encounter fight = king.encounter();
			ending(level, king, () -> {
				try {
					helper.assertTrue(king.phase() == YetiKingEntity.Phase.HUNT && fight.underway() && fight.share(striker) == null,
							"He was not hunting in a fight under way, or the striker already had a part: " + king.phase());
					boolean felled = king.hurtServer(level, level.damageSources().playerAttack(striker), king.getMaxHealth() * 4.0F);
					Encounter.Share share = fight.share(striker);
					helper.assertTrue(felled && king.isDeadOrDying() && share != null && share.qualifies(),
							"The blow that felled him was not the striker's part in his fight");
					helper.assertTrue(striker.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4 && trophies(striker) == 1,
							"The striker whose only blow felled him was not paid");
					helper.assertFalse(YetiKingLoot.firstKill(striker), "Abominable is not the striker's");
				} finally {
					striker.discard();
				}
			});
			helper.succeed();
		});
	}

	/**
	 * Concordance Chunk B: a tamed wolf's bite on him, or on one of his whelps, is its owner's part in his fight, and
	 * each owner is paid when he falls.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 120)
	public void aPetsBlowIsItsOwners(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 34, 34, 45, 41);
		king.wake(level);
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 2, () -> {
			ServerPlayer kingOwner = player(helper, king, 0.5, 3.0);
			ServerPlayer whelpOwner = player(helper, king, 2.5, 3.0);
			var kingWolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, new BlockPos(38, 1, 40));
			var whelpWolf = helper.spawnWithNoFreeWill(EntityTypes.WOLF, new BlockPos(41, 1, 40));
			kingWolf.tame(kingOwner);
			whelpWolf.tame(whelpOwner);
			Encounter fight = king.encounter();
			ending(level, king, () -> {
				try {
					helper.assertTrue(king.callKin(level, 1) == 1, "No whelp came out of a den");
					YetiWhelpEntity whelp = (YetiWhelpEntity) level.getEntity(king.whelps().getFirst());
					boolean bitten = whelp.hurtServer(level, level.damageSources().mobAttack(whelpWolf), 2.0F);
					Encounter.Share whelpShare = fight.share(whelpOwner);
					helper.assertTrue(bitten && whelpShare != null && whelpShare.damage() == 2.0F && whelpShare.qualifies(),
							"A wolf's bite on his whelp was not its owner's part");
					boolean bitHim = king.hurtServer(level, level.damageSources().mobAttack(kingWolf), 4.0F);
					Encounter.Share kingShare = fight.share(kingOwner);
					helper.assertTrue(bitHim && kingShare != null && kingShare.damage() == 4.0F && kingShare.qualifies(),
							"A wolf's bite on him was not its owner's part");
					king.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
					helper.assertTrue(kingOwner.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4
							&& whelpOwner.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4, "A wolf's owner was not paid");
				} finally {
					kingWolf.discard();
					whelpWolf.discard();
					kingOwner.discard();
					whelpOwner.discard();
				}
			});
			helper.succeed();
		});
	}

	/**
	 * Concordance Chunk B: his charge aimed at a player who already has a part in his fight (here a ward on a friend,
	 * short of qualifying) and ending in an ice column is that player's deed, once a fight; a second baiter earns their
	 * own on his next charge, and both qualify by it alone.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 240)
	public void twoBaitersEachEarnTheirDeed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 24, 10, 29, 14);
		ServerPlayer first = northOfTheColumn(helper, king, origin, 26.5);
		ServerPlayer second = northOfTheColumn(helper, king, origin, 25.5);
		int run = YetiKingEntity.Attack.AVALANCHE_CHARGE.windup + 12;
		Encounter fight = king.encounter();
		king.wake(level);
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 2, () -> {
			// Each wards the other: a part in his fight, short of qualifying.
			fight.warded(first, second, 2.0F, 600);
			fight.warded(second, first, 2.0F, 600);
			Encounter.Share firstPart = fight.share(first);
			Encounter.Share secondPart = fight.share(second);
			boolean shortOfQualifying = firstPart != null && !firstPart.qualifies() && secondPart != null && !secondPart.qualifies();
			chargeAt(level, king, origin, first);
			helper.runAfterDelay(run, () -> {
				boolean firstStunned = king.stunnedTicks() > 0;
				int firstDeeds = deeds(fight, first);
				int secondThen = deeds(fight, second);
				// Once he has reeled, and before his own pause between attacks is up, he charges the other.
				helper.runAfterDelay(YetiKingEntity.STUN_TICKS + 4, () -> {
					chargeAt(level, king, origin, second);
					helper.runAfterDelay(run, () -> {
						ending(level, king, () -> {
							try {
								helper.assertTrue(shortOfQualifying, "A ward on a friend did not give each a part short of qualifying");
								helper.assertTrue(firstStunned && king.stunnedTicks() > 0, "A charge did not end in the column");
								helper.assertTrue(firstDeeds == 1 && secondThen == 0,
										"The first charge was not the first baiter's deed alone: " + firstDeeds + " and " + secondThen);
								helper.assertTrue(deeds(fight, first) == 1 && deeds(fight, second) == 1,
										"Each baiter did not earn their own deed: " + deeds(fight, first) + " and " + deeds(fight, second));
								helper.assertTrue(fight.share(first).damage() == 0.0F && fight.share(second).damage() == 0.0F,
										"A baiter struck him");
								helper.assertTrue(fight.qualified().containsAll(List.of(first.getUUID(), second.getUUID())),
										"The deed alone did not qualify each baiter");
								helper.assertFalse(fight.deed(first, "column " + first.getUUID()), "A baiter's deed counted twice in one fight");
							} finally {
								first.discard();
								second.discard();
							}
						});
						helper.succeed();
					});
				});
			});
		});
	}

	/**
	 * Concordance Chunk B: a late arrival with no part in his fight, though his fight has struck them, whom he charges into
	 * a column (as when he turns on the nearest player) earns no deed and no reward, and no one else gets the deed.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 160)
	public void anIdleLateArrivalEarnsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 24, 10, 29, 14);
		floor(level, origin, 34, 34, 45, 41);
		ServerPlayer fighter = player(helper, king, 0.5, 3.0);
		fighter.setGameMode(GameType.CREATIVE);  // he pays them no heed
		int run = YetiKingEntity.Attack.AVALANCHE_CHARGE.windup + 12;
		Encounter fight = king.encounter();
		king.wake(level);
		helper.runAfterDelay(YetiKingEntity.WAKE_TICKS + 2, () -> {
			king.took(fighter, 5.0F);
			ServerPlayer late = northOfTheColumn(helper, king, origin, 26.5);
			fight.hurt(late, 4.0F);  // struck by his fight, but no part in it
			boolean idle = fight.share(late) == null;
			chargeAt(level, king, origin, late);
			helper.runAfterDelay(run, () -> {
				ending(level, king, () -> {
					try {
						helper.assertTrue(idle, "The late arrival already had a part in his fight");
						helper.assertTrue(king.stunnedTicks() > 0 && king.action() == YetiKingEntity.Action.STUNNED,
								"His charge at the late arrival did not end in the column: " + king.action());
						helper.assertTrue(fight.share(late) == null && !fight.qualified().contains(late.getUUID()),
								"A late arrival with no part earned a deed by where his charge ended");
						helper.assertTrue(deeds(fight, fighter) == 0, "The deed went to someone else");
						king.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
						helper.assertTrue(late.getInventory().countItem(JugcraftYeti.YETI_FUR) == 0, "The idle late arrival was paid");
						helper.assertTrue(fighter.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4, "The one who struck him was not paid");
					} finally {
						fighter.discard();
						late.discard();
					}
				});
				helper.succeed();
			});
		});
	}

	/** The game caps a creature's health at its max_health attribute's top (1024 in 26.3): a party's scale stops there. */
	private static double capped(double health) {
		return Attributes.MAX_HEALTH.value() instanceof RangedAttribute ranged ? Math.min(health, ranged.getMaxValue()) : health;
	}

	/**
	 * Concordance Chunk B and the party cap: woken by four players his health is two and a half times his own, and when
	 * he falls each of the four is paid once, a first kill's trophy and a quarter of the experience; his fall reported
	 * again pays no one twice.
	 */
	@GameTest(structure = HALL, padding = 8)
	public void fourPlayersAreEachPaidOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 34, 34, 45, 41);
		List<ServerPlayer> party = List.of(player(helper, king, -3.5, 3.0), player(helper, king, -1.5, 3.0), player(helper, king, 0.5, 3.0),
				player(helper, king, 2.5, 3.0));
		king.wake(level);
		ending(level, king, () -> {
			try {
				helper.assertTrue(Math.abs(king.getMaxHealth() - capped(YetiKingEntity.HEALTH * YetiKingEntity.partyScale(4))) < 0.01,
						"Woken by four, his health is not " + YetiKingEntity.partyScale(4) + " times his own, at most the game's cap: " + king.getMaxHealth());
				int[] before = new int[party.size()];
				for (int i = 0; i < party.size(); i++) {
					king.took(party.get(i), 5.0F);
					before[i] = party.get(i).totalExperience;
				}
				king.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				int[] gained = new int[party.size()];
				for (int i = 0; i < party.size(); i++) {
					ServerPlayer player = party.get(i);
					gained[i] = player.totalExperience - before[i];
					helper.assertTrue(player.getInventory().countItem(JugcraftYeti.YETI_FUR) >= 4 && trophies(player) == 1
							&& gained[i] == YetiKingEntity.EXPERIENCE / 4, "A player of the four was not paid once: " + gained[i] + " experience");
				}
				king.die(level.damageSources().genericKill());
				for (int i = 0; i < party.size(); i++) {
					ServerPlayer player = party.get(i);
					helper.assertTrue(trophies(player) == 1 && player.totalExperience - before[i] == gained[i], "His fall reported again paid someone twice");
				}
			} finally {
				party.forEach(ServerPlayer::discard);
			}
		});
		helper.succeed();
	}

	/**
	 * Concordance Chunk B: his fight's record is not saved. Saved awake and loaded again, he holds no one's part, and on
	 * his first ticks he begins a fresh record, so who takes part from then on is counted.
	 */
	@GameTest(structure = HALL, padding = 8, maxTicks = 40)
	public void aFightLoadedAwakeIsRecordedAfresh(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		YetiKingEntity king = waiting(helper, origin);
		floor(level, origin, 34, 34, 45, 41);
		ServerPlayer striker = player(helper, king, 0.5, 3.0);
		striker.setGameMode(GameType.CREATIVE);  // he pays them no heed
		king.wake(level);
		king.took(striker, 4.0F);
		boolean before = king.encounter().qualified().contains(striker.getUUID());
		// Saved awake and loaded again, as his chunk unloading and loading would.
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		king.saveWithoutId(out);
		king.discard();
		YetiKingEntity loaded = new YetiKingEntity(JugcraftYeti.YETI_KING, level);
		loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
		loaded.setUUID(UUID.randomUUID());
		level.addFreshEntity(loaded);
		boolean unsaved = loaded.encounter().share(striker) == null && !loaded.encounter().underway();
		helper.runAfterDelay(3, () -> {
			ending(level, loaded, () -> {
				try {
					helper.assertTrue(before && unsaved, "His record was saved with him, or the striker never had a part");
					helper.assertTrue(loaded.phase() == YetiKingEntity.Phase.HUNT, "Loaded waking, he does not hunt: " + loaded.phase());
					helper.assertTrue(loaded.encounter().underway() && loaded.encounter().share(striker) == null,
							"Loaded awake, he did not begin a fresh record");
					loaded.took(striker, 2.0F);
					helper.assertTrue(loaded.encounter().qualified().contains(striker.getUUID()), "A blow after loading was not counted");
				} finally {
					striker.discard();
				}
			});
			helper.succeed();
		});
	}

	/**
	 * His JEI pages (tools/acquisition.py; Concordance Chunk B, B-02) say what the server rolls, with the default data: each
	 * share holds 4 to 8 Yeti Fur and has a 15% chance of one of his two trophies (7.5% each, never both), a 25% chance of
	 * the Yeti Mitten and a 20% chance of his Crown. A player who has already won Abominable (so no first-kill trophy)
	 * rolls {@value #SHARES} shares: none holds both trophies, and each count lies within five standard deviations of its
	 * chance (a pool's chance ignored, or his trophies split into two pools, falls outside). check_acquisition holds the
	 * same table to the pages' words; the first kill's trophy is hisLootIsEachParticipantsOwn's.
	 */
	@GameTest(maxTicks = 20)
	public void hisLootIsWhatHisPagesSay(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer veteran = helper.makeMockServerPlayerInLevel();
		TrickOrTreat.award(veteran, YetiKingLoot.ADVANCEMENT);
		helper.assertFalse(YetiKingLoot.firstKill(veteran), "A player who has won Abominable rolls no first-kill trophy");
		Vec3 origin = helper.absoluteVec(new Vec3(1.5, 2.0, 1.5));
		int maul = 0;
		int rimeclaw = 0;
		int mitten = 0;
		int crown = 0;
		try {
			for (int share = 0; share < SHARES; share++) {
				List<ItemStack> loot = YetiKingLoot.roll(level, origin, veteran);
				int fur = loot.stream().filter(stack -> stack.is(JugcraftYeti.YETI_FUR)).mapToInt(ItemStack::getCount).sum();
				helper.assertTrue(fur >= 4 && fur <= 8, fur + " Yeti Fur in a share, not 4 to 8");
				long trophies = loot.stream().filter(YetiKingLoot::trophy).count();
				helper.assertTrue(trophies <= 1, "A share held " + trophies + " trophies: his two share one pool");
				maul += (int) loot.stream().filter(stack -> stack.is(JugcraftYeti.glacierMaul())).count();
				rimeclaw += (int) loot.stream().filter(stack -> stack.is(JugcraftYeti.rimeclaw())).count();
				mitten += (int) loot.stream().filter(stack -> stack.is(JugcraftYeti.YETI_MITTEN)).count();
				crown += (int) loot.stream().filter(stack -> stack.is(JugcraftYeti.YETI_KING_CROWN)).count();
			}
		} finally {
			veteran.discard();
		}
		helper.assertTrue(within(maul + rimeclaw, 0.15), (maul + rimeclaw) + " trophies in " + SHARES + " shares, not about 15%");
		helper.assertTrue(within(maul, 0.075) && within(rimeclaw, 0.075), maul + " Glacier Mauls and " + rimeclaw + " Rimeclaws, not about 7.5% each");
		helper.assertTrue(within(mitten, 0.25), mitten + " Yeti Mittens, not about 25%");
		helper.assertTrue(within(crown, 0.2), crown + " Crowns, not about 20%");
		helper.succeed();
	}
}
