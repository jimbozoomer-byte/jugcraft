package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.*;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Bounded, searchable index over the existing authored guides; all progress is a server snapshot. */
public class EncyclopediaScreen extends Screen {
    private final Screen parent;
    private final List<JsonObject> entries=new ArrayList<>(),goals=new ArrayList<>();
    private final List<Button> rows=new ArrayList<>();
    private final Deque<String[]> history=new ArrayDeque<>();
    private String group="Home",query="",chapter="";
    private int offset,left,top,panelWidth,panelHeight,rowCount;
    private EditBox search;
    private List<JsonObject> filtered=List.of();
    private String error="";
    public EncyclopediaScreen(Screen parent){
        super(Component.translatable("screen.jugcraft.encyclopedia"));this.parent=parent;
        var resource=Minecraft.getInstance().getResourceManager().getResource(Jugcraft.id("encyclopedia/en_us.json"));
        if(resource.isEmpty()){error="Guide content is missing. Check the installed Jugcraft build.";return;}
        try(var reader=resource.get().openAsReader()){
            var data=JsonParser.parseReader(reader).getAsJsonObject();
            for(var e:data.getAsJsonArray("entries"))if(entries.size()<2048)entries.add(e.getAsJsonObject());
            for(var g:data.getAsJsonArray("goals"))if(goals.size()<512)goals.add(g.getAsJsonObject());
        }catch(Exception e){error="Guide content could not be read.";Jugcraft.LOGGER.error(error,e);}
    }
    @Override protected void init(){
        panelWidth=Math.max(280,Math.min(650,width-16));panelHeight=Math.max(180,height-24);
        left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        int navWidth=(panelWidth-16)/5;
        String[] groups={"Home","Technology","Magic","Goals","Routes"};
        for(int i=0;i<groups.length;i++){
            String g=groups[i];addRenderableWidget(Button.builder(Component.literal(g),b->selectGroup(g))
                .bounds(left+8+i*navWidth,top+24,navWidth-3,18).build());
        }
        search=addRenderableWidget(new EditBox(font,left+8,top+47,panelWidth-16,18,Component.translatable("screen.jugcraft.encyclopedia.search")));
        search.setMaxLength(120);search.setHint(Component.translatable("screen.jugcraft.encyclopedia.search"));search.setValue(query);
        search.setResponder(value->{query=value;offset=0;refreshRows();});
        rows.clear();rowCount=Math.max(1,(panelHeight-115)/23);
        for(int i=0;i<rowCount;i++){
            final int index=i;
            rows.add(addRenderableWidget(Button.builder(Component.empty(),b->openRow(index))
                .bounds(left+8,top+83+i*23,panelWidth-16,21).build()));
        }
        int y=top+panelHeight-23;
        addRenderableWidget(Button.builder(Component.literal("Back"),b->back()).bounds(left+8,y,45,18).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->scroll(-rowCount)).bounds(left+57,y,22,18).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->scroll(rowCount)).bounds(left+83,y,22,18).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh goals"),b->EncyclopediaClient.request()).bounds(left+109,y,88,18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(left+panelWidth-55,y,47,18).build());
        refreshRows();
    }
    private void remember(){if(history.size()>=32)history.removeFirst();history.addLast(new String[]{group,query,chapter,Integer.toString(offset)});}
    public void selectGroup(String value){
        if(value.equals("Routes")){minecraft.gui.setScreen(new EncyclopediaRoutesScreen(this));return;}
        remember();group=value;query="";chapter="";offset=0;search.setValue("");refreshRows();
    }
    public void browseChapter(String value,String category){remember();group=category;chapter=value;query="";offset=0;search.setValue("");refreshRows();}
    public void searchFor(String value){chapter="";group="All";search.setValue(value);}
    private void back(){
        if(history.isEmpty()){onClose();return;}
        String[] h=history.removeLast();group=h[0];chapter=h[2];search.setValue(h[1]);offset=Integer.parseInt(h[3]);refreshRows();
    }
    public int resultCount(){return filtered.size();}
    public String selectedGroup(){return group;}
    public void refreshRows(){
        if(search==null)return;
        String q=query.toLowerCase(Locale.ROOT);
        filtered=(group.equals("Goals")?goals:entries).stream().filter(e->{
            if(group.equals("Goals"))return (str(e,"title")+" "+str(e,"description")+" "+str(e,"group")).toLowerCase(Locale.ROOT).contains(q);
            if(q.isBlank() && !group.equals("All") && !str(e,"group").equals(group))return false;
            if(!chapter.isEmpty() && !str(e,"chapter").equals(chapter))return false;
            var p=e.getAsJsonObject("page");String haystack=str(p,"title")+" "+str(e,"chapter");
            if(EncyclopediaClient.unlocked(str(e,"gate")))haystack+=" "+p.get("text");
            return haystack.toLowerCase(Locale.ROOT).contains(q);
        }).toList();
        offset=Math.max(0,Math.min(offset,Math.max(0,filtered.size()-rowCount)));
        for(int i=0;i<rows.size();i++){
            Button b=rows.get(i);b.visible=offset+i<filtered.size();if(!b.visible)continue;
            JsonObject e=filtered.get(offset+i);String label;
            if(group.equals("Goals")){
                var p=EncyclopediaClient.progress(str(e,"id"));String state=p==null?"Waiting":!p.available()?"Unavailable":p.done()?"Complete":p.completed()+"/"+p.total();
                label="["+state+"] "+str(e,"title");
            }else label=(EncyclopediaClient.unlocked(str(e,"gate"))?"":"[Research] ")+str(e,"chapter")+" / "+str(e.getAsJsonObject("page"),"title");
            b.setMessage(Component.literal(font.plainSubstrByWidth(label,panelWidth-30)));
            b.setTooltip(Tooltip.create(Component.literal(label)));
        }
    }
    public void openEntry(String id){for(JsonObject e:entries)if(str(e,"id").equals(id)){detail(e);return;}}
    private void openRow(int row){if(offset+row>=filtered.size())return;JsonObject e=filtered.get(offset+row);
        if(!group.equals("Goals")){detail(e);return;}
        var p=EncyclopediaClient.progress(str(e,"id"));
        String state=p==null?"Waiting for the server. Use Refresh goals.":!p.available()?"This objective is unavailable on this server.":p.done()?"Complete. Your progress is saved by the server.":"Recorded criteria: "+p.completed()+" / "+p.total()+". Not complete yet.";
        openText(str(e,"title"),str(e,"icon"),List.of(str(e,"description"),state,"Personal, optional goal. Its original advancement rules decide what counts. No ingredients are consumed, no extra rewards are granted and no machine or spell is gated by this guide. Return and search the objective's item or research name for its detailed pathway."),str(e,"id").replace("jugcraft:","").replace("encyclopedia_","").replace("concordance_","").replaceAll("_(encountered|observed|understood|mastered)$","").replace("_bucket","").replace('_',' '));
    }
    private void detail(JsonObject e){
        String gate=str(e,"gate");
        if(!EncyclopediaClient.unlocked(gate)){openText("Research still needed","minecraft:book",List.of("This entry follows the Concordance's existing discovery rules.","Required milestone: "+gate.replace("jugcraft:concordance_","").replace('_',' '),"Follow the Magic introduction or your Concordance Journal, then refresh goals here. Reading this page cannot grant research."));return;}
        minecraft.gui.setScreen(new HandbookScreen(this,e.getAsJsonObject("page")));
    }
    private void openText(String title,String icon,List<String> lines){openText(title,icon,lines,"");}
    private void openText(String title,String icon,List<String> lines,String related){JsonObject p=new JsonObject();p.addProperty("title",title);p.addProperty("icon",icon);JsonArray t=new JsonArray();lines.forEach(t::add);p.add("text",t);if(!related.isEmpty())p.addProperty("related",related);minecraft.gui.setScreen(new HandbookScreen(this,p));}
    private void scroll(int by){offset=Math.max(0,Math.min(offset+by,Math.max(0,filtered.size()-rowCount)));refreshRows();}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){scroll((int)-Math.signum(dy)*3);return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){
        super.extractBackground(g,x,y,delta);g.fill(left-2,top-2,left+panelWidth+2,top+panelHeight+2,0xFFAD8B4D);
        g.fill(left,top,left+panelWidth,top+panelHeight,0xFF15252B);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float delta){
        super.extractRenderState(g,x,y,delta);
        g.text(font,title,left+10,top+8,0xFFFFE3AA,false);
        String subtitle=error.isEmpty()?group+(chapter.isEmpty()?"":" / "+chapter)+"   "+filtered.size()+" entries   •   "+(filtered.isEmpty()?0:offset+1)+"–"+Math.min(filtered.size(),offset+rowCount):error;
        g.text(font,font.plainSubstrByWidth(subtitle,panelWidth-16),left+8,top+70,0xFFBDD5CF,false);
    }
    private static String str(JsonObject o,String key){return o.has(key)?o.get(key).getAsString():"";}
}
