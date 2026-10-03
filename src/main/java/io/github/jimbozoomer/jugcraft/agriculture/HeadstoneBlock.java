package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A headstone of the graveyard pack (tools/graveyard.py), one of the {@link Style}s: one to three blocks, placed facing
 * whoever placed it and broken as one, part 0 holding the {@link HeadstoneBlockEntity} with its {@link Epitaph}.
 *
 * <p>It weathers, as stone in a churchyard does: clean, worn, mossy, overgrown ({@link #WEATHERING}, the same on every
 * part). Each random tick of an unwaxed headstone takes the next stage {@value #AGE_CHANCE} of the time, twice that
 * with open sky above it. A Brush scrubs a stage off; Honeycomb waxes it so it weathers no more, and an axe scrapes the
 * wax off again; Bone Meal ages it a stage at once. The Stonemason's Chisel opens the epitaph screen ({@link Epitaphs});
 * a named Name Tag cuts its name as the first line. Everything that changes it needs build rights.
 *
 * <p>Neglect wakes the dead: at night a headstone stirs restless spirits ({@link Spirits#stir}) the more often the
 * further it has weathered ({@link #STIR}).
 */
public class HeadstoneBlock extends BaseEntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
	public static final IntegerProperty WEATHERING = IntegerProperty.create("weathering", 0, 3);
	public static final BooleanProperty WAXED = BooleanProperty.create("waxed");
	public static final int CLEAN = 0;
	public static final int OVERGROWN = 3;
	public static final float AGE_CHANCE = 0.02F;
	public static final int SKY_FACTOR = 2;
	/** How much more often than other graves a headstone stirs a spirit, for each stage. */
	public static final float[] STIR = {0.25F, 0.5F, 1.0F, 1.5F};

	static final int[][] SINGLE = {{0, 0, 0}};
	static final int[][] TALL2 = {{0, 0, 0}, {0, 1, 0}};
	static final int[][] TALL3 = {{0, 0, 0}, {0, 1, 0}, {0, 2, 0}};
	static final int[][] LONG = {{0, 0, 0}, {0, 0, 1}};
	static final int[][] TALL4 = {{0, 0, 0}, {0, 1, 0}, {0, 2, 0}, {0, 3, 0}};
	/** The Angel at the Tomb: the altar's two halves and, above the one to the placer's right, the angel's wings. */
	static final int[][] WIDE = {{0, 0, 0}, {1, 0, 0}, {1, 1, 0}};

	/** A stone: the colour of letters cut into it, and the colour they fade towards as it weathers. */
	public enum Stone {
		MARBLE(0xFF2C2B30, 0xFFB9B7B0),
		SLATE(0xFFD3D8DE, 0xFF4B525C),
		GRANITE(0xFFE6DFD6, 0xFF7A7270),
		SANDSTONE(0xFF3E2C1A, 0xFFB59C6E),
		IRON(0xFFC9A961, 0xFF6E3A1E);

		public final int ink;
		public final int fadeTo;

		Stone(int ink, int fadeTo) {
			this.ink = ink;
			this.fadeTo = fadeTo;
		}
	}

	/**
	 * Where the epitaph is cut, facing north, in pixels: on a FRONT face whose plane is z = {@code z}, or on a TOP face at
	 * height {@code y}; centred at ({@code x}, {@code y}) or ({@code x}, {@code z}), {@code width} by {@code height}; no
	 * letter larger than {@code maxScale} blocks per font pixel.
	 */
	public record Text(boolean top, float x, float y, float z, float width, float height, float maxScale) {
	}

	/** Each headstone: its id, stone, cells (right, up, back), boxes per part (facing north) and where its epitaph goes. */
	public enum Style {
		GOTHIC("gothic_headstone", Stone.MARBLE, SINGLE,
				new double[][][] {{{1.5, 0, 4.5, 14.5, 2, 11.5}, {2.5, 2, 6, 13.5, 16, 9.5}, {4.5, 16, 6.5, 11.5, 20, 9.5}}},
				new Text(false, 8.0F, 8.0F, 6.5F, 6.8F, 7.6F, 1.0F / 64)),
		WILLOW_URN("willow_urn_headstone", Stone.SLATE, SINGLE,
				new double[][][] {{{1.5, 0, 7, 14.5, 14, 9}, {4.5, 14, 7, 11.5, 17.5, 9}}},
				new Text(false, 8.0F, 6.6F, 7.25F, 9.2F, 9.4F, 1.0F / 64)),
		WINGED_SKULL("winged_skull_headstone", Stone.SLATE, SINGLE,
				new double[][][] {{{1.5, 0, 7, 14.5, 14, 9}, {4.5, 14, 7, 11.5, 17.5, 9}}},
				new Text(false, 8.0F, 6.6F, 7.25F, 9.2F, 9.4F, 1.0F / 64)),
		LAMB("lamb_headstone", Stone.MARBLE, SINGLE,
				new double[][][] {{{3, 0, 5, 13, 2, 11}, {3.7, 2, 6.2, 12.3, 9.5, 9.8}, {4, 9.5, 6.3, 12.2, 13, 9.2}}},
				new Text(false, 8.0F, 5.6F, 6.5F, 5.8F, 5.0F, 1.0F / 80)),
		BROKEN_COLUMN("broken_column", Stone.MARBLE, TALL2,
				new double[][][] {{{2.5, 0, 2.5, 13.5, 3, 13.5}, {3.5, 3, 3.5, 12.5, 12.4, 12.5}, {4.6, 12.4, 4.6, 11.4, 16, 11.4}},
						{{4.5, 0, 4.5, 11.5, 12.6, 11.5}}},
				new Text(false, 8.0F, 7.1F, 3.5F, 6.4F, 5.0F, 1.0F / 80)),
		CELTIC_CROSS("celtic_cross", Stone.GRANITE, TALL3,
				new double[][][] {{{1, 0, 3, 15, 1.5, 13}, {2, 1.5, 4, 14, 6.5, 12}, {2.5, 6.5, 4.5, 13.5, 7.5, 11.5}, {5, 7.5, 5.5, 11, 16, 10.5}},
						{{5, 0, 5.5, 11, 16, 10.5}, {0, 14, 5.5, 16, 16, 10.5}},
						{{0, 0, 5.5, 16, 4, 10.5}, {1.5, 0, 6, 14.5, 9, 10}, {5, 0, 5.5, 11, 12, 10.5}}},
				new Text(false, 8.0F, 4.0F, 4.0F, 10.4F, 4.2F, 1.0F / 96)),
		RUSTIC_SCROLL("rustic_scroll_headstone", Stone.GRANITE, SINGLE,
				new double[][][] {{{1, 0, 4.2, 15, 10, 11.4}, {3, 10, 5.4, 13, 15, 10.6}}},
				new Text(false, 8.0F, 7.0F, 4.6F, 8.0F, 6.4F, 1.0F / 64)),
		TABLE_TOMB("table_tomb", Stone.SANDSTONE, LONG,
				new double[][][] {{{0.5, 0, 0.5, 15.5, 12.6, 16}}, {{0.5, 0, 0, 15.5, 12.6, 15.5}}},
				new Text(true, 8.0F, 12.6F, 16.0F, 11.0F, 24.0F, 1.0F / 40)),
		LEDGER("ledger_stone", Stone.SANDSTONE, LONG,
				new double[][][] {{{1, 0, 1, 15, 2.4, 16}}, {{1, 0, 0, 15, 2.4, 15}}},
				new Text(true, 8.0F, 2.4F, 10.2F, 9.6F, 14.0F, 1.0F / 48)),
		// Pack 2: monuments.
		GRAND_OBELISK("grand_obelisk", Stone.GRANITE, TALL4,
				new double[][][] {{{0, 0, 0, 16, 2, 16}, {1, 2, 1, 15, 4, 15}, {2.5, 4, 2.5, 13.5, 16, 13.5}},
						{{2.6, 0, 2.6, 13.4, 0.8, 13.4}, {3.5, 0.8, 3.5, 12.5, 2.5, 12.5}, {3.7, 2.5, 3.7, 12.3, 16, 12.3}},
						{{4.4, 0, 4.4, 11.6, 16, 11.6}}, {{5.2, 0, 5.2, 10.8, 12, 10.8}}},
				new Text(false, 8.0F, 10.0F, 2.2F, 8.2F, 7.2F, 1.0F / 72)),
		DRAPED_URN("draped_urn", Stone.MARBLE, TALL2,
				new double[][][] {{{2, 0, 2, 14, 2.8, 14}, {3, 2.8, 3, 13, 13, 13}, {2.4, 13, 2.4, 13.6, 15, 13.6}, {4, 15, 4, 12, 16, 12}},
						{{4, 0, 4, 12, 12.2, 12}}},
				new Text(false, 8.0F, 8.0F, 3.0F, 7.6F, 6.6F, 1.0F / 80)),
		ANGEL_AT_THE_TOMB("angel_at_the_tomb", Stone.MARBLE, WIDE,
				new double[][][] {{{0, 0, 0, 16, 2.4, 16}, {0, 2.4, 2.5, 14, 14, 14}},
						{{0, 0, 0, 16, 2.4, 16}, {10, 2.4, 2.5, 16, 14, 14}, {1, 2.4, 4, 10, 16, 12}},
						{{0, 0, 5, 9, 12, 11}}},
				new Text(false, 3.5F, 7.2F, 2.5F, 11.5F, 6.0F, 1.0F / 64)),
		TRUMPETING_ANGEL("trumpeting_angel", Stone.MARBLE, TALL4,
				new double[][][] {{{1, 0, 1, 15, 2.8, 15}, {2, 2.8, 2, 14, 12, 14}, {1.5, 12, 1.5, 14.5, 14.2, 14.5}, {4, 14.2, 4, 12, 16, 12}},
						{{5, 0, 5, 11, 16, 11}}, {{5, 0, 5, 11, 8, 11}, {3.6, 8, 3.6, 12.4, 11.6, 12.4}}, {{5, 0, 4, 11, 15, 12}}},
				new Text(false, 8.0F, 7.4F, 2.0F, 8.8F, 6.0F, 1.0F / 72)),
		MORTSAFE("mortsafe", Stone.IRON, LONG,
				new double[][][] {{{0.5, 0, 0.5, 15.5, 13.9, 16}}, {{0.5, 0, 0, 15.5, 13.9, 15.5}}},
				new Text(false, 8.0F, 6.9F, 0.4F, 5.4F, 2.8F, 1.0F / 96)),
		FAITHFUL_HOUND("faithful_hound", Stone.GRANITE, SINGLE,
				new double[][][] {{{1, 0, 2, 15, 6.2, 14}, {1, 6.2, 4.5, 14.5, 12.5, 12}}},
				new Text(false, 8.0F, 3.3F, 2.6F, 11.0F, 3.6F, 1.0F / 80));

		public final String id;
		public final Stone stone;
		public final int[][] cells;
		public final Text text;
		private final VoxelShape[][] shapes;

		Style(String id, Stone stone, int[][] cells, double[][][] boxes, Text text) {
			this.id = id;
			this.stone = stone;
			this.cells = cells;
			this.text = text;
			shapes = new VoxelShape[cells.length][4];
			for (int part = 0; part < cells.length; part++) {
				for (Direction facing : Direction.Plane.HORIZONTAL) {
					VoxelShape shape = Shapes.empty();
					for (double[] b : boxes[part]) {
						shape = Shapes.or(shape, turned(b, facing));
					}
					shapes[part][facing.get2DDataValue()] = shape;
				}
			}
		}

		/** A box given facing north, turned to face {@code facing} about the block's centre. */
		private static VoxelShape turned(double[] b, Direction facing) {
			return switch (facing) {
				case SOUTH -> Block.box(16 - b[3], b[1], 16 - b[5], 16 - b[0], b[4], 16 - b[2]);
				case EAST -> Block.box(16 - b[5], b[1], b[0], 16 - b[2], b[4], b[3]);
				case WEST -> Block.box(b[2], b[1], 16 - b[3], b[5], b[4], 16 - b[0]);
				default -> Block.box(b[0], b[1], b[2], b[3], b[4], b[5]);
			};
		}
	}

	private final Style style;

	public HeadstoneBlock(Properties properties, Style style) {
		super(properties);
		this.style = style;
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0).setValue(WEATHERING, CLEAN)
				.setValue(WAXED, false));
	}

	public Style style() {
		return style;
	}

	public static int stage(BlockState state) {
		return state.getValue(WEATHERING);
	}

	// ---------------------------------------------------------------- parts

	/** Where part {@code part} of the headstone with its part 0 at {@code master}, facing {@code facing}, is. */
	public BlockPos partPos(BlockPos master, Direction facing, int part) {
		int[] cell = style.cells[part];
		return master.relative(facing.getCounterClockWise(), cell[0]).above(cell[1]).relative(facing.getOpposite(), cell[2]);
	}

	/** Where part 0 of the headstone this block belongs to is. */
	public BlockPos masterPos(BlockPos pos, BlockState state) {
		int[] cell = style.cells[Math.min(state.getValue(PART), style.cells.length - 1)];
		Direction facing = state.getValue(FACING);
		return pos.relative(facing.getCounterClockWise(), -cell[0]).below(cell[1]).relative(facing.getOpposite(), -cell[2]);
	}

	private boolean isPart(Level level, BlockPos pos, int part, Direction facing) {
		BlockState state = level.getBlockState(pos);
		return state.is(this) && state.getValue(PART) == part && state.getValue(FACING) == facing;
	}

	/** Placed facing the player, every block it needs free. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		Level level = context.getLevel();
		BlockPos master = context.getClickedPos();
		for (int part = 1; part < style.cells.length; part++) {
			BlockPos pos = partPos(master, facing, part);
			if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
					|| !level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, Direction.UP))) {
				return null;
			}
		}
		return defaultBlockState().setValue(FACING, facing);
	}

	/** Fills in the rest of it. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide()) {
			return;
		}
		Direction facing = state.getValue(FACING);
		for (int part = 1; part < style.cells.length; part++) {
			level.setBlock(partPos(pos, facing, part), state.setValue(PART, part), Block.UPDATE_ALL);
		}
	}

	/** Breaking any block breaks the headstone: part 0 drops it once, with its epitaph; the rest go without drops. */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (level.getBlockState(pos).is(this)) {
			return; // Weathered, scrubbed or waxed, not removed.
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		if (state.getValue(PART) != 0) {
			if (isPart(level, master, 0, facing)) {
				level.destroyBlock(master, true);
			}
			return;
		}
		for (int part = 1; part < style.cells.length; part++) {
			BlockPos partPos = partPos(master, facing, part);
			if (isPart(level, partPos, part, facing)) {
				level.removeBlock(partPos, false);
			}
		}
	}

	/** In creative, breaking any block takes the whole headstone away without dropping it. */
	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide() && player.getAbilities().instabuild && state.getValue(PART) != 0) {
			BlockPos master = masterPos(pos, state);
			if (level.getBlockState(master).is(this)) {
				level.removeBlock(master, false);
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PART) == 0 ? new HeadstoneBlockEntity(pos, state) : null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int part = Math.min(state.getValue(PART), style.cells.length - 1);
		return style.shapes[part][state.getValue(FACING).get2DDataValue()];
	}

	// ---------------------------------------------------------------- weathering

	/** Sets the stage and wax of every part of the headstone with its part 0 at {@code master}. */
	public void setWeather(Level level, BlockPos master, int stage, boolean waxed) {
		BlockState masterState = level.getBlockState(master);
		if (!masterState.is(this)) {
			return;
		}
		Direction facing = masterState.getValue(FACING);
		for (int part = 0; part < style.cells.length; part++) {
			BlockPos pos = partPos(master, facing, part);
			BlockState state = level.getBlockState(pos);
			if (isPart(level, pos, part, facing)) {
				level.setBlock(pos, state.setValue(WEATHERING, stage).setValue(WAXED, waxed), Block.UPDATE_ALL);
			}
		}
	}

	/** Only part 0 weathers (and the rest follow it). */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(PART) == 0;
	}

	/** Weathers now and then; at night the grave may stir, the more often the more neglected. */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int stage = state.getValue(WEATHERING);
		if (!state.getValue(WAXED) && stage < OVERGROWN && JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE)) {
			float chance = AGE_CHANCE * (level.canSeeSky(pos.above(style.cells.length)) ? SKY_FACTOR : 1);
			if (random.nextFloat() < chance) {
				setWeather(level, pos, stage + 1, false);
			}
		}
		Spirits.stir(level, pos, random, STIR[stage]);
	}

	/** The brush, honeycomb, axe, bone meal, chisel and a named Name Tag; anything else does what it would anywhere. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean chisel = stack.is(JugcraftAgriculture.item(Epitaphs.CHISEL));
		boolean tag = stack.is(Items.NAME_TAG);
		boolean brush = stack.is(Items.BRUSH);
		boolean honeycomb = stack.is(Items.HONEYCOMB);
		boolean axe = stack.is(ItemTags.AXES) && state.getValue(WAXED);
		boolean boneMeal = stack.is(Items.BONE_MEAL);
		if (!chisel && !tag && !brush && !honeycomb && !axe && !boneMeal) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!player.mayBuild()) {
			return InteractionResult.PASS;
		}
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer worker)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos master = masterPos(pos, state);
		if (!level.getBlockState(master).is(this)) {
			return InteractionResult.PASS;
		}
		int stage = state.getValue(WEATHERING);
		boolean waxed = state.getValue(WAXED);
		if (chisel) {
			Epitaphs.open(worker, master);
		} else if (tag) {
			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name == null || name.getString().isBlank()) {
				worker.sendOverlayMessage(Component.translatable("message.jugcraft.gravestone.unnamed_tag"));
			} else if (level.getBlockEntity(master) instanceof HeadstoneBlockEntity stone) {
				stone.engrave(stone.epitaph().withFirstLine(name.getString()));
				level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		} else if (brush) {
			if (stage == CLEAN) {
				worker.sendOverlayMessage(Component.translatable("message.jugcraft.headstone.clean"));
				return InteractionResult.SUCCESS;
			}
			setWeather(level, master, stage - 1, waxed);
			stack.hurtAndBreak(1, player, hand);
			level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
			server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MOSS_BLOCK.defaultBlockState()),
					hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 12, 0.2, 0.2, 0.2, 0.05);
			if (stage == OVERGROWN) {
				TrickOrTreat.award(worker, "groundskeeper");
			}
		} else if (honeycomb) {
			if (waxed) {
				return InteractionResult.PASS;
			}
			setWeather(level, master, stage, true);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			server.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.35, 0.35, 0.35, 0.02);
		} else if (axe) {
			setWeather(level, master, stage, false);
			stack.hurtAndBreak(1, player, hand);
			level.playSound(null, pos, SoundEvents.AXE_WAX_OFF.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
			server.sendParticles(ParticleTypes.WAX_OFF, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.35, 0.35, 0.35, 0.02);
		} else {
			if (waxed) {
				worker.sendOverlayMessage(Component.translatable("message.jugcraft.headstone.waxed"));
				return InteractionResult.SUCCESS;
			}
			if (stage == OVERGROWN) {
				worker.sendOverlayMessage(Component.translatable("message.jugcraft.headstone.overgrown"));
				return InteractionResult.SUCCESS;
			}
			setWeather(level, master, stage + 1, false);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			server.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.4, 0.3, 0.4, 0.0);
		}
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
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
		builder.add(FACING, PART, WEATHERING, WAXED);
	}
}
