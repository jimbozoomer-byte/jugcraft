package io.github.jimbozoomer.jugcraft.lair.roc;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;
import org.jspecify.annotations.Nullable;

/**
 * The Weather-Vane Lens (docs/features/storm-roc.md): a round lens of storm-blue glass in a copper rim, a weather vane's
 * arrow etched across it. Held in the offhand in the Overworld, it tells its holder the coming weather above their
 * hotbar every {@value #EVERY} ticks: fair for at least so long, rain in so long, rain or thunder now and how long until it
 * clears. It reads the server's own weather clock, and changes nothing: vanilla's lightning rods and plain watching of the
 * sky still do the same work. Decided on the server; the work is bounded: a look at each player's offhand every
 * {@value #EVERY} ticks.
 */
public final class WeatherVaneLens {
	public static final int EVERY = 100;
	/** Ticks in a minute. */
	private static final double MINUTE = 1200.0;

	private WeatherVaneLens() {
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(WeatherVaneLens::tick);
	}

	/** Whether {@code living} holds a Weather-Vane Lens in their offhand. */
	public static boolean holds(LivingEntity living) {
		return JugcraftRoc.WEATHER_VANE_LENS != null && living.getOffhandItem().is(JugcraftRoc.WEATHER_VANE_LENS);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % EVERY != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tell(player);
		}
	}

	/** Tells {@code player} the coming weather if they hold the lens in the Overworld. Returns what it said, or null. */
	public static @Nullable Component tell(ServerPlayer player) {
		if (!holds(player) || !(player.level() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
			return null;
		}
		Component forecast = forecast(level);
		player.sendOverlayMessage(forecast);
		return forecast;
	}

	/**
	 * The coming weather in {@code level}, from its weather clock: while the clear spell set by a command or a sleep lasts,
	 * fair for at least that long; raining, thunder now or soon, and how long until it clears; dry, how long until it rains.
	 */
	public static Component forecast(ServerLevel level) {
		WeatherData weather = level.getWeatherData();
		if (weather.getClearWeatherTime() > 0) {
			return Component.translatable("message.jugcraft.storm_roc.lens.fair", minutes(weather.getClearWeatherTime()));
		}
		if (level.isRaining()) {
			if (level.isThundering()) {
				return Component.translatable("message.jugcraft.storm_roc.lens.thunder_now", minutes(weather.getRainTime()));
			}
			if (weather.getThunderTime() < weather.getRainTime()) {
				return Component.translatable("message.jugcraft.storm_roc.lens.thunder_in", minutes(weather.getThunderTime()));
			}
			return Component.translatable("message.jugcraft.storm_roc.lens.rain_now", minutes(weather.getRainTime()));
		}
		return Component.translatable("message.jugcraft.storm_roc.lens.rain_in", minutes(weather.getRainTime()));
	}

	/** Ticks as whole minutes, at least one. */
	public static int minutes(int ticks) {
		return Math.max(1, (int) Math.round(ticks / MINUTE));
	}
}
