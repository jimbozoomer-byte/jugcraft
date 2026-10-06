# Art direction

Jugcraft's look changes with its tiers, the way real technology did: the early game is brass-and-steam, the later tiers move towards dieselpunk, and electrical power gear and the high-tech tiers to come are graphite and glowing light. Every texture and model is original, drawn by the generators in `tools/`.

## Rules for everything
- Detailed models built from boxes (see `tools/steampunk_models.py`): round prisms, gears, gauges, rivets, pipes. No flat cubes where a real machine would have shape.
- **Things that are big in real life are big in the world.** A turbine, a foundry or a charging station takes several blocks; a hand tool stays in the hand.
- Overlapping boxes never share a visible face plane (that flickers, z-fighting). The generators enforce it: `model_writer.separate_coplanar` runs on every model they write and pushes the smaller of two flush, differently drawn faces out by 0.02 pixels, so a band, dial or trim always draws in front of the body it sits on.
- Textures are deterministic (seeded) and opaque on blocks. They are 16×16, or 32×32, 64×64 or (for a sculpted prop's packed texture) 128×128 where the art needs the detail (see [High resolution](#high-resolution)).

## Texturing: keep it clean
On 4 October 2026 the owner rejected the noisy, rust-covered dieselpunk textures ("you are doing way too much in terms of noise"). They pointed to vanilla copper blocks, a weathered pipe, Immersive Engineering Reimmersed's machines and a car drawn in vanilla's palette as the standard. Those references guided the style only; nothing of them is copied. The helpers in `tools/clean_metal.py` draw this way, and new textures should follow it:
- **Flat fills from a short palette.** Use four or five shades per material. Never pick a random shade for every pixel.
- **Shape comes from light.** Give a panel a one-pixel bevel: lit along the top and left, shaded along the bottom and right, with a dark seam round the outside. Shade recessed insets the other way round. Draw bolts as two-by-two heads lit at the top left.
- **Wear is placed, not sprinkled.** Use a chip at a corner, a stain weeping from a bolt or a seam, or a few short streaks one shade off the fill. Rust is an accent, never a whole surface.
- **Pattern beats noise.** Show grain, ribs, tread and ripples as regular shapes: plank lines, ribs every four rows, raised lozenges, long ripple lines.
- **Each material looks like its vanilla counterpart.** On 5 October 2026 the owner added that flat is not the goal either: stone should look like stone, cobblestone or stone bricks, and every material should take after the vanilla textures so it does not stand out. The painters in `tools/block_style.py` work that way. `bs.fill` gives one ground tone with small clumps of the tones either side, in place of a random shade at every pixel. Stone, cobblestone and bricks are lit along their top and left, wood is drawn as planks, and straw and hair as streaks.
- **Ores, metal blocks and ingots follow the vanilla ones.** On 6 October 2026 the owner asked for ore stone to match vanilla stone and deepslate, metal blocks to look like the vanilla metal blocks, and ingots to take the vanilla ingot's shape in each metal's colours. `tools/material_style.py` draws them that way from code; vanilla files are studied, never copied or recoloured.
  - Ores are nodules on neutral stone or on layered deepslate.
  - Storage blocks are one bright, bevelled plate, the same pattern for every metal.
  - Ingots share one diagonal bar, and nuggets one cluster of lumps.
- **Machine faces are clean plates with one clear feature.** A machine front is a frame lit along its top and left round a recessed plate of one tone, with one feature in it: a recessed window, vent or port. A running glow rises in smooth bands, brightest at the bottom. `panel()` and `window()` in `tools/generate_textures.py` draw this way.

## Creatures and faces: cute and clean
On 5 October 2026 the owner found the Ember Bed's fire speckly and the Horned Skull Cauldron's nostrils ugly, and asked for every creature prop to be simplified: cute, or at least smooth, but still good-looking, after their reference pictures (the Frankenstein head above all). The painters in `tools/cute_art.py` draw this way, and every skull, bone, monster, bug, ghost and other creature prop should follow it:
- **Two or three tones a material, no noise.** Fill flat, light the top and left edge, shade the bottom and right one. A rounded form gets a lighter band over its top and a darker one under it, nothing else.
- **Simple faces.** Big round eye sockets with a white glint, or closed eyes drawn as one clean curve. No nose holes or nostrils. Teeth are a neat row of squares with a dark gap; a mouth is one clean line or curve. Rosy cheeks where it suits.
- **Fire in smooth bands.** Rounded tongues of flame banded deep orange, orange, yellow and a pale core, standing on a glow of coals; never a scatter of random sparks.
- **Pattern, not scatter.** Fur, scales, feathers, straw, quills and stone carvings are regular shapes (offset rows, even stripes, scallops), and moss or lichen sits in neat tufts at fixed places.
- **Bright, friendly colours.** The monster is a bright green, bone a warm cream, sockets a soft dark plum rather than black.

![Before and after: the hearth, the monster head, the Colossal Skull, a chimera and the crawling hand, the singing pumpkins and the Harvest Moon](images/cute_creatures_before_after.jpg)

*Before (left) and after (right), drawn from the block models and textures outside the game.*

## Steampunk: stone, bronze and early steel tiers
Brass, copper and riveted iron; glass portholes and valve wheels; firebrick and wood. Textures start with `sp_` (`tools/steampunk_textures.py`). Since batch 53 they follow [Texturing: keep it clean](#texturing-keep-it-clean) too. The classic style pack keeps the older plain look for anyone who prefers it.

## Dieselpunk: steel tier and above
As the tech gets higher tier, it becomes more dieselpunk and less steampunk. The powered tools and the charging station (#40) are the first dieselpunk content.
- **Materials:** gunmetal and olive-drab paint worn through to bare metal at the edges; chrome trim; yellow-and-black hazard stripes; black rubber hoses and grips; bakelite handles; louvred grilles; soot-stained exhaust stacks.
- **Details:** green phosphor gauges, caged amber warning lamps, stencilled serials, heavy bolts rather than decorative rivets.
- Textures start with `dp_` (`tools/dieselpunk_textures.py`); models for tools and stations are in `tools/tool_models.py`.

The steel-tier machines went dieselpunk in #41: the steel foundry, capacitor bank, steel tank, ore drill and high-pressure extractor (`tools/dieselpunk_models.py`, which replaces their entries in `steampunk_models.MODELS`). Their footprints, ports and running lights are unchanged, and the classic style pack keeps their plain look. The coke oven stays brick: it is the bridge into steel.

- **Surface deposits:** rubble-grey faces packed with big shaded ore lumps (black coal, tan raw iron, orange copper with verdigris specks, dark cassiterite with silver glints), so a patch never reads as an ordinary ore block (`tools/deposits.py`).
- **Deposit drill:** a 3×3 skid with hazard edges, four braced pylons and a hazard-striped top frame, an olive drill turret with augers, the gantry-hung upright motor, exhaust stack, ore chute and a control box with gauge and lamp (`dieselpunk_models.deposit_drill`).

## Electric: power gear and the high-tech tiers

The owner asked on 1 October 2026 for the electrical things to look like a modern tech mod's (reference: Mekanism's machines and universal cables), with cables carrying "the same green glowing light", and for higher tiers to grow more high-tech from there (references: dark sci-fi casings with cyan glass panels and screens, a dark multi-block with violet and cyan conduits and a monitor bank with a keyboard, a glowing glass stasis tank with hoses, and a beige retro computer). They chose to restyle the **cables and power gear** now and keep the other references for the next high-tech tier. Every texture is original; the references guide colour and detail only.

- **Applies to:** the copper, silver and aluminum cables, battery box, capacitor bank, charging station, solar panel, electric pump, electric motor and dynamo (`tools/electric_models.py`, `tool_models.charging_station`, `kinetic_models.electric_motor` / `dynamo`, and the cable models in `generate_material_data.py`). Processing machines keep their steampunk or dieselpunk look.
- **Materials:** mid-grey graphite panels with a raised bevel and a recessed groove, darker trim posts and bezels, panel seams on big surfaces, horizontal vents with a faint green glow, yellow-and-black stripes only where there is high voltage.
- **Light:** mint-green strips (`el_glow`). Model elements drawn only in a glow texture get `"light_emission": 15` (`model_writer.EMISSIVE`), so they stay lit in the dark without lighting the blocks around them. Strips sit at least 0.1 pixel proud of the surface they lie on, so they never flicker against it.
- **Details:** green-on-black screens (`el_screen`, lit while the charging station works), round status lamps, power ports (a three-pin socket in a glowing ring) wherever a cable meets the block, and bars of green light on charge panels.
- **Cables:** 6 pixels thick (was 4), so the core strip reads at a distance. The collars at each end show the tier: copper, silver or aluminum.
- **Electronics tier (batch 7):** the cyan look has started: `el_dark` casings with cyan seams, `el_glass` panels that glow while running, cyan screens (`el_screen_cyan`), violet conduits (`el_conduit`, `el_glow_violet`) and beige `rt_` textures for the retro computer. Models in `tools/hightech_models.py`; see [electronics.md](features/electronics.md).
- **Tanks (batch 10):** the owner's reference: white bodies with bold black-and-white checker bands (`tk_checker`), dark rims and lids (`tk_rim`), sight glasses and flanges; the tinplate tank, steel tank and gas holder share it (`tools/tank_models.py`).
- **Advanced power (batch 10):** the advanced solar panel is a white pedestal with a green-lit ring and deep blue cell wings (`el_white`, `el_solar_large`); the advanced engine is graphite with a light ribbed cylinder bank (`el_ribbed`) and white caps.
- **Next tiers:** cyan (`el_glow_cyan`) is reserved for the high-tech tier after oil, with glass tanks, conduits in violet and cyan, monitor banks and keyboards (`el_keyboard`), following the owner's other reference images. Textures start with `el_` (`tools/electric_textures.py`).

## Outside the tech tiers: the Pixel Hollows and the arcade
Places and decor that are not machines keep their own identity. The Pixel Hollows and the Retro Trader are retro electronics: dark slate with copper traces, square-faceted teal and violet crystals, LED-pixel lamps, and an 1980s arcade cabinet with neon side art, a CRT and a lit marquee. Everything is original (no real consoles, games, brands or characters). Textures are drawn by `tools/pixel_hollows_textures.py` (names `ph_*` and `rt_*`) and the cluster and cabinet models are in `tools/retro_models.py`. The arcade cabinet is real-life sized: two blocks tall.

## High-detail items (64x64)
Items that need more than 16 pixels, starting with the batch 32 construction tools, are drawn at 64x64 with `tools/hd_art.py`, a small shaded-shape renderer:
- **Shapes:** capsules (rods and tubes), bevelled boxes, domes and discs, rims, flat polygons and lumpy blobs. They are painted in order onto a canvas, so later shapes cover earlier ones.
- **Lighting:** every shape gives each pixel a surface normal, lit from the top left with a specular glint.
- **Pixel-art finish:** the brightness snaps to the material's colour ramp, with ordered dithering only across the falloff between two steps, so the result stays crisp pixel art rather than a blurry render.
- **Materials:** a shared palette (steel, gunmetal, chrome, brass, rubber, olive drab, safety yellow, hazard black, glass, foam and more), plus paint helpers for hazard stripes and paint worn through to bare metal at the edges (`hazard`, `worn`).
- **Tool space:** `construction_art.Tool` lays a tool out along its barrel (s) and across it (t), so a diagonal held tool can be drawn with straight-line coordinates.

The 64x64 items are listed in `construction_art.ITEMS`. New high-detail items should use the same renderer and palette, so they match each other.

## High resolution
The owner asked on 3 October 2026 that new art not be held to Minecraft's 16×16, so it can follow the reference images more closely. From then on:
- **Blocks and items** may use 32×32 or 64×64 textures (`tools/check_mod_data.py` allows 16, 32, 48 and 64; 48 is for the long arms' icons). A block model's faces sample their texture by position (0 to 16), so a 64×64 texture draws four times finer with no change to the model. Vanilla blocks beside them stay 16×16, so new things look sharper than the world around them.
- **Sculpted props** (`tools/flora_art.py`'s `Sculpt`, which packs every painted piece of a prop into one texture) may use 128×128 when a prop has too many pieces for 64×64 at the same detail: a cabinet two blocks tall, a chandelier three blocks across. The Witch's Workshop (batch 17, 4 October 2026) was the first to need it; `tools/check_mod_data.py` allows 128×128 for these.
- **Creatures** are painted at several times their model's texture size. The model keeps its layout (its `LayerDefinition` size) and samples the larger image by it, so each model pixel shows a patch of painted detail. The werewolves are painted at four times (512×512, `TEXTURE_SCALE` in `tools/werewolf_model.py`).
- **Fur** is painted by `tools/fur_paint.py`:
  - each face shaded towards the light, with soft patches;
  - fur laid on in tapering locks, lit along one side and shadowed along the other, with fine hairs over them;
  - eyes, noses, pads, teeth and claws painted on top.

  Mob textures are drawn as cut-outs, so cleared pixels cut a shaggy mane's lower edge into points, and turn fangs and claws into real points.
- **Shapes are still boxes.** Smoother outlines come from more, smaller, turned boxes. A renderer for real meshes would be a project of its own, and a dependency such as GeckoLib would need a reviewed platform change.
- **Weapons are pixel art, with 3D in the hand** ([arms-restyle.md](features/arms-restyle.md)):
  - icons are 32×32 or 48×48, on the 45-degree pixel diagonal, in flat tones lit from the top left, with a one-pixel outline: no dithering, noise or glints;
  - in the hand each weapon is a box model with thickness, built from the same design (`tools/arms_pixel.py`).
- **Art stays original.** References guide the look; nothing is traced or copied from them.
