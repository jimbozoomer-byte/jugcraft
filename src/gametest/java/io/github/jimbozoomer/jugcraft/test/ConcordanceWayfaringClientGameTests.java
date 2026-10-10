package io.github.jimbozoomer.jugcraft.test;

import eu.pb4.trinkets.api.TrinketsApi;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.trinket.Wayfaring;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Client game test for Wayfaring, the trinkets slice's part 1 (docs/features/arcane-concordance-trinkets.md), on the real,
 * ticking player, whose equipment Trinkets scans every tick (the server tests' mock players are never scanned):
 * <ul>
 * <li>a worn Leather Belt gives a second Charm slot, on the server and, synced, on the client; a charm goes in it;</li>
 * <li>the worn feather, boot and Ice Breaker give their attributes under their own names, and once;</li>
 * <li>with the second charm taken out first, the belt comes off, the slot goes, and nothing falls to the ground;</li>
 * <li>the eight icons in frames on a wall (three of them the owner's animated strips), and the inventory, for people to
 * look at.</li>
 * </ul>
 * The HUD is hidden whatever state an earlier test left it in, and put back at the end.
 */
public class ConcordanceWayfaringClientGameTests implements FabricClientGameTest {
	private static final String[] ICONS = {"leather_belt", "angelic_feather", "kraken_shell", "infernal_claws", "angelheart_vial",
			"phoenix_down", "amphibian_boot", "ice_breaker"};
	/** What the worn items should give the player, each once: attribute -> modifier id -> amount. */
	private static final Map<String, Map<String, Double>> WORN = Map.of(
			"minecraft:jump_strength", Map.of("jugcraft:wayfaring/angelic_feather/jump_strength", Wayfaring.FEATHER_JUMP),
			"minecraft:water_movement_efficiency", Map.of("jugcraft:wayfaring/amphibian_boot/water_movement_efficiency", Wayfaring.AMPHIBIAN_SWIM),
			"minecraft:oxygen_bonus", Map.of("jugcraft:wayfaring/amphibian_boot/oxygen_bonus", Wayfaring.AMPHIBIAN_OXYGEN),
			"minecraft:knockback_resistance", Map.of("jugcraft:wayfaring/ice_breaker/knockback_resistance", Wayfaring.ICE_BREAKER_KNOCKBACK));

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("gamerule minecraft:advance_time false");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 6, y - 1, z - 14, x + 8, y - 1, z + 4));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 6, y, z - 14, x + 8, y + 6, z + 4));
			context.waitTicks(10);
			boolean hudWasHidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			setHudHidden(context, true);

			// The belt, a feather in the first Charm slot, the boot and the Ice Breaker on the feet: Trinkets' own scan applies them.
			server.runOnServer(minecraft -> {
				var attachment = TrinketsApi.getAttachment(player(minecraft));
				attachment.getInventory(Wayfaring.BELT_SLOT).setItem(0, new ItemStack(Wayfaring.LEATHER_BELT));
				attachment.getInventory(Wayfaring.CHARM_SLOT).setItem(0, new ItemStack(Wayfaring.ANGELIC_FEATHER));
				attachment.getInventory(Wayfaring.FEET_SLOT).setItem(0, new ItemStack(Wayfaring.AMPHIBIAN_BOOT));
				attachment.getInventory(Wayfaring.FEET_SLOT).setItem(1, new ItemStack(Wayfaring.ICE_BREAKER));
			});
			context.waitTicks(10);
			int serverCharms = server.computeOnServer(minecraft -> charms(minecraft));
			context.waitTicks(10);
			int clientCharms = context.computeOnClient(client -> {
				var charms = TrinketsApi.getAttachment(client.player).getInventory(Wayfaring.CHARM_SLOT);
				return charms == null ? -1 : charms.getContainerSize();
			});
			Jugcraft.LOGGER.info("[wayfaring client] Charm slots with the belt: {} on the server, {} on the client", serverCharms, clientCharms);
			check(serverCharms == 2 && clientCharms == 2, "A worn Leather Belt should give two Charm slots, not " + serverCharms
					+ " on the server and " + clientCharms + " on the client");
			// A Phoenix Down in the second slot: a feather as well, so the jump is still given once.
			server.runOnServer(minecraft -> TrinketsApi.getAttachment(player(minecraft)).getInventory(Wayfaring.CHARM_SLOT)
					.setItem(1, new ItemStack(Wayfaring.PHOENIX_DOWN)));
			context.waitTicks(10);
			String given = server.computeOnServer(minecraft -> given(minecraft));
			Jugcraft.LOGGER.info("[wayfaring client] attributes worn: {}", given.isEmpty() ? "as designed" : given);
			check(given.isEmpty(), "The worn items' attributes are not as designed: " + given);

			// Off in order: the second charm, then the belt. The slot goes and nothing is dropped.
			server.runOnServer(minecraft -> TrinketsApi.getAttachment(player(minecraft)).getInventory(Wayfaring.CHARM_SLOT)
					.setItem(1, ItemStack.EMPTY));
			context.waitTicks(5);
			server.runOnServer(minecraft -> TrinketsApi.getAttachment(player(minecraft)).getInventory(Wayfaring.BELT_SLOT)
					.setItem(0, ItemStack.EMPTY));
			context.waitTicks(10);
			int after = server.computeOnServer(minecraft -> charms(minecraft));
			int dropped = server.computeOnServer(minecraft -> minecraft.overworld().getEntitiesOfClass(ItemEntity.class,
					new AABB(x - 6, y - 2, z - 6, x + 6, y + 4, z + 6)).size());
			check(after == 1 && dropped == 0, "Without the belt there should be one Charm slot and nothing dropped, not " + after
					+ " slots and " + dropped + " dropped");

			context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_wayfaring_inventory");
			context.setScreen(() -> null);
			server.runOnServer(minecraft -> {
				var attachment = TrinketsApi.getAttachment(player(minecraft));
				attachment.getInventory(Wayfaring.CHARM_SLOT).setItem(0, ItemStack.EMPTY);
				attachment.getInventory(Wayfaring.FEET_SLOT).setItem(0, ItemStack.EMPTY);
				attachment.getInventory(Wayfaring.FEET_SLOT).setItem(1, ItemStack.EMPTY);
			});

			// The eight icons in frames on a wall.
			server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(x - 2, y, z - 12, x + 7, y + 3, z - 12));
			for (int i = 0; i < ICONS.length; i++) {
				frame(server, x - 1 + i, y + 1, z - 11, ICONS[i]);
			}
			context.waitTicks(20);
			int frames = server.computeOnServer(minecraft -> minecraft.overworld().getEntitiesOfClass(Entity.class,
					new AABB(x - 2, y, z - 12, x + 8, y + 4, z - 10), entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())
							.getPath().equals("item_frame")).size());
			check(frames == ICONS.length, frames + " item frames on the wall, not " + ICONS.length);
			shoot(context, singleplayer, x + 3.0, y, z - 7.5, 0, "jugcraft_wayfaring_icons");
			setHudHidden(context, hudWasHidden);
		}
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().getFirst();
	}

	private static int charms(MinecraftServer minecraft) {
		var charms = TrinketsApi.getAttachment(player(minecraft)).getInventory(Wayfaring.CHARM_SLOT);
		return charms == null ? -1 : charms.getContainerSize();
	}

	/** What differs from WORN in the player's attributes (each expected modifier present once, with its amount), or "". */
	private static String given(MinecraftServer minecraft) {
		ServerPlayer player = player(minecraft);
		List<String> wrong = new ArrayList<>();
		WORN.forEach((attribute, modifiers) -> {
			var holder = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attribute));
			AttributeInstance instance = holder.isPresent() ? player.getAttribute(holder.get()) : null;
			if (instance == null) {
				wrong.add(attribute + " missing");
				return;
			}
			long ours = instance.getModifiers().stream().filter(modifier -> modifier.id().getPath().startsWith("wayfaring/")).count();
			if (ours != modifiers.size()) {
				wrong.add(attribute + " has " + ours + " Wayfaring modifiers");
			}
			modifiers.forEach((id, amount) -> {
				var modifier = instance.getModifier(Identifier.parse(id));
				if (modifier == null || modifier.amount() != amount) {
					wrong.add(attribute + " " + id + " is " + (modifier == null ? "absent" : modifier.amount()));
				}
			});
		});
		return String.join("; ", wrong);
	}

	private static void frame(TestServerContext server, int x, int y, int z, String item) {
		server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}".formatted(x, y, z, item));
	}

	/** Stands the camera at (x, y, z) looking north and {@code pitch} down, waits for the world to draw, and takes the shot. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, double x, int y, double z, int pitch,
			String name) {
		singleplayer.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %d %.2f 180 %d", x, y, z, pitch));
		context.waitTicks(40);
		singleplayer.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}

	private static void setHudHidden(ClientGameTestContext context, boolean hidden) {
		context.runOnClient(client -> {
			if (client.gui.hud.isHidden() != hidden) {
				client.gui.hud.toggle();
			}
		});
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
