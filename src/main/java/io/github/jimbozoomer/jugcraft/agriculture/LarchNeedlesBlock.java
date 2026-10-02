package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Larch needles: the foliage of a conifer that changes with the seasons. They follow the server's season day
 * ({@link JugcraftSeasons#today()}): green from {@link #GREEN_FROM}, gold from {@link #GOLD_FROM} and bare twigs from
 * {@link #BARE_FROM} until spring. Each block turns up to {@link #JITTER} days early or late, fixed by its position,
 * so a crown turns gradually and neighbouring trees differ. Needles catch up on their random ticks; one that changes
 * also brings the needles it touches up to date (up to {@link #SPREAD} of them, in loaded chunks), so a tree generated
 * green in autumn turns together within moments of its first tick. Needles placed by a player or grown from a sapling
 * start in today's state; with seasons off they stay green. Only the look changes: otherwise these are vanilla leaves
 * (natural ones decay away from logs; they drop saplings and sticks).
 */
public class LarchNeedlesBlock extends TintedParticleLeavesBlock {
	public static final EnumProperty<Needles> SEASON = EnumProperty.create("season", Needles.class);
	/** Season days (northern calendar). Keep in sync with LARCH in tools/agriculture.py. */
	public static final int GREEN_FROM = 91;
	public static final int GOLD_FROM = 268;
	public static final int BARE_FROM = 318;
	public static final int JITTER = 7;
	/** At most this many touching needles catch up with one that changed on its random tick. */
	public static final int SPREAD = 128;

	/** The needles' look in each season. */
	public enum Needles implements StringRepresentable {
		GREEN, GOLD, BARE;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public LarchNeedlesBlock(Properties properties) {
		super(0.01F, properties);
		registerDefaultState(defaultBlockState().setValue(SEASON, Needles.GREEN));
	}

	/** The needles at {@code pos} on season day {@code day} (0: seasons off, always green). */
	public static Needles forDay(int day, BlockPos pos) {
		if (day <= 0) {
			return Needles.GREEN;
		}
		int shifted = Math.floorMod(day - 1 + jitter(pos), SeasonCalendar.DAYS) + 1;
		if (shifted >= BARE_FROM || shifted < GREEN_FROM) {
			return Needles.BARE;
		}
		return shifted >= GOLD_FROM ? Needles.GOLD : Needles.GREEN;
	}

	/** How many days early (negative) or late this position turns: -JITTER to JITTER, always the same. */
	public static int jitter(BlockPos pos) {
		int hash = pos.getX() * 73428767 ^ pos.getY() * 912931 ^ pos.getZ() * 42317861;
		hash ^= hash >>> 15;
		return Math.floorMod(hash, 2 * JITTER + 1) - JITTER;
	}

	/** Every larch needle ticks, placed ones too, so all of them follow the season. */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.randomTick(state, level, pos, random);
		BlockState now = level.getBlockState(pos);
		int day = JugcraftSeasons.today();
		if (now.is(this) && follow(now, level, pos, day)) {
			catchUp(level, pos, day);
		}
	}

	/** Needles placed or grown from a sapling start in today's state rather than waiting for a random tick. */
	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide() && !oldState.is(this)) {
			follow(state, level, pos, JugcraftSeasons.today());
		}
	}

	/** Brings the loaded needles connected to {@code start} up to date for {@code day}, at most {@link #SPREAD} of them. */
	private void catchUp(ServerLevel level, BlockPos start, int day) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		seen.add(start);
		queue.add(start);
		while (!queue.isEmpty()) {
			BlockPos pos = queue.poll();
			for (Direction direction : Direction.values()) {
				if (seen.size() >= SPREAD) {
					return;
				}
				BlockPos next = pos.relative(direction);
				if (!seen.contains(next) && level.isLoaded(next)) {
					BlockState state = level.getBlockState(next);
					if (state.is(this)) {
						seen.add(next);
						follow(state, level, next, day);
						queue.add(next);
					}
				}
			}
		}
	}

	/** Sets the needles at {@code pos} to their look on {@code day}; true if that changed them. */
	private boolean follow(BlockState state, Level level, BlockPos pos, int day) {
		Needles needles = forDay(day, pos);
		if (state.getValue(SEASON) == needles) {
			return false;
		}
		level.setBlock(pos, state.setValue(SEASON, needles), Block.UPDATE_CLIENTS);
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SEASON);
	}
}
