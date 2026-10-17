package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Existing silver/stone tablet controls, clear slot purposes and actual server status. */
public final class AlchemyMachinesScreen extends AbstractContainerScreen<AlchemyMachinesMenu> {
    private final TransmutationScreen.ActionButton[] buttons=new TransmutationScreen.ActionButton[3];
    public AlchemyMachinesScreen(AlchemyMachinesMenu menu,Inventory inv,Component title){super(menu,inv,title,348,236);}
    private static Component ui(String key,Object... args){return Component.translatable("screen.jugcraft.alchemy_machine."+key,args);}
    private static String n(long v){return String.format("%,d",v);}
    @Override protected void init(){
        super.init();for(int i=0;i<3;i++){final int action=i;buttons[i]=addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+21+i*103,topPos+126,100,ui(i==0?"pause":i==1?"deposit":"withdraw"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,menu.kind()==AlchemyMachines.Kind.NOVA?3:action);}));}refresh();
    }
    private void refresh(){
        boolean nova=menu.kind()==AlchemyMachines.Kind.NOVA;
        buttons[0].setMessage(ui(nova?"detonate":menu.paused()?"resume":"pause"));buttons[0].active=menu.canOperate();
        buttons[0].setTooltip(Tooltip.create(ui(nova?"nova_help":"pause_help")));
        buttons[1].active=!nova&&menu.canOperate()&&menu.wallet()>0&&menu.essence()<menu.capacity();buttons[2].active=!nova&&menu.canOperate()&&menu.essence()>0&&menu.wallet()<1000000;
        buttons[1].visible=!nova;buttons[2].visible=!nova;
    }
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;g.fill(0,0,width,height,0xA00B0B14);TransmutationScreen.slab(g,x+4,y+5,imageWidth,imageHeight,0x70000000);
        TransmutationScreen.slab(g,x,y,imageWidth,imageHeight,0xFF20222B);TransmutationScreen.slab(g,x+2,y+2,imageWidth-4,imageHeight-4,0xFF747784);TransmutationScreen.slab(g,x+4,y+4,imageWidth-8,imageHeight-8,0xFF353640);
        g.fill(x+16,y+34,x+332,y+124,0xFF20232D);g.fill(x+23,y+39,x+325,y+45,0xFF111820);
        if(menu.capacity()>0)g.fill(x+24,y+40,x+24+(int)(300L*menu.essence()/menu.capacity()),y+44,0xFF79CECC);
        if(menu.kind()==AlchemyMachines.Kind.FURNACE){g.fill(x+185,y+76,x+225,y+79,0xFF596571);int advance=ConcordanceClientOptions.reducedMotion()?17:(int)(minecraft.level.getGameTime()%30)*34/30;if(menu.status()==7)g.fill(x+186+advance,y+75,x+191+advance,y+80,0xFFEFAD6B);}
        else if(menu.kind()==AlchemyMachines.Kind.RELAY){double phase=ConcordanceClientOptions.reducedMotion()?0:(minecraft.level.getGameTime()+delta)*.035;for(int i=0;i<4;i++)BookArt.star(g,x+259+(int)(Math.cos(phase+i*Math.PI/2)*23),y+79+(int)(Math.sin(phase+i*Math.PI/2)*16),2,menu.status()==7?0xFF9CE2DA:0xFF6E788E);}
        if(menu.kind()==AlchemyMachines.Kind.FURNACE)OwnerAlchemyArt.crop(g,menu.tier()==1?"dmfurnace":"rmfurnace",x+187,y+65,menu.tier()==1?72:88,32,24,20);
        if(menu.kind()==AlchemyMachines.Kind.RELAY)OwnerAlchemyArt.crop(g,"relay"+menu.tier(),x+244,y+65,menu.tier()==1?120:menu.tier()==2?137:157,menu.tier()==3?52:38,29,28);
        g.fill(x+89,y+151,x+259,y+232,0xFF22232B);
        for(var slot:menu.slots){if(!slot.isActive())continue;int sx=x+slot.x,sy=y+slot.y;if(slot.index==0)g.fill(sx-3,sy-3,sx+19,sy+19,0xFFAB9BCA);g.fill(sx-1,sy-1,sx+17,sy+17,0xFF131822);g.fill(sx,sy,sx+16,sy+16,0xFF525761);g.fill(sx,sy+16,sx+17,sy+17,0xFFB5BDCB);
            if(menu.kind()==AlchemyMachines.Kind.FURNACE)OwnerAlchemyArt.slot(g,menu.tier()==1?"dmfurnace":"rmfurnace",sx,sy,menu.tier()==1?12:10,7);
            else if(menu.kind()==AlchemyMachines.Kind.RELAY)OwnerAlchemyArt.slot(g,"relay"+menu.tier(),sx,sy,26,16);}
    }
    private void line(GuiGraphicsExtractor g,Component text,int x,int y,int width,int color){g.text(font,BookArt.fit(font,text.getString(),width),x,y,color,false);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        line(g,title,19,13,311,0xFFE4DCEC);line(g,ui("buffer",n(menu.essence()),n(menu.capacity())),19,25,312,0xFFB5DFE2);
        var kind=menu.kind();if(kind!=AlchemyMachines.Kind.NOVA)line(g,ui(kind==AlchemyMachines.Kind.RELAY?"star_charge":"star_supply"),24,54,91,0xFFBDAED3);
        if(kind==AlchemyMachines.Kind.RELAY||kind==AlchemyMachines.Kind.FURNACE)line(g,ui(kind==AlchemyMachines.Kind.RELAY?"dissolve":"smelt"),115,54,102,0xFFE1C39F);
        if(kind==AlchemyMachines.Kind.FURNACE)line(g,ui("output"),239,54,80,0xFFB6E4D0);
        if(kind==AlchemyMachines.Kind.PEDESTAL)line(g,ui("artifact"),115,54,130,0xFFE4C786);
        if(kind==AlchemyMachines.Kind.NOVA)line(g,ui("nova_cost",n(menu.tier()==1?4096:16384)),24,64,295,0xFFE4C786);
        line(g,ui("hint_"+kind.name().toLowerCase(),menu.tier()==1?1:4),24,95,296,0xFFB5C3D3);
        line(g,ui("status_"+menu.status()),19,116,311,menu.status()==7?0xFF9CE2BC:0xFFE2C99D);line(g,ui("wallet",n(menu.wallet())),93,145,237,0xFFCEC5DF);
    }
}
