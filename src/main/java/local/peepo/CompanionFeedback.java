package local.peepo;

import java.util.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import local.peepo.CompanionFeedbackRules.Kind;
import local.peepo.CompanionFeedbackRules.Reaction;

/** Cosmetic reactions consume existing report/committed-transfer events, never taking ownership of AI. */
public final class CompanionFeedback {
    private static final Set<String> KITCHENS=Set.of("cooking_pot","hearth_oven","cutting_board","skillet","kitchen_stove","candy_kettle","canning_kettle");
    private final PeepoEntity npc;
    final CompanionFeedbackRules rules=new CompanionFeedbackRules();
    private Reaction pending=Reaction.NONE;
    private Kind shown=Kind.NONE;
    private long pendingUntil,bubbleUntil;
    private final Set<UUID> viewers=new HashSet<>();
    private boolean recovering;
    private record Finish(Kind kind,Item item,long until,net.minecraft.core.BlockPos fishingWater){}
    private Finish finish;
    CompanionFeedback(PeepoEntity npc){this.npc=npc;}

    void report(CompanionStatus status){
        if(!npc.orders.tamed() || npc.editingSettings() || status==CompanionStatus.RECOVERING)return;
        if(shown.problem() && (status==CompanionStatus.WORKING || status==CompanionStatus.DELIVERING
            || status==CompanionStatus.EATING || status==CompanionStatus.RESTING || status==CompanionStatus.FETCHING_SUPPLIES))clearPresentation();
        CompanionAssignments.Target target=npc.transport.feedbackTarget();
        // An active job wins; errors on inactive rows cannot override productive work or deliveries.
        for(int row=1;target==null && row<=4;row++){
            var assigned=npc.assignments.get(row);
            if(assigned!=null && npc.isUsingJobAt(assigned.at().pos())){target=assigned;break;}
        }
        if(target==null)for(int row=1;row<=4;row++){
            var assigned=npc.assignments.get(row);
            if(assigned!=null && npc.report.row(row)==status.ordinal()){target=assigned;break;}
        }
        boolean generator=target!=null && Set.of("generator_wheel","hand_crank").contains(target.block().getPath());
        long now=npc.level().getGameTime();
        Kind event=rules.observe(status,target==null?null:target.at(),generator,now,npc.preferences.feedback);
        if(event!=Kind.NONE)emit(event,icon(status,target),now);
    }
    /** Called only after an actual item transfer succeeded; many stacks coalesce into one event. */
    void delivered(){success(Kind.DELIVERY);}
    /** Kitchen deliveries use the same successful transfer event, never a predicted output. */
    void delivered(CompanionAssignments.Target job,ItemStack stack){
        if(job!=null && kitchen(job.block().getPath()) && kitchenProduct(stack))finished(Kind.COOK,Items.AIR);
        else delivered();
    }
    static boolean kitchen(String path){return KITCHENS.contains(path);}
    static boolean kitchenProduct(ItemStack stack){
        // Whole oven pies/cakes are placeable blocks, not items with a FOOD component.
        return PeepoEntity.isEdible(stack) || !stack.isEmpty() && stack.getItem() instanceof BlockItem block
            && block.getBlock() instanceof io.github.jimbozoomer.jugcraft.agriculture.PieBlock pie && pie.filling()!=null;
    }
    /** At most one short-lived finishing touch. No inventory reservation, AI delay or saved state. */
    public void finished(Kind kind,Item item){
        long now=npc.level().getGameTime();
        if(npc.level().isClientSide() || !kind.reaction.finishing() || !npc.orders.tamed() || !rules.ready(kind,now,npc.preferences.feedback) || npc.editingSettings())return;
        if((kind==Kind.HARVEST || kind==Kind.CATCH) && item==Items.AIR)return;
        // Keep the first committed result in a rapid batch instead of extending it indefinitely.
        if(finish==null || now>=finish.until)finish=new Finish(kind,item,now+60,npc.workTarget());
    }
    public void finished(Kind kind,Item item,TransactionContext transaction){
        if(!rules.ready(kind,npc.level().getGameTime(),npc.preferences.feedback))return;
        new net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant<Boolean>(){
            {updateSnapshots(transaction);}
            protected Boolean createSnapshot(){return false;}
            protected void readSnapshot(Boolean value){}
            protected void onFinalCommit(){finished(kind,item);}
        };
    }
    private void success(Kind kind){
        long now=npc.level().getGameTime();
        if(npc.orders.tamed() && rules.admit(kind,now,npc.preferences.feedback))emit(kind,Items.AIR,now);
    }
    /** A rolled bonus is not a result. This participant fires only if the outer production transaction commits. */
    public void bonusProduced(TransactionContext transaction){
        new net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant<Boolean>(){
            {updateSnapshots(transaction);}
            protected Boolean createSnapshot(){return false;}
            protected void readSnapshot(Boolean value){}
            protected void onFinalCommit(){success(Kind.BONUS);}
        };
    }
    public boolean safePose(){
        return npc.isAlive() && !npc.isRemoved() && !npc.editingSettings() && !npc.isEating()
            && npc.getRestMode()==CompanionEnergy.Rest.NONE && !npc.isWheelRunning()
            && npc.workAnimation()==WorkAnimation.NONE && npc.gesture()==CompanionGesture.NONE && npc.socialPose()==0
            && npc.getMainHandItem().isEmpty() && !npc.transport.hasPendingDelivery()
            && !npc.isPassenger() && !npc.isInWater() && npc.onGround()
            && npc.getNavigation().isDone() && npc.getDeltaMovement().horizontalDistanceSqr()<.0004;
    }
    /** Free-handed walking can continue; only an unhurried fishing wait permits a work overlay. */
    public boolean safeFinishingPose(Reaction reaction){
        if(npc.preferences.feedback!=CompanionFeedbackRules.EXPRESSIVE || !reaction.finishing() || !npc.isAlive() || npc.isRemoved() || npc.editingSettings() || npc.isEating()
            || npc.isRecovering() || npc.getRestMode()!=CompanionEnergy.Rest.NONE || npc.isWheelRunning()
            || npc.gesture()!=CompanionGesture.NONE || npc.socialPose()!=0 || npc.transport.hasPendingDelivery()
            || npc.isPassenger() || npc.isInWater() || !npc.onGround())return false;
        if(reaction==Reaction.CATCH && npc.workAnimation()==WorkAnimation.FISH_WAIT)
            return CompanionFishing.rod(npc.getMainHandItem()) && npc.getDeltaMovement().horizontalDistanceSqr()<.0004;
        return npc.workAnimation()==WorkAnimation.NONE && npc.getMainHandItem().isEmpty();
    }
    void tick(){
        long now=npc.level().getGameTime();
        if(!npc.isAlive() || npc.preferences.feedback==CompanionFeedbackRules.OFF || npc.editingSettings()){
            clear();recovering=npc.isRecovering();return;
        }
        if(npc.isRecovering() && !recovering && npc.orders.tamed() && rules.admit(Kind.TIRED,now,npc.preferences.feedback))emit(Kind.TIRED,Items.AIR,now);
        recovering=npc.isRecovering();
        if(shown==Kind.TIRED && !recovering)clearPresentation();
        var active=npc.feedbackReaction();
        if(active!=Reaction.NONE && (!(active.finishing()?safeFinishingPose(active):safePose()) || now-npc.feedbackStarted()>=CompanionFeedbackRules.DURATION))npc.setFeedbackReaction(Reaction.NONE,now);
        if(pending!=Reaction.NONE){
            if(now>=pendingUntil || npc.getRestMode()==CompanionEnergy.Rest.SLEEPING)pending=Reaction.NONE;
            else if(safePose()){npc.setFeedbackReaction(pending,now);pending=Reaction.NONE;}
        }
        if(finish!=null){
            if(now>=finish.until || !rules.ready(finish.kind,now,npc.preferences.feedback) || npc.isRecovering()
                || npc.getRestMode()!=CompanionEnergy.Rest.NONE)finish=null;
            else if(pending==Reaction.NONE && npc.feedbackReaction()==Reaction.NONE && safeFinishingPose(finish.kind.reaction)
                && (npc.workAnimation()!=WorkAnimation.FISH_WAIT || npc.workTarget().equals(finish.fishingWater))){
                var done=finish;finish=null;
                if(rules.admit(done.kind,now,npc.preferences.feedback)){
                    emit(done.kind,done.item,now);pending=Reaction.NONE;
                    npc.setFeedbackReaction(done.kind.reaction,now,done.item);
                }
            }
        }
        if(!viewers.isEmpty() && now%10==0){
            for(var player:PlayerLookup.tracking(npc))if(viewers.contains(player.getUUID()) && (!canView(player) || now>=bubbleUntil)){
                send(player,Kind.NONE,Items.AIR,now,false);viewers.remove(player.getUUID());
            }
            if(now>=bubbleUntil)viewers.clear();
        }
    }
    public boolean canView(ServerPlayer player){return npc.orders.allowed(player) && player.level()==npc.level() && npc.distanceToSqr(player)<=256;}
    private void emit(Kind event,Item icon,long now){
        shown=event;pending=event.reaction;pendingUntil=now+60;bubbleUntil=now+CompanionFeedbackRules.DURATION;
        viewers.clear();
        for(var player:PlayerLookup.tracking(npc))if(canView(player)){
            send(player,event,icon,now,npc.preferences.alerts && event.problem());viewers.add(player.getUUID());
        }
    }
    private void send(ServerPlayer player,Kind event,Item icon,long now,boolean audible){
        if(ServerPlayNetworking.canSend(player,CompanionFeedbackPayload.TYPE))ServerPlayNetworking.send(player,
            new CompanionFeedbackPayload(npc.getId(),npc.getUUID(),event.ordinal(),now,BuiltInRegistries.ITEM.getId(icon),audible));
    }
    void clear(){
        rules.reset();clearPresentation();
    }
    private void clearPresentation(){
        shown=Kind.NONE;pending=Reaction.NONE;finish=null;
        if(npc.feedbackReaction()!=Reaction.NONE)npc.setFeedbackReaction(Reaction.NONE,npc.level().getGameTime());
        if(!viewers.isEmpty())for(var player:PlayerLookup.tracking(npc))if(viewers.contains(player.getUUID()))send(player,Kind.NONE,Items.AIR,npc.level().getGameTime(),false);
        viewers.clear();
    }
    private Item icon(CompanionStatus status,CompanionAssignments.Target target){
        return switch(status){
            case NEEDS_SHEARS -> Items.SHEARS;
            case NEEDS_HOE -> Items.IRON_HOE;
            case NEEDS_ROD -> Items.FISHING_ROD;
            case NEEDS_WEAPON -> Items.IRON_SWORD;
            case NO_BUCKETS,NEEDS_CONTAINERS -> Items.BUCKET;
            case NEEDS_BOTTLES -> Items.GLASS_BOTTLE;
            case FULL -> Items.CHEST;
            case NO_SEEDS -> target==null?Items.WHEAT_SEEDS:Optional.ofNullable(npc.assignments.gardenSeed(target)).orElse(Items.WHEAT_SEEDS);
            case NO_FOOD,NEEDS_BREEDING_FOOD -> Items.WHEAT;
            case NO_INPUT -> CompanionFeedbackItems.missing(npc,target);
            default -> Items.AIR;
        };
    }
}
