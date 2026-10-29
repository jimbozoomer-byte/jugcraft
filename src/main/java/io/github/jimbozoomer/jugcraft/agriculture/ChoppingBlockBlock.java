package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Chopping Block (Cozy Fall Hearth, part 3; tools/hearth.py CHOPPING_BLOCK): a sawn round of oak 44 cm across and
 * 45 cm high, its bark on and its rings on its top, with a hatchet sunk in it. Use a log on it to split it: the log is
 * spent and its planks given, exactly what crafting it gives ({@link #split}: the log's own crafting recipe, never more),
 * with the chop's sound and chips of that wood. The hatchet swings up and down through it each time, drawn by
 * client/ChoppingBlockRenderer from the blockstate's {@link #RUNNER} model (the hatchet alone, which a placed block never
 * takes; the block's own model is the round), timed from the moment a block event {@link DecorationBlockEntity#mark}s.
 */
public class ChoppingBlockBlock extends FacingDecorationBlock implements EntityBlock {
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** How long the hatchet's swing takes, in ticks, and how far up it goes, in degrees. */
	public static final int SWING_TICKS = 8;
	public static final float SWING_DEGREES = 55.0F;
	private static final int CHOP = 1;

	public ChoppingBlockBlock(Properties properties) {
		super(properties, new double[] {4.5, 0.0, 4.5, 11.5, 7.25, 11.5});
		registerDefaultState(defaultBlockState().setValue(RUNNER, false));
	}

	/** What splitting one of {@code log} gives: its crafting recipe's result (planks), or nothing if it isn't a log. */
	public static ItemStack split(ServerLevel level, ItemStack log) {
		if (!log.is(ItemTags.LOGS)) {
			return ItemStack.EMPTY;
		}
		CraftingInput input = CraftingInput.of(1, 1, List.of(log.copyWithCount(1)));
		return level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).map(recipe -> recipe.value().assemble(input))
				.orElse(ItemStack.EMPTY);
	}

	/** How far the hatchet is raised {@code since} ticks after a chop, in degrees: up and back down, then still. */
	public static float swing(float since) {
		if (since < 0.0F || since >= SWING_TICKS) {
			return 0.0F;
		}
		return SWING_DEGREES * (float) Math.sin(Math.PI * since / SWING_TICKS);
	}

	/** A log is split on it; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(ItemTags.LOGS)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level instanceof ServerLevel server) {
			ItemStack planks = split(server, stack);
			if (planks.isEmpty()) {
				return InteractionResult.PASS;
			}
			BlockState wood = stack.getItem() instanceof BlockItem item ? item.getBlock().defaultBlockState() : state;
			stack.consume(1, player);
			if (!player.getInventory().add(planks)) {
				Block.popResource(level, pos.above(), planks);
			}
			level.playSound(null, pos, SoundEvents.AXE_STRIP.value(), SoundSource.BLOCKS, 1.0F, 0.8F + level.getRandom().nextFloat() * 0.2F);
			server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, wood), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10,
					0.15, 0.05, 0.15, 0.05);
			level.blockEvent(pos, this, CHOP, 0);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** A chop: its block entity marks the time, for the hatchet's swing. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id == CHOP && level.getBlockEntity(pos) instanceof DecorationBlockEntity block) {
			block.mark(level.getGameTime());
			return true;
		}
		return false;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DecorationBlockEntity(JugcraftAgriculture.CHOPPING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}
