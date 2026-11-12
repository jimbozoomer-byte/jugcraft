package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The shared meal (worker D), menu option {@code milestones/meal}: once per player and guest per game day, the player
 * brings one plain, modest food in the main hand ({@link #FOODS}), exactly that one item is eaten together, and
 * {@link GuestRewards#grant} adds at most {@link #GRANT} affection within the one shared daily budget. Friendly for every
 * guest, held guests included: nothing romantic.
 *
 * <p>As {@link GuestRewards} requires, the meal is offered and taken only while a gain is possible
 * ({@link GuestRewards#left} above 0 and {@link GuestLedger#canGainAffection}), so no food is taken for nothing, with
 * one stated exception (owner decision, 12 November): a couple the ledger already has married, at full affection
 * ({@link #marriedAtFull}), may still share their one meal a day. It takes exactly one food, records the day, adds no
 * affection and says so plainly; the shared budget is not touched. An unmarried guest at full affection (every held
 * guest included, since the ledger never marries one) is still refused, and nothing is taken.
 *
 * <p>The day is the one the shared budget uses ({@code GuestFavors}' day, never below the player's high-water day), and
 * a meal of either kind is refused while the last one's day is today or later, so a clock moved backwards gives no second
 * meal.
 */
public final class GuestMilestoneMeals {
	/** What a shared meal adds, at most, within the shared daily budget. */
	public static final int GRANT = 8;
	/** The foods a meal may be: plain, modest, cooked or ready to eat; no bowl foods and nothing golden. */
	public static final List<Item> FOODS = List.of(Items.BREAD, Items.BAKED_POTATO, Items.COOKED_COD, Items.COOKED_SALMON,
			Items.COOKED_CHICKEN, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON, Items.COOKED_BEEF, Items.PUMPKIN_PIE,
			Items.COOKIE, Items.APPLE);

	private GuestMilestoneMeals() {
	}

	/** Whether a meal is offered now (cheap, no side effects). */
	static boolean available(ServerPlayer player, String guest) {
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		GuestMilestoneState state = GuestMilestoneState.get(server);
		if (state.frozen(me) || !food(player.getMainHandItem())) {
			return false;
		}
		Long last = state.mealDay(me, guest);
		if (last != null && last >= today(player)) {
			return false;
		}
		return GuestRewards.left(player, guest) > 0 && GuestLedger.get(server).canGainAffection(me, guest) || marriedAtFull(server, me, guest);
	}

	/**
	 * Shares a meal: every check, then exactly one food from the hand, the day recorded, then the bounded grant (or, for a
	 * married couple at full affection, no grant at all).
	 */
	static GuestResult share(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult away = GuestMilestones.beside(player, npc, guest);
		if (away != null) {
			return away;
		}
		MinecraftServer server = player.level().getServer();
		UUID me = player.getUUID();
		Component name = GuestMilestones.name(guest);
		GuestMilestoneState state = GuestMilestoneState.get(server);
		if (state.frozen(me)) {
			return GuestResult.refused("message.jugcraft.guest.milestone.locked",
					"Your keepsake record cannot be read just now; an operator can check it.");
		}
		ItemStack hand = player.getMainHandItem();
		if (!food(hand)) {
			return GuestResult.refused("message.jugcraft.guest.meal.food",
					"Hold a simple meal in your main hand to share it: bread, a baked potato, cooked fish or meat, pumpkin pie, a cookie or an apple.");
		}
		long today = today(player);
		Long last = state.mealDay(me, guest);
		if (last != null && last >= today) {
			return GuestResult.refused("message.jugcraft.guest.meal.today", "You have already shared a meal with %s today.", name);
		}
		GuestLedger ledger = GuestLedger.get(server);
		if (!ledger.canGainAffection(me, guest)) {
			if (marriedAtFull(server, me, guest)) {
				Component eaten = eat(player, hand);
				state.meal(me, guest, today);
				GuestMilestones.check(player, guest);
				return GuestResult.done("message.jugcraft.guest.meal.shared_full",
						"You share a simple meal of %s with %s. You are already as close as can be, so it adds no affection.", eaten, name);
			}
			return ledger.affection(me, guest) >= GuestBalance.MAX_AFFECTION
					? GuestResult.refused("message.jugcraft.guest.full_affection", "%s is already as fond of you as can be.", name)
					: GuestResult.refused("message.jugcraft.guest.record_locked",
							"Your friendship with %s cannot change right now; an operator can check it.", name);
		}
		if (GuestRewards.left(player, guest) <= 0) {
			return GuestResult.refused("message.jugcraft.guest.cap_today",
					"You have earned all the affection you can with %s today. Come back tomorrow.", name);
		}
		Component eaten = eat(player, hand);
		state.meal(me, guest, today);
		int added = GuestRewards.grant(player, guest, GRANT);
		GuestMilestones.check(player, guest);
		return GuestResult.done("message.jugcraft.guest.meal.shared", "You share a simple meal of %s with %s: +%s affection.", eaten, name, added);
	}

	/**
	 * The one case a meal is shared for no affection: the ledger has the player and this marriage-eligible guest married
	 * to each other ({@link GuestKeepsakes#romance}: {@code mayMarry()} and the spouse both ways), both saved entries are
	 * readable, and the player's affection with the guest is at {@link GuestBalance#MAX_AFFECTION}. Never true for a held
	 * guest. Reads only.
	 */
	static boolean marriedAtFull(MinecraftServer server, UUID player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		GuestLedger ledger = GuestLedger.get(server);
		return entry != null && entry.available() && entry.mayMarry() && GuestKeepsakes.romance(server, player, guest)
				&& !ledger.unreadable(guest) && !ledger.affectionLocked(player) && ledger.affection(player, guest) >= GuestBalance.MAX_AFFECTION;
	}

	/** Eats exactly one food from the very stack in the hand; what it was. */
	private static Component eat(ServerPlayer player, ItemStack hand) {
		Component eaten = hand.getHoverName();
		hand.shrink(1);
		player.getInventory().setChanged();
		return eaten;
	}

	/** One of {@link #FOODS}, a plain stack (no custom name or other changed component). */
	static boolean food(ItemStack stack) {
		return !stack.isEmpty() && FOODS.contains(stack.getItem()) && ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()));
	}

	/** The day the shared budget uses for the player: GuestFavors' day, never below their high-water day. */
	static long today(ServerPlayer player) {
		return GuestFavors.State.get(player.level().getServer()).effective(player.getUUID(), GuestFavors.day(player));
	}
}
