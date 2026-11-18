package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Contents live in one server ledger. A hand-delivered wrapper and its counter entry redeem the same parcel. */
public final class WinterGiftPost extends SavedData {
    record Parcel(UUID id, UUID from, UUID to, String sender, ItemStack contents) {
        static final Codec<Parcel> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(Parcel::id),
            UUIDUtil.STRING_CODEC.fieldOf("from").forGetter(Parcel::from),
            UUIDUtil.STRING_CODEC.fieldOf("to").forGetter(Parcel::to),
            Codec.STRING.fieldOf("sender").forGetter(Parcel::sender),
            ItemStack.CODEC.fieldOf("contents").forGetter(Parcel::contents)).apply(i, Parcel::new));
    }

    private final Map<UUID,String> names = new HashMap<>();
    private final Map<String,Integer> nameCounts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<UUID,Parcel> parcels = new LinkedHashMap<>();
    static final Codec<WinterGiftPost> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING).fieldOf("address_book").forGetter(p -> p.names),
        Parcel.CODEC.listOf().fieldOf("parcels").forGetter((WinterGiftPost p) -> List.copyOf(p.parcels.values()))
    ).apply(i, WinterGiftPost::new));
    private static final SavedDataType<WinterGiftPost> TYPE = new SavedDataType<>(Jugcraft.id("winter_gift_post"),
        () -> new WinterGiftPost(Map.of(),List.of()), CODEC, null);

    private WinterGiftPost(Map<UUID,String> addresses, List<Parcel> saved) {
        names.putAll(addresses);
        addresses.values().forEach(n -> nameCounts.merge(n,1,Integer::sum));
        for (var parcel : saved) parcels.put(parcel.id(),parcel);
    }
    static WinterGiftPost get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
    static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server) -> {
            var player = handler.getPlayer();
            var post = get(server);
            post.remember(player);
            int waiting = post.inbox(player.getUUID()).size();
            if (waiting > 0) player.sendSystemMessage(Component.translatable("message.jugcraft.market.waiting",waiting));
        });
    }
    void remember(ServerPlayer player) {
        String name = player.getGameProfile().name();
        String old = names.put(player.getUUID(),name);
        if (name.equals(old)) return;
        if (old != null) nameCounts.computeIfPresent(old,(key,n) -> n==1 ? null : n-1);
        nameCounts.merge(name,1,Integer::sum);
        setDirty();
    }
    List<UUID> recipients(UUID sender) {
        return names.keySet().stream().filter(id -> !id.equals(sender))
            .sorted(Comparator.<UUID,String>comparing(names::get,String.CASE_INSENSITIVE_ORDER).thenComparing(id -> id)).toList();
    }
    String name(UUID id) {
        String name = names.get(id);
        if (nameCounts.getOrDefault(name,0)>1)
            return name + " · " + id.toString().substring(0,8);
        return name;
    }
    List<Parcel> inbox(UUID recipient) {
        return parcels.values().stream().filter(p -> p.to().equals(recipient)).toList();
    }
    boolean wrap(ServerPlayer player, UUID recipient, ItemStack expected, boolean carry) {
        ItemStack held = player.getMainHandItem();
        if (!player.isAlive() || player.isSpectator() || recipient.equals(player.getUUID()) || !names.containsKey(recipient)
                || held.isEmpty() || held.is(WinterMarket.WRAPPED) || !ItemStack.matches(held,expected)) return false;
        var waiting = inbox(recipient);
        if (waiting.size() >= 32 || waiting.stream().filter(p -> p.from().equals(player.getUUID())).count() >= 8) {
            player.sendOverlayMessage(Component.translatable("message.jugcraft.market.full"));
            return false;
        }
        var parcel = new Parcel(UUID.randomUUID(),player.getUUID(),recipient,player.getGameProfile().name(),held.copy());
        var wrapper = ItemStack.EMPTY;
        if (carry) {
            wrapper = new ItemStack(WinterMarket.WRAPPED);
            CustomData.update(DataComponents.CUSTOM_DATA,wrapper,t -> t.putString("parcel",parcel.id().toString()));
            wrapper.set(DataComponents.CUSTOM_NAME,Component.translatable("item.jugcraft.market.address",name(recipient)));
            wrapper.set(DataComponents.LORE,new ItemLore(List.of(Component.translatable("item.jugcraft.market.sender",parcel.sender()))));
        }
        parcels.put(parcel.id(),parcel);
        player.setItemInHand(InteractionHand.MAIN_HAND,wrapper);
        setDirty();
        player.sendSystemMessage(Component.translatable(carry ? "message.jugcraft.market.carried" : "message.jugcraft.market.sent",name(recipient)));
        var other = player.level().getServer().getPlayerList().getPlayer(recipient);
        if (other != null) other.sendSystemMessage(Component.translatable("message.jugcraft.market.arrival",parcel.sender()));
        return true;
    }

    static UUID parcelId(ItemStack stack) {
        if (!stack.is(WinterMarket.WRAPPED)) return null;
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        try { return UUID.fromString(tag.getStringOr("parcel","")); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    static boolean fitsInventory(ServerPlayer player,ItemStack contents) {
        int room = 0;
        for (var slot : player.getInventory().getNonEquipmentItems()) {
            if (slot.isEmpty()) room += contents.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(slot,contents)) room += Math.max(0, slot.getMaxStackSize()-slot.getCount());
            if (room >= contents.getCount()) return true;
        }
        return false;
    }

    boolean unwrap(ServerPlayer player, UUID id, InteractionHand hand) {
        var parcel = parcels.get(id);
        if (!player.isAlive() || player.isSpectator() || parcel == null || !player.getUUID().equals(parcel.to())) {
            player.sendOverlayMessage(Component.translatable("message.jugcraft.market.sealed"));
            return false;
        }
        var contents = parcel.contents().copy();
        var held = player.getItemInHand(hand);
        if (held.isEmpty() || id.equals(parcelId(held))) {
            player.setItemInHand(hand,contents);
        } else {
            if (!fitsInventory(player,contents)) {
                player.sendOverlayMessage(Component.translatable("message.jugcraft.market.make_room"));
                return false;
            }
            player.getInventory().add(contents);
        }
        parcels.remove(id);
        setDirty();
        player.sendSystemMessage(Component.translatable("message.jugcraft.market.opened",parcel.sender(),parcel.contents().getHoverName()));
        return true;
    }
}
