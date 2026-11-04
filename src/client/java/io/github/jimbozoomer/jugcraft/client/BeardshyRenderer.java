package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.season.Beardshy;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Draws Beardshy on the villager's own model, like the Retro Trader (Beardson), in his own whole skin
 * (textures/entity/beardshy/beardshy.png, drawn by tools/beardshy_look.py). Shy: with a player near he bows his head
 * and looks at the floor; hit, he shakes it.
 */
public class BeardshyRenderer extends MobRenderer<Beardshy, VillagerRenderState, VillagerModel> {
	private static final Identifier TEXTURE = Jugcraft.id("textures/entity/beardshy/beardshy.png");
	/** How far he bows his head with a player near, in degrees. */
	private static final float BOW = 32.0F;

	public BeardshyRenderer(EntityRendererProvider.Context context) {
		super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
	}

	public static void register() {
		EntityRendererRegistry.register(Beardshy.TYPE, BeardshyRenderer::new);
	}

	@Override
	public VillagerRenderState createRenderState() {
		return new VillagerRenderState();
	}

	@Override
	public void extractRenderState(Beardshy beardshy, VillagerRenderState state, float partialTick) {
		super.extractRenderState(beardshy, state, partialTick);
		state.isUnhappy = beardshy.hurtTime > 0;
		if (beardshy.level().getNearestPlayer(beardshy, Beardshy.STARTLE_RANGE) != null) {
			state.xRot = Math.max(state.xRot, BOW);
		}
	}

	@Override
	public Identifier getTextureLocation(VillagerRenderState state) {
		return TEXTURE;
	}
}
