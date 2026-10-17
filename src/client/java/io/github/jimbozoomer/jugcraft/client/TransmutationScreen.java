package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.TransmutationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** A compact illustrated spread: source -> personal ledger -> sampled material, with real vanilla inventory slots. */
public final class TransmutationScreen extends AbstractContainerScreen<TransmutationMenu> {
    private Button dissolve, form;
    public TransmutationScreen(TransmutationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 310, 232);
    }
    private static Component ui(String key, Object... args) { return Component.translatable("screen.jugcraft.alchemy." + key, args); }
    @Override protected void init() {
        super.init();
        dissolve = addRenderableWidget(new BookArt.Tab(leftPos + 22, topPos + 106, 118, 20, ui("dissolve"),
                ui("dissolve_help"), "", false, false, b -> press(0)));
        form = addRenderableWidget(new BookArt.Tab(leftPos + 170, topPos + 106, 118, 20, ui("form"),
                ui("form_help"), "", false, false, b -> press(1)));
        refresh();
    }
    private void press(int id) {
        if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
    private void refresh() {
        dissolve.active = menu.canDissolve(); form.active = menu.canForm();
        dissolve.setMessage(ui(menu.confirming() ? "confirm" : "dissolve"));
        dissolve.setTooltip(Tooltip.create(ui(menu.canDissolve() ? "dissolve_help" : "input_help")));
        form.setTooltip(Tooltip.create(ui("status_" + menu.formStatus())));
    }
    @Override protected void containerTick() { super.containerTick(); refresh(); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        extractBackground(g, mx, my, delta);
        super.extractRenderState(g, mx, my, delta);
        extractTooltip(g, mx, my);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x = leftPos, y = topPos;
        g.fill(0, 0, width, height, 0x900B1019);
        BookArt.book(g, x, y, imageWidth, imageHeight);
        BookArt.rule(g, x + 21, y + 27, 120); BookArt.rule(g, x + 170, y + 27, 117);
        // The stone and small orbit give the page a living instrument without obscuring its slots.
        BookArt.circle(g, x + 64, y + 71, 20, 0x70986B2E);
        double phase = (minecraft.level == null ? 0 : minecraft.level.getGameTime()) * 0.025;
        BookArt.star(g, x + 64 + (int)(Math.cos(phase) * 22), y + 71 + (int)(Math.sin(phase) * 22), 1, BookArt.GOLD);
        BookArt.line(g, x + 221, y + 71, x + 250, y + 71, BookArt.GOLD);
        BookArt.line(g, x + 246, y + 68, x + 250, y + 71, BookArt.GOLD);
        BookArt.line(g, x + 246, y + 74, x + 250, y + 71, BookArt.GOLD);
        for (var slot : menu.slots) {
            int sx = x + slot.x, sy = y + slot.y;
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF725D42);
            g.fill(sx, sy, sx + 16, sy + 16, 0xFFC3AE88);
            g.fill(sx, sy + 15, sx + 16, sy + 16, 0xFFE9D7B5);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        text(g, ui("title"), 22, 14, 121);
        text(g, ui("balance", menu.balance()), 170, 14, 117);
        text(g, ui("input"), 28, 35, 110);
        text(g, ui("sample_kept"), 170, 35, 117);
        text(g, ui("sample"), 181, 49, 65); text(g, ui("output"), 249, 49, 45);
        text(g, ui("gain", menu.gain()), 25, 93, 115);
        text(g, ui("cost", menu.cost()), 171, 93, 115);
        text(g, ui("inventory"), 74, 129, 162);
    }
    private void text(GuiGraphicsExtractor g, Component text, int x, int y, int width) {
        g.text(font, BookArt.fit(font, text.getString(), width), x, y, BookArt.INK, false);
    }
}
