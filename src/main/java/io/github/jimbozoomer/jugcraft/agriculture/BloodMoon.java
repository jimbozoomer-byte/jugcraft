package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * The Blood Moon (docs/features/blood-moon.md): through the Halloween season, every {@value #EVERY}th night (by the
 * overworld's day count) and the nights of Halloween itself ({@link HalloweenSeason#harvestMoon()}), the night sky turns
 * blood red ({@link #tint}, drawn by the client) and a red haze hangs about everyone out under it. Under it the dead walk
 * thick: zombies and skeletons put on costumes {@value #COSTUME_CHANCE} of the time ({@link CostumedMobs#chance}), a
 * costumed mob a player kills drops {@value #CANDY_ROLLS} rolls of candy, will-o'-wisps come every try and gather
 * {@value #WISP_NEAR_CAP} to a player ({@link Wisps}), and the Headless Horseman rides by everyone out that night who
 * has not seen him that day ({@link HorsemanSightings}). Everyone who sees it rise earns Bad Moon Rising. Night is the
 * overworld's ({@link HarvestMoon#DUSK} to {@link HarvestMoon#DAWN}).
 *
 * <p>The server works it out every {@value #CHECK_TICKS} ticks and keeps the answer; when it changes, players are told
 * (the moon rising or setting) and clients get a {@link Payload} for the sky; a player who joins gets the current state.
 * The server's {@code halloween.blood_moon} ({@code on} or {@code off}) and the {@code agriculture} switch turn it off;
 * the season's own dates and {@code halloween.mode} bound it. It leaves nothing behind: the costumes and candy already
 * handed out stay, as every Halloween event's do.
 */
public final class BloodMoon {
	public static final int EVERY = 4;
	public static final int CHECK_TICKS = 100;
	public static final float COSTUME_CHANCE = 0.4F;
	public static final int CANDY_ROLLS = 2;
	public static final float WISP_CHANCE = 1.0F;
	public static final int WISP_NEAR_CAP = 8;
	public static final int HAZE_TICKS = 40;
	public static final int HAZE_COUNT = 6;
	public static final double HAZE_RADIUS = 10.0;
	public static final String OPTION = "halloween.blood_moon";
	public static final String ADVANCEMENT = "bad_moon_rising";
	/** The haze: a dark blood red. */
	private static final DustParticleOptions HAZE = new DustParticleOptions(0x8A0B0B, 1.6F);

	private static boolean active;
	/** Set by tests to have it up or down whatever the date; null follows the date. */
	private static @Nullable Boolean forced;
	/** What the server last told this client (set by the client's receiver; read by the sky). */
	public static volatile boolean clientActive;

	private BloodMoon() {
	}

	/** Server to client: whether the Blood Moon is up. */
	public record Payload(boolean active) implements CustomPacketPayload {
		public static final Type<Payload> TYPE = new Type<>(Jugcraft.id("blood_moon"));
		public static final StreamCodec<ByteBuf, Payload> CODEC = ByteBufCodecs.BOOL.map(Payload::new, Payload::active);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	static void register() {
		PayloadTypeRegistry.clientboundPlay().register(Payload.TYPE, Payload.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % CHECK_TICKS == 0) {
				update(server);
			}
			if (active && server.getTickCount() % HAZE_TICKS == 0) {
				haze(server.overworld());
			}
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.getPlayer(), active));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			active = false;
			forced = null;
		});
	}

	/** Whether the Blood Moon is up, as last worked out on the server. */
	public static boolean active() {
		return active;
	}

	/** Whether the Blood Moon is up at {@code dayTime} on the overworld clock, on today's date (server clock). */
	public static boolean rising(long dayTime) {
		long hour = Math.floorMod(dayTime, TrickOrTreat.DAY);
		return HalloweenSeason.active() && JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE)
				&& !"off".equalsIgnoreCase(JugcraftConfig.textOption(OPTION)) && hour >= HarvestMoon.DUSK && hour < HarvestMoon.DAWN
				&& (Math.floorMod(Math.floorDiv(dayTime, TrickOrTreat.DAY), EVERY) == 0 || HalloweenSeason.today().equals(HalloweenSeason.harvestMoon()));
	}

	/** Works the state out again; if it changed, tells every player. Returns the new state. */
	public static boolean update(MinecraftServer server) {
		return update(server, server.overworld().getOverworldClockTime());
	}

	/** Works the state out as if the overworld clock read {@code dayTime}; if it changed, tells every player. */
	public static boolean update(MinecraftServer server, long dayTime) {
		boolean now = forced != null ? forced : rising(dayTime);
		if (now != active) {
			active = now;
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.sendSystemMessage(Component.translatable(now ? "message.jugcraft.blood_moon.rises" : "message.jugcraft.blood_moon.sets"));
				send(player, now);
				if (now && player.level() == server.overworld() && !player.isSpectator()) {
					TrickOrTreat.award(player, ADVANCEMENT);
				}
			}
		}
		return active;
	}

	/** Has it up ({@code true}) or down ({@code false}) whatever the date, or follows the date again ({@code null}) (tests). */
	public static void force(@Nullable Boolean state) {
		forced = state;
	}

	/** A red haze about everyone out under it in the overworld: {@value #HAZE_COUNT} motes each, at most. */
	private static void haze(ServerLevel level) {
		for (ServerPlayer player : level.players()) {
			if (player.isSpectator() || !level.canSeeSky(player.blockPosition())) {
				continue;
			}
			for (int i = 0; i < HAZE_COUNT; i++) {
				double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
				double reach = 2.0 + level.getRandom().nextDouble() * (HAZE_RADIUS - 2.0);
				level.sendParticles(HAZE, player.getX() + Math.cos(angle) * reach, player.getY() + level.getRandom().nextDouble() * 4.0,
						player.getZ() + Math.sin(angle) * reach, 1, 0.2, 0.2, 0.2, 0.0);
			}
		}
	}

	/**
	 * The night sky under the Blood Moon: the sky colour ({@code red}, {@code green}, {@code blue}) turned blood red, the
	 * more as night deepens ({@code night}, 0 by day to 1 at night: the sky's star brightness).
	 */
	public static Vector3f tint(float red, float green, float blue, float night) {
		float depth = Math.max(0.0F, Math.min(1.0F, night));
		return new Vector3f(red + (0.28F + red * 0.4F - red) * depth, green + (green * 0.2F - green) * depth, blue + (blue * 0.2F - blue) * depth);
	}

	private static void send(ServerPlayer player, boolean state) {
		if (ServerPlayNetworking.canSend(player, Payload.TYPE)) {
			ServerPlayNetworking.send(player, new Payload(state));
		}
	}
}
