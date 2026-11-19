"""Bake the Harbour Gift Fir, its separately wrapped parcels and its inventory miniature.

Model coordinates are pixels about the centre of the trunk. Clip complete elements at cell
borders, retaining UVs and omitting cut faces so the assembled boughs have no seams.
"""
import json
from math import ceil, cos, floor, sin, tau
from pathlib import Path
from model_writer import finish_elements, separate_parts

RES = Path(__file__).resolve().parents[1] / "src/main/resources"
ASSETS = RES / "assets/jugcraft"
DATA = RES / "data/jugcraft"
ID = "harbour_gift_fir"
ROOT = "jugcraft:block/" + ID
RING = [(0, 0), (-1, -1), (0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0)]
CELLS = [(x, y, z) for y in range(4) for x, z in RING if y != 3 or not x or not z] + [(0, 4, 0)]
TEX = {
    "particle": "jugcraft:newest/holidayworks/block/christmas_tree",
    "fir": "jugcraft:newest/holidayworks/block/christmas_tree",
    "twig": "jugcraft:newest/holidayworks/block/pine",
    "frost": "jugcraft:newest/holidayworks/block/pine_snowy",
    "bark": "minecraft:block/spruce_log", "oak": "minecraft:block/stripped_dark_oak_log",
    "brass": "minecraft:block/raw_gold_block", "gold": "minecraft:block/gold_block",
    "star": "minecraft:block/ochre_froglight_side", "light": "minecraft:block/shroomlight",
    "garnet": "minecraft:block/red_wool", "ruby": "minecraft:block/red_glazed_terracotta",
    "plum": "minecraft:block/purple_terracotta", "navy": "minecraft:block/blue_terracotta",
    "blue": "minecraft:block/cyan_wool", "ivory": "minecraft:block/bone_block_side",
    "silver": "minecraft:block/iron_block", "rose": "minecraft:block/pink_terracotta",
    "teal": "minecraft:block/warped_planks", "spruce": "minecraft:block/green_wool",
}
# The opaque section of the supplied triangular needle sprites. Texture samples are clipped
# from this rectangle for branch ends where a brighter, strand-like pattern reads well.
SAMPLE = {"twig": (9, 1, 16, 14), "frost": (9, 0, 15, 5), "ruby": (4, 4, 9, 10),
          "gold": (3, 3, 13, 13), "brass": (3, 1, 11, 9), "light": (2, 2, 10, 10)}
FACES = ("west", "down", "north", "east", "up", "south")


def write(path, doc):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(doc, separators=(",", ":")) + "\n", encoding="utf8")


def box(scene, material, x, y, z, X, Y, Z):
    scene.append((material, (x, y, z), (X, Y, Z)))


def bead(scene, material, x, y, z, width=2):
    h = width / 2
    box(scene, material, x-h, y, z-h, x+h, y+width, z+h)


def fir():
    scene = []
    # Pegged timber stand on the central woven mat; clearance under the first whorl leaves
    # parcels visible from the paths on every side.
    box(scene, "oak", -7, 0, -2, 7, 2, 2)
    box(scene, "oak", -2, 0, -7, 2, 2, 7)
    box(scene, "bark", -3, 1, -3, 3, 37, 3)
    box(scene, "bark", -2, 32, -2, 2, 65, 2)
    box(scene, "brass", -3.5, 5, -3.5, 3.5, 6.5, 3.5)
    # The eight embroidered panels are sewn to one octagonal wine-red skirt.
    for z,Z,r in [(-21,-17,13),(-17,-12,18),(-12,12,21),(12,17,18),(17,21,13)]:
        box(scene,"garnet",-r,0,z,r,.28,Z)
    for pile, (dx, dz) in enumerate(RING[1:]):
        x, z = dx * 16, dz * 16
        x += -dx * 2
        z += -dz * 2
        box(scene, "garnet", x-6, .05, z-6, x+6, .55, z+6)
        for side in [-1, 1]:
            box(scene, "brass", x+side*4.75-.32, .55, z-5.5, x+side*4.75+.32, .7, z+5.5)
            box(scene, "brass", x-5.5, .55, z+side*4.75-.32, x+5.5, .7, z+side*4.75+.32)
    # Five staggered whorls: crooked asymmetric sprays overlap like shingles and droop
    # at the extremities. Add short silver-green tips on scattered individual branches.
    palette = ("ruby", "gold", "blue", "ivory", "garnet", "brass")
    for tier, (base, radius, limbs, phase) in enumerate([(17,23,12,.02),(26,21,11,.30),(35,17.5,10,.04),
                                                        (44,14.5,9,.38),(52,10.8,8,.22),(60,6.4,6,.49)]):
        core = radius * .44
        box(scene, "fir", -core, base+1, -core*.7, core, min(base+10,66.5), core*.7)
        box(scene, "fir", -core*.7, base+2, -core, core*.7, min(base+9,66.8), core)
        for k in range(limbs):
            angle = tau * (k+phase) / limbs
            cx, cz = cos(angle), sin(angle)
            length = radius * (.96 + .04*sin(k*7+tier))
            droop = .8 * sin(k*3+tier)
            # Overlapping planar needle fans start full at the crown, ending in separate
            # thin tips. Inward sections merge into the dense core.
            for j, (r, half, thick, height) in enumerate([(.36,.29,6,5),(.60,.23,5,3),(.78,.17,4,1),(.93,.10,2.8,0)]):
                x, z = cx*length*r, cz*length*r
                h = max(1.2, length*half)
                y = base + height + droop
                box(scene, "twig" if j>=2 else "fir", x-h,y,z-h,x+h,y+thick,z+h)
                if j==2 and (k+2*tier)%5==0:
                    box(scene, "frost", x-h*.7,y+thick,z-h*.7,x+h*.6,y+thick+.65,z+h*.6)
            if (k+tier)%2==0 and tier < 5:
                x,z = cx*length*.93, cz*length*.93
                y = base+droop-4
                size = 3.15 if tier<3 else 2.15
                bead(scene, "brass", x,y+size,z,1.15)
                bead(scene, palette[(k//2+2*tier)%len(palette)], x,y,z,size)
                box(scene, "ivory", x+.5,y+size-.9,z-size/2-.06,x+1,y+size-.3,z-size/2+.18)
        # Twinned scallops of narrow gold cord and cranberry ribbon; seed lamps hang from
        # the lower edge. The stagger between rows keeps them from reading as rigid hoops.
        if tier in (0,2,4):
            steps=limbs*9
            previous=None
            for j in range(steps+1):
                t=j/steps
                angle=t*tau+tau*phase/limbs
                r=radius*(.97+.025*cos(t*tau*limbs))
                cur=(cos(angle)*r,base+1.4+1.4*cos(t*tau*limbs),sin(angle)*r)
                if previous:
                    x,y,z=cur
                    a,b,c=previous
                    mid=(y+b)/2
                    box(scene,"garnet" if tier==2 else "brass",min(x,a)-.18,mid-.24,min(z,c)-.18,
                        max(x,a)+.18,mid+(.59 if tier==2 else .26),max(z,c)+.18)
                previous=cur
            for k in range(limbs*3):
                angle = tau*(k/3+phase)/limbs
                r = radius*(.97 if k%3==0 else .945)
                x,z = cos(angle)*r,sin(angle)*r
                y = base+2-(1.8 if k%3 else 0)
                if k%3==1: bead(scene,"light",x,y-.85,z,1.3)
    # Pierced five-point hammered star, readable from the quay as well as from its sides.
    # Solid one-pixel steps catch light along the crown, arms, and two separated lower points.
    box(scene, "gold", -1,64,-1,1,70,1)
    rows=[[(0,1)],[(0,1)],[(-1,2)],[(-1,2)],[(-2,3)],[(-7,8)],
          [(-6,7)],[(-5,6)],[(-4,5)],[(-3,4)],[(-4,5)],[(-4,0),(1,5)],
          [(-5,-1),(2,6)],[(-5,-3),(4,6)]]
    for row,runs in enumerate(rows):
        for x,X in runs:
            y=79-row
            metal="brass" if row in (7,10,13) else "gold"
            box(scene,metal,x-.5,y,-.62,X-.5,y+1,.62)
            box(scene,metal,-.52,y,x-.5,.52,y+1,X-.5)
    box(scene,"star",-1.35,71.8,-1.35,1.35,74.5,1.35)
    # The shoulders of the upper crown occupy a cross. Trim the ends of diagonal sprays
    # flush before slicing, so an exposed part edge has a proper textured cap.
    for i,(material,lo,hi) in enumerate(scene):
        if hi[1]<=48: continue
        x,z=((lo[k]+hi[k])/2 for k in (0,2))
        if max(-lo[0],hi[0])>8 and max(-lo[2],hi[2])>8:
            k=0 if abs(x)<abs(z) else 2
            l,h=list(lo),list(hi)
            l[k]=max(-8,l[k]);h[k]=min(8,h[k])
            if h[k]<=l[k]:
                # A tip with its centre just past the corner bends inward at its end.
                shift=8-hi[k] if (x if k==0 else z)>0 else -8-lo[k]
                l[k],h[k]=lo[k]+shift,hi[k]+shift
            scene[i]=material,tuple(l),tuple(h)
    return scene


def parcel(pile, upper):
    scene = []
    dx, dz = RING[pile+1]
    x, z = dx*14, dz*14
    if upper:
        x += -1.5 if pile%2 else 1
        z += 1 if pile%3 else -1
    bottom = 7 if upper else .72
    w, d, h = (5.7,6,3.6) if upper else (9.4,8.4,5.3)
    w, d = w/2, d/2
    pairs = [("garnet","gold"),("navy","ivory"),("teal","gold"),("ivory","garnet"),
             ("plum","silver"),("blue","ivory"),("spruce","rose"),("rose","navy")]
    paper, ribbon = pairs[(pile + upper*3)%8]
    box(scene,paper,x-w,bottom,z-d,x+w,bottom+h,z+d)
    box(scene,paper,x-w-.23,bottom+h-.8,z-d-.23,x+w+.23,bottom+h+.2,z+d+.23)
    top = bottom+h+.26
    box(scene,ribbon,x-.63,bottom+.03,z-d-.28,x+.63,top,z+d+.28)
    box(scene,ribbon,x-w-.29,bottom+.03,z-.55,x+w+.29,top+.03,z+.55)
    for side in (-1,1):
        box(scene,ribbon,x+side*1.3-.65,top+.06,z-.65,x+side*1.3+.65,top+1.25,z+.65)
    bead(scene,"gold",x,top+.15,z,1.1)
    return scene


def element(material, lo, hi, local_lo, local_hi, translate, scale=1):
    uv = SAMPLE.get(material,(0,0,16,16))
    faces = {}
    def axis_value(k, val, a, b):
        return a+(b-a)*(val-lo[k])/(hi[k]-lo[k])
    for axis, name in enumerate(FACES):
        k = axis%3
        if axis < 3 and local_lo[k] > lo[k] or axis >= 3 and local_hi[k] < hi[k]:
            continue
        u,v = ((2,1) if k==0 else (0,2) if k==1 else (0,1))
        # Texture origin is at the top left. Keep the sample constant across a section.
        coords = [axis_value(u,local_lo[u],uv[0],uv[2]),axis_value(v,local_hi[v],uv[3],uv[1]),
                  axis_value(u,local_hi[u],uv[0],uv[2]),axis_value(v,local_lo[v],uv[3],uv[1])]
        faces[name] = {"uv":[round(n,3) for n in coords],"texture":"#"+material}
    return {"from":[round((p-t)*scale,3) for p,t in zip(local_lo,translate)],
            "to":[round((p-t)*scale,3) for p,t in zip(local_hi,translate)],"faces":faces}


def bake(scene, cell=None, sources=None):
    parts = []
    for index,(m,lo,hi) in enumerate(scene):
        if cell is None:
            # World tree is 80 px high. Centre its miniature around the inventory pivot.
            parts.append(element(m,lo,hi,lo,hi,(-40,0,-40),.2))
            continue
        x,y,z=cell
        start=(x*16-8,y*16,z*16-8)
        l=tuple(max(lo[k],start[k]) for k in range(3))
        h=tuple(min(hi[k],start[k]+16) for k in range(3))
        if all(h[k]-l[k]>.001 for k in range(3)):
            e=element(m,lo,hi,l,h,start)
            if e["faces"]:
                parts.append(e)
                if sources is not None: sources.append(index)
    used = {f["texture"][1:] for e in parts for f in e["faces"].values()} | {"particle"}
    return {"parent":"minecraft:block/block","ambientocclusion":False,
            "textures":{k:t for k,t in TEX.items() if k in used},"elements":parts}



def inventory_model(scene):
    """Sample the complete dressed tree onto a two-pixel lattice so fine cords stay legible at inventory scale."""
    voxels = {}
    for material, lo, hi in scene:
        spans = []
        for start, end in zip(lo, hi):
            # Include voxel centres inside the sculpt. A thin ribbon or star point still gets one voxel.
            first, stop = ceil(start / 2 - .5), ceil(end / 2 - .5)
            if stop <= first:
                first = floor((start + end) / 4)
                stop = first + 1
            spans.append(range(first, stop))
        for x in spans[0]:
            for y in spans[1]:
                for z in spans[2]:
                    voxels[x,y,z] = material
    boxes = []
    for x,y,z in sorted(voxels):
        material = voxels.get((x,y,z))
        if material is None:
            continue
        X,Y,Z = x+1,y+1,z+1
        while voxels.get((X,y,z)) == material:
            X += 1
        while all(voxels.get((a,y,Z)) == material for a in range(x,X)):
            Z += 1
        while all(voxels.get((a,Y,c)) == material for a in range(x,X) for c in range(z,Z)):
            Y += 1
        for a in range(x,X):
            for b in range(y,Y):
                for c in range(z,Z):
                    del voxels[a,b,c]
        boxes.append((material,(2*x,2*y,2*z),(2*X,2*Y,2*Z)))
    return bake(boxes)


def main():
    scene = fir()
    multipart = []
    sources = [[] for cell in CELLS]
    sections = [bake(scene, cell, source) for cell, source in zip(CELLS, sources)]
    separate_parts([section["elements"] for section in sections], CELLS, sources)
    for i, cell in enumerate(CELLS):
        for index, geom in enumerate([scene] + ([parcel(i-1,0),parcel(i-1,1)] if 0<i<9 else [])):
            name = str(i) if index == 0 else f"{i}_parcel_{index}"
            doc = sections[i] if index == 0 else bake(geom, cell)
            if index > 0: finish_elements(doc["elements"])
            if not doc["elements"]:
                raise ValueError(f"Empty geometry in {name}")
            write(ASSETS/"models/block"/ID/(name+".json"), doc)
            for y,facing in enumerate(("north","east","south","west")):
                when = {"part":str(i),"facing":facing}
                if index==1: when["parcels"]="1|2"
                if index==2: when["parcels"]="2"
                apply={"model":ROOT+"/"+name}
                if y: apply["y"]=y*90
                multipart.append({"when":when,"apply":apply})
    write(ASSETS/"blockstates"/(ID+".json"), {"multipart":multipart})
    skirt = [row for row in multipart if int(row["when"]["part"]) < 9]
    skirt = [row for row in skirt if row["when"]["part"] != "0"]
    skirt.append({"when":{"part":"0"},"apply":{"model":"minecraft:block/spruce_log"}})
    write(ASSETS/"blockstates/civic_gift_skirt.json", {"multipart":skirt})
    miniature = inventory_model(scene+parcel(2,0)+parcel(6,0))
    finish_elements(miniature["elements"])
    miniature["display"]={"gui":{"rotation":[12,-32,0],"translation":[0,0,0],"scale":[.98,.98,.98]}}
    write(ASSETS/"models/item"/(ID+".json"),miniature)
    write(ASSETS/"items"/(ID+".json"),{"model":{"type":"minecraft:model","model":"jugcraft:item/"+ID}})
    loot = json.loads((DATA/"loot_table/blocks/haunted_mailbox.json").read_text())
    write(DATA/"loot_table/blocks"/(ID+".json"),json.loads(json.dumps(loot).replace("haunted_mailbox",ID)))
    write(DATA/"recipe"/(ID+".json"),{"type":"minecraft:crafting_shaped","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"agriculture"}],"category":"misc","pattern":[" S ","LGL","WFW"],
          "key":{"S":"minecraft:gold_nugget","L":"minecraft:spruce_leaves","G":"minecraft:spruce_sapling",
                 "W":"minecraft:red_wool","F":"minecraft:spruce_log"},"result":{"id":"jugcraft:"+ID,"count":1}})
    write(DATA/"advancement/recipes/building"/(ID+".json"), {"parent":"minecraft:recipes/root",
          "criteria":{"sapling":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"items":"minecraft:spruce_sapling"}]}}},
          "rewards":{"recipes":["jugcraft:"+ID]}})


if __name__ == "__main__":
    main()
