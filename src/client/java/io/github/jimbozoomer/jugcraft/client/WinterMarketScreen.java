package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.client.ExchangeScreen.Plate;
import io.github.jimbozoomer.jugcraft.town.WinterMarketMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** A spruce-framed stationer's ledger with recipient confirmation on its facing page. */
public final class WinterMarketScreen extends AbstractContainerScreen<WinterMarketMenu> {
    private int selected=-1;
    public WinterMarketScreen(WinterMarketMenu menu,Inventory inv,Component title) {
        super(menu,inv,title,250,168);
    }
    private String tr(String key) { return Component.translatable("screen.jugcraft.market."+key).getString(); }
    @Override protected void init() {
        super.init();
        drawButtons();
    }
    private void button(int x,int y,int w,String label,Plate.Kind kind,Runnable run) {
        addRenderableWidget(new Plate(leftPos+x,topPos+y,w,17,label,kind,b->run.run()));
    }
    private void click(int id) {
        if (minecraft!=null && minecraft.gameMode!=null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);
    }
    private void drawButtons() {
        clearWidgets();
        var v=menu.view;
        for (int i=0;i<v.rows().size();i++) {
            final int row=i;
            button(10,43+18*i,v.mode()==2?230:112,font.plainSubstrByWidth(v.rows().get(i),v.mode()==2?220:102),
                row==selected?Plate.Kind.BRASS:Plate.Kind.ROW,()->{
                    if (v.mode()==2) click(row);
                    else { selected=row; drawButtons(); }
                });
        }
        if (v.mode()!=2) {
            button(128,141,111,tr(v.mode()==0?"inbox":"address_book"),Plate.Kind.TAB,()->click(WinterMarketMenu.SWITCH));
            if (selected>=0 && v.mode()==1) button(130,103,107,tr("unwrap"),Plate.Kind.BRASS,()->click(selected));
            if (selected>=0 && v.mode()==0 && !v.held().isEmpty()) {
                button(130,87,107,tr("post"),Plate.Kind.BRASS,()->click(selected));
                button(130,107,107,tr("carry"),Plate.Kind.WOOD,()->click(WinterMarketMenu.CARRY+selected));
            }
        }
        if (v.page()>0) button(11,141,25,"<",Plate.Kind.WOOD,()->click(WinterMarketMenu.PREV));
        if (v.page()+1<v.pages()) button(94,141,25,">",Plate.Kind.WOOD,()->click(WinterMarketMenu.NEXT));
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        TradeLook.frame(g,leftPos,topPos,leftPos+imageWidth,topPos+imageHeight);
        g.fill(leftPos+6,topPos+5,leftPos+244,topPos+24,0xff274d40);
        TradeLook.rule(g,leftPos+6,topPos+24,leftPos+244);
        if (menu.view.mode()!=2) g.fill(leftPos+126,topPos+42,leftPos+240,topPos+136,0xffe9d8ad);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        extractBackground(g,mx,my,delta); super.extractRenderState(g,mx,my,delta);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my) {
        var v=menu.view;
        g.text(font,title,10,10,TradeLook.GOLD,false);
        g.text(font,tr(v.mode()==2?"wares":v.mode()==1?"from":"to"),10,30,TradeLook.TAN,false);
        if (v.rows().isEmpty()) {
            int y=49;
            for (var line:font.split(Component.translatable("screen.jugcraft.market."+(v.mode()==1?"no_mail":"no_names")),104)) {
                g.text(font,line,13,y,TradeLook.TAN,false); y+=10;
            }
        }
        if (v.mode()!=2) {
            String note=v.mode()==1 ? tr("unseal_hint") : v.held().isEmpty()?tr("hold_hint"):v.held();
            int y=49;
            for (var line:font.split(Component.literal(note),102)) {
                if (y>=80) break;
                g.text(font,line,131,y,0xff3e2e24,false); y+=10;
            }
            g.text(font,(v.page()+1)+" / "+v.pages(),46,146,TradeLook.TAN,false);
        }
    }
}
