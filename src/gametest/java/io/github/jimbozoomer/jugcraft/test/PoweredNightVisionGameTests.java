package io.github.jimbozoomer.jugcraft.test;

import eu.pb4.trinkets.api.TrinketsApi;
import io.github.jimbozoomer.jugcraft.gear.Exosuit;
import io.github.jimbozoomer.jugcraft.gear.ExosuitItem;
import io.github.jimbozoomer.jugcraft.gear.JugcraftExosuit;
import io.github.jimbozoomer.jugcraft.jimtafacts.Jimtafacts;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import io.github.jimbozoomer.jugcraft.tools.EquipmentSettings;
import io.github.jimbozoomer.jugcraft.tools.PoweredNightVision;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Who owns a player's night vision when powered helmets, potions, beacons and accessories overlap
 * (docs/features/powered-night-vision.md). Vanilla merges every new night vision into the player's one existing instance
 * in place, so these tests merge real effects into the helmet's own and check what the helmet then does.
 * <p>
 * Each test seals its player in a stone room so the server's light at their eyes is 0, and waits 20 ticks for the light
 * to settle. A test player's connection is never ticked, so the tests that let time pass call {@code doTick} once a
 * game tick, as a connected player's connection would; the equipment callbacks are the server's own end-of-tick ones.
 */
public final class PoweredNightVisionGameTests {
    private static final String GOGGLES = "jimta_industrial_night_vision_goggles";

    private static ItemStack goggles(long energy) {
        ItemStack stack = new ItemStack(IndustrialGear.ITEMS.get(GOGGLES));
        Chargeable.setEnergy(stack, energy);
        return stack;
    }

    private static ItemStack exosuit(ArmorType type, long energy) {
        ItemStack stack = new ItemStack(JugcraftExosuit.piece(ExosuitItem.Style.VANGUARD, type));
        Chargeable.setEnergy(stack, energy);
        return stack;
    }

    /** What a beacon gives: ambient, with particles and an icon (BeaconBlockEntity's effect). */
    private static MobEffectInstance beacon(int duration) {
        return new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0, true, true);
    }

    private static MobEffectInstance vision(ServerPlayer player) {
        return player.getEffect(MobEffects.NIGHT_VISION);
    }

    private static long energy(ServerPlayer player) {
        return Chargeable.energy(player.getItemBySlot(EquipmentSlot.HEAD));
    }

    /** The look of a powered helmet's own vision: ambient, without particles, with an icon, level I. */
    private static boolean helmetLook(MobEffectInstance effect) {
        return effect != null && effect.isAmbient() && !effect.isVisible() && effect.showIcon() && effect.getAmplifier() == 0;
    }

    private static boolean dark(ServerPlayer player) {
        return player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) < Exosuit.DARK_BELOW;
    }

    /** A survival player standing in a sealed stone room (inside 1..3 x 1..4 x 1..3), logged out when the test ends. */
    private static ServerPlayer inTheDark(GameTestHelper h) {
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = 0; z <= 4; z++) {
                    boolean wall = x == 0 || x == 4 || y == 0 || y == 5 || z == 0 || z == 4;
                    h.setBlock(new BlockPos(x, y, z), wall ? Blocks.STONE : Blocks.AIR);
                }
            }
        }
        ServerPlayer player = TestPlayers.survivor(h);
        player.snapTo(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(2, 1, 2))));
        return player;
    }

    @GameTest(maxTicks = 60)
    public void helmetsGiveAndTakeAwayOnlyTheirOwnVision(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            // Industrial goggles: on, off, back on, switched off.
            ItemStack goggles = goggles(1_000);
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            h.assertTrue(helmetLook(vision(p)) && vision(p).getDuration() == PoweredNightVision.INDUSTRIAL_DURATION
                    && energy(p) == 1_000 - PoweredNightVision.INDUSTRIAL_COST, "Worn in the dark, the goggles pay once for their own vision: " + vision(p) + ", " + energy(p) + " JE");
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == null, "Taking the goggles off takes their vision away at once");
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            h.assertTrue(helmetLook(vision(p)) && energy(p) == 800, "Put back on, they resume their already-paid vision without paying again: " + energy(p));
            CustomData.update(DataComponents.CUSTOM_DATA, goggles, tag -> tag.putBoolean("JimtaGearOff", true));
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == null && energy(p) == 800, "Switched off, the goggles take their vision away and pay nothing");
            CustomData.update(DataComponents.CUSTOM_DATA, goggles, tag -> tag.putBoolean("JimtaGearOff", false));
            PoweredNightVision.tick(p);
            MobEffectInstance gogglesVision = vision(p);
            h.assertTrue(helmetLook(gogglesVision), "Switched on again, they give it again");

            // An exosuit helmet takes over: the goggles' vision goes, the helmet gives and pays for its own.
            ItemStack helmet = exosuit(ArmorType.HELMET, 3);
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            h.assertTrue(helmetLook(vision(p)) && vision(p) != gogglesVision && vision(p).getDuration() == Exosuit.NIGHT_VISION_DURATION
                    && energy(p) == 3 - Exosuit.NIGHT_VISION_PER_TICK, "The exosuit helmet replaces the goggles' vision with its own: " + vision(p) + ", " + energy(p) + " JE");
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == null && energy(p) == 1, "Flat, the helmet takes its own vision away");
            Chargeable.setEnergy(helmet, 1_000);
            PoweredNightVision.tick(p);
            h.assertTrue(helmetLook(vision(p)) && energy(p) == 998, "Recharged, it gives its vision again for 2 JE a tick");
            EquipmentSettings.setNumber(helmet, "night_vision", 0);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == null && energy(p) == 998, "Its night vision setting off, the helmet takes its vision away and pays nothing");
            EquipmentSettings.setNumber(helmet, "night_vision", 1);

            // Vision the helmet did not give, even with exactly its look, is neither paid for nor taken away.
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Exosuit.NIGHT_VISION_DURATION, 0, true, false, true));
            MobEffectInstance alike = vision(p);
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == alike && Chargeable.energy(helmet) == 998, "An alike vision the helmet did not give stays, unpaid");
            p.removeEffect(MobEffects.NIGHT_VISION);

            // Spectators get no powered vision.
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            h.assertTrue(helmetLook(vision(p)), "Worn again, the helmet gives its vision");
            p.setGameMode(GameType.SPECTATOR);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == null, "A spectator's powered vision is taken away");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 60)
    public void aLongerPotionIsKeptAndTheHelmetResumesAfterIt(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            // The potion first: the goggles leave it alone and pay nothing.
            PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION).finishUsingItem(h.getLevel(), p);
            MobEffectInstance potion = vision(p);
            h.assertTrue(potion != null && !potion.isAmbient() && potion.getDuration() == 3600, "The drunk potion gives three minutes: " + potion);
            p.setItemSlot(EquipmentSlot.HEAD, goggles(10_000));
            Exosuit.tick(p);
            h.assertTrue(vision(p) == potion && potion.getDuration() == 3600 && energy(p) == 10_000, "The goggles neither pay for nor change the potion's vision");
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            Exosuit.tick(p);
            h.assertTrue(vision(p) == potion, "Taking them off keeps the potion");
            p.removeEffect(MobEffects.NIGHT_VISION);

            // The goggles first, then the potion: vanilla merges the potion into the goggles' own instance in place.
            ItemStack goggles = goggles(10_000);
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            Exosuit.tick(p);
            MobEffectInstance own = vision(p);
            h.assertTrue(helmetLook(own) && energy(p) == 9_800, "The goggles' own vision is on");
            PotionContents.createItemStack(Items.POTION, Potions.NIGHT_VISION).finishUsingItem(h.getLevel(), p);
            h.assertTrue(vision(p) == own && !own.isAmbient() && own.isVisible() && own.getDuration() == 3600,
                    "The longer potion took over the same instance's duration and look: " + vision(p));
            for (int i = 0; i < 3; i++) {
                Exosuit.tick(p);
            }
            h.assertTrue(vision(p) == own && own.getDuration() == 3600 && energy(p) == 9_800, "The goggles neither pay for nor refresh it");
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            Exosuit.tick(p);
            h.assertTrue(vision(p) == own && own.getDuration() == 3600, "Taking the goggles off keeps the potion's whole vision");
            // Once the potion's vision has gone (here at once), the goggles give and own their vision again.
            p.removeEffect(MobEffects.NIGHT_VISION);
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            Exosuit.tick(p);
            h.assertTrue(helmetLook(vision(p)) && vision(p) != own, "The goggles' own vision is restored after the potion: " + vision(p));
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            Exosuit.tick(p);
            h.assertTrue(vision(p) == null, "...and is again theirs to take away");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 520)
    public void aShorterPotionMergedIntoHelmetVisionRunsOutBeforeTheHelmetResumes(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        MobEffectInstance[] merged = {null};
        long[] paid = {0};
        int[] left = {0};
        boolean[] ended = {false};
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            ItemStack goggles = goggles(10_000);
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            MobEffectInstance own = vision(p);
            h.assertTrue(helmetLook(own) && own.getDuration() == PoweredNightVision.INDUSTRIAL_DURATION, "The goggles' own vision is on");
            // A food's or a potion's shorter vision: the longer duration stays, the look becomes the potion's, in place.
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300));
            h.assertTrue(vision(p) == own && own.getDuration() == PoweredNightVision.INDUSTRIAL_DURATION && !own.isAmbient() && own.isVisible(),
                    "The shorter potion merged into the same instance: " + own);
            paid[0] = energy(p);
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own, "Taking the goggles off never takes away vision a potion is part of");
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own && energy(p) == paid[0], "Back on, the goggles leave it alone and pay nothing");
            left[0] = own.getDuration();
            merged[0] = own;
        });
        h.onEachTick(() -> {
            if (merged[0] == null) {
                return;
            }
            p.doTick();
            MobEffectInstance now = vision(p);
            if (!ended[0]) {
                if (now == merged[0]) {
                    h.assertTrue(energy(p) == paid[0] && !now.isAmbient(), "The goggles do not pay for or take over the merged vision");
                    left[0] = now.getDuration();
                    return;
                }
                h.assertTrue(now == null && left[0] <= 1, "The merged vision ran its whole time; it went with " + left[0] + " ticks left: " + now);
                ended[0] = true;
                return;
            }
            // The next end-of-tick callback gave the goggles' own vision again, on the time already paid.
            h.assertTrue(helmetLook(now) && now.getDuration() > PoweredNightVision.INDUSTRIAL_REFRESH && energy(p) == paid[0],
                    "The goggles resume with their own vision once it has run out: " + now + ", " + energy(p) + " JE");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 60)
    public void beaconVisionIsNeverTheHelmets(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            ItemStack helmet = exosuit(ArmorType.HELMET, 1_000);
            // A beacon's vision first.
            p.addEffect(beacon(340));
            MobEffectInstance pulse = vision(p);
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == pulse && pulse.isVisible() && pulse.getDuration() == 340 && energy(p) == 1_000,
                    "The helmet neither pays for nor changes a beacon's vision");
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == pulse, "Taking the helmet off keeps the beacon's vision");
            p.removeEffect(MobEffects.NIGHT_VISION);

            // The helmet's vision first, then a shorter beacon pulse (a one-level beacon gives 220 ticks): only its particles merge in.
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            MobEffectInstance own = vision(p);
            h.assertTrue(helmetLook(own) && energy(p) == 998, "The helmet's own vision is on");
            p.addEffect(beacon(220));
            h.assertTrue(vision(p) == own && own.isAmbient() && own.isVisible() && own.getDuration() == Exosuit.NIGHT_VISION_DURATION,
                    "The shorter beacon pulse kept the duration and took the particles, in place: " + own);
            PoweredNightVision.tick(p);
            h.assertTrue(energy(p) == 998, "The helmet stops paying once a beacon is part of the vision");
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own, "Taking the helmet off keeps vision a beacon is part of");
            p.removeEffect(MobEffects.NIGHT_VISION);

            // A longer beacon pulse merged into the helmet's vision.
            p.setItemSlot(EquipmentSlot.HEAD, helmet);
            PoweredNightVision.tick(p);
            own = vision(p);
            p.addEffect(beacon(340));
            h.assertTrue(vision(p) == own && own.isVisible() && own.getDuration() == 340, "The longer beacon pulse took over, in place: " + own);
            PoweredNightVision.tick(p);
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own && own.getDuration() == 340 && Chargeable.energy(helmet) == 996, "The beacon's vision is kept, unpaid");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 60)
    public void alikeOrEndlessVisionMergedIntoTheHelmetsIsNotTakenAway(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            ItemStack goggles = goggles(10_000);
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            MobEffectInstance own = vision(p);
            // Ten player ticks, each followed by the helmet's check, as on a running server.
            for (int i = 0; i < 10; i++) {
                p.doTick();
                PoweredNightVision.tick(p);
            }
            int remaining = PoweredNightVision.INDUSTRIAL_DURATION - 10;
            h.assertTrue(vision(p) == own && own.getDuration() == remaining && energy(p) == 9_800, "The goggles' vision counts down: " + own);
            // Another source with exactly the helmet's look, a little longer than what is left (still within the
            // helmet's own full duration): the same instance's duration goes up, which the helmet's alone never does.
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, remaining + 5, 0, true, false, true));
            h.assertTrue(vision(p) == own && own.getDuration() == remaining + 5, "The alike effect merged in place: " + own);
            PoweredNightVision.tick(p);
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own && own.getDuration() == remaining + 5 && Chargeable.energy(goggles) == 9_800,
                    "Vision something else lengthened is no longer the goggles' to take away");
            p.removeEffect(MobEffects.NIGHT_VISION);

            // An endless vision with the helmet's look (MobEffectInstance.INFINITE_DURATION is -1).
            p.setItemSlot(EquipmentSlot.HEAD, goggles);
            PoweredNightVision.tick(p);
            own = vision(p);
            h.assertTrue(helmetLook(own), "The goggles' own vision is on again");
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, true, false, true));
            h.assertTrue(vision(p) == own && own.isInfiniteDuration(), "The endless effect merged in place: " + own);
            p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            PoweredNightVision.tick(p);
            h.assertTrue(vision(p) == own && own.isInfiniteDuration(), "An endless vision is never the goggles' to take away");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 420)
    public void gogglesBesideTheExosuitGiveSteadyVisionAtTheirOwnPrice(GameTestHelper h) {
        // The 7 November report: worn with the exosuit, whose callback runs for every player each tick, the goggles
        // spent charge but gave no vision. Only the server's own end-of-tick callbacks act here.
        ServerPlayer p = inTheDark(h);
        long[] start = {-1};
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            p.setItemSlot(EquipmentSlot.CHEST, exosuit(ArmorType.CHESTPLATE, 100_000));
            p.setItemSlot(EquipmentSlot.LEGS, exosuit(ArmorType.LEGGINGS, 100_000));
            p.setItemSlot(EquipmentSlot.FEET, exosuit(ArmorType.BOOTS, 100_000));
            p.setItemSlot(EquipmentSlot.HEAD, goggles(10_000));
            start[0] = h.getLevel().getGameTime();
        });
        h.onEachTick(() -> {
            long k = start[0] < 0 ? 0 : h.getLevel().getGameTime() - start[0];
            if (k <= 0) {
                return;
            }
            p.doTick();
            MobEffectInstance now = vision(p);
            h.assertTrue(helmetLook(now) && now.getDuration() > 200, "Steady goggles vision, above the 200-tick flashing window, at tick " + k + ": " + now);
            // k end-of-tick callbacks have run since the goggles went on; each 200 JE pays for 160 of them.
            long expected = 10_000 - PoweredNightVision.INDUSTRIAL_COST * ((k - 1) / PoweredNightVision.INDUSTRIAL_PAID_TICKS + 1);
            h.assertTrue(energy(p) == expected, "The goggles pay 200 JE per 160 ticks of vision: " + energy(p) + " JE at tick " + k + ", expected " + expected);
            if (k == 340) {
                p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                Exosuit.tick(p);
                h.assertTrue(vision(p) == null, "Taking the goggles off beside the suit takes their vision away at once");
                h.succeed();
            }
        });
    }

    @GameTest(maxTicks = 460)
    public void theJimtafactsGogglesAndAnExosuitHelmetDoNotFight(GameTestHelper h) {
        // Two Jugcraft sources worn together: the accessory (head/hat slot) refreshes its own ambient, icon-less vision
        // every fourth tick; the exosuit helmet must neither strip it nor pay for it, and takes over once it has run out.
        ServerPlayer p = inTheDark(h);
        Jimtafacts.Accessory accessory = (Jimtafacts.Accessory) Jimtafacts.ITEMS.get("night_vision_goggles");
        long[] start = {-1};
        long[] removedAt = {-1};
        long[] spent = {-1};
        int[] untilGone = {-1};
        boolean[] gone = {false};
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            p.setItemSlot(EquipmentSlot.HEAD, exosuit(ArmorType.HELMET, 10_000));
            TrinketsApi.getAttachment(p).getInventory(accessory.slot).setItem(0, new ItemStack(accessory));
            start[0] = h.getLevel().getGameTime();
        });
        h.onEachTick(() -> {
            long k = start[0] < 0 ? 0 : h.getLevel().getGameTime() - start[0];
            if (k <= 0) {
                return;
            }
            p.doTick();
            MobEffectInstance now = vision(p);
            long used = 10_000 - energy(p);
            if (removedAt[0] < 0) {
                h.assertTrue(now != null && now.getDuration() > 200, "Steady vision with both worn, at tick " + k + ": " + now);
                if (k == 10) {
                    // The helmet paid until the accessory's vision merged into its own (the accessory acts every fourth tick).
                    h.assertTrue(used <= 4 * Exosuit.NIGHT_VISION_PER_TICK && !now.showIcon(), "The accessory's vision took over: " + now + ", " + used + " JE");
                    spent[0] = used;
                } else if (k > 10) {
                    h.assertTrue(used == spent[0], "The helmet pays nothing while the accessory gives vision: " + used + " JE at tick " + k);
                }
                if (k == 100) {
                    TrinketsApi.getAttachment(p).getInventory(accessory.slot).setItem(0, ItemStack.EMPTY);
                    untilGone[0] = now.getDuration();
                    removedAt[0] = h.getLevel().getGameTime();
                }
                return;
            }
            long sinceRemoved = h.getLevel().getGameTime() - removedAt[0];
            if (!gone[0]) {
                if (now != null) {
                    h.assertTrue(!now.showIcon() && used == spent[0], "The helmet leaves the accessory's fading vision alone and unpaid: " + now + ", " + used + " JE");
                    return;
                }
                h.assertTrue(sinceRemoved == untilGone[0], "The accessory's vision ran its whole time: gone after " + sinceRemoved + " of " + untilGone[0] + " ticks");
                gone[0] = true;
                return;
            }
            h.assertTrue(helmetLook(now) && used == spent[0] + Exosuit.NIGHT_VISION_PER_TICK, "The helmet gives and pays for its own vision again: " + now + ", " + used + " JE");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 260)
    public void flatGogglesStopTheirOwnVisionAndRechargedGogglesResume(GameTestHelper h) {
        ServerPlayer p = inTheDark(h);
        long[] start = {-1};
        boolean[] recharged = {false};
        h.runAfterDelay(20, () -> {
            h.assertTrue(dark(p), "The sealed room is dark at the player's eyes");
            p.setItemSlot(EquipmentSlot.HEAD, goggles(PoweredNightVision.INDUSTRIAL_COST));
            start[0] = h.getLevel().getGameTime();
        });
        h.onEachTick(() -> {
            long k = start[0] < 0 ? 0 : h.getLevel().getGameTime() - start[0];
            if (k <= 0) {
                return;
            }
            p.doTick();
            if (!recharged[0]) {
                if (k <= PoweredNightVision.INDUSTRIAL_PAID_TICKS) {
                    h.assertTrue(helmetLook(vision(p)) && energy(p) == 0, "The one 200 JE payment gives 160 ticks of vision, at tick " + k);
                    return;
                }
                h.assertTrue(vision(p) == null && energy(p) == 0, "Flat goggles take their own vision away once the paid time is used, at tick " + k);
                Chargeable.setEnergy(p.getItemBySlot(EquipmentSlot.HEAD), PoweredNightVision.INDUSTRIAL_COST);
                recharged[0] = true;
                return;
            }
            h.assertTrue(helmetLook(vision(p)) && energy(p) == 0, "Recharged goggles resume their vision with the next payment");
            h.succeed();
        });
    }
}
