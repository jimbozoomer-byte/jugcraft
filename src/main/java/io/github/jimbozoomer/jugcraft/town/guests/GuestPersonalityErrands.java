package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestDialogueLines.Line;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPersonalityState.ErrandState;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Personality errands (worker B): original, character-flavoured requests (suspicious-chest investigations, eccentric
 * laboratory requests, absurd deliveries) on the favors' fetch pattern, in this module's own saved data
 * ({@link GuestPersonalityState}). Every real person (and any guest without errands of its own) asks one of the
 * {@link #GENERIC} errands, which are about the town only.
 *
 * <p><b>Rules.</b> One errand per player and guest per day, picked by a fixed hash of guest, player and day from the
 * guest's own list. Taking it up ({@link #accept}) takes nothing; turning it in ({@link #turnIn}) takes exactly the count
 * of plain stacks (no custom name or other changed component) from the 36 main slots and the offhand, once, and grants
 * {@link #ERRAND_REWARD} affection through {@link GuestRewards#grant} (within the shared daily budget); no money and no
 * item is ever given. Each entry point checks for itself ({@link GuestPersonality#check}: alive, not a spectator, same
 * level, within reach, the canonical entity, the guest id, the guest available). Before anything is taken (and before an
 * errand is taken up) the shared budget must have something left ({@link GuestRewards#left}) and the ledger must record
 * a gain ({@link GuestLedger#canGainAffection}); otherwise the action is refused and nothing is taken or moved on. A
 * replayed or doubled turn-in finds the errand done and takes nothing; too few items take nothing. An errand taken up
 * and not turned in lapses with its day (nothing was taken for it). Counting and taking copy {@link GuestFavors}' exact
 * inventory handling.
 *
 * <p><b>Items</b> are modest survival items only (food, flowers, sticks, paper, string, copper, coal and the like); the
 * greatest ask is 10. Items never come back.
 */
public final class GuestPersonalityErrands {
	/** One errand: bring {@code count} of {@code item}; what the guest says asking and thanking. */
	public record Errand(Item item, int count, Line ask, Line thanks) {
	}

	/** The affection a turned-in errand grants through {@link GuestRewards#grant} (subject to the shared budget). */
	public static final int ERRAND_REWARD = 12;
	/** The most items any errand asks for. */
	public static final int MAX_COUNT = 10;

	/** Every real person's errands, and any guest's without its own: about the town only. */
	public static final List<Errand> GENERIC = List.of(
			errand(Items.DANDELION, 6,
					say("message.jugcraft.guest.personality.generic.errand.0.ask", "The flower beds in the square look a little bare. Could you bring 6 dandelions?"),
					say("message.jugcraft.guest.personality.generic.errand.0.thanks", "Thank you! The square looks much brighter now.")),
			errand(Items.WHEAT, 8,
					say("message.jugcraft.guest.personality.generic.errand.1.ask", "The bakery is running low on wheat. Could you bring 8?"),
					say("message.jugcraft.guest.personality.generic.errand.1.thanks", "Thanks! The bakery will be glad of it.")),
			errand(Items.TORCH, 4,
					say("message.jugcraft.guest.personality.generic.errand.2.ask", "The path by the fishing dock is dark at night. Could you bring 4 torches?"),
					say("message.jugcraft.guest.personality.generic.errand.2.thanks", "Thank you! The dock will be much safer tonight.")));

	/** Fictional guests' own errands, by roster id. */
	static final Map<String, List<Errand>> AUTHORED = new LinkedHashMap<>();

	/** Test seam: a game-test guest (id starting "test_") asking an authored guest's errands. Empty in play. */
	static final Map<String, String> BORROWED = new java.util.concurrent.ConcurrentHashMap<>();

	static {
		AUTHORED.put("power", List.of(
				errand(Items.COOKED_PORKCHOP, 3,
						say("message.jugcraft.guest.personality.power.errand.0.ask", "Bring me 3 cooked porkchops. They are for a cat. A large, important cat who is definitely not me."),
						say("message.jugcraft.guest.personality.power.errand.0.thanks", "The cat thanks you. The cat is very full. The cat sounds a lot like me, coincidentally.")),
				errand(Items.STICK, 8,
						say("message.jugcraft.guest.personality.power.errand.1.ask", "I am building a throne. Bring me 8 sticks. It will be the greatest throne in town, and the only one."),
						say("message.jugcraft.guest.personality.power.errand.1.thanks", "Behold my throne! It wobbles, which makes it more dramatic."))));
		AUTHORED.put("makima", List.of(
				errand(Items.BONE, 6,
						say("message.jugcraft.guest.personality.makima.errand.0.ask", "The town dogs deserve a reward for good behaviour. Bring me 6 bones, please."),
						say("message.jugcraft.guest.personality.makima.errand.0.thanks", "They'll be very well behaved now. Thank you for helping me keep order.")),
				errand(Items.PAPER, 4,
						say("message.jugcraft.guest.personality.makima.errand.1.ask", "Someone left a suspicious chest by the square. Bring me 4 paper so I can file a proper report."),
						say("message.jugcraft.guest.personality.makima.errand.1.thanks", "Filed, stamped and sorted. That chest will never trouble anyone again."))));
		AUTHORED.put("reze", List.of(
				errand(Items.COOKED_COD, 4,
						say("message.jugcraft.guest.personality.reze.errand.0.ask", "The café ran out of fish for the lunch special. Could you bring 4 cooked cod?"),
						say("message.jugcraft.guest.personality.reze.errand.0.thanks", "Lunch is saved! The regulars will never know how close it was.")),
				errand(Items.KELP, 8,
						say("message.jugcraft.guest.personality.reze.errand.1.ask", "I promised the kids a swimming lesson and need floats. Bring me 8 kelp... don't ask how that works."),
						say("message.jugcraft.guest.personality.reze.errand.1.thanks", "Perfect! Kelp floats are a brand new invention. Probably."))));
		AUTHORED.put("asa_mitaka", List.of(
				errand(Items.PAPER, 6,
						say("message.jugcraft.guest.personality.asa_mitaka.errand.0.ask", "My study notes keep vanishing. Could you bring 6 paper? I'll write smaller this time."),
						say("message.jugcraft.guest.personality.asa_mitaka.errand.0.thanks", "Thank you. This time I'll guard them carefully. Or at least put them in a drawer.")),
				errand(Items.SUGAR, 4,
						say("message.jugcraft.guest.personality.asa_mitaka.errand.1.ask", "I'm trying to be more sociable, so I'm baking for the town. Could you bring 4 sugar?"),
						say("message.jugcraft.guest.personality.asa_mitaka.errand.1.thanks", "Now I just have to actually hand the cookies to people. That's the hard part."))));
		AUTHORED.put("himeno", List.of(
				errand(Items.COOKIE, 6,
						say("message.jugcraft.guest.personality.himeno.errand.0.ask", "The office snack drawer is empty and morale is falling. Bring me 6 cookies, quick!"),
						say("message.jugcraft.guest.personality.himeno.errand.0.thanks", "Morale restored! You're officially the office hero today.")),
				errand(Items.TORCH, 8,
						say("message.jugcraft.guest.personality.himeno.errand.1.ask", "A rookie lost the patrol lantern again. Bring me 8 torches. They'll lose those too, but slower."),
						say("message.jugcraft.guest.personality.himeno.errand.1.thanks", "Eight torches. That should last them nearly a whole week."))));
		AUTHORED.put("tokai_teio", List.of(
				errand(Items.HONEY_BOTTLE, 2,
						say("message.jugcraft.guest.personality.tokai_teio.errand.0.ask", "A champion needs fuel! Bring me 2 honey bottles for my special drink!"),
						say("message.jugcraft.guest.personality.tokai_teio.errand.0.thanks", "Sweet victory fuel! The next race is definitely mine!")),
				errand(Items.CANDLE, 2,
						say("message.jugcraft.guest.personality.tokai_teio.errand.1.ask", "I'm planning my victory party before the race. Bring me 2 candles for the cake!"),
						say("message.jugcraft.guest.personality.tokai_teio.errand.1.thanks", "Perfect! Now I just have to win. Easy!"))));
		AUTHORED.put("agnes_tachyon", List.of(
				errand(Items.GLASS_BOTTLE, 4,
						say("message.jugcraft.guest.personality.agnes_tachyon.errand.0.ask", "I require 4 glass bottles for a perfectly harmless experiment. It might fizz."),
						say("message.jugcraft.guest.personality.agnes_tachyon.errand.0.thanks", "Splendid! If anything turns green, that was intentional.")),
				errand(Items.GLOW_BERRIES, 6,
						say("message.jugcraft.guest.personality.agnes_tachyon.errand.1.ask", "For my next study I need 6 glow berries. Do not ask what they are for. I don't know yet."),
						say("message.jugcraft.guest.personality.agnes_tachyon.errand.1.thanks", "Ah, the glow is optimal! Science thanks you, and so, briefly, do I."))));
		AUTHORED.put("gold_ship", List.of(
				errand(Items.KELP, 6,
						say("message.jugcraft.guest.personality.gold_ship.errand.0.ask", "Quick! I need 6 kelp for a secret ceremony. It's a secret, so I can't tell you it's a kelp hat."),
						say("message.jugcraft.guest.personality.gold_ship.errand.0.thanks", "Hat complete! I mean, ceremony complete! I've said too much!")),
				errand(Items.PUMPKIN, 1,
						say("message.jugcraft.guest.personality.gold_ship.errand.1.ask", "Bring me a pumpkin. Just one. For reasons. Very serious reasons."),
						say("message.jugcraft.guest.personality.gold_ship.errand.1.thanks", "Perfect. This pumpkin is now the mayor. Don't tell the actual mayor."))));
		AUTHORED.put("oguri_cap", List.of(
				errand(Items.BREAD, 10,
						say("message.jugcraft.guest.personality.oguri_cap.errand.0.ask", "Could you bring me 10 bread? It's for a snack. A small one."),
						say("message.jugcraft.guest.personality.oguri_cap.errand.0.thanks", "Thank you. This will last me until dinner. Possibly.")),
				errand(Items.CARROT, 8,
						say("message.jugcraft.guest.personality.oguri_cap.errand.1.ask", "I need 8 carrots. For energy. And then more energy."),
						say("message.jugcraft.guest.personality.oguri_cap.errand.1.thanks", "Crunchy and perfect. I can feel myself getting faster."))));
		AUTHORED.put("special_week", List.of(
				errand(Items.POTATO, 8,
						say("message.jugcraft.guest.personality.special_week.errand.0.ask", "I want to cook a big countryside dinner for everyone! Could you bring 8 potatoes?"),
						say("message.jugcraft.guest.personality.special_week.errand.0.thanks", "Thank you! Everyone's going to eat so well tonight!")),
				errand(Items.STRING, 4,
						say("message.jugcraft.guest.personality.special_week.errand.1.ask", "I'm making good luck charms for the track team. Could you bring 4 string?"),
						say("message.jugcraft.guest.personality.special_week.errand.1.thanks", "They're perfect! Now we'll all run our very best!"))));
		AUTHORED.put("rei_ayanami", List.of(
				errand(Items.BONE_MEAL, 6,
						say("message.jugcraft.guest.personality.rei_ayanami.errand.0.ask", "The flower beds by the square need bone meal. Would you bring 6?"),
						say("message.jugcraft.guest.personality.rei_ayanami.errand.0.thanks", "...The flowers will grow. Thank you.")),
				errand(Items.BOOK, 2,
						say("message.jugcraft.guest.personality.rei_ayanami.errand.1.ask", "I would like something new to read. Please bring 2 books."),
						say("message.jugcraft.guest.personality.rei_ayanami.errand.1.thanks", "Thank you. I will read them slowly."))));
		AUTHORED.put("asuka_langley", List.of(
				errand(Items.ARROW, 8,
						say("message.jugcraft.guest.personality.asuka_langley.errand.0.ask", "I'm setting up a target range to prove I'm the best shot in town. Bring me 8 arrows!"),
						say("message.jugcraft.guest.personality.asuka_langley.errand.0.thanks", "Perfect. Prepare to be amazed. Everyone, really, but especially you.")),
				errand(Items.HAY_BLOCK, 3,
						say("message.jugcraft.guest.personality.asuka_langley.errand.1.ask", "Somebody keeps knocking over the training dummy. Bring me 3 hay bales to fix it."),
						say("message.jugcraft.guest.personality.asuka_langley.errand.1.thanks", "There. Now it can lose to me properly."))));
		AUTHORED.put("misato_katsuragi", List.of(
				errand(Items.COD, 6,
						say("message.jugcraft.guest.personality.misato_katsuragi.errand.0.ask", "My penguin's fridge is empty and he's giving me the look. Could you bring 6 raw cod?"),
						say("message.jugcraft.guest.personality.misato_katsuragi.errand.0.thanks", "He's doing a little happy dance. I'm doing one too. Don't look.")),
				errand(Items.BAKED_POTATO, 6,
						say("message.jugcraft.guest.personality.misato_katsuragi.errand.1.ask", "I promised to cook for the town and need 6 baked potatoes as a backup plan. A very necessary backup plan."),
						say("message.jugcraft.guest.personality.misato_katsuragi.errand.1.thanks", "Phew! Now nobody has to taste my experimental curry. Well, not much of it."))));
		AUTHORED.put("frieren", List.of(
				errand(Items.APPLE, 3,
						say("message.jugcraft.guest.personality.frieren.errand.0.ask", "There's a chest by the square that I'm almost sure is a mimic. Bring me 3 apples. If it bites one, we'll know."),
						say("message.jugcraft.guest.personality.frieren.errand.0.thanks", "It bit two apples and a bit of my sleeve. Investigation complete. Very successful.")),
				errand(Items.BOOK, 2,
						say("message.jugcraft.guest.personality.frieren.errand.1.ask", "I need 2 books to copy a spell into. It makes ink dry neatly. Truly essential."),
						say("message.jugcraft.guest.personality.frieren.errand.1.thanks", "Thank you. Now my notes will be tidy for the next few hundred years."))));
		AUTHORED.put("rem", List.of(
				errand(Items.WHEAT, 10,
						say("message.jugcraft.guest.personality.rem.errand.0.ask", "Rem wants to bake bread for the whole town. Could you bring 10 wheat?"),
						say("message.jugcraft.guest.personality.rem.errand.0.thanks", "Thank you! The whole town will have warm bread this evening.")),
				errand(Items.STICK, 8,
						say("message.jugcraft.guest.personality.rem.errand.1.ask", "Rem's broom is worn out from all the sweeping. Could you bring 8 sticks for a new one?"),
						say("message.jugcraft.guest.personality.rem.errand.1.thanks", "A new broom! Rem will sweep with renewed determination!"))));
		AUTHORED.put("megumin", List.of(
				errand(Items.GUNPOWDER, 4,
						say("message.jugcraft.guest.personality.megumin.errand.0.ask", "I require 4 gunpowder. For research. Purely theoretical research."),
						say("message.jugcraft.guest.personality.megumin.errand.0.thanks", "Excellent! I shall study it intensely and do absolutely nothing reckless. Today.")),
				errand(Items.COAL, 6,
						say("message.jugcraft.guest.personality.megumin.errand.1.ask", "My evening speech needs a dramatic campfire. Bring me 6 coal!"),
						say("message.jugcraft.guest.personality.megumin.errand.1.thanks", "The campfire roars! My speech will be legendary!"))));
		AUTHORED.put("zero_two", List.of(
				errand(Items.HONEY_BOTTLE, 3,
						say("message.jugcraft.guest.personality.zero_two.errand.0.ask", "I'm making honey toast for everyone. Bring me 3 honey bottles?"),
						say("message.jugcraft.guest.personality.zero_two.errand.0.thanks", "Sweet! Everyone gets toast. Well, everyone who asks nicely.")),
				errand(Items.STRING, 6,
						say("message.jugcraft.guest.personality.zero_two.errand.1.ask", "I want a kite to race the clouds. Bring me 6 string and some luck."),
						say("message.jugcraft.guest.personality.zero_two.errand.1.thanks", "Perfect! Those clouds don't stand a chance."))));
		AUTHORED.put("rin_ishtar", List.of(
				errand(Items.LAPIS_LAZULI, 4,
						say("message.jugcraft.guest.personality.rin_ishtar.errand.0.ask", "I need 4 lapis lazuli for a spell. Gems aren't cheap, and you look like you have spares."),
						say("message.jugcraft.guest.personality.rin_ishtar.errand.0.thanks", "Perfect quality! The spell will be flawless. Thanks. Really.")),
				errand(Items.REDSTONE, 4,
						say("message.jugcraft.guest.personality.rin_ishtar.errand.1.ask", "The old chest by the wall keeps rattling. Bring me 4 redstone dust so I can set a ward."),
						say("message.jugcraft.guest.personality.rin_ishtar.errand.1.thanks", "Ward set. If the chest rattles again, it's officially not my problem."))));
		AUTHORED.put("tifa_lockhart", List.of(
				errand(Items.BEETROOT, 6,
						say("message.jugcraft.guest.personality.tifa_lockhart.errand.0.ask", "I'm trying a new stew for the town. Could you bring 6 beetroots?"),
						say("message.jugcraft.guest.personality.tifa_lockhart.errand.0.thanks", "Perfect. It's going to be a very red, very good stew.")),
				errand(Items.LEATHER, 3,
						say("message.jugcraft.guest.personality.tifa_lockhart.errand.1.ask", "My training gloves are worn through. Could you bring 3 leather?"),
						say("message.jugcraft.guest.personality.tifa_lockhart.errand.1.thanks", "These will last a whole season. Maybe two, if the training dummy goes easy on me."))));
		AUTHORED.put("yorha_2b", List.of(
				errand(Items.COPPER_INGOT, 6,
						say("message.jugcraft.guest.personality.yorha_2b.errand.0.ask", "Equipment maintenance required. Please bring 6 copper ingots."),
						say("message.jugcraft.guest.personality.yorha_2b.errand.0.thanks", "Maintenance complete. Thank you for your assistance.")),
				errand(Items.COOKED_CHICKEN, 3,
						say("message.jugcraft.guest.personality.yorha_2b.errand.1.ask", "Field rations depleted. Request: 3 cooked chicken."),
						say("message.jugcraft.guest.personality.yorha_2b.errand.1.thanks", "Rations restored. Your support is noted."))));
		AUTHORED.put("chun_li", List.of(
				errand(Items.FEATHER, 6,
						say("message.jugcraft.guest.personality.chun_li.errand.0.ask", "I'm investigating the suspicious chest by the stables. Bring me 6 feathers so I can dust it for prints."),
						say("message.jugcraft.guest.personality.chun_li.errand.0.thanks", "The prints belong to... a chicken. Case closed. Excellent work, detective.")),
				errand(Items.EGG, 6,
						say("message.jugcraft.guest.personality.chun_li.errand.1.ask", "I'm making dumplings for the training group. Could you bring 6 eggs?"),
						say("message.jugcraft.guest.personality.chun_li.errand.1.thanks", "Dumpling day is saved! Everyone trained extra hard for these."))));
		AUTHORED.put("ada_wong", List.of(
				errand(Items.STRING, 6,
						say("message.jugcraft.guest.personality.ada_wong.errand.0.ask", "I need 6 string for a grappling line. Don't ask where I'm grappling to."),
						say("message.jugcraft.guest.personality.ada_wong.errand.0.thanks", "Perfect. You'll hear about where this goes. Or you won't.")),
				errand(Items.SPYGLASS, 1,
						say("message.jugcraft.guest.personality.ada_wong.errand.1.ask", "I'm keeping an eye on a suspicious chest from the rooftops. Bring me a spyglass."),
						say("message.jugcraft.guest.personality.ada_wong.errand.1.thanks", "Now I can watch it from a very respectful distance. It still hasn't moved. Suspicious."))));
		AUTHORED.put("lara_croft", List.of(
				errand(Items.LADDER, 8,
						say("message.jugcraft.guest.personality.lara_croft.errand.0.ask", "I'm mapping the old well. Bring me 8 ladders; rope always frays at the worst moment."),
						say("message.jugcraft.guest.personality.lara_croft.errand.0.thanks", "Splendid. The well goes down further than anyone thought. As I suspected.")),
				errand(Items.BRUSH, 1,
						say("message.jugcraft.guest.personality.lara_croft.errand.1.ask", "A suspicious chest by the library has a lock older than the town. Bring me a brush; archaeology is mostly gentle dusting."),
						say("message.jugcraft.guest.personality.lara_croft.errand.1.thanks", "Under the dust: more dust. And then a very old button. Thrilling!"))));
		AUTHORED.put("kafka", List.of(
				errand(Items.AMETHYST_SHARD, 4,
						say("message.jugcraft.guest.personality.kafka.errand.0.ask", "The square deserves a little music. Bring me 4 amethyst shards; they chime beautifully."),
						say("message.jugcraft.guest.personality.kafka.errand.0.thanks", "Listen. The square has a soundtrack now.")),
				errand(Items.OAK_PLANKS, 6,
						say("message.jugcraft.guest.personality.kafka.errand.1.ask", "My coat collection needs a new rack. Bring me 6 oak planks."),
						say("message.jugcraft.guest.personality.kafka.errand.1.thanks", "Elegant. Each coat will finally have room to breathe."))));
		AUTHORED.put("miyabi", List.of(
				errand(Items.BAMBOO, 8,
						say("message.jugcraft.guest.personality.miyabi.errand.0.ask", "My practice targets are splintered. Please bring 8 bamboo."),
						say("message.jugcraft.guest.personality.miyabi.errand.0.thanks", "Thank you. They will be splintered again soon. That is the point.")),
				errand(Items.SWEET_BERRIES, 6,
						say("message.jugcraft.guest.personality.miyabi.errand.1.ask", "I am making tea for my section. Bring 6 sweet berries? I heard they go well in tea."),
						say("message.jugcraft.guest.personality.miyabi.errand.1.thanks", "Thank you. The tea is... unusual. Everyone is being polite."))));
		AUTHORED.put("nicole_demara", List.of(
				errand(Items.RAW_COPPER, 6,
						say("message.jugcraft.guest.personality.nicole_demara.errand.0.ask", "I've got a delivery job, and I need 6 raw copper to make it look official. Don't ask."),
						say("message.jugcraft.guest.personality.nicole_demara.errand.0.thanks", "Delivery complete! The client was baffled. Payment pending. Forever, probably.")),
				errand(Items.PUMPKIN_PIE, 1,
						say("message.jugcraft.guest.personality.nicole_demara.errand.1.ask", "Absurd delivery: someone ordered exactly one pumpkin pie to the top of the clock tower. Bring me one?"),
						say("message.jugcraft.guest.personality.nicole_demara.errand.1.thanks", "Delivered! The pigeon at the top was very pleased with it."))));
		validate();
	}

	private GuestPersonalityErrands() {
	}

	/** The errands {@code guest} asks: its own if a fictional guest has any, else {@link #GENERIC}. */
	public static List<Errand> errands(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || entry.origin() == GuestRoster.Origin.REAL_PERSON) {
			return GENERIC;
		}
		String borrowed = guest.startsWith("test_") ? BORROWED.get(guest) : null;
		List<Errand> own = AUTHORED.get(borrowed != null ? borrowed : guest);
		return own != null ? own : GENERIC;
	}

	// ---------------------------------------------------------------- entry points

	/** Takes up today's errand (OFFERED to ACTIVE), taking nothing; the guest says what it wants. */
	public static GuestResult accept(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestPersonality.check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Component name = GuestPersonality.name(npc, guest);
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		GuestPersonalityState state = GuestPersonalityState.get(server);
		long day = GuestPersonality.day(player);
		GuestPersonalityState.Errand today = state.errand(me, guest, day);
		if (today.state() == ErrandState.ACTIVE) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.already_active", "You are already on %s's errand.", name);
		}
		if (today.state() != ErrandState.OFFERED) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.done_today", "You have run %s's errand today. Come back tomorrow.", name);
		}
		GuestResult refused = gainless(player, guest, name);
		if (refused != null) {
			return refused;
		}
		int index = pick(guest, me, day);
		Errand errand = errands(guest).get(index);
		state.putErrand(me, guest, new GuestPersonalityState.Errand(day, ErrandState.ACTIVE, index,
				BuiltInRegistries.ITEM.getKey(errand.item()).toString(), errand.count()));
		player.sendSystemMessage(GuestPersonality.says(name, errand.ask()));
		return GuestResult.done("message.jugcraft.guest.personality.errand.accepted", "%s's errand: bring %s %s.", name, errand.count(),
				new ItemStack(errand.item()).getHoverName());
	}

	/**
	 * Turns in today's active errand: refused, taking nothing, unless the player carries at least its count (plain
	 * stacks, main inventory and offhand) and a gain can be recorded; otherwise exactly the count is taken, once, the
	 * errand is done for the day and {@link #ERRAND_REWARD} is granted through {@link GuestRewards#grant}.
	 */
	public static GuestResult turnIn(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestPersonality.check(player, npc, guest);
		if (away != null) {
			return away;
		}
		Component name = GuestPersonality.name(npc, guest);
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		GuestPersonalityState state = GuestPersonalityState.get(server);
		long day = GuestPersonality.day(player);
		GuestPersonalityState.Errand today = state.errand(me, guest, day);
		if (today.state() == ErrandState.DONE) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.done_today", "You have run %s's errand today. Come back tomorrow.", name);
		}
		if (today.state() != ErrandState.ACTIVE) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.not_accepted", "Ask %s for an errand first.", name);
		}
		Item item = stored(today);
		int count = today.count();
		if (item == null || count <= 0) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.none", "%s has no errand for you today.", name);
		}
		Component wanted = new ItemStack(item).getHoverName();
		int have = count(player, item);
		if (have < count) {
			return GuestResult.refused("message.jugcraft.guest.personality.errand.too_few", "You carry %s of the %s %s needed.", have, count, wanted);
		}
		GuestResult refused = gainless(player, guest, name);
		if (refused != null) {
			return refused;
		}
		// Counted and taken with the same test in one server tick, so exactly the count leaves; then recorded done.
		take(player, item, count);
		state.putErrand(me, guest, new GuestPersonalityState.Errand(today.day(), ErrandState.DONE, today.index(), today.item(), count));
		int added = GuestRewards.grant(player, guest, ERRAND_REWARD);
		List<Errand> list = errands(guest);
		if (today.index() < list.size() && list.get(today.index()).item() == item) {
			player.sendSystemMessage(GuestPersonality.says(name, list.get(today.index()).thanks()));
		}
		return GuestResult.done("message.jugcraft.guest.personality.errand.turned_in", "You brought %s %s %s: +%s affection.", name, count, wanted, added);
	}

	/** Today's errand state for the player and guest (OFFERED when nothing is recorded); DONE for an unavailable guest. */
	public static ErrandState state(ServerPlayer player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return ErrandState.DONE;
		}
		return GuestPersonalityState.get(player.level().getServer()).errand(player.getUUID(), guest, GuestPersonality.day(player)).state();
	}

	/** The item and count today's errand asks (offered or taken up); empty when there is none or it is done. */
	public static ItemStack wanted(ServerPlayer player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return ItemStack.EMPTY;
		}
		long day = GuestPersonality.day(player);
		GuestPersonalityState.Errand today = GuestPersonalityState.get(player.level().getServer()).errand(player.getUUID(), guest, day);
		if (today.state() == ErrandState.OFFERED) {
			Errand errand = errands(guest).get(pick(guest, player.getUUID(), day));
			return new ItemStack(errand.item(), errand.count());
		}
		Item item = stored(today);
		return today.state() != ErrandState.ACTIVE || item == null || today.count() <= 0 ? ItemStack.EMPTY : new ItemStack(item, today.count());
	}

	/** Why nothing can be gained now (the shared budget used, full affection, a damaged ledger entry), or null. */
	private static @Nullable GuestResult gainless(ServerPlayer player, String guest, Component name) {
		if (GuestRewards.left(player, guest) <= 0) {
			return GuestResult.refused("message.jugcraft.guest.cap_today",
					"You have earned all the affection you can with %s today. Come back tomorrow.", name);
		}
		if (!GuestLedger.get(player.level().getServer()).canGainAffection(player.getUUID(), guest)) {
			return GuestResult.refused("message.jugcraft.guest.record_locked",
					"Your friendship with %s cannot change right now; an operator can check it.", name);
		}
		return null;
	}

	/** The index of the errand offered to {@code player} by {@code guest} on {@code day}: fixed by the three. */
	static int pick(String guest, UUID player, long day) {
		long seed = mix(hash(guest) ^ mix(player.getMostSignificantBits() + 0x632BE59BD9B4E019L));
		seed = mix(seed ^ mix(player.getLeastSignificantBits()));
		seed = mix(seed ^ mix(day + 0x2545F4914F6CDD1DL));
		return (int) Long.remainderUnsigned(seed, errands(guest).size());
	}

	// ---------------------------------------------------------------- the inventory (as GuestFavors handles it)

	/** A stack of {@code item} with nothing changed on it (no custom name, enchantment or other component). */
	private static boolean plain(ItemStack stack, Item item) {
		return !stack.isEmpty() && stack.is(item) && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
	}

	/** Plain {@code item} the player carries: the 36 main slots and the offhand (never armour). */
	static int count(ServerPlayer player, Item item) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (plain(stack, item)) {
				total += stack.getCount();
			}
		}
		ItemStack offhand = player.getOffhandItem();
		return plain(offhand, item) ? total + offhand.getCount() : total;
	}

	/** Takes {@code count} plain {@code item} from the slots {@link #count} reads; returns how many it took. */
	private static int take(ServerPlayer player, Item item, int count) {
		Inventory inventory = player.getInventory();
		int remaining = count;
		for (int slot = 0; slot < Inventory.INVENTORY_SIZE && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (plain(stack, item)) {
				int taken = Math.min(remaining, stack.getCount());
				stack.shrink(taken);
				remaining -= taken;
			}
		}
		ItemStack offhand = player.getOffhandItem();
		if (remaining > 0 && plain(offhand, item)) {
			int taken = Math.min(remaining, offhand.getCount());
			offhand.shrink(taken);
			remaining -= taken;
		}
		inventory.setChanged();
		return count - remaining;
	}

	/** The item a taken-up errand saved; null if it no longer exists. */
	private static @Nullable Item stored(GuestPersonalityState.Errand errand) {
		Identifier id = errand.item().isEmpty() ? null : Identifier.tryParse(errand.item());
		Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
		return item == null || item == Items.AIR ? null : item;
	}

	/** FNV-1a over the id's UTF-8 bytes: the same on every machine and run. */
	private static long hash(String text) {
		long hash = 0xCBF29CE484222325L;
		for (byte b : text.getBytes(StandardCharsets.UTF_8)) {
			hash ^= b & 0xFF;
			hash *= 0x100000001B3L;
		}
		return hash;
	}

	/** SplitMix64's finalizer. */
	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	// ---------------------------------------------------------------- data

	private static Errand errand(Item item, int count, Line ask, Line thanks) {
		return new Errand(item, count, ask, thanks);
	}

	private static Line say(String key, String english) {
		return GuestDialogueLines.say(key, english);
	}

	/**
	 * Checked once, when the class loads: every authored guest is an available fictional roster guest with errands;
	 * every errand asks 1 to {@link #MAX_COUNT} of a real item, names its count (digits, or "a"/"one" for one) and has
	 * unique keys of its own guest; no errand line is romantic or situational.
	 */
	private static void validate() {
		Set<String> keys = new HashSet<>();
		check("generic", GENERIC, keys);
		AUTHORED.forEach((guest, errands) -> {
			GuestRoster.Guest entry = GuestRoster.get(guest);
			if (entry == null || entry.pending() || entry.origin() != GuestRoster.Origin.FICTIONAL) {
				throw new IllegalStateException("Errands for a guest that is not an available fictional guest: " + guest);
			}
			check(guest, errands, keys);
		});
	}

	private static void check(String owner, List<Errand> errands, Set<String> keys) {
		String prefix = "message.jugcraft.guest.personality." + owner + ".errand.";
		if (errands.isEmpty()) {
			throw new IllegalStateException("No errands for " + owner);
		}
		for (Errand errand : errands) {
			if (errand.item() == Items.AIR || errand.count() < 1 || errand.count() > MAX_COUNT
					|| errand.count() > 1 && !errand.ask().english().contains(String.valueOf(errand.count()))) {
				throw new IllegalStateException("Bad errand for " + owner + ": " + errand.ask().key());
			}
			for (Line line : List.of(errand.ask(), errand.thanks())) {
				if (!line.key().startsWith(prefix) || !keys.add(line.key()) || line.romantic() || line.situational()
						|| line.english().indexOf('%') >= 0) {
					throw new IllegalStateException("Bad or repeated errand line: " + line.key());
				}
			}
		}
	}
}
