package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.Werewolf;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Draws a werewolf ({@link WerewolfModel}), hunched lower with its jaws open while it hunts. */
public class WerewolfRenderer extends MobRenderer<Werewolf, WerewolfRenderer.State, WerewolfModel> {
	private static final Identifier TEXTURE = Jugcraft.id("textures/entity/werewolf.png");

	public static class State extends LivingEntityRenderState {
		boolean hunting;
	}

	public WerewolfRenderer(EntityRendererProvider.Context context) {
		super(context, new WerewolfModel(context.bakeLayer(WerewolfModel.LAYER)), 0.8F);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(Werewolf werewolf, State state, float partialTick) {
		super.extractRenderState(werewolf, state, partialTick);
		state.hunting = werewolf.isAggressive();
	}

	@Override
	public Identifier getTextureLocation(State state) {
		return TEXTURE;
	}
}
