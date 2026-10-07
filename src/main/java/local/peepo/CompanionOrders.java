package local.peepo;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;

/** Persisted, dimension-scoped commands. All mutation and permission checks are server-side. */
public final class CompanionOrders {
    public enum Mode { FOLLOW, STAY, HOME, WORK }
    private final PeepoEntity npc;
    private UUID owner,follow;
    private GlobalPos home,work,stay;
    private Mode mode=Mode.WORK;
    private int radius=8;
    private boolean party,returningHome;
    CompanionOrders(PeepoEntity npc){this.npc=npc;}
    public boolean tamed(){return owner!=null;}
    public boolean owner(Player p){return p.getUUID().equals(owner);}
    public boolean allowed(Player p){return owner(p) || owner!=null && party && JugcraftParties.sameParty(owner,p.getUUID());}
    public boolean foodAccess(UUID sourceOwner,boolean shared){return owner!=null && (owner.equals(sourceOwner) || shared && JugcraftParties.sameParty(owner,sourceOwner));}
    public int mode(){return mode.ordinal();}
    public int radius(){return radius;}
    public boolean party(){return party;}
    private GlobalPos here(){return GlobalPos.of(npc.level().dimension(),npc.blockPosition());}
    public boolean local(GlobalPos pos){return pos!=null && pos.dimension().equals(npc.level().dimension());}
    public boolean homeHere(){return local(home);}
    public boolean workHere(){return local(work);}
    public void tame(Player p){
        if(tamed() || npc.level().isClientSide())return;
        owner=p.getUUID();follow=owner;home=work=stay=here();mode=Mode.HOME;apply();
    }
    public boolean command(Player p,int button){
        if(!allowed(p) || p.isSpectator() || button<0 || button>8)return false;
        if(button<=3){mode=Mode.values()[button];if(mode==Mode.FOLLOW)follow=p.getUUID();if(mode==Mode.STAY)stay=here();}
        else switch(button){case 4->home=here();case 5->work=here();case 6->radius=Math.max(4,radius-4);case 7->radius=Math.min(16,radius+4);case 8->{if(!owner(p))return false;party=!party;}default->{return false;}}
        apply();
        if(button==1){stay=here();npc.setHomeTo(stay.pos(),radius);}
        if(button==2)returningHome=true;
        return true;
    }
    private void apply(){
        npc.resetCompanionRoutine();npc.getNavigation().stop();npc.setRestMode(CompanionEnergy.Rest.NONE);npc.setNoGravity(false);npc.leaveCompanionBed();
        GlobalPos anchor=mode==Mode.HOME?home:mode==Mode.WORK?work:mode==Mode.STAY?stay:null;
        if(local(anchor))npc.setHomeTo(anchor.pos(),radius);else npc.clearHome();
    }
    private boolean in(GlobalPos center,Vec3 point){return local(center) && center.pos().distToCenterSqr(point)<=radius*radius;}
    public boolean station(CompanionStation s){
        if(!tamed())return true;
        if(mode!=Mode.HOME && mode!=Mode.WORK)return false;
        return s.kind()==CompanionStation.Kind.WHEEL ? mode==Mode.WORK && in(work,s.approachPosition()) : in(home,s.approachPosition());
    }
    public boolean food(Vec3 point){
        if(!tamed())return true;
        return switch(mode){case STAY->npc.position().distanceToSqr(point)<1;case HOME->in(home,point);case WORK->in(home,point)||in(work,point);case FOLLOW->{Player p=followPlayer();yield p!=null && p.position().distanceToSqr(point)<64;}};
    }
    private Player followPlayer(){
        if(follow==null)return null;
        Player p=npc.level().getPlayerByUUID(follow);
        return p!=null && p.isAlive() && !p.isSpectator() && allowed(p)?p:null;
    }
    private Vec3 target(){
        if(mode==Mode.FOLLOW){var p=followPlayer();return p==null?null:p.position();}
        GlobalPos pos=mode==Mode.STAY?stay:mode==Mode.HOME || npc.isRecovering()?home:work;
        return local(pos)?Vec3.atBottomCenterOf(pos.pos()):null;
    }
    public boolean targetAvailable(){Vec3 target=target();return target!=null && npc.level().hasChunkAt(BlockPos.containing(target));}
    public boolean routineAllowed(){return !tamed() || (mode==Mode.HOME || mode==Mode.WORK) && targetAvailable();}
    public void save(ValueOutput out){
        if(owner==null)return;
        out.putString("CompanionOwner",owner.toString());out.putString("CompanionFollow",(follow==null?owner:follow).toString());
        out.putInt("CompanionCommand",mode.ordinal());out.putInt("CompanionRadius",radius);out.putBoolean("CompanionParty",party);
        if(home!=null)out.store("CompanionHome",GlobalPos.CODEC,home);if(work!=null)out.store("CompanionWork",GlobalPos.CODEC,work);if(stay!=null)out.store("CompanionStay",GlobalPos.CODEC,stay);
    }
    public void load(ValueInput in){
        try{owner=UUID.fromString(in.getStringOr("CompanionOwner",""));}catch(IllegalArgumentException e){owner=null;}
        if(owner==null)return;
        try{follow=UUID.fromString(in.getStringOr("CompanionFollow",owner.toString()));}catch(IllegalArgumentException e){follow=owner;}
        mode=Mode.values()[Math.clamp(in.getIntOr("CompanionCommand",2),0,3)];radius=Math.clamp(in.getIntOr("CompanionRadius",8),4,16);party=in.getBooleanOr("CompanionParty",false);
        home=in.read("CompanionHome",GlobalPos.CODEC).orElse(null);work=in.read("CompanionWork",GlobalPos.CODEC).orElse(null);stay=in.read("CompanionStay",GlobalPos.CODEC).orElse(null);
    }
    public static final class CommandGoal extends Goal {
        private final PeepoEntity npc;private int repath;
        public CommandGoal(PeepoEntity npc){this.npc=npc;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        public boolean requiresUpdateEveryTick(){return true;}
        public boolean canUse(){
            var o=npc.orders;if(!o.tamed() || npc.isEating())return false;
            Vec3 target=o.target();
            if(o.returningHome && o.mode==Mode.HOME && target!=null){if(npc.position().distanceToSqr(target)>4)return true;o.returningHome=false;}
            return o.mode==Mode.STAY || o.mode==Mode.FOLLOW || target==null || npc.position().distanceToSqr(target)>o.radius*o.radius;
        }
        public boolean canContinueToUse(){
            var o=npc.orders;if(npc.isEating())return false;
            if(o.mode==Mode.FOLLOW || o.mode==Mode.STAY)return canUse();
            var target=o.target();return target==null || npc.position().distanceToSqr(target)>4;
        }
        public void start(){npc.resetCompanionRoutine();repath=0;}
        public void stop(){npc.getNavigation().stop();}
        public void tick(){
            var o=npc.orders;Vec3 target=o.target();
            double near=o.mode==Mode.FOLLOW?4:o.mode==Mode.STAY?.25:4;
            if(target==null || !npc.level().hasChunkAt(BlockPos.containing(target)) || npc.position().distanceToSqr(target)<=near){npc.getNavigation().stop();return;}
            if(--repath<=0){repath=40+Math.floorMod(npc.getId(),10);npc.getNavigation().moveTo(target.x,target.y,target.z,1.05);}
        }
    }
}
