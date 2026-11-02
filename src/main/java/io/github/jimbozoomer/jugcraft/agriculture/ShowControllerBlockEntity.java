package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.kinetic.ElectricMotorBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Show Controller's works (its block, {@link ShowControllerBlock}). It takes JE from cables (buffer
 * {@value #CAPACITY}, up to {@value #INPUT} a tick). Every {@value #SCAN_TICKS} ticks it finds its cast: the clockwork
 * figures within {@value #RANGE} blocks, nearest first, up to {@value #MAX_CAST}, that no other controller is driving
 * ({@link ClockworkFigureBlockEntity#near}, which searches no blocks). Switched on, with a cast and with enough JE for all
 * their needs ({@link #cost}, at the copper-wound electric motor's rate, rounded up so it never makes power), it cues
 * its show: it holds them still {@value #CUE_TICKS} ticks, long enough for every client to see them stop and count them
 * from the top, then drives them all from one count, a tick at a time, using that JE each tick. A new cast, or a lack of
 * power or the switch thrown, ends the show; the figures then run down as they would on their own. It saves only its
 * JE: after a reload it cues its show again.
 */
public class ShowControllerBlockEntity extends BlockEntity {
	public static final long CAPACITY = 8_000;
	public static final long INPUT = 128;
	public static final int RANGE = 8;
	public static final int MAX_CAST = 12;
	public static final int SCAN_TICKS = 20;
	public static final int CUE_TICKS = 30;
	/** KE a figure gets for 100 JE: the copper-wound electric motor's rate, so no route to a figure is cheaper. */
	public static final int EFFICIENCY_PERCENT = ElectricMotorBlockEntity.EFFICIENCY_PERCENT;

	final SimpleEnergyStorage energy = new SimpleEnergyStorage(CAPACITY, INPUT, 0, this::setChanged);
	/** Its cast: the lower blocks of the figures it drives, nearest first. */
	private List<BlockPos> cast = List.of();
	private long nextScan;
	/** The tick its show starts, after its cue; {@code Long.MIN_VALUE} while it has none (it cues one when it can). */
	private long startAt = Long.MIN_VALUE;
	/** Ticks into its show. */
	private int show;

	public ShowControllerBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.SHOW_CONTROLLER_ENTITY, pos, state);
	}

	public SimpleEnergyStorage energy() {
		return energy;
	}

	/** JE a tick to give each of {@code figures} all it needs: their needs in KE, at the electric motor's rate, rounded up. */
	public static long cost(List<ClockworkFigureBlockEntity> figures) {
		long need = 0;
		for (ClockworkFigureBlockEntity entity : figures) {
			if (entity.getBlockState().getBlock() instanceof ClockworkFigure figure) {
				need += figure.need();
			}
		}
		return (need * 100 + EFFICIENCY_PERCENT - 1) / EFFICIENCY_PERCENT;
	}

	void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		long now = level.getGameTime();
		if (now >= nextScan) {
			nextScan = now + SCAN_TICKS;
			List<BlockPos> found = new ArrayList<>();
			for (ClockworkFigureBlockEntity figure : ClockworkFigureBlockEntity.near(level, pos, RANGE)) {
				if (found.size() < MAX_CAST && figure.free(pos, now)) {
					found.add(figure.getBlockPos());
				}
			}
			if (!found.equals(cast)) {
				cast = List.copyOf(found);
				startAt = Long.MIN_VALUE; // a new cast: the show from the top, for all of them
			}
		}
		List<ClockworkFigureBlockEntity> figures = new ArrayList<>();
		for (BlockPos at : cast) {
			if (level.getBlockEntity(at) instanceof ClockworkFigureBlockEntity figure && figure.free(pos, now)) {
				figures.add(figure);
			}
		}
		long cost = cost(figures);
		boolean able = state.getValue(ShowControllerBlock.ENABLED) && !figures.isEmpty() && energy.getAmount() >= cost;
		boolean running = false;
		if (!able) {
			startAt = Long.MIN_VALUE;
		} else if (startAt == Long.MIN_VALUE || now < startAt) {
			if (startAt == Long.MIN_VALUE) {
				startAt = now + CUE_TICKS;
				show = 0;
			}
			for (ClockworkFigureBlockEntity figure : figures) {
				figure.hold(pos, now, startAt);
			}
		} else {
			energy.setAmount(energy.getAmount() - cost);
			setChanged();
			for (ClockworkFigureBlockEntity figure : figures) {
				figure.cue(pos, now, show);
			}
			show++;
			running = true;
		}
		BlockState next = state.setValue(ShowControllerBlock.RUNNING, running)
				.setValue(ShowControllerBlock.CAST, able ? Math.min(figures.size(), ShowControllerBlock.LAMPS) : 0);
		if (next != state) {
			level.setBlock(pos, next, Block.UPDATE_CLIENTS);
		}
	}

	/** Its cast as it last found it (for tests). */
	public List<BlockPos> cast() {
		return cast;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		energy.setAmount(input.getLong("energy").orElse(0L));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("energy", energy.getAmount());
	}
}
