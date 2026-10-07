package local.peepo;

import java.util.UUID;
import io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A pot owns one short-lived reservation. No ticking worker search or saved entity reference. */
public final class CookingPotJob implements CompanionJob {
    private final CookingPotBlockEntity pot;
    private UUID worker;
    private long lease, nextEntrance;
    private Vec3 entrance;
    private Direction rimSide = Direction.NORTH;
    private boolean mounted;
    private long nextClearance;
    private float checkedHeight;
    private boolean clearance;
    public CookingPotJob(CookingPotBlockEntity pot) { this.pot = pot; }
    public CookingPotJob prepare(PeepoEntity npc) {
        expire();
        if (worker == null && (entrance == null || npc.level().getGameTime() >= nextEntrance)) {
            nextEntrance = npc.level().getGameTime() + 20;
            entrance = null;
            nextClearance = 0;
            for (var side : Direction.Plane.HORIZONTAL) for (int dy = -1; dy <= 0; dy++) {
                var pos = stationPosition().relative(side).offset(0, dy, 0);
                if (!npc.level().hasChunkAt(pos) || !npc.level().getBlockState(pos.below()).isFaceSturdy(npc.level(), pos.below(), Direction.UP)) continue;
                var point = Vec3.atBottomCenterOf(pos);
                if (!npc.level().noCollision(new AABB(point.x-.22, point.y, point.z-.22, point.x+.22, point.y+1, point.z+.22))) continue;
                if (entrance == null || npc.position().distanceToSqr(point) < npc.position().distanceToSqr(entrance)) {
                    entrance = point; rimSide = side;
                }
            }
        }
        return this;
    }
    private PeepoEntity occupant() {
        return pot.getLevel() instanceof ServerLevel level && worker != null && level.getEntity(worker) instanceof PeepoEntity npc ? npc : null;
    }
    private void expire() {
        if (worker == null) return;
        var npc = occupant();
        if (pot.isRemoved() || pot.getLevel().getGameTime() > lease || npc == null || !npc.isAlive()) removed();
    }
    public void removed() {
        var npc = occupant();
        if (npc != null) {
            npc.setWorkAnimation(WorkAnimation.NONE, stationPosition());
            if (mounted) { npc.setNoGravity(false); npc.setDeltaMovement(Vec3.ZERO); npc.leaveCompanionBed(); }
        }
        worker = null; mounted = false;
    }
    public Kind kind() { return Kind.WORK; }
    public BlockPos stationPosition() { return pot.getBlockPos(); }
    public Vec3 approachPosition() {
        var npc = mounted ? occupant() : null;
        return npc != null ? npc.position() : entrance == null ? Vec3.atBottomCenterOf(stationPosition().north()) : entrance;
    }
    public boolean availableTo(PeepoEntity npc) { expire(); return !pot.isRemoved() && (worker == null || worker.equals(npc.getUUID())); }
    public boolean isOccupant(PeepoEntity npc) {
        return worker != null && worker.equals(npc.getUUID()) && npc.level() == pot.getLevel()
            && npc.level().getGameTime() <= lease && (mounted ? stationPosition().distToCenterSqr(npc.position()) < 4 : npc.position().distanceToSqr(approachPosition()) < .81);
    }
    public CompanionStatus workStatus(PeepoEntity npc) {
        if (!availableTo(npc)) return CompanionStatus.OCCUPIED;
        if (pot.isLocked() || !npc.orders.tamed() || !npc.assignments.assignedWork(stationPosition()) || !CompanionJobs.permitted(npc, stationPosition())) return CompanionStatus.FORBIDDEN;
        if (entrance == null || !roomFor(npc)) return CompanionStatus.BLOCKED;
        return pot.cookingStatus();
    }
    private boolean roomFor(PeepoEntity npc) {
        long now=npc.level().getGameTime();
        if(now<nextClearance && checkedHeight==npc.getBbHeight())return clearance;
        nextClearance=now+20;checkedHeight=npc.getBbHeight();
        var pos=stationPosition();
        for(int dx=-1;dx<=1;dx+=2)for(int dz=-1;dz<=1;dz+=2)
            if(!npc.level().hasChunkAt(pos.offset(dx,0,dz)))return clearance=false;
        // Stationary rim position, including Jughead's jug; at most once per second.
        return clearance=npc.level().noCollision(npc.getBoundingBox().move(rimPosition().subtract(npc.position())));
    }
    private Vec3 rimPosition() {
        var pos=stationPosition();
        return new Vec3(pos.getX()+.5+rimSide.getStepX()*WorkAnimation.STIR_RADIUS,
            pos.getY()+WorkAnimation.STIR_HEIGHT,pos.getZ()+.5+rimSide.getStepZ()*WorkAnimation.STIR_RADIUS);
    }
    public boolean claim(PeepoEntity npc) {
        if (workStatus(npc) != CompanionStatus.READY) return false;
        worker = npc.getUUID(); lease = npc.level().getGameTime() + 100; return true;
    }
    public boolean occupy(PeepoEntity npc) {
        if (!isOccupant(npc) || !availableTo(npc)) return false;
        lease = npc.level().getGameTime() + 100;
        var at=rimPosition();
        if(!roomFor(npc))return false;
        if(!mounted)npc.setBedExit(BlockPos.containing(entrance)); // Existing persisted safe-exit recovery also handles a save while stirring.
        mounted=true;npc.setNoGravity(true);npc.resetFallDistance();
        float yaw = rimSide.getOpposite().toYRot();
        // Only correct displacement; spoon motion is client-side and needs no position updates.
        if(npc.position().distanceToSqr(at)>1.0E-8)npc.snapTo(at.x, at.y, at.z, yaw, 0);
        npc.setYRot(yaw); npc.setXRot(0);
        npc.yBodyRot = yaw; npc.setYHeadRot(yaw); npc.setDeltaMovement(Vec3.ZERO); npc.getNavigation().stop();
        return true;
    }
    public void release(PeepoEntity npc) { if (worker != null && worker.equals(npc.getUUID())) removed(); }
    public CompanionStatus work(PeepoEntity npc) {
        if (!npc.preferences.canWork() || !npc.orders.station(this) || !pot.assist(npc)) return pot.cookingStatus();
        return CompanionStatus.WORKING;
    }
    @Override public WorkAnimation animation() { return WorkAnimation.STIR; }
}
