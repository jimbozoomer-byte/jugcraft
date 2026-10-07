package local.peepo;

/** Only action changes are synchronized. Motion phases are evaluated on the client. */
public enum WorkAnimation {
    NONE, INTERACT, STIR;
    public static final double STIR_RADIUS=.5325, STIR_HEIGHT=.60;
    public static double stirPhase(long tick,float partial){return (Math.floorMod(tick,80)+partial)*Math.PI*2/80;}
}
