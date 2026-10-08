package local.peepo.client;

import java.util.HashSet;
import java.util.function.BiConsumer;
import local.peepo.WorkAnimation;

/** Uses the same clip evaluation and rig as rendering, without making production internals public. */
public final class CompanionClipChecks {
    public static void run(BiConsumer<Boolean,String> check){
        var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);
        for(boolean jughead:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true})
        for(var action:new WorkAnimation[]{WorkAnimation.VALVE,WorkAnimation.LEVER,WorkAnimation.MALLET,WorkAnimation.WRENCH,WorkAnimation.CRANK}){
            var s=new PeepoState();s.jughead=jughead;s.pumpkin=pumpkin;s.work=action;
            var arms=new HashSet<String>();var bodies=new HashSet<String>();
            for(int i=0;i<8;i++){
                s.workPhase=(float)(i*Math.PI/4);s.toolPose.evaluate(action,s.workPhase,pumpkin);
                s.crankX=(float)Math.sin(s.workPhase)*4.5F;s.crankY=16-(float)Math.cos(s.workPhase)*4.5F;s.crankZ=-5;
                model.setupAnim(s);var arm=root.getChild("right_arm");var body=root.getChild("body");
                check.accept(Float.isFinite(arm.xRot) && Float.isFinite(arm.yScale) && Float.isFinite(body.y),"finite pose "+action);
                arms.add(arm.xRot+":"+arm.yRot);bodies.add(body.y+":"+body.z+":"+body.xRot+":"+body.zRot);
            }
            check.accept(arms.size()>2 && bodies.size()>2,"body and arms animate: "+action+", Jughead="+jughead+", pumpkin="+pumpkin);
            s.work=WorkAnimation.NONE;s.pumpkin=false;model.setupAnim(s);
            check.accept(root.getChild("right_arm").yScale==1,"work pose resets on idle: "+action);
        }
    }
}
