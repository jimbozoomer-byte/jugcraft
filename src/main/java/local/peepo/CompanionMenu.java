package local.peepo;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.flag.FeatureFlags;

public final class CompanionMenu extends AbstractContainerMenu {
    public static MenuType<CompanionMenu> TYPE;
    private final PeepoEntity npc;
    private final ContainerData data;
    public static void initialize(){TYPE=Registry.register(BuiltInRegistries.MENU,PeepoMod.id("companion_commands"),new MenuType<>(CompanionMenu::new,FeatureFlags.VANILLA_SET));}
    public CompanionMenu(int id,Inventory inventory){this(id,inventory,null);}
    public CompanionMenu(int id,Inventory inventory,PeepoEntity npc){
        super(TYPE,id);this.npc=npc;
        data=npc==null?new SimpleContainerData(11):new ContainerData(){
            public int get(int i){return switch(i){case 0->npc.orders.mode();case 1->npc.getEnergy()*100/npc.getEnergyCapacity();case 2->Math.round(npc.getHealth()*100/npc.getMaxHealth());case 3->npc.orders.radius();case 4->npc.orders.party()?1:0;case 5->npc.orders.owner(inventory.player)?1:0;case 6->npc.orders.homeHere()?1:0;case 7->npc.orders.workHere()?1:0;case 8->npc.orders.targetAvailable()?1:0;case 9->npc.getId()&0xffff;case 10->(npc.getId()>>>16)&0xffff;default->0;};}
            public void set(int i,int value){}public int getCount(){return 11;}
        };
        addDataSlots(data);
        var contents=npc==null?new net.minecraft.world.SimpleContainer(10):npc.belongings;
        for(int row=0;row<2;row++)for(int col=0;col<4;col++)addSlot(new Slot(contents,col+row*4,16+col*18,96+row*18));
        addSlot(new Slot(contents,8,16,54){
            public boolean mayPlace(ItemStack stack){return stack.is(net.minecraft.world.item.Items.JACK_O_LANTERN);}
            public int getMaxStackSize(){return 1;}
        });
        addSlot(new Slot(contents,9,70,54){public int getMaxStackSize(){return 1;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,80+col*18,150+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,80+col*18,208));
    }
    public PeepoEntity companion(Player player){if(npc!=null)return npc;var entity=player.level().getEntity((data.get(9)&0xffff)|((data.get(10)&0xffff)<<16));return entity instanceof PeepoEntity p?p:null;}
    public int value(int i){return data.get(i);}
    @Override public boolean stillValid(Player player){return npc==null || npc.isAlive() && npc.level()==player.level() && npc.distanceToSqr(player)<=64 && !player.isSpectator() && npc.orders.allowed(player);}
    @Override public boolean clickMenuButton(Player player,int id){
        if(npc==null || !stillValid(player))return false;
        boolean changed=npc.orders.command(player,id);if(changed)broadcastChanges();return changed;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var copy=stack.copy();
        if(index<10){if(!moveItemStackTo(stack,10,46,true))return ItemStack.EMPTY;}
        else {
            if(stack.is(net.minecraft.world.item.Items.JACK_O_LANTERN) && !slots.get(8).hasItem())moveItemStackTo(stack,8,9,false);
            if(!stack.isEmpty())moveItemStackTo(stack,0,8,false);
            if(stack.getCount()==copy.getCount())return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return copy;
    }
}
