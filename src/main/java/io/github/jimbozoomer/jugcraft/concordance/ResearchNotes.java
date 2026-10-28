package io.github.jimbozoomer.jugcraft.concordance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * What a sheet of Research Notes holds ({@code jugcraft:research_notes}): who wrote it and, for each research entry they
 * had begun, how far they had come, never beyond understood. Notes are a shared record: another player who reads them
 * gains evidence where the research allows notes ({@link ResearchNotesItem}), but never the author's own observation
 * or mastery. At most {@value #MAX_ENTRIES} entries and a name of at most {@value #MAX_NAME} characters are kept.
 * <p>
 * A fair copy (Gloam Ink, {@link ResearchNotesCopyRecipe}) holds its original's author, entries and time written and says
 * it is a copy ({@code fair_copy}). It is read exactly as its original is: notes count once per author. An original saves
 * as it always has, since {@code fair_copy} is written only when true; a sheet without it is an original.
 */
public record ResearchNotes(UUID author, String authorName, Map<String, ResearchState> entries, long written, boolean fairCopy) {
	public static final int MAX_ENTRIES = 32;
	public static final int MAX_NAME = 16;

	public ResearchNotes {
		authorName = authorName.length() > MAX_NAME ? authorName.substring(0, MAX_NAME) : authorName;
		TreeMap<String, ResearchState> kept = new TreeMap<>();
		for (Map.Entry<String, ResearchState> entry : new TreeMap<>(entries).entrySet()) {
			if (kept.size() >= MAX_ENTRIES) {
				break;
			}
			ResearchState state = entry.getValue();
			if (state != ResearchState.NONE) {
				kept.put(entry.getKey(), state.atLeast(ResearchState.UNDERSTOOD) ? ResearchState.UNDERSTOOD : state);
			}
		}
		entries = Collections.unmodifiableMap(kept);
	}

	/** An original sheet, as {@link ResearchNotesItem#write} takes one down. */
	public ResearchNotes(UUID author, String authorName, Map<String, ResearchState> entries, long written) {
		this(author, authorName, entries, written, false);
	}

	/** A fair copy of these notes: the same author, entries and time written, marked a copy. */
	public ResearchNotes asFairCopy() {
		return new ResearchNotes(author, authorName, entries, written, true);
	}

	public static final Codec<ResearchNotes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("author").forGetter(ResearchNotes::author),
			Codec.STRING.optionalFieldOf("author_name", "").forGetter(ResearchNotes::authorName),
			Codec.unboundedMap(Codec.STRING, ConcordanceCodecs.STATE).optionalFieldOf("entries", Map.of()).forGetter(ResearchNotes::entries),
			Codec.LONG.optionalFieldOf("written", 0L).forGetter(ResearchNotes::written),
			Codec.BOOL.optionalFieldOf("fair_copy", false).forGetter(ResearchNotes::fairCopy)
	).apply(instance, ResearchNotes::new));

	public static final StreamCodec<ByteBuf, ResearchNotes> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
