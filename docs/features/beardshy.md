# Beardshy

Status: the NPC is implemented in source, not yet played; it builds and passes its tests in CI (see Verification). The Harvest Feast story below is **proposed, not built**: it waits for the owner's approval.
Proposal issue: none. The contributor asked on 1 November 2026 for a Beardshy NPC "like the Beardson villager but shy and submissive", made the enemy of the Thanksgiving event who wants the holiday cancelled for being racist against the First Peoples, and for just the NPC and the story for now. Beardshy is one of Beardson Beardly's personas in the owner's Jimboverse (its wiki's Beardshy page); Beardson is the [Retro Trader](retro-trader.md), the bearded millennial who runs the Retro Game Shop.
Owner: @jimbozoomer-byte
Target milestone and tier: none for the NPC; the story would be a seasonal event open from Discovery tier.
Primary specialty and supported player role: seasonal events and village life.

## Player experience

### The NPC (built)
**Beardshy** has the villager's body like Beardson, in his own look: curly dark hair, a ginger beard, freckles and cheeks that are always a little red, round tortoiseshell glasses with his eyes cast down, his pink hoodie (drawstrings, kangaroo pocket, the hood bunched behind his neck), black headphones round his neck, "uwu" inked on the backs of his hands, dark jeans and grey sneakers.

He is shy:
- **Walk up to him and he startles**: within 5 blocks of a standing player he hurries away. Spoken to while you're standing, he gives a startled apology ("O-oh! S-sorry, I didn't see you there... sorry.", "C-could you maybe crouch? Sorry. You're just very... tall.").
- **Crouch and he stays.** A crouching player can come right up to him; with anyone near he bows his head and looks at the floor.
- **Talk to him** (use him) and he says something soft and hesitant: one of 18 lines, never the same twice running, at most one every 1.5 seconds. He worries that we're standing on stolen land ("Before you mine that, should we, um, acknowledge whose land it is?"), insists the big mountain is called Denali, explains simple things a little too kindly ("So, um, a villager is a person who lives in a village..."), uses internet sayings and slang not quite right ("Hold my non-alcoholic beer.", "Your base is so slay. Is slay a good word?"), and apologises for how long his sentences get.
- **Hit him** and he apologises to you, as if it were his fault ("S-sorry! Was I in the way?"), shakes his head and runs. He never fights back.

He trades nothing, drops nothing, takes nothing and never despawns. He doesn't spawn on his own yet: the story would bring him. Until then, his **spawn egg** (in the Spawn Eggs tab: a pink egg with ginger spots, his hoodie and his beard) or `/summon jugcraft:beardshy`.

He is built from the wiki's personality: shy, apologetic, guilty about stolen land, Denali, Reddit sayings and over-explaining. The wiki's roleplay material, Jaylin and the grift streams are not in the game.

### The Harvest Feast story (proposed, not built)
**"The Feast That Almost Wasn't."** During the [Harvest Feast](seasons.md) (Thanksgiving: the fourth Thursday of November over the weekend by default, or Canada's date), Beardson closes the Retro Game Shop and Beardshy comes out. He has decided Thanksgiving is racist against the First Peoples and should be cancelled. He's far too shy to say it to anyone's face, so he fights the feast quietly, apologetically and badly.

1. **A strongly worded note.** On the first day of the feast, in a village near a player, the Retro Game Shop's sign is turned to "Closed" and a note hangs on its door: "Dear whoever is in charge of Thanksgiving, sorry to bother you, I just think we should cancel it, sorry..." Hand-lettered signs go up around the village square: "Cancel the Feast", "Respect Indigenous Names", "Denali Forever". Villagers shrug at them.
2. **Quiet sabotage.** While no player is looking, he hides the village's feast things: he shoos the turkeys out of their pen and tucks the pies and cranberries into barrels round the village, each with an apology ("Sorry. I'll put it back. I won't put it back. Sorry."). He only touches what the event puts out, never a player's chest, crops or build. You find him by sneaking: he flees a standing player, so the hunt is crouch-walking after a man in a pink hoodie. Each time you corner him without startling him, he hands one thing back and gives a short speech about stolen land.
3. **The land acknowledgement.** When a player sets the [Harvest Feast Table](fall-additions.md#the-harvest-feast-table), he turns up at its end and starts a land acknowledgement that keeps getting longer, apologising for its length as it goes. Nobody can eat at the table until he finishes or someone gently talks him down (crouch and talk to him).
4. **The confrontation: no fight.** The boss is a conversation. Cornered while crouching, he lays out his objections one at a time and the player answers from three choices (clickable lines in chat). Kind answers ("Thank the land and the people who were here first", "Ask him to sit down", "Say there's pie") wear his objections down. "Call the mountain McKinley" makes him cry and run off, and he has to be found again. Hitting him does the same and costs the feast a dish.
5. **A seat at the table.** Out of objections, he admits he just didn't want anyone to feel left out. Invited to the table, he gives the longest, teariest grace in Minecraft, land acknowledgement included, eats a slice of pie and has a lovely time. He counts as a guest, so his company helps the table towards a grand feast.

**Rewards (to choose):** a keepsake of his apology note; a pink hoodie to wear (a cosmetic chest piece); a Denali postcard painting; an advancement, "Thankful (Sorry)". Earned items stay after the event. Nothing here gates any progression, and the whole thing is optional fun around the Harvest Feast Table.

**After the feast** he goes back to being Beardson at the Retro Game Shop until next year, and the signs come down. Next year he's back, a little braver.

**Tone.** The joke is Beardshy: his nerves, his apologies and his habit of turning everything into a speech. It is never Indigenous peoples or their history. The First Peoples aren't characters in it and he never speaks for them; there are no Native costumes or invented "Native" items.

## Connections
- Existing input producer: none for the NPC. The story would use the Harvest Feast event (the season clock), villages, the Retro Game Shop, turkeys and the Harvest Feast Table.
- Existing output consumer: none for the NPC. The story would feed the Harvest Feast Table (a guest and the returned dishes) and give keepsakes.
- Technology connection: none.
- Magic connection: none.
- Reachable entry path: nothing to unlock. The NPC is summoned; the story would need only a village during the feast.
- Required vs optional; trade and solo routes: all optional, and the story can be played solo.
- How this specialty stays useful without mastering every other branch: it is a character and a seasonal story, not a resource.
- For infrastructure/cosmetics: an NPC and an event, so resource links do not apply.

## Balance and automation
No resources, trades or drops; nothing to automate or loop. Talking to him is rate-limited (1.5 seconds between lines, server side).

## Multiplayer and persistence
- Server authority: his moving away, his lines and his apologies are all decided on the server; the client only bows his head (with a player within 5 blocks) and shakes it (when hurt). His lines are sent to the player who spoke to him or hit him, through the town's speech format ("Beardshy: ...").
- Persistence: he never despawns (persistence required) and saves as an ordinary mob. No new saved data.
- **Stable IDs:** `jugcraft:beardshy` (entity type) and `jugcraft:beardshy_spawn_egg` (item, priced 1 like the mod's other spawn eggs); the language keys `entity.jugcraft.beardshy`, `item.jugcraft.beardshy_spawn_egg` and `message.jugcraft.beardshy.talk.0`-`17`, `startled.0`-`3` and `hurt.0`-`2`.
- Disable behaviour: there is no switch, because nothing spawns him. His entity type is always registered, so a saved Beardshy always loads.

## Dependencies and assets
- No new dependencies. He is a pathfinding mob on the game's own villager model (`BeardshyRenderer`, the villager model with his whole skin; no mixin).
- **Original assets:** his 64x64 skin and his spawn egg's 16x16 icon (vanilla's egg form drawn fresh, as docs/ITEM_ICONS.md asks) are drawn by `tools/beardshy_look.py`; the skin is on the villager model's UV layout, with the Retro Trader's drawing helpers; nothing is read, traced or recoloured from Mojang's textures. His lines are in `tools/beardshy.py`, which `tools/generate_material_data.py` writes to the language file. His voice is the villager's own sounds by name, higher and softer. MIT.

## Verification
- **Server game tests** (`BeardshyGameTests`): a standing player startles him and a crouching one does not; he speaks to a crouching player, not again before his cooldown, and again after it; he isn't a merchant and he can't despawn; hit by a player, he takes the blow and doesn't fight back.
- **Client game test** (`BeardshyClientGameTests`): screenshots of him from further off than he startles (head up), from near (head bowed), his face, three-quarters, his back, and beside the Retro Trader.
- **Unit tests** (`scripts/tests/test_beardshy.py`): the line counts in `Beardshy.java` match `tools/beardshy.py`; the language file has his name and every line; the skin and the spawn egg's icon on disk are what `tools/beardshy_look.py` draws, and the egg's item uses its icon and has a price record; and every face of every model part he shows is painted (his skin is drawn alone, so a clear pixel would be a hole).
- Local results are in the pull request. The Java was checked only for syntax locally; it was not compiled.
- CI on the fork, [Build run 41364594278](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41364594278) on 91284a2098: the mod compiled. `BeardshyGameTests` caught a real bug: his "last spoke" time started at `Long.MIN_VALUE`, so the cooldown check overflowed and he never spoke; it now starts a cooldown back (fixed in the next commit). `BeardshyClientGameTests` passed (client shard 13): he renders on the villager model in his skin, named Beardshy, and bows his head with the camera near; the next commit fixes two camera angles (the back shot faced away; the face shot now crouches). The run's other failures are not Beardshy's: the main job stopped at "Check generated JSON is up to date" because the generator crashes on current upstream main (a stored walls-tag repair in `tools/data/owner-resource-patches.json` expects the 84-entry list that upstream's NEWEST integration grew), as it does on a clean upstream checkout here; other client shards failed on network tests timing out, upstream's NEWEST models with unstitched textures, a weapon-arts distance check and running out of memory, and the server job without the optional integrations ran out of memory after Beardshy's tests had run.
- CI on the fork, [Build run 41370166774](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41370166774) on c6b92306cd: he speaks now (the talking test got past both of its speaking checks), but the test then ran past its 20-tick limit, because it waits his 30-tick cooldown before speaking again: it now has 60 ticks (next commit). `BeardshyClientGameTests` passed again (shard 13): his face from a crouch under his bowed head, his back, and beside Beardson. The other failures were the same upstream ones as before.
- CI on the fork, [Build run 41374803650](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41374803650) on a87189e286: all three `BeardshyGameTests` passed (the server job's log, which covers the time they run, shows no Beardshy failure and no failed test before the job ran out of memory, as on main).
- CI on the fork, [Build run 41419177078](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41419177078) on d3620afc21 (the spawn egg): the mod compiled with the egg and the game started with it; `BeardshyClientGameTests` passed (shard 13, all six screenshots). The server job without the optional integrations had passed 2,350 tests with none failed when it ran out of memory, about 920 short of the end, so this run doesn't show Beardshy's three server tests (they passed in the run above, and the egg doesn't touch them). The main job stopped at the upstream generator check again, and none of the failed client shards' logs mention Beardshy.

## World and event applicability
The NPC can stand anywhere. The story belongs to the Harvest Feast window and to villages, and is preserved and deactivated as the season clock's other events are: it starts and ends with the event, and earned items stay.

## Rollout and open questions
- For the owner: approve the story, or change it, before any of it is built.
- Should he only hide the feast things the event puts out (recommended: no griefing), or anything on a Harvest Feast Table?
- Which rewards: the note, the hoodie, the postcard, the advancement?
- Should Beardgod or other Jimboverse personas appear (Beardgod bullying him, say, or Beardson closing up the shop)?
- Canada's Thanksgiving date as well as the US one? The season clock already supports both.
- Removing him later only needs his entity type kept registered or migrated.
