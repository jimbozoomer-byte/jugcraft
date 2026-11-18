package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * The Bounty List (docs/features/bounty-list.md): every boss on it is a registered creature and every lair item a
 * registered item; the book holds a first page and a page for each; and it is given once per player.
 */
public class BountyListGameTests {
	@GameTest
	public void listNamesEveryBossOnce(GameTestHelper h) {
		for (BountyList.Boss boss : BountyList.BOSSES) {
			h.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(Jugcraft.id(boss.entity())), boss.entity() + " is not a registered creature");
			h.assertTrue(BuiltInRegistries.ITEM.containsKey(Jugcraft.id(boss.item())), boss.item() + " is not a registered item");
		}
		var content = BountyList.book().get(DataComponents.WRITTEN_BOOK_CONTENT);
		h.assertTrue(content != null && content.pages().size() == BountyList.BOSSES.size() + 1, "A first page and one per boss");
		var player = h.makeMockServerPlayerInLevel();
		h.assertTrue(BountyList.questCompleted(player), "The first quest gives the list");
		h.assertTrue(!BountyList.questCompleted(player), "Only the first");
		h.assertTrue(player.getInventory().countItem(net.minecraft.world.item.Items.WRITTEN_BOOK) == 1, "One book");
		h.succeed();
	}
}
