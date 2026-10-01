package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.chemistry.OilReservoirs;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.fluid.FluidTankBlockEntity;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.prospecting.OreSurvey;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

/**
 * In-game tests for the Chemistry branch's oil line (docs/branches/CHEMISTRY.md). Like {@link JugcraftGameTests},
 * they run on a headless server during {@code ./gradlew build}.
 */
public class PetroGameTests {
	/** Crude oil is a real fluid: its bucket holds it and a tinplate tank stores it like any other fluid. */
	@GameTest
	public void crudeOilFillsTanks(GameTestHelper helper) {
		PetroFluids.Entry oil = PetroFluids.CRUDE_OIL;
		helper.assertTrue(oil.bucket() instanceof BucketItem bucket && bucket.getContent() == oil.source(),
				"The crude oil bucket does not hold crude oil");
		BlockPos tank = new BlockPos(1, 1, 1);
		helper.setBlock(tank, JugcraftFluids.FLUID_TANK);
		FluidTankBlockEntity entity = helper.getBlockEntity(tank, FluidTankBlockEntity.class);
		try (Transaction transaction = Transaction.openOuter()) {
			long inserted = entity.storage.insert(FluidVariant.of(oil.source()), FluidConstants.BUCKET, transaction);
			helper.assertTrue(inserted == FluidConstants.BUCKET, "The tank took " + inserted + " droplets of crude oil");
			transaction.commit();
		}
		helper.assertTrue(entity.storage.variant.isOf(oil.source()), "The tank holds " + entity.storage.variant);
		helper.succeed();
	}

	/** Crude oil never makes new source blocks: two sources with a gap between them leave the gap flowing. */
	@GameTest(maxTicks = 200)
	public void crudeOilMakesNoNewSources(GameTestHelper helper) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 3; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		helper.setBlock(new BlockPos(1, 2, 1), PetroFluids.CRUDE_OIL.block());
		helper.setBlock(new BlockPos(3, 2, 1), PetroFluids.CRUDE_OIL.block());
		helper.runAfterDelay(160, () -> {
			var gap = helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(2, 2, 1)));
			helper.assertTrue(gap.getType() == PetroFluids.CRUDE_OIL.flowing(), "The gap holds " + gap + ", not flowing oil");
			helper.succeed();
		});
	}

	/**
	 * Reservoirs are fixed by the seed: the same chunk always reads the same, roughly one chunk in twelve holds pumpable
	 * oil, and a reservoir runs dry for good. A pumpjack can't draw on shale.
	 */
	@GameTest
	public void oilReservoirsAreSeededAndFinite(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		int conventional = 0;
		ChunkPos found = null;
		// A patch of chunks far from every test structure, so taking oil here disturbs nothing else.
		for (int cx = 4000; cx < 4040; cx++) {
			for (int cz = 4000; cz < 4040; cz++) {
				ChunkPos chunk = new ChunkPos(cx, cz);
				OilReservoirs.Reservoir reservoir = OilReservoirs.get(level, chunk);
				helper.assertTrue(reservoir.equals(OilReservoirs.get(level, chunk)), "Chunk " + chunk + " reads differently twice");
				if (reservoir.kind() == OilReservoirs.Kind.CONVENTIONAL) {
					conventional++;
					found = found == null ? chunk : found;
				}
			}
		}
		// 1,600 chunks at 1 in 12 is about 133; allow a wide margin.
		helper.assertTrue(conventional > 60 && conventional < 260, conventional + " of 1,600 chunks hold pumpable oil");
		OilReservoirs.Reservoir before = OilReservoirs.get(level, found);
		helper.assertTrue(OilReservoirs.extract(level, found, OilReservoirs.Kind.SHALE, 1000) == 0, "Took shale oil from a conventional reservoir");
		int got = OilReservoirs.extract(level, found, OilReservoirs.Kind.CONVENTIONAL, (int) before.capacity() + 5000);
		helper.assertTrue(got == before.remaining(), "Took " + got + " mB from a reservoir with " + before.remaining());
		helper.assertTrue(OilReservoirs.get(level, found).isDry(), "The reservoir is not dry after taking everything");
		helper.assertTrue(OilReservoirs.extract(level, found, OilReservoirs.Kind.CONVENTIONAL, 1000) == 0, "A dry reservoir gave more oil");
		helper.succeed();
	}

	/** The prospector reports oil under the surveyed chunks. */
	@GameTest
	public void surveyFindsOil(GameTestHelper helper) {
		BlockPos center = helper.absolutePos(new BlockPos(2, 2, 2));
		OilReservoirs.overrideForTest(ChunkPos.containing(center), OilReservoirs.Kind.CONVENTIONAL, 120_000);
		List<OreSurvey.Reading> readings = OreSurvey.survey(helper.getLevel(), center, helper.getLevel().getRandom());
		helper.assertTrue(readings.stream().anyMatch(reading -> reading.label().equals("prospector.jugcraft.oil")),
				"No oil reading in " + readings);
		helper.succeed();
	}
}
