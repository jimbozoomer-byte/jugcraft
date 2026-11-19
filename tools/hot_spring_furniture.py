"""Icelandic bathhouse sets. Run from any directory with python tools/hot_spring_furniture.py.
Each join, slat, stepped stone or cloth fold is a solid. The shared homestead atlas and painters
assign roughly one texel per model pixel. North is the front. Dimensions are in model pixels.
"""
import json
from pathlib import Path

import flora_art as art
from asset_png import save_rgba
from model_writer import finish_closed
from pumpkin_patch_data import flat, lengthwise, panel
from thanksgiving_homestead_data import ring, floor, wool

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD = "jugcraft"
A = ROOT / "assets" / MOD
D = ROOT / "data" / MOD
P = "hot_spring_"

DRIFT = art.pal("463b32", "715740", "987958", "b29771", "c8b693")
ASH = art.pal("493e36", "79634e", "a28665", "c0a47f", "d9c7a4")
BARK = art.pal("2b2928", "453930", "645143", "876f54", "998465")
ROCK = art.pal("292b31", "3c4147", "4e575c", "697274", "838d8b")
COPPER = art.pal("482f2a", "72503b", "a16e48", "c09161", "d9ae78")
PATINA = art.pal("2b4646", "3e615e", "58817a", "7c9a87", "a9bb9f")
CREAM = art.pal("615953", "877c6c", "b6aa91", "d5cbb1", "e7dfc7")
SEA = art.pal("283e43", "38575c", "4d7071", "688c85", "90aca0")
BOOT = art.pal("392521", "59382e", "79513b", "966f4b", "b08d69")
WATER = art.pal("315d63", "477882", "66a4ab", "9fc5be", "c4dfd1")
EMBER = art.pal("7a4626", "b36b38", "dfa24c", "edc47a", "f7df9b")


def knit(p):
    """Crossed sea-green stitches between two hems on the ends of cream towels."""
    for y in range(p.h):
        hem = p.h - y - 1
        for x in range(p.w):
            colour = art.shade(CREAM, 3 if x % 4 < 3 else 2)
            if hem in (1, 5):
                colour = art.shade(SEA, 2)
            elif 2 <= hem <= 4 and min(x % 6, 6 - x % 6) == hem - 2:
                colour = art.shade(SEA, 3)
            p.put(x, y, colour)


knit.tag = "bath_towel_diamond_hem"


def rock(p):
    for y in range(p.h):
        for x in range(p.w):
            band = 2 if (x // 2 + y // 7) % 4 else 1
            if x == 0 or y == 0:
                band = 3
            elif (x * 3 + y * 7) % 23 == 0:
                band = min(4, band + 1)
            p.put(x, y, art.shade(ROCK, band))


rock.tag = "columnar_basalt"


def sawn(palette):
    """Long wood fibres split and rejoin at the knot, leaving a quieter seating surface."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                run, lane = (x, y) if along_x else (y, x)
                shade = 2
                if lane == 0:
                    shade = 3
                elif lane == across - 1 and across > 2:
                    shade = 1 if run % 9 < 6 else 2
                elif (run + 5 * lane) % 15 in (2, 3, 4, 5):
                    shade = 1
                elif (run + 3 * lane) % 15 in (9, 10, 11):
                    shade = 3
                p.put(x, y, art.shade(palette,shade))
    paint.tag = repr(("springhouse_driftwood", palette))
    return paint


def opal(p):
    """Frosted honey glass, dark at the ferrule with a narrow reflected highlight."""
    for y in range(p.h):
        for x in range(p.w):
            shade = 2
            if y == p.h-1:
                shade = 1
            elif x == 0 or (x == 1 and y < p.h//2):
                shade = 4
            elif (x + y//2) % 5 == 1:
                shade = 3
            p.put(x, y, art.shade(EMBER,shade))


opal.tag = "honey_opal"


def stream(p):
    for y in range(p.h):
        for x in range(p.w):
            k = 3 if x == 0 and y % 6 < 4 else 2
            if x == p.w-1 and p.w > 1:
                k = 1 if y % 5 < 3 else 2
            p.put(x, y, art.shade(WATER,k))


stream.tag = "spring_water"


def wicker(p):
    for y in range(p.h):
        for x in range(p.w):
            offset = x + 2 * (y % 2)
            k = (2, 3, 2, 1)[offset % 4] if p.w > 2 else 2
            p.put(x, y, art.shade(DRIFT,k))


wicker.tag = "bath_hamper_willow"


def paint_box(sc, parts, name, paint, lo, hi, **faces):
    parts.append(panel(sc, name, paint, lo, hi, **faces))


def bench(sc):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc, e, name, mat, lo, hi)
    for x in (1, 12):
        for z in (3, 10):
            b("basalt_foot", rock, (x,0,z), (x+3,1.5,z+2))
            b("splayed_trestle", lengthwise(BARK), (x+.4,1.5,z), (x+2.6,6,z+2))
        b("end_rail", lengthwise(DRIFT), (x,5,2.4), (x+3,6.5,12.6))
    b("centre_stretcher", lengthwise(BARK), (2,2.5,7), (14,4,8.5))
    for x in (2,12):
        b("tenon", lengthwise(DRIFT), (x,2.5,4.8), (x+2,4,10.2))
    for z in (2,5.9,9.8):
        b("rounded_slat", sawn(ASH), (0,6.5,z), (16,8,z+3.2))
    # The narrower folded hand towel hangs clear of the sitting position.
    b("towel_top", wool(CREAM,False), (.6,8,2), (4.8,8.65,9))
    paint_box(sc,e,"towel_fold",wool(CREAM,False),(.6,4.8,1.35),(4.8,8.65,2),north=knit)
    return e


def peg_rail(sc):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    b("rail",sawn(DRIFT),(.3,11.5,14.3),(15.7,14.6,16))
    b("upper_bead",lengthwise(ASH),(0,14.6,14),(16,15.3,16))
    for x in (1.3,7.5,13.7):
        b("socket",flat(COPPER),(x-.7,12.1,13.8),(x+.7,13.7,14.4))
        b("peg",lengthwise(ASH),(x-.45,12.65,11),(x+.45,13.35,14))
        b("peg_tip",lengthwise(BARK),(x-.65,12.45,10.65),(x+.65,13.55,11.3))
    # Two towels hang from loops, leaving the third peg free.
    for x,w,bottom,mat in ((1.3,5,4.6,SEA),(7.5,6.1,.8,CREAM)):
        b("hanging_loop",wool(mat,False),(x-.35,11.1,11.05),(x+.35,13.6,12))
        left = max(.15,x-1.2)
        for j in range(3):
            z = 10.25 if j == 1 else 10.8
            lo = bottom + (.45 if j == 2 else 0)
            paint_box(sc,e,"terry_fold",wool(mat,False),(left+j*w/3,lo,z),
                      (left+(j+1)*w/3,10.85,12.2),north=knit if mat is CREAM else wool(SEA))
        b("towel_neck",wool(mat,False),(left+.65,10.85,10.9),(left+w-.8,11.55,12.2))
    return e


def boot_tray(sc):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    for z in (3,6,9,12):
        b("drain_slat",sawn(DRIFT),(0,.6,z),(16,1.5,z+1.6))
    for x in (.75,13.25):
        b("runner",lengthwise(BARK),(x,0,2.5),(x+2,.6,14))
    for x in (0,15.1):
        b("lip",lengthwise(ASH),(x,1.5,2.5),(x+.9,2.2,14))
    for x,z in ((2.25,4),(8.5,3)):
        b("sole",flat(BARK),(x,1.5,z),(x+4.1,2.25,z+8.1))
        b("welt",flat(COPPER),(x+.15,2.25,z+.2),(x+3.95,2.65,z+7.9))
        b("toe",flat(BOOT),(x+.3,2.65,z+.25),(x+3.8,3.8,z+3))
        b("vamp",flat(BOOT),(x+.3,2.65,z+3),(x+3.8,4.65,z+4.6))
        # Open shaft and folded wool lining; no painted-on cavity.
        for y0,y1,t,mat in ((2.65,6.4,.65,flat(BOOT)),(6.4,7.1,.65,wool(CREAM,False))):
            for sx in (x+.25,x+3.85-t):
                b("boot_side",mat,(sx,y0,z+4.6),(sx+t,y1,z+7.95))
            for sz in (z+4.6,z+7.95-t):
                b("boot_heel",mat,(x+.9,y0,sz),(x+3.2,y1,sz+t))
        b("pull_tab",flat(BOOT),(x+1.5,6.3,z+7.45),(x+2.55,7.9,z+7.9))
        for sz in (3.3,4.0):
            b("laces",lengthwise(CREAM),(x+1.05,4.65,z+sz),(x+3.1,4.95,z+sz+.35))
    return e


def spout(sc, state="closed"):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    b("trough_foot",rock,(2,0,.5),(14,1.5,15.7))
    for x in (2,12):
        b("trough_rim",rock,(x,1.5,1.25),(x+2,2.8,14.7))
    b("front_lip",rock,(4,1.5,.5),(12,2.1,2))
    # Cluster of unequal basalt prisms, stepped twice at the end to read as cut columns.
    for x,z,w,h in ((4,9,5.3,11.6),(8.2,10.4,4.4,13),(9,6.8,3.5,7.25),(3.15,7.8,3,8.5)):
        b("basalt_face",rock,(x,1.5,z+.65),(x+w,h,z+w-.65))
        b("basalt_facet",rock,(x+.65,1.5,z),(x+w-.65,h,z+.65))
        b("basalt_facet",rock,(x+.65,1.5,z+w-.65),(x+w-.65,h,z+w))
    b("pipe_cuff",flat(COPPER),(5.65,7.65,8.55),(9.3,10.8,9.8))
    b("spigot",flat(PATINA),(6.1,8.2,3.4),(8.9,10.2,8.55))
    b("turned_mouth",flat(COPPER),(6.05,7.35,3.25),(8.95,8.2,5.7))
    b("valve_stem",flat(COPPER),(7,10.2,6),(8,11.75,7))
    if state == "closed":
        b("valve_lever",flat(PATINA),(5.55,11.75,5.9),(9.5,12.6,7.1))
    else:
        b("valve_lever",flat(PATINA),(6.95,11.75,4.5),(8.15,12.6,8.4))
    if state == "flowing":
        b("water_in_trough",stream,(4,1.5,2),(12,1.8,8))
        b("falling_water",stream,(6.55,1.8,3.7),(8.45,7.35,5.25))
    return e


def pail(sc):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    for y0,y1,d in ((.5,2.5,8),(2.5,5.5,9),(5.5,8.1,10)):
        e += ring(sc,"coopered_staves",lengthwise(DRIFT),lengthwise(ASH),7,9,d,y0,y1,t=1,c=1.5)
    e.append(panel(sc,"pail_sole",lengthwise(BARK),(4.4,0,6.4),(9.6,.5,11.6)))
    e += floor(sc,"floor",lengthwise(ASH),7,9,8,.5,1,t=.8,c=1.5)
    for y,d in ((2,8.4),(6.5,10.5)):
        e += ring(sc,"hoop",flat(PATINA),flat(PATINA),7,9,d,y,y+.65,t=.45,c=1.5)
    for x in (2.25,11.25):
        b("handle_stile",lengthwise(ASH),(x,7,8.45),(x+.5,12,9.65))
        b("handle_knee",lengthwise(ASH),(x,12,8.45),(x+(1.75 if x<7 else .5),12.7,9.65))
    b("handle_grip",lengthwise(BARK),(3.5,12.25,8.4),(11.4,13.05,9.7))
    # The ladle rests across the rim, with an open bowl to its right.
    b("dipper_shaft",lengthwise(ASH),(2.8,8.1,5.85),(12.3,8.85,6.75))
    e += ring(sc,"ladle_bowl",flat(ASH),flat(DRIFT),13.2,6.3,3.75,8.1,9.45,t=.55,c=.9)
    e += floor(sc,"ladle_bowl_floor",flat(DRIFT),13.2,6.3,3.75,7.65,8.15,t=.45,c=.9)
    return e


def pool_lantern(sc):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    def step(name,mat,rad,y0,y1):
        # Three abutting bars form an eight-sided stone or cap course.
        b(name,mat,(8-rad,y0,9-rad),(8+rad,y1,7+rad))
        b(name,mat,(9-rad,y0,8-rad),(7+rad,y1,9-rad))
        b(name,mat,(9-rad,y0,7+rad),(7+rad,y1,8+rad))
    step("plinth",rock,5.7,0,2)
    step("pedestal",rock,4.2,2,3.4)
    step("copper_sill",flat(COPPER),4.35,3.4,4.15)
    step("amber_core",opal,2.75,4.15,11.4)
    for x in (4,10.9):
        for z in (4,10.9):
            b("frame_post",flat(PATINA),(x,4.15,z),(x+1.1,11.4,z+1.1))
    for y in (5,10.1):
        e += ring(sc,"gallery",flat(COPPER),flat(COPPER),8,8,8.2,y,y+.55,t=.6,c=.75)
    step("stone_eave",rock,5.8,11.4,12.65)
    step("cap",flat(PATINA),4.8,12.65,13.65)
    step("cap",flat(PATINA),3.6,13.65,14.4)
    b("finial",flat(COPPER),(7,14.4,7),(9,15.8,9))
    return e


def wrapped_kit(sc, pool):
    e = []
    b = lambda name, mat, lo, hi: paint_box(sc,e,name,mat,lo,hi)
    if pool:
        for y0,y1,d in ((0,2,9),(2,6,10.5),(6,9,12)):
            e += ring(sc,"hamper",wicker,wicker,8,8,d,y0,y1,t=1,c=1.75)
        e += floor(sc,"cover",knit,8,8,12,8,8.8,t=.9,c=1.75)
        for x in (2,13):
            b("handle",lengthwise(BARK),(x,8.5,7.5),(x+1,13.3,8.5))
        b("handle",lengthwise(ASH),(2,13.3,7.5),(14,14.3,8.5))
    else:
        b("crate",sawn(DRIFT),(1,1,3),(15,8,13))
        for x in (2.4,11.8):
            for z in (2.5,12.95):
                b("rope",lengthwise(CREAM),(x,1,z),(x+1.3,8.9,z+.55))
            b("strap",lengthwise(CREAM),(x,8.6,2.5),(x+1.3,9.25,13.5))
        paint_box(sc,e,"folded_linen",wool(SEA,False),(1.4,8,3.2),(14.6,8.65,12.8),north=knit)
        b("tag",flat(CREAM),(6.3,4.1,2.55),(9.7,6.75,3))
    return e


PIECES = {
    "bench": ("Slatted Springhouse Bench", bench, "axe"),
    "peg_rail": ("Springhouse Towel Pegs", peg_rail, "axe"),
    "boot_tray": ("Springhouse Boot Tray", boot_tray, "axe"),
    "overflow_spout": ("Basalt Spring Spout", spout, "pickaxe"),
    "dipper_pail": ("Copper-Bound Dipper Pail", pail, "axe"),
    "pool_lantern": ("Basalt Pool Lantern", pool_lantern, "pickaxe"),
}
KITS = {"changing_kit": ("Icelandic Changing Room Kit", ("bench","peg_rail","boot_tray")),
        "pool_kit": ("Icelandic Poolside Kit", ("overflow_spout","dipper_pail","pool_lantern"))}
KIT_RECIPES = {
    "changing_kit": (["W W","LSL","BHB"], {"W":"#minecraft:wool","L":"#minecraft:logs","S":"minecraft:string",
                                            "B":"minecraft:polished_basalt","H":"minecraft:leather"}),
    "pool_kit": (["CLC","BWB","BCB"], {"C":"minecraft:copper_ingot","L":"minecraft:lantern","W":"#minecraft:logs",
                                       "B":"minecraft:polished_basalt"}),
}
RECIPES = {
    "bench": (["SSS","P P"], {"S":"#minecraft:wooden_slabs","P":"minecraft:polished_basalt"}),
    "peg_rail": (["PSP","W W"], {"S":"minecraft:stick","P":"#minecraft:planks","W":"#minecraft:wool"}),
    "boot_tray": (["L L","SSS"], {"L":"minecraft:leather","S":"#minecraft:wooden_slabs"}),
    "overflow_spout": ([" C ","BCC","BBB"], {"B":"minecraft:polished_basalt","C":"minecraft:copper_ingot"}),
    "dipper_pail": ([" S ","P P","PCP"], {"S":"minecraft:stick","P":"#minecraft:planks","C":"minecraft:copper_ingot"}),
    "pool_lantern": ([" B ","CLC","BBB"], {"B":"minecraft:polished_basalt","C":"minecraft:copper_ingot","L":"minecraft:lantern"}),
}


def rid(key):
    return MOD + ":" + P + key


def language():
    entries = {"block.jugcraft."+P+k:v[0] for k,v in PIECES.items()}
    entries.update({"item.jugcraft."+P+k:v[0] for k,v in KITS.items()})
    entries.update({
        "item.jugcraft.hot_spring_kit.open": "Use to unpack all three furnishings.",
        "item.jugcraft.hot_spring_kit.full": "Make room for all three furnishings before unpacking the kit.",
        "block.jugcraft.hot_spring_overflow_spout.dry": "Valve open. Supply source water directly behind the spout.",
    })
    return entries


def write_all(write, assets, data, lang):
    texture_dir = assets / "textures/block"
    texture_dir.mkdir(parents=True, exist_ok=True)
    lang.update(language())
    for key in (*PIECES, *KITS):
        name = P + key
        sc = art.Sculpt(name, 1873, 128)
        versions = ("closed","dry","flowing") if key == "overflow_spout" else ("base",)
        for variant in versions:
            filename = name if variant in ("base","closed") else name + "_" + variant
            if key in KITS:
                elements = wrapped_kit(sc, key == "pool_kit")
            elif key == "overflow_spout":
                elements = spout(sc, variant)
            else:
                elements = PIECES[key][1](sc)
            finish_closed(elements)
            model = art.model(name, elements)
            write(assets / "models/block" / (filename + ".json"), model)
        save_rgba(sc.atlas.img, texture_dir / (name + ".png"))
        item_view = dict(art.PLANT_DISPLAY)
        item_view["gui"] = {"rotation":[26,218,0], "translation":[0,2,0], "scale":[.91,.91,.91]}
        if key in ("boot_tray","dipper_pail","bench"):
            item_view["gui"]["translation"] = [0,2.5,0]
        write(assets/"models/item"/(name+".json"), {"parent":MOD+":block/"+name,"display":item_view})
        write(assets/"items"/(name+".json"), {"model":{"type":"minecraft:model","model":MOD+":item/"+name}})
        if key in KITS:
            pattern, ingredients = KIT_RECIPES[key]
            write(data/"recipe"/(name+".json"), {"type":"minecraft:crafting_shaped","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"town"}],"category":"building",
                "pattern":pattern,"key":ingredients,"result":{"id":rid(key)}})
            write(data/"recipe"/(name+"_repack.json"), {"type":"minecraft:crafting_shapeless","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"town"}],"category":"building",
                "ingredients":[rid(k) for k in KITS[key][1]], "result":{"id":rid(key)}})
            continue
        variants = {}
        for facing, y in (("north",0),("east",90),("south",180),("west",270)):
            options = {"facing="+facing:name}
            if key == "overflow_spout":
                options = {f"facing={facing},flowing={flow},open={opened}":name+suffix
                           for flow,opened,suffix in (("false","false",""),("true","false",""),("false","true","_dry"),("true","true","_flowing"))}
            for properties, model in options.items():
                variants[properties] = {"model":MOD+":block/"+model, **({"y":y} if y else {})}
        write(assets/"blockstates"/(name+".json"), {"variants":variants})
        write(data/"loot_table/blocks"/(name+".json"), {"type":"minecraft:block","pools":[{
            "condition":{"type":"minecraft:survives_explosion"},"rolls":1,
            "entries":[{"type":"minecraft:item","name":rid(key)}]}],"random_sequence":MOD+":blocks/"+name})
        pattern, ingredients = RECIPES[key]
        write(data/"recipe"/(name+".json"), {"type":"minecraft:crafting_shaped","fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"town"}],"category":"building",
              "pattern":pattern,"key":ingredients,"result":{"id":rid(key)}})


def mining_tags(tags):
    for key, (_, _, tool) in PIECES.items():
        tags.add("block","minecraft:mineable/"+tool,rid(key))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(value, indent=2) + "\n"
    if not path.exists() or path.read_text(encoding="utf-8") != text:
        path.write_text(text, encoding="utf-8")


if __name__ == "__main__":
    lang_file = A/"lang/en_us.json"
    names = json.loads(lang_file.read_text(encoding="utf-8"))
    write_all(save, A, D, names)
    save(lang_file, names)
    for tool in ("axe","pickaxe"):
        path = ROOT/"data/minecraft/tags/block/mineable"/(tool+".json")
        tag = json.loads(path.read_text(encoding="utf-8"))
        for key,(_,_,pick) in PIECES.items():
            if pick == tool and rid(key) not in tag["values"]:
                tag["values"].append(rid(key))
        save(path, tag)

