package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Stone Fireplace (Cozy Fall Hearth, part 1; tools/hearth.py FIREPLACE): a fieldstone chimney breast three blocks
 * across and three up on a flagstone hearth, its firebox opening in the middle of its lowest row, an oak mantel shelf
 * across it and the stack stepping in above ({@link MultiDecorationBlock}). It is placed out ({@link #LIT} false). Flint
 * and steel or a fire charge lights it; a shovel puts it out. While lit its fire burns in the firebox (a full-bright
 * model in an animated texture, added by the blockstate) and its firebox block gives light {@value #LIGHT}. A Mantel
 * Garland used on it drapes its mantel ({@link #GARLAND}, on every block, drawn on the mantel's); sneaking with an empty
 * hand takes the garland back. It burns no fuel and makes nothing. Its model is tools/hearth_data.py fireplace().
 */
public class StoneFireplaceBlock extends MultiDecorationBlock {
	public static final int LIGHT = 15;
	/** Its blocks: 3 across, 3 up, 1 deep (tools/hearth.py FIREPLACE["size"]); part 1 is the firebox's. */
	private static final int[][] CELLS = box(3, 3, 1);
	public static final int FIREBOX = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final BooleanProperty GARLAND = BooleanProperty.create("garland");
	private static final TagKey<Item> IGNITERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("creeper_igniters"));
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS,
			new double[] {0.0, 0.0, 6.0, 48.0, 1.5, 16.0}, new double[] {4.0, 0.0, 0.0, 44.0, 1.5, 6.0},
			new double[] {0.0, 1.5, 6.0, 14.0, 22.0, 16.0}, new double[] {34.0, 1.5, 6.0, 48.0, 22.0, 16.0},
			new double[] {14.0, 14.0, 6.0, 34.0, 22.0, 16.0}, new double[] {14.0, 1.5, 14.0, 34.0, 14.0, 16.0},
			new double[] {2.0, 22.0, 3.0, 46.0, 24.5, 16.0}, new double[] {4.0, 24.5, 8.0, 44.0, 48.0, 16.0});

	public StoneFireplaceBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(GARLAND, false));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** The light it gives: its firebox block, while lit. */
	public static int light(BlockState state) {
		return state.getValue(LIT) && state.getValue(PART) == FIREBOX ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** Sets {@code property} to {@code value} on every block of the fireplace whose master is at {@code master}. */
	private void setAll(Level level, BlockPos master, Direction facing, BooleanProperty property, boolean value) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(property, value), Block.UPDATE_ALL);
			}
		}
	}

	/** Flint and steel or a fire charge lights it, a shovel puts it out, a garland drapes its mantel. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean lighter = stack.is(IGNITERS);
		boolean shovel = stack.is(ItemTags.SHOVELS);
		boolean garland = stack.is(JugcraftAgriculture.item("mantel_garland"));
		if (!lighter && !shovel && !garland) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		boolean lit = state.getValue(LIT);
		if ((lighter && lit) || (shovel && !lit) || (garland && state.getValue(GARLAND))) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		if (garland) {
			setAll(level, master, facing, GARLAND, true);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
		} else {
			setAll(level, master, facing, LIT, lighter);
			BlockPos box = partPos(master, facing, FIREBOX);
			if (lighter) {
				boolean worn = stack.isDamageableItem();
				level.playSound(null, box, worn ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F,
						level.getRandom().nextFloat() * 0.4F + 0.8F);
				if (worn) {
					stack.hurtAndBreak(1, player, hand);
				} else {
					stack.consume(1, player);
				}
			} else {
				level.playSound(null, box, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
			}
		}
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Sneaking with an empty hand takes the garland back; otherwise nothing (no lanterns to light by hand). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!player.isSecondaryUseActive() || !state.getValue(GARLAND)) {
			if (!state.getValue(LIT) && !level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.stone_fireplace.light"));
			}
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			setAll(level, masterPos(pos, state), state.getValue(FACING), GARLAND, false);
			ItemStack back = new ItemStack(JugcraftAgriculture.item("mantel_garland"));
			if (!player.getInventory().add(back)) {
				Block.popResource(level, pos, back);
			}
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** While lit, the fire crackles and a little smoke rises in the firebox (each client's own, near it). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT) || part(state) != FIREBOX) {
			return;
		}
		if (random.nextInt(8) == 0) {
			level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
					0.6F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
		}
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.7, pos.getZ() + 0.5, 0.0, 0.03, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(GARLAND);
	}
}
