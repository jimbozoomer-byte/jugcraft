package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

/**
 * Carving pumpkins with the Carving Knife, on the server. Using the knife on the side of a pumpkin
 * opens a carving session for that side and sends the screen to the player; the finished face comes
 * back as a {@link CarvePayload} and is checked here before anything changes:
 * <ul>
 * <li>the player opened a session for exactly that block and side, in this dimension, in the last five minutes;</li>
 * <li>they still hold a Carving Knife, are within reach and may build there;</li>
 * <li>the block is still a pumpkin, the face is 16 valid rows, and it only carves deeper (a knife cannot put skin back);</li>
 * <li>with {@code carving.free_draw=false}, the face is one of the {@link CarvingTemplates}.</li>
 * </ul>
 * One session allows one carve, so a client cannot carve faster than it uses the knife. The first carve
 * turns a plain pumpkin into a {@link CarvedPumpkinBlock} facing that side and drops its seeds, as shears do.
 */
public final class PumpkinCarvings {
	/** How long an opened carving screen stays valid: five minutes. */
	public static final long SESSION_TICKS = 6000;
	/** Server switch: false allows only the starter faces. Tests may change it. */
	public static boolean freeDraw = true;

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private record Session(ResourceKey<Level> dimension, BlockPos pos, Direction side, long expires) {
	}

	public enum Result {
		CARVED, UNCHANGED, NO_SESSION, NO_KNIFE, TOO_FAR, NOT_ALLOWED, NOT_A_PUMPKIN, INVALID, UNCARVING, NOT_A_TEMPLATE
	}

	private PumpkinCarvings() {
	}

	static void register() {
		freeDraw = JugcraftConfig.option("carving.free_draw");
		PayloadTypeRegistry.clientboundPlay().register(OpenCarvingPayload.TYPE, OpenCarvingPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CarvePayload.TYPE, CarvePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(CarvePayload.TYPE, (payload, context) -> {
			Result result = carve(context.player(), payload.pos(), payload.side(), payload.face());
			if (result != Result.CARVED && result != Result.UNCHANGED) {
				context.player().sendOverlayMessage(Component.translatable("message.jugcraft.carving." + result.name().toLowerCase()));
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SESSIONS.remove(handler.getPlayer().getUUID()));
		// A new world (singleplayer) starts without the last one's sessions.
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
	}

	/** Whether a block can be carved: a plain pumpkin or one already carved by hand. */
	public static boolean isCarvable(BlockState state) {
		return state.is(Blocks.PUMPKIN) || state.getBlock() instanceof CarvedPumpkinBlock;
	}

	/** Opens a carving session on one side of a pumpkin and sends the player the carving screen. */
	public static void open(ServerPlayer player, BlockPos pos, Direction side) {
		startSession(player, pos, side);
		if (ServerPlayNetworking.canSend(player, OpenCarvingPayload.TYPE)) {
			ServerPlayNetworking.send(player, new OpenCarvingPayload(pos.immutable(), side, currentFace(player.level(), pos, side), freeDraw));
		}
	}

	/** Records a carving session for {@code side} of the pumpkin at {@code pos}, replacing any earlier one. */
	public static void startSession(ServerPlayer player, BlockPos pos, Direction side) {
		ServerLevel level = player.level();
		SESSIONS.put(player.getUUID(), new Session(level.dimension(), pos.immutable(), side, level.getGameTime() + SESSION_TICKS));
	}

	/** What is carved into one side of a pumpkin now (all skin for a plain pumpkin). */
	public static int[] currentFace(Level level, BlockPos pos, Direction side) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof CarvedPumpkinBlock && level.getBlockEntity(pos) instanceof CarvedPumpkinBlockEntity pumpkin) {
			return pumpkin.carving().face(PumpkinCarving.faceIndex(state.getValue(CarvedPumpkinBlock.FACING), side));
		}
		return new int[PumpkinCarving.SIZE];
	}

	/** Checks and applies a finished face; see the class comment for every check. */
	public static Result carve(ServerPlayer player, BlockPos pos, Direction side, int[] face) {
		Session session = SESSIONS.remove(player.getUUID());
		ServerLevel level = player.level();
		if (session == null || !session.pos().equals(pos) || session.side() != side || session.dimension() != level.dimension()
				|| level.getGameTime() > session.expires()) {
			return Result.NO_SESSION;
		}
		InteractionHand hand = knifeHand(player);
		if (hand == null) {
			return Result.NO_KNIFE;
		}
		if (!player.isWithinBlockInteractionRange(pos, 1.0)) {
			return Result.TOO_FAR;
		}
		ItemStack knife = player.getItemInHand(hand);
		if (!player.mayUseItemAt(pos, side, knife) || !level.mayInteract(player, pos)) {
			return Result.NOT_ALLOWED;
		}
		BlockState state = level.getBlockState(pos);
		if (!isCarvable(state) || side.getAxis() == Direction.Axis.Y) {
			return Result.NOT_A_PUMPKIN;
		}
		if (!PumpkinCarving.isValidFace(face)) {
			return Result.INVALID;
		}
		int[] before = currentFace(level, pos, side);
		if (Arrays.equals(before, face)) {
			return Result.UNCHANGED;
		}
		if (!PumpkinCarving.deepensOnly(before, face)) {
			return Result.UNCARVING;
		}
		if (!freeDraw && !CarvingTemplates.isTemplate(face)) {
			return Result.NOT_A_TEMPLATE;
		}

		if (state.is(Blocks.PUMPKIN)) {
			// The first cut opens the pumpkin: its seeds come out, from vanilla's carving loot table.
			Block.dropFromBlockInteractLootTable(level, BuiltInLootTables.CARVE_PUMPKIN, pos, state, level.getBlockEntity(pos), knife, player,
					(serverLevel, stack) -> drop(serverLevel, pos, side, stack));
			state = JugcraftAgriculture.block("hand_carved_pumpkin").defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side);
			level.setBlock(pos, state, Block.UPDATE_ALL);
		}
		if (!(level.getBlockEntity(pos) instanceof CarvedPumpkinBlockEntity pumpkin)) {
			return Result.NOT_A_PUMPKIN;
		}
		PumpkinCarving carving = pumpkin.carving().withFace(PumpkinCarving.faceIndex(state.getValue(CarvedPumpkinBlock.FACING), side), face);
		pumpkin.setCarving(carving, player);
		if (state.getValue(CarvedPumpkinBlock.GLOW) != carving.glow()) {
			level.setBlock(pos, state.setValue(CarvedPumpkinBlock.GLOW, carving.glow()), Block.UPDATE_ALL);
		}
		knife.hurtAndBreak(1, player, hand.asEquipmentSlot());
		level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		player.awardStat(Stats.ITEM_USED.get(knife.getItem()));
		return Result.CARVED;
	}

	private static @Nullable InteractionHand knifeHand(ServerPlayer player) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).getItem() instanceof CarvingKnifeItem) {
				return hand;
			}
		}
		return null;
	}

	/** Drops an item in front of the carved side, as vanilla does when shears carve a pumpkin. */
	private static void drop(ServerLevel level, BlockPos pos, Direction side, ItemStack stack) {
		ItemEntity item = new ItemEntity(level, pos.getX() + 0.5 + side.getStepX() * 0.65, pos.getY() + 0.1,
				pos.getZ() + 0.5 + side.getStepZ() * 0.65, stack);
		item.setDeltaMovement(0.05 * side.getStepX() + level.getRandom().nextDouble() * 0.02, 0.05,
				0.05 * side.getStepZ() + level.getRandom().nextDouble() * 0.02);
		level.addFreshEntity(item);
	}
}
