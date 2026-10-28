"""Textures for the Church Bridge (tools/church_bridge.py, docs/features/church-bridge.md): only the Hessian's Bridle's icon
(its map, tools/item_icons/hessians_bridle.txt). The hollow itself is built from vanilla blocks and the lairs' own (the
Soul Braziers, Moonwood Leaves, Grey Mist and the moon), so it has no textures of its own. Called from
tools/lair_textures.py. No Mojang texture is read, traced or copied.
"""
import item_icons


def church_bridge_textures():
    return {("item", "hessians_bridle"): item_icons.draw("hessians_bridle")}
