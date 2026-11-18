package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.server.TownDispatch;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TownDispatchScreen extends Screen {
    private static final Identifier SKYLINE = Jugcraft.id("textures/gui/town_dispatch.png");
    private static final int PAPER = 0xfff1e1bb, INK = 0xff362e25, SEAL = 0xff9b4136,
            TEAL = 0xff193f43, GOLD = 0xffd7b779, WHITE = 0xfffbf0d7;
    private static TownDispatch.View pending;
    private TownDispatch.View data;
    private final List<Line> lines = new ArrayList<>();
    private record Line(FormattedCharSequence text, boolean bullet) {}
    private int left, top, w, h, header, split, rows, page, revealTicks, awaitingTicks;
    private long refreshAt;
    private Button previous, next, parcel;
    private boolean claiming;

    public TownDispatchScreen(TownDispatch.View data) {
        super(Component.literal("The Town Dispatch"));
        accept(data);
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(TownDispatch.View.TYPE, (view, context) -> {
            if (context.client().gui.screen() instanceof TownDispatchScreen screen) screen.accept(view);
            else if (view.open()) pending = view;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pending != null && client.player != null && client.player.isAlive() && client.level != null
                    && client.gui.screen() == null && client.player.containerMenu == client.player.inventoryMenu) {
                var view = pending;
                pending = null;
                client.gui.setScreen(new TownDispatchScreen(view));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> pending = null);
    }

    private void accept(TownDispatch.View view) {
        if (claiming && view.parcel() >= 0 && data.parcel() < 0) revealTicks = 16;
        claiming = false;
        awaitingTicks = 0;
        data = view;
        refreshAt = Util.getMillis() + Math.clamp(view.refreshAfterSeconds(), 1L, 7L * 86_400) * 1000;
        if (parcel != null) updateButtons();
    }

    @Override protected void init() {
        w = Math.min(474, width - 12);
        h = Math.min(290, height - 12);
        left = (width - w) / 2;
        top = (height - h) / 2;
        header = Math.clamp(h / 4, 41, 68);
        split = w - Math.clamp(w / 3, 108, 145);
        rows = Math.max(1, (h - header - 52) / 11);
        lines.clear();
        for (String entry : data.notes()) {
            var wrapped = font.split(Component.literal(entry), split - 30);
            int used = lines.size() % rows;
            if (used != 0) {
                int remaining = rows - used;
                int gap = wrapped.size() <= rows && wrapped.size() + 1 > remaining ? remaining : 1;
                for (int i = 0; i < gap; i++) lines.add(new Line(FormattedCharSequence.EMPTY, false));
            }
            boolean first = true;
            for (var line : wrapped) {
                lines.add(new Line(line, first));
                first = false;
            }
        }
        page = Math.min(page, pages() - 1);
        var close = button(w - 24, 5, 18, 18, "×", b -> onClose());
        close.setTooltip(Tooltip.create(Component.literal("Close the dispatch (Esc). Reopen with /bulletin.")));
        previous = button(10, h - 23, 26, 17, "‹", b -> turn(-1));
        next = button(split - 36, h - 23, 26, 17, "›", b -> turn(1));
        previous.setTooltip(Tooltip.create(Component.literal("Previous page")));
        next.setTooltip(Tooltip.create(Component.literal("Next page")));
        parcel = button(split + 7, h - 27, w - split - 14, 20, "", b -> {
            awaitingTicks = 100;
            claiming = true;
            updateButtons();
            ClientPlayNetworking.send(new TownDispatch.Request(true));
        });
        var odds = Component.literal("One free draw per UTC day.");
        for (var prize : TownDispatch.PRIZES) {
            odds.append("\n" + prize.weight() + "%   " + prize.count() + " × ").append(prize.stack().getHoverName());
        }
        parcel.setTooltip(Tooltip.create(odds));
        updateButtons();
    }

    private Button button(int x, int y, int width, int height, String text, Button.OnPress action) {
        return addRenderableWidget(new PetInventoryScreen.StableButton(left + x, top + y, width, height,
                Component.literal(text), action, false));
    }

    private int pages() { return Math.max(1, (lines.size() + rows - 1) / rows); }
    private void turn(int direction) {
        page = Math.clamp(page + direction, 0, pages() - 1);
        updateButtons();
    }

    private void updateButtons() {
        previous.active = page > 0;
        next.active = page + 1 < pages();
        boolean spectator = minecraft.player != null && minecraft.player.isSpectator();
        parcel.active = data.parcel() < 0 && awaitingTicks == 0 && !spectator;
        parcel.setMessage(Component.literal(spectator ? "Spectating" : revealTicks > 0 ? "Unwrapping…"
                : data.parcel() >= 0 ? "Collected" : claiming ? "Opening…" : "Open parcel"));
    }

    @Override public void tick() {
        if (revealTicks > 0 && --revealTicks == 0) updateButtons();
        if (awaitingTicks > 0 && --awaitingTicks == 0) {
            claiming = false;
            updateButtons();
        }
        if (Util.getMillis() >= refreshAt && !claiming) {
            refreshAt = Util.getMillis() + 30_000;
            ClientPlayNetworking.send(new TownDispatch.Request(false));
        }
    }

    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (dy != 0 && x >= left && x < left + split && y >= top + header && y < top + h) {
            turn(dy < 0 ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(x, y, dx, dy);
    }
    @Override public boolean isPauseScreen() { return false; }

    @Override public Component getNarrationMessage() {
        var message = new StringBuilder("The Town Dispatch. ").append(data.motd()).append(". ");
        for (int i = page * rows; i < Math.min(lines.size(), (page + 1) * rows); i++) {
            lines.get(i).text().accept((index, style, codePoint) -> {
                message.appendCodePoint(codePoint);
                return true;
            });
            message.append(" ");
        }
        return Component.literal(message.append(parcelText()).toString());
    }

    private String parcelText() {
        if (data.parcel() == TownDispatch.INVENTORY_FULL) return "Make room in your inventory and try again.";
        if (revealTicks > 0) return "Breaking the seal…";
        if (data.parcel() >= 0 && data.parcel() < TownDispatch.PRIZES.size()) {
            var prize = TownDispatch.PRIZES.get(data.parcel());
            return prize.count() + " × " + prize.stack().getHoverName().getString();
        }
        return "A small gift from town. Draw one each day.";
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x = left, y = top, body = y + header, edge = x + split, mid = (edge + x + w) / 2;
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x990b100f);
        g.fill(x, y, x + w, y + h, PAPER);
        g.fill(x, y, x + w, body, TEAL);
        int photoWidth = w / 2;
        g.blit(RenderPipelines.GUI_TEXTURED, SKYLINE, x + w - photoWidth, y, 0, 84,
                photoWidth, header, 854, 262, 854, 480);
        for (int i = 0; i < 36; i++)
            g.fill(x + w - photoWidth + i, y, x + w - photoWidth + i + 1, body,
                    ((255 - i * 7) << 24) | (TEAL & 0xffffff));
        g.fill(x, body - 2, x + w, body, GOLD);
        g.pose().pushMatrix();
        g.pose().translate(x + 10, y + 7);
        g.pose().scale(1.5f, 1.5f);
        g.text(font, "The Town Dispatch", 0, 0, WHITE, false);
        g.pose().popMatrix();
        var motd = font.split(Component.literal(data.motd()), w / 2 - 14);
        for (int i = 0; i < Math.min((header - 25) / 10, motd.size()); i++)
            g.text(font, motd.get(i), x + 10, y + 24 + i * 10, WHITE, false);
        if (motd.size() > (header - 25) / 10 && mx >= x && mx < x + w / 2 && my >= y && my < body)
            g.setTooltipForNextFrame(font, Component.literal(data.motd()), mx, my);

        g.text(font, "Update notes", x + 12, body + 8, SEAL, false);
        g.fill(x + 11, body + 20, edge - 10, body + 21, 0xffb49c72);
        for (int row = 0; row < rows && page * rows + row < lines.size(); row++) {
            var line = lines.get(page * rows + row);
            int lineY = body + 26 + row * 11;
            if (line.bullet()) BookArt.star(g, x + 12, lineY + 4, 2, SEAL);
            g.text(font, line.text(), x + 20, lineY, INK, false);
        }
        String pageNumber = (page + 1) + " / " + pages();
        g.text(font, pageNumber, x + (split - font.width(pageNumber)) / 2, y + h - 19, INK, false);
        g.fill(edge, body, x + w, y + h, TEAL);
        g.fill(edge + 2, body + 2, edge + 3, y + h - 2, GOLD);
        g.centeredText(font, "Daily parcel", mid, body + 9, WHITE);
        int iconY = body + Math.clamp((h - header) / 4, 22, 48);
        g.fill(mid - 15, iconY - 5, mid + 15, iconY + 23, GOLD);
        g.fill(mid - 14, iconY - 4, mid + 14, iconY + 22, 0xff734538);
        BookArt.star(g, mid - 24, iconY + 9, 2, GOLD);
        BookArt.star(g, mid + 24, iconY + 9, 2, GOLD);
        ItemStack icon = new ItemStack(Items.CHEST);
        if (revealTicks > 0) icon = TownDispatch.PRIZES.get((16 - revealTicks) / 3 % TownDispatch.PRIZES.size()).stack();
        else if (data.parcel() >= 0 && data.parcel() < TownDispatch.PRIZES.size()) icon = TownDispatch.PRIZES.get(data.parcel()).stack();
        g.item(icon, mid - 8, iconY + 1);
        int lineY = iconY + 30;
        for (var line : font.split(Component.literal(parcelText()), w - split - 14)) {
            g.text(font, line, mid - font.width(line) / 2, lineY, WHITE, false);
            lineY += 10;
        }
        if (lineY + 12 < y + h - 29)
            g.centeredText(font, "Resets 00:00 UTC", mid, y + h - 41, GOLD);
        g.outline(x, y, w, h, GOLD);
        super.extractRenderState(g, mx, my, delta);
    }
}

