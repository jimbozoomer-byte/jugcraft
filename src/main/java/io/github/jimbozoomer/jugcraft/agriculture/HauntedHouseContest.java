package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Haunted House contest (docs/features/haunted-house-contest.md), all through the Halloween season, decided on the
 * server. When trick-or-treaters are answered at a Candy Bowl ({@link TrickOrTreaters}) and took at least one treat,
 * and the bowl's owner is on the server, the children judge the owner's haunted house by the props
 * ({@code #jugcraft:haunted_props}) round the door: within {@value #REACH} blocks across, {@value #BELOW} down and
 * {@value #ABOVE} up, a point for each prop, at most {@value #PER_KIND} of a kind, and {@value #VARIETY} more for each
 * kind. Each owner's best score of the season is their entry. Each Halloween ({@link HalloweenSeason#year()}) is its
 * own contest; once the season ends (or a later one begins) the {@value #PLACES} best get the Harvest Scale's First,
 * Second and Third Prize Ribbons, a winner who is offline on their next visit. Scores, results and prizes still owed are
 * saved with the world, and every contest is awarded once. The server's {@code halloween.haunted_house} ({@code on} or
 * {@code off}) turns judging off; prizes already won are still handed out.
 */
public final class HauntedHouseContest {
	public static final int REACH = 12;
	public static final int BELOW = 4;
	public static final int ABOVE = 10;
	public static final int PER_KIND = 4;
	public static final int VARIETY = 2;
	public static final int SHRIEK = 40;
	public static final int PLACES = 3;
	public static final int CHECK_TICKS = 200;
	public static final int MAX_ENTRANTS = 4096;
	public static final int MAX_CONTESTS = 8;
	public static final String OPTION = "halloween.haunted_house";
	public static final String ADVANCEMENT = "haunted_house";
	public static final String WINNER_ADVANCEMENT = "scariest_house_on_the_block";
	public static final TagKey<Block> PROPS = TagKey.create(Registries.BLOCK, Jugcraft.id("haunted_props"));

	/** What the children made of a house: its points, how many props they counted and of how many kinds. */
	public record Score(int points, int props, int kinds) {
	}

	/** One place in the standings. */
	public record Standing(UUID entrant, String name, int best) {
	}

	private HauntedHouseContest() {
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % CHECK_TICKS == 0) {
				check(server);
			}
		});
	}

	/** Whether judging is on: the {@code agriculture} switch and {@code halloween.haunted_house}. */
	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE) && !"off".equalsIgnoreCase(JugcraftConfig.textOption(OPTION));
	}

	/**
	 * Scores the haunted house round {@code door}: reads {@code (2 x REACH + 1)^2 x (BELOW + ABOVE + 1)} blocks, only from
	 * loaded chunks.
	 */
	public static Score score(ServerLevel level, BlockPos door) {
		Map<Block, Integer> counted = new HashMap<>();
		for (BlockPos pos : BlockPos.betweenClosed(door.offset(-REACH, -BELOW, -REACH), door.offset(REACH, ABOVE, REACH))) {
			if (!level.isLoaded(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.is(PROPS)) {
				counted.merge(state.getBlock(), 1, Integer::sum);
			}
		}
		int props = 0;
		for (int count : counted.values()) {
			props += Math.min(count, PER_KIND);
		}
		return new Score(props + VARIETY * counted.size(), props, counted.size());
	}

	/**
	 * The trick-or-treaters at the door {@code door} judge the haunted house of the bowl's owner {@code owner} (if on the
	 * server, and judging is on and the season runs); records it as their entry if it is their best; returns the score,
	 * or null if nobody was judged.
	 */
	public static @Nullable Score judge(ServerLevel level, BlockPos door, @Nullable UUID owner) {
		MinecraftServer server = level.getServer();
		ServerPlayer host = owner == null ? null : server.getPlayerList().getPlayer(owner);
		if (host == null || !enabled() || !HalloweenSeason.active()) {
			return null;
		}
		Score score = score(level, door);
		Data data = data(server);
		Contest contest = data.contest(HalloweenSeason.year());
		int best = contest.judged(owner, host.getName().getString(), score.points());
		data.setDirty();
		TrickOrTreat.award(host, ADVANCEMENT);
		Component said = Component.translatable("message.jugcraft.haunted_house.judged", host.getName(), score.points(), score.props(),
				score.kinds(), best);
		Vec3 at = Vec3.atCenterOf(door);
		for (ServerPlayer player : level.players()) {
			if (player.position().closerThan(at, TrickOrTreaters.PLAYER_RANGE)) {
				player.sendSystemMessage(said);
			}
		}
		if (host.level() != level || !host.position().closerThan(at, TrickOrTreaters.PLAYER_RANGE)) {
			host.sendSystemMessage(said);
		}
		if (score.points() >= SHRIEK) {
			level.playSound(null, door, SoundEvents.WITCH_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 2.0F);
			level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 0.5, at.z, 10, 0.8, 0.6, 0.8, 0.02);
			for (ServerPlayer player : level.players()) {
				if (player.position().closerThan(at, TrickOrTreaters.PLAYER_RANGE)) {
					player.sendOverlayMessage(Component.translatable("message.jugcraft.haunted_house.shriek"));
				}
			}
		}
		return score;
	}

	/** Tells {@code player} where the house of {@code owner} stands this season, if it has been judged. */
	public static void tellStanding(ServerPlayer player, @Nullable UUID owner) {
		if (owner == null || !HalloweenSeason.active()) {
			return;
		}
		List<Standing> standings = standings(player.level().getServer(), HalloweenSeason.year());
		for (int place = 0; place < standings.size(); place++) {
			Standing standing = standings.get(place);
			if (standing.entrant().equals(owner)) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.haunted_house.standing", standing.name(), standing.best(),
						place + 1, standings.size()));
				return;
			}
		}
	}

	/** This Halloween's standings, best first (ties by name). */
	public static List<Standing> standings(MinecraftServer server, int year) {
		Contest contest = data(server).contests.get(year);
		return contest == null ? List.of() : contest.standings();
	}

	/** Awards every contest that is over, and hands out prizes owed to players who are online. */
	public static void check(MinecraftServer server) {
		Data data = data(server);
		int year = HalloweenSeason.year();
		boolean running = HalloweenSeason.active();
		for (Map.Entry<Integer, Contest> entry : data.contests.entrySet()) {
			Contest contest = entry.getValue();
			if (!contest.awarded && (!running || entry.getKey() < year)) {
				award(server, data, entry.getKey(), contest);
			}
		}
		for (Iterator<Map.Entry<UUID, List<String>>> it = data.prizes.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, List<String>> owed = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(owed.getKey());
			if (player == null) {
				continue;
			}
			for (String ribbon : owed.getValue()) {
				ItemStack stack = new ItemStack(JugcraftAgriculture.item(ribbon));
				player.sendSystemMessage(Component.translatable("message.jugcraft.haunted_house.prize", stack.getHoverName()));
				if (ribbon.equals(HarvestScaleBlockEntity.RIBBONS.get(0))) {
					TrickOrTreat.award(player, WINNER_ADVANCEMENT);
				}
				if (!player.getInventory().add(stack)) {
					player.spawnAtLocation(player.level(), stack); // Full inventory: drop it at their feet.
				}
			}
			it.remove();
			data.setDirty();
		}
	}

	private static void award(MinecraftServer server, Data data, int year, Contest contest) {
		contest.awarded = true;
		data.setDirty();
		List<Standing> standings = contest.standings();
		if (standings.isEmpty()) {
			return;
		}
		server.getPlayerList().broadcastSystemMessage(Component.translatable("message.jugcraft.haunted_house.over", year), false);
		for (int place = 0; place < Math.min(PLACES, standings.size()); place++) {
			Standing standing = standings.get(place);
			data.prizes.computeIfAbsent(standing.entrant(), id -> new ArrayList<>()).add(HarvestScaleBlockEntity.RIBBONS.get(place));
			server.getPlayerList().broadcastSystemMessage(Component.translatable("message.jugcraft.haunted_house.place",
					place + 1, standing.name(), standing.best()), false);
		}
	}

	public static Data data(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(Data.TYPE);
	}

	/** One Halloween's contest: each entrant's name and best score, and whether it was awarded. */
	public static final class Contest {
		private record Entry(String name, int best) {
			static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.STRING.fieldOf("name").forGetter(Entry::name),
					Codec.INT.fieldOf("best").forGetter(Entry::best)).apply(i, Entry::new));
		}

		static final Codec<Contest> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.unboundedMap(UUIDUtil.STRING_CODEC, Entry.CODEC).fieldOf("entries").forGetter(c -> c.entries),
				Codec.BOOL.fieldOf("awarded").forGetter(c -> c.awarded)).apply(i, Contest::new));

		final Map<UUID, Entry> entries;
		boolean awarded;

		Contest() {
			this(Map.of(), false);
		}

		Contest(Map<UUID, Entry> entries, boolean awarded) {
			this.entries = new LinkedHashMap<>();
			entries.entrySet().stream().limit(MAX_ENTRANTS).forEach(e -> this.entries.put(e.getKey(), e.getValue()));
			this.awarded = awarded;
		}

		public boolean awarded() {
			return awarded;
		}

		/** Records a judging of {@code entrant}'s house at {@code points}; returns their best this season. */
		public int judged(UUID entrant, String name, int points) {
			Entry old = entries.get(entrant);
			if (old == null && entries.size() >= MAX_ENTRANTS) {
				return points;
			}
			int best = old == null ? points : Math.max(old.best, points);
			entries.put(entrant, new Entry(name, best));
			return best;
		}

		/** {@code entrant}'s best score this season, or -1 if their house has not been judged. */
		public int best(UUID entrant) {
			Entry entry = entries.get(entrant);
			return entry == null ? -1 : entry.best;
		}

		public List<Standing> standings() {
			List<Standing> out = new ArrayList<>();
			entries.forEach((entrant, entry) -> out.add(new Standing(entrant, entry.name, entry.best)));
			out.sort(Comparator.comparingInt(Standing::best).reversed().thenComparing(Standing::name).thenComparing(Standing::entrant));
			return out;
		}
	}

	/** Every contest kept (the latest {@link #MAX_CONTESTS}) and the prizes still owed (data/jugcraft_haunted_house_contest.dat). */
	public static final class Data extends SavedData {
		/** A contest's year as a map key: a bad key is a decoding error, not an exception. */
		private static final Codec<Integer> YEAR = Codec.STRING.comapFlatMap(text -> {
			try {
				return DataResult.success(Integer.parseInt(text));
			} catch (NumberFormatException e) {
				return DataResult.error(() -> "Not a contest year: " + text);
			}
		}, String::valueOf);
		public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.unboundedMap(YEAR, Contest.CODEC).fieldOf("contests").forGetter(d -> d.contests),
				Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING.listOf()).fieldOf("prizes").forGetter(d -> d.prizes))
				.apply(i, Data::new));
		static final SavedDataType<Data> TYPE = new SavedDataType<>(Jugcraft.id("haunted_house_contest"), Data::new, CODEC, null);

		final TreeMap<Integer, Contest> contests = new TreeMap<>();
		final Map<UUID, List<String>> prizes = new HashMap<>();

		Data() {
		}

		Data(Map<Integer, Contest> contests, Map<UUID, List<String>> prizes) {
			this.contests.putAll(contests);
			while (this.contests.size() > MAX_CONTESTS) {
				this.contests.pollFirstEntry();
			}
			prizes.forEach((player, ribbons) -> this.prizes.put(player, new ArrayList<>(ribbons)));
		}

		/** The contest of {@code year}, begun if need be. */
		public Contest contest(int year) {
			Contest contest = contests.computeIfAbsent(year, y -> new Contest());
			while (contests.size() > MAX_CONTESTS) {
				contests.pollFirstEntry();
			}
			return contest;
		}

		public @Nullable Contest get(int year) {
			return contests.get(year);
		}

		/** Prizes still owed to a player (ribbon item IDs). */
		public List<String> owed(UUID player) {
			return List.copyOf(prizes.getOrDefault(player, List.of()));
		}
	}
}
