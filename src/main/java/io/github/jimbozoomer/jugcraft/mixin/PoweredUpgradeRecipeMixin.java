package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.jimbozoomer.jugcraft.prospecting.OreScannerItem;
import io.github.jimbozoomer.jugcraft.tools.MiningDrillItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve the real tool in the three existing native shapeless tier-upgrade recipes. */
@Mixin(ShapelessRecipe.class)
public abstract class PoweredUpgradeRecipeMixin {
    @ModifyReturnValue(method="assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",at=@At("RETURN"))
    private ItemStack jugcraft$preservePoweredTool(ItemStack result,@Local(argsOnly=true) CraftingInput input) {
        if(result.isEmpty())return result;
        var id=BuiltInRegistries.ITEM.getKey(result.getItem());
        if(!id.getNamespace().equals("jugcraft") || !java.util.Set.of("jimta_advanced_ore_scanner","jimta_diamond_drill","jimta_iridium_drill").contains(id.getPath()))return result;
        for(int i=0;i<input.size();i++){
            ItemStack source=input.getItem(i);
            if(result.getItem() instanceof OreScannerItem && source.getItem() instanceof OreScannerItem
                || result.getItem() instanceof MiningDrillItem && source.getItem() instanceof MiningDrillItem)
                return source.transmuteCopy(result.getItem(),result.getCount());
        }
        return result;
    }
}
