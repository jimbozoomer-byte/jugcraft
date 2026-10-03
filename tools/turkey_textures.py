"""Original textures for wild turkeys (fall additions 18) (requires Pillow): the tom (bronze body barred black, a dark
breast with its beard, white-barred wings, a blue-white head with a red wattle and snood, and a chestnut tail of
black-barred feathers tipped with buff), the hen (mottled brown with buff edging, a grey-blue head) and the poult (buff
down striped brown), laid out for the client's TurkeyModel (64 by 64; each box's faces where a vanilla model box puts
them); the roast turkey on its platter (crisp golden skin, carved meat, bone, a silver platter, cranberries and herbs);
and the raw turkey and a slice as items.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from a fixed seed; no Mojang texture is
read, traced or recoloured.
"""
import random

from PIL import Image

from crop_textures import Canvas, rgb, outline
from halloween_textures import shade

# (u, v, width, height, depth) of each box, as TurkeyModel lays them out.
BOXES = {"body": (0, 0, 8, 7, 10), "breast": (0, 17, 6, 6, 3), "neck": (20, 17, 2, 7, 2), "head": (28, 17, 3, 3, 4),
         "beak": (42, 17, 1, 1, 2), "wattle": (48, 17, 1, 3, 1), "snood": (52, 17, 1, 3, 1), "wing": (36, 0, 1, 5, 8),
         "leg": (0, 26, 1, 7, 1), "foot": (4, 26, 3, 1, 3), "feather": (16, 26, 2, 12, 1)}

DARK = rgb("1a1410")
BUFF = rgb("d8b880")
WHITE = rgb("e8e4d8")
RED = rgb("c42828")
BEAK = rgb("c8b088")
LEG = rgb("b08a78")

# Per bird: the palette of each part (darkest first) and what is drawn over it.
BIRDS = {
    "tom": {"body": [rgb("3a2a1e"), rgb("5a3e24"), rgb("7a5430"), rgb("9a6e3a")], "breast": [rgb("2a2018"), rgb("3e2e20"), rgb("5a4028")],
            "neck": [rgb("a82a2a"), rgb("c03838")], "head": [rgb("8aa4d8"), rgb("c8d4ec"), rgb("e4ecf8")],
            "feather": [rgb("5a3a20"), rgb("7a5030"), rgb("8e6038")], "barred": True, "beard": True},
    "hen": {"body": [rgb("4a3624"), rgb("6a4e32"), rgb("8a6a44"), rgb("a8865a")], "breast": [rgb("5a4430"), rgb("7a5e40"), rgb("947656")],
            "neck": [rgb("7a8a9a"), rgb("8e9eac")], "head": [rgb("7a8a9a"), rgb("8e9eac"), rgb("a4b2be")],
            "feather": [rgb("5a4028"), rgb("74583a"), rgb("8a6c4a")], "barred": True, "beard": False},
    "poult": {"body": [rgb("a88858"), rgb("c0a070"), rgb("d4b888"), rgb("e4cca0")], "breast": [rgb("d4bc90"), rgb("e0cca4"), rgb("ecdcb8")],
              "neck": [rgb("c8b088"), rgb("d8c09a")], "head": [rgb("c8b088"), rgb("d8c09a"), rgb("e8d4b0")],
              "feather": [rgb("b89868"), rgb("c8a878"), rgb("d8bc8c")], "barred": False, "beard": False},
}


def faces(u, v, w, h, d):
    """Each face of a box at (u, v): name -> (x, y, width, height) on the texture."""
    return {"up": (u + d, v, w, d), "down": (u + d + w, v, w, d), "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


def fill(img, face, pick):
    x0, y0, w, h = face
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            colour = pick(x - x0, y - y0, w, h)
            if colour is not None:
                img.putpixel((x, y), colour + (255,))


def bird(kind):
    """The whole 64 by 64 texture for one kind of turkey."""
    info = BIRDS[kind]
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rng = random.Random({"tom": 18001, "hen": 18002, "poult": 18003}[kind])

    def feathered(palette, barred):
        def pick(x, y, w, h):
            if barred and (y + x // 3) % 3 == 2:
                return shade(palette[0], 0.6)
            return palette[min(len(palette) - 1, max(0, (x + 2 * y) % len(palette) + rng.choice((-1, 0, 0, 1))))]
        return pick

    for name in ("body", "breast", "neck", "head", "beak", "wattle", "snood", "wing", "leg", "foot", "feather"):
        for side, face in faces(*BOXES[name]).items():
            if name in ("body", "breast"):
                pick = feathered(info[name], info["barred"])
                if kind == "poult" and name == "body" and side == "up":
                    # A poult's back: two brown stripes down the down.
                    base = pick
                    pick = lambda x, y, w, h, base=base: rgb("7a5a34") if x in (2, w - 3) else base(x, y, w, h)
                if kind == "hen" and name == "body":
                    # A hen's feathers are edged with buff.
                    base = pick
                    pick = lambda x, y, w, h, base=base: BUFF if (y % 3 == 0 and (x + y) % 2 == 0) else base(x, y, w, h)
                if info["beard"] and name == "breast" and side == "front":
                    # The tom's beard: a black tuft hanging from the middle of the breast.
                    base = pick
                    pick = lambda x, y, w, h, base=base: DARK if x in (w // 2 - 1, w // 2) and y >= 2 else base(x, y, w, h)
            elif name == "wing":
                # Barred flight feathers, white and dark across the folded wing (plainer on a poult).
                light = WHITE if kind != "poult" else info["body"][3]
                pick = lambda x, y, w, h, light=light: light if x % 2 == 0 and y >= 1 else info["body"][0] if y >= 1 else info["body"][2]
            elif name == "neck":
                pick = lambda x, y, w, h: info["neck"][(x + y) % 2]
            elif name == "head":
                pick = lambda x, y, w, h: info["head"][min(2, (x + y + rng.randrange(2)) % 3)]
            elif name in ("wattle", "snood"):
                colour = RED if kind == "tom" else rgb("b07070") if kind == "hen" else info["head"][0]
                pick = lambda x, y, w, h, colour=colour: shade(colour, 0.85 + 0.1 * (y % 2))
            elif name == "beak":
                pick = lambda x, y, w, h: BEAK
            elif name in ("leg", "foot"):
                pick = lambda x, y, w, h: shade(LEG, 0.9 + 0.1 * ((x + y) % 2))
            else:
                # Tail feathers: the tip (the top of each face) buff, then a black band, then barred chestnut (a poult's plain).
                palette = info["feather"]

                def pick(x, y, w, h, palette=palette):
                    if kind == "poult":
                        return palette[(x + y) % 3]
                    if y <= 1:
                        return BUFF
                    if y == 2:
                        return DARK
                    return DARK if y % 3 == 0 else palette[(x + y) % 3]
            fill(img, face, pick)
    return img


SKIN = [rgb("8a4a18"), rgb("a85e22"), rgb("c4782e"), rgb("d8943e"), rgb("e8b060")]
MEAT = [rgb("d8c0a0"), rgb("e4d0b4"), rgb("ecdcc4"), rgb("f2e6d2")]
BONE = [rgb("d8d0bc"), rgb("e6e0d0"), rgb("f0ece0")]
PLATTER = [rgb("9a9ea4"), rgb("b4b8be"), rgb("ccd0d6"), rgb("e0e4e8")]
CRANBERRY = [rgb("7a1020"), rgb("a01c34"), rgb("c43048")]
HERB = [rgb("2e5a22"), rgb("3e7a2c"), rgb("5a9a3a")]


def noise(palette, seed, weights=None):
    img = Image.new("RGBA", (16, 16))
    rng = random.Random(seed)
    for x in range(16):
        for y in range(16):
            img.putpixel((x, y), rng.choices(palette, weights or [1] * len(palette))[0] + (255,))
    return img


def skin():
    """Crisp roast skin: golden brown, darker where it crackled, with glossy highlights."""
    img = noise(SKIN, 18101, [1, 2, 3, 2, 1])
    rng = random.Random(18102)
    for _ in range(10):
        x, y = rng.randrange(16), rng.randrange(16)
        img.putpixel((x, y), rgb("f4d08a") + (255,))
    return img


def meat():
    """Carved breast meat, its grain running across, a rim of skin at the top."""
    img = Image.new("RGBA", (16, 16))
    rng = random.Random(18110)
    for x in range(16):
        for y in range(16):
            colour = SKIN[2] if y < 2 else MEAT[(y + rng.choice((0, 0, 1))) % 4]
            img.putpixel((x, y), colour + (255,))
    return img


def platter():
    """A silver platter: a polished rim round a plain middle."""
    img = Image.new("RGBA", (16, 16))
    rng = random.Random(18150)
    for x in range(16):
        for y in range(16):
            rim = min(x, y, 15 - x, 15 - y) < 2
            if rim:
                colour = PLATTER[3] if (x + y) % 5 == 0 else PLATTER[2]
            else:
                colour = PLATTER[rng.choices((0, 1, 2), (1, 6, 2))[0]]
            img.putpixel((x, y), colour + (255,))
    return img


def raw_item():
    """A raw, plucked turkey: a pale pink body with its drumsticks up."""
    c = Canvas()
    pink = [rgb("e8b4a4"), rgb("f0c4b4"), rgb("f8d4c4")]
    for y in range(5, 14):
        for x in range(2, 14):
            if (x - 7.5) ** 2 / 30 + (y - 9.5) ** 2 / 14 <= 1:
                c.px(x, y, pink[(x + y) % 3])
    for x, y in ((3, 3), (4, 4), (11, 3), (12, 4), (4, 5), (11, 5)):
        c.px(x, y, pink[1])
    for x, y in ((2, 2), (13, 2)):
        c.px(x, y, BONE[2])
    outline(c, shade(pink[0], 0.6))
    return c.img


def slice_item():
    """A slice of roast turkey: pale meat, browned skin along its top edge."""
    c = Canvas()
    for y in range(5, 12):
        for x in range(3, 13):
            if abs(x - 8) <= 5 - (y - 5) // 3:
                c.px(x, y, SKIN[2] if y == 5 else MEAT[(x + y) % 4])
    outline(c, shade(SKIN[0], 0.7))
    return c.img


def turkey_textures():
    return {("entity", "turkey_tom"): bird("tom"), ("entity", "turkey_hen"): bird("hen"), ("entity", "turkey_poult"): bird("poult"),
            ("block", "roast_turkey_skin"): skin(), ("block", "roast_turkey_meat"): meat(),
            ("block", "roast_turkey_bone"): noise(BONE, 18120), ("block", "turkey_platter"): platter(),
            ("block", "turkey_cranberries"): noise(CRANBERRY, 18130), ("block", "turkey_herbs"): noise(HERB, 18140),
            ("item", "raw_turkey"): raw_item(), ("item", "turkey_slice"): slice_item()}
