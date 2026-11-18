package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.*;
import io.github.jimbozoomer.jugcraft.client.DeepsteadDwarfRenderer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

/** Exercise the operator commands on a disposable world; the optional region source is read-only. */
public final class WesternWaterfrontClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        String reference=System.getenv("JUGCRAFT_WESTERN_TERRAIN_REFERENCE");
        int oldDistance=context.computeOnClient(c->c.options.renderDistance().get());
        context.runOnClient(c->c.options.renderDistance().set(10));
        try {
            reviewWaterfront(context,reference);
        } finally { context.runOnClient(c->c.options.renderDistance().set(oldDistance)); }
    }

    private static void reviewWaterfront(ClientGameTestContext context,String reference) {
        BlockPos origin=new BlockPos(1296,114,400);
        final TestWorldSave save;
        final Path status;
        final WesternDistrictPlan plan;
        final BlockState[] original;
        final Map<BlockPos,CompoundTag> protectedContainers;
        final String token;
        try(var world=WesternTerracedCityClientGameTests.reviewWorld(context,reference)) {
            save=world.getWorldSave();
            var server=world.getServer();
            status=server.computeOnServer(s->s.getWorldPath(LevelResource.ROOT)
                .resolve("data/jugcraft-western-district/status.json").toAbsolutePath().normalize());
            server.runCommand("gamemode spectator @p");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runOnServer(s->s.getPlayerList().setViewDistance(10));
            server.runCommand("tp @p 1175 120 498");
            plan=server.computeOnServer(s->WesternDistrictPlan.waterfront());
            var bounds=plan.bounds;
            int first=origin.getX()+bounds[0]-2, last=origin.getX()+bounds[3]+2;
            for(int x=first;x<=last;x+=128)
                server.runCommand("forceload add "+x+" "+(origin.getZ()+bounds[2]-2)+" "+Math.min(last,x+127)+" "+(origin.getZ()+bounds[5]+2));
            server.runOnServer(s->{
                var town=TownState.get(s); town.place(origin);
                for(var spot:TownData.get().spots) town.setTownsperson(spot.index(),
                    UUID.nameUUIDFromBytes(("waterfront-test-"+spot.index()).getBytes(StandardCharsets.UTF_8)));
                for(int x=origin.getX()/16-1;x<=origin.getX()/16+13;x++) for(int z=origin.getZ()/16-1;z<=origin.getZ()/16+13;z++) {
                    long chunk=new ChunkPos(x,z).pack(); town.markBuilt(chunk); TownUpgrades.markCurrent(s.overworld(),chunk);
                }
                if(reference==null) {
                    BlockPos water=origin.offset(-236,-52,96);
                    for(var direction:net.minecraft.core.Direction.values()) if(direction!=net.minecraft.core.Direction.UP)
                        TownBuilder.set(s.overworld(),water.relative(direction),Blocks.STONE.defaultBlockState());
                    TownBuilder.set(s.overworld(),water,Blocks.WATER.defaultBlockState());
                }
            });
            context.waitTicks(80);
            original=server.computeOnServer(s->{
                var states=new net.minecraft.world.level.block.state.BlockState[plan.size()];
                for(int i=0;i<plan.size();i++) {
                    states[i]=s.overworld().getBlockState(origin.offset(plan.local(i)));
                }
                return states;
            });
            protectedContainers=server.computeOnServer(s->{
                var saved=new java.util.HashMap<BlockPos,net.minecraft.nbt.CompoundTag>();
                if(reference!=null)
                    for(int x=(origin.getX()-272)>>4;x<=(origin.getX()+bounds[3])>>4;x++)
                        for(int z=(origin.getZ()+bounds[2])>>4;z<=(origin.getZ()+bounds[5])>>4;z++)
                            for(var be:s.overworld().getChunk(x,z).getBlockEntities().values())
                                if(be instanceof net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity)
                                    saved.put(be.getBlockPos(),be.saveWithoutMetadata(s.registryAccess()));
                if(reference!=null && !saved.containsKey(origin.offset(-271,-56,93)))
                    throw new AssertionError("Reference shipwreck chest missing");
                return saved;
            });
            if(reference==null) server.runCommand("jugcraft town expansion survey waterfront");
            else server.runOnServer(s->GuildShopRetrofitFixture.legacySurvey(s.overworld()));
            waitFor(context,server,"ready");
            token=server.computeOnServer(s->WesternDistrict.progress(s.overworld()).surveyToken());
            server.runCommand("jugcraft town expansion apply "+token);
            waitFor(context,server,"installed");
            context.waitTicks(200);
            server.runOnServer(s->{
                var level=s.overworld();
                DistrictResidentsGameTests.installed(level,s.getPlayerList().getPlayers().getFirst(),origin,token);
                if(reference!=null) GuildShopRetrofitFixture.rejectConflictAndInstall(level,origin,token);
                else GuildShopRetrofitFixture.expectLayout(level,origin,true);
                DistrictResidentsGameTests.guildKeepers(level,origin,token,true);
                for(var entry:protectedContainers.entrySet()) {
                    var be=level.getBlockEntity(entry.getKey());
                    if(be==null || !entry.getValue().equals(be.saveWithoutMetadata(s.registryAccess())))
                        throw new AssertionError("Existing container changed at "+entry.getKey());
                }
                for(var room:plan.rooms) {
                    int[] b=room.bounds();
                    if(!ApartmentRentals.protects(level,origin.offset(b[0],b[1],b[2])))
                        throw new AssertionError("Missing rentable room "+room.id());
                }
                for(int i=0;i<plan.size();i++) {
                    var p=plan.local(i);
                    if(p.getY() < -52 && p.getX()>-215 && plan.state(i).isAir() && !level.getFluidState(origin.offset(p)).isEmpty())
                        throw new AssertionError("Underground water entered a dry space at "+p);
                }
            });
            for(int keeper: new int[]{20,21}) {
                context.waitTicks(10); // Preserve the live shop's normal per-player rate limit.
                server.runOnServer(s->DistrictResidentsGameTests.buyFromGuildKeeper(s.overworld(),
                    s.getPlayerList().getPlayers().getFirst(),origin,token,keeper));
            }
            context.runOnClient(c->{if(!c.gui.hud.isHidden()) c.gui.hud.toggle();});
            int[][] views={{-127,31,155,178,27},{-151,-38,89,155,-15},{55,-50,88,-88,7},
                {105,-50,103,135,10},{89,-75,102,165,-6},{-286,-14,135,-127,25},{-259,-44,108,-110,-18},
                {85,-81,70,180,-12},{85,-81,57,-143,2},{83,-67,107,0,-12},{83,-67,118,-46,4}};
            String[] names={"apartments","brick_house","mine_gallery","mine_watchtower","mine_quay","port","docks",
                "stonewright_front","stonewright_hall","copper_kettle_front","copper_kettle_taproom"};
            for(int i=0;i<views.length;i++) {
                var a=views[i];
                server.runCommand(String.format(Locale.ROOT,"tp @p %.1f %d %.1f %d %d",
                    origin.getX()+a[0]+.5,origin.getY()+a[1],origin.getZ()+a[2]+.5,a[3],a[4]));
                context.waitTicks(50); world.getConnection().waitForChunksRender(false,1200);
                context.takeScreenshot("waterfront_"+names[i]);
                if(names[i].equals("stonewright_hall") || names[i].equals("copper_kettle_taproom")) {
                    boolean mason=names[i].equals("stonewright_hall");
                    int actor=server.computeOnServer(s->DistrictResidentsGameTests.guildKeeperId(s.overworld(),origin,token,mason?20:21));
                    verifyGuildWardrobe(context,actor,mason);
                }
            }
            context.runOnClient(c->{if(c.gui.hud.isHidden()) c.gui.hud.toggle();});
            server.runOnServer(s->GuildShopRetrofitFixture.stageInterruptedSave(s.overworld(),origin));
        }
        GuildShopRetrofitFixture.markInterruptedOnDisk(status);
        try(var reopened=save.open()) {
            var server=reopened.getServer();
            int[] bounds=plan.bounds;
            server.waitFor(s->{
                for(int x=(origin.getX()+bounds[0]-2)>>4;x<=(origin.getX()+bounds[3]+2)>>4;x++)
                    for(int z=(origin.getZ()+bounds[2]-2)>>4;z<=(origin.getZ()+bounds[5]+2)>>4;z++)
                        if(s.overworld().getChunkSource().getChunkNow(x,z)==null) return false;
                return s.overworld().isPositionEntityTicking(origin.offset(91,-81,53))
                    && s.overworld().isPositionEntityTicking(origin.offset(87,-67,121));
            },1200);
            context.waitTicks(40);
            server.runOnServer(s->GuildShopRetrofitFixture.resumeInterruptedSave(s.overworld(),origin,token));
            server.runCommand("jugcraft town expansion rollback "+token);
            waitFor(context,server,"rolled_back");
            context.waitTicks(120);
            server.runOnServer(s->{
                DistrictResidentsGameTests.removed(s.overworld(),origin,token);
                for(int i=0;i<plan.size();i++) {
                    var p=origin.offset(plan.local(i));
                    var restored=s.overworld().getBlockState(p);
                    if(restored!=original[i])
                        throw new AssertionError("Rollback at "+p+" restored "+restored+" instead of "+original[i]);
                }
                for(var entry:protectedContainers.entrySet()) {
                    var be=s.overworld().getBlockEntity(entry.getKey());
                    if(be==null || !entry.getValue().equals(be.saveWithoutMetadata(s.registryAccess())))
                        throw new AssertionError("Rollback altered existing container at "+entry.getKey());
                }
                for(var room:plan.rooms) {
                    var b=room.bounds();
                    if(ApartmentRentals.protects(s.overworld(),origin.offset(b[0],b[1],b[2])))
                        throw new AssertionError("Rolled back apartment still listed: "+room.id());
                }
            });
        }
    }

    private static void verifyGuildWardrobe(ClientGameTestContext context,int actor,boolean mason) {
        context.waitFor(c->c.level.getEntity(actor) instanceof DeepsteadDwarf,240);
        context.runOnClient(c->{
            var dwarf=(DeepsteadDwarf)c.level.getEntity(actor);
            if(dwarf.wearsGuildColors(DeepsteadWares.STONEWRIGHT)!=mason
                    || dwarf.wearsGuildColors(DeepsteadWares.COPPER_KETTLE)==mason)
                throw new AssertionError("Specialty outfit failed to synchronize for "+dwarf.getName().getString());
            var renderer=(DeepsteadDwarfRenderer)c.getEntityRenderDispatcher().getRenderer(dwarf);
            var state=renderer.createRenderState();
            renderer.extractRenderState(dwarf,state,.5F);
            var model=renderer.getModel();
            model.setupAnim(state);
            var body=model.root().getChild("body");
            var square=body.getChild("left_arm").getChild("masons_square");
            var ladle=body.getChild("right_arm").getChild("copper_ladle");
            if(square.visible!=mason || ladle.visible==mason || body.getChild("bar_towel").visible==mason
                    || !body.getChild("work_apron").visible || body.getChild("left_arm").getChild("ledger").visible)
                throw new AssertionError("Keeper has the wrong visible profession equipment");
            if((mason?square:ladle).getAllParts().stream().allMatch(part->part.isEmpty()))
                throw new AssertionError("Keeper's displayed tool has no baked geometry");
        });
    }
    private static void waitFor(ClientGameTestContext context,TestServerContext server,String phase) {
        for(int tick=0;tick<14000;tick+=40) {
            var progress=server.computeOnServer(s->WesternDistrict.progress(s.overworld()));
            if(!progress.running()) {
                if(progress.phase().equals(phase)) return;
                throw new AssertionError("Waterfront stopped before "+phase+": "+progress);
            }
            context.waitTicks(40);
        }
        throw new AssertionError("Timed out waiting for "+phase);
    }
}
