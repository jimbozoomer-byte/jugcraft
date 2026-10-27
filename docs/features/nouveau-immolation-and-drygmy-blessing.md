# Immolation and Drygmy's Blessing take effect

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor asked for the worst problems in the pack to be fixed (26 October 2026); two Jug Nouveau statuses that nothing gave, and two statuses both called "Immolate", were on the list.

Owner: jimbozoomer-byte
Target milestone and tier: Jug Nouveau potions and threads, mid magic
Primary specialty and supported player role: magic; fire casters and mob farmers

## Player experience

- **Immolation** (renamed from "Immolate"): the Immolation thread already made fire spells stronger after you cast while burning, but it kept that in a hidden timer and never showed the status. It now gives the Immolation status, for 10 seconds per thread level, and fire spells deal 2 more damage per level of the status, as the supplied thread text describes. The status is now shown on the HUD.
- **Drygmy's Blessing**: the looting bonus was already read when a mob dies, but nothing gave the status.
- **Potions:** both now brew in a brewing stand from an Awkward Potion, with the usual redstone (longer), glowstone (level II), gunpowder (splash) and dragon's breath (lingering) steps:

  | Potion | Reagent | Effect |
  | --- | --- | --- |
  | Potion of Immolation | Fire Essence | fire spells +2 damage (+4 at level II); 3 min, 8 min long, 1.5 min at level II |
  | Potion of Drygmy's Blessing | Drygmy Token | +1 looting (+2 at level II); same durations |

- The Jimtafacts burning status keeps the name **Immolate**; the Jug Nouveau one is now **Immolation**, the name the supplied thread text uses, so the two no longer share a name.

## Connections

- **Input producer:** Fire Essence (Imbuement Chamber) and the Drygmy Token (befriending a Drygmy); vanilla brewing.
- **Output consumer:** fire spells resolved through the shared Concordance effect system (`ConcordanceEffects`, which calls the thread hook), such as Jug Nouveau's fire glyphs; mob kills with looting.
- **Costs:** one reagent per three potions, as for the other Jug Nouveau potions.
- **Failure behavior:** unchanged. With Jug Nouveau switched off the statuses do nothing, as before.

## Balance and automation

The numbers follow the existing ones: the thread gave 2 per level, and the other Jug Nouveau potions use the same durations and level II amplifier. Flagged for the owner as provisional.

## Multiplayer and persistence

Six new potion IDs (`jugcraft:nouveau_immolate`, `_long`, `_strong`, and the same for `nouveau_looting`) and 30 brewing recipes. The Immolation thread's state used to be lost on logout; as a status it now saves with the player. No existing IDs change.

## Verification

New server game tests in `NouveauStatusSourcesGameTests`:
- `immolationAndDrygmysBlessingBrew`: all six potions are registered and the brewing recipes load;
- `immolationAddsToFireSpellsOnly`: a fire spell gains 4 damage under Immolation II, and a non-fire spell gains nothing.

Local: `python tools/generate_material_data.py` wrote the brewing recipes and names. `python tools/check_mod_data.py` passed (7,797 material IDs, data files and recipe audit) and `python scripts/check_repository.py` passed.

CI: fork [run 40426167694](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40426167694) on 06e42ff62: green. The mod built, the generated-data and material checks passed, the server game tests passed (with and without optional integrations), and all sixteen client shards passed.
After merging main (5b8d6520c): fork [run 40465636343](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40465636343) on 70cf49d8a, green, every job and all sixteen client shards.

Not run: a human brewing the potions or casting under them in game.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
