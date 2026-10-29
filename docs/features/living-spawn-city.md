# Living spawn city: daily routines, shop hours and street furniture

Status: local only, on `claude/living-spawn-city` from main `a5803a77e4dadb17559cc416b21ddb495b824b2f`. Nothing has been pushed, merged, released or deployed. This half has not been compiled or run: the cloud machine that wrote it has no Minecraft or Gradle. Combined validation follows integration with OpenAI Codex's half.

Owner: jimbozoomer-byte. Owner-approved collaboration, 28 October 2026.

## Who builds what

- **Claude (this record):**
  - the residents' daily routines, bedtime, work poses and dialogue (`Townsfolk`, `TownRoutine`, client `TownsfolkModel` and `TownsfolkRenderer`);
  - shop hours and the taverns' essentials counter (`TownShops`, `ShopMenu`);
  - the revision-4 blueprint and its generator (`tools/town_living.py` and small hooks in `tools/town.py`, `town_shops.py`, `town_wayfinding.py` and `town_lang.py`);
  - `TownRoutineGameTests`.
- **OpenAI Codex:**
  - the town commission board (block, GUI, deliveries, Jugs rewards, party credit, replay protection);
  - the migration integration, including bumping `TownUpgrades.CURRENT` to 4;
  - the public-interaction audit (`TownProtection`) and `TownDecor.apply`'s preservation fix.

`TownUpgrades.java`, `TownDecor.java` and `TownProtection.java` are untouched here.

## What players see

### A day in the town

The day follows the Overworld's clock (0 is 6 am; 1000 ticks an hour):

| Time | Ticks | What residents do |
| --- | --- | --- |
| Dawn to 7 am | 0–999 | Get up and walk to work. Shops are not open yet. |
| 7 am to 6 pm | 1000–11999 | Work at their place. Shops are open. |
| 6 pm to midnight | 12000–17999 | Gather at a tavern's doorstep or round the fountain. Shops are shut. |
| Midnight to dawn | 18000–23999 | Asleep in their own bed. |

The guards stay on watch all day and all night. The innkeepers keep the taverns open all night.

### Jobs

The ten street residents take five jobs in turn:

- **Gardener:** kneels at a planter.
- **Porter:** carries crates at a market stall's crate stack.
- **Sweeper:** sweeps up and down the main or south street.
- **Lamplighter:** polishes a lamp post by day and does the rounds of the square's lamps in the evening.
- **Busker:** fiddles by the fountain by day and at a tavern in the evening.

The others keep their old places:

- the baker kneads;
- the banker counts;
- the mayor holds forth at the town hall and, in the evening, by the fountain;
- the priest preaches in the Church of Notch, evenings included;
- shopkeepers, vendors and innkeepers serve, and the florist tends their stall;
- decorators decorate by day and join a gathering in the evening.

### Evening gatherings

Residents stand in a ring, facing in, at three places: before each of the two taverns' doors and round the fountain. At the taverns they raise a mug now and then; by the fountain they chat. Everyone has their own spot, so the crowd does not pile up.

### Poses

Each job has its own looping pose while the resident stands at their place. Each person moves on their own beat, so a crowd is not in step. A pose fades out as soon as they walk off. Gardeners kneel using Minecraft's own crouch pose. Held items do not change: the guards' swords, the baker's bread, the priest's book, the decorators' lanterns and the florist's poppy stay as they were.

### Dialogue

Half the time a resident says something for the season (the existing lines). The other half they say something about their day: 93 new lines for their job and the time of day. Everyone shares three dawn greetings, and the watch and the innkeepers have night lines.

### Shop hours

The four shops (General Store, Seasonal Stall, Curiosities, Florist) open from 7 am to 6 pm.

- **The hours are posted.** Their existing signs now read "Open 7am-6pm".
- **Opening the shop** shows the hours as an overlay.
- **A closed shop's keeper** says when it opens and points to the taverns.
- **A screen left open past closing time** refuses further trades.

### Essential-service fallback

At any hour, talking to either innkeeper opens the **Tavern Essentials** counter. It sells the General Store's essentials (torches, bread, cooked beef, a white bed) at the store's own prices. It buys nothing. The tavern signs now read "Open all hours" and "Essentials sold".

### Bedtime without sleepwalking

These rules keep residents in bed at night and out of it by day:

- They sleep only in their own bed and only at night.
- One loaded asleep in daylight (from a save made at night) gets up.
- One asleep in a bed that is no longer theirs gets up.
- Sleepers' legs no longer swing while lying down.
- If a resident gets no nearer their bed for 20 seconds (a blocked door, a walled-in bed), they stop trying. They rest and doze where they are until dawn.
- If their bed is taken, they go to it and wait beside it.
- At night, only the watch strolls about.
- There are still no teleports and no chunk loading.

### Street furniture

Revision 4 adds 58 pieces, all from existing Jugcraft and vanilla blocks:

- produce crates beside and behind the market stalls, some topped with lanterns;
- crates and hay bales outside the General Store and the bakery;
- café tables with a chair each side on the square;
- benches facing the streets against house walls;
- little flower stands (a side table with a potted flower);
- flowering azalea hedges;
- potted flowers.

### Commission board

OpenAI Codex's `jugcraft:town_commission_board[facing=north,part=0]` stands at town position **(104, 1, 112)**. It faces north into the square from its south edge, away from the market stalls. The three-wide, three-high assembly stays outside the church plot and main street.

## How existing towns are kept safe

### Revision 3 is frozen

Revision 3 is frozen byte-for-byte at `data/jugcraft/town/revisions/3.json.gz` (sha256 `7b63d919…6f1b5`). That is exactly the blueprint shipped on main.

### Revision 4 only adds

The new pass runs last in `Town.build()`, after every existing step, from its own random stream (`SEED + 4`). No earlier layout draw, resident index, name, skin, place, bed, sign position, site or ATM changes. No residents are added: the existing 48 spots keep their indices and gain only new keys (`job`, `work`, `work_look`, `evening`, `evening_look`, `gathering`).

### Where a decoration may go

Every added cell:

- was authored air inside the wall;
- is outdoors, on stone paving (or grass, for blocks that let light through);
- is clear of doors, gates, stairs, ladders, decor sites, signs, map boards, ATMs, lamp posts, residents' places and the gatherings;
- is off the **whole Church of Notch plot**, x 78–114 and z 35–87, which covers its interior and the owner's slot machines;
- is off the Styx district's road.

### What a decoration may be

- No decoration has a block entity or an inventory.
- None joins its neighbours: furniture is placed as single, unconnected pieces.
- None is placed beside a fence, pane, bars or wall that would join it.

### How it is applied and checked

TownUpgrades applies a cell only where the live world still shows the old blueprint's air. Anything an owner built there stays, and block entities are protected.

`tools/town.py` checks this whenever it writes the town. The game test `revisionFourOnlyAddsToAuthoredAir` checks it again in game.

### Sign text

The shop and tavern signs change only through the existing sign-text migration. A sign whose text an owner has edited is preserved.

## Shared-file wiring for integration

These are additive. They are kept in their own commit so they can be re-applied by script if they conflict:

- **`assets/jugcraft/lang/en_us.json`:** 98 new keys, written in the generator's own sorted section. No key is removed or changed. A plain list of the keys and their text is in `lang-additions.json`, delivered alongside the patch.
- **`src/gametest/resources/fabric.mod.json`:** one line registers `TownRoutineGameTests`.
- **`docs/TODO.md` and `CHANGELOG`:** suggested entries are in the handoff notes. Neither file is edited here.

The commission board's palette state only resolves once OpenAI Codex's block is registered. On this branch alone it parses to air, so `TownGameTests.townDataReadsWhole`'s `unknownStates() == 0` would fail until the halves are combined.

## Validation

### Done here

- `python3 tools/town.py` regenerates the blueprint and reports no problems, including the additivity check against the frozen revision 3.
- An independent decode of both blueprints found:
  - 58 changed cells, all of them air before, none on the church plot or outside the wall;
  - 48 of 48 residents with identical role, name, skin, place, shop and bed;
  - identical sites, ATMs and sign positions.
- `scripts/check_repository.py` passes.
- `tools/check_mod_data.py`: see the handoff notes for the exact result.
- Every changed Java file parses with a Java syntax checker.

### Not done

Compilation, the game tests and the client rendering have not been run. Pose angles were chosen from the model's conventions and not seen in game.

### Game tests to run after integration (`TownRoutineGameTests`)

- `theTownKeepsItsDay`: period boundaries, hour labels, jobs to activities, reach, and every dialogue key present in the shipped language file.
- `revisionFourOnlyAddsToAuthoredAir`:
  - every changed cell was air, is inside the wall, is off the church plot and has no block entity;
  - the board stands at (104, 1, 112);
  - identities, beds, sites, ATMs and signs are preserved;
  - every resident's places are valid.
- `residentsWorkByDayGatherInTheEveningAndStayPutAtNight`: a baker walks to work and kneads (keeping the bread), gathers and chats at 6 pm, and at night rests without wandering for 100 ticks.
- `aResidentKeptFromTheirBedRestsInsteadOfSleepwalking`: a walled-in bed is never slept in, and the resident gives up and stays put.
- `sleepersOutOfTurnGetUp`: daylight sleepers and sleepers in someone else's bed get up.
- `shopsKeepTheirHoursAndTavernsSellEssentialsAllNight`:
  - hours for every shop;
  - a sale by day and none at 7 pm with the screen still open;
  - a closed keeper opens no shop;
  - the tavern lists exactly the essentials, sells bread at 2 am (tick 20000) and buys nothing.

The existing `TownLifeGameTests.civiliansWalkToBedLateAndWakeWhileGuardsStayUp` covers the unchanged midnight bed walk.

## Known limits

- **Decorators decorate only by day.** A season or event that changes in the evening is decorated the next morning.
- **Innkeepers no longer use their beds.** The beds stay in the blueprint unused.
- **A taken bed.** A resident whose bed is occupied by a player waits beside it rather than finding another.
- **Positions are fixed in the blueprint.** Gathering and work places come from revision 4. A place an owner has since built over is still walked to: the resident stands as near as they can.

Integration follow-up: the final combined blueprint has 63 additive cells; the original 58-cell cloud audit above is historical. See [combined local results](living-city-local.md) for the owner-requested noticeboard relocation and revised economy.
