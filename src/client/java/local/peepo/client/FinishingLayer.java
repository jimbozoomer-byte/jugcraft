package local.peepo.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import local.peepo.CompanionFeedbackRules.Reaction;
import local.peepo.PeepoMod;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Display-only committed product and four tiny flour flecks; no inventory changes or particle entities. */
final class FinishingLayer extends RenderLayer<PeepoState,PeepoModel> {
    FinishingLayer(PeepoRenderer parent){super(parent);}
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState s,float yaw,float pitch){
        if(s.isInvisible || !s.reaction.finishing() || s.distanceToCameraSq>144)return;
        float amount=FinishingPose.amount(s.reactionTime);if(amount<=0)return;
        var hand=getParentModel().feedbackItemPosition(s.reaction!=Reaction.CATCH);
        pose.pushPose();pose.translate(hand.x/16,hand.y/16,(hand.z-.2F)/16);
        if(!s.finishItem.isEmpty()){
            pose.rotateDegrees(Axis.XP,180);pose.rotateDegrees(Axis.ZP,(float)Math.sin(s.reactionTime*.16F)*9*amount);
            float scale=.23F*amount;pose.scale(scale,scale,scale);
            s.finishItem.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,s.outlineColor);
        }else if(s.reaction==Reaction.COOK){
            float tick=s.reactionTime;
            collector.submitCustomGeometry(pose,RenderTypes.entitySolid(PeepoMod.id("textures/entity/peepo.png")),(m,c)->{
                for(int i=0;i<4;i++){
                    float age=tick-(i<2?13:23);if(age<0 || age>9)continue;
                    float side=i%2==0?-1:1,x=side*(.018F+age*.006F),y=.004F*age,z=-.02F-age*.002F;
                    float r=.008F*(1-age/10);
                    vertex(m,c,x-r,y-r,z,light);vertex(m,c,x+r,y-r,z,light);vertex(m,c,x+r,y+r,z,light);vertex(m,c,x-r,y+r,z,light);
                    // Visible from either side of the hands, including the entity's negative-Z front.
                    vertex(m,c,x-r,y+r,z,light);vertex(m,c,x+r,y+r,z,light);vertex(m,c,x+r,y-r,z,light);vertex(m,c,x-r,y-r,z,light);
                }
            });
        }
        pose.popPose();
    }
    private static void vertex(PoseStack.Pose m,VertexConsumer c,float x,float y,float z,int light){
        c.addVertex(m,x,y,z).setColor(0xFFFFFBEE).setUv(9.5F/256,73.5F/256).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,0,0,-1);
    }
}
