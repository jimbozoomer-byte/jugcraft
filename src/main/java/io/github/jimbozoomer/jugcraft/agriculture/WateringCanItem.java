package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Watering Can (Barn & Harvest Tools, tools/barn_tools.py WATERING_CAN): a tinplate can that holds {@link #CAPACITY}
 * pours of water ({@link JugcraftAgriculture#WATERING_CAN_WATER}), filled at any water source as the Gourd Canteen is.
 * A pour, used on farmland or on a crop standing on it, wets every farmland in the square {@link #RADIUS} round it and
 * gives each crop on it one chance in {@link #GROW_ODDS} of a random tick: the growth an ordinary moment brings, never
 * more than one a crop a pour. The can then waits {@link #COOLDOWN} ticks (the server checks the cooldown before any use),
 * so a pour moves at most nine crops a step, half a second apart. Every change is made on the server, and each farmland
 * only where the player may build.
 */
public class WateringCanItem extends Item {
	public static final int CAPACITY = 16;
	public static final int RADIUS = 1;
	public static final int GROW_ODDS = 6;
	public static final int COOLDOWN = 10;
	private static final int WATER_BLUE = 0x3F76E4;

	public WateringCanItem(Properties properties) {
		super(properties);
	}

	public static int water(ItemStack stack) {
		return Math.clamp(stack.getOrDefault(JugcraftAgriculture.WATERING_CAN_WATER, 0), 0, CAPACITY);
	}

	/** Fills the can from the water source the player looks at. */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (water(stack) >= CAPACITY || hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.mayInteract(player, pos)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			stack.set(JugcraftAgriculture.WATERING_CAN_WATER, CAPACITY);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.NEUTRAL, 0.8F, 1.1F);
			level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return InteractionResult.SUCCESS;
	}

	/** The farmland a pour on {@code pos} lands on: the block itself, or the farmland under the crop (one or two blocks tall). */
	static @Nullable BlockPos farmland(Level level, BlockPos pos) {
		for (int down = 0; down <= 2; down++) {
			BlockPos below = pos.below(down);
			BlockState state = level.getBlockState(below);
			if (state.is(Blocks.FARMLAND)) {
				return below;
			}
			if (!isCrop(state)) {
				return null;
			}
		}
		return null;
	}

	private static boolean isCrop(BlockState state) {
		return state.is(BlockTags.CROPS) || state.getBlock() instanceof CropBlock || state.getBlock() instanceof TallCropBlock;
	}

	/** Pours once over the farmland used on and the farmland round it. */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		Level level = context.getLevel();
		Player player = context.getPlayer();
		BlockPos center = water(stack) > 0 ? farmland(level, context.getClickedPos()) : null;
		if (center == null) {
			return InteractionResult.PASS;
		}
		if (player != null && (!player.mayUseItemAt(center, Direction.UP, stack) || !level.mayInteract(player, center))) {
			return InteractionResult.FAIL;
		}
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		for (BlockPos at : BlockPos.betweenClosed(center.offset(-RADIUS, 0, -RADIUS), center.offset(RADIUS, 0, RADIUS))) {
			BlockPos pos = at.immutable();
			BlockState state = server.getBlockState(pos);
			if (!state.is(Blocks.FARMLAND) || player != null && !server.mayInteract(player, pos)) {
				continue;
			}
			if (state.getValue(BlockStateProperties.MOISTURE) < 7) {
				server.setBlock(pos, state.setValue(BlockStateProperties.MOISTURE, 7), Block.UPDATE_CLIENTS);
			}
			BlockState crop = server.getBlockState(pos.above());
			if (isCrop(crop) && server.getRandom().nextInt(GROW_ODDS) == 0) {
				crop.randomTick(server, pos.above(), server.getRandom());
			}
			server.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 5, 0.3, 0.05, 0.3, 0.0);
		}
		stack.set(JugcraftAgriculture.WATERING_CAN_WATER, water(stack) - 1);
		server.playSound(null, center, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 0.7F);
		server.gameEvent(player, GameEvent.FLUID_PLACE, center);
		if (player != null) {
			player.getCooldowns().addCooldown(stack, COOLDOWN);
			player.awardStat(Stats.ITEM_USED.get(this));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * water(stack) / CAPACITY);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return WATER_BLUE;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		tooltip.accept(Component.translatable("item.jugcraft.watering_can.water", water(stack), CAPACITY).withStyle(ChatFormatting.BLUE));
	}
}
