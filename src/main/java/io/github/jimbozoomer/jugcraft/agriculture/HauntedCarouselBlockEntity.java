package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.kinetic.KineticConsumer;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Haunted Carousel's drive (Haunted Midway, part 5): it takes kinetic energy from any shaft, gearbox or source
 * touching the carousel's middle block, up to {@value HauntedCarousel#NEED} KE a tick in all, and hands it to the carousel,
 * which turns as fast as that allows ({@link HauntedCarousel#power}). It takes nothing while there is no carousel. The
 * energy goes into the turning, so nothing comes back out; it keeps nothing between ticks and saves nothing.
 */
public class HauntedCarouselBlockEntity extends BlockEntity implements KineticConsumer {
	private long tick = -1;
	private long taken;

	public HauntedCarouselBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.HAUNTED_CAROUSEL_DRIVE, pos, state);
	}

	@Override
	public long acceptKinetic(Direction side, long maxAmount) {
		if (!(level instanceof ServerLevel server)) {
			return 0;
		}
		long now = server.getGameTime();
		if (now != tick) {
			tick = now;
			taken = 0;
		}
		long take = Math.min(maxAmount, HauntedCarousel.NEED - taken);
		if (take <= 0) {
			return 0;
		}
		List<HauntedCarousel> carousels = HauntedCarouselBlock.carousels(server, worldPosition);
		if (carousels.isEmpty()) {
			return 0;
		}
		taken += take;
		carousels.get(0).power(take);
		return take;
	}
}
