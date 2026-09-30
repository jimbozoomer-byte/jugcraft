package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Optional;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The Cooking Pot's contents and cooking. Every tick (server only) it checks the one block under it
 * for heat; with heat and a {@link CookingPotRecipe} in its ingredient slots it cooks, and after the
 * recipe's time it takes one batch of ingredients and adds the meal to the result slot. Without
 * heat, progress falls back {@value #COOLING} per tick, like a furnace going out. The recipe is looked
 * up again only when the slots change.
 * <p>
 * Hoppers and pipes put ingredients in from the top and sides and take meals out from the bottom.
 */
public class CookingPotBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, ExtendedMenuProvider<BlockPos> {
	/** What heats the pot from directly below. Keep in sync with HEAT_TAG in tools/agriculture.py. */
	public static final TagKey<Block> HEAT_SOURCES = TagKey.create(Registries.BLOCK, Jugcraft.id("heat_sources"));
	/** Ingredient slots, then the result slot. Keep in sync with POT_INPUTS in tools/agriculture.py. */
	public static final int INPUTS = 6;
	public static final int RESULT = INPUTS;
	public static final int SLOTS = INPUTS + 1;
	/** Progress lost per tick without heat. Keep in sync with POT_COOLING in tools/agriculture.py. */
	public static final int COOLING = 2;

	public static final int DATA_PROGRESS = 0;
	public static final int DATA_TIME = 1;
	public static final int DATA_HEAT = 2;
	public static final int DATA_COUNT = 3;

	private static final int[] INPUT_SLOTS = {0, 1, 2, 3, 4, 5};
	private static final int[] RESULT_SLOTS = {RESULT};

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
	private int progress;
	private int time;
	private boolean heated;
	/** The slots changed since the recipe was last looked up. */
	private boolean recheck = true;
	/** Just loaded from disk: the first lookup keeps the saved progress. */
	private boolean loaded;
	private @Nullable CookingPotRecipe recipe;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_PROGRESS -> progress;
				case DATA_TIME -> time;
				case DATA_HEAT -> heated ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public CookingPotBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.COOKING_POT_ENTITY, pos, state);
	}

	/** Whether the block under {@code pos} heats a pot: in {@link #HEAT_SOURCES}, and lit if it can be unlit (campfires). */
	public static boolean isHeated(Level level, BlockPos pos) {
		BlockState below = level.getBlockState(pos.below());
		return below.is(HEAT_SOURCES) && (!below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT));
	}

	public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		heated = isHeated(level, pos);
		if (recheck) {
			recheck = false;
			CookingPotRecipe found = CookingPotRecipe.find(level.getServer(), items.subList(0, INPUTS))
					.map(CookingPotRecipe.Match::recipe).orElse(null);
			if (found != recipe) {
				recipe = found;
				progress = loaded && found != null ? progress : 0;
			}
			loaded = false;
			time = recipe == null ? 0 : recipe.time();
		}
		boolean cooking = heated && recipe != null && hasRoomFor(recipe.output());
		if (cooking) {
			progress++;
			if (progress >= time) {
				progress = 0;
				cook(level, pos, recipe);
			}
			setChanged();
		} else if (progress > 0) {
			progress = Math.max(0, progress - COOLING);
			setChanged();
		}
		if (state.getValue(CookingPotBlock.COOKING) != cooking) {
			level.setBlock(pos, state.setValue(CookingPotBlock.COOKING, cooking), Block.UPDATE_ALL);
		}
	}

	private boolean hasRoomFor(ItemStackTemplate output) {
		ItemStack result = items.get(RESULT);
		if (result.isEmpty()) {
			return true;
		}
		ItemStack meal = output.create();
		return ItemStack.isSameItemSameComponents(result, meal) && result.getCount() + meal.getCount() <= result.getMaxStackSize();
	}

	/** Takes one batch of ingredients (leaving containers such as buckets behind) and adds the meal. */
	private void cook(ServerLevel level, BlockPos pos, CookingPotRecipe cooked) {
		Optional<CookingPotRecipe.Match> match = CookingPotRecipe.find(level.getServer(), items.subList(0, INPUTS));
		if (match.isEmpty() || match.get().recipe() != cooked) {
			recheck = true;
			return;
		}
		int[] take = match.get().take();
		for (int slot = 0; slot < INPUTS; slot++) {
			if (take[slot] == 0) {
				continue;
			}
			ItemStack stack = items.get(slot);
			ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
			stack.shrink(take[slot]);
			for (int n = 0; remainder != null && n < take[slot]; n++) {
				if (items.get(slot).isEmpty()) {
					items.set(slot, remainder.create());
				} else {
					Block.popResource(level, pos.above(), remainder.create());
				}
			}
		}
		ItemStack meal = cooked.output().create();
		if (items.get(RESULT).isEmpty()) {
			items.set(RESULT, meal);
		} else {
			items.get(RESULT).grow(meal.getCount());
		}
		level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.8F, 0.9F + level.getRandom().nextFloat() * 0.2F);
		recheck = true;
	}

	/** 0 when the result slot is empty, up to 15 when it is full. */
	public int comparatorSignal() {
		ItemStack result = items.get(RESULT);
		return result.isEmpty() ? 0 : 1 + result.getCount() * 14 / result.getMaxStackSize();
	}

	// ---------------------------------------------------------------- inventory

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.jugcraft.cooking_pot");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
		recheck = true;
	}

	@Override
	public int getContainerSize() {
		return SLOTS;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		super.setItem(slot, stack);
		recheck = true;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		recheck = true;
		return super.removeItem(slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		recheck = true;
		return super.removeItemNoUpdate(slot);
	}

	/** Ingredient slots take only items some pot recipe uses; only the server knows the recipes. */
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot < INPUTS && (!(level instanceof ServerLevel server) || CookingPotRecipe.isIngredient(server.getServer(), stack));
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? RESULT_SLOTS : INPUT_SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
		return side != Direction.DOWN && canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side == Direction.DOWN && slot == RESULT;
	}

	// ---------------------------------------------------------------- menu

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new CookingPotMenu(containerId, inventory, this, data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return worldPosition;
	}

	// ---------------------------------------------------------------- saving

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		progress = input.getInt("progress").orElse(0);
		recheck = true;
		loaded = true;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("progress", progress);
	}
}
