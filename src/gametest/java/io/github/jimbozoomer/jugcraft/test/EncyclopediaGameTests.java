package io.github.jimbozoomer.jugcraft.test;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guide.EncyclopediaProgress;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
public class EncyclopediaGameTests {
    @GameTest
    public void goalsArePersonalReadOnlyAndReflectAuthoritativeCompletion(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();var other=h.makeMockServerPlayerInLevel();
        var id=Jugcraft.id("encyclopedia_hydrochloric_acid_bucket");
        var advancement=h.getLevel().getServer().getAdvancements().get(id);
        h.assertTrue(advancement!=null,"New acid objective loaded");
        var before=EncyclopediaProgress.snapshot(player);
        h.assertTrue(before.goals().size()>=60 && before.goals().size()<=512,"Bounded full goal catalog");
        h.assertFalse(player.getAdvancements().getOrStartProgress(advancement).isDone(),"Reading never grants progress");
        player.getAdvancements().award(advancement,"obtain");
        var goal=EncyclopediaProgress.snapshot(player).goals().stream().filter(g->g.id().equals(id.toString())).findFirst().orElseThrow();
        h.assertTrue(goal.done() && goal.completed()==1 && goal.total()==1,"Only server-earned completion displayed");
        var theirs=EncyclopediaProgress.snapshot(other).goals().stream().filter(g->g.id().equals(id.toString())).findFirst().orElseThrow();
        h.assertFalse(theirs.done(),"No cross-player progress leak");
        h.assertTrue(EncyclopediaProgress.snapshot(player).equals(EncyclopediaProgress.snapshot(player)),"Repeated reads neither claim rewards nor mutate completion");
        h.succeed();
    }
}
