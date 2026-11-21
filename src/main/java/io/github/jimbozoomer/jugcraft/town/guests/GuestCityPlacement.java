package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import io.github.jimbozoomer.jugcraft.town.ApartmentRentals;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Explicit, repeatable placement into clear town lawns. Never edits terrain or loads chunks. */
public final class GuestCityPlacement {
    private static final int MIN_POST_DISTANCE = 12;
    private GuestCityPlacement() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("guests")
                .then(Commands.literal("city").requires(CustomNpcs::isOperator)
                        .then(Commands.literal("preview").executes(c -> run(c.getSource(), false, false)))
                        .then(Commands.literal("place").executes(c -> run(c.getSource(), false, true)))
                        .then(Commands.literal("spread")
                                .then(Commands.literal("preview").executes(c -> run(c.getSource(), true, false)))
                                .then(Commands.literal("apply").executes(c -> run(c.getSource(), true, true))))));
    }

    private static int run(CommandSourceStack source, boolean spread, boolean apply) {
        boolean console = isServerConsole(source);
        if (!CustomNpcs.isOperator(source) || !console && (source.getPlayer() == null || !source.getPlayer().isCreative())) {
            source.sendFailure(Component.literal("Use the server console or a creative operator in the Overworld."));
            return 0;
        }
        ServerLevel level = source.getLevel();
        BlockPos origin = TownState.get(level).origin();
        if (origin == null || level != level.getServer().overworld()) {
            source.sendFailure(Component.literal("There is no generated spawn city in this dimension."));
            return 0;
        }
        TownData data = TownData.get();
        GuestLedger ledger = GuestLedger.get(level.getServer());
        Map<String, BlockPos> posts = new LinkedHashMap<>();
        Map<String, Townsfolk> movable = new LinkedHashMap<>();
        for (var guest : GuestRoster.ALL) {
            UUID uuid = ledger.canonical(guest.id());
            if (uuid == null || ledger.missing(guest.id())) continue;
            if (level.getEntity(uuid) instanceof Townsfolk npc && guest.id().equals(npc.guest())) {
                BlockPos home = npc.home();
                posts.put(guest.id(), home);
                if (spread && ordinaryCityPost(level, origin, data, ledger, npc, guest.id())) {
                    movable.put(guest.id(), npc);
                }
            } else {
                var seen = ledger.lastSeen(guest.id());
                if (seen != null && seen.dimension().equals(level.dimension())) posts.put(guest.id(), seen.pos());
            }
        }
        var waiting = GuestRoster.ALL.stream().filter(g -> g.available() && (spread ? movable.containsKey(g.id())
                : GuestPlacement.status(ledger, g.id()) == GuestPlacement.Status.UNPLACED)).toList();
        List<BlockPos> candidates = waiting.isEmpty() ? List.of() : sites(level, origin, data);
        int handled = 0;
        for (var guest : waiting) {
            BlockPos old = posts.remove(guest.id());
            boolean crowded = old == null || nearestSquared(old, posts.values()) < MIN_POST_DISTANCE * MIN_POST_DISTANCE;
            if (old != null) posts.put(guest.id(), old);
            if (!crowded) continue;
            var site = nextSite(level, candidates, posts.values());
            if (site == null) break;
            if (!apply) {
                source.sendSuccess(() -> Component.literal(guest.name() + (old == null ? " -> " : " " + old.toShortString() + " -> ")
                        + site.toShortString()), false);
            } else if (spread) {
                Townsfolk npc = movable.get(guest.id());
                if (!GuestOutings.putHome(level, npc, site)) {
                    source.sendFailure(Component.literal("Could not move " + guest.name() + " to " + site.toShortString() + "."));
                    continue;
                }
                CustomNpcs.setPost(npc, level, site);
                ledger.seen(guest.id(), npc.getUUID(), level.dimension(), site);
            } else {
                var result = GuestPlacement.place(source, guest.id(), level, Vec3.atBottomCenterOf(site), 180, console);
                if (!result.placed()) {
                    source.sendFailure(result.message());
                    continue;
                }
            }
            posts.put(guest.id(), site);
            handled++;
        }
        int count = handled;
        source.sendSuccess(() -> Component.literal((apply ? (spread ? "Moved " : "Placed ") : "Preview for ")
                + count + " guests on loaded city lawns. New posts are at least " + MIN_POST_DISTANCE + " blocks from reserved posts."
                + (spread ? "" : " Still unplaced after this plan: " + (waiting.size() - count) + ".")), false);
        return count;
    }

    private static boolean ordinaryCityPost(ServerLevel level, BlockPos origin, TownData data, GuestLedger ledger,
            Townsfolk npc, String id) {
        BlockPos home = npc.home(), relative = home.subtract(origin);
        return relative.getY() == 1 && data.mask(relative.getX(), relative.getZ()) == TownData.INSIDE
                && data.state(data.index(relative.getX(), 0, relative.getZ())).is(Blocks.GRASS_BLOCK)
                && npc.custom() != null && npc.custom().patrol().isEmpty() && ledger.spouseOf(id) == null
                && GuestActivity.get(level.getServer()).current(id) == null && !npc.isLeashed()
                && !npc.isPassenger() && !npc.isVehicle() && !npc.isSleeping()
                && home.closerThan(npc.blockPosition(), 12)
                && LandRegistry.mayBuildFor(null, level, home) && !OperatorRegions.protects(level, home)
                && !ApartmentRentals.protects(level, home);
    }

    private static double nearestSquared(BlockPos site, Collection<BlockPos> posts) {
        double distance = Double.POSITIVE_INFINITY;
        for (BlockPos post : posts) distance = Math.min(distance, site.distSqr(post));
        return distance;
    }

    static @Nullable BlockPos farthest(List<BlockPos> candidates, Collection<BlockPos> posts) {
        BlockPos best = null;
        double score = MIN_POST_DISTANCE * MIN_POST_DISTANCE;
        for (BlockPos site : candidates) {
            double distance = nearestSquared(site, posts);
            if (distance >= score && (best == null || distance > score)) {
                best = site;
                score = distance;
            }
        }
        return best;
    }

    private static @Nullable BlockPos nextSite(ServerLevel level, List<BlockPos> candidates, Collection<BlockPos> posts) {
        BlockPos site;
        while ((site = farthest(candidates, posts)) != null) {
            candidates.remove(site);
            if (unclaimedAndEmpty(level, site)) return site;
        }
        return null;
    }

    static boolean isServerConsole(CommandSourceStack source) {
        // withSource returns the same stack exactly when its underlying command source already matches.
        return source.getEntity() == null && source.withSource(source.getServer()) == source
                && Commands.LEVEL_OWNERS.check(source.permissions());
    }

    /** Survey loaded grass first. Check claims and occupants only for nominated sites. */
    private static List<BlockPos> sites(ServerLevel level, BlockPos origin, TownData data) {
        List<BlockPos> result = new ArrayList<>();
        for (int z = 12; z < data.size - 12; z += 2) {
            for (int x = 12; x < data.size - 12; x += 2) {
                if (data.mask(x, z) != TownData.INSIDE) continue;
                var designed = data.state(data.index(x, 0, z));
                if (designed == null || !designed.is(Blocks.GRASS_BLOCK)) continue;
                BlockPos feet = origin.offset(x, 1, z);
                BlockPos relative = new BlockPos(x, 1, z);
                if (data.spots.stream().anyMatch(s -> s.pos().distSqr(relative) < 25)) continue;
                if (clearGround(level, feet)) result.add(feet);
            }
        }
        return result;
    }

    /** A full clear lawn margin excludes blocks and block entities such as owner slot machines. */
    static boolean clearLawn(ServerLevel level, BlockPos feet) {
        return clearGround(level, feet) && unclaimedAndEmpty(level, feet);
    }

    private static boolean clearGround(ServerLevel level, BlockPos feet) {
        for (BlockPos cell : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 2, 1))) {
            if (!level.isInWorldBounds(cell) || !level.getWorldBorder().isWithinBounds(cell) || !level.isLoaded(cell)) return false;
            if (cell.getY() == feet.getY() - 1) {
                if (!level.getBlockState(cell).is(Blocks.GRASS_BLOCK)) return false;
            } else if (!level.getBlockState(cell).isAir()) return false;
        }
        return true;
    }

    private static boolean unclaimedAndEmpty(ServerLevel level, BlockPos feet) {
        for (BlockPos cell : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 2, 1))) {
            if (!LandRegistry.mayBuildFor(null, level, cell) || OperatorRegions.protects(level, cell)
                    || ApartmentRentals.protects(level, cell)) return false;
        }
        return level.getEntities(null, new AABB(feet).inflate(3)).isEmpty();
    }
}
