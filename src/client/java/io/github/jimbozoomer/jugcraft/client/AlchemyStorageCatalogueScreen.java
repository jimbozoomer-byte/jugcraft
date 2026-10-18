package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemyStorageCatalogueMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AlchemyStorageCatalogueScreen extends AbstractContainerScreen<AlchemyStorageCatalogueMenu> {
    public AlchemyStorageCatalogueScreen(AlchemyStorageCatalogueMenu menu,Inventory inventory,Component title){super(menu,inventory,title,220,186);}
    private static Component ui(String key,Object... args){return Component.translatable("screen.jugcraft.alchemy_storage."+key,args);}
    @Override protected void init(){super.init();
        addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+20,topPos+154,78,ui("previous"),b->press(0)));
        addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+122,topPos+154,78,ui("next"),b->press(1)));
    }
    private void press(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;g.fill(0,0,width,height,0xA00B0B14);TransmutationScreen.slab(g,x,y,imageWidth,imageHeight,0xFF747784);
        TransmutationScreen.slab(g,x+3,y+3,imageWidth-6,imageHeight-6,0xFF30333D);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;g.fill(sx-2,sy-2,sx+18,sy+18,slot.index==menu.selected()?0xFFE4C786:0xFF161B25);g.fill(sx,sy,sx+16,sy+16,0xFF535B66);}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.text(font,BookArt.fit(font,title.getString(),185),20,12,0xFFE5DDEB,false);
        g.text(font,ui("catalogue_hint",menu.page()+1,menu.pages()),20,24,0xFFA5BCC7,false);
        var sample=menu.slots.get(menu.selected()).getItem();
        g.text(font,BookArt.fit(font,sample.isEmpty()?"":sample.getHoverName().getString(),180),20,116,0xFFE4C786,false);
        g.text(font,ui("value",menu.dissolve(),menu.form()),20,130,0xFFB9DAD4,false);
        g.text(font,ui("source_"+menu.source()),20,142,0xFFB6B9C8,false);
    }
}
