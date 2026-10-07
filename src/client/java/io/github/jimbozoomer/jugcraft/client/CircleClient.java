package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoBlockRenderer;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.CircleAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LumenMoteBlock;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualMachine;
import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Rituals on the client (roadmap step 12). Nothing here decides anything; it shows what the server sent:
 * <ul>
 * <li>GeckoLib draws the Circle Anchor and plays its idle or channel animation from the synced phase;</li>
 * <li>the players a gathering or channelling anchor names as participants hold the {@code circle_channel} gesture,
 * on a Player Animation Library layer of Jugcraft's own;</li>
 * <li>motes run along the channels the server last validated (the anchor's linked mask), never along a channel the
 * server did not check;</li>
 * <li>with Fusion installed, the built-in pack {@code fusion_textures} joins neighbouring Warding Stones.</li>
 * </ul>
 * Anchors are tracked as their block entities load and unload on this client, so nothing scans the world.
 */
public final class CircleClient {
	public static final Identifier LAYER = Jugcraft.id("circle");
	public static final Identifier GESTURE = Jugcraft.id("circle_channel");
	/** Below Spell Engine's casting gestures, so a participant who casts still shows the cast. */
	private static final int LAYER_PRIORITY = 900;
	/** How far away beams are drawn. */
	private static final double BEAM_RANGE = 48.0;
	private static final Set<CircleAnchorBlockEntity> ANCHORS = new HashSet<>();

	private CircleClient() {
	}

	static void register() {
		BlockEntityRenderers.register(JugcraftConcordance.ANCHOR_ENTITY,
				context -> new GeoBlockRenderer<CircleAnchorBlockEntity, BlockEntityRenderState>(context, JugcraftConcordance.ANCHOR_ENTITY));
		PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, LAYER_PRIORITY,
				avatar -> new PlayerAnimationController(avatar, (controller, state, setter) -> PlayState.STOP));
		ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof CircleAnchorBlockEntity anchor) {
				ANCHORS.add(anchor);
			}
		});
		ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof CircleAnchorBlockEntity anchor) {
				ANCHORS.remove(anchor);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(CircleClient::tick);
		if (FabricLoader.getInstance().isModLoaded("fusion")) {
			FabricLoader.getInstance().getModContainer(Jugcraft.MOD_ID).ifPresent(container -> ResourceLoader.registerBuiltinPack(
					Jugcraft.id("fusion_textures"), container, Component.translatable("pack.jugcraft.fusion_textures"),
					PackActivationType.ALWAYS_ENABLED));
		}
	}

	private static void tick(Minecraft client) {
		ClientLevel level = client.level;
		if (level == null) {
			ANCHORS.clear();
			return;
		}
		Set<UUID> participants = new HashSet<>();
		long time = level.getGameTime();
		boolean beams = time % (LumenMoteBlock.reducedMotion ? 12 : 4) == 0;
		for (CircleAnchorBlockEntity anchor : List.copyOf(ANCHORS)) {
			if (anchor.isRemoved() || anchor.getLevel() != level || !anchor.phase().reserving()) {
				continue;
			}
			participants.addAll(anchor.run().joined());
			if (beams && anchor.phase() == RitualMachine.Phase.CHANNELING && client.player != null
					&& client.player.distanceToSqr(Vec3.atCenterOf(anchor.getBlockPos())) <= BEAM_RANGE * BEAM_RANGE) {
				beams(level, anchor);
			}
		}
		for (Player player : level.players()) {
			if (!(PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER) instanceof PlayerAnimationController controller)) {
				continue;
			}
			boolean taking = participants.contains(player.getUUID());
			if (taking && !controller.isPlayingTriggeredAnimation()) {
				controller.triggerAnimation(GESTURE);
			} else if (!taking && controller.isPlayingTriggeredAnimation()) {
				controller.stopTriggeredAnimation();
			}
		}
	}

	/** A mote travelling from each linked pylon's crystal to the anchor's. */
	private static void beams(ClientLevel level, CircleAnchorBlockEntity anchor) {
		BlockPos pos = anchor.getBlockPos();
		double tx = pos.getX() + 0.5;
		double ty = pos.getY() + 1.1;
		double tz = pos.getZ() + 0.5;
		List<StructurePattern.Offset> channels = anchor.channelOffsets();
		for (int i = 0; i < channels.size(); i++) {
			if ((anchor.linked() & (1 << i)) == 0) {
				continue;
			}
			StructurePattern.Offset offset = channels.get(i);
			double fx = tx + offset.x();
			double fy = pos.getY() + offset.y() + 0.6;
			double fz = tz + offset.z();
			double t = level.getRandom().nextDouble();
			// The mote drifts towards the anchor; END_ROD's speed is in blocks a tick.
			level.addParticle(ParticleTypes.END_ROD, fx + (tx - fx) * t, fy + (ty - fy) * t, fz + (tz - fz) * t,
					(tx - fx) * 0.04, (ty - fy) * 0.04, (tz - fz) * 0.04);
		}
	}
}
