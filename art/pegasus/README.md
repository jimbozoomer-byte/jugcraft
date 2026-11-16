# Pegasus art sources

Editable sources for the Pegasus ([feature record](../../docs/features/pegasus.md)).

| File | What it is |
| --- | --- |
| `pegasus.bbmodel` | The model: vanilla 26.3's horse rig (12 bones, names, parents, pivots and cubes unchanged) plus two articulated wings, 48 cubes, box UV, 128x64, texture embedded |
| `pegasus_saddle.bbmodel` | The saddle layer: blanket, seat, pommel, cantle, billets, girth, breast collar and straps on `body`; headstall, noseband and bit rings on `head_parts`; two reins (zero-thickness planes, shown only while ridden). 16 cubes, 64x64, texture embedded |
| `pegasus_animation.json` | The wing keyframes and stroke constants `PegasusFlight.wingPose` uses, with sample poses |

The game's textures (`src/main/resources/assets/jugcraft/textures/entity/pegasus/pegasus.png` and
`pegasus_saddle.png`) are byte for byte the PNGs embedded in these files.

## Numbers: raw mesh px, scaled once in Java

Both files are in **raw mesh px**, Minecraft model space converted to Blockbench's Java-entity space, exactly the numbers
the Java part definitions use. They are not pre-scaled. In game, `PegasusModel` and `PegasusSaddleModel` apply
`MeshTransformer.scaling(1.1f)` once, as Codex reported `ModelLayers.HORSE` and `HORSE_SADDLE` do for the adult horse
(root pose scaled by 1.1, then lifted by 24.016 x (1 - 1.1) = -2.4016 px; the fixed point is the floor). Heights in game
are therefore 1.1 x (Blockbench y + 0.016). Never bake 1.1 into these files.

Conversion (Minecraft -> Blockbench): a root bone's pivot p becomes the group origin (-p.x, 24 - p.y, p.z); a child's
origin is its parent's plus (-o.x, -o.y, o.z); rotations become degrees (-x, -y, z), both turned Z*Y*X; a cube at local
from (x, y, z) with size (w, h, d) becomes from = origin + (-(x+w), -(y+h), z), to = origin + (-x, -y, z+d);
`texOffs` is `uv_offset` and `.mirror()` is `mirror_uv`. `tools/check_pegasus_model.py` applies this to the Java source
and compares every bone and cube with these files.

## The rig

- **Vanilla bones:** `body`, `tail` (child of `body`), `head_parts` with `head` (ears), `mane`, `upper_mouth`, and the four
  legs, as `AbstractEquineModel.createBodyMesh(CubeDeformation.NONE)` builds them in 26.3 (from the supplied javap).
  `head_parts` and `tail` keep their 30 degree rest rotation; vanilla's setupAnim keeps animating them.
- **Wings:** `left_wing` (shoulder, pivot 5.5, -8.5, -12 on `body`: the top edge of the withers) -> `left_wing_elbow`
  (+7.5 along the wing) -> `left_wing_hand` (+8). Each bone has a 2 px leading edge, a 1 px covert layer on top and a 1 px
  layer of flight feathers below it: tertials (3 px wide, 4 long), secondaries (3, 3 and 2 px wide, 5 to 6 long) and
  primaries (3 px wide, 6 and 7 long, then four outer primaries running out along the span to the tip). TG2 refinement
  (14 November 2026, coordinator review): fewer, broader and shorter feathers on the shoulder and elbow so the folded
  wing sits high on the shoulder and back; bones, pivots, names and poses unchanged. No two cubes overlap. The
  right wing is the mirror image, sharing the left's texture regions (the left wing `.mirror()`ed, as vanilla does the
  left legs). The rest pose is spread level; the game folds and beats them.
- **Poses** (`PegasusFlight.wingPose`, left wing; the right is (x, -y, -z)): `open` 0 folds the wing along the flank
  (span back, feathers hanging and flaring out), 0.25 lifts it while ridden on the ground, 0.5 raises it up and back,
  1 spreads it. The shoulder moves between those by quaternion slerp and beats about the body's z axis, the elbow and
  hand following later in the beat.
- **Saddle fit:** the seat and blanket sit within 3.5 px of the spine, inside where the folded wings lie, and the girth
  starts 3 px down the flank, below them; the breast collar and its straps end ahead of the wing roots. Sampled over the
  folded, ridden, raised, spread, upstroke and downstroke poses, no wing cube passes through any saddle part, except the
  billets, which lie under the folded secondaries and are hidden by them. The rider's riding-pose thighs pass through
  the wing roots by about a pixel (an approximation of vanilla's riding pose, not checked in game).

## Texture

Original pixels, painted by script at one texel a model pixel, in the manner of docs/ART_DIRECTION.md: an off-white coat
(lit on top, cool shade below), silver-white mane and tail in even strands, warm grey hooves, a soft muzzle with no
nostrils, 2x2 eyes with one glint; feathers in broad 2-3 texel groups of flat tone, neighbours a step apart, a pale covert
band at the root and darker blue-grey tips (no vane lines), under short rounded 3 px scalloped coverts. The saddle is brown leather with a sky-blue blanket trimmed in gold, gold buckles and steel bit
rings. No vanilla or owner-library pixels are used.

## How these were made and checked

Built by a script from the vanilla base rig (parsed from the supplied 26.3 javap of `AbstractEquineModel.createBodyMesh`
and `EquineSaddleModel.createSaddleLayer`) plus the wing and saddle specifications, then validated (JSON, UVs inside the
texture, no zero-size cubes except the two reins, unique bones, every cube in a bone, the 12 vanilla bones and parents):
both pass. The build scripts, the posed previews (labelled "generated preview (Python box renderer), not a Blockbench or
in-game render") and the validation output are in the review package that accompanied this change, not in the
repository. None of this has been opened in Blockbench or seen in the game.
