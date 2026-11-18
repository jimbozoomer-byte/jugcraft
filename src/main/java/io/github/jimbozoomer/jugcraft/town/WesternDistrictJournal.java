package io.github.jimbozoomer.jugcraft.town;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Durable before-image. Runtime registry numeric IDs are never stored on disk. */
public final class WesternDistrictJournal {
    public record Snapshot(String hash, BlockPos origin, BlockState[] before) {}
    private WesternDistrictJournal() {}
    public static void write(Path path, Snapshot snapshot) throws IOException {
        var palette=new LinkedHashMap<BlockState,Integer>();
        for(var state:snapshot.before()) palette.computeIfAbsent(state,k->palette.size());
        Path tmp=path.resolveSibling(path.getFileName()+".tmp"); Files.createDirectories(path.getParent());
        try(var out=new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(tmp)))) {
            out.writeUTF("JUGCRAFT-WEST-1"); out.writeUTF(snapshot.hash()); out.writeLong(snapshot.origin().asLong());
            out.writeInt(snapshot.before().length); out.writeInt(palette.size());
            for(var state:palette.keySet()) out.writeUTF(BlockStateParser.serialize(state));
            for(var state:snapshot.before()) out.writeInt(palette.get(state));
        }
        durableMove(tmp,path);
    }
    public static Snapshot read(Path path, String hash, BlockPos origin, int count) throws IOException {
        if(Files.size(path)>32*1024*1024) throw new IOException("Oversized western before-image");
        try(var in=new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            if(!in.readUTF().equals("JUGCRAFT-WEST-1") || !in.readUTF().equals(hash)
                || in.readLong()!=origin.asLong() || in.readInt()!=count) throw new IOException("Survey belongs to another plan or town");
            int n=in.readInt(); if(n<1 || n>65536 || count<1 || count>WesternDistrictPlan.MAX_CELLS) throw new IOException("Invalid snapshot palette");
            BlockState[] palette=new BlockState[n], before=new BlockState[count];
            for(int i=0;i<n;i++) palette[i]=WesternDistrictPlan.parse(in.readUTF());
            for(int i=0;i<count;i++) { int p=in.readInt(); if(p<0||p>=n) throw new IOException("Bad snapshot index"); before[i]=palette[p]; }
            if(in.read()!=-1) throw new IOException("Trailing snapshot data");
            return new Snapshot(hash,origin,before);
        } catch(IllegalArgumentException e) { throw new IOException("Unrecognized saved block state",e); }
    }
    static void durableMove(Path tmp, Path target) throws IOException {
        try(var channel=FileChannel.open(tmp,StandardOpenOption.WRITE)) { channel.force(true); }
        for(int attempt=0;;attempt++) {
            try {
                Files.move(tmp,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch(AccessDeniedException e) {
                // On Windows a briefly open destination can prevent atomic replacement.
                if(attempt==3 || Thread.currentThread().isInterrupted()) throw e;
                java.util.concurrent.locks.LockSupport.parkNanos(5_000_000L);
            }
        }
    }
    /** Three-way comparison: never overwrite an unrelated later edit. */
    public static boolean canApply(BlockState actual, BlockState before, BlockState after) {
        return actual==before || actual==after;
    }
    public static boolean canRollback(BlockState actual, BlockState before, BlockState after) {
        return actual==after || actual==before;
    }
}
