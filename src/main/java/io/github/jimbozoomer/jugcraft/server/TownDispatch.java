package io.github.jimbozoomer.jugcraft.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** News of the installed server build and one small, free parcel per player and UTC date. */
public final class TownDispatch {
    private TownDispatch() {}

    public record Prize(Item item, int count, int weight) {
        public ItemStack stack() { return new ItemStack(item, count); }
    }

    // Append new entries so the receipts in existing player saves continue to identify the same item.
    public static final List<Prize> PRIZES = List.of(
            new Prize(Items.TORCH, 6, 30), new Prize(Items.BREAD, 2, 25),
            new Prize(Items.APPLE, 2, 20), new Prize(Items.COOKIE, 4, 10),
            new Prize(Items.LANTERN, 1, 10), new Prize(Items.FLOWER_POT, 1, 5));
    private static final int TOTAL_WEIGHT = PRIZES.stream().mapToInt(Prize::weight).sum();

    record Receipt(long day, int prize, boolean delivered) {
        static final Codec<Receipt> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("utc_day").forGetter(Receipt::day),
                Codec.intRange(0, PRIZES.size() - 1).fieldOf("prize").forGetter(Receipt::prize),
                Codec.BOOL.fieldOf("delivered").forGetter(Receipt::delivered)).apply(i, Receipt::new));
    }
    private static AttachmentType<Receipt> receiptType;
    private static List<String> news = List.of();
    public static final int SEALED = -1, INVENTORY_FULL = -2;

    public record View(boolean open, String motd, List<String> notes, int parcel, long refreshAfterSeconds)
            implements CustomPacketPayload {
        public static final Type<View> TYPE = new Type<>(Jugcraft.id("town_dispatch"));
        public static final StreamCodec<RegistryFriendlyByteBuf, View> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, View::open, ByteBufCodecs.stringUtf8(512), View::motd,
                ByteBufCodecs.stringUtf8(1024).apply(ByteBufCodecs.list(64)), View::notes,
                ByteBufCodecs.VAR_INT, View::parcel, ByteBufCodecs.VAR_LONG, View::refreshAfterSeconds, View::new);
        @Override public Type<View> type() { return TYPE; }
    }

    public record Request(boolean claim) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(Jugcraft.id("town_dispatch_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, Request::claim, Request::new);
        @Override public Type<Request> type() { return TYPE; }
    }

    public static void register() {
        receiptType = AttachmentRegistry.<Receipt>builder().persistent(Saved.versioned("town_dispatch_receipt", Receipt.CODEC))
                .copyOnDeath().buildAndRegister(Jugcraft.id("town_dispatch_receipt"));
        news = loadNews();
        PayloadTypeRegistry.clientboundPlay().register(View.TYPE, View.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(Request.TYPE, Request.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (server.isDedicatedServer()) send(handler.getPlayer(), true, false);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) ->
                dispatcher.register(Commands.literal("bulletin").executes(context -> {
                    send(context.getSource().getPlayerOrException(), true, false);
                    return 1;
                })));
        ServerPlayNetworking.registerGlobalReceiver(Request.TYPE, (request, context) -> {
            var player = context.player();
            if (RateGate.allow(player, request.claim() ? "parcel_claim" : "parcel_refresh", 5))
                send(player, false, request.claim() && player.isAlive() && !player.isSpectator());
        });
    }

    private static List<String> loadNews() {
        var stream = TownDispatch.class.getResourceAsStream("/jugcraft/bulletin/RELEASE-NOTES.md");
        if (stream != null) {
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                var entries = new ArrayList<String>();
                boolean publicSection = false;
                for (String line; (line = reader.readLine()) != null;) {
                    line = line.strip();
                    if (line.equals("<!-- /player-notes -->") && publicSection) return List.copyOf(entries);
                    if (line.equals("<!-- player-notes -->")) publicSection = true;
                    if (publicSection && line.startsWith("- ") && entries.size() < 64)
                        entries.add(line.substring(2, Math.min(line.length(), 1026)));
                }
            } catch (IOException e) {
                Jugcraft.LOGGER.warn("Could not read the Town Dispatch release notes", e);
            }
        }
        Jugcraft.LOGGER.warn("Town Dispatch has no marked player notes in the bundled release notes");
        return List.of("There are no new notices in this edition.");
    }

    private static void send(ServerPlayer player, boolean open, boolean claim) {
        if (!ServerPlayNetworking.canSend(player, View.TYPE)) return;
        var now = java.time.Instant.now();
        long day = LocalDate.ofInstant(now, ZoneOffset.UTC).toEpochDay();
        int parcel = parcel(player, day, claim);
        var receipt = player.getAttached(receiptType);
        long nextDay = receipt == null ? day + 1 : Math.max(day, receipt.day()) + 1;
        String motd = player.level().getServer().getMotd();
        if (motd == null || motd.isBlank()) motd = "Welcome to Jugcraft";
        ServerPlayNetworking.send(player, new View(open, motd.substring(0, Math.min(512, motd.length())), news,
                parcel, nextDay * 86_400L - now.getEpochSecond()));
    }

    static int parcel(ServerPlayer player, long day, boolean claim) {
        var receipt = player.getAttached(receiptType);
        if (receipt == null || day > receipt.day()) {
            if (!claim) return SEALED;
            int roll = player.getRandom().nextInt(TOTAL_WEIGHT);
            int index = 0;
            while ((roll -= PRIZES.get(index).weight()) >= 0) index++;
            receipt = new Receipt(day, index, false);
            player.setAttached(receiptType, receipt);
        }
        if (receipt.delivered()) return receipt.prize();
        if (!claim) return SEALED;

        var stack = PRIZES.get(receipt.prize()).stack();
        var inventory = player.getInventory();
        int room = 0;
        for (var slot : inventory.getNonEquipmentItems()) {
            if (slot.isEmpty()) room += stack.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(slot, stack)) room += Math.max(0, slot.getMaxStackSize() - slot.getCount());
            if (room >= stack.getCount()) break;
        }
        if (room < stack.getCount()) return INVENTORY_FULL;
        inventory.add(stack);
        player.setAttached(receiptType, new Receipt(receipt.day(), receipt.prize(), true));
        player.containerMenu.broadcastChanges();
        return receipt.prize();
    }
}

