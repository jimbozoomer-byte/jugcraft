package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.PactAfford;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Item stacks in the kit's slots (docs/features/magic-interfaces.md, spec 2.6): a gold-framed slot on dark and on the
 * scroll (the item at +2,+2), or the documentation frame on the book's pages (+3,+3); vanilla's count where it sits;
 * the vanilla item tooltip on hover. A slot the player is short of has a red rim and "have/need" beside it in red ink,
 * shown before Accept from {@link PactAfford}'s count (display only: the server decides). An offering tag's slot cycles
 * its items once a second, with a "+N" badge saying how many more it holds, so a still frame says it too; under reduced
 * motion its first item holds still. Its words are its sentence's (a chip's or a row's), so it takes no focus.
 */
final class MagicStacks extends AbstractWidget implements MagicScreen.Drawn {
	/** One slot: the items it shows in turn (one, or a tag's), how many are needed, and how many are had (-1: not counted). */
	record Slot(List<ItemStack> items, int need, int have) {
		Slot {
			items = List.copyOf(items);
		}

		boolean isShort() {
			return have >= 0 && have < need;
		}
	}

	private static final int GAP = 4;
	private static final int SHORT_RIM = 0xFFC0392B;
	private final Font font;
	private final List<Slot> slots;
	private final boolean paper;
	private final int[] offsets;
	private MagicTheme.Inks inks = MagicTheme.DARK;

	MagicStacks(Font font, List<Slot> slots, boolean paper) {
		super(0, 0, 1, paper ? MagicArt.PAPER_SLOT : MagicArt.SLOT, CommonComponents.EMPTY);
		this.font = font;
		this.slots = List.copyOf(slots);
		this.paper = paper;
		this.offsets = new int[this.slots.size()];
		int x = 0;
		for (int i = 0; i < this.slots.size(); i++) {
			offsets[i] = x;
			x += size() + 1 + (this.slots.get(i).isShort() ? font.width(shortText(this.slots.get(i))) + GAP : 0) + GAP;
		}
		setWidth(Math.max(1, x - GAP));
		active = false;
	}

	/** The inks of the surface it sits on (the short mark's red): the scroll's when the price sits on parchment (variant B). */
	MagicStacks inks(MagicTheme.Inks inks) {
		this.inks = inks;
		return this;
	}

	/** The compact's slots from the payload's stacks, each marked against {@code count} (PactAfford; none marked for infinite materials). */
	static List<Slot> compact(List<ItemStack> stacks, PactAfford.Count count) {
		List<Slot> out = new ArrayList<>(stacks.size());
		for (ItemStack stack : stacks) {
			int have = -1;
			for (PactAfford.Line line : count.lines()) {
				if (line.item() == stack.getItem() && count.isShort(line)) {
					have = line.have();
				}
			}
			out.add(new Slot(List.of(stack.copyWithCount(1)), stack.getCount(), have));
		}
		return out;
	}

	/** The items of item tag {@code id} (synced to the client), one of each, in the tag's order; empty if it holds none. */
	static List<ItemStack> tag(String id) {
		Identifier key = Identifier.tryParse(id.startsWith("#") ? id.substring(1) : id);
		List<ItemStack> out = new ArrayList<>();
		if (key != null) {
			for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, key))) {
				out.add(new ItemStack(holder));
			}
		}
		return out;
	}

	private int size() {
		return paper ? MagicArt.PAPER_SLOT : MagicArt.SLOT;
	}

	private static String shortText(Slot slot) {
		return slot.have() + "/" + slot.need();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		int inset = paper ? 3 : 2;
		long second = ConcordanceClientOptions.reducedMotion() ? 0 : Util.getMillis() / 1000L;
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			if (slot.items().isEmpty()) {
				continue;
			}
			int sx = getX() + offsets[i];
			int sy = getY();
			MagicArt.slot(g, sx, sy, paper);
			ItemStack stack = slot.items().get((int) (second % slot.items().size()));
			g.item(stack, sx + inset, sy + inset);
			int more = slot.items().size() - 1;
			// Where a count would sit: the count needed, or how many more the tag holds (its hover lists them all).
			String count = slot.need() != 1 ? String.valueOf(slot.need()) : more > 0 ? "+" + more : null;
			if (count != null && paper) {
				// On the book's pages the count is ink (spec 2.6): vanilla's white is weak on cream.
				g.text(font, count, sx + inset + 17 - font.width(count), sy + inset + 9, MagicTheme.INK, false);
			} else if (count != null) {
				g.itemDecorations(font, stack, sx + inset, sy + inset, count);
			}
			if (slot.isShort()) {
				g.outline(sx - 1, sy - 1, size() + 2, size() + 2, SHORT_RIM);
				g.text(font, shortText(slot), sx + size() + 2, sy + (size() - 8) / 2, paper ? MagicTheme.RED : inks.refused(), !paper && inks.shadow());
			}
			if (mouseX >= sx && mouseX < sx + size() && mouseY >= sy && mouseY < sy + size()) {
				g.setTooltipForNextFrame(font, stack, mouseX, mouseY);
			}
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
	}

	/** Its counts and short marks: each a word of the screen's budget. */
	@Override
	public String drawnText() {
		StringBuilder out = new StringBuilder();
		for (Slot slot : slots) {
			String count = slot.need() != 1 ? String.valueOf(slot.need()) : slot.items().size() > 1 ? "+" + (slot.items().size() - 1) : "";
			String mark = slot.isShort() ? shortText(slot) : "";
			for (String word : new String[] {count, mark}) {
				if (!word.isEmpty()) {
					out.append(out.isEmpty() ? "" : " ").append(word);
				}
			}
		}
		return out.toString();
	}
}
