package local.peepo.client;

import local.peepo.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact player-style storage and command panel, with five explicit assignment rows. */
public final class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private final Button[] buttons=new Button[9],remove=new Button[5];
    private static final String[] MODES={"Follow","Stay","Home","Work"};
    public CompanionScreen(CompanionMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,232);}
    private Button button(String text,int x,int y,int width,int height,int id){return addRenderableWidget(Button.builder(Component.literal(text),b->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+x,topPos+y,width,height).build());}
    @Override protected void init(){
        super.init();inventoryLabelX=80;inventoryLabelY=138;
        for(int i=0;i<4;i++)buttons[i]=button(MODES[i],100+i*53,34,50,18,i);
        buttons[6]=button("-",100,56,22,18,6);buttons[7]=button("+",125,56,22,18,7);
        buttons[8]=button("Party",210,56,102,18,8);
        buttons[8].setTooltip(Tooltip.create(Component.literal("Only the owner can allow party members to give commands.")));
        for(int i=0;i<5;i++){
            remove[i]=button("x",301,78+i*11,11,10,20+i);
            remove[i].setTooltip(Tooltip.create(Component.literal(i==0?"Clear home assignment":"Clear work "+i+" assignment")));
        }
    }
    private PeepoEntity npc(){return minecraft==null || minecraft.player==null?null:menu.companion(minecraft.player);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        for(int i=0;i<4;i++)buttons[i].setMessage(Component.literal(MODES[i]).withColor(menu.value(0)==i?0x80FF80:0xFFFFFF));
        buttons[6].active=menu.value(3)>4;buttons[7].active=menu.value(3)<16;buttons[8].active=menu.value(5)==1;
        buttons[8].setMessage(Component.literal(menu.value(4)==1?"Party: Allowed":"Owner only"));
        var npc=npc();for(int i=0;i<5;i++)remove[i].active=npc!=null && npc.assignments.view().get(i)!=null;
        extractBackground(g,mouseX,mouseY,delta);super.extractRenderState(g,mouseX,mouseY,delta);extractTooltip(g,mouseX,mouseY);
        int row=(mouseY-topPos-78)/11;
        if(npc!=null && mouseX>=leftPos+100 && mouseX<leftPos+300 && mouseY>=topPos+78 && row>=0 && row<5){
            var target=npc.assignments.view().get(row);
            String tip=target==null?"Use a Companion Planner: select this companion, then right-click a "+(row==0?"bed.":"workstation."):
                target.name()+" at "+target.at().pos().toShortString()+" in "+target.at().dimension().identifier()+" - "+status(target,npc);
            g.setTooltipForNextFrame(font,Component.literal(tip),mouseX,mouseY);
        }
    }
    private String status(CompanionAssignments.Target target,PeepoEntity npc){
        if(!target.local(npc.level()))return "Other dimension";
        if(!npc.level().hasChunkAt(target.at().pos()))return "Unloaded";
        if(!target.present(npc.level()))return "Missing/replaced";
        if(CompanionAssignments.bed(npc.level(),target.at().pos()) || npc.level().getBlockState(target.at().pos()).getBlock() instanceof WheelBlock)return "Assigned";
        return "Assigned; work behavior not implemented yet";
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+320,y+232,0xFF373737);g.fill(x+1,y+1,x+318,y+230,0xFFFFFFFF);g.fill(x+3,y+3,x+319,y+231,0xFF555555);g.fill(x+4,y+4,x+316,y+228,0xFFC6C6C6);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xFFFFFFFF);g.fill(sx-1,sy-1,sx+16,sy+16,0xFF373737);g.fill(sx,sy,sx+16,sy+16,0xFF8B8B8B);}
        for(int i=0;i<5;i++)g.fill(x+99,y+77+i*11,x+300,y+87+i*11,0xFFDADADA);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        g.text(font,title,8,8,0xFF404040,false);
        g.text(font,"Energy "+menu.value(1)+"%   Health "+menu.value(2)+"%",8,20,0xFF404040,false);
        g.text(font,"Range: "+menu.value(3),151,61,0xFF404040,false);
        g.text(font,"Costume",8,42,0xFF404040,false);g.text(font,"Hand",66,42,0xFF404040,false);g.text(font,"Storage",16,82,0xFF404040,false);
        g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF404040,false);
        if(menu.value(8)==0)g.text(font,"Unavailable",8,31,0xFF884444,false);
        var npc=npc();
        for(int i=0;i<5;i++){
            var target=npc==null?null:npc.assignments.view().get(i);
            String label=(i==0?"Home: ":"Work "+i+": ");
            if(target==null)label+="Not assigned";
            else {var p=target.at().pos();String coords=" "+p.getX()+","+p.getY()+","+p.getZ();int room=195-font.width(label+coords);String name=target.name();label+=(font.width(name)>room?font.plainSubstrByWidth(name,Math.max(0,room-9))+"...":name)+coords;}
            if(font.width(label)>195)label=font.plainSubstrByWidth(label,186)+"...";
            int color=target!=null && !target.present(npc.level())?0xFF994433:0xFF304030;
            g.text(font,label,101,79+i*11,color,false);
        }
    }
}
