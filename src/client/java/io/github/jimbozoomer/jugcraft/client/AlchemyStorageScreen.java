package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemyStorageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** A quiet 54-slot cabinet, with its physical bag lock shown beside the player inventory. */
public final class AlchemyStorageScreen extends AbstractContainerScreen<AlchemyStorageMenu> {
    public static final int PANEL_WIDTH=216,PANEL_HEIGHT=232;
    public AlchemyStorageScreen(AlchemyStorageMenu menu,Inventory inv,Component title){super(menu,inv,title,PANEL_WIDTH,PANEL_HEIGHT);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;g.fill(0,0,width,height,0xA00B0B14);
        TransmutationScreen.slab(g,x+3,y+4,imageWidth,imageHeight,0x70000000);
        TransmutationScreen.slab(g,x,y,imageWidth,imageHeight,0xFF747784);
        TransmutationScreen.slab(g,x+3,y+3,imageWidth-6,imageHeight-6,0xFF30333D);
        g.fill(x+20,y+27,x+195,y+137,0xFF20232D);g.fill(x+20,y+147,x+195,y+227,0xFF20232D);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;
            g.fill(sx-1,sy-1,sx+17,sy+17,0xFF151822);g.fill(sx,sy,sx+16,sy+16,0xFF535B66);g.fill(sx,sy+16,sx+17,sy+17,0xFF9DA8BA);
            OwnerAlchemyArt.slot(g,"alchchest",sx,sy,11,3);
            if(slot.index>=54&&slot.getContainerSlot()==menu.lockedSlot())g.fill(sx-2,sy+17,sx+18,sy+19,0xFFE4C786);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.text(font,BookArt.fit(font,title.getString(),180),19,7,0xFFE5DDEB,false);
        g.text(font,BookArt.fit(font,Component.translatable("screen.jugcraft.alchemy_storage.no_nesting").getString(),175),20,18,0xFFA5BCC7,false);
        g.text(font,BookArt.fit(font,Component.translatable("screen.jugcraft.alchemy_storage.inventory").getString(),168),27,138,0xFFCEC5DF,false);
    }
}
