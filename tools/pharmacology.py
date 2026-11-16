"""Optional buff-focused pharmacy. All formulations and effects are fictional game abstractions."""
import pharmacy_factory
import sterile_pharmacy
import precision_pharmacy
import alcohol_pharmacy
import controlled_release
import psychedelic_pharmacy
import drug_visuals
import pharmacology_strain

MOD = "jugcraft"
FEATURE = "agriculture"
ITEMS = {
    "caffeine_concentrate": ("Caffeine Concentrate", "jimtafacts/crops/coffee_powder"),
    "caffeine_drink": ("Caffeine Drink", "stimulant"),
    "pharmaceutical_starch": ("Pharmaceutical Starch", "flour"),
    "paracetamol": ("Paracetamol", "aspirin"),
    "rehydration_salts": ("Rehydration Salts", "glucose_syrup"),
    "activated_charcoal": ("Activated Charcoal", "jimtafacts/industry/jimta_coal_dust"),
}
ITEMS.update(pharmacy_factory.ITEMS)
ITEMS.update(sterile_pharmacy.ITEMS)
ITEMS.update(precision_pharmacy.ITEMS)
ITEMS.update(alcohol_pharmacy.ITEMS)
ITEMS.update(controlled_release.ITEMS)
ITEMS.update(psychedelic_pharmacy.ITEMS)

CRAFTING = {
    "pharmaceutical_starch": (["minecraft:potato", "minecraft:potato", "minecraft:paper"], 2),
    "activated_charcoal": (["minecraft:charcoal", "minecraft:charcoal", "minecraft:redstone", "minecraft:paper"], 2),
}

# Legacy source labels are retained only as the registered-effect assignment catalogue.
# Dedicated pharmacy_hud_art owns these PNGs; alias copies would destroy their saved encoding.
EFFECT_TEXTURES = {
    "caffeinated": "coffee_beans", "caffeine_jitters": "coffee_beans", "fortitude": "aspirin",
    "pharmacy_recovery": "first_aid_kit", "endurance": "glucose_syrup", "stimulated": "stimulant",
    "stimulant_appetite": "jimtafacts/crops/coffee_powder",
}
EFFECT_TEXTURES.update({key: 'stimulant' for key in ('sterile_recovery', 'lidocaine', 'adrenaline',
    'salbutamol', 'morphine', 'pharmacy_absorption', 'adrenaline_fatigue', 'opioid_drowsiness')})
EFFECT_TEXTURES.update({key: 'stimulant' for key in (*precision_pharmacy.FORMULAS, *alcohol_pharmacy.GRADES, 'intoxication', 'precision_focus')})
EFFECT_TEXTURES.update({key: 'stimulant' for key in controlled_release.PRODUCTS})
EFFECT_TEXTURES.update({key: 'dreamglass' for key in (*psychedelic_pharmacy.PRODUCTS, 'botanical_relaxation', 'pattern_interference')})


def items():
    return list(ITEMS)

def blocks():
    return psychedelic_pharmacy.BLOCKS


def write_all(write, assets, data, lang, condition):
    drug_visuals.write_all(write, assets, lang)
    for name, (display, texture) in ITEMS.items():
        lang[f"item.{MOD}.{name}"] = display
        write(assets / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{texture}"}})
        write(assets / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"{MOD}:item/{name}"}})
    lang.update({
        "drug.jugcraft.caffeine": "Caffeine",
        "drug.jugcraft.aspirin": "Aspirin",
        "drug.jugcraft.paracetamol": "Paracetamol",
        "drug.jugcraft.rehydration_salts": "Rehydration Salts",
        "drug.jugcraft.stimulant": "Stimulant",
        "effect.jugcraft.caffeinated": "Caffeine",
        "effect.jugcraft.caffeine_jitters": "Caffeine Jitters",
        "effect.jugcraft.fortitude": "Fortitude",
        "effect.jugcraft.pharmacy_recovery": "Recovery",
        "effect.jugcraft.endurance": "Endurance",
        "effect.jugcraft.stimulated": "Stimulated",
        "effect.jugcraft.stimulant_appetite": "Drug Appetite",
        "drug.jugcraft.phase.onset": "taking effect",
        "drug.jugcraft.phase.active": "active",
        "drug.jugcraft.phase.recovery": "recovering",
        "tooltip.jugcraft.caffeine_drink": "Haste I and 10% faster movement; stronger Speed effects take priority.",
        "tooltip.jugcraft.pharmaceutical_starch": "A fictional pharmacy binder, prepared from potatoes and paper.",
        "tooltip.jugcraft.aspirin": "Fortitude: 10% less armor-sensitive physical damage. Stronger Resistance takes priority.",
        "tooltip.jugcraft.paracetamol": "Recovery: 25% more natural healing, with normal food requirements and proportional food cost.",
        "tooltip.jugcraft.rehydration_salts": "Endurance: 20% less activity exhaustion. Healing still costs its normal food.",
        "tooltip.jugcraft.stimulant": "Speed II and Haste II for 60 s; Drug Appetite adds food exhaustion. Shares caffeine's family.",
        "tooltip.jugcraft.activated_charcoal": "Clears Caffeine Jitters and Drug Appetite. Keeps benefits, other effects, strain and tolerance. 60 s cooldown.",
        "tooltip.jugcraft.drug.timing": "%s s benefit · %s s onset · %s/%s strain",
        "tooltip.jugcraft.drug.redosing": "One drug per family. Redose only in the final quarter; repeat doses last 75%, then 50% as long.",
        "tooltip.jugcraft.drug.stimulant_redosing": "An active stimulant redose causes 60 s of jitters (+20% gun spread).",
        "message.jugcraft.drug.wait": "This drug family is still taking effect, active, or recovering. Redose only in its final quarter.",
        "message.jugcraft.drug.strain_full": "Not enough strain capacity for this formulation.",
        "message.jugcraft.drug.unavailable": "You cannot use this formulation now.",
        "message.jugcraft.drug.status": "%s: %s %s:%s · Strain %s/%s",
        "message.jugcraft.drug.ready": "%s: ready · Strain %s/%s",
    })
    for name, (ingredients, count) in CRAFTING.items():
        write(data / "recipe" / f"{name}.json", {
            "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shapeless",
            "category": "misc", "ingredients": ingredients,
            "result": {"id": f"{MOD}:{name}", "count": count}})
    write(data / "recipe" / "rehydration_salts.json", {
        "fabric:load_conditions": condition(FEATURE) + condition("salt"),
        "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["SGS", "BWB"],
        "key": {"S": "jugcraft:salt", "G": "minecraft:sugar", "B": "minecraft:paper", "W": "minecraft:water_bucket"},
        "result": {"id": "jugcraft:rehydration_salts", "count": 2}})
    for source, count, output in [("coffee", 2, 2), ("cocoa", 4, 1)]:
        ingredient = "jugcraft:coffee_beans" if source == "coffee" else "minecraft:cocoa_beans"
        name = f"caffeine_concentrate_from_{source}"
        write(data / "recipe" / f"{name}.json", {
            "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shapeless",
            "category": "misc", "ingredients": [ingredient] * count,
            "result": {"id": "jugcraft:caffeine_concentrate", "count": output}})
    write(data / "recipe" / "caffeine_drink.json", {
        "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shaped", "category": "misc",
        "pattern": ["CSC", "BWB"],
        "key": {"C": "jugcraft:caffeine_concentrate", "S": "minecraft:sugar", "B": "minecraft:glass_bottle",
                "W": "minecraft:water_bucket"},
        "result": {"id": "jugcraft:caffeine_drink", "count": 2}})
    for recipe, ingredient in [("caffeine_concentrate_from_coffee", "jugcraft:coffee_beans"),
                               ("caffeine_concentrate_from_cocoa", "minecraft:cocoa_beans"),
                               ("caffeine_drink", "jugcraft:caffeine_concentrate"),
                               ("pharmaceutical_starch", "minecraft:potato"),
                               ("activated_charcoal", "minecraft:charcoal"),
                               ("rehydration_salts", "jugcraft:salt"),
                               ("chemical_reaction/paracetamol", "jugcraft:pharmaceutical_starch")]:
        conditions = condition(FEATURE)
        if recipe == "rehydration_salts":
            conditions += condition("salt")
        elif recipe.startswith("chemical_reaction/"):
            conditions += condition("machines")
        write(data / "advancement" / "recipes" / "pharmacology" / f"{recipe}.json", {
            "fabric:load_conditions": conditions, "parent": "minecraft:recipes/root",
            "criteria": {
                "has_ingredient": {"trigger": "minecraft:inventory_changed",
                                   "conditions": {"items": [{"items": ingredient}]}},
                "has_the_recipe": {"trigger": "minecraft:recipe_unlocked",
                                   "conditions": {"recipes": [f"jugcraft:{recipe}"]}}},
            "requirements": [["has_ingredient", "has_the_recipe"]],
            "rewards": {"recipes": [f"jugcraft:{recipe}"]}})
    sterile_pharmacy.write_extras(write, assets, data, lang, condition)
    precision_pharmacy.write_extras(write, assets, data, lang, condition)
    alcohol_pharmacy.write_extras(write, assets, data, lang, condition)
    controlled_release.write_extras(write, assets, data, lang, condition)
    psychedelic_pharmacy.write_extras(write, assets, data, lang, condition)
    pharmacology_strain.write_all(lang)
    import pharmacy_item_art
    pharmacy_item_art.write_all(write, assets)
    pharmacy_item_art.language(lang)


def handbook_pages():
    return [{"title": "Coffee and Caffeine", "icon": "jugcraft:caffeine_drink", "text": [
        "Grow the existing Coffee Plant: pick Coffee Cherries, then roast them into Coffee Beans in a furnace, smoker or campfire.",
        "Two Coffee Beans make two Caffeine Concentrate. Four cocoa beans make one concentrate as a lower-yield alternative.",
        "Craft two concentrates, one sugar, two glass bottles and a water bucket into two Caffeine Drinks. The bucket is returned. Drinking returns a glass bottle.",
        "After a 10-second onset, a drink gives Haste I and 10% faster movement for five minutes, adding 10 strain; 100 is the safe threshold. Stronger potion effects take priority; they do not add their amplifiers together.",
        "Hold a drink to see remaining onset or benefit time. The strain HUD shows your current strain and the next-dose forecast. The Caffeine status also shows the active effect.",
        "A new dose replaces its family for a full new strain charge and restarts onset. Replacing an active stimulant causes 60 seconds of Caffeine Jitters: 20% more firearm spread.",
        "Repeat doses have 75%, then 50% duration. Thirty online minutes without an active stimulant clears tolerance. Strain falls by 6 points per online minute during onset/benefits and 12 otherwise, independently of duration.",
        "Leaving pauses saved drug timers. Death ends benefits without resetting recovery or tolerance. Milk cancels pending or active caffeine; it does not erase its recovery accounting.",
        "These are fictional game effects. This system adds no injury, illness or combat-triggered debuffs. Existing coffee food recipes remain available."
    ]}, {"title": "Early Pharmacy", "icon": "jugcraft:aspirin", "text": [
        "Aspirin grants Fortitude: 10% less armor-sensitive physical damage for 12 minutes, after 5 seconds, at 10 strain. Stronger Resistance takes priority. It does not reduce fire, magic, falling or other armor-bypassing damage.",
        "Its existing reactor routes remain: four willow logs and 500 mB water give two salicylic acid; birch gives one. Two salicylic acid, cider vinegar and 50 mB sulfuric acid give four Aspirin.",
        "Paracetamol grants Recovery: 25% more healing from vanilla natural regeneration for 12 minutes, after 5 seconds, at 10 strain. It requires the normal food conditions and natural-regeneration rule. Extra healing pays a proportional food cost; it never amplifies instant healing or regeneration potions.",
        "Two potatoes and paper make two Pharmaceutical Starch. In the Chemical Reactor, two starch and redstone with 250 mB water make four Paracetamol. This is a fictional game formulation.",
        "Rehydration Salts grant Endurance: 20% less activity food exhaustion for 15 minutes, after 5 seconds, at 5 strain. They do not restore food or discount natural healing's food cost.",
        "Craft two salt, sugar, two paper and a water bucket into two Rehydration Salts sachets. Crafting returns the water bucket. A used sachet leaves no glass bottle."
    ]}, {"title": "Dosing and Side Effects", "icon": "jugcraft:activated_charcoal", "text": [
        "Stimulant tablets grant Speed II and Haste II for 60 seconds immediately, at 25 strain. The existing reactor route uses four cocoa beans and one Pharmaceutical Starch with 250 mB bioethanol. Tablets leave no glass bottle. Drug Appetite adds 0.1 activity exhaustion per second while present.",
        "Caffeine and Stimulant share one family and replace each other rather than stacking. Aspirin, Paracetamol and Salts have separate families. All doses charge strain; crossing 100 is allowed with penalties.",
        "One active formulation per family. Any new formulation replaces it for a full strain charge and restarts onset. Repeat duration is 75%, then 50%; it never accumulates. Only an active stimulant redose gives Jitters.",
        "Two charcoal, redstone and paper make two Activated Charcoal. Taking it clears Caffeine Jitters, Drug Appetite and Adrenaline Fatigue, keeps benefits and unrelated effects, and has a 60-second cooldown. It never frees strain or erases tolerance. Opioid Drowsiness instead ends with Naloxone or the opioid benefit.",
        "Hold any timed medicine to see its phase, remaining time and total strain. Milk cancels pending and active benefits; accumulated strain continues decaying rather than resetting. Offline time pauses all accounting. Death ends benefits and preserves recovery and tolerance.",
        "First Aid Kits and Antidote retain their existing instant effects. Aspirin and Stimulant keep their saved item and recipe IDs; older stacks acquire the new shared rules. No new injury or combat conditions are added."
    ]}] + pharmacy_factory.handbook_pages() + sterile_pharmacy.handbook_pages() + precision_pharmacy.handbook_pages() + alcohol_pharmacy.handbook_pages() + controlled_release.handbook_pages() + psychedelic_pharmacy.handbook_pages() + pharmacology_strain.handbook_pages() + drug_visuals.handbook_pages()
