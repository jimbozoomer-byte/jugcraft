"""Owner's EE-inspired utility gear; original implementation over the existing bounded Assay ledger."""
MOD = "jugcraft"
def rid(path): return f"{MOD}:{path}"

ITEMS = {"philosophers_stone": {"name": "Philosopher's Stone"},
         "repair_talisman": {"name": "Repair Talisman"}, "black_hole_band": {"name": "Black Hole Band"}}
OWNER_FILES = {
    "textures/item/philosophers_stone.png": "textures/item/philosophers_stone.png",
    "textures/item/repair_talisman.png": "textures/item/repair_talisman.png",
    "textures/item/black_hole_band.png": "textures/item/rings/black_hole_off.png",
    "textures/item/black_hole_band_active.png": "textures/item/rings/black_hole_on.png",
}
TOOLTIPS = {
    "philosophers_stone": "Use: portable Assay. Dissolve plain matter; keep a sample to form more. Requires understood Assay.",
    "repair_talisman": "Use: toggle. Hold or wear in a Charm slot. Mends up to 4 held-tool / worn-armor durability each second; 1 Prima grain per point. Requires Assay.",
    "black_hole_band": "Use: toggle. Hold or wear in a Ring slot. Attracts nearby loose items within 6 blocks; 1 Prima grain per item entity moved. Requires Assay.",
}
for _item, _info in ITEMS.items():
    _info["tooltip"] = TOOLTIPS[_item]

RECIPES = {
    "philosophers_stone": {"pattern": ["RGR", "GSG", "RGR"], "key": {"R": "minecraft:redstone", "G": "minecraft:gold_ingot", "S": rid("assayers_scale")}},
    "repair_talisman": {"pattern": [" L ", "ISI", " R "], "key": {"L": "minecraft:lapis_lazuli", "I": "minecraft:iron_ingot", "S": "minecraft:string", "R": "minecraft:redstone"}},
    "black_hole_band": {"pattern": ["GOG", "OEO", "GOG"], "key": {"G": "minecraft:gold_ingot", "O": "minecraft:obsidian", "E": "minecraft:ender_pearl"}},
}
UI = {
    "title": "Philosopher's Stone", "balance": "%s grains", "input": "Matter to dissolve", "sample": "Sample", "output": "Formed",
    "gain": "+%s grains", "cost": "%s grains / item", "inventory": "Your inventory", "sample_kept": "Your sample is kept",
    "dissolve": "Dissolve stack", "confirm": "Confirm dissolve", "form": "Form one", "on": "Active", "off": "Inactive",
    "dissolve_help": "Consumes the whole input stack for the displayed grains. Click again within 10 seconds to confirm. Changing the stack cancels confirmation.",
    "input_help": "Insert plain catalogued matter. Named or modified items are refused. Your ledger must have room for the whole stack (maximum 1,000,000 grains).",
    "form_help": "Keep one sample here. Creates one copy for the displayed price, paid from your personal ledger.",
    "status_0": "Form one copy. The sample is kept; take the result from the output slot.",
    "status_1": "Place one sample in the sample slot.", "status_2": "This sample cannot be formed. Only plain catalogued mundane materials qualify.",
    "status_3": "Not enough Prima Materia. Dissolve spare materials first.", "status_4": "Take the formed items out first; the output is full or holds another material.",
}

def codex():
    return {("equivalence", "alchemical_gear"): {
        "name": "Alchemical Gear", "x": 2, "y": 0, "icon": rid("philosophers_stone"), "condition": None,
        "description": "Transmute, repair and gather without casting spells",
        "pages": [
            ("text", "Equipment, Not Spells", "Understand **Assay** through First Light observation and bench study, then raw-matter examination and study. No wand or spell cast is needed. These tools spend your existing personal **Prima Materia** balance, also used by the Assayer's Scale. No Focus, mana or spell inscription is involved."),
            ("crafting_recipe", "Philosopher's Stone", "Use to open a portable transmutation book. Put spare matter on the left, then click **Dissolve stack** and **Confirm dissolve**. On the right, keep a sample and click **Form one**. The sample stays; the output costs 125% of its value, rounded up. Named, enchanted, filled and magical items cannot be converted. Items return when the book closes.", rid("philosophers_stone")),
            ("crafting_recipe", "Repair Talisman", "Use to toggle **Active**, then hold it or equip it in a **Charm** slot. Every second it repairs up to **4 durability** on one held tool or worn armor piece, for **1 grain per point**. Main hand, offhand, then head-to-feet armor are checked in order. Only damaged items with normal repair ingredients qualify. Enchantments and names are preserved; backpacks and machines are untouched.", rid("repair_talisman")),
            ("crafting_recipe", "Black Hole Band", "Use to toggle **Active**, then hold it or equip it in a **Ring** slot. Every second it draws up to **8 item entities** within **6 blocks** toward you, costing **1 grain for each moved entity**. It stops at solid walls, respects loot ownership and claims, and pauses when your inventory cannot accept the item. Minecraft's ordinary pickup collects it. Containers and mobs are unaffected.", rid("black_hole_band")),
            ("text", "Switches and Upkeep", "Both passive tools start off. Use again to switch off; the message shows your balance. Merely carrying one in a backpack does nothing. Duplicates do not multiply the effect. Empty balance, missing Assay knowledge or disabled Concordance stops all work. Trade the crafted equipment or formed materials; the ledger stays personal."),
        ]}}

def write_all(write, assets, data, lang, condition, self_drop):
    for key, text in UI.items(): lang[f"screen.{MOD}.alchemy.{key}"] = text
    lang[f"message.{MOD}.alchemy.toggle"] = "%s: %s · %s Prima Materia grains"
    for name in [*ITEMS, "black_hole_band_active"]:
        write(assets / "models" / "item" / f"{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{name}")}})
    for name in ITEMS:
        model = {"type": "minecraft:model", "model": rid(f"item/{name}")}
        if name == "black_hole_band": model = {"type": "minecraft:condition", "property": "minecraft:has_component", "component": rid("alchemy_active"), "on_false": model, "on_true": {"type": "minecraft:model", "model": rid("item/black_hole_band_active")}}
        write(assets / "items" / f"{name}.json", {"model": model})
    for name, recipe in RECIPES.items():
        write(data / "recipe" / f"{name}.json", {"fabric:load_conditions": condition("concordance"), "type": "minecraft:crafting_shaped", "category": "equipment", **recipe, "result": {"id": rid(name), "count": 1}})
    # Existing Charm tag is written by the trinkets module; add the talisman there after that writer runs.
    import concordance_trinkets
    write(data.parent / "trinkets" / "tags" / "item" / "legs" / "charm.json", {"replace": False, "values":
          [rid(item) for item, slot in concordance_trinkets.TRINKETS.items() if slot == "legs/charm"] + [rid("repair_talisman")]})
    # Resonant Ring already owns hand/ring; tags() merges this new ring into that shared tag.

def tags(tags):
    for item in ITEMS: tags.add("item", rid("equivalence/excluded"), rid(item))
    tags.add("item", "trinkets:hand/ring", rid("black_hole_band"))
