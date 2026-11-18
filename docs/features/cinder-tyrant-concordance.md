# The Cinder Tyrant's heat and cooling package

Status: implemented in source, not merged. The Python audits and unit tests pass locally (below); the Java compiles and the game tests run only in CI, whose results are recorded in [Verification](#verification) once they come in. It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. The owner chose it on 28 October 2026 ("Cinder Tyrant package"), from Concordance Chunk E's E2 and E3 in the [delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md#7-chunk-e--expedition-season): give the existing Cinder Tyrant a controlled heat and cooling school package (a few operations that help in his kiln, one everyday use, one workshop use) and ordinary preparation routes, so the magic stays optional.
Owner: @jimbozoomer-byte
Target milestone and tier: Specialization tier, as [the Cinder Tyrant](cinder-tyrant.md) is. Quench is Practitioner research (Rimekeeping understood); the Quench Flask needs only a glass bottle and snowballs.
Primary specialty and supported player role: adventuring, with the Rimekeepers (cooling), Stratawrights (bracing) and Hearthbinders (heat) supporting a party in his kiln, and the workshop's alembists using the heat at home.

## Player experience

**Ordinary preparation, no magic.** A **Quench Flask** is a glass bottle shaken with three snowballs (shapeless, stacks to 16). Thrown, it bursts in a splash 2.5 blocks round:
- everyone it reaches is put out and **Cooled** for 10 seconds;
- a creature born of fire (`#jugcraft:quenchable`: a blaze, a magma cube, his Cinderlings) is struck for 6 instead, and a Cinderling gutters out as it would in a flooded trough (its thrower has taken part in his fight);
- fire on the ground goes out, and so do his fire patches.

It never quenches him: only his sluices' water does.

**Cooled** (`jugcraft:cooled`) is a heat buffer, not Fire Resistance. It puts the creature out the moment it takes, and while it lasts its burning is put out each tick, and hot ground (a magma block, the Cinder Kiln's slag) does not hurt it. Fire itself, lava and a creature's fire (his breath, his spit, his wave) still do. Milk ends it.

**The Concordance's answers to him** (all elective):
- **Quench** (Rime, support; taught with Rimebind and Preserve when Rimekeeping is understood; 3 Focus, 2 mastered). Touch a creature: it is put out and Cooled for 5 seconds. *The everyday use:* quench a friend standing in fire, or yourself before crossing a magma floor.
- **Rimebind on him while he is quenched** keeps his seams dark 2 seconds longer, once a quench. On him hot, the frost hisses off and does nothing. The binding that lengthens a window is its caster's deed in his fight.
- **Brace** (Strata) takes half of his Tail Sweep's and Body Slam's push off a braced player, as it does against the Yeti King's. A Brace put on another player that meets his push is its caster's deed; one's own credits no one.
- **Hearthguard** (Ember, already) gives Fire Resistance, which stops his fire.

**The workshop use.** Hearthspark on an **Alembic Crucible** tempers it: for 60 seconds its heat climbs toward hot (the Crucible Stand's hot target) and holds there, whatever is beneath. A hotter source beneath still has its way, and Preserve's hold still keeps the band it holds. A recast restarts the minute, never adds to it. A tempered crucible counts as one kind of hearth kindled for Hearthbinding's mastery. *Use:* a cold crucible brewed hot for a minute without carrying fuel to it.

**His objective (B-P4).** Whoever opens a sluice is remembered with its trough while it floods. When he is quenched in that trough, its turner has done a deed of his fight, one for each quench. A deed alone qualifies a player for a share, as in the other lairs that have objectives.

**Where it is told.** The Cinder Kiln's dossier, while its rite is shown, now names:
- the Quench Flask first (always, with or without the Concordance), with its seconds;
- the Concordance's answers to him (only while the Concordance is on), with Rime's seconds;
- the Kiln Draught and the Salamander Charm, as before.

His credit line names the three deeds, and JEI's pages for his loot now say "a blow, healing, a ward or an objective". The codex has a Quench entry in Rimekeeping's winter category, a page on Rimebound about him, a Tempering page in Ember's Smoulder entry and Quench's invocation entry.

## Connections

- **Existing input producer:**
  - the flask: vanilla glass bottles and snowballs;
  - Quench: Rimekeeping (Concordance Chunk B, unit 3);
  - tempering: Hearthspark (Ember) and the Alembic Crucible;
  - Brace: Stratawrighting;
  - the deeds: his existing sluices.
- **Existing output consumer:** his fight (the slag, his fire patches, his Cinderlings, his quench window, his push), the lair credit (`lair/Encounter`), the Alembic Arts' crucible heat bands, and Hearthbinding's mastery practice.
- **Technology connection:** none required; the flask is a vanilla-material craft.
- **Magic connection:** Rime (Quench, Rimebind's window), Strata (Brace), Ember (Hearthspark's tempering, Hearthguard).
- **Reachable entry path:** a Quench Flask needs a glass bottle and snow; Quench needs Rimekeeping understood (First Light, then Rimekeeping's own entry path); tempering needs Hearthbinding understood. None of them needs anything from him, so nothing here is a circular unlock.
- **Required vs optional:** all optional. He is beaten as before with a weapon and his sluices; every answer here only eases or speeds the fight.
- **Trade and solo routes:** a solo player can make flasks, learn all three schools alone and turn their own sluices (a solo turner's deed changes nothing, as they already strike him). Flasks can be traded.
- **Specialty stays useful alone:** an adventurer without magic has the flask; a Rimekeeper without a weapon earns a share by binding him quenched; a Stratawright, by bracing friends; a party member who only works the sluices, by quenching him.

## Balance and automation

- **The flask:** 4 essence (a glass bottle and three snowballs; `alchemy/values.json`). It is consumed when thrown. It makes nothing and turns into nothing, so it is no conversion loop.
- **Cooled:** 10 seconds from a flask, 5 from Quench, timed (no stacking: a recast restarts it). It spares only hot-floor and burning damage, so his flame stays dangerous and Fire Resistance (Hearthguard, the Kiln Draught, a potion) keeps its place.
- **Rime's window:** 40 ticks, once each time he is quenched. His quench is 160 ticks, so a binding lengthens it by a quarter. Reset with his fight.
- **Brace:** the existing Brace (half), spent on his push for the rest of its tick.
- **The flask on fire-born creatures:** 6, once a throw; never on him.
- **Tempering:** up to 60 seconds of heat at most 120 (hot) for one cast's Focus. It cannot pass hot, so searing still needs a real source, and it is cut at a hold's cap (`EffectSpec.MAX_DURATION`). It makes no item.
- **Units:** ticks (20 a second), blocks, half hearts. Every number is in `tools/cinder_tyrant.py` (`RIME_LENGTHEN`, `QUENCH_FLASK`, `QUENCHABLE`), `tools/concordance_rime.py` (`COOLED_TICKS`) and `tools/concordance_ember.py` (`TEMPER_TICKS`), and `check_tyrant_concordance` holds the Java to them.
- **No automation:** only a player's turn of a gate tells him of a flood, and only a player's cast or throw does the rest.

## Multiplayer and persistence

- **Server authority:** the server decides every splash, status, lengthening, push and deed. Clients only draw the thrown flask (`ThrownItemRenderer`), the particles and the status icon.
- **Validation:**
  - Quench and Rimebind go through the Concordance's effect boundary (authority, tolerance, ledger); his tolerance is resistant, so a Rimebind on him lasts half as long, but it takes.
  - A deed goes only to an eligible player (alive, not a spectator or invulnerable) within his fight's reach.
  - The flask's fire-born blow is its thrower's damage.
- **What is saved:**
  - A crucible's tempering is saved with it (`tempered_until`, written only while tempered), so an older build ignores it.
  - Cooled is saved with the creature, as any status is, and is registered whatever the Concordance switch, so its id stays.
  - His floods, quench count and lengthened window are fight state in memory only, cleared when his fight resets.
- **Save compatibility:**
  - New ids: `jugcraft:quench_flask` (item and entity), `jugcraft:cooled`, `jugcraft:quench`, `jugcraft:quenching`, `#jugcraft:quenchable`.
  - Nothing is renamed or removed.
  - A player who already understands Rimekeeping learns Quench when their knowledge is relearned, as any newly taught invocation is.
- **Chunks:** nothing is force-loaded; the splash and the gate listener look only near the place and the trough.

## Dependencies and assets

No new dependency. The owner's library supplies:
- Quench's spell icon: `ars_jimbaux/textures/item/conjure_water.png`;
- Cooled's status icon: `ars_jymbaumental/textures/mob_effect/mist.png`.

`tools/concordance_rime.py OWNER_FILES` copies both, and records them, under the owner's authorization ([MAGIC_ASSETS.md](../../art/owner-library/MAGIC_ASSETS.md)). The Quench Flask's icon is a new 16×16 map, `tools/item_icons/quench_flask.txt` (dew water, glass, a cork), drawn by `tools/item_icons.py`. Sounds and particles are vanilla's and the Concordance's existing events. No Mojang texture is read, traced or copied.

## Verification

**Local, on this branch (Python only; the Java does not compile here):**
- The package's own audit, `check_tyrant_concordance`, passes. Five deliberate breaks are each caught:
  - Cooled sparing fire itself;
  - the sluice deed removed;
  - a quench no longer clearing Rime's window;
  - the flask's Cooled time off its table;
  - Brace dropped from his push.
- These also pass: `check_cinder_tyrant`, `check_concordance` (the invocation count, Rimekeeping's teachings, codex positions, signs), `check_acquisition`, `check_encyclopedia`, `check_assets`, `check_recipes`, `check_loot`, `check_tags` and `check_item_icons`.
- `tools/check_icon_maps.py` passes on the flask's map.
- The full `python3 tools/check_mod_data.py` passes (8026 material IDs, data files and recipe audit); `python3 scripts/check_repository.py` passes; `python3 -m unittest discover -s scripts/tests` runs 287 tests, all OK; `python3 tools/check_icon_maps.py` passes on all 254 maps.

**Game tests (CI), `CinderTyrantConcordanceGameTests`:**
- `whoeverTurnsTheSluiceThatQuenchesHimHasADeed`: two players open real sluices, he is quenched in one, only its turner has the deed, a second quench is a second deed, and the reset forgets the floods;
- `aRimebindHoldsHisSeamsDarkOnceAQuench`;
- `aBraceHalvesHisPushAndAFriendsIsADeed`;
- `aQuenchFlaskPutsOutHisFirePatches`;
- `aQuenchFlaskCoolsPutsOutAndStrikesTheFireBorn` (with the recipe);
- `quenchCoolsButIsNoFireResistance`;
- `hearthsparkTempersACrucible` (climbing, practice, save, end).

Updated tests:
- `EncyclopediaGameTests.theCinderKilnsRiteNamesTheKilnDraught` checks the flask and Concordance facts, with the Concordance on and off.
- `ConcordanceRimeStrataGameTests` checks Rimekeeping teaching Quench, and relearning.
- `ConcordanceRimeStrataClientGameTests` checks Quench's and Cooled's icons.

Results:
- **fc4ea2abd:** 3042 of 3043 game tests passed. The one failure was `quenchCoolsButIsNoFireResistance` ("Quench did not put out and cool its friend"). Quench's Cooled took, but the friend's burning went out only on Cooled's next tick.
- **The fix:** `Cooling.took`, called from the effect boundary as Rimebind's and Brace's hooks are, now puts the creature out at once. `check_tyrant_concordance` holds the hook, and catches it removed.
- **After the fix:** the run on the next commit is pending.

**Not done:** a human playthrough, and the two-client dedicated-server playtest.

## World and event applicability

The flask works anywhere; Cooled helps on any magma floor, in the Nether and in the volcanic lands. The deeds and Rime's window exist only in his fight. No seasonal gate. Boss containment is unchanged.

## Rollout and open questions

- **Open for the owner:**
  - whether Cooled should also soften fire itself (it does not, so Fire Resistance keeps its place);
  - whether a lone sluice turner should qualify for a share (it does, like the other lairs' objectives);
  - whether tempering should count as a hearth kind for Hearthbinding's mastery (it does).
- **Tide priming** (the planning pack's Tide link) waits for Tide research.
- **Known limits:**
  - Spell Engine's bar keys only 9 invocations, and Quench makes 14 (see [the invocations record](arcane-concordance-invocations.md));
  - a flask's splash also cools hostile creatures it reaches (as a water splash would put them out).
- **Reversible:** switching the Concordance off hides the Concordance fact and its spells but keeps every id.
