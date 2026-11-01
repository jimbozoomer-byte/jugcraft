package local.peepo.client;

import java.util.function.BiConsumer;
import net.minecraft.world.phys.Vec3;

/** Fixed-length rig and continuous staff transitions, sampled independently of capture pacing. */
public final class StaffPostChecks {
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean jughead:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true})for(int[] change:new int[][]{{0,1},{1,2},{2,1},{1,3},{2,3}}){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();s.jughead=jughead;s.pumpkin=pumpkin;
            Vec3 previous=null;float lastSit=change[0]==2?1:0;
            var from=new Vec3(.1,.44,.8);
            for(int frame=0;frame<=80;frame++){
                float age=frame*.25F;s.ageInTicks=150+age;s.sitting=change[1]==2;s.staff.sample(change[1],change[0],age,from);model.setupAnim(s);
                check.accept(Double.isFinite(s.staff.offset.lengthSqr()) && Float.isFinite(s.staff.sit),"staff transition has finite position and pose");
                if(previous!=null)check.accept(previous.distanceTo(s.staff.offset)<.08 && Math.abs(s.staff.sit-lastSit)<.04,"staff mount and sit/stand transitions are continuous");
                previous=s.staff.offset;lastSit=s.staff.sit;
                for(String name:new String[]{"left_arm","right_arm","left_leg","right_leg"}){
                    var part=root.getChild(name);check.accept(part.xScale==1 && part.yScale==1 && part.zScale==1,"staff poses keep native limb lengths");
                }
                check.accept(root.getChild("left_leg").y==22.5F && root.getChild("right_leg").y==22.5F,"staff legs remain attached to hips");
            }
            check.accept(s.staff.offset.lengthSqr()<1e-8,"staff transition joins the authoritative position at its endpoint");
            check.accept(change[1]==3?!s.staff.active:s.staff.active && s.staff.sit==(change[1]==2?1:0),"staff transition settles into its intended pose");
        }
        var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();s.staff.sample(1,1,80,Vec3.ZERO);s.ageInTicks=200;model.setupAnim(s);
        check.accept(root.getChild("left_leg").xRot==0 && root.getChild("right_leg").xRot==0,"standing feet remain planted during idle weight shifts");
        s.staff.sample(2,2,80,Vec3.ZERO);s.sitting=true;model.setupAnim(s);
        check.accept(Math.abs(root.getChild("left_leg").xRot-root.getChild("right_leg").xRot)>.05,"seated staff kicks both little legs");
    }
}
