package io.github.jimbozoomer.jugcraft.town;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla menu synchronization; no client-supplied item, quota, recipient or price. Fields 0-18 are the city orders;
 * 19-34 each Halloween errand's done flag and carried count (HalloweenQuests), and 35 whether it is the season.
 */
public final class CommissionMenu extends AbstractContainerMenu {
    private static final int ORDER_FIELDS=19;
    private static final int FIELDS=ORDER_FIELDS+2*HalloweenQuests.QUESTS.size()+1;
    private final BlockPos board;
    private final ContainerData data;
    private final ServerPlayer owner;
    private final net.minecraft.server.level.ServerLevel boardLevel;
    private long lastAction=Long.MIN_VALUE;
    public CommissionMenu(int id, Inventory inventory, BlockPos board) {
        this(id,board,new SimpleContainerData(FIELDS),null);
    }
    private CommissionMenu(int id,BlockPos board,ContainerData data,ServerPlayer owner) {
        super(JugcraftTown.COMMISSION_MENU,id);this.board=board.immutable();this.data=data;this.owner=owner;this.boardLevel=owner==null?null:owner.level();addDataSlots(data);
    }
    public static void open(ServerPlayer player,BlockPos pos) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            public BlockPos getScreenOpeningData(ServerPlayer p) { return pos; }
            public Component getDisplayName() { return Component.translatable("block.jugcraft.town_commission_board"); }
            public AbstractContainerMenu createMenu(int id,Inventory inventory,Player p) {
                return new CommissionMenu(id,pos,new ContainerData() {
                    public int getCount(){return FIELDS;}
                    public void set(int i,int v){}
                    public int get(int i){
                        var ledger=Jugs.data(player.level().getServer());
                        if(i==FIELDS-1)return io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason.active()?1:0;
                        if(i>=ORDER_FIELDS){int q=(i-ORDER_FIELDS)/2;
                            return (i-ORDER_FIELDS)%2==0?(HalloweenQuests.done(ledger,player.getUUID(),q)?1:0):HalloweenQuests.carried(player,q);}
                        if(i==18)return (int)((TownCommissions.PERIOD-player.level().getServer().overworld().getGameTime()%TownCommissions.PERIOD+19)/20);
                        if(i==16)return (int)(ledger.balance(player.getUUID())&65535);
                        if(i==17)return (int)((ledger.balance(player.getUUID())>>>16)&65535);
                        int n=i/4;var progress=TownCommissions.progress(ledger,player.getUUID(),n,TownCommissions.cycle(player));
                        return switch(i%4){case 0->progress.count();case 1->progress.paid()?1:0;
                            case 2->TownCommissions.carried(player,n);default->TownCommissions.partyDelivered(player,n);};
                    }
                },player);
            }
        });
    }
    public int progress(int i){return data.get(i*4);}
    public boolean paid(int i){return data.get(i*4+1)!=0;}
    public int carried(int i){return data.get(i*4+2);}
    public int party(int i){return data.get(i*4+3);}
    public int renewalSeconds(){return data.get(18);}
    public long balance(){return(data.get(16)&65535L)|((data.get(17)&65535L)<<16);}
    public boolean questDone(int q){return data.get(ORDER_FIELDS+2*q)!=0;}
    public int questCarried(int q){return data.get(ORDER_FIELDS+2*q+1);}
    public boolean halloween(){return data.get(FIELDS-1)!=0;}
    @Override public boolean stillValid(Player player){
        return owner==null || (player==owner && player.level()==boardLevel && player.isAlive() && !player.isSpectator() && player.level().getBlockState(board).is(JugcraftTown.COMMISSION_BOARD)
            && player.distanceToSqr(board.getX()+.5,board.getY()+.5,board.getZ()+.5)<=64
            && io.github.jimbozoomer.jugcraft.finance.LandRegistry.mayBuild(player,player.level(),board));
    }
    @Override public boolean clickMenuButton(Player player,int id){
        // 0-3 deliver a city order, 4-7 collect one, 8 on: hand in a Halloween errand.
        if(owner==null || !stillValid(player) || id<0 || id>=8+HalloweenQuests.QUESTS.size())return false;
        long now=owner.level().getGameTime();if(lastAction!=Long.MIN_VALUE && now-lastAction<4)return false;lastAction=now;
        boolean done=id>=8?HalloweenQuests.complete(owner,id-8)
            :id<4?TownCommissions.deliver(owner,id):TownCommissions.claim(Jugs.data(owner.level().getServer()),owner.getUUID(),id-4,TownCommissions.cycle(owner));
        if(!done)owner.sendOverlayMessage(Component.translatable("message.jugcraft.commission.unavailable"));
        else if(id>=4&&id<8)BountyList.questCompleted(owner);
        broadcastChanges();return done;
    }
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}
