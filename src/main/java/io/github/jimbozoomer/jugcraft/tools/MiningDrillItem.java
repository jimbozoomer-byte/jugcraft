package io.github.jimbozoomer.jugcraft.tools;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The mining drill: a JE-powered pickaxe and shovel in one. Sneak and use it to switch between three
 * modes: one block, a 3×3 square parallel to the starting block face (5×5 with a range module), or a whole ore
 * vein (every touching block of the same ore, up to {@link #VEIN_LIMIT}).
 */
public class MiningDrillItem extends PoweredToolItem {
	public static final int SINGLE = 0;
	public static final int AREA = 1;
	public static final int VEIN = 2;
	public static final int AREA_LARGE = 3;
	public static final int VEIN_LIMIT = 32;
	private static final String[] MODE_NAMES = {"single", "area", "vein", "large"};
	private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

	public MiningDrillItem(Properties properties, long capacity, long energyPerBlock) {
		super(properties, capacity, energyPerBlock);
	}

	public static int mode(ItemStack stack) {
		int mode = Math.floorMod(stack.getOrDefault(JugcraftTools.DRILL_MODE, SINGLE), MODE_NAMES.length);
        return mode == AREA_LARGE && ToolUpgrades.level(stack, ToolUpgrades.Kind.RANGE) == 0 ? AREA : mode;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			int current = mode(stack);
            int mode = switch (current) {
                case SINGLE -> AREA;
                case AREA -> ToolUpgrades.level(stack, ToolUpgrades.Kind.RANGE) > 0 ? AREA_LARGE : VEIN;
                case AREA_LARGE -> VEIN;
                default -> SINGLE;
            };
			stack.set(JugcraftTools.DRILL_MODE, mode);
			player.sendOverlayMessage(Component.translatable("message.jugcraft.drill_mode",
					Component.translatable("message.jugcraft.drill_mode." + MODE_NAMES[mode])));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void afterMining(ItemStack stack, ServerLevel level, BlockState state, BlockPos pos, ServerPlayer player) {
        if (player.isShiftKeyDown()) return;
		switch (mode(stack)) {
			case AREA, AREA_LARGE -> {
                Direction face = ((DrillMiningContext) player.gameMode).jugcraft$drillFace(pos);
                if (face == null) return;
                for (BlockPos target : areaTargets(stack, pos, face)) {
                    if (!target.equals(pos) && level.hasChunkAt(target)
                        && level.getBlockState(target).getDestroySpeed(level, target) <= state.getDestroySpeed(level, pos))
                        breakExtra(stack, level, target, player);
                }
			}
			case VEIN -> {
				if (state.is(ORES)) {
					mineVein(stack, level, state.getBlock(), pos, player);
				}
			}
			default -> {
			}
		}
	}

	/** Blocks mined each way from the centre in area mode: 1 (3×3), or 2 (5×5) with a range module. */
	public static int areaReach(ItemStack stack) {
		return mode(stack) == AREA_LARGE ? 2 : 1;
	}

    /** One layer centred on the clicked block, parallel to its hit face, for both mining and preview. */
    public static List<BlockPos> areaTargets(ItemStack stack, BlockPos root, Direction face) {
        int reach = areaReach(stack);
        List<BlockPos> result = new ArrayList<>((2 * reach + 1) * (2 * reach + 1));
        for (int a = -reach; a <= reach; a++) for (int b = -reach; b <= reach; b++)
            result.add(offset(root, face.getAxis(), a, b));
        return result;
    }

	/** {@code pos} moved by (a, b) in the plane square to {@code axis}. */
	static BlockPos offset(BlockPos pos, Direction.Axis axis, int a, int b) {
		return switch (axis) {
			case X -> pos.offset(0, a, b);
			case Y -> pos.offset(a, 0, b);
			case Z -> pos.offset(a, b, 0);
		};
	}

    public static int veinLimit(ItemStack stack) { return VEIN_LIMIT * (1 + ToolUpgrades.level(stack, ToolUpgrades.Kind.RANGE)); }

	private void mineVein(ItemStack stack, ServerLevel level, Block ore, BlockPos start, ServerPlayer player) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		seen.add(start);
		queue.add(start);
		int mined = 1;
        int limit = veinLimit(stack);
		while (!queue.isEmpty() && mined < limit) {
			BlockPos pos = queue.poll();
			for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
				if (seen.size() > limit * 8 || mined >= limit) {
					return;
				}
				if (level.hasChunkAt(next) && seen.add(next.immutable()) && level.getBlockState(next).is(ore)) {
					BlockPos found = next.immutable();
					if (breakExtra(stack, level, found, player)) {
						mined++;
						queue.add(found);
					}
				}
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable("tooltip.jugcraft.drill_mode",
				Component.translatable("message.jugcraft.drill_mode." + MODE_NAMES[mode(stack)])).withStyle(ChatFormatting.GRAY));
	}
}
