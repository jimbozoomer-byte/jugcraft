package io.github.jimbozoomer.jugcraft.test;

import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.entity.HumanoidArm;

/** Native player hand gallery: both dominant hands, actual first/third-person rendering and offhand use. */
public final class HeldEquipmentClientGameTests implements FabricClientGameTest {
    private static final List<String> ITEMS=List.of(
        "mining_drill","chainsaw","leaf_blower","power_katana","ronin_katana","power_bow",
        "jimta_industrial_drill","jimta_diamond_drill","jimta_iridium_drill","jimta_industrial_chainsaw",
        "jimta_electric_hoe","jimta_mining_laser","jimta_electric_wrench","jimta_electric_treetap",
        "jimta_industrial_wrench","jimta_hand_treetap","jimta_crop_analyzer","jimta_ore_scanner","jimta_advanced_ore_scanner",
        "jimta_nano_saber","jimta_obscurator","jimta_entropy_manipulator","jimta_charged_staff",
        "jimta_color_applicator","jimta_matter_cannon","jimta_network_tool",
        "minecraft:iron_sword","steel_pickaxe","steel_axe","steel_shovel","steel_hoe");

    @Override public void runTest(ClientGameTestContext context) {
        HumanoidArm original=context.computeOnClient(c->c.options.mainHand().get());
        int oldFov=context.computeOnClient(c->c.options.fov().get());
        try(var world=context.worldBuilder().adjustSettings(c->c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            var server=world.getServer();
            server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand("fill -8 99 -8 8 99 8 minecraft:smooth_stone");
            server.runCommand("tp @p 0.5 100 0.5 0 0");
            context.getInput().resizeWindow(1000,700);
            context.runOnClient(c->{if(c.gui.hud.isHidden())c.gui.hud.toggle();c.options.guiScale().set(2);c.resizeGui();});
            for(HumanoidArm hand:HumanoidArm.values()) {
                context.runOnClient(c->c.options.mainHand().set(hand));
                server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setMainArm(hand));
                context.waitFor(c->c.player.getMainArm()==hand,100);
                for(String item:ITEMS) {
                    String id=item.contains(":")?item:"jugcraft:"+item;
                    server.runCommand("item replace entity @p weapon.mainhand with "+id);
                    server.runCommand("item replace entity @p weapon.offhand with minecraft:air");
                    context.runOnClient(c->{c.options.setCameraType(CameraType.FIRST_PERSON);c.options.fov().set(70);c.gui.toastManager().clear();});
                    context.waitTicks(35);
                    context.waitFor(c->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(c.player.getMainHandItem().getItem()).toString().equals(id),100);
                    String name=item.replace(':','_')+"_"+hand.getSerializedName();
                    context.runOnClient(c->{c.gui.hud.getChat().clearMessages(true);c.gui.toastManager().clear();});
                    context.takeScreenshot("held_"+name+"_first");
                    context.runOnClient(c->{c.options.setCameraType(CameraType.THIRD_PERSON_FRONT);c.options.fov().set(40);});
                    context.waitTicks(4);context.takeScreenshot("held_"+name+"_third");
                }
                for(String item:List.of("mining_drill","chainsaw","leaf_blower","power_katana","jimta_electric_hoe",
                        "jimta_industrial_drill","jimta_ore_scanner","jimta_obscurator","jimta_matter_cannon")) {
                    server.runCommand("item replace entity @p weapon.mainhand with minecraft:air");
                    server.runCommand("item replace entity @p weapon.offhand with jugcraft:"+item);
                    context.runOnClient(c->{c.options.setCameraType(CameraType.FIRST_PERSON);c.options.fov().set(70);});
                    context.waitTicks(35);
                    context.runOnClient(c->{c.gui.hud.getChat().clearMessages(true);c.gui.toastManager().clear();});
                    context.takeScreenshot("held_"+item+"_"+hand.getSerializedName()+"_offhand");
                }
            }
            server.runCommand("item replace entity @p weapon.mainhand with jugcraft:power_bow");
            server.runCommand("item replace entity @p weapon.offhand with minecraft:arrow");
            context.runOnClient(c->{c.options.mainHand().set(HumanoidArm.RIGHT);c.options.setCameraType(CameraType.FIRST_PERSON);c.options.fov().set(70);});
            server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setMainArm(HumanoidArm.RIGHT));
            context.waitTicks(35);context.getInput().holdKey(o->o.keyUse);
            try {context.waitTicks(22);context.takeScreenshot("held_power_bow_drawn");}
            finally {context.getInput().releaseKey(o->o.keyUse);}
        }finally {
            context.runOnClient(c->{c.options.mainHand().set(original);c.options.fov().set(oldFov);c.options.setCameraType(CameraType.FIRST_PERSON);});
        }
    }
}
