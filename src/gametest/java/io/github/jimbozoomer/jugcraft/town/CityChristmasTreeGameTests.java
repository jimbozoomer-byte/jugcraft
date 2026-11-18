package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.agriculture.HarbourGiftFir;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import java.time.LocalDate;
import java.time.MonthDay;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class CityChristmasTreeGameTests {
    @GameTest
    public void giftsArePublicAndCollectionStillWorksAfterClosing(GameTestHelper helper) {
        var skirt = HarbourGiftFir.CITY_SKIRT;
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(3, 2, 3));
        var north = Direction.NORTH;
        for (int i=0; i<skirt.cells().length; i++) {
            TownBuilder.set(level, skirt.partPos(root,north,i), skirt.defaultBlockState().setValue(skirt.partProperty(),i));
        }
        var pile = skirt.partPos(root,north,2);
        var hit = new BlockHitResult(Vec3.atCenterOf(pile),
            Direction.UP,pile,false);
        var donor = io.github.jimbozoomer.jugcraft.test.TestPlayers.survivor(helper);
        var guest = io.github.jimbozoomer.jugcraft.test.TestPlayers.survivor(helper);
        var hand = InteractionHand.MAIN_HAND;
        try {
            TownDecor.force("december");
            for (var player : new ServerPlayer[]{donor,guest}) {
                player.setGameMode(GameType.SURVIVAL);
                player.snapTo(Vec3.atBottomCenterOf(pile.north(1)));
            }
            donor.setShiftKeyDown(true);
            donor.setItemInHand(hand,new ItemStack(Items.COOKIE,7));
            donor.gameMode.useItemOn(donor,level,donor.getMainHandItem(),hand,hit);
            if (!donor.getMainHandItem().isEmpty() || level.getBlockState(pile).getValue(HarbourGiftFir.PARCELS)!=1)
                throw new AssertionError("Parcel never appeared in the chosen pile");
            TownDecor.force("winter");
            donor.setItemInHand(hand,new ItemStack(Items.APPLE,2));
            donor.gameMode.useItemOn(donor,level,donor.getMainHandItem(),hand,hit);
            if (donor.getMainHandItem().getCount()!=2) throw new AssertionError("Accepted a gift after closing");
            guest.setItemInHand(hand, ItemStack.EMPTY);
            guest.gameMode.useItemOn(guest,level,guest.getMainHandItem(),hand,hit);
            if (!guest.getMainHandItem().is(Items.COOKIE) || guest.getMainHandItem().getCount()!=7
                    || level.getBlockState(pile).getValue(HarbourGiftFir.PARCELS)!=0) throw new AssertionError("Could not collect the last gift");
        } finally {
            TownDecor.force(null);
        }
        helper.succeed();
    }

    @GameTest
    public void everyAuthoredBlockAndHangingOrnamentResolves(GameTestHelper helper) {
        var scene = TownData.get().centerpiece.get("december_v2");
        if (scene == null || scene.size() < 1000) throw new AssertionError("City-scale fir is missing");
        for (String text : new java.util.HashSet<>(scene.values())) {
            if (TownData.parse(text).isAir()) throw new AssertionError("Unregistered Christmas tree state " + text);
        }
        helper.succeed();
    }

    @GameTest
    public void squareKeepsThanksgivingAndClosesAfterTwelfthNight(GameTestHelper helper) {
        var settings = SeasonCalendar.Settings.DEFAULT;
        for (int year = 2026; year <= 2035; year++) {
            var days = settings.feastWindow(year);
            for (var day = days[0].minusDays(1); !day.isAfter(days[1]); day = day.plusDays(1)) {
                if (TownDecor.cityTreeOn(settings, day)) throw new AssertionError("Early Christmas on " + day);
            }
            for (var day = days[1].plusDays(1); !day.isAfter(LocalDate.of(year + 1, 1, 6)); day = day.plusDays(1)) {
                if (!TownDecor.cityTreeOn(settings, day)) throw new AssertionError("Missing city fir on " + day);
            }
            if (TownDecor.cityTreeOn(settings, LocalDate.of(year + 1, 1, 7))) throw new AssertionError("Late Christmas");
        }
        var later = new SeasonCalendar.Settings(settings.mode(), false, settings.zone(), false, 2, settings.feast(), 4,
            new SeasonCalendar.Window(MonthDay.of(12, 12), MonthDay.of(1, 6)), null);
        if (TownDecor.cityTreeOn(later, LocalDate.of(2026, 11, 30)) || !TownDecor.cityTreeOn(later, LocalDate.of(2026, 12, 12)))
            throw new AssertionError("Custom December start ignored");
        helper.succeed();
    }
}
