package local.peepo;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Transient furniture presentation. Reservations, collision and energy remain authoritative. */
public final class CompanionRestMotion {
    public enum Clip { NONE, SIT, SLEEP, STAND, WAKE;
        public boolean exit(){return this==STAND || this==WAKE;}
        public boolean bed(){return this==SLEEP || this==WAKE;}
        public int ticks(){return bed()?24:this==SIT?14:12;}
    }
    private static final EntityDataAccessor<Integer> CLIP=PeepoEntity.REST_CLIP;
    private static final EntityDataAccessor<Long> START=PeepoEntity.REST_START;
    private static final EntityDataAccessor<BlockPos> ANCHOR=PeepoEntity.REST_ANCHOR;
    private static final EntityDataAccessor<Vector3fc> MOUNT=PeepoEntity.REST_MOUNT;
    private static final EntityDataAccessor<Vector3fc> ENTRY=PeepoEntity.REST_ENTRY;
    private static final EntityDataAccessor<Float> YAW=PeepoEntity.REST_YAW;
    private static final EntityDataAccessor<Integer> COLOR=PeepoEntity.REST_COLOR;
    private static final EntityDataAccessor<Integer> CLIMB=PeepoEntity.REST_CLIMB;
    private final PeepoEntity npc;
    private Block original;
    private boolean pendingExit;
    private boolean exitClaim;
    private BlockPos climbClaim,secondary;
    private long climbUntil;
    public CompanionRestMotion(PeepoEntity npc){this.npc=npc;}
    static void define(SynchedEntityData.Builder b){b.define(CLIP,0);b.define(START,0L);b.define(ANCHOR,BlockPos.ZERO);b.define(MOUNT,new Vector3f());b.define(ENTRY,new Vector3f());b.define(YAW,0F);b.define(COLOR,-1);b.define(CLIMB,0);}
    public Clip clip(){return Clip.values()[Math.clamp(npc.getEntityData().get(CLIP),0,Clip.values().length-1)];}
    public long started(){return npc.getEntityData().get(START);}
    public BlockPos anchor(){return npc.getEntityData().get(ANCHOR);}
    private Vec3 point(EntityDataAccessor<Vector3fc> key){var v=npc.getEntityData().get(key);return Vec3.atLowerCornerOf(anchor()).add(v.x(),v.y(),v.z());}
    public Vec3 mount(){return point(MOUNT);}
    public Vec3 entry(){return point(ENTRY);}
    public float yaw(){return npc.getEntityData().get(YAW);}
    public int color(){return npc.getEntityData().get(COLOR);}
    public int climbTicks(){return npc.getEntityData().get(CLIMB);}
    public int duration(){return clip().ticks()+climbTicks();}
    public boolean exiting(){return clip().exit() && npc.level().getGameTime()-started()<duration();}
    public boolean holdingExit(){return pendingExit || exiting();}
    private Vector3f relative(Vec3 at){return at.subtract(Vec3.atLowerCornerOf(anchor())).toVector3f();}
    public static int bedColor(Block block){
        if(block instanceof BedBlock bed)return bed.getColor().getId();
        int i=CompanionBeds.BLOCKS.indexOf(block);return i<0?-1:DyeColor.values()[i].getId();
    }
    void enter(boolean bed,BlockPos at,Vec3 entrance,Vec3 mount,float yaw){
        if(npc.level().isClientSide())return;
        cancel();secondary=null;
        var d=npc.getEntityData();original=npc.level().getBlockState(at).getBlock();pendingExit=false;
        d.set(ANCHOR,at.immutable());d.set(MOUNT,relative(mount));d.set(ENTRY,relative(entrance));d.set(YAW,yaw);
        d.set(COLOR,bed?bedColor(original):-1);
        int rise=bed && original instanceof CompanionBedBlock?(int)Math.max(0,Math.round(mount.y-entrance.y-.375)):0;
        d.set(CLIMB,rise==0?0:Math.clamp(rise*14,14,160));d.set(START,npc.level().getGameTime());d.set(CLIP,(bed?Clip.SLEEP:Clip.SIT).ordinal());
        if(original instanceof BedBlock)secondary=at.relative(npc.level().getBlockState(at).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING).getOpposite());
        if(rise>0){climbClaim=BlockPos.containing(entrance);climbUntil=npc.level().getGameTime()+climbTicks()+6;CompanionSeats.reserveExternal(npc,climbClaim,climbTicks()+8);}
    }
    void secondary(BlockPos pos){secondary=pos;}
    void released(){
        pendingExit=!clip().exit() && clip()!=Clip.NONE && npc.level().getGameTime()-started()>=duration();
        if(!pendingExit)cancel();
        else{exitClaim=true;CompanionSeats.reserveExternal(npc,anchor(),duration()+40);if(secondary!=null)CompanionSeats.reserveExternal(npc,secondary,duration()+40);}
    }
    void exited(Vec3 safeExit){
        if(!pendingExit)return;
        pendingExit=false;
        if(!npc.isAlive() || npc.editingSettings() || npc.isEating() || !npc.level().hasChunkAt(anchor()) || npc.level().getBlockState(anchor()).getBlock()!=original){cancel();return;}
        // A fallback exit can be safe without sharing the checked animation corridor.
        if(safeExit.distanceToSqr(entry())>.1){cancel();return;}
        // A newly occupied access column is an interruption, never permission to overlap a climber.
        if(climbTicks()>0 && !CompanionSeats.externalAvailable(npc,BlockPos.containing(safeExit))){cancel();return;}
        var d=npc.getEntityData();d.set(ENTRY,relative(safeExit));d.set(START,npc.level().getGameTime());d.set(CLIP,(clip().bed()?Clip.WAKE:Clip.STAND).ordinal());
        exitClaim=true;CompanionSeats.reserveExternal(npc,anchor(),duration()+5);if(secondary!=null)CompanionSeats.reserveExternal(npc,secondary,duration()+5);
        if(climbTicks()>0 && CompanionSeats.externalAvailable(npc,BlockPos.containing(safeExit))){climbClaim=BlockPos.containing(safeExit);climbUntil=npc.level().getGameTime()+duration();CompanionSeats.reserveExternal(npc,climbClaim,duration()+5);}
        npc.getNavigation().stop();npc.setDeltaMovement(Vec3.ZERO);
    }
    public void cancel(){if(!npc.level().isClientSide()){
        if(exitClaim){CompanionSeats.releaseExternal(npc,anchor());if(secondary!=null)CompanionSeats.releaseExternal(npc,secondary);exitClaim=false;}
        if(climbClaim!=null){CompanionSeats.releaseExternal(npc,climbClaim);climbClaim=null;}
        pendingExit=false;npc.getEntityData().set(CLIP,0);
    }}
    void tick(){
        if(clip()==Clip.NONE)return;
        if(climbClaim!=null && npc.level().getGameTime()>=climbUntil){CompanionSeats.releaseExternal(npc,climbClaim);climbClaim=null;}
        if(!npc.isAlive() || npc.editingSettings() || npc.isEating() || npc.isPassenger() || npc.isCompanionUnloading() || npc.isInLiquid()) {cancel();return;}
        if(pendingExit){
            if(npc.level().getGameTime()%20==0){
                if(!npc.level().hasChunkAt(anchor()) || npc.level().getBlockState(anchor()).getBlock()!=original){cancel();return;}
                CompanionSeats.reserveExternal(npc,anchor(),duration()+40);if(secondary!=null)CompanionSeats.reserveExternal(npc,secondary,duration()+40);
            }
            npc.getNavigation().stop();npc.setDeltaMovement(Vec3.ZERO);return;
        }
        if(clip().exit()){
            if(npc.level().getGameTime()%5==0 && (!npc.level().hasChunkAt(anchor()) || npc.level().getBlockState(anchor()).getBlock()!=original)){cancel();return;}
            if(!exiting() || npc.position().distanceToSqr(entry())>.5){cancel();return;}
            npc.getNavigation().stop();npc.setDeltaMovement(Vec3.ZERO);
        }else if(npc.getRestMode()==CompanionEnergy.Rest.NONE && !pendingExit)cancel();
    }
}
