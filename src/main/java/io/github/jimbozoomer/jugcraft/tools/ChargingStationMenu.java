package io.github.jimbozoomer.jugcraft.tools;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Native container synchronization and bounded server-validated module buttons. */
public final class ChargingStationMenu extends AbstractContainerMenu {
    public static final int WIDTH=308, HEIGHT=232, INVENTORY_X=73, INVENTORY_Y=149, HOTBAR_Y=207;
    public static final int MISSING_MODULE=7, NO_ROOM=8;
    private static final ToolUpgrades.Kind[] KINDS=ToolUpgrades.Kind.values();
    private final Container contents;
    private final ContainerData data;
    private final DataSlot status=DataSlot.standalone();

    public ChargingStationMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(2), new SimpleContainerData(2)); }
    public ChargingStationMenu(int id, Inventory inventory, ChargingStationBlockEntity station) {
        this(id, inventory, station, new ContainerData() {
            public int get(int index) { return (int)(station.energy().getAmount() >>> (index*16)) & 65535; }
            public void set(int index,int value) {}
            public int getCount() { return 2; }
        });
    }
    private ChargingStationMenu(int id, Inventory inventory, Container contents, ContainerData data) {
        super(JugcraftTools.CHARGING_STATION_MENU,id); this.contents=contents; this.data=data;
        checkContainerSize(contents,2); checkContainerDataCount(data,2);
        for(int i=0;i<2;i++) addSlot(new Slot(contents,i,11+i*150,30) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof Chargeable c && c.baseCapacity()>0; }
            @Override public int getMaxStackSize() { return 1; }
        });
        for(int row=0;row<3;row++)for(int col=0;col<9;col++) addSlot(new Slot(inventory,9+row*9+col,INVENTORY_X+col*18,INVENTORY_Y+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,INVENTORY_X+col*18,HOTBAR_Y));
        addDataSlots(data); addDataSlot(status);
    }
    /** Native menu data is transmitted as signed 16-bit words. */
    public long energy() { return (data.get(0)&65535L) | ((data.get(1)&65535L)<<16); }
    public int status() { return status.get(); }
    public static int button(int bay, ToolUpgrades.Kind kind, boolean remove) { return (bay*KINDS.length+kind.ordinal())*2+(remove?1:0); }
    @Override public boolean stillValid(Player player) { return !player.isSpectator() && contents.stillValid(player); }
    @Override public void clicked(int slot,int button,ContainerInput type,Player player) { if(stillValid(player))super.clicked(slot,button,type,player); }
    private ItemStack source(Player player,ToolUpgrades.Kind kind) {
        var module=JugcraftTools.MODULES.get(kind);
        if(getCarried().is(module))return getCarried();
        for(int i=0;i<player.getInventory().getContainerSize();i++) if(player.getInventory().getItem(i).is(module))return player.getInventory().getItem(i);
        return ItemStack.EMPTY;
    }
    public boolean hasModule(Player player,ToolUpgrades.Kind kind) { return !source(player,kind).isEmpty(); }
    private boolean canReturn(Player player,ItemStack module) {
        var cursor=getCarried();
        if(cursor.isEmpty() || ItemStack.isSameItemSameComponents(cursor,module)&&cursor.getCount()<cursor.getMaxStackSize())return true;
        // Inventory.add fills the main inventory; empty armor slots are not spare module storage.
        for(int i=0;i<36;i++) {
            var at=player.getInventory().getItem(i);
            if(at.isEmpty() || ItemStack.isSameItemSameComponents(at,module)&&at.getCount()<at.getMaxStackSize())return true;
        }
        return false;
    }
    private void returnModule(Player player,ItemStack module) {
        if(getCarried().isEmpty())setCarried(module);
        else if(ItemStack.isSameItemSameComponents(getCarried(),module)&&getCarried().getCount()<getCarried().getMaxStackSize())getCarried().grow(1);
        else player.getInventory().add(module);
    }
    @Override public boolean clickMenuButton(Player player,int id) {
        if(player.level().isClientSide() || player.containerMenu!=this || !stillValid(player) || id<0 || id>=2*KINDS.length*2)return false;
        int bay=id/(KINDS.length*2); var kind=KINDS[id/2%KINDS.length]; boolean remove=(id&1)!=0;
        var tool=contents.getItem(bay).copy();
        ToolUpgrades.Result result=remove?ToolUpgrades.removeResult(player.level(),tool,kind):ToolUpgrades.fitResult(player.level(),tool,kind);
        if(result!=(remove?ToolUpgrades.Result.REMOVED:ToolUpgrades.Result.FITTED)) { status.set(result.ordinal()+1); broadcastChanges(); return false; }
        if(remove) {
            var module=new ItemStack(JugcraftTools.MODULES.get(kind));
            if(!canReturn(player,module)) { status.set(NO_ROOM); broadcastChanges(); return false; }
            ToolUpgrades.remove(player.level(),tool,kind); returnModule(player,module);
        } else {
            var module=source(player,kind);
            if(module.isEmpty()) { status.set(MISSING_MODULE); broadcastChanges(); return false; }
            ToolUpgrades.fit(player.level(),tool,kind); module.consume(1,player);
        }
        contents.setItem(bay,tool); player.getInventory().setChanged(); status.set(result.ordinal()+1); broadcastChanges(); return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index); if(!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem(); var original=stack.copy();
        if(index<2) { if(!moveItemStackTo(stack,2,38,true))return ItemStack.EMPTY; }
        else if(!slots.get(0).mayPlace(stack)||!moveItemStackTo(stack,0,2,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==original.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack); return original;
    }
}
