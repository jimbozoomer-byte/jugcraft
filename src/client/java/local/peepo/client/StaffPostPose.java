package local.peepo.client;

import local.peepo.PeepoEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Presentation of the server's one reserved post; no client movement or furniture search. */
public final class StaffPostPose {
    public boolean active,mounting,leaving;
    public float sit,transition,age,yaw,weight,greetingTime=-1;
    public Vec3 offset=Vec3.ZERO;

    /** Shared deterministic sampler for native rendering and focused rig checks. */
    public void sample(int pose,int previous,float elapsed,Vec3 fromOffset){
        age=Float.isFinite(elapsed)?Math.max(0,elapsed):20;
        active=pose>=1 && pose<=3 && (pose!=3 || age<20);
        offset=Vec3.ZERO;sit=transition=0;weight=1;mounting=leaving=false;
        if(!active)return;
        transition=Mth.clamp(age/20F,0,1);
        float eased=RestPose.ease(transition);
        sit=Mth.lerp(eased,previous==2?1F:0F,pose==2?1F:0F);
        mounting=previous==0;leaving=pose==3;
        offset=fromOffset.scale(1-eased);
        if(mounting || leaving){
            // Lift before crossing the block's front edge; leave the deck before dropping.
            // This also works for a full-height block without passing through its face.
            float horizontal=RestPose.ease(mounting?(transition-.25F)/.75F:transition/.75F);
            float vertical=RestPose.ease(mounting?transition/.5F:(transition-.5F)/.5F);
            offset=new Vec3(fromOffset.x*(1-horizontal),fromOffset.y*(1-vertical)+Math.sin(transition*Math.PI)*.08,fromOffset.z*(1-horizontal));
        }
        if(leaving)weight=1-RestPose.ease((transition-.75F)/.25F);
    }

    static void extract(PeepoEntity npc,PeepoState s,float partial){
        var motion=npc.staffPost;
        int pose=npc.isAlive() && !npc.isEating() && !npc.isPassenger()?motion.pose():0;
        s.staff.sample(pose,motion.previousPose(),npc.level().getGameTime()-motion.started()+partial,motion.offset());
        s.staff.yaw=npc.getYRot();
        s.staff.greetingTime=-1;
        if(!s.staff.active)return;
        s.staff.offset=s.staff.offset.add(motion.target().subtract(npc.getPosition(partial)));
        if(motion.greetingStarted()>=0)s.staff.greetingTime=npc.level().getGameTime()-motion.greetingStarted()+partial;
        s.bodyRot=s.staff.yaw;s.yRot=s.xRot=0;
    }

    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart leftLeg,ModelPart rightLeg,ModelPart[] torso){
        var p=s.staff;
        if(!p.active || s.eating || s.sleeping || s.rest.active)return;
        float weight=p.weight;
        float settled=RestPose.ease((p.age-14)/16F);
        float kick=Mth.sin(s.ageInTicks*.16F)*.30F*p.sit;
        float step=(p.mounting || p.leaving)?Mth.sin(p.transition*Mth.PI)*.20F:0;
        leftLeg.xRot=Mth.lerp(weight,leftLeg.xRot,-1.05F*p.sit+kick+step);
        rightLeg.xRot=Mth.lerp(weight,rightLeg.xRot,-1.05F*p.sit-kick-step);
        leftLeg.yRot=rightLeg.yRot=leftLeg.zRot=rightLeg.zRot=0;
        float arm=-.70F*p.sit-.25F*step;
        left.xRot=Mth.lerp(weight,left.xRot,arm);
        left.yRot=Mth.lerp(weight,left.yRot,.35F*p.sit);
        left.zRot=Mth.lerp(weight,left.zRot,-.06F*(1-p.sit)-.18F*step);
        if(s.held.isEmpty()){
            right.xRot=Mth.lerp(weight,right.xRot,arm);
            right.yRot=Mth.lerp(weight,right.yRot,-.35F*p.sit);
            right.zRot=Mth.lerp(weight,right.zRot,.06F*(1-p.sit)+.18F*step);
        }
        // Pivot about the hips; standing feet never slide or change length.
        float roll=Mth.sin(s.ageInTicks*.035F)*.035F*(1-p.sit)*settled*weight;
        float pitch=(.035F*p.sit+.35F*step)*weight;
        for(var part:torso)CompanionWorkPose.lean(part,pitch,roll,0,0);
        CompanionWorkPose.lean(left,pitch,roll,0,0);CompanionWorkPose.lean(right,pitch,roll,0,0);
        head.yRot+=Mth.sin(s.ageInTicks*.025F)*.10F*settled*weight;
        head.xRot+=Mth.sin(s.ageInTicks*.04F)*.025F*settled*weight;
        if(p.greetingTime>=0 && p.greetingTime<40 && !p.leaving){
            float t=p.greetingTime;
            float hello=RestPose.ease(t/6F)*(1-RestPose.ease((t-32)/8F))*settled*weight;
            left.xRot=Mth.lerp(hello,left.xRot,-.35F);left.yRot=Mth.lerp(hello,left.yRot,.2F);
            left.zRot=Mth.lerp(hello,left.zRot,-2.05F+Mth.sin(t*.55F)*.28F);
            head.xRot+=Mth.sin(t*.18F)*.055F*hello;
        }
    }
}
