package local.peepo.client;
import local.peepo.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.*;

public final class PeepoRenderer extends MobRenderer<PeepoEntity,PeepoState,PeepoModel> {
    private final ItemModelResolver items;
    public PeepoRenderer(EntityRendererProvider.Context context) { super(context, new PeepoModel(context.bakeLayer(PeepoClient.LAYER)), .18F);
        items=context.getItemModelResolver();
        addLayer(new FoodLayer(this));
        addLayer(new EyesLayer<PeepoState,PeepoModel>(this) {
            @Override public RenderType renderType() { return RenderTypes.eyes(PeepoMod.id("textures/entity/peepo_pumpkin_glow.png")); }
        });
    }
    @Override public PeepoState createRenderState() { return new PeepoState(); }
    @Override protected void setupRotations(PeepoState state, PoseStack pose, float bodyRot, float scale) {
        super.setupRotations(state, pose, bodyRot, scale);
        if (state.sleeping && state.deathTime <= 0) {
            pose.translate(0, state.pumpkin ? .26 : .17, state.jughead && !state.pumpkin ? -.30 : -.25);
            pose.rotateDegrees(com.mojang.math.Axis.XP, 90);
        }
    }
    @Override public Identifier getTextureLocation(PeepoState state) { if(state.jughead) return PeepoMod.id(state.blushing ? "textures/entity/jughead_blush.png" : "textures/entity/jughead.png"); return PeepoMod.id(state.blushing ? "textures/entity/peepo_blush.png" : "textures/entity/peepo.png"); }
    @Override protected RenderType getRenderType(PeepoState state, boolean visible, boolean forceTransparent, boolean glowing) {
        if(state.jughead && visible && !forceTransparent) return RenderTypes.entityTranslucent(getTextureLocation(state));
        return super.getRenderType(state,visible,forceTransparent,glowing);
    }
    @Override public void extractRenderState(PeepoEntity entity, PeepoState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.blushing = entity.isBlushing();
        state.pumpkin = entity.isWearingPumpkin();
        state.jughead = entity.isJughead();
        state.eating=entity.isEating();
        state.wheelRunning=entity.isWheelRunning();
        state.sleeping=entity.getRestMode()==CompanionEnergy.Rest.SLEEPING;
        state.eatingTime=PeepoEntity.EAT_DURATION-entity.getEatingTicks()+partialTick;
        items.updateForLiving(state.food,state.eating ? entity.getMainHandItem() : net.minecraft.world.item.ItemStack.EMPTY,ItemDisplayContext.FIXED,entity);
    }
    private static final class FoodLayer extends RenderLayer<PeepoState,PeepoModel> {
        FoodLayer(PeepoRenderer renderer) { super(renderer); }
        @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState state,float yaw,float pitch) {
            if(!state.eating || state.isInvisible || state.food.isEmpty())return;
            pose.pushPose();
            pose.translate(0,1.10-Math.sin(state.eatingTime*.8)*.012,state.pumpkin ? -.28 : -.235);
            // Entity layers use a Y-down pose; turn the item upright in both hands.
            pose.rotateDegrees(com.mojang.math.Axis.XP,180);
            pose.scale(.17F,.17F,.17F);
            state.food.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);
            pose.popPose();
        }
    }

}

