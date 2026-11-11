package io.github.jimbozoomer.jugcraft.town.guests;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

/**
 * Worker A's test seams into the wardrobe's package-private parts (GuestWardrobeGameTests): synthetic guests wearing a
 * named guest's art, wardrobes of the test's own, the codec, dressing one entity, the sweep, and the command with any
 * source and an explicit list of guests. Nothing here is used by the mod.
 *
 * <p>Tests bind only their own synthetic guests ({@code test_a_*}, {@link GuestRosterTesting#guest}); the 32 real ids
 * belong to the existing tests.
 */
public final class GuestWardrobeTesting {
	private GuestWardrobeTesting() {
	}

	/** Registers synthetic guest {@code id} (a {@code test_a_} id) wearing named guest {@code art}'s skins; returns the id. */
	public static String guest(String id, boolean mayMarry, String art) {
		if (!id.startsWith("test_a_")) {
			throw new IllegalArgumentException("Worker A's test guests start with test_a_: " + id);
		}
		GuestRosterTesting.guest(id, mayMarry);
		GuestWardrobe.TEST_ART.put(id, art);
		return id;
	}

	/** Forgets these guests' outfit choices in the server's wardrobe, so a test starts from "normal". */
	public static void reset(MinecraftServer server, String... guests) {
		GuestWardrobe wardrobe = GuestWardrobe.get(server);
		for (String guest : guests) {
			wardrobe.forget(guest);
		}
	}

	/** A new, empty wardrobe of the test's own, not the server's. */
	public static GuestWardrobe fresh() {
		return new GuestWardrobe();
	}

	public static boolean choose(GuestWardrobe wardrobe, String guest, String outfit) {
		return wardrobe.choose(guest, outfit);
	}

	/** Dresses one entity as the sweep would ({@code lookups}: a player-skin lookup is in flight). The result's name. */
	public static String dress(Townsfolk npc, String guest, boolean lookups) {
		return GuestWardrobe.dress(npc, guest, lookups).name();
	}

	/** What the sweep does once a second. */
	public static void sweep(MinecraftServer server) {
		GuestWardrobe.sweep(server);
	}

	public static boolean ours(String guest, String skin) {
		return GuestWardrobe.ours(guest, skin);
	}

	/** {@code /guests outfit <target> <outfit>} with any source. */
	public static int outfit(CommandSourceStack source, String target, String outfit) {
		return GuestWardrobe.outfit(source, target, outfit);
	}

	/** {@code /guests outfit <target>} with any source. */
	public static int show(CommandSourceStack source, String target) {
		return GuestWardrobe.show(source, target);
	}

	/** The command's own path for a list of guests, as "all" takes it (tests pass their own guests, never the roster). */
	public static int outfitAll(CommandSourceStack source, List<String> guests, String outfit) {
		return GuestWardrobe.outfit(source, guests, outfit, true);
	}

	// ---------------------------------------------------------------- the codec

	public static Tag save(GuestWardrobe wardrobe) {
		return GuestWardrobe.CODEC.encodeStart(NbtOps.INSTANCE, wardrobe).getOrThrow();
	}

	public static GuestWardrobe load(Tag saved) {
		return GuestWardrobe.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow();
	}

	public static JsonElement saveJson(GuestWardrobe wardrobe) {
		return GuestWardrobe.CODEC.encodeStart(JsonOps.INSTANCE, wardrobe).getOrThrow();
	}

	public static GuestWardrobe loadJson(JsonElement saved) {
		return GuestWardrobe.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
	}
}
