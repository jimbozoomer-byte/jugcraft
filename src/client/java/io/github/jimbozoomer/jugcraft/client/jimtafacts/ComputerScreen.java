package io.github.jimbozoomer.jugcraft.client.jimtafacts;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.jimtafacts.computers.ComputerTerminal;
import io.github.jimbozoomer.jugcraft.jimtafacts.computers.WorkshopComputers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original supplied computer surround, readable editable program, live output and ordinary inventory. */
public final class ComputerScreen extends AbstractContainerScreen<ComputerTerminal.Menu> {
    private float scale=1;
    private MultiLineEditBox editor;
    private MultiLineEditBox log;
    private String accepted = "", lastOutput = "";
    private boolean received;
    private final java.util.List<Button> actions = new java.util.ArrayList<>();
    private Button previous, next;
    private Button programToggle,storeFile,loadFile,deleteFile,previousFiles,nextFiles;
    private EditBox fileName;
    private EditBox terminalInput;
    private Button sendInput;
    private Button terminalToggle,luaExample;
    private Button keyboardToggle;
    private boolean rawKeyboard;
    private boolean browsing;
    private int filePage,seenLoadRevision;
    private String armed="";
    private final java.util.List<Button> fileRows=new java.util.ArrayList<>();
    public ComputerScreen(ComputerTerminal.Menu menu, Inventory inventory, Component title) { super(menu, inventory, title, 360, 288); }
    @Override protected void init() {
        String draft=editor==null?menu.program:editor.getValue();String name=fileName==null?"program":fileName.getValue();
        super.init(); titleLabelY = 10000; inventoryLabelY = 10000;
        scale=Math.min(1F,Math.min((width-12F)/imageWidth,(height-12F)/imageHeight));
        leftPos=Math.round((width/scale-imageWidth)/2);topPos=Math.round((height/scale-imageHeight)/2);
        editor = new MultiLineEditBox.Builder().setX(leftPos + 16).setY(topPos + 32)
                .setShowBackground(false).setTextColor(0xFFE0EFE0).setCursorColor(0xFF96E4AE)
                .setPlaceholder(Component.literal("print Hello, Jugcraft!\nstop"))
                .build(font, 155, 125, Component.literal("Computer program"));
        editor.setCharacterLimit(WorkshopComputers.MAX_PROGRAM); editor.setValue(draft); addRenderableWidget(editor);
        log = new MultiLineEditBox.Builder().setX(leftPos + 190).setY(topPos + 32)
                .setShowBackground(false).setTextColor(0xFFA1E7B3).setCursorColor(0xFFA1E7B3)
                .build(font, 155, 103, Component.literal("Computer output"));
        log.setCharacterLimit(8192); log.setValue(menu.output); addRenderableWidget(log);
        terminalInput=addRenderableWidget(new EditBox(font,leftPos+190,topPos+140,115,17,Component.literal("Input for running Lua program")));
        terminalInput.setMaxLength(256);
        sendInput=addRenderableWidget(Button.builder(Component.literal("Send"),b->{ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId,9,terminalInput.getValue()));terminalInput.setValue("");}).bounds(leftPos+309,topPos+140,36,17).build());
        actions.clear();
        String[] labels = {"Save", "Run", "Stop", "To disk", "From disk", "Refuel", "Help"};
        for (int i = 0; i < labels.length; i++) {
            int action = i;
            actions.add(addRenderableWidget(Button.builder(Component.literal(labels[i]), button -> {
                ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId, action, action == 0 ? editor.getValue() : ""));
                if (action == 0) accepted = editor.getValue();
            }).bounds(leftPos + 9 + i * 49, topPos + 174, 47, 18).build()));
        }
        previous=addRenderableWidget(Button.builder(Component.literal("Previous page"),b->ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId,7,""))).bounds(leftPos+16,topPos+174,145,18).build());
        next=addRenderableWidget(Button.builder(Component.literal("Next page"),b->ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId,8,""))).bounds(leftPos+190,topPos+174,145,18).build());
        luaExample=addRenderableWidget(Button.builder(Component.literal("ROM shell"),b->{if(!armed.equals("lua-template")){armed="lua-template";b.setMessage(Component.literal("Replace draft?"));return;}editor.setValue("--!lua\nshell.run('shell')");armed="";b.setMessage(Component.literal("ROM shell"));browsing=false;}).bounds(leftPos+190,topPos+4,90,16).build());
        terminalToggle=addRenderableWidget(Button.builder(Component.literal("Terminal"),b->{menu.terminalView=!menu.terminalView;browsing=false;}).bounds(leftPos+105,topPos+4,78,16).build());
        keyboardToggle=addRenderableWidget(Button.builder(Component.literal("Line input"),b->{rawKeyboard=!rawKeyboard;setFocused(null);}).bounds(leftPos+190,topPos+4,90,16).build());
        programToggle=addRenderableWidget(Button.builder(Component.literal("Programs"),b->{browsing=!browsing;armed="";}).bounds(leftPos+286,topPos+4,66,16).build());
        fileName=addRenderableWidget(new EditBox(font,leftPos+16,topPos+32,155,18,Component.literal("Saved program name")));
        fileName.setMaxLength(32);fileName.setValue(name);fileName.setResponder(v->armed="");
        fileRows.clear();
        for(int i=0;i<4;i++){
            int row=i;fileRows.add(addRenderableWidget(Button.builder(Component.empty(),b->{int index=filePage*4+row;if(index<menu.files.size())fileName.setValue(menu.files.get(index));}).bounds(leftPos+16,topPos+55+i*19,155,17).build()));
        }
        previousFiles=addRenderableWidget(Button.builder(Component.literal("<"),b->{filePage=Math.max(0,filePage-1);armed="";}).bounds(leftPos+16,topPos+132,24,16).build());
        nextFiles=addRenderableWidget(Button.builder(Component.literal(">"),b->{filePage=Math.min(3,filePage+1);armed="";}).bounds(leftPos+147,topPos+132,24,16).build());
        storeFile=addRenderableWidget(Button.builder(Component.literal("Store"),b->fileAction(0)).bounds(leftPos+16,topPos+151,49,17).build());
        loadFile=addRenderableWidget(Button.builder(Component.literal("Load"),b->fileAction(1)).bounds(leftPos+69,topPos+151,49,17).build());
        deleteFile=addRenderableWidget(Button.builder(Component.literal("Delete"),b->fileAction(2)).bounds(leftPos+122,topPos+151,49,17).build());
    }
    private void fileAction(int action){
        String name=fileName.getValue(),key=action+":"+name;
        boolean confirm=action==2||action==0&&menu.files.contains(name)||action==1&&!editor.getValue().equals(accepted);
        if(confirm&&!armed.equals(key)){armed=key;return;}
        ClientPlayNetworking.send(new ComputerTerminal.FileEdit(menu.containerId,action,name,action==0?editor.getValue():""));armed="";
        // A successful server acknowledgment replaces the editor; a failed load preserves its draft.
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        if(menu.loadRevision!=seenLoadRevision){seenLoadRevision=menu.loadRevision;editor.setValue(menu.program);accepted=menu.program;browsing=false;}
        // Preserve unsaved edits when live output arrives; disk/load updates replace only an unmodified editor.
        if ((!received || editor.getValue().equals(accepted)) && !menu.program.equals(accepted)) {
            editor.setValue(menu.program); accepted = menu.program;
        }
        received = !menu.output.equals("Connecting to the workshop terminal…");
        if (!menu.output.equals(lastOutput)) { log.setValue(menu.output); lastOutput = menu.output; }
        editor.active = !menu.readOnly;
        terminalInput.visible=sendInput.visible=menu.hardware&&!menu.readOnly;
        terminalInput.active=sendInput.active=menu.running;
        browsing&=menu.fileStorage&&!menu.readOnly;
        programToggle.visible=menu.fileStorage&&!menu.readOnly;programToggle.setMessage(Component.literal(browsing?"Editor":"Programs"));
        editor.visible=!browsing;
        fileName.visible=fileName.active=browsing;
        for(int i=0;i<fileRows.size();i++){
            int index=filePage*4+i;var button=fileRows.get(i);button.visible=browsing;button.active=index<menu.files.size();
            button.setMessage(Component.literal(index<menu.files.size()?menu.files.get(index):"—"));
        }
        filePage=Math.min(filePage,Math.max(0,(menu.files.size()-1)/4));
        previousFiles.visible=nextFiles.visible=storeFile.visible=loadFile.visible=deleteFile.visible=browsing;
        previousFiles.active=filePage>0;nextFiles.active=(filePage+1)*4<menu.files.size();
        storeFile.active=fileName.getValue().matches("[a-zA-Z0-9_.-]{1,32}")&&!java.util.Set.of(".","..","rom").contains(fileName.getValue());loadFile.active=deleteFile.active=menu.files.contains(fileName.getValue());
        storeFile.setMessage(Component.literal(armed.equals("0:"+fileName.getValue())?"Replace?":"Store"));
        loadFile.setMessage(Component.literal(armed.equals("1:"+fileName.getValue())?"Discard?":"Load"));
        deleteFile.setMessage(Component.literal(armed.equals("2:"+fileName.getValue())?"Delete?":"Delete"));
        actions.forEach(button->button.visible=!menu.readOnly);
        previous.visible=next.visible=menu.readOnly;
        terminalToggle.visible=menu.hardware&&!menu.readOnly;terminalToggle.setMessage(Component.literal(menu.terminalView?"Editor":"Terminal"));
        luaExample.visible=!menu.readOnly&&!menu.terminalView;
        keyboardToggle.visible=menu.terminalView;keyboardToggle.setMessage(Component.literal(rawKeyboard?"Direct keys":"Line input"));
        if(menu.terminalView){
            editor.visible=log.visible=programToggle.visible=false;
            actions.forEach(button->button.visible=false);actions.get(2).visible=true;actions.get(2).setX(leftPos+286);actions.get(2).setY(topPos+4);
            terminalInput.setX(leftPos+16);terminalInput.setY(topPos+247);terminalInput.setWidth(270);sendInput.setX(leftPos+294);sendInput.setY(topPos+247);
            terminalInput.visible=sendInput.visible=!rawKeyboard;
        }else{
            log.visible=true;actions.get(2).setX(leftPos+9+2*49);actions.get(2).setY(topPos+174);
            terminalInput.setX(leftPos+190);terminalInput.setY(topPos+140);terminalInput.setWidth(115);sendInput.setX(leftPos+309);sendInput.setY(topPos+140);
        }
        extractBackground(g, x, y, delta);
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        super.extractRenderState(g, Math.round(x/scale), Math.round(y/scale), delta);
        extractTooltip(g, Math.round(x/scale), Math.round(y/scale));g.pose().popMatrix();
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int x, int y, float delta) {
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        drawComputerBackground(g);g.pose().popMatrix();
    }
    private void drawComputerBackground(GuiGraphicsExtractor g) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF292C2B);
        g.outline(leftPos, topPos, imageWidth, imageHeight, 0xFF848C88);
        if(menu.terminalView){
            g.text(font,Component.literal("Workshop OS"),leftPos+12,topPos+8,0xFFE4EAE6,false);
            g.fill(leftPos+12,topPos+26,leftPos+348,topPos+236,0xFF111713);
            int count=Math.min(1152,Math.min(menu.canvasText.length(),Math.min(menu.canvasForeground.length(),menu.canvasBackground.length())));
            for(int i=0;i<count;i++){
                int cx=leftPos+17+i%48*6,cy=topPos+30+i/48*8;
                int front=Character.digit(menu.canvasForeground.charAt(i),16),back=Character.digit(menu.canvasBackground.charAt(i),16);
                g.fill(cx,cy,cx+6,cy+8,palette(back));
                char letter=menu.canvasText.charAt(i);if(letter!=' '){g.pose().pushMatrix();g.pose().translate(cx,cy);g.pose().scale(.8F,.8F);g.text(font,String.valueOf(letter),0,0,palette(front),false);g.pose().popMatrix();}
            }
            g.text(font,menu.energy+" JE · "+(menu.running?"Running":"Stopped"),leftPos+16,topPos+271,0xFFA1E7B3,false);return;
        }
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Jugcraft.id("jimtafacts/computer_border"), leftPos + 5, topPos + 21, 174, 146, -1);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Jugcraft.id("jimtafacts/computer_border"), leftPos + 179, topPos + 21, 176, 146, -1);
        g.text(font, title, leftPos + 12, topPos + 8, 0xFFE4EAE6, false);
        g.text(font, menu.running ? "RUNNING" : "READY", leftPos + 190, topPos + 161, menu.running ? 0xFF93E3A8 : 0xFFABB5AD, false);
        if(browsing)g.text(font,(filePage+1)+" / "+Math.max(1,(menu.files.size()+3)/4)+" · "+menu.files.size()+" saved",leftPos+47,topPos+136,0xFFB3C5B8,false);
        g.text(font, menu.hardware ? "Device / disk / turtle storage" : "Portable program editor", leftPos + 11, topPos + 194, 0xFFD1D7D2, false);
        g.text(font, "Inventory", leftPos + 188, topPos + 194, 0xFFD1D7D2, false);
        if (menu.hardware) for (int i = 0; i < 18; i++) slot(g, leftPos + 11 + i % 9 * 18, topPos + 203 + i / 9 * 18);
        for (int i = 0; i < 27; i++) slot(g, leftPos + 187 + i % 9 * 18, topPos + 203 + i / 9 * 18);
        for (int i = 0; i < 9; i++) slot(g, leftPos + 187 + i * 18, topPos + 261);
        g.text(font, menu.hardware ? menu.energy + " / 16000 JE" : "Save preserves this item's program", leftPos + 12, topPos + 251, 0xFF9ACDB1, false);
        if (menu.turtle) g.text(font, "Slots 17/18: left/right upgrades", leftPos + 12, topPos + 267, 0xFFB3C5B8, false);
    }
    private static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF161A18); g.outline(x, y, 18, 18, 0xFF646F67);
    }
    private static int palette(int color){return 0xFF000000|net.minecraft.world.item.DyeColor.byId(Math.clamp(color,0,15)).getTextureDiffuseColor();}
    private void key(String kind,int code){ClientPlayNetworking.send(new ComputerTerminal.Edit(menu.containerId,10,kind+"\t"+code));}
    private net.minecraft.client.input.MouseButtonEvent scaled(net.minecraft.client.input.MouseButtonEvent e){return new net.minecraft.client.input.MouseButtonEvent(e.x()/scale,e.y()/scale,e.buttonInfo());}
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent e,boolean twice){return super.mouseClicked(scaled(e),twice);}
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent e){return super.mouseReleased(scaled(e));}
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent e,double dx,double dy){return super.mouseDragged(scaled(e),dx/scale,dy/scale);}
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){return super.mouseScrolled(x/scale,y/scale,dx,dy);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event){
        if(menu.terminalView&&rawKeyboard&&menu.running&&event.input()!=256){key("key",event.input());return true;}
        return super.keyPressed(event);
    }
    @Override public boolean keyReleased(net.minecraft.client.input.KeyEvent event){
        if(menu.terminalView&&rawKeyboard&&menu.running&&event.input()!=256){key("key_up",event.input());return true;}
        return super.keyReleased(event);
    }
    @Override public boolean charTyped(net.minecraft.client.input.CharacterEvent event){
        if(menu.terminalView&&rawKeyboard&&menu.running){if(event.codepoint()>=32&&event.codepoint()<=126)key("char",event.codepoint());return true;}
        return super.charTyped(event);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {}
}
