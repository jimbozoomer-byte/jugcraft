package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Staff assignment, safe furniture decks, real occupation/preemption and native-render previews. */
public final class PeepoStaffPostClientTests implements FabricClientGameTest {
    private BlockPos base,post;private PeepoEntity npc,other;private int checks;
    private final List<Throwable> failures=new ArrayList<>();private int groups;
    private void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private void group(String name,Runnable action){
        try{action.run();groups++;org.slf4j.LoggerFactory.getLogger("peepo-staff-test").info("[peepo-staff-test] PASS {}",name);}
        catch(AssertionError | RuntimeException failure){failures.add(failure);org.slf4j.LoggerFactory.getLogger("peepo-staff-test").error("[peepo-staff-test] FAIL "+name,failure);}
    }
    private static Object field(Object target,String name){try{var f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private static void field(Object target,String name,Object value){try{var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private boolean corridor(PeepoEntity p,Vec3 entry,Vec3 mount){try{var method=CompanionStaffPost.class.getDeclaredMethod("corridor",PeepoEntity.class,Vec3.class,Vec3.class);method.setAccessible(true);return (boolean)method.invoke(null,p,entry,mount);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    @SuppressWarnings("unchecked") private static <T> void metadata(PeepoEntity p,String name,T value){try{var f=PeepoEntity.class.getDeclaredField(name);f.setAccessible(true);p.getEntityData().set((EntityDataAccessor<T>)f.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private static Block block(String id){return BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));}
    private static BlockState face(Block block,Direction direction){var state=block.defaultBlockState();return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)?state.setValue(BlockStateProperties.HORIZONTAL_FACING,direction):state;}
    private void pause(TestServerContext server,int ticks){long start=server.computeOnServer(s->s.overworld().getGameTime());server.waitFor(s->s.overworld().getGameTime()>=start+ticks,ticks+40);}
    private void cleanup(){for(var p:new PeepoEntity[]{npc,other})if(p!=null){p.transport.stop();p.resetCompanionRoutine();p.restMotion.cancel();p.discard();}npc=other=null;}
    private PeepoEntity spawn(ServerLevel l,boolean jughead,double x){
        var p=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);p.setNoAi(true);p.snapTo(base.getX()+x,base.getY(),base.getZ()+3.5,180,0);p.setOnGround(true);l.addFreshEntity(p);
        p.orders.tame(l.getServer().getPlayerList().getPlayers().getFirst());p.preferences.social=false;p.preferences.feedback=0;p.preferences.carryMeals=0;return p;
    }
    private void setup(ServerLevel l,boolean jughead){
        cleanup();post=base;for(var at:BlockPos.betweenClosed(base.offset(-8,-1,-7),base.offset(8,4,8)))l.setBlock(at,at.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        l.getEntitiesOfClass(ItemEntity.class,new AABB(base).inflate(10)).forEach(Entity::discard);
        var owner=l.getServer().getPlayerList().getPlayers().getFirst();owner.closeContainer();owner.setGameMode(GameType.CREATIVE);owner.snapTo(base.getX()+3.5,base.getY(),base.getZ()+4.5,180,0);
        npc=spawn(l,jughead,.5);l.setBlockAndUpdate(post,block("peepo_companion:wooden_stool").defaultBlockState());
    }
    private int staffCount(PeepoEntity p){int n=0;for(int i=1;i<=4;i++)if(p.assignments.get(i)!=null && p.assignments.get(i).staffPost())n++;return n;}
    private void assignments(ServerLevel l,boolean jughead){
        setup(l,jughead);var soil=base.west(3).below();l.setBlockAndUpdate(soil,Blocks.FARMLAND.defaultBlockState());
        npc.assignments.assignGardenZone(soil,soil,false);String assigned=npc.assignments.assignStaffPost(post,Direction.SOUTH);
        check(npc.assignments.get(1)!=null && npc.assignments.get(1).garden() && npc.assignments.get(2)!=null && npc.assignments.get(2).staffPost(),"staff post appends below existing productive jobs: "+assigned+"; surface="+CompanionStaffPost.surface(npc,post,Direction.SOUTH)+"; boxes="+l.getBlockState(post).getCollisionShape(l,post).toAabbs());
        check(npc.assignments.get(2).staffPose()==0 && npc.assignments.get(2).face()==Direction.SOUTH,"new furniture post keeps Automatic pose and chosen facing");
        npc.assignments.assignStaffPost(post,Direction.NORTH);npc.assignments.assignStaffPost(base.east().below(),Direction.SOUTH);
        check(staffCount(npc)==1 && npc.assignments.get(3)==null,"duplicate and alternate posts cannot consume a second job slot");
        check(npc.assignments.cycleStaffPose(2) && npc.assignments.get(2).staffPose()==1,"pose cycles to standing");
        check(npc.assignments.cycleStaffPose(2) && npc.assignments.get(2).staffPose()==2,"furniture supports forced sitting");
        check(npc.assignments.rotateStaffPost(2,true),"post facing can rotate");Direction facing=npc.assignments.get(2).face();
        npc.assignments.renameJob(2,"Bakery Attendant");check(npc.assignments.moveWork(2,-1),"staff post participates in ordinary job priority");
        check(npc.assignments.get(1).staffPose()==2 && npc.assignments.get(1).face()==facing && npc.assignments.label(1).equals("Bakery Attendant"),"post pose, facing and name follow a reordered row");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());npc.saveWithoutId(saved);
        var copy=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),saved.buildResult()));
        check(staffCount(copy)==1 && copy.assignments.get(1).staffPose()==2 && copy.assignments.get(1).face()==facing && copy.assignments.label(1).equals("Bakery Attendant"),"post round-trips through native entity save/load");
        check(!copy.staffPost.active() && copy.getRestMode()==CompanionEnergy.Rest.NONE,"loading never resumes a phantom occupied post");
        var corrupted=saved.buildResult().copy();corrupted.put("Assignment3",corrupted.getCompound("Assignment1").orElseThrow().copy());
        copy.load(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),corrupted));check(staffCount(copy)==1,"duplicate saved staff assignments are bounded to one");copy.discard();
        npc.assignments.clear(1);check(staffCount(npc)==0 && npc.assignments.get(1).garden(),"removal compacts remaining ordinary jobs");
        npc.assignments.assignStaffPost(base.east().below(),Direction.SOUTH);check(staffCount(npc)==1,"removal allows a new ordinary-block post");
        int row=npc.assignments.workPriority(base.east().below());for(int i=0;i<4;i++){npc.assignments.cycleStaffPose(row);check(npc.assignments.get(row).staffPose()!=2,"ordinary blocks never offer unsupported sitting");}
    }
    private void surfaces(ServerLevel l,boolean jughead){
        setup(l,jughead);String[] names={"peepo_companion:wooden_stool","jugcraft:hay_bale_seat","jugcraft:skull_footstool","jugcraft:household_white_stool","jugcraft:household_oak_chair"};double[] heights={7/16.0,10/16.0,8/16.0,8/16.0,10/16.0};
        for(int i=0;i<names.length;i++){
            // Household chair FACING points toward its backrest; the occupant faces the open side.
            l.setBlockAndUpdate(post,face(block(names[i]),i==4?Direction.NORTH:Direction.SOUTH));var surface=CompanionStaffPost.surface(npc,post,Direction.SOUTH);
            check(surface!=null && surface.canSit(),"both furniture seating systems provide a staff surface: "+names[i]+"; surface="+surface);
            check(Math.abs(surface.standing().y-post.getY()-heights[i])<.001,"feet use the actual deck, not player sitting offset or chair back: "+names[i]);
            check(surface.seated().y<surface.standing().y && surface.entrance().y==base.getY(),"furniture supplies distinct sit/stand and reachable ground entry");
        }
        l.setBlockAndUpdate(post,face(block("jugcraft:household_oak_chair"),Direction.SOUTH));var backwards=CompanionStaffPost.surface(npc,post,Direction.SOUTH);
        check(backwards!=null && !backwards.canSit(),"facing a chair back keeps standing available without sitting through its backrest");
        for(var state:List.of(Blocks.STONE.defaultBlockState(),Blocks.OAK_SLAB.defaultBlockState(),Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP))){
            l.setBlockAndUpdate(post,state);var surface=CompanionStaffPost.surface(npc,post,Direction.SOUTH);check(surface!=null && !surface.canSit(),"ordinary support is standing-only");
            double expected=state.is(Blocks.OAK_SLAB) && state.getValue(SlabBlock.TYPE)==SlabType.BOTTOM?.5:1;
            check(Math.abs(surface.standing().y-post.getY()-expected)<.001,"ordinary support uses the full-block or slab top");
        }
        for(var unsafe:List.of(Blocks.AIR,Blocks.WATER,Blocks.MAGMA_BLOCK,Blocks.CACTUS,Blocks.CAMPFIRE)){
            l.setBlockAndUpdate(post,unsafe.defaultBlockState());check(CompanionStaffPost.surface(npc,post,Direction.SOUTH)==null,"unsafe or unsupported post rejected: "+unsafe);
        }
        l.setBlockAndUpdate(post,block("peepo_companion:wooden_stool").defaultBlockState());l.setBlockAndUpdate(post.above(),Blocks.STONE.defaultBlockState());
        check(CompanionStaffPost.surface(npc,post,Direction.SOUTH)==null,"blocked headroom prevents mounting for either companion");l.setBlockAndUpdate(post.above(),Blocks.AIR.defaultBlockState());
        npc.assignments.assignStaffPost(post,Direction.SOUTH);var job=npc.staffPost.job(post);other=spawn(l,!jughead,3.5);other.assignments.assignStaffPost(post,Direction.SOUTH);
        check(job!=null && job.claim(npc),"first worker claims post");check(!other.staffPost.job(post).claim(other),"second companion cannot share occupied post");
        var owner=l.getServer().getPlayerList().getPlayers().getFirst();check(!io.github.jimbozoomer.jugcraft.agriculture.Seat.sit(l,post,l.getBlockState(post),owner),"player sitting respects occupied staff post");job.release(npc);
        check(job.availableTo(npc),"released post primes a ready cached availability result");
        check(io.github.jimbozoomer.jugcraft.agriculture.Seat.sit(l,post,l.getBlockState(post),owner),"released staff post becomes available to player");check(!job.claim(npc),"fresh claim cannot use cached readiness to displace a player seated in the same tick");
        owner.stopRiding();io.github.jimbozoomer.jugcraft.agriculture.Seat.at(l,post).forEach(Entity::discard);
        owner.snapTo(base.getX()+3.5,base.getY(),base.getZ()+4.5,180,0);job.release(npc);check(job.claim(npc),"post can be claimed after player stands up");
        l.setBlockAndUpdate(post.above(),Blocks.STONE.defaultBlockState());check(!job.occupy(npc) && !npc.staffPost.active(),"first occupation rechecks a ceiling placed after the claim in the same tick");l.setBlockAndUpdate(post.above(),Blocks.AIR.defaultBlockState());job.release(npc);
        var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(l.getServer());var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(l.getServer()).book();long key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(post);
        try{check(land.assign(book,key,book.account(UUID.randomUUID(),"StaffPostStranger"),0).ok(),"foreign claim fixture installed");check(!job.availableTo(npc) && !job.claim(npc),"live claim permission changes invalidate cached staff jobs");}finally{land.abandon(owner,book,key,true,0);}
    }
    private void sweptApproach(ServerLevel l){
        setup(l,false);l.setBlockAndUpdate(post,Blocks.STONE.defaultBlockState());var entry=Vec3.atBottomCenterOf(post.south());var mount=Vec3.atBottomCenterOf(post.above());
        check(corridor(npc,entry,mount),"clear full-block mounting corridor is usable");
        l.setBlockAndUpdate(post.south(),Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        double radius=npc.getBbWidth()/2+.005;var low=new AABB(entry.x-radius,entry.y+.001,entry.z-radius,entry.x+radius,entry.y+npc.getBbHeight(),entry.z+radius);
        check(l.noCollision(npc,low) && l.noCollision(npc,low.move(0,1.08,0)),"trapdoor fixture leaves the low entry and raised crossing clear");
        check(!corridor(npc,entry,mount),"mounting checks the vertical lift through the trapdoor between two clear endpoints");l.setBlockAndUpdate(post.south(),Blocks.AIR.defaultBlockState());
        var edge=new BlockPos((base.getX()>>4)*16,base.getY(),base.getZ());
        for(var p:BlockPos.betweenClosed(edge.offset(-2,-1,-2),edge.offset(2,3,2)))l.setBlock(p,p.getY()<edge.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        l.setBlockAndUpdate(edge,Blocks.STONE.defaultBlockState());var west=Vec3.atBottomCenterOf(edge.west());var top=Vec3.atBottomCenterOf(edge.above());
        check(corridor(npc,west,top),"cross-chunk mounting path starts unclaimed");
        var owner=l.getServer().getPlayerList().getPlayers().getFirst();var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(l.getServer());var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(l.getServer()).book();long key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(edge.west());
        try{check(land.assign(book,key,book.account(UUID.randomUUID(),"StaffEntryStranger"),0).ok(),"foreign approach-side claim installed");
            check(CompanionJobs.permitted(npc,edge) && !corridor(npc,west,top),"an allowed support block does not authorize crossing its claimed neighbor");
            var alternative=CompanionStaffPost.surface(npc,edge,Direction.WEST);check(alternative!=null && alternative.entrance().distanceTo(west)>.1 && CompanionJobs.permitted(npc,BlockPos.containing(alternative.entrance())),"post selects another safe permitted entry when the preferred side belongs to someone else");
        }finally{land.abandon(owner,book,key,true,0);}
    }
    private void click(Screen screen,String label){screen.children().stream().filter(w->w instanceof Button b && b.visible && b.active && b.getMessage().getString().equals(label)).map(w->(Button)w).findFirst().orElseThrow(()->new AssertionError("Missing active button: "+label)).onPress(null);}
    private void plannerAndMenu(ClientGameTestContext c,TestServerContext server){
        server.runOnServer(s->{setup(s.overworld(),false);var p=s.getPlayerList().getPlayers().getFirst();var tool=new ItemStack(AssignmentTool.ITEM);p.setItemInHand(InteractionHand.MAIN_HAND,tool);AssignmentTool.select(p,tool,npc);});
        c.waitFor(m->AssignmentTool.selected(m.level,m.player.getMainHandItem())!=null,100);c.runOnClient(m->AssignmentTool.openPlanner.accept(InteractionHand.MAIN_HAND));c.waitTicks(3);c.takeScreenshot("staff_post_planner");
        c.runOnClient(m->click(m.gui.screen(),"Staff Post: choose furniture or a block"));server.waitFor(s->AssignmentTool.zoneKind(s.getPlayerList().getPlayers().getFirst().getMainHandItem())==5,100);pause(server,6);
        server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();AssignmentTool.edit(p,s.overworld(),InteractionHand.MAIN_HAND,post,Direction.UP,false);check(staffCount(npc)==1 && !AssignmentTool.zoneMode(p.getMainHandItem()),"one planner click creates staff post and exits selection mode");
            p.openMenu(new SimpleMenuProvider((id,inv,who)->new CompanionMenu(id,inv,npc),npc.getDisplayName()));});
        c.waitFor(m->m.gui.screen() instanceof CompanionScreen && m.player.containerMenu instanceof CompanionMenu menu && menu.companion(m.player)!=null && menu.companion(m.player).assignments.view().get(1)!=null,100);
        server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();var menu=(CompanionMenu)p.containerMenu;check(npc.editingSettings(),"staff options use the authorized companion settings pause");
            check(!menu.editDetails(p,new CompanionEditPayload(menu.containerId+1,1,post,128,"")),"forged menu cannot change pose");check(!menu.editDetails(p,new CompanionEditPayload(menu.containerId,1,post.east(),128,"")),"stale anchor cannot change another row");});
        c.waitTicks(3);c.runOnClient(m->click(m.gui.screen(),"Staff Post options"));c.waitTicks(2);c.runOnClient(m->click(m.gui.screen(),"Pose: Automatic"));
        server.waitFor(s->npc.assignments.get(1).staffPose()==1,100);c.waitFor(m->((CompanionMenu)m.player.containerMenu).companion(m.player).assignments.view().get(1).staffPose()==1,100);pause(server,4);
        Direction prior=server.computeOnServer(s->npc.assignments.get(1).face());c.runOnClient(m->click(m.gui.screen(),"Turn right >"));
        server.waitFor(s->npc.assignments.get(1).face()!=prior,100);c.takeScreenshot("staff_post_jobs_settings");c.runOnClient(m->m.player.closeContainer());
        server.waitFor(s->!npc.editingSettings(),100);pause(server,6);
        server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();AssignmentTool.edit(p,s.overworld(),InteractionHand.MAIN_HAND,post,Direction.UP,true);check(staffCount(npc)==0 && s.overworld().getBlockState(post).getBlock()==block("peepo_companion:wooden_stool"),"planner left-click removes post without breaking its furniture");});
    }
    private void live(TestServerContext server,boolean jughead){
        server.runOnServer(s->{setup(s.overworld(),jughead);npc.assignments.assignStaffPost(post,Direction.SOUTH);npc.setStoredEnergy(60000);npc.belongings.setItem(0,new ItemStack(Items.DIAMOND,5));npc.setNoAi(false);s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);});
        server.waitFor(s->npc.staffPost.active() && npc.staffPost.pose()==1 && s.overworld().getGameTime()-npc.staffPost.started()>npc.staffPost.duration(),600);
        server.runOnServer(s->{var surface=CompanionStaffPost.surface(npc,post,Direction.SOUTH);check(npc.position().distanceTo(surface.standing())<.01,"actual navigation ends with feet centered on stool");check(npc.getEnergyRegenPerTick()==2 && npc.getEnergy()>=60000,"staff standing charges no work energy and retains passive regeneration");
            long delay=(long)field(npc.staffPost,"nextPose")-s.overworld().getGameTime();check(delay>0 && delay<=1800,"initial random pose deadline is bounded");field(npc.staffPost,"nextPose",s.overworld().getGameTime());});
        server.waitFor(s->npc.staffPost.sitting(),50);pause(server,25);
        server.runOnServer(s->{check(npc.staffPost.active() && npc.getRestMode()==CompanionEnergy.Rest.SITTING && npc.getEnergyRegenPerTick()==8,"seated staff remains assigned and receives native sitting recovery");
            long delay=(long)field(npc.staffPost,"nextPose")-s.overworld().getGameTime();check(delay>=550 && delay<=1800,"subsequent automatic pose interval stays within 30-90 seconds");field(npc.staffPost,"nextPose",s.overworld().getGameTime());});
        server.waitFor(s->npc.staffPost.pose()==1,50);pause(server,25);
        server.runOnServer(s->{check(npc.staffPost.active() && npc.getRestMode()==CompanionEnergy.Rest.NONE && npc.getEnergyRegenPerTick()==2,"standing again restores ordinary regeneration");check(npc.belongings.getItem(0).getCount()==5,"pose changes never mutate cargo");
            npc.preferences.social=true;s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.CREATIVE);field(npc.staffPost,"nextGreeting",s.overworld().getGameTime());});
        server.waitFor(s->npc.staffPost.greetingStarted()>=0,80);
        server.runOnServer(s->{check(npc.getNavigation().isDone(),"staff greeting keeps feet at the assigned post");npc.preferences.social=false;});server.waitFor(s->npc.staffPost.greetingStarted()<0,20);
        server.runOnServer(s->npc.orders.command(s.getPlayerList().getPlayers().getFirst(),1));
        server.waitFor(s->npc.staffPost.pose()==0,80);
        server.runOnServer(s->{check(!npc.isNoGravity() && !CompanionSeats.isReserved(s.overworld(),post),"Stay releases mounting and reservation safely");npc.orders.command(s.getPlayerList().getPlayers().getFirst(),3);});
        server.waitFor(s->npc.staffPost.active(),600);
        server.runOnServer(s->{var save=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,s.overworld().registryAccess());npc.saveWithoutId(save);var copy=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(s.overworld(),EntitySpawnReason.COMMAND);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,s.overworld().registryAccess(),save.buildResult()));
            check(staffCount(copy)==1 && !copy.staffPost.active() && !copy.isNoGravity(),"occupied-post save reload clears transient mount and keeps assignment");copy.discard();
            var before=npc.position();npc.unloadCompanionRoutine();check(npc.position().equals(before) && !npc.staffPost.active() && !CompanionSeats.isReserved(s.overworld(),post),"tracking unload releases the lease without moving between entity sections");});
        server.waitFor(s->npc.staffPost.active(),600);server.runOnServer(s->s.overworld().setBlockAndUpdate(post,Blocks.AIR.defaultBlockState()));
        server.waitFor(s->npc.staffPost.pose()==0 && !npc.isNoGravity(),80);server.runOnServer(s->check(!CompanionSeats.isReserved(s.overworld(),post),"removing the supporting block releases post"));
    }
    private void ordinary(TestServerContext server,boolean jughead){
        server.runOnServer(s->{setup(s.overworld(),jughead);s.overworld().setBlockAndUpdate(post,jughead?Blocks.OAK_SLAB.defaultBlockState():Blocks.STONE.defaultBlockState());npc.assignments.assignStaffPost(post,Direction.SOUTH);npc.setNoAi(false);s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);});
        server.waitFor(s->npc.staffPost.active() && !npc.staffPost.transitioning(),600);
        server.runOnServer(s->{check(npc.staffPost.pose()==1 && Math.abs(npc.getY()-post.getY()-(jughead?.5:1))<.001,"real worker stands at the exact ordinary block/slab top");field(npc.staffPost,"nextPose",s.overworld().getGameTime());});pause(server,30);
        server.runOnServer(s->check(npc.staffPost.pose()==1 && npc.getRestMode()==CompanionEnergy.Rest.NONE,"ordinary post never cycles to a floating seated pose"));
    }
    private void seatedObstacle(TestServerContext server,boolean jughead){
        server.runOnServer(s->{setup(s.overworld(),jughead);s.overworld().setBlockAndUpdate(post,face(block("jugcraft:household_oak_chair"),Direction.NORTH));npc.assignments.assignStaffPost(post,Direction.SOUTH);npc.assignments.cycleStaffPose(1);npc.assignments.cycleStaffPose(1);npc.setNoAi(false);s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);});
        server.waitFor(s->npc.staffPost.sitting() && !npc.staffPost.transitioning(),600);
        server.runOnServer(s->{s.overworld().setBlockAndUpdate(post.south(),Blocks.STONE.defaultBlockState());var surface=CompanionStaffPost.surface(npc,post,Direction.SOUTH);check(surface!=null && !surface.canSit(),"front obstruction blocks only the chair's seated edge while its standing deck remains clear");});
        server.waitFor(s->npc.staffPost.pose()==0 && npc.getRestMode()==CompanionEnergy.Rest.NONE && !npc.isNoGravity(),100);
        server.runOnServer(s->check(!CompanionSeats.isReserved(s.overworld(),post),"new seated-edge obstruction releases the occupied post and lease"));
    }
    private void priority(TestServerContext server,boolean jughead){
        final BlockPos[] soil={null};server.runOnServer(s->{setup(s.overworld(),jughead);soil[0]=base.west(3).below();s.overworld().setBlockAndUpdate(soil[0],Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7));s.overworld().setBlockAndUpdate(soil[0].above(),Blocks.WHEAT.defaultBlockState());
            npc.assignments.assignGardenZone(soil[0],soil[0],false);npc.assignments.assignStaffPost(post,Direction.SOUTH);npc.belongings.setItem(9,new ItemStack(Items.IRON_HOE));npc.belongings.setItem(0,new ItemStack(Items.WHEAT_SEEDS,8));npc.setNoAi(false);s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);});
        server.waitFor(s->npc.staffPost.active(),600);
        server.runOnServer(s->{s.overworld().setBlockAndUpdate(soil[0].above(),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));npc.garden.inputsChanged(soil[0]);});
        server.waitFor(s->npc.workAnimation()==WorkAnimation.HARVEST,400);server.runOnServer(s->check(!npc.staffPost.active(),"higher-priority ready crop safely preempts staffed furniture"));
        server.waitFor(s->s.overworld().getBlockState(soil[0].above()).is(Blocks.WHEAT) && s.overworld().getBlockState(soil[0].above()).getValue(CropBlock.AGE)<7,100);
        server.waitFor(s->npc.staffPost.active(),600);server.runOnServer(s->check(npc.belongings.getItem(9).getDamageValue()==1,"worker returns to post after one real harvest and replant"));
    }
    private static int count(Container container,Item item){int total=0;for(int i=0;i<container.getContainerSize();i++)if(container.getItem(i).is(item))total+=container.getItem(i).getCount();return total;}
    private void porterDeparture(TestServerContext server,boolean jughead){
        final Container[] containers=new Container[2];
        server.runOnServer(s->{setup(s.overworld(),jughead);var l=s.overworld();
            for(int i=0;i<2;i++){var pos=i==0?base.west(4):base.east(4);l.setBlockAndUpdate(pos,Blocks.BARREL.defaultBlockState());containers[i]=(Container)l.getBlockEntity(pos);npc.assignments.assignContainer(l,pos,Direction.UP,i==1);}
            npc.assignments.supplies.route(0).enabled=true;npc.assignments.assignStaffPost(post,Direction.SOUTH);npc.setNoAi(false);s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);
        });
        server.waitFor(s->npc.staffPost.active() && !npc.staffPost.transitioning(),600);
        server.runOnServer(s->{containers[0].setItem(0,new ItemStack(Items.DIAMOND,40));npc.transport.workChanged();});
        server.waitFor(s->npc.staffPost.exiting(),400);
        var departure=server.computeOnServer(s->npc.position());var visited=new HashSet<Long>();
        Object source=server.computeOnServer(s->field(npc.transport,"workstation")),destination=server.computeOnServer(s->field(npc.transport,"store"));
        server.waitFor(s->{
            if(npc.staffPost.pose()==0)return true;
            if(!npc.staffPost.exiting())return false;
            visited.add(s.overworld().getGameTime());
            check(npc.transport.holdingStaffExit() && (boolean)field(npc.transport,"porter") && (int)field(npc.transport,"route")==0,"a real porter route remains active throughout staff departure");
            check(Objects.equals(source,field(npc.transport,"workstation")) && Objects.equals(destination,field(npc.transport,"store")),"rest transition does not discard or replace the planned porter route");
            check(npc.position().distanceToSqr(departure)<.000001 && npc.getNavigation().isDone(),"porter holds the departure entry until the staff animation finishes");
            check(CompanionSeats.isReserved(s.overworld(),post),"staff support remains reserved during departure");
            check(count(containers[0],Items.DIAMOND)==40 && count(containers[1],Items.DIAMOND)==0 && count(npc.belongings,Items.DIAMOND)==0,"no source pickup or remote transfer occurs during staff departure");
            return false;
        },80);
        server.runOnServer(s->{check(visited.size()>=10,"porter departure was observed across at least ten server ticks");check(npc.staffPost.pose()==0 && !CompanionSeats.isReserved(s.overworld(),post),"completed departure clears its pose and releases its post");});
        server.waitFor(s->npc.transport.carryingDelivery(),600);
        server.runOnServer(s->check(count(containers[0],Items.DIAMOND)==0 && count(npc.belongings,Items.DIAMOND)==40 && count(containers[1],Items.DIAMOND)==0,"departed attendant physically carries the whole supply load"));
        server.waitFor(s->count(containers[1],Items.DIAMOND)==40,600);
        server.waitFor(s->npc.staffPost.active() && !npc.staffPost.transitioning(),600);
        server.runOnServer(s->check(count(containers[0],Items.DIAMOND)==0 && count(npc.belongings,Items.DIAMOND)==0 && count(containers[1],Items.DIAMOND)==40,"attendant returns to its post after completing the conserved porter delivery"));
    }
    private void previews(ClientGameTestContext c,TestServerContext server){
        c.runOnClient(m->{if(m.gui.screen()!=null)m.gui.screen().onClose();});
        server.runOnServer(s->{setup(s.overworld(),false);s.overworld().setBlockAndUpdate(base,Blocks.AIR.defaultBlockState());
            for(int i=0;i<2;i++){var at=base.offset(i*2-1,0,0);s.overworld().setBlockAndUpdate(at,block("peepo_companion:wooden_stool").defaultBlockState());var p=i==0?npc:(other=spawn(s.overworld(),true,2.5));p.setNoAi(true);p.setNoGravity(true);p.snapTo(at.getX()+.5,at.getY()+7/16.0,at.getZ()+.5,0,0);}
            s.getPlayerList().getPlayers().getFirst().setGameMode(GameType.SPECTATOR);
        });
        c.waitTicks(30);int first=npc.getId(),second=other.getId();
        server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+2.5,base.getY()-.2,base.getZ()+4.8));
        c.runOnClient(m->m.options.fov().set(40));c.waitTicks(3);c.getInput().lookAt(base);c.getInput().pressKey(o->o.keyToggleGui);c.waitTicks(2);server.runOnServer(s->s.tickRateManager().setFrozen(true));
        try{for(int tick=0;tick<=160;tick+=2){final int time=tick;
            c.runOnClient(m->{for(int i=0;i<2;i++){
                var p=(PeepoEntity)m.level.getEntity(i==0?first:second);var anchor=base.offset(i*2-1,0,0);
                var stand=new Vec3(anchor.getX()+.5,anchor.getY()+7/16.0,anchor.getZ()+.5);
                var seat=stand.add(0,-.094,.2975);var entry=new Vec3(anchor.getX()+.5,anchor.getY(),anchor.getZ()+1.5);
                int pose,previous,age;Vec3 position,offset;
                if(time<40){pose=1;previous=0;age=time;position=stand;offset=entry.subtract(stand);}
                else if(time<100){pose=2;previous=1;age=time-40;position=seat;offset=stand.subtract(seat);}
                else if(time<140){pose=1;previous=2;age=time-100;position=stand;offset=seat.subtract(stand);}
                else{pose=3;previous=1;age=time-140;position=entry;offset=stand.subtract(entry);}
                metadata(p,"STAFF_POSE",pose);metadata(p,"STAFF_PREVIOUS",previous);metadata(p,"STAFF_STARTED",m.level.getGameTime()-age);metadata(p,"STAFF_OFFSET",offset.toVector3f());
                metadata(p,"STAFF_ANCHOR",anchor);metadata(p,"STAFF_POINT",position.subtract(Vec3.atLowerCornerOf(anchor)).toVector3f());metadata(p,"STAFF_GREETING",time>=60 && time<100?m.level.getGameTime()-(time-60):-1L);
                metadata(p,"REST",pose==2?1:0);metadata(p,"REST_CLIP",0);metadata(p,"GESTURE",0);metadata(p,"WORK_ANIMATION",0);
                p.snapTo(position.x,position.y,position.z,0,0);p.tickCount=200+time;p.walkAnimation.stop();p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                p.setYRot(0);p.yRotO=p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=0;
                var renderer=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=renderer.createRenderState();renderer.extractRenderState(p,state,0);
                check(state.staff.active==(time<160) && !state.rest.active,"native renderer receives staff pose without conflicting rest animation");
                if(time==80)check(state.staff.sit==1,"native staff capture reaches settled seated leg kicks");
            }});c.waitTicks(1);c.takeScreenshot("staff_post_poses"+String.format(Locale.ROOT,"_%03d",tick));
        }}finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));c.getInput().pressKey(o->o.keyToggleGui);}
    }
    @Override public void runTest(ClientGameTestContext c){
        group("continuous native staff poses and fixed limb lengths",()->c.runOnClient(m->StaffPostChecks.run(this::check)));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(4));server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            for(boolean jughead:new boolean[]{false,true}){
                group("one post, priority and persistence: "+(jughead?"Jughead":"Peepo"),()->server.runOnServer(s->assignments(s.overworld(),jughead)));
                group("safe furniture surfaces and exclusive occupancy: "+(jughead?"Jughead":"Peepo"),()->server.runOnServer(s->surfaces(s.overworld(),jughead)));
            }
            group("complete mounting sweep and approach permissions",()->server.runOnServer(s->sweptApproach(s.overworld())));
            group("actual Planner and staff options controls",()->plannerAndMenu(c,server));
            for(boolean jughead:new boolean[]{false,true}){
                group("live poses, Social, cancellation and unload: "+(jughead?"Jughead":"Peepo"),()->live(server,jughead));
                group("actual ordinary block and slab mounting: "+(jughead?"Jughead":"Peepo"),()->ordinary(server,jughead));
                group("seated edge invalidation: "+(jughead?"Jughead":"Peepo"),()->seatedObstacle(server,jughead));
                group("higher-priority work and return: "+(jughead?"Jughead":"Peepo"),()->priority(server,jughead));
                group("staff departure, real porter delivery and return: "+(jughead?"Jughead":"Peepo"),()->porterDeparture(server,jughead));
            }
            group("native rendered staff previews for both companions",()->previews(c,server));
            group("fixture cleanup",()->server.runOnServer(s->cleanup()));
        }
        org.slf4j.LoggerFactory.getLogger("peepo-staff-test").info("[peepo-staff-test] {} groups passed, {} assertions, {} failed groups",groups,checks,failures.size());
        if(!failures.isEmpty()){var error=new AssertionError(failures.size()+" Staff Post groups failed");failures.forEach(error::addSuppressed);throw error;}
    }
}
