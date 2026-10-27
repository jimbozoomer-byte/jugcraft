"""JSON resources for the Glass Abbot (tools/glass_abbot.py, docs/features/glass-abbot.md): the GeckoLib body he and his
Glass Images share (tools/glass_abbot_models.py); the names; his loot table, the Prism Shard, the Glass Reliquary, the
Lantern Lens, the Mirror Inscription and the Abbot's Cowl (worn as a costume); Abbey Glass, its block and item; the
recipes (Abbey Glass, a Glass Accord from Prism Shards, and the reliquary's two choices); the costume tags; and Seen
Clearly.

Called from tools/agriculture_data.py (assets, loot, recipes, tags, advancements).
"""
from agriculture import COSTUME_HAT_TAG, COSTUME_TAG
from decor_data import MOD, rid, self_drop
from regatta_data import box, hat_display
import glass_abbot as ga
import glass_abbot_models as am


def cowl_model():
    """The Abbot's Cowl as worn (the head fills 1.6 to 14.4, its face to the north): a deep hood of grey wool over the
    head, open at the front, its peak drawn up behind, and a fold falling down the back of the neck."""
    wool = "#wool"
    return {"textures": {"particle": rid("item/abbots_cowl_wool"), "wool": rid("item/abbots_cowl_wool")},
            "elements": [
                box((0.8, 14.4, 0.8), (15.2, 16.4, 15.2), wool),
                box((0.8, 4.0, 0.8), (2.0, 14.4, 15.2), wool),
                box((14.0, 4.0, 0.8), (15.2, 14.4, 15.2), wool),
                box((2.0, 4.0, 14.0), (14.0, 14.4, 15.2), wool),
                box((5.0, 16.4, 9.0), (11.0, 18.4, 15.2), wool),
                box((4.0, 0.0, 14.4), (12.0, 4.0, 15.6), wool),
            ],
            "display": hat_display(-6)}


# ---------------------------------------------------------------- assets

def assets(root, write, lang):
    models = root / "models"
    # His body, and his images': each entity its own copy of the model and clips (GeckoLib finds them by the entity's id).
    for entity, body in ga.GECKO.items():
        geo = am.MODELS[body]().geo()
        geo["minecraft:geometry"][0]["description"] = {**geo["minecraft:geometry"][0]["description"],
                                                       "identifier": f"geometry.{entity}"}
        write(root / "geckolib" / "models" / "entity" / f"{entity}.geo.json", geo)
        write(root / "geckolib" / "animations" / "entity" / f"{entity}.animation.json", am.ANIMATIONS[body]())
    for entity, display in ga.ENTITIES.items():
        lang[f"entity.{MOD}.{entity}"] = display

    # His loot's icons; the cowl's icon in slots, frames and on the ground, and worn or held, the cowl.
    for item in ga.ITEMS:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    for item in ga.COSTUMES:
        write(models / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(models / "item" / f"{item}_worn.json", cowl_model())
        write(root / "items" / f"{item}.json", {"model": {
            "type": "minecraft:select", "property": "minecraft:display_context",
            "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": rid(f"item/{item}")}}],
            "fallback": {"type": "minecraft:model", "model": rid(f"item/{item}_worn")}}})
    # Abbey Glass: see-through, its panes stained.
    for block in ga.BLOCKS:
        texture = {"force_translucent": True, "sprite": rid(f"block/{block}")}
        write(models / "block" / f"{block}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": texture, "particle": texture}})
        write(root / "blockstates" / f"{block}.json", {"variants": {"": {"model": rid(f"block/{block}")}}})
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"block/{block}")}})
    for item, display in {**ga.ITEMS, **ga.COSTUMES}.items():
        lang[f"item.{MOD}.{item}"] = display
    for block, display in ga.BLOCKS.items():
        lang[f"block.{MOD}.{block}"] = display
    lang[f"tooltip.{MOD}.prism_shard"] = ("A shard of the Glass Abbot's mantle: it leads four glass into Abbey Glass, and two "
                                          "with a spyglass make the next Glass Accord")
    lang[f"tooltip.{MOD}.glass_reliquary"] = ("His light, kept: craft it with a glass pane for the Lantern Lens, or with an "
                                              "amethyst shard for the Mirror Inscription")
    lang[f"tooltip.{MOD}.lantern_lens"] = ("In the offhand, it outlines in light, for you alone, invisible creatures near you "
                                           "and the true Glass Abbot among his images")
    lang[f"tooltip.{MOD}.mirror_inscription"] = ("In the offhand while a ward protects you, it turns the next arrow or other "
                                                 "shot of four hearts or less back at its shooter, and rests half a minute")
    lang[f"tooltip.{MOD}.abbots_cowl"] = "A costume: counts for trick-or-treating and the costume contest"

    for key, text in ga.MESSAGES.items():
        lang[f"message.{MOD}.glass_abbot.{key}"] = text
    info = ga.ADVANCEMENT
    lang[f"advancements.{MOD}.{info['key']}.title"] = info["title"]
    lang[f"advancements.{MOD}.{info['key']}.description"] = info["description"]


# ---------------------------------------------------------------- data

def chance(items, probability):
    return {"condition": {"type": "minecraft:random_chance", "chance": probability},
            "entries": [{"type": "minecraft:item", "name": rid(item)} for item in items], "rolls": 1}


def loot(out, write):
    """His loot (rolled for each participant by AbbotLoot, as a gift: nothing in it reads a killer): Prism Shards, a
    chance of the Glass Reliquary, and the chance of each of the rest; and Abbey Glass's own drop. `out` is the blocks'
    loot table folder."""
    low, high = ga.SHARDS_DROPPED
    pools = [{"entries": [{"type": "minecraft:item", "name": rid("prism_shard"), "modifier": {
        "type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": low, "max": high}}}], "rolls": 1}]
    pools.append(chance(("glass_reliquary",), ga.RELIQUARY_CHANCE))
    pools += [chance((item,), p) for item, p in ga.CHANCES.items()]
    write(out.parent / "entities" / "glass_abbot.json", {"type": "minecraft:gift", "random_sequence": rid("entities/glass_abbot"),
                                                         "pools": pools})
    for block in ga.BLOCKS:
        write(out / f"{block}.json", self_drop(block))


def recipes(out, write, conditions):
    for key, recipe in ga.RECIPES.items():
        result = recipe["result"]
        result = result if ":" in result else rid(result)
        if "shapeless" in recipe:
            body = {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": recipe["shapeless"]}
        else:
            body = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": recipe["pattern"], "key": recipe["key"]}
        write(out / f"{key}.json", {"fabric:load_conditions": conditions(*recipe.get("features", ())), **body,
                                    "result": {"id": result, "count": recipe.get("count", 1)}})


def tags(tags):
    # The cowl is a costume worn on the head.
    for item in ga.COSTUMES:
        tags.add("item", COSTUME_TAG, rid(item))
        tags.add("item", COSTUME_HAT_TAG, rid(item))
    # (Abbey Glass lets the sun through to a Glass Accord: tools/lairs.py ACCORD_GLASS names it.)


def advancements(data, write):
    info = ga.ADVANCEMENT
    write(data / MOD / "advancement" / f"{info['key']}.json", {
        "parent": "minecraft:adventure/root",
        "display": {"icon": {"id": info["icon"]}, "title": {"translate": f"advancements.{MOD}.{info['key']}.title"},
                    "description": {"translate": f"advancements.{MOD}.{info['key']}.description"},
                    "frame": info["frame"], "show_toast": True, "announce_to_chat": True},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
