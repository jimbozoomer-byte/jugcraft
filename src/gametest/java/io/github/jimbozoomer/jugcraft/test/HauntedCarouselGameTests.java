package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedCarousel;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedCarouselBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedCarouselBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlock;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlockEntity;
import io.github.jimbozoomer.jugcraft.kinetic.JugcraftKinetics;
import io.github.jimbozoomer.jugcraft.kinetic.KineticNetworks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Game tests for the Haunted Carousel (Haunted Midway, part 5; docs/features/haunted-midway.md), in the 44 by 44 empty
 * arena the Ferris wheel's tests use, so it has room. Placed with room, its middle sets one carousel turning round itself,
 * facing the player, and where a block stands in its way it isn't placed. Its middle takes at most
 * {@value HauntedCarousel#NEED} KE a tick, and a hand crank under it brings it up to full speed, its lights on and its
 * middle giving light; when the crank stops, it slows to a stop and its lights go out. A player boards the free mount
 * nearest them, a second the coach beside it, and a rider rides up and down with their horse; getting off sets them down
 * on the ground outside their mount. A block where a mount is about to go jams it; cleared, it turns again. While it
 * turns its organ plays, a beat every {@value HauntedCarousel#BEAT_TICKS} ticks; sneaking, an empty hand quiets it.
 * Breaking its middle takes it down, its riders set on the ground, and the middle drops once. Run by CI's
 * {@code runGameTest}.
 */
public class HauntedCarouselGameTests {
	private static final String ARENA = "jugcraft-test:drone_tower";
	/** The middle's place in the arena. */
	private static final BlockPos CENTRE = new BlockPos(22, 1, 22);

	private static Block middle() {
		return JugcraftAgriculture.block(HauntedCarouselBlock.ID);
	}

	private static ServerPlayer player(GameTestHelper helper, Vec3 at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.getInventory().clearContent();
		Vec3 absolute = helper.absoluteVec(at);
		player.setPos(absolute.x, absolute.y, absolute.z);
		return player;
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 14; x <= 30; x++) {
			for (int z = 14; z <= 40; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	/** A middle facing north at the arena's middle, its carousel set turning round it; returns the carousel. */
	private static HauntedCarousel raised(GameTestHelper helper) {
		floor(helper);
		helper.setBlock(CENTRE, middle().defaultBlockState().setValue(HauntedCarouselBlock.FACING, Direction.NORTH));
		HauntedCarousel carousel = HauntedCarousel.raise(helper.getLevel(), helper.absolutePos(CENTRE));
		helper.assertTrue(carousel != null, "The carousel is set up");
		return carousel;
	}

	/** Uses an empty hand on the middle, sneaking or not. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute, boolean sneaking) {
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setShiftKeyDown(sneaking);
		player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
		player.setShiftKeyDown(false);
	}

	@GameTest(structure = ARENA, skyAccess = true)
	public void theCarouselStandsWhereThereIsRoom(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = player(helper, new Vec3(22.5, 1.0, 27.5));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(middle()));
		BlockPos ground = helper.absolutePos(CENTRE.below());
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(ground).add(0.0, 0.5, 0.0), Direction.UP, ground, false));
		BlockPos pos = ground.above();
		helper.assertTrue(level.getBlockState(pos).is(middle()), "Its middle is placed");
		Direction facing = level.getBlockState(pos).getValue(HauntedCarouselBlock.FACING);
		helper.assertTrue(facing == player.getDirection().getOpposite(), "facing whoever placed it: " + facing);
		helper.assertTrue(HauntedCarouselBlock.carousels(level, pos).size() == 1, "One carousel turns round it");
		helper.assertTrue(level.getBlockEntity(pos) instanceof HauntedCarouselBlockEntity, "Its drive is there");

		// A second with a block in its way can't be placed.
		BlockPos second = helper.absolutePos(new BlockPos(22, 0, 34));
		helper.setBlock(new BlockPos(24, 2, 34), Blocks.STONE);
		player.setPos(second.getX() + 0.5, second.getY() + 1, second.getZ() + 4.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(middle()));
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(second).add(0.0, 0.5, 0.0), Direction.UP, second, false));
		helper.assertTrue(!level.getBlockState(second.above()).is(middle()) && player.getMainHandItem().is(middle().asItem()),
				"With a block in its way it isn't placed");
		helper.succeed();
	}

	@GameTest(structure = ARENA, skyAccess = true, maxTicks = 300)
	public void aHandCrankUnderItTurnsIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HauntedCarousel carousel = raised(helper);
		BlockPos pos = helper.absolutePos(CENTRE);
		HauntedCarouselBlockEntity drive = (HauntedCarouselBlockEntity) level.getBlockEntity(pos);
		helper.assertTrue(drive.acceptKinetic(Direction.DOWN, 100) == HauntedCarousel.NEED, "It takes " + HauntedCarousel.NEED + " KE a tick");
		helper.assertTrue(drive.acceptKinetic(Direction.EAST, 100) == 0, "and no more in the same tick");
		BlockPos crank = CENTRE.below();
		helper.setBlock(crank, JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.UP));
		((HandCrankBlockEntity) level.getBlockEntity(helper.absolutePos(crank))).addTurns(100);
		helper.runAfterDelay(80, () -> {
			helper.assertTrue(Math.abs(carousel.speed() - HauntedCarousel.FULL_SPEED) < 1.0E-5F, "Cranked, it comes up to full speed: " + carousel.speed());
			helper.assertTrue(carousel.angle() > 0.1F && carousel.lit(), "It has turned, and its lights are on: " + carousel.angle());
			helper.assertTrue(level.getBlockState(pos).getValue(HauntedCarouselBlock.LIT) && level.getBlockState(pos).getLightEmission() == HauntedCarouselBlock.LIGHT,
					"Its middle gives light " + HauntedCarouselBlock.LIGHT);
		});
		helper.runAfterDelay(280, () -> {
			helper.assertTrue(carousel.speed() == 0.0F && !level.getBlockState(pos).getValue(HauntedCarouselBlock.LIT),
					"When the crank stops, so does the carousel, and its lights go out: " + carousel.speed());
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, skyAccess = true)
	public void ridersBoardTheNearestMountAndRideUpAndDown(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HauntedCarousel carousel = raised(helper);
		BlockPos pos = helper.absolutePos(CENTRE);
		// With the carousel at 0, slot 0 (the coach) is east of its middle and slot 2 (a bat) north; slot 1's horses between.
		Vec3 middle = Vec3.atBottomCenterOf(CENTRE);
		ServerPlayer far = player(helper, middle.add(0.0, 0.0, 9.0));
		helper.assertTrue(!carousel.board(far), "Not from afar");
		ServerPlayer first = player(helper, middle.add(new Vec3(3.4, 0.0, 0.0).yRot(HauntedCarousel.slotAngle(1, 0.0F))));
		helper.assertTrue(carousel.board(first) && first.getVehicle() == carousel, "A player boards");
		int seat = carousel.seatOf(first);
		helper.assertTrue(seat == 2, "the outer horse nearest them (seat 2), not seat " + seat);
		ServerPlayer second = player(helper, middle.add(3.4, 0.0, 0.0));
		helper.assertTrue(carousel.board(second) && HauntedCarousel.mountOf(carousel.seatOf(second)) == 0,
				"A second boards the coach beside them: seat " + carousel.seatOf(second));
		// The horse at slot 1 rides highest with the carousel at 0 and lowest a sixth of a turn on (HauntedCarousel.bob).
		carousel.setAngle(0.0F);
		carousel.positionRider(first);
		double up = first.getY() - pos.getY();
		carousel.setAngle((float) (Math.PI / 3.0));
		carousel.positionRider(first);
		double down = first.getY() - pos.getY();
		helper.assertTrue(Math.abs(up - down - 2 * HauntedCarousel.BOB) < 0.01 && up < 2.5 && down > 0.0,
				"A rider rides up and down with their horse: " + up + " up, then " + down);
		Vec3 sat = first.position();
		first.stopRiding();
		double out = Math.hypot(first.getX() - (pos.getX() + 0.5), first.getZ() - (pos.getZ() + 0.5));
		double was = Math.hypot(sat.x - (pos.getX() + 0.5), sat.z - (pos.getZ() + 0.5));
		helper.assertTrue(!first.isPassenger() && out > was && Math.abs(first.getY() - pos.getY()) < 0.01,
				"Getting off sets them down on the ground outside their mount: " + out + " out, " + (first.getY() - pos.getY()) + " up");
		helper.assertTrue(carousel.seatOf(first) < 0, "Their seat is free again");
		helper.succeed();
	}

	@GameTest(structure = ARENA, skyAccess = true, maxTicks = 60)
	public void aBlockInTheWayJamsIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HauntedCarousel carousel = raised(helper);
		helper.assertTrue(!carousel.blocked(level, 0.0F), "Nothing is in its way");
		// A block where the bat at slot 2 (north of the middle, the carousel at 0) has its seat.
		Vec3 seat = carousel.position().add(HauntedCarousel.seat(3, 0.0F));
		BlockPos in = BlockPos.containing(seat);
		level.setBlockAndUpdate(in, Blocks.STONE.defaultBlockState());
		helper.assertTrue(carousel.blocked(level, 0.0F), "A block in a mount's way blocks it");
		BlockPos source = helper.absolutePos(CENTRE).below();
		helper.onEachTick(() -> KineticNetworks.push(level, source, Direction.UP, 16));
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(carousel.jammed() && carousel.speed() == 0.0F, "Driven, it stays jammed");
			level.setBlockAndUpdate(in, Blocks.AIR.defaultBlockState());
		});
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(!carousel.jammed() && carousel.speed() > 0.0F, "Cleared, it turns again");
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, skyAccess = true, maxTicks = 120)
	public void itsOrganPlaysWhileItTurnsAndCanBeQuieted(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HauntedCarousel carousel = raised(helper);
		BlockPos pos = helper.absolutePos(CENTRE);
		BlockPos source = pos.below();
		helper.onEachTick(() -> KineticNetworks.push(level, source, Direction.UP, 16));
		ServerPlayer player = player(helper, Vec3.atBottomCenterOf(CENTRE).add(0.0, 0.0, -4.0));
		helper.runAfterDelay(4 * HauntedCarousel.BEAT_TICKS + 1, () -> {
			helper.assertTrue(carousel.beat() >= 3, "Turning, its organ plays a beat every " + HauntedCarousel.BEAT_TICKS + " ticks: " + carousel.beat());
			use(player, level, pos, true);
			helper.assertTrue(!level.getBlockState(pos).getValue(HauntedCarouselBlock.ORGAN) && !player.isPassenger(),
					"Sneaking, an empty hand quiets it (and boards nothing)");
		});
		helper.runAfterDelay(4 * HauntedCarousel.BEAT_TICKS + 2, () -> {
			int beat = carousel.beat();
			helper.runAfterDelay(4 * HauntedCarousel.BEAT_TICKS, () -> {
				helper.assertTrue(carousel.beat() == beat, "Quiet, it plays no more: " + carousel.beat() + " after " + beat);
				use(player, level, pos, false);
				helper.assertTrue(player.getVehicle() == carousel, "Not sneaking, an empty hand boards it");
				helper.succeed();
			});
		});
	}

	@GameTest(structure = ARENA, skyAccess = true)
	public void breakingItsMiddleTakesItDown(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HauntedCarousel carousel = raised(helper);
		BlockPos pos = helper.absolutePos(CENTRE);
		ServerPlayer rider = player(helper, Vec3.atBottomCenterOf(CENTRE).add(3.0, 0.0, 0.0));
		helper.assertTrue(carousel.board(rider), "A rider boards");
		level.destroyBlock(pos, true);
		helper.assertTrue(carousel.isRemoved() && !rider.isPassenger(), "The carousel goes, and its rider is off");
		int dropped = 0;
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3.0))) {
			if (item.getItem().is(middle().asItem())) {
				dropped += item.getItem().getCount();
			}
		}
		helper.assertTrue(dropped == 1, "Its middle drops once, not " + dropped);
		helper.succeed();
	}

	@GameTest
	public void hauntedCarouselDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(HauntedCarouselBlock.ID))).isPresent(),
				"The Haunted Carousel has a recipe");
		helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
				Jugcraft.id("blocks/" + HauntedCarouselBlock.ID))) != LootTable.EMPTY, "Its loot loads");
		helper.succeed();
	}
}
