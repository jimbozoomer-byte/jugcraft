package io.github.jimbozoomer.jugcraft.tools;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Shared bounded geometry for the laser's authoritative action and cosmetic target preview. */
public final class MiningLaserPattern {
    private MiningLaserPattern() {}
    public static List<BlockPos> targets(BlockPos first,Direction into,Direction face,int mode) {
        var targets=new ArrayList<BlockPos>();mode=Math.floorMod(mode,7);
        if(mode==5||mode==6){
            for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)
                if(mode==6||switch(face.getAxis()){case X->x==0;case Y->y==0;case Z->z==0;})targets.add(first.offset(x,y,z));
        }else for(int i=0;i<(mode==1||mode==4?1:mode==2?16:8);i++)targets.add(first.relative(into,i));
        return List.copyOf(targets);
    }
}
