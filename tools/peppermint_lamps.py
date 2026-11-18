"""Build the Lane and Boulevard lamps, their complete tiled surface models, recipes and loot.

One voxel of the sculpt is one model pixel. Greedy meshing joins coplanar exposed faces into
textured rectangles. Geometry is clipped at block and glass boundaries, preserving the inside
of the crook, the lamp frames and their backs. The item uses the same complete sculpt.
"""
from collections import defaultdict
from math import atan2, pi, hypot
from pathlib import Path
import json

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
ASSETS = ROOT / "assets/jugcraft"
DATA = ROOT / "data/jugcraft"
NAMES = ("peppermint_lane_lamp", "peppermint_boulevard_lamp")
DIRECTIONS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0),
              "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
BRASS = Image.open(ASSETS / "textures/block/deepstead_hammered_brass.png").convert("RGB")
CANE = Image.open(ASSETS / "textures/menagerie/cavern/block/candy_cane_block.png").convert("RGB")


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, separators=(",", ":"), ensure_ascii=False) + "\n", encoding="utf8")


def lamp_voxels(tall):
    voxels = {}

    def box(x0, y0, z0, x1, y1, z1, material):
        for x in range(x0, x1):
            for y in range(y0, y1):
                for z in range(z0, z1):
                    voxels[x, y, z] = material

    def octagon(cx, y0, cz, radius, y1, material):
        for x in range(cx-radius, cx+radius):
            for z in range(cz-radius, cz+radius):
                if abs(x+.5-cx) + abs(z+.5-cz) <= radius * 1.53:
                    box(x, y0, z, x+1, y1, z+1, material)

    # Follow the cane round the bend in arc length. Two enamel colors wind around its
    # local circumference; the stripe therefore continues onto the return tip.
    axle = 69 if tall else 37
    radius = 3.0 if tall else 2.35
    for x in range(3, 29):
        for y in range(4, 80 if tall else 48):
            for z in range(4, 12):
                dx, dy, dz = x + .5 - 16, y + .5 - axle, z + .5 - 8
                if dy >= 0:
                    angle = min(pi, max(0, atan2(dy, dx)))
                    section = hypot(dx, dy) - 8
                    arc = axle + angle * 8
                elif dx > 0:
                    section, arc = dx - 8, y + .5
                elif dy >= -3:
                    section, arc = -dx - 8, axle + pi * 8 - dy
                else:
                    continue
                if hypot(section, dz) > radius:
                    continue
                phase = (arc / (10 if tall else 8) - atan2(dz, section) / (2*pi)) % 1
                voxels[x, y, z] = "red" if phase < .40 else "ivory"

    # Chamfered cast footing, raised plinth, gilded drip rail and square-headed anchors.
    foot = 7 if tall else 5
    for y0, r, y1, mat in [(0,foot,2,"iron"), (2,foot-1,3,"brass"),
                            (3,foot-1,5,"iron"), (5,foot-2,7,"iron")]:
        octagon(24, y0, 8, r, y1, mat)
    if tall:
        octagon(24, 7, 8, 5, 12, "fir")
        octagon(24, 12, 8, 5, 14, "brass")
        octagon(24, 14, 8, 4, 16, "iron")
        for z in (4, 10):
            for x in (20, 26):
                box(x, 8, z, x+2, 12, z+2, "iron")
    else:
        octagon(24, 7, 8, 3, 8, "brass")
    for dx, dz in ((-1,-1),(-1,1),(1,-1),(1,1)):
        x = 24 + dx*(foot-3) - (1 if dx < 0 else 0)
        z = 8 + dz*(foot-3) - (1 if dz < 0 else 0)
        box(x, 2, z, x+1, 4, z+1, "brass")

    # A square-link suspension supports stepped lantern eaves. The front and back
    # leadwork on the boulevard lantern make a matching diamond in the amber glass.
    height = 33 if tall else 0
    def lampbox(x0, y0, z0, x1, y1, z1, mat):
        box(x0, y0+height, z0, x1, y1+height, z1, mat)
    def lampoct(y0, r, y1, mat):
        octagon(8, y0+height, 8, r, y1+height, mat)

    # Small and large lanterns hang at y19 and y49 respectively.
    bottom = 16 if tall else 19
    shoulder = 29 if tall else 28
    r = 5 if tall else 4
    lampoct(bottom, 1, bottom+1, "brass")
    lampoct(bottom+1, 2, bottom+2, "iron")
    lampoct(bottom+2, 3, bottom+3, "brass")
    lampoct(bottom+3, r, bottom+4, "iron")
    lampoct(bottom+4, r-1, shoulder, "glass")
    for x in (8-r+1, 8+r-2):
        for z in (8-r+1, 8+r-2):
            lampbox(x, bottom+4, z, x+1, shoulder, z+1, "iron")
    lampoct(shoulder, r+1, shoulder+1, "brass")
    lampoct(shoulder+1, r, shoulder+2, "iron")
    lampoct(shoulder+2, r-1, shoulder+3, "fir")
    lampoct(shoulder+3, 2, shoulder+4, "iron")
    lampbox(7, shoulder+4, 7, 9, 34, 9, "brass")
    if tall:
        for z in (4, 11):
            for j, dx in enumerate((0, 1, 2, 1, 0)):
                for x in {7-dx, 8+dx}:
                    lampbox(x, 21+j, z, x+1, 22+j, z+1, "brass")
    else:
        lampbox(7, 32, 7, 8, 35, 8, "iron")
        lampbox(9, 32, 7, 10, 35, 8, "iron")
        lampbox(7, 34, 7, 10, 35, 8, "brass")

    # The boulevard post carries an evergreen collar, pegged red bow and two little
    # flared bells. Put the paired hanging bells on opposite sides of the post.
    if tall:
        octagon(24, 53, 8, 5, 55, "fir")
        octagon(24, 52, 8, 4, 53, "brass")
        for z in (3, 12):
            # Open ribbon loops, backed by pointed evergreen sprigs, with two
            # stepped tails falling to either side of the visible center knot.
            for hand in (-1,1):
                for dist,lo,hi,mat in [(1,53,56,"ribbon"),(2,54,55,"ribbon"),
                                        (3,54,55,"ribbon"),(4,54,58,"ribbon"),
                                        (3,56,58,"ribbon"),(2,56,57,"ribbon"),
                                        (5,55,57,"fir"),(6,56,57,"fir"),
                                        (1,51,53,"ribbon"),(2,50,52,"ribbon")]:
                    x = 24+dist if hand>0 else 23-dist
                    box(x,lo,z,x+1,hi,z+1,mat)
            box(23,53,z,25,56,z+1,"brass")
        for cx,drop in ((19,0),(29,2)):
            # Chain touching the brass collar, then pinched shoulders, a rolled
            # rim and an exposed iron clapper. The two bells hang at different heights.
            box(min(cx,24),53,7,max(cx,24)+1,54,9,"fir")
            box(cx,51-drop,8,cx+1,54,9,"brass")
            octagon(cx,50-drop,8,1,52-drop,"brass")
            octagon(cx,48-drop,8,2,50-drop,"brass")
            octagon(cx,47-drop,8,3,48-drop,"brass")
            box(cx-1,46-drop,7,cx+1,47-drop,9,"iron")
    else:
        octagon(24, 11, 8, 3, 13, "fir")
        box(22, 10, 5, 24, 12, 6, "ribbon")
        box(24, 9, 5, 26, 11, 6, "ribbon")
    return voxels


def pigment(mat, x, y, z, face, lit):
    u, v = (x + z, 15 - y) if face not in ("up", "down") else (x, z)
    source = CANE.getpixel((u % 16, v % 16))
    if mat == "brass":
        return BRASS.getpixel((u % 16, v % 16))
    # Sparse glaze highlights pick up the supplied candy cane palette. Darken its
    # candy pinks to lacquer red and warm the white where the enamel turns away.
    grain = (source[1] * 2 + source[0] + source[2]) // 4
    if mat == "red":
        return (192, 42, 49) if grain > 185 else (167, 33, 44)
    if mat == "ivory":
        return (249, 234, 203) if grain > 90 else (233, 217, 188)
    if mat == "glass":
        side = face in ("east", "west")
        if not lit:
            return (106, 77, 52) if (u + v) % 8 < 2 else (83, 65, 50)
        if (x in (6, 7) and z in (4, 5, 10, 11)) or (z in (6, 7) and x in (4, 5, 10, 11)):
            return (255, 239, 168)
        return (245, 181, 85) if side or (v % 9 == 0) else (255, 209, 119)
    palette = {"iron": ((47, 58, 55), (64, 74, 69)), "fir": ((42, 79, 65), (54, 96, 76)),
               "ribbon": ((127, 31, 43), (162, 43, 52))}
    return palette[mat][grain > 200]


def uv_key(side, p):
    x, y, z = (v % 16 for v in p)
    return {"north": (z,15-x,15-y), "south": (z,x,15-y), "east": (x,15-z,15-y),
            "west": (x,z,15-y), "up": (y,x,z), "down": (y,x,15-z)}[side]


def rectangles(voxels):
    planes = defaultdict(dict)
    for (x,y,z), material in voxels.items():
        cell = (x//16,y//16)
        for name, (dx,dy,dz) in DIRECTIONS.items():
            if (x+dx,y+dy,z+dz) not in voxels:
                depth,u,v = uv_key(name, (x,y,z))
                planes[cell,name,depth,material=="glass"][u,v] = (x,y,z)
    faces = []
    for (cell, side, depth, glow), plane in sorted(planes.items()):
        while plane:
            u,v = min(plane, key=lambda pair:(pair[1],pair[0]))
            w = h = 1
            while (u+w,v) in plane:
                w += 1
            while all((u+du,v+h) in plane for du in range(w)):
                h += 1
            pixels = [plane.pop((u+du,v+dv)) for dv in range(h) for du in range(w)]
            corners = ([min(p[k] for p in pixels) for k in range(3)],
                       [max(p[k] for p in pixels)+1 for k in range(3)])
            faces.append((cell, side, glow, w, h, pixels, corners))
    return faces


def mesh(name, tall):
    voxels = lamp_voxels(tall)
    # Catch corner-only ribbon joins and detached ornaments before writing the model.
    remaining = set(voxels)
    connected = [remaining.pop()]
    while connected:
        x, y, z = connected.pop()
        for dx, dy, dz in DIRECTIONS.values():
            neighbor = x+dx, y+dy, z+dz
            if neighbor in remaining:
                remaining.remove(neighbor)
                connected.append(neighbor)
    if remaining:
        raise ValueError(f"{name} has disconnected ornaments at {min(remaining)}")
    faces = sorted(rectangles(voxels), key=lambda f: (-f[4], -f[3]))
    # Rectangles carry a one-pixel gutter, repeated at the edges for mipmapping.
    layouts = []
    size = 64
    while True:
        layouts = []
        x = y = shelf = 0
        for _, _, _, w, h, _, _ in faces:
            if x+w+2 > size:
                x = 0; y += shelf; shelf = 0
            if y+h+2 > size:
                break
            layouts.append((x+1,y+1))
            x += w+2
            shelf = max(shelf,h+2)
        if len(layouts) == len(faces):
            break
        size *= 2
    cells = [(1,y) for y in range(5 if tall else 3)] + [(0,y) for y in ((3,4) if tall else (1,2))]
    assert set(cells) == {c for c, *_ in faces}
    assets = "jugcraft:block/" + name
    particle = "jugcraft:menagerie/cavern/block/candy_cane_block"
    for lit in (True, False):
        atlas = Image.new("RGB",(size,size),(47,58,55))
        parts = [[] for _ in cells]
        item = []
        for (cell, side, glow, w, h, pixels, (low,high)), (ox,oy) in zip(faces, layouts):
            for dv in range(-1,h+1):
                for du in range(-1,w+1):
                    pos = pixels[min(max(dv,0),h-1)*w+min(max(du,0),w-1)]
                    atlas.putpixel((ox+du,oy+dv),pigment(voxels[pos],*pos,side,lit))
            uv = [v*16/size for v in (ox,oy,ox+w,oy+h)]
            surface = {"faces":{side:{"uv":uv,"texture":"#skin"}}}
            if glow and lit:
                surface["light_emission"] = 13
            origin = [16*cell[0],16*cell[1],0]
            parts[cells.index(cell)].append(dict(surface, **{"from":[v-o for v,o in zip(low,origin)],"to":[v-o for v,o in zip(high,origin)]}))
            if lit:
                # Preserve the silhouette in the slots, centered as one full model.
                scale = 15 / (80 if tall else 48)
                def small(coords):
                    return [round(8+(coords[0]-16)*scale,4), round(.5+coords[1]*scale,4),round(8+(coords[2]-8)*scale,4)]
                item.append(dict(surface, **{"from":small(low), "to":small(high)}))
        suffix = "" if lit else "_off"
        atlas.save(ASSETS / f"textures/block/{name}{suffix}.png", optimize=True)
        textures = {"particle":particle,"skin":assets+suffix}
        for i, elements in enumerate(parts):
            # Only the lower lantern cell changes; other part models are shared.
            if lit or i == len(parts)-2:
                write(ASSETS/f"models/block/{name}_{i}{suffix}.json",{"textures":textures,"elements":elements})
        if lit:
            write(ASSETS/f"models/item/{name}.json",{"parent":"minecraft:block/block","textures":textures,"elements":item,
                  "display":{"gui":{"rotation":[12,18,0],"translation":[0,0,0],"scale":[1,1,1]}}})
    variants = {}
    for facing,rotation in zip(("north","east","south","west"),(0,90,180,270)):
        for i in range(len(cells)):
            for lit in (True,False):
                suffix = "_off" if not lit and i == len(cells)-2 else ""
                variants[f"facing={facing},lit={str(lit).lower()},part={i}"] = {"model":f"{assets}_{i}{suffix}","y":rotation}
    write(ASSETS/f"blockstates/{name}.json",{"variants":variants})
    write(ASSETS/f"items/{name}.json",{"model":{"type":"minecraft:model","model":"jugcraft:item/"+name}})
    loot = json.loads((DATA/"loot_table/blocks/harvest_photo_backdrop.json").read_text())
    loot = json.loads(json.dumps(loot).replace("harvest_photo_backdrop",name))
    write(DATA/f"loot_table/blocks/{name}.json",loot)
    key = {"R":"minecraft:red_dye","W":"minecraft:white_dye","L":"minecraft:lantern",
           "I":"minecraft:iron_ingot","C":"minecraft:copper_ingot"}
    pattern = ["RWL"," I "," C "]
    if tall:
        pattern = [" GL"," P ","ICI"]
        key = {"G":"minecraft:gold_ingot","L":"minecraft:lantern","P":"jugcraft:"+NAMES[0],
               "I":"minecraft:iron_ingot","C":"minecraft:copper_ingot"}
    write(DATA/f"recipe/{name}.json",{"type":"minecraft:crafting_shaped","category":"building","pattern":pattern,
                                     "key":key,"result":{"id":"jugcraft:"+name,"count":1}})
    recipe = "jugcraft:"+name
    ingredient = "jugcraft:"+NAMES[0] if tall else "minecraft:lantern"
    write(DATA/f"advancement/recipes/building/{name}.json", {
        "parent":"minecraft:recipes/root",
        "criteria":{"has_ingredient":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"items":ingredient}]}}},
        "rewards":{"recipes":[recipe]}})
    print(name, len(voxels), "voxels,",len(faces),"exposed rectangles,",size,"pixel atlas")


if __name__ == "__main__":
    for index, name in enumerate(NAMES):
        mesh(name, bool(index))

