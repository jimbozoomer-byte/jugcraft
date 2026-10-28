package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.CompanionRestMotion.Clip;
import net.minecraft.world.phys.Vec3;

public final class RestChecks {
    public static void run(BiConsumer<Boolean,String> check){
        var entry=new Vec3(2.5,65,3.5);var mount=new Vec3(2.5,67.375,2.5);
        for(boolean pink:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true})for(var kind:new Clip[]{Clip.SIT,Clip.STAND,Clip.SLEEP,Clip.WAKE})for(int climb:new int[]{0,28}){
            if(!kind.bed() && climb>0)continue;
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();s.jughead=pink;s.pumpkin=pumpkin;s.sleeping=kind.bed();s.sitting=!kind.bed();
            Vec3 previous=null;
            for(int frame=0;frame<=120;frame++){
                float age=frame*(kind.ticks()+climb)/120F;s.ageInTicks=age;s.rest.sample(kind,age,climb);model.setupAnim(s);
                for(String limb:new String[]{"left_arm","right_arm","left_leg","right_leg"}){
                    var p=root.getChild(limb);check.accept(p.xScale==1 && p.yScale==1 && p.zScale==1,"rest keeps normal limb lengths");check.accept(Float.isFinite(p.xRot+p.yRot+p.zRot),"finite rest rotation");
                }
                check.accept(root.getChild("left_leg").y==22.5F && root.getChild("right_leg").y==22.5F,"rest feet stay attached");
                var position=RestPose.position(kind,age,climb,entry,mount);
                if(previous!=null)check.accept(previous.distanceTo(position)<.35,"continuous furniture-relative motion");previous=position;
                if(s.rest.climbing)check.accept(Math.abs(position.x-entry.x)<.0001 && Math.abs(position.z-entry.z)<.0001,"bunk travel stays in the checked front access column");
                if(s.rest.grip>.999F){var hands=model.taskHandPosition(true).add(model.taskHandPosition(false)).mul(.5F);check.accept(RestPose.blanketEdge(s,model).distance(hands)<.001,"blanket fold follows both hands");}
            }
            var first=RestPose.position(kind,0,climb,entry,mount);var last=RestPose.position(kind,kind.ticks()+climb,climb,entry,mount);
            check.accept(first.distanceTo(kind.exit()?mount:entry)<.0001 && last.distanceTo(kind.exit()?entry:mount)<.0001,"furniture endpoints join without a snap");
        }
        var pose=new RestPose();pose.sample(Clip.SLEEP,60,28);check.accept(pose.eyesClosed && pose.pull==1 && pose.lie==1,"sleep closes eyes and covers body");
        pose.sample(Clip.WAKE,5,28);check.accept(!pose.eyesClosed && pose.pull<1,"wake opens eyes while removing blanket");
        pose.sample(Clip.SLEEP,0,28);check.accept(!pose.eyesClosed && pose.climbing && pose.pull==0,"climbing has open eyes and no blanket");
    }
}
