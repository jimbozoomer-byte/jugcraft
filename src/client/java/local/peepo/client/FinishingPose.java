package local.peepo.client;

import local.peepo.CompanionFeedbackRules.Reaction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Cosmetic upper-body clips. Locomotion, the fishing seat and the rod hand remain owned by their existing poses. */
final class FinishingPose {
    private FinishingPose(){}
    private static float smooth(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
    private static float window(float t,float in,float hold,float release,float end){return smooth((t-in)/(hold-in))*(1-smooth((t-release)/(end-release)));}
    static float amount(float ticks){return window(Mth.clamp(ticks/40F,0,1),0,.22F,.76F,1);}
    static float sweep(float ticks){float t=ticks/40F;return window(t,.22F,.34F,.34F,.46F)+window(t,.47F,.59F,.59F,.71F);}
    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right){
        float t=Mth.clamp(s.reactionTime/40F,0,1),a=amount(s.reactionTime);
        float out=window(t,0,.12F,.86F,1),look=Mth.sin(t*Mth.TWO_PI)*a;
        if(s.reaction==Reaction.CATCH){
            // Admire the landed item in the left hand while the right continues holding the rod.
            left.z=Mth.lerp(out,left.z,s.pumpkin?-3.3F:-2.3F);
            left.xRot=Mth.lerp(a,left.xRot,-2.02F+.1F*look);
            left.yScale=Mth.lerp(a,left.yScale,1);
            left.yRot=Mth.lerp(a,left.yRot,-.28F);left.zRot=Mth.lerp(a,left.zRot,-.12F);
            head.yRot=Mth.lerp(a,head.yRot,-.20F);head.xRot+=.09F*a+.06F*look;head.zRot-=.07F*a;
            return;
        }
        float brush=s.reaction==Reaction.COOK?sweep(s.reactionTime):0;
        for(var arm:new ModelPart[]{left,right}){
            arm.z=Mth.lerp(out,arm.z,s.pumpkin?-2.65F:-1.65F);
            arm.zRot=Mth.lerp(a,arm.zRot,0);
        }
        left.xRot=Mth.lerp(a,left.xRot,-1.35F+.08F*look);
        right.xRot=Mth.lerp(a,right.xRot,-1.35F+.08F*look-.22F*brush);
        left.yRot=Mth.lerp(a,left.yRot,1.03F);
        right.yRot=Mth.lerp(a,right.yRot,-1.03F-.42F*brush);
        head.xRot+=.17F*a+.05F*look;head.yRot+=.10F*look;
        // The harvest turns gently between both hands; cooking brushes one palm across the other twice.
        if(s.reaction==Reaction.HARVEST){left.xRot+=.07F*look;right.xRot-=.07F*look;}
    }
}
