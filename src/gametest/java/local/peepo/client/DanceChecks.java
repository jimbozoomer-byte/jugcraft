package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.CompanionSocial;
import local.peepo.WorkAnimation;

public final class DanceChecks {
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean pink:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true})for(int mirror:new int[]{-1,1})for(int style=0;style<3;style++){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();
            s.jughead=pink;s.pumpkin=pumpkin;s.social=CompanionSocial.DANCE;s.socialTime=40;s.danceMirror=mirror;s.danceStyle=style;
            org.joml.Vector3f previous=null;
            for(int t=0;t<=160;t++){
                s.ageInTicks=200+t;s.dancePhase=t;model.setupAnim(s);
                for(String name:new String[]{"left_arm","right_arm","left_leg","right_leg"}){
                    var part=root.getChild(name);check.accept(part.xScale==1 && part.yScale==1 && part.zScale==1,"dance retains every authored limb length");
                    check.accept(Float.isFinite(part.x+part.y+part.z+part.xRot+part.yRot+part.zRot),"dance pose remains finite");
                }
                var leg=root.getChild("left_leg");check.accept(Math.abs(leg.y-22.5F)<.21F && Math.abs(leg.x-1.26F)<.02F,"little legs remain attached to the rocking hips");
                var hand=model.taskHandPosition(true);if(previous!=null)check.accept(hand.distance(previous)<1.1F,"dance hand path is continuous");previous=hand;
            }
            s.dancePhase=10;model.setupAnim(s);float first=root.getChild("left_leg").xRot-root.getChild("right_leg").xRot;
            s.dancePhase=30;model.setupAnim(s);float second=root.getChild("left_leg").xRot-root.getChild("right_leg").xRot;
            check.accept(first*second<0,"toe taps alternate between feet");
            for(float entered:new float[]{2,6,40}){
                s.socialTime=entered;s.danceRelease=10;model.setupAnim(s);
                check.accept(DancePose.weight(s)==0 && root.getChild("head").zRot==0 && root.getChild("left_leg").xRot==0 && root.getChild("left_arm").xRot==0,"early and late music stops settle fully");
            }
            s.danceRelease=-1;s.work=WorkAnimation.INTERACT;check.accept(!SocialPose.active(s),"work owns the rig over a stale dance");
            s.work=WorkAnimation.NONE;s.rest.active=true;check.accept(!SocialPose.active(s),"rest transitions own the rig over dancing");
        }
    }
    public static void held(BiConsumer<Boolean,String> check,PeepoState s){
        var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);s.social=CompanionSocial.DANCE;s.socialTime=40;s.danceRelease=-1;s.danceStyle=0;
        check.accept(!s.held.isEmpty(),"native renderer resolves the carried light/tool");
        for(int t=0;t<=160;t+=4){s.dancePhase=t;s.ageInTicks=200+t;model.setupAnim(s);var arm=root.getChild("right_arm");
            check.accept(Math.abs(arm.xRot-(s.holdingLight?-2.55F:-.65F))<.001F && arm.yRot==0,"held light/tool keeps its normal pitch and yaw");
            check.accept(Math.abs(arm.zRot-(s.holdingLight?.15F:.06F))<.026F,"held arm is not recruited for a dance wave");
        }
    }
}
