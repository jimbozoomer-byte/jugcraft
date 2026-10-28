package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Focused rest lifecycle, shared furniture reservations, rig checks and native rendered previews. */
public final class PeepoRestClientTests implements FabricClientGameTest {
    private int checks;private BlockPos base;private PeepoEntity a,b;
    private void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    @SuppressWarnings("unchecked") private static <T> void data(PeepoEntity p,Class<?> owner,String name,T value){try{var field=owner.getDeclaredField(name);field.setAccessible(true);p.getEntityData().set((EntityDataAccessor<T>)field.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private static void elapsed(PeepoEntity p,int ticks){data(p,CompanionRestMotion.class,"START",p.level().getGameTime()-ticks);}
    private PeepoEntity spawn(ServerLevel level,boolean pink){var p=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);p.setNoAi(true);p.snapTo(base.getX()+.5,base.getY(),base.getZ()+3.5,0,0);p.setOnGround(true);level.addFreshEntity(p);p.preferences.social=false;p.preferences.feedback=0;return p;}
    private void floor(ServerLevel l){
        if(a!=null){a.restMotion.cancel();a.resetCompanionRoutine();a.discard();}if(b!=null){b.restMotion.cancel();b.resetCompanionRoutine();b.discard();}
        for(var at:BlockPos.betweenClosed(base.offset(-10,-1,-8),base.offset(10,5,8)))l.setBlock(at,at.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        a=spawn(l,false);b=spawn(l,true);
    }
    private void rules(ServerLevel l){
        floor(l);var at=base.offset(-5,0,0);
        for(var color:DyeColor.values()){
            l.setBlockAndUpdate(at,CompanionBeds.BLOCKS.get(color.getId()).defaultBlockState());var bed=(CompanionBedEntity)l.getBlockEntity(at);
            check(bed.claim(a) && bed.occupy(a),"colored bed can be occupied");
            check(a.restMotion.color()==color.getId() && a.restMotion.clip()==CompanionRestMotion.Clip.SLEEP,"blanket inherits every companion bed color");
            long started=a.restMotion.started();bed.occupy(a);check(a.restMotion.started()==started,"occupancy renewal never restarts the clip");
            bed.release(a);check(a.restMotion.clip()==CompanionRestMotion.Clip.NONE,"interrupted entrance cancels safely");
        }
        var head=base.offset(5,0,-1);var foot=head.south();
        var cyan=BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("cyan_bed"));
        l.setBlockAndUpdate(foot,cyan.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        l.setBlockAndUpdate(head,cyan.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        var vanilla=AssignedVanillaBed.create(a,head);check(vanilla!=null && vanilla.claim(a) && vanilla.occupy(a),"vanilla bed adapter accepts rest");
        check(a.restMotion.color()==DyeColor.CYAN.getId() && a.getEnergyRegenPerTick()==16,"vanilla blanket color and sleep recovery");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());a.saveWithoutId(saved);
        var copy=PeepoMod.PEEPO.create(l,EntitySpawnReason.COMMAND);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved.buildResult()));
        check(copy.restMotion.clip()==CompanionRestMotion.Clip.NONE && copy.getRestMode()==CompanionEnergy.Rest.NONE,"reload never resumes an orphaned transition");
        copy.leaveCompanionBed();check(copy.getY()==base.getY(),"reload retains safe bed exit");
        elapsed(a,30);vanilla.release(a);check(a.restMotion.clip()==CompanionRestMotion.Clip.WAKE && a.restMotion.exiting(),"normal release starts wake");
        check(CompanionSeats.isReserved(l,head) && CompanionSeats.isReserved(l,foot),"both vanilla halves remain reserved during wake");
        a.restMotion.cancel();check(!CompanionSeats.isReserved(l,head) && !CompanionSeats.isReserved(l,foot),"cancellation releases transient reservations");
        a.belongings.setItem(0,new ItemStack(Items.WHEAT,17));
        check(vanilla.claim(a) && vanilla.occupy(a),"bed can be reused after cancellation");elapsed(a,30);
        a.hurtServer(l,l.damageSources().generic(),1);vanilla.release(a);
        check(a.restMotion.clip()==CompanionRestMotion.Clip.NONE && a.getRestMode()==CompanionEnergy.Rest.NONE && a.belongings.getItem(0).getCount()==17,"damage cancels rest without dropping or creating cargo");
        var removable=(CompanionBedEntity)l.getBlockEntity(at);check(removable.claim(a) && removable.occupy(a),"removal fixture mounts");elapsed(a,30);l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
        check(a.restMotion.clip()==CompanionRestMotion.Clip.NONE && a.getRestMode()==CompanionEnergy.Rest.NONE,"bed removal cancels blanket and transition");
        for(int diagonal=0;diagonal<2;diagonal++){
            var p=base.offset(-3+diagonal*5,0,4);var q=p.offset(1,0,diagonal);
            l.setBlockAndUpdate(p,Blocks.OAK_FENCE.defaultBlockState());l.setBlockAndUpdate(q,Blocks.OAK_FENCE.defaultBlockState());
            var seat=new CompanionSeats.Surface(l,p,l.getBlockState(p),1,diagonal,1);
            check(seat.claim(b) && seat.occupy(b),"straight/diagonal rail mount");check(b.getEnergyRegenPerTick()==8,"seat recovery is unchanged");
            elapsed(b,20);seat.release(b);check(b.restMotion.clip()==CompanionRestMotion.Clip.STAND && CompanionSeats.isReserved(l,q),"rail exit keeps adjacent reservation");b.restMotion.cancel();
        }
    }
    private void bunks(TestServerContext server){
        server.runOnServer(s->{var l=s.overworld();floor(l);var at=base;
            for(int i=0;i<3;i++)l.setBlockAndUpdate(at.above(i),CompanionBeds.BLOCKS.get(i==2?11:14).defaultBlockState());
            var upper=(CompanionBedEntity)l.getBlockEntity(at.above(2));var lower=(CompanionBedEntity)l.getBlockEntity(at);
            check(upper.claim(a) && upper.occupy(a),"upper bunk occupied from bottom entrance");check(a.restMotion.climbTicks()==28,"bunk height controls climb duration");
            check(!upper.claim(b) && !lower.claim(b),"occupied bunk and active access column reject a second mount");
        });
        server.waitFor(s->{var upper=(CompanionBedEntity)s.overworld().getBlockEntity(base.above(2));upper.occupy(a);return s.overworld().getGameTime()-a.restMotion.started()>=a.restMotion.duration()+1;},100);
        server.runOnServer(s->{var l=s.overworld();var upper=(CompanionBedEntity)l.getBlockEntity(base.above(2));var lower=(CompanionBedEntity)l.getBlockEntity(base);
            check(lower.claim(b) && lower.occupy(b),"access column releases after climbing");check(a.getRestMode()==CompanionEnergy.Rest.SLEEPING && a.getEnergyRegenPerTick()==16,"sleep settled with normal recovery");
            upper.release(a);check(a.restMotion.clip()==CompanionRestMotion.Clip.WAKE && a.getY()==base.getY(),"wake uses authoritative safe bottom exit");check(!upper.claim(b),"bunk remains reserved through descent");
            lower.release(b);b.restMotion.cancel();
        });
        server.waitFor(s->a.restMotion.clip()==CompanionRestMotion.Clip.NONE,100);
        server.runOnServer(s->{var upper=(CompanionBedEntity)s.overworld().getBlockEntity(base.above(2));check(upper.claim(b) && upper.occupy(b),"bunk reservation clears after descent");b.resetCompanionRoutine();upper.release(b);check(b.restMotion.clip()==CompanionRestMotion.Clip.NONE,"command interruption leaves no blanket or climb");
            s.overworld().setBlockAndUpdate(BlockPos.containing(upper.entrance()).above(),Blocks.STONE.defaultBlockState());upper.invalidateSpace();check(!upper.claim(a),"blocked access column prevents bunk climb");});
    }
    private void previews(ClientGameTestContext c,TestServerContext server){
        for(String scene:List.of("seats","blanket","bunks")){
            boolean seat=scene.equals("seats"),bunk=scene.equals("bunks");int rise=bunk?2:0,climb=bunk?28:0;
            server.runOnServer(s->{var l=s.overworld();floor(l);for(int i=0;i<2;i++){
                var pos=base.offset(i*2-1,0,0);
                if(seat)l.setBlockAndUpdate(pos,BuiltInRegistries.BLOCK.getValue(PeepoMod.id("wooden_stool")).defaultBlockState());
                else for(int y=0;y<=rise;y++)l.setBlockAndUpdate(pos.above(y),CompanionBeds.BLOCKS.get(i==0?14:11).defaultBlockState());
                var p=i==0?a:b;p.snapTo(pos.getX()+.5,pos.getY()+rise+(seat?.3435:.375),pos.getZ()+(seat?.7975:.5),0,0);p.setNoGravity(true);
            }l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(16)).forEach(Entity::discard);});
            c.waitTicks(40);int first=a.getId(),second=b.getId();server.runCommand("gamemode spectator @a");
            server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+2.5,base.getY()+(bunk?2.2:seat?.2:1.7),base.getZ()+(bunk?6:seat?4.5:3.6)));
            c.runOnClient(m->m.options.fov().set(40));c.waitTicks(3);c.getInput().lookAt(bunk?base.above():base);c.getInput().pressKey(o->o.keyToggleGui);c.waitTicks(2);server.runOnServer(s->s.tickRateManager().setFrozen(true));
            int enter=seat?14:24+climb,hold=seat?28:32,exit=seat?12:24+climb,total=enter+hold+exit+8;
            try{for(int tick=0;tick<=total;tick+=2){final int time=tick;
                c.runOnClient(m->{for(int i=0;i<2;i++){
                    var p=(PeepoEntity)m.level.getEntity(i==0?first:second);var anchor=base.offset(i*2-1,rise,0);
                    var kind=time<enter+hold?(seat?CompanionRestMotion.Clip.SIT:CompanionRestMotion.Clip.SLEEP):time<enter+hold+exit?(seat?CompanionRestMotion.Clip.STAND:CompanionRestMotion.Clip.WAKE):CompanionRestMotion.Clip.NONE;
                    int age=kind.exit()?time-enter-hold:time;
                    data(p,PeepoEntity.class,"REST",kind==CompanionRestMotion.Clip.NONE?0:seat?1:2);
                    data(p,CompanionRestMotion.class,"ANCHOR",anchor);data(p,CompanionRestMotion.class,"MOUNT",new Vector3f(.5F,seat?.3435F:.375F,seat?.7975F:.5F));
                    data(p,CompanionRestMotion.class,"ENTRY",new Vector3f(.5F,-rise,1.5F));data(p,CompanionRestMotion.class,"YAW",0F);data(p,CompanionRestMotion.class,"COLOR",seat?-1:i==0?14:11);data(p,CompanionRestMotion.class,"CLIMB",climb);
                    data(p,CompanionRestMotion.class,"START",m.level.getGameTime()-age);data(p,CompanionRestMotion.class,"CLIP",kind.ordinal());
                    data(p,PeepoEntity.class,"GESTURE",0);data(p,PeepoEntity.class,"WORK_ANIMATION",0);p.tickCount=200+time;p.walkAnimation.stop();p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                    if(kind==CompanionRestMotion.Clip.NONE)p.snapTo(anchor.getX()+.5,base.getY(),base.getZ()+1.5,0,0);
                    p.setYRot(0);p.yRotO=p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=0;
                    if(time==enter+4){var renderer=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=renderer.createRenderState();renderer.extractRenderState(p,state,0);check(state.rest.active && (seat || state.rest.eyesClosed && state.rest.color==(i==0?14:11)),"native renderer receives settled pose and bed color");}
                }});c.waitTicks(1);c.takeScreenshot("rest_"+scene+String.format(Locale.ROOT,"_%03d",tick));
            }}finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));c.getInput().pressKey(o->o.keyToggleGui);}
        }
    }
    private void blockedExit(TestServerContext server){
        server.runOnServer(s->{var l=s.overworld();floor(l);l.setBlockAndUpdate(base,CompanionBeds.BLOCKS.getFirst().defaultBlockState());var bed=(CompanionBedEntity)l.getBlockEntity(base);
            check(bed.claim(a) && bed.occupy(a),"blocked exit fixture mounts");elapsed(a,30);var entry=BlockPos.containing(bed.entrance());
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){var pos=entry.offset(dx,0,dz);if(!pos.equals(base))l.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());}
            bed.release(a);check(a.restMotion.holdingExit() && !a.restMotion.exiting() && CompanionSeats.isReserved(l,base),"blocked exit holds furniture and movement while retaining safe-exit retry");
            l.setBlockAndUpdate(entry,Blocks.AIR.defaultBlockState());
        });
        server.waitFor(s->a.restMotion.exiting(),40);server.runOnServer(s->check(a.getY()==base.getY(),"cleared exit resumes wake at safe ground position"));
        server.waitFor(s->a.restMotion.clip()==CompanionRestMotion.Clip.NONE,50);server.runOnServer(s->check(!CompanionSeats.isReserved(s.overworld(),base),"delayed wake releases its reservation"));
    }
    @Override public void runTest(ClientGameTestContext c){
        c.runOnClient(m->RestChecks.run(this::check));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(5));server.runCommand("time set midnight");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->rules(s.overworld()));bunks(server);blockedExit(server);
            server.runCommand("time set noon");server.runOnServer(s->check(!a.setRestMode(CompanionEnergy.Rest.SLEEPING),"daytime sleep remains forbidden"));previews(c,server);
            server.runOnServer(s->{a.restMotion.cancel();b.restMotion.cancel();a.discard();b.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-rest-test").info("[peepo-rest-test] PASS {} assertions",checks);
    }
}
