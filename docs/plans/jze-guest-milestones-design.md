# JZE guest milestones, ceremonies and notices: design (worker D, November 11)

Assignment 4 of [jze-guest-expansion-nov11.md](jze-guest-expansion-nov11.md). Local/review-only work on
`claude/nov11-jze-d-milestones`, built on the coordinator's seams (`claude/nov11-jze-api` at `dc4469ab72`). Nothing here
is compiled or run in the cloud worktree: Codex compiles and runs the tests locally.

## Rules this design keeps

- Nonsexual for everyone. Real-person guests get generic, game-world material only: no letter is signed by a real
  person, no line is put in their mouth, and gossip about them only mentions town mechanics.
- **Romance is for unambiguous adults only (owner clarification, 11 November).** Romantic material (the church ceremony,
  its bouquet, anniversary letters) needs both `GuestRoster.Guest.mayMarry()` and an actual marriage of the two in
  `GuestLedger`. Held guests (`mayMarry()` false: asa_mitaka, tokai_teio, agnes_tachyon, gold_ship, oguri_cap,
  special_week, rei_ayanami, asuka_langley, rem, megumin, rin_ishtar, power, reze, zero_two) get the friendship set
  only. There is no "adult alternate" path and no ceremony path for them.
- No eligibility is kept or changed here: the ledger alone says who is married. Nothing here calls `GuestLedger.marry`,
  `divorce`, `addAffection` or `setAffection`, or touches the roster.
- All affection goes through `GuestRewards.grant` (the shared per player and guest daily budget). Nothing pays money or
  gives a valuable item.
- Nothing sets a block, loads a chunk, opens storage, moves or removes an owner object (the Church of Notch's slot
  machines included) or clones an NPC. Every block or entity read is in an already loaded chunk.

## Classes (all in `town/guests`, worker D's prefixes)

| Class | What it does |
| --- | --- |
| `GuestMilestones` | `register()`: event listeners, the server tick, commands, and the `milestones` GuestFeature. Milestone rules and delivery. |
| `GuestMilestoneState` | Saved data `jugcraft:town_guest_milestones` (overworld): per player, the milestones reached, rewards waiting, meal days, the marriage record (serial, start day, ceremony held), notice settings. Versioned; an unreadable player entry is kept as written and freezes that player's milestones, meals and ceremonies. |
| `GuestMilestoneMeals` | The once-a-day shared meal. |
| `GuestKeepsakes` | Letters (written books) and keepsakes (named vanilla items with lore), the friendship and romance sets, give-or-drop delivery. |
| `GuestCeremonies` | The optional church ceremony: request, confirm, cancel, timeout, the bounded rite, invitations. |
| `GuestCeremonyVenue` | Finds the loaded Church of Notch and its loaded Priest of Notch without loading anything. |
| `GuestNotices` | The opt-in notice settings, fictional gossip, rivalry notices, opted-in announcements. |
| `GuestMilestoneTesting` (gametest) | Test seams. |
| `GuestMilestoneGameTests` (gametest) | Server tests. |

## Milestones

Per player and guest, each fires once, is recorded in the saved data, and is rewarded once:

| Milestone key | When | Reward (set) |
| --- | --- | --- |
| `first_gift` | `GuestEvents.GIFT_GIVEN` | thank-you letter (friendship) |
| `first_quest` | `GuestEvents.QUEST_TURNED_IN` | errand letter (friendship) |
| `first_outing` | an `outing` activity in `GuestActivity` ends (polled once a second; worker C's classes are never called) | "Outing Sketch" paper note (friendship) |
| `affection_250` | affection reaches 250 | pressed flower (friendship) |
| `affection_500` | 500 | letter (friendship) |
| `affection_750` | 750 | dyed leather keepsake cap (friendship) |
| `affection_1000` | 1000 | letter (friendship) |
| `ceremony_<n>` | the church ceremony of marriage `n` completes | "Ceremony Bouquet" (romance; mayMarry and married only) |
| `anniversary_<n>_<days>` | marriage `n` is 7, 30, 100, 365, 730, ... game days old | anniversary letter (romance; mayMarry and married only) |

- Affection is read from the ledger after a gift, quest, talk (`TALKED`), meal, and every 5 seconds for online players,
  so affection added by other workers' rewards (without events) is noticed too. Thresholds already passed when the
  feature arrives fire once, each.
- A milestone reached while the player is offline (an outing ended by logout) waits in the saved data and is handed
  over when they are next online.
- **Letters**: a written book (title at most 32 characters). Fictional guests sign their own letters with generic,
  original, friendly text; for real-person guests the letter comes from "The Town Post" and only notes game-world
  events (no signature, quote or claim attributed to the person).
- **Keepsakes**: a pressed flower (poppy, cornflower, blue orchid, allium, azure bluet or oxeye daisy, by a fixed hash),
  a dyed leather cap, a paper sketch-map note, and the ceremony's lily-of-the-valley bouquet. Each has a custom name,
  lore and a `jugcraft_keepsake` custom-data marker, so it is never a plain stack: guests do not take it as a gift
  (favors accept plain stacks only) and the town shops buy none of these items. Delivery uses
  `Inventory.placeItemBackInInventory`, which drops whatever does not fit at the player's feet: nothing is lost.
- **Marriage record.** `MARRIED` starts a new marriage record for the player (a serial number per player, the start
  game day, ceremony not held); `DIVORCED` clears it. Each check reconciles with the ledger: no ledger spouse clears the
  record (an operator's forced divorce has no event); a ledger marriage without a record (made before this feature) is
  adopted with today as its start. Anniversaries count from that start; romantic milestones apply only while
  `mayMarry()` is true and the ledger marriage is current.

## Shared meal

`GuestFeature` option `milestones/meal`. The player holds a plain modest food in the main hand (bread, baked potato,
cooked cod, salmon, chicken, porkchop, mutton, rabbit or beef, pumpkin pie, cookie or apple; no bowl foods, nothing
golden). Once per player and guest per game day (the day `GuestFavors` uses, never earlier than the last meal's day,
so a clock moved backwards gives no second meal). As `GuestRewards` requires, the meal is offered only while a gain is
possible (`GuestRewards.left > 0` and `GuestLedger.canGainAffection`), so no food is taken for nothing. Exactly one item
leaves the hand stack, the day is recorded, then `GuestRewards.grant(player, guest, 8)` adds at most 8 within the shared
budget. The text is friendly for every guest.

## Church ceremony (optional, cosmetic, adults only)

Offered only when the guest's `mayMarry()` is true and the ledger says this player and this guest are married to each
other, once per marriage. It never performs a marriage transaction: the ledger's spouses, cooldowns and proposal rules
are read only.

1. **Request** (`milestones/ceremony`): every entry check (player alive, not a spectator, same level, within
   `GuestBalance.INTERACT_RANGE`, the canonical entity, `npc.guest()` is the guest, the guest available), then eligibility
   (mayMarry, ledger marriage both ways, not yet held for this marriage, no activity holding the guest, no other ceremony
   of the player or guest), then the venue. Kept in memory for `GuestBalance.CONFIRM_TICKS` (30 s).
2. **Confirm** (`milestones/ceremony_confirm`): every check again, then `GuestActivity.begin(guest, "ceremony")` with
   the venue's anchor. The guest walks there with its usual goals.
3. **Walk** (up to 2 minutes): the guest within 6.5 blocks of the anchor and the player within 16 starts the rite; otherwise
   the ceremony ends ("timeout").
4. **Rite** (5 lines, 3 s apart): the Priest of Notch's original lines, bell and chime sounds, happy-villager, note and
   end-rod particles. Then the ceremony is recorded for this marriage and the bouquet given.
5. **End**: completion, cancel (`milestones/ceremony_cancel` or `/guests ceremony cancel`), a lapsed request, the
   walk timeout, the player leaving (more than 48 blocks, another dimension, logout, death), the guest, priest or church
   unloading, a divorce, the activity taken by anything else, or the server stopping. Every end calls
   `GuestActivity.end(guest, "ceremony")`; after a restart, a ceremony activity with no ceremony in memory is ended.
   Only completion records the ceremony.

**Venue.** The spawn town's origin (`TownState`) plus the Church of Notch plot from `tools/town_living.py` (`CHURCH`,
town x 78..114, z 35..87), from the ground to 48 above. Every chunk under the plot must already be loaded and the
anchor entity-ticking, and a loaded `Townsfolk` with role `priest` (not a custom NPC or guest) must stand in the plot.
The anchor is a standable cell 2 to 3 blocks from the priest, read only from loaded chunks. Otherwise the request is
refused with a clear message (no town, the church not loaded, no priest). Nothing loads a chunk.

**Invitations** are voluntary: `/guests ceremony invite <players>` (at most 8) sends each named online player a chat
invitation; if the couple's player has opted in to sharing, opted-in players within 48 blocks of the church are
invited too. The priest's lines go to the couple and to invited players within 48 blocks. Nobody else is told.

## Notices and gossip (opt-in, default off)

Two per-player settings in the saved data, both off by default:

- `notices` (`milestones/notices_toggle`, `/guests notices on|off`): receive fictional town gossip and rivalry notices.
- `share` (`milestones/notices_share`, `/guests notices share on|off`): let my milestones and ceremonies be mentioned
  to players who receive notices.

Gossip comes about every 10 minutes per receiving player, about a placed, available guest, from generic templates:
real-person guests only get lines about town mechanics (errands, visitors, gifts); fictional guests may also get light
original town-flavoured lines. Rivalry notices follow a new `rivalry` activity (at most once per guest per 5 minutes)
and name only the guest. A milestone or ceremony announcement names a player only when that player has `share` on, and
goes only to players with `notices` on. No relationship is ever broadcast to everyone.

## Numbers (first choices for owner review)

| What | Value |
| --- | --- |
| Affection thresholds | 250, 500, 750, 1000 |
| Meal grant / limit | 8 affection within the shared budget / once per player and guest per game day |
| Anniversaries | 7, 30, 100, 365 game days, then every 365 |
| Ceremony | confirm 30 s, walk 2 min, rite 5 lines x 3 s, hard cap 3 min, arrive 6.5 blocks, start within 16, leave beyond 48, 8 named invitations, nearby invitations 48 blocks |
| Gossip / rivalry notices | every 10 min per receiver / once per guest per 5 min |
| Notice settings | both off by default |

## Commands

`/guests notices on|off`, `/guests notices share on|off`, `/guests ceremony invite <players>`,
`/guests ceremony cancel`. Integration rule (coordinator, 11 November): the `/guests` root is registered with no
requirement, because Brigadier keeps the requirement of whichever root registration comes first; each subcommand node
carries its own requirement (here: the source is a player), checked again when it runs. The menu options work without
the commands.

## Tests (`GuestMilestoneGameTests`, synthetic `test_d_*` guests)

Milestones once and across a save and load; keepsakes non-valuable and delivered once with a full inventory; the meal
once a day within the shared budget; the ceremony refused when unmarried, for a mayMarry-false guest at full
affection, when the church or priest is unloaded, never touching the ledger's spouses and cooldowns; request, confirm,
cancel, timeout, once per marriage, a new ceremony only after divorce and a new marriage; no block changes in the
church box; notices off by default, opt-in only, never naming a non-opted-in player; romantic letters refused and only
friendship letters for held guests; entry-point checks.

## Limitations and deferred items

- Held guests (14 entries, see above) get friendship milestones only, by the owner's rule; this is intended, not a gap.
- A marriage to a now-held guest that already exists in a saved ledger stays in the ledger untouched; this feature
  treats it as friendship (no ceremony, no anniversaries).
- An outing that starts and ends within one second is not observed as an outing.
- The ceremony walk uses the guest's ordinary goals; a guest that cannot path into the church times out cleanly. Its
  goals may open and close an ordinary wooden door as every resident does; this feature sets no block.
