"""Electroplating (batch 34, docs/features/electroplating.md): the electroplating bath plates tools, weapons and armor.

Java: machine/Electroplating.java and MachineKind.ELECTROPLATING_BATH. tools/check_mod_data.py keeps the numbers here and
in Java the same. A plating also repairs the item fully, without experience, so the bath doubles as a repair station for
enchanted gear; plating again with the same metal repairs it again.
"""

MOD = "jugcraft"

# Ticks per plating, and sulfuric acid (the electrolyte) it uses (mB); the bath's acid tank (mB).
TICKS = 200
ACID_PER_PLATING = 100
TANK = 4_000
# Nickel plating: the item's durability is multiplied by this (percent).
NICKEL_DURABILITY_PERCENT = 150
# Silver plating: the Smite level it gives a weapon (raised to this if lower).
SILVER_SMITE = 3
# Chrome plating (batch 57): the item's durability is multiplied by this (percent). Hard chrome is what real wear
# parts are plated with, so it beats nickel; chromium itself is a deeper, arc-furnace metal.
CHROMIUM_DURABILITY_PERCENT = 200

# metal -> (ingot ingredient tag, display adjective, tooltip)
METALS = {
    "nickel": ("c:ingots/nickel", "Nickel-plated", "Nickel-plated: half as durable again"),
    "silver": ("c:ingots/silver", "Silver-plated", "Silver-plated: smites the undead"),
    "gold": ("c:ingots/gold", "Gold-plated", "Gold-plated: piglins take it for gold"),
    "chromium": ("c:ingots/chromium", "Chrome-plated", "Chrome-plated: twice as durable"),
}


def write_all(write, assets, data, lang, condition):
    for metal, (_, _, tip) in METALS.items():
        lang[f"tooltip.{MOD}.plating.{metal}"] = tip
    lang[f"tooltip.{MOD}.plating.repair"] = "Plate again with the same metal to repair it."
