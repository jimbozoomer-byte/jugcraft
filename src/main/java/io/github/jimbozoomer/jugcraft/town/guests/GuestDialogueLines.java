package io.github.jimbozoomer.jugcraft.town.guests;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The guests' lines (worker B, November 11): gift reactions, situational banter, spouse banter and rivalry boasts, kept as
 * data. Every line is original; nothing is copied from a source work. Everything is nonsexual for everyone.
 *
 * <ul>
 * <li><b>Fictional guests</b> have lines in their own flavour ({@link #AUTHORED}).</li>
 * <li><b>Real people</b> (every {@link GuestRoster.Origin#REAL_PERSON}) all share one generic set, {@link #GENERIC}:
 * friendly, respectful and about the game world only, with no names, no invented facts, opinions, habits or quotes, and
 * no romance. A guest without lines of its own (a game-test guest) gets the same generic set.</li>
 * <li><b>Pending guests</b> get nothing: their lines are never asked for, because nothing here acts for a guest that is
 * not available.</li>
 * <li><b>Romance</b> (owner rule, 11 November 2026): a line tagged {@link Line#romantic} is spouse banter. Each one needs
 * the {@link Cue#MARRIED} cue and the other way round (checked when this class loads), and {@link GuestDialogue}
 * offers one only when the guest's roster entry {@code mayMarry()} and the ledger's {@code spouseOf} shows this player
 * and this guest married to each other. Only adult, marriage-eligible fictional guests are written any; every line of
 * a guest held from marriage is friendly companionship only.</li>
 * </ul>
 *
 * <p>Keys are {@code message.jugcraft.guest.personality.<guest>.<slot>.<n>} with English fallbacks written beside them,
 * so the coordinator's key tool copies them into en_us.json. The lines carry no format arguments: the speaker's name is
 * put in front by {@link GuestDialogue}.
 */
public final class GuestDialogueLines {
	/** What a line needs of the moment; a line with no cues fits any moment. */
	public enum Cue {
		MORNING, DAY, EVENING, NIGHT,
		CLEAR, RAIN, THUNDER,
		SPRING, SUMMER, AUTUMN, WINTER,
		HARVEST, DECEMBER,
		/** The player gave this guest a gift today. */
		GIFTED,
		/** The ledger shows this player and this guest married to each other (spouse lines only). */
		MARRIED
	}

	/** One line: its key, its English, the cues it needs (all of them), and whether it is romantic (spouse banter). */
	public record Line(String key, String english, Set<Cue> when, boolean romantic) {
		public Line {
			when = when.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(when));
		}

		/** Situational: needs at least one cue. */
		public boolean situational() {
			return !when.isEmpty();
		}

		public Component component() {
			return Component.translatableWithFallback(key, english);
		}
	}

	/** A guest's lines: gift reactions (favourite, ordinary, a rare one-off), banter (situational and spouse), a rivalry boast. */
	public record Lines(List<Line> favourite, List<Line> ordinary, List<Line> rare, List<Line> banter, Line boast) {
		public Lines {
			favourite = List.copyOf(favourite);
			ordinary = List.copyOf(ordinary);
			rare = List.copyOf(rare);
			banter = List.copyOf(banter);
		}

		/** Every line, in a fixed order. */
		public List<Line> all() {
			java.util.ArrayList<Line> all = new java.util.ArrayList<>(favourite);
			all.addAll(ordinary);
			all.addAll(rare);
			all.addAll(banter);
			all.add(boast);
			return List.copyOf(all);
		}
	}

	/** Marks a spouse line in the tables below. */
	private static final boolean ROMANTIC = true;

	/**
	 * Every real person's lines, and any guest's without its own: friendly, about the town and the moment, never naming
	 * anyone, never romantic.
	 */
	public static final Lines GENERIC = new Lines(
			List.of(
					say("message.jugcraft.guest.personality.generic.favourite.0", "Oh, this is lovely. Thank you so much!"),
					say("message.jugcraft.guest.personality.generic.favourite.1", "What a thoughtful gift. Thank you!"),
					say("message.jugcraft.guest.personality.generic.favourite.2", "This really brightens the day. Thanks!")),
			List.of(
					say("message.jugcraft.guest.personality.generic.ordinary.0", "Thank you, that's very kind."),
					say("message.jugcraft.guest.personality.generic.ordinary.1", "That's nice of you. Thanks!"),
					say("message.jugcraft.guest.personality.generic.ordinary.2", "Thanks! It's nice to see a friendly face around town.")),
			List.of(
					say("message.jugcraft.guest.personality.generic.rare.0", "What a surprise! Thank you, that made the afternoon.")),
			List.of(
					say("message.jugcraft.guest.personality.generic.banter.0", "Good to see you! The town's been lively today."),
					say("message.jugcraft.guest.personality.generic.banter.1", "The market stalls look busy. Have you been by the square?"),
					say("message.jugcraft.guest.personality.generic.banter.2", "Thanks for stopping by to say hello."),
					say("message.jugcraft.guest.personality.generic.banter.3", "Good morning! The town's just waking up.", Cue.MORNING),
					say("message.jugcraft.guest.personality.generic.banter.4", "Lovely afternoon for a walk around town.", Cue.DAY),
					say("message.jugcraft.guest.personality.generic.banter.5", "Good evening! The lanterns make the streets look lovely.", Cue.EVENING),
					say("message.jugcraft.guest.personality.generic.banter.6", "It's getting late. Stay safe out there tonight.", Cue.NIGHT),
					say("message.jugcraft.guest.personality.generic.banter.7", "Quite the rain today. A good day to stay near the fireplace.", Cue.RAIN),
					say("message.jugcraft.guest.personality.generic.banter.8", "That thunder is loud! Best to stay indoors for a while.", Cue.THUNDER),
					say("message.jugcraft.guest.personality.generic.banter.9", "Clear skies today. Great weather to be outside.", Cue.CLEAR),
					say("message.jugcraft.guest.personality.generic.banter.10", "The flowers around town are coming up nicely this spring.", Cue.SPRING),
					say("message.jugcraft.guest.personality.generic.banter.11", "Warm days like these are perfect for exploring.", Cue.SUMMER),
					say("message.jugcraft.guest.personality.generic.banter.12", "The leaves around town look wonderful this time of year.", Cue.AUTUMN),
					say("message.jugcraft.guest.personality.generic.banter.13", "Chilly out today! Keep warm.", Cue.WINTER),
					say("message.jugcraft.guest.personality.generic.banter.14", "Happy Harvest Feast! The town looks wonderful.", Cue.HARVEST),
					say("message.jugcraft.guest.personality.generic.banter.15", "The town looks so festive this month.", Cue.DECEMBER),
					say("message.jugcraft.guest.personality.generic.banter.16", "Thanks again for the gift earlier. That was kind.", Cue.GIFTED)),
			say("message.jugcraft.guest.personality.generic.boast.0", "I'll give it my best shot!"));

	/** Fictional guests' own lines, by roster id. */
	static final Map<String, Lines> AUTHORED = new LinkedHashMap<>();

	/**
	 * Test seam: a game-test guest (id starting "test_") speaking with an authored guest's lines, so tests can check the
	 * romance gate on spouse lines with a synthetic guest. Empty in play.
	 */
	static final Map<String, String> BORROWED = new ConcurrentHashMap<>();

	static {
		AUTHORED.put("power", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.power.favourite.0", "Finally, a tribute worthy of me! You may continue being useful."),
						say("message.jugcraft.guest.personality.power.favourite.1", "This is the best gift anyone has ever given, which is only right, since it is for me.")),
				List.of(
						say("message.jugcraft.guest.personality.power.ordinary.0", "I accept this. I will tell everyone I found it myself."),
						say("message.jugcraft.guest.personality.power.ordinary.1", "Hmm. Not bad. You may stand near me for a while.")),
				List.of(
						say("message.jugcraft.guest.personality.power.rare.0", "Wait. Is this a trap? It smells like a trap. ...It is mine now anyway.")),
				List.of(
						say("message.jugcraft.guest.personality.power.banter.0", "I single-handedly saved this town eleven times today. You missed all of them."),
						say("message.jugcraft.guest.personality.power.banter.1", "If anyone asks, the missing pie was taken by a very suspicious cat. Not me."),
						say("message.jugcraft.guest.personality.power.banter.2", "Mornings are an insult. Wake me when something exciting happens.", Cue.MORNING),
						say("message.jugcraft.guest.personality.power.banter.3", "The sky is crying because it cannot be as great as me.", Cue.RAIN),
						say("message.jugcraft.guest.personality.power.banter.4", "Snow is just the clouds admitting defeat. To me.", Cue.WINTER),
						say("message.jugcraft.guest.personality.power.banter.5", "Your gift has been added to my legendary hoard. It is a very small hoard. For now.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.power.boast.0", "Obviously I will win. I have never lost anything, ever, according to me.")));

		AUTHORED.put("makima", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.makima.favourite.0", "How thoughtful. I'll remember that you chose well."),
						say("message.jugcraft.guest.personality.makima.favourite.1", "This pleases me. Good instincts deserve to be noticed.")),
				List.of(
						say("message.jugcraft.guest.personality.makima.ordinary.0", "Thank you. I'll put it somewhere sensible."),
						say("message.jugcraft.guest.personality.makima.ordinary.1", "A kind gesture. The town is lucky to have you running about.")),
				List.of(
						say("message.jugcraft.guest.personality.makima.rare.0", "Interesting. Most people bring me paperwork. This is far better.")),
				List.of(
						say("message.jugcraft.guest.personality.makima.banter.0", "The town runs smoothly when everyone knows their place. Yours seems to be... everywhere."),
						say("message.jugcraft.guest.personality.makima.banter.1", "I've been watching the dogs by the gate. They keep a very orderly schedule."),
						say("message.jugcraft.guest.personality.makima.banter.2", "Evenings are for quiet films and quieter streets. This one qualifies.", Cue.EVENING),
						say("message.jugcraft.guest.personality.makima.banter.3", "Clear skies. Everyone is outside, where I can see them. How convenient.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.makima.banter.4", "Autumn. The leaves fall exactly when they're told to. I appreciate that.", Cue.AUTUMN),
						say("message.jugcraft.guest.personality.makima.banter.5", "I haven't forgotten your gift. I rarely forget anything.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.makima.spouse.0", "Welcome back, dear. I saved you the good seat for movie night.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.makima.boast.0", "I don't compete. I simply let everyone discover how it was always going to end.")));

		AUTHORED.put("reze", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.reze.favourite.0", "Oh, you remembered! Thanks, really. That's kind of you."),
						say("message.jugcraft.guest.personality.reze.favourite.1", "This is perfect! It's going straight on the café counter where everyone can see it.")),
				List.of(
						say("message.jugcraft.guest.personality.reze.ordinary.0", "Thanks! Every little thing brightens up the café corner."),
						say("message.jugcraft.guest.personality.reze.ordinary.1", "A present? You didn't have to. I'm keeping it anyway.")),
				List.of(
						say("message.jugcraft.guest.personality.reze.rare.0", "Hold on, let me check it isn't ticking. ...Just kidding. Thank you!")),
				List.of(
						say("message.jugcraft.guest.personality.reze.banter.0", "If you ever want to learn to swim, I know a quiet pond. The fish are terrible teachers."),
						say("message.jugcraft.guest.personality.reze.banter.1", "I keep the café tidy, but somehow there's always one more cup to wash."),
						say("message.jugcraft.guest.personality.reze.banter.2", "Night swims are the best. The water's calm and nobody splashes you.", Cue.NIGHT),
						say("message.jugcraft.guest.personality.reze.banter.3", "Thunder always makes me jump. A completely normal amount. Totally normal.", Cue.THUNDER),
						say("message.jugcraft.guest.personality.reze.banter.4", "Summer! Perfect weather for the pond, and for very cold drinks.", Cue.SUMMER),
						say("message.jugcraft.guest.personality.reze.banter.5", "Your gift is still on the counter. A few customers asked where they could get one.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.reze.boast.0", "I'm quicker than I look. People always find that out a little too late.")));

		AUTHORED.put("asa_mitaka", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.asa_mitaka.favourite.0", "Oh. Oh! This is actually... good. I'm writing this down so I remember to be grateful properly."),
						say("message.jugcraft.guest.personality.asa_mitaka.favourite.1", "You chose this on purpose? I... appreciate it. A normal, sensible amount.")),
				List.of(
						say("message.jugcraft.guest.personality.asa_mitaka.ordinary.0", "Thank you. I'll find a responsible place for it."),
						say("message.jugcraft.guest.personality.asa_mitaka.ordinary.1", "A gift. Right. I'm supposed to say something gracious. ...Thanks.")),
				List.of(
						say("message.jugcraft.guest.personality.asa_mitaka.rare.0", "Is this a test? Am I supposed to give something back? I have a pencil. Do you want a pencil?")),
				List.of(
						say("message.jugcraft.guest.personality.asa_mitaka.banter.0", "I made a list of conversation topics. Weather is number one. ...So. Weather."),
						say("message.jugcraft.guest.personality.asa_mitaka.banter.1", "I'm not antisocial. I'm selectively social. There's a difference."),
						say("message.jugcraft.guest.personality.asa_mitaka.banter.2", "I'm up early to study. Nobody can say I'm unprepared. Nobody says anything to me, but still.", Cue.MORNING),
						say("message.jugcraft.guest.personality.asa_mitaka.banter.3", "Rain is good. Nobody expects you to go out and be cheerful in the rain.", Cue.RAIN),
						say("message.jugcraft.guest.personality.asa_mitaka.banter.4", "Everyone keeps exchanging presents. I've drafted three possible thank-you speeches.", Cue.DECEMBER),
						say("message.jugcraft.guest.personality.asa_mitaka.banter.5", "I'm still deciding where to keep your gift. I made a shortlist. It's a long shortlist.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.asa_mitaka.boast.0", "I have prepared extensively for this. I also have a backup plan. And a backup backup plan.")));

		AUTHORED.put("himeno", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.himeno.favourite.0", "Ha! You've got good taste. Stick around, rookie, you're growing on me."),
						say("message.jugcraft.guest.personality.himeno.favourite.1", "Now this is a present. I'm telling everyone who gave it to me.")),
				List.of(
						say("message.jugcraft.guest.personality.himeno.ordinary.0", "Aw, thanks! I'll keep it in my desk next to the emergency snacks."),
						say("message.jugcraft.guest.personality.himeno.ordinary.1", "Cute! You're one of the good ones, you know that?")),
				List.of(
						say("message.jugcraft.guest.personality.himeno.rare.0", "Okay, I didn't see that coming. You keep surprising me, rookie.")),
				List.of(
						say("message.jugcraft.guest.personality.himeno.banter.0", "Rule one of the job: always know where the nearest snack is."),
						say("message.jugcraft.guest.personality.himeno.banter.1", "I keep telling the new hires to relax. Then I make them run drills anyway."),
						say("message.jugcraft.guest.personality.himeno.banter.2", "Lunch break! Officially I'm on patrol. Unofficially I'm on patrol near the bakery.", Cue.DAY),
						say("message.jugcraft.guest.personality.himeno.banter.3", "Rainy patrols are the worst. The rain always finds the gap in my collar.", Cue.RAIN),
						say("message.jugcraft.guest.personality.himeno.banter.4", "A harvest feast? Count me in. Patrol can wait until after second helpings.", Cue.HARVEST),
						say("message.jugcraft.guest.personality.himeno.banter.5", "I showed your gift to the whole office. They're jealous. As they should be.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.himeno.spouse.0", "There's my favourite person! I kept the kettle warm for you, love.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.himeno.boast.0", "I've got years of experience on you. Also I cheat a little. Kidding! Mostly.")));

		AUTHORED.put("tokai_teio", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.tokai_teio.favourite.0", "Wow! You really know what a champion likes! Thank you!"),
						say("message.jugcraft.guest.personality.tokai_teio.favourite.1", "This is the best! I'm going to do a victory lap just for this!")),
				List.of(
						say("message.jugcraft.guest.personality.tokai_teio.ordinary.0", "Thanks! A future legend appreciates her fans!"),
						say("message.jugcraft.guest.personality.tokai_teio.ordinary.1", "A present! I'll put it next to my trophies. All of them.")),
				List.of(
						say("message.jugcraft.guest.personality.tokai_teio.rare.0", "Huh? Is this a medal? It's not a medal. I'll pretend it's a medal!")),
				List.of(
						say("message.jugcraft.guest.personality.tokai_teio.banter.0", "I'm going to be the strongest runner ever. This whole town will cheer for me!"),
						say("message.jugcraft.guest.personality.tokai_teio.banter.1", "Want to see my special step? Watch closely! ...Okay, you blinked."),
						say("message.jugcraft.guest.personality.tokai_teio.banter.2", "Morning training done already! Bet you're impressed.", Cue.MORNING),
						say("message.jugcraft.guest.personality.tokai_teio.banter.3", "Perfect running weather! The track is calling my name!", Cue.CLEAR),
						say("message.jugcraft.guest.personality.tokai_teio.banter.4", "Spring is when champions are made! Also flowers. Mostly champions.", Cue.SPRING),
						say("message.jugcraft.guest.personality.tokai_teio.banter.5", "Your gift gave me so much energy I ran three extra laps!", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.tokai_teio.boast.0", "Get ready! I'll even let you watch me win up close!")));

		AUTHORED.put("agnes_tachyon", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.agnes_tachyon.favourite.0", "Fascinating! A specimen of exceptional quality. You have my scientific gratitude."),
						say("message.jugcraft.guest.personality.agnes_tachyon.favourite.1", "Excellent. This will be put to rigorous experimental use. Probably in tea.")),
				List.of(
						say("message.jugcraft.guest.personality.agnes_tachyon.ordinary.0", "Thank you, test subject. I mean, friend. I mean... thank you."),
						say("message.jugcraft.guest.personality.agnes_tachyon.ordinary.1", "Noted and catalogued. Your contribution to science is appreciated.")),
				List.of(
						say("message.jugcraft.guest.personality.agnes_tachyon.rare.0", "Hmm, it's glowing. Is it supposed to glow? Wait, that's just my lab coat. Carry on.")),
				List.of(
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.0", "Would you like a drink that is faintly luminous? For science? No obligation. Some obligation."),
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.1", "My latest hypothesis: the town's chickens are plotting. Further research is required."),
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.2", "Night is ideal for experiments. Fewer witnesses. I mean, fewer distractions.", Cue.NIGHT),
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.3", "A thunderstorm! Free electricity! Quick, where did I put my copper?", Cue.THUNDER),
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.4", "Winter slows every reaction. Even mine. Especially mine before breakfast.", Cue.WINTER),
						say("message.jugcraft.guest.personality.agnes_tachyon.banter.5", "Your gift has been entered into the data. The data is very pleased.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.agnes_tachyon.boast.0", "I have calculated the result in advance. The calculation says I win. Science is never wrong.")));

		AUTHORED.put("gold_ship", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.gold_ship.favourite.0", "Whoa, this is exactly what I needed for my secret plan! What plan? Can't say!"),
						say("message.jugcraft.guest.personality.gold_ship.favourite.1", "You get it! You really get it! This is going in my time capsule!")),
				List.of(
						say("message.jugcraft.guest.personality.gold_ship.ordinary.0", "Thanks! I'll trade it to a passing fisherman for a very confused fish."),
						say("message.jugcraft.guest.personality.gold_ship.ordinary.1", "Ooh, a present! I'll wear it as a hat. Don't stop me.")),
				List.of(
						say("message.jugcraft.guest.personality.gold_ship.rare.0", "A gift? Then I declare today a holiday! Nobody else has agreed yet!")),
				List.of(
						say("message.jugcraft.guest.personality.gold_ship.banter.0", "Did you know the moon is just a really shy sun? I made that up, but it sounds right."),
						say("message.jugcraft.guest.personality.gold_ship.banter.1", "I'm building a raft to sail to the edge of the world. Need a deckhand?"),
						say("message.jugcraft.guest.personality.gold_ship.banter.2", "Evenings are perfect for climbing onto rooftops and yelling encouragement at clouds.", Cue.EVENING),
						say("message.jugcraft.guest.personality.gold_ship.banter.3", "Rain means puddles, and puddles mean jumping contests. I'm undefeated. I'm also the only entrant.", Cue.RAIN),
						say("message.jugcraft.guest.personality.gold_ship.banter.4", "Summer! Time to bury something on the beach and forget where. Tradition!", Cue.SUMMER),
						say("message.jugcraft.guest.personality.gold_ship.banter.5", "I told the cows about your gift. They seemed impressed. One mooed.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.gold_ship.boast.0", "I'm going to win, then lose on purpose, then win again. You'll never see it coming!")));

		AUTHORED.put("oguri_cap", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.oguri_cap.favourite.0", "Thank you. This is wonderful. Do you have more? ...Not that I'm asking."),
						say("message.jugcraft.guest.personality.oguri_cap.favourite.1", "This is a very good gift. I'm already looking forward to dinner.")),
				List.of(
						say("message.jugcraft.guest.personality.oguri_cap.ordinary.0", "Thank you. I'll treasure it. Is it edible? ...Just checking."),
						say("message.jugcraft.guest.personality.oguri_cap.ordinary.1", "Thanks. You're very kind. Kinder than the bakery's opening hours.")),
				List.of(
						say("message.jugcraft.guest.personality.oguri_cap.rare.0", "A gift that isn't a snack? That's new. I'll think about it while I eat.")),
				List.of(
						say("message.jugcraft.guest.personality.oguri_cap.banter.0", "I had a small lunch today. Only five plates. The baker looked relieved."),
						say("message.jugcraft.guest.personality.oguri_cap.banter.1", "Running and eating. Eating and running. It's a good life."),
						say("message.jugcraft.guest.personality.oguri_cap.banter.2", "The bakery puts out fresh bread around now. I'm not hovering. I'm stretching nearby.", Cue.DAY),
						say("message.jugcraft.guest.personality.oguri_cap.banter.3", "Good weather for running. And for picnics. Mostly picnics.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.oguri_cap.banter.4", "A harvest feast? I've been training for this my whole life.", Cue.HARVEST),
						say("message.jugcraft.guest.personality.oguri_cap.banter.5", "Thank you for earlier. I'm still thinking about it. Was it a snack?", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.oguri_cap.boast.0", "I'll do my best. And then I'll have a snack.")));

		AUTHORED.put("special_week", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.special_week.favourite.0", "Wow, thank you! This is just like the things back home! I'm so happy!"),
						say("message.jugcraft.guest.personality.special_week.favourite.1", "You're so kind! I'll do my very best in my next race, just for this!")),
				List.of(
						say("message.jugcraft.guest.personality.special_week.ordinary.0", "Thank you so much! I'll keep it safe in my locker!"),
						say("message.jugcraft.guest.personality.special_week.ordinary.1", "A present? Thank you! I'm going to tell everyone!")),
				List.of(
						say("message.jugcraft.guest.personality.special_week.rare.0", "Is this... a lucky charm? It must be! I'm going to win everything now!")),
				List.of(
						say("message.jugcraft.guest.personality.special_week.banter.0", "I came to town to become the best in the whole country! And to try all the food!"),
						say("message.jugcraft.guest.personality.special_week.banter.1", "Everyone here is so friendly! It's like the countryside, but with more stairs."),
						say("message.jugcraft.guest.personality.special_week.banter.2", "Morning run, then breakfast, then second breakfast! That's the schedule!", Cue.MORNING),
						say("message.jugcraft.guest.personality.special_week.banter.3", "Running in the rain is tough, but it'll make me stronger! Achoo!", Cue.RAIN),
						say("message.jugcraft.guest.personality.special_week.banter.4", "Autumn means roast potatoes! The best season for a hungry runner!", Cue.AUTUMN),
						say("message.jugcraft.guest.personality.special_week.banter.5", "I'm still so happy about your gift! I told the whole track team!", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.special_week.boast.0", "I'll give it everything I've got! I promised everyone back home I would!")));

		AUTHORED.put("rei_ayanami", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.rei_ayanami.favourite.0", "...Thank you. I think I like this."),
						say("message.jugcraft.guest.personality.rei_ayanami.favourite.1", "It is... nice. I will keep it.")),
				List.of(
						say("message.jugcraft.guest.personality.rei_ayanami.ordinary.0", "Thank you."),
						say("message.jugcraft.guest.personality.rei_ayanami.ordinary.1", "I see. A gift. ...Thank you.")),
				List.of(
						say("message.jugcraft.guest.personality.rei_ayanami.rare.0", "I do not know what to say. So I will say thank you. Twice. Thank you.")),
				List.of(
						say("message.jugcraft.guest.personality.rei_ayanami.banter.0", "...The town is quiet today. I don't mind."),
						say("message.jugcraft.guest.personality.rei_ayanami.banter.1", "People say I am hard to read. I am reading a book. That is all."),
						say("message.jugcraft.guest.personality.rei_ayanami.banter.2", "The moon is bright tonight. ...That is all I wanted to say.", Cue.NIGHT),
						say("message.jugcraft.guest.personality.rei_ayanami.banter.3", "I like the sound of rain. It asks nothing of anyone.", Cue.RAIN),
						say("message.jugcraft.guest.personality.rei_ayanami.banter.4", "Snow. Everything becomes the same colour. It is calm.", Cue.WINTER),
						say("message.jugcraft.guest.personality.rei_ayanami.banter.5", "...I still have your gift. I looked at it this morning.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.rei_ayanami.boast.0", "...I will try. That is enough.")));

		AUTHORED.put("asuka_langley", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.asuka_langley.favourite.0", "Hmph! Well, at least you have taste. I suppose I'll accept it."),
						say("message.jugcraft.guest.personality.asuka_langley.favourite.1", "Finally, someone who knows quality! Don't let it go to your head.")),
				List.of(
						say("message.jugcraft.guest.personality.asuka_langley.ordinary.0", "I guess it's fine. Don't expect me to gush about it."),
						say("message.jugcraft.guest.personality.asuka_langley.ordinary.1", "...Thanks. Whatever. It's not like I was waiting for a present.")),
				List.of(
						say("message.jugcraft.guest.personality.asuka_langley.rare.0", "What is this? Is this a joke? ...Fine. It's kind of nice. Don't tell anyone I said that.")),
				List.of(
						say("message.jugcraft.guest.personality.asuka_langley.banter.0", "I'm the best at everything in this town. It's not bragging if it's true."),
						say("message.jugcraft.guest.personality.asuka_langley.banter.1", "Ugh, everyone here is so slow! Try to keep up for once!"),
						say("message.jugcraft.guest.personality.asuka_langley.banter.2", "Already up and done with my training. Unlike some people.", Cue.MORNING),
						say("message.jugcraft.guest.personality.asuka_langley.banter.3", "Thunder? Ha! It's not even that loud. I'm not flinching. You're flinching.", Cue.THUNDER),
						say("message.jugcraft.guest.personality.asuka_langley.banter.4", "Summer means everyone gets to watch me win at absolutely everything outdoors.", Cue.SUMMER),
						say("message.jugcraft.guest.personality.asuka_langley.banter.5", "Your gift's not bad. I'm only mentioning it so you don't think I forgot.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.asuka_langley.boast.0", "This isn't even a contest. I'm going to win so hard it's embarrassing. For them.")));

		AUTHORED.put("misato_katsuragi", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.misato_katsuragi.favourite.0", "Oh, you're a star! This is going straight onto the kitchen table. The clean bit."),
						say("message.jugcraft.guest.personality.misato_katsuragi.favourite.1", "Now that's a thoughtful gift! You're officially my favourite visitor today.")),
				List.of(
						say("message.jugcraft.guest.personality.misato_katsuragi.ordinary.0", "Thanks! I'll put it somewhere safe. Somewhere I'll definitely remember."),
						say("message.jugcraft.guest.personality.misato_katsuragi.ordinary.1", "Aw, you shouldn't have! But I'm glad you did.")),
				List.of(
						say("message.jugcraft.guest.personality.misato_katsuragi.rare.0", "My penguin would be thrilled with this. I mean, I would. We both would. He's very opinionated.")),
				List.of(
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.0", "My cooking? It's... an adventure. Bring your own spoon and your own courage."),
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.1", "Work hard, then relax harder. That's my motto. Mostly the second half."),
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.2", "Evening! Finally I can kick off my boots and pretend paperwork doesn't exist.", Cue.EVENING),
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.3", "Rain again? Perfect excuse for a cosy night in with instant curry.", Cue.RAIN),
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.4", "December! I've already lost three rolls of wrapping paper. They're in the apartment somewhere.", Cue.DECEMBER),
						say("message.jugcraft.guest.personality.misato_katsuragi.banter.5", "Thanks again for earlier! You've made my day a whole lot brighter.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.misato_katsuragi.spouse.0", "Welcome home, dear! Dinner's ready. Well, dinner's... attempted.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.misato_katsuragi.boast.0", "I'll win this with tactics! And if tactics fail, enthusiasm!")));

		AUTHORED.put("frieren", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.frieren.favourite.0", "Oh. This is nice. I'll remember it for a few centuries, at least."),
						say("message.jugcraft.guest.personality.frieren.favourite.1", "Thank you. This is better than most of the spells I've collected. Not all of them.")),
				List.of(
						say("message.jugcraft.guest.personality.frieren.ordinary.0", "Thank you. I'll put it with my other things. I have a lot of other things."),
						say("message.jugcraft.guest.personality.frieren.ordinary.1", "A gift? That's kind. Humans are always so thoughtful about small things.")),
				List.of(
						say("message.jugcraft.guest.personality.frieren.rare.0", "Is this a grimoire? No? Then it's still a nice gift. Slightly less exciting.")),
				List.of(
						say("message.jugcraft.guest.personality.frieren.banter.0", "I learned a spell that polishes the rust off old statues. Most useful thing I've found all decade."),
						say("message.jugcraft.guest.personality.frieren.banter.1", "That chest by the well looks suspicious. I'm going to check inside it. I'm sure it's fine."),
						say("message.jugcraft.guest.personality.frieren.banter.2", "Is it morning already? I was planning to wake up next week.", Cue.MORNING),
						say("message.jugcraft.guest.personality.frieren.banter.3", "Clear skies. A good day to walk somewhere slowly and arrive eventually.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.frieren.banter.4", "Spring already? It feels like last spring was yesterday. It was eleven months ago? Huh.", Cue.SPRING),
						say("message.jugcraft.guest.personality.frieren.banter.5", "I still have your gift. It's in my bag next to a very old sandwich. I should check on that sandwich.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.frieren.spouse.0", "Welcome back, dear. I'd like to spend the next century or so of afternoons just like this.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.frieren.boast.0", "I've had a thousand years of practice. Most of it was napping, but still.")));

		AUTHORED.put("rem", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.rem.favourite.0", "Thank you so much! Rem will treasure this and keep it perfectly polished."),
						say("message.jugcraft.guest.personality.rem.favourite.1", "This is wonderful! Rem is very happy you thought of her.")),
				List.of(
						say("message.jugcraft.guest.personality.rem.ordinary.0", "Thank you! Rem will find a tidy place for it right away."),
						say("message.jugcraft.guest.personality.rem.ordinary.1", "How kind of you! Rem appreciates it.")),
				List.of(
						say("message.jugcraft.guest.personality.rem.rare.0", "Oh! This is unexpected. Rem will dust it every morning. And every evening.")),
				List.of(
						say("message.jugcraft.guest.personality.rem.banter.0", "Rem has swept the square twice today. It's dusty again already. Rem will sweep it a third time."),
						say("message.jugcraft.guest.personality.rem.banter.1", "If anything ever needs mending, sweeping or folding, Rem is happy to help!"),
						say("message.jugcraft.guest.personality.rem.banter.2", "Good morning! Rem has been up since before dawn making breakfast for everyone.", Cue.MORNING),
						say("message.jugcraft.guest.personality.rem.banter.3", "Rain means muddy boots. Rem has prepared a very large doormat.", Cue.RAIN),
						say("message.jugcraft.guest.personality.rem.banter.4", "Rem is knitting scarves for the whole town this winter. Even the guards. Especially the guards.", Cue.WINTER),
						say("message.jugcraft.guest.personality.rem.banter.5", "Rem is still smiling about your gift. Thank you again!", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.rem.boast.0", "Rem will do her very best. Rem always does!")));

		AUTHORED.put("megumin", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.megumin.favourite.0", "Yes! A gift worthy of the greatest mage of explosions! I accept with full dramatic flair!"),
						say("message.jugcraft.guest.personality.megumin.favourite.1", "This! This is magnificent! I shall name it and give it a tragic backstory!")),
				List.of(
						say("message.jugcraft.guest.personality.megumin.ordinary.0", "Hmm. It doesn't explode. But it is still a fine tribute."),
						say("message.jugcraft.guest.personality.megumin.ordinary.1", "I accept this offering. You may now witness my dramatic pose.")),
				List.of(
						say("message.jugcraft.guest.personality.megumin.rare.0", "Wait. Is this... gunpowder-adjacent? No? Pity. Still, thank you!")),
				List.of(
						say("message.jugcraft.guest.personality.megumin.banter.0", "There is only one true magic, and it goes very loudly boom. I will not be taking questions."),
						say("message.jugcraft.guest.personality.megumin.banter.1", "I cast one spell a day. Today's spell was at a rock. The rock lost."),
						say("message.jugcraft.guest.personality.megumin.banter.2", "Sunset is the most dramatic time of day. I always pose against it. Always.", Cue.EVENING),
						say("message.jugcraft.guest.personality.megumin.banter.3", "Thunder! The sky is trying to imitate me. It's a decent effort.", Cue.THUNDER),
						say("message.jugcraft.guest.personality.megumin.banter.4", "December is the perfect season for fireworks. Fireworks are tiny tributes to my art.", Cue.DECEMBER),
						say("message.jugcraft.guest.personality.megumin.banter.5", "Your gift sits proudly beside my staff. That is the highest honour I can give.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.megumin.boast.0", "Prepare yourselves! This contest will end with a bang. Metaphorically. Probably.")));

		AUTHORED.put("zero_two", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.zero_two.favourite.0", "Ooh, sweet! You've got good instincts. Thanks!"),
						say("message.jugcraft.guest.personality.zero_two.favourite.1", "Now this is a gift! I'm sharing it with absolutely no one.")),
				List.of(
						say("message.jugcraft.guest.personality.zero_two.ordinary.0", "Thanks! I'll find a use for it. Maybe. Eventually."),
						say("message.jugcraft.guest.personality.zero_two.ordinary.1", "A present? Neat. I'll keep it.")),
				List.of(
						say("message.jugcraft.guest.personality.zero_two.rare.0", "Huh, you're full of surprises. I like surprises. Mostly.")),
				List.of(
						say("message.jugcraft.guest.personality.zero_two.banter.0", "Honey makes everything better. Bread, apples, even boring afternoons."),
						say("message.jugcraft.guest.personality.zero_two.banter.1", "People stare at my horns. I stare back. It's a fun game."),
						say("message.jugcraft.guest.personality.zero_two.banter.2", "Midday. The perfect time to find a sunny roof and do nothing at all.", Cue.DAY),
						say("message.jugcraft.guest.personality.zero_two.banter.3", "Blue sky everywhere! Makes me want to fly something fast.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.zero_two.banter.4", "Spring flowers smell like honey. Okay, not really. But they should.", Cue.SPRING),
						say("message.jugcraft.guest.personality.zero_two.banter.5", "Your gift earlier was nice. You get a gold star from me.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.zero_two.boast.0", "Race me? Sure. Just don't blink, or you'll miss me winning.")));

		AUTHORED.put("rin_ishtar", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.rin_ishtar.favourite.0", "Well, you have a good eye. I'll accept this. It's not like I'm thrilled. ...Fine, I'm a little thrilled."),
						say("message.jugcraft.guest.personality.rin_ishtar.favourite.1", "A gift of real quality! Fine, I admit it, you chose well.")),
				List.of(
						say("message.jugcraft.guest.personality.rin_ishtar.ordinary.0", "Thanks. I'll put it somewhere sensible. Not near my gems."),
						say("message.jugcraft.guest.personality.rin_ishtar.ordinary.1", "Hmph. It's nice. Don't make a big deal about it.")),
				List.of(
						say("message.jugcraft.guest.personality.rin_ishtar.rare.0", "Is this worth anything? I'm just asking! Purely for appraisal purposes!")),
				List.of(
						say("message.jugcraft.guest.personality.rin_ishtar.banter.0", "Magic is expensive, you know. Every gem I use is a gem I'll never see again."),
						say("message.jugcraft.guest.personality.rin_ishtar.banter.1", "Machines? I can manage machines. I just prefer that they don't make noises at me."),
						say("message.jugcraft.guest.personality.rin_ishtar.banter.2", "Evening is when the real study starts. Mornings are for... recovering.", Cue.EVENING),
						say("message.jugcraft.guest.personality.rin_ishtar.banter.3", "Rain ruins my hair and my schedule. I'm only upset about one of those.", Cue.RAIN),
						say("message.jugcraft.guest.personality.rin_ishtar.banter.4", "Autumn leaves are red. Good colour. The best colour, actually.", Cue.AUTUMN),
						say("message.jugcraft.guest.personality.rin_ishtar.banter.5", "About your gift... it was nice. Just so you know. I'm only saying it once.", Cue.GIFTED)),
				say("message.jugcraft.guest.personality.rin_ishtar.boast.0", "Of course I'll win. I've planned for every outcome. Except losing. That isn't an outcome.")));

		AUTHORED.put("tifa_lockhart", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.tifa_lockhart.favourite.0", "Oh, this is lovely! Thank you, really. It's going behind the counter where I can see it."),
						say("message.jugcraft.guest.personality.tifa_lockhart.favourite.1", "That's so thoughtful. This made my whole afternoon.")),
				List.of(
						say("message.jugcraft.guest.personality.tifa_lockhart.ordinary.0", "Thanks! I'll put it to good use."),
						say("message.jugcraft.guest.personality.tifa_lockhart.ordinary.1", "That's kind of you. Come by when the stew's on, there's always enough.")),
				List.of(
						say("message.jugcraft.guest.personality.tifa_lockhart.rare.0", "Ha! Not what I expected, but I like it. You're full of surprises.")),
				List.of(
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.0", "Training in the morning, cooking in the evening. Keeps the hands busy and the heart steady."),
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.1", "If anyone in town gives you trouble, let me know. I'll have a friendly word. A very firm friendly word."),
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.2", "The evening rush is coming. Everyone in town gets hungry at once.", Cue.EVENING),
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.3", "Rainy days bring everyone indoors. More customers, more stories.", Cue.RAIN),
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.4", "Harvest time means fresh ingredients. I'm already planning three new recipes.", Cue.HARVEST),
						say("message.jugcraft.guest.personality.tifa_lockhart.banter.5", "Thanks again for the gift. You've got a good heart.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.tifa_lockhart.spouse.0", "Welcome home, love. I saved you a plate and a seat by the window.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.tifa_lockhart.boast.0", "I don't like showing off. But I don't like losing either.")));

		AUTHORED.put("yorha_2b", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.yorha_2b.favourite.0", "...This is acceptable. More than acceptable. Thank you."),
						say("message.jugcraft.guest.personality.yorha_2b.favourite.1", "I did not expect this. It is... appreciated.")),
				List.of(
						say("message.jugcraft.guest.personality.yorha_2b.ordinary.0", "Received. Thank you."),
						say("message.jugcraft.guest.personality.yorha_2b.ordinary.1", "I will store this safely. Thank you.")),
				List.of(
						say("message.jugcraft.guest.personality.yorha_2b.rare.0", "Analysing... This item has no tactical value. I will keep it anyway.")),
				List.of(
						say("message.jugcraft.guest.personality.yorha_2b.banter.0", "Report: the town perimeter is secure. The ducks remain unaccounted for."),
						say("message.jugcraft.guest.personality.yorha_2b.banter.1", "I do not require rest. I am resting now only to be polite."),
						say("message.jugcraft.guest.personality.yorha_2b.banter.2", "Night patrol is quiet. I prefer it that way.", Cue.NIGHT),
						say("message.jugcraft.guest.personality.yorha_2b.banter.3", "Thunderstorm detected. Recommend indoor activities. I will remain outside.", Cue.THUNDER),
						say("message.jugcraft.guest.personality.yorha_2b.banter.4", "Cold weather reduces efficiency. Not mine. Everyone else's.", Cue.WINTER),
						say("message.jugcraft.guest.personality.yorha_2b.banter.5", "I have kept your gift. It is in a secure location. I check on it.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.yorha_2b.spouse.0", "Welcome back, dear. Status: improved now that you are here.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.yorha_2b.boast.0", "Victory probability: high. Proceeding.")));

		AUTHORED.put("chun_li", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.chun_li.favourite.0", "What a thoughtful gift! You're welcome at my training sessions anytime."),
						say("message.jugcraft.guest.personality.chun_li.favourite.1", "This is wonderful, thank you! It's going right next to my training gear.")),
				List.of(
						say("message.jugcraft.guest.personality.chun_li.ordinary.0", "Thank you! Every kindness makes the town stronger."),
						say("message.jugcraft.guest.personality.chun_li.ordinary.1", "Thanks! I'll put it to good use after practice.")),
				List.of(
						say("message.jugcraft.guest.personality.chun_li.rare.0", "Ha! You caught me off guard. That doesn't happen often!")),
				List.of(
						say("message.jugcraft.guest.personality.chun_li.banter.0", "A thousand kicks before breakfast keeps the mind sharp. And the breakfast well deserved."),
						say("message.jugcraft.guest.personality.chun_li.banter.1", "I've been investigating who keeps moving the market stalls overnight. The trail leads to a goat."),
						say("message.jugcraft.guest.personality.chun_li.banter.2", "Morning drills are done. Want to join tomorrow? We start before sunrise.", Cue.MORNING),
						say("message.jugcraft.guest.personality.chun_li.banter.3", "Clear skies! Perfect for training in the square.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.chun_li.banter.4", "Spring air is the best for running drills through the orchard.", Cue.SPRING),
						say("message.jugcraft.guest.personality.chun_li.banter.5", "Thanks again for your gift. It's been a lucky charm all day.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.chun_li.spouse.0", "Welcome back, dear! I kept the tea warm for you after practice.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.chun_li.boast.0", "Discipline beats bravado. Every time. Shall we?")));

		AUTHORED.put("ada_wong", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.ada_wong.favourite.0", "Well, well. You've done your research. I'm impressed."),
						say("message.jugcraft.guest.personality.ada_wong.favourite.1", "Nice choice. I'll keep this one close.")),
				List.of(
						say("message.jugcraft.guest.personality.ada_wong.ordinary.0", "Thanks. I'll find somewhere discreet for it."),
						say("message.jugcraft.guest.personality.ada_wong.ordinary.1", "A gift? You're full of surprises. So am I.")),
				List.of(
						say("message.jugcraft.guest.personality.ada_wong.rare.0", "Interesting. I'll pretend I didn't already know you were bringing this.")),
				List.of(
						say("message.jugcraft.guest.personality.ada_wong.banter.0", "I'm not here. If anyone asks, you never saw me. Except you're seeing me. Awkward."),
						say("message.jugcraft.guest.personality.ada_wong.banter.1", "Every town has secrets. This one mostly has very secretive chickens."),
						say("message.jugcraft.guest.personality.ada_wong.banter.2", "Night's when the interesting work happens. And the quiet.", Cue.NIGHT),
						say("message.jugcraft.guest.personality.ada_wong.banter.3", "Rain covers footsteps. Very convenient. For reasons.", Cue.RAIN),
						say("message.jugcraft.guest.personality.ada_wong.banter.4", "December. Everyone's so busy wrapping presents that nobody notices who's watching. Useful.", Cue.DECEMBER),
						say("message.jugcraft.guest.personality.ada_wong.banter.5", "I've kept your gift. Classified location. Very safe.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.ada_wong.spouse.0", "Welcome back, dear. I cleared my schedule for you. Don't tell anyone.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.ada_wong.boast.0", "I don't play fair. I play smart. There's a difference.")));

		AUTHORED.put("lara_croft", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.lara_croft.favourite.0", "Brilliant! A genuine find. You've got the instincts of an explorer."),
						say("message.jugcraft.guest.personality.lara_croft.favourite.1", "Oh, this is lovely. It's going straight into my collection, properly labelled.")),
				List.of(
						say("message.jugcraft.guest.personality.lara_croft.ordinary.0", "Thank you! I'll catalogue it with the rest of my finds."),
						say("message.jugcraft.guest.personality.lara_croft.ordinary.1", "Cheers! Every artefact tells a story. This one says you're thoughtful.")),
				List.of(
						say("message.jugcraft.guest.personality.lara_croft.rare.0", "Hold on, there might be an inscription. ...No. Still, splendid gift!")),
				List.of(
						say("message.jugcraft.guest.personality.lara_croft.banter.0", "There's an old ruin under the north wall, I'm sure of it. I just need a very long ladder."),
						say("message.jugcraft.guest.personality.lara_croft.banter.1", "That chest in the square has a hidden compartment. I can tell by the hinges."),
						say("message.jugcraft.guest.personality.lara_croft.banter.2", "Midday. Shadows are short. Perfect for reading carvings on the old stones.", Cue.DAY),
						say("message.jugcraft.guest.personality.lara_croft.banter.3", "A thunderstorm! Every tomb I've ever explored had one of these going on outside.", Cue.THUNDER),
						say("message.jugcraft.guest.personality.lara_croft.banter.4", "Autumn is digging season. The ground's soft and the mosquitoes have given up.", Cue.AUTUMN),
						say("message.jugcraft.guest.personality.lara_croft.banter.5", "Your gift's on my mantel now, next to a very suspicious idol. They get along.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.lara_croft.spouse.0", "Welcome home, love. I've got a map, two lanterns and a free weekend for us.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.lara_croft.boast.0", "I've outrun boulders. I can outrun this.")));

		AUTHORED.put("kafka", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.kafka.favourite.0", "How lovely. You've a fine ear for what suits me."),
						say("message.jugcraft.guest.personality.kafka.favourite.1", "Exquisite. It goes perfectly with my coat collection.")),
				List.of(
						say("message.jugcraft.guest.personality.kafka.ordinary.0", "Thank you. A pleasant note in an otherwise quiet afternoon."),
						say("message.jugcraft.guest.personality.kafka.ordinary.1", "How kind. I'll find it a place on the shelf by the gramophone.")),
				List.of(
						say("message.jugcraft.guest.personality.kafka.rare.0", "Oh? That wasn't in the script. I approve.")),
				List.of(
						say("message.jugcraft.guest.personality.kafka.banter.0", "Everything in this town follows a script. I simply read ahead."),
						say("message.jugcraft.guest.personality.kafka.banter.1", "Listen. Hear that? Even the wind here keeps time. Lovely."),
						say("message.jugcraft.guest.personality.kafka.banter.2", "Evenings are made for opera. Or for quiet strolls that feel like opera.", Cue.EVENING),
						say("message.jugcraft.guest.personality.kafka.banter.3", "Rain on the roof. Nature's percussion section.", Cue.RAIN),
						say("message.jugcraft.guest.personality.kafka.banter.4", "Winter suits me. Long coats are finally appropriate everywhere.", Cue.WINTER),
						say("message.jugcraft.guest.personality.kafka.banter.5", "Your gift hasn't left my thoughts. That's rarer than you think.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.kafka.spouse.0", "Welcome back, dear. I saved the best seat beside me for tonight's music.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.kafka.boast.0", "I already know how this ends. Let's see if you do.")));

		AUTHORED.put("miyabi", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.miyabi.favourite.0", "...This is good. Thank you. I will keep it near my sword."),
						say("message.jugcraft.guest.personality.miyabi.favourite.1", "A fine choice. You have a calm eye.")),
				List.of(
						say("message.jugcraft.guest.personality.miyabi.ordinary.0", "Thank you. I will remember this."),
						say("message.jugcraft.guest.personality.miyabi.ordinary.1", "Ah. A gift. ...Thank you. Where was I going?")),
				List.of(
						say("message.jugcraft.guest.personality.miyabi.rare.0", "Hm. Unexpected. Like a cut that lands before you see it. Thank you.")),
				List.of(
						say("message.jugcraft.guest.personality.miyabi.banter.0", "I went for a walk to the market. I am now at the bakery. I will consider this a success."),
						say("message.jugcraft.guest.personality.miyabi.banter.1", "A blade is calm until the moment it isn't. I aim to be like that. Today I am mostly calm."),
						say("message.jugcraft.guest.personality.miyabi.banter.2", "Morning practice. One hundred cuts at the air. The air has surrendered.", Cue.MORNING),
						say("message.jugcraft.guest.personality.miyabi.banter.3", "The sky is clear. A good day for a slow walk and a fast draw.", Cue.CLEAR),
						say("message.jugcraft.guest.personality.miyabi.banter.4", "Snow on the rooftops. Quiet. I like quiet.", Cue.WINTER),
						say("message.jugcraft.guest.personality.miyabi.banter.5", "Your gift. I still have it. I did not lose it. I checked twice.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.miyabi.spouse.0", "Welcome back, dear. I waited for you by the gate. I only got lost once.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.miyabi.boast.0", "I will not hurry. I will simply arrive first.")));

		AUTHORED.put("nicole_demara", new Lines(
				List.of(
						say("message.jugcraft.guest.personality.nicole_demara.favourite.0", "Ooh, now that's a gift with resale value! I mean, sentimental value! Thanks!"),
						say("message.jugcraft.guest.personality.nicole_demara.favourite.1", "You're the best client I've ever had. And you're not even paying me!")),
				List.of(
						say("message.jugcraft.guest.personality.nicole_demara.ordinary.0", "Thanks! Everything's an investment if you think about it hard enough."),
						say("message.jugcraft.guest.personality.nicole_demara.ordinary.1", "A freebie? My favourite price!")),
				List.of(
						say("message.jugcraft.guest.personality.nicole_demara.rare.0", "Hold on, is this a bribe? Because I accept. Wait, it's a gift? Even better!")),
				List.of(
						say("message.jugcraft.guest.personality.nicole_demara.banter.0", "Business is booming! By booming I mean we're broke, but loudly."),
						say("message.jugcraft.guest.personality.nicole_demara.banter.1", "I have a plan to get rich. It has seven steps. Step one is borrowing someone else's plan."),
						say("message.jugcraft.guest.personality.nicole_demara.banter.2", "Midday means lunch, and lunch means finding someone else to pay for it.", Cue.DAY),
						say("message.jugcraft.guest.personality.nicole_demara.banter.3", "Rain! Perfect for selling umbrellas. Anyone have umbrellas? Anyone?", Cue.RAIN),
						say("message.jugcraft.guest.personality.nicole_demara.banter.4", "December is great for business. Everyone's buying presents. Nobody's buying from me, but still.", Cue.DECEMBER),
						say("message.jugcraft.guest.personality.nicole_demara.banter.5", "Your gift is in my safe. The only thing in my safe, actually.", Cue.GIFTED),
						say(ROMANTIC, "message.jugcraft.guest.personality.nicole_demara.spouse.0", "Welcome home, dear! I balanced the books. They're still sad, but I'm happy you're here.", Cue.MARRIED)),
				say("message.jugcraft.guest.personality.nicole_demara.boast.0", "Winner takes all the credit! Credit's free, so I'll take extra.")));

		validate();
	}

	private GuestDialogueLines() {
	}

	/**
	 * The lines {@code guest} speaks: its own if it is a fictional guest with authored lines, else {@link #GENERIC} (every
	 * real person, and any guest without its own). Callers act only for available guests, so pending ones never speak.
	 */
	public static Lines lines(String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || entry.origin() == GuestRoster.Origin.REAL_PERSON) {
			return GENERIC;
		}
		String borrowed = guest.startsWith("test_") ? BORROWED.get(guest) : null;
		Lines own = AUTHORED.get(borrowed != null ? borrowed : guest);
		return own != null ? own : GENERIC;
	}

	/** An ordinary line. */
	static Line say(String key, String english, Cue... when) {
		return new Line(key, english, when.length == 0 ? Set.of() : EnumSet.of(when[0], when), false);
	}

	/** A line that is {@code romantic} (spouse banter) or not. */
	static Line say(boolean romantic, String key, String english, Cue... when) {
		return new Line(key, english, when.length == 0 ? Set.of() : EnumSet.of(when[0], when), romantic);
	}

	/**
	 * Checked once, when the class loads: every authored guest is a fictional roster guest; every key is unique and names
	 * its own guest; gift lines and boasts need no cue and are never romantic; a line is romantic exactly when it needs
	 * {@link Cue#MARRIED}; only guests whose roster entry may marry have romantic lines; the generic set has none.
	 */
	private static void validate() {
		Set<String> keys = new HashSet<>();
		check("generic", GENERIC, false, keys);
		AUTHORED.forEach((guest, lines) -> {
			GuestRoster.Guest entry = GuestRoster.get(guest);
			if (entry == null || entry.pending() || entry.origin() != GuestRoster.Origin.FICTIONAL) {
				throw new IllegalStateException("Lines for a guest that is not an available fictional guest: " + guest);
			}
			check(guest, lines, entry.mayMarry(), keys);
		});
	}

	private static void check(String owner, Lines lines, boolean mayRomance, Set<String> keys) {
		String prefix = "message.jugcraft.guest.personality." + owner + ".";
		List<Line> plain = new java.util.ArrayList<>(lines.favourite());
		plain.addAll(lines.ordinary());
		plain.addAll(lines.rare());
		plain.add(lines.boast());
		for (Line line : plain) {
			if (line.situational() || line.romantic()) {
				throw new IllegalStateException("Gift and boast lines fit any moment and are never romantic: " + line.key());
			}
		}
		if (lines.favourite().isEmpty() || lines.ordinary().isEmpty() || lines.rare().isEmpty() || lines.banter().isEmpty()) {
			throw new IllegalStateException("Every guest needs gift reactions and banter: " + owner);
		}
		boolean everyday = false;
		for (Line line : lines.all()) {
			if (!line.key().startsWith(prefix) || !keys.add(line.key()) || line.english().isBlank() || line.english().indexOf('%') >= 0) {
				throw new IllegalStateException("Bad or repeated line key, or a line with a format argument: " + line.key());
			}
			if (line.romantic() != line.when().contains(Cue.MARRIED) || line.romantic() && !mayRomance) {
				throw new IllegalStateException("Romantic lines are spouse lines of marriage-eligible guests only: " + line.key());
			}
			everyday |= lines.banter().contains(line) && !line.situational();
		}
		if (!everyday) {
			throw new IllegalStateException("Every guest needs banter that fits any moment: " + owner);
		}
	}

	/** The roster id whose lines a game-test guest borrows, or null (test seam). */
	static @Nullable String borrowed(String guest) {
		return BORROWED.get(guest);
	}
}
