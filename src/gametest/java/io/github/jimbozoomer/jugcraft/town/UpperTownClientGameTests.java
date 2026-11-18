package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.JsonOps;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Compare the same four street views across an actual old-town retrofit in a disposable local world. */
public final class UpperTownClientGameTests implements FabricClientGameTest {
    private record View(String name,double x,double eye,double z,double toX,double toY,double toZ) {}
    private static final View[] FACADES={
        new View("west_gate_house",57,23,88,36,15,104),
        new View("south_square_inn",108,23,106,90,12,120),
        new View("west_close_cottage",59,20,109,42,9,121),
        new View("south_lane_house",98.5,7.25,136.5,79.5,10,129)
    };

    @Override public void runTest(ClientGameTestContext context) {
        int oldDistance=context.computeOnClient(c->c.options.renderDistance().get());
        boolean oldHud=context.computeOnClient(c->c.gui.hud.isHidden());
        context.runOnClient(c->c.options.renderDistance().set(9));
        try(var world=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
        }).create()) {
            var server=world.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runOnServer(s->s.getPlayerList().setViewDistance(9));
            var origin=server.computeOnServer(s->{
                var level=s.overworld();
                var at=new BlockPos(400,Math.max(level.getMinY()+12,level.getHeight(Heightmap.Types.WORLD_SURFACE,496,496)-1),400);
                var fields=new com.google.gson.JsonObject();
                fields.add("origin",BlockPos.CODEC.encodeStart(JsonOps.INSTANCE,at).getOrThrow());
                var legacy=TownState.CODEC.parse(JsonOps.INSTANCE,fields).getOrThrow();
                if(legacy.newUpperFloors()) throw new AssertionError("Old town gained floors on load");
                level.getDataStorage().set(TownState.TYPE,legacy);
                // The test is about the houses. Keep their original roster slots but do not introduce actors on the roof.
                for(var spot:TownData.get().spots) legacy.setTownsperson(spot.index(),
                    java.util.UUID.nameUUIDFromBytes(("upper-town-"+spot.index()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                TownBuilder.queueLoaded(level);
                return at;
            });
            server.runCommand("forceload add 400 400 591 591");
            server.runCommand("tp @p 496 "+(origin.getY()+50)+" 490");
            boolean built=false;
            for(int tick=0;tick<2000 && !built;tick+=40) {
                context.waitTicks(40);
                built=server.computeOnServer(s->{
                    for(int x=25;x<=36;x++) for(int z=25;z<=36;z++)
                        if(!TownState.get(s).built(new ChunkPos(x,z).pack())) return false;
                    return true;
                });
            }
            if(!built) throw new AssertionError("Original town was not completely built");
            var lift=origin.offset(39,15,107);
            if(server.computeOnServer(s->s.overworld().getBlockState(lift).is(CounterweightLift.BLOCK)))
                throw new AssertionError("Lift was already installed in the legacy town");
            // A player-built copper-faced dormer and skylight on the old West Gate roof.
            var skylight=origin.offset(35,20,100);
            var glass=origin.offset(40,19,101);
            var glassState=WesternDistrictPlan.parse("minecraft:light_blue_stained_glass");
            var surround=WesternDistrictPlan.parse("minecraft:oxidized_cut_copper");
            var trim=WesternDistrictPlan.parse("minecraft:cut_copper_slab");
            var copper=WesternDistrictPlan.parse("minecraft:waxed_oxidized_cut_copper_stairs[facing=west,half=top]");
            server.runOnServer(s->{
                var level=s.overworld();
                for(int z=100;z<=102;z++) {
                    for(int y=17;y<=19;y++) TownBuilder.set(level,origin.offset(40,y,z),
                        z==101 && y==19?glassState:surround);
                    TownBuilder.set(level,origin.offset(40,20,z),trim);
                }
                TownBuilder.set(level,skylight,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            });
            context.runOnClient(c->{ if(!c.gui.hud.isHidden()) c.gui.hud.toggle(); });
            for(var v:FACADES) photograph(context,world,server,origin,v,"before");
            // Simulate a crash after one custom source was overwritten but before its new
            // chunk was saved. Read the persisted move through its real codec for retry.
            var interrupted=server.computeOnServer(s->{
                var level=s.overworld(); var house=UpperTown.houses().get(1);
                var tile=house.changes().keySet().stream().filter(p->p.getY()>=house.minRoof().getY()
                    && level.getBlockState(origin.offset(p)).getBlock() instanceof net.minecraft.world.level.block.StairBlock).findFirst().orElseThrow();
                TownBuilder.set(level,origin.offset(tile),copper);
                var move=UpperRoofMove.prepare(level,origin,house); move.check(level,origin,house);
                TownState.get(s).stageRoof(house.id(),move);
                level.getDataStorage().saveAndJoin();
                var ops=level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                var encoded=TownState.CODEC.encodeStart(ops,TownState.get(s)).getOrThrow();
                level.getDataStorage().set(TownState.TYPE,TownState.CODEC.parse(ops,encoded).getOrThrow());
                var cell=move.cells().stream().filter(c->c.pos().equals(tile)).findFirst().orElseThrow();
                TownBuilder.set(level,origin.offset(tile),cell.after());
                return origin.offset(tile).above(5);
            });
            var obstruction=origin.offset(42,29,101);
            server.runOnServer(s->{
                var level=s.overworld();
                TownBuilder.set(level,obstruction,net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());
                ((net.minecraft.world.Container)level.getBlockEntity(obstruction)).setItem(0,
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3));
            });
            server.runCommand("jugcraft town upperfloors");
            server.runCommand("jugcraft town upperfloors apply");
            server.runOnServer(s->{
                if(s.overworld().getBlockState(glass.above(5))!=glassState
                        || !s.overworld().getBlockState(skylight.above(5)).isAir()
                        || s.overworld().getBlockState(interrupted.above(16))!=copper)
                    throw new AssertionError("Custom roof, skylight or interrupted roof move failed");
                var town=TownState.get(s);
                if(town.raised("west_gate_house_jetties") || !town.raised("south_square_inn_jetties")
                        || ((net.minecraft.world.Container)s.overworld().getBlockEntity(obstruction)).getItem(0).getCount()!=3)
                    throw new AssertionError("An obstruction in one upper room must preserve inventory and allow the other house to finish");
                ((net.minecraft.world.Container)s.overworld().getBlockEntity(obstruction)).clearContent();
                TownBuilder.set(s.overworld(),obstruction,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                for(var house:UpperTown.houses()) {
                    if(!TownState.get(s).raised(house.id())) throw new AssertionError("Skipped "+house.id());
                    for(var entry:house.changes().entrySet()) if(entry.getValue().is(CounterweightLift.BLOCK)
                            && !s.overworld().getBlockState(origin.offset(entry.getKey())).is(CounterweightLift.BLOCK))
                        throw new AssertionError("Missing real lift landing "+house.id());
                }
            });
            server.runCommand("jugcraft town upperfloors apply");
            server.runCommand("jugcraft town upperfloors apply");
            server.runOnServer(s->{
                if(!TownState.get(s).raised("west_gate_house_jetties") || s.overworld().getBlockState(glass.above(16))!=glassState
                        || !s.overworld().getBlockState(skylight.above(16)).isAir())
                    throw new AssertionError("The gatehouse's edited roof must rise exactly once above its two new floors");
                for(var hall:UpperTown.jetties()) {
                    for(var cell:hall.changes().entrySet()) if(cell.getValue().is(CounterweightLift.BLOCK)
                            && !s.overworld().getBlockState(origin.offset(cell.getKey())).is(CounterweightLift.BLOCK))
                        throw new AssertionError("Missing gallery stop in "+hall.id());
                }
            });
            server.runCommand("gamemode creative @p");
            server.runOnServer(s->{
                var player=s.getPlayerList().getPlayers().getFirst();
                for(var hall:UpperTown.jetties()) {
                    var first=hall.changes().entrySet().stream().filter(e->e.getValue().is(CounterweightLift.BLOCK))
                        .map(java.util.Map.Entry::getKey).min(java.util.Comparator.comparingInt(BlockPos::getY)).orElseThrow();
                    var stop=origin.offset(first).below(5);
                    player.snapTo(stop.getX()+.5,stop.getY()+1,stop.getZ()+.5,0,0);
                    for(int i=0;i<hall.rise()/5;i++) {
                        if(!CounterweightLift.ride(player,stop,false) || !player.blockPosition().equals(stop.above(6)))
                            throw new AssertionError("Unreachable upper room at "+stop.above(5));
                        stop=stop.above(5);
                    }
                    while(stop.getY()>=origin.getY()+first.getY()) {
                        if(!CounterweightLift.ride(player,stop,true)) throw new AssertionError("Could not descend from "+stop);
                        stop=stop.below(5);
                    }
                }
            });
            server.runCommand("gamemode spectator @p");
            for(var v:FACADES) photograph(context,world,server,origin,v,"after");
            for(var v:new View[]{
                    new View("gatehouse_east_hoist",63,28,90,36,23,104),
                    new View("inn_north_jetties",111,29,98,91,25,120),
                    new View("gatehouse_window_bay",42,22,83,36,28,96),
                    new View("inn_window_bay",113,26,113,96,32,120),
                    new View("gatehouse_chart_room",39.3,27.6,106.7,33,27,99.2),
                    new View("inn_best_chamber",86.6,32.6,124.5,92.6,32.2,124.0)})
                photograph(context,world,server,origin,v,"");
            photograph(context,world,server,origin,new View("lift_landing",35.4,17.35,102.7,39.4,15.95,107.2),"");
            server.runOnServer(s->checkGenerationVersions(s.overworld(),origin.offset(4,80,0)));
        } finally {
            context.runOnClient(c->{ c.options.renderDistance().set(oldDistance); if(c.gui.hud.isHidden()!=oldHud)c.gui.hud.toggle(); });
        }
    }

    private static void checkGenerationVersions(net.minecraft.server.level.ServerLevel level,BlockPos site) {
        var storage=level.getDataStorage();
        var previous=TownState.get(level);
        try {
            var fields=new com.google.gson.JsonObject();
            fields.add("origin",BlockPos.CODEC.encodeStart(JsonOps.INSTANCE,site).getOrThrow());
            fields.addProperty("original_upper_floors",true);
            var older=TownState.CODEC.parse(JsonOps.INSTANCE,fields).getOrThrow();
            storage.set(TownState.TYPE,older);
            var chunks=new java.util.LinkedHashSet<ChunkPos>();
            for(var hall:UpperTown.jetties())
                for(var pos:hall.changes().keySet()) chunks.add(ChunkPos.containing(site.offset(pos)));
            for(var chunk:chunks) UpperTown.buildNewChunk(level,site,chunk);
            if(!level.getBlockState(site.offset(39,15,107)).is(CounterweightLift.BLOCK)
                    || level.getBlockState(site.offset(39,25,107)).is(CounterweightLift.BLOCK))
                throw new AssertionError("A save from the one-storey release must keep generating the same layout until surveyed");
            var fresh=new TownState();
            fresh.place(site);
            storage.set(TownState.TYPE,fresh);
            for(var chunk:chunks) UpperTown.buildNewChunk(level,site,chunk);
            for(var hall:UpperTown.jetties()) for(var cell:hall.changes().entrySet())
                if(level.getBlockState(site.offset(cell.getKey()))!=cell.getValue())
                    throw new AssertionError("Fresh-town jetty clipped or missing at "+cell.getKey());
        } finally {
            storage.set(TownState.TYPE,previous);
        }
    }

    private static void photograph(ClientGameTestContext context,TestSingleplayerContext world,TestServerContext server,
            BlockPos origin,View v,String suffix) {
        var dx=v.toX-v.x; var dz=v.toZ-v.z;
        double yaw=Math.toDegrees(Math.atan2(-dx,dz));
        double pitch=Math.toDegrees(Math.atan2(v.eye-v.toY,Math.hypot(dx,dz)));
        server.runCommand(String.format(Locale.ROOT,"tp @p %.2f %.2f %.2f %.2f %.2f",
            origin.getX()+v.x,origin.getY()+v.eye-1.62,origin.getZ()+v.z,yaw,pitch));
        context.waitTicks(55);
        world.getConnection().waitForChunksRender(false,1200);
        context.takeScreenshot("upper_town_"+v.name+(suffix.isEmpty()?"":"_"+suffix));
    }
}
