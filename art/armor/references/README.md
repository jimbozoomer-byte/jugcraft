# The owner's armor renders

The owner designs armor in Blockbench and sends renders of it. These are those renders, kept so the 3D armor sets can
be compared with them and rebuilt from them (`tools/armor_reference.py`). They are the owner's own work, given for
this project; like the rest of the repository they fall under its [license](../../../LICENSE_POLICY.md).

Each set has a folder. A view `<view>.png` is the render as sent; `<view>.fit.json` beside it is what was fitted to it:
the camera, the figure's pose (shared by the set's views) and polygons round anything that hides the armor (a weapon, a
shield, sparks), which the fitting and the texel lifting leave out.

| Set | Views | Sent |
| --- | --- | --- |
| [Sentinel](sentinel/) | `front_right`, `front_left`, `back` | 8 October 2026, with "Just made these ones aswell want them done weapons too please" (the sword and the star shield in them are the set's arms, not its armor) |

The armor built from them is a Blockbench project under [art/armor/](../), one per set: see
[docs/features/blockbench-armor.md](../../../docs/features/blockbench-armor.md).
