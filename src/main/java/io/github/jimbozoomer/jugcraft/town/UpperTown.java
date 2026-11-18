package io.github.jimbozoomer.jugcraft.town;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Successive, independently saved additions. Each stage moves the roof that is actually on the house. */
public final class UpperTown {
    public record House(String id, BlockPos minRoof, BlockPos maxRoof, Map<BlockPos, BlockState> changes,
            House base, int rise, BoundingBox workBounds) {
        public House(String id, BlockPos min, BlockPos max, Map<BlockPos, BlockState> changes) {
            this(id,min,max,changes,null,5,bounds(min,max,changes));
        }

        private static BoundingBox bounds(BlockPos min,BlockPos max,Map<BlockPos,BlockState> changes) {
            var box=BoundingBox.fromCorners(min,max);
            for(var p:changes.keySet()) box.encapsulate(p);
            return box;
        }

        BlockState baseline(BlockPos p) {
            if(base!=null) {
                var state=base.changes.get(p);
                if(state!=null) return state;
            }
            var town=TownData.get();
            return town.state(town.index(p.getX(),p.getY(),p.getZ()));
        }
    }
    private UpperTown() {}
    private record Stages(List<House> houses,List<House> jetties) {}
    private static final class Plans { static final Stages ALL=read(); }
    public static List<House> houses() { return Plans.ALL.houses; }
    public static List<House> jetties() { return Plans.ALL.jetties; }

    private static Stages read() {
        try (var stream=UpperTown.class.getResourceAsStream("/data/jugcraft/town/old_town_upper_floors.json.gz");
                var reader=new InputStreamReader(new GZIPInputStream(Objects.requireNonNull(stream)),StandardCharsets.UTF_8)) {
            var plan=JsonParser.parseReader(reader).getAsJsonObject();
            var palette=new ArrayList<BlockState>();
            for(var value:plan.getAsJsonArray("palette")) palette.add(WesternDistrictPlan.parse(value.getAsString()));
            var houses=new LinkedHashMap<String,House>();
            var jetties=new ArrayList<House>();
            for(String stage:List.of("houses","jetties")) for(var value:plan.getAsJsonArray(stage)) {
                var house=value.getAsJsonObject();
                var roof=house.getAsJsonArray("roof");
                var changes=new LinkedHashMap<BlockPos,BlockState>();
                for(var entry:house.getAsJsonArray("cells")) {
                    var c=entry.getAsJsonArray();
                    var pos=new BlockPos(c.get(0).getAsInt(),c.get(1).getAsInt(),c.get(2).getAsInt());
                    changes.put(pos,palette.get(c.get(3).getAsInt()));
                }
                var min=new BlockPos(roof.get(0).getAsInt(),roof.get(1).getAsInt(),roof.get(2).getAsInt());
                var max=new BlockPos(roof.get(3).getAsInt(),roof.get(4).getAsInt(),roof.get(5).getAsInt());
                String id=house.get("id").getAsString();
                var cells=Collections.unmodifiableMap(changes);
                if(house.has("base")) {
                    var base=Objects.requireNonNull(houses.get(house.get("base").getAsString()));
                    jetties.add(new House(id,min,max,cells,base,house.get("rise").getAsInt(),House.bounds(min,max,cells)));
                } else houses.put(id,new House(id,min,max,cells));
            }
            return new Stages(List.copyOf(houses.values()),List.copyOf(jetties));
        } catch(Exception e) { throw new IllegalStateException("Cannot read the Old Town upper floors",e); }
    }

    static void buildNewChunk(ServerLevel level,BlockPos origin,ChunkPos chunk) {
        buildStage(level,origin,chunk,houses());
        if(TownState.get(level).newJettyFloors()) buildStage(level,origin,chunk,jetties());
    }

    private static void buildStage(ServerLevel level,BlockPos origin,ChunkPos chunk,List<House> stage) {
        for(var house:stage) {
            var bounds=house.workBounds;
            if(chunk.x()<(origin.getX()+bounds.minX()>>4) || chunk.x()>(origin.getX()+bounds.maxX()>>4)
                    || chunk.z()<(origin.getZ()+bounds.minZ()>>4) || chunk.z()>(origin.getZ()+bounds.maxZ()>>4)) continue;
            for(var entry:house.changes.entrySet()) {
                var pos=origin.offset(entry.getKey());
                if(ChunkPos.containing(pos).equals(chunk)) TownBuilder.set(level,pos,entry.getValue());
            }
        }
    }

    public static int retrofit(CommandSourceStack source,boolean apply) {
        var level=source.getServer().overworld();
        var town=TownState.get(level); var origin=town.origin();
        if(origin==null) { source.sendFailure(Component.literal("No original town has been placed.")); return 0; }
        if(town.newJettyFloors()) {
            source.sendSuccess(()->Component.literal("The raised houses and timber galleries are included as this town's chunks are built."),false);
            return 1;
        }
        int first=town.newUpperFloors()?0:retrofitStage(source,level,origin,town,houses(),apply);
        if(first<0) return 0;
        int second=retrofitStage(source,level,origin,town,jetties(),apply);
        return second<0?0:first+second;
    }

    private static int retrofitStage(CommandSourceStack source,ServerLevel level,BlockPos origin,
            TownState town,List<House> stage,boolean apply) {
        var jobs=new LinkedHashMap<House,UpperRoofMove>();
        for(var house:stage) {
            if(house.base!=null && !town.newUpperFloors() && !town.raised(house.base.id)) {
                source.sendSuccess(()->Component.literal(house.id+" — surveyed after its first upper storey is installed"),false);
                continue;
            }
            if(town.raised(house.id)) {
                source.sendSuccess(()->Component.literal(house.id+" — already raised"),false);
                continue;
            }
            try {
                var move=plan(level,origin,house);
                jobs.put(house,move);
                if(!apply) source.sendSuccess(()->Component.literal(house.id+" — ready"),false);
            } catch(UpperRoofMove.Conflict e) {
                source.sendSuccess(()->Component.literal(house.id+" — "+e.getMessage()),false);
            }
        }
        if(apply && !jobs.isEmpty()) {
            try { install(level,jobs); }
            catch(UpperRoofMove.Conflict e) {
                source.sendFailure(Component.literal("Roof installation paused: "+e.getMessage()+". Run upperfloors apply to resume."));
                return -1;
            }
            for(var house:jobs.keySet()) source.sendSuccess(()->Component.literal(house.id+" — upper floors and lift landings installed"),false);
        }
        return jobs.size();
    }

    private static UpperRoofMove plan(ServerLevel level,BlockPos origin,House house) {
        UpperRoofMove.checkChunks(level,origin,house);
        var recorded=TownState.get(level).pendingRoof(house.id);
        var move=recorded==null?UpperRoofMove.prepare(level,origin,house):recorded;
        move.check(level,origin,house);
        return move;
    }

    private static void install(ServerLevel level,Map<House,UpperRoofMove> jobs) {
        var town=TownState.get(level);
        jobs.forEach((house,move)->town.stageRoof(house.id,move));
        // Freeze the live states and their exact destinations before changing the first block.
        level.getDataStorage().saveAndJoin();
        for(var move:jobs.values()) move.apply(level);
        if(!level.getServer().saveEverything(true,true,true))
            throw new UpperRoofMove.Conflict("world save did not complete");
        jobs.keySet().forEach(house->town.markRaised(house.id));
        level.getDataStorage().saveAndJoin();
    }

    public static String retrofitHouse(ServerLevel level,BlockPos origin,House house,boolean apply) {
        if(TownState.get(level).raised(house.id)) return null;
        try {
            var move=plan(level,origin,house);
            if(apply) install(level,Map.of(house,move));
            return null;
        } catch(UpperRoofMove.Conflict e) { return e.getMessage(); }
    }
}
