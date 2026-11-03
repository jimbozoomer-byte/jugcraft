package local.peepo;

import java.util.Locale;
import local.peepo.client.CompanionClipChecks;
import local.peepo.client.PeepoRenderer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;

/** Focused hoe grip/contact checks, one real harvest per rig, and native-render motion frames. */
public final class PeepoHoeMotionClientTests implements FabricClientGameTest {
    private int checks;
    private BlockPos base,soil;
    private PeepoEntity npc;
    private CompanionJob job;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}

    private void setup(ServerLevel level,boolean jughead){
        if(npc!=null){npc.resetCompanionRoutine();npc.discard();}
        for(var pos:BlockPos.betweenClosed(base.offset(-5,-1,-5),base.offset(5,4,5)))
            level.setBlock(pos,pos.getY()<base.getY()?Blocks.OAK_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        level.getEntitiesOfClass(ItemEntity.class,new AABB(base).inflate(7)).forEach(Entity::discard);
        soil=base.north().below();
        level.setBlockAndUpdate(soil,Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7));
        level.setBlockAndUpdate(soil.above(),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);
        npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()+.5,180,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.setGameMode(GameType.CREATIVE);
        npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.feedback=0;npc.preferences.carryMeals=0;
        npc.belongings.setItem(9,new ItemStack(Items.IRON_HOE));npc.assignments.assignGardenZone(soil,soil,false);
        job=npc.garden.job(soil);check(job!=null && job.workStatus(npc)==CompanionStatus.READY,"mature wheat selects a real garden action");
        var at=job.approachPosition();npc.snapTo(at.x,at.y,at.z,180,0);npc.setOnGround(true);
        check(job.claim(npc),"hoe worker claims its garden cell");owner.setGameMode(GameType.SPECTATOR);
    }

    private void harvest(TestServerContext server){
        long[] last={Long.MIN_VALUE};int[] ticks={0};CompanionStatus[] result={CompanionStatus.WORKING};
        server.waitFor(s->{long now=s.overworld().getGameTime();if(last[0]==now)return false;last[0]=now;
            npc.setWorkAnimation(job.animation(),job.animationTarget());result[0]=job.work(npc);ticks[0]++;
            return result[0]!=CompanionStatus.WORKING;
        },100);
        server.runOnServer(s->{
            check(result[0]==CompanionStatus.IDLE && ticks[0]==CompanionGarden.HOE_TICKS,"hoe revision preserves the forty-tick harvest");
            check(npc.belongings.getItem(9).getDamageValue()==1,"one harvest consumes one hoe durability");
            int wheat=0;for(int slot=0;slot<8;slot++)if(npc.belongings.getItem(slot).is(Items.WHEAT))wheat+=npc.belongings.getItem(slot).getCount();
            check(wheat>=1,"completed hoe action collects actual wheat");
            check(s.overworld().getBlockState(soil.above()).is(Blocks.WHEAT)
                && s.overworld().getBlockState(soil.above()).getValue(CropBlock.AGE)<7,"native wheat regrowth remains intact");
            job.release(npc);npc.resetCompanionRoutine();npc.setNoAi(true);
            npc.snapTo(base.getX()+.5,base.getY(),base.getZ()+.5,180,0);npc.setOnGround(true);
            // Bare soil exposes the hoe head and both hands throughout the contact/recovery frames.
            s.overworld().setBlockAndUpdate(soil.above(),Blocks.AIR.defaultBlockState());
        });
    }

    @SuppressWarnings("unchecked") private static <T> void metadata(PeepoEntity entity,String name,T value){
        try{var field=PeepoEntity.class.getDeclaredField(name);field.setAccessible(true);entity.getEntityData().set((EntityDataAccessor<T>)field.get(null),value);}
        catch(ReflectiveOperationException error){throw new AssertionError(error);}
    }

    private void preview(ClientGameTestContext context,TestServerContext server,boolean jughead){
        int id=npc.getId();context.waitTicks(30);
        server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+2.7,base.getY()-.7,base.getZ()-2.6));
        context.runOnClient(c->c.options.fov().set(28));context.waitTicks(3);context.getInput().lookAt(base);context.waitTicks(2);
        context.waitFor(c->c.level.getEntity(id) instanceof PeepoEntity,100);
        server.runOnServer(s->s.tickRateManager().setFrozen(true));
        try{
            for(int frame=0;frame<=40;frame++){
                final int tick=frame;
                context.runOnClient(c->{
                    var shown=(PeepoEntity)c.level.getEntity(id);
                    metadata(shown,"GESTURE",CompanionGesture.NONE.ordinal());metadata(shown,"GESTURE_STARTED",0L);
                    metadata(shown,"WORK_ANIMATION",WorkAnimation.HARVEST.ordinal());metadata(shown,"WORK_TARGET",soil);
                    metadata(shown,"WORK_STARTED",c.level.getGameTime()-tick);
                    shown.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_HOE));
                    shown.setYRot(180);shown.yRotO=180;shown.yBodyRot=shown.yBodyRotO=shown.yHeadRot=shown.yHeadRotO=180;
                    var renderer=(PeepoRenderer)c.getEntityRenderDispatcher().getRenderer(shown);var state=renderer.createRenderState();renderer.extractRenderState(shown,state,0);
                    check(state.work==WorkAnimation.HARVEST && !state.hoe.isEmpty() && state.held.isEmpty(),"native renderer shows one hoe for "+(jughead?"Jughead":"Peepo")+" frame "+tick);
                    check(!state.toolHandling,"tool-draw gesture does not obscure the hoe swing preview");
                });
                context.waitTicks(1);context.takeScreenshot("hoe_motion_"+(jughead?"jughead":"peepo")+String.format(Locale.ROOT,"_%02d",frame));
            }
        }finally{server.runOnServer(s->s.tickRateManager().setFrozen(false));}
    }

    @Override public void runTest(ClientGameTestContext context){
        context.runOnClient(c->CompanionClipChecks.harvesting(this::check));
        try(var world=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
            c.setGenerateStructures(false);
        }).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");
            context.getInput().pressKey(options->options.keyToggleGui);
            try{
                for(boolean jughead:new boolean[]{false,true}){
                    server.runOnServer(s->setup(s.overworld(),jughead));harvest(server);preview(context,server,jughead);
                }
            }finally{server.runOnServer(s->{if(npc!=null){npc.resetCompanionRoutine();npc.discard();}});context.getInput().pressKey(options->options.keyToggleGui);}
        }
        org.slf4j.LoggerFactory.getLogger("peepo-hoe-motion-test").info("[peepo-hoe-motion-test] PASS {} assertions",checks);
    }
}
