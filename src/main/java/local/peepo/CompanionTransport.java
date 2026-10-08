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
import io.github.jimbozoomer.jugcraft.agriculture.HearthOvenBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HearthOvenBlockEntity;

/** One physical stack per trip, carried in existing cargo. No remote inventory-to-inventory transfers. */
public final class CompanionTransport extends Goal {
    private final PeepoEntity npc;
    private CompanionAssignments.Target workstation,store;
    private Identifier recipe;
    private ItemStack manifest=ItemStack.EMPTY;
    private int cargoSlot=-1;
    private boolean supply,returning,active,porter;
    private Vec3 approach,lastPosition;
    private long nextSearch,deadline,nextPath;
    private long nextValidity;
    private boolean validRoute;
    private io.github.jimbozoomer.jugcraft.agriculture.HearthOvenBlockEntity tending;
    private long tendingStarted;
    private WorkAnimation pieAction=WorkAnimation.NONE;
    private long pieActionStarted;
    private static final int PIE_ACTION_TICKS=20;
    private CompanionStatus status=CompanionStatus.IDLE;
    private CompanionStatus porterState=CompanionStatus.IDLE;
    private final Map<BlockPos,Long> blocked=new HashMap<>();
    public CompanionTransport(PeepoEntity npc){this.npc=npc;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    boolean hasPendingDelivery(){return !manifest.isEmpty() || tending!=null;}
    public boolean reserved(int slot){return cargoSlot==slot && (tending!=null || !manifest.isEmpty());}
    public CompanionStatus activity(){return active?status:CompanionStatus.IDLE;}
    public CompanionStatus porterStatus(){return npc.assignments.get(CompanionAssignments.SUPPLY)==null || npc.assignments.get(CompanionAssignments.OUTPUT)==null?CompanionStatus.PORTER_SETUP:porterState;}
    public CompanionStatus containerStatus(int row){
        if(npc.orders.porter() && row>=CompanionAssignments.SUPPLY)return porterStatus();
        return store!=null && store.equals(npc.assignments.get(row)) && (active || !manifest.isEmpty())?status:CompanionStatus.READY;
    }
    private ItemStack carried(){
        if(cargoSlot<0 || cargoSlot>=8 || manifest.isEmpty())return ItemStack.EMPTY;
        var held=npc.belongings.getItem(cargoSlot);
        return ItemStack.isSameItemSameComponents(held,manifest)?held.copyWithCount(Math.min(held.getCount(),manifest.getCount())):ItemStack.EMPTY;
    }
    private boolean linked(){
        if(store==null || workstation==null || !store.equals(npc.assignments.get(supply?CompanionAssignments.SUPPLY:CompanionAssignments.OUTPUT)))return false;
        if(porter)return workstation.equals(npc.assignments.get(CompanionAssignments.SUPPLY)) && !workstation.at().equals(store.at());
        for(int i=1;i<5;i++)if(workstation.equals(npc.assignments.get(i)))return true;
        return false;
    }
    private boolean allowed(){return npc.orders.tamed() && (npc.orders.porter() || npc.orders.mode()==CompanionOrders.Mode.WORK.ordinal()) && npc.preferences.canWork() && !npc.isEating();}
    private boolean loaded(){
        if(!linked() || !workstation.present(npc.level()) || !store.present(npc.level()) || CompanionStorage.find(npc,store)==null)return false;
        if(!porter)return CompanionLogistics.resolve(npc,workstation)!=null;
        return CompanionStorage.find(npc,workstation)!=null
            && !CompanionAssignments.canonical(npc.level(),workstation.at().pos()).equals(CompanionAssignments.canonical(npc.level(),store.at().pos()));
    }
    private boolean travelValid(){
        long now=npc.level().getGameTime();
        if(now>=nextValidity){nextValidity=now+10;validRoute=loaded();}
        return validRoute && linked();
    }
    private void releaseTending(){if(tending!=null){tending.releaseTender(npc.getUUID());tending=null;npc.setWorkAnimation(WorkAnimation.NONE,npc.blockPosition());}}
    private boolean hearthRoute(){return !porter && workstation!=null && workstation.block().equals(io.github.jimbozoomer.jugcraft.Jugcraft.id("hearth_oven"));}
    private void clearPiePose(){
        pieAction=WorkAnimation.NONE;npc.setWorkPie(ItemStack.EMPTY);
        if(npc.workAnimation().isPie())npc.setWorkAnimation(WorkAnimation.NONE,npc.blockPosition());
    }
    private void faceOven(){
        var pos=workstation.at().pos();float yaw=(float)Math.toDegrees(Math.atan2(-(pos.getX()+.5-npc.getX()),pos.getZ()+.5-npc.getZ()));
        npc.setYRot(yaw);npc.yBodyRot=yaw;npc.setYHeadRot(yaw);
    }
    private void piePose(WorkAnimation action,ItemStack stack){
        npc.setWorkAnimation(action,workstation.at().pos());npc.setWorkPie(stack);
    }
    /** The load commits after this reach; collection commits before the pull so a ready pie cannot burn during it. */
    private boolean animatePie(WorkAnimation action,ItemStack stack){
        long now=npc.level().getGameTime();
        if(pieAction!=action){pieAction=action;pieActionStarted=now;}
        faceOven();piePose(action,stack);npc.getNavigation().stop();
        return now-pieActionStarted>=PIE_ACTION_TICKS;
    }
    private void forget(){releaseTending();clearPiePose();manifest=ItemStack.EMPTY;cargoSlot=-1;workstation=store=null;recipe=null;returning=porter=false;approach=null;}
    private int emptySlot(){for(int i=0;i<8;i++)if(npc.belongings.getItem(i).isEmpty())return i;return -1;}
    /** A loaded hot pie has a deadline. One helper stays until it can take the result into real cargo. */
    private boolean readyTending(CompanionAssignments.Target work,boolean here){
        if(!npc.assignments.transportAllowed(work,false))return false;
        var port=CompanionLogistics.resolve(npc,work);
        var output=npc.assignments.get(CompanionAssignments.OUTPUT);
        int slot=emptySlot();
        if(port==null || slot<0 || CompanionStorage.find(npc,output)==null
            || !(npc.level().getBlockEntity(work.at().pos()) instanceof io.github.jimbozoomer.jugcraft.agriculture.HearthOvenBlockEntity oven)
            || !oven.needsTending() || !oven.claimTender(npc.getUUID()))return false;
        workstation=work;store=output;supply=returning=false;recipe=port.plan();cargoSlot=slot;
        tending=oven;tendingStarted=0;nextValidity=0;deadline=npc.level().getGameTime()+600;
        if(!here){approach=null;nextPath=0;}
        return true;
    }
    private boolean readyRoute(CompanionAssignments.Target work,boolean supplying){
        if(!npc.assignments.transportAllowed(work,supplying))return false;
        var bound=npc.assignments.get(supplying?CompanionAssignments.SUPPLY:CompanionAssignments.OUTPUT);
        var port=CompanionLogistics.resolve(npc,work);var storage=CompanionStorage.find(npc,bound);
        if(port==null || storage==null)return false;
        if(supplying)port.prepare(storage);
        if(supplying && port.plan()==null)return false;
        workstation=work;store=bound;supply=supplying;recipe=port.plan();returning=false;
        if(work.garden() && !supplying){
            // Harvests are already in cargo: adopt a real stack, even when all eight slots are full.
            for(int i=0;i<8;i++){
                var stack=npc.garden.output(i,work);if(stack.isEmpty())continue;
                int room;
                try(var tx=Transaction.openOuter()){
                    room=(int)storage.insert(ItemVariant.of(stack),stack.getCount(),tx);
                }
                if(room<=0)continue;
                cargoSlot=i;manifest=stack.copyWithCount(room);npc.garden.collected(i,room);return true;
            }
            return false;
        }
        if(emptySlot()<0)return false;
        return !candidate().isEmpty();
    }
    private boolean readyPorter(){
        workstation=npc.assignments.get(CompanionAssignments.SUPPLY);store=npc.assignments.get(CompanionAssignments.OUTPUT);
        porter=true;supply=returning=false;recipe=null;
        if(workstation==null || store==null){porterState=CompanionStatus.PORTER_SETUP;return false;}
        if(!workstation.local(npc.level()) || !store.local(npc.level())){porterState=CompanionStatus.OTHER_DIMENSION;return false;}
        if(!npc.level().hasChunkAt(workstation.at().pos()) || !npc.level().hasChunkAt(store.at().pos())){porterState=CompanionStatus.UNLOADED;return false;}
        if(!workstation.present(npc.level()) || !store.present(npc.level())){porterState=CompanionStatus.MISSING;return false;}
        if(!loaded()){porterState=CompanionStatus.FORBIDDEN;return false;}
        if(workstation.at().pos().distSqr(store.at().pos())>64*64){porterState=CompanionStatus.BLOCKED;return false;}
        if(blocked.containsKey(workstation.at().pos()))return false;
        return !candidate().isEmpty();
    }
    /** A bounded, rolled-back capacity probe. Repeated with fresh endpoints at pickup. */
    private ItemStack candidate(){
        if(!porter && !npc.assignments.transportAllowed(workstation,supply))return ItemStack.EMPTY;
        var port=porter?null:CompanionLogistics.resolve(npc,workstation);var external=CompanionStorage.find(npc,store);
        if((!porter && port==null) || external==null || supply && !Objects.equals(recipe,port.plan()))return ItemStack.EMPTY;
        var from=porter?CompanionStorage.find(npc,workstation):supply?external:port.outputs();var to=supply?port.inputs():external;
        if(from==null || to==null)return ItemStack.EMPTY;
        int views=0;boolean extractable=false;
        for(var view:from){
            if(++views>128)break;
            if(view.isResourceBlank() || view.getAmount()<=0)continue;
            var variant=view.getResource();var stack=variant.toStack(1);
            int max=Math.min(32,stack.getMaxStackSize());if(supply)max=Math.min(max,port.needed(stack));
            if(max<=0)continue;
            try(var scope=io.github.jimbozoomer.jugcraft.machine.MachineItemAutomation.companionTransfer();var tx=Transaction.openOuter()){
                int take=(int)view.extract(variant,max,tx);
                if(take<=0)continue;extractable=true;
                int room=(int)to.insert(variant,take,tx);
                if(room>0){if(porter)porterState=CompanionStatus.READY;return variant.toStack(room);}
            }
        }
        if(porter)porterState=extractable?CompanionStatus.FULL:CompanionStatus.SUPPLY_EMPTY;
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
        forget();if(emptySlot()<0 && npc.orders.porter()){porterState=CompanionStatus.FULL;return false;}
        blocked.entrySet().removeIf(e->e.getValue()<=now);
        if(npc.orders.porter()){if(readyPorter())return true;forget();return false;}
        // Rescue/tend an existing hot pie before ordinary job priority. At most four assigned blocks.
        for(int i=1;i<5;i++){
            var work=npc.assignments.get(i);if(work!=null && !blocked.containsKey(work.at().pos()) && readyTending(work,false))return true;
        }
        // Respect workstation priority; clear outputs before stocking the next recipe batch.
        for(int i=1;i<5;i++){
            var work=npc.assignments.get(i);if(work==null || blocked.containsKey(work.at().pos()))continue;
            if(readyRoute(work,false) || readyRoute(work,true))return true;
        }
        forget();return false;
    }
    @Override public boolean canContinueToUse(){return active && allowed() && (!manifest.isEmpty() || porter==npc.orders.porter()) && travelValid() && npc.level().getGameTime()<deadline;}
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public void start(){
        active=true;npc.resetCompanionRoutine();npc.leaveCompanionBed();npc.setRestMode(CompanionEnergy.Rest.NONE);
        deadline=npc.level().getGameTime()+600;nextPath=nextValidity=0;approach=null;lastPosition=npc.position();
    }
    @Override public void stop(){active=false;clearPiePose();npc.getNavigation().stop();approach=null;if(npc.level().getGameTime()>=deadline)nextSearch=npc.level().getGameTime()+200;if(manifest.isEmpty())forget();}
    private CompanionAssignments.Target destination(){return manifest.isEmpty()?(supply?store:workstation):supply && !returning?workstation:store;}
    private void fail(CompanionStatus why){
        clearPiePose();
        if(porter)porterState=why;
        status=why;active=false;nextSearch=npc.level().getGameTime()+200;
        if(workstation!=null){if(blocked.size()>=8)blocked.clear();blocked.put(workstation.at().pos(),nextSearch);}
    }
    private void pathTo(CompanionAssignments.Target target){
        if(!target.present(npc.level()) || target.at().pos().distToCenterSqr(npc.position())>64*64){fail(CompanionStatus.UNLOADED);return;}
        var points=new ArrayList<BlockPos>(12);
        var targetState=npc.level().getBlockState(target.at().pos());
        boolean oven=targetState.getBlock() instanceof HearthOvenBlock;
        if(target.garden()){
            var floors=new LinkedHashSet<BlockPos>();
            for(var soil:target.plot())if(CompanionGarden.farmland(npc.level(),soil) && CompanionJobs.permitted(npc,soil)){
                floors.add(soil);for(var side:Direction.Plane.HORIZONTAL)floors.add(soil.relative(side));
            }
            for(var floor:floors){
                if(!npc.level().hasChunkAt(floor))continue;
                var state=npc.level().getBlockState(floor);
                double top=state.getBlock() instanceof net.minecraft.world.level.block.FarmlandBlock?.9375:state.isFaceSturdy(npc.level(),floor,Direction.UP)?1:0;
                if(top==0)continue;
                var p=new Vec3(floor.getX()+.5,floor.getY()+top,floor.getZ()+.5);double r=npc.getBbWidth()/2+.01;
                if(npc.level().noCollision(new AABB(p.x-r,p.y+.001,p.z-r,p.x+r,p.y+npc.getBbHeight(),p.z+r)))points.add(floor.above());
            }
        }
        for(var side:Direction.Plane.HORIZONTAL)for(int dy=0;dy>=-1;dy--){
            // The pie must enter the mouth, with a standing surface at the oven's level.
            if(oven && (side!=targetState.getValue(HearthOvenBlock.FACING) || dy!=0))continue;
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
        approach=Vec3.atBottomCenterOf(path.getTarget());
        if(oven){var facing=targetState.getValue(HearthOvenBlock.FACING);approach=approach.add(-facing.getStepX()*.24,0,-facing.getStepZ()*.24);}
        npc.getNavigation().moveTo(path,1);
    }
    @Override public void tick(){
        if(!allowed() || !travelValid()){fail(CompanionStatus.FORBIDDEN);return;}
        long now=npc.level().getGameTime();
        if(tending!=null && (tending.isRemoved() || !tending.claimTender(npc.getUUID()))){fail(CompanionStatus.OCCUPIED);return;}
        if(!manifest.isEmpty() && carried().isEmpty()){forget();active=false;return;}
        if(!porter && !npc.assignments.transportAllowed(workstation,supply)){
            if(manifest.isEmpty()){forget();active=false;return;}
            if(supply && !returning){clearPiePose();returning=true;approach=null;nextPath=0;}
            // Already collected outputs finish their delivery; unused supplies return to their source.
        }
        if(pieAction==WorkAnimation.PIE_TAKE){
            if(!animatePie(WorkAnimation.PIE_TAKE,carried()))return;
            clearPiePose();approach=null;nextPath=0;
        }
        var visibleCargo=carried();
        if(pieAction!=WorkAnimation.PIE_LOAD && hearthRoute() && !visibleCargo.isEmpty()
            && (HearthOvenBlockEntity.selectedFilling(visibleCargo)!=null || (visibleCargo.is(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item("burnt_pie")) || visibleCargo.is(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item("burnt_cake")))))
            piePose(WorkAnimation.PIE_CARRY,visibleCargo);
        status=manifest.isEmpty()?(supply || porter?CompanionStatus.FETCHING_SUPPLIES:CompanionStatus.COLLECTING_OUTPUT):returning?CompanionStatus.RETURNING_SUPPLIES:CompanionStatus.DELIVERING;
        if(porter)porterState=status;
        var target=destination();
        if(approach==null || npc.position().distanceToSqr(approach)>.64){
            if(pieAction==WorkAnimation.PIE_LOAD)clearPiePose();
            if(now>=nextPath)pathTo(target);
            if(npc.position().distanceToSqr(lastPosition)>.0001)try(var tx=Transaction.openOuter()){npc.extractEnergy(2,tx);tx.commit();}
            lastPosition=npc.position();return;
        }
        npc.getNavigation().stop();
        if(hearthRoute() && target.equals(workstation) && npc.position().distanceToSqr(approach)>.0225){
            if(pieAction==WorkAnimation.PIE_LOAD)clearPiePose();
            npc.getMoveControl().setWantedPosition(approach.x,approach.y,approach.z,.7);return;
        }
        if(tending!=null){
            if(target.at().pos().distToCenterSqr(npc.position())>6.25){fail(CompanionStatus.BLOCKED);return;}
            tendAtWork();return;
        }
        // Never rely on the travel cache for an inventory mutation.
        if(!loaded()){fail(CompanionStatus.FORBIDDEN);return;}
        if(supply && !manifest.isEmpty() && !returning){
            var port=CompanionLogistics.resolve(npc,workstation);
            if(!Objects.equals(recipe,port.plan()) || port.needed(carried())<=0){clearPiePose();returning=true;approach=null;nextPath=0;return;}
        }
        boolean near=target.garden()?target.plot().stream().anyMatch(p->CompanionGarden.farmland(npc.level(),p) && CompanionJobs.permitted(npc,p) && p.distToCenterSqr(npc.position())<=6.25):target.at().pos().distToCenterSqr(npc.position())<=6.25;
        if(!near){fail(CompanionStatus.BLOCKED);return;}
        if(hearthRoute() && supply && !returning && HearthOvenBlockEntity.rawFilling(carried())!=null){
            if(!animatePie(WorkAnimation.PIE_LOAD,carried()))return;
        }
        if(manifest.isEmpty())pickup();else deliver();
    }
    private void tendAtWork(){
        long now=npc.level().getGameTime();
        if(!tending.needsTending()){forget();active=false;nextSearch=now+20;return;}
        if(tendingStarted==0){tendingStarted=now;deadline=now+800;}
        status=CompanionStatus.WORKING;
        faceOven();piePose(WorkAnimation.PIE_WAIT,ItemStack.EMPTY);
        var outputs=tending.companionOutputs();
        for(var view:outputs){
            if(view.isResourceBlank() || view.getAmount()==0)continue;
            // Waiting uses the ten-tick route cache; collection rechecks access immediately.
            if(!loaded()){fail(CompanionStatus.FORBIDDEN);return;}
            int slot=cargoSlot>=0 && npc.belongings.getItem(cargoSlot).isEmpty()?cargoSlot:emptySlot();
            if(slot<0){fail(CompanionStatus.FULL);return;}
            var variant=view.getResource();var stack=variant.toStack(1);
            try(var tx=Transaction.openOuter()){
                if(view.extract(variant,1,tx)!=1 || !npc.food.putCargo(slot,stack,tx))return;
                if(npc.extractEnergy(16,tx)<=0){fail(CompanionStatus.RECOVERING);return;}
                tx.commit();cargoSlot=slot;manifest=stack.copy();
            }
            releaseTending();animatePie(WorkAnimation.PIE_TAKE,carried());approach=null;nextPath=0;deadline=now+600;return;
        }
    }
    private void pickup(){
        var selected=candidate();int slot=emptySlot();
        if(selected.isEmpty() || slot<0){fail(slot<0?CompanionStatus.FULL:porter?porterState:CompanionStatus.NO_INPUT);return;}
        var port=porter?null:CompanionLogistics.resolve(npc,workstation);
        var source=porter?CompanionStorage.find(npc,workstation):supply?CompanionStorage.find(npc,store):port.outputs();
        try(var scope=io.github.jimbozoomer.jugcraft.machine.MachineItemAutomation.companionTransfer();var tx=Transaction.openOuter()){
            int taken=(int)source.extract(ItemVariant.of(selected),selected.getCount(),tx);
            if(taken<=0){fail(CompanionStatus.NO_INPUT);return;}
            var stack=selected.copyWithCount(taken);
            if(!npc.food.putCargo(slot,stack,tx)){fail(CompanionStatus.FULL);return;}
            if(!porter && !supply && npc.level().getBlockEntity(workstation.at().pos()) instanceof io.github.jimbozoomer.jugcraft.agriculture.HearthOvenBlockEntity
                && npc.extractEnergy(16,tx)<=0){fail(CompanionStatus.RECOVERING);return;}
            tx.commit();cargoSlot=slot;manifest=stack.copy();
        }
        approach=null;nextPath=0;deadline=npc.level().getGameTime()+600;
        if(hearthRoute() && !supply)animatePie(WorkAnimation.PIE_TAKE,carried());
    }
    private void deliver(){
        var stack=carried();var port=porter?null:CompanionLogistics.resolve(npc,workstation);
        Storage<ItemVariant> destination=supply && !returning?port.inputs():CompanionStorage.find(npc,store);
        if(destination==null){fail(CompanionStatus.FORBIDDEN);return;}
        int amount=stack.getCount();if(supply && !returning)amount=Math.min(amount,port.needed(stack));
        if(amount<=0){returning=true;approach=null;nextPath=0;return;}
        if(supply && !returning && workstation.garden()){
            // Pickup already put these seeds into this companion's inventory. Do not insert a second copy.
            forget();active=false;nextSearch=npc.level().getGameTime()+20;return;
        }
        int inserted;
        try(var scope=io.github.jimbozoomer.jugcraft.machine.MachineItemAutomation.companionTransfer();var tx=Transaction.openOuter()){
            inserted=(int)destination.insert(ItemVariant.of(stack),amount,tx);
            if(inserted<=0){fail(CompanionStatus.FULL);return;}
            if(npc.food.takeCargo(cargoSlot,stack,inserted,tx)!=inserted)return;
            tx.commit();
        }
        manifest.shrink(inserted);
        clearPiePose();
        if(manifest.isEmpty()){
            if(porter)porterState=CompanionStatus.READY;
            if(supply && !returning && readyTending(workstation,true))return;
            forget();active=false;nextSearch=npc.level().getGameTime()+20;
        }
        else if(supply && !returning){returning=true;approach=null;nextPath=0;}
        else fail(CompanionStatus.FULL);
    }
    private static void saveTarget(ValueOutput out,String name,CompanionAssignments.Target t){
        var child=out.child(name);child.store("At",GlobalPos.CODEC,t.at());child.store("Block",Identifier.CODEC,t.block());child.putInt("Face",t.face().ordinal());
        if(t.garden())child.store("Plot",BlockPos.CODEC.listOf(),t.plot());
    }
    private static CompanionAssignments.Target loadTarget(ValueInput in,String name){
        var c=in.child(name);if(c.isEmpty())return null;var at=c.get().read("At",GlobalPos.CODEC).orElse(null);var id=c.get().read("Block",Identifier.CODEC).orElse(null);
        return at==null || id==null?null:new CompanionAssignments.Target(at,id,Direction.values()[Math.clamp(c.get().getIntOr("Face",1),0,5)],c.get().read("Plot",BlockPos.CODEC.listOf()).orElse(List.of()));
    }
    public void save(ValueOutput out){
        if(manifest.isEmpty() || workstation==null || store==null)return;
        var c=out.child("Transport");c.store("Manifest",ItemStack.CODEC,manifest);c.putInt("Slot",cargoSlot);c.putBoolean("Supply",supply);c.putBoolean("Returning",returning);
        c.putBoolean("Porter",porter);
        if(recipe!=null)c.store("Recipe",Identifier.CODEC,recipe);saveTarget(c,"Work",workstation);saveTarget(c,"Store",store);
    }
    public void load(ValueInput in){
        forget();var c=in.child("Transport");if(c.isEmpty())return;
        workstation=loadTarget(c.get(),"Work");store=loadTarget(c.get(),"Store");cargoSlot=c.get().getIntOr("Slot",-1);
        manifest=c.get().read("Manifest",ItemStack.CODEC).orElse(ItemStack.EMPTY);supply=c.get().getBooleanOr("Supply",false);returning=c.get().getBooleanOr("Returning",false);recipe=c.get().read("Recipe",Identifier.CODEC).orElse(null);
        porter=c.get().getBooleanOr("Porter",false);if(porter){supply=returning=false;recipe=null;}
        if(workstation==null || store==null || carried().isEmpty())forget();
    }
}
