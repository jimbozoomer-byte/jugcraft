"""Jug Nouveau: preserve the owner's resources; register only implemented gameplay.

The full source manifest tracks the ars_jimbaux upload, including data. Nothing in
the library is executed. Models/animation/textures used below are copied unchanged.
"""
from pathlib import Path
import csv
import json

ROOT = Path(__file__).resolve().parents[1]
MAGIC = ROOT / "art/owner-library/originals/Magic"
SOURCE = "assets/ars_jimbaux/"
COLORS = ("black", "blue", "green", "purple", "red", "white")
ITEMS = {
    "bookwyrm_charm": {"name": "Book Wyrm Charm", "tooltip": "Use on your Storage Lectern to bind a Book Wyrm. Four per lectern; eight chest links each."},
    "dominion_wand": {"name": "Dominion Wand", "tooltip": "Select a Storage Lectern, then link nearby chests/barrels. Sneak-use a chest to unlink it."},
}
BLOCKS = {"bookwyrm_lectern": {"name": "Storage Lectern"}}
OWNER_FILES = {
    "geckolib/models/entity/book_wyrm.geo.json": SOURCE + "geo/book_wyrm.geo.json",
    "geckolib/animations/entity/book_wyrm.animation.json": SOURCE + "animations/book_wyrm_animation.json",
    "geckolib/models/block/bookwyrm_lectern.geo.json": SOURCE + "geo/book_wyrm_lectern.geo.json",
    "geckolib/animations/block/bookwyrm_lectern.animation.json": SOURCE + "animations/book_wyrm_lectern_animations.json",
    "geckolib/models/item/bookwyrm_lectern.geo.json": SOURCE + "geo/book_wyrm_lectern.geo.json",
    "geckolib/animations/item/bookwyrm_lectern.animation.json": SOURCE + "animations/book_wyrm_lectern_animations.json",
    "textures/block/bookwyrm_lectern.png": SOURCE + "textures/block/book_wyrm_lectern.png",
    **{f"textures/entity/book_wyrm_{color}.png": SOURCE + f"textures/entity/book_wyrm_{color}.png" for color in COLORS},
    **{f"textures/item/{name}.png": SOURCE + f"textures/item/{name}.png" for name in ("bookwyrm_charm", "dominion_wand")},
}


def source_manifest():
    imported = {}
    for target, source in OWNER_FILES.items():
        imported.setdefault(source, []).append(target)
    rows = []
    with (ROOT / "art/owner-library/catalog/files.csv").open(encoding="utf-8-sig", newline="") as stream:
        for entry in csv.DictReader(stream):
            name = entry["path"].removeprefix("originals/Magic/")
            if name.startswith((SOURCE, "data/ars_jimbaux/")):
                rows.append({"source": name, "sha256": entry["sha256"], "kind": entry["kind"],
                             "jug_nouveau_runtime": imported.get(name),
                             "status": "integrated_bookwyrm" if name in imported else "available_for_port"})
    return {"collection": "Jug Nouveau (supplied namespace ars_jimbaux)",
            "scope": "Tracks this port; earlier Jugcraft art imports remain recorded by their own feature manifests.",
            "source_files": len(rows), "files": rows}


def write_all(write, assets, data, lang, condition, self_drop):
    for target, source in OWNER_FILES.items():
        if target.endswith(".json"):
            # Preserve owner geometry/UVs/animation channels; only normalize line endings.
            (assets / target).parent.mkdir(parents=True, exist_ok=True)
            (assets / target).write_bytes((MAGIC / source).read_bytes().replace(b"\r\n", b"\n"))
    names = {"bookwyrm_charm": "Book Wyrm Charm", "dominion_wand": "Dominion Wand", "bookwyrm_lectern": "Storage Lectern"}
    for name, title in names.items():
        lang[("block" if name == "bookwyrm_lectern" else "item") + ".jugcraft." + name] = title
        write(assets / "items" / f"{name}.json", {"model": {"type": "minecraft:model", "model": f"jugcraft:item/{name}"}})
        if name != "bookwyrm_lectern":
            write(assets / "models/item" / f"{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"jugcraft:item/{name}"}})
        write(data / "spell_assignments" / f"{name}.json", {"access": "NONE", "access_param": ""})
    write(assets / "items/bookwyrm_lectern.json", {"model": {"type": "minecraft:special", "base": "jugcraft:item/bookwyrm_lectern", "model": {"type": "geckolib:geckolib"}}})
    write(assets / "models/item/bookwyrm_lectern.json", {"parent": "minecraft:block/block", "textures": {"particle": "jugcraft:block/bookwyrm_lectern"}, "display": {
        "gui": {"rotation": [15, 135, 0], "translation": [0, -3, 0], "scale": [0.48, 0.48, 0.48]},
        "ground": {"translation": [0, 2, 0], "scale": [0.25, 0.25, 0.25]},
        "fixed": {"scale": [0.4, 0.4, 0.4]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.3, 0.3, 0.3]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "scale": [0.4, 0.4, 0.4]}}})
    write(assets / "blockstates/bookwyrm_lectern.json", {"variants": {"": {"model": "jugcraft:block/bookwyrm_lectern"}}})
    write(assets / "models/block/bookwyrm_lectern.json", {"textures": {"particle": "jugcraft:block/bookwyrm_lectern"}, "elements": []})
    write(data / "loot_table/blocks/bookwyrm_lectern.json", self_drop("bookwyrm_lectern"))
    recipes = {
        "bookwyrm_lectern": ([" B ", "ALA", " G "], {"B": "minecraft:book", "A": "minecraft:amethyst_shard", "L": "minecraft:lectern", "G": "minecraft:gold_ingot"}),
        "bookwyrm_charm": ([" A ", "GBG", " A "], {"A": "minecraft:amethyst_shard", "G": "minecraft:gold_ingot", "B": "minecraft:book"}),
        "dominion_wand": (["  A", " S ", "G  "], {"A": "minecraft:amethyst_shard", "S": "minecraft:stick", "G": "minecraft:gold_nugget"}),
    }
    for name, (pattern, key) in recipes.items():
        write(data / "recipe" / f"{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern,
              "key": key, "result": {"id": f"jugcraft:{name}", "count": 1}, "fabric:load_conditions": condition("concordance")})
    lang.update({"entity.jugcraft.book_wyrm": "Book Wyrm",
        "screen.jugcraft.bookwyrm.title": "Book Wyrm Storage", "screen.jugcraft.bookwyrm.previous": "Previous",
        "screen.jugcraft.bookwyrm.next": "Next", "screen.jugcraft.bookwyrm.unlink": "Unlink chest",
        "screen.jugcraft.bookwyrm.page": "Chest %s / %s", "screen.jugcraft.bookwyrm.wyrms": "%s / 4 Book Wyrms",
        "screen.jugcraft.bookwyrm.empty": "No linked chests", "screen.jugcraft.bookwyrm.offline": "Chest unavailable",
        "screen.jugcraft.bookwyrm.help": "Use a charm on the lectern. Select it with a Dominion Wand, then use the wand on chests or barrels within 16 blocks. Sneak-use a linked chest to unlink it.",
        "screen.jugcraft.bookwyrm.help_button": "Next setup tip",
        "screen.jugcraft.bookwyrm.step0": "1. Use a charm on your Storage Lectern.",
        "screen.jugcraft.bookwyrm.step1": "2. Select it with a Dominion Wand.",
        "screen.jugcraft.bookwyrm.step2": "3. Use the wand on a nearby chest or barrel.",
        "screen.jugcraft.bookwyrm.step3": "Each wyrm links eight chests within 16 blocks.",
        "screen.jugcraft.bookwyrm.setup": "Link chests with your Dominion Wand",
        "screen.jugcraft.bookwyrm.inventory": "Your inventory",
        "message.jugcraft.bookwyrm.selected": "Lectern selected. Use the wand on nearby chests or barrels.",
        "message.jugcraft.bookwyrm.select_first": "Select your Storage Lectern with the wand first.",
        "message.jugcraft.bookwyrm.linked": "Chest linked (%s / %s).",
        "message.jugcraft.bookwyrm.unlinked": "Chest unlinked. Its contents stay where they are.",
        "message.jugcraft.bookwyrm.refused": "Cannot link here: check ownership, distance, chest access and Book Wyrm capacity.",
        "message.jugcraft.bookwyrm.bound": "Book Wyrm bound! Each wyrm supports eight nearby chests.",
        "message.jugcraft.bookwyrm.full": "This lectern already has four Book Wyrms.",
        "message.jugcraft.bookwyrm.status": "Book Wyrm — tending its Storage Lectern. Dye it with white, black, blue, green, red or purple dye.",
        "tooltip.jugcraft.bookwyrm_charm": "Use on your Storage Lectern to bind a Book Wyrm. Four per lectern; eight chest links each.",
        "tooltip.jugcraft.dominion_wand": "Select a Storage Lectern, then link nearby chests/barrels. Sneak-use a chest to unlink it.",
        "tooltip.jugcraft.bookwyrm_lectern": "Access linked storage through your Book Wyrms. Items stay in their real chests.",
    })


def codex():
    return {("binding", "book_wyrms"): {
        "name": "Book Wyrm Storage", "x": 6, "y": 0, "icon": "jugcraft:bookwyrm_charm", "condition": None,
        "description": "Your flying librarians and a linked chest workshop",
        "pages": [
            ("crafting_recipe", "A Place for Your Wyrm", "Craft and place a **Storage Lectern**, then use a **Book Wyrm Charm** on it. Each wyrm lets the lectern reach **eight** nearby chests or barrels; four wyrms support **32** links. No research unlock is needed. The supplied wyrm and floating ledger animate in the workshop.", "jugcraft:bookwyrm_lectern"),
            ("crafting_recipe", "Bind a Book Wyrm", "One charm binds one wyrm. Use black, blue, green, purple, red or white dye on your wyrm to choose its color. Breaking the lectern returns its bound charms and removes the corresponding wyrms; the storage contents stay in their original chests.", "jugcraft:bookwyrm_charm"),
            ("crafting_recipe", "Connect Your Storage", "Use a **Dominion Wand** on the lectern to select it, then on each chest or barrel within **16 blocks**. Sneak-use a linked chest to remove its link. The owner's party can use the lectern, but every linked container must also permit access. Unlock locked containers directly first. Ender chests and machines cannot be linked.", "jugcraft:dominion_wand"),
            ("text", "Browse and Move Items", "Use the lectern empty-handed. **Previous** and **Next** browse the real containers; shift-click moves items between the selected container and your inventory. A double chest's halves can be linked separately. Put down any cursor-held stack before changing pages. **Unlink chest** removes only the connection. The setup-tip button cycles through the steps."),
            ("text", "When a Link Is Unavailable", "A lost wyrm disables excess links; binding a replacement restores that capacity. An unloaded, locked or newly protected chest stays unavailable. The lectern never loads chunks or copies items into a second hidden inventory. Walk away or remove the lectern and access closes. Book Wyrms visit the links visually; they do not carry a second copy of the contents."),
        ]}}


if __name__ == "__main__":
    path = ROOT / "docs/features/jug-nouveau-source-manifest.json"
    path.write_text(json.dumps(source_manifest(), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Recorded {source_manifest()['source_files']} source files; no library code executed.")
