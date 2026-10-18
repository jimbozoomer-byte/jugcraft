package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.*;
import com.geckolib.animatable.client.GeoRenderProvider;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.nouveau.*;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** GeckoLib owns all posing; the server owns links, colors and movement. */
public final class JugNouveauClient {
    private static final DataTicket<Integer> COLOR=DataTicket.create("jugcraft_bookwyrm_color",Integer.class);
    private JugNouveauClient(){}
    public static void register(){
        EntityRendererRegistry.register(JugNouveau.BOOK_WYRM,context->new GeoEntityRenderer<BookWyrm,EntityRenderState>(context,JugNouveau.BOOK_WYRM){
            @Override public void addRenderData(BookWyrm wyrm,@Nullable Void unused,EntityRenderState state,float partial){state.addGeckolibData(COLOR,wyrm.color());}
            @Override public Identifier getTextureLocation(EntityRenderState state){
                Integer color=state.getGeckolibData(COLOR);return Jugcraft.id("textures/entity/book_wyrm_"+JugNouveau.COLORS[color==null?3:Math.clamp(color,0,5)]+".png");
            }
        });
        BlockEntityRenderers.register(JugNouveau.LECTERN_ENTITY,context->new GeoBlockRenderer<StorageLectern,BlockEntityRenderState>(context,JugNouveau.LECTERN_ENTITY));
        MenuScreens.register(JugNouveau.MENU,StorageLecternScreen::new);
        var provider=new GeoRenderProvider(){
            private @Nullable GeoItemRenderer<LecternItem> renderer;
            @Override public GeoItemRenderer<?> getGeoItemRenderer(){
                if(renderer==null)renderer=new GeoItemRenderer<>(new DefaultedItemGeoModel<LecternItem>(Jugcraft.id("bookwyrm_lectern")){
                    @Override public Identifier getTextureResource(com.geckolib.renderer.base.GeoRenderState state){return Jugcraft.id("textures/block/bookwyrm_lectern.png");}
                });
                return renderer;
            }
        };
        LecternItem.renderer=()->provider;
    }
}
