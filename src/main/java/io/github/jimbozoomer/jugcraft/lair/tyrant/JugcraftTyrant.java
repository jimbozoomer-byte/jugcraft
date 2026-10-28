package io.github.jimbozoomer.jugcraft.lair.tyrant;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.rime.Rimebind;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.SluiceGateBlock;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;

/**
 * The Cinder Tyrant's registrations (docs/features/cinder-tyrant.md): he, his Cinderlings, his gobs of magma and his
 * falling cinders; his loot (the Tyrant Scale, the Salamander Charm and the Tyrant's Crest; the Cinderbrand and the Magmaw
 * are Arms VII trophies, weapons/ArmVariants.java, with their Ember boon). He is sunk in the crucible of every Cinder Kiln
 * as it is placed. Since Concordance Chunk E (docs/features/cinder-tyrant-concordance.md): the Quench Flask, the ordinary
 * player's preparation for his kiln, thrown; and he hears of the sluices players open and the Rimebinds that take on him.
 */
public final class JugcraftTyrant {
	public static EntityType<CinderTyrantEntity> CINDER_TYRANT;
	public static EntityType<CinderlingEntity> CINDERLING;
	public static EntityType<MagmaGobEntity> MAGMA_GOB;
	public static EntityType<FallingCinderEntity> FALLING_CINDER;
	public static EntityType<ThrownQuenchFlask> THROWN_QUENCH_FLASK;
	public static Item TYRANT_SCALE;
	public static Item SALAMANDER_CHARM;
	public static Item TYRANT_CREST;
	public static Item QUENCH_FLASK;

	private JugcraftTyrant() {
	}

	public static void register() {
		// Concordance Chunk B (B-01): his rewards may wait for a player, kept under his name.
		LairRewards.payer(CinderTyrantLoot.BOSS, CinderTyrantLoot::pay);
		CINDER_TYRANT = entity("cinder_tyrant", EntityType.Builder.<CinderTyrantEntity>of(CinderTyrantEntity::new, MobCategory.MONSTER)
				.sized(2.6F, 2.2F).eyeHeight(1.4F).fireImmune().noLootTable().clientTrackingRange(12));
		FabricDefaultAttributeRegistry.register(CINDER_TYRANT, CinderTyrantEntity.createAttributes());
		CINDERLING = entity("cinderling", EntityType.Builder.<CinderlingEntity>of(CinderlingEntity::new, MobCategory.MONSTER)
				.sized(0.8F, 0.5F).eyeHeight(0.35F).fireImmune().noLootTable().noSave().clientTrackingRange(8));
		FabricDefaultAttributeRegistry.register(CINDERLING, CinderlingEntity.createAttributes());
		MAGMA_GOB = entity("magma_gob", EntityType.Builder.<MagmaGobEntity>of(MagmaGobEntity::new, MobCategory.MISC)
				.sized(0.6F, 0.6F).fireImmune().noLootTable().noSave().clientTrackingRange(8).updateInterval(1));
		FALLING_CINDER = entity("falling_cinder", EntityType.Builder.<FallingCinderEntity>of(FallingCinderEntity::new, MobCategory.MISC)
				.sized(0.5F, 0.8F).fireImmune().noLootTable().noSave().clientTrackingRange(8).updateInterval(1));
		THROWN_QUENCH_FLASK = entity("quench_flask", EntityType.Builder.<ThrownQuenchFlask>of(ThrownQuenchFlask::new, MobCategory.MISC)
				.sized(0.25F, 0.25F).noLootTable().clientTrackingRange(4).updateInterval(10));

		// Each says what it is for in a grey line (tooltip.jugcraft.<id>).
		TYRANT_SCALE = item("tyrant_scale", new Item.Properties().rarity(Rarity.UNCOMMON));
		SALAMANDER_CHARM = item("salamander_charm", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		// Worn on the head, it is drawn there as its own model (its "head" display), as the costume hats are.
		TYRANT_CREST = item("tyrant_crest", new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(
				DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC).build()));
		ResourceKey<Item> flaskKey = ResourceKey.create(Registries.ITEM, Jugcraft.id("quench_flask"));
		QUENCH_FLASK = Registry.register(BuiltInRegistries.ITEM, flaskKey, new QuenchFlaskItem(new Item.Properties().stacksTo(16).setId(flaskKey)));

		Lairs.onPlaced(Lair.CINDER_KILN, CinderTyrantEntity::summon);
		SalamanderCharm.register();
		// Concordance Chunk E (E3, E4): whoever opens the sluice that quenches him, and a Rimebind on him while he is quenched.
		SluiceGateBlock.onOpened((level, trough, player) -> {
			if (trough.isEmpty()) {
				return;
			}
			AABB near = new AABB(trough.iterator().next()).inflate(64.0);
			for (CinderTyrantEntity tyrant : level.getEntitiesOfClass(CinderTyrantEntity.class, near, CinderTyrantEntity::isAlive)) {
				tyrant.sluiceOpened(level, trough, player);
			}
		});
		Rimebind.onBound((caster, target) -> {
			if (target instanceof CinderTyrantEntity tyrant && tyrant.level() instanceof ServerLevel level) {
				tyrant.rimebound(level, caster);
			}
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(TYRANT_SCALE));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(SALAMANDER_CHARM);
			output.accept(TYRANT_CREST);
			output.accept(QUENCH_FLASK);
		});
	}

	/** The Cinderbrand and the Magmaw, registered with the other Arms VII trophies (weapons/ArmVariants.java). */
	public static Item cinderbrand() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("cinderbrand"));
	}

	public static Item magmaw() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("magmaw"));
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String id, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	private static Item item(String id, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ITEM, key, new DescribedItem(properties.setId(key)));
	}
}
