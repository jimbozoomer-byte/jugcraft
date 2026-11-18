package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** A saved before/after map. A partially saved custom roof is never surveyed as a new roof to move again. */
record UpperRoofMove(BlockPos origin,List<Cell> cells) {
    static final int EAVE_MARGIN=2, CHIMNEY_MARGIN=16;
    record Cell(BlockPos pos,BlockState before,BlockState after) {
        static final Codec<Cell> CODEC=RecordCodecBuilder.create(i->i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Cell::pos),BlockState.CODEC.fieldOf("before").forGetter(Cell::before),
            BlockState.CODEC.fieldOf("after").forGetter(Cell::after)).apply(i,Cell::new));
    }
    static final Codec<UpperRoofMove> CODEC=RecordCodecBuilder.create(i->i.group(
        BlockPos.CODEC.fieldOf("origin").forGetter(UpperRoofMove::origin),
        Cell.CODEC.listOf().fieldOf("cells").forGetter(UpperRoofMove::cells)).apply(i,UpperRoofMove::new));
    static final class Conflict extends RuntimeException { Conflict(String text) { super(text); } }

    static void checkChunks(ServerLevel level,BlockPos origin,UpperTown.House house) {
        var bounds=house.workBounds();
        var min=origin.offset(bounds.minX(),house.minRoof().getY(),bounds.minZ());
        var max=origin.offset(bounds.maxX(),bounds.maxY(),bounds.maxZ());
        if(level.isOutsideBuildHeight(max)) throw new Conflict("above world build height");
        int buffer=EAVE_MARGIN+1;
        for(int x=(min.getX()-buffer)>>4;x<=(max.getX()+buffer)>>4;x++)
            for(int z=(min.getZ()-buffer)>>4;z<=(max.getZ()+buffer)>>4;z++) {
                if(level.getChunkSource().getChunkNow(x,z)==null) throw new Conflict("visit the building and its eaves first");
                if(x<(min.getX()>>4)||x>(max.getX()>>4)||z<(min.getZ()>>4)||z>(max.getZ()>>4)) continue;
                long key=new ChunkPos(x,z).pack();
                if(!TownState.get(level).built(key)) throw new Conflict("visit the entire building first");
                if(TownUpgrades.version(level,key)<TownUpgrades.CURRENT) throw new Conflict("complete the original town upgrade first");
            }
    }
    private static boolean inFootprint(BlockPos p,UpperTown.House h,int margin) {
        return p.getX()>=h.minRoof().getX()-margin && p.getZ()>=h.minRoof().getZ()-margin
            && p.getX()<=h.maxRoof().getX()+margin && p.getZ()<=h.maxRoof().getZ()+margin;
    }
    private static Conflict conflict(String why,BlockPos world) { return new Conflict(why+" at "+world.toShortString()); }

    static UpperRoofMove prepare(ServerLevel level,BlockPos origin,UpperTown.House h) {
        var wanted=new LinkedHashMap<>(h.changes());
        // An earlier installer may have flushed every chunk immediately before recording completion.
        if(wanted.entrySet().stream().allMatch(e->level.getBlockState(origin.offset(e.getKey()))==e.getValue()))
            return new UpperRoofMove(origin,List.of());
        var firstStop=h.changes().entrySet().stream().filter(e->e.getValue().is(CounterweightLift.BLOCK))
            .map(Map.Entry::getKey).min(Comparator.comparingInt(BlockPos::getY)).orElseThrow();
        for(var shaft=firstStop;shaft.getY()<h.minRoof().getY();shaft=shaft.above())
            wanted.putIfAbsent(shaft,h.baseline(shaft));
        for(var entry:wanted.entrySet()) if(entry.getKey().getY()<h.minRoof().getY()
                && level.getBlockState(origin.offset(entry.getKey()))!=h.baseline(entry.getKey()))
            throw conflict("a lift shaft cell has changed",origin.offset(entry.getKey()));

        var roof=new LinkedHashMap<BlockPos,BlockState>();
        var attached=new ArrayDeque<BlockPos>();
        for(var at:BlockPos.betweenClosed(h.minRoof(),h.maxRoof().below(h.rise()))) {
            var p=at.immutable();
            if(h.baseline(p).isAir()) continue;
            var state=level.getBlockState(origin.offset(p));
            roof.put(p,state); // Air here is a real player-cut skylight or removed roof tile.
            if(!state.isAir()) attached.add(p);
        }
        while(!attached.isEmpty()) {
            var p=attached.removeFirst();
            for(var dir:Direction.values()) {
                var n=p.relative(dir);
                if(n.getY()<h.minRoof().getY() || roof.containsKey(n)) continue;
                var world=origin.offset(n);
                var live=level.getBlockState(world);
                if(live.isAir()) continue;
                if(!h.baseline(n).isAir()) {
                    // All authored blocks of THIS roof were seeded above. Another authored
                    // building is a stationary obstacle, never part of a touching dormer.
                    if(!inFootprint(n,h,0) && h.baseline(p).isAir()) throw conflict("custom roof joins a neighboring structure",world);
                    continue;
                }
                if(!inFootprint(n,h,EAVE_MARGIN) || n.getY()>h.maxRoof().getY()-h.rise()+CHIMNEY_MARGIN)
                    throw conflict("attached build continues beyond the roof survey",world);
                if(!inFootprint(n,h,0) && n.getY()==h.minRoof().getY() && !level.getBlockState(world.below()).isAir())
                    throw conflict("custom eave is supported by a separate structure",world);
                roof.put(n,live); attached.add(n);
            }
        }

        for(var entry:roof.entrySet()) {
            var p=entry.getKey(); var state=entry.getValue();
            if(!state.isAir()) {
                checkFixture(level,origin.offset(p),state);
                for(int i=1;i<=h.rise();i++) {
                    var next=p.above(i); var world=origin.offset(next);
                    if(!roof.containsKey(next) && !level.getBlockState(world).isAir())
                        throw conflict("roof travel is obstructed",world);
                    // Save clear intermediate cells too, so a later edit can stop a resumed move.
                    wanted.putIfAbsent(next,h.baseline(next));
                }
            }
            wanted.putIfAbsent(p,h.baseline(p));
        }
        for(var entry:roof.entrySet()) wanted.put(entry.getKey().above(h.rise()),entry.getValue());

        var cells=new ArrayList<Cell>();
        for(var entry:wanted.entrySet()) {
            var p=entry.getKey(); var world=origin.offset(p);
            var actual=level.getBlockState(world);
            if(!roof.containsKey(p) && actual!=h.baseline(p)) throw conflict("separate build in the work area",world);
            cells.add(new Cell(p,actual,entry.getValue()));
        }
        return new UpperRoofMove(origin,List.copyOf(cells));
    }

    void check(ServerLevel level,BlockPos site,UpperTown.House house) {
        if(!origin.equals(site)) throw new Conflict("pending move belongs to a different town origin");
        var min=origin.offset(house.minRoof()); var max=origin.offset(house.maxRoof());
        for(var p:BlockPos.betweenClosed(min,max)) checkProtected(level,p);
        var chunks=new HashSet<Long>();
        for(var cell:cells) {
            var p=origin.offset(cell.pos);
            if(level.isOutsideBuildHeight(p)) throw conflict("above world build height",p);
            long key=ChunkPos.containing(p).pack();
            if(chunks.add(key) && (!TownState.get(level).built(key)||TownUpgrades.version(level,key)<TownUpgrades.CURRENT))
                throw conflict("visit and upgrade the entire building and its eaves first",p);
            checkProtected(level,p);
            var actual=level.getBlockState(p);
            if(actual!=cell.before && actual!=cell.after) throw conflict("changed since the roof was surveyed",p);
            if(cell.pos.getY()>=house.minRoof().getY()) {
                min=new BlockPos(Math.min(min.getX(),p.getX()),min.getY(),Math.min(min.getZ(),p.getZ()));
                max=new BlockPos(Math.max(max.getX(),p.getX()),Math.max(max.getY(),p.getY()),Math.max(max.getZ(),p.getZ()));
            }
            if(actual==cell.after) continue;
            checkFixture(level,p,actual);
            if(!cell.after.isAir() && occupied(level,new AABB(p))) throw conflict("occupied",p);
        }
        if(occupied(level,new AABB(min.getX()-1,min.getY(),min.getZ()-1,max.getX()+2,max.getY()+2,max.getZ()+2)))
            throw new Conflict("move occupants or hanging decorations away from the roof");
    }
    private static boolean occupied(ServerLevel level,AABB bounds) {
        return !level.getEntities((net.minecraft.world.entity.Entity)null,bounds,e->!e.isSpectator()).isEmpty();
    }
    private static void checkProtected(ServerLevel level,BlockPos p) {
        if(LandRegistry.isClaimed(level,p) || ApartmentRentals.protects(level,p) || OperatorRegions.protects(level,p)
                || TownUpgrades.preservedDecor(level,p)) throw conflict("protected",p);
    }
    private static void checkFixture(ServerLevel level,BlockPos p,BlockState state) {
        if(!state.getFluidState().isEmpty()) throw conflict("drain the wet roof",p);
        if(TownUpgrades.fixtureNearby(level,p)) throw conflict("owner fixture nearby",p);
        var be=level.getBlockEntity(p);
        if(be==null) return;
        if(!(be instanceof CampfireBlockEntity) || !state.is(Blocks.CAMPFIRE)) throw conflict("remove the fixture before moving its roof",p);
        var empty=new CampfireBlockEntity(p,state);
        if(!be.saveWithoutMetadata(level.registryAccess()).equals(empty.saveWithoutMetadata(level.registryAccess())))
            throw conflict("chimney in use",p);
    }
    void apply(ServerLevel level) {
        for(var cell:cells) {
            var pos=origin.offset(cell.pos);
            TownBuilder.set(level,pos,cell.after);
            if(level.getBlockState(pos)!=cell.after) throw conflict("could not place block",pos);
        }
    }
}
