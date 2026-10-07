package local.peepo.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared procedural clips on the existing rig. Work targets/timing stay server-authoritative. */
final class CompanionWorkPose {
    static final float GRIP_Y=17.5F, GRIP_Z=-3.4F;
    static void interact(PeepoState s,ModelPart left,ModelPart right,ModelPart head){
        float lift=Mth.sin(s.workPhase*3)*.24F;
        left.xRot=right.xRot=-1.45F+lift;
        left.yRot=.28F;right.yRot=-.28F;left.zRot=right.zRot=0;
        head.yRot=0;head.xRot=.12F+lift*.08F;
    }
    static void stir(PeepoState s,ModelPart left,ModelPart right,ModelPart head,ModelPart leftLeg,ModelPart rightLeg){
        grip(left,1);grip(right,-1);
        head.yRot=0;head.xRot=.12F;
        float kick=Mth.sin(s.workPhase*2)*.16F;
        leftLeg.xRot=.22F+kick;rightLeg.xRot=.22F-kick;
    }
    private static void grip(ModelPart arm,int side){
        // Move the shoulders towards the front of the broad torso; both hands share the spoon grip.
        arm.x=side*2.3F;arm.y=19.2F;arm.z=-1.4F;
        float dx=side*.35F-arm.x,dy=GRIP_Y-arm.y,dz=GRIP_Z-arm.z;
        float length=Mth.sqrt(dx*dx+dy*dy+dz*dz);
        arm.xRot=-(float)Math.acos(dy/length);arm.yRot=(float)Math.atan2(-dx,-dz);arm.zRot=0;
        arm.yScale=length/2.7F;
    }
}
