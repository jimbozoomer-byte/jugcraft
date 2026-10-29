"""Readable vanilla building signs; no changes to layout RNG or resident IDs.

Revision 4 posts each shop's opening hours and the taverns' all-hours essentials on the signs (the mod rewrites a sign's
text only where it still reads as the town wrote it: town/TownUpgrades.java)."""
from town_voxels import S, turn_xz, turn_facing
from town_shops import hours_line

SHOP_LINES = {
    "general": ["GENERAL STORE", "Buy & sell", "Trade for Jugs", hours_line("general")],
    "seasonal": ["SEASONAL STALL", "Seasonal decor", "Trade for Jugs", hours_line("seasonal")],
    "curios": ["CURIOSITIES", "Music & curios", "Trade for Jugs", hours_line("curios")],
    "florist": ["FLORIST", "Flowers & trees", "Trade for Jugs", hours_line("florist")],
    "baker": ["THE BAKERY", "Meet the baker", "Local workshop", "No shop menu yet"],
    "innkeeper": ["THE TAVERN", "Open all hours", "Rest & chat", "Essentials sold"],
    "banker": ["JUGS BANK", "Four Jug Tellers", "Jugs transfers", "Use a teller"],
    "mayor": ["TOWN HALL", "Meet the mayor", "Civic gathering", "Welcome!"],
    "priest": ["CHURCH OF NOTCH", "Praise Notch", "Priest of Notch", "All are welcome"],
}


def add_piece(town, piece, ox, oy, oz, turns, purpose):
    if not piece.spots:
        return
    role = piece.spots[0]["role"]
    key = purpose if purpose in SHOP_LINES else {"bakery": "baker", "tavern": "innkeeper"}.get(
        getattr(piece, "purpose", ""), role)
    if key not in SHOP_LINES:
        return
    x = piece.door[0] if piece.door else piece.w // 2
    z = piece.door[1] if piece.door else -1
    height = 3 if piece.door else 2
    if not piece.door:
        x = 0  # counter post; leave the seasonal valance and serving opening free
    # Supported lintel plaques, above the player's head and outside the entrance.
    for local_z in (z + 1, z):
        rx, rz = turn_xz(x, local_z, turns, (piece.w, piece.d))
        state = S("spruce_planks")
        if local_z == z:
            state = S("spruce_wall_sign", facing=turn_facing("north", turns), waterlogged="false")
            town.signs.append({"pos": [ox + rx, oy + height, oz + rz], "lines": SHOP_LINES[key]})
        town.v.set(ox + rx, oy + height, oz + rz, state)


def boards(town):
    # The old map kiosk is retired. Individual buildings retain their signs.
    pass
