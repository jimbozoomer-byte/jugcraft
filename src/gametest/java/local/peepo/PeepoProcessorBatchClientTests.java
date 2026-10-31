package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.machine.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import org.slf4j.LoggerFactory;

/** Bounded native processor pickup plans plus actual Supply/work/Output cycles on both companion rigs. */
public final class PeepoProcessorBatchClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private MachineBlockEntity machine;
    private Container source,output;
    private Item product;
    private int assertions;
    private void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    private void phase(String text){LoggerFactory.getLogger("peepo-processor-batch").info("[peepo-processor-batch] {}",text);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private int count(List<ItemStack> stacks,Item item){return stacks.stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    private CompanionLogistics.Port port(){return CompanionLogistics.resolve(npc,npc.assignments.get(1));}
    private List<ItemStack> batch(){var p=port();var storage=ContainerStorage.of(source,Direction.UP);p.prepare(storage);return p.supplyBatch(storage,8);}
    private void recharge(){try(var tx=Transaction.openOuter()){machine.energyFor(null).insert(128,tx);tx.commit();}}
    private void setup(ServerLevel level,MachineKind kind,boolean jughead,int recipes){
        if(npc!=null){npc.transport.releaseStock();npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-7),base.offset(8,4,7)))level.setBlock(p,p.getY()<base.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        var block=JugcraftMachines.MACHINES.get(kind);var state=block.defaultBlockState();level.setBlockAndUpdate(base,state);
        var footprint=block.footprint(state);for(int i=1;i<footprint.size();i++)level.setBlock(footprint.partPos(base,state.getValue(MachineBlock.FACING),i),state.setValue(LargeMachineBlock.PART,i),2);
        machine=(MachineBlockEntity)level.getBlockEntity(base);
        level.setBlockAndUpdate(base.west(5),Blocks.BARREL.defaultBlockState());source=(Container)level.getBlockEntity(base.west(5));
        level.setBlockAndUpdate(base.east(5),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(5));
        product=kind==MachineKind.ALLOY_SMELTER?BuiltInRegistries.ITEM.getValue(Identifier.parse("jugcraft:bronze_ingot")):Items.IRON_INGOT;
        machine.companionPort.filter().set(0,new ItemStack(product));
        if(kind==MachineKind.ELECTRIC_FURNACE)source.setItem(0,new ItemStack(Items.IRON_ORE,recipes));
        else{
            machine.companionPort.select(new ItemStack(product));int slot=0;
            for(var part:machine.companionPort.selection().parts())source.setItem(slot++,new ItemStack(part.ingredient().items().findFirst().orElseThrow(),part.count()*recipes));
        }
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);
        npc.snapTo(base.getX()-2.5,base.getY(),base.getZ()-3.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assign(level,base);npc.assignments.assignContainer(level,base.west(5),Direction.UP,false);npc.assignments.assignContainer(level,base.east(5),Direction.UP,true);
        recharge();
    }
    private void insert(List<ItemStack> stacks,boolean commit){
        try(var scope=MachineItemAutomation.companionTransfer();var tx=Transaction.openOuter()){
            for(var stack:stacks)check(port().inputs().insert(ItemVariant.of(stack),stack.getCount(),tx)==stack.getCount(),"accept committed batch "+stack);
            if(commit)tx.commit();
        }
    }
    private void planning(ServerLevel level,MachineKind kind,boolean jughead){
        setup(level,kind,jughead,16);var planned=batch();var recipe=machine.companionPort.selection();
        for(var part:recipe.parts()){
            var item=part.ingredient().items().findFirst().orElseThrow().value();
            check(count(planned,item)==part.count()*8,"balanced eight-recipe input: "+item);
            check(count(source,item)==part.count()*16,"planning restores Supply: "+item);
        }
        check(machine.isEmpty(),"planning restores every native machine slot");
        insert(planned,false);check(machine.isEmpty(),"explicit delivery rollback restores destination");
        insert(planned,true);check(machine.companionPort.loadedBatch(),"loaded native batch recognized");
        check(batch().isEmpty(),"loaded batch never gets endlessly topped up");
        for(int i=0;i<kind.outputSlot();i++)if(!machine.getItem(i).isEmpty())machine.getItem(i).shrink(1);
        check(batch().isEmpty(),"partly processed complete batch still never tops up");
        for(int i=0;i<machine.getContainerSize();i++)machine.setItem(i,ItemStack.EMPTY);
        machine.setItem(kind.outputSlot(),new ItemStack(product,new ItemStack(product).getMaxStackSize()));
        check(batch().isEmpty(),"finished output blocks another pickup until collection");
        check(machine.companionPort.finishedOutput(),"resident job can detect completed products");
        setup(level,kind,jughead,16);batch();recipe=machine.companionPort.selection();
        var scarce=recipe.parts().getLast();source.setItem(recipe.parts().size()-1,new ItemStack(scarce.ingredient().items().findFirst().orElseThrow(),scarce.count()*2));
        planned=batch();for(var part:recipe.parts())check(count(planned,part.ingredient().items().findFirst().orElseThrow().value())==part.count()*2,"scarce ingredient bounds every input");
        setup(level,kind,jughead,16);var stock=machine.companionPort.filter.stock;stock.useOutputs(npc,npc.assignments.get(1));
        batch();recipe=machine.companionPort.selection();stock.set(new ItemStack(product),recipe.results().getFirst().getCount()*3,0);
        planned=batch();for(var part:recipe.parts())check(count(planned,part.ingredient().items().findFirst().orElseThrow().value())==part.count()*3,"stock target caps bulk inputs");
        check(machine.isEmpty(),"stock pickup simulation rolls back all native slots");
    }
    private void componentsAndAutomation(ServerLevel level){
        setup(level,MachineKind.ELECTRIC_FURNACE,false,4);
        var named=new ItemStack(Items.IRON_ORE,4);named.set(DataComponents.CUSTOM_NAME,Component.literal("Separate ore"));source.setItem(1,named);
        var planned=batch();check(planned.size()==1 && planned.getFirst().getCount()==4,"different item components never pretend to stack in one input");
        check(machine.isEmpty() && count(source,Items.IRON_ORE)==8,"component plan is speculative");
        machine.setItem(0,new ItemStack(Items.IRON_ORE,3));machine.companionPort.select(ItemStack.EMPTY);port().prepare(ContainerStorage.of(source,Direction.UP));
        check(machine.companionPort.plan()!=null && machine.companionPort.loadedBatch(),"manual complete recipe recovers selected plan");
        check(batch().isEmpty(),"manual ingredients are worked before another pickup");
        machine.setItem(0,ItemStack.EMPTY);
        try(var tx=Transaction.openOuter()){check(ContainerStorage.of(machine,Direction.UP).insert(ItemVariant.of(Items.IRON_ORE),3,tx)==3,"native automation still inserts normally");tx.commit();}
        check(batch().isEmpty(),"externally automated inputs are also not topped up");
        machine.setItem(0,ItemStack.EMPTY);
        for(var face:SideConfig.Face.values())while(machine.sides().mode(face)!=SideConfig.Mode.NONE)machine.sides().click(face.ordinal());
        check(batch().isEmpty(),"closed native input sides forbid companion pickup");
        check(port().needed(new ItemStack(Items.IRON_ORE))==0,"closed sides also reject already-carried input");
    }
    private void inFlight(ServerLevel level){
        setup(level,MachineKind.ELECTRIC_FURNACE,false,8);var planned=batch();var raw=planned.getFirst();
        source.clearContent();npc.belongings.setItem(0,raw.copy());
        var data=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,npc.registryAccess());var cargo=data.child("Transport");
        cargo.store("Manifest",ItemStack.CODEC,raw);cargo.putInt("Slot",0);cargo.putBoolean("Supply",true);cargo.putBoolean("StockTracked",true);
        CompanionTransport.saveTarget(cargo,"Work",npc.assignments.get(1));CompanionTransport.saveTarget(cargo,"Store",npc.assignments.get(CompanionAssignments.SUPPLY));cargo.store("Recipe",Identifier.CODEC,port().plan());
        npc.transport.load(TagValueInput.create(ProblemReporter.DISCARDING,npc.registryAccess(),data.buildResult()));
        var different=raw.copy();different.set(DataComponents.CUSTOM_NAME,Component.literal("Other courier ore"));source.setItem(0,different);
        check(!machine.companionPort.filter.stock.enabled(),"incoming reservation fixture does not rely on stock targets");
        check(batch().isEmpty(),"another supplier cannot overwrite exact variant plan during a real incoming manifest");
        check(port().needed(raw)==raw.getCount(),"original physical cargo remains acceptable");insert(planned,false);
        npc.transport.releaseStock();npc.transport.stop();npc.belongings.setItem(0,ItemStack.EMPTY);
        check(!machine.companionPort.filter.stock.hasIncoming(machine),"cancelled manifest releases pickup reservation");
    }
    @SuppressWarnings("unchecked")
    private void overlappingParts(ServerLevel level){
        setup(level,MachineKind.ALLOY_SMELTER,false,8);batch();var original=machine.companionPort.selection();
        // A temporary catalog fixture exercises data-pack overlapping tags without changing registered recipes.
        try{
            var field=MachineCompanionPort.class.getDeclaredField("CATALOG");field.setAccessible(true);
            var catalogs=(Map<MachineKind,Map<Identifier,MachineCompanionPort.Plan>>)field.get(null);var previous=catalogs.get(MachineKind.ALLOY_SMELTER);
            try{
                var fixture=new MachineCompanionPort.Plan(original.id(),List.of(
                    new MachineCompanionPort.Part(Ingredient.of(Items.IRON_INGOT,Items.COPPER_INGOT),1),
                    new MachineCompanionPort.Part(Ingredient.of(Items.IRON_INGOT),1)),original.results(),false,original.recipe());
                catalogs.put(MachineKind.ALLOY_SMELTER,Map.of(fixture.id(),fixture));
                source.clearContent();source.setItem(0,new ItemStack(Items.IRON_INGOT,8));source.setItem(1,new ItemStack(Items.COPPER_INGOT,8));
                var planned=batch();check(count(planned,Items.IRON_INGOT)==8 && count(planned,Items.COPPER_INGOT)==8,"narrow ingredient arriving first does not steal broad ingredient slot");
                insert(planned,false);check(machine.isEmpty(),"overlapping ingredient probe rolls back slot allocation");
                var search=MachineCompanionPort.class.getDeclaredMethod("balancedInputs",MachineCompanionPort.Plan.class,List.class,int[].class,int[].class);search.setAccessible(true);
                int[] exhausted={0},reserve={256};
                var fallback=(List<ItemStack>)search.invoke(machine.companionPort,fixture,List.of(new ItemStack(Items.IRON_INGOT,8),new ItemStack(Items.COPPER_INGOT,8)),exhausted,reserve);
                check(fallback.size()==2 && fallback.stream().allMatch(stack->stack.getCount()==1),"exhausted bulk search still offers one balanced recipe");
                check(exhausted[0]==0 && reserve[0]>=0 && reserve[0]<256,"single-recipe fallback uses only its shared bounded reserve");
                insert(List.of(new ItemStack(Items.IRON_INGOT,8)),true);
                var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());machine.companionPort.save(saved);
                machine.companionPort.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
                check(machine.getItem(0).isEmpty() && machine.getItem(1).is(Items.IRON_INGOT),"saved guidance recreates no inventory");
                insert(List.of(new ItemStack(Items.COPPER_INGOT,8)),true);
                check(machine.getItem(0).is(Items.COPPER_INGOT) && machine.getItem(1).is(Items.IRON_INGOT),"partial real deliveries retain distinct slots through reload");
            }finally{catalogs.put(MachineKind.ALLOY_SMELTER,previous);}
        }catch(ReflectiveOperationException failure){throw new AssertionError("overlapping recipe fixture",failure);}
    }
    private void cycle(TestServerContext server,MachineKind kind,boolean jughead,boolean target){
        String name=kind+" / "+(jughead?"Jughead":"Peepo")+(target?" / target":"");phase("physical "+name);
        boolean surplus=!target && !jughead && kind==MachineKind.ELECTRIC_FURNACE;int supplied=target || surplus?16:8,recipes=target?3:8;
        final int[] expected={0};boolean[] loaded={false},worked={false},collecting={false},residentCompletion={false};long[] lastWork={Long.MIN_VALUE},completed={-1};
        server.runOnServer(s->{setup(s.overworld(),kind,jughead,supplied);batch();var recipe=machine.companionPort.selection();expected[0]=recipe.results().getFirst().getCount()*recipes;
            if(target){var stock=machine.companionPort.filter.stock;stock.useOutputs(npc,npc.assignments.get(1));stock.set(new ItemStack(product),expected[0],0);}npc.setNoAi(false);});
        try{server.waitFor(s->{
            recharge();long now=s.overworld().getGameTime();
            if(npc.isUsingJobAt(base) && npc.routineStatus()==CompanionStatus.WORKING){worked[0]=true;lastWork[0]=now;}
            int carried=count(npc.belongings,product),delivered=count(output,product);boolean picked=carried>0 || delivered>0;
            if(machine.companionPort.loadedBatch()){
                loaded[0]=true;check(!picked,name+" finishes loaded recipes before hauling partial products");
                if(surplus)check(count(source,Items.IRON_ORE)==8,name+" leaves surplus Supply untouched during its loaded batch");
            }else if(loaded[0] && count(machine,product)+carried+delivered>=expected[0]){
                if(completed[0]<0){completed[0]=now;residentCompletion[0]=worked[0] && now-lastWork[0]<=2;}
                // A powered machine can finish a short recipe before the carrier has reached its work position.
                // Timed handoff applies to a helper actually resident at the final native operation.
                if(residentCompletion[0]){
                    var activity=npc.transport.activity();collecting[0]|=activity==CompanionStatus.COLLECTING_OUTPUT || activity==CompanionStatus.DELIVERING || picked;
                    check(now-completed[0]<=10 || collecting[0],name+" starts completed Output collection within ten ticks: "+activity);
                    check(now-completed[0]<=40 || picked,name+" picks up finished products before a later scheduler retry");
                    if(!picked)check(npc.getRestMode()==CompanionEnergy.Rest.NONE && activity!=CompanionStatus.FETCHING_SUPPLIES,name+" keeps delivery ahead of leisure or another input trip");
                }
            }
            return delivered>=expected[0];
        },3500);}
        catch(AssertionError failure){throw new AssertionError(server.computeOnServer(s->name+" stalled: "+npc.routineStatus()+" / "+npc.transport.activity()+" / "+port().status()+" at "+npc.position()+", inputs="+CompanionStockRecipes.inputs(machine)),failure);}
        server.runOnServer(s->{check(count(output,product)==expected[0],name+" delivers entire expected batch");check(!machine.companionPort.loadedBatch(),name+" has consumed all loaded ingredients");
            check(loaded[0] && completed[0]>=0,name+" observed physical input-to-completion transition");
            if(!target){check(worked[0],name+" companion actually assisted its native processor");check(residentCompletion[0],name+" companion stayed resident through its long loaded batch");}
            check(count(npc.belongings,product)==0,name+" cargo really reached Output");
            var recipe=machine.companionPort.selection();for(var part:recipe.parts())check(count(source,part.ingredient().items().findFirst().orElseThrow().value())==part.count()*(supplied-recipes),name+" exact source conservation");
            npc.setNoAi(true);npc.transport.stop();npc.resetCompanionRoutine();});
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand(String.format(Locale.ROOT,"tp @a %.1f %d %.1f",base.getX()+.5,base.getY(),base.getZ()-5.5));
            server.runOnServer(s->{for(boolean jughead:new boolean[]{false,true})for(var kind:List.of(MachineKind.ELECTRIC_FURNACE,MachineKind.ALLOY_SMELTER))planning(s.overworld(),kind,jughead);componentsAndAutomation(s.overworld());overlappingParts(s.overworld());inFlight(s.overworld());});
            for(boolean jughead:new boolean[]{false,true})for(var kind:List.of(MachineKind.ELECTRIC_FURNACE,MachineKind.ALLOY_SMELTER)){cycle(server,kind,jughead,false);cycle(server,kind,jughead,true);}
            server.runOnServer(s->{npc.transport.releaseStock();npc.discard();});
        }
        phase("PASS "+assertions+" assertions");
    }
}
