package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Native transfer events, mixed bulk visits, partial failures, and controlled rendered previews. */
public final class PeepoHandlingClientTests implements FabricClientGameTest {
    private BlockPos base;private PeepoEntity npc,second;private Container source,output;private int checks;
    private final List<Item> goods=List.of(Items.IRON_INGOT,Items.COPPER_INGOT,Items.GOLD_INGOT,Items.DIAMOND,Items.EMERALD,Items.COAL,Items.REDSTONE,Items.LAPIS_LAZULI);
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private int count(Container c){int n=0;for(int i=0;i<c.getContainerSize();i++)if(goods.contains(c.getItem(i).getItem()))n+=c.getItem(i).getCount();return n;}
    private PeepoEntity spawn(ServerLevel l,boolean pink,double x,double z){
        var p=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);p.setNoAi(true);p.snapTo(base.getX()+x,base.getY(),base.getZ()+z,180,0);p.setOnGround(true);l.addFreshEntity(p);
        var owner=l.getServer().getPlayerList().getPlayers().getFirst();p.orders.tame(owner);p.orders.command(owner,3);p.preferences.social=false;p.preferences.carryMeals=0;p.preferences.feedback=0;return p;
    }
    private void reset(ServerLevel l,boolean pink){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}if(second!=null){second.discard();second=null;}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-7),base.offset(8,4,7)))l.setBlock(p,p.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        l.setBlockAndUpdate(base.west(5),Blocks.CHEST.defaultBlockState());source=(Container)l.getBlockEntity(base.west(5));
        l.setBlockAndUpdate(base.east(5),Blocks.BARREL.defaultBlockState());output=(Container)l.getBlockEntity(base.east(5));
        npc=spawn(l,pink,-3.5,-2.5);npc.assignments.assignContainer(l,base.west(5),Direction.UP,false);npc.assignments.assignContainer(l,base.east(5),Direction.UP,true);npc.assignments.supplies.route(0).enabled=true;
    }
    private void fill(){for(int i=0;i<goods.size();i++)source.setItem(i,new ItemStack(goods.get(i),64));source.getItem(0).set(DataComponents.CUSTOM_NAME,Component.literal("Handled carefully"));}
    private void rules(ServerLevel l){
        reset(l,false);var visual=new CompanionHandling(npc);var at=base.west(5);
        visual.approach(at,true,ItemStack.EMPTY,0);check(npc.gesture()==CompanionGesture.TAKE_READY && npc.gestureItem().isEmpty(),"approach shows empty hands");
        long start=npc.gestureStarted();visual.approach(at,true,ItemStack.EMPTY,0);check(npc.gestureStarted()==start,"nearby approach lease does not restart animation");
        visual.reset();check(npc.gesture()==CompanionGesture.NONE && count(npc.belongings)==0,"failed/cancelled anticipation creates no item or placement");
        visual.transferred(at,false,new ItemStack(Items.IRON_INGOT),2);start=npc.gestureStarted();
        visual.transferred(at,false,new ItemStack(Items.DIAMOND),2);check(npc.gestureStarted()==start && npc.gestureItem().is(Items.IRON_INGOT),"mixed cargo at one endpoint keeps the first item and phase");
        npc.handlingRemainder(new ItemStack(Items.DIAMOND,32));check(npc.gestureAux().is(Items.DIAMOND) && npc.gestureAux().getCount()==1,"remaining cargo is only a cosmetic snapshot");
        npc.showGesture(CompanionGesture.BREW_ADD,new ItemStack(Items.SUGAR),new ItemStack(Items.POTION),at);
        visual.reset();visual.transferred(at,false,new ItemStack(Items.GLASS_BOTTLE),0);check(npc.gesture()==CompanionGesture.BREW_ADD,"brewing keeps ownership over a later generic transfer");
        npc.showGesture(CompanionGesture.NONE,ItemStack.EMPTY,at);
    }
    private void bulk(TestServerContext server,boolean pink){
        var seen=EnumSet.noneOf(CompanionGesture.class);var takes=new HashSet<Long>();var puts=new HashMap<Long,ItemStack>();
        server.runOnServer(s->{reset(s.overworld(),pink);fill();npc.setNoAi(false);});
        server.waitFor(s->{
            var kind=npc.gesture();seen.add(kind);
            if(kind==CompanionGesture.TAKE){takes.add(npc.gestureStarted());check(count(source)==0 && count(npc.belongings)>0,"pickup pose follows real bulk extraction");}
            if(kind==CompanionGesture.PUT){var first=puts.putIfAbsent(npc.gestureStarted(),npc.gestureItem().copy());check(first==null || ItemStack.matches(first,npc.gestureItem()),"bulk placement never cycles through cargo icons");}
            return count(output)==512;
        },1400);
        server.runOnServer(s->{check(takes.size()==1 && puts.size()==1,"eight cargo slots use one pickup and one placement");check(seen.containsAll(List.of(CompanionGesture.TAKE_READY,CompanionGesture.PUT_READY,CompanionGesture.CARRY)),"real route includes approach and carry transitions");check(count(npc.belongings)==0 && source.isEmpty(),"full delivery conserves all 512 items");check(output.getItem(0).getHoverName().getString().equals("Handled carefully"),"real cargo components survive animation");});
    }
    private void partial(TestServerContext server){
        server.runOnServer(s->{reset(s.overworld(),false);fill();npc.setNoAi(false);});
        server.waitFor(s->count(npc.belongings)==512,800);
        server.runOnServer(s->{for(int i=0;i<output.getContainerSize();i++)output.setItem(i,new ItemStack(Items.STONE,64));output.setItem(0,source.getItem(0));
            var matching=npc.belongings.getItem(0).copyWithCount(56);output.setItem(0,matching);});
        server.waitFor(s->npc.transport.porterStatus()==CompanionStatus.FULL,800);
        server.runOnServer(s->check(npc.gesture()==CompanionGesture.PUT && !npc.gestureAux().isEmpty() && count(npc.belongings)==504,"partial commit keeps remaining real cargo in the return pose"));
        server.waitFor(s->npc.gesture()==CompanionGesture.CARRY,80);
        server.runOnServer(s->{check(npc.gestureItem().is(Items.IRON_INGOT) && count(npc.belongings)==504,"blocked output settles into carrying instead of dropping leftovers");output.clearContent();});
        server.waitFor(s->count(output)==504,1400);server.runOnServer(s->check(count(npc.belongings)==0,"retry delivers remaining items exactly once"));
    }
    @SuppressWarnings("unchecked") private static <T> void metadata(PeepoEntity npc,String key,T value){try{var f=PeepoEntity.class.getDeclaredField(key);f.setAccessible(true);npc.getEntityData().set((EntityDataAccessor<T>)f.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private void previews(ClientGameTestContext c,TestServerContext server){
        server.runOnServer(s->{reset(s.overworld(),false);npc.assignments.supplies.route(0).enabled=false;npc.snapTo(base.getX()-.6,base.getY(),base.getZ()+.5,180,0);second=spawn(s.overworld(),true,1.6,.5);
            s.overworld().setBlockAndUpdate(base.west(5),Blocks.AIR.defaultBlockState());s.overworld().setBlockAndUpdate(base.east(5),Blocks.AIR.defaultBlockState());
            s.overworld().setBlockAndUpdate(base.offset(-1,-1,-1),Blocks.BARREL.defaultBlockState());s.overworld().setBlockAndUpdate(base.offset(1,-1,-1),Blocks.BARREL.defaultBlockState());});
        c.waitTicks(6);int a=npc.getId(),b=second.getId();server.runCommand("gamemode spectator @a");
        server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+1.5,base.getY()-.7,base.getZ()-3.5));
        c.runOnClient(m->m.options.fov().set(40));c.waitTicks(3);c.getInput().lookAt(base);c.getInput().pressKey(o->o.keyToggleGui);c.waitTicks(2);
        server.runOnServer(s->s.tickRateManager().setFrozen(true));
        try{for(String clip:List.of("pickup","carry","place","partial"))for(int frame=0;frame<=24;frame++){
            int tick=frame*2;
            c.runOnClient(m->{for(int id:new int[]{a,b}){
                var p=(PeepoEntity)m.level.getEntity(id);CompanionGesture kind;int age;float lead=0;
                if(clip.equals("carry")){kind=CompanionGesture.CARRY;age=tick;}
                else if(tick<12){kind=clip.equals("pickup")?CompanionGesture.TAKE_READY:CompanionGesture.PUT_READY;age=tick;}
                else if(tick<28){kind=clip.equals("pickup")?CompanionGesture.TAKE:CompanionGesture.PUT;age=tick-12;lead=1;}
                else{kind=clip.equals("pickup") || clip.equals("partial")?CompanionGesture.CARRY:CompanionGesture.NONE;age=tick-28;}
                metadata(p,"GESTURE",kind.ordinal());metadata(p,"GESTURE_STARTED",m.level.getGameTime()-age);metadata(p,"GESTURE_LEAD",lead);metadata(p,"GESTURE_LOAD",2);
                metadata(p,"GESTURE_TARGET",base.offset(id==a?-1:1,-1,-1));metadata(p,"GESTURE_ITEM",kind==CompanionGesture.TAKE_READY || kind==CompanionGesture.NONE?ItemStack.EMPTY:new ItemStack(clip.equals("partial") && tick>=28?Items.COPPER_INGOT:Items.IRON_INGOT));
                metadata(p,"GESTURE_AUX",clip.equals("partial") && kind==CompanionGesture.PUT?new ItemStack(Items.COPPER_INGOT):ItemStack.EMPTY);
                metadata(p,"WORK_ANIMATION",WorkAnimation.NONE.ordinal());p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                p.walkAnimation.stop();if(clip.equals("carry")){for(int i=0;i<=tick;i++)p.walkAnimation.update(.4F,1,1);}
                p.setYRot(180);p.yRotO=180;p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=180;
                var r=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(p);var state=r.createRenderState();r.extractRenderState(p,state,0);
                if(tick==16)check(state.actionPose,"native renderer shows handling on both companions");
            }});c.waitTicks(1);c.takeScreenshot("handling_"+clip+String.format(Locale.ROOT,"_%02d",frame));
        }}finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));c.getInput().pressKey(o->o.keyToggleGui);}
    }
    @Override public void runTest(ClientGameTestContext c){
        c.runOnClient(m->HandlingChecks.run(this::check));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(5));server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runCommand(String.format(Locale.ROOT,"tp @a %.1f %d %.1f",base.getX()+.5,base.getY(),base.getZ()-5.5));
            server.runOnServer(s->rules(s.overworld()));bulk(server,false);bulk(server,true);partial(server);previews(c,server);
            server.runOnServer(s->{npc.discard();second.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-handling-test").info("[peepo-handling-test] PASS {} assertions",checks);
    }
}
