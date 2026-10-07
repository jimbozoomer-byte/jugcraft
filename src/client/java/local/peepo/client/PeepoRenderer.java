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
        addLayer(new HeldLayer(this));
        addLayer(new SpoonLayer(this,new StirringSpoonModel(context.bakeLayer(PeepoClient.SPOON))));
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
        state.holdingLight=TikiTorch.isHeldLight(entity.getMainHandItem());
        state.wheelRunning=entity.isWheelRunning();
        state.sleeping=entity.getRestMode()==CompanionEnergy.Rest.SLEEPING;
        state.sitting=entity.getRestMode()==CompanionEnergy.Rest.SITTING;
        state.work=state.eating || state.sleeping || state.sitting || state.wheelRunning || !entity.isAlive()?WorkAnimation.NONE:entity.workAnimation();
        state.workPhase=(float)WorkAnimation.stirPhase(entity.level().getGameTime(),partialTick);
        if(state.work==WorkAnimation.STIR){
            var target=entity.workTarget();
            // The server plants the companion on the rim; only the hands/spoon animate.
            if(!entity.level().hasChunkAt(target) || !(entity.level().getBlockState(target).getBlock() instanceof io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlock))state.work=WorkAnimation.NONE;
            else{
                state.bodyRot=entity.getYRot();state.yRot=0;state.xRot=0;state.shadowRadius=0;
            }
        }
        state.eatingTime=PeepoEntity.EAT_DURATION-entity.getEatingTicks()+partialTick;
        items.updateForLiving(state.held,!state.eating && !state.sleeping && state.work==WorkAnimation.NONE ? entity.getMainHandItem() : net.minecraft.world.item.ItemStack.EMPTY,state.holdingLight ? ItemDisplayContext.NONE : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,entity);
        items.updateForLiving(state.food,state.eating ? entity.getMainHandItem() : net.minecraft.world.item.ItemStack.EMPTY,ItemDisplayContext.FIXED,entity);
    }
    private static final class SpoonLayer extends RenderLayer<PeepoState,PeepoModel>{
        private final StirringSpoonModel spoon;
        SpoonLayer(PeepoRenderer parent,StirringSpoonModel spoon){super(parent);this.spoon=spoon;}
        @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState state,float yaw,float pitch){
            if(state.isInvisible || state.work!=WorkAnimation.STIR)return;
            collector.order(1).submitModel(spoon,state,pose,RenderTypes.entitySolid(io.github.jimbozoomer.jugcraft.Jugcraft.id("textures/block/cider_press_wood.png")),light,OverlayTexture.NO_OVERLAY,state.outlineColor);
        }
    }
    private static final class HeldLayer extends RenderLayer<PeepoState,PeepoModel> {
        HeldLayer(PeepoRenderer renderer){super(renderer);}
        @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState state,float yaw,float pitch){
            if(state.isInvisible || state.held.isEmpty())return;
            pose.pushPose();
            if(state.holdingLight){
                getParentModel().translateToUprightHand(pose);
                // Raw item model: remove both arm tilt and third-person item rotation.
                pose.rotateDegrees(com.mojang.math.Axis.XP,180);
                pose.scale(.35F,.35F,.35F);
                pose.translate(0,.25F,0);
            }else{
                getParentModel().translateToHand(pose);
                pose.rotateDegrees(com.mojang.math.Axis.XP,-90);
                pose.rotateDegrees(com.mojang.math.Axis.YP,180);
                pose.scale(.35F,.35F,.35F);
            }
            state.held.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);
            pose.popPose();
        }
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

