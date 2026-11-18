package io.github.jimbozoomer.jugcraft.town;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A revision inside the already surveyed mine. The base journal keeps its original cell indices. */
final class DeepsteadShopPlan {
    record Change(BlockPos local, BlockState previous, BlockState replacement) {}
    record Shop(BlockPos merchant, BlockPos entrance) {}
    final List<Change> changes;
    final Map<String,Shop> shops;
    final String hash;

    private DeepsteadShopPlan(List<Change> changes,Map<String,Shop> shops,String hash) {
        this.changes=List.copyOf(changes);
        this.shops=Map.copyOf(shops);
        this.hash=hash;
    }
    private static final class Holder { static final DeepsteadShopPlan PLAN=load(); }
    static DeepsteadShopPlan get() { return Holder.PLAN; }
    private static BlockPos position(JsonArray a) {
        return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());
    }
    private static DeepsteadShopPlan load() {
        try(var resource=DeepsteadShopPlan.class.getResourceAsStream("/data/jugcraft/town/deepstead_guild_shops.json.gz")) {
            if(resource==null) throw new IllegalStateException("Missing Deepstead guild shops");
            byte[] compressed=resource.readAllBytes();
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(compressed));
            try(var reader=new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(compressed)), StandardCharsets.UTF_8)) {
                var json=JsonParser.parseReader(reader).getAsJsonObject();
                if(json.get("version").getAsInt()!=1) throw new IllegalArgumentException("Unknown Deepstead shop version");
                var palette=new ArrayList<BlockState>();
                for(var p:json.getAsJsonArray("palette")) palette.add(WesternDistrictPlan.parse(p.getAsString()));
                var changes=new ArrayList<Change>();
                var positions=new HashSet<BlockPos>();
                for(var entry:json.getAsJsonArray("cells")) {
                    var a=entry.getAsJsonArray(); var at=position(a);
                    if(a.size()!=5 || !positions.add(at)) throw new IllegalArgumentException("Invalid guild shop cell "+at);
                    changes.add(new Change(at,palette.get(a.get(3).getAsInt()),palette.get(a.get(4).getAsInt())));
                }
                var shops=new java.util.HashMap<String,Shop>();
                for(var value:json.getAsJsonArray("shops")) {
                    var s=value.getAsJsonObject();
                    var site=new Shop(position(s.getAsJsonArray("merchant")),position(s.getAsJsonArray("entrance")));
                    if(shops.put(s.get("id").getAsString(),site)!=null) throw new IllegalArgumentException("Duplicate shop");
                }
                if(changes.isEmpty() || !shops.keySet().equals(Set.of("stonewright","copper_kettle")))
                    throw new IllegalArgumentException("Incomplete Deepstead shops");
                return new DeepsteadShopPlan(changes,shops,hash);
            }
        } catch(Exception e) { throw new IllegalStateException("Cannot load the dwarf shop revision",e); }
    }
}
