package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
 * The Steam Traction Engine (the Harvest Festival Grounds, decor set 10, part 3; tools/harvest_festival.py ENGINE): a
 * farm traction engine of about 1910 at its real size, 5 m long, 2.2 m wide and 3 m to its chimney's top. Three blocks
 * across, three up and five deep, placed and broken as one ({@link MultiDecorationBlock}). Use coal or charcoal on it to
 * fire it: each burns {@value #BURN} ticks, as in a furnace, and the firebox holds up to {@value #MAX_FUEL}
 * ({@link SteamTractionEngineBlockEntity}, on its first block). While it is fired ({@link #LIT}, on every block) its
 * flywheel turns once every {@value #TURN_TICKS} ticks with its crank, connecting rod, crosshead and governor, its fire
 * glows through its door (client/SteamTractionEngineRenderer), and smoke rises from its chimney; when its coal is
 * gone it runs down and stops. An empty hand tells how much coal is left. Firing it changes the world, so it needs
 * build permission. Its model is tools/harvest_festival_data.py traction_engine().
 */
public class SteamTractionEngineBlock extends MultiDecorationBlock implements EntityBlock {
	/** Its blocks: 3 across, 3 up, 5 deep (tools/harvest_festival.py ENGINE["size"]). */
	private static final int[][] CELLS = box(3, 3, 5);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Ticks a coal or charcoal fires it (a furnace's), and the most the firebox holds. */
	public static final int BURN = 1600;
	public static final int MAX_FUEL = 6400;
	/** Ticks the flywheel takes to turn once while it is fired (150 turns a minute). */
	public static final int TURN_TICKS = 8;
	/** The crankshaft's middle in its frame's pixels (across, height, depth), the crank's throw and the connecting rod's length. */
	public static final float CRANK_X = 28.0F;
	public static final float CRANK_Y = 34.0F;
	public static final float CRANK_Z = 45.0F;
	public static final float THROW = 2.5F;
	public static final float ROD = 15.0F;
	/** The governor's spindle (across, depth) and the chimney's mouth (across, height, depth), in its frame's pixels. */
	public static final float GOVERNOR_X = 20.0F;
	public static final float GOVERNOR_Z = 28.0F;
	public static final float CHIMNEY_X = 24.0F;
	public static final float CHIMNEY_Y = 47.0F;
	public static final float CHIMNEY_Z = 7.0F;
	/** The engine in rough boxes, facing north in its frame: the front wheels, the smokebox and chimney, the barrel, the firebox, the rear wheels, the cylinder and motion, the crankshaft and flywheel, the footplate and tender. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {9.5, 0.0, 3.0, 38.5, 18.0, 21.0},
			new double[] {16.0, 14.0, 3.0, 32.0, 30.0, 12.0}, new double[] {21.5, 30.0, 4.5, 26.5, 47.0, 9.5}, new double[] {17.0, 15.0, 12.0, 31.0, 29.0, 44.0},
			new double[] {16.0, 8.0, 44.0, 32.0, 30.0, 56.0}, new double[] {1.0, 0.0, 44.0, 8.0, 28.0, 72.0}, new double[] {40.0, 0.0, 44.0, 47.0, 28.0, 72.0},
			new double[] {24.0, 29.0, 13.5, 32.0, 38.0, 36.0}, new double[] {15.0, 22.0, 40.0, 33.0, 38.0, 50.0},
			new double[] {37.5, 23.5, 34.5, 40.5, 44.5, 55.5}, new double[] {10.0, 8.0, 56.0, 38.0, 29.0, 80.0});

	public SteamTractionEngineBlock(Properties properties) {
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

	/** Placed cold. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		return placed == null ? null : placed.setValue(LIT, false);
	}

	/**
	 * Where a point of its frame (pixels: across from its far end, up, back from its front, for an engine facing north)
	 * is in the world, for the engine whose first block is at {@code master} facing {@code facing}. The first block is
	 * the frame's last 16 pixels across (tools/decor17_data.py frame_cell); the frame is turned about its middle as the
	 * blocks' models are, clockwise seen from above by 90 degrees a facing on from north.
	 */
	public static Vec3 inWorld(BlockPos master, Direction facing, double x, double y, double z) {
		double dx = (x - 16.0 * CELLS[CELLS.length - 1][0] - 8.0) / 16.0;
		double dz = (z - 8.0) / 16.0;
		double angle = -Math.toRadians(facing.get2DDataValue() * 90.0 + 180.0);
		double wx = dx * Math.cos(angle) + dz * Math.sin(angle);
		double wz = -dx * Math.sin(angle) + dz * Math.cos(angle);
		return new Vec3(master.getX() + 0.5 + wx, master.getY() + y / 16.0, master.getZ() + 0.5 + wz);
	}

	/** Fires or lets out the engine whose first block is at {@code master}, on every block of it. */
	public void setLit(Level level, BlockPos master, boolean lit) {
		BlockState first = level.getBlockState(master);
		if (!first.is(this)) {
			return;
		}
		Direction facing = first.getValue(FACING);
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState state = level.getBlockState(at);
			if (state.is(this) && part(state) == part && state.getValue(FACING) == facing && state.getValue(LIT) != lit) {
				level.setBlock(at, state.setValue(LIT, lit), Block.UPDATE_ALL);
			}
		}
	}

	/** Coal or charcoal fires it, if the firebox has room for its burn, using it up. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(ItemTags.COALS)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!player.mayBuild() || !level.mayInteract(player, pos)) {
			return InteractionResult.FAIL;
		}
		BlockPos master = masterPos(pos, state);
		if (!(level.getBlockEntity(master) instanceof SteamTractionEngineBlockEntity engine)) {
			return InteractionResult.PASS;
		}
		if (!engine.addFuel(BURN)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.steam_traction_engine.full"));
			return InteractionResult.SUCCESS;
		}
		stack.consume(1, player);
		setLit(level, master, true);
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Tells how many seconds of coal are left in the firebox, or that it wants firing. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(masterPos(pos, state)) instanceof SteamTractionEngineBlockEntity engine) {
			player.sendOverlayMessage(engine.fuel() > 0
					? Component.translatable("message.jugcraft.steam_traction_engine.fired", (engine.fuel() + 19) / 20)
					: Component.translatable("message.jugcraft.steam_traction_engine.cold"));
		}
		return InteractionResult.SUCCESS;
	}

	/** Fired, smoke rises from its chimney, and its fire crackles now and then. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (part(state) != MASTER || !state.getValue(LIT)) {
			return;
		}
		Vec3 mouth = inWorld(pos, state.getValue(FACING), CHIMNEY_X, CHIMNEY_Y, CHIMNEY_Z);
		level.addParticle(ParticleTypes.LARGE_SMOKE, mouth.x + (random.nextDouble() - 0.5) * 0.1, mouth.y, mouth.z + (random.nextDouble() - 0.5) * 0.1,
				0.0, 0.07, 0.0);
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, mouth.x, mouth.y + 0.1, mouth.z, 0.0, 0.05, 0.0);
		}
		if (random.nextInt(10) == 0) {
			level.playLocalSound(mouth.x, pos.getY() + 1.0, mouth.z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 0.9F, false);
		}
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new SteamTractionEngineBlockEntity(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || type != JugcraftAgriculture.STEAM_TRACTION_ENGINE_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((SteamTractionEngineBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos);
	}
}
