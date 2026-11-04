package io.github.jimbozoomer.jugcraft.tools;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Small persistent settings carried by the item, including through livery smithing. */
public final class EquipmentSettings {
    private EquipmentSettings() {}
    public static int number(ItemStack stack, String key, int fallback) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("jugcraft_" + key, fallback);
    }
    public static void setNumber(ItemStack stack, String key, int value) {
        if (number(stack, key, Integer.MIN_VALUE) != value)
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("jugcraft_" + key, value));
    }
    public static boolean enabled(ItemStack stack, String key) { return number(stack, key, 1) != 0; }
    public static boolean toggle(ItemStack stack, String key) {
        boolean enabled = !enabled(stack, key); setNumber(stack, key, enabled ? 1 : 0); return enabled;
    }
}
