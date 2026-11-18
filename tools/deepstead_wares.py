"""Author Deepstead's guild goods. The geode core uses the supplied sky-geode texture."""
import json
from pathlib import Path

from PIL import Image, ImageDraw
import model_writer

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
ART = RES / "assets/jugcraft"
BLOCKS = ("miners_knot_tile", "geode_reading_lamp", "gallery_chimes")
TOOLS = ("shaft_plummet", "surveyors_reel")
LANG = {
    "block.jugcraft.miners_knot_tile": "Miner's Knot Tile",
    "block.jugcraft.geode_reading_lamp": "Geode Reading Lamp",
    "block.jugcraft.gallery_chimes": "Gallery Chimes",
    "item.jugcraft.shaft_plummet": "Shaft Plummet",
    "item.jugcraft.surveyors_reel": "Surveyor's Reel",
    "tooltip.jugcraft.miners_knot_tile": "Two strands of beaten brass inlaid in slate.",
    "tooltip.jugcraft.geode_reading_lamp": "Use to turn the crystal off, dim or bright.",
    "tooltip.jugcraft.gallery_chimes": "Use to strike. Sneak-use to tune. Plays on a redstone pulse.",
    "tooltip.jugcraft.shaft_plummet": "Face a shaft edge and use to sound the first 64 blocks below.",
    "tooltip.jugcraft.surveyors_reel": "Sneak-use to set a survey pin. Use again to measure from it.",
    "message.jugcraft.shaft_plummet.stone": "Line meets solid ground %s blocks below (Y %s).",
    "message.jugcraft.shaft_plummet.water": "Line meets water %s blocks below (Y %s).",
    "message.jugcraft.shaft_plummet.lava": "Heat on the line! Lava %s blocks below (Y %s).",
    "message.jugcraft.shaft_plummet.liquid": "Line meets liquid %s blocks below (Y %s).",
    "message.jugcraft.shaft_plummet.unavailable": "The shaft ahead is out of reach.",
    "message.jugcraft.shaft_plummet.unreached": "The line found no floor within reach.",
    "message.jugcraft.surveyors_reel.set": "Survey pin set at %s.",
    "message.jugcraft.surveyors_reel.other": "Your pin is in %s. Sneak-use to move it here.",
    "message.jugcraft.surveyors_reel.distance": "From pin: %s blocks across, ΔY %s (ΔX %s, ΔZ %s).",
    "message.jugcraft.gallery_chimes.tune": "Gallery chimes tuned to %s.",
}


def save_image(image, folder, name):
    destination = ART / "textures" / folder / (name + ".png")
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination)


def surface_textures():
    source = ROOT / "art/owner-library/originals/Blocks/Weapon Smithing/geode/sky/block.png"
    geode = Image.open(source).convert("RGB")
    for brightness, suffix in ((1.0, "lit"), (0.67, "dim"), (0.33, "off")):
        save_image(geode.point(lambda value: round(value * brightness)), "block", "deepstead_geode_" + suffix)
    # Long shallow hammer marks catch light across the brass collars.
    brass = Image.new("RGB", (16, 16), "#ae824b")
    draw = ImageDraw.Draw(brass)
    for x, y, length in ((0, 2, 4), (8, 1, 5), (3, 6, 3), (10, 8, 4), (0, 12, 6), (7, 14, 5), (11, 4, 4)):
        draw.line((x, y, x + length, y), fill="#c79e62")
        draw.line((x + 1, y + 1, x + length, y + 1), fill="#936b40")
    save_image(brass, "block", "deepstead_hammered_brass")
    tile = Image.new("RGB", (16, 16), "#47505c")
    draw = ImageDraw.Draw(tile)
    draw.rectangle((0, 0, 15, 15), outline="#323943")
    draw.line((1, 1, 14, 1), fill="#63707a")
    draw.line((1, 1, 1, 14), fill="#596671")
    # Two angular interlocking lozenges. Restore the underpass at the crossing.
    for poly in (((2, 8), (6, 4), (10, 8), (6, 12), (2, 8)), ((6, 8), (10, 4), (14, 8), (10, 12), (6, 8))):
        draw.line([(x, y+1) for x, y in poly], fill="#332f35")
        draw.line(poly, fill="#c3975a")
        draw.line(poly[:2], fill="#efd58d")
    draw.point((8, 6), fill="#e6bf79")
    draw.point((8, 10), fill="#896441")
    save_image(tile, "block", "miners_knot_tile")


def sprite(name, rows):
    colors = {
        "x": "#302b31", "s": "#53515b", "v": "#85858a", "b": "#8d633d", "g": "#bd945a",
        "l": "#ecd18a", "w": "#67443b", "a": "#845440", "t": "#a36a4a", "c": "#68a8a2",
    }
    image = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        assert len(row) <= 16
        for x, pixel in enumerate(row):
            if pixel != ".": image.putpixel((x, y), (*bytes.fromhex(colors[pixel][1:]), 255))
    save_image(image, "item", name)


def tools():
    sprite("shaft_plummet", [
        "....xxxxxxxx", "...xgllllllgx", "..xbgbbbbggbx", "..xwtttttttwx",
        "...xaaaaaaasx", "....xxxxxxxsx", "...........vx", "..........sv",
        ".........vs", "........slx", ".......xggbx", "......xglggbx",
        "......xgggbbx", ".......xgbbx", "........xbx", ".........x",
    ])
    sprite("surveyors_reel", [
        "...........xx", "..........xglx", "....xxxx..xbx", "...xgllgxxsx",
        "..xglbbbglx", ".xggssssbgx", ".xlbvvvssblx", ".xlblccbsglx",
        ".xgbvcscsbgx", "..xgbvvssbgx", "...xggbbglx", "....xgglgx.tx",
        ".....xxx.xxtx", "........xttx", "........xwx", ".........x",
    ])


def element(start, end, material):
    return {"from": start, "to": end, "faces": {f: {"texture": "#" + material} for f in model_writer.FACES}}


def block_model(name, textures, pieces):
    model_writer.write(ART / "models/block" / (name + ".json"), {
        "parent": "minecraft:block/block", "textures": textures,
        "elements": [element(start, end, material) for start, end, material in pieces],
    })


def render_models():
    def model(name): return {"model": "jugcraft:block/" + name}
    write = model_writer.write
    for level in ("off", "dim", "lit"):
        block_model("geode_reading_lamp_" + level, {
            "particle": "minecraft:block/polished_deepslate", "stone": "minecraft:block/polished_deepslate",
            "brass": "jugcraft:block/deepstead_hammered_brass", "gem": "jugcraft:block/deepstead_geode_" + level,
        }, [
            ([2, 0, 2], [14, 2, 14], "stone"), ([3, 2, 3], [13, 3, 13], "brass"),
            ([4, 3, 4], [12, 5, 12], "stone"), ([5, 5, 5], [11, 6, 11], "brass"),
            ([6, 6, 6], [10, 12, 10], "gem"), ([7, 12, 7], [9, 14, 9], "gem"),
            ([3.5, 5, 7], [6, 9, 10], "gem"), ([4.25, 9, 7.75], [5.5, 10.5, 9.25], "gem"),
            ([10, 5, 4], [12.5, 10, 7], "gem"), ([10.5, 10, 4.5], [12, 11.5, 6.5], "gem"),
        ])
    write(ART / "blockstates/geode_reading_lamp.json", {"variants": {
        "brightness=" + str(i): model("geode_reading_lamp_" + level) for i, level in enumerate(("off", "dim", "lit"))}})
    pieces = [
        ([1, 0, 4], [4, 1.5, 12], "stone"), ([12, 0, 4], [15, 1.5, 12], "stone"),
        ([1.5, 1.5, 7], [3.5, 13, 9], "wood"), ([12.5, 1.5, 7], [14.5, 13, 9], "wood"),
        ([1, 13, 6.5], [15, 15, 9.5], "wood"),
        ([1.375, 10, 6.875], [3.625, 11.5, 9.125], "brass"), ([12.375, 10, 6.875], [14.625, 11.5, 9.125], "brass"),
    ]
    for x, bottom in ((5, 3), (8, 4.5), (11, 6.5)):
        pieces += [([x-0.3, 11.5, 7.7], [x+0.3, 13, 8.3], "iron"),
                   ([x-0.85, bottom, 7.15], [x+0.85, 11.5, 8.85], "brass"),
                   ([x-0.98, bottom, 7.02], [x+0.98, bottom+0.6, 8.98], "iron")]
    block_model("gallery_chimes", {
        "particle": "minecraft:block/dark_oak_planks", "wood": "minecraft:block/dark_oak_planks",
        "brass": "jugcraft:block/deepstead_hammered_brass", "stone": "minecraft:block/polished_deepslate",
        "iron": "minecraft:block/iron_block",
    }, pieces)
    write(ART / "blockstates/gallery_chimes.json", {"variants": {
        "facing=" + facing: dict(model("gallery_chimes"), y=y) for facing, y in model_writer.FACING_Y.items()}})
    write(ART / "models/block/miners_knot_tile.json", {"parent":"minecraft:block/cube_all", "textures":{"all":"jugcraft:block/miners_knot_tile"}})
    write(ART / "blockstates/miners_knot_tile.json", {"variants":{"": model("miners_knot_tile")}})
    for key in BLOCKS + TOOLS:
        entry = "jugcraft:block/" + (key + "_lit" if key == "geode_reading_lamp" else key)
        if key in TOOLS:
            entry = "jugcraft:item/" + key
            write(ART / "models/item" / (key + ".json"), {"parent":"minecraft:item/generated", "textures":{"layer0":entry}})
        write(ART / "items" / (key + ".json"), {"model":{"type":"minecraft:model", "model":entry}})
    for key in BLOCKS:
        loot = {"type":"minecraft:block", "pools":[{"rolls":1, "entries":[{"type":"minecraft:item", "name":"jugcraft:"+key}],
                "condition":{"type":"minecraft:survives_explosion"}}], "random_sequence":"jugcraft:blocks/"+key}
        write(RES / "data/jugcraft/loot_table/blocks" / (key + ".json"), loot)


if __name__ == "__main__":
    surface_textures()
    tools()
    render_models()
