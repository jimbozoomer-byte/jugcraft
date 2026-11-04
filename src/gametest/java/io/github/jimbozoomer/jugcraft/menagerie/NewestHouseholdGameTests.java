package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.test.TestPlayers;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;

/** Inventory conservation and authority checks for the new household adapters. */
public final class NewestHouseholdGameTests {
    private static void clibanoFire(GameTestHelper h,String source,int expected) {
        var level=h.getLevel();var actor=TestPlayers.survivor(h);
        var middle=h.absolutePos(new BlockPos(2,2,2));var front=middle.south();
        var blocks=io.github.jimbozoomer.jugcraft.arcane.ForbiddenArcane.BLOCKS;
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++) {
            var at=middle.offset(x,y,z);
            var state=x==0&&y==0&&z==0?Blocks.AIR.defaultBlockState():
                blocks.get(at.equals(front)?"clibano_core":x!=0&&y!=0&&z!=0?"polished_darkstone":"polished_darkstone_bricks").defaultBlockState();
            level.setBlockAndUpdate(at,state);
        }
        h.assertTrue(io.github.jimbozoomer.jugcraft.arcane.ArcaneClibano.form(actor,front,Direction.SOUTH),"Complete native Clibano forms");
        var machine=(io.github.jimbozoomer.jugcraft.arcane.ArcaneClibano)level.getBlockEntity(middle);
        var input=source.equals("coal")?new ItemStack(Items.COAL,2):new ItemStack(io.github.jimbozoomer.jugcraft.arcane.ForbiddenArcane.ITEMS.get(source),2);
        machine.setItem(1,input);machine.setItem(2,new ItemStack(Items.COAL));machine.setItem(3,new ItemStack(Items.RAW_IRON));
        io.github.jimbozoomer.jugcraft.arcane.ArcaneClibano.tick(level,middle,level.getBlockState(middle),machine);
        h.assertTrue(machine.data(0)>0,"Actual native input starts combustion with ordinary fuel");
        h.assertTrue(machine.data(3)==expected,"Loaded soul tags choose the expected native fire tier: "+source);
        h.assertTrue(machine.getItem(1).getCount()==(expected==0?2:1),"Only a valid soul is consumed when special fire starts");
        h.assertTrue((machine.data(2)>0)==(expected>0),"Special fire duration starts only from a supported soul");
        h.succeed();
    }
    @GameTest public void clibanoOrdinarySoulStartsSoulFire(GameTestHelper h){clibanoFire(h,"soul",1);}
    @GameTest public void clibanoCorruptSoulStartsSoulFire(GameTestHelper h){clibanoFire(h,"corrupt_soul",1);}
    @GameTest public void clibanoEnchantedSoulStartsEnchantedFire(GameTestHelper h){clibanoFire(h,"enchanted_soul",2);}
    @GameTest public void clibanoOrdinaryItemCannotCreateSoulFire(GameTestHelper h){clibanoFire(h,"coal",0);}
    private static OwnerSupplementData node(GameTestHelper h,String kind) {
        var pos=h.absolutePos(new BlockPos(1,2,1));
        h.getLevel().setBlockAndUpdate(pos,OwnerSupplementaries.BLOCKS.get(kind).defaultBlockState());
        return (OwnerSupplementData)h.getLevel().getBlockEntity(pos);
    }
    @GameTest
    public void statueNameAndCargoSurviveRenameSaveAndPacking(GameTestHelper h) {
        var n=node(h,"statue");var item=new ItemStack(Items.DIAMOND);
        item.set(DataComponents.CUSTOM_NAME,Component.literal("Keepsake"));n.setItem(0,item);
        n.setCustomName(Component.literal("Alex"));
        h.assertTrue(n.getCustomName()!=null&&n.getCustomName().getString().equals("Alex"),"Native name setter updates actual name");
        h.assertTrue(n.getItem(0).getCount()==1,"Rename preserves the statue's single displayed item");
        var saved=n.saveWithoutMetadata(h.getLevel().registryAccess());
        var copy=new OwnerSupplementData(n.getBlockPos(),n.getBlockState());
        copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        var packed=copy.packed();var restored=new OwnerSupplementData(n.getBlockPos(),n.getBlockState());restored.restore(packed);
        h.assertTrue(restored.getCustomName()!=null&&restored.getCustomName().getString().equals("Alex"),"Portable statue preserves name");
        h.assertTrue(ItemStack.matches(restored.getItem(0),item),"Portable statue preserves exact cargo components");
        restored.setCustomName(null);h.assertTrue(!restored.hasCustomName()&&ItemStack.matches(restored.getItem(0),item),"Clearing skin name leaves cargo intact");h.succeed();
    }
    @GameTest
    public void foreignClaimCannonCannotBeAimedOrIgnited(GameTestHelper h) {
        var n=node(h,"cannon");var actor=TestPlayers.survivor(h);n.owner=UUID.randomUUID();
        n.setItem(0,new ItemStack(Items.GUNPOWDER,8));n.setItem(1,new ItemStack(Items.COBBLESTONE,4));
        h.assertTrue(OwnerCannon.loaded(n),"Test cannon is loaded");
        var land=io.github.jimbozoomer.jugcraft.finance.LandRegistry.get(h.getLevel().getServer());
        var book=io.github.jimbozoomer.jugcraft.finance.Ledger.get(h.getLevel().getServer()).book();
        var key=io.github.jimbozoomer.jugcraft.finance.LandRegistry.keyOf(n.getBlockPos());
        land.assign(book,key,book.account(n.owner,"CannonOwner"),0);
        try {
            h.assertTrue(!OwnerCannon.aim(n,actor)&&!OwnerCannon.ignite(n,actor),"Foreign claim blocks both operations");
            h.assertTrue(n.cannonFuse==0&&n.getItem(0).getCount()==8&&n.getItem(1).getCount()==4,"Denied action consumes nothing");
        } finally {land.abandon(actor,book,key,true,0);}
        h.succeed();
    }
    @GameTest
    public void cannonLoadSanitizesInvalidAimAndPower(GameTestHelper h) {
        var n=node(h,"cannon");var tag=n.saveWithoutMetadata(h.getLevel().registryAccess());
        tag.putFloat("CannonYaw",Float.NaN);tag.putFloat("CannonPitch",Float.POSITIVE_INFINITY);tag.putInt("CannonPower",Integer.MAX_VALUE);tag.putInt("CannonFuse",Integer.MAX_VALUE);
        n.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),tag));
        h.assertTrue(Float.isFinite(n.cannonYaw)&&Float.isFinite(n.cannonPitch)&&n.cannonPower==4&&n.cannonFuse==OwnerCannon.FUSE,"Saved invalid values remain bounded");h.succeed();
    }
    @GameTest(maxTicks=60)
    public void movingAssemblyPreservesContainerAcrossAnimation(GameTestHelper h) {
        var level=h.getLevel();var actor=TestPlayers.survivor(h);var source=h.absolutePos(new BlockPos(1,2,1));
        var direction=(source.getX()&15)<14?Direction.EAST:Direction.WEST;var target=source.relative(direction);
        level.setBlockAndUpdate(source,Blocks.BARREL.defaultBlockState());level.setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());
        var barrel=(BarrelBlockEntity)level.getBlockEntity(source);var stack=new ItemStack(Items.EMERALD,17);stack.set(DataComponents.CUSTOM_NAME,Component.literal("Assembly cargo"));barrel.setItem(0,stack);
        h.assertTrue(OwnerAssemblyMotion.move(level,actor,List.of(source),direction,4,Map.of(),null),"Loaded authorized assembly starts");
        h.runAfterDelay(12,()->{
            h.assertTrue(level.getBlockState(source).isAir(),"Source removed after movement");
            h.assertTrue(level.getBlockEntity(target) instanceof BarrelBlockEntity,"Destination container restored");
            h.assertTrue(ItemStack.matches(((BarrelBlockEntity)level.getBlockEntity(target)).getItem(0),stack),"Movement conserves count and item components");h.succeed();
        });
    }
    @GameTest
    public void blockedAssemblyDoesNotConsumeOrDetachCargo(GameTestHelper h) {
        var level=h.getLevel();var actor=TestPlayers.survivor(h);var source=h.absolutePos(new BlockPos(1,2,1));var direction=(source.getX()&15)<14?Direction.EAST:Direction.WEST;
        level.setBlockAndUpdate(source,Blocks.BARREL.defaultBlockState());level.setBlockAndUpdate(source.relative(direction),Blocks.BEDROCK.defaultBlockState());
        var barrel=(BarrelBlockEntity)level.getBlockEntity(source);barrel.setItem(0,new ItemStack(Items.DIAMOND,9));
        h.assertTrue(!OwnerAssemblyMotion.move(level,actor,List.of(source),direction,4,Map.of(),null),"Occupied destination refuses move");
        h.assertTrue(level.getBlockEntity(source)==barrel&&barrel.getItem(0).getCount()==9,"Refused move retains original container and cargo");h.succeed();
    }
    @GameTest
    public void decorativePillarsRejoinWhenNeighboursChange(GameTestHelper h) {
        var level=h.getLevel();var p=h.absolutePos(new BlockPos(1,2,1));
        var pillar=io.github.jimbozoomer.jugcraft.arcane.ForbiddenArcane.BLOCKS.get("arcane_polished_darkstone_pillar");
        var state=pillar.defaultBlockState();
        level.setBlockAndUpdate(p,state);level.setBlockAndUpdate(p.above(),state);level.setBlockAndUpdate(p.above(2),state);
        h.assertTrue(level.getBlockState(p).getValue(OwnerDecorVariants.JoinedPillar.TYPE)==OwnerDecorVariants.Segment.BOTTOM,"Stack has original bottom mesh");
        h.assertTrue(level.getBlockState(p.above()).getValue(OwnerDecorVariants.JoinedPillar.TYPE)==OwnerDecorVariants.Segment.MIDDLE,"Stack has original middle mesh");
        level.removeBlock(p.above(),false);
        h.assertTrue(level.getBlockState(p).getValue(OwnerDecorVariants.JoinedPillar.TYPE)==OwnerDecorVariants.Segment.SINGLE,"Removed connection restores single mesh");
        h.succeed();
    }

}
