package io.github.jimbozoomer.jugcraft.mixin;

import com.klikli_dev.modonomicon.bookstate.BookVisualStates;
import com.klikli_dev.modonomicon.bookstate.visual.BookVisualState;
import com.klikli_dev.modonomicon.bookstate.visual.CategoryVisualState;
import com.klikli_dev.modonomicon.bookstate.visual.EntryVisualState;
import com.klikli_dev.modonomicon.networking.SyncBookVisualStatesMessage;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Detach every mutable visual-state layer on the sender thread before Netty encodes it. */
@Mixin(value = SyncBookVisualStatesMessage.class, remap = false)
public abstract class ModonomiconVisualSnapshotMixin {
    @Shadow public BookVisualStates states;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void jugcraft$snapshotVisualState(BookVisualStates source, CallbackInfo ci) {
        var books = new HashMap<Identifier, BookVisualState>();
        source.bookStates.forEach((bookId, book) -> {
            var categories = new HashMap<Identifier, CategoryVisualState>();
            book.categoryStates.forEach((categoryId, category) -> {
                var entries = new HashMap<Identifier, EntryVisualState>();
                category.entryStates.forEach((entryId, entry) -> entries.put(entryId,
                        new EntryVisualState(entry.openPagesIndex, entry.unread)));
                categories.put(categoryId, new CategoryVisualState(entries, category.scrollX,
                        category.scrollY, category.targetZoom, Optional.ofNullable(category.openEntry),
                        category.openPagesIndex, category.unread));
            });
            books.put(bookId, new BookVisualState(categories, book.openCategory, book.openPagesIndex));
        });
        // BookVisualStates copies bookmark lists; their BookAddress records are immutable.
        states = new BookVisualStates(books, source.bookBookmarks);
    }
}
