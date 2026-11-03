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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Native quern/churn transactions, worker/energy safety and autonomous filtered stock-target deliveries. */
public final class PeepoHandFarmClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private Container source,output;
    private HandWorkedBlockEntity block;
    private HandFarmCompanion farm;
    private int checks;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private long count(Storage<ItemVariant> c,Item item){long n=0;for(var view:c)if(view.getResource().isOf(item))n+=view.getAmount();return n;}
    private void reset(ServerLevel level,boolean jughead,boolean churn){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-5),base.offset(8,4,5)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        level.setBlockAndUpdate(base.west(6),Blocks.BARREL.defaultBlockState());source=(Container)level.getBlockEntity(base.west(6));
        level.setBlockAndUpdate(base.east(6),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(6));
        level.setBlockAndUpdate(base,JugcraftAgriculture.block(churn?"butter_churn":"quern").defaultBlockState());block=(HandWorkedBlockEntity)level.getBlockEntity(base);farm=HandFarmCompanion.of(block);
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assignContainer(level,base.west(6),Direction.UP,false);npc.assignments.assignContainer(level,base.east(6),Direction.UP,true);
        check(npc.assignments.assign(level,base)!=null && npc.assignments.assignedWork(base),"planner assigns hand-worked station");
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("gamemode spectator @a");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{nativeRecipes(s.overworld());menus(s.overworld());transactions(s.overworld());for(boolean pink:new boolean[]{false,true})churnQueue(s.overworld(),pink);});
            paprika(server);
            for(boolean jughead:new boolean[]{false,true})for(boolean churn:new boolean[]{false,true})delivery(server,jughead,churn);
            for(boolean jughead:new boolean[]{false,true})blockedOutput(server,jughead);
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-hand-farm-test").info("[peepo-hand-farm-test] PASS {} assertions",checks);
    }
    private void nativeRecipes(ServerLevel level){
        reset(level,false,false);var quern=(QuernBlockEntity)block;var wheat=new ItemStack(Items.WHEAT);
        check(quern.add(wheat)==1 && wheat.isEmpty(),"manual quern still takes the real ingredient");long now=level.getGameTime();
        check(quern.turn(now).isEmpty() && !quern.ready(now),"manual quern first turn and cooldown unchanged");
        check(quern.ready(now+QuernBlock.SPIN_TICKS) && quern.turn(now+QuernBlock.SPIN_TICKS).is(JugcraftAgriculture.item("flour")) && quern.grain().isEmpty(),"manual quern keeps its native yield");
        reset(level,true,true);var churn=(ButterChurnBlockEntity)block;check(churn.fill() && !churn.fill(),"manual churn still holds one milk bucket");
        for(int i=0;i<ButterChurnBlock.STROKES;i++){var made=churn.plunge(now+i*ButterChurnBlock.STROKE_TICKS);check(i<ButterChurnBlock.STROKES-1?made.isEmpty():made.is(JugcraftAgriculture.item("butter")) && made.getCount()==2,"manual churn preserves eight-stroke two-butter recipe");}
    }
    private void menus(ServerLevel level){
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        for(boolean churn:new boolean[]{false,true}){
            reset(level,churn,churn);owner.snapTo(npc.getX(),npc.getY(),npc.getZ()-2,0,0);
            owner.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new CompanionMenu(id,inventory,npc),npc.getDisplayName()));
            var menu=(CompanionMenu)owner.containerMenu;
            check(menu.value(25)==16 && menu.clickMenuButton(owner,60) && menu.filterRow()==0,"companion menu exposes and opens the hand-worked station Filter");
            var icon=new ItemStack(JugcraftAgriculture.item(churn?"butter":"flour"));menu.setCarried(icon.copy());
            menu.clicked(CompanionMenu.FILTER_START,0,net.minecraft.world.inventory.ContainerInput.PICKUP,owner);
            check(ItemStack.isSameItemSameComponents(farm.filter.get(0),icon) && menu.getCarried().getCount()==1,"Filter ghost selection updates the station without consuming the cursor item");
            menu.setCarried(ItemStack.EMPTY);owner.closeContainer();
        }
        owner.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
    }
    private void transactions(ServerLevel level){
        for(boolean churn:new boolean[]{false,true}){
            reset(level,churn,churn);var raw=churn?Items.MILK_BUCKET:Items.WHEAT;var product=JugcraftAgriculture.item(churn?"butter":"flour");var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));
            check(!block.getBlockState().isPathfindable(net.minecraft.world.level.pathfinder.PathComputationType.LAND),"inset equipment cannot be routed through");
            check(CompanionFilters.get(block)==farm.filter && CompanionProductionStock.of(block)==farm.filter.stock,"existing Filter and stock UI uses station state");
            check(!CompanionFilters.set(block,0,new ItemStack(Items.DIAMOND)),"invalid ghost filter rejected");check(CompanionFilters.set(block,0,new ItemStack(product)),"product ghost filter accepted");
            check(port.needed(new ItemStack(Items.STONE))==0,"unrelated input rejected");
            try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(raw),1,tx)==1,"native ingredient can be supplied");}
            check(!farm.busy() && count(farm.outputs(),Items.BUCKET)==0,"aborted input creates neither contents nor a free bucket");
            try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(raw),1,tx)==1,"committed input");tx.commit();}
            check(farm.busy() && count(farm.outputs(),Items.BUCKET)==(churn?1:0),"milk bucket converts exactly once");
            if(churn){try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(raw),1,tx)==1,"filled churn stages another real bucket transactionally");}check(farm.queuedMilkCount()==0,"aborted extra bucket leaves native load and queue unchanged");}
            var job=farm.job.prepare(npc);check(job.workStatus(npc)==CompanionStatus.READY,"supplied station ready");var at=job.approachPosition();npc.snapTo(at.x,at.y,at.z,0,0);check(job.claim(npc) && job.occupy(npc),"worker occupies safe ground position");
            int energy=npc.getEnergy();check(job.work(npc)==CompanionStatus.WORKING,"real hand-work stroke advances");check(npc.getEnergy()==energy-HandFarmCompanion.ENERGY_PER_TICK,"energy charged for working tick");
            int progress=churn?((ButterChurnBlockEntity)block).strokes():((QuernBlockEntity)block).turns();job.work(npc);check(npc.getEnergy()==energy-HandFarmCompanion.ENERGY_PER_TICK && progress==(churn?((ButterChurnBlockEntity)block).strokes():((QuernBlockEntity)block).turns()),"same-tick replay adds no stroke or energy charge");
            var other=PeepoMod.PEEPO.create(level,EntitySpawnReason.COMMAND);other.setNoAi(true);level.addFreshEntity(other);check(!job.availableTo(other),"second helper cannot take occupied station");other.discard();
            var saved=block.saveWithFullMetadata(level.registryAccess());block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            check(farm.busy() && progress==(churn?((ButterChurnBlockEntity)block).strokes():((QuernBlockEntity)block).turns()) && count(farm.outputs(),Items.BUCKET)==(churn?1:0),"native progress and companion products survive reload");
            var tray=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());tray.store("CompanionProducts",ItemStack.CODEC.listOf(),List.of(new ItemStack(product,HandFarmCompanion.OUTPUT_CAPACITY)));farm.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tray.buildResult()));
            int fullEnergy=npc.getEnergy();check(job.work(npc)==CompanionStatus.FULL && npc.getEnergy()==fullEnergy && farm.busy(),"full tray preserves input, progress and energy");
            block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            npc.setStoredEnergy(2);
            check(farm.assist(npc)==CompanionStatus.RECOVERING && npc.getEnergy()==2 && farm.busy() && progress==(churn?((ButterChurnBlockEntity)block).strokes():((QuernBlockEntity)block).turns()),"insufficient energy rolls back the partial debit and leaves native progress unchanged");
            npc.setStoredEnergy(0);check(job.work(npc)==CompanionStatus.IDLE,"exhausted helper does not start work");
            npc.assignments.clear(1);try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(raw),1,tx)==0 && port.outputs().extract(ItemVariant.of(Items.BUCKET),1,tx)==0,"cached port refuses removed work assignment");}
        }
    }
    private void churnQueue(ServerLevel level,boolean pink){
        reset(level,pink,true);var churn=(ButterChurnBlockEntity)block;var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));
        var named=new ItemStack(Items.MILK_BUCKET);named.set(DataComponents.CUSTOM_NAME,Component.literal("Reserved milk"));var milk=ItemVariant.of(named);
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(milk,8,tx)==8,"eight milk buckets fit the bounded churn batch");}
        check(!farm.busy() && farm.queuedMilkCount()==0 && count(farm.outputs(),Items.BUCKET)==0,"aborted batch restores every queued bucket and remainder");
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(milk,8,tx)==8,"bounded batch commits");check(port.inputs().insert(milk,1,tx)==0,"ninth milk bucket cannot overfill batch");tx.commit();}
        check(churn.milk() && farm.queuedMilkCount()==7 && count(farm.outputs(),Items.BUCKET)==1,"one native load and seven real queued milk buckets");
        check(farm.pending(new ItemStack(JugcraftAgriculture.item("butter")),List.of())==16,"stock reserves butter from all queued milk");
        check(port.pickupNeeded(named)==0 && !farm.acceptsSupplyBatch(),"active batch does not ask couriers to top it up");
        var saved=block.saveWithFullMetadata(level.registryAccess());block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(churn.milk() && farm.queuedMilkCount()==7 && count(farm.outputs(),Items.BUCKET)==1,"queued batch and existing remainder survive native reload");
        var job=farm.job.prepare(npc);var at=job.approachPosition();npc.snapTo(at.x,at.y,at.z,0,0);check(job.claim(npc) && job.occupy(npc),"queued churn worker claims safe position");
        check(job.finishLoadedBatch(npc) && job.collectAfterWork(),"native batch requests continuous work and immediate finished-goods handoff");
        var full=saved.copy();var tray=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        tray.store("CompanionProducts",ItemStack.CODEC.listOf(),List.of(new ItemStack(JugcraftAgriculture.item("butter"),64)));full.merge(tray.buildResult());
        block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),full));int energy=npc.getEnergy();
        check(job.work(npc)==CompanionStatus.FULL && npc.getEnergy()==energy && farm.queuedMilkCount()==7 && churn.strokes()==0,"full tray preserves all queued milk and stops work without energy cost");
        check(!job.finishLoadedBatch(npc) && !job.timeCritical(),"full output may be unloaded before continuing queued work");
        block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        for(var item:level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(2)))item.discard();
        level.destroyBlock(base,false);
        var drops=level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(2));
        check(drops.stream().filter(e->e.getItem().is(Items.MILK_BUCKET)).mapToInt(e->e.getItem().getCount()).sum()==7,"breaking churn drops every unpoured real milk bucket once");
        check(drops.stream().filter(e->e.getItem().is(Items.MILK_BUCKET)).allMatch(e->ItemStack.isSameItemSameComponents(e.getItem(),named)),"queued milk keeps exact item components on recovery");
        check(drops.stream().filter(e->e.getItem().is(Items.BUCKET)).mapToInt(e->e.getItem().getCount()).sum()==1,"breaking churn returns only the active load's already-created bucket");
        farm.drop();check(farm.queuedMilkCount()==0 && count(farm.outputs(),Items.BUCKET)==0,"repeat cleanup cannot duplicate returned items");
        drops.forEach(ItemEntity::discard);
    }
    private void paprika(TestServerContext server){
        HandFarmJob[] job={null};long[] last={Long.MIN_VALUE};
        server.runOnServer(s->{reset(s.overworld(),false,false);check(CompanionFilters.set(block,0,new ItemStack(JugcraftAgriculture.item("paprika"))),"paprika filter accepted");var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));check(port.needed(new ItemStack(Items.WHEAT))==0,"paprika filter rejects flour ingredients");
            try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(JugcraftAgriculture.item("dried_chili")),1,tx)==1,"dried chili supplied");tx.commit();}
            job[0]=farm.job.prepare(npc);var at=job[0].approachPosition();npc.snapTo(at.x,at.y,at.z,0,0);check(job[0].claim(npc) && job[0].occupy(npc),"paprika worker claimed");
        });
        server.waitFor(s->{long now=s.overworld().getGameTime();if(now!=last[0]){last[0]=now;job[0].occupy(npc);job[0].work(npc);}return count(farm.outputs(),JugcraftAgriculture.item("paprika"))==2;},80);
        server.runOnServer(s->{check(!farm.busy(),"two native quern turns consume exactly one chili");check(farm.status()==CompanionStatus.FULL,"finished product waits for collection");
            int before=npc.getEnergy();check(job[0].work(npc)==CompanionStatus.FULL && npc.getEnergy()==before,"waiting for collection costs no work energy");
            try(var tx=Transaction.openOuter()){check(farm.outputs().extract(ItemVariant.of(JugcraftAgriculture.item("paprika")),1,tx)==1,"partial output probe");}
            check(count(farm.outputs(),JugcraftAgriculture.item("paprika"))==2,"aborted output extraction preserves products");
        });
    }
    private void delivery(TestServerContext server,boolean jughead,boolean churn){
        var raw=churn?Items.MILK_BUCKET:Items.WHEAT;var product=JugcraftAgriculture.item(churn?"butter":"flour");int target=churn?8:6;
        server.runOnServer(s->{reset(s.overworld(),jughead,churn);CompanionFilters.set(block,0,new ItemStack(product));farm.filter.stock.set(new ItemStack(product),target,1);farm.filter.stock.useOutputs(npc,npc.assignments.get(1));
            if(churn){for(int slot=0;slot<6;slot++)source.setItem(slot,new ItemStack(raw));}else source.setItem(0,new ItemStack(raw,8));npc.setNoAi(false);
        });
        long[] report={0},completed={-1};int[] mostQueued={0};boolean[] loaded={false},picked={false},collecting={false},carrying={false};server.waitFor(s->{
            long now=s.overworld().getGameTime();if(now>=report[0]){report[0]=now+400;org.slf4j.LoggerFactory.getLogger("peepo-hand-farm-test").info("Delivery jughead={} churn={} source={} output={}/{} nativeBusy={} station={} transport={} position={}",jughead,churn,count(source,raw),count(output,product),target,farm.busy(),farm.status(),npc.transport.activity(),npc.position());}
            if(count(npc.belongings,raw)>0 && !farm.busy()){
                carrying[0]=true;check(!farm.acceptsSupplyBatch() && farm.needed(new ItemStack(raw))>0,"in-flight batch blocks redundant pickups while allowing its committed delivery");
            }
            mostQueued[0]=Math.max(mostQueued[0],farm.queuedMilkCount());
            if(farm.busy()){
                loaded[0]=true;check(count(output,product)==0 && count(npc.belongings,product)==0,"finishes the actual loaded batch before partial output hauling");
            }else if(loaded[0] && !picked[0]){
                if(completed[0]<0)completed[0]=now;
                var activity=npc.transport.activity();picked[0]=count(output,product)>0 || count(npc.belongings,product)>0;
                collecting[0]|=activity==CompanionStatus.COLLECTING_OUTPUT || activity==CompanionStatus.DELIVERING || picked[0];
                check(now-completed[0]<=10 || collecting[0],"completed hand-work enters output collection within ten ticks: "+activity);
                check(now-completed[0]<=40 || picked[0],"finished tray is collected before later scheduler retry");
                check(activity!=CompanionStatus.FETCHING_SUPPLIES && npc.getRestMode()==CompanionEnergy.Rest.NONE,"output handoff precedes new supplies and leisure");
            }
            return count(output,product)==target;
        },2200);
        final long[] until={0};server.runOnServer(s->until[0]=s.overworld().getGameTime()+100);server.waitFor(s->s.overworld().getGameTime()>=until[0],140);
        server.runOnServer(s->{check(count(output,product)==target,"autonomous deliveries stop at requested stock for both companions");check(count(source,raw)==2,"stock target leaves excess ingredients in Supply");check(!farm.busy(),"completed stock does not leave an unnecessary new batch");check(carrying[0] && loaded[0] && picked[0],"autonomous physical batch and immediate output handoff observed");if(churn){check(mostQueued[0]==3,"four milk buckets are delivered together before sequential native work");check(count(output,Items.BUCKET)==4,"all four real empty milk buckets reach Output");}});
    }
    private void blockedOutput(TestServerContext server,boolean pink){
        var product=JugcraftAgriculture.item("butter");
        server.runOnServer(s->{reset(s.overworld(),pink,true);for(int i=0;i<output.getContainerSize();i++)output.setItem(i,new ItemStack(Items.STONE,64));
            var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(Items.MILK_BUCKET),2,tx)==2,"two-cycle batch loaded before Output becomes available");tx.commit();}npc.setNoAi(false);
        });
        server.waitFor(s->!farm.busy() && count(farm.outputs(),product)==4,700);
        final long[] until={0};server.runOnServer(s->until[0]=s.overworld().getGameTime()+100);server.waitFor(s->s.overworld().getGameTime()>=until[0],140);
        server.runOnServer(s->{check(count(farm.outputs(),product)==4 && count(farm.outputs(),Items.BUCKET)==2 && count(npc.belongings,product)==0,"full destination leaves complete batch and every bucket safely in tray");output.clearContent();});
        server.waitFor(s->count(output,product)==4 && count(output,Items.BUCKET)==2,1200);
        server.runOnServer(s->check(count(farm.outputs(),product)==0 && count(farm.outputs(),Items.BUCKET)==0,"freed Output receives the complete retained batch"));
    }
}
