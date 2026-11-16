package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.artillery.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Fixed city services; no chunk tickets, player ownership, ammunition inventories or salvage. */
public final class TownDefense {
    public static final int RANGE = 250;
    private TownDefense() {}

    /** A bounded, reachable firing/meeting post just inside the authored gate.
     * Keep the opponent identity while rerouting, never carve a shortcut through
     * the wall or accept a partial path ending at it. Three lanes reduce crowding.
     */
    public static net.minecraft.world.level.pathfinder.Path gatePath(Townsfolk guard,
            net.minecraft.core.BlockPos origin, Vec3 enemy) {
        var gate = io.github.jimbozoomer.jugcraft.raiders.CityRaidRoute.nearest(origin, enemy);
        int preferred = Math.floorMod(guard.getUUID().hashCode(), 3) - 1;
        for (int lane : preferred == 0 ? new int[]{0,-1,1} : new int[]{preferred,0,-preferred}) {
            var post = gate.passage().offset(-gate.dx()*4 + gate.dz()*lane, 0, -gate.dz()*4 - gate.dx()*lane);
            if (!guard.level().isLoaded(post)) continue;
            var path = guard.getNavigation().createPath(post, 0);
            if (path != null && path.canReach() && path.getEndNode() != null
                    && path.getEndNode().asBlockPos().equals(post)) return path;
        }
        return null;
    }

    public static LivingEntity target(ServerLevel level, net.minecraft.world.entity.Entity shooter, Vec3 from) {
        return target(level, shooter, from, candidate -> true);
    }

    public static LivingEntity target(ServerLevel level, net.minecraft.world.entity.Entity shooter, Vec3 from,
            java.util.function.Predicate<LivingEntity> reachable) {
        LivingEntity best = null;
        double distance = RANGE * RANGE;
        for (var candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(RANGE), AutomatedDefense::hostile)) {
            double d = candidate.distanceToSqr(from);
            if (d >= distance || !reachable.test(candidate) || !visible(level, shooter, from, candidate.getBoundingBox().getCenter())) continue;
            best = candidate; distance = d;
        }
        return best;
    }

    public static boolean visible(ServerLevel level, net.minecraft.world.entity.Entity shooter, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }

    public static void crew(ServerLevel level, Townsfolk guard) {
        if (!guard.role().equals("flak_guard") || guard.spot() < 0 || guard.isPassenger()
                || !level.isPositionEntityTicking(guard.home())) return;
        var guns = level.getEntitiesOfClass(FlakGun.class, new AABB(guard.home()).inflate(4),
                gun -> guard.home().equals(gun.townStation()));
        FlakGun gun;
        if (guns.isEmpty()) {
            gun = JugcraftArtillery.FLAK_GUN.create(level, EntitySpawnReason.STRUCTURE);
            if (gun == null) return;
            gun.setTownStation(guard.home());
            gun.setPos(Vec3.atBottomCenterOf(guard.home()));
            if (!level.noCollision(gun) || !level.addFreshEntity(gun)) return;
        } else {
            gun = guns.getFirst();
            for (int i = 1; i < guns.size(); i++) guns.get(i).discard();
        }
        guard.getNavigation().stop();
        guard.setPos(Vec3.atBottomCenterOf(guard.home()));
        guard.startRiding(gun);
    }
}
