package local.peepo;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** Each bunk has its own reservation, but all bunks share the bottom ladder entrance. */
public final class CompanionBedEntity extends BlockEntity implements CompanionStation {
    private UUID occupant;
    private long lease;
    private boolean mounted;
    private Vec3 exit;
    public CompanionBedEntity(BlockPos pos, BlockState state) { super(CompanionBeds.ENTITY, pos, state); }
    private Direction facing() { return getBlockState().getValue(CompanionBedBlock.FACING); }
    private PeepoEntity npc() { return level instanceof ServerLevel s && occupant != null && s.getEntity(occupant) instanceof PeepoEntity p ? p : null; }
    private Vec3 entrance() {
        BlockPos bottom = worldPosition;
        while (bottom.getY() > level.getMinY() && CompanionBedBlock.matching(level.getBlockState(bottom.below()), facing())) bottom = bottom.below();
        return Vec3.atBottomCenterOf(bottom.relative(facing().getOpposite()));
    }
    private Vec3 pillow() { return new Vec3(worldPosition.getX() + .5, worldPosition.getY() + .375, worldPosition.getZ() + .5); }
    private boolean clearEntrance() {
        Vec3 entry = entrance();
        BlockPos floor = BlockPos.containing(entry).below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
            && level.noCollision(new AABB(entry.x - .22, entry.y, entry.z - .22, entry.x + .22, worldPosition.getY() + 1.35, entry.z + .22));
    }
    private void expire() {
        if (occupant != null && (level.getGameTime() > lease || npc() == null || !npc().isAlive())) {
            var p = npc(); if (p != null) release(p); else { occupant = null; mounted = false; }
        }
    }
    @Override public Kind kind() { return Kind.BED; }
    @Override public Vec3 approachPosition() { return mounted ? pillow() : entrance(); }
    @Override public boolean availableTo(PeepoEntity p) {
        expire();
        return !isRemoved() && p.isRestNight() && (occupant == null || occupant.equals(p.getUUID())) && clearEntrance();
    }
    @Override public boolean claim(PeepoEntity p) {
        if (!availableTo(p)) return false;
        occupant = p.getUUID(); lease = level.getGameTime() + 240; exit = entrance(); return true;
    }
    @Override public boolean occupy(PeepoEntity p) {
        if (!availableTo(p) || occupant == null || !p.setRestMode(CompanionEnergy.Rest.SLEEPING)) return false;
        mounted = true; lease = level.getGameTime() + 20;
        Vec3 at = pillow(); float yaw = facing().getOpposite().toYRot();
        p.snapTo(at.x, at.y, at.z, yaw, 0); p.yBodyRot = yaw; p.setYHeadRot(yaw);
        p.setDeltaMovement(Vec3.ZERO); p.resetFallDistance(); p.getNavigation().stop();
        p.setBedExit(BlockPos.containing(exit));
        return true;
    }
    @Override public void release(PeepoEntity p) {
        if (occupant == null || !occupant.equals(p.getUUID())) return;
        p.setRestMode(CompanionEnergy.Rest.NONE);
        if (mounted) p.leaveCompanionBed();
        occupant = null; mounted = false; exit = null;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, CompanionBedEntity bed) {
        bed.expire(); var p = bed.npc();
        if (p != null && bed.mounted && (!p.isRestNight() || p.isEating() || !bed.clearEntrance())) bed.release(p);
    }
    @Override public void setRemoved() { var p = npc(); if (p != null) release(p); super.setRemoved(); }
}
