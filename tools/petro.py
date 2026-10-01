"""Petrochemistry (the dieselpunk oil line of the Chemistry branch): single source of truth.

See docs/branches/CHEMISTRY.md for the plan. generate_material_data.py writes the fluid blocks, buckets and names
from here, petro_textures.py draws them, and check_mod_data.py keeps the Java registration in sync.
Amounts are millibuckets (mB); 1 bucket = 1,000 mB.
"""

# Fluids: display name, colours (dark, mid, light, sheen) for the textures, and how they flow.
# tick_delay: ticks between spreads (water 5, lava 30); slope: how far it looks for a way down (water 4);
# drop_off: level lost per block (water 1, lava 2). None of them makes new source blocks.
FLUIDS = {
    "crude_oil": {"display": "Crude Oil", "feature": "crude_oil",
                  "colors": [(14, 11, 9), (30, 23, 17), (48, 38, 28), (70, 62, 84)],
                  "tick_delay": 20, "slope": 2, "drop_off": 2},
}


def fluid_blocks():
    return list(FLUIDS)


def buckets():
    return [f"{fluid}_bucket" for fluid in FLUIDS]


def petro_items():
    """Items of the petrochemistry line that are not blocks."""
    return buckets()
