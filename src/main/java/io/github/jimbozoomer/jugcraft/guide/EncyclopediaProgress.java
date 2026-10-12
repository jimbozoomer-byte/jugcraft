package io.github.jimbozoomer.jugcraft.guide;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.ArrayList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Read-only personal goals. Vanilla advancements own completion, persistence and criteria; no claims or rewards. */
public final class EncyclopediaProgress {
    public static final int LIMIT=512;
    private static List<Identifier> goals=List.of();
    public record Request() implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(Jugcraft.id("encyclopedia_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Request> CODEC=StreamCodec.unit(new Request());
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record Goal(String id, int completed, int total, boolean done, boolean available) {
        public static final StreamCodec<RegistryFriendlyByteBuf,Goal> CODEC=StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256),Goal::id,ByteBufCodecs.VAR_INT,Goal::completed,
            ByteBufCodecs.VAR_INT,Goal::total,ByteBufCodecs.BOOL,Goal::done,ByteBufCodecs.BOOL,Goal::available,Goal::new);
    }
    public record Snapshot(List<Goal> goals) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE=new Type<>(Jugcraft.id("encyclopedia_progress"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Snapshot> CODEC=StreamCodec.composite(
            Goal.CODEC.apply(ByteBufCodecs.list(LIMIT)),Snapshot::goals,Snapshot::new);
        public Snapshot {goals=List.copyOf(goals);}
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(){
        PayloadTypeRegistry.serverboundPlay().register(Request.TYPE,Request.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Snapshot.TYPE,Snapshot.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(EncyclopediaProgress::reload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((s,r,ok)->{if(ok)reload(s);});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->goals=List.of());
        ServerPlayNetworking.registerGlobalReceiver(Request.TYPE,(p,c)->{
            if(RateGate.allow(c.player(),"encyclopedia",20)) ServerPlayNetworking.send(c.player(),snapshot(c.player()));
        });
    }
    private static void reload(MinecraftServer server){
        var resource=server.getResourceManager().getResource(Jugcraft.id("encyclopedia/quests.json"));
        goals=List.of();if(resource.isEmpty())return;
        try(var reader=resource.get().openAsReader()){
            char[] buffer = new char[1_048_577];
            int length = reader.read(buffer,0,buffer.length);
            var text = new StringBuilder();
            while(length>0){text.append(buffer,0,length);if(text.length()>1_048_576)throw new IllegalArgumentException("Goal catalog exceeds 1 MiB");length=reader.read(buffer,0,buffer.length);}
            var json=JsonParser.parseString(text.toString());
            var loaded=new ArrayList<Identifier>();
            for(var entry:json.getAsJsonObject().getAsJsonArray("advancements")){
                if(loaded.size()>=LIMIT)break;
                var id=Identifier.tryParse(entry.getAsString());
                if(id!=null && id.getNamespace().equals(Jugcraft.MOD_ID) && !loaded.contains(id))loaded.add(id);
            }
            goals=List.copyOf(loaded);
        }catch(Exception e){Jugcraft.LOGGER.error("Could not load Encyclopedia goal catalog",e);}
    }
    public static Snapshot snapshot(ServerPlayer player){
        var result=new ArrayList<Goal>();
        for(var id:goals){
            var holder=player.level().getServer().getAdvancements().get(id);
            if(holder==null){result.add(new Goal(id.toString(),0,0,false,false));continue;}
            var progress=player.getAdvancements().getOrStartProgress(holder);
            int complete=0,total=0;
            for(var ignored:progress.getCompletedCriteria()){complete++;total++;}
            for(var ignored:progress.getRemainingCriteria())total++;
            result.add(new Goal(id.toString(),complete,total,progress.isDone(),true));
        }
        return new Snapshot(result);
    }
}
