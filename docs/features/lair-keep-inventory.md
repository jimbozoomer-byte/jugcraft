# Keeping your inventory in boss fights

Status: implemented (draft PR)
Proposal issue: none. This is an owner instruction from 22 October 2026: "players should not lose their inventory on death in boss fights".
Owner: jimbozoomer-byte
Target milestone and tier: the shared lair framework ([hollow-acre.md](hollow-acre.md#lairs-the-shared-rules)), for every lair and its boss; no tier of its own.
Primary specialty and supported player role: combat. It supports every player who fights a boss.

## Player experience

A boss fight is meant to be a fight you can try again, not one that costs your kit. Death in a lair works like vanilla's keepInventory rule, but only in a lair:
- **Nothing is dropped**: no items and no experience orbs. Curse of Vanishing items stay too, as keepInventory keeps them.
- **You respawn outside the lair** as before, with:
  - every inventory slot as it was;
  - armour still worn and the offhand still held;
  - the same hotbar slot selected;
  - all your experience, its level and progress.
  
  A chat line tells you: "You fell in the lair, but kept everything you carried and all your experience".
- **Your visit ends**, as before. Your party can go on, and you can rejoin through the gate while it is open. When the fight ends, your share of the spoils is kept for you until you respawn, as before (`LairRewards`).
- **Dying anywhere else is unchanged**: you still drop everything.

**Grave Goods** remain as a safety net. Anything still dropped at your feet when you die in a lair is gathered at once and handed back when you respawn. Such drops would come from another mod's death drops, since the game itself now drops nothing.

## Connections

- Existing input producer: the lairs and their bosses (`lair/Lairs`, `LairInstance`, every boss's arena).
- Existing output consumer: the player's own inventory and experience. Grave Goods (`GraveGoods`, `Lairs.handBack`) still catch stray drops.
- Technology connection: none needed. It is a rule of the shared framework, so every lair, present and future, gets it.
- Magic connection: none needed, for the same reason.
- Reachable entry path: unchanged. Every lair's own rite or opening is the way in.
- Required vs optional: the rule applies in every lair, with no setting to switch it off.
- Usefulness without other branches: any player who reaches a boss benefits.
- Infrastructure note: this is a framework rule. It adds no items, recipes or progression.

## Balance and automation

- **No duplication:** nothing is dropped, and the respawned player is given copies of the dead player's slots. The dead player is discarded when they respawn, so each item exists exactly once.
- **Experience** works the same way: the dead player drops no orbs, because their experience reward is 0 in a lair. The new player gets the same level, progress and total, so nothing is gained or lost.
- **A cheaper death in a boss fight**: the cost of dying is now:
  - the walk back to the grave or altar;
  - the gate's time limit;
  - losing your place in the fight.
  
  The boss still heals and resets when everyone leaves, and every reward still needs your part in the fight (`Encounter`).

## Multiplayer and persistence

- **Server authority:** everything happens on the server.
  - `Player.dropEquipment` and `Player.getBaseExperienceReward` are cut short (`LairKeepInventoryMixin`) only when the dying player's level is a lair (`Lairs.keepsBelongings`).
  - Fabric's `ServerPlayerEvents.COPY_FROM` copies the slots and experience onto the respawned player (`Lairs.keep`). It runs after vanilla's own copy, with the same fields vanilla copies for keepInventory.
- **Logging out on the death screen:** a player who leaves while dead in a lair, or whose server restarts, comes back still dead where they fell.
  - `Lairs.joined` now leaves dead players alone, as `Lairs.check` already did. They are not sent home while dead, so their respawn still sees that they fell in a lair and keeps everything.
  - Their items are saved with them as with any player.
- **Save compatibility:** no new saved data, IDs or registrations. Grave Goods already held by a player (from before this change) are still handed back on their next respawn.
- **Disable behaviour:** none. The rule needs no configuration.

## Dependencies and assets

This adds no dependencies and no assets. It uses one new mixin, `LairKeepInventoryMixin`, with two cancellable HEAD injections on `Player`. A mixin is needed because neither Fabric API nor the game offers a per-dimension keepInventory hook.

## Verification

| Check | Command | Result | When |
| --- | --- | --- | --- |
| Structure and links | `python scripts/check_repository.py` | passed | locally, before pushing |
| Material data and recipe audit | `python tools/check_mod_data.py` | passed | locally, before pushing |
| Syntax of the changed Java files | the session's parse helper (javac's parser, no Minecraft classes) | passed | locally |
| Compilation, server game tests and rendered-client tests | `./gradlew build` and `./gradlew runClientGameTest` | not run locally: the game only compiles in CI, so CI on this draft PR is the evidence | in CI |

The rendered-client test (`LairClientGameTests`, step 4) covers a real death in the Hollow Acre in survival:
1. Setup: a helmet worn, a shield in the offhand, 9 emeralds in hotbar slot 5 (selected), 5 bread in slot 21, and about 500 experience.
2. The player is killed with `genericKill`. No item entities or experience orbs are left within 8 blocks, there are no Grave Goods, and the visit has ended.
3. The client presses respawn. The respawned player is outside the lair, with:
   - the helmet and shield still worn;
   - the emeralds and bread in their own slots;
   - slot 5 still selected;
   - the same experience level, progress and total;
   - no Grave Goods left over.
4. `Lairs.keepsBelongings` is false outside a lair.
5. The player goes back in through `Lairs.enter`. The old Grave Goods check (3 diamonds and 7 experience dropped by hand) still passes as the safety net.

Not run: a two-client dedicated-server playtest, and play by hand.

## World and event applicability

This applies in every lair dimension and nowhere else. It does not depend on season or event.

## Rollout and open questions

- Known limit: a mod that moves items out of the inventory at death by some route other than `dropEquipment` (a grave mod, say) works as that mod decides.
- Rollout is reversible: removing the mixin and `Lairs.keep` restores the old Grave Goods behaviour, with no saved data to migrate.
