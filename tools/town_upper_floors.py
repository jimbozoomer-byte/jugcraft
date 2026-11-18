"""Raise four existing Old Town roofs by one storey, using their own timbers, windows and chimneys.

Read the actual shipped revision so no layout, resident or RNG stream changes.
The runtime checks an entire roof and its new interior before touching an old house.
"""
import base64
import gzip
import json
from pathlib import Path
from town_jetties import build_jetty

ROOT = Path(__file__).resolve().parents[1]
TOWN = ROOT / "src/main/resources/data/jugcraft/town"
LIFT = "jugcraft:counterweight_lift"
# id, roof rectangle (including eaves), old attic deck, original ladder column.
HOUSES = [
    ("west_gate_house", (31, 97, 41, 109), 15, (39, 107)),
    ("south_square_inn", (84, 115, 95, 126), 15, (86, 124)),
    ("west_close_cottage", (37, 117, 48, 125), 10, (39, 123)),
    ("south_lane_house", (75, 123, 83, 133), 10, (77, 125)),
]


def build():
    blueprint = json.loads(gzip.decompress((TOWN / "town.json.gz").read_bytes()))
    size, low = blueprint["size"], blueprint["y_min"]
    blocks = memoryview(base64.b64decode(blueprint["blocks"]))
    palette = blueprint["palette"]

    def old(x, y, z):
        index = ((y - low) * size * size + z * size + x) * 2
        if not (0 <= x < size and 0 <= z < size and low <= y < low + blueprint["height"]):
            return "minecraft:air"
        code = int.from_bytes(blocks[index:index+2], "little")
        return palette[code] if code < len(palette) else "minecraft:air"

    out_palette = []
    houses = []
    jetties = []
    for name, (x0, z0, x1, z1), deck, (sx, sz) in HOUSES:
        def roof_positions(y):
            return ((x, y, z) for x in range(x0, x1+1) for z in range(z0, z1+1))

        top = max(y for y in range(deck, low + blueprint["height"])
                  if any(old(*p) != "minecraft:air" for p in roof_positions(y)))
        desired = {}
        chimney = {(x,z) for x,_,z in roof_positions(deck)
                   if any("campfire" in old(x,yy,z) for yy in range(deck,top+1))}
        for y in range(deck, top+6):
            for x, _, z in roof_positions(y):
                # Keep the original eaves and chimney cap intact, five blocks higher.
                desired[x, y, z] = old(x, y-5, z)
                if y < deck+5:
                    if (x,z) in chimney:
                        desired[x, y, z] = old(x,deck+1,z)
                    elif "minecraft:glass_pane" in desired[x,y,z] and "glass_pane" not in old(x,y-4,z):
                        # A leaded transom on the top light distinguishes the new storey.
                        desired[x,y,z] = desired[x,y,z].replace("minecraft:glass_pane","minecraft:light_gray_stained_glass_pane")
        # The lift follows the original ladder; all its stops are reachable from the landing.
        assert "ladder" in old(sx, 2, sz)
        for y in range(deck+4):
            desired[sx, y, sz] = LIFT if y % 5 == 0 else "minecraft:air"
        cells = []
        for (x,y,z), state in sorted(desired.items(),key=lambda e:(e[0][1],e[0][2],e[0][0])):
            if state == old(x,y,z):
                continue
            if state not in out_palette:
                out_palette.append(state)
            cells.append([x,y,z,out_palette.index(state)])
        houses.append({"id":name, "roof":[x0,deck,z0,x1,top+5,z1], "lift":[sx,sz], "cells":cells})
        if name in ("west_gate_house","south_square_inn"):
            def baseline(x,y,z):
                return desired.get((x,y,z),old(x,y,z))
            rise, crown = build_jetty(name,(x0,deck+5,z0,x1,top+5,z1),(sx,sz),baseline)
            cells = []
            for (x,y,z),state in sorted(crown.items(),key=lambda e:(e[0][1],e[0][2],e[0][0])):
                # Keep empty room cells in the survey: an occupied bay must not be built around a chest.
                if state not in out_palette:
                    out_palette.append(state)
                cells.append([x,y,z,out_palette.index(state)])
            jetties.append({"id":name+"_jetties","base":name,"rise":rise,
                            "roof":[x0,deck+5,z0,x1,top+5+rise,z1],"cells":cells})
    return {"palette":out_palette, "houses":houses, "jetties":jetties}


if __name__ == "__main__":
    plan = build()
    source = json.dumps(plan, separators=(",",":")).encode()
    packed = bytearray(gzip.compress(source, mtime=0))
    packed[9] = 255
    (TOWN / "old_town_upper_floors.json.gz").write_bytes(packed)
    for house in plan["houses"]:
        print(house["id"], len(house["cells"]), "changed cells")
