package io.github.jimbozoomer.jugcraft.client.jimtafacts;

import io.github.jimbozoomer.jugcraft.client.FittedContainerScreen;
import io.github.jimbozoomer.jugcraft.client.MachineScreenThemes;
import io.github.jimbozoomer.jugcraft.client.ScreenKit;
import io.github.jimbozoomer.jugcraft.jimtafacts.computers.ComputerTerminal;
import io.github.jimbozoomer.jugcraft.jimtafacts.computers.WorkshopComputers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * A workshop computer, in the electric look (docs/features/new-screen-styles.md): the editable program and the live
 * output on two panes of glass, the actions as keys under them, and the device's storage beside the inventory in the
 * tray. The status lamp is green and the chip says RUNNING while a program runs. Laid out at 360 x 288 and scaled down
 * to fit a smaller window.
 */
public final class ComputerScreen extends FittedContainerScreen<ComputerTerminal.Menu> {
    private MultiLineEditBox editor;
    private MultiLineEditBox log;
    private String accepted = "", lastOutput = "";
    private boolean received;
    private final java.util.List<ScreenKit.TextButton> actions = new java.util.ArrayList<>();
    private ScreenKit.TextButton previous, next;
    private final MachineScreenThemes.Theme theme = MachineScreenThemes.screen("computer");

    public ComputerScreen(ComputerTerminal.Menu menu, Inventory inventory, Component title) { super(menu, inventory, title, 360, 288); }

    @Override protected void init() {
        super.init(); titleLabelY = 10000; inventoryLabelY = 10000;
        editor = new MultiLineEditBox.Builder().setX(leftPos + 16).setY(topPos + 26)
                .setShowBackground(false).setTextColor(theme.text()).setCursorColor(theme.progress())
                .setPlaceholder(Component.literal("print Hello, Jugcraft!\nstop"))
                .build(font, 155, 130, Component.literal("Native computer program"));
        editor.setCharacterLimit(WorkshopComputers.MAX_PROGRAM); editor.setValue(menu.program); addRenderableWidget(editor);
        log = new MultiLineEditBox.Builder().setX(leftPos + 192).setY(topPos + 26)
                .setShowBackground(false).setTextColor(theme.ok()).setCursorColor(theme.ok())
                .build(font, 155, 130, Component.literal("Computer output"));
        log.setCharacterLimit(8192); log.setValue(menu.output); addRenderableWidget(log);
        actions.clear();
        String[] labels = {"Save", "Run", "Stop", "To disk", "From disk", "Refuel", "Help"};
        for (int i = 0; i < labels.length; i++) {
            int action = i;
            actions.add(addRenderableWidget(new ScreenKit.TextButton(leftPos + 9 + i * 49, topPos + 169, 47, 16, theme, Component.literal(labels[i]), button -> {
                ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId, action, action == 0 ? editor.getValue() : ""));
                if (action == 0) accepted = editor.getValue();
            })));
        }
        previous = addRenderableWidget(new ScreenKit.TextButton(leftPos + 16, topPos + 169, 145, 16, theme, Component.literal("Previous page"),
                b -> ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId, 7, ""))));
        next = addRenderableWidget(new ScreenKit.TextButton(leftPos + 192, topPos + 169, 145, 16, theme, Component.literal("Next page"),
                b -> ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId, 8, ""))));
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        // Preserve unsaved edits when live output arrives; disk/load updates replace only an unmodified editor.
        if ((!received || editor.getValue().equals(accepted)) && !menu.program.equals(accepted)) {
            editor.setValue(menu.program); accepted = menu.program;
        }
        received = !menu.output.equals("Connecting to the workshop terminal…");
        if (!menu.output.equals(lastOutput)) { log.setValue(menu.output); lastOutput = menu.output; }
        editor.active = !menu.readOnly;
        actions.forEach(button -> button.visible = !menu.readOnly);
        previous.visible = next.visible = menu.readOnly;
        super.extractRenderState(g, x, y, delta);
    }

    @Override protected void background(GuiGraphicsExtractor g, int x, int y, float delta) {
        ScreenKit.frame(g, theme, leftPos, topPos, imageWidth, imageHeight, menu);
        // The program and its output, each on its glass.
        ScreenKit.pane(g, theme, leftPos + 8, topPos + 16, 168, 148);
        ScreenKit.pane(g, theme, leftPos + 184, topPos + 16, 168, 148);
        ScreenKit.title(g, font, theme, leftPos, topPos, title, menu.running ? theme.ok() : theme.dim(), imageWidth - 80);
        ScreenKit.chip(g, font, theme, leftPos + imageWidth - 5, topPos + 1, menu.running ? "RUNNING" : "READY", menu.running ? theme.ok() : theme.dim());
        g.text(font, menu.hardware ? "Device / disk / turtle storage" : "Portable program editor", leftPos + 11, topPos + 189, theme.label(), false);
        g.text(font, "Inventory", leftPos + 188, topPos + 189, theme.label(), false);
        // The device's own slots are in use only when it has storage; the inventory always.
        ScreenKit.slots(g, theme, leftPos, topPos, menu);
        g.text(font, menu.hardware ? menu.energy + " / 16000 JE" : "Save preserves this item's program", leftPos + 12, topPos + 251, theme.text(), false);
        if (menu.turtle) g.text(font, "Slots 17/18: left/right upgrades", leftPos + 12, topPos + 267, theme.dim(), false);
    }

    @Override protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {}
}
