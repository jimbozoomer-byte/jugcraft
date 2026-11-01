package local.peepo.client;

import local.peepo.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

final class PlannerScreen extends Screen {
    private final InteractionHand hand;
    PlannerScreen(InteractionHand hand){super(Component.literal("Companion Planner"));this.hand=hand;}
    protected void init(){
        var stack=minecraft.player.getItemInHand(hand);boolean selected=AssignmentTool.selected(minecraft.level,stack)!=null;
        String[] labels={"Normal assignments","Livestock Management: select two corners","Farmland: select two corners","Orchard: select two corners","Fishing: choose seat, then water","Staff Post: choose furniture or a block","Cancel selection","Clear selected companion"};
        for(int i=0;i<labels.length;i++){final int action=new int[]{0,1,4,5,6,7,2,3}[i];var button=addRenderableWidget(Button.builder(Component.literal(labels[i]),b->{ClientPlayNetworking.send(new PlannerActionPayload(hand.ordinal(),action));onClose();}).bounds(width/2-120,height/2-76+i*20,240,18).build());button.active=selected || action==3;}
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2-60,height/2+90,120,20).build());
    }
    public boolean isPauseScreen(){return false;}
    public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(width/2-130,height/2-116,width/2+130,height/2+118,0xFFC6C6C6);
        g.text(font,title,width/2-120,height/2-106,0xFF303030,false);
        var npc=AssignmentTool.selected(minecraft.level,minecraft.player.getItemInHand(hand));
        String selection=npc==null?"Select a companion first.":"Selected: "+npc.getDisplayName().getString();
        if(font.width(selection)>240)selection=font.plainSubstrByWidth(selection,230)+"...";
        g.text(font,selection,width/2-120,height/2-92,0xFF303030,false);
        super.extractRenderState(g,mx,my,delta);
    }
}
