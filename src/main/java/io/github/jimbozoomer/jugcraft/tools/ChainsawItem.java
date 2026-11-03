package io.github.jimbozoomer.jugcraft.tools;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

/**
 * A JE-powered axe with conservative rooted-tree recognition. Ambiguous timber is cut one block at a time.
 */
public class ChainsawItem extends PoweredToolItem {
	public static final int TREE_LIMIT = 128;

	public ChainsawItem(Properties properties, long capacity, long energyPerBlock) {
		super(properties, capacity, energyPerBlock);
	}

    @Override
    public net.minecraft.world.InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            int mode = (Math.floorMod(EquipmentSettings.number(stack, "saw_mode", 0),3) + 1) % 3;
            EquipmentSettings.setNumber(stack, "saw_mode", mode);
            player.sendOverlayMessage(Component.literal("Chainsaw: " + new String[]{"tree felling", "single log", "leaf clearing"}[mode]));
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override public long energyPerBlock(ItemStack stack) {
        return EquipmentSettings.number(stack,"saw_mode",0)==2 ? efficientCost(stack, 5L * (2 + ToolUpgrades.level(stack,ToolUpgrades.Kind.OVERCLOCK))) : super.energyPerBlock(stack);
    }
    @Override public float getDestroySpeed(ItemStack stack,BlockState state) {
        return EquipmentSettings.number(stack,"saw_mode",0)==2 && !state.is(BlockTags.LEAVES) ? 1 : super.getDestroySpeed(stack,state);
    }
    @Override public boolean isCorrectToolForDrops(ItemStack stack,BlockState state) {
        return (EquipmentSettings.number(stack,"saw_mode",0)!=2 || state.is(BlockTags.LEAVES)) && super.isCorrectToolForDrops(stack,state);
    }

	@Override
	protected void afterMining(ItemStack stack, ServerLevel level, BlockState state, BlockPos pos, ServerPlayer player) {
        if (EquipmentSettings.number(stack,"saw_mode",0)==2) {
            if(!state.is(BlockTags.LEAVES)||player.isShiftKeyDown())return;
            var seenLeaves=new HashSet<BlockPos>();var leaves=new ArrayDeque<BlockPos>();seenLeaves.add(pos);leaves.add(pos);int cutLeaves=1;
            while(!leaves.isEmpty()&&cutLeaves<64){
                var leaf=leaves.poll();
                for(var direction:net.minecraft.core.Direction.values()){
                    var next=leaf.relative(direction);
                    if(seenLeaves.size()>384||cutLeaves>=64)break;
                    if(level.hasChunkAt(next)&&seenLeaves.add(next)&&level.getBlockState(next).is(state.getBlock())&&breakExtra(stack,level,next,player)){cutLeaves++;leaves.add(next);}
                }
            }return;
        }
		if (!state.is(BlockTags.LOGS) || player.isShiftKeyDown() || EquipmentSettings.number(stack, "saw_mode", 0) == 1) {
			return;
		}
        for (BlockPos target:ChainsawTree.targets(level,pos,state))
            if(!target.equals(pos)&&!breakExtra(stack,level,target,player))break;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		tooltip.accept(Component.translatable("tooltip.jugcraft.chainsaw").withStyle(ChatFormatting.GRAY));
	}
}
