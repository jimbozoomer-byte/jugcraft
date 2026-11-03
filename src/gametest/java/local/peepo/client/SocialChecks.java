package local.peepo.client;

import java.util.function.BiConsumer;
import local.peepo.CompanionSocial;
import local.peepo.WorkAnimation;

public final class SocialChecks {
    public static void held(BiConsumer<Boolean,String> check,PeepoState s){
        var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);
        check.accept(!s.held.isEmpty(),"native renderer resolved the held item");
        for(int kind:new int[]{CompanionSocial.LAUGH_FIRST,CompanionSocial.LAUGH_SECOND,CompanionSocial.FAREWELL_WAVE,CompanionSocial.FAREWELL_NOD}){
            s.social=kind;s.socialTime=20;model.setupAnim(s);var arm=root.getChild("right_arm");
            check.accept(Math.abs(arm.xRot-(s.holdingLight?-2.55F:-.65F))<.12F,"social gesture retains equipped right-hand pose");
            check.accept(arm.yRot==0 && Math.abs(arm.zRot-(s.holdingLight?.15F:.06F))<.026F,"held item remains upright without a right-arm wave");
        }
    }
    public static void run(BiConsumer<Boolean,String> check){
        for(boolean pink:new boolean[]{false,true})for(boolean pumpkin:new boolean[]{false,true})for(int kind=CompanionSocial.HIGH_FIVE_LEFT;kind<=CompanionSocial.FAREWELL_NOD;kind++){
            var root=PeepoGeometry.create().bakeRoot();var model=new PeepoModel(root);var s=new PeepoState();s.jughead=pink;s.pumpkin=pumpkin;s.social=kind;
            float width=pumpkin?3.69F:3.21F;s.socialTarget.set(kind==CompanionSocial.HIGH_FIVE_RIGHT?-width:width,17,pumpkin?-4.8F:-4);
            boolean highFive=kind==CompanionSocial.HIGH_FIVE_LEFT || kind==CompanionSocial.HIGH_FIVE_RIGHT;
            int duration=highFive?40:kind<=CompanionSocial.LAUGH_SECOND?52:24;
            org.joml.Vector3f previous=null;
            for(int frame=0;frame<=duration*2;frame++){
                s.socialTime=frame*.5F;s.ageInTicks=200+s.socialTime;model.setupAnim(s);
                for(String bone:new String[]{"left_arm","right_arm","left_leg","right_leg"}){
                    var p=root.getChild(bone);check.accept(p.xScale==1 && p.yScale==1 && p.zScale==1,"social clips retain native limb lengths");
                    check.accept(Float.isFinite(p.xRot+p.yRot+p.zRot+p.x+p.y+p.z),"finite social pose");
                }
                check.accept(root.getChild("left_leg").y==22.5F && root.getChild("right_leg").y==22.5F,"social feet stay attached and planted");
                var hand=model.taskHandPosition(kind!=CompanionSocial.HIGH_FIVE_RIGHT);
                if(previous!=null)check.accept(hand.distance(previous)<.9F,"social hand path has no visible frame snap");previous=hand;
                if(highFive && s.socialTime>=18 && s.socialTime<=22)check.accept(hand.distance(s.socialTarget)<.001F,"both high-five palms meet their shared contact target");
            }
            check.accept(!root.getChild("head").getChild("sleeping_eyes").visible,"eyes reopen at social clip end");
            check.accept(Math.abs(root.getChild("left_arm").xRot)<.001F && Math.abs(root.getChild("right_arm").xRot)<.001F,"social arms settle back to idle");
            s.socialTime=20;s.work=WorkAnimation.INTERACT;check.accept(!SocialPose.active(s),"work suppresses stale social metadata");
            s.work=WorkAnimation.NONE;s.eating=true;check.accept(!SocialPose.active(s),"eating owns the pose");s.eating=false;
            s.rest.active=true;check.accept(!SocialPose.active(s),"rest transition owns the pose");s.rest.active=false;
            s.actionPose=true;check.accept(!SocialPose.active(s),"handling owns the pose");s.actionPose=false;
        }
        for(boolean aPumpkin:new boolean[]{false,true})for(boolean bPumpkin:new boolean[]{false,true}){
            float width=((aPumpkin?3.69F:3.21F)+(bPumpkin?3.69F:3.21F))*.5F;
            float distance=.5F+(aPumpkin?.05F:0)+(bPumpkin?.05F:0);
            var a=new PeepoState();a.pumpkin=aPumpkin;a.social=CompanionSocial.HIGH_FIVE_LEFT;a.socialTime=20;a.socialTarget.set(width,17,-distance*8);
            var b=new PeepoState();b.pumpkin=bPumpkin;b.social=CompanionSocial.HIGH_FIVE_RIGHT;b.socialTime=20;b.socialTarget.set(-width,17,-distance*8);
            var am=new PeepoModel(PeepoGeometry.create().bakeRoot());var bm=new PeepoModel(PeepoGeometry.create().bakeRoot());am.setupAnim(a);bm.setupAnim(b);
            var ah=am.taskHandPosition(true);var bh=bm.taskHandPosition(false);
            check.accept(Math.abs(ah.x+bh.x)<.001F && Math.abs(ah.y-bh.y)<.001F && Math.abs(-ah.z-bh.z-distance*16)<.001F,"opposing Peepo/Jughead costume palms coincide in world space");
        }
    }
}
