package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceSpells;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.rules.FocusPool;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Arcane Concordance on the client: the Lampwright's Bench screen, the Focus line shown while an instrument is in
 * the main hand, specimen and lantern tooltips, and the settings key. Everything here reads state the server sent
 * (the player's own Focus attachment, item components, menu data); nothing here decides an outcome.
 */
final class ConcordanceClient {
	private static final int FOCUS_COLOR = 0xFFB89AE6;
	private static final int LOW_COLOR = 0xFF8A8A8A;
	private static KeyMapping openSettings;

	private ConcordanceClient() {
	}

	static void register() {
		ConcordanceClientOptions.load();
		MenuScreens.register(JugcraftConcordance.BENCH_MENU, LampwrightBenchScreen::new);
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Jugcraft.id("concordance_focus"), ConcordanceClient::focusLine);
		ItemTooltipCallback.EVENT.register(ConcordanceClient::tooltip);
		openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.concordance_config",
				InputConstants.UNKNOWN.getValue(), PartyClient.CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openSettings.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(ConcordanceSettingsScreen.create(null));
				}
			}
		});
	}

	/** "Focus 12/20" at the bottom right while a Concordance instrument is in the main hand. */
	private static void focusLine(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		Player player = client.player;
		if (!ConcordanceClientOptions.hud() || player == null || player.isSpectator() || !ConcordanceSpells.holdsInstrument(player)) {
			return;
		}
		int focus = ConcordanceProgress.currentFocus(player);
		Font font = client.font;
		String text = Component.translatable("screen.jugcraft.concordance.focus.hud", focus, FocusPool.MAX).getString();
		graphics.text(font, text, graphics.guiWidth() - font.width(text) - 4, graphics.guiHeight() - 12,
				focus > 0 ? FOCUS_COLOR : LOW_COLOR);
	}

	private static void tooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag, List<Component> lines) {
		if (stack.is(JugcraftConcordance.SPECIMENS)) {
			lines.add(Component.translatable("tooltip.jugcraft.concordance.specimen").withStyle(ChatFormatting.DARK_AQUA));
		}
		if (stack.is(JugcraftConcordance.LUMEN_SCONCE.asItem()) && stack.has(JugcraftConcordance.RADIANCE)) {
			// A sconce item keeps the Radiance it held when it was broken (unlit, so none burns).
			lines.add(Component.translatable("tooltip.jugcraft.concordance.lantern.charge",
					KindledLanternItem.charge(stack).stored(), KindledLanternItem.CAPACITY).withStyle(ChatFormatting.GOLD));
		}
		if (stack.is(JugcraftConcordance.KINDLED_LANTERN)) {
			Level level = Minecraft.getInstance().level;
			long now = level == null ? 0L : ConcordanceProgress.now(level);
			lines.add(Component.translatable("tooltip.jugcraft.concordance.lantern.charge", KindledLanternItem.remaining(stack, now),
					KindledLanternItem.CAPACITY).withStyle(ChatFormatting.GOLD));
			lines.add(Component.translatable(KindledLanternItem.lit(stack) ? "tooltip.jugcraft.concordance.lantern.lit"
					: "tooltip.jugcraft.concordance.lantern.unlit").withStyle(ChatFormatting.GRAY));
		}
	}
}
