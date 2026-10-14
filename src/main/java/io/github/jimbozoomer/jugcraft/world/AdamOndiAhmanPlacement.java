package io.github.jimbozoomer.jugcraft.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Where the knolls of Adam-ondi-Ahman stand (structure set {@code jugcraft:altar_knolls}): exactly the chunks that hold
 * a valley's site in the loaded Overworld ({@link AdamOndiAhman#isSiteChunk}), so the knoll and its valley always agree.
 * The structure's own biome check (the valley) still applies.
 */
public record AdamOndiAhmanPlacement() implements StructurePlacement {
	public static final AdamOndiAhmanPlacement INSTANCE = new AdamOndiAhmanPlacement();
	public static final MapCodec<AdamOndiAhmanPlacement> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean isStructureChunk(ChunkGeneratorStructureState state, int x, int z) {
		return AdamOndiAhman.isSiteChunk(x, z);
	}

	@Override
	public MapCodec<? extends StructurePlacement> codec() {
		return CODEC;
	}
}
