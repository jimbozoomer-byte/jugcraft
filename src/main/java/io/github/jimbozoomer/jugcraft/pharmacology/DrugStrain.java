package io.github.jimbozoomer.jugcraft.pharmacology;

/** Integer tenths avoid rounding drift. 100 is a safe threshold, never a consumption limit. */
public final class DrugStrain {
	private DrugStrain() {}
	public enum Band {
		STABLE(0, 0, 0, 1, 1, 0xFF86C995),
		STRAINED(0, 0, 0, 1.15F, 1.10F, 0xFFE0C777),
		OVERLOADED(.10, .10, 0, 1.25F, 1.20F, 0xFFE8AA62),
		SEVERE(.20, .25, .15, 1.40F, 1.35F, 0xFFE88265),
		CRITICAL(.30, .40, .25, 1.60F, 1.50F, 0xFFF46E75);
		public final double movement, mining, attackSpeed;
		public final float food, spread;
		public final int color;
		Band(double movement, double mining, double attackSpeed, float food, float spread, int color) {
			this.movement=movement; this.mining=mining; this.attackSpeed=attackSpeed; this.food=food; this.spread=spread; this.color=color;
		}
		public String key() { return "strain.jugcraft." + name().toLowerCase(java.util.Locale.ROOT); }
	}
	public static Band band(int tenths) {
		return tenths <= 1000 ? Band.STABLE : tenths <= 1250 ? Band.STRAINED : tenths <= 1500 ? Band.OVERLOADED : tenths <= 2000 ? Band.SEVERE : Band.CRITICAL;
	}
	public static int familyBit(String family) {
		return switch(family) { case "intoxicant" -> 1; case "opioid" -> 2; case "dissociative" -> 4; default -> 0; };
	}
	public static int surcharge(String family, int mask) { int bit=familyBit(family); return bit != 0 && (mask & ~bit & 7) != 0 ? 25 : 0; }
	public static int add(int tenths, int points) { return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, tenths) + points * 10L); }
	public static String format(int tenths) { return tenths / 10 + "." + tenths % 10; }
}
