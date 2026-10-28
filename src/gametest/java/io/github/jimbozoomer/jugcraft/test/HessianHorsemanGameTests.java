package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge.Brazier;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.LairBrazierBlock;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Action;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Attack;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Lantern;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity.Phase;
import io.github.jimbozoomer.jugcraft.lair.horseman.HorsemanLoot;
import io.github.jimbozoomer.jugcraft.lair.horseman.JugcraftHorseman;
import io.github.jimbozoomer.jugcraft.lair.horseman.PhantomRiderEntity;
import io.github.jimbozoomer.jugcraft.weapons.DreadBoon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Headless Horseman in his lair (docs/features/hessian-horseman.md), as far as a game-test server can show him: one
 * put down on his own (with no lair instance, over a hollow laid out from the corner of an arena big enough for the
 * Church Bridge's hollow, its bridge and the road's foot; each test lays a floor of dirt and sets the eight Soul Braziers
 * lit in their ring) waits mounted at the bridge's foot until a player who may fight him steps off the road into the
 * hollow, then rears and rides; his health grows with the party; his Dark Road snuffs the braziers nearest his foe, a
 * snuffing lit again in time reveals him (and those who lit it take part), late it does not, and four dark hide him; the
 * Hollow Wakes at half health and the Last Ride below a quarter; his Sabre Pass, Pumpkin Toss, Rearing Stomp, Head Hurl
 * (anyone on the bridge first) and Phantom Riders; his horse balks at the bridge and he only throws; left alone he rides
 * back to the bridge's foot; the Dread boon and the Hessian's Spur; and his loot is each player's own. His fight in the
 * Church Bridge itself runs in a real world, in {@link HessianHorsemanClientGameTests}.
 *
 * <p>He scales for players in his lair and fights those in his hollow (or throws at those on its bridge); the arena keeps
 * every other test's players out of both. Test players are creative (no one he may fight) until the tick a test needs
 * them; those he must not hurt are shielded (Resistance V). Each test sets him back at the bridge's foot and takes him
 * away.
 */
public class HessianHorsemanGameTests {
	/** An arena of air 69 x 76: the Church Bridge's hollow, its bridge and the road's foot fit in it. */
	private static final String FIELD = "jugcraft-test:crowfield_plain";
	/** A mock player cannot be hurt until it has loaded the world, or for 60 ticks; a test that hurts one waits this long. */
	private static final int LOADING_TICKS = 80;
	/** A creature hurt shrugs off a blow no harder than the last for 10 ticks after (vanilla's); a test waits this long between blows. */
	private static final int HURT_COOLDOWN = 12;
	/** Some ticks past his waking, when he rides. */
	private static final int AWAKE = HessianHorsemanEntity.WAKE_TICKS + 6;

	/** The origin of a hollow laid out from the arena's corner: its floor a block over the arena's. */
	private static BlockPos origin(GameTestHelper helper) {
		return helper.absolutePos(new BlockPos(0, 1 - ChurchBridge.STAND, 0));
	}

	/** The hollow's floor, dirt, under the whole arena, and the eight braziers lit in their ring. */
	private static void hollow(ServerLevel level, BlockPos origin) {
		for (int x = 0; x < 69; x++) {
			for (int z = 0; z < 76; z++) {
				level.setBlock(origin.offset(x, ChurchBridge.SOIL, z), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		for (Brazier brazier : Brazier.values()) {
			level.setBlock(brazier.pos(origin), JugcraftLairs.LAIR_BRAZIER.defaultBlockState().setValue(LairBrazierBlock.LIT, true),
					Block.UPDATE_CLIENTS);
		}
	}

	/** He waits at the bridge's foot of the hollow laid out from {@code origin}. */
	private static HessianHorsemanEntity waiting(GameTestHelper helper, BlockPos origin) {
		return HessianHorsemanEntity.summon(helper.getLevel(), origin, null);
	}

	/** A creative player (no one he may fight) standing at the template's ({@code x}, {@code z}) on the floor. */
	private static ServerPlayer player(GameTestHelper helper, BlockPos origin, double x, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.CREATIVE);
		player.snapTo(origin.getX() + x, origin.getY() + ChurchBridge.STAND, origin.getZ() + z, 0.0F, 0.0F);
		return player;
	}

	/** Kept from harm (Resistance V) for the rest of a test. */
	private static void shield(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 4, false, false));
	}

	/** {@code player} uses the block at {@code pos} with an empty hand, as a click on its side would. */
	private static InteractionResult use(ServerLevel level, ServerPlayer player, BlockPos pos) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
		return player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));
	}

	private static float lost(ServerPlayer player) {
		return player.getMaxHealth() - player.getHealth();
	}

	/** Whether his bar's words are {@code key}'s (read from the words, not their text: the server has no mod's words). */
	private static boolean says(Component bar, String key) {
		return bar.getContents() instanceof TranslatableContents words && words.getKey().equals(key);
	}

	private static boolean lit(ServerLevel level, BlockPos origin, Brazier brazier) {
		return ChurchBridge.lit(level, origin, brazier);
	}

	/** Puts him on the floor at the template's ({@code x}, {@code z}), facing south. */
	private static void stand(HessianHorsemanEntity horseman, double x, double z) {
		BlockPos origin = horseman.origin();
		horseman.teleportTo(origin.getX() + x, horseman.floorY(), origin.getZ() + z);
		horseman.setYRot(0.0F);
		horseman.setYBodyRot(0.0F);
		horseman.setYHeadRot(0.0F);
	}

	/** Checks {@code body}, then sets him back at the bridge's foot and takes him away whether it passed or failed. */
	private static void ending(ServerLevel level, HessianHorsemanEntity horseman, Runnable body) {
		try {
			body.run();
		} finally {
			if (horseman.isAlive()) {
				horseman.reset(level);
			}
			horseman.discard();
		}
	}

	/**
	 * He waits mounted at the bridge's foot, his lantern dark, unhurt by anything; a player who may fight him on the wood
	 * road does not wake him, nor does a creative player off it, or their blow; one who may fight him off the road in the
	 * hollow does: his horse rears, his lantern bursts into flame, he is unhurt while it rears, and then he rides, his health
	 * scaled for the party in his lair (two: half as much again).
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 200)
	public void heWaitsAtTheBridgesFootUntilAPlayerStepsOffTheRoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer player = player(helper, origin, 36.5, 60.5);
		ServerPlayer friend = player(helper, origin, 36.5, 75.5);
		player.setGameMode(GameType.SURVIVAL);
		friend.setGameMode(GameType.SURVIVAL);
		shield(player);
		shield(friend);
		helper.runAfterDelay(25, () -> {
			boolean road = horseman.phase() == Phase.WAITING;
			boolean dark = horseman.position().distanceTo(horseman.waiting()) < 0.6 && horseman.action() == Action.WAIT
					&& horseman.lantern() == Lantern.DARK;
			player.setGameMode(GameType.CREATIVE);
			player.snapTo(origin.getX() + 28.5, horseman.floorY(), origin.getZ() + 50.5, 0.0F, 0.0F);
			boolean harmless = !horseman.hurtServer(level, level.damageSources().playerAttack(player), 10.0F)
					&& horseman.getHealth() == horseman.getMaxHealth();
			helper.runAfterDelay(25, () -> {
				boolean slept = horseman.phase() == Phase.WAITING;
				player.setGameMode(GameType.SURVIVAL);
				helper.runAfterDelay(12, () -> {
					boolean woken = horseman.phase() == Phase.WAKING && horseman.lantern() == Lantern.LIT
							&& horseman.getHealth() == horseman.getMaxHealth();
					boolean unhurt = !horseman.hurtServer(level, level.damageSources().playerAttack(player), 10.0F);
					helper.runAfterDelay(AWAKE, () -> {
						ending(level, horseman, () -> {
							helper.assertTrue(road, "A player on the wood road woke him");
							helper.assertTrue(dark, "He does not wait at the bridge's foot, his lantern dark");
							helper.assertTrue(harmless, "Waiting, he was harmed");
							helper.assertTrue(slept, "A creative player off the road, or their blow, woke him");
							helper.assertTrue(woken, "A player off the road did not wake him at full health, his lantern lit");
							helper.assertTrue(unhurt, "Rearing, he was harmed");
							helper.assertTrue(horseman.getMaxHealth() == (float) (HessianHorsemanEntity.HEALTH * HessianHorsemanEntity.partyScale(2)),
									"Woken with two players in his lair, his health is " + horseman.getMaxHealth());
							helper.assertTrue(horseman.phase() == Phase.RIDE, "He does not ride: " + horseman.phase());
							player.discard();
							friend.discard();
						});
						helper.succeed();
					});
				});
			});
		});
	}

	/**
	 * His Dark Road: he wheels and lifts his lantern for {@value HessianHorsemanEntity#SNUFF_TICKS} ticks, then the brazier
	 * nearest his foe and its nearer neighbour go dark, and the rest burn on. A player who lights them both again in time
	 * reveals him: his lantern flares white, his horse rears, every blow bites half as much again, his bar says so, and the
	 * lighter has taken part.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void hisDarkRoadSnuffsTheBraziersNearestHisFoeAndLightingThemRevealsHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 26.5, 47.5);
		foe.setGameMode(GameType.SURVIVAL);
		shield(foe);
		horseman.wake(level);
		List<Brazier> expected = ChurchBridge.run(26.5, 47.5, HessianHorsemanEntity.SNUFFED);
		helper.runAfterDelay(AWAKE, () -> {
			horseman.snuffIn(1);
			helper.runAfterDelay(4, () -> {
				List<Brazier> calling = horseman.snuffing();
				boolean tell = horseman.action() == Action.SNUFF;
				helper.runAfterDelay(HessianHorsemanEntity.SNUFF_TICKS + 2, () -> {
					List<Brazier> dark = new ArrayList<>();
					for (Brazier brazier : Brazier.values()) {
						if (!lit(level, origin, brazier)) {
							dark.add(brazier);
						}
					}
					boolean counting = horseman.relightTicks() > 0 && horseman.snuffed().equals(expected);
					InteractionResult first = use(level, foe, expected.get(0).pos(origin));
					boolean notYet = !horseman.revealed();
					use(level, foe, expected.get(1).pos(origin));
					ending(level, horseman, () -> {
						helper.assertTrue(calling.equals(expected), "He called the braziers " + calling + ", not the nearest his foe " + expected);
						helper.assertTrue(tell, "He did not wheel and lift his lantern before the dark");
						helper.assertTrue(new HashSet<>(dark).equals(new HashSet<>(expected)), "The braziers that went dark are " + dark + ", not " + expected);
						helper.assertTrue(counting, "The time to light them again is not running: " + horseman.relightTicks());
						helper.assertTrue(first.consumesAction() && lit(level, origin, expected.get(0)), "Using a dark brazier did not light it: " + first);
						helper.assertTrue(notYet, "One brazier of two lit again revealed him");
						helper.assertTrue(horseman.revealed() && horseman.revealedTicks() == HessianHorsemanEntity.REVEALED_TICKS,
								"Both lit again in time did not reveal him");
						helper.assertTrue(horseman.lantern() == Lantern.WHITE && horseman.staggerTicks() > 0 && horseman.action() == Action.STAGGER,
								"Revealed, his lantern does not flare white, or his horse does not rear");
						helper.assertTrue(horseman.exposure() == HessianHorsemanEntity.REVEALED, "Revealed, a blow is not " + HessianHorsemanEntity.REVEALED);
						helper.assertTrue(says(horseman.bar(), "message.jugcraft.hessian_horseman.revealed_bar"), "His bar does not say he is revealed");
						helper.assertTrue(horseman.encounter().qualified().contains(foe.getUUID()), "Whoever lit them did not take part");
						foe.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * A snuffing lit again too late does not reveal him; four braziers dark hide him, and every blow does half; revealed,
	 * every blow does half as much again, whatever is dark. (His armour is taken off for the measure.)
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 200)
	public void theDarkHidesHimAndLateLightDoesNotRevealHim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer lighter = player(helper, origin, 26.5, 47.5);
		lighter.setGameMode(GameType.SURVIVAL);
		shield(lighter);
		horseman.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			horseman.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
			horseman.snuffIn(10000);
			List<Brazier> two = List.of(Brazier.WEST_NORTH, Brazier.WEST_SOUTH);
			horseman.snuff(level, two);
			horseman.relightTicks(0);
			use(level, lighter, Brazier.WEST_NORTH.pos(origin));
			use(level, lighter, Brazier.WEST_SOUTH.pos(origin));
			boolean late = !horseman.revealed() && lit(level, origin, Brazier.WEST_NORTH) && lit(level, origin, Brazier.WEST_SOUTH);
			float before = horseman.getHealth();
			horseman.hurtServer(level, level.damageSources().generic(), 4.0F);
			float plain = before - horseman.getHealth();
			horseman.snuff(level, List.of(Brazier.NORTH_EAST, Brazier.EAST_NORTH));
			horseman.snuff(level, List.of(Brazier.EAST_SOUTH, Brazier.SOUTH_EAST));
			boolean hidden = horseman.hidden() && horseman.dark() == 4;
			helper.runAfterDelay(HURT_COOLDOWN, () -> {
				float before2 = horseman.getHealth();
				horseman.hurtServer(level, level.damageSources().generic(), 4.0F);
				float dark = before2 - horseman.getHealth();
				horseman.reveal(level);
				helper.runAfterDelay(HURT_COOLDOWN, () -> {
					float before3 = horseman.getHealth();
					horseman.hurtServer(level, level.damageSources().generic(), 4.0F);
					float revealed = before3 - horseman.getHealth();
					ending(level, horseman, () -> {
						helper.assertTrue(late, "Lit again too late, his snuffing revealed him");
						helper.assertTrue(Math.abs(plain - 4.0F) < 0.01F, "A plain blow of 4 did " + plain);
						helper.assertTrue(hidden, "Four braziers dark did not hide him: " + horseman.dark());
						helper.assertTrue(Math.abs(dark - 4.0F * HessianHorsemanEntity.HIDDEN) < 0.01F, "Hidden, a blow of 4 did " + dark);
						helper.assertTrue(Math.abs(revealed - 4.0F * HessianHorsemanEntity.REVEALED) < 0.01F, "Revealed, a blow of 4 did " + revealed);
						lighter.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * At half health the Hollow Wakes: he rides back to the bridge's foot, unhurt, and lifts his lantern high; then two
	 * braziers go dark and the Headless Charge begins, his snuffings coming sooner. Below a quarter, the Last Ride: three at
	 * a time, sooner still.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void theHollowWakesAtHalfHealthAndTheLastRideBelowAQuarter(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 30.5, 55.5);
		foe.setGameMode(GameType.SURVIVAL);
		shield(foe);
		horseman.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			stand(horseman, 36.5, 50.5);
			horseman.snuffIn(10000);
			horseman.setHealth(horseman.getMaxHealth() * HessianHorsemanEntity.WAKES_AT - 1.0F);
			helper.runAfterDelay(3, () -> {
				boolean wakes = horseman.phase() == Phase.HOLLOW_WAKES;
				boolean unhurt = !horseman.hurtServer(level, level.damageSources().playerAttack(foe), 10.0F);
				helper.runAfterDelay(HessianHorsemanEntity.RIDE_BACK_TICKS + 4, () -> {
					boolean back = horseman.position().distanceTo(horseman.waiting()) < 0.8 && horseman.action() == Action.LIFT;
					helper.runAfterDelay(HessianHorsemanEntity.WAKES_TICKS + 2, () -> {
						boolean charge = horseman.phase() == Phase.CHARGE && horseman.dark() == HessianHorsemanEntity.WAKES_SNUFFED
								&& horseman.snuffEvery() == HessianHorsemanEntity.CHARGE_EVERY;
						horseman.setHealth(horseman.getMaxHealth() * HessianHorsemanEntity.LAST_AT - 1.0F);
						helper.runAfterDelay(3, () -> {
							ending(level, horseman, () -> {
								helper.assertTrue(wakes, "At half health the Hollow did not wake");
								helper.assertTrue(unhurt, "At the Hollow Wakes he was harmed");
								helper.assertTrue(back, "He did not ride back to the bridge's foot and lift his lantern: " + horseman.position());
								helper.assertTrue(charge, "After the Hollow Wakes two braziers are not dark, or the Headless Charge did not begin: "
										+ horseman.phase() + ", " + horseman.dark());
								helper.assertTrue(horseman.phase() == Phase.LAST && horseman.snuffs() == HessianHorsemanEntity.LAST_SNUFFED
										&& horseman.snuffEvery() == HessianHorsemanEntity.LAST_EVERY, "Below a quarter the Last Ride did not begin");
								foe.discard();
							});
							helper.succeed();
						});
					});
				});
			});
		});
	}

	/**
	 * His Sabre Pass: his sabre lowered along his line for {@value HessianHorsemanEntity#SABRE_PASS} ticks' wind-up, then
	 * a gallop down it through his foe's place: the foe takes {@value HessianHorsemanEntity#PASS_DAMAGE}, a player off the
	 * line nothing, and he ends his gallop down the line, never off the hollow's floor.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void hisSabrePassRidesThroughHisFoe(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 36.5, 52.5);
		ServerPlayer aside = player(helper, origin, 41.5, 52.5);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			foe.setGameMode(GameType.SURVIVAL);
			aside.setGameMode(GameType.SURVIVAL);
			horseman.wake(level);
			helper.runAfterDelay(AWAKE, () -> {
				stand(horseman, 36.5, 44.5);
				horseman.snuffIn(10000);
				foe.setHealth(foe.getMaxHealth());
				aside.setHealth(aside.getMaxHealth());
				foe.snapTo(origin.getX() + 36.5, horseman.floorY(), origin.getZ() + 52.5, 0.0F, 0.0F);
				aside.snapTo(origin.getX() + 41.5, horseman.floorY(), origin.getZ() + 52.5, 0.0F, 0.0F);
				horseman.begin(level, Attack.SABRE_PASS, foe);
				boolean lowered = horseman.action() == Action.PASS_WINDUP;
				Attack pass = Attack.SABRE_PASS;
				helper.runAfterDelay(pass.windup + pass.active + 2, () -> {
					Vec3 end = horseman.lineFrom().add(horseman.lineWay().scale(horseman.lineLength()));
					ending(level, horseman, () -> {
						helper.assertTrue(lowered, "He did not lower his sabre before the pass");
						helper.assertTrue(Math.abs(lost(foe) - HessianHorsemanEntity.PASS_DAMAGE) < 0.01F, "His foe lost " + lost(foe));
						helper.assertTrue(lost(aside) == 0.0F, "A player off his line lost " + lost(aside));
						helper.assertTrue(horseman.passedThrough() == 1, "He rode through " + horseman.passedThrough());
						helper.assertTrue(Math.hypot(horseman.getX() - end.x, horseman.getZ() - end.z) < 1.5, "He did not end his gallop down his line");
						helper.assertTrue(horseman.lineLength() <= HessianHorsemanEntity.PASS_LENGTH, "His line is " + horseman.lineLength());
						foe.discard();
						aside.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * His Pumpkin Toss lands on the ring of embers where his foe stood, {@value HessianHorsemanEntity#TOSS_DAMAGE} and
	 * set alight; his Rearing Stomp hurts those within {@value HessianHorsemanEntity#STOMP_REACH} blocks, and nobody past.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void hisPumpkinTossAndRearingStomp(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 36.5, 56.5);
		ServerPlayer near = player(helper, origin, 33.5, 46.5);
		ServerPlayer far = player(helper, origin, 30.5, 46.5);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			foe.setGameMode(GameType.SURVIVAL);
			near.setGameMode(GameType.SURVIVAL);
			far.setGameMode(GameType.SURVIVAL);
			horseman.wake(level);
			helper.runAfterDelay(AWAKE, () -> {
				stand(horseman, 36.5, 46.5);
				horseman.snuffIn(10000);
				for (ServerPlayer player : List.of(foe, near, far)) {
					player.setHealth(player.getMaxHealth());
				}
				foe.snapTo(origin.getX() + 36.5, horseman.floorY(), origin.getZ() + 56.5, 0.0F, 0.0F);
				near.snapTo(origin.getX() + 33.5, horseman.floorY(), origin.getZ() + 46.5, 0.0F, 0.0F);
				far.snapTo(origin.getX() + 30.5, horseman.floorY(), origin.getZ() + 46.5, 0.0F, 0.0F);
				horseman.begin(level, Attack.PUMPKIN_TOSS, foe);
				boolean held = horseman.holding() && horseman.held() == HessianHorsemanEntity.Held.PUMPKIN;
				Vec3 mark = horseman.mark();
				Attack toss = Attack.PUMPKIN_TOSS;
				helper.runAfterDelay(toss.windup + toss.active + 2, () -> {
					float tossed = lost(foe);
					boolean alight = foe.isOnFire();
					boolean nearSpared = lost(near) == 0.0F;
					helper.runAfterDelay(toss.recovery + 4, () -> {
						stand(horseman, 36.5, 46.5);
						near.setHealth(near.getMaxHealth());
						horseman.begin(level, Attack.REARING_STOMP, near);
						Attack stomp = Attack.REARING_STOMP;
						helper.runAfterDelay(stomp.windup + 2, () -> {
							ending(level, horseman, () -> {
								helper.assertTrue(held, "He did not lift a pumpkin from his saddle");
								helper.assertTrue(Math.hypot(mark.x - foe.getX(), mark.z - foe.getZ()) < 0.6, "His ring of embers is not where his foe stood");
								helper.assertTrue(tossed >= HessianHorsemanEntity.TOSS_DAMAGE - 0.01F && alight, "His foe lost " + tossed + ", alight " + alight);
								helper.assertTrue(nearSpared, "A player off the ring was hurt by the pumpkin");
								helper.assertTrue(Math.abs(lost(near) - HessianHorsemanEntity.STOMP_DAMAGE) < 0.01F, "Within his stomp a player lost " + lost(near));
								helper.assertTrue(lost(far) == 0.0F, "Past his stomp a player lost " + lost(far));
								List.of(foe, near, far).forEach(ServerPlayer::discard);
							});
							helper.succeed();
						});
					});
				});
			});
		});
	}

	/**
	 * His horse balks at the bridge: with his foe on it he keeps to the hollow and only throws (the foe shielded meanwhile).
	 * His Head Hurl goes to anyone on the bridge before the farthest in the hollow, {@value HessianHorsemanEntity#HURL_DAMAGE}
	 * and set alight.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 400)
	public void heBalksAtTheBridgeAndHurlsHisHeadAtWhoeverIsOnIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer onBridge = player(helper, origin, 36.5, 25.5);
		ServerPlayer farOff = player(helper, origin, 36.5, 66.5);
		Set<Attack> used = new HashSet<>();
		boolean[] crossed = {false};
		boolean[] awake = {false};
		// Watched every tick once he wakes. Registered here, not in a delayed task: onEachTick schedules a task for each
		// tick left, and adding that many while the test's delayed tasks are being run breaks their iteration.
		helper.onEachTick(() -> {
			if (!awake[0]) {
				return;
			}
			if (horseman.attack() != null) {
				used.add(horseman.attack());
			}
			crossed[0] |= horseman.onBridge(horseman);
		});
		helper.runAfterDelay(LOADING_TICKS, () -> {
			onBridge.setGameMode(GameType.SURVIVAL);
			shield(onBridge);
			shield(farOff);
			horseman.wake(level);
			awake[0] = true;
			helper.runAfterDelay(AWAKE + 100, () -> {
				boolean throwsOnly = used.stream().allMatch(Attack::thrown);
				horseman.snuffIn(10000);
				farOff.setGameMode(GameType.SURVIVAL);
				Player target = horseman.hurlTarget(level, farOff);
				onBridge.removeEffect(MobEffects.RESISTANCE);
				onBridge.setHealth(onBridge.getMaxHealth());
				onBridge.clearFire();
				horseman.begin(level, Attack.HEAD_HURL, farOff);
				boolean head = horseman.held() == HessianHorsemanEntity.Held.HEAD;
				Attack hurl = Attack.HEAD_HURL;
				helper.runAfterDelay(hurl.windup + hurl.active + 2, () -> {
					ending(level, horseman, () -> {
						helper.assertTrue(throwsOnly, "With his foe on the bridge he used " + used);
						helper.assertFalse(crossed[0], "He set foot on the bridge");
						helper.assertTrue(target == onBridge, "His Head Hurl does not go to the player on the bridge first");
						helper.assertTrue(head, "He did not lift his lantern head to hurl it");
						helper.assertTrue(lost(onBridge) >= HessianHorsemanEntity.HURL_DAMAGE - 0.01F && onBridge.isOnFire(),
								"On the bridge a player lost " + lost(onBridge) + ", alight " + onBridge.isOnFire());
						onBridge.discard();
						farOff.discard();
					});
					helper.succeed();
				});
			});
		});
	}

	/**
	 * His Phantom Riders: blue fire runs along two lines crossing at his foe's place, then a ghostly rider gallops down each
	 * (two, no more), riding through the foe; they cannot be hurt, and they fade at their lines' ends.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void hisPhantomRidersGallopDownTheirLines(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 36.5, 52.5);
		helper.runAfterDelay(LOADING_TICKS, () -> {
			foe.setGameMode(GameType.SURVIVAL);
			horseman.wake(level);
			helper.runAfterDelay(AWAKE, () -> {
				stand(horseman, 36.5, 40.5);
				horseman.snuffIn(10000);
				foe.setHealth(foe.getMaxHealth());
				foe.snapTo(origin.getX() + 36.5, horseman.floorY(), origin.getZ() + 52.5, 0.0F, 0.0F);
				horseman.begin(level, Attack.PHANTOM_RIDERS, foe);
				List<Vec3[]> lines = horseman.riderLines();
				Attack riders = Attack.PHANTOM_RIDERS;
				helper.runAfterDelay(riders.windup + 2, () -> {
					List<PhantomRiderEntity> galloping = new ArrayList<>();
					for (var id : horseman.riders()) {
						if (level.getEntity(id) instanceof PhantomRiderEntity rider) {
							galloping.add(rider);
						}
					}
					boolean unharmed = galloping.stream().noneMatch(rider -> rider.hurtServer(level, level.damageSources().generic(), 10.0F));
					helper.runAfterDelay(riders.active + 4, () -> {
						ending(level, horseman, () -> {
							helper.assertTrue(lines.size() == 2, "His riders have " + lines.size() + " lines");
							for (Vec3[] line : lines) {
								Vec3 way = line[1].subtract(line[0]);
								Vec3 to = foe.position().subtract(line[0]);
								double across = Math.abs(to.x * way.z - to.z * way.x) / Math.max(1.0E-6, Math.hypot(way.x, way.z));
								helper.assertTrue(across < 0.6, "A rider's line does not cross his foe's place");
							}
							helper.assertTrue(galloping.size() == HessianHorsemanEntity.RIDERS, galloping.size() + " riders galloped");
							helper.assertTrue(unharmed, "A phantom rider was harmed");
							helper.assertTrue(lost(foe) >= HessianHorsemanEntity.RIDER_DAMAGE - 0.01F, "His foe lost " + lost(foe));
							helper.assertTrue(galloping.stream().allMatch(PhantomRiderEntity::isRemoved), "A rider did not fade at its line's end");
							foe.discard();
						});
						helper.succeed();
					});
				});
			});
		});
	}

	/**
	 * Left alone (nobody who may fight him in his hollow or on its bridge) for {@value HessianHorsemanEntity#ABANDON_TICKS}
	 * ticks, he rides back to the bridge's foot, healed; the braziers burn again, his lantern goes dark, and who hurt him is
	 * forgotten.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 500)
	public void leftAloneHeRidesBackToTheBridgesFoot(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer foe = player(helper, origin, 30.5, 55.5);
		foe.setGameMode(GameType.SURVIVAL);
		shield(foe);
		horseman.wake(level);
		helper.runAfterDelay(AWAKE, () -> {
			horseman.took(foe, 10.0F);
			horseman.setHealth(horseman.getMaxHealth() - 20.0F);
			horseman.snuffIn(10000);
			horseman.snuff(level, List.of(Brazier.SOUTH_WEST, Brazier.WEST_SOUTH));
			foe.setGameMode(GameType.CREATIVE);
			helper.runAfterDelay(HessianHorsemanEntity.ABANDON_TICKS + 20, () -> {
				ending(level, horseman, () -> {
					helper.assertTrue(horseman.phase() == Phase.WAITING, "Left alone he did not go back to waiting: " + horseman.phase());
					helper.assertTrue(horseman.getHealth() == horseman.getMaxHealth(), "Left alone he was not healed");
					helper.assertTrue(horseman.position().distanceTo(horseman.waiting()) < 0.8, "He did not ride back to the bridge's foot");
					helper.assertTrue(horseman.dark() == 0, "The braziers do not all burn again: " + horseman.dark() + " dark");
					helper.assertTrue(horseman.lantern() == Lantern.DARK, "His lantern did not go dark");
					helper.assertTrue(horseman.encounter().qualified().isEmpty(), "Who hurt him is not forgotten");
					foe.discard();
				});
				helper.succeed();
			});
		});
	}

	/**
	 * Each player who took part (struck him, or lit a brazier of a snuffing that revealed him) rolls his loot for
	 * themselves: the Horseman's Lantern always, and on a first win one of his trophies; the experience is shared; Laid to
	 * Rest is theirs. A bystander gets nothing. He falls with his lantern going out, the braziers burn again, and with no
	 * lair instance no Grey Mist opens on the bridge.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 300)
	public void hisLootIsEachPlayersOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ServerPlayer first = player(helper, origin, 30.5, 58.5);
		ServerPlayer lighter = player(helper, origin, 26.5, 47.5);
		ServerPlayer bystander = player(helper, origin, 42.5, 58.5);
		List<ServerPlayer> all = List.of(first, lighter, bystander);
		all.forEach(player -> {
			player.setGameMode(GameType.SURVIVAL);
			shield(player);
		});
		horseman.wake(level);
		Item lantern = item("horseman_lantern");
		helper.runAfterDelay(AWAKE, () -> {
			horseman.snuffIn(10000);
			horseman.took(first, 10.0F);
			horseman.snuff(level, List.of(Brazier.WEST_NORTH, Brazier.WEST_SOUTH));
			use(level, lighter, Brazier.WEST_NORTH.pos(origin));
			use(level, lighter, Brazier.WEST_SOUTH.pos(origin));
			horseman.snuff(level, List.of(Brazier.EAST_NORTH, Brazier.EAST_SOUTH));
			ending(level, horseman, () -> {
				List<ServerPlayer> participants = horseman.participants(level);
				helper.assertTrue(participants.contains(first) && participants.contains(lighter),
						"Both who struck him and the one who lit his braziers in time take part");
				helper.assertFalse(participants.contains(bystander), "A bystander who took no part does");
				helper.assertTrue(HorsemanLoot.firstKill(first), "A newcomer has already won Laid to Rest");
				List<ItemStack> loot = HorsemanLoot.roll(level, horseman.position(), first);
				helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(lantern)), "His loot has no Horseman's Lantern");
				helper.assertTrue(loot.stream().filter(HorsemanLoot::trophy).count() == 1, "A first win always brings one of his trophies");
				horseman.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				helper.assertTrue(horseman.isDeadOrDying() && horseman.lantern() == Lantern.DARK, "He did not fall with his lantern going out");
				helper.assertTrue(horseman.dark() == 0, "When he fell the braziers did not all burn again");
				for (BlockPos cell : ChurchBridge.bridgeExitCells()) {
					helper.assertFalse(level.getBlockState(cell.offset(origin)).is(JugcraftLairs.LAIR_EXIT), "With no lair instance Grey Mist opened on the bridge");
				}
				int share = HessianHorsemanEntity.EXPERIENCE / 2;
				for (ServerPlayer player : List.of(first, lighter)) {
					helper.assertTrue(player.getInventory().countItem(lantern) >= 1, "A participant's loot did not go into their inventory");
					int trophies = player.getInventory().countItem(JugcraftHorseman.hollowflail()) + player.getInventory().countItem(JugcraftHorseman.hessianSabre());
					helper.assertTrue(trophies == 1, "A first win brought " + trophies + " of his trophies, not 1");
					helper.assertTrue(player.totalExperience >= share, "A participant got " + player.totalExperience + " experience, not " + share);
					helper.assertFalse(HorsemanLoot.firstKill(player), "Laid to Rest is not theirs");
				}
				helper.assertTrue(bystander.getInventory().countItem(lantern) == 0 && bystander.totalExperience == 0, "The bystander got loot");
				helper.assertTrue(HorsemanLoot.firstKill(bystander), "The bystander won Laid to Rest");
				all.forEach(ServerPlayer::discard);
			});
			helper.succeed();
		});
	}

	/**
	 * The Dread boon (his trophies'): a monster struck with the Hessian Sabre is filled with dread, and every blow on it
	 * then does a fifth more; it cannot be filled again until its cooldown is over; a boss never is.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 200)
	public void theDreadBoonMakesEveryBlowBiteDeeper(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		ServerPlayer wielder = player(helper, origin, 10.5, 70.5);
		wielder.setGameMode(GameType.SURVIVAL);
		shield(wielder);
		Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(12, 1, 70));
		zombie.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
		zombie.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, false));  // no burning by day
		HessianHorsemanEntity horseman = waiting(helper, origin);
		ItemStack sabre = new ItemStack(JugcraftHorseman.hessianSabre());
		float before = zombie.getHealth();
		zombie.hurtServer(level, level.damageSources().generic(), 4.0F);
		float plain = before - zombie.getHealth();
		sabre.getItem().hurtEnemy(sabre, zombie, wielder);
		sabre.getItem().hurtEnemy(sabre, horseman, wielder);
		boolean dreaded = DreadBoon.DREAD != null && zombie.hasEffect(DreadBoon.DREAD);
		boolean spared = !horseman.hasEffect(DreadBoon.DREAD) && !DreadBoon.dreads(horseman);
		helper.runAfterDelay(HURT_COOLDOWN, () -> {
			float before2 = zombie.getHealth();
			zombie.hurtServer(level, level.damageSources().generic(), 4.0F);
			float dread = before2 - zombie.getHealth();
			zombie.removeEffect(DreadBoon.DREAD);
			sabre.getItem().hurtEnemy(sabre, zombie, wielder);
			boolean cooling = !zombie.hasEffect(DreadBoon.DREAD);
			try {
				helper.assertTrue(Math.abs(plain - 4.0F) < 0.01F, "A plain blow of 4 did " + plain);
				helper.assertTrue(dreaded, "The Hessian Sabre did not fill a zombie with dread");
				helper.assertTrue(Math.abs(dread - 4.0F * (1.0F + DreadBoon.SHARE)) < 0.01F, "In dread, a blow of 4 did " + dread);
				helper.assertTrue(cooling, "A monster was filled with dread again before its cooldown was over");
				helper.assertTrue(spared, "The Horseman, a boss, was filled with dread");
			} finally {
				zombie.discard();
				horseman.discard();
				wielder.discard();
			}
			helper.succeed();
		});
	}

	/**
	 * The Hessian's Spur: held in the offhand by a player riding a horse at night in the Overworld, it puts its speed on the
	 * horse; put away, or by day, the horse is as it was.
	 */
	@GameTest(structure = FIELD, padding = 8, maxTicks = 200)
	public void theHessiansSpurSpursAHorseAtNight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = origin(helper);
		hollow(level, origin);
		ServerPlayer rider = player(helper, origin, 10.5, 70.5);
		rider.setGameMode(GameType.SURVIVAL);
		shield(rider);
		AbstractHorse horse = (AbstractHorse) EntityTypes.HORSE.create(level, EntitySpawnReason.MOB_SUMMONED);
		horse.snapTo(origin.getX() + 10.5, origin.getY() + ChurchBridge.STAND, origin.getZ() + 70.5, 0.0F, 0.0F);
		horse.setTamed(true);
		level.addFreshEntity(horse);
		rider.startRiding(horse);
		rider.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(JugcraftHorseman.HESSIANS_SPUR));
		boolean night = !level.isBrightOutside();
		helper.runAfterDelay(JugcraftHorseman.SPUR_CHECK * 2 + 2, () -> {
			boolean spurred = JugcraftHorseman.spurred(horse);
			rider.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
			helper.runAfterDelay(JugcraftHorseman.SPUR_CHECK * 2 + 2, () -> {
				try {
					helper.assertTrue(rider.getVehicle() == horse, "The player does not ride the horse");
					helper.assertTrue(spurred == night, "At night " + night + ", the horse spurred " + spurred);
					helper.assertFalse(JugcraftHorseman.spurred(horse), "The spur put away, the horse is still spurred");
				} finally {
					rider.stopRiding();
					horse.discard();
					rider.discard();
				}
				helper.succeed();
			});
		});
	}
}
