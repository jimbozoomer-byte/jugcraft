package io.github.jimbozoomer.jugcraft.deposit;

import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A removable mast mounted at the centre of the existing rig's roof. No second machine or inventory. */
public final class DeepDrillingAssemblyBlock extends Block {
	public static final int DEPTH = 128, RADIUS = 4, BORE_TICKS = 20, USE = 32;
	public DeepDrillingAssemblyBlock(Properties properties) { super(properties); }

	public static BlockPos center(BlockPos master, BlockState state) {
		MachineBlock block = (MachineBlock) state.getBlock();
		var offsets = block.footprint(state);
		// The large rig's centre is part 4; support compact legacy rigs too.
		return offsets.size() > 1 ? offsets.partPos(master, state.getValue(MachineBlock.FACING), 4) : master;
	}

	public static BlockPos mount(BlockPos master, BlockState state) {
		return center(master, state).above(((MachineBlock) state.getBlock()).height(state));
	}

	public static boolean installed(Level level, BlockPos master, BlockState state) {
		BlockPos mount = mount(master, state);
		return level.isLoaded(mount) && level.getBlockState(mount).is(JugcraftDeposits.DEEP_DRILLING_ASSEMBLY);
	}

	public static InteractionResult install(ItemStack stack, Level level, BlockPos part, BlockState state, Player player) {
		BlockPos master = ((MachineBlock) state.getBlock()).masterPos(part, state);
		BlockPos mount = mount(master, state);
		if (!level.isLoaded(mount) || !level.getBlockState(mount).isAir() || !level.isInWorldBounds(mount)
				|| !level.getWorldBorder().isWithinBounds(mount) || !level.mayInteract(player, mount)
				|| !player.mayUseItemAt(mount, Direction.UP, stack) || TownProtection.denies(player, level, mount)) return InteractionResult.FAIL;
		if (!level.isClientSide()) {
			if (level instanceof net.minecraft.server.level.ServerLevel server
					&& !io.github.jimbozoomer.jugcraft.concordance.Authority.mayChange(server, player, mount)) return InteractionResult.FAIL;
			level.setBlock(mount, JugcraftDeposits.DEEP_DRILLING_ASSEMBLY.defaultBlockState(), 3);
			var machine = MachineBlock.machineAt(level, part, state);
			if (machine != null) machine.claimDepositDrill(player);
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}

	@Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		if (!(below.getBlock() instanceof MachineBlock machine) || machine.kind() != MachineKind.DEPOSIT_DRILL) return false;
		BlockPos master = machine.masterPos(pos.below(), below);
		return mount(master, below).equals(pos);
	}

	@Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighbor, BlockState neighborState, net.minecraft.util.RandomSource random) {
		return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
	}

	@Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Block.box(2, 0, 2, 14, 16, 14);
	}

	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		var machine = MachineBlock.machineAt(level, pos.below(), level.getBlockState(pos.below()));
		if (!level.isClientSide() && machine != null) player.sendOverlayMessage(Component.translatable(
				"message.jugcraft.deep_drilling", machine.depositDepth(), DEPTH, machine.depositBoreProgress() * 100 / BORE_TICKS,
				machine.processingProgress(), machine.processingDuration()));
		return InteractionResult.SUCCESS;
	}
}
