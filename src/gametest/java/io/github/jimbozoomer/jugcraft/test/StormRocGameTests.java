package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder;
import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder.Rod;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.StormRodBlock;
import io.github.jimbozoomer.jugcraft.lair.dreadnought.GroundingStrap;
import io.github.jimbozoomer.jugcraft.lair.dreadnought.JugcraftDreadnought;
import io.github.jimbozoomer.jugcraft.lair.roc.GaleFledglingEntity;
import io.github.jimbozoomer.jugcraft.lair.roc.JugcraftRoc;
import io.github.jimbozoomer.jugcraft.lair.roc.RocLoot;
import io.github.jimbozoomer.jugcraft.lair.roc.StormRocEntity;
import io.github.jimbozoomer.jugcraft.lair.roc.WeatherVaneLens;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.phys.Vec3;

/**
 * The Storm Roc (docs/features/storm-roc.md), as far as a game-test server can show him: one put down on his own (with no
 * lair instance, over a Crown laid out from beyond the corner of an arena the summit's size, so his nest, the summit and
 * where he dives from are all loaded; each test lays the summit's floor and the rods it needs) waits in his nest until a
 * player who may fight him stands on the summit, then glides down to it; his health grows with the party, and lightning
 * never harms him; a charged rod between him and his marked foe grounds him, stunned and harmed half as much again by every
 * blow, and a lane far from it does not, the rod discharging; his Wing Buffet, Lightning Marks (which the Grounding Strap
 * halves) and Wind Strips (a boulder upwind shelters); at half health the Squall Rises, unhurt, and his fledglings leave
 * the nest, never more than three; below a fifth, the Eye of the Storm; he keeps to the summit; left alone he flies back to
 * his nest; his loot is each participant's own; the Weather-Vane Lens reads the weather's clock; and his feathers' recipes.
 * His fight on the Crown itself runs in a real world, in {@link StormRocClientGameTests}.
 *
 * <p>He fights any player within 38 blocks of the summit's middle; the arena keeps every other test's players farther off
 * than that. Each test sends him back to his nest, with his fledglings, and takes him away.
 */
public class StormRocGameTests {
	/** An arena of air (78 x 14 x 78), as big as the summit with where he dives from and his nest. */
	private static final String ARENA = "jugcraft-test:crypt_hall";
	/** A new player cannot be hurt until its client has loaded the world, or for 60 ticks; a mock player has none. */
	private static final int LOADING_TICKS = 80;
	/** A creature hurt shrugs off a blow no harder than the last for 10 ticks after (vanilla's). */
	private static final int HURT_COOLDOWN = 12;
	/** A tick or two past his waking, when he has glided down to the summit. */
	private static final int AWAKE = StormRocEntity.WAKE_TICKS + 5;
	/** From the start of a dive until his lane holds and he dives. */
	private static final int TELL = StormRocEntity.CHARGE_TICKS + StormRocEntity.LANE_TICKS;

	/** The origin of a Crown laid out so its summit lies in the arena: the summit's floor two blocks over the arena's. */
	private static BlockPos origin(GameTestHelper helper) {
		return helper.absolutePos(new BlockPos(7, 2 - CrownOfThunder.STAND, 7));
	}

	/** The summit's floor in every cell whose middle is on it. */
	private static void summit(ServerLevel level, BlockPos origin) {
		BlockState floor = Blocks.STONE.defaultBlockState();
		for (int x = 12; x <= 53; x++) {
			for (int z = 12; z <= 53; z++) {
				if (CrownOfThunder.onSummit(x + 0.5, z + 0.5)) {
					level.setBlock(origin.offset(x, CrownOfThunder.PAVE, z), floor, Block.UPDATE_CLIENTS);
				}
			}
		}
	}

	/** A Storm Rod on its plinth, as the Crown's: chiseled stone under it, its base, middle and tip. */
	private static void rod(ServerLevel level, BlockPos origin, Rod rod) {
		BlockPos base = rod.base(origin);
		level.setBlock(base.below(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
		StormRodBlock.Part[] parts = StormRodBlock.Part.values();
		for (int i = 0; i < parts.length; i++) {
			level.setBlock(base.above(i), JugcraftLairs.STORM_ROD.defaultBlockState().setValue(StormRodBlock.PART, parts[i]), Block.UPDATE_CLIENTS);
		}
	}

	/** A survival player standing on the summit at the template's ({@code x}, {@code z}), shielded by Resistance. */
	private static ServerPlayer player(GameTestHelper helper, BlockPos origin, double x, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.snapTo(origin.getX() + x, origin.getY() + CrownOfThunder.STAND, origin.getZ() + z, 0.0F, 0.0F);
		return player;
	}

	private static void shield(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 2, false, false));
	}

	/** Puts him on the summit at the template's ({@code x}, {@code z}), facing south. */
	private static void stand(StormRocEntity roc, double x, double z) {
		BlockPos origin = roc.origin();
		roc.teleportTo(origin.getX() + x, roc.floorY(), origin.getZ() + z);
		roc.setYRot(0.0F);
	}

	private static boolean hurt(ServerPlayer player) {
		return player.getHealth() < player.getMaxHealth();
	}

	/** Checks {@code body}, then sends him back to his nest and takes him away whether it passed or failed. */
	private static void ending(ServerLevel level, StormRocEntity roc, Runnable body) {
		try {
			body.run();
		} finally {
			roc.reset(level);
			roc.discard();
		}
	}

	private static String key(Component component) {
		return component.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : component.getString();
	}

	/**
	 * He waits in his nest, unhurt by anything, and a creative player's blow does not wake him; a player who may fight him
	 * on the summit does: unhurt while he rises, {@value StormRocEntity#WAKE_TICKS} ticks later he has glided down to the
	 * summit at full health, scaled for one player.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void heWaitsInHisNestAndAPlayerOnTheSummitWakesHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer player = player(helper, origin, 40.5, 40.5);
		player.setGameMode(GameType.CREATIVE);
		helper.runAfterDelay(20, () -> {
			boolean waited = roc.phase() == StormRocEntity.Phase.WAITING && roc.position().distanceTo(roc.waiting()) < 0.01
					&& roc.action() == StormRocEntity.Action.PERCH;
			boolean harmless = !roc.hurtServer(level, level.damageSources().playerAttack(player), 10.0F) && roc.getHealth() == roc.getMaxHealth();
			boolean slept = roc.phase() == StormRocEntity.Phase.WAITING;
			player.setGameMode(GameType.SURVIVAL);
			helper.runAfterDelay(12, () -> {
				boolean woken = roc.phase() == StormRocEntity.Phase.WAKING && roc.getHealth() == roc.getMaxHealth();
				boolean unhurt = !roc.hurtServer(level, level.damageSources().playerAttack(player), 10.0F);
				player.setGameMode(GameType.CREATIVE);
				helper.runAfterDelay(StormRocEntity.WAKE_TICKS + 15, () -> {
					ending(level, roc, () -> {
						helper.assertTrue(waited, "He did not wait in his nest, perched");
						helper.assertTrue(harmless, "Waiting, he was harmed");
						helper.assertTrue(slept, "A creative player's blow woke him");
						helper.assertTrue(woken, "A player on the summit did not wake him at full health");
						helper.assertTrue(unhurt, "Waking, he was harmed");
						helper.assertTrue(roc.getMaxHealth() == (float) (StormRocEntity.HEALTH * StormRocEntity.partyScale(1)),
								"Woken by one player, his health is " + roc.getMaxHealth());
						helper.assertTrue(roc.phase() == StormRocEntity.Phase.GATHERING, "He did not come down to fight: " + roc.phase());
						Vec3 woke = roc.woke();
						helper.assertTrue(Math.abs(roc.getY() - roc.floorY()) < 0.1 && Math.hypot(roc.getX() - woke.x, roc.getZ() - woke.z) < 6.0,
								"He did not come down on the summit where he wakes but to " + roc.position().subtract(Vec3.atLowerCornerOf(origin)));
						player.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * His health grows by half for each player after the first, at most two and a half times; a player's blow from afar
	 * wakes him, unhurt. Lightning and electric arcs never harm him; a blow does, and counts as taking part.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 120)
	public void hisHealthGrowsWithThePartyAndLightningNeverHarmsHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer far = player(helper, origin, 32.5, 50.5);
		ServerPlayer other = player(helper, origin, 34.5, 50.5);
		helper.assertTrue(StormRocEntity.partyScale(1) == 1.0 && StormRocEntity.partyScale(2) == 1.5 && StormRocEntity.partyScale(4) == 2.5
				&& StormRocEntity.partyScale(9) == 2.5, "His health scales with the party, at most 2.5 times");
		boolean blow = roc.hurtServer(level, level.damageSources().playerAttack(far), 10.0F);
		helper.assertTrue(!blow && roc.phase() == StormRocEntity.Phase.WAKING, "A player's blow from afar did not wake him, unhurt");
		helper.assertTrue(roc.getMaxHealth() == (float) (StormRocEntity.HEALTH * StormRocEntity.partyScale(2)),
				"Woken with two players by the summit, his health is " + roc.getMaxHealth());
		helper.runAfterDelay(AWAKE, () -> {
			ending(level, roc, () -> {
				float before = roc.getHealth();
				helper.assertTrue(!roc.hurtServer(level, level.damageSources().lightningBolt(), 10.0F) && roc.getHealth() == before,
						"Lightning harmed him");
				helper.assertTrue(!roc.hurtServer(level, GroundingStrap.arc(level, far, far), 10.0F) && roc.getHealth() == before,
						"An electric arc harmed him");
				boolean hurt = roc.hurtServer(level, level.damageSources().playerAttack(far), 10.0F);
				helper.assertTrue(hurt && roc.getHealth() < before, "A blow did not hurt him");
				helper.assertTrue(roc.participants(level).contains(far) && !roc.participants(level).contains(other),
						"Hurting him did not count as taking part, or standing by did");
				far.discard();
				other.discard();
			});
			helper.succeed();
		});
	}

	/**
	 * His rule: the north rod charges and the bar names it while he climbs over the summit's edge behind it; his lane
	 * follows his foe, who stands beyond the rod; he dives, and as he passes the rod it grounds him: the rod goes quiet, he
	 * lies stunned on the summit clear of its plinth, every blow does half as much again, and whoever stood by the rod takes
	 * part. Stunned, he cannot fly; {@value StormRocEntity#GROUNDED_TICKS} ticks later he rises.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 360)
	public void aChargedRodBetweenHimAndHisFoeGroundsHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		rod(level, origin, Rod.NORTH);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer foe = player(helper, origin, 32.5, 40.5);
		ServerPlayer helping = player(helper, origin, 34.5, 21.5);
		shield(foe);
		shield(helping);
		roc.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			foe.setHealth(foe.getMaxHealth());
			roc.dive(level, foe, Rod.NORTH, 0.0);
			boolean charging = roc.dive() == StormRocEntity.Dive.CHARGE && roc.charged() == Rod.NORTH
					&& CrownOfThunder.charged(level, origin) == Rod.NORTH;
			Vec3 perch = roc.perch();
			helper.runAfterDelay(StormRocEntity.CHARGE_TICKS + 2, () -> {
				boolean marked = roc.dive() == StormRocEntity.Dive.LANE && roc.position().distanceTo(perch) < 1.0;
				helper.runAfterDelay(StormRocEntity.LANE_TICKS + 20, () -> {
					boolean grounded = roc.dive() == StormRocEntity.Dive.GROUNDED && CrownOfThunder.charged(level, origin) == null
							&& Math.abs(roc.getY() - roc.floorY()) < 0.2;
					Vec3 middle = roc.rod(Rod.NORTH);
					boolean clear = Math.hypot(roc.getX() - middle.x, roc.getZ() - middle.z) >= 2.5;
					boolean spared = !hurt(foe);
					float before = roc.getHealth();
					boolean doubled = roc.hurtServer(level, level.damageSources().indirectMagic(foe, foe), 4.0F)
							&& Math.abs(before - roc.getHealth() - 4.0F * StormRocEntity.GROUNDED_MULTIPLIER) < 1.0E-3F;
					boolean helped = roc.participants(level).contains(helping);
					helper.runAfterDelay(StormRocEntity.GROUNDED_TICKS, () -> {
						ending(level, roc, () -> {
							helper.assertTrue(perch.distanceTo(roc.perch(Rod.NORTH, 0.0)) < 1.0E-6
									&& Math.abs(perch.y - roc.floorY() - StormRocEntity.PERCH_UP) < 1.0E-6, "He did not climb behind the north rod");
							helper.assertTrue(charging, "The dive did not charge the north rod");
							helper.assertTrue(marked, "He did not hold over the edge while his lane followed his foe");
							helper.assertTrue(grounded, "Passing the charged rod, he was not grounded on the summit: " + roc.dive());
							helper.assertTrue(clear, "Grounded, he lies on the rod's plinth");
							helper.assertTrue(spared, "Grounded before he reached them, his foe was struck");
							helper.assertTrue(doubled, "Grounded, a blow did not do half as much again");
							helper.assertTrue(helped, "Whoever stood by the rod that grounded him did not take part");
							helper.assertTrue(roc.dive() == StormRocEntity.Dive.NONE && roc.diveTimer() > roc.diveEvery() - 40,
									"After his stun he did not rise, his next dive a full while off: " + roc.dive());
							foe.discard();
							helping.discard();
						});
						helper.succeed();
					});
				});
			});
		});
	}

	/**
	 * A lane far from the charged rod does not ground him: he dives down it, striking his foe in it, lands on the summit,
	 * and the rod discharges harmlessly. Where a lane would ground him is where it passes within
	 * {@value StormRocEntity#GROUND_RADIUS} of the rod, before its end.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 360)
	public void aLaneFarFromTheRodDoesNotGroundHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		rod(level, origin, Rod.NORTH);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer foe = player(helper, origin, 46.5, 36.5);
		shield(foe);
		Vec3 rod = roc.rod(Rod.NORTH);
		Vec3 from = roc.perch(Rod.NORTH, 0.0);
		helper.assertTrue(StormRocEntity.groundingAt(from, rod.add(0.0, 0.0, 10.0), rod) > 0.0, "A lane through the rod does not ground him");
		// Ten blocks east and ten past it: the lane passes the rod some three and a half blocks off.
		helper.assertTrue(StormRocEntity.groundingAt(from, rod.add(10.0, 0.0, 10.0), rod) < 0.0, "A lane passing the rod too far off grounds him");
		helper.assertTrue(StormRocEntity.groundingAt(from, from.add(0.0, 0.0, 3.0), rod) < 0.0, "A lane ending before the rod grounds him");
		roc.wake(level);
		helper.runAfterDelay(Math.max(AWAKE, LOADING_TICKS), () -> {
			foe.setHealth(foe.getMaxHealth());
			roc.dive(level, foe, Rod.NORTH, 0.0);
			helper.runAfterDelay(TELL + 40, () -> {
				ending(level, roc, () -> {
					helper.assertTrue(roc.groundAt() < 0.0, "A lane far from the rod was set to ground him");
					helper.assertTrue(roc.dive() == StormRocEntity.Dive.NONE && Math.abs(roc.getY() - roc.floorY()) < 0.2,
							"Diving past the rod, he did not land on the summit: " + roc.dive());
					helper.assertTrue(CrownOfThunder.charged(level, origin) == null, "The rod did not discharge");
					helper.assertTrue(hurt(foe), "His dive did not strike his foe in its lane");
					foe.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * His Wing Buffet strikes before him and throws back, not behind him; his Lightning Marks strike whoever stands in a
	 * ring, not outside, and the Grounding Strap halves their harm; his Wind Strips push whoever is in one the way the wind
	 * blows, unless a boulder stands upwind of them, and nobody outside them.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void hisBuffetMarksAndWindStrike(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer front = player(helper, origin, 32.5, 36.5);
		ServerPlayer behind = player(helper, origin, 32.5, 28.5);
		ServerPlayer strapped = player(helper, origin, 40.5, 40.5);
		ServerPlayer bare = player(helper, origin, 24.5, 40.5);
		ServerPlayer away = player(helper, origin, 32.5, 46.5);
		strapped.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftDreadnought.GROUNDING_STRAP));
		// Held still (no AI: he neither wakes nor attacks on his own), so only what the test asks of him strikes.
		roc.setNoAi(true);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			ending(level, roc, () -> {
				stand(roc, 32.5, 32.5);
				roc.setYRot(0.0F);
				int buffeted = roc.buffet(level);
				helper.assertTrue(buffeted >= 1 && hurt(front) && !hurt(behind), "His Wing Buffet did not strike before him only");
				helper.assertTrue(front.getDeltaMovement().z > 0.5, "His Wing Buffet did not throw back: " + front.getDeltaMovement());
				roc.marks(List.of(new Vec3(strapped.getX(), roc.floorY(), strapped.getZ()), new Vec3(bare.getX(), roc.floorY(), bare.getZ())));
				int struck = roc.strikeMarks(level);
				float strappedLoss = strapped.getMaxHealth() - strapped.getHealth();
				float bareLoss = bare.getMaxHealth() - bare.getHealth();
				helper.assertTrue(struck == 2 && !hurt(away), "His marks struck " + struck + " players, or one outside a ring");
				helper.assertTrue(bareLoss > 0.0F && Math.abs(strappedLoss - bareLoss * GroundingStrap.SHARE) < 0.51F,
						"The Grounding Strap did not halve a mark's harm: " + strappedLoss + " against " + bareLoss);
				// The wind blows east along a strip through the player away from it all; a boulder sheltering one.
				ServerPlayer gusted = player(helper, origin, 30.5, 46.5);
				ServerPlayer lee = player(helper, origin, 36.5, 46.5);
				level.setBlock(BlockPos.containing(lee.getX() - 1.5, lee.getY(), lee.getZ()), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
				roc.strips(new Vec3(1.0, 0.0, 0.0), List.of(gusted.position()));
				gusted.setDeltaMovement(Vec3.ZERO);
				front.setDeltaMovement(Vec3.ZERO);
				lee.setDeltaMovement(Vec3.ZERO);
				int pushed = roc.blow(level);
				helper.assertTrue(gusted.getDeltaMovement().x > 0.0 && roc.inStrip(gusted.getX(), gusted.getZ(), 0.0), "The wind did not push along its strip");
				helper.assertTrue(lee.getDeltaMovement().x == 0.0 && roc.sheltered(level, lee, roc.wind()), "A boulder upwind did not shelter");
				helper.assertTrue(front.getDeltaMovement().x == 0.0 && !roc.inStrip(front.getX(), front.getZ(), 0.0), "The wind pushed outside its strips");
				helper.assertTrue(pushed >= 1, "The wind pushed nobody");
				List.of(front, behind, strapped, bare, away, gusted, lee).forEach(ServerPlayer::discard);
			});
			helper.succeed();
		});
	}

	/**
	 * At half health the Squall Rises: he climbs and circles, unhurt, and three fledglings leave the nest; then the Squall,
	 * his dives oftener. A fledgling pecks his foe, and whoever hurts one takes part; no more than three come. Below a fifth,
	 * the Eye of the Storm: oftener still, and five marks.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 300)
	public void theSquallRisesAndHisFledglingsCome(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer foe = player(helper, origin, 32.5, 40.5);
		shield(foe);
		roc.wake(level);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			roc.setHealth(roc.getMaxHealth() * StormRocEntity.SQUALL_AT - 1.0F);
			roc.squall(level);
			boolean rising = roc.phase() == StormRocEntity.Phase.RISING && !roc.hurtServer(level, level.damageSources().playerAttack(foe), 5.0F);
			int fledglings = roc.fledglings().size();
			int more = roc.callFledglings(level);
			helper.runAfterDelay(StormRocEntity.SQUALL_TICKS + 5, () -> {
				boolean squall = roc.phase() == StormRocEntity.Phase.SQUALL && roc.diveEvery() == StormRocEntity.DIVE_EVERY_SQUALL
						&& Math.abs(roc.getY() - roc.floorY()) < 0.2;
				GaleFledglingEntity fledgling = null;
				for (UUID id : roc.fledglings()) {
					if (level.getEntity(id) instanceof GaleFledglingEntity found) {
						fledgling = found;
					}
				}
				GaleFledglingEntity young = fledgling;
				boolean pecked = young != null && young.peck(level, foe) && hurt(foe);
				ServerPlayer hunter = player(helper, origin, 30.5, 40.5);
				boolean counted = young != null && young.hurtServer(level, level.damageSources().playerAttack(hunter), 2.0F)
						&& roc.participants(level).contains(hunter);
				roc.setHealth(roc.getMaxHealth() * StormRocEntity.EYE_AT - 1.0F);
				helper.runAfterDelay(HURT_COOLDOWN, () -> {
					ending(level, roc, () -> {
						helper.assertTrue(rising, "At half health he did not rise into the Squall, unhurt: " + roc.phase());
						helper.assertTrue(fledglings == StormRocEntity.FLEDGLING_CAP && more == 0, fledglings + " fledglings left the nest, then "
								+ more + " more; at most " + StormRocEntity.FLEDGLING_CAP);
						helper.assertTrue(squall, "The Squall did not begin on the summit, his dives oftener");
						helper.assertTrue(pecked, "A fledgling did not peck his foe");
						helper.assertTrue(counted, "Hurting a fledgling did not count as taking part");
						helper.assertTrue(roc.phase() == StormRocEntity.Phase.EYE && roc.markCount() == StormRocEntity.EYE_MARKS
								&& roc.diveEvery() == StormRocEntity.DIVE_EVERY_EYE, "Below a fifth the Eye of the Storm did not come: " + roc.phase());
						foe.discard();
						hunter.discard();
					});
					helper.assertTrue(roc.fledglings().isEmpty(), "His fledglings outlived his reset");
					helper.succeed();
				});
			});
		});
	}

	/** He keeps to the summit: set down beyond his leash, he is set back on it. */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 100)
	public void heKeepsToTheSummit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		roc.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			Vec3 centre = roc.centre();
			roc.teleportTo(centre.x + StormRocEntity.LEASH + 4.0, centre.y, centre.z);
			helper.runAfterDelay(2, () -> {
				ending(level, roc, () -> helper.assertTrue(roc.position().distanceTo(centre) <= StormRocEntity.LEASH,
						"Strayed from the summit, he was not set back: " + roc.position()));
				helper.succeed();
			});
		});
	}

	/**
	 * Left alone {@value StormRocEntity#ABANDON_TICKS} ticks he flies back to his nest, healed: his fledglings gone, every
	 * rod quiet, and who hurt him forgotten.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 400)
	public void leftAloneHeFliesBackToHisNest(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		rod(level, origin, Rod.EAST);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer gone = player(helper, origin, 32.5, 40.5);
		gone.setGameMode(GameType.CREATIVE);
		roc.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			roc.took(gone, 5.0F);
			roc.setHealth(roc.getMaxHealth() * 0.8F);
			roc.callFledglings(level);
			CrownOfThunder.charge(level, origin, Rod.EAST, true);
			boolean counted = roc.participants(level).contains(gone);
			helper.runAfterDelay(StormRocEntity.ABANDON_TICKS + 20, () -> {
				helper.assertTrue(counted, "Whoever hurt him was not counted");
				helper.assertTrue(roc.phase() == StormRocEntity.Phase.WAITING && roc.position().distanceTo(roc.waiting()) < 0.1,
						"Left alone he did not fly back to his nest: " + roc.phase() + " at " + roc.position());
				helper.assertTrue(roc.getHealth() == roc.getMaxHealth() && roc.action() == StormRocEntity.Action.PERCH,
						"Back in his nest he is not healed and perched");
				helper.assertTrue(roc.fledglings().isEmpty() && CrownOfThunder.charged(level, origin) == null, "His fledglings or a charged rod outlived it");
				helper.assertTrue(roc.participants(level).isEmpty(), "Who hurt him was not forgotten");
				roc.discard();
				gone.discard();
				helper.succeed();
			});
		});
	}

	/**
	 * His loot is each participant's own: everyone who struck him rolls for themselves (Roc Feathers, and on a first kill
	 * always one of his trophies) into their own inventory with a share of his experience, and a bystander who took no part
	 * gets nothing. Afterwards Eye of the Storm is theirs, and a later roll no longer counts as a first kill. As he falls the
	 * Grey Mist opens at the foot of his crag: the way home.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 60)
	public void hisLootIsEachParticipantsOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		summit(level, origin);
		StormRocEntity roc = StormRocEntity.summon(level, origin, null);
		ServerPlayer first = player(helper, origin, 30.5, 36.5);
		ServerPlayer second = player(helper, origin, 34.5, 36.5);
		ServerPlayer bystander = player(helper, origin, 32.5, 46.5);
		roc.took(first, 10.0F);
		roc.took(second, 4.0F);
		ending(level, roc, () -> {
			List<ServerPlayer> participants = roc.participants(level);
			helper.assertTrue(participants.contains(first) && participants.contains(second), "Both who struck him take part");
			helper.assertFalse(participants.contains(bystander), "A bystander who took no part does");
			helper.assertTrue(RocLoot.firstKill(first), "A newcomer has already won Eye of the Storm");
			List<ItemStack> loot = RocLoot.roll(level, roc, first);
			int feathers = loot.stream().filter(stack -> stack.is(JugcraftRoc.ROC_FEATHER)).mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(feathers >= 2 && feathers <= 3, feathers + " Roc Feathers, not 2 to 3");
			helper.assertTrue(loot.stream().filter(RocLoot::trophy).count() == 1, "A first kill always brings one of his trophies");
			roc.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			helper.assertTrue(roc.isDeadOrDying(), "He did not fall");
			for (BlockPos cell : CrownOfThunder.fallExitCells()) {
				helper.assertTrue(level.getBlockState(origin.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist at the foot of his crag at " + cell);
			}
			int share = StormRocEntity.EXPERIENCE / 2;
			for (ServerPlayer player : List.of(first, second)) {
				helper.assertTrue(player.getInventory().countItem(JugcraftRoc.ROC_FEATHER) >= 2, "A participant's loot did not go into their inventory");
				int trophies = player.getInventory().countItem(JugcraftRoc.stormcaller()) + player.getInventory().countItem(JugcraftRoc.galefeather());
				helper.assertTrue(trophies == 1, "A first kill brought " + trophies + " of his trophies, not 1");
				helper.assertTrue(player.totalExperience >= share, "A participant got " + player.totalExperience + " experience, not " + share);
				helper.assertFalse(RocLoot.firstKill(player), "Eye of the Storm is not theirs");
			}
			helper.assertTrue(bystander.getInventory().countItem(JugcraftRoc.ROC_FEATHER) == 0 && bystander.totalExperience == 0, "The bystander got loot");
			helper.assertTrue(RocLoot.firstKill(bystander), "The bystander won Eye of the Storm");
			for (BlockPos cell : CrownOfThunder.fallExitCells()) {
				level.setBlock(origin.offset(cell), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			List.of(first, second, bystander).forEach(ServerPlayer::discard);
		});
		helper.succeed();
	}

	/**
	 * The Weather-Vane Lens held in the offhand reads the weather's clock: while a clear spell lasts, fair for that long; then
	 * how long until it rains. Not held, it tells nothing. Minutes are whole, at least one.
	 */
	@GameTest(maxTicks = 20)
	public void theWeatherVaneLensReadsTheWeather(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = helper.makeMockServerPlayerInLevel();
		BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
		holder.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
		WeatherData weather = level.getWeatherData();
		int clear = weather.getClearWeatherTime();
		int rain = weather.getRainTime();
		try {
			helper.assertTrue(WeatherVaneLens.tell(holder) == null && !WeatherVaneLens.holds(holder), "With nothing in the offhand, the lens told something");
			holder.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftRoc.WEATHER_VANE_LENS));
			weather.setClearWeatherTime(2400);
			Component fair = WeatherVaneLens.tell(holder);
			helper.assertTrue(fair != null && key(fair).equals("message.jugcraft.storm_roc.lens.fair"), "In a clear spell the lens did not say fair: " + fair);
			weather.setClearWeatherTime(0);
			weather.setRainTime(6000);
			if (!level.isRaining()) {
				Component coming = WeatherVaneLens.forecast(level);
				helper.assertTrue(key(coming).equals("message.jugcraft.storm_roc.lens.rain_in"), "Dry, the lens did not say when it rains: " + coming);
			}
			helper.assertTrue(WeatherVaneLens.minutes(6000) == 5 && WeatherVaneLens.minutes(1) == 1 && WeatherVaneLens.minutes(2400) == 2,
					"The lens's minutes are not whole and at least one");
		} finally {
			weather.setClearWeatherTime(clear);
			weather.setRainTime(rain);
			holder.discard();
		}
		helper.succeed();
	}

	/** A Roc Feather is four feathers; two of them, three copper ingots and a compass make a Thunder Vane without its lightning rod. */
	@GameTest
	public void hisFeathersRecipes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CraftingInput one = CraftingInput.of(1, 1, List.of(new ItemStack(JugcraftRoc.ROC_FEATHER)));
		ItemStack feathers = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, one, level)
				.orElseThrow(() -> helper.assertionException("A Roc Feather crafts nothing")).value().assemble(one);
		helper.assertTrue(feathers.is(Items.FEATHER) && feathers.getCount() == 4, "A Roc Feather made " + feathers);
		ItemStack roc = new ItemStack(JugcraftRoc.ROC_FEATHER);
		ItemStack copper = new ItemStack(Items.COPPER_INGOT);
		CraftingInput grid = CraftingInput.of(3, 3, List.of(roc, ItemStack.EMPTY, roc.copy(), copper, new ItemStack(Items.COMPASS), copper.copy(),
				ItemStack.EMPTY, copper.copy(), ItemStack.EMPTY));
		ItemStack vane = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
				.orElseThrow(() -> helper.assertionException("Two Roc Feathers, copper and a compass craft nothing")).value().assemble(grid);
		helper.assertTrue(vane.is(JugcraftLairs.THUNDER_VANE) && vane.getCount() == 1, "Two Roc Feathers, copper and a compass made " + vane);
		helper.succeed();
	}
}
