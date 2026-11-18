"""Deepstead's first permanent retail halls, an overlay on the shipped waterfront.

Regenerate with python tools/deepstead_guild_shops.py. Before-images are read
from the packed base asset, including its resolved stair and pane connections.
The base plan, world and installation journals are never opened for writing.
"""
import gzip
import json
from pathlib import Path

from town_voxels import S, Voxels, connections, stair_shapes


TOWN = Path(__file__).resolve().parents[1] / "src/main/resources/data/jugcraft/town"
SHOPS = (
    {"id":"stonewright", "merchant":[91,-81,53], "entrance":[85,-81,58]},
    {"id":"copper_kettle", "merchant":[87,-67,121], "entrance":[83,-67,117]},
)
BOUNDS = ((80,-82,49,94,-70,63),(79,-68,116,91,-57,124))
AIR = S("air")
TUFF = S("tuff_bricks")
PILLAR = S("polished_tuff")
LIME = S("polished_diorite")
BAND = S("waxed_cut_copper")
COPPER = S("waxed_exposed_cut_copper")
PATINA = S("waxed_weathered_cut_copper")
OAK = S("dark_oak_planks")
TIMBER = S("stripped_dark_oak_log",axis="y")


def stonewright(v):
    # Northern cliff hall. The public forecourt, gantry and cart-sized arch
    # face the lower ring; its narrower clerestory stands above the side bays.
    for x in range(80,95):
        for z in range(49,64):
            flag = PILLAR if x in (80,94) or z in (49,58,63) else S("stone_bricks")
            if z>=59 and x in (83,87):
                flag = S("polished_andesite")
            v.set(x,-82,z,flag)
    v.walls(80,-81,49,94,-76,58,TUFF)
    for x in (80,89,94):
        for z in (49,58):
            v.box(x,-81,z,x,-75,z,S("chiseled_tuff_bricks"))
            v.set(x,-81,z,PILLAR)
    for z in (51,54,57):
        v.box(80,-79,z,80,-77,z,S("yellow_stained_glass_pane"))
        v.box(94,-79,z,94,-77,z,S("yellow_stained_glass_pane"))
    # Five-block nave arch with sprung haunches and a contrasting keystone.
    for x in range(82,89):
        rise = 4-min(2,abs(x-85)//2)
        if 83<=x<=87:
            v.box(x,-81,58,x,-82+rise,58,AIR)
        v.set(x,-81+rise,58,LIME if x==85 else PILLAR)
    v.box(90,-80,58,93,-80,58,PILLAR)
    for x in (91,92):
        v.box(x,-79,58,x,-77,58,S("light_blue_stained_glass_pane"))
    # Long ridge above three transverse ribs and stepped slate eaves.
    for z in range(49,59):
        inset = min(z-49,58-z)
        y = -75+inset
        for x in range(80,95):
            roof = S("polished_deepslate_stairs",facing="south" if z<54 else "north")
            v.set(x,y,z,roof if inset<4 else S("deepslate_tiles"))
            if x in (80,94) or x in (83,88,92) and inset in (1,2):
                v.box(x,-76,z,x,y-1,z,PILLAR if x in (80,94) else S("stripped_spruce_log",axis="z"))
        if inset==4:
            v.box(81,y+1,z,93,y+1,z,S("waxed_cut_copper_slab",type="bottom"))
    # The sample wall is a physical display: paired rough and dressed cuts
    # on an oak bench with brass dividers, lit by the clerestory.
    materials = (("tuff","polished_tuff"),("granite","polished_granite"),
                 ("andesite","polished_andesite"),("diorite","polished_diorite"))
    for group,(rough,dressed) in enumerate(materials):
        x = 81+group*3
        v.box(x,-81,50,x+1,-81,50,S("polished_blackstone_bricks"))
        v.set(x,-80,50,S(rough))
        v.set(x+1,-80,50,S(dressed))
        v.set(x,-79,50,S("smooth_stone_slab",type="bottom"))
        v.set(x+1,-79,50,S("smooth_stone_slab",type="bottom"))
        v.set(x,-77,49,S("ochre_froglight"))
    v.box(90,-81,55,93,-81,55,S("polished_andesite"))
    v.set(92,-80,55,S("smooth_stone_slab",type="bottom"))
    v.set(81,-81,54,S("stonecutter",facing="east"))
    v.set(81,-81,55,S("crafting_table"))
    # The projecting crane grows out of the masonry and has a rail-mounted
    # handling bench. Its chain suspends a cut sample clear of the main aisle.
    for z in (59,62):
        v.box(92,-81,z,92,-77,z,TIMBER)
        v.set(92,-76,z,BAND)
    v.box(90,-76,59,92,-76,59,S("stripped_spruce_log",axis="x"))
    v.box(90,-76,59,90,-76,62,S("stripped_spruce_log",axis="z"))
    v.box(90,-79,61,90,-77,61,S("iron_chain",axis="y"))
    v.set(90,-80,61,S("chiseled_stone_bricks"))
    v.box(93,-81,60,94,-81,61,TUFF)
    v.set(93,-80,61,S("grindstone",face="floor",facing="west"))
    for x in (82,88):
        v.set(x,-76,59,S("polished_tuff_slab",type="top"))
        v.set(x,-77,59,S("lantern",hanging=True))
    v.box(80,-76,52,94,-76,52,S("stripped_spruce_log",axis="x"))
    for x in (83,92):
        v.set(x,-77,52,S("iron_chain",axis="y"))
        v.set(x,-78,52,S("lantern",hanging=True))


def copper_kettle(v):
    # Narrow old-world inn facade; the copper barrel roof has a full back and
    # stepped gable ends. Recessed windows flank a warm open entry.
    for x in range(79,92):
        for z in range(116,125):
            floor = OAK if 79<x<91 and z>=117 else S("polished_blackstone_bricks")
            v.set(x,-68,z,floor)
    v.walls(79,-67,117,91,-62,124,S("mud_bricks"))
    for x in (79,84,91):
        for z in (117,124):
            v.box(x,-67,z,x,-62,z,TIMBER)
    for x in (80,81,85,86,88,89):
        v.box(x,-66,117,x,-64,117,S("yellow_stained_glass_pane"))
    for x in (82,83):
        v.box(x,-67,117,x,-64,117,AIR)
    for z in (119,120,122):
        v.box(79,-66,z,79,-64,z,S("orange_stained_glass_pane"))
    # Ridge parallel to the gallery. Its higher ribs and brown/copper bands
    # read as a brewhouse roof between the stone balconies.
    for z in range(116,125):
        tier = min(z-116,124-z)
        y = -62+tier
        for x in range(79,92):
            copper = PATINA if x in (79,85,91) else COPPER
            if tier < 4:
                cover = "waxed_weathered_cut_copper_stairs" if copper==PATINA else "waxed_exposed_cut_copper_stairs"
                v.set(x,y,z,S(cover,facing="south" if z<120 else "north"))
            else:
                v.set(x,y,z,copper)
            if x in (79,91) and tier:
                v.box(x,-62,z,x,y-1,z,OAK)
        if tier==4:
            v.box(80,y+1,z,90,y+1,z,S("waxed_exposed_cut_copper_slab",type="bottom"))
    # The back hearth has a brick chimney, lit firebox, iron grate and a
    # serviceable cauldron hung beneath the copper hood. No loose fire or lava.
    v.box(89,-67,122,90,-63,123,S("bricks"))
    v.set(89,-66,122,S("ochre_froglight"))
    v.set(89,-66,121,S("iron_bars"))
    v.set(90,-66,121,S("cauldron"))
    v.set(90,-65,121,S("iron_chain",axis="y"))
    v.box(89,-64,121,90,-64,121,BAND)
    v.box(90,-62,123,90,-58,123,S("bricks"))
    v.set(90,-57,123,S("brick_slab",type="bottom"))
    # Bartender's side stays open at the west end of the L counter. Guests
    # can stand opposite both merchants or walk into the seating bay.
    v.box(86,-67,119,89,-67,119,S("stripped_spruce_log",axis="x"))
    v.box(89,-67,119,89,-67,120,S("stripped_spruce_log",axis="z"))
    v.set(88,-66,119,S("flower_pot"))
    v.set(86,-66,119,S("lantern"))
    v.set(86,-67,123,S("crafting_table"))
    v.set(87,-67,123,OAK)
    v.box(86,-64,123,88,-64,123,S("spruce_slab",type="top"))
    v.set(87,-63,123,S("flower_pot"))
    # Four oak bench seats face the lamp-lit fireside table.
    for x in (80,82):
        v.set(x,-67,121,S("spruce_stairs",facing="west" if x==80 else "east"))
        v.set(x,-67,122,S("spruce_stairs",facing="west" if x==80 else "east"))
    v.box(81,-67,121,81,-67,122,S("dark_oak_slab",type="top"))
    v.set(81,-66,122,S("lantern"))
    # Hanging kettle on a bracket is a recognizable trade sign at street scale.
    v.set(90,-63,116,TIMBER)
    v.set(90,-64,116,S("iron_chain",axis="y"))
    v.set(90,-65,116,S("cauldron"))
    for x in (81,85):
        v.set(x,-63,116,S("lantern",hanging=True))


def structures():
    v = Voxels()
    stonewright(v)
    copper_kettle(v)
    return v


def resolve_overlay(base, desired):
    """Resolve neighbor shapes on a small scratch map. Only authored edits emit."""
    neighborhood = set()
    for x,y,z in desired.b:
        for dx,dy,dz in ((0,0,0),(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            neighborhood.add((x+dx,y+dy,z+dz))
    scene = Voxels()
    scene.b = {p:base[p] for p in neighborhood if p in base}
    absent = desired.b.keys()-base.keys()
    if absent:
        raise ValueError(f"Retail cell outside the installed base plan: {min(absent)}")
    scene.b.update(desired.b)
    connections(scene)
    stair_shapes(scene)
    # Keep the original guild fixtures, rails, piers and workshop structures.
    # Mark only deliberately replaced floor flags and empty gallery space.
    changes = []
    for p in sorted(desired.b, key=lambda q:(q[1],q[0],q[2])):
        before,after = base[p],scene.b[p]
        if before != after:
            if before != AIR and not (p[1] in (-82,-68) and before in
                                      (S("polished_deepslate"),S("stone_bricks"))):
                raise ValueError(f"Original fixture at {p}: {before} -> {after}")
            changes.append((p,before,after))
    return changes


def build_asset():
    v = structures()
    base_plan = json.loads(gzip.decompress((TOWN/"western_waterfront.json.gz").read_bytes()))
    palette = base_plan["palette"]
    bounds = [(a-1,b-1,c-1,d+1,e+1,f+1) for a,b,c,d,e,f in BOUNDS]
    base = {}
    for x,y,z,s in base_plan["cells"]:
        if any(a<=x<=d and b<=y<=e and c<=z<=f for a,b,c,d,e,f in bounds):
            base[x,y,z] = palette[s]
    changes = resolve_overlay(base,v)
    states = sorted({state for _,before,after in changes for state in (before,after)})
    indices = {s:i for i,s in enumerate(states)}
    return {"version":1,"palette":states,
            "cells":[[*p,indices[before],indices[after]] for p,before,after in changes],
            "shops":list(SHOPS)}


if __name__ == "__main__":
    plan = build_asset()
    raw = json.dumps(plan,separators=(",",":")).encode()
    packed = bytearray(gzip.compress(raw,mtime=0))
    packed[9] = 255
    (TOWN/"deepstead_guild_shops.json.gz").write_bytes(packed)
    print(f"{len(plan['cells'])} changed cells, {len(plan['palette'])} states")
