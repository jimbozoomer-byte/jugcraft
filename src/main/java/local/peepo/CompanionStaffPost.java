package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.tower.SeatEntity;
import io.github.jimbozoomer.jugcraft.tower.Sittable;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.joml.Vector3f;

/** One explicitly assigned, nonproductive post. Surface probes and pose changes are bounded. */
public final class CompanionStaffPost {
    public static final int AUTOMATIC=0, STANDING=1, SITTING=2;
    private final PeepoEntity npc;
    private Job current;
    private long nextPose;
    private long nextGreeting;
    private BlockPos exitClaim;
    public CompanionStaffPost(PeepoEntity npc){this.npc=npc;}
    static void define(SynchedEntityData.Builder b){
        b.define(PeepoEntity.STAFF_POSE,0);b.define(PeepoEntity.STAFF_PREVIOUS,0);
        b.define(PeepoEntity.STAFF_STARTED,0L);b.define(PeepoEntity.STAFF_OFFSET,new Vector3f());
        b.define(PeepoEntity.STAFF_ANCHOR,BlockPos.ZERO);b.define(PeepoEntity.STAFF_POINT,new Vector3f());
        b.define(PeepoEntity.STAFF_GREETING,-1L);
    }
    public int pose(){return Math.clamp(npc.getEntityData().get(PeepoEntity.STAFF_POSE),0,3);}
    public int previousPose(){return Math.clamp(npc.getEntityData().get(PeepoEntity.STAFF_PREVIOUS),0,2);}
    public long started(){return npc.getEntityData().get(PeepoEntity.STAFF_STARTED);}
    public Vec3 offset(){var p=npc.getEntityData().get(PeepoEntity.STAFF_OFFSET);return new Vec3(p.x(),p.y(),p.z());}
    public Vec3 target(){var p=npc.getEntityData().get(PeepoEntity.STAFF_POINT);return Vec3.atLowerCornerOf(npc.getEntityData().get(PeepoEntity.STAFF_ANCHOR)).add(p.x(),p.y(),p.z());}
    public long greetingStarted(){return npc.getEntityData().get(PeepoEntity.STAFF_GREETING);}
    public int duration(){return 20;}
    public boolean active(){return pose()==STANDING || pose()==SITTING;}
    public boolean sitting(){return pose()==SITTING;}
    public boolean transitioning(){return pose()!=0 && npc.level().getGameTime()-started()<duration();}
    public boolean exiting(){return pose()==3 && transitioning();}
    private void presentation(int next,Vec3 from,Vec3 to){
        var delta=from.subtract(to);
        if(!Double.isFinite(delta.lengthSqr()) || delta.lengthSqr()>16)delta=Vec3.ZERO;
        var data=npc.getEntityData();data.set(PeepoEntity.STAFF_PREVIOUS,active()?pose():0);
        var anchor=BlockPos.containing(to);data.set(PeepoEntity.STAFF_ANCHOR,anchor);data.set(PeepoEntity.STAFF_POINT,to.subtract(Vec3.atLowerCornerOf(anchor)).toVector3f());
        data.set(PeepoEntity.STAFF_OFFSET,delta.toVector3f());data.set(PeepoEntity.STAFF_STARTED,npc.level().getGameTime());
        data.set(PeepoEntity.STAFF_POSE,next);
        data.set(PeepoEntity.STAFF_GREETING,-1L);
    }
    void clear(){
        if(npc.level().isClientSide())return;
        if(exitClaim!=null){CompanionSeats.releaseExternal(npc,exitClaim);exitClaim=null;}
        npc.getEntityData().set(PeepoEntity.STAFF_POSE,0);
        npc.getEntityData().set(PeepoEntity.STAFF_OFFSET,new Vector3f());
        npc.getEntityData().set(PeepoEntity.STAFF_GREETING,-1L);
    }
    /** Tracking removal may not relocate an entity between sections. */
    void unload(){if(current!=null)current.release(npc);current=null;clear();}
    void loaded(){
        if(current!=null){CompanionSeats.releaseExternal(npc,current.stationPosition());current.mounted=false;current=null;}
        clear();
    }
    void tick(){
        if(pose()==0)return;
        if(!npc.isAlive() || npc.isCompanionUnloading()){clear();return;}
        if(pose()==3){
            if(!exiting() || npc.editingSettings() || npc.isEating() || npc.isPassenger()){clear();return;}
            npc.getNavigation().stop();npc.setDeltaMovement(Vec3.ZERO);
        }
    }
    public CompanionJob job(BlockPos pos){
        for(int row=1;row<=4;row++){
            var t=npc.assignments.get(row);
            if(t==null || !t.staffPost() || !t.local(npc.level()) || !t.at().pos().equals(pos))continue;
            if(current==null || !current.target.equals(t)){if(current!=null)current.release(npc);current=new Job(t);}
            return current;
        }
        return null;
    }
    public record Surface(BlockPos position,BlockState blockState,Direction facing,Vec3 standing,Vec3 seated,Vec3 entrance,boolean canSit){}
    private static boolean furniture(BlockState state,PeepoEntity npc,BlockPos pos){
        if(npc.level().getBlockEntity(pos) instanceof io.github.jimbozoomer.jugcraft.furniture.DesignedFurnitureEntity designed)
            return designed.design().seat()>0 && !designed.design().storage();
        if(state.getBlock() instanceof Seat.Sittable s)return s.seatHeight(state)>0;
        if(state.getBlock() instanceof Sittable s)return s.seatHeight()>0;
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals("jugcraft:operator_chair");
    }
    /** Largest center-supported deck; chair backs and narrow posts cannot become a standing surface. */
    private static double deck(BlockState state,PeepoEntity npc,BlockPos at){
        var shape=state.getCollisionShape(npc.level(),at);var boxes=shape.toAabbs();if(boxes.isEmpty() || boxes.size()>128)return -1;
        double top=-1,r=Math.max(.20,npc.getBbWidth()/2+.005);
        var heights=new TreeSet<Double>(Comparator.reverseOrder());
        for(var b:boxes)if(b.maxY>0 && b.maxY<=1.001)heights.add(b.maxY);
        for(double height:heights){
            // Optimized stool shapes may partition the tabletop across its four legs.
            // The union must support both feet, not one particular decomposed box.
            var feet=Shapes.create(new AABB(.5-r,height-.0001,.5-r,.5+r,height-.000001,.5+r));
            if(!Shapes.joinIsNotEmpty(feet,shape,BooleanOp.ONLY_FIRST)){top=height;break;}
        }
        return top;
    }
    private static AABB body(PeepoEntity npc,Vec3 at,boolean seated){
        double r=npc.getBbWidth()/2+.005;
        return new AABB(at.x-r,at.y+(seated?.11:.001),at.z-r,at.x+r,at.y+npc.getBbHeight(),at.z+r);
    }
    private static boolean clearBody(PeepoEntity npc,Vec3 at,boolean seated){
        var box=body(npc,at,seated);
        for(var p:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))
            if(!CompanionJobs.permitted(npc,p))return false;
        return CompanionHazards.safeAt(npc,at) && npc.level().noCollision(npc,box);
    }
    private static boolean corridor(PeepoEntity npc,Vec3 entry,Vec3 mount){
        double lift=Math.max(entry.y,mount.y)+.08;
        if(!clearSweep(npc,entry,new Vec3(entry.x,lift,entry.z))
            || !clearSweep(npc,mount,new Vec3(mount.x,lift,mount.z)))return false;
        for(int step=0;step<=4;step++){
            double t=step/4.0;
            var at=new Vec3(entry.x+(mount.x-entry.x)*t,lift,entry.z+(mount.z-entry.z)*t);
            if(!CompanionJobs.permitted(npc,BlockPos.containing(at)) || !clearBody(npc,at,false))return false;
        }
        return true;
    }
    private static boolean clearSweep(PeepoEntity npc,Vec3 from,Vec3 to){
        var box=body(npc,from,false).expandTowards(to.subtract(from));
        for(var p:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))
            if(!CompanionJobs.permitted(npc,p) || CompanionHazards.harmful(npc,npc.level().getBlockState(p)))return false;
        return npc.level().noCollision(npc,box);
    }
    private static Vec3 entrance(PeepoEntity npc,BlockPos pos,Direction facing,Vec3 mount){
        for(var side:new Direction[]{facing,facing.getClockWise(),facing.getCounterClockWise(),facing.getOpposite()})
            for(int dy:new int[]{0,1,-1}){
                BlockPos floor=pos.relative(side).offset(0,dy-1,0);
                if(!npc.level().hasChunkAt(floor) || !npc.level().hasChunkAt(floor.above(2)))continue;
                var state=npc.level().getBlockState(floor);if(!state.getFluidState().isEmpty())continue;
                // Use the same grounded exit contract as beds and ordinary companion seats.
                double top=deck(state,npc,floor);if(Math.abs(top-1)>.001 || !state.isFaceSturdy(npc.level(),floor,Direction.UP))continue;
                var at=new Vec3(floor.getX()+.5,floor.getY()+top,floor.getZ()+.5);
                if(CompanionJobs.permitted(npc,floor) && CompanionJobs.permitted(npc,BlockPos.containing(at)) && clearBody(npc,at,false) && corridor(npc,at,mount))return at;
            }
        return null;
    }
    public static Surface surface(PeepoEntity npc,BlockPos pos,Direction facing){
        return surface(npc,pos,facing,facing);
    }
    private static Surface surface(PeepoEntity npc,BlockPos pos,Direction facing,Direction entranceFacing){
        if(facing.getAxis().isVertical() || !npc.level().hasChunkAt(pos) || !npc.level().hasChunkAt(pos.above(2)))return null;
        var state=npc.level().getBlockState(pos);
        if(state.isAir() || !state.getFluidState().isEmpty() || CompanionHazards.harmful(npc,state)
            || state.getBlock() instanceof FenceBlock || state.getBlock() instanceof WallBlock || state.getBlock() instanceof FenceGateBlock)return null;
        double top=deck(state,npc,pos);if(top<0)return null;
        var stand=new Vec3(pos.getX()+.5,pos.getY()+top,pos.getZ()+.5);
        if(!clearBody(npc,stand,false))return null;
        Vec3 entry=entrance(npc,pos,entranceFacing,stand);if(entry==null)return null;
        // The front of the deck supports the hips; the short legs hang beyond it.
        double extent=.5;
        var shape=state.getShape(npc.level(),pos);
        if(!shape.isEmpty())extent=facing.getAxisDirection()==Direction.AxisDirection.POSITIVE?shape.max(facing.getAxis())-.5:.5-shape.min(facing.getAxis());
        double edge=Math.max(.20,Math.min(.49,extent-.015));
        var seat=stand.add(facing.getStepX()*edge,-.094,facing.getStepZ()*edge);
        boolean canSit=furniture(state,npc,pos) && clearBody(npc,seat,true);
        return new Surface(pos.immutable(),state,facing,stand,seat,entry,canSit);
    }
    public static boolean supportsSitting(PeepoEntity npc,BlockPos pos,Direction facing){var s=surface(npc,pos,facing);return s!=null && s.canSit;}
    public final class Job implements CompanionJob {
        private final CompanionAssignments.Target target;
        private Surface surface;
        private boolean mounted;
        private int approachTurn;
        private long nextValidation;
        private boolean valid;
        Job(CompanionAssignments.Target target){this.target=target;}
        public Kind kind(){return Kind.WORK;}
        public BlockPos stationPosition(){return target.at().pos();}
        public Vec3 approachPosition(){
            if(surface==null)surface=findSurface();
            return surface==null?Vec3.atBottomCenterOf(stationPosition()):mounted?(sitting()?surface.seated:surface.standing):surface.entrance;
        }
        public WorkAnimation animation(){return WorkAnimation.NONE;}
        private Surface findSurface(){var side=target.face();for(int i=0;i<approachTurn;i++)side=side.getClockWise();return CompanionStaffPost.surface(npc,stationPosition(),target.face(),side);}
        public void approachFailed(PeepoEntity who){if(who==npc && !mounted){approachTurn=(approachTurn+1)%4;surface=null;nextValidation=0;}}
        private boolean assigned(){
            for(int row=1;row<=4;row++)if(target.equals(npc.assignments.get(row)))return true;
            return false;
        }
        private boolean playerOccupied(){
            return !Seat.at(npc.level(),stationPosition()).isEmpty()
                || !npc.level().getEntitiesOfClass(SeatEntity.class,new AABB(stationPosition()),e->e.isVehicle()).isEmpty();
        }
        public boolean availableTo(PeepoEntity who){
            if(who!=npc || !assigned() || !target.present(npc.level()) || !CompanionJobs.permitted(npc,stationPosition()) || npc.isPassenger() || exiting()
                || !CompanionSeats.externalAvailable(npc,stationPosition()))return false;
            if(surface!=null && mounted && npc.level().getBlockState(stationPosition())!=surface.blockState)return false;
            var state=npc.level().getBlockState(stationPosition());
            if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.OCCUPIED)
                && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OCCUPIED)
                || npc.level().getBlockEntity(stationPosition()) instanceof CompanionBedEntity bed && bed.hasOccupant())return false;
            long now=npc.level().getGameTime();if(now<nextValidation)return valid;
            nextValidation=now+(mounted?20:5);valid=false;
            var candidate=findSurface();
            if(candidate==null || (target.staffPose()==SITTING || mounted && sitting()) && !candidate.canSit)return false;
            if(mounted && (!candidate.standing.equals(surface.standing) || sitting() && !candidate.seated.equals(surface.seated)))return false;
            if(playerOccupied())return false;
            surface=candidate;return valid=true;
        }
        public boolean claim(PeepoEntity who){
            nextValidation=0; // A cached planning result is never permission to take a newly occupied seat.
            if(!availableTo(who))return false;
            CompanionSeats.reserveExternal(npc,stationPosition(),240);return true;
        }
        public boolean occupy(PeepoEntity who){
            if(!mounted)nextValidation=0; // Fresh geometry and player-seat checks at the actual mount.
            if(!availableTo(who) || !CompanionSeats.isReserved(npc.level(),stationPosition()))return false;
            CompanionSeats.reserveExternal(npc,stationPosition(),80);
            if(!mounted){
                npc.restMotion.cancel();npc.social.cancel();npc.feedback.clear();
                mounted=true;setPose(target.staffPose()==SITTING?SITTING:STANDING);
                nextGreeting=npc.level().getGameTime()+200+npc.getRandom().nextInt(401);
            }
            npc.setNoGravity(true);npc.getNavigation().stop();npc.setDeltaMovement(Vec3.ZERO);npc.resetFallDistance();
            CompanionMotion.position(npc,sitting()?surface.seated:surface.standing,target.face().toYRot());
            return true;
        }
        private void setPose(int pose){
            var at=pose==SITTING?surface.seated:surface.standing;
            presentation(pose,npc.position(),at);
            npc.restMotion.cancel();npc.setRestMode(pose==SITTING?CompanionEnergy.Rest.SITTING:CompanionEnergy.Rest.NONE);
            npc.setNoGravity(true);npc.setBedExit(BlockPos.containing(surface.entrance));
            CompanionMotion.position(npc,at,target.face().toYRot());
            nextPose=npc.level().getGameTime()+600+npc.getRandom().nextInt(1201);
        }
        public CompanionStatus workStatus(PeepoEntity who){
            if(!CompanionJobs.permitted(npc,stationPosition()))return CompanionStatus.FORBIDDEN;
            if(!availableTo(who))return CompanionStatus.BLOCKED;
            if(!npc.preferences.canWork())return CompanionStatus.RECOVERING;
            if(npc.transport!=null && npc.transport.hasPendingDelivery())return CompanionStatus.WAITING;
            return CompanionStatus.READY;
        }
        public CompanionStatus work(PeepoEntity who){
            var status=workStatus(who);if(status!=CompanionStatus.READY)return status;
            if(!mounted)return CompanionStatus.WAITING;
            if(target.staffPose()==AUTOMATIC && surface.canSit && npc.level().getGameTime()>=nextPose){
                nextValidation=0; // Pose changes must use fresh space and full-body claim checks.
                if(!availableTo(who))return CompanionStatus.BLOCKED;
                if(surface.canSit)setPose(sitting()?STANDING:SITTING);
            }
            long now=npc.level().getGameTime();
            if(!npc.preferences.social)npc.getEntityData().set(PeepoEntity.STAFF_GREETING,-1L);
            else if(!transitioning() && now>=nextGreeting && CompanionBudget.social(npc)){
                nextGreeting=now+1200+npc.getRandom().nextInt(601);
                // The already-known owner is enough for a greeting; no nearby-entity scan.
                var owner=npc.orders.ownerPlayer();
                if(owner!=null && owner.isAlive() && !owner.isSpectator() && npc.distanceToSqr(owner)<=36 && npc.hasLineOfSight(owner))
                    npc.getEntityData().set(PeepoEntity.STAFF_GREETING,now);
            }
            return CompanionStatus.WORKING;
        }
        public void release(PeepoEntity who){
            if(who!=npc)return;
            CompanionSeats.releaseExternal(npc,stationPosition());
            nextValidation=0;valid=false;
            if(mounted){
                var from=npc.position();int previous=pose();
                npc.restMotion.cancel();npc.setRestMode(CompanionEnergy.Rest.NONE);npc.setNoGravity(false);
                mounted=false;
                if(!npc.isCompanionUnloading())npc.leaveCompanionBed();
                if(npc.isAlive() && !npc.isCompanionUnloading() && !npc.editingSettings() && !npc.isEating()
                    && npc.level().hasChunkAt(stationPosition()) && npc.level().getBlockState(stationPosition())==surface.blockState
                    && npc.position().distanceToSqr(surface.entrance)<.01 && corridor(npc,surface.entrance,surface.standing)
                    && from.distanceToSqr(npc.position())>.001){
                    presentation(3,from,npc.position());npc.getEntityData().set(PeepoEntity.STAFF_PREVIOUS,Math.clamp(previous,1,2));
                    exitClaim=stationPosition();CompanionSeats.reserveExternal(npc,exitClaim,duration()+5);
                }else clear();
            }
            surface=null;
        }
    }
}
