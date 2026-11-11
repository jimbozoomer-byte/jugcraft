package io.github.jimbozoomer.jugcraft.town.guests;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.function.ToLongFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Worker A's test seams into the town guests' package-private parts (GuestLedgerGameTests): isolated ledgers, the wall
 * clock and game time swapped only for the length of one synchronous block, the ledger's codec, and the repair commands
 * called with any source. Nothing here is used by the mod.
 *
 * <p>The clock and game time are static: a test swaps them only inside {@link #withClock} or {@link #withGameTime},
 * which put them (and the expiry high-water mark) back before returning, so no other test ever sees them changed. Inside
 * {@link #withClock} a test touches only ledgers it made with {@link #fresh} or {@link #load}, never the server's.
 */
public final class GuestLedgerTesting {
	private static final LongSupplier CLOCK = GuestLedger.clock;
	private static final ToLongFunction<MinecraftServer> GAME_TIME = GuestMarriage.gameTime;

	private GuestLedgerTesting() {
	}

	/** A new, empty ledger of the test's own, not the server's; its clock is anchored now (the swapped clock inside {@link #withClock}). */
	public static GuestLedger fresh() {
		return new GuestLedger();
	}

	/** Runs {@code body} with the wall clock reading {@code clock}, then puts the real clock back. */
	public static void withClock(LongSupplier clock, Runnable body) {
		GuestLedger.clock = clock;
		try {
			body.run();
		} finally {
			GuestLedger.clock = CLOCK;
		}
	}

	/** Runs {@code body} with game time read from {@code time}, then puts the real game time and the expiry high-water mark back. */
	public static void withGameTime(ToLongFunction<MinecraftServer> time, Runnable body) {
		long highest = GuestMarriage.highestGameTime();
		GuestMarriage.gameTime = time;
		try {
			body.run();
		} finally {
			GuestMarriage.gameTime = GAME_TIME;
			GuestMarriage.highestGameTime(highest);
		}
	}

	/** The server tick's note of the game time (without reading any request). */
	public static void observe(MinecraftServer server) {
		GuestMarriage.observe(server);
	}

	/** Forgets these guests in the server's ledger, so a test starts from a clean guest (its reserved ids only). */
	public static void reset(MinecraftServer server, String... guests) {
		GuestLedger ledger = GuestLedger.get(server);
		for (String guest : guests) {
			ledger.forget(guest);
		}
	}

	public static int changes(GuestLedger ledger) {
		return ledger.changes;
	}

	public static void advance(GuestLedger ledger) {
		ledger.advance();
	}

	public static boolean forceBind(GuestLedger ledger, String guest, UUID entity) {
		return ledger.forceBind(guest, entity);
	}

	public static List<UUID> replaced(GuestLedger ledger, String guest) {
		return ledger.replaced(guest);
	}

	public static int maxReplaced() {
		return GuestLedger.MAX_REPLACED;
	}

	public static boolean unreadable(GuestLedger ledger, String guest) {
		return ledger.unreadable(guest);
	}

	public static boolean unreadable(GuestLedger ledger, UUID player) {
		return ledger.unreadable(player);
	}

	public static boolean damaged(GuestLedger ledger, UUID player) {
		return ledger.damaged(player);
	}

	public static int setAffection(GuestLedger ledger, UUID player, String guest, int value) {
		return ledger.setAffection(player, guest, value);
	}

	public static @org.jspecify.annotations.Nullable UUID forceDivorce(GuestLedger ledger, String guest, boolean cooldowns) {
		return ledger.forceDivorce(guest, cooldowns);
	}

	public static void unmarry(GuestLedger ledger, UUID player, String guest) {
		ledger.unmarry(player, guest);
	}

	/** The game time the player's pending proposal lapses at, as asked, or -1. */
	public static long proposalExpiry(ServerPlayer player) {
		return GuestMarriage.proposalExpiry(player.getUUID());
	}

	// ---------------------------------------------------------------- the codec

	public static Tag save(GuestLedger ledger) {
		return GuestLedger.CODEC.encodeStart(NbtOps.INSTANCE, ledger).getOrThrow();
	}

	public static GuestLedger load(Tag saved) {
		return GuestLedger.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
	}

	public static JsonElement saveJson(GuestLedger ledger) {
		return GuestLedger.CODEC.encodeStart(JsonOps.INSTANCE, ledger).getOrThrow();
	}

	public static GuestLedger loadJson(JsonElement saved) {
		return GuestLedger.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
	}

	// ---------------------------------------------------------------- the repair commands, with any source

	public static int guestState(CommandSourceStack source, String guest) {
		return GuestRepairCommand.guestState(source, guest);
	}

	public static int playerState(CommandSourceStack source, String player) {
		return GuestRepairCommand.playerState(source, player);
	}

	public static int repairDivorce(CommandSourceStack source, String guest, boolean cooldowns) {
		return GuestRepairCommand.divorce(source, guest, cooldowns);
	}

	public static int repairPlayerCooldown(CommandSourceStack source, String player) {
		return GuestRepairCommand.clearPlayerCooldown(source, player);
	}

	public static int repairGuestCooldown(CommandSourceStack source, String guest) {
		return GuestRepairCommand.clearGuestCooldown(source, guest);
	}

	public static int repairAffection(CommandSourceStack source, String player, String guest, int value) {
		return GuestRepairCommand.affection(source, player, guest, value);
	}

	public static int repairRebind(CommandSourceStack source, String guest, boolean force) {
		return GuestRepairCommand.rebind(source, guest, force);
	}
}
