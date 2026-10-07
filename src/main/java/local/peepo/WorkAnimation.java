package local.peepo;

/** Only action changes are synchronized. Motion phases are evaluated on the client. */
public enum WorkAnimation {
    // Append actions: the ordinal is an entity-data wire ID.
    NONE(80), INTERACT(80), STIR(80), VALVE(100), LEVER(64), MALLET(48), WRENCH(64);
    private final int period;
    WorkAnimation(int period){this.period=period;}
    public boolean hasTool(){return this==VALVE || this==LEVER || this==MALLET || this==WRENCH;}
    public float phase(long tick,float partial,int seed){
        return (float)((Math.floorMod(tick+Math.floorMod(seed,period),period)+partial)*Math.PI*2/period);
    }
    /** Reusable roles, independent of footprint, recipe, energy and the companion's appearance. */
    public static WorkAnimation processor(io.github.jimbozoomer.jugcraft.machine.MachineKind kind,int slot){
        boolean second=slot!=0;
        return switch(kind){
            case ORE_WASHER, HYDROPONIC_BAY, ELECTROPLATING_BATH, PUMPJACK, FRACKING_RIG,
                AIR_SEPARATION_UNIT, DISTILLATION_TOWER, CATALYTIC_CRACKER, FLOWBACK_TREATMENT_UNIT,
                POLYMERIZATION_REACTOR, ELECTROLYTIC_CELL, CHEMICAL_REACTOR, SYNTHESIS_CONVERTER,
                HYDROTREATER, AMMONIA_CHILLER, CRYOGENIC_LIQUEFIER -> second?WRENCH:VALVE;
            case CRUSHER, METAL_PRESS, PULVERIZER, SIEVE, COBBLESTONE_GENERATOR -> second?LEVER:MALLET;
            case CIRCUIT_ASSEMBLER, AUTO_CRAFTER, ROCKET_WORKSHOP, LITHOGRAPHY_STATION -> second?LEVER:WRENCH;
            case ELECTRIC_FURNACE, ARC_FURNACE, ALLOY_SMELTER, WIRE_DRAWER, SAWMILL, COKE_OVEN,
                STEEL_FOUNDRY, ORE_DRILL, DEPOSIT_DRILL, TREE_FARM, CROP_HARVESTER -> second?WRENCH:LEVER;
            default -> INTERACT;
        };
    }
    // Centre of the pot's one-pixel-wide rim, at its actual top surface.
    public static final double STIR_RADIUS=5.0/16, STIR_HEIGHT=8.5/16;
    public static double stirPhase(long tick,float partial){return (Math.floorMod(tick,80)+partial)*Math.PI*2/80;}
}
