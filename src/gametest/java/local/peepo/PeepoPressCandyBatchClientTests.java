package local.peepo;

import java.util.*;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import org.slf4j.LoggerFactory;

/** Real press finishing supplies and candy completion delivery, including passive release on both rigs. */
public final class PeepoPressCandyBatchClientTests implements FabricClientGameTest {
    private BlockPos base;private PeepoEntity npc;private CiderPressBlockEntity press,alternative;private CandyKettleBlockEntity kettle;
    private Container source,output;private int assertions;
    private final Item cider=JugcraftAgriculture.item("sweet_cider"),pomace=JugcraftAgriculture.item("apple_pomace");
    private void check(boolean value,String message){assertions++;if(!value)throw new AssertionError(message);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private int count(Storage<ItemVariant> storage,Item item){int n=0;for(var v:storage)if(v.getResource().getItem()==item)n+=(int)v.getAmount();return n;}
    private CompanionLogistics.Port port(){return CompanionLogistics.resolve(npc,npc.assignments.get(1));}
    private List<ItemStack> batch(){var port=port();var storage=ContainerStorage.of(source,Direction.UP);port.prepare(storage);return port.supplyBatch(storage,8);}
    private CompanionJob job(){return press!=null?press.companionJob:kettle.companionJob;}
    private CompanionRecipeFilter filter(){return press!=null?press.companionFilter:kettle.companion.filter;}
    private void goal(Item item,int count){filter().set(0,new ItemStack(item));filter().stock.set(new ItemStack(item),count,1);filter().stock.useOutputs(npc,npc.assignments.get(1));}
    private void reset(ServerLevel level,boolean pink,boolean candy){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-2,-7),base.offset(8,4,7)))level.setBlock(p,p.getY()<base.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        level.setBlockAndUpdate(base,JugcraftAgriculture.block(candy?"candy_kettle":"cider_press").defaultBlockState());
        press=candy?null:(CiderPressBlockEntity)level.getBlockEntity(base);kettle=candy?(CandyKettleBlockEntity)level.getBlockEntity(base):null;
        if(candy)level.setBlockAndUpdate(base.below(),Blocks.CAMPFIRE.defaultBlockState());
        level.setBlockAndUpdate(base.south(4),JugcraftAgriculture.block("cider_press").defaultBlockState());alternative=(CiderPressBlockEntity)level.getBlockEntity(base.south(4));
        level.setBlockAndUpdate(base.west(5),Blocks.BARREL.defaultBlockState());source=(Container)level.getBlockEntity(base.west(5));
        level.setBlockAndUpdate(base.east(5),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(5));
        npc=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);
        npc.snapTo(base.getX()-3.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);
        npc.preferences.social=false;npc.preferences.carryMeals=0;npc.assignments.assign(level,base);npc.assignments.assign(level,alternative.getBlockPos());
        npc.assignments.assignContainer(level,base.west(5),Direction.UP,false);npc.assignments.assignContainer(level,base.east(5),Direction.UP,true);
    }
    private void candySupplies(int sugar){
        source.setItem(0,new ItemStack(Items.SUGAR,sugar));source.setItem(1,PotionContents.createItemStack(Items.POTION,Potions.WATER));
        source.setItem(2,new ItemStack(JugcraftAgriculture.item("candy_tray")));source.setItem(3,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("blue_dye"))));
    }
    private void insert(List<ItemStack> stacks){try(var tx=Transaction.openOuter()){for(var stack:stacks)check(port().inputs().insert(ItemVariant.of(stack),stack.getCount(),tx)==stack.getCount(),"insert planned physical inputs");tx.commit();}}
    private void planning(ServerLevel level){
        reset(level,false,false);goal(cider,2);source.setItem(0,new ItemStack(Items.APPLE,32));source.setItem(1,new ItemStack(Items.GLASS_BOTTLE,32));
        var batch=batch();
        check(batch.size()==1 && batch.getFirst().is(Items.APPLE) && batch.getFirst().getCount()==2,"press stock target requests only two apples");
        check(press.apples()==0 && count(source,Items.APPLE)==32,"press batch simulation rolls back");insert(batch);
        for(int i=0;i<2;i++)press.grind(level.getGameTime());for(int i=0;i<4;i++)press.turn(level.getGameTime());
        check(job().completionNeedsSupplies(npc) && !job().finishLoadedBatch(npc),"juice needs finishing bottles rather than another work session");
        batch=batch();
        check(batch.size()==1 && batch.getFirst().is(Items.GLASS_BOTTLE) && batch.getFirst().getCount()==2,"stock target still permits bottles for already committed juice");
        check(press.juice()==2 && count(press.companionOutputs(),cider)==0,"bottle planning rolls conversion back");insert(batch);
        check(press.juice()==0 && count(press.companionOutputs(),cider)==2 && port().needed(new ItemStack(Items.APPLE))==0,"bottled output blocks another apple batch");
        var saved=press.saveWithFullMetadata(level.registryAccess());press.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(count(press.companionOutputs(),cider)==2,"finished cider survives reload");
        reset(level,false,true);goal(CandyKind.HARD_CANDY.item(),3);candySupplies(16);
        batch=batch();
        check(batch.stream().filter(s->s.is(Items.SUGAR)).mapToInt(ItemStack::getCount).sum()==2,"target three requests two sugar for four native candy pieces");
        check(!kettle.batch() && count(source,Items.SUGAR)==16,"candy planning does not start or consume a batch");insert(batch);
        check(kettle.companion.stockPending(List.of())==4 && job().finishLoadedBatch(npc),"loaded sugar counts once and finite work can finish");
        npc.setStoredEnergy(0);var station=kettle.companionJob.prepare(npc);var stand=station.approachPosition();npc.snapTo(stand.x,stand.y,stand.z,0,0);
        check(station.claim(npc) && station.occupy(npc),"claim prepared candy work");
        check(station.work(npc)==CompanionStatus.IDLE && !kettle.batch() && kettle.companion.stockPending(List.of())==4,"depleted worker cannot start the staged batch");
        check(kettle.companion.assist(npc)==CompanionStatus.RECOVERING && !kettle.batch() && kettle.companion.stockPending(List.of())==4 && count(kettle.companion.outputs(),Items.GLASS_BOTTLE)==0,"failed native energy debit preserves ingredients and container remainder");station.release(npc);
    }
    private void pressCycle(TestServerContext server,boolean pink,boolean limited){
        int amount=limited?2:8;
        server.runOnServer(s->{reset(s.overworld(),pink,false);if(limited)goal(cider,amount);source.setItem(0,new ItemStack(Items.APPLE,limited?16:amount));source.setItem(1,new ItemStack(Items.GLASS_BOTTLE,amount));npc.setNoAi(false);});
        server.waitFor(s->press.pulp()>0,900);server.runOnServer(s->alternative.addApple());
        server.waitFor(s->{
            check(alternative.apples()==1,"loaded press finishes before lower-priority work");
            check(count(output,pomace)==0 && count(npc.belongings,pomace)==0,"pomace is not collected from a partial pressing sequence");
            return press.apples()==0 && press.pulp()==0 && press.juice()==amount;
        },300);
        long finished=server.computeOnServer(s->s.overworld().getGameTime());
        server.waitFor(s->{
            check(alternative.apples()==1 && !npc.isUsingJobAt(alternative.getBlockPos()),"finish bottles and collect current press before another job");
            boolean loaded=count(press.companionOutputs(),cider)==amount || count(npc.belongings,cider)==amount;
            if(s.overworld().getGameTime()-finished>10)check(npc.transport.activity()!=CompanionStatus.IDLE,"finished press enters bottle Supply without idle retry");
            check(loaded || count(npc.belongings,pomace)==0 && count(output,pomace)==0,"bottles precede separate pomace haul");
            return count(npc.belongings,cider)==amount && count(npc.belongings,pomace)==(amount+1)/2;
        },900);
        server.waitFor(s->count(output,cider)==amount && count(output,pomace)==(amount+1)/2,900);
        server.runOnServer(s->{check(count(source,Items.GLASS_BOTTLE)==0 && count(source,Items.APPLE)==(limited?14:0),"exact real apple and bottle consumption");check(count(press.companionOutputs(),cider)==0 && count(press.companionOutputs(),pomace)==0 && press.juice()==0,"whole cider batch and pomace delivered");});
    }
    private void candyCycle(TestServerContext server,boolean pink,boolean limited){
        int pieces=limited?4:8;
        server.runOnServer(s->{reset(s.overworld(),pink,true);if(limited)goal(CandyKind.HARD_CANDY.item(),3);candySupplies(limited?16:4);npc.setNoAi(false);});
        server.waitFor(s->kettle.batch(),900);server.runOnServer(s->kettle.setTemperature(CandyStage.HARD_CRACK.from));
        server.waitFor(s->kettle.companion.status()==CompanionStatus.CANDY_SETTING,300);
        server.runOnServer(s->check(!job().timeCritical() && !job().finishLoadedBatch(npc),"setting candy does not reserve an active worker"));
        server.waitFor(s->count(kettle.companion.outputs(),CandyKind.HARD_CANDY.item())==pieces,1000);
        server.runOnServer(s->alternative.addApple());long finished=server.computeOnServer(s->s.overworld().getGameTime());
        server.waitFor(s->{
            check(alternative.apples()==1 && !npc.isUsingJobAt(alternative.getBlockPos()),"finished candy is collected before lower-priority work");
            boolean picked=count(npc.belongings,CandyKind.HARD_CANDY.item())==pieces;
            if(s.overworld().getGameTime()-finished>10)check(picked || npc.transport.activity()==CompanionStatus.COLLECTING_OUTPUT,"finished candy enters immediate output collection");
            check(s.overworld().getGameTime()-finished<=40 || picked,"candy pickup precedes ordinary scheduler retry");return picked;
        },100);
        server.waitFor(s->count(output,CandyKind.HARD_CANDY.item())==pieces,900);
        server.runOnServer(s->{
            check(count(source,Items.SUGAR)==(limited?14:0) && count(output,Items.GLASS_BOTTLE)==1,"candy preserves exact sugar and bottle remainder");
            for(int i=0;i<output.getContainerSize();i++)if(output.getItem(i).is(CandyKind.HARD_CANDY.item()))check(output.getItem(i).has(DataComponents.DYED_COLOR),"candy delivery preserves native colour component");
            check(kettle.companion.stockPending(List.of())==0,"delivered candy clears pending production");
        });
    }
    private void unavailableOutput(TestServerContext server,boolean pink,boolean candy){
        server.runOnServer(s->{
            reset(s.overworld(),pink,candy);for(int i=0;i<output.getContainerSize();i++)output.setItem(i,new ItemStack(Items.STONE,64));
            if(candy){goal(CandyKind.HARD_CANDY.item(),8);candySupplies(4);insert(batch());source.clearContent();}
            else {for(int i=0;i<8;i++)press.addApple();source.setItem(0,new ItemStack(Items.GLASS_BOTTLE,8));}
            npc.setNoAi(false);
        });
        if(candy){server.waitFor(s->kettle.batch(),900);server.runOnServer(s->kettle.setTemperature(CandyStage.HARD_CRACK.from));server.waitFor(s->count(kettle.companion.outputs(),CandyKind.HARD_CANDY.item())==8,1300);}
        else server.waitFor(s->count(press.companionOutputs(),cider)==8,1300);
        server.runOnServer(s->alternative.addApple());server.waitFor(s->npc.isUsingJobAt(alternative.getBlockPos()),180);
        server.runOnServer(s->{check(!npc.transport.hasPendingDelivery(),"full Output releases completion handoff");check(count(port().outputs(),candy?CandyKind.HARD_CANDY.item():cider)==8,"full Output retains every completed product");output.clearContent();});
        server.waitFor(s->count(output,candy?CandyKind.HARD_CANDY.item():cider)==8,1000);
    }
    private void passiveCrystals(TestServerContext server,boolean pink){
        server.runOnServer(s->{reset(s.overworld(),pink,true);goal(CandyKind.ROCK_CANDY.item(),8);candySupplies(4);npc.setNoAi(false);});
        server.waitFor(s->kettle.batch(),900);server.runOnServer(s->kettle.setTemperature(CandyStage.THREAD.from));
        server.waitFor(s->kettle.companion.status()==CompanionStatus.CANDY_SETTING,300);
        server.runOnServer(s->{check(!job().timeCritical() && !job().finishLoadedBatch(npc),"one-day crystallization has no active work reservation");alternative.addApple();});
        server.waitFor(s->npc.isUsingJobAt(alternative.getBlockPos()),300);
        server.runOnServer(s->check(kettle.companion.status()==CompanionStatus.CANDY_SETTING && kettle.companion.stockPending(List.of())==8,"other work proceeds while the real candy tray crystallizes"));
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand(String.format(Locale.ROOT,"tp @a %.1f %d %.1f",base.getX()+.5,base.getY(),base.getZ()-5.5));
            server.runOnServer(s->planning(s.overworld()));
            for(boolean pink:new boolean[]{false,true}){pressCycle(server,pink,false);pressCycle(server,pink,true);candyCycle(server,pink,false);candyCycle(server,pink,true);unavailableOutput(server,pink,false);unavailableOutput(server,pink,true);passiveCrystals(server,pink);}
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        LoggerFactory.getLogger("peepo-press-candy-batch-test").info("[peepo-press-candy-batch-test] PASS {} assertions",assertions);
    }
}
