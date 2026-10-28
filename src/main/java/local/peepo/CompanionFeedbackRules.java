package local.peepo;

import java.util.Objects;

/** Small event filter over cached job reports. No world access, polling, or work scheduling. */
public final class CompanionFeedbackRules {
    public static final int OFF=0,SUBTLE=1,EXPRESSIVE=2,DURATION=40,DEBOUNCE=60;
    public enum Reaction {
        NONE, PLEASED, PUZZLED, TIRED, RELIEVED, HARVEST, CATCH, COOK;
        public boolean finishing(){return this==HARVEST || this==CATCH || this==COOK;}
        public static Reaction from(int id){return id>=0 && id<values().length?values()[id]:NONE;}
    }
    public enum Kind {
        NONE(Reaction.NONE), STOCKED(Reaction.PLEASED), SUPPLIES(Reaction.PUZZLED), TOOL(Reaction.PUZZLED),
        OUTPUT(Reaction.PUZZLED), POWER(Reaction.PUZZLED), HEAT(Reaction.PUZZLED), PATH(Reaction.PUZZLED),
        TIRED(Reaction.TIRED), RELIEVED(Reaction.RELIEVED), BONUS(Reaction.PLEASED), DELIVERY(Reaction.PLEASED),
        HARVEST(Reaction.HARVEST), CATCH(Reaction.CATCH), COOK(Reaction.COOK);
        public final Reaction reaction;
        Kind(Reaction reaction){this.reaction=reaction;}
        public boolean problem(){return ordinal()>=SUPPLIES.ordinal() && ordinal()<=PATH.ordinal();}
        public static Kind from(int id){return id>=0 && id<values().length?values()[id]:NONE;}
    }
    private Kind candidate=Kind.NONE,notified=Kind.NONE;
    private Object candidateKey,notifiedKey;
    private long since,nextProblem,nextPositive;
    private boolean healthy;

    public static Kind classify(CompanionStatus status,boolean generator){
        return switch(status){
            case STOCKED -> Kind.STOCKED;
            case RECOVERING -> Kind.TIRED;
            case NO_INPUT -> generator?Kind.NONE:Kind.SUPPLIES;
            case NO_SEEDS,NO_FOOD,SUPPLY_EMPTY,NEEDS_BREEDING_FOOD,NEEDS_BOTTLES,NEEDS_CONTAINERS,NO_BUCKETS,NEEDS_CERAMICS -> Kind.SUPPLIES;
            case NO_TOOL,NEEDS_SHEARS,NEEDS_CUTTER,NEEDS_HOE,NEEDS_ROD,NEEDS_WEAPON,MACHINE_TOOL -> Kind.TOOL;
            case FULL -> generator?Kind.NONE:Kind.OUTPUT;
            case NO_POWER -> Kind.POWER;
            case NO_HEAT,NEEDS_SMOKE -> Kind.HEAT;
            case BLOCKED,FISHING_BLOCKED -> Kind.PATH;
            default -> Kind.NONE;
        };
    }
    /** Identity is the assignment, never its mutable row number. Normal waits do not resolve an error. */
    public Kind observe(CompanionStatus status,Object key,boolean generator,long now,int mode){
        if(mode==OFF){reset();return Kind.NONE;}
        Kind kind=classify(status,generator);
        boolean productive=status==CompanionStatus.WORKING || status==CompanionStatus.DELIVERING;
        if(kind!=candidate || !Objects.equals(key,candidateKey) || productive!=healthy){
            candidate=kind;candidateKey=key;healthy=productive;since=now;return Kind.NONE;
        }
        if(now-since<DEBOUNCE)return Kind.NONE;
        if(productive && notified!=Kind.NONE && Objects.equals(key,notifiedKey)){
            boolean resolved=notified.problem();
            notified=Kind.NONE;notifiedKey=null;
            return resolved && admit(Kind.RELIEVED,now,mode)?Kind.RELIEVED:Kind.NONE;
        }
        if(kind==Kind.NONE || kind==notified && Objects.equals(key,notifiedKey))return Kind.NONE;
        if(!admit(kind,now,mode))return Kind.NONE;
        notified=kind;notifiedKey=key;return kind;
    }
    public boolean admit(Kind kind,long now,int mode){
        if(!ready(kind,now,mode))return false;
        if(kind.problem() || kind==Kind.TIRED){if(now<nextProblem)return false;nextProblem=now+400;}
        else {if(now<nextPositive)return false;nextPositive=now+600;}
        return true;
    }
    /** Peek without spending a cooldown while a finishing touch waits for a safe transition. */
    public boolean ready(Kind kind,long now,int mode){
        if(mode==OFF || kind==Kind.NONE || (kind==Kind.BONUS || kind==Kind.DELIVERY || kind.reaction.finishing()) && mode!=EXPRESSIVE)return false;
        return now>=(kind.problem() || kind==Kind.TIRED?nextProblem:nextPositive);
    }
    public void reset(){candidate=notified=Kind.NONE;candidateKey=notifiedKey=null;since=0;healthy=false;}
}
