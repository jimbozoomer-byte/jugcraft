package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.LoggerFactory;

/** Real bounded cutting batches, save/rollback, stock accounting and physical delivery on both rigs. */
public final class PeepoCuttingBatchClientTests implements FabricClientGameTest {
    private BlockPos base;private PeepoEntity npc;private CuttingBoardBlockEntity board,alternative;private Container source,output;private int assertions;
    private final Item bacon=JugcraftAgriculture.item("bacon");
    private void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private int finished(){int n=0;for(var v:board.companionKitchen.outputStorage())if(v.getResource().getItem()==bacon)n+=(int)v.getAmount();return n;}
    private CompanionLogistics.Port port(){return CompanionLogistics.resolve(npc,npc.assignments.get(1));}
    private int queued(){return board.item().getCount()+board.companionKitchen.queuedCuts().getCount();}
    private void reset(ServerLevel level,boolean pink){reset(level,pink,false);}
    private void reset(ServerLevel level,boolean pink,boolean elevated){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-7),base.offset(8,4,7)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        alternative=null;var boardAt=elevated?base.above():base;
        if(elevated)level.setBlockAndUpdate(base,Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(boardAt,JugcraftAgriculture.block("cutting_board").defaultBlockState());board=(CuttingBoardBlockEntity)level.getBlockEntity(boardAt);
        level.setBlockAndUpdate(base.west(5),Blocks.BARREL.defaultBlockState());source=(Container)level.getBlockEntity(base.west(5));
        level.setBlockAndUpdate(base.east(5),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(5));
        npc=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);
        npc.snapTo(base.getX()-3.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);
        npc.preferences.social=false;npc.preferences.carryMeals=0;npc.assignments.assign(level,boardAt);
        npc.belongings.setItem(9,new ItemStack(JugcraftAgriculture.item("iron_knife")));
        npc.assignments.assignContainer(level,base.west(5),Direction.UP,false);npc.assignments.assignContainer(level,base.east(5),Direction.UP,true);
    }
    private void alternativeWork(ServerLevel level){
        var at=base.south(4);level.setBlockAndUpdate(at,JugcraftAgriculture.block("cutting_board").defaultBlockState());
        alternative=(CuttingBoardBlockEntity)level.getBlockEntity(at);npc.assignments.assign(level,at);
    }
    private void readyAlternative(){alternative.companionPut(new ItemStack(Items.PORKCHOP));alternative.companionKitchen.changed();}
    private void load(int n){try(var tx=Transaction.openOuter()){check(port().inputs().insert(ItemVariant.of(Items.PORKCHOP),n,tx)==n,"accept whole cutting batch");tx.commit();}}
    private void goal(){var f=board.companionKitchen.filter;f.set(0,new ItemStack(bacon));f.stock.set(new ItemStack(bacon),3,1);f.stock.useOutputs(npc,npc.assignments.get(1));}
    private void planning(ServerLevel level){
        reset(level,false);source.setItem(0,new ItemStack(Items.PORKCHOP,64));source.setItem(1,new ItemStack(Items.PORKCHOP,64));
        var storage=ContainerStorage.of(source,Direction.UP);var batch=port().supplyBatch(storage,8);
        check(batch.size()==1 && batch.getFirst().getCount()==64,"one stack batch even with multiple supply stacks");
        check(queued()==0 && count(source,Items.PORKCHOP)==128,"planning restores queue, visible piece and supplies");
        try(var tx=Transaction.openOuter()){check(port().inputs().insert(ItemVariant.of(Items.PORKCHOP),64,tx)==64,"simulate full queue");}
        check(queued()==0,"aborted insert restores queue");
        load(64);check(board.item().getCount()==1 && board.companionKitchen.queuedCuts().getCount()==63,"native visible workpiece stays singular");
        var named=new ItemStack(Items.PORKCHOP);named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Special pork"));
        check(port().needed(named)==0,"queued ingredients do not merge different item components");
        check(port().needed(new ItemStack(Items.PORKCHOP))==0 && port().needed(new ItemStack(Items.CHICKEN))==0,"full batch rejects overflow and mixed ingredients");
        var saved=board.saveWithFullMetadata(level.registryAccess());board.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(queued()==64,"native save/load preserves batch");
        int energy=npc.getEnergy();check(board.companionKitchen.cut(npc),"cut one queued recipe");
        check(queued()==63 && finished()==2 && energy-npc.getEnergy()==16 && npc.assignedHand().getDamageValue()==1,"each cut consumes one piece, normal energy and wear");
        check(port().pickupNeeded(new ItemStack(Items.PORKCHOP))==0 && port().supplyBatch(storage,8).isEmpty(),"partly processed batch is not endlessly refilled by another courier");
        check(board.take().getCount()==1 && queued()==62,"manual cut/take advances only one workpiece");
        var player=level.getServer().getPlayerList().getPlayers().getFirst();int before=count(player.getInventory(),Items.PORKCHOP);
        check(board.companionKitchen.handOver(player) && queued()==0 && count(player.getInventory(),Items.PORKCHOP)==before+62,"empty hand recovers entire unfinished batch");
        goal();batch=port().supplyBatch(storage,8);
        check(batch.size()==1 && batch.getFirst().getCount()==2,"stock target three fetches exactly two two-result cuts");
        load(2);check(board.companionKitchen.filter.stock.count(npc,board,new ItemStack(bacon)).underway()==4,"stock counts visible and queued recipes exactly once");
        check(port().supplyBatch(storage,8).isEmpty(),"queued products prevent over-fetching");
        npc.belongings.setItem(9,ItemStack.EMPTY);check(!board.companionKitchen.cut(npc) && queued()==2,"missing knife preserves whole batch");
        npc.belongings.setItem(9,new ItemStack(JugcraftAgriculture.item("iron_knife")));
        int reserve=npc.getEnergy();npc.setStoredEnergy(0);
        check(!board.companionKitchen.cut(npc) && queued()==2 && finished()==0,"failed energy payment rolls back result and raw input together");npc.setStoredEnergy(reserve);
        for(int i=0;i<9;i++)check(board.companionKitchen.finish(new ItemStack(Items.COBBLESTONE,64)),"fill output buffer");
        check(!board.companionKitchen.cut(npc) && queued()==2,"full output pauses without losing queued raw input");
        board.companionKitchen.takeFinished();board.companionKitchen.changed();
        var drops=new net.minecraft.world.phys.AABB(base).inflate(2);for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,drops))e.discard();
        level.destroyBlock(base,false);
        int dropped=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,drops).stream().filter(e->e.getItem().is(Items.PORKCHOP)).mapToInt(e->e.getItem().getCount()).sum();
        check(dropped==2,"breaking board drops active and queued raw inputs once");
    }
    private void cycle(ClientGameTestContext context,TestServerContext server,boolean pink,boolean limited){
        int amount=limited?2:12;
        server.runOnServer(s->{reset(s.overworld(),pink);alternativeWork(s.overworld());if(limited)goal();source.setItem(0,new ItemStack(Items.PORKCHOP,limited?8:amount));npc.setNoAi(false);});
        server.waitFor(s->count(npc.belongings,Items.PORKCHOP)==amount,900);
        server.runOnServer(s->check(queued()==0,"batch physically carried before board changes"));
        server.waitFor(s->finished()>0,900);
        server.runOnServer(s->readyAlternative());
        if(!limited)looping(context,server);
        pickupHandoff(server,900);
        server.waitFor(s->count(output,bacon)==amount*2,1000);
        server.runOnServer(s->{check(npc.assignedHand().getDamageValue()==amount,"one knife wear per processed ingredient");check(queued()==0 && finished()==0 && count(npc.belongings,bacon)==0,"complete batch delivered without stranded goods");check(count(source,Items.PORKCHOP)==(limited?6:0),"Supply respects batch and stock limit");});
    }
    /** Observe every server tick across the last cut, rather than allowing a later scheduler retry to pass. */
    private void pickupHandoff(TestServerContext server,int timeout){
        long[] completed={-1};boolean[] collecting={false};
        server.waitFor(s->{
            if(queued()>0){check(count(npc.belongings,bacon)==0 && output.isEmpty(),"stays chopping instead of hauling partial batch");return false;}
            long now=s.overworld().getGameTime();if(completed[0]<0)completed[0]=now;
            var activity=npc.transport.activity();boolean picked=count(npc.belongings,bacon)>0 || count(output,bacon)>0;
            collecting[0]|=activity==CompanionStatus.COLLECTING_OUTPUT || activity==CompanionStatus.DELIVERING || picked;
            check(now-completed[0]<=10 || collecting[0],"finished batch must enter Output collection within ten ticks: "+activity);
            check(now-completed[0]<=40 || picked,"finished batch must be collected before a later scheduler retry");
            check(npc.getRestMode()==CompanionEnergy.Rest.NONE && !npc.isUsingJobAt(alternative.getBlockPos()),"finished batch is collected before leisure or lower-priority work");
            check(alternative.item().is(Items.PORKCHOP),"lower-priority workpiece remains untouched until pickup");
            check(activity!=CompanionStatus.BLOCKED && activity!=CompanionStatus.FETCHING_SUPPLIES,"collection does not fail during dismount or switch to a new supply trip");
            var target=npc.transport.feedbackTarget();
            check(target==null || target.at().pos().equals(board.getBlockPos()),"collection retains the workstation that just finished");
            check(picked || board.getBlockPos().distToCenterSqr(npc.position())<6.25,"worker stays beside finished goods until pickup");
            return picked;
        },timeout);
    }
    private void elevatedHandoff(TestServerContext server,boolean pink){
        server.runOnServer(s->{reset(s.overworld(),pink,true);alternativeWork(s.overworld());goal();load(2);npc.setNoAi(false);});
        server.waitFor(s->finished()>0,900);
        server.runOnServer(s->{check(npc.isNoGravity() && npc.getY()>base.getY()+1,"counter-height board is worked from its raised edge");readyAlternative();});
        pickupHandoff(server,180);
        server.waitFor(s->count(output,bacon)==4,600);
        server.runOnServer(s->check(finished()==0 && queued()==0 && count(npc.belongings,bacon)==0,"counter dismount delivers all real results"));
    }
    private void unavailableOutput(TestServerContext server,boolean pink,boolean disabled){
        server.runOnServer(s->{
            reset(s.overworld(),pink);alternativeWork(s.overworld());goal();load(2);
            if(disabled){npc.assignments.cycleTransport(1,false);npc.assignments.cycleTransport(1,false);}
            else for(int i=0;i<output.getContainerSize();i++)output.setItem(i,new ItemStack(Items.STONE,64));
            npc.setNoAi(false);
        });
        server.waitFor(s->finished()>0,900);server.runOnServer(s->readyAlternative());
        server.waitFor(s->queued()==0,180);
        server.waitFor(s->npc.isUsingJobAt(alternative.getBlockPos()),100);
        server.runOnServer(s->{
            check(finished()==4 && count(npc.belongings,bacon)==0 && count(output,bacon)==0,"unavailable Output preserves finished goods at the board");
            check(!npc.transport.hasPendingDelivery(),"unavailable Output cannot hold a worker in a permanent handoff");
            // End the alternate job without manufacturing any extra result, then restore the real endpoint.
            alternative.companionPut(ItemStack.EMPTY);alternative.companionKitchen.changed();
            if(disabled)npc.assignments.cycleTransport(1,false);else output.clearContent();
        });
        server.waitFor(s->count(output,bacon)==4,900);
        server.runOnServer(s->check(finished()==0 && count(npc.belongings,bacon)==0,"retained batch delivers once Output becomes usable"));
    }
    private void looping(ClientGameTestContext context,TestServerContext server){
        int id=server.computeOnServer(s->npc.getId());long started=server.computeOnServer(s->npc.workStarted());
        for(boolean raised:new boolean[]{true,false}){
            context.waitFor(c->{
                if(!(c.level.getEntity(id) instanceof PeepoEntity p) || p.workAnimation()!=WorkAnimation.CHOP)return false;
                int period=p.uniforms.duration(CompanionUniforms.Type.CHEF,40);long elapsed=p.level().getGameTime()-p.workStarted();
                double phase=(Math.floorMod(elapsed,period)+.5)/period;
                if(elapsed<(raised?2:3)*period || (raised?phase<.55 || phase>.65:phase>.10))return false;
                var renderer=(local.peepo.client.PeepoRenderer)c.getEntityRenderDispatcher().getRenderer(p);
                var state=renderer.createRenderState();renderer.extractRenderState(p,state,.5F);
                local.peepo.client.CompanionClipChecks.choppingBatchFrame(state,raised,this::check);return true;
            },200);
        }
        server.runOnServer(s->check(queued()>0 && npc.workStarted()==started,"later swings keep the same active batch and action start"));
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand(String.format(Locale.ROOT,"tp @a %.1f %d %.1f",base.getX()+.5,base.getY(),base.getZ()-5.5));
            server.runOnServer(s->planning(s.overworld()));
            for(boolean pink:new boolean[]{false,true}){cycle(context,server,pink,false);cycle(context,server,pink,true);elevatedHandoff(server,pink);unavailableOutput(server,pink,false);unavailableOutput(server,pink,true);}
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        LoggerFactory.getLogger("peepo-cutting-batch-test").info("[peepo-cutting-batch-test] PASS {} assertions",assertions);
    }
}
