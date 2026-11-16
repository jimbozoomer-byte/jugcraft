package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Shared local display settings, built with Cloth Config. Opened from Mod Menu when installed, or with
 * the (unbound by default) "Jugcraft display settings" key in Controls.
 */
public final class ConcordanceSettingsScreen {
	private ConcordanceSettingsScreen() {
	}

	public static Screen create(@Nullable Screen parent) {
		boolean[] values = {ConcordanceClientOptions.hud(), ConcordanceClientOptions.reducedMotion(), ConcordanceClientOptions.exactValues(),
				ConcordanceClientOptions.simpleJournal(), ConcordanceClientOptions.wornTrinkets()};
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
		display.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.concordance.config.exact"), values[2])
				.setDefaultValue(false)
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.exact.tooltip"))
				.setSaveConsumer(value -> values[2] = value)
				.build());
		display.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.concordance.config.simple_journal"), values[3])
				.setDefaultValue(false)
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.simple_journal.tooltip"))
				.setSaveConsumer(value -> values[3] = value)
				.build());
		display.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.concordance.config.worn_trinkets"), values[4])
				.setDefaultValue(true)
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.worn_trinkets.tooltip"))
				.setSaveConsumer(value -> values[4] = value)
				.build());
		Presentation.Intensity[] intensity = {ConcordanceClientOptions.intensity()};
		display.addEntry(entries.startSelector(Component.translatable("screen.jugcraft.concordance.config.intensity"),
						Presentation.Intensity.values(), intensity[0])
				.setDefaultValue(Presentation.Intensity.FULL)
				.setNameProvider(value -> Component.translatable("screen.jugcraft.concordance.config.intensity." + value.id()))
				.setTooltip(Component.translatable("screen.jugcraft.concordance.config.intensity.tooltip"))
				.setSaveConsumer(value -> intensity[0] = value)
				.build());
		var drug = ConcordanceClientOptions.drugVisuals();
		boolean[] enabled = {drug.enabled()};
		int[] levels = {drug.intensity(), drug.blur(), drug.motion(), drug.doubleVision()};
		int[] defaults = {75, 70, 60, 70};
		ConfigCategory visuals = builder.getOrCreateCategory(Component.translatable("screen.jugcraft.drug_visuals.title"));
		visuals.addEntry(entries.startBooleanToggle(Component.translatable("screen.jugcraft.drug_visuals.enabled"), enabled[0])
				.setDefaultValue(true).setTooltip(Component.translatable("screen.jugcraft.drug_visuals.enabled.tooltip"))
				.setSaveConsumer(value -> enabled[0] = value).build());
		String[] names = {"intensity", "blur", "motion", "double_vision"};
		for (int i = 0; i < names.length; i++) {
			final int index = i;
			visuals.addEntry(entries.startIntSlider(Component.translatable("screen.jugcraft.drug_visuals." + names[i]), levels[i], 0, 100)
					.setDefaultValue(defaults[i]).setTooltip(Component.translatable("screen.jugcraft.drug_visuals." + names[i] + ".tooltip"))
					.setSaveConsumer(value -> levels[index] = value).build());
		}
		builder.setSavingRunnable(() -> {
			ConcordanceClientOptions.setDrugVisuals(new DrugVisualOptions(enabled[0], levels[0], levels[1], levels[2], levels[3]));
			ConcordanceClientOptions.set(values[0], values[1], values[2], values[3], values[4], intensity[0]);
		});
		return builder.build();
	}
}
