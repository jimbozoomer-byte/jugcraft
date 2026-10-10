"""Imports the owner's own textures from the shared library (art/owner-library/originals/Blocks, docs: art/owner-library/
README.md) into the mod's resources, and checks the imported files still match their sources.

On 7 October 2026 the owner asked for their farming and food textures to be used ("I have already made a ton of custom
textures and food ... I made all of the textures in there myself its all mine"). Each imported texture is a byte-for-byte
copy of its library file, with its .png.mcmeta animation sidecar when it has one (its line ends made LF, as Git stores
the mod's text files). The only changes are the recolourings listed in a feature's RECOLOURED table (the bronze and
steel knives: the owner's iron knife with its blade's tones swapped for the approved bronze and steel ramps) and the
icons listed in a feature's COMPOSED table (a serving the owner drew no icon for: their bowl with a window of their
whole-dish icon heaped in it), and ornamental corn's ripe stage (tools/garden.py ORNAMENTAL_EARS: their ripe corn with its
ears in flint corn's colours). No generator draws over these files: tools/generate_textures.py runs this
import last, and tools/check_mod_data.py fails if a runtime copy differs from what this import would write.

    python3 tools/owner_art.py           write every import (generate_textures.py also does)
    python3 tools/owner_art.py --check   report differences only
"""
import io
import os
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)

import arms_pixel  # noqa: E402
import feasts  # noqa: E402
import garden  # noqa: E402
import kitchen  # noqa: E402
import menu  # noqa: E402
import rice  # noqa: E402
import soil  # noqa: E402

LIBRARY = os.path.join(ROOT, "art", "owner-library", "originals", "Blocks")
FOOD = "farming and food textures"
TEXTURES = os.path.join(ROOT, "src", "main", "resources", "assets", "jugcraft", "textures")


def imports():
    """(runtime path under textures/ without .png, library path under Blocks/ without .png) for every copied texture."""
    out = []
    out.append(("block/transport_crate_wood", "biomes and tree blocks/origin_oak_planks"))
    for table in (kitchen.TEXTURES, feasts.TEXTURES, menu.TEXTURES, rice.TEXTURES, soil.TEXTURES, garden.TEXTURES):
        for target, source in table.items():
            out.append((target, f"{FOOD}/{source}"))
    return out


def recolourings():
    """(runtime path, library path, ramp) for every recoloured texture."""
    return [(target, f"{FOOD}/{source}", getattr(arms_pixel, ramp)) for target, (source, ramp) in kitchen.RECOLOURED.items()]


def compositions():
    """(runtime path, spec) for every composed icon (tools/feasts.py COMPOSED)."""
    return list(feasts.COMPOSED.items())


def compose(spec):
    """A serving's icon: the window `crop` of the owner's dish icon set at `at`, behind the front of their bowl (the
    rows of their bowl icon from `bowl_from` down)."""
    bowl = Image.open(_source(f"{FOOD}/{spec['bowl']}")).convert("RGBA")
    out = Image.new("RGBA", bowl.size)
    out.alpha_composite(Image.open(_source(f"{FOOD}/{spec['dish']}")).convert("RGBA").crop(tuple(spec["crop"])), tuple(spec["at"]))
    front = Image.new("RGBA", bowl.size)
    front.paste(bowl.crop((0, spec["bowl_from"], bowl.width, bowl.height)), (0, spec["bowl_from"]))
    out.alpha_composite(front)
    return out


def _source(name):
    return os.path.join(LIBRARY, name + ".png")


def _target(name):
    return os.path.join(TEXTURES, name + ".png")


def recolour(image, ramp):
    """The image with its grey (blade) tones mapped, darkest first, onto `ramp` (an arms_pixel.Material: two outline
    tones, then dark, mid, light and highlight); the coloured (handle) pixels stay as drawn."""
    img = image.convert("RGBA")
    greys = sorted({p[:3] for p in img.get_flattened_data() if p[3] and p[0] == p[1] == p[2]}, key=sum)
    tones = [ramp.outline_dark, ramp.outline_light, ramp.dark, ramp.mid, ramp.light, ramp.highlight]
    # The two darkest greys are the outline's two tones; the rest climb the ramp from its dark tone up.
    mapping = {}
    for index, grey in enumerate(greys):
        tone = tones[0] if index == 0 else tones[1] if index <= 2 else tones[min(2 + index - 3, len(tones) - 1)]
        mapping[grey] = tuple(tone)
    out = img.copy()
    for y in range(img.height):
        for x in range(img.width):
            p = img.getpixel((x, y))
            if p[3] and p[:3] in mapping:
                out.putpixel((x, y), mapping[p[:3]] + (p[3],))
    return out


def _png_bytes(image):
    buffer = io.BytesIO()
    image.save(buffer, "PNG", optimize=True)
    return buffer.getvalue()


def expected():
    """{absolute runtime path: bytes} for every file the import writes."""
    files = {}
    for target, source in imports():
        with open(_source(source), "rb") as handle:
            files[_target(target)] = handle.read()
        meta = _source(source) + ".mcmeta"
        if os.path.isfile(meta):
            # A sidecar is text: the library keeps the owner's CRLF line ends, and Git stores resource text with LF
            # (.gitattributes), so the copy is written with LF to match what is committed.
            with open(meta, "rb") as handle:
                files[_target(target) + ".mcmeta"] = handle.read().replace(b"\r\n", b"\n")
    for target, source, ramp in recolourings():
        files[_target(target)] = _png_bytes(recolour(Image.open(_source(source)), ramp))
    for target, spec in compositions():
        files[_target(target)] = _png_bytes(compose(spec))
    for target, source in garden.ORNAMENTAL_EARS.items():
        # Ornamental corn's ripe stage: the owner's ripe corn, its ears in flint corn's colours (tools/garden.py).
        files[_target(target)] = _png_bytes(garden.flint_ears(Image.open(_source(f"{FOOD}/{source}"))))
    return files


def write_all():
    for path, data in expected().items():
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "wb") as handle:
            handle.write(data)


def errors():
    """Every imported file that is missing or differs from its source (or its recolouring)."""
    out = []
    for path, data in expected().items():
        rel = os.path.relpath(path, ROOT)
        if not os.path.isfile(path):
            out.append(f"{rel} is missing: run tools/owner_art.py (the owner's texture library import)")
            continue
        with open(path, "rb") as handle:
            if handle.read() != data:
                out.append(f"{rel} differs from the owner's library file it is imported from; the owner's art is kept as "
                           "drawn (tools/owner_art.py)")
    return out


if __name__ == "__main__":
    if "--check" in sys.argv:
        problems = errors()
        print("\n".join(problems) or "Every imported owner texture matches its source.")
        sys.exit(1 if problems else 0)
    write_all()
    print(f"Wrote {len(expected())} files from the owner's library.")
