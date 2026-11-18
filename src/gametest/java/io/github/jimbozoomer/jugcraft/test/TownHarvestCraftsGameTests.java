package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownFeast;
import io.github.jimbozoomer.jugcraft.town.TownHarvestCrafts;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/** Runtime migration checks: real multiblock callbacks, save codecs and an explicit seasonal clock. */
public final class TownHarvestCraftsGameTests {
    private static final String ARENA = "jugcraft-test:claim_combat_arena";
    private static final LocalDate OPEN = LocalDate.of(2026, 11, 14), CLOSED = LocalDate.of(2026, 12, 1);
    private static BlockPos origin(GameTestHelper h) { return h.absolutePos(new BlockPos(8, 1, 7)); }
    private static void floor(GameTestHelper h) {
        for (int x = 0; x < 30; x++) for (int z = 0; z < 15; z++) {
            h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            for (int y = 2; y < 7; y++) h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
        }
    }
    private static TownFeast.Baseline baseline(Set<Long> marks) {
        return new TownFeast.Baseline() {
            public BlockState expected(BlockPos p) { return p.getY() == 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(); }
            public boolean preserved(BlockPos p) { return marks.contains(p.asLong()); }
        };
    }
    private static TownHarvestCrafts.Scene scene(String id, Direction facing) {
        return new TownHarvestCrafts.Scene("fixture-v1", List.of(new TownHarvestCrafts.Prop(id, new BlockPos(0, 1, 0), facing)));
    }
    private static List<String> run(GameTestHelper h, TownHarvestCrafts.Ledger ledger,
            TownHarvestCrafts.Scene scene, LocalDate date, boolean dryRun) {
        return TownHarvestCrafts.run(h.getLevel(), origin(h), "harvest", date, baseline(Set.of()), ledger, List.of(scene), dryRun);
    }
    private static TownHarvestCrafts.Ledger saved(TownHarvestCrafts.Ledger ledger) {
        return TownHarvestCrafts.Ledger.CODEC.parse(NbtOps.INSTANCE,
                TownHarvestCrafts.Ledger.CODEC.encodeStart(NbtOps.INSTANCE, ledger).getOrThrow()).getOrThrow();
    }
    private static void assertAir(GameTestHelper h, TownHarvestCrafts.Scene scene) {
        for (var cell : TownHarvestCrafts.cells(scene, origin(h))) h.assertTrue(h.getLevel().getBlockState(cell.pos()).isAir(), "Scene cell remains air");
    }

    @GameTest(structure = ARENA)
    public void preview_save_repeat_and_next_year(GameTestHelper h) {
        floor(h);
        var scene = scene("oak_settle", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        var preview = run(h, ledger, scene, OPEN, true);
        h.assertTrue(preview.stream().anyMatch(s -> s.contains("would set out")), "Preview accepts open ground: " + preview);
        h.assertTrue(ledger.entries().isEmpty(), "Preview does not record ownership");
        assertAir(h, scene);
        var placed = run(h, ledger, scene, OPEN, false);
        h.assertTrue(ledger.entries().size() == 1 && ledger.entries().getFirst().status().equals("placed"), "Scene placed: " + placed);
        ledger = saved(ledger);
        run(h, ledger, scene, OPEN, false);
        h.assertTrue(ledger.entries().size() == 1, "Save/reload and repeat do not duplicate entries");
        run(h, ledger, scene, CLOSED, true);
        h.assertTrue(ledger.entries().getFirst().status().equals("placed"), "Removal preview is read only");
        run(h, ledger, scene, CLOSED, false);
        assertAir(h, scene);
        run(h, ledger, scene, OPEN, false);
        assertAir(h, scene);
        run(h, ledger, scene, OPEN.plusYears(1), false);
        h.assertTrue(ledger.entries().size() == 2, "A new year has its own record");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void owner_blocks_buffers_and_marks_prevent_all_placement(GameTestHelper h) {
        floor(h);
        var scene = scene("harvest_photo_backdrop", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        BlockPos edited = origin(h).offset(1, 2, 0); // Open doorway: not a part, but still within the scene.
        h.getLevel().setBlockAndUpdate(edited, Blocks.CHEST.defaultBlockState());
        var lines = run(h, ledger, scene, OPEN, false);
        h.assertTrue(lines.stream().anyMatch(s -> s.contains("owner changes")), "A chest in the opening rejects the entire frame");
        assertAir(h, scene);
        h.assertTrue(h.getLevel().getBlockState(edited).is(Blocks.CHEST), "Chest preserved");
        h.getLevel().setBlockAndUpdate(edited, Blocks.AIR.defaultBlockState());
        var marked = TownHarvestCrafts.run(h.getLevel(), origin(h), "harvest", OPEN,
                baseline(Set.of(edited.asLong())), ledger, List.of(scene), false);
        h.assertTrue(marked.stream().anyMatch(s -> s.contains("owner-preserved")), "Saved owner mark rejects empty cells too");
        assertAir(h, scene);
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void owner_change_preserves_whole_scene_through_season_end(GameTestHelper h) {
        floor(h);
        var scene = new TownHarvestCrafts.Scene("two-props", List.of(
                new TownHarvestCrafts.Prop("lidded_twined_basket", new BlockPos(0, 1, 0), Direction.SOUTH),
                new TownHarvestCrafts.Prop("wooden_corn_mortar", new BlockPos(2, 1, 0), Direction.SOUTH)));
        var ledger = new TownHarvestCrafts.Ledger();
        run(h, ledger, scene, OPEN, false);
        var cells = TownHarvestCrafts.cells(scene, origin(h));
        h.getLevel().setBlockAndUpdate(cells.getFirst().pos(), Blocks.DIAMOND_BLOCK.defaultBlockState());
        run(h, ledger, scene, CLOSED, false);
        h.assertTrue(h.getLevel().getBlockState(cells.getFirst().pos()).is(Blocks.DIAMOND_BLOCK), "Owner replacement retained");
        h.assertTrue(h.getLevel().getBlockState(cells.getLast().pos()) == cells.getLast().placed(), "Unchanged scene sibling also retained");
        h.assertTrue(ledger.entries().getFirst().status().equals("preserved"), "Preservation recorded persistently");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void all_facings_remove_multiblocks_without_loot(GameTestHelper h) {
        floor(h);
        for (String id : List.of("oak_settle", "harvest_photo_backdrop")) for (Direction facing : Direction.Plane.HORIZONTAL) {
            var scene = scene(id, facing);
            var ledger = new TownHarvestCrafts.Ledger();
            var lines = run(h, ledger, scene, OPEN, false);
            h.assertTrue(ledger.entries().size() == 1 && ledger.entries().getFirst().status().equals("placed"), id + " " + facing + ": " + lines);
            for (var cell : TownHarvestCrafts.cells(scene, origin(h))) {
                h.assertTrue(h.getLevel().getBlockState(cell.pos()) == cell.placed(), "Every actual model part is set out");
            }
            run(h, saved(ledger), scene, CLOSED, false);
            assertAir(h, scene);
        }
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(origin(h)).inflate(12)).isEmpty(), "Seasonal removal never drops a multiblock item");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void public_lantern_toggle_does_not_prevent_cleanup(GameTestHelper h) {
        floor(h);
        var scene = scene("harvest_photo_backdrop", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        run(h, ledger, scene, OPEN, false);
        for (var cell : TownHarvestCrafts.cells(scene, origin(h))) {
            h.getLevel().setBlockAndUpdate(cell.pos(), cell.placed().setValue(BlockStateProperties.LIT, false));
        }
        run(h, ledger, scene, CLOSED, false);
        assertAir(h, scene);
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void recorded_positions_survive_layout_changes(GameTestHelper h) {
        floor(h);
        var old = scene("oak_settle", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        run(h, ledger, old, OPEN, false);
        var moved = new TownHarvestCrafts.Scene("fixture-v2", List.of(
                new TownHarvestCrafts.Prop("oak_settle", new BlockPos(8, 1, 0), Direction.SOUTH)));
        run(h, saved(ledger), moved, OPEN, false);
        assertAir(h, old);
        for (var cell : TownHarvestCrafts.cells(moved, origin(h))) h.assertTrue(h.getLevel().getBlockState(cell.pos()) == cell.placed(), "New revision uses its own footprint");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void operator_regions_prevent_placement_and_seasonal_removal(GameTestHelper h) {
        floor(h);
        var scene = scene("oak_settle", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        var op = TestPlayers.survivor(h);
        BlockPos at = origin(h).above();
        op.snapTo(at.getX(), at.getY(), at.getZ(), 0, 0);
        String name = "harvest_" + op.getUUID().toString().replace("-", "");
        var server = h.getLevel().getServer();
        var source = server.createCommandSourceStack().withEntity(op).withSuppressedOutput();
        try {
            server.getCommands().getDispatcher().execute("protect pos1", source);
            server.getCommands().getDispatcher().execute("protect pos2", source);
            server.getCommands().getDispatcher().execute("protect create " + name, source);
            var result = run(h, ledger, scene, OPEN, false);
            h.assertTrue(result.stream().anyMatch(s -> s.contains("operator-protected")), "Even untouched blueprint air respects a protected operator region");
            assertAir(h, scene);
            server.getCommands().getDispatcher().execute("protect remove " + name, source);
            op.snapTo(at.getX() + 10, at.getY(), at.getZ(), 0, 0);
            run(h, ledger, scene, OPEN, false);
            h.assertTrue(ledger.entries().getFirst().status().equals("placed"), "An unprotected scene can be installed later");
            // Saved selection corners still cover the same scene after the operator steps away.
            server.getCommands().getDispatcher().execute("protect create " + name, source);
            run(h, ledger, scene, CLOSED, false);
            h.assertTrue(ledger.entries().getFirst().status().equals("preserved"), "A new operator region preserves the whole existing scene");
            for (var cell : TownHarvestCrafts.cells(scene, origin(h))) h.assertTrue(h.getLevel().getBlockState(cell.pos()) == cell.placed(), "Protected scene remains intact");
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw new AssertionError("Protection fixture command failed", e);
        } finally {
            try { server.getCommands().getDispatcher().execute("protect remove " + name, source); }
            catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) { throw new AssertionError("Protection fixture cleanup failed", e); }
        }
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void unavailable_chunks_are_not_loaded_or_recorded(GameTestHelper h) {
        var scene = scene("harvest_photo_backdrop", Direction.SOUTH);
        BlockPos far = origin(h).offset(350000, 0, 350000);
        var ledger = new TownHarvestCrafts.Ledger();
        var chunkSource = h.getLevel().getChunkSource();
        h.assertTrue(chunkSource.getChunkNow(far.getX() >> 4, far.getZ() >> 4) == null, "Fixture starts unloaded");
        var lines = TownHarvestCrafts.run(h.getLevel(), far, "harvest", OPEN, baseline(Set.of()), ledger, List.of(scene), false);
        h.assertTrue(lines.stream().anyMatch(s -> s.contains("waiting: unloaded")), "Unloaded scenes wait");
        h.assertTrue(ledger.entries().isEmpty(), "A deferred check does not consume the year's scene");
        h.assertTrue(chunkSource.getChunkNow(far.getX() >> 4, far.getZ() >> 4) == null, "Seasonal checks never generate or load remote terrain");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void a_missing_support_cannot_leave_a_partial_scene(GameTestHelper h) {
        floor(h);
        var scene = scene("oak_settle", Direction.SOUTH);
        var ledger = new TownHarvestCrafts.Ledger();
        BlockPos floor = origin(h);
        h.getLevel().setBlockAndUpdate(floor, Blocks.AIR.defaultBlockState());
        TownFeast.Baseline baseline = new TownFeast.Baseline() {
            public BlockState expected(BlockPos p) {
                return p.equals(BlockPos.ZERO) ? Blocks.AIR.defaultBlockState()
                        : p.getY() == 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }
            public boolean preserved(BlockPos p) { return false; }
        };
        var lines = TownHarvestCrafts.run(h.getLevel(), origin(h), "harvest", OPEN, baseline, ledger, List.of(scene), false);
        h.assertTrue(lines.stream().anyMatch(s -> s.contains("unsupported")), "Open blueprint ground must still support every post");
        assertAir(h, scene);
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void planned_scene_cells_are_open_in_the_real_town(GameTestHelper h) {
        var data = TownData.get();
        var occupied = new java.util.HashSet<BlockPos>();
        for (var scene : TownHarvestCrafts.LAYOUT) for (var cell : TownHarvestCrafts.cells(scene, BlockPos.ZERO)) {
            BlockPos p = cell.pos();
            h.assertTrue(data.index(p.getX(), p.getY(), p.getZ()) == TownData.AIR, scene.id() + " must start in blueprint air: " + p);
            h.assertTrue(occupied.add(p), "New scenes must not overlap");
            for (var site : data.sites) for (int[] b : site.blocks()) {
                h.assertTrue(!p.equals(new BlockPos(b[0], b[1], b[2])), "Do not overlap existing seasonal sites");
            }
            for (var feast : TownFeast.LAYOUT) h.assertTrue(!feast.cells().contains(p), "Feast footprint remains clear");
        }
        h.succeed();
    }
}
