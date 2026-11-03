package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.*;
import java.util.function.*;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Transactional access to the actual saved farm-store list; visual updates happen only after commit. */
public final class FarmStoreStorage extends SnapshotParticipant<List<ItemStack>> implements Storage<ItemVariant> {
    private final BlockEntity owner;
    private final List<ItemStack> stacks;
    private final IntSupplier capacity;
    private final Predicate<ItemStack> accepts;
    private final Runnable changed;
    private final boolean single;
    public FarmStoreStorage(BlockEntity owner,List<ItemStack> stacks,IntSupplier capacity,Predicate<ItemStack> accepts,Runnable changed,boolean single){
        this.owner=owner;this.stacks=stacks;this.capacity=capacity;this.accepts=accepts;this.changed=changed;this.single=single;
    }
    /** Never load a neighboring chunk while updating the visible parts of a large store. */
    public static boolean complete(BlockEntity owner){
        var level=owner.getLevel();var pos=owner.getBlockPos();
        if(owner.isRemoved() || level==null || level.isClientSide() || !level.hasChunkAt(pos) || level.getBlockEntity(pos)!=owner)return false;
        var state=owner.getBlockState();
        if(state.getBlock() instanceof FruitBinBlock bin){
            if(bin.part(state)!=MultiDecorationBlock.MASTER)return false;
            var facing=state.getValue(MultiDecorationBlock.FACING);
            for(int i=0;i<bin.cells().length;i++){
                BlockPos at=bin.partPos(pos,facing,i);if(!level.hasChunkAt(at))return false;
                var there=level.getBlockState(at);
                if(!there.is(bin) || bin.part(there)!=i || there.getValue(MultiDecorationBlock.FACING)!=facing)return false;
            }
        }
        return true;
    }
    protected List<ItemStack> createSnapshot(){return stacks.stream().map(ItemStack::copy).toList();}
    protected void readSnapshot(List<ItemStack> before){stacks.clear();before.forEach(s->stacks.add(s.copy()));}
    protected void onFinalCommit(){changed.run();}
    private int count(){return stacks.stream().mapToInt(ItemStack::getCount).sum();}
    private long count(ItemVariant item){return complete(owner)?stacks.stream().filter(item::matches).mapToLong(ItemStack::getCount).sum():0;}
    public long insert(ItemVariant item,long amount,TransactionContext tx){
        StoragePreconditions.notBlankNotNegative(item,amount);
        if(!complete(owner) || !accepts.test(item.toStack(1)))return 0;
        int n=(int)Math.min(amount,Math.max(0,capacity.getAsInt()-count()));if(n==0)return 0;
        updateSnapshots(tx);var remaining=item.toStack(n);
        if(!single && !stacks.isEmpty() && item.matches(stacks.getLast())){
            var last=stacks.getLast();int moved=Math.min(remaining.getCount(),Math.max(0,last.getMaxStackSize()-last.getCount()));last.grow(moved);remaining.shrink(moved);
        }
        while(!remaining.isEmpty())stacks.add(remaining.split(single?1:remaining.getMaxStackSize()));
        return n;
    }
    public long extract(ItemVariant item,long amount,TransactionContext tx){
        StoragePreconditions.notBlankNotNegative(item,amount);if(!complete(owner) || amount==0)return 0;
        long n=Math.min(amount,count(item));if(n==0)return 0;updateSnapshots(tx);long remaining=n;
        for(int i=stacks.size()-1;i>=0 && remaining>0;i--){var stack=stacks.get(i);if(!item.matches(stack))continue;
            int moved=(int)Math.min(remaining,stack.getCount());stack.shrink(moved);remaining-=moved;if(stack.isEmpty())stacks.remove(i);
        }
        return n;
    }
    public Iterator<StorageView<ItemVariant>> iterator(){
        if(!complete(owner))return Collections.emptyIterator();
        var kinds=new LinkedHashSet<ItemVariant>();for(var stack:stacks)if(!stack.isEmpty())kinds.add(ItemVariant.of(stack));
        return kinds.stream().<StorageView<ItemVariant>>map(item->new StorageView<>(){
            public boolean isResourceBlank(){return getAmount()==0;}
            public ItemVariant getResource(){return item;}
            public long getAmount(){return count(item);}
            public long getCapacity(){return capacity.getAsInt();}
            public long extract(ItemVariant wanted,long amount,TransactionContext tx){StoragePreconditions.notBlankNotNegative(wanted,amount);return item.equals(wanted)?FarmStoreStorage.this.extract(wanted,amount,tx):0;}
        }).iterator();
    }
}
