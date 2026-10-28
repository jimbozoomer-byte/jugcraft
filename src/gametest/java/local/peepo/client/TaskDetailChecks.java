package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.*;

public final class TaskDetailChecks {
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean pink:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true}){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();s.jughead=pink;s.pumpkin=pumpkin;
            for(int clip=0;clip<5;clip++){
                s.detail=switch(clip){case 0->CompanionGesture.TOOL_DRAW;case 1->CompanionGesture.TOOL_STOW;case 2->CompanionGesture.BREW_ADD;case 3->CompanionGesture.BREW_SWIRL;default->CompanionGesture.NONE;};
                s.toolHandling=clip<2;s.fertilizing=clip==4;s.work=clip==4?WorkAnimation.FERTILIZE:WorkAnimation.NONE;
                var poses=new java.util.HashSet<Float>();
                for(int tick=0;tick<=20;tick++){
                    s.detailTime=tick/20F;model.setupAnim(s);poses.add(root.getChild("right_arm").xRot);
                    for(var name:new String[]{"left_arm","right_arm"}){
                        var arm=root.getChild(name);check.accept(arm.yScale==1 && Float.isFinite(arm.xRot),"task detail never stretches short arms");
                    }
                    for(var name:new String[]{"left_leg","right_leg"}){var leg=root.getChild(name);check.accept(leg.y==22.5F && leg.z==0 && leg.xRot==0,"detail preserves planted attached feet");}
                    var at=model.taskHandPosition(false);check.accept(Float.isFinite(at.x) && Float.isFinite(at.y) && Float.isFinite(at.z),"prop uses actual hand endpoint");
                    if(s.detail==CompanionGesture.BREW_ADD){
                        var bottle=TaskDetailPose.brewBottle(model.taskHandPosition(true),at,TaskDetailPose.brewSwirl(s.detailTime));
                        var ingredient=TaskDetailPose.brewIngredient(at,bottle,s.detailTime);
                        if(s.detailTime<=.55F)check.accept(ingredient.distance(at)<.0001F,"ingredient stays in fingers until hand reaches bottle");
                        if(s.detailTime>=.20F && s.detailTime<=.65F)check.accept(at.z< -3.5F,"ingredient hand stays forward of the face");
                        if(s.detailTime>=.55F && s.detailTime<=.65F)check.accept(at.distance(new org.joml.Vector3f(bottle).add(0,-1.2F,0))<1.8F,"ingredient travels only a short distance into bottle neck");
                    }
                }
                check.accept(poses.size()>4,"each clip has eased distinct phases");
            }
            s.detail=CompanionGesture.NONE;s.toolHandling=s.fertilizing=false;s.work=WorkAnimation.NONE;model.setupAnim(s);
            check.accept(root.getChild("head").xRot==0 && root.getChild("body").xRot==0 && root.getChild("right_arm").z==0,"all detail pose transforms reset");
            s.walkAnimationSpeed=.4F;s.walkAnimationPos=1;model.setupAnim(s);float walk=root.getChild("left_leg").xRot;
            s.detail=CompanionGesture.BREW_SWIRL;s.detailTime=.5F;model.setupAnim(s);
            check.accept(root.getChild("left_leg").xRot==walk,"bottle swirl keeps the delivery walking animation");
            s.detail=CompanionGesture.NONE;s.work=WorkAnimation.HARVEST;s.toolPose.harvest(1,24,-5);model.setupAnim(s);
            float pitch=root.getChild("right_arm").xRot,scale=root.getChild("right_arm").yScale;
            s.detail=CompanionGesture.TOOL_DRAW;s.toolHandling=true;s.detailTime=1;model.setupAnim(s);
            check.accept(root.getChild("right_arm").xRot==pitch && root.getChild("right_arm").yScale==scale,"drawing blends back to the exact working arm pose");
            var grip=TaskDetailPose.toolPosition(s,model.taskHandPosition(false));
            check.accept(grip.distance(s.toolPose.x,s.toolPose.y,s.toolPose.z)<.001F && TaskDetailPose.toolBlend(s)==1,"drawn tool finishes at the original work prop grip");
        }
    }
}
