package local.peepo;

import java.util.*;
import local.peepo.client.*;
import local.peepo.CompanionFeedbackRules.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import org.slf4j.LoggerFactory;

/** Focused rules, authorized menu/network, committed outcomes and real rendered reaction frames. */
public final class PeepoFeedbackClientTests implements FabricClientGameTest {
    private int assertions;
    private BlockPos base;
    private PeepoEntity peepo,jug;
    private void check(boolean ok,String message){assertions++;if(!ok)throw new AssertionError(message);}
    private void rules(){
        var r=new CompanionFeedbackRules();
        check(r.observe(CompanionStatus.NO_INPUT,"pot",false,0,1)==Kind.NONE,"new problem waits");
        check(r.observe(CompanionStatus.NO_INPUT,"pot",false,59,1)==Kind.NONE,"short problem stays quiet");
        check(r.observe(CompanionStatus.NO_INPUT,"pot",false,60,1)==Kind.SUPPLIES,"persistent input problem reacts once");
        check(r.observe(CompanionStatus.NO_INPUT,"pot",false,1000,1)==Kind.NONE,"unchanged problem never repeats after cooldown");
        r.observe(CompanionStatus.WAITING,"pot",false,1001,1);
        check(r.observe(CompanionStatus.WAITING,"pot",false,1100,1)==Kind.NONE,"budget wait neither complains nor claims recovery");
        r.observe(CompanionStatus.WORKING,"pot",false,1101,1);
        check(r.observe(CompanionStatus.WORKING,"pot",false,1161,1)==Kind.RELIEVED,"actual resumed work resolves the same job");
        check(r.observe(CompanionStatus.WORKING,"pot",false,2000,1)==Kind.NONE,"resolution is one-shot");
        r.observe(CompanionStatus.FULL,"wheel",true,3000,1);
        check(r.observe(CompanionStatus.FULL,"wheel",true,3100,1)==Kind.NONE,"full energy storage is calm");
        for(var status:new CompanionStatus[]{CompanionStatus.GROWING,CompanionStatus.AGING,CompanionStatus.HONEY_GROWING,CompanionStatus.ANIMALS_WAIT,CompanionStatus.DRYING,CompanionStatus.WAITING})
            check(CompanionFeedbackRules.classify(status,false)==Kind.NONE,"normal waiting is calm: "+status);
        r.observe(CompanionStatus.STOCKED,"pot",false,4000,1);
        check(r.observe(CompanionStatus.STOCKED,"pot",false,4060,1)==Kind.STOCKED,"stock target reacts");
        check(r.observe(CompanionStatus.STOCKED,"pot",false,5000,1)==Kind.NONE,"stocked does not loop celebrations");
        r.observe(CompanionStatus.NO_TOOL,"other",false,6000,1);
        r.observe(CompanionStatus.WORKING,"pot",false,6020,1);
        check(r.observe(CompanionStatus.WORKING,"pot",false,6100,1)==Kind.NONE,"inactive short problem cannot override productive job");
        check(!r.admit(Kind.DELIVERY,8000,1) && !r.admit(Kind.BONUS,8000,1),"Subtle excludes routine positives");
        check(r.admit(Kind.DELIVERY,8000,2) && !r.admit(Kind.BONUS,8001,2) && r.admit(Kind.BONUS,8600,2),"Expressive coalesces positives for thirty seconds");
        check(!r.admit(Kind.PATH,10000,0) && r.observe(CompanionStatus.BLOCKED,"pot",false,10000,0)==Kind.NONE,"Off suppresses every event");
        check(Kind.from(-1)==Kind.NONE && Kind.from(900)==Kind.NONE,"invalid wire enum is safe");
        check(Reaction.from(-1)==Reaction.NONE && Reaction.from(900)==Reaction.NONE,"invalid reaction metadata is safe");
        for(var kind:new Kind[]{Kind.HARVEST,Kind.CATCH,Kind.COOK}){
            var finishes=new CompanionFeedbackRules();
            check(!finishes.ready(kind,0,0) && !finishes.ready(kind,0,1),"finishing touches require Expressive");
            check(finishes.ready(kind,0,2) && finishes.ready(kind,0,2) && finishes.admit(kind,0,2),"queuing does not spend positive cooldown");
            check(!finishes.admit(Kind.DELIVERY,1,2) && !finishes.admit(Kind.BONUS,1,2),"finishing shares the existing positive cooldown");
        }
        var installed=new ItemStack(Items.WHEAT,2);
        check(CompanionFeedbackItems.missing(List.of(new RecipeSupplies.Part(Ingredient.of(Items.WHEAT),3)),List.of(installed))==Items.WHEAT && installed.getCount()==2,"known deficit icon never consumes station inventory");
        check(CompanionFeedbackItems.missing(List.of(new RecipeSupplies.Part(Ingredient.of(Items.OAK_PLANKS,Items.BIRCH_PLANKS),3)),List.of())==Items.AIR,"alternatives use generic supplies instead of claiming one required item");
    }
    private PeepoEntity spawn(ServerLevel level,boolean pink,double x){
        var npc=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);
        npc.setNoAi(true);npc.snapTo(base.getX()+x,base.getY(),base.getZ()+.5,180,0);npc.setOnGround(true);
        level.addFreshEntity(npc);npc.orders.tame(level.getServer().getPlayerList().getPlayers().getFirst());npc.preferences.social=false;return npc;
    }
    private void setup(ServerLevel level){
        for(var p:BlockPos.betweenClosed(base.offset(-6,-1,-6),base.offset(6,4,6)))level.setBlock(p,p.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        peepo=spawn(level,false,-.15);jug=spawn(level,true,1.15);
    }
    private void serverChecks(ServerLevel level){
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.snapTo(base.getX(),base.getY(),base.getZ()-3,0,0);
        var stranger=new ServerPlayer(level.getServer(),level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FeedbackStranger"),ClientInformation.createDefault());
        stranger.snapTo(owner.getX(),owner.getY(),owner.getZ(),0,0);
        check(peepo.feedback.canView(owner) && !peepo.feedback.canView(stranger),"private bubble authorization rejects stranger at same location");
        owner.snapTo(base.getX()+30,base.getY(),base.getZ(),0,0);check(!peepo.feedback.canView(owner),"distant owner receives no event");
        owner.snapTo(base.getX(),base.getY(),base.getZ()-3,0,0);
        for(var npc:List.of(peepo,jug)){
            check(npc.preferences.feedback==1,"old/default companions start Subtle");
            npc.preferences.feedback=2;npc.preferences.alerts=true;
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());npc.preferences.save(saved);
            npc.preferences.feedback=0;npc.preferences.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(npc.preferences.feedback==2 && npc.preferences.alerts,"Routine preference survives reload");
            var old=saved.buildResult();old.remove("WorkFeedback");npc.preferences.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),old));
            check(npc.preferences.feedback==1,"missing saved preference migrates to Subtle");
            old.putInt("WorkFeedback",900);npc.preferences.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),old));check(npc.preferences.feedback==2,"invalid saved mode clamps safely");
            npc.preferences.alerts=false;check(npc.feedback.safePose(),"idle companion can react");
            npc.setWorkAnimation(WorkAnimation.INTERACT,base);check(!npc.feedback.safePose(),"work preempts reactions");npc.setWorkAnimation(WorkAnimation.NONE,base);
            npc.showGesture(CompanionGesture.CARRY,new ItemStack(Items.APPLE),base);check(!npc.feedback.safePose(),"carried cargo preempts reactions");npc.showGesture(CompanionGesture.NONE,ItemStack.EMPTY,base);
            metadata(npc,"REST",CompanionEnergy.Rest.SLEEPING.ordinal());check(!npc.feedback.safePose(),"sleep preempts reactions");npc.setRestMode(CompanionEnergy.Rest.NONE);
            npc.belongings.setItem(9,new ItemStack(Items.TORCH));check(!npc.feedback.safePose(),"held torch keeps its arm pose");npc.belongings.setItem(9,ItemStack.EMPTY);
            npc.feedback.rules.observe(CompanionStatus.NO_INPUT,null,false,level.getGameTime()-61,1);npc.feedback.report(CompanionStatus.NO_INPUT);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.PUZZLED,"persistent cached report drives actual reaction");npc.feedback.report(CompanionStatus.WORKING);
            check(npc.feedbackReaction()==Reaction.NONE,"resumed work clears stale complaint without forgetting its resolution state");
            try(var outer=Transaction.openOuter()){try(var inner=outer.openNested()){npc.feedback.bonusProduced(inner);inner.commit();}}
            npc.feedback.tick();check(npc.feedbackReaction()==Reaction.NONE,"aborted outer transaction cannot celebrate a bonus");
            try(var tx=Transaction.openOuter()){npc.feedback.bonusProduced(tx);tx.commit();}
            npc.feedback.tick();check(npc.feedbackReaction()==Reaction.PLEASED,"committed bonus starts reaction on both rigs");
            npc.preferences.feedback=0;npc.feedback.tick();check(npc.feedbackReaction()==Reaction.NONE,"Off clears an active reaction");
            npc.preferences.feedback=1;
        }
        var menu=new CompanionMenu(98,owner.getInventory(),peepo);
        check(menu.value(80)==1 && !menu.clickMenuButton(stranger,49),"new menu field retains permissions");
        check(menu.clickMenuButton(owner,49) && peepo.preferences.feedback==2,"authorized Routine command cycles Subtle to Expressive");menu.removed(owner);
    }
    private void menu(ClientGameTestContext context,TestServerContext server){
        server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.openMenu(new SimpleMenuProvider((id,inv,who)->new CompanionMenu(id,inv,peepo),peepo.getDisplayName()));});
        context.waitForScreen(CompanionScreen.class);
        context.runOnClient(c->{for(var child:c.gui.screen().children())if(child instanceof Button b && b.getMessage().getString().equals("Routine"))b.onPress(null);});
        context.waitFor(c->c.player.containerMenu instanceof CompanionMenu m && m.value(80)==2,100);
        context.waitTicks(3);context.takeScreenshot("feedback_routine");
        context.runOnClient(c->c.gameMode.handleInventoryButtonClick(c.player.containerMenu.containerId,49));
        server.waitFor(s->peepo.preferences.feedback==0,100);
        context.waitFor(c->((CompanionMenu)c.player.containerMenu).value(80)==0,100);
        check(true,"Routine button round-trip synchronizes Off to client");
        context.runOnClient(c->c.player.closeContainer());context.waitForScreen(null);
    }
    private void finishingChecks(ServerLevel level){
        for(var filling:io.github.jimbozoomer.jugcraft.agriculture.PieFilling.values()){
            var baked=new ItemStack(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item(filling.pie()));
            var raw=new ItemStack(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item(filling.rawPie()));
            check(CompanionFeedback.kitchenProduct(baked) && !CompanionFeedback.kitchenProduct(raw),"whole baked pies/cakes finish kitchen work, raw inputs do not");
        }
        check(!CompanionFeedback.kitchenProduct(new ItemStack(Items.BUCKET)) && !CompanionFeedback.kitchenProduct(new ItemStack(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item("burnt_pie"))),"empty containers and burnt products are not successful kitchen finishes");
        var baker=spawn(level,false,3);baker.preferences.feedback=2;
        var oven=new CompanionAssignments.Target(GlobalPos.of(level.dimension(),base),io.github.jimbozoomer.jugcraft.Jugcraft.id("hearth_oven"),Direction.UP);
        var filling=io.github.jimbozoomer.jugcraft.agriculture.PieFilling.values()[0];
        baker.feedback.delivered(oven,new ItemStack(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item(filling.pie())));baker.feedback.tick();
        check(baker.feedbackReaction()==Reaction.COOK,"delivered placeable oven bake triggers flour brushing");baker.discard();
        for(boolean pink:new boolean[]{false,true})for(var kind:new Kind[]{Kind.HARVEST,Kind.CATCH,Kind.COOK}){
            var npc=spawn(level,pink,3);var item=kind==Kind.HARVEST?Items.CARROT:kind==Kind.CATCH?Items.COD:Items.AIR;
            npc.preferences.feedback=1;npc.feedback.finished(kind,item);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.NONE,"Subtle keeps job finishers quiet");
            npc.preferences.feedback=2;
            try(var outer=Transaction.openOuter()){try(var inner=outer.openNested()){npc.feedback.finished(kind,item,inner);inner.commit();}}
            npc.feedback.tick();check(npc.feedbackReaction()==Reaction.NONE,"aborted production does not inspect a nonexistent item");
            npc.setWorkAnimation(WorkAnimation.INTERACT,base);
            try(var tx=Transaction.openOuter()){npc.feedback.finished(kind,item,tx);tx.commit();}
            npc.feedback.tick();check(npc.feedbackReaction()==Reaction.NONE,"finisher waits for ongoing tool work");
            npc.setWorkAnimation(WorkAnimation.NONE,base);npc.setDeltaMovement(.025,0,0);
            int energy=npc.getEnergy();npc.feedback.tick();
            check(npc.feedbackReaction()==kind.reaction && npc.feedbackItem()==item,"committed product starts the matching finisher with its real item");
            check(npc.getDeltaMovement().x==.025 && npc.getEnergy()==energy && npc.belongings.isEmpty(),"cosmetic item neither stops walking nor creates cargo or spends energy");
            npc.setWorkAnimation(WorkAnimation.CHOP,base);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.NONE && npc.feedbackItem()==Items.AIR,"tool work preempts and clears the display item");
            npc.setWorkAnimation(WorkAnimation.NONE,base);npc.feedback.finished(kind,item);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.NONE,"rapid repeated products coalesce");npc.discard();
        }
        var npc=spawn(level,false,3);npc.preferences.feedback=2;npc.belongings.setItem(9,new ItemStack(Items.FISHING_ROD));
        npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,new ItemStack(Items.FISHING_ROD));
        npc.setWorkAnimation(WorkAnimation.FISH_CAST,base);npc.feedback.finished(Kind.CATCH,Items.SALMON);npc.feedback.tick();
        check(npc.feedbackReaction()==Reaction.NONE,"catch admiration cannot interrupt casting");
        npc.setWorkAnimation(WorkAnimation.FISH_WAIT,base);npc.feedback.tick();
        check(npc.feedbackReaction()==Reaction.CATCH,"catch admiration fits into the next fishing wait");
        npc.setWorkAnimation(WorkAnimation.FISH_BITE,base);npc.feedback.tick();
        check(npc.feedbackReaction()==Reaction.NONE,"a bite immediately restores both fishing hands");npc.discard();
        productionChecks(level);
    }
    private void productionChecks(ServerLevel level){
        var npc=spawn(level,false,3);npc.preferences.feedback=2;
        var soil=base.east(4).below();level.setBlock(soil,Blocks.FARMLAND.defaultBlockState(),2);
        level.setBlock(soil.above(),Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE,7),2);
        npc.assignments.assignGardenZone(soil,soil,false);var garden=npc.garden.job(soil);
        check(garden!=null && garden.workStatus(npc)==CompanionStatus.READY,"real ripe garden job is ready");
        npc.setPos(garden.approachPosition());npc.setOnGround(true);
        try{var field=garden.getClass().getDeclaredField("progress");field.setAccessible(true);field.setInt(garden,CompanionGarden.WORK_TICKS-1);}
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
        check(garden.work(npc)==CompanionStatus.IDLE,"real garden final work tick completes");npc.feedback.tick();
        check(npc.feedbackReaction()==Reaction.HARVEST && npc.feedbackItem()==Items.WHEAT,"actual harvest commit queues its own produce");
        check(level.getBlockState(soil.above()).getValue(net.minecraft.world.level.block.CropBlock.AGE)==0,"harvest feedback preserves replanting");npc.discard();
        level.setBlock(soil.above(),Blocks.AIR.defaultBlockState(),2);level.setBlock(soil,Blocks.OAK_PLANKS.defaultBlockState(),2);
        npc=spawn(level,true,3);npc.preferences.feedback=2;npc.belongings.setItem(9,new ItemStack(Items.FISHING_ROD));
        var seat=base.south(4).below();var water=seat.south(2);level.setBlock(water,Blocks.WATER.defaultBlockState(),2);
        npc.assignments.assignFishing(seat,water);var fishing=npc.fishing.job(seat);
        check(fishing!=null,"real fishing assignment is valid");npc.setPos(fishing.approachPosition());npc.setOnGround(true);
        npc.setWorkAnimation(WorkAnimation.FISH_REEL,water);fishing.phase=3;fishing.ticks=19;
        check(fishing.work(npc)==CompanionStatus.WORKING,"actual landing completes and remains productive");
        npc.setWorkAnimation(WorkAnimation.FISH_WAIT,water);npc.belongings.setChanged();npc.feedback.tick();
        check(npc.gesture()==CompanionGesture.TOOL_DRAW && npc.feedbackReaction()==Reaction.NONE,"first rod draw defers catch admiration");
        // This fixture lands a catch in one server callback; let the real draw's timestamp finish.
        metadata(npc,"GESTURE_STARTED",level.getGameTime()-CompanionGesture.TOOL_DRAW.duration());npc.feedback.tick();
        check(npc.feedbackReaction()==Reaction.CATCH && npc.feedbackItem()!=Items.AIR,"native fishing loot triggers admiration without another catch or inventory change");
        check(fishing.catches==1 && npc.assignedHand().getDamageValue()==1,"catch still pays one rod durability and increments once");
        fishing.release(npc);npc.discard();level.setBlock(water,Blocks.OAK_PLANKS.defaultBlockState(),2);
    }
    private void finishingExpiry(TestServerContext server){
        PeepoEntity npc=server.computeOnServer(s->{var n=spawn(s.overworld(),false,3);n.preferences.feedback=2;
            n.belongings.setItem(9,new ItemStack(Items.TORCH));n.feedback.finished(Kind.HARVEST,Items.WHEAT);return n;});
        long until=server.computeOnServer(s->s.overworld().getGameTime()+65);
        server.waitFor(s->s.overworld().getGameTime()>=until,100);
        server.runOnServer(s->{
            npc.belongings.setItem(9,ItemStack.EMPTY);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.NONE,"occupied hands let pending finishers expire after three seconds");
            npc.feedback.finished(Kind.HARVEST,Items.CARROT);npc.feedback.tick();
            check(npc.feedbackReaction()==Reaction.HARVEST,"skipped finisher does not spend the positive cooldown");
            npc.preferences.feedback=1;npc.feedback.tick();check(npc.feedbackReaction()==Reaction.NONE,"switching to Subtle cancels an active Expressive pose");
            npc.discard();
        });
    }
    private void live(ClientGameTestContext context,TestServerContext server){
        server.runOnServer(s->{peepo.preferences.feedback=2;peepo.setOnGround(true);peepo.setFeedbackReaction(Reaction.RELIEVED,s.overworld().getGameTime());
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(s.getPlayerList().getPlayers().getFirst(),new CompanionFeedbackPayload(peepo.getId(),peepo.getUUID(),Kind.RELIEVED.ordinal(),s.overworld().getGameTime(),0,false));});
        int id=server.computeOnServer(s->peepo.getId());
        context.waitFor(c->c.level.getEntity(id) instanceof PeepoEntity p && p.feedbackReaction()==Reaction.RELIEVED && CompanionThoughts.get(p)!=null,100);
        context.runOnClient(c->{var p=(PeepoEntity)c.level.getEntity(id);var renderer=(PeepoRenderer)c.getEntityRenderDispatcher().getRenderer(p);var state=renderer.createRenderState();renderer.extractRenderState(p,state,0);
            check(state.reaction==Reaction.RELIEVED && state.thought==Kind.RELIEVED,"real metadata and private packet reach rendered client");
            CompanionThoughts.receive(new CompanionFeedbackPayload(id,UUID.randomUUID(),Kind.PATH.ordinal(),c.level.getGameTime(),0,false));check(CompanionThoughts.size()==1,"wrong entity UUID rejected");
        });
        server.runOnServer(s->{peepo.setWorkAnimation(WorkAnimation.INTERACT,base);peepo.feedback.tick();check(peepo.feedbackReaction()==Reaction.NONE,"higher-priority work cancels active reaction immediately");peepo.setWorkAnimation(WorkAnimation.NONE,base);});
        context.waitTicks(44);context.runOnClient(c->check(CompanionThoughts.size()==0,"client bubbles expire without a second packet"));
    }
    private void limits(ClientGameTestContext context,TestServerContext server){
        var ids=new ArrayList<Integer>();
        server.runOnServer(s->{for(int i=0;i<8;i++){var npc=spawn(s.overworld(),false,.5);npc.preferences.feedback=0;ids.add(npc.getId());}});
        context.waitFor(c->ids.stream().allMatch(id->c.level.getEntity(id) instanceof PeepoEntity),100);
        context.runOnClient(c->{
            CompanionThoughts.clear();long now=c.level.getGameTime();
            for(int id:ids){var p=(PeepoEntity)c.level.getEntity(id);CompanionThoughts.receive(new CompanionFeedbackPayload(id,p.getUUID(),Kind.PATH.ordinal(),now,0,false));}
            check(CompanionThoughts.size()==CompanionThoughts.LIMIT,"crowd limits private bubbles to six");
            var first=(PeepoEntity)c.level.getEntity(ids.getFirst());
            CompanionThoughts.receive(new CompanionFeedbackPayload(first.getId(),first.getUUID(),Kind.NONE.ordinal(),now,0,false));
            check(CompanionThoughts.get(first)==null && CompanionThoughts.size()==5,"explicit clear removes active bubble");
            CompanionThoughts.receive(new CompanionFeedbackPayload(first.getId(),first.getUUID(),Kind.TOOL.ordinal(),now-41,0,false));
            check(CompanionThoughts.get(first)==null,"expired event cannot replay");CompanionThoughts.clear();
        });
        server.runOnServer(s->ids.forEach(id->s.overworld().getEntity(id).discard()));
    }
    @SuppressWarnings("unchecked") private static <T> void metadata(PeepoEntity npc,String name,T value){
        try{var field=PeepoEntity.class.getDeclaredField(name);field.setAccessible(true);npc.getEntityData().set((EntityDataAccessor<T>)field.get(null),value);}
        catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private void previews(ClientGameTestContext context,TestServerContext server){
        int a=server.computeOnServer(s->peepo.getId()),b=server.computeOnServer(s->jug.getId());
        server.runCommand("gamemode spectator @a");
        server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+.5,base.getY()-.45,base.getZ()-3.6));
        context.runOnClient(c->c.options.fov().set(32));context.waitTicks(3);context.getInput().lookAt(base);
        context.getInput().pressKey(o->o.keyToggleGui);context.waitTicks(2);
        server.runOnServer(s->{peepo.preferences.feedback=jug.preferences.feedback=0;s.tickRateManager().setFrozen(true);});
        try{
            for(var kind:new Kind[]{Kind.STOCKED,Kind.SUPPLIES,Kind.TIRED,Kind.RELIEVED,Kind.HARVEST,Kind.CATCH,Kind.COOK}){
                if(kind==Kind.CATCH || kind==Kind.COOK)server.runOnServer(s->{
                    for(var at:BlockPos.betweenClosed(base.offset(-6,-1,-6),base.offset(6,-1,-1)))
                        s.overworld().setBlock(at,(kind==Kind.CATCH?Blocks.WATER:Blocks.OAK_PLANKS).defaultBlockState(),2);
                });
                for(int t=0;t<=40;t+=2){
                int frame=t;
                context.runOnClient(c->{CompanionThoughts.clear();for(int id:new int[]{a,b}){
                    var npc=(PeepoEntity)c.level.getEntity(id);metadata(npc,"FEEDBACK_REACTION",kind.reaction.ordinal());metadata(npc,"FEEDBACK_STARTED",c.level.getGameTime()-frame);
                    metadata(npc,"FEEDBACK_ITEM",BuiltInRegistries.ITEM.getId(kind==Kind.HARVEST?Items.CARROT:kind==Kind.CATCH?Items.SALMON:Items.AIR));
                    metadata(npc,"WORK_ANIMATION",(kind==Kind.CATCH?WorkAnimation.FISH_WAIT:WorkAnimation.NONE).ordinal());
                    metadata(npc,"WORK_TARGET",base.north(5).below());
                    npc.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,kind==Kind.CATCH?new ItemStack(Items.FISHING_ROD):ItemStack.EMPTY);
                    npc.setPos(npc.getX(),base.getY(),base.getZ()+(kind==Kind.CATCH?.02:.5));npc.zo=npc.zOld=npc.getZ();
                    npc.setYRot(180);npc.yRotO=180;npc.yBodyRot=npc.yBodyRotO=npc.yHeadRot=npc.yHeadRotO=180;
                    var icon=kind==Kind.SUPPLIES?Items.WHEAT:kind==Kind.HARVEST?Items.CARROT:kind==Kind.CATCH?Items.SALMON:Items.AIR;
                    CompanionThoughts.receive(new CompanionFeedbackPayload(id,npc.getUUID(),kind.ordinal(),c.level.getGameTime()-frame,BuiltInRegistries.ITEM.getId(icon),false));
                }});
                context.waitTicks(1);context.takeScreenshot("feedback_"+kind.name().toLowerCase(Locale.ROOT)+String.format(Locale.ROOT,"_%02d",frame));
                }
            }
        }finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));context.getInput().pressKey(o->o.keyToggleGui);}
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();context.runOnClient(c->{c.options.guiScale().set(1);c.resizeGui();});var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("weather clear");
            server.runOnServer(s->{rules();setup(s.overworld());serverChecks(s.overworld());finishingChecks(s.overworld());});context.runOnClient(c->FeedbackChecks.run(this::check));
            menu(context,server);live(context,server);limits(context,server);finishingExpiry(server);previews(context,server);
            server.runOnServer(s->{peepo.discard();jug.discard();});
        }
        LoggerFactory.getLogger("peepo-feedback").info("[peepo-feedback] PASS {} assertions",assertions);
    }
}
