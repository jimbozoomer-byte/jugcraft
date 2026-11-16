"""JSON resources for the Storm Roc (tools/storm_roc.py, docs/features/storm-roc.md): the GeckoLib bodies of him and his
Gale Fledglings (tools/storm_roc_models.py); the names; his loot table, the Roc Feather, the Weather-Vane Lens and the
Roc's Crest (worn as a costume); the feathers' recipes (four feathers, and a Thunder Vane without its lightning rod); the
costume tags; and Eye of the Storm. The Stormcaller and the Galefeather are Arms VII trophies, written with the other
variants (tools/arms_variants.py), and so is his trophy loot table (loot_table/bosses/storm_roc.json).

Called from tools/agriculture_data.py (assets, loot, recipes, tags, advancements).
"""
from agriculture import COSTUME_HAT_TAG, COSTUME_TAG
from decor_data import MOD, rid
from regatta_data import box, hat_display
import storm_roc as sr
import storm_roc_models as rm


def crest_model():
    """The Roc's Crest as worn (the head fills 1.6 to 14.4, its face to the north): three plumes of slate feathers rising
    from the crown of the head and swept back, the middle one tallest, and a short fall of feathers down the back of the
    head."""
    feathers = "#feathers"
    return {"textures": {"particle": rid("item/rocs_crest_feathers"), "feathers": rid("item/rocs_crest_feathers")},
            "elements": [
                box((7.0, 14.4, 3.0), (9.0, 21.0, 15.0), feathers),
                box((4.0, 14.4, 5.0), (6.0, 19.0, 15.0), feathers),
                box((10.0, 14.4, 5.0), (12.0, 19.0, 15.0), feathers),
                box((5.0, 9.0, 14.4), (11.0, 15.0, 15.6), feathers),
            ],
            "display": hat_display(-6)}


# ---------------------------------------------------------------- assets

def assets(root, write, lang):
    models = root / "models"
    # His bodies: each entity its own model and clips (GeckoLib finds them by the entity's id).
    for entity, body in sr.GECKO.items():
        geo = rm.MODELS[body]().geo()
        geo["minecraft:geometry"][0]["description"] = {**geo["minecraft:geometry"][0]["description"],
                                                       "identifier": f"geometry.{entity}"}
        write(root / "geckolib" / "models" / "entity" / f"{entity}.geo.json", geo)
        write(root / "geckolib" / "animations" / "entity" / f"{entity}.animation.json", rm.ANIMATIONS[body]())
    for entity, display in sr.ENTITIES.items():
        lang[f"entity.{MOD}.{entity}"] = display

    # The Roc Feather and the Weather-Vane Lens: icons. The crest: its icon in slots, frames and on the ground; worn or
    # held, the crest.
    for item in sr.ITEMS:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    for item in sr.COSTUMES:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(models / "item" / f"{item}_worn.json", crest_model())
        write(root / "items" / f"{item}.json", {"model": {
            "type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": rid(f"item/{item}")}}],
            "fallback": {"type": "minecraft:model", "model": rid(f"item/{item}_worn")}}})
    for item, display in {**sr.ITEMS, **sr.COSTUMES}.items():
        lang[f"item.{MOD}.{item}"] = display
    lang[f"tooltip.{MOD}.roc_feather"] = ("A flight feather of the Storm Roc's: four feathers, or with another the next "
                                          "Thunder Vane without its lightning rod")
    lang[f"tooltip.{MOD}.weather_vane_lens"] = "In the offhand, it tells you the coming weather every few seconds"
    lang[f"tooltip.{MOD}.rocs_crest"] = "A costume: counts for trick-or-treating and the costume contest"

    for key, text in sr.MESSAGES.items():
        lang[f"message.{MOD}.storm_roc.{key}"] = text
    for rod, text in sr.CHARGED.items():
        lang[f"message.{MOD}.storm_roc.charged.{rod}"] = text
    info = sr.ADVANCEMENT
    lang[f"advancements.{MOD}.{info['key']}.title"] = info["title"]
    lang[f"advancements.{MOD}.{info['key']}.description"] = info["description"]


# ---------------------------------------------------------------- data

def chance(items, probability):
    return {"condition": {"type": "minecraft:random_chance", "chance": probability},
            "entries": [{"type": "minecraft:item", "name": rid(item)} for item in items], "rolls": 1}


def loot(out, write):
    """His loot (rolled for each participant by RocLoot, as a gift: nothing in it reads a killer): Roc Feathers, a chance
    of one of his two trophies (as his bosses/storm_roc table holds them), and the chance of each of the rest. `out` is
    the blocks' loot table folder."""
    low, high = sr.FEATHERS
    pools = [{"entries": [{"type": "minecraft:item", "name": rid("roc_feather"), "modifier": {
        "type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": low, "max": high}}}], "rolls": 1}]
    pools.append(chance(sr.TROPHIES, sr.TROPHY_CHANCE))
    pools += [chance((item,), p) for item, p in sr.CHANCES.items()]
    write(out.parent / "entities" / "storm_roc.json", {"type": "minecraft:gift", "random_sequence": rid("entities/storm_roc"),
                                                       "pools": pools})


def recipes(out, write, conditions):
    for key, recipe in sr.RECIPES.items():
        result = recipe["result"]
        result = result if ":" in result else rid(result)
        if "shapeless" in recipe:
            body = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": recipe["shapeless"]}
        else:
            body = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": recipe["pattern"], "key": recipe["key"]}
        write(out / f"{key}.json", {"fabric:load_conditions": conditions(*recipe.get("features", ())), **body,
                                    "result": {"id": result, "count": recipe.get("count", 1)}})


def tags(tags):
    # The crest is a costume worn on the head.
    for item in sr.COSTUMES:
        tags.add("item", COSTUME_TAG, rid(item))
        tags.add("item", COSTUME_HAT_TAG, rid(item))


def advancements(data, write):
    info = sr.ADVANCEMENT
    write(data / MOD / "advancement" / f"{info['key']}.json", {
        "parent": "minecraft:adventure/root",
        "display": {"icon": {"id": info["icon"]}, "title": {"translate": f"advancements.{MOD}.{info['key']}.title"},
                    "description": {"translate": f"advancements.{MOD}.{info['key']}.description"},
                    "frame": info["frame"], "show_toast": True, "announce_to_chat": True},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
