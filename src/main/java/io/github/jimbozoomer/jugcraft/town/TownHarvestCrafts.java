package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

/** Small, versioned harvest scenes on existing lawns. Never rebuilds the town or loads a chunk.
 * The complete scene is checked before placement/removal, including every multiblock part.
 * Saved absolute positions and states keep later layout edits from moving or erasing old scenery.
 * A scene changed by its owner is left intact, including unchanged siblings. */
public final class TownHarvestCrafts {
    public static final int CHECK_TICKS = 100;
    public record Prop(String id, BlockPos pos, Direction facing) {}
    public record Scene(String id, List<Prop> props) {}
    public record Cell(BlockPos pos, BlockState before, BlockState placed) {
        public static final Codec<Cell> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Cell::pos),
                BlockState.CODEC.fieldOf("before").forGetter(Cell::before),
                BlockState.CODEC.fieldOf("placed").forGetter(Cell::placed)
        ).apply(i, Cell::new));
    }
    // Stable scene IDs: change the version for a genuinely new layout, never move an old saved footprint.
    // Lawn pockets beside the church and inn; no placements in the church, stalls, gate roads or feast lanes.
    public static final List<Scene> LAYOUT = List.of(
            new Scene("woodland-v1", List.of(
                    prop("corn_drying_frame", 79, 1, 80, Direction.SOUTH),
                    prop("twined_pack_basket", 81, 1, 80, Direction.SOUTH),
                    prop("wooden_corn_mortar", 79, 1, 81, Direction.EAST),
                    prop("incised_earthenware_pot", 81, 1, 81, Direction.SOUTH))),
            new Scene("photo-lawn-v1", List.of(
                    prop("harvest_photo_backdrop", 111, 1, 83, Direction.SOUTH),
                    prop("prize_pumpkin_display", 110, 1, 82, Direction.SOUTH))),
            new Scene("inn-rest-v1", List.of(
                    prop("oak_settle", 78, 1, 121, Direction.SOUTH),
                    prop("lidded_twined_basket", 81, 1, 121, Direction.SOUTH))),
            new Scene("harvest-craft-v1", List.of(
                    prop("pumpkin_carving_bench", 104, 1, 114, Direction.NORTH),
                    prop("birch_bark_box", 107, 1, 114, Direction.WEST))));

    private static Prop prop(String id, int x, int y, int z, Direction facing) {
        return new Prop(id, new BlockPos(x, y, z), facing);
    }
    private TownHarvestCrafts() {}

    /** Expand from the registered block definition; no second hand-maintained multiblock geometry. */
    public static List<Cell> cells(Scene scene, BlockPos origin) {
        Map<BlockPos, Cell> cells = new LinkedHashMap<>();
        for (Prop prop : scene.props()) {
            var block = BuiltInRegistries.BLOCK.getValue(Jugcraft.id(prop.id()));
            if (block == null || block == Blocks.AIR) throw new IllegalStateException("Unknown harvest prop " + prop.id());
            BlockState state = block.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, prop.facing());
            }
            BlockPos root = origin.offset(prop.pos());
            int parts = block instanceof MultiDecorationBlock multi ? multi.cells().length : 1;
            for (int part = 0; part < parts; part++) {
                BlockPos pos = root;
                BlockState partState = state;
                if (block instanceof MultiDecorationBlock multi) {
                    pos = multi.partPos(root, prop.facing(), part);
                    partState = state.setValue(multi.partProperty(), part);
                }
                if (cells.put(pos, new Cell(pos, Blocks.AIR.defaultBlockState(), partState)) != null) {
                    throw new IllegalStateException("Overlapping harvest props in " + scene.id());
                }
            }
        }
        return List.copyOf(cells.values());
    }

    public static Ledger ledger(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Ledger.TYPE);
    }
    public static void tick(ServerLevel level) {
        if (level != level.getServer().overworld() || level.getServer().getTickCount() % CHECK_TICKS != 0) return;
        BlockPos origin = TownState.get(level).origin();
        if (origin != null) run(level, origin, TownState.get(level).theme(), TownFeast.today(),
                TownFeast.blueprint(level, origin), ledger(level), LAYOUT, false);
    }
    public static List<String> report(ServerLevel level, boolean dryRun) {
        BlockPos origin = TownState.get(level).origin();
        if (origin == null) return List.of("Harvest crafts: this world has no town.");
        List<String> lines = new ArrayList<>();
        lines.add("Harvest crafts: " + TownFeast.today() + (dryRun ? " (preview; nothing changes)" : " (saved status)"));
        if (dryRun) lines.addAll(run(level, origin, TownState.get(level).theme(), TownFeast.today(),
                TownFeast.blueprint(level, origin), ledger(level), LAYOUT, true));
        else ledger(level).entries.values().forEach(e -> lines.add(e.scene() + ": " + e.status() + " " + e.reason()));
        return lines;
    }

    /** Testable pass with explicit date/layout. Existing scenery retires before any new scene is considered. */
    public static List<String> run(ServerLevel level, BlockPos origin, String theme, LocalDate date,
            TownFeast.Baseline baseline, Ledger ledger, List<Scene> layout, boolean dryRun) {
        boolean open = TownFeast.inSeason(theme, date);
        List<String> report = new ArrayList<>();
        for (Entry e : List.copyOf(ledger.entries.values())) {
            if (!e.status().equals("placed")) continue;
            boolean current = open && e.year() == date.getYear() && e.origin().equals(origin)
                    && layout.stream().anyMatch(s -> s.id().equals(e.scene()));
            if (current) continue;
            String problem = removalProblem(level, e);
            if (problem != null) {
                report.add(e.scene() + ": " + problem);
                if (!dryRun && !problem.startsWith("waiting:")) ledger.put(e.with("preserved", problem));
                continue;
            }
            if (!dryRun) {
                // Masters first: removing a child first would invoke the normal master-item drop callback.
                // The complete scene was checked before removal, so cleared siblings are still our own cells.
                e.cells().stream().filter(c -> c.placed().getBlock() instanceof MultiDecorationBlock multi
                        && multi.masterPos(c.pos(), c.placed()).equals(c.pos()))
                        .forEach(c -> TownBuilder.set(level, c.pos(), c.before()));
                e.cells().stream().sorted((a, b) -> Integer.compare(b.pos().getY(), a.pos().getY()))
                        .forEach(c -> TownBuilder.set(level, c.pos(), c.before()));
                ledger.put(e.with("removed", "season ended; restored original air"));
            }
            report.add(e.scene() + (dryRun ? ": would take down" : ": taken down"));
        }
        if (!open) return report;
        for (Scene scene : layout) {
            String key = Entry.key(origin, date.getYear(), scene.id());
            Entry previous = ledger.entries.get(key);
            if (previous != null && !previous.status().equals("skipped")) {
                report.add(scene.id() + ": " + previous.status() + "; not replenished");
                continue;
            }
            List<Cell> cells = cells(scene, origin);
            String problem = placementProblem(level, origin, cells, baseline);
            if (problem != null) {
                report.add(scene.id() + ": " + problem);
                if (!dryRun && !problem.startsWith("waiting:")) {
                    ledger.put(new Entry(origin, date.getYear(), scene.id(), "skipped", problem, List.of()));
                }
                continue;
            }
            if (!dryRun) {
                cells.forEach(c -> TownBuilder.set(level, c.pos(), c.placed()));
                ledger.put(new Entry(origin, date.getYear(), scene.id(), "placed", "", cells));
            }
            report.add(scene.id() + (dryRun ? ": would set out" : ": set out"));
        }
        return report;
    }

    private static boolean loaded(ServerLevel level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }
    /** One-block buffer includes the floor, space above, and open doorway of a multiblock. */
    private static Iterable<BlockPos> buffer(List<Cell> cells) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (Cell c : cells) {
            minX = Math.min(minX, c.pos().getX()); maxX = Math.max(maxX, c.pos().getX());
            minY = Math.min(minY, c.pos().getY()); maxY = Math.max(maxY, c.pos().getY());
            minZ = Math.min(minZ, c.pos().getZ()); maxZ = Math.max(maxZ, c.pos().getZ());
        }
        return BlockPos.betweenClosed(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
    }
    public static @Nullable String placementProblem(ServerLevel level, BlockPos origin, List<Cell> cells,
            TownFeast.Baseline baseline) {
        if (cells.isEmpty()) return "skipped: empty scene";
        for (BlockPos p : buffer(cells)) if (!loaded(level, p)) return "waiting: unloaded chunk";
        for (BlockPos p : buffer(cells)) {
            BlockState expected = baseline.expected(p.subtract(origin));
            if (expected == null || baseline.preserved(p)) return "skipped: unknown or owner-preserved blueprint cell";
            if (level.getBlockState(p) != expected || level.getBlockEntity(p) != null) return "skipped: owner changes near scene";
            if (LandRegistry.isClaimed(level, p)) return "skipped: claimed land";
            if (OperatorRegions.protects(level, p)) return "skipped: operator-protected land";
            if (TownUpgrades.fixtureNearby(level, p)) return "skipped: casino furniture nearby";
        }
        for (Cell c : cells) {
            if (!level.getBlockState(c.pos()).isAir()) return "skipped: occupied prop footprint";
            boolean supportedByScene = cells.stream().anyMatch(other -> other.pos().equals(c.pos().below()));
            // Upper arch beams need not have a block directly underneath; their own definition supplies posts.
            boolean upperMulti = c.placed().getBlock() instanceof MultiDecorationBlock multi
                    && multi.masterPos(c.pos(), c.placed()).getY() < c.pos().getY();
            if (!supportedByScene && !upperMulti && !level.getBlockState(c.pos().below()).isSolid()) {
                return "skipped: unsupported prop";
            }
            if (!level.isUnobstructed(c.placed(), c.pos(), CollisionContext.empty())) return "waiting: entity in prop footprint";
        }
        return null;
    }
    private static boolean sameManaged(BlockState actual, BlockState wanted) {
        if (actual.getBlock() != wanted.getBlock()) return false;
        // Snuffing a public lantern is interaction, not a request to keep the whole seasonal scene forever.
        if (actual.hasProperty(BlockStateProperties.LIT)) actual = actual.setValue(BlockStateProperties.LIT, wanted.getValue(BlockStateProperties.LIT));
        return actual == wanted;
    }
    private static @Nullable String removalProblem(ServerLevel level, Entry entry) {
        for (Cell c : entry.cells()) if (!loaded(level, c.pos())) return "waiting: unloaded chunk";
        for (Cell c : entry.cells()) {
            if (!sameManaged(level.getBlockState(c.pos()), c.placed()) || level.getBlockEntity(c.pos()) != null
                    || TownUpgrades.preservedDecor(level, c.pos()) || LandRegistry.isClaimed(level, c.pos())
                    || OperatorRegions.protects(level, c.pos())
                    || TownUpgrades.fixtureNearby(level, c.pos())) return "preserved: scene changed or claimed";
            if (!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(c.pos()).inflate(0.1)).isEmpty()) {
                return "waiting: someone is using the scene";
            }
        }
        return null;
    }

    public record Entry(BlockPos origin, int year, String scene, String status, String reason, List<Cell> cells) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("origin").forGetter(Entry::origin), Codec.INT.fieldOf("year").forGetter(Entry::year),
                Codec.STRING.fieldOf("scene").forGetter(Entry::scene), Codec.STRING.fieldOf("status").forGetter(Entry::status),
                Codec.STRING.optionalFieldOf("reason", "").forGetter(Entry::reason),
                Cell.CODEC.listOf().fieldOf("cells").forGetter(Entry::cells)
        ).apply(i, Entry::new));
        public static String key(BlockPos origin, int year, String scene) { return origin.asLong() + "/" + year + "/" + scene; }
        Entry with(String status, String reason) { return new Entry(origin, year, scene, status, reason, cells); }
    }
    public static final class Ledger extends SavedData {
        public static final Codec<Ledger> CODEC = Entry.CODEC.listOf().xmap(Ledger::new, l -> List.copyOf(l.entries.values()));
        static final SavedDataType<Ledger> TYPE = new SavedDataType<>(Jugcraft.id("town_harvest_crafts"), Ledger::new, CODEC, null);
        private final Map<String, Entry> entries = new LinkedHashMap<>();
        public Ledger() {}
        private Ledger(List<Entry> entries) { entries.forEach(e -> this.entries.put(Entry.key(e.origin(), e.year(), e.scene()), e)); }
        public List<Entry> entries() { return List.copyOf(entries.values()); }
        void put(Entry entry) {
            String key = Entry.key(entry.origin(), entry.year(), entry.scene());
            if (!entry.equals(entries.put(key, entry))) setDirty();
        }
    }
}
