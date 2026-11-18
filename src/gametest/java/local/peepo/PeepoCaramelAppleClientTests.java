package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;

/** Native dipping/cooling, rollback and real companion deliveries, independently of other stations. */
public final class PeepoCaramelAppleClientTests implements FabricClientGameTest {
    private final ConfectionTestSupport f=new ConfectionTestSupport();
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();f.base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.waitFor(s->s.overworld().getGameTime()>=CaramelAppleStandBlock.COOL_TICKS+1,800);
            server.runOnServer(s->transactions(s.overworld()));
            for(boolean jughead:new boolean[]{false,true})delivery(server,jughead);
            server.runOnServer(s->f.close());
        }
        org.slf4j.LoggerFactory.getLogger("peepo-caramel-apple-test").info("[peepo-caramel-apple-test] PASS {} assertions",f.checks);
    }
    private void transactions(ServerLevel level){
        for(boolean jughead:new boolean[]{false,true}){
            f.setup(level,"caramel_apple_stand",jughead,true);var stand=(CaramelAppleStandBlockEntity)f.station;var caramel=ConfectionTestSupport.food("caramel");var finished=ConfectionTestSupport.food("caramel_apple");
            f.check(CompanionFilters.get(stand)==f.confection.filter && CompanionProductionStock.of(stand)==f.confection.filter.stock,"shared Filter and stock settings belong to stand");
            f.check(!CompanionFilters.set(stand,0,new ItemStack(Items.DIAMOND)),"invalid recipe Filter rejected");
            f.menu(finished);
            f.check(f.port.needed(new ItemStack(Items.APPLE))==0,"bare apples require real caramel and sticks");
            var before=level.getBlockState(f.base);
            try(var outer=Transaction.openOuter()){
                f.check(f.port.inputs().insert(ItemVariant.of(caramel),2,outer)==2 && f.port.inputs().insert(ItemVariant.of(Items.STICK),2,outer)==2,"fund dipping inside transaction");
                try(var inner=outer.openNested()){f.check(f.port.inputs().insert(ItemVariant.of(Items.APPLE),2,inner)==2,"two apples can be dipped atomically");inner.commit();}
                f.check(level.getBlockState(f.base).equals(before),"probe does not change visible fill state");
            }
            f.check(stand.caramel()==0 && stand.sticks()==0 && stand.apples()==0,"outer rollback restores pot, jar and cooling tray");
            f.give(caramel,2);f.give(Items.STICK,2);f.give(Items.APPLE,2);
            f.check(stand.apples()==2 && stand.caramel()==0 && stand.sticks()==0,"dipping consumes exact native recipe");
            f.check(f.count(f.port.outputs(),finished)==0 && f.port.status()==CompanionStatus.COOLING,"fresh caramel apples are not extracted early");
            var saved=stand.saveWithFullMetadata(level.registryAccess());stand.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            f.check(stand.apples()==2 && f.count(f.port.outputs(),finished)==0,"native cooling timestamps survive reload");
            long now=level.getGameTime();f.check(stand.takeSet(now+CaramelAppleStandBlock.COOL_TICKS-1).isEmpty(),"native cooling remains full duration");
            f.check(stand.takeSet(now+CaramelAppleStandBlock.COOL_TICKS).is(finished),"native manual take works at cooling boundary");stand.takeSet(now+CaramelAppleStandBlock.COOL_TICKS);
            stand.addCaramel();stand.addStick();f.check(stand.dip(now-CaramelAppleStandBlock.COOL_TICKS),"already cooled native tray fixture");
            var named=new ItemStack(finished);named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Not a held variant"));
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(named),1,tx)==0,"native tray cannot create requested arbitrary item components");}
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(finished),1,tx)==1,"cooled apple output probe");}
            f.check(stand.apples()==1 && f.count(f.port.outputs(),finished)==1,"aborted output leaves cooled apple intact");
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(finished),1,tx)==1,"cooled apple commits once");tx.commit();}
            f.check(stand.apples()==0,"committed output clears native cooling slot");
            f.give(caramel,6);f.give(Items.STICK,6);f.give(Items.APPLE,6);
            f.check(f.port.needed(new ItemStack(caramel))==0 && f.port.needed(new ItemStack(Items.APPLE))==0,"full six-apple tray stops restocking");
            f.safePorts(level,caramel);
        }
    }
    private void delivery(TestServerContext server,boolean jughead){
        var caramel=ConfectionTestSupport.food("caramel");var finished=ConfectionTestSupport.food("caramel_apple");
        server.runOnServer(s->{f.setup(s.overworld(),"caramel_apple_stand",jughead,true);f.target(finished,2);f.source.setItem(0,new ItemStack(caramel,3));f.source.setItem(1,new ItemStack(Items.STICK,3));f.source.setItem(2,new ItemStack(Items.APPLE,3));f.npc.setNoAi(false);});
        f.waitDelivery(server,finished,2,3000);
        server.runOnServer(s->{f.check(f.count(f.source,caramel)==1 && f.count(f.source,Items.STICK)==1 && f.count(f.source,Items.APPLE)==1,"stock target leaves excess recipe ingredients in Supply");f.check(((CaramelAppleStandBlockEntity)f.station).apples()==0,"cooled native tray emptied to real Output");});
    }
}
