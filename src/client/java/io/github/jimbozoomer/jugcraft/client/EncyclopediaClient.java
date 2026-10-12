package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The two ways into the Jugcraft Encyclopedia (EncyclopediaScreen), neither needing an item: the Encyclopedia key (K by
 * default, remappable in Controls under Jugcraft) from the game, and a book button on the survival inventory, beside
 * the recipe book's, which closes back to the inventory. The creative inventory has no button; the key works there
 * once the inventory is closed.
 *
 * <p>The button sits right of the recipe book's toggle (the inventory's 20x18 button) and follows it, so it moves with
 * the panel when the recipe book opens; no mixin is needed. Without a toggle it takes the toggle's vanilla place.
 */
public final class EncyclopediaClient {
	/** The recipe book toggle's size, and the gap between it and the book button. */
	private static final int TOGGLE_WIDTH = 20;
	private static final int TOGGLE_HEIGHT = 18;
	private static final int GAP = 4;
	/** Vanilla's inventory panel and its toggle's place on it, for when no toggle is found. */
	private static final int PANEL_WIDTH = 176;
	private static final int PANEL_HEIGHT = 166;
	private static final int TOGGLE_X = 104;
	private static final int TOGGLE_Y = 61;
	private static KeyMapping open;

	private EncyclopediaClient() {
	}

	static void register() {
		open = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.encyclopedia", InputConstants.KEY_K, PartyClient.CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (open.consumeClick()) {
				if (client.gui.screen() == null && client.player != null) {
					client.gui.setScreen(new EncyclopediaScreen(null));
				}
			}
		});
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen inventory) {
				addButton(client, inventory);
			}
		});
	}

	/** The key that opens the Encyclopedia (K unless remapped); tests press it as a player would. */
	public static KeyMapping key() {
		return open;
	}

	private static void addButton(Minecraft client, InventoryScreen inventory) {
		List<AbstractWidget> widgets = Screens.getWidgets(inventory);
		AbstractWidget toggle = null;
		for (AbstractWidget widget : widgets) {
			if (widget.getWidth() == TOGGLE_WIDTH && widget.getHeight() == TOGGLE_HEIGHT) {
				toggle = widget;
				break;
			}
		}
		int x = toggle != null ? toggle.getX() + toggle.getWidth() + GAP
				: (inventory.width - PANEL_WIDTH) / 2 + TOGGLE_X + TOGGLE_WIDTH + GAP;
		int y = toggle != null ? toggle.getY() : (inventory.height - PANEL_HEIGHT) / 2 + TOGGLE_Y;
		BookButton button = new BookButton(x, y, pressed -> client.gui.setScreen(new EncyclopediaScreen(inventory)));
		widgets.add(button);
		if (toggle != null) {
			// The recipe book moves the panel and its toggle when it opens or closes; the button follows the toggle.
			AbstractWidget anchor = toggle;
			ScreenEvents.beforeExtract(inventory).register((screen, graphics, mouseX, mouseY, delta) -> {
				button.setX(anchor.getX() + anchor.getWidth() + GAP);
				button.setY(anchor.getY());
			});
		}
	}

	/** A recipe-book-sized button showing a book; its message names it for the narrator. */
	private static final class BookButton extends Button {
		private static final int EDGE = 0xFF373737;
		private static final int FACE = 0xFF8B8B8B;
		private static final int FACE_LIT = 0xFFB4B4B4;
		private static final int LIGHT = 0xFFFFFFFF;
		private static final ItemStack ICON = new ItemStack(Items.BOOK);

		BookButton(int x, int y, OnPress onPress) {
			super(x, y, 20, 18, Component.translatable("screen.jugcraft.encyclopedia.open"), onPress, Button.DEFAULT_NARRATION);
			setTooltip(Tooltip.create(Component.translatable("screen.jugcraft.encyclopedia.open.tooltip")));
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			int x = getX();
			int y = getY();
			graphics.fill(x, y, x + width, y + height, EDGE);
			graphics.fill(x, y, x + width - 1, y + height - 1, LIGHT);
			graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, isHovered() || isFocused() ? FACE_LIT : FACE);
			graphics.item(ICON, x + 2, y + 1);
		}
	}
}
