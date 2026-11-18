package io.github.jimbozoomer.jugcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Screenshot gallery for the 13 November decor geometry review, in a disposable world: connected cozyhome tables
 * and fountains in their joined states (including a lone diagonal neighbour, which the blockstates now ignore as the
 * source does), the Christmas tree stacks whose bulb caps and inner branch ring were closed, and the bathtub taps
 * (reviewed, unchanged). It renders and captures; it asserts only that the blocks exist and the shots are taken.
 */
public final class ConnectedDecorClientTests implements FabricClientGameTest {
    private static final String TABLE="homeworks_oak_table",FOUNTAIN="homeworks_andesite_fountain";
    /** Joined layouts as (x,z) cells relative to the group's origin. */
    private static final List<int[][]> LAYOUTS=List.of(
        new int[][]{{0,0}},                                                    // single
        new int[][]{{0,0},{1,0},{2,0}},                                        // row of three
        new int[][]{{0,0},{1,0},{0,1}},                                        // L: one concave corner
        new int[][]{{0,0},{1,0},{0,1},{1,1}},                                  // 2x2: four inner fills
        new int[][]{{0,0},{1,0},{2,0},{0,1},{2,1},{0,2},{1,2},{2,2}},          // ring round an empty cell
        new int[][]{{0,0},{1,1}});                                             // lone diagonal neighbours
    private static final String[] NAMES={"single","row","l","square","ring","diagonal"};

    @Override public void runTest(ClientGameTestContext c){
        for(String name:List.of(TABLE,FOUNTAIN,"homeworks_birch_table","homeworks_andesite_bathtub",
                "holidayworks_christmas_tree_base","holidayworks_christmas_tree_bottom","holidayworks_christmas_tree_middle","holidayworks_christmas_tree_top",
                "holidayworks_red_decorated_christmas_tree_base","holidayworks_red_decorated_christmas_tree_bottom",
                "holidayworks_red_decorated_christmas_tree_middle","holidayworks_red_decorated_christmas_tree_top")){
            var id=Identifier.fromNamespaceAndPath("jugcraft",name);
            if(!BuiltInRegistries.BLOCK.containsKey(id))throw new AssertionError("Missing block "+id);
        }
        try(var world=c.worldBuilder().create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();
            // Spectator: the camera stays exactly where each tp puts it (no falling between shots).
            server.runCommand("gamemode spectator @a");server.runCommand("time set noon");server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("fill -16 79 -8 16 79 16 minecraft:smooth_stone");
            server.runCommand("fill -16 80 -8 16 90 16 minecraft:air");
            // A one-deep trench in front of the groups lets the low shots look up under table tops and bowls.
            server.runCommand("fill -6 79 -1 9 79 1 minecraft:air");
            for(String block:List.of(TABLE,FOUNTAIN)){
                for(int i=0;i<LAYOUTS.size();i++){
                    for(int[] cell:LAYOUTS.get(i))server.runCommand("setblock "+cell[0]+" 80 "+(4+cell[1])+" jugcraft:"+block);
                    c.waitTicks(20);
                    // Three-quarter views from above (standing eye height), and one from the trench, eye below the table
                    // top and the bowl (feet at y 78.4, eye about 80.0), looking up under them.
                    server.runCommand("tp @a -3 82.6 1 -45 25");c.waitTicks(10);c.takeScreenshot("nov13_"+block+"_"+NAMES[i]+"_front");
                    server.runCommand("tp @a 6 82.6 10 135 25");c.waitTicks(10);c.takeScreenshot("nov13_"+block+"_"+NAMES[i]+"_back");
                    server.runCommand("tp @a 4.5 78.4 0 30 -12");c.waitTicks(10);c.takeScreenshot("nov13_"+block+"_"+NAMES[i]+"_low");
                    server.runCommand("fill -4 80 0 8 83 10 minecraft:air");
                }
            }
            // A filled fountain basin stays open to the top on purpose; the water sits inside the joined rim.
            for(int[] cell:LAYOUTS.get(2))server.runCommand("setblock "+cell[0]+" 80 "+(4+cell[1])+" jugcraft:"+FOUNTAIN+"[contains=water]");
            c.waitTicks(20);server.runCommand("tp @a -3 83 1 -45 35");c.waitTicks(10);c.takeScreenshot("nov13_"+FOUNTAIN+"_l_water");
            server.runCommand("fill -4 80 0 8 83 10 minecraft:air");
            // Different woods retain their own closed edges; matching tables alone join.
            server.runCommand("setblock 0 80 4 jugcraft:"+TABLE);server.runCommand("setblock 1 80 4 jugcraft:homeworks_birch_table");
            c.waitTicks(20);server.runCommand("tp @a -3 82.6 1 -45 25");c.waitTicks(10);c.takeScreenshot("nov13_table_oak_birch_join");
            server.runCommand("fill -4 80 0 8 83 10 minecraft:air");
            // Christmas trees: full stacks (plain and red), and a base and a bottom left as the top of the stack.
            String[] sections={"base","bottom","middle","top"};
            for(int s=0;s<4;s++){
                server.runCommand("setblock 0 "+(80+s)+" 4 jugcraft:holidayworks_christmas_tree_"+sections[s]+"[facing=south]");
                server.runCommand("setblock 4 "+(80+s)+" 4 jugcraft:holidayworks_red_decorated_christmas_tree_"+sections[s]+"[facing=south]");
            }
            server.runCommand("setblock 8 80 4 jugcraft:holidayworks_red_decorated_christmas_tree_base[facing=south]");
            server.runCommand("setblock 11 80 4 jugcraft:holidayworks_red_decorated_christmas_tree_base[facing=south]");
            server.runCommand("setblock 11 81 4 jugcraft:holidayworks_red_decorated_christmas_tree_bottom[facing=south]");
            c.waitTicks(30);
            server.runCommand("tp @a 2 80 -3 0 -12");c.waitTicks(12);c.takeScreenshot("nov13_trees_eye_level");
            server.runCommand("tp @a 4.5 80 1.5 0 -45");c.waitTicks(12);c.takeScreenshot("nov13_red_tree_from_below");
            server.runCommand("tp @a 9.5 84 0 0 50");c.waitTicks(12);c.takeScreenshot("nov13_tree_tops_base_and_bottom");
            server.runCommand("tp @a 2 86 10 180 30");c.waitTicks(12);c.takeScreenshot("nov13_trees_back_above");
            server.runCommand("fill -4 80 0 14 86 10 minecraft:air");
            // Bathtub with running taps (back part triggered): the taps' 0.4 px overhang was reviewed and left as supplied.
            server.runCommand("setblock 0 80 4 jugcraft:homeworks_andesite_bathtub[facing=south,part=front]");
            server.runCommand("setblock 0 80 5 jugcraft:homeworks_andesite_bathtub[facing=south,part=back,triggered=true]");
            c.waitTicks(20);
            server.runCommand("tp @a 2.5 81.6 2 35 30");c.waitTicks(10);c.takeScreenshot("nov13_bathtub_taps_front");
            // Worst case for the overhang: a pit behind the tub puts the eye (about 80.1) under the taps (80.75).
            server.runCommand("fill -1 79 7 2 79 9 minecraft:air");
            server.runCommand("tp @a 0.5 78.5 8.5 170 -25");c.waitTicks(10);c.takeScreenshot("nov13_bathtub_taps_behind_low");
        }
    }
}
