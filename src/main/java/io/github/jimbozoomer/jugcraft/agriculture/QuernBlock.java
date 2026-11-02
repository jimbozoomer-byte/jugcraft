package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Quern (Barn & Harvest Tools, part 2; tools/barn_tools.py QUERN): a pair of hand millstones in a flour tray on a
 * wooden stand. Use wheat or dried chili on it to fill its eye, up to {@value #CAPACITY} of one kind ({@link #LOADED}
 * shows grain there). Use it with an empty hand to turn the upper stone once round; every {@value #TURNS} turns grind
 * one piece, which falls out of the spout at its front: wheat into Flour, a dried chili into Paprika as the Mortar and
 * Pestle grinds it ({@link SpiceGrinding}). A turn takes {@value #SPIN_TICKS} ticks, and the next waits for it.
 * Sneak-use with an empty hand to take the grain back. Everything changes on the server; the stone is drawn turning by
 * client/QuernRenderer from the block's {@code runner} model.
 */
public class QuernBlock extends HandWorkedBlock {
	public static final int CAPACITY = 16;
	public static final int TURNS = 2;
	public static final int SPIN_TICKS = 10;
	public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
	private static final VoxelShape SHAPE = Shapes.or(Block.box(2.5, 0.0, 2.5, 13.5, 11.0, 13.5), Block.box(3.5, 11.0, 3.5, 12.5, 14.0, 12.5));

	public QuernBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LOADED, false));
	}

	/** What one piece of {@code grain} grinds into, or nothing if the quern doesn't grind it. */
	public static ItemStack ground(ItemStack grain) {
		if (grain.is(Items.WHEAT)) {
			return new ItemStack(JugcraftAgriculture.item("flour"));
		}
		return SpiceGrinding.grinds(grain) ? SpiceGrinding.result() : ItemStack.EMPTY;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new QuernBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/** Grain goes into the eye; anything else does what it would on any block, and an empty hand turns the stone. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (ground(stack).isEmpty()) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof QuernBlockEntity quern)) {
			return InteractionResult.SUCCESS;
		}
		if (quern.add(stack) <= 0) {
			boolean full = quern.grain().getCount() >= CAPACITY;
			player.sendOverlayMessage(Component.translatable(full ? "message.jugcraft.quern.full" : "message.jugcraft.quern.other"));
			return InteractionResult.CONSUME;
		}
		level.setBlock(pos, state.setValue(LOADED, true), Block.UPDATE_CLIENTS);
		level.playSound(null, pos, SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 0.6F, 1.4F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Turns the stone; sneaking, takes the grain back. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof QuernBlockEntity quern)) {
			return InteractionResult.SUCCESS;
		}
		if (player.isSecondaryUseActive()) {
			ItemStack grain = quern.takeGrain();
			if (grain.isEmpty()) {
				return InteractionResult.PASS;
			}
			player.getInventory().placeItemBackInInventory(grain, Prediction.SERVER_ONLY);
			level.setBlock(pos, state.setValue(LOADED, false), Block.UPDATE_CLIENTS);
			return InteractionResult.SUCCESS;
		}
		long now = level.getGameTime();
		if (!quern.ready(now)) {
			return InteractionResult.CONSUME;
		}
		ItemStack out = quern.turn(now);
		level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 0.8F + level.getRandom().nextFloat() * 0.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		if (!out.isEmpty()) {
			Block.popResourceFromFace(level, pos, state.getValue(FACING), out);
		}
		if (state.getValue(LOADED) == quern.grain().isEmpty()) {
			level.setBlock(pos, state.setValue(LOADED, !quern.grain().isEmpty()), Block.UPDATE_CLIENTS);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LOADED);
	}
}
