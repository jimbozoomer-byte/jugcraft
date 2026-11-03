package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;

/** Real saved farm inventories, rollback and all store families, plus autonomous porter journeys. */
public final class PeepoFarmStorageClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private int checks;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private long count(Storage<ItemVariant> storage,ItemVariant item){long n=0;for(var v:storage)if(v.getResource().equals(item))n+=v.getAmount();return n;}
    private void reset(ServerLevel level,boolean jughead){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-9,-1,-5),base.offset(9,5,6)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);
        npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
    }
    private BlockEntity place(ServerLevel level,BlockPos pos,String id){
        var block=JugcraftAgriculture.block(id);var state=block.defaultBlockState();
        if(block instanceof FruitBinBlock bin){
            var facing=state.getValue(MultiDecorationBlock.FACING);
            for(int i=0;i<bin.cells().length;i++)level.setBlock(bin.partPos(pos,facing,i),state.setValue(bin.partProperty(),i),18);
        }else{
            if(block instanceof PickingBagBlock)level.setBlock(pos.south(),Blocks.STONE.defaultBlockState(),18);
            level.setBlock(pos,state,18);
        }
        return level.getBlockEntity(pos);
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();base=context.computeOnClient(c->c.player.blockPosition().above(4));
            server.runCommand("time set noon");server.runCommand("gamemode spectator @a");server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->stores(s.overworld()));
            for(boolean jughead:new boolean[]{false,true})porter(server,jughead);
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-farm-storage-test").info("[peepo-farm-storage-test] PASS {} assertions",checks);
    }
    private void stores(ServerLevel level){
        for(String id:new String[]{"apple_bin","picking_bag","orchard_handcart","corn_crib","feed_trough","firewood_rack"}){
            reset(level,false);var be=place(level,base,id);check(be!=null,"store creates inventory: "+id);
            var storage=ItemStorage.SIDED.find(level,base,Direction.UP);check(storage!=null,"shared storage exists: "+id);
            var item=id.equals("corn_crib")?JugcraftAgriculture.item("corn"):id.equals("firewood_rack")?Items.OAK_LOG:Items.APPLE;
            var variant=ItemVariant.of(item);int capacity=be instanceof FruitStoreBlockEntity fruit?fruit.capacity():FirewoodRackBlock.CAPACITY;
            var original=level.getBlockState(base);
            try(var tx=Transaction.openOuter()){check(storage.insert(variant,capacity+10,tx)==capacity,"insert honors total capacity");check(storage.insert(ItemVariant.of(Items.DIAMOND),1,tx)==0,"unrelated items rejected");}
            check(count(storage,variant)==0 && level.getBlockState(base)==original,"probe restores real inventory and visible fill");
            try(var tx=Transaction.openOuter()){
                check(storage.insert(variant,capacity,tx)==capacity,"real insertion");
                try(var nested=tx.openNested()){check(storage.extract(variant,3,nested)==3,"nested extraction");}
                check(count(storage,variant)==capacity,"nested abort restores items");tx.commit();
            }
            check(level.getBlockState(base)!=original,"committed insertion updates visible fill");
            var saved=be.saveWithFullMetadata(level.registryAccess());var copy=be.getType().create(base,level.getBlockState(base));copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            check(copy instanceof FruitStoreBlockEntity fruit?fruit.count()==capacity:((FirewoodRackBlockEntity)copy).count()==capacity,"native saved inventory includes transfers");
            var clicked=base;
            if(be.getBlockState().getBlock() instanceof FruitBinBlock bin){
                clicked=bin.partPos(base,be.getBlockState().getValue(MultiDecorationBlock.FACING),bin.cells().length-1);
                check(CompanionAssignments.canonical(level,clicked).equals(base),"all parts share one planner endpoint");
            }
            check(npc.assignments.assignContainer(level,clicked,Direction.UP,false).startsWith("Supply"),"planner assigns native store");
            check(npc.assignments.get(CompanionAssignments.SUPPLY).at().pos().equals(base),"saved endpoint is master");
            check(CompanionStorage.find(npc,npc.assignments.get(CompanionAssignments.SUPPLY))==storage,"Supply uses actual shared storage");
            try(var tx=Transaction.openOuter()){check(storage.extract(variant,capacity+10,tx)==capacity,"exact extraction never overdraws");tx.commit();}
            var named=new ItemStack(item);named.set(DataComponents.CUSTOM_NAME,Component.literal("Kept variant"));var special=ItemVariant.of(named);
            try(var tx=Transaction.openOuter()){storage.insert(variant,2,tx);storage.insert(special,3,tx);tx.commit();}
            check(count(storage,variant)==2 && count(storage,special)==3,"item components stay distinct");
            try(var tx=Transaction.openOuter()){check(storage.extract(variant,99,tx)==2,"ordinary request does not take named items");check(storage.extract(special,99,tx)==3,"named stack preserved");tx.commit();}
            if(be.getBlockState().getBlock() instanceof FruitBinBlock bin){
                level.setBlock(bin.partPos(base,be.getBlockState().getValue(MultiDecorationBlock.FACING),bin.cells().length-1),Blocks.AIR.defaultBlockState(),18);
                try(var tx=Transaction.openOuter()){check(storage.insert(variant,1,tx)==0,"broken structure refuses cached transfer");}
                check(CompanionStorage.find(npc,npc.assignments.get(CompanionAssignments.SUPPLY))==null,"broken structure unavailable to companion");
            }
        }
    }
    private void porter(TestServerContext server,boolean jughead){
        Storage<ItemVariant>[] ends=new Storage[2];var apple=ItemVariant.of(Items.APPLE);
        server.runCommand("gamemode creative @a");
        server.runOnServer(s->{var level=s.overworld();reset(level,jughead);place(level,base.west(5),"apple_bin");place(level,base.east(4),"orchard_handcart");
            check(npc.orders.mode()==CompanionOrders.Mode.WORK.ordinal(),"porter starts in Work mode");
            ends[0]=ItemStorage.SIDED.find(level,base.west(5),Direction.UP);ends[1]=ItemStorage.SIDED.find(level,base.east(4),Direction.UP);
            try(var tx=Transaction.openOuter()){ends[0].insert(apple,24,tx);tx.commit();}
            check(npc.assignments.assignContainer(level,base.west(5),Direction.UP,false).startsWith("Supply"),"porter source assigned");check(npc.assignments.assignContainer(level,base.east(4),Direction.UP,true).startsWith("Output"),"porter destination assigned");
            var route=npc.assignments.supplies.route(0);route.enabled=true;route.filter(0,new ItemStack(Items.APPLE));route.leave=4;route.keep=20;npc.setNoAi(false);
        });
        server.runCommand("gamemode spectator @a");
        server.waitFor(s->count(ends[1],apple)==20,1800);
        server.runOnServer(s->{check(count(ends[0],apple)==4,"porter preserves source reserve for both companions");check(count(ends[1],apple)==20,"porter delivers native farm storage items");});
    }
}
