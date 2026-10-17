package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

/** Runtime crops of the unchanged owner GUI atlases, kept at integer pixel sizes. */
final class OwnerAlchemyArt {
    private OwnerAlchemyArt(){}
    static void crop(GuiGraphicsExtractor g,String atlas,int x,int y,int u,int v,int w,int h){
        g.blit(RenderPipelines.GUI_TEXTURED,Jugcraft.id("textures/gui/alchemy/"+atlas+".png"),x,y,u,v,w,h,w,h,256,256);
    }
    static void slot(GuiGraphicsExtractor g,String atlas,int x,int y,int u,int v){crop(g,atlas,x-1,y-1,u,v,18,18);}
    static void frame(GuiGraphicsExtractor g,int x,int y,int w,int h){
        TransmutationScreen.slab(g,x+3,y+4,w,h,0x70000000);
        TransmutationScreen.slab(g,x,y,w,h,0xFF777B86);
        TransmutationScreen.slab(g,x+3,y+3,w-6,h-6,0xFF30333D);
    }
}
