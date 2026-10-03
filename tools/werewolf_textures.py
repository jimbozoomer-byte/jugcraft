"""Original textures for full-moon werewolves (fall addition 23) (requires Pillow). For each kind of werewolf (brown, snow
and shadow) its fur (128 by 128, painted box by box and face by face from tools/werewolf_model.py: shaggy streaked fur,
lighter on top and darker beneath, a paler belly and muzzle, a black nose, a red mouth with ivory teeth and fangs, eyes in
dark sockets under a heavy brow, inner ears, ivory claws and dark pads), its glowing eyes alone, its rug (the pelt and its
head) and its pelt as an item; wolfsbane (a hooded violet-blue raceme over palmate leaves); the silver dagger and the
silver arrow as items.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from a fixed seed; no Mojang texture, nor
any other texture, is read, traced or recoloured.
"""
import random

from PIL import Image

from agriculture import WEREWOLF, WOLFSBANE
from crop_textures import rgb
from werewolf_model import PARTS, TEXTURE_SIZE

# Each kind: fur from dark to light, belly from dark to light, inner ears, eyes (the glowing iris, its bright point, the
# dark socket), and the nose.
KINDS = {
    "brown": {"fur": [rgb("24170f"), rgb("362315"), rgb("4a301d"), rgb("5f3f27"), rgb("775235")],
              "belly": [rgb("5e4230"), rgb("72533c"), rgb("866548")], "ear": rgb("3a2219"),
              "eye": rgb("d01414"), "glint": rgb("ff6a48"), "socket": rgb("120a06"), "nose": rgb("0e0a08")},
    "snow": {"fur": [rgb("8e959c"), rgb("aab0b6"), rgb("c4c9ce"), rgb("dcdfe2"), rgb("f2f3f4")],
             "belly": [rgb("dfe2e5"), rgb("eceef0"), rgb("f8f9fa")], "ear": rgb("b89a9c"),
             "eye": rgb("d01818"), "glint": rgb("ff7058"), "socket": rgb("3c3639"), "nose": rgb("161416")},
    "shadow": {"fur": [rgb("0c0c0e"), rgb("16161a"), rgb("212126"), rgb("2e2e34"), rgb("3e3e46")],
               "belly": [rgb("26262b"), rgb("303036"), rgb("3c3c43")], "ear": rgb("2a1e1e"),
               "eye": rgb("f0a818"), "glint": rgb("ffe27a"), "socket": rgb("040404"), "nose": rgb("060606")},
}
# Each box the texture paints, by its role, as (u, v, width, height, depth) from tools/werewolf_model.py.
_ROLES = {"chest": ("body", 1), "waist": ("body", 0), "hump": ("body", 2), "ruff": ("body", 3), "skull": ("head", 0),
          "muzzle": ("head", 1), "cheek": ("head", 2), "fang": ("head", 4), "jaw": ("jaw", 0), "ear": ("right_ear", 0), "ear_tip": ("right_ear", 1),
          "shoulder": ("right_arm", 0), "upper_arm": ("right_arm", 1), "forearm": ("right_forearm", 0), "hand": ("right_hand", 0),
          "claw": ("right_hand", 1), "thigh": ("right_leg", 0), "shin": ("right_shin", 0), "hock": ("right_hock", 0),
          "paw": ("right_paw", 0), "tail": ("tail", 0), "tail_mid": ("tail_mid", 0), "tail_tip": ("tail_tip", 0)}
BOXES = {}
for _role, (_part, _index) in _ROLES.items():
    _u, _v, _x, _y, _z, _w, _h, _d, _m = next(p for p in PARTS if p["name"] == _part)["boxes"][_index]
    BOXES[_role] = (_u, _v, _w, _h, _d)
MOUTH = [rgb("4a0c0c"), rgb("6e1414"), rgb("962222"), rgb("b23a3a")]
TOOTH = [rgb("bdb49c"), rgb("e2d9c0"), rgb("f4eedc")]
CLAW = [rgb("8a8068"), rgb("d8ceb2"), rgb("efe8d4")]
PAD = rgb("1a1412")
SILVER = [rgb("6e7480"), rgb("9aa2ae"), rgb("c4ccd6"), rgb("eef2f6")]
LEATHER = [rgb("3e2414"), rgb("5a361e")]
SHAFT = [rgb("7a5a32"), rgb("9a7444")]
FLETCH = [rgb("d8d8d8"), rgb("b0b0b0")]
STEM = [rgb("2f5a24"), rgb("3e7230"), rgb("50883c")]
HOOD = [rgb("2c2470"), rgb("3e34a0"), rgb("5a4cc8"), rgb("7e70e0")]


def faces(u, v, w, h, d):
    """A box's faces in its unwrapped texture, as (x, y, width, height). On top and bottom the last row is the front
    edge; on the right-hand side (x at the box's start) the last column is the front, on the left-hand side the first."""
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def put(img, x, y, colour):
    img.putpixel((x, y), colour + (255,))


def fur(img, rng, rect, palette, level=2, shaggy=0.2, ragged=False):
    """Streaky fur over a face: each column wanders about `level`, darker at the edges, with now and then a pale guard
    hair running down. Ragged: the bottom row is broken, as long fur ends."""
    x0, y0, w, h = rect
    top = len(palette) - 1
    for x in range(w):
        tone = level + rng.choice((-1, 0, 0, 1))
        for y in range(h):
            if rng.random() < shaggy:
                tone += rng.choice((-1, 1))
                tone = max(level - 1, min(level + 1, tone))
            t = tone - (1 if x in (0, w - 1) and w > 2 else 0)
            if rng.random() < 0.06:
                t += 1
            if ragged and y == h - 1 and rng.random() < 0.5:
                t -= 1
            put(img, x0 + x, y0 + y, palette[max(0, min(top, t))])


def fill(img, rect, colour):
    x0, y0, w, h = rect
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            put(img, x, y, colour)


def box_fur(img, rng, box, pal, level=2, ragged=False):
    """Fur on every face of a box: lighter on top, darker beneath."""
    for name, rect in faces(*box).items():
        shift = {"top": 1, "bottom": -1}.get(name, 0)
        fur(img, rng, rect, pal["fur"], level=level + shift, ragged=ragged and name not in ("top", "bottom"))


def werewolf(kind, eyes_only=False):
    """The fur of one kind of werewolf, or (eyes_only) just its eyes, for the glowing layer."""
    pal = KINDS[kind]
    width, height = TEXTURE_SIZE
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    rng = random.Random({"brown": 23101, "snow": 23102, "shadow": 23103}[kind])
    belly, furp = pal["belly"], pal["fur"]

    def b(role):
        return BOXES[role]

    # Body: the chest's front a paler blaze, the waist darker, shaggy hump and ruff.
    box_fur(img, rng, b('chest'), pal)
    x0, y0, w, h = faces(*b('chest'))["front"]
    for y in range(h):
        for x in range(w):
            if abs(x - (w - 1) / 2.0) <= 4.5 - y * 0.25:
                put(img, x0 + x, y0 + y, belly[min(2, max(0, 1 + rng.choice((-1, 0, 0, 1))))])
    box_fur(img, rng, b('waist'), pal, level=1)
    fur(img, rng, faces(*b('waist'))["front"], belly, level=1)
    box_fur(img, rng, b('hump'), pal, level=2, ragged=True)
    fur(img, rng, faces(*b('hump'))["top"], furp, level=3, shaggy=0.35)
    box_fur(img, rng, b('ruff'), pal, level=3, ragged=True)
    fur(img, rng, faces(*b('ruff'))["front"], belly, level=1, shaggy=0.3, ragged=True)

    # The skull: forehead, a heavy brow, eyes deep in dark sockets.
    box_fur(img, rng, b('skull'), pal)
    x0, y0, w, h = faces(*b('skull'))["front"]
    # The brow on the second row, the eyes under it on the third, both above where the muzzle meets the skull.
    middle = (w // 2 - (1 - w % 2), w // 2) if w % 2 == 0 else (w // 2,)
    for x in range(w):
        put(img, x0 + x, y0 + 1, furp[0] if x not in middle else furp[2])
    for ex, inner in ((1, 2), (w - 2, w - 3)):
        put(img, x0 + ex - (1 if ex == 1 else -1), y0 + 2, pal["socket"])
        put(img, x0 + ex, y0 + 2, pal["eye"])
        put(img, x0 + inner, y0 + 2, pal["glint"])
        put(img, x0 + ex, y0 + 3, pal["socket"])
        put(img, x0 + inner, y0 + 3, pal["socket"])
        eyes.putpixel((x0 + inner, y0 + 2), pal["glint"] + (255,))
        eyes.putpixel((x0 + ex, y0 + 2), pal["eye"] + (255,))
    for y in range(2, h):
        for x in middle:
            put(img, x0 + x, y0 + y, belly[1] if y > 3 else furp[3])

    # The muzzle: a paler bridge, a broad black nose, a dark lip; the roof of the open mouth red, edged with teeth.
    m = faces(*b('muzzle'))
    box_fur(img, rng, b('muzzle'), pal, level=2)
    fur(img, rng, m["top"], belly if kind != "shadow" else furp, level=1)
    x0, y0, w, h = m["front"]
    for x in range(1, w - 1):
        put(img, x0 + x, y0, pal["nose"])
    for x in range(2, w - 2):
        put(img, x0 + x, y0 + 1, pal["nose"])
    put(img, x0 + 1, y0, rgb("4a4a4e") if kind == "snow" else furp[1])
    for x in range(w):
        put(img, x0 + x, y0 + 2, MOUTH[0])
    x0, y0, w, h = m["bottom"]
    for y in range(h):
        for x in range(w):
            edge = x in (0, w - 1) or y == h - 1
            put(img, x0 + x, y0 + y, TOOTH[1 + (x + y) % 2] if edge and (x + y) % 2 == 0 else MOUTH[1 + (y % 2)])
    for side in ("right", "left"):
        x0, y0, w, h = m[side]
        for x in range(w):
            put(img, x0 + x, y0 + h - 1, TOOTH[1] if x % 2 == 0 else MOUTH[0])

    # The jaw: inside, a tongue between rows of teeth; in front, teeth over a furred chin.
    j = faces(*b('jaw'))
    box_fur(img, rng, b('jaw'), pal, level=1)
    x0, y0, w, h = j["top"]
    for y in range(h):
        for x in range(w):
            edge = x in (0, w - 1) or y == h - 1
            colour = TOOTH[1 + (x + y) % 2] if edge and (x + y) % 2 == 0 else MOUTH[3] if 0 < x < w - 1 and y < h - 1 else MOUTH[1]
            put(img, x0 + x, y0 + y, colour)
    x0, y0, w, h = j["front"]
    for x in range(w):
        put(img, x0 + x, y0, TOOTH[2] if x % 2 == 0 else TOOTH[0])
    for side in ("right", "left"):
        x0, y0, w, h = j[side]
        for x in range(w):
            put(img, x0 + x, y0, TOOTH[1] if x % 2 == 1 else MOUTH[1])

    # Fangs and claws: ivory, darker at the root.
    for key, palette in (("fang", TOOTH), ("claw", CLAW)):
        for name, (x0, y0, w, h) in faces(*b(key)).items():
            for y in range(h):
                for x in range(w):
                    tone = 0 if name == "top" or (name != "bottom" and y == 0) else 1 if y < h - 1 else 2
                    put(img, x0 + x, y0 + y, palette[tone])

    # Cheek tufts and ears (inner ear on the front, a dark tip).
    box_fur(img, rng, b('cheek'), pal, level=3, ragged=True)
    box_fur(img, rng, b('ear'), pal, level=2)
    x0, y0, w, h = faces(*b('ear'))["front"]
    for y in range(1, h):
        for x in range(w):
            put(img, x0 + x, y0 + y, pal["ear"] if y > 1 or x == 0 else furp[1])
    fill(img, faces(*b('ear'))["top"], furp[0])
    box_fur(img, rng, b('ear_tip'), pal, level=1)

    # Arms: shoulders and upper arms shaggy; forearms darker at the wrist; hands with dark palms.
    box_fur(img, rng, b('shoulder'), pal, level=2, ragged=True)
    box_fur(img, rng, b('upper_arm'), pal, level=2)
    box_fur(img, rng, b('forearm'), pal, level=2)
    for name in ("front", "left", "right", "back"):
        x0, y0, w, h = faces(*b('forearm'))[name]
        for x in range(w):
            put(img, x0 + x, y0 + h - 1, furp[0])
    box_fur(img, rng, b('hand'), pal, level=1)
    fill(img, faces(*b('hand'))["bottom"], PAD)

    # Legs: the thigh's front paler; shins and hocks with a dark heel; paws with toes, pads and ivory claws.
    box_fur(img, rng, b('thigh'), pal, level=2)
    fur(img, rng, faces(*b('thigh'))["front"], furp, level=3)
    box_fur(img, rng, b('shin'), pal, level=2)
    box_fur(img, rng, b('hock'), pal, level=1)
    fill(img, faces(*b('hock'))["back"], furp[0])
    p = faces(*b('paw'))
    box_fur(img, rng, b('paw'), pal, level=1)
    fill(img, p["bottom"], PAD)
    x0, y0, w, h = p["top"]
    for y in range(h):
        put(img, x0 + 1, y0 + y, furp[0])
        if w > 3:
            put(img, x0 + 3, y0 + y, furp[0])
    x0, y0, w, h = p["front"]
    for x in range(w):
        put(img, x0 + x, y0 + h - 1, CLAW[1] if x % 2 == 0 else PAD)

    # The tail: shaggy, a paler tip (darker on a shadow werewolf).
    box_fur(img, rng, b('tail'), pal, level=2)
    box_fur(img, rng, b('tail_mid'), pal, level=2, ragged=True)
    box_fur(img, rng, b('tail_tip'), pal, level=3 if kind != "shadow" else 1, ragged=True)
    return eyes if eyes_only else img


def wolfsbane():
    """A tall stem, palmate leaves low down, and a spike of hooded violet-blue flowers above."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(4, 16):
        img.putpixel((8, y), STEM[1] + (255,))
    for side in (-1, 1):
        for finger in range(3):
            for k in range(1, 5 - finger):
                x = 8 + side * k
                y = 13 - finger - (k // 2) * (1 if finger else 0)
                if 0 <= x < 16 and 0 <= y < 16:
                    img.putpixel((x, y), STEM[(finger + k) % 3] + (255,))
    flowers = [(6, 10, 2), (10, 8, 2), (6, 6, 2), (10, 4, 1), (7, 2, 1), (9, 1, 1)]
    for fx, fy, size in flowers:
        for dy in range(-size, size + 1):
            for dx in range(-size, size + 1):
                if abs(dx) + abs(dy) <= size + 1:
                    x, y = fx + dx, fy + dy
                    if 0 <= x < 16 and 0 <= y < 16:
                        tone = 3 if dy < 0 else 2 if dy == 0 else 1
                        img.putpixel((x, y), HOOD[tone - (1 if abs(dx) == size else 0)] + (255,))
        if 0 <= fy + 1 < 16:
            img.putpixel((fx, fy + 1), HOOD[0] + (255,))
    img.putpixel((8, 0), HOOD[2] + (255,))
    return img


def plain_fur(width, height, seed, palette, level=2):
    img = Image.new("RGBA", (width, height))
    fur(img, random.Random(seed), (0, 0, width, height), palette, level=level)
    return img


def rug(kind):
    """The pelt seen from above: the kind's fur, a darker stripe down the spine."""
    pal = KINDS[kind]["fur"]
    img = plain_fur(16, 16, 23003 + len(kind), pal, level=3)
    for y in range(16):
        for x in (7, 8):
            img.putpixel((x, y), pal[1] + (255,))
    return img


def rug_head(kind):
    """The head's fur, with its eyes, a black nose and teeth where the face is."""
    pal = KINDS[kind]
    img = plain_fur(16, 16, 23004 + len(kind), pal["fur"], level=2)
    for x in (6, 9):
        img.putpixel((x, 12), pal["eye"] + (255,))
    for x in range(7, 9):
        img.putpixel((x, 14), pal["nose"] + (255,))
    for x in range(5, 11, 2):
        img.putpixel((x, 15), TOOTH[2] + (255,))
    return img


def pelt(kind):
    """A folded pelt of the kind's fur, a paler edge showing."""
    pal = KINDS[kind]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    fur_img = plain_fur(16, 16, 23005 + len(kind), pal["fur"], level=2)
    for y in range(3, 14):
        for x in range(2 + (y % 2), 14 - (y % 3 == 0)):
            img.putpixel((x, y), fur_img.getpixel((x, y)))
    for x in range(3, 13):
        img.putpixel((x, 13), pal["belly"][1] + (255,))
    return img


def dagger():
    """A silver dagger held point up to the right: a leaf blade, a cross-guard, a leather grip and a round pommel."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(7, 15):
        x, y = i, 15 - i
        img.putpixel((x, y), SILVER[2] + (255,))
        img.putpixel((x - 1, y), SILVER[1] + (255,))
        if i < 13:
            img.putpixel((x, y + 1), SILVER[0] + (255,))
    img.putpixel((15, 0), SILVER[3] + (255,))
    img.putpixel((14, 1), SILVER[3] + (255,))
    for x, y in ((4, 9), (5, 8), (6, 9), (7, 10), (8, 11), (5, 10), (6, 11)):
        img.putpixel((x, y), SILVER[1] + (255,))
    for x, y in ((4, 11), (3, 12), (5, 12), (4, 13)):
        img.putpixel((x, y), LEATHER[(x + y) % 2] + (255,))
    for x, y in ((2, 13), (1, 14), (2, 14), (1, 13)):
        img.putpixel((x, y), SILVER[2] + (255,))
    return img


def arrow():
    """An arrow, fletching at the lower left and a bright silver head at the upper right."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(3, 13):
        img.putpixel((i, 15 - i), SHAFT[i % 2] + (255,))
    for x, y in ((12, 1), (13, 2), (14, 1), (13, 0), (12, 2), (14, 2), (13, 1), (11, 2)):
        img.putpixel((x, y), SILVER[2 + (x + y) % 2] + (255,))
    for x, y in ((2, 11), (3, 11), (1, 12), (2, 13), (4, 13), (4, 14), (3, 14), (1, 13)):
        img.putpixel((x, y), FLETCH[(x + y) % 2] + (255,))
    return img


def werewolf_textures():
    out = {("block", WOLFSBANE["block"]): wolfsbane(), ("item", WEREWOLF["dagger"]): dagger(), ("item", WEREWOLF["arrow"]): arrow()}
    for kind, spec in WEREWOLF["kinds"].items():
        out[("entity", f"werewolf_{kind}")] = werewolf(kind)
        out[("entity", f"werewolf_{kind}_eyes")] = werewolf(kind, eyes_only=True)
        out[("block", spec["rug"])] = rug(kind)
        out[("block", f"{spec['rug']}_head")] = rug_head(kind)
        out[("item", spec["pelt"])] = pelt(kind)
    return out
