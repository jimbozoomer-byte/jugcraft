"""Progression route maps for the Unified Encyclopedia: a Technology and a Magic starter-to-next-step route.

Only the route selection and order are written by hand here (ROUTES below: which items, machines and milestones, in
which groups, and whether each is required, recommended or an alternative). Every step's machine, inputs and outputs
are derived from the recipe it names in src/main/resources/data (vanilla recipe manager files, or Jug Nouveau's
nouveau_recipe files), and every gathered material from the loot table of the block it comes from. Inputs made by an
earlier step become that step's connection; other inputs say how they are made (their own recipe, again read from the
data) or that they are vanilla items.

Output: src/main/resources/jugcraft/progression_routes.json. Both sides read it:
- the server (guide/encyclopedia/ProgressionRoutes.java) writes the route_technology and route_magic Encyclopedia
  pages, reading every recipe again from the live recipe manager (or Jug Nouveau's live recipes) so a data pack that
  removes or changes a recipe shows as unavailable rather than as a stale grid;
- the client (client/EncyclopediaRoutesScreen.java, Route map tab) draws the route graph and links every node to its
  existing Encyclopedia entry (an Engineer's Handbook page, a codex category or an Encyclopedia page) and to the live
  recipe page.

No research gates, rewards, quests or currency: the maps describe what already exists. Run it to regenerate:
    python3 tools/encyclopedia_progression.py
scripts/tests/test_encyclopedia_progression.py checks the result against the recipe, tag, loot, handbook and registry
data independently of this module's derivation.
"""
import json
from pathlib import Path

MOD = "jugcraft"
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
DATA = RES / "data"
PATH = RES / "jugcraft" / "progression_routes.json"
HANDBOOK = RES / "assets" / MOD / "handbook" / "en_us.json"
CODEX_CATEGORIES = DATA / MOD / "modonomicon" / "books" / "arcane_concordance" / "categories"

# Vanilla and Fabric convention tags the routes' recipes accept; they are defined outside this repository (pinned 26.3
# vanilla and Fabric API conventions), so the member each route names is listed here. Repository tags are read.
EXTERNAL_TAGS = {
    "c:ingots/copper": ["minecraft:copper_ingot"],
    "c:ingots/iron": ["minecraft:iron_ingot"],
    "c:ingots/gold": ["minecraft:gold_ingot"],
    "c:nuggets/gold": ["minecraft:gold_nugget"],
    "c:gems/lapis": ["minecraft:lapis_lazuli"],
    "c:glass_blocks": ["minecraft:glass"],
    "minecraft:planks": ["minecraft:oak_planks"],
}

# Recipe type -> (method, station item). The starter workshop's station is its recipe's "station" index; a machine's
# item is its MachineKind id (MachineKind.recipeType() names the type; the data test reads MachineKind.java).
TYPES = {
    "minecraft:crafting_shaped": ("crafting", "minecraft:crafting_table"),
    "minecraft:crafting_shapeless": ("crafting", "minecraft:crafting_table"),
    "minecraft:smelting": ("smelting", "minecraft:furnace"),
    "minecraft:blasting": ("smelting", "minecraft:blast_furnace"),
    "jugcraft:crushing": ("machine", "jugcraft:crusher"),
    "jugcraft:alloying": ("machine", "jugcraft:alloy_smelter"),
    "jugcraft:pressing": ("machine", "jugcraft:metal_press"),
    "jugcraft:wire_drawing": ("machine", "jugcraft:wire_drawer"),
    "jugcraft:circuit_assembly": ("machine", "jugcraft:circuit_assembler"),
    "jugcraft:coking": ("machine", "jugcraft:coke_oven"),
    "jugcraft:steelmaking": ("machine", "jugcraft:steel_foundry"),
    "jugcraft:arc_smelting": ("machine", "jugcraft:arc_furnace_controller"),
    "jugcraft:starter_working": ("workshop", None),
    "imbuement": ("imbuement", "jugcraft:nouveau_imbuement_chamber"),
}
# jugcraft/workshop/JugcraftWorkshop.STATIONS, by a starter-working recipe's "station".
WORKSHOP_STATIONS = ["jugcraft:workshop_bench", "jugcraft:small_ceramic_kiln", "jugcraft:small_crucible_furnace"]

ROLES = ("required", "recommended", "alternative")


# ---------------------------------------------------------------------------------------------------------------------
# Hand-written selection: which steps, in which order and group, and how much each matters. Everything else is derived.
# ---------------------------------------------------------------------------------------------------------------------

def make(sid, recipe, role="required", note="", alts=(), entry=None, how=None):
    """A step made by recipe `recipe` (a vanilla recipe manager id). `alts`: other recipes for the same item.
    `how`: for off-route inputs, the recipe chosen to describe them ({item: recipe id})."""
    return {"id": sid, "kind": "step", "source": "recipe", "recipe": recipe, "role": role, "note": note,
            "alts": list(alts), "entry": entry, "how": dict(how or {})}


def imbue(sid, recipe, role="required", note="", entry=None):
    """A step made by a Jug Nouveau workshop recipe (data/jugcraft/nouveau_recipe)."""
    return {"id": sid, "kind": "step", "source": "nouveau", "recipe": recipe, "role": role, "note": note,
            "alts": [], "entry": entry, "how": {}}


def gather(sid, item, block, role="required", note="", entry=None, vanilla=False):
    """A material gathered by breaking `block` (its loot table must drop `item`; vanilla loot is outside this repo)."""
    return {"id": sid, "kind": "step", "source": "gathering", "item": item, "from": block, "vanilla": vanilla,
            "role": role, "note": note, "alts": [], "entry": entry, "how": {}}


def milestone(sid, title, icon, after, note, entry):
    """A milestone: no recipe of its own; it is reached when the steps in `after` are done."""
    return {"id": sid, "kind": "milestone", "source": "none", "title": title, "item": icon, "after": list(after),
            "role": "required", "note": note, "alts": [], "entry": entry, "how": {}}


def group(gid, title, text, steps, milestone_group=False):
    return {"id": gid, "title": title, "text": text, "milestone": milestone_group, "steps": steps}


def handbook(chapter, page):
    """An Engineer's Handbook page, by its chapter and page titles (resolved to indices against the handbook)."""
    return {"type": "handbook", "chapter_title": chapter, "page_title": page}


def page(target):
    return {"type": "page", "target": target}


def codex(category):
    return {"type": "codex", "category": f"{MOD}:{category}"}


TECHNOLOGY_GROUPS = [
    group("ores", "Mine and smelt tin and zinc",
          "Everything below starts with ore you can mine with a stone pickaxe and an ordinary furnace. Copper, iron, "
          "coal and redstone are vanilla materials.", [
              gather("raw_tin", "jugcraft:raw_tin", "jugcraft:tin_ore", entry=handbook("Materials", "Ores"),
                     note="Tin ore breaks into raw tin; Fortune gives more. Deepslate tin ore works the same way."),
              make("tin_ingot", "jugcraft:tin_ingot_from_smelting_raw_tin",
                   alts=["jugcraft:tin_ingot_from_blasting_raw_tin"], entry=handbook("Materials", "Ores")),
              gather("raw_zinc", "jugcraft:raw_zinc", "jugcraft:zinc_ore", entry=handbook("Materials", "Ores"),
                     note="Zinc goes into the machine casing and, later, brass."),
              make("zinc_ingot", "jugcraft:zinc_ingot_from_smelting_raw_zinc",
                   alts=["jugcraft:zinc_ingot_from_blasting_raw_zinc"], entry=handbook("Materials", "Ores")),
          ]),
    group("casing", "Bronze and the machine casing",
          "The first gate: every early machine is built around a Machine Casing, and the casing needs bronze. No "
          "powered machine is needed to make it.", [
              make("bronze_blend", "jugcraft:bronze_blend", entry=handbook("Getting Started", "Your First Power"),
                   note="Mix by hand: three copper ingots and one tin ingot make four blend."),
              make("bronze_ingot", "jugcraft:bronze_ingot_from_smelting_bronze_blend",
                   alts=["jugcraft:starter_workshop/bronze_ingot", "jugcraft:alloying/bronze_ingot"],
                   entry=handbook("Getting Started", "Your First Power"),
                   note="Smelting the blend is enough. The crucible and the Alloy Smelter are other ways, not requirements."),
              make("machine_casing", "jugcraft:machine_casing", entry=handbook("Getting Started", "Your First Power"),
                   note="A casing is a crafting part: placed alone it is not a working machine."),
          ], milestone_group=True),
    group("power", "Your first power",
          "A generator, cable and one machine are enough to start. JE is electrical energy; JE/t is per game tick.", [
              make("copper_cable", "jugcraft:copper_cable",
                   note="Cable must touch the generator's power port and the machine's socket, not a decorative panel."),
              make("coal_generator", "jugcraft:coal_generator",
                   note="Burns coal, charcoal or coke. It stops burning when full, so no fuel is wasted."),
              milestone("first_power", "Power is flowing", "jugcraft:coal_generator", ["copper_cable", "coal_generator"],
                        "Fuel in the generator and cable to a machine: your first power source.",
                        handbook("Getting Started", "Your First Power")),
          ], milestone_group=True),
    group("machines", "Your first powered machines",
          "The Electric Furnace is the first useful output. The Crusher is optional, but it doubles what an ore block "
          "gives.", [
              make("electric_furnace", "jugcraft:electric_furnace",
                   note="Smelts anything a vanilla furnace can, twice as fast. Leave its output slot free."),
              make("crusher", "jugcraft:crusher", role="recommended"),
              make("crushed_iron", "jugcraft:crushing/iron_ore", role="recommended",
                   entry=handbook("Processing", "Crusher"),
                   note="The crusher takes the ore block itself: mine it with Silk Touch, or use a drill."),
              milestone("first_machine", "A working production line", "jugcraft:electric_furnace",
                        ["first_power", "electric_furnace"],
                        "Generator, cable and a powered machine working together. Expand only where it helps.",
                        handbook("Progression", "1. Bronze Age")),
          ], milestone_group=True),
    group("workshop", "The pre-electric workshop (parallel)",
          "The starter workshop makes plates, wire and alloys without power. It is a sideways route beside "
          "electricity, and its iron plates are what make steel independent of the generator.", [
              make("workshop_bench", "jugcraft:starter_workshop/workshop_bench",
                   entry=handbook("Starter workshop", "A bench and three tools")),
              make("workshop_hammer", "jugcraft:starter_workshop/workshop_hammer",
                   entry=handbook("Starter workshop", "A bench and three tools")),
              make("iron_plate", "jugcraft:starter_workshop/iron_plate",
                   alts=["jugcraft:pressing/iron_ingot"], entry=handbook("Starter workshop", "A bench and three tools"),
                   note="The hammer stays on the bench; each use wears it a little."),
          ]),
    group("circuits", "Plates, wire and circuits",
          "The gate to the next tier: later machines need plates, wire and basic circuits.", [
              make("alloy_smelter", "jugcraft:alloy_smelter"),
              make("crucible_furnace", "jugcraft:starter_workshop/small_crucible_furnace", role="alternative",
                   entry=handbook("Starter workshop", "From clay to alloys"),
                   note="An unpowered way to alloy brass and bronze, instead of the Alloy Smelter."),
              make("brass_ingot", "jugcraft:alloying/brass_ingot", alts=["jugcraft:starter_workshop/brass_ingot"],
                   entry=handbook("Processing", "Alloy Smelter"),
                   note="The wire drawer is built from brass. The small crucible furnace makes brass without power."),
              make("metal_press", "jugcraft:metal_press"),
              make("wire_drawer", "jugcraft:wire_drawer"),
              make("circuit_assembler", "jugcraft:circuit_assembler",
                   how={"jugcraft:tin_plate": "jugcraft:pressing/tin_ingot", "jugcraft:bronze_gear": "jugcraft:bronze_gear"}),
              make("basic_circuit", "jugcraft:circuit_assembly/basic_circuit", entry=handbook("Processing", "Circuit Assembler"),
                   how={"jugcraft:silicon": "jugcraft:silicon_from_blasting_quartz",
                        "jugcraft:copper_wire": "jugcraft:wire_drawing/copper_ingot",
                        "jugcraft:solder_ingot": "jugcraft:alloying/solder_ingot"},
                   note="Silicon comes from quartz; solder from tin and lead."),
              milestone("first_circuit", "Basic circuits", "jugcraft:basic_circuit", ["basic_circuit"],
                        "Plates, wire and circuits: the parts every later machine needs.",
                        handbook("Progression", "2. Workshop")),
          ], milestone_group=True),
    group("steel", "Steel, a parallel capability",
          "Steel needs no electricity: the coke oven and the steel foundry run on heat. It opens the heavy machines.", [
              make("coke_oven", "jugcraft:coke_oven"),
              make("coke", "jugcraft:coking/coal", entry=handbook("Steel", "Coke Oven"), note="Coke is a hotter fuel and the carbon for steel."),
              make("steel_foundry", "jugcraft:steel_foundry",
                   note="Iron plates come from the workshop bench or the metal press."),
              make("steel_ingot", "jugcraft:steelmaking/steel_ingot", entry=handbook("Steel", "Steel Foundry"),
                   note="Piping in oxygen doubles the foundry's speed; it is optional."),
              milestone("first_steel", "Steel", "jugcraft:steel_ingot", ["steel_ingot"],
                        "Steel opens the arc furnace, advanced circuits and powered tools.",
                        handbook("Progression", "4. Steel")),
          ], milestone_group=True),
]

MAGIC_GROUPS = [
    group("glow", "Find something that glows",
          "Every magic branch can begin here. Luminous matter is an amethyst shard or glowstone dust; nothing needs a "
          "boss, a quest or electricity.", [
              gather("amethyst_shard", "minecraft:amethyst_shard", "minecraft:amethyst_cluster", vanilla=True,
                     entry=page("discover"),
                     note="Break amethyst clusters in a geode. Glowstone dust from the Nether also counts."),
              gather("glowstone_dust", "minecraft:glowstone_dust", "minecraft:glowstone", role="alternative",
                     vanilla=True, entry=page("discover"),
                     note="Glowstone from the Nether breaks into glowstone dust, which is luminous matter too."),
              milestone("first_light", "Examine a glow", "minecraft:amethyst_shard", ["amethyst_shard"],
                        "Sneak and use the shard to examine it. Your live First Light route shows what your research "
                        "still needs.", page("discover")),
          ]),
    group("casting", "Casting: your first instrument",
          "Owning an instrument is enough to use it. Compose a small light spell first.", [
              make("initiate_wand", "jugcraft:initiate_wand", entry=codex("foundations"),
                   note="Sneak-use the wand to open the constellation composer."),
              make("lampwright_bench", "jugcraft:lampwright_bench", role="recommended", entry=codex("radiance"),
                   note="Studying at the bench is one way to understand First Light."),
              milestone("first_spell", "Your first spell", "jugcraft:initiate_wand", ["initiate_wand", "first_light"],
                        "Kindle, or any spell you compose and inscribe. Spellcraft shows what you hold.",
                        page("spellcraft")),
          ], milestone_group=True),
    group("workshop", "A magical workshop: the first apparatus and Source",
          "Jug Nouveau is an independent start. The Imbuement Chamber works slowly without charge, so it needs no "
          "power to begin.", [
              make("novice_spell_book", "jugcraft:nouveau/novice_spell_book", role="recommended", entry=page("nouveau"),
                   note="Jug Nouveau's own spellbook, for glyph spells. The chamber below does not need it."),
              gather("archwood_log", "jugcraft:nouveau_green_archwood_log", "jugcraft:nouveau_green_archwood_log",
                     entry=page("nouveau"),
                     note="Archwood grows in its own forests in four colours; any archwood log works."),
              make("archwood_planks", "jugcraft:nouveau/archwood_planks", entry=page("nouveau")),
              make("imbuement_chamber", "jugcraft:nouveau/imbuement_chamber", entry=page("nouveau"),
                   note="Your first magical apparatus."),
              imbue("source_gem", "jugcraft:nouveau/imbuement_amethyst", entry=page("nouveau"),
                    note="Place an amethyst shard in the chamber. Ley Charge speeds it up; it is not required."),
              make("dominion_wand", "jugcraft:dominion_wand", role="recommended", entry=codex("nouveau"),
                   note="Links Source Jars, Sourcelinks and workshop apparatus."),
              make("source_jar", "jugcraft:nouveau/source_jar", role="recommended", entry=codex("nouveau"),
                   how={"jugcraft:nouveau_archwood_slab": "jugcraft:nouveau/archwood_slab"},
                   note="Stores Source for powered workshops and helpers."),
              milestone("first_source", "Your first Source Gem", "jugcraft:nouveau_source_gem",
                        ["imbuement_chamber", "source_gem"],
                        "Source Gems open glyphs, apparatus and helpers.", page("nouveau")),
          ], milestone_group=True),
    group("ley", "Shared charge with technology",
          "A Ley Pylon stores Ley Charge for rituals and compatible apparatus. Fill it with Radiance from a Kindled "
          "Lantern or with electrical JE, so a technology player can supply a mage.", [
              make("ley_pylon", "jugcraft:ley_pylon", role="recommended", entry=codex("circles"),
                   note="1000 JE make one Ley Charge; it never converts back."),
          ]),
    group("transmutation", "Transmutation without casting",
          "A separate specialty: no spellcasting needed. Learning and output permission are separate, so valuables "
          "stay protected.", [
              make("assayers_scale", "jugcraft:assayers_scale", entry=page("alchemy_start")),
              make("philosophers_stone", "jugcraft:philosophers_stone", entry=page("alchemy_start")),
              make("transmutation_table", "jugcraft:transmutation_table", role="recommended", entry=codex("equivalence"),
                   how={"jugcraft:dark_matter": "jugcraft:dark_matter"},
                   note="A later upgrade: Dark Matter is made from Aeternalis Fuel and a diamond block."),
          ], milestone_group=True),
]

ROUTES = [
    {"id": "technology", "page": "route_technology", "title": "Technology route map", "icon": "jugcraft:coal_generator",
     "accent": "B08A3A",
     "summary": ["From ore to your first power, your first machines and the next tier. Steel and the starter workshop "
                 "are parallel routes, not a ladder."],
     "groups": TECHNOLOGY_GROUPS},
    {"id": "magic", "page": "route_magic", "title": "Magic route map", "icon": "jugcraft:initiate_wand",
     "accent": "8A6FD0",
     "summary": ["Casting, a magical workshop and transmutation are independent starts. Begin with any of them."],
     "groups": MAGIC_GROUPS},
]

# The Encyclopedia's words for the route maps (server page rows and the client's Route map tab). The page titles are
# tools/encyclopedia.py PAGES and the Alternative weight its EDGES.
LANG = {
    "encyclopedia.jugcraft.progression.step": "%s: %s",
    "encyclopedia.jugcraft.progression.milestone": "Milestone: %s",
    "encyclopedia.jugcraft.progression.method.crafting": "craft at a %s",
    "encyclopedia.jugcraft.progression.method.smelting": "smelt in a %s",
    "encyclopedia.jugcraft.progression.method.machine": "process in a %s",
    "encyclopedia.jugcraft.progression.method.workshop": "work at a %s",
    "encyclopedia.jugcraft.progression.method.imbuement": "imbue in an %s",
    "encyclopedia.jugcraft.progression.method.gathering": "gather from %s",
    "encyclopedia.jugcraft.progression.method.milestone": "reached when the steps before it are done",
    "encyclopedia.jugcraft.progression.needs": "Required inputs: %s",
    "encyclopedia.jugcraft.progression.count": "%s × %s",
    "encyclopedia.jugcraft.progression.made_elsewhere": "Made off this route: %s",
    "encyclopedia.jugcraft.progression.made_by": "%s (%s)",
    "encyclopedia.jugcraft.progression.nouveau": "%s: %s makes %s",
    "encyclopedia.jugcraft.progression.tool": "Uses the %s on the station",
    "encyclopedia.jugcraft.grid.smelting": "Furnace",
    "encyclopedia.jugcraft.grid.machine": "Machine",
    "encyclopedia.jugcraft.grid.workshop": "Starter workshop",
    "ui.jugcraft.book.route_map": "Map",
    "ui.jugcraft.book.route_map.hint": "Route map: items, machines and milestones in order",
    "ui.jugcraft.book.route_technology": "Technology route map",
    "ui.jugcraft.book.route_magic": "Magic route map",
    "ui.jugcraft.book.back_to_route": "< Back to the route",
    "ui.jugcraft.book.open_entry": "Open entry",
    "ui.jugcraft.book.recipe_page": "Live recipe",
    "ui.jugcraft.book.map.legend": "Solid frame: required. Dotted blue frame: recommended. Orange corners: an "
                                    "alternative. Gold double frame and star: a milestone. Lines show what each step "
                                    "needs; dashed lines, what it may use instead.",
    "ui.jugcraft.book.map.choose": "Choose a step on the map, or use Up and Down. Enter opens its entry.",
    "ui.jugcraft.book.map.role.required": "Required step",
    "ui.jugcraft.book.map.role.recommended": "Recommended",
    "ui.jugcraft.book.map.role.alternative": "Alternative",
    "ui.jugcraft.book.map.role.milestone": "Milestone",
    "ui.jugcraft.book.map.method.crafting": "Crafted at",
    "ui.jugcraft.book.map.method.smelting": "Smelted in",
    "ui.jugcraft.book.map.method.machine": "Processed in",
    "ui.jugcraft.book.map.method.workshop": "Worked at",
    "ui.jugcraft.book.map.method.imbuement": "Imbued in",
    "ui.jugcraft.book.map.method.gathering": "Gathered from",
    "ui.jugcraft.book.map.needs": "Needs",
    "ui.jugcraft.book.map.makes": "Makes",
    "ui.jugcraft.book.map.after": "After: %s",
    "ui.jugcraft.book.map.from_step": "%s × %s: from the step above",
    "ui.jugcraft.book.map.vanilla": "%s × %s: a vanilla item",
    "ui.jugcraft.book.map.made": "%s × %s: %s",
    "ui.jugcraft.book.map.alternatives": "Other ways: %s",
    "ui.jugcraft.book.map.live": "This server's recipe:",
    "ui.jugcraft.book.map.not_here": "This server has no recipe for this step.",
    "ui.jugcraft.book.map.waiting": "Open the live recipe once the server has answered.",
}


# ---------------------------------------------------------------------------------------------------------------------
# Derivation from the data.
# ---------------------------------------------------------------------------------------------------------------------

def split(ref):
    namespace, path = ref.split(":", 1) if ":" in ref else ("minecraft", ref)
    return namespace, path


def recipe_file(rid):
    namespace, path = split(rid)
    return DATA / namespace / "recipe" / f"{path}.json"


def nouveau_file(rid):
    namespace, path = split(rid)
    return DATA / namespace / "nouveau_recipe" / f"{path}.json"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def tag_members(tag, seen=None):
    """The items of an item tag: repository tags (recursively) and EXTERNAL_TAGS."""
    seen = seen or set()
    if tag in seen:
        return []
    seen.add(tag)
    if tag in EXTERNAL_TAGS:
        return list(EXTERNAL_TAGS[tag])
    namespace, path = split(tag)
    file = DATA / namespace / "tags" / "item" / f"{path}.json"
    if not file.is_file():
        raise ValueError(f"unknown item tag #{tag}")
    out = []
    for value in load(file)["values"]:
        ref = value["id"] if isinstance(value, dict) else value
        out += tag_members(ref[1:], seen) if ref.startswith("#") else [ref]
    return out


def accepted(accepts):
    """Every item an ingredient accepts (an id, a #tag, a list of either, or {"item"/"tag"})."""
    if isinstance(accepts, list):
        return [item for option in accepts for item in accepted(option)]
    if isinstance(accepts, dict):
        return accepted(accepts["item"] if "item" in accepts else "#" + accepts["tag"])
    return tag_members(accepts[1:]) if accepts.startswith("#") else [accepts]


def plain(accepts):
    """An ingredient in the routes' own form: an id or #tag string, or a list of them."""
    if isinstance(accepts, list):
        return [plain(option) for option in accepts]
    if isinstance(accepts, dict):
        return accepts["item"] if "item" in accepts else "#" + accepts["tag"]
    return accepts


def parts(ingredients):
    """[(accepts, count)] from a list of ingredients, counting repeats in their first order."""
    counted = []
    for accepts in ingredients:
        accepts = plain(accepts)
        for entry in counted:
            if entry[0] == accepts:
                entry[1] += 1
                break
        else:
            counted.append([accepts, 1])
    return [(accepts, count) for accepts, count in counted]


def read_recipe(rid, source="recipe"):
    """A recipe's method, station, inputs [(accepts, count)], output (item, count) and tool, from its data file."""
    data = load(nouveau_file(rid) if source == "nouveau" else recipe_file(rid))
    kind = data["type"]
    if kind not in TYPES:
        raise ValueError(f"{rid}: recipe type {kind} is not one the route maps describe")
    method, station = TYPES[kind]
    tool = None
    if kind == "minecraft:crafting_shaped":
        symbols = [symbol for row in data["pattern"] for symbol in row if symbol != " "]
        inputs = parts([data["key"][symbol] for symbol in symbols])
    elif kind == "minecraft:crafting_shapeless":
        inputs = parts(data["ingredients"])
    elif "ingredients" in data:
        inputs = [(plain(part["ingredient"]), part.get("count", 1)) for part in data["ingredients"]]
    elif kind == "imbuement":
        inputs = [(plain(data["input"]), 1)]
    else:
        inputs = [(plain(data["ingredient"]), data.get("input_count", 1))]
    if kind == "jugcraft:starter_working":
        station = WORKSHOP_STATIONS[data["station"]]
        tool = data.get("tool", {}).get("id")
    result = data.get("result") or data.get("output")
    return {"method": method, "machine": station, "inputs": inputs, "output": (result["id"], result.get("count", 1)),
            "tool": tool}


def representative(accepts):
    """The item a route shows for an ingredient: the item itself, or the first member of its tag."""
    members = accepted(accepts)
    if not members:
        raise ValueError(f"ingredient {accepts} accepts nothing")
    return members[0]


def loot_drops(block):
    """Items a block's own loot table names (any branch: silk touch, fortune or plain)."""
    namespace, path = split(block)
    table = load(DATA / namespace / "loot_table" / "blocks" / f"{path}.json")
    found = []

    def walk(node):
        if isinstance(node, dict):
            if node.get("type") == "minecraft:item" and "name" in node:
                found.append(node["name"])
            for value in node.values():
                walk(value)
        elif isinstance(node, list):
            for value in node:
                walk(value)
    walk(table)
    return found


def resolve_link(entry, book):
    """An entry link in the client's form; handbook pages by title -> indices."""
    if entry is None:
        return None
    if entry["type"] != "handbook":
        return dict(entry)
    for c, chapter in enumerate(book["chapters"]):
        if chapter["title"] != entry["chapter_title"]:
            continue
        for p, found in enumerate(chapter["pages"]):
            if found["title"] == entry["page_title"]:
                return {"type": "handbook", "chapter": c, "page": p}
    raise ValueError(f"no handbook page {entry['chapter_title']} / {entry['page_title']}")


def handbook_entry(item, book):
    """The handbook page about an item: the page that crafts it, else the first whose icon it is (skipping the
    Progression overview chapter)."""
    for matches in (lambda found: (found.get("craft") or {}).get("result") == item, lambda found: found.get("icon") == item):
        for c, chapter in enumerate(book["chapters"]):
            for p, found in enumerate(chapter["pages"]):
                if c > 0 and matches(found):
                    return {"type": "handbook", "chapter": c, "page": p}
    return None


def makers_of(recipe, made):
    """Earlier steps an off-route recipe uses: its station, its tool, and any input an earlier step makes."""
    found = [made[m] for accepts, _ in recipe["inputs"] for m in accepted(accepts)[:1] if m in made]
    found += [made[ref] for ref in (recipe["machine"], recipe.get("tool")) if ref in made]
    return found


def off_route(item, accepts, chosen):
    """How an input that no earlier step makes is made: a vanilla item, or its chosen recipe read from the data."""
    if split(item)[0] == "minecraft":
        return {"method": "vanilla"}
    if item not in chosen:
        raise ValueError(f"{item}: no step makes it and no recipe is chosen to describe it (how=...)")
    rid = chosen[item]
    recipe = read_recipe(rid)
    if recipe["output"][0] != item:
        raise ValueError(f"{rid} makes {recipe['output'][0]}, not {item}")
    return {"method": recipe["method"], "machine": recipe["machine"], "recipe": rid}


def build_route(route, book):
    steps_out = []
    made = {}  # item -> step id, in order
    groups = []
    for spec_group in route["groups"]:
        out_steps = []
        for spec in spec_group["steps"]:
            sid = spec["id"]
            if spec["role"] not in ROLES:
                raise ValueError(f"{sid}: role {spec['role']}")
            step = {"id": sid, "kind": spec["kind"], "role": spec["role"]}
            needs, alt_needs = [], []
            if spec["kind"] == "milestone":
                step.update({"title": spec["title"], "item": spec["item"], "method": "milestone", "machine": None,
                             "source": "none", "recipe": None, "inputs": [], "outputs": []})
                needs = list(spec["after"])
            elif spec["source"] == "gathering":
                drops = loot_drops(spec["from"]) if not spec["vanilla"] else [spec["item"]]
                if spec["item"] not in drops:
                    raise ValueError(f"{sid}: {spec['from']} does not drop {spec['item']}")
                step.update({"item": spec["item"], "method": "gathering", "machine": None, "source": "gathering",
                             "recipe": None, "from": spec["from"], "vanilla": spec["vanilla"], "inputs": [],
                             "outputs": [{"item": spec["item"], "count": 1}]})
            else:
                recipe = read_recipe(spec["recipe"], spec["source"])
                item, count = recipe["output"]
                inputs = []
                for accepts, amount in recipe["inputs"]:
                    members = accepted(accepts)
                    makers = [made[m] for m in members if m in made]
                    shown = next((m for m in members if m in made), representative(accepts))
                    part = {"item": shown, "count": amount, "accepts": accepts}
                    if makers:
                        part["from_step"] = makers[0]
                        needs.append(makers[0])
                        alt_needs += makers[1:]
                    else:
                        part["how"] = off_route(shown, accepts, spec["how"])
                        if "recipe" in part["how"]:
                            needs += makers_of(read_recipe(part["how"]["recipe"]), made)
                    inputs.append(part)
                needs += makers_of({"inputs": [], **recipe}, made)
                alternatives = []
                for alt in spec["alts"]:
                    other = read_recipe(alt)
                    if other["output"][0] != item:
                        raise ValueError(f"{sid}: alternative {alt} makes {other['output'][0]}, not {item}")
                    alternatives.append({"recipe": alt, "method": other["method"], "machine": other["machine"]})
                    if other["machine"] in made:
                        alt_needs.append(made[other["machine"]])
                step.update({"item": item, "method": recipe["method"], "machine": recipe["machine"],
                             "source": spec["source"], "recipe": spec["recipe"], "inputs": inputs,
                             "outputs": [{"item": item, "count": count}], "alternatives": alternatives})
                if recipe["tool"]:
                    step["tool"] = recipe["tool"]
            step.setdefault("alternatives", [])
            unknown = [need for need in needs if need not in [s["id"] for s in steps_out]]
            if unknown:
                raise ValueError(f"{sid}: needs {unknown}, which are not earlier steps")
            step["needs"] = list(dict.fromkeys(needs))
            step["alt_needs"] = [need for need in dict.fromkeys(alt_needs) if need not in step["needs"]]
            roles = {s["id"]: s["role"] for s in steps_out}
            if step["role"] == "required" and any(roles[need] != "required" for need in step["needs"]):
                raise ValueError(f"{sid}: a required step needs a step that is not required: {step['needs']}")
            step["note"] = spec["note"]
            link = resolve_link(spec["entry"], book) if spec["entry"] else handbook_entry(step["item"], book)
            if link is None:
                raise ValueError(f"{sid}: no existing Encyclopedia entry for {step['item']}")
            step["link"] = link
            if step["kind"] == "step":
                made.setdefault(step["item"], sid)
            steps_out.append(step)
            out_steps.append(step)
        groups.append({"id": spec_group["id"], "title": spec_group["title"], "text": spec_group["text"],
                       "milestone": spec_group["milestone"], "steps": out_steps})
    ids = [step["id"] for step in steps_out]
    if len(ids) != len(set(ids)):
        raise ValueError(f"{route['id']}: step ids repeat")
    return {"id": route["id"], "page": route["page"], "title": route["title"], "icon": route["icon"],
            "accent": route["accent"], "summary": route["summary"], "groups": groups}


def build(book=None):
    """Both route maps; book is the generated Engineer's Handbook (assets/jugcraft/handbook/en_us.json)."""
    book = book if book is not None else load(HANDBOOK)
    return {"version": 1, "routes": [build_route(route, book) for route in ROUTES]}


def generate(write):
    write(PATH, build())


def lang(entries):
    entries.update(LANG)


def route_table(data=None):
    """The routes as a readable table, for the feature record."""
    data = data or build()
    lines = []
    for route in data["routes"]:
        lines.append(f"## {route['title']}")
        for group_data in route["groups"]:
            lines.append(f"### {group_data['title']}{' (milestone)' if group_data['milestone'] else ''}")
            for step in group_data["steps"]:
                ins = ", ".join(f"{part['count']} {part['item']}" for part in step["inputs"])
                outs = ", ".join(f"{part['count']} {part['item']}" for part in step["outputs"])
                lines.append(f"- {step['id']} [{step['role']}] {step['method']} @ {step['machine']} "
                             f"recipe={step['recipe']} in=({ins}) out=({outs}) needs={step['needs']}")
    return "\n".join(lines)


if __name__ == "__main__":
    PATH.parent.mkdir(parents=True, exist_ok=True)
    # The same form as generate_material_data.write(): two-space indent, ASCII escapes.
    PATH.write_text(json.dumps(build(), indent=2) + "\n", encoding="utf-8")
    print(route_table())
