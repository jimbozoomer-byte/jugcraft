package local.peepo;

import java.lang.ref.WeakReference;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;

/** Loaded, playing vanilla jukeboxes only. No chunk tickets, block scans or strong world references in entries. */
public final class CompanionMusic {
    public static final int RANGE=8,MAX_CANDIDATES=64;
    private static final Map<ServerLevel,Map<Long,Map<Integer,LinkedHashMap<BlockPos,Source>>>> LEVELS=new WeakHashMap<>();
    public static final class Source {
        public final BlockPos pos;
        public final Vec3 center;
        public final long started,ends;
        private final WeakReference<JukeboxBlockEntity> box;
        private final JukeboxSong song;
        private boolean active=true;
        private Source(JukeboxBlockEntity box,long started,JukeboxSong song){
            this.box=new WeakReference<>(box);pos=box.getBlockPos().immutable();center=Vec3.atCenterOf(pos);this.started=started;this.song=song;ends=started+song.lengthInTicks();
        }
        public boolean live(ServerLevel level){var be=box.get();return active && be!=null && !be.isRemoved() && be.getLevel()==level && level.getGameTime()<ends;}
        public boolean inRange(Vec3 at){return at.distanceToSqr(center)<=RANGE*RANGE;}
    }
    private CompanionMusic(){}
    /** Called at song changes and once on the first native tick after loading, never once per companion. */
    public static void changed(JukeboxBlockEntity box){
        if(!(box.getLevel() instanceof ServerLevel level))return;
        var player=box.getSongPlayer();var song=player.getSong();
        if(box.isRemoved() || !player.isPlaying() || song==null || player.getTicksSinceSongStarted()>=song.lengthInTicks()){removed(box);return;}
        long start=level.getGameTime()-player.getTicksSinceSongStarted();var pos=box.getBlockPos();
        var buckets=LEVELS.computeIfAbsent(level,l->new HashMap<>());
        var section=buckets.computeIfAbsent(ChunkPos.pack(pos),c->new HashMap<>())
            .computeIfAbsent(pos.getY()>>4,y->new LinkedHashMap<>());
        var old=section.get(pos);
        if(old!=null && old.box.get()==box && old.started==start && old.song==song && old.active)return;
        if(old!=null)old.active=false;
        section.put(pos.immutable(),new Source(box,start,song));
    }
    public static void removed(JukeboxBlockEntity box){
        if(!(box.getLevel() instanceof ServerLevel level))return;
        var buckets=LEVELS.get(level);if(buckets==null)return;var pos=box.getBlockPos();long key=ChunkPos.pack(pos);
        var sections=buckets.get(key);if(sections==null)return;var entries=sections.get(pos.getY()>>4);if(entries==null)return;
        var source=entries.get(pos);if(source==null || source.box.get()!=box)return;
        source.active=false;entries.remove(pos);if(entries.isEmpty())sections.remove(pos.getY()>>4);if(sections.isEmpty())buckets.remove(key);if(buckets.isEmpty())LEVELS.remove(level);
    }
    static void unload(ServerLevel level,ChunkPos chunk){
        var buckets=LEVELS.get(level);if(buckets==null)return;var sections=buckets.remove(chunk.pack());
        if(sections!=null)sections.values().forEach(entries->entries.values().forEach(s->s.active=false));
        if(buckets.isEmpty())LEVELS.remove(level);
    }
    /** At most eight 16-cubed buckets and 64 entries, admitted by CompanionBudget.social at the caller. */
    static Source nearest(ServerLevel level,Vec3 at){
        var buckets=LEVELS.get(level);if(buckets==null)return null;
        var min=BlockPos.containing(at.add(-RANGE,-RANGE,-RANGE));var max=BlockPos.containing(at.add(RANGE,RANGE,RANGE));
        Source best=null;double distance=RANGE*RANGE;int visited=0;
        for(int x=min.getX()>>4;x<=max.getX()>>4;x++)for(int z=min.getZ()>>4;z<=max.getZ()>>4;z++){
            var sections=buckets.get(ChunkPos.pack(x,z));if(sections==null)continue;
            for(int y=min.getY()>>4;y<=max.getY()>>4;y++){
                var entries=sections.get(y);if(entries==null)continue;
                for(var source:entries.values()){
                    if(++visited>MAX_CANDIDATES)return best;
                    if(!source.live(level))continue;double d=at.distanceToSqr(source.center);
                    if(d<=distance){best=source;distance=d;}
                }
            }
        }
        return best;
    }
    public static void initialize(){
        ServerChunkEvents.CHUNK_UNLOAD.register((level,chunk)->unload(level,chunk.getPos()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server->LEVELS.entrySet().removeIf(entry->{
            if(entry.getKey().getServer()!=server)return false;
            entry.getValue().values().forEach(sections->sections.values().forEach(entries->entries.values().forEach(s->s.active=false)));return true;
        }));
    }
}
