package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.CompanionFeedbackRules.Reaction;

/** Actual shared rig checks, including planted feet, reset and closed eyes on both variants. */
public final class FeedbackChecks {
    private FeedbackChecks(){}
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean jug:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true}){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var state=new PeepoState();state.jughead=jug;state.pumpkin=pumpkin;
            for(var reaction:Reaction.values())if(reaction!=Reaction.NONE){
                state.reaction=reaction;var heads=new java.util.HashSet<Float>();
                for(int t:new int[]{0,5,12,20,28,36,40}){
                    state.reactionTime=t;model.setupAnim(state);heads.add(root.getChild("head").xRot);
                    for(String name:new String[]{"left_leg","right_leg"}){
                        var part=root.getChild(name);check.accept(part.y==22.5F && part.z==0 && part.xRot==0 && part.yScale==1,"reaction keeps feet planted and legs attached");
                    }
                    for(String name:new String[]{"left_arm","right_arm"}){var part=root.getChild(name);check.accept(part.yScale==1 && Float.isFinite(part.xRot),"reaction preserves short arm lengths");}
                    check.accept(root.getChild("head").getChild("sleeping_eyes").visible==(reaction==Reaction.TIRED && t>10 && t<29),"only yawn closes eyes during its middle");
                }
                check.accept(heads.size()>2,"every clip has distinct eased phases");
                state.reaction=Reaction.NONE;model.setupAnim(state);
                check.accept(root.getChild("head").xRot==0 && root.getChild("head").zRot==0 && !root.getChild("head").getChild("sleeping_eyes").visible,"leaving reaction resets head and eyes");
            }
            state.reaction=Reaction.PLEASED;
            float[] heights=new float[3];int n=0;
            for(int tick:new int[]{12,18,23}){
                state.reactionTime=tick;model.setupAnim(state);
                var arm=root.getChild("left_arm");var hand=new org.joml.Vector3f(0,2.7F,0);
                new org.joml.Quaternionf().rotationZYX(arm.zRot,arm.yRot,arm.xRot).transform(hand);heights[n++]=arm.y+hand.y;
                check.accept(root.getChild("right_arm").xRot==0 && root.getChild("right_arm").yRot==0,"fist pump leaves the other arm resting");
            }
            check.accept(heights[0]<heights[1]-.7F && heights[2]<heights[1]-.7F,"fist pump has two distinct upward strokes");
            state.reaction=Reaction.RELIEVED;state.reactionTime=20;model.setupAnim(state);
            var arm=root.getChild("right_arm");var hand=new org.joml.Vector3f(0,2.7F,0);
            new org.joml.Quaternionf().rotationZYX(arm.zRot,arm.yRot,arm.xRot).transform(hand);hand.add(arm.x,arm.y,arm.z);
            check.accept(Math.abs(hand.x)<1.6F && hand.y>19.5F && hand.y<21.5F && hand.z<(pumpkin?-3.4F:-2.3F),"relieved hand rests in front of chest, not spread beside the body");
            check.accept(root.getChild("left_arm").xRot==0 && root.getChild("left_arm").yRot==0,"hand-to-chest keeps the free arm relaxed");
            for(int tick:new int[]{6,10,20,30}){
                state.reactionTime=tick;model.setupAnim(state);arm=root.getChild("right_arm");
                var forearm=new org.joml.Vector3f(0,.9F,0);
                new org.joml.Quaternionf().rotationZYX(arm.zRot,arm.yRot,arm.xRot).transform(forearm);
                check.accept(arm.z+forearm.z<(pumpkin?-3.5F:-2.3F),"hand-to-chest moves around the front before folding inward");
            }
            state.reaction=Reaction.NONE;state.work=local.peepo.WorkAnimation.FISH_WAIT;FishingPose.evaluate(state);model.setupAnim(state);
            var right=root.getChild("right_arm");var leg=root.getChild("left_leg");
            float rx=right.xRot,ry=right.yRot,rz=right.z,scale=right.yScale,ly=leg.y,lz=leg.z,lp=leg.xRot;
            state.reaction=Reaction.CATCH;
            for(int tick:new int[]{0,8,16,24,32,40}){
                state.reactionTime=tick;model.setupAnim(state);right=root.getChild("right_arm");leg=root.getChild("left_leg");
                check.accept(right.xRot==rx && right.yRot==ry && right.z==rz && right.yScale==scale,"admiring catch preserves the rod hand exactly");
                check.accept(leg.y==ly && leg.z==lz && leg.xRot==lp,"admiring catch preserves attached seated legs and kicking");
                var pos=model.feedbackItemPosition(false);check.accept(Float.isFinite(pos.x) && Float.isFinite(pos.y) && Float.isFinite(pos.z),"caught item follows the actual left hand");
                if(tick==16)check.accept(root.getChild("left_arm").yScale==1,"raised catch hand returns to its natural short length");
            }
            state.work=local.peepo.WorkAnimation.NONE;state.reaction=Reaction.HARVEST;state.reactionTime=20;model.setupAnim(state);
            var produce=model.feedbackItemPosition(true);
            check.accept(Math.abs(produce.x)<.2F && produce.y>19 && produce.y<21 && produce.z<(pumpkin?-3.4F:-2.5F),"harvest is held between both hands in front of clothing");
            check.accept(FinishingPose.sweep(13)>FinishingPose.sweep(18) && FinishingPose.sweep(24)>FinishingPose.sweep(18),"flour brushing has two separate strokes");
        }
    }
}
