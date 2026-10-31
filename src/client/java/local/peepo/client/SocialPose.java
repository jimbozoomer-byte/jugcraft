package local.peepo.client;

import local.peepo.CompanionSocial;
import local.peepo.WorkAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Stationary paired clips on the native rig. No stretched arms, moving feet or client-side AI. */
final class SocialPose {
    private SocialPose(){}
    private static float ease(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    private static float window(float t,float rise,float hold,float release,float end){return ease((t-rise)/(hold-rise))*(1-ease((t-release)/(end-release)));}
    static boolean active(PeepoState s){return s.social!=CompanionSocial.NONE && !s.actionPose && !s.toolHandling && !s.fertilizing
        && !s.eating && !s.sleeping && !s.sitting && !s.rest.active && !s.wheelRunning && s.work==WorkAnimation.NONE;}
    private static boolean laughing(PeepoState s){return s.social==CompanionSocial.LAUGH_FIRST || s.social==CompanionSocial.LAUGH_SECOND;}
    private static float laughAge(PeepoState s){return s.socialTime-(s.social==CompanionSocial.LAUGH_FIRST?7:0);}
    static boolean eyesClosed(PeepoState s){return active(s) && laughing(s) && laughAge(s)>10 && laughAge(s)<34;}
    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart[] torso){
        if(!active(s))return;
        float t=s.socialTime;
        switch(s.social){
            case CompanionSocial.WAVE -> wave(left,head,t,40);
            case CompanionSocial.TALK -> {
                float gesture=Mth.sin(t*.3F),fade=window(t,0,4,26,30);
                left.xRot=(-.8F+gesture*.2F)*fade;left.yRot=.3F*fade;left.zRot=Mth.lerp(fade,left.zRot,-.25F);
                if(s.held.isEmpty()){right.xRot=(-.6F-gesture*.18F)*fade;right.yRot=-.25F*fade;right.zRot=Mth.lerp(fade,right.zRot,.2F);}
                head.xRot+=Mth.sin(t*.28F)*.07F*fade;
            }
            case CompanionSocial.LISTEN -> head.xRot+=Mth.sin(t*.16F)*.045F*window(t,0,4,26,30);
            case CompanionSocial.HIGH_FIVE_LEFT,CompanionSocial.HIGH_FIVE_RIGHT -> highFive(s,head,left,right,torso);
            case CompanionSocial.LAUGH_FIRST,CompanionSocial.LAUGH_SECOND -> laugh(s,head,left,right,torso);
            case CompanionSocial.FAREWELL_WAVE -> wave(left,head,t,24);
            case CompanionSocial.FAREWELL_NOD -> {
                head.xRot+=.22F*window(t,2,9,11,21);
                left.xRot-=.25F*window(t,3,9,13,24);
            }
            default -> {}
        }
    }
    private static void wave(ModelPart arm,ModelPart head,float t,float end){
        float fade=window(t,0,5,end-6,end);
        arm.xRot=-.35F*fade;arm.yRot=.2F*fade;
        arm.zRot=Mth.lerp(fade,arm.zRot,-2.15F+Mth.sin(t*.65F)*.35F);
        head.xRot+=Mth.sin(t*.18F)*.045F*fade;
    }
    private static void highFive(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart[] torso){
        // Raise beside the face, lean into the palm contact, rebound and give a pleased nod.
        if(!s.held.isEmpty())return;
        boolean useLeft=s.social==CompanionSocial.HIGH_FIVE_LEFT;
        var arm=useLeft?left:right;float t=s.socialTime;
        float lift=window(t,0,9,27,40),contact=window(t,8,18,22,36);
        float pitch=.10F*contact;
        for(var part:torso)CompanionWorkPose.lean(part,pitch,0,0,0);
        CompanionWorkPose.lean(left,pitch,0,0,0);CompanionWorkPose.lean(right,pitch,0,0,0);
        arm.y-=.8F*lift;
        arm.xRot=-.6F*lift;arm.zRot=Mth.lerp(lift,arm.zRot,(useLeft?-1:1)*2.35F);
        // Roll the shoulder around the side of the torso. Solve the final shoulder depth so
        // the 2.7-pixel palm point meets the shared target without scaling either limb.
        float dx=s.socialTarget.x-arm.x,dy=s.socialTarget.y-arm.y;
        float depth=Mth.sqrt(Math.max(0,2.7F*2.7F-dx*dx-dy*dy));
        arm.z=Mth.lerp(contact,arm.z,s.socialTarget.z+depth);
        aim(arm,s.socialTarget.x,s.socialTarget.y,s.socialTarget.z,contact);
        head.xRot+=.15F*window(t,25,30,30,37);
    }
    private static void laugh(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart[] torso){
        float t=laughAge(s),fade=window(t,0,9,32,45);
        float chuckle=Mth.sin(t*.85F),pitch=(.08F+.035F*chuckle)*fade;
        for(var part:torso)CompanionWorkPose.lean(part,pitch,0,0,0);
        CompanionWorkPose.lean(left,pitch,0,0,0);CompanionWorkPose.lean(right,pitch,0,0,0);
        left.z-=(s.pumpkin?3.7F:2.35F)*fade;
        aim(left,s.pumpkin?1.8F:1.2F,20.1F,s.pumpkin?-4.6F:-3.25F,fade);
        left.y-=.13F*chuckle*fade;
        if(s.held.isEmpty()){right.xRot-=.5F*fade;right.zRot+=.16F*fade;right.y-=.10F*chuckle*fade;}
        head.xRot+=(-.09F+.07F*chuckle)*fade;head.zRot+=.045F*Mth.sin(t*.4F)*fade;
    }
    private static void aim(ModelPart arm,float x,float y,float z,float weight){
        float dx=x-arm.x,dy=y-arm.y,dz=z-arm.z,length=Mth.sqrt(dx*dx+dy*dy+dz*dz);
        if(length<.001F)return;
        arm.xRot=Mth.lerp(weight,arm.xRot,-(float)Math.acos(Mth.clamp(dy/length,-1,1)));
        arm.yRot=Mth.lerp(weight,arm.yRot,(float)Math.atan2(-dx,-dz));arm.zRot=Mth.lerp(weight,arm.zRot,0);
    }
}
