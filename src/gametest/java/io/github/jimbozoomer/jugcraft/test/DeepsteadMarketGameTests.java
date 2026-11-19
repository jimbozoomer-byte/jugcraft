package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.DeepsteadDwarf;
import io.github.jimbozoomer.jugcraft.town.DeepsteadWares;
import io.github.jimbozoomer.jugcraft.town.GalleryChimesBlock;
import io.github.jimbozoomer.jugcraft.town.Jugs;
import io.github.jimbozoomer.jugcraft.town.ShopMenu;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.concordance.assay.TabletOutputs;
import java.util.HashSet;
import net.minecraft.world.entity.player.Inventory;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class DeepsteadMarketGameTests {
    @GameTest public void guild_specialists_sell_all_twenty_products_and_reject_invalid_customers(GameTestHelper h) {
        var level = h.getLevel();
        var buyer = h.makeMockServerPlayerInLevel();
        buyer.setGameMode(GameType.SURVIVAL);
        var other = h.makeMockServerPlayerInLevel();
        var treasury = level.getServer();
        Jugs.add(treasury, buyer.getUUID(), 2000);
        var merchant = new DeepsteadDwarf(DeepsteadDwarf.TYPE, level);
        merchant.snapTo(buyer.getX(), buyer.getY(), buyer.getZ(), 0, 0);
        var distinct = new HashSet<String>();
        for (String shopId : new String[]{DeepsteadWares.STONEWRIGHT, DeepsteadWares.COPPER_KETTLE}) {
            var position = buyer.blockPosition();
            var stall = new TownData.Spot(-1, "deepstead_merchant", shopId, "Guild keeper", "deepstead_merchant_"+shopId+"_0", position);
            merchant.setUp(stall, position);
            var impostor = new Townsfolk(JugcraftTown.TOWNSFOLK, level);
            impostor.setUp(stall, position);
            h.assertTrue(ShopMenu.serve(1, buyer, impostor, false)==null, "A non-dwarf cannot host "+shopId);
            var shop = ShopMenu.serve(1, buyer, merchant, false);
            h.assertTrue(shop!=null && shop.sales().size()==10 && shop.purchases().isEmpty(), "Exactly ten wares at "+shopId);
            h.assertTrue(!shop.clickMenuButton(other, 0), "Another player cannot reuse a screen");
            for (int slot=0; slot<10; slot++) {
                var offer = shop.sales().get(slot);
                var ware = new ItemStack(offer.itemType());
                h.assertTrue(distinct.add(offer.item()) && !TabletOutputs.allowed(ware), "Every specialty is unique and non-formable");
                long balance = Jugs.balance(treasury,buyer.getUUID());
                int owned = buyer.getInventory().countItem(offer.itemType());
                ShopMenu.clear();
                h.assertTrue(shop.clickMenuButton(buyer,slot) && Jugs.balance(treasury,buyer.getUUID())==balance-offer.price()
                        && buyer.getInventory().countItem(offer.itemType())==owned+offer.count(), "Supply, price and count of "+offer.item());
            }
            ShopMenu.clear();
            h.assertTrue(!shop.clickMenuButton(buyer,10),"Cannot buy a hidden eleventh offer");
            shop.removed(buyer);
            h.assertTrue(!shop.clickMenuButton(buyer,0),"Closed guild screen cannot replay a sale");
        }
        h.assertTrue(distinct.size()==20,"Twenty distinct specialties across both guild halls");
        var menu = ShopMenu.serve(2,buyer,merchant,false);
        buyer.getInventory().clearContent();
        for(int i=0;i<Inventory.INVENTORY_SIZE;i++) buyer.getInventory().setItem(i,new ItemStack(net.minecraft.world.item.Items.DIRT,64));
        long balance=Jugs.balance(treasury,buyer.getUUID());
        ShopMenu.clear();
        h.assertTrue(menu!=null && !menu.clickMenuButton(buyer,7) && Jugs.balance(treasury,buyer.getUUID())==balance,
            "A full pack is never charged or spilled into the public market");
        h.succeed();
    }

    @GameTest public void dwarf_stalls_charge_jugs_and_refuse_stale_or_remote_sales(GameTestHelper h) {
        var level=h.getLevel();
        var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        var npc=new DeepsteadDwarf(DeepsteadDwarf.TYPE,level);
        npc.configure(DeepsteadDwarf.Role.MERCHANT,"Lampwright",player.blockPosition(),0);
        npc.snapTo(player.getX(),player.getY(),player.getZ(),0,0);
        var shop=ShopMenu.serve(1,player,npc,false);
        h.assertTrue(shop!=null && shop.opening.shop().equals(DeepsteadWares.LAMPWRIGHT), "Lampwright serves a real town menu");
        h.assertTrue(shop.shop().openAt(23999) && shop.purchases().isEmpty(), "Overnight guild counter sells for Jugs only");
        var id=player.getUUID(); var server=level.getServer();
        long starting=Jugs.balance(server,id);
        if(starting>0) Jugs.take(server,id,starting);
        ShopMenu.clear();
        h.assertTrue(!shop.clickMenuButton(player,0) && Jugs.balance(server,id)==0, "Unaffordable wares stay on the counter");
        Jugs.add(server,id,20);
        ShopMenu.clear();
        h.assertTrue(shop.sales().getFirst().itemType()==DeepsteadWares.KNOT_TILE.asItem()
                && shop.clickMenuButton(player,0) && Jugs.balance(server,id)==8
                && player.getInventory().countItem(DeepsteadWares.KNOT_TILE.asItem())==4, "Four inlay tiles cost twelve Jugs");
        h.assertTrue(!shop.clickMenuButton(player,0), "A replayed click in the same tick buys nothing");
        npc.configure(DeepsteadDwarf.Role.MERCHANT,"Outfitter",player.blockPosition(),1);
        h.assertTrue(!shop.stillValid(player), "Changing counter or keeper invalidates the old screen");
        var outfitter=ShopMenu.serve(2,player,npc,false);
        h.assertTrue(outfitter!=null && outfitter.sales().getFirst().itemType()==DeepsteadWares.SHAFT_PLUMMET
                && outfitter.sales().stream().noneMatch(o->o.itemType()==DeepsteadWares.READING_LAMP.asItem()),"Outfitter has its own utility stock");
        npc.setPos(player.getX()+TownShops.get().tradeRange+1,player.getY(),player.getZ());
        ShopMenu.clear();
        h.assertTrue(!outfitter.clickMenuButton(player,0) && Jugs.balance(server,id)==8, "Walking away prevents payment and delivery");
        h.succeed();
    }

    @GameTest public void home_fixtures_change_light_and_hold_their_tune_on_a_redstone_pulse(GameTestHelper h) {
        var level=h.getLevel(); var player=h.makeMockServerPlayerInLevel();
        var at=h.absolutePos(new BlockPos(2,2,2));
        var hit=new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false);
        level.setBlock(at,DeepsteadWares.READING_LAMP.defaultBlockState(),Block.UPDATE_ALL);
        for(int next:new int[]{0,7,14}) {
            level.getBlockState(at).useWithoutItem(level,player,hit);
            h.assertTrue(level.getBlockState(at).getLightEmission()==next,"Lamp advances to "+next+" light");
        }
        level.setBlock(at,DeepsteadWares.GALLERY_CHIMES.defaultBlockState(),Block.UPDATE_ALL);
        player.setShiftKeyDown(true);
        level.getBlockState(at).useWithoutItem(level,player,hit);
        player.setShiftKeyDown(false);
        level.getBlockState(at).useWithoutItem(level,player,hit);
        level.setBlock(at.east(),Blocks.REDSTONE_BLOCK.defaultBlockState(),Block.UPDATE_ALL);
        h.assertTrue(level.getBlockState(at).getValue(GalleryChimesBlock.TONE)==1 &&
                level.getBlockState(at).getValue(BlockStateProperties.POWERED),"Pulse strikes the selected note");
        level.removeBlock(at.east(),false);
        h.assertTrue(!level.getBlockState(at).getValue(BlockStateProperties.POWERED),"Stand is ready for the next pulse");
        h.succeed();
    }

    @GameTest public void plummet_stops_at_surface_and_reel_remembers_its_pin(GameTestHelper h) {
        var level=h.getLevel(); var lip=h.absolutePos(new BlockPos(2,4,2));
        for(int i=0;i<3;i++)level.setBlock(lip.below(i),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        level.setBlock(lip.below(3),Blocks.WATER.defaultBlockState(),Block.UPDATE_CLIENTS);
        h.assertTrue(DeepsteadWares.soundShaft(level,lip).equals(new DeepsteadWares.Sounding("water",3,lip.getY()-3)), "Open shaft finds water");
        level.setBlock(lip.below(2),Blocks.LAVA.defaultBlockState(),Block.UPDATE_CLIENTS);
        h.assertTrue(DeepsteadWares.soundShaft(level,lip).surface().equals("lava"),"Lava is distinguished from water");
        level.setBlock(lip.below(),Blocks.DEEPSLATE.defaultBlockState(),Block.UPDATE_CLIENTS);
        h.assertTrue(DeepsteadWares.soundShaft(level,lip).equals(new DeepsteadWares.Sounding("stone",1,lip.getY()-1)), "Solid ledge conceals the lava below");
        var player=h.makeMockServerPlayerInLevel();
        var reel=new ItemStack(DeepsteadWares.SURVEYORS_REEL);
        player.setItemInHand(InteractionHand.MAIN_HAND,reel);
        var mark=GlobalPos.of(level.dimension(),player.blockPosition());
        player.setShiftKeyDown(true);
        DeepsteadWares.SURVEYORS_REEL.use(level,player,InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        player.setPos(player.getX()+3,player.getY()+8,player.getZ()+4);
        DeepsteadWares.SURVEYORS_REEL.use(level,player,InteractionHand.MAIN_HAND);
        var stored=reel.copy().get(DataComponents.LODESTONE_TRACKER);
        h.assertTrue(stored!=null && stored.target().orElseThrow().equals(mark) && !stored.tracked(),
                "Reading the reel preserves its untracked position on the item without placing a marker");
        h.succeed();
    }
}
