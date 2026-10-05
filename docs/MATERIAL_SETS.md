# Material sets: how an ore and everything made from it look

How a material's whole set is drawn: its ore, the raw ore, nugget, ingot and blocks, and the tools, armor and weapons made
from it. Follow it for every new ore that turns into gear, and when an existing material's set is redrawn.

On 5 October 2026 the owner drew a complete set for a new ore in one chartreuse palette. It covers the ore in stone and
deepslate, the raw chunk and raw block, nugget, ingot, a storage block, sword, pickaxe, axe, shovel and hoe, helmet,
chestplate, leggings and boots (each plain and gold-trimmed), daggers, a sabre, longswords, a spear, a hammer, a spiked
mace, a bow, a crossbow, an arrow and horse armor. They asked: "maybe we can use this for a basis on how new ores that
turn into tools are styled?". It is the same style as their weapon sheet, which the arms' 16×16 icons follow (in progress). This page is that
basis; [features/thallite.md](features/thallite.md) is the first material built on it.

The owner's pictures are references for the style. Every texture in the repository is drawn fresh by code, and nothing
is recoloured from Mojang's files ([CLAUDE.md](../CLAUDE.md)).

## The look

A set looks as if vanilla had one more material:
- **Size:** 16×16, everything.
- **Shapes:** vanilla's proportions. A pickaxe is a pickaxe, and an ingot is a slanted bar.
- **Colour:** the whole set in **one bold colour**, stepped into a few tones.
- **Outline:** a one-pixel outline in that colour's darkest tone.
- **Light:** from the top left, with a bright edge along the lit side of every blade, head and plate.
- **Handles:** a stick's two browns.
- **Sameness:** every item in the set shares the same tones, so the set reads as one material from across a chest.

## The palette

One hue, five tones and an outline:

| Tone | Lightness | Use |
|---|---|---|
| Outline | about 20–25% | every edge against air or another material; never pure black |
| Dark | about 40% | shaded sides, the lower right of blades and plates, ore blobs' undersides |
| Mid | about 60% | most of every surface; the most saturated tone |
| Light | about 75% | lit faces, upper-left halves |
| Highlight | about 90%, nudged towards white or yellow | the lit edge of a blade, the top of an ingot, a glint on armor |

- **Saturation is highest in the mid tones** and eases off towards the highlight.
- **The colour should be bolder than vanilla's iron** and in the range of its gold, copper or emerald.
- **Check the palette across the whole set,** not one item: the ingot, a sword and a chestplate side by side.

Other materials keep their own tones:
- **Handles:** a stick's two browns, with a dark brown outline.
- **Set stones:** a contrasting gem colour; the owner's chartreuse set uses emerald green.
- **The trimmed look:** gold and orange.

## Each item

| Item | How it is drawn |
|---|---|
| **Ore** (`<ore>_ore`, `deepslate_<ore>_ore`) | **The host rock in vanilla's manner:** stone's grey speckle, or deepslate's darker banding. On it sit five to seven **chunky, rounded blobs** of the ore, each 3 to 5 pixels. **Each blob:** light on top, mid in the body, dark along its lower right. **No outline round blobs:** the rock is the outline. **Same layout** in stone and deepslate. **Clearly visible:** the blobs are bigger and bolder than the mod's older ores' specks (and as clear as vanilla's). |
| **Raw ore** (`raw_<ore>`) | **One lumpy, rounded chunk** filling most of the slot, in the lighter tones, lit top-left, outlined. |
| **Raw block** | **Rounded lumps packed like cobblestone,** each lit top-left, with dark joints between them. |
| **Nugget** | **A small lozenge** about 3 by 5 pixels with a highlight along its top. |
| **Ingot** | **Vanilla's slanted bar:** a light top face, a mid front face, a highlight along the top edge, outlined. |
| **Storage block** | **A bevelled square:** a light frame, inside it a field divided by a cross into **four rounded inset panels** (a four-panel inlay), each lit top-left. The block a player stacks is the material's emblem, so it is the most decorated piece. |
| **Tools** (sword, pickaxe, axe, shovel, hoe) | **Vanilla's silhouettes on the diagonal,** the head in the material and the handle a two-tone stick. **The head:** a highlight along its lit edge, dark along the other, outlined. **No extra parts.** |
| **Armor icons** (helmet, chestplate, leggings, boots) | **Rounded vanilla shapes in the material,** lit along their upper parts. **A gem** (two tones of a contrasting colour) may sit at the knees or ankles, as the owner's leggings and boots carry emeralds. |
| **The trimmed look** | **An upgraded version of each armor piece:** the same piece with **gold or orange trim** on its corners and edges and a brighter highlight. What earns it is the material's design to decide (an upgrade, attunement, an enchantment). |
| **Horse armor** | **Vanilla's blanket shape in the material,** with one coloured band (the owner's is red). |
| **Weapons** | **The arms' 16×16 maps** (`tools/arms_icons/`), coloured in the material: swords, daggers, sabres, spears, hammers, maces, bows and crossbows, as in the owner's sheet. |

## How a set is made

The arms' new 16×16 icons are drawn this way (`tools/arms_icons.py`, in progress):
- **The map:** each item kind is one hand-drawn 16×16 map of symbols (blade or head, fittings, haft, grip, stone, accent).
- **The palette:** a material is a palette that fills the map in.
- **Extending it:** a new material's tools, armor icons, ingot, nugget, blocks and ore are drawn the same way, from shared maps.
- **What a new ore then needs:**
  - a palette, from a colour the owner approves;
  - a seed for its ore blobs' layout;
  - its design: where it is found, its tier, what its gear does.

When an existing material (bronze, steel, tin, silver and the rest) is redrawn in this style, it follows this page too.

## Checklist
- [ ] 16×16; vanilla's proportions; drawn by code from maps, nothing recoloured from Mojang's files.
- [ ] One bold colour, stepped into five tones and an outline, the same across the whole set.
- [ ] One-pixel outline in the darkest tone; light from the top left; a highlight on every lit edge.
- [ ] Ore: chunky rounded blobs on vanilla-style stone and deepslate, readable at a glance.
- [ ] Storage block decorated as the material's emblem; nugget, ingot and raw ore in vanilla's shapes.
- [ ] Armor in a plain look, and a trimmed look in gold if the material has an upgrade.
- [ ] Every item checked beside the rest of the set and beside vanilla's at 1× and 2×.
