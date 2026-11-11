package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * What each guest is doing (coordinator's seam): at most one activity per guest at a time, an outing, a home visit, a
 * ceremony or a rivalry, saved with the overworld as {@code jugcraft:town_guest_activity} so a restart neither loses nor
 * doubles one. The feature that began an activity ends it (completion, cancellation, logout, unload, timeout, or its own
 * recovery after a restart); nothing else may. While one holds a guest, {@code Townsfolk.anchor()} is the activity's
 * anchor in its level (wander range {@link #RANGE}), so the guest walks there by its usual goals and back to its post
 * when the activity ends. Nothing here loads a chunk, moves a block or moves the entity itself.
 */
public final class GuestActivity extends SavedData {
	/** How far a guest wanders from an activity's anchor. */
	public static final int RANGE = 2;

	/** One activity: its kind ("outing", "visit", "ceremony", "rivalry"), the player, its game-time start, and where. */
	public record Activity(String kind, UUID player, long started, ResourceKey<Level> dimension, BlockPos anchor) {
		static final Codec<Activity> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("kind").forGetter(Activity::kind),
				UUIDUtil.CODEC.fieldOf("player").forGetter(Activity::player),
				Codec.LONG.fieldOf("started").forGetter(Activity::started),
				ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Activity::dimension),
				BlockPos.CODEC.fieldOf("anchor").forGetter(Activity::anchor)).apply(i, Activity::new));
	}

	static final Codec<GuestActivity> CODEC = Codec.unboundedMap(Codec.STRING, Activity.CODEC)
			.xmap(GuestActivity::new, activity -> activity.activities);
	static final SavedDataType<GuestActivity> TYPE = new SavedDataType<>(Jugcraft.id("town_guest_activity"), GuestActivity::new, CODEC, null);

	private final Map<String, Activity> activities = new HashMap<>();

	public GuestActivity() {
	}

	private GuestActivity(Map<String, Activity> saved) {
		activities.putAll(saved);
	}

	public static GuestActivity get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	/** What holds the guest now, or null when it is free. */
	public @Nullable Activity current(String guest) {
		return activities.get(guest);
	}

	/** Every activity held now, by guest (a copy), for a feature's own recovery after a restart. */
	public Map<String, Activity> all() {
		return Map.copyOf(activities);
	}

	/** Holds the guest for {@code activity}; false (nothing changed) when anything already holds it. */
	public boolean begin(String guest, Activity activity) {
		if (activities.containsKey(guest) || GuestRoster.get(guest) == null) {
			return false;
		}
		activities.put(guest, activity);
		setDirty();
		return true;
	}

	/** Moves the holder's anchor; false unless an activity of {@code kind} holds the guest. */
	public boolean move(String guest, String kind, ResourceKey<Level> dimension, BlockPos anchor) {
		Activity held = activities.get(guest);
		if (held == null || !held.kind().equals(kind)) {
			return false;
		}
		activities.put(guest, new Activity(kind, held.player(), held.started(), dimension, anchor.immutable()));
		setDirty();
		return true;
	}

	/** Ends the holder's activity; false unless an activity of {@code kind} holds the guest. */
	public boolean end(String guest, String kind) {
		Activity held = activities.get(guest);
		if (held == null || !held.kind().equals(kind)) {
			return false;
		}
		activities.remove(guest);
		setDirty();
		return true;
	}

	/** Where {@code npc} should be now: its activity's anchor if one holds it in this level, else null (its post). */
	public static @Nullable BlockPos anchor(ServerLevel level, Townsfolk npc) {
		String guest = npc.guest();
		if (guest == null || !npc.getUUID().equals(GuestLedger.get(level.getServer()).canonical(guest))) {
			return null;
		}
		Activity held = get(level.getServer()).current(guest);
		return held != null && held.dimension().equals(level.dimension()) ? held.anchor() : null;
	}
}
