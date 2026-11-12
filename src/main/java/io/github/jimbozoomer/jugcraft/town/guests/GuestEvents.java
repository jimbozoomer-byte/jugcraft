package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * What happened between a player and a guest, after it is committed (coordinator's seam). Listeners (personality
 * reactions, milestones, notices) may send messages or record their own state; they must never take items, grant
 * affection outside {@link GuestRewards}, or change the ledger. Every event fires on the server thread, synchronously,
 * from the place that committed it.
 *
 * <p>Each event is a {@link Listeners}: {@code register} a listener, {@code invoker()} to fire. It is a plain list
 * rather than Fabric's {@code EventFactory}, which nothing else in Jugcraft calls (so its use here was never compiled
 * against this platform); the call sites are the same as with a Fabric event.
 */
public final class GuestEvents {
	/** A gift was taken and recorded: {@code gift} is a one-item copy, {@code gained} the affection it added. */
	public interface GiftGiven {
		void given(ServerPlayer player, Townsfolk npc, String guest, ItemStack gift, boolean favourite, int gained);
	}

	/** Today's fetch quest was turned in and recorded. */
	public interface QuestTurnedIn {
		void turnedIn(ServerPlayer player, Townsfolk npc, String guest, ItemStack delivered, int gained);
	}

	/** A confirmed proposal was committed: the two are married. */
	public interface Married {
		void married(ServerPlayer player, Townsfolk npc, String guest);
	}

	/** A confirmed divorce was committed: both are free and their cooldowns run. */
	public interface Divorced {
		void divorced(ServerPlayer player, String guest);
	}

	/** The player opened the guest's menu (talked to it). */
	public interface Talked {
		void talked(ServerPlayer player, Townsfolk npc, String guest);
	}

	/**
	 * One event: its listeners, in the order they registered, and the one invoker that calls each in turn on the calling
	 * thread. A listener that throws is logged and skipped, so the others still run and the source, whose work is already
	 * committed, is never interrupted by a listener.
	 */
	public static final class Listeners<T> {
		private final List<T> listeners = new CopyOnWriteArrayList<>();
		private final T invoker;

		private Listeners(Function<Listeners<T>, T> invoker) {
			this.invoker = invoker.apply(this);
		}

		/** Adds a listener, called after those already registered. */
		public void register(T listener) {
			listeners.add(Objects.requireNonNull(listener, "listener"));
		}

		/** What the source calls to fire the event to every listener. */
		public T invoker() {
			return invoker;
		}

		/** Calls {@code call} with each listener, logging (not throwing) what a listener throws. */
		void each(Consumer<T> call) {
			for (T listener : listeners) {
				try {
					call.accept(listener);
				} catch (RuntimeException e) {
					Jugcraft.LOGGER.error("[guests] A guest event listener failed: {}", e.toString(), e);
				}
			}
		}
	}

	public static final Listeners<GiftGiven> GIFT_GIVEN = new Listeners<GiftGiven>(all -> (player, npc, guest, gift, favourite, gained) ->
			all.each(listener -> listener.given(player, npc, guest, gift, favourite, gained)));
	public static final Listeners<QuestTurnedIn> QUEST_TURNED_IN = new Listeners<QuestTurnedIn>(all -> (player, npc, guest, delivered, gained) ->
			all.each(listener -> listener.turnedIn(player, npc, guest, delivered, gained)));
	public static final Listeners<Married> MARRIED = new Listeners<Married>(all -> (player, npc, guest) ->
			all.each(listener -> listener.married(player, npc, guest)));
	public static final Listeners<Divorced> DIVORCED = new Listeners<Divorced>(all -> (player, guest) ->
			all.each(listener -> listener.divorced(player, guest)));
	public static final Listeners<Talked> TALKED = new Listeners<Talked>(all -> (player, npc, guest) ->
			all.each(listener -> listener.talked(player, npc, guest)));

	private GuestEvents() {
	}
}
