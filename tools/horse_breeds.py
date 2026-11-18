#!/usr/bin/env python3
"""Generate the five horse breeds' runtime models, sizes and textures from their Blockbench sources.

    python3 tools/horse_breeds.py            # write the generated files
    python3 tools/horse_breeds.py --check    # compare them with what is committed (exit 1 on any difference)

Feature record: docs/features/horse-breeds.md. Sources: art/horse_breeds/ (see its README for provenance).

What it writes:
- src/client/java/.../client/horses/BreedHorseMeshes.java: every breed's horse, saddle and (where it has them)
  saddlebag meshes, built from the .bbmodel files with the documented Blockbench -> Minecraft conversion, plus each
  breed's rearing rig (the AbstractEquineModel hook values; vanilla's except the Giant Finnhorse's);
- src/main/java/.../horses/BreedDimensions.java: each breed's hitbox, eye height and rider seat, measured from the
  same models against vanilla's horse (EntityType.HORSE: 1.3964844 x 1.6, eyes 1.52, seat 1.44375 with the saddle top
  23.67 in-game px above the floor);
- textures/entity/horse_breeds/<id>.png and <id>_saddle.png: byte for byte the PNGs the .bbmodel files embed;
- the saddlebag models (art/horse_breeds/<art>/<art>_saddlebags.bbmodel and art/horse_breeds/pegasus/) and their
  textures, from the specifications below (horse_breeds_art.py paints them);
- the spawn egg icons, the loot tables and the jugcraft:horse_roads block tag.

Conversion (Blockbench -> Minecraft model space, the inverse of tools/check_pegasus_model.py's): a root group's
origin o gives the part pivot (-o.x, 24 - o.y, o.z); a child's pivot is (-(o.x - p.x), -(o.y - p.y), o.z - p.z) from
its parent's origin p; rotations (degrees) become radians (-x, -y, z); a cube from f to t (un-inflated) in a group with
origin o is addBox(o.x - t.x, o.y - t.y, f.z - o.z, w, h, d) with its inflate as the CubeDeformation; uv_offset is
texOffs and mirror_uv is .mirror(). In game the layers are scaled once with MeshTransformer.scaling(1.1), as vanilla's
ModelLayers.HORSE is (Codex's 26.3 evidence); the .bbmodel files never are.

Standard library plus Pillow (for the painted saddlebag and egg textures). Not a Java compile or a game run.
"""
import argparse
import base64
import io
import json
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art/horse_breeds"
CLIENT = ROOT / "src/client/java/io/github/jimbozoomer/jugcraft/client/horses"
SERVER = ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/horses"
ASSETS = ROOT / "src/main/resources/assets/jugcraft"
DATA = ROOT / "src/main/resources/data/jugcraft"
GAME_SCALE = 1.1

# Vanilla 26.3 horse (EntityType.HORSE and the base .bbmodel measured the same way as the breeds).
VANILLA_WIDTH, VANILLA_HEIGHT, VANILLA_EYES, VANILLA_SEAT = 1.3964844, 1.6, 1.52, 1.44375
VANILLA_BODY_LENGTH = 22.1     # raw px, the body group's length (with the 0.05 inflate)
VANILLA_WITHERS = 23.17        # in-game px above the floor, the top of the body group
VANILLA_SADDLE_TOP = 23.67     # in-game px above the floor, the top of the saddle cube

# The vanilla rearing rig (AbstractEquineModel's hook values in the 26.3 javap). A breed rig changes only what it names.
VANILLA_RIG = {"leg_standing_y": 12.0, "leg_standing_z": 4.0, "head_stand_lift": -8.0, "head_stand_z": -4.0,
               "head_eat_drop": 7.0, "body_stand_lift": 0.0, "hind_leg_stand_z": 0.0}

BREEDS = {
    "icelandic_horse": {"art": "icelandic", "rig": {}},
    "mongolian_horse": {"art": "mongolian", "rig": {}},
    "shire_horse": {"art": "shire", "rig": {}, "bags": True},
    # The Giant Finnhorse's rearing (v2 README: the hind legs sank 6.94 raw px into the rump). Chosen by a grid search
    # over these seven values against the shared pose sweep's rearing states (front legs and neck kept attached, nothing
    # through the floor): the deepest hind-leg sink falls from 7.0 to 3.3 raw px (vanilla 2.9; 5.3 scaled to the giant).
    # See docs/features/horse-breeds.md#giant-finnhorse-rearing.
    "giant_finnhorse": {"art": "finnhorse", "bags": True, "rig": {
        "leg_standing_y": 22.0, "leg_standing_z": 6.0, "head_stand_lift": -16.0, "head_stand_z": -6.0,
        "head_eat_drop": 7.0, "body_stand_lift": 8.0, "hind_leg_stand_z": 3.0}},
    "friesian_horse": {"art": "friesian", "rig": {}},
}
PEGASUS_PACK = "pegasus"       # art/horse_breeds/pegasus/pegasus_saddlebags.bbmodel (the Pegasus's light pack)


def rig(breed):
    out = dict(VANILLA_RIG)
    out.update(BREEDS[breed]["rig"])
    return out


# ---------------------------------------------------------------------------------------------- bbmodel -> parts
def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def parts_of(model):
    """[(name, parent or None, pivot offset, rotation radians, [cube])] in outliner order (Minecraft space)."""
    elements = {e["uuid"]: e for e in model["elements"]}
    out = []

    def walk(node, parent):
        o = node["origin"]
        if parent is None:
            offset = (-o[0], 24.0 - o[1], o[2])
        else:
            p = parent["origin"]
            offset = (-(o[0] - p[0]), -(o[1] - p[1]), o[2] - p[2])
        r = node.get("rotation", [0, 0, 0])
        rotation = (-math.radians(r[0]), -math.radians(r[1]), math.radians(r[2]))
        cubes = []
        for child in node.get("children", []):
            if isinstance(child, str):
                el = elements[child]
                if any(el.get("rotation", [0, 0, 0])):
                    raise SystemExit(f"{el['name']}: element rotations are not supported")
                f, t = el["from"], el["to"]
                cubes.append({"name": el["name"], "from": (o[0] - t[0], o[1] - t[1], f[2] - o[2]),
                              "size": (t[0] - f[0], t[1] - f[1], t[2] - f[2]), "uv": tuple(el.get("uv_offset", [0, 0])),
                              "mirror": bool(el.get("mirror_uv")), "inflate": float(el.get("inflate", 0) or 0)})
        out.append((node["name"], parent["name"] if parent else None, offset, rotation, cubes))
        for child in node.get("children", []):
            if isinstance(child, dict):
                walk(child, node)

    for top in model["outliner"]:
        walk(top, None)
    return out


def resolution(model):
    return model["resolution"]["width"], model["resolution"]["height"]


def embedded_png(model, index=0):
    return base64.b64decode(model["textures"][index]["source"].split(",", 1)[1])


# ---------------------------------------------------------------------------------------------- Java
def f(v):
    v = round(v, 7)
    if v == 0:
        v = 0.0
    text = repr(float(v))
    return text + "F"


def java_var(name):
    head, *rest = name.split("_")
    return head + "".join(w.capitalize() for w in rest)


def mesh_java(method, parts, comment, keep_cubes=None):
    """A static method building the parts; keep_cubes: part names whose cubes are kept (None keeps all)."""
    lines = [f"\t/** {comment} */", f"\tstatic MeshDefinition {method}() {{",
             "\t\tMeshDefinition mesh = new MeshDefinition();", "\t\tPartDefinition root = mesh.getRoot();"]
    used = {}
    for name, parent, offset, rotation, cubes in parts:
        var = java_var(name)
        while var in used.values() or var in ("mesh", "root"):
            var += "Part"
        used[name] = var
        parent_var = used[parent] if parent else "root"
        builder = "CubeListBuilder.create()"
        mirror = False
        for c in cubes if keep_cubes is None or name in keep_cubes else []:
            if c["mirror"] != mirror:
                builder += "\n\t\t\t\t" + (".mirror()" if c["mirror"] else ".mirror(false)")
                mirror = c["mirror"]
            x, y, z = c["from"]
            w, h, d = c["size"]
            grow = f", new CubeDeformation({f(c['inflate'])})" if c["inflate"] else ""
            builder += (f"\n\t\t\t\t.texOffs({c['uv'][0]}, {c['uv'][1]}).addBox({f(x)}, {f(y)}, {f(z)}, "
                        f"{f(w)}, {f(h)}, {f(d)}{grow})")
        if any(abs(a) > 1e-9 for a in rotation):
            pose = (f"PartPose.offsetAndRotation({f(offset[0])}, {f(offset[1])}, {f(offset[2])}, "
                    f"{f(rotation[0])}, {f(rotation[1])}, {f(rotation[2])})")
        else:
            pose = f"PartPose.offset({f(offset[0])}, {f(offset[1])}, {f(offset[2])})"
        lines.append(f"\t\tPartDefinition {var} = {parent_var}.addOrReplaceChild(\"{name}\", {builder},")
        lines.append(f"\t\t\t\t{pose});")
    lines.append("\t\treturn mesh;")
    lines.append("\t}")
    return "\n".join(lines)


def tack_parts(horse_parts, tack_parts_list, label):
    """A tack layer (saddle or saddlebags) on cube-less copies of every horse bone it does not carry itself.

    The tack file's own groups must sit on the horse's pivots (they share the horse's group origins), so the layer moves
    with the horse under vanilla's setupAnim and the breed's rig."""
    horse = {p[0]: p for p in horse_parts}
    own = {p[0]: p for p in tack_parts_list}
    out = []
    for name, parent, offset, rotation, cubes in horse_parts:
        if name in own:
            _, tparent, toffset, trotation, tcubes = own[name]
            if tparent != parent or max(abs(a - b) for a, b in zip(toffset, offset)) > 1e-6 or max(
                    abs(a - b) for a, b in zip(trotation, rotation)) > 1e-6:
                raise SystemExit(f"{label}: bone {name} is not on the horse's pivot ({toffset} vs {offset})")
            out.append((name, parent, offset, rotation, tcubes))
        elif parent is None or name == "tail":
            out.append((name, parent, offset, rotation, []))
    for name, parent, offset, rotation, cubes in tack_parts_list:
        if name not in horse:
            if parent not in horse:
                raise SystemExit(f"{label}: {name} hangs on {parent}, not a horse bone")
            out.append((name, parent, offset, rotation, cubes))
    return out


# ---------------------------------------------------------------------------------------------- measuring
def world_boxes(parts):
    """Rest-pose world boxes (Minecraft space, raw px) of every cube: [(part, cube, lo, hi)]."""
    pivots = {}
    rot = {}
    out = []
    for name, parent, offset, rotation, cubes in parts:
        base = pivots[parent] if parent else (0.0, 0.0, 0.0)
        pivots[name] = tuple(base[i] + offset[i] for i in range(3))
        rot[name] = (rot.get(parent, (0, 0, 0)) if parent else (0, 0, 0))
        rx = rot[name][0] + rotation[0]
        rot[name] = (rx, 0, 0)
        for c in cubes:
            g = c["inflate"]
            lo = [c["from"][i] - g for i in range(3)]
            hi = [c["from"][i] + c["size"][i] + g for i in range(3)]
            corners = [(x, y, z) for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])]
            cx, sx = math.cos(rx), math.sin(rx)
            world = [(pivots[name][0] + x, pivots[name][1] + y * cx - z * sx, pivots[name][2] + y * sx + z * cx)
                     for x, y, z in corners]
            out.append((name, c["name"], tuple(min(p[i] for p in world) for i in range(3)),
                        tuple(max(p[i] for p in world) for i in range(3))))
    return out


def height_game(mc_y):
    """Minecraft model y (down, floor 24.016) -> in-game px above the floor after MeshTransformer.scaling(1.1)."""
    return GAME_SCALE * (24.016 - mc_y)


def measure(horse_parts, saddle_parts):
    boxes = world_boxes(horse_parts)
    body = [b for b in boxes if b[0] == "body"]
    length = max(b[3][2] for b in body) - min(b[2][2] for b in body)
    withers = height_game(min(b[2][1] for b in body))
    top = height_game(min(b[2][1] for b in boxes))
    saddle = [b for b in world_boxes(saddle_parts) if b[1] == "saddle"]
    seat = height_game(min(b[2][1] for b in saddle))
    return {"body_length": length, "withers": withers, "top": top, "saddle_top": seat}


def sixteenth(v):
    return round(v * 16) / 16


def dimensions(m):
    width = sixteenth(VANILLA_WIDTH * m["body_length"] / VANILLA_BODY_LENGTH)
    height = sixteenth(VANILLA_HEIGHT * m["withers"] / VANILLA_WITHERS)
    if abs(m["body_length"] - VANILLA_BODY_LENGTH) < 0.6:
        width = VANILLA_WIDTH
    if abs(m["withers"] - VANILLA_WITHERS) < 0.5:
        height = VANILLA_HEIGHT
    eyes = round(height * VANILLA_EYES / VANILLA_HEIGHT, 4)
    seat = round(VANILLA_SEAT + (m["saddle_top"] - VANILLA_SADDLE_TOP) / 16.0, 4)
    if abs(seat - VANILLA_SEAT) < 0.002:
        seat = VANILLA_SEAT
    return {"width": width, "height": height, "eyes": eyes, "seat": seat}


# ---------------------------------------------------------------------------------------------- generation
def art_paths(breed):
    a = BREEDS[breed]["art"]
    return ART / a / f"{a}_horse.bbmodel", ART / a / f"{a}_saddle.bbmodel", ART / a / f"{a}_saddlebags.bbmodel"


def const(breed):
    return breed.upper()


def generate():
    """{path: bytes} of every generated file."""
    import horse_breeds_art as paint
    files = {}
    for path, model in paint.saddlebag_models().items():
        files[path] = (json.dumps(model, indent=2) + "\n").encode("utf-8")
    loaded = {p: json.loads(b) for p, b in files.items()}

    methods, table, rigs, sizes = [], [], [], []
    dims = {}
    for breed in BREEDS:
        horse_path, saddle_path, bags_path = art_paths(breed)
        horse, saddle = load(horse_path), load(saddle_path)
        hp, sp = parts_of(horse), parts_of(saddle)
        name = java_var(breed)
        rel = lambda p: p.relative_to(ROOT).as_posix()
        methods.append(mesh_java(name, hp, f"{rel(horse_path)}: the horse (12 vanilla bones, the breed's cubes)."))
        methods.append(mesh_java(name + "Saddle", tack_parts(hp, sp, rel(saddle_path)),
                                 f"{rel(saddle_path)}: saddle and bridle on cube-less copies of the horse's bones."))
        layers = [("main", name), ("saddle", name + "Saddle")]
        if BREEDS[breed].get("bags"):
            bags = loaded[bags_path]
            methods.append(mesh_java(name + "Saddlebags", tack_parts(hp, parts_of(bags), rel(bags_path)),
                                     f"{rel(bags_path)}: the saddlebags, fitted with a chest."))
            layers.append(("saddlebags", name + "Saddlebags"))
        for layer, method in layers:
            source = {"main": horse, "saddle": saddle}.get(layer) or loaded[bags_path]
            w, h = resolution(source)
            table.append(f"\t\t\tcase \"{breed}/{layer}\" -> new Source({method}(), {w}, {h});")
        r = rig(breed)
        rigs.append(f"\t\t\tcase \"{breed}\" -> new Rig({f(r['leg_standing_y'])}, {f(r['leg_standing_z'])}, "
                    f"{f(r['head_stand_lift'])}, {f(r['head_stand_z'])}, {f(r['head_eat_drop'])}, "
                    f"{f(r['body_stand_lift'])}, {f(r['hind_leg_stand_z'])});")
        m = measure(hp, tack_parts(hp, sp, "saddle"))
        dims[breed] = dimensions(m)
        d = dims[breed]
        sizes.append(f"\t\t\tcase \"{breed}\" -> new Size({f(d['width'])}, {f(d['height'])}, {f(d['eyes'])}, {f(d['seat'])});"
                     f" // body {m['body_length']:.2f} raw px long, withers {m['withers']:.2f} and saddle top"
                     f" {m['saddle_top']:.2f} in-game px")
        files[ASSETS / "textures/entity/horse_breeds" / f"{breed}.png"] = embedded_png(horse)
        files[ASSETS / "textures/entity/horse_breeds" / f"{breed}_saddle.png"] = embedded_png(saddle)
        if BREEDS[breed].get("bags"):
            files[ASSETS / "textures/entity/horse_breeds" / f"{breed}_saddlebags.png"] = embedded_png(loaded[bags_path])
    # The Pegasus's light pack, on the Pegasus's own rig (vanilla's bones, from art/pegasus/pegasus.bbmodel).
    pegasus = parts_of(load(ROOT / "art/pegasus/pegasus.bbmodel"))
    pegasus = [p for p in pegasus if "wing" not in p[0]]
    pack_path = ART / "pegasus" / "pegasus_saddlebags.bbmodel"
    pack = loaded[pack_path]
    methods.append(mesh_java("pegasusSaddlebags", tack_parts(pegasus, parts_of(pack), pack_path.relative_to(ROOT).as_posix()),
                             "art/horse_breeds/pegasus/pegasus_saddlebags.bbmodel: the Pegasus's light pack, fitted with a chest."))
    w, h = resolution(pack)
    table.append(f"\t\t\tcase \"pegasus/saddlebags\" -> new Source(pegasusSaddlebags(), {w}, {h});")
    files[ASSETS / "textures/entity/pegasus/pegasus_saddlebags.png"] = embedded_png(pack)

    files[CLIENT / "BreedHorseMeshes.java"] = meshes_java(methods, table, rigs).encode("utf-8")
    files[SERVER / "BreedDimensions.java"] = dimensions_java(sizes).encode("utf-8")
    for breed in list(BREEDS) + ["pegasus"]:
        files[DATA / "loot_table/entities" / f"{breed}.json"] = (json.dumps(loot(), indent=2) + "\n").encode("utf-8")
    files[DATA / "tags/block/horse_roads.json"] = (json.dumps(ROADS, indent=2) + "\n").encode("utf-8")
    for breed in BREEDS:
        files[ASSETS / "textures/item" / f"{breed}_spawn_egg.png"] = paint.egg_png(breed)
    return files, dims


ROADS = {"replace": False, "values": [
    "minecraft:dirt_path", "minecraft:gravel", "minecraft:cobblestone", "minecraft:mossy_cobblestone",
    "minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks", "minecraft:bricks",
    "minecraft:mud_bricks", "minecraft:polished_andesite", "minecraft:polished_granite", "minecraft:polished_diorite",
    "minecraft:smooth_stone", "minecraft:packed_mud", "minecraft:cobblestone_slab", "minecraft:stone_brick_slab",
    "minecraft:brick_slab", "minecraft:smooth_stone_slab"]}


def loot():
    """A horse's drop (vanilla's horse table: 0-2 leather, +0-1 a looting level), once, from the killed animal only."""
    return {"type": "minecraft:entity", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
        "type": "minecraft:item", "name": "minecraft:leather", "modifier": [
            {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}, "add": False},
            {"type": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting",
             "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}]}


HEADER = "// GENERATED by tools/horse_breeds.py from art/horse_breeds and art/pegasus: do not edit by hand.\n"


def meshes_java(methods, table, rigs):
    return HEADER + """package io.github.jimbozoomer.jugcraft.client.horses;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The horse breeds' meshes (docs/features/horse-breeds.md), in raw mesh px exactly as their editable Blockbench sources
 * (art/horse_breeds/, art/pegasus/pegasus.bbmodel for the Pegasus pack's bones). Each horse keeps vanilla 26.3's 12
 * bones, names and parents, so AbstractEquineModel's setupAnim walks, grazes, rears and swishes it; the tack layers carry
 * cube-less copies of the same bones, so they move with it. BreedHorseModels scales each layer once with
 * MeshTransformer.scaling(1.1F), as vanilla's ModelLayers.HORSE is; nothing here is pre-scaled.
 */
final class BreedHorseMeshes {
	/** A layer's mesh and its texture size. */
	record Source(MeshDefinition mesh, int textureWidth, int textureHeight) {
	}

	/**
	 * A breed's rearing and grazing rig: the values AbstractEquineModel's protected hooks return (vanilla's 26.3 values
	 * are 12, 4, -8, -4 and 7), plus how far the body rises and the hind legs step back while it stands (vanilla: none).
	 */
	record Rig(float legStandingY, float legStandingZ, float headStandLift, float headStandZ, float headEatDrop,
			float bodyStandLift, float hindLegStandZ) {
	}

	private BreedHorseMeshes() {
	}

	/** The mesh of {@code breed}'s {@code layer} (main, saddle or saddlebags). */
	static Source source(String breed, String layer) {
		return switch (breed + "/" + layer) {
""" + "\n".join(table) + """
			default -> throw new IllegalArgumentException("No horse breed layer " + breed + "/" + layer);
		};
	}

	static Rig rig(String breed) {
		return switch (breed) {
""" + "\n".join(rigs) + """
			default -> new Rig(12.0F, 4.0F, -8.0F, -4.0F, 7.0F, 0.0F, 0.0F);
		};
	}

""" + "\n\n".join(methods) + "\n}\n"


def dimensions_java(sizes):
    return HEADER + """package io.github.jimbozoomer.jugcraft.horses;

/**
 * Each breed's hitbox, eye height and rider seat (blocks), measured by tools/horse_breeds.py from its model against
 * vanilla's horse: the width follows the body's length and the height its withers, as EntityType.HORSE's 1.3964844 x 1.6
 * follow vanilla's; the seat (the passenger attachment) is vanilla's 1.44375 moved by how much higher or lower the saddle
 * top sits than vanilla's 23.67 in-game px. Pure data: no Minecraft classes.
 */
public final class BreedDimensions {
	/** Width and height of the hitbox, the eye height and the rider attachment height, in blocks. */
	public record Size(float width, float height, float eyes, float seat) {
	}

	private BreedDimensions() {
	}

	public static Size of(String breed) {
		return switch (breed) {
""" + "\n".join(sizes) + """
			default -> new Size(1.3964844F, 1.6F, 1.52F, 1.44375F);
		};
	}
}
"""


def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--check", action="store_true", help="compare with the committed files; write nothing")
    a = ap.parse_args(argv)
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    files, dims = generate()
    stale = []
    for path, data in sorted(files.items()):
        current = path.read_bytes() if path.exists() else None
        if current != data:
            stale.append(path)
            if not a.check:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(data)
    for breed, d in dims.items():
        print(f"{breed}: hitbox {d['width']} x {d['height']}, eyes {d['eyes']}, seat {d['seat']}")
    if a.check:
        for p in stale:
            print("STALE", p.relative_to(ROOT))
        print(f"horse_breeds --check: {len(files)} files, {len(stale)} stale")
        return 1 if stale else 0
    print(f"horse_breeds: {len(files)} files, {len(stale)} written")
    return 0


if __name__ == "__main__":
    sys.exit(main())
