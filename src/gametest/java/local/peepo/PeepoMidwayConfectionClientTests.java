package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;

/** Native cart toggles/popping and physical Supply/Output stock-target routes for both companions. */
public final class PeepoMidwayConfectionClientTests implements FabricClientGameTest {
    private final ConfectionTestSupport f=new ConfectionTestSupport();
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();f.base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->cotton(s.overworld()));corn(server);
            for(boolean jughead:new boolean[]{false,true})for(int mode=0;mode<3;mode++)delivery(server,jughead,mode);
            midcycle(server);
            server.runOnServer(s->f.close());
        }
        org.slf4j.LoggerFactory.getLogger("peepo-midway-confection-test").info("[peepo-midway-confection-test] PASS {} assertions",f.checks);
    }
    private void cotton(ServerLevel level){
        for(boolean jughead:new boolean[]{false,true}){
            f.setup(level,"cotton_candy_cart",jughead,false);var product=ConfectionTestSupport.food("cotton_candy");
            f.check(f.port.needed(new ItemStack(Items.SUGAR))==0 && f.port.status()==CompanionStatus.NO_HEAT,"player-off cart is not automatically switched on");
            f.check(!CompanionFilters.set(f.station,0,new ItemStack(Items.DIAMOND)),"cart rejects unrelated product Filter");
            f.menu(product);
            var block=(MultiDecorationBlock)f.station.getBlockState().getBlock();var facing=f.station.getBlockState().getValue(MultiDecorationBlock.FACING);
            for(int i=0;i<block.cells().length;i++){var pos=block.partPos(f.base,facing,i);level.setBlock(pos,level.getBlockState(pos).setValue(MultiDecorationBlock.LIT,true),18);}
            var before=f.station.getBlockState();
            try(var tx=Transaction.openOuter()){
                f.check(f.port.inputs().insert(ItemVariant.of(Items.SUGAR),10,tx)==3,"native spinner holds only three servings");
                f.check(f.port.inputs().insert(ItemVariant.of(Items.PAPER),2,tx)==2,"paper winds two real sugar servings into cones");
                f.check(f.count(f.port.outputs(),product)==2 && f.station.getBlockState().equals(before),"simulated output exists without publishing fill state");
            }
            f.check(f.count(f.port.outputs(),product)==0 && f.station.getBlockState().getValue(CottonCandyCartBlock.FLOSS_LEVEL)==0,"probe rolls back sugar, cones and display");
            f.give(Items.SUGAR,3);f.give(Items.PAPER,3);f.check(f.count(f.port.outputs(),product)==3,"sugar and paper yield native cotton candy exactly once");
            var saved=f.station.saveWithFullMetadata(level.registryAccess());f.station.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));f.check(f.count(f.port.outputs(),product)==3,"completed cones persist on the native cart entity");
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(product),2,tx)==2,"cone extraction probe");}
            f.check(f.count(f.port.outputs(),product)==3,"aborted extraction preserves finished cones");
            var full=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());full.store("CompanionConfections",ItemStack.CODEC.listOf(),List.of(new ItemStack(product,64)));f.confection.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),full.buildResult()));
            f.check(f.port.needed(new ItemStack(Items.SUGAR))==0,"full output tray stops spending sugar");f.safePorts(level,Items.SUGAR);
        }
    }
    private void corn(TestServerContext server){
        var kernel=ConfectionTestSupport.food("corn_kernels");var popcorn=ConfectionTestSupport.food("popcorn");var caramel=ConfectionTestSupport.food("caramel");var coated=ConfectionTestSupport.food("caramel_popcorn");long[] started={0};
        server.runOnServer(s->{f.setup(s.overworld(),"popcorn_wagon",false,false);f.check(f.port.needed(new ItemStack(kernel))==0,"unlit wagon does not accept kernels");});
        server.runOnServer(s->{f.setup(s.overworld(),"popcorn_wagon",false,true);f.menu(popcorn);
            try(var tx=Transaction.openOuter()){f.check(f.port.inputs().insert(ItemVariant.of(kernel),1,tx)==1,"kernel insertion probe");}
            f.check(!f.station.getBlockState().getValue(PopcornWagonBlock.POPPING),"aborted kernel schedules no native pop");
            f.give(kernel,1);started[0]=s.overworld().getGameTime();f.check(f.station.getBlockState().getValue(PopcornWagonBlock.POPPING) && f.count(f.port.outputs(),popcorn)==0,"committed kernel starts native timed popping");
        });
        server.waitFor(s->s.overworld().getGameTime()>=started[0]+PopcornWagonBlock.POP_TICKS-1,100);
        server.runOnServer(s->{if(s.overworld().getGameTime()<started[0]+PopcornWagonBlock.POP_TICKS)f.check(f.count(f.port.outputs(),popcorn)==0,"native popping does not finish early");});
        server.waitFor(s->f.count(f.port.outputs(),popcorn)==1,50);
        server.runOnServer(s->{f.check(s.overworld().getGameTime()>=started[0]+PopcornWagonBlock.POP_TICKS,"native scheduled tick preserves sixty-tick popping");
            var named=new ItemStack(popcorn);named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Not held"));
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(named),1,tx)==0,"property output cannot fabricate requested item components");}
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(popcorn),1,tx)==1,"plain popcorn probe");}
            f.check(f.count(f.port.outputs(),popcorn)==1,"plain output rolls back");
            try(var tx=Transaction.openOuter()){f.check(f.port.outputs().extract(ItemVariant.of(popcorn),1,tx)==1,"plain output takes one real serving");tx.commit();}
            CompanonMode(s.overworld(),coated,caramel);f.give(kernel,1);
            f.check(f.confection.pending(new ItemStack(coated),List.of())==1,"first kernel reserves a caramel batch");
        });
        server.waitFor(s->!f.station.getBlockState().getValue(PopcornWagonBlock.POPPING),100);
        server.runOnServer(s->{f.check(f.count(f.port.outputs(),popcorn)==0,"caramel mode does not haul away its intermediate popcorn");
            f.check(f.confection.limit(new ItemStack(kernel),1,List.of(),out->0)==1,"second kernel may finish a committed pair even at stock limit");f.give(kernel,1);
        });
        server.waitFor(s->!f.station.getBlockState().getValue(PopcornWagonBlock.POPPING),100);
        server.runOnServer(s->{
            try(var tx=Transaction.openOuter()){f.check(f.port.inputs().insert(ItemVariant.of(caramel),1,tx)==1,"coating probe spends two popcorn");}
            f.check(f.station.getBlockState().getValue(PopcornWagonBlock.POPCORN)==2 && f.count(f.port.outputs(),coated)==0,"aborted coating preserves case and products");
            f.give(caramel,1);f.check(f.count(f.port.outputs(),coated)==1 && f.station.getBlockState().getValue(PopcornWagonBlock.POPCORN)==0,"two popcorn plus one caramel make one caramel popcorn");
            var saved=f.station.saveWithFullMetadata(s.overworld().registryAccess());f.station.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,s.overworld().registryAccess(),saved));f.check(f.count(f.port.outputs(),coated)==1 && f.confection.product().is(coated),"caramel selection and goods survive reload");
            f.safePorts(s.overworld(),kernel);
        });
    }
    private void CompanonMode(ServerLevel level,Item product,Item caramel){CompanionFilters.set(f.station,0,new ItemStack(product));f.source.setItem(0,new ItemStack(caramel));f.port.prepare(ItemStorage.SIDED.find(level,f.base.west(6),Direction.UP));f.check(f.confection.product().is(product),"Filter selects caramel preparation");}
    private void delivery(TestServerContext server,boolean jughead,int mode){
        var product=ConfectionTestSupport.food(mode==0?"cotton_candy":mode==1?"popcorn":"caramel_popcorn");int target=mode==0?8:mode==2?2:3;
        server.runOnServer(s->{f.setup(s.overworld(),mode==0?"cotton_candy_cart":"popcorn_wagon",jughead,true);f.target(product,target);
            if(mode==0){f.source.setItem(0,new ItemStack(Items.SUGAR,10));f.source.setItem(1,new ItemStack(Items.PAPER,10));
                var supplies=ItemStorage.SIDED.find(s.overworld(),f.base.west(6),Direction.UP);f.port.prepare(supplies);var plan=f.port.supplyBatch(supplies,8);
                f.check(plan.stream().filter(v->v.is(Items.SUGAR)).mapToInt(ItemStack::getCount).sum()==3 && plan.stream().filter(v->v.is(Items.PAPER)).mapToInt(ItemStack::getCount).sum()==3,"large target plans at most one native three-serving load per ingredient");
                f.check(f.count(f.source,Items.SUGAR)==10 && f.count(f.source,Items.PAPER)==10 && !f.confection.busy(),"large-batch probe moves no real supplies");
            }
            else {f.source.setItem(0,new ItemStack(ConfectionTestSupport.food("corn_kernels"),6));f.source.setItem(1,new ItemStack(ConfectionTestSupport.food("caramel"),3));}
            f.npc.setNoAi(false);
        });
        f.waitDelivery(server,product,target,3000);
        server.runOnServer(s->{
            if(mode==0)f.check(f.count(f.source,Items.SUGAR)==2 && f.count(f.source,Items.PAPER)==2,"cotton stock target leaves excess sugar and paper");
            else f.check(f.count(f.source,ConfectionTestSupport.food("corn_kernels"))==(mode==1?3:2) && f.count(f.source,ConfectionTestSupport.food("caramel"))==(mode==1?3:1),"corn stock target preserves excess native ingredients");
            f.check(!f.confection.busy(),"all committed confection inputs finish before idle");
        });
    }
    private void midcycle(TestServerContext server){
        var product=ConfectionTestSupport.food("cotton_candy");
        server.runOnServer(s->{f.setup(s.overworld(),"cotton_candy_cart",true,true);f.target(product,8);f.confection.filter.stock.set(new ItemStack(product),8,2);f.output.setItem(0,new ItemStack(product,6));f.source.setItem(0,new ItemStack(Items.SUGAR,5));f.source.setItem(1,new ItemStack(Items.PAPER,5));
            var supply=ItemStorage.SIDED.find(s.overworld(),f.base.west(6),Direction.UP);f.port.prepare(supply);
            for(int i=0;i<2;i++){var plan=f.port.supplyBatch(supply,8);f.check(plan.stream().filter(v->v.is(Items.SUGAR)).mapToInt(ItemStack::getCount).sum()==2 && plan.stream().filter(v->v.is(Items.PAPER)).mapToInt(ItemStack::getCount).sum()==2,"mid-cycle probe plans only two missing cones");}
            f.check(f.confection.filter.stock.wants(f.npc,f.station,new ItemStack(product)),"rolled-back simulated completion cannot stop replenishing above the restart threshold");
            f.check(f.count(f.source,Items.SUGAR)==5 && f.count(f.source,Items.PAPER)==5 && f.count(f.output,product)==6 && !f.confection.busy(),"repeated mid-cycle plans leave world and inventory intact");f.npc.setNoAi(false);
        });
        f.waitDelivery(server,product,8,2200);
        server.runOnServer(s->f.check(f.count(f.source,Items.SUGAR)==3 && f.count(f.source,Items.PAPER)==3,"mid-cycle replenishment consumes only its two missing recipes"));
    }
}
