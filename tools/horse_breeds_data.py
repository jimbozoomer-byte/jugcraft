"""The horse breeds' resources (docs/features/horse-breeds.md; Java: horses/ and client/horses/): names, spawn egg item
definitions and models, tooltips, the lineage card, the saddlebag menu and messages.

Called from generate_material_data.py. The models, textures, loot tables and the road tag come from
tools/horse_breeds.py (python3 tools/horse_breeds.py), from art/horse_breeds/.
"""
MOD = "jugcraft"
BREEDS = ("icelandic_horse", "mongolian_horse", "shire_horse", "giant_finnhorse", "friesian_horse")
TEXT = {
    "entity.jugcraft.icelandic_horse": "Icelandic Horse",
    "entity.jugcraft.mongolian_horse": "Mongolian Horse",
    "entity.jugcraft.shire_horse": "Shire Horse",
    "entity.jugcraft.giant_finnhorse": "Giant Finnhorse",
    "entity.jugcraft.friesian_horse": "Friesian Horse",
    "item.jugcraft.icelandic_horse_spawn_egg": "Icelandic Horse Spawn Egg",
    "item.jugcraft.mongolian_horse_spawn_egg": "Mongolian Horse Spawn Egg",
    "item.jugcraft.shire_horse_spawn_egg": "Shire Horse Spawn Egg",
    "item.jugcraft.giant_finnhorse_spawn_egg": "Giant Finnhorse Spawn Egg",
    "item.jugcraft.friesian_horse_spawn_egg": "Friesian Horse Spawn Egg",
    "tooltip.jugcraft.horse_breeds.traits": "Breed",
    "tooltip.jugcraft.horse_breeds.bags": "Saddlebags",
    "tooltip.jugcraft.horse_breeds.bags.how": "Tamed, use a chest on it to fit saddlebags; sneak and use it with a stick "
                                              "to open them.",
    "tooltip.jugcraft.horse_breeds.icelandic_horse": "A small, sure-footed trail and snow pony",
    "tooltip.jugcraft.horse_breeds.icelandic_horse.good": "Steps up broken ground, walks on powder snow, never freezes, "
                                                          "shrugs off falls",
    "tooltip.jugcraft.horse_breeds.icelandic_horse.bad": "Slower and a weaker jumper",
    "tooltip.jugcraft.horse_breeds.icelandic_horse.home": "Wild in snowy and windswept lands",
    "tooltip.jugcraft.horse_breeds.mongolian_horse": "A hardy endurance horse",
    "tooltip.jugcraft.horse_breeds.mongolian_horse.good": "More health; heals itself while calm; never freezes",
    "tooltip.jugcraft.horse_breeds.mongolian_horse.bad": "Average speed and jump",
    "tooltip.jugcraft.horse_breeds.mongolian_horse.home": "Wild on plains and savannas",
    "tooltip.jugcraft.horse_breeds.shire_horse": "A slow, strong pack horse",
    "tooltip.jugcraft.horse_breeds.shire_horse.good": "The biggest saddlebags (27 slots), more health, hard to push",
    "tooltip.jugcraft.horse_breeds.shire_horse.bad": "The slowest and the weakest jumper; a wide body",
    "tooltip.jugcraft.horse_breeds.shire_horse.home": "Wild on plains, rarely",
    "tooltip.jugcraft.horse_breeds.giant_finnhorse": "A huge, broad-striding heavy mount",
    "tooltip.jugcraft.horse_breeds.giant_finnhorse.good": "The most health, natural armour, very hard to push, a long "
                                                          "stride; small saddlebags (9 slots)",
    "tooltip.jugcraft.horse_breeds.giant_finnhorse.bad": "Too big for doors and tunnels; a weaker jumper",
    "tooltip.jugcraft.horse_breeds.giant_finnhorse.home": "Wild in taiga, rarely",
    "tooltip.jugcraft.horse_breeds.friesian_horse": "A fast road and jumping horse",
    "tooltip.jugcraft.horse_breeds.friesian_horse.good": "The fastest and the best jumper, faster still on roads",
    "tooltip.jugcraft.horse_breeds.friesian_horse.bad": "The least health",
    "tooltip.jugcraft.horse_breeds.friesian_horse.home": "Wild in flowery meadows and on plains",
    "container.jugcraft.saddlebags": "%s: Saddlebags",
    "message.jugcraft.horse_breeds.not_yours": "Only this horse's owner can use its saddlebags",
    "message.jugcraft.horse_breeds.pack_needs_saddle": "The pack rides on the saddle: saddle it first",
    "message.jugcraft.horse_breeds.overflow": "%s stacks no longer fit in the saddlebags and were handed back",
    "message.jugcraft.horse_breeds.foal": "A %s foal is born: %s",
    "message.jugcraft.horse_breeds.card": "%s, a %s: %s (generation %s)",
    "message.jugcraft.horse_breeds.card_parents": "; foal of a %s and a %s",
    "message.jugcraft.horse_breeds.card_bags": "; saddlebags hold %s",
}


def write_all(write, resources, lang):
    assets = resources / "assets" / MOD
    lang.update(TEXT)
    for breed in BREEDS:
        egg = f"{breed}_spawn_egg"
        write(assets / "items" / f"{egg}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{egg}"}})
        write(assets / "models" / "item" / f"{egg}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{egg}"}})
