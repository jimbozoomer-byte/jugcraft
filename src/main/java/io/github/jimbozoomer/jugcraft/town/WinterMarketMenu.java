package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class WinterMarketMenu extends AbstractContainerMenu {
    public static final int PAGE_SIZE=5, CARRY=5, PREV=10, NEXT=11, SWITCH=12;
    public record Opening(BlockPos pos,int mode,int page,int pages,List<String> rows,String held) {
        public static final StreamCodec<RegistryFriendlyByteBuf,Opening> CODEC=StreamCodec.composite(
            BlockPos.STREAM_CODEC,Opening::pos,ByteBufCodecs.VAR_INT,Opening::mode,ByteBufCodecs.VAR_INT,Opening::page,
            ByteBufCodecs.VAR_INT,Opening::pages,ByteBufCodecs.stringUtf8(200).apply(ByteBufCodecs.list(PAGE_SIZE)),Opening::rows,
            ByteBufCodecs.stringUtf8(128),Opening::held,Opening::new);
    }
    public final Opening view;
    private final WinterMarket.Counter counter;
    private final List<UUID> keys;
    private final ItemStack offered;
    private boolean closed;

    public WinterMarketMenu(int id,Inventory inventory,Opening view) { this(id,view,null,List.of(),ItemStack.EMPTY); }
    private WinterMarketMenu(int id,Opening view,WinterMarket.Counter counter,List<UUID> keys,ItemStack offered) {
        super(WinterMarket.MENU,id);
        this.view=view; this.counter=counter; this.keys=keys; this.offered=offered;
    }
    static boolean canUse(ServerPlayer p,BlockPos pos,WinterMarket.Counter counter) {
        return p.isAlive() && !p.isSpectator() && p.level().hasChunkAt(pos) && p.level().getBlockState(pos).is(counter)
            && p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=36
            && LandRegistry.mayBuild(p,p.level(),pos)
            && (!OperatorRegions.protects(p.level(),pos) || TownProtection.exempt(p))
            && (!ApartmentRentals.protects(p.level(),pos) || ApartmentRentals.mayUseFurniture(p,p.level(),pos) || TownProtection.exempt(p));
    }
    static void open(ServerPlayer p,BlockPos pos,WinterMarket.Counter counter,boolean inbox,int requestedPage) {
        var post=WinterGiftPost.get(p.level().getServer());
        List<String> rows;
        List<UUID> all;
        int mode;
        if (counter==WinterMarket.POST && inbox) {
            var mail=post.inbox(p.getUUID());
            rows=mail.stream().map(WinterGiftPost.Parcel::sender).toList();
            all=mail.stream().map(WinterGiftPost.Parcel::id).toList(); mode=1;
        } else if (counter==WinterMarket.POST) {
            all=post.recipients(p.getUUID()); rows=all.stream().map(post::name).toList(); mode=0;
        } else {
            rows=counter.offers.stream().map(o->o.count()+" × "+o.stack().getHoverName().getString()+"   ·   "+o.price()+" J").toList();
            all=List.of(); mode=2;
        }
        int pages=Math.max(1,Math.ceilDiv(rows.size(),PAGE_SIZE)),page=Math.clamp(requestedPage,0,pages-1);
        int from=page*PAGE_SIZE,to=Math.min(from+PAGE_SIZE,rows.size());
        var keys=all.isEmpty()?List.<UUID>of():List.copyOf(all.subList(from,to));
        var stack=p.getMainHandItem().copy();
        String label=stack.isEmpty() || stack.is(WinterMarket.WRAPPED) ? "" : stack.getCount()+" × "+stack.getHoverName().getString();
        var view=new Opening(pos.immutable(),mode,page,pages,List.copyOf(rows.subList(from,to)),label.substring(0,Math.min(128,label.length())));
        p.openMenu(new ExtendedMenuProvider<Opening>() {
            @Override public Opening getScreenOpeningData(ServerPlayer player) { return view; }
            @Override public Component getDisplayName() { return counter.getName(); }
            @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player) {
                return new WinterMarketMenu(id,view,counter,keys,stack);
            }
        });
    }
    @Override public void removed(Player p) { closed=true; super.removed(p); }
    @Override public boolean stillValid(Player p) { return !(p instanceof ServerPlayer sp) || !closed && canUse(sp,view.pos(),counter); }
    @Override public ItemStack quickMoveStack(Player p,int slot) { return ItemStack.EMPTY; }
    @Override public boolean clickMenuButton(Player player,int action) {
        if (!(player instanceof ServerPlayer p) || p.containerMenu!=this || !stillValid(p)) return false;
        boolean inbox=view.mode()==1;
        if (action==PREV || action==NEXT || action==SWITCH) {
            if (action==SWITCH && counter!=WinterMarket.POST) return false;
            open(p,view.pos(),counter,action==SWITCH?!inbox:inbox,action==SWITCH?0:view.page()+(action==NEXT?1:-1));
            return true;
        }
        if (action<0 || action>=2*PAGE_SIZE) return false;
        int row=action%PAGE_SIZE;
        if (row>=view.rows().size() || (action>=CARRY && view.mode()!=0)) return false;
        boolean success;
        var post=WinterGiftPost.get(p.level().getServer());
        if (view.mode()==0) success=post.wrap(p,keys.get(row),offered,action>=CARRY);
        else if (inbox) success=post.unwrap(p,keys.get(row),InteractionHand.MAIN_HAND);
        else {
            var offer=counter.offers.get(row);
            var goods=offer.stack();
            if (goods.isEmpty()) return false;
            if (!WinterGiftPost.fitsInventory(p,goods)) {
                p.sendOverlayMessage(Component.translatable("message.jugcraft.market.make_room"));
                return false;
            }
            success=Jugs.take(p.level().getServer(),p.getUUID(),offer.price());
            if (success) p.getInventory().add(goods);
            else p.sendOverlayMessage(Component.translatable("message.jugcraft.shop.too_few_jugs",offer.price()));
        }
        open(p,view.pos(),counter,inbox,view.page());
        return success;
    }
}
