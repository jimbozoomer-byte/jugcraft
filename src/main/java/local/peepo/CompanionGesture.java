package local.peepo;

/** Short visual transfer gestures. They never delay or own inventory operations. */
public enum CompanionGesture {
    NONE, CARRY, TAKE(16), PUT(16), POUR, TURN_FOOD, TOOL_DRAW(16), TOOL_STOW(16), BREW_ADD(32), BREW_SWIRL(24),
    TAKE_READY(12), PUT_READY(12);
    public static final int TICKS=12;
    private final int duration;
    CompanionGesture(){this(TICKS);}
    CompanionGesture(int duration){this.duration=duration;}
    public int duration(){return duration;}
    public boolean tool(){return this==TOOL_DRAW || this==TOOL_STOW;}
    public boolean brewing(){return this==BREW_ADD || this==BREW_SWIRL;}
    public boolean preparing(){return this==TAKE_READY || this==PUT_READY;}
    public boolean handling(){return preparing() || this==TAKE || this==PUT || this==CARRY;}
}
