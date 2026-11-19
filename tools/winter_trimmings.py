"""Generate wind-carved and shovelled snow profiles and the solid icicle fringe."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ART = RES / "assets/jugcraft"
DATA = RES / "data/jugcraft"
JAVA = ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/agriculture/WinterTrimmings.java"
FACES = ("west", "east", "north", "south", "up", "down")
LANGUAGE = {
    "block.jugcraft.peppermint_lane_lamp": "Peppermint Lane Lamp",
    "block.jugcraft.peppermint_boulevard_lamp": "Peppermint Boulevard Lamp",
    "block.jugcraft.harbour_gift_fir": "Harbour Gift Fir",
    "block.jugcraft.civic_gift_skirt": "City Christmas Tree Gifts",
    "message.jugcraft.gift_fir.collection_only": "Christmas is over. Please collect the remaining gifts.",
    "item.jugcraft.harbour_gift_fir.space": "Needs a clear 3 × 3 space, five blocks high.",
    "item.jugcraft.harbour_gift_fir.giving": "Crouch and use to leave a stack for anyone.",
    "item.jugcraft.harbour_gift_fir.taking": "Use an empty hand to unwrap a gift.",
    "block.jugcraft.wind_carved_snowdrift": "Wind-Carved Snowdrift",
    "block.jugcraft.shovelled_snow_pile": "Shovelled Snow Pile",
    "block.jugcraft.eave_icicle_fringe": "Eave Icicle Fringe",
    "message.jugcraft.gift_fir.offer": "Crouch and use to wrap this entire stack for anyone.",
    "message.jugcraft.gift_fir.full": "This gift pile is full. Try another side of the tree.",
    "message.jugcraft.gift_fir.wrapped": "You left a wrapped gift for someone to find.",
    "message.jugcraft.gift_fir.empty": "No gifts here yet. Crouch and use with a stack to leave one.",
    "message.jugcraft.gift_fir.received": "You unwrapped %s.",
}


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def box(start, end, tex, faces=FACES):
    return {"from": start, "to": end, "faces": {face: {"texture": "#" + tex} for face in faces}}


def bank(profile):
    source = re.search(rf"String\[\] {profile} = \{{([^}}]+)\}}", JAVA.read_text()).group(1)
    heights = re.findall(r'"([0-9]{8})"', source)
    assert len(heights) == 8
    result = []
    # Only draw exposed portions. Continuous UV coordinates follow the original snow texture across strips.
    for z, row in enumerate(heights):
        for x, digit in enumerate(row):
            height = int(digit)
            if not height:
                continue
            start = [2*x, 0, 2*z]
            end = [2*x + 2, height, 2*z + 2]
            result.append(box(start, end, "snow", ["up", "down"]))
            for face, dx, dz in (("west",-1,0), ("east",1,0), ("north",0,-1), ("south",0,1)):
                nx, nz = x+dx, z+dz
                adjacent = int(heights[nz][nx]) if 0 <= nx < 8 and 0 <= nz < 8 else 0
                if height > adjacent:
                    result.append(box([start[0], adjacent, start[2]], end, "snow", [face]))
    return result


def fringe():
    # A wind-packed lip with five unequal teeth. Offset cores catch skylight from different angles.
    pieces = [box([0,14,4],[16,16,8],"ice"), box([1,13,5],[7,14,9],"ice"),
              box([6,14,7],[15,16,10],"ice"), box([0,15,3],[12,16,4],"snow")]
    for x, z, width, bottom in [(0,4,3,7), (3,5,4,1), (7.5,6,2.5,9), (10,4,3,4), (13,6,2.5,8)]:
        shoulder = bottom+(14-bottom)*.5
        pieces.extend([box([x,shoulder,z],[x+width,14,z+width],"ice"),
                       box([x+.5,bottom+2,z+.5],[x+width-.5,shoulder,z+width-.5],"ice"),
                       box([x+width/2-.4,bottom,z+width/2-.4],[x+width/2+.4,bottom+2,z+width/2+.4],"ice")])
    return pieces


def generate():
    for name, profile, ingredients, count in (
        ("wind_carved_snowdrift", "DRIFT", ["minecraft:snowball"]*2, 2),
        ("shovelled_snow_pile", "PILE", ["minecraft:snowball"]*3, 2),
        ("eave_icicle_fringe", None, ["minecraft:ice","minecraft:snowball"], 4),
    ):
        model = "jugcraft:block/" + name
        item = "jugcraft:" + name
        textures = {"particle": "minecraft:block/snow", "snow":"minecraft:block/snow"}
        if not profile:
            textures.update(particle="minecraft:block/packed_ice", ice="minecraft:block/packed_ice")
        save(ART/"models/block"/(name+".json"), {"parent":"minecraft:block/block","ambientocclusion":False,"textures":textures,
                "elements":bank(profile) if profile else fringe()})
        save(ART/"items"/(name+".json"), {"model":{"type":"minecraft:model","model":model}})
        save(ART/"blockstates"/(name+".json"), {"variants":{"facing="+d:{"model":model,**({"y":i*90} if i else {})}
                for i,d in enumerate(("north","east","south","west"))}})
        save(DATA/"recipe"/(name+".json"), {"type":"minecraft:crafting_shapeless","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"agriculture"}],"category":"building",
                "ingredients":ingredients,"result":{"id":item,"count":count}})
        save(DATA/"advancement/recipes/building"/(name+".json"), {"parent":"minecraft:recipes/root",
                "criteria":{"material":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"items":ingredients[0]}]}}},
                "rewards":{"recipes":[item]}})
        save(DATA/"loot_table/blocks"/(name+".json"), {"type":"minecraft:block","pools":[{"rolls":1,
                "condition":{"type":"minecraft:survives_explosion"},"entries":[{"type":"minecraft:item","name":item}]}]})


if __name__ == "__main__":
    generate()
