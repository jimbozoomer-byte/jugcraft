package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.agriculture.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Vanilla container controls and a server-supplied searchable plan, independent of JEI. */
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final int TEXT=0xFF404040, PAGE_SIZE=6;
    private final Button[] rows=new Button[PAGE_SIZE];
    private List<Integer> filtered=List.of();
    private final List<List<List<ItemStack>>> examples=new ArrayList<>();
    private int page;
    private EditBox search;
    private Button previous,next,automatic;
    private int renderedSelection=Integer.MIN_VALUE;
    public CookingPotScreen(CookingPotMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title,368,250);
        for(var plan:menu.recipes()) examples.add(plan.ingredients().stream()
            .map(part->part.ingredient().items().limit(32).map(holder->new ItemStack(holder.value())).toList()).toList());
    }
    private void select(int index){if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,index+1);}
    private Button button(String label,int x,int y,int width,int height,Runnable action){
        return addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(leftPos+x,topPos+y,width,height).build());
    }
    @Override protected void init(){
        super.init();inventoryLabelX=8;inventoryLabelY=154;
        automatic=button("Auto recipe",8,86,160,18,()->select(-1));
        automatic.setTooltip(Tooltip.create(Component.literal("Auto cooks any matching recipe. A selected recipe locks production and filters incoming ingredients. Changing the plan resets progress; items stay in the pot.")));
        search=addRenderableWidget(new EditBox(font,leftPos+184,topPos+20,176,16,Component.literal("Search pot recipes")));
        search.setMaxLength(64);search.setHint(Component.literal("Search recipes..."));
        search.setResponder(value->{page=0;filter();});
        for(int i=0;i<PAGE_SIZE;i++){final int row=i;rows[i]=button("",205,42+i*18,155,16,()->{int at=page*PAGE_SIZE+row;if(at<filtered.size())select(filtered.get(at));});}
        previous=button("<",184,152,28,16,()->{page--;updateRows();});
        next=button(">",332,152,28,16,()->{page++;updateRows();});
        filter();
    }
    private void filter(){
        String term=search.getValue().toLowerCase(Locale.ROOT);
        var found=new ArrayList<Integer>();
        for(int i=0;i<menu.recipes().size();i++){
            var p=menu.recipes().get(i);
            if(p.output().create().getHoverName().getString().toLowerCase(Locale.ROOT).contains(term) || p.id().toString().contains(term))found.add(i);
        }
        found.sort(Comparator.comparing(i->menu.recipes().get(i).output().create().getHoverName().getString()));
        filtered=List.copyOf(found);updateRows();
    }
    private String fit(String text,int width){return font.width(text)>width?font.plainSubstrByWidth(text,width-9)+"...":text;}
    private void updateRows(){
        renderedSelection=menu.selected();
        page=Math.clamp(page,0,Math.max(0,(filtered.size()-1)/PAGE_SIZE));
        previous.active=page>0;next.active=(page+1)*PAGE_SIZE<filtered.size();
        automatic.setMessage(Component.literal(menu.selected()==-1?"Auto recipe: On":"Clear recipe / Auto"));
        for(int i=0;i<PAGE_SIZE;i++){
            int at=page*PAGE_SIZE+i;rows[i].visible=at<filtered.size();if(!rows[i].visible)continue;
            int index=filtered.get(at);var p=menu.recipes().get(index);var result=p.output().create();
            rows[i].setMessage(Component.literal((menu.selected()==index?"* ":"")+fit(result.getHoverName().getString(),136)));
            rows[i].setTooltip(Tooltip.create(Component.literal(result.getHoverName().getString()+" x"+result.getCount()+" | "+p.time()/20.0+" s\n"+p.id()+"\nSelect to save this recipe on the pot.")));
        }
    }
    private ItemStack example(int recipe,int part){
        var variants=examples.get(recipe).get(part);if(variants.isEmpty())return ItemStack.EMPTY;
        int cycle=minecraft==null || minecraft.level==null?0:(int)(minecraft.level.getGameTime()/30);
        return variants.get(Math.floorMod(cycle,variants.size()));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        if(renderedSelection!=menu.selected())updateRows();
        extractBackground(g,mouseX,mouseY,delta);super.extractRenderState(g,mouseX,mouseY,delta);extractTooltip(g,mouseX,mouseY);
        int selected=menu.selected();
        if(selected>=0 && selected<menu.recipes().size()){
            var plan=menu.recipes().get(selected);
            for(int i=0;i<Math.min(8,plan.ingredients().size());i++){
                int x=leftPos+187+(i%4)*43,y=topPos+193+(i/4)*24;
                if(mouseX>=x && mouseX<x+40 && mouseY>=y && mouseY<y+20){
                    var names=examples.get(selected).get(i).stream().map(stack->stack.getHoverName().getString()).toList();
                    g.setTooltipForNextFrame(font,Component.literal(plan.ingredients().get(i).count()+" x "+String.join(" / ",names)+"\nIngredient plan only; supply transport is not implemented yet."),mouseX,mouseY);
                }
            }
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xFF373737);g.fill(x+1,y+1,x+imageWidth-2,y+imageHeight-2,0xFFFFFFFF);
        g.fill(x+3,y+3,x+imageWidth-1,y+imageHeight-1,0xFF555555);g.fill(x+4,y+4,x+imageWidth-4,y+imageHeight-4,0xFFC6C6C6);
        g.fill(x+175,y+8,x+177,y+242,0xFF777777);
        for(var slot:menu.slots){int sx=x+slot.x,sy=y+slot.y;g.fill(sx-1,sy-1,sx+17,sy+17,0xFFFFFFFF);g.fill(sx-1,sy-1,sx+16,sy+16,0xFF373737);g.fill(sx,sy,sx+16,sy+16,0xFF8B8B8B);}
        g.fill(x+88,y+41,x+110,y+45,0xFF373737);g.fill(x+88,y+41,x+88+menu.progress(22),y+45,menu.assisted()?0xFF4C9B48:0xFFD8862C);
        int color=menu.heated()?0xFFE06020:0xFF6A6A6A;
        g.fill(x+96,y+55,x+102,y+60,color);g.fill(x+97,y+51,x+101,y+55,color);g.fill(x+98,y+49,x+100,y+51,color);
        for(int i=0;i<PAGE_SIZE;i++){
            int at=page*PAGE_SIZE+i;if(at<filtered.size())g.item(menu.recipes().get(filtered.get(at)).output().create(),x+185,y+42+i*18);
        }
        int selected=menu.selected();
        if(selected>=0 && selected<menu.recipes().size()){
            var p=menu.recipes().get(selected);g.item(p.output().create(),x+8,y+112);
            for(int i=0;i<Math.min(8,p.ingredients().size());i++){
                int ix=x+187+(i%4)*43,iy=y+193+(i/4)*24;
                g.fill(ix-1,iy-1,ix+17,iy+17,0xFFA8A8A8);g.item(example(selected,i),ix,iy);
                g.text(font,"x"+p.ingredients().get(i).count(),ix+18,iy+6,TEXT,false);
            }
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        g.text(font,fit(title.getString(),164),8,8,TEXT,false);g.text(font,playerInventoryTitle,8,154,TEXT,false);
        g.text(font,menu.assisted()?"Helper: +50% speed":menu.status().label,8,72,menu.assisted()?0xFF276C27:TEXT,false);
        int selected=menu.selected();
        String name=selected>=0 && selected<menu.recipes().size()?menu.recipes().get(selected).output().create().getHoverName().getString():selected==-2?"Recipe unavailable":"Any matching recipe";
        g.text(font,fit(name,139),28,115,TEXT,false);
        g.text(font,fit(selected==-1?"Choose a recipe for supplies":"Saved on this Cooking Pot",160),8,137,TEXT,false);
        g.text(font,"Recipe plan",184,8,TEXT,false);
        String pages=(page+1)+" / "+Math.max(1,(filtered.size()+PAGE_SIZE-1)/PAGE_SIZE);
        g.text(font,pages,272-font.width(pages)/2,156,TEXT,false);
        g.text(font,"Ingredients per batch",184,179,TEXT,false);
        if(selected<0)g.text(font,"Select a recipe above",184,199,TEXT,false);
        if(filtered.isEmpty())g.text(font,"No matching recipes",184,45,TEXT,false);
    }
}
