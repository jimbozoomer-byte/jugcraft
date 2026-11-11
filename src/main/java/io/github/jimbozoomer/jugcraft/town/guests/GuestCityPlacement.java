package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Explicit, repeatable placement into clear town lawns. Never edits terrain or loads chunks. */
public final class GuestCityPlacement {
    private GuestCityPlacement() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("guests").requires(CustomNpcs::isOperator)
                .then(Commands.literal("city")
                        .then(Commands.literal("preview").executes(c -> run(c.getSource(), false)))
                        .then(Commands.literal("place").executes(c -> run(c.getSource(), true)))));
    }

    private static int run(CommandSourceStack source, boolean apply) {
        if (!CustomNpcs.isOperator(source) || source.getPlayer() == null || !source.getPlayer().isCreative()) {
            source.sendFailure(Component.literal("Use this command as an operator in creative mode in the spawn city."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        BlockPos origin = TownState.get(level).origin();
        if (origin == null || level != level.getServer().overworld()) {
            source.sendFailure(Component.literal("There is no generated spawn city in this dimension."));
            return 0;
        }
        List<BlockPos> sites = sites(level, origin, TownData.get());
        GuestLedger ledger = GuestLedger.get(level.getServer());
        var waiting = GuestRoster.ALL.stream().filter(g -> g.available()
                && GuestPlacement.status(ledger, g.id()) == GuestPlacement.Status.UNPLACED).toList();
        int count = Math.min(sites.size(), waiting.size()), placed = 0;
        for (int i = 0; i < count; i++) {
            var guest = waiting.get(i);
            BlockPos site = sites.get(i);
            if (apply) {
                var result = GuestPlacement.place(source, guest.id(), level, Vec3.atBottomCenterOf(site), 180);
                if (result.placed()) placed++;
                else source.sendFailure(result.message());
            } else {
                source.sendSuccess(() -> Component.literal(guest.name() + ": " + site.toShortString()), false);
            }
        }
        int done = placed;
        source.sendSuccess(() -> Component.literal(apply
                ? "Placed " + done + " guests. " + (waiting.size() - done) + " still unplaced; no blocks changed."
                : "Preview: " + count + " available lawn sites for " + waiting.size()
                        + " guests. Load more city chunks if needed. /guests city place rechecks before placing."), false);
        return apply ? placed : count;
    }

    /** Grass only: never a street, shop, doorstep, decoration, water or existing entity's space. */
    static List<BlockPos> sites(ServerLevel level, BlockPos origin, TownData data) {
        List<BlockPos> result = new ArrayList<>();
        for (int z = 12; z < data.size - 12 && result.size() < 32; z += 3) {
            for (int x = 12; x < data.size - 12 && result.size() < 32; x += 3) {
                if (data.mask(x, z) != TownData.INSIDE) continue;
                var designed = data.state(data.index(x, 0, z));
                if (designed == null || !designed.is(Blocks.GRASS_BLOCK)) continue;
                BlockPos feet = origin.offset(x, 1, z);
                if (result.stream().anyMatch(p -> p.distSqr(feet) < 25)) continue;
                BlockPos relative = new BlockPos(x, 1, z);
                if (data.spots.stream().anyMatch(s -> s.pos().distSqr(relative) < 25)) continue;
                if (clearLawn(level, feet)) result.add(feet);
            }
        }
        return result;
    }

    /** A full clear lawn margin excludes blocks and block entities such as owner slot machines. */
    static boolean clearLawn(ServerLevel level, BlockPos feet) {
        for (BlockPos cell : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 2, 1))) {
            if (!level.isInWorldBounds(cell) || !level.getWorldBorder().isWithinBounds(cell) || !level.isLoaded(cell)) return false;
            if (cell.getY() == feet.getY() - 1) {
                if (!level.getBlockState(cell).is(Blocks.GRASS_BLOCK)) return false;
            } else if (!level.getBlockState(cell).isAir()) return false;
        }
        return level.getEntities(null, new AABB(feet).inflate(3)).isEmpty();
    }
}
