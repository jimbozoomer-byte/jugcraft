package local.peepo.client;

import local.peepo.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Furniture-relative phases shared by the rig, world offset and blanket. */
public final class RestPose {
    public boolean active,climbing,eyesClosed;
    public float lie,sit,pull,grip,breathe,phase,yaw,blend;
    public int color=-1;
    public Vec3 offset=Vec3.ZERO;
    public CompanionRestMotion.Clip clip=CompanionRestMotion.Clip.NONE;
    public static float ease(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    public void sample(CompanionRestMotion.Clip kind,float age,int climb){
        clip=kind;active=kind!=CompanionRestMotion.Clip.NONE;climbing=eyesClosed=false;lie=sit=pull=grip=breathe=phase=0;blend=1;
        if(!active)return;
        float t=Mth.clamp((age-(kind==CompanionRestMotion.Clip.SLEEP?climb:0))/kind.ticks(),0,1);
        if(kind==CompanionRestMotion.Clip.SIT){sit=ease(t);phase=t;}
        else if(kind==CompanionRestMotion.Clip.STAND){sit=1-ease(t);phase=t;blend=1-ease((t-.65F)/.35F);}
        else {
            boolean wake=kind==CompanionRestMotion.Clip.WAKE;
            climbing=climb>0 && (wake?age>=kind.ticks():age<climb);
            if(climbing){phase=Mth.clamp(wake?(age-kind.ticks())/climb:age/climb,0,1);blend=ease(phase/.12F)*(1-ease((phase-.85F)/.15F));return;}
            phase=t;
            float settle=wake?1-t:t;
            lie=ease((settle-.25F)/.40F);
            sit=ease(settle/.25F)*(1-lie);
            pull=ease((settle-.60F)/.32F);
            grip=ease((settle-.48F)/.12F)*(1-ease((settle-.90F)/.10F));
            eyesClosed=wake?t<.12F:t>.88F;
            if(!wake && t>=1)breathe=Mth.sin(age*.07F)*.035F;
            if(wake && climb==0)blend=1-ease((t-.80F)/.20F);
        }
    }
    public static Vec3 position(CompanionRestMotion.Clip clip,float age,int climb,Vec3 entry,Vec3 mount){
        float t=Mth.clamp((age-(clip==CompanionRestMotion.Clip.SLEEP?climb:0))/clip.ticks(),0,1);
        if(clip==CompanionRestMotion.Clip.SIT)return entry.lerp(mount,ease(t)).add(0,Math.sin(t*Math.PI)*.10,0);
        if(clip==CompanionRestMotion.Clip.STAND)return mount.lerp(entry,ease(t)).add(0,Math.sin(t*Math.PI)*.08,0);
        var top=climb==0?entry:new Vec3(entry.x,mount.y,entry.z);
        if(clip==CompanionRestMotion.Clip.SLEEP){
            if(climb>0 && age<climb)return entry.lerp(top,ease(age/climb));
            return top.lerp(mount,ease(t/.25F)).add(0,Math.sin(Mth.clamp(t/.25F,0,1)*Math.PI)*.10,0);
        }
        if(clip==CompanionRestMotion.Clip.WAKE){
            if(climb>0 && age>=clip.ticks())return top.lerp(entry,ease((age-clip.ticks())/climb));
            float off=ease((t-.75F)/.25F);return mount.lerp(top,off).add(0,Math.sin(off*Math.PI)*.08,0);
        }
        return mount;
    }
    static void extract(PeepoEntity npc,PeepoState s,float partial){
        var motion=npc.restMotion;var r=s.rest;var kind=motion.clip();
        if(!npc.isAlive() || npc.isEating() || npc.isPassenger())kind=CompanionRestMotion.Clip.NONE;
        float age=npc.level().getGameTime()-motion.started()+partial;
        if(kind.exit() && age>=motion.duration())kind=CompanionRestMotion.Clip.NONE;
        r.sample(kind,age,motion.climbTicks());r.color=motion.color();r.offset=Vec3.ZERO;
        if(!r.active)return;
        var at=position(kind,age,motion.climbTicks(),motion.entry(),motion.mount());
        r.offset=at.subtract(npc.getPosition(partial));
        // Climb facing the rungs; turn outward only while scooting onto the mattress.
        float turn=r.climbing?(kind.exit()?1-ease((r.phase-.8F)/.2F):1):kind==CompanionRestMotion.Clip.SLEEP && motion.climbTicks()>0?1-ease(r.phase/.25F):kind==CompanionRestMotion.Clip.WAKE && motion.climbTicks()>0?ease((r.phase-.75F)/.25F):0;
        r.yaw=motion.yaw()+180*turn;
        s.sleeping=kind.bed();s.sitting=!kind.bed();
    }
    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart leftLeg,ModelPart rightLeg,ModelPart[] torso){
        var r=s.rest;if(!r.active)return;
        float age=s.ageInTicks,weight=r.blend;
        if(r.climbing){
            float swing=Mth.sin(age*.65F);
            left.xRot=Mth.lerp(weight,left.xRot,-2.25F+swing*.40F);right.xRot=Mth.lerp(weight,right.xRot,-2.25F-swing*.40F);
            left.yRot=.24F*weight;right.yRot=-.24F*weight;left.zRot=right.zRot=0;
            leftLeg.xRot=Mth.lerp(weight,leftLeg.xRot,-.30F+swing*.45F);rightLeg.xRot=Mth.lerp(weight,rightLeg.xRot,-.30F-swing*.45F);head.xRot=-.12F*weight;
            return;
        }
        float kick=Mth.sin(age*.16F)*.30F*r.sit;
        leftLeg.xRot=Mth.lerp(weight,leftLeg.xRot,-1.05F*r.sit+kick);rightLeg.xRot=Mth.lerp(weight,rightLeg.xRot,-1.05F*r.sit-kick);
        left.xRot=Mth.lerp(weight,left.xRot,-.70F*r.sit);right.xRot=Mth.lerp(weight,right.xRot,-.70F*r.sit);
        left.yRot=.35F*r.sit;right.yRot=-.35F*r.sit;left.zRot=-.08F*r.lie;right.zRot=.08F*r.lie;
        head.xRot=Mth.lerp(weight,head.xRot,.04F*r.sit);head.yRot*=1-weight;
        float lean=(r.clip==CompanionRestMotion.Clip.STAND?Mth.sin(r.phase*Mth.PI)*.22F:0)*weight;
        for(var part:torso)CompanionWorkPose.lean(part,lean,0,0,0);
        if(r.lie>.9F)for(var part:torso)part.z+=r.breathe;
        if(r.grip>0){blanketArm(s,left,true);blanketArm(s,right,false);}
    }
    static Vector3f blanketEdge(PeepoState s,PeepoModel model){
        var r=s.rest;
        var fixed=new Vector3f(0,Mth.lerp(r.pull,21.9F,18.95F),(s.pumpkin?-3.75F:-2.65F)+r.breathe);
        if(r.grip>0){var hands=model.taskHandPosition(true).add(model.taskHandPosition(false)).mul(.5F);fixed.lerp(hands,r.grip);}
        return fixed;
    }
    private static void blanketArm(PeepoState s,ModelPart arm,boolean left){
        var r=s.rest;float weight=r.grip;
        arm.z=Mth.lerp(weight,arm.z,s.pumpkin?-2.3F:-1.4F);
        float x=(left?1:-1)*1.6F-arm.x,y=Mth.lerp(r.pull,21.9F,18.95F)-arm.y,z=(s.pumpkin?-3.75F:-2.65F)-arm.z;
        float length=Mth.sqrt(x*x+y*y+z*z);
        arm.xRot=Mth.lerp(weight,arm.xRot,-(float)Math.acos(Mth.clamp(y/length,-1,1)));arm.yRot=Mth.lerp(weight,arm.yRot,(float)Math.atan2(-x,-z));arm.zRot*=1-weight;
        arm.xScale=arm.yScale=arm.zScale=1;
    }
}
