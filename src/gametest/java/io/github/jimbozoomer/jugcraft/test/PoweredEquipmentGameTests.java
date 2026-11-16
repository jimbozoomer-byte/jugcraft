package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.gear.*;
import io.github.jimbozoomer.jugcraft.tools.*;
import io.github.jimbozoomer.jugcraft.prospecting.*;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.*;

/** Behavior regressions for the owner-directed November 2 powered equipment rebalance. */
public class PoweredEquipmentGameTests {
    private static ItemStack suit(ArmorType type, long charge) {
        ItemStack stack = new ItemStack(JugcraftExosuit.piece(ExosuitItem.Style.VANGUARD, type));
        Chargeable.setEnergy(stack, charge); return stack;
    }
    private static ServerPlayer player(GameTestHelper h) {
        h.setBlock(new BlockPos(1,1,1), Blocks.STONE);
        ServerPlayer p = h.makeMockServerPlayerInLevel(); p.setGameMode(GameType.SURVIVAL);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 2, 1)))); return p;
    }
    @GameTest public void capacitiesDoubleWithoutAddingCharge(GameTestHelper h) {
        for (Item item : new Item[]{JugcraftTools.MINING_DRILL, JugcraftTools.CHAINSAW, JugcraftGear.ITEMS.get("power_katana"), JugcraftGear.ITEMS.get("power_bow")}) {
            ItemStack stack = new ItemStack(item); Chargeable.setEnergy(stack, 1234);
            h.assertTrue(Chargeable.capacity(stack) == 250_000, "Incorrect tool base capacity");
            for (int n = 1; n <= 2; n++) {
                h.assertTrue(ToolUpgrades.fit(h.getLevel(), stack, ToolUpgrades.Kind.CAPACITY) == ToolUpgrades.Result.FITTED, "Module refused");
                h.assertTrue(Chargeable.capacity(stack) == 250_000L << n, "Capacity did not double");
                h.assertTrue(Chargeable.energy(stack) == 1234, "Upgrade created energy");
            }
            h.assertTrue(ToolUpgrades.fit(h.getLevel(), stack, ToolUpgrades.Kind.CAPACITY) == ToolUpgrades.Result.FULL, "Third module allowed");
        }
        ItemStack chest = suit(ArmorType.CHESTPLATE, 0);
        ToolUpgrades.fit(h.getLevel(), chest, ToolUpgrades.Kind.CAPACITY); ToolUpgrades.fit(h.getLevel(), chest, ToolUpgrades.Kind.CAPACITY);
        h.assertTrue(Chargeable.capacity(chest) == 2_000_000, "Incorrect exosuit capacity"); h.succeed();
    }
    @GameTest public void legacyOverCapacityEnergyIsSpentWithoutLoss(GameTestHelper h) {
        ItemStack drill = new ItemStack(JugcraftTools.MINING_DRILL); drill.set(JugcraftTools.ENERGY, 600_000L);
        h.assertTrue(Chargeable.drain(drill, 60) && Chargeable.energy(drill) == 599_940, "Spending discarded old saved energy");
        h.assertTrue(Chargeable.charge(drill, 100) == 0, "Over-cap item gained charge");
        h.assertTrue(!Chargeable.drain(drill, -10), "Negative spending created energy"); h.succeed();
    }
    @GameTest public void removalPreservesEnergyAndRecoversOneLevel(GameTestHelper h) {
        ItemStack drill = new ItemStack(JugcraftTools.MINING_DRILL);
        ToolUpgrades.fit(h.getLevel(), drill, ToolUpgrades.Kind.CAPACITY); Chargeable.setEnergy(drill, 400_000);
        h.assertTrue(ToolUpgrades.remove(h.getLevel(), drill, ToolUpgrades.Kind.CAPACITY) == ToolUpgrades.Result.TOO_CHARGED, "Removed a full battery upgrade");
        Chargeable.drain(drill, 200_000);
        h.assertTrue(ToolUpgrades.remove(h.getLevel(), drill, ToolUpgrades.Kind.CAPACITY) == ToolUpgrades.Result.REMOVED, "Module not removable");
        h.assertTrue(Chargeable.capacity(drill) == 250_000 && Chargeable.energy(drill) == 200_000, "Removal changed energy"); h.succeed();
    }
    @GameTest public void idleLegsAndBootsSpendNothing(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack legs = suit(ArmorType.LEGGINGS, 12345), feet = suit(ArmorType.BOOTS, 12345);
        p.setItemSlot(EquipmentSlot.LEGS, legs); p.setItemSlot(EquipmentSlot.FEET, feet);
        for (int n = 0; n < 100; n++) Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(legs) == 12345 && Chargeable.energy(feet) == 12345, "Standby drain remains");
        h.assertTrue(p.getAttribute(Attributes.STEP_HEIGHT).getValue() >= 1, "Charged boots lost step assist");
        h.assertTrue(!p.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER).hasModifier(Exosuit.FALL), "Permanent immunity remains"); h.succeed();
    }
    @GameTest public void leggingsBillMovementButNotBlockedInput(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack legs = suit(ArmorType.LEGGINGS, 100); p.setItemSlot(EquipmentSlot.LEGS, legs);
        p.setLastClientInput(new Input(true,false,false,false,false,false,false)); Exosuit.tick(p); Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(legs) == 100, "Blocked input spent energy");
        p.setPos(p.getX() + .2, p.getY(), p.getZ()); Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(legs) == 99, "Moving did not cost exactly one JE"); h.succeed();
    }
    @GameTest public void fallsCostOneHundredPerHeartAndProtectPartially(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack feet = suit(ArmorType.BOOTS, 1000); p.setItemSlot(EquipmentSlot.FEET, feet);
        h.assertTrue(Exosuit.cushion(p, p.damageSources().fall(), 10) == 0 && Chargeable.energy(feet) == 500, "Five hearts must cost 500 JE");
        Chargeable.setEnergy(feet, 125);
        h.assertTrue(Exosuit.cushion(p, p.damageSources().fall(), 10) == 7.5F && Chargeable.energy(feet) == 0, "Partial protection incorrect");
        h.assertTrue(Exosuit.cushion(p, p.damageSources().fall(), 10) == 10, "Empty boots cushioned"); h.succeed();
    }
    @GameTest public void realFallDamageUsesTheLandingHook(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack feet = suit(ArmorType.BOOTS, 1000); p.setItemSlot(EquipmentSlot.FEET, feet);
        float health = p.getHealth(); p.hurtServer(h.getLevel(), p.damageSources().fall(), 6);
        h.assertTrue(p.getHealth() == health && Chargeable.energy(feet) == 700, "Real damage path: health="+p.getHealth()+", energy="+Chargeable.energy(feet)); h.succeed();
    }
    @GameTest public void harmlessFallsAndOtherDamageDoNotBillBoots(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack feet = suit(ArmorType.BOOTS, 1000); p.setItemSlot(EquipmentSlot.FEET, feet);
        Exosuit.cushion(p, p.damageSources().fall(), 0); Exosuit.cushion(p, p.damageSources().generic(), 4);
        h.assertTrue(Chargeable.energy(feet) == 1000, "Unrelated damage spent boot energy"); h.succeed();
    }
    @GameTest public void potionVisionIsNotBilledOrRemoved(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack head = suit(ArmorType.HELMET, 1000); p.setItemSlot(EquipmentSlot.HEAD, head);
        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200)); Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(head) == 1000 && p.hasEffect(MobEffects.NIGHT_VISION), "Potion vision was billed or removed");
        EquipmentSettings.setNumber(head, "night_vision", 0); Exosuit.tick(p);
        h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION), "Helmet toggle removed a potion"); h.succeed();
    }
    @GameTest public void distanceBandsAndCompassBearingsAreDistinct(GameTestHelper h) {
        int[] vague = OreScannerItem.distanceBand(37, false), precise = OreScannerItem.distanceBand(37, true);
        h.assertTrue(vague[0] == 32 && vague[1] == 64, "Standard reading is not vague");
        h.assertTrue(precise[0] == 32 && precise[1] == 40, "Advanced distance not in eight-block bands");
        h.assertTrue(OreScannerItem.bearing(10,0).equals("E") && OreScannerItem.bearing(0,-10).equals("N")
            && OreScannerItem.bearing(-10,10).equals("SW"), "Compass directions incorrect"); h.succeed();
    }
    @GameTest public void scannerChargeAndModeAreIndependent(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack stack = new ItemStack(IndustrialGear.ITEMS.get("jimta_advanced_ore_scanner"));
        Chargeable.setEnergy(stack, 10_000); p.setItemInHand(InteractionHand.MAIN_HAND, stack); p.setShiftKeyDown(true);
        stack.getItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(Chargeable.capacity(stack) == 250_000 && Chargeable.energy(stack) == 10_000, "Mode change spent energy or incorrect capacity");
        h.assertTrue(!EquipmentSettings.enabled(stack, "scanner_detail"), "Advanced wide mode did not activate"); h.succeed();
    }
    @GameTest(maxTicks=500) public void advancedSurveyReportsPaidDistanceDirectionAndSurvivesCopy(GameTestHelper h) {
        ServerPlayer p = player(h); ItemStack stack = new ItemStack(IndustrialGear.ITEMS.get("jimta_advanced_ore_scanner"));
        Chargeable.setEnergy(stack, 10_000); p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos target = p.blockPosition().offset(10,-4,0); h.getLevel().setBlockAndUpdate(target, Blocks.DIAMOND_ORE.defaultBlockState());
        stack.getItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(Chargeable.energy(stack)==10_000,"An unfinished scan was billed");
        h.succeedWhen(() -> {
            SurveyPayload result = SurveyPayload.saved(stack.copy(),h.getLevel().registryAccess());
            h.assertTrue(result != null,"Survey has not completed");
            var diamond = result.readings().stream().filter(r -> r.icon().getPath().equals("diamond_ore")).findFirst();
            h.assertTrue(diamond.isPresent(),"Detailed sampling missed the planted ore");
            h.assertTrue(diamond.get().direction().equals("E") && diamond.get().distanceHigh()-diamond.get().distanceLow()==8,"Advanced location is incorrect");
            h.assertTrue(Chargeable.energy(stack)==9_000,"Completed detailed scan did not cost exactly 1,000 JE");
            h.assertTrue(result.instrument()==2 && result.diameter()==3 && result.coverage()>0,"Coverage metadata lost");
        });
    }
    @GameTest(maxTicks=500) public void standardSurveyHasVagueDistanceWithoutBearing(GameTestHelper h) {
        ServerPlayer p=player(h); ItemStack stack=new ItemStack(IndustrialGear.ITEMS.get("jimta_ore_scanner"));
        Chargeable.setEnergy(stack,10_000);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        BlockPos target=p.blockPosition().offset(10,-4,0);target=new BlockPos(target.getX()&~1,target.getY(),target.getZ()&~1);
        h.getLevel().setBlockAndUpdate(target,Blocks.EMERALD_ORE.defaultBlockState());stack.getItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.succeedWhen(()->{
            SurveyPayload result=SurveyPayload.saved(stack,h.getLevel().registryAccess());h.assertTrue(result!=null,"Survey pending");
            var emerald=result.readings().stream().filter(r->r.icon().getPath().equals("emerald_ore")).findFirst();
            h.assertTrue(emerald.isPresent() && emerald.get().direction().isEmpty() && emerald.get().distanceHigh()-emerald.get().distanceLow()>=16,"Standard location is not vague");
            h.assertTrue(Chargeable.energy(stack)==9_500,"Completed standard scan did not cost 500 JE");
        });
    }
    @GameTest(maxTicks=30) public void interruptedScannerDoesNotSpendEnergy(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack stack=new ItemStack(IndustrialGear.ITEMS.get("jimta_ore_scanner"));
        Chargeable.setEnergy(stack,10_000);p.setItemInHand(InteractionHand.MAIN_HAND,stack);stack.getItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.runAtTickTime(10,()->{h.assertTrue(Chargeable.energy(stack)==10_000 && SurveyPayload.saved(stack,h.getLevel().registryAccess())==null,"Cancelled scan spent energy or produced a result");h.succeed();});
    }
    @GameTest(maxTicks=100) public void shieldSpendsOneThousandAndKeepsPaidProtectionWhenFlat(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack chest=suit(ArmorType.CHESTPLATE,1000);p.setItemSlot(EquipmentSlot.CHEST,chest);
        h.succeedWhen(()->{
            Exosuit.tick(p);h.assertTrue(Chargeable.energy(chest)==0,"Shield has not charged");
            h.assertTrue(p.getAbsorptionAmount()==1,"Last paid shield point vanished");
            Exosuit.tick(p);h.assertTrue(p.getAbsorptionAmount()==1,"Flat chestplate erased paid protection");
            p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);Exosuit.tick(p);h.assertTrue(p.getAbsorptionAmount()==0,"Unequipped shield remained on the player");
            p.setItemSlot(EquipmentSlot.CHEST,chest);Exosuit.tick(p);h.assertTrue(p.getAbsorptionAmount()==1 && Chargeable.energy(chest)==0,"Re-equipping created or lost a paid shield point");
        });
    }

    @GameTest(maxTicks=100) public void helmetBillsOnlyItsOwnActiveNightVision(GameTestHelper h) {
        for(int x=0;x<=4;x++)for(int y=0;y<=5;y++)for(int z=0;z<=4;z++)h.setBlock(new BlockPos(x,y,z),Blocks.STONE);
        for(int x=1;x<=3;x++)for(int y=1;y<=4;y++)for(int z=1;z<=3;z++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        ServerPlayer p=player(h);p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new BlockPos(2,2,2))));
        ItemStack head=suit(ArmorType.HELMET,1000);p.setItemSlot(EquipmentSlot.HEAD,head);
        h.runAtTickTime(20,()->{
            h.assertTrue(h.getLevel().getMaxLocalRawBrightness(BlockPos.containing(p.getEyePosition()))<Exosuit.DARK_BELOW,"Fixture is not dark");
            long before=Chargeable.energy(head);Exosuit.tick(p);
            h.assertTrue(Chargeable.energy(head)==before-2 && p.hasEffect(MobEffects.NIGHT_VISION),"Active helmet vision not billed at two JE");
            EquipmentSettings.setNumber(head,"night_vision",0);before=Chargeable.energy(head);Exosuit.tick(p);
            h.assertTrue(Chargeable.energy(head)==before&&!p.hasEffect(MobEffects.NIGHT_VISION),"Disabled helmet retained its effect or drain");h.succeed();
        });
    }

    @GameTest public void hoeAreaIsBoundedAndBillsOnlyChangedBlocks(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack hoe=new ItemStack(IndustrialGear.ITEMS.get("jimta_electric_hoe"));
        Chargeable.setEnergy(hoe,1000);EquipmentSettings.setNumber(hoe,"hoe_area",Integer.MAX_VALUE);p.setItemInHand(InteractionHand.MAIN_HAND,hoe);
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)h.setBlock(new BlockPos(x,1,z),Blocks.DIRT);
        h.setBlock(new BlockPos(1,1,1),Blocks.STONE);
        BlockPos center=h.absolutePos(new BlockPos(2,1,2));
        var context=new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(center),net.minecraft.core.Direction.UP,center,false));
        hoe.getItem().useOn(context);
        h.assertTrue(Chargeable.energy(hoe)==600,"Area tilling did not charge exactly eight successful blocks");
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)if(x!=1||z!=1)h.assertBlockPresent(Blocks.FARMLAND,new BlockPos(x,1,z));
        hoe.getItem().useOn(context);h.assertTrue(Chargeable.energy(hoe)==600,"Unchanged farmland was billed");h.succeed();
    }
    @GameTest public void batteryPriorityTransfersRatherThanCreatesEnergy(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack battery=new ItemStack(IndustrialGear.ITEMS.get("jimta_re_battery")), drill=new ItemStack(JugcraftTools.MINING_DRILL), legs=suit(ArmorType.LEGGINGS,0);
        p.setItemInHand(InteractionHand.MAIN_HAND,drill);p.setItemInHand(InteractionHand.OFF_HAND,battery);p.setItemSlot(EquipmentSlot.LEGS,legs);
        Chargeable.setEnergy(battery,10_000);battery.getItem().use(h.getLevel(),p,InteractionHand.OFF_HAND);
        h.assertTrue(Chargeable.energy(drill)==10_000&&Chargeable.energy(legs)==0&&Chargeable.energy(battery)==0,"Held-tool priority or energy conservation failed");
        Chargeable.setEnergy(battery,10_000);EquipmentSettings.setNumber(battery,"charging_priority",1);battery.getItem().use(h.getLevel(),p,InteractionHand.OFF_HAND);
        h.assertTrue(Chargeable.energy(legs)==10_000&&Chargeable.energy(drill)==10_000&&Chargeable.energy(battery)==0,"Armor-first priority failed");h.succeed();
    }
    @GameTest public void blowerStrengthScalesItsActiveEnergyCost(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack blower=new ItemStack(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.item("leaf_blower"));
        p.setItemInHand(InteractionHand.MAIN_HAND,blower);Chargeable.setEnergy(blower,100);EquipmentSettings.setNumber(blower,"blower_strength",0);
        blower.getItem().onUseTick(h.getLevel(),p,blower,blower.getUseDuration(p)-1);
        h.assertTrue(Chargeable.energy(blower)==98,"Gentle blowing did not cost two JE");
        EquipmentSettings.setNumber(blower,"blower_strength",2);blower.getItem().onUseTick(h.getLevel(),p,blower,blower.getUseDuration(p)-2);
        h.assertTrue(Chargeable.energy(blower)==94,"Full blowing did not cost four JE");
        EquipmentSettings.setNumber(blower,"blower_strength",0);Chargeable.setEnergy(blower,2);
        blower.getItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(p.isUsingItem()&&Chargeable.energy(blower)==2,"Gentle mode refused its final two JE or charged before use");
        blower.getItem().onUseTick(h.getLevel(),p,blower,blower.getUseDuration(p)-1);p.stopUsingItem();
        h.assertTrue(Chargeable.energy(blower)==0,"Final gentle tick cost changed");
        Chargeable.setEnergy(blower,3);p.setShiftKeyDown(true);blower.getItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(p.isUsingItem(),"Gentle vacuum refused its final three JE");
        blower.getItem().onUseTick(h.getLevel(),p,blower,blower.getUseDuration(p)-1);p.stopUsingItem();
        h.assertTrue(Chargeable.energy(blower)==0,"Gentle vacuum cost changed");h.succeed();
    }

    @GameTest public void scannerCraftUpgradePreservesChargeModulesAndSettings(GameTestHelper h) {
        ItemStack scanner=new ItemStack(IndustrialGear.ITEMS.get("jimta_ore_scanner"));Chargeable.setEnergy(scanner,12345);
        ToolUpgrades.fit(h.getLevel(),scanner,ToolUpgrades.Kind.CAPACITY);EquipmentSettings.setNumber(scanner,"test_setting",77);
        // The battery deliberately comes first: its components must never replace the scanner's.
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,1,java.util.List.of(
            new ItemStack(IndustrialGear.ITEMS.get("jimta_energy_crystal")),
            new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("advanced_circuit"))),scanner));
        var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(recipe.isPresent(),"Advanced scanner recipe no longer matches");
        ItemStack result=recipe.orElseThrow().value().assemble(input);
        h.assertTrue(result.getItem()==IndustrialGear.ITEMS.get("jimta_advanced_ore_scanner")&&Chargeable.energy(result)==12345
            && ToolUpgrades.level(result,ToolUpgrades.Kind.CAPACITY)==1 && EquipmentSettings.number(result,"test_setting",0)==77,"Upgrade discarded scanner components");
        h.assertTrue(Chargeable.energy(scanner)==12345&&scanner.getCount()==1,"Craft preview consumed its input");h.succeed();
    }

    @GameTest public void adjustableThrustKeepsTheRequestedEnergyPrice(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack pack=new ItemStack(JugcraftTools.ROCKET_PACK);Chargeable.setEnergy(pack,100);p.setItemSlot(EquipmentSlot.CHEST,pack);
        Jetpack.cycleThrust(p,pack);h.assertTrue(Jetpack.thrustScale(pack)==.75,"Gentle thrust not selected");
        p.fallDistance=10;RocketPackItem.thrust(p);
        h.assertTrue(Chargeable.energy(pack)==80&&p.fallDistance==0,"Gentle thrust changed the fixed twenty-JE price or landing safety");h.succeed();
    }
    @GameTest public void laserPreviewUsesTheAuthoritativeGeometry(GameTestHelper h) {
        BlockPos root=new BlockPos(1,2,3);
        var plane=MiningLaserPattern.targets(root,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.NORTH,5);
        var cube=MiningLaserPattern.targets(root,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.NORTH,6);
        h.assertTrue(plane.size()==9&&plane.stream().allMatch(pos->pos.getZ()==3)&&cube.size()==27,"Laser excavation shapes changed");
        h.assertTrue(MiningLaserPattern.targets(root,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST,2).getLast().equals(root.east(15)),"Long-range line exceeds or misses its sixteen-block bound");h.succeed();
    }

    @GameTest(maxTicks=160) public void expiringAbsorptionDoesNotErasePaidShieldPoints(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack chest=suit(ArmorType.CHESTPLATE,8000);p.setItemSlot(EquipmentSlot.CHEST,chest);
        h.succeedWhen(()->{
            Exosuit.tick(p);h.assertTrue(Chargeable.energy(chest)==0,"Shield is still charging");
            h.assertTrue(p.getAbsorptionAmount()==8,"Shield did not reach eight points");
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,100,2));Exosuit.tick(p);
            p.removeEffect(MobEffects.ABSORPTION);Exosuit.tick(p);
            h.assertTrue(EquipmentSettings.number(chest,"shield",0)==8000&&p.getAbsorptionAmount()==8,"Potion expiry erased shield points that were already paid for");
        });
    }

    @GameTest public void finalMovementChargeRemovesTheBoostImmediately(GameTestHelper h) {
        ServerPlayer p=player(h);ItemStack legs=suit(ArmorType.LEGGINGS,1);p.setItemSlot(EquipmentSlot.LEGS,legs);
        p.setLastClientInput(new Input(true,false,false,false,false,false,false));Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(legs)==1&&p.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(Exosuit.SPEED),"Ready but idle leggings were billed");
        p.setPos(p.getX()+.2,p.getY(),p.getZ());Exosuit.tick(p);
        h.assertTrue(Chargeable.energy(legs)==0&&!p.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(Exosuit.SPEED),"Empty leggings retained a free speed tick");h.succeed();
    }

}
