"""Named guest skins and their seasonal outfits, in the player skin layout (64 x 64, client/TownsfolkModel.java).

Original pixel interpretations, not downloaded skins. No randomness/noise, no resampling: an opaque base layer and
transparent outer layers (hat, jacket, sleeves, trousers) for layered hair, collars, coats, capes and skirts. Run this
file to regenerate only guest_*.png; it never rewrites the town cast (tools/town_skins.py).

Every guest has four outfits: normal (guest_<id>.png) and the seasonal winter, harvest and spring
(guest_<id>_<outfit>.png), chosen by an operator (town/guests/GuestWardrobe.java). A seasonal outfit keeps the face
and the hair and adds its own layer: a winter coat and scarf; a knitted cardigan in harvest colours and a leaf pin;
or a light bolero, flowers at the hem and a flower crown.

Body rule (owner, binding, 11 November 2026):
- HELD: characters depicted as minors or school students, and those whose age is uncertain. Classic 4-pixel arms,
  straight proportions, no shaping of any kind; clothing and hair only; their seasonal outfits stay age-appropriate.
  No aged-up or "adult alternate" versions.
- REAL: real people. Classic arms; clothing and hair only, no body emphasis; respectful, non-exaggerated likenesses.
- FITTED: clearly adult fictional characters. Slim 3-pixel arms and modest shaping only: a rounded shoulder seam and
  a slightly tapered waist seam at the sides of the torso.
- PETITE: Frieren (adult in her source, a petite design): slim arms only.
Nothing is ever drawn on the chest or hips for anyone. Slim arms need the renderer to choose the model's slim arms for
these skins (client/GuestBody.java holds the same lists).
"""
from pathlib import Path
from PIL import Image, ImageDraw
from town_skins import OUT, PARTS as CLASSIC, darker, lighter


def rgb(value):
    return tuple(bytes.fromhex(value))


def mix(a, b, t):
    return tuple(int(round(x + (y - x) * t)) for x, y in zip(a, b))


# Hair, eyes, outfit, lower garment, trim, skin; hair style; outfit style. One deliberate design per identity.
DESIGNS = {
    'emiru': ('34272c','715449','34313e','383343','bb718a','efd0bd','long','hoodie'),
    'lauren_chen': ('292329','533c30','ece4db','344257','b58d5a','e5bfa5','long','blazer'),
    'brittany_venti': ('34251f','574334','743d4d','32313e','c1966c','d7ac8c','curls','blazer'),
    'aoc': ('372821','533e30','f1e8d9','ddd4c7','b78949','c99473','long','blazer'),
    'sarah_stock': ('b99b63','627b88','eee5d6','e3d9c8','a08bb0','b1bb9e','long','bride'),
    'pokimane': ('5b3d30','614b35','c79bab','4d435a','efe4d5','e2b897','curls','hoodie'),
    'power': ('e3bb99','bd623a','e9e5dc','343649','253c61','f1cbb6','long','tie'),
    'makima': ('ae594b','c6a148','e9e5df','30313b','292b34','f0ceba','braid','tie'),
    'reze': ('51405f','648866','e8e3db','333344','27232f','edc8b9','bob','tie'),
    'asa_mitaka': ('282733','645348','e7e5df','263344','352b3a','efd1c2','long','uniform'),
    'himeno': ('263c3d','6c9978','30353d','272c35','ede5dc','e8c7af','bob','suit'),
    'tokai_teio': ('9a5b39','73538f','e9e4d8','e3ddd3','326f9f','f0cbb8','long','race'),
    'agnes_tachyon': ('9b6345','a55565','ece6d8','423344','ad3546','f0cdb7','bob','lab'),
    'gold_ship': ('c6c6d0','b64e62','9a3449','8c3044','ddd8c6','f1d5c5','long','race'),
    'oguri_cap': ('c5c4d1','617aa0','e8e4db','ddd7ce','3d5b88','edcebb','long','race'),
    'special_week': ('6e453e','8064a6','eae3d9','e3dace','715090','efcbb9','long','race'),
    'rei_ayanami': ('a5bfd0','a74b53','ebe9de','3f667a','943b42','eed4c7','bob','uniform'),
    'asuka_langley': ('b76d39','4d79aa','e8e4d8','34597a','a53936','f0c9ad','long','uniform'),
    'misato_katsuragi': ('383148','745071','9c3c45','37313f','d9bd62','edc3ae','long','jacket'),
    'frieren': ('dcdcd5','4a866d','eee9d8','33313e','c6a15d','f2d6c3','tails','mage'),
    'rem': ('7dadd1','486fbb','33313d','302e3a','e5e3df','f0d3c4','bob','maid'),
    'megumin': ('4a3430','a34547','9c3337','69343a','d6b35c','efcbb8','bob','mage'),
    'zero_two': ('dd9baa','4f9391','ad3b42','a0363d','e5dfcf','f1cdbc','long','military'),
    'rin_ishtar': ('4b302b','487b9e','a5343c','302d39','e6dfd3','f0ccba','long','cross'),
    'tifa_lockhart': ('30282b','8b493e','e6e4df','333139','8f3338','edc2aa','long','fighter'),
    'yorha_2b': ('dcd9d3','737786','302e39','292733','b7b0a8','eed4c3','bob','android'),
    'chun_li': ('352b28','644733','315ca0','6e4e3b','dabf66','e8b998','bun','qipao'),
    'ada_wong': ('292831','5a4b42','9f303d','352e38','bd8d60','ebc3ac','bob','agent'),
    'lara_croft': ('594030','705039','5b9690','74563e','393439','d8a67d','braid','explorer'),
    'kafka': ('6c364d','a5668f','e1d8d0','4a3546','986582','e8c4b7','long','jacket'),
    'miyabi': ('30323e','a24748','334052','2e3444','548cb1','edcbb9','long','military'),
    'nicole_demara': ('de9bab','65966e','e6e1d5','383344','77a486','ebc3ad','long','jacket'),
}

# The owner's body rule (module docstring). Every guest is in exactly one group.
HELD = ('asa_mitaka', 'tokai_teio', 'agnes_tachyon', 'gold_ship', 'oguri_cap', 'special_week', 'rei_ayanami',
        'asuka_langley', 'rem', 'megumin', 'rin_ishtar', 'power', 'reze', 'zero_two')
REAL = ('emiru', 'lauren_chen', 'brittany_venti', 'aoc', 'sarah_stock', 'pokimane')
FITTED = ('makima', 'himeno', 'misato_katsuragi', 'tifa_lockhart', 'yorha_2b', 'chun_li', 'ada_wong', 'lara_croft',
          'kafka', 'miyabi', 'nicole_demara')
PETITE = ('frieren',)
# Slim (3-pixel) arms: the adult fictional guests only.
SLIM = FITTED + PETITE
# A long gown or robe whose skirt flares a little at the hip, drawn by GuestBody's closed flare panels, which sample
# FLARE_UV. Clothing only, never body shape, and never for a held guest.
FLARE = ('sarah_stock', 'frieren')
# The flare panels' texture island, unused by the player layout: x 56-63, y 16-31.
FLARE_UV = (56, 16, 8, 16)

# Outfits in the order GuestWardrobe offers them; "normal" is the named skin itself.
OUTFITS = ('normal', 'winter', 'harvest', 'spring')

# Below the waist: a skirt (the rows of leg it covers, then legwear and shoes), a gown, or trousers (the default). Over
# the top: an open coat or jacket over an inner top, a long coat's tails, a cape; sleeve length in rows; extras.
LOOKS = {
    'emiru': dict(sleeves=10),
    'lauren_chen': dict(coat='2f3a4f', inner='ece4db', sleeves=10),
    'brittany_venti': dict(coat='743d4d', inner='eee5d8', sleeves=10),
    'aoc': dict(coat='f1e8d9', inner='3b4254', sleeves=10),
    'sarah_stock': dict(lower='gown', sleeves=10),
    'pokimane': dict(sleeves=10),
    'power': dict(sleeves=10),
    'makima': dict(sleeves=10, belt='292b34'),
    'reze': dict(sleeves=10, belt='27232f'),
    'asa_mitaka': dict(lower='skirt', skirt=7, legwear='2b2a34', shoes='2a2328', sleeves=10),
    'himeno': dict(coat='30353d', inner='ede5dc', tie='272c35', sleeves=10),
    'tokai_teio': dict(sleeves=10),
    'agnes_tachyon': dict(coat='ece6d8', inner='ddc98f', tie='ad3546', tails=6, sleeves=12),
    'gold_ship': dict(sleeves=10),
    'oguri_cap': dict(sleeves=10),
    'special_week': dict(sleeves=10),
    'rei_ayanami': dict(lower='skirt', skirt=7, legwear='e8e6df', shoes='6b4a3a', sleeves=4, cuffs=False, pinafore=True),
    'asuka_langley': dict(lower='skirt', skirt=7, legwear='e8e6df', shoes='6b3a32', sleeves=4, cuffs=False, pinafore=True),
    'misato_katsuragi': dict(lower='skirt', skirt=6, legwear='2d2a33', shoes='3a2a2a', coat='9c3c45', inner='37313f',
                             sleeves=10),
    'frieren': dict(lower='skirt', skirt=8, skirt_colour='eee9d8', legwear='33313e', shoes='5d4636', sleeves=10),
    'rem': dict(lower='skirt', skirt=8, legwear='2d2b36', shoes='2a2630', sleeves=10),
    'megumin': dict(lower='skirt', skirt=8, legwear='2e2a33', shoes='5a3a2e', cape='2b2930', sleeves=10),
    'zero_two': dict(sleeves=10, belt='e5dfcf'),
    'rin_ishtar': dict(lower='skirt', skirt=6, legwear='26242e', shoes='3a2a28', sleeves=10),
    'tifa_lockhart': dict(lower='skirt', skirt=5, legwear='2a2830', shoes='5a4a40', sleeves=3, cuffs=False),
    'yorha_2b': dict(lower='skirt', skirt=6, legwear='25232c', shoes='25232c', sleeves=12),
    'chun_li': dict(lower='skirt', skirt=5, legwear='6e4e3b', shoes='ece7dc', sleeves=5),
    'ada_wong': dict(sleeves=10, belt='352e38', gloves='2b2830'),
    'lara_croft': dict(sleeves=2, cuffs=False, belt='4a3a2c', gloves='3a302c', shoes='4d3a2c'),
    'kafka': dict(coat='3c2a3a', inner='e1d8d0', tails=7, sleeves=10),
    'miyabi': dict(coat='334052', inner='2a2f3d', tails=7, sleeves=10),
    'nicole_demara': dict(coat='e6e1d5', inner='3a3446', sleeves=10),
}

SIDES = ('front', 'back', 'right', 'left')
BASE = ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg')
WHITE = rgb('f2ede4')
SHOES, SOLE = rgb('34313a'), rgb('514a50')
# Slim arms, as the model's slim arms and a player's slim skin lay them out: three pixels wide.
SLIM_PARTS = dict(CLASSIC, right_arm=(40, 16, 3, 12, 4), right_sleeve=(40, 32, 3, 12, 4),
                  left_arm=(32, 48, 3, 12, 4), left_sleeve=(48, 48, 3, 12, 4))


def faces(part, slim=False):
    """Each face's rectangle (x, y, w, h), as town_skins.faces, with slim arms too. A top face's row 0 is its back
    edge; a right face's last column and a left face's first column are its front edge; a front face's column 0 is
    the character's right."""
    u, v, w, h, d = (SLIM_PARTS if slim else CLASSIC)[part]
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + d + w + d, v + d, w, h),
    }


class Painter:
    """A clean town skin (town_skins.Skin, clean=True) whose arms may be slim."""

    def __init__(self, slim):
        self.slim = slim
        self.img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
        self.px = self.img.load()

    def faces(self, part):
        return faces(part, self.slim)

    def w(self, part, face):
        """A face's width in pixels."""
        return self.faces(part)[face][2]

    def dot(self, part, face, i, j, colour, alpha=255):
        x, y, w, h = self.faces(part)[face]
        if 0 <= i < w and 0 <= j < h:
            self.px[x + i, y + j] = (*colour[:3], alpha)

    def get(self, part, face, i, j):
        x, y, w, h = self.faces(part)[face]
        return self.px[x + i, y + j]

    def rect(self, part, face, x, y, w, h, colour, alpha=255):
        for j in range(y, y + h):
            for i in range(x, x + w):
                self.dot(part, face, i, j, colour, alpha)

    def fill(self, part, colour, sides=None, rows=None, shade=True, alpha=255):
        """As town_skins.Skin.fill on a clean skin: cloth lit above, a shade darker in its lower half and at the hem;
        bare skin (shade=False) keeps its sides flat. rows limits the side faces to rows [r0, r1) from the top (the
        top face only when r0 is 0, the bottom only when r1 is the part's height)."""
        height = (SLIM_PARTS if self.slim else CLASSIC)[part][3]
        for name, (x, y, w, h) in self.faces(part).items():
            if sides and name not in sides:
                continue
            if rows and (name == 'top' and rows[0] > 0 or name == 'bottom' and rows[1] < height):
                continue
            for j in range(h):
                if rows and name not in ('top', 'bottom') and not rows[0] <= j < rows[1]:
                    continue
                if name in ('top', 'bottom'):
                    f = 1.06 if name == 'top' else 0.8
                elif not shade:
                    f = 1.0
                else:
                    f = 0.84 if j == h - 1 else 0.92 if j >= h // 2 else 1.0
                for i in range(w):
                    self.px[x + i, y + j] = tuple(max(0, min(255, int(c * f))) for c in colour[:3]) + (alpha,)

    def ring(self, part, j0, j1, colour, shade=True):
        """Rows [j0, j1) all the way round a part."""
        self.fill(part, colour, rows=(j0, j1), shade=shade)

    def shade(self, part, face, i, j, f):
        x, y, w, h = self.faces(part)[face]
        if 0 <= i < w and 0 <= j < h:
            r, g, b, a = self.px[x + i, y + j]
            if a:
                self.px[x + i, y + j] = (int(r * f), int(g * f), int(b * f), a)

    def clear(self, part, face, i, j):
        self.dot(part, face, i, j, (0, 0, 0), 0)

    def wipe(self, part, rows=None):
        """Clears an outer layer's side faces (rows [r0, r1) only, if given)."""
        for name in SIDES:
            x, y, w, h = self.faces(part)[name]
            for j in range(h):
                if rows is None or rows[0] <= j < rows[1]:
                    for i in range(w):
                        self.px[x + i, y + j] = (0, 0, 0, 0)

    def save(self, path):
        self.img.save(path, optimize=False, compress_level=0)


# ---------------------------------------------------------------- face and hair

def face(p, eyes, skin):
    """Single-pixel-high eyes and a small mouth, ordinary Minecraft face proportions (unchanged from the first pass)."""
    for x in (1, 5):
        p.rect('head', 'front', x, 4, 2, 1, rgb('f3eee4'))
    for x in (2, 5):
        p.dot('head', 'front', x, 4, eyes)
    p.rect('head', 'front', 3, 6, 2, 1, darker(skin, .79))


LONG = ('long', 'curls', 'tails')
# The fringe on the hat layer: gaps in its second row, strands in its third, and how far the framing locks fall.
FRINGE = {
    'long': ((3,), (0, 1, 6, 7), 6),
    'curls': ((), (0, 2, 5, 7), 6),
    'tails': ((4,), (0, 1, 5, 6, 7), 6),
    'bob': ((), (0, 1, 2, 5, 6, 7), 5),
    'braid': ((3, 4), (0, 7), 4),
    'bun': ((), (0, 7), 3),
}


def head_hair(p, hair, length):
    """The under layer, on the head itself: crown, back and sides, with darker strands on a slant."""
    sides = 7 if length in LONG else 5
    p.fill('head', hair, sides=['top', 'back'])
    p.fill('head', hair, sides=['left', 'right'], rows=(0, sides))
    p.rect('head', 'front', 0, 0, 8, 2, hair)
    for x in (0, 7):
        p.rect('head', 'front', x, 2, 1, 4, hair)
    strand = darker(hair, .84)
    for name, rows in (('top', 8), ('back', 8), ('left', sides), ('right', sides)):
        x, y, w, h = p.faces('head')[name]
        for j in range(rows):
            for i in range(w):
                if (i + 2 * j) % 5 == 0:
                    p.px[x + i, y + j] = (*strand, 255)


def hat_hair(p, hair, length):
    """The outer layer: lighter strands over the crown and sides, a fringe with an uneven edge and face-framing locks,
    each layer a little shorter than the one beneath, so it reads as hair rather than a box."""
    light, dark = lighter(hair, 1.10), darker(hair, .86)
    back_rows = 8 if length in LONG + ('braid',) else 7
    side_rows = 8 if length in LONG else 6
    p.fill('hat', hair, sides=['top'])
    p.fill('hat', hair, sides=['back'], rows=(0, back_rows))
    p.fill('hat', hair, sides=['right', 'left'], rows=(0, side_rows))
    for name in ('top', 'back', 'left', 'right'):
        x, y, w, h = p.faces('hat')[name]
        for j in range(h):
            for i in range(w):
                if p.px[x + i, y + j][3] and (i + (j // 2 if length == 'curls' else 0)) % 3 == 0:
                    p.px[x + i, y + j] = (*light, 255)
    for name, edge in (('right', 3), ('left', 0)):
        p.clear('hat', name, edge, side_rows - 1)
        if length == 'curls':
            for i in (1, 3) if name == 'right' else (0, 2):
                p.clear('hat', name, i, side_rows - 1)
    if length == 'bob':
        for name in ('right', 'left', 'back'):
            row = (side_rows if name != 'back' else back_rows) - 1
            for i in range(p.w('hat', name)):
                p.shade('hat', name, i, row, .86)   # the bob curls in
    gaps, strands, lock = FRINGE[length]
    for i in range(8):
        p.dot('hat', 'front', i, 0, hair)
        if i not in gaps:
            p.dot('hat', 'front', i, 1, light if i % 3 == 0 else hair)
    for i in strands:
        p.dot('hat', 'front', i, 2, hair)
    for i in (0, 7):
        for j in range(2, lock + 1):
            p.dot('hat', 'front', i, j, dark if j == lock else hair)


def back_hair(p, hair, length, tie):
    """Hair below the head, on the jacket layer: long hair down the back with a tapered end and locks over the
    shoulders, a braid, two tails, or a bob's nape."""
    light, dark = lighter(hair, 1.12), darker(hair, .86)
    if length in ('long', 'curls'):
        spans = [(1, 7)] * 6 + [(2, 6), (3, 5)]
        for j, (a, b) in enumerate(spans):
            for i in range(a, b):
                wave = j if length == 'curls' else 0
                p.dot('jacket', 'back', i, j, light if (i + wave) % 3 == 1 else hair)
        if length == 'curls':
            for i in (1, 6):
                p.dot('jacket', 'back', i, 6, dark)
        for name, cols in (('right', (0, 1)), ('left', (2, 3))):
            for i in cols:
                p.rect('jacket', name, i, 0, 1, 4, hair)
                p.dot('jacket', name, i, 3, dark)
        for i in (0, 7):
            p.rect('jacket', 'front', i, 0, 1, 4, hair)
            p.dot('jacket', 'front', i, 1, light)
            p.dot('jacket', 'front', i, 3, dark)
    elif length == 'braid':
        p.rect('jacket', 'back', 2, 0, 4, 1, hair)
        for j in range(1, 10):
            for i in (3, 4):
                p.dot('jacket', 'back', i, j, light if (i + j) % 2 else dark)
        p.rect('jacket', 'back', 3, 9, 2, 1, tie)
    elif length == 'tails':
        for a in (1, 5):
            for j in range(10):
                for i in (a, a + 1):
                    outer = 1 if a == 1 else 6
                    if j < 9 or i == outer:
                        p.dot('jacket', 'back', i, j, dark if j >= 8 else light if i == outer and j % 4 != 3 else hair)
            p.rect('jacket', 'back', a, 1, 2, 1, tie)
        for name, col in (('right', 0), ('left', 3)):
            p.rect('jacket', name, col, 0, 1, 6, hair)
    elif length == 'bob':
        p.rect('jacket', 'back', 2, 0, 4, 1, hair)


# ---------------------------------------------------------------- clothes

def arms(p, colour, rows, cuff, gloves=None):
    """Sleeves of `rows` rows (a cuff on the last unless they reach the hand), bare hands below, or gloves."""
    for part in ('right_arm', 'left_arm'):
        if rows:
            p.ring(part, 0, rows, colour)
            if cuff is not None and rows < 12:
                p.ring(part, rows - 1, rows, cuff)
        if gloves is not None:
            p.ring(part, 10, 12, gloves)


def open_coat(p, colour, tails=0):
    """An open coat or jacket on the jacket layer: its fronts, sides and back over the inner top, lapels opening in a
    V; a long coat's tails fall on the trouser layer, open in front. Its sleeves stand out at the shoulder."""
    edge = darker(colour, .82)
    for j in range(12):
        f = 0.84 if j == 11 else 0.92 if j >= 6 else 1.0
        c = tuple(int(v * f) for v in colour)
        for i in (0, 1, 6, 7):
            p.dot('jacket', 'front', i, j, c)
            p.dot('body', 'front', i, j, c)
        for name in ('right', 'left', 'back'):
            p.rect('jacket', name, 0, j, p.w('jacket', name), 1, c)
            p.rect('body', name, 0, j, p.w('body', name), 1, c)
    for j in range(3):
        for i in (2, 5):
            p.dot('jacket', 'front', i, j, edge if j == 2 else colour)
    for i in (1, 6):
        p.rect('jacket', 'front', i, 4, 1, 8, edge)
    for part in ('right_sleeve', 'left_sleeve'):
        p.ring(part, 0, 2, lighter(colour, 1.04))
    if tails:
        for part, outer, side in (('right_pants', (0, 1), 'right'), ('left_pants', (2, 3), 'left')):
            for j in range(tails):
                c = darker(colour, .84) if j == tails - 1 else darker(colour, .92) if j >= 3 else colour
                for i in outer:
                    p.dot(part, 'front', i, j, c)
                for name in ('back', side):
                    p.rect(part, name, 0, j, 4, 1, c)


def skirt_colour(spec, look):
    return rgb(look['skirt_colour']) if 'skirt_colour' in look else rgb(spec[3])


def legs(p, spec, look):
    lower = rgb(spec[3])
    kind = look.get('lower', 'trousers')
    shoes = rgb(look['shoes']) if 'shoes' in look else SHOES
    for part in ('right_leg', 'left_leg'):
        if kind == 'gown':
            p.fill(part, lower)
            p.ring(part, 11, 12, lighter(lower, 1.03))
        elif kind == 'skirt':
            k = look['skirt']
            p.ring(part, 0, k, skirt_colour(spec, look))
            p.ring(part, k, 10, rgb(look['legwear']))
            p.ring(part, 10, 12, shoes)
            p.rect(part, 'front', 0, 10, 4, 1, lighter(shoes, 1.18))
        else:
            p.fill(part, lower)
            p.ring(part, 9, 12, shoes)
            p.rect(part, 'front', 0, 10, 4, 1, SOLE if 'shoes' not in look else lighter(shoes, 1.18))


def skirt_layer(p, rows, colour, hem):
    """A skirt on the trouser layer, so it hangs a little wider than the legs; its hem darker or trimmed."""
    for part in ('right_pants', 'left_pants'):
        p.fill(part, colour, sides=list(SIDES), rows=(0, rows))
        p.fill(part, hem, sides=list(SIDES), rows=(rows - 1, rows))


def clothes(p, name, spec):
    """The guest's own outfit, drawn round every side of each part, never a flat front decal."""
    hair, eyes, cloth, lower, trim, skin = map(rgb, spec[:6])
    style = spec[7]
    look = LOOKS[name]
    inner = rgb(look['inner']) if 'inner' in look else cloth
    coat = rgb(look['coat']) if 'coat' in look else None
    sleeve = coat if coat else cloth
    cuff = darker(sleeve, .86) if coat or not look.get('cuffs', True) else trim
    p.fill('body', inner)
    arms(p, sleeve, look.get('sleeves', 10), cuff, rgb(look['gloves']) if 'gloves' in look else None)
    legs(p, spec, look)
    # A neck opening and straight side seams; no anatomy is drawn on clothes.
    p.rect('body', 'front', 3, 0, 2, 2, skin)
    for x in (0, 7):
        p.rect('body', 'front', x, 2, 1, 10, darker(inner, .88))
    if 'belt' in look:
        for name_ in SIDES:
            p.rect('body', name_, 0, 8, p.w('body', name_), 1, rgb(look['belt']))
    else:
        p.rect('body', 'front', 0, 10, 8, 1, trim)
        p.rect('body', 'back', 0, 10, 8, 1, trim)
    kind = look.get('lower', 'trousers')
    if kind == 'skirt':
        hem = trim if style in ('mage', 'qipao') else darker(skirt_colour(spec, look), .8)
        skirt_layer(p, look['skirt'], skirt_colour(spec, look), hem)
    elif kind == 'gown':
        for part in ('right_pants', 'left_pants'):
            p.fill(part, lower, sides=list(SIDES), rows=(0, 11))
            p.fill(part, lighter(lower, 1.04), sides=list(SIDES), rows=(10, 11))
    if coat:
        open_coat(p, coat, look.get('tails', 0))
        for y in (5, 8):
            p.dot('jacket', 'front', 6, y, rgb('cbb98b') if coat[0] < 200 else rgb('b0a48a'))
    if style in ('tie', 'suit'):
        tie = trim if style == 'tie' else rgb(look.get('tie', '272c35'))
        for x in (2, 5):
            p.dot('body', 'front', x, 1, WHITE)
        p.rect('body', 'front', 3, 2, 2, 5, tie)
        p.dot('body', 'front', 3, 7, tie)
        if style == 'tie':
            for part in ('right_arm', 'left_arm'):
                p.ring(part, 9, 10, WHITE)
    if 'tie' in look and style != 'suit':
        p.rect('body', 'front', 3, 2, 2, 5, rgb(look['tie']))
        p.dot('body', 'front', 3, 7, rgb(look['tie']))
    if style == 'uniform':
        for x, y in ((2, 1), (5, 1), (3, 2), (4, 2)):
            p.dot('body', 'front', x, y, trim)
        if look.get('pinafore'):
            for name_ in ('front', 'back'):
                for x in (1, 6):
                    p.rect('body', name_, x, 0, 1, 8, lower)
            for name_ in SIDES:
                p.rect('body', name_, 0, 8, p.w('body', name_), 4, lower)
    if style == 'hoodie':
        for x in (2, 5):
            p.rect('body', 'front', x, 1, 1, 4, trim)
        p.rect('body', 'front', 2, 7, 4, 2, darker(cloth, .8))
        for name_ in SIDES:
            p.rect('body', name_, 0, 11, p.w('body', name_), 1, darker(cloth, .78))
    if style == 'blazer':
        for x in (2, 5):
            p.dot('body', 'front', x, 1, lighter(inner, 1.03))
    if style == 'lab':
        for x in (2, 5):
            p.rect('body', 'front', x, 0, 1, 2, WHITE)
    if style == 'military' and not coat:
        for x in (1, 6):
            p.rect('jacket', 'front', x, 2, 1, 8, darker(cloth, .75))
        for y in (4, 7, 9):
            p.dot('jacket', 'front', 5, y, rgb('cbb98b'))
        p.rect('body', 'front', 0, 0, 8, 1, trim)
        p.rect('body', 'front', 3, 1, 2, 1, trim)
    if style == 'military' and coat:
        p.rect('jacket', 'front', 2, 0, 1, 2, trim)
        p.rect('jacket', 'front', 5, 0, 1, 2, trim)
        for y in (4, 7):
            p.dot('jacket', 'front', 0, y, trim)
            p.dot('jacket', 'front', 7, y, trim)
    if style == 'maid':
        apron = rgb('eee8df')
        p.rect('jacket', 'front', 2, 3, 4, 9, apron)
        for x in (2, 5):
            p.rect('jacket', 'front', x, 0, 1, 3, apron)
        for part, cols in (('right_pants', (2, 3)), ('left_pants', (0, 1))):
            for i in cols:
                p.rect(part, 'front', i, 0, 1, 6, apron)
        p.rect('body', 'front', 2, 0, 4, 1, apron)
    if style == 'bride':
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 0, 10, lighter(cloth, 1.02))
        for name_ in SIDES:
            p.rect('body', name_, 0, 7, p.w('body', name_), 1, trim)
    if style == 'race':
        for part in ('body', 'right_arm', 'left_arm'):
            p.ring(part, 0, 2, trim)
        p.rect('body', 'front', 3, 2, 2, 8, trim)
        for y in (3, 6, 8):
            p.dot('body', 'front', 4, y, rgb('d9ba67'))
    if style == 'mage' and name == 'frieren':
        # A short capelet with a gold edge over a white dress.
        for name_ in SIDES:
            p.rect('jacket', name_, 0, 0, p.w('jacket', name_), 3, lighter(cloth, 1.02))
            p.rect('jacket', name_, 0, 3, p.w('jacket', name_), 1, trim)
        p.clear('jacket', 'front', 3, 0)
        p.clear('jacket', 'front', 4, 0)
        p.rect('body', 'front', 0, 8, 8, 1, trim)
    elif style == 'mage':
        for part in ('body', 'right_arm', 'left_arm'):
            p.ring(part, 9, 10, trim)
        p.rect('body', 'front', 3, 2, 2, 7, trim)
    if 'cape' in look:
        cape = rgb(look['cape'])
        for j in range(12):
            c = darker(cape, .9) if j >= 6 else cape
            p.rect('jacket', 'back', 0, j, 8, 1, c)
            p.dot('jacket', 'right', 0, j, c)
            p.dot('jacket', 'left', 3, j, c)
        for x in (0, 7):
            p.rect('jacket', 'front', x, 0, 1, 2, cape)
    if style in ('cross', 'agent'):
        p.rect('body', 'front', 2, 0, 4, 2, cloth)   # a high neck
    if style == 'cross':
        p.rect('body', 'front', 3, 2, 1, 4, trim)
        p.rect('body', 'front', 2, 3, 3, 1, trim)
    if style == 'fighter':
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 9, 12, trim)
        for name_ in ('front', 'back'):
            for x in (1, 6):
                p.rect('body', name_, x, 0, 1, 10, rgb('3c3332'))
    if style == 'explorer':
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 0, 2, cloth)
        for x in (1, 6):
            p.dot('body', 'front', x, 9, rgb('6b5340'))
        # A small backpack on the jacket layer (the braid hangs over it), its straps over the shoulders.
        p.rect('jacket', 'back', 1, 2, 6, 7, rgb('6b5340'))
        p.rect('jacket', 'back', 1, 2, 6, 1, rgb('7d6249'))
        for x in (1, 6):
            p.rect('jacket', 'front', x, 0, 1, 5, rgb('5a4434'))
    if style == 'qipao':
        p.rect('body', 'front', 0, 0, 8, 1, trim)   # a mandarin collar
        p.rect('body', 'front', 3, 1, 2, 1, trim)
        p.rect('body', 'front', 5, 1, 1, 9, trim)
        p.rect('body', 'front', 0, 7, 8, 1, trim)
        for part in ('right_sleeve', 'left_sleeve'):
            p.ring(part, 0, 3, lighter(cloth, 1.06))
            p.ring(part, 3, 4, trim)
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 9, 11, rgb('3a3436'))
    if style == 'android':
        p.rect('body', 'front', 2, 0, 4, 2, cloth)
        p.rect('body', 'front', 3, 2, 2, 9, darker(cloth, .8))
        p.rect('body', 'front', 0, 7, 8, 1, trim)
        for part in ('right_sleeve', 'left_sleeve'):
            p.ring(part, 0, 3, lighter(cloth, 1.12))
    if name == 'misato_katsuragi':
        p.dot('body', 'front', 4, 2, trim)   # her pendant
    if name == 'nicole_demara':
        p.rect('body', 'front', 3, 4, 2, 2, rgb('4c382f'))
    return look


def fitted(p, name):
    """Modest tailoring for clearly adult fictional characters only: a rounded shoulder seam and a slightly tapered
    waist seam at the outer columns of the torso. Nothing on the chest or hips."""
    if name not in FITTED:
        return
    for layer in ('body', 'jacket'):
        for name_ in ('front', 'back'):
            for i in (0, 7):
                for j in (0, 5, 6, 7):
                    p.shade(layer, name_, i, j, .86)


def marks(p, name, spec):
    """Character details on the hat layer, over the hair: headbands, a veil, an eyepatch, horn bases, forelocks,
    clips, a visor, hair-bun covers."""
    hair, trim, style = rgb(spec[0]), rgb(spec[4]), spec[7]
    if style in ('maid', 'bride'):
        for x in range(1, 7):
            p.dot('hat', 'front', x, 0, rgb('f2eee6'))
    if style == 'bride':
        # A short veil behind the head, lightly see-through, and two flowers on the band.
        p.fill('hat', rgb('e8e0d1'), sides=['back'], alpha=150)
        for x in (2, 5):
            p.dot('hat', 'front', x, 1, trim)
    if style == 'android':
        p.rect('hat', 'front', 0, 3, 8, 2, rgb('302e37'))
    if style == 'qipao':
        for name_ in ('left', 'right'):
            p.rect('hat', name_, 1, 1, 2, 3, rgb('ece7dc'))
    if name == 'himeno':
        p.rect('hat', 'front', 0, 4, 3, 2, rgb('282b32'))
        for x in range(8):
            p.dot('hat', 'front', x, 3, rgb('303139'))
    if name in ('power', 'zero_two'):
        for x in (1, 6):
            p.rect('hat', 'front', x, 0, 1, 3, rgb('b23d47'))
    if name in ('tokai_teio', 'oguri_cap', 'special_week'):
        p.rect('hat', 'front', 3, 0, 2, 3, rgb('eee5d6'))
    if name == 'gold_ship':
        p.rect('hat', 'front', 2, 0, 4, 2, rgb('5f536c'))
        p.rect('hat', 'front', 3, 1, 2, 1, rgb('cdb56e'))
    if name == 'rem':
        p.rect('hat', 'front', 4, 1, 3, 4, hair)
        for x, y in ((1, 1), (0, 2), (2, 2), (1, 3)):
            p.dot('hat', 'front', x, y, rgb('d99fb5'))
    if name == 'megumin':
        p.rect('hat', 'front', 0, 0, 8, 2, rgb('604237'))
        for x in (2, 5):
            p.dot('hat', 'front', x, 1, trim)
    if name == 'kafka':
        for x in (1, 5):
            p.rect('hat', 'front', x, 0, 2, 1, rgb('4d293b'))


# ---------------------------------------------------------------- seasonal layers

WINTER = [rgb(c) for c in ('3f4a66', '5a3640', '3d5446', '4a4450', '6a5442', '3f5560')]
HARVEST = [rgb(c) for c in ('a8572e', 'c2873a', '7a7a40', '96463a', 'b8903e', '8a5a34')]
SPRING = [rgb(c) for c in ('b9dcc4', 'd8c6e6', 'f1cfd8', 'f3e3b0', 'c8dff0', 'e8d6c6')]
BLOOMS = [rgb(c) for c in ('f2a7c0', 'f7f3ea', 'f2d36b', 'c9a2e0')]
LEAF = rgb('7fb069')
FUR = rgb('eeeae2')
SILVER, FROST = rgb('cfd6de'), rgb('e6eaee')


def distance(a, b):
    return sum(abs(x - y) for x, y in zip(a, b))


def seasonal(palette, index, cloth):
    """The guest's colour from a seasonal palette: of its own and the next, the one that stands out from its clothes."""
    a, b = palette[index % len(palette)], palette[(index + 1) % len(palette)]
    return a if distance(a, cloth) >= distance(b, cloth) else b


def drop_tails(p, look):
    """A long coat's tails would show under a seasonal layer that replaces the coat: take them off the trousers."""
    if look.get('tails'):
        for part in ('right_pants', 'left_pants'):
            p.wipe(part)


def winter_under(p, spec, look, index):
    """A closed winter coat over the outfit, mittens and boots (the scarf goes on over the hair). A bride keeps her
    gown and wears a white stole and long gloves."""
    cloth, trim = rgb(spec[2]), rgb(spec[4])
    if spec[7] == 'bride':
        for name_ in SIDES:
            for j in range(3):
                for i in range(p.w('jacket', name_)):
                    p.dot('jacket', name_, i, j, FROST if (i + j) % 3 == 0 else SILVER)
        p.clear('jacket', 'front', 3, 0)
        p.clear('jacket', 'front', 4, 0)
        for part in ('right_sleeve', 'left_sleeve'):
            p.ring(part, 0, 3, SILVER)
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 6, 12, FROST)
        return
    coat = mix(seasonal(WINTER, index, cloth), cloth, .25)
    p.fill('body', coat)
    p.rect('body', 'front', 3, 1, 1, 11, darker(coat, .8))
    for y in (3, 6, 9):
        p.dot('body', 'front', 4, y, FUR)
    for part in ('right_arm', 'left_arm'):
        p.ring(part, 0, 10, coat)
        p.ring(part, 10, 12, lighter(trim, 1.05))
    for part in ('right_sleeve', 'left_sleeve'):
        p.wipe(part)
        p.ring(part, 9, 10, FUR)
    p.wipe('jacket')   # the coat covers the outfit's own jacket details; the hair goes back on after
    kind = look.get('lower', 'trousers')
    for part in ('right_pants', 'left_pants'):
        if kind != 'skirt':
            p.wipe(part)
        else:
            k = look['skirt']
            p.fill(part, skirt_colour(spec, look), sides=list(SIDES), rows=(4, k))   # the skirt below the coat, apron-free
        for name_ in SIDES:
            for j in range(4):
                for i in range(p.w(part, name_)):
                    p.dot(part, name_, i, j, FUR if j == 3 else darker(coat, .92) if j else coat)
    boots = rgb('4a3a32')
    for part in ('right_leg', 'left_leg'):
        if kind == 'skirt':
            p.ring(part, look['skirt'], 9, darker(rgb(look['legwear']), .95))
        p.ring(part, 9, 12, boots)
        p.ring(part, 9, 10, lighter(boots, 1.25))


def winter_over(p, spec):
    """A scarf in the guest's own accent colour, wound round on the jacket layer with one end hanging in front."""
    trim = rgb(spec[4])
    scarf = trim if sum(trim) < 600 else rgb('9c3c45')
    stripe = lighter(scarf, 1.18)
    for name_ in SIDES:
        for i in range(p.w('jacket', name_)):
            p.dot('jacket', name_, i, 0, scarf)
            p.dot('jacket', name_, i, 1, stripe if i % 2 else scarf)
    for j in range(2, 6):
        p.dot('jacket', 'front', 5, j, stripe if j % 2 else scarf)
        if j < 5:
            p.dot('jacket', 'front', 6, j, scarf)
    p.clear('jacket', 'front', 3, 0)
    p.clear('jacket', 'front', 4, 0)


def harvest_under(p, spec, look, index):
    """An open knitted cardigan in a harvest colour over the outfit, and brown boots. A bride wears a knitted shawl
    and an amber band at the hem of her gown."""
    knit = seasonal(HARVEST, index, rgb(spec[2]))
    purl = darker(knit, .86)
    if spec[7] == 'bride':
        # A knitted shawl round the shoulders and upper arms.
        for name_ in SIDES:
            for j in range(5):
                for i in range(p.w('jacket', name_)):
                    if name_ == 'front' and 2 <= i <= 5:
                        continue
                    p.dot('jacket', name_, i, j, purl if (i + j) % 2 else knit)
        for part in ('right_sleeve', 'left_sleeve'):
            for name_ in SIDES:
                for j in range(5):
                    for i in range(p.w(part, name_)):
                        p.dot(part, name_, i, j, purl if (i + j) % 2 else knit)
        for part in ('right_pants', 'left_pants'):
            p.fill(part, knit, sides=list(SIDES), rows=(10, 11))
        return
    drop_tails(p, look)
    for j in range(11):
        for i in (0, 1, 6, 7):
            p.dot('jacket', 'front', i, j, purl if (i + j) % 2 else knit)
        for name_ in ('right', 'left', 'back'):
            for i in range(p.w('jacket', name_)):
                p.dot('jacket', name_, i, j, purl if (i + j) % 2 else knit)
    for j in range(3):
        for i in (2, 5):
            p.clear('jacket', 'front', i, j)   # no lapels of a coat it replaces
    for i in range(8):
        p.dot('jacket', 'back', i, 10, darker(knit, .78))
    for i in (0, 7):
        p.dot('jacket', 'front', i, 10, darker(knit, .78))
    for y in (3, 6, 9):
        p.dot('jacket', 'front', 1, y, rgb('e8d6b0'))
    rows = min(max(look.get('sleeves', 10), 9), 10)
    for part in ('right_arm', 'left_arm'):
        p.ring(part, 0, rows, knit)
        p.ring(part, rows - 1, rows, darker(knit, .8))
    for part in ('right_sleeve', 'left_sleeve'):
        p.wipe(part)
        p.ring(part, 0, 2, lighter(knit, 1.05))
    boots = rgb('5b3a26')
    for part in ('right_leg', 'left_leg'):
        p.ring(part, 10 if look.get('lower') == 'skirt' else 9, 12, boots)


def harvest_over(p):
    """A leaf pin in the hair: orange, red and gold."""
    for (i, j), c in zip(((6, 0), (7, 1), (6, 1), (7, 0)), ('d9782d', 'b5452f', 'e3b04b', '8a6a2a')):
        p.dot('hat', 'front', i, j, rgb(c))
    p.dot('hat', 'left', 0, 1, rgb('d9782d'))
    p.dot('hat', 'left', 1, 0, rgb('e3b04b'))


def spring_under(p, spec, look, index):
    """A light bolero in a spring colour, and small flowers at the hem."""
    pastel = seasonal(SPRING, index, rgb(spec[2]))
    if 'coat' in look:
        # A light spring jacket instead of the coat.
        drop_tails(p, look)
        open_coat(p, pastel)
        rows = look.get('sleeves', 10)
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 0, rows, pastel)
            p.ring(part, rows - 1, rows, darker(pastel, .86))
    elif spec[7] != 'bride':
        drop_tails(p, look)
        for j in range(6):
            c = darker(pastel, .93) if j >= 4 else pastel
            for i in (0, 1, 6, 7):
                p.dot('jacket', 'front', i, j, c)
            for name_ in ('right', 'left', 'back'):
                for i in range(p.w('jacket', name_)):
                    p.dot('jacket', name_, i, j, c)
        for j in range(3):
            for i in (2, 5):
                p.clear('jacket', 'front', i, j)
        for part in ('right_arm', 'left_arm'):
            p.ring(part, 0, 4, pastel)
            p.ring(part, 3, 4, darker(pastel, .86))
        for part in ('right_sleeve', 'left_sleeve'):
            p.wipe(part)
            p.ring(part, 0, 2, lighter(pastel, 1.03))
    kind = look.get('lower', 'trousers')
    row = {'gown': 9, 'skirt': look.get('skirt', 6) - 2}.get(kind, 7)
    for part in ('right_pants', 'left_pants'):
        for name_, cols in (('front', (0, 2)), ('back', (1, 3)), ('right', (1,)), ('left', (2,))):
            for n, i in enumerate(cols):
                p.dot(part, name_, i, row, BLOOMS[(index + n + len(name_)) % len(BLOOMS)])


def spring_over(p, index):
    """A flower crown round the hat layer's top row: small blooms between leaves."""
    for name_ in SIDES:
        for i in range(p.w('hat', name_)):
            p.dot('hat', name_, i, 0, BLOOMS[(i // 2 + index) % len(BLOOMS)] if i % 2 == 0 else LEAF)


# ---------------------------------------------------------------- assembly

def islands(p, name, spec):
    """Reserved unused UV islands: (0..7, 0..7) and (24..31, 0..7) for the closed head-accessory geometry, and the
    flare panels' island for a flared gown or robe, sampled from the skirt as finally drawn."""
    hair, eyes, cloth, lower, trim, skin = map(rgb, spec[:6])
    accessory = (rgb('604237') if name == 'megumin' else
                 rgb('b23d47') if name in ('power', 'zero_two') else
                 skin if name == 'frieren' else hair)
    for y in range(8):
        for x in range(8):
            p.px[x, y] = (*accessory, 255)
            p.px[24 + x, y] = (*trim, 255)
    if name in FLARE:
        u, v, w, h = FLARE_UV
        rows = [p.get('right_pants', 'front', 1, j) for j in range(4)]
        rows = [r if r[3] else p.get('right_leg', 'front', 1, j) for j, r in enumerate(rows)]
        for y in range(v, v + h):
            for x in range(u, u + w):
                p.px[x, y] = (*rows[0][:3], 255)
        # Front and back tiles (3x4x1 at 56,16) have their sides on rows 17-20; side tiles (1x4x3 at 56,24) on 27-30.
        for top in (17, 27):
            for j in range(4):
                for x in range(u, u + w):
                    p.px[x, top + j] = (*rows[j][:3], 255)


def fallback(p):
    """Slim arms leave two columns of a classic arm unused: fill them from their neighbours, so the base stays
    opaque and the skin still reads correctly if it is ever drawn on classic arms."""
    if not p.slim:
        return
    for source, xs, ys in ((53, (54, 55), range(20, 32)), (49, (50, 51), range(16, 20)),
                           (45, (46, 47), range(52, 64)), (41, (42, 43), range(48, 52))):
        for y in ys:
            for x in xs:
                p.px[x, y] = p.px[source, y]


def make(name, spec, outfit='normal'):
    """One guest's skin in one outfit. The face and hair are the same in every outfit."""
    if outfit not in OUTFITS:
        raise ValueError(outfit)
    hair, eyes, cloth, lower, trim, skin = map(rgb, spec[:6])
    length = spec[6]
    index = list(DESIGNS).index(name)
    p = Painter(slim=name in SLIM)
    for part in BASE:
        p.fill(part, skin, shade=False)
    face(p, eyes, skin)
    head_hair(p, hair, length)
    look = clothes(p, name, spec)
    fitted(p, name)
    if outfit == 'winter':
        winter_under(p, spec, look, index)
    elif outfit == 'harvest':
        harvest_under(p, spec, look, index)
    elif outfit == 'spring':
        spring_under(p, spec, look, index)
    hat_hair(p, hair, length)
    back_hair(p, hair, length, trim)
    marks(p, name, spec)
    if outfit == 'winter' and spec[7] != 'bride':
        winter_over(p, spec)
    elif outfit == 'harvest':
        harvest_over(p)
    elif outfit == 'spring':
        spring_over(p, index)
    islands(p, name, spec)
    fallback(p)
    return p


def skin_name(name, outfit='normal'):
    """The packaged texture name: guest_<id>, or guest_<id>_<outfit> for a seasonal outfit."""
    return f'guest_{name}' if outfit == 'normal' else f'guest_{name}_{outfit}'


def write():
    OUT.mkdir(parents=True, exist_ok=True)
    for name, spec in DESIGNS.items():
        for outfit in OUTFITS:
            make(name, spec, outfit).save(OUT / f'{skin_name(name, outfit)}.png')


def sprite(p, side):
    """A flat front or back view of a skin, each base part then its outer layer (no geometry accessories)."""
    out = Image.new('RGBA', (16, 32))
    w = 3 if p.slim else 4
    at = {'head': (4, 0), 'body': (4, 8), 'right_leg': (4, 20), 'left_leg': (8, 20),
          'right_arm': (4 - w, 8), 'left_arm': (12, 8)}
    if side == 'back':
        at.update(right_arm=(12, 8), left_arm=(4 - w, 8), right_leg=(8, 20), left_leg=(4, 20))
    overlay = {'head': 'hat', 'body': 'jacket', 'right_arm': 'right_sleeve', 'left_arm': 'left_sleeve',
               'right_leg': 'right_pants', 'left_leg': 'left_pants'}
    for part, xy in at.items():
        for layer in (part, overlay[part]):
            x, y, fw, fh = p.faces(layer)[side]
            out.alpha_composite(p.img.crop((x, y, x + fw, y + fh)), xy)
    return out


def group(name):
    return 'held' if name in HELD else 'real person' if name in REAL else 'adult, fitted' if name in FITTED else 'adult, petite'


def preview(path, outfits=('normal',)):
    """An unfiltered front/back sheet for design review, not an in-game render."""
    cols, cell_w, cell_h = 8, 160, 200
    rows = (len(DESIGNS) * len(outfits) + cols - 1) // cols
    sheet = Image.new('RGB', (cols * cell_w, rows * cell_h), (35, 39, 47))
    draw = ImageDraw.Draw(sheet)
    n = 0
    for name, spec in DESIGNS.items():
        for outfit in outfits:
            p = make(name, spec, outfit)
            x, y = (n % cols) * cell_w, (n // cols) * cell_h
            for column, side in enumerate(('front', 'back')):
                s = sprite(p, side).resize((64, 128), Image.Resampling.NEAREST)
                sheet.paste(s, (x + column * 76 + 8, y + 35), s)
            draw.text((x + 5, y + 8), name if outfit == 'normal' else f'{name}: {outfit}', fill='white')
            draw.text((x + 8, y + 169), group(name) + (', slim arms' if name in SLIM else ''), fill=(180, 185, 195))
            n += 1
    path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(path)


if __name__ == '__main__':
    write()
    preview(Path('build/jze-guest-skins.png'))
    preview(Path('build/jze-guest-outfits.png'), OUTFITS)
    print(f'Wrote {len(DESIGNS) * len(OUTFITS)} guest skins ({len(DESIGNS)} guests x {len(OUTFITS)} outfits)')
