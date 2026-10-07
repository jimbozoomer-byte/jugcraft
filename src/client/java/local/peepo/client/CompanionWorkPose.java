package local.peepo.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared procedural clips on the existing rig. Work targets/timing stay server-authoritative. */
final class CompanionWorkPose {
    static final float GRIP_Y=20F, GRIP_Z=-3.4F;
    static float gripX(PeepoState s){return Mth.cos(s.workPhase)*.65F;}
    static float gripZ(PeepoState s){return GRIP_Z+Mth.sin(s.workPhase)*.65F;}
    static void interact(PeepoState s,ModelPart left,ModelPart right,ModelPart head){
        float lift=Mth.sin(s.workPhase*3)*.24F;
        left.xRot=right.xRot=-1.45F+lift;
        left.yRot=.28F;right.yRot=-.28F;left.zRot=right.zRot=0;
        head.yRot=0;head.xRot=.12F+lift*.08F;
    }
    static void stir(PeepoState s,ModelPart left,ModelPart right,ModelPart head,ModelPart leftLeg,ModelPart rightLeg){
        grip(s,left,1);grip(s,right,-1);
        head.yRot=0;head.xRot=.18F;
        // Keep both feet planted on the rim while the hands circle the spoon.
        leftLeg.xRot=rightLeg.xRot=0;
        leftLeg.yRot=rightLeg.yRot=leftLeg.zRot=rightLeg.zRot=0;
    }
    private static void grip(PeepoState s,ModelPart arm,int side){
        // Move the shoulders towards the front of the broad torso; both hands share the spoon grip.
        arm.x=side*2.3F;arm.y=19.2F;arm.z=-1.4F;
        reach(arm,gripX(s)+side*.35F,GRIP_Y,gripZ(s));
    }
    static void tool(PeepoState s,ModelPart left,ModelPart right,ModelPart head,ModelPart leftLeg,ModelPart rightLeg){
        var p=s.toolPose;
        left.x=s.pumpkin?2.9F:2.3F;right.x=-left.x;
        left.y=right.y=19.2F;left.z=right.z=s.pumpkin?-2.6F:-1.4F;
        reach(left,p.leftX,p.leftY,p.leftZ);reach(right,p.rightX,p.rightY,p.rightZ);
        head.yRot=0;head.xRot=.14F;
        leftLeg.xRot=rightLeg.xRot=0;
        leftLeg.yRot=rightLeg.yRot=leftLeg.zRot=rightLeg.zRot=0;
    }
    private static void reach(ModelPart arm,float x,float y,float z){
        float dx=x-arm.x,dy=y-arm.y,dz=z-arm.z;
        float length=Mth.sqrt(dx*dx+dy*dy+dz*dz);
        arm.xRot=-(float)Math.acos(Mth.clamp(dy/length,-1,1));arm.yRot=(float)Math.atan2(-dx,-dz);arm.zRot=0;
        arm.yScale=length/2.7F;
    }
}
