package io.github.jimbozoomer.jugcraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * The valley near this world's start (data/jugcraft_adam_ondi_ahman.dat in the Overworld): its site, and whether it has
 * been chosen (once, on the world's first start; a world with no suitable land near its start keeps none). Saved so
 * that the valley stays where it was chosen whatever later moves the world spawn.
 */
public final class AdamOndiAhmanState extends SavedData {
	public static final Codec<AdamOndiAhmanState> CODEC = RecordCodecBuilder.create(i -> i.group(
			BlockPos.CODEC.optionalFieldOf("start").forGetter(s -> Optional.ofNullable(s.start)),
			Codec.BOOL.optionalFieldOf("chosen", false).forGetter(s -> s.chosen))
			.apply(i, AdamOndiAhmanState::new));
	static final SavedDataType<AdamOndiAhmanState> TYPE = new SavedDataType<>(Jugcraft.id("adam_ondi_ahman"), AdamOndiAhmanState::new,
			CODEC, null);

	private @Nullable BlockPos start;
	private boolean chosen;

	AdamOndiAhmanState() {
		this(Optional.empty(), false);
	}

	AdamOndiAhmanState(Optional<BlockPos> start, boolean chosen) {
		this.start = start.orElse(null);
		this.chosen = chosen;
	}

	/** The state of this Overworld. */
	public static AdamOndiAhmanState get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	/** The site of the valley near the start; null if there is none (yet). */
	public @Nullable BlockPos start() {
		return start;
	}

	public boolean chosen() {
		return chosen;
	}

	void choose(@Nullable BlockPos site) {
		start = site;
		chosen = true;
		setDirty();
	}
}
