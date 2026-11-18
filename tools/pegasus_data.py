"""The Pegasus's resources (docs/features/pegasus.md; Java: pegasus/ and client/pegasus/): its names, the spawn egg's
item model, the flight keys' names and help, the egg's control tooltip and its message.

Called from generate_material_data.py. Its textures are painted from art/pegasus/ (pegasus.bbmodel and
pegasus_saddle.bbmodel embed them; tools/check_pegasus_model.py checks the copies match).
"""
MOD = "jugcraft"
TEXT = {
    "entity.jugcraft.pegasus": "Pegasus",
    "item.jugcraft.pegasus_spawn_egg": "Pegasus Spawn Egg",
    "key.category.jugcraft.pegasus": "Jugcraft: Pegasus",
    "key.jugcraft.pegasus_ascend": "Pegasus: Ascend / Take Off",
    "key.jugcraft.pegasus_descend": "Pegasus: Descend / Land",
    "encyclopedia.jugcraft.key.pegasus_ascend": "Piloting a Pegasus: hold a second on the ground, or again mid-jump, "
                                                "to take off; in flight, hold to climb",
    "encyclopedia.jugcraft.key.pegasus_descend": "Piloting a Pegasus in flight: hold to descend and land (no sprinting "
                                                 "while you fly)",
    "tooltip.jugcraft.pegasus.egg": "A rare flying horse, found alone on high mountain meadows",
    "tooltip.jugcraft.pegasus.controls.trait": "Flight controls",
    "tooltip.jugcraft.pegasus.controls.saddle": "Tame and saddle it like a horse; its saddled rider flies it.",
    "tooltip.jugcraft.pegasus.controls.takeoff": "Tap or charge %1$s for a normal horse jump. Hold %2$s for a second "
                                                 "on the ground, or hold it again mid-jump, to take off.",
    "tooltip.jugcraft.pegasus.controls.fly": "Flying: hold %1$s to climb and %2$s to descend and land; release both "
                                             "to fly level.",
    "tooltip.jugcraft.pegasus.controls.steer": "%1$s %2$s %3$s %4$s steer; %5$s gets off (in the air, you fall).",
    "message.jugcraft.pegasus.bucked": "The pegasus bucks you off",
    "tooltip.jugcraft.pegasus.pack.trait": "Light pack",
    "tooltip.jugcraft.pegasus.pack.fit": "Saddled and tamed, use a chest on it to strap a small pack (9 slots) behind the "
                                         "saddle.",
    "tooltip.jugcraft.pegasus.pack.open": "Sneak and use it with a stick to open the pack; it rides on the saddle.",
}


def write_all(write, resources, lang):
    from horse_breeds_data import BREEDS
    assets = resources / "assets" / MOD
    lang.update(TEXT)
    # 26.3 validates the saddle's Equippable component against this entity tag.
    # A SADDLE stack alone does not grant control to a newly registered horse type.
    write(resources / "data" / "minecraft" / "tags" / "entity_type" / "can_equip_saddle.json",
          {"replace": False, "values": [f"{MOD}:pegasus", *(f"{MOD}:{breed}" for breed in BREEDS)]})
    write(assets / "items" / "pegasus_spawn_egg.json",
          {"model": {"type": "minecraft:model", "model": f"{MOD}:item/pegasus_spawn_egg"}})
    write(assets / "models" / "item" / "pegasus_spawn_egg.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/pegasus_spawn_egg"}})
