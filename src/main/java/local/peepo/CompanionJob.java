package local.peepo;

/** A productive station. The job owns its costs/atomic processing; the routine owns travel and cancellation. */
public interface CompanionJob extends CompanionStation {
    /** Ordinary hauling/priority changes must not abandon a short, perishable operation. Rest and player orders still win. */
    default boolean timeCritical(){return false;}
    /** Finish finite, already-loaded productive work before ordinary same/lower-priority hauling. */
    default boolean finishLoadedBatch(PeepoEntity npc){return false;}
    /** Ask the budgeted transport planner to collect this job's completed goods before moving on. */
    default boolean collectAfterWork(){return false;}
    /** Some completed work still needs a physical finishing supply, such as cider bottles. */
    default boolean completionNeedsSupplies(PeepoEntity npc){return false;}
    default WorkAnimation animation(){return WorkAnimation.INTERACT;}
    default net.minecraft.core.BlockPos animationTarget(){return stationPosition();}
    /** Cheap loaded-state query. Never search for workers or perform recipes here. */
    CompanionStatus workStatus(PeepoEntity npc);
    /** Reusable status for scheduler/UI reads, never for an actual work operation. */
    default CompanionStatus planningStatus(PeepoEntity npc){return npc.readiness.status(this);}
    default CompanionStatus planningFacts(PeepoEntity npc){return workStatus(npc);}
    /** Perform bounded work, or register presence for a ready processor to debit during its validated tick. */
    CompanionStatus work(PeepoEntity npc);
    /** Avoid travelling to a nearly full machine for a fraction of a second of work. */
    default boolean worthStarting(PeepoEntity npc){return planningStatus(npc)==CompanionStatus.READY;}
}
