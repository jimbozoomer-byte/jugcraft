package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.*;
import org.joml.Vector3f;

public final class HandlingChecks {
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean pink:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true}){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();
            s.jughead=pink;s.pumpkin=pumpkin;s.actionPose=true;s.walkAnimationPos=2;s.walkAnimationSpeed=.4F;
            for(float height:new float[]{18.5F,21,24})for(var gesture:new CompanionGesture[]{CompanionGesture.TAKE_READY,CompanionGesture.TAKE,CompanionGesture.CARRY,CompanionGesture.PUT_READY,CompanionGesture.PUT}){
                s.gesture=gesture;s.handlingLead=.6F;s.handlingLoad=2;s.handlingRemainder=false;Vector3f prior=null;
                for(int tick=0;tick<=40;tick++){
                    HandlingPose.prepare(s,tick/40F,.4F,height,-12);model.setupAnim(s);
                    for(var name:new String[]{"left_arm","right_arm"}){var arm=root.getChild(name);check.accept(arm.xScale==1 && arm.yScale==1 && arm.zScale==1,"handling never stretches arms on either rig/costume");}
                    var item=HandlingPose.itemPosition(model);var left=model.taskHandPosition(true);var right=model.taskHandPosition(false);
                    check.accept(new Vector3f(item).add(0,.3F,0).distance(new Vector3f(left).add(right).mul(.5F))<.0001F,"prop stays between actual hands");
                    if(prior!=null)check.accept(prior.distance(item)<1.1F,"handling has continuous hand/prop movement");prior=item;
                    check.accept(root.getChild("left_leg").xRot!=0 && root.getChild("left_leg").y==22.5F,"handling preserves attached walking feet");
                    if(gesture==CompanionGesture.TAKE_READY)check.accept(HandlingPose.itemScale(s)==0,"anticipation never invents cargo before successful pickup");
                    if(gesture==CompanionGesture.PUT && tick==40)check.accept(root.getChild("body").xRot==0 && HandlingPose.itemScale(s)==0,"empty placement returns to idle");
                }
            }
            s.handlingLoad=1;s.handlingRemainder=true;s.gesture=CompanionGesture.PUT;
            HandlingPose.prepare(s,1,0,23,-12);model.setupAnim(s);var last=HandlingPose.itemPosition(model);
            s.gesture=CompanionGesture.CARRY;HandlingPose.prepare(s,0,0,23,-12);model.setupAnim(s);
            check.accept(last.distance(HandlingPose.itemPosition(model))<.0001F,"partial delivery joins carry without a position jump");
            s.gesture=CompanionGesture.TAKE;HandlingPose.prepare(s,1,0,23,-12);model.setupAnim(s);
            check.accept(last.distance(HandlingPose.itemPosition(model))<.0001F,"pickup joins the same carrying pose");
            s.gesture=CompanionGesture.PUT_READY;HandlingPose.prepare(s,.6F,0,23,-12);model.setupAnim(s);last=HandlingPose.itemPosition(model);
            s.gesture=CompanionGesture.PUT;s.handlingLead=.6F;HandlingPose.prepare(s,0,0,23,-12);model.setupAnim(s);
            check.accept(last.distance(HandlingPose.itemPosition(model))<.0001F,"confirmed placement continues from its anticipated reach");
        }
    }
}
