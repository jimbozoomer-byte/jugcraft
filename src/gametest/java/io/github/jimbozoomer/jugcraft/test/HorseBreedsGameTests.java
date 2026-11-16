package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.horses.BreedDimensions;
import io.github.jimbozoomer.jugcraft.horses.BreedHorse;
import io.github.jimbozoomer.jugcraft.horses.HorseBreed;
import io.github.jimbozoomer.jugcraft.horses.HorseBreeds;
import io.github.jimbozoomer.jugcraft.horses.HorseCargo;
import io.github.jimbozoomer.jugcraft.horses.HorseLineage;
import io.github.jimbozoomer.jugcraft.pegasus.JugcraftPegasus;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import io.github.jimbozoomer.jugcraft.pets.PetRoster;
import io.github.jimbozoomer.jugcraft.pets.Pets;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Server game tests for the horse breeds and the Pegasus's line (docs/features/horse-breeds.md): registration, sizes,
 * eggs and tags; the base roll and traits applied once; save and reload; crossbreeding and the flight rule; road speed
 * and recovery; and cargo conservation through pet recall, knock-out, reload, an open menu, an unloaded chunk, a smaller
 * capacity and death. Written for the repository's suite; not run in this change.
 */
public class HorseBreedsGameTests {
	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos at = helper.absolutePos(standAt);
		player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		return player;
	}

	private static void remove(ServerPlayer player) {
		player.stopRiding();
		player.level().getServer().getPlayerList().remove(player);
		player.discard();
	}

	private static void floor(GameTestHelper helper, int size) {
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** A breed horse spawned as an egg would (its base rolled, its traits applied), tamed by {@code owner} if given. */
	private static BreedHorse horse(GameTestHelper helper, HorseBreed breed, Vec3 relative, ServerPlayer owner) {
		ServerLevel level = helper.getLevel();
		BreedHorse horse = HorseBreeds.type(breed).create(level, EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(relative);
		horse.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		horse.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), EntitySpawnReason.COMMAND, null);
		if (owner != null) {
			horse.tameWithName(owner);
		}
		level.addFreshEntity(horse);
		return horse;
	}

	private static double modifier(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier id) {
		var instance = mob.getAttribute(attribute);
		var current = instance == null ? null : instance.getModifier(id);
		return current == null ? 0.0 : current.amount();
	}

	/** Every item in a container, by slot, as count-and-item text (for exact before/after comparisons). */
	private static String contents(SimpleContainer container) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				text.append(i).append('=').append(stack.getCount()).append('x').append(BuiltInRegistries.ITEM.getKey(stack.getItem())).append(';');
			}
		}
		return text.toString();
	}

	private static int count(SimpleContainer container, net.minecraft.world.item.Item item) {
		int total = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			if (container.getItem(i).is(item)) {
				total += container.getItem(i).getCount();
			}
		}
		return total;
	}

	private static int dropped(GameTestHelper helper, net.minecraft.world.item.Item item) {
		int total = 0;
		for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24))) {
			if (entity.getItem().is(item)) {
				total += entity.getItem().getCount();
			}
		}
		return total;
	}

	/** A saddled, bag-fitted Shire owned by {@code owner}, its bags holding {@code stacks} stacks of 5 iron ingots. */
	private static BreedHorse packedShire(GameTestHelper helper, ServerPlayer owner, Vec3 at, int stacks) {
		BreedHorse shire = horse(helper, HorseBreed.SHIRE, at, owner);
		shire.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
		shire.bags().setFitted(true);
		for (int i = 0; i < stacks; i++) {
			shire.bags().items().setItem(i, new ItemStack(Items.IRON_INGOT, 5));
		}
		return shire;
	}

	private static TagKey<EntityType<?>> vanillaTag(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace(path));
	}

	// ------------------------------------------------------------------------------------------------ registration

	/** Each breed is registered with its egg, its measured hitbox and seat, the saddle tag and its terrain tags. */
	@GameTest
	public void breedsAreRegisteredWithEggsSizesAndTags(GameTestHelper helper) {
		double vanillaSeat = EntityTypes.HORSE.getDimensions().attachments().get(EntityAttachment.PASSENGER, 0, 0).y;
		helper.assertTrue(Math.abs(vanillaSeat - BreedDimensions.of("horse").seat()) < 1e-5,
				"The generator uses the live vanilla horse attachment, not the skeleton horse seat: " + vanillaSeat);
		for (HorseBreed breed : HorseBreed.GROUND) {
			EntityType<BreedHorse> type = HorseBreeds.type(breed);
			helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getValue(Jugcraft.id(breed.id())) == type, breed.id() + " is registered");
			helper.assertTrue(SpawnEggItem.getType(new ItemStack(HorseBreeds.egg(breed))) == type, "its egg hatches it");
			helper.assertTrue(BuiltInRegistries.ITEM.getValue(Jugcraft.id(breed.id() + "_spawn_egg")) == HorseBreeds.egg(breed), "egg id");
			BreedHorse horse = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
			BreedDimensions.Size size = BreedDimensions.of(breed.id());
			helper.assertTrue(Math.abs(horse.getBbWidth() - size.width()) < 1e-4 && Math.abs(horse.getBbHeight() - size.height()) < 1e-4,
					breed.id() + " hitbox " + horse.getBbWidth() + " x " + horse.getBbHeight());
			helper.assertTrue(horse.breed() == breed && HorseBreeds.line(horse) == breed, "its breed is its type");
			helper.assertTrue(type.builtInRegistryHolder().is(vanillaTag("can_equip_saddle")), breed.id() + " can wear a saddle");
			helper.assertTrue(type.builtInRegistryHolder().is(vanillaTag("powder_snow_walkable_mobs")) == (breed == HorseBreed.ICELANDIC),
					"only the Icelandic walks on powder snow");
			helper.assertTrue(type.builtInRegistryHolder().is(vanillaTag("freeze_immune_entity_types"))
					== (breed == HorseBreed.ICELANDIC || breed == HorseBreed.MONGOLIAN), "the snow breeds never freeze");
			helper.assertTrue(!horse.canUseSlot(EquipmentSlot.BODY) && horse.canUseSlot(EquipmentSlot.SADDLE), "no armour slot; a saddle slot");
			horse.discard();
		}
		helper.assertTrue(BreedDimensions.of("giant_finnhorse").width() > 2.0F && BreedDimensions.of("shire_horse").width() < 2.0F,
				"the giant needs more than a two-wide gate; the Shire does not");
		helper.succeed();
	}

	/** A spawn rolls a vanilla-range base and applies its breed's traits once; applying them again changes nothing. */
	@GameTest(padding = 8)
	public void spawnRollsTheBaseAndAppliesTraitsOnce(GameTestHelper helper) {
		floor(helper, 9);
		for (HorseBreed breed : HorseBreed.GROUND) {
			BreedHorse horse = horse(helper, breed, new Vec3(4.5, 2, 4.5), null);
			double[] base = horse.base();
			helper.assertTrue(base[0] >= 15 && base[0] <= 30 && base[1] >= 0.1125 && base[1] <= 0.3375 && base[2] >= 0.4 && base[2] <= 1.0,
					breed.id() + " base in vanilla's ranges: " + Arrays.toString(base));
			double health = horse.getAttributeValue(Attributes.MAX_HEALTH);
			double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
			helper.assertTrue(Math.abs(health - base[0] * breed.health) < 1e-6, breed.id() + " health x" + breed.health + ": " + health);
			helper.assertTrue(Math.abs(speed - base[1] * breed.speed) < 1e-6, breed.id() + " speed x" + breed.speed + ": " + speed);
			helper.assertTrue(Math.abs(modifier(horse, Attributes.ARMOR, BreedHorse.ARMOR) - breed.armor) < 1e-9
					&& Math.abs(modifier(horse, Attributes.STEP_HEIGHT, BreedHorse.STEP) - breed.step) < 1e-9
					&& Math.abs(modifier(horse, Attributes.KNOCKBACK_RESISTANCE, BreedHorse.KNOCKBACK) - breed.knockback) < 1e-9, "bonuses");
			helper.assertTrue(horse.getHealth() == horse.getMaxHealth(), "spawned at full health");
			for (int i = 0; i < 3; i++) {
				horse.applyTraits();
			}
			helper.assertTrue(horse.getAttributeValue(Attributes.MAX_HEALTH) == health && horse.getAttributeValue(Attributes.MOVEMENT_SPEED) == speed,
					"applying the traits again never stacks them");
			horse.discard();
		}
		helper.succeed();
	}

	// ------------------------------------------------------------------------------------------------ saving

	/** Save and reload keep the breed, ancestry, parents, generation, base, traits, saddle and every cargo slot. */
	@GameTest(padding = 8)
	public void saveAndReloadKeepLineageTraitsAndCargo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 9);
		ServerPlayer owner = player(helper, new BlockPos(1, 2, 1));
		BreedHorse horse = packedShire(helper, owner, new Vec3(4.5, 2, 4.5), 27);
		int[] half = HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN));
		horse.setLineage(half, "shire_horse,friesian_horse", 3);
		horse.setBase(new double[] {27.0, 0.25, 0.8});
		horse.applyTraits();
		horse.bags().items().setItem(26, new ItemStack(Items.DIAMOND, 2));
		String cargo = contents(horse.bags().items());
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		horse.saveWithoutId(saved);
		BreedHorse copy = HorseBreeds.type(HorseBreed.SHIRE).create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(copy.getUUID().equals(horse.getUUID()) && copy.breed() == HorseBreed.SHIRE, "identity and breed");
		helper.assertTrue(Arrays.equals(copy.ancestry(), half) && copy.parents().equals("shire_horse,friesian_horse") && copy.generation() == 3,
				"ancestry, parents and generation: " + Arrays.toString(copy.ancestry()));
		helper.assertTrue(Arrays.equals(copy.base(), horse.base()), "base roll");
		helper.assertTrue(copy.getAttributeValue(Attributes.MAX_HEALTH) == horse.getAttributeValue(Attributes.MAX_HEALTH)
				&& copy.getAttributeValue(Attributes.MOVEMENT_SPEED) == horse.getAttributeValue(Attributes.MOVEMENT_SPEED), "traits");
		helper.assertTrue(copy.getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE) && copy.bags().fitted() && copy.bagsFitted(), "saddle and bags");
		helper.assertTrue(contents(copy.bags().items()).equals(cargo), "every slot, even past its capacity: " + contents(copy.bags().items()));
		copy.discard();
		horse.discard();
		remove(owner);
		helper.succeed();
	}

	// ------------------------------------------------------------------------------------------------ cargo conservation

	/** A pet recall stores the horse with its cargo, a summon brings back exactly the same cargo, and nothing drops. */
	@GameTest(maxTicks = 80, padding = 12)
	public void recallAndSummonKeepEveryItemOnce(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(6, 2, 6));
		BreedHorse shire = packedShire(helper, owner, new Vec3(3.5, 2, 3.5), 20);
		String cargo = contents(shire.bags().items());
		var id = shire.getUUID();
		Pets.enroll(shire);
		helper.assertTrue(Pets.action(owner, id.toString(), "recall").equals("Pet recalled."), "recalled");
		helper.assertTrue(shire.isRemoved() && dropped(helper, Items.IRON_INGOT) == 0, "recalled without dropping anything");
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(Pets.action(owner, id.toString(), "summon").equals("Pet summoned."), "summoned");
			BreedHorse back = (BreedHorse) helper.getLevel().getEntity(id);
			helper.assertTrue(back != null && contents(back.bags().items()).equals(cargo), "the same cargo, once");
			helper.assertTrue(back.getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE) && back.bags().fitted(), "saddle and bags kept");
			helper.assertTrue(Pets.action(owner, id.toString(), "summon").equals("Pet is already active."), "no second copy");
			helper.assertTrue(dropped(helper, Items.IRON_INGOT) == 0, "nothing on the ground");
			back.discard();
			remove(owner);
			helper.succeed();
		});
	}

	/** A knock-out mid-ride throws the rider, stores the horse with all its cargo and drops nothing (no death loot). */
	@GameTest(maxTicks = 60, padding = 12)
	public void aKnockoutMidRideKeepsTheCargoAndDropsNothing(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(6, 2, 6));
		BreedHorse shire = packedShire(helper, owner, new Vec3(3.5, 2, 3.5), 27);
		String cargo = contents(shire.bags().items());
		owner.startRiding(shire, true, false);
		helper.assertTrue(owner.getVehicle() == shire, "riding");
		shire.hurtServer(helper.getLevel(), shire.damageSources().generic(), 10000);
		CompoundTag entry = PetRoster.get(helper.getLevel().getServer()).entries.get(shire.getUUID());
		helper.assertTrue(entry != null && entry.getStringOr("state", "").equals("KNOCKED_OUT") && shire.isRemoved(), "knocked out and stored");
		helper.assertTrue(owner.getVehicle() == null, "the rider is thrown");
		helper.assertTrue(dropped(helper, Items.IRON_INGOT) == 0 && dropped(helper, Items.CHEST) == 0 && dropped(helper, Items.SADDLE) == 0,
				"a knock-out drops nothing");
		entry.putLong("ready", 0L);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(Pets.action(owner, shire.getUUID().toString(), "summon").equals("Pet summoned."), "recovered");
			BreedHorse back = (BreedHorse) helper.getLevel().getEntity(shire.getUUID());
			helper.assertTrue(back != null && contents(back.bags().items()).equals(cargo), "every item came back once");
			back.discard();
			remove(owner);
			helper.succeed();
		});
	}

	/** Recalled while its bags are open: the menu closes and no click can move anything out of the stored horse. */
	@GameTest(maxTicks = 40, padding = 12)
	public void aRecallWithTheBagsOpenMovesNothing(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
		BreedHorse shire = packedShire(helper, owner, new Vec3(4.5, 2, 4.5), 5);
		String cargo = contents(shire.bags().items());
		owner.setShiftKeyDown(true);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		shire.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(owner.containerMenu instanceof ChestMenu, "sneak-use with a stick opens the bags");
		ChestMenu open = (ChestMenu) owner.containerMenu;
		helper.assertTrue(open.getRowCount() == 3, "a Shire's bags show three rows");
		Pets.enroll(shire);
		helper.assertTrue(Pets.action(owner, shire.getUUID().toString(), "recall").equals("Pet recalled."), "recalled with the menu open");
		helper.assertTrue(owner.containerMenu == owner.inventoryMenu, "the recall closed the menu");
		helper.assertTrue(!open.stillValid(owner) && !shire.bags().mayUse(owner), "the old menu is no longer valid");
		CompoundTag entry = PetRoster.get(helper.getLevel().getServer()).entries.get(shire.getUUID());
		BreedHorse stored = (BreedHorse) EntityType.loadEntityRecursive(TagValueInput.create(ProblemReporter.DISCARDING,
				helper.getLevel().registryAccess(), entry.getCompoundOrEmpty("entity")), helper.getLevel(), EntitySpawnReason.LOAD, EntityProcessor.NOP);
		helper.assertTrue(contents(stored.bags().items()).equals(cargo), "the stored horse has all of it");
		helper.assertTrue(owner.getInventory().countItem(Items.IRON_INGOT) == 0 && dropped(helper, Items.IRON_INGOT) == 0, "and nobody else has any");
		stored.discard();
		remove(owner);
		helper.succeed();
	}

	/**
	 * A recall queued while the horse is in an unloaded chunk stores it once it loads; a stale copy of it (as a chunk saved
	 * earlier would hold) is discarded on load, so its cargo is never doubled.
	 */
	@GameTest(maxTicks = 60, padding = 12)
	public void anUnloadedRecallNeverDoublesTheCargo(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(6, 2, 6));
		BreedHorse shire = HorseBreeds.type(HorseBreed.SHIRE).create(level, EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(new Vec3(3.5, 2, 3.5));
		shire.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		shire.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), EntitySpawnReason.COMMAND, null);
		shire.tameWithName(owner);
		shire.bags().setFitted(true);
		shire.bags().items().setItem(0, new ItemStack(Items.GOLD_INGOT, 7));
		Pets.enroll(shire);
		TagValueOutput staleSave = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		helper.assertTrue(shire.save(staleSave), "the unloaded save includes its entity type");
		helper.assertTrue(Pets.action(owner, shire.getUUID().toString(), "recall").startsWith("Recall queued"), "queued while unloaded");
		level.addFreshEntity(shire);
		BreedHorse[] stale = new BreedHorse[1];
		// Schedule both steps before execution: 26.3 iterates its live callback map while invoking them.
		helper.runAfterDelay(22, () -> {
			CompoundTag entry = PetRoster.get(level.getServer()).entries.get(shire.getUUID());
			helper.assertTrue(shire.isRemoved() && entry.getStringOr("state", "").equals("STORED"), "stored once it loaded");
			stale[0] = (BreedHorse) EntityType.loadEntityRecursive(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(),
					staleSave.buildResult()), level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
			level.addFreshEntity(stale[0]);
		});
		helper.runAfterDelay(24, () -> {
			helper.assertTrue(stale[0] != null && stale[0].isRemoved(), "the stale copy cannot come back");
			helper.assertTrue(dropped(helper, Items.GOLD_INGOT) == 0, "and drops nothing");
			remove(owner);
			helper.succeed();
		});
	}

	/**
	 * A smaller capacity never deletes: a half-Shire (one row) holding 27 stacks keeps nine and hands the other 18 back to
	 * its owner when opened; a horse that can carry no bags at all returns everything and its chest.
	 */
	@GameTest(padding = 12)
	public void aSmallerCapacityHandsOverflowBackExactlyOnce(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
		BreedHorse shire = packedShire(helper, owner, new Vec3(4.5, 2, 4.5), 27);
		shire.setLineage(HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN)), "shire_horse,friesian_horse", 1);
		shire.applyTraits();
		helper.assertTrue(shire.traits().cargoRows() == 1, "a half Shire carries one row");
		int stored = count(shire.bags().items(), Items.IRON_INGOT);
		int handedBack = shire.bags().settle(owner, HorseCargo.capacity(shire.traits().cargoRows()));
		int inBags = count(shire.bags().items(), Items.IRON_INGOT);
		int inInventory = owner.getInventory().countItem(Items.IRON_INGOT);
		helper.assertTrue(handedBack == 18 && inBags == 45 && inBags + inInventory + dropped(helper, Items.IRON_INGOT) == stored,
				"9 stacks kept, 18 handed back, none lost or doubled: " + inBags + " + " + inInventory);
		BreedHorse friesian = horse(helper, HorseBreed.FRIESIAN, new Vec3(7.5, 2, 7.5), owner);
		friesian.bags().setFitted(true);
		friesian.bags().items().setItem(3, new ItemStack(Items.EMERALD, 4));
		owner.setShiftKeyDown(true);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		friesian.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(!friesian.bags().fitted() && friesian.bags().stacks() == 0, "a horse that cannot carry bags gives them back");
		helper.assertTrue(owner.getInventory().countItem(Items.EMERALD) + dropped(helper, Items.EMERALD) == 4
				&& owner.getInventory().countItem(Items.CHEST) + dropped(helper, Items.CHEST) == 1, "with every item and the chest");
		shire.discard();
		friesian.discard();
		remove(owner);
		helper.succeed();
	}

	/** Bags need a chest from the owner; a stranger can neither fit nor open them; a real death drops everything once. */
	@GameTest(maxTicks = 40, padding = 12)
	public void onlyTheOwnerUsesTheBagsAndDeathDropsThemOnce(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
		ServerPlayer stranger = player(helper, new BlockPos(6, 2, 6));
		BreedHorse giant = horse(helper, HorseBreed.GIANT_FINNHORSE, new Vec3(4.5, 2, 4.5), owner);
		stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
		giant.mobInteract(stranger, InteractionHand.MAIN_HAND);
		helper.assertTrue(!giant.bags().fitted() && stranger.getMainHandItem().is(Items.CHEST), "a stranger cannot fit them");
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
		giant.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(giant.bags().fitted() && owner.getMainHandItem().isEmpty(), "the owner's chest fits them");
		giant.bags().items().setItem(0, new ItemStack(Items.COPPER_INGOT, 9));
		giant.bags().items().setItem(20, new ItemStack(Items.COPPER_INGOT, 3));
		stranger.setShiftKeyDown(true);
		stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		giant.mobInteract(stranger, InteractionHand.MAIN_HAND);
		helper.assertTrue(!(stranger.containerMenu instanceof ChestMenu), "a stranger cannot open them");
		remove(owner);
		Pets.retired(giant);
		// An owner's logout must still protect a pet. Exercise real death only after ownership is relinquished.
		giant.setOwner(null);
		giant.hurtServer(helper.getLevel(), giant.damageSources().generic(), 10000);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(giant.isDeadOrDying() || giant.isRemoved(), "it died");
			helper.assertTrue(dropped(helper, Items.COPPER_INGOT) == 12 && dropped(helper, Items.CHEST) == 1, "12 ingots and the chest, once");
			remove(stranger);
			helper.succeed();
		});
	}

	/**
	 * Overflow into a full survival inventory drops beside the horse; into a partly full one, it fills the free slots first
	 * (unstackable cargo, so each slot takes one): every item once.
	 */
	@GameTest(padding = 12)
	public void overflowIntoFullAndPartlyFullInventoriesLosesNothing(GameTestHelper helper) {
		floor(helper, 13);
		for (int fill : new int[] {36, 30}) {
			ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
			for (int i = 0; i < fill; i++) {
				owner.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
			}
			BreedHorse shire = packedShire(helper, owner, new Vec3(4.5, 2, 4.5), 0);
			for (int i = 0; i < 27; i++) {
				shire.bags().items().setItem(i, new ItemStack(Items.SADDLE));
			}
			shire.setLineage(HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.ICELANDIC)), "shire_horse,icelandic_horse", 1);
			shire.applyTraits();
			int before = dropped(helper, Items.SADDLE);
			int handedBack = shire.bags().settle(owner, HorseCargo.capacity(shire.traits().cargoRows()));
			int inBags = count(shire.bags().items(), Items.SADDLE);
			int inInventory = owner.getInventory().countItem(Items.SADDLE);
			int onGround = dropped(helper, Items.SADDLE) - before;
			helper.assertTrue(handedBack == 18 && inBags + inInventory + onGround == 27,
					"inventory " + fill + "/36 full: " + inBags + " in bags + " + inInventory + " carried + " + onGround + " dropped");
			helper.assertTrue(fill == 36 ? inInventory == 0 && onGround == 18 : inInventory == 6 && onGround == 12,
					"a full inventory takes nothing, a partly full one takes what fits: " + inInventory + " / " + onGround);
			for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24))) {
				item.discard();
			}
			shire.discard();
			remove(owner);
		}
		helper.succeed();
	}

	/** A creative player's full inventory would swallow a stack (vanilla empties it): the overflow drops instead, whole. */
	@GameTest(padding = 12)
	public void overflowForACreativePlayerIsNeverVoided(GameTestHelper helper) {
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
		owner.setGameMode(GameType.CREATIVE);
		for (int i = 0; i < 36; i++) {
			owner.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
		}
		BreedHorse shire = packedShire(helper, owner, new Vec3(4.5, 2, 4.5), 27);
		shire.setLineage(HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.ICELANDIC)), "shire_horse,icelandic_horse", 1);
		shire.applyTraits();
		int handedBack = shire.bags().settle(owner, HorseCargo.capacity(shire.traits().cargoRows()));
		int inBags = count(shire.bags().items(), Items.IRON_INGOT);
		helper.assertTrue(handedBack == 18 && inBags == 9 * 5 && dropped(helper, Items.IRON_INGOT) == 18 * 5,
				"every overflow stack lies beside the horse, none voided: " + dropped(helper, Items.IRON_INGOT));
		shire.discard();
		remove(owner);
		helper.succeed();
	}

	// ------------------------------------------------------------------------------------------------ breeding

	/** Breeds pair with breeds; a foal blends its ancestry, inherits a capped base and has its parents' line recorded. */
	@GameTest(padding = 12)
	public void breedsCrossAndFoalsInherit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(6, 2, 6));
		BreedHorse shire = horse(helper, HorseBreed.SHIRE, new Vec3(2.5, 2, 2.5), owner);
		BreedHorse friesian = horse(helper, HorseBreed.FRIESIAN, new Vec3(5.5, 2, 2.5), owner);
		AbstractHorse plain = EntityTypes.HORSE.create(level, EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(new Vec3(8.5, 2, 2.5));
		plain.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		plain.tameWithName(owner);
		level.addFreshEntity(plain);
		Pegasus pegasus = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.COMMAND);
		Vec3 pat = helper.absoluteVec(new Vec3(8.5, 2, 8.5));
		pegasus.snapTo(pat.x, pat.y, pat.z, 0.0F, 0.0F);
		pegasus.tameWithName(owner);
		level.addFreshEntity(pegasus);
		for (var animal : List.of(shire, friesian, plain, pegasus)) {
			animal.setInLove(owner);
		}
		helper.assertTrue(shire.canMate(friesian) && friesian.canMate(shire), "two breeds pair");
		helper.assertTrue(!shire.canMate(plain) && !shire.canMate(pegasus) && !pegasus.canMate(shire) && !pegasus.canMate(plain),
				"a breed or a Pegasus does not pair with another line");
		BreedHorse foal = (BreedHorse) shire.getBreedOffspring(level, friesian);
		helper.assertTrue(foal != null && (foal.breed() == HorseBreed.SHIRE || foal.breed() == HorseBreed.FRIESIAN), "a ground foal");
		helper.assertTrue(HorseLineage.share(foal.ancestry(), HorseBreed.SHIRE) == 2048 && HorseLineage.share(foal.ancestry(), HorseBreed.FRIESIAN) == 2048,
				"half each: " + Arrays.toString(foal.ancestry()));
		helper.assertTrue(foal.generation() == 1 && foal.parents().equals("shire_horse,friesian_horse"), "its parents' line");
		double[] base = foal.base();
		helper.assertTrue(base[0] >= 15 && base[0] <= 30 && base[1] >= 0.1125 && base[1] <= 0.3375 && base[2] >= 0.4 && base[2] <= 1.0, "base in range");
		HorseLineage.Traits t = foal.traits();
		helper.assertTrue(t.speed() < HorseBreed.FRIESIAN.speed && t.speed() > HorseBreed.SHIRE.speed && t.cargoRows() <= 1, "blended, capped: " + t);
		helper.assertTrue(pegasus.getBreedOffspring(level, shire) == null && shire.getBreedOffspring(level, pegasus) == null, "no Pegasus cross");
		// A vanilla horse that starts a pairing with a breed or a Pegasus gets no foal (HorseLineageMixin).
		int horses = level.getEntitiesOfClass(AbstractHorse.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24)).size();
		plain.spawnChildFromBreeding(level, shire);
		plain.setInLove(owner);
		pegasus.setInLove(owner);
		plain.spawnChildFromBreeding(level, pegasus);
		helper.assertTrue(level.getEntitiesOfClass(AbstractHorse.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24)).size() == horses,
				"no foal crosses the lines");
		foal.discard();
		for (var animal : List.of(shire, friesian, plain, pegasus)) {
			animal.discard();
		}
		remove(owner);
		helper.succeed();
	}

	/** Two Pegasi have a Pegasus foal (the one way a foal flies); a ground cross never has one. */
	@GameTest(padding = 12)
	public void onlyTwoPegasiHaveAFlyingFoal(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 13);
		ServerPlayer owner = player(helper, new BlockPos(6, 2, 6));
		Pegasus a = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.COMMAND);
		Pegasus b = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.COMMAND);
		for (Pegasus p : List.of(a, b)) {
			Vec3 at = helper.absoluteVec(new Vec3(p == a ? 3.5 : 6.5, 2, 3.5));
			p.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
			p.tameWithName(owner);
			level.addFreshEntity(p);
			p.setInLove(owner);
		}
		helper.assertTrue(a.canFallInLove() || a.isInLove(), "a Pegasus can be bred");
		helper.assertTrue(a.canMate(b), "two Pegasi pair");
		var foal = a.getBreedOffspring(level, b);
		helper.assertTrue(foal instanceof Pegasus pegasus && pegasus.getAttributeValue(Attributes.MAX_HEALTH) == Pegasus.HEALTH,
				"their foal is a Pegasus with the line's own stats");
		helper.assertTrue(HorseLineage.foalFlies(HorseBreed.PEGASUS, HorseBreed.PEGASUS) && !HorseLineage.foalFlies(HorseBreed.SHIRE, HorseBreed.PEGASUS),
				"the rule");
		if (foal != null) {
			foal.discard();
		}
		a.discard();
		b.discard();
		remove(owner);
		helper.succeed();
	}

	// ------------------------------------------------------------------------------------------------ traits at work

	/** A Friesian is faster on a road block and not on stone; a Shire gets no road bonus. */
	@GameTest(maxTicks = 40, padding = 8)
	public void roadSpeedOnlyOnRoadsAndOnlyWithFriesianBlood(GameTestHelper helper) {
		for (int x = 0; x < 9; x++) {
			for (int z = 0; z < 9; z++) {
				helper.setBlock(new BlockPos(x, 1, z), x < 4 ? Blocks.DIRT_PATH : Blocks.STONE);
			}
		}
		BreedHorse onRoad = horse(helper, HorseBreed.FRIESIAN, new Vec3(1.5, 2, 1.5), null);
		BreedHorse offRoad = horse(helper, HorseBreed.FRIESIAN, new Vec3(7.5, 2, 7.5), null);
		BreedHorse shire = horse(helper, HorseBreed.SHIRE, new Vec3(1.5, 2, 6.5), null);
		BreedHorse cross = horse(helper, HorseBreed.SHIRE, new Vec3(1.5, 2, 4.0), null);
		cross.setLineage(HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN)), "shire_horse,friesian_horse", 1);
		cross.applyTraits();
		for (BreedHorse h : List.of(onRoad, offRoad, shire, cross)) {
			h.setNoAi(true);
			// NoAI freezes travel, including the settling step. Place this stationary fixture on its actual surface.
			BlockPos support = h.blockPosition().below();
			double top = helper.getLevel().getBlockState(support).getCollisionShape(helper.getLevel(), support)
					.max(net.minecraft.core.Direction.Axis.Y);
			h.setPos(h.getX(), support.getY() + top, h.getZ());
			h.setOnGround(true);
		}
		helper.assertTrue(Math.abs(onRoad.roadBonus() - 0.1) < 1e-6 && shire.roadBonus() == 0.0 && Math.abs(cross.roadBonus() - 0.05) < 1e-6,
				"the synced road bonus is the ancestry's, not the look's: " + cross.roadBonus());
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(Math.abs(modifier(onRoad, Attributes.MOVEMENT_SPEED, BreedHorse.ROAD) - 0.1) < 1e-6,
					"a Friesian on a path is faster: pos=" + onRoad.position() + " ground=" + onRoad.onGround()
					+ " support=" + helper.getLevel().getBlockState(onRoad.getOnPos()) + " bonus=" + onRoad.roadBonus());
			helper.assertTrue(modifier(offRoad, Attributes.MOVEMENT_SPEED, BreedHorse.ROAD) == 0.0, "not on stone");
			helper.assertTrue(modifier(shire, Attributes.MOVEMENT_SPEED, BreedHorse.ROAD) == 0.0, "a Shire has no road bonus");
			helper.assertTrue(Math.abs(modifier(cross, Attributes.MOVEMENT_SPEED, BreedHorse.ROAD) - 0.05) < 1e-6,
					"a Shire-looking half Friesian gets half the bonus");
			onRoad.setOnGround(false);
			onRoad.tick();
			helper.assertTrue(modifier(onRoad, Attributes.MOVEMENT_SPEED, BreedHorse.ROAD) == 0.0,
					"leaving the ground removes the road bonus");
			for (BreedHorse h : List.of(onRoad, offRoad, shire, cross)) {
				h.discard();
			}
			helper.succeed();
		});
	}

	/** A calm Mongolian slowly regains health; an Icelandic does not (beyond vanilla's own rare regeneration). */
	@GameTest(maxTicks = 800, padding = 8)
	public void aCalmMongolianRecovers(GameTestHelper helper) {
		floor(helper, 9);
		BreedHorse mongolian = horse(helper, HorseBreed.MONGOLIAN, new Vec3(2.5, 2, 2.5), null);
		mongolian.setNoAi(true);
		mongolian.setHealth(5.0F);
		helper.runAfterDelay(HorseLineage.CALM_TICKS + 2 * HorseLineage.RECOVERY_TICKS + 20, () -> {
			helper.assertTrue(mongolian.getHealth() >= 6.0F, "it recovered: " + mongolian.getHealth());
			helper.assertTrue(mongolian.getHealth() <= mongolian.getMaxHealth(), "never past full");
			mongolian.discard();
			helper.succeed();
		});
	}

	/** The Pegasus's light pack: one row, fitted only while saddled, kept through a reload. */
	@GameTest(padding = 8)
	public void thePegasusPackRidesOnTheSaddle(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 9);
		ServerPlayer owner = player(helper, new BlockPos(5, 2, 5));
		Pegasus pegasus = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(new Vec3(3.5, 2, 3.5));
		pegasus.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		pegasus.tameWithName(owner);
		level.addFreshEntity(pegasus);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CHEST));
		pegasus.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(!pegasus.pack().fitted() && owner.getMainHandItem().is(Items.CHEST), "no pack without a saddle");
		pegasus.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
		pegasus.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(pegasus.pack().fitted() && pegasus.packFitted(), "saddled, the chest straps on a pack");
		pegasus.pack().items().setItem(0, new ItemStack(Items.BREAD, 16));
		owner.setShiftKeyDown(true);
		owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		pegasus.mobInteract(owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(owner.containerMenu instanceof ChestMenu menu && menu.getRowCount() == Pegasus.PACK_ROWS, "it opens one row");
		owner.closeContainer();
		TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		pegasus.saveWithoutId(saved);
		Pegasus copy = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
		helper.assertTrue(copy.pack().fitted() && copy.pack().items().getItem(0).is(Items.BREAD) && copy.pack().items().getItem(0).getCount() == 16,
				"the pack and its bread survive a reload");
		copy.discard();
		pegasus.discard();
		remove(owner);
		helper.succeed();
	}
}
