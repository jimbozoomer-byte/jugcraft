package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Trick-or-Treat Candy Chute (Front-Yard Haunt Scenes, part 5; tools/front_yard.py CANDY_CHUTE): an open chute 2.5 m
 * long, wrapped in orange and black, from a funnel on its post at porch-rail height down to a mouth at a child's hand
 * height, one block across, two up and three long ({@link MultiDecorationBlock}), its mouth toward whoever places it and
 * its funnel behind. A treat ({@link CandyBagItem#TREATS}) used on it goes in at the funnel, one at a time, no faster than
 * one in {@value #DROP_TICKS} ticks and no more than {@value #HOLD} at once, and slides down it for {@value #SLIDE_TICKS}
 * ticks ({@link #slide}; client/CandyChuteRenderer draws it). At the mouth it goes into the Candy Bag of whoever stands
 * within {@value #CATCH} blocks of it carrying one, or drops out onto the path ({@link CandyChuteBlockEntity}). Its model
 * is tools/front_yard_data.py candy_chute().
 */
public class CandyChuteBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int SLIDE_TICKS = 20;
	public static final int HOLD = 8;
	public static final int DROP_TICKS = 4;
	public static final double CATCH = 1.5;
	/** The chute's floor at its funnel and at its mouth (pixels: height and depth from the mouth's block's front, facing north). */
	public static final float FUNNEL_Y = 25.0F;
	public static final float FUNNEL_Z = 42.0F;
	public static final float MOUTH_Y = 8.5F;
	public static final float MOUTH_Z = 2.0F;
	/** Its blocks: 1 across, 2 up, 3 long (tools/front_yard.py CANDY_CHUTE["size"]). */
	private static final int[][] CELLS = box(1, 2, 3);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The chute down its three blocks, the funnel and the post. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {5.75, 6.5, 0.0, 10.25, 13.0, 16.0},
			new double[] {6.0, 12.0, 16.0, 10.0, 20.0, 32.0}, new double[] {6.0, 19.0, 32.0, 10.0, 26.0, 40.0},
			new double[] {4.25, 24.5, 40.5, 11.75, 28.0, 48.0}, new double[] {7.25, 0.0, 44.0, 8.75, 25.0, 45.5});

	public CandyChuteBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** Where in the world a point of the chute is: {@code y} high and {@code z} deep (pixels, facing north) from the front of its first block at {@code master}. */
	public static Vec3 point(BlockPos master, Direction facing, float y, float z) {
		double back = z / 16.0 - 0.5;
		return new Vec3(master.getX() + 0.5 - facing.getStepX() * back, master.getY() + y / 16.0, master.getZ() + 0.5 - facing.getStepZ() * back);
	}

	/** The chute's mouth, where its treats come out. */
	public static Vec3 mouth(BlockPos master, Direction facing) {
		return point(master, facing, MOUTH_Y, MOUTH_Z);
	}

	/** How far down the chute a treat is {@code since} ticks after it went in, from 0 (the funnel) to 1 (the mouth), quickening as it goes. */
	public static float slide(float since) {
		float t = Mth.clamp(since / SLIDE_TICKS, 0.0F, 1.0F);
		return t * t;
	}

	/** A treat goes in at the funnel; anything else is an empty hand. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(CandyBagItem.TREATS)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (level instanceof ServerLevel server && server.getBlockEntity(masterPos(pos, state)) instanceof CandyChuteBlockEntity chute) {
			if (!chute.drop(stack, server.getGameTime())) {
				return InteractionResult.FAIL;
			}
			Vec3 funnel = point(chute.getBlockPos(), state.getValue(FACING), FUNNEL_Y, FUNNEL_Z);
			server.playSound(null, funnel.x, funnel.y, funnel.z, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 0.8F, 1.2F);
			server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand does nothing to it (it has no light to light). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new CandyChuteBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || part(state) != MASTER || type != JugcraftAgriculture.CANDY_CHUTE_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((CandyChuteBlockEntity) entity).serverTick((ServerLevel) tickLevel);
	}
}
