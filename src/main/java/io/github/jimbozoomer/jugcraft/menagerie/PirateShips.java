package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Moving pirate ships (docs/features/pirate-ships.md): the ship, its deck squares, and the natural spawner's tick. New
 * IDs only; the static galleon (OwnerGalleon), its crew and its data keep theirs.
 */
public final class PirateShips {
	public static EntityType<PirateShip> SHIP;
	public static EntityType<PirateShipDeck> DECK;

	private PirateShips() {
	}

	public static void register() {
		ResourceKey<EntityType<?>> ship = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id("pirate_ship"));
		SHIP = Registry.register(BuiltInRegistries.ENTITY_TYPE, ship, EntityType.Builder
				.<PirateShip>of(PirateShip::new, MobCategory.MISC).sized(PirateShip.WIDTH, PirateShip.HEIGHT).noLootTable()
				.fireImmune().clientTrackingRange(10).updateInterval(1).build(ship));
		ResourceKey<EntityType<?>> deck = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id("pirate_ship_deck"));
		DECK = Registry.register(BuiltInRegistries.ENTITY_TYPE, deck, EntityType.Builder
				.<PirateShipDeck>of(PirateShipDeck::new, MobCategory.MISC).sized(PirateShipDeck.WIDTH, 1.0F).noLootTable().noSave()
				.noSummon().fireImmune().clientTrackingRange(10).updateInterval(1).build(deck));
		ServerTickEvents.END_SERVER_TICK.register(server -> PirateSeas.tick(server.overworld()));
	}
}
