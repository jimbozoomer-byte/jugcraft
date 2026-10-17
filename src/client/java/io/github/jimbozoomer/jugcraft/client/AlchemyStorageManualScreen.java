package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Six short sections rather than an unsearchable wall of text; no network writes or research unlocks. */
public final class AlchemyStorageManualScreen extends Screen {
    private int topic;
    public AlchemyStorageManualScreen(){super(Component.translatable("item.jugcraft.alchemy_manual"));}
    private static Component ui(String key){return Component.translatable("screen.jugcraft.alchemy_storage.manual."+key);}
    @Override protected void init(){
        int x=(width-330)/2,y=(height-220)/2;
        for(int i=0;i<6;i++){final int t=i;addRenderableWidget(new TransmutationScreen.ActionButton(x+13,y+40+i*25,95,ui("title_"+i),b->topic=t));}
        addRenderableWidget(new TransmutationScreen.ActionButton(x+223,y+190,94,ui("close"),b->onClose()));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=(width-330)/2,y=(height-220)/2;g.fill(0,0,width,height,0xB00B0B14);
        TransmutationScreen.slab(g,x,y,330,220,0xFF747784);TransmutationScreen.slab(g,x+3,y+3,324,214,0xFF30333D);
        g.fill(x+119,y+36,x+317,y+181,0xFF20232D);g.fill(x+119,y+36,x+122,y+181,0xFFB7A274);
        g.text(font,title,x+15,y+15,0xFFE5DDEB,false);g.text(font,ui("title_"+topic),x+131,y+47,0xFFE4C786,false);
        int yy=y+65;for(var line:font.split(ui("body_"+topic),172)){g.text(font,line,x+131,yy,0xFFCAD3DF,false);yy+=11;}
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public boolean isPauseScreen(){return false;}
}
