# Pumpkin carving: the Carving Knife and hand-carved pumpkins

Status: implemented in source; **not yet played**. The Build workflow compiles it, and CI's game tests pass (details below).
Proposal issue: none; requested directly by the owner on 1 October 2026 ("start with the carving knife, 16 per block is fine"), the first of the Halloween and harvest additions discussed with them.
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead); Discovery tier (an iron ingot and a stick).
Primary specialty and supported player role: decoration and building; supports farmers (pumpkin seeds, a roasted snack) and anyone decorating for Halloween

## Player experience
Carve any face you like into a pumpkin, pixel by pixel:

- **The Carving Knife** (an iron ingot over a stick). Use it on the side of a pumpkin to open the carving screen for that side.
- **The carving screen** shows the side as a 16x16 grid, one cell per texel of the block, so carvings match Minecraft's pixel size. Left-drag carves with the chosen tool:
  - **Cut** goes right through (a hole the candle shines out of);
  - **Shave** peels the skin, so light glows through it more softly;
  - **Erase** undoes this session's strokes (right-drag erases too).
  Brush sizes 1-3, **Mirror** (copies every stroke to the other half), four **starter faces** (Classic, Cat, Ghost, Spooky), **Undo** (Ctrl+Z), **Reset**, a **Candle** preview and an actual-size preview. **Done** carves it.
- **A knife can't put skin back.** What was carved before the screen opened is fixed; you can only carve deeper. Within one session, Erase and Undo take back your own strokes.
- **Every side** of the pumpkin can carry its own face. The first cut opens the pumpkin: its 4 seeds fall out (vanilla's carving loot, as with shears) and it becomes a **Hand-Carved Pumpkin** facing the side you carved.
- **Light it:** use a torch on it. It glows more the more is carved out: 4, plus 1 for every 3 holes and every 12 shaved pixels, up to 15 (a face about the size of vanilla's jack-o'-lantern gives 15). Use it with an empty hand to take the torch back out.
- **Keep it:** broken, it drops as an item with its design and its torch (the icon shows whether it is lit), and placed again it faces you with the same carving.
- **Roasted Pumpkin Seeds:** roast pumpkin seeds in a furnace, smoker or campfire for a small snack.

Nothing is seasonal: the knife and carved pumpkins work all year and stay in the world.

## Connections
- Existing input producer: vanilla pumpkins (wild patches and pumpkin farms), iron and sticks, torches.
- Existing output consumer: decoration and light for builders; pumpkin seeds for farmers; roasted seeds (food, compost); the `c:foods` tag.
- Technology connection: none needed. A Jugcraft lamp or electric candle could light pumpkins later.
- Magic connection: none yet.
- Reachable entry path: an iron ingot, a stick and a pumpkin. Nothing else.
- Required vs optional: entirely optional; nothing in progression needs a carved pumpkin.
- How this stays useful without other branches: decoration and light on its own.

## Balance and automation
- The knife lasts 238 carvings (shears' durability); one finished carving (one Done) uses one.
- Carving a pumpkin gives the same 4 seeds as carving it with shears, once (the first cut). Hand-carved pumpkins are never turned back into plain ones, so there is no seed loop.
- Light: at most 15, like a jack-o'-lantern, and only with a torch inside (the torch is kept, not used up while lit).
- Roasted pumpkin seeds restore 2 hunger (like roasted sunflower seeds); roasting only turns seeds into food.
- No automation: carving is by hand, one side at a time.

## Multiplayer and persistence
- **Server authority.** The client only draws the screen. The finished face comes back as a fixed-size message (16 ints, so no length to forge), and the server checks every one before anything changes:
  - the player opened a carving session for exactly that block and side, in that dimension, within the last 5 minutes (one session per use of the knife, one carve per session);
  - they hold a Carving Knife in either hand, are within block reach, may use items there (`mayUseItemAt`: adventure mode can't) and may interact with the block (`mayInteract`: spawn protection);
  - the block is still a pumpkin and the side is not the top or bottom;
  - every pixel is skin, shaved or cut, and no pixel is shallower than before;
  - with `carving.free_draw=false` in `config/jugcraft.properties`, the face is one of the starter faces.
  Refusals send the player a short message and change nothing.
- **Moderation.** Free drawing means players can carve anything, as with signs and maps. Each carved pumpkin records who carved it last (UUID and name) in the saved world for server operators (`/data get block`); it is not sent to clients. Servers that want no free drawing set `carving.free_draw=false`, which allows only the starter faces to be carved. It does not reach creative mode or commands, which can give any item with any design, as they can with other block data.
- **Saved state.** The design (four sides, 2 bits a pixel, 256 bytes) and the carver live in the block entity; on the item, the design is the `jugcraft:carving` data component and the torch is the block state component (`lit`). Clients receive the design only.
- **Bounded work.** No ticking. A carve checks 256 pixels once. The client keeps one 64x16 texture per distinct design (and lit or not), at most 256 at a time, the least recently drawn released first, and all of them when leaving a world.
- New IDs only: `carving_knife`, `hand_carved_pumpkin`, `roasted_pumpkin_seeds`. Vanilla pumpkins, carved pumpkins and jack-o'-lanterns are unchanged. The `agriculture` switch turns off the knife's and the seeds' recipes; existing carved pumpkins stay.

## Dependencies and assets
No new dependencies. Uses Fabric API's networking (two play payloads), block entity and creative tab APIs, which are already required. The hand-carved pumpkin uses vanilla's pumpkin model and textures by reference (so resource packs apply); the carving colours, the knife, the item icons and the starter faces are Jugcraft's own (`tools/carving_textures.py`, `CarvingTemplates.java`).

## Verification
VERIFICATION

## World and event applicability
No worldgen. Nothing is seasonal; a later Halloween event could add a carving contest on top, but carving never depends on an event.
