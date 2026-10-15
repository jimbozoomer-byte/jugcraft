# October 15 community integration

Status: prepared locally on `integrate/ready-prs-oct15`; publishing and combined CI are pending. Do not treat this batch as shipped until its integration PR is merged.

Owner request: merge all ready open PRs and repair blockers. Integration and fixes by OpenAI Codex (GPT-6). Base: `40381834a75312b8cb8a9b326d32c671f2f20a8e`, which includes the separately reviewed and merged PR #357. The seven uncommitted local technology packages are outside this batch.

## Source branches

The original branch heads are retained as merge ancestors so contributors keep their history. Some earlier features had already been partly integrated in October 13; their remaining changes and provenance are reconciled here.

| PR | Feature | Source head |
| --- | --- | --- |
| [#351](https://github.com/jimbozoomer-byte/jugcraft/pull/351) | Add rocky-upland spodumene deposits with stone outcrops | `c619e89b5e09975388c9b5c504a71afa82d50f45` |
| [#353](https://github.com/jimbozoomer-byte/jugcraft/pull/353) | The Crypt Lich, his dead and his loot (boss 6, part 2) | `9cfe0fdab6716bf8104a8b2b9ea0e609d9f2c3c8` |
| [#354](https://github.com/jimbozoomer-byte/jugcraft/pull/354) | Add four regional mineral-sand deposits with finite reserves | `a78c8cb5fcac916589d97c03cac9d906cc2842cc` |
| [#355](https://github.com/jimbozoomer-byte/jugcraft/pull/355) | Guns slice 13A: the frontier guns (Tabby Revolver, Lookout Rifle, Ammonite Carbine) | `71247f14f87aee23824f6747c9e1c0e689103e8c` |
| [#359](https://github.com/jimbozoomer-byte/jugcraft/pull/359) | Guns slice 13B: more frontier guns (Fencepost Carbine, Waystation Shotgun, Highball Rifle) | `bf6246cdcfa0955b042ede0a6ba59e20335745dc` |
| [#366](https://github.com/jimbozoomer-byte/jugcraft/pull/366) | Machine screens in five looks by tech level, simpler to read | `a36a41c4a7c53b1c76713c77f0033c9c2b2f554d` |
| [#367](https://github.com/jimbozoomer-byte/jugcraft/pull/367) | The Foundry Trench and Sealed Orders (boss 7, the Iron Dreadnought, part 1) | `c8f33f0741cf5ea7ac8b78235edfa680dabed25e` |
| [#368](https://github.com/jimbozoomer-byte/jugcraft/pull/368) | Guns slice 13C: a punt gun, a repeater and a heavier rifle (Flatboat Gun, Windlass Musket, Millstone Rifle) | `36dcdabb5e7928899879f8b525a749941ed111d5` |
| [#369](https://github.com/jimbozoomer-byte/jugcraft/pull/369) | The Iron Dreadnought: the boss, his Sparkers and his loot (boss 7, part 2) | `d8ac567d211f1ea90f64355ab6df22ea049e743c` |
| [#371](https://github.com/jimbozoomer-byte/jugcraft/pull/371) | KV-1/W walker: model, textures and animation clips (ready to implement) | `36e35b509102fe9aa682f82c857763acb550566d` |
| [#373](https://github.com/jimbozoomer-byte/jugcraft/pull/373) | Guns slice 13D: a revolving launcher, a carbine revolver and a musket of light | `5dbaee564086fa18ec15a6db80341044380fcc93` |
| [#375](https://github.com/jimbozoomer-byte/jugcraft/pull/375) | Guns slice 14A: the brass sidearms (Holler Pistol, Minuet Pistol, Half-Crown Revolver) | `553526bc1741345e8f01596e4dd17cc6a1627c78` |
| [#376](https://github.com/jimbozoomer-byte/jugcraft/pull/376) | Tests: fix three chance failures (ore survey at a chunk border, the Culverin's miss) | `acc023429d112a6ede5ba981363291e894236269` |
| [#377](https://github.com/jimbozoomer-byte/jugcraft/pull/377) | Guns: give the Highwayman a recipe of its own (a second brass ingot) | `d8e835ab74c1386acc6ad02bede5f3365046afbd` |
| [#378](https://github.com/jimbozoomer-byte/jugcraft/pull/378) | Add regional vanadium-bearing iron deposits and concentrate processing | `8c54ee432216f3e2083ae10f3aa81f8e8adb365d` |
| [#380](https://github.com/jimbozoomer-byte/jugcraft/pull/380) | Guns slice 14B: the brass carbines (Holler Carbine, Minuet Carbine, Grange Carbine) | `a7d5ac28a868f97af410184ca1a5ccb7638469dc` |
| [#381](https://github.com/jimbozoomer-byte/jugcraft/pull/381) | The other machine screens in their looks | `1ee9a8d0d1342a1f58d9f87667492b26ba82b461` |
| [#383](https://github.com/jimbozoomer-byte/jugcraft/pull/383) | Guns slice 14C: the brass quick-firers (Sprocket Gun, Navvy Shotgun, Freeholder Rifle) | `0b89429783c9a8432fc97cc7c535d84a5cf014c3` |
| [#384](https://github.com/jimbozoomer-byte/jugcraft/pull/384) | Moonlit Ring and the Howling Totem (Alpha Werewolf, part 1) | `746ad52f3f3d378c0edd8b69d2815fdf0f8f3e4f` |
| [#385](https://github.com/jimbozoomer-byte/jugcraft/pull/385) | The Alpha Werewolf: his Hunt, his pack and his loot (boss 8, part 2) | `be3c7ecb157414dc0d3d4d89fb5b1297f0c3cd2a` |
| [#386](https://github.com/jimbozoomer-byte/jugcraft/pull/386) | Tree generation batch 2: pine, warm and mountain trees | `00e317d887a7770b36a868969c917454eaf1ac7a` |
| [#388](https://github.com/jimbozoomer-byte/jugcraft/pull/388) | Tree generation batch 3: yew and the Gloomweald canopy | `42065b0335e26a5a2f4d03e35830cd93b3036228` |
| [#389](https://github.com/jimbozoomer-byte/jugcraft/pull/389) | Tree generation batch 4: snowpetal cherry and shimmer birch | `d3f60a68c637a2ef472bd4b3963d0db83a81b695` |

The KV-1/W walker PR (#371) supplies models, textures and animation clips only. It does not implement a playable vehicle.

## Integration corrections

- Preserve newer factory registrations, language entries, acquisition audits and Mire Hag test isolation when reconciling older branches.
- Register both mineral-sand and vanadium deposit switches/world generation/prospecting; keep both branches in generated handbook data.
- Preserve the Highwayman recipe fix and ore-survey border fixes across the gun/tree chains.
- Combine mineral-sand and pine owner-art imports. Preserve the preview renderer’s translation support from the walker assets.
- Alpha Werewolf client test: Rending Howl was frozen before recovery completed. The hunt timer correctly waits during attacks, so an eight-tick assertion could see `NONE`. Observe the bounded server transition to `MARK`; keep the encounter and hunt assertions intact. Production attack timing is unchanged.

## Validation

- Repository structure and documentation links: passed, including this record.
- Locked dependency/platform check: passed (18 libraries).
- World Designer Node tests: 11 passed. The initial sandbox attempt could not spawn a worker; the authorized retry passed.
- Generated resources refreshed and audited: passed, 2,719 material IDs. Preserved 17 pixel-identical PNGs and the pre-existing Linux floating-point model output; language regeneration changes ordering only.
- Python unit tests: passed (see local validation log for count).
- Common, client and game-test compilation: passed. Full offline Gradle build: passed, all 1,734 required server game tests.
- Optional-integrations-absent build and repaired Alpha Werewolf rendered client test: in progress. Combined GitHub CI is still pending.
- No manual playtest, two-player dedicated-server session, or performance approval is claimed.

## Drafts excluded

These PRs were still draft submissions at the initial inventory. Several are cumulative industrial branches; a green individual workflow alone does not mark their full scope complete. They are not silently promoted by this integration.

| PR | Draft |
| --- | --- |
| [#360](https://github.com/jimbozoomer-byte/jugcraft/pull/360) | Add Chima, a lion cub that hunts cows and is tamed with beef |
| [#361](https://github.com/jimbozoomer-byte/jugcraft/pull/361) | Add the Combined Cycle Power Plant and make the big machines pay |
| [#362](https://github.com/jimbozoomer-byte/jugcraft/pull/362) | Add the optional ruthenium methanation bed |
| [#363](https://github.com/jimbozoomer-byte/jugcraft/pull/363) | Wire the aluminum cell with copper and check for self-output gates |
| [#364](https://github.com/jimbozoomer-byte/jugcraft/pull/364) | Add the Bulk Fermenter, the 5x5x6 variant |
| [#365](https://github.com/jimbozoomer-byte/jugcraft/pull/365) | Add the Large Bio Generator, the 5x6x5 variant |
| [#370](https://github.com/jimbozoomer-byte/jugcraft/pull/370) | Adam-ondi-Ahman, part 1: a valley near the start with a dormant altar knoll |
| [#372](https://github.com/jimbozoomer-byte/jugcraft/pull/372) | Adam-ondi-Ahman, part 2: the Cydonian Sanctum on Mars and the Altar Keystone |
| [#374](https://github.com/jimbozoomer-byte/jugcraft/pull/374) | Add the Bulk Electrolytic Separator |
| [#379](https://github.com/jimbozoomer-byte/jugcraft/pull/379) | Add the blueprint design kit and its in-JAR AI guide |
| [#382](https://github.com/jimbozoomer-byte/jugcraft/pull/382) | The Cydonian Sentinel: the Mars boss, its Red Storm and its loot (Adam-ondi-Ahman part 3) |
| [#387](https://github.com/jimbozoomer-byte/jugcraft/pull/387) | Add the Reagent Purification Station for electronic-grade sulfuric acid |
| [#390](https://github.com/jimbozoomer-byte/jugcraft/pull/390) | Add titanium tools and armor, the step above steel |
