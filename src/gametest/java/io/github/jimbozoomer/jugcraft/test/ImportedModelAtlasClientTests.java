package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Actual stitched sprites, not merely the existence of PNG files on disk. */
public final class ImportedModelAtlasClientTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext c) {
        c.runOnClient(mc -> {
            verifyInheritedMaterials();
            var resources = mc.getResourceManager();
            var blocks = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
            var items = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS);
            var definitions = new HashMap<Identifier, ModelDefinition>();
            var required = new HashMap<Identifier, String>();
            var missing = new ArrayList<String>();
            for (var entry : resources.listResources("models", id -> id.getNamespace().equals("jugcraft") && id.getPath().endsWith(".json")).entrySet()) {
                var path = entry.getKey().getPath();
                var modelId = entry.getKey().withPath(path.substring("models/".length(), path.length() - ".json".length()));
                var definition = ModelDefinition.read(readJson(entry.getValue(), entry.getKey()));
                definitions.put(modelId, definition);
                // Retain coverage of every supplied model texture, even unused source models.
                // Nonexistent template defaults matter only if a native root actually uses them.
                for (String texture : definition.textures().values()) {
                    if (!texture.startsWith("jugcraft:")) continue;
                    var id = Identifier.parse(texture);
                    if (!required.containsKey(id) && resources.getResource(id.withPath("textures/" + id.getPath() + ".png")).isPresent())
                        required.put(id, modelId.toString());
                }
            }
            var roots = new HashSet<Identifier>();
            for (String folder : new String[]{"blockstates", "items"})
                for (var entry : resources.listResources(folder, id -> id.getNamespace().equals("jugcraft") && id.getPath().endsWith(".json")).entrySet())
                    collectRoots(readJson(entry.getValue(), entry.getKey()), roots);
            var resolver = new ModelResolver(id -> {
                var definition = definitions.get(id);
                if (definition != null) return definition;
                var location = id.withPath("models/" + id.getPath() + ".json");
                var resource = resources.getResource(location);
                // Generated/special item models have no JSON parent geometry.
                if (resource.isEmpty() && id.getNamespace().equals("minecraft") && id.getPath().startsWith("builtin/"))
                    return ModelDefinition.EMPTY;
                if (resource.isEmpty()) throw new AssertionError("Missing native model or parent: " + id);
                definition = ModelDefinition.read(readJson(resource.get(), location));
                definitions.put(id, definition);
                return definition;
            });
            // Match tools/model_atlas.py: resolve parent overrides before choosing drawn
            // face, generated-layer and particle materials. Missing drawn PNGs still fail.
            for (var root : roots) {
                try {
                    for (String texture : resolver.resolve(root).materials())
                        if (texture.startsWith("jugcraft:")) required.putIfAbsent(Identifier.parse(texture), root.toString());
                } catch (AssertionError e) { throw new AssertionError("Invalid materials in native model " + root, e); }
            }
            for (var entry : required.entrySet()) {
                var id = entry.getKey();
                if (blocks.getSprite(id) == blocks.missingSprite() && items.getSprite(id) == items.missingSprite())
                    missing.add(id + " used by " + entry.getValue());
            }
            missing.sort(String::compareTo);
            if (!missing.isEmpty()) throw new AssertionError("Unstitched model textures: " + missing);
            if (required.size() < 11000) throw new AssertionError("Incomplete model audit: " + required.size());
            System.out.println("Jugcraft runtime model atlas audit: " + required.size() + " unique textures resolved from " + roots.size() + " native roots and source art.");
            var blockModels = mc.getModelManager().getBlockStateModelSet();
            int states = 0;
            for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
                var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
                if (!id.getNamespace().equals("jugcraft") || !id.getPath().startsWith("jimta_")) continue;
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    if (state.getRenderShape() != net.minecraft.world.level.block.RenderShape.MODEL) continue;
                    var model = blockModels.get(state);
                    if (model == blockModels.missingModel()) throw new AssertionError("Rejected block model: " + state);
                    var parts = new ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
                    model.collectParts(net.minecraft.util.RandomSource.create(0), parts);
                    for (var part : parts) for (int face = -1; face < 6; face++)
                        for (var quad : part.getQuads(face == -1 ? null : net.minecraft.core.Direction.values()[face]))
                            if (quad.materialInfo().sprite() == blocks.missingSprite())
                                throw new AssertionError("Missing baked face texture: " + state);
                    states++;
                }
            }
            System.out.println("Jugcraft baked imported block audit: " + states + " states resolved without missing faces.");
        });
        try (var world = c.worldBuilder().adjustSettings(s -> s.setGameMode(
                net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                var inventory = new net.minecraft.world.SimpleContainer(54);
                int slot = 0;
                for (String name : java.util.List.of("certus_quartz_crystal", "charged_certus_quartz_crystal",
                        "engineering_processor_press", "calculation_processor_press", "printed_silicon",
                        "black_paint_ball", "blue_lumen_paint_ball", "quantum_ring", "quantum_link",
                        "quantum_entangled_singularity", "item_storage_cell_1k", "fluid_storage_cell_1k",
                        "charged_staff", "color_applicator", "matter_cannon", "annihilation_core", "formation_core",
                        "crafting_card", "advanced_card", "basic_card", "spellbooks_portal_frame", "spellbooks_blaze_spell_book",
                        "power_tester", "mimic_spawn_egg", "spellbooks_firefly_jar", "spellbooks_brazier", "iron_fence")) {
                    var id = Identifier.fromNamespaceAndPath("jugcraft", "jimta_" + name);
                    var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                    if (item == net.minecraft.world.item.Items.AIR) throw new AssertionError("Missing reported item " + id);
                    inventory.setItem(slot++, new net.minecraft.world.item.ItemStack(item));
                }
                player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, who) ->
                        new net.minecraft.world.inventory.ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x6,
                                id, inv, inventory, 6), net.minecraft.network.chat.Component.literal("Reported missing assets — original art")));
            });
            c.waitTicks(15);
            c.takeScreenshot("oct29_reported_item_models");
            c.runOnClient(mc -> mc.player.closeContainer());
            var gallery = new ArrayList<String>();
            try(var reader=new java.io.InputStreamReader(java.util.Objects.requireNonNull(
                    getClass().getResourceAsStream("/imported-visual-gallery.json")),java.nio.charset.StandardCharsets.UTF_8)){
                for(var id:JsonParser.parseReader(reader).getAsJsonArray())gallery.add(id.getAsString());
            }catch(java.io.IOException e){throw new AssertionError(e);}
            for(int start=0;start<gallery.size();start+=54){
                var page=gallery.subList(start,Math.min(start+54,gallery.size()));
                world.getServer().runOnServer(server->{
                    var inventory=new net.minecraft.world.SimpleContainer(54);int slot=0;
                    for(String name:page){
                        var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse(name));
                        if(item==net.minecraft.world.item.Items.AIR)throw new AssertionError("Gallery item absent: "+name);
                        inventory.setItem(slot++,new net.minecraft.world.item.ItemStack(item));
                    }
                    server.getPlayerList().getPlayers().getFirst().openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,who)->
                        new net.minecraft.world.inventory.ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x6,id,inv,inventory,6),
                        net.minecraft.network.chat.Component.literal("Original art repair gallery")));
                });
                c.waitTicks(15);c.takeScreenshot("nov1_original_art_gallery_"+(start/54));
                c.runOnClient(mc->mc.player.closeContainer());
            }
        }
    }

    private static JsonObject readJson(Resource resource, Identifier location) {
        try (var reader = resource.openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException e) { throw new AssertionError(location.toString(), e); }
    }

    private static void collectRoots(JsonElement value, Set<Identifier> roots) {
        if (value.isJsonObject()) {
            for (var entry : value.getAsJsonObject().entrySet()) {
                var child = entry.getValue();
                if ((entry.getKey().equals("model") || entry.getKey().equals("base"))
                        && child.isJsonPrimitive() && child.getAsJsonPrimitive().isString())
                    roots.add(Identifier.parse(child.getAsString()));
                else collectRoots(child, roots);
            }
        } else if (value.isJsonArray()) {
            for (var child : value.getAsJsonArray()) collectRoots(child, roots);
        }
    }

    /** Keep only material references; retaining every imported cuboid would waste client heap. */
    private record ModelDefinition(Identifier parent, Map<String, String> textures, Set<String> faces) {
        private static final ModelDefinition EMPTY = new ModelDefinition(null, Map.of(), Set.of());
        private static ModelDefinition read(JsonObject json) {
            Identifier parent = json.has("parent") ? Identifier.parse(json.get("parent").getAsString()) : null;
            var textures = new HashMap<String, String>();
            if (json.has("textures")) for (var entry : json.getAsJsonObject("textures").entrySet()) {
                var value = entry.getValue();
                // 26.3 also allows material options around a sprite identifier.
                if (value.isJsonObject()) value = value.getAsJsonObject().get("sprite");
                if (value != null && value.isJsonPrimitive()) textures.put(entry.getKey(), value.getAsString());
            }
            Set<String> faces = null; // Absent elements inherit; an empty array replaces them.
            if (json.has("elements")) {
                faces = new HashSet<>();
                for (var element : json.getAsJsonArray("elements")) {
                    var cuboid = element.getAsJsonObject();
                    if (!cuboid.has("faces")) continue;
                    for (var face : cuboid.getAsJsonObject("faces").entrySet()) {
                        var data = face.getValue().getAsJsonObject();
                        if (data.has("texture")) faces.add(data.get("texture").getAsString());
                    }
                }
                faces = Set.copyOf(faces);
            }
            return new ModelDefinition(parent, Map.copyOf(textures), faces);
        }
    }

    private record ResolvedModel(Map<String, String> textures, Set<String> faces) {
        private static final ResolvedModel EMPTY = new ResolvedModel(Map.of(), Set.of());
        private Set<String> materials() {
            var variables = new HashSet<>(faces);
            for (String key : textures.keySet())
                if (key.equals("particle") || key.startsWith("layer")) variables.add("#" + key);
            var result = new HashSet<String>();
            for (String variable : variables) {
                String texture = variable;
                var seen = new HashSet<String>();
                while (texture.startsWith("#")) {
                    if (!seen.add(texture)) throw new AssertionError("Texture alias cycle: " + variable);
                    String resolved = textures.get(texture.substring(1));
                    if (resolved == null) throw new AssertionError("Unresolved used texture alias: " + texture);
                    texture = resolved;
                }
                if (texture.isEmpty()) throw new AssertionError("Empty used texture: " + variable);
                result.add(texture);
            }
            return result;
        }
    }

    private static final class ModelResolver {
        private final Function<Identifier, ModelDefinition> definitions;
        private final Map<Identifier, ResolvedModel> resolved = new HashMap<>();
        private final Set<Identifier> visiting = new HashSet<>();
        private ModelResolver(Function<Identifier, ModelDefinition> definitions) { this.definitions = definitions; }
        private ResolvedModel resolve(Identifier id) {
            var cached = resolved.get(id);
            if (cached != null) return cached;
            if (!visiting.add(id)) throw new AssertionError("Model parent cycle: " + id);
            var model = definitions.apply(id);
            var parent = model.parent() == null ? ResolvedModel.EMPTY : resolve(model.parent());
            var textures = new HashMap<>(parent.textures());
            textures.putAll(model.textures());
            var result = new ResolvedModel(Map.copyOf(textures), model.faces() == null ? parent.faces() : model.faces());
            resolved.put(id, result);
            visiting.remove(id);
            return result;
        }
    }

    private static void verifyInheritedMaterials() {
        var parent = Identifier.parse("jugcraft:test/template");
        var child = Identifier.parse("jugcraft:test/child");
        var replacement = Identifier.parse("jugcraft:test/replacement");
        var definitions = Map.of(
                parent, new ModelDefinition(null, Map.of("face", "#selected", "selected", "jugcraft:unused_default",
                        "unused", "jugcraft:unused_extra", "particle", "#face"), Set.of("#face")),
                child, new ModelDefinition(parent, Map.of("selected", "jugcraft:drawn_child"), null),
                replacement, new ModelDefinition(parent, Map.of("replacement", "jugcraft:drawn_replacement",
                        "particle", "#replacement", "layer0", "jugcraft:drawn_layer"), Set.of("#replacement")));
        var resolver = new ModelResolver(definitions::get);
        if (!resolver.resolve(child).materials().equals(Set.of("jugcraft:drawn_child")))
            throw new AssertionError("Child overrides must replace unused parent texture defaults");
        if (!resolver.resolve(replacement).materials().equals(Set.of("jugcraft:drawn_replacement", "jugcraft:drawn_layer")))
            throw new AssertionError("Replacement elements, particles and generated layers must keep their effective materials");
        if (!resolver.resolve(parent).materials().contains("jugcraft:unused_default"))
            throw new AssertionError("A template used directly must still require its own texture");
        var objectMaterial = ModelDefinition.read(JsonParser.parseString("""
                {"textures":{"face":{"sprite":"jugcraft:object_material","force_translucent":true}},
                 "elements":[{"faces":{"north":{"texture":"#face"}}}]}
                """).getAsJsonObject());
        if (!new ResolvedModel(objectMaterial.textures(), objectMaterial.faces()).materials().equals(Set.of("jugcraft:object_material")))
            throw new AssertionError("Object-form sprite materials must be audited");
        assertMaterialFailure(new ResolvedModel(Map.of(), Set.of("#missing")), "Unresolved used texture alias:");
        assertMaterialFailure(new ResolvedModel(Map.of("a", "#b", "b", "#a"), Set.of("#a")), "Texture alias cycle:");
    }

    private static void assertMaterialFailure(ResolvedModel model, String expected) {
        try { model.materials(); }
        catch (AssertionError e) {
            if (e.getMessage().startsWith(expected)) return;
            throw e;
        }
        throw new AssertionError("Material audit accepted an invalid used alias: " + expected);
    }
}
