package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.OwnerTooltipFrames;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/** Exercises {@link OwnerTooltipFrames} through the deferred callback that crashed on an unassigned ItemEntity ID. */
public final class OwnerTooltipClientGameTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            var server = world.getServer();
            server.runCommand("gamemode survival @a");
            server.runCommand("gamerule minecraft:spawn_mobs false");

            // Execute the same deferred callback as a normal screen, making the
            // original crash fail here even if the test cursor misses a slot.
            context.runOnClient(client -> {
                for (var item : new net.minecraft.world.item.Item[] {Items.IRON_PICKAXE, Items.BOW, Items.EMERALD}) {
                    var renderState = new GuiRenderState();
                    var graphics = new GuiGraphicsExtractor(client, renderState, 100, 100);
                    var stack = new ItemStack(item);
                    if (stack.isDamageableItem()) stack.setDamageValue(1);
                    graphics.setTooltipForNextFrame(client.font, stack, 100, 100);
                    graphics.extractDeferredElements(100, 100, 0);
                    int[] previews = {0};
                    renderState.forEachPictureInPicture(state -> {
                        if (state instanceof net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState preview
                                && preview.renderState() instanceof net.minecraft.client.renderer.entity.state.ItemEntityRenderState dropped
                                && !dropped.item.isEmpty()) previews[0]++;
                    });
                    if (previews[0] != (stack.isDamageableItem() ? 1 : 0)) {
                        throw new AssertionError("Wrong number of item previews for " + item + ": " + previews[0]);
                    }
                    if (stack.getCount() != 1 || stack.getDamageValue() != (stack.isDamageableItem() ? 1 : 0)) {
                        throw new AssertionError("Tooltip changed the original stack");
                    }
                }
            });

            server.runCommand("item replace entity @p hotbar.0 with minecraft:iron_pickaxe");
            context.waitTicks(5);
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            hover(context, 176, 16, 150);
            context.takeScreenshot("jugcraft_tooltip_inventory_preview");
            context.runOnClient(client -> client.player.closeContainer());

            server.runOnServer(minecraft -> {
                var player = minecraft.getPlayerList().getPlayers().getFirst();
                var trader = EntityTypes.WANDERING_TRADER.create(player.level(), EntitySpawnReason.COMMAND);
                trader.setNoAi(true);
                trader.snapTo(player.getX() + 2, player.getY(), player.getZ(), 0, 0);
                player.level().addFreshEntity(trader);
                var offers = trader.getOffers();
                offers.clear();
                offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 1), new ItemStack(Items.IRON_PICKAXE), 10, 0, 0));
                trader.setTradingPlayer(player);
                trader.openTradingScreen(player, Component.literal("Tooltip test trader"), 1);
            });
            context.waitForScreen(MerchantScreen.class);
            context.waitFor(client -> client.gui.screen() instanceof MerchantScreen screen
                    && screen.getMenu().getOffers().size() == 1, 100);
            // First trade's output, followed by its nondamageable emerald cost.
            hover(context, 276, 78, 28);
            context.takeScreenshot("jugcraft_tooltip_wandering_trader_preview");
            hover(context, 276, 13, 28);
            context.takeScreenshot("jugcraft_tooltip_wandering_trader_cost");
            context.runOnClient(client -> client.player.closeContainer());
            Jugcraft.LOGGER.info("[tooltip regression] Deferred previews, inventory and wandering trader tooltips passed");
        }
    }

    private static void hover(ClientGameTestContext context, int width, int x, int y) {
        double[] cursor = context.computeOnClient(client -> {
            var screen = client.gui.screen();
            return new double[] {
                ((screen.width - width) / 2 + x) / MouseHandler.getScaledXPos(client.getWindow(), 1.0),
                ((screen.height - 166) / 2 + y) / MouseHandler.getScaledYPos(client.getWindow(), 1.0)
            };
        });
        context.getInput().setCursorPos(cursor[0], cursor[1]);
        context.waitTicks(5);
    }
}
