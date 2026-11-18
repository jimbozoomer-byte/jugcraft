package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import io.github.jimbozoomer.jugcraft.weapons.ArmItem;
import io.github.jimbozoomer.jugcraft.weapons.NavalCutlassItem;
import io.github.jimbozoomer.jugcraft.weapons.NavalCutlassStats;
import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Thanksgiving weapons (tools/thanksgiving_weapons.py; docs/features/thanksgiving-weapons.md).
 * <ul>
 * <li>The Naval Cutlass carries its numbers as the game sees them, parries 40% of a blow from in front, deals its
 * boarding bonus afloat and not ashore, and adds no way round land claims or the PvP rules.</li>
 * <li>The black-powder guns the inventory found covering the blunderbuss, flintlock pistol and musket (the Bellmouth,
 * Duelling Pistol and Line Musket) keep the owner's rules: one Paper Cartridge a shot (the Bellmouth's ten balls from
 * one), a slow muzzle reload taking one cartridge, no block broken by a shot, and no shot through someone's claim or
 * past the PvP rules.</li>
 * </ul>
 * Written for the server game tests; not run here (no Minecraft toolchain in the environment that wrote them).
 */
public class ColonialWeaponsGameTests {
	/** The new hunting items retain owner-attributed projectile protection as well as the shared melee path. */
	@GameTest(structure = "jugcraft-test:claim_combat_arena", padding = 48)
	public void huntingProjectilesRespectClaimOwnership(GameTestHelper helper) {
		Claim claim = new Claim(helper);
		try {
			LivingEntity cow = claim.livestock();
			var level = helper.getLevel();
			for (Item item : List.of(io.github.jimbozoomer.jugcraft.weapons.HuntingWeapons.STONE_HEADED_HATCHET,
					io.github.jimbozoomer.jugcraft.weapons.HuntingWeapons.HUNTING_SPEAR)) {
				for (ServerPlayer player : List.of(claim.stranger, claim.owner)) {
					cow.setHealth(cow.getMaxHealth());
					cow.setInvulnerableTime(0);
					cow.damageCooldownTime = 0;
					player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
					var projectile = new io.github.jimbozoomer.jugcraft.weapons.ThrownArm(level, player, new ItemStack(item));
					cow.hurtServer(level, level.damageSources().trident(projectile, player), 4);
					helper.assertTrue((cow.getHealth() < cow.getMaxHealth()) == (player == claim.owner),
							"Thrown hunting weapon must follow claim ownership");
				}
			}
		} finally {
			claim.close();
		}
		helper.succeed();
	}

	private static final List<String> POWDER_GUNS = List.of("duelling_pistol", "line_musket", "bellmouth");
	/** finance/ClaimCombatGameTests' arena: 33 blocks wide, so both sides of a chunk border in it are active. */
	private static final String CLAIM_ARENA = "jugcraft-test:claim_combat_arena";
	/** Ticks a newly joined player is spared other players' blows (ConcordanceRitualGameTests waits as long). */
	private static final int PAST_SPAWN_GRACE = 70;

	@GameTest
	public void theCutlassCarriesItsNumbers(GameTestHelper helper) {
		ItemStack stack = new ItemStack(NavalCutlassItem.CUTLASS);
		AttackRange range = stack.get(DataComponents.ATTACK_RANGE);
		SwingAnimation swing = stack.get(DataComponents.ATTACK_ANIMATION);
		helper.assertTrue(range != null && swing != null && stack.has(DataComponents.WEAPON), "The cutlass lacks its range, swing or weapon");
		helper.assertTrue(range.minReach() == 0.0F && range.maxReach() == NavalCutlassStats.REACH, "It reaches " + range.maxReach());
		helper.assertTrue(swing.type() == SwingAnimationType.WHACK && swing.duration() == NavalCutlassStats.SWING_TICKS, "It swings " + swing);
		helper.assertTrue(Math.abs(modifier(stack, Item.BASE_ATTACK_DAMAGE_ID) - (NavalCutlassStats.IRON_BONUS + NavalCutlassStats.DAMAGE)) < 1.0E-4,
				"It hits for " + (1.0 + modifier(stack, Item.BASE_ATTACK_DAMAGE_ID)) + ", not 5");
		helper.assertTrue(Math.abs(modifier(stack, Item.BASE_ATTACK_SPEED_ID) - NavalCutlassStats.SPEED) < 1.0E-4, "It swings at the wrong rate");
		BlocksAttacks parry = stack.get(DataComponents.BLOCKS_ATTACKS);
		helper.assertTrue(parry != null && parry.bypassedBy().isPresent(), "Its parry is missing, or blocks everything");
		helper.assertTrue(stack.is(ItemTags.SWORDS) && stack.isDamageableItem() && stack.getMaxDamage() == 250,
				"It is not an iron sword's kin (sword enchantments, 250 uses)");
		ArmItem arm = (ArmItem) stack.getItem();
		helper.assertTrue(arm.kind().equals(NavalCutlassItem.MOTION) && arm.trait() == null,
				"It is swung as the sabre is, without the sabre's saddle trait");
		helper.assertTrue(stack.getMaxStackSize() == 1, "It stacks");
		helper.succeed();
	}

	/** Held up, the cutlass's shell guard stops 40% of a blow from in front, and none of one from behind. */
	@GameTest
	public void theCutlassParriesFromTheFront(GameTestHelper helper) {
		floor(helper);
		ItemStack cutlass = new ItemStack(NavalCutlassItem.CUTLASS);
		Mob front = fighter(helper, new BlockPos(1, 2, 1), cutlass.copy(), 0.0F);
		Mob back = fighter(helper, new BlockPos(4, 2, 1), cutlass.copy(), 180.0F);
		Mob attacker = fighter(helper, new BlockPos(1, 2, 3), ItemStack.EMPTY, 180.0F);
		Mob attacker2 = fighter(helper, new BlockPos(4, 2, 3), ItemStack.EMPTY, 180.0F);
		front.startUsingItem(InteractionHand.MAIN_HAND);
		back.startUsingItem(InteractionHand.MAIN_HAND);
		helper.runAfterDelay(6, () -> {
			ServerLevel level = helper.getLevel();
			helper.assertTrue(front.isBlocking() && back.isBlocking(), "The husks are not parrying after 6 ticks");
			front.hurtServer(level, level.damageSources().mobAttack(attacker), 10.0F);
			back.hurtServer(level, level.damageSources().mobAttack(attacker2), 10.0F);
			float left = 20.0F - NavalCutlassStats.parried(10.0F);
			helper.assertTrue(Math.abs(front.getHealth() - left) < 0.01F, "A parried blow of 10 left " + front.getHealth() + ", not " + left);
			helper.assertTrue(Math.abs(back.getHealth() - 10.0F) < 0.01F, "A blow from behind left " + back.getHealth() + " of 20");
			helper.succeed();
		});
	}

	/** Boarding: nothing more ashore; 2 more with its wielder aboard a boat, or its foe standing in water. */
	@GameTest(maxTicks = 40)
	public void theCutlassBitesHarderAfloat(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		Item cutlass = NavalCutlassItem.CUTLASS;
		Mob wielder = fighter(helper, new BlockPos(1, 2, 1), new ItemStack(cutlass), 0.0F);
		Mob foe = fighter(helper, new BlockPos(5, 2, 1), ItemStack.EMPTY, 180.0F);
		DamageSource source = level.damageSources().mobAttack(wielder);
		helper.assertTrue(cutlass.getAttackDamageBonus(foe, 5.0F, source) == 0.0F, "Ashore the cutlass adds something");
		@SuppressWarnings("unchecked")
		EntityType<Entity> boatType = (EntityType<Entity>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("oak_boat"));
		Entity boat = boatType.create(level, EntitySpawnReason.TRIGGERED);
		helper.assertTrue(boat != null, "No boat");
		boat.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 2, 4))));
		level.addFreshEntity(boat);
		wielder.startRiding(boat);
		helper.assertTrue(wielder.getVehicle() == boat && NavalCutlassItem.afloat(wielder), "The wielder did not board the boat");
		helper.assertTrue(cutlass.getAttackDamageBonus(foe, 5.0F, source) == NavalCutlassStats.BOARDING,
				"Aboard a boat the cutlass adds " + cutlass.getAttackDamageBonus(foe, 5.0F, source));
		wielder.stopRiding();
		helper.setBlock(new BlockPos(5, 2, 1), Blocks.WATER);
		helper.succeedWhen(() -> {
			helper.assertTrue(foe.isInWater(), "The foe is not in the water yet");
			helper.assertTrue(cutlass.getAttackDamageBonus(foe, 5.0F, source) == NavalCutlassStats.BOARDING,
					"Against a foe in water the cutlass adds " + cutlass.getAttackDamageBonus(foe, 5.0F, source));
		});
	}

	/**
	 * The cutlass has no attack path of its own. In the claim-combat arena, a stranger beside a cow in someone's claim
	 * cannot swing at it (the claim's attack check refuses) and a blow lands nothing (its damage check refuses, as for
	 * any sword); the claim's owner can. Between two players, once their spawn grace is over, a blow lands exactly
	 * where the PvP rules let the one harm the other.
	 */
	@GameTest(structure = CLAIM_ARENA, maxTicks = 120)
	public void theCutlassKeepsClaimsAndPvp(GameTestHelper helper) {
		Claim claim = new Claim(helper);
		claim.owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NavalCutlassItem.CUTLASS));
		claim.stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NavalCutlassItem.CUTLASS));
		claim.stranger.snapTo(Vec3.atCenterOf(claim.target.west()));
		try {
			LivingEntity cow = claim.livestock();
			helper.assertTrue(AttackEntityCallback.EVENT.invoker().interact(claim.stranger, helper.getLevel(), InteractionHand.MAIN_HAND, cow,
					null) == InteractionResult.FAIL, "A stranger may swing the cutlass at livestock in someone's claim");
			float before = cow.getHealth();
			claim.stranger.attack(cow);
			helper.assertTrue(cow.getHealth() == before, "A stranger's cutlass blow hurt livestock in someone's claim");
			claim.owner.snapTo(Vec3.atCenterOf(claim.target.east()));
			claim.owner.attack(cow);
			helper.assertTrue(cow.getHealth() < before, "The claim's owner cannot strike with the cutlass on their own land");
		} catch (RuntimeException failed) {
			claim.close();
			throw failed;
		}
		Runnable pvp = () -> {
			try {
				boolean mayHarm = claim.stranger.canHarmPlayer(claim.owner);
				float owners = claim.owner.getHealth();
				claim.stranger.attack(claim.owner);
				helper.assertTrue(mayHarm == (claim.owner.getHealth() < owners), "A cutlass blow between players landed "
						+ (claim.owner.getHealth() < owners) + " where the PvP rules say " + mayHarm);
			} finally {
				claim.close();
			}
			helper.succeed();
		};
		helper.runAfterDelay(PAST_SPAWN_GRACE, pvp);
	}

	/**
	 * Each covered black-powder gun holds one round and spends exactly it a shot (the Bellmouth's ten balls come from one
	 * cartridge, and firing takes nothing from the inventory); it will not fire again empty; its reload is slow, loads
	 * nothing before its time, and then takes exactly one cartridge.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 140)
	public void powderGunsSpendOneCartridgeAShotAndLoadSlowly(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Item cartridge = JugcraftGuns.ROUNDS.get("paper_cartridge");
		for (int i = 0; i < POWDER_GUNS.size(); i++) {
			String gun = POWDER_GUNS.get(i);
			GunSpec spec = JugcraftGuns.SPECS.get(gun);
			helper.assertTrue(spec.capacity() == 1 && !spec.byShell() && spec.reload() >= 70 && spec.ammo().equals("paper_cartridge"),
					gun + " is not a slow single-shot muzzle-loader firing paper cartridges");
			Mob pig = GunsGameTests.pig(helper, new BlockPos(1 + 4 * i, 2, 6));
			pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
			pig.setHealth(200.0F);
			ServerPlayer shooter = GunsGameTests.shooter(helper, gun, 1, pig, GameType.SURVIVAL);
			BlockPos at = helper.absolutePos(new BlockPos(1 + 4 * i, 2, 1));
			shooter.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
			shooter.getInventory().add(new ItemStack(cartridge, 3));
			helper.assertTrue(GunShots.fire(shooter), gun + " did not fire");
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0
					&& GunShots.count(shooter.getInventory(), cartridge) == 3, gun + " spent other than its one loaded round");
			helper.assertTrue(pig.getHealth() < 200.0F, gun + "'s shot missed the pig");
			helper.assertFalse(GunShots.fire(shooter), gun + " fired again empty");
			helper.assertTrue(GunShots.reload(shooter), gun + " did not start its reload");
			Runnable early = () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0, gun + " loaded before its reload was up");
			helper.runAfterDelay(spec.reload() - 2, early);
		}
		helper.succeedWhen(() -> {
			// once every reload is up: each gun holds one round again, and took one cartridge of three
			for (ServerPlayer shooter : helper.getLevel().getEntitiesOfClass(ServerPlayer.class, new net.minecraft.world.phys.AABB(
					helper.absolutePos(BlockPos.ZERO)).inflate(16.0), p -> p.getMainHandItem().getItem() instanceof GunItem)) {
				helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 1, "A powder gun is not loaded after its reload");
				helper.assertTrue(GunShots.count(shooter.getInventory(), cartridge) == 2, "A reload took other than one cartridge");
			}
		});
	}

	/**
	 * No black-powder shot breaks a block: the Bellmouth's ten balls and the musket's ball pass to a pig through nothing
	 * but leave glass, a glass pane, a lantern and a flower pot before it as they were.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 20)
	public void powderShotsBreakNoBlocks(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		List<BlockPos> fragile = List.of(new BlockPos(0, 3, 4), new BlockPos(1, 4, 4), new BlockPos(2, 3, 4), new BlockPos(1, 2, 7));
		List<BlockState> states = List.of(Blocks.GLASS.defaultBlockState(), Blocks.GLASS_PANE.defaultBlockState(),
				Blocks.LANTERN.defaultBlockState(), Blocks.FLOWER_POT.defaultBlockState());
		for (int i = 0; i < fragile.size(); i++) {
			helper.setBlock(fragile.get(i), states.get(i));
		}
		// a wall of glass straight in the line of fire, the pig behind it
		helper.setBlock(new BlockPos(1, 3, 4), Blocks.GLASS);
		Mob pig = GunsGameTests.pig(helper, new BlockPos(1, 2, 8));
		for (String gun : List.of("bellmouth", "line_musket")) {
			ServerPlayer shooter = GunsGameTests.shooter(helper, gun, 1, pig, GameType.SURVIVAL);
			GunShots.fire(shooter);
		}
		for (int i = 0; i < fragile.size(); i++) {
			helper.assertTrue(helper.getBlockState(fragile.get(i)) == states.get(i), "A shot changed " + states.get(i) + " at " + fragile.get(i));
		}
		helper.assertTrue(helper.getBlockState(new BlockPos(1, 3, 4)).is(Blocks.GLASS), "A shot broke the glass in its way");
		helper.succeed();
	}

	/**
	 * In the claim-combat arena, a stranger's Bellmouth shot from outside a claim at livestock inside it hurts nothing
	 * (the claim's attack check, which every gun asks, refuses it); the owner's shot lands. Between players, once their
	 * spawn grace is over, a shot lands exactly where the PvP rules let the shooter harm the other.
	 */
	@GameTest(structure = CLAIM_ARENA, maxTicks = 120)
	public void powderShotsKeepClaimsAndPvp(GameTestHelper helper) {
		Claim claim = new Claim(helper);
		try {
			LivingEntity cow = claim.livestock();
			float before = cow.getHealth();
			aimWith(claim.stranger, "bellmouth", cow);
			helper.assertTrue(GunShots.fire(claim.stranger), "The stranger's Bellmouth did not fire");
			helper.assertTrue(cow.getHealth() == before, "A stranger's shot hurt livestock in someone's claim");
			// Clear the firing lane: the protected stranger must not intercept the owner's shot.
			claim.stranger.snapTo(Vec3.atCenterOf(claim.outside.south(2)));
			claim.owner.snapTo(Vec3.atCenterOf(claim.outside.west()));
			aimWith(claim.owner, "bellmouth", cow);
			helper.assertTrue(GunShots.fire(claim.owner) && cow.getHealth() < before, "The claim's owner's shot did not land");
			cow.discard();
		} catch (RuntimeException failed) {
			claim.close();
			throw failed;
		}
		Runnable pvp = () -> {
			try {
				claim.stranger.snapTo(Vec3.atCenterOf(claim.outside));
				claim.owner.snapTo(Vec3.atCenterOf(claim.outside.north()));
				aimWith(claim.stranger, "line_musket", claim.owner);
				boolean mayHarm = claim.stranger.canHarmPlayer(claim.owner);
				float owners = claim.owner.getHealth();
				helper.assertTrue(GunShots.fire(claim.stranger), "The stranger's musket did not fire");
				helper.assertTrue(mayHarm == (claim.owner.getHealth() < owners), "A musket shot between players landed "
						+ (claim.owner.getHealth() < owners) + " where the PvP rules say " + mayHarm);
			} finally {
				claim.close();
			}
			helper.succeed();
		};
		helper.runAfterDelay(PAST_SPAWN_GRACE, pvp);
	}

	/** Puts a loaded `gun` in the player's hand and turns them to aim at the target's middle. */
	private static void aimWith(ServerPlayer player, String gun, LivingEntity target) {
		ItemStack stack = new ItemStack(JugcraftGuns.GUNS.get(gun));
		GunItem.setLoaded(stack, 1);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		Vec3 aim = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
		player.setYRot((float) Math.toDegrees(Math.atan2(-aim.x, aim.z)));
		player.setYHeadRot(player.getYRot());
		player.setXRot((float) Math.toDegrees(Math.atan2(-aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z))));
	}

	/**
	 * The claim-combat arena's claim (as finance/ClaimCombatGameTests sets it up): `outside` on the last block of one
	 * chunk, unclaimed; `target`, three blocks into the next, claimed by `owner`; a stranger at `outside`. close() lets
	 * the claim go and puts the livestock away.
	 */
	private static final class Claim {
		final GameTestHelper helper;
		final ServerPlayer owner;
		final ServerPlayer stranger;
		final BlockPos outside;
		final BlockPos target;
		final LandRegistry land;
		final io.github.jimbozoomer.jugcraft.finance.FinanceBook book;
		final long key;
		LivingEntity animal;

		Claim(GameTestHelper helper) {
			this.helper = helper;
			ServerLevel level = helper.getLevel();
			owner = TestPlayers.survivor(helper);
			stranger = TestPlayers.survivor(helper);
			BlockPos base = helper.absolutePos(new BlockPos(0, 3, 8));
			outside = new BlockPos(Math.floorDiv(base.getX(), 16) * 16 + 15, base.getY(), base.getZ());
			target = outside.east(3);
			land = LandRegistry.get(level.getServer());
			book = Ledger.get(level.getServer()).book();
			key = LandRegistry.keyOf(target);
			land.assign(book, key, book.account(owner.getUUID(), "ColonialClaimOwner"), 0);
			for (int x = -1; x <= 6; x++) {
				for (int y = 0; y <= 4; y++) {
					for (int z = -2; z <= 2; z++) {
						level.setBlockAndUpdate(outside.offset(x, y, z), Blocks.AIR.defaultBlockState());
					}
				}
			}
			owner.snapTo(Vec3.atCenterOf(target.south(2)));
			stranger.snapTo(Vec3.atCenterOf(outside));
			helper.assertFalse(LandRegistry.mayBuild(stranger, level, target), "The stranger has claim access");
		}

		/** A cow standing still in the claim. */
		LivingEntity livestock() {
			ServerLevel level = helper.getLevel();
			@SuppressWarnings("unchecked")
			EntityType<Mob> type = (EntityType<Mob>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("cow"));
			Mob cow = type.create(level, EntitySpawnReason.TRIGGERED);
			helper.assertTrue(cow != null, "No cow");
			cow.setNoAi(true);
			cow.snapTo(Vec3.atCenterOf(target));
			level.addFreshEntity(cow);
			animal = cow;
			return cow;
		}

		void close() {
			if (animal != null) {
				animal.discard();
			}
			land.abandon(owner, book, key, true, 0);
		}
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
				for (int y = 2; y <= 3; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
	}

	/** A still husk (it does not burn by day) with no armour, facing south (yaw 0) or north (180), holding the stack. */
	private static Mob fighter(GameTestHelper helper, BlockPos pos, ItemStack held, float yaw) {
		@SuppressWarnings("unchecked")
		EntityType<Mob> type = (EntityType<Mob>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("husk"));
		Mob husk = helper.spawnWithNoFreeWill(type, pos);
		husk.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
		husk.setItemSlot(EquipmentSlot.MAINHAND, held);
		husk.setYRot(yaw);
		husk.setYHeadRot(yaw);
		husk.setYBodyRot(yaw);
		return husk;
	}

	private static double modifier(ItemStack stack, Identifier id) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
				.modifiers().stream().filter(entry -> entry.modifier().id().equals(id)).mapToDouble(entry -> entry.modifier().amount())
				.sum();
	}
}
