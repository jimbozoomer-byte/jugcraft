package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A pumpkin carved by hand with the Carving Knife: any design on any of its four sides, kept in
 * {@link CarvedPumpkinBlockEntity} and drawn by the client. A torch inside lights it ({@link #LIT}); it
 * then gives {@link #GLOW} light, more the more is carved out ({@link PumpkinCarving#glow}). Using it with
 * an empty hand takes the torch back out.
 */
public class CarvedPumpkinBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, 15);

	public CarvedPumpkinBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false).setValue(GLOW, 0));
	}

	/** The light a carved pumpkin gives: its glow while a torch is inside, none without. */
	public static int light(BlockState state) {
		return state.getValue(LIT) ? state.getValue(GLOW) : 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, GLOW);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		PumpkinCarving carving = context.getItemInHand().getOrDefault(JugcraftAgriculture.CARVING, PumpkinCarving.BLANK);
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(GLOW, carving.glow());
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		// The design came with the item; the glow follows it, whatever the item claimed.
		if (level.getBlockEntity(pos) instanceof CarvedPumpkinBlockEntity pumpkin && state.getValue(GLOW) != pumpkin.carving().glow()) {
			level.setBlock(pos, state.setValue(GLOW, pumpkin.carving().glow()), Block.UPDATE_ALL);
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CarvedPumpkinBlockEntity(pos, state);
	}

	/** A torch lights a carved pumpkin that has something carved out to shine through. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(Items.TORCH) || state.getValue(LIT) || state.getValue(GLOW) == 0) {
			// Only a truly empty hand takes the torch out; anything else (the knife, a block) does its own thing.
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes the torch back out. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!state.getValue(LIT)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
			ItemStack torch = new ItemStack(Items.TORCH);
			if (!player.getInventory().add(torch)) {
				Block.popResourceFromFace(level, pos, hit.getDirection(), torch);
			}
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.2F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}
}
