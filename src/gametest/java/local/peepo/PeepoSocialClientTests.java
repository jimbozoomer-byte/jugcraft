package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

/** Paired lifecycle, interruptions, native spawn/sync, rig contact and actual rendered frames. */
public final class PeepoSocialClientTests implements FabricClientGameTest {
    private int checks;private BlockPos base;private PeepoEntity a,b;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private static void field(Object owner,String name,Object value){try{var f=owner.getClass().getDeclaredField(name);f.setAccessible(true);f.set(owner,value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private static Object field(Object owner,String name){try{var f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    @SuppressWarnings("unchecked") private static <T> void data(PeepoEntity p,String name,T value){try{var f=PeepoEntity.class.getDeclaredField(name);f.setAccessible(true);p.getEntityData().set((EntityDataAccessor<T>)f.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private PeepoEntity spawn(ServerLevel l,boolean pink,double x,float yaw){
        var p=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);p.setNoAi(true);
        p.snapTo(base.getX()+x,base.getY(),base.getZ()+.5,yaw,0);p.setOnGround(true);l.addFreshEntity(p);
        p.preferences.feedback=0;p.setStoredEnergy(p.getEnergyCapacity());return p;
    }
    private void pair(ServerLevel l,boolean pinkA,boolean pinkB,double distance){
        if(a!=null)a.discard();if(b!=null)b.discard();
        a=spawn(l,pinkA,.5-distance*.5,-90);b=spawn(l,pinkB,.5+distance*.5,90);
    }
    /** Start at the final chat turn, through the real social tick and goal, without spending six seconds per case. */
    private void ending(ServerLevel l,int pick){
        long now=l.getGameTime();
        for(var p:List.of(a,b)){
            p.social.cancel();p.preferences.social=true;p.setDeltaMovement(Vec3.ZERO);p.getNavigation().stop();p.setOnGround(true);
            field(p.social,"started",now-120);field(p.social,"until",now+52);field(p.social,"nextConversation",now+1800);
        }
        field(a.social,"partner",b);field(b.social,"partner",a);field(a.social,"firstSpeaker",true);field(b.social,"firstSpeaker",false);
        new CompanionSocial.ConversationGoal(a).start();new CompanionSocial.ConversationGoal(b).start();
        long seed=0;while(RandomSource.create(seed).nextInt(3)!=pick)seed++;a.getRandom().setSeed(seed);
        a.social.tick();b.social.tick();
    }
    private void checks(ServerLevel l){
        for(boolean pinkA:new boolean[]{false,true})for(boolean pinkB:new boolean[]{false,true})for(int pick=0;pick<3;pick++){
            pair(l,pinkA,pinkB,.5);String budget=CompanionBudget.statistics(l.getServer());int energy=a.getEnergy();
            ending(l,pick);int expected=pick==0?CompanionSocial.HIGH_FIVE_LEFT:pick==1?CompanionSocial.LAUGH_FIRST:CompanionSocial.FAREWELL_WAVE;
            check(a.socialPose()==expected && b.socialPose()==expected+1,"every variant pairing gets the intended complementary ending");
            check(a.socialStarted()==b.socialStarted(),"paired clips share one authoritative start tick");
            long start=a.socialStarted();for(int i=0;i<8;i++){a.social.tick();b.social.tick();}
            check(a.socialStarted()==start && b.socialStarted()==start,"repeated ticks never restart the clip");
            check(a.getEnergy()==energy && a.getMainHandItem().isEmpty() && b.getMainHandItem().isEmpty(),"social endings neither spend energy nor create items");
            check(budget.equals(CompanionBudget.statistics(l.getServer())) && a.getNavigation().isDone() && b.getNavigation().isDone(),"active social pairs use no extra search or path allowance");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());a.social.save(saved);
            a.social.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved.buildResult()));
            check(a.socialPose()==0 && b.socialPose()==0 && field(a.social,"partner")==null && field(b.social,"partner")==null,"reload cancels both runtime halves");
            check((long)field(a.social,"nextConversation")==l.getGameTime()+1800,"conversation cooldown survives reload");
            ending(l,pick);field(a.social,"until",l.getGameTime());a.social.tick();
            check(a.socialPose()==0 && b.socialPose()==0,"natural deadline releases both companions");
        }
        for(boolean pa:new boolean[]{false,true})for(boolean pb:new boolean[]{false,true}){
            pair(l,false,true,.5+(pa?.05:0)+(pb?.05:0));data(a,"PUMPKIN",pa);data(b,"PUMPKIN",pb);ending(l,0);
            check(a.socialPose()==CompanionSocial.HIGH_FIVE_LEFT && b.socialPose()==CompanionSocial.HIGH_FIVE_RIGHT,"pumpkin and mixed costume pairs retain reachable high-fives");
        }
        pair(l,false,true,1.5);ending(l,0);check(a.socialPose()==CompanionSocial.FAREWELL_WAVE,"unreachable high-five becomes a farewell without approaching");
        pair(l,false,true,.5);b.setPos(b.position().add(0,.1,0));ending(l,0);check(a.socialPose()==CompanionSocial.FAREWELL_WAVE,"uneven ground avoids a missed palm contact");
        for(int side=0;side<2;side++)for(int interruption=0;interruption<10;interruption++){
            pair(l,false,true,.5);var p=side==0?a:b;
            if(interruption>=8)p.orders.tame(l.getServer().getPlayerList().getPlayers().getFirst());
            ending(l,0);
            switch(interruption){
                case 0->p.preferences.command(48);
                case 1->p.setWorkAnimation(WorkAnimation.INTERACT,base);
                case 2->p.showGesture(CompanionGesture.CARRY,new ItemStack(Items.APPLE),base);
                case 3->p.setStoredEnergy(0);
                case 4->p.setRestMode(CompanionEnergy.Rest.SITTING);
                case 5->p.setPos(p.position().add(.06,0,0));
                case 6->p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.TORCH));
                case 7->p.discard();
                case 8->{var owner=l.getServer().getPlayerList().getPlayers().getFirst();var menu=new CompanionMenu(95,owner.getInventory(),p);
                    check(p.editingSettings() && a.socialPose()==0 && b.socialPose()==0,"opening an authorized settings menu itself releases both partners");menu.removed(owner);}
                case 9->p.orders.command(l.getServer().getPlayerList().getPlayers().getFirst(),1);
            }
            a.social.tick();b.social.tick();check(a.socialPose()==0 && b.socialPose()==0,"either participant can immediately interrupt the pair: "+interruption);
        }
        pair(l,false,true,.5);a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.TORCH));ending(l,0);
        check(a.socialPose()==CompanionSocial.FAREWELL_WAVE,"held torch selects safe free-hand farewell");
        ending(l,1);check(a.socialPose()==CompanionSocial.LAUGH_FIRST,"held torch still permits shared laughter");
        a.social.cancel();a.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        int admitted=0;for(int i=0;i<12;i++){var p=spawn(l,false,5+i,0);if(CompanionBudget.social(p))admitted++;p.discard();}
        check(admitted<=1,"server social admission remains capped at one scan per tick");
    }
    private void live(ClientGameTestContext c,TestServerContext server){
        server.runOnServer(s->{pair(s.overworld(),false,true,.5);for(var p:List.of(a,b)){
            // This fixture supplies idle time; the real goal selector still owns conversations and preemption.
            try{var f=Mob.class.getDeclaredField("goalSelector");f.setAccessible(true);var goals=(net.minecraft.world.entity.ai.goal.GoalSelector)f.get(p);
                for(var wrapped:goals.getAvailableGoals())if(wrapped.getGoal().getClass().getSimpleName().equals("BudgetedStroll"))field(wrapped.getGoal(),"nextCheck",s.overworld().getGameTime()+500);
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            p.setNoAi(false);field(p.social,"nextScan",0L);
        }});
        try{server.waitFor(s->a.social.activity()==CompanionStatus.CHATTING && b.social.activity()==CompanionStatus.CHATTING,220);}
        catch(AssertionError failure){server.runOnServer(s->{for(var p:List.of(a,b))LoggerFactory.getLogger("peepo-social").error("social fixture: id={} idle={} ground={} motion={} nav={} activity={} partner={} goal={} pose={} scan={} cooldown={} now={} position={}",p.getId(),p.socialIdle(),p.onGround(),p.getDeltaMovement(),p.getNavigation().isDone(),p.social.activity(),field(p.social,"partner"),field(p.social,"goalActive"),p.socialPose(),field(p.social,"nextScan"),field(p.social,"nextConversation"),s.overworld().getGameTime(),p.position());});throw failure;}
        server.waitFor(s->a.socialPose()>=CompanionSocial.HIGH_FIVE_LEFT && b.socialPose()>=CompanionSocial.HIGH_FIVE_LEFT,180);
        int first=a.getId(),second=b.getId();
        c.waitFor(m->m.level.getEntity(first) instanceof PeepoEntity p && p.socialPose()>=CompanionSocial.HIGH_FIVE_LEFT
            && m.level.getEntity(second) instanceof PeepoEntity q && q.socialPose()>=CompanionSocial.HIGH_FIVE_LEFT && p.socialStarted()==q.socialStarted(),20);
        c.runOnClient(m->{for(int id:new int[]{first,second}){var p=(PeepoEntity)m.level.getEntity(id);var r=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=r.createRenderState();r.extractRenderState(p,state,0);check(state.social==p.socialPose() && state.socialTarget.equals(p.socialTarget()),"native spawned client receives paired metadata and target");}});
        server.waitFor(s->a.socialPose()==0 && b.socialPose()==0,60);
        server.runOnServer(s->{check(field(a.social,"partner")==null && field(b.social,"partner")==null,"native goals finish and release both partners");check((long)field(a.social,"nextConversation")>s.overworld().getGameTime()+1600,"real encounter keeps the long cooldown");a.setNoAi(true);b.setNoAi(true);});
    }
    private void previews(ClientGameTestContext c,TestServerContext server){
        server.runCommand("gamemode spectator @a");
        c.runOnClient(m->m.options.fov().set(32));c.getInput().pressKey(o->o.keyToggleGui);
        for(int pick=0;pick<3;pick++){
            int selected=pick;server.runOnServer(s->{pair(s.overworld(),false,true,selected==0?.5:.9);a.preferences.social=b.preferences.social=false;});c.waitTicks(20);
            int first=a.getId(),second=b.getId(),pose=pick==0?CompanionSocial.HIGH_FIVE_LEFT:pick==1?CompanionSocial.LAUGH_FIRST:CompanionSocial.FAREWELL_WAVE;
            int duration=pick==0?40:pick==1?52:24;String name=pick==0?"high_five":pick==1?"laughter":"farewell";
            for(int view=0;view<2;view++){
            server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+.5+(view==0?1.65:-1.65),base.getY()-.75,base.getZ()-1.45));
            c.waitTicks(4);c.getInput().lookAt(base);c.waitTicks(2);
            server.runOnServer(s->s.tickRateManager().setFrozen(true));
            try{
                if(pick==0 && view==0)c.runOnClient(m->{for(int id:new int[]{first,second})for(var item:new Item[]{Items.TORCH,Items.IRON_HOE}){
                    var p=(PeepoEntity)m.level.getEntity(id);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));
                    var renderer=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=renderer.createRenderState();renderer.extractRenderState(p,state,0);SocialChecks.held(this::check,state);p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                }});
                for(int tick=0;tick<=duration+8;tick+=2){int time=tick;
                c.runOnClient(m->{for(int i=0;i<2;i++){
                    var p=(PeepoEntity)m.level.getEntity(i==0?first:second);data(p,"SOCIAL_POSE",pose+i);data(p,"SOCIAL_STARTED",m.level.getGameTime()-time);
                    data(p,"SOCIAL_TARGET",new org.joml.Vector3f(i==0?3.21F:-3.21F,17,-4));p.tickCount=200+time;p.walkAnimation.stop();
                    float yaw=i==0?-90:90;p.setYRot(yaw);p.yRotO=p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=yaw;
                }});c.waitTicks(1);c.takeScreenshot("social_"+name+"_view"+view+String.format(Locale.ROOT,"_%03d",tick));
            }}finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));}
            }
        }
        c.getInput().pressKey(o->o.keyToggleGui);
    }
    @Override public void runTest(ClientGameTestContext c){
        c.runOnClient(m->SocialChecks.run(this::check));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(5));
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{var l=s.overworld();for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-8),base.offset(8,3,8)))l.setBlock(p,p.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);checks(l);});
            live(c,server);previews(c,server);server.runOnServer(s->{a.discard();b.discard();});
        }
        LoggerFactory.getLogger("peepo-social").info("[peepo-social] PASS {} assertions",checks);
    }
}
