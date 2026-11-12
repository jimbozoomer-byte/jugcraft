package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.NpcSkins;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Seasonal outfits for the named guests (worker A, owner request of 11 November 2026; the art record is
 * docs/audits/jze-guest-appearances.md). The owner, as an operator, chooses each guest's outfit: "normal" is its named
 * skin ({@link GuestAppearances}); "winter", "harvest" and "spring" are packaged alternates with the same face and hair,
 * {@code guest_<id>_<outfit>} (tools/guest_skins.py). The choice is saved with the overworld as
 * {@code jugcraft:town_guest_wardrobe}; nothing else is saved, and no relationship, ledger or reward state changes.
 *
 * <pre>
 * /guests outfit &lt;guest|all&gt; &lt;normal|winter|harvest|spring&gt;     choose (operators, permission level 2)
 * /guests outfit &lt;guest|all&gt;                                      what each wears
 * </pre>
 *
 * <p>A chosen outfit is worn through {@link CustomNpcs#setTownSkin}, as the appearance migration does
 * ({@link GuestService#tick}), and only by the guest's canonical entity while it wears one of its own looks: its named
 * skin, one of its outfits, the stand-in placement gave it, or the exact historical stand-in the migration replaces.
 * Anything else an operator gave it (another town skin, another guest's skin, a player's skin, resolved or pending) is
 * the operator's and is never overwritten; the choice waits until the guest wears its own look again. While any
 * player-skin lookup is in flight ({@link NpcSkins#outstanding()}) nothing is changed, so a lookup an operator asked
 * for is never cancelled by a wardrobe change. A guest that is not loaded changes when it is next loaded: once a
 * second the wardrobe looks for each chosen guest's canonical entity among loaded entities (never loading a chunk).
 *
 * <p>The {@code /guests} root carries no requirement (other {@code /guests} subcommands are for players); the
 * {@code outfit} node requires permission level 2 and every command checks it again before it reads or changes anything.
 */
public final class GuestWardrobe extends SavedData {
	public static final String NORMAL = "normal";
	/** Every outfit, in the order offered; "normal" is the named skin itself. tools/guest_skins.py has the same list. */
	public static final List<String> OUTFITS = List.of(NORMAL, "winter", "harvest", "spring");
	/** The command's word for every available guest in the roster. */
	public static final String ALL = "all";
	/** How often (server ticks) loaded guests are dressed in their chosen outfit. */
	public static final int SWEEP_TICKS = 20;

	private static final Codec<GuestWardrobe> BODY = RecordCodecBuilder.create(i -> i.group(
			Saved.keeping("town_guest_wardrobe", Codec.STRING, Codec.STRING)
					.optionalFieldOf("outfits", Saved.Kept.<String, String>of(Map.of())).forGetter(GuestWardrobe::saved)
	).apply(i, GuestWardrobe::new));
	/** The whole wardrobe, versioned ({@link Saved#versioned}). */
	public static final Codec<GuestWardrobe> CODEC = Saved.versioned("town_guest_wardrobe", BODY);
	static final SavedDataType<GuestWardrobe> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_wardrobe"), GuestWardrobe::new, CODEC, null);

	/** Every packaged seasonal skin: guest_&lt;id&gt;_&lt;outfit&gt; for each named skin and seasonal outfit. */
	private static final Set<String> SEASONAL = seasonal();

	/**
	 * Game tests only (GuestWardrobeTesting): a synthetic "test_" guest dressed in a named guest's art, so the tests
	 * need not bind the real ids. The mod never adds to it; ids without the prefix are never looked up here.
	 */
	static final Map<String, String> TEST_ART = new ConcurrentHashMap<>();

	/** Guest id to the outfit an operator chose, as written (an outfit this version does not know is kept, and ignored). */
	private final Map<String, String> chosen = new TreeMap<>();
	/** Entries kept exactly as written because they could not be read. */
	private final Map<String, Dynamic<?>> unread = new LinkedHashMap<>();

	public GuestWardrobe() {
	}

	private GuestWardrobe(Saved.Kept<String, String> saved) {
		chosen.putAll(saved.read());
		unread.putAll(saved.unread());
	}

	private Saved.Kept<String, String> saved() {
		return new Saved.Kept<>(chosen, unread);
	}

	private static Set<String> seasonal() {
		Set<String> skins = new TreeSet<>();
		for (String named : GuestAppearances.skins()) {
			for (String outfit : OUTFITS) {
				if (!outfit.equals(NORMAL)) {
					skins.add(named + "_" + outfit);
				}
			}
		}
		return Collections.unmodifiableSet(skins);
	}

	/** The server's wardrobe (the overworld's saved data). */
	public static GuestWardrobe get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/** Called once from {@link GuestService#register}: the command, and dressing loaded guests once a second. */
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % SWEEP_TICKS == 0) {
				sweep(server);
			}
		});
	}

	// ---------------------------------------------------------------- the choice

	/** The guest's chosen outfit: "normal" unless an operator chose one this version knows. */
	public String outfit(String guest) {
		String outfit = chosen.get(guest);
		return outfit != null && OUTFITS.contains(outfit) ? outfit : NORMAL;
	}

	/** Every recorded choice (a copy, as written). */
	public Map<String, String> choices() {
		return Map.copyOf(chosen);
	}

	/** Records a choice for an available guest; false (nothing changed) for an unknown guest or outfit, or the same choice. */
	boolean choose(String guest, String outfit) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available() || !OUTFITS.contains(outfit) || outfit.equals(chosen.get(guest))) {
			return false;
		}
		chosen.put(guest, outfit);
		setDirty();
		return true;
	}

	/** Forgets a guest's choice (game tests only, through GuestWardrobeTesting). */
	void forget(String guest) {
		if (chosen.remove(guest) != null) {
			setDirty();
		}
	}

	// ---------------------------------------------------------------- the skins

	/** Every packaged seasonal skin (for {@code CustomNpcs.townSkins()}, alongside {@link GuestAppearances#skins()}). */
	public static Set<String> skins() {
		return SEASONAL;
	}

	/** The guest's named skin (its authored art, guest_&lt;id&gt;), or null for a guest wearing a placeholder. */
	static @Nullable String namedSkin(String guest) {
		String art = guest.startsWith("test_") ? TEST_ART.getOrDefault(guest, guest) : guest;
		GuestAppearances.Appearance look = GuestAppearances.of(art);
		return look != null && look.finalArt() ? look.townSkin() : null;
	}

	/**
	 * The skin the guest wears in an outfit: its named skin for "normal" (its placeholder if it has no art yet), or its
	 * packaged seasonal skin; null when it has none (an unknown or pending guest, an unknown outfit, a placeholder guest's
	 * seasonal outfit).
	 */
	public static @Nullable String skin(String guest, String outfit) {
		if (!OUTFITS.contains(outfit)) {
			return null;
		}
		String named = namedSkin(guest);
		if (outfit.equals(NORMAL)) {
			if (named != null) {
				return named;
			}
			GuestAppearances.Appearance look = GuestAppearances.of(guest);
			return look == null ? null : look.townSkin();
		}
		String seasonal = named == null ? null : named + "_" + outfit;
		return seasonal != null && SEASONAL.contains(seasonal) ? seasonal : null;
	}

	/**
	 * Whether {@code skin} is one of the guest's own looks, which the wardrobe may change: its named skin, an outfit's,
	 * the stand-in placement gave it, or (with authored art) the exact historical stand-in the migration replaces.
	 */
	static boolean ours(String guest, String skin) {
		for (String outfit : OUTFITS) {
			if (skin.equals(skin(guest, outfit))) {
				return true;
			}
		}
		GuestAppearances.Appearance look = GuestAppearances.of(guest);
		return look != null && (skin.equals(look.townSkin()) || look.finalArt() && skin.equals(GuestAppearances.legacySkin(guest)));
	}

	/** What dressing one guest did. */
	public enum Dressed {
		/** Now wears the chosen outfit. */
		CHANGED,
		/** Already wore it. */
		UNCHANGED,
		/** Wears a town skin an operator gave it, which is kept. */
		KEPT_OWN_SKIN,
		/** Wears (or is waiting for) a player's skin, which is kept. */
		KEPT_PLAYER_SKIN,
		/** A player-skin lookup is in flight somewhere: nothing changes until it answers. */
		WAITING,
		/** No packaged skin for that outfit: the look is unchanged. */
		NO_ART,
		/** Not the guest's canonical, available entity (or not loaded): left alone. */
		NOT_GUEST
	}

	/**
	 * Dresses the guest's canonical entity in its chosen outfit, if it wears one of its own looks (server). Every check
	 * is made here: the roster entry available, the entity a custom NPC carrying that guest id, not removed, in a server
	 * level and the ledger's canonical entity.
	 */
	static Dressed dress(Townsfolk npc, String guest, boolean lookupsInFlight) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		CustomNpcs.Data data = npc.custom();
		if (entry == null || !entry.available() || data == null || npc.isRemoved() || !guest.equals(npc.guest())) {
			return Dressed.NOT_GUEST;
		}
		if (!(npc.level() instanceof ServerLevel level)) {
			return Dressed.NOT_GUEST;
		}
		MinecraftServer server = level.getServer();
		if (!npc.getUUID().equals(GuestLedger.get(server).canonical(guest))) {
			return Dressed.NOT_GUEST;
		}
		if (!data.skinName().isEmpty() || data.profile() != null || CustomNpcs.PLAYER_SKIN.equals(npc.skin())) {
			return Dressed.KEPT_PLAYER_SKIN;
		}
		String current = npc.skin();
		if (!ours(guest, current)) {
			return Dressed.KEPT_OWN_SKIN;
		}
		String wanted = skin(guest, get(server).outfit(guest));
		if (wanted == null) {
			return Dressed.NO_ART;
		}
		if (wanted.equals(current)) {
			return Dressed.UNCHANGED;
		}
		if (lookupsInFlight) {
			return Dressed.WAITING;
		}
		return CustomNpcs.setTownSkin(npc, wanted) ? Dressed.CHANGED : Dressed.NO_ART;
	}

	/** The guest's canonical entity if it is loaded in any level (never loads a chunk), else null. */
	static @Nullable Townsfolk loaded(MinecraftServer server, String guest) {
		UUID canonical = GuestLedger.get(server).canonical(guest);
		if (canonical == null) {
			return null;
		}
		for (ServerLevel level : server.getAllLevels()) {
			if (level.getEntity(canonical) instanceof Townsfolk npc && !npc.isRemoved() && guest.equals(npc.guest())) {
				return npc;
			}
		}
		return null;
	}

	/** Once a second: every guest with a recorded choice, if its canonical entity is loaded, is dressed. */
	static void sweep(MinecraftServer server) {
		GuestWardrobe wardrobe = get(server);
		if (wardrobe.chosen.isEmpty()) {
			return;
		}
		boolean waiting = NpcSkins.outstanding() > 0;
		for (String guest : List.copyOf(wardrobe.chosen.keySet())) {
			if (!OUTFITS.contains(wardrobe.chosen.get(guest))) {
				continue;   // an outfit from a newer version: kept, never acted on
			}
			Townsfolk npc = loaded(server, guest);
			if (npc != null) {
				dress(npc, guest, waiting);
			}
		}
	}

	// ---------------------------------------------------------------- the command

	/** {@code /guests outfit}: no requirement on the shared root, operators only from the {@code outfit} node down. */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests")
				.then(Commands.literal("outfit").requires(GuestWardrobe::allowed)
						.then(guestArgument()
								.executes(c -> show(c.getSource(), StringArgumentType.getString(c, "guest")))
								.then(outfitArgument()
										.executes(c -> outfit(c.getSource(), StringArgumentType.getString(c, "guest"),
												StringArgumentType.getString(c, "outfit")))))));
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> guestArgument() {
		return Commands.argument("guest", StringArgumentType.word()).suggests((context, builder) -> {
			if (allowed(context.getSource())) {
				String typed = builder.getRemaining().toLowerCase(Locale.ROOT);
				if (ALL.startsWith(typed)) {
					builder.suggest(ALL);
				}
				for (GuestRoster.Guest guest : GuestRoster.ALL) {
					if (guest.available() && guest.id().startsWith(typed)) {
						builder.suggest(guest.id());
					}
				}
			}
			return builder.buildFuture();
		});
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> outfitArgument() {
		return Commands.argument("outfit", StringArgumentType.word()).suggests((context, builder) -> {
			if (allowed(context.getSource())) {
				String typed = builder.getRemaining().toLowerCase(Locale.ROOT);
				for (String outfit : OUTFITS) {
					if (outfit.startsWith(typed)) {
						builder.suggest(outfit);
					}
				}
			}
			return builder.buildFuture();
		});
	}

	/** Operators only: permission level 2, as {@code CustomNpcs.isOperator}. Quiet, for the command tree. */
	private static boolean allowed(CommandSourceStack source) {
		return Commands.LEVEL_GAMEMASTERS.check(source.permissions());
	}

	/** The check again, inside each command; a refused source is told why. */
	private static boolean operator(CommandSourceStack source) {
		if (allowed(source)) {
			return true;
		}
		source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.outfit.operator",
				"Only operators can choose the guests' outfits."));
		return false;
	}

	/** The guests a command names: one available guest, or every available guest for "all"; null (and told) otherwise. */
	private static @Nullable List<String> targets(CommandSourceStack source, String target) {
		if (ALL.equals(target)) {
			List<String> all = new ArrayList<>();
			for (GuestRoster.Guest guest : GuestRoster.ALL) {
				if (guest.available()) {
					all.add(guest.id());
				}
			}
			return all;
		}
		GuestRoster.Guest entry = GuestRoster.get(target);
		if (entry == null || !entry.available()) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.outfit.unknown_guest",
					"There is no available guest called %s.", target));
			return null;
		}
		return List.of(entry.id());
	}

	/** {@code /guests outfit <guest|all> <outfit>}: the number of guests whose choice is recorded (0: refused). */
	static int outfit(CommandSourceStack source, String target, String outfit) {
		if (!operator(source)) {
			return 0;
		}
		if (!OUTFITS.contains(outfit)) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.outfit.unknown_outfit",
					"There is no outfit called %s. Outfits: %s.", outfit, String.join(", ", OUTFITS)));
			return 0;
		}
		List<String> guests = targets(source, target);
		return guests == null ? 0 : outfit(source, guests, outfit, ALL.equals(target));
	}

	/** Records the outfit for each named guest and dresses those loaded now; a line each, or one summary for "all". */
	static int outfit(CommandSourceStack source, List<String> guests, String outfit, boolean summary) {
		if (!operator(source) || !OUTFITS.contains(outfit)) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		GuestWardrobe wardrobe = get(server);
		boolean waiting = NpcSkins.outstanding() > 0;
		Map<Dressed, Integer> counts = new EnumMap<>(Dressed.class);
		int recorded = 0, later = 0;
		for (String guest : guests) {
			GuestRoster.Guest entry = GuestRoster.get(guest);
			if (entry == null || !entry.available()) {
				continue;
			}
			wardrobe.choose(guest, outfit);
			recorded++;
			Townsfolk npc = loaded(server, guest);
			Dressed result = npc == null ? Dressed.NOT_GUEST : dress(npc, guest, waiting);
			if (result == Dressed.NOT_GUEST) {
				later++;
			} else {
				counts.merge(result, 1, Integer::sum);
			}
			if (!summary) {
				Component line = line(entry, outfit, result, npc);
				source.sendSuccess(() -> line, true);
			}
		}
		if (summary) {
			Component line = Component.translatableWithFallback("message.jugcraft.guest.outfit.summary",
					"%s guests will wear the %s outfit: %s changed now, %s already wearing it, %s keeping an operator's skin, "
							+ "%s waiting for skin lookups, %s not loaded (they change when they next load).",
					recorded, label(outfit), counts.getOrDefault(Dressed.CHANGED, 0), counts.getOrDefault(Dressed.UNCHANGED, 0),
					counts.getOrDefault(Dressed.KEPT_OWN_SKIN, 0) + counts.getOrDefault(Dressed.KEPT_PLAYER_SKIN, 0),
					counts.getOrDefault(Dressed.WAITING, 0), later);
			source.sendSuccess(() -> line, true);
		}
		Jugcraft.LOGGER.info("[guests] Wardrobe by {}: {} set to the {} outfit", source.getTextName(),
				summary ? "every guest" : String.join(", ", guests), outfit);
		return recorded;
	}

	/** What one guest's choice did, for the operator. */
	private static Component line(GuestRoster.Guest guest, String outfit, Dressed result, @Nullable Townsfolk npc) {
		Component name = Component.literal(guest.name());
		Component wear = label(outfit);
		String key = "message.jugcraft.guest.outfit.";
		return switch (result) {
			case CHANGED -> Component.translatableWithFallback(key + "changed", "%s now wears the %s outfit.", name, wear);
			case UNCHANGED -> Component.translatableWithFallback(key + "unchanged", "%s already wears the %s outfit.", name, wear);
			case KEPT_OWN_SKIN -> Component.translatableWithFallback(key + "kept_skin",
					"%s will wear the %s outfit, but keeps the town skin an operator gave it (%s) until it wears its own look again.",
					name, wear, npc == null ? "" : npc.skin());
			case KEPT_PLAYER_SKIN -> Component.translatableWithFallback(key + "kept_player",
					"%s will wear the %s outfit, but keeps its player skin until it wears its own look again.", name, wear);
			case WAITING -> Component.translatableWithFallback(key + "waiting",
					"%s will change into the %s outfit once the player-skin lookups in progress finish.", name, wear);
			case NO_ART -> Component.translatableWithFallback(key + "no_art", "%s has no %s outfit yet; its look is unchanged.", name, wear);
			case NOT_GUEST -> Component.translatableWithFallback(key + "later", "%s will wear the %s outfit when it is next loaded.", name, wear);
		};
	}

	/** An outfit's shown name. */
	public static Component label(String outfit) {
		String english = outfit.equals("harvest") ? "autumn harvest" : outfit;
		return Component.translatableWithFallback("message.jugcraft.guest.outfit.name." + outfit, english);
	}

	/** {@code /guests outfit <guest|all>}: what the guest (or every guest with a choice) wears. */
	static int show(CommandSourceStack source, String target) {
		if (!operator(source)) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		GuestWardrobe wardrobe = get(server);
		if (ALL.equals(target)) {
			List<String> shown = new ArrayList<>();
			for (GuestRoster.Guest guest : GuestRoster.ALL) {
				if (guest.available() && !wardrobe.outfit(guest.id()).equals(NORMAL)) {
					shown.add(guest.id() + ": " + wardrobe.outfit(guest.id()));
				}
			}
			Component line = shown.isEmpty()
					? Component.translatableWithFallback("message.jugcraft.guest.outfit.list_normal", "Every guest wears the normal outfit.")
					: Component.translatableWithFallback("message.jugcraft.guest.outfit.list", "Guest outfits: %s.", String.join(", ", shown));
			source.sendSuccess(() -> line, false);
			return 1;
		}
		List<String> guests = targets(source, target);
		if (guests == null) {
			return 0;
		}
		GuestRoster.Guest entry = GuestRoster.get(guests.getFirst());
		if (entry == null) {
			return 0;
		}
		Townsfolk npc = loaded(server, entry.id());
		Component wearing = npc == null ? Component.translatableWithFallback("message.jugcraft.guest.outfit.not_loaded", "not loaded now")
				: Component.literal(npc.skin());
		Component line = Component.translatableWithFallback("message.jugcraft.guest.outfit.show", "%s: the %s outfit (wearing %s).",
				Component.literal(entry.name()), label(wardrobe.outfit(entry.id())), wearing);
		source.sendSuccess(() -> line, false);
		return 1;
	}
}
