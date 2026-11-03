package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.*;
import java.util.function.*;
import local.peepo.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Native confection recipes through physical deliveries; no ticker, fake player or accelerated cooling. */
public final class ConfectionCompanion extends SnapshotParticipant<ConfectionCompanion.Snapshot> {
    public static final int OUTPUT_CAPACITY=64;
    private final BlockEntity station;
    public final CompanionRecipeFilter filter=new CompanionRecipeFilter();
    private final List<ItemStack> products=new ArrayList<>();
    private CaramelAppleStandBlockEntity.Stock applePending;
    private BlockState statePending;
    private boolean caramelMode;
    private int batchSugar,batchPaper;
    public ConfectionCompanion(BlockEntity station){this.station=station;}
    public static ConfectionCompanion of(BlockEntity b){return b instanceof CaramelAppleStandBlockEntity s?s.companion:b instanceof DecorationBlockEntity d?d.confection:null;}
    private boolean apple(){return station instanceof CaramelAppleStandBlockEntity;}
    private boolean cotton(){return station.getBlockState().getBlock() instanceof CottonCandyCartBlock;}
    private static ItemStack item(String id){return new ItemStack(JugcraftAgriculture.item(id));}
    private long now(){return station.getLevel()==null?0:station.getLevel().getGameTime();}
    private BlockState state(){return statePending==null?station.getBlockState():statePending;}
    private CaramelAppleStandBlockEntity.Stock apples(){return applePending==null?((CaramelAppleStandBlockEntity)station).companionStock():applePending;}
    private static CaramelAppleStandBlockEntity.Stock copy(CaramelAppleStandBlockEntity.Stock value){return value==null?null:new CaramelAppleStandBlockEntity.Stock(value.caramel(),value.sticks(),value.dipped().clone());}
    private int appleCount(boolean ready){int n=0;for(long time:apples().dipped())if(time>=0 && (!ready || now()-time>=CaramelAppleStandBlock.COOL_TICKS))n++;return n;}
    private int floss(){return state().getValue(CottonCandyCartBlock.FLOSS_LEVEL);}
    private int corn(){return state().getValue(PopcornWagonBlock.POPCORN);}
    private boolean popping(){return state().getValue(PopcornWagonBlock.POPPING);}
    private int room(){return OUTPUT_CAPACITY-products.stream().mapToInt(ItemStack::getCount).sum();}
    public boolean complete(){
        var level=station.getLevel();var pos=station.getBlockPos();
        if(station.isRemoved() || !(level instanceof ServerLevel) || !level.hasChunkAt(pos) || level.getBlockEntity(pos)!=station
            || !(station.getBlockState().getBlock() instanceof MultiDecorationBlock block) || block.part(station.getBlockState())!=MultiDecorationBlock.MASTER)return false;
        var facing=station.getBlockState().getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++){
            var at=block.partPos(pos,facing,i);if(!level.hasChunkAt(at))return false;var s=level.getBlockState(at);
            if(!s.is(block) || block.part(s)!=i || s.getValue(MultiDecorationBlock.FACING)!=facing)return false;
        }
        return true;
    }
    public boolean allowed(PeepoEntity npc){
        if(!complete() || npc.level()!=station.getLevel() || !npc.orders.tamed())return false;
        var block=(MultiDecorationBlock)station.getBlockState().getBlock();var facing=station.getBlockState().getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++)if(!CompanionJobs.permitted(npc,block.partPos(station.getBlockPos(),facing,i)))return false;
        return true;
    }
    private boolean assigned(PeepoEntity npc){return allowed(npc) && npc.assignments.assignedWork(station.getBlockPos());}
    public ItemStack product(){return item(apple()?"caramel_apple":cotton()?"cotton_candy":caramelMode?"caramel_popcorn":"popcorn");}
    public boolean acceptsFilter(ItemStack stack){return stack.isEmpty() || (apple()?stack.is(JugcraftAgriculture.item("caramel_apple")):cotton()?stack.is(JugcraftAgriculture.item("cotton_candy")):stack.is(JugcraftAgriculture.item("popcorn")) || stack.is(JugcraftAgriculture.item("caramel_popcorn")));}
    private boolean input(ItemStack raw){return apple()?raw.is(Items.APPLE) || raw.is(Items.STICK) || raw.is(JugcraftAgriculture.item("caramel")):cotton()?raw.is(Items.SUGAR) || raw.is(Items.PAPER):raw.is(JugcraftAgriculture.item("corn_kernels")) || raw.is(JugcraftAgriculture.item("caramel"));}
    public List<ItemStack> results(ItemStack raw){return input(raw)?List.of(product()):List.of();}
    public boolean busy(){return apple()?appleCount(false)>0:cotton()?floss()>0:corn()>0 || popping();}
    public void prepare(Storage<ItemVariant> source,ToIntFunction<ItemStack> goal,ToDoubleFunction<ItemStack> priority){
        if(apple() || cotton() || busy())return;
        var plain=item("popcorn");var caramel=item("caramel_popcorn");boolean available=false;int reads=0;
        for(var view:source){if(++reads>128)break;if(view.getAmount()>0 && view.getResource().isOf(JugcraftAgriculture.item("caramel"))){available=true;break;}}
        boolean a=filter.allows(plain) && goal.applyAsInt(plain)>0,b=filter.allows(caramel) && goal.applyAsInt(caramel)>0;
        boolean next=b && (!a || available && priority.applyAsDouble(caramel)>=priority.applyAsDouble(plain));
        if(next!=caramelMode){caramelMode=next;station.setChanged();CompanionReadiness.invalidate(station);}
    }
    private int matching(List<ItemStack> incoming,Item item){return incoming.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    public int pending(ItemStack icon,List<ItemStack> incoming){
        if(!filter.stock.matches(icon,product()))return 0;
        if(apple())return appleCount(false)-appleCount(true)+matching(incoming,Items.APPLE);
        if(cotton())return floss()+matching(incoming,Items.SUGAR);
        int kernels=(popping()?1:0)+matching(incoming,JugcraftAgriculture.item("corn_kernels"));
        return caramelMode?(corn()+kernels+1)/2:kernels;
    }
    /** missing is the shared goal after pending ingredients; finish already-started native batches. */
    public int limit(ItemStack raw,int nativeNeed,List<ItemStack> incoming,ToIntFunction<ItemStack> missing){
        if(nativeNeed<=0)return 0;int deficit=missing.applyAsInt(product());int reserved=matching(incoming,raw.getItem());
        if(apple()){
            if(raw.is(Items.APPLE))return deficit==Integer.MAX_VALUE?nativeNeed:Math.min(nativeNeed,deficit);
            int installed=raw.is(Items.STICK)?apples().sticks():apples().caramel();
            return deficit==Integer.MAX_VALUE?Math.max(0,nativeNeed-reserved):Math.min(nativeNeed,Math.max(0,deficit-installed-reserved));
        }
        if(cotton()){
            if(raw.is(Items.PAPER))return Math.max(0,nativeNeed-reserved);
            return deficit==Integer.MAX_VALUE?nativeNeed:Math.min(nativeNeed,deficit);
        }
        if(raw.is(JugcraftAgriculture.item("caramel")))return Math.max(0,nativeNeed-reserved);
        if(deficit==Integer.MAX_VALUE)return nativeNeed;
        int kernels=corn()+(popping()?1:0)+matching(incoming,JugcraftAgriculture.item("corn_kernels"));
        return Math.min(nativeNeed,caramelMode?Math.max(0,deficit*2+(kernels%2)):deficit);
    }
    private int needed(ItemStack raw){
        if(!complete() || raw.isEmpty())return 0;
        if(apple()){
            if(!filter.allows(product()))return 0;var s=apples();int space=CaramelAppleStandBlock.RACK-appleCount(false);
            if(raw.is(Items.APPLE))return Math.min(space,Math.min(s.caramel(),s.sticks()));
            if(raw.is(Items.STICK))return Math.max(0,Math.min(CaramelAppleStandBlock.STICKS-s.sticks(),space-s.sticks()));
            if(raw.is(JugcraftAgriculture.item("caramel")))return Math.max(0,Math.min(CaramelAppleStandBlock.CARAMEL-s.caramel(),space-s.caramel()));
        }else if(cotton()){
            // A physical trip delivers merged stacks in order. Never plan several imaginary
            // sugar/paper cycles by repeatedly emptying and refilling the bowl in one probe.
            if(raw.is(Items.PAPER))return Math.max(0,Math.min(CottonCandyCartBlock.FLOSS-batchPaper,Math.min(floss(),room())));
            if(raw.is(Items.SUGAR) && batchPaper==0 && state().getValue(MultiDecorationBlock.LIT) && filter.allows(product()))return Math.max(0,Math.min(CottonCandyCartBlock.FLOSS-batchSugar,Math.min(CottonCandyCartBlock.FLOSS-floss(),room()-floss())));
        }else{
            if(raw.is(JugcraftAgriculture.item("caramel")) && caramelMode)return Math.min(corn()/2,room());
            if(raw.is(JugcraftAgriculture.item("corn_kernels")) && state().getValue(MultiDecorationBlock.LIT) && !popping() && corn()<PopcornWagonBlock.CAPACITY
                && (!caramelMode || (corn()+2)/2<=room()) && (filter.allows(product()) || caramelMode && corn()%2==1))return 1;
        }
        return 0;
    }
    record Snapshot(CaramelAppleStandBlockEntity.Stock apple,BlockState state,List<ItemStack> products,int sugar,int paper){}
    protected Snapshot createSnapshot(){return new Snapshot(copy(applePending),statePending,products.stream().map(ItemStack::copy).toList(),batchSugar,batchPaper);}
    protected void readSnapshot(Snapshot s){applePending=copy(s.apple());statePending=s.state();products.clear();s.products().forEach(v->products.add(v.copy()));batchSugar=s.sugar();batchPaper=s.paper();}
    protected void onFinalCommit(){
        batchSugar=batchPaper=0;
        if(applePending!=null){((CaramelAppleStandBlockEntity)station).restoreCompanionStock(applePending);applePending=null;}
        if(statePending!=null){
            var before=station.getBlockState();var after=statePending;statePending=null;
            var block=(MultiDecorationBlock)before.getBlock();var facing=before.getValue(MultiDecorationBlock.FACING);
            boolean start=block instanceof PopcornWagonBlock && !before.getValue(PopcornWagonBlock.POPPING) && after.getValue(PopcornWagonBlock.POPPING);
            for(int i=0;i<block.cells().length;i++){
                var pos=block.partPos(station.getBlockPos(),facing,i);var old=station.getLevel().getBlockState(pos);var next=old;
                if(block instanceof CottonCandyCartBlock)next=old.setValue(CottonCandyCartBlock.FLOSS_LEVEL,after.getValue(CottonCandyCartBlock.FLOSS_LEVEL));
                else next=old.setValue(PopcornWagonBlock.POPCORN,after.getValue(PopcornWagonBlock.POPCORN)).setValue(PopcornWagonBlock.POPPING,after.getValue(PopcornWagonBlock.POPPING));
                if(next!=old)station.getLevel().setBlock(pos,next,Block.UPDATE_CLIENTS);
            }
            if(start)station.getLevel().scheduleTick(station.getBlockPos(),block,PopcornWagonBlock.POP_TICKS);
        }
        station.setChanged();CompanionReadiness.invalidate(station);
    }
    private void addProduct(ItemStack stack){for(var held:products)if(ItemStack.isSameItemSameComponents(held,stack) && held.getCount()+stack.getCount()<=held.getMaxStackSize()){held.grow(stack.getCount());return;}products.add(stack.copy());}
    private long insert(ItemVariant item,long amount,TransactionContext tx){
        StoragePreconditions.notBlankNotNegative(item,amount);var raw=item.toStack(1);int n=(int)Math.min(amount,needed(raw));if(n<=0)return 0;
        updateSnapshots(tx);
        if(apple()){
            var s=apples();long[] dipped=s.dipped().clone();int caramel=s.caramel(),sticks=s.sticks();
            if(raw.is(Items.APPLE)){int left=n;for(int i=0;i<dipped.length && left>0;i++)if(dipped[i]<0){dipped[i]=now();left--;}caramel-=n;sticks-=n;}
            else if(raw.is(Items.STICK))sticks+=n;else caramel+=n;
            applePending=new CaramelAppleStandBlockEntity.Stock(caramel,sticks,dipped);
        }else if(cotton()){
            if(raw.is(Items.SUGAR)){batchSugar+=n;statePending=state().setValue(CottonCandyCartBlock.FLOSS_LEVEL,floss()+n);}
            else {batchPaper+=n;statePending=state().setValue(CottonCandyCartBlock.FLOSS_LEVEL,floss()-n);addProduct(item("cotton_candy").copyWithCount(n));}
        }else if(raw.is(JugcraftAgriculture.item("corn_kernels")))statePending=state().setValue(PopcornWagonBlock.POPPING,true);
        else {statePending=state().setValue(PopcornWagonBlock.POPCORN,corn()-n*2);addProduct(item("caramel_popcorn").copyWithCount(n));}
        return n;
    }
    public CompanionLogistics.Port port(PeepoEntity npc){return new CompanionLogistics.Port(){
        public Identifier plan(){return assigned(npc)?net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(station.getBlockState().getBlock()):null;}
        public void prepare(Storage<ItemVariant> source){if(assigned(npc))ConfectionCompanion.this.prepare(source,s->Integer.MAX_VALUE,s->-1);}
        public int needed(ItemStack raw){return assigned(npc)?ConfectionCompanion.this.needed(raw):0;}
        private final Storage<ItemVariant> input=new InsertionOnlyStorage<>(){public long insert(ItemVariant item,long amount,TransactionContext tx){return assigned(npc)?ConfectionCompanion.this.insert(item,amount,tx):0;}};
        public Storage<ItemVariant> inputs(){return input;}
        public Storage<ItemVariant> outputs(){return new Output(npc);}
        public CompanionStatus status(){
            if(!allowed(npc))return complete()?CompanionStatus.FORBIDDEN:CompanionStatus.MACHINE_UNFORMED;
            if(apple())return appleCount(true)>0?CompanionStatus.FULL:appleCount(false)>0?CompanionStatus.COOLING:CompanionStatus.NO_INPUT;
            if(!products.isEmpty() || !cotton() && !caramelMode && corn()>0)return CompanionStatus.FULL;
            if(!state().getValue(MultiDecorationBlock.LIT))return CompanionStatus.NO_HEAT;
            return !cotton() && popping()?CompanionStatus.WORKING:CompanionStatus.NO_INPUT;
        }
    };}
    public Storage<ItemVariant> outputs(){return new Output(null);}
    private long amount(ItemVariant item){
        if(!complete())return 0;
        long n=products.stream().filter(item::matches).mapToLong(ItemStack::getCount).sum();
        if(apple() && item.matches(item("caramel_apple")))n+=appleCount(true);
        if(!apple() && !cotton() && !caramelMode && item.matches(item("popcorn")))n+=corn();
        return n;
    }
    private final class Output implements Storage<ItemVariant>{
        private final PeepoEntity npc;Output(PeepoEntity npc){this.npc=npc;}
        private boolean available(){return npc==null?complete():assigned(npc);}
        public long insert(ItemVariant item,long amount,TransactionContext tx){return 0;}
        public boolean supportsInsertion(){return false;}
        public long extract(ItemVariant item,long amount,TransactionContext tx){
            StoragePreconditions.notBlankNotNegative(item,amount);long n=available()?Math.min(amount,ConfectionCompanion.this.amount(item)):0;if(n<=0)return 0;updateSnapshots(tx);long left=n;
            if(apple() && item.matches(item("caramel_apple"))){var s=apples();var dipped=s.dipped().clone();for(int i=0;i<dipped.length && left>0;i++)if(dipped[i]>=0 && now()-dipped[i]>=CaramelAppleStandBlock.COOL_TICKS){dipped[i]=-1;left--;}applePending=new CaramelAppleStandBlockEntity.Stock(s.caramel(),s.sticks(),dipped);}
            else if(!cotton() && !apple() && !caramelMode && item.matches(item("popcorn"))){int take=(int)Math.min(left,corn());statePending=state().setValue(PopcornWagonBlock.POPCORN,corn()-take);left-=take;}
            for(int i=products.size()-1;i>=0 && left>0;i--){var stack=products.get(i);if(item.matches(stack)){int take=(int)Math.min(left,stack.getCount());stack.shrink(take);left-=take;if(stack.isEmpty())products.remove(i);}}
            return n;
        }
        public Iterator<StorageView<ItemVariant>> iterator(){
            if(!available())return Collections.emptyIterator();var variants=new LinkedHashSet<ItemVariant>();for(var stack:products)variants.add(ItemVariant.of(stack));variants.add(ItemVariant.of(product()));
            return variants.stream().filter(v->amount(v)>0).<StorageView<ItemVariant>>map(v->new StorageView<>(){
                public boolean isResourceBlank(){return getAmount()==0;}public ItemVariant getResource(){return v;}
                public long getAmount(){return available()?amount(v):0;}public long getCapacity(){return apple()?CaramelAppleStandBlock.RACK:!cotton() && v.matches(item("popcorn"))?PopcornWagonBlock.CAPACITY:OUTPUT_CAPACITY;}
                public long extract(ItemVariant item,long amount,TransactionContext tx){return item.equals(v)?Output.this.extract(item,amount,tx):0;}
            }).iterator();
        }
    }
    public void save(ValueOutput out){filter.save(out);out.putBoolean("CompanionCaramel",caramelMode);if(!products.isEmpty())out.store("CompanionConfections",ItemStack.CODEC.listOf(),products);}
    public void load(ValueInput in){
        applePending=null;statePending=null;batchSugar=batchPaper=0;filter.load(in);caramelMode=in.getBooleanOr("CompanionCaramel",false);products.clear();
        in.read("CompanionConfections",ItemStack.CODEC.listOf()).ifPresent(list->{for(var s:list){if(room()==0)break;if(s.is(JugcraftAgriculture.item("cotton_candy")) || s.is(JugcraftAgriculture.item("caramel_popcorn")))addProduct(s.copyWithCount(Math.min(room(),s.getCount())));}});
    }
    public boolean collect(Player player){if(products.isEmpty())return false;for(var stack:products){var copy=stack.copy();player.getInventory().add(copy);if(!copy.isEmpty())Containers.dropItemStack(station.getLevel(),station.getBlockPos().getX(),station.getBlockPos().getY()+1,station.getBlockPos().getZ(),copy);}products.clear();station.setChanged();CompanionReadiness.invalidate(station);return true;}
    public void dropProducts(){if(station.getLevel()!=null && !station.getLevel().isClientSide())for(var stack:products)Containers.dropItemStack(station.getLevel(),station.getBlockPos().getX(),station.getBlockPos().getY(),station.getBlockPos().getZ(),stack);products.clear();}
}
