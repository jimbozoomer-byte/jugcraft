package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Seed Pouch (Barn & Harvest Tools, tools/barn_tools.py SEED_POUCH): a wool pouch that holds up to {@link #KINDS}
 * kinds of seed, a stack of each, in the vanilla container component. Seeds are anything tagged {@code c:seeds} that
 * plants a block. Used in the air it gathers the seeds the player carries into it; sneak-used in the air it empties
 * back into the inventory (or at the player's feet, if full). Used on farmland, or a crop on farmland, it sows every
 * empty farmland in the square {@link #RADIUS} round it, a seed a block, its first kind first, each placed as the seed
 * itself would be (so a seed that cannot grow there is not spent). All of it happens on the server, against the
 * server's inventory, and each block only where the player may build.
 */
public class SeedPouchItem extends Item {
	public static final int KINDS = 4;
	public static final int RADIUS = 1;
	public static final TagKey<Item> SEEDS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "seeds"));

	public SeedPouchItem(Properties properties) {
		super(properties);
	}

	/** What the pouch holds: copies, at most {@link #KINDS} stacks. */
	public static List<ItemStack> contents(ItemStack pouch) {
		return pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().limit(KINDS)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static void store(ItemStack pouch, List<ItemStack> seeds) {
		seeds.removeIf(ItemStack::isEmpty);
		pouch.set(DataComponents.CONTAINER, seeds.isEmpty() ? ItemContainerContents.EMPTY : ItemContainerContents.fromItems(seeds));
	}

	/** Whether a stack is a seed the pouch takes: tagged {@code c:seeds} and planting a block. */
	public static boolean sows(ItemStack stack) {
		return stack.is(SEEDS) && stack.getItem() instanceof BlockItem;
	}

	/** Gathers the player's seeds into the pouch, or (sneaking) empties it back. */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack pouch = player.getItemInHand(hand);
		List<ItemStack> held = contents(pouch);
		if (player.isShiftKeyDown()) {
			if (held.isEmpty()) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				for (ItemStack seed : held) {
					player.getInventory().placeItemBackInInventory(seed, Prediction.SERVER_ONLY);
				}
				store(pouch, new ArrayList<>());
				level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		if (level.isClientSide()) {
			return gather(player.getInventory(), new ArrayList<>(held), true) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		if (!gather(player.getInventory(), held, false)) {
			return InteractionResult.PASS;
		}
		store(pouch, held);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 1.0F);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}

	/**
	 * Moves the inventory's seeds into {@code held}: onto a stack of the same seed while it has room, or as a new kind
	 * while there are fewer than {@link #KINDS}. With {@code dryRun} nothing moves; either way, whether anything would.
	 */
	static boolean gather(Inventory inventory, List<ItemStack> held, boolean dryRun) {
		boolean moved = false;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!sows(stack)) {
				continue;
			}
			ItemStack into = null;
			for (ItemStack kind : held) {
				if (ItemStack.isSameItemSameComponents(kind, stack)) {
					into = kind;
					break;
				}
			}
			int count = Math.min(stack.getCount(), stack.getMaxStackSize() - (into == null ? 0 : into.getCount()));
			if (count <= 0 || into == null && held.size() >= KINDS) {
				continue;
			}
			moved = true;
			if (dryRun) {
				return true;
			}
			if (into == null) {
				held.add(stack.copyWithCount(count));
			} else {
				into.grow(count);
			}
			stack.shrink(count);
		}
		return moved;
	}

	/** Sows the empty farmland round the farmland used on. */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack pouch = context.getItemInHand();
		Level level = context.getLevel();
		Player player = context.getPlayer();
		List<ItemStack> held = contents(pouch);
		BlockPos center = held.isEmpty() || player == null ? null : WateringCanItem.farmland(level, context.getClickedPos());
		if (center == null) {
			return InteractionResult.PASS;
		}
		if (!player.mayUseItemAt(center, Direction.UP, pouch) || !level.mayInteract(player, center)) {
			return InteractionResult.FAIL;
		}
		if (!(level instanceof ServerLevel server)) {
			return InteractionResult.SUCCESS;
		}
		int sown = 0;
		for (BlockPos at : BlockPos.betweenClosed(center.offset(-RADIUS, 0, -RADIUS), center.offset(RADIUS, 0, RADIUS))) {
			BlockPos pos = at.immutable();
			if (!server.getBlockState(pos).is(Blocks.FARMLAND) || !server.isEmptyBlock(pos.above()) || !server.mayInteract(player, pos.above())) {
				continue;
			}
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), Direction.UP, pos, false);
			for (ItemStack seed : held) {
				if (seed.isEmpty() || !(seed.getItem() instanceof BlockItem item)) {
					continue;
				}
				if (item.place(new BlockPlaceContext(player, context.getHand(), seed.copyWithCount(1), hit)).consumesAction()) {
					if (!player.getAbilities().instabuild) {
						seed.shrink(1);
					}
					sown++;
					break;
				}
			}
		}
		if (sown == 0) {
			return InteractionResult.PASS;
		}
		store(pouch, held);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS_SERVER;
	}

	/** How full the pouch is: kinds and seeds. */
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		List<ItemStack> held = contents(stack);
		int seeds = held.stream().mapToInt(ItemStack::getCount).sum();
		tooltip.accept(Component.translatable("item.jugcraft.seed_pouch.contents", held.size(), KINDS, seeds).withStyle(ChatFormatting.GRAY));
	}
}
