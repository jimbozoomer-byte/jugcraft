package io.github.jimbozoomer.jugcraft.prospecting;

import io.github.jimbozoomer.jugcraft.tools.*;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Incremental, loaded-terrain surveys. Distance is to the nearest sampled signal, not an exact vein locator. */
public class OreScannerItem extends Item implements Chargeable {
    public static final long CAPACITY = 250_000;
    public static final long QUICK_COST = 500, DETAIL_COST = 1_000, WIDE_COST = 4_000;
    private static final int BUDGET = 16_384, PER_JOB = 4_096;
    private static final Map<net.minecraft.server.MinecraftServer, Map<ServerPlayer, Job>> JOBS = new java.util.concurrent.ConcurrentHashMap<>();
    public final boolean advanced;
    public OreScannerItem(Properties properties, boolean advanced) { super(properties.stacksTo(1)); this.advanced = advanced; }
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var jobs = JOBS.computeIfAbsent(server, key -> new LinkedHashMap<>());
            int remaining = BUDGET;
            for (Job job : List.copyOf(jobs.values())) {
                if (job.level.getServer() != server) continue;
                if (!job.valid()) { jobs.remove(job.player); continue; }
                if (remaining <= 0) break;
                int allowance = Math.min(remaining, PER_JOB); remaining -= allowance;
                if (job.step(allowance)) { job.finish(); jobs.remove(job.player); }
                else { jobs.remove(job.player); jobs.put(job.player, job); }
            }
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> JOBS.remove(server));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> { var jobs = JOBS.get(server); if (jobs != null) jobs.remove(handler.player); });
    }
    @Override public long baseCapacity() { return CAPACITY; }
    @Override public boolean isBarVisible(ItemStack stack) { return true; }
    @Override public int getBarWidth(ItemStack stack) { return PoweredToolItem.barWidth(stack); }
    @Override public int getBarColor(ItemStack stack) { return PoweredToolItem.BAR_COLOR; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext c, TooltipDisplay d, java.util.function.Consumer<Component> out, TooltipFlag flag) {
        out.accept(PoweredToolItem.energyLine(stack)); ToolUpgrades.appendTooltip(stack, out);
        out.accept(Component.literal(advanced ? "Detailed distance and compass bearing. Shift-use: detailed / wide." : "Approximate distance to sampled resources."));
        out.accept(Component.literal("Use to survey; results show loaded coverage and broad depth. No idle drain."));
        out.accept(Component.literal("Mining-machine slot: " + (advanced ? "4" : "2") + "-block lateral search; 100 JE per depth survey."));
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer p) || !(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var jobs = JOBS.computeIfAbsent(server.getServer(), key -> new LinkedHashMap<>());
        if (!p.isAlive() || p.isSpectator() || jobs.containsKey(p)) return InteractionResult.FAIL;
        if (p.isShiftKeyDown() && (!advanced || p.getLastClientInput().sprint())) {
            SurveyPayload previous = SurveyPayload.saved(stack, level.registryAccess());
            if (previous != null) ServerPlayNetworking.send(p, previous);
            else p.sendOverlayMessage(Component.literal("No saved survey yet."));
            return InteractionResult.SUCCESS;
        }
        if (advanced && p.isShiftKeyDown()) {
            boolean wide = !EquipmentSettings.enabled(stack, "scanner_detail");
            EquipmentSettings.setNumber(stack, "scanner_detail", wide ? 1 : 0);
            p.sendOverlayMessage(Component.literal(wide ? "Advanced Scanner: detailed 3x3" : "Advanced Scanner: wide 5x5"));
            return InteractionResult.SUCCESS;
        }
        if (p.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (jobs.size() >= 8) { p.sendOverlayMessage(Component.literal("Survey instruments are busy; try again shortly.")); return InteractionResult.FAIL; }
        boolean wide = advanced && !EquipmentSettings.enabled(stack, "scanner_detail");
        long cost = !advanced ? QUICK_COST : wide ? WIDE_COST : DETAIL_COST;
        if (Chargeable.energy(stack) < cost) { p.sendOverlayMessage(Component.literal("Survey needs " + cost + " JE.")); return InteractionResult.FAIL; }
        jobs.put(p, new Job(server, p, stack, hand, advanced, wide, cost));
        p.getCooldowns().addCooldown(stack, wide ? 200 : advanced ? 100 : 60);
        p.sendOverlayMessage(Component.literal("Surveying loaded terrain..."));
        return InteractionResult.SUCCESS;
    }
    /** Inclusive lower / exclusive upper bound, intentionally coarse for the ordinary scanner. */
    public static int[] distanceBand(double distance, boolean advanced) {
        if (advanced) { int low = (int) Math.floor(distance / 8) * 8; return new int[]{low, low + 8}; }
        if (distance < 16) return new int[]{0, 16};
        if (distance < 32) return new int[]{16, 32};
        if (distance < 64) return new int[]{32, 64};
        if (distance < 128) return new int[]{64, 128};
        int low = (int) Math.floor(distance / 128) * 128; return new int[]{low, low + 128};
    }
    public static String bearing(double dx, double dz) {
        if (dx * dx + dz * dz < 4) return "below/above";
        String[] names = {"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
        return names[Math.floorMod((int) Math.round(Math.atan2(dz, dx) / (Math.PI / 4)), 8)];
    }
    private static final class Job {
        final ServerLevel level; final ServerPlayer player; final ItemStack stack; final InteractionHand hand;
        final boolean advanced, wide; final long cost; final BlockPos origin; final Vec3 eye;
        final int radius, stride, bottom, top, columns, height, diameter;
        final int[] counts = new int[OreSurvey.FAMILIES.size()];
        final long[] heights = new long[counts.length];
        final double[] nearest = new double[counts.length]; final BlockPos[] targets = new BlockPos[counts.length];
        final Map<Block, Integer> families = new IdentityHashMap<>(); final Set<Long> covered = new HashSet<>();
        int cursor; long started;
        Job(ServerLevel level, ServerPlayer player, ItemStack stack, InteractionHand hand, boolean advanced, boolean wide, long cost) {
            this.level = level; this.player = player; this.stack = stack; this.hand = hand; this.advanced = advanced; this.wide = wide; this.cost = cost;
            origin = player.blockPosition(); eye = player.position(); radius = wide ? 2 : 1; diameter = radius * 2 + 1;
            stride = advanced && !wide ? 1 : wide ? 4 : 2; columns = 16 / stride;
            bottom = level.getMinY(); top = Math.min(level.getMaxY() - 1, origin.getY() + OreSurvey.ABOVE); height = top - bottom + 1;
            Arrays.fill(nearest, Double.POSITIVE_INFINITY); started = level.getGameTime();
        }
        boolean valid() { return player.isAlive() && !player.isSpectator() && player.level() == level && player.getItemInHand(hand) == stack && level.getGameTime() - started <= 600; }
        boolean step(int budget) {
            int perChunk = columns * columns * height, total = diameter * diameter * perChunk;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int n = 0; n < budget && cursor < total; n++, cursor++) {
                int chunk = cursor / perChunk;
                int cx = (origin.getX() >> 4) - radius + chunk / diameter, cz = (origin.getZ() >> 4) - radius + chunk % diameter;
                if (!level.hasChunk(cx, cz)) { cursor = (chunk + 1) * perChunk - 1; continue; }
                covered.add(((long) cx << 32) ^ (cz & 0xffffffffL));
                int local = cursor % perChunk, column = local / height;
                pos.set((cx << 4) + column / columns * stride, bottom + local % height, (cz << 4) + column % columns * stride);
                BlockState state = level.getBlockState(pos); if (state.isAir()) continue;
                int family = families.computeIfAbsent(state.getBlock(), block -> {
                    for (int i = 0; i < counts.length; i++) if (state.is(OreSurvey.FAMILIES.get(i).tag())) return i;
                    return -1;
                });
                if (family < 0) continue;
                int weight = OreSurvey.FAMILIES.get(family).name().startsWith("bulk_") ? io.github.jimbozoomer.jugcraft.deposit.Deposits.remaining(level, pos) : 1;
                if (weight <= 0) continue;
                counts[family] += weight; heights[family] += (long) pos.getY() * weight;
                double distance = Vec3.atCenterOf(pos).distanceToSqr(eye);
                if (distance < nearest[family]) { nearest[family] = distance; targets[family] = pos.immutable(); }
            }
            if (level.getGameTime() % 20 == 0 && cursor < total) player.sendOverlayMessage(Component.literal("Surveying loaded terrain: " + cursor * 100L / Math.max(1,total) + "%"));
            return cursor >= total;
        }
        void finish() {
            if (!valid()) return;
            if (covered.isEmpty()) { player.sendOverlayMessage(Component.literal("No loaded terrain could be surveyed. No energy spent.")); return; }
            if (!Chargeable.drain(stack, cost)) { player.sendOverlayMessage(Component.literal("Survey cancelled: insufficient charge to complete it.")); return; }
            var readings = new ArrayList<OreSurvey.Reading>();
            double sampledArea = Math.max(1, covered.size() * (double) columns * columns);
            for (int i = 0; i < counts.length; i++) if (counts[i] > 0) {
                var family = OreSurvey.FAMILIES.get(i); boolean bulk = family.name().startsWith("bulk_");
                double normalized = counts[i] * (9 * 64D / sampledArea) / (bulk ? 250 : 1);
                int strength = normalized < 4 ? 1 : normalized < 16 ? 2 : normalized < 48 ? 3 : normalized < 128 ? 4 : 5;
                int y = (int) (heights[i] / counts[i]), low = Math.floorDiv(y, 32) * 32;
                int[] band = distanceBand(Math.sqrt(nearest[i]), advanced);
                BlockPos target = targets[i]; String direction = advanced ? bearing(target.getX() + .5 - eye.x, target.getZ() + .5 - eye.z) : "";
                if (direction.equals("below/above")) direction = target.getY() < eye.y ? "below" : "above";
                readings.add(new OreSurvey.Reading(family.icon(), strength, y >= 40 ? 0 : y >= 0 ? 1 : 2,
                    bulk ? "prospector.jugcraft." + family.name() : "", band[0], band[1], direction, advanced ? low : 0, advanced ? low + 31 : 0));
            }
            OreSurvey.oil(level, origin.getX() >> 4, origin.getZ() >> 4, radius, net.minecraft.util.RandomSource.create(origin.asLong() ^ diameter), readings);
            for (int i=0;i<readings.size();i++) {
                var reading=readings.get(i); if(!reading.label().contains("oil"))continue;
                boolean shale=reading.label().contains("shale");double best=Double.POSITIVE_INFINITY,dxBest=0,dzBest=0;
                for(int cx=(origin.getX()>>4)-radius;cx<=(origin.getX()>>4)+radius;cx++)for(int cz=(origin.getZ()>>4)-radius;cz<=(origin.getZ()>>4)+radius;cz++){
                    if(!level.hasChunk(cx,cz))continue;
                    var reservoir=io.github.jimbozoomer.jugcraft.chemistry.OilReservoirs.get(level,new net.minecraft.world.level.ChunkPos(cx,cz));
                    if(reservoir.remaining()<=0 || !(shale?reservoir.kind().name().equals("SHALE"):reservoir.kind().name().equals("CONVENTIONAL")))continue;
                    double dx=Math.clamp(eye.x,cx*16+.5,cx*16+15.5)-eye.x,dz=Math.clamp(eye.z,cz*16+.5,cz*16+15.5)-eye.z;
                    double d=dx*dx+dz*dz;if(d<best){best=d;dxBest=dx;dzBest=dz;}
                }
                if(Double.isFinite(best)){
                    int[] band=distanceBand(Math.sqrt(best),advanced);String direction=advanced?bearing(dxBest,dzBest):"";
                    if(direction.equals("below/above"))direction="below";
                    readings.set(i,new OreSurvey.Reading(reading.icon(),reading.signal(),reading.depth(),reading.label(),band[0],band[1],direction,0,0));
                }
            }
            readings.sort(Comparator.comparingInt(OreSurvey.Reading::signal).reversed());
            var result = new SurveyPayload(readings, advanced ? 2 : 1, diameter, covered.size(), origin, level.getGameTime(), level.dimension().identifier().toString());
            result.save(stack, level.registryAccess()); ServerPlayNetworking.send(player, result);
            EquipmentSettings.setNumber(stack, "last_survey", (int) Math.min(Integer.MAX_VALUE, level.getGameTime()));
        }
    }
}
