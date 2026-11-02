package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Caramel Apple Stand (Orchard & Apple Harvest, decor set 2, part 3; tools/orchard_harvest.py STAND): a wooden
 * counter two blocks across, CARAMEL APPLES painted on its front, with a copper dipping pot on a little burner, a jar of
 * sticks and a cooling tray on top; placed and broken as one ({@link MultiDecorationBlock}). Use Caramel on it to fill
 * the pot (up to {@value #CARAMEL} servings) and sticks to fill the jar (up to {@value #STICKS}); use an apple on it to
 * dip one: an apple, a stick and a serving make a caramel apple, as the crafting recipe does, stood on the tray (up to
 * {@value #RACK}). After {@value #COOL_TICKS} ticks it has set, and an empty hand takes it. Its stock shows on every
 * block ({@link #POT}, {@link #JAR}, {@link #RACK_APPLES}); while there is caramel the burner burns, lighting it
 * {@value #LIGHT}, and the caramel bubbles (animated textures). Its first block keeps the stock
 * ({@link CaramelAppleStandBlockEntity}); breaking it gives it all back. It has no lanterns ({@link #LIT} stays on).
 */
public class CaramelAppleStandBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int CARAMEL = 8;
	public static final int STICKS = 16;
	public static final int RACK = 6;
	public static final int COOL_TICKS = 600;
	public static final int LIGHT = 6;
	/** Its blocks: 2 across, 1 up, 1 deep (tools/orchard_harvest.py STAND["size"]). */
	private static final int[][] CELLS = box(2, 1, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** The caramel in the pot: none, some (up to half), or plenty. */
	public static final IntegerProperty POT = IntegerProperty.create("pot", 0, 2);
	/** The sticks in the jar: none, some (up to half), or plenty. */
	public static final IntegerProperty JAR = IntegerProperty.create("jar", 0, 2);
	public static final IntegerProperty RACK_APPLES = IntegerProperty.create("rack", 0, RACK);
	/** The counter; the burner and pot, the jar, and the tray on top. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 1.5, 32.0, 14.5, 14.5},
			new double[] {2.75, 14.5, 4.0, 10.25, 21.0, 11.0}, new double[] {12.0, 14.5, 5.5, 15.0, 19.5, 8.5},
			new double[] {18.0, 14.5, 4.0, 30.0, 15.25, 12.0});

	public CaramelAppleStandBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POT, 0).setValue(JAR, 0).setValue(RACK_APPLES, 0));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** How bright the burner makes it: lit while there is caramel to keep warm. */
	public static int light(BlockState state) {
		return state.getValue(POT) > 0 ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** The stand's stock, on its first block, from any of its blocks. */
	public @Nullable CaramelAppleStandBlockEntity stock(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof CaramelAppleStandBlockEntity stock ? stock : null;
	}

	/** Shows {@code caramel} servings, {@code sticks} and {@code apples} on the tray on every block of the stand at {@code pos}. */
	public void show(Level level, BlockPos pos, BlockState state, int caramel, int sticks, int apples) {
		Direction facing = state.getValue(FACING);
		int pot = caramel == 0 ? 0 : caramel <= CARAMEL / 2 ? 1 : 2;
		int jar = sticks == 0 ? 0 : sticks <= STICKS / 2 ? 1 : 2;
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(pos, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(POT, pot).setValue(JAR, jar).setValue(RACK_APPLES, apples), Block.UPDATE_CLIENTS);
			}
		}
	}

	/** Caramel fills the pot, sticks the jar, and an apple is dipped; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean caramel = stack.is(JugcraftAgriculture.item("caramel"));
		boolean stick = stack.is(Items.STICK);
		boolean apple = stack.is(Items.APPLE);
		if (!caramel && !stick && !apple) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		CaramelAppleStandBlockEntity stock = stock(level, pos, state);
		if (level.isClientSide() || stock == null) {
			return InteractionResult.SUCCESS;
		}
		String refusal = null;
		SoundEvent sound;
		if (caramel) {
			refusal = stock.addCaramel() ? null : "pot_full";
			sound = SoundEvents.HONEY_BLOCK_PLACE;
		} else if (stick) {
			refusal = stock.addStick() ? null : "jar_full";
			sound = SoundEvents.WOOD_HIT;
		} else {
			if (stock.caramel() == 0) {
				refusal = "no_caramel";
			} else if (stock.sticks() == 0) {
				refusal = "no_sticks";
			} else if (!stock.dip(level.getGameTime())) {
				refusal = "rack_full";
			}
			sound = SoundEvents.HONEY_BLOCK_SLIDE;
		}
		if (refusal != null) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.caramel_apple_stand." + refusal));
			return InteractionResult.CONSUME;
		}
		stack.consume(1, player);
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Takes a caramel apple that has set (it has no lanterns to light). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(RACK_APPLES) == 0) {
			return InteractionResult.PASS;
		}
		CaramelAppleStandBlockEntity stock = stock(level, pos, state);
		if (level.isClientSide() || stock == null) {
			return InteractionResult.SUCCESS;
		}
		ItemStack taken = stock.takeSet(level.getGameTime());
		if (taken.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.caramel_apple_stand.setting"));
			return InteractionResult.CONSUME;
		}
		if (!player.getInventory().add(taken)) {
			Block.popResource(level, pos.above(), taken);
		}
		level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.4F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new CaramelAppleStandBlockEntity(pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POT, JAR, RACK_APPLES);
	}
}
