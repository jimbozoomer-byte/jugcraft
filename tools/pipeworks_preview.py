"""Offline previews of the Pipeworks props (tools/pipeworks_models.py): every box drawn as its six faces, back faces
culled, sorted far to near, in a three-quarter orthographic view, each face flat-filled with its texture's average
colour and lit by its normal. A review aid, not what the game draws (docs/ART_DIRECTION.md asks for culled quads: a hole
left open shows here as the inside of the box behind it).

    python tools/pipeworks_preview.py [out.png]
"""
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
import model_writer  # noqa: E402
from pipeworks_models import MODELS, PROPS  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
TEX = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft" / "textures" / "block"
COLORS = {}


def color_of(name):
    name = name.rstrip("!")
    if name not in COLORS:
        path = TEX / f"{name}.png"
        if path.exists():
            img = Image.open(path).convert("RGB").resize((1, 1), Image.BOX)
            COLORS[name] = img.getpixel((0, 0))
        else:
            import pipeworks
            img = pipeworks.TEXTURES[name]().convert("RGB").resize((1, 1), Image.BOX)
            COLORS[name] = img.getpixel((0, 0))
    return COLORS[name]


FACE_CORNERS = {
    "down": ((0, 0, 0), (1, 0, 0), (1, 0, 1), (0, 0, 1)),
    "up": ((0, 1, 1), (1, 1, 1), (1, 1, 0), (0, 1, 0)),
    "north": ((1, 0, 0), (0, 0, 0), (0, 1, 0), (1, 1, 0)),
    "south": ((0, 0, 1), (1, 0, 1), (1, 1, 1), (0, 1, 1)),
    "west": ((0, 0, 0), (0, 0, 1), (0, 1, 1), (0, 1, 0)),
    "east": ((1, 0, 1), (1, 0, 0), (1, 1, 0), (1, 1, 1)),
}


def rotate(p, rotation):
    if not rotation:
        return p
    axis, angle, origin = rotation[:3]
    a = math.radians(angle)
    i = "xyz".index(axis)
    j, k = [(1, 2), (2, 0), (0, 1)][i]
    out = list(p)
    u, v = p[j] - origin[j], p[k] - origin[k]
    out[j] = origin[j] + u * math.cos(a) - v * math.sin(a)
    out[k] = origin[k] + u * math.sin(a) + v * math.cos(a)
    return out


def faces_of(elements):
    out = []
    for item in elements:
        frm, to, texture, options = model_writer.unpack(item)
        rotation = options.get("rotation")
        for face, corners in FACE_CORNERS.items():
            tex = model_writer._face_texture(texture, face)
            if tex is None:
                continue
            pts = [rotate([frm[i] + c[i] * (to[i] - frm[i]) for i in range(3)], rotation) for c in corners]
            out.append((pts, color_of(tex)))
    return out


def view(p, yaw=35.0, pitch=28.0):
    """Orthographic three-quarter view: returns (sx, sy, depth)."""
    ya, pa = math.radians(yaw), math.radians(pitch)
    x, y, z = p
    xr = x * math.cos(ya) - z * math.sin(ya)
    zr = x * math.sin(ya) + z * math.cos(ya)
    yr = y * math.cos(pa) - zr * math.sin(pa)
    depth = y * math.sin(pa) + zr * math.cos(pa)
    return xr, -yr, depth


def render(elements, scale=4.0, margin=12, yaw=35.0, pitch=28.0):
    faces = []
    for pts, color in faces_of(elements):
        screen = [view(p, yaw, pitch) for p in pts]
        # Winding: counter-clockwise on screen (y down) means the face looks away.
        area = 0.0
        for i in range(4):
            x0, y0, _ = screen[i]
            x1, y1, _ = screen[(i + 1) % 4]
            area += x0 * y1 - x1 * y0
        if area >= 0:
            continue
        a = [pts[1][i] - pts[0][i] for i in range(3)]
        b = [pts[3][i] - pts[0][i] for i in range(3)]
        n = [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
        length = math.sqrt(sum(c * c for c in n)) or 1
        n = [c / length for c in n]
        light = max(0.0, n[0] * -0.45 + n[1] * 0.8 + n[2] * -0.4)
        shade = 0.55 + 0.45 * light
        depth = sum(s[2] for s in screen) / 4
        faces.append((depth, [(s[0], s[1]) for s in screen], tuple(min(255, int(c * shade)) for c in color)))
    faces.sort(key=lambda f: f[0])
    xs = [x for _, poly, _ in faces for x, _ in poly]
    ys = [y for _, poly, _ in faces for _, y in poly]
    x0, y0 = min(xs), min(ys)
    img = Image.new("RGB", (int((max(xs) - x0) * scale) + 2 * margin, int((max(ys) - y0) * scale) + 2 * margin), (236, 238, 242))
    draw = ImageDraw.Draw(img)
    for _, poly, color in faces:
        draw.polygon([((x - x0) * scale + margin, (y - y0) * scale + margin) for x, y in poly], fill=color,
                     outline=tuple(max(0, c - 28) for c in color))
    return img


def sheet(out, names=None, scale=3.0):
    names = names or list(PROPS)
    tiles = [(name, render(MODELS[name], scale=scale)) for name in names]
    width = max(t.width for _, t in tiles)
    cols = 4
    rows = (len(tiles) + cols - 1) // cols
    row_heights = [max(tiles[i][1].height for i in range(r * cols, min(len(tiles), (r + 1) * cols))) for r in range(rows)]
    img = Image.new("RGB", (cols * (width + 10), sum(h + 24 for h in row_heights)), (236, 238, 242))
    draw = ImageDraw.Draw(img)
    y = 0
    for r in range(rows):
        for c in range(cols):
            i = r * cols + c
            if i >= len(tiles):
                break
            name, tile = tiles[i]
            x = c * (width + 10)
            img.paste(tile, (x + (width - tile.width) // 2, y + (row_heights[r] - tile.height)))
            draw.text((x + 4, y + row_heights[r] + 4), f"{name} ({PROPS[name][0]}, {'x'.join(str(s) for s in PROPS[name][1])})", fill=(30, 30, 30))
        y += row_heights[r] + 24
    img.save(out)
    return out


if __name__ == "__main__":
    print(sheet(sys.argv[1] if len(sys.argv) > 1 else "pipeworks_preview.png"))
