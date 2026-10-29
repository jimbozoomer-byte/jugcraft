# Living city, harvest and Halloween release integration — 29 October 2026

Owner request: merge all ready PRs and prepare the matching client/server update. Integration by OpenAI Codex. Snapshot: 72 open PRs, 52 non-drafts selected and 20 drafts deferred. No draft is promoted merely because it merges cleanly.

## Included

Local living-city work adds resident routines, safe additive decorations, a full-size commission board away from the market stalls, conservative crop/commission payouts, public interactions, proper winter aspen foliage and the guarded GeckoLib/Iris glow adapter. See [living-city-local.md](living-city-local.md) and [rendering-lines-oct28.md](rendering-lines-oct28.md). The exact reported live shader streaks still need owner confirmation; the reproduced missing-pipeline error is fixed.

The following PR heads are integrated together, retaining original history and authors. The harvest/decoration stack and gun stack include every prerequisite.

- [#498](https://github.com/jimbozoomer-byte/jugcraft/pull/498): The Cursed Mirror: a gilt mirror that curses the player who breaks it (`42ab9e778cc3f7ac96d119762d8b47f070069a8a`).
- [#517](https://github.com/jimbozoomer-byte/jugcraft/pull/517): Costume disguises: monsters take a costumed player for one of their own, all year (`070bc93e4106deda37c1949b4745d769cc8f6cb0`).
- [#531](https://github.com/jimbozoomer-byte/jugcraft/pull/531): Guns: guns that push their shooter back (`f63f8b5efe1043997f66624253573c6cb5307aa1`).
- [#533](https://github.com/jimbozoomer-byte/jugcraft/pull/533): The Pumpkin Patch Market, part 1: six squashes and gourds (`433ef2417aabe27ae55e7abc5dac5c4bcf8ed2ce`).
- [#534](https://github.com/jimbozoomer-byte/jugcraft/pull/534): Guns: flying microjets and heavy guns that slow their holder (`e050f7c21aa012d3c62f025326662cb7d4774af1`).
- [#535](https://github.com/jimbozoomer-byte/jugcraft/pull/535): The Pumpkin Patch Market, part 2: the market's displays (`2dc13eb82c7922512d02c0862d4e25959c1bca98`).
- [#536](https://github.com/jimbozoomer-byte/jugcraft/pull/536): Guns slice 16A: a silenced pistol, a burst rifle and a repeater (Hush Pistol, Salvo Rifle, Highball Repeater) (`f212ba0a3571e1db94ae06b4ef12b5b671b0dcd9`).
- [#538](https://github.com/jimbozoomer-byte/jugcraft/pull/538): The Pumpkin Patch Market, part 3: the sign, the ground and the giant (`ce69abb00146e069b9a597b67a92275889a4bee4`).
- [#542](https://github.com/jimbozoomer-byte/jugcraft/pull/542): Orchard & Apple Harvest, part 1: the ladder, the bin and the bag (`453e58cbbcab7a40889c4b271ffb0f7f24385646`).
- [#545](https://github.com/jimbozoomer-byte/jugcraft/pull/545): Orchard & Apple Harvest, part 2: crates, windfalls and the handcart (`e22f81a1435db1d9e68fc08a2ac49f84c5eddb8d`).
- [#546](https://github.com/jimbozoomer-byte/jugcraft/pull/546): Orchard & Apple Harvest, part 3: the Caramel Apple Stand (`58ab518d4aa13867e842be8b0aa2bdd9a006f26a`).
- [#547](https://github.com/jimbozoomer-byte/jugcraft/pull/547): Barn & Harvest Tools, part 1: the Steel Sickle, Watering Can and Seed Pouch (`7402d8852682db2f4c88bbbff207666d39561f9a`).
- [#548](https://github.com/jimbozoomer-byte/jugcraft/pull/548): Barn & Harvest Tools, part 2: the Quern and the Butter Churn (`b2def9af1c92528847042b68315378fcecd40c20`).
- [#549](https://github.com/jimbozoomer-byte/jugcraft/pull/549): Barn & Harvest Tools, part 3: the Drying Rack and the Corn Crib (`50b5845d9c19681432260ade6934c47887441191`).
- [#550](https://github.com/jimbozoomer-byte/jugcraft/pull/550): Barn & Harvest Tools, part 4: the Hay-Loft Pulley and the Harvest Bell (`9ae483c4fc87a3314687b98f98cd3aa5154f3c57`).
- [#551](https://github.com/jimbozoomer-byte/jugcraft/pull/551): Barn & Harvest Tools, part 5: the barn's stores and fittings (`e04e04607ff63aad3f74918fb4c63587c3526ce2`).
- [#552](https://github.com/jimbozoomer-byte/jugcraft/pull/552): Fall Garden & Porch, part 1: the fall garden's plants (`541cd27a57e248f614ae3bb8b1ee07a04e4308d9`).
- [#553](https://github.com/jimbozoomer-byte/jugcraft/pull/553): Fall Garden & Porch, part 2: planters (`e1bcb6df03a1491f2571970c3cae68375e982bdb`).
- [#554](https://github.com/jimbozoomer-byte/jugcraft/pull/554): Fall Garden & Porch, part 3: the porch (`293852f97af85e8a4d839301c02bd1936bad0603`).
- [#555](https://github.com/jimbozoomer-byte/jugcraft/pull/555): Fall Garden & Porch, part 4: café lights that light along their length (`4ff30aa7b2ddcdb5824d2398c7a0ffb702849de3`).
- [#556](https://github.com/jimbozoomer-byte/jugcraft/pull/556): Cozy Fall Hearth, part 1: the Stone Fireplace and its Mantel Garland (`d6694c99f7ed43d4350cfadb8e7d90868cf4dc18`).
- [#557](https://github.com/jimbozoomer-byte/jugcraft/pull/557): Cozy Fall Hearth, part 2: the Woodstove, the Tea Kettle and the Simmer Pot (`684627a2fc3c7fb7fcd602389f1e63b3f657056d`).
- [#558](https://github.com/jimbozoomer-byte/jugcraft/pull/558): Cozy Fall Hearth, part 3: the Firewood Rack and the Chopping Block (`c6c3cfb57573d3dc3f3b4d6f7d84348cda761701`).
- [#559](https://github.com/jimbozoomer-byte/jugcraft/pull/559): Cozy Fall Hearth, part 4: the Blanket Ladder and the Knitting Basket (`45d713eb0af914323ce59819581688d9cefa362a`).
- [#560](https://github.com/jimbozoomer-byte/jugcraft/pull/560): Cozy Fall Hearth, part 5: the Hurricane Lamp and Mason-Jar Candles (`a3daf91e01ace6edc418ce728195c8f7c1e2dd15`).
- [#561](https://github.com/jimbozoomer-byte/jugcraft/pull/561): Haunted Midway, part 1: the Cotton-Candy Cart and the Caramel-Corn Wagon (`d110931b61aac4fdd67fa088fb6849d8c366cf89`).
- [#562](https://github.com/jimbozoomer-byte/jugcraft/pull/562): Haunted Midway, part 2: the Ticket Booth and Whack-a-Zombie (`7cdb4e2762211be8afc5ba31e744aa970735a18a`).
- [#563](https://github.com/jimbozoomer-byte/jugcraft/pull/563): Haunted Midway, part 3: the Shooting Gallery (`faccafbbb4c9321ebb5fa9a5960c97aa3da872f0`).
- [#564](https://github.com/jimbozoomer-byte/jugcraft/pull/564): Guns slice 16B: two microjet guns and a grapeshot gun (`78505940a0ee364c4aabe6951b1467eb9eb77ba9`).
- [#565](https://github.com/jimbozoomer-byte/jugcraft/pull/565): Fix holding industrial machines crashing placement previews (`ada1d55ff418c2eb26c9061f5eb66ec44cd76a0e`).
- [#568](https://github.com/jimbozoomer-byte/jugcraft/pull/568): Haunted Midway, part 4: the Ghost Train (`74358c9b23cc9e4a0ab823ca193bd47525f0f72b`).
- [#569](https://github.com/jimbozoomer-byte/jugcraft/pull/569): Haunted Midway, part 5: the Haunted Carousel (`368a22709355445107fcef9cb250b007bc3efc82`).
- [#570](https://github.com/jimbozoomer-byte/jugcraft/pull/570): Front-Yard Haunt Scenes, part 1: the Haunted Mailbox and Pumpkin Path Light (`955ae26815eba158c6eaaf728145f64c944b64f4`).
- [#571](https://github.com/jimbozoomer-byte/jugcraft/pull/571): Front-Yard Haunt Scenes, part 2: skeleton dog, cat and crow, and the Rising Zombie (`778e0559178c28043505a8c112f5a67d510c11b5`).
- [#572](https://github.com/jimbozoomer-byte/jugcraft/pull/572): Front-Yard Haunt Scenes, part 3: gargoyle door knocker, bat wreath and Beware doormat (`ca61cdeecd522f543a6724a056be6db291594625`).
- [#573](https://github.com/jimbozoomer-byte/jugcraft/pull/573): Front-Yard Haunt Scenes, part 4: the Crashed Witch (`457ab1ad08629a692fd54819fbabcf7d2d93ff2d`).
- [#574](https://github.com/jimbozoomer-byte/jugcraft/pull/574): Front-Yard Haunt Scenes, part 5: the Trick-or-Treat Candy Chute (`aee681cc63a438b35f2edae841a5cec809501a80`).
- [#575](https://github.com/jimbozoomer-byte/jugcraft/pull/575): Halloween Party Table, part 1: the Candy Corn Dispenser (`cef3f9dce65ade735e49691f902cb526f9f9ce3a`).
- [#576](https://github.com/jimbozoomer-byte/jugcraft/pull/576): Halloween Party Table, part 2: the cake stands and the Ghost Meringue (`76f31b42140c2085225931490747c68d152aecae`).
- [#577](https://github.com/jimbozoomer-byte/jugcraft/pull/577): Halloween Party Table, part 3: the trays and their foods (`6b1045b9a542fda336b9968246bbbff70d5a5f7b`).
- [#579](https://github.com/jimbozoomer-byte/jugcraft/pull/579): Halloween Party Table part 4: the Pumpkin Layer Cake (`1a314fc68865e1554311df6c9fa5cd72ac27b074`).
- [#580](https://github.com/jimbozoomer-byte/jugcraft/pull/580): Halloween Party Table part 5: the table (`5eeb8d5d07c5c1b0c284bc44ad9b9e0b042a9006`).
- [#581](https://github.com/jimbozoomer-byte/jugcraft/pull/581): Clockwork Animatronics part 1: the Organ-Grinder Skeleton (`8969c888d542cab9c2d66683c31bf5820c449f52`).
- [#582](https://github.com/jimbozoomer-byte/jugcraft/pull/582): Clockwork Animatronics part 2: the Animatronic Werewolf (`e36f3ca98d9bc37e4c7ba20e742dcd6a4d5ccd37`).
- [#583](https://github.com/jimbozoomer-byte/jugcraft/pull/583): Clockwork Animatronics part 3: the Flying Crank Ghost (`aa4dfeba7c452bd054d7aa88827659471a380874`).
- [#584](https://github.com/jimbozoomer-byte/jugcraft/pull/584): Clockwork Animatronics part 4: the Clockwork Raven (`e454ac15ab190f8a7b0a19a1a5e6fafc1eedb872`).
- [#585](https://github.com/jimbozoomer-byte/jugcraft/pull/585): Clockwork Animatronics part 5: the Pumpkin-Headed Drummer (`0273f477cbb1465c82acc31d4fb86ea2f6be4bd9`).
- [#586](https://github.com/jimbozoomer-byte/jugcraft/pull/586): Clockwork Animatronics part 6: the Show Controller (`8829f9a20e57c16f68cef2402314b8789b6f9602`).
- [#587](https://github.com/jimbozoomer-byte/jugcraft/pull/587): Harvest Festival Grounds part 1: the Giant Cornucopia (`472ef3a88e3ac0c25e2d7508342298b1fdffb962`).
- [#588](https://github.com/jimbozoomer-byte/jugcraft/pull/588): Harvest Festival Grounds part 2: the Giant Vegetable Show (`56fdefd18ff35a25fb843a4508019b3f7d9e2c44`).
- [#589](https://github.com/jimbozoomer-byte/jugcraft/pull/589): Harvest Festival Grounds part 3: the Steam Traction Engine and Hay Wagon (`e72d8bc60b405d0339628de7744b0c3413163b56`).
- [#590](https://github.com/jimbozoomer-byte/jugcraft/pull/590): Harvest Festival Grounds part 4: Harvest Bunting and the Sack-Race Sack (`df57e154c33139537f4c235923671ad569654ba4`).

## Integration and validation

- The harvest tip, gun tip, disguises and machine-preview heads had successful individual checks. The Cursed Mirror had no check run on its current head and must pass combined validation before release.
- PR #549's earlier optional-absence failure was a Fabric Maven dependency download timeout, not a compiler or game-test failure. Later descendants passed that job. Cancelled superseded Foundation jobs are recorded in the local snapshot; they are not counted as failed gameplay tests.
- Shared registration, configuration, translations, generated-file inventory and alchemy-value additions retain both sides. The October 28 release ledger keeps the newer main-branch account of repairs rather than the older branch copy.
- Existing protected city blocks and owner casino fixtures remain untouched; revision 4 requires an explicit conservative upgrade, never regeneration.
- Combined generation, resource audits, native and rendered checks are pending. No human gameplay or two-account test is claimed.

## Deferred drafts

- [#496](https://github.com/jimbozoomer-byte/jugcraft/pull/496): The Storm Roc, his fledglings and his loot (the Storm Roc, part 2).
- [#501](https://github.com/jimbozoomer-byte/jugcraft/pull/501): Let blank runes be crafted again.
- [#502](https://github.com/jimbozoomer-byte/jugcraft/pull/502): Give Immolation and Drygmy's Blessing a source.
- [#503](https://github.com/jimbozoomer-byte/jugcraft/pull/503): Welcome new players to the encyclopedia.
- [#504](https://github.com/jimbozoomer-byte/jugcraft/pull/504): Give every item a name of its own.
- [#506](https://github.com/jimbozoomer-byte/jugcraft/pull/506): Fix the encyclopedia's unreachable chapters, empty topics and keys.
- [#509](https://github.com/jimbozoomer-byte/jugcraft/pull/509): Make Industrial copper and iron cables carry what their metal should.
- [#512](https://github.com/jimbozoomer-byte/jugcraft/pull/512): Let naphtha make plastic.
- [#513](https://github.com/jimbozoomer-byte/jugcraft/pull/513): Found plastic: chest loot that breaks down into pellets.
- [#515](https://github.com/jimbozoomer-byte/jugcraft/pull/515): Add the Glass Chapel and the Glass Accord (the Glass Abbot, part 1).
- [#518](https://github.com/jimbozoomer-byte/jugcraft/pull/518): Add the Glass Abbot, his images and his loot (the Glass Abbot, part 2).
- [#521](https://github.com/jimbozoomer-byte/jugcraft/pull/521): Proposals: plastic, first powered tool, currencies, overlap.
- [#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526): Arcane Concordance garden cohort: six garden spirits on the shared pact record.
- [#532](https://github.com/jimbozoomer-byte/jugcraft/pull/532): Add the Cinder Tyrant's heat and cooling package (Concordance Chunk E).
- [#539](https://github.com/jimbozoomer-byte/jugcraft/pull/539): Every lair keeper pays its support: five keepers onto Encounter and LairRewards (Chunk E, E4 part 1).
- [#540](https://github.com/jimbozoomer-byte/jugcraft/pull/540): Give every lair a dossier, with lore, preparation and the way home (Concordance Chunk E, E4).
- [#543](https://github.com/jimbozoomer-byte/jugcraft/pull/543): Faster replays, a clear reset and a loadout measurement (Concordance Chunk E, E4).
- [#544](https://github.com/jimbozoomer-byte/jugcraft/pull/544): Astronomy ideas in the TODO, and the star-bred lines plan (animals first, the Astrawand, a gene lab).
- [#566](https://github.com/jimbozoomer-byte/jugcraft/pull/566): Star-bred lines, phase 1: star-born animals and the Astrawand.
- [#578](https://github.com/jimbozoomer-byte/jugcraft/pull/578): Make the owner's armour sets winnable.

## Live baseline checked

Read-only inspection of the hosting panel on 29 October confirms the server is still on `0.1.0-alpha.20261027.1`, source `233cca018de96f068b639b0b8ed93ad0eef53962`, and running. The October 28 source integration was not deployed. Therefore this rollout also includes that batch's companions, guns, seasonal events, Horseman lair and reliability repairs. The latest published GitHub pack agrees with the panel. Use the October 27 artifact inventory as the patch/rollback baseline.

## Rollout

No live restart is authorized by this update request. Prepare and validate the exact merged source, matching artifacts and rollback files first. Then obtain fresh owner restart confirmation, send the authorized in-game warning and wait all 30 seconds. Preserve worlds/player data/configuration, replace only managed release files, verify startup and then promote the client channel. Publishing a release alone must not activate client updates.
