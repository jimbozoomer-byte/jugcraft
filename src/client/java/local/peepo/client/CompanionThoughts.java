package local.peepo.client;

import java.util.*;
import local.peepo.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.*;

/** Private ephemeral events: six visible bubbles per viewer, no distant cache or replay after reconnect. */
public final class CompanionThoughts {
    public static final int LIMIT=6;
    private static final Map<UUID,CompanionFeedbackPayload> ACTIVE=new LinkedHashMap<>();
    private static Object world;
    private static long nextSound;
    private CompanionThoughts(){}
    public static void initialize(){
        ClientPlayNetworking.registerGlobalReceiver(CompanionFeedbackPayload.TYPE,(payload,context)->receive(payload));
        ClientTickEvents.END_CLIENT_TICK.register(client->prune());
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->clear());
    }
    public static void clear(){ACTIVE.clear();world=null;nextSound=0;}
    private static void prune(){
        var client=Minecraft.getInstance();
        if(world!=client.level){clear();world=client.level;}
        if(client.level==null)return;
        long now=client.level.getGameTime();
        ACTIVE.values().removeIf(p->now-p.started()>=CompanionFeedbackRules.DURATION || now<p.started()-2
            || !(client.level.getEntity(p.entity()) instanceof PeepoEntity entity) || !entity.getUUID().equals(p.uuid())
            || !entity.isAlive() || entity.isInvisible() || client.player==null || entity.distanceToSqr(client.player)>256);
    }
    public static void receive(CompanionFeedbackPayload event){
        prune();var client=Minecraft.getInstance();if(client.level==null)return;
        var kind=CompanionFeedbackRules.Kind.from(event.kind());
        if(kind==CompanionFeedbackRules.Kind.NONE){ACTIVE.remove(event.uuid());return;}
        long now=client.level.getGameTime();
        if(!(client.level.getEntity(event.entity()) instanceof PeepoEntity entity) || !entity.getUUID().equals(event.uuid())
            || !entity.isAlive() || entity.isInvisible() || client.player==null || entity.distanceToSqr(client.player)>256
            || event.started()>now+2 || now-event.started()>=CompanionFeedbackRules.DURATION)return;
        if(!ACTIVE.containsKey(event.uuid()) && ACTIVE.size()>=LIMIT)return;
        var old=ACTIVE.get(event.uuid());if(old!=null && old.started()>=event.started())return;
        ACTIVE.put(event.uuid(),event);
        if(event.audible() && kind.problem() && now>=nextSound){
            client.level.playLocalSound(entity.getX(),entity.getY(),entity.getZ(),SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.NEUTRAL,.18F,1.35F,false);
            nextSound=now+80;
        }
    }
    public static CompanionFeedbackPayload get(PeepoEntity npc){
        var event=ACTIVE.get(npc.getUUID());
        return event!=null && event.entity()==npc.getId() && npc.level().getGameTime()-event.started()<CompanionFeedbackRules.DURATION?event:null;
    }
    public static int size(){return ACTIVE.size();}
}
