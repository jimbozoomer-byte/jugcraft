package io.github.jimbozoomer.jugcraft.client;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lineage.AstrawandGesturePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Star-bred lines (docs/features/star-bred-lines.md): when the server says a player has read an animal with the
 * Astrawand, clients that see them play the conducting flourish, on a Player Animation Library layer of its own.
 * Presentation only. Like the Vigil's offering, it gives way to a gun or an arm that owns the player's arms
 * ({@link CastPoses#gesturesYield}).
 */
public final class AstrawandClient {
	public static final Identifier LAYER = Jugcraft.id("astrawand");
	public static final Identifier FLOURISH = Jugcraft.id("astrawand_flourish");
	/** Beside the Vigil's layer, below Spell Engine's casting gestures. */
	private static final int LAYER_PRIORITY = 940;

	private AstrawandClient() {
	}

	static void register() {
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, LAYER_PRIORITY,
				avatar -> new PlayerAnimationController(avatar, (controller, state, setter) -> PlayState.STOP));
		ClientPlayNetworking.registerGlobalReceiver(AstrawandGesturePayload.TYPE, (payload, context) -> flourish(payload.entity()));
	}

	private static void flourish(int entity) {
		Minecraft client = Minecraft.getInstance();
		Entity found = client.level == null ? null : client.level.getEntity(entity);
		if (!(found instanceof Player player) || CastPoses.gesturesYield(player)) {
			return;
		}
		if (PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER) instanceof PlayerAnimationController controller) {
			controller.triggerAnimation(FLOURISH);
		}
	}
}
