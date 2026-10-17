package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.assay.KleinStarMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Reuses the tablet's stone frame, owner sigil and native narrated buttons. No text input. */
public final class KleinStarScreen extends AbstractContainerScreen<KleinStarMenu> {
    private static final Identifier ART=Jugcraft.id("textures/gui/alchemy/transmutation.png");
    private final TransmutationScreen.ActionButton[] buttons=new TransmutationScreen.ActionButton[6];
    public KleinStarScreen(KleinStarMenu menu,Inventory inventory,Component title){super(menu,inventory,title,302,238);}
    private static Component ui(String key,Object...args){return Component.translatable("screen.jugcraft.star."+key,args);}
    @Override protected void init(){
        super.init();
        for(int i=0;i<6;i++){
            final int id=i;
            buttons[i]=addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+18+(i%3)*89,topPos+94+(i/3)*22,88,
                    ui("button_"+i),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}));
            buttons[i].setTooltip(Tooltip.create(ui(i<3?"store_help":"withdraw_help")));
        }
        refresh();
    }
    private void refresh(){for(int i=0;i<6;i++)buttons[i].active=i<3?menu.canStore():menu.canWithdraw();}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;
        g.fill(0,0,width,height,0xA00B0B14);
        TransmutationScreen.slab(g,x+4,y+5,imageWidth,imageHeight,0x70000000);
        TransmutationScreen.slab(g,x,y,imageWidth,imageHeight,0xFF20222B);
        TransmutationScreen.slab(g,x+2,y+2,imageWidth-4,imageHeight-4,0xFF747784);
        TransmutationScreen.slab(g,x+4,y+4,imageWidth-8,imageHeight-8,0xFF353640);
        g.fill(x+14,y+32,x+288,y+90,0xFF22232E);
        g.enableScissor(x+108,y+33,x+194,y+78);
        g.blit(RenderPipelines.GUI_TEXTURED,ART,x+112,y+29,12,20,80,76,80,76,256,256);
        g.disableScissor();
        g.fill(x+19,y+82,x+283,y+88,0xFF141820);
        if(menu.capacity()>0)g.fill(x+20,y+83,x+20+(int)(262L*menu.stored()/menu.capacity()),y+87,0xFF78C8CD);
        if(menu.stored()>0){
            double phase=ConcordanceClientOptions.reducedMotion()?0:(minecraft.level.getGameTime()+delta)*.04;
            BookArt.star(g,x+151+(int)(Math.cos(phase)*27),y+56+(int)(Math.sin(phase)*17),2,0xFFE7D3FF);
        }
        g.fill(x+65,y+147,x+237,y+228,0xFF20222A);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;
            if(slot.index==0)g.fill(sx-3,sy-3,sx+19,sy+19,0xFFD5BCE7);
            g.fill(sx-1,sy-1,sx+17,sy+17,0xFF181B23);g.fill(sx,sy,sx+16,sy+16,0xFF575462);
            g.fill(sx,sy+16,sx+17,sy+17,0xFFC0BCCB);
        }
    }
    private void text(GuiGraphicsExtractor g,Component text,int x,int y,int width,int color){g.text(font,BookArt.fit(font,text.getString(),width),x,y,color,false);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        text(g,ui("title"),18,15,265,0xFFE1DAE9);
        text(g,ui("wallet"),22,39,90,0xFFACA5B8);text(g,Component.literal(String.format("%,d",menu.balance())),22,54,90,0xFFEDDAFF);
        text(g,ui("stored"),195,39,90,0xFFACA5B8);text(g,Component.literal(String.format("%,d",menu.stored())),195,54,90,0xFFB8F0ED);
        text(g,menu.capacity()>0?ui("capacity",String.format("%,d",menu.capacity())):ui("insert"),22,70,262,0xFFC9C6D0);
        text(g,Component.translatable("screen.jugcraft.alchemy.inventory"),70,138,163,0xFFD5D5DE);
    }
}
