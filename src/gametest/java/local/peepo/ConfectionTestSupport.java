package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared fixture only for the two focused confection suites. */
final class ConfectionTestSupport {
    BlockPos base;
    PeepoEntity npc;
    Container source,output;
    BlockEntity station;
    ConfectionCompanion confection;
    CompanionLogistics.Port port;
    int checks;
    void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static Item food(String id){return JugcraftAgriculture.item(id);}
    int count(Container container,Item item){int n=0;for(int i=0;i<container.getContainerSize();i++)if(container.getItem(i).is(item))n+=container.getItem(i).getCount();return n;}
    long count(Storage<ItemVariant> storage,Item item){long n=0;for(var view:storage)if(view.getResource().isOf(item))n+=view.getAmount();return n;}
    void close(){if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();npc=null;}}
    void setup(ServerLevel level,String id,boolean jughead,boolean running){
        close();for(var p:BlockPos.betweenClosed(base.offset(-8,-1,-6),base.offset(8,4,6)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        var block=(MultiDecorationBlock)JugcraftAgriculture.block(id);var state=block.defaultBlockState().setValue(MultiDecorationBlock.LIT,running);var facing=state.getValue(MultiDecorationBlock.FACING);
        for(int i=0;i<block.cells().length;i++)level.setBlock(block.partPos(base,facing,i),state.setValue(block.partProperty(),i),18);
        station=level.getBlockEntity(base);confection=ConfectionCompanion.of(station);check(confection!=null && confection.complete(),"native multiblock creates a complete companion adapter");
        level.setBlock(base.west(6),Blocks.BARREL.defaultBlockState(),18);source=(Container)level.getBlockEntity(base.west(6));
        level.setBlock(base.east(6),Blocks.BARREL.defaultBlockState(),18);output=(Container)level.getBlockEntity(base.east(6));
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.setGameMode(net.minecraft.world.level.GameType.CREATIVE);npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assignContainer(level,base.west(6),Direction.UP,false);npc.assignments.assignContainer(level,base.east(6),Direction.UP,true);
        var second=block.partPos(base,facing,1);npc.assignments.assign(level,second);
        check(npc.assignments.assignedWork(base) && CompanionAssignments.canonical(level,second).equals(base),"any native part assigns the master once");
        port=CompanionLogistics.resolve(npc,npc.assignments.get(1));check(port!=null,"existing Supply/Output resolves the station");owner.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
    }
    void give(Item item,int count){try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(item),count,tx)==count,"real supplies insert: "+item);tx.commit();}}
    void target(Item product,int target){check(CompanionFilters.set(station,0,new ItemStack(product)),"product Filter accepted");confection.filter.stock.set(new ItemStack(product),target,0);confection.filter.stock.useOutputs(npc,npc.assignments.get(1));port=CompanionLogistics.resolve(npc,npc.assignments.get(1));}
    void menu(Item product){
        var owner=((ServerLevel)npc.level()).getServer().getPlayerList().getPlayers().getFirst();owner.setGameMode(net.minecraft.world.level.GameType.CREATIVE);owner.snapTo(npc.getX(),npc.getY(),npc.getZ()-2,0,0);
        owner.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new CompanionMenu(id,inventory,npc),npc.getDisplayName()));var menu=(CompanionMenu)owner.containerMenu;
        check((menu.value(25)&16)!=0 && menu.clickMenuButton(owner,60) && menu.filterRow()==0,"companion GUI opens confection Filter");
        menu.setCarried(new ItemStack(product));menu.clicked(CompanionMenu.FILTER_START,0,net.minecraft.world.inventory.ContainerInput.PICKUP,owner);
        check(confection.filter.get(0).is(product) && menu.getCarried().getCount()==1,"Filter ghost sets real station recipe without consuming cursor item");
        menu.setCarried(ItemStack.EMPTY);owner.closeContainer();owner.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
    }
    void safePorts(ServerLevel level,Item input){
        var block=(MultiDecorationBlock)station.getBlockState().getBlock();var other=block.partPos(base,station.getBlockState().getValue(MultiDecorationBlock.FACING),1);var saved=level.getBlockState(other);
        level.setBlock(other,Blocks.AIR.defaultBlockState(),18);check(!confection.complete() && port.needed(new ItemStack(input))==0,"incomplete structure refuses supplies");
        try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(input),1,tx)==0,"cached port refuses incomplete structure");}
        level.setBlock(other,saved,18);
        var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(level.getServer()).book();var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(level.getServer());var owner=level.getServer().getPlayerList().getPlayers().getFirst();
        var stranger=book.account(UUID.randomUUID(),"ConfectionStranger");long key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(base);
        try{check(land.assign(book,key,stranger,0).ok(),"foreign claim fixture");check(!confection.allowed(npc),"foreign claim denies station");try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(input),1,tx)==0,"cached input refuses revoked claim");}}
        finally{land.abandon(owner,book,key,true,0);}
        npc.assignments.clear(1);try(var tx=Transaction.openOuter()){check(port.inputs().insert(ItemVariant.of(input),1,tx)==0 && port.outputs().extract(ItemVariant.of(confection.product()),1,tx)==0,"removed assignment invalidates cached input and output");}
    }
    void waitDelivery(TestServerContext server,Item product,int target,int ticks){
        long[] next={0};server.waitFor(s->{long now=s.overworld().getGameTime();if(now>=next[0]){next[0]=now+400;org.slf4j.LoggerFactory.getLogger("peepo-confection-test").info("Station={} jughead={} product={} output={}/{} busy={} native={} transport={} position={}",station.getBlockState().getBlock(),npc.isJughead(),product,count(output,product),target,confection.busy(),port.status(),npc.transport.activity(),npc.position());}return count(output,product)==target;},ticks);
        long[] until={0};server.runOnServer(s->until[0]=s.overworld().getGameTime()+100);server.waitFor(s->s.overworld().getGameTime()>=until[0],150);
        server.runOnServer(s->check(count(output,product)==target,"stock target stays stable after autonomous completion"));
    }
}
