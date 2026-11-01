package io.github.jimbozoomer.jugcraft.botany;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

/** Source-shaped upgrades retain equipment data; water recipes reject other potions. */
public final class BotanicalAcquisitionRecipe extends CustomRecipe {
    private final ShapedRecipe shaped;
    private final boolean armor;
    public static final RecipeSerializer<BotanicalAcquisitionRecipe> ARMOR = serializer(true), WATER = serializer(false);
    private static RecipeSerializer<BotanicalAcquisitionRecipe> serializer(boolean armor) {
        return new RecipeSerializer<>(ShapedRecipe.MAP_CODEC.xmap(r -> new BotanicalAcquisitionRecipe(r, armor), r -> r.shaped), ShapedRecipe.STREAM_CODEC.map(r -> new BotanicalAcquisitionRecipe(r, armor), r -> r.shaped));
    }
    public BotanicalAcquisitionRecipe(ShapedRecipe shaped, boolean armor) {this.shaped=shaped;this.armor=armor;}
    @Override public boolean matches(CraftingInput input, Level level) {
        if(!shaped.matches(input,level))return false;
        if(!armor)for(int i=0;i<input.size();i++) {
            var stack=input.getItem(i);
            if(stack.is(Items.POTION)) {
                var potion=stack.get(DataComponents.POTION_CONTENTS);
                if(potion==null||!potion.is(Potions.WATER))return false;
            }
        }
        return true;
    }
    @Override public ItemStack assemble(CraftingInput input) {
        if(!matches(input,null))return ItemStack.EMPTY;
        var result=shaped.assemble(input);
        if(armor)for(int i=0;i<input.size();i++) {
            var source=input.getItem(i);
            if(source.getItem() instanceof BotanicalArmor.Armor)return source.transmuteCopy(result.getItem(),result.getCount());
        }
        return result;
    }
    @Override public java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display(){return shaped.display();}
    @Override public RecipeSerializer<BotanicalAcquisitionRecipe> getSerializer(){return armor?ARMOR:WATER;}
}
