package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.assay.TransmutationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Stone tablet with real item sockets; balances and transactions come from the server menu. */
public final class TransmutationScreen extends AbstractContainerScreen<TransmutationMenu> {
    private static final Identifier ART = Jugcraft.id("textures/gui/alchemy/transmutation.png");
    private static final int INK = 0xFF30323B, SILVER = 0xFFD5D5DE, VIOLET = 0xFFBD8FE3;
    private ActionButton dissolve, form;
    public TransmutationScreen(TransmutationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 310, 238);
    }
    private static Component ui(String key, Object... args) { return Component.translatable("screen.jugcraft.alchemy." + key, args); }
    @Override protected void init() {
        super.init();
        dissolve = addRenderableWidget(new ActionButton(leftPos + 24, topPos + 114, 116, ui("dissolve"), b -> press(0)));
        form = addRenderableWidget(new ActionButton(leftPos + 170, topPos + 114, 116, ui("form"), b -> press(1)));
        refresh();
    }
    private void press(int id) {
        if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
    private void refresh() {
        dissolve.active = menu.canDissolve(); form.active = menu.canForm();
        dissolve.confirming = menu.confirming();
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
        g.fill(0, 0, width, height, 0xA00B0B14);
        // Chamfered stone, raised silver rim and inset plates, without a page or spine.
        slab(g, x + 4, y + 5, imageWidth, imageHeight, 0x70000000);
        slab(g, x, y, imageWidth, imageHeight, 0xFF20222B);
        slab(g, x + 2, y + 2, imageWidth - 4, imageHeight - 4, 0xFF6D6F7B);
        slab(g, x + 4, y + 4, imageWidth - 8, imageHeight - 8, 0xFF353640);
        g.fill(x + 12, y + 7, x + 298, y + 8, 0xFFA0A0AF);
        g.fill(x + 14, y + 36, x + 296, y + 111, 0xFF96969E);
        g.fill(x + 15, y + 37, x + 295, y + 110, 0xFFC6C6C6);
        g.fill(x + 16, y + 37, x + 294, y + 38, 0xFFE4E4E8);
        // Unchanged owner art. Sample just the sigils, omitting its legacy controls.
        g.enableScissor(x + 15, y + 38, x + 295, y + 98);
        g.blit(RenderPipelines.GUI_TEXTURED, ART, x + 26, y + 42, 12, 20, 80, 76, 80, 76, 256, 256);
        g.blit(RenderPipelines.GUI_TEXTURED, ART, x + 167, y + 40, 107, 8, 80, 76, 115, 96, 256, 256);
        g.disableScissor();
        g.fill(x + 18, y + 98, x + 292, y + 110, 0xFFC6C6C6);
        BookArt.icon(g, "jugcraft:philosophers_stone", x + 16, y + 15);
        g.fill(x + 170, y + 11, x + 294, y + 32, 0xFF1D1C29);
        g.fill(x + 171, y + 11, x + 293, y + 12, 0xFF7E688F);
        BookArt.star(g, x + 181, y + 22, 4, VIOLET);
        arrow(g, x + 91, x + 146, y + 78, 0xFF80668D);
        BookArt.star(g, x + 150, y + 78, 5, 0xFF866299);
        BookArt.star(g, x + 150, y + 78, 2, 0xFFE5C4F5);
        arrow(g, x + 235, x + 265, y + 78, 0xFF80668D);
        double phase = (minecraft.level == null ? 0 : minecraft.level.getGameTime() + delta) * .045;
        if (menu.canDissolve()) {
            BookArt.star(g, x + 64 + (int)(Math.cos(phase) * 29), y + 78 + (int)(Math.sin(phase) * 23), 1, 0xFFFAE3FF);
            int flow = (int)(phase * 10) % 44;
            g.fill(x + 96 + flow, y + 77, x + 99 + flow, y + 79, 0xFFDBB3EF);
        }
        if (menu.canForm()) BookArt.star(g, x + 207 + (int)(Math.cos(-phase) * 29), y + 78 + (int)(Math.sin(-phase) * 23), 1, 0xFFFAE3FF);
        // Inventory is a recessed tray with standard vanilla slot spacing.
        g.fill(x + 69, y + 147, x + 241, y + 228, 0xFF20222A);
        g.fill(x + 70, y + 148, x + 240, y + 227, 0xFF757783);
        for (var slot : menu.slots) {
            int sx = x + slot.x, sy = y + slot.y;
            if (slot.index < 3) {
                g.fill(sx - 3, sy - 3, sx + 19, sy + 19, 0xFF77717D);
                g.fill(sx - 2, sy - 2, sx + 18, sy + 18, 0xFFF0E1F5);
            }
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF292A34);
            g.fill(sx, sy, sx + 16, sy + 16, 0xFF575462);
            g.fill(sx, sy + 16, sx + 17, sy + 17, 0xFFD0CBD8);
            g.fill(sx + 16, sy, sx + 17, sy + 16, 0xFFAAA5B4);
        }
        for (int corner : new int[]{12, 297}) {
            BookArt.star(g, x + corner, y + 14, 2, 0xFF92939F);
            BookArt.star(g, x + corner, y + 223, 2, 0xFF92939F);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        text(g, ui("title"), 37, 19, 128, SILVER);
        text(g, ui("balance", menu.balance()), 191, 18, 98, 0xFFE9D2F6);
        text(g, ui("input"), 29, 43, 103, INK);
        text(g, ui("sample_kept"), 165, 43, 83, INK);
        text(g, ui("output"), 257, 51, 36, INK);
        text(g, ui("gain", menu.gain()), 28, 100, 114, INK);
        text(g, ui("cost", menu.cost()), 171, 100, 120, INK);
        text(g, ui("inventory"), 74, 137, 162, SILVER);
    }
    private void text(GuiGraphicsExtractor g, Component text, int x, int y, int width, int color) {
        g.text(font, BookArt.fit(font, text.getString(), width), x, y, color, false);
    }
    private static void arrow(GuiGraphicsExtractor g, int x, int end, int y, int color) {
        g.fill(x, y, end, y + 1, color);
        BookArt.line(g, end - 3, y - 3, end, y, color);
        BookArt.line(g, end - 3, y + 3, end, y, color);
    }
    static void slab(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x + 6, y, x + w - 6, y + h, color);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, color);
        g.fill(x, y + 6, x + w, y + h - 6, color);
    }
    /** Native keyboard activation and narration, with a tactile inset metal key. */
    static final class ActionButton extends Button {
        boolean confirming;
        ActionButton(int x, int y, int w, Component title, OnPress action) {
            super(x, y, w, 20, title, action, DEFAULT_NARRATION);
        }
        @Override protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
            int x = getX(), y = getY(), w = getWidth();
            boolean lit = active && (isHovered() || isFocused());
            int accent = confirming ? 0xFFE5B579 : VIOLET;
            g.fill(x, y, x + w, y + 20, 0xFF171720);
            g.fill(x + 1, y + 1, x + w - 1, y + 18, lit ? 0xFF61526D : 0xFF43404E);
            g.fill(x + 2, y + 1, x + w - 2, y + 2, active ? accent : 0xFF66616F);
            g.fill(x + 2, y + 17, x + w - 2, y + 18, 0xFF27242F);
            if (isFocused()) g.outline(x, y, w, 20, accent);
            var font = Minecraft.getInstance().font;
            String label = BookArt.fit(font, getMessage().getString(), w - 10);
            g.text(font, label, x + (w - font.width(label)) / 2, y + 6, active ? 0xFFF0E6F5 : 0xFFAAA2B2, false);
        }
    }
}
