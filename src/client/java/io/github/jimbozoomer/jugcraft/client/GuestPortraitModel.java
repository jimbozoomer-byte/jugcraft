package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Guest-specific six-bone rigs share the town's poses and item attachment transforms. */
public final class GuestPortraitModel extends TownsfolkModel {
    public static final List<String> GUESTS = List.of("chun_li", "tifa_lockhart", "yorha_2b", "ada_wong",
            "lara_croft", "kafka", "makima", "misato_katsuragi",
            "emiru", "lauren_chen", "brittany_venti", "aoc", "sarah_stock", "pokimane", "power", "reze",
            "asa_mitaka", "himeno", "tokai_teio", "agnes_tachyon", "gold_ship", "oguri_cap", "special_week", "rei_ayanami", "asuka_langley",
            "frieren", "rem", "megumin", "zero_two", "rin_ishtar", "miyabi", "nicole_demara");
    private final List<ModelPart> winter, harvest, spring;
    private final @Nullable ModelPart braid;
    private final @Nullable ModelPart horseTail;
    private final String guest;
    final Identifier texture;

    private GuestPortraitModel(String guest, ModelPart root) {
        super(root);
        this.guest = guest;
        texture = Jugcraft.id("textures/entity/town_guests/guest_" + guest + ".png");
        winter = List.of(body.getChild("winter_collar"), leftArm.getChild("winter_left_arm"), rightArm.getChild("winter_right_arm"));
        harvest = List.of(head.getChild("harvest_pin"), body.getChild("harvest_collar"));
        spring = List.of(head.getChild("spring_pin"), body.getChild("spring_collar"));
        braid = head.hasChild("braid") ? head.getChild("braid") : null;
        horseTail = body.hasChild("horse_tail") ? body.getChild("horse_tail") : null;
    }

    static GuestPortraitModel read(String id) {
        var file = Jugcraft.id("town_models/guests/" + id + ".json");
        try (var reader = Minecraft.getInstance().getResourceManager().getResourceOrThrow(file).openAsReader()) {
            JsonObject definition = JsonParser.parseReader(reader).getAsJsonObject();
            return new GuestPortraitModel(id, bakeMesh(definition));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read guest model " + file, e);
        }
    }


    private static ModelPart bakeMesh(JsonObject definition) {
        var parts = definition.getAsJsonArray("parts");
        Map<String, Map<String, ModelPart>> children = new HashMap<>();
        for (int i = parts.size() - 1; i >= 0; i--) {
            var part = parts.get(i).getAsJsonObject();
            String name = part.get("name").getAsString();
            List<ModelPart.Cube> geometry = part.has("bounds") ? List.of(new SculptedMesh(part)) : List.of();
            var bone = new ModelPart(geometry, children.getOrDefault(name, Map.of()));
            var pivot = part.getAsJsonArray("pose");
            var pose = PartPose.offset(number(pivot, 0), number(pivot, 1), number(pivot, 2));
            bone.setInitialPose(pose);
            bone.loadPose(pose);
            children.computeIfAbsent(part.get("parent").getAsString(), key -> new LinkedHashMap<>()).put(name, bone);
        }
        return new ModelPart(List.of(), children.get("$mesh"));
    }

    private static float number(JsonArray values, int index) {
        return values.get(index).getAsFloat();
    }

    /** Emit authored UVs and normals through the same consumer as vanilla animated model parts. */
    private static final class SculptedMesh extends ModelPart.Cube {
        private final float[] vertices;
        private final int[] indices;
        private final float[] transformed;
        private final Vector3f position = new Vector3f();
        private final Vector3f normal = new Vector3f();

        SculptedMesh(JsonObject part) {
            this(part, part.getAsJsonArray("bounds"));
        }

        private SculptedMesh(JsonObject part, JsonArray bounds) {
            super(0, 0, number(bounds, 0), number(bounds, 1), number(bounds, 2),
                    number(bounds, 3) - number(bounds, 0), number(bounds, 4) - number(bounds, 1),
                    number(bounds, 5) - number(bounds, 2), 0, 0, 0, false, 1, 1, Set.of());
            var points = part.getAsJsonArray("vertices");
            vertices = new float[points.size() * 8];
            transformed = new float[points.size() * 6];
            for (int i = 0; i < points.size(); i++) {
                var point = points.get(i).getAsJsonArray();
                for (int j = 0; j < 8; j++) vertices[8 * i + j] = number(point, j) / (j < 3 ? 16F : 1F);
            }
            var faces = part.getAsJsonArray("faces");
            indices = new int[faces.size() * 4];
            for (int i = 0; i < faces.size(); i++) {
                var quad = faces.get(i).getAsJsonArray();
                for (int j = 0; j < 4; j++) indices[i * 4 + j] = quad.get(j).getAsInt();
            }
        }

        @Override
        public void compile(PoseStack.Pose pose, VertexConsumer output, int light, int overlay, int color) {
            for (int v = 0, p = 0; v < vertices.length; v += 8, p += 6) {
                pose.pose().transformPosition(vertices[v], vertices[v+1], vertices[v+2], position);
                normal.set(vertices[v+5], vertices[v+6], vertices[v+7]);
                pose.transformNormal(normal, normal);
                transformed[p] = position.x;
                transformed[p+1] = position.y;
                transformed[p+2] = position.z;
                transformed[p+3] = normal.x;
                transformed[p+4] = normal.y;
                transformed[p+5] = normal.z;
            }
            for (int i : indices) {
                int p = i * 6, v = i * 8;
                output.addVertex(transformed[p], transformed[p+1], transformed[p+2], color,
                        vertices[v+3], vertices[v+4], overlay, light, transformed[p+3], transformed[p+4], transformed[p+5]);
            }
        }
    }

    @Override
    public void setupAnim(TownsfolkRenderer.State state) {
        super.setupAnim(state);
        for (ModelPart piece : winter) piece.visible = state.skin.endsWith("_winter");
        for (ModelPart piece : harvest) piece.visible = state.skin.endsWith("_harvest");
        for (ModelPart piece : spring) piece.visible = state.skin.endsWith("_spring");
        if (!state.isPassenger && !state.isCrouching && state.bedOrientation == null
                && state.activity == io.github.jimbozoomer.jugcraft.town.TownRoutine.Activity.NONE) {
            float rest = 1F - Mth.clamp(state.walkAnimationSpeed * 4F, 0F, 1F);
            if (guest.equals("chun_li")) {
                rightLeg.zRot += .115F * rest;
                leftLeg.zRot -= .115F * rest;
                rightArm.xRot -= .25F * rest;
                leftArm.xRot -= .38F * rest;
            } else if (guest.equals("tifa_lockhart") || guest.equals("lara_croft")) {
                rightArm.xRot -= .23F * rest;
                leftArm.zRot -= .09F * rest;
            } else if (guest.equals("kafka") || guest.equals("ada_wong")) {
                leftLeg.zRot -= .065F * rest;
                rightArm.zRot += .10F * rest;
            }
        }
        if (braid != null) {
            braid.yRot -= head.yRot * .36F;
            braid.xRot += Mth.cos(state.walkAnimationPos * .68F) * state.walkAnimationSpeed * .12F;
        }
        if (horseTail != null) {
            horseTail.yRot += Mth.sin(state.walkAnimationPos * .65F) * state.walkAnimationSpeed * .22F;
        }
    }
}
