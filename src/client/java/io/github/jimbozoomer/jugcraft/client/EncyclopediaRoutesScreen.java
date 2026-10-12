package io.github.jimbozoomer.jugcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Suggested specialty connections, deliberately separate from actual recipe and research requirements. */
public final class EncyclopediaRoutesScreen extends Screen {
    private final EncyclopediaScreen parent;
    private record Node(String chapter,int column,int row,String[] from){}
    private static final List<Node> TECH=List.of(
        new Node("Getting Started",1,0,new String[]{}),
        new Node("Materials",0,1,new String[]{"Getting Started"}),new Node("Power",2,1,new String[]{"Getting Started"}),
        new Node("Steel",0,2,new String[]{"Materials"}),new Node("Processing",2,2,new String[]{"Power","Materials"}),
        new Node("Farming",0,3,new String[]{"Processing"}),new Node("Fluids",1,3,new String[]{"Steel","Processing"}),new Node("Logistics",2,3,new String[]{"Processing"}),
        new Node("Renewables",0,4,new String[]{"Farming","Power"}),new Node("Chemistry",1,4,new String[]{"Fluids"}),new Node("Oil",2,4,new String[]{"Fluids"}),
        new Node("Storage",0,5,new String[]{"Power","Chemistry"}),new Node("Electronics",1,5,new String[]{"Chemistry","Processing"}),new Node("Rocketry",2,5,new String[]{"Oil","Chemistry"}),
        new Node("Upgrades",1,6,new String[]{"Electronics"}));
    private static final List<Node> MAGIC=List.of(
        new Node("Foundations",1,0,new String[]{}),new Node("Radiance",1,1,new String[]{"Foundations"}),
        new Node("Hearth",0,2,new String[]{"Radiance"}),new Node("Composition",1,2,new String[]{"Radiance"}),new Node("Invocations",2,2,new String[]{"Radiance"}),
        new Node("Circles",0,3,new String[]{"Radiance"}),new Node("Alembic",1,3,new String[]{"Radiance"}),new Node("Garden",2,3,new String[]{"Radiance"}),
        new Node("Vigil",0,4,new String[]{"Circles"}),new Node("Binding",1,4,new String[]{"Circles"}),new Node("Sky",2,4,new String[]{"Circles"}),
        new Node("Relics",0,5,new String[]{"Vigil"}),new Node("Artifice",1,5,new String[]{"Binding"}),new Node("The Starbound Conclave",2,5,new String[]{"Sky"}),
        new Node("Hexes and Dreams",0,6,new String[]{"Vigil"}),new Node("Equivalence",1,6,new String[]{"Alembic"}),new Node("Wonders",2,6,new String[]{"The Starbound Conclave","Artifice"}));
    private final List<Button> nodes=new ArrayList<>();
    private String route="Technology";
    private int left,top,w,h,scroll;
    private double zoom=1;
    public EncyclopediaRoutesScreen(EncyclopediaScreen parent){super(Component.literal("Jugcraft pathways"));this.parent=parent;}
    private List<Node> data(){return route.equals("Technology")?TECH:MAGIC;}
    @Override protected void init(){
        w=Math.max(280,Math.min(650,width-16));h=Math.max(180,height-24);left=(width-w)/2;top=(height-h)/2;
        addRenderableWidget(Button.builder(Component.literal("Technology"),b->switchRoute("Technology")).bounds(left+8,top+23,88,18).build());
        addRenderableWidget(Button.builder(Component.literal("Magic"),b->switchRoute("Magic")).bounds(left+100,top+23,68,18).build());
        addRenderableWidget(Button.builder(Component.literal("−"),b->resize(-.2)).bounds(left+174,top+23,24,18).build());
        addRenderableWidget(Button.builder(Component.literal("+"),b->resize(.2)).bounds(left+202,top+23,24,18).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(left+w-54,top+23,46,18).build());
        nodes.clear();for(Node node:data()){
            Button button=addRenderableWidget(Button.builder(Component.literal(node.chapter()),b->{minecraft.gui.setScreen(parent);parent.browseChapter(node.chapter(),route);}).bounds(0,0,10,22).build());
            button.setTooltip(Tooltip.create(Component.literal(node.chapter()+" — open its guide entries")));nodes.add(button);
        }
        position();
    }
    private void switchRoute(String value){route=value;scroll=0;rebuildWidgets();}
    private void resize(double by){zoom=Math.clamp(zoom+by,.6,1.6);position();}
    private int step(){return (int)(56*zoom);}
    private int x(Node n){return left+8+n.column()*((w-16)/3);}
    private int y(Node n){return top+65+n.row()*step()-scroll;}
    private void position(){
        int max=Math.max(0,7*step()-(h-85));scroll=Math.clamp(scroll,0,max);
        for(int i=0;i<nodes.size();i++){
            Node n=data().get(i);Button b=nodes.get(i);b.setPosition(x(n),y(n));b.setWidth((w-28)/3);
            b.setMessage(Component.literal(font.plainSubstrByWidth(n.chapter(),b.getWidth()-8)));
            b.visible=y(n)>=top+62 && y(n)+22<=top+h-10;
        }
    }
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){scroll-=(int)(dy*24);position();return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float dt){
        super.extractBackground(g,mx,my,dt);g.fill(left-2,top-2,left+w+2,top+h+2,0xFFAD8B4D);g.fill(left,top,left+w,top+h,0xFF15252B);
        g.enableScissor(left+4,top+62,left+w-4,top+h-8);
        Map<String,Node> byName=new HashMap<>();for(Node n:data())byName.put(n.chapter(),n);
        for(Node n:data())for(String source:n.from()){
            Node a=byName.get(source);int ax=x(a)+(w-28)/6,ay=y(a)+22,bx=x(n)+(w-28)/6,by=y(n),mid=(ay+by)/2;
            g.fill(ax,ay,ax+1,mid,0xFF729C91);g.fill(Math.min(ax,bx),mid,Math.max(ax,bx)+1,mid+1,0xFF729C91);g.fill(bx,mid,bx+1,by,0xFF729C91);
        }
        g.disableScissor();g.text(font,title,left+8,top+8,0xFFFFE3AA,false);
        g.text(font,font.plainSubstrByWidth("Suggested connections • Trade can supply inputs • Scroll to explore",w-16),left+8,top+48,0xFFBDD5CF,false);
    }
}
