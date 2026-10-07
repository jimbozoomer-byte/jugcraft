package local.peepo;

import java.util.*;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** One physical stack per trip, carried in existing cargo. No remote inventory-to-inventory transfers. */
public final class CompanionTransport extends Goal {
    private final PeepoEntity npc;
    private CompanionAssignments.Target workstation,store;
    private Identifier recipe;
    private ItemStack manifest=ItemStack.EMPTY;
    private int cargoSlot=-1;
    private boolean supply,returning,active;
    private Vec3 approach,lastPosition;
    private long nextSearch,deadline,nextPath;
    private long nextValidity;
    private boolean validRoute;
    private CompanionStatus status=CompanionStatus.IDLE;
    private final Map<BlockPos,Long> blocked=new HashMap<>();
    public CompanionTransport(PeepoEntity npc){this.npc=npc;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    public boolean reserved(int slot){return cargoSlot==slot && !manifest.isEmpty();}
    public CompanionStatus activity(){return active?status:CompanionStatus.IDLE;}
    public CompanionStatus containerStatus(int row){
        return store!=null && store.equals(npc.assignments.get(row)) && (active || !manifest.isEmpty())?status:CompanionStatus.READY;
    }
    private ItemStack carried(){
        if(cargoSlot<0 || cargoSlot>=8 || manifest.isEmpty())return ItemStack.EMPTY;
        var held=npc.belongings.getItem(cargoSlot);
        return ItemStack.isSameItemSameComponents(held,manifest)?held.copyWithCount(Math.min(held.getCount(),manifest.getCount())):ItemStack.EMPTY;
    }
    private boolean linked(){
        if(store==null || workstation==null || !store.equals(npc.assignments.get(supply?CompanionAssignments.SUPPLY:CompanionAssignments.OUTPUT)))return false;
        for(int i=1;i<5;i++)if(workstation.equals(npc.assignments.get(i)))return true;
        return false;
    }
    private boolean allowed(){return npc.orders.tamed() && npc.orders.mode()==CompanionOrders.Mode.WORK.ordinal() && npc.preferences.canWork() && !npc.isEating();}
    private boolean loaded(){return linked() && workstation.present(npc.level()) && store.present(npc.level())
        && CompanionStorage.find(npc,store)!=null && CompanionLogistics.resolve(npc,workstation)!=null;}
    private boolean travelValid(){
        long now=npc.level().getGameTime();
        if(now>=nextValidity){nextValidity=now+10;validRoute=loaded();}
        return validRoute && linked();
    }
    private void forget(){manifest=ItemStack.EMPTY;cargoSlot=-1;workstation=store=null;recipe=null;returning=false;approach=null;}
    private int emptySlot(){for(int i=0;i<8;i++)if(npc.belongings.getItem(i).isEmpty())return i;return -1;}
    private boolean readyRoute(CompanionAssignments.Target work,boolean supplying){
        var bound=npc.assignments.get(supplying?CompanionAssignments.SUPPLY:CompanionAssignments.OUTPUT);
        var port=CompanionLogistics.resolve(npc,work);var storage=CompanionStorage.find(npc,bound);
        if(port==null || storage==null || supplying && port.plan()==null)return false;
        workstation=work;store=bound;supply=supplying;recipe=port.plan();returning=false;
        return !candidate().isEmpty();
    }
    /** A bounded, rolled-back capacity probe. Repeated with fresh endpoints at pickup. */
    private ItemStack candidate(){
        var port=CompanionLogistics.resolve(npc,workstation);var external=CompanionStorage.find(npc,store);
        if(port==null || external==null || supply && !Objects.equals(recipe,port.plan()))return ItemStack.EMPTY;
        var from=supply?external:port.outputs();var to=supply?port.inputs():external;
        if(from==null || to==null)return ItemStack.EMPTY;
        int views=0;
        for(var view:from){
            if(++views>128)break;
            if(view.isResourceBlank() || view.getAmount()<=0)continue;
            var variant=view.getResource();var stack=variant.toStack(1);
            int max=Math.min(32,stack.getMaxStackSize());if(supply)max=Math.min(max,port.needed(stack));
            if(max<=0)continue;
            try(var tx=Transaction.openOuter()){
                int take=(int)view.extract(variant,max,tx);
                int room=(int)to.insert(variant,take,tx);
                if(room>0)return variant.toStack(room);
            }
        }
        return ItemStack.EMPTY;
    }
    @Override public boolean canUse(){
        long now=npc.level().getGameTime();if(!allowed() || now<nextSearch)return false;
        if(!CompanionBudget.search(npc))return false;
        nextSearch=now+80+Math.floorMod(npc.getId(),20);
        if(!manifest.isEmpty()){
            if(!linked() || carried().isEmpty())forget();
            else return loaded();
        }
        forget();if(emptySlot()<0)return false;
        blocked.entrySet().removeIf(e->e.getValue()<=now);
        // Respect workstation priority; clear outputs before stocking the next recipe batch.
        for(int i=1;i<5;i++){
            var work=npc.assignments.get(i);if(work==null || blocked.containsKey(work.at().pos()))continue;
            if(readyRoute(work,false) || readyRoute(work,true))return true;
        }
        forget();return false;
    }
    @Override public boolean canContinueToUse(){return active && allowed() && travelValid() && npc.level().getGameTime()<deadline;}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void start(){
        active=true;npc.resetCompanionRoutine();npc.leaveCompanionBed();npc.setRestMode(CompanionEnergy.Rest.NONE);
        deadline=npc.level().getGameTime()+600;nextPath=nextValidity=0;approach=null;lastPosition=npc.position();
    }
    @Override public void stop(){active=false;npc.getNavigation().stop();approach=null;if(npc.level().getGameTime()>=deadline)nextSearch=npc.level().getGameTime()+200;if(manifest.isEmpty())forget();}
    private CompanionAssignments.Target destination(){return manifest.isEmpty()?(supply?store:workstation):supply && !returning?workstation:store;}
    private void fail(CompanionStatus why){
        status=why;active=false;nextSearch=npc.level().getGameTime()+200;
        if(workstation!=null){if(blocked.size()>=8)blocked.clear();blocked.put(workstation.at().pos(),nextSearch);}
    }
    private void pathTo(CompanionAssignments.Target target){
        if(!target.present(npc.level()) || target.at().pos().distToCenterSqr(npc.position())>64*64){fail(CompanionStatus.UNLOADED);return;}
        var points=new ArrayList<BlockPos>(12);
        for(var side:Direction.Plane.HORIZONTAL)for(int dy=0;dy>=-1;dy--){
            var pos=target.at().pos().relative(side).offset(0,dy,0);var p=Vec3.atBottomCenterOf(pos);var floor=pos.below();
            if(!npc.level().hasChunkAt(pos) || !npc.level().getBlockState(floor).isFaceSturdy(npc.level(),floor,Direction.UP))continue;
            double r=npc.getBbWidth()/2+.01;
            if(npc.level().noCollision(new AABB(p.x-r,p.y,p.z-r,p.x+r,p.y+npc.getBbHeight(),p.z+r)))points.add(pos);
        }
        points.sort(Comparator.comparingDouble(p->p.distToCenterSqr(npc.position())));
        // Explicit 64-block range; the multi-target overload uses the mob's shorter follow range.
        net.minecraft.world.level.pathfinder.Path path=null;
        for(int i=0;i<Math.min(2,points.size());i++){
            if(!CompanionBudget.path(npc)){status=CompanionStatus.WAITING;return;}
            path=npc.getNavigation().createPath(points.get(i),0,64);
            if(path!=null && path.canReach())break;
        }
        nextPath=npc.level().getGameTime()+40;
        if(path==null || !path.canReach()){fail(CompanionStatus.BLOCKED);return;}
        approach=Vec3.atBottomCenterOf(path.getTarget());npc.getNavigation().moveTo(path,1);
    }
    @Override public void tick(){
        if(!allowed() || !travelValid()){fail(CompanionStatus.FORBIDDEN);return;}
        long now=npc.level().getGameTime();
        if(!manifest.isEmpty() && carried().isEmpty()){forget();active=false;return;}
        status=manifest.isEmpty()?(supply?CompanionStatus.FETCHING_SUPPLIES:CompanionStatus.COLLECTING_OUTPUT):returning?CompanionStatus.RETURNING_SUPPLIES:CompanionStatus.DELIVERING;
        var target=destination();
        if(approach==null || npc.position().distanceToSqr(approach)>.64){
            if(now>=nextPath)pathTo(target);
            if(npc.position().distanceToSqr(lastPosition)>.0001)try(var tx=Transaction.openOuter()){npc.extractEnergy(2,tx);tx.commit();}
            lastPosition=npc.position();return;
        }
        npc.getNavigation().stop();
        // Never rely on the travel cache for an inventory mutation.
        if(!loaded()){fail(CompanionStatus.FORBIDDEN);return;}
        if(supply && !manifest.isEmpty() && !returning){
            var port=CompanionLogistics.resolve(npc,workstation);
            if(!Objects.equals(recipe,port.plan()) || port.needed(carried())<=0){returning=true;approach=null;nextPath=0;return;}
        }
        if(target.at().pos().distToCenterSqr(npc.position())>6.25){fail(CompanionStatus.BLOCKED);return;}
        if(manifest.isEmpty())pickup();else deliver();
    }
    private void pickup(){
        var selected=candidate();int slot=emptySlot();
        if(selected.isEmpty() || slot<0){fail(CompanionStatus.NO_INPUT);return;}
        var port=CompanionLogistics.resolve(npc,workstation);
        var source=supply?CompanionStorage.find(npc,store):port.outputs();
        try(var tx=Transaction.openOuter()){
            int taken=(int)source.extract(ItemVariant.of(selected),selected.getCount(),tx);
            if(taken<=0){fail(CompanionStatus.NO_INPUT);return;}
            var stack=selected.copyWithCount(taken);
            if(!npc.food.putCargo(slot,stack,tx)){fail(CompanionStatus.FULL);return;}
            tx.commit();cargoSlot=slot;manifest=stack.copy();
        }
        approach=null;nextPath=0;deadline=npc.level().getGameTime()+600;
    }
    private void deliver(){
        var stack=carried();var port=CompanionLogistics.resolve(npc,workstation);
        Storage<ItemVariant> destination=supply && !returning?port.inputs():CompanionStorage.find(npc,store);
        if(destination==null){fail(CompanionStatus.FORBIDDEN);return;}
        int amount=stack.getCount();if(supply && !returning)amount=Math.min(amount,port.needed(stack));
        if(amount<=0){returning=true;approach=null;nextPath=0;return;}
        int inserted;
        try(var tx=Transaction.openOuter()){
            inserted=(int)destination.insert(ItemVariant.of(stack),amount,tx);
            if(inserted<=0){fail(CompanionStatus.FULL);return;}
            if(npc.food.takeCargo(cargoSlot,stack,inserted,tx)!=inserted)return;
            tx.commit();
        }
        manifest.shrink(inserted);
        if(manifest.isEmpty()){forget();active=false;nextSearch=npc.level().getGameTime()+20;}
        else if(supply && !returning){returning=true;approach=null;nextPath=0;}
        else fail(CompanionStatus.FULL);
    }
    private static void saveTarget(ValueOutput out,String name,CompanionAssignments.Target t){
        var child=out.child(name);child.store("At",GlobalPos.CODEC,t.at());child.store("Block",Identifier.CODEC,t.block());child.putInt("Face",t.face().ordinal());
    }
    private static CompanionAssignments.Target loadTarget(ValueInput in,String name){
        var c=in.child(name);if(c.isEmpty())return null;var at=c.get().read("At",GlobalPos.CODEC).orElse(null);var id=c.get().read("Block",Identifier.CODEC).orElse(null);
        return at==null || id==null?null:new CompanionAssignments.Target(at,id,Direction.values()[Math.clamp(c.get().getIntOr("Face",1),0,5)]);
    }
    public void save(ValueOutput out){
        if(manifest.isEmpty() || workstation==null || store==null)return;
        var c=out.child("Transport");c.store("Manifest",ItemStack.CODEC,manifest);c.putInt("Slot",cargoSlot);c.putBoolean("Supply",supply);c.putBoolean("Returning",returning);
        if(recipe!=null)c.store("Recipe",Identifier.CODEC,recipe);saveTarget(c,"Work",workstation);saveTarget(c,"Store",store);
    }
    public void load(ValueInput in){
        forget();var c=in.child("Transport");if(c.isEmpty())return;
        workstation=loadTarget(c.get(),"Work");store=loadTarget(c.get(),"Store");cargoSlot=c.get().getIntOr("Slot",-1);
        manifest=c.get().read("Manifest",ItemStack.CODEC).orElse(ItemStack.EMPTY);supply=c.get().getBooleanOr("Supply",false);returning=c.get().getBooleanOr("Returning",false);recipe=c.get().read("Recipe",Identifier.CODEC).orElse(null);
        if(workstation==null || store==null || carried().isEmpty())forget();
    }
}
