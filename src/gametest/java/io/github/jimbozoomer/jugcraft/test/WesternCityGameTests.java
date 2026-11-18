package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.*;
import java.nio.file.Files;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class WesternCityGameTests {
    @GameTest
    public void waterfront_resolves_all_rental_interiors_and_public_workstations(GameTestHelper h) {
        var plan = WesternDistrictPlan.waterfront();
        var origin = new BlockPos(1296,114,400);
        h.assertTrue(plan.waterfront && plan.rooms.size()==72, "Production waterfront exposes 72 apartments");
        var expectedIds = new java.util.HashSet<String>();
        int[] storeys = {8,7,6};
        for (int court=0; court<storeys.length; court++)
            for (int floor=1; floor<storeys[court]; floor++)
                for (String wing : java.util.List.of("L1","L2","R1","R2"))
                    expectedIds.add("west_"+(court+1)+"_"+floor+wing);
        var workstationBlocks = java.util.Map.of(
            new BlockPos(122,-53,71), Blocks.LOOM,
            new BlockPos(125,-67,84), Blocks.SMITHING_TABLE,
            new BlockPos(123,-81,96), Blocks.STONECUTTER,
            new BlockPos(-228,-49,57), Blocks.CARTOGRAPHY_TABLE);
        var probes = new java.util.HashSet<BlockPos>(workstationBlocks.keySet());
        var pierTip = new BlockPos(-262,-50,119);
        probes.add(pierTip);
        for (var room : plan.rooms) {
            h.assertTrue(expectedIds.remove(room.id()), "Unexpected or duplicate room "+room.id());
            var b = room.bounds();
            h.assertTrue(b[0]<b[3] && b[1]+2<=b[4] && b[2]<b[5], "Usable interior "+room.id());
            var door = room.door();
            h.assertTrue(door.getY()==b[1] && door.getZ()>=b[2] && door.getZ()<=b[5] &&
                (door.getX()==b[0]-1 || door.getX()==b[3]+1), "Public door borders the tenant interior "+room.id());
            h.assertTrue(plan.contains(origin,origin.offset(b[0],b[1],b[2])) &&
                plan.contains(origin,origin.offset(b[3],b[4],b[5])), "Both interior corners protected "+room.id());
            probes.add(door);
            probes.add(door.above());
        }
        h.assertTrue(expectedIds.isEmpty(), "Every expected floor and wing exists");
        var found = new java.util.HashMap<BlockPos, BlockState>();
        for (int i=0; i<plan.size(); i++) {
            var pos = plan.local(i);
            if (probes.contains(pos)) found.put(pos,plan.state(i));
        }
        h.assertTrue(found.size()==probes.size(), "Every designated doorway, workstation and pier is built");
        for (var room : plan.rooms) {
            var door = room.door();
            var half = net.minecraft.world.level.block.DoorBlock.HALF;
            h.assertTrue(found.get(door).is(Blocks.SPRUCE_DOOR) && found.get(door.above()).is(Blocks.SPRUCE_DOOR) &&
                found.get(door).getValue(half)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER &&
                found.get(door.above()).getValue(half)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER,
                "Physical two-block entrance "+room.id());
        }
        var fixture = h.absolutePos(new BlockPos(1,1,1));
        for (var entry : workstationBlocks.entrySet()) {
            var state = found.get(entry.getKey());
            h.assertTrue(state.is(entry.getValue()) && plan.contains(origin,origin.offset(entry.getKey())),
                "Registered, protected workstation at "+entry.getKey());
            h.getLevel().setBlockAndUpdate(fixture,state);
            h.assertTrue(state.getMenuProvider(h.getLevel(),fixture)!=null &&
                TownProtection.publicBlock(h.getLevel(),fixture,state), "Working public menu at "+entry.getKey());
        }
        h.assertTrue(found.get(pierTip).is(Blocks.SPRUCE_PLANKS) && plan.contains(origin,origin.offset(pierTip)),
            "Boat landing physically present and protected");
        h.assertTrue(!plan.contains(origin,origin.offset(-264,-52,112)), "Unauthored navigation water remains open");
        h.assertTrue(!plan.contains(origin,origin.offset(87,2,86)), "Deep mine does not claim the city overhead");
        h.succeed();
    }

    @GameTest
    public void blueprint_resolves_real_registry_and_preserves_old_city(GameTestHelper h) {
        var p=WesternDistrictPlan.get();
        h.assertTrue(p.rooms.size()==48 && p.plots.size()==6,"48 rooms and six service premises");
        h.assertTrue(p.rooms.stream().map(WesternDistrictPlan.Room::id).distinct().count()==48,"Stable unique room IDs");
        for(int i=0;i<p.size();i++) {
            var at=p.local(i);
            h.assertTrue(!p.state(i).is(Blocks.BARRIER),"No placeholder blocks");
            if(at.getX()>=0) h.assertTrue(at.getY()==0 && at.getX()<=16 && at.getZ()>=94 && at.getZ()<=98,"Old city only has five-wide surface connector");
        }
        h.succeed();
    }
    @GameTest
    public void snapshot_survives_reopen_and_rejects_other_origins_and_versions(GameTestHelper h) throws Exception {
        var path=Files.createTempDirectory("jugcraft-west-journal-").resolve("before.gz");
        try {
            var before=new BlockState[]{Blocks.STONE.defaultBlockState(),Blocks.AIR.defaultBlockState(),Blocks.DIRT.defaultBlockState()};
            WesternDistrictJournal.write(path,new WesternDistrictJournal.Snapshot("fixture-v1",BlockPos.ZERO,before));
            var reopened=WesternDistrictJournal.read(path,"fixture-v1",BlockPos.ZERO,3);
            h.assertTrue(java.util.Arrays.equals(before,reopened.before()),"Exact states persist without runtime registry IDs");
            boolean wrongOrigin=false, wrongPlan=false;
            try { WesternDistrictJournal.read(path,"fixture-v1",BlockPos.ZERO.above(),3); } catch(java.io.IOException expected) { wrongOrigin=true; }
            try { WesternDistrictJournal.read(path,"fixture-v2",BlockPos.ZERO,3); } catch(java.io.IOException expected) { wrongPlan=true; }
            h.assertTrue(wrongOrigin && wrongPlan,"Wrong-site or changed-plan journal refused");
        } finally { Files.deleteIfExists(path); Files.deleteIfExists(path.getParent()); }
        h.succeed();
    }
    @GameTest
    public void later_edits_are_preserved_by_apply_and_rollback(GameTestHelper h) {
        var a=Blocks.STONE.defaultBlockState(); var b=Blocks.STONE_BRICKS.defaultBlockState(); var edit=Blocks.CHEST.defaultBlockState();
        h.assertTrue(WesternDistrictJournal.canApply(a,a,b) && WesternDistrictJournal.canApply(b,a,b),"Fresh and resumed placement supported");
        h.assertTrue(!WesternDistrictJournal.canApply(edit,a,b) && !WesternDistrictJournal.canRollback(edit,a,b),"Owner chest preserved both ways");
        h.assertTrue(WesternDistrictJournal.canRollback(a,a,b) && WesternDistrictJournal.canRollback(b,a,b),"Rollback repeat safe");
        h.assertTrue(!WesternDistrict.surveyTerrain(edit) && !WesternDistrict.surveyTerrain(Blocks.STONE_BRICKS.defaultBlockState()),"Builds do not become natural terrain");
        h.assertTrue(WesternDistrict.surveyTerrain(Blocks.GRASS_BLOCK.defaultBlockState()) && WesternDistrict.surveyTerrain(Blocks.DIRT.defaultBlockState()),"Ordinary grass and dirt are eligible for reviewed excavation even when supplied tags omit them");
        h.succeed();
    }
    @GameTest
    public void underground_protection_is_bounded_and_does_not_move_origin(GameTestHelper h) {
        var p=WesternDistrictPlan.get(); var origin=new BlockPos(1296,100,400);
        h.assertTrue(p.contains(origin,origin.offset(-102,-25,105)),"Undercity main floor included");
        h.assertTrue(!p.contains(origin,origin.offset(96,-25,96)),"Old town caves not absorbed");
        h.assertTrue(!p.contains(origin,origin.offset(-102,-45,105)),"Caves below district not absorbed");
        h.assertTrue(!p.contains(origin,origin.offset(-300,0,105)),"Unbuilt western land excluded");
        h.assertTrue(!p.contains(origin,origin.offset(-178,-25,140)),"Unmodified caves beside the service hall are not annexed");
        var authored=new java.util.HashSet<Long>();
        for(int i=0;i<p.size();i++)authored.add(p.local(i).asLong());
        boolean gapChecked=false;
        for(int x=-180;x<-16 && !gapChecked;x++)for(int z=18;z<175 && !gapChecked;z++) {
            boolean below=false;
            for(int y=-28;y<0;y++) {
                boolean here=authored.contains(new BlockPos(x,y,z).asLong());
                if(here)below=true;
                else if(below && authored.contains(new BlockPos(x,0,z).asLong())) {
                    h.assertTrue(!p.contains(origin,origin.offset(x,y,z)),"Unauthored vertical gap must stay outside district protection");
                    gapChecked=true;break;
                }
            }
        }
        h.assertTrue(gapChecked,"Fixture includes an unmodified gap between surface and underground cells");
        h.succeed();
    }
}
