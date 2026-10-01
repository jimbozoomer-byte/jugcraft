package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import io.github.jimbozoomer.jugcraft.season.SeasonPalette;
import io.github.jimbozoomer.jugcraft.season.SeasonPayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;

/**
 * Seasonal grass and foliage colours on the client. The server sends the season day (the client's own clock is never
 * used); the client shifts grass and foliage tints in biomes tagged {@code #jugcraft:has_seasons} by
 * {@link SeasonPalette}. The hook is {@code mixin/client/ClientLevelSeasonMixin}; colours are cached by the level's
 * tint caches, so the work happens once per block and season change, not every frame.
 */
public final class SeasonColors {
	/** Biomes already looked up in the tag. Chunk meshing threads read it, hence a concurrent map. */
	private static final Map<Biome, Boolean> SEASONAL = new ConcurrentHashMap<>();
	private static volatile int day;

	private SeasonColors() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(SeasonPayload.TYPE, (payload, context) -> setDay(payload.day()));
		// Leaving a server returns to vanilla colours, so a server without Jugcraft shows no seasons.
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
			day = 0;
			SEASONAL.clear();
		});
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
			if (client) {
				SEASONAL.clear();
				if (day != 0) {
					Minecraft.getInstance().execute(SeasonColors::redraw);
				}
			}
		});
	}

	/** The season day the server last sent (0: off or not told). */
	public static int day() {
		return day;
	}

	private static void setDay(int newDay) {
		int checked = newDay < 0 || newDay > SeasonCalendar.DAYS ? 0 : newDay;
		if (checked != day) {
			day = checked;
			redraw();
		}
	}

	/** Drops the cached tints and rebuilds the chunk meshes, so a new season shows at once. */
	private static void redraw() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			minecraft.level.clearTintCaches();
			minecraft.levelExtractor.allChanged();
		}
	}

	/** The grass or foliage tint a biome gives at (x, z), shifted for the season if the biome has seasons. */
	public static int adjust(RegistryAccess registries, ColorResolver resolver, Biome biome, double x, double z, int vanilla) {
		int today = day;
		if (today == 0) {
			return vanilla;
		}
		boolean foliage = resolver == BiomeColors.FOLIAGE_COLOR_RESOLVER;
		if (!foliage && resolver != BiomeColors.GRASS_COLOR_RESOLVER) {
			return vanilla;
		}
		boolean seasonal = SEASONAL.computeIfAbsent(biome,
				key -> registries.lookupOrThrow(Registries.BIOME).wrapAsHolder(key).is(JugcraftSeasons.HAS_SEASONS));
		return seasonal ? SeasonPalette.colour(today, vanilla, foliage, x, z) : vanilla;
	}
}
