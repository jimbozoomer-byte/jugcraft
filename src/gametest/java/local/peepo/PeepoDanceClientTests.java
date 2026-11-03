package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

/** Actual vanilla playback hooks, bounded shared discovery, goal preemption and native rendered previews. */
public final class PeepoDanceClientTests implements FabricClientGameTest {
    private int checks;private BlockPos base,musicPos;private PeepoEntity a,b;private JukeboxBlockEntity box;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private static void field(Object target,String name,Object value){try{var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    @SuppressWarnings("unchecked") private static <T> void data(PeepoEntity p,String name,T value){try{var f=PeepoEntity.class.getDeclaredField(name);f.setAccessible(true);p.getEntityData().set((EntityDataAccessor<T>)f.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private PeepoEntity spawn(ServerLevel l,boolean pink,double x){
        var p=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);
        // Stable complementary visual styles for comparing both rigs in captures.
        while(Math.floorMod(p.getUUID().hashCode(),6)!=(pink?1:0))p.setUUID(UUID.randomUUID());
        p.setNoAi(true);p.snapTo(base.getX()+x,base.getY(),base.getZ()+.5,180,0);p.setOnGround(true);
        l.addFreshEntity(p);p.preferences.feedback=0;p.setStoredEnergy(p.getEnergyCapacity());return p;
    }
    private void pair(ServerLevel l){if(a!=null)a.discard();if(b!=null)b.discard();a=spawn(l,false,-.25);b=spawn(l,true,1.25);}
    private JukeboxBlockEntity jukebox(ServerLevel l,BlockPos at){l.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());l.setBlockAndUpdate(at,Blocks.JUKEBOX.defaultBlockState());return (JukeboxBlockEntity)l.getBlockEntity(at);}
    private void record(JukeboxBlockEntity at,Item item){at.setTheItem(new ItemStack(item));}
    private void activate(PeepoEntity p,CompanionMusic.Source source){
        p.social.cancel();p.preferences.social=true;p.setDeltaMovement(Vec3.ZERO);p.getNavigation().stop();p.setOnGround(true);
        field(p.social,"music",source);field(p.social,"danceJoined",p.level().getGameTime());field(p.social,"goalActive",true);p.beginDance(source.started);
    }
    private void hooks(ServerLevel l){
        box=jukebox(l,musicPos);var at=Vec3.atCenterOf(musicPos);
        check(CompanionMusic.nearest(l,at)==null,"empty jukebox never registers music");
        record(box,Items.MUSIC_DISC_CAT);var first=CompanionMusic.nearest(l,at);
        check(first!=null && first.live(l) && first.started==l.getGameTime(),"native disc insertion registers actual playback through the mixin");
        box.onSongChanged();check(first==CompanionMusic.nearest(l,at),"repeated notification retains one source and phase");
        check(CompanionMusic.nearest(l,at.add(8.1,0,0))==null && CompanionMusic.nearest(l,at.add(8,0,0))==first,"discovery respects eight-block distance");
        check(CompanionMusic.nearest(l.getServer().getLevel(net.minecraft.world.level.Level.NETHER),at)==null,"music does not leak across dimensions");
        record(box,Items.MUSIC_DISC_13);var replacement=CompanionMusic.nearest(l,at);
        check(replacement!=first && !first.live(l),"changing the native record invalidates the old dance clock");
        box.getSongPlayer().stop(l,box.getBlockState());
        check(!replacement.live(l) && CompanionMusic.nearest(l,at)==null && !box.getTheItem().isEmpty(),"stopped record stays in its slot without remaining a music source");
        var custom=new ItemStack(Items.STONE);custom.set(DataComponents.JUKEBOX_PLAYABLE,new ItemStack(Items.MUSIC_DISC_CAT).get(DataComponents.JUKEBOX_PLAYABLE));box.setTheItem(custom);
        check(CompanionMusic.nearest(l,at)!=null,"compatible custom disc components work without a hard-coded item list");
        var song=JukeboxSong.fromStack(box.getTheItem()).orElseThrow();box.getSongPlayer().setSongWithoutPlaying(song,100);box.onSongChanged();
        var saved=box.saveWithFullMetadata(l.registryAccess());box.setRemoved();check(CompanionMusic.nearest(l,at)==null,"native block-entity removal clears music");
        var loaded=new JukeboxBlockEntity(musicPos,l.getBlockState(musicPos));loaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved));l.setBlockEntity(loaded);
        JukeboxBlockEntity.tick(l,musicPos,loaded.getBlockState(),loaded);box=loaded;
        var restored=CompanionMusic.nearest(l,at);check(restored!=null && restored.started==l.getGameTime()-100,"first native tick restores an in-progress saved disc and its phase");
        CompanionMusic.unload(l,ChunkPos.containing(musicPos));check(!restored.live(l) && CompanionMusic.nearest(l,at)==null,"chunk unload invalidates cached sources without tickets");
        record(box,Items.MUSIC_DISC_13);var broken=CompanionMusic.nearest(l,at);l.setBlockAndUpdate(musicPos,Blocks.AIR.defaultBlockState());check(broken!=null && !broken.live(l),"breaking the jukebox releases its source");
    }
    private void bounded(ServerLevel l){
        var corner=new BlockPos((base.getX()>>4)*16,base.getY()+16,(base.getZ()>>4)*16);var positions=new ArrayList<BlockPos>();
        var song=JukeboxSong.fromStack(new ItemStack(Items.MUSIC_DISC_CAT)).orElseThrow();
        for(int x=0;x<8;x++)for(int z=0;z<8;z++){
            var pos=corner.offset(x,0,z);positions.add(pos);var music=jukebox(l,pos);music.getSongPlayer().setSongWithoutPlaying(song,0);music.onSongChanged();
        }
        var last=corner.offset(15,0,15);positions.add(last);var extra=jukebox(l,last);extra.getSongPlayer().setSongWithoutPlaying(song,0);extra.onSongChanged();var point=Vec3.atCenterOf(last);
        check(CompanionMusic.nearest(l,point)==null,"one lookup stops at 64 candidates even in a dense bucket");
        l.setBlockAndUpdate(positions.getFirst(),Blocks.AIR.defaultBlockState());check(CompanionMusic.nearest(l,point)!=null,"removal frees candidate capacity for a nearby source");
        positions.forEach(pos->l.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState()));
    }
    private void rules(ServerLevel l){
        box=jukebox(l,musicPos);record(box,Items.MUSIC_DISC_CAT);var source=CompanionMusic.nearest(l,Vec3.atCenterOf(musicPos));
        for(boolean pink:new boolean[]{false,true})for(int reason=0;reason<9;reason++){
            var p=spawn(l,pink,.5);var owner=l.getServer().getPlayerList().getPlayers().getFirst();if(reason>=7)p.orders.tame(owner);activate(p,source);
            int energy=p.getEnergy();String budget=CompanionBudget.statistics(l.getServer());p.social.tick();
            check(p.socialPose()==CompanionSocial.DANCE && p.getEnergy()==energy && budget.equals(CompanionBudget.statistics(l.getServer())),"active dancing spends no energy or discovery/path allowance");
            switch(reason){
                case 0->p.preferences.command(48);
                case 1->p.setWorkAnimation(WorkAnimation.INTERACT,base);
                case 2->p.showGesture(CompanionGesture.CARRY,new ItemStack(Items.APPLE),base);
                case 3->p.setStoredEnergy(0);
                case 4->p.setRestMode(CompanionEnergy.Rest.SITTING);
                case 5->p.setDeltaMovement(.1,0,0);
                case 6->p.discard();
                case 7->{var menu=new CompanionMenu(90,owner.getInventory(),p);check(p.editingSettings(),"authorized menu opens during dancing");menu.removed(owner);}
                case 8->p.orders.command(owner,1);
            }
            p.social.tick();check(p.socialPose()==0,"urgent activity interrupts dancing immediately: "+reason);p.discard();
        }
        pair(l);activate(a,source);activate(b,source);
        check(a.danceSongStart()==b.danceSongStart(),"both variants share one jukebox clock");
        var other=jukebox(l,base.offset(1,-1,0));record(other,Items.MUSIC_DISC_13);long clock=a.danceSongStart();a.social.tick();
        check(a.danceSongStart()==clock && a.danceEnd()<0,"a competing jukebox cannot repeatedly switch the selected source");
        a.setPos(a.position().add(12,0,0));a.social.tick();check(a.socialPose()==CompanionSocial.DANCE && a.danceEnd()>=0,"leaving range starts a short natural exit");
        data(a,"DANCE_END",l.getGameTime()-10);a.social.tick();check(a.socialPose()==0,"range exit settles and releases the social goal");
        var save=net.minecraft.world.level.storage.TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());b.saveWithoutId(save);
        var copy=PeepoMod.JUGHEAD.create(l,EntitySpawnReason.COMMAND);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),save.buildResult()));
        check(copy.socialPose()==0 && copy.social.activity()==CompanionStatus.IDLE,"reload never resumes an orphaned dance");
        a.discard();b.discard();l.setBlockAndUpdate(base.offset(1,-1,0),Blocks.OAK_PLANKS.defaultBlockState());
    }
    private void idleGoals(PeepoEntity p){
        try{var f=Mob.class.getDeclaredField("goalSelector");f.setAccessible(true);var goals=(net.minecraft.world.entity.ai.goal.GoalSelector)f.get(p);
            for(var wrapped:goals.getAvailableGoals())if(wrapped.getGoal().getClass().getSimpleName().equals("BudgetedStroll"))field(wrapped.getGoal(),"nextCheck",p.level().getGameTime()+1000);
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
        field(p.social,"nextScan",0L);field(p.social,"ownerNearby",true);p.setNoAi(false);
    }
    private void live(ClientGameTestContext c,TestServerContext server){
        server.runOnServer(s->{pair(s.overworld());box=jukebox(s.overworld(),musicPos);record(box,Items.MUSIC_DISC_CAT);idleGoals(a);idleGoals(b);});
        server.waitFor(s->a.social.activity()==CompanionStatus.DANCING && b.social.activity()==CompanionStatus.DANCING,140);
        int first=a.getId(),second=b.getId();c.waitFor(m->m.level.getEntity(first) instanceof PeepoEntity p && p.socialPose()==CompanionSocial.DANCE
            && m.level.getEntity(second) instanceof PeepoEntity q && q.socialPose()==CompanionSocial.DANCE && p.danceSongStart()==q.danceSongStart(),40);
        c.runOnClient(m->{for(int id:new int[]{first,second}){
            var p=(PeepoEntity)m.level.getEntity(id);var r=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=r.createRenderState();r.extractRenderState(p,state,0);
            check(state.social==CompanionSocial.DANCE && state.dancePhase==Math.floorMod(m.level.getGameTime()-p.danceSongStart(),160),"native client receives the synchronized jukebox dance phase");
        }});
        server.runOnServer(s->{check(a.getEnergy()==a.getEnergyCapacity() && b.getEnergy()==b.getEnergyCapacity(),"live dancing has no energy charge");
            var song=JukeboxSong.fromStack(box.getTheItem()).orElseThrow();box.getSongPlayer().setSongWithoutPlaying(song,song.value().lengthInTicks()-2);box.onSongChanged();});
        server.waitFor(s->a.socialPose()==0 && b.socialPose()==0,30);
        server.runOnServer(s->check(!box.getTheItem().isEmpty() && CompanionMusic.nearest(s.overworld(),Vec3.atCenterOf(musicPos))==null,"song ending stops dancing even while its record remains inserted"));
        server.runOnServer(s->{var owner=s.getPlayerList().getPlayers().getFirst();for(var p:List.of(a,b)){p.orders.tame(owner);p.orders.command(owner,1);idleGoals(p);}record(box,Items.MUSIC_DISC_CAT);});
        server.waitFor(s->a.social.activity()==CompanionStatus.DANCING && b.social.activity()==CompanionStatus.DANCING,140);
        server.runOnServer(s->{check(a.orders.mode()==CompanionOrders.Mode.STAY.ordinal() && a.getNavigation().isDone(),"Stay dances in place under its native command goal");
            a.setNoAi(true);b.setNoAi(true);a.social.cancel();b.social.cancel();});
        server.runOnServer(s->{var owner=s.getPlayerList().getPlayers().getFirst();owner.snapTo(base.getX()+.5,base.getY(),base.getZ()-1,0,0);a.orders.command(owner,0);idleGoals(a);b.preferences.social=false;});
        server.waitFor(s->a.social.activity()==CompanionStatus.DANCING,140);
        server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().snapTo(base.getX()+.5,base.getY(),base.getZ()-6,0,0));
        server.waitFor(s->a.socialPose()==0,20);
        server.runOnServer(s->{check(a.orders.mode()==CompanionOrders.Mode.FOLLOW.ordinal(),"following movement preempts dancing without changing the command");a.setNoAi(true);});
    }
    private void previews(ClientGameTestContext c,TestServerContext server){
        server.runCommand("gamemode spectator @a");
        server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+.5,base.getY()-.5,base.getZ()-4.4));
        c.runOnClient(m->m.options.fov().set(32));c.waitTicks(4);c.getInput().lookAt(base);c.getInput().pressKey(o->o.keyToggleGui);
        for(boolean lights:new boolean[]{false,true}){
            server.runOnServer(s->{pair(s.overworld());record(box,Items.MUSIC_DISC_CAT);a.preferences.social=b.preferences.social=false;
                s.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(24)).forEach(Entity::discard);
                if(lights){a.belongings.setItem(8,new ItemStack(Items.JACK_O_LANTERN));a.belongings.setItem(9,new ItemStack(Items.TORCH));b.belongings.setItem(9,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(PeepoMod.id("tiki_torch"))));}});
            c.waitTicks(25);int first=a.getId(),second=b.getId();server.runOnServer(s->s.tickRateManager().setFrozen(true));
            try{
                c.runOnClient(m->{for(int id:new int[]{first,second})for(var item:new Item[]{Items.TORCH,Items.IRON_HOE}){
                    var p=(PeepoEntity)m.level.getEntity(id);var original=p.getMainHandItem();p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));
                    var renderer=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=renderer.createRenderState();renderer.extractRenderState(p,state,0);DanceChecks.held(this::check,state);p.setItemSlot(EquipmentSlot.MAINHAND,original);
                }});
                for(int tick=0;tick<=180;tick+=2){int time=tick;
                    c.runOnClient(m->{for(int id:new int[]{first,second}){
                        var p=(PeepoEntity)m.level.getEntity(id);data(p,"SOCIAL_POSE",CompanionSocial.DANCE);data(p,"SOCIAL_STARTED",m.level.getGameTime()-time);
                        data(p,"DANCE_SONG_START",m.level.getGameTime()-time);data(p,"DANCE_END",time<160?-1L:m.level.getGameTime()-(time-160));
                        p.tickCount=200+time;p.walkAnimation.stop();p.setYRot(180);p.yRotO=p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=180;
                    }});c.waitTicks(1);c.takeScreenshot("dance_"+(lights?"lights":"default")+String.format(Locale.ROOT,"_%03d",tick));
                }
            }finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));}
        }
        c.getInput().pressKey(o->o.keyToggleGui);
    }
    @Override public void runTest(ClientGameTestContext c){
        c.runOnClient(m->DanceChecks.run(this::check));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(5));musicPos=base.offset(0,-1,-2);
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{var l=s.overworld();for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-8),base.offset(8,3,8)))l.setBlock(p,p.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);hooks(l);bounded(l);rules(l);});
            live(c,server);previews(c,server);server.runOnServer(s->{a.discard();b.discard();});
        }
        LoggerFactory.getLogger("peepo-dance").info("[peepo-dance] PASS {} assertions",checks);
    }
}
