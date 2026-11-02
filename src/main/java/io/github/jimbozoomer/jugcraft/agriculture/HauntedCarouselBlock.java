package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Haunted Carousel's middle (Haunted Midway, part 5; tools/haunted_midway.py CAROUSEL): the drive house under its
 * column, with its band organ's pipes on the front, the one real block of the carousel. Placed facing whoever places it,
 * where the carousel has room ({@link #clear}: {@value HauntedCarousel#WIDTH} blocks across and through and
 * {@value #HEIGHT} up, all of it air or plants), it sets the {@link HauntedCarousel} turning round itself. Using it with an
 * empty hand boards the free mount nearest the player ({@link HauntedCarousel#board}); sneaking, it switches the organ on
 * or off ({@link #ORGAN}). Its block entity takes kinetic energy from a touching shaft, gearbox or source and hands it to
 * the carousel ({@link HauntedCarouselBlockEntity}). While the carousel's lights are on ({@link #LIT}, set by it), it gives
 * light {@value #LIGHT} under the canopy. Breaking it takes the carousel down too, setting any riders on the ground; it
 * drops itself. Its model is tools/haunted_midway_data.py carousel().
 */
public class HauntedCarouselBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final String ID = "haunted_carousel";
	public static final int HEIGHT = 5;
	public static final int LIGHT = 12;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty ORGAN = BooleanProperty.create("organ");
	private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

	public HauntedCarouselBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false).setValue(ORGAN, true));
	}

	public static int light(BlockState state) {
		return state.getValue(LIT) ? LIGHT : 0;
	}

	/**
	 * Whether the carousel has room round a middle at {@code centre}: every block across, through and up the space it
	 * turns in, but the middle's own, can be replaced (air, grass, flowers), and none is outside the world.
	 */
	public static boolean clear(Level level, BlockPos centre) {
		int half = HauntedCarousel.WIDTH / 2;
		for (int x = -half; x <= half; x++) {
			for (int z = -half; z <= half; z++) {
				for (int up = 0; up < HEIGHT; up++) {
					if (x == 0 && z == 0 && up == 0) {
						continue;
					}
					BlockPos pos = centre.offset(x, up, z);
					if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.getBlockState(pos).canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/** The carousels turning round the middle at {@code pos}. */
	public static List<HauntedCarousel> carousels(Level level, BlockPos pos) {
		return level.getEntitiesOfClass(HauntedCarousel.class, new AABB(pos).inflate(0.5), carousel -> carousel.centre().equals(pos));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/** Facing whoever places it, if the carousel has room. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		if (!clear(context.getLevel(), context.getClickedPos())) {
			if (context.getPlayer() instanceof ServerPlayer player) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.haunted_carousel.no_room", HauntedCarousel.WIDTH,
						HauntedCarousel.WIDTH, HEIGHT));
			}
			return null;
		}
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/** Sets the carousel turning round it. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel server && carousels(server, pos).isEmpty()) {
			HauntedCarousel.raise(server, pos);
		}
	}

	/** Boards the nearest free mount (setting the carousel up again first, should it have gone); sneaking, switches the organ. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer rider)) {
			return InteractionResult.SUCCESS;
		}
		if (player.isSecondaryUseActive()) {
			boolean organ = !state.getValue(ORGAN);
			level.setBlock(pos, state.setValue(ORGAN, organ), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, organ ? 0.8F : 0.6F);
			rider.sendOverlayMessage(Component.translatable(organ ? "message.jugcraft.haunted_carousel.organ_on" : "message.jugcraft.haunted_carousel.organ_off"));
			return InteractionResult.SUCCESS;
		}
		List<HauntedCarousel> found = carousels(server, pos);
		HauntedCarousel carousel = found.isEmpty() ? HauntedCarousel.raise(server, pos) : found.get(0);
		if (carousel != null) {
			carousel.board(rider);
		}
		return InteractionResult.SUCCESS;
	}

	/** The middle gone, the carousel goes: its riders are set down, and it is taken away. */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (level.getBlockState(pos).getBlock() instanceof HauntedCarouselBlock) {
			return;
		}
		for (HauntedCarousel carousel : carousels(level, pos)) {
			carousel.ejectPassengers();
			carousel.discard();
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HauntedCarouselBlockEntity(pos, state);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, ORGAN);
	}
}
