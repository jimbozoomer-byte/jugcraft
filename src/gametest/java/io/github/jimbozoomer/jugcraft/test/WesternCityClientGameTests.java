package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.*;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Authored geometry only in a disposable flat world; never touches a launcher profile. */
public final class WesternCityClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        int previousDistance=context.computeOnClient(c->c.options.renderDistance().get());
        context.runOnClient(c->c.options.renderDistance().set(8));
        final BlockPos origin=new BlockPos(400,100,400), marker=origin.offset(-100,1,96);
        final TestWorldSave save;
        final String token;
        try(var world=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
        }).create()) {
            var server=world.getServer();
            server.runOnServer(s->s.getPlayerList().setViewDistance(10));
            server.runCommand("time set noon"); server.runCommand("weather clear");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("gamemode spectator @p");
            server.runCommand("tp @p 298 150 496");
            // Explicit test-only chunk loading and an unowned flat survey fixture.
            server.runCommand("forceload add 192 400 431 591");
            var p=server.computeOnServer(s->WesternDistrictPlan.get());
            server.runOnServer(s->{
                TownState.get(s).place(origin);
                // Existing residents are elsewhere/unloaded in this migration fixture.
                // Otherwise routine care respawns gate guards inside the construction buffer.
                for(var spot:TownData.get().spots) TownState.get(s).setTownsperson(spot.index(),
                    java.util.UUID.nameUUIDFromBytes(("western-fixture-resident-"+spot.index()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                // Simulate an already-built old town so reopening tests only the expansion.
                for(int x=24;x<=38;x++) for(int z=24;z<=38;z++) {
                    long chunk=new net.minecraft.world.level.ChunkPos(x,z).pack();
                    TownState.get(s).markBuilt(chunk); TownUpgrades.markCurrent(s.overworld(),chunk);
                }
                // Restored ground can sit beneath the wall while rollback is paused at the
                // owner's chest. Coarse dirt keeps the before-image stable across random ticks.
                for(int x=-196;x<=19;x++) for(int z=12;z<=180;z++)
                    TownBuilder.set(s.overworld(),origin.offset(x,0,z),net.minecraft.world.level.block.Blocks.COARSE_DIRT.defaultBlockState());
            });
            context.waitTicks(40);
            server.runCommand("jugcraft town expansion survey");
            waitFor(context,server,"ready",1600);
            token=server.computeOnServer(s->WesternDistrict.progress(s.overworld()).surveyToken());
            // An edit after survey must refuse the whole operation before writes.
            server.runOnServer(s->s.overworld().setBlockAndUpdate(marker,net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState()));
            server.runCommand("jugcraft town expansion apply "+token);
            for(int tick=0;tick<1600;tick+=20) {
                context.waitTicks(20);
                if(!server.computeOnServer(s->WesternDistrict.progress(s.overworld()).running())) break;
            }
            var refused=server.computeOnServer(s->WesternDistrict.progress(s.overworld()));
            if(refused.running() || refused.cursor()!=0 || !refused.phase().equals("ready")) throw new AssertionError("Changed survey was not refused: "+refused);
            server.runOnServer(s->{
                if(!s.overworld().getBlockState(marker).is(net.minecraft.world.level.block.Blocks.CHEST)) throw new AssertionError("Owner chest was changed");
                s.overworld().setBlockAndUpdate(marker,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            });
            server.runCommand("jugcraft town expansion resume "+token);
            boolean partial=false;
            for(int tick=0;tick<3600;tick+=20) {
                context.waitTicks(20);
                var state=server.computeOnServer(s->WesternDistrict.progress(s.overworld()));
                if(!state.running()) throw new AssertionError("Installation stopped before reopen checkpoint: "+state);
                if(state.phase().equals("applying") && state.cursor()>p.size()*9/10) { partial=true; break; }
            }
            if(!partial) throw new AssertionError("Did not reach a partially built checkpoint");
            server.runCommand("jugcraft town expansion pause");
            save=world.getWorldSave();
        }
        try(var world=save.open()) {
            var server=world.getServer();
            server.runOnServer(s->s.getPlayerList().setViewDistance(10)); var p=server.computeOnServer(s->WesternDistrictPlan.get());
            context.waitTicks(40);
            var restored=server.computeOnServer(s->WesternDistrict.progress(s.overworld()));
            if(restored.running() || !restored.phase().equals("applying") || restored.cursor()<=0 || !restored.surveyToken().equals(token))
                throw new AssertionError("Partial installation was not retained idle across reopening: "+restored);
            server.runCommand("jugcraft town expansion resume "+token);
            waitFor(context,server,"installed",3600);
            server.runCommand("jugcraft town expansion apply "+token); // Already installed: a no-op.
            server.runOnServer(s->{
                if(!WesternDistrict.protects(s.overworld(),origin.offset(-102,-25,105))) throw new AssertionError("Underground is unprotected");
                var door=origin.offset(p.rooms.getFirst().door());
                if(!TownProtection.publicBlock(s.overworld(),door,s.overworld().getBlockState(door))) throw new AssertionError("Public apartment door denied");
            });
            context.runOnClient(c->{if(!c.gui.hud.isHidden()) c.gui.hud.toggle();});
            int[][] views={{-102,50,188,180,25},{-156,2,82,180,-14},{-104,7,82,180,-8},
                {-102,3,103,180,0},{-149,7,139,180,6},{-102,-23,126,180,-12},{-102,-9,117,180,5},
                {-173,-23,117,210,0},{-169,2,63,0,-15},{-168,6,47,20,8}};
            String[] names={"overview","apartments","balconies","avenue","shops","rotunda","gallery","service_hall","stair_entrance","apartment_room"};
            for(int i=0;i<views.length;i++) {
                var a=views[i];
                server.runOnServer(s->{var player=s.getPlayerList().getPlayers().getFirst(); player.getAbilities().flying=true; player.onUpdateAbilities();});
                server.runCommand(String.format(Locale.ROOT,"tp @p %.1f %d %.1f %d %d",origin.getX()+a[0]+0.5,origin.getY()+a[1],origin.getZ()+a[2]+0.5,a[3],a[4]));
                // Wait for the authored views to render; full-square download checks exceed the
                // integrated server's circular chunk tracking at larger review distances.
                context.waitTicks(80); world.getConnection().waitForChunksRender(false,1200);
                context.takeScreenshot("western_city_"+names[i]);
            }
            context.runOnClient(c->{if(c.gui.hud.isHidden()) c.gui.hud.toggle();});
            server.runOnServer(s->s.overworld().setBlockAndUpdate(marker,net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState()));
            server.runCommand("jugcraft town expansion rollback "+token);
            for(int tick=0;tick<3000;tick+=20) {
                context.waitTicks(20);
                if(!server.computeOnServer(s->WesternDistrict.progress(s.overworld()).running())) break;
            }
            server.runOnServer(s->{
                var progress=WesternDistrict.progress(s.overworld());
                if(progress.running() || !progress.phase().equals("rolling_back") || !progress.note().contains(marker.toShortString())
                        || !s.overworld().getBlockState(marker).is(net.minecraft.world.level.block.Blocks.CHEST))
                    throw new AssertionError("Rollback did not preserve later owner edit: "+progress);
                s.overworld().setBlockAndUpdate(marker,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            });
            server.runCommand("jugcraft town expansion resume "+token);
            waitFor(context,server,"rolled_back",3000);
            server.runOnServer(s->{
                try {
                    var snapshot=WesternDistrictJournal.read(s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                        .resolve("data/jugcraft-western-district/before.dat.gz"),p.hash,origin,p.size());
                    for(int i=0;i<p.size();i++) if(s.overworld().getBlockState(origin.offset(p.local(i)))!=snapshot.before()[i])
                        throw new AssertionError("Rollback differs from before-image at "+p.local(i));
                    if(WesternDistrict.protects(s.overworld(),origin.offset(-102,-25,105))) throw new AssertionError("Rollback left district protected");
                } catch(java.io.IOException e) { throw new AssertionError(e); }
            });
        }
        try(var world=save.open()) {
            var restored=world.getServer().computeOnServer(s->WesternDistrict.progress(s.overworld()));
            if(restored.running() || !restored.phase().equals("rolled_back"))
                throw new AssertionError("Completed rollback was not retained across reopening: "+restored);
        }
        context.runOnClient(c->c.options.renderDistance().set(previousDistance));
    }
    private static void waitFor(ClientGameTestContext context,net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,String phase,int ticks) {
        for(int tick=0;tick<ticks;tick+=20) {
            context.waitTicks(20);
            var p=server.computeOnServer(s->WesternDistrict.progress(s.overworld()));
            if(p.phase().equals(phase) && !p.running()) return;
            if(!p.running()) throw new AssertionError("Unexpected stopped district operation: "+p);
        }
        throw new AssertionError("District operation did not reach "+phase);
    }
}
