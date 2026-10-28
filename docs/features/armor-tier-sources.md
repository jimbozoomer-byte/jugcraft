# How the owner's armour sets are won

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. On 6 October 2026 the owner made their armour designs creative-only for the time being: "They might get dropped by bosses or be craftable for now just make the armor we can figure that out later" ([bloodthorn-armor.md](bloodthorn-armor.md)). On 28 October 2026 the contributor, working through items players can see but never get, chose both: a crafted smithing pattern for most sets, and boss drops for the sets that belong to a boss. They then asked: "make the bosses the only way to get the boss armor". This record is that choice, for the owner's review. Every number here is provisional.

Owner: jimbozoomer-byte
Target milestone and tier: endgame armour, beside netherite
Primary specialty and supported player role: combat; boss hunters for the boss sets

## Player experience

All 18 of the owner's armour sets (`tools/gear.py` `ARMOR_TIERS`, 72 pieces) and the 6 arms of the boss sets can now be had in survival, each in one way only.

### Five sets come only from their boss

| Set | Boss | Arms |
| --- | --- | --- |
| Reaper | Vesperine, the Last Reaper | Reaper Scythe |
| Frost Knight | The Yeti King | Frost Knight Greatsword |
| Hades | The Cinder Tyrant | Hades Scythe |
| Wight King | The Crypt Lich | Wight King Zweihander |
| Sentinel | The Cydonian Sentinel | Sentinel Longsword, Sentinel Shield |

These are the five sets that came with arms of their own, and each matches its boss by name or look. Every player paid a share of the boss's reward gets one piece of the set every time, each piece as likely (the contributor: "should be a drop every time"), so a full set takes about 8 victories on average, duplicates and all. The set's arm is 15% a share, the same as a boss trophy; the Sentinel's two arms are 7.5% each. Nothing else makes or gives these sets: no recipe, chest, shop or trade.

For the Yeti King, the Cinder Tyrant and Vesperine, the JEI information page of each piece and arm states these chances, as their other loot's pages do.

### Thirteen sets are smithed

Craft the set's pattern (four at a time) from a sheet of paper and the set's own things, then at a smithing table put in the pattern, a plain armour piece and the set's mending material. Enchantments and wear carry over, as with the Steampunk and Kaiser patterns.

| Set | Pattern: paper and | Smithed onto | With |
| --- | --- | --- | --- |
| Bloodthorn | 4 crimson fungus, 4 redstone | netherite | a netherite ingot |
| Reforged White Diamond | 6 quartz, 2 diamonds | netherite | a diamond |
| Sunset Gem | 4 amethyst shards, 4 orange dye | netherite | an amethyst shard |
| Pharaoh | 4 gold nuggets, 4 chiseled sandstone | netherite | a gold ingot |
| Dread Knight | 4 gold nuggets, 4 black dye | netherite | a netherite ingot |
| Valkyrie | 4 feathers, 4 gold nuggets | netherite | a phantom membrane |
| Wayfarer | 4 leather, 3 blue dye, a compass | netherite | leather |
| Spartan | 4 bronze nuggets, 4 red dye | netherite | a bronze ingot |
| Berserker | 4 bones, 2 red dye, 2 quartz | netherite | quartz |
| Paladin | 4 white dye, 2 amethyst shards, 2 gold nuggets | netherite | an amethyst shard |
| Templar | 4 iron nuggets, 2 red dye, 2 black dye | netherite | a netherite ingot |
| Banana | 4 yellow dye, 4 bananas | iron | yellow wool |
| Scarab | 4 gold nuggets, 4 lapis lazuli | netherite | lapis lazuli |

The mending material is each set's repair item, already in its repair tag. Every set but the Banana is smithed onto netherite, so it costs at least a netherite set. The Banana is a costume with iron's defense, so it is smithed onto iron.

### The patterns

Thirteen new 16×16 items, `jugcraft:<set>_pattern`, drawn on the same rolled parchment as the Steampunk and Kaiser patterns (`tools/arms_variants_art.py` `parchment16`). Each has an emblem in two inks of the set: a thorned crimson bloom, a white diamond, a setting sun, a gold ankh, a crowned dark helm, white wings on a gold disc, a compass rose, a plumed helm, horns on a red helm, a shield with a violet cross, a great helm with a red cross, a banana and a scarab. The contributor was shown them first.

## Connections

- **Input producer:** the five lair bosses; netherite and iron armour; ordinary materials for the patterns.
- **Output consumer:** the wearer. No recipe takes a tier piece back apart.
- **Unlocks:** none.
- **Failure behavior:** with the `machines` feature off, the pattern and smithing recipes do not load; the boss drops are unchanged.

## Balance

- A smithed set costs a netherite set (or an iron one for the Banana), one pattern per piece and four mending items. Nothing turns a piece back, so there is no loop.
- Each boss's existing loot is unchanged; the set is added beside it.

## Checks changed

The repository enforced "creative-only" in several places; each now enforces the new single source instead:
- `check_mod_data.py`: `check_acquisition` holds each tier's pieces to one source (its smithing recipes, or its boss's share table, where they must be one sure pool), and `check_arms_variants` holds each set's arm and shield to its boss's share table. `JugcraftGear.TIER_PATTERNS` must match `gear.tier_patterns()`.
- `tools/acquisition.py`: `CREATIVE_ONLY` is gone.
- The survival audit no longer marks the 72 pieces and 6 arms as deferred (`tools/survival_native_routes.py`, `tools/survival_native_followup.py`, `scripts/tests/test_survival_recipes.py`). The 13 patterns are added to the audit's registry snapshot (`docs/audits/survival-item-registry.json`). The saved report (`docs/audits/survival-recipes.*`) is not rewritten here: a fresh run also picks up unrelated changes merged since it was last saved. Run fresh, the pieces count as recipe-reachable (smithed) or loot-only (boss), and nothing is deferred.

## Multiplayer and persistence

New item IDs: the 13 `jugcraft:<set>_pattern`. New recipes: 13 pattern recipes and 52 smithing recipes, each named for its result. Five boss share tables gain pools. No existing ID or saved data changes. Boss loot is rolled on the server for each paid participant, as before.

## Verification

- New server game test `ArmorTiersGameTests.eachTierIsSmithedOrDroppedByItsBoss`:
  - every smithed set's pattern recipe loads, and the pattern with an enchanted, worn base piece and the mending item makes that set's piece with the enchantment and wear kept;
  - a diamond piece makes nothing;
  - no boss-set piece or arm has a recipe of its name;
  - every share of each boss's loot holds exactly one piece of its set, and 200 shares hold every piece and arm.
- Local:
  - `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8051 material IDs, data files and recipe audit) and `python3 scripts/check_repository.py` passed;
  - the Foundation workflow's tests passed: `python3 -m unittest discover -s scripts/tests` (287 tests), `test_winterhold_academy.py` and `scripts/package_modrinth.py check`;
  - the changed Java files parse (tree-sitter); they were not compiled locally.
- New items get the alchemy value file's labelled one-essence fallback ("fallback (balance pending)") until the value builder is next run with the Minecraft jar.
- CI on the fork: pending.

Not run: a human smithing a set or beating a boss for one.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
