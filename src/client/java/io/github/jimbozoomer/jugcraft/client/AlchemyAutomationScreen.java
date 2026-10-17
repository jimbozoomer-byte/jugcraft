package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemyAutomationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AlchemyAutomationScreen extends AbstractContainerScreen<AlchemyAutomationMenu> {
    private TransmutationScreen.ActionButton active,target;
    public AlchemyAutomationScreen(AlchemyAutomationMenu menu,Inventory inv,Component title){super(menu,inv,title,216,214);}
    private static Component ui(String s,Object... args){return Component.translatable("screen.jugcraft.automation."+s,args);}
    private void press(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override protected void init(){super.init();
        active=addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+91,topPos+43,108,ui("start"),b->press(0)));
        target=addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+91,topPos+68,108,ui("target_2"),b->press(1)));
        addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+91,topPos+93,108,ui("clear"),b->press(2)));
        active.setTooltip(Tooltip.create(ui("help")));target.setTooltip(Tooltip.create(ui("output_help")));refresh();
    }
    private void refresh(){active.setMessage(ui(menu.active()?"stop":"start"));target.visible=!menu.voiding();target.setMessage(ui("target_"+Math.max(0,menu.target())));}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;g.fill(0,0,width,height,0xA00B0B14);OwnerAlchemyArt.frame(g,x,y,imageWidth,imageHeight);
        OwnerAlchemyArt.crop(g,"eternal_density",x+24,y+47,60,24,56,56);
        for(var s:menu.slots)OwnerAlchemyArt.slot(g,"eternal_density",x+s.x,y+s.y,61,25);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.text(font,BookArt.fit(font,title.getString(),188),13,12,0xFFE5DDEB,false);
        g.text(font,ui("filters"),16,29,0xFFE4C786,false);
        g.text(font,ui("hint"),16,116,0xFFBBCBD7,false);
    }
}
