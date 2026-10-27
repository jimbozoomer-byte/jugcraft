package local.peepo;

import java.util.*;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Bounded real tool transitions, garden work and brewing logistics, plus native two-rig previews. */
public final class PeepoTaskDetailClientTests implements FabricClientGameTest {
    private int checks;private BlockPos base;private PeepoEntity peepo,jug;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private PeepoEntity spawn(ServerLevel l,boolean pink,double x){
        var npc=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(l,EntitySpawnReason.COMMAND);npc.setNoAi(true);npc.snapTo(base.getX()+x,base.getY(),base.getZ()+.5,180,0);npc.setOnGround(true);l.addFreshEntity(npc);
        var owner=l.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;return npc;
    }
    private void tools(ServerLevel l){
        for(var npc:List.of(peepo,jug)){
            for(var tool:List.of(Items.IRON_HOE,Items.SHEARS,Items.FISHING_ROD)){
                var work=tool==Items.IRON_HOE?WorkAnimation.HARVEST:tool==Items.SHEARS?WorkAnimation.SHEAR:WorkAnimation.FISH_CAST;
                npc.belongings.setItem(9,new ItemStack(tool));check(npc.getMainHandItem().isEmpty(),"idle work tool stays stowed");
                npc.setWorkAnimation(work,base);npc.belongings.setChanged();
                check(npc.gesture()==CompanionGesture.TOOL_DRAW && npc.getMainHandItem().is(tool),"real tool activation starts draw");
                long started=l.getGameTime()-3;metadata(npc,"GESTURE_STARTED",started);npc.assignedHand().setDamageValue(1);npc.belongings.setChanged();
                check(npc.gestureStarted()==started && npc.getMainHandItem().getDamageValue()==1,"durability sync does not replay equip");
                npc.setWorkAnimation(WorkAnimation.NONE,base);npc.belongings.setChanged();
                check(npc.gesture()==CompanionGesture.TOOL_STOW && npc.getMainHandItem().isEmpty() && npc.assignedHand().getDamageValue()==1,"stow retains inventory ownership and durability");
                npc.setWorkAnimation(work,base);npc.belongings.setChanged();check(npc.gesture()==CompanionGesture.NONE,"adjacent actions cancel stow without repeated drawing");
                npc.setWorkAnimation(WorkAnimation.NONE,base);npc.belongings.setChanged();npc.belongings.setItem(9,ItemStack.EMPTY);
                check(npc.gesture()==CompanionGesture.NONE,"removing a tool cancels its snapshot");
            }
            npc.belongings.setItem(9,new ItemStack(Items.TORCH));check(npc.gesture()==CompanionGesture.NONE && npc.getMainHandItem().is(Items.TORCH),"torch remains a held light");npc.belongings.setItem(9,ItemStack.EMPTY);
            var potion=PotionContents.createItemStack(Items.POTION,Potions.SWIFTNESS);npc.showGesture(CompanionGesture.BREW_ADD,new ItemStack(Items.SUGAR,8),potion,base);
            check(npc.gestureItem().getCount()==1 && npc.gestureAux().get(DataComponents.POTION_CONTENTS).is(Potions.SWIFTNESS),"brewing snapshots retain actual potion components and one reagent");
            npc.showGesture(CompanionGesture.CARRY,new ItemStack(Items.POTION),base);check(npc.gesture()==CompanionGesture.BREW_ADD,"carrying does not overwrite active brew action");
            metadata(npc,"GESTURE_STARTED",l.getGameTime()-32);check(npc.gesture()==CompanionGesture.NONE,"brew action expires at bounded duration");npc.showGesture(CompanionGesture.NONE,ItemStack.EMPTY,base);
        }
    }
    private void garden(TestServerContext server,boolean pink){
        PeepoEntity[] n={null};CompanionJob[] job={null};int[] ticks={0};long[] last={Long.MIN_VALUE};CompanionStatus[] result={CompanionStatus.WORKING};var soil=base.offset(pink?6:4,-1,0);
        server.runOnServer(s->{var l=s.overworld();n[0]=spawn(l,pink,4);l.setBlockAndUpdate(soil,Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7));l.setBlockAndUpdate(soil.above(),Blocks.WHEAT.defaultBlockState());
            var npc=n[0];npc.assignments.assignGardenZone(soil,soil,false);npc.assignments.toggleGardenMaintenance(1,CompanionAssignments.FERTILIZE);npc.belongings.setItem(0,new ItemStack(Items.BONE_MEAL,2));
            job[0]=npc.garden.job(soil);check(job[0]!=null,"assigned garden supplies a job");
        });
        server.waitFor(s->job[0].workStatus(n[0])==CompanionStatus.READY && job[0].claim(n[0]),120);
        server.runOnServer(s->{var at=job[0].approachPosition();n[0].snapTo(at.x,at.y,at.z,0,0);check(job[0].animation()==WorkAnimation.FERTILIZE,"young crop selects fertilizing clip");});
        server.waitFor(s->{if(last[0]==s.overworld().getGameTime())return false;last[0]=s.overworld().getGameTime();var npc=n[0];
            npc.setWorkAnimation(job[0].animation(),job[0].animationTarget());int energy=npc.getEnergy();result[0]=job[0].work(npc);ticks[0]++;
            check(npc.getEnergy()==energy-CompanionGarden.ENERGY_PER_TICK,"fertilizing retains existing energy cost");
            if(ticks[0]==5)check(npc.workPie().is(Items.BONE_MEAL) && npc.workPie().getCount()==1 && npc.belongings.getItem(0).getCount()==2,"cosmetic pouch does not consume fertilizer early");
            return result[0]!=CompanionStatus.WORKING;
        },140);
        server.runOnServer(s->{check(result[0]==CompanionStatus.IDLE && ticks[0]==80,"fertilizing retains eighty-tick work timing");check(n[0].belongings.getItem(0).getCount()==1 && s.overworld().getBlockState(soil.above()).getValue(CropBlock.AGE)>0,"native crop growth consumes one fertilizer");n[0].discard();});
    }
    private void brewing(TestServerContext server){
        var at=base.offset(0,0,4);PeepoEntity[] n={null};Container[] source={null},output={null};var seen=EnumSet.noneOf(CompanionGesture.class);
        server.runOnServer(s->{var l=s.overworld();n[0]=spawn(l,true,3);var npc=n[0];l.setBlockAndUpdate(at,Blocks.BREWING_STAND.defaultBlockState());l.setBlockAndUpdate(at.west(3),Blocks.BARREL.defaultBlockState());l.setBlockAndUpdate(at.east(3),Blocks.BARREL.defaultBlockState());
            source[0]=(Container)l.getBlockEntity(at.west(3));output[0]=(Container)l.getBlockEntity(at.east(3));npc.assignments.assign(l,at);npc.assignments.assignContainer(l,at.west(3),Direction.UP,false);npc.assignments.assignContainer(l,at.east(3),Direction.UP,true);
            var port=(VanillaCompanionPort)CompanionLogistics.resolve(npc,npc.assignments.get(1));port.filter.set(0,PotionContents.createItemStack(Items.POTION,Potions.SWIFTNESS));port.filterChanged();
            source[0].setItem(0,PotionContents.createItemStack(Items.POTION,Potions.WATER));source[0].setItem(1,new ItemStack(Items.NETHER_WART));source[0].setItem(2,new ItemStack(Items.SUGAR));source[0].setItem(3,new ItemStack(Items.BLAZE_POWDER));
            port.prepare(ContainerStorage.of(source[0],Direction.UP));
            try(var tx=Transaction.openOuter()){port.inputs().insert(ItemVariant.of(source[0].getItem(0)),1,tx);}
            check(((BrewingStandBlockEntity)l.getBlockEntity(at)).getItem(0).isEmpty() && npc.gesture()==CompanionGesture.NONE,"rolled-back probe creates neither bottle nor brewing gesture");npc.setNoAi(false);
        });
        server.waitFor(s->{var npc=n[0];var kind=npc.gesture();seen.add(kind);
            if(kind==CompanionGesture.BREW_ADD)check(!npc.gestureItem().is(Items.BLAZE_POWDER) && npc.gestureAux().has(DataComponents.POTION_CONTENTS),"only committed reagent, not fuel, uses bottle animation");
            for(int i=0;i<output[0].getContainerSize();i++)if(output[0].getItem(i).is(Items.POTION))return true;return false;
        },2400);
        server.runOnServer(s->{check(seen.contains(CompanionGesture.BREW_ADD) && seen.contains(CompanionGesture.BREW_SWIRL),"real Supply/Output chain triggers addition and finished-potion swirl");
            check(output[0].getItem(0).get(DataComponents.POTION_CONTENTS).is(Potions.SWIFTNESS),"native two-step brewing still produces selected potion");n[0].discard();
            for(var p:List.of(at,at.west(3),at.east(3)))s.overworld().setBlockAndUpdate(p,Blocks.AIR.defaultBlockState());
        });
    }
    @SuppressWarnings("unchecked") private static <T> void metadata(PeepoEntity npc,String key,T value){try{var f=PeepoEntity.class.getDeclaredField(key);f.setAccessible(true);npc.getEntityData().set((EntityDataAccessor<T>)f.get(null),value);}catch(ReflectiveOperationException e){throw new AssertionError(e);}}
    private void previews(ClientGameTestContext c,TestServerContext server){
        server.runOnServer(s->s.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(20)).forEach(Entity::discard));
        c.waitTicks(40); // Let the dismantled test brewing stand's smoke clear before freezing the showcase.
        int a=peepo.getId(),b=jug.getId();server.runCommand("gamemode spectator @a");server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+.5,base.getY()-.45,base.getZ()-3.6));
        c.runOnClient(m->m.options.fov().set(32));c.waitTicks(3);c.getInput().lookAt(base);c.getInput().pressKey(o->o.keyToggleGui);c.waitTicks(2);
        server.runOnServer(s->{peepo.preferences.feedback=jug.preferences.feedback=0;s.tickRateManager().setFrozen(true);});
        try{for(String clip:List.of("tool_draw","tool_stow","brewing","fertilizing"))for(int frame=0;frame<=20;frame++){
            int tick=frame;
            c.runOnClient(m->{for(int id:new int[]{a,b}){
                var npc=(PeepoEntity)m.level.getEntity(id);var gesture=switch(clip){case "tool_draw"->CompanionGesture.TOOL_DRAW;case "tool_stow"->CompanionGesture.TOOL_STOW;case "brewing"->CompanionGesture.BREW_ADD;default->CompanionGesture.NONE;};
                metadata(npc,"GESTURE",gesture.ordinal());metadata(npc,"GESTURE_STARTED",m.level.getGameTime()-(long)Math.min(gesture==CompanionGesture.TOOL_DRAW?15:gesture.duration(),tick*gesture.duration()/20F));
                metadata(npc,"GESTURE_ITEM",new ItemStack(clip.equals("brewing")?Items.NETHER_WART:Items.IRON_HOE));metadata(npc,"GESTURE_AUX",PotionContents.createItemStack(Items.POTION,Potions.SWIFTNESS));
                metadata(npc,"WORK_ANIMATION",(clip.equals("fertilizing")?WorkAnimation.FERTILIZE:WorkAnimation.NONE).ordinal());metadata(npc,"WORK_STARTED",m.level.getGameTime()-tick*4L);metadata(npc,"WORK_PIE",new ItemStack(Items.BONE_MEAL));
                npc.setItemSlot(EquipmentSlot.MAINHAND,clip.equals("tool_draw")?new ItemStack(Items.IRON_HOE):ItemStack.EMPTY);npc.setYRot(180);npc.yRotO=180;npc.yBodyRot=npc.yBodyRotO=npc.yHeadRot=npc.yHeadRotO=180;
                var renderer=(PeepoRenderer)m.getEntityRenderDispatcher().getRenderer(npc);var state=renderer.createRenderState();renderer.extractRenderState(npc,state,0);
                if(tick==10)check(clip.equals("fertilizing")?state.fertilizing:clip.equals("brewing")?state.detail.brewing():state.toolHandling,"native renderer selects "+clip+" for both variants");
            }});c.waitTicks(1);c.takeScreenshot("task_detail_"+clip+String.format(Locale.ROOT,"_%02d",frame));
        }}finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));c.getInput().pressKey(o->o.keyToggleGui);}
    }
    @Override public void runTest(ClientGameTestContext c){
        c.runOnClient(m->TaskDetailChecks.run(this::check));
        try(var world=c.worldBuilder().adjustSettings(w->{w.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);w.setWorldType(new WorldCreationUiState.WorldTypeEntry(w.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));w.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=c.computeOnClient(m->m.player.blockPosition().above(5));server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{var l=s.overworld();for(var p:BlockPos.betweenClosed(base.offset(-7,-1,-6),base.offset(7,4,7)))l.setBlock(p,p.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);peepo=spawn(l,false,-.15);jug=spawn(l,true,1.15);tools(l);});
            garden(server,false);garden(server,true);brewing(server);previews(c,server);server.runOnServer(s->{peepo.discard();jug.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-task-detail").info("[peepo-task-detail] PASS {} assertions",checks);
    }
}
