package io.github.jimbozoomer.jugcraft.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Exercise the deferred item-tooltip path used by merchant offers, without a world-rendered item entity. */
public final class OwnerTooltipClientTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            for (var item : new net.minecraft.world.item.Item[] {Items.IRON_AXE, Items.DIAMOND_CHESTPLATE, Items.EMERALD}) {
                context.setScreen(() -> new Preview(item.getDefaultInstance()));
                context.waitTicks(8);
                context.takeScreenshot("tooltip_" + item.getDescriptionId().replace('.', '_'));
            }
            context.setScreen(() -> null);
        }
    }
    private static final class Preview extends Screen {
        private final ItemStack stack;
        Preview(ItemStack stack) { super(Component.literal("Merchant tooltip regression")); this.stack=stack; }
        @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
            super.extractRenderState(g,x,y,delta);
            g.setTooltipForNextFrame(font,stack,width/2,height/2);
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
