package local.peepo.client;

import local.peepo.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;

/** Cosmetic item handling driven by existing work and committed transfer events. */
final class TaskDetailPose {
    static void extract(PeepoEntity npc,PeepoState s,float partial,ItemModelResolver items){
        s.detail=CompanionGesture.NONE;s.toolHandling=s.fertilizing=false;s.detailTime=0;
        var primary=ItemStack.EMPTY;var secondary=ItemStack.EMPTY;
        // Settings editing cancels the server gesture/work before metadata is sent.
        boolean available=npc.isAlive() && !npc.isPassenger() && !s.eating && !s.sleeping && !s.sitting && !s.wheelRunning && s.social==0;
        if(available){
            var gesture=npc.gesture();
            if(gesture.tool() && !s.actionPose && (gesture==CompanionGesture.TOOL_DRAW
                ?!npc.gestureItem().isEmpty() && npc.getMainHandItem().is(npc.gestureItem().getItem())
                :s.work==WorkAnimation.NONE && npc.getMainHandItem().isEmpty())){
                s.detail=gesture;s.toolHandling=true;primary=npc.gestureItem();
                s.detailTime=Mth.clamp((npc.level().getGameTime()-npc.gestureStarted()+partial)/gesture.duration(),0,1);
                s.detailScale=primary.is(net.minecraft.tags.ItemTags.HOES)?MachineWorkClip.HOE_SCALE
                    :primary.is(io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.KNIVES)?MachineWorkClip.KNIFE_SCALE
                    :primary.is(Items.FISHING_ROD)?FishingPose.ROD_SCALE:primary.is(Items.SHEARS)?.45F:.35F;
            }else if(gesture.brewing() && s.work==WorkAnimation.NONE){
                s.detail=gesture;s.detailTime=Mth.clamp((npc.level().getGameTime()-npc.gestureStarted()+partial)/gesture.duration(),0,1);
                primary=gesture==CompanionGesture.BREW_ADD?npc.gestureItem():ItemStack.EMPTY;
                secondary=gesture==CompanionGesture.BREW_ADD?npc.gestureAux():npc.gestureItem();
            }else if(s.work==WorkAnimation.FERTILIZE){
                s.fertilizing=true;s.detailTime=Mth.clamp((npc.level().getGameTime()-npc.workStarted()+partial)/CompanionGarden.WORK_TICKS,0,1);
                primary=npc.workPie();secondary=new ItemStack(Items.BUNDLE);
            }
        }
        items.updateForLiving(s.detailPrimary,primary,ItemDisplayContext.NONE,npc);
        items.updateForLiving(s.detailSecondary,secondary,ItemDisplayContext.NONE,npc);
    }
    static float smooth(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
    static float envelope(float t){return smooth(t/.16F)*(1-smooth((t-.84F)/.16F));}
    static float scatter(float t){return Mth.sin(Mth.clamp((t-.28F)/.48F,0,1)*Mth.PI*4);}
    static float brewApproach(float t){return smooth((t-.18F)/.34F);}
    static float brewDrop(float t){return smooth((t-.56F)/.10F);}
    static float brewSwirl(float t){return smooth((t-.68F)/.16F);}
    static org.joml.Vector3f brewBottle(org.joml.Vector3f left,org.joml.Vector3f right,float swirl){
        return new org.joml.Vector3f(left).add(-.85F,.35F,-.25F)
            .lerp(new org.joml.Vector3f(left).add(right).mul(.5F).add(0,.35F,-.25F),swirl);
    }
    static org.joml.Vector3f brewIngredient(org.joml.Vector3f right,org.joml.Vector3f bottle,float t){
        return new org.joml.Vector3f(right).lerp(new org.joml.Vector3f(bottle).add(0,-1.2F,0),brewDrop(t));
    }
    static float toolBlend(PeepoState s){
        boolean nativeGrip=s.work==WorkAnimation.HARVEST || s.work==WorkAnimation.CHOP || s.work==WorkAnimation.SHEAR || s.work==WorkAnimation.HIVE_SHEAR || s.work.isFishing();
        return nativeGrip?smooth((s.detailTime-.5F)/.5F):0;
    }
    static org.joml.Vector3f toolPosition(PeepoState s,org.joml.Vector3f hand){return hand.lerp(new org.joml.Vector3f(s.toolPose.x,s.toolPose.y,s.toolPose.z),toolBlend(s));}
    static void apply(PeepoState s,ModelPart head,ModelPart left,ModelPart right,ModelPart[] torso){
        float t=s.detailTime;
        if(s.toolHandling){
            if(s.work==WorkAnimation.NONE)right.xRot=-.65F;
            float draw=s.detail==CompanionGesture.TOOL_DRAW?t:1-t;
            float pocket=1-smooth((draw-.35F)/.65F);
            // The working pose continues underneath; neither feet nor a workstation perch move.
            blend(right,pocket,.28F,-.35F,.24F,s.pumpkin?-1F:0);
            blend(left,pocket,-.12F,.1F,-.10F,s.pumpkin?-1F:0);
            head.xRot=Mth.lerp(pocket,head.xRot,.20F);head.yRot=Mth.lerp(pocket,head.yRot,-.15F);
            return;
        }
        if(!s.fertilizing && !s.detail.brewing())return;
        float a=envelope(t),swirl=s.detail==CompanionGesture.BREW_ADD?brewSwirl(t):1;
        float wave=Mth.sin((t-.45F)*Mth.PI*7)*swirl*a;
        float bend=s.fertilizing?.24F*a:.05F*a;
        for(var part:torso)CompanionWorkPose.lean(part,bend,0,0,0);
        head.xRot+=a*(s.fertilizing?.14F:.17F);head.yRot=0;
        float z=s.pumpkin?-2.65F:-1.65F;
        if(s.fertilizing){
            float toss=scatter(t),reach=Mth.clamp((t-.2F)/.15F,0,1)*(1-smooth((t-.76F)/.12F));
            blend(left,a,-1.18F, .80F,-.06F,z);
            blend(right,a,-1.22F+.32F*toss,-.85F+.75F*reach,.05F+.16F*toss,z-.25F*reach);
        }else{
            float adding=s.detail==CompanionGesture.BREW_ADD?1-swirl:0;
            float approach=brewApproach(t);
            // Bring the ingredient forward, then over the bottle before it leaves the fingers.
            // Moving the shoulders forward keeps the normal-length arms clear of the wide face.
            blend(left,a,-1.40F+.10F*wave,1.10F-.14F*swirl,-.04F+.08F*wave,z-1.10F);
            blend(right,a,-1.40F+adding*Mth.lerp(approach,-.15F,-.40F)+.10F*wave,
                -Mth.lerp(adding,.96F,Mth.lerp(approach,.65F,1.25F)),.04F+.08F*wave,z-1.35F);
        }
        // Shoulder follows the same modest hip-centered lean; arms remain their natural length.
        for(var arm:new ModelPart[]{left,right}){
            float y=arm.y-22.5F,oldZ=arm.z;
            arm.y=22.5F+y*Mth.cos(bend)-oldZ*Mth.sin(bend);
            arm.z=y*Mth.sin(bend)+oldZ*Mth.cos(bend);
        }
    }
    private static void blend(ModelPart arm,float amount,float pitch,float yaw,float roll,float z){
        arm.xRot=Mth.lerp(amount,arm.xRot,pitch);arm.yRot=Mth.lerp(amount,arm.yRot,yaw);arm.zRot=Mth.lerp(amount,arm.zRot,roll);
        arm.z=Mth.lerp(amount,arm.z,z);arm.yScale=Mth.lerp(amount,arm.yScale,1);
    }
}
