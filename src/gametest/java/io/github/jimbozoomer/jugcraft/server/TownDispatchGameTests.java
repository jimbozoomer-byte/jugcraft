package io.github.jimbozoomer.jugcraft.server;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TownDispatchGameTests {
    @GameTest public void parcelIsPerPlayerPerUtcDate(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        p.getInventory().clearContent();
        h.assertTrue(TownDispatch.parcel(p, 100, false) == TownDispatch.SEALED, "Browsing awards no items");
        int first = TownDispatch.parcel(p, 100, true);
        int count = TownDispatch.PRIZES.get(first).count();
        h.assertTrue(total(p) == count, "Rolled stack reached inventory");
        h.assertTrue(TownDispatch.parcel(p, 100, true) == first && total(p) == count, "Repeat cannot pay twice");
        h.assertTrue(TownDispatch.parcel(p, 99, true) == first && total(p) == count, "Clock rollback cannot pay twice");
        h.assertTrue(TownDispatch.parcel(p, 101, false) == TownDispatch.SEALED, "Next day's parcel is sealed");
        int second = TownDispatch.parcel(p, 101, true);
        h.assertTrue(total(p) == count + TownDispatch.PRIZES.get(second).count(), "Next day pays once");
        var other = h.makeMockServerPlayerInLevel();
        h.assertTrue(TownDispatch.parcel(other, 101, false) == TownDispatch.SEALED, "Second player has their own receipt");
        h.succeed();
    }

    @GameTest public void fullInventoryKeepsParcel(GameTestHelper h) {
        var p = h.makeMockServerPlayerInLevel();
        var inventory = p.getInventory();
        for (int i = 0; i < inventory.getNonEquipmentItems().size(); i++) inventory.setItem(i, new ItemStack(Items.STONE, 64));
        int fullCount = total(p);
        h.assertTrue(TownDispatch.parcel(p, 100, true) == TownDispatch.INVENTORY_FULL && total(p) == fullCount,
                "Full bags do not spend a parcel or drop items");
        inventory.setItem(0, ItemStack.EMPTY);
        int prize = TownDispatch.parcel(p, 100, true);
        var expected = TownDispatch.PRIZES.get(prize);
        h.assertTrue(inventory.getItem(0).is(expected.item()) && inventory.getItem(0).getCount() == expected.count(),
                "A free slot accepts the whole waiting parcel");
        h.succeed();
    }

    private static int total(ServerPlayer p) {
        return p.getInventory().getNonEquipmentItems().stream().mapToInt(ItemStack::getCount).sum();
    }
}

