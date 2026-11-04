package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.botany.Jugtania;
import io.github.jimbozoomer.jugcraft.agriculture.StrippableLogBlock;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPresentRecipe;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import java.util.*;

public final class AcquisitionRepairGameTests {
    private static ItemStack stack(String name){return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(name)));}
    private static ItemStack craft(GameTestHelper h,CraftingInput in){return h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,in,h.getLevel()).map(r->r.value().assemble(in)).orElse(ItemStack.EMPTY);}
    @GameTest public void presentsPreserveContentsAndRejectExtraInputs(GameTestHelper h){
        var present=stack("supps_present");present.set(DataComponents.CUSTOM_NAME,Component.literal("Gift"));
        var contents=ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,7)));present.set(DataComponents.CONTAINER,contents);
        var dyed=craft(h,CraftingInput.of(2,1,List.of(present,new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:red_dye"))))));
        h.assertTrue(dyed.is(stack("supps_present_red").getItem())&&dyed.getCount()==1&&contents.equals(dyed.get(DataComponents.CONTAINER)),"Dye preserves contents exactly once");
        var trapped=craft(h,CraftingInput.of(2,1,List.of(dyed,new ItemStack(Items.TRIPWIRE_HOOK))));
        h.assertTrue(trapped.is(stack("supps_trapped_present_red").getItem())&&contents.equals(trapped.get(DataComponents.CONTAINER))&&Component.literal("Gift").equals(trapped.get(DataComponents.CUSTOM_NAME)),"Trap retains color, name and contents");
        h.assertTrue(craft(h,CraftingInput.of(3,1,List.of(present,present,new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("minecraft:red_dye")))))).isEmpty(),"Cannot combine two inventories");
        h.assertTrue(present.getCount()==1&&contents.equals(present.get(DataComponents.CONTAINER)),"Preview does not mutate input");h.succeed();
    }
    @GameTest public void botanicalUpgradeAndWaterMatching(GameTestHelper h){
        var armor=stack("jugtania_manasteel_helmet");armor.set(DataComponents.CUSTOM_NAME,Component.literal("Keepsake"));armor.setDamageValue(19);
        var twig=stack("jugtania_livingwood_twig");var ingot=stack("jugtania_terrasteel_ingot");
        var output=craft(h,CraftingInput.of(3,3,List.of(twig,stack("jugtania_rune_spring"),twig,ingot,armor,ingot,ItemStack.EMPTY,ingot,ItemStack.EMPTY)));
        h.assertTrue(output.is(stack("jugtania_terrasteel_helmet").getItem())&&output.getDamageValue()==19&&Component.literal("Keepsake").equals(output.get(DataComponents.CUSTOM_NAME)),"Armor upgrade keeps components");
        for(var potion:List.of(Potions.WATER,Potions.POISON)){
            var bottle=new ItemStack(Items.POTION);bottle.set(DataComponents.POTION_CONTENTS,new PotionContents(potion));
            var result=craft(h,CraftingInput.of(3,3,List.of(ItemStack.EMPTY,ItemStack.EMPTY,bottle,ItemStack.EMPTY,twig,ItemStack.EMPTY,stack("jugtania_rune_water"),ItemStack.EMPTY,ItemStack.EMPTY)));
            h.assertTrue(potion==Potions.WATER?result.is(stack("jugtania_water_rod").getItem()):result.isEmpty(),"Only water bottles match");
        }
        for(String wood:List.of("livingwood","dreamwood"))h.assertTrue(Jugtania.block(wood+"_log") instanceof StrippableLogBlock log&&log.stripped()==Jugtania.block("stripped_"+wood+"_log"),"Native stripping target "+wood);
        for(String part:List.of("log","wood"))h.assertTrue(io.github.jimbozoomer.jugcraft.arcane.ForbiddenArcane.block("aurum_"+part) instanceof StrippableLogBlock log&&log.stripped()==io.github.jimbozoomer.jugcraft.arcane.ForbiddenArcane.block("stripped_aurum_"+part),"Aurum stripping target "+part);
        h.succeed();
    }
    @GameTest public void ashenBasaltRequiresSilkTouch(GameTestHelper h){
        var block=io.github.jimbozoomer.jugcraft.menagerie.OwnerSupplementaries.BLOCKS.get("ashen_basalt");
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);
        var pos=h.absolutePos(new net.minecraft.core.BlockPos(1,1,1));
        var ordinary=net.minecraft.world.level.block.Block.getDrops(block.defaultBlockState(),h.getLevel(),pos,null,null,pick);
        h.assertTrue(ordinary.size()==1&&ordinary.getFirst().is(Items.BASALT),"Normal harvest returns basalt");
        pick.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);
        var silk=net.minecraft.world.level.block.Block.getDrops(block.defaultBlockState(),h.getLevel(),pos,null,null,pick);
        h.assertTrue(silk.size()==1&&silk.getFirst().is(block.asItem()),"Silk Touch retains Ashen Basalt");h.succeed();
    }
    @GameTest public void nativeRecipesAreLoaded(GameTestHelper h){
        for(String name:List.of("divine_pact","candle_holder_cupric","candle_holder_ender","candle_holder_soul","candle_holder_spectacle","sconce_ender","sconce_glow","sconce_green","sconce_nether_brass"))
            h.assertTrue(h.getLevel().recipeAccess().byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,Jugcraft.id("acquisition/"+name))).isPresent(),"Loaded acquisition recipe "+name);
        h.succeed();
    }

    @GameTest public void codexConsolidationAndSoulscribePreserveValue(GameTestHelper h){
        for(String[] pair:List.of(new String[]{"codex_entry","lost_codex_entry"},new String[]{"lost_codex_entry","ancient_codex_entry"})){
            var input=stack("ars_additions_"+pair[0]);
            var result=craft(h,CraftingInput.of(3,1,List.of(input.copy(),input.copy(),input.copy())));
            h.assertTrue(result.is(stack("ars_additions_"+pair[1]).getItem())&&result.getCount()==1,"Three paid codices consolidate once");
            h.assertTrue(craft(h,CraftingInput.of(2,1,List.of(input.copy(),input.copy()))).isEmpty(),"Two codices cannot fund the higher experience grant");
        }
        var dagger=stack("jugtania_ender_dagger");dagger.setDamageValue(17);dagger.set(DataComponents.CUSTOM_NAME,Component.literal("Recovered blade"));
        var input=new SmithingRecipeInput(ItemStack.EMPTY,dagger,new ItemStack(Items.INK_SAC));
        var output=h.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING,input,h.getLevel()).orElseThrow().value().assemble(input);
        h.assertTrue(output.is(stack("jugtania_soulscribe").getItem())&&output.getDamageValue()==17&&dagger.get(DataComponents.CUSTOM_NAME).equals(output.get(DataComponents.CUSTOM_NAME)),"Paid conversion preserves name and wear");
        h.assertTrue(dagger.getDamageValue()==17&&dagger.getCount()==1,"Smithing preview does not mutate the original");h.succeed();
    }

    @GameTest public void loadedArcaneUpgradesRequirePaidBasesAndKeepComponents(GameTestHelper h){
        int checked=0;
        for(var ritual:io.github.jimbozoomer.jugcraft.arcane.ArcaneRituals.all()){
            if(!ritual.data().getAsJsonObject("result").get("type").getAsString().endsWith("transmute_input"))continue;
            var main=io.github.jimbozoomer.jugcraft.nouveau.NouveauRecipes.stack(ritual.data().get("main_ingredient"),h.getLevel().registryAccess());
            h.assertTrue(!main.isEmpty(),"Registered ritual base "+ritual.id());
            main.set(DataComponents.CUSTOM_NAME,Component.literal("Keepsake"));if(main.isDamageableItem())main.setDamageValue(19);
            main.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),1);
            var slots=new ArrayList<ItemStack>();
            for(var ingredient:ritual.inputs())slots.add(io.github.jimbozoomer.jugcraft.nouveau.NouveauRecipes.stack(ingredient,h.getLevel().registryAccess()));
            while(slots.size()<8)slots.add(ItemStack.EMPTY);
            h.assertTrue(ritual.matches(main,slots,ritual.tier(),ritual.essence(),ritual.essence(),List.of()),"Loaded ingredients match "+ritual.id());
            h.assertTrue(!ritual.matches(new ItemStack(Items.DIRT),slots,ritual.tier(),ritual.essence(),ritual.essence(),List.of()),"Wrong base cannot buy equipment");
            var output=ritual.output(h.getLevel().registryAccess(),main);
            h.assertTrue(!output.isEmpty()&&output.getCount()==1&&main.get(DataComponents.CUSTOM_NAME).equals(output.get(DataComponents.CUSTOM_NAME))&&main.get(DataComponents.ENCHANTMENTS).equals(output.get(DataComponents.ENCHANTMENTS)),"Ritual keeps name and enchantments "+ritual.id());
            if(BuiltInRegistries.ITEM.getKey(output.getItem()).getPath().startsWith("forbidden_mortem_"))h.assertTrue(!output.isDamageableItem()&&!output.has(DataComponents.MAX_DAMAGE),"Mortem retains its native indestructible armor behavior");
            else if(output.isDamageableItem())h.assertTrue(output.getDamageValue()==main.getDamageValue(),"Damageable upgrades retain paid wear "+ritual.id());
            h.assertTrue(main.getCount()==1,"Ritual preview leaves the paid base untouched");checked++;
        }
        h.assertTrue(checked==48,"All PR and upstream transmutations, including existing Draco/Tyr armor upgrades, loaded");h.succeed();
    }
}
