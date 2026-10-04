package io.github.jimbozoomer.jugcraft.walker;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Batch 47: the Diesel Walker (docs/features/diesel-walker.md). While a player pilots one, their own attack and use
 * are switched off (on both sides): those buttons drive the walker's fist and drill instead.
 */
public final class JugcraftWalkers {
	public static EntityType<DieselWalker> DIESEL_WALKER;
	public static Item DIESEL_WALKER_ITEM;

	private JugcraftWalkers() {
	}

	public static void register() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id("diesel_walker"));
		DIESEL_WALKER = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder
				.<DieselWalker>of(DieselWalker::new, MobCategory.MISC).sized(DieselWalker.WIDTH, DieselWalker.HEIGHT).noLootTable()
				.clientTrackingRange(10).updateInterval(1).build(key));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id("diesel_walker"));
		DIESEL_WALKER_ITEM = Registry.register(BuiltInRegistries.ITEM, itemKey,
				new DieselWalkerItem(new Item.Properties().setId(itemKey).stacksTo(1)));

		PayloadTypeRegistry.serverboundPlay().register(WalkerInputPayload.TYPE, WalkerInputPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(WalkerInputPayload.TYPE, (payload, context) -> {
			if (context.player().getVehicle() instanceof DieselWalker walker) {
				walker.steer(context.player(), payload.forward(), payload.turn(), payload.jump(), payload.drill(), payload.punch());
			}
		});
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> piloting(player) ? InteractionResult.FAIL
				: entity instanceof DieselWalker walker ? walker.use(player, hand) : InteractionResult.PASS);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> piloting(player) ? InteractionResult.FAIL : InteractionResult.PASS);
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> piloting(player) ? InteractionResult.FAIL : InteractionResult.PASS);
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> piloting(player) ? InteractionResult.FAIL : InteractionResult.PASS);
		UseItemCallback.EVENT.register((player, level, hand) -> piloting(player) ? InteractionResult.FAIL : InteractionResult.PASS);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(DIESEL_WALKER_ITEM));
	}

	/** Whether the player is at the controls of a walker (its first rider). */
	public static boolean piloting(Player player) {
		return player.getVehicle() instanceof DieselWalker walker && walker.getFirstPassenger() == player;
	}
}
