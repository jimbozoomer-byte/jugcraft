package local.peepo;

import java.util.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.*;

/** Cosmetic, stationary social encounters. A pair holds only mutual runtime references, never world tickets. */
public final class CompanionSocial {
    public static final int NONE=0,WAVE=1,TALK=2,LISTEN=3,HIGH_FIVE_LEFT=4,HIGH_FIVE_RIGHT=5,
        LAUGH_FIRST=6,LAUGH_SECOND=7,FAREWELL_WAVE=8,FAREWELL_NOD=9,DANCE=10;
    enum Ending { NONE(0),HIGH_FIVE(40),LAUGHTER(52),FAREWELL(24);
        final int ticks;Ending(int ticks){this.ticks=ticks;}
    }
    private static final int CHAT_TICKS=120,TURN_TICKS=30,GREETING_COOLDOWN=1200,CHAT_COOLDOWN=1800;
    private final PeepoEntity npc;
    private PeepoEntity partner;
    private Player greeted;
    private boolean ownerNearby,firstSpeaker,goalActive;
    private long nextScan,nextGreeting,nextConversation,until,started;
    private int spokenTurn=-1;
    private Ending ending=Ending.NONE;
    private long endingStarted;
    private boolean endingSound;
    private net.minecraft.world.phys.Vec3 contactOrigin;
    private CompanionMusic.Source music;
    private long danceJoined;
    // Client spawn packets assign the numeric ID after constructing the entity. UUID already exists here.
    CompanionSocial(PeepoEntity npc){this.npc=npc;nextScan=npc.level().getGameTime()+20+Math.floorMod(npc.getUUID().hashCode(),80);}
    public CompanionStatus activity(){return music!=null?CompanionStatus.DANCING:partner!=null && goalActive?CompanionStatus.CHATTING:greeted!=null?CompanionStatus.GREETING:CompanionStatus.IDLE;}
    private boolean idle(){return npc.preferences.social && npc.socialIdle() && npc.getNavigation().isDone()
        && npc.getDeltaMovement().horizontalDistanceSqr()<.0004 && !npc.isInWater() && !npc.isPassenger() && npc.onGround();}
    private boolean mayConverse(){return idle() && npc.orders.mode()!=CompanionOrders.Mode.FOLLOW.ordinal() && npc.orders.mode()!=CompanionOrders.Mode.STAY.ordinal();}
    private boolean mayDance(){return idle() && npc.orders.allowsIdleDance();}
    private boolean visible(LivingEntity other,double distance){return other!=null && other.isAlive() && !other.isRemoved() && other.level()==npc.level()
        && npc.distanceToSqr(other)<=distance*distance && npc.hasLineOfSight(other);}
    private void face(LivingEntity other){
        float yaw=(float)Math.toDegrees(Math.atan2(-(other.getX()-npc.getX()),other.getZ()-npc.getZ()));
        npc.setYRot(net.minecraft.util.Mth.rotLerp(.25F,npc.getYRot(),yaw));npc.yBodyRot=npc.getYRot();
        npc.getLookControl().setLookAt(other,25,20);
    }
    /** Cancel both halves immediately on a command, menu, unload or higher-priority goal. */
    void cancel(){
        var old=partner;clear();
        if(old!=null && old.social.partner==npc)old.social.clear();
    }
    private void clear(){partner=null;greeted=null;music=null;goalActive=false;ending=Ending.NONE;contactOrigin=null;npc.setSocialPose(NONE);}
    private void dance(long now){
        if(!mayDance() || now-danceJoined>10 && !goalActive && !npc.orders.holdsDancePosition()){cancel();return;}
        if(npc.danceEnd()<0 && (!music.live((net.minecraft.server.level.ServerLevel)npc.level()) || !music.inRange(npc.position())))npc.endDance();
        if(npc.danceEnd()>=0 && now-npc.danceEnd()>=10){cancel();nextScan=now+20;return;}
        double x=music.pos.getX()+.5-npc.getX(),z=music.pos.getZ()+.5-npc.getZ();
        if(x*x+z*z>.01){
            float yaw=(float)Math.toDegrees(Math.atan2(-x,z));
            npc.setYRot(net.minecraft.util.Mth.rotLerp(.20F,npc.getYRot(),yaw));npc.yBodyRot=npc.getYRot();
        }
    }
    /** The short native arms can meet only at close, level standing positions. No approach path. */
    static boolean canHighFive(PeepoEntity a,PeepoEntity b){
        int pumpkins=(a.isWearingPumpkin()?1:0)+(b.isWearingPumpkin()?1:0);
        double min=.46+pumpkins*.05,max=.54+pumpkins*.05,d=a.distanceToSqr(b);
        return a.getMainHandItem().isEmpty() && b.getMainHandItem().isEmpty()
            && a.getOffhandItem().isEmpty() && b.getOffhandItem().isEmpty()
            && Math.abs(a.getY()-b.getY())<=.015 && d>=min*min && d<=max*max;
    }
    private void beginEnding(long now){
        Ending chosen=switch(npc.getRandom().nextInt(3)){case 0->Ending.HIGH_FIVE;case 1->Ending.LAUGHTER;default->Ending.FAREWELL;};
        if(chosen==Ending.HIGH_FIVE && !canHighFive(npc,partner))chosen=Ending.FAREWELL;
        ending=partner.social.ending=chosen;endingStarted=partner.social.endingStarted=now;
        until=partner.social.until=now+chosen.ticks;endingSound=partner.social.endingSound=false;
        contactOrigin=npc.position();partner.social.contactOrigin=partner.position();
        float width=((npc.isWearingPumpkin()?3.69F:3.21F)+(partner.isWearingPumpkin()?3.69F:3.21F))*.5F;
        float depth=(float)npc.position().subtract(partner.position()).horizontalDistance()*8;
        float height=(float)(partner.getY()-npc.getY())*8;
        npc.setSocialTarget(new org.joml.Vector3f(width,17-height,-depth));
        partner.setSocialTarget(new org.joml.Vector3f(-width,17+height,-depth));
        npc.setSocialPose(endingPose(chosen,true),now);partner.setSocialPose(endingPose(chosen,false),now);
    }
    private static int endingPose(Ending ending,boolean lead){return switch(ending){
        case HIGH_FIVE->lead?HIGH_FIVE_LEFT:HIGH_FIVE_RIGHT;
        case LAUGHTER->lead?LAUGH_FIRST:LAUGH_SECOND;
        case FAREWELL->lead?FAREWELL_WAVE:FAREWELL_NOD;
        default->NONE;
    };}
    public void tick(){
        long now=npc.level().getGameTime();
        if(music!=null){dance(now);return;}
        if(partner!=null){
            if(now>=until || partner.social.partner!=npc || !mayConverse() || !partner.social.mayConverse()
                || !visible(partner,3.5) || now-started>10 && (!goalActive || !partner.social.goalActive)){cancel();return;}
            if(goalActive && partner.social.goalActive){
                if(firstSpeaker && ending==Ending.NONE && now-started>=CHAT_TICKS)beginEnding(now);
                if(ending!=Ending.NONE){
                    if(ending==Ending.HIGH_FIVE && (!canHighFive(npc,partner)
                        || npc.position().distanceToSqr(contactOrigin)>.0004
                        || partner.position().distanceToSqr(partner.social.contactOrigin)>.0004)){cancel();return;}
                    face(partner);
                    if(!endingSound && ending==Ending.LAUGHTER && now-endingStarted>=(firstSpeaker?8:0)){
                        endingSound=true;npc.playSound(SoundEvents.FROG_AMBIENT,.13F,npc.isJughead()?1.7F:1.5F);
                    }else if(!endingSound && ending==Ending.HIGH_FIVE && firstSpeaker && now-endingStarted>=18){
                        endingSound=true;npc.playSound(SoundEvents.FROG_STEP,.16F,1.5F);
                    }
                    return;
                }
                int turn=(int)((now-started)/TURN_TICKS);boolean speaking=(turn%2==0)==firstSpeaker;
                npc.setSocialPose(speaking?TALK:LISTEN);face(partner);
                if(speaking && spokenTurn!=turn){spokenTurn=turn;npc.playSound(SoundEvents.FROG_AMBIENT,.16F,npc.isJughead()?1.55F:1.35F);}
            }
            return;
        }
        if(greeted!=null){
            if(now>=until || !idle() || !visible(greeted,8) || greeted.isSpectator() || !npc.orders.owner(greeted)){cancel();return;}
            face(greeted);return;
        }
        if(now<nextScan || !idle())return;
        if(!CompanionBudget.social(npc))return;
        nextScan=now+80+Math.floorMod(npc.getId(),20);
        var owner=npc.orders.ownerPlayer();
        boolean near=owner!=null && !owner.isSpectator() && visible(owner,ownerNearby?8:6);
        boolean arriving=near && !ownerNearby;ownerNearby=near;
        if(arriving && now>=nextGreeting){
            greeted=owner;until=now+40;nextGreeting=now+GREETING_COOLDOWN;npc.setSocialPose(WAVE);face(owner);return;
        }
        // Music and conversations share this admission; a dance never performs another discovery scan.
        if(mayDance() && npc.level() instanceof net.minecraft.server.level.ServerLevel level){
            music=CompanionMusic.nearest(level,npc.position());
            if(music!=null){danceJoined=now;npc.beginDance(music.started);dance(now);return;}
        }
        if(!mayConverse() || now<nextConversation)return;
        var nearby=new ArrayList<PeepoEntity>(16);
        // Cap results before filtering eligibility, including crowded pens. Only already-loaded local entities.
        npc.level().getEntities(EntityTypeTest.forClass(PeepoEntity.class),npc.getBoundingBox().inflate(3),p->true,nearby,16);
        if(nearby.stream().anyMatch(p->p.social.partner!=null))return;
        for(var other:nearby){
            if(other==npc || other.social.greeted!=null || now<other.social.nextConversation || !other.social.mayConverse()
                || other.social.music!=null || npc.distanceToSqr(other)<.1936 || !visible(other,3))continue;
            partner=other;other.social.partner=npc;firstSpeaker=true;other.social.firstSpeaker=false;
            started=other.social.started=now;until=other.social.until=now+CHAT_TICKS+52;
            ending=other.social.ending=Ending.NONE;
            npc.setSocialPose(LISTEN,now);other.setSocialPose(LISTEN,now);
            spokenTurn=other.social.spokenTurn=-1;
            nextConversation=now+CHAT_COOLDOWN+Math.floorMod(npc.getId(),600);
            other.social.nextConversation=now+CHAT_COOLDOWN+Math.floorMod(other.getId(),600);
            return;
        }
    }
    public void save(ValueOutput out){long now=npc.level().getGameTime();out.putInt("SocialGreetingWait",(int)Math.clamp(nextGreeting-now,0,GREETING_COOLDOWN));out.putInt("SocialChatWait",(int)Math.clamp(nextConversation-now,0,CHAT_COOLDOWN+600));}
    public void load(ValueInput in){
        cancel();long now=npc.level().getGameTime();ownerNearby=false;
        nextGreeting=now+Math.clamp(in.getIntOr("SocialGreetingWait",0),0,GREETING_COOLDOWN);
        nextConversation=now+Math.clamp(in.getIntOr("SocialChatWait",0),0,CHAT_COOLDOWN+600);
        nextScan=now+20+Math.floorMod(npc.getId(),80);
    }
    /** Below work/rest/transport/commands, above wandering and ordinary looking. No paths are created. */
    static final class ConversationGoal extends Goal {
        private final PeepoEntity npc;
        ConversationGoal(PeepoEntity npc){this.npc=npc;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean requiresUpdateEveryTick(){return true;}
        public boolean canUse(){return npc.social.partner!=null && npc.social.mayConverse()
            || npc.social.music!=null && !npc.orders.holdsDancePosition() && npc.social.mayDance();}
        public boolean canContinueToUse(){return canUse();}
        public void start(){npc.social.goalActive=true;npc.getNavigation().stop();}
        public void tick(){npc.getNavigation().stop();}
        public void stop(){npc.social.cancel();}
    }
}
