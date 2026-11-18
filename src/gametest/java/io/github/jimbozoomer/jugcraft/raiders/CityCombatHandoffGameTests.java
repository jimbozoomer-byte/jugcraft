package io.github.jimbozoomer.jugcraft.raiders;

import io.github.jimbozoomer.jugcraft.town.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Real gate geometry and navigation, without mutating the world's shared town save. */
public final class CityCombatHandoffGameTests {
    private static BlockPos gateFixture(GameTestHelper h) {
        var data=TownData.get();
        for(int x=0;x<40;x++)for(int z=0;z<40;z++){
            h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
            for(int y=1;y<22;y++)h.setBlock(new BlockPos(x,y,z),data.state(data.index(x+152,y,z+80)));
        }
        return h.absolutePos(BlockPos.ZERO).offset(-152,0,-80);
    }
    private static Townsfolk guard(GameTestHelper h, Vec3 pos) {
        var guard=h.spawnWithNoFreeWill(JugcraftTown.TOWNSFOLK,pos);
        guard.setUp(new TownData.Spot(-1,"guard",null,"Gate Guard","guard_blue",BlockPos.ZERO),guard.blockPosition());
        guard.setOnGround(true); // Tick-zero fixture: ground navigation is otherwise not ready yet.
        return guard;
    }
    @GameTest(structure="jugcraft-test:drone_tower",skyAccess=true)
    public void occludedCombatTargetDoesNotCancelCityApproach(GameTestHelper h) {
        var origin=gateFixture(h);
        var defender=guard(h,new Vec3(6.5,1,5.5));
        var attacker=h.spawnWithNoFreeWill(JugcraftRaiders.GRUNT,new Vec3(34.5,1,5.5));
        attacker.setTarget(defender);
        var march=new MarchGoal(attacker,()->Town.centre(origin),1,()->origin);
        h.assertFalse(attacker.getSensing().hasLineOfSight(defender),"Authored wall hides the acquired opponent");
        h.assertTrue(march.canUse(),"Hidden guard keeps city routing active");
        march.start();march.tick();
        h.assertTrue(attacker.getTarget()==defender,"Rerouting preserves the opponent identity");
        var ordinary=new MarchGoal(attacker,()->attacker.blockPosition().offset(0,0,4),1,()->origin);
        h.assertFalse(ordinary.canUse(),"Non-city combat is unchanged");
        defender.setPos(attacker.position().add(-2,0,0));attacker.getSensing().tick();
        h.assertTrue(attacker.getSensing().hasLineOfSight(defender),"Fixture exposes target outside wall");
        h.assertFalse(march.canContinueToUse(),"Visible opponent hands movement back to combat");
        march.stop();attacker.discard();defender.discard();h.succeed();
    }
    @GameTest(structure="jugcraft-test:drone_tower",skyAccess=true)
    public void defendersChooseReachablePostsInsideTheRealGate(GameTestHelper h) {
        var origin=gateFixture(h);
        var defender=guard(h,new Vec3(6.5,1,16.5));
        var enemy=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(34,1,16)));
        var path=TownDefense.gatePath(defender,origin,enemy);
        h.assertTrue(path!=null&&path.canReach(),"Complete path exists to an inner gate post");
        var end=path.getEndNode().asBlockPos();
        var gate=CityRaidRoute.nearest(origin,enemy);
        h.assertTrue(end.distManhattan(gate.passage())<=5,"Defender meets raid at gate, not fourteen blocks behind it");
        h.assertTrue(TownDefense.visible(h.getLevel(),defender,Vec3.atBottomCenterOf(end).add(0,1.6,0),enemy.add(0,1.6,0)),"Post sees the forecourt through actual gate geometry");
        for(int y=0;y<8;y++)h.getLevel().setBlockAndUpdate(end.above(y),Blocks.STONE.defaultBlockState());
        var alternate=TownDefense.gatePath(defender,origin,enemy);
        h.assertTrue(alternate!=null&&!alternate.getEndNode().asBlockPos().equals(end),"An obstructed preferred lane falls back to another reachable post");
        for(int lane=-1;lane<=1;lane++)for(int y=0;y<8;y++){
            var blocked=gate.passage().offset(-gate.dx()*4+gate.dz()*lane,y,-gate.dz()*4-gate.dx()*lane);
            h.getLevel().setBlockAndUpdate(blocked,Blocks.STONE.defaultBlockState());
        }
        h.assertTrue(TownDefense.gatePath(defender,origin,enemy)==null,"Owner-blocked posts are rejected rather than routing onto their roof");
        defender.discard();h.succeed();
    }
}
