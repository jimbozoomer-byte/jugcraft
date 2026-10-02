package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.gear.ExosuitItem;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * The exosuit's 3D parts (batch 28): the stacked shoulder plates, the skirt plates and knee guards, and the Ronin's
 * conical hat, which the flat armor layers cannot show. Each part is a set of boxes in a body part's space
 * (tools/exosuit.py), exported to worn_models.json as "style_piece_part" and drawn fixed to that body part, so it
 * follows walking, swinging and sneaking. Drawn on every humanoid (players, zombies, armor stands) wearing the piece.
 */
public class ExosuitLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
	private static final Identifier FILE = Jugcraft.id("worn_models.json");
	/** Body part name in worn_models.json -> that part of the model. */
	private static final Map<String, Function<HumanoidModel<?>, ModelPart>> PARTS = Map.of(
			"head", model -> model.head, "body", model -> model.body,
			"right_arm", model -> model.rightArm, "left_arm", model -> model.leftArm,
			"right_leg", model -> model.rightLeg, "left_leg", model -> model.leftLeg);
	private static final Map<String, QuadModel> MODELS = new HashMap<>();
	private static boolean loaded;

	public ExosuitLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void register() {
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer.getModel() instanceof HumanoidModel<?>) {
				helper.register(new ExosuitLayer((RenderLayerParent) renderer));
			}
		});
	}

	@Override
	public void submit(PoseStack pose, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		draw(pose, collector, light, state.headEquipment, ArmorType.HELMET);
		draw(pose, collector, light, state.chestEquipment, ArmorType.CHESTPLATE);
		draw(pose, collector, light, state.legsEquipment, ArmorType.LEGGINGS);
		draw(pose, collector, light, state.feetEquipment, ArmorType.BOOTS);
	}

	private void draw(PoseStack pose, SubmitNodeCollector collector, int light, ItemStack stack, ArmorType type) {
		if (!(stack.getItem() instanceof ExosuitItem exosuit) || exosuit.type() != type) {
			return;
		}
		Map<String, QuadModel> models = models();
		String prefix = exosuit.style().id + "_" + exosuit.piece() + "_";
		for (Map.Entry<String, Function<HumanoidModel<?>, ModelPart>> part : PARTS.entrySet()) {
			QuadModel model = models.get(prefix + part.getKey());
			ModelPart modelPart = part.getValue().apply(getParentModel());
			if (model == null || !modelPart.visible) {
				continue;
			}
			pose.pushPose();
			modelPart.translateAndRotate(pose);
			// The boxes were flipped half a turn about z when exported (tools/exosuit.py), as for the rocket pack:
			// turn them back, so faces keep the winding the renderer culls by.
			pose.rotateDegrees(Axis.ZP, 180);
			model.submit(pose, collector, light);
			pose.popPose();
		}
	}

	private static Map<String, QuadModel> models() {
		if (!loaded) {
			loaded = true;
			Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(FILE);
			if (resource.isEmpty()) {
				Jugcraft.LOGGER.warn("Missing {}: worn exosuits show only their armor layers", FILE);
				return MODELS;
			}
			try (Reader reader = resource.get().openAsReader()) {
				JsonObject all = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : all.entrySet()) {
					if (entry.getKey().startsWith("vanguard_") || entry.getKey().startsWith("ronin_")) {
						MODELS.put(entry.getKey(), QuadModel.parse(entry.getValue().getAsJsonArray()));
					}
				}
			} catch (Exception e) {
				Jugcraft.LOGGER.warn("Could not read {}", FILE, e);
			}
		}
		return MODELS;
	}
}
