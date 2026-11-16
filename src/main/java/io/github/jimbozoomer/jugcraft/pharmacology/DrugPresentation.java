package io.github.jimbozoomer.jugcraft.pharmacology;

/** Small server-authored presentation flags. Onset markers alone never authorize active visuals. */
public final class DrugPresentation {
	public static final int CAFFEINE=1, METHYLPHENIDATE=2, AMPHETAMINE=4, MODAFINIL=8, ADRENALINE=16, STIMULANT=32;
	public static final int THC=64, MORPHINE=128, FENTANYL=256, FENTANYL_PATCH=512, KETAMINE=1024;
	public static final int PSILOCYBIN=2048, MESCALINE=4096, LSD=8192, DMT=16384;
	public static final int MASK=32767;
	private DrugPresentation() {}
	public static int flag(DrugProfile profile) {
		return switch(profile.id()) {
			case "caffeine", "caffeine_xr" -> CAFFEINE;
			case "methylphenidate", "methylphenidate_xr" -> METHYLPHENIDATE;
			case "amphetamine", "amphetamine_xr" -> AMPHETAMINE;
			case "modafinil", "modafinil_xr" -> MODAFINIL;
			case "adrenaline" -> ADRENALINE;
			case "stimulant" -> STIMULANT;
			case "thc" -> THC;
			case "morphine" -> MORPHINE;
			case "fentanyl" -> FENTANYL;
			case "fentanyl_patch" -> FENTANYL_PATCH;
			case "ketamine" -> KETAMINE;
			case "psilocybin" -> PSILOCYBIN;
			case "mescaline" -> MESCALINE;
			case "lsd" -> LSD;
			case "dmt" -> DMT;
			default -> 0;
		};
	}
}
