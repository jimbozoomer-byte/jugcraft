# Guns: the scrap guns (slice 1), the iron set (slice 2), the lever set (slice 3), the black powder guns (slice 4), the attachments (slices 5 and 7) and the guns in use (slice 6)

Status:
- **Slice 1** (the Rust Midge, Patchwork Carbine and Thunderpipe): implemented on `claude/guns` (#248), awaiting review. It is stacked on `claude/owner-gun-models` (#247), which adds the owner's gun models to the owner asset library.
- **Slice 2** (the iron set: the Warden Pistol, Riveter SMG and Haymaker; [below](#slice-2-the-iron-set)): implemented on `claude/guns-iron` (#252), stacked on slice 1, awaiting review.
- **Slice 3** (the lever set: the Longhorn Rifle, Drover Rifle and Coach Gun; [below](#slice-3-the-lever-set)): implemented on `claude/guns-lever` (#253), stacked on slice 2, awaiting review.
- **Slice 4** (the black powder guns: the Duelling Pistol, Line Musket and Bellmouth, and the Paper Cartridge; [below](#slice-4-the-black-powder-guns)): implemented on `claude/guns-powder` (#255), stacked on slice 3, awaiting review.
- **Slice 5** (the attachments: silencers, a muzzle brake, an extended barrel, magazines, stocks and grips; [below](#slice-5-the-attachments)): implemented on `claude/guns-attachments` (#256), stacked on slice 4, awaiting review.
- **Slice 6** (the guns in use: muzzle flash, spent casings, a zoom when aiming and the hold seen from outside; [below](#slice-6-the-guns-in-use)): implemented on `claude/guns-polish` (#259), stacked on slice 5, awaiting review.
- **Slice 7** (finishing the attachments: bayonets that stab, and the five guns whose parts use shared textures; [below](#slice-7-bayonets-and-the-shared-texture-guns)): implemented on `claude/guns-attachments-2`, stacked on slice 6, awaiting review. Scopes wait for the owner (see there).
- **Not yet played:** the Java compiles only in CI, and the game tests there are the only runs.
Proposal issue: none. The owner asked on 7 October 2026: "I want to start working on the Guns plugin which I want to base off of the Mod Scorched Guns 2 I have models and animations that I have created already on the github in the "Blocks" folder for that part". The owner's answers:
- on the files: "the files in the blocks folder are all mine I made all of them myself and have all the rights to them they are inspired by scorched guns 2 but I made all of them including the animations";
- on the models, with the upload: "I have rights for all these";
- the first guns: "Gnat, Makeshift, Boomstick";
- the names: "Propose names";
- the eight sounds that carry other sources' tags: "I have the rights";
- the next guns ("Ok lets do more!!!"): all four sets offered, the iron set, the lever rifles, the black powder guns and the attachments, each in its own slice; names: "Propose names".
- what next ("ok what next"): all four offered, each its own pull request: gun polish (this slice 6), finishing the attachments, more guns, and the flaky tests.

Owner: jimbozoomer-byte (models, textures, animations and sounds: the owner; implementation: Claude Opus 5.5).
Target milestone and tier: early firearms, after the first copper and iron. These are the bottom rungs of a gun line the owner's library holds well over a hundred more of.
Primary specialty and supported player role: combat (ranged). It serves anyone who fights, and players who would rather not melee.

## Player experience
Three guns, each the owner's model with the owner's animations:

| | Rust Midge | Patchwork Carbine | Thunderpipe |
|---|---|---|---|
| The owner's gun | Rusty Gnat | Makeshift Rifle | Boomstick |
| What it is | a copper machine pistol | a stockless carbine | a sawn-off double barrel |
| Fires | held, automatic | one shot each pull | one shot each pull, 8 pellets |
| Damage | 2 a bullet | 6 | 2.5 a pellet (20 if all land) |
| Rate | 6.7 a second (every 3 ticks) | 3.3 a second (every 6 ticks) | 2.5 a second (every 8 ticks) |
| Holds | 20 | 10 | 2 |
| Reload | 2.35 s, the magazine | 2.4 s, the magazine | 0.25 s, then 0.6 s a shell, then 0.65 s |
| Spread, hip / aimed | 4° / 1.5° | 2° / 0.25° | 9° / 6° |
| Range | 48 blocks | 96 | 24 |
| Round | Light Round | Rifle Round | Buckshot Shell |

**Controls:**
- **Left click** fires (held, for the Midge). With a gun in hand the attack button never mines or punches.
- **Right click** (held) aims down the sights: the gun slides so its sight sits on the crosshair, the spread tightens, and you walk at 60%.
- **G** reloads. **H** inspects. Both are rebindable under Jugcraft's controls. They are not R and I because Iris, in the pack, keeps R for reloading shaders and I for its shader screen.
- Pulling the trigger on an empty gun clicks, and reloads it if you carry its rounds.

**What you see:**
- In first person you see the gun and your own arms (your skin, wide or slim). The owner's animations play: draw, idle, shoot (and aimed shoot), inspect, and the reloads.
  - The Midge's magazine drops out and the new one slides in.
  - The Thunderpipe's left hand fetches each shell and pushes it into the breech.
- The animations' sound cues play the owner's sounds: mag out, mag in, slap, bolt, rack, rustle and the rest.
- Each shot kicks the view up a little: more for a heavy shot, less when aimed.
- The counter beside the hotbar shows the rounds loaded and the rounds you carry, or "Reloading".
- The gun's durability bar shows how full it is.
- Other players see your gun fire and reload in your hand, and hear it.
- In the inventory, on the ground and in frames each gun shows as its 3D model, as the owner's do.

**Hits:** a shot is instant along your look, strayed by the spread, to the first block or creature in range.
- Blocks it strikes throw up dust.
- A creature takes the damage at once. Guns fire faster than vanilla's half-second shield after a hit, so the bullet damage type passes it (`minecraft:bypasses_cooldown`) and each shot counts.
- The Thunderpipe's pellets on one creature land as one hit.

**The rounds:** crafted cheaply from early metal.

| Round | Recipe | Makes |
|---|---|---|
| Light Round | copper ingot, lead nugget, gunpowder (in a column) | 8 |
| Rifle Round | brass nugget, lead nugget, gunpowder (in a column) | 4 |
| Buckshot Shell | paper and lead nugget, paper and gunpowder (each beside paper), brass nugget below | 4 |

**The guns** are crafted at a crafting table:
- **Rust Midge:** copper ingots and iron ingots round a lever.
- **Patchwork Carbine:** three iron ingots over planks, a lever and a copper ingot.
- **Thunderpipe:** two iron ingots over planks and a lever.

## Slice 2: the iron set
Three more of the owner's guns, a step up from the scrap guns, in iron and brass:

| | Warden Pistol | Riveter SMG | Haymaker |
|---|---|---|---|
| The owner's gun | Defender Pistol | Greaser SMG | Bruiser |
| What it is | an iron service pistol, held in one hand | an iron submachine gun | a short pump shotgun, fired one-handed |
| Fires | one shot each pull | held, automatic | one shot each pull, 8 pellets |
| Damage | 4 | 2.5 a bullet | 3 a pellet (24 if all land) |
| Rate | 4 a second (every 5 ticks) | 6.7 a second (every 3 ticks) | 1.4 a second (every 14 ticks) |
| Holds | 12 | 30 | 5 |
| Reload | 2.35 s, the magazine | 2.25 s, the magazine | 0.35 s, then 0.55 s a shell, then 1.15 s |
| Spread, hip / aimed | 2.5° / 0.75° | 3° / 1° | 7° / 5° |
| Range | 56 blocks | 48 | 28 |
| Round | Light Round | Light Round | Buckshot Shell |

**What you see:** the owner's animations, as for the scrap guns.
- The Warden Pistol and the Haymaker are held in the right hand alone; the left hand comes in only to reload. The pistol's slide kicks back on each shot, its magazine drops, and the left hand brings in the new one. The Haymaker's left hand feeds each shell, then works the pump.
- The Riveter is held in both hands; its bolt runs on each shot, and the magazine drops out and back.

**Crafting** (a crafting table, each round the same as before):
- **Warden Pistol:** three iron ingots over a lever and a brass ingot.
- **Riveter SMG:** three iron ingots over a brass ingot, a lever and an iron ingot.
- **Haymaker:** three iron ingots over a brass ingot, a lever and planks.

**Connections:** brass ingots (brass is alloyed from copper and zinc, the zinc switch's ore) join the iron. No new round, no new material: the pistol and SMG fire Light Rounds and the Haymaker Buckshot Shells.

**Balance:** starting numbers, a little above the scrap guns. The Riveter's 16.7 damage a second empties 30 rounds in 4.5 seconds for 75 damage; the Haymaker's 24 at point blank outdoes the Thunderpipe's 20 but holds five and fires slower.

**How the models were built** (as for the scrap guns, `tools/guns.py`):

| Gun | Bones |
|---|---|
| Warden Pistol | `gun_body2` > `gun_body` (main and standard barrel); `bolt` (the owner's `receiver`, the slide); `magazine` and `magazine_2` (both the standard magazine: the reload drops one and brings the other in) |
| Riveter SMG | `gun_body2` > `gun_body` (main, standard barrel and sights); `bolt`; `magazine` |
| Haymaker | `gun_body2` > `gun_body` (main); `barrel` (the pump: the shot and the pump slide it back) |

- **Hands:** the right hand on each grip (pistol (8, 1.9, 15.3), SMG (8, 1.45, 15.85), Haymaker (8, 1.7, 15.75)); the Riveter's left on the fore-end (8, 2, 8.5).
- **One-handed guns:** the Warden's and Haymaker's idles hide the left arm, so its rest point is given for a reload keyframe instead (`"hand_pose"` in `BUILDS`):
  - the Warden's left hand is at the magazine's base (8, 0.35, 15.3) at 1.75 s into the reload. Through the insert (1.37 to 1.7 s) the hand stays within a pixel of the new magazine;
  - the Haymaker's is under the barrel (8, 4.3, 9) at 0.29 s into the reload's end. Through the pump (0.33 to 0.58 s) it stays about 2 px under the barrel as both slide back.
- **Arms:** run down, back and out from each hand, as for the scrap guns (each arm's direction is set in its reference pose).
- **Sounds:** each gun's own shot (the Warden fires the library's iron pistol shot); the Haymaker's loop plays the shell insert, as the Thunderpipe's does.

## Slice 3: the lever set
The owner's two lever rifles and the Callwell, offered as the "lever rifles". The Callwell turned out to be an over-and-under shotgun that breaks open to load, not a lever gun; it is here as the owner chose it, as the Coach Gun.

| | Longhorn Rifle | Drover Rifle | Coach Gun |
|---|---|---|---|
| The owner's gun | Marlin | Winnie | Callwell |
| What it is | a heavy lever-action rifle | a lever-action rifle with a long tube | an over-and-under shotgun that breaks open |
| Fires | one shot, then the lever is worked | the same | one shot each pull, 8 pellets |
| Damage | 8 | 6.5 | 3 a pellet (24 if all land) |
| Rate | 1.5 a second (every 13 ticks: the lever) | 1.5 a second | 2.5 a second (every 8 ticks) |
| Holds | 6 | 10 | 2 |
| Reload, a round at a time | 0.4 s, then 0.55 s a round, then 0.7 s | 0.4 s, then 0.6 s a round, then 0.85 s | 0.6 s, then 0.65 s a shell, then 0.65 s |
| Spread, hip / aimed | 1.5° / 0.15° | 2° / 0.3° | 6° / 4° |
| Range | 120 blocks | 100 | 32 |
| Round | Rifle Round | Rifle Round | Buckshot Shell |

**What you see:** after each shot the rifles' levers swing down and back, and the gun cants as the lever is worked. The Longhorn's left hand carries each cartridge to the receiver. The Coach Gun's barrels tip open on their hinge for loading and snap shut.

**Crafting** (a crafting table):
- **Longhorn Rifle:** two iron ingots and a brass ingot over planks and a lever.
- **Drover Rifle:** two iron ingots and a brass ingot over a lever and planks.
- **Coach Gun:** two iron ingots over a brass ingot, a lever and planks.

**Connections and balance:** the same metals as the iron set; no new round or material. The rifles are the long-range option: the Longhorn's 8 a shot at 120 blocks against the Patchwork Carbine's 6 at 96, at under half the carbine's rate. The Coach Gun sits between the Thunderpipe and the Haymaker. Starting numbers.

**How the models were built** (`tools/guns.py`): the lever loops and the Callwell's barrels are not separate parts in the owner's files, so a bone's part can now be some of a part's elements:

| Gun | Bones |
|---|---|
| Longhorn Rifle | `gun_body` (main but its 8th element, standard barrel, sights); `lever` (main's 8th element: the flat lever loop), turning about (8, 2, 12.7); `bolt`; `shell` (a drawn cartridge) |
| Drover Rifle | `gun_body` (main but its 11th element, standard barrel, sights, hammer); `lever` (main's 11th element), turning about (8, 2.3, 12.9); empty `bolt` and `shell` (its animations never show the round) |
| Coach Gun | `gun_body` (main's elements the owner named "main"); `barrel` (those named "barrel": both barrels, the rib, the bead and the fore-end), tipping about the hinge (8, 1.9, 11.6); empty `bolt` and `shell` |

- **The cartridge:** a 1 × 1 × 3 px brass case with a lead tip, drawn into an empty corner of the Longhorn's atlas copy. Its rest place keeps it beside the left hand while the hand carries it in (2 to 3 px off through the carry), and puts it at the receiver's side as it goes in.
- **Hands:** the right hand on each wrist, low on it, (8, 1.7 to 1.9, 16.4). Placed at the wrist's middle, the fist reached up to the sight line when aiming and hid the target in a first-person preview. The left hands are on the fore-ends.
- **Sounds:** the rifles' lever cue plays the library's lever sound (a new shared event, `guns.lever`); each loop's insert cue plays the shell insert. Shots: the Longhorn the library's heavy rifle shot, the Drover the cowboy rifle's, the Coach Gun the brass shotgun's.

## Slice 4: the black powder guns
Muzzle-loaders: one heavy shot, then a long reload (bite the cartridge, pour, drop the ball, ram it home). They are the cheapest guns, iron, wood and a flint, so they also make an early way in.

| | Duelling Pistol | Line Musket | Bellmouth |
|---|---|---|---|
| The owner's gun | Flintlock Pistol | Musket | Blunderbuss |
| What it is | a flintlock pistol, held in one hand | a long flintlock musket | a flintlock blunderbuss with a flared muzzle |
| Fires | one shot | one shot | one shot, 10 balls |
| Damage | 9 | 14 | 2.5 a ball (25 if all land) |
| Holds | 1 | 1 | 1 |
| Reload | 3.7 s | 3.9 s | 3.9 s |
| Spread, hip / aimed | 4° / 2° | 2.5° / 0.75° | 12° / 9° |
| Range | 32 blocks | 64 | 20 |
| Round | Paper Cartridge | Paper Cartridge | Paper Cartridge |

**The round:** the **Paper Cartridge**, a lead ball and its powder in paper: paper, a lead nugget and gunpowder in a column make 4. A new round, since none of the others suits a muzzle-loader; its icon is a map (`tools/item_icons/paper_cartridge.txt`, canvas and iron).

**Crafting** (a crafting table): the Duelling Pistol, two iron ingots and a flint over planks; the Line Musket, three iron ingots over a flint and two planks; the Bellmouth, a copper ingot and two iron ingots over a flint and two planks.

**What you see:** the owner's animations: the hammer falls and a flash of priming fire jumps from the pan; on the reload the gun tips up, the ball goes down the muzzle and the ramrod drives it home in strokes. The Duelling Pistol is held in one hand; the left comes in to load.

**How the models were built:**
- **Hammer:** the owner's part, a flat cock on the lock's right side, on the `hammer` bone, turning about its foot.
- **Ball, ramrod and flash:** the animations move them on bones that had no parts. They are drawn here, each one box in an empty corner of the gun's atlas copy (the Musket's flash is the owner's own part):
  - a 1 px lead ball, resting where the reload's first hold puts it at the muzzle;
  - a half-pixel iron ramrod with a brass tip (6, 12 and 10 px long), resting where the reload's farthest reach puts its back end at the muzzle, in line with the bore, so its strokes drive it in;
  - a 1 px priming flash at the pan.
- **Shown only while moved:** the renderer now hides every such prop (shell, ball, ramrod, flash) unless an animation is moving it, as it already did the Thunderpipe's shell. The idle, which leaves them alone, would otherwise show them at rest.
- **The Duelling Pistol's left hand** (hidden by its idle) is placed at the muzzle, holding the ball, 0.71 s into the reload.
- **The Bellmouth's left arm** hangs from a `left_arm2` bone, which its reload slides back 5.75 px.
- **Sounds:** each gun fires the library's black powder shot. The reload cues `insert`, `metal` and `jam` play the library's insert and metal sounds (new shared events: `guns.insert`, `guns.metal`, `guns.jam`).
- **Known limit:** at about half a second into the Duelling Pistol's reload, its left hand comes close to the view with the pistol tipped up, and the arm covers much of the screen (first-person preview) for under half a second. Play will tell whether it wants a different anchor for that hand.

**Balance:** starting numbers. The Line Musket hits hardest of any gun so far (14) but needs 3.9 s to reload; the Bellmouth's 25 at point blank falls off fast.

## Slice 5: the attachments
The owner's parts include, for most guns, a set of attachments drawn to fit that gun: silencers, a muzzle brake, an extended barrel, magazines, stocks and grips. Slice 5 makes each kind one item that fits every gun with such a part, and shows it on each gun as that gun's own part.

**The attachments** (each the owner's item model, with its texture; a gun takes one in each slot):

| Attachment | Slot | Effect |
|---|---|---|
| Silencer | barrel | damage −5%, shot sound −65% |
| Baffled Silencer | barrel | shot sound −80% |
| Muzzle Brake | barrel | aimed spread −15%, kick −50% |
| Extended Barrel | barrel | range +30%, hip spread −15%, aimed spread −15% |
| Extended Magazine | magazine | rounds +50%, reload time +15% |
| Speed Magazine | magazine | reload time −35% |
| Light Stock | stock | hip spread −15% |
| Weighted Stock | stock | aimed spread −30%, kick −40% |
| Wooden Stock | stock | hip spread −10%, aimed spread −15%, kick −25% |
| Light Grip | grip | hip spread −20% |
| Vertical Grip | grip | kick −35% |

The silencers and the muzzle brake sit at the muzzle in front of the barrel; the Extended Barrel takes the barrel's place, the magazines the magazine's, and on the Thunderpipe a stock takes the place of its pistol grip (the owner's stocks carry their own grip). The grips go under the fore-end.

**Which guns take which** (where the owner made the part for the gun, on the gun's own texture):

| Gun | Barrel | Magazine | Stock | Grip |
|---|---|---|---|---|
| Rust Midge | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | Extended Magazine, Speed Magazine | Light Stock, Weighted Stock, Wooden Stock | – |
| Patchwork Carbine | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | Extended Magazine, Speed Magazine | Light Stock, Weighted Stock, Wooden Stock | Light Grip, Vertical Grip |
| Thunderpipe | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | – | Light Stock, Weighted Stock, Wooden Stock | Light Grip, Vertical Grip |
| Warden Pistol | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | Extended Magazine, Speed Magazine | – | – |
| Riveter SMG | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | Extended Magazine, Speed Magazine | Light Stock, Weighted Stock, Wooden Stock | – |
| Haymaker | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | – | – | – |
| Longhorn Rifle | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel | – | Light Stock, Weighted Stock, Wooden Stock | Light Grip, Vertical Grip |

The Drover Rifle, the Coach Gun and the muzzle-loaders took none in slice 5: the owner drew their attachments on textures shared between guns (`carabine_grips`, `flintlock_stocks`, `musket_bayonets` and others), and a GeckoLib model draws from one texture. Slice 7 packs those into each gun's atlas and adds the bayonets ([below](#slice-7-bayonets-and-the-shared-texture-guns)); the tactical grip and scopes wait for a later slice.

**Fitting and taking off** (a crafting table, or the 2 × 2 grid):
- A gun and an attachment it takes, alone in the grid, give the gun with the attachment fitted. The rounds loaded stay. An attachment the gun wore in that slot comes off and stays in the grid where the new one lay.
- A gun with attachments and shears give the gun without the attachment fitted last; that attachment stays in the grid where the gun lay, and the shears are kept.
- The recipe book does not list these (they are special recipes, as the Skeleton Key's copying); each attachment's tooltip says how, and names the guns it fits.
- A gun whose larger magazine comes off keeps the rounds it held beyond its own magazine until they are fired; it will not reload until it is below its own.

**What you see:** the attachment on the gun, in your hands, in other players' hands and in the inventory; it moves with the part it is fixed to (a magazine drops out with the reload, the Haymaker's silencer rides its pump). A Speed Magazine's change plays the owner's reload faster, an Extended Magazine's a little slower, so the animation ends when the reload does. The tooltips list each attachment's effects, green where it helps and red where it costs, and a gun's tooltip its attachments and its numbers with them.

**Crafting** (a crafting table):
- Silencer: an iron ingot, wool, an iron ingot in a row; the Baffled Silencer: a brass ingot either side of a Silencer.
- Muzzle Brake: an iron nugget, a brass ingot, an iron nugget in a row; Extended Barrel: two iron ingots and a brass ingot in a row.
- Extended Magazine: an iron ingot over a brass ingot over an iron ingot; Speed Magazine: a brass ingot over a slime ball over a brass ingot.
- Light Stock: two sticks and leather in a row; Weighted Stock: two planks and an iron ingot; Wooden Stock: two planks and leather.
- Light Grip: leather over a stick; Vertical Grip: an iron ingot over a stick over leather.

**How the models were built** (`tools/guns.py`, `effective_bones()`): each gun's model gains, under the bone holding a slot's standard part (or under `gun_body`), an `att_<id>` bone for each attachment it takes, with that bone's pivot, so the attachment rides the barrel or magazine as the animations move them. Where an attachment replaces the standard part, the part moves to a `std_<slot>` bone of its own. The Warden Pistol's magazine slot is on two bones (the magazine and the one its reload brings in), so it has a second set suffixed `_2`. Every attachment part is re-baked and compared face for face with the owner's, as the guns' own parts are; the props' atlas corners are checked to stay clear of every attachment's texture. The renderer shows a fitted attachment's bone and hides a replaced part's; nothing else in the model or the animations changes.

**Connections:** iron, brass, wool, leather, sticks, planks and a slime ball, all early. Each attachment is a choice, not a strict upgrade: the Silencer costs a little damage, the Extended Magazine reload time; the slots keep a gun from stacking two of a kind.

**Balance:** starting numbers. The largest changes: a Riveter SMG with an Extended Magazine holds 45; a Patchwork Carbine with an Extended Barrel reaches 125 blocks; a Weighted Stock and a Muzzle Brake together leave 30% of the kick.

**Known limits:** the left hand stays where the owner's animations put it, so it does not move onto a vertical grip; a silenced shot is quieter, not shorter-ranged: other players within 16 blocks still hear it, softly (no gun's shot is sent to players farther away than that).

## Slice 6: the guns in use
Slice 6 adds no items. It makes a shot look like one, and a gun look held, using the owner's flash and casing art and the cues the owner put in the animations.

**Muzzle flash:**
- Each shot shows one of the owner's four flash frames (`Big Cannons and Mounted Guns/textures/muzzleflash*.png`) at the gun's `muzzle` locator, full bright, for two ticks. It swells a little and fades.
- Each shot picks its frame and its turn about the barrel from its time, so a burst flickers.
- The frame faces back down the barrel: the shooter sees a star. Two copies cross along the barrel, so a player beside them sees the flash too.
- **Size,** in the model's pixels, by the round: light 5, rifle 7, buckshot 8, paper cartridge 10 (black powder flares widest).
- **Barrel attachments:** a Muzzle Brake or an Extended Barrel moves the flash to its own front (a `muzzle_<attachment>` locator, on the bore at the front of the part). A Silencer or a Baffled Silencer hides it.
- **Black powder:** a muzzle-loader's shot also blows a white cloud in front of the muzzle.
- **Who sees it:** everyone near the shooter. The player's own shots show as they fire; other players' shots show when the server's `GunActionPayload` arrives.

**Spent casings:**
- The owner's animations cue `eject_casing` where a case comes out: at the start of the shot for most guns, and as the Coach Gun breaks open to load. The Drover Rifle's also cues one as its loading ends. The Line Musket has none.
- At the cue, the round's case flies out of the ejection port to the gun's right and a little up. It tumbles, falls and lies where it lands for two or three seconds. A left-handed player's go to the left.
- **Each case** is the owner's art: a Light Round's is `small_copper_casing`, a Rifle Round's `large_brass_casing`, a Buckshot Shell's `shotgun_shell`. They are particles: `jugcraft:light_round_casing`, `rifle_round_casing` and `buckshot_shell_casing`.
- A paper cartridge leaves no case. The Duelling Pistol's and the Bellmouth's cue puffs a little smoke from the lock instead.
- The other cues (`loaded`, `end_reload`, `loop_end`, `reload_end`) mark points in a reload that the server's timing already covers, and show nothing.

**Zoom:** aimed down the sights, the view narrows, eased in and out with the aim.

| Guns | Field of view, aimed |
|---|---|
| Thunderpipe, Haymaker, Bellmouth | 92% |
| Rust Midge, Warden Pistol, Coach Gun, Duelling Pistol | 90% |
| Riveter SMG | 88% |
| Patchwork Carbine, Line Musket | 82% |
| Drover Rifle | 80% |
| Longhorn Rifle | 75% |

It multiplies vanilla's own modifier (sprinting, a speed effect), and the camera smooths it as it does vanilla's. Scopes (a later slice) will narrow it further.

**Seen from outside:**
- The gun arm comes up along the look, so the gun points where the player looks. The owner's third-person transforms are made for a raised arm: hanging at the side, the gun pointed at the ground.
- For a gun held in both hands, the other arm comes across to the fore-end, as vanilla holds a loaded crossbow.
- The Warden Pistol, the Haymaker and the Duelling Pistol are one-handed (their idle hides the left arm). The other arm stays as vanilla poses it.
- The arms follow the head up and down, keep vanilla's crouch, and a left-handed player's are mirrored.
- The other arm keeps to its own work while it eats or holds up a shield.
- Not while swimming, gliding or asleep. Armor is posed from the same state, so it follows.

**How:**
- `client/guns/GunEffects`: the shots, the casings and the smoke.
- `GunFlashLayer`: a GeckoLib render layer at the muzzle locator's bone.
- `GunCasingParticle`: the cases.
- `GunLooks`: each gun's hold and zoom, the flash sizes, and the attachments that hide the flash.
- `GunPose`: the hold. It is called from the arms motion hooks (`ArmsRenderStateMixin`, `ArmsHumanoidModelMixin`) that already pose players.
- `GunView.fov`: the zoom.
- **One new client mixin,** `GunFovMixin`:
  - It multiplies what `AbstractClientPlayer.getFieldOfViewModifier` returns; Fabric API has no field of view event.
  - It takes no arguments and is optional (`require = 0`): a renamed method would load without the zoom, and the client game test would fail and say so.
- **`tools/guns.py`:** the numbers (`ZOOM`, `FLASH_SIZE`, `CASINGS`, `two_handed()`, `hides_flash`), the locators and the art copies. `check_guns` holds the Java to them.

**Server and saves:** nothing changes on the server, and nothing is saved. The flash, casings, smoke, zoom and pose are drawn by each client from what it already knows. The three casing particle types are registered on both sides, as particle types must be.

**Known limits:**
- The flash's timing comes from the shot, not from the animation, so a remote player's flash shows when their shot reaches you.
- Cases do not bounce or roll; they stop where they land.
- The zoom does not follow the field of view effects slider, as the spyglass's does not.

## Slice 7: bayonets and the shared-texture guns
Slice 7 finishes the attachments the owner modelled for these twelve guns, except the scopes.

**Bayonets** (the grip slot, now called "under-barrel": a gun has a bayonet or a grip, not both):

| Bayonet | Stab | Crafting |
|---|---|---|
| Iron Bayonet | 4 | an iron ingot over an iron nugget |
| Steel Bayonet | 5 | a steel ingot over an iron nugget |
| Diamond Bayonet | 5 | a diamond over an iron nugget |
| Netherite Bayonet | 6 | a Diamond Bayonet, a netherite ingot and a netherite upgrade template, at a smithing table |

- The owner's art for each: the item icon, and the blade on each gun that takes one. The owner's fourth bayonet is anthralite, a Scorched Guns metal Jugcraft does not have. Its grey blade and wrapped grip read as steel, so it is the Steel Bayonet (a proposed name).
- **Which guns:** the Patchwork Carbine, Thunderpipe, Longhorn Rifle, Drover Rifle, Coach Gun, Line Musket and Bellmouth take all four. The pistols, the SMG and the Haymaker have no bayonet parts.
- **Stabbing:** V (the "Stab with bayonet" key) thrusts the gun forward and back. The server strikes the nearest creature along the look within the player's reach, stopping at blocks:
  - for the bayonet's damage, as a melee blow, with a sword's push;
  - at most once every 12 ticks, and not while reloading;
  - past the same ally, mount and protection checks a bullet uses.
- **Others see it:** the server tells the clients that see the stabber (`GunActionPayload.STAB`); they thrust the stabber's arms forward.
- Bayonets change none of the gun's numbers. The owner's animations have no stab, so the thrust is drawn in code: the gun driven 0.35 blocks forward on screen and back over 6 ticks, and the arms 4 pixels forward seen from outside.

**The shared-texture guns:** the owner drew some guns' attachment parts on textures shared between guns. GeckoLib draws a model from one texture, so slice 5 left them out. `tools/guns.py` now builds each gun one atlas:
- the gun's own texture, plus the shared textures its parts use;
- packed into room the gun's own texture leaves free: clear blocks that no face's UVs and no prop reach (`atlas_layout()`, `own_footprint()`);
- a 64 px atlas grows to 128 to make room. Every atlas stays a square of 128 or less, the texture rule for packed textures.

| Gun | Now takes | Its atlas |
|---|---|---|
| Drover Rifle | Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel, Light Grip, the bayonets | 128, with `advanced_silencer`, `carabine_grips`, `greaser_smg_barrels` and `makeshift_rifle_bayonets` in its free room |
| Coach Gun | Light Grip, the bayonets | 128, with `carabine_grips` and `makeshift_rifle_bayonets` |
| Duelling Pistol | Light, Weighted and Wooden Stocks | 64 grown to 128, with `flintlock_stocks` |
| Line Musket | the three stocks, Light and Vertical Grips, the bayonets | 64 grown to 128, with `musket_bayonets`, `musket_stocks` and `musket_grips` |
| Bellmouth | Light and Vertical Grips, the bayonets | 64 grown to 128, with `musket_bayonets` and `musket_grips` |

- Each face's UVs are moved into the atlas from its own texture, and the face check compares them that way. Every attachment part re-bakes face for face as before.
- A new check holds the written atlas to its parts: the owner's files and the props, placed as the layout says, never over a pixel the gun's own texture uses.
- The Muzzle Brake's and Extended Barrel's flash comes from their front on the Drover too (slice 6's locators).

**Connections:** iron, steel (Jugcraft's), diamond and netherite, as their tools are; the Netherite Bayonet is an upgrade, as netherite tools are. A bayonet is a choice against a grip: a melee blow against less spread or kick.

**Balance:** a bayonet stab is weaker than a sword of its metal (a sword is 6 to 8), and a stab every 12 ticks. It is a last resort, not a melee weapon.

**Known limits:**
- **No scopes:**
  - The owner's scope models (reflex sight, medium and long scopes, laser sight) draw their lenses with reticle and vignette textures (`scguns:effect/...`). Those are not in the library.
  - Where a scope sits on each gun is not in the library either (Scorched Guns keeps it in gun data).
  - Scopes need those textures from the owner, or ones drawn here; and a mount point set on each gun.
- **The tactical grip:** the Drover's and Coach Gun's `tact_grip` parts are not an item yet.
- **The thrust is code, not an animation:** the owner may want to animate a stab.

## Connections
- **Existing input producers:** copper, iron and gunpowder (vanilla); lead nuggets (the lead switch's lead); brass nuggets (brass, from zinc); paper and planks.
- **Existing output consumer:** combat. The guns kill what drops loot for every branch.
- **Technology connection:** the rounds are made from the mod's lead and brass; the guns from early metals.
- **Magic connection:** none yet.
- **Reachable entry path:**
  - copper and iron need only mining and smelting; gunpowder comes from creepers;
  - lead and zinc ores are worldgen (their switches, on by default);
  - so every gun and round is reachable in early survival, and nothing circular gates them.
- **Required vs optional:** all optional; no other feature needs a gun.
- **Trade and solo routes:** solo, crafted. Trading rounds between players works as with any item.
- **How the specialty stays useful:** a strong ranged option that costs ammunition. Melee and bows stay free to use.

## Balance and automation
- **Damage per round:** Light 2, Rifle 6, Buckshot up to 20 at point blank.
  - A bow at full draw does about 6 to 10 with no ammunition cost beyond arrows.
  - The Midge's 13 damage a second, held, empties a 20-round magazine in 3 seconds for 40 damage, then 2.35 s of reloading.
- **Costs:**
  - 8 light rounds cost a copper ingot, a lead nugget and a gunpowder;
  - a rifle round costs a quarter of a brass nugget, a lead nugget and a gunpowder;
  - a buckshot shell a quarter of each, plus paper.
- **Metal accounting:** rounds and guns hold no metal units (`tools/check_mod_data.py`). Nothing turns them back into metal, so there is no conversion loop.
- **Server-side rate:** each player has a trigger credit refilling at one shot per interval, banking up to two (`GunShots.BURST`). A late packet does not lose a shot, and a fast client gains none.
- **Starting numbers,** not tuned in play. The owner may want higher-tier guns to outclass these clearly.

## Multiplayer and persistence
- **Server authority:** the client only asks, with `GunShotPayload` (one per shot, no data) and `GunReloadPayload` (none). The server (`guns/GunShots`):
  - checks the gun in the main hand, that the player is alive and not a spectator, the loaded rounds and the trigger credit;
  - computes every bullet from the player's server-side position and look;
  - spends rounds, takes them from the inventory and deals the damage.
- **Ally and protection checks:** a bullet skips allies, the player's own mount and marker stands. It skips anything other code refuses through `AttackEntityCallback`, such as the walled town's protection.
- **Prediction:** the client plays the shot, sound and kick at once for responsiveness. A refused shot is only a sound. The counter allows for shots not yet answered and writes them off after half a second.
- **Reloads:**
  - A reload is held on the server per player and stops if the gun leaves the main hand.
  - A magazine loads at the end of its time.
  - A shell-at-a-time reload loads one shell after each shell's time, and a shot cuts it short.
  - The player's own client times its animation from the same numbers.
- **Others' animations:** the server tells the clients that see the shooter (not the shooter's own) with `GunActionPayload`, and they play the shot or reload on that gun.
- **Persistence:** the rounds loaded are a data component on the gun, `jugcraft:loaded_rounds` (0 to 64). Its attachments are another, `jugcraft:attachments` (a list of up to four attachment ids, oldest first; an id no longer known is ignored). GeckoLib gives each gun a stable animation id the first time the server ticks it. Nothing else is saved.
- **Attachments and authority:** attachments are fitted only by the crafting recipes, which the server runs. The server reads a gun's numbers from its own copy of the stack (`GunItem.spec(stack)`), for the shot, the trigger rate, the reload and the rounds it may load; the client uses the same numbers only to predict.
- **Disconnect:** clears that player's trigger credit and reload.
- **Disable:** a new switch, `guns.enabled` (config `jugcraft.properties`), gates the guns', rounds' and attachments' recipes (twenty-nine with slice 5: sixteen guns and rounds, eleven attachments, and the fitting and removal recipes). Items and the attachments component stay registered, so saved guns, rounds and attachments survive with it off.

## The shared parts it uses
- **Items:** `JugcraftRegistry.item` for every gun, round and attachment; the Combat tab.
- **Recipes:** two special crafting recipes, `jugcraft:gun_attachment` and `jugcraft:gun_attachment_removal`, built as the Skeleton Key's copying is (`CustomRecipe`).
- **Config:** the `guns` feature switch (`JugcraftConfig.FEATURES`, `tools/materials.py`).
- **Damage type:** `jugcraft:bullet`, tagged `minecraft:is_projectile` (Projectile Protection works against it) and `minecraft:bypasses_cooldown` (each shot counts).
- **Sounds:** in `sounds.json`, through `tools/generate_material_data.py`.
- **Icons:** the item-icon maps (`tools/item_icons/`, `docs/ITEM_ICONS.md`) for the rounds.
- **Keys:** Jugcraft's key category, beside the party key.
- **Frameworks:** GeckoLib 5.5.7, already a required, approved dependency (`docs/FRAMEWORKS.md`); this is the first Jugcraft code to use it.
- **ArmsMotion** animates only Jugcraft's melee arms (`ArmItem`), and a gun is not one.
  - GeckoLib owns everything a gun does: the gun's parts and, in first person, the arms that hold it.
  - In third person (slice 6), `GunPose` raises the arms through the same two hooks that pose ArmsMotion's: `ArmsRenderStateMixin` and `ArmsHumanoidModelMixin`.
  - The off hand plays no part: guns fire from the main hand only.
- **Particles:** the casings are registered as the Fog Machine's fog is (`FabricParticleTypes.simple()`, a provider on the client).

## How the owner's models became GeckoLib models
The owner supplied each gun's parts as Blockbench Java item models (`Guns/models/special/<gun>/<part>.json`) and Bedrock animations made for GeckoLib models that were not in the upload. `tools/guns.py`:
1. **Builds one bone per animated part,** placing each of the owner's elements as one GeckoLib cube with the same corners, the same UV at each corner and the same turn about the same point:

   | Gun | Bones |
   |---|---|
   | Midge | `gun_body2` > `gun_body` (main); `bolt`, `barrels`, `magazine` (the standard magazine) |
   | Carbine | `gun_body` (main and standard barrel); `bolt`, `magazine`, and an empty `magazine_2` (its animations keep it at scale 0) |
   | Thunderpipe | `gun_body` (main and standard grip); `barrels`; an empty `bolt`; and `shell` |

   The Thunderpipe's reload moves a `shell` bone that had no part. It gets a 2 × 2 × 5 px shell, red paper on a brass head, drawn into an empty corner of the atlas copy. Its rest place is the breech less the loop's last offset, so the loop slides it home. Every other animation hides it except the inspect, which leaves it at that rest place, so the renderer draws it only while an animation moves it.
2. **Places the arm bones.** The animations move `right_arm` and `left_arm`, whose models were not supplied:
   - **Hierarchy:** each is a child of `gun_body`, so the hands follow the gun. This fit the Thunderpipe's shell-carrying left hand best of the arrangements tried (1.1 px, against 1.2 to 2.1).
   - **Pivot:** each pivot is the hand, placed so the idle pose's offsets bring it to the grip (right) and the fore-end or magazine (left).
   - **The arm:** each idle pose turns the arm bone so its -y points straight back at the camera, which showed the arms end-on as big slabs. So each arm runs instead toward a `<side>_shoulder` locator 10 px from the hand, down, back and out (`"arms"` in `BUILDS`, in the bone's own frame). The directions were chosen in a first-person preview of the idle pose, and the renderer turns the player's arm from -y onto the locator.
   - **Rest points (owner pixels):**
     - Midge: right hand (8, 2.2, 14.8), left (7.2, 0.6, 10.7);
     - Carbine: right (8, 1.2, 16.0), left (8, 2.8, 7.0);
     - Thunderpipe: right (8, 1.6, 17.0), left (8, 2.8, 9.5).
   - **Gun body:** turns about the right hand's grip.
3. **Copies the files unchanged:** the atlases (the Thunderpipe's with the shell added), the animations (byte for byte) and the sounds.
4. **Checks the result:** it re-bakes every model the way GeckoLib 5.5.7 does (`GeometryCube`, `VertexSet`, `GeometryQuadUvs`) and compares each face with the owner's. Run `python3 tools/guns.py --check`; `tools/check_mod_data.py` runs it too. It was shown to fail on a flipped UV, a moved cube and a turned face.

The animations' sound cues map to the library's sounds as follows:
- `gun_rustle` and `rustle`: gun_rustle
- `rack`, `bolt`, `bolt_pull`, `bolt_release`: the sound of the same name
- `reload_mag_out`, `reload_mag_in`: mag_out, mag_in (the Thunderpipe's `reload_mag_in` plays the shell insert)
- `slap`, `reload_end`: the sound of the same name

An empty click plays the Rusty Gnat's copper_jam. Each gun's shot is its own `fire.ogg`. Of the animations' particle cues, `eject_casing` throws a spent casing ([slice 6](#slice-6-the-guns-in-use)); the others show nothing.

## Dependencies and assets
- **No new dependencies.** GeckoLib 5.5.7 is already required (`distribution/frameworks.lock.json`).
- **The owner's files,** from the owner asset library (`art/owner-library/originals/Blocks/`). Their SHA-256 begins:

| Gun | Library file | SHA-256 (first 16) |
|---|---|---|
| rust_midge | `Guns/models/item/rusty_gnat.json` (display transforms) | `605fd78cc682e5fa` |
| rust_midge | `Guns/item/rusty_gnat.png` | `2cf1804a29b889d8` |
| rust_midge | `Guns/item/rusty_gnat.animation.json` | `188ed3e28158cc07` |
| rust_midge | `Guns/models/special/rusty_gnat/main.json` | `d00bacd6fbd29ef7` |
| rust_midge | `Guns/models/special/rusty_gnat/bolt.json` | `7a88e2dd891f3576` |
| rust_midge | `Guns/models/special/rusty_gnat/barrel.json` | `4fcaeeb5be426afc` |
| rust_midge | `Guns/models/special/rusty_gnat/stan_mag.json` | `f8f36fe847727c96` |
| rust_midge | `Guns/sounds/item/rusty_gnat/fire.ogg` | `02c2854e36a0f377` |
| patchwork_carbine | `Guns/models/item/makeshift_rifle.json` | `5a28bbf2e7e1bdcf` |
| patchwork_carbine | `Guns/item/makeshift_rifle.png` | `db7bd9ccf55c6a45` |
| patchwork_carbine | `Guns/item/makeshift_rifle.animation.json` | `1d9c970f64578bb7` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/main.json` | `a501f77774546816` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/stan_barrel.json` | `bcfeb9a40b33e5f7` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/bolt.json` | `c0b217fd56b381d0` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/stan_mag.json` | `a26c7b8753d94492` |
| patchwork_carbine | `Guns/sounds/item/makeshift_rifle/fire.ogg` | `9c4469e45b9b77e4` |
| thunderpipe | `Guns/models/item/boomstick.json` | `4c1a97aa58a16ed0` |
| thunderpipe | `Guns/item/boomstick.png` (copied with the shell added) | `d06afbd303f6c1e6` |
| thunderpipe | `Guns/item/boomstick.animation.json` | `933bbc2a481f37e1` |
| thunderpipe | `Guns/models/special/boomstick/main.json` | `26a760935188b36d` |
| thunderpipe | `Guns/models/special/boomstick/stan_grip.json` | `c1600f32e348032e` |
| thunderpipe | `Guns/models/special/boomstick/barrel.json` | `5cfbb08d0c0ed36f` |
| thunderpipe | `Guns/sounds/item/boomstick/fire.ogg` | `17a57973db433feb` |
| warden_pistol | `Guns/models/item/defender_pistol.json` | `bf3301680bc96f45` |
| warden_pistol | `Guns/item/defender_pistol.png` | `ad202f717aeba7ad` |
| warden_pistol | `Guns/item/defender_pistol.animation.json` | `12cfc09f3808047f` |
| warden_pistol | `Guns/models/special/defender_pistol/main.json` | `2876ee81c267a62b` |
| warden_pistol | `Guns/models/special/defender_pistol/stan_barrel.json` | `19ad2fd0bd8b90fb` |
| warden_pistol | `Guns/models/special/defender_pistol/receiver.json` | `36e10152e2d99dc9` |
| warden_pistol | `Guns/models/special/defender_pistol/stan_mag.json` | `7ab12d526d23f4cc` |
| warden_pistol | `Guns/sounds/item/iron_pistol/fire.ogg` | `6a0fe18dfc3165d5` |
| riveter_smg | `Guns/models/item/greaser_smg.json` | `8f0509099a336944` |
| riveter_smg | `Guns/item/greaser_smg.png` | `25c4edd154a19db3` |
| riveter_smg | `Guns/item/greaser_smg.animation.json` | `f3652168b8a19a97` |
| riveter_smg | `Guns/models/special/greaser_smg/main.json` | `18913c7642478b66` |
| riveter_smg | `Guns/models/special/greaser_smg/stan_barrel.json` | `71ade7f1ea1cb5e2` |
| riveter_smg | `Guns/models/special/greaser_smg/sights.json` | `8b2ddcda44cffbc1` |
| riveter_smg | `Guns/models/special/greaser_smg/bolt.json` | `55f84a9152c3883b` |
| riveter_smg | `Guns/models/special/greaser_smg/stan_mag.json` | `0d2d5d33f5974d07` |
| riveter_smg | `Guns/sounds/item/greaser_smg/fire.ogg` | `201fd4258dea84a2` |
| haymaker | `Guns/models/item/bruiser.json` | `26242eca95ba17df` |
| haymaker | `Guns/item/bruiser.png` | `3bd5c461326585b5` |
| haymaker | `Guns/item/bruiser.animation.json` | `2e58fe5e8cf7fa97` |
| haymaker | `Guns/models/special/bruiser/main.json` | `3d2bb3bdad310541` |
| haymaker | `Guns/models/special/bruiser/barrel.json` | `ea98ae5f5582c0ce` |
| haymaker | `Guns/sounds/item/bruiser/fire.ogg` | `b965c68e659f88e3` |
| longhorn_rifle | `Guns/models/item/marlin.json` | `e55dbcff4031e78b` |
| longhorn_rifle | `Guns/item/marlin.png` | `103b847aa693002d` |
| longhorn_rifle | `Guns/item/marlin.animation.json` | `142ef222c32d4677` |
| longhorn_rifle | `Guns/models/special/marlin/main.json` | `c6ad3d720abbf099` |
| longhorn_rifle | `Guns/models/special/marlin/stan_barrel.json` | `6fb89e7f959d1014` |
| longhorn_rifle | `Guns/models/special/marlin/sights.json` | `8f5fcc8e50769101` |
| longhorn_rifle | `Guns/models/special/marlin/bolt.json` | `eff14fef76793cd0` |
| longhorn_rifle | `Guns/sounds/item/heavy_rifle/fire.ogg` | `389a087108dc1565` |
| drover_rifle | `Guns/models/item/winnie.json` | `19ff6562ae558f77` |
| drover_rifle | `Guns/item/winnie.png` | `8f41f0400c45d5d1` |
| drover_rifle | `Guns/item/winnie.animation.json` | `949f1214d786712c` |
| drover_rifle | `Guns/models/special/winnie/main.json` | `ff44e4d5052b6601` |
| drover_rifle | `Guns/models/special/winnie/stan_barrel.json` | `73e379d0f13ab28e` |
| drover_rifle | `Guns/models/special/winnie/sights.json` | `7e42e8d0e4b10629` |
| drover_rifle | `Guns/models/special/winnie/hammer.json` | `d92ff90ae9bcb8e2` |
| drover_rifle | `Guns/sounds/item/cowboy/fire.ogg` | `27ab9051ec48ba94` |
| coach_gun | `Guns/models/item/callwell.json` | `4bffc3664e0333c3` |
| coach_gun | `Guns/item/callwell.png` | `6c7198a2cc8a4f19` |
| coach_gun | `Guns/item/callwell.animation.json` | `ab04e81b8490abec` |
| coach_gun | `Guns/models/special/callwell/main.json` | `1c1681b7bdadd76f` |
| coach_gun | `Guns/sounds/item/brass_shotgun/fire.ogg` | `95eee6d27b87d35a` |
| duelling_pistol | `Guns/models/item/flintlock_pistol.json` | `2d67c5d0470be932` |
| duelling_pistol | `Guns/item/flintlock_pistol.png` | `3217c9593149ede6` |
| duelling_pistol | `Guns/item/flintlock_pistol.animation.json` | `61b3660ac8829525` |
| duelling_pistol | `Guns/models/special/flintlock_pistol/main.json` | `1d94ca1c4f73eae0` |
| duelling_pistol | `Guns/models/special/flintlock_pistol/hammer.json` | `8bf431167c8b7557` |
| duelling_pistol | `Guns/sounds/item/blackpowder/fire.ogg` | `a3d3d49a332d034f` |
| line_musket | `Guns/models/item/musket.json` | `e6231ba72c95949a` |
| line_musket | `Guns/item/musket.png` | `2e80294309d87927` |
| line_musket | `Guns/item/musket.animation.json` | `3de10c7807e5940c` |
| line_musket | `Guns/models/special/musket/main.json` | `2e24d5f0620e9051` |
| line_musket | `Guns/models/special/musket/hammer.json` | `059525f269d8120c` |
| line_musket | `Guns/models/special/musket/flash.json` | `7a6ab5acaca6669f` |
| line_musket | `Guns/sounds/item/blackpowder/fire.ogg` | `a3d3d49a332d034f` |
| bellmouth | `Guns/models/item/blunderbuss.json` | `7d99e9a7cedc85b3` |
| bellmouth | `Guns/item/blunderbuss.png` | `4e90eca70ba48802` |
| bellmouth | `Guns/item/blunderbuss.animation.json` | `82acaf92b7654690` |
| bellmouth | `Guns/models/special/blunderbuss/main.json` | `611befd4a9d8e37d` |
| bellmouth | `Guns/models/special/blunderbuss/hammer.json` | `9365a5b0b9c90007` |
| bellmouth | `Guns/sounds/item/blackpowder/fire.ogg` | `a3d3d49a332d034f` |
| shared | `Guns/sounds/item/bolt/bolt.ogg` | `1cf1102f6ba52725` |
| shared | `Guns/sounds/item/bolt_pull/bolt_pull.ogg` | `dbbda8b00abcab8c` |
| shared | `Guns/sounds/item/bolt_release/bolt_release.ogg` | `7c1096f545d72ec3` |
| shared | `Guns/sounds/item/gun_rustle/gun_rustle.ogg` | `aeec657cb4c25acd` |
| shared | `Guns/sounds/item/gun_sounds/insert.ogg` | `cbc0479276e5262c` |
| shared | `Guns/sounds/item/lever/lever.ogg` | `0b6c3fb22142e42e` |
| shared | `Guns/sounds/item/mag_in/mag_in.ogg` | `9595cc14d1209f85` |
| shared | `Guns/sounds/item/mag_out/mag_out.ogg` | `5f805eaadc8fd476` |
| shared | `Guns/sounds/item/gun_sounds/metal.ogg` | `3c48586e2406bdea` |
| shared | `Guns/sounds/item/rack/rack.ogg` | `aa98a804ed7809ab` |
| shared | `Guns/sounds/item/reload_end/reload_end.ogg` | `c1db5357e55ed953` |
| shared | `Guns/sounds/item/rusty_gnat/copper_jam.ogg` | `d1b93136045c83cc` |
| shared | `Guns/sounds/item/slap/slap.ogg` | `ed6fbb36974a444a` |

Slice 5's files: each gun's attachment parts, and each attachment's item model and texture (copied to `textures/item/guns/attachments/<name>.png`):

| Gun or attachment | Library file | SHA-256 (first 16) |
|---|---|---|
| rust_midge | `Guns/models/special/rusty_gnat/stock_light.json` | `f89f9862600c31ef` |
| rust_midge | `Guns/models/special/rusty_gnat/stock_weighted.json` | `a0339d40d9cc62cf` |
| rust_midge | `Guns/models/special/rusty_gnat/stock_wooden.json` | `8925d4a0861f75a2` |
| rust_midge | `Guns/models/special/rusty_gnat/silencer.json` | `7992f7b7c39283d9` |
| rust_midge | `Guns/models/special/rusty_gnat/advanced_silencer.json` | `4f8e878fa1f24ff2` |
| rust_midge | `Guns/models/special/rusty_gnat/muzzle_brake.json` | `ebd59126a5057c32` |
| rust_midge | `Guns/models/special/rusty_gnat/ext_barrel.json` | `0fa9b18a2b500014` |
| rust_midge | `Guns/models/special/rusty_gnat/ext_mag.json` | `d3fb272f16cdba67` |
| rust_midge | `Guns/models/special/rusty_gnat/speed_mag.json` | `6c1e0d6dbb380fc6` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/silencer.json` | `dffc0edb1e71755e` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/advanced_silencer.json` | `91afc30e748e26b0` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/muzzle_brake.json` | `f71cac197c938957` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/ext_barrel.json` | `c77d09365b3319fc` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/stock_light.json` | `dac577681811ba5e` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/stock_weighted.json` | `9015f21f7e8a80a0` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/stock_wooden.json` | `c2fe96d4aaa65326` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/light_grip.json` | `d4ca3eaba519db7c` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/vertical_grip.json` | `4745ab80863d07a0` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/ext_mag.json` | `79e5c531276a42f6` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/speed_mag.json` | `223c1ed7dde033ce` |
| thunderpipe | `Guns/models/special/boomstick/stock_light.json` | `741f67115e17eb74` |
| thunderpipe | `Guns/models/special/boomstick/stock_weighted.json` | `d2b1bde81916a3ea` |
| thunderpipe | `Guns/models/special/boomstick/stock_wooden.json` | `17478321875df40d` |
| thunderpipe | `Guns/models/special/boomstick/grip_light.json` | `69354d23e651023f` |
| thunderpipe | `Guns/models/special/boomstick/grip_vertical.json` | `606dfa6f76926cd6` |
| thunderpipe | `Guns/models/special/boomstick/silencer.json` | `817c4466213b999c` |
| thunderpipe | `Guns/models/special/boomstick/advanced_silencer.json` | `f63167ba66948f9d` |
| thunderpipe | `Guns/models/special/boomstick/muzzle_brake.json` | `843ea4ed4bebdece` |
| thunderpipe | `Guns/models/special/boomstick/ext_barrel.json` | `dab35bd7189c1fc2` |
| warden_pistol | `Guns/models/special/defender_pistol/silencer.json` | `7a8d0cfd65d78b73` |
| warden_pistol | `Guns/models/special/defender_pistol/advanced_silencer.json` | `ca7ee9d5a5f47d8e` |
| warden_pistol | `Guns/models/special/defender_pistol/muzzle_brake.json` | `feb08e673e6f0537` |
| warden_pistol | `Guns/models/special/defender_pistol/ext_barrel.json` | `5f915c9b732d162b` |
| warden_pistol | `Guns/models/special/defender_pistol/ext_mag.json` | `b04afe2b90af3acd` |
| warden_pistol | `Guns/models/special/defender_pistol/speed_mag.json` | `0b48d5f83ddb9d4f` |
| riveter_smg | `Guns/models/special/greaser_smg/silencer.json` | `ae24d4720cdab105` |
| riveter_smg | `Guns/models/special/greaser_smg/advanced_silencer.json` | `3eb2f44c0d7119d1` |
| riveter_smg | `Guns/models/special/greaser_smg/muzzle_brake.json` | `ef6827ac9edbf547` |
| riveter_smg | `Guns/models/special/greaser_smg/ext_barrel.json` | `25b9012c673d7378` |
| riveter_smg | `Guns/models/special/greaser_smg/light_stock.json` | `985e1ffda3e8b38c` |
| riveter_smg | `Guns/models/special/greaser_smg/heavy_stock.json` | `77f98977d10dfb6e` |
| riveter_smg | `Guns/models/special/greaser_smg/wooden_stock.json` | `d09f18d87edd8dd8` |
| riveter_smg | `Guns/models/special/greaser_smg/ext_mag.json` | `479107b02452fd12` |
| riveter_smg | `Guns/models/special/greaser_smg/speed_mag.json` | `d942ea99e194948b` |
| haymaker | `Guns/models/special/bruiser/silencer.json` | `d7fa4a7a73675bd0` |
| haymaker | `Guns/models/special/bruiser/advanced_silencer.json` | `2fedfc9db6cb22d1` |
| haymaker | `Guns/models/special/bruiser/muzzle_brake.json` | `598d5dde49e75d04` |
| haymaker | `Guns/models/special/bruiser/ext_barrel.json` | `569b881c2d9d7d1b` |
| longhorn_rifle | `Guns/models/special/marlin/silencer.json` | `06bf50eaea127309` |
| longhorn_rifle | `Guns/models/special/marlin/advanced_silencer.json` | `4b7fe982ca0d6f0f` |
| longhorn_rifle | `Guns/models/special/marlin/muzzle_brake.json` | `ebeda0ce9770bfb1` |
| longhorn_rifle | `Guns/models/special/marlin/ext_barrel.json` | `b15a641d1ee802e9` |
| longhorn_rifle | `Guns/models/special/marlin/light_stock.json` | `35101c5b5528e6c1` |
| longhorn_rifle | `Guns/models/special/marlin/heavy_stock.json` | `581c80a1bd6b41f1` |
| longhorn_rifle | `Guns/models/special/marlin/wooden_stock.json` | `2af1dbde0f745927` |
| longhorn_rifle | `Guns/models/special/marlin/light_grip.json` | `266eff6b2f577212` |
| longhorn_rifle | `Guns/models/special/marlin/vert_grip.json` | `9179d93c68a1ff68` |
| silencer | `Guns/models/item/silencer.json` | `049d959e9534d047` |
| texture `baffled_silencer` | `Guns/models/item/advanced_silencer.json` | `4c139fb12152d923` |
| muzzle_brake | `Guns/models/item/muzzle_brake.json` | `47dff9ea7cd62512` |
| texture `extended_barrel` | `Guns/models/item/extended_barrel.json` | `c5d63e72f9e28a5f` |
| texture `extended_magazine` | `Guns/models/item/extended_mag.json` | `ef32296bd1f9a1cc` |
| texture `speed_magazine` | `Guns/models/item/speed_mag.json` | `990831d677e8b769` |
| texture `light_stock` | `Guns/models/item/light_stock.json` | `709b176621240c73` |
| texture `weighted_stock` | `Guns/models/item/weighted_stock.json` | `1e0aa4edf441a53d` |
| texture `wooden_stock` | `Guns/models/item/wooden_stock.json` | `dceb43555135ec17` |
| light_grip | `Guns/models/item/light_grip.json` | `8965dcdf94effedb` |
| vertical_grip | `Guns/models/item/vertical_grip.json` | `31667ebc84eaa55d` |
| texture `muzzle_devices` | `Guns/item/greaser_smg_barrels.png` | `e912ec7c95188909` |
| texture `baffled_silencer` | `Guns/item/advanced_silencer.png` | `f94615d5c4cdd84c` |
| texture `extended_barrel` | `Guns/item/extended_barrel.png` | `2fc5027a3f329c25` |
| texture `extended_magazine` | `Guns/item/extended_mag.png` | `0b34bd0828783d13` |
| texture `speed_magazine` | `Guns/item/carabine.png` | `305297d9327603b1` |
| texture `light_stock` | `Guns/item/light_stock.png` | `2b4a27580b4871ae` |
| texture `weighted_stock` | `Guns/item/greaser_smg_stocks.png` | `247deccc5517be15` |
| texture `wooden_stock` | `Guns/item/musket_stocks.png` | `848fb17c2a71ad8d` |
| texture `grips` | `Guns/item/carabine_grips.png` | `e54eb16b4a01aa0b` |
| light_round_casing | `Guns/item/small_copper_casing.png` | `534d4d0375da46cc` |
| rifle_round_casing | `Guns/item/large_brass_casing.png` | `2b43b9961479bdbb` |
| buckshot_shell_casing | `Guns/item/shotgun_shell.png` | `89657710d20d041d` |
| flash_0 | `Big Cannons and Mounted Guns/textures/muzzleflash.png` | `9e6b36b22790ec34` |
| flash_1 | `Big Cannons and Mounted Guns/textures/muzzleflash2.png` | `31029be6554ebd9f` |
| flash_2 | `Big Cannons and Mounted Guns/textures/muzzleflash3.png` | `5c695fc1d9c5b365` |
| flash_3 | `Big Cannons and Mounted Guns/textures/muzzleflash4.png` | `189ef124242eaa98` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/iron_bayonet.json` | `fd7d5b1b45d681a4` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/anthralite_bayonet.json` | `815aea98eeb13043` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/diamond_bayonet.json` | `46735b82875edf0e` |
| patchwork_carbine | `Guns/models/special/makeshift_rifle/netherite_bayonet.json` | `5ffc2005b15bff2e` |
| thunderpipe | `Guns/models/special/boomstick/iron_bayonet.json` | `dedc0f2f291b357f` |
| thunderpipe | `Guns/models/special/boomstick/anthralite_bayonet.json` | `58e120d3a0dc3692` |
| thunderpipe | `Guns/models/special/boomstick/diamond_bayonet.json` | `f093c45484d3a732` |
| thunderpipe | `Guns/models/special/boomstick/netherite_bayonet.json` | `52793ba001a50581` |
| longhorn_rifle | `Guns/models/special/marlin/iron_bayonet.json` | `5ea1cf38b1cae83b` |
| longhorn_rifle | `Guns/models/special/marlin/anthralite_bayonet.json` | `114add26956515e8` |
| longhorn_rifle | `Guns/models/special/marlin/diamond_bayonet.json` | `ada6f62d7bd00777` |
| longhorn_rifle | `Guns/models/special/marlin/netherite_bayonet.json` | `6e046c9c441e1c77` |
| drover_rifle | `Guns/models/special/winnie/silencer.json` | `46a106c68f3b9acc` |
| drover_rifle | `Guns/models/special/winnie/advanced_silencer.json` | `0ba9ea154fd1c29e` |
| drover_rifle | `Guns/models/special/winnie/muzzle_brake.json` | `35232221bcd934fd` |
| drover_rifle | `Guns/models/special/winnie/ext_barrel.json` | `c3102b0d3a24de87` |
| drover_rifle | `Guns/models/special/winnie/light_grip.json` | `2c40e0b3c5c89703` |
| drover_rifle | `Guns/models/special/winnie/iron_bayonet.json` | `25131e44a4015957` |
| drover_rifle | `Guns/models/special/winnie/anthralite_bayonet.json` | `689d50f88cc9bfd1` |
| drover_rifle | `Guns/models/special/winnie/diamond_bayonet.json` | `267d49add46a2579` |
| drover_rifle | `Guns/models/special/winnie/netherite_bayonet.json` | `f102098b23e5dff6` |
| drover_rifle | `Guns/item/advanced_silencer.png` | `f94615d5c4cdd84c` |
| drover_rifle | `Guns/item/carabine_grips.png` | `e54eb16b4a01aa0b` |
| drover_rifle | `Guns/item/greaser_smg_barrels.png` | `e912ec7c95188909` |
| drover_rifle | `Guns/item/makeshift_rifle_bayonets.png` | `dfc93f5032504095` |
| coach_gun | `Guns/models/special/callwell/light_grip.json` | `1d9cf66979d3b6c6` |
| coach_gun | `Guns/models/special/callwell/iron_bayonet.json` | `0f47d60920ffe6cd` |
| coach_gun | `Guns/models/special/callwell/anthralite_bayonet.json` | `87f6acec33f5554b` |
| coach_gun | `Guns/models/special/callwell/diamond_bayonet.json` | `ad1f37ed70be7c3f` |
| coach_gun | `Guns/models/special/callwell/netherite_bayonet.json` | `83ba23f2e811940b` |
| coach_gun | `Guns/item/carabine_grips.png` | `e54eb16b4a01aa0b` |
| coach_gun | `Guns/item/makeshift_rifle_bayonets.png` | `dfc93f5032504095` |
| duelling_pistol | `Guns/models/special/flintlock_pistol/light_stock.json` | `024de83c7d6232ed` |
| duelling_pistol | `Guns/models/special/flintlock_pistol/heavy_stock.json` | `d478328ccbef3984` |
| duelling_pistol | `Guns/models/special/flintlock_pistol/wooden_stock.json` | `0cf64a73c5e531d2` |
| duelling_pistol | `Guns/item/flintlock_stocks.png` | `f08f21f3c71aafed` |
| line_musket | `Guns/models/special/musket/light_stock.json` | `a608540600bd9c78` |
| line_musket | `Guns/models/special/musket/heavy_stock.json` | `3d715a98ab3195fd` |
| line_musket | `Guns/models/special/musket/wooden_stock.json` | `cafc11238b17b3d3` |
| line_musket | `Guns/models/special/musket/light_grip.json` | `eb4cf964a249f4fc` |
| line_musket | `Guns/models/special/musket/vert_grip.json` | `3a8ebd80047712e9` |
| line_musket | `Guns/models/special/musket/iron_bayonet.json` | `d723788a1b9f6e99` |
| line_musket | `Guns/models/special/musket/anthralite_bayonet.json` | `1eaccdbb7a9b5fbd` |
| line_musket | `Guns/models/special/musket/diamond_bayonet.json` | `291bb6a540371af6` |
| line_musket | `Guns/models/special/musket/netherite_bayonet.json` | `267a94385517eb04` |
| line_musket | `Guns/item/musket_bayonets.png` | `0830d15655ef543b` |
| line_musket | `Guns/item/musket_stocks.png` | `848fb17c2a71ad8d` |
| line_musket | `Guns/item/musket_grips.png` | `7f451a500ba89baa` |
| bellmouth | `Guns/models/special/blunderbuss/light_grip.json` | `9b57e017c6ec6be9` |
| bellmouth | `Guns/models/special/blunderbuss/vert_grip.json` | `1c12cccb8864994d` |
| bellmouth | `Guns/models/special/blunderbuss/iron_bayonet.json` | `486b32647347a744` |
| bellmouth | `Guns/models/special/blunderbuss/anthralite_bayonet.json` | `51b6c59e0ab545bb` |
| bellmouth | `Guns/models/special/blunderbuss/diamond_bayonet.json` | `289974a1f5a20182` |
| bellmouth | `Guns/models/special/blunderbuss/netherite_bayonet.json` | `b69269d8a63c8f4e` |
| bellmouth | `Guns/item/musket_bayonets.png` | `0830d15655ef543b` |
| bellmouth | `Guns/item/musket_grips.png` | `7f451a500ba89baa` |
| iron_bayonet | `Guns/models/item/iron_bayonet.json` | `6daa8ca0d9223b8e` |
| steel_bayonet | `Guns/models/item/anthralite_bayonet.json` | `c8e0836f6f6e3098` |
| diamond_bayonet | `Guns/models/item/diamond_bayonet.json` | `fcc0759915935a9a` |
| netherite_bayonet | `Guns/models/item/netherite_bayonet.json` | `89e257ea581a1a6e` |
| iron_bayonet | `Guns/item/iron_bayonet.png` | `b3bc20be7702ac39` |
| steel_bayonet | `Guns/item/anthralite_bayonet.png` | `a9d69f394988fc4f` |
| diamond_bayonet | `Guns/item/diamond_bayonet.png` | `2262785d6c0da5d0` |
| netherite_bayonet | `Guns/item/netherite_bayonet.png` | `f7a49396eac13816` |

- **The bolt sound's tag:** `bolt.ogg` carries Vorbis tags naming another source ("All Epic Infantry Assault Rifle Reload Sounds (Fortnite)"). The owner, asked about the eight tagged sounds in the library, answered "I have the rights", so it is used like the rest. None of the other sounds used here carries such a tag.
- **Names:** Jugcraft's own, under the license policy's fan-homage rules. No `scguns:` reference reaches the game: the converter writes its own files with `jugcraft:` paths, and the animations name no resources.
- **Drawn here:** the round icons, as maps (`tools/item_icons/light_round.txt`, `rifle_round.txt`, `buckshot_shell.txt`, `paper_cartridge.txt`), and the props' pixels in the atlas copies (the Thunderpipe's shell, the Longhorn's cartridge, the muzzle-loaders' balls, ramrods and flashes).

## Verification
- **Run locally (7 October 2026):**
  - `python3 tools/guns.py`: writes the GeckoLib files, then PASS on the check. Every face of the 3 guns' parts comes back at the same corners with the same UVs and turns.
  - `python3 tools/generate_material_data.py`: run twice; the second run changed nothing.
  - `python3 tools/check_mod_data.py`: PASS (1551 material IDs), with `check_guns`.
  - `python3 scripts/check_repository.py`, `python3 tools/check_icon_maps.py`: PASS. The icon checker warns that the Light Round and the Buckshot Shell are a little shorter (7 and 8 steps) than the "short" family's 9 to 12. They are small rounds.
  - **Java:** a syntax parse only: 0 errors in the changed files.
- **Game tests (written; they run in CI):**
  - `GunsGameTests` (server): every gun and round registered; a carbine shot hurts its target by 6 and spends a round; an empty gun and an empty hand do not fire; the trigger rate holds and two quick shots both land; creative spends nothing; a magazine reload loads from the inventory after its time and not before; no reload without rounds or room; a shell reload loads one at a time; a shot cuts a shell reload short; switching away stops a reload; pellets land together; bullets are projectiles.
  - `GunsClientGameTests` (client): end to end through the real keys, for each gun:
    - draw, aim, fire at a husk (the server lands it and spends a round), reload part way and done (one round from the inventory), inspect;
    - the Thunderpipe's shell reload part way; each gun in third person; the inventory.
    - Screenshots `jugcraft_guns_*`.
- **CI on `33823d0c0` (Build run 37651397033):** every job passed: the mod build with the server game tests, the build without the optional integrations, and the client test.
  - The client test's log: the Midge took the husk from 200 to 198 health and spent a round; the Carbine took it from 198 to 192; the Thunderpipe from 192 to 174.5 (7 of its 8 pellets). Each reload then loaded one round and left 31 in the inventory.
  - **Screenshots read:** the guns in the owner's textures; your arms rising from the bottom of the screen to the grip and fore-end; the sight on the crosshair when aiming; the reloads and inspects; each gun in the right hand in third person; the guns and rounds in the inventory.
  - **Fixed after reading them:** the Thunderpipe's spare shell showed, out of place, during its inspect; and the counter read one round low until the next shot settled it (it now settles each answer as it comes). Their run is in the pull request.
- **Slice 2, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS. Every face of the six guns' parts comes back at the same corners with the same UVs and turns; each hand is where `BUILDS` puts it in its pose; each shoulder locator is 10 px from its hand, below it.
  - `python3 tools/generate_material_data.py`: run twice; the second run changed nothing.
  - `python3 tools/check_mod_data.py`: PASS (1554 material IDs), with `check_guns`; `python3 scripts/check_repository.py`: PASS.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - **First-person previews** of each new gun held, aimed and part way through its reload, drawn with the game's hand transforms; and the hand-to-part distances above.
- **Slice 2 game tests (written; they run in CI):**
  - `GunsGameTests` adds: the Warden Pistol and the Riveter SMG each land one shot's damage and spend a round; the Haymaker loads a shell at a time, stops at the two shells there are, then its pellets land together. "Every gun registered" now expects six guns.
  - `GunsClientGameTests` runs every gun, so the new three are drawn, aimed, fired at the husk, reloaded and inspected through the real keys too.
- **Slice 3, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS for all nine guns, the lever loops and the Callwell's barrels included (each element on its bone re-bakes to the owner's).
  - `python3 tools/generate_material_data.py`: run twice; the second run changed nothing.
  - `python3 tools/check_mod_data.py`: PASS (1557 material IDs), with `check_guns`; `python3 scripts/check_repository.py`: PASS.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - **Previews:** side and top views of the levers swinging and the Coach Gun breaking open; first-person views held, aimed and reloading, which led to the lowered right hands. The preview now clips what crosses the near plane, as the game does, instead of dropping it.
- **Slice 3 game tests (written; they run in CI):** `GunsGameTests` adds: the Longhorn and the Drover each land one shot's damage and spend a round, and the Drover loads three rounds one at a time; the Coach Gun's pellets land together and spend one barrel. "Every gun registered" expects nine guns. The client test runs every gun.
- **Slice 4, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS for all twelve guns (the hammers re-bake to the owner's parts; the drawn props are skipped by the face check and drawn into empty corners only).
  - `python3 tools/generate_material_data.py` twice (the second run changed nothing) and `python3 tools/generate_textures.py` for the cartridge's icon. That generator also rewrote 24 unrelated flower textures; those were left out of the change.
  - `python3 tools/check_mod_data.py`: PASS (1561 material IDs), with `check_guns`; `python3 scripts/check_repository.py` and `python3 tools/check_icon_maps.py`: PASS.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - **Previews:** side and top views of the shots (hammer, flash) and the reloads (ball, ramrod); first-person views held, aimed and through the Duelling Pistol's reload, which found the known limit above and shrank the flash from 2 px to 1.
- **Slice 4 game tests (written; they run in CI):** `GunsGameTests` adds: the Line Musket lands its 14, will not fire again empty, and loads one cartridge after its reload and not before; the Bellmouth's balls land together at close range. "Every gun registered" expects twelve guns and four rounds. The client test runs every gun; its husk now has 1000 health, enough for all twelve, and cannot be knocked back, and the player's aim is set afresh before each gun (on the first CI run the eleven shots before it had kicked the view up over the husk's head, so the Line Musket's shot passed over it).
- **Slice 5, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS for all twelve guns with their attachment bones: every attachment part re-bakes to the owner's, face for face, and no prop's atlas corner overlaps a part's texture.
  - `python3 tools/generate_material_data.py`: run twice; the second run changed nothing.
  - `python3 tools/check_mod_data.py`: PASS (1572 material IDs), with `check_guns`, which now also holds `JugcraftGuns.ATTACHMENTS`, `SLOTS` and `ACCEPTS` to `tools/guns.py`. Changing one attachment's number or one gun's list in the Java made it fail, as it should.
  - `python3 scripts/check_repository.py`: PASS. A check for recipes sharing a pattern found none for the eleven new ones.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - **Previews:** side views of the seven guns bare and with four sets of attachments fitted, drawn from the converted models; each attachment sits where the owner modelled it, and the replaced barrels, magazines and the Thunderpipe's grip are hidden.
- **Slice 5 game tests (written; they run in CI):**
  - `GunsGameTests` adds: attachments fit and come off through the crafting recipes (an Extended Magazine gives the Rust Midge 30 rounds and a 54-tick reload; a Speed Magazine replaces it, leaving it in the grid, for a 31-tick reload; a Silencer fits beside it at 35% volume and 95% damage; the Thunderpipe refuses a magazine, a gun a second Silencer, shears a bare gun; shears take the Silencer off and stay); an Extended Magazine loads 30 rounds after its longer reload and not in the Midge's own time, and with it taken off the gun keeps and fires them but will not reload; a silenced shot lands 95% of the damage. "Every gun registered" checks the eleven attachments.
  - `GunsClientGameTests` adds each gun that takes attachments held and aimed with two sets fitted, checks that the client sees a fitted Extended Magazine's 30 rounds, and shows the attachments and a fitted Patchwork Carbine in the inventory.
- **Slice 6, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS. The new locators changed the seven models with barrel attachments and nothing else. The casing and flash textures are the owner's files unchanged, and every particle cue in the animations is one the guns know.
  - `python3 tools/generate_material_data.py`: it added only the three casing particle definitions.
  - `python3 tools/check_mod_data.py`: PASS (1572 material IDs), with `check_guns`, which now also holds `GunLooks` and `JugcraftGuns.CASING_AMMO` to `tools/guns.py` and checks the hooks are called. `python3 scripts/check_repository.py`: PASS.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - Not seen yet: the flash, the casings and the pose in the game. The CI screenshots are the first look.
- **Slice 6 game tests (written; they run in CI):** `GunsClientGameTests`, for every gun:
  - aimed, the field of view modifier is narrowed (or the test says the mixin never ran);
  - fired, at least one muzzle flash frame is drawn;
  - fired and reloaded, a casing (or a puff) is thrown if its animation cues one;
  - in third person, each gun is held and fired (screenshots `_third_person` and `_third_person_fired`), and the player is posed holding it; the Longhorn and the Warden Pistol are also shown from behind.
- **Slice 7, run locally (7 October 2026):**
  - `python3 tools/guns.py`: PASS. Every face of every attachment part, the bayonets and the five guns' shared-texture parts included, re-bakes to the owner's through the gun's atlas. The atlases are the owner's files placed as the layout says, over no pixel the gun's own texture uses.
  - `python3 tools/generate_material_data.py`: the four bayonets' items, models, recipes and names.
  - `python3 tools/check_mod_data.py`: PASS (1576 material IDs). Its texture rule first failed the atlases at 256 × 128, which led to packing into free room. `python3 scripts/check_repository.py`: PASS.
  - **Java:** a syntax parse only: 0 errors in the changed files.
  - **Previews:** side views of the five guns bare and with each kind of attachment, and of the bayonets on four guns, drawn from the converted models. The bayonet blades are one-sided planes (GeckoLib draws them from both sides).
- **Slice 7 game tests (written; they run in CI):**
  - `GunsGameTests` adds:
    - a Patchwork Carbine's Iron Bayonet strikes a pig two blocks ahead for 4, and cannot stab again at once;
    - a bare carbine cannot stab;
    - a bayonet does not reach a pig seven blocks off;
    - the Drover Rifle, Line Musket, Coach Gun, Duelling Pistol and Bellmouth each take a shared-texture attachment through the crafting recipe;
    - the Netherite Bayonet's smithing recipe loads;
    - "Every gun registered" checks the fifteen attachments.
  - `GunsClientGameTests` adds a third fitted set (Muzzle Brake, Wooden Stock, Iron Bayonet) on every gun that takes attachments (now all twelve), and a Steel Bayonet stab with the V key that hurts the husk.
- **Not run:** the client by hand, a two-client dedicated server, and play.

## World and event applicability
Not applicable: no worldgen, loot, structures, bosses or seasonal content. Guns and rounds come only from crafting (and the creative tab).

## Rollout and open questions
- **Names:** proposed here (Rust Midge, Patchwork Carbine, Thunderpipe, Warden Pistol, Riveter SMG, Haymaker, Longhorn Rifle, Drover Rifle, Coach Gun, Duelling Pistol, Line Musket, Bellmouth, Light Round, Rifle Round, Buckshot Shell, Paper Cartridge; the attachments keep plain names: Silencer, Baffled Silencer, Muzzle Brake, Extended Barrel, Extended Magazine, Speed Magazine, Light Stock, Weighted Stock, Wooden Stock, Light Grip, Vertical Grip). The owner may rename them before release; IDs are stable only after release.
- **The arms:** placed from the animations' own evidence, without the models they were made for. The CI screenshots show where they sit; the rest points and arm directions above are the knobs.
- **Later slices,** each its own pull request: the tactical grip and scopes (scopes wait for the owner's reticle and vignette textures, or leave to draw them here).
- **Not yet:**
  - the jam the Gnat's sound suggests;
  - off-hand guns;
  - mob use;
  - the guns beyond these sets.
- **Balance:** the numbers are starting points for the owner to set.
