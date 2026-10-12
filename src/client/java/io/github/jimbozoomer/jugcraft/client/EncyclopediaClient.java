package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.guide.EncyclopediaProgress;
import java.util.Map;
import java.util.HashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;

public final class EncyclopediaClient {
    private static KeyMapping open;
    private static Map<String,EncyclopediaProgress.Goal> progress=Map.of();
    public static void register(){
        open=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.encyclopedia",InputConstants.KEY_K,PartyClient.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            while(open.consumeClick()) if(client.player!=null && client.gui.screen()==null) open(null);
        });
        ScreenEvents.AFTER_INIT.register((client,screen,w,h)->{
            if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen){
                Screens.getWidgets(screen).add(Button.builder(Component.translatable("screen.jugcraft.encyclopedia.button"),b->open(screen))
                    .bounds(4,4,88,18).build());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(EncyclopediaProgress.Snapshot.TYPE,(packet,context)->{
            var next=new HashMap<String,EncyclopediaProgress.Goal>();for(var goal:packet.goals())next.put(goal.id(),goal);
            progress=Map.copyOf(next);
            if(context.client().gui.screen() instanceof EncyclopediaScreen s)s.refreshRows();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->progress=Map.of());
    }
    public static KeyMapping key(){return open;}
    public static void open(Screen parent){request();Minecraft.getInstance().gui.setScreen(new EncyclopediaScreen(parent));}
    public static void request(){if(ClientPlayNetworking.canSend(EncyclopediaProgress.Request.TYPE))ClientPlayNetworking.send(new EncyclopediaProgress.Request());}
    public static EncyclopediaProgress.Goal progress(String id){return progress.get(id);}
    public static boolean unlocked(String id){var g=progress.get(id);return id.isEmpty() || (g!=null && g.available() && g.done());}
}
