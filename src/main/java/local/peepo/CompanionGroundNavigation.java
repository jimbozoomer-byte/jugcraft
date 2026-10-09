package local.peepo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;

/** Normal ground evaluation, admitting closed usable gates only for companions. */
final class CompanionGroundNavigation extends GroundPathNavigation {
    CompanionGroundNavigation(Mob mob,Level level){super(mob,level);}
    @Override protected PathFinder createPathFinder(int range){
        nodeEvaluator=new WalkNodeEvaluator(){
            @Override public PathType getPathType(PathfindingContext context,int x,int y,int z){
                var original=super.getPathType(context,x,y,z);
                if(original==PathType.FENCE && mob instanceof PeepoEntity npc){
                    var pos=new BlockPos(x,y,z);
                    if(CompanionGates.mayOpen(npc,pos,context.getBlockState(pos)))return PathType.WALKABLE;
                }
                return original;
            }
        };
        nodeEvaluator.setCanPassDoors(true);return new PathFinder(nodeEvaluator,range);
    }
}
