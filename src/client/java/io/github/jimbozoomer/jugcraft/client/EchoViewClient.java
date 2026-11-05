package io.github.jimbozoomer.jugcraft.client;
import io.github.jimbozoomer.jugcraft.pharmacology.EchoView;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Reuses the existing detached scrying camera; the server supplies every accepted view position. */
public final class EchoViewClient {
	private static Vec3 target, visible, body;
	private static LocalPlayer owner;
	private static net.minecraft.client.multiplayer.ClientLevel level;
	private static final String[] ARROWS={"↑","↗","→","↘","↓","↙","←","↖"};
	private static final String[] DIRECTIONS={"ahead","ahead_right","right","behind_right","behind","behind_left","left","ahead_left"};
	/** The same data drives the HUD and native rendered verification. Elevation is body eye minus view eye. */
	public record Guidance(int sector,double distance,double elevation) {
		public boolean vertical() { return distance*distance-elevation*elevation<.25; }
	}
	private EchoViewClient() {}
	public static boolean active() { return target != null; }
	public static Guidance guidance() {
		var c=Minecraft.getInstance();
		if(target==null || body==null || c.player==null || c.player!=owner || c.level!=level || c.getCameraEntity()==null)return null;
		Vec3 delta=body.add(0,c.player.getEyeHeight(),0).subtract(c.getCameraEntity().getEyePosition());
		float relative=Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-delta.x,delta.z))-c.player.getYRot());
		return new Guidance(Math.floorMod(Math.round(relative/45F),8),delta.length(),delta.y);
	}
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(EchoView.State.TYPE, (packet, context) -> {
			if (!packet.active()) { clear(context.client()); return; }
			if (context.client().player == null || context.client().level == null) return;
			if (target == null) visible = packet.view();
			target = packet.view(); body = packet.body(); owner=context.client().player;level=context.client().level;
			NouveauScryingClient.showView(visible);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear(client));
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,io.github.jimbozoomer.jugcraft.Jugcraft.id("pharmacy_echo_body"),(g,delta)-> {
			var c=Minecraft.getInstance();var guidance=guidance();
			if(guidance==null || c.player==null || !c.player.isAlive() || c.gui.hud.isHidden())return;
			int x=4,y=g.guiHeight()-145;
			String height=Component.translatable(Math.abs(guidance.elevation())<.5?"hud.jugcraft.echo.level":guidance.elevation()>0?"hud.jugcraft.echo.above":"hud.jugcraft.echo.below",String.format(java.util.Locale.ROOT,"%.1f",Math.abs(guidance.elevation()))).getString();
			String arrow=guidance.vertical()?(guidance.elevation()>0?"↑":"↓"):ARROWS[guidance.sector()];
			var direction=Component.translatable("hud.jugcraft.echo."+(guidance.vertical()?"directly":DIRECTIONS[guidance.sector()]));
			String line=guidance.distance()<.5?Component.translatable("hud.jugcraft.echo.at_body").getString():Component.translatable("hud.jugcraft.echo.body",arrow,String.format(java.util.Locale.ROOT,"%.1f",guidance.distance()),direction,height).getString();
			g.text(c.font,c.font.plainSubstrByWidth(line,g.guiWidth()-8),x,y,0xFFABDFE1);
			line=Component.translatable("hud.jugcraft.echo.return",EchoView.RADIUS).getString();
			g.text(c.font,c.font.plainSubstrByWidth(line,g.guiWidth()-8),x,y+11,0xFFE8CFAC);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (target == null) return;
			if (client.player == null || client.level == null || client.player!=owner || client.level!=level) { clear(client); return; }
			if (client.options.keyShift.isDown() || client.gui.screen() != null || !NouveauScryingClient.active() || !client.player.isAlive() || client.player.isSpectator()) {
				ClientPlayNetworking.send(new EchoView.Input((byte) 0, (byte) 0, (byte) 0, 0, 0, true)); clear(client); return;
			}
			client.player.setPos(body); client.player.setDeltaMovement(Vec3.ZERO);
			visible = visible.lerp(target, .5);
			NouveauScryingClient.showView(visible);
			if (client.player.tickCount % 2 == 0) ClientPlayNetworking.send(new EchoView.Input(
					(byte) ((client.options.keyUp.isDown() ? 1 : 0) - (client.options.keyDown.isDown() ? 1 : 0)),
					(byte) ((client.options.keyRight.isDown() ? 1 : 0) - (client.options.keyLeft.isDown() ? 1 : 0)),
					(byte) ((client.options.keyJump.isDown() ? 1 : 0) - (client.options.keySprint.isDown() ? 1 : 0)),
					client.player.getYRot(), client.player.getXRot(), false));
		});
	}
	private static void clear(net.minecraft.client.Minecraft client) {
		if (target != null) NouveauScryingClient.clearView(client);
		target = visible = body = null;owner=null;level=null;
	}
}
