package io.github.jimbozoomer.jugcraft.town.guests;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Worker B's test seams for {@link GuestFavors} (game tests only): a day pinned per player, the guests' favourites and
 * gift values, today's affection already gained, and the saved data's codec round trip. Nothing here is reachable in
 * play.
 */
public final class GuestFavorTesting {
	private GuestFavorTesting() {
	}

	/** {@code player}'s favors use {@code day} in place of the overworld's until {@link #unpin}. */
	public static void pinDay(ServerPlayer player, long day) {
		GuestFavors.PINNED_DAYS.put(player.getUUID(), day);
	}

	/** {@code player}'s favors use the overworld's day again. */
	public static void unpin(UUID player) {
		GuestFavors.PINNED_DAYS.remove(player);
	}

	/** The clock's day for {@code player} (the pinned one, if pinned). */
	public static long day(ServerPlayer player) {
		return GuestFavors.day(player);
	}

	/** The day {@code player}'s limits belong to now: the clock's day, never earlier than their high-water day. */
	public static long effectiveDay(ServerPlayer player) {
		return GuestFavors.State.get(player.level().getServer()).effective(player.getUUID(), GuestFavors.day(player));
	}

	/** The quest {@code guest} offers {@code player} on {@code day}. */
	public static ItemStack offeredOn(String guest, ServerPlayer player, long day) {
		GuestFavors.Quest quest = GuestFavors.offered(guest, player.getUUID(), day);
		return new ItemStack(quest.item(), quest.count());
	}

	/** The guest's favourite gifts. */
	public static List<Item> favourites(String guest) {
		return GuestFavors.favourites(guest);
	}

	/** What one plain {@code item} is worth to {@code guest} as a gift, a favourite's bonus included; 0 if not taken. */
	public static int giftValue(String guest, Item item) {
		return GuestFavors.giftValue(guest, new ItemStack(item));
	}

	/** Records that {@code player} has already gained {@code amount} affection with {@code guest} today (as earlier gifts would). */
	public static void gainedToday(ServerPlayer player, String guest, int amount) {
		GuestFavors.State state = GuestFavors.State.get(player.level().getServer());
		long day = state.effective(player.getUUID(), GuestFavors.day(player));
		state.put(player.getUUID(), guest, state.today(player.getUUID(), guest, day).withAffection(amount), day);
	}

	/** {@code player}'s saved favor record with {@code guest} and their high-water day, as text ("null" when none): to check nothing moved on. */
	public static String record(ServerPlayer player, String guest) {
		GuestFavors.State state = GuestFavors.State.get(player.level().getServer());
		Map<String, GuestFavors.Favor> mine = state.players.get(player.getUUID());
		return (mine == null ? null : mine.get(guest)) + " @ " + state.highWater.get(player.getUUID());
	}

	/**
	 * Records today's offered quest for {@code guest} as taken up by {@code player}, without the entry points' checks: for
	 * a player who could not take it up in place (one standing in another level), so a refused turn-in is meaningful.
	 */
	public static void takeUp(ServerPlayer player, String guest) {
		GuestFavors.State state = GuestFavors.State.get(player.level().getServer());
		UUID id = player.getUUID();
		long day = state.effective(id, GuestFavors.day(player));
		GuestFavors.Quest quest = GuestFavors.offered(guest, id, day);
		state.put(id, guest, state.today(id, guest, day).withQuest(GuestFavors.QuestState.ACTIVE,
				BuiltInRegistries.ITEM.getKey(quest.item()).toString(), quest.count()), day);
	}

	/** The favors' saved data, written with its codec as the game writes it. */
	public static Tag save(MinecraftServer server) {
		return GuestFavors.State.CODEC.encodeStart(NbtOps.INSTANCE, GuestFavors.State.get(server)).getOrThrow();
	}

	/** Drops {@code player}'s live records and high-water day, as if they had never been kept. */
	public static void forget(MinecraftServer server, UUID player) {
		GuestFavors.State state = GuestFavors.State.get(server);
		state.players.remove(player);
		state.highWater.remove(player);
		state.setDirty();
	}

	/** Reads {@code saved} back with the codec and puts {@code player}'s records and high-water day from it into the live data. */
	public static void load(MinecraftServer server, Tag saved, UUID player) {
		GuestFavors.State restored = GuestFavors.State.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
		GuestFavors.State state = GuestFavors.State.get(server);
		Map<String, GuestFavors.Favor> mine = restored.players.get(player);
		if (mine == null) {
			state.players.remove(player);
		} else {
			state.players.put(player, new LinkedHashMap<>(mine));
		}
		Long water = restored.highWater.get(player);
		if (water == null) {
			state.highWater.remove(player);
		} else {
			state.highWater.put(player, water);
		}
		state.setDirty();
	}
}
