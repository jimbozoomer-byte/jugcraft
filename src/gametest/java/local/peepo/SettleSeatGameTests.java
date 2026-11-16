package local.peepo;

import io.github.jimbozoomer.jugcraft.agriculture.HomesteadSettleBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The High-Backed Oak Settle and the companions (CompanionSeats; the settle is tools/thanksgiving_homestead.py's): each
 * half of the settle is one seat, kept at its lower block whichever of its two blocks a player or a companion comes to,
 * so a player and a companion, or two companions, never share a squab; and broken while a companion holds a half, the
 * settle forgets that companion's reservation at once. In this package for CompanionSeats' package-private seats.
 * Written for the server game tests; not run here (no Minecraft toolchain in the environment that wrote them).
 */
public class SettleSeatGameTests {
	@GameTest(maxTicks = 40)
	public void aSettleHalfIsOneSeatForCompanionsAndPlayers(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		HomesteadSettleBlock settle = (HomesteadSettleBlock) JugcraftAgriculture.block("oak_settle");
		ServerPlayer placer = player(helper, new BlockPos(4, 2, 1));
		BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
		placer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftAgriculture.item("oak_settle")));
		placer.getMainHandItem().useOn(new UseOnContext(placer, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(at.below()).relative(Direction.UP, 0.5), Direction.UP, at.below(), false)));
		// Facing north, its second half is to the west; the upper blocks are above.
		BlockPos other = at.west();
		helper.assertTrue(level.getBlockState(at).is(settle) && level.getBlockState(other.above()).is(settle), "The settle stands, facing north");
		helper.assertTrue(CompanionSeats.seatCell(level, at.above(), level.getBlockState(at.above())).equals(at)
						&& CompanionSeats.seatCell(level, other.above(), level.getBlockState(other.above())).equals(other)
						&& CompanionSeats.seatCell(level, at, level.getBlockState(at)).equals(at),
				"Each half's seat is its lower block, from either of its blocks");
		PeepoEntity first = companion(helper, level, new BlockPos(1, 2, 1));
		PeepoEntity second = companion(helper, level, new BlockPos(2, 2, 1));
		// A companion coming to the first half by its upper block is sent to the squab below, and holds that seat.
		CompanionSeats.Surface high = new CompanionSeats.Surface(level, at.above(), level.getBlockState(at.above()), Direction.NORTH);
		helper.assertTrue(high.stationPosition().equals(at), "A companion choosing the settle at an upper block sits at the lower block below it");
		CompanionSeats.reserveExternal(first, high.stationPosition(), 200);
		helper.assertTrue(CompanionSeats.isReserved(level, at), "The first companion holds the first half");
		CompanionSeats.Surface low = new CompanionSeats.Surface(level, at, level.getBlockState(at), Direction.NORTH);
		helper.assertFalse(high.availableTo(second) || low.availableTo(second), "A second companion cannot take that half, from either of its blocks");
		ServerPlayer sitter = player(helper, new BlockPos(6, 2, 1));
		use(sitter, level, at.above());
		helper.assertFalse(sitter.isPassenger(), "nor can a player, from its upper block");
		use(sitter, level, at);
		helper.assertFalse(sitter.isPassenger(), "or from its lower block");
		// The other half is free: the player sits there, from its upper block, on its squab.
		use(sitter, level, other.above());
		helper.assertTrue(sitter.getVehicle() instanceof Seat && sitter.getVehicle().blockPosition().equals(other),
				"The player sits on the other half's squab, from its upper block");
		CompanionSeats.Surface taken = new CompanionSeats.Surface(level, other.above(), level.getBlockState(other.above()), Direction.NORTH);
		helper.assertTrue(taken.stationPosition().equals(other) && !taken.availableTo(second),
				"and a companion cannot take the half a player sits on, coming to either of its blocks");
		// Broken while both halves are taken: the player stands up, no seat stays, and the reservation is forgotten.
		level.destroyBlock(other.above(), true);
		helper.assertFalse(sitter.isPassenger(), "Breaking the settle stands its sitter up at once");
		helper.assertTrue(level.getEntitiesOfClass(Seat.class, new AABB(at).inflate(3.0)).isEmpty(), "and leaves no seat behind");
		helper.assertFalse(CompanionSeats.isReserved(level, at) || CompanionSeats.isReserved(level, other),
				"and its companion's reservation is released at once");
		first.discard();
		second.discard();
		helper.succeed();
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	/** A companion standing still (no AI) at {@code at} (relative). */
	private static PeepoEntity companion(GameTestHelper helper, ServerLevel level, BlockPos at) {
		PeepoEntity npc = PeepoMod.PEEPO.create(level, EntitySpawnReason.COMMAND);
		helper.assertTrue(npc != null, "A companion is made");
		npc.setNoAi(true);
		BlockPos absolute = helper.absolutePos(at);
		npc.snapTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5, 0.0F, 0.0F);
		level.addFreshEntity(npc);
		return npc;
	}

	/** Uses the block at {@code absolute} with an empty hand, from its north face, as a player does. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute) {
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
	}
}
