package local.peepo.client;

import local.peepo.CompanionGesture;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Smooth endpoint handling; native-length arms and props attached to the resulting hands. */
final class HandlingPose {
    private static float ease(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    static void prepare(PeepoState s,float t,float targetX,float targetY,float targetZ){
        s.handling=true;s.handlingTime=t;s.handlingWeight=1;
        float lead=.65F*ease(s.handlingLead),reach=0;
        switch(s.gesture){
            case TAKE_READY,PUT_READY -> reach=.65F*ease(t);
            case TAKE -> reach=t<.25F?Mth.lerp(ease(t/.25F),lead,1):1-ease((t-.25F)/.75F);
            case PUT -> {
                reach=t<.5F?Mth.lerp(ease(t/.5F),lead,1):1-ease((t-.65F)/.35F);
                if(!s.handlingRemainder)s.handlingWeight=1-ease((t-.74F)/.26F);
            }
            default -> {}
        }
        s.handlingReach=reach;
        if(s.gesture==CompanionGesture.TAKE_READY)s.handlingWeight=ease(t/.4F);
        var p=s.toolPose;
        float bob=Mth.sin(s.walkAnimationPos*1.3F)*Math.min(s.walkAnimationSpeed,.5F)*.20F*(1-reach);
        float carryY=20.1F+s.handlingLoad*.10F+bob,carryZ=s.pumpkin?-5.05F:-3.85F;
        p.x=Mth.lerp(reach,0,Mth.clamp(targetX,-1.1F,1.1F));
        p.y=Mth.lerp(reach,carryY,Mth.clamp(targetY,18.5F,24));p.z=Mth.lerp(reach,carryZ,targetZ);
        p.xRot=p.zRot=0;p.bodyX=0;p.bodyRoll=Mth.sin(s.walkAnimationPos*.65F)*Math.min(s.walkAnimationSpeed,.4F)*.035F*(1-reach);
        p.bodyPitch=(.035F*s.handlingLoad+reach*(targetY>21?.32F:.18F))*s.handlingWeight;
        p.bodyY=reach*.20F*s.handlingWeight;
        p.grips(.30F,1.50F,0);
    }
    static float itemScale(PeepoState s){
        float t=s.handlingTime;
        if(s.gesture==CompanionGesture.TAKE_READY)return 0;
        if(s.gesture==CompanionGesture.TAKE)return ease(t/.22F);
        if(s.gesture==CompanionGesture.PUT)return t<.72F?1-ease((t-.62F)/.10F):s.handlingRemainder?ease((t-.78F)/.22F):0;
        return 1;
    }
    static Vector3f itemPosition(PeepoModel model){return model.taskHandPosition(true).add(model.taskHandPosition(false)).mul(.5F).add(0,-.30F,0);}
    static void apply(PeepoState s,ModelPart left,ModelPart right,ModelPart head,ModelPart[] torso){
        var p=s.toolPose;float weight=s.handlingWeight;
        for(var part:torso)CompanionWorkPose.lean(part,p.bodyPitch,p.bodyRoll,p.bodyX,p.bodyY);
        head.xRot=Mth.lerp(weight,head.xRot,.14F+s.handlingReach*.16F);
        head.yRot=Mth.lerp(weight,head.yRot,-p.x*.09F);
        fixedArm(s,left,true);fixedArm(s,right,false);
    }
    private static void fixedArm(PeepoState s,ModelPart arm,boolean left){
        var p=s.toolPose;float weight=s.handlingWeight,oldX=arm.xRot,oldY=arm.yRot,oldZ=arm.zRot;
        arm.x=Mth.lerp(weight,arm.x,(left?1:-1)*(s.pumpkin?2.9F:2.3F));
        arm.y=Mth.lerp(weight,arm.y,19.2F);arm.z=Mth.lerp(weight,arm.z,s.pumpkin?-2.6F:-1.4F);
        CompanionWorkPose.lean(arm,p.bodyPitch,p.bodyRoll,p.bodyX,p.bodyY);
        float x=(left?p.leftX:p.rightX)-arm.x,y=(left?p.leftY:p.rightY)-arm.y,z=(left?p.leftZ:p.rightZ)-arm.z;
        float length=Mth.sqrt(x*x+y*y+z*z);
        if(length>.0001F){arm.xRot=Mth.lerp(weight,oldX,-(float)Math.acos(Mth.clamp(y/length,-1,1)));arm.yRot=Mth.lerp(weight,oldY,(float)Math.atan2(-x,-z));arm.zRot=Mth.lerp(weight,oldZ,0);}
        arm.xScale=arm.yScale=arm.zScale=1;
    }
}
