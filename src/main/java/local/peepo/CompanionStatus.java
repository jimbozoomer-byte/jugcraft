package local.peepo;

public enum CompanionStatus {
    NONE("Not assigned"), READY("Ready"), TRAVELLING("Travelling"), WORKING("Working"),
    RESTING("Resting"), EATING("Eating"), FULL("Full"), NO_INPUT("Missing inputs"), NO_POWER("No power"),
    OCCUPIED("Occupied"), BLOCKED("Path blocked"), UNLOADED("Unloaded"), MISSING("Missing/replaced"),
    UNSUPPORTED("Unsupported job"), SCHEDULED_REST("Off shift"), RECOVERING("Recovering"),
    FORBIDDEN("Access denied"), NO_FOOD("No suitable food"), WAITING("Waiting for budget"), IDLE("Idle"),
    FOLLOWING("Following"), STAYING("Staying"), FETCHING_FOOD("Getting lunch"), OTHER_DIMENSION("Other dimension"),
    NO_HEAT("No heat"), RECIPE_MISSING("Recipe unavailable"), REDSTONE_DISABLED("Disabled by redstone");
    public final String label;
    CompanionStatus(String label){this.label=label;}
    public static CompanionStatus from(int id){return values()[Math.clamp(id,0,values().length-1)];}
    public boolean problem(){return this==FULL || this==NO_INPUT || this==NO_POWER || this==BLOCKED || this==NO_FOOD || this==MISSING || this==FORBIDDEN || this==NO_HEAT || this==RECIPE_MISSING || this==REDSTONE_DISABLED;}
}
