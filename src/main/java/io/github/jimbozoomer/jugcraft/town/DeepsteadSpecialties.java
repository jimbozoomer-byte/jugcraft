package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** The Stonewright's and Kettle's native products. Their only sale catalogs live in town/shops.json. */
final class DeepsteadSpecialties {
    private DeepsteadSpecialties() {}

    static void register() {
        List<Block> furnishings = new ArrayList<>();
        for (var style : GuildFurnishingBlock.Style.values()) {
            String id = style.name().toLowerCase(Locale.ROOT);
            var properties = BlockBehaviour.Properties.ofFullCopy(style.material).strength(1.5F);
            if (!style.solid) properties.noOcclusion();
            if (style == GuildFurnishingBlock.Style.COPPER_SAMOVAR)
                properties.lightLevel(s -> s.getValue(GuildFurnishingBlock.SETTING) > 0 ? 5 : 0);
            furnishings.add(DeepsteadWares.furnishing(id, properties, p -> new GuildFurnishingBlock(p, style)));
        }
        List<Item> instruments = new ArrayList<>();
        for (var instrument : QuarryInstrument.Kind.values()) {
            String id = instrument.name().toLowerCase(Locale.ROOT);
            instruments.add(DeepsteadWares.tool(id, p -> new QuarryInstrument(p, instrument)));
        }
        List<Item> meals = List.of(
            meal("ember_onion_pasty", 6, .55F, null), meal("saltcap_skewers", 4, .5F, null),
            meal("golden_leek_tart", 7, .5F, null), meal("emberpepper_hotpot", 9, .7F, Items.BOWL),
            meal("caveberry_shrub", 2, .1F, Items.GLASS_BOTTLE), meal("glowroot_steep", 1, .1F, Items.GLASS_BOTTLE)
        );
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> furnishings.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> instruments.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(tab -> meals.forEach(tab::accept));
    }

    private static Item meal(String name, int nutrition, float saturation, Item vessel) {
        var key = ResourceKey.create(Registries.ITEM, Jugcraft.id(name));
        var food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        var properties = DeepsteadWares.properties(name, key);
        if (vessel == Items.GLASS_BOTTLE) food.alwaysEdible();
        properties.food(food.build());
        if (vessel != null) properties.stacksTo(16).usingConvertsTo(vessel);
        if (vessel == Items.GLASS_BOTTLE) {
            var drink = Consumables.defaultDrink().consumeSeconds(1.6F).hasConsumeParticles(false);
            if (name.equals("caveberry_shrub"))
                drink.onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(MobEffects.NAUSEA)));
            else drink.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 900)));
            properties.component(DataComponents.CONSUMABLE, drink.build());
        }
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
    }
}
