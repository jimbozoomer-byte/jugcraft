package io.github.jimbozoomer.jugcraft.concordance;

import com.mojang.serialization.MapCodec;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A fair copy of Research Notes in Gloam Ink (recipe type {@code jugcraft:research_notes_copy}; Gloam Ink, C-D6): one
 * original written sheet, one blank sheet and one Gloam Ink in a crafting grid, and nothing else, make one fair copy. The
 * copy holds the original's author, entries and time written and is marked a copy ({@link ResearchNotes#fairCopy}); the
 * original stays in the grid, as a written book does when it is copied, and the ink's bottle comes back. A copy teaches
 * exactly what its original does: notes are evidence keyed by their author, so a reader who has read the original or
 * another copy gains nothing more, and nothing here records evidence, a research state or a Conclave teaching. A fair
 * copy is never copied again. The game crafts on the server; nothing a client says is trusted.
 */
public class ResearchNotesCopyRecipe extends CustomRecipe {
	public static final ResearchNotesCopyRecipe INSTANCE = new ResearchNotesCopyRecipe();
	public static final MapCodec<ResearchNotesCopyRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchNotesCopyRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	public static final RecipeSerializer<ResearchNotesCopyRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

	/**
	 * The notes of the grid's one original written sheet, if the grid holds exactly that, one blank sheet and one Gloam
	 * Ink, and nothing else, while the Concordance is on; otherwise null. A fair copy is never an original.
	 */
	static @Nullable ResearchNotes original(CraftingInput input) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE)) {
			return null;
		}
		ResearchNotes original = null;
		int blanks = 0;
		int inks = 0;
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (stack.is(JugcraftConcordance.GLOAM_INK)) {
				inks++;
			} else if (stack.is(JugcraftConcordance.RESEARCH_NOTES_ITEM)) {
				ResearchNotes notes = stack.get(JugcraftConcordance.RESEARCH_NOTES);
				if (notes == null) {
					blanks++;
				} else if (original != null || notes.fairCopy()) {
					return null;
				} else {
					original = notes;
				}
			} else {
				return null;
			}
		}
		return blanks == 1 && inks == 1 ? original : null;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return original(input) != null;
	}

	/** One new sheet holding only the original's notes, marked a copy: no name or other component of the original's. */
	@Override
	public ItemStack assemble(CraftingInput input) {
		ResearchNotes notes = original(input);
		if (notes == null) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM);
		copy.set(JugcraftConcordance.RESEARCH_NOTES, notes.asFairCopy());
		return copy;
	}

	/** The original stays in the grid; the ink's bottle comes back; the blank is used. */
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> left = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int slot = 0; slot < input.size(); slot++) {
			ItemStack stack = input.getItem(slot);
			if (stack.has(JugcraftConcordance.RESEARCH_NOTES)) {
				left.set(slot, stack.copyWithCount(1));
			} else {
				ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
				if (remainder != null) {
					left.set(slot, remainder.create());
				}
			}
		}
		return left;
	}

	@Override
	public RecipeSerializer<ResearchNotesCopyRecipe> getSerializer() {
		return SERIALIZER;
	}
}
