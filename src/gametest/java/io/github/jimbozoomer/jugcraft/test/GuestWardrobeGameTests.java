package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.GuestSkinFixture;
import io.github.jimbozoomer.jugcraft.town.NpcSkins;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestAppearances;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedgerTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestWardrobe;
import io.github.jimbozoomer.jugcraft.town.guests.GuestWardrobeTesting;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * JZE guest expansion (docs/plans/jze-guest-expansion-nov11.md), worker A's server tests: seasonal outfits
 * ({@link GuestWardrobe}, docs/audits/jze-guest-appearances.md). Only operators choose outfits; choices survive a save
 * and load; "normal" puts the named skin back; an operator's own town skin or a player's skin, pending or resolved, is
 * never overwritten; only the canonical entity is dressed; a guest placed later wears its choice; every seasonal skin
 * is packaged. Registered in fabric.mod.json by the coordinator.
 *
 * <p>Every test binds only worker A's synthetic guests ({@code test_a_*}, through
 * {@link GuestWardrobeTesting#guest}), each wearing a named guest's art, one set per test (game tests share one
 * server and run side by side); none binds a real roster id or uses the command's "all" on the server's roster.
 *
 * <p>Needs the coordinator's {@code CustomNpcs.townSkins()} patch (worker A's report): without it setTownSkin refuses
 * the seasonal names and the tests that dress a guest fail.
 */
public class GuestWardrobeGameTests {
	/** Synthetic guests (worker A's own ids): an adult fictional look, a held (younger) look, a real person's, the dress. */
	private static final String ADULT = GuestWardrobeTesting.guest("test_a_tifa", true, "tifa_lockhart");
	private static final String YOUNGER = GuestWardrobeTesting.guest("test_a_teio", false, "tokai_teio");
	private static final String REAL = GuestWardrobeTesting.guest("test_a_lauren", true, "lauren_chen");
	private static final String DRESS = GuestWardrobeTesting.guest("test_a_sarah", true, "sarah_stock");
	private static final String LATER = GuestWardrobeTesting.guest("test_a_frieren", true, "frieren");

	@GameTest(maxTicks = 200)
	public void onlyOperatorsChooseOutfits(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		Townsfolk npc = place(h, ADULT, 2, 2, "guest_tifa_lockhart");
		Townsfolk second = place(h, YOUNGER, 5, 5, "guest_tokai_teio");
		GuestWardrobe wardrobe = GuestWardrobe.get(server);
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		ServerPlayer visitor = TestPlayers.survivor(h);
		CommandSourceStack stranger = visitor.createCommandSourceStack().withSuppressedOutput();
		CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput().withLevel(h.getLevel());
		h.assertFalse(CustomNpcs.isOperator(stranger), "(the visitor is no operator)");
		h.assertTrue(!parses(dispatcher, "guests outfit " + ADULT + " winter", stranger) && !parses(dispatcher, "guests outfit " + ADULT, stranger)
				&& !parses(dispatcher, "guests outfit all winter", stranger), "A non-operator cannot run /guests outfit at all");
		h.assertTrue(GuestWardrobeTesting.outfit(stranger, ADULT, "winter") == 0 && GuestWardrobeTesting.show(stranger, ADULT) == 0
				&& GuestWardrobeTesting.outfitAll(stranger, List.of(ADULT, YOUNGER), "winter") == 0,
				"and every command refuses a non-operator by itself");
		h.assertTrue(wardrobe.outfit(ADULT).equals(GuestWardrobe.NORMAL) && wardrobe.outfit(YOUNGER).equals(GuestWardrobe.NORMAL)
				&& npc.skin().equals("guest_tifa_lockhart") && second.skin().equals("guest_tokai_teio"), "so nothing changed");
		h.assertTrue(dispatcher.execute("guests outfit " + ADULT + " summer", operator) == 0
				&& dispatcher.execute("guests outfit test_a_nobody winter", operator) == 0
				&& dispatcher.execute("guests outfit rooni winter", operator) == 0 && wardrobe.outfit(ADULT).equals(GuestWardrobe.NORMAL),
				"An unknown outfit, an unknown guest and a pending guest are refused");
		h.assertTrue(dispatcher.execute("guests outfit " + ADULT + " winter", operator) == 1 && wardrobe.outfit(ADULT).equals("winter")
				&& dispatcher.execute("guests outfit " + ADULT, operator) == 1, "An operator chooses the winter outfit and reads it back");
		h.assertTrue(GuestWardrobeTesting.outfitAll(operator, List.of(ADULT, YOUNGER), "harvest") == 2
				&& wardrobe.outfit(ADULT).equals("harvest") && wardrobe.outfit(YOUNGER).equals("harvest"),
				"The path \"all\" takes records each guest it is given (here only this test's own)");
		h.succeedWhen(() -> {
			// At once, or (if a player-skin lookup elsewhere is in flight) when the once-a-second sweep runs after it answers.
			h.assertTrue(npc.skin().equals("guest_tifa_lockhart_harvest") && second.skin().equals("guest_tokai_teio_harvest"),
					"Both wear the harvest outfit: " + npc.skin() + ", " + second.skin());
			remove(server, npc, second);
			GuestWardrobeTesting.reset(server, ADULT, YOUNGER);
		});
	}

	@GameTest(maxTicks = 1)
	public void choicesSurviveASaveAndLoad(GameTestHelper h) {
		GuestWardrobe wardrobe = GuestWardrobeTesting.fresh();
		h.assertTrue(GuestWardrobeTesting.choose(wardrobe, ADULT, "winter") && GuestWardrobeTesting.choose(wardrobe, YOUNGER, "spring")
				&& GuestWardrobeTesting.choose(wardrobe, REAL, GuestWardrobe.NORMAL) && GuestWardrobeTesting.choose(wardrobe, DRESS, "harvest"),
				"Four choices are recorded");
		h.assertTrue(!GuestWardrobeTesting.choose(wardrobe, ADULT, "winter") && !GuestWardrobeTesting.choose(wardrobe, ADULT, "summer")
				&& !GuestWardrobeTesting.choose(wardrobe, "rooni", "winter") && !GuestWardrobeTesting.choose(wardrobe, "test_a_nobody", "winter"),
				"The same choice again, an unknown outfit, a pending guest and an unknown guest change nothing");
		for (GuestWardrobe copy : List.of(GuestWardrobeTesting.load(GuestWardrobeTesting.save(wardrobe)),
				GuestWardrobeTesting.loadJson(GuestWardrobeTesting.saveJson(wardrobe)))) {
			h.assertTrue(copy.choices().equals(wardrobe.choices()) && copy.outfit(ADULT).equals("winter") && copy.outfit(YOUNGER).equals("spring")
					&& copy.outfit(REAL).equals(GuestWardrobe.NORMAL) && copy.outfit(DRESS).equals("harvest")
					&& copy.outfit(LATER).equals(GuestWardrobe.NORMAL), "Every choice survives a save and load: " + copy.choices());
		}
		// A save from a newer version (an outfit this one does not know) and a damaged entry: kept as written, never acted on.
		JsonObject outfits = new JsonObject();
		outfits.addProperty(ADULT, "winter");
		outfits.addProperty(YOUNGER, "summer");
		outfits.add("test_a_damaged", new JsonObject());
		JsonObject data = new JsonObject();
		data.add("outfits", outfits);
		JsonObject saved = new JsonObject();
		saved.addProperty("version", 1);
		saved.add("data", data);
		GuestWardrobe read = GuestWardrobeTesting.loadJson(saved);
		h.assertTrue(read.outfit(ADULT).equals("winter") && read.outfit(YOUNGER).equals(GuestWardrobe.NORMAL)
				&& "summer".equals(read.choices().get(YOUNGER)) && !read.choices().containsKey("test_a_damaged"),
				"An unknown outfit reads as normal but is kept; a damaged entry is set aside: " + read.choices());
		JsonObject again = GuestWardrobeTesting.saveJson(read).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("outfits");
		h.assertTrue(again.get(YOUNGER).getAsString().equals("summer") && again.get("test_a_damaged").isJsonObject()
				&& again.get(ADULT).getAsString().equals("winter"), "and both are written back unchanged: " + again);
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void normalPutsTheNamedSkinBackAndOnlyTheCanonicalGuestIsDressed(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		GuestWardrobe wardrobe = GuestWardrobe.get(server);
		Townsfolk npc = place(h, DRESS, 2, 2, "guest_sarah_stock");
		Townsfolk copy = null;
		try {
			for (String outfit : List.of("winter", "harvest", "spring")) {
				String skin = "guest_sarah_stock_" + outfit;
				String before = npc.skin();
				h.assertTrue(GuestWardrobe.skins().contains(skin) && skin.equals(GuestWardrobe.skin(DRESS, outfit)) && GuestWardrobeTesting.ours(DRESS, skin),
						skin + " is packaged and is the guest's own look");
				h.assertTrue(GuestWardrobeTesting.choose(wardrobe, DRESS, outfit), "(" + outfit + " chosen)");
				h.assertTrue(GuestWardrobeTesting.dress(npc, DRESS, true).equals("WAITING") && npc.skin().equals(before),
						"While a player-skin lookup is in flight nothing changes");
				h.assertTrue(GuestWardrobeTesting.dress(npc, DRESS, false).equals("CHANGED") && npc.skin().equals(skin)
						&& npc.custom().townSkin().equals(skin) && npc.custom().skinName().isEmpty(), "It now wears " + skin + ": " + npc.skin());
				h.assertTrue(GuestWardrobeTesting.dress(npc, DRESS, false).equals("UNCHANGED") && npc.skin().equals(skin), "Dressing again changes nothing");
			}
			h.assertTrue(GuestWardrobeTesting.choose(wardrobe, DRESS, GuestWardrobe.NORMAL) && GuestWardrobeTesting.dress(npc, DRESS, false).equals("CHANGED")
					&& npc.skin().equals("guest_sarah_stock") && "guest_sarah_stock".equals(GuestWardrobe.skin(DRESS, GuestWardrobe.NORMAL)),
					"normal puts the named skin back");
			String standIn = GuestAppearances.of(DRESS).townSkin();
			h.assertTrue(CustomNpcs.setTownSkin(npc, standIn) && GuestWardrobeTesting.ours(DRESS, standIn)
					&& GuestWardrobeTesting.dress(npc, DRESS, false).equals("CHANGED") && npc.skin().equals("guest_sarah_stock"),
					"The stand-in placement gave it is its own look too, and is replaced by its named skin");
			// A choice made while it is loaded but not yet dressed: the sweep dresses it (unless a lookup is in flight).
			h.assertTrue(GuestWardrobeTesting.choose(wardrobe, DRESS, "spring"), "(spring chosen)");
			GuestWardrobeTesting.sweep(server);
			h.assertTrue(npc.skin().equals("guest_sarah_stock_spring") || NpcSkins.outstanding() > 0, "The sweep dresses the loaded guest: " + npc.skin());
			// Only the canonical entity is ever dressed.
			copy = CustomNpcs.create(h.getLevel(), centre(h, 5, 5), 0, "Copy", CustomNpcs.NPC, null, DRESS);
			h.assertTrue(copy != null && CustomNpcs.setTownSkin(copy, "guest_sarah_stock"), "(an unbound entity carrying the same guest id)");
			h.assertTrue(GuestWardrobeTesting.dress(copy, DRESS, false).equals("NOT_GUEST") && copy.skin().equals("guest_sarah_stock"),
					"A duplicate is never dressed");
			h.assertTrue(GuestWardrobeTesting.dress(npc, ADULT, false).equals("NOT_GUEST"), "Nor is an entity under another guest's id");
		} finally {
			remove(server, npc, copy);
			GuestWardrobeTesting.reset(server, DRESS);
		}
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void anOperatorsSkinOrAPlayersSkinIsNeverOverwritten(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		GuestWardrobe wardrobe = GuestWardrobe.get(server);
		CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput().withLevel(h.getLevel());
		Townsfolk npc = place(h, REAL, 2, 2, "guest_lauren_chen");
		try {
			h.assertTrue(GuestWardrobeTesting.choose(wardrobe, REAL, "winter"), "(winter chosen)");
			// The guard's skin is never a placeholder; another guest's named and seasonal skins are never this guest's.
			for (String own : List.of(CustomNpcs.DEFAULT_GUARD_SKIN, "guest_makima", "guest_makima_winter")) {
				h.assertTrue(CustomNpcs.setTownSkin(npc, own), "(an operator gives it " + own + ")");
				h.assertTrue(!GuestWardrobeTesting.ours(REAL, own) && GuestWardrobeTesting.dress(npc, REAL, false).equals("KEPT_OWN_SKIN")
						&& npc.skin().equals(own), "An operator's " + own + " is kept");
				GuestWardrobeTesting.sweep(server);
				h.assertTrue(npc.skin().equals(own), "and the sweep leaves it");
			}
			h.assertTrue(GuestWardrobeTesting.outfit(operator, REAL, "spring") == 1 && wardrobe.outfit(REAL).equals("spring")
					&& npc.skin().equals("guest_makima_winter"), "The command records the choice and still keeps the operator's skin");
			h.assertTrue(CustomNpcs.setTownSkin(npc, "guest_lauren_chen"), "(its own skin again)");
			GuestSkinFixture.pendingPlayerSkin(npc, "Alex");
			h.assertTrue(GuestWardrobeTesting.dress(npc, REAL, false).equals("KEPT_PLAYER_SKIN") && npc.custom().skinName().equals("Alex")
					&& npc.skin().equals("guest_lauren_chen"), "A pending player skin is never overwritten");
			GuestWardrobeTesting.sweep(server);
			h.assertTrue(npc.custom().skinName().equals("Alex") && npc.skin().equals("guest_lauren_chen"), "nor by the sweep");
			CustomNpcs.setPlayerSkin(npc, "Steve", ResolvableProfile.createResolved(new GameProfile(UUID.randomUUID(), "Steve")));
			h.assertTrue(GuestWardrobeTesting.dress(npc, REAL, false).equals("KEPT_PLAYER_SKIN") && npc.skin().equals(CustomNpcs.PLAYER_SKIN)
					&& npc.custom().skinName().equals("Steve"), "Nor is a resolved player skin");
			GuestWardrobeTesting.sweep(server);
			h.assertTrue(npc.skin().equals(CustomNpcs.PLAYER_SKIN) && npc.custom().profile() != null, "nor by the sweep");
			h.assertTrue(GuestWardrobeTesting.outfit(operator, REAL, GuestWardrobe.NORMAL) == 1 && npc.skin().equals(CustomNpcs.PLAYER_SKIN),
					"Choosing normal keeps a player skin too");
			h.assertTrue(CustomNpcs.setTownSkin(npc, "guest_lauren_chen") && GuestWardrobeTesting.choose(wardrobe, REAL, "spring")
					&& GuestWardrobeTesting.dress(npc, REAL, false).equals("CHANGED") && npc.skin().equals("guest_lauren_chen_spring"),
					"Back in its own look, it wears the chosen outfit");
		} finally {
			remove(server, npc);
			GuestWardrobeTesting.reset(server, REAL);
		}
		h.succeed();
	}

	@GameTest(maxTicks = 200)
	public void aGuestPlacedLaterWearsItsChosenOutfit(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput().withLevel(h.getLevel());
		GuestLedgerTesting.reset(server, LATER);
		GuestWardrobeTesting.reset(server, LATER);
		h.assertTrue(GuestWardrobeTesting.outfit(operator, LATER, "spring") == 1 && GuestWardrobe.get(server).outfit(LATER).equals("spring"),
				"A choice for a guest with no entity is recorded");
		Townsfolk npc = place(h, LATER, 3, 3, "guest_frieren", false);
		h.succeedWhen(() -> {
			h.assertTrue(npc.skin().equals("guest_frieren_spring"), "Placed later, it changes into its chosen outfit: " + npc.skin());
			remove(server, npc);
			GuestWardrobeTesting.reset(server, LATER);
		});
	}

	@GameTest(maxTicks = 1)
	public void everySeasonalOutfitIsPackagedForEveryNamedGuest(GameTestHelper h) {
		Set<String> town = CustomNpcs.townSkins();
		int outfits = 0;
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			if (!guest.available()) {
				h.assertTrue(GuestWardrobe.skin(guest.id(), GuestWardrobe.NORMAL) == null && GuestWardrobe.skin(guest.id(), "winter") == null,
						guest.id() + " is pending and has no outfits");
				continue;
			}
			String named = GuestAppearances.of(guest.id()).townSkin();
			h.assertTrue(named.equals(GuestWardrobe.skin(guest.id(), GuestWardrobe.NORMAL)), guest.id() + "'s normal outfit is its named skin");
			for (String outfit : GuestWardrobe.OUTFITS) {
				if (outfit.equals(GuestWardrobe.NORMAL)) {
					continue;
				}
				String skin = GuestWardrobe.skin(guest.id(), outfit);
				h.assertTrue((named + "_" + outfit).equals(skin) && GuestWardrobe.skins().contains(skin), guest.id() + " has a " + outfit + " outfit");
				h.assertTrue(town.contains(skin), skin + " is a town skin an NPC may wear (CustomNpcs.townSkins patch)");
				h.assertTrue(GuestWardrobe.class.getResource("/assets/jugcraft/textures/entity/townsfolk/" + skin + ".png") != null,
						skin + " is packaged");
				outfits++;
			}
		}
		h.assertTrue(outfits == GuestAppearances.skins().size() * (GuestWardrobe.OUTFITS.size() - 1) && GuestWardrobe.skins().size() == outfits,
				"Every named guest has every seasonal outfit: " + outfits);
		h.assertTrue(GuestWardrobe.skin("emiru", "summer") == null && GuestWardrobe.skin("not_a_guest", "winter") == null,
				"An unknown outfit or guest has none");
		h.succeed();
	}

	// ---------------------------------------------------------------- helpers

	/** A synthetic guest's NPC, bound as its canonical entity and wearing {@code skin} (still, so it stays in place). */
	private static Townsfolk place(GameTestHelper h, String guest, int x, int z, String skin) {
		return place(h, guest, x, z, skin, true);
	}

	private static Townsfolk place(GameTestHelper h, String guest, int x, int z, String skin, boolean reset) {
		MinecraftServer server = h.getLevel().getServer();
		if (reset) {
			GuestLedgerTesting.reset(server, guest);
			GuestWardrobeTesting.reset(server, guest);
		}
		GuestRoster.Guest entry = GuestRoster.get(guest);
		Townsfolk npc = CustomNpcs.create(h.getLevel(), centre(h, x, z), 0, entry == null ? guest : entry.name(), CustomNpcs.NPC, null, guest);
		h.assertTrue(npc != null && guest.equals(npc.guest()), "A guest NPC for " + guest + " stands at " + x + ", " + z);
		h.assertTrue(GuestLedger.get(server).bind(guest, npc.getUUID()), guest + " is bound to it");
		h.assertTrue(CustomNpcs.setTownSkin(npc, skin), guest + " wears " + skin);
		npc.setNoAi(true);
		return npc;
	}

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static Vec3 centre(GameTestHelper h, int x, int z) {
		BlockPos at = h.absolutePos(new BlockPos(x, 2, z));
		return new Vec3(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
	}

	private static boolean parses(CommandDispatcher<CommandSourceStack> dispatcher, String command, CommandSourceStack source) {
		try {
			dispatcher.execute(command, source);
			return true;
		} catch (CommandSyntaxException refused) {
			return false;
		}
	}

	private static void remove(MinecraftServer server, @Nullable Townsfolk... npcs) {
		for (Townsfolk npc : npcs) {
			if (npc != null && !npc.isRemoved() && npc.custom() != null) {
				CustomNpcs.remove(server, npc.custom().id());
			}
		}
	}
}
