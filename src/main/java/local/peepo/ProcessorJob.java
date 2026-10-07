package local.peepo;

import java.util.UUID;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Exclusive ground-level helper for explicitly enabled standard processors. No worker scans. */
public final class ProcessorJob implements CompanionJob {
    private final MachineBlockEntity machine;
    private UUID worker;
    private long lease, nextEntrance, nextClearance;
    private Vec3 entrance;
    private float checkedHeight;
    private boolean clearance;

    public ProcessorJob(MachineBlockEntity machine) { this.machine=machine; }

    public ProcessorJob prepare(PeepoEntity npc) {
        expire();
        long now=npc.level().getGameTime();
        if(worker==null && now>=nextEntrance) {
            nextEntrance=now+20; nextClearance=0; entrance=null;
            // The controller is at the front corner for compact and enlarged machines.
            // At most eight positions, independent of factory/multiblock size.
            for(var side:Direction.Plane.HORIZONTAL) for(int dy=-1;dy<=0;dy++) {
                var pos=stationPosition().relative(side).offset(0,dy,0);
                var point=new Vec3(stationPosition().getX()+.5+side.getStepX()*.85,
                    pos.getY(),stationPosition().getZ()+.5+side.getStepZ()*.85);
                if(!clearAt(npc,point))continue;
                if(entrance==null || npc.position().distanceToSqr(point)<npc.position().distanceToSqr(entrance))entrance=point;
            }
        }
        return this;
    }
    private boolean clearAt(PeepoEntity npc,Vec3 point) {
        var floor=BlockPos.containing(point).below();
        double radius=npc.getBbWidth()/2+.01;
        var bounds=new AABB(point.x-radius,point.y,point.z-radius,point.x+radius,point.y+npc.getBbHeight(),point.z+radius);
        for(int dx=-1;dx<=1;dx+=2)for(int dz=-1;dz<=1;dz+=2)
            if(!npc.level().hasChunkAt(BlockPos.containing(point.x+dx*radius,point.y,point.z+dz*radius)))return false;
        return npc.level().hasChunkAt(floor) && npc.level().getBlockState(floor).isFaceSturdy(npc.level(),floor,Direction.UP)
            && npc.level().noCollision(bounds);
    }
    private boolean roomFor(PeepoEntity npc) {
        if(entrance==null)return false;
        long now=npc.level().getGameTime();
        if(now>=nextClearance || checkedHeight!=npc.getBbHeight()) {
            nextClearance=now+20;checkedHeight=npc.getBbHeight();clearance=clearAt(npc,entrance);
        }
        return clearance;
    }
    private PeepoEntity occupant() {
        return machine.getLevel() instanceof ServerLevel level && worker!=null
            && level.getEntity(worker) instanceof PeepoEntity npc ? npc : null;
    }
    private void expire() {
        if(worker==null)return;
        var npc=occupant();
        if(machine.isRemoved() || npc==null || !npc.isAlive() || machine.getLevel().getGameTime()>lease)removed();
    }
    public void removed() {
        var npc=occupant();
        if(npc!=null)npc.setWorkAnimation(WorkAnimation.NONE,stationPosition());
        worker=null;machine.resetCompanionEffort();
    }
    public Kind kind(){return Kind.WORK;}
    public BlockPos stationPosition(){return machine.getBlockPos();}
    public Vec3 approachPosition(){return entrance==null?Vec3.atBottomCenterOf(stationPosition().north()):entrance;}
    public boolean availableTo(PeepoEntity npc){expire();return !machine.isRemoved() && (worker==null || worker.equals(npc.getUUID()));}
    public boolean isOccupant(PeepoEntity npc){
        return worker!=null && worker.equals(npc.getUUID()) && npc.isAlive() && npc.level()==machine.getLevel()
            && npc.level().getGameTime()<=lease && entrance!=null && npc.position().distanceToSqr(entrance)<.81;
    }
    public CompanionStatus workStatus(PeepoEntity npc){
        if(!availableTo(npc))return CompanionStatus.OCCUPIED;
        if(machine.isLocked() || !npc.orders.tamed() || !npc.assignments.assignedWork(stationPosition())
            || !CompanionJobs.permitted(npc,stationPosition()))return CompanionStatus.FORBIDDEN;
        if(!roomFor(npc))return CompanionStatus.BLOCKED;
        return machine.companionStatus();
    }
    public boolean claim(PeepoEntity npc){
        if(workStatus(npc)!=CompanionStatus.READY)return false;
        worker=npc.getUUID();lease=npc.level().getGameTime()+100;return true;
    }
    public boolean occupy(PeepoEntity npc){
        if(!availableTo(npc) || !isOccupant(npc) || !roomFor(npc))return false;
        lease=npc.level().getGameTime()+100;
        double dx=stationPosition().getX()+.5-entrance.x,dz=stationPosition().getZ()+.5-entrance.z;
        float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
        // Remain on ordinary solid ground; no no-gravity mount or unload teleport needed.
        if(npc.position().distanceToSqr(entrance)>1.0E-6)npc.snapTo(entrance.x,entrance.y,entrance.z,yaw,0);
        npc.setYRot(yaw);npc.yBodyRot=yaw;npc.setYHeadRot(yaw);npc.getNavigation().stop();
        return true;
    }
    public void release(PeepoEntity npc){if(worker!=null && worker.equals(npc.getUUID()))removed();}
    public CompanionStatus work(PeepoEntity npc){
        if(!npc.preferences.canWork() || !npc.orders.station(this) || !npc.assignments.assignedWork(stationPosition()))return CompanionStatus.IDLE;
        return machine.assistProcessor(npc)?CompanionStatus.WORKING:machine.companionStatus();
    }
}
