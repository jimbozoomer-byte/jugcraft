package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;

/** Restock real display places; serving food is never exposed as logistics output. */
public final class DisplayCompanion extends SnapshotParticipant<DisplayCompanion.Snapshot> {
    private final BlockEntity station;
    private final ItemStack[] templates;
    private int pendingServings=-1;
    public final CompanionRecipeFilter filter=new CompanionRecipeFilter();

    public DisplayCompanion(BlockEntity station){
        this.station=station;templates=new ItemStack[station instanceof ShowcaseBlockEntity shown?shown.places():0];
        Arrays.fill(templates,ItemStack.EMPTY);
    }
    public static DisplayCompanion of(BlockEntity station){
        if(station instanceof ServingDisplayBlockEntity serving)return serving.companion;
        if(station instanceof CandyCornDispenserBlockEntity dispenser)return dispenser.displayCompanion;
        if(station instanceof ShowcaseBlockEntity shown && shown.getBlockState().getBlock() instanceof FoodDisplayBlock)return shown.companion;
        return station instanceof ProduceDisplayBlockEntity produce?produce.companion:null;
    }
    public boolean acceptsFilter(ItemStack stack){
        return stack.isEmpty() || station.getBlockState().getBlock() instanceof PartyServingBlock serving && stack.is(serving.serves())
            || station instanceof CandyCornDispenserBlockEntity && stack.is(JugcraftAgriculture.item("candy_corn"))
            || station instanceof ProduceDisplayBlockEntity && stack.is(PumpkinCrateBlock.PRODUCE)
            || station instanceof ShowcaseBlockEntity && station.getBlockState().getBlock() instanceof FoodDisplayBlock;
    }
    /** Complete loaded native structure, including permission on every occupied part. No assignment prerequisite for the Planner. */
    public boolean allowed(PeepoEntity npc){
        var level=station.getLevel();var pos=station.getBlockPos();
        if(!(level instanceof ServerLevel) || level!=npc.level() || station.isRemoved() || !level.hasChunkAt(pos)
            || level.getBlockEntity(pos)!=station || !CompanionJobs.permitted(npc,pos))return false;
        var state=station.getBlockState();
        if(state.getBlock() instanceof ProduceDisplayBlock display){
            if(display.part(state)!=MultiDecorationBlock.MASTER)return false;
            var facing=state.getValue(MultiDecorationBlock.FACING);
            for(int i=0;i<display.cells().length;i++){
                var at=display.partPos(pos,facing,i);if(!level.hasChunkAt(at) || !CompanionJobs.permitted(npc,at))return false;
                var part=level.getBlockState(at);
                if(!part.is(display) || display.part(part)!=i || part.getValue(MultiDecorationBlock.FACING)!=facing)return false;
            }
            return true;
        }
        return state.getBlock() instanceof PartyServingBlock || state.getBlock() instanceof FoodDisplayBlock || state.getBlock() instanceof CandyCornDispenserBlock;
    }
    private boolean accessible(PeepoEntity npc){return npc.orders.tamed() && npc.assignments.assignedWork(station.getBlockPos()) && allowed(npc);}
    /** Retain each exact displayed variant when the player takes its last serving. */
    public void observe(){
        if(!(station instanceof ShowcaseBlockEntity shown))return;
        boolean changed=false;
        for(int i=0;i<templates.length;i++){
            var held=shown.get(i);
            if(!held.isEmpty() && !ItemStack.isSameItemSameComponents(held,templates[i])){templates[i]=held.copyWithCount(1);changed=true;}
        }
        if(changed)station.setChanged();
    }
    public void save(ValueOutput out){filter.save(out);if(templates.length>0)out.store("CompanionDisplayTemplates",ItemStack.OPTIONAL_CODEC.listOf(),Arrays.asList(templates));}
    public void load(ValueInput in){
        filter.load(in);pendingServings=-1;Arrays.fill(templates,ItemStack.EMPTY);
        in.read("CompanionDisplayTemplates",ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list->{for(int i=0;i<Math.min(list.size(),templates.length);i++)templates[i]=list.get(i).copyWithCount(1);});observe();
    }
    record Snapshot(List<ItemStack> stock,List<ItemStack> templates,int pending,int candy){}
    private List<ItemStack> stock(){
        if(station instanceof ShowcaseBlockEntity shown){var items=new ArrayList<ItemStack>(shown.places());for(int i=0;i<shown.places();i++)items.add(shown.get(i).copy());return items;}
        if(station instanceof ProduceDisplayBlockEntity produce)return produce.produce().stream().map(ItemStack::copy).toList();
        return List.of();
    }
    protected Snapshot createSnapshot(){return new Snapshot(stock(),Arrays.stream(templates).map(ItemStack::copy).toList(),pendingServings,station instanceof CandyCornDispenserBlockEntity dispenser?dispenser.count():0);}
    protected void readSnapshot(Snapshot before){
        restore(before.stock());pendingServings=before.pending();
        if(station instanceof CandyCornDispenserBlockEntity dispenser)dispenser.restoreCompanionCount(before.candy());
        for(int i=0;i<templates.length;i++)templates[i]=before.templates().get(i).copy();
    }
    private void restore(List<ItemStack> stock){
        if(station instanceof ShowcaseBlockEntity shown)shown.restoreCompanionStock(stock);
        else if(station instanceof ProduceDisplayBlockEntity produce)produce.restoreCompanionStock(stock);
    }
    protected void onFinalCommit(){
        if(pendingServings>=0){
            var level=station.getLevel();var pos=station.getBlockPos();int count=pendingServings;pendingServings=-1;
            if(level!=null && level.hasChunkAt(pos) && level.getBlockEntity(pos)==station && station.getBlockState().getBlock() instanceof PartyServingBlock serving)
                level.setBlock(pos,station.getBlockState().setValue(serving.servings(),count),Block.UPDATE_ALL);
        }
        if(station instanceof ShowcaseBlockEntity shown)shown.companionChanged();
        else if(station instanceof ProduceDisplayBlockEntity produce)produce.companionChanged();
        else if(station instanceof CandyCornDispenserBlockEntity dispenser)dispenser.companionChanged();
        station.setChanged();CompanionReadiness.invalidate(station);
    }
    private int needed(ItemStack stack){
        if(stack.isEmpty() || !filter.allows(stack))return 0;
        if(station instanceof CandyCornDispenserBlockEntity dispenser)return ItemStack.isSameItemSameComponents(stack,new ItemStack(JugcraftAgriculture.item("candy_corn")))
            ?Math.max(0,CandyCornDispenserBlock.CAPACITY-dispenser.count()):0;
        if(station.getBlockState().getBlock() instanceof PartyServingBlock serving){
            // Native party stands store counts, not components. Do not erase a named/custom serving.
            return ItemStack.isSameItemSameComponents(stack,new ItemStack(serving.serves()))
                ?Math.max(0,serving.capacity()-(pendingServings>=0?pendingServings:station.getBlockState().getValue(serving.servings()))):0;
        }
        if(station instanceof ProduceDisplayBlockEntity produce)return stack.is(PumpkinCrateBlock.PRODUCE)?Math.max(0,produce.capacity()-produce.produce().size()):0;
        if(station instanceof ShowcaseBlockEntity shown){
            int room=0;for(int i=0;i<templates.length;i++)if(shown.get(i).isEmpty() && !templates[i].isEmpty()
                && ItemStack.isSameItemSameComponents(templates[i],stack))room++;
            return room;
        }
        return 0;
    }
    public CompanionLogistics.Port forCompanion(PeepoEntity npc){return new CompanionLogistics.Port(){
        public void prepare(Storage<ItemVariant> source){if(accessible(npc))observe();}
        public Identifier plan(){return accessible(npc)?PeepoMod.id("serving_display"):null;}
        public int needed(ItemStack stack){return accessible(npc)?DisplayCompanion.this.needed(stack):0;}
        private final Storage<ItemVariant> input=new InsertionOnlyStorage<>(){
            public long insert(ItemVariant item,long amount,TransactionContext tx){
                StoragePreconditions.notBlankNotNegative(item,amount);if(!accessible(npc))return 0;
                var stack=item.toStack(1);int n=(int)Math.min(amount,needed(stack));if(n<=0)return 0;
                updateSnapshots(tx);
                if(station instanceof CandyCornDispenserBlockEntity dispenser){
                    dispenser.restoreCompanionCount(dispenser.count()+n);
                }else if(station.getBlockState().getBlock() instanceof PartyServingBlock serving){
                    pendingServings=(pendingServings>=0?pendingServings:station.getBlockState().getValue(serving.servings()))+n;
                }else{
                    var values=new ArrayList<>(stock());
                    if(station instanceof ProduceDisplayBlockEntity){for(int i=0;i<n;i++)values.add(stack.copy());}
                    else{int left=n;for(int i=0;i<values.size() && left>0;i++)if(values.get(i).isEmpty()
                        && !templates[i].isEmpty() && ItemStack.isSameItemSameComponents(templates[i],stack)){values.set(i,stack.copy());left--;}}
                    restore(values);
                }
                return n;
            }
        };
        public Storage<ItemVariant> inputs(){return input;}
        public Storage<ItemVariant> outputs(){return Storage.empty();}
        public CompanionStatus status(){
            if(!accessible(npc))return CompanionStatus.FORBIDDEN;
            if(station instanceof CandyCornDispenserBlockEntity dispenser)return dispenser.count()>=CandyCornDispenserBlock.CAPACITY?CompanionStatus.FULL:CompanionStatus.NO_INPUT;
            if(station instanceof ShowcaseBlockEntity){
                observe();boolean configured=false;
                for(var template:templates)if(!template.isEmpty() && filter.allows(template)){configured=true;if(needed(template)>0)return CompanionStatus.NO_INPUT;}
                return configured?CompanionStatus.FULL:CompanionStatus.SERVING_SETUP;
            }
            if(station instanceof ProduceDisplayBlockEntity produce)return produce.isFull()?CompanionStatus.FULL:CompanionStatus.NO_INPUT;
            var serving=(PartyServingBlock)station.getBlockState().getBlock();
            return needed(new ItemStack(serving.serves()))>0?CompanionStatus.NO_INPUT:CompanionStatus.FULL;
        }
    };}
}
