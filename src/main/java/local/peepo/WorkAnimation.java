package local.peepo;

/** Only action changes are synchronized. Motion phases are evaluated on the client. */
public enum WorkAnimation {
    NONE, INTERACT, STIR;
    // Centre of the pot's one-pixel-wide rim, at its actual top surface.
    public static final double STIR_RADIUS=5.0/16, STIR_HEIGHT=8.5/16;
    public static double stirPhase(long tick,float partial){return (Math.floorMod(tick,80)+partial)*Math.PI*2/80;}
}
