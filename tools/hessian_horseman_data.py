"""JSON resources for the Headless Horseman in his lair (tools/hessian_horseman.py, docs/features/hessian-horseman.md): the
GeckoLib bodies of him, his phantom riders, the pumpkin he tosses and the lantern head he hurls
(tools/hessian_horseman_models.py); their names; his loot table, the Hessian's Spur and the Hessian's Tricorn (worn as a
costume); the costume tags; and Laid to Rest. The Hollowflail and the Hessian Sabre are Arms VII trophies, written with
the other variants (tools/arms_variants.py), and so is his trophy loot table (loot_table/bosses/hessian_horseman.json); the
Dread boon spares the bosses #jugcraft:frightless names (written by tools/pumpkin_king_data.py from
arms_variants.FRIGHTLESS). His hollow, the Church Bridge, is part 1's (tools/church_bridge.py, tools/lair_data.py).

Called from tools/agriculture_data.py (assets, loot, recipes, tags, advancements). Since Concordance Chunk E (E4), his
recipes: a cheaper Hessian's Bridle from his lantern.
"""
from agriculture import COSTUME_HAT_TAG, COSTUME_TAG
from decor_data import MOD, rid
from regatta_data import box, hat_display
import hessian_horseman as hh
import hessian_horseman_models as hhm


def tricorn_model():
    """The Hessian's Tricorn as worn: a wide flat brim on the head (which fills 1.6 to 14.4), a low crown with a braid round
    its foot, and the brim cocked up into three walls, two from the front point flaring back to the corners and one
    across the back, each topped with gold braid. Where two parts meet, only one of the touching faces is kept, so no two
    faces flicker against each other."""
    felt, braid = "#felt", "#braid"
    sides = ("north", "south", "east", "west", "up")
    elements = [
        box((-0.5, 14.0, -0.5), (16.5, 14.8, 16.5), felt),
        box((3.0, 14.8, 3.0), (13.0, 18.4, 13.0), felt, faces=sides),
        box((2.8, 14.8, 2.8), (13.2, 15.6, 13.2), braid, faces=sides),
        box((2.0, 14.8, 14.6), (14.0, 17.2, 15.4), felt, faces=sides),
        box((2.0, 17.2, 14.6), (14.0, 17.6, 15.4), braid, faces=sides),
    ]
    for angle in (22.5, -22.5):
        for lo, hi, texture in (((7.6, 14.8, 0.6), (8.4, 17.2, 15.4), felt), ((7.6, 17.2, 0.6), (8.4, 17.6, 15.4), braid)):
            element = box(lo, hi, texture, faces=sides)
            element["rotation"] = {"angle": angle, "axis": "y", "origin": [8.0, 16.0, 0.6]}
            elements.append(element)
    return {"textures": {"particle": rid("item/hessians_tricorn_felt"), "felt": rid("item/hessians_tricorn_felt"),
                         "braid": rid("item/hessians_tricorn_braid")},
            "elements": elements, "display": hat_display(-6)}


# ---------------------------------------------------------------- assets

def assets(root, write, lang):
    models = root / "models"
    # His bodies: each entity its own model and clips (GeckoLib finds them by the entity's id).
    for entity, body in hh.GECKO.items():
        geo = hhm.MODELS[body]().geo()
        geo["minecraft:geometry"][0]["description"] = {**geo["minecraft:geometry"][0]["description"],
                                                       "identifier": f"geometry.{entity}"}
        write(root / "geckolib" / "models" / "entity" / f"{entity}.geo.json", geo)
        write(root / "geckolib" / "animations" / "entity" / f"{entity}.animation.json", hhm.ANIMATIONS[body]())
    for entity, display in hh.ENTITIES.items():
        lang[f"entity.{MOD}.{entity}"] = display

    # The spur: an icon. The tricorn: its icon in slots, frames and on the ground; worn or held, the hat.
    for item in hh.ITEMS:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    for item in hh.COSTUMES:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(models / "item" / f"{item}_worn.json", tricorn_model())
        write(root / "items" / f"{item}.json", {"model": {
            "type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": rid(f"item/{item}")}}],
            "fallback": {"type": "minecraft:model", "model": rid(f"item/{item}_worn")}}})
    for item, display in {**hh.ITEMS, **hh.COSTUMES}.items():
        lang[f"item.{MOD}.{item}"] = display
    lang[f"tooltip.{MOD}.hessians_spur"] = "In the offhand, the horse you ride gallops a fifth faster at night in the Overworld"
    lang[f"tooltip.{MOD}.hessians_tricorn"] = "A costume: counts for trick-or-treating and the costume contest"
    lang[f"effect.{MOD}.dread"] = "Dread"

    for key, text in hh.MESSAGES.items():
        lang[f"message.{MOD}.hessian_horseman.{key}"] = text
    info = hh.ADVANCEMENT
    lang[f"advancements.{MOD}.{info['key']}.title"] = info["title"]
    lang[f"advancements.{MOD}.{info['key']}.description"] = info["description"]


# ---------------------------------------------------------------- data

def chance(items, probability):
    return {"condition": {"type": "minecraft:random_chance", "chance": probability},
            "entries": [{"type": "minecraft:item", "name": rid(item)} for item in items], "rolls": 1}


def loot(out, write):
    """His loot (rolled for each player paid by HorsemanLoot, as a gift: nothing in it reads a killer): the Horseman's
    Lantern, a chance of one of his two trophies (as his bosses/hessian_horseman table holds them), and the chance of each
    of the rest. `out` is the blocks' loot table folder."""
    pools = [{"entries": [{"type": "minecraft:item", "name": rid(item)}], "rolls": 1} for item in hh.ALWAYS]
    pools.append(chance(hh.TROPHIES, hh.TROPHY_CHANCE))
    pools += [chance((item,), p) for item, p in hh.CHANCES.items()]
    write(out.parent / "entities" / "hessian_horseman.json", {"type": "minecraft:gift",
                                                              "random_sequence": rid("entities/hessian_horseman"),
                                                              "pools": pools})


def recipes(out, write, conditions):
    for key, recipe in hh.RECIPES.items():
        result = recipe["result"]
        result = result if ":" in result else rid(result)
        body = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": recipe["shapeless"]}
        write(out / f"{key}.json", {"fabric:load_conditions": conditions(), **body,
                                    "result": {"id": result, "count": recipe.get("count", 1)}})


def tags(tags):
    # The tricorn is a costume worn on the head.
    for item in hh.COSTUMES:
        tags.add("item", COSTUME_TAG, rid(item))
        tags.add("item", COSTUME_HAT_TAG, rid(item))


def advancements(data, write):
    info = hh.ADVANCEMENT
    write(data / MOD / "advancement" / f"{info['key']}.json", {
        "parent": "minecraft:adventure/root",
        "display": {"icon": {"id": info["icon"]}, "title": {"translate": f"advancements.{MOD}.{info['key']}.title"},
                    "description": {"translate": f"advancements.{MOD}.{info['key']}.description"},
                    "frame": info["frame"], "show_toast": True, "announce_to_chat": True},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
