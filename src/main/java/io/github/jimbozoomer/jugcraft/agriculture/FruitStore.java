package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * A block that holds picked fruit (Orchard & Apple Harvest, decor set 2; tools/orchard_harvest.py): the Apple Bin and
 * the Picking Bag. It takes the Cider Press's fruit ({@link #FRUIT}: apples and pears), up to its {@link #capacity}, and
 * shows how full it is in {@link #stages} and which fruit went in last ({@link #KIND}). Its contents are kept by a
 * {@link FruitStoreBlockEntity}; the server alone changes them.
 */
public interface FruitStore {
	TagKey<Item> FRUIT = CiderPressBlock.APPLES;
	EnumProperty<Kind> KIND = EnumProperty.create("fruit", Kind.class);

	/** The fruit shown on top of the heap. */
	enum Kind implements StringRepresentable {
		APPLE, PEAR;

		public static Kind of(ItemStack stack) {
			return stack.is(JugcraftAgriculture.item("pear")) ? PEAR : APPLE;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/** How many fruit it holds when full. */
	int capacity();

	/** What it takes: the Cider Press's fruit, unless it keeps something else (the Corn Crib its corn). */
	default TagKey<Item> takes() {
		return FRUIT;
	}

	/** What {@code store}'s block takes. */
	static TagKey<Item> takes(FruitStoreBlockEntity store) {
		return store.getBlockState().getBlock() instanceof FruitStore kind ? kind.takes() : FRUIT;
	}

	/** How many steps its heap rises through, empty not counted. */
	int stages();

	/** Shows {@code fill} (0 to {@link #stages}) and {@code fruit} on every block it has. */
	void show(Level level, BlockPos pos, BlockState state, int fill, Kind fruit);

	/** The stage {@code count} fruit fill: 0 when empty, else 1 to {@code stages}, each a like share of the capacity. */
	static int fill(int count, int capacity, int stages) {
		return count <= 0 ? 0 : Math.max(1, Math.min(stages, (count * stages + capacity - 1) / capacity));
	}

	/** Puts all of {@code stack} that fits, if it is what {@code store} takes, into it (on the server; the client only predicts). */
	static InteractionResult putIn(FruitStoreBlockEntity store, ItemStack stack, Level level, BlockPos pos, Player player) {
		if (!stack.is(takes(store))) {
			// Only an empty hand goes on to take a stack back; anything else does what it would on any block.
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (store.count() >= store.capacity()) {
			// (not PASS or FAIL: either would go on to use the fruit itself)
			player.sendOverlayMessage(Component.translatable("message.jugcraft.fruit_store.full"));
			return InteractionResult.CONSUME;
		}
		if (store.add(stack) > 0) {
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.7F, 1.3F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * Gives back a stack (or what is left) of the fruit that went in last. The client, which doesn't see the fruit, goes
	 * by whether the block shows any ({@code showsFruit}).
	 */
	static InteractionResult takeOut(FruitStoreBlockEntity store, Level level, BlockPos pos, Player player, boolean showsFruit) {
		if (level.isClientSide()) {
			return showsFruit ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		ItemStack taken = store.takeLast();
		if (taken.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!player.getInventory().add(taken)) {
			Block.popResource(level, pos.above(), taken);
		}
		level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.7F, 1.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}
}
