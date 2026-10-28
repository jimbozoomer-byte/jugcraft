package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The magic interfaces (docs/features/magic-interfaces.md): what a player carries against a pact's Open Hand Compact,
 * counted one way for both sides. The seal decides through {@link PactCompact#affords}, which asks {@link Count#affords()}
 * of this count and nothing else; the pact screen marks each short material and the resonance chip from the same count
 * before Accept is pressed, as display only (the server still decides when it is). Pure: it reads the player it is
 * given, changes nothing and names no client class.
 */
public final class PactAfford {
	/** One material of the compact: the item, how many it asks and how many the player carries. */
	public record Line(Item item, int need, int have) {
		public boolean met() {
			return have >= need;
		}
	}

	/**
	 * A whole count: each material, the resonance asked and the most any one carried astrolabe holds (the seal pays from
	 * one astrolabe that holds enough, PactCompact.astrolabe, so that is the "have" that matters), and whether the player
	 * has infinite materials (a creative player pays nothing, as vanilla's crafting).
	 */
	public record Count(List<Line> lines, int resonance, int charge, boolean infinite) {
		public Count {
			lines = List.copyOf(lines);
		}

		/** Whether the resonance is covered: none asked, or one carried astrolabe holds at least that much. */
		public boolean resonanceMet() {
			return resonance <= 0 || charge >= resonance;
		}

		/** Whether the compact can be paid now: the seal's own test (PactCompact.affords). */
		public boolean affords() {
			if (infinite) {
				return true;
			}
			for (Line line : lines) {
				if (!line.met()) {
					return false;
				}
			}
			return resonanceMet();
		}

		/** Whether {@code line} should be marked short on the screen (never for infinite materials). */
		public boolean isShort(Line line) {
			return !infinite && !line.met();
		}
	}

	private PactAfford() {
	}

	/** Counts {@code player}'s inventory against {@code needs} and {@code resonance}. */
	public static Count count(Player player, List<PactCompact.Need> needs, int resonance) {
		Inventory inventory = player.getInventory();
		List<Line> lines = new ArrayList<>(needs.size());
		for (PactCompact.Need need : needs) {
			lines.add(new Line(need.item(), need.count(), inventory.countItem(need.item())));
		}
		return new Count(lines, resonance, resonance <= 0 ? 0 : charge(player), player.hasInfiniteMaterials());
	}

	/** The compact's materials as the pact screen receives them (PactStatePayload.compact: one stack each, its count the need). */
	public static List<PactCompact.Need> needs(List<ItemStack> compact) {
		List<PactCompact.Need> needs = new ArrayList<>(compact.size());
		for (ItemStack stack : compact) {
			if (!stack.isEmpty()) {
				needs.add(new PactCompact.Need(stack.getItem(), stack.getCount()));
			}
		}
		return List.copyOf(needs);
	}

	/**
	 * The most resonance any one astrolabe {@code player} carries holds: main hand, off hand, then the inventory in slot
	 * order, as PactCompact.astrolabe looks (0 when none is carried).
	 */
	public static int charge(Player player) {
		int most = Math.max(charge(player.getMainHandItem()), charge(player.getOffhandItem()));
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			most = Math.max(most, charge(inventory.getItem(slot)));
		}
		return most;
	}

	private static int charge(ItemStack stack) {
		return stack.is(Sky.ASTROLABE) ? AstrolabeItem.charge(stack) : 0;
	}
}
