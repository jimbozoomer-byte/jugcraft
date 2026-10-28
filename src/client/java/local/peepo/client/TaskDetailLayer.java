package local.peepo.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import local.peepo.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

/** Native item models attached to the animated hands; six local grains, no world particles. */
final class TaskDetailLayer extends RenderLayer<PeepoState,PeepoModel>{
    TaskDetailLayer(PeepoRenderer renderer){super(renderer);}
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState s,float yaw,float pitch){
        if(s.isInvisible || !s.toolHandling && !s.fertilizing && !s.detail.brewing())return;
        var left=getParentModel().taskHandPosition(true);var right=getParentModel().taskHandPosition(false);
        float t=s.detailTime;
        if(s.toolHandling){
            float draw=s.detail==CompanionGesture.TOOL_DRAW?t:1-t,scale=s.detailScale*TaskDetailPose.smooth((draw-.12F)/.27F);
            float blend=TaskDetailPose.toolBlend(s);var grip=TaskDetailPose.toolPosition(s,right);
            pose.pushPose();pose.translate(grip.x/16,grip.y/16,grip.z/16);
            pose.rotateDegrees(Axis.ZP,25*(1-TaskDetailPose.smooth(draw))*(1-blend));
            pose.rotateDegrees(Axis.XP,net.minecraft.util.Mth.lerp(blend,-20,s.toolPose.xRot*net.minecraft.util.Mth.RAD_TO_DEG));pose.scale(scale,scale,scale);
            pose.rotateDegrees(Axis.YP,net.minecraft.util.Mth.lerp(blend,35,s.work==WorkAnimation.HARVEST?-90:s.work==WorkAnimation.CHOP?MachineWorkClip.KNIFE_FACING:90));pose.rotateDegrees(Axis.ZP,-45);pose.rotateDegrees(Axis.XP,180);pose.translate(.25,.25,0);
            if(scale>0)s.detailPrimary.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,s.outlineColor);
            pose.popPose();return;
        }
        float amount=TaskDetailPose.envelope(t);
        if(s.fertilizing){
            item(pose,collector,light,s,s.detailSecondary,new Vector3f(left).add(0,.65F,-.2F),.25F*amount,0);
            if(t<.3F)item(pose,collector,light,s,s.detailPrimary,new Vector3f(right).add(0,0,-.2F),.12F*amount,0);
            if(s.distanceToCameraSq<144)grains(pose,collector,light,right,t);
        }else{
            float swirl=s.detail==CompanionGesture.BREW_ADD?TaskDetailPose.brewSwirl(t):1;
            var bottle=TaskDetailPose.brewBottle(left,right,swirl);
            item(pose,collector,light,s,s.detailSecondary,bottle,.27F*amount,(float)Math.sin(t*22)*12*swirl);
            if(s.detail==CompanionGesture.BREW_ADD && t<.66F){
                float drop=TaskDetailPose.brewDrop(t);
                var reagent=TaskDetailPose.brewIngredient(right,bottle,t);
                item(pose,collector,light,s,s.detailPrimary,reagent,.15F*amount*(1-drop),-20);
            }
        }
    }
    private static void item(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState s,ItemStackRenderState item,Vector3f at,float scale,float roll){
        if(item.isEmpty() || scale<=0)return;
        pose.pushPose();pose.translate(at.x/16,at.y/16,at.z/16);pose.rotateDegrees(Axis.XP,180);pose.rotateDegrees(Axis.ZP,roll);pose.scale(scale,scale,scale);
        item.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,s.outlineColor);pose.popPose();
    }
    private static void grains(PoseStack pose,SubmitNodeCollector collector,int light,Vector3f hand,float t){
        if(t<.28F || t>.86F)return;
        pose.pushPose();pose.translate(hand.x/16,hand.y/16,hand.z/16);
        collector.submitCustomGeometry(pose,RenderTypes.entitySolid(PeepoMod.id("textures/entity/peepo.png")),(m,c)->{
            for(int i=0;i<6;i++){
                float age=(t-.28F-i*.035F)% .24F;if(age<0)continue;
                float life=age/.24F,x=(i%3-1)*.035F*life,y=.16F*life*life,z=-.025F-.13F*life,r=.007F*(1-life*.6F);
                vertex(m,c,x-r,y-r,z,light);vertex(m,c,x+r,y-r,z,light);vertex(m,c,x+r,y+r,z,light);vertex(m,c,x-r,y+r,z,light);
                vertex(m,c,x-r,y+r,z,light);vertex(m,c,x+r,y+r,z,light);vertex(m,c,x+r,y-r,z,light);vertex(m,c,x-r,y-r,z,light);
            }
        });pose.popPose();
    }
    private static void vertex(PoseStack.Pose m,VertexConsumer c,float x,float y,float z,int light){
        c.addVertex(m,x,y,z).setColor(0xFFF2E8C9).setUv(9.5F/256,73.5F/256).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,-1);
    }
}
