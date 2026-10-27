package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * Server to client: what a pact perch's screen shows and offers (Concordance Chunk D, D-04, D-05; D-S14, D-S15). It is sent
 * when the menu opens, as the answer to each request and after a reload. The client has no pact rules: this is all it
 * knows. Every list is cut to its bound before it is sent (and an empty stack dropped); the words are the server's.
 *
 * @param revision  the terms revision (it counts the offer); a request naming another is answered {@link PactMenu#CHANGED}
 * @param mode      NEGOTIATE (a visit: his beats, then his terms), KEEP (the reader's pact kept here), SEAT (the reader's pact
 *                  without its perch, offered a seat here), CLOSED (nothing the reader can do here; the notes say why) or
 *                  INVITE (an empty perch's chooser)
 * @param terms     every term of {@link #TERMS}, in that order: its key and its value in words (the record's, once sealed)
 * @param notes     what stands in the way of agreeing, how the pact stands, or why nothing can be done
 * @param patterns  the sky patterns the reader observed (KEEP): id, name, recall cost, whether this occurrence can still be recalled;
 *                  INVITE: the spirits the reader may invite (id: the spirit's sign; amount: how many things are still missing;
 *                  ready: none is); SEAT, with two or more: the reader's pacts without their perch (id: each spirit's sign)
 * @param offerings the offerings the reader carries (KEEP): item id, name, how many carried, whether the keeping has room
 * @param compact   the Open Hand Compact's materials, for their icons (its term says them in words); resonance its resonance
 * @param actions   a bit for each action the mode offers now ({@link #offers}); the server decides when one is pressed
 * @param result    the answer to the request this state answers; empty for the opening, CHOOSE and a reload
 * @param spirit    the spirit this state is about: its sign and the work it keeps or offers (GC-01; NONE for the chooser);
 *                  GC-05: its matter and answers on the kept page
 */
public record PactStatePayload(int containerId, int revision, int mode, Component speaker, List<Component> beats, List<Term> terms,
		List<Component> notes, List<Choice> patterns, List<Choice> offerings, List<ItemStack> compact, int resonance, int actions,
		Optional<Result> result, Spirit spirit) implements CustomPacketPayload {
	public static final int NEGOTIATE = 0;
	public static final int KEEP = 1;
	public static final int SEAT = 2;
	public static final int CLOSED = 3;
	/** GC-01 (SG4): an empty perch's chooser: the spirits its owner may invite (patterns), ready or not (notes). */
	public static final int INVITE = 4;
	public static final int MAX_BEATS = 3;
	public static final int MAX_TERMS = 16;
	public static final int MAX_NOTES = 12;
	public static final int MAX_CHOICES = 8;
	public static final int MAX_COMPACT = 8;
	/** GC-05: the most answers a matter offers (tools/concordance_pacts.py STATE_LIMITS). */
	public static final int MAX_ANSWERS = 3;
	public static final int MAX_KEY = 64;
	/**
	 * A choice's id: a pattern's or an offering's, both from data (the item tag, the pattern files), so bounded only as any
	 * string is (vanilla's 32,767), never a length that could fail to encode and drop its player (the unit review).
	 */
	public static final int MAX_ID = 32767;
	/** Every term the terms page shows, in order (D-S15 and the perch's own; tools/concordance_pacts.py TERMS). */
	public static final List<String> TERMS = List.of("service", "hours", "area", "quota", "upkeep", "grace", "suspension", "duration",
			"breach", "ending", "returns", "perch", "beneficiaries", "compact");
	public static final Type<PactStatePayload> TYPE = new Type<>(Jugcraft.id("concordance_pact_state"));

	/** One term: its key (labelled by the client, screen.jugcraft.pact.term.<key>) and its value in words. */
	public record Term(String key, Component value) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Term> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.stringUtf8(MAX_KEY), Term::key,
				ComponentSerialization.TRUSTED_STREAM_CODEC, Term::value,
				Term::new);
	}

	/** One thing to choose: its id, its name, a number (a recall's cost, how many are carried) and whether it can be chosen now. */
	public record Choice(String id, Component name, int amount, boolean ready) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Choice> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.stringUtf8(MAX_ID), Choice::id,
				ComponentSerialization.TRUSTED_STREAM_CODEC, Choice::name,
				ByteBufCodecs.VAR_INT, Choice::amount,
				ByteBufCodecs.BOOL, Choice::ready,
				Choice::new);
	}

	/** What a request came to: its own sequence, its reason (a stable id), whether it was done, and the words for it. */
	public record Result(int sequence, String reason, boolean done, Component message) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Result> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, Result::sequence,
				ByteBufCodecs.stringUtf8(MAX_KEY), Result::reason,
				ByteBufCodecs.BOOL, Result::done,
				ComponentSerialization.TRUSTED_STREAM_CODEC, Result::message,
				Result::new);
	}

	/**
	 * GC-01 (SG5): the spirit a state is about: its sign (an item id the screen draws by its name; PactSpirits, never derived
	 * from an offering) and the work of the pact it keeps, offers or seats ("" when none). GC-05: on the kept page, the
	 * matter its spirit raises (empty when none) and its answers, the safe course first (at most MAX_ANSWERS; each id an
	 * accord, name its words, amount 1 for the answer kept now and 0 otherwise, ready when RESOLVE may choose it). NONE for
	 * the chooser and a state built without one. Network only; never a fifteenth top-level field.
	 */
	public record Spirit(String sign, String work, Component matter, List<Choice> answers) {
		public static final Spirit NONE = new Spirit("", "", Component.empty(), List.of());
		public static final StreamCodec<RegistryFriendlyByteBuf, Spirit> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.stringUtf8(MAX_ID), Spirit::sign,
				ByteBufCodecs.stringUtf8(MAX_ID), Spirit::work,
				ComponentSerialization.TRUSTED_STREAM_CODEC, Spirit::matter,
				Choice.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ANSWERS)), Spirit::answers,
				Spirit::new);

		public Spirit {
			matter = matter == null ? Component.empty() : matter;
			answers = answers == null ? List.of() : bounded(answers, MAX_ANSWERS);
		}

		/** A spirit with no matter (GC-01's two parts): no line, no answers. */
		public Spirit(String sign, String work) {
			this(sign, work, Component.empty(), List.of());
		}
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, PactStatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PactStatePayload::containerId,
			ByteBufCodecs.VAR_INT, PactStatePayload::revision,
			ByteBufCodecs.VAR_INT, PactStatePayload::mode,
			ComponentSerialization.TRUSTED_STREAM_CODEC, PactStatePayload::speaker,
			ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_BEATS)), PactStatePayload::beats,
			Term.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_TERMS)), PactStatePayload::terms,
			ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_NOTES)), PactStatePayload::notes,
			Choice.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CHOICES)), PactStatePayload::patterns,
			Choice.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_CHOICES)), PactStatePayload::offerings,
			ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_COMPACT)), PactStatePayload::compact,
			ByteBufCodecs.VAR_INT, PactStatePayload::resonance,
			ByteBufCodecs.VAR_INT, PactStatePayload::actions,
			ByteBufCodecs.optional(Result.STREAM_CODEC), PactStatePayload::result,
			Spirit.STREAM_CODEC, PactStatePayload::spirit,
			PactStatePayload::new);

	public PactStatePayload {
		beats = bounded(beats, MAX_BEATS);
		terms = bounded(terms, MAX_TERMS);
		notes = bounded(notes, MAX_NOTES);
		patterns = bounded(patterns, MAX_CHOICES);
		offerings = bounded(offerings, MAX_CHOICES);
		compact = bounded(compact.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList(), MAX_COMPACT);
		spirit = spirit == null ? Spirit.NONE : spirit;
	}

	/** A state about no spirit in particular: Chunk D's thirteen fields (its tests build these), its spirit NONE. */
	public PactStatePayload(int containerId, int revision, int mode, Component speaker, List<Component> beats, List<Term> terms,
			List<Component> notes, List<Choice> patterns, List<Choice> offerings, List<ItemStack> compact, int resonance, int actions,
			Optional<Result> result) {
		this(containerId, revision, mode, speaker, beats, terms, notes, patterns, offerings, compact, resonance, actions, result, Spirit.NONE);
	}

	private static <T> List<T> bounded(List<T> list, int max) {
		return List.copyOf(list.subList(0, Math.min(list.size(), max)));
	}

	/** Whether the mode offers {@code action} now (the server still decides when it is pressed). */
	public boolean offers(int action) {
		return action >= 0 && action < 31 && (actions & (1 << action)) != 0;
	}

	/** The bits for {@code offered}. */
	public static int mask(int... offered) {
		int mask = 0;
		for (int action : offered) {
			mask |= 1 << action;
		}
		return mask;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
