package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;

/** Personal daily delivery quotas, with a party contribution ledger. All mutations run on the server thread. */
public final class TownCommissions {
    private TownCommissions() {}
    // Elapsed world time, not the adjustable sun clock: sleeping and /time set cannot reset rewards.
    // One running-server hour; modest civic income at the owner's 1 gold ingot = 1 Jug peg.
    public static final long PERIOD = 72_000L;
    public record Order(String id, String patron, String description, String item, int count, int reward) {
        public Item itemType() { return BuiltInRegistries.ITEM.getValue(Identifier.parse(item)); }
    }
    public static final List<Order> ORDERS = List.of(
        new Order("bakery", "The Bakery", "Fresh wheat for tomorrow's bread.", "minecraft:wheat", 32, 1),
        new Order("guards", "City Watch", "Arrows to restock the city watch.", "minecraft:arrow", 32, 1),
        new Order("styx", "Styx's Conservatory", "Starflowers for the observatory greenhouse.", "jugcraft:stolas_starflower", 8, 1),
        new Order("workshop", "Public Workshop", "Copper wire for the workshop's repairs.", "jugcraft:copper_wire", 16, 2));
    public record Delivery(long cycle, int count, boolean paid, String party) {
        public static final Codec<Delivery> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.fieldOf("cycle").forGetter(Delivery::cycle), Codec.intRange(0, 32767).fieldOf("count").forGetter(Delivery::count),
            Codec.BOOL.fieldOf("paid").forGetter(Delivery::paid), Codec.STRING.optionalFieldOf("party", "").forGetter(Delivery::party)
        ).apply(i, Delivery::new));
    }
    public static long cycle(ServerPlayer player) { return Math.max(0, player.level().getServer().overworld().getGameTime() / PERIOD); }
    public static String key(UUID player, Order order) { return player + "/" + order.id(); }
    public static Delivery progress(Jugs.Data data, UUID player, int order, long cycle) {
        var saved = data.civic.get(key(player, ORDERS.get(order)));
        if (saved == null || (saved.cycle() < cycle && saved.paid())) return new Delivery(cycle, 0, false, "");
        return saved.cycle() < cycle ? new Delivery(cycle,saved.count(),false,saved.party()) : saved;
    }
    public static int carried(ServerPlayer player, int order) {
        int total = 0; Item item = ORDERS.get(order).itemType();
        for (int slot=0; slot<36; slot++) if (plain(player.getInventory().getItem(slot), item)) total += player.getInventory().getItem(slot).getCount();
        return total;
    }
    private static boolean plain(ItemStack stack, Item item) {
        return item != Items.AIR && !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
    }
    /** Takes only requested, unmodified items from the main inventory; never equipment or personal containers. */
    public static boolean deliver(ServerPlayer player, int index) {
        if (index < 0 || index >= ORDERS.size() || player.isSpectator()) return false;
        var data = Jugs.data(player.level().getServer()); var order = ORDERS.get(index); long now = cycle(player);
        var old = progress(data, player.getUUID(), index, now);
        if (old.cycle() > now || old.paid() || old.count() >= order.count()) return false;
        int amount = Math.min(carried(player, index), order.count() - old.count());
        if (amount <= 0) return false;
        var party = JugcraftParties.manager().partyOf(player.getUUID()).map(p -> p.id().toString()).orElse("");
        // Membership is captured at the first contribution. Joining/leaving never resets a personal quota.
        String credit = old.count() == 0 ? party : old.party();
        int remaining = amount;
        for (int slot=0; slot<36 && remaining>0; slot++) {
            var stack = player.getInventory().getItem(slot);
            if (plain(stack, order.itemType())) { int take = Math.min(remaining, stack.getCount()); stack.shrink(take); remaining -= take; }
        }
        player.getInventory().setChanged();
        data.civic.put(key(player.getUUID(), order), new Delivery(now, old.count()+amount, false, credit));
        data.setDirty();
        return true;
    }
    /** Payout and its receipt live in the SAME SavedData file as Jugs: no separate reward ledger save race. */
    public static boolean claim(Jugs.Data data, UUID player, int index, long cycle) {
        if (index<0 || index>=ORDERS.size()) return false;
        var order=ORDERS.get(index); var old=progress(data,player,index,cycle);
        if (old.cycle()!=cycle || old.paid() || old.count()!=order.count()
            || data.balance(player)>TownShops.get().maxBalance-order.reward()) return false;
        data.civic.put(key(player,order),new Delivery(cycle,old.count(),true,old.party()));
        data.add(player,order.reward()); data.setDirty(); return true;
    }
    public static int partyDelivered(ServerPlayer player, int index) {
        String party=JugcraftParties.manager().partyOf(player.getUUID()).map(p->p.id().toString()).orElse("");
        if (party.isEmpty()) return 0;
        long cycle=cycle(player); int total=0;
        var data=Jugs.data(player.level().getServer());
        for (UUID member:JugcraftParties.manager().partyOf(player.getUUID()).orElseThrow().members()) {
            var d=data.civic.get(key(member,ORDERS.get(index)));
            if(d!=null&&d.cycle()==cycle&&d.party().equals(party)) total+=d.count();
        }
        return Math.min(32767,total);
    }
}
