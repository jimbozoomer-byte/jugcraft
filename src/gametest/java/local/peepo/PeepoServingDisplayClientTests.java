package local.peepo;

import java.util.*;
import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import io.github.jimbozoomer.jugcraft.finance.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.entity.FakePlayer;
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
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.*;

/** Native serving capacities, rollback, saved layouts and real restocking routes for both companions. */
public final class PeepoServingDisplayClientTests implements FabricClientGameTest {
    private BlockPos base;
    private PeepoEntity npc;
    private BlockEntity station;
    private DisplayCompanion display;
    private Container source,output;
    private int checks;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private long insert(Storage<ItemVariant> port,ItemStack stack,long count,boolean commit){try(var tx=Transaction.openOuter()){long n=port.insert(ItemVariant.of(stack),count,tx);if(commit)tx.commit();return n;}}
    private int count(Container container){int n=0;for(int i=0;i<container.getContainerSize();i++)n+=container.getItem(i).getCount();return n;}
    private void reset(ServerLevel level,boolean jughead){
        if(npc!=null){npc.transport.stop();npc.resetCompanionRoutine();npc.discard();}
        for(var p:BlockPos.betweenClosed(base.offset(-9,-1,-5),base.offset(9,5,6)))level.setBlock(p,p.getY()==base.getY()-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        // Native display/container removal spills its contents; isolate each fixture after those drops occur.
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(Vec3.atLowerCornerOf(base.offset(-9,-1,-5)),Vec3.atLowerCornerOf(base.offset(10,6,7))).inflate(4),e->true).forEach(e->e.discard());
        level.setBlock(base.west(6),Blocks.BARREL.defaultBlockState(),18);source=(Container)level.getBlockEntity(base.west(6));
        level.setBlock(base.east(6),Blocks.BARREL.defaultBlockState(),18);output=(Container)level.getBlockEntity(base.east(6));
        npc=(jughead?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);npc.setNoAi(true);
        npc.snapTo(base.getX()+.5,base.getY(),base.getZ()-2.5,0,0);npc.setOnGround(true);level.addFreshEntity(npc);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();npc.orders.tame(owner);npc.orders.command(owner,3);npc.preferences.social=false;npc.preferences.carryMeals=0;
        npc.assignments.assignContainer(level,base.west(6),Direction.UP,false);npc.assignments.assignContainer(level,base.east(6),Direction.UP,true);
    }
    private CompanionLogistics.Port place(ServerLevel level,String id){
        var block=JugcraftAgriculture.block(id);var state=block.defaultBlockState();var clicked=base;
        if(block instanceof MultiDecorationBlock multi){
            var facing=state.getValue(MultiDecorationBlock.FACING);
            for(int i=0;i<multi.cells().length;i++)level.setBlock(multi.partPos(base,facing,i),state.setValue(multi.partProperty(),i),18);
            clicked=multi.partPos(base,facing,multi.cells().length-1);
            check(CompanionAssignments.canonical(level,clicked).equals(base),"display parts canonicalize to native master");
        }else level.setBlock(base,state,18);
        station=level.getBlockEntity(base);display=DisplayCompanion.of(station);
        check(display!=null && display.allowed(npc),"native serving display has accessible adapter: "+id);
        npc.assignments.assign(level,clicked);check(npc.assignments.assignedWork(base),"Planner assigns serving display: "+id);
        check(CompanionFilters.get(station)==display.filter,"shared Filter is attached to the native station");
        check(npc.assignments.transportDisplay(1,false)>>2==3,"display Output is marked unsupported");
        var port=CompanionLogistics.resolve(npc,npc.assignments.get(1));check(port!=null,"display has shared logistics route");
        check(!port.outputs().iterator().hasNext(),"serving food is not removed as Output");return port;
    }
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->{c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));c.setGenerateStructures(false);}).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();var origin=context.computeOnClient(c->c.player.blockPosition().above(4));
            base=new BlockPos((origin.getX()>>4)*16,origin.getY(),(origin.getZ()>>4)*16);
            server.runCommand("time set noon");server.runCommand("gamerule minecraft:random_tick_speed 0");server.runCommand("gamemode spectator @a");
            server.runOnServer(s->{party(s.overworld());food(s.overworld());produce(s.overworld());dispenser(s.overworld());claims(s.overworld());menu(s.overworld());});
            for(boolean jughead:new boolean[]{false,true})for(String id:new String[]{"spider_cupcake_tower","platter","pumpkin_pyramid","candy_corn_dispenser"})delivery(server,jughead,id);
            server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.discard();});
        }
        org.slf4j.LoggerFactory.getLogger("peepo-serving-display-test").info("[peepo-serving-display-test] PASS {} assertions",checks);
    }
    private void party(ServerLevel level){
        for(String id:new String[]{"spider_cupcake_tower","ghost_meringue_platter","mummy_sausage_tray","eyeball_jelly_mould","witch_finger_cauldron","goodie_bag_tree","skull_goblets"}){
            reset(level,false);var port=place(level,id);var block=(PartyServingBlock)station.getBlockState().getBlock();var food=new ItemStack(block.serves());var empty=level.getBlockState(base);
            check(CompanionFilters.set(station,0,food) && !CompanionFilters.set(station,1,new ItemStack(Items.DIAMOND)),"fixed serving filter accepts only its native food");
            check(insert(port.inputs(),food,100,false)==block.capacity(),"party probe obeys native capacity");
            check(level.getBlockState(base)==empty && port.needed(food)==block.capacity(),"aborted restock leaves native serving count unchanged");
            try(var outer=Transaction.openOuter()){
                check(port.inputs().insert(ItemVariant.of(food),2,outer)==2,"outer insert reserves two places");
                try(var nested=outer.openNested()){check(port.inputs().insert(ItemVariant.of(food),99,nested)==block.capacity()-2,"nested insert sees prior reservation");}
                check(port.needed(food)==block.capacity()-2,"nested abort restores pending native serving count");outer.commit();
            }
            check(level.getBlockState(base).getValue(block.servings())==2,"committed restock changes visible native count");
            var named=food.copy();named.set(DataComponents.CUSTOM_NAME,Component.literal("Keep this variant"));check(insert(port.inputs(),named,1,true)==0,"count-only party stand does not erase custom item data");
            var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            level.getBlockState(base).useWithoutItem(level,owner,new BlockHitResult(Vec3.atCenterOf(base),Direction.UP,base,false));
            check(level.getBlockState(base).getValue(block.servings())==1,"manual serving still takes exactly one food");
            check(insert(port.inputs(),food,99,true)==block.capacity()-1 && level.getBlockState(base).getValue(block.servings())==block.capacity(),"restocking replenishes manual consumption without overfill");
            var saved=station.saveWithFullMetadata(level.registryAccess());var clone=station.getType().create(base,station.getBlockState());clone.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            check(DisplayCompanion.of(clone).filter.get(0).is(food.getItem()),"party Filter persists beside native block-state contents");
            npc.assignments.clear(1);check(insert(port.inputs(),food,1,true)==0 && port.plan()==null,"cached display route refuses removed assignment");
            level.removeBlockEntity(base);var upgraded=level.getBlockEntity(base);
            check(upgraded instanceof ServingDisplayBlockEntity && level.getBlockState(base).getValue(block.servings())==block.capacity(),"existing party block without saved settings gains its unticked entity and keeps servings");
        }
    }
    private void emptyFood(ServerLevel level,ShowcaseBlockEntity shown){
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();
        for(int i=0;i<shown.places();i++)if(!shown.get(i).isEmpty())shown.use(i,ItemStack.EMPTY,owner,InteractionHand.MAIN_HAND,(FoodDisplayBlock)shown.getBlockState().getBlock());
    }
    private void food(ServerLevel level){
        for(String id:new String[]{"plate","platter","serving_tray"}){
            reset(level,false);var port=place(level,id);var shown=(ShowcaseBlockEntity)station;
            check(port.status()==CompanionStatus.SERVING_SETUP && port.needed(new ItemStack(Items.APPLE))==0,"unconfigured display waits for a real layout");
            var named=new ItemStack(Items.APPLE);named.set(DataComponents.CUSTOM_NAME,Component.literal("Display apple"));
            for(int i=0;i<shown.places();i++)shown.put(i,i%2==0?named:new ItemStack(Items.BREAD));
            emptyFood(level,shown);int apples=(shown.places()+1)/2,bread=shown.places()/2;
            check(shown.count()==0 && port.needed(named)==apples && port.needed(new ItemStack(Items.APPLE))==0,"empty food places remember exact item components");
            check(insert(port.inputs(),named,99,false)==apples && shown.count()==0,"food layout probe fully rolls back native slots");
            check(CompanionFilters.set(station,0,new ItemStack(Items.BREAD)) && port.needed(named)==0,"Filter restricts remembered layout");
            CompanionFilters.set(station,0,ItemStack.EMPTY);
            check(insert(port.inputs(),named,99,true)==apples,"food refill retains named variant");
            check(insert(port.inputs(),new ItemStack(Items.BREAD),99,true)==bread && shown.count()==shown.places(),"food refill preserves per-slot layout and native capacity");
            for(int i=0;i<shown.places();i++)check(ItemStack.isSameItemSameComponents(shown.get(i),i%2==0?named:new ItemStack(Items.BREAD)),"refilled place has its selected exact item");
            emptyFood(level,shown);var saved=station.saveWithFullMetadata(level.registryAccess());var clone=station.getType().create(base,station.getBlockState());clone.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            level.setBlockEntity(clone);station=clone;display=DisplayCompanion.of(clone);port=display.forCompanion(npc);
            check(port.needed(named)==apples && port.needed(new ItemStack(Items.BREAD))==bread,"empty slot templates persist through native save/load");
            ((ShowcaseBlockEntity)clone).put(0,new ItemStack(Items.CARROT));emptyFood(level,(ShowcaseBlockEntity)clone);
            check(port.needed(new ItemStack(Items.CARROT))==1 && port.needed(named)==apples-1,"manual replacement changes only that place's remembered food");
        }
    }
    private void produce(ServerLevel level){
        for(String id:new String[]{"pumpkin_wheelbarrow","pumpkin_wagon","pumpkin_pyramid"}){
            reset(level,false);var port=place(level,id);var produce=(ProduceDisplayBlockEntity)station;var pumpkin=new ItemStack(Items.PUMPKIN);
            check(insert(port.inputs(),pumpkin,99,false)==produce.capacity() && produce.isEmpty(),"produce probe restores native display contents");
            check(insert(port.inputs(),new ItemStack(Items.DIAMOND),1,true)==0,"produce display rejects unrelated items");
            CompanionFilters.set(station,0,new ItemStack(Items.MELON));check(port.needed(pumpkin)==0,"produce Filter limits native produce tag");CompanionFilters.set(station,0,pumpkin);
            pumpkin.set(DataComponents.CUSTOM_NAME,Component.literal("Prize pumpkin"));
            check(insert(port.inputs(),pumpkin,99,true)==produce.capacity() && produce.isFull(),"produce inserts only native number of display places");
            check(produce.produce().stream().allMatch(s->ItemStack.isSameItemSameComponents(s,pumpkin)),"produce display preserves each exact item variant");
            var saved=station.saveWithFullMetadata(level.registryAccess());var clone=(ProduceDisplayBlockEntity)station.getType().create(base,station.getBlockState());clone.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
            check(clone.produce().size()==produce.capacity() && clone.produce().stream().allMatch(s->ItemStack.isSameItemSameComponents(s,pumpkin)) && clone.companion.filter.get(0).is(Items.PUMPKIN),"native produce and Filter survive save/load");
            check(ItemStack.isSameItemSameComponents(produce.takeLast(),pumpkin) && port.needed(pumpkin)==1,"manual produce removal permits one replenishment");
            var block=(ProduceDisplayBlock)station.getBlockState().getBlock();var part=block.partPos(base,station.getBlockState().getValue(MultiDecorationBlock.FACING),block.cells().length-1);
            level.setBlock(part,Blocks.AIR.defaultBlockState(),18);
            check(!display.allowed(npc) && insert(port.inputs(),pumpkin,1,true)==0,"incomplete multipart display refuses a cached route");
        }
    }
    private void dispenser(ServerLevel level){
        reset(level,false);var port=place(level,"candy_corn_dispenser");var dispenser=(CandyCornDispenserBlockEntity)station;var corn=new ItemStack(JugcraftAgriculture.item("candy_corn"));
        check(insert(port.inputs(),corn,100,false)==64 && dispenser.count()==0 && station.getBlockState().getValue(CandyCornDispenserBlock.FILL)==0,"candy dispenser probe leaves native count and globe unchanged");
        check(insert(port.inputs(),corn,100,true)==64 && dispenser.count()==64 && station.getBlockState().getValue(CandyCornDispenserBlock.FILL)==4,"candy refill commits native capacity and globe fill");
        check(dispenser.take() && port.needed(corn)==1 && insert(port.inputs(),corn,10,true)==1,"native dispenser removal allows exact replenishment");
        check(!CompanionFilters.set(station,0,new ItemStack(Items.DIAMOND)) && CompanionFilters.set(station,0,corn),"dispenser Filter accepts candy corn only");
        var saved=station.saveWithFullMetadata(level.registryAccess());var clone=(CandyCornDispenserBlockEntity)station.getType().create(base,station.getBlockState());clone.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        check(clone.count()==64 && clone.displayCompanion.filter.get(0).is(corn.getItem()),"dispenser native count and Filter persist");
    }
    private void claims(ServerLevel level){
        reset(level,false);var port=place(level,"pumpkin_pyramid");var block=(ProduceDisplayBlock)station.getBlockState().getBlock();var state=station.getBlockState();
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();var stranger=FakePlayer.get(level,new GameProfile(UUID.randomUUID(),"DisplayOwner"));
        var book=Ledger.get(level.getServer()).book();var land=LandRegistry.get(level.getServer());var ownerAccount=book.account(owner.getUUID(),owner.getName().getString());var otherAccount=book.account(stranger.getUUID(),"DisplayOwner");
        var keys=new HashSet<Long>();for(int i=0;i<block.cells().length;i++)keys.add(LandRegistry.keyOf(block.partPos(base,state.getValue(MultiDecorationBlock.FACING),i)));
        long masterKey=LandRegistry.keyOf(base);var otherKey=keys.stream().filter(k->k!=masterKey).findFirst().orElseThrow();
        try{
            for(var key:keys)check(land.assign(book,key,ownerAccount,0).ok(),"owned display footprint claim");
            check(display.allowed(npc),"owner claim permits restocking");land.assign(book,otherKey,otherAccount,0);
            check(!display.allowed(npc) && insert(port.inputs(),new ItemStack(Items.PUMPKIN),1,true)==0,"foreign non-master display part denies cached transfer");
            check(land.trust(stranger,book,ownerAccount.name,true).ok() && display.allowed(npc),"live trust restores permitted full-footprint access");
            land.trust(stranger,book,ownerAccount.name,false);check(!display.allowed(npc),"live trust revocation takes effect");
        }finally{for(var key:keys)land.abandon(owner,book,key,true,0);}
    }
    private void menu(ServerLevel level){
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();owner.setGameMode(GameType.CREATIVE);reset(level,false);place(level,"spider_cupcake_tower");owner.snapTo(npc.getX(),npc.getY(),npc.getZ()-2,0,0);
        owner.openMenu(new SimpleMenuProvider((id,inventory,p)->new CompanionMenu(id,inventory,npc),npc.getDisplayName()));var menu=(CompanionMenu)owner.containerMenu;
        check(menu.clickMenuButton(owner,60) && menu.filterRow()==0,"companion menu opens serving-display Filter");
        var icon=new ItemStack(JugcraftAgriculture.item("spiderweb_cupcake"));menu.setCarried(icon.copy());menu.clicked(CompanionMenu.FILTER_START,0,net.minecraft.world.inventory.ContainerInput.PICKUP,owner);
        check(display.filter.get(0).is(icon.getItem()) && menu.getCarried().getCount()==1,"display ghost Filter does not consume cursor items");menu.setCarried(ItemStack.EMPTY);owner.closeContainer();owner.setGameMode(GameType.SPECTATOR);
    }
    private int displayed(){
        if(station instanceof ShowcaseBlockEntity shown)return shown.count();
        if(station instanceof ProduceDisplayBlockEntity produce)return produce.produce().size();
        if(station instanceof CandyCornDispenserBlockEntity dispenser)return dispenser.count();
        return station.getBlockState().getValue(((PartyServingBlock)station.getBlockState().getBlock()).servings());
    }
    private void delivery(TestServerContext server,boolean jughead,String id){
        int[] target={0},supplied={0};
        server.runCommand("gamemode creative @a");server.runOnServer(s->{var level=s.overworld();reset(level,jughead);place(level,id);check(npc.orders.mode()==CompanionOrders.Mode.WORK.ordinal(),"autonomous display worker uses Work command");ItemStack ingredient;
            if(station instanceof ShowcaseBlockEntity shown){ingredient=new ItemStack(Items.BREAD);for(int i=0;i<shown.places();i++)shown.put(i,ingredient);emptyFood(level,shown);target[0]=shown.places();}
            else if(station instanceof ProduceDisplayBlockEntity produce){ingredient=new ItemStack(Items.PUMPKIN);target[0]=produce.capacity();}
            else if(station instanceof CandyCornDispenserBlockEntity){ingredient=new ItemStack(JugcraftAgriculture.item("candy_corn"));target[0]=64;}
            else{var party=(PartyServingBlock)station.getBlockState().getBlock();ingredient=new ItemStack(party.serves());target[0]=party.capacity();}
            CompanionFilters.set(station,0,ingredient);source.setItem(0,ingredient.copyWithCount(ingredient.getMaxStackSize()));source.setItem(1,ingredient.copyWithCount(ingredient.getMaxStackSize()));supplied[0]=count(source);npc.setNoAi(false);
        });server.runCommand("gamemode spectator @a");
        int before=supplied[0];server.waitFor(s->displayed()==target[0],1800);
        server.runOnServer(s->{check(displayed()==target[0] && count(source)==before-target[0],"autonomous "+(jughead?"Jughead":"Peepo")+" fills "+id+" using real Supply");check(count(output)==0,"display restocking does not drain food to Output");});
        server.runOnServer(s->{var owner=s.getPlayerList().getPlayers().getFirst();owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var level=s.overworld();
                if(station instanceof ShowcaseBlockEntity shown)shown.use(0,ItemStack.EMPTY,owner,InteractionHand.MAIN_HAND,(FoodDisplayBlock)shown.getBlockState().getBlock());
                else level.getBlockState(base).useWithoutItem(level,owner,new BlockHitResult(Vec3.atCenterOf(base),Direction.UP,base,false));
                check(displayed()==target[0]-(id.equals("candy_corn_dispenser")?0:1),"manual serving removes one immediately, dispenser waits for its native crank");
        });
        if(id.equals("candy_corn_dispenser")){
            server.waitFor(s->displayed()==target[0]-1,120);
            server.runOnServer(s->{int loose=s.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(base).inflate(2),e->e.getItem().is(JugcraftAgriculture.item("candy_corn"))).stream().mapToInt(e->e.getItem().getCount()).sum();check(loose==1,"native crank dispenses exactly one real candy after its delay; found "+loose);});
        }
        server.waitFor(s->displayed()==target[0],1200);
        server.runOnServer(s->check(count(source)==before-target[0]-1 && count(output)==0,"autonomous replenishment replaces exactly one consumed serving"));
        server.runOnServer(s->{npc.transport.stop();npc.resetCompanionRoutine();npc.setNoAi(true);});
    }
}
