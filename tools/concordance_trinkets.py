"""Wayfaring, the trinkets slice, part 1 (docs/features/arcane-concordance-trinkets.md): the owner's belt, boots and
charms, worn in Trinkets slots once Relic Lore is understood.

The owner made these from other Minecraft mods (a relics mod and a reliquary mod) as a base and redid them; their art is
imported as supplied by tools/owner_art.py from OWNER_FILES below, under Jugcraft ids. Each item keeps the owner's
English name and the kind of effect the owner's own text gives it; the code, numbers and tooltips are Jugcraft's own:

- Leather Belt (worn in the belt slot): one more Charm slot.
- Angelic Feather (charm): a fall's harm is taken from your food instead of your health while you have food, and you jump
  a little higher.
- Kraken Shell (charm): the same for drowning.
- Infernal Claws (charm): the same for fire (not lava).
- Angelheart Vial (charm): a blow that would kill you leaves you on a little health and heals you a while; the vial is
  used up.
- Phoenix Down (charm): the same, at full health and with a moment's Fire Resistance; it becomes an Angelic Feather in the
  same slot. Worn, it gives the feather's effects.
- Amphibian Boot (feet): you swim faster, and your air lasts about twice as long.
- Ice Breaker (feet): a little knockback resistance, and a fall that hurts you sends a wave through the ground that
  harms, throws back and slows the hostile creatures round you, through the shared effect boundary.

Nothing here ticks, holds Focus or charge, or needs a mixin: the attributes are Trinkets modifiers, and the rest answers
three Fabric damage events (Java: concordance/trinket/Wayfaring.java). tools/concordance.py merges these tables into its
own; numbers the Java repeats are checked by tools/check_mod_data.py.
"""
import json
from pathlib import Path

MOD = "jugcraft"
# The owner's library (art/owner-library): read here, never written.
LIBRARY = Path(__file__).resolve().parents[1] / "art" / "owner-library" / "originals"
MAGIC = LIBRARY / "Magic"


def rid(path):
    return f"{MOD}:{path}"


# ------------------------------------------------------------------------------------------------------- numbers

# Charm slots a player has, and how many a worn Leather Belt adds (the owner's own: a charm slot, one more per belt).
CHARM_SLOTS = 1
BELT_CHARM_SLOTS = 1
# The owner's feet slot holds two.
FEET_SLOTS = 2
# Exhaustion each point of harm a charm takes from food instead of health costs (vanilla: 4 exhaustion is one point of
# saturation or food). A 10-block fall's 7 harm costs 3.5 points; standing in fire, about one a second. Nothing is taken
# while the food bar is empty: the harm then lands as it would.
ABSORB_EXHAUSTION = 2.0
# Attribute modifiers (added values) while worn where Trinkets applies effects. Jump strength: the feather's jump from
# about 1.25 blocks to 1.42, still short of a fence. Oxygen bonus: one more chance in two of keeping a breath.
FEATHER_JUMP = 0.03
AMPHIBIAN_SWIM = 0.5
AMPHIBIAN_OXYGEN = 1.0
ICE_BREAKER_KNOCKBACK = 0.1
# The Ice Breaker's wave: its radius in blocks grows with the fall's harm, up to a ceiling; it reaches the nearest hostile
# creatures up to a count, harms them (health points), throws them back (tenths of a block a tick) and slows them
# (Slowness I, ticks).
WAVE_RADIUS = 3.0
WAVE_RADIUS_PER_HARM = 0.25
WAVE_MAX_RADIUS = 6.0
WAVE_TARGETS = 12
WAVE_DAMAGE = 2
WAVE_PUSH = 6
WAVE_SLOW_TICKS = 40
# Death saves: the vial leaves you on this much health with Regeneration II; the Phoenix Down at full health, with
# Regeneration II and Fire Resistance.
VIAL_HEALTH = 4.0
VIAL_REGENERATION_TICKS = 100
PHOENIX_REGENERATION_TICKS = 200
PHOENIX_FIRE_RESISTANCE_TICKS = 200

# The slots (data/trinkets/slots/<group>/<slot>.json, given to players by data/trinkets/entities/jugcraft_wayfaring.json).
# The belt slot is Trinkets' own, with its own icon and name. The charm and feet slots are Jugcraft's, drawn with the
# owner's slot icons and named as the owner named them; a cosmetic copy is never made of them (Trinkets drops and
# restores cosmetic stacks badly on death), so a charm or boot is either worn or not.
BELT_SLOT = "legs/belt"
CHARM_SLOT = "legs/charm"
FEET_SLOT = "feet/boots"
TRINKET_SLOTS = {
    CHARM_SLOT: {"name": "Charm", "amount": CHARM_SLOTS, "icon": "charm"},
    FEET_SLOT: {"name": "Feet", "amount": FEET_SLOTS, "icon": "feet",
                "source": "data/jymbelics/curios/slots/feet.json"},
}
GRANTED_SLOTS = [BELT_SLOT, CHARM_SLOT, FEET_SLOT]

# Each item's slot, owner namespace and owner id (their file names), and its effects.
TRINKETS = {
    "leather_belt": BELT_SLOT,
    "angelic_feather": CHARM_SLOT, "kraken_shell": CHARM_SLOT, "infernal_claws": CHARM_SLOT,
    "angelheart_vial": CHARM_SLOT, "phoenix_down": CHARM_SLOT,
    "amphibian_boot": FEET_SLOT, "ice_breaker": FEET_SLOT,
}
OWNER_ITEMS = {
    "leather_belt": ("jymbelics", "leather_belt"),
    "angelic_feather": ("jymbaquary", "angelic_feather"),
    "kraken_shell": ("jymbaquary", "kraken_shell"),
    "infernal_claws": ("jymbaquary", "infernal_claws"),
    "angelheart_vial": ("jymbaquary", "angelheart_vial"),
    "phoenix_down": ("jymbaquary", "phoenix_down"),
    "amphibian_boot": ("jymbelics", "amphibian_boot"),
    "ice_breaker": ("jymbelics", "ice_breaker"),
}
# The owner's icons that are animated strips: their .png.mcmeta sidecars are imported with them.
ANIMATED = ["leather_belt", "amphibian_boot", "ice_breaker"]

# Attribute modifiers each item gives worn (attribute id -> added value). The Phoenix Down gives the feather's, under the
# feather's ids, so the two never add up; nor do two of anything (Java: WornTrinketItem names a modifier by its kind, not
# its slot).
SLOT_COUNT = "trinkets:slot_count/" + CHARM_SLOT
MODIFIERS = {
    "leather_belt": {SLOT_COUNT: BELT_CHARM_SLOTS},
    "angelic_feather": {"minecraft:jump_strength": FEATHER_JUMP},
    "phoenix_down": {"minecraft:jump_strength": FEATHER_JUMP},
    "amphibian_boot": {"minecraft:water_movement_efficiency": AMPHIBIAN_SWIM, "minecraft:oxygen_bonus": AMPHIBIAN_OXYGEN},
    "ice_breaker": {"minecraft:knockback_resistance": ICE_BREAKER_KNOCKBACK},
}
# The kind each item's modifiers are named after (Java: WornTrinketItem's kind).
KIND = {item: "angelic_feather" if item == "phoenix_down" else item for item in TRINKETS}
# What harm each charm takes from food instead of health (Java: Wayfaring.allowDamage): vanilla damage types.
ABSORBS = {
    "angelic_feather": ["minecraft:fall"],
    "phoenix_down": ["minecraft:fall"],
    "kraken_shell": ["minecraft:drown"],
    "infernal_claws": ["minecraft:in_fire", "minecraft:on_fire", "minecraft:campfire", "minecraft:hot_floor"],
}

_FOOD = f"while you have food, at {ABSORB_EXHAUSTION / 4:g} food or saturation a point"
ITEMS = {
    "leather_belt": {
        "name": "Leather Belt",
        "tooltip": "Worn in the Belt slot: one more Charm slot. It cannot be taken off while that slot holds a charm."},
    "angelic_feather": {
        "name": "Angelic Feather",
        "tooltip": f"Worn as a charm: a fall's harm comes from your food instead of your health {_FOOD}, and you jump a "
                   "little higher."},
    "kraken_shell": {
        "name": "Kraken Shell",
        "tooltip": f"Worn as a charm: drowning's harm comes from your food instead of your health {_FOOD}."},
    "infernal_claws": {
        "name": "Infernal Claws",
        "tooltip": f"Worn as a charm: fire's harm (not lava's) comes from your food instead of your health {_FOOD}."},
    "angelheart_vial": {
        "name": "Angelheart Vial",
        "tooltip": f"Worn as a charm: a blow that would kill you leaves you on {VIAL_HEALTH / 2:g} hearts, healing for "
                   f"{VIAL_REGENERATION_TICKS // 20} seconds. The vial is used up."},
    "phoenix_down": {
        "name": "Phoenix Down",
        "tooltip": "Worn as a charm: a blow that would kill you leaves you at full health, healing and proof against "
                   f"fire for {PHOENIX_REGENERATION_TICKS // 20} seconds, and it becomes an Angelic Feather. Until then it "
                   "is a feather as well."},
    "amphibian_boot": {
        "name": "Amphibian Boot",
        "tooltip": "Worn on your feet: you swim faster, and your air lasts about twice as long."},
    "ice_breaker": {
        "name": "Ice Breaker",
        "tooltip": "Worn on your feet: a little knockback resistance, and a fall that hurts you sends a wave through the "
                   f"ground that harms, throws back and slows hostile creatures up to {WAVE_MAX_RADIUS:g} blocks away."},
}
BLOCKS = {}

# Shaped crafting from Overworld materials. The owner's own recipes use the base mod's ingredients (mob drops and
# essences it adds), so they are not imported, except the Phoenix Down's, which is made only of this slice's items:
# three Angelheart Vials and an Angelic Feather. A death save costs a golden apple.
RECIPES = {
    "leather_belt": {"pattern": ["LIL", "L L"], "key": {"L": "minecraft:leather", "I": "minecraft:iron_ingot"}},
    "angelic_feather": {"pattern": [" A ", "PFP", " G "],
                        "key": {"A": "minecraft:amethyst_shard", "P": "minecraft:phantom_membrane", "F": "minecraft:feather",
                                "G": "minecraft:gold_ingot"}},
    "kraken_shell": {"pattern": ["PIP", "PNP"],
                     "key": {"P": "minecraft:prismarine_shard", "I": "minecraft:ink_sac", "N": "minecraft:nautilus_shell"}},
    "infernal_claws": {"pattern": ["M M", "LGL"],
                       "key": {"M": "minecraft:magma_block", "L": "minecraft:leather", "G": "minecraft:gold_ingot"}},
    "angelheart_vial": {"pattern": [" A ", "PHP", " B "],
                        "key": {"A": "minecraft:amethyst_shard", "P": "minecraft:phantom_membrane",
                                "H": "minecraft:golden_apple", "B": "minecraft:glass_bottle"}},
    "amphibian_boot": {"pattern": ["L L", "PKP"],
                       "key": {"L": "minecraft:leather", "P": "minecraft:prismarine_shard", "K": "minecraft:dried_kelp"}},
    "ice_breaker": {"pattern": ["ILI", "I I", "F F"],
                    "key": {"I": "minecraft:iron_ingot", "L": "minecraft:leather", "F": "minecraft:flint"}},
}
# The owner's shapeless Phoenix Down recipe (data/jymbaquary/recipe/phoenix_down.json), its ids renamed.
SHAPELESS = {"phoenix_down": [rid("angelheart_vial")] * 3 + [rid("angelic_feather")]}

# Every owner file this slice uses, copied as supplied by tools/owner_art.py: runtime path under assets/jugcraft -> path
# under originals/Magic (or, for OWNER_BLOCKS_FILES, under originals/Blocks). The only changes are the names and, for an
# animation sidecar, its line ends (LF, as Git stores the mod's text).
OWNER_FILES = {
    **{f"textures/item/{item}.png": f"assets/{ns}/textures/item/{owner}.png" for item, (ns, owner) in OWNER_ITEMS.items()},
    **{f"textures/item/{item}.png.mcmeta": f"assets/{OWNER_ITEMS[item][0]}/textures/item/{OWNER_ITEMS[item][1]}.png.mcmeta"
       for item in ANIMATED},
    f"textures/gui/sprites/container/slots/{TRINKET_SLOTS[FEET_SLOT]['icon']}.png": "assets/jymbelics/textures/slot/empty_feet_slot.png",
}
OWNER_BLOCKS_FILES = {
    f"textures/gui/sprites/container/slots/{TRINKET_SLOTS[CHARM_SLOT]['icon']}.png": "Trinket Type Mod/slot/empty_charm_slot.png",
}
OWNER_LANGS = {"jymbelics": "assets/jymbelics/lang/en_us.json", "jymbaquary": "assets/jymbaquary/lang/en_us.json"}


def owner_name(item):
    """The owner's English name for `item`, from their language file (only the names are taken from it)."""
    ns, owner = OWNER_ITEMS[item]
    lang = json.loads((MAGIC / OWNER_LANGS[ns]).read_text(encoding="utf-8-sig"))
    return lang.get(f"item.{ns}.{owner}")


def owner_model(item):
    """The owner's item model for `item`, with its texture renamed to the Jugcraft copy. It must be a plain generated model
    (the parent written either way, an empty overrides list allowed); anything else stops the generator, since a changed
    library file is a decision, not a merge."""
    ns, owner = OWNER_ITEMS[item]
    model = json.loads((MAGIC / "assets" / ns / "models" / "item" / f"{owner}.json").read_text(encoding="utf-8-sig"))
    plain = (model.get("parent") in ("item/generated", "minecraft:item/generated")
             and model.get("textures") == {"layer0": f"{ns}:item/{owner}"}
             and set(model) <= {"parent", "textures", "overrides"} and not model.get("overrides"))
    if not plain:
        raise SystemExit(f"tools/concordance_trinkets.py: the owner's model for {ns}:{owner} is not the plain generated model expected")
    return {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}}


def lang_entries(lang):
    for slot, info in TRINKET_SLOTS.items():
        lang[f"trinkets.slot.{slot.replace('/', '.')}"] = info["name"]


def codex():
    return {
        ("relics", "wayfaring"): {
            "name": "Belts, Charms and Boots", "x": 2, "y": 0, "icon": rid("leather_belt"), "condition": None,
            "description": "Worn things for the road",
            "pages": [
                ("text", "Belts, Charms and Boots",
                 "Once you understand **Relic Lore**, you can wear these: a belt in the Belt slot, a charm in a Charm "
                 f"slot (a Leather Belt gives a second) and up to {FEET_SLOTS} on your feet. They hold no Focus or charge "
                 "and need no pylon. Two of a kind never add up; a second vial only waits its turn."),
                ("crafting_recipe", "Leather Belt",
                 "One more Charm slot. Take the second charm off before the belt.", rid("leather_belt")),
                ("crafting_recipe", "Angelic Feather",
                 f"A fall's harm comes from your food instead of your health, at {ABSORB_EXHAUSTION / 4:g} food or "
                 "saturation a point, and you jump a little higher. With an empty food bar the fall hurts as ever.",
                 rid("angelic_feather")),
                ("crafting_recipe", "Kraken Shell", "The same for drowning.", rid("kraken_shell")),
                ("crafting_recipe", "Infernal Claws", "The same for fire and hot floors, not lava.", rid("infernal_claws")),
                ("crafting_recipe", "Angelheart Vial",
                 f"A blow that would kill you leaves you on {VIAL_HEALTH / 2:g} hearts, healing for "
                 f"{VIAL_REGENERATION_TICKS // 20} seconds, and the vial is used up. It does not answer a fall into the "
                 "void, a held totem (the totem answers), or a death in a dream.", rid("angelheart_vial")),
                ("text", "Phoenix Down",
                 "Three Angelheart Vials and an Angelic Feather, crafted together. A blow that would kill you leaves you "
                 f"at full health, healing and proof against fire for {PHOENIX_REGENERATION_TICKS // 20} seconds, and "
                 "the down becomes an Angelic Feather where it was worn. Until then it is a feather as well. A vial "
                 "worn beside it answers first."),
                ("crafting_recipe", "Amphibian Boot", "You swim faster, and your air lasts about twice as long.",
                 rid("amphibian_boot")),
                ("crafting_recipe", "Ice Breaker",
                 f"A little knockback resistance. A fall that hurts you sends a wave through the ground: up to "
                 f"{WAVE_TARGETS} hostile creatures within {WAVE_RADIUS:g} blocks (more for a harder fall, at most "
                 f"{WAVE_MAX_RADIUS:g}) are harmed, thrown back and slowed, wherever you may harm them.",
                 rid("ice_breaker")),
            ],
        },
    }


def tags(tags):
    pass


def write_all(write, assets, data, lang, condition, self_drop):
    lang_entries(lang)
    for item in ITEMS:
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
        write(assets / "models" / "item" / f"{item}.json", owner_model(item))
    for item, recipe in RECIPES.items():
        write(data / "recipe" / f"{item}.json", {
            "fabric:load_conditions": condition("concordance"), "type": "minecraft:crafting_shaped", "category": "equipment",
            "pattern": recipe["pattern"], "key": recipe["key"], "result": {"id": rid(item), "count": 1}})
    for item, ingredients in SHAPELESS.items():
        write(data / "recipe" / f"{item}.json", {
            "fabric:load_conditions": condition("concordance"), "type": "minecraft:crafting_shapeless", "category": "equipment",
            "ingredients": ingredients, "result": {"id": rid(item), "count": 1}})
    # The two new slots (the belt slot is Trinkets' own), the items each slot takes, and players' having all three.
    trinkets = data.parent / "trinkets"
    for slot, info in TRINKET_SLOTS.items():
        group, name = slot.split("/")
        write(trinkets / "slots" / group / f"{name}.json", {"icon": rid(f"container/slots/{info['icon']}"), "amount": info["amount"],
                                                            "validator_predicates": ["trinkets:default"], "cosmetic_slots": False})
    for slot in GRANTED_SLOTS:
        group, name = slot.split("/")
        write(trinkets / "tags" / "item" / group / f"{name}.json",
              {"replace": False, "values": [rid(item) for item, worn in TRINKETS.items() if worn == slot]})
    write(trinkets / "entities" / f"{MOD}_wayfaring.json", {"entities": ["player"], "slots": sorted(GRANTED_SLOTS)})


def write_data(write, data):
    pass
