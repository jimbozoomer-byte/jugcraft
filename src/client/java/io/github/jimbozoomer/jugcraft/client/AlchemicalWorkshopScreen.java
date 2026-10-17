package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemicalWorkshopMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** A native inventory panel in the existing essence tablet style, with explicit feed/target/output roles. */
public final class AlchemicalWorkshopScreen extends AbstractContainerScreen<AlchemicalWorkshopMenu> {
    private static final Identifier ART = Jugcraft.id("textures/gui/alchemy/transmutation.png");
    private TransmutationScreen.ActionButton fuel;
    private final TransmutationScreen.ActionButton[] buttons = new TransmutationScreen.ActionButton[3];
    public AlchemicalWorkshopScreen(AlchemicalWorkshopMenu menu, Inventory inv, Component title) { super(menu, inv, title, 348, 240); }
    private static Component ui(String key, Object... args) { return Component.translatable("screen.jugcraft.workshop." + key, args); }
    private static String n(long value) { return String.format("%,d", value); }
    @Override protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            final int action = i;
            buttons[i] = addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+21+i*103, topPos+126, 100,
                    ui(i == 0 ? "pause" : i == 1 ? "deposit" : "withdraw"), b -> { if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action); }));
            buttons[i].setTooltip(Tooltip.create(ui(i == 0 ? "pause_help" : i == 1 ? "deposit_help" : "withdraw_help")));
        }
        if(menu.collector){fuel=addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+116,topPos+65,124,ui("fuel_0"),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,3);}));fuel.setTooltip(Tooltip.create(ui("fuel_help")));}
        refresh();
    }
    private void refresh() {
        if(fuel!=null)fuel.setMessage(ui("fuel_"+menu.fuelTarget()));
        buttons[0].setMessage(ui(menu.paused() ? "resume" : "pause")); buttons[0].active = menu.canOperate();
        buttons[1].active = menu.canOperate() && menu.wallet() > 0 && menu.essence() < menu.capacity();
        buttons[2].active = menu.canOperate() && menu.essence() > 0 && menu.wallet() < 1000000;
    }
    @Override protected void containerTick() { super.containerTick(); refresh(); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) { extractBackground(g,mx,my,delta); super.extractRenderState(g,mx,my,delta); extractTooltip(g,mx,my); }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
        int x=leftPos,y=topPos;
        g.fill(0,0,width,height,0xA00B0B14);
        TransmutationScreen.slab(g,x+4,y+5,imageWidth,imageHeight,0x70000000);
        TransmutationScreen.slab(g,x,y,imageWidth,imageHeight,0xFF20222B);
        TransmutationScreen.slab(g,x+2,y+2,imageWidth-4,imageHeight-4,0xFF747784);
        TransmutationScreen.slab(g,x+4,y+4,imageWidth-8,imageHeight-8,0xFF353640);
        g.fill(x+16,y+34,x+332,y+124,0xFF20232D);
        g.fill(x+23,y+39,x+325,y+45,0xFF111820);
        if(menu.capacity()>0)g.fill(x+24,y+40,x+24+(int)(300L*menu.essence()/menu.capacity()),y+44,0xFF79CECC);
        if(menu.collector){
            OwnerAlchemyArt.crop(g,"collector"+menu.tier(),x+143,y+88,58+(menu.tier()-1)*20,56,58,18);
        } else {
            g.fill(x+171,y+73,x+233,y+76,0xFF596571);
            int advance=ConcordanceClientOptions.reducedMotion()?28:(int)(minecraft.level.getGameTime()%40)*55/40;
            if(menu.status()==14)g.fill(x+174+advance,y+72,x+179+advance,y+77,0xFF9CE2DA);
            g.fill(x+228,y+70,x+231,y+79,0xFF8EADB4);
        }
        g.fill(x+89,y+151,x+259,y+232,0xFF22232B);
        for(var slot:menu.slots){
            if(!slot.isActive())continue;
            int sx=x+slot.x,sy=y+slot.y;
            if(slot.index==0||!menu.collector&&slot.index==1)g.fill(sx-3,sy-3,sx+19,sy+19,slot.index==0?0xFFAB9BCA:0xFFE4C786);
            g.fill(sx-1,sy-1,sx+17,sy+17,0xFF131822);g.fill(sx,sy,sx+16,sy+16,0xFF525761);g.fill(sx,sy+16,sx+17,sy+17,0xFFB5BDCB);
            if(menu.collector)OwnerAlchemyArt.slot(g,"collector"+menu.tier(),sx,sy,menu.tier()==1?7:menu.tier()==2?19:29,83);
            else OwnerAlchemyArt.slot(g,menu.tier()==1?"condenser":"condenser_mk2",sx,sy,11,25);
        }
    }
    private void line(GuiGraphicsExtractor g, Component text, int x, int y, int width, int color) { g.text(font,BookArt.fit(font,text.getString(),width),x,y,color,false); }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        line(g,title,19,13,210,0xFFE4DCEC);
        line(g,ui("buffer",n(menu.essence()),n(menu.capacity())),19,25,312,0xFFB5DFE2);
        line(g,ui(menu.collector?"star_charge":"star_supply"),24,52,91,0xFFBDAED3);
        if(menu.collector){
            line(g,ui("sunlight",menu.sunlight()),119,51,122,0xFFE3CE9A);
            line(g,ui("fuel_input"),24,84,88,0xFFE1C39F);
            line(g,ui("output"),251,48,78,0xFFB6E4D0);
        } else {
            line(g,ui("sample"),119,52,105,0xFFE4C786);line(g,ui("output"),251,48,78,0xFFB6E4D0);
            line(g,ui("feed"),24,84,88,0xFFE1C39F);line(g,ui("cost",n(menu.cost())),118,94,207,0xFFC8CEDD);
            line(g,ui("speed",menu.rate()),118,104,207,0xFF9FAEBE);
        }
        line(g,ui("status_"+menu.status()),19,116,311,menu.status()==9||menu.status()==14?0xFF9CE2BC:0xFFE2C99D);
        line(g,ui("wallet",n(menu.wallet())),93,145,237,0xFFCEC5DF);
    }
}
