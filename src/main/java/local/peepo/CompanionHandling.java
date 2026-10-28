package local.peepo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** One cosmetic transfer per visited endpoint. Never owns cargo or waits for an animation. */
final class CompanionHandling {
    private final PeepoEntity npc;
    private BlockPos target;
    private boolean pickup,prepared,committed;
    CompanionHandling(PeepoEntity npc){this.npc=npc;}
    private void visit(BlockPos at,boolean take){
        if(target==null || !target.equals(at) || pickup!=take){target=at.immutable();pickup=take;prepared=committed=false;}
    }
    void approach(BlockPos at,boolean take,ItemStack carried,int load){
        visit(at,take);
        if(prepared){npc.keepHandlingReady();return;}
        if(committed || npc.gesture()!=CompanionGesture.NONE && npc.gesture()!=CompanionGesture.CARRY)return;
        prepared=true;npc.showHandling(take?CompanionGesture.TAKE_READY:CompanionGesture.PUT_READY,take?ItemStack.EMPTY:carried,at,load);
    }
    void transferred(BlockPos at,boolean take,ItemStack item,int load){
        visit(at,take);if(committed)return;committed=true;
        // A genuine machine-specific action owns its prop for the remainder of this visit.
        if(npc.gesture().brewing() || npc.gesture()==CompanionGesture.POUR || npc.gesture()==CompanionGesture.TURN_FOOD)return;
        npc.showHandling(take?CompanionGesture.TAKE:CompanionGesture.PUT,item,at,load);
    }
    void reset(){
        if(npc.gesture().preparing())npc.showGesture(CompanionGesture.NONE,ItemStack.EMPTY,npc.blockPosition());
        target=null;prepared=committed=false;
    }
}
