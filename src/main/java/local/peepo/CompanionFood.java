package local.peepo;

import java.util.*;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;

/** Only slots 0-7 are cargo. Equipped hands/costumes are never meals or transfer destinations. */
public final class CompanionFood extends SnapshotParticipant<List<ItemStack>> {
    private final PeepoEntity npc;
    private final List<ItemStack> returns=new ArrayList<>();
    private long nextEat;
    CompanionFood(PeepoEntity npc){this.npc=npc;}
    @Override protected List<ItemStack> createSnapshot(){var copy=new ArrayList<ItemStack>(8);for(int i=0;i<8;i++)copy.add(npc.belongings.getItem(i).copy());return copy;}
    @Override protected void readSnapshot(List<ItemStack> snapshot){for(int i=0;i<8;i++)npc.belongings.setItem(i,snapshot.get(i));}
    public int meals(){int count=0;for(int i=0;i<8;i++)if(PeepoEntity.isEdible(npc.belongings.getItem(i)))count+=npc.belongings.getItem(i).getCount();return count;}
    public boolean needsSupplies(){return npc.orders.tamed() && meals()<npc.preferences.carryMeals;}
    public boolean hasReturns(){return !returns.isEmpty();}
    public boolean hasRoom(){for(int i=0;i<8;i++){var s=npc.belongings.getItem(i);if(s.isEmpty() || PeepoEntity.isEdible(s) && s.getCount()<s.getMaxStackSize())return true;}return false;}
    public int store(ItemStack input,TransactionContext tx){
        if(input.isEmpty())return 0;updateSnapshots(tx);int left=input.getCount();
        for(int pass=0;pass<2;pass++)for(int i=0;i<8 && left>0;i++){
            var held=npc.belongings.getItem(i);
            if(pass==0 && !held.isEmpty() && ItemStack.isSameItemSameComponents(held,input)){
                int n=Math.min(left,held.getMaxStackSize()-held.getCount());if(n<=0)continue;
                var copy=held.copy();copy.grow(n);npc.belongings.setItem(i,copy);left-=n;
            }else if(pass==1 && held.isEmpty()){
                int n=Math.min(left,input.getMaxStackSize());npc.belongings.setItem(i,input.copyWithCount(n));left-=n;
            }
        }
        return input.getCount()-left;
    }
    public void tick(){
        long now=npc.level().getGameTime();if(now<nextEat)return;nextEat=now+20;
        if(!npc.orders.tamed() || npc.isEating() || !npc.needsAutomaticFood())return;
        int slot=-1,score=Integer.MIN_VALUE;
        for(int i=0;i<8;i++){int candidate=npc.preferences.foodScore(npc.belongings.getItem(i));if(candidate>score){score=candidate;slot=i;}}
        if(slot<0)return;
        var meal=npc.belongings.removeItem(slot,1);npc.beginLunchMeal(meal,null);
    }
    public ItemStack keepRemainder(ItemStack stack){
        int stored;
        try(var tx=Transaction.openOuter()){stored=store(stack,tx);tx.commit();}
        if(stored>0){
            boolean recorded=false;
            for(var debt:returns)if(ItemStack.isSameItemSameComponents(debt,stack)){debt.setCount(Math.min(64,debt.getCount()+stored));recorded=true;break;}
            if(!recorded && returns.size()<8)returns.add(stack.copyWithCount(stored));
        }
        var rest=stack.copy();rest.shrink(stored);return rest;
    }
    public void returnContainers(LunchBlockEntity source){
        if(!source.feeds(npc))return;var storage=source.source();if(storage==null)return;
        for(var it=returns.iterator();it.hasNext();){
            var debt=it.next();int owed=debt.getCount(),found=0;
            for(int i=0;i<8 && owed>0;i++){
                var stack=npc.belongings.getItem(i);if(!ItemStack.isSameItemSameComponents(stack,debt))continue;
                int amount=Math.min(owed,stack.getCount());found+=amount;
                try(var tx=Transaction.openOuter()){
                    int inserted=(int)storage.insert(ItemVariant.of(stack),amount,tx);
                    if(inserted>0){updateSnapshots(tx);npc.belongings.setItem(i,stack.copyWithCount(stack.getCount()-inserted));tx.commit();owed-=inserted;}
                }
            }
            // A player may have taken the containers out of cargo already.
            if(owed==0 || found==0)it.remove();else debt.setCount(owed);
        }
    }
    public void save(ValueOutput out){out.store("MealContainers",ItemStack.CODEC.listOf(),returns);}
    public void load(ValueInput in){returns.clear();for(var stack:in.read("MealContainers",ItemStack.CODEC.listOf()).orElse(List.of()))if(returns.size()<8 && !stack.isEmpty())returns.add(stack.copyWithCount(Math.clamp(stack.getCount(),1,64)));}
}
