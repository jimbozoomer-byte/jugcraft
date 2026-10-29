package io.github.jimbozoomer.jugcraft.town;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * The town's day (tools/town_living.py gives each resident their places). Residents wake at dawn and walk to work;
 * they work from 7 am until the shops shut at 6 pm; they spend the evening at a tavern's doorstep or round the fountain;
 * at midnight they go to bed ({@link Townsfolk#bedtime}). Guards keep watch all night and the innkeepers keep the
 * taverns open, selling the General Store's essentials at any hour. Times are the day's ticks: 0 is 6 am, 1000 an hour.
 */
public final class TownRoutine {
	public static final long DAY = 24000L;
	public static final long WORK_STARTS = 1000L;
	public static final long EVENING_STARTS = 12000L;
	public static final long NIGHT_STARTS = 18000L;

	/** Which part of the day it is. */
	public enum Period {
		/** Dawn to 7 am: up and on the way to work. */
		MORNING("morning"),
		/** 7 am to 6 pm. */
		WORK("work"),
		/** 6 pm to midnight. */
		EVENING("evening"),
		/** Midnight to dawn. */
		NIGHT("night");

		public final String key;

		Period(String key) {
			this.key = key;
		}

		public boolean daytime() {
			return this == MORNING || this == WORK;
		}
	}

	/** What a resident is seen doing where they belong (synced to the client, which poses them: TownsfolkModel). */
	public enum Activity {
		NONE, SERVE, KNEAD, COUNT, TEND, SWEEP, CARRY, LIGHT, PLAY, WATCH, PREACH, ADDRESS, CHAT, TOAST, DOZE;

		public static Activity byId(int id) {
			Activity[] all = values();
			return id >= 0 && id < all.length ? all[id] : NONE;
		}
	}

	/**
	 * A resident's places, from their spot in the blueprint: their job, the gathering they join, where they work and
	 * what they look at there, and where they stand in the evening and what they face. Null places mean their own spot.
	 */
	public record Places(String job, String gathering, @Nullable BlockPos work, @Nullable BlockPos workLook,
			@Nullable BlockPos evening, @Nullable BlockPos eveningLook) {
		/** The same places in the world, for a town whose (0, 0, 0) is at {@code origin}. */
		public Places offset(BlockPos origin) {
			return new Places(job, gathering, at(work, origin), at(workLook, origin), at(evening, origin), at(eveningLook, origin));
		}

		private static @Nullable BlockPos at(@Nullable BlockPos relative, BlockPos origin) {
			return relative == null ? null : origin.offset(relative);
		}
	}

	/** The jobs and parts of the day that have their own lines (tools/town_lang.py ROUTINE_LINES, three each). */
	public static final Map<String, Set<String>> TALK = Map.ofEntries(
			Map.entry("townsfolk", Set.of("morning")),
			Map.entry("gardener", Set.of("work", "evening")),
			Map.entry("porter", Set.of("work", "evening")),
			Map.entry("sweeper", Set.of("work", "evening")),
			Map.entry("lamplighter", Set.of("work", "evening")),
			Map.entry("busker", Set.of("work", "evening")),
			Map.entry("baker", Set.of("work", "evening")),
			Map.entry("mayor", Set.of("work", "evening")),
			Map.entry("priest", Set.of("work", "evening")),
			Map.entry("shopkeeper", Set.of("work", "evening")),
			Map.entry("florist", Set.of("work", "evening")),
			Map.entry("vendor", Set.of("work", "evening")),
			Map.entry("decorator", Set.of("work", "evening")),
			Map.entry("innkeeper", Set.of("work", "evening", "night")),
			Map.entry("watch", Set.of("work", "evening", "night")));

	private TownRoutine() {
	}

	public static Period period(long dayTime) {
		long t = Math.floorMod(dayTime, DAY);
		if (t >= NIGHT_STARTS) {
			return Period.NIGHT;
		}
		if (t >= EVENING_STARTS) {
			return Period.EVENING;
		}
		return t < WORK_STARTS ? Period.MORNING : Period.WORK;
	}

	/** Whether a shop keeping these hours (day ticks, open from {@code opens} until {@code closes}) is open now. */
	public static boolean open(int opens, int closes, long dayTime) {
		long t = Math.floorMod(dayTime, DAY);
		return t >= opens && t < closes;
	}

	/** A time of day as people say it: 1000 is "7am", 12000 "6pm". */
	public static String clock(long ticks) {
		long hour = (Math.floorMod(ticks, DAY) / 1000 + 6) % 24;
		return (hour % 12 == 0 ? 12 : hour % 12) + (hour < 12 ? "am" : "pm");
	}

	/**
	 * What someone with this job does at their place in this part of the day; {@code gathering} is the evening gathering
	 * they joined ("tavern", "fountain", "lamps"), or empty.
	 */
	public static Activity activity(String job, String gathering, Period period) {
		boolean watch = job.equals("watch");
		if (period == Period.NIGHT) {
			return watch ? Activity.WATCH : job.equals("innkeeper") ? Activity.DOZE : Activity.NONE;
		}
		if (period == Period.EVENING) {
			return switch (job) {
				case "watch" -> Activity.WATCH;
				case "lamplighter" -> Activity.LIGHT;
				case "busker" -> Activity.PLAY;
				case "priest" -> Activity.PREACH;
				case "innkeeper" -> Activity.SERVE;
				case "mayor" -> Activity.ADDRESS;
				default -> gathering.equals("tavern") ? Activity.TOAST : gathering.isEmpty() ? Activity.NONE : Activity.CHAT;
			};
		}
		return switch (job) {
			case "watch" -> Activity.WATCH;
			case "baker" -> Activity.KNEAD;
			case "banker" -> Activity.COUNT;
			case "mayor" -> Activity.ADDRESS;
			case "priest" -> Activity.PREACH;
			case "shopkeeper", "vendor", "innkeeper" -> Activity.SERVE;
			case "florist", "gardener" -> Activity.TEND;
			case "porter" -> Activity.CARRY;
			case "sweeper" -> Activity.SWEEP;
			case "lamplighter" -> Activity.LIGHT;
			case "busker" -> Activity.PLAY;
			default -> Activity.NONE;
		};
	}

	/**
	 * How far from their place a resident wanders now. Guards walk their beat at any hour and decorators range over the
	 * whole town by day ({@link Townsfolk#range()}); everyone else keeps close to their work, their gathering, or (at
	 * night, when not abed) where they stand.
	 */
	public static int reach(String job, int range, Period period, boolean hasPlace) {
		if (job.equals("watch") || job.equals("walker") || job.equals("flak") || job.equals("decorator") && period.daytime()) {
			return range;
		}
		if (period == Period.NIGHT) {
			return 1;
		}
		if (period == Period.EVENING) {
			return job.equals("lamplighter") ? 8 : hasPlace ? 1 : Math.min(range, 2);
		}
		return switch (job) {
			case "sweeper" -> 10;
			case "lamplighter" -> 6;
			case "gardener", "porter" -> 2;
			case "busker" -> 1;
			default -> hasPlace ? 2 : range;
		};
	}

	/** The key of a line for this job in this part of the day, or null when it has none. */
	public static @Nullable String line(String job, Period period, int index) {
		// Dawn greetings are everyone's, but the watch and the innkeepers are already at work.
		boolean dawn = period == Period.MORNING && !job.equals("watch") && !job.equals("innkeeper");
		String who = dawn ? "townsfolk" : job;
		String part = period == Period.MORNING && !dawn ? "work" : period.key;
		Set<String> parts = TALK.get(who);
		return parts != null && parts.contains(part) ? "message.jugcraft.townsfolk.routine." + who + "." + part + "." + index : null;
	}
}
