package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.PrecisionPharmacy;
import io.github.jimbozoomer.jugcraft.pharmacology.DrugPresentation;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import io.github.jimbozoomer.jugcraft.pharmacology.SterilePharmacy;
import io.github.jimbozoomer.jugcraft.pharmacology.DrugStrain;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

/** One bounded native world filter, driven by synchronized effects and private server-authored active flags. */
public final class DrugVisuals {
	public static final Identifier ALCOHOL = Jugcraft.id("drug_alcohol");
	public static final String UNIFORM = "JugcraftDrugVisuals";
	public record Frame(float strength, float blur, float motion, float doubling, float time, float warmth, float vignette,
			float focus, float energy, float tone, float fatigue, float warning, float jitter,
			float thc, float opioid, float echo, float psilocybin, float mescaline, float lsd, float dmt,
			float trailAX,float trailAY,float trailBX,float trailBY,float trailCX,float trailCY,float glimpse) {
		public static final Frame NONE = new Frame(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
	private static volatile Frame frame = Frame.NONE;
	private static LocalPlayer owner;
	private static int age, amplifier = -1, lastDuration;
	private static float previous, strength;
	private static int flags, stimulantAge, lastRushCue;
	// Current and previous extracted channels; fixed size, no entity/world queries during GPU work.
	private static final float[] channels=new float[13], oldChannels=new float[13];
	private DrugVisuals() {}
	public static void register() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
		ClientTickEvents.END_CLIENT_TICK.register(DrugVisuals::tick);
	}
	private static void clear() {
		owner = null; age = 0; amplifier = -1; lastDuration = 0; previous = strength = 0; frame = Frame.NONE;
		flags=stimulantAge=lastRushCue=0; java.util.Arrays.fill(channels,0); java.util.Arrays.fill(oldChannels,0);DrugTrails.clear();
	}
	private static void tick(Minecraft client) {
		if (client.player == null || client.level == null || !client.player.isAlive() || client.player.isSpectator()) { clear(); return; }
		if (owner != client.player) { clear(); owner = client.player; }
		if (client.isPaused()) { previous = strength; System.arraycopy(channels,0,oldChannels,0,channels.length); return; }
		previous = strength;
		var effect = owner.getEffect(PrecisionPharmacy.INTOXICATION);
		float target = 0;
		if (effect != null) {
			if (effect.getAmplifier() != amplifier || effect.getDuration() > lastDuration + 20) age = 0;
			amplifier = effect.getAmplifier(); lastDuration = effect.getDuration();
			// Five seconds rising, forty seconds at peak, then twenty seconds settling to 45%.
			float onset = smooth(age / 100F);
			float settle = 1 - .55F * smooth((age - 800) / 400F);
			float ending = smooth(effect.getDuration() / 100F);
			target = alcoholStrength(amplifier) * onset * settle * ending;
			age++;
		} else { age = 0; amplifier = -1; lastDuration = 0; }
		// Cleansing and expiry ease out within two seconds; death and disconnect clear immediately.
		strength += Math.clamp(target - strength, -.025F, .025F);
		if (strength < .0001F) strength = 0;
		System.arraycopy(channels,0,oldChannels,0,channels.length);
		int active=StrainClient.state().visuals();
		int rushCue=StrainClient.state().rushCue();
		// Other families changing must not replay a settled rush; a real renewed Adrenaline dose does.
		if((active&DrugPresentation.ADRENALINE)!=(flags&DrugPresentation.ADRENALINE) || rushCue!=lastRushCue)stimulantAge=0;
		lastRushCue=rushCue;
		flags=active; stimulantAge=Math.min(1200,stimulantAge+1);
		boolean caffeine=(flags&DrugPresentation.CAFFEINE)!=0, methyl=(flags&DrugPresentation.METHYLPHENIDATE)!=0;
		boolean moda=(flags&DrugPresentation.MODAFINIL)!=0, amp=(flags&DrugPresentation.AMPHETAMINE)!=0;
		boolean adrenaline=(flags&DrugPresentation.ADRENALINE)!=0, stimulant=(flags&DrugPresentation.STIMULANT)!=0;
		approach(0,methyl?.48F:moda?.24F:caffeine?.20F:0);
		float rush=1-.55F*smooth((stimulantAge-120)/200F);
		approach(1,Math.max(adrenaline?.8F*rush:0,amp?.55F:stimulant?.45F:0));
		approach(2,methyl?-1F:moda?-.2F:caffeine?.5F:amp||stimulant?.35F:0);
		approach(3,owner.hasEffect(SterilePharmacy.FATIGUE)?.45F:0);
		approach(4,switch(DrugStrain.band(StrainClient.state().tenths())) {case STABLE -> 0;case STRAINED -> .16F;case OVERLOADED -> .35F;case SEVERE -> .65F;case CRITICAL -> 1F;});
		approach(5,owner.hasEffect(Pharmacology.JITTERS)?.6F:0);
		approach(6,(flags&DrugPresentation.THC)!=0?.55F:0);
		approach(7,(flags&DrugPresentation.FENTANYL)!=0?.75F:(flags&DrugPresentation.MORPHINE)!=0?.5F:(flags&DrugPresentation.FENTANYL_PATCH)!=0?.3F:0);
		approach(8,(flags&DrugPresentation.KETAMINE)!=0 && EchoViewClient.active()?.8F:0);
		approach(9,(flags&DrugPresentation.PSILOCYBIN)!=0?.65F:0);
		approach(10,(flags&DrugPresentation.MESCALINE)!=0?.6F:0);
		approach(11,(flags&DrugPresentation.LSD)!=0?.7F:0);
		approach(12,(flags&DrugPresentation.DMT)!=0?.9F:0);
		var options=ConcordanceClientOptions.drugVisuals();
		DrugTrails.tick(owner,client.level,(flags&(DrugPresentation.LSD|DrugPresentation.DMT))!=0
				&& client.getCameraEntity()==owner && client.gui.screen()==null && options.enabled() && options.intensity()>0
				&& options.motion()>0 && options.doubleVision()>0 && !ConcordanceClientOptions.reducedMotion() && client.options.screenEffectScale().get()>0);
	}
	private static void approach(int channel,float target) {
		channels[channel]+=Math.clamp(target-channels[channel],-.025F,.025F);
		if(Math.abs(channels[channel])<.0001F)channels[channel]=0;
	}
	public static float alcoholStrength(int amplifier) {
		return switch (Math.clamp(amplifier, 1, 5)) { case 1 -> .22F; case 2 -> .38F; case 3 -> .55F; case 4 -> .75F; default -> 1F; };
	}
	private static float smooth(float value) { float x = Math.clamp(value, 0, 1); return x * x * (3 - 2 * x); }
	/** Called during native extraction, never from the renderer's GPU work. */
	public static boolean request(LocalPlayer player) {
		Minecraft client = Minecraft.getInstance();
		var options = ConcordanceClientOptions.drugVisuals();
		boolean echoCamera=EchoViewClient.active() && NouveauScryingClient.active() && client.getCameraEntity()!=player;
		if (player != owner || !player.isAlive() || player.isSpectator() || client.getCameraEntity() != player && !echoCamera
				|| client.gui.screen() != null || !options.enabled() || options.intensity() == 0) { frame = Frame.NONE; DrugTrails.clear();return false; }
		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float scale=options.intensity()/100F*client.options.screenEffectScale().get().floatValue();
		float amount = (previous + (strength - previous) * partial) * scale;
		float focus=channel(0,partial,scale), energy=channel(1,partial,scale), tone=channel(2,partial,scale);
		float fatigue=channel(3,partial,scale), warning=channel(4,partial,scale), jitter=channel(5,partial,scale);
		float thc=channel(6,partial,scale), opioid=channel(7,partial,scale), echo=echoCamera?channel(8,partial,scale):0;
		float psilocybin=channel(9,partial,scale),mescaline=channel(10,partial,scale);
		float lsd=channel(11,partial,scale),dmt=channel(12,partial,scale);
		float strongest=Math.max(Math.max(psilocybin,mescaline),Math.max(Math.max(thc,Math.max(opioid,echo)),Math.max(amount,Math.max(focus,Math.max(energy,Math.max(fatigue,Math.max(warning,jitter)))))));
		if (Math.max(Math.max(lsd,dmt),strongest) < .0001F) { frame = Frame.NONE;DrugTrails.clear();return false; }
		float time = (Math.floorMod(client.level.getGameTime(), 120000) + partial) / 20F;
		float motion=ConcordanceClientOptions.reducedMotion()?0:options.motion()/100F;
		if(motion==0 || options.doubleVision()==0)DrugTrails.clear();
		float glimpse=dmt*(.65F+.35F*motion*(float)Math.sin(time*.22F));
		frame = new Frame(amount, options.blur() / 100F, ConcordanceClientOptions.reducedMotion() ? 0 : options.motion() / 100F,
				options.doubleVision() / 100F, time, .055F, .10F, focus, energy, tone, fatigue, warning, jitter, thc, opioid, echo,psilocybin,mescaline,lsd,dmt,
				DrugTrails.x(0),DrugTrails.y(0),DrugTrails.x(1),DrugTrails.y(1),DrugTrails.x(2),DrugTrails.y(2),glimpse);
		return true;
	}
	public static Frame frame() { return frame; }
	private static float channel(int index,float partial,float scale) { return (oldChannels[index]+(channels[index]-oldChannels[index])*partial)*scale; }
}
