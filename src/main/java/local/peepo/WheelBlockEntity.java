package local.peepo;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import io.github.jimbozoomer.jugcraft.energy.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WheelBlockEntity extends BlockEntity implements CompanionStation {
    public final SimpleEnergyStorage energy=new SimpleEnergyStorage(32000,64,64,this::setChanged);
    public final EnergyStorage output=new EnergyStorage() {
        public boolean supportsInsertion(){return false;}
        public long insert(long n,TransactionContext tx){return 0;}
        public boolean supportsExtraction(){return true;}
        public long extract(long n,TransactionContext tx){return energy.extract(n,tx);}
        public long getAmount(){return energy.getAmount();}
        public long getCapacity(){return energy.getCapacity();}
    };
    private UUID occupant;
    private long lease;
    private boolean mounted;
    public float angle;
    public WheelBlockEntity(BlockPos pos,BlockState state){super(GeneratorWheel.ENTITY,pos,state);}
    public Direction facing(){return getBlockState().getValue(WheelBlock.FACING);}
    private Vec3 point(double x,double y,double z) {
        var right=facing().getClockWise();var back=facing().getOpposite();
        return new Vec3(worldPosition.getX()+.5+right.getStepX()*(x-.5)+back.getStepX()*(z-.5),
            worldPosition.getY()+y,worldPosition.getZ()+.5+right.getStepZ()*(x-.5)+back.getStepZ()*(z-.5));
    }
    private PeepoEntity npc(){return level instanceof ServerLevel s && occupant!=null && s.getEntity(occupant) instanceof PeepoEntity p?p:null;}
    private void expire(){if(occupant!=null && (level.getGameTime()>lease || npc()==null || !npc().isAlive())){var old=npc();if(old!=null)release(old);else{occupant=null;mounted=false;}}}
    public Kind kind(){return Kind.WHEEL;}
    public Vec3 approachPosition(){return mounted?point(1,5/16.0,.5):point(1,0,-.45);}
    public boolean availableTo(PeepoEntity p){expire();return !isRemoved() && (occupant==null || occupant.equals(p.getUUID()));}
    public boolean claim(PeepoEntity p){if(!availableTo(p))return false;occupant=p.getUUID();lease=level.getGameTime()+240;return true;}
    public boolean occupy(PeepoEntity p){
        if(!availableTo(p) || occupant==null)return false;
        lease=level.getGameTime()+20;mounted=true;
        var at=point(1,5/16.0,.5);float yaw=facing().getClockWise().toYRot();
        p.snapTo(at.x,at.y,at.z,yaw,0);p.yBodyRot=yaw;p.setYHeadRot(yaw);p.setDeltaMovement(Vec3.ZERO);p.getNavigation().stop();return true;
    }
    public void release(PeepoEntity p){
        if(occupant==null || !occupant.equals(p.getUUID()))return;
        if(mounted){var at=point(1,0,-.45);p.snapTo(at.x,at.y,at.z,p.getYRot(),0);}
        p.setWheelRunning(false);occupant=null;mounted=false;
    }
    public int energySpace(){return (int)(energy.getCapacity()-energy.getAmount());}
    public int insertEnergy(int n,TransactionContext tx){return (int)energy.insert(n,tx);}
    public boolean working(){var p=npc();return mounted && p!=null && p.isWheelRunning();}
    public static void tick(Level level,BlockPos pos,BlockState state,WheelBlockEntity wheel){
        if(level.isClientSide()){if(state.getValue(WheelBlock.RUNNING))wheel.angle=(wheel.angle+9)%360;return;}
        wheel.expire();
        Direction right=wheel.facing().getClockWise();
        long moved=EnergyNetworks.pushToNeighbors(level,pos,wheel.output,64,List.of(right.getOpposite()));
        EnergyNetworks.pushToNeighbors(level,pos.relative(right),wheel.output,64-moved,List.of(right));
        boolean running=wheel.working();
        if(state.getValue(WheelBlock.RUNNING)!=running)level.setBlock(pos,state.setValue(WheelBlock.RUNNING,running),Block.UPDATE_ALL);
    }
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putLong("Energy",energy.getAmount());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);energy.setAmount(in.getLongOr("Energy",0));}
    @Override public void setRemoved(){var p=npc();if(p!=null)release(p);super.setRemoved();if(level!=null)EnergyNetworks.invalidate(level);}
}
