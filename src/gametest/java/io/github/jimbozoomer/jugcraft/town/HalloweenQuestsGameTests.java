package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Halloween quests (docs/features/halloween-quests.md): every errand names real items; out of season nothing is
 * taken; in season an errand needs every item, takes only plain ones, pays its Jugs and decoration once a Halloween,
 * keeps that through a save, and waits while the purse is too full.
 */
public class HalloweenQuestsGameTests {
	@GameTest
	public void errandsPayOnceAHalloween(GameTestHelper h) {
		for (HalloweenQuests.Quest quest : HalloweenQuests.QUESTS) {
			h.assertTrue(quest.itemType() != Items.AIR && quest.giftType() != Items.AIR, quest.id() + " names an item that is not registered");
		}
		var player = h.makeMockServerPlayerInLevel();
		var id = player.getUUID();
		var data = Jugs.data(h.getLevel().getServer());
		HalloweenQuests.Quest blessing = HalloweenQuests.QUESTS.get(0);
		var mode = HalloweenSeason.mode();
		// Switched back at once: the server runs other tests.
		try {
			HalloweenSeason.setMode(HalloweenSeason.Mode.OFF);
			player.getInventory().setItem(0, new ItemStack(blessing.itemType(), blessing.count()));
			h.assertTrue(!HalloweenQuests.complete(player, 0), "Out of season an errand is not taken");
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			player.getInventory().setItem(0, new ItemStack(blessing.itemType(), blessing.count() - 1));
			ItemStack named = new ItemStack(blessing.itemType(), blessing.count());
			named.set(DataComponents.CUSTOM_NAME, Component.literal("Keepsake"));
			player.getInventory().setItem(1, named);
			h.assertTrue(!HalloweenQuests.complete(player, 0), "One short (named items do not count) is not enough");
			player.getInventory().setItem(2, new ItemStack(blessing.itemType(), 3));
			long before = data.balance(id);
			h.assertTrue(HalloweenQuests.complete(player, 0), "Carrying them all hands the errand in");
			h.assertTrue(player.getInventory().getItem(1).getCount() == blessing.count(), "The named items are untouched");
			int left = player.getInventory().getItem(0).getCount() + player.getInventory().getItem(2).getCount();
			h.assertTrue(left == 2, "Exactly " + blessing.count() + " plain items were taken, leaving " + left + " of 8");
			h.assertTrue(data.balance(id) == before + blessing.reward(), "It pays " + blessing.reward() + " Jugs");
			h.assertTrue(player.getInventory().countItem(blessing.giftType()) == blessing.gifts(), "It gives its decoration");
			player.getInventory().setItem(0, new ItemStack(blessing.itemType(), blessing.count()));
			h.assertTrue(!HalloweenQuests.complete(player, 0), "It is done once a Halloween");
			var restored = Jugs.Data.CODEC.parse(JsonOps.INSTANCE, Jugs.Data.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow()).getOrThrow();
			h.assertTrue(HalloweenQuests.done(restored, id, 0), "Done survives a save");
			// A purse too full for the Jugs: the errand waits and nothing is taken.
			HalloweenQuests.Quest treats = HalloweenQuests.QUESTS.get(4);
			data.add(id, TownShops.get().maxBalance);
			player.getInventory().setItem(3, new ItemStack(treats.itemType(), treats.count()));
			h.assertTrue(!HalloweenQuests.complete(player, 4) && player.getInventory().getItem(3).getCount() == treats.count(),
					"A full purse leaves the errand and the items alone");
			h.assertTrue(!HalloweenQuests.complete(player, -1) && !HalloweenQuests.complete(player, 99), "Invalid errands refused");
		} finally {
			HalloweenSeason.setMode(mode);
		}
		h.succeed();
	}
}
