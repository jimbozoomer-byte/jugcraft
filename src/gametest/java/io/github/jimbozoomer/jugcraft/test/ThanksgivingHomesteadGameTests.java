package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CandleCenterpieceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HomesteadFurnitureBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HomesteadSettleBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Thanksgiving homestead and feast room (tools/thanksgiving_homestead.py; docs/features/thanksgiving-homestead.md).
 * The settle and the dresser are placed whole, all four blocks facing the player in every direction, and never over
 * another block; breaking any one of their blocks takes all four and drops the piece once. The settle seats two, one
 * on each half, at the top of its squab, whichever block of a half is used. The dresser does nothing when used. The
 * floor pieces face whoever places them and drop themselves; the chamberstick lights, gives its light and snuffs; the
 * wall pieces hang on a wall, not on nothing, and fall with it. In a claim, a stranger cannot sit, light or break.
 * Each half of the settle is one seat, at its lower block, whichever of its blocks is used, in every facing (and turned
 * as a structure turns it); broken from any block while both halves are taken, it stands both sitters up at once and
 * leaves no seat behind; no piston moves it, and an explosion takes it whole or not at all. The furniture is never
 * placed over water or round an entity. In a protected town, every piece is a public amenity (no stored items), so the
 * town lets its plain use through (TownProtection). The companions' side of the settle is SettleSeatGameTests.
 * Written for the server game tests; not run here (no Minecraft toolchain in the environment that wrote them).
 */
public class ThanksgivingHomesteadGameTests {
	private static final List<String> FURNITURE = List.of("oak_settle", "carved_dish_dresser");
	private static final List<String> FLOOR = List.of("pewter_pitcher_and_basin", "pewter_chamberstick");
	private static final List<String> WALL = List.of("pilgrim_peg_rail", "harvest_sampler");
	/** The yaws that face a player south, west, north and east, so a piece placed by them faces the other way. */
	private static final float[] YAWS = {0.0F, 90.0F, 180.0F, 270.0F};

	@GameTest(maxTicks = 80)
	public void furnitureIsPlacedWholeInEveryFacingAndBreaksAsOne(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos master = new BlockPos(4, 2, 4);
		for (String id : FURNITURE) {
			MultiDecorationBlock block = (MultiDecorationBlock) block(id);
			for (int turn = 0; turn < YAWS.length; turn++) {
				clear(helper);
				ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
				player.setYRot(YAWS[turn]);
				place(player, helper, master, new ItemStack(item(id)));
				Direction facing = player.getDirection().getOpposite();
				BlockPos at = helper.absolutePos(master);
				// Its four blocks: the one aimed at, the one to the placer's right (its facing's counter-clockwise), and the
				// two above them.
				BlockPos[] parts = {at, at.relative(facing.getCounterClockWise()), at.above(), at.relative(facing.getCounterClockWise()).above()};
				for (int part = 0; part < parts.length; part++) {
					BlockState state = level.getBlockState(parts[part]);
					helper.assertTrue(state.is(block) && state.getValue(MultiDecorationBlock.FACING) == facing
									&& state.getValue(HomesteadFurnitureBlock.PART) == part,
							id + " placed facing " + facing + ": part " + part + " is in place, facing the player: " + state);
					helper.assertTrue(block.partPos(at, facing, part).equals(parts[part]), id + ": part " + part + " is where the block says");
				}
				helper.assertTrue(player.getMainHandItem().isEmpty(), id + " is used up placing it");
				// Break a different block each turn: every block takes the whole piece and it drops once.
				level.destroyBlock(parts[turn], true);
				for (BlockPos pos : parts) {
					helper.assertTrue(level.getBlockState(pos).isAir(), id + ": breaking part " + turn + " takes every block, leaving none behind");
				}
				helper.assertTrue(dropped(helper, item(id)) == 1, id + ": breaking part " + turn + " drops it once");
				clearDrops(helper);
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void furnitureNeverOverwritesABlock(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos master = new BlockPos(4, 2, 4);
		for (String id : FURNITURE) {
			// Facing north, its right-hand half is to the west and its upper blocks above: block each in turn.
			for (BlockPos blocker : List.of(master.west(), master.above(), master.west().above())) {
				clear(helper);
				helper.setBlock(blocker, Blocks.STONE);
				ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
				place(player, helper, master, new ItemStack(item(id)));
				helper.assertTrue(level.getBlockState(helper.absolutePos(master)).isAir() && helper.getBlockState(blocker).is(Blocks.STONE),
						id + " is not placed where its block at " + blocker + " is taken, and the stone is left as it was");
				helper.assertTrue(player.getMainHandItem().is(item(id)) && player.getMainHandItem().getCount() == 1, id + " is not used up");
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void theSettleSeatsTwoAtTheTopOfItsSquabs(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		BlockPos master = new BlockPos(4, 2, 4);
		ServerPlayer placer = standing(helper, new BlockPos(4, 2, 1));
		place(placer, helper, master, new ItemStack(item("oak_settle")));
		BlockPos at = helper.absolutePos(master);
		BlockPos other = at.west();
		helper.assertTrue(level.getBlockState(other).is(block("oak_settle")), "The settle stands two blocks across, facing north");
		ServerPlayer first = standing(helper, new BlockPos(4, 2, 2));
		ServerPlayer second = standing(helper, new BlockPos(3, 2, 2));
		ServerPlayer third = standing(helper, new BlockPos(5, 2, 2));
		use(first, level, at, ItemStack.EMPTY);
		helper.assertTrue(first.getVehicle() instanceof Seat, "Using its first half sits the player down");
		double height = first.getVehicle().getY() - at.getY();
		helper.assertTrue(Math.abs(height - HomesteadSettleBlock.SEAT) < 0.01, "at the top of the squab (" + height + ")");
		// The second half, used at its upper block: the second sitter sits on its own squab, not on the back.
		use(second, level, other.above(), ItemStack.EMPTY);
		helper.assertTrue(second.getVehicle() instanceof Seat && second.getVehicle() != first.getVehicle()
				&& second.getVehicle().blockPosition().equals(other), "The other half seats a second player on its own squab");
		helper.assertTrue(Math.abs(second.getVehicle().getY() - other.getY() - HomesteadSettleBlock.SEAT) < 0.01, "at the same height");
		use(third, level, at, ItemStack.EMPTY);
		helper.assertFalse(third.isPassenger(), "One sitter a seat");
		third.setShiftKeyDown(true);
		use(third, level, other, ItemStack.EMPTY);
		helper.assertFalse(third.isPassenger(), "A sneaking player doesn't sit");
		third.setShiftKeyDown(false);
		level.destroyBlock(at.above(), true);
		// The seats go on their own next tick (Seat.tick): wait until they have, rather than a fixed delay.
		helper.succeedWhen(() -> {
			helper.assertTrue(!first.isPassenger() && !second.isPassenger(), "Breaking the settle stands both sitters up");
			helper.assertTrue(level.getEntitiesOfClass(Seat.class, new AABB(at).inflate(3.0)).isEmpty(), "and leaves no seat behind");
		});
	}

	@GameTest(maxTicks = 40)
	public void theDresserIsDecorationOnly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		BlockPos master = new BlockPos(4, 2, 4);
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		place(player, helper, master, new ItemStack(item("carved_dish_dresser")));
		BlockPos at = helper.absolutePos(master);
		BlockState before = level.getBlockState(at);
		for (BlockPos pos : List.of(at, at.west(), at.above(), at.west().above())) {
			use(player, level, pos, ItemStack.EMPTY);
		}
		helper.assertTrue(level.getBlockState(at) == before && level.getBlockState(at.above()).getValue(MultiDecorationBlock.LIT) == before.getValue(MultiDecorationBlock.LIT),
				"Using it changes nothing");
		helper.assertTrue(player.containerMenu == player.inventoryMenu, "It opens no storage");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void floorPiecesFaceTheirPlacerAndDropThemselves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (String id : FLOOR) {
			place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(id)));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.is(block(id)) && placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH,
					id + " stands where it was placed, facing the player: " + placed);
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(id)) == 1, id + ", broken, drops itself once");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theChamberstickLightsAndSnuffs(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item("pewter_chamberstick")));
		helper.assertTrue(!level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT) && level.getBlockState(at).getLightEmission() == 0,
				"It is placed unlit, giving no light");
		use(player, level, at, new ItemStack(Items.FLINT_AND_STEEL));
		helper.assertTrue(level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT)
				&& level.getBlockState(at).getLightEmission() == JugcraftAgriculture.PEWTER_CHAMBERSTICK_LIGHT, "Flint and steel lights its candle");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(!level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT) && level.getBlockState(at).getLightEmission() == 0,
				"An empty hand snuffs it");
		use(player, level, at, new ItemStack(Items.FIRE_CHARGE));
		helper.assertTrue(level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT) && player.getMainHandItem().isEmpty(),
				"A fire charge lights it, used up");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void wallPiecesHangOnAWallAndFallWithIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : WALL) {
			clear(helper);
			ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
			// With no wall near, there is nothing to hang it on.
			place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(id)));
			helper.assertTrue(helper.getBlockState(new BlockPos(3, 2, 3)).isAir(), id + " is not hung on nothing");
			// A wall to the south: used on its north face, it hangs there facing north.
			helper.setBlock(new BlockPos(3, 2, 4), Blocks.STONE);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(id)));
			BlockPos wall = helper.absolutePos(new BlockPos(3, 2, 4));
			player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(wall).relative(Direction.NORTH, 0.5), Direction.NORTH, wall, false)));
			BlockState hung = helper.getBlockState(new BlockPos(3, 2, 3));
			helper.assertTrue(hung.is(block(id)) && hung.getValue(WallDecorationBlock.FACING) == Direction.NORTH, id + " hangs on the wall: " + hung);
			level.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 3)), true);
			helper.assertTrue(dropped(helper, item(id)) == 1, id + ", broken, drops itself once");
			clearDrops(helper);
			helper.setBlock(new BlockPos(3, 2, 3), hung);
			helper.setBlock(new BlockPos(3, 2, 4), Blocks.AIR);
			helper.assertTrue(helper.getBlockState(new BlockPos(3, 2, 3)).isAir(), id + " falls when its wall goes");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void inAClaimStrangersCannotSitLightOrBreak(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// Far from every other test, as the claim tests do, so the claim covers nothing else; in the middle of its chunk,
		// so every block used here is inside the one claimed chunk.
		BlockPos far = helper.absolutePos(new BlockPos(10240, 3, 10240));
		BlockPos base = new BlockPos((far.getX() >> 4 << 4) + 8, far.getY(), (far.getZ() >> 4 << 4) + 8);
		ServerPlayer owner = helper.makeMockServerPlayerInLevel();
		ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
		owner.setGameMode(GameType.SURVIVAL);
		stranger.setGameMode(GameType.SURVIVAL);
		owner.snapTo(Vec3.atCenterOf(base.north(3)));
		stranger.snapTo(Vec3.atCenterOf(base.north(3).east()));
		var land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(base);
		try {
			land.assign(book, key, book.account(owner.getUUID(), "HomesteadOwner"), 0);
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					level.setBlockAndUpdate(base.offset(x, -1, z), Blocks.STONE.defaultBlockState());
				}
			}
			placeAt(owner, base.below(), new ItemStack(item("oak_settle")));
			placeAt(owner, base.east(2).below(), new ItemStack(item("pewter_chamberstick")));
			helper.assertTrue(level.getBlockState(base).is(block("oak_settle")) && level.getBlockState(base.east(2)).is(block("pewter_chamberstick")),
					"The owner places the settle and the chamberstick in their claim");
			use(stranger, level, base, ItemStack.EMPTY);
			helper.assertFalse(stranger.isPassenger(), "A stranger cannot sit on the settle in someone's claim");
			use(stranger, level, base.east(2), new ItemStack(Items.FLINT_AND_STEEL));
			helper.assertFalse(level.getBlockState(base.east(2)).getValue(CandleCenterpieceBlock.LIT), "nor light the chamberstick");
			stranger.gameMode.destroyBlock(base.above());
			helper.assertTrue(level.getBlockState(base).is(block("oak_settle")) && level.getBlockState(base.above()).is(block("oak_settle")),
					"nor break the settle");
			use(owner, level, base, ItemStack.EMPTY);
			helper.assertTrue(owner.getVehicle() instanceof Seat, "The owner can sit on it");
			owner.stopRiding();
		} finally {
			land.abandon(owner, book, key, true, 0);
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					for (int y = -1; y <= 2; y++) {
						level.setBlockAndUpdate(base.offset(x, y, z), Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void eachSettleHalfIsOneSeatInEveryFacing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HomesteadSettleBlock settle = (HomesteadSettleBlock) block("oak_settle");
		BlockPos master = new BlockPos(4, 2, 4);
		for (int turn = 0; turn < YAWS.length; turn++) {
			clear(helper);
			ServerPlayer placer = standing(helper, new BlockPos(4, 2, 1));
			placer.setYRot(YAWS[turn]);
			place(placer, helper, master, new ItemStack(item("oak_settle")));
			Direction facing = placer.getDirection().getOpposite();
			BlockPos at = helper.absolutePos(master);
			BlockPos[] parts = new BlockPos[4];
			for (int part = 0; part < parts.length; part++) {
				parts[part] = settle.partPos(at, facing, part);
				BlockState state = level.getBlockState(parts[part]);
				helper.assertTrue(state.is(settle) && settle.seatCell(level, parts[part], state).equals(settle.partPos(at, facing, part % 2)),
						"Facing " + facing + ", part " + part + " seats at the lower block of its half");
				// A structure turned to this facing turns each part the same way: the same seat, from the same block.
				Rotation rotation = Rotation.NONE;
				for (Rotation turned : Rotation.values()) {
					if (turned.rotate(Direction.NORTH) == facing) {
						rotation = turned;
					}
				}
				helper.assertTrue(settle.defaultBlockState().setValue(HomesteadFurnitureBlock.PART, part).rotate(rotation) == state,
						"Part " + part + " facing north, turned to face " + facing + ", is the part placed facing " + facing);
			}
			// Used at their upper blocks, both halves seat, each at its lower block; the half taken refuses anyone else,
			// from either of its blocks.
			ServerPlayer first = standing(helper, new BlockPos(1, 2, 1));
			ServerPlayer second = standing(helper, new BlockPos(2, 2, 1));
			ServerPlayer third = standing(helper, new BlockPos(6, 2, 1));
			use(first, level, parts[2], ItemStack.EMPTY);
			use(second, level, parts[3], ItemStack.EMPTY);
			helper.assertTrue(first.getVehicle() instanceof Seat && first.getVehicle().blockPosition().equals(parts[0]),
					"Facing " + facing + ", the first half's upper block seats the player on its squab, at its lower block");
			helper.assertTrue(second.getVehicle() instanceof Seat && second.getVehicle().blockPosition().equals(parts[1]),
					"Facing " + facing + ", so does the second half's");
			for (BlockPos pos : parts) {
				use(third, level, pos, ItemStack.EMPTY);
				helper.assertFalse(third.isPassenger(), "Facing " + facing + ", a third player finds no seat free at " + pos);
			}
			// Broken from a different block each turn while both halves are taken: both stand up at once, no seat stays.
			level.destroyBlock(parts[turn], true);
			helper.assertTrue(!first.isPassenger() && !second.isPassenger(), "Facing " + facing + ", breaking part " + turn + " stands both sitters up at once");
			helper.assertTrue(level.getEntitiesOfClass(Seat.class, new AABB(at).inflate(3.0)).isEmpty(), "and leaves no seat behind");
			for (BlockPos pos : parts) {
				helper.assertTrue(level.getBlockState(pos).isAir(), "Facing " + facing + ", breaking part " + turn + " takes every block");
			}
			helper.assertTrue(dropped(helper, item("oak_settle")) == 1, "and the settle drops once");
			clearDrops(helper);
			// Dismounting leaves players standing on the old seat. Move this
			// iteration's players away before testing placement in the next facing.
			for (ServerPlayer player : new ServerPlayer[] {placer, first, second, third}) {
				player.snapTo(helper.absoluteVec(new Vec3(10.5, 2.0, 10.5)));
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void pistonsCannotMoveTheSettleAndAnExplosionTakesItWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		BlockPos master = new BlockPos(4, 2, 4);
		ServerPlayer placer = standing(helper, new BlockPos(4, 2, 1));
		place(placer, helper, master, new ItemStack(item("oak_settle")));
		BlockPos at = helper.absolutePos(master);
		BlockPos[] parts = {at, at.west(), at.above(), at.west().above()};
		for (BlockPos part : parts) {
			for (Direction push : Direction.values()) {
				helper.assertFalse(PistonBaseBlock.isPushable(level.getBlockState(part), level, part, push, true, push),
						"No piston pushes, pulls or pops the settle's block at " + part + " (" + push + ")");
			}
		}
		ServerPlayer first = standing(helper, new BlockPos(1, 2, 1));
		ServerPlayer second = standing(helper, new BlockPos(2, 2, 1));
		use(first, level, at, ItemStack.EMPTY);
		use(second, level, at.west().above(), ItemStack.EMPTY);
		helper.assertTrue(first.getVehicle() instanceof Seat && second.getVehicle() instanceof Seat, "Both halves are taken");
		level.explode(null, at.getX() + 0.5, at.getY() + 0.5, at.getZ() - 0.5, 3.0F, Level.ExplosionInteraction.TNT);
		Block settle = block("oak_settle");
		helper.succeedWhen(() -> {
			int standingParts = 0;
			for (BlockPos part : parts) {
				if (level.getBlockState(part).is(settle)) {
					standingParts++;
				}
			}
			helper.assertTrue(standingParts == 0 || standingParts == parts.length,
					"An explosion takes the settle whole or not at all (" + standingParts + " of 4 blocks left)");
			helper.assertTrue(dropped(helper, item("oak_settle")) <= 1, "and it drops at most once");
			if (standingParts == 0) {
				helper.assertTrue(level.getEntitiesOfClass(Seat.class, new AABB(at).inflate(3.0)).isEmpty(), "Blown away, it leaves no seat behind");
				helper.assertTrue(!first.isPassenger() && !second.isPassenger(), "and nobody sitting on nothing");
			}
		});
	}

	@GameTest(maxTicks = 40)
	public void furnitureIsNeverPlacedOverWaterOrRoundAnEntity(GameTestHelper helper) {
		BlockPos master = new BlockPos(4, 2, 4);
		for (String id : FURNITURE) {
			// Facing north, its upper right-hand block is above the block to the west.
			clear(helper);
			helper.setBlock(master.west().above(), Blocks.WATER);
			ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
			place(player, helper, master, new ItemStack(item(id)));
			helper.assertTrue(helper.getBlockState(master).isAir() && helper.getBlockState(master.west().above()).is(Blocks.WATER),
					id + " is not placed where one of its blocks would take the place of water");
			clear(helper);
			ServerPlayer inTheWay = standing(helper, master.west());
			place(player, helper, master, new ItemStack(item(id)));
			helper.assertTrue(helper.getBlockState(master).isAir() && helper.getBlockState(master.west()).isAir(),
					id + " is not placed where one of its blocks would close round a player");
			helper.assertTrue(player.getMainHandItem().is(item(id)), id + " is not used up");
			// Out of the way of the next piece.
			BlockPos aside = helper.absolutePos(new BlockPos(0, 2, 7));
			inTheWay.setPos(aside.getX() + 0.5, aside.getY(), aside.getZ() + 0.5);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void everyPieceIsAPublicAmenityInATown(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		clear(helper);
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		place(player, helper, new BlockPos(2, 2, 4), new ItemStack(item("oak_settle")));
		place(player, helper, new BlockPos(6, 2, 4), new ItemStack(item("carved_dish_dresser")));
		for (BlockPos master : List.of(new BlockPos(2, 2, 4), new BlockPos(6, 2, 4))) {
			for (BlockPos pos : List.of(master, master.west(), master.above(), master.west().above())) {
				BlockState state = helper.getBlockState(pos);
				helper.assertTrue(state.getBlock() instanceof HomesteadFurnitureBlock && TownProtection.publicBlock(level, helper.absolutePos(pos), state),
						state + " is a public amenity in a protected town: it stores nothing");
			}
		}
		for (String id : FLOOR) {
			BlockPos pos = new BlockPos(FLOOR.indexOf(id) * 2 + 1, 2, 7);
			helper.setBlock(pos, block(id));
			helper.assertTrue(TownProtection.publicBlock(level, helper.absolutePos(pos), helper.getBlockState(pos)), id + " is a public amenity");
		}
		for (String id : WALL) {
			BlockPos pos = new BlockPos(WALL.indexOf(id) * 2 + 1, 3, 1);
			helper.setBlock(pos.south(), Blocks.STONE);
			helper.setBlock(pos, block(id).defaultBlockState().setValue(WallDecorationBlock.FACING, Direction.NORTH));
			helper.assertTrue(helper.getBlockState(pos).is(block(id)) && TownProtection.publicBlock(level, helper.absolutePos(pos), helper.getBlockState(pos)),
					id + " is a public amenity");
		}
		helper.succeed();
	}

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	/** A stone floor at y 1 and the air above it cleared, for a fresh placement. */
	private static void clear(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
				for (int y = 2; y < 5; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
		clearDrops(helper);
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	/** Uses {@code held} on the block at {@code absolute} (an absolute position) from its front, as a player does. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
	}

	/** Places {@code held} from the player's hand on the top of the block below {@code pos} (relative). */
	private static void place(ServerPlayer player, GameTestHelper helper, BlockPos pos, ItemStack held) {
		placeAt(player, helper.absolutePos(pos.below()), held);
	}

	/** Places {@code held} from the player's hand on the top of the block at {@code below} (absolute). */
	private static void placeAt(ServerPlayer player, BlockPos below, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 3, 4))).inflate(6.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clearDrops(GameTestHelper helper) {
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 3, 4))).inflate(6.0))
				.forEach(ItemEntity::discard);
	}
}
