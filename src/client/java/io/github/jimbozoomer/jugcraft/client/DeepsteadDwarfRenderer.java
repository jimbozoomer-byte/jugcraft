package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.DeepsteadDwarf;
import io.github.jimbozoomer.jugcraft.town.DeepsteadWares;
import java.io.IOException;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** A weighted short stride, layered beard and tools fixed to the animated wrists. */
public final class DeepsteadDwarfRenderer extends MobRenderer<DeepsteadDwarf,DeepsteadDwarfRenderer.State,DeepsteadDwarfRenderer.Model> {
    private static final Identifier[] TEXTURES = {
        Jugcraft.id("textures/entity/deepstead/dwarf_0.png"),
        Jugcraft.id("textures/entity/deepstead/dwarf_1.png"),
        Jugcraft.id("textures/entity/deepstead/dwarf_2.png")
    };
    public static void register() { EntityRendererRegistry.register(DeepsteadDwarf.TYPE,DeepsteadDwarfRenderer::new); }

    public DeepsteadDwarfRenderer(EntityRendererProvider.Context context) {
        super(context,new Model(readRig()),.46F);
    }
    private static ModelPart readRig() {
        try(var in=Minecraft.getInstance().getResourceManager().getResourceOrThrow(Jugcraft.id("town_models/deepstead_dwarf.json")).openAsReader()) {
            return RecoveredWildlifeModel.bake(JsonParser.parseReader(in).getAsJsonObject());
        } catch(IOException e) { throw new IllegalStateException("Could not read the Deepstead dwarf model",e); }
    }
    public static final class State extends LivingEntityRenderState {
        DeepsteadDwarf.Role role;
        int look;
        boolean alert,stonewright,kettleKeeper;
        float swing, workTicks;
    }
    @Override public State createRenderState() { return new State(); }
    @Override public Identifier getTextureLocation(State state) { return TEXTURES[state.look]; }
    @Override public void extractRenderState(DeepsteadDwarf entity,State state,float partial) {
        super.extractRenderState(entity,state,partial);
        state.role=entity.dwarfRole();
        state.look=entity.appearance();
        state.stonewright=entity.wearsGuildColors(DeepsteadWares.STONEWRIGHT);
        state.kettleKeeper=entity.wearsGuildColors(DeepsteadWares.COPPER_KETTLE);
        state.alert=entity.isAggressive();
        state.swing=entity.getSwingAnimation(partial);
        state.workTicks=entity.workTicks(partial);
    }

    public static final class Model extends EntityModel<State> {
        private final ModelPart body,head,beard,right,left,rightLeg,leftLeg;
        Model(ModelPart rig) {
            super(rig);
            body=rig.getChild("body"); head=body.getChild("head"); beard=head.getChild("beard");
            right=body.getChild("right_arm"); left=body.getChild("left_arm");
            rightLeg=rig.getChild("right_leg"); leftLeg=rig.getChild("left_leg");
        }
        @Override public void setupAnim(State s) {
            super.setupAnim(s);
            boolean guard=s.role==DeepsteadDwarf.Role.GUARD, seller=s.role==DeepsteadDwarf.Role.MERCHANT;
            boolean smith=!guard && !seller;
            head.getChild("miner_hair").visible=smith || s.stonewright;
            head.getChild("merchant_cap").visible=seller && !s.stonewright;
            beard.getChild("smith_beard").visible=smith;
            beard.getChild("merchant_beard").visible=seller;
            beard.getChild("watch_beard").visible=guard;
            var helm=head.getChild("guard_helmet");
            helm.visible=guard;
            helm.getChild("visor").xRot=s.alert || s.look==1?0:-1.04F;
            body.getChild("work_apron").visible=smith || s.stonewright || s.kettleKeeper;
            body.getChild("bar_towel").visible=s.kettleKeeper;
            body.getChild("tabard").visible=guard;
            boolean trader=seller && !s.stonewright && !s.kettleKeeper;
            body.getChild("strap").visible=body.getChild("satchel").visible=trader;
            body.getChild("merchant_mantle").visible=trader;
            var bag=body.getChild("satchel");
            bag.getChild("map_case").visible=s.look==1;
            bag.getChild("lamp_sample").visible=s.look!=1;
            rightLeg.getChild("right_greave").visible=leftLeg.getChild("left_greave").visible=guard;
            rightLeg.getChild("right_toe_cap").visible=leftLeg.getChild("left_toe_cap").visible=!seller;
            right.getChild("right_gauntlet").visible=left.getChild("left_gauntlet").visible=!seller;
            right.getChild("right_sleeve").visible=left.getChild("left_sleeve").visible=seller;
            right.getChild("right_pauldron").visible=left.getChild("left_pauldron").visible=guard;
            right.getChild("stone_maul").visible=smith;
            right.getChild("guard_mace").visible=left.getChild("buckler").visible=guard;
            left.getChild("ledger").visible=trader;
            left.getChild("masons_square").visible=s.stonewright;
            right.getChild("copper_ladle").visible=s.kettleKeeper;

            float phase=s.walkAnimationPos*1.24F;
            float speed=Math.min(s.walkAnimationSpeed,.85F);
            float stride=Mth.cos(phase)*speed;
            float breath=Mth.sin(s.ageInTicks*.065F)*.023F;
            rightLeg.xRot=stride*.83F; leftLeg.xRot=-rightLeg.xRot;
            body.y-=Math.abs(Mth.sin(phase))*speed*.55F;
            body.zRot=stride*.042F;
            right.xRot=-.23F-stride*.49F+breath;
            left.xRot=(seller?-.92F:guard?-.36F:-.15F)+stride*(seller?.21F:.50F)-breath;
            right.zRot=smith?.16F:.085F;
            if(s.kettleKeeper) right.xRot-=.48F;
            left.zRot=guard?-.15F:-.09F;
            if(seller) left.yRot=.21F;
            head.xRot=Mth.clamp(s.xRot,-26F,27F)*Mth.DEG_TO_RAD;
            head.yRot=Mth.clamp(s.yRot,-54F,54F)*Mth.DEG_TO_RAD;
            beard.xRot=-head.xRot*.22F+.023F*stride;
            if(smith && s.workTicks>=0 && s.workTicks<22) {
                // Lift slowly, snap down onto the work face, settle, then release.
                float t=s.workTicks;
                float lift=t<11 ? t/11F : t<14 ? 1-(t-11)/3F : 0;
                float contact=t<11 ? 0 : t<14 ? (t-11)/3F : t<17 ? 1 : (22-t)/5F;
                right.xRot-=lift*2.0F+contact*.96F;
                body.xRot+=contact*.17F; head.xRot+=contact*.12F;
            }
            if(guard) {
                if(s.alert) left.xRot-=.63F;
                right.xRot-=Mth.sin(Mth.sqrt(s.swing)*Mth.PI)*1.57F;
            }
        }
    }
}
