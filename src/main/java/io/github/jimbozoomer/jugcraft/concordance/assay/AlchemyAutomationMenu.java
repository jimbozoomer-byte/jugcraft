package io.github.jimbozoomer.jugcraft.concordance.assay;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Nine ghost filter entries; clicking copies an identity, never a stack. The physical artifact is locked. */
public final class AlchemyAutomationMenu extends AbstractContainerMenu {
    private final Player owner;
    private final ItemStack artifact;
    private final SimpleContainer filters=new SimpleContainer(9);
    private final SimpleContainerData data=new SimpleContainerData(4);
    public AlchemyAutomationMenu(int id,Inventory inv){this(id,inv,ItemStack.EMPTY,-1);}
    public AlchemyAutomationMenu(int id,Inventory inv,ItemStack artifact,int locked){
        super(AlchemyAutomation.MENU,id);owner=inv.player;this.artifact=artifact;data.set(0,locked);
        for(int i=0;i<9;i++)addSlot(new Slot(filters,i,25+(i%3)*18,48+(i/3)*18){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});
        addStandardInventorySlots(inv,25,128);addDataSlots(data);refresh();
    }
    public boolean active(){return data.get(1)!=0;} public int target(){return data.get(2);} public boolean voiding(){return data.get(3)!=0;}
    private void refresh(){
        if(!(owner instanceof ServerPlayer))return;
        var ids=artifact.getOrDefault(AlchemyAutomation.FILTER,List.<String>of());
        for(int i=0;i<9;i++){var key=i<ids.size()?Identifier.tryParse(ids.get(i)):null;filters.setItem(i,key==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.getValue(key)));}
        data.set(1,artifact.has(AlchemicalGear.ACTIVE)?1:0);data.set(2,TARGETS.indexOf(artifact.getOrDefault(AlchemyPatterns.TARGET,"minecraft:diamond")));
        data.set(3,artifact.is(AlchemyStorage.ITEMS.get("void_ring"))?1:0);
    }
    public static final List<String> TARGETS=List.of("minecraft:iron_ingot","minecraft:gold_ingot","minecraft:diamond");
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean clickMenuButton(Player p,int action){
        if(!(p instanceof ServerPlayer server)||!stillValid(p)||action<0||action>2||!RateGate.allow(server,"alchemy_filter",2))return false;
        if(action==0){if(active())artifact.remove(AlchemicalGear.ACTIVE);else artifact.set(AlchemicalGear.ACTIVE,Unit.INSTANCE);}
        if(action==1)artifact.set(AlchemyPatterns.TARGET,TARGETS.get(Math.floorMod(target()+1,3)));
        if(action==2)artifact.remove(AlchemyAutomation.FILTER);
        p.getInventory().setChanged();broadcastChanges();return true;
    }
    @Override public void clicked(int slot,int button,ContainerInput input,Player p){
        if(!stillValid(p))return;
        if(input==ContainerInput.SWAP&&button==data.get(0)||slot>=9&&slot<slots.size()&&slots.get(slot).getContainerSlot()==data.get(0))return;
        if(slot>=0&&slot<9){
            if(p instanceof ServerPlayer&&input==ContainerInput.PICKUP){
                var ids=new ArrayList<>(artifact.getOrDefault(AlchemyAutomation.FILTER,List.<String>of()));
                var carried=getCarried();
                if(carried.isEmpty()){if(slot<ids.size())ids.remove(slot);}
                else if(AlchemicalWorkshopBlockEntity.material(carried)!=null){
                    String id=BuiltInRegistries.ITEM.getKey(carried.getItem()).toString();
                    if(!ids.contains(id)){if(slot<ids.size())ids.set(slot,id);else if(ids.size()<9)ids.add(id);}
                }
                artifact.set(AlchemyAutomation.FILTER,List.copyOf(ids));p.getInventory().setChanged();broadcastChanges();
            }return;
        }
        super.clicked(slot,button,input,p);
    }
    @Override public boolean stillValid(Player p){return p==owner&&(!(p instanceof ServerPlayer s)||AlchemicalGear.ready(s)&&data.get(0)>=0&&p.getInventory().getItem(data.get(0))==artifact);}
    @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
}
