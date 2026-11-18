"""A hand-placeable quay market. Origin is the NW corner of the deck, water to the north."""
from functools import lru_cache
from pathlib import Path
from lair_layout import block, template, write

SIZE = (23, 9, 22)
ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/data/jugcraft/structure/holiday/west_gate_winter_market.nbt"
state = lru_cache(maxsize=None)(block)


def market():
    cells = {}

    def put(x, y, z, material, **props):
        if (x,y,z) in cells:
            raise ValueError(f"Overlapping market pieces at {(x,y,z)}: {material}, {cells[x,y,z]}")
        if not all(0 <= n < limit for n, limit in zip((x,y,z), SIZE)):
            raise ValueError(f"Outside the market template at {(x,y,z)}")
        cells[x,y,z] = state(material if ":" in material else "minecraft:"+material, **props)

    for z in range(22):
        for x in range(23):
            crossbeam = z % 6 == 0
            if z <= 2:
                material = "stripped_spruce_log" if crossbeam else "spruce_planks"
            elif x in (7,15) or z == 21:
                material = "polished_andesite"
            elif 8 <= x <= 14:
                material = "stone_bricks" if (x+2*z)%9 in (0,2) else "andesite"
            else:
                material = "spruce_planks" if (z+x//4)%7 else "dark_oak_planks"
            put(x,0,z,material,**({"axis":"x"} if material.endswith("_log") else {}))

    def stall(front, z0, outward, color, trade):
        # u is shop frontage and v runs back from the serving counter.
        def p(u,y,v,name,**props):
            put(front + outward*v, y, z0+u, name, **props)
        facing = "east" if outward < 0 else "west"
        for u in range(7):
            for v in (0,4):
                if u in (0,6):
                    for y in range(1,5):
                        p(u,y,v,"stripped_spruce_log",axis="y")
                elif v == 4:
                    p(u,1,v,"dark_oak_planks")
                    p(u,2,v,"spruce_planks")
                    if u in (1,3,5):
                        p(u,3,v,"spruce_planks")
            if u in (0,6):
                for v in range(1,4):
                    if u == 0 or v != 2:
                        p(u,1,v,"spruce_planks")
            elif 1 <= u <= 5:
                if u == 3:
                    p(u,1,0,"jugcraft:"+trade,facing=facing)
                else:
                    p(u,1,0,"stripped_dark_oak_log",axis="z")
            for v in range(-1,6):
                if v in (-1,5):
                    p(u,4,v,"spruce_stairs",facing="west" if (v==-1)==(outward<0) else "east")
                elif v in (0,4):
                    if u not in (0,6):
                        p(u,4,v,"stripped_spruce_log",axis="z")
                    p(u,5,v,"spruce_slab",type="bottom")
                else:
                    rise = 6 if v==2 else 5
                    p(u,rise,v,"dark_oak_planks" if v==2 else "spruce_planks")
                    if (u+2*v)%5 != 0:
                        p(u,rise+1,v,"snow",layers="2" if v==2 else "1")
            if 1 <= u <= 5:
                p(u,3,-1,color+"_wool" if u in (1,3,5) else "white_wool")
        p(0,3,-1,"jugcraft:eave_icicle_fringe",facing=facing)
        p(6,3,-1,"jugcraft:eave_icicle_fringe",facing=facing)
        p(3,4,2,"lantern",hanging="true")
        p(3,5,2,"iron_chain",axis="y")
        displays = {
            "winter_wrapping_counter": ("holidayworks_couple_red_presents","folded_linen_stack"),
            "winter_bakery_counter": ("copper_samovar","braided_bread_basket"),
            "winter_toy_counter": ("spinning_top","mayflower_model_ship"),
        }
        for u, display in zip((1,5), displays[trade]):
            p(u,2,0,"jugcraft:"+display,facing=facing,**({"setting":"1"} if display=="copper_samovar" else {}))
        # Shelving faces the serving hatch. The rear wall and the side entrance stay distinct.
        p(1,2,3,"spruce_slab",type="top")
        p(5,2,3,"spruce_slab",type="top")
        if trade=="winter_wrapping_counter":
            for u in (1,3,5):
                p(u,1,3,"chiseled_bookshelf",facing=facing)
        elif trade=="winter_bakery_counter":
            p(2,1,3,"smoker",facing=facing)
            p(4,1,3,"jugcraft:cider_barrel",facing=facing)
        else:
            p(2,1,3,"barrel",facing="up")
            p(2,2,3,"jugcraft:turkey_pull_toy",facing=facing)
        p(3,3,0,"jugcraft:holidayworks_wreath_wall_deco_1",facing=facing)
        for u in (1,3,5):
            p(u,3,3,"jugcraft:holidayworks_yellow_string_lights",facing=facing,twinkle="false")
    stall(6,4,-1,"green","winter_wrapping_counter")
    stall(16,10,1,"red","winter_bakery_counter")
    stall(6,14,-1,"blue","winter_toy_counter")

    floor_ring = [(0,0),(-1,-1),(0,-1),(1,-1),(1,0),(1,1),(0,1),(-1,1),(-1,0)]
    part = 0
    for y in range(5):
        for dx,dz in floor_ring:
            if (y == 3 and dx and dz) or (y == 4 and (dx or dz)):
                continue
            put(15+dx,1+y,3+dz,"jugcraft:harbour_gift_fir",facing="north",part=part)
            part += 1
    assert part == 33

    def lamp(x,z,tall,east):
        heights = 5 if tall else 3
        offsets = [(0,y) for y in range(heights)] + [(1,heights-2),(1,heights-1)]
        for part,(across,up) in enumerate(offsets):
            put(x+(across if east else -across),up+1,z,
                "jugcraft:peppermint_boulevard_lamp" if tall else "jugcraft:peppermint_lane_lamp",
                facing="south" if east else "north",lit=True,part=part)
    lamp(8,21,True,True)
    lamp(15,21,True,False)
    lamp(7,12,False,True)
    lamp(17,8,False,False)
    for x,z,high,face in [(1,12,1,"east"),(4,11,0,"west"),(18,8,0,"north"),(20,9,1,"east"),
                           (2,13,1,"north"),(0,17,0,"east"),(20,3,0,"west"),(0,2,1,"south")]:
        put(x,1,z,"jugcraft:shovelled_snow_pile" if high else "jugcraft:wind_carved_snowdrift",facing=face)
    for x in (1,5,17,21):
        put(x,1,0,"stripped_dark_oak_log",axis="y")
        put(x,2,0,"dark_oak_slab",type="bottom")
    # The turned barrels and rope hook tie the market to the working quay.
    put(2,1,1,"barrel",facing="east")
    put(3,1,1,"barrel",facing="up")
    put(3,2,1,"lantern")
    put(19,1,1,"barrel",facing="south")
    for x,z,layers in [(0,3,2),(1,3,1),(0,4,1),(2,12,2),(3,12,1),(20,8,1),
                       (21,8,2),(21,9,1),(20,4,2),(21,4,1),(0,15,1),(0,16,2)]:
        put(x,1,z,"snow",layers=layers)
    assert not any(y in (1,2) and 9 <= x <= 13 for x,y,z in cells)
    # Check the oblique sightline as well as the open central lane. A lamp can mask
    # the fir from the entrance even when pedestrians can walk straight to the water.
    for step in range(1,90):
        t = step / 100
        x, z = int(11.8 + (15.5-11.8)*t), int(25 + (3.5-25)*t)
        assert all((x,y,z) not in cells for y in (2,3))
    return cells


def market_template():
    result = template(market(),SIZE)
    result["entities"] = [
        {"pos":[x+.5,1.,z+.5],"blockPos":[x,1,z],"nbt":{
            "id":"minecraft:villager","Tags":["winter_market_"+role],"CustomName":{"text":name},
            "NoAI":True,"PersistenceRequired":True,"Invulnerable":True,"Rotation":[90. if role=="bakery" else -90.,0.],
            "VillagerData":{"type":"minecraft:snow","profession":"minecraft:"+job,"level":2}}}
        for x,z,role,name,job in ((5,7,"post","Inga the Parcel Clerk","librarian"),
                                  (17,13,"bakery","Oskar the Baker","farmer"),
                                  (5,17,"toys","Marta the Toymaker","toolsmith"))]
    return result


if __name__ == "__main__":
    write(DEST,market_template())
