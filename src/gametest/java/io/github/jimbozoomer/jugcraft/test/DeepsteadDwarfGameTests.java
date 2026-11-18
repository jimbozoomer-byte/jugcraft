package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.DeepsteadDwarf;
import io.github.jimbozoomer.jugcraft.town.DeepsteadWares;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

public final class DeepsteadDwarfGameTests {
    private static DeepsteadDwarf dwarf(GameTestHelper h,DeepsteadDwarf.Role role,int look) {
        var home=h.absolutePos(new BlockPos(2,2,2));
        var dwarf=DeepsteadDwarf.TYPE.create(h.getLevel(),EntitySpawnReason.COMMAND);
        dwarf.configure(role,"Torra Oakhammer",home,look);
        dwarf.setPos(home.getX()+.5,home.getY(),home.getZ()+.5);
        return dwarf;
    }

    @GameTest public void savedMerchantsRetainPostSkinAndPersonalCounter(GameTestHelper h) {
        var level=h.getLevel();
        for(int appearance=0;appearance<2;appearance++) {
            var original=dwarf(h,DeepsteadDwarf.Role.MERCHANT,appearance);
            var data=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            original.saveWithoutId(data);
            var restored=DeepsteadDwarf.TYPE.create(level,EntitySpawnReason.COMMAND);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data.buildResult()));
            h.assertTrue(restored.isMerchant() && restored.dwarfRole()==DeepsteadDwarf.Role.MERCHANT && restored.appearance()==appearance,
                "The guild merchant still wears the right skin on load");
            h.assertTrue(restored.home().equals(original.home()) && restored.getName().getString().equals("Torra Oakhammer"),
                "Named merchant keeps its assigned work post");
            h.assertTrue(restored.shop().equals(appearance==0?DeepsteadWares.LAMPWRIGHT:DeepsteadWares.OUTFITTER),
                "Both guild counters must select their own stock");
        }
        h.succeed();
    }

    @GameTest public void onlyTheWatchEngagesAndOnlyOnItsOwnTier(GameTestHelper h) {
        var guard=dwarf(h,DeepsteadDwarf.Role.GUARD,1);
        var hostile=EntityTypes.HUSK.create(h.getLevel(),EntitySpawnReason.COMMAND);
        hostile.setPos(guard.position().add(2,0,0));
        h.assertTrue(guard.canAttack(hostile) && !dwarf(h,DeepsteadDwarf.Role.MINER,1).canAttack(hostile)
            && !dwarf(h,DeepsteadDwarf.Role.MERCHANT,0).canAttack(hostile), "Only the watch engages local hostiles");
        h.assertTrue(!guard.canAttack(h.makeMockServerPlayerInLevel()), "Visitor is welcome");
        hostile.setPos(guard.position().add(1,14,1));
        h.assertTrue(!guard.canAttack(hostile), "No pursuit onto a bridge one gallery above");
        hostile.setPos(guard.position().add(26,0,0));
        h.assertTrue(!guard.canAttack(hostile), "The gate and unrelated city sections are outside this patrol");
        h.assertTrue(!guard.hurtServer(h.getLevel(),guard.damageSources().generic(),100), "City watch is protected");
        h.succeed();
    }

    @GameTest(maxTicks=160) public void watchActuallyDefendsItsLanding(GameTestHelper h) {
        for(int x=0;x<=6;x++) for(int z=0;z<=4;z++) h.setBlock(new BlockPos(x,1,z),Blocks.DEEPSLATE_BRICKS);
        var guard=dwarf(h,DeepsteadDwarf.Role.GUARD,0);
        h.getLevel().addFreshEntity(guard);
        var intruder=h.spawn(EntityTypes.HUSK,new BlockPos(4,2,2));
        intruder.setNoAi(true);
        h.succeedWhen(()->h.assertTrue(intruder.getHealth()<intruder.getMaxHealth() && guard.getHealth()==guard.getMaxHealth(),
            "The Deepstead watch must acquire and damage the intruder on its own"));
    }
}
