package local.peepo;

import java.util.UUID;
import java.util.function.BiConsumer;
import io.github.jimbozoomer.jugcraft.agriculture.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.gamerules.GameRules;

/** Native job actions with mob_griefing off, without changing permissions or other mobs' rule. */
final class CompanionWorkPermissionChecks {
    private final BiConsumer<Boolean,String> check;
    private final ServerLevel level;
    private final BlockPos base;
    private PeepoEntity npc;
    private CompanionJob active;
    private VanillaCompanionPort hivePort;
    CompanionWorkPermissionChecks(BiConsumer<Boolean,String> check,ServerLevel level,BlockPos base){this.check=check;this.level=level;this.base=base;}
    private PeepoEntity helper(boolean pink){
        for(var pos:BlockPos.betweenClosed(base.offset(-5,-2,-5),base.offset(5,3,5)))level.setBlock(pos,pos.getY()<base.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        var p=(pink?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(level,EntitySpawnReason.COMMAND);
        p.setNoAi(true);p.snapTo(base.getX()+2.5,base.getY(),base.getZ()+2.5,0,0);p.setOnGround(true);level.addFreshEntity(p);
        var owner=level.getServer().getPlayerList().getPlayers().getFirst();p.orders.tame(owner);p.orders.command(owner,3);p.preferences.social=false;p.preferences.carryMeals=0;return p;
    }
    private void work(TestServerContext server,int duration,CompanionStatus last){
        long[] previous={Long.MIN_VALUE};int[] ticks={0};
        server.waitFor(s->{
            if(previous[0]==level.getGameTime())return false;previous[0]=level.getGameTime();
            var status=active.work(npc);ticks[0]++;
            check.accept(status==(ticks[0]==duration?last:CompanionStatus.WORKING),"normal-tick work with mob griefing off: tick "+ticks[0]+", "+status);
            return ticks[0]==duration;
        },duration+60);
    }
    void run(TestServerContext server){
        server.runOnServer(s->{
            var rules=level.getGameRules();rules.set(GameRules.MOB_GRIEFING,true,level.getServer());
            check.accept(Crow.mayPeck(level),"ordinary crow follows enabled mob griefing");
            rules.set(GameRules.MOB_GRIEFING,false,level.getServer());
        });
        for(boolean pink:new boolean[]{false,true}){
            server.runOnServer(s->{
                var p=npc=helper(pink);
                var gate=Blocks.OAK_FENCE_GATE.defaultBlockState();
                check.accept(CompanionGates.mayOpen(p,base,gate),"owned companion may use unpowered gates with mob griefing off");
                check.accept(!CompanionGates.mayOpen(p,base,gate.setValue(FenceGateBlock.POWERED,true)),"powered gates retain player/redstone control");
                var wild=PeepoMod.PEEPO.create(level,EntitySpawnReason.COMMAND);
                check.accept(!CompanionGates.mayOpen(wild,base,gate),"wild companions do not gain gate permission");
                var owner=level.getServer().getPlayerList().getPlayers().getFirst();
                var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(level.getServer());
                var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(level.getServer()).book();
                var key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(base);
                try{
                    check.accept(land.assign(book,key,book.account(UUID.randomUUID(),"WorkClaimOwner"),0).ok(),"foreign claim fixture");
                    check.accept(!CompanionJobs.permitted(p,base) && !CompanionGates.mayOpen(p,base,gate),"mob-griefing exception never bypasses foreign claims");
                }finally{land.abandon(owner,book,key,true,0);}
                level.setBlockAndUpdate(base,Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL,5));
                var hive=(BeehiveBlockEntity)level.getBlockEntity(base);p.assignments.assign(level,base);
                var port=hivePort=VanillaCompanionPort.of(hive);var job=port.hiveJob().prepare(p);active=job;
                check.accept(job.readiness()==CompanionStatus.NEEDS_SMOKE,"smoke safety still applies with mob griefing off");
                level.setBlockAndUpdate(base.below(),Blocks.CAMPFIRE.defaultBlockState());
                check.accept(HiveCompanionJob.ready(hive),"hive readiness and tool Supply ignore the global mob rule");
                try(var tx=Transaction.openOuter()){check.accept(port.inputs().insert(ItemVariant.of(Items.GLASS_BOTTLE),1,tx)==1,"hive accepts actual bottle input");tx.commit();}
                check.accept(job.claim(p),"ripe smoked hive claims with mob griefing off");p.setPos(job.approachPosition());
            });
            work(server,60,CompanionStatus.WORKING);
            server.runOnServer(s->{
                check.accept(level.getBlockState(base).getValue(BeehiveBlock.HONEY_LEVEL)==0,"hive harvest resets real honey");
                long honey=0;for(var view:hivePort.outputs())if(view.getResource().isOf(Items.HONEY_BOTTLE))honey+=view.getAmount();
                check.accept(honey==1,"hive produces exactly one real honey bottle");active.release(npc);npc.discard();
                var p=npc=helper(pink);
                level.setBlockAndUpdate(base,JugcraftAgriculture.block("bat_house").defaultBlockState());
                var house=(BatHouseBlockEntity)level.getBlockEntity(base);house.set(0,3);p.assignments.assign(level,base);
                var job=house.companionJob.prepare(p);active=job;check.accept(job.claim(p),"bat house claims with mob griefing off");p.setPos(job.approachPosition());
            });
            work(server,BatHouseCompanionJob.WORK_TICKS,CompanionStatus.GUANO_WAIT);
            server.runOnServer(s->{
                var p=npc;var house=(BatHouseBlockEntity)level.getBlockEntity(base);
                check.accept(house.guano()==0,"bat house collects real guano with mob griefing off");
                int guano=0;for(int i=0;i<8;i++)if(p.belongings.getItem(i).is(JugcraftAgriculture.item("bat_guano")))guano+=p.belongings.getItem(i).getCount();
                check.accept(guano==3,"bat guano is preserved in companion cargo");active.release(p);p.discard();
                p=helper(pink);
                var seat=base.below();var water=seat.south(2);level.setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());
                check.accept(p.assignments.assignFishing(seat,water).startsWith("Fishing assigned"),"fishing assignment accepts a safe bank");
                p.belongings.setItem(9,new ItemStack(Items.FISHING_ROD));var job=p.fishing.job(seat);
                check.accept(job!=null && job.workStatus(p)==CompanionStatus.READY && job.claim(p),"fishing readiness ignores mob griefing");
                p.setPos(job.approachPosition());check.accept(job.work(p)==CompanionStatus.WORKING,"fishing starts a cast with mob griefing off");job.release(p);
                p.discard();
            });
        }
        server.runOnServer(s->check.accept(!level.getGameRules().get(GameRules.MOB_GRIEFING) && !Crow.mayPeck(level),"companion work leaves the global rule and ordinary crow protection unchanged"));
    }
}
