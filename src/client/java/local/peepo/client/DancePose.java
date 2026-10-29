package local.peepo.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** A shared two-second groove, not audio/BPM analysis. All bones stay at their authored lengths. */
final class DancePose {
    private DancePose(){}
    private static float ease(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    static float weight(PeepoState s){return ease(s.socialTime/12)*(s.danceRelease<0?1:1-ease(s.danceRelease/10));}
    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart leftLeg,ModelPart rightLeg,ModelPart[] torso){
        if(!SocialPose.active(s))return;
        float weight=weight(s),phase=s.dancePhase*Mth.TWO_PI/40;
        float sway=Mth.sin(phase)*s.danceMirror,rock=.08F*sway*weight;
        // Rotate the hips and every attached bone together. The weight-bearing foot stays near the floor.
        float rise=-Math.abs(Mth.sin(rock))*1.26F,pitch=.025F*(1-Mth.cos(phase*2))*weight;
        for(var part:torso)CompanionWorkPose.lean(part,pitch,rock,0,rise);
        CompanionWorkPose.lean(left,pitch,rock,0,rise);CompanionWorkPose.lean(right,pitch,rock,0,rise);
        CompanionWorkPose.lean(leftLeg,pitch,rock,0,rise);CompanionWorkPose.lean(rightLeg,pitch,rock,0,rise);
        float leftTap=Math.max(0,-sway),rightTap=Math.max(0,sway);
        leftLeg.xRot-=.38F*leftTap*leftTap*weight;rightLeg.xRot-=.38F*rightTap*rightTap*weight;
        left.xRot+=(-.5F+.25F*Mth.sin(phase+.5F))*weight;left.zRot-=.16F*weight;
        if(s.held.isEmpty()){right.xRot+=(-.5F-.25F*Mth.sin(phase+.5F))*weight;right.zRot+=.16F*weight;}
        else {
            // Keep the light/tool arm's angle; its shoulder follows the same small body rock.
            right.xRot-=pitch;right.zRot-=rock;
        }
        head.xRot+=(.055F*Mth.sin(phase*2)-.015F)*weight;
        head.zRot-=rock*.45F;
        float notice=ease(s.socialTime/3)*(1-ease((s.socialTime-5)/7))*(s.danceRelease<0?1:1-ease(s.danceRelease/10));head.zRot+=.10F*notice;
        // One small variation in four loops. It shares the music phase, never a repeating network event.
        float variation=ease((s.dancePhase-82)/8)*(1-ease((s.dancePhase-106)/12))*weight;
        if(s.danceStyle==0){
            var waving=s.held.isEmpty() && s.danceMirror<0?right:left;
            waving.zRot+=(waving==left?-1:1)*(1.65F+.16F*Mth.sin(phase*3))*variation;
        }else{
            float wiggle=.09F*Mth.sin(phase*4)*variation;
            head.zRot+=wiggle;left.xRot+=wiggle;
            if(s.held.isEmpty())right.xRot-=wiggle;
        }
    }
}
