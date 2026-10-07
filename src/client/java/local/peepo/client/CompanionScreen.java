package local.peepo.client;

import local.peepo.CompanionMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Vanilla-style inventory panel with server-validated command buttons. */
public final class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private final Button[] buttons=new Button[9];
    private static final String[] NAMES={"Follow me","Stay","Go home","Work","Set home","Set work","Radius -","Radius +","Party access"};
    public CompanionScreen(CompanionMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,232);}
    @Override protected void init(){
        super.init();inventoryLabelX=80;inventoryLabelY=138;
        int[][] positions={{100,40},{172,40},{244,40},{100,64},{172,64},{244,64},{100,88},{244,88},{100,112}};
        for(int i=0;i<buttons.length;i++){
            final int id=i;buttons[i]=addRenderableWidget(Button.builder(Component.literal(NAMES[i]),b->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+positions[i][0],topPos+positions[i][1],i==8?212:68,20).build());
        }
        buttons[4].setTooltip(Tooltip.create(Component.literal("Use the companion's current position as home.")));
        buttons[5].setTooltip(Tooltip.create(Component.literal("Use the companion's current position as its work center.")));
        buttons[8].setTooltip(Tooltip.create(Component.literal("Only the owner can allow party members to give commands.")));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){
        for(int i=0;i<4;i++)buttons[i].setMessage(Component.literal(NAMES[i]).withColor(menu.value(0)==i?0x80FF80:0xFFFFFF));
        buttons[6].active=menu.value(3)>4;buttons[7].active=menu.value(3)<16;buttons[8].active=menu.value(5)==1;
        buttons[8].setMessage(Component.literal("Party commands: "+(menu.value(4)==1?"Allowed":"Owner only")));
        extractBackground(graphics,mouseX,mouseY,delta);super.extractRenderState(graphics,mouseX,mouseY,delta);extractTooltip(graphics,mouseX,mouseY);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+320,y+232,0xFF373737);g.fill(x+1,y+1,x+318,y+230,0xFFFFFFFF);g.fill(x+3,y+3,x+319,y+231,0xFF555555);g.fill(x+4,y+4,x+316,y+228,0xFFC6C6C6);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xFFFFFFFF);g.fill(sx-1,sy-1,sx+16,sy+16,0xFF373737);g.fill(sx,sy,sx+16,sy+16,0xFF8B8B8B);}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        g.text(font,title,8,8,0xFF404040,false);
        g.text(font,"Energy "+menu.value(1)+"%   Health "+menu.value(2)+"%",8,20,0xFF404040,false);
        g.text(font,"Range: "+menu.value(3),180,94,0xFF404040,false);
        g.text(font,"Costume",8,42,0xFF404040,false);
        g.text(font,"Hand",66,42,0xFF404040,false);
        g.text(font,"Storage",16,82,0xFF404040,false);
        g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF404040,false);
        if(menu.value(8)==0)g.text(font,"Target offline / other dimension",8,30,0xFF884444,false);
    }
}
