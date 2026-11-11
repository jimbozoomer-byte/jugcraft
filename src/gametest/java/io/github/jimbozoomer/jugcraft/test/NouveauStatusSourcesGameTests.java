package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import io.github.jimbozoomer.jugcraft.concordance.effect.*;
import io.github.jimbozoomer.jugcraft.nouveau.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;

/** Immolation and Drygmy's Blessing were registered with nothing giving them; both now brew, and Immolation boosts fire spells. */
public class NouveauStatusSourcesGameTests {
    @GameTest(maxTicks=20)
    public void immolationAndDrygmysBlessingBrew(GameTestHelper h){
        for(String name:List.of("immolate","looting"))for(String suffix:List.of("","_long","_strong"))
            h.assertTrue(BuiltInRegistries.POTION.containsKey(Jugcraft.id("nouveau_"+name+suffix)),"Potion nouveau_"+name+suffix+" registered");
        for(String recipe:List.of("potion_immolate","potion_looting","potion_immolate_strong","splash_potion_looting_extended","immolate_lingering_potion"))
            h.assertTrue(h.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Jugcraft.id("nouveau/brewing/"+recipe))).isPresent(),"Brewing recipe "+recipe);
        h.succeed();
    }
    @GameTest(maxTicks=20)
    public void immolationAddsToFireSpellsOnly(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);
        var target=h.spawn(EntityTypes.ZOMBIE,new BlockPos(2,2,2));
        var cause=Cause.of(p.getUUID(),Cause.Origin.SPELL,"jugcraft:test_immolation",ConcordanceEffects.nextSerial());
        var context=new ConcordanceEffects.Context(h.getLevel(),cause,p,new Ledger(new Ledger.Limits(16,64,0)),"test",p.position());
        var fire=new EffectSpec(EffectKind.DAMAGE,Intent.HARMFUL,4,0,null,Stacking.STRONGEST,"fire");
        var plain=EffectSpec.of(EffectKind.DAMAGE,Intent.HARMFUL,4,0);
        h.assertTrue(NouveauThreads.beforeSpell(context,fire,target).magnitude()==4,"No bonus without Immolation");
        h.assertTrue(NouveauStatuses.give(p,"immolate",200,1,p),"Immolation applies");
        h.assertTrue(NouveauThreads.beforeSpell(context,fire,target).magnitude()==8,"Immolation II adds four to a fire spell");
        h.assertTrue(NouveauThreads.beforeSpell(context,plain,target).magnitude()==4,"Other spells are unchanged");
        h.succeed();
    }
}
