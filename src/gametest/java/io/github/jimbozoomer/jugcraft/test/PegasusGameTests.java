package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pegasus.JugcraftPegasus;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight.State;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusInputPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Server game tests for the Pegasus (docs/features/pegasus.md): the pilot's bounded input and who may send it, the
 * server's flight states (takeoff, climb, level flight, descent and landing), a ceiling and the build limit, landing and
 * a mid-air dismount without fall damage, stale input, the plausibility check, saving and reloading, Pegasus-only breeding and
 * registration. The flight arithmetic itself is tested without a game by PegasusFlightTests (src/test).
 *
 * <p>In the game the pilot's client moves a ridden pegasus and the server checks the moves; a mock player has no
 * client, so these tests stand in for it, calling the same step ({@link Pegasus#flyTravel}) on the server's copy each
 * tick, as vanilla applies a client's vehicle move. Written for the repository's suite; not run in this change.
 */
public class PegasusGameTests {
	// Flight needs an open sky; the default empty fixture has a barrier roof.

	private static int sequence;
	/** A grounded step of the stand-in client: enough to settle onto the floor and report it, small enough to read. */
	private static final double GROUND_STEP = 0.1;

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt) {
		ServerPlayer player = TestPlayers.survivor(helper);
		BlockPos at = helper.absolutePos(standAt);
		player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		helper.getLevel().getChunkSource().move(player);
		return player;
	}

	private static void remove(List<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			player.stopRiding();
			player.level().getServer().getPlayerList().remove(player);
			player.discard();
		}
	}

	private static void floor(GameTestHelper helper, int size) {
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** A tamed, saddled Pegasus at a relative position, facing south. */
	private static Pegasus pegasus(GameTestHelper helper, Vec3 relative, ServerPlayer owner, boolean saddled) {
		Pegasus pegasus = JugcraftPegasus.PEGASUS.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(relative);
		pegasus.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		pegasus.tameWithName(owner);
		if (saddled) {
			pegasus.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
		}
		helper.getLevel().addFreshEntity(pegasus);
		return pegasus;
	}

	private static Pegasus.InputResult send(ServerPlayer pilot, Pegasus pegasus, int input) {
		return JugcraftPegasus.receive(pilot, new PegasusInputPayload(pegasus.getId(), input, ++sequence));
	}

	private static String flightDiagnostic(Pegasus pegasus, ServerPlayer pilot) {
		return pegasus.flightState() + "; ticks=" + pegasus.tickCount + "; input=" + pegasus.activeInput()
				+ "; controller=" + (pegasus.getControllingPassenger() == pilot) + "; ground=" + pegasus.onGround()
				+ "; horseClear=" + pegasus.level().noCollision(pegasus, pegasus.getBoundingBox().move(0, 0.1, 0))
				+ "; riderClear=" + pegasus.level().noCollision(pilot, pilot.getBoundingBox().move(0, 0.1, 0))
				+ "; position=" + pegasus.position() + "; removed=" + pegasus.isRemoved();
	}

	/**
	 * What the pilot's client does each tick: sends its keys, and moves the pegasus one step: while the synced state
	 * flies, a flight step (velocity carried over in {@code velocity}, as the client keeps it; the server zeroes a ridden
	 * horse's own); otherwise a horse's fall under gravity, so the server's copy learns when it stands on the ground, as
	 * a client's vehicle moves tell it.
	 */
	private static void pilotTick(ServerPlayer pilot, Pegasus pegasus, boolean ascend, boolean descend, Vec3[] velocity) {
		send(pilot, pegasus, PegasusFlight.encode(ascend, descend));
		if (pegasus.flightActive() && pegasus.isVehicle()) {
			pegasus.setDeltaMovement(velocity[0]);
			pegasus.flyTravel(Vec3.ZERO, ascend, descend, pilot);
			velocity[0] = pegasus.getDeltaMovement();
		} else {
			velocity[0] = Vec3.ZERO;
			pegasus.move(MoverType.SELF, new Vec3(0.0, -GROUND_STEP, 0.0));
		}
	}

	/** Runs {@code step} on each of the next {@code ticks} ticks, starting at {@code from}. */
	private static void each(GameTestHelper helper, int from, int ticks, Runnable step) {
		for (int t = 0; t < ticks; t++) {
			// GameTestInfo keys scheduled callbacks by Runnable identity. Reusing step
			// overwrites every earlier tick and sends only the final input packet.
			helper.runAfterDelay(from + t, new Runnable() {
				@Override
				public void run() {
					step.run();
				}
			});
		}
	}

	/**
	 * Input carries two key bits and a sequence; the server takes it only from the pegasus's controlling passenger, for
	 * the pegasus it names, in order: a malformed value, a stale or replayed sequence, another entity's id, a player who
	 * is not riding, and a rider who does not control (no saddle) are all dropped and change nothing.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, padding = 16)
	public void flightInputIsBoundedAndOnlyThePilotsCounts(GameTestHelper helper) {
		floor(helper, 9);
		ServerPlayer pilot = player(helper, new BlockPos(2, 2, 2));
		ServerPlayer stranger = player(helper, new BlockPos(6, 2, 6));
		ServerPlayer passenger = player(helper, new BlockPos(6, 2, 2));
		Pegasus pegasus = pegasus(helper, new Vec3(2.5, 2.0, 2.5), pilot, true);
		Pegasus unsaddled = pegasus(helper, new Vec3(6.5, 2.0, 2.5), passenger, false);
		pilot.startRiding(pegasus, true, false);
		passenger.startRiding(unsaddled, true, false);
		helper.assertTrue(pegasus.getControllingPassenger() == pilot, "The saddled rider controls the pegasus");
		helper.assertTrue(unsaddled.getControllingPassenger() == null && passenger.getVehicle() == unsaddled,
				"An unsaddled pegasus's rider is a passenger, not a pilot");

		helper.assertTrue(send(pilot, pegasus, PegasusFlight.INPUT_ASCEND) == Pegasus.InputResult.ACCEPTED
				&& pegasus.activeInput() == PegasusFlight.INPUT_ASCEND, "The pilot's Ascend is accepted");
		int last = sequence;
		for (int bad : new int[] {4, 8, 0x7F, -1, Integer.MAX_VALUE}) {
			helper.assertTrue(send(pilot, pegasus, bad) == Pegasus.InputResult.MALFORMED, "Out-of-range input is dropped: " + bad);
		}
		helper.assertTrue(pegasus.activeInput() == PegasusFlight.INPUT_ASCEND, "Dropped input changes nothing");
		helper.assertTrue(JugcraftPegasus.receive(pilot, new PegasusInputPayload(pegasus.getId(), PegasusFlight.INPUT_DESCEND, last))
				== Pegasus.InputResult.STALE, "A replayed sequence is dropped");
		helper.assertTrue(JugcraftPegasus.receive(pilot, new PegasusInputPayload(pegasus.getId(), PegasusFlight.INPUT_DESCEND, last - 5))
				== Pegasus.InputResult.STALE, "An older sequence is dropped");
		helper.assertTrue(JugcraftPegasus.receive(pilot, new PegasusInputPayload(unsaddled.getId(), PegasusFlight.INPUT_DESCEND, ++sequence))
				== Pegasus.InputResult.WRONG_VEHICLE, "Input naming another entity is dropped");
		helper.assertTrue(send(stranger, pegasus, PegasusFlight.INPUT_DESCEND) == Pegasus.InputResult.NOT_RIDING,
				"A player who is not riding is ignored");
		helper.assertTrue(send(passenger, unsaddled, PegasusFlight.INPUT_ASCEND) == Pegasus.InputResult.NOT_CONTROLLER
				&& unsaddled.activeInput() == 0, "A rider who does not control is ignored");
		helper.assertTrue(pegasus.activeInput() == PegasusFlight.INPUT_ASCEND, "The pilot's input survived every bad packet");

		var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
		PegasusInputPayload payload = new PegasusInputPayload(pegasus.getId(), PegasusFlight.INPUT_DESCEND, 123456);
		PegasusInputPayload.CODEC.encode(buffer, payload);
		helper.assertTrue(PegasusInputPayload.CODEC.decode(buffer).equals(payload) && buffer.readableBytes() == 0,
				"The payload round-trips through its codec");
		remove(List.of(pilot, stranger, passenger));
		pegasus.discard();
		unsaddled.discard();
		helper.succeed();
	}

	/**
	 * Holding Ascend a moment is still a horse; held a second it takes off, unfolds and climbs; released, it holds level;
	 * Descend brings it down slowly near the ground and it lands, gravity back on, with neither it nor its rider hurt.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 420, padding = 16)
	public void takeoffClimbLevelFlightAndALandingWithoutFallDamage(GameTestHelper helper) {
		floor(helper, 9);
		ServerPlayer pilot = player(helper, new BlockPos(4, 2, 4));
		Pegasus pegasus = pegasus(helper, new Vec3(4.5, 2.0, 4.5), pilot, true);
		pilot.startRiding(pegasus, true, false);
		float riderHealth = pilot.getHealth();
		double ground = pegasus.getY();
		Vec3[] velocity = {Vec3.ZERO};
		double[] level = new double[2];
		// A short hold is a jump, not a takeoff.
		each(helper, 1, 10, () -> pilotTick(pilot, pegasus, true, false, velocity));
		each(helper, 11, 3, () -> pilotTick(pilot, pegasus, false, false, velocity));
		helper.runAfterDelay(14, () -> helper.assertTrue(pegasus.flightState() == State.GROUNDED && !pegasus.isNoGravity(),
				"Half a second of Ascend on the ground leaves a horse"));
		// A second's hold takes off and climbs.
		each(helper, 15, 60, () -> pilotTick(pilot, pegasus, true, false, velocity));
		helper.runAfterDelay(15 + PegasusFlight.TAKEOFF_HOLD_TICKS + 2, () -> helper.assertTrue(pegasus.flightState() == State.TAKEOFF
				&& pegasus.isNoGravity() && pegasus.flightVertical() == 1, "Held a second, it takes off: " + flightDiagnostic(pegasus, pilot)));
		helper.runAfterDelay(75, () -> helper.assertTrue(pegasus.flightState() == State.FLYING && pegasus.getY() > ground + 4.0,
				"It climbs: " + pegasus.flightState() + " at " + (pegasus.getY() - ground)));
		// Released: level flight.
		each(helper, 76, 40, () -> pilotTick(pilot, pegasus, false, false, velocity));
		helper.runAfterDelay(95, () -> level[0] = pegasus.getY());
		helper.runAfterDelay(115, () -> {
			level[1] = pegasus.getY();
			helper.assertTrue(Math.abs(level[1] - level[0]) < 0.2 && pegasus.flightState() == State.FLYING && pegasus.flightVertical() == 0,
					"Releasing both keys holds level flight: " + (level[1] - level[0]));
		});
		// Descend and land.
		each(helper, 116, 280, () -> {
			if (pegasus.flightActive()) {
				pilotTick(pilot, pegasus, false, true, velocity);
			}
		});
		helper.runAfterDelay(120, () -> helper.assertTrue(pegasus.flightVertical() == -1, "Descend is synced as a descent"));
		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getTick() > 120, "Takeoff, climb, level flight and descent must run before accepting a landing");
			helper.assertTrue(pegasus.flightState() == State.GROUNDED && pegasus.onGround(), "It lands");
			helper.assertTrue(!pegasus.isNoGravity(), "Landed, gravity is back");
			helper.assertTrue(pegasus.getHealth() == pegasus.getMaxHealth(), "A routine landing does not hurt the pegasus");
			helper.assertTrue(pilot.getHealth() == riderHealth && pilot.getVehicle() == pegasus, "nor its rider, still mounted");
			remove(List.of(pilot));
			pegasus.discard();
		});
	}

	/** Climbing stops under a ceiling with the rider's head clear, and at the top of the build limit. */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 200, padding = 16)
	public void aCeilingAndTheBuildLimitStopTheClimb(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 9);
		for (int x = 0; x < 9; x++) {
			for (int z = 0; z < 9; z++) {
				helper.setBlock(new BlockPos(x, 8, z), Blocks.GLASS);
			}
		}
		double ceiling = helper.absolutePos(new BlockPos(0, 8, 0)).getY();
		ServerPlayer pilot = player(helper, new BlockPos(4, 2, 4));
		Pegasus pegasus = pegasus(helper, new Vec3(4.5, 2.0, 4.5), pilot, true);
		pilot.startRiding(pegasus, true, false);
		Vec3[] velocity = {Vec3.ZERO};
		each(helper, 1, 90, () -> pilotTick(pilot, pegasus, true, false, velocity));
		List<ServerPlayer> high = new ArrayList<>();
		Pegasus[] top = new Pegasus[1];
		Vec3[] climb = {Vec3.ZERO};
		helper.runAfterDelay(91, () -> {
			helper.assertTrue(pegasus.flightActive(), "It took off under the glass: " + flightDiagnostic(pegasus, pilot));
			helper.assertTrue(pilot.getBoundingBox().maxY <= ceiling + 1.0E-6 && pegasus.getBoundingBox().maxY <= ceiling + 1.0E-6,
					"The climb stops below the ceiling, the rider's head clear: " + (pilot.getBoundingBox().maxY - ceiling));
			helper.assertTrue(level.noCollision(pilot, pilot.getBoundingBox().deflate(0.01)), "The rider is not in a block");
			// Now near the top of the world: the climb stops at the build limit.
			ServerPlayer flyer = player(helper, new BlockPos(2, 2, 2));
			high.add(flyer);
			top[0] = pegasus(helper, new Vec3(2.5, 2.0, 2.5), flyer, true);
			flyer.startRiding(top[0], true, false);
			top[0].snapTo(top[0].getX(), level.getMaxY() - 4.0, top[0].getZ(), 0.0F, 0.0F);
		});
		// Preserve the 80 input ticks without inserting callbacks during the live scheduler iteration.
		each(helper, 92, 80, () -> pilotTick(high.getFirst(), top[0], true, false, climb));
		helper.runAfterDelay(173, () -> {
			double topY = Math.max(top[0].getBoundingBox().maxY, high.getFirst().getBoundingBox().maxY);
			helper.assertTrue(top[0].flightActive() && topY <= level.getMaxY() + 1.0 + 1.0E-6,
					"The climb stops at the build limit: " + (topY - level.getMaxY() - 1.0));
			helper.assertTrue(PegasusFlight.clampClimb(PegasusFlight.CLIMB, level.getMaxY() + 1.0 - topY) <= 1.0E-6,
					"with no room left to climb");
			remove(List.of(pilot));
			remove(high);
			pegasus.discard();
			top[0].discard();
			helper.succeed();
		});
	}

	/**
	 * A rider who gets off in the air ends the pilot's control: the pegasus glides, ignores the old rider's keys, comes
	 * down slowly by itself and lands unhurt. (The rider falls by vanilla's rules.)
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 400, padding = 16)
	public void aMidAirDismountLeavesASafeGlide(GameTestHelper helper) {
		floor(helper, 9);
		ServerPlayer pilot = player(helper, new BlockPos(4, 12, 4));
		Pegasus pegasus = pegasus(helper, new Vec3(4.5, 12.0, 4.5), pilot, true);
		pilot.startRiding(pegasus, true, false);
		Vec3[] velocity = {Vec3.ZERO};
		// Ascend held in the air takes off at once (a press during a jump or a fall).
		each(helper, 1, PegasusFlight.AIR_TAKEOFF_TICKS + PegasusFlight.TAKEOFF_TICKS + 6, () -> pilotTick(pilot, pegasus, true, false, velocity));
		int off = PegasusFlight.AIR_TAKEOFF_TICKS + PegasusFlight.TAKEOFF_TICKS + 8;
		helper.runAfterDelay(off, () -> {
			helper.assertTrue(pegasus.flightState() == State.FLYING, "Held in the air, Ascend took off: " + flightDiagnostic(pegasus, pilot));
			pilot.stopRiding();
			helper.assertTrue(pegasus.flightState() == State.GLIDING && pegasus.activeInput() == 0, "Dismounting glides and forgets the keys");
			helper.assertTrue(send(pilot, pegasus, PegasusFlight.INPUT_ASCEND) == Pegasus.InputResult.NOT_RIDING, "The old rider's keys are ignored");
		});
		helper.runAfterDelay(off + 10, () -> helper.assertTrue(pegasus.flightState() == State.GLIDING && pegasus.isNoGravity(),
				"Riderless, it keeps gliding"));
		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getTick() > off + 10, "Takeoff, dismount and the observed glide must run before accepting a landing");
			helper.assertTrue(pegasus.flightState() == State.GROUNDED && pegasus.onGround(), "The glide lands");
			helper.assertTrue(pegasus.getHealth() == pegasus.getMaxHealth(), "unhurt");
			remove(List.of(pilot));
			pegasus.discard();
		});
	}

	/** Input that stops arriving makes a glide; the pilot's next packet takes it back. */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 120, padding = 16)
	public void staleInputGlidesUntilThePilotIsHeardAgain(GameTestHelper helper) {
		floor(helper, 9);
		ServerPlayer pilot = player(helper, new BlockPos(4, 10, 4));
		Pegasus pegasus = pegasus(helper, new Vec3(4.5, 10.0, 4.5), pilot, true);
		pilot.startRiding(pegasus, true, false);
		Vec3[] velocity = {Vec3.ZERO};
		int flying = PegasusFlight.AIR_TAKEOFF_TICKS + PegasusFlight.TAKEOFF_TICKS + 4;
		each(helper, 1, flying, () -> pilotTick(pilot, pegasus, true, false, velocity));
		helper.runAfterDelay(flying + PegasusFlight.INPUT_TIMEOUT_TICKS + 3, () -> {
			helper.assertTrue(pegasus.flightState() == State.GLIDING && pegasus.activeInput() == 0, "Silence glides: " + pegasus.flightState());
			helper.assertTrue(send(pilot, pegasus, 0) == Pegasus.InputResult.ACCEPTED, "The pilot is heard again");
		});
		helper.runAfterDelay(flying + PegasusFlight.INPUT_TIMEOUT_TICKS + 5, () -> {
			helper.assertTrue(pegasus.flightState() == State.FLYING, "and flies on: " + pegasus.flightState());
			remove(List.of(pilot));
			pegasus.discard();
			helper.succeed();
		});
	}

	/** A flight faster than any pegasus climbs (as only a client ignoring the server could make it) throws the rider off. */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 120, padding = 16)
	public void anImpossibleClimbThrowsTheRiderOff(GameTestHelper helper) {
		ServerPlayer pilot = player(helper, new BlockPos(4, 6, 4));
		Pegasus pegasus = pegasus(helper, new Vec3(4.5, 6.0, 4.5), pilot, true);
		pilot.startRiding(pegasus, true, false);
		Vec3[] velocity = {Vec3.ZERO};
		int flying = PegasusFlight.AIR_TAKEOFF_TICKS + 2;
		each(helper, 1, 70, () -> send(pilot, pegasus, PegasusFlight.INPUT_ASCEND));
		for (int t = 0; t < 4; t++) {
			// Up 9 blocks a tick: under vanilla's per-move limit, far over a pegasus's climb.
			helper.runAfterDelay(flying + 4 + t, () -> pegasus.setPos(pegasus.getX(), pegasus.getY() + 9.0, pegasus.getZ()));
		}
		helper.succeedWhen(() -> {
			helper.assertTrue(!pilot.isPassenger() && pegasus.flightState() == State.GLIDING, "The rider is thrown and the pegasus glides");
			remove(List.of(pilot));
			pegasus.discard();
		});
	}

	/**
	 * Saved and loaded, a pegasus keeps its identity, health, owner, tameness and saddle; flight does not survive: in the
	 * air it comes back gliding (gravity off so it does not fall), on the ground grounded, and no input is carried over.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, maxTicks = 60, padding = 16)
	public void saveAndReloadKeepTheHorseAndResetFlight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper, 9);
		ServerPlayer owner = player(helper, new BlockPos(4, 9, 4));
		Pegasus flying = pegasus(helper, new Vec3(4.5, 9.0, 4.5), owner, true);
		Pegasus grounded = pegasus(helper, new Vec3(1.5, 2.0, 1.5), owner, true);
		owner.startRiding(flying, true, false);
		flying.setHealth(17.0F);
		Vec3[] velocity = {Vec3.ZERO};
		each(helper, 1, PegasusFlight.AIR_TAKEOFF_TICKS + 3, () -> pilotTick(owner, flying, true, false, velocity));
		helper.runAfterDelay(PegasusFlight.AIR_TAKEOFF_TICKS + 5, () -> {
			helper.assertTrue(flying.flightActive(), "It is in the air when saved: " + flightDiagnostic(flying, owner));
			for (Pegasus original : List.of(flying, grounded)) {
				TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
				original.saveWithoutId(saved);
				Pegasus copy = JugcraftPegasus.PEGASUS.create(level, EntitySpawnReason.LOAD);
				copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
				helper.assertTrue(copy.getUUID().equals(original.getUUID()), "Its identity is kept");
				helper.assertTrue(copy.getHealth() == original.getHealth(), "its health: " + copy.getHealth());
				helper.assertTrue(copy.isTamed() && copy.getOwnerReference() != null && copy.getOwnerReference().matches(owner), "its owner");
				helper.assertTrue(copy.getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE), "its saddle");
				helper.assertTrue(copy.activeInput() == 0, "no input survives a reload");
				if (original == flying) {
					helper.assertTrue(copy.getHealth() == 17.0F, "the health it had");
					helper.assertTrue(copy.flightState() == State.GLIDING && copy.isNoGravity(), "Saved in the air, it comes back gliding");
				} else {
					helper.assertTrue(copy.flightState() == State.GROUNDED && !copy.isNoGravity(), "Saved on the ground, it comes back a horse");
				}
				copy.discard();
			}
			remove(List.of(owner));
			flying.discard();
			grounded.discard();
			helper.succeed();
		});
	}

	/**
	 * A Pegasus pairs only with a Pegasus (docs/features/horse-breeds.md#crossbreeding): no foal with a vanilla horse,
	 * whichever of the two starts it; horse armour does not fit; the saddle does.
	 */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true, padding = 16)
	public void pegasusPairsOnlyWithPegasiAndArmourDoesNotFit(GameTestHelper helper) {
		floor(helper, 9);
		ServerPlayer owner = player(helper, new BlockPos(2, 2, 2));
		Pegasus pegasus = pegasus(helper, new Vec3(2.5, 2.0, 2.5), owner, false);
		AbstractHorse horse = EntityTypes.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		Vec3 at = helper.absoluteVec(new Vec3(5.5, 2.0, 5.5));
		horse.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		horse.tameWithName(owner);
		helper.getLevel().addFreshEntity(horse);
		horse.setInLove(owner);
		pegasus.setInLove(owner);
		helper.assertTrue(!pegasus.canMate(horse), "No pegasus pairs with a horse");
		helper.assertTrue(pegasus.getBreedOffspring(helper.getLevel(), horse) == null, "No foal with a horse");
		Pegasus other = pegasus(helper, new Vec3(7.5, 2.0, 2.5), owner, false);
		helper.assertTrue(pegasus.getBreedOffspring(helper.getLevel(), other) instanceof Pegasus, "Two pegasi have a pegasus foal");
		int horses = helper.getLevel().getEntitiesOfClass(AbstractHorse.class, new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16)).size();
		horse.spawnChildFromBreeding(helper.getLevel(), pegasus);
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(AbstractHorse.class, new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16)).size() == horses,
				"A horse that starts it gets no foal either");
		other.discard();
		helper.assertTrue(!pegasus.canUseSlot(EquipmentSlot.BODY) && pegasus.canUseSlot(EquipmentSlot.SADDLE), "Armour no, saddle yes");
		remove(List.of(owner));
		pegasus.discard();
		horse.discard();
		helper.succeed();
	}

	/** The entity, its egg and its attributes load; the egg makes a pegasus. */
	@GameTest(structure = "jugcraft-test:drone_tower", skyAccess = true)
	public void pegasusDataLoads(GameTestHelper helper) {
		helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getValue(Jugcraft.id("pegasus")) == JugcraftPegasus.PEGASUS, "jugcraft:pegasus is registered");
		helper.assertTrue(BuiltInRegistries.ITEM.getValue(Jugcraft.id("pegasus_spawn_egg")) == JugcraftPegasus.SPAWN_EGG, "Its egg is registered");
		helper.assertTrue(SpawnEggItem.getType(new ItemStack(JugcraftPegasus.SPAWN_EGG)) == JugcraftPegasus.PEGASUS, "The egg hatches a pegasus");
		Pegasus pegasus = JugcraftPegasus.PEGASUS.create(helper.getLevel(), EntitySpawnReason.COMMAND);
		helper.assertTrue(pegasus.isEquippableInSlot(new ItemStack(Items.SADDLE), EquipmentSlot.SADDLE),
				"The vanilla saddle's allowed-entity tag includes Pegasus");
		helper.assertTrue(pegasus.getAttributeValue(Attributes.MAX_HEALTH) == Pegasus.HEALTH && pegasus.flightState() == State.GROUNDED,
				"A new pegasus has its health and stands on the ground");
		pegasus.discard();
		helper.succeed();
	}
}
