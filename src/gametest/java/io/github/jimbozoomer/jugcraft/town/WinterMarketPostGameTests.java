package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.test.TestPlayers;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class WinterMarketPostGameTests {
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;

    @GameTest public void offlineAddressAndCopiedWrapperRedeemOnlyOnce(GameTestHelper h) {
        var giver = TestPlayers.survivor(h);
        var friend = TestPlayers.survivor(h);
        var recipient = friend.getUUID();
        var post = WinterGiftPost.get(h.getLevel().getServer());
        post.remember(friend);
        TestPlayers.leave(friend);
        var gift = new ItemStack(Items.WRITTEN_BOOK);
        gift.set(DataComponents.CUSTOM_NAME,Component.literal("For the harbourmaster"));
        gift.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("Winter crossings"),
            "Harbour pilot",0,java.util.List.of(Filterable.passThrough(Component.literal("Meet me at the blue stall."))),true));
        giver.setItemInHand(HAND,gift.copy());
        h.assertTrue(post.recipients(giver.getUUID()).contains(recipient),"Offline player missing from address book");
        var level=h.getLevel();
        var desk=h.absolutePos(new BlockPos(2,2,2));
        level.setBlockAndUpdate(desk,WinterMarket.POST.defaultBlockState());
        giver.snapTo(Vec3.atBottomCenterOf(desk.south()));
        int address=post.recipients(giver.getUUID()).indexOf(recipient);
        WinterMarketMenu.open(giver,desk,WinterMarket.POST,false,address/WinterMarketMenu.PAGE_SIZE);
        var menu=(WinterMarketMenu)giver.containerMenu;
        int action=WinterMarketMenu.CARRY+address%WinterMarketMenu.PAGE_SIZE;
        h.assertTrue(menu.clickMenuButton(giver,action),"Could not wrap the signed book at a counter");
        h.assertTrue(!menu.clickMenuButton(giver,action),"Old menu resent the book");
        var wrapper = giver.getMainHandItem().copy();
        var id = WinterGiftPost.parcelId(wrapper);
        h.assertTrue(id != null && !post.unwrap(giver,id,HAND),"Sender opened a private gift");
        var ops = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        var saved = WinterGiftPost.CODEC.encodeStart(ops,post).getOrThrow();
        var reopened = WinterGiftPost.CODEC.parse(ops,saved).getOrThrow();
        var receiver = TestPlayers.again(h,recipient);
        receiver.setItemInHand(HAND,wrapper.copy());
        h.assertTrue(reopened.unwrap(receiver,id,HAND) && ItemStack.matches(receiver.getMainHandItem(),gift),"Lost contents or custom name on restore");
        receiver.setItemInHand(HAND,wrapper);
        h.assertTrue(!reopened.unwrap(receiver,id,HAND) && reopened.inbox(recipient).isEmpty(),"Copied wrapper paid twice");
        h.succeed();
    }

    @GameTest public void changedStackAndFullBagsKeepBothPlayersWhole(GameTestHelper h) {
        var giver = TestPlayers.survivor(h);
        var other = TestPlayers.survivor(h);
        var post = WinterGiftPost.get(h.getLevel().getServer());
        post.remember(other);
        var gift = new ItemStack(Items.DIAMOND,3);
        giver.setItemInHand(HAND,gift.copy());
        h.assertTrue(!post.wrap(giver,other.getUUID(),new ItemStack(Items.DIAMOND,2),false),"Stale preview sent a different stack");
        h.assertTrue(post.wrap(giver,other.getUUID(),gift,false) && giver.getMainHandItem().isEmpty(),"Post did not take the stack");
        for (int i=0;i<other.getInventory().getNonEquipmentItems().size();i++)
            other.getInventory().setItem(i,new ItemStack(Items.DIRT,64));
        var id=post.inbox(other.getUUID()).getFirst().id();
        h.assertTrue(!post.unwrap(other,id,HAND) && post.inbox(other.getUUID()).size()==1,"Full bag lost a parcel");
        other.setItemInHand(HAND,ItemStack.EMPTY);
        h.assertTrue(post.unwrap(other,id,HAND) && ItemStack.matches(gift,other.getMainHandItem()),"Could not collect mailed gift");
        h.succeed();
    }

    @GameTest public void bakeryAndToymakerChargeOnlyForDeliveredStock(GameTestHelper h) {
        var p=TestPlayers.survivor(h);
        var level=h.getLevel();
        var pos=h.absolutePos(new BlockPos(2,2,2));
        p.snapTo(Vec3.atBottomCenterOf(pos.south()));
        Jugs.add(level.getServer(),p.getUUID(),25);
        for (var booth : new WinterMarket.Counter[]{WinterMarket.BAKERY,WinterMarket.TOYS}) {
            level.setBlockAndUpdate(pos,booth.defaultBlockState());
            for (int row=0; row<booth.offers.size(); row++) {
                long before=Jugs.balance(level.getServer(),p.getUUID());
                var offer=booth.offers.get(row);
                WinterMarketMenu.open(p,pos,booth,false,0);
                var menu=(WinterMarketMenu)p.containerMenu;
                h.assertTrue(menu.clickMenuButton(p,row),"Purchase failed: "+offer.id());
                h.assertTrue(!menu.clickMenuButton(p,row),"Expired purchase replayed");
                h.assertTrue(Jugs.balance(level.getServer(),p.getUUID())==before-offer.price()
                    && p.getInventory().countItem(offer.stack().getItem())>=offer.count(),"Wrong stock or price: "+offer.id());
            }
        }
        for (int i=0;i<p.getInventory().getNonEquipmentItems().size();i++) p.getInventory().setItem(i,new ItemStack(Items.DIRT,64));
        long before=Jugs.balance(level.getServer(),p.getUUID());
        WinterMarketMenu.open(p,pos,WinterMarket.TOYS,false,0);
        h.assertTrue(!p.containerMenu.clickMenuButton(p,0) && before==Jugs.balance(level.getServer(),p.getUUID()),"Charged a full inventory");
        h.succeed();
    }
}
