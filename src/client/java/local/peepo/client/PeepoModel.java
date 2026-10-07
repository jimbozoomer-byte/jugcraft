package local.peepo.client;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
public final class PeepoModel extends EntityModel<PeepoState> {
    private final ModelPart head,leftArm,rightArm,leftLeg,rightLeg,body,hips,pumpkin,hat,shorts,jug;
    public PeepoModel(ModelPart root) {
        super(root);head=root.getChild("head");
        body=root.getChild("body");hips=root.getChild("hips");pumpkin=root.getChild("pumpkin");hat=head.getChild("pumpkin_hat");
        shorts=root.getChild("jughead_shorts");jug=head.getChild("jughead_jug");
        leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");
        leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");
    }
    public void translateToHand(com.mojang.blaze3d.vertex.PoseStack pose){
        rightArm.translateAndRotate(pose);
        pose.translate(0,2.6F/16F,0);
    }
    @Override public void setupAnim(PeepoState s) {
        super.setupAnim(s);
        head.getChild("sleeping_eyes").visible=s.sleeping;
        for (String side : new String[]{"left", "right"}) {
            for (int i=0;i<4;i++) {
                head.getChild(side+"_eye"+i).visible=!s.sleeping;
                head.getChild(side+"_eye_rim"+i).visible=!s.sleeping;
            }
            for (String part : new String[]{"pupil", "shine", "upper_eyelid", "lower_eyelid"})
                head.getChild(side+"_"+part).visible=!s.sleeping;
        }
        boolean jugheadClothes=s.jughead && !s.pumpkin;
        shorts.visible=jugheadClothes;jug.visible=jugheadClothes;
        leftLeg.getChild("left_shorts_cuff").visible=jugheadClothes;
        rightLeg.getChild("right_shorts_cuff").visible=jugheadClothes;
        body.visible=!s.pumpkin;hips.visible=!s.pumpkin;
        pumpkin.visible=s.pumpkin;hat.visible=s.pumpkin;
        leftArm.getChild("left_sleeve").visible=!s.pumpkin && !s.jughead;
        rightArm.getChild("right_sleeve").visible=!s.pumpkin && !s.jughead;
        leftArm.getChild("left_bare_shoulder").visible=s.pumpkin || s.jughead;
        rightArm.getChild("right_bare_shoulder").visible=s.pumpkin || s.jughead;
        if(s.pumpkin) { leftArm.x=3.69F;rightArm.x=-3.69F; }
        head.yRot=Mth.clamp(s.yRot,-40,40)*Mth.DEG_TO_RAD;
        head.xRot=Mth.clamp(s.xRot,-20,20)*Mth.DEG_TO_RAD;
        float walk=s.walkAnimationPos*1.3F, speed=Math.min(s.walkAnimationSpeed,.7F);
        leftLeg.xRot=Mth.cos(walk)*speed;rightLeg.xRot=Mth.cos(walk+Mth.PI)*speed;
        leftArm.xRot=rightLeg.xRot*.65F;rightArm.xRot=leftLeg.xRot*.65F;
        leftArm.zRot=-.06F-Mth.sin(s.ageInTicks*.06F)*.025F;
        rightArm.zRot=.06F+Mth.sin(s.ageInTicks*.06F)*.025F;
        if(s.wheelRunning && !s.eating) {
            // Twelve ticks per stride; bob twice per stride. Position stays on the wheel.
            float phase=s.ageInTicks*Mth.TWO_PI/12F;
            float stride=Mth.sin(phase), bob=(1-Mth.cos(phase*2))*.12F;
            leftLeg.xRot=stride*.85F;rightLeg.xRot=-stride*.85F;
            leftArm.xRot=-.30F-stride*.60F;rightArm.xRot=-.30F+stride*.60F;
            leftArm.zRot=-.10F;rightArm.zRot=.10F;
            head.yRot=0;head.xRot=.10F+Mth.sin(phase*2)*.025F;
            for(ModelPart part:new ModelPart[]{head,body,hips,pumpkin,shorts,leftArm,rightArm,leftLeg,rightLeg})part.y-=bob;
        }
        if(s.eating) {
            float bite=Mth.sin(s.eatingTime*.8F)*.06F;
            leftArm.xRot=rightArm.xRot=-2.05F+bite;
            leftArm.yRot=s.pumpkin ? 1.05F : .8F;rightArm.yRot=-leftArm.yRot;
            leftArm.zRot=rightArm.zRot=0;
            leftArm.z=rightArm.z=s.pumpkin ? -2.2F : -1.5F;
            head.yRot=0;head.xRot=.08F+bite*.5F;
        }
        if(s.sitting && !s.eating) {
            float kick=Mth.sin(s.ageInTicks*.16F)*.30F;
            leftLeg.xRot=-1.05F+kick;rightLeg.xRot=-1.05F-kick;
            leftArm.xRot=rightArm.xRot=-.70F;
            leftArm.yRot=.35F;rightArm.yRot=-.35F;
            leftArm.zRot=rightArm.zRot=0;
            head.xRot=.04F;
        }
        if(!s.held.isEmpty() && !s.eating && !s.wheelRunning && !s.sleeping)rightArm.xRot=-.65F;
        if(s.sleeping) {
            head.xRot=head.yRot=0;
            leftLeg.xRot=rightLeg.xRot=0;
            leftArm.xRot=rightArm.xRot=0;
            leftArm.zRot=-.08F;rightArm.zRot=.08F;
        }
    }
}
