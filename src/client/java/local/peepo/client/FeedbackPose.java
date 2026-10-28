package local.peepo.client;

import local.peepo.CompanionFeedbackRules.Reaction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Four eased clips on the existing rig. Feet and limb lengths never change. */
public final class FeedbackPose {
    private FeedbackPose(){}
    private static float smooth(float x){x=Mth.clamp(x,0,1);return x*x*(3-2*x);}
    private static float window(float t,float from,float peak,float release,float end){return smooth((t-from)/(peak-from))*(1-smooth((t-release)/(end-release)));}
    private static float raised(float t){return window(t,0,.22F,.76F,1);}
    private static float pumps(float t){return window(t,.20F,.31F,.31F,.43F)+window(t,.45F,.57F,.57F,.69F);}
    private static float chest(float t){return window(t,.08F,.30F,.68F,.90F);}
    public record Sample(float pitch,float yaw,float tilt,float leftPitch,float rightPitch,float spread,float shoulder,boolean eyesClosed){}
    public static Sample sample(Reaction kind,float ticks){
        float t=Mth.clamp(ticks/40F,0,1),fade=window(t,0,.16F,.76F,1);
        return switch(kind){
            case PLEASED -> new Sample(.16F*pumps(t)-.035F*raised(t),0,-.035F*raised(t),-.15F*raised(t),0,0,0,false);
            case PUZZLED -> new Sample(.08F*fade,Mth.sin(t*Mth.TWO_PI)*.2F*fade,.23F*fade,
                -.7F*window(t,.15F,.4F,.72F,1),-.65F*window(t,.22F,.45F,.72F,1),.65F*window(t,.18F,.42F,.7F,1),-.3F*fade,false);
            case TIRED -> new Sample(-.12F*fade+.3F*window(t,.48F,.66F,.8F,1),0,-.06F*fade,
                -.25F*fade,-2.25F*window(t,.08F,.34F,.7F,.96F),.12F*fade,.12F*fade,t>.25F && t<.72F);
            case RELIEVED -> new Sample(.18F*window(t,.30F,.53F,.53F,.84F),0,0,0,-1.15F*chest(t),0,0,false);
            default -> new Sample(0,0,0,0,0,0,0,false);
        };
    }
    static void apply(PeepoState state,ModelPart head,ModelPart left,ModelPart right){
        var s=sample(state.reaction,state.reactionTime);
        head.xRot+=s.pitch;head.yRot+=s.yaw;head.zRot+=s.tilt;
        left.xRot+=s.leftPitch;right.xRot+=s.rightPitch;
        left.zRot-=s.spread;right.zRot+=s.spread;
        left.y+=s.shoulder;right.y+=s.shoulder;
        if(state.reaction==Reaction.PLEASED){
            float t=Mth.clamp(state.reactionTime/40F,0,1),up=raised(t);
            // One short arm punches upward twice beside the head; the other stays relaxed.
            left.zRot-=1.9F*up+.95F*pumps(t);left.y-=.10F*up;
        }else if(state.reaction==Reaction.RELIEVED){
            float t=Mth.clamp(state.reactionTime/40F,0,1),hand=chest(t);
            // Move around the front shoulder before bringing the hand inward, clear of clothing.
            right.yRot-=Mth.HALF_PI*hand;right.z-=(state.pumpkin?3.75F:2.65F)*window(t,0,.14F,.84F,1);
            right.y-=.10F*window(t,.20F,.40F,.40F,.72F);
        }
        if(state.reaction==Reaction.TIRED){right.yRot-=.7F*window(state.reactionTime/40F,.08F,.34F,.7F,.96F);right.z-=.65F*window(state.reactionTime/40F,.08F,.34F,.7F,.96F);}
    }
}
