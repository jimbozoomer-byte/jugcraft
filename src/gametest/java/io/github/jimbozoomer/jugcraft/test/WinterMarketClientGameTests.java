package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FirGifts;
import io.github.jimbozoomer.jugcraft.agriculture.HarbourGiftFir;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/** Disposable local harbour and bathhouse. Photograph the shipped template through the normal block renderer. */
public final class WinterMarketClientGameTests implements FabricClientGameTest {
    private static final BlockPos QUAY = new BlockPos(1104, 65, 1056);

    @Override public void runTest(ClientGameTestContext context) {
        boolean hidden = context.computeOnClient(c -> c.gui.hud.isHidden());
        try (var world = context.worldBuilder().adjustSettings(c -> {
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
        }).create()) {
            var server = world.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("gamerule minecraft:advance_time false");
            server.runCommand("gamerule minecraft:advance_weather false");
            server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runCommand("tp @p 1114 70 1077");
            context.waitTicks(60);
            server.runCommand("fill 1080 63 1008 1144 63 1061 minecraft:polished_andesite");
            server.runCommand("fill 1080 64 1008 1144 64 1061 minecraft:water");
            server.runCommand("fill 1088 64 1056 1144 64 1094 minecraft:stone_bricks");
            server.runOnServer(s -> {
                var level = s.overworld();
                var template = level.getStructureTemplateManager().get(Jugcraft.id("holiday/west_gate_winter_market")).orElseThrow();
                if (!template.placeInWorld(level, QUAY, QUAY, new StructurePlaceSettings().setKnownShape(true), level.getRandom(), Block.UPDATE_CLIENTS))
                    throw new AssertionError("Market template placement failed");
                var trunk = QUAY.offset(15,1,3);
                if (!(level.getBlockEntity(trunk) instanceof FirGifts gifts)) throw new AssertionError("Gift fir has no trunk inventory");
                for (int i = 0; i < 12; i++)
                    if (!gifts.deposit(new ItemStack(Items.COOKIE, i+1), i%8)) throw new AssertionError("Lost a gallery gift");
                var fir = HarbourGiftFir.TREE;
                for (int i = 0; i < fir.cells().length; i++) {
                    var at = fir.partPos(trunk, net.minecraft.core.Direction.NORTH, i);
                    if (!level.getBlockState(at).is(fir) || level.getBlockState(at).getValue(HarbourGiftFir.PART) != i)
                        throw new AssertionError("Missing market fir part " + i);
                }
                for(int z=0; z<22; z++) for(int x=9; x<=13; x++) for(int y=1; y<=2; y++)
                    if (!level.isEmptyBlock(QUAY.offset(x,y,z))) throw new AssertionError("Quay sightline blocked");
            });
            server.runCommand("gamemode survival @p");
            server.runCommand("tp @p 1111.5 66 1063.5");
            server.runOnServer(s -> {
                var level=s.overworld();
                var customer=s.getPlayerList().getPlayers().getFirst();
                int count=0;
                for (var resident : level.getAllEntities()) {
                    for (String role : java.util.List.of("post","bakery","toys")) {
                        if (!resident.entityTags().contains("winter_market_"+role)) continue;
                        count++;
                        if (!(resident instanceof net.minecraft.world.entity.npc.villager.Villager clerk)
                                || !clerk.isNoAi() || !clerk.hasCustomName()) throw new AssertionError("Invalid "+role+" keeper");
                        if (role.equals("post"))
                            net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.invoker().interact(customer,level,
                                net.minecraft.world.InteractionHand.MAIN_HAND,clerk,null);
                    }
                }
                if (count!=3) throw new AssertionError("Expected three staffed booths, got "+count);
            });
            context.waitForScreen(io.github.jimbozoomer.jugcraft.client.WinterMarketScreen.class);
            context.clickScreenButton("My parcels");
            context.waitFor(mc -> mc.gui.screen() instanceof io.github.jimbozoomer.jugcraft.client.WinterMarketScreen screen
                && screen.getMenu().view.mode()==1,200);
            context.takeScreenshot("winter_parcel_counter");
            server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().closeContainer());
            context.waitForScreen(null);
            server.runCommand("gamemode spectator @p");
            buildBath(world);
            context.runOnClient(c -> { if (!c.gui.hud.isHidden()) c.gui.hud.toggle(); });
            shoot(context,world,"market",16,7.2,27,11.5,2.3,9);
            shoot(context,world,"parcel_stall",10.5,2.8,9.8,5.3,2.4,7.4);
            shoot(context,world,"bakery_stall",12.1,2.7,11,17,2.5,13.3);
            shoot(context,world,"toy_stall",11.3,2.8,19.7,5.4,2.4,17);
            shoot(context,world,"fir",18.9,3.5,8.1,15.5,3,3.5);
            shoot(context,world,"gift_piles",16.8,1.8,6,15.5,1.8,3.7);
            shoot(context,world,"snow_and_eaves",8.4,3.4,12.1,5.8,3.2,9.9);
            shoot(context,world,"changing",32.5,3.3,15.8,30.3,2.1,11.6);
            shoot(context,world,"pool",36.8,2.8,9.8,33.1,1.75,6.2);
            server.runCommand("time set 13000");
            shoot(context,world,"market_dusk",11.8,3.3,25,11.7,3.1,7);
            shoot(context,world,"fir_reverse",12.4,3.6,-1.5,15.5,3,3.5);
        } finally {
            context.runOnClient(c -> { if (c.gui.hud.isHidden() != hidden) c.gui.hud.toggle(); });
        }
    }

    private static void buildBath(TestSingleplayerContext world) {
        var server=world.getServer();
        server.runCommand("fill 1131 65 1057 1141 65 1070 minecraft:polished_basalt");
        server.runCommand("fill 1132 65 1060 1140 65 1064 minecraft:water");
        server.runCommand("fill 1131 66 1066 1138 68 1066 minecraft:stripped_spruce_wood");
        server.runCommand("fill 1131 69 1066 1138 69 1066 minecraft:spruce_slab");
        server.runCommand("setblock 1134 67 1067 jugcraft:hot_spring_peg_rail[facing=south]");
        server.runCommand("setblock 1133 66 1068 jugcraft:hot_spring_bench[facing=south]");
        server.runCommand("setblock 1134 66 1068 jugcraft:hot_spring_bench[facing=south]");
        server.runCommand("setblock 1136 66 1068 jugcraft:hot_spring_boot_tray[facing=south]");
        server.runCommand("setblock 1135 66 1064 minecraft:polished_basalt");
        server.runCommand("setblock 1135 67 1064 jugcraft:hot_spring_dipper_pail[facing=south]");
        server.runCommand("setblock 1137 65 1061 minecraft:polished_basalt");
        server.runCommand("setblock 1137 65 1060 minecraft:polished_basalt");
        // A waterlogged stair seals the feed and supplies source water without flooding the display.
        server.runCommand("setblock 1137 66 1059 minecraft:polished_deepslate_stairs[facing=north,waterlogged=true]");
        server.runCommand("setblock 1136 66 1059 minecraft:polished_basalt");
        server.runCommand("setblock 1138 66 1059 minecraft:polished_basalt");
        server.runCommand("setblock 1137 66 1058 minecraft:polished_basalt");
        server.runCommand("setblock 1137 66 1060 jugcraft:hot_spring_overflow_spout[facing=south,open=true,flowing=true]");
        server.runCommand("setblock 1139 65 1062 minecraft:polished_basalt");
        server.runCommand("setblock 1139 66 1062 jugcraft:hot_spring_pool_lantern");
    }

    private static void shoot(ClientGameTestContext c, TestSingleplayerContext w, String name, double x, double y,
            double z, double targetX, double targetY, double targetZ) {
        double dx = targetX-x, dz=targetZ-z;
        w.getServer().runCommand(String.format(Locale.ROOT,"tp @p %.2f %.2f %.2f %.2f %.2f",
                QUAY.getX()+x,QUAY.getY()+y-1.62,QUAY.getZ()+z,
                Math.toDegrees(Math.atan2(-dx,dz)),Math.toDegrees(Math.atan2(y-targetY,Math.hypot(dx,dz)))));
        c.waitTicks(40);
        w.getConnection().waitForChunksRender(false,1200);
        c.takeScreenshot("winter_"+name);
    }
}
