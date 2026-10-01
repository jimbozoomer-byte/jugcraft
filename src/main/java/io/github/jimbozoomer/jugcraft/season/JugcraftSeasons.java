package io.github.jimbozoomer.jugcraft.season;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.time.LocalDate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Seasons on the server: it reads the operator's settings, tells each player the season day when they join and
 * again whenever the day changes (checked once a minute). Seasons are only colours: no item, block, recipe or
 * progression depends on them, so nothing is lost or gated when a season ends.
 */
public final class JugcraftSeasons {
	/** Biomes whose grass and leaves change colour with the season (generated from tools/seasons.py). */
	public static final TagKey<Biome> HAS_SEASONS = TagKey.create(Registries.BIOME, Jugcraft.id("has_seasons"));
	/** Ticks between date checks: one minute. */
	public static final int CHECK_TICKS = 1200;

	private static volatile SeasonCalendar.Settings settings = SeasonCalendar.Settings.DEFAULT;
	private static int lastSent = -1;
	private static int ticks;

	private JugcraftSeasons() {
	}

	public static void register() {
		settings = SeasonCalendar.Settings.fromConfig();
		PayloadTypeRegistry.clientboundPlay().register(SeasonPayload.TYPE, SeasonPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			if (ServerPlayNetworking.canSend(listener, SeasonPayload.TYPE)) {
				sender.sendPacket(new SeasonPayload(today()));
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (++ticks >= CHECK_TICKS) {
				ticks = 0;
				if (today() != lastSent) {
					broadcast(server);
				}
			}
		});
	}

	public static SeasonCalendar.Settings settings() {
		return settings;
	}

	/** Today's season day in the configured zone and hemisphere (or the fixed mode's day, or 0 when off). */
	public static int today() {
		SeasonCalendar.Settings current = settings;
		return current.dayOn(LocalDate.now(current.zone()));
	}

	/**
	 * Switches the mode until the server stops (the config file is unchanged) and tells every player. For tests; a
	 * lasting change goes in config/jugcraft.properties.
	 */
	public static void setMode(MinecraftServer server, SeasonCalendar.Mode mode) {
		settings = settings.withMode(mode);
		broadcast(server);
	}

	private static void broadcast(MinecraftServer server) {
		int day = today();
		lastSent = day;
		for (ServerPlayer player : PlayerLookup.all(server)) {
			if (ServerPlayNetworking.canSend(player, SeasonPayload.TYPE)) {
				ServerPlayNetworking.send(player, new SeasonPayload(day));
			}
		}
	}
}
