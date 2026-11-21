package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.TownRoutine;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Draws a townsperson with the skin the server names (one of the town's own; anything else gets the default), posed
 * for what they are doing at their place.
 */
public class TownsfolkRenderer extends HumanoidMobRenderer<Townsfolk, TownsfolkRenderer.State, TownsfolkModel> {
	private static final double PORTRAIT_DISTANCE_SQ = 12.0 * 12.0;
	private static final Pattern SKIN_ID = Pattern.compile("[a-z0-9_]{1,40}");
	private static final Map<String, Identifier> TEXTURES = new HashMap<>();
	private final TownsfolkModel standard;
	private final Map<String, GuestPortraitModel> portraits = new HashMap<>();

	public static class State extends HumanoidRenderState {
		String skin = "townsfolk_1";
		@Nullable GuestPortraitModel portrait;
		/** What they are doing at their place (TownRoutine), posed by TownsfolkModel. */
		TownRoutine.Activity activity = TownRoutine.Activity.NONE;
		/** Each person's own offset into their gestures, so a crowd does not move in step. */
		float phase;
		/** A custom NPC's player skin once loaded (NpcPlayerSkins), drawn instead of the town skin; slim arms if it has them. */
		@Nullable Identifier playerSkin;
		boolean slim;
        boolean seasonalTownResident;
	}

	public TownsfolkRenderer(EntityRendererProvider.Context context) {
		super(context, new TownsfolkModel(context.bakeLayer(TownsfolkModel.SKINNABLE_LAYER)), 0.5F);
		standard = model;
		for (String id : GuestPortraitModel.GUESTS) portraits.put(id, GuestPortraitModel.read(id));
	}

	public TownsfolkModel modelFor(State state) {
		return state.portrait == null ? standard : state.portrait;
	}

	@Override
	protected @Nullable RenderType getRenderType(State state, boolean visible, boolean translucent, boolean outline) {
		// AgeableMobRenderer selects its own base rig at the start of submit. Choose the guest
		// here, when the draw call binds the atlas, so the body and every equipment layer use it.
		model = modelFor(state);
		return super.getRenderType(state, visible, translucent, outline);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public boolean shouldRender(Townsfolk npc, Frustum frustum, double x, double y, double z, float partialTick) {
		if (!super.shouldRender(npc, frustum, x, y, z, partialTick)) return false;
		if (!portraits.containsKey(GuestBody.guest(npc.skin())) || npc.isLeashed() || npc.isPassenger()
				|| npc.isVehicle() || Minecraft.getInstance().shouldEntityAppearGlowing(npc)) return true;
		// Include hair, flared clothes, animated limbs and interpolation outside the collision box.
		var bounds = getBoundingBoxForCulling(npc, partialTick).inflate(1.0);
		return !GuestWallOcclusion.hidden(npc.level(), bounds, x, y, z);
	}

	@Override
	public void extractRenderState(Townsfolk townsfolk, State state, float partialTick) {
		super.extractRenderState(townsfolk, state, partialTick);
		String skin = townsfolk.skin();
        state.seasonalTownResident = !townsfolk.isCustomNpc();
		state.playerSkin = null;
		state.slim = false;
		if (skin.equals(CustomNpcs.PLAYER_SKIN)) {
			// A custom NPC in a player's skin: the town's default until (or unless) it loads.
			NpcPlayerSkins.extract(townsfolk, state);
			skin = "townsfolk_1";
		}
		state.skin = SKIN_ID.matcher(skin).matches() ? skin : "townsfolk_1";
		// Distant guests already have a named, season-specific skin on the lightweight town rig.
		double distanceSq = entityRenderDispatcher.camera == null ? 0 : entityRenderDispatcher.distanceToSqr(townsfolk);
		state.portrait = state.playerSkin == null ? portraitAtDistance(portraits, state.skin, distanceSq) : null;
		if (state.playerSkin == null) {
			// A clearly adult fictional guest's town skin, in any outfit, is drawn on the model's slim arms (GuestBody.SLIM).
			state.slim = GuestBody.slim(state.skin);
		}
		state.activity = townsfolk.activity();
		state.phase = Math.floorMod(townsfolk.getId() * 37, 100) * 0.63F;
		if (townsfolk.isSleeping()) {
			// Lying in bed: no leg swing left over from the last steps to it.
			state.walkAnimationPos = 0.0F;
			state.walkAnimationSpeed = 0.0F;
			state.activity = TownRoutine.Activity.NONE;
		}
		// Gardeners and the florist kneel to their flowers.
		if (state.activity == TownRoutine.Activity.TEND) {
			state.isCrouching = true;
		}
	}

	@Override
	public Identifier getTextureLocation(State state) {
		if (state.playerSkin != null) {
			return state.playerSkin;
		}
		if (state.portrait != null) return state.portrait.texture;
		return TEXTURES.computeIfAbsent(state.skin, skin -> Jugcraft.id("textures/entity/townsfolk/" + skin + ".png"));
	}

	static @Nullable GuestPortraitModel portraitAtDistance(Map<String, GuestPortraitModel> portraits, String skin, double distanceSq) {
		return distanceSq <= PORTRAIT_DISTANCE_SQ ? portraits.get(GuestBody.guest(skin)) : null;
	}
}
