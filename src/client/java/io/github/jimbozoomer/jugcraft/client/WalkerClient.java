package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.walker.DieselWalker;
import io.github.jimbozoomer.jugcraft.walker.HowitzerWalker;
import io.github.jimbozoomer.jugcraft.walker.WalkerInputPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Sends the Diesel Walker pilot's keys to the server, which drives it: forward/back, left/right to turn, jump, use
 * to drill and attack to punch. Sent when they change, and every few ticks anyway so the server knows the pilot is
 * still there. The pilot's view turns with the hull (docs/features/walker-cockpit.md), so turning with left/right keeps
 * the crosshair where it was on the walker, as a turret's sight does, and the mouse aims from there.
 */
public final class WalkerClient {
	private static final int RESEND = 5;
	private static WalkerInputPayload last = new WalkerInputPayload(0, 0, 0, 0, 0);
	private static int sinceSent;
	/** The hull's yaw last tick, and which walker it was, to turn the view with it. */
	private static int followed = -1;
	private static float followedYaw;

	private WalkerClient() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(WalkerClient::tick);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.isPaused() || !(player.getVehicle() instanceof DieselWalker walker)
				|| walker.getFirstPassenger() != player) {
			followed = -1;
			return;
		}
		WalkerInputPayload input = client.gui.screen() != null ? new WalkerInputPayload(0, 0, 0, 0, 0)
				: new WalkerInputPayload(
						(client.options.keyUp.isDown() ? 1 : 0) - (client.options.keyDown.isDown() ? 1 : 0),
						(client.options.keyLeft.isDown() ? 1 : 0) - (client.options.keyRight.isDown() ? 1 : 0),
						client.options.keyJump.isDown() ? 1 : 0, client.options.keyUse.isDown() ? 1 : 0,
						client.options.keyAttack.isDown() ? 1 : 0);
		followHull(player, walker, input.turn() != 0);
		if (!input.equals(last) || ++sinceSent >= RESEND) {
			ClientPlayNetworking.send(input);
			last = input;
			sinceSent = 0;
		}
	}

	/**
	 * Turns the pilot's view by however far the hull turned since last tick. The Howitzer Walker's hull also turns by
	 * itself to lay its gun where the pilot looks; following that would chase the look round, so its view follows only
	 * while the pilot turns it with left/right.
	 */
	private static void followHull(LocalPlayer player, DieselWalker walker, boolean turning) {
		float yaw = walker.getYRot();
		if (followed == walker.getId() && (turning || !(walker instanceof HowitzerWalker))) {
			float turned = net.minecraft.util.Mth.wrapDegrees(yaw - followedYaw);
			if (turned != 0.0F) {
				player.setYRot(player.getYRot() + turned);
				player.setYHeadRot(player.getYHeadRot() + turned);
			}
		}
		followed = walker.getId();
		followedYaw = yaw;
	}
}
