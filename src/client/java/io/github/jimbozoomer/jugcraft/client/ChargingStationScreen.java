package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.tools.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Two equipment cards above the native inventory tray, using the shared electric workshop skin. */
public final class ChargingStationScreen extends AbstractContainerScreen<ChargingStationMenu> implements ScreenKit.Fitted {
    private final MachineScreenThemes.Theme theme=MachineScreenThemes.of(io.github.jimbozoomer.jugcraft.machine.MachineKind.BATTERY_BOX);
    private record Controls(int bay,ToolUpgrades.Kind kind,Button insert,Button remove) {}
    private final List<Controls> controls=new ArrayList<>();
    public ChargingStationScreen(ChargingStationMenu menu,Inventory inventory,Component title) {
        super(menu,inventory,title,ChargingStationMenu.WIDTH,ChargingStationMenu.HEIGHT);
    }
    private static Component text(String key,Object... args) { return Component.translatable("screen.jugcraft.charging_station."+key,args); }
    private void send(int id) { if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id); }
    @Override protected void init() {
        super.init(); titleLabelX=12; titleLabelY=2; inventoryLabelY=10_000; controls.clear();
        for(int bay=0;bay<2;bay++) for(var kind:ToolUpgrades.Kind.values()) {
            int item=bay, row=kind.ordinal(), x=leftPos+6+bay*150, y=topPos+58+row*12;
            var name=Component.translatable("item.jugcraft."+kind.id+"_module.short");
            var plus=addRenderableWidget(Button.builder(Component.literal("+"),b->send(ChargingStationMenu.button(item,kind,false)))
                .bounds(x+113,y,12,12).build());
            var minus=addRenderableWidget(Button.builder(Component.literal("-"),b->send(ChargingStationMenu.button(item,kind,true)))
                .bounds(x+128,y,12,12).build());
            plus.setTooltip(Tooltip.create(text("insert",name,bay+1)));
            minus.setTooltip(Tooltip.create(text(kind==ToolUpgrades.Kind.CAPACITY?"remove_capacity":"remove",name,bay+1)));
            controls.add(new Controls(bay,kind,plus,minus));
        }
        updateControls();
    }
    private void updateControls() {
        if(minecraft.level==null||minecraft.player==null)return;
        for(var row:controls) {
            var tool=menu.getSlot(row.bay).getItem();
            row.insert.active=ToolUpgrades.fitResult(minecraft.level,tool,row.kind)==ToolUpgrades.Result.FITTED && menu.hasModule(minecraft.player,row.kind);
            row.remove.active=ToolUpgrades.removeResult(minecraft.level,tool,row.kind)==ToolUpgrades.Result.REMOVED;
        }
    }
    @Override protected void containerTick() { super.containerTick(); updateControls(); }
    private static String compact(long value) {
        if(value>=1_000_000)return String.format(Locale.ROOT,"%.2fM",value/1_000_000D);
        if(value>=1000)return String.format(Locale.ROOT,"%.1fk",value/1000D);
        return Long.toString(value);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        extractBackground(graphics,mouseX,mouseY,delta); super.extractRenderState(graphics,mouseX,mouseY,delta); extractTooltip(graphics,mouseX,mouseY);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta) {
        int x=leftPos,y=topPos;
        ScreenKit.body(g,theme,x,y,imageWidth,144);
        MachineSkin.lamp(g,x+4,y+3,menu.energy()>0?theme.ok():theme.dim());
        ScreenKit.tray(g,theme,x,y+146,imageWidth,imageHeight-146);
        ScreenKit.inventory(g,theme,x+ChargingStationMenu.INVENTORY_X,y+ChargingStationMenu.INVENTORY_Y);
        g.text(font,text("buffer",String.format("%,d",menu.energy()),String.format("%,d",ChargingStationBlockEntity.CAPACITY)),x+9,y+17,theme.dim(),false);
        for(int bay=0;bay<2;bay++) {
            int cx=x+6+bay*150; var stack=menu.getSlot(bay).getItem();
            ScreenKit.pane(g,theme,cx,y+27,146,106);
            MachineSkin.slot(g,theme,cx+5,y+30);
            g.text(font,ScreenKit.fit(font,stack.isEmpty()?text("empty").getString():stack.getHoverName().getString(),115),cx+26,y+30,theme.text(),false);
            if(!stack.isEmpty()) {
                g.text(font,compact(Chargeable.energy(stack))+" / "+compact(Chargeable.capacity(stack))+" JE",cx+26,y+41,theme.dim(),false);
                g.fill(cx+26,y+52,cx+139,y+54,0xFF18242A);
                int width=(int)Math.clamp(113D*Chargeable.energy(stack)/Math.max(1,Chargeable.capacity(stack)),0,113);
                g.fill(cx+26,y+52,cx+26+width,y+54,theme.progress());
            }
            for(var kind:ToolUpgrades.Kind.values()) {
                int count=ToolUpgrades.installed(minecraft.level,stack,kind);
                var label=Component.translatable("item.jugcraft."+kind.id+"_module.short").getString()+" "+count+"/"+kind.max;
                g.text(font,ScreenKit.fit(font,label,105),cx+5,y+60+kind.ordinal()*12,ToolUpgrades.supports(stack,kind)?theme.text():theme.dim(),false);
            }
        }
        g.text(font,ScreenKit.fit(font,text("status."+menu.status()).getString(),290),x+9,y+134,menu.status()>2?theme.progress():theme.text(),false);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY) { g.text(font,title,titleLabelX,titleLabelY,theme.text(),false); }
    @Override public ScreenRectangle panelArea() { return new ScreenRectangle(leftPos,topPos,imageWidth,imageHeight); }
}
