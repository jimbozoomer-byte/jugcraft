package io.github.jimbozoomer.jugcraft.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The Concordance's client settings screen, built with Cloth Config. Opened from Mod Menu when it is installed, or with
 * the (unbound by default) "Concordance settings" key in Controls.
 */
public final class ConcordanceSettingsScreen {
	private ConcordanceSettingsScreen() {
	}

	public static Screen create(@Nullable Screen parent) {
		boolean[] values = {ConcordanceClientOptions.hud(), ConcordanceClientOptions.reducedMotion()};
		ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
				.setTitle(Component.translatable("screen.jugcraft.concordance.config.title"));
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory display = builder.getOrCreateCategory(Component.translatable("screen.jugcraft.concordance.config.title"));
		display.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.concordance.config.hud"), values[0])
				.setDefaultValue(true)
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.hud.tooltip"))
				.setSaveConsumer(value -> values[0] = value)
				.build());
		display.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.concordance.config.reduced_motion"), values[1])
				.setDefaultValue(false)
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.reduced_motion.tooltip"))
				.setSaveConsumer(value -> values[1] = value)
				.build());
		builder.setSavingRunnable(() -> ConcordanceClientOptions.set(values[0], values[1]));
		return builder.build();
	}
}
