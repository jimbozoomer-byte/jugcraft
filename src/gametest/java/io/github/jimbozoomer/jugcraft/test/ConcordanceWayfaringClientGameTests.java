package io.github.jimbozoomer.jugcraft.test;

import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import eu.pb4.trinkets.api.client.renderer.element.TrinketRenderElement;
import eu.pb4.trinkets.impl.client.render.ClientTrinketsManager;
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
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Client game test for Wayfaring, the trinkets slice's part 1 (docs/features/arcane-concordance-trinkets.md), on the real,
 * ticking player, whose equipment Trinkets scans every tick (the server tests' mock players are never scanned):
 * <ul>
 * <li>a worn Leather Belt gives a second Charm slot, on the server and, synced, on the client; a charm goes in it;</li>
 * <li>the worn feather, boot and Ice Breaker give their attributes under their own names, and once;</li>
 * <li>part 1b: Trinkets has what it needs to draw the Leather Belt and the Amphibian Boot on the body (no Java renderer in
 * the way; each render definition loaded, with as many elements as parts it draws on, none of them empty (which parts:
 * tools/check_mod_data.py check_wayfaring_worn); the worn models among the client's resources; the owner's worn sheets in
 * the items atlas; nothing for the Ice Breaker, which has no worn sheet). Whether they look right only the shots show:
 * the player wearing them from the front and from behind, whole, closer and from each quarter, over iron leggings and
 * boots, over a chestplate, and sneaking, for people to look at;</li>
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

			// Part 1b: the belt and boot drawn on the player, who wears both (and the Ice Breaker, which is not drawn).
			String undrawn = context.computeOnClient(client -> undrawn(client));
			Jugcraft.LOGGER.info("[wayfaring client] worn models: {}", undrawn.isEmpty()
					? "definitions resolved to non-empty elements, models present, sheets stitched" : undrawn);
			check(undrawn.isEmpty(), "Trinkets cannot draw the belt and boot on the body: " + undrawn);
			wornShots(context, singleplayer, x, y, z);

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

	/**
	 * What keeps Trinkets from drawing the belt and boot on the body, or "": a Java renderer for either (Trinkets would use
	 * it instead of the data); a render definition not loaded or not matched to its item, or with a different number of
	 * elements than the parts it draws on (the belt one, the boot two; which part each element is on cannot be seen here,
	 * so check_wayfaring_worn holds it), or with an element that baked no quads (Trinkets' no-op); a worn model missing
	 * from the client's resources (the game would bake its missing-model cube instead, which this cannot tell apart: only
	 * the shots show the models are the owner's); a worn sheet missing from the items atlas; or a definition for the Ice
	 * Breaker, which has no worn sheet.
	 */
	private static String undrawn(Minecraft client) {
		List<String> wrong = new ArrayList<>();
		var atlas = client.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
		Map<Item, List<String>> models = Map.of(Wayfaring.LEATHER_BELT, List.of("leather_belt_worn"),
				Wayfaring.AMPHIBIAN_BOOT, List.of("amphibian_boot_worn_right", "amphibian_boot_worn_left"));
		models.forEach((item, names) -> {
			String id = BuiltInRegistries.ITEM.getKey(item).getPath();
			if (TrinketRendererRegistry.hasRenderer(item)) {
				wrong.add(id + " has a Java renderer, which Trinkets uses instead of its render definition");
			}
			List<TrinketRenderElement.Baked> baked = ClientTrinketsManager.INSTANCE.getResolved(new ItemStack(item));
			if (baked.size() != names.size() || baked.contains(TrinketRenderElement.Baked.NO_OP)) {
				wrong.add(id + " resolves to " + baked.size() + " drawn elements, not " + names.size()
						+ (baked.contains(TrinketRenderElement.Baked.NO_OP) ? ", one of them baked from a model with no quads" : ""));
			}
			for (String name : names) {
				if (client.getResourceManager().getResource(Jugcraft.id("models/item/" + name + ".json")).isEmpty()) {
					wrong.add("the worn model " + name + " is missing from the client's resources");
				}
			}
			Identifier sheet = Jugcraft.id("item/" + id + "_worn");
			if (atlas.getSprite(sheet) == atlas.missingSprite()) {
				wrong.add(sheet + " is not in the items atlas");
			}
		});
		if (!ClientTrinketsManager.INSTANCE.getResolved(new ItemStack(Wayfaring.ICE_BREAKER)).isEmpty()) {
			wrong.add("the Ice Breaker has a render definition, but no worn sheet to draw");
		}
		return String.join("; ", wrong);
	}

	/**
	 * The player wearing the belt and the boot, from the front and from behind: whole (field of view 70, looking level), then
	 * closer (50, the camera above, looking down 20 degrees, so the feet stay in the picture), then from each quarter (the
	 * view turned 45 degrees, which shows the boots' sides and fins), then over iron leggings and boots (the belt and boot
	 * stand clear of both and should show unbroken over them) and over an iron chestplate too (which hides the strap; the
	 * buckle stands out through it), then sneaking from behind. Standing still, a player's body turns only once the head is
	 * more than 50 degrees past it, and a teleport turns only the head, so the body is set facing north on the client; the
	 * later turns, 45 degrees at most, are the view's alone. The creative guide every creative player is given would be in
	 * the hand, in front of the belt, so the hand is emptied first. The camera is put back to first person at 70 after.
	 */
	private static void wornShots(ClientGameTestContext context, TestSingleplayerContext singleplayer, int x, int y, int z) {
		TestServerContext server = singleplayer.getServer();
		server.runCommand(String.format(Locale.ROOT, "tp @p %.2f %d %.2f 180 0", x + 0.5, y, z - 2.5));
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
		context.waitTicks(10);
		context.runOnClient(client -> {
			client.player.setYBodyRot(180.0F);
			client.player.yBodyRotO = 180.0F;
		});
		context.waitTicks(5);
		singleplayer.getConnection().waitForChunksRender();
		look(context, server, CameraType.THIRD_PERSON_FRONT, 70, 180, 0, "jugcraft_wayfaring_worn_front");
		look(context, server, CameraType.THIRD_PERSON_BACK, 70, 180, 0, "jugcraft_wayfaring_worn_back");
		// The front camera sits opposite the look: looking up 20 degrees puts it above, looking down at the player.
		look(context, server, CameraType.THIRD_PERSON_FRONT, 50, 180, -20, "jugcraft_wayfaring_worn_front_close");
		look(context, server, CameraType.THIRD_PERSON_BACK, 50, 180, 20, "jugcraft_wayfaring_worn_back_close");
		// Facing north, a view turned to 135 (north-west) puts the front camera at the wearer's left and the back camera at
		// their right; 225 (north-east) the other way round.
		look(context, server, CameraType.THIRD_PERSON_FRONT, 50, 135, -20, "jugcraft_wayfaring_worn_front_left");
		look(context, server, CameraType.THIRD_PERSON_FRONT, 50, 225, -20, "jugcraft_wayfaring_worn_front_right");
		look(context, server, CameraType.THIRD_PERSON_BACK, 50, 135, 20, "jugcraft_wayfaring_worn_back_right");
		look(context, server, CameraType.THIRD_PERSON_BACK, 50, 225, 20, "jugcraft_wayfaring_worn_back_left");
		server.runCommand("item replace entity @p armor.legs with minecraft:iron_leggings");
		server.runCommand("item replace entity @p armor.feet with minecraft:iron_boots");
		look(context, server, CameraType.THIRD_PERSON_FRONT, 50, 180, -20, "jugcraft_wayfaring_worn_over_armour");
		server.runCommand("item replace entity @p armor.chest with minecraft:iron_chestplate");
		look(context, server, CameraType.THIRD_PERSON_FRONT, 50, 180, -20, "jugcraft_wayfaring_worn_over_chestplate");
		server.runCommand("item replace entity @p armor.chest with minecraft:air");
		look(context, server, CameraType.THIRD_PERSON_BACK, 50, 180, 20, "jugcraft_wayfaring_worn_over_armour_back");
		server.runCommand("item replace entity @p armor.legs with minecraft:air");
		server.runCommand("item replace entity @p armor.feet with minecraft:air");
		context.getInput().holdKey(options -> options.keyShift);
		context.waitTicks(15);
		boolean sneaking = context.computeOnClient(client -> client.player.isShiftKeyDown());
		context.takeScreenshot("jugcraft_wayfaring_worn_sneaking_back");
		context.getInput().releaseKey(options -> options.keyShift);
		context.runOnClient(client -> {
			client.options.setCameraType(CameraType.FIRST_PERSON);
			client.options.fov().set(70);
		});
		check(sneaking, "The player was not sneaking for the sneaking shot");
	}

	/** Turns the player's view (not their place, so their body stays as it is) and takes a shot with that camera. */
	private static void look(ClientGameTestContext context, TestServerContext server, CameraType camera, int fov, int yaw, int pitch,
			String name) {
		server.runCommand("execute as @p at @s run tp @s ~ ~ ~ " + yaw + " " + pitch);
		context.runOnClient(client -> {
			client.options.setCameraType(camera);
			client.options.fov().set(fov);
		});
		context.waitTicks(10);
		context.takeScreenshot(name);
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
