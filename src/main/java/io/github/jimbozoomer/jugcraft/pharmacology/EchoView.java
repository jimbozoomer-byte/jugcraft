package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauScrying;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** A bounded, loaded-space viewport. The real player, inventory, game mode and chunk center never move. */
public final class EchoView {
	public static final int RADIUS = 24;
	private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();
	private static final class Session {
		final Vec3 body, origin;
		final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
		final long until;
		Vec3 view;
		long lastInput = Long.MIN_VALUE;
		Session(ServerPlayer player, int seconds) {
			body = player.position(); origin = player.getEyePosition(); view = origin;
			dimension = player.level().dimension(); until = player.level().getGameTime() + seconds * 20L;
		}
	}
	private EchoView() {}
	public static boolean active(ServerPlayer player) { return SESSIONS.containsKey(player.getUUID()); }
	public static Vec3 position(ServerPlayer player) { var s = SESSIONS.get(player.getUUID()); return s == null ? player.getEyePosition() : s.view; }
	public static boolean canStart(ServerPlayer player) {
		return player.isAlive() && !player.isSpectator() && !player.isPassenger() && !player.isSleeping()
				&& (player.onGround() || player.getAbilities().flying)
				&& player.containerMenu == player.inventoryMenu && !NouveauScrying.active(player);
	}
	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(State.TYPE, State.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(Input.TYPE, Input.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Input.TYPE, (input, context) -> control(context.player(), input));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var player : server.getPlayerList().getPlayers()) {
				var session = SESSIONS.get(player.getUUID());
				if (session == null) continue;
				if (!player.isAlive() || player.isSpectator() || player.isPassenger() || !session.dimension.equals(player.level().dimension())
						|| player.position().distanceToSqr(session.body) > .25 || player.level().getGameTime() >= session.until
						|| player.containerMenu != player.inventoryMenu || !Pharmacology.activeProfile(player, PrecisionPharmacy.KETAMINE)) {
					cancel(player); continue;
				}
				player.setDeltaMovement(Vec3.ZERO);
				if (player.tickCount % 40 == 0) player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.jugcraft.echo_view"));
			}
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, damage, blocked) -> { if (entity instanceof ServerPlayer player && damage > 0) cancel(player); });
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> server.execute(() -> cancel(handler.getPlayer())));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();
			if (!active(player) && Pharmacology.state(player).doses().stream().anyMatch(d -> d.drug().equals("ketamine") && d.active())) Pharmacology.cancelFamily(player, "dissociative");
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> { for (var player : server.getPlayerList().getPlayers()) cancel(player); });
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
	}
	static void open(ServerPlayer player, int seconds) {
		// mayDrink checked availability before reserving the dose; no separate duration ledger is persisted.
		player.closeContainer();
		var session = new Session(player, seconds);
		SESSIONS.put(player.getUUID(), session);
		send(player, new State(true, session.view, session.body));
	}
	private static void send(ServerPlayer player, State state) {
		if (ServerPlayNetworking.canSend(player, State.TYPE)) ServerPlayNetworking.send(player, state);
	}
	public static void cancel(ServerPlayer player) {
		if (SESSIONS.remove(player.getUUID()) == null) return;
		Pharmacology.cancelFamily(player, "dissociative");
		player.inventoryMenu.sendAllDataToRemote();
		send(player, new State(false, player.getEyePosition(), player.position()));
	}
	/** Inputs are directions only. Position, collision, range and input rate are decided here. */
	public static void control(ServerPlayer player, Input input) {
		var session = SESSIONS.get(player.getUUID());
		if (session == null) return;
		if (input.stop()) { cancel(player); return; }
		long now = player.level().getGameTime();
		if (session.lastInput != Long.MIN_VALUE && now - session.lastInput < 2) return;
		if (!Float.isFinite(input.yaw()) || !Float.isFinite(input.pitch()) || Math.abs(input.forward()) > 1 || Math.abs(input.strafe()) > 1 || Math.abs(input.vertical()) > 1) return;
		session.lastInput = now;
		if (!Pharmacology.activeProfile(player, PrecisionPharmacy.KETAMINE) || !session.dimension.equals(player.level().dimension())) { cancel(player); return; }
		double yaw = Math.toRadians(input.yaw()), pitch = Math.toRadians(Math.clamp(input.pitch(), -90, 90));
		Vec3 step = new Vec3(-Math.sin(yaw) * Math.cos(pitch) * input.forward() - Math.cos(yaw) * input.strafe(),
				-Math.sin(pitch) * input.forward() + input.vertical(), Math.cos(yaw) * Math.cos(pitch) * input.forward() - Math.sin(yaw) * input.strafe());
		if (step.lengthSqr() > 0) step = step.normalize().scale(.30);
		Vec3 next = session.view.add(step);
		if (next.distanceToSqr(session.origin) <= RADIUS * RADIUS && loadedSpace(player,next)
				&& player.level().noCollision(AABB.ofSize(next, .3, .3, .3))
				&& player.level().clip(new ClipContext(session.view, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS) session.view = next;
		send(player, new State(true, session.view, session.body));
	}
	/** Check every corner before collision/ray queries, including boxes straddling a chunk boundary. */
	private static boolean loadedSpace(ServerPlayer player,Vec3 next) {
		for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})
			if(!player.level().hasChunkAt(BlockPos.containing(next.x+x*.15,next.y,next.z+z*.15)))return false;
		return true;
	}
	public record Input(byte forward, byte strafe, byte vertical, float yaw, float pitch, boolean stop) implements CustomPacketPayload {
		public static final Type<Input> TYPE = new Type<>(Jugcraft.id("pharmacy_echo_input"));
		public static final StreamCodec<RegistryFriendlyByteBuf, Input> CODEC = new StreamCodec<>() {
			public Input decode(RegistryFriendlyByteBuf b) { return new Input(b.readByte(), b.readByte(), b.readByte(), b.readFloat(), b.readFloat(), b.readBoolean()); }
			public void encode(RegistryFriendlyByteBuf b, Input p) { b.writeByte(p.forward); b.writeByte(p.strafe); b.writeByte(p.vertical); b.writeFloat(p.yaw); b.writeFloat(p.pitch); b.writeBoolean(p.stop); }
		};
		public Type<? extends CustomPacketPayload> type() { return TYPE; }
	}
	public record State(boolean active, Vec3 view, Vec3 body) implements CustomPacketPayload {
		public static final Type<State> TYPE = new Type<>(Jugcraft.id("pharmacy_echo_state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, State> CODEC = new StreamCodec<>() {
			public State decode(RegistryFriendlyByteBuf b) { return new State(b.readBoolean(), new Vec3(b.readDouble(), b.readDouble(), b.readDouble()), new Vec3(b.readDouble(), b.readDouble(), b.readDouble())); }
			public void encode(RegistryFriendlyByteBuf b, State p) { b.writeBoolean(p.active); b.writeDouble(p.view.x); b.writeDouble(p.view.y); b.writeDouble(p.view.z); b.writeDouble(p.body.x); b.writeDouble(p.body.y); b.writeDouble(p.body.z); }
		};
		public Type<? extends CustomPacketPayload> type() { return TYPE; }
	}
}
