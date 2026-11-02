package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Hurricane Lamp (Cozy Fall Hearth, part 5; tools/hearth.py LAMP): a table oil lamp 37 cm high, a brass font on a
 * footed base, its burner with a wick knob, and a tall glass chimney swelling round the flame. Flint and steel or a fire
 * charge lights it and an empty hand turns its wick down ({@link CandleLighting}). Lit, its chimney glows from the flame
 * inside (the blockstate's lit glass, full-bright, in steady bands) and it gives light {@value #LIGHT}. It burns no oil.
 * Its model is tools/hearth_data.py hurricane_lamp(). (Set 3's Barn Lantern is the one for a wall.)
 */
public class HurricaneLampBlock extends FacingDecorationBlock {
	public static final int LIGHT = 12;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public HurricaneLampBlock(Properties properties) {
		super(properties, new double[] {6.25, 0.0, 6.25, 9.75, 6.0, 9.75});
		registerDefaultState(defaultBlockState().setValue(LIT, false));
	}

	/** The light it gives: {@value #LIGHT} while lit. */
	public static int light(BlockState state) {
		return state.getValue(LIT) ? LIGHT : 0;
	}

	/** Flint and steel or a fire charge lights it; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		InteractionResult lighting = CandleLighting.light(stack, state, level, pos, player, hand, LIT);
		return lighting == InteractionResult.TRY_WITH_EMPTY_HAND && !stack.isEmpty() ? InteractionResult.PASS : lighting;
	}

	/** An empty hand turns its wick down. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return CandleLighting.snuff(state, level, pos, player, LIT);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIT);
	}
}
