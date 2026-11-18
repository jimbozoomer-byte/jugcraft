package io.github.jimbozoomer.jugcraft.town;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Compatibility fixture for a waterfront installed before the two guild shops shipped. */
public final class GuildShopRetrofitFixture {
    private GuildShopRetrofitFixture() {}

    public static void legacySurvey(ServerLevel level) {
        if(WesternDistrict.survey(level.getServer().createCommandSourceStack().withSuppressedOutput(),true,false)!=1)
            throw new AssertionError("Could not prepare the pre-guild waterfront fixture");
    }

    private static int run(ServerLevel level,String command) {
        var server=level.getServer();
        try {
            return server.getCommands().getDispatcher().execute("jugcraft town expansion "+command,
                server.createCommandSourceStack().withSuppressedOutput());
        } catch(CommandSyntaxException e) { throw new AssertionError(e); }
    }

    public static void expectLayout(ServerLevel level,BlockPos origin,boolean revised) {
        for(var edit:DeepsteadShopPlan.get().changes)
            if(level.getBlockState(origin.offset(edit.local()))!=(revised?edit.replacement():edit.previous()))
                throw new AssertionError((revised?"Missing guild shop":"Unexpected change to prior waterfront")+" at "+edit.local());
    }

    // Mimic a save where only the masonry shop's chunks have reached storage.
    private static boolean savedAfterWrite(DeepsteadShopPlan.Change edit) {
        return edit.local().getZ()<80;
    }

    public static void stageInterruptedSave(ServerLevel level,BlockPos origin) {
        var changes=DeepsteadShopPlan.get().changes;
        if(changes.stream().allMatch(GuildShopRetrofitFixture::savedAfterWrite)
                || changes.stream().noneMatch(GuildShopRetrofitFixture::savedAfterWrite))
            throw new AssertionError("The interruption fixture must contain both revisions");
        for(var edit:changes) if(!savedAfterWrite(edit)) TownBuilder.set(level,origin.offset(edit.local()),edit.previous());
        var last=origin.offset(changes.getLast().local());
        TownBuilder.set(level,last,Blocks.CHEST.defaultBlockState());
        if(!(level.getBlockEntity(last) instanceof ChestBlockEntity chest)) throw new AssertionError("Missing test chest");
        chest.setItem(0,new ItemStack(Items.EMERALD,7));
        chest.setChanged();
    }

    public static void markInterruptedOnDisk(Path status) {
        // Only invoked after closing this disposable test save. Production reloads this file on reopen.
        try {
            var record=JsonParser.parseString(Files.readString(status)).getAsJsonObject();
            if(!record.get("phase").getAsString().equals("installed") || record.get("openingGuilds").getAsBoolean()
                    || !DeepsteadShopPlan.get().hash.equals(record.get("guildHash").getAsString()))
                throw new AssertionError("Expected the exact shop revision from a completed waterfront");
            record.addProperty("openingGuilds",true);
            Files.writeString(status,record.toString());
        } catch(IOException e) {throw new AssertionError("Cannot prepare interrupted test save",e);}
    }

    private static void expectInterrupted(ServerLevel level,BlockPos origin) {
        var changes=DeepsteadShopPlan.get().changes;
        var last=changes.getLast();
        for(var edit:changes) {
            var pos=origin.offset(edit.local());
            if(edit==last) {
                if(!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)
                        || !chest.getItem(0).is(Items.EMERALD) || chest.getItem(0).getCount()!=7)
                    throw new AssertionError("Lost later player chest at "+pos);
            } else if(level.getBlockState(pos)!=(savedAfterWrite(edit)?edit.replacement():edit.previous())) {
                throw new AssertionError("Unexpected write during interrupted retrofit at "+pos);
            }
        }
    }

    public static void resumeInterruptedSave(ServerLevel level,BlockPos origin,String token) {
        expectInterrupted(level,origin);
        if(WesternDistrict.guildShopsOpen(level) || run(level,"guildshops apply")!=0)
            throw new AssertionError("An interrupted install must refuse a third state anywhere in its footprint");
        expectInterrupted(level,origin);
        var last=DeepsteadShopPlan.get().changes.getLast();
        var pos=origin.offset(last.local());
        ((ChestBlockEntity)level.getBlockEntity(pos)).clearContent();
        TownBuilder.set(level,pos,last.previous());
        if(run(level,"guildshops apply")!=1 || !WesternDistrict.guildShopsOpen(level))
            throw new AssertionError("The mixed old/new footprint did not resume from its persisted marker");
        expectLayout(level,origin,true);
        DistrictResidentsGameTests.guildKeepers(level,origin,token,true);
    }

    public static void rejectConflictAndInstall(ServerLevel level,BlockPos origin,String token) {
        expectLayout(level,origin,false);
        DistrictResidentsGameTests.guildKeepers(level,origin,token,false);
        var roster=TownState.get(level);
        var previousResidents=new java.util.HashMap<Integer,java.util.UUID>();
        for(var post:DistrictResidents.POSTS) if(post.id()<=19)
            previousResidents.put(post.rosterKey(),roster.townsperson(post.rosterKey()));
        var changes=DeepsteadShopPlan.get().changes;
        // A conflict near the end of the batch must leave even the first stone unlaid.
        var last=changes.getLast();
        var marker=origin.offset(last.local());
        TownBuilder.set(level,marker,Blocks.CHEST.defaultBlockState());
        if(!(level.getBlockEntity(marker) instanceof ChestBlockEntity chest)) throw new AssertionError("Missing owner chest");
        chest.setItem(0,new ItemStack(Items.DIAMOND,3));
        if(run(level,"guildshops apply")!=0 || WesternDistrict.guildShopsOpen(level))
            throw new AssertionError("A conflicting owner chest failed to stop the upgrade");
        for(var edit:changes) if(!edit.local().equals(last.local()) && level.getBlockState(origin.offset(edit.local()))!=edit.previous())
            throw new AssertionError("Failed preflight changed "+edit.local());
        if(level.getBlockEntity(marker)!=chest || !chest.getItem(0).is(Items.DIAMOND) || chest.getItem(0).getCount()!=3)
            throw new AssertionError("Conflicting owner inventory damaged");
        DistrictResidentsGameTests.guildKeepers(level,origin,token,false);
        chest.clearContent();
        TownBuilder.set(level,marker,last.previous());
        if(run(level,"guildshops")!=1 || WesternDistrict.guildShopsOpen(level))
            throw new AssertionError("Read-only survey should find a clear, unopened site");
        expectLayout(level,origin,false);
        if(run(level,"guildshops apply")!=1 || !WesternDistrict.guildShopsOpen(level))
            throw new AssertionError("Cleared guild shop sites did not open");
        expectLayout(level,origin,true);
        if(run(level,"guildshops apply")!=1) throw new AssertionError("Repeat installation should be a no-op");
        expectLayout(level,origin,true);
        for(var entry:previousResidents.entrySet())
            if(!java.util.Objects.equals(roster.townsperson(entry.getKey()),entry.getValue()))
                throw new AssertionError("Retrofit replaced resident "+entry.getKey());
    }
}
