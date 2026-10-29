# Costume disguises

Status: implemented in source; **not yet through CI**. Nothing here has been compiled or run as a game test (below). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for more Halloween ideas and chose costume disguises among them, asking that "the costume disguises work year round". This is one of the season pieces, each in its own pull request (the others: Headless Horseman sightings, the Blood Moon, the Graveyard Rising, the Haunted House contest, and the Horseman's lair and its fight).
Owner: @jimbozoomer-byte

Target milestone and tier: Milestone 3 (first homestead), as the costumes are ([halloween-nights.md](halloween-nights.md)). Nothing new is crafted: a disguise is a costume a player already has, worn.
Primary specialty and supported player role: play and atmosphere, for every player; a gentle way through the night for builders, farmers and explorers who would rather not fight.

## Player experience

Wear a costume and the monsters it imitates take you for one of their own. All year, not only in the Halloween season:

| Disguise | Costumes (worn on the head) | Fools |
| --- | --- | --- |
| Skeleton | Skeleton Suit | skeletons, strays, bogged |
| Walking dead | Mummy Wraps | zombies, husks, zombie villagers, drowned |
| Witch | Witch Hat | witches |
| Werewolf | Werewolf Mask, The Alpha's Hood | werewolves |
| Ghost | Ghost Sheet | vexes |
| Cat | Cat Ears and Tail | creepers |
| Night | Bat Wings, Vampire Cape | phantoms |

A fooled monster never picks you as its target: zombies shamble past a mummy, skeletons hold their fire at one of their own, a creeper will not creep up on a cat, and phantoms circle a bat without swooping.

**Until you strike.** Hurt a fooled monster and that one knows you: it fights back as ever (for as long as it remembers who hurt it, five seconds, and then for as long as it keeps you in its sights). The others of its kind are still fooled.

Limits:
- A costume fools only its own kind: cat ears do nothing for a skeleton.
- A monster that was already after you before you dressed keeps after you until it loses you.
- No boss is ever fooled, nor any monster not in the table (spiders, endermen, pillagers and the rest see you as you are).
- The Scarecrow Hat, the carved pumpkin and the Tony Tony Chompa Costume are costumes for trick-or-treating and the contest but disguise you as nothing.

### Settings

In `config/jugcraft.properties`, `costumes.disguises` (`on` or `off`, default `on`) turns disguises on or off for the whole server. Disguises do not depend on the Halloween season settings.

## Connections

- **Input producer:** the costumes, as before: crafted (the outfits, the Witch Hat, the Ghost Sheet), bought from the Halloween Peddler, dropped by costumed mobs, or (The Alpha's Hood) won from the Alpha Werewolf. Every costume has a crafting or drop route outside the season, so a disguise is never seasonal-only.
- **Output consumer:** safe passage. It saves armour and time; it gives no items.
- **Technology and magic:** none.
- **Required vs optional:** optional. Nothing in progression needs a disguise, and nothing is gated behind one.
- **Trade and solo routes:** a costume can be made alone or traded.

## Balance and automation

- **No new rewards:** a disguise gives no loot, experience or candy; it only stops some monsters choosing you.
- **A head slot, not armour:** every disguising costume is worn on the head, so it costs a helmet's protection; most monsters in the world are still dangerous.
- **Not a farm cheat:** a monster you hurt fights back, and monsters you never fight drop nothing.
- **Bosses excluded:** checked by the audit against the list of bosses Fright cannot touch (`#jugcraft:frightless`) and the vanilla bosses.

## Multiplayer and persistence

- **Server authority:** decided only on the server, when a monster picks a target (`Mob.setTarget`), from what the player is wearing there. Clients claim nothing.
- **Bounded work:** one check of at most seven tags and four worn slots each time a monster picks a player as its target; nothing runs each tick.
- **Saved state:** none new. A disguise is only what is worn.
- **Chunks:** nothing is loaded.
- **Still to do:** the two-client dedicated-server playtest.

## Dependencies and assets

No new dependency, model or texture. One new mixin (`DisguiseTargetMixin`, on `Mob.setTarget`, server side), because no Fabric event lets a mod refuse a monster's choice of target.

New IDs (all under `jugcraft`):
- the item tags and entity type tags `disguises/skeleton`, `disguises/walking_dead`, `disguises/witch`, `disguises/werewolf`, `disguises/ghost`, `disguises/cat`, `disguises/night` (a data pack can add costumes or monsters to any kind);
- the config option `costumes.disguises`.

Nothing is renamed or removed.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s agriculture checks (`check_agriculture`, which now also runs `check_disguises`) | Pass |
| Deliberate breaks of the Java, the table, the tags and the config, one at a time (below) | 8 of 8 failed it |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's fourteen tags only (the files it rewrote for other features were put back) |
| `javac` on the new and changed Java and the test class, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit checks that `Disguises.KINDS` and its option are `tools/agriculture.py`'s `DISGUISES`, that the option is on by default, that the mixin is registered and cancels `setTarget` when `Disguises.fooled`, that a hurt monster still fights back, that each kind's tags hold exactly the table's costumes and monsters, that every disguising costume is a trick-or-treat costume, and that no boss (or tag) is in any kind.

The 8 deliberate breaks: the kinds out of order in the Java; the option off by default; the mixin unregistered; a struck monster still fooled; the mixin never cancelling; a tag holding a monster the table does not; a boss in the table and its tag; a non-costume item in the table and its tag.

The game tests (`DisguiseGameTests`):
1. a zombie targets a player without a costume, and never one in Mummy Wraps;
2. a zombie the player in Mummy Wraps has hurt targets them;
3. Cat Ears and Tail fool a creeper but not a skeleton; a Skeleton Suit fools the skeleton;
4. with `costumes.disguises` off, a zombie targets a player in Mummy Wraps.

Not run: the game tests (CI will run them), the two-client dedicated-server playtest, and play by hand.

## World and event applicability

All year, in every dimension. The Halloween season's end changes nothing.

## Rollout and open questions

- **Which monsters:** the table above is a first pairing; the owner may want more kinds (a spider costume, an enderman mask) after playing.
- **Feedback:** a fooled monster gives no sign it is fooled beyond leaving you alone; a small particle or sound may help if playtesting shows players unsure.
