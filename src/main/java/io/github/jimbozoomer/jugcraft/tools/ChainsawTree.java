package io.github.jimbozoomer.jugcraft.tools;

import io.github.jimbozoomer.jugcraft.agriculture.RubberLogBlock;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

/** Conservative, read-only tree recognition shared by powered felling and its cosmetic preview. */
public final class ChainsawTree {
    public static final int READ_LIMIT = 8192, RADIUS = 10, HEIGHT = 48;
    private ChainsawTree() {}

    /** Include the selected log virtually: the server calls this after that log has disappeared. */
    public static List<BlockPos> targets(Level level, BlockPos root, BlockState original) {
        List<BlockPos> single = List.of(root.immutable());
        if (!rawLog(original) || !vertical(original)) return single;
        Scan scan = new Scan(level, root, original);
        var logs = new LinkedHashSet<BlockPos>();
        var seen = new HashSet<BlockPos>();
        var queue = new ArrayDeque<BlockPos>();
        logs.add(root.immutable()); seen.add(root.immutable()); queue.add(root.immutable());
        var leaves = new HashSet<BlockPos>();
        while (!queue.isEmpty()) {
            var from = queue.removeFirst();
            for (BlockPos at : BlockPos.betweenClosed(from.offset(-1,-1,-1),from.offset(1,1,1))) {
                if (!seen.add(at.immutable())) continue;
                BlockState state = scan.get(at);
                if (state == null || construction(state)) return single;
                if (state.is(BlockTags.LOGS)) {
                    // Mixed or worked timber touching the tree makes the whole shape ambiguous.
                    if (!state.is(original.getBlock()) || !rawLog(state)
                        || Math.abs(at.getX()-root.getX())>RADIUS || Math.abs(at.getZ()-root.getZ())>RADIUS
                        || Math.abs(at.getY()-root.getY())>HEIGHT || logs.size()>=ChainsawItem.TREE_LIMIT) return single;
                    var log = at.immutable(); logs.add(log); queue.add(log);
                } else if (livingLeaf(state)) leaves.add(at.immutable());
            }
        }
        int bottom = logs.stream().mapToInt(BlockPos::getY).min().orElse(root.getY());
        int top = logs.stream().mapToInt(BlockPos::getY).max().orElse(root.getY());
        if (top-bottom<2 || top-bottom>HEIGHT) return single;
        var feet = logs.stream().filter(p->p.getY()==bottom).toList();
        int minX=feet.stream().mapToInt(BlockPos::getX).min().orElse(0), maxX=feet.stream().mapToInt(BlockPos::getX).max().orElse(0);
        int minZ=feet.stream().mapToInt(BlockPos::getZ).min().orElse(0), maxZ=feet.stream().mapToInt(BlockPos::getZ).max().orElse(0);
        if (!(feet.size()==1 || feet.size()==4&&maxX-minX==1&&maxZ-minZ==1)) return single;
        for (var foot:feet) {
            BlockState ground=scan.get(foot.below());
            if (ground==null || !(ground.is(BlockTags.DIRT)||ground.is(BlockTags.SAND)||ground.is(BlockTags.NYLIUM)||ground.is(Blocks.END_STONE))) return single;
            for(int y=bottom;y<=bottom+1;y++) {
                BlockPos trunk=new BlockPos(foot.getX(),y,foot.getZ());
                BlockState state=scan.get(trunk);
                if (!logs.contains(trunk)||state==null||!vertical(state)) return single;
            }
        }
        var crown=leaves.stream().filter(p->p.getY()>=bottom+2).toList();
        if (crown.size()<4 || crown.stream().map(BlockPos::getY).distinct().count()<2
            || crown.stream().map(p->new BlockPos(p.getX(),0,p.getZ())).distinct().count()<3
            || crown.stream().mapToInt(BlockPos::getY).max().orElse(bottom)<top-1) return single;
        int crownFloor=crown.stream().mapToInt(BlockPos::getY).min().orElse(top);
        for(var log:logs) {
            boolean trunk=log.getX()>=minX&&log.getX()<=maxX&&log.getZ()>=minZ&&log.getZ()<=maxZ;
            BlockState state=scan.get(log);
            if((log.getY()<=bottom+1||log.getY()<crownFloor-1)&&(!trunk||state==null||!vertical(state))) return single;
            if(!trunk) {
                boolean supported=false;
                for(BlockPos at:BlockPos.betweenClosed(log.offset(-2,-2,-2),log.offset(2,2,2))) {
                    var neighbor=scan.get(at);
                    if(neighbor==null||construction(neighbor))return single;
                    if(livingLeaf(neighbor))supported=true;
                }
                if(!supported)return single;
            }
        }
        // Retain the existing upward-only cut, even when recognition inspected the lower trunk.
        var result=new LinkedHashSet<BlockPos>();result.add(root.immutable());queue.add(root.immutable());
        while(!queue.isEmpty()) {
            var from=queue.removeFirst();
            for(BlockPos at:BlockPos.betweenClosed(from.offset(-1,0,-1),from.offset(1,1,1)))
                if(logs.contains(at)&&result.add(at.immutable()))queue.add(at.immutable());
        }
        return List.copyOf(result);
    }

    private static boolean vertical(BlockState state) {
        return state.hasProperty(BlockStateProperties.AXIS)&&state.getValue(BlockStateProperties.AXIS)==Direction.Axis.Y;
    }

    private static boolean rawLog(BlockState state) {
        if(!state.is(BlockTags.LOGS)||state.hasProperty(RubberLogBlock.NATURAL)&&!state.getValue(RubberLogBlock.NATURAL))return false;
        String path=BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return !path.contains("stripped")&&!path.endsWith("_wood")&&!path.endsWith("_hyphae");
    }

    private static boolean livingLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES)&&state.hasProperty(LeavesBlock.PERSISTENT)&&!state.getValue(LeavesBlock.PERSISTENT)
            &&(!state.hasProperty(LeavesBlock.DISTANCE)||state.getValue(LeavesBlock.DISTANCE)<7);
    }

    private static boolean construction(BlockState state) {
        return state.hasBlockEntity()&&!state.is(Blocks.BEE_NEST) || state.is(BlockTags.PLANKS)
            ||state.is(BlockTags.STAIRS)||state.is(BlockTags.SLABS)||state.is(BlockTags.FENCES)
            ||state.is(BlockTags.FENCE_GATES)||state.is(BlockTags.DOORS)||state.is(BlockTags.TRAPDOORS)
            ||state.is(BlockTags.IMPERMEABLE);
    }

    private static final class Scan {
        private final Level level;
        private final Map<BlockPos,BlockState> states=new HashMap<>();
        private Scan(Level level,BlockPos root,BlockState original) {this.level=level;states.put(root.immutable(),original);}
        private @Nullable BlockState get(BlockPos at) {
            var known=states.get(at);if(known!=null)return known;
            if(states.size()>=READ_LIMIT||!level.hasChunkAt(at))return null;
            var state=level.getBlockState(at);states.put(at.immutable(),state);return state;
        }
    }
}
