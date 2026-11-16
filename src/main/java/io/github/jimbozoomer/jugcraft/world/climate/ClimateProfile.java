package io.github.jimbozoomer.jugcraft.world.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;

/**
 * A world's spawn-relative climate bands: their geometry and biome pools. One codec, used by every biome source that
 * carries climate (the {@code jugcraft:climate} wrapper and the optional {@code climate} field of {@code jugcraft:design}).
 * It lives in that source's serialized settings, so two worlds keep two profiles and nothing is global.
 *
 * <pre>
 * { "schema": 1, "anchor": { "mode": "fixed", "x": 0, "z": 0 },
 *   "north_frigid": 2500, "south_hot_start": 2000, "south_hot_end": 4000, "south_frigid": 5500, "transition": 256,
 *   "salt": 0,                                                   (optional, default 0)
 *   "pools": { "frigid": { "land": "#jugcraft:climate/frigid/land", ... }, "hot": {...}, "temperate": {...} } }
 *                                                                (optional; each entry defaults to its tag)
 * </pre>
 *
 * The codec enforces exactly the eleven rules of {@link ClimateRules}, on exact integers: a number with a fraction or
 * beyond the 32-bit range is refused, never rounded. It needs registry ops (pools are biome holder sets). On
 * encoding, {@code salt} 0 and pools at their default tags are left out.
 */
public record ClimateProfile(ClimateGeometry geometry, ClimatePools pools) {
	/** An integer read exactly: whole numbers only, within the int range; written as an int. */
	static final Codec<Integer> EXACT_INT = new PrimitiveCodec<>() {
		@Override
		public <T> DataResult<Integer> read(DynamicOps<T> ops, T input) {
			return ops.getNumberValue(input).flatMap(ClimateProfile::exact);
		}

		@Override
		public <T> T write(DynamicOps<T> ops, Integer value) {
			return ops.createInt(value);
		}

		@Override
		public String toString() {
			return "ExactInt";
		}
	};

	private record RawAnchor(String mode, int x, int z) {
	}

	private record Raw(HolderGetter<Biome> biomes, int schema, RawAnchor anchor, int northFrigid, int southHotStart,
			int southHotEnd, int southFrigid, int transition, int salt,
			Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> pools) {
	}

	private static final Codec<RawAnchor> ANCHOR = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("mode").forGetter(RawAnchor::mode),
			EXACT_INT.fieldOf("x").forGetter(RawAnchor::x),
			EXACT_INT.fieldOf("z").forGetter(RawAnchor::z)
	).apply(i, RawAnchor::new));

	public static final Codec<ClimateProfile> CODEC = RecordCodecBuilder.<Raw>mapCodec(i -> i.group(
			RegistryOps.retrieveGetter(Registries.BIOME),
			EXACT_INT.fieldOf("schema").forGetter(Raw::schema),
			ANCHOR.fieldOf("anchor").forGetter(Raw::anchor),
			EXACT_INT.fieldOf("north_frigid").forGetter(Raw::northFrigid),
			EXACT_INT.fieldOf("south_hot_start").forGetter(Raw::southHotStart),
			EXACT_INT.fieldOf("south_hot_end").forGetter(Raw::southHotEnd),
			EXACT_INT.fieldOf("south_frigid").forGetter(Raw::southFrigid),
			EXACT_INT.fieldOf("transition").forGetter(Raw::transition),
			EXACT_INT.optionalFieldOf("salt", ClimateRules.DEFAULT_SALT).forGetter(Raw::salt),
			ClimatePools.OVERRIDES.optionalFieldOf("pools", Map.of()).forGetter(Raw::pools)
	).apply(i, Raw::new)).flatXmap(ClimateProfile::fromRaw, profile -> DataResult.success(profile.toRaw())).codec();

	public ClimateProfile {
		if (geometry == null || pools == null) {
			throw new IllegalArgumentException("A climate profile needs a geometry and pools");
		}
	}

	/** The default belts and pools, measured from this anchor (a designed world's spawn x/z). */
	public static ClimateProfile defaults(HolderGetter<Biome> biomes, ClimateAnchor anchor) {
		return new ClimateProfile(ClimateGeometry.defaults(anchor), ClimatePools.defaults(biomes));
	}

	/** The anchor's x: the block column the belts are measured from (contract API). */
	public int anchorX() {
		return geometry.anchor().x();
	}

	/** The anchor's z: north is -Z from here, south +Z (contract API). */
	public int anchorZ() {
		return geometry.anchor().z();
	}

	public int salt() {
		return geometry.salt();
	}

	private static DataResult<Integer> exact(Number number) {
		double value = number.doubleValue();
		if (!Double.isFinite(value) || value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			return DataResult.error(() -> "Not a 32-bit integer: " + number);
		}
		return DataResult.success((int) value);
	}

	private static DataResult<ClimateProfile> fromRaw(Raw raw) {
		List<String> problems = new ArrayList<>(ClimateRules.problems(raw.schema(), raw.anchor().mode(), raw.anchor().x(),
				raw.anchor().z(), raw.northFrigid(), raw.southHotStart(), raw.southHotEnd(), raw.southFrigid(), raw.transition()));
		DataResult<ClimatePools> pools = ClimatePools.resolve(raw.biomes(), raw.pools());
		pools.error().ifPresent(error -> problems.add(error.message()));
		if (!problems.isEmpty()) {
			return DataResult.error(() -> "Invalid climate profile: " + String.join("; ", problems));
		}
		ClimateGeometry geometry = new ClimateGeometry(new ClimateAnchor(raw.anchor().x(), raw.anchor().z()), raw.northFrigid(),
				raw.southHotStart(), raw.southHotEnd(), raw.southFrigid(), raw.transition(), raw.salt());
		return DataResult.success(new ClimateProfile(geometry, pools.getOrThrow()));
	}

	private Raw toRaw() {
		ClimateGeometry g = geometry;
		return new Raw(null, ClimateRules.SCHEMA, new RawAnchor(ClimateAnchor.FIXED, g.anchor().x(), g.anchor().z()), g.northFrigid(),
				g.southHotStart(), g.southHotEnd(), g.southFrigid(), g.transition(), g.salt(), pools.overrides());
	}
}
