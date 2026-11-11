package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * What happened between a player and a guest, after it is committed (coordinator's seam). Listeners (personality
 * reactions, milestones, notices) may send messages or record their own state; they must never take items, grant
 * affection outside {@link GuestRewards}, or change the ledger. Every event fires on the server thread.
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

	public static final Event<GiftGiven> GIFT_GIVEN = EventFactory.createArrayBacked(GiftGiven.class,
			listeners -> (player, npc, guest, gift, favourite, gained) -> {
				for (GiftGiven listener : listeners) {
					listener.given(player, npc, guest, gift, favourite, gained);
				}
			});
	public static final Event<QuestTurnedIn> QUEST_TURNED_IN = EventFactory.createArrayBacked(QuestTurnedIn.class,
			listeners -> (player, npc, guest, delivered, gained) -> {
				for (QuestTurnedIn listener : listeners) {
					listener.turnedIn(player, npc, guest, delivered, gained);
				}
			});
	public static final Event<Married> MARRIED = EventFactory.createArrayBacked(Married.class, listeners -> (player, npc, guest) -> {
		for (Married listener : listeners) {
			listener.married(player, npc, guest);
		}
	});
	public static final Event<Divorced> DIVORCED = EventFactory.createArrayBacked(Divorced.class, listeners -> (player, guest) -> {
		for (Divorced listener : listeners) {
			listener.divorced(player, guest);
		}
	});
	public static final Event<Talked> TALKED = EventFactory.createArrayBacked(Talked.class, listeners -> (player, npc, guest) -> {
		for (Talked listener : listeners) {
			listener.talked(player, npc, guest);
		}
	});

	private GuestEvents() {
	}
}
