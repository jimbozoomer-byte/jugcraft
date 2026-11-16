package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Halloween quests (docs/features/halloween-quests.md): during the Halloween season, residents with these jobs ask for
 * things from the pack's other Halloween content, and the commission board lists the same errands. Each is done once
 * a Halloween per player, at whichever of the two the player brings it to: all the items at once, for Jugs and a
 * decoration. The words are in the language file (tools/halloween_quests.py, which holds the same table). All
 * mutations run on the server thread; the record is the town ledger's, as the commissions' are.
 */
public final class HalloweenQuests {
	private HalloweenQuests() {
	}

	/** An errand: its residents' job, what they ask for and how many, the Jugs paid and the decoration given. */
	public record Quest(String id, String job, String item, int count, int reward, String gift, int gifts) {
		public Item itemType() {
			return BuiltInRegistries.ITEM.getValue(Identifier.parse(item));
		}

		public Item giftType() {
			return BuiltInRegistries.ITEM.getValue(Identifier.parse(gift));
		}

		public String key(String part) {
			return "halloween_quest.jugcraft." + id + "." + part;
		}
	}

	public static final List<Quest> QUESTS = List.of(
			new Quest("graveyard_blessing", "priest", "jugcraft:reaper_shade", 6, 8, "jugcraft:mourning_wreath", 1),
			new Quest("costume_silk", "vendor", "jugcraft:gossamer_silk", 4, 6, "jugcraft:witch_hat", 1),
			new Quest("horseman_proof", "mayor", "jugcraft:horseman_lantern", 1, 8, "jugcraft:haunted_portrait", 1),
			new Quest("harvest_bonfire", "gardener", "jugcraft:kings_ember", 1, 6, "jugcraft:mini_pumpkin_stack", 2),
			new Quest("party_treats", "baker", "jugcraft:candy_corn", 16, 3, "jugcraft:candy_bag", 1),
			new Quest("wisp_lanterns", "lamplighter", "jugcraft:wisp_in_a_jar", 3, 3, "jugcraft:jack_o_lantern_string_lights", 4),
			new Quest("street_pumpkins", "decorator", "minecraft:jack_o_lantern", 8, 2, "jugcraft:bat_bunting", 4),
			new Quest("ghost_samples", "watch", "jugcraft:ectoplasm", 4, 3, "jugcraft:black_cat_figure", 1));

	/** The errand a resident with this job gives, if any. */
	public static Optional<Integer> forJob(String job) {
		for (int i = 0; i < QUESTS.size(); i++) {
			if (QUESTS.get(i).job().equals(job)) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	private static String key(UUID player, Quest quest) {
		return player + "/halloween/" + quest.id();
	}

	/** Whether the player has done this errand this Halloween (HalloweenSeason.year). */
	public static boolean done(Jugs.Data data, UUID player, int index) {
		TownCommissions.Delivery saved = data.civic.get(key(player, QUESTS.get(index)));
		return saved != null && saved.paid() && saved.cycle() == HalloweenSeason.year();
	}

	/** How many of the asked-for item the player carries, unmodified, in the main inventory. */
	public static int carried(ServerPlayer player, int index) {
		Item item = QUESTS.get(index).itemType();
		int total = 0;
		for (int slot = 0; slot < 36; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (plain(stack, item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static boolean plain(ItemStack stack, Item item) {
		return item != Items.AIR && !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, new ItemStack(item));
	}

	/**
	 * Hands an errand in: in season, not yet done this Halloween, all the items carried and room in the purse. Takes
	 * the items from the main inventory (never equipment), pays the Jugs and gives the decoration (at the player's feet
	 * if the inventory is full). Returns whether it was done.
	 */
	public static boolean complete(ServerPlayer player, int index) {
		if (index < 0 || index >= QUESTS.size() || player.isSpectator() || !HalloweenSeason.active()) {
			return false;
		}
		Jugs.Data data = Jugs.data(player.level().getServer());
		Quest quest = QUESTS.get(index);
		if (done(data, player.getUUID(), index) || carried(player, index) < quest.count()
				|| data.balance(player.getUUID()) > TownShops.get().maxBalance - quest.reward()) {
			return false;
		}
		int remaining = quest.count();
		for (int slot = 0; slot < 36 && remaining > 0; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (plain(stack, quest.itemType())) {
				int take = Math.min(remaining, stack.getCount());
				stack.shrink(take);
				remaining -= take;
			}
		}
		player.getInventory().setChanged();
		data.civic.put(key(player.getUUID(), quest), new TownCommissions.Delivery(HalloweenSeason.year(), quest.count(), true, ""));
		data.add(player.getUUID(), quest.reward());
		data.setDirty();
		player.getInventory().placeItemBackInInventory(new ItemStack(quest.giftType(), quest.gifts()), Prediction.SERVER_ONLY);
		BountyList.questCompleted(player);
		return true;
	}

	/**
	 * A player speaks to a resident in the Halloween season. Returns false when this resident has no errand for them
	 * (out of season, no errand for the job, or done this Halloween), so they chat as usual; otherwise they take the
	 * errand if the player carries it all, or say what they want.
	 */
	public static boolean talk(ServerPlayer player, Townsfolk resident) {
		if (!HalloweenSeason.active()) {
			return false;
		}
		Optional<Integer> found = forJob(resident.job());
		if (found.isEmpty() || done(Jugs.data(player.level().getServer()), player.getUUID(), found.get())) {
			return false;
		}
		int index = found.get();
		Quest quest = QUESTS.get(index);
		if (carried(player, index) >= quest.count()
				&& Jugs.data(player.level().getServer()).balance(player.getUUID()) > TownShops.get().maxBalance - quest.reward()) {
			player.sendSystemMessage(Component.translatable("message.jugcraft.halloween_quest.purse_full", resident.getDisplayName()));
			return true;
		}
		Component said = Component.translatable(quest.key(complete(player, index) ? "thanks" : "ask"));
		player.sendSystemMessage(Component.translatable("message.jugcraft.townsfolk.says", resident.getDisplayName(), said));
		return true;
	}
}
