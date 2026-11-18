package io.github.jimbozoomer.jugcraft.town;

import com.google.gson.Gson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import org.slf4j.LoggerFactory;

/** Opt-in, bounded, reversible town expansion. No lifecycle hook starts an installation. */
public final class WesternDistrict {
    private static final Gson JSON=new Gson();
    private static final Map<ServerLevel,Job> JOBS=new WeakHashMap<>();
    private static final int BUDGET=2048;
    private WesternDistrict() {}
    private static final class Record {
        String hash="", token="", phase="absent", note="";
        long origin; int cursor; boolean started, waterfront;
        String guildHash="";
        boolean openingGuilds;
        int[] bounds;
    }
    private static final class Job {
        final ServerLevel level; final Path root; Record record=new Record();
        WesternDistrictPlan plan; BlockState[] before; int[] restoreOrder; boolean running; int scan, savedCursor;
        CommandSourceStack source;
        Job(ServerLevel level) {
            this.level=level;
            root=level.getServer().getWorldPath(LevelResource.ROOT).resolve("data/jugcraft-western-district");
            try {
                if(Files.exists(root.resolve("status.json"))) {
                    record=JSON.fromJson(Files.readString(root.resolve("status.json")),Record.class);
                    if(record==null || record.phase==null || record.hash==null) throw new IllegalStateException("Invalid district record");
                }
            } catch(Exception e) { throw new IllegalStateException("Cannot read western installation record; refusing operations",e); }
        }
        void save() throws Exception {
            Files.createDirectories(root); Path tmp=root.resolve("status.json.tmp");
            Files.writeString(tmp,JSON.toJson(record),StandardCharsets.UTF_8);
            WesternDistrictJournal.durableMove(tmp,root.resolve("status.json"));
            savedCursor=record.cursor;
        }
        BlockPos origin() { return BlockPos.of(record.origin); }
        void say(String text) {
            LoggerFactory.getLogger("jugcraft-western-district").info("{}",text);
            if(source!=null) source.sendSuccess(()->Component.literal(text),false);
        }
        void stop(String why) throws Exception { running=false; record.note=why; save(); say(why); }
    }
    private static Job job(ServerLevel level) { return JOBS.computeIfAbsent(level,Job::new); }
    public record Progress(String phase, boolean running, int cursor, String surveyToken, String note) {}
    public static Progress progress(ServerLevel level) {
        var j=job(level); return new Progress(j.record.phase,j.running,j.record.cursor,j.record.token,j.record.note);
    }
    static String populatedInstallation(ServerLevel level) {
        var j=job(level);
        return j.record.waterfront && "installed".equals(j.record.phase)?j.record.token:null;
    }
    static boolean guildShopsOpen(ServerLevel level) {
        var record=job(level).record;
        return record.waterfront && "installed".equals(record.phase) && !record.openingGuilds
            && DeepsteadShopPlan.get().hash.equals(record.guildHash);
    }
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var j=JOBS.get(server.overworld()); if(j==null || !j.running) return;
            try { tick(j); } catch(Exception e) {
                j.running=false; j.record.note="Paused: "+e.getMessage();
                try { j.save(); } catch(Exception disk) { e.addSuppressed(disk); }
                j.say(j.record.note); LoggerFactory.getLogger("jugcraft-western-district").error("Western operation paused; before-image retained",e);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> JOBS.remove(server.overworld()));
    }
    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("expansion").requires(s->Commands.LEVEL_OWNERS.check(s.permissions()))
            .executes(c->status(c.getSource()))
            .then(Commands.literal("survey").executes(c->survey(c.getSource(),false))
                .then(Commands.literal("waterfront").executes(c->survey(c.getSource(),true))))
            .then(Commands.literal("pause").executes(c->operate(c.getSource(),"pause","")))
            .then(Commands.literal("guildshops").executes(c->guildShops(c.getSource(),false))
                .then(Commands.literal("apply").executes(c->guildShops(c.getSource(),true))))
            .then(Commands.literal("apply").then(Commands.argument("surveyHash",StringArgumentType.word())
                .executes(c->operate(c.getSource(),"apply",StringArgumentType.getString(c,"surveyHash")))))
            .then(Commands.literal("resume").then(Commands.argument("surveyHash",StringArgumentType.word())
                .executes(c->operate(c.getSource(),"resume",StringArgumentType.getString(c,"surveyHash")))))
            .then(Commands.literal("rollback").then(Commands.argument("surveyHash",StringArgumentType.word())
                .executes(c->operate(c.getSource(),"rollback",StringArgumentType.getString(c,"surveyHash")))));
    }
    private static int status(CommandSourceStack source) {
        var j=job(source.getServer().overworld());
        source.sendSuccess(()->Component.literal("Western district: "+j.record.phase+"; "+(j.running?"working":"idle")
            +"; cell "+j.record.cursor+"; "+j.record.note+"; survey "+j.record.token),false); return 1;
    }
    private static int fail(CommandSourceStack source,String message) { source.sendFailure(Component.literal(message)); return 0; }
    private static WesternDistrictPlan plan(boolean waterfront) {
        return waterfront?WesternDistrictPlan.waterfront():WesternDistrictPlan.get();
    }
    private static int survey(CommandSourceStack source,boolean waterfront) {
        return survey(source,waterfront,waterfront);
    }
    static int survey(CommandSourceStack source,boolean waterfront,boolean includeGuildShops) {
        var j=job(source.getServer().overworld());
        if(j.running || j.record.started && !j.record.phase.equals("rolled_back")) return fail(source,"Pause or finish/rollback the existing operation first. Its before-image will not be overwritten.");
        var origin=Town.origin(j.level); if(origin==null) return fail(source,"No saved town origin in this world.");
        try {
            boolean guilds=waterfront && includeGuildShops;
            j.plan=guilds?WesternDistrictPlan.waterfrontWithShops():plan(waterfront); j.source=source;
            var problem=siteProblem(j.level,origin,j.plan,true);
            if(problem!=null) return fail(source,problem);
            j.record=new Record(); j.record.hash=j.plan.hash; j.record.token=UUID.randomUUID().toString(); j.record.origin=origin.asLong(); j.record.phase="surveying";
            j.record.bounds=j.plan.bounds.clone(); j.record.waterfront=waterfront;
            if(guilds) j.record.guildHash=DeepsteadShopPlan.get().hash;
            j.before=new BlockState[j.plan.size()]; j.scan=0; j.save(); j.running=true;
            j.say("Survey started. No world blocks will change. Review this whole site, including unclaimed owner builds, before approving the resulting hash."); return 1;
        } catch(Exception e) { return fail(source,"Survey refused: "+e.getMessage()); }
    }
    private static int operate(CommandSourceStack source,String action,String hash) {
        var j=job(source.getServer().overworld()); j.source=source;
        try {
            if(action.equals("pause")) { j.stop("Paused by operator. No automatic resume on restart."); return 1; }
            if(j.running) return fail(source,"An operation is already running.");
            if(j.record.guildHash!=null && !j.record.guildHash.isEmpty()) {
                if(!DeepsteadShopPlan.get().hash.equals(j.record.guildHash))
                    return fail(source,"Guild shop revision differs from this installation. Restore its matching build.");
                j.plan=WesternDistrictPlan.waterfrontWithShops();
            } else j.plan=plan(j.record.waterfront);
            if(!j.record.token.equals(hash) || !j.plan.hash.equals(j.record.hash) || !j.origin().equals(Town.origin(j.level)))
                return fail(source,"Plan hash or saved town origin differs. Refusing stale approval.");
            if(j.record.phase.equals("installed") && !action.equals("rollback")) {
                if(!ApartmentRentals.installRooms(j.level,j.origin(),j.plan.rooms))
                    return fail(source,"Existing apartment designation conflicts with this installed layout.");
                return status(source);
            }
            if(!List.of("ready","applying","rolling_back","installed").contains(j.record.phase))
                return fail(source,"A completed, conflict-free survey is required first.");
            if(action.equals("apply") && !j.record.phase.equals("ready")) return fail(source,"Use resume for an interrupted operation.");
            boolean restoring=action.equals("rollback") || j.record.phase.equals("rolling_back");
            String problem=siteProblem(j.level,j.origin(),j.plan,!j.record.started,restoring?j.record.token:null); if(problem!=null) return fail(source,problem);
            j.before=WesternDistrictJournal.read(j.root.resolve("before.dat.gz"),j.record.hash,j.origin(),j.plan.size()).before();
            // Chunks may have reached disk later than our cursor before a process crash.
            // Revisit the whole before/after set; equality makes already-written cells cheap.
            j.record.cursor=0;
            if(action.equals("rollback") || j.record.phase.equals("rolling_back")) {
                if(!j.record.started) return fail(source,"Nothing has been installed.");
                if(!ApartmentRentals.canRemoveRooms(j.level,j.origin(),j.plan.rooms))
                    return fail(source,"Rental rooms are occupied or contain tenant furniture. Settle leases and recover the furniture before rollback.");
                j.restoreOrder=j.plan.restorationOrder(j.before);
                j.record.phase="rolling_back"; j.record.cursor=0;
                j.save(); // Persist the intent before withdrawing listings.
                if(j.plan.waterfront) DistrictResidents.withdraw(j.level,j.origin(),j.record.token);
                if(!ApartmentRentals.removeRooms(j.level,j.origin(),j.plan.rooms))
                    return fail(source,"Rental rooms could not be withdrawn; no blocks restored.");
            } else {
                // Full revalidation before any writes, including after a restart.
                j.scan=0; j.record.note="Revalidating every surveyed cell before installation";
            }
            j.save(); j.running=true; j.say(j.record.phase.equals("rolling_back")?"Rollback queued; later edits will pause restoration.":"Revalidation queued; no blocks change before it passes."); return 1;
        } catch(Exception e) { j.running=false; return fail(source,"Operation refused: "+e.getMessage()); }
    }
    /** Add the two guilds to a completed older mine without surveying or rewriting the city. */
    private static int guildShops(CommandSourceStack source,boolean apply) {
        var j=job(source.getServer().overworld());
        if(j.running || !j.record.waterfront || !"installed".equals(j.record.phase))
            return fail(source,"A completed waterfront installation is required.");
        var delta=DeepsteadShopPlan.get();
        if(!j.origin().equals(Town.origin(j.level)) || !WesternDistrictPlan.waterfront().hash.equals(j.record.hash))
            return fail(source,"The recorded town origin or waterfront layout has changed.");
        if(j.record.guildHash!=null && !j.record.guildHash.isEmpty() && !delta.hash.equals(j.record.guildHash))
            return fail(source,"This town has a different guild shop revision.");
        if(guildShopsOpen(j.level)) {
            source.sendSuccess(()->Component.literal("Stonewright's Hall and the Copper Kettle are open."),false);
            return 1;
        }
        var revised=WesternDistrictPlan.waterfrontWithShops();
        for(var change:delta.changes) {
            var pos=j.origin().offset(change.local());
            String problem=protectedProblem(j.level,pos);
            if(problem!=null) return fail(source,problem);
            var actual=j.level.getBlockState(pos);
            if(j.record.openingGuilds && actual==change.replacement()) continue;
            if(actual!=change.previous() || j.level.getBlockEntity(pos)!=null)
                return fail(source,"Shop footprint has a later edit at "+pos.toShortString()+". The edit was preserved.");
            if(!change.replacement().getCollisionShape(j.level,pos).isEmpty() &&
                    !j.level.getEntities((net.minecraft.world.entity.Entity)null,new AABB(pos),e->!e.isSpectator()).isEmpty())
                return fail(source,"The shop footprint is occupied at "+pos.toShortString()+".");
        }
        if(!apply) {
            source.sendSuccess(()->Component.literal("Both sites are clear ("+delta.changes.size()
                +" blocks). Use /jugcraft town expansion guildshops apply to open the shops."),false);
            return 1;
        }
        try {
            j.record.guildHash=delta.hash;
            j.record.openingGuilds=true;
            j.save();
            for(var change:delta.changes) {
                var pos=j.origin().offset(change.local());
                if(j.level.getBlockState(pos)!=change.replacement()) {
                    TownBuilder.set(j.level,pos,change.replacement());
                    if(j.level.getBlockState(pos)!=change.replacement())
                        throw new IllegalStateException("Could not place shop block at "+pos.toShortString());
                }
            }
            if(!j.level.getServer().saveEverything(true,true,true))
                throw new IllegalStateException("World save did not complete");
            j.plan=revised;
            j.record.openingGuilds=false;
            j.save();
        } catch(Exception e) {
            j.record.openingGuilds=true;
            return fail(source,"Guild shop installation paused. Retry guildshops apply. "+e.getMessage());
        }
        DistrictResidents.maintain(j.level);
        source.sendSuccess(()->Component.literal("Stonewright's Hall and the Copper Kettle are open."),false);
        return 1;
    }
    /** Only generic terrain can be excavated, and even that still needs site review. */
    public static boolean surveyTerrain(BlockState s) {
        return s.isAir() || s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(BlockTags.DIRT)
            || s.is(BlockTags.SAND) || s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)
            || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.GRAVEL) || s.is(Blocks.SNOW) || s.is(Blocks.SNOW_BLOCK)
            || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.LARGE_FERN);
    }
    private static String protectedProblem(ServerLevel level,BlockPos p) {
        if(level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4)==null) return "Chunk unloaded at "+p.toShortString();
        if(LandRegistry.isClaimed(level,p) || OperatorRegions.protects(level,p) || TownUpgrades.preservedDecor(level,p)
                || ApartmentRentals.customAt(level,p)) return "Protected/player land at "+p.toShortString();
        return null;
    }
    private static String siteProblem(ServerLevel level,BlockPos origin,WesternDistrictPlan plan,boolean terrain) {
        return siteProblem(level,origin,plan,terrain,null);
    }
    private static String siteProblem(ServerLevel level,BlockPos origin,WesternDistrictPlan plan,boolean terrain,String removing) {
        int[] b=plan.bounds;
        if(origin.getY()+b[1]<level.getMinY() || origin.getY()+b[4]>=level.getMaxY()) return "District exceeds build height";
        for(int x=(origin.getX()+b[0]-2)>>4;x<=(origin.getX()+b[3]+2)>>4;x++)
            for(int z=(origin.getZ()+b[2]-2)>>4;z<=(origin.getZ()+b[5]+2)>>4;z++)
                if(level.getChunkSource().getChunkNow(x,z)==null) return "Survey buffer has unloaded chunks. Load the site normally first; this command does not force-load it.";
        AABB area=new AABB(origin.getX()+b[0],origin.getY()+b[1],origin.getZ()+b[2],
            origin.getX()+b[3]+1,origin.getY()+b[4]+1,origin.getZ()+b[5]+1).inflate(2);
        var occupants=level.getEntities((net.minecraft.world.entity.Entity)null,area,e->!e.isSpectator()
            && (removing==null || !DistrictResidents.belongsToInstallation(e,origin,removing))
            && (!plan.waterfront || intersects(plan,origin,e.getBoundingBox().inflate(2))));
        if(!occupants.isEmpty()) {
            var e=occupants.getFirst();
            return "Work area is occupied by "+e.getName().getString()+" at "+e.blockPosition().toShortString()
                +". Move occupants outside its two-block buffer; this operation never removes them.";
        }
        if(terrain) {
            if(!ApartmentRentals.validateInstallRooms(level,origin,plan.rooms)) return "Apartment IDs or rental interiors overlap an existing designation.";
            if(plan.waterfront) {
                // The landscape is relative to the gate, but the quay also has
                // to meet real open water. Never excavate a dry hill into a port.
                if(!level.getFluidState(origin.offset(-236,-52,96)).is(net.minecraft.tags.FluidTags.WATER))
                    return "No water at the designed harbor level. Use a layout surveyed for the local shoreline.";
                return null; // Containers and ownership checked at every affected cell in tick().
            }
            for(int x=(origin.getX()+b[0]-2)>>4;x<=(origin.getX()+b[3]+2)>>4;x++)
                for(int z=(origin.getZ()+b[2]-2)>>4;z<=(origin.getZ()+b[5]+2)>>4;z++)
                    for(var entity:level.getChunkSource().getChunkNow(x,z).getBlockEntities().values())
                        if(area.contains(net.minecraft.world.phys.Vec3.atCenterOf(entity.getBlockPos())))
                            return "Block entity in the work area or safety buffer at "+entity.getBlockPos().toShortString();
            // Reject unsupported plateaus/cliffs. A survey-specific graded approach is required there.
            for(int x=-192;x<=-16;x+=4) for(int z: new int[]{16,176}) {
                int h=level.getHeight(Heightmap.Types.WORLD_SURFACE,origin.getX()+x,origin.getZ()+z)-1-origin.getY();
                if(h < -2 || h>2) return "Terrain grading required at local "+x+","+z+" (height "+h+"). Keep the before-state; adapt the blueprint before installation.";
            }
            for(int z=16;z<=176;z+=4) {
                int h=level.getHeight(Heightmap.Types.WORLD_SURFACE,origin.getX()-192,origin.getZ()+z)-1-origin.getY();
                if(h < -2 || h>2) return "Western edge requires surveyed terrain grading (height "+h+").";
            }
        }
        return null;
    }
    private static boolean intersects(WesternDistrictPlan plan,BlockPos origin,AABB area) {
        for(BlockPos p:BlockPos.betweenClosed(net.minecraft.util.Mth.floor(area.minX),net.minecraft.util.Mth.floor(area.minY),net.minecraft.util.Mth.floor(area.minZ),
                net.minecraft.util.Mth.floor(area.maxX),net.minecraft.util.Mth.floor(area.maxY),net.minecraft.util.Mth.floor(area.maxZ)))
            if(plan.contains(origin,p)) return true;
        return false;
    }
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHORE_TERRAIN =
        net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,io.github.jimbozoomer.jugcraft.Jugcraft.id("western_shore_terrain"));
    private static void tick(Job j) throws Exception {
        boolean surveying=j.record.phase.equals("surveying"), rollback=j.record.phase.equals("rolling_back");
        boolean validating=!surveying && !rollback && j.scan<j.plan.size();
        // People cannot be left inside excavation, including when entering midway through a job.
        String site=siteProblem(j.level,j.origin(),j.plan,false,rollback?j.record.token:null); if(site!=null) { j.stop(site); return; }
        long until=System.nanoTime()+8_000_000L;
        for(int work=0;work<BUDGET && System.nanoTime()<until;work++) {
            int cursor=surveying||validating?j.scan:j.record.cursor;
            if(cursor>=j.plan.size()) break;
            int i=rollback?j.restoreOrder[cursor]:cursor;
            BlockPos p=j.origin().offset(j.plan.local(i)); var wanted=j.plan.state(i); var actual=j.level.getBlockState(p);
            String problem=protectedProblem(j.level,p);
            if(problem==null && surveying) {
                boolean terrain=surveyTerrain(actual) || j.plan.waterfront && actual.is(SHORE_TERRAIN);
                if(j.level.getBlockEntity(p)!=null || !j.plan.waterfront && !actual.getFluidState().isEmpty() || !terrain && actual!=wanted)
                    problem="Non-terrain, fluid or block entity at "+p.toShortString()+" ("+actual+"). Entire survey refused; no changes made.";
                if(problem==null && wanted.isAir() && !actual.isAir()) for(var direction:net.minecraft.core.Direction.values())
                    if(!j.level.getFluidState(p.relative(direction)).isEmpty() && !(j.plan.waterfront && j.plan.contains(j.origin(),p.relative(direction)))) {
                        problem="Exposed fluid beside excavation at "+p.toShortString(); break;
                    }
            }
            boolean pendingGuildCell=rollback && j.record.openingGuilds && actual==WesternDistrictPlan.waterfront().state(i);
            if(problem==null && !surveying && (!WesternDistrictJournal.canApply(actual,j.before[i],wanted) && !pendingGuildCell
                    || !j.record.started && actual!=j.before[i]
                    || j.level.getBlockEntity(p)!=null && actual!=j.before[i] && actual!=wanted))
                problem="Later edit at "+p.toShortString()+"; paused, preserving the edit and before-image.";
            if(problem!=null) { if(surveying) j.record.phase="rejected"; j.stop(problem); return; }
            if(surveying) { j.before[i]=actual; j.scan++; }
            else if(validating) {
                if(j.level.getBlockEntity(p)!=null && actual==j.before[i] && actual!=wanted) { j.stop("New block entity at "+p.toShortString()); return; }
                j.scan++;
            } else {
                var target=rollback?j.before[i]:wanted;
                if(actual!=target) {
                    // Never erase an inventory, edited sign or other entity added after installation.
                    if(j.level.getBlockEntity(p)!=null && !(rollback && actual==wanted && pristineFixture(j.level,p,actual))) {
                        j.stop("Block entity retained at "+p.toShortString()+". Resolve it before continuing."); return;
                    }
                    if(rollback && target.getBlock() instanceof net.minecraft.world.level.block.FallingBlock) {
                        // World generation can leave gravel suspended. Restoring the surveyed
                        // terrain must not spawn a falling entity or disturb nearby belongings.
                        j.level.setBlock(p,target,net.minecraft.world.level.block.Block.UPDATE_CLIENTS
                            | net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE
                            | net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS
                            | net.minecraft.world.level.block.Block.UPDATE_SKIP_ON_PLACE);
                    } else TownBuilder.set(j.level,p,target);
                }
                j.record.cursor++;
            }
        }
        if(surveying && j.scan==j.plan.size()) {
            WesternDistrictJournal.write(j.root.resolve("before.dat.gz"),new WesternDistrictJournal.Snapshot(j.record.hash,j.origin(),j.before));
            j.record.phase="ready"; j.record.note="Reviewed-site approval required: apply "+j.record.token; j.running=false;
            j.save(); j.say("Survey complete; before-image saved. Review terrain/ownership, then /jugcraft town expansion apply "+j.record.token);
        } else if(validating && j.scan==j.plan.size()) {
            if(!j.record.started) {
                String latest=siteProblem(j.level,j.origin(),j.plan,true);
                if(latest!=null) { j.stop(latest); return; }
            }
            j.record.phase="applying"; j.record.started=true; j.record.note="Installing reviewed district"; j.save();
        } else if(!surveying && !validating) {
            if(j.record.cursor==j.plan.size()) {
                if(!rollback && !ApartmentRentals.installRooms(j.level,j.origin(),j.plan.rooms)) {
                    j.stop("Buildings installed; room designation conflict. Resolve it and resume to open rentals."); return;
                }
                j.level.getServer().saveEverything(true,true,true);
                j.record.phase=rollback?"rolled_back":"installed"; j.record.note="Completed; before-image retained"; j.running=false;
                j.restoreOrder=null;
                j.say("Western district "+j.record.phase+". Original town blueprint and room identities retained.");
                j.save();
            } else if(j.record.cursor-j.savedCursor>=65536) {
                // Resume always rechecks the entire before-image. Save milestones,
                // not an fsync and Windows file replacement every server tick.
                j.save();
            }
        }
    }
    private static boolean pristineFixture(ServerLevel level,BlockPos p,BlockState state) {
        if(!(state.getBlock() instanceof net.minecraft.world.level.block.EntityBlock block)) return false;
        var actual=level.getBlockEntity(p);
        var empty=block.newBlockEntity(p,state);
        return empty!=null && actual!=null && actual.getType()==empty.getType()
            && actual.saveWithoutMetadata(level.registryAccess()).equals(empty.saveWithoutMetadata(level.registryAccess()));
    }
    public static boolean protects(ServerLevel level,BlockPos p) {
        if(level!=level.getServer().overworld()) return false;
        var j=job(level);
        if(!j.record.started || j.record.phase.equals("rolled_back")) return false;
        var plan=plan(j.record.waterfront);
        if(plan.hash.equals(j.record.hash)) return plan.contains(j.origin(),p);
        // A later mod version must not silently move or drop an installed district's
        // protection. Retain its recorded envelope until its matching build is restored.
        int[] b=j.record.bounds;
        if(b==null || b.length!=6) throw new IllegalStateException("Installed district has no saved protection envelope; restore its matching build and journal");
        BlockPos local=p.subtract(j.origin());
        return local.getX()>=b[0] && local.getX()<=b[3] && local.getY()>=b[1] && local.getY()<=b[4]+4
            && local.getZ()>=b[2] && local.getZ()<=b[5];
    }
}
