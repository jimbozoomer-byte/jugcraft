package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.fluid.ElectricPumpBlockEntity;
import io.github.jimbozoomer.jugcraft.fluid.FluidTankBlockEntity;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.machine.EnlargedMachineBlock;
import io.github.jimbozoomer.jugcraft.machine.Footprint;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.LargeMachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The hydroponic bay's fluid supply (owner bug report, 8 November 2026: "will not accept water from an electric pump and
 * pipes"). The bay runs on nutrient solution by design (docs/features/hydroponics.md), so these check the real supply
 * paths into the full 3x2x3 machine a player builds, not the one-block compact copy the older test uses:
 * <ul>
 * <li>an electric pump on a tank of nutrient solution fills it through bronze pipes that end on a dummy part;</li>
 * <li>pumped water is refused and stays in the pump, none of it lost;</li>
 * <li>a nearly full bay takes only what fits, and the rest stays in the pump;</li>
 * <li>every outward face of every part reaches the one tank, takes nutrient solution and refuses water;</li>
 * <li>a bucket on a dummy part pours nutrient solution in; a water bucket, or one that does not fit, is refused with the
 * bucket kept, the screen left shut and nothing spilled;</li>
 * <li>every one-fluid machine tank takes the fluid its screen names ({@link MachineKind#tankFluid()}).</li>
 * </ul>
 * Amounts are compared in whole millibuckets: every move here is a multiple of {@link #MB} droplets.
 */
public class HydroponicBayFluidGameTests {
	private static final long MB = FluidConstants.BUCKET / 1000;
	/** The bay's master; north-facing, it fills x 2-4, y 1-2, z 2-4. */
	private static final BlockPos MASTER = new BlockPos(4, 1, 2);
	/** Where the pipes end: dummy part 12, the middle of the bay's top right row. */
	private static final BlockPos ENTRY = new BlockPos(4, 2, 3);

	private static Fluid nutrient() {
		return PetroFluids.NUTRIENT_SOLUTION.source();
	}

	/** A full bay (not the compact pre-batch-44 copy), built as placing its item does. */
	private static MachineBlockEntity bay(GameTestHelper helper, BlockPos master, Direction facing) {
		EnlargedMachineBlock block = (EnlargedMachineBlock) JugcraftMachines.MACHINES.get(MachineKind.HYDROPONIC_BAY);
		helper.setBlock(master, block.formed(block.defaultBlockState().setValue(MachineBlock.FACING, facing)));
		block.setPlacedBy(helper.getLevel(), helper.absolutePos(master), helper.getBlockState(master), null, ItemStack.EMPTY);
		Footprint footprint = block.footprint(helper.getBlockState(master));
		helper.assertTrue(footprint.size() == 18, "The bay should fill 18 blocks, not " + footprint.size());
		for (int part = 0; part < footprint.size(); part++) {
			BlockPos at = footprint.partPos(helper.absolutePos(master), facing, part);
			helper.assertTrue(helper.getLevel().getBlockState(at).is(block), "Bay part " + part + " is missing at " + at);
		}
		return helper.getBlockEntity(master, MachineBlockEntity.class);
	}

	/** Puts {@code mb} of {@code fluid} into a storage, all of it or fails the test. */
	private static void fill(GameTestHelper helper, Storage<FluidVariant> storage, Fluid fluid, long mb, String what) {
		try (Transaction transaction = Transaction.openOuter()) {
			long put = storage.insert(FluidVariant.of(fluid), mb * MB, transaction);
			helper.assertTrue(put == mb * MB, what + " took " + put / MB + " mB, not " + mb);
			transaction.commit();
		}
	}

	/**
	 * The shared rig: the bay; a tinplate tank of {@code mb} of {@code fluid} at (6, 1, 3) under a charged electric pump
	 * at (6, 2, 3); and three bronze pipes from the pump's top over to the bay's top at {@link #ENTRY}. The pump and the
	 * tank touch nothing else, so everything the bay gets comes through the pipes. {@code prefillMb} of nutrient solution
	 * is put in the bay first, through its far back side (a different dummy part).
	 */
	private record Rig(MachineBlockEntity bay, FluidTankBlockEntity source, ElectricPumpBlockEntity pump) {
		long totalMb() {
			return bay.tankAmount() + source.storage.amount / MB + pump.bufferedMb();
		}
	}

	private static Rig rig(GameTestHelper helper, Fluid fluid, long mb, int prefillMb) {
		MachineBlockEntity bay = bay(helper, MASTER, Direction.NORTH);
		int entryPart = helper.getBlockState(ENTRY).getValue(LargeMachineBlock.PART);
		helper.assertTrue(entryPart == 12, "The pipes should end on dummy part 12, not part " + entryPart);
		if (prefillMb > 0) {
			fill(helper, FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 4)), Direction.WEST),
					nutrient(), prefillMb, "The bay's far side");
		}
		for (BlockPos pipe : new BlockPos[] {new BlockPos(6, 3, 3), new BlockPos(5, 3, 3), new BlockPos(4, 3, 3)}) {
			helper.setBlock(pipe, JugcraftFluids.BRONZE_FLUID_PIPE);
		}
		BlockPos tankPos = new BlockPos(6, 1, 3);
		helper.setBlock(tankPos, JugcraftFluids.FLUID_TANK);
		FluidTankBlockEntity source = helper.getBlockEntity(tankPos, FluidTankBlockEntity.class);
		fill(helper, source.storage, fluid, mb, "The source tank");
		BlockPos pumpPos = new BlockPos(6, 2, 3);
		helper.setBlock(pumpPos, JugcraftFluids.ELECTRIC_PUMP);
		ElectricPumpBlockEntity pump = helper.getBlockEntity(pumpPos, ElectricPumpBlockEntity.class);
		pump.energy().setAmount(ElectricPumpBlockEntity.ENERGY_CAPACITY);
		return new Rig(bay, source, pump);
	}

	/** Pump -> three pipes -> a dummy part's top: all 2,000 mB of nutrient solution reaches the bay's tank, none lost. */
	@GameTest(maxTicks = 300)
	public void pumpPipesNutrientSolutionIntoTheBay(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		Rig rig = rig(helper, nutrient(), 2_000, 0);
		helper.succeedWhen(() -> {
			helper.assertTrue(rig.totalMb() == 2_000, "Nutrient solution was lost or made: " + rig.totalMb() + " mB in all");
			helper.assertTrue(rig.bay().tankAmount() == 2_000, "The bay holds " + rig.bay().tankAmount() + " of 2,000 mB");
			helper.assertTrue(rig.source().storage.amount == 0 && rig.pump().bufferedMb() == 0, "Left behind: "
					+ rig.source().storage.amount / MB + " mB in the tank, " + rig.pump().bufferedMb() + " mB in the pump");
		});
	}

	/** The same rig pumping water: the bay takes none, and all 2,000 mB is still in the pump (none lost in the pipes). */
	@GameTest(maxTicks = 300)
	public void pumpedWaterIsRefusedAndKept(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		Rig rig = rig(helper, Fluids.WATER, 2_000, 0);
		Storage<FluidVariant> entry = FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(ENTRY), Direction.UP);
		helper.assertTrue(entry != null && entry.supportsInsertion(), "The bay's top face offers no inlet to the pipe");
		// The pump empties the tank in about 20 ticks; the pipes then offer the water to the bay every tick after that.
		helper.runAfterDelay(120, () -> {
			helper.assertTrue(rig.bay().tankAmount() == 0, "The bay took " + rig.bay().tankAmount() + " mB of plain water");
			helper.assertTrue(rig.source().storage.amount == 0, "The pump did not draw the water up");
			helper.assertTrue(rig.pump().bufferedMb() == 2_000, "The pump holds " + rig.pump().bufferedMb() + " of 2,000 mB of water");
			FluidVariant held = StorageUtil.findStoredResource(rig.pump().fluidFor(Direction.UP));
			helper.assertTrue(held != null && held.isOf(Fluids.WATER), "The pump holds " + held + ", not water");
			helper.succeed();
		});
	}

	/** A bay 50 mB short of full takes exactly 50 mB through the pipes; the other 950 mB stays in the pump. */
	@GameTest(maxTicks = 300)
	public void bayFillsToCapacityAndTheRestStaysInThePump(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		int capacity = MachineKind.HYDROPONIC_TANK;
		Rig rig = rig(helper, nutrient(), 1_000, capacity - 50);
		helper.succeedWhen(() -> {
			helper.assertTrue(rig.totalMb() == capacity + 950, "Nutrient solution was lost or made: " + rig.totalMb() + " mB in all");
			helper.assertTrue(rig.bay().tankAmount() == capacity, "The bay holds " + rig.bay().tankAmount() + " of " + capacity + " mB");
			helper.assertTrue(rig.source().storage.amount == 0 && rig.pump().bufferedMb() == 950,
					"The pump holds " + rig.pump().bufferedMb() + " mB, not the 950 mB that did not fit");
		});
	}

	/**
	 * An east-facing bay (so the parts are turned): each of its 42 outward faces finds a fluid inlet that refuses water
	 * and takes nutrient solution into the master's one tank, 1 mB a face.
	 */
	@GameTest(maxTicks = 40)
	public void everyBayFaceFeedsTheOneTank(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		BlockPos master = new BlockPos(4, 1, 4);
		MachineBlockEntity bay = bay(helper, master, Direction.EAST);
		Footprint footprint = MachineKind.HYDROPONIC_BAY.footprint();
		Set<BlockPos> parts = new HashSet<>();
		for (int part = 0; part < footprint.size(); part++) {
			parts.add(footprint.partPos(helper.absolutePos(master), Direction.EAST, part));
		}
		int faces = 0;
		for (BlockPos part : parts) {
			for (Direction face : Direction.values()) {
				if (parts.contains(part.relative(face))) {
					continue;
				}
				faces++;
				Storage<FluidVariant> inlet = FluidStorage.SIDED.find(helper.getLevel(), part, face);
				helper.assertTrue(inlet != null, "No fluid inlet on the " + face + " face of " + part);
				try (Transaction transaction = Transaction.openOuter()) {
					long water = inlet.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
					helper.assertTrue(water == 0, "The " + face + " face of " + part + " took " + water / MB + " mB of water");
					long solution = inlet.insert(FluidVariant.of(nutrient()), MB, transaction);
					helper.assertTrue(solution == MB, "The " + face + " face of " + part + " refused nutrient solution");
					transaction.commit();
				}
				helper.assertTrue(bay.tankAmount() == faces, "After " + faces + " faces the tank holds " + bay.tankAmount() + " mB");
			}
		}
		helper.assertTrue(faces == 42, "A 3x2x3 bay has 42 outward faces, not " + faces);
		helper.succeed();
	}

	/** Uses the player's main-hand item on {@code relative}'s {@code face}, as a right-click does. */
	private static InteractionResult click(GameTestHelper helper, ServerPlayer player, BlockPos relative, Direction face) {
		BlockPos at = helper.absolutePos(relative);
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(at).relative(face, 0.5), face, at, false));
	}

	/**
	 * Buckets on dummy parts: a nutrient solution bucket pours 1,000 mB in and leaves an empty bucket; a water bucket is
	 * refused, kept and not spilled, and the screen stays shut; a nutrient solution bucket that would not fit is kept.
	 */
	@GameTest(maxTicks = 40)
	public void bucketsPourNutrientSolutionAndWaterIsRefused(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		MachineBlockEntity bay = bay(helper, MASTER, Direction.NORTH);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = helper.absolutePos(new BlockPos(1, 1, 3));
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);

		// The far back right block's outer (west) side.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PetroFluids.NUTRIENT_SOLUTION.bucket()));
		InteractionResult poured = click(helper, player, new BlockPos(2, 1, 4), Direction.WEST);
		helper.assertTrue(poured.consumesAction(), "Pouring nutrient solution gave " + poured);
		helper.assertTrue(bay.tankAmount() == 1_000, "The bay holds " + bay.tankAmount() + " mB after one bucket");
		helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "The hand holds " + player.getMainHandItem() + ", not an empty bucket");

		// The top of a front block: water is refused with the bucket kept, no screen and nothing spilled above it.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
		InteractionResult water = click(helper, player, new BlockPos(3, 2, 2), Direction.UP);
		helper.assertTrue(water.consumesAction(), "The refused water bucket fell through to " + water);
		helper.assertTrue(bay.tankAmount() == 1_000, "The bay holds " + bay.tankAmount() + " mB after a water bucket");
		helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "The water bucket became " + player.getMainHandItem());
		helper.assertTrue(player.containerMenu == player.inventoryMenu, "The bay's screen opened instead of refusing the water");
		helper.assertTrue(helper.getBlockState(new BlockPos(3, 3, 2)).isAir(), "Water was spilled on the bay");

		// Nearly full: 500 mB of room is not a bucket's worth, so the bucket is kept and the tank unchanged.
		fill(helper, bay.fluidFor(Direction.UP), nutrient(), 6_500, "The bay");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PetroFluids.NUTRIENT_SOLUTION.bucket()));
		InteractionResult full = click(helper, player, new BlockPos(2, 2, 3), Direction.WEST);
		helper.assertTrue(full.consumesAction(), "The bucket that did not fit fell through to " + full);
		helper.assertTrue(bay.tankAmount() == 7_500, "The bay holds " + bay.tankAmount() + " mB, not 7,500");
		helper.assertTrue(player.getMainHandItem().is(PetroFluids.NUTRIENT_SOLUTION.bucket()),
				"The bucket that did not fit became " + player.getMainHandItem());
		helper.succeed();
	}

	/** Every one-fluid machine tank takes the fluid its screen names and shows, through its default (compact) block. */
	@GameTest(maxTicks = 20)
	public void oneFluidTanksTakeTheFluidTheirScreenNames(GameTestHelper helper) {
		if (TestFocus.skip("machines")) {
			helper.succeed();
			return;
		}
		int x = 0;
		for (MachineKind kind : MachineKind.values()) {
			if (kind.tankCapacity() <= 0) {
				continue;
			}
			Fluid wanted = kind.tankFluid();
			helper.assertTrue(wanted != null, kind + " has a tank but names no fluid for it");
			BlockPos at = new BlockPos(++x, 1, 6);
			helper.setBlock(at, JugcraftMachines.MACHINES.get(kind).defaultBlockState());
			Storage<FluidVariant> inlet = FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(at), Direction.UP);
			helper.assertTrue(inlet != null, kind + " has no fluid inlet");
			fill(helper, inlet, wanted, 100, kind.toString());
			MachineBlockEntity machine = helper.getBlockEntity(at, MachineBlockEntity.class);
			helper.assertTrue(machine.tankAmount() == 100, kind + "'s tank holds " + machine.tankAmount() + " mB, not 100");
		}
		helper.assertTrue(x >= 6, "Expected at least six one-fluid machine tanks, found " + x);
		helper.succeed();
	}
}
