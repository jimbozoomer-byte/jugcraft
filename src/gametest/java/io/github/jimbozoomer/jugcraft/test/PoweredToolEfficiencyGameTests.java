package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.tools.*;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Pricing contracts plus actual survival breaks, acquisition and persistent component upgrades. */
public final class PoweredToolEfficiencyGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        h.setBlock(new BlockPos(1,1,1),Blocks.STONE);var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        p.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));return p;
    }
    private static ItemStack tool(Item item,long energy,int overclock,int efficiency) {
        var stack=new ItemStack(item);stack.set(JugcraftTools.OVERCLOCK,overclock);stack.set(JugcraftTools.EFFICIENCY,efficiency);Chargeable.setEnergy(stack,energy);return stack;
    }
    private static Item item(String path) { return BuiltInRegistries.ITEM.getValue(Jugcraft.id(path)); }
    @GameTest public void allOverclockEfficiencyPricesAreRoundedPerBlock(GameTestHelper h) {
        long[][] drill={{60,51,42},{90,77,63},{120,102,84}},saw={{40,34,28},{60,51,42},{80,68,56}},leaves={{10,9,7},{15,13,11},{20,17,14}};
        for(int o=0;o<3;o++)for(int e=0;e<3;e++) {
            var d=tool(JugcraftTools.MINING_DRILL,10000,o,e);var s=tool(JugcraftTools.CHAINSAW,10000,o,e);
            h.assertTrue(((PoweredToolItem)d.getItem()).energyPerBlock(d)==drill[o][e],"Drill price differs from agreed table");
            h.assertTrue(((PoweredToolItem)s.getItem()).energyPerBlock(s)==saw[o][e],"Chainsaw price differs from agreed table");
            EquipmentSettings.setNumber(s,"saw_mode",2);h.assertTrue(((PoweredToolItem)s.getItem()).energyPerBlock(s)==leaves[o][e],"Leaf price was discounted twice or rounded down");
            var plain=tool(JugcraftTools.MINING_DRILL,10000,o,0);
            h.assertTrue(d.getDestroySpeed(Blocks.STONE.defaultBlockState())==plain.getDestroySpeed(Blocks.STONE.defaultBlockState()),"Efficiency changed mining speed");
        }h.succeed();
    }
    @GameTest public void fittingLimitsRemovalAndSerializationPreserveCharge(GameTestHelper h) {
        var stack=tool(JugcraftTools.MINING_DRILL,1234,0,0);stack.set(DataComponents.CUSTOM_NAME,Component.literal("Efficient drill"));
        for(int i=0;i<2;i++)h.assertTrue(ToolUpgrades.fit(h.getLevel(),stack,ToolUpgrades.Kind.EFFICIENCY)==ToolUpgrades.Result.FITTED,"Efficiency did not fit");
        h.assertTrue(ToolUpgrades.fit(h.getLevel(),stack,ToolUpgrades.Kind.EFFICIENCY)==ToolUpgrades.Result.FULL,"Third module fitted");
        var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());out.store("tool",ItemStack.CODEC,stack);
        var copy=TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()).read("tool",ItemStack.CODEC).orElseThrow();
        h.assertTrue(ToolUpgrades.level(copy,ToolUpgrades.Kind.EFFICIENCY)==2&&copy.getHoverName().getString().equals("Efficient drill")&&Chargeable.energy(copy)==1234&&Chargeable.capacity(copy)==250000,"Save changed modules, name, charge or capacity");
        h.assertTrue(ToolUpgrades.remove(h.getLevel(),copy,ToolUpgrades.Kind.EFFICIENCY)==ToolUpgrades.Result.REMOVED&&((PoweredToolItem)copy.getItem()).energyPerBlock(copy)==51,"Removing one module did not restore the right price");
        h.assertTrue(Chargeable.energy(copy)==1234,"Module changes consumed or created JE");h.succeed();
    }
    @GameTest public void eligibilityMalformedLevelsAndLastPaidBlockAreSafe(GameTestHelper h) {
        for(Item item:new Item[]{JugcraftTools.ROCKET_PACK,io.github.jimbozoomer.jugcraft.gear.JugcraftGear.ITEMS.get("power_bow"),io.github.jimbozoomer.jugcraft.gear.JugcraftGear.ITEMS.get("power_katana")})
            h.assertTrue(ToolUpgrades.fit(h.getLevel(),new ItemStack(item),ToolUpgrades.Kind.EFFICIENCY)==ToolUpgrades.Result.WRONG_TOOL,"Mining efficiency fitted non-mining equipment");
        var drill=tool(JugcraftTools.MINING_DRILL,42,0,Integer.MAX_VALUE);
        h.assertTrue(((PoweredToolItem)drill.getItem()).energyPerBlock(drill)==42&&drill.getDestroySpeed(Blocks.STONE.defaultBlockState())>1,"Discounted last-block threshold is incorrect");
        Chargeable.setEnergy(drill,41);h.assertTrue(drill.getDestroySpeed(Blocks.STONE.defaultBlockState())==1,"Insufficient JE still powered the tool");
        h.assertTrue(PoweredToolItem.efficientCost(drill,1)==1&&PoweredToolItem.efficientCost(drill,Long.MAX_VALUE)>0,"Discount created free or negative-cost actions");
        drill.set(JugcraftTools.EFFICIENCY,Integer.MIN_VALUE);h.assertTrue(((PoweredToolItem)drill.getItem()).energyPerBlock(drill)==60,"Negative level did not fall back to no efficiency");
        h.succeed();
    }
    @GameTest public void threeByThreeChargesEveryBrokenBlockAtDiscountedPrice(GameTestHelper h) {
        var p=player(h);var drill=tool(JugcraftTools.MINING_DRILL,378,0,2);drill.set(JugcraftTools.DRILL_MODE,MiningDrillItem.AREA);p.setItemInHand(InteractionHand.MAIN_HAND,drill);p.setYRot(0);p.setXRot(0);
        for(int x=1;x<=3;x++)for(int y=2;y<=4;y++)h.setBlock(new BlockPos(x,y,3),Blocks.STONE);
        p.gameMode.handleBlockBreakAction(h.absolutePos(new BlockPos(2,3,3)),net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,Direction.NORTH,h.getLevel().getMaxY(),0);
        h.assertTrue(p.gameMode.destroyBlock(h.absolutePos(new BlockPos(2,3,3))),"Root block was not mined");
        for(int x=1;x<=3;x++)for(int y=2;y<=4;y++)h.assertTrue(h.getBlockState(new BlockPos(x,y,3)).isAir(),"An extra block used the old price or remained behind");
        h.assertTrue(Chargeable.energy(drill)==0,"Nine blocks must spend exactly 378 JE");h.succeed();
    }
    @GameTest public void treeAndLeafModesChargeEachSuccessfulBreak(GameTestHelper h) {
        var p=player(h);var saw=tool(JugcraftTools.CHAINSAW,84,0,2);p.setItemInHand(InteractionHand.MAIN_HAND,saw);
        for(int y=2;y<=4;y++)h.setBlock(new BlockPos(2,y,2),Blocks.OAK_LOG);
        h.setBlock(new BlockPos(2,1,2),Blocks.DIRT);ChainsawTreeGameTests.canopy(h,new BlockPos(2,4,2),false);
        p.gameMode.destroyBlock(h.absolutePos(new BlockPos(2,2,2)));
        for(int y=2;y<=4;y++)h.assertTrue(h.getBlockState(new BlockPos(2,y,2)).isAir(),"Tree felling did not apply efficiency to all logs");
        h.assertTrue(Chargeable.energy(saw)==0,"Three logs must cost 84 JE");
        for(var at:BlockPos.betweenClosed(new BlockPos(0,3,0),new BlockPos(4,5,4)))
            if(h.getBlockState(at).is(net.minecraft.tags.BlockTags.LEAVES))h.setBlock(at,Blocks.AIR);
        Chargeable.setEnergy(saw,28);EquipmentSettings.setNumber(saw,"saw_mode",2);
        for(int x=2;x<=5;x++)h.setBlock(new BlockPos(x,3,4),Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        p.gameMode.destroyBlock(h.absolutePos(new BlockPos(2,3,4)));
        for(int x=2;x<=5;x++)h.assertTrue(h.getBlockState(new BlockPos(x,3,4)).isAir(),"Leaf clearing did not apply efficiency to all leaves");
        h.assertTrue(Chargeable.energy(saw)==0,"Four leaves must cost 28 JE");h.succeed();
    }
    @GameTest public void recipeAndDrillTierUpgradeKeepNativeAcquisitionAndComponents(GameTestHelper h) {
        var s=new ItemStack(item("steel_plate"));var c=new ItemStack(item("copper_cable"));var a=new ItemStack(item("advanced_circuit"));
        var input=CraftingInput.of(3,3,List.of(s,c,s,c,a,c,s,c,s));
        var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(recipe.isPresent(),"Efficiency recipe is unavailable");var result=recipe.orElseThrow().value().assemble(input);
        h.assertTrue(result.is(JugcraftTools.MODULES.get(ToolUpgrades.Kind.EFFICIENCY))&&result.getCount()==1,"Recipe returned the wrong module");
        h.assertTrue(io.github.jimbozoomer.jugcraft.concordance.assay.AlchemyValues.source("jugcraft:efficiency_module").equals("recipe jugcraft:efficiency_module"),"New module uses a one-essence fallback");
        var drill=tool(IndustrialGear.ITEMS.get("jimta_industrial_drill"),12345,1,2);drill.set(DataComponents.CUSTOM_NAME,Component.literal("Upgraded efficient drill"));
        var tier=CraftingInput.of(2,2,List.of(drill,new ItemStack(Items.DIAMOND),new ItemStack(Items.DIAMOND),a));
        var upgrade=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,tier,h.getLevel()).orElseThrow().value().assemble(tier);
        h.assertTrue(upgrade.is(IndustrialGear.ITEMS.get("jimta_diamond_drill"))&&ToolUpgrades.level(upgrade,ToolUpgrades.Kind.EFFICIENCY)==2&&Chargeable.energy(upgrade)==12345,"Tier crafting lost efficiency or charge");
        h.assertTrue(((PoweredToolItem)upgrade.getItem()).energyPerBlock(upgrade)==63&&upgrade.getHoverName().getString().equals("Upgraded efficient drill"),"Tier crafting changed configured tool behavior");h.succeed();
    }
    @GameTest public void vanillaEfficiencyEnchantIsSeparateFromEnergyModule(GameTestHelper h) {
        var drill=tool(JugcraftTools.MINING_DRILL,10000,0,0);var enchantment=h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY);drill.enchant(enchantment,5);
        h.assertTrue(((PoweredToolItem)drill.getItem()).energyPerBlock(drill)==60,"Vanilla Efficiency unexpectedly discounted JE");
        ToolUpgrades.fit(h.getLevel(),drill,ToolUpgrades.Kind.EFFICIENCY);
        h.assertTrue(((PoweredToolItem)drill.getItem()).energyPerBlock(drill)==51&&drill.getEnchantments().getLevel(enchantment)==5,"Energy module modified the existing enchantment");h.succeed();
    }
}
