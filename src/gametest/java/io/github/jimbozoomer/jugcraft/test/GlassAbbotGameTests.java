package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.abbot.AbbotLoot;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassAbbotEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassImageEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.JugcraftAbbot;
import io.github.jimbozoomer.jugcraft.lair.abbot.LanternLens;
import io.github.jimbozoomer.jugcraft.lair.abbot.MirrorInscription;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * The Glass Abbot (docs/features/glass-abbot.md), as far as a game-test server can show him: one put down on his own
 * (with no lair instance, over a chapel laid out from the middle of an arena of air; each test lays the floor of the nave
 * and the apse and stands the three mirrors at rest) kneels at his place until a player who may fight him steps into the
 * nave, then rises, unhurt as he wakes, his health scaled for the party; his mantle turns three quarters of every blow
 * until his own beam, sent home off both marked mirrors, cracks it, and then lets more through, and whoever turned a
 * marked mirror into place has taken part; a beam gone astray burns whoever stands on its path; his shards and his line
 * strike whoever stands in them; at half health the Veil falls, unhurt, and his images stand up, one blow shattering one,
 * and below a fifth the Breaking quickens his beam; left alone he kneels again, healed, his images gone and his mirrors at
 * rest; his loot is each participant's own; the Lantern Lens shows its holder invisible creatures and the true Abbot
 * among his images; the Mirror Inscription turns a small shot back under a ward; and his recipes. His fight in the Glass
 * Chapel itself runs in a real world, in {@link GlassAbbotClientGameTests}.
 *
 * <p>He fights any player within 34 blocks of the chapel's middle, which lies in the middle of the arena; the arena keeps
 * every other test's players farther off than that. Each test sends him back to his lectern, with his images, and takes
 * him away.
 */
public class GlassAbbotGameTests {
	/** An arena of air (78 x 78), as big as the chapel with room round it. */
	private static final String ARENA = "jugcraft-test:crypt_hall";
	/**
	 * A new player cannot be hurt until its client has loaded the world, or for 60 ticks; a mock player has no client, so
	 * a test that hurts one waits this long first.
	 */
	private static final int LOADING_TICKS = 80;
	/** A creature hurt shrugs off a blow no harder than the last for 10 ticks after (vanilla's); a test waits this long between blows. */
	private static final int HURT_COOLDOWN = 12;
	/** A tick or two past his waking, when he stands to fight. */
	private static final int AWAKE = GlassAbbotEntity.WAKE_TICKS + 5;

	/** The origin of a chapel laid out with its middle in the arena's: its floor two blocks over the arena's. */
	private static BlockPos origin(GameTestHelper helper) {
		return helper.absolutePos(new BlockPos(21, 2 - GlassChapel.STAND, 16));
	}

	/**
	 * The chapel's floor under the nave and the apse, and its three mirrors at rest where they stand: what his fight needs
	 * of it here.
	 */
	private static void chapel(ServerLevel level, BlockPos origin) {
		BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
		for (int x = 0; x < 35; x++) {
			for (int z = 0; z < 46; z++) {
				if (GlassChapel.inArena(x + 0.5, z + 0.5)) {
					level.setBlock(origin.offset(x, GlassChapel.STAND - 1, z), floor, Block.UPDATE_CLIENTS);
				}
			}
		}
		for (Mirror mirror : Mirror.values()) {
			BlockState rest = JugcraftLairs.CHAPEL_MIRROR.defaultBlockState().setValue(ChapelMirrorBlock.MIRROR, mirror);
			BlockPos base = mirror.base(origin);
			level.setBlock(base, rest.setValue(ChapelMirrorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
			level.setBlock(base.above(), rest.setValue(ChapelMirrorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
		}
	}

	/** He kneels at the lectern of the chapel laid out from {@code origin}. */
	private static GlassAbbotEntity kneeling(GameTestHelper helper, BlockPos origin) {
		chapel(helper.getLevel(), origin);
		return GlassAbbotEntity.summon(helper.getLevel(), origin, null);
	}

	/** A creative player (whom he does not fight) standing on the chapel's floor at the template's ({@code x}, {@code z}). */
	private static ServerPlayer player(GameTestHelper helper, BlockPos origin, double x, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.CREATIVE);
		player.snapTo(origin.getX() + x, origin.getY() + GlassChapel.STAND, origin.getZ() + z, 0.0F, 0.0F);
		return player;
	}

	/** {@code player} turns {@code mirror} until it stands at {@code stop}. */
	private static void turnTo(ServerLevel level, BlockPos origin, Mirror mirror, int stop, ServerPlayer player) {
		BlockPos base = mirror.base(origin);
		for (int i = 0; i < Mirror.STOPS && ChapelMirrorBlock.stop(level, base) != stop; i++) {
			ChapelMirrorBlock.turn(level, base, player);
		}
	}

	/** His Glass Images standing now. */
	private static List<GlassImageEntity> images(ServerLevel level, GlassAbbotEntity abbot) {
		List<GlassImageEntity> out = new ArrayList<>();
		for (UUID id : abbot.images()) {
			if (level.getEntity(id) instanceof GlassImageEntity found) {
				out.add(found);
			}
		}
		return out;
	}

	/** A blow from {@code player} that armour does not lessen, so his mantle's share of it can be read. */
	private static DamageSource blow(ServerLevel level, ServerPlayer player) {
		return level.damageSources().indirectMagic(player, player);
	}

	/** Checks {@code body}, then sends him back to his lectern and takes him away whether it passed or failed. */
	private static void ending(ServerLevel level, GlassAbbotEntity abbot, Runnable body) {
		try {
			body.run();
		} finally {
			abbot.reset(level);
			abbot.discard();
		}
	}

	/**
	 * He kneels at his place before the lectern, praying, unhurt by anything, and a creative player's blow does not wake
	 * him; a player who may fight him stepping into the nave does: he is unhurt while he wakes, and
	 * {@value GlassAbbotEntity#WAKE_TICKS} ticks later he stands to fight at full health, scaled for the one player.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void heKneelsAtHisLecternAndAPlayerInTheNaveWakesHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer player = player(helper, origin, 17.5, 20.5);
		helper.runAfterDelay(20, () -> {
			boolean knelt = abbot.phase() == GlassAbbotEntity.Phase.WAITING && abbot.position().distanceTo(abbot.place()) < 0.01
					&& abbot.action() == GlassAbbotEntity.Action.PRAY;
			boolean harmless = !abbot.hurtServer(level, level.damageSources().playerAttack(player), 10.0F)
					&& abbot.getHealth() == abbot.getMaxHealth();
			boolean slept = abbot.phase() == GlassAbbotEntity.Phase.WAITING;
			player.setGameMode(GameType.SURVIVAL);
			helper.runAfterDelay(12, () -> {
				boolean woken = abbot.phase() == GlassAbbotEntity.Phase.WAKING && abbot.getHealth() == abbot.getMaxHealth();
				boolean unhurt = !abbot.hurtServer(level, level.damageSources().playerAttack(player), 10.0F);
				// Woken, he has no foe to fight: he only walks.
				player.setGameMode(GameType.CREATIVE);
				helper.runAfterDelay(GlassAbbotEntity.WAKE_TICKS + 5, () -> {
					ending(level, abbot, () -> {
						helper.assertTrue(knelt, "He did not kneel at his place, praying");
						helper.assertTrue(harmless, "Kneeling, he was harmed");
						helper.assertTrue(slept, "A creative player's blow woke him");
						helper.assertTrue(woken, "A player in the nave did not wake him at full health");
						helper.assertTrue(unhurt, "Waking, he was harmed");
						helper.assertTrue(abbot.getMaxHealth() == (float) (GlassAbbotEntity.HEALTH * GlassAbbotEntity.partyScale(1)),
								"Woken by one player, his health is " + abbot.getMaxHealth());
						helper.assertTrue(abbot.phase() == GlassAbbotEntity.Phase.VESPERS, "He did not stand to fight: " + abbot.phase());
						helper.assertTrue(GlassAbbotEntity.partyScale(2) == 1.5 && GlassAbbotEntity.partyScale(9) == 2.5,
								"His health does not scale with the party, at most 2.5 times");
						player.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * His rule: two mirrors marked (their frames burn), whoever turns each to the pair's stop and he fires: his beam comes
	 * home off both and his mantle cracks; he kneels dazed, the marks go out, and blows that did a quarter of their harm now
	 * do more than all of it. Whoever turned a marked mirror into place has taken part, though they never struck him.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void hisMantleHoldsUntilHisOwnLightComesHome(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer striker = player(helper, origin, 17.5, 30.5);
		ServerPlayer turner = player(helper, origin, 20.5, 30.5);
		abbot.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			float before = abbot.getHealth();
			boolean whole = abbot.hurtServer(level, blow(level, striker), 8.0F)
					&& Math.abs(before - abbot.getHealth() - 8.0F * GlassAbbotEntity.MANTLE) < 1.0E-3F;
			abbot.mark(level, Mirror.EAST, Mirror.SOUTH, Mirror.EAST);
			BlockState east = level.getBlockState(Mirror.EAST.base(origin));
			boolean marked = abbot.beam() == GlassAbbotEntity.Beam.MARKED && east.getValue(ChapelMirrorBlock.MARKED)
					&& level.getBlockState(Mirror.SOUTH.base(origin)).getValue(ChapelMirrorBlock.MARKED)
					&& !level.getBlockState(Mirror.WEST.base(origin)).getValue(ChapelMirrorBlock.MARKED);
			// The turner may fight him for the moment it takes, so their turns count.
			turner.setGameMode(GameType.SURVIVAL);
			turnTo(level, origin, Mirror.EAST, Mirror.EAST.stopFor(Mirror.SOUTH), turner);
			turnTo(level, origin, Mirror.SOUTH, Mirror.SOUTH.stopFor(Mirror.EAST), turner);
			boolean home = abbot.fire(level);
			turner.setGameMode(GameType.CREATIVE);
			boolean cracked = abbot.cracked() && abbot.beam() == GlassAbbotEntity.Beam.CRACKED && abbot.action() == GlassAbbotEntity.Action.DAZED
					&& !level.getBlockState(Mirror.EAST.base(origin)).getValue(ChapelMirrorBlock.MARKED);
			helper.runAfterDelay(HURT_COOLDOWN, () -> {
				float mid = abbot.getHealth();
				boolean through = abbot.hurtServer(level, blow(level, striker), 8.0F)
						&& Math.abs(mid - abbot.getHealth() - 8.0F * GlassAbbotEntity.CRACKED_MULTIPLIER) < 1.0E-3F;
				ending(level, abbot, () -> {
					helper.assertTrue(whole, "His whole mantle did not turn three quarters of a blow");
					helper.assertTrue(marked, "Marking the east and south mirrors did not set their frames burning, and only theirs");
					helper.assertTrue(home, "With both marked mirrors at the pair's stops his beam did not come home");
					helper.assertTrue(cracked, "His light come home did not crack his mantle and daze him, or the marks stayed");
					helper.assertTrue(through, "Cracked, his mantle did not let more than the whole blow through");
					List<ServerPlayer> participants = abbot.participants(level);
					helper.assertTrue(participants.contains(striker) && participants.contains(turner),
							"Whoever struck him and whoever turned his mirrors into place do not both take part");
					striker.discard();
					turner.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * A beam with its mirrors left at rest goes astray: it does not come home, his mantle holds, and it burns whoever stands
	 * on its path; one who stands off it is not burned.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void aLostBeamBurnsAlongItsPathAndHisMantleHolds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		// On the beam's first leg, from his place to the east mirror; and well off it.
		ServerPlayer burned = player(helper, origin, (GlassChapel.ABBOT_X + Mirror.EAST.middleX()) / 2.0,
				(GlassChapel.ABBOT_Z + Mirror.EAST.middleZ()) / 2.0);
		ServerPlayer spared = player(helper, origin, 6.5, 32.5);
		abbot.wake(level);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			abbot.mark(level, Mirror.EAST, Mirror.SOUTH, Mirror.EAST);
			burned.setGameMode(GameType.SURVIVAL);
			spared.setGameMode(GameType.SURVIVAL);
			float burnedBefore = burned.getHealth();
			float sparedBefore = spared.getHealth();
			boolean home = abbot.fire(level);
			boolean burnt = burned.getHealth() < burnedBefore;
			boolean untouched = spared.getHealth() == sparedBefore;
			burned.setGameMode(GameType.CREATIVE);
			spared.setGameMode(GameType.CREATIVE);
			ending(level, abbot, () -> {
				helper.assertFalse(home, "With its mirrors at rest his beam came home");
				helper.assertTrue(!abbot.cracked() && abbot.mantle() == GlassAbbotEntity.MANTLE && abbot.beam() == GlassAbbotEntity.Beam.NONE,
						"A lost beam cracked his mantle, or is still under way");
				helper.assertTrue(abbot.beamPath().size() >= 3, "A lost beam's path is not drawn from mirror to mirror: " + abbot.beamPath());
				helper.assertTrue(burnt, "Standing on its path, a player was not burned");
				helper.assertTrue(untouched, "Standing off its path, a player was burned");
				burned.discard();
				spared.discard();
			});
			helper.succeed();
		});
	}

	/**
	 * His Shard Fan: three shards from his hand strike whoever they pass; his Prism Line: a lance down the line drawn at a
	 * player strikes them, and not one standing beside it.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 160)
	public void hisShardsAndHisLineStrikeWhoeverStandsInThem(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer target = player(helper, origin, 17.5, 16.5);
		ServerPlayer beside = player(helper, origin, 22.5, 16.5);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			// Kneeling, he wakes only on his own tick: everything here happens between two.
			target.setGameMode(GameType.SURVIVAL);
			float before = target.getHealth();
			abbot.shardFan(level, target.position().add(0.0, 1.0, 0.0));
			int hits = 0;
			for (int tick = 0; tick < GlassAbbotEntity.SHARD_LIFE && !abbot.shards().isEmpty(); tick++) {
				hits += abbot.flyShards(level);
			}
			int struck = hits;
			boolean shards = struck >= 1 && target.getHealth() < before && abbot.shards().isEmpty();
			target.setGameMode(GameType.CREATIVE);
			helper.runAfterDelay(HURT_COOLDOWN, () -> {
				target.setGameMode(GameType.SURVIVAL);
				beside.setGameMode(GameType.SURVIVAL);
				float mid = target.getHealth();
				float besideBefore = beside.getHealth();
				abbot.markPrism(target.position());
				int lanced = abbot.lance(level);
				boolean line = lanced == 1 && target.getHealth() < mid && beside.getHealth() == besideBefore
						&& Math.abs(abbot.prismFrom().distanceTo(abbot.prismTo()) - GlassAbbotEntity.PRISM_LENGTH) < 1.0E-6;
				target.setGameMode(GameType.CREATIVE);
				beside.setGameMode(GameType.CREATIVE);
				ending(level, abbot, () -> {
					helper.assertTrue(shards, "His shards did not strike the player they were thrown at, or did not break: " + struck);
					helper.assertTrue(line, "His lance did not strike the player on his line alone: " + lanced);
					target.discard();
					beside.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * At half health the Veil falls: he is unhurt while it falls, then his images stand up, two of them, each his likeness
	 * playing what he plays; one blow shatters an image and its striker takes part. Below a fifth the Breaking: three
	 * images, his beam quicker. Left alone he kneels again, healed: his images fade, his mirrors turn back to rest and
	 * nobody's part is kept.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 200)
	public void theVeilCallsHisImagesAndLeftAloneHeKneelsAgain(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer striker = player(helper, origin, 17.5, 24.5);
		abbot.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			abbot.setHealth(abbot.getMaxHealth() * GlassAbbotEntity.VEIL_AT - 1.0F);
			helper.runAfterDelay(2, () -> {
				boolean veiling = abbot.phase() == GlassAbbotEntity.Phase.VEILING && abbot.action() == GlassAbbotEntity.Action.VEIL;
				boolean unhurt = !abbot.hurtServer(level, blow(level, striker), 8.0F);
				helper.runAfterDelay(GlassAbbotEntity.VEIL_TICKS + 2, () -> {
					List<GlassImageEntity> standing = images(level, abbot);
					boolean called = abbot.phase() == GlassAbbotEntity.Phase.VEIL && standing.size() == GlassAbbotEntity.IMAGES_VEIL
							&& abbot.beamEvery() == GlassAbbotEntity.BEAM_EVERY_VEIL;
					boolean alike = !standing.isEmpty() && standing.get(0).action() == abbot.action();
					GlassImageEntity first = standing.isEmpty() ? null : standing.get(0);
					boolean shattered = first != null && first.hurtServer(level, level.damageSources().playerAttack(striker), 1.0F) && first.isRemoved();
					abbot.setHealth(abbot.getMaxHealth() * GlassAbbotEntity.BREAKING_AT - 1.0F);
					helper.runAfterDelay(2, () -> {
						boolean breaking = abbot.phase() == GlassAbbotEntity.Phase.BREAKING && abbot.imageCap() == GlassAbbotEntity.IMAGES_BREAKING
								&& abbot.beamEvery() == GlassAbbotEntity.BEAM_EVERY_BREAKING;
						boolean striking = abbot.participants(level).contains(striker);
						abbot.mark(level, Mirror.WEST, Mirror.SOUTH, Mirror.WEST);
						ChapelMirrorBlock.turn(level, Mirror.WEST.base(origin), null);
						List<GlassImageEntity> left = images(level, abbot);
						abbot.reset(level);
						ending(level, abbot, () -> {
							helper.assertTrue(veiling, "At half health the Veil did not fall: " + abbot.phase());
							helper.assertTrue(unhurt, "As the Veil fell he was harmed");
							helper.assertTrue(called, "The Veil did not stand up two images: " + standing.size());
							helper.assertTrue(alike, "An image does not play what he plays");
							helper.assertTrue(shattered, "One blow did not shatter an image");
							helper.assertTrue(striking, "Shattering his image did not count as taking part");
							helper.assertTrue(breaking, "Below a fifth the Breaking did not come: " + abbot.phase());
							helper.assertTrue(left.stream().allMatch(GlassImageEntity::isRemoved), "His images outlived his kneeling again");
							helper.assertTrue(abbot.phase() == GlassAbbotEntity.Phase.WAITING && abbot.getHealth() == abbot.getMaxHealth()
									&& abbot.getMaxHealth() == GlassAbbotEntity.HEALTH && abbot.position().distanceTo(abbot.place()) < 0.01,
									"Left alone he did not kneel again at his place, healed");
							for (Mirror mirror : Mirror.values()) {
								BlockState state = level.getBlockState(mirror.base(origin));
								helper.assertTrue(state.getValue(ChapelMirrorBlock.STOP) == Mirror.REST && !state.getValue(ChapelMirrorBlock.MARKED)
										&& !state.getValue(ChapelMirrorBlock.VEILED), "The " + mirror.getSerializedName() + " mirror is not back at rest");
							}
							helper.assertTrue(abbot.participants(level).isEmpty(), "Who took part was not forgotten");
							striker.discard();
						});
						helper.succeed();
					});
				});
			});
		});
	}

	/**
	 * His loot is each participant's own: everyone who struck him or turned his mirrors into place rolls for themselves
	 * (Prism Shards, and on a first win always the Glass Reliquary) into their own inventory with a share of his
	 * experience, and a bystander who took no part gets nothing. Afterwards Seen Clearly is theirs. As he falls the Grey
	 * Mist opens in the apse behind his lectern.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 120)
	public void hisLootIsEachParticipantsOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer first = player(helper, origin, 14.5, 20.5);
		ServerPlayer second = player(helper, origin, 20.5, 20.5);
		ServerPlayer turner = player(helper, origin, 17.5, 24.5);
		ServerPlayer bystander = player(helper, origin, 17.5, 41.5);
		abbot.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			abbot.took(first, 10.0F);
			abbot.took(second, 4.0F);
			abbot.mark(level, Mirror.EAST, Mirror.WEST, Mirror.WEST);
			turner.setGameMode(GameType.SURVIVAL);
			turnTo(level, origin, Mirror.EAST, Mirror.EAST.stopFor(Mirror.WEST), turner);
			turnTo(level, origin, Mirror.WEST, Mirror.WEST.stopFor(Mirror.EAST), turner);
			boolean home = abbot.fire(level);
			turner.setGameMode(GameType.CREATIVE);
			ending(level, abbot, () -> {
				helper.assertTrue(home, "With the east and west mirrors at their stops his beam did not come home");
				List<ServerPlayer> participants = abbot.participants(level);
				helper.assertTrue(participants.contains(first) && participants.contains(second) && participants.contains(turner),
						"Both who struck him and the one who turned his mirrors do not all take part");
				helper.assertFalse(participants.contains(bystander), "A bystander who took no part does");
				helper.assertTrue(AbbotLoot.firstKill(first), "A newcomer has already won Seen Clearly");
				List<ItemStack> loot = AbbotLoot.roll(level, abbot.position(), first);
				int shards = loot.stream().filter(stack -> stack.is(JugcraftAbbot.PRISM_SHARD)).mapToInt(ItemStack::getCount).sum();
				helper.assertTrue(shards >= 2 && shards <= 3, shards + " Prism Shards, not 2 to 3");
				helper.assertTrue(loot.stream().filter(stack -> stack.is(JugcraftAbbot.GLASS_RELIQUARY)).count() == 1,
						"A first win always brings the Glass Reliquary");
				abbot.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				helper.assertTrue(abbot.isDeadOrDying(), "He did not fall");
				for (BlockPos cell : GlassAbbotEntity.fallExitCells()) {
					helper.assertTrue(level.getBlockState(origin.offset(cell)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist opened in the apse at " + cell);
				}
				int share = GlassAbbotEntity.EXPERIENCE / 3;
				for (ServerPlayer player : List.of(first, second, turner)) {
					helper.assertTrue(player.getInventory().countItem(JugcraftAbbot.PRISM_SHARD) >= 2, "A participant's loot did not go into their inventory");
					helper.assertTrue(player.getInventory().countItem(JugcraftAbbot.GLASS_RELIQUARY) == 1, "A first win did not bring one Glass Reliquary");
					helper.assertTrue(player.totalExperience >= share, "A participant got " + player.totalExperience + " experience, not " + share);
					helper.assertFalse(AbbotLoot.firstKill(player), "Seen Clearly is not theirs");
				}
				helper.assertTrue(bystander.getInventory().countItem(JugcraftAbbot.PRISM_SHARD) == 0 && bystander.totalExperience == 0, "The bystander got loot");
				helper.assertTrue(AbbotLoot.firstKill(bystander), "The bystander won Seen Clearly");
				for (BlockPos cell : GlassAbbotEntity.fallExitCells()) {
					level.setBlock(origin.offset(cell), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
				List.of(first, second, turner, bystander).forEach(ServerPlayer::discard);
			});
			helper.succeed();
		});
	}

	/**
	 * The Lantern Lens held in the offhand shows its holder an invisible creature near them, not a visible one, and, while
	 * his images stand, the true Glass Abbot (and none of the images); put away, nothing.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 60)
	public void theLanternLensShowsInvisibleCreaturesAndTheTrueAbbot(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		GlassAbbotEntity abbot = kneeling(helper, origin);
		ServerPlayer holder = player(helper, origin, 17.5, 14.5);
		Zombie hidden = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
		Zombie plain = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
		if (hidden == null || plain == null) {
			throw helper.assertionException("No zombie to look at");
		}
		for (Zombie zombie : List.of(hidden, plain)) {
			zombie.setNoAi(true);
			zombie.snapTo(holder.getX() + (zombie == hidden ? 3.0 : -3.0), holder.getY(), holder.getZ(), 0.0F, 0.0F);
			level.addFreshEntity(zombie);
		}
		hidden.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0, false, false));
		hidden.setInvisible(true);
		ending(level, abbot, () -> {
			helper.assertFalse(LanternLens.holds(holder), "With nothing in the offhand, the lens is held");
			holder.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftAbbot.LANTERN_LENS));
			helper.assertTrue(LanternLens.holds(holder), "Held in the offhand, the lens is not held");
			helper.assertTrue(LanternLens.shows(holder, hidden) && !LanternLens.shows(holder, plain) && !LanternLens.shows(holder, abbot),
					"The lens did not show the invisible zombie alone");
			helper.assertTrue(LanternLens.see(holder) == 1, "The lens showed " + LanternLens.see(holder) + " creatures, not 1");
			abbot.callImages(level);
			List<GlassImageEntity> standing = images(level, abbot);
			helper.assertTrue(!standing.isEmpty() && LanternLens.shows(holder, abbot) && standing.stream().noneMatch(image -> LanternLens.shows(holder, image)),
					"Among his images the lens did not show the true Abbot alone");
			holder.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			helper.assertFalse(LanternLens.holds(holder), "Put away, the lens is still held");
			hidden.discard();
			plain.discard();
			holder.discard();
		});
		helper.succeed();
	}

	/**
	 * The Mirror Inscription in the offhand turns a small shot back at its shooter only under a ward (absorption): the
	 * arrow is spent and its shooter takes the harm; then it rests. A shot harder than {@value MirrorInscription#MAX} it
	 * does not turn.
	 */
	@GameTest(structure = ARENA, padding = 8, maxTicks = 60)
	public void theMirrorInscriptionTurnsASmallShotBackUnderAWard(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		ServerPlayer holder = player(helper, origin, 17.5, 14.5);
		holder.setGameMode(GameType.SURVIVAL);
		holder.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftAbbot.MIRROR_INSCRIPTION));
		Skeleton shooter = EntityTypes.SKELETON.create(level, EntitySpawnReason.COMMAND);
		if (shooter == null) {
			throw helper.assertionException("No skeleton to shoot");
		}
		shooter.setNoAi(true);
		shooter.snapTo(holder.getX() + 6.0, holder.getY(), holder.getZ(), 0.0F, 0.0F);
		level.addFreshEntity(shooter);
		Arrow arrow = new Arrow(level, shooter, new ItemStack(Items.ARROW), null);
		level.addFreshEntity(arrow);
		DamageSource shot = level.damageSources().arrow(arrow, shooter);
		boolean bare = !MirrorInscription.turnBack(level, holder, shot, 4.0F);
		// A ward: absorption hearts, as a golden apple gives.
		holder.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 0, false, false));
		boolean big = !MirrorInscription.turnBack(level, holder, shot, MirrorInscription.MAX + 1.0F);
		float before = shooter.getHealth();
		boolean turned = MirrorInscription.turnBack(level, holder, shot, 4.0F);
		boolean returned = shooter.getHealth() < before && arrow.isRemoved();
		boolean rests = holder.getCooldowns().isOnCooldown(holder.getOffhandItem()) && !MirrorInscription.turnBack(level, holder, shot, 4.0F);
		helper.assertTrue(bare, "Without a ward the inscription turned a shot");
		helper.assertTrue(big, "A shot harder than " + MirrorInscription.MAX + " was turned");
		helper.assertTrue(turned, "Under a ward the inscription did not turn a small shot");
		helper.assertTrue(returned, "The shot was not spent, or its shooter took no harm");
		helper.assertTrue(rests, "The inscription did not rest after turning a shot");
		shooter.discard();
		holder.discard();
		helper.succeed();
	}

	/**
	 * A Prism Shard leads four glass into four Abbey Glass, which glows; two Prism Shards and a spyglass make a Glass
	 * Accord; the Glass Reliquary with a glass pane makes the Lantern Lens, and with an amethyst shard the Mirror
	 * Inscription.
	 */
	@GameTest
	public void hisRecipes(GameTestHelper helper) {
		ItemStack none = ItemStack.EMPTY;
		ItemStack glass = new ItemStack(Items.GLASS);
		ItemStack shard = new ItemStack(JugcraftAbbot.PRISM_SHARD);
		CraftingInput abbey = CraftingInput.of(3, 3, List.of(none, glass, none, glass.copy(), shard, glass.copy(), none, glass.copy(), none));
		ItemStack made = craft(helper, abbey, "a Prism Shard in four glass");
		helper.assertTrue(made.is(JugcraftAbbot.ABBEY_GLASS_ITEM) && made.getCount() == 4, "A Prism Shard in four glass made " + made);
		helper.assertTrue(JugcraftAbbot.ABBEY_GLASS.defaultBlockState().getLightEmission() == JugcraftAbbot.ABBEY_GLASS_LIGHT, "Abbey Glass does not glow");
		ItemStack accord = craft(helper, CraftingInput.of(3, 1, List.of(shard.copy(), shard.copy(), new ItemStack(Items.SPYGLASS))),
				"two Prism Shards and a spyglass");
		helper.assertTrue(accord.is(JugcraftLairs.GLASS_ACCORD), "Two Prism Shards and a spyglass made " + accord);
		ItemStack lens = craft(helper, CraftingInput.of(2, 1, List.of(new ItemStack(JugcraftAbbot.GLASS_RELIQUARY), new ItemStack(Items.GLASS_PANE))),
				"the reliquary and a glass pane");
		helper.assertTrue(lens.is(JugcraftAbbot.LANTERN_LENS), "The reliquary and a glass pane made " + lens);
		ItemStack inscription = craft(helper, CraftingInput.of(2, 1, List.of(new ItemStack(JugcraftAbbot.GLASS_RELIQUARY), new ItemStack(Items.AMETHYST_SHARD))),
				"the reliquary and an amethyst shard");
		helper.assertTrue(inscription.is(JugcraftAbbot.MIRROR_INSCRIPTION), "The reliquary and an amethyst shard made " + inscription);
		helper.succeed();
	}

	private static ItemStack craft(GameTestHelper helper, CraftingInput grid, String what) {
		ServerLevel level = helper.getLevel();
		return level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
				.orElseThrow(() -> helper.assertionException(what + " craft nothing")).value().assemble(grid);
	}
}
