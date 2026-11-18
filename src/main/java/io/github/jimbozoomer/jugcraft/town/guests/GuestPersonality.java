package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Personality and town comedy (worker B, November 11; docs/plans/jze-guest-expansion-nov11.md item 2): who each guest
 * is, as a small trait set kept as data, and the entry point that wires the parts together.
 *
 * <ul>
 * <li>{@link GuestDialogueLines}: every line, as data. {@link GuestDialogue}: gift reactions, situational banter and the
 * "chat" option. {@link GuestPersonalityErrands}: character-flavoured errands. {@link GuestRivalries}: friendly rivalry
 * events. {@link GuestPersonalityFeature}: the Activities panel options. {@link GuestPersonalityState}: the saved data
 * ({@code jugcraft:town_guest_personality}).</li>
 * <li><b>Real people</b> all share {@link #GENERIC}: friendly, the town as their only interest, gentle humour. That is a
 * generic voice for talking about the game world, not a description of anyone; nothing here claims anything about a
 * real person. They take part in chats and errands with generic lines only, and never in rivalries.</li>
 * <li><b>Fictional guests</b> have an original trait set in their character's flavour ({@link #PROFILES}) and a short
 * list of preferred rivals. Traits are flavour, not mechanics: they do not change any number.</li>
 * <li><b>Pending guests</b> get nothing: {@link #profile} is null for them, and nothing here acts for them.</li>
 * <li><b>Romance</b> belongs only to guests whose roster entry {@code mayMarry()} (owner rule, 11 November 2026); see
 * {@link GuestDialogueLines}. Errands and rivalries are nonsexual and non-romantic for everyone.</li>
 * </ul>
 *
 * <p><b>Affection.</b> Every gain here goes through {@link GuestRewards#grant} (the shared daily budget,
 * {@link GuestFavors#DAILY_AFFECTION_CAP} per player and guest per day): a chat {@link GuestDialogue#CHAT_AFFECTION},
 * an errand {@link GuestPersonalityErrands#ERRAND_REWARD}, a rivalry {@link GuestRivalries#REWARD} per guest. Gift
 * reactions and banter grant nothing. No money or item is ever given.
 *
 * <p><b>The day</b> ({@link #day}) is the favors' day (the overworld's game time in whole days, never earlier than the
 * player's high-water day there), and never earlier than the player's high-water day in this module's own saved data,
 * so a clock moved backwards never grants a chat, errand or rivalry reward twice.
 */
public final class GuestPersonality {
	/** How a guest carries themself. */
	public enum Temperament {
		FRIENDLY, CHEERFUL, CALM, PROUD, STOIC, MISCHIEVOUS, EARNEST, DREAMY, SCHEMING, FIERY, CHAOTIC
	}

	/** What a guest likes talking about. */
	public enum Interest {
		THE_TOWN, FOOD, SWEETS, ANIMALS, MOVIES, MUSIC, BOOKS, MAGIC, EXPLOSIONS, GEMS, SCIENCE, RACING, TRAINING,
		MARTIAL_ARTS, MISSIONS, MYSTERIES, TREASURE, HOUSEWORK, SWIMMING, BARGAINS
	}

	/** How a guest is funny. */
	public enum Humour {
		GENTLE, BOASTFUL, DEADPAN, DRY, ABSURD, TEASING, DRAMATIC, SINCERE
	}

	/**
	 * A guest's traits: temperament, interests, humour, preferred rivals (fictional roster ids, tried first, nearest
	 * eligible guest otherwise) and whether this is the generic profile.
	 */
	public record Profile(Temperament temperament, List<Interest> interests, Humour humour, List<String> rivals, boolean generic) {
		public Profile {
			interests = List.copyOf(interests);
			rivals = List.copyOf(rivals);
		}
	}

	/** Every real person's profile, and any available guest's without one of its own: a generic, friendly voice. */
	public static final Profile GENERIC = new Profile(Temperament.FRIENDLY, List.of(Interest.THE_TOWN), Humour.GENTLE, List.of(), true);

	/** The fictional guests' own traits, by roster id (original flavour; nothing copied from a source work). */
	static final Map<String, Profile> PROFILES = new LinkedHashMap<>();

	static {
		fiction("power", Temperament.MISCHIEVOUS, Humour.BOASTFUL, List.of(Interest.FOOD, Interest.ANIMALS), "asa_mitaka", "himeno");
		fiction("makima", Temperament.CALM, Humour.DRY, List.of(Interest.ANIMALS, Interest.MOVIES), "kafka", "himeno");
		fiction("reze", Temperament.CHEERFUL, Humour.TEASING, List.of(Interest.SWIMMING, Interest.FOOD), "power", "himeno");
		fiction("asa_mitaka", Temperament.EARNEST, Humour.DEADPAN, List.of(Interest.BOOKS), "power", "reze");
		fiction("himeno", Temperament.CHEERFUL, Humour.TEASING, List.of(Interest.MISSIONS, Interest.FOOD), "makima", "power");
		fiction("tokai_teio", Temperament.PROUD, Humour.BOASTFUL, List.of(Interest.RACING, Interest.SWEETS), "gold_ship", "special_week");
		fiction("agnes_tachyon", Temperament.SCHEMING, Humour.ABSURD, List.of(Interest.SCIENCE), "gold_ship", "oguri_cap");
		fiction("gold_ship", Temperament.CHAOTIC, Humour.ABSURD, List.of(Interest.RACING, Interest.MYSTERIES), "tokai_teio", "agnes_tachyon");
		fiction("oguri_cap", Temperament.CALM, Humour.DEADPAN, List.of(Interest.FOOD, Interest.RACING), "special_week", "tokai_teio");
		fiction("special_week", Temperament.EARNEST, Humour.SINCERE, List.of(Interest.FOOD, Interest.RACING), "oguri_cap", "tokai_teio");
		fiction("rei_ayanami", Temperament.STOIC, Humour.DEADPAN, List.of(Interest.BOOKS), "asuka_langley");
		fiction("asuka_langley", Temperament.FIERY, Humour.DRAMATIC, List.of(Interest.TRAINING), "rei_ayanami", "megumin");
		fiction("misato_katsuragi", Temperament.CHEERFUL, Humour.TEASING, List.of(Interest.ANIMALS, Interest.FOOD), "himeno", "tifa_lockhart");
		fiction("frieren", Temperament.DREAMY, Humour.DRY, List.of(Interest.MAGIC, Interest.MYSTERIES, Interest.BOOKS), "megumin", "rin_ishtar");
		fiction("rem", Temperament.EARNEST, Humour.SINCERE, List.of(Interest.HOUSEWORK, Interest.FOOD), "tifa_lockhart", "megumin");
		fiction("megumin", Temperament.FIERY, Humour.DRAMATIC, List.of(Interest.EXPLOSIONS, Interest.MAGIC), "frieren", "asuka_langley");
		fiction("zero_two", Temperament.MISCHIEVOUS, Humour.TEASING, List.of(Interest.SWEETS), "asuka_langley", "power");
		fiction("rin_ishtar", Temperament.PROUD, Humour.DRY, List.of(Interest.GEMS, Interest.MAGIC, Interest.BARGAINS), "megumin", "asuka_langley");
		fiction("tifa_lockhart", Temperament.CHEERFUL, Humour.GENTLE, List.of(Interest.MARTIAL_ARTS, Interest.FOOD), "chun_li", "rem");
		fiction("yorha_2b", Temperament.STOIC, Humour.DEADPAN, List.of(Interest.MISSIONS), "ada_wong", "miyabi");
		fiction("chun_li", Temperament.EARNEST, Humour.SINCERE, List.of(Interest.MARTIAL_ARTS, Interest.MYSTERIES), "tifa_lockhart");
		fiction("ada_wong", Temperament.SCHEMING, Humour.DRY, List.of(Interest.MISSIONS, Interest.MYSTERIES), "lara_croft", "yorha_2b");
		fiction("lara_croft", Temperament.PROUD, Humour.DRY, List.of(Interest.TREASURE, Interest.MYSTERIES), "ada_wong");
		fiction("kafka", Temperament.CALM, Humour.TEASING, List.of(Interest.MUSIC), "makima");
		fiction("miyabi", Temperament.STOIC, Humour.DEADPAN, List.of(Interest.TRAINING), "yorha_2b", "nicole_demara");
		fiction("nicole_demara", Temperament.SCHEMING, Humour.BOASTFUL, List.of(Interest.BARGAINS), "miyabi", "kafka");
		for (Map.Entry<String, Profile> entry : PROFILES.entrySet()) {
			GuestRoster.Guest guest = GuestRoster.get(entry.getKey());
			if (guest == null || guest.pending() || guest.origin() != GuestRoster.Origin.FICTIONAL) {
				throw new IllegalStateException("A profile for a guest that is not an available fictional guest: " + entry.getKey());
			}
			for (String rival : entry.getValue().rivals()) {
				GuestRoster.Guest other = GuestRoster.get(rival);
				if (rival.equals(entry.getKey()) || other == null || other.pending() || other.origin() != GuestRoster.Origin.FICTIONAL) {
					throw new IllegalStateException("A rival must be another available fictional guest: " + entry.getKey() + " -> " + rival);
				}
			}
		}
	}

	private GuestPersonality() {
	}

	/** Called once from {@link GuestService#register}, after the favors: the feature, the event listeners and the rivalries. */
	public static void register() {
		GuestFeatures.register(GuestPersonalityFeature.INSTANCE);
		GuestEvents.GIFT_GIVEN.register(GuestDialogue::reactToGift);
		GuestEvents.TALKED.register(GuestDialogue::greet);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> GuestDialogue.forget(handler.player.getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> GuestDialogue.clear());
		GuestRivalries.register();
	}

	/**
	 * The guest's traits: null for an unknown or pending guest; {@link #GENERIC} for every real person and for an
	 * available guest with no profile of its own; else its own.
	 */
	public static @Nullable Profile profile(@Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return null;
		}
		if (entry.origin() == GuestRoster.Origin.REAL_PERSON) {
			return GENERIC;
		}
		Profile own = PROFILES.get(entry.id());
		return own != null ? own : GENERIC;
	}

	/** Whether the guest may take part in a rivalry: available and fictional (never a real person). */
	static boolean mayCompete(@Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return entry != null && entry.available() && entry.origin() == GuestRoster.Origin.FICTIONAL;
	}

	// ---------------------------------------------------------------- shared checks

	/**
	 * Why the player may not act with this guest now, or null when they may (every entry point here asks this before it
	 * takes or adds anything): the guest in the roster and available; the player alive and not a spectator; the entity
	 * alive, not removed, carrying {@code guest}, in the player's level within {@link GuestBalance#INTERACT_RANGE} of the
	 * player's eyes, and the ledger's canonical entity for the guest. The same test as the favors' own.
	 */
	static @Nullable GuestResult check(ServerPlayer player, @Nullable Townsfolk npc, @Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || guest == null) {
			Component name = npc != null ? npc.getDisplayName() : Component.literal(entry != null ? entry.name() : String.valueOf(guest));
			return GuestResult.refused("message.jugcraft.guest.unavailable", "%s is not receiving visitors yet.", name);
		}
		return here(player, npc, guest) ? null
				: GuestResult.refused("message.jugcraft.guest.not_here", "%s is not here to talk to.", Component.literal(entry.name()));
	}

	/** The position half of {@link #check}: player and canonical entity alive, together, within reach. */
	static boolean here(ServerPlayer player, @Nullable Townsfolk npc, String guest) {
		double reach = GuestBalance.INTERACT_RANGE;
		return npc != null && player.isAlive() && !player.isSpectator() && npc.isAlive() && !npc.isRemoved()
				&& guest.equals(npc.guest()) && npc.level() == player.level()
				&& npc.getBoundingBox().distanceToSqr(player.getEyePosition()) <= reach * reach
				&& npc.getUUID().equals(GuestLedger.get(player.level().getServer()).canonical(guest));
	}

	/** The guest's shown name. */
	static Component name(@Nullable Townsfolk npc, String guest) {
		if (npc != null) {
			return npc.getDisplayName();
		}
		GuestRoster.Guest entry = GuestRoster.get(guest);
		return Component.literal(entry != null ? entry.name() : guest);
	}

	/** "{@code speaker}: {@code line}", the way every line here is shown to the player. */
	static Component says(Component speaker, GuestDialogueLines.Line line) {
		return Component.translatableWithFallback("message.jugcraft.guest.personality.says", "%s: \"%s\"", speaker, line.component());
	}

	/**
	 * The day this module's daily limits belong to for the player: the favors' day (never earlier than their high-water
	 * day there), and never earlier than the player's high-water day in {@link GuestPersonalityState}.
	 */
	static long day(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		long favors = GuestFavors.State.get(server).effective(me, GuestFavors.day(player));
		return GuestPersonalityState.get(server).effective(me, favors);
	}

	private static void fiction(String id, Temperament temperament, Humour humour, List<Interest> interests, String... rivals) {
		if (PROFILES.put(id, new Profile(temperament, interests, humour, List.of(rivals), false)) != null) {
			throw new IllegalStateException("Repeated profile " + id);
		}
	}
}
