package io.github.jimbozoomer.jugcraft.town;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * The Bounty List (docs/features/bounty-list.md): the first quest a player completes, a city order collected or a
 * Halloween errand handed in, also gives them a written book naming every lair boss and how its lair is opened. Once per
 * player, kept in the town ledger as the quests are. The words are in the language file (tools/bounty_list.py, which
 * holds the same table).
 */
public final class BountyList {
	private BountyList() {
	}

	/** A boss on the list: its entity id and the item that opens its lair (both jugcraft:). */
	public record Boss(String entity, String item) {
	}

	public static final List<Boss> BOSSES = List.of(
			new Boss("vesperine", "death_knell"),
			new Boss("tatterlace", "cursed_spindle"),
			new Boss("yeti_king", "frost_horn"),
			new Boss("cinder_tyrant", "kiln_seal"),
			new Boss("mire_hag", "hag_stone"),
			new Boss("crypt_lich", "exhumation_writ"),
			new Boss("iron_dreadnought", "sealed_orders"),
			new Boss("alpha_werewolf", "howling_totem"),
			new Boss("cydonian_sentinel", "altar_keystone"),
			new Boss("pumpkin_king", "harvest_crown"),
			new Boss("hessian_horseman", "hessians_bridle"));

	private static String key(UUID player) {
		return player + "/bounty_list";
	}

	/** Whether the player has been given the list. */
	public static boolean given(Jugs.Data data, UUID player) {
		return data.civic.containsKey(key(player));
	}

	/** The book: a first page, then a page for each boss with its name, its lair's item and how to use it. */
	public static ItemStack book() {
		List<Filterable<Component>> pages = new ArrayList<>();
		pages.add(Filterable.passThrough(Component.translatable("bounty_list.jugcraft.intro")));
		for (Boss boss : BOSSES) {
			pages.add(Filterable.passThrough(Component.empty()
					.append(Component.translatable("entity.jugcraft." + boss.entity()).withStyle(ChatFormatting.BOLD))
					.append("\n\n")
					.append(Component.translatable("bounty_list.jugcraft." + boss.entity()))));
		}
		ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
		stack.set(DataComponents.WRITTEN_BOOK_CONTENT,
				new WrittenBookContent(Filterable.passThrough("Bounty List"), "Town Hall", 0, List.copyOf(pages), true));
		return stack;
	}

	/**
	 * Called when a player completes a quest: the first time, records it and gives the book (at their feet if the
	 * inventory is full) with a line saying why. Returns whether the book was given.
	 */
	public static boolean questCompleted(ServerPlayer player) {
		Jugs.Data data = Jugs.data(player.level().getServer());
		if (given(data, player.getUUID())) {
			return false;
		}
		data.civic.put(key(player.getUUID()), new TownCommissions.Delivery(0, 1, true, ""));
		data.setDirty();
		player.getInventory().placeItemBackInInventory(book(), Prediction.SERVER_ONLY);
		player.sendSystemMessage(Component.translatable("message.jugcraft.bounty_list.given"));
		return true;
	}
}
