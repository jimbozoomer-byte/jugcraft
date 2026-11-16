package io.github.jimbozoomer.jugcraft.horses;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A horse's saddlebags (docs/features/horse-breeds.md#saddlebags-and-cargo-conservation): fitted by its owner with a
 * chest, as a donkey's chest is; opened by its owner by sneak-using it with a stick, as the menagerie's mounts' are
 * (plain sneak-use stays vanilla's horse screen). The Shire shows three rows, the Giant Finnhorse and the Pegasus one.
 *
 * <p>Cargo conservation. The bags always store {@value HorseCargo#SLOTS} slots and save them with the horse (in its own
 * {@value #ITEMS_TAG} compound), so a recall, a knock-out and a save and reload keep every item exactly once: the Pets
 * roster saves the whole entity, and a knock-out cancels the death, so nothing drops then. Only a real death drops the
 * chest and every slot, each stack removed as it drops. The menu stays usable only while the horse is alive, present,
 * near and still its owner's (vanilla ignores clicks once a menu is no longer valid), so a recall or unload with the
 * menu open cannot move anything. A smaller capacity than what is stored never deletes items: see
 * {@link HorseCargo#plan}. Only the server changes anything.
 */
public final class Saddlebags {
	public static final String FITTED_TAG = "JugcraftBagsFitted";
	public static final String ITEMS_TAG = "JugcraftSaddlebags";
	/** How close (squared blocks) the owner must stay for the open bags to keep working. */
	private static final double REACH_SQR = 64.0;

	private final AbstractHorse horse;
	private final Consumer<Boolean> sync;
	private boolean fitted;
	private final SimpleContainer items = new SimpleContainer(HorseCargo.SLOTS) {
		@Override
		public boolean stillValid(Player player) {
			return mayUse(player);
		}
	};

	/** {@code sync} copies the fitted flag into the horse's synced data, so every client draws the bags. */
	public Saddlebags(AbstractHorse horse, Consumer<Boolean> sync) {
		this.horse = horse;
		this.sync = sync;
	}

	public boolean fitted() {
		return fitted;
	}

	/** The stored slots (all of them, including any past the current capacity). */
	public SimpleContainer items() {
		return items;
	}

	/** Server, and tests: fit or take off the bags (the chest itself is the caller's business). */
	public void setFitted(boolean value) {
		fitted = value;
		sync.accept(value);
	}

	/** Whether {@code player} owns the horse (its tamer). */
	public static boolean ownedBy(AbstractHorse horse, Player player) {
		return horse.isTamed() && horse.getOwnerReference() != null && horse.getOwnerReference().getUUID().equals(player.getUUID());
	}

	/** Whether the open menu may still be used: the horse alive and present, the bags on, the owner near. */
	public boolean mayUse(Player player) {
		return fitted && !horse.isRemoved() && horse.isAlive() && ownedBy(horse, player) && player.distanceToSqr(horse) < REACH_SQR;
	}

	private static boolean saddled(AbstractHorse horse) {
		return !horse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
	}

	/**
	 * The bags' part of using the horse, or null to carry on with the horse's own interaction. {@code rows} is the
	 * capacity in rows of nine (0 for a horse that cannot carry them); {@code onSaddle}: the bags hang on the saddle (the
	 * Pegasus), so fitting and opening them need the saddle on.
	 */
	public InteractionResult interact(Player player, InteractionHand hand, int rows, boolean onSaddle, Component title) {
		ItemStack held = player.getItemInHand(hand);
		if (held.is(Items.CHEST) && !fitted && rows > 0 && horse.isTamed() && !horse.isBaby()) {
			if (player instanceof ServerPlayer server && allowed(server)) {
				if (onSaddle && !saddled(horse)) {
					server.sendOverlayMessage(Component.translatable("message.jugcraft.horse_breeds.pack_needs_saddle"));
					return InteractionResult.FAIL;
				}
				held.consume(1, server);
				setFitted(true);
				horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.CHEST_CLOSE, SoundSource.NEUTRAL, 0.8F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		if (fitted && held.is(Items.STICK) && player.isSecondaryUseActive()) {
			if (player instanceof ServerPlayer server && allowed(server)) {
				open(server, rows, onSaddle, title);
			}
			return InteractionResult.SUCCESS;
		}
		return null;
	}

	/** The owner, where they may change things (land claims and towns, as everywhere else). */
	private boolean allowed(ServerPlayer player) {
		if (!ownedBy(horse, player)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.horse_breeds.not_yours"));
			return false;
		}
		return Authority.mayChange(player.level(), player, horse.blockPosition());
	}

	/** Server: settle anything stored past the capacity, then open the menu (or return everything if it holds none). */
	void open(ServerPlayer player, int rows, boolean onSaddle, Component title) {
		if (onSaddle && !saddled(horse)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.horse_breeds.pack_needs_saddle"));
			return;
		}
		int capacity = HorseCargo.capacity(rows);
		int returned = settle(player, capacity);
		if (returned > 0) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.horse_breeds.overflow", returned));
		}
		if (capacity <= 0) {
			// It can no longer carry bags at all: everything came back above; the chest comes back too.
			setFitted(false);
			give(player, new ItemStack(Items.CHEST));
			return;
		}
		player.openMenu(new SimpleMenuProvider((id, inventory, who) -> capacity >= 27 ? ChestMenu.threeRows(id, inventory, items)
				: new ChestMenu(MenuType.GENERIC_9x1, id, inventory, items, 1), title));
		horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.CHEST_OPEN, SoundSource.NEUTRAL, 0.6F, 1.2F);
	}

	/**
	 * Applies {@link HorseCargo#plan}: stacks stored past {@code capacity} move into empty visible slots; what still does
	 * not fit goes to the owner. Returns how many stacks went back to the owner.
	 */
	public int settle(ServerPlayer player, int capacity) {
		boolean[] occupied = new boolean[HorseCargo.SLOTS];
		for (int i = 0; i < occupied.length; i++) {
			occupied[i] = !items.getItem(i).isEmpty();
		}
		HorseCargo.Plan plan = HorseCargo.plan(occupied, capacity);
		for (int[] move : plan.moves()) {
			items.setItem(move[1], items.removeItemNoUpdate(move[0]));
		}
		for (int slot : plan.handBack()) {
			give(player, items.removeItemNoUpdate(slot));
		}
		if (!plan.empty()) {
			items.setChanged();
		}
		return plan.handBack().size();
	}

	/**
	 * Into the player's inventory; what does not fit drops beside the horse. Never lost, never doubled: a copy goes to
	 * {@code Inventory.add}, which leaves in it what it could not take, and that remainder drops. A player with infinite
	 * materials (creative) gets nothing through {@code add}, which would empty the stack into a full inventory: the whole
	 * stack drops instead.
	 */
	private void give(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty() || !(horse.level() instanceof ServerLevel level)) {
			return;
		}
		ItemStack rest = stack.copy();
		if (!player.hasInfiniteMaterials()) {
			player.getInventory().add(rest);
		}
		if (!rest.isEmpty()) {
			horse.spawnAtLocation(level, rest);
		}
	}

	/** On the horse's real death (not a knock-out): the chest, if fitted, and every stored stack, once. */
	public void dropAll(ServerLevel level) {
		for (int i = 0; i < items.getContainerSize(); i++) {
			ItemStack stack = items.removeItemNoUpdate(i);
			if (!stack.isEmpty()) {
				horse.spawnAtLocation(level, stack);
			}
		}
		if (fitted) {
			setFitted(false);
			horse.spawnAtLocation(level, new ItemStack(Items.CHEST));
		}
	}

	/** How many stacks are stored, in any slot (tests and the lineage card). */
	public int stacks() {
		int count = 0;
		for (int i = 0; i < items.getContainerSize(); i++) {
			if (!items.getItem(i).isEmpty()) {
				count++;
			}
		}
		return count;
	}

	public void save(ValueOutput out) {
		out.putBoolean(FITTED_TAG, fitted);
		NonNullList<ItemStack> list = NonNullList.withSize(HorseCargo.SLOTS, ItemStack.EMPTY);
		for (int i = 0; i < HorseCargo.SLOTS; i++) {
			list.set(i, items.getItem(i));
		}
		ContainerHelper.saveAllItems(out.child(ITEMS_TAG), list);
	}

	/** Reads what {@link #save} wrote; a save from before the bags existed has none (and loses nothing). */
	public void load(ValueInput in) {
		setFitted(in.getBooleanOr(FITTED_TAG, false));
		NonNullList<ItemStack> list = NonNullList.withSize(HorseCargo.SLOTS, ItemStack.EMPTY);
		in.child(ITEMS_TAG).ifPresent(saved -> ContainerHelper.loadAllItems(saved, list));
		for (int i = 0; i < HorseCargo.SLOTS; i++) {
			items.setItem(i, list.get(i));
		}
	}
}
