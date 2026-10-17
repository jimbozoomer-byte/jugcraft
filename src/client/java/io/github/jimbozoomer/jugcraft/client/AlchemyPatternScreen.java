package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** No typing needed to browse: real learned icons, favorites and pages. Optional bounded search narrows the library. */
public final class AlchemyPatternScreen extends AbstractContainerScreen<AlchemyPatternMenu> {
    private EditBox search;
    private String sent="";
    private int debounce;
    private TransmutationScreen.ActionButton learn,make,favorite,filter,previous,next;
    public AlchemyPatternScreen(AlchemyPatternMenu menu,Inventory inv,Component title){super(menu,inv,title,330,238);}
    private static Component ui(String s,Object... args){return Component.translatable("screen.jugcraft.pattern."+s,args);}
    private void press(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    private TransmutationScreen.ActionButton button(int x,int y,int w,String key,int id){return addRenderableWidget(new TransmutationScreen.ActionButton(leftPos+x,topPos+y,w,ui(key),b->press(id)));}
    @Override protected void init(){
        super.init();
        search=addRenderableWidget(new EditBox(font,leftPos+180,topPos+30,132,18,ui("search")));search.setMaxLength(48);search.setHint(ui("search"));
        search.setResponder(s->debounce=5);
        learn=button(16,87,56,"learn",0);learn.setTooltip(Tooltip.create(ui("learn_help")));make=button(77,87,82,"make",1);
        favorite=button(16,112,68,"favorite",4);filter=button(89,112,70,"all",5);
        previous=button(181,129,35,"previous",2);next=button(277,129,35,"next",3);
        refresh();
    }
    private void refresh(){
        learn.active=menu.getSlot(0).hasItem();favorite.active=!menu.selection().isEmpty();previous.active=menu.page()>0;next.active=menu.page()+1<menu.pages();
        make.active=menu.canMake();make.setMessage(ui(menu.eye()?"choose":"make"));
        favorite.setMessage(ui(menu.isFavorite()?"unfavorite":"favorite"));filter.setMessage(ui(menu.favorites()?"favorites":"all"));
        make.setTooltip(Tooltip.create(ui(menu.eye()?"build_help":"make_help")));
    }
    @Override protected void containerTick(){
        super.containerTick();refresh();
        if(debounce>0&&--debounce==0&&!sent.equals(search.getValue())){sent=search.getValue();ClientPlayNetworking.send(new AlchemyPatterns.Search(menu.containerId,sent));}
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){extractBackground(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);extractTooltip(g,mx,my);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){
        int x=leftPos,y=topPos;g.fill(0,0,width,height,0xA00B0B14);OwnerAlchemyArt.frame(g,x,y,imageWidth,imageHeight);
        g.fill(x+10,y+35,x+164,y+84,0xFF20232D);
        OwnerAlchemyArt.crop(g,menu.eye()?"mercurial_eye":"nei",x+52,y+61,menu.eye()?83:88,menu.eye()?23:34,menu.eye()?16:22,16);
        g.fill(x+176,y+51,x+317,y+128,0xFF20232D);
        for(var s:menu.slots){int sx=x+s.x,sy=y+s.y;
            OwnerAlchemyArt.slot(g,"alchchest",sx,sy,11,3);
            if(s.index>=AlchemyPatternMenu.GHOST_START&&net.minecraft.world.item.ItemStack.isSameItemSameComponents(s.getItem(),menu.selection())&&!s.getItem().isEmpty())g.outline(sx-2,sy-2,20,20,0xFFE9C989);
        }
        if(!menu.selection().isEmpty())g.item(menu.selection(),x+(menu.eye()?84:134),y+63);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.text(font,ui(menu.eye()?"build":"title"),13,12,0xFFE5DDEB,false);
        g.text(font,BookArt.fit(font,ui("balance",menu.balance()).getString(),155),13,25,0xFFAADCD9,false);
        g.text(font,ui("sample"),16,43,0xFFE4C786,false);g.text(font,ui(menu.eye()?"preview":"output"),76,43,0xFFE4C786,false);
        if(!menu.eye())g.text(font,ui("target"),119,43,0xFFE4C786,false);
        g.text(font,BookArt.fit(font,ui("cost",menu.price()+(menu.eye()?8:0)).getString(),145),16,136,0xFFBBCBD7,false);
        g.text(font,ui("page",menu.page()+1,menu.pages()),222,135,0xFFDDD0EB,false);

    }
}
