package io.github.jimbozoomer.jugcraft.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Transient mining input: never saved on equipment or shared between players. */
public record DrillMiningTarget(Level level, BlockPos pos, Direction face, ItemStack tool, int mode) {
    public static @Nullable DrillMiningTarget start(Player player, BlockPos pos, Direction face) {
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof MiningDrillItem)) return null;
        int mode = MiningDrillItem.mode(tool);
        return mode == MiningDrillItem.AREA || mode == MiningDrillItem.AREA_LARGE
            ? new DrillMiningTarget(player.level(), pos.immutable(), face, tool, mode) : null;
    }

    public boolean matches(Player player, BlockPos target) {
        return player.isAlive() && !player.isSpectator() && player.level() == level && pos.equals(target)
            && player.getMainHandItem() == tool && !tool.isEmpty() && MiningDrillItem.mode(tool) == mode;
    }
}
