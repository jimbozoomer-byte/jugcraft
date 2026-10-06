package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.tower.SeatEntity;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;

/** Adapts player furniture and natural perches without adding block entities to every seat. */
public final class CompanionSeats {
    public static final TagKey<Block> EXTRA_SEATS = TagKey.create(Registries.BLOCK, PeepoMod.id("seats"));
    private record Claim(UUID npc, long expires) {}
    private static final Map<Level, Map<BlockPos, Claim>> CLAIMS = new WeakHashMap<>();
    public static boolean isReserved(Level level, BlockPos pos) {
        var claims=CLAIMS.get(level);
        if(claims==null)return false;
        var claim=claims.get(pos);
        if(claim==null)return false;
        if(claim.expires < level.getGameTime() || !(level instanceof ServerLevel s) || s.getEntity(claim.npc)==null || !s.getEntity(claim.npc).isAlive()) {
            claims.remove(pos); return false;
        }
        return true;
    }
    private static boolean operator(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(Identifier.fromNamespaceAndPath("jugcraft","operator_chair"));
    }
    private static boolean supported(BlockState state) {
        return state.getBlock() instanceof Seat.Sittable || operator(state) || state.is(EXTRA_SEATS)
            || state.getBlock() instanceof CompanionBedBlock || state.getBlock() instanceof FenceBlock
            || state.getBlock() instanceof BedBlock && state.getValue(BlockStateProperties.BED_PART)==BedPart.FOOT;
    }
    public static Surface find(PeepoEntity npc, Map<BlockPos,Long> excluded) {
        List<Surface> candidates=new ArrayList<>();
        for(BlockPos mutable:BlockPos.betweenClosed(npc.blockPosition().offset(-6,-2,-6),npc.blockPosition().offset(6,2,6))) {
            BlockPos pos=mutable.immutable();
            if(!npc.level().hasChunkAt(pos) || excluded.containsKey(pos))continue;
            var state=npc.level().getBlockState(pos);
            if(!supported(state))continue;
            for(Direction facing:Direction.Plane.HORIZONTAL) {
                if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && !(state.getBlock() instanceof BedBlock)
                    && !(state.getBlock() instanceof CompanionBedBlock) && facing!=state.getValue(BlockStateProperties.HORIZONTAL_FACING))continue;
                if(state.getBlock() instanceof CompanionBedBlock && facing!=state.getValue(CompanionBedBlock.FACING).getOpposite())continue;
                var seat=new Surface(npc.level(),pos,state,facing);
                if(seat.availableTo(npc))candidates.add(seat);
            }
        }
        candidates.sort(Comparator.comparingDouble(s->npc.position().distanceToSqr(s.approachPosition())));
        for(var seat:candidates) {
            var path=npc.getNavigation().createPath(BlockPos.containing(seat.approachPosition()),0);
            if(path!=null && path.canReach())return seat;
        }
        return null;
    }
    public static final class Surface implements CompanionStation {
        public final BlockPos pos;
        private final Level level;
        private final BlockState original;
        private final Direction facing;
        private boolean mounted;
        Surface(Level level,BlockPos pos,BlockState state,Direction facing) { this.level=level;this.pos=pos;original=state;this.facing=facing; }
        private boolean edge() { return original.getBlock() instanceof BedBlock || original.getBlock() instanceof CompanionBedBlock; }
        private Vec3 entrance() {
            if(level.getBlockEntity(pos) instanceof CompanionBedEntity bed)return bed.entrance();
            return Vec3.atBottomCenterOf(pos.relative(facing));
        }
        private Vec3 seatPosition() {
            double height=original.getBlock() instanceof Seat.Sittable s ? s.seatHeight(original) : operator(original) ? .56
                : original.getCollisionShape(level,pos).max(Direction.Axis.Y);
            double offset=edge() ? .43 : original.getBlock() instanceof FenceBlock ? 0 : .15;
            return new Vec3(pos.getX()+.5+facing.getStepX()*offset,pos.getY()+height-.094,pos.getZ()+.5+facing.getStepZ()*offset);
        }
        public Kind kind() { return Kind.CHAIR; }
        public Vec3 approachPosition() { return mounted?seatPosition():entrance(); }
        public boolean availableTo(PeepoEntity npc) {
            if(!level.hasChunkAt(pos) || level.getBlockState(pos)!=original || npc.isPassenger())return false;
            if(isReserved(level,pos) && !CLAIMS.get(level).get(pos).npc.equals(npc.getUUID()))return false;
            if(original.hasProperty(BlockStateProperties.OCCUPIED) && original.getValue(BlockStateProperties.OCCUPIED))return false;
            if(level.getBlockEntity(pos) instanceof CompanionBedEntity bed && bed.hasOccupant())return false;
            if(!Seat.at(level,pos).isEmpty() || !level.getEntitiesOfClass(SeatEntity.class,new AABB(pos),e->e.isVehicle()).isEmpty())return false;
            Vec3 entry=entrance(),at=seatPosition();BlockPos floor=BlockPos.containing(entry).below();
            return level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP)
                && level.noCollision(npc,new AABB(entry.x-.22,entry.y,entry.z-.22,entry.x+.22,entry.y+1,entry.z+.22))
                && level.noCollision(npc,new AABB(at.x-.19,at.y+.11,at.z-.19,at.x+.19,at.y+npc.getBbHeight(),at.z+.19));
        }
        public boolean claim(PeepoEntity npc) {
            if(!availableTo(npc))return false;
            CLAIMS.computeIfAbsent(level,l->new HashMap<>()).put(pos,new Claim(npc.getUUID(),level.getGameTime()+240));return true;
        }
        public boolean occupy(PeepoEntity npc) {
            if(!availableTo(npc) || !isReserved(level,pos))return false;
            CLAIMS.get(level).put(pos,new Claim(npc.getUUID(),level.getGameTime()+20));
            if(!npc.setRestMode(CompanionEnergy.Rest.SITTING))return false;
            mounted=true;npc.setNoGravity(true);npc.setBedExit(BlockPos.containing(entrance()));
            Vec3 at=seatPosition();float yaw=facing.toYRot();
            npc.snapTo(at.x,at.y,at.z,yaw,0);npc.yBodyRot=yaw;npc.setYHeadRot(yaw);npc.setDeltaMovement(Vec3.ZERO);npc.resetFallDistance();npc.getNavigation().stop();return true;
        }
        public void release(PeepoEntity npc) {
            var claims=CLAIMS.get(level);var claim=claims==null?null:claims.get(pos);
            if(claim!=null && claim.npc.equals(npc.getUUID()))claims.remove(pos);
            if(mounted) { npc.setRestMode(CompanionEnergy.Rest.NONE);npc.setNoGravity(false);npc.leaveCompanionBed(); }
            mounted=false;
        }
    }
}
