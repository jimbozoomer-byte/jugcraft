package local.peepo;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.*;

/** Bounded, staggered food searches; no chunk loading and no inventory reservation. */
final class FindLunchGoal extends Goal {
    private final PeepoEntity npc;
    private LunchBlockEntity lunch;
    private Vec3 approach;
    private long nextSearch,deadline;
    private int repath,cursor;
    FindLunchGoal(PeepoEntity npc){this.npc=npc;nextSearch=npc.level().getGameTime()+Math.floorMod(npc.getId(),80);setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    public boolean requiresUpdateEveryTick(){return true;}
    private boolean valid(){return lunch!=null && npc.level().hasChunkAt(lunch.getBlockPos())
        && npc.level().getBlockEntity(lunch.getBlockPos())==lunch && lunch.feeds(npc)
        && npc.orders.food(Vec3.atCenterOf(lunch.getBlockPos()));}
    public boolean canUse(){
        long now=npc.level().getGameTime();
        if(!npc.orders.tamed() || !npc.needsAutomaticFood() || npc.isEating() || now<nextSearch)return false;
        nextSearch=now+80+Math.floorMod(npc.getId(),20);
        List<LunchBlockEntity> candidates=new ArrayList<>();
        var origin=npc.blockPosition();
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
            for(var pos:CompanionStationIndex.positions(npc.level(),origin.offset(dx*16,0,dz*16)))
                if(pos.distToCenterSqr(npc.position())<=256 && npc.orders.food(Vec3.atCenterOf(pos))
                    && npc.level().getBlockEntity(pos) instanceof LunchBlockEntity source && source.feeds(npc))candidates.add(source);
        candidates.sort(Comparator.comparingDouble(b->b.getBlockPos().distToCenterSqr(npc.position())));
        int paths=0;
        for(int n=0;n<Math.min(4,candidates.size());n++){
            var source=candidates.get(Math.floorMod(cursor++,candidates.size()));
            if(source.takeMeal(npc,false).isEmpty())continue;
            List<BlockPos> points=new ArrayList<>();
            for(Direction d:Direction.Plane.HORIZONTAL)for(int dy=0;dy>=-1;dy--){
                var pos=source.sourcePos().relative(d).offset(0,dy,0);var at=Vec3.atBottomCenterOf(pos);
                if(npc.level().hasChunkAt(pos) && npc.orders.food(at)
                    && npc.level().getBlockState(pos.below()).isFaceSturdy(npc.level(),pos.below(),Direction.UP)
                    && npc.level().noCollision(npc,new AABB(at.x-.22,at.y,at.z-.22,at.x+.22,at.y+1,at.z+.22)))points.add(pos);
            }
            points.sort(Comparator.comparingDouble(p->p.distToCenterSqr(npc.position())));
            for(var pos:points){
                if(paths++>=2)return false;
                var path=npc.getNavigation().createPath(pos,0);
                if(path!=null && path.canReach()){lunch=source;approach=Vec3.atBottomCenterOf(pos);return true;}
            }
        }
        return false;
    }
    public boolean canContinueToUse(){return valid() && npc.needsAutomaticFood() && !npc.isEating() && npc.level().getGameTime()<deadline;}
    public void start(){npc.resetCompanionRoutine();npc.leaveCompanionBed();deadline=npc.level().getGameTime()+200;repath=0;}
    public void stop(){lunch=null;approach=null;npc.getNavigation().stop();}
    public void tick(){
        if(!valid())return;
        if(npc.position().distanceToSqr(approach)<=.64){
            var meal=lunch.takeMeal(npc,true);
            if(!meal.isEmpty())npc.beginLunchMeal(meal,GlobalPos.of(npc.level().dimension(),lunch.getBlockPos()));
            deadline=0;return;
        }
        if(--repath<=0){repath=40;npc.getNavigation().moveTo(approach.x,approach.y,approach.z,1);}
    }
}
