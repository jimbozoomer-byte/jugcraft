package io.github.jimbozoomer.jugcraft.town;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Vanilla menu synchronization; no client-supplied item, quota, recipient or price. */
public final class CommissionMenu extends AbstractContainerMenu {
    private static final int FIELDS=19;
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
    @Override public boolean stillValid(Player player){
        return owner==null || (player==owner && player.level()==boardLevel && player.isAlive() && !player.isSpectator() && player.level().getBlockState(board).is(JugcraftTown.COMMISSION_BOARD)
            && player.distanceToSqr(board.getX()+.5,board.getY()+.5,board.getZ()+.5)<=64
            && io.github.jimbozoomer.jugcraft.finance.LandRegistry.mayBuild(player,player.level(),board));
    }
    @Override public boolean clickMenuButton(Player player,int id){
        if(owner==null || !stillValid(player) || id<0 || id>=8)return false;
        long now=owner.level().getGameTime();if(lastAction!=Long.MIN_VALUE && now-lastAction<4)return false;lastAction=now;
        boolean done=id<4?TownCommissions.deliver(owner,id):TownCommissions.claim(Jugs.data(owner.level().getServer()),owner.getUUID(),id-4,TownCommissions.cycle(owner));
        if(!done)owner.sendOverlayMessage(Component.translatable("message.jugcraft.commission.unavailable"));
        broadcastChanges();return done;
    }
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}
