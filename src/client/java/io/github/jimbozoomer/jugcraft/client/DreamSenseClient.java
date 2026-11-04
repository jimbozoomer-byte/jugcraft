package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.sign.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Essential recorded-site guidance plus optional budgeted wisps, using only private server-authored sites. */
public final class DreamSenseClient {
	private static DreamCue cue=DreamCue.EMPTY;
	private static LocalPlayer owner;
	private static ClientLevel level;
	private static int age,lastParticles;
	private static boolean pending;
	private static final String[] ARROWS={"↑","↗","→","↘","↓","↙","←","↖"};
	private static final String[] DIRECTIONS={"ahead","ahead_right","right","behind_right","behind","behind_left","left","ahead_left"};
	private DreamSenseClient() {}
	public record Guidance(int sector,double distance,double elevation) {
		public boolean vertical() {return distance*distance-elevation*elevation<.25;}
	}
	public static int lastParticles() {return lastParticles;}
	public static DreamCue state() {
		var c=Minecraft.getInstance();
		if(c.player==null || c.level==null || c.player!=owner || c.level!=level || !c.player.isAlive() || c.player.isSpectator()
				|| !c.level.dimension().equals(Level.OVERWORLD) || NouveauScryingClient.active() || age>60)return DreamCue.EMPTY;
		int flag=switch(cue.mode()) {case 1 -> DrugPresentation.PSILOCYBIN;case 2 -> DrugPresentation.LSD;case 3 -> DrugPresentation.DMT;case 4 -> DrugPresentation.MESCALINE;default -> 0;};
		return (StrainClient.state().visuals()&flag)!=0?cue:DreamCue.EMPTY;
	}
	public static Guidance guidance() {
		var current=state();var c=Minecraft.getInstance();
		if(current.sites().isEmpty() || c.player==null)return null;
		Vec3 delta=Vec3.atCenterOf(current.sites().getFirst()).subtract(c.player.getEyePosition());
		float relative=Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-delta.x,delta.z))-c.player.getYRot());
		return new Guidance(Math.floorMod(Math.round(relative/45F),8),delta.length(),delta.y);
	}
	private static void clear() {cue=DreamCue.EMPTY;owner=null;level=null;age=lastParticles=0;pending=false;}
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(DreamCue.TYPE,(packet,context)-> {
			if(packet.mode()==0){clear();return;}
			cue=packet;owner=context.client().player;level=context.client().level;age=lastParticles=0;pending=true;
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->clear());
		ClientTickEvents.END_CLIENT_TICK.register(c-> {
			if(owner==null)return;
			if(c.player!=owner || c.level!=level || !owner.isAlive() || owner.isSpectator()){clear();return;}
			if(!c.isPaused())age++;
			if(age>60){clear();return;}
			if(pending && state().mode()>0 && c.gui.screen()==null) {pending=false;lastParticles=emit(c,state());}
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,Jugcraft.id("pharmacy_dream_sense"),(g,delta)-> {
			var c=Minecraft.getInstance();var current=state();
			if(current.mode()==0 || c.gui.hud.isHidden() || c.gui.screen()!=null)return;
			int x=4,y=g.guiHeight()-145;String name=Component.translatable("hud.jugcraft.dream.mode_"+current.mode()).getString();
			String line;
			var direction=guidance();
			if(!current.folio())line=Component.translatable("hud.jugcraft.dream.no_folio",name).getString();
			else if(direction==null)line=Component.translatable("hud.jugcraft.dream.no_site",name,current.mode()==4?96:current.mode()==1?16:24).getString();
			else {
				String arrow=direction.vertical()?(direction.elevation()>0?"↑":"↓"):ARROWS[direction.sector()];
				var bearing=Component.translatable("hud.jugcraft.echo."+(direction.vertical()?"directly":DIRECTIONS[direction.sector()]));
				var height=Component.translatable(Math.abs(direction.elevation())<.5?"hud.jugcraft.echo.level":direction.elevation()>0?"hud.jugcraft.echo.above":"hud.jugcraft.echo.below",format(Math.abs(direction.elevation())));
				line=Component.translatable("hud.jugcraft.dream.target",arrow,name,format(direction.distance()),bearing,height).getString();
			}
			g.text(c.font,c.font.plainSubstrByWidth(line,g.guiWidth()-8),x,y,current.mode()==4?0xFF9FE7CF:0xFFD9BCF0);
			line=Component.translatable(current.mode()==4?"hud.jugcraft.dream.recorded_only":"hud.jugcraft.dream.study",current.sites().size()).getString();
			g.text(c.font,c.font.plainSubstrByWidth(line,g.guiWidth()-8),x,y+11,0xFFE7D6B8);
		});
	}
	private static String format(double value) {return String.format(java.util.Locale.ROOT,"%.1f",value);}
	private static int emit(Minecraft c,DreamCue current) {
		var options=ConcordanceClientOptions.drugVisuals();
		if(!options.enabled() || options.intensity()==0 || options.motion()==0 || ConcordanceClientOptions.reducedMotion()
				|| c.options.screenEffectScale().get()==0 || c.player==null || c.level==null)return 0;
		Sign sign=current.mode()==1?Sign.GATHER:current.mode()==3?Sign.CHARM:Sign.FLOW;
		int total=0;
		for(var pos:current.sites()) {
			if(!c.level.hasChunkAt(pos))continue;
			var at=Vec3.atCenterOf(pos).add(0,.8,0);double distance=Math.sqrt(c.player.distanceToSqr(at));
			int base=Math.min(6,Presentation.count(sign,Presentation.intensity(),distance));
			int wanted=(int)Math.ceil(base*options.intensity()/100.0*options.motion()/100.0*c.options.screenEffectScale().get());
			int count=Presentation.take(wanted,false);
			SignClient.draw(c.level,sign,at,null,count,false);total+=count;
		}
		return total;
	}
}
