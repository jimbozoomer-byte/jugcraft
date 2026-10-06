package local.peepo.client;

import io.github.jimbozoomer.jugcraft.client.MachineScreenThemes;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import local.peepo.WheelMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Uses the same bay, energy gauge, inventory and terminal as the Jugcraft generators. */
public final class WheelScreen extends AbstractContainerScreen<WheelMenu> {
    private final MachineScreenThemes.Theme theme = MachineScreenThemes.of(MachineKind.COAL_GENERATOR);

    public WheelScreen(WheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 268, 166);
    }

    @Override protected void init() {
        super.init();
        titleLabelX = (176 - font.width(title)) / 2;
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractBackground(graphics, mouseX, mouseY, delta);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        extractTooltip(graphics, mouseX, mouseY);
        if (mouseX >= leftPos + 9 && mouseX <= leftPos + 23 && mouseY >= topPos + 16 && mouseY <= topPos + 70)
            graphics.setTooltipForNextFrame(font, Component.literal(String.format("%,d / 32,000 JE", menu.energy())), mouseX, mouseY);
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = leftPos, y = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, theme.texture(), x, y, 0.0F, 0.0F, imageWidth, imageHeight, 512, 256);
        graphics.fill(x + 9, y + 16, x + 23, y + 70, theme.slotDark());
        graphics.fill(x + 10, y + 17, x + 22, y + 69, theme.slotFace());
        int fill = menu.energy() * 52 / 32000;
        graphics.fill(x + 10, y + 69 - fill, x + 22, y + 69, theme.energy());
        for (int tick = 1; tick < 4; tick++)
            graphics.fill(x + 19, y + 69 - tick * 13, x + 22, y + 70 - tick * 13, theme.slotLight());
        // The companion reserve replaces the generators' fuel/lava gauge.
        graphics.fill(x + 36, y + 48, x + 162, y + 57, theme.slotDark());
        graphics.fill(x + 37, y + 49, x + 161, y + 56, theme.slotFace());
        if (menu.occupantKind() != 0)
            graphics.fill(x + 37, y + 49, x + 37 + menu.reservePercent() * 124 / 100, y + 56, theme.progress());
    }

    private void line(GuiGraphicsExtractor graphics, String key, int x, int y, int color) {
        graphics.text(font, Component.translatable("container.peepo_companion.wheel." + key), x, y, color, false);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, theme.label(), false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, theme.label(), false);
        String worker = switch (menu.occupantKind()) { case 1 -> "Peepo"; case 2 -> "Jughead"; default -> ""; };
        if (worker.isEmpty()) line(graphics, "no_worker", 36, 24, theme.label());
        else graphics.text(font, worker, 36, 24, theme.label(), false);
        graphics.text(font, Component.translatable("container.peepo_companion.wheel.reserve", menu.reservePercent()), 36, 37, theme.label(), false);
        line(graphics, "max_rate", 36, 61, theme.label());

        line(graphics, "terminal", 185, 10, theme.dim());
        graphics.fill(185, 23, 259, 24, theme.dim());
        line(graphics, menu.energy() >= 32000 ? "full" : menu.running() ? "running" : "waiting", 185, 29, theme.text());
        line(graphics, "stored", 185, 43, theme.dim());
        graphics.text(font, String.format("%,d JE", menu.energy()), 185, 54, theme.text(), false);
        line(graphics, "push", 185, 68, theme.dim());
        graphics.text(font, menu.outputRate() + " JE/t", 185, 79, theme.text(), false);
        line(graphics, "outputs", 183, 108, theme.label());
        line(graphics, "sides", 183, 120, theme.label());
        line(graphics, "capacity", 183, 146, theme.label());
    }
}
