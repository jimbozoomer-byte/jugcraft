package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.effect.Cause;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectKind;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import io.github.jimbozoomer.jugcraft.concordance.effect.Intent;
import io.github.jimbozoomer.jugcraft.concordance.effect.Ledger;
import io.github.jimbozoomer.jugcraft.concordance.effect.Stacking;
import io.github.jimbozoomer.jugcraft.concordance.ember.Ember;
import io.github.jimbozoomer.jugcraft.concordance.rime.Cooling;
import io.github.jimbozoomer.jugcraft.concordance.rime.Rimebind;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.strata.Brace;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.Encounter;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.MoltenSlagBlock;
import io.github.jimbozoomer.jugcraft.lair.SluiceGateBlock;
import io.github.jimbozoomer.jugcraft.lair.tyrant.CinderTyrantEntity;
import io.github.jimbozoomer.jugcraft.lair.tyrant.JugcraftTyrant;
import io.github.jimbozoomer.jugcraft.lair.tyrant.ThrownQuenchFlask;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

/**
 * The Cinder Tyrant's heat and cooling package (Concordance Chunk E, E2 and E3; docs/features/cinder-tyrant-concordance.md),
 * as far as a game-test server can show it. In his fight (a kiln laid out as {@link CinderTyrantGameTests} lays it, whose
 * helpers these share): whoever turns the sluice whose water quenches him has done a deed of his fight, and whoever turns
 * another has not; a Rimebind that takes on him while he is quenched keeps his seams dark
 * {@value CinderTyrantEntity#RIME_LENGTHEN} ticks longer, once a quench, and is its caster's deed, while on him hot it does
 * nothing; a Brace halves his slam's push, and a friend's Brace is its caster's deed (one's own credits no one); a Quench
 * Flask puts out his fire patches. Outside it: Quench cools whoever it touches; a Quench Flask cools and puts out those it
 * splashes, strikes a blaze, puts out fire, and is made from a glass bottle and three snowballs; Cooled spares hot ground
 * and burning but not fire itself, and the kiln's slag spares it; Hearthspark tempers an Alembic Crucible, which climbs
 * toward hot while one beside it stays cold, and the tempering is saved and counts as a hearth kindled.
 *
 * <p>The fights here begin and end within one synchronous block after he crawls out, and assert only their own players'
 * deeds (see {@link CinderTyrantGameTests} on the reach of his fight's record).
 */
public class CinderTyrantConcordanceGameTests {
	private static final String WAND = "jugcraft:initiate_wand";

	/** The first effect of an invocation's composed form on the Initiate's Wand: what its cast applies. */
	private static EffectSpec invoked(String id) {
		return ConcordanceData.rules().authored(id, WAND).plan().root().steps().getFirst().effect();
	}

	/** {@code spec} on {@code target} as {@code caster}'s own new cast through the effect boundary (the test places them). */
	private static ConcordanceEffects.Result cast(ServerPlayer caster, EffectSpec spec, LivingEntity target) {
		Cause cause = Cause.of(caster.getUUID(), Cause.Origin.INVOCATION, "test:tyrant_concordance", ConcordanceEffects.nextSerial());
		return ConcordanceEffects.apply(new ConcordanceEffects.Context(caster.level(), cause, caster,
				new Ledger(new Ledger.Limits(16, 256, 0)), "0/0/0", caster.position()), spec, target);
	}

	/** {@code player}'s deeds in {@code fight} (none without a part). */
	private static int deeds(Encounter fight, ServerPlayer player) {
		Encounter.Share share = fight.share(player);
		return share == null ? 0 : share.deeds();
	}

	private static double[] moved(ServerPlayer player, Vec3 before) {
		Vec3 change = player.getDeltaMovement().subtract(before);
		return new double[] {Math.hypot(change.x, change.z), change.y};
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < 1.0E-9;
	}

	/** A sluice gate at {@code wheel} (its wheel over its frame) with three trough stones under it, as the choke test lays them. */
	private static void gate(ServerLevel level, BlockPos wheel) {
		BlockState gate = JugcraftLairs.SLUICE_GATE.defaultBlockState().setValue(SluiceGateBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(wheel, gate.setValue(SluiceGateBlock.PART, SluiceGateBlock.Part.WHEEL));
		level.setBlockAndUpdate(wheel.below(), gate.setValue(SluiceGateBlock.PART, SluiceGateBlock.Part.FRAME));
		for (BlockPos stone : List.of(wheel.below(2), wheel.below(2).north(), wheel.below(2).south())) {
			level.setBlockAndUpdate(stone, JugcraftLairs.TROUGH_STONE.defaultBlockState());
		}
	}

	// ---------------------------------------------------------------- his fight

	/**
	 * B-P4: two players each open a sluice; he lands a Body Slam in the west one's flooded trough and is quenched, and its
	 * turner has done one deed of his fight; the east one's turner, whose water never reached him, none. He remembers the
	 * floods players made, and forgets them when the fight resets.
	 */
	@GameTest(structure = CinderTyrantGameTests.KILN, padding = 8, maxTicks = 160)
	public void whoeverTurnsTheSluiceThatQuenchesHimHasADeed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = CinderTyrantGameTests.origin(helper);
		CinderTyrantEntity tyrant = CinderTyrantGameTests.waiting(helper, origin);
		CinderTyrantGameTests.floor(level, origin, 28, 28, 43, 48);
		List<BlockPos> wheels = tyrant.wheels();
		gate(level, wheels.get(0));
		gate(level, wheels.get(1));
		ServerPlayer west = CinderTyrantGameTests.player(helper, tyrant, -2.0, 6.0);
		ServerPlayer east = CinderTyrantGameTests.player(helper, tyrant, 2.0, 6.0);
		List<ServerPlayer> players = List.of(west, east);
		players.forEach(player -> player.setGameMode(GameType.CREATIVE));  // he pays them no heed while he wakes
		tyrant.wake(level);
		helper.runAfterDelay(CinderTyrantEntity.WAKE_TICKS + 3, () -> {
			players.forEach(player -> player.setGameMode(GameType.SURVIVAL));
			CinderTyrantGameTests.ending(level, tyrant, () -> {
				try {
					Encounter fight = tyrant.encounter();
					helper.assertTrue(tyrant.fighting() && fight.underway(), "He did not crawl out to fight: " + tyrant.phase());
					helper.assertTrue(SluiceGateBlock.turn(level, wheels.get(0), west) == SluiceGateBlock.Turn.OPENED
							&& SluiceGateBlock.turn(level, wheels.get(1), east) == SluiceGateBlock.Turn.OPENED, "The sluices did not open");
					List<CinderTyrantEntity.Flood> floods = tyrant.floods();
					BlockPos wet = wheels.get(0).below(2);
					helper.assertTrue(floods.size() == 2 && floods.get(0).turner().equals(west.getUUID()) && floods.get(0).cells().contains(wet)
							&& floods.get(1).turner().equals(east.getUUID()), "He did not hear of both floods and their turners: " + floods);
					tyrant.teleportTo(wet.getX() + 0.5, tyrant.floorY(), wet.getZ() + 0.5);
					tyrant.slamLands(level);
					helper.assertTrue(tyrant.heat() == CinderTyrantEntity.Heat.QUENCHED, "Landing in the west sluice's trough did not quench him");
					helper.assertTrue(deeds(fight, west) == 1 && deeds(fight, east) == 0,
							"Only the west sluice's turner has a deed: " + deeds(fight, west) + " and " + deeds(fight, east));
					tyrant.slamLands(level);
					helper.assertTrue(deeds(fight, west) == 2, "A second quench in the same water is a second deed: " + deeds(fight, west));
					tyrant.reset(level);
					helper.assertTrue(tyrant.floods().isEmpty(), "The floods outlived the fight's reset");
				} finally {
					players.forEach(ServerPlayer::discard);
				}
			});
			helper.succeed();
		});
	}

	/**
	 * Rime's answer: a Rimebind on him hot takes but does nothing to his heat and is no deed. On him quenched it keeps his
	 * seams dark {@value CinderTyrantEntity#RIME_LENGTHEN} ticks longer and is its caster's deed; a second binding in that
	 * window does nothing more; quenched again, it lengthens the new window once more.
	 */
	@GameTest(structure = CinderTyrantGameTests.KILN, padding = 8, maxTicks = 160)
	public void aRimebindHoldsHisSeamsDarkOnceAQuench(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = CinderTyrantGameTests.origin(helper);
		CinderTyrantEntity tyrant = CinderTyrantGameTests.waiting(helper, origin);
		CinderTyrantGameTests.floor(level, origin, 28, 28, 43, 48);
		ServerPlayer caster = CinderTyrantGameTests.player(helper, tyrant, 0.5, 8.0);
		caster.setGameMode(GameType.CREATIVE);
		tyrant.wake(level);
		helper.runAfterDelay(CinderTyrantEntity.WAKE_TICKS + 3, () -> {
			caster.setGameMode(GameType.SURVIVAL);
			CinderTyrantGameTests.ending(level, tyrant, () -> {
				try {
					Encounter fight = tyrant.encounter();
					EffectSpec grip = invoked("jugcraft:rimebind");
					helper.assertTrue(tyrant.fighting() && tyrant.heat() == CinderTyrantEntity.Heat.HOT, "He is not fighting hot: " + tyrant.phase());
					helper.assertTrue(cast(caster, grip, tyrant).applied(), "A Rimebind did not take on him hot");
					helper.assertTrue(tyrant.quenchedTicks() == 0 && !tyrant.lengthened() && deeds(fight, caster) == 0,
							"A Rimebind on him hot changed his heat or was a deed");
					// The Thaw that binding started would keep the next one off; a later one in the fight would take.
					tyrant.removeEffect(Rimebind.RIMEBOUND);
					tyrant.removeEffect(Rimebind.RIME_THAW);
					tyrant.quench(level);
					helper.assertTrue(cast(caster, grip, tyrant).applied(), "A Rimebind did not take on him quenched");
					helper.assertTrue(tyrant.quenchedTicks() == CinderTyrantEntity.QUENCH_TICKS + CinderTyrantEntity.RIME_LENGTHEN
							&& tyrant.lengthened() && deeds(fight, caster) == 1,
							"Quenched, the Rimebind did not hold him dark " + CinderTyrantEntity.RIME_LENGTHEN + " ticks longer as its caster's deed: "
									+ tyrant.quenchedTicks() + ", deeds " + deeds(fight, caster));
					helper.assertTrue(!tyrant.rimebound(level, caster) && tyrant.quenchedTicks() == CinderTyrantEntity.QUENCH_TICKS
							+ CinderTyrantEntity.RIME_LENGTHEN && deeds(fight, caster) == 1, "A second binding in the same window lengthened it again");
					tyrant.quench(level);
					helper.assertTrue(!tyrant.lengthened() && tyrant.rimebound(level, caster)
							&& tyrant.quenchedTicks() == CinderTyrantEntity.QUENCH_TICKS + CinderTyrantEntity.RIME_LENGTHEN && deeds(fight, caster) == 2,
							"Quenched again, a binding did not lengthen the new window once: " + tyrant.quenchedTicks());
				} finally {
					caster.discard();
				}
			});
			helper.succeed();
		});
	}

	/**
	 * Strata's answer, as against the Yeti King: his slam pushes a bare player {@value CinderTyrantEntity#SLAM_KNOCKBACK}
	 * across and 0.4 up, and a braced one half that, whoever put the Brace on. A friend's Brace that meets his push is its
	 * caster's deed; one's own credits no one.
	 */
	@GameTest(structure = CinderTyrantGameTests.KILN, padding = 8, maxTicks = 160)
	public void aBraceHalvesHisPushAndAFriendsIsADeed(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = CinderTyrantGameTests.origin(helper);
		CinderTyrantEntity tyrant = CinderTyrantGameTests.waiting(helper, origin);
		CinderTyrantGameTests.floor(level, origin, 28, 28, 43, 48);
		ServerPlayer bare = CinderTyrantGameTests.player(helper, tyrant, 4.5, 0.0);
		ServerPlayer friend = CinderTyrantGameTests.player(helper, tyrant, -4.5, 0.0);
		ServerPlayer own = CinderTyrantGameTests.player(helper, tyrant, 0.0, 4.5);
		ServerPlayer caster = CinderTyrantGameTests.player(helper, tyrant, 0.0, -9.0);
		List<ServerPlayer> players = List.of(bare, friend, own, caster);
		players.forEach(player -> player.setGameMode(GameType.CREATIVE));
		tyrant.wake(level);
		helper.runAfterDelay(CinderTyrantEntity.WAKE_TICKS + 3, () -> {
			players.forEach(player -> player.setGameMode(GameType.SURVIVAL));
			CinderTyrantGameTests.ending(level, tyrant, () -> {
				try {
					Encounter fight = tyrant.encounter();
					CinderTyrantGameTests.middle(tyrant);
					for (ServerPlayer player : List.of(bare, friend, own)) {
						Vec3 at = tyrant.position().add(player == bare ? 4.5 : player == friend ? -4.5 : 0.0, 0.0, player == own ? 4.5 : 0.0);
						player.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
						player.setDeltaMovement(Vec3.ZERO);
					}
					EffectSpec brace = invoked("jugcraft:brace");
					helper.assertTrue(cast(caster, brace, friend).applied() && cast(own, brace, own).applied(), "The Braces did not take");
					tyrant.slamLands(level);
					double half = 1.0 - Brace.RESISTANCE;
					double[] pushed = moved(bare, Vec3.ZERO);
					helper.assertTrue(near(pushed[0], CinderTyrantEntity.SLAM_KNOCKBACK) && near(pushed[1], 0.4),
							"His slam did not push a bare player its whole way: " + pushed[0] + ", " + pushed[1]);
					for (ServerPlayer player : List.of(friend, own)) {
						double[] braced = moved(player, Vec3.ZERO);
						helper.assertTrue(near(braced[0], CinderTyrantEntity.SLAM_KNOCKBACK * half) && near(braced[1], 0.4 * half),
								"A braced player was not pushed half as far: " + braced[0] + ", " + braced[1]);
					}
					helper.assertTrue(deeds(fight, caster) == 1 && deeds(fight, own) == 0,
							"A friend's Brace is its caster's deed and one's own no one's: " + deeds(fight, caster) + " and " + deeds(fight, own));
				} finally {
					players.forEach(ServerPlayer::discard);
				}
			});
			helper.succeed();
		});
	}

	/** A Quench Flask breaking puts out his fire patches within its reach, and leaves one farther off burning. */
	@GameTest(structure = CinderTyrantGameTests.KILN, padding = 8, maxTicks = 20)
	public void aQuenchFlaskPutsOutHisFirePatches(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = CinderTyrantGameTests.origin(helper);
		CinderTyrantEntity tyrant = CinderTyrantGameTests.waiting(helper, origin);
		CinderTyrantGameTests.ending(level, tyrant, () -> {
			Vec3 centre = tyrant.centre();
			Vec3 near = new Vec3(centre.x + 2.0, tyrant.floorY(), centre.z);
			Vec3 far = new Vec3(centre.x - 8.0, tyrant.floorY(), centre.z);
			tyrant.firePatch(level, near);
			tyrant.firePatch(level, far);
			ThrownQuenchFlask.Splash splash = ThrownQuenchFlask.splash(level, near.add(1.0, 0.0, 0.0), null, null);
			helper.assertTrue(splash.doused() >= 1 && tyrant.patches().size() == 1 && tyrant.patches().getFirst().at().equals(far),
					"The flask did not put out only the patch within its reach: " + tyrant.patches());
			helper.assertTrue(tyrant.heat() == CinderTyrantEntity.Heat.HOT, "A Quench Flask quenched him; only his sluices' water may");
		});
		helper.succeed();
	}

	// ---------------------------------------------------------------- outside his fight

	/**
	 * A Quench Flask's splash: a burning player within its reach is put out and Cooled for
	 * {@value ThrownQuenchFlask#COOLED_TICKS} ticks; a blaze is struck {@value ThrownQuenchFlask#FIRE_BORN_DAMAGE} and never
	 * Cooled; fire on the ground goes out; a player beyond its reach is left as they were. It is made from a glass bottle
	 * and three snowballs.
	 */
	@GameTest(maxTicks = 120)
	public void aQuenchFlaskCoolsPutsOutAndStrikesTheFireBorn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		ServerPlayer wet = TestPlayers.mock(helper);
		ServerPlayer dry = TestPlayers.mock(helper);
		List.of(wet, dry).forEach(player -> player.setGameMode(GameType.CREATIVE));
		helper.runAfterDelay(CinderTyrantGameTests.LOADING_TICKS, () -> {
			Vec3 at = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
			wet.setGameMode(GameType.SURVIVAL);
			dry.setGameMode(GameType.SURVIVAL);
			wet.snapTo(at.x + 1.0, at.y, at.z, 0.0F, 0.0F);
			dry.snapTo(at.x - 3.5, at.y, at.z - 3.0, 0.0F, 0.0F);
			wet.igniteForSeconds(5.0F);
			dry.igniteForSeconds(5.0F);
			LivingEntity blaze = helper.spawnWithNoFreeWill(EntityTypes.BLAZE, new BlockPos(3, 2, 2));
			helper.setBlock(new BlockPos(4, 2, 4), Blocks.FIRE);
			float blazeHealth = blaze.getHealth();
			ThrownQuenchFlask.Splash splash = ThrownQuenchFlask.splash(level, at, null, null);
			helper.assertTrue(Cooling.cooled(wet) && !wet.isOnFire() && wet.getEffect(Cooling.COOLED).getDuration() == ThrownQuenchFlask.COOLED_TICKS,
					"The splashed player was not put out and Cooled for " + ThrownQuenchFlask.COOLED_TICKS + " ticks");
			helper.assertTrue(!Cooling.cooled(dry) && dry.isOnFire(), "A player beyond the splash was cooled or put out");
			helper.assertTrue(blazeHealth - blaze.getHealth() == ThrownQuenchFlask.FIRE_BORN_DAMAGE && !Cooling.cooled(blaze),
					"The blaze was not struck " + ThrownQuenchFlask.FIRE_BORN_DAMAGE + ", or was Cooled: " + (blazeHealth - blaze.getHealth()));
			helper.assertTrue(!helper.getBlockState(new BlockPos(4, 2, 4)).is(Blocks.FIRE), "The fire within the splash still burns");
			helper.assertTrue(splash.cooled() == 1 && splash.struck() == 1 && splash.doused() >= 1, "The splash did not count what it did: " + splash);
			if (JugcraftConfig.isFeatureEnabled(JugcraftLairs.FEATURE)) {
				ItemStack snowball = new ItemStack(Items.SNOWBALL);
				CraftingInput shaken = CraftingInput.of(2, 2, List.of(new ItemStack(Items.GLASS_BOTTLE), snowball, snowball.copy(), snowball.copy()));
				ItemStack made = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, shaken, level)
						.orElseThrow(() -> helper.assertionException("A glass bottle and three snowballs make nothing")).value().assemble(shaken);
				helper.assertTrue(made.is(JugcraftTyrant.QUENCH_FLASK) && made.getCount() == 1, "A glass bottle and three snowballs made " + made);
			}
			wet.discard();
			dry.discard();
			blaze.discard();
			helper.succeed();
		});
	}

	/**
	 * Cooled, from Quench: whoever it touches is put out and Cooled for the data's time. Cooled, hot ground and burning
	 * pass a player by and the kiln's slag spares them, but fire itself still burns them; it is no Fire Resistance.
	 */
	@GameTest(maxTicks = 120)
	public void quenchCoolsButIsNoFireResistance(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer caster = TestPlayers.mock(helper);
		ServerPlayer friend = TestPlayers.mock(helper);
		List.of(caster, friend).forEach(player -> player.setGameMode(GameType.CREATIVE));
		helper.runAfterDelay(CinderTyrantGameTests.LOADING_TICKS, () -> {
			caster.setGameMode(GameType.SURVIVAL);
			friend.setGameMode(GameType.SURVIVAL);
			friend.igniteForSeconds(5.0F);
			EffectSpec quench = invoked("jugcraft:quench");
			helper.assertTrue(quench.kind() == EffectKind.STATUS && Cooling.STATUS.equals(quench.status()) && quench.intent() == Intent.HELPFUL,
					"Quench's word is not a helpful Cooled: " + quench);
			helper.assertTrue(cast(caster, quench, friend).applied() && Cooling.cooled(friend) && !friend.isOnFire()
					&& friend.getEffect(Cooling.COOLED).getDuration() == quench.duration(), "Quench did not put out and cool its friend");
			float health = friend.getHealth();
			boolean floor = friend.hurtServer(level, level.damageSources().hotFloor(), 1.0F);
			boolean burning = friend.hurtServer(level, level.damageSources().onFire(), 1.0F);
			helper.assertTrue(!floor && !burning && friend.getHealth() == health && !MoltenSlagBlock.harms(friend),
					"Hot ground, burning or the slag hurt a Cooled player");
			boolean fire = friend.hurtServer(level, level.damageSources().inFire(), 1.0F);
			helper.assertTrue(fire && friend.getHealth() < health, "Fire itself did not burn a Cooled player: Cooled is no Fire Resistance");
			friend.removeEffect(Cooling.COOLED);
			helper.assertTrue(MoltenSlagBlock.harms(friend), "Without Cooled the slag spared them");
			caster.discard();
			friend.discard();
			helper.succeed();
		});
	}

	/**
	 * Hearthspark's spark on an Alembic Crucible tempers it for {@value Ember#TEMPER_TICKS} ticks: with nothing hot
	 * beneath it climbs toward hot while an untempered one beside it stays cold, and a tempered crucible is a hearth kind
	 * kindled (Hearthbinding's practice). The tempering is saved with the crucible, and ended, it is forgotten.
	 */
	@GameTest(maxTicks = 80)
	public void hearthsparkTempersACrucible(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		BlockPos temperedAt = new BlockPos(2, 2, 3);
		BlockPos controlAt = new BlockPos(5, 2, 3);
		helper.setBlock(temperedAt, JugcraftConcordance.CRUCIBLE);
		helper.setBlock(controlAt, JugcraftConcordance.CRUCIBLE);
		CrucibleBlockEntity tempered = (CrucibleBlockEntity) level.getBlockEntity(helper.absolutePos(temperedAt));
		CrucibleBlockEntity control = (CrucibleBlockEntity) level.getBlockEntity(helper.absolutePos(controlAt));
		tempered.setTemperature(20);
		control.setTemperature(20);
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = helper.absolutePos(new BlockPos(2, 2, 1));
		player.snapTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0.0F, 0.0F);
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, Ember.RESEARCH, ResearchState.UNDERSTOOD);
		EffectSpec kindling = new EffectSpec(EffectKind.ALTERATION, Intent.HELPFUL, 0, 0, null, Stacking.STRONGEST, Ember.SCHOOL);
		Cause cause = Cause.of(player.getUUID(), Cause.Origin.INVOCATION, "jugcraft:hearthspark", ConcordanceEffects.nextSerial());
		ConcordanceEffects.Result result = ConcordanceEffects.apply(new ConcordanceEffects.Context(level, cause, player,
				new Ledger(new Ledger.Limits(16, 256, 0)), "0/0/0", player.position()), kindling, helper.absolutePos(temperedAt));
		long now = ConcordanceProgress.now(level);
		helper.assertTrue(result.applied() && tempered.tempered(now) == Ember.TEMPER_TICKS && control.tempered(now) == 0,
				"Hearthspark did not temper the crucible for " + Ember.TEMPER_TICKS + " ticks: " + result + ", " + tempered.tempered(now));
		helper.assertTrue(ConcordanceProgress.knowledge(player).progress(Ember.RESEARCH).evidence().containsKey("practice:" + Ember.ACTIVITY + ":crucible"),
				"A tempered crucible is no hearth kindled");
		CompoundTag saved = tempered.saveWithoutMetadata(level.registryAccess());
		CrucibleBlockEntity copy = new CrucibleBlockEntity(tempered.getBlockPos(), tempered.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.tempered(now) == Ember.TEMPER_TICKS, "The tempering was not saved: " + copy.tempered(now));
		helper.runAfterDelay(40, () -> {
			helper.assertTrue(tempered.temperature() >= 50 && control.temperature() <= 20,
					"Tempered, it did not climb toward hot while the one beside it stayed cold: " + tempered.temperature() + " and " + control.temperature());
			tempered.temper(ConcordanceProgress.now(level), 0);
			helper.assertTrue(tempered.tempered(ConcordanceProgress.now(level)) == 0
					&& !tempered.saveWithoutMetadata(level.registryAccess()).keySet().contains(CrucibleBlockEntity.TEMPERED_UNTIL),
					"An ended tempering was not forgotten");
			player.discard();
			helper.succeed();
		});
	}
}
