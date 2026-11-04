package local.peepo;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Real catalogue paging and source conservation, without a rendered client. */
public final class CraftingTablePlanningGameTests {
    private CraftingTableCompanionBlockEntity table(GameTestHelper helper) {
        var pos=new BlockPos(1,1,1);
        helper.setBlock(pos,Blocks.CRAFTING_TABLE);
        var block=helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(block instanceof CraftingTableCompanionBlockEntity,"Vanilla table has its native companion workspace");
        return (CraftingTableCompanionBlockEntity)block;
    }

    @GameTest public void emptyFilterSearchesTheLoadedCatalogueWithoutConsumingSupply(GameTestHelper helper) {
        var table=table(helper);
        var supply=new SimpleContainer(new ItemStack(Items.OAK_LOG));
        var storage=ContainerStorage.of(supply,Direction.UP);
        int recipes=CraftingTableRecipes.of(helper.getLevel().getServer().getRecipeManager()).all.size();
        int pages=Math.ceilDiv(recipes,CraftingTableCompanionBlockEntity.PLANNING_PAGE_SIZE),attempt=0;
        while(attempt<pages&&table.plan()==null){table.prepare(storage,null);attempt++;}
        helper.assertTrue(table.plan()!=null&&!table.plan().result().isEmpty(),"Available ordinary recipe found within a full bounded catalogue pass");
        helper.assertTrue(supply.getItem(0).is(Items.OAK_LOG)&&supply.getItem(0).getCount()==1,"Planning leaves the actual Supply unchanged");
        helper.assertTrue(!table.hasInputs()&&!table.hasOutput(),"Planning creates neither staged ingredients nor products");
        io.github.jimbozoomer.jugcraft.Jugcraft.LOGGER.info("Crafting-table catalogue regression: {} recipes, {} bounded pages, available plan on page {}",recipes,pages,attempt);
        helper.succeed();
    }

    @GameTest public void emptySupplyCannotProduceAPlanAfterEveryCataloguePage(GameTestHelper helper) {
        var table=table(helper);
        var storage=ContainerStorage.of(new SimpleContainer(1),Direction.UP);
        int pages=Math.ceilDiv(CraftingTableRecipes.of(helper.getLevel().getServer().getRecipeManager()).all.size(),CraftingTableCompanionBlockEntity.PLANNING_PAGE_SIZE);
        for(int page=0;page<pages;page++)table.prepare(storage,null);
        helper.assertTrue(table.plan()==null&&!table.hasInputs()&&!table.hasOutput(),"Paging through recipes cannot invent ingredients or products");
        helper.succeed();
    }
}
