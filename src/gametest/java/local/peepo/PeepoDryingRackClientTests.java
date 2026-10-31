package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;

/** Native passive drying, transactional transfers and real stock-target Supply/Output journeys. */
public final class PeepoDryingRackClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private Container supply,output;
    private DryingRackBlockEntity rack;
    private int checks;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private long count(Storage<ItemVariant> c,Item item){long n=0;for(var view:c)if(view.getResource().isOf(item))n+=view.getAmount();return n;}
    private int hanging(){int n=0;for(int i=0;i<DryingRackBlock.SLOTS;i++)n+=rack.bunch(i).getCount();return n;}
    private void reset(ServerLevel level,boolean jughead){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-5),base.offset(8,4,5)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        level.setBlockAndUpdate(base.west(6),Blocks.BARREL.defaultBlockState());supply=(Container)level.getBlockEntity(base.west(6));
        level.setBlockAndUpdate(base.east(6),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(6));
        var block=(DryingRackBlock)JugcraftAgriculture.block("drying_rack");var state=block.defaultBlockState();var facing=state.getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++)level.setBlock(block.partPos(base,facing,i),state.setValue(block.partProperty(),i),18);
        rack=(DryingRackBlockEntity)level.getBlockEntity(base);
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assignContainer(level,base.west(6),Direction.UP,false);npc.assignments.assignContainer(level,base.east(6),Direction.UP,true);
        check(CompanionAssignments.canonical(level,base.above()).equals(base),"upper rail resolves to one rack assignment");
        check(npc.assignments.assign(level,base.above())!=null && npc.assignments.assignedWork(base),"planner assigns upper rail as rack master");
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{nativeAndTransfers(s.overworld());protection(s.overworld());});
            for(boolean jughead:new boolean[]{false,true})delivery(server,jughead);
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-drying-rack-test").info("[peepo-drying-rack-test] PASS {} assertions",checks);
    }
    private void nativeAndTransfers(ServerLevel level){
        reset(level,false);var herb=JugcraftAgriculture.item("basil");var chili=JugcraftAgriculture.item("pepper");var dry=JugcraftAgriculture.item("dried_herbs");var dryChili=JugcraftAgriculture.item("dried_chili");long now=level.getGameTime();
        var bunch=new ItemStack(herb,6);check(rack.hang(bunch,now)==0 && bunch.getCount()==2,"manual native hook consumes only four herbs");
        check(!rack.dried(0,now+DryingRackBlock.DRY_TICKS-1) && rack.dried(0,now+DryingRackBlock.DRY_TICKS),"native duration remains exactly 2400 ticks");
        check(rack.takeDown(now).is(herb) && hanging()==0,"manual early take-down still returns fresh bunch");
        rack.hang(new ItemStack(chili,3),now-DryingRackBlock.DRY_TICKS);
        var got=rack.takeDown(now);check(got.is(dryChili) && got.getCount()==3,"native pepper conversion remains one-for-one");
        var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));check(port!=null,"rack resolves logistics port");
        check(CompanionFilters.get(rack)==rack.companion.filter && CompanionProductionStock.of(rack)==rack.companion.filter.stock,"Filter and production targets use native saved rack");
        check(!CompanionFilters.set(rack,0,new ItemStack(Items.DIAMOND)) && CompanionFilters.set(rack,0,new ItemStack(dry)),"only native finished products accepted as filters");
        check(port.needed(new ItemStack(chili))==0 && port.needed(new ItemStack(herb))==16,"product filter maps fresh supply and four-hook capacity");
        var before=level.getBlockState(base);
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(herb),30,tx)==16,"bulk probe fills at most four bunches");check(port.outputs().extract(ItemVariant.of(dry),20,tx)==0,"fresh hanging bunches never become outputs");}
        check(hanging()==0 && level.getBlockState(base)==before,"aborted hanging restores contents/times and visible rail");
        try(var tx=Transaction.openOuter()){
            check(port.inputs().insert(ItemVariant.of(herb),5,tx)==5,"five herbs fit into two native bunches");
            try(var nested=tx.openNested()){check(port.inputs().insert(ItemVariant.of(herb),8,nested)==8,"nested insertion reaches free hooks");}
            check(hanging()==5,"nested rollback restores only parent hanging");tx.commit();
        }
        check(rack.bunch(0).getCount()==4 && rack.bunch(1).getCount()==1,"one batch occupies native four-item hooks");
        check(level.getBlockState(base).getValue(DryingRackBlock.RIGHT)==DryingRackBlock.Bunch.HERBS,"commit shows fresh bunches");
        check(port.status()==CompanionStatus.DRYING && count(port.outputs(),dry)==0,"drying status has no early harvest");
        check(rack.companion.pending(new ItemStack(dry),List.of(new ItemStack(herb,2)))==7,"stock counts hanging and incoming herbs once");
        var saved=rack.saveWithFullMetadata(level.registryAccess());var copy=new DryingRackBlockEntity(base,level.getBlockState(base));copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(copy.bunch(0).getCount()==4 && copy.bunch(1).getCount()==1 && !copy.dried(0,now+DryingRackBlock.DRY_TICKS-1) && copy.dried(0,now+DryingRackBlock.DRY_TICKS),"native save keeps contents and exact drying timestamps");
        check(copy.companion.filter.get(0).is(dry),"rack filter survives reload");
        // The fixture ages the saved native timestamps; it does not call a companion conversion or spawn outputs.
        for(int i=0;i<DryingRackBlock.SLOTS;i++)saved.putLong("hung_"+i,now-DryingRackBlock.DRY_TICKS);
        rack.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(count(port.outputs(),dry)==5 && rack.companion.pending(new ItemStack(dry),List.of())==0,"ready goods leave pending and enter finished stock exactly once");
        try(var tx=Transaction.openOuter()){check(port.outputs().extract(ItemVariant.of(dry),3,tx)==3,"partial dried extraction probe");}
        check(hanging()==5 && count(port.outputs(),dry)==5,"aborted collection restores native bunches");
        try(var tx=Transaction.openOuter()){check(port.outputs().extract(ItemVariant.of(dry),3,tx)==3,"committed partial dried extraction");tx.commit();}
        check(hanging()==2 && count(port.outputs(),dry)==2,"partial extraction leaves ready remainder");
        try(var tx=Transaction.openOuter()){check(port.outputs().extract(ItemVariant.of(dry),90,tx)==2,"output cannot overdraft");tx.commit();}
        check(hanging()==0 && level.getBlockState(base).getValue(DryingRackBlock.RIGHT)==DryingRackBlock.Bunch.EMPTY,"last extraction clears visible hook");
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.snapTo(npc.getX(),npc.getY(),npc.getZ()-2,0,0);
        owner.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new CompanionMenu(id,inventory,npc),npc.getDisplayName()));var menu=(CompanionMenu)owner.containerMenu;
        check(menu.value(25)==16 && menu.clickMenuButton(owner,60) && menu.filterRow()==0,"rack Filter opens from companion GUI");owner.closeContainer();
    }
    private void protection(ServerLevel level){
        reset(level,true);var herb=ItemVariant.of(JugcraftAgriculture.item("basil"));var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(level.getServer());var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(level.getServer()).book();var key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(base);
        try{
            check(land.assign(book,key,book.account(UUID.randomUUID(),"RackClaimOwner"),0).ok(),"foreign claim fixture installed");
            try(var tx=Transaction.openOuter()){check(port.inputs().insert(herb,4,tx)==0 && port.plan()==null,"cached port rejects newly protected rack");}
        }finally{land.abandon(owner,book,key,true,0);}
        var wild=PeepoMod.PEEPO.create(level,EntitySpawnReason.COMMAND);check(!rack.companion.allowed(wild),"wild companion cannot access assigned logistics");
        var upper=level.getBlockState(base.above());level.setBlock(base.above(),Blocks.AIR.defaultBlockState(),18);
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(herb,4,tx)==0 && !rack.companion.complete(),"missing upper rail blocks cached transfer");}
        level.setBlock(base.above(),upper,18);npc.assignments.clear(1);
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(herb,4,tx)==0,"removed assignment invalidates cached input");}
    }
    private void delivery(TestServerContext server,boolean jughead){
        var raw=JugcraftAgriculture.item(jughead?"pepper":"basil");var product=JugcraftAgriculture.item(jughead?"dried_chili":"dried_herbs");
        server.runCommand("gamemode creative @a");
        server.runOnServer(s->{reset(s.overworld(),jughead);check(npc.orders.mode()==CompanionOrders.Mode.WORK.ordinal(),"autonomous rack companion starts working");
            CompanionFilters.set(rack,0,new ItemStack(product));rack.companion.filter.stock.set(new ItemStack(product),5,1);rack.companion.filter.stock.useOutputs(npc,npc.assignments.get(1));supply.setItem(0,new ItemStack(raw,11));npc.setNoAi(false);
        });server.runCommand("gamemode spectator @a");
        server.waitFor(s->hanging()==5,1800);
        server.runOnServer(s->{check(count(supply,raw)==6 && count(output,product)==0,"Supply delivers only stock deficit and no instant dried output");
            for(int i=0;i<output.getContainerSize();i++)output.setItem(i,new ItemStack(Items.STONE,64));
            var saved=rack.saveWithFullMetadata(s.registryAccess());long now=s.overworld().getGameTime();
            for(int i=0;i<DryingRackBlock.SLOTS;i++)saved.putLong("hung_"+i,now-DryingRackBlock.DRY_TICKS+5);
            rack.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,s.registryAccess(),saved));
            check(count(rack.companion.outputs(),product)==0,"five remaining drying ticks still hide output");s.overworld().scheduleTick(base,(DryingRackBlock)rack.getBlockState().getBlock(),5);
        });
        long[] blockedUntil={0};server.runOnServer(s->blockedUntil[0]=s.overworld().getGameTime()+120);server.waitFor(s->s.overworld().getGameTime()>=blockedUntil[0],160);
        server.runOnServer(s->{
            check(hanging()==5 && count(rack.companion.outputs(),product)==5 && count(output,product)==0,"full external Output leaves every ready bunch on its native hook");
            check(count(supply,raw)==6,"ready blocked goods count toward stock and prevent another input batch");
            // The fixture changed saved timestamps; the earlier real scheduled tick still targets its old deadline.
            // Execute that native tick body at the now-ready time, without changing any recipe or product storage.
            s.overworld().getBlockState(base).tick(s.overworld(),base,s.overworld().getRandom());
            check(s.overworld().getBlockState(base).getValue(DryingRackBlock.RIGHT)==(jughead?DryingRackBlock.Bunch.DRIED_CHILI:DryingRackBlock.Bunch.DRIED_HERBS),"native completion tick displays dry hooks even when Output is full");
            output.setItem(0,ItemStack.EMPTY);
        });
        server.waitFor(s->count(output,product)==5,1800);
        long[] until={0};server.runOnServer(s->until[0]=s.overworld().getGameTime()+120);server.waitFor(s->s.overworld().getGameTime()>=until[0],160);
        server.runOnServer(s->{check(count(output,product)==5 && count(supply,raw)==6 && hanging()==0,"autonomous native drying and Output stop at stock target for both variants");
            check(rack.companion.filter.stock.count(npc,rack,new ItemStack(product)).stored()==5,"stock reports real stored finished products");
        });
    }
}
