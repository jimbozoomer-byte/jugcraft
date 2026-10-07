package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The Cooking Pot's screen: six ingredient slots (3x2) at the left, four result slots (2x2) at the right. */
public class CookingPotMenu extends AbstractContainerMenu {
	public static final int INPUT_X = 30;
	public static final int INPUT_Y = 26;
	public static final int RESULT_X = 116;
	public static final int RESULT_Y = 26;

	private final Container container;
	private final ContainerData data;
	private final java.util.List<CookingPotPlan> recipes;
	private final Object revision = CookingPotRecipe.revision();
	private long nextEdit;
	private boolean selectionInitialized;
	private net.minecraft.resources.Identifier displayedRecipe;
	private final net.minecraft.world.inventory.DataSlot selection = net.minecraft.world.inventory.DataSlot.standalone();

	/** Client-side constructor: contents arrive through slot and data syncing. */
	public CookingPotMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new CookingPotPlan.Opening(java.util.List.of()));
	}
	public CookingPotMenu(int containerId, Inventory inventory, CookingPotPlan.Opening opening) {
		this(containerId, inventory, new SimpleContainer(CookingPotBlockEntity.SLOTS),
				new SimpleContainerData(CookingPotBlockEntity.DATA_COUNT), opening.recipes());
	}

	public CookingPotMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		this(containerId, inventory, container, data, inventory.player.level() instanceof net.minecraft.server.level.ServerLevel server
			? CookingPotRecipe.plans(server.getServer()) : java.util.List.of());
	}
	private CookingPotMenu(int containerId, Inventory inventory, Container container, ContainerData data, java.util.List<CookingPotPlan> recipes) {
		super(JugcraftAgriculture.COOKING_POT_MENU, containerId);
		checkContainerSize(container, CookingPotBlockEntity.SLOTS);
		checkContainerDataCount(data, CookingPotBlockEntity.DATA_COUNT);
		this.container = container;
		this.data = data;
		this.recipes = java.util.List.copyOf(recipes);
		selection.set(-1);
		addDataSlot(selection);
		for (int slot = 0; slot < CookingPotBlockEntity.INPUTS; slot++) {
			addSlot(new Slot(container, slot, INPUT_X + (slot % 3) * 18, INPUT_Y + (slot / 3) * 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return container.canPlaceItem(getContainerSlot(), stack);
				}
			});
		}
		for (int index = 0; index < CookingPotBlockEntity.OUTPUTS; index++) {
			addSlot(new Slot(container, CookingPotBlockEntity.RESULT + index, RESULT_X + (index % 2) * 18, RESULT_Y + (index / 2) * 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return false;
				}
			});
		}
		addStandardInventorySlots(inventory, 8, 166);
		addDataSlots(data);
		updateSelection();
	}
	public java.util.List<CookingPotPlan> recipes() { return recipes; }
	public int selected() { return selection.get(); }
	public boolean assisted() { return data.get(CookingPotBlockEntity.DATA_ASSISTED) != 0; }
	public local.peepo.CompanionStatus status() { return local.peepo.CompanionStatus.from(data.get(CookingPotBlockEntity.DATA_STATUS)); }
	private void updateSelection() {
		if (!(container instanceof CookingPotBlockEntity pot)) return;
		var id = pot.selectedRecipe();
		if (selectionInitialized && java.util.Objects.equals(id, displayedRecipe)) return;
		selectionInitialized = true; displayedRecipe = id;
		int index = id == null ? -1 : -2;
		for (int i=0; i<recipes.size(); i++) if (recipes.get(i).id().equals(id)) { index=i; break; }
		selection.set(index);
	}
	@Override public void broadcastChanges() { updateSelection(); super.broadcastChanges(); }
	@Override public boolean clickMenuButton(Player player, int button) {
		if (!(container instanceof CookingPotBlockEntity pot) || !stillValid(player) || button < 0 || button > recipes.size()) return false;
		long now = player.level().getGameTime();
		if (now < nextEdit) return false;
		nextEdit = now + 2;
		pot.selectRecipe(button == 0 ? null : recipes.get(button-1).id());
		broadcastChanges(); return true;
	}

	/** Cooking progress from 0 to {@code width}. */
	public int progress(int width) {
		int time = data.get(CookingPotBlockEntity.DATA_TIME);
		return time <= 0 ? 0 : data.get(CookingPotBlockEntity.DATA_PROGRESS) * width / time;
	}

	/** Whether a heat source is under the pot. */
	public boolean heated() {
		return data.get(CookingPotBlockEntity.DATA_HEAT) != 0;
	}

	@Override
	public boolean stillValid(Player player) {
		return !player.isSpectator() && container.stillValid(player)
			&& (!(container instanceof CookingPotBlockEntity pot) || revision == CookingPotRecipe.revision()
			&& player.level() == pot.getLevel() && (!pot.isLocked() || pot.canOpen(player))
			&& !io.github.jimbozoomer.jugcraft.town.TownProtection.denies(player, player.level(), pot.getBlockPos()));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (!stillValid(player) || slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
		int potSlots = CookingPotBlockEntity.SLOTS;
		Slot slot = slots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		if (slotIndex < potSlots) {
			if (!moveItemStackTo(stack, potSlots, potSlots + 36, true)) {
				return ItemStack.EMPTY;
			}
		} else if (!container.canPlaceItem(0, stack) || !moveItemStackTo(stack, 0, CookingPotBlockEntity.INPUTS, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		if (stack.getCount() == original.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, stack);
		return original;
	}
}
