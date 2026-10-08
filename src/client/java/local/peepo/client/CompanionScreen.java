package local.peepo.client;

import local.peepo.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Inventory stays visible while the right panel switches between assignments and routine preferences. */
public final class CompanionScreen extends AbstractContainerScreen<CompanionMenu> {
    private final Button[] buttons=new Button[9],remove=new Button[CompanionAssignments.COUNT],moveUp=new Button[5],moveDown=new Button[5],settings=new Button[8];
    private final Button[] transport=new Button[8];
    private static final String[] MODES={"Follow","Stay","Home","Work"};
    private int panel; // Jobs and routine. Transport controls live on each job row.
    private Button tab;
    public CompanionScreen(CompanionMenu menu,Inventory inventory,Component title){super(menu,inventory,title,CompanionMenu.WIDTH,312);}
    private Button button(String text,int x,int y,int width,int height,int id){
        return addRenderableWidget(Button.builder(Component.literal(text),b->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+x,topPos+y,width,height).build());
    }
    private void tip(Button b,String text){b.setTooltip(Tooltip.create(Component.literal(text)));}
    @Override protected void init(){
        super.init();inventoryLabelX=80;inventoryLabelY=218;
        tab=addRenderableWidget(Button.builder(Component.literal("Routine >"),b->{panel=1-panel;updateButtons();}).bounds(leftPos+imageWidth-84,topPos+6,76,16).build());
        for(int i=0;i<8;i++)transport[i]=button("",254+(i%2)*80,CompanionMenu.assignmentY(1+i/2),78,16,50+i);
        for(int i=0;i<4;i++)buttons[i]=button(MODES[i],100+i*53,34,50,18,i);
        buttons[6]=button("-",100,56,22,18,6);buttons[7]=button("+",125,56,22,18,7);
        buttons[8]=button("Party",210,56,102,18,8);
        tip(buttons[8],"Only the owner can allow party members to give commands.");
        for(int i=0;i<CompanionAssignments.COUNT;i++){
            remove[i]=button("x",imageWidth-19,CompanionMenu.assignmentY(i)+2,11,12,20+i);
            tip(remove[i],i==0?"Clear home":i==5?"Clear assigned lunch source":i==6?"Clear Supply":i==7?"Clear Output":"Clear work "+i);
            if(i>0 && i<5){
                moveUp[i]=button("\u2191",imageWidth-43,CompanionMenu.assignmentY(i)+2,11,12,30+(i-1)*2);
                moveDown[i]=button("\u2193",imageWidth-31,CompanionMenu.assignmentY(i)+2,11,12,31+(i-1)*2);
                tip(moveUp[i],"Move up: higher work priority");tip(moveDown[i],"Move down: lower work priority");
            }
        }
        settings[0]=button("Schedule",100,78,212,18,40);
        settings[1]=button("Alerts",100,99,103,18,41);
        settings[2]=button("Food",207,99,105,18,42);
        settings[3]=button("Carry meals",100,159,212,18,43);
        settings[4]=button("-",263,120,22,16,44);settings[5]=button("+",290,120,22,16,45);
        settings[6]=button("-",263,139,22,16,46);settings[7]=button("+",290,139,22,16,47);
        tip(settings[0],"Auto: work whenever energy allows. Day/Night shifts follow Overworld time in every dimension. Sleep remains night-only in the Overworld; other dimensions use seated rest.");
        tip(settings[1],"A short nearby sound when a new problem starts. At most once every 20 seconds.");
        tip(settings[2],"Best meal favors stronger energy regeneration. Small meal favors cheaper, weaker meals. Only cargo slots are used.");
        tip(settings[3],"Keep 0-4 meals in the eight cargo slots. Refill only at your assigned lunch source.");
        tip(settings[4],"Lower the energy percentage at which work pauses.");tip(settings[5],"Raise the energy percentage at which work pauses.");
        tip(settings[6],"Lower the energy needed to resume. Always at least 10 points above the break threshold.");tip(settings[7],"Raise the energy needed to resume.");
        updateButtons();
    }
    private PeepoEntity npc(){return minecraft==null || minecraft.player==null?null:menu.companion(minecraft.player);}
    private void updateButtons(){
        menu.showRecipes=panel==0;
        tab.setMessage(Component.literal(panel==0?"Routine >":"< Jobs"));
        for(var setting:settings)setting.visible=panel==1;
        var npc=npc();
        for(int i=0;i<8;i++){
            int value=menu.value(31+i),mode=Math.min(2,value&3),reason=value>>2;
            var b=transport[i];b.visible=panel==0;b.active=npc!=null && npc.assignments.view().get(1+i/2)!=null && reason!=3;
            b.setMessage(Component.literal((i%2==0?"Supply: ":"Output: ")+new String[]{"Auto","On","Off"}[mode]).withColor(reason==2?0xFFCC66:reason==1?0xAAAAAA:0xFFFFFF));
            String state=switch(reason){case 1->"Disabled by you.";case 2->"External item automation detected; Peepo yields this direction.";case 3->"This station has no companion item transport for this direction.";case 4->"No available, loaded station.";case 5->"Choose a ghost recipe in Jobs before supplying ingredients.";default->"Peepo may handle this direction when there is work to do.";};
            tip(b,state+" Click to cycle Auto / On / Off. Auto recognizes connected transport and recent machine transfers. On overrides detection; Off stops this direction. Speed assistance is unchanged.");
        }
        for(int i=0;i<CompanionAssignments.COUNT;i++){remove[i].visible=panel==0;remove[i].active=npc!=null && npc.assignments.view().get(i)!=null;}
        for(int i=1;i<5;i++){
            moveUp[i].visible=moveDown[i].visible=panel==0;
            moveUp[i].active=remove[i].active && i>1 && remove[i-1].active;
            moveDown[i].active=remove[i].active && i<4 && remove[i+1].active;
        }
        settings[0].setMessage(Component.literal("Schedule: "+CompanionPreferences.SCHEDULES[Math.clamp(menu.value(18),0,2)]));
        settings[1].setMessage(Component.literal(menu.value(23)==1?"Alerts: On":"Alerts: Off"));
        settings[2].setMessage(Component.literal(CompanionPreferences.FOODS[Math.clamp(menu.value(21),0,1)]));
        settings[3].setMessage(Component.literal("Carry meals: "+menu.value(22)+" (have "+menu.value(24)+")"));
        settings[4].active=menu.value(19)>0;settings[5].active=menu.value(19)<Math.min(70,menu.value(20)-10);
        settings[6].active=menu.value(20)>menu.value(19)+10;settings[7].active=menu.value(20)<95;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        for(int i=0;i<4;i++)buttons[i].setMessage(Component.literal(MODES[i]).withColor(menu.value(0)==i?0x80FF80:0xFFFFFF));
        buttons[6].active=menu.value(3)>4;buttons[7].active=menu.value(3)<16;buttons[8].active=menu.value(5)==1;
        buttons[8].setMessage(Component.literal(menu.value(4)==1?"Party: Allowed":"Owner only"));
        updateButtons();
        extractBackground(g,mouseX,mouseY,delta);super.extractRenderState(g,mouseX,mouseY,delta);extractTooltip(g,mouseX,mouseY);
        int row=assignmentRow(mouseY-topPos);var npc=npc();
        if(panel==0 && npc!=null && mouseX>=leftPos+100 && mouseX<leftPos+(row>0 && row<5?253:imageWidth-20) && mouseY>=topPos+78 && row>=0 && row<CompanionAssignments.COUNT){
            var target=npc.assignments.view().get(row);
            String text=target==null?"Use the Companion Planner to select this companion, then right-click a "+(row==0?"bed.":row==5?"lunch crate or lunch cover.":row==6?"container for Supply.":row==7?"container while holding Shift for Output.":"workstation."):
                target.name()+" at "+target.at().pos().toShortString()+" in "+target.at().dimension().identifier()+" - "+CompanionStatus.from(menu.value(CompanionMenu.assignmentData(row))).label;
            if(row>0 && row<5 && target!=null)text+=". Priority "+row+" (top is highest).";
            g.setTooltipForNextFrame(font,Component.literal(text),mouseX,mouseY);
        }
        if(panel==0)for(int i=0;i<4;i++){
            var slot=menu.getSlot(CompanionMenu.RECIPE_START+i);
            if(slot.isActive() && mouseX>=leftPos+slot.x && mouseX<leftPos+slot.x+16 && mouseY>=topPos+slot.y && mouseY<topPos+slot.y+16){
                var lines=new java.util.ArrayList<Component>();
                if(slot.hasItem()){
                    lines.add(slot.getItem().getHoverName());
                    var lore=slot.getItem().get(net.minecraft.core.component.DataComponents.LORE);
                    if(lore!=null)lines.addAll(lore.lines());
                }else lines.add(Component.literal(menu.value(25+i)==2?"Choose a pie to enable supplies.":"Choose a recipe to enable supplies."));
                lines.add(Component.literal("Click with a finished item to set a ghost recipe."));
                lines.add(Component.literal("Your item stays on the cursor. Right-click to clear."));
                if(menu.value(25+i)==2)lines.add(Component.literal("Clearing stops new pies; an existing pie can still finish."));
                else lines.add(Component.literal("Click the same output again to cycle matching recipes."));
                g.setTooltipForNextFrame(font,lines,java.util.Optional.empty(),mouseX,mouseY);
            }
        }

    }
    private int assignmentRow(int y){
        for(int row=0;row<CompanionAssignments.COUNT;row++)if(y>=CompanionMenu.assignmentY(row) && y<CompanionMenu.assignmentY(row)+16)return row;
        return -1;
    }
    private int color(CompanionStatus status){return status.problem()?0xFF994433:status==CompanionStatus.WORKING?0xFF168030:status==CompanionStatus.UNSUPPORTED?0xFF775577:0xFF304030;}
    private String fit(String text,int width){return font.width(text)>width?font.plainSubstrByWidth(text,width-9)+"...":text;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFF373737);g.fill(x+1,y+1,x+imageWidth-2,y+imageHeight-2,0xFFFFFFFF);g.fill(x+3,y+3,x+imageWidth-1,y+imageHeight-1,0xFF555555);g.fill(x+4,y+4,x+imageWidth-4,y+imageHeight-4,0xFFC6C6C6);
        for(var slot:menu.slots){if(!slot.isActive())continue;int sx=x+slot.x,sy=y+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xFFFFFFFF);g.fill(sx-1,sy-1,sx+16,sy+16,0xFF373737);g.fill(sx,sy,sx+16,sy+16,0xFF8B8B8B);}
        if(panel==0)for(int i=0;i<CompanionAssignments.COUNT;i++)g.fill(x+99,y+CompanionMenu.assignmentY(i),x+(i==0 || i>=5?imageWidth-20:252),y+CompanionMenu.assignmentY(i)+14,0xFFDADADA);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        g.text(font,fit(title.getString(),222),8,8,0xFF404040,false);
        g.text(font,"Energy "+menu.value(1)+"%   Health "+menu.value(2)+"%",8,24,0xFF404040,false);
        g.text(font,"Range: "+menu.value(3),151,61,0xFF404040,false);
        g.text(font,"Costume",8,42,0xFF404040,false);g.text(font,"Hand",66,42,0xFF404040,false);g.text(font,"Storage",16,82,0xFF404040,false);
        g.text(font,playerInventoryTitle,inventoryLabelX,inventoryLabelY,0xFF404040,false);
        var status=CompanionStatus.from(menu.value(11));
        g.text(font,fit(status.label,84),8,140,color(status),false);
        if(panel==1){
            g.text(font,"Break at: "+menu.value(19)+"%",101,124,0xFF404040,false);
            g.text(font,"Resume at: "+menu.value(20)+"%",101,143,0xFF404040,false);return;
        }
        var npc=npc();
        for(int i=0;i<CompanionAssignments.COUNT;i++){
            var target=npc==null?null:npc.assignments.view().get(i);
            var rowStatus=CompanionStatus.from(menu.value(CompanionMenu.assignmentData(i)));
            String prefix=i==0?"Home":i==5?"Lunch":i==6?"Supply":i==7?"Output":"Work "+i;
            String label=prefix+": "+(target==null?"Not assigned":target.name()+" "+target.at().pos().toShortString());
            g.text(font,fit(label,i==0 || i>=5?imageWidth-125:148),101,CompanionMenu.assignmentY(i)+3,color(rowStatus),false);
        }

    }
}
