package io.github.jimbozoomer.jugcraft.menagerie;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Exactly one present plus dye or tripwire hook; retain contents, lock and custom name. */
public final class OwnerPresentRecipe extends CustomRecipe {
    private static final OwnerPresentRecipe INSTANCE=new OwnerPresentRecipe();
    public static final RecipeSerializer<OwnerPresentRecipe> SERIALIZER=new RecipeSerializer<>(MapCodec.unit(INSTANCE),StreamCodec.<RegistryFriendlyByteBuf,OwnerPresentRecipe>unit(INSTANCE));
    @Override public boolean matches(CraftingInput input,Level level){return !assemble(input).isEmpty();}
    @Override public ItemStack assemble(CraftingInput input){
        ItemStack present=ItemStack.EMPTY,material=ItemStack.EMPTY;String name="";
        for(int i=0;i<input.size();i++){
            var stack=input.getItem(i);if(stack.isEmpty())continue;
            String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if(id.startsWith("jugcraft:supps_present")||id.startsWith("jugcraft:supps_trapped_present")){
                if(!present.isEmpty())return ItemStack.EMPTY;
                present=stack;name=id.substring("jugcraft:supps_".length());
            }else if(material.isEmpty()&&(stack.getItem() instanceof DyeItem||stack.is(Items.TRIPWIRE_HOOK)))material=stack;
            else return ItemStack.EMPTY;
        }
        if(present.isEmpty()||material.isEmpty())return ItemStack.EMPTY;
        String target;
        if(material.is(Items.TRIPWIRE_HOOK)){
            if(name.startsWith("trapped_"))return ItemStack.EMPTY;
            target="trapped_"+name;
        }else target=(name.startsWith("trapped_")?"trapped_present_":"present_")+material.getOrDefault(DataComponents.DYE,DyeColor.WHITE).getName();
        var block=OwnerSupplementaries.BLOCKS.get(target);
        return block==null||block.asItem()==present.getItem()?ItemStack.EMPTY:present.transmuteCopy(block.asItem(),1);
    }
    @Override public RecipeSerializer<OwnerPresentRecipe> getSerializer(){return SERIALIZER;}
}
