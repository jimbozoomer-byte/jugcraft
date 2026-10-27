package io.github.jimbozoomer.jugcraft.creatures.herobrine;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.Town;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Herobrine's marks (docs/features/herobrine.md): what he leaves behind after a confirmed distant sighting, and the
 * saved record of every block a mark changed, so an operator can take any mark back. A mark is planned by
 * {@link HerobrineMarkPlans} only in untouched wilderness, then built {@value #BLOCKS_PER_TICK} blocks a tick (across
 * all marks), each block checked again just before it changes: if its chunk is not loaded it waits; if the land became
 * claimed or town, the block is no longer what was planned on, or it holds a block entity, the mark stops there and
 * never overwrites what changed. Undoing a mark restores only blocks still exactly as the mark left them; anything a
 * player changed since stays as they left it.
 */
public final class HerobrineMarks extends SavedData {
	/** How many blocks all marks together change in one tick. */
	public static final int BLOCKS_PER_TICK = 8;
	/** The chance a confirmed distant sighting leaves a mark. */
	public static final double MARK_CHANCE = 0.35;
	/** How many marks may be building at once. */
	public static final int MAX_JOBS = 2;
	/** How many ticks a mark waits for an unloaded chunk or someone standing in the way before giving up. */
	public static final int MAX_WAITS = 200;
	/** How many sites one mark may weigh before giving up (each is a bounded scan of loaded blocks). */
	public static final int MAX_PLAN_TRIES = 8;

	/** One block a mark changed: what it was, and what the mark made it. */
	public record Change(BlockPos pos, BlockState before, BlockState after) {
		static final Codec<Change> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BlockPos.CODEC.fieldOf("pos").forGetter(Change::pos),
				BlockState.CODEC.fieldOf("before").forGetter(Change::before),
				BlockState.CODEC.fieldOf("after").forGetter(Change::after)).apply(instance, Change::new));
	}

	/** A mark's record: its kind, where he stood, when (game time), and every block it changed, in order. */
	public record Mark(String type, BlockPos at, long time, List<Change> changes) {
		static final Codec<Mark> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("type").forGetter(Mark::type),
				BlockPos.CODEC.fieldOf("at").forGetter(Mark::at),
				Codec.LONG.fieldOf("time").forGetter(Mark::time),
				Change.CODEC.listOf().fieldOf("changes").forGetter(Mark::changes)).apply(instance, Mark::new));

		public Mark {
			changes = new ArrayList<>(changes);
		}
	}

	public static final Codec<HerobrineMarks> CODEC = Codec.unboundedMap(Codec.STRING, Mark.CODEC)
			.xmap(HerobrineMarks::new, marks -> marks.marks);
	private static final SavedDataType<HerobrineMarks> TYPE = new SavedDataType<>(Jugcraft.id("herobrine_marks"),
			HerobrineMarks::new, CODEC, null);

	/** What an undo did: blocks put back, blocks left because a player changed them, and blocks waiting on unloaded land. */
	public record Undo(int restored, int kept, int waiting) {
	}

	private static final class Job {
		final String id;
		final ServerLevel level;
		final Deque<HerobrineMarkPlans.Step> steps;
		final Map<BlockPos, List<String>> signs;
		int waits;

		Job(String id, ServerLevel level, List<HerobrineMarkPlans.Step> steps, Map<BlockPos, List<String>> signs) {
			this.id = id;
			this.level = level;
			this.steps = new ArrayDeque<>(steps);
			this.signs = signs;
		}
	}

	private enum Result {
		DONE, SKIP, WAIT, STOP
	}

	private static final List<Job> JOBS = new ArrayList<>();
	private final Map<String, Mark> marks;

	public HerobrineMarks() {
		this(Map.of());
	}

	private HerobrineMarks(Map<String, Mark> marks) {
		this.marks = new HashMap<>(marks);
	}

	public static HerobrineMarks get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/** Every mark on record, by id. */
	public Map<String, Mark> marks() {
		return Collections.unmodifiableMap(marks);
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(HerobrineMarks::work);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> JOBS.clear());
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> commands(dispatcher));
	}

	/** Whether he leaves marks ({@code herobrine.marks}, on unless set to off). Undo works either way. */
	public static boolean enabled() {
		return !"off".equalsIgnoreCase(JugcraftConfig.textOption("herobrine.marks").trim());
	}

	/** Marks still being built. */
	public static int building() {
		return JOBS.size();
	}

	/**
	 * After a confirmed distant sighting: now and then ({@value #MARK_CHANCE}) he leaves a mark where he stood. A sighting
	 * several players shared may leave "you brought someone".
	 */
	static void afterSighting(ServerLevel level, BlockPos where, ServerPlayer player, int witnesses) {
		RandomSource random = level.getRandom();
		if (enabled() && JOBS.size() < MAX_JOBS && random.nextDouble() < MARK_CHANCE) {
			leave(level, where, player, witnesses > 1, random, null);
		}
	}

	/**
	 * Leaves a mark at or near {@code where} (where he stood), facing {@code player}; of {@code only} if given, otherwise
	 * of a kind that suits the place: a tunnel underground, a sand pyramid on sand, a bare grove under trees, otherwise a
	 * shrine, a mossy pyramid or a tunnel into a hillside, and a lone sign if nothing else fits; at most
	 * {@value #MAX_PLAN_TRIES} sites are weighed. Returns the mark's id, or null if nowhere near was wild enough.
	 */
	public static @Nullable String leave(ServerLevel level, BlockPos where, ServerPlayer player, boolean company, RandomSource random,
			HerobrineMarkPlans.@Nullable Type only) {
		if (JOBS.size() >= MAX_JOBS || !level.hasChunkAt(where)) {
			return null;
		}
		List<String> message = company ? HerobrineMarkPlans.BROUGHT_SOMEONE
				: HerobrineMarkPlans.MESSAGES.get(random.nextInt(HerobrineMarkPlans.MESSAGES.size()));
		BlockPos toward = player.blockPosition();
		List<HerobrineMarkPlans.Type> kinds = new ArrayList<>();
		if (only != null) {
			kinds.add(only);
		} else {
			boolean underground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, where.getX(), where.getZ()) > where.getY() + 1;
			boolean underTrees = !underground && !level.canSeeSky(where.above());
			if (underground) {
				kinds.add(HerobrineMarkPlans.Type.TUNNEL);
			} else if (level.getBlockState(where.below()).is(BlockTags.SAND)) {
				kinds.add(HerobrineMarkPlans.Type.SAND_PYRAMID);
			} else if (underTrees) {
				kinds.add(HerobrineMarkPlans.Type.GROVE);
			} else {
				List<HerobrineMarkPlans.Type> open = new ArrayList<>(List.of(HerobrineMarkPlans.Type.SHRINE,
						HerobrineMarkPlans.Type.MOSSY_PYRAMID, HerobrineMarkPlans.Type.TUNNEL));
				Collections.shuffle(open, new java.util.Random(random.nextLong()));
				kinds.addAll(open);
			}
			kinds.add(HerobrineMarkPlans.Type.SIGN);
		}
		Direction away = HerobrineMarkPlans.toward(toward, where);
		int tries = 0;
		for (HerobrineMarkPlans.Type kind : kinds) {
			boolean wide = kind == HerobrineMarkPlans.Type.GROVE || kind == HerobrineMarkPlans.Type.TUNNEL;
			for (BlockPos feet : wide ? List.of(where) : near(level, where)) {
				if (++tries > MAX_PLAN_TRIES) {
					return null;
				}
				HerobrineMarkPlans.Plan plan = switch (kind) {
					case SHRINE -> HerobrineMarkPlans.shrine(level, feet, toward, message);
					case MOSSY_PYRAMID -> HerobrineMarkPlans.pyramid(level, feet, false, toward, message);
					case SAND_PYRAMID -> HerobrineMarkPlans.pyramid(level, feet, true, toward, message);
					case SIGN -> HerobrineMarkPlans.sign(level, feet, toward, message);
					case GROVE -> HerobrineMarkPlans.grove(level, feet, toward, message, random);
					case TUNNEL -> HerobrineMarkPlans.tunnel(level, feet, away, toward, message);
				};
				if (plan != null) {
					return start(level, plan);
				}
			}
		}
		return null;
	}

	/** Where he stood, then four places three blocks round it, each at a standable height within a block. */
	private static List<BlockPos> near(ServerLevel level, BlockPos where) {
		List<BlockPos> places = new ArrayList<>();
		places.add(where);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos side = where.relative(direction, 3);
			for (int dy = 1; dy >= -1; dy--) {
				if (HerobrineEncounters.standable(level, side.above(dy))) {
					places.add(side.above(dy));
					break;
				}
			}
		}
		return places;
	}

	/** Starts building {@code plan}, recording it under a new id. Null if two marks are already building. */
	public static @Nullable String start(ServerLevel level, HerobrineMarkPlans.Plan plan) {
		if (JOBS.size() >= MAX_JOBS) {
			return null;
		}
		HerobrineMarks marks = get(level.getServer());
		String id;
		do {
			id = UUID.randomUUID().toString().substring(0, 8);
		} while (marks.marks.containsKey(id));
		marks.marks.put(id, new Mark(plan.type().name().toLowerCase(Locale.ROOT), plan.at(), level.getGameTime(), List.of()));
		marks.setDirty();
		JOBS.add(new Job(id, level, plan.steps(), plan.signs()));
		return id;
	}

	private static void work(MinecraftServer server) {
		if (JOBS.isEmpty()) {
			return;
		}
		HerobrineMarks marks = get(server);
		int budget = BLOCKS_PER_TICK;
		for (Iterator<Job> jobs = JOBS.iterator(); jobs.hasNext() && budget > 0; ) {
			Job job = jobs.next();
			Mark mark = marks.marks.get(job.id);
			boolean over = mark == null;
			while (!over && budget > 0 && !job.steps.isEmpty()) {
				Result result = apply(job, job.steps.peekFirst(), mark, marks);
				if (result == Result.WAIT) {
					over = ++job.waits > MAX_WAITS;
					break;
				}
				if (result == Result.STOP) {
					over = true;
					break;
				}
				job.steps.removeFirst();
				budget -= result == Result.DONE ? 1 : 0;
			}
			if (over || job.steps.isEmpty()) {
				jobs.remove();
				if (mark != null && mark.changes().isEmpty()) {
					marks.marks.remove(job.id);
					marks.setDirty();
				}
			}
		}
	}

	private static Result apply(Job job, HerobrineMarkPlans.Step step, Mark mark, HerobrineMarks marks) {
		ServerLevel level = job.level;
		BlockPos pos = step.pos();
		if (!level.hasChunkAt(pos)) {
			return Result.WAIT;
		}
		if (LandRegistry.isClaimed(level, pos) || Town.isProtected(level, pos)) {
			return Result.STOP;
		}
		BlockState now = level.getBlockState(pos);
		if (!now.is(step.expect()) || now.hasBlockEntity() || !HerobrineMarkPlans.natural(now)) {
			return Result.STOP;
		}
		if (step.state().isAir()) {
			// Never open a way for water or lava: a leaf by the water stays; a tunnel that meets it stops.
			for (Direction direction : Direction.values()) {
				BlockPos next = pos.relative(direction);
				if (!level.hasChunkAt(next) || !level.getBlockState(next).getFluidState().isEmpty()) {
					return now.is(BlockTags.LEAVES) ? Result.SKIP : Result.STOP;
				}
			}
		} else if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(pos)).isEmpty()) {
			return Result.WAIT;
		}
		level.setBlock(pos, step.state(), Block.UPDATE_ALL);
		mark.changes().add(new Change(pos, now, step.state()));
		marks.setDirty();
		List<String> text = job.signs.get(pos);
		if (text != null && level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
			List<Component> lines = text.stream().map(line -> (Component) Component.literal(line)).toList();
			for (SignTextSlot slot : SignTextSlot.values()) {
				sign.setText(new SignText(lines, lines, DyeColor.BLACK, false), slot);
			}
			sign.setWaxed(true);
			sign.setChanged();
			level.sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), Block.UPDATE_ALL);
		}
		return Result.DONE;
	}

	/**
	 * Takes mark {@code id} back, newest block first: a block still exactly as the mark left it is put back as it was; a
	 * block a player changed since (now some other block) is left alone and forgotten; a block in an unloaded chunk, or with someone standing
	 * where it would go back, waits in the record for another undo. Stops the mark building, if it still is. Null if
	 * there is no such mark.
	 */
	public static @Nullable Undo undo(ServerLevel level, String id) {
		HerobrineMarks marks = get(level.getServer());
		Mark mark = marks.marks.get(id);
		if (mark == null) {
			return null;
		}
		JOBS.removeIf(job -> job.id.equals(id));
		int restored = 0;
		int kept = 0;
		List<Change> waiting = new ArrayList<>();
		List<Change> changes = mark.changes();
		for (int i = changes.size() - 1; i >= 0; i--) {
			Change change = changes.get(i);
			if (!level.hasChunkAt(change.pos())
					|| !change.before().isAir() && !level.getEntitiesOfClass(LivingEntity.class, new AABB(change.pos())).isEmpty()) {
				waiting.add(0, change);
			} else if (level.getBlockState(change.pos()).is(change.after().getBlock())
					|| change.after().isAir() && level.getBlockState(change.pos()).isAir()) {
				level.setBlock(change.pos(), change.before(), Block.UPDATE_ALL);
				restored++;
			} else {
				kept++;
			}
		}
		if (waiting.isEmpty()) {
			marks.marks.remove(id);
		} else {
			changes.clear();
			changes.addAll(waiting);
		}
		marks.setDirty();
		return new Undo(restored, kept, waiting.size());
	}

	private static void commands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jugcraft").then(Commands.literal("herobrine")
				.requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
				.then(Commands.literal("marks").executes(context -> list(context.getSource()))
						.then(Commands.literal("undo").then(Commands.argument("id", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(
										get(context.getSource().getServer()).marks.keySet(), builder))
								.executes(context -> undo(context.getSource(), StringArgumentType.getString(context, "id"))))))
				.then(Commands.literal("mark").then(Commands.argument("player", EntityArgument.player())
						.then(Commands.argument("kind", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(java.util.Arrays.stream(
										HerobrineMarkPlans.Type.values()).map(type -> type.name().toLowerCase(Locale.ROOT)), builder))
								.executes(context -> force(context.getSource(), EntityArgument.getPlayer(context, "player"),
										StringArgumentType.getString(context, "kind"))))))));
	}

	private static int list(CommandSourceStack source) {
		Map<String, Mark> all = get(source.getServer()).marks;
		if (all.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.marks.none"), false);
		}
		all.entrySet().stream().sorted(Map.Entry.comparingByValue((a, b) -> Long.compare(a.time(), b.time()))).forEach(entry -> {
			Mark mark = entry.getValue();
			source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.marks.entry", entry.getKey(), mark.type(),
					mark.at().getX(), mark.at().getY(), mark.at().getZ(), mark.changes().size()), false);
		});
		return all.size();
	}

	private static int undo(CommandSourceStack source, String id) {
		Undo undo = undo(source.getServer().overworld(), id);
		if (undo == null) {
			source.sendFailure(Component.translatable("commands.jugcraft.herobrine.marks.unknown", id));
			return 0;
		}
		source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.marks.undone", id, undo.restored(), undo.kept(),
				undo.waiting()), true);
		return undo.restored();
	}

	/** An operator's mark: a fixed kind, about six blocks in front of {@code player}. */
	private static int force(CommandSourceStack source, ServerPlayer player, String kind) {
		HerobrineMarkPlans.Type type;
		try {
			type = HerobrineMarkPlans.Type.valueOf(kind.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			source.sendFailure(Component.translatable("commands.jugcraft.herobrine.marks.unknown_kind", kind));
			return 0;
		}
		BlockPos ahead = player.blockPosition().relative(player.getDirection(), 6);
		String id = leave(source.getServer().overworld(), ahead, player, false, player.getRandom(), type);
		if (id == null) {
			source.sendFailure(Component.translatable("commands.jugcraft.herobrine.marks.no_site", kind));
			return 0;
		}
		source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.marks.left", kind, id), true);
		return 1;
	}
}
