package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.*;
import local.peepo.*;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Passive native drying, with transactional hanging and dried-only collection. No resident worker or ticker. */
public final class DryingRackCompanion extends SnapshotParticipant<DryingRackCompanion.Snapshot> {
    private final DryingRackBlockEntity rack;
    public final CompanionRecipeFilter filter=new CompanionRecipeFilter();
    public DryingRackCompanion(DryingRackBlockEntity rack){this.rack=rack;}
    public static DryingRackCompanion of(BlockEntity block){return block instanceof DryingRackBlockEntity rack?rack.companion:null;}
    record Snapshot(List<ItemStack> bunches,long[] hung){}
    protected Snapshot createSnapshot(){
        var bunches=new ArrayList<ItemStack>(DryingRackBlock.SLOTS);var hung=new long[DryingRackBlock.SLOTS];
        for(int i=0;i<DryingRackBlock.SLOTS;i++){bunches.add(rack.bunch(i).copy());hung[i]=rack.hungAt(i);}return new Snapshot(bunches,hung);
    }
    protected void readSnapshot(Snapshot before){for(int i=0;i<DryingRackBlock.SLOTS;i++)rack.restoreBunch(i,before.bunches.get(i).copy(),before.hung[i]);}
    protected void onFinalCommit(){
        rack.setChanged();CompanionReadiness.invalidate(rack);
        if(!complete())return;
        var level=rack.getLevel();var block=(DryingRackBlock)rack.getBlockState().getBlock();block.show(level,rack.getBlockPos());
        long next=rack.untilNextDry(level.getGameTime());if(next>0)level.scheduleTick(rack.getBlockPos(),block,(int)Math.min(next,Integer.MAX_VALUE));
    }
    public boolean complete(){
        var level=rack.getLevel();var pos=rack.getBlockPos();
        if(level==null || level.isClientSide() || rack.isRemoved() || !level.hasChunkAt(pos) || level.getBlockEntity(pos)!=rack)return false;
        var state=rack.getBlockState();if(!(state.getBlock() instanceof DryingRackBlock block) || block.part(state)!=MultiDecorationBlock.MASTER)return false;
        var facing=state.getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++){
            var at=block.partPos(pos,facing,i);if(!level.hasChunkAt(at))return false;var there=level.getBlockState(at);
            if(!there.is(block) || block.part(there)!=i || there.getValue(MultiDecorationBlock.FACING)!=facing)return false;
        }return true;
    }
    public boolean allowed(PeepoEntity npc){
        if(npc.level()!=rack.getLevel() || !npc.orders.tamed() || !complete())return false;
        var block=(DryingRackBlock)rack.getBlockState().getBlock();var facing=rack.getBlockState().getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++)if(!CompanionJobs.permitted(npc,block.partPos(rack.getBlockPos(),facing,i)))return false;
        return true;
    }
    private boolean active(PeepoEntity npc){return npc.assignments.assignedWork(rack.getBlockPos()) && allowed(npc);}
    public boolean acceptsFilter(ItemStack stack){return stack.isEmpty() || stack.is(JugcraftAgriculture.item("dried_herbs")) || stack.is(JugcraftAgriculture.item("dried_chili"));}
    public ItemStack result(ItemStack raw){return DryingRackBlock.hangs(raw)?DryingRackBlock.driedForm(raw.copyWithCount(1)):ItemStack.EMPTY;}
    public boolean busy(){if(!complete())return false;for(int i=0;i<DryingRackBlock.SLOTS;i++)if(!rack.bunch(i).isEmpty())return true;return false;}
    public int pending(ItemStack icon,List<ItemStack> incoming){
        int n=0;long now=rack.getLevel()==null?0:rack.getLevel().getGameTime();
        for(int i=0;i<DryingRackBlock.SLOTS;i++)if(!rack.bunch(i).isEmpty() && !rack.dried(i,now) && filter.stock.matches(icon,result(rack.bunch(i))))n+=rack.bunch(i).getCount();
        for(var raw:incoming){var out=result(raw);if(!out.isEmpty() && filter.stock.matches(icon,out))n+=raw.getCount();}return n;
    }
    private int needed(ItemStack raw){
        var out=result(raw);if(!complete() || out.isEmpty() || !filter.allows(out))return 0;
        int free=0;for(int i=0;i<DryingRackBlock.SLOTS;i++)if(rack.bunch(i).isEmpty())free++;return free*DryingRackBlock.BUNCH;
    }
    private final Storage<ItemVariant> input=new InsertionOnlyStorage<>(){
        public long insert(ItemVariant item,long amount,TransactionContext tx){
            StoragePreconditions.notBlankNotNegative(item,amount);int n=(int)Math.min(amount,needed(item.toStack(1)));if(n<=0)return 0;
            updateSnapshots(tx);int left=n;long now=rack.getLevel().getGameTime();
            for(int i=0;i<DryingRackBlock.SLOTS && left>0;i++)if(rack.bunch(i).isEmpty()){int add=Math.min(left,DryingRackBlock.BUNCH);rack.restoreBunch(i,item.toStack(add),now);left-=add;}
            return n-left;
        }
    };
    private long driedAmount(ItemVariant item){
        if(!complete())return 0;long n=0,now=rack.getLevel().getGameTime();
        for(int i=0;i<DryingRackBlock.SLOTS;i++)if(rack.dried(i,now) && item.matches(DryingRackBlock.driedForm(rack.bunch(i))))n+=rack.bunch(i).getCount();return n;
    }
    private final Storage<ItemVariant> output=new Storage<>(){
        public boolean supportsInsertion(){return false;}
        public long insert(ItemVariant item,long amount,TransactionContext tx){StoragePreconditions.notBlankNotNegative(item,amount);return 0;}
        public long extract(ItemVariant item,long amount,TransactionContext tx){
            StoragePreconditions.notBlankNotNegative(item,amount);long n=Math.min(amount,driedAmount(item));if(n<=0)return 0;
            updateSnapshots(tx);long left=n,now=rack.getLevel().getGameTime();
            for(int i=0;i<DryingRackBlock.SLOTS && left>0;i++)if(rack.dried(i,now) && item.matches(DryingRackBlock.driedForm(rack.bunch(i)))){
                var bunch=rack.bunch(i);int removed=(int)Math.min(left,bunch.getCount());rack.restoreBunch(i,bunch.copyWithCount(bunch.getCount()-removed),rack.hungAt(i));left-=removed;
            }return n-left;
        }
        public Iterator<StorageView<ItemVariant>> iterator(){
            if(!complete())return Collections.emptyIterator();var kinds=new LinkedHashSet<ItemVariant>();long now=rack.getLevel().getGameTime();
            for(int i=0;i<DryingRackBlock.SLOTS;i++)if(rack.dried(i,now))kinds.add(ItemVariant.of(DryingRackBlock.driedForm(rack.bunch(i))));
            return kinds.stream().<StorageView<ItemVariant>>map(item->new StorageView<>(){
                public boolean isResourceBlank(){return getAmount()==0;}public ItemVariant getResource(){return item;}
                public long getAmount(){return driedAmount(item);}public long getCapacity(){return DryingRackBlock.SLOTS*DryingRackBlock.BUNCH;}
                public long extract(ItemVariant wanted,long amount,TransactionContext tx){StoragePreconditions.notBlankNotNegative(wanted,amount);return item.equals(wanted)?output.extract(wanted,amount,tx):0;}
            }).iterator();
        }
    };
    public Storage<ItemVariant> outputs(){return output;}
    private Storage<ItemVariant> guarded(PeepoEntity npc,Storage<ItemVariant> storage){return new Storage<>(){
        public boolean supportsInsertion(){return storage.supportsInsertion();}public boolean supportsExtraction(){return storage.supportsExtraction();}
        public long insert(ItemVariant item,long amount,TransactionContext tx){return active(npc)?storage.insert(item,amount,tx):0;}
        public long extract(ItemVariant item,long amount,TransactionContext tx){return active(npc)?storage.extract(item,amount,tx):0;}
        public Iterator<StorageView<ItemVariant>> iterator(){
            if(!active(npc))return Collections.emptyIterator();var views=new ArrayList<StorageView<ItemVariant>>();
            for(var v:storage)views.add(new StorageView<>(){
                public boolean isResourceBlank(){return getAmount()==0;}public ItemVariant getResource(){return v.getResource();}
                public long getAmount(){return active(npc)?v.getAmount():0;}public long getCapacity(){return v.getCapacity();}
                public long extract(ItemVariant item,long amount,TransactionContext tx){return active(npc)?v.extract(item,amount,tx):0;}
            });return views.iterator();
        }
    };}
    public CompanionLogistics.Port port(PeepoEntity npc){return new CompanionLogistics.Port(){
        private final Storage<ItemVariant> in=guarded(npc,input),out=guarded(npc,output);
        public Identifier plan(){return active(npc)?Identifier.fromNamespaceAndPath("jugcraft","drying_rack"):null;}
        public int needed(ItemStack raw){return active(npc)?DryingRackCompanion.this.needed(raw):0;}
        public Storage<ItemVariant> inputs(){return in;}public Storage<ItemVariant> outputs(){return out;}
        public CompanionStatus status(){
            if(!active(npc))return CompanionStatus.FORBIDDEN;
            long now=rack.getLevel().getGameTime();for(int i=0;i<DryingRackBlock.SLOTS;i++)if(rack.dried(i,now))return CompanionStatus.FULL;
            return busy()?CompanionStatus.DRYING:CompanionStatus.NO_INPUT;
        }
    };}
}
