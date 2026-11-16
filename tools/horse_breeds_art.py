"""The horse breeds' saddlebags and spawn eggs (docs/features/horse-breeds.md), painted by script for horse_breeds.py.

Saddlebags (fitted with a chest, as a donkey's are): an editable Blockbench model per carrier, written to
art/horse_breeds/<art>/<art>_saddlebags.bbmodel with its texture embedded, on the horse's own `body` pivot so it moves
with the horse. Every cube has whole-pixel sizes (one texel a model pixel), touches or sinks a little into what carries
it (nothing floats), and no face lies in another's plane (no z-fighting). The right-hand pieces are specified once; the
left-hand ones are their mirror images and share their texture (mirror_uv), as vanilla's legs do.

- Shire: two deep bags behind the saddle, hanging on the haunches from a strap over the loins; black leather and brass
  like its saddle. Opens as three rows (27 slots).
- Giant Finnhorse: two smaller bags the same way, dark brown leather and steel like its saddle. One row (9 slots).
- Pegasus: a light pack rolled behind the cantle and strapped to it, between the folded wings, in the saddle's sky-blue
  canvas, brown leather and gold. One row (9 slots); drawn and opened only while the saddle is on.

Spawn eggs: 16x16 in the shape of the Pegasus egg (docs/ITEM_ICONS.md: a one-pixel outline in the egg's own dark, light
from the top left, flat tones), in each breed's coat with spots of its mane and markings.

Coordinates below are Blockbench space (y up; +x is the horse's right side, Minecraft's -x; -z towards the head), raw mesh
px, matching the breed .bbmodel files. Pieces offset from each other by 0.05-0.1 px where they would otherwise share a
plane.
"""
import base64
import io
import json
import uuid
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art/horse_breeds"
NS = uuid.UUID("6b1d2a4e-3f2c-5d6e-9a7b-1c2d3e4f5a6b")

# ------------------------------------------------------------------------------------------------ palettes
BLACK_LEATHER = {"light": (78, 70, 66), "base": (56, 50, 48), "shade": (46, 41, 40), "dark": (40, 36, 36), "deep": (26, 24, 26)}
BRASS = {"light": (226, 190, 100), "base": (190, 150, 66), "dark": (144, 108, 46)}
BROWN_LEATHER = {"light": (122, 84, 56), "base": (96, 64, 42), "shade": (84, 55, 37), "dark": (72, 46, 32), "deep": (52, 32, 24)}
STEEL = {"light": (214, 214, 206), "base": (168, 168, 162), "dark": (118, 118, 114)}
SKY_CANVAS = {"light": (150, 186, 226), "base": (118, 160, 212), "shade": (92, 132, 186), "dark": (64, 100, 156), "deep": (44, 74, 120)}
PEG_LEATHER = {"light": (146, 98, 62), "base": (118, 76, 46), "shade": (104, 66, 40), "dark": (88, 54, 32), "deep": (58, 34, 22)}
GOLD = {"light": (236, 204, 120), "base": (204, 162, 70), "dark": (150, 112, 44)}

# ------------------------------------------------------------------------------------------------ specifications
# (name, from, to, material, kind). Right-hand (Blockbench +x) pieces; "panel"/"pack" pieces span both sides and are
# not mirrored. kind chooses the painting: bag, lid, strap, buckle, panel, pack.
SHIRE = {
    "art": "shire", "origin": [0.0, 17.5, 5.0], "size": (64, 32), "leather": BLACK_LEATHER, "metal": BRASS,
    "pieces": [
        ("loin_strap", [-6.5, 27.0, 7.5], [6.5, 28.0, 9.5], "leather", "panel", False),
        ("drop", [5.6, 24.9, 7.6], [6.6, 27.9, 9.6], "leather", "strap", True),
        ("hanger", [5.7, 24.95, 7.5], [7.7, 25.95, 9.5], "leather", "strap", True),
        ("lid", [7.05, 24.0, 5.5], [11.05, 26.0, 12.5], "leather", "lid", True),
        ("bag", [7.1, 18.0, 6.0], [10.1, 25.0, 12.0], "leather", "bag", True),
        ("bag_strap", [9.85, 19.9, 8.5], [10.85, 24.9, 9.5], "leather", "strap", True),
        ("buckle", [9.95, 20.5, 8.0], [10.95, 21.5, 10.0], "metal", "buckle", True),
    ]}
GIANT = {
    "art": "finnhorse", "origin": [0.0, 18.0, 2.0], "size": (64, 32), "leather": BROWN_LEATHER, "metal": STEEL,
    "pieces": [
        ("loin_strap", [-9.5, 38.0, 10.5], [9.5, 39.0, 12.5], "leather", "panel", False),
        ("drop", [8.6, 34.9, 10.6], [9.6, 38.9, 12.6], "leather", "strap", True),
        ("hanger", [8.7, 34.95, 10.5], [11.7, 35.95, 12.5], "leather", "strap", True),
        ("lid", [11.05, 33.0, 8.5], [15.05, 35.0, 15.5], "leather", "lid", True),
        ("bag", [11.1, 27.0, 9.0], [14.1, 34.0, 15.0], "leather", "bag", True),
        ("bag_strap", [13.9, 28.9, 11.5], [14.9, 33.9, 12.5], "leather", "strap", True),
        ("buckle", [14.0, 29.5, 11.0], [15.0, 30.5, 13.0], "metal", "buckle", True),
    ]}
# The Pegasus: vanilla's body pivot (0, 13, 5) in Blockbench; the seat ends at z 5, the cantle at z 4..5.
PEGASUS = {
    "art": "pegasus", "origin": [0.0, 13.0, 5.0], "size": (32, 32), "leather": PEG_LEATHER, "metal": GOLD, "canvas": SKY_CANVAS,
    "pieces": [
        ("pack", [-3.0, 21.0, 6.6], [3.0, 24.0, 9.6], "canvas", "pack", False),
        ("pack_band", [-3.5, 20.6, 7.6], [3.5, 24.6, 8.6], "leather", "band", False),
        ("cantle_strap", [1.5, 21.0, 4.7], [2.5, 22.0, 6.7], "leather", "strap", True),
        ("buckle", [-0.5, 24.0, 7.35], [0.5, 25.0, 8.35], "metal", "buckle_flat", False),
    ]}
CARRIERS = {"shire_horse": SHIRE, "giant_finnhorse": GIANT, "pegasus": PEGASUS}


def mirrored(name, frm, to):
    return "left_" + name, [-to[0], frm[1], frm[2]], [-frm[0], to[1], to[2]]


def cubes_of(spec):
    """Every cube: (name, from, to, material, kind, mirror, uv key)."""
    out = []
    for name, frm, to, material, kind, sided in spec["pieces"]:
        if sided:
            out.append(("right_" + name, frm, to, material, kind, False, name))
            lname, lf, lt = mirrored(name, frm, to)
            out.append((lname, lf, lt, material, kind, True, name))
        else:
            out.append((name, frm, to, material, kind, False, name))
    return out


def size_of(frm, to):
    s = [round(to[i] - frm[i], 6) for i in range(3)]
    for v in s:
        if abs(v - round(v)) > 1e-6:
            raise SystemExit(f"cube size {s} is not whole pixels")
    return [int(round(v)) for v in s]


def pack_uv(spec):
    """Shelf-pack one box-UV net per distinct piece into the texture. -> {uv key: (u, v)}."""
    width, height = spec["size"]
    seen, items = set(), []
    for name, frm, to, material, kind, mirror, key in cubes_of(spec):
        if key in seen:
            continue
        seen.add(key)
        w, h, d = size_of(frm, to)
        items.append((key, 2 * (w + d), d + h))
    items.sort(key=lambda i: (-i[2], -i[1], i[0]))
    out, x, y, row = {}, 0, 0, 0
    for key, nw, nh in items:
        if x + nw > width:
            x, y, row = 0, y + row, 0
        if y + nh > height or nw > width:
            raise SystemExit(f"{spec['art']} saddlebags: no room for {key} in {width}x{height}")
        out[key] = (x, y)
        x += nw
        row = max(row, nh)
    return out


def box_faces(uv, size, mirror):
    """Blockbench box-UV face rectangles (the same convention as the shared meshlib.box_uv_faces)."""
    sx, sy, sz = size
    u, v = uv
    faces = {
        "east": [u, v + sz, u + sz, v + sz + sy],
        "west": [u + sz + sx, v + sz, u + 2 * sz + sx, v + sz + sy],
        "up": [u + sz + sx, v + sz, u + sz, v],
        "down": [u + sz + 2 * sx, v, u + sz + sx, v + sz],
        "south": [u + 2 * sz + sx, v + sz, u + 2 * sz + 2 * sx, v + sz + sy],
        "north": [u + sz, v + sz, u + sz + sx, v + sz + sy],
    }
    if mirror:
        faces = {k: [b[2], b[1], b[0], b[3]] for k, b in faces.items()}
        faces["east"], faces["west"] = faces["west"], faces["east"]
    return faces


# ------------------------------------------------------------------------------------------------ painting
def paint_face(img, rect, colours, kind, face, metal):
    """Fill one face's texels in planes: lit top, base body, a shaded lower plane, a dark bottom edge (side faces);
    up faces lit, down faces dark. Nothing random, no stripes."""
    u1, v1, u2, v2 = rect
    x0, x1 = min(u1, u2), max(u1, u2)
    y0, y1 = min(v1, v2), max(v1, v2)
    w, h = x1 - x0, y1 - y0
    if w <= 0 or h <= 0:
        return
    c = colours
    for ty in range(h):
        for tx in range(w):
            if face == "up":
                col = c["light"] if kind in ("buckle", "buckle_flat") else c["base"]
                if kind in ("lid", "pack") and ty in (0, h - 1):
                    col = c["light"]
            elif face == "down":
                col = c["dark"]
            else:
                if kind in ("buckle", "buckle_flat"):
                    col = c["light"] if ty == 0 else (c["dark"] if ty == h - 1 and h > 1 else c["base"])
                elif ty == 0:
                    col = c["light"]
                elif ty == h - 1 and h > 2:
                    col = c["deep"] if kind in ("bag", "lid") else c["dark"]
                elif kind == "bag" and ty >= h - 3:
                    col = c["shade"]
                elif kind == "lid" and h > 1:
                    col = c["dark"]
                elif kind == "pack" and ty >= h - 2:
                    col = c["shade"]
                else:
                    col = c["base"]
            img.putpixel((x0 + tx, y0 + ty), col + (255,))
    # The bag's face carries a stitched pocket seam one texel in from its edges (a single closed outline, not stripes).
    if kind == "bag" and face in ("east", "west", "north", "south") and w >= 4 and h >= 5:
        for tx in range(1, w - 1):
            img.putpixel((x0 + tx, y0 + 1), c["shade"] + (255,))
    if kind == "pack" and face in ("north", "south") and w >= 4:
        # The rolled canvas's end: a darker spiral turn across the roll's face.
        for tx in range(1, w - 1):
            img.putpixel((x0 + tx, y0 + h // 2), c["shade"] + (255,))


def texture(spec, cubes, uvs):
    width, height = spec["size"]
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    done = set()
    for name, frm, to, material, kind, mirror, key in cubes:
        if key in done:
            continue
        done.add(key)
        colours = spec[material]
        for face, rect in box_faces(uvs[key], size_of(frm, to), False).items():
            paint_face(img, rect, colours, kind, face, spec["metal"])
    return img


def png_bytes(img):
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=False)
    return buf.getvalue()


# ------------------------------------------------------------------------------------------------ bbmodel
def bbmodel(carrier, spec):
    cubes = cubes_of(spec)
    uvs = pack_uv(spec)
    img = texture(spec, cubes, uvs)
    width, height = spec["size"]
    name = f"{spec['art']}_saddlebags"
    elements, child_ids = [], []
    for cname, frm, to, material, kind, mirror, key in cubes:
        size = size_of(frm, to)
        uid = str(uuid.uuid5(NS, f"{name}/{cname}"))
        child_ids.append(uid)
        faces = {k: {"uv": r, "texture": 0} for k, r in box_faces(uvs[key], size, mirror).items()}
        elements.append({"name": cname, "box_uv": True, "rescale": False, "locked": False, "render_order": "default",
                         "allow_mirror_modeling": True, "from": [float(v) for v in frm], "to": [float(v) for v in to],
                         "autouv": 0, "color": 3, "inflate": 0.0, "origin": list(spec["origin"]),
                         "uv_offset": list(uvs[key]), "mirror_uv": mirror, "faces": faces, "type": "cube", "uuid": uid})
    group = lambda gname, children: {"name": gname, "origin": list(spec["origin"]), "color": 0, "nbt": "{}",
                                     "uuid": str(uuid.uuid5(NS, f"{name}/group/{gname}")), "export": True,
                                     "mirror_uv": False, "isOpen": True, "locked": False, "visibility": True,
                                     "autouv": 0, "rotation": [0.0, 0.0, 0.0], "children": children}
    source = "data:image/png;base64," + base64.b64encode(png_bytes(img)).decode("ascii")
    return {"meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True}, "name": name,
            "model_identifier": name, "modded_entity_version": "1.17", "modded_entity_flip_y": True,
            "visible_box": [1, 1, 0], "resolution": {"width": width, "height": height}, "elements": elements,
            "outliner": [group("body", [group("saddlebags", child_ids)])],
            "textures": [{"path": "", "name": f"{name}.png", "folder": "", "namespace": "", "id": "0", "width": width,
                          "height": height, "uv_width": width, "uv_height": height, "particle": False,
                          "use_as_default": False, "layers_enabled": False, "sync_to_project": "", "render_mode": "default",
                          "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
                          "frame_interpolate": False, "visible": True, "internal": True, "saved": True,
                          "uuid": str(uuid.uuid5(NS, f"{name}/texture")), "relative_path": "", "source": source}]}


def saddlebag_models():
    """{path: bbmodel} for the three carriers."""
    return {ART / spec["art"] / f"{spec['art']}_saddlebags.bbmodel": bbmodel(carrier, spec) for carrier, spec in CARRIERS.items()}


# ------------------------------------------------------------------------------------------------ spawn eggs
# The Pegasus egg's silhouette (textures/item/pegasus_spawn_egg.png): '#' outline, 'L' lit, 'B' body, 'S' shade.
EGG = [
    "................",
    "................",
    "......###.......",
    ".....#LBB#......",
    "....#LLBBB#.....",
    "....#LBBBBB#....",
    "...#BBBBBBBB#...",
    "...#BBBBBBBB#...",
    "...#BBBBBBBB#...",
    "...#BBBBBBBBS#..",
    "...#BBBBBBBSS#..",
    "...#BBBBBBSSS#..",
    "....#BBSSSSS#...",
    ".....#######....",
    "................",
    "................",
]
# Spots of each breed's mane/markings: (x, y, colour key). Coats from each breed's texture (art/horse_breeds/*).
EGGS = {
    "icelandic_horse": {"outline": (58, 34, 24), "light": (170, 112, 72), "body": (138, 86, 52), "shade": (106, 64, 40),
                        "spot": (232, 222, 200), "spot_dark": (196, 184, 160),
                        "spots": [(5, 6), (6, 6), (5, 7), (9, 8), (10, 8), (10, 9), (6, 11), (7, 11)]},
    "mongolian_horse": {"outline": (82, 62, 40), "light": (222, 196, 150), "body": (196, 166, 118), "shade": (160, 130, 88),
                        "spot": (70, 52, 38), "spot_dark": (52, 38, 28),
                        "spots": [(7, 3), (7, 4), (7, 5), (7, 6), (7, 7), (8, 8), (8, 9), (8, 10)]},
    "shire_horse": {"outline": (44, 24, 18), "light": (160, 92, 48), "body": (138, 74, 40), "shade": (114, 58, 34),
                    "spot": (230, 226, 216), "spot_dark": (184, 178, 170),
                    "spots": [(4, 9), (5, 9), (4, 10), (5, 10), (11, 9), (11, 10), (6, 4), (6, 5)]},
    "giant_finnhorse": {"outline": (92, 46, 26), "light": (206, 130, 68), "body": (182, 106, 54), "shade": (154, 84, 44),
                        "spot": (232, 214, 158), "spot_dark": (214, 190, 130),
                        "spots": [(6, 3), (7, 3), (6, 4), (7, 4), (8, 5), (5, 10), (6, 10), (10, 10)]},
    "friesian_horse": {"outline": (14, 14, 18), "light": (58, 60, 72), "body": (40, 42, 50), "shade": (30, 31, 38),
                       "spot": (82, 92, 118), "spot_dark": (62, 70, 92),
                       "spots": [(5, 5), (5, 6), (6, 7), (9, 9), (10, 9), (10, 10), (7, 11), (8, 11)]},
}


def egg_png(breed):
    e = EGGS[breed]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(EGG):
        for x, ch in enumerate(row):
            col = {"#": e["outline"], "L": e["light"], "B": e["body"], "S": e["shade"]}.get(ch)
            if col:
                img.putpixel((x, y), col + (255,))
    for x, y in e["spots"]:
        if EGG[y][x] in "LBS":
            img.putpixel((x, y), (e["spot_dark"] if EGG[y][x] == "S" else e["spot"]) + (255,))
    return png_bytes(img)
