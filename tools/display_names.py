"""Distinct English names: no two registered items share one (docs/features/distinct-item-names.md).

Several supplied collections register their own copy of something Jugcraft already has: a Landscapes maple log beside
Jugcraft's maple log, a Household chair beside a crafted one, a Jimtafacts steam generator beside Jugcraft's. The
original keeps its plain name; each collection's copy gets the collection's name after it in brackets. Pairs that
share a collection, or where the bracket would not tell them apart, get a name of their own in NAMES.

generate_material_data runs this last, after every collection has written its names; check_mod_data checks that no
duplicate is left.
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/jugcraft"

# An ID prefix and the collection named after a copy that carries it.
FAMILIES = {
    "landscape": "Landscapes",
    "ecology": "Ecology",
    "wildlife": "Wildlife",
    "household": "Household",
    "crafted": "Crafted",
    "jimta": "Jimtafacts",
    "cavern": "Caverns",
    "beast": "Beasts",
    "nouveau": "Nouveau",
    "pocket": "Pocket Pet",
}

# Names of their own, for pairs a collection bracket cannot separate.
NAMES = {
    "blueprint_sheet": "Blueprint Sheet",
    "hv_duct": "High-Voltage Duct",
    "printer": "Desk Printer",
    "jimta_monitor": "Bright Monitor",
    "jimta_computer_wired_modem_full": "Wired Modem (Full Block)",
    "nouveau_glyph_rune": "Rune Glyph",
    "cavern_fissure_primal_magma": "Primal Magma Fissure",
    "beast_red_sand_circle": "Red Anti-Sea Bear Circle",
    "beast_transmutation_table": "Beast Transmutation Table",
}

PREFIX = re.compile(r"(" + "|".join(FAMILIES) + r")_")


def registered(assets=ASSETS):
    """Every item's ID: each has an item model definition."""
    return sorted(path.stem for path in (assets / "items").glob("*.json"))


def key(lang, item):
    for kind in ("item", "block"):
        if f"{kind}.jugcraft.{item}" in lang:
            return f"{kind}.jugcraft.{item}"
    return None


def duplicates(lang, items):
    """Names (lower case) shared by more than one registered item, with the items' IDs."""
    groups = {}
    for item in items:
        name_key = key(lang, item)
        if name_key:
            groups.setdefault(lang[name_key].strip().lower(), []).append(item)
    return {name: ids for name, ids in groups.items() if len(ids) > 1}


def disambiguate(lang, items):
    """Renames the copies in every shared name; returns how many names changed."""
    changed = 0
    for ids in duplicates(lang, items).values():
        sharing = [item for item in ids if item not in NAMES]
        for item in ids:
            name_key = key(lang, item)
            if item in NAMES:
                name = NAMES[item]
            else:
                family = PREFIX.match(item)
                if not family or len(sharing) < 2:
                    continue  # the original, or the only one left with the name, keeps it
                name = f"{lang[name_key]} ({FAMILIES[family.group(1)]})"
            if lang[name_key] != name:
                lang[name_key] = name
                changed += 1
    return changed


def apply(write, assets=ASSETS):
    path = assets / "lang/en_us.json"
    lang = json.loads(path.read_text(encoding="utf-8"))
    items = registered(assets)
    changed = disambiguate(lang, items)
    write(path, lang)
    left = duplicates(lang, items)
    if left:
        print("display names still shared:", left)
    return changed
