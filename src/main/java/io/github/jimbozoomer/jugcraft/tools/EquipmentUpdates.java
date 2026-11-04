package io.github.jimbozoomer.jugcraft.tools;

import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialBackpacks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Runtime resource updates are not armor replacement. Never change inventory/equipment synchronization. */
public final class EquipmentUpdates {
    private EquipmentUpdates() {}
    public static boolean sameEquipment(ItemStack before, ItemStack after) {
        if (ItemStack.isSameItemSameComponents(before, after)) return true;
        if (before.isEmpty() || after.isEmpty() || !ItemStack.isSameItem(before, after)
                || !(after.getItem() instanceof Chargeable || after.getItem() instanceof Jetpack
                    || after.getItem() instanceof IndustrialBackpacks.FoamPack)) return false;
        ItemStack old = before.copy(), now = after.copy();
        resources(old); resources(now);
        return ItemStack.isSameItemSameComponents(old, now);
    }
    private static void resources(ItemStack stack) {
        stack.remove(JugcraftTools.ENERGY);
        if (stack.getItem() instanceof Jetpack) stack.remove(JugcraftFluids.STORED_FLUID);
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove("jugcraft_shield"); tag.remove("jugcraft_fuel_warning");
        if (stack.getItem() instanceof IndustrialBackpacks.FoamPack) tag.remove("JimtaFoam");
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
