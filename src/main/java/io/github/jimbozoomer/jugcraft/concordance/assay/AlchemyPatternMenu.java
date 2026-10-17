package io.github.jimbozoomer.jugcraft.concordance.assay;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Assay;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Two real transaction slots and 24 read-only, server-synchronized learned patterns. */
public final class AlchemyPatternMenu extends AbstractContainerMenu {
    public static final int GHOST_START=38, PAGE_SIZE=24;
    private final Player owner;
    private final BooleanSupplier access;
    private final ItemStack eye;
    private final SimpleContainer real=new SimpleContainer(2),ghost=new SimpleContainer(PAGE_SIZE);
    private final SimpleContainerData data=new SimpleContainerData(10);
    private String query="",selected="";
    private int page;
    private boolean favorites;
    private List<String> cachedIds=List.of(),lastKnown=List.of(),lastFavorites=List.of();
    private Object lastCatalogue;
    private String lastQuery;
    private boolean lastFilter;
    private int lastPage=-1;
    public AlchemyPatternMenu(int id,Inventory inv){this(id,inv,()->true,ItemStack.EMPTY);}
    public AlchemyPatternMenu(int id,Inventory inv,BooleanSupplier access,ItemStack eye){
        super(AlchemyPatterns.MENU,id);owner=inv.player;this.access=access;this.eye=eye;
        if(!eye.isEmpty())selected=eye.getOrDefault(AlchemyPatterns.TARGET,"");
        addSlot(new Slot(real,0,23,63){@Override public int getMaxStackSize(){return 1;}});
        addSlot(new Slot(real,1,84,63){@Override public boolean mayPlace(ItemStack s){return false;}});
        addStandardInventorySlots(inv,85,151);
        for(int i=0;i<PAGE_SIZE;i++)addSlot(new Slot(ghost,i,181+(i%6)*22,52+(i/6)*20){
            @Override public boolean mayPlace(ItemStack s){return false;}
            @Override public boolean mayPickup(Player p){return false;}
        });
        addDataSlots(data);refresh();
    }
    public void search(String s){query=s.substring(0,Math.min(48,s.length())).toLowerCase(Locale.ROOT);page=0;broadcastChanges();}
    private ItemStack target(){var id=Identifier.tryParse(selected);return id==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.getValue(id));}
    public int page(){return data.get(0);} public int pages(){return Math.max(1,data.get(1));}
    public boolean eye(){return data.get(2)!=0;} public boolean favorites(){return data.get(3)!=0;}
    public long price(){return (data.get(4)&65535L)|((data.get(5)&65535L)<<16);}
    public long balance(){return (data.get(6)&65535L)|((data.get(7)&65535L)<<16);}
    public boolean canMake(){return (data.get(8)&1)!=0;} public boolean isFavorite(){return (data.get(8)&2)!=0;}
    public ItemStack selection(){return new ItemStack(BuiltInRegistries.ITEM.byId(data.get(9)));}
    private boolean allowed(ItemStack stack){var m=AlchemicalWorkshopBlockEntity.material(stack);return m!=null&&m.formable()&&(eye.isEmpty()||AlchemyPatterns.building(stack));}
    private void refresh(){
        if(!(owner instanceof ServerPlayer p))return;
        var known=AlchemyPatterns.known(p);var starred=AlchemyPatterns.favorites(p);var catalogue=Assaying.catalog();
        boolean rebuild=known!=lastKnown||starred!=lastFavorites||catalogue!=lastCatalogue||!query.equals(lastQuery)||favorites!=lastFilter;
        if(rebuild){cachedIds=known.stream().filter(id->{
            var key=Identifier.tryParse(id);if(key==null)return false;
            var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(key));
            return allowed(stack)&&(!favorites||AlchemyPatterns.favorites(p).contains(id))
                    &&(id.contains(query)||stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query));
        }).sorted().toList();lastKnown=known;lastFavorites=starred;lastCatalogue=catalogue;lastQuery=query;lastFilter=favorites;}
        var ids=cachedIds;
        int pages=Math.max(1,(ids.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.clamp(page,0,pages-1);
        if(rebuild||lastPage!=page)for(int i=0;i<PAGE_SIZE;i++)ghost.setItem(i,page*PAGE_SIZE+i<ids.size()?new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(ids.get(page*PAGE_SIZE+i)))):ItemStack.EMPTY);
        lastPage=page;
        var stack=target();var m=AlchemicalWorkshopBlockEntity.material(stack);
        long cost=m!=null&&allowed(stack)?Assay.form(m,1):0;
        var out=real.getItem(1);
        boolean valid=stillValid(p)&&AlchemyPatterns.known(p).contains(selected)&&cost>0;
        boolean fits=out.isEmpty()||ItemStack.isSameItemSameComponents(out,stack)&&out.getCount()<out.getMaxStackSize();
        data.set(0,page);data.set(1,pages);data.set(2,eye.isEmpty()?0:1);data.set(3,favorites?1:0);
        data.set(4,(int)cost&65535);data.set(5,(int)(cost>>>16));long balance=Assaying.balance(p);
        data.set(6,(int)balance&65535);data.set(7,(int)(balance>>>16));
        data.set(8,(valid&&(!eye.isEmpty()||balance>=cost&&fits)?1:0)|(AlchemyPatterns.favorites(p).contains(selected)?2:0));
        data.set(9,BuiltInRegistries.ITEM.getId(stack.getItem()));
    }
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public boolean clickMenuButton(Player player,int action){
        if(player!=owner||!(player instanceof ServerPlayer p)||!stillValid(p)||action<0||action>5||!RateGate.allow(p,"alchemy_patterns",2))return false;
        refresh();
        switch(action){
            case 0->{if(AlchemyPatterns.learn(p,real.getItem(0))){selected=BuiltInRegistries.ITEM.getKey(real.getItem(0).getItem()).toString();}}
            case 1->{if(canMake()){
                if(!eye.isEmpty()){eye.set(AlchemyPatterns.TARGET,selected);p.getInventory().setChanged();}
                else {Assaying.setBalance(p,Assaying.balance(p)-price());var out=real.getItem(1);if(out.isEmpty())real.setItem(1,target());else out.grow(1);}
            }}
            case 2->page--;
            case 3->page++;
            case 4->{AlchemyPatterns.favorite(p,selected);}
            case 5->{favorites=!favorites;page=0;}
        }
        broadcastChanges();return true;
    }
    @Override public void clicked(int slot,int button,ContainerInput input,Player player){
        if(!stillValid(player))return;
        if(slot>=GHOST_START&&slot<GHOST_START+PAGE_SIZE){
            if(player instanceof ServerPlayer p&&input==ContainerInput.PICKUP&&RateGate.allow(p,"alchemy_pattern_select",1)){
                var s=ghost.getItem(slot-GHOST_START);if(!s.isEmpty()){selected=BuiltInRegistries.ITEM.getKey(s.getItem()).toString();broadcastChanges();}
            }return;
        }
        super.clicked(slot,button,input,player);
    }
    @Override public boolean stillValid(Player p){return p==owner&&(!(p instanceof ServerPlayer s)||AlchemicalGear.ready(s)&&access.getAsBoolean());}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(!stillValid(p)||index<0||index>=GHOST_START)return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();
        if(index<2){if(!moveItemStackTo(s,2,GHOST_START,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;
        if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;
    }
    @Override public void removed(Player p){super.removed(p);if(p instanceof ServerPlayer)clearContainer(p,real);}
}
