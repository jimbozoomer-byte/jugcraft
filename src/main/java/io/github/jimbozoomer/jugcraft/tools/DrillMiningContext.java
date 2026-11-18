package io.github.jimbozoomer.jugcraft.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

/** The face of this player's current drill break, scoped to its native game-mode instance. */
public interface DrillMiningContext {
    @Nullable Direction jugcraft$drillFace(BlockPos pos);
}
