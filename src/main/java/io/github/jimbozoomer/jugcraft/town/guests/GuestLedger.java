package io.github.jimbozoomer.jugcraft.town.guests;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

/**
 * WORKER A IMPLEMENTS (API agreed; the bodies below are placeholders). The one persistent, canonical record of the
 * town guests, saved with the overworld as {@code jugcraft:town_guests}: which entity is each guest, affection per
 * player and guest, spouses, and remarriage cooldowns.
 *
 * <p>Rules every implementation keeps:
 * <ul>
 * <li>One canonical entity per guest id, server-wide, whether loaded or not. The first entity to {@link #verify} an
 * unbound id binds it; any other entity (a copy from an old save, a second placement) is {@link Claim#DUPLICATE} and
 * leaves. Its removal never frees the identity: {@link #lost} keeps the reservation, spouse and affection, and only
 * {@link #bind} (an operator's placement or repair) puts a new entity in its place.</li>
 * <li>One spouse per player, one player per guest; {@link #marry} checks and commits both sides at once.</li>
 * <li>{@link #divorce} frees both and gives each a cooldown of {@link GuestBalance#REMARRIAGE_COOLDOWN_MILLIS} of real
 * time, saved: it keeps running while the server is down and never shortens when the wall clock moves backwards.</li>
 * <li>Affection stays within 0 to {@link GuestBalance#MAX_AFFECTION}. Everything is saved on change.</li>
 * </ul>
 */
public final class GuestLedger extends SavedData {
	/** What a loaded guest entity is to its identity. */
	public enum Claim {
		CANONICAL, DUPLICATE, UNKNOWN_GUEST
	}

	/** Why a marriage cannot happen now. */
	public enum Refusal {
		NO_MARRIAGE, NOT_FULL_AFFECTION, PLAYER_MARRIED, GUEST_MARRIED, PLAYER_COOLDOWN, GUEST_COOLDOWN, GUEST_MISSING
	}

	/** Where a guest was last seen. */
	public record Place(ResourceKey<Level> dimension, BlockPos pos) {
	}

	/** Server hooks (the cooldown clock, anything else the ledger needs). Called once from {@link GuestService}. */
	public static void register() {
	}

	public static GuestLedger get(MinecraftServer server) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Called for each loaded guest entity once a second: binds an unbound id to it, else says whether it is the one. */
	public Claim verify(String guest, UUID entity) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** The guest's canonical entity, loaded or not; null while unbound. */
	public @Nullable UUID canonical(String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/**
	 * Makes {@code entity} the guest's canonical entity (placement, operator repair). Refused (false) while another
	 * canonical entity exists and has not been {@link #lost}; the entity it replaces is never accepted again.
	 */
	public boolean bind(String guest, UUID entity) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Records where the canonical entity is (for menus and repair); marks the ledger changed only when the block changes. Ignored for any other entity. */
	public void seen(String guest, UUID entity, ResourceKey<Level> dimension, BlockPos pos) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** The canonical entity was killed or removed for good: the identity, spouse and affection are kept. Ignored for any other entity. */
	public void lost(String guest, UUID entity) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Whether the guest's canonical entity is gone (lost and not yet replaced). */
	public boolean missing(String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	public @Nullable Place lastSeen(String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	public int affection(UUID player, String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Adds {@code delta} (may be negative), kept within 0 to the maximum; returns the new value. */
	public int addAffection(UUID player, String guest, int delta) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	public @Nullable String spouseOf(UUID player) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	public @Nullable UUID spouseOf(String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Real milliseconds left before the player may marry again; 0 when free. */
	public long cooldownMillis(UUID player) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Real milliseconds left before the guest may marry again; 0 when free. */
	public long cooldownMillis(String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Why the player may not marry the guest now, or null when they may (items and distance are not the ledger's). */
	public @Nullable Refusal canMarry(UUID player, String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Marries them if {@link #canMarry} allows it, both sides at once; false (nothing changed) otherwise. */
	public boolean marry(UUID player, String guest) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}

	/** Divorces the player from their spouse, starting both cooldowns; the guest freed, or null if unmarried. */
	public @Nullable String divorce(UUID player) {
		throw new UnsupportedOperationException("GuestLedger: worker A");
	}
}
