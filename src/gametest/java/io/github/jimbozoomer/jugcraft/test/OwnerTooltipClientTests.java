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
        int[] window=context.computeOnClient(client->new int[]{client.getWindow().getScreenWidth(),client.getWindow().getScreenHeight()});
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            for (var item : new net.minecraft.world.item.Item[] {Items.IRON_AXE, Items.DIAMOND_CHESTPLATE, Items.EMERALD}) {
                context.setScreen(() -> new Preview(item.getDefaultInstance()));
                context.waitTicks(8);
                context.takeScreenshot("tooltip_" + item.getDescriptionId().replace('.', '_'));
            }
            context.getInput().resizeWindow(640,480);
            context.waitTicks(10);
            context.runOnClient(client->{
                var text=Component.translatable("tooltip.jugcraft.guns.thunderpipe").withStyle(net.minecraft.ChatFormatting.GRAY);
                var lines=io.github.jimbozoomer.jugcraft.client.OwnerTooltipFrames.boundedLines(client.font,java.util.List.of(text),320);
                if(lines.size()<2||lines.stream().anyMatch(line->client.font.width(line)>288))throw new AssertionError("Long gun description did not wrap");
                String joined=String.join(" ",lines.stream().map(Component::getString).toList()).replaceAll("\\s+"," ").trim();
                if(!joined.equals(text.getString().replaceAll("\\s+"," ").trim()))throw new AssertionError("Wrapping lost tooltip words");
                for(var line:lines)line.visit((style,part)->{
                    if(!part.isEmpty()&&!java.util.Objects.equals(style.getColor(),text.getStyle().getColor()))throw new AssertionError("Wrapping lost text color");
                    return java.util.Optional.empty();
                },net.minecraft.network.chat.Style.EMPTY);
                var state=new net.minecraft.client.renderer.state.gui.GuiRenderState();
                var graphics=new GuiGraphicsExtractor(client,state,100,100);
                graphics.setTooltipForNextFrame(client.font,new ItemStack(io.github.jimbozoomer.jugcraft.guns.JugcraftGuns.GUNS.get("thunderpipe")),100,100);
                graphics.extractDeferredElements(100,100,0);
                state.forEachText(line->{line.ensurePrepared();var bounds=line.bounds();
                    if(bounds!=null&&(bounds.left()<0||bounds.right()>graphics.guiWidth()))throw new AssertionError("Native item tooltip extends beyond the GUI");
                });
            });
            context.setScreen(()->new Preview(new ItemStack(io.github.jimbozoomer.jugcraft.guns.JugcraftGuns.GUNS.get("thunderpipe"))));
            context.waitTicks(8);
            context.takeScreenshot("tooltip_thunderpipe_wrapped_small");
            context.setScreen(() -> null);
        } finally {
            context.getInput().resizeWindow(window[0],window[1]);
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
