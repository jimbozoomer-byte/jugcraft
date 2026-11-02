package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.client.ExchangeScreen.Plate;
import io.github.jimbozoomer.jugcraft.town.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Walnut noticeboard with four illustrated request cards and an explicitly selected delivery. */
public final class CommissionScreen extends AbstractContainerScreen<CommissionMenu> implements ScreenKit.Fitted {
    private int selected;
    private Plate deliver,claim;
    public CommissionScreen(CommissionMenu menu,Inventory inventory,Component title){super(menu,inventory,title,300,236);}
    @Override protected void init(){
        super.init();inventoryLabelY=10000;clearWidgets();
        for(int i=0;i<TownCommissions.ORDERS.size();i++){
            final int choice=i;var order=TownCommissions.ORDERS.get(i);
            addRenderableWidget(new Plate(leftPos+12,topPos+32+i*36,114,30,order.patron(),Plate.Kind.WOOD,b->{selected=choice;}));
        }
        deliver=addRenderableWidget(new Plate(leftPos+140,topPos+154,146,20,"Deliver requested items",Plate.Kind.BRASS,b->send(selected)));
        claim=addRenderableWidget(new Plate(leftPos+140,topPos+178,146,20,"Collect Jugs",Plate.Kind.BRASS,b->send(4+selected)));
        updateButtons();
    }
    private void send(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private void updateButtons(){
        var order=TownCommissions.ORDERS.get(selected);
        deliver.active=!menu.paid(selected)&&menu.progress(selected)<order.count()&&menu.carried(selected)>0;
        claim.active=!menu.paid(selected)&&menu.progress(selected)==order.count()&&menu.balance()<=TownShops.get().maxBalance-order.reward();
    }
    @Override protected void containerTick(){super.containerTick();updateButtons();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        TradeLook.frame(g,leftPos,topPos,leftPos+imageWidth,topPos+imageHeight);
        TradeLook.rule(g,leftPos+8,topPos+25,leftPos+292);
        TradeLook.tray(g,leftPos+134,topPos+32,leftPos+290,topPos+150);
        g.fill(leftPos+8,topPos+32+selected*36,leftPos+10,topPos+62+selected*36,TradeLook.GOLD);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        var order=TownCommissions.ORDERS.get(selected);int progress=menu.progress(selected);
        g.text(font,"CITY COMMISSIONS",12,10,TradeLook.GOLD,false);
        String purse=menu.balance()+(menu.balance()==1?" Jug":" Jugs");
        g.text(font,purse,288-font.width(purse),10,TradeLook.CREAM,false);
        g.item(new ItemStack(order.itemType()),142,39);
        g.text(font,ScreenKit.fit(font,new ItemStack(order.itemType()).getHoverName().getString(),116),163,43,TradeLook.CREAM,false);
        int y=64;for(var line:font.split(Component.literal(order.description()),138)){g.text(font,line,142,y,TradeLook.TAN,false);y+=10;}
        g.text(font,"Delivered: "+progress+" / "+order.count(),142,94,TradeLook.CREAM,false);
        g.fill(142,107,282,113,0xFF171E21);
        g.fill(142,107,142+140*Math.min(progress,order.count())/order.count(),113,TradeLook.GAIN);
        g.text(font,"Reward: "+order.reward()+(order.reward()==1?" Jug":" Jugs"),142,120,TradeLook.GOLD,false);
        g.text(font,"Party delivered: "+menu.party(selected),142,134,TradeLook.TAN,false);
        String status=menu.paid(selected)?"Completed for this cycle":(progress==order.count()?"Ready to collect":"In your bag: "+menu.carried(selected));
        g.text(font,ScreenKit.fit(font,status,114),12,180,menu.paid(selected)?TradeLook.GAIN:TradeLook.CREAM,false);
        g.text(font,"Personal quotas. Party totals recognize contributors.",12,204,TradeLook.TAN,false);
        g.text(font,"Next cycle: "+(menu.renewalSeconds()/60)+"m "+(menu.renewalSeconds()%60)+"s. Unfinished orders saved.",12,218,TradeLook.CREAM,false);
    }
    @Override public ScreenRectangle panelArea(){return new ScreenRectangle(leftPos-3,topPos-3,imageWidth+6,imageHeight+6);}
}
