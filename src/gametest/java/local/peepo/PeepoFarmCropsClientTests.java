package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Registered gourd families, real autonomous Supply/harvest/Output, and hot woodstove safety. */
public final class PeepoFarmCropsClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private Container source,output;
    private int checks;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private int count(Container c,Item item){int n=0;for(int i=0;i<c.getContainerSize();i++)if(c.getItem(i).is(item))n+=c.getItem(i).getCount();return n;}
    private void reset(ServerLevel level,boolean jughead){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-5),base.offset(8,4,5)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        level.setBlockAndUpdate(base.west(6),Blocks.BARREL.defaultBlockState());source=(Container)level.getBlockEntity(base.west(6));
        level.setBlockAndUpdate(base.east(6),Blocks.BARREL.defaultBlockState());output=(Container)level.getBlockEntity(base.east(6));
        level.setBlock(base.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE,7),2);
        level.setBlock(base.east().below(),Blocks.DIRT.defaultBlockState(),2);
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);
        npc.setNoAi(true);npc.snapTo(base.getX()-2.5,base.getY(),base.getZ()+2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);
        npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assignContainer(level,base.west(6),Direction.UP,false);
        npc.assignments.assignContainer(level,base.east(6),Direction.UP,true);
        npc.assignments.assignGardenZone(base.below(),base.east().below(),false);
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
            c.setGenerateStructures(false);
        }).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamemode spectator @a");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->families(s.overworld()));
            delivery(server,false,"turban_squash");delivery(server,true,"swan_gourd");
            boundaries(server);
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-farm-crops-test").info("[peepo-farm-crops-test] PASS {} assertions",checks);
    }
    private void families(ServerLevel level){
        reset(level,false);int families=0;
        for(var block:BuiltInRegistries.BLOCK)if(block instanceof GourdBlock fruit){
            families++;var seed=new ItemStack(fruit.seed());var start=CompanionGardenCrops.seedState(seed);
            check(start!=null && start.getBlock() instanceof GourdStemBlock,"every gourd has a plantable seed");
            var stem=(GourdStemBlock)start.getBlock();
            check(stem.gourd()==fruit && CompanionGarden.selectionSeed(new ItemStack(fruit))==fruit.seed(),"produce filter maps to its own seeds");
            check(npc.assignments.selectGardenSeed(1,new ItemStack(fruit)),"garden accepts gourd produce selection");
            check(CompanionLogistics.resolve(npc,npc.assignments.get(1)).needed(new ItemStack(Items.WHEAT_SEEDS))==0,"Supply respects selected gourd");
            level.setBlockAndUpdate(base.east(),Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(base,start.setValue(GourdStemBlock.AGE,7));
            check(stem.growGourd(level,base,Direction.EAST),"native stem grows fruit");
            var attached=level.getBlockState(base);
            check(CompanionGardenCrops.stem(attached) && CompanionGardenCrops.seedFor(attached)==fruit.seed(),"attached stems retain planting identity");
            check(CompanionGardenCrops.start(attached).is(stem),"remembered sites reset to growing stems");
            check(CompanionGardenCrops.ripe(level,base.east(),level.getBlockState(base.east())),"matching attached fruit is ripe");
            level.setBlock(base.east(),JugcraftAgriculture.block(fruit==JugcraftAgriculture.block("swan_gourd")?"turban_squash":"swan_gourd").defaultBlockState(),18);
            check(!CompanionGardenCrops.ripe(level,base.east(),level.getBlockState(base.east())),"wrong fruit beside attached stem is protected");
            level.setBlockAndUpdate(base,Blocks.AIR.defaultBlockState());
            check(!CompanionGardenCrops.ripe(level,base.east(),fruit.defaultBlockState()),"standalone decorative gourds are protected");
        }
        check(families>=6,"new squash and gourd families were checked");
        var stove=JugcraftAgriculture.block("woodstove");
        for(boolean jughead:new boolean[]{false,true}){
            reset(level,jughead);var cold=stove.defaultBlockState().setValue(BlockStateProperties.LIT,false);var hot=cold.setValue(BlockStateProperties.LIT,true);
            check(!CompanionHazards.harmful(npc,cold) && CompanionHazards.harmful(npc,hot),"woodstove avoidance follows lit state for both companions");
            level.setBlockAndUpdate(base,hot);check(!CompanionHazards.safeAt(npc,net.minecraft.world.phys.Vec3.atBottomCenterOf(base.above())),"hot top is not a safe work/standing position");
        }
    }
    private void delivery(TestServerContext server,boolean jughead,String id){
        var fruit=(GourdBlock)JugcraftAgriculture.block(id);var start=CompanionGardenCrops.seedState(new ItemStack(fruit.seed()));
        server.runOnServer(s->{reset(s.overworld(),jughead);npc.assignments.selectGardenSeed(1,new ItemStack(fruit));source.setItem(0,new ItemStack(fruit.seed(),5));npc.setNoAi(false);});
        server.waitFor(s->s.overworld().getBlockState(base).is(start.getBlock()),1600);
        server.runOnServer(s->{var level=s.overworld();check(count(source,fruit.seed())==4,"planting consumes one supplied seed");level.setBlock(base,start.setValue(GourdStemBlock.AGE,7),2);check(((GourdStemBlock)start.getBlock()).growGourd(level,base,Direction.EAST),"fruit grows for delivery");npc.garden.inputsChanged(base.below());});
        server.waitFor(s->count(output,fruit.asItem())==1,1800);
        server.runOnServer(s->{check(s.overworld().getBlockState(base).is(start.getBlock()) && s.overworld().getBlockState(base).getValue(GourdStemBlock.AGE)==7,"harvest preserves mature stem for regrowth");check(s.overworld().getBlockState(base.east()).isAir(),"fruit delivered through existing Output");check(count(source,fruit.seed())==4,"harvesting does not consume another seed");});
    }
    private void boundaries(TestServerContext server){
        CompanionGarden.Job[] job={null};long[] last={Long.MIN_VALUE};CompanionStatus[] result={null};
        server.runOnServer(s->{var level=s.overworld();reset(level,false);var stem=(GourdStemBlock)JugcraftAgriculture.block("turban_squash_stem");
            level.setBlock(base,stem.defaultBlockState().setValue(GourdStemBlock.AGE,7),2);stem.growGourd(level,base,Direction.EAST);
            for(int i=0;i<8;i++)npc.belongings.setItem(i,new ItemStack(Items.COBBLESTONE,64));
            job[0]=npc.garden.job(base.below());check(job[0].workStatus(npc)==CompanionStatus.READY,"mature fruit offers garden work");
            var at=job[0].approachPosition();npc.snapTo(at.x,at.y,at.z,0,0);npc.setNoGravity(true);check(job[0].claim(npc),"fruit work claims its cell");
        });
        server.waitFor(s->{long now=s.overworld().getGameTime();if(last[0]==now)return false;last[0]=now;result[0]=job[0].work(npc);return result[0]!=CompanionStatus.WORKING;},140);
        server.runOnServer(s->{check(result[0]==CompanionStatus.FULL,"full cargo stops picking");check(s.overworld().getBlockState(base.east()).is(JugcraftAgriculture.block("turban_squash")),"full cargo preserves fruit");
            reset(s.overworld(),true);var stem=(GourdStemBlock)JugcraftAgriculture.block("swan_gourd_stem");s.overworld().setBlock(base,stem.defaultBlockState().setValue(GourdStemBlock.AGE,7),2);stem.growGourd(s.overworld(),base,Direction.EAST);
            npc.assignments.clear(1);npc.assignments.assignGardenZone(base.east().below(),base.east().below(),false);
            check(npc.garden.job(base.east().below()).workStatus(npc)!=CompanionStatus.READY,"fruit whose stem is outside the zone is protected");
        });
    }
}
