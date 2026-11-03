package io.github.jimbozoomer.jugcraft.machine;

import java.util.*;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipe;
import local.peepo.CompanionLogistics;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;

/** Recipe-guided item port; fluids, power and machine processing still belong to the machine. */
public final class MachineCompanionPort implements CompanionLogistics.Port {
    public record Part(Ingredient ingredient,int count){}
    public record Plan(Identifier id,List<Part> parts,List<ItemStack> results,boolean ordered,Recipe<?> recipe){}
    private static final Map<MachineKind,Map<Identifier,Plan>> CATALOG=new EnumMap<>(MachineKind.class);
    public static void clearCatalog(){CATALOG.clear();}
    private final MachineBlockEntity machine;
    private Identifier selected;
    private static final int MAX_BATCHES=8;
    /** Bounded saved delivery caps/mapping only; real ingredients remain solely in inventories. */
    private List<ItemStack> batchInputs=List.of();
    private int[] batchSlots=new int[0];
    private final MachineSpecialSupply special;
    public final local.peepo.CompanionRecipeFilter filter=new local.peepo.CompanionRecipeFilter();
    public local.peepo.CompanionRecipeFilter filter(){var old=selection();filter.initialize(old==null || old.results.isEmpty()?ItemStack.EMPTY:old.results.getFirst());return filter;}
    public boolean acceptsFilter(ItemStack stack){return special.applies()?special.acceptsFilter(stack):stack.isEmpty() || catalog().values().stream().anyMatch(p->p.results.stream().anyMatch(s->s.is(stack.getItem())));}
    public void filterChanged(){selected=null;batchInputs=List.of();batchSlots=new int[0];machine.companionRecipeChanged();}
    @Override public void prepare(Storage<ItemVariant> source){
        prepare(source,null);
    }
    public void prepare(Storage<ItemVariant> source,java.util.function.ToDoubleFunction<ItemStack> priority){
        if(special.applies()){special.prepare(source,priority);return;}
        if(batchProcessor() && filter.stock.hasIncoming(machine))return;
        // Finish the already loaded operation before fetching more. This also respects manually/hopper-loaded inputs.
        if(batchProcessor()){
            var loaded=recipe();
            if(loaded!=null){for(var p:catalog().values())if(p.recipe==loaded){select(p.id);break;}return;}
            if(finishedOutput())return;
        }
        var allowed=filter();var supplies=local.peepo.RecipeSupplies.available(source);
        var installed=new ArrayList<ItemStack>();for(int i=0;i<machine.kind().outputSlot();i++)installed.add(machine.getItem(i));
        var choices=new ArrayList<>(catalog().values());
        java.util.function.ToDoubleFunction<Plan> score=p->priority==null?0:p.results.stream().filter(allowed::allows).mapToDouble(priority).max().orElse(-2);
        choices.sort(Comparator.<Plan>comparingDouble(score).reversed().thenComparingInt(p->p.id.equals(selected)?0:1));
        // Shared across every candidate recipe and batch size, with a small one-recipe escape hatch.
        int[] bulkBudget={8192},singleBudget={256};
        int inspected=0;for(var p:choices){if(++inspected>256)break;
            if(score.applyAsDouble(p)<=-2 || (!allowed.empty() && p.results.stream().noneMatch(allowed::allows)) || allocation(p)==null)continue;
            if(p.recipe instanceof FluidRecipe fluid && (machine.tanks()==null || !fluid.fluidsMatch(machine.tanks())))continue;
            var parts=p.parts.stream().map(part->new local.peepo.RecipeSupplies.Part(part.ingredient,part.count)).toList();
            if(local.peepo.RecipeSupplies.completes(parts,installed,supplies)){
                if(batchProcessor()){
                    var batch=balancedInputs(p,supplies,bulkBudget,singleBudget);if(batch.isEmpty())continue;
                    var slots=allocation(p);select(p.id);
                    boolean changed=!Arrays.equals(batchSlots,slots) || batchInputs.size()!=batch.size();
                    if(!changed)for(int i=0;i<batch.size();i++)if(!ItemStack.matches(batchInputs.get(i),batch.get(i))){changed=true;break;}
                    batchInputs=batch;batchSlots=slots;if(changed)machine.setChanged();
                }else select(p.id);
                return;
            }
        }
        if(installed.stream().allMatch(ItemStack::isEmpty))select((Identifier)null);
    }
    public MachineCompanionPort(MachineBlockEntity machine){this.machine=machine;this.special=new MachineSpecialSupply(machine,filter);}
    private Map<Identifier,Plan> catalog(){
        if(!(machine.getLevel() instanceof ServerLevel level))return Map.of();
        return CATALOG.computeIfAbsent(machine.kind(),kind->catalog(level.getServer(),kind));
    }
    private static Map<Identifier,Plan> catalog(MinecraftServer server,MachineKind kind){
        var plans=new ArrayList<Plan>();
        for(var holder:server.getRecipeManager().getRecipes()){
            var recipe=holder.value();var parts=new ArrayList<Part>();var results=new ArrayList<ItemStack>();boolean ordered=false;
            if(recipe instanceof MachineRecipe single && single.machine()==kind){
                parts.add(new Part(single.input(),single.inputCount()));results.add(single.output().create());
                for(var extra:single.byproducts())if(extra.enabled())results.add(extra.result().create());
            }else if(recipe instanceof MultiMachineRecipe multi && multi.getType()==MachineRecipeTypes.multi(kind)){
                for(var part:multi.parts())parts.add(new Part(part.ingredient(),part.count()));results.add(multi.output().create());
            }else if(recipe instanceof SmeltingRecipe smelting && kind==MachineKind.ELECTRIC_FURNACE){
                parts.add(new Part(smelting.input(),1));results.add(smelting.assemble(new SingleRecipeInput(ItemStack.EMPTY)));
            }else if(recipe instanceof FluidRecipe fluid && fluid.machine()==kind && fluid.legacy()){
                ordered=true;for(var part:fluid.items())parts.add(new Part(part.ingredient(),part.count()));
                for(var result:fluid.results())results.add(result.create());
            }else continue;
            if(parts.size()<=kind.outputSlot())plans.add(new Plan(holder.id().identifier(),List.copyOf(parts),List.copyOf(results),ordered,recipe));
        }
        plans.sort(Comparator.comparing(p->p.id().toString()));
        var catalog=new LinkedHashMap<Identifier,Plan>();for(var plan:plans)catalog.put(plan.id,plan);return Collections.unmodifiableMap(catalog);
    }
    public boolean selectable(){return special.applies()?special.recipeCapable():catalog().values().stream().anyMatch(p->!p.results.isEmpty());}
    public boolean supportsStockTargets(){return machine.kind()==MachineKind.AUTO_CRAFTER || machine.kind()==MachineKind.ELECTROPLATING_BATH || !special.applies() && catalog().values().stream().anyMatch(p->!p.parts.isEmpty() && !p.results.isEmpty());}
    public boolean supportsSupply(){return special.applies() || catalog().values().stream().anyMatch(p->!p.parts.isEmpty());}
    public boolean supportsOutput(){return machine.kind()!=MachineKind.COAL_GENERATOR && machine.kind().outputSlot()<machine.kind().slots;}
    public Plan selection(){return machine.kind()==MachineKind.ELECTROPLATING_BATH?special.stockPlan():selected==null?null:catalog().get(selected);}
    public boolean locked(){return selected!=null || !filter().empty();}
    /** Only the original item processors opt into bounded resident batches; fluids and special ports keep their rules. */
    public boolean batchProcessor(){return !special.applies() && !machine.kind().isFluidProcessor();}
    public boolean loadedBatch(){var current=selection();return batchProcessor() && current!=null && ready(current);}
    public boolean finishedOutput(){
        if(!batchProcessor())return false;
        for(int i=machine.kind().outputSlot();i<machine.kind().slots;i++)if(!machine.getItem(i).isEmpty())return true;
        return false;
    }
    private boolean ready(Plan p){
        var slots=allocation(p);if(slots==null)return false;
        for(int i=0;i<slots.length;i++)if(machine.getItem(slots[i]).getCount()<p.parts.get(i).count)return false;
        return !(p.recipe instanceof FluidRecipe f) || machine.tanks()!=null && f.fluidsMatch(machine.tanks());
    }
    public Recipe<?> recipe(){
        var allowed=filter();var current=selection();
        if(current!=null && (allowed.empty() || current.results.stream().anyMatch(allowed::allows)) && ready(current))return current.recipe;
        int inspected=0;for(var p:catalog().values()){if(++inspected>256)break;
            if((allowed.empty() || p.results.stream().anyMatch(allowed::allows)) && ready(p))return p.recipe;
        }return null;
    }
    private void select(Identifier id){if(Objects.equals(selected,id))return;selected=id;batchInputs=List.of();batchSlots=new int[0];machine.companionRecipeChanged();}
    public boolean select(ItemStack output){
        if(output.isEmpty()){select((Identifier)null);return true;}
        var matches=catalog().values().stream().filter(p->p.results.stream().anyMatch(s->s.is(output.getItem()))).toList();
        if(matches.isEmpty())return false;
        int index=-1;for(int i=0;i<matches.size();i++)if(matches.get(i).id.equals(selected))index=i;
        select(matches.get((index+1)%matches.size()).id);return true;
    }
    public Identifier plan(){return special.applies()?special.plan():selection()==null?null:selected;}
    private boolean guidance(Plan plan){
        if(!plan.id.equals(selected) || batchInputs.size()!=plan.parts.size() || batchSlots.length!=plan.parts.size())return false;
        var used=new boolean[machine.kind().outputSlot()];int batches=0;
        for(int i=0;i<batchSlots.length;i++){
            int slot=batchSlots[i];var part=plan.parts.get(i);var target=batchInputs.get(i);
            if(slot<0 || slot>=used.length || used[slot] || target.isEmpty() || !part.ingredient.test(target)
                || target.getCount()>Math.min(machine.getMaxStackSize(),target.getMaxStackSize())
                || part.count<=0 || target.getCount()%part.count!=0 || target.getCount()/part.count>MAX_BATCHES)return false;
            int amount=target.getCount()/part.count;if(i>0 && amount!=batches || plan.ordered && slot!=i)return false;batches=amount;
            used[slot]=true;
        }
        return true;
    }
    /** Assign every ingredient its own slot; preserve existing stacks, including ambiguous tag recipes. */
    private int[] allocation(Plan plan){
        // A narrow ingredient can arrive before a broad tag ingredient. Keep each in its planned slot
        // through transaction probes and real multi-stack deliveries instead of greedily remapping it.
        if(guidance(plan)){
            boolean valid=true;var used=new boolean[machine.kind().outputSlot()];
            for(int i=0;i<batchSlots.length;i++){
                int slot=batchSlots[i];if(slot<0 || slot>=used.length || used[slot]){valid=false;break;}
                used[slot]=true;var held=machine.getItem(slot);
                if(!held.isEmpty() && !plan.parts.get(i).ingredient.test(held)){valid=false;break;}
            }
            if(valid)for(int slot=0;slot<used.length;slot++)if(!used[slot] && !machine.getItem(slot).isEmpty()){valid=false;break;}
            if(valid)return batchSlots.clone();
        }
        int[] slots=new int[plan.parts.size()];Arrays.fill(slots,-1);
        return assign(plan,0,slots,new boolean[machine.kind().outputSlot()])?slots:null;
    }
    private boolean assign(Plan plan,int index,int[] slots,boolean[] used){
        if(index==slots.length){for(int slot=0;slot<used.length;slot++)if(!used[slot] && !machine.getItem(slot).isEmpty())return false;return true;}
        // Existing stacks first. At most the machine's small fixed set of input slots.
        for(int pass=0;pass<2;pass++)for(int slot=0;slot<used.length;slot++){
            if(used[slot] || plan.ordered && slot!=index)continue;
            var held=machine.getItem(slot);if(held.isEmpty()!=(pass==1) || !held.isEmpty() && !plan.parts.get(index).ingredient.test(held))continue;
            used[slot]=true;slots[index]=slot;if(assign(plan,index+1,slots,used))return true;used[slot]=false;
        }
        return false;
    }
    private boolean inputAllowed(int slot,ItemStack stack){
        for(var side:net.minecraft.core.Direction.values())for(int exposed:machine.getSlotsForFace(side))
            if(exposed==slot && machine.canPlaceItemThroughFace(slot,stack,side))return true;
        return false;
    }
    /** Worst-case native output packing, including every enabled probabilistic byproduct. */
    private int outputBatches(Plan plan){
        var held=new ArrayList<ItemStack>();
        for(int slot=machine.kind().outputSlot();slot<machine.kind().slots;slot++)held.add(machine.getItem(slot).copy());
        if(plan.results.isEmpty() || held.isEmpty())return 0;
        for(int batch=0;batch<MAX_BATCHES;batch++)for(int result=0;result<plan.results.size();result++){
            var stack=plan.results.get(result);int found=-1;
            for(int slot=result==0?0:1;slot<(result==0?1:held.size());slot++){
                var present=held.get(slot);int cap=Math.min(machine.getMaxStackSize(),stack.getMaxStackSize());
                if((present.isEmpty() || ItemStack.isSameItemSameComponents(present,stack)) && present.getCount()+stack.getCount()<=cap){found=slot;break;}
            }
            if(found<0)return batch;
            var present=held.get(found);held.set(found,stack.copyWithCount(present.getCount()+stack.getCount()));
        }
        return MAX_BATCHES;
    }
    private List<ItemStack> balancedInputs(Plan plan,List<ItemStack> supplies,int[] bulkBudget,int[] singleBudget){
        var slots=allocation(plan);if(slots==null)return List.of();
        // Aggregate exact components. Tagged ingredients must not pretend differently named stacks can merge.
        var variants=new LinkedHashMap<ItemVariant,Integer>();
        for(var stack:supplies)if(!stack.isEmpty())variants.merge(ItemVariant.of(stack),stack.getCount(),Integer::sum);
        for(int batches=outputBatches(plan);batches>0;batches--){
            var budget=batches==1?singleBudget:bulkBudget;if(budget[0]<=0)continue;
            var targets=new ArrayList<ItemStack>(Collections.nCopies(slots.length,ItemStack.EMPTY));
            if(balance(plan,slots,0,batches,variants,targets,budget))return List.copyOf(targets);
        }
        return List.of();
    }
    private boolean balance(Plan plan,int[] slots,int index,int batches,Map<ItemVariant,Integer> supplies,List<ItemStack> targets,int[] budget){
        if(index==slots.length)return true;
        if(--budget[0]<0)return false;
        var part=plan.parts.get(index);var held=machine.getItem(slots[index]);int wanted=part.count*batches;
        var choices=held.isEmpty()?new ArrayList<>(supplies.keySet()):List.of(ItemVariant.of(held));
        for(var variant:choices){
            if(--budget[0]<0)return false;
            var stack=variant.toStack(1);
            if(!part.ingredient.test(stack) || wanted>Math.min(machine.getMaxStackSize(),stack.getMaxStackSize()))continue;
            int missing=Math.max(0,wanted-held.getCount()),available=supplies.getOrDefault(variant,0);
            if(missing>available || missing>0 && !inputAllowed(slots[index],stack))continue;
            supplies.put(variant,available-missing);targets.set(index,stack.copyWithCount(wanted));
            boolean fits=balance(plan,slots,index+1,batches,supplies,targets,budget);supplies.put(variant,available);
            if(fits)return true;
        }
        return false;
    }
    private int missing(Plan plan,int part,int slot,ItemStack stack){
        var held=machine.getItem(slot);var ingredient=plan.parts.get(part);
        int target=ingredient.count;
        if(batchProcessor()){
            // Old saves may have an in-flight manifest without guidance; retain the native eight-batch cap.
            target=ingredient.count*MAX_BATCHES;
            if(guidance(plan)){
                var planned=batchInputs.get(part);
                if(!ItemStack.isSameItemSameComponents(planned,stack))return 0;
                target=planned.getCount();
            }
        }
        return ingredient.ingredient.test(stack) && (held.isEmpty() || ItemStack.isSameItemSameComponents(stack,held)) && inputAllowed(slot,stack)
            ?Math.max(0,Math.min(machine.getMaxStackSize(),Math.min(stack.getMaxStackSize(),target))-held.getCount()):0;
    }
    @Override public int pickupNeeded(ItemStack stack){return acceptsSupplyBatch()?needed(stack):0;}
    @Override public boolean acceptsSupplyBatch(){return !batchProcessor() || !loadedBatch() && !finishedOutput() && !filter.stock.hasIncoming(machine);}
    public int needed(ItemStack stack){
        if(special.applies())return Arrays.stream(special.needs(stack)).sum();
        var plan=selection();if(plan==null)return 0;var slots=allocation(plan);if(slots==null)return 0;
        int total=0;for(int i=0;i<slots.length;i++)total=Math.min(stack.getMaxStackSize(),total+missing(plan,i,slots[i],stack));return total;
    }
    public Storage<ItemVariant> inputs(){return new InsertionOnlyStorage<>(){
        public long insert(ItemVariant item,long amount,TransactionContext tx){
            StoragePreconditions.notBlankNotNegative(item,amount);
            if(special.applies()){long moved=0;var needs=special.needs(item.toStack(1));var inventory=ContainerStorage.of(machine,null);for(int i=0;i<needs.length && moved<amount;i++)if(needs[i]>0)moved+=inventory.getSlot(i).insert(item,Math.min(amount-moved,needs[i]),tx);return moved;}
            var plan=selection();if(plan==null)return 0;
            var slots=allocation(plan);if(slots==null)return 0;long moved=0;var stack=item.toStack(1);var inventory=ContainerStorage.of(machine,null);
            for(int i=0;i<slots.length && moved<amount;i++){
                int need=missing(plan,i,slots[i],stack);
                if(need>0)moved+=inventory.getSlot(slots[i]).insert(item,Math.min(amount-moved,need),tx);
            }
            return moved;
        }
    };}
    public Storage<ItemVariant> outputs(){
        if(!supportsOutput())return Storage.empty();
        var inventory=ContainerStorage.of(machine,null);var outputs=new ArrayList<Storage<ItemVariant>>();
        for(int slot=machine.kind().outputSlot();slot<machine.kind().slots;slot++){
            boolean allowed=false;
            for(var side:net.minecraft.core.Direction.values())for(int exposed:machine.getSlotsForFace(side))
                if(exposed==slot && machine.canTakeItemThroughFace(slot,machine.getItem(slot),side))allowed=true;
            if(allowed)outputs.add(inventory.getSlot(slot));
        }
        return new CombinedStorage<>(outputs);
    }
    public local.peepo.CompanionStatus status(){return machine.isLocked()?local.peepo.CompanionStatus.FORBIDDEN:local.peepo.CompanionStatus.IDLE;}
    public void save(ValueOutput out){
        filter().save(out);if(selected!=null)out.store("CompanionSupplyRecipe",Identifier.CODEC,selected);
        var plan=selection();if(batchProcessor() && plan!=null && guidance(plan)){
            var guidance=out.child("CompanionBatchGuidance");guidance.putInt("Count",batchInputs.size());
            for(int i=0;i<batchInputs.size();i++){var entry=guidance.child("Part"+i);entry.putInt("Slot",batchSlots[i]);entry.store("Target",ItemStack.CODEC,batchInputs.get(i));}
        }
    }
    public void load(ValueInput in){
        filter.load(in);selected=in.read("CompanionSupplyRecipe",Identifier.CODEC).orElse(null);batchInputs=List.of();batchSlots=new int[0];
        var data=in.child("CompanionBatchGuidance");if(!batchProcessor() || selected==null || data.isEmpty())return;
        int count=data.get().getIntOr("Count",0);if(count<=0 || count>machine.kind().outputSlot())return;
        var targets=new ArrayList<ItemStack>();var slots=new int[count];var used=new boolean[machine.kind().outputSlot()];
        for(int i=0;i<count;i++){
            var entry=data.get().child("Part"+i);if(entry.isEmpty())return;
            int slot=entry.get().getIntOr("Slot",-1);var target=entry.get().read("Target",ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if(slot<0 || slot>=used.length || used[slot] || target.isEmpty() || target.getCount()>Math.min(machine.getMaxStackSize(),target.getMaxStackSize()))return;
            used[slot]=true;slots[i]=slot;targets.add(target);
        }
        batchInputs=List.copyOf(targets);batchSlots=slots;
    }
}
