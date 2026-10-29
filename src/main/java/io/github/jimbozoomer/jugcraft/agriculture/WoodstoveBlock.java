package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Cast-Iron Woodstove (Cozy Fall Hearth, part 2; tools/hearth.py WOODSTOVE): a parlour box stove on four cast legs,
 * its firebox door glazed with mica, a nickel rail round its cooktop and its stovepipe going out of its back into the
 * wall. It is placed out ({@link #LIT} false). Flint and steel or a fire charge lights it ({@link CandleLighting}); a
 * shovel puts it out. Lit, its fire shows through the mica (a full-bright model in the fireplace's animated texture,
 * added by the blockstate), it gives light {@value #LIGHT}, and it heats whatever stands on it (block tag
 * jugcraft:heat_sources, as the Kitchen Stove does): a Cooking Pot, a Skillet, a Tea Kettle, a Simmer Pot. It burns no
 * fuel and cooks nothing itself (the Kitchen Stove's hob does that); its hot top burns what stands on it, as a magma
 * block does. Its model is tools/hearth_data.py woodstove().
 */
public class WoodstoveBlock extends FacingDecorationBlock {
	public static final int LIGHT = 13;
	public static final float BURN = 1.0F;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public WoodstoveBlock(Properties properties) {
		super(properties, new double[] {1.75, 0.0, 2.5, 14.25, 16.0, 16.0});
		registerDefaultState(defaultBlockState().setValue(LIT, false));
	}

	/** The light it gives: {@value #LIGHT} while lit. */
	public static int light(BlockState state) {
		return state.getValue(LIT) ? LIGHT : 0;
	}

	/** Flint and steel or a fire charge lights it and a shovel puts it out; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (stack.is(ItemTags.SHOVELS) && state.getValue(LIT)) {
			if (!level.isClientSide()) {
				level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
				level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			}
			return InteractionResult.SUCCESS;
		}
		InteractionResult lighting = CandleLighting.light(stack, state, level, pos, player, hand, LIT);
		return lighting == InteractionResult.TRY_WITH_EMPTY_HAND && !stack.isEmpty() ? InteractionResult.PASS : lighting;
	}

	/** Its hot top burns whatever stands on it while it is lit, unless it is sneaking (as a magma block does). */
	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (state.getValue(LIT) && !entity.isSteppingCarefully() && level instanceof ServerLevel server && entity instanceof LivingEntity living
				&& !living.fireImmune()) {
			living.hurtServer(server, level.damageSources().hotFloor(), BURN);
		}
		super.stepOn(level, pos, state, entity);
	}

	/** Lit, the fire crackles behind its door (each client's own, near it). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(LIT) && random.nextInt(10) == 0) {
			Direction front = state.getValue(FACING);
			level.playLocalSound(pos.getX() + 0.5 + front.getStepX() * 0.3, pos.getY() + 0.6, pos.getZ() + 0.5 + front.getStepZ() * 0.3,
					SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat() * 0.5F, random.nextFloat() * 0.7F + 0.6F, false);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIT);
	}
}
