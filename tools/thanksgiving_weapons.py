"""Thanksgiving weapons (Thanksgiving batch 2, worker A; docs/features/thanksgiving-weapons.md): colonial and woodland
weapons, added only where no equivalent exists.

The existing Bellmouth, Duelling Pistol and Line Musket cover the colonial guns. The new Naval Cutlass adds a boarding
niche; three distinct woodland hunting items reuse the shared Arms combat systems with their own stone/wood art,
recipes and numbers. Their designs are everyday hunting tools, not a claim of nation-specific ceremonial accuracy.

The cutlass is an arm (weapons/NavalCutlassItem.java, an ArmItem): its numbers are vanilla item components made by the
arms' own JugcraftArms.arm() from CUTLASS below (kept the same as weapons/NavalCutlassStats.java by
scripts/tests/test_thanksgiving_weapons.py), and it is swung with the sabre's motion clips. Its art:
- the GUI icon, a 16x16 map, tools/item_icons/naval_cutlass.txt, drawn by tools/item_icons.py in the owner's manner
  (docs/ITEM_ICONS.md);
- a 3D model for every other place (hands, the ground, item frames and shelves): boxes at one texel per model pixel,
  painted face by face (`build`), held as the sabre is (`display`).

Run: python3 tools/thanksgiving_weapons.py  (writes the files below; --check compares instead of writing).
"""
import argparse
import json
import math
import os
import sys
from pathlib import Path

from PIL import Image

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE))

import icon_materials  # noqa: E402
import item_icons  # noqa: E402
import model_writer  # noqa: E402
from arms_pixel import BRASS, LEATHER, WOOD  # noqa: E402

MOD = "jugcraft"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / MOD
DATA = ROOT / "src" / "main" / "resources" / "data" / MOD

# ---------------------------------------------------------------- the inventory (docs/features/thanksgiving-weapons.md)

# candidate -> (decision, the existing items, reason). New: built here. Covered: an existing item is the same weapon.
INVENTORY = {
    "blunderbuss": ("Covered", ["jugcraft:bellmouth"],
                    "The Bellmouth is a flintlock blunderbuss (the owner's own model and animations, tools/guns.py "
                    "source 'blunderbuss'): ten balls a shot from one Paper Cartridge, 12/9 degree spread, 20 blocks, "
                    "a 78-tick reload."),
    "flintlock pistol": ("Covered", ["jugcraft:duelling_pistol"],
                         "The Duelling Pistol is a flintlock pistol (the owner's model, source 'flintlock_pistol'): one "
                         "heavy ball, a 74-tick muzzle reload."),
    "musket": ("Covered", ["jugcraft:line_musket", "jugcraft:windlass_musket"],
               "The Line Musket is a flintlock musket (the owner's model, source 'musket'): one heavy ball, rammed home "
               "in 78 ticks; the Windlass Musket is its repeating cousin."),
    "naval cutlass": ("New", ["jugcraft:naval_cutlass"],
                      "No cutlass or naval sword exists: the arms' sabres (bronze, steel, gilded, Hessian, Moonfang) are "
                      "horseman's blades whose trait is 3 more damage from the saddle, and the pirate crews carry "
                      "golden swords. The cutlass is a short, broad sailor's blade for fighting aboard."),
    "stone-headed hatchet or tomahawk": ("New", ["jugcraft:stone_headed_hatchet"],
                                         "Codex's decision (14 November 2026): an inspired, everyday hunting tool, not "
                                         "covered by vanilla's stone axe or the arms' metal francisca: a knapped stone "
                                         "head lashed to a wooden haft, chopping as a stone axe and thrown as the "
                                         "francisca is."),
    "woodland hunting bow": ("New", ["jugcraft:woodland_hunting_bow"],
                             "Codex's decision (14 November 2026): an inspired, everyday hunting tool, not covered by "
                             "vanilla's bow: a short self bow of natural wood, a sinew string and a leather grip, drawn "
                             "on the arms' longbow path, quicker than a bow with slower arrows."),
    "hunting spear": ("New", ["jugcraft:hunting_spear"],
                      "Codex's decision (14 November 2026): an inspired, everyday hunting tool, not covered by vanilla's "
                      "stone spear or the arms' spears: a knapped stone point bound to a wooden shaft, jabbing with a "
                      "long reach and thrown as the javelin is."),
}

# ---------------------------------------------------------------- the Naval Cutlass

# Its numbers (weapons/NavalCutlassStats.java). damage is added to iron's 2 and the hand's 1, as a sword's 3 is, so a
# blow is 5 at 1.8 a second (9.0 a second; the iron sword's 6 at 1.6 is 9.6). reach in blocks (the hand's 3). parry:
# the share of a blow from in front that holding it up stops (the longsword's 0.6). boarding: what a blow adds while
# its wielder or its target is afloat (in water, or aboard a boat).
CUTLASS = {
    "id": "naval_cutlass",
    "kind": "cutlass",
    "display": "Naval Cutlass",
    "trait": "Boarding Blade",
    "tooltip": "A sailor's short, broad blade: quick cuts, and 2 more damage while you or your foe is in water or aboard "
               "a boat. Use to parry with its shell guard: blocks 40% of the damage from in front.",
    "damage": 2.0, "speed": -2.2, "swing": ("whack", 6), "reach": 2.75, "parry": 0.4, "boarding": 2.0,
    "material": "iron", "durability": 250,
    "recipe": (["  I", " I ", "BL "], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass", "L": "minecraft:leather"}),
    "feature": "machines",
    "tags": ["minecraft:swords"],
}
IRON_BONUS = 2.0       # vanilla ToolMaterial.IRON's attack damage bonus
IRON_SWORD = 6.0 * 1.6  # vanilla's iron sword: 6 a blow, 1.6 a second

# The 3D model's materials, from the icon palettes so the model and its icon match (tools/icon_materials.py).
IRON = icon_materials.IRON


def tones(material):
    return {"dark": material.dark, "mid": material.mid, "light": material.light, "high": material.highlight,
            "edge": material.outline_light, "deep": material.outline_dark}


STEEL_T, BRASS_T, LEATHER_T = tones(IRON), tones(BRASS), tones(LEATHER)


def blow(bonus=IRON_BONUS):
    return 1.0 + bonus + CUTLASS["damage"]


def per_second(bonus=IRON_BONUS):
    return blow(bonus) * (4.0 + CUTLASS["speed"])


# ---------------------------------------------------------------- the model's boxes (model pixels, upright)

# Built upright: the blade runs up +y, its edge to +x (the side the knuckle bow guards) and its back to -x, one pixel
# thick in z. Every box is axis-aligned, so its texels stay square; the gentle curve is one step back of a pixel between
# two long runs, and the back is clipped to the point. Parts overlap half a pixel where they join (the knuckle bow's foot meets the bow face to face), so
# no two faces drawn alike-facing share a plane and nothing floats.
# (name, material, from, to)
BOXES = [
    ("pommel", "brass", (6.5, -3.0, 6.5), (9.5, -1.0, 9.5)),
    ("grip", "grip", (7.0, -1.5, 7.0), (9.0, 4.0, 9.0)),
    # the shell: a bar across the blade's root and, below it, the dish that covers the hand, lower at its rims
    ("guard", "brass", (5.0, 4.0, 7.0), (12.0, 5.0, 9.0)),
    ("shell", "brass", (6.0, 3.5, 6.0), (10.0, 4.5, 10.0)),
    ("quillon", "brass", (3.5, 3.0, 7.5), (5.5, 4.5, 8.5)),
    ("bow_foot", "brass", (9.0, -2.5, 7.5), (10.5, -1.5, 8.5)),
    ("bow", "brass", (10.5, -2.0, 7.5), (11.5, 4.5, 8.5)),
    # the blade: four pixels broad, its line stepping a pixel towards the back once, at mid-length (a gentle curve, the
    # edge convex), the edge then running straight to the point; the back clipped, dropping a pixel a row to it
    ("forte", "blade", (7.0, 4.5, 7.5), (11.0, 12.0, 8.5)),
    ("upper", "blade", (6.0, 12.0, 7.5), (10.0, 18.0, 8.5)),
    ("clip", "blade", (7.0, 18.0, 7.5), (10.0, 19.0, 8.5)),
    ("clip_upper", "blade", (8.0, 19.0, 7.5), (10.0, 20.0, 8.5)),
    ("point", "blade", (9.0, 20.0, 7.5), (10.0, 21.0, 8.5)),
]
GRIP_CENTRE = (8.0, 1.5, 8.0)   # where the hand closes (the grip's middle)
TEXTURE = 32                    # the model texture, one texel a model pixel


def face_size(side, lo, hi):
    x, y, z = (hi[k] - lo[k] for k in range(3))
    return {"north": (x, y), "south": (x, y), "east": (z, y), "west": (z, y), "up": (x, z), "down": (x, z)}[side]


def paint(material, side, w, h, box):
    """The texels of one face, w x h, top row first: flat tones in broad bands (no noise, no pinstripes).

    The blade's flats are a lit band along the edge and a darker band along the back, its edge strip bright; brass is
    lit on top, its sides lit along their top row and shaded along their bottom one, dark underneath; the grip is dark
    leather, its lit side a step lighter."""
    px = [[None] * w for _ in range(h)]
    if material == "blade":
        t = STEEL_T
        # across the flat from the back to the edge: a dark back, the body and a lit edge band; the edge strip brightest
        ramp = {1: [t["light"]], 2: [t["mid"], t["light"]], 3: [t["dark"], t["mid"], t["light"]],
                4: [t["dark"], t["mid"], t["mid"], t["light"]]}
        for y in range(h):
            for x in range(w):
                if side in ("south", "north"):
                    # the edge is +x: the right of the south picture, the left of the north one
                    across = x if side == "south" else w - 1 - x
                    c = ramp[min(w, 4)][min(across, 3)]
                    if box == "point":
                        c = t["high"]
                elif side == "east":
                    c = t["high"]
                elif side == "west":
                    c = t["edge"]
                elif side == "up":
                    # a step's ledge: lit; the clip's ledges a step lighter, so the clipped back reads
                    c = t["light"] if box.startswith("clip") or box == "point" else t["mid"]
                else:
                    c = t["dark"]
                px[y][x] = c
    elif material == "brass":
        t = BRASS_T
        for y in range(h):
            for x in range(w):
                if side == "up":
                    c = t["light"]
                elif side == "down":
                    c = t["dark"]
                else:
                    c = t["light"] if (y == 0 and h > 2) else t["dark"] if (y == h - 1 and h > 2) else t["mid"]
                px[y][x] = c
    else:  # the grip: dark leather, its lit side a step lighter
        lt = LEATHER_T
        for y in range(h):
            for x in range(w):
                if side in ("up", "down"):
                    c = lt["mid"]
                else:
                    c = lt["light"] if (x == 0 and side in ("south", "west")) else lt["mid"] if x == 0 else lt["dark"]
                px[y][x] = c
    return px


class Atlas:
    """Faces packed into one TEXTURE x TEXTURE image in rows, tallest first, a texel apart; uv in 0..16."""

    def __init__(self):
        self.faces = []

    def add(self, key, w, h, texels):
        self.faces.append((key, w, h, texels))

    def pack(self):
        img = Image.new("RGBA", (TEXTURE, TEXTURE), (0, 0, 0, 0))
        uvs, x, y, row = {}, 0, 0, 0
        # identical pictures share their texels
        seen = {}
        for key, w, h, texels in sorted(self.faces, key=lambda f: (-f[2], -f[1], f[0])):
            pw, ph = int(math.ceil(w - 1e-6)), int(math.ceil(h - 1e-6))
            sig = json.dumps(texels)
            if sig in seen:
                ox, oy = seen[sig]
            else:
                if x + pw > TEXTURE:
                    x, y, row = 0, y + row + 1, 0
                if y + ph > TEXTURE:
                    raise ValueError(f"model texture full placing {key}")
                for ty in range(ph):
                    for tx in range(pw):
                        img.putpixel((x + tx, y + ty), tuple(texels[ty][tx]) + (255,))
                ox, oy = x, y
                seen[sig] = (x, y)
                x += pw + 1
                row = max(row, ph)
            unit = 16 / TEXTURE
            uvs[key] = [round(ox * unit, 4), round(oy * unit, 4), round((ox + w) * unit, 4), round((oy + h) * unit, 4)]
        return img, uvs


def build():
    """(elements, texture image) of the in-hand model: every face of every box painted for itself, one texel to a
    model pixel (a face 5.5 pixels tall reads 5.5 texels)."""
    atlas = Atlas()
    for name, material, lo, hi in BOXES:
        for side in ("north", "south", "east", "west", "up", "down"):
            w, h = face_size(side, lo, hi)
            pw, ph = int(math.ceil(w - 1e-6)), int(math.ceil(h - 1e-6))
            atlas.add(f"{name}:{side}", w, h, paint(material, side, pw, ph, name))
    img, uvs = atlas.pack()
    elements = []
    for name, material, lo, hi in BOXES:
        faces = {side: {"uv": uvs[f"{name}:{side}"], "texture": "#tex"}
                 for side in ("north", "south", "east", "west", "up", "down")}
        elements.append({"from": list(lo), "to": list(hi), "faces": faces})
    return elements, img


# ---------------------------------------------------------------- held as the sabre is

# The arms' sabre (tools/arms_*: models/item/arms_sabre.json): its display, and the frame its boxes are built in, upright
# about SABRE_GRIP and turned -45 degrees about z there to lie on the sprite's diagonal.
SABRE_DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0.0, 5.27, -1.09], "scale": [1.132] * 3},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0.0, 5.27, -1.09], "scale": [1.132] * 3},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 4.72, 0.53], "scale": [0.906] * 3},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 4.72, 0.53], "scale": [0.906] * 3},
}
SABRE_GRIP = (4.5094, 2.8251, 8.0)   # the sabre's rotation origin, the middle of its grip
SABRE_TURN = -45.0
SIZE = 0.69                          # the cutlass against the sabre: 24 model pixels held at 0.69 of the sabre's scale

# Where it is not in a hand: standing on the diagonal, as a dropped or framed sword's sprite does.
STILL_DISPLAY = {
    "ground": {"rotation": [0, 0, -45], "translation": [0, 2.5, 0], "scale": [0.42, 0.42, 0.42]},
    "fixed": {"rotation": [0, 180, -45], "translation": [0, 0, 0], "scale": [0.66, 0.66, 0.66]},
    "on_shelf": {"rotation": [0, 180, -45], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
    "head": {"rotation": [0, 180, -45], "translation": [0, 13, 7], "scale": [0.6, 0.6, 0.6]},
}


def _rot(axis, deg):
    a = math.radians(deg)
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        return [[1, 0, 0], [0, c, -s], [0, s, c]]
    if axis == "y":
        return [[c, 0, s], [0, 1, 0], [-s, 0, c]]
    return [[c, -s, 0], [s, c, 0], [0, 0, 1]]


def _mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def _apply(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


def _runtime(tr, left):
    """(translation in blocks, rotation matrix, uniform scale) as vanilla's ItemTransform.apply uses a display entry:
    in the left hand it turns the y and z rotations and the x translation round."""
    rx, ry, rz = tr["rotation"]
    tx, ty, tz = [v / 16.0 for v in tr["translation"]]
    if left:
        tx, ry, rz = -tx, -ry, -rz
    return [tx, ty, tz], _mul(_mul(_rot("x", rx), _rot("y", ry)), _rot("z", rz)), tr["scale"][0]


def display():
    """The cutlass's display: in each hand, where and how the sabre's grip is held, its own grip there, at SIZE of the
    sabre's scale; elsewhere STILL_DISPLAY. Solved, not tuned: for every model point p,
    T' + R' s' (p/16 - 1/2) = T + R s (q(p)/16 - 1/2), where q(p) is p moved onto the sabre's grip, scaled by SIZE and
    turned -45 degrees about z as the sabre's boxes are; so R' = R Rz(-45) and s' = s SIZE."""
    out = {}
    turn = _rot("z", SABRE_TURN)
    for context, tr in SABRE_DISPLAY.items():
        left = context.endswith("lefthand")
        T, R, s = _runtime(tr, left)
        # q(p) = G + turn (SIZE (p - g)), with the sabre's grip G and ours g; its constant part, p = 0:
        g, G = GRIP_CENTRE, SABRE_GRIP
        q0 = [G[i] + v for i, v in enumerate(_apply(turn, [-SIZE * g[k] for k in range(3)]))]
        Rp = _mul(R, turn)
        sp = s * SIZE
        lhs = _apply(R, [s * (q0[k] / 16.0 - 0.5) for k in range(3)])
        rhs = _apply(Rp, [sp * -0.5] * 3)
        t_runtime = [T[k] + lhs[k] - rhs[k] for k in range(3)]
        rx, ry, rz = tr["rotation"]
        rz_right = rz + SABRE_TURN if not left else rz - SABRE_TURN   # the JSON's left entry is the right's mirror
        translation = [(-t_runtime[0] if left else t_runtime[0]) * 16.0, t_runtime[1] * 16.0, t_runtime[2] * 16.0]
        out[context] = {"rotation": [rx, ry, round(rz_right, 4)], "translation": [round(v, 4) + 0.0 for v in translation],
                        "scale": [round(sp, 4)] * 3}
    out.update(STILL_DISPLAY)
    return out


# ---------------------------------------------------------------- the woodland hunting weapons

# Codex's decision (14 November 2026; docs/features/thanksgiving-weapons.md): three new items, labelled "inspired,
# everyday hunting tools". They are ordinary tools of the stone and wood tiers, not ceremonial or regalia pieces, and
# carry no invented iconography or tribal attribution. Their numbers are weapons/HuntingWeaponStats.java's; their items
# weapons/HuntingWeapons.java's. The hatchet and the spear are arms thrown as the francisca and the javelin are (their
# motion, flight and throw line); the bow is the arms' longbow path (ArmBowItem) at a bow's size.
HATCHET = {
    "id": "stone_headed_hatchet", "trait_key": "hatchet", "moves": "francisca", "display": "Stone-headed Hatchet",
    "trait": "Chop",
    "tooltip": "A light hatchet, its knapped stone head lashed to the haft: chops wood as a stone axe does, and can be "
               "thrown.",
    "damage": 3.0, "speed": -2.8, "swing": ("whack", 8), "reach": 2.75, "disable": 1.5,
    "thrown": {"wind": 6, "speed": 1.5, "gravity": 0.06, "damage": 4.0},
    "material": "stone", "durability": 131,
    "recipe": (["FF", "TS", " S"], {"F": "minecraft:flint", "T": "minecraft:string", "S": "minecraft:stick"}),
    "feature": "agriculture",
    "tags": ["minecraft:enchantable/melee_weapon", "minecraft:enchantable/durability"],
}
SPEAR = {
    "id": "hunting_spear", "trait_key": "hunting_spear", "moves": "javelin", "display": "Hunting Spear",
    "trait": "Hunter's Reach",
    "tooltip": "A stone-pointed spear bound to a wooden shaft: quick jabs with a long reach, and thrown far and straight.",
    "damage": 2.0, "speed": -2.6, "swing": ("stab", 7), "reach": 3.5, "margin": 0.125,
    "thrown": {"wind": 10, "speed": 2.4, "gravity": 0.035, "damage": 5.0},
    "material": "stone", "durability": 131,
    "recipe": (["  F", " ST", "S  "], {"F": "minecraft:flint", "T": "minecraft:string", "S": "minecraft:stick"}),
    "feature": "agriculture",
    "tags": ["minecraft:enchantable/melee_weapon", "minecraft:enchantable/durability"],
}
BOW = {
    "id": "woodland_hunting_bow", "trait_key": "hunting_bow", "display": "Woodland Hunting Bow", "trait": "Quick Draw",
    "tooltip": "A short wooden bow with a leather grip: quick to draw, its arrows a little slower than a bow's.",
    "stats": "Full draw in %s s. Arrows fly %s%% slower than a bow's.",
    "draw": 17, "speed": 2.4, "arrow_damage": 2.0, "durability": 256, "material": "wood",
    "recipe": ([" ST", "L T", " ST"], {"S": "minecraft:stick", "T": "minecraft:string", "L": "minecraft:leather"}),
    "feature": "agriculture",
    "tags": ["minecraft:enchantable/bow", "minecraft:enchantable/durability"],
}
HUNTING = (HATCHET, SPEAR, BOW)
# Vanilla's bow, as tools/arms.py VANILLA_BOW: full draw in 20 ticks, arrows at 3.0 blocks a tick with base damage 2.
VANILLA_BOW = {"draw": 20, "speed": 3.0, "damage": 2.0}
STONE_BONUS = 1.0   # vanilla ToolMaterial.STONE's attack damage bonus

FLINT_T, WOOD_T, SINEW_T = tones(icon_materials.FLINT), tones(WOOD), tones(icon_materials.HORN)

# The 3D in-hand models, built upright in model pixels about the grip (x across, y along the haft, z through), one
# texel a model pixel, every part an axis-aligned box. tools/arms.py's francisca and javelin models are built the same
# way about their grips and turned -45 degrees about z there to lie on the icon's diagonal; these are placed on the same
# grips and turned the same way, so the arms' hand poses hold them by their grips and ThrownArmRenderer flies them as it
# flies the arms. Parts overlap a quarter pixel where they join, and the lashings stand proud of what they bind.
# (name, material, (x0, y0, z0), (x1, y1, z1)): x and y from the grip; z in the model's own pixels.
HATCHET_BOXES = [
    ("haft", "wood", (-0.5, -3.0, 7.5), (0.5, 11.0, 8.5)),
    # the knapped head: a tang seated against the haft, a thick body and a thinner bit, taller than the body
    ("tang", "flint", (0.25, 7.5, 7.25), (2.0, 10.0, 8.75)),
    ("body", "flint", (2.0, 7.0, 7.25), (3.5, 10.5, 8.75)),
    ("bit", "flint", (3.5, 6.5, 7.5), (4.5, 11.0, 8.5)),
    # sinew lashing: below the head round the haft, across the tang and haft, and above the head round the haft
    ("lash_low", "sinew", (-0.75, 6.25, 7.25), (0.75, 7.0, 8.75)),
    ("lash_cross", "sinew", (-0.75, 8.0, 7.0), (2.25, 9.0, 9.0)),
    ("lash_high", "sinew", (-0.75, 10.25, 7.25), (0.75, 10.75, 8.75)),
]
SPEAR_BOXES = [
    ("shaft", "wood", (-0.5, -8.0, 7.5), (0.5, 10.5, 8.5)),
    # the leaf-shaped knapped point: its base seated on the shaft's end, widest a third of the way up, to a sharp tip
    ("point_base", "flint", (-0.75, 10.5, 7.5), (0.75, 12.5, 8.5)),
    ("point_mid", "flint", (-1.5, 12.5, 7.5), (1.5, 14.5, 8.5)),
    ("point_upper", "flint", (-1.0, 14.5, 7.5), (1.0, 16.0, 8.5)),
    ("point_tip", "flint", (-0.5, 16.0, 7.5), (0.5, 17.0, 8.5)),
    # sinew binding round the shaft's end and the point's base
    ("bind_low", "sinew", (-0.75, 9.0, 7.25), (0.75, 10.0, 8.75)),
    ("bind_high", "sinew", (-1.0, 10.25, 7.25), (1.0, 11.5, 8.75)),
]
HUNTING_MODELS = {"stone_headed_hatchet": ("francisca", HATCHET_BOXES), "hunting_spear": ("javelin", SPEAR_BOXES)}


def paint_hunting(material, side, w, h, box):
    """The texels of one face of a hunting weapon, w x h, top row first, in flat bands (no noise, no pinstripes).

    Flint: its flats knapped into two broad facets, lit towards the bit or tip and darker behind; lit on top, dark
    underneath; the bit's edge and the point's tip bright. Wood: a lit column on its lit side and a shaded one on the
    other. Sinew: a lit top row and a shaded bottom row round a pale band."""
    t = {"flint": FLINT_T, "wood": WOOD_T, "sinew": SINEW_T}[material]
    px = [[None] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            if material == "flint":
                if side == "up":
                    c = t["light"]
                elif side == "down":
                    c = t["dark"]
                elif side in ("south", "north"):
                    # two facets across the flat: lit on the left (south) / right (north) half, mid on the other
                    lit = x < (w + 1) // 2 if side == "south" else x >= w // 2
                    c = t["light"] if lit else t["mid"]
                    if box in ("bit", "point_tip"):
                        c = t["high"] if (side == "south" and x == w - 1) or (side == "north" and x == 0) else c
                elif side == "east":
                    c = t["high"] if box == "bit" else t["mid"]
                else:
                    c = t["dark"]
            elif material == "wood":
                if side == "up":
                    c = t["light"]
                elif side == "down":
                    c = t["dark"]
                elif side in ("south", "west"):
                    c = t["light"] if x == 0 and w > 1 else t["mid"]
                else:
                    c = t["dark"] if x == w - 1 and w > 1 else t["mid"]
            else:
                if side == "up":
                    c = t["light"]
                elif side == "down":
                    c = t["dark"]
                else:
                    c = t["light"] if (y == 0 and h > 1) else t["dark"] if (y == h - 1 and h > 2) else t["mid"]
            px[y][x] = c
    return px


def hunting_model(item):
    """(elements, texture image, display) of a thrown hunting weapon's 3D model: its boxes on the arms' grip for its
    kind, turned -45 degrees about z there, held with that kind's hand poses (tools/arms.py held_model)."""
    import arms
    kind, boxes = HUNTING_MODELS[item]
    held = arms.held_model(kind)
    origin = held["elements"][0]["rotation"]["origin"]
    atlas = Atlas()
    for name, material, lo, hi in boxes:
        for side in ("north", "south", "east", "west", "up", "down"):
            w, h = face_size(side, lo, hi)
            pw, ph = int(math.ceil(w - 1e-6)), int(math.ceil(h - 1e-6))
            atlas.add(f"{name}:{side}", w, h, paint_hunting(material, side, pw, ph, name))
    img, uvs = atlas.pack()
    elements = []
    for name, material, lo, hi in boxes:
        frm = [round(origin[0] + lo[0], 4), round(origin[1] + lo[1], 4), lo[2]]
        to = [round(origin[0] + hi[0], 4), round(origin[1] + hi[1], 4), hi[2]]
        elements.append({"from": frm, "to": to, "rotation": {"angle": -45.0, "axis": "z", "origin": list(origin)},
                         "faces": {side: {"uv": uvs[f"{name}:{side}"], "texture": "#tex"}
                                   for side in ("north", "south", "east", "west", "up", "down")}})
    display = {context: json.loads(json.dumps(pose)) for context, pose in held["display"].items()}
    display.update(_still_display(elements))
    return elements, img, display


def _still_display(elements):
    """Lying on the ground, in a frame, on a shelf and on a head: the turned model, centred, at most a block across."""
    pts = []
    for e in elements:
        r, o = math.radians(e["rotation"]["angle"]), e["rotation"]["origin"]
        for x in (e["from"][0], e["to"][0]):
            for y in (e["from"][1], e["to"][1]):
                dx, dy = x - o[0], y - o[1]
                pts.append((o[0] + dx * math.cos(r) - dy * math.sin(r), o[1] + dx * math.sin(r) + dy * math.cos(r)))
    xs, ys = [p[0] for p in pts], [p[1] for p in pts]
    cx, cy = (min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2
    span = max(max(xs) - min(xs), max(ys) - min(ys))
    fit = round(min(1.0, 15.0 / span), 3)

    def pose(rotation, scale, flip, lift=0.0):
        # translation (pixels) that brings the box's centre to the middle, after the scale (and the y half-turn)
        tx = -(cx - 8.0) * scale * (-1 if flip else 1)
        ty = -(cy - 8.0) * scale + lift
        return {"rotation": rotation, "translation": [round(tx, 3) + 0.0, round(ty, 3) + 0.0, 0.0], "scale": [scale] * 3}
    ground = round(0.5 * fit, 3)
    return {"ground": pose([0, 0, 0], ground, False, 2.0), "fixed": pose([0, 180, 0], fit, True),
            "on_shelf": pose([0, 180, 0], round(0.9 * fit, 3), True), "head": pose([0, 180, 0], fit, True, 13.0)}


def bow_definition():
    """The bow's item definition: at rest, or drawn back in three steps as vanilla's bow is, at the same shares of its
    own full draw (tools/arms_kit.py BOW_STEPS)."""
    import arms_kit
    b = BOW["id"]
    draw = BOW["draw"] / 20

    def model(name):
        return {"type": "minecraft:model", "model": f"{MOD}:item/{name}"}
    return {"model": {"type": "minecraft:condition", "property": "minecraft:using_item", "on_false": model(b),
                      "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 0.05,
                                  "fallback": model(f"{b}_pulling_0"),
                                  "entries": [{"threshold": round(share * draw, 4), "model": model(f"{b}_pulling_{step}")}
                                              for step, share in arms_kit.BOW_STEPS]}},
            "swap_animation_scale": 1.0}


BOW_SPRITES = ["", "_pulling_0", "_pulling_1", "_pulling_2"]


# ---------------------------------------------------------------- the generation pipeline

# Every file here is made by Jugcraft's ordinary generators, as the arms' are:
# - tools/generate_material_data.py calls write_all (in assets(), after the arms): the JSON (models, item definitions,
#   recipes) through its writer, which lists each in tools/data/generated-material-resources.json, and the lang entries
#   into its table for en_us.json;
# - tools/gear.py merges item_tags() into the shared vanilla tag files it alone writes (minecraft:swords);
# - tools/generate_textures.py calls draw_all (after the arms): the PNG textures, which no manifest lists.
# Run alone (main, below), this module writes or checks its own JSON and PNG files; the lang, tags and manifest are the
# pipeline's, and --check also confirms that the committed copies carry this module's entries and that the three hooks
# are in place (hook_problems).

RES = ROOT / "src" / "main" / "resources"
MANIFEST = HERE / "data" / "generated-material-resources.json"
# The pipeline files that call this module, and the call each makes.
HOOKS = {"tools/generate_material_data.py": "thanksgiving_weapons.write_all(write, ASSETS, DATA / MOD, lang, condition)",
         "tools/gear.py": "thanksgiving_weapons.item_tags().items()",
         "tools/generate_textures.py": "thanksgiving_weapons.draw_all(save)"}


def _condition(feature):
    """generate_material_data.condition's load condition, for a run of this module alone: the recipe loads with its
    feature switch (agriculture_data.condition)."""
    return [{"condition": f"{MOD}:feature_enabled", "feature": feature}]


def textures():
    """{(kind, name): Image}: every texture, saved at textures/<kind>/<name>.png."""
    c = CUTLASS
    _elements, img = build()
    out = {("item", c["id"]): item_icons.draw(c["id"]), ("item", f"{c['id']}_model"): img}
    for item in HUNTING_MODELS:
        out[("item", item)] = item_icons.draw(item)
        out[("item", f"{item}_model")] = hunting_model(item)[1]
    for suffix in BOW_SPRITES:
        out[("item", BOW["id"] + suffix)] = item_icons.draw(BOW["id"] + suffix)
    return out


def json_files(condition=_condition):
    """{path relative to src/main/resources: dict}: every JSON file (models, item definitions, recipes)."""
    c = CUTLASS
    elements, _img = build()
    item = f"assets/{MOD}/items/{c['id']}.json"
    models = f"assets/{MOD}/models/item"
    pattern, key = c["recipe"]
    out = {
        f"{models}/{c['id']}.json": {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/{c['id']}"}},
        f"{models}/{c['id']}_in_hand.json": {
            "textures": {"tex": f"{MOD}:item/{c['id']}_model", "particle": f"{MOD}:item/{c['id']}_model"},
            "elements": elements, "display": display()},
        item: {
            "model": {"type": "minecraft:select", "property": "minecraft:display_context",
                      "cases": [{"when": ["gui"], "model": {"type": "minecraft:model", "model": f"{MOD}:item/{c['id']}"}}],
                      "fallback": {"type": "minecraft:model", "model": f"{MOD}:item/{c['id']}_in_hand"}},
            "swap_animation_scale": 1.2},
        f"data/{MOD}/recipe/{c['id']}.json": {
            "fabric:load_conditions": condition(c["feature"]), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{c['id']}", "count": 1}},
    }
    for item in HUNTING_MODELS:
        elements, _img, poses = hunting_model(item)
        out[f"{models}/{item}.json"] = {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{MOD}:item/{item}"}}
        out[f"{models}/{item}_in_hand.json"] = {
            "textures": {"tex": f"{MOD}:item/{item}_model", "particle": f"{MOD}:item/{item}_model"},
            "elements": elements, "display": poses}
        out[f"assets/{MOD}/items/{item}.json"] = {
            "model": {"type": "minecraft:select", "property": "minecraft:display_context",
                      "cases": [{"when": ["gui"], "model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}}],
                      "fallback": {"type": "minecraft:model", "model": f"{MOD}:item/{item}_in_hand"}},
            "swap_animation_scale": 1.0}
    for suffix in BOW_SPRITES:
        name = BOW["id"] + suffix
        out[f"{models}/{name}.json"] = {"parent": "minecraft:item/bow", "textures": {"layer0": f"{MOD}:item/{name}"}}
    out[f"assets/{MOD}/items/{BOW['id']}.json"] = bow_definition()
    for weapon in HUNTING:
        pattern, key = weapon["recipe"]
        out[f"data/{MOD}/recipe/{weapon['id']}.json"] = {
            "fabric:load_conditions": condition(weapon["feature"]), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{weapon['id']}", "count": 1}}
    return out


def lang():
    """The lang entries: names, tooltips and trait names (en_us.json, written by the pipeline from write_all)."""
    c = CUTLASS
    out = {f"item.{MOD}.{c['id']}": c["display"],
           f"tooltip.{MOD}.arms.{c['kind']}": c["tooltip"],
           f"tooltip.{MOD}.arms.{c['kind']}.trait": c["trait"]}
    for weapon in HUNTING:
        out[f"item.{MOD}.{weapon['id']}"] = weapon["display"]
        out[f"tooltip.{MOD}.arms.{weapon['trait_key']}"] = weapon["tooltip"]
        out[f"tooltip.{MOD}.arms.{weapon['trait_key']}.trait"] = weapon["trait"]
    out[f"tooltip.{MOD}.arms.{BOW['trait_key']}.stats"] = BOW["stats"]
    return out


def items():
    """The four registered items; reused colonial guns belong to tools/guns.py."""
    return [weapon['id'] for weapon in (CUTLASS, HATCHET, SPEAR, BOW)]


def metal_content(item):
    """Track the cutlass's two iron ingots and brass guard in nugget units."""
    return {'iron': 18, 'brass': 9} if item == CUTLASS['id'] else {}


def feature_of(item):
    return next(weapon['feature'] for weapon in (CUTLASS, *HUNTING) if weapon['id'] == item)


def item_tags():
    """{vanilla item tag, as tools/gear.py names it ("swords"): [this module's items in it]}."""
    tags = {}
    for weapon in (CUTLASS, *HUNTING):
        for tag in weapon["tags"]:
            tags.setdefault(tag.removeprefix("minecraft:"), []).append(f"{MOD}:{weapon['id']}")
    return tags


def write_all(write, assets, data, lang_table, condition):
    """tools/generate_material_data.py's hook: every JSON file through its `write` (which lists it in the manifest and
    finishes model elements), and the lang entries into its table. `assets` is assets/jugcraft, `data` data/jugcraft."""
    res = assets.parent.parent
    for rel, obj in json_files(condition).items():
        write(res / rel, obj)
    lang_table.update(lang())


def draw_all(save):
    """tools/generate_textures.py's hook: every texture through its `save(image, kind, name)`."""
    for (kind, name), img in textures().items():
        save(img, kind, name)


# ---------------------------------------------------------------- run alone

def _json_text(obj):
    """A JSON file exactly as the pipeline's writer writes it: a model's elements through model_writer.finish_elements
    (coplanar faces pulled apart, UVs fitted), two-space indent, a final newline."""
    obj = json.loads(json.dumps(obj))
    if obj.get("elements"):
        model_writer.finish_elements(obj["elements"])
    return json.dumps(obj, indent=2) + "\n"


def files():
    """{path relative to ROOT: str (JSON text) or Image (PNG)}: every file this module makes."""
    out = {f"src/main/resources/{rel}": _json_text(obj) for rel, obj in json_files().items()}
    for (kind, name), img in textures().items():
        out[f"src/main/resources/assets/{MOD}/textures/{kind}/{name}.png"] = img
    return out


def hook_problems():
    """What the shared files lack: this module's lang entries in en_us.json, its items in their vanilla tags, its JSON
    paths in the generated-resource manifest, and the three hook calls."""
    problems = []
    table = json.loads((RES / "assets" / MOD / "lang" / "en_us.json").read_text(encoding="utf-8"))
    problems += [f"en_us.json lacks {key}" for key, value in lang().items() if table.get(key) != value]
    for tag, items in item_tags().items():
        values = json.loads((RES / "data" / "minecraft" / "tags" / "item" / f"{tag}.json").read_text(encoding="utf-8"))["values"]
        problems += [f"minecraft:{tag} lacks {item}" for item in items if item not in values]
    listed = set(json.loads(MANIFEST.read_text(encoding="utf-8")))
    problems += [f"the manifest lacks {rel}" for rel in json_files() if rel not in listed]
    for rel, call in HOOKS.items():
        if call not in (ROOT / rel).read_text(encoding="utf-8"):
            problems.append(f"{rel} lacks the call {call}")
    return problems


def generate(check=False):
    """Writes (or, with check, compares) every file this module makes; returns the paths that differ."""
    differ = []
    for rel, obj in files().items():
        path = ROOT / rel
        if isinstance(obj, Image.Image):
            if check:
                old = Image.open(path).convert("RGBA") if path.exists() else None
                if old is None or old.size != obj.size or old.tobytes() != obj.convert("RGBA").tobytes():
                    differ.append(rel)
            else:
                # as tools/generate_textures.py's save writes a texture
                path.parent.mkdir(parents=True, exist_ok=True)
                obj.save(path, optimize=True)
        elif check:
            if not path.exists() or path.read_text(encoding="utf-8") != obj:
                differ.append(rel)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(obj, encoding="utf-8")
    return differ


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--check", action="store_true",
                        help="compare the committed files, and check the shared files and hooks, instead of writing")
    args = parser.parse_args(argv)
    differ = generate(check=args.check)
    if args.check:
        problems = hook_problems()
        for rel in differ:
            print(f"differs: {rel}")
        for problem in problems:
            print(f"hook: {problem}")
        print("PASS" if not differ and not problems else f"FAIL: {len(differ)} file(s) differ, {len(problems)} hook problem(s)")
        return 1 if differ or problems else 0
    print(f"wrote {len(files())} file(s); the lang, tags and manifest are written by tools/generate_material_data.py")
    return 0


if __name__ == "__main__":
    sys.exit(main())
