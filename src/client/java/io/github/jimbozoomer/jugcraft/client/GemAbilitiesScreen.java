package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.concordance.assay.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

public final class GemAbilitiesScreen extends Screen {
    public GemAbilitiesScreen(){super(Component.translatable("screen.jugcraft.gem.title"));}
    public static void register(){
        var key=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.gem_abilities",InputConstants.UNKNOWN.getValue(),PartyClient.CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(c->{while(key.consumeClick())if(c.player!=null&&c.gui.screen()==null)c.gui.setScreen(new GemAbilitiesScreen());});
    }
    @Override protected void init(){int x=(width-280)/2,y=(height-210)/2;
        for(int i=0;i<4;i++){final int action=i;
            var button=addRenderableWidget(new TransmutationScreen.ActionButton(x+57,y+43+i*30,206,Component.translatable("screen.jugcraft.gem.action_"+i),b->{ClientPlayNetworking.send(new GemAbilities.Action(action));onClose();}));
            var slot=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}[i];
            var suffix=new String[]{"helmet","chestplate","leggings","boots"}[i];
            var piece=minecraft.player==null?net.minecraft.world.item.ItemStack.EMPTY:minecraft.player.getItemBySlot(slot);
            button.active=piece.is(ExtendedAlchemyEquipment.ITEMS.get("gem_"+suffix))&&piece.has(AlchemicalGear.ACTIVE);
            button.setTooltip(Tooltip.create(Component.translatable("screen.jugcraft.gem.help_"+i)));
        }
        addRenderableWidget(new TransmutationScreen.ActionButton(x+57,y+173,206,Component.translatable("gui.done"),b->onClose()));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=(width-280)/2,y=(height-210)/2;g.fill(0,0,width,height,0xA00B0B14);OwnerAlchemyArt.frame(g,x,y,280,210);
        g.text(font,title,x+16,y+15,0xFFE5DDEB,false);
        // The supplied legacy rune sheet becomes a decorative armor control rail; it does not claim to add portals.
        for(int i=0;i<4;i++){OwnerAlchemyArt.crop(g,"portaldevice",x+14,y+42+i*30,178+i*14,0,12,12);BookArt.icon(g,"jugcraft:gem_"+new String[]{"helmet","chestplate","leggings","boots"}[i],x+33,y+45+i*30);}
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
}
