package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Native container buttons; the server validates ownership, reach, sight, dose, canvas and reward atomically. */
public final class DreamStudyMenu extends AbstractContainerMenu {
	private final Player owner;
	private final @Nullable BlockPos pos;
	private final ContainerData data = new SimpleContainerData(8);
	private int selected, progress;
	private boolean completed;
	public DreamStudyMenu(int id, Inventory inventory) { this(id, inventory, null); }
	public DreamStudyMenu(int id, Inventory inventory, @Nullable BlockPos pos) {
		super(DreamStudies.MENU, id);
		this.owner = inventory.player; this.pos = pos == null ? null : pos.immutable();
		addDataSlots(data); refresh();
	}
	public int lesson() { return data.get(0); }
	public int progress() { return data.get(1); }
	public int status() { return data.get(2); } // 0 dose missing, 1 ready, 2 finished, 3 canvas/space needed
	public int clue(int index) { return index >= 0 && index < 3 ? data.get(3 + index) : -1; }
	public int sight() { return data.get(6); }
	public boolean ready() { return status() == 1 || status() == 3; }
	/** Readable hint derived only from synchronized authorized clues; never an authority for awarding a reward. */
	public int trace(int index) {
		if(!ready() || index<0 || index>=3)return -1;
		int source=clue(lesson()==1?2-index:index);
		return source<0?-1:lesson()==2?(source+1)%4:source;
	}
	private int[] pattern() {
		int seed = pos == null ? 0 : Math.floorMod(pos.getX() * 31 + pos.getZ() * 17 + pos.getY(), 4);
		return new int[] {seed, (seed + 1 + selected) % 4, (seed + 3) % 4};
	}
	private int answer(int index) {
		int[] sequence = pattern();
		return selected == 1 ? sequence[2 - index] : selected == 2 ? (sequence[index] + 1) % 4 : sequence[index];
	}
	private boolean authorized(ServerPlayer player) { return PsychedelicPharmacy.sight(player) > selected && stillValid(player); }
	private void refresh() {
		if (!(owner instanceof ServerPlayer player)) return;
		boolean allowed = authorized(player);
		data.set(0, selected); data.set(1, allowed ? progress : 0);
		data.set(2, completed ? 2 : allowed ? data.get(2) == 3 ? 3 : 1 : 0);
		int[] pattern = pattern();
		for (int i = 0; i < 3; i++) data.set(3 + i, allowed && !completed ? pattern[i] : -1);
		data.set(6, stillValid(player) ? PsychedelicPharmacy.sight(player) : 0);
	}
	@Override public void broadcastChanges() { refresh(); super.broadcastChanges(); }
	@Override public boolean stillValid(Player player) {
		return player == owner && (!(player instanceof ServerPlayer server) || pos != null && DreamStudies.available(server)
				&& !DreamStudies.folio(server).isEmpty() && DreamStudies.visible(server, pos, 6));
	}
	@Override public boolean clickMenuButton(Player player, int button) {
		if (player != owner || !(player instanceof ServerPlayer server) || player.containerMenu != this || !stillValid(player)
				|| completed || button < 0 || button > 6 || !RateGate.allow(server, "dream_study_button", 3)) return false;
		if (button >= 4) {
			selected = button - 4; progress = 0; data.set(2, 0); refresh(); broadcastChanges(); return true;
		}
		if (!authorized(server)) { progress = 0; refresh(); return false; }
		if (button != answer(progress)) { progress = 0; data.set(2, 1); broadcastChanges(); return true; }
		if (progress < 2) { progress++; broadcastChanges(); return true; }
		var inventory = server.getInventory();
		int canvasSlot = -1;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++)
			if (inventory.getItem(slot).is(PsychedelicPharmacy.item("study_canvas"))) { canvasSlot = slot; break; }
		if (canvasSlot < 0) { data.set(2, 3); broadcastChanges(); return false; }
		var payment = inventory.getItem(canvasSlot);
		payment.shrink(1);
		var reward = new ItemStack(PsychedelicPharmacy.item(switch (selected) { case 0 -> "lucid_imprint"; case 1 -> "pattern_imprint"; default -> "oneiric_imprint"; }));
		if (!inventory.add(reward)) { payment.grow(1); inventory.setItem(canvasSlot, payment); data.set(2, 3); broadcastChanges(); return false; }
		completed = true; progress = 3; inventory.setChanged(); inventoryMenuSync(server); broadcastChanges(); return true;
	}
	private static void inventoryMenuSync(ServerPlayer player) { player.inventoryMenu.broadcastChanges(); }
	@Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
