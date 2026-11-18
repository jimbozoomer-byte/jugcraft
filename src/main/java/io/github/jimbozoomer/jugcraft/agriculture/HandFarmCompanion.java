package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.*;
import local.peepo.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.*;
import net.minecraft.util.Prediction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;

/** Native hand-worked recipes, existing companion logistics and a saved, bounded output tray. */
public final class HandFarmCompanion extends SnapshotParticipant<HandFarmCompanion.InputState> {
    public static final int OUTPUT_CAPACITY=64, ENERGY_PER_TICK=4, CHURN_BATCH_CAPACITY=8;
    private final HandWorkedBlockEntity station;
    public final CompanionRecipeFilter filter=new CompanionRecipeFilter();
    public final HandFarmJob job;
    private final List<ItemStack> products=new ArrayList<>();
    /** Unpoured, real milk buckets; the native churn still processes one load at a time. */
    private final List<ItemStack> queuedMilk=new ArrayList<>();
    private final FarmStoreStorage buffer;
    private long lastTick=Long.MIN_VALUE;
    public HandFarmCompanion(HandWorkedBlockEntity station){
        this.station=station;job=new HandFarmJob(station,this);
        buffer=new FarmStoreStorage(station,products,()->OUTPUT_CAPACITY,s->true,this::changed,false);
    }
    public static HandFarmCompanion of(BlockEntity block){return block instanceof QuernBlockEntity q?q.companion:block instanceof ButterChurnBlockEntity c?c.companion:null;}
    record InputState(ItemStack grain,int progress,boolean milk,long stroked,List<ItemStack> queue){}
    protected InputState createSnapshot(){return station instanceof QuernBlockEntity q?new InputState(q.grain().copy(),q.turns(),false,station.strokeTime(),List.of()):new InputState(ItemStack.EMPTY,((ButterChurnBlockEntity)station).strokes(),((ButterChurnBlockEntity)station).milk(),station.strokeTime(),queuedMilk.stream().map(ItemStack::copy).toList());}
    protected void readSnapshot(InputState state){
        if(station instanceof QuernBlockEntity q)q.restoreInput(state.grain(),state.progress());else ((ButterChurnBlockEntity)station).restoreInput(state.milk(),state.progress());
        station.restoreStroke(state.stroked());
        queuedMilk.clear();state.queue().forEach(s->queuedMilk.add(s.copy()));
    }
    protected void onFinalCommit(){changed();}
    private void changed(){
        if(FarmStoreStorage.complete(station)){
            var state=station.getBlockState();var next=station instanceof QuernBlockEntity q?state.setValue(QuernBlock.LOADED,!q.grain().isEmpty()):state.setValue(ButterChurnBlock.FULL,((ButterChurnBlockEntity)station).milk());
            if(next!=state)station.getLevel().setBlock(station.getBlockPos(),next,Block.UPDATE_CLIENTS);
        }
        station.changed();
    }
    public boolean acceptsFilter(ItemStack stack){return stack.isEmpty() || station instanceof QuernBlockEntity && (stack.is(JugcraftAgriculture.item("flour")) || stack.is(JugcraftAgriculture.item("paprika"))) || station instanceof ButterChurnBlockEntity && stack.is(JugcraftAgriculture.item("butter"));}
    public ItemStack result(ItemStack raw){return station instanceof QuernBlockEntity?QuernBlock.ground(raw):raw.is(Items.MILK_BUCKET)?new ItemStack(JugcraftAgriculture.item("butter"),ButterChurnBlock.BUTTER):ItemStack.EMPTY;}
    public int queuedMilkCount(){return queuedMilk.size();}
    private int milkLoads(){return queuedMilk.size()+(((ButterChurnBlockEntity)station).milk()?1:0);}
    public boolean busy(){return station instanceof QuernBlockEntity q?!q.grain().isEmpty():milkLoads()>0;}
    public boolean acceptsSupplyBatch(){return !busy() && products.isEmpty() && !filter.stock.hasIncoming(station);}
    public ItemStack activeProduct(){return station instanceof QuernBlockEntity q?result(q.grain()):busy()?result(new ItemStack(Items.MILK_BUCKET)):ItemStack.EMPTY;}
    private int room(){return Math.max(0,OUTPUT_CAPACITY-products.stream().mapToInt(ItemStack::getCount).sum());}
    public int pending(ItemStack icon,List<ItemStack> incoming){
        int count=0;var active=activeProduct();int batches=station instanceof QuernBlockEntity q?q.grain().getCount():milkLoads();
        if(!active.isEmpty() && filter.stock.matches(icon,active))count+=batches*active.getCount();
        for(var raw:incoming){var out=result(raw);if(!out.isEmpty() && filter.stock.matches(icon,out))count+=raw.getCount()*out.getCount();}return count;
    }
    public int needed(ItemStack raw){
        var out=result(raw);if(!FarmStoreStorage.complete(station) || out.isEmpty() || !filter.allows(out))return 0;
        if(station instanceof QuernBlockEntity q){
            if(!q.grain().isEmpty() && !ItemStack.isSameItemSameComponents(q.grain(),raw))return 0;
            return Math.max(0,Math.min(QuernBlock.CAPACITY-q.grain().getCount(),room()/out.getCount()-q.grain().getCount()));
        }
        int reserved=queuedMilk.size()*(ButterChurnBlock.BUTTER+1)+(((ButterChurnBlockEntity)station).milk()?ButterChurnBlock.BUTTER:0);
        return Math.max(0,Math.min(CHURN_BATCH_CAPACITY-milkLoads(),(room()-reserved)/(ButterChurnBlock.BUTTER+1)));
    }
    private final Storage<ItemVariant> input=new InsertionOnlyStorage<>(){
        public long insert(ItemVariant item,long amount,TransactionContext tx){
            StoragePreconditions.notBlankNotNegative(item,amount);int n=(int)Math.min(amount,needed(item.toStack(1)));if(n<=0)return 0;
            updateSnapshots(tx);
            if(station instanceof QuernBlockEntity q)q.restoreInput(item.toStack(q.grain().getCount()+n),q.turns());
            else{
                // Insertions and their bucket remainders roll back together, including nested pickup probes.
                try(var nested=Transaction.openNested(tx)){
                    updateSnapshots(nested);
                    for(int i=0;i<n;i++)queuedMilk.add(item.toStack(1));
                    if(!pourQueuedMilk(nested))return 0;
                    nested.commit();
                }
            }
            return n;
        }
    };
    private final Storage<ItemVariant> output=new Storage<>(){
        public boolean supportsInsertion(){return false;}
        public long insert(ItemVariant item,long amount,TransactionContext tx){StoragePreconditions.notBlankNotNegative(item,amount);return 0;}
        public long extract(ItemVariant item,long amount,TransactionContext tx){return buffer.extract(item,amount,tx);}
        public Iterator<StorageView<ItemVariant>> iterator(){return buffer.iterator();}
    };
    public Storage<ItemVariant> outputs(){return output;}
    private boolean allowed(PeepoEntity npc){return npc.level()==station.getLevel() && npc.orders.tamed() && npc.assignments.assignedWork(station.getBlockPos()) && CompanionJobs.permitted(npc,station.getBlockPos()) && FarmStoreStorage.complete(station);}
    private Storage<ItemVariant> guarded(PeepoEntity npc,Storage<ItemVariant> delegate){return new Storage<>(){
        public boolean supportsInsertion(){return delegate.supportsInsertion();}
        public boolean supportsExtraction(){return delegate.supportsExtraction();}
        public long insert(ItemVariant item,long amount,TransactionContext tx){return allowed(npc)?delegate.insert(item,amount,tx):0;}
        public long extract(ItemVariant item,long amount,TransactionContext tx){return allowed(npc)?delegate.extract(item,amount,tx):0;}
        public Iterator<StorageView<ItemVariant>> iterator(){
            if(!allowed(npc))return Collections.emptyIterator();
            var views=new ArrayList<StorageView<ItemVariant>>();for(var view:delegate)views.add(new StorageView<>(){
                public boolean isResourceBlank(){return getAmount()==0;}
                public ItemVariant getResource(){return view.getResource();}
                public long getAmount(){return allowed(npc)?view.getAmount():0;}
                public long getCapacity(){return view.getCapacity();}
                public long extract(ItemVariant item,long amount,TransactionContext tx){return allowed(npc)?view.extract(item,amount,tx):0;}
            });return views.iterator();
        }
    };}
    public CompanionLogistics.Port port(PeepoEntity npc){return new CompanionLogistics.Port(){
        private final Storage<ItemVariant> in=guarded(npc,input),out=guarded(npc,output);
        public Identifier plan(){return allowed(npc)?Identifier.fromNamespaceAndPath("jugcraft",station instanceof QuernBlockEntity?"quern":"butter_churn"):null;}
        public boolean acceptsSupplyBatch(){return allowed(npc) && HandFarmCompanion.this.acceptsSupplyBatch();}
        public int needed(ItemStack stack){return allowed(npc)?HandFarmCompanion.this.needed(stack):0;}
        public int pickupNeeded(ItemStack stack){return acceptsSupplyBatch()?needed(stack):0;}
        public List<ItemStack> supplyBatch(Storage<ItemVariant> source,int slots){return acceptsSupplyBatch()?CompanionLogistics.Port.super.supplyBatch(source,slots):List.of();}
        public Storage<ItemVariant> inputs(){return in;}
        public Storage<ItemVariant> outputs(){return out;}
        public CompanionStatus status(){return allowed(npc)?HandFarmCompanion.this.status():CompanionStatus.FORBIDDEN;}
    };}
    public CompanionStatus status(){
        if(!FarmStoreStorage.complete(station))return CompanionStatus.MISSING;
        if(!busy())return products.isEmpty()?CompanionStatus.NO_INPUT:CompanionStatus.FULL;
        var product=activeProduct();if(product.isEmpty())return CompanionStatus.RECIPE_MISSING;
        int immediate=product.getCount()+(station instanceof ButterChurnBlockEntity c && !c.milk()?1:0);
        return room()<immediate?CompanionStatus.FULL:CompanionStatus.READY;
    }
    /** A real queued bucket is poured only when the native milk slot is empty. */
    private boolean pourQueuedMilk(TransactionContext tx){
        if(!(station instanceof ButterChurnBlockEntity churn) || churn.milk() || queuedMilk.isEmpty())return true;
        if(buffer.insert(ItemVariant.of(Items.BUCKET),1,tx)!=1)return false;
        updateSnapshots(tx);queuedMilk.removeFirst();churn.restoreInput(true,0);return true;
    }
    public CompanionStatus assist(PeepoEntity npc){
        if(!allowed(npc) || !job.isOccupant(npc))return CompanionStatus.FORBIDDEN;
        var status=status();if(status!=CompanionStatus.READY)return status;
        long now=station.getLevel().getGameTime();if(now==lastTick)return CompanionStatus.WORKING;
        boolean stroke=station.ready(now);ItemStack made=ItemStack.EMPTY;
        try(var tx=Transaction.openOuter()){
            if(npc.extractEnergy(ENERGY_PER_TICK,tx)!=ENERGY_PER_TICK)return CompanionStatus.RECOVERING;
            if(stroke){
                updateSnapshots(tx);if(!pourQueuedMilk(tx))return CompanionStatus.FULL;
                made=station instanceof QuernBlockEntity q?q.advance(now):((ButterChurnBlockEntity)station).advance(now);
                if(!made.isEmpty() && buffer.insert(ItemVariant.of(made),made.getCount(),tx)!=made.getCount())return CompanionStatus.FULL;
                if(!made.isEmpty())npc.feedback.finished(CompanionFeedbackRules.Kind.COOK,made.getItem(),tx);
            }
            tx.commit();
        }
        lastTick=now;
        if(stroke)station.getLevel().playSound(null,station.getBlockPos(),station instanceof QuernBlockEntity?SoundEvents.GRINDSTONE_USE:SoundEvents.GENERIC_SPLASH,SoundSource.BLOCKS,.4F,1);
        return CompanionStatus.WORKING;
    }
    public boolean collect(Player player){
        if(products.isEmpty() && queuedMilk.isEmpty())return false;
        for(var stack:products)player.getInventory().placeItemBackInInventory(stack.copy(),Prediction.SERVER_ONLY);
        for(var stack:queuedMilk)player.getInventory().placeItemBackInInventory(stack.copy(),Prediction.SERVER_ONLY);
        products.clear();queuedMilk.clear();changed();return true;
    }
    public void drop(){
        if(station.getLevel()!=null && !station.getLevel().isClientSide()){
            BlockPos pos=station.getBlockPos();
            for(var stack:products)Containers.dropItemStack(station.getLevel(),pos.getX(),pos.getY(),pos.getZ(),stack);
            for(var stack:queuedMilk)Containers.dropItemStack(station.getLevel(),pos.getX(),pos.getY(),pos.getZ(),stack);
        }
        products.clear();queuedMilk.clear();job.removed();
    }
    public void save(ValueOutput out){filter.save(out);out.store("CompanionProducts",ItemStack.CODEC.listOf(),List.copyOf(products));out.store("CompanionMilkQueue",ItemStack.CODEC.listOf(),List.copyOf(queuedMilk));}
    public void load(ValueInput in){
        filter.load(in);products.clear();int room=OUTPUT_CAPACITY;
        for(var stack:in.read("CompanionProducts",ItemStack.CODEC.listOf()).orElse(List.of()))if(!stack.isEmpty() && room>0){int n=Math.min(room,stack.getCount());products.add(stack.copyWithCount(n));room-=n;}
        queuedMilk.clear();if(station instanceof ButterChurnBlockEntity)for(var stack:in.read("CompanionMilkQueue",ItemStack.CODEC.listOf()).orElse(List.of())){
            if(!stack.is(Items.MILK_BUCKET))continue;
            int n=Math.min(CHURN_BATCH_CAPACITY-1-queuedMilk.size(),stack.getCount());for(int i=0;i<n;i++)queuedMilk.add(stack.copyWithCount(1));
        }
        lastTick=Long.MIN_VALUE;
    }
}
