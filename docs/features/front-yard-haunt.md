# Front-Yard Haunt Scenes

Status: part 1 (the path: the Haunted Mailbox and the Pumpkin Path Light) implemented. CI builds it and runs its game tests and client screenshots; not yet played by hand. Parts 2 to 5 are planned (below).
Proposal issue: none. The owner asked for it directly on 28 October 2026, as set 7 of the ten sets of fall and Halloween decor ("Then I want you to do 1-10 in parts in that order one by one"), drawn by their rules.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery tier; the mailbox is made from iron nuggets, a spider eye, red dye and a wooden fence, and two path lights from iron bars and a jack-o'-lantern.
Primary specialty and supported player role: building. A builder dresses the front of a house for Halloween night, from the street to the door. Nothing in it is needed to progress.

## Player experience
The front yard after dark: what a trick-or-treater passes on the way to the door.

### Part 1: the path
- **Haunted Mailbox** (one block across, two up): a rural mailbox of the large size, 31 cm wide, 34 cm high and 62 cm long, at real height on its post.
  - It is black sheet steel under an arched top, with an orange reflector strip down each flank, on a weathered wooden post with a brace up to its arm. A cobweb is strung between the post and the arm.
  - **An empty hand raises its red flag**, or lowers it.
  - **It is haunted**: now and then its door rattles open a crack and shuts, each mailbox in its own time. **Walk within 4 blocks and its door falls open** and two glowing eyes stare out of the dark inside, blinking now and then. Walk away and it shuts.
  - The haunting is your own sight of it, drawn by your game; it holds nothing.
  - Recipe: an iron nugget, a spider eye and an iron nugget, over an iron nugget, red dye and an iron nugget, over a wooden fence.
  - Broken, it comes away whole and drops itself once.
- **Pumpkin Path Light** (one block across, two up): a shepherd's hook of black wrought iron 1.3 m high, with a foot step to tread it into the ground and its crook bending out over the path.
  - From the crook's tip a little jack-o'-lantern, 25 cm across, hangs on a chain and a wire bail.
  - **Placed lit**: its carved face glows, steady, and its lower block gives light 10. An empty hand puts it out or lights it again.
  - **It sways on its chain**, to and fro along the path and a little from side to side, each hook in its own time.
  - Recipe (makes two): two iron bars, over an iron bar and a jack-o'-lantern, over an iron bar.
  - Broken, it comes away whole and drops itself once.

### Planned
- **Part 2: skeleton animals and the Rising Zombie**: a skeleton dog, crow and cat with square sockets, and a zombie's whole torso rising out of the ground, a step past the Grasping Hands.
- **Part 3: the doorway**: a gargoyle door knocker that moves and knocks, a black-twig bat wreath (with the autumn, bittersweet and holly wreaths) and a "Beware" doormat.
- **Part 4: the Crashed Witch**: striped stockings and a snapped broom sticking out of a tree or a wall.
- **Part 5: the Trick-or-Treat Candy Chute**: from the porch rail to the path; candy dropped in slides down it, tied into trick-or-treating.

## How it is drawn (docs/ART_DIRECTION.md, docs/NATURAL_TEXTURES.md)
- Built from boxes at real size, one texel to a model pixel (`tools/front_yard_data.py`), in the Haunted Midway's manner, whose painters it shares. Each two-high piece is cut into its two blocks' models.
- **Sheet steel**: black, lit along its top row and shaded along its foot, with a row of rivets along its seam. An orange reflector strip runs along each flank, with an amber reflector at its front end. Its inside is dark.
- **Weathered wood**: grey-brown stripes along the grain, for the post, arm and brace.
- **Wrought iron**: black, in stripes along it. The crook bends through a bevel at each corner.
- **The jack-o'-lantern**: the midway's ribbed pumpkin, bulging at its middle. Its carved face glows from inside as its own full-bright model, cut out to its holes and steady.
- **The cobweb**: thin pale strands fanning from the corner between the post and the arm, with two strands across them, cut out. Each side fans from that corner as it is seen.
- **Moving parts move**, from each blockstate's `runner` models:
  - `HauntedMailboxRenderer` swings the mailbox's door about its hinge at its foot and draws its eyes full-bright while it is well open;
  - `PumpkinPathLightRenderer` sways the lantern about the crook's tip and draws its glow full-bright while it is lit.
- **The first previews**, fixed before commit:
  - three claw scratches down the mailbox's flank stepped into a grille that read as teeth; the flank now has a reflector strip;
  - the crook was a square bend; it now bends through a bevel at each corner;
  - the little pumpkin was a box; it now bulges at its middle.

## Connections
- Existing input producer: vanilla iron, spider eyes, dye, fences and jack-o'-lanterns.
- Existing output consumer: none; they are a yard's decoration.
- Technology/magic connections (or infrastructure/cosmetic applicability): cosmetic.
- Reachable entry path: from the first days, with a little iron.
- Which connections are required vs optional; trade and solo routes: all optional, crafted solo, and tradeable like any item.
- How this specialty stays useful without mastering every other branch: it is a house's Halloween dressing for anyone's build.

## Balance and automation
- The mailbox costs four iron nuggets, a spider eye, red dye and a fence; two path lights cost four iron bars and a jack-o'-lantern. Each gives back only itself.
- They make nothing and take nothing. No loop.

## Multiplayer and persistence
- **The mailbox's flag is the server's**: its blocks' state (`flag`, on both blocks), changed only by a player using it, and saved with the world.
- **Its haunting is each player's own**: their client works out from their own distance and the game's clock when its door rattles and when it falls open. Its block entity (`haunted_mailbox`) keeps on each client whether that player is near and since when; nothing is sent or saved, and nothing ticks on the server.
- **The path light's light is the server's**: `lit`, on both blocks. Its sway is worked out from the game's clock on each client; nothing is sent. Its block entity (`pumpkin_path_light`) keeps nothing; it is there for its renderer.
- Neither can be pushed by a piston.
- IDs (stable from release):
  - the block and item `haunted_mailbox` (`facing`, `part` 0 to 1, `lit` (unused), `flag`, model-only `runner` 0 to 2; block entity `haunted_mailbox`, saving nothing);
  - the block and item `pumpkin_path_light` (`facing`, `part` 0 to 1, `lit`, model-only `runner` 0 to 2; block entity `pumpkin_path_light`, saving nothing).
- Their recipes follow the agriculture switch. The blocks and items stay registered when it is off.

## Dependencies and assets
- No new dependency.
- Models, blockstates, loot tables and textures are generated by `tools/front_yard_data.py` from `tools/front_yard.py`, through `tools/generate_material_data.py` and `tools/generate_textures.py`.
- Nothing is copied, traced or recoloured from Minecraft's textures.

## Verification
Local checks run on this branch, and their results:
- Part 1, all on the first run:
  - `python3 tools/check_mod_data.py`: PASS (8,136 material IDs), including the art check of every block and item model. It includes the new `check_front_yard`, which checks:
    - the mailbox's and the path light's Java against `tools/front_yard.py`, and each one's registration with its own block entity and renderer;
    - that the renderers open the door by `lid`, show the eyes by `eyes` and sway the lantern by `sway`;
    - that each blockstate shows its runners alone, and the mailbox's flag down and up by `flag`;
    - that the eyes and the lantern's face glow full-bright, that the path light gives its light from its lower block, and their names.
  - `python3 scripts/check_repository.py`: PASS.
  - `python3 -m unittest discover -s scripts/tests`: 287 tests, OK.
  - `python3 tools/check_icon_maps.py`: PASS (no new map; both items are 3D).
  - `javac` scan of the new and changed Java for syntax errors: none. The full compile runs in CI.
  - The models were previewed offline (review aids, not committed); what the previews showed is under How it is drawn.

`FrontYardGameTests` (run in CI):
- **The haunted mailbox raises its flag**: it stands two blocks high, facing the player, its flag down, its block entity on its first block; an empty hand raises its flag on both blocks and lowers it again; broken at its box, it comes away whole and drops once.
- **The mailbox's door falls open for whoever comes near**: with someone near, it falls open 75 degrees from shut, its eyes staring out; with no one, it shuts, its eyes hidden, and now and then rattles open a crack; each box in its own time.
- **The pumpkin path light lights the path**: it stands two blocks high, placed lit, its lower block alone giving light 10; an empty hand puts it out and lights it again; its lantern sways, up to 6 degrees; broken, it comes away whole and drops once.
- **Their data loads**: both recipes and loot tables.

`FrontYardClientGameTests` takes three screenshots of a gravel path across a lawn. It holds:
- a Haunted Mailbox at the street end, its flag up;
- three Pumpkin Path Lights down the path's side, lit, their lanterns swaying (drawn by `PumpkinPathLightRenderer`).

The shots are from along the path, far enough that the mailbox stays shut; from the street close by the mailbox, near enough that its door falls open and its eyes stare out (drawn by `HauntedMailboxRenderer`); and by night.

Not run: a hand playtest, and a two-client playtest.

## World and event applicability
Seasonal in look, not in use: it is made and works all year, and nothing about it ends with October. It doesn't generate in the world.

## Rollout and open questions
- Part 1 is the path; parts 2 to 5 follow, each stacked on the last.
- Open: whether the mailbox should hold a letter or two (it holds nothing now); whether the path lights should take a dye for their glow's colour.
