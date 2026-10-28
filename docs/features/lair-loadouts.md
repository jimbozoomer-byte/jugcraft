# Lair loadouts: how long each keeper takes, with melee, magic and guns

Status: implemented in source for Concordance Chunk E (E4, shared expedition polish, part 3 of 3), not merged. It is a
model worked out from the game's own numbers, not a playtest: nobody has timed these fights by hand.
Proposal issue: none. The owner chose "Finish Chunk E (E4)" on 28 October 2026. E4 says to "measure against current
melee, magic and gun loadouts" ([the delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md)).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: the lairs' own tiers. The loadouts are steel-tier, as a player reaching most keepers carries.
Primary specialty and supported player role: the owner and balance reviewers, and players planning a fight.

## Player experience

Nothing in the game changes. This record answers how each keeper compares with the others, and whether a loadout or a
rule decides a fight. `tools/lair_loadouts.py` works it out, and `tools/check_mod_data.py` (`check_lair_loadouts`) keeps
the tables below equal to its output, so they follow the game when a number changes.

## What each fight asks

The keeper's health and armour, its rule, and what it takes of a blow with the rule answered (open) and not (shut):

| Keeper | Health | Armour | The rule | Takes, open / shut |
|---|---|---|---|---|
| Vesperine | 400 | 10 | her skulls guard her: strike them down | ×1 / ×0.5 |
| Madame Tatterlace | 360 | 8 | keep your footing on her lace | ×1 / ×1 |
| The Yeti King | 420 | 10 | lead his charge into an ice column | ×1.3333 / ×1 |
| The Cinder Tyrant | 440 | 10 | quench him in a flooded trough | ×1.25 / ×0.5 |
| The Mire Hag | 360 | 6 | relight the lamps to find her | ×1 / ×1 |
| The Crypt Lich | 400 | 8 | break his seals to expose him | ×1 / ×0 |
| The Iron Dreadnought | 440 | 12 | throw the lit pylon's breaker | ×1 / ×0.25 |
| The Alpha Werewolf | 400 | 8 | ring the bell to flush him; silver bites | ×1 / ×0.5 (silver ×1.5 / ×1) |
| The Cydonian Sentinel | 480 | 12 | strike its open core | ×2 / ×1, no armour open |
| The Pumpkin King | 400 | 6 | pick his harvest clean to leave him bereft | ×1.5 / ×1 |
| The Headless Horseman | 420 | 8 | relight the braziers he snuffs | ×1.5 / ×0.5 |

## The times

Seconds of hitting, alone, for each keeper and loadout: **open** (every blow lands while the rule is answered) and
**shut** (none does). A real fight lies between the two.

| Keeper (open / shut) | Steel Sword | Steel Battle Axe | Silver Dagger | Lance of Dawn alone | Garrison Rifle | Ranger Rifle |
|---|---|---|---|---|---|---|
| Vesperine | 52 s / 116 s | 59 s / 2 min 19 s | 104 s / 3 min 44 s | 10 min 00 s / 20 min 40 s | 30 s / 65 s | 34 s / 82 s |
| Madame Tatterlace | 42 s / 42 s | 49 s / 49 s | 84 s / 84 s | 8 min 56 s / 8 min 56 s | 24 s / 24 s | 29 s / 29 s |
| The Yeti King | 39 s / 55 s | 43 s / 63 s | 77 s / 108 s | 7 min 52 s / 10 min 32 s | 23 s / 31 s | 25 s / 37 s |
| The Cinder Tyrant | 44 s / 2 min 07 s | 49 s / 2 min 33 s | 88 s / 4 min 05 s | 8 min 48 s / 22 min 48 s | 25 s / 72 s | 30 s / 90 s |
| The Mire Hag | 39 s / 39 s | 46 s / 46 s | 75 s / 75 s | 8 min 56 s / 8 min 56 s | 22 s / 22 s | 25 s / 25 s |
| The Crypt Lich | 47 s / never | 54 s / never | 93 s / never | 10 min 00 s / never | 26 s / never | 32 s / never |
| The Iron Dreadnought | 65 s / 5 min 06 s | 73 s / 6 min 19 s | 2 min 08 s / 9 min 46 s | 11 min 04 s / > 30 min | 37 s / 2 min 47 s | 44 s / 3 min 42 s |
| The Alpha Werewolf | 47 s / 103 s | 54 s / 2 min 04 s | 58 s / 93 s | 10 min 00 s / 20 min 40 s | 26 s / 59 s | 32 s / 74 s |
| The Cydonian Sentinel | 22 s / 71 s | 29 s / 79 s | 43 s / 2 min 20 s | 5 min 44 s / 12 min 08 s | 12 s / 40 s | 16 s / 46 s |
| The Pumpkin King | 27 s / 43 s | 34 s / 51 s | 53 s / 85 s | 6 min 32 s / 10 min 00 s | 16 s / 24 s | 18 s / 30 s |
| The Headless Horseman | 31 s / 108 s | 37 s / 2 min 10 s | 61 s / 3 min 29 s | 6 min 48 s / 21 min 44 s | 17 s / 60 s | 19 s / 76 s |

The loadouts:
- **Steel Sword**: 6.5 a blow at 1.6 a second (the sword's 3, the hand's 1 and steel's 2.5).
- **Steel Battle Axe** (Arms): 11.5 a blow at 0.7 a second.
- **Silver Dagger**: 5 a blow at 2.2 a second. It is the only silver weapon (`#jugcraft:silver_weapons`).
- **Lance of Dawn alone**: the mastered Lance with no Spell Power gear: 5 a beam every 2 seconds while Focus lasts (4 a
  cast from a pool of 20, one back every 2 seconds), then as Focus returns. Armour does not reduce it.
- **Garrison Rifle**: 4 a shot every 3 ticks, 30 a magazine, 53 ticks to reload, piercing 2 armour.
- **Ranger Rifle**: 10 a shot every 10 ticks, 10 a magazine, 45 ticks to reload.

## How it is worked out

- One player and the server's default `lairs.boss_health`: a party of four faces 2.5 times the health with four times
  the blows, so each player's share of the work is 0.625 of the times above.
- Each blow loses to the keeper's armour as vanilla's armour does: toughness 0, as every keeper's. The Garrison Rifle's
  pierce lowers the armour first. The Lance ignores armour. An open Cydonian Sentinel's core has none.
- A melee blow within 10 ticks of the last that counted deals nothing, as vanilla's hurt cooldown does. Bullets skip it
  (`#minecraft:bypasses_cooldown` holds `jugcraft:bullet`).
- Only hitting time counts. Not counted:
  - each keeper's waking and unhurt phase changes;
  - healing (Vesperine's souls, the Lich's Soul Drain, the Pumpkin King's fed fire);
  - Vesperine's skulls' own health (80 each);
  - decoys that eat blows (the Mire Hag's Likenesses);
  - the Alpha's hunts, when he is hidden and cannot be hurt.
- No fire loadout is measured: whether Spell Power's fire school counts as fire, which the Cinder Tyrant ignores and the
  Pumpkin King takes half again, is Spell Power's to say.

## Findings

- **The rules decide the fights.** Answering a keeper's rule cuts the time by half or more against seven of the eleven:
  - the Iron Dreadnought by nearly five times;
  - the Crypt Lich cannot be hurt at all until his seals break.

  Only Madame Tatterlace and the Mire Hag take every blow alike; their rules are about footing and finding her.
- **Guns are the quickest, by about half.** A Garrison Rifle takes about 0.55 of a steel sword's time against every
  keeper, because its bullets skip the hurt cooldown and pierce 2 armour. Its bullets are never silver, so a shut Alpha
  Werewolf halves them.
- **The silver dagger loses half its blows to the hurt cooldown.** At 2.2 blows a second, every other one lands inside
  the 10-tick cooldown, so it is slower than a steel sword against every keeper but a shut Alpha Werewolf: 93 seconds
  against the sword's 103. Flushed, it is 58 against the sword's 47. This is a balance point for the owner; this record
  changes nothing.
- **The Lance alone is support, not a boss-killer.** It takes 6 to 11 minutes open, held back by Focus at about 0.6
  damage a second sustained. This agrees with the Concordance's own benchmark
  ([arcane-concordance-baselines.md](arcane-concordance-baselines.md): "no single invocation carries a fight").
- **The quickest fights open:** the Cydonian Sentinel (its core twice as soft, and bare) and the Pumpkin King (bereft).
  **The longest shut:** the Iron Dreadnought and the Cinder Tyrant, both plated or hot to the halving.

## Connections

- Existing input producer: the keepers' numbers (`tools/<keeper>.py`, held to their Java by each keeper's audit), the
  arms (`tools/arms.py`, `tools/gear.py`), the guns (`tools/guns.py`) and the Concordance (`tools/concordance.py`). The
  silver dagger's numbers are held to `agriculture/JugcraftAgriculture.java` and `agriculture/Werewolf.java`.
- Existing output consumer: this record, and the owner's Chunk E checkpoint.
- Required vs optional: nothing in the game reads it.

## Balance and automation

No change to the game. The findings are inputs to the owner's balance decisions.

## Multiplayer and persistence

Not applicable: a tool and a record.

## Dependencies and assets

None.

## Verification

- `python3 tools/lair_loadouts.py` prints both tables above.
- `check_lair_loadouts` (in `python3 tools/check_mod_data.py`) checks:
  - this record's tables are the tool's;
  - each keeper's numbers are its own tool module's;
  - the silver dagger's damage, speed and silver bonus are the Java's;
  - it is the only silver weapon;
  - bullets skip the hurt cooldown.
- Not done: timing any fight by hand or in a game test.

## World and event applicability

Not applicable.

## Rollout and open questions

- The numbers are a model's; actual completion times need play.
- The silver dagger's speed and the hurt cooldown are an owner's call.
- When a keeper's rule changes, its row in `KEEPERS` must change with it: the audit holds the numbers, not the meaning.
