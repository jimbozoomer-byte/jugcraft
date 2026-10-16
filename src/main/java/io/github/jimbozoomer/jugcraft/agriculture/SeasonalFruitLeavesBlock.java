package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** The established fruit harvest and decay rules, with independent seasonal appearance. */
public final class SeasonalFruitLeavesBlock extends FruitingLeavesBlock implements SeasonalFoliage {
	public static final int FRUIT_CHANCE = 10;
	private final SeasonalLeavesBlock.Schedule schedule;
	private final String fruitId;

	public SeasonalFruitLeavesBlock(SeasonalLeavesBlock.Schedule schedule, String fruitId, Properties properties) {
		super(properties);
		this.schedule = schedule;
		this.fruitId = fruitId;
		registerDefaultState(defaultBlockState().setValue(SeasonalLeavesBlock.SEASON, SeasonalLeavesBlock.Foliage.GREEN));
	}

	@Override public SeasonalLeavesBlock.Schedule schedule() { return schedule; }
	@Override protected int fruitChance() { return FRUIT_CHANCE; }
	@Override protected ItemStack fruit(RandomSource random) {
		return new ItemStack(JugcraftAgriculture.item(fruitId), 1 + random.nextInt(2));
	}

	// Even picked, ripe or player-placed leaves must still catch up with the calendar.
	@Override protected boolean isRandomlyTicking(BlockState state) { return true; }

	@Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.randomTick(state, level, pos, random);
		SeasonalLeavesBlock.updateSeason(this, level.getBlockState(pos), level, pos, JugcraftSeasons.today(), schedule);
	}

	@Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide() && !oldState.is(this)) {
			SeasonalLeavesBlock.follow(state, level, pos, JugcraftSeasons.today(), schedule);
		}
	}

	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(SeasonalLeavesBlock.SEASON);
	}
}
