package io.github.jimbozoomer.jugcraft.town;

import com.google.gson.stream.JsonReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

/** Immutable, local-coordinate blueprint, independent of the original town and any world. */
public final class WesternDistrictPlan {
    public record Room(String id, int[] bounds, BlockPos door) {}
    public record Plot(String id, int[] bounds) {}
    private final long[] cells;
    private final short[] states;
    private final BlockState[] palette;
    public final List<Room> rooms;
    public final List<Plot> plots;
    public final String hash;
    public final boolean waterfront;
    static final int MAX_CELLS = 2_500_000;
    public final int[] bounds;
    private final int columnWidth, columnDepth, minimumY, maximumY;
    private final java.util.BitSet protectedCells;
    private WesternDistrictPlan(long[] cells, short[] states, BlockState[] palette, List<Room> rooms,
            List<Plot> plots, String hash, int[] bounds, boolean waterfront) {
        this.waterfront=waterfront;
        this.cells=cells; this.states=states; this.palette=palette;
        this.rooms=List.copyOf(rooms); this.plots=List.copyOf(plots); this.hash=hash; this.bounds=bounds;
        columnWidth=bounds[3]-bounds[0]+1;
        columnDepth=bounds[5]-bounds[2]+1;
        minimumY=bounds[1]; maximumY=Math.max(bounds[4],4);
        protectedCells=new java.util.BitSet(columnWidth*columnDepth*(maximumY-minimumY+1));
        for(long cell:cells) {
            BlockPos p=BlockPos.of(cell);
            protectedCells.set(protectionIndex(p.getX(),p.getY(),p.getZ()));
            // Only the five-wide road connector reserves otherwise unauthored
            // walking headroom. Unmodified gaps above caves/piers remain unclaimed.
            if(!waterfront && p.getX()>=-15)for(int y=1;y<=4;y++)
                protectedCells.set(protectionIndex(p.getX(),y,p.getZ()));
        }
    }
    private static final class Holder { static final WesternDistrictPlan PLAN=load(false); }
    private static final class WaterfrontHolder { static final WesternDistrictPlan PLAN=load(true); }
    private static final class GuildHolder { static final WesternDistrictPlan PLAN=waterfront().withGuildShops(); }
    public static WesternDistrictPlan get() { return Holder.PLAN; }
    public static WesternDistrictPlan waterfront() { return WaterfrontHolder.PLAN; }
    public static WesternDistrictPlan waterfrontWithShops() { return GuildHolder.PLAN; }

    /** Replace states at existing indices so old surveys and before-images retain their meaning. */
    private WesternDistrictPlan withGuildShops() {
        var pending=new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<DeepsteadShopPlan.Change>();
        for(var change:DeepsteadShopPlan.get().changes) pending.put(change.local().asLong(),change);
        var revised=states.clone();
        var colors=new ArrayList<>(Arrays.asList(palette));
        for(int i=0;i<cells.length;i++) {
            var change=pending.remove(cells[i]);
            if(change==null) continue;
            if(state(i)!=change.previous())
                throw new IllegalArgumentException("Shop revision does not match the base city at "+change.local());
            int index=colors.indexOf(change.replacement());
            if(index<0) { index=colors.size(); colors.add(change.replacement()); }
            revised[i]=(short)index;
        }
        if(!pending.isEmpty()) throw new IllegalArgumentException("Guild shop extends beyond the surveyed town");
        return new WesternDistrictPlan(cells,revised,colors.toArray(BlockState[]::new),rooms,plots,hash,bounds,true);
    }
    public int size() { return cells.length; }
    public BlockPos local(int index) { return BlockPos.of(cells[index]); }
    public BlockState state(int index) { return palette[Short.toUnsignedInt(states[index])]; }
    /** Restore the surveyed landscape bottom-up, sealing tunnels before removing their lining. */
    int[] restorationOrder(BlockState[] before) {
        int height=bounds[4]-bounds[1]+1;
        int[] starts=new int[height*3];
        for(int i=0;i<before.length;i++) starts[restoreBucket(i,before[i],height)]++;
        int offset=0;
        for(int b=0;b<starts.length;b++) {
            int count=starts[b]; starts[b]=offset; offset+=count;
        }
        int[] order=new int[before.length];
        for(int i=0;i<before.length;i++) order[starts[restoreBucket(i,before[i],height)]++]=i;
        return order;
    }
    private int restoreBucket(int i,BlockState state,int height) {
        int phase=state.isAir()?2:state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock?1:0;
        return phase*height+BlockPos.getY(cells[i])-bounds[1];
    }
    public boolean contains(BlockPos origin, BlockPos pos) {
        int x=pos.getX()-origin.getX(), y=pos.getY()-origin.getY(), z=pos.getZ()-origin.getZ();
        if(x<bounds[0] || x>bounds[3] || z<bounds[2] || z>bounds[5] || y<minimumY || y>maximumY)return false;
        return protectedCells.get(protectionIndex(x,y,z));
    }
    private int protectionIndex(int x,int y,int z) {
        return x-bounds[0]+columnWidth*(z-bounds[2]+columnDepth*(y-minimumY));
    }

    public static BlockState parse(String text) {
        try { return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK,text,false).blockState(); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid western district state: "+text,e); }
    }
    private static int[] ints(JsonReader in, int count) throws Exception {
        int[] a=new int[count]; in.beginArray(); for(int i=0;i<count;i++) a[i]=in.nextInt(); in.endArray(); return a;
    }
    private static WesternDistrictPlan load(boolean waterfront) {
        String name=waterfront?"western_waterfront":"western_district";
        try (var resource=WesternDistrictPlan.class.getResourceAsStream("/data/jugcraft/town/"+name+".json.gz")) {
            if(resource==null) throw new IllegalStateException("Missing western district blueprint");
            byte[] packed=resource.readNBytes(32*1024*1024);
            if(resource.read()!=-1) throw new IllegalStateException("Western blueprint exceeds budget");
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(packed));
            try(var in=new JsonReader(new InputStreamReader(new GZIPInputStream(new java.io.ByteArrayInputStream(packed)),StandardCharsets.UTF_8))) {
                var palette=new ArrayList<BlockState>(); var rooms=new ArrayList<Room>(); var plots=new ArrayList<Plot>();
                long[] cells=new long[MAX_CELLS]; short[] states=new short[cells.length]; int count=0; int[] bounds=null;
                boolean safe=false; int version=0;
                in.beginObject();
                while(in.hasNext()) switch(in.nextName()) {
                    case "version" -> version=in.nextInt();
                    case "automatic" -> safe=!in.nextBoolean();
                    case "bounds" -> {
                        bounds=ints(in,6);
                        if(bounds[0]<-350 || bounds[3]>192 || bounds[1]<-160 || bounds[4]>80
                            || bounds[2]<-16 || bounds[5]>208 || bounds[3]<bounds[0] || bounds[4]<bounds[1] || bounds[5]<bounds[2])
                            throw new IllegalStateException("Invalid district extent");
                    }
                    case "palette" -> { in.beginArray(); while(in.hasNext()) palette.add(parse(in.nextString())); in.endArray(); }
                    case "cells" -> {
                        in.beginArray(); while(in.hasNext()) {
                            if(count==cells.length) throw new IllegalStateException("Too many western cells");
                            int[] c=ints(in,4);
                            if(bounds==null || c[0]<bounds[0] || c[0]>bounds[3] || c[1]<bounds[1] || c[1]>bounds[4] || c[2]<bounds[2] || c[2]>bounds[5]
                                || c[3]<0 || c[3]>=palette.size() || c[3]>4095) throw new IllegalStateException("Cell outside western envelope");
                            cells[count]=new BlockPos(c[0],c[1],c[2]).asLong(); states[count++]=(short)c[3];
                        } in.endArray();
                    }
                    case "rooms", "plots" -> {
                        in.beginArray(); while(in.hasNext()) {
                            String id=""; int[] b=null; BlockPos door=null; in.beginObject();
                            while(in.hasNext()) switch(in.nextName()) {
                                case "id" -> id=in.nextString(); case "bounds" -> b=ints(in,6);
                                case "door" -> { int[] a=ints(in,3); door=new BlockPos(a[0],a[1],a[2]); }
                                default -> in.skipValue();
                            }
                            in.endObject(); if(b==null || id.isEmpty()) throw new IllegalStateException("Invalid room/plot");
                            if(door==null) plots.add(new Plot(id,b)); else rooms.add(new Room(id,b,door));
                        } in.endArray();
                    }
                    default -> in.skipValue();
                }
                in.endObject();
                if(!safe || version!=1 || count==0 || bounds==null || rooms.size()!=(waterfront?72:48)
                    || rooms.stream().map(Room::id).distinct().count()!=rooms.size()) throw new IllegalStateException("Invalid western blueprint contract");
                return new WesternDistrictPlan(Arrays.copyOf(cells,count),Arrays.copyOf(states,count),palette.toArray(BlockState[]::new),rooms,plots,hash,bounds,waterfront);
            }
        } catch(Exception e) { throw new IllegalStateException("Cannot load western district",e); }
    }
}
