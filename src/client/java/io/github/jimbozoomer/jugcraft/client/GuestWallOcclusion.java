package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;

/** Hide a guest only if a continuous sheet of opaque cubes covers the entire projected bounds. */
final class GuestWallOcclusion {
    private GuestWallOcclusion() {}

    static boolean hidden(BlockGetter level, AABB box, double x, double y, double z) {
        double dx = Math.max(box.minX - x, x - box.maxX);
        double dy = Math.max(box.minY - y, y - box.maxY);
        double dz = Math.max(box.minZ - z, z - box.maxZ);
        if (Math.max(dx, Math.max(dy, dz)) < 1.0) return false;
        if (dx >= dy && dx >= dz)
            return covered(level, 0, x, y, z, box.minX, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ);
        if (dy >= dz)
            return covered(level, 1, y, z, x, box.minY, box.maxY, box.minZ, box.maxZ, box.minX, box.maxX);
        return covered(level, 2, z, x, y, box.minZ, box.maxZ, box.minX, box.maxX, box.minY, box.maxY);
    }

    private static boolean covered(BlockGetter level, int axis, double eye, double u, double v,
            double minDepth, double maxDepth, double minU, double maxU, double minV, double maxV) {
        int step = eye < minDepth ? -1 : 1;
        double edge = step < 0 ? minDepth : maxDepth;
        int first = Mth.floor(edge);
        if ((first + .5 - edge) * step <= 0) first += step;
        var cursor = new BlockPos.MutableBlockPos();
        // Probe more densely next to the guest. Missing a wall just keeps the guest visible.
        for (int offset = 0; ; offset = offset * 2 + 1) {
            int slice = first + offset * step;
            double depth = slice + .5;
            if ((eye - depth) * step <= 0) return false;
            double a = (depth - eye) / (minDepth - eye);
            double b = (depth - eye) / (maxDepth - eye);
            int u0 = Mth.floor(u + Math.min((minU - u) * a, (minU - u) * b));
            int u1 = Mth.floor(u + Math.max((maxU - u) * a, (maxU - u) * b));
            int v0 = Mth.floor(v + Math.min((minV - v) * a, (minV - v) * b));
            int v1 = Mth.floor(v + Math.max((maxV - v) * a, (maxV - v) * b));
            boolean solid = true;
            for (int i = u0; solid && i <= u1; i++) {
                for (int j = v0; j <= v1; j++) {
                    switch (axis) {
                        case 0 -> cursor.set(slice, i, j);
                        case 1 -> cursor.set(j, slice, i);
                        default -> cursor.set(i, j, slice);
                    }
                    if (!level.getBlockState(cursor).isSolidRender()) {
                        solid = false;
                        break;
                    }
                }
            }
            if (solid) return true;
        }
    }
}
