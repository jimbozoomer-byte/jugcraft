package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.jimtafacts.spellbooks.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;

/** Deferred until the final combined suite, as requested by the owner. */
public final class JimtaEncounterGameTests {
    @GameTest public void predatorsRespectVillagersAndClaimedAnimals(GameTestHelper h){
        var spider=h.spawn(JimtaEncounterCreatures.ICE_SPIDER,2,2,2);var keeper=h.spawn(JimtaEncounterCreatures.KEEPER,4,2,2);
        var villager=h.spawn(EntityType.VILLAGER,2,2,4);var cow=h.spawn(EntityType.COW,4,2,4);
        h.assertTrue(!spider.canAttack(villager)&&!keeper.canAttack(villager),"Source enemies retain Jugcraft villager protection");
        try(var claim=TestClaims.open().block(cow.blockPosition(),java.util.UUID.randomUUID())){h.assertTrue(!spider.canAttack(cow),"Claimed livestock cannot be hunted");}
        h.succeed();
    }
    @GameTest public void hostileTombReleasesWhenItsSpiderDies(GameTestHelper h){
        var spider=h.spawn(JimtaEncounterCreatures.ICE_SPIDER,2,3,2);var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        p.setPos(spider.position().add(5,0,0));p.setHealth(10);JimtaSpiderTomb.entomb(spider,p);
        h.assertTrue(p.getVehicle() instanceof JimtaSpiderTomb,"Spider captures the player");var tomb=(JimtaSpiderTomb)p.getVehicle();
        spider.discard();tomb.tick();h.assertTrue(!p.isPassenger()&&tomb.isRemoved(),"Losing the owner releases the victim");h.assertTrue(p.getHealth()==10,"Hostile tomb never heals");h.succeed();
    }
    @GameTest public void restoredKeeperUsesTheOriginalRestoredWeapon(GameTestHelper h){
        var keeper=h.spawn(JimtaEncounterCreatures.KEEPER,2,2,2);keeper.setRestored(true);
        h.assertTrue(keeper.getMainHandItem().is(JimtaEncounterItems.ITEMS.get("legionnaire_flamberge")),"Restored state equips its source weapon");
        keeper.setRestored(false);h.assertTrue(keeper.getMainHandItem().is(JimtaEncounterItems.ITEMS.get("keeper_flamberge")),"Ordinary state equips the weathered weapon");h.succeed();
    }
}
