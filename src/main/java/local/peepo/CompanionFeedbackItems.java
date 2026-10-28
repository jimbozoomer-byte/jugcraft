package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import net.minecraft.world.item.*;

/** Read a known selected plan only when emitting a bubble; never search recipes or source chests. */
final class CompanionFeedbackItems {
    private CompanionFeedbackItems(){}
    static Item missing(PeepoEntity npc,CompanionAssignments.Target target){
        if(target==null || !target.local(npc.level()) || !npc.level().hasChunkAt(target.at().pos()) || !CompanionJobs.permitted(npc,target.at().pos()))return Items.AIR;
        var block=npc.level().getBlockEntity(target.at().pos());
        List<RecipeSupplies.Part> parts=List.of();var installed=new ArrayList<ItemStack>();
        if(block instanceof CraftingTableCompanionBlockEntity table && table.plan()!=null){parts=table.plan().parts();installed.addAll(table.installed());}
        else if(block instanceof CookingPotBlockEntity pot && !pot.isLocked()){
            var plan=pot.supplyPlan().orElse(null);
            if(plan!=null){parts=plan.ingredients().stream().map(p->new RecipeSupplies.Part(p.ingredient(),p.count())).toList();for(int i=0;i<CookingPotBlockEntity.INPUTS;i++)installed.add(pot.getItem(i));}
        }else if(block instanceof MachineBlockEntity machine && !machine.isLocked()){
            var plan=machine.companionPort.selection();
            if(plan!=null){parts=plan.parts().stream().map(p->new RecipeSupplies.Part(p.ingredient(),p.count())).toList();for(int i=0;i<machine.kind().outputSlot();i++)installed.add(machine.getItem(i));}
        }
        return missing(parts,installed);
    }
    static Item missing(List<RecipeSupplies.Part> parts,List<ItemStack> installed){
        var stock=installed.stream().map(ItemStack::copy).toList();
        for(var part:parts){
            int need=part.count();for(var stack:stock)if(need>0 && part.ingredient().test(stack)){int used=Math.min(need,stack.getCount());need-=used;stack.shrink(used);}
            if(need>0){var examples=part.ingredient().items().limit(2).toList();return examples.size()==1?examples.getFirst().value():Items.AIR;}
        }
        return Items.AIR;
    }
}
