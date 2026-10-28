package io.github.jimbozoomer.jugcraft.lair.horseman;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import io.github.jimbozoomer.jugcraft.weapons.DreadBoon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * The Headless Horseman's lair fight's registrations (docs/features/hessian-horseman.md): he, his phantom riders, the
 * pumpkin he tosses and the lantern head he hurls; his loot (the Hessian's Spur; the Hessian's Tricorn; the Horseman's
 * Lantern is the summoned Horseman's, agriculture's; the Hollowflail and the Hessian Sabre are Arms VII trophies,
 * weapons/ArmVariants.java, with their Dread boon). He is set waiting at the bridge's foot of every Church Bridge as it is
 * placed (part 1, docs/features/church-bridge.md), and told when a player lights one of its braziers.
 *
 * <p>The Hessian's Spur: held in the offhand, the horse its holder rides gallops {@value #SPUR_SPEED} faster at night in
 * the Overworld: a movement-speed modifier on the horse, checked every {@value #SPUR_CHECK} ticks for each player riding
 * one, and taken away again once its rider dismounts, puts the spur away or day comes. It is never saved; decided on the
 * server.
 */
public final class JugcraftHorseman {
	public static final double SPUR_SPEED = 0.2;
	public static final int SPUR_CHECK = 10;
	private static final Identifier SPURRED = Jugcraft.id("hessians_spur");
	public static EntityType<HessianHorsemanEntity> HESSIAN_HORSEMAN;
	public static EntityType<PhantomRiderEntity> PHANTOM_RIDER;
	public static EntityType<HessianPumpkinEntity> HESSIAN_PUMPKIN;
	public static EntityType<HessianPumpkinEntity> HESSIAN_HEAD;
	public static Item HESSIANS_SPUR;
	public static Item HESSIANS_TRICORN;
	/** The horses spurred now, and the level each is in; at most one for each player riding. */
	private static final Map<UUID, ResourceKey<Level>> SPURRED_HORSES = new HashMap<>();

	private JugcraftHorseman() {
	}

	public static void register() {
		// His rewards may wait for a player (lair/LairRewards), as every lair boss's do.
		LairRewards.payer(HorsemanLoot.BOSS, HorsemanLoot::pay);
		HESSIAN_HORSEMAN = entity("hessian_horseman", EntityType.Builder.<HessianHorsemanEntity>of(HessianHorsemanEntity::new, MobCategory.MONSTER)
				.sized(1.5F, 3.1F).eyeHeight(2.7F).fireImmune().noLootTable().clientTrackingRange(14));
		FabricDefaultAttributeRegistry.register(HESSIAN_HORSEMAN, HessianHorsemanEntity.createAttributes());
		PHANTOM_RIDER = entity("phantom_rider", EntityType.Builder.<PhantomRiderEntity>of(PhantomRiderEntity::new, MobCategory.MISC)
				.sized(1.4F, 2.8F).fireImmune().noLootTable().noSave().clientTrackingRange(10).updateInterval(1));
		HESSIAN_PUMPKIN = entity("hessian_pumpkin", EntityType.Builder.<HessianPumpkinEntity>of(HessianPumpkinEntity::new, MobCategory.MISC)
				.sized(0.8F, 0.8F).fireImmune().noLootTable().noSave().clientTrackingRange(8).updateInterval(1));
		HESSIAN_HEAD = entity("hessian_head", EntityType.Builder.<HessianPumpkinEntity>of(HessianPumpkinEntity::new, MobCategory.MISC)
				.sized(1.0F, 1.0F).fireImmune().noLootTable().noSave().clientTrackingRange(8).updateInterval(1));

		// Each says what it is for in a grey line (tooltip.jugcraft.<id>).
		HESSIANS_SPUR = item("hessians_spur", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		// Worn on the head, it is drawn there as its own model (its "head" display), as the costume hats are.
		HESSIANS_TRICORN = item("hessians_tricorn", new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(
				DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build()));

		Lairs.onPlaced(Lair.CHURCH_BRIDGE, HessianHorsemanEntity::summon);
		Lairs.onRelit(JugcraftHorseman::relit);
		DreadBoon.register();
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SPURRED_HORSES.clear());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % SPUR_CHECK == 0) {
				spur(server);
			}
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(HESSIANS_SPUR);
			output.accept(HESSIANS_TRICORN);
		});
	}

	/** A player lit a lair brazier: the Horseman whose hollow it is (if he is near) answers. */
	private static void relit(ServerLevel level, BlockPos pos, ServerPlayer player) {
		for (HessianHorsemanEntity horseman : level.getEntitiesOfClass(HessianHorsemanEntity.class, new AABB(pos).inflate(64.0),
				HessianHorsemanEntity::isAlive)) {
			if (horseman.brazierLit(level, pos, player)) {
				return;
			}
		}
	}

	/** Whether {@code player} spurs the horse they ride now: the spur in their offhand, at night, in the Overworld. */
	public static boolean spurs(ServerPlayer player) {
		Level level = player.level();
		return player.getVehicle() instanceof AbstractHorse && player.getOffhandItem().is(HESSIANS_SPUR) && level.dimension() == Level.OVERWORLD
				&& !level.isBrightOutside();
	}

	/** Each horse ridden by a player who spurs it gallops faster; any horse no longer spurred is as it was. */
	static void spur(MinecraftServer server) {
		Set<UUID> now = new HashSet<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (spurs(player) && player.getVehicle() instanceof AbstractHorse horse) {
				speed(horse, true);
				now.add(horse.getUUID());
				SPURRED_HORSES.put(horse.getUUID(), horse.level().dimension());
			}
		}
		for (Iterator<Map.Entry<UUID, ResourceKey<Level>>> it = SPURRED_HORSES.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, ResourceKey<Level>> entry = it.next();
			if (now.contains(entry.getKey())) {
				continue;
			}
			ServerLevel level = server.getLevel(entry.getValue());
			if (level != null && level.getEntity(entry.getKey()) instanceof AbstractHorse horse) {
				speed(horse, false);
			}
			it.remove();
		}
	}

	/** Puts the spur's speed on {@code horse}, or takes it off. */
	static void speed(AbstractHorse horse, boolean on) {
		AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		boolean has = speed.getModifier(SPURRED) != null;
		if (on && !has) {
			speed.addTransientModifier(new AttributeModifier(SPURRED, SPUR_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		} else if (!on && has) {
			speed.removeModifier(SPURRED);
		}
	}

	/** Whether {@code horse} gallops spurred now. */
	public static boolean spurred(AbstractHorse horse) {
		AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
		return speed != null && speed.getModifier(SPURRED) != null;
	}

	/** The Hollowflail and the Hessian Sabre, registered with the other Arms VII trophies (weapons/ArmVariants.java). */
	public static Item hollowflail() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("hollowflail"));
	}

	public static Item hessianSabre() {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id("hessian_sabre"));
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
