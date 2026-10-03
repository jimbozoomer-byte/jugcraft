package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.HashMap;
import java.util.List;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

/**
 * Cutting epitaphs with the Stonemason's Chisel, on the server. Using the chisel on a headstone opens a session for
 * that headstone (its part 0) and sends the player the epitaph screen with what is cut there now; the lines come back
 * as an {@link EngravePayload} and are checked here before anything changes:
 * <ul>
 * <li>the player opened a session for exactly that headstone, in this dimension, in the last {@value #SESSION_TICKS}
 * ticks (one session, one cut, so a client cannot cut faster than it uses the chisel);</li>
 * <li>they still hold a chisel, are within reach and may build there;</li>
 * <li>the block is still a headstone's part 0, and the lines fit: at most {@value Epitaph#LINES} of
 * {@value Epitaph#LINE_LENGTH} characters (the network codec refuses anything much longer before it gets here).</li>
 * </ul>
 * A cut wears the chisel by one; the first earns Here Lies….
 */
public final class Epitaphs {
	public static final String CHISEL = "stonemasons_chisel";
	public static final long SESSION_TICKS = 6000;

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();

	private record Session(ResourceKey<Level> dimension, BlockPos pos, long expires) {
	}

	public enum Result {
		ENGRAVED, NO_SESSION, NO_CHISEL, TOO_FAR, NOT_ALLOWED, NOT_A_HEADSTONE, INVALID
	}

	private Epitaphs() {
	}

	static void register() {
		PayloadTypeRegistry.clientboundPlay().register(OpenEpitaphPayload.TYPE, OpenEpitaphPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(EngravePayload.TYPE, EngravePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(EngravePayload.TYPE, (payload, context) -> {
			Result result = engrave(context.player(), payload.pos(), payload.lines());
			context.player().sendOverlayMessage(Component.translatable("message.jugcraft.epitaph." + result.name().toLowerCase()));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SESSIONS.remove(handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
	}

	/** Opens a session on the headstone whose part 0 is at {@code master} and sends the player the epitaph screen. */
	public static void open(ServerPlayer player, BlockPos master) {
		startSession(player, master);
		Epitaph now = player.level().getBlockEntity(master) instanceof HeadstoneBlockEntity stone ? stone.epitaph() : Epitaph.BLANK;
		if (ServerPlayNetworking.canSend(player, OpenEpitaphPayload.TYPE)) {
			ServerPlayNetworking.send(player, new OpenEpitaphPayload(master.immutable(), now.padded()));
		}
	}

	/** Records a session on the headstone at {@code master}, replacing any earlier one (tests start one directly). */
	public static void startSession(ServerPlayer player, BlockPos master) {
		ServerLevel level = player.level();
		SESSIONS.put(player.getUUID(), new Session(level.dimension(), master.immutable(), level.getGameTime() + SESSION_TICKS));
	}

	private static @Nullable InteractionHand chiselHand(ServerPlayer player) {
		for (InteractionHand hand : InteractionHand.values()) {
			if (player.getItemInHand(hand).is(JugcraftAgriculture.item(CHISEL))) {
				return hand;
			}
		}
		return null;
	}

	/** Checks and cuts the lines a player sent; see the class comment for every check. */
	public static Result engrave(ServerPlayer player, BlockPos pos, List<String> lines) {
		Session session = SESSIONS.remove(player.getUUID());
		ServerLevel level = player.level();
		if (session == null || !session.pos().equals(pos) || session.dimension() != level.dimension() || level.getGameTime() > session.expires()) {
			return Result.NO_SESSION;
		}
		InteractionHand hand = chiselHand(player);
		if (hand == null) {
			return Result.NO_CHISEL;
		}
		if (!player.isWithinBlockInteractionRange(pos, 1.0)) {
			return Result.TOO_FAR;
		}
		ItemStack chisel = player.getItemInHand(hand);
		if (!player.mayBuild() || !player.mayUseItemAt(pos, Direction.UP, chisel) || !level.mayInteract(player, pos)) {
			return Result.NOT_ALLOWED;
		}
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof HeadstoneBlock) || state.getValue(HeadstoneBlock.PART) != 0
				|| !(level.getBlockEntity(pos) instanceof HeadstoneBlockEntity stone)) {
			return Result.NOT_A_HEADSTONE;
		}
		if (!Epitaph.fits(lines)) {
			return Result.INVALID;
		}
		Epitaph cut = Epitaph.of(lines);
		stone.engrave(cut);
		chisel.hurtAndBreak(1, player, hand);
		level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 0.8F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		if (!cut.isBlank()) {
			TrickOrTreat.award(player, "here_lies");
		}
		return Result.ENGRAVED;
	}
}
