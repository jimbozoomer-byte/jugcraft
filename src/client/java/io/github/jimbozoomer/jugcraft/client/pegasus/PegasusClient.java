package io.github.jimbozoomer.jugcraft.client.pegasus;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pegasus.JugcraftPegasus;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusFlight;
import io.github.jimbozoomer.jugcraft.pegasus.PegasusInputPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;

/**
 * The Pegasus pilot's client (docs/features/pegasus.md): two rebindable keys in their own Jugcraft category
 * ({@code Jugcraft: Pegasus}), Ascend (Space) and Descend (Left Ctrl), read only while this player is the controlling
 * passenger of a Pegasus and no screen or chat is open. Each tick it hands them to the pegasus (whose flight this
 * client simulates, as for any ridden horse) and sends them to the server when they change, and every
 * {@value PegasusFlight#RESEND_TICKS} ticks anyway, numbered, so the server can drop stale or replayed packets.
 *
 * <p>Space and Left Ctrl are also vanilla's Jump and Sprint. Jump keeps working as the horse's normal jump (the
 * takeoff needs a long hold, see PegasusFlight). Sprint is hidden from the player's movement input only while piloting
 * a Pegasus with Descend bound to the sprint key (mixin.client.PegasusSprintKeyMixin), so Ctrl descends without
 * sprinting; no key state is changed, and sprint everywhere else is untouched.
 */
public final class PegasusClient {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Jugcraft.id("pegasus"));
	private static KeyMapping ascend;
	private static KeyMapping descend;
	private static int sequence;
	private static int lastInput = -1;
	private static int lastVehicle = -1;
	private static int sinceSent;

	private PegasusClient() {
	}

	public static void register() {
		ascend = KeyMappingHelper.registerKeyMapping(new KeyMapping(JugcraftPegasus.ASCEND_KEY, InputConstants.KEY_SPACE, CATEGORY));
		descend = KeyMappingHelper.registerKeyMapping(new KeyMapping(JugcraftPegasus.DESCEND_KEY, InputConstants.KEY_LCONTROL, CATEGORY));
		ModelLayerRegistry.registerModelLayer(PegasusModel.LAYER, PegasusModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(PegasusModel.BABY_LAYER, PegasusModel::createBabyLayer);
		ModelLayerRegistry.registerModelLayer(PegasusSaddleModel.LAYER, PegasusSaddleModel::createSaddleLayer);
		EntityRendererRegistry.register(JugcraftPegasus.PEGASUS, PegasusRenderer::new);
		// Before the player's own tick reads its input and the pegasus travels.
		ClientTickEvents.START_CLIENT_TICK.register(PegasusClient::tick);
	}

	public static KeyMapping ascendKey() {
		return ascend;
	}

	public static KeyMapping descendKey() {
		return descend;
	}

	/** The Pegasus this client's player pilots (rides as its controlling passenger), or null. */
	public static @Nullable Pegasus piloted(Minecraft client) {
		LocalPlayer player = client.player;
		return player != null && player.getVehicle() instanceof Pegasus pegasus && pegasus.getControllingPassenger() == player ? pegasus : null;
	}

	/**
	 * Whether a key is held: its own state, or that of any other mapping bound to the same key (should vanilla hand a
	 * shared key, such as Space, to only one of the mappings bound to it).
	 */
	public static boolean held(Minecraft client, KeyMapping mapping) {
		if (mapping.isDown()) {
			return true;
		}
		if (mapping.isUnbound()) {
			return false;
		}
		for (KeyMapping other : client.options.keyMappings) {
			if (other != mapping && other.same(mapping) && other.isDown()) {
				return true;
			}
		}
		return false;
	}

	/** The pilot's input now: nothing while a screen or chat is open, or when not piloting. */
	public static int input(Minecraft client) {
		if (piloted(client) == null || client.gui.screen() != null) {
			return 0;
		}
		return PegasusFlight.encode(held(client, ascend), held(client, descend));
	}

	/** Whether sprint is held off now: piloting a Pegasus with Descend on the sprint key (PegasusSprintKeyMixin). */
	public static boolean suppressSprint() {
		Minecraft client = Minecraft.getInstance();
		return descend != null && piloted(client) != null && descend.same(client.options.keySprint);
	}

	/** The last value sent (or -1), for tests. */
	public static int lastSentInput() {
		return lastInput;
	}

	private static void tick(Minecraft client) {
		Pegasus pegasus = piloted(client);
		if (pegasus == null) {
			lastInput = -1;
			lastVehicle = -1;
			sinceSent = 0;
			return;
		}
		int value = input(client);
		pegasus.pilotInput(PegasusFlight.ascend(value), PegasusFlight.descend(value));
		if (value != lastInput || pegasus.getId() != lastVehicle || ++sinceSent >= PegasusFlight.RESEND_TICKS) {
			ClientPlayNetworking.send(new PegasusInputPayload(pegasus.getId(), value, ++sequence));
			lastInput = value;
			lastVehicle = pegasus.getId();
			sinceSent = 0;
		}
	}
}
