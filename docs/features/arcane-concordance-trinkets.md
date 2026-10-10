# Arcane Concordance: Wayfaring (the owner's belt, boots and charms; trinkets part 1)

Status: implemented on branch `claude/awesome-davinci-iwv3b9`; see Verification for what has run.
Proposal issue: none; the owner asked for their supplied magic content to be built into Jugcraft (9 October 2026), named
this slice ("the trinkets (Relics/Reliquary)") as the one after the fire school, and authorized it at the top of
[CLAUDE.md](../../CLAUDE.md).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: Practitioner stage, beside [Relic Lore](arcane-concordance-relics.md) (roadmap step 20),
which these items need understood before they can be put on.
Primary specialty and supported player role: anyone who travels, explores and fights away from home; no school.

Builds on [relics and shrines](arcane-concordance-relics.md) (Relic Lore and the Trinkets slots), the [shared effect
boundary](arcane-concordance-composition.md) and [Ember part 2](arcane-concordance-ember-regalia.md) (the owner's art
imported as supplied, slots ported to Trinkets). Contract and checklist: [ARCANE_CONCORDANCE.md](../ARCANE_CONCORDANCE.md).

## Player experience

Once **Relic Lore** is understood, eight worn things can be put on, in three Trinkets slots: Trinkets' own **Belt** slot,
a new **Charm** slot (one; a Leather Belt gives a second) and a new **Feet** slot (two). The names, icons and kinds of
effect are the owner's; the numbers are Jugcraft's.

| Item | Worn in | What it does |
|---|---|---|
| **Leather Belt** | Belt | One more Charm slot. It cannot be taken off while that slot holds a charm. |
| **Angelic Feather** | Charm | A fall's harm comes from your food instead of your health (half a point of food or saturation for each point of harm) while your food bar is not empty; and you jump a little higher (about 1.42 blocks instead of 1.25, still short of a fence). |
| **Kraken Shell** | Charm | The same for drowning. |
| **Infernal Claws** | Charm | The same for fire, burning and hot floors (magma blocks); not lava. |
| **Angelheart Vial** | Charm | A blow that would kill you leaves you on 2 hearts with Regeneration II for 5 seconds. The vial is used up. |
| **Phoenix Down** | Charm | A blow that would kill you leaves you at full health with Regeneration II and Fire Resistance for 10 seconds, and the down becomes an Angelic Feather where it was worn. Until then it is a feather as well (its fall and jump). |
| **Amphibian Boot** | Feet | You swim faster (water movement +0.5, half of Depth Strider's most), and your air lasts about twice as long. |
| **Ice Breaker** | Feet | +0.1 knockback resistance. A fall that hurts you sends a wave through the ground: the nearest hostile creatures (up to 12) within 3 blocks, a block more for each 4 points of harm (at most 6), take 2 damage, are thrown back and are slowed for 2 seconds, wherever you may harm them. |

- Two of a kind never add up: a second feather, boot or Ice Breaker adds nothing, and a feather and a Phoenix Down jump as
  one. A second vial waits its turn for the next death; a vial worn beside a Phoenix Down answers first.
- A death save does not answer the void or `/kill` (harm that passes through invulnerability), a totem held in either
  hand (the totem answers, as vanilla's), or a death in a dream (the dream ends as it would).
- A codex entry, **Belts, Charms and Boots** (Relics category), shows every recipe and says all of this.
- The kinetic belt that links two pulleys (`jugcraft:belt`, unchanged) is now called the **Drive Belt** in English, so
  the two belts are told apart.

## How it works

- Data (`tools/concordance_trinkets.py`): the eight items, their recipes (seven shaped, and the owner's shapeless Phoenix
  Down), the two new slots (`data/trinkets/slots/legs/charm.json` and `feet/boots.json`: the owner's icons, no cosmetic
  copies), the item tags for all three slots, `data/trinkets/entities/jugcraft_wayfaring.json` (players get all three),
  the codex entry and the slot names. `tools/concordance_equivalence.py` excludes the eight from the Assayer's Scale;
  `tools/concordance_progression.py` gains leather and the golden apple as Overworld sources.
- Java (`concordance/trinket`):
  - `WornTrinketItem` is a Trinkets callback. Its attributes are Trinkets modifiers added while it is worn where Trinkets
    applies effects, each named `jugcraft:wayfaring/<kind>/<attribute>`, never by slot, so two of a kind give one
    modifier (Trinkets replaces a modifier with the same name and keeps it while any copy is worn). The Phoenix Down's
    kind is the feather's. `canEquip` asks whether the wearer understands Relic Lore; it runs on the server and, for
    one's own screen, on the client, which holds its own player's research. The belt's `canUnequip` refuses while a
    Charm slot beyond the first holds something, then defers to Trinkets' default (Curse of Binding).
  - `Wayfaring` registers the items, the Charm slot's count attribute (`SlotAttributes.createAttributeForSlot`; the belt's
    modifier is +1 on it) and three listeners:
    - `ALLOW_DAMAGE`: a worn feather or down (plain fall damage), shell (drowning) or claws (`in_fire`, `on_fire`,
      `campfire`, `hot_floor`) refuses the harm and charges the wearer `2 × harm` exhaustion, if the food bar is not
      empty and Relic Lore is understood.
    - `ALLOW_DEATH`, in its own phase ordered before the default one: the death saves. Dreaming's listener runs in the
      default phase and ends an open dream as a death; only before it can a save see the dream and decline.
    - `AFTER_DAMAGE`: after a plain fall (`minecraft:fall`, not an ender pearl's landing) that hurt the wearer, on foot,
      the Ice Breaker's wave.
  - Everything the slice does to a creature goes through `ConcordanceEffects.apply` as an item's effect: the wave's harm,
    throw and Slowness (so PvP, parties, claims, protected creatures and tolerance tags apply; only `Enemy` creatures are
    chosen) and the saves' statuses on the wearer. What shows is the shared signs (`Signs.show`).
- Client: nothing new. Trinkets draws the slots with the owner's icons (GUI sprites under
  `textures/gui/sprites/container/slots/`); the items are the owner's plain generated models.
- Nothing ticks: the attributes are Trinkets modifiers and the rest answers damage events.

## Connections

- Tier and unlock: Practitioner, after Relic Lore understood (examine relic-like things, then study one at a
  Lampwright's Bench). Before that the items can be crafted but not put on.
- Input producer: Overworld leather, iron, gold, amethyst, phantom membranes, feathers, prismarine shards, an ink sac, a
  nautilus shell, magma blocks, golden apples, glass bottles, dried kelp and flint (crafting table).
- Output consumer: the wearer (falls, drowning, fire, death, swimming, air, knockback); hostile creatures, through the
  shared effect boundary (the wave); the Charm slot (the belt).
- Costs: materials only. A vial is used up by its save (a golden apple each); a Phoenix Down costs three vials and
  becomes a feather. The charms' protection is paid for in food. No Focus, charge or energy.
- Technology connection: none needed. Magic connection: Relic Lore gates every item; the wave and the saves' statuses are
  Concordance item effects.
- Reachable entry path: every recipe input has an Overworld source in the progression graph (`check_wayfaring` checks
  each); Relic Lore's own entry path is unchanged. No research, practice or stage requires these items, so no unlock can
  become circular.
- Solo, trade and cooperative routes: entirely solo; the items are ordinary stacks.

## Balance

- **Units.** Harm in health points; exhaustion as vanilla counts it (4 is a point of saturation or food); attributes as
  vanilla's added values; ticks.
- **The charms' protection is paid in food**, point for point at half a food point per point of harm, and stops when the
  bar is empty. A 10-block fall (7 harm) costs 3.5 points; standing in fire about a point a second; drowning about one
  a second. The Free Runners and the charged Exosuit boots already prevent fall harm outright, from an armour slot.
- **Death saves against the totem.** A Totem of Undying cannot be crafted; the vial can, so it does less: 2 hearts and 5
  seconds of Regeneration II (about 2 more hearts), against the totem's 1 health with 45 seconds of Regeneration II, 5 of
  Absorption II and 40 of Fire Resistance. Each costs a golden apple (8 gold). The Phoenix Down (3 vials) rises at full
  health. One save per death, and a held totem keeps its turn.
- **The wave** is 2 damage, a 0.6-block-a-tick throw and 2 seconds of Slowness I to at most 12 hostile creatures, and only
  after a fall that hurt the wearer (the feather's absorbed falls make none). A Hearthbinder's Cinderbolt deals 3.
- **Attributes.** Jump +0.03 (below a fence), water movement +0.5 (the attribute's most is 1, Depth Strider III's), oxygen
  bonus +1 (Respiration I's), knockback resistance +0.1 (one netherite piece's).
- **No conversion loops:** no recipe makes a material; the Phoenix Down's feather was one of its inputs.

## Multiplayer and persistence

- Server authority: Trinkets applies the modifiers on the server; the absorption, saves and wave are decided on the server
  from damage events. The client's `canEquip` reads only its own synced research, as the server does. No packets.
- Failure behaviour:
  - before Relic Lore: cannot be put on (an item put on by other means, such as a command, gives its attributes but
    answers no damage event);
  - held, loose, in a container or (if a server enables them) in a cosmetic belt slot: nothing;
  - the charms with an empty food bar: the harm lands; the claws against lava: the harm lands;
  - the saves against the void, `/kill`, a held totem or a death in a dream: no save, nothing spent;
  - the wave after an ender pearl, a fall that did no harm, from the saddle, or against a creature the wearer may not
    harm (a villager, a claimed or protected creature): nothing;
  - the belt with its added charm: stays on; were its slot to go another way (a command), Trinkets drops the charm at
    the wearer's feet.
- Persistence: vanilla item stacks; Trinkets saves its slots and the belt's slot-count modifier (as a persistent
  modifier named `jugcraft:wayfaring/leather_belt/slot_count/legs/charm`, which must never change: a renamed one would
  leave a Charm slot behind). No new saved data. Save compatibility: additive.
- Disable behaviour: with `concordance.enabled=false` the recipes do not load and nothing answers the damage events; the
  items, slots and attributes stay registered.
- Dreams: any worn trinket still refuses a dream ("accessories", unchanged). A pre-existing gap, not changed here:
  `Dreaming.end` gives back only the main inventory, so a trinket put on during a dream leaves the dream with the dreamer.

## Dependencies and assets

No new dependency. Framework use: **Trinkets Updated** (three slots given by data, one defined by Trinkets; the
callback modifiers, including a slot-count attribute; `canEquip`/`canUnequip`), **Fabric API** (the three damage events
and an event phase), **Modonomicon** (the codex entry).

**Provenance.** The files below are from the owner's library (`art/owner-library/originals`): the magic collection
(supplied 8 October 2026, [MAGIC_ASSETS.md](../../art/owner-library/MAGIC_ASSETS.md); folders `jymbelics` and
`jymbaquary`) and, for the charm slot icon, the Blocks folder's `Trinket Type Mod`. The owner states they made this work
"using other Minecraft mods as a base" (a relics mod and a reliquary mod) and then "completely redid and remade all the
stuff" ([PROVENANCE.md](../../art/owner-library/PROVENANCE.md)); this record repeats that declaration, which is not an
independent rights audit. The project uses the files under the owner's project-use authorization
([LICENSE_POLICY.md](../../LICENSE_POLICY.md), owner-supplied collections). The source folders' names are kept here as
labels; nothing in the game uses them. Read for notices: the five `jymbaquary` icons carry Photoshop 23.1 XMP packets
(created 23 January 2022), the Leather Belt and Ice Breaker strips Photoshop 22.1 packets (October 2021 and April 2022),
the Amphibian Boot and both slot icons only colour chunks; none names an author or rights, and
`catalog/legacy-metadata.csv` has no row for them. They are byte-for-byte copies under Jugcraft names (an animation
sidecar with its line ends made LF), written by `tools/owner_art.py` from `tools/concordance_trinkets.py` `OWNER_FILES`
and `OWNER_BLOCKS_FILES` and checked byte for byte by `check_mod_data`. `python3 tools/owner_art.py --provenance trinkets`
prints this table:

| Runtime file (under `assets/jugcraft`) | Owner's file | SHA-256 of the owner's file |
|---|---|---|
| `textures/gui/sprites/container/slots/feet.png` | `originals/Magic/assets/jymbelics/textures/slot/empty_feet_slot.png` | `f15fb69d3d0fd147f48457560eb5f8bf58529dde17318a5ac3e3f509fbc0c551` |
| `textures/item/amphibian_boot.png` | `originals/Magic/assets/jymbelics/textures/item/amphibian_boot.png` | `0bb844a015db5be3274df912fdbe3bb6d5d3a017611f1e05ee186f85301b2b40` |
| `textures/item/amphibian_boot.png.mcmeta` | `originals/Magic/assets/jymbelics/textures/item/amphibian_boot.png.mcmeta` | `39092f18e788de94796dbcf85e8ccf5a2b22ada0ba7c0ea3d902b6b5d1f1618b` |
| `textures/item/angelheart_vial.png` | `originals/Magic/assets/jymbaquary/textures/item/angelheart_vial.png` | `d8bfd161cf04c3c17b52206293773d6cce3cbe2638333f3e1698b22c8eba1b78` |
| `textures/item/angelic_feather.png` | `originals/Magic/assets/jymbaquary/textures/item/angelic_feather.png` | `3fb19d8705af7c911642b814290ebf20cb8b1faa79cc4286b477864a75f78827` |
| `textures/item/ice_breaker.png` | `originals/Magic/assets/jymbelics/textures/item/ice_breaker.png` | `099c1a26b62e134470276ded1fb1e20a68eabd4cf2aaf434979170e4cd8c8f0c` |
| `textures/item/ice_breaker.png.mcmeta` | `originals/Magic/assets/jymbelics/textures/item/ice_breaker.png.mcmeta` | `575cb57f14a7855ae586fd5a4eeec790435ece5d322f27d2b2f5a8c114b5e02e` |
| `textures/item/infernal_claws.png` | `originals/Magic/assets/jymbaquary/textures/item/infernal_claws.png` | `4aba40a2562033221254dcb507a3021b1fd475d07ad07c2b1ac059870b79c7d2` |
| `textures/item/kraken_shell.png` | `originals/Magic/assets/jymbaquary/textures/item/kraken_shell.png` | `02b8741b3f6a0ea5d8a0096a0250074158a61cacee41b68ae5bb11abefd68ede` |
| `textures/item/leather_belt.png` | `originals/Magic/assets/jymbelics/textures/item/leather_belt.png` | `b0c028bbb6f45e6f0e8493abec416b12d67960d29e83871d5d3d2d7026febaf2` |
| `textures/item/leather_belt.png.mcmeta` | `originals/Magic/assets/jymbelics/textures/item/leather_belt.png.mcmeta` | `f14cc62fbcf862476d19facd33cb4fa569616b597497eb8c5b110028edd0521c` |
| `textures/item/phoenix_down.png` | `originals/Magic/assets/jymbaquary/textures/item/phoenix_down.png` | `c35cf03feb47ee12c3b26ce7aa5c435a5f98be38bd648b2fb17037afd7d3f4c2` |
| `textures/gui/sprites/container/slots/charm.png` | `originals/Blocks/Trinket Type Mod/slot/empty_charm_slot.png` | `9862b3a181dff7ae5cb791bf093239613a7cfe9e30d729c5ec9ac80185a9fd72` |

Also taken from the owner's files, as data rather than copies:
- the eight item models (`models/item/<id>.json`), each the owner's plain generated model with its texture renamed
  (the `jymbelics` ones write their parent as `item/generated` and carry an empty `overrides` list, both dropped);
- the eight display names, from `jymbelics/lang/en_us.json` and `jymbaquary/lang/en_us.json`. Only the names are taken:
  the tooltips, codex text and numbers are Jugcraft's, and the rest of those files (relic levelling and experience text,
  guide pages, other items) is left out;
- the kind of effect each item has, from the owner's item descriptions (a fall's, drowning's and fire's harm taken for
  hunger; a vial that saves once and breaks; a down that saves at full health and reverts to a feather, keeping the
  feather's effects; swimming and breath; knockback resistance and a landing shockwave; a belt adding charm slots);
- the Feet slot (two, named "Feet": `data/jymbelics/curios/slots/feet.json`) and the charm slot (one, more from belts),
  ported to Trinkets slots in the feet and legs groups;
- the Phoenix Down recipe (`data/jymbaquary/recipe/phoenix_down.json`: three vials and a feather, shapeless), its ids
  renamed. The owner's other recipes use the base mod's own ingredients (mob drops and essences it adds), so Overworld
  recipes are used instead.

Not imported, and why: the owner's chest-loot additions for the feather and vial (progression never relies on loot), the
Leather Belt's and Amphibian Boot's worn sheets (no geometry exists for them yet; see Rollout), the other 161 items of
the two folders (later parts, below).

## Verification

Local (this branch, before CI):
- `python3 tools/check_mod_data.py`: PASS, with the new `check_wayfaring`. Mutation test: 25 deliberate breakages, each
  caught (among them a changed number, the save moved to the default phase or stripped of its dream or void guard, the
  claws taking lava, the Phoenix Down named apart from the feather, a modifier named by slot, an open `canEquip` or belt,
  a cosmetic charm slot, Trinkets' belt slot redefined, a Nether ingredient, an unconditioned recipe, a renamed item, and
  the two belts sharing a name); every file restored after.
- `python3 tools/owner_art.py --check` (every imported file matches its source), `python3 scripts/check_repository.py`
  and `python3 tools/check_icon_maps.py`: PASS (the icon check's 11 warnings are older items').
- No Minecraft jar here: compilation and the game tests run only in CI.

Tests (in CI, not yet run):
- Server, `ConcordanceWayfaringGameTests`: the items and slots as designed (sizes, the owner's icons, no cosmetic copies,
  tags, every attribute present, never weighed, the two belts' names); only Relic Lore puts them on and two of a kind
  never add up (each item's modifiers from two slots, a feather and a down jumping once); the belt keeps its charm; the
  charms take their own harm from food and nothing else (not lava, not held, not with an empty bar, not before Relic
  Lore); the vial, then the down, answer death through the real event, and the void, a held totem and Relic Lore not
  understood do not; a vial put on during a dream does not answer a death in it, and the dream ends as a death; the
  wave reaches a zombie and not a villager, a claimed zombie, one beyond a light fall's radius, nor after an ender
  pearl or before Relic Lore, and at most twelve.
- Client, `ConcordanceWayfaringClientGameTests`, on the real ticking player: the belt's second Charm slot on the server
  and the client; the worn attributes each present once; the slot gone again without the belt; screenshots of the eight
  icons and the inventory, for a person to look at.

Not yet run: a person looking at the screenshots, the Trinkets screen opened by hand (the slot icons and the grey slot
names before Relic Lore), a real fall, drowning or burning in survival, and a two-client dedicated server.

## World and event applicability

No worldgen, creatures, loot or seasons.

## Rollout and open questions

- New stable ids: items `jugcraft:leather_belt`, `angelic_feather`, `kraken_shell`, `infernal_claws`, `angelheart_vial`,
  `phoenix_down`, `amphibian_boot`, `ice_breaker`; Trinkets slots `legs/charm` and `feet/boots`
  (`data/trinkets/entities/jugcraft_wayfaring.json`, which also gives players Trinkets' `legs/belt`); modifier ids
  `jugcraft:wayfaring/<kind>/<attribute>`; codex entry `relics/wayfaring`. Save compatibility: additive. The kinetic
  belt's English name changed (Leather Belt to Drive Belt); its id did not.
- **Simplified from the owner's text, for a concrete reason:**
  - the Ice Breaker's faster falling and grip on ice, and the Amphibian Boot's speed in the rain: no event or attribute
    gives them without a mixin or a per-tick check;
  - the Phoenix Down's "variety of buffs": two (Regeneration II and Fire Resistance);
  - the base mods' relic levelling and experience: Relic Lore's mastery already stands for it.
- **Deferred:** drawing the belt and boots on the body (part 1b: the owner's worn sheets for the belt and boot exist, but
  no geometry; Trinkets' data-driven renderer can attach a model once one is fitted); the other worn relics (parts 2, 3
  and 5); the held relics and staves (parts 4a and 4b, after the Ars-based core).
- **For the owner to decide** (nothing above waits on them): whether the guns in the Reliquary folder become one more
  gun line; whether narrow mixins are approved for the relics that need them (walking on water or lava, slippery
  ground, Riptide without rain, a chorus fruit's teleport, barter results, mob neutrality, healing hooks, the backstab
  bonus); names and designs for the 12 unnamed legacy icons; and the terms of `jymbelics/sounds/ricochet.ogg`, whose tags
  name a sound-effects publisher (it matters only for a later part's Shadow Glaive).
- Skipped: `researching_table` (removed from the base mod; Relic Lore and Artifice cover it), `relic_experience_bottle`
  (levelling), `blank_rune` (a model with no texture), `witch_hat` (its id is taken by Jugcraft's own Witch Hat).
