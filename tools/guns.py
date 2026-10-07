"""Guns (docs/features/guns.md): slice 1, the owner's Rust Midge, Patchwork Carbine and Thunderpipe; slice 2, the
iron set: the Warden Pistol, Riveter SMG and Haymaker; slice 3, the Longhorn Rifle, Drover Rifle and Coach Gun;
slice 4, the black powder guns: the Duelling Pistol, Line Musket and Bellmouth.

The owner made these guns (inspired by Scorched Guns 2) and supplied, in the owner asset library:
  - a Blockbench Java model of every part (art/owner-library/originals/Blocks/Guns/models/special/<gun>/<part>.json),
  - a packed 128 x 128 texture atlas for each gun (.../Guns/item/<gun>.png),
  - Bedrock animations for each gun (.../Guns/item/<gun>.animation.json: draw, idle, shoot, aim_shoot, inspect and
    reload, or reload_start / reload_loop / reload_stop for a gun loaded a shell at a time),
  - the sounds (.../Guns/sounds/item/...).
The animations were made for GeckoLib models that were not supplied, so this module rebuilds those models from the
parts: one GeckoLib bone per animated part (gun_body, bolt, barrels, magazine ...), cube for cube and face for face,
plus the bones the animations move the player's arms by. GeckoLib then plays the owner's animations unchanged.

Run from the repository root:
  python3 tools/guns.py            write the GeckoLib models, animations, textures and sounds (committed; CI does not
                                   re-run this), then check them
  python3 tools/guns.py --check    only check: re-bake every model the way GeckoLib 5.5.7 does and compare each face,
                                   corner for corner, with the owner's part model it came from
tools/generate_material_data.py calls write_all() for the item definitions, names, recipes and sounds.json entries.
Java: guns/JugcraftGuns.java (the numbers) and guns/GunItem.java; client/guns/*. tools/check_mod_data.py keeps the
numbers here and in Java the same and runs check().
"""
import hashlib
import json
import math
import shutil
import sys
from pathlib import Path

MOD = "jugcraft"
ROOT = Path(__file__).resolve().parents[1]
LIBRARY = ROOT / "art" / "owner-library" / "originals" / "Blocks" / "Guns"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / MOD

# ------------------------------------------------------------------ the guns

# Each gun's numbers, read by Java through JugcraftGuns (check_guns in tools/check_mod_data.py keeps them equal):
#   damage   per bullet (per pellet for a shotgun), in half hearts
#   pellets  bullets per shot
#   interval ticks between shots (the fastest a trigger can be pulled, or the automatic rate)
#   auto     fires while the button is held
#   capacity rounds the magazine (or the barrels) hold
#   reload   ticks to change a magazine; for a gun loaded a shell at a time: (start, per shell, finish)
#   spread   degrees off the aim, (from the hip, aimed)
#   range    blocks
#   ammo     the round it fires
GUNS = {
    "rust_midge": {
        "display": "Rust Midge",
        "source": "rusty_gnat",
        "tooltip": "A copper machine pistol, scrap-built and quick. Hold to fire. Fires light rounds.",
        "damage": 2.0, "pellets": 1, "interval": 3, "auto": True, "capacity": 20,
        "reload": 47, "spread": (4.0, 1.5), "range": 48, "ammo": "light_round",
    },
    "patchwork_carbine": {
        "display": "Patchwork Carbine",
        "source": "makeshift_rifle",
        "tooltip": "A stockless carbine patched from scrap. One shot each pull. Fires rifle rounds.",
        "damage": 6.0, "pellets": 1, "interval": 6, "auto": False, "capacity": 10,
        "reload": 48, "spread": (2.0, 0.25), "range": 96, "ammo": "rifle_round",
    },
    "thunderpipe": {
        "display": "Thunderpipe",
        "source": "boomstick",
        "tooltip": "A sawn-off double barrel. Eight pellets a shot, loaded a shell at a time. Fires buckshot shells.",
        "damage": 2.5, "pellets": 8, "interval": 8, "auto": False, "capacity": 2,
        "reload": (5, 12, 13), "spread": (9.0, 6.0), "range": 24, "ammo": "buckshot_shell",
    },
    # Slice 2, the iron set: a step up from the scrap guns, in iron and brass.
    "warden_pistol": {
        "display": "Warden Pistol",
        "source": "defender_pistol",
        "tooltip": "An iron service pistol with a sliding breech, held in one hand. One shot each pull. Fires light rounds.",
        "damage": 4.0, "pellets": 1, "interval": 5, "auto": False, "capacity": 12,
        "reload": 47, "spread": (2.5, 0.75), "range": 56, "ammo": "light_round",
    },
    "riveter_smg": {
        "display": "Riveter SMG",
        "source": "greaser_smg",
        "tooltip": "An iron submachine gun that empties a long magazine in a hurry. Hold to fire. Fires light rounds.",
        "damage": 2.5, "pellets": 1, "interval": 3, "auto": True, "capacity": 30,
        "reload": 45, "spread": (3.0, 1.0), "range": 48, "ammo": "light_round",
    },
    "haymaker": {
        "display": "Haymaker",
        "source": "bruiser",
        "tooltip": "A short pump shotgun, fired one-handed. Eight pellets a shot, loaded a shell at a time. Fires buckshot "
                   "shells.",
        "damage": 3.0, "pellets": 8, "interval": 14, "auto": False, "capacity": 5,
        "reload": (7, 11, 23), "spread": (7.0, 5.0), "range": 28, "ammo": "buckshot_shell",
    },
    # Slice 3: two lever rifles, worked between shots and loaded a round at a time, and a break-open coach gun.
    "longhorn_rifle": {
        "display": "Longhorn Rifle",
        "source": "marlin",
        "tooltip": "A heavy lever-action rifle, worked between shots and loaded a round at a time. Fires rifle rounds.",
        "damage": 8.0, "pellets": 1, "interval": 13, "auto": False, "capacity": 6,
        "reload": (8, 11, 14), "spread": (1.5, 0.15), "range": 120, "ammo": "rifle_round",
    },
    "drover_rifle": {
        "display": "Drover Rifle",
        "source": "winnie",
        "tooltip": "A lever-action rifle with a long tube, worked between shots and loaded a round at a time. Fires rifle "
                   "rounds.",
        "damage": 6.5, "pellets": 1, "interval": 13, "auto": False, "capacity": 10,
        "reload": (8, 12, 17), "spread": (2.0, 0.3), "range": 100, "ammo": "rifle_round",
    },
    "coach_gun": {
        "display": "Coach Gun",
        "source": "callwell",
        "tooltip": "An over-and-under shotgun that breaks open to load. Eight pellets a shot, two barrels. Fires "
                   "buckshot shells.",
        "damage": 3.0, "pellets": 8, "interval": 8, "auto": False, "capacity": 2,
        "reload": (12, 13, 13), "spread": (6.0, 4.0), "range": 32, "ammo": "buckshot_shell",
    },
    # Slice 4: muzzle-loaders, one shot and then a long reload (powder, ball and ramrod), the earliest guns.
    "duelling_pistol": {
        "display": "Duelling Pistol",
        "source": "flintlock_pistol",
        "tooltip": "A flintlock pistol, loaded down the muzzle with powder and ball. One heavy shot, then a long reload. "
                   "Fires paper cartridges.",
        "damage": 9.0, "pellets": 1, "interval": 20, "auto": False, "capacity": 1,
        "reload": 74, "spread": (4.0, 2.0), "range": 32, "ammo": "paper_cartridge",
    },
    "line_musket": {
        "display": "Line Musket",
        "source": "musket",
        "tooltip": "A long flintlock musket, loaded down the muzzle and rammed home. One heavy shot, then a long reload. "
                   "Fires paper cartridges.",
        "damage": 14.0, "pellets": 1, "interval": 8, "auto": False, "capacity": 1,
        "reload": 78, "spread": (2.5, 0.75), "range": 64, "ammo": "paper_cartridge",
    },
    "bellmouth": {
        "display": "Bellmouth",
        "source": "blunderbuss",
        "tooltip": "A flintlock blunderbuss with a flared muzzle. Ten balls a shot at close range, then a long reload. "
                   "Fires paper cartridges.",
        "damage": 2.5, "pellets": 10, "interval": 10, "auto": False, "capacity": 1,
        "reload": 78, "spread": (12.0, 9.0), "range": 20, "ammo": "paper_cartridge",
    },
}

# The rounds: display name, tooltip, recipe (pattern, key, count). Cheap and early: copper or brass, lead and gunpowder.
AMMO = {
    "light_round": ("Light Round", "Copper-jacketed lead for small guns.",
                    (["C", "L", "G"], {"C": "minecraft:copper_ingot", "L": "#c:nuggets/lead", "G": "minecraft:gunpowder"}, 8)),
    "rifle_round": ("Rifle Round", "A brass case, a lead bullet and a full charge.",
                    (["B", "L", "G"], {"B": "#c:nuggets/brass", "L": "#c:nuggets/lead", "G": "minecraft:gunpowder"}, 4)),
    "buckshot_shell": ("Buckshot Shell", "A paper shell of lead shot over a brass head.",
                       (["PLP", "PGP", " B "], {"P": "minecraft:paper", "L": "#c:nuggets/lead", "G": "minecraft:gunpowder",
                                                "B": "#c:nuggets/brass"}, 4)),
    "paper_cartridge": ("Paper Cartridge", "A lead ball and its powder, wrapped in paper for a muzzle-loader.",
                        (["P", "L", "G"], {"P": "minecraft:paper", "L": "#c:nuggets/lead", "G": "minecraft:gunpowder"}, 4)),
}

# Gun recipes: crafted at a crafting table from early metal (the iron set adds brass); the owner's art carries the look.
RECIPES = {
    "rust_midge": (["CCI", " LI"], {"C": "minecraft:copper_ingot", "I": "minecraft:iron_ingot", "L": "minecraft:lever"}),
    "patchwork_carbine": (["III", "PLC"], {"I": "minecraft:iron_ingot", "P": "#minecraft:planks", "L": "minecraft:lever",
                                          "C": "minecraft:copper_ingot"}),
    "thunderpipe": (["II ", "PLP"], {"I": "minecraft:iron_ingot", "P": "#minecraft:planks", "L": "minecraft:lever"}),
    "warden_pistol": (["III", " LB"], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass"}),
    "riveter_smg": (["III", "BLI"], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass"}),
    "haymaker": (["III", "BLP"], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                  "P": "#minecraft:planks"}),
    "longhorn_rifle": (["IIB", "PL "], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                        "P": "#minecraft:planks"}),
    "drover_rifle": (["IIB", " LP"], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                      "P": "#minecraft:planks"}),
    "coach_gun": (["II ", "BLP"], {"I": "minecraft:iron_ingot", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                   "P": "#minecraft:planks"}),
    # The muzzle-loaders: iron, wood and a flint for the lock; no lever, no brass.
    "duelling_pistol": (["IIF", " P "], {"I": "minecraft:iron_ingot", "F": "minecraft:flint", "P": "#minecraft:planks"}),
    "line_musket": (["III", "FPP"], {"I": "minecraft:iron_ingot", "F": "minecraft:flint", "P": "#minecraft:planks"}),
    "bellmouth": (["CII", "FPP"], {"C": "minecraft:copper_ingot", "I": "minecraft:iron_ingot", "F": "minecraft:flint",
                                  "P": "#minecraft:planks"}),
}

# How each gun is built from the owner's parts, in the owner's model space (Java item-model pixels: x east, y up,
# z south; the muzzle points north, -z). Each bone: (name, parent, [parts], pivot). The pivots are where the
# animations turn each part about: the gun body turns about the grip in the right hand, the moving parts about
# their own centres (docs/features/guns.md has the survey of the animations that chose them).
# "hands": where the right hand holds the grip and the left hand holds the gun in the idle pose. The arm bones are
# children of gun_body, so the hands go where the gun goes; each one's pivot is the hand, placed so that the idle
# animation's offset brings it to these points (arm_pivot()). A one-handed gun's idle hides the left arm; its
# "hand_pose" names the animation and keyframe whose offset the left hand point is given for instead.
# "arms": which way each arm runs from the hand to the shoulder, in its arm bone's own frame (owner axes). The idle
# animation turns the arm bones so that their -y points straight back at the camera, which showed the arms end-on as
# big slabs; these run each arm down, back and out, so it rises from the bottom of the screen to the gun (chosen in a
# first-person preview of the idle pose, or of the "hand_pose"; docs/features/guns.md). The model carries each as a
# "<side>_shoulder" locator ARM_REACH pixels from the pivot, and the renderer turns the player's arm from -y onto it.
# "muzzle" and "sight": the locators the shot's smoke and aiming down the sights use.
BUILDS = {
    "rust_midge": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.2, 14.8)),
            ("gun_body", "gun_body2", ["main"], (8.0, 2.2, 14.8)),
            ("bolt", "gun_body", ["bolt"], (8.0, 4.65, 12.68)),
            ("barrels", "gun_body", ["barrel"], (8.0, 4.4, 8.32)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 2.39, 10.71)),
        ],
        "hands": {"right": (8.0, 2.2, 14.8), "left": (7.2, 0.6, 10.7)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.2847, -0.4787, 0.8305)},
        "muzzle": (8.05, 4.4, 5.47),
        "sight": (8.0, 5.95, 14.5),
    },
    "patchwork_carbine": {
        "bones": [
            ("gun_body", None, ["main", "stan_barrel"], (8.0, 1.2, 16.0)),
            ("bolt", "gun_body", ["bolt"], (9.43, 3.65, 11.0)),
            ("magazine", "gun_body", ["stan_mag"], (7.99, 3.47, 11.5)),
            ("magazine_2", "gun_body", [], (7.99, 3.47, 11.5)),
        ],
        "hands": {"right": (8.0, 1.2, 16.0), "left": (8.0, 2.8, 7.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.01, 4.06, 3.77),
        "sight": (8.0, 5.22, 15.0),
    },
    "thunderpipe": {
        "bones": [
            ("gun_body", None, ["main", "stan_grip"], (8.0, 1.6, 17.0)),
            ("barrels", "gun_body", ["barrel"], (8.0, 4.17, 12.38)),
            ("bolt", "gun_body", [], (8.0, 4.17, 12.38)),
            ("shell", "gun_body", ["@shell"], (9.0, 4.17, 14.4)),
        ],
        "hands": {"right": (8.0, 1.6, 17.0), "left": (8.0, 2.8, 9.5)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 4.17, 1.91),
        "sight": (8.0, 5.68, 14.0),
    },
    # The iron set. The Warden Pistol's slide is the owner's "receiver" part (the animations call it the bolt); its
    # reload drops the magazine and brings a new one in on magazine_2, so both carry the magazine part.
    "warden_pistol": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.9, 15.3)),
            ("gun_body", "gun_body2", ["main", "stan_barrel"], (8.0, 1.9, 15.3)),
            ("bolt", "gun_body", ["receiver"], (8.0, 5.38, 14.38)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 0.65, 15.05)),
            ("magazine_2", "gun_body", ["stan_mag"], (8.0, 0.65, 15.05)),
        ],
        "hands": {"right": (8.0, 1.9, 15.3), "left": (8.0, 0.35, 15.3)},
        "hand_pose": {"left": ("reload", "1.75")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.6771, -0.3794, 0.6306)},
        "muzzle": (8.0, 4.9, 6.9),
        "sight": (8.0, 6.0, 14.6),
    },
    "riveter_smg": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.45, 15.85)),
            ("gun_body", "gun_body2", ["main", "stan_barrel", "sights"], (8.0, 1.45, 15.85)),
            ("bolt", "gun_body", ["bolt"], (8.68, 5.33, 9.0)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 1.49, 11.0)),
        ],
        "hands": {"right": (8.0, 1.45, 15.85), "left": (8.0, 2.0, 8.5)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.1636, -0.6192, 0.768)},
        "muzzle": (8.0, 5.6, 4.0),
        "sight": (8.0, 6.85, 15.5),
    },
    # The Haymaker's pump is its barrel part: the shot and the pump slide it back.
    "haymaker": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.7, 15.75)),
            ("gun_body", "gun_body2", ["main"], (8.0, 1.7, 15.75)),
            ("barrel", "gun_body", ["barrel"], (8.0, 5.25, 10.5)),
        ],
        "hands": {"right": (8.0, 1.7, 15.75), "left": (8.0, 4.3, 9.0)},
        "hand_pose": {"left": ("reload_stop", "0.2917")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.5969, -0.4453, 0.6674)},
        "muzzle": (8.0, 5.1, 4.6),
        "sight": (8.0, 6.4, 14.7),
    },
    # Slice 3. The owner's lever rifles keep the lever loop in their main part, as one flat element (the Marlin's 8th,
    # the Winnie's 11th): it rides the lever bone, which turns about the loop's front where it meets the receiver.
    # The Marlin's reload carries a cartridge in on the shell bone (PROPS); the Winnie's and the Callwell's keep it
    # at scale 0, so their shell bones are empty.
    "longhorn_rifle": {
        "bones": [
            ("gun_body", None, ["main-#8", "stan_barrel", "sights"], (8.0, 1.7, 16.4)),
            ("lever", "gun_body", ["main#8"], (8.0, 2.0, 12.7)),
            ("bolt", "gun_body", ["bolt"], (8.0, 4.0, 14.66)),
            ("shell", "gun_body", ["@shell"], (8.8, 3.2, 13.5)),
        ],
        "hands": {"right": (8.0, 1.7, 16.4), "left": (8.0, 2.3, 8.0)},
        "arms": {"right": (-0.2762, -0.2601, 0.9253), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 3.76, -0.7),
        "sight": (8.0, 4.85, 10.86),
    },
    "drover_rifle": {
        "bones": [
            ("gun_body", None, ["main-#11", "stan_barrel", "sights", "hammer"], (8.0, 1.9, 16.4)),
            ("lever", "gun_body", ["main#11"], (8.0, 2.3, 12.9)),
            ("bolt", "gun_body", [], (8.0, 4.0, 14.5)),
            ("shell", "gun_body", [], (8.8, 3.2, 13.5)),
        ],
        "hands": {"right": (8.0, 1.9, 16.4), "left": (8.0, 2.3, 7.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 4.08, 1.05),
        "sight": (8.0, 4.98, 11.05),
    },
    # The Callwell's barrels and fore-end are the main part's elements the owner named "barrel": they ride the barrel
    # bone, which tips them open about the hinge at the front of the frame.
    "coach_gun": {
        "bones": [
            ("gun_body", None, ["main-@barrel"], (8.0, 1.9, 16.4)),
            ("barrel", "gun_body", ["main@barrel"], (8.0, 1.9, 11.6)),
            ("bolt", "gun_body", [], (8.0, 4.0, 14.5)),
            ("shell", "gun_body", [], (8.0, 3.4, 13.0)),
        ],
        "hands": {"right": (8.0, 1.9, 16.4), "left": (8.0, 1.5, 8.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 3.4, -1.95),
        "sight": (8.0, 5.25, 14.8),
    },
    # Slice 4. The muzzle-loaders' animations move a hammer (the owner's part, a flat cock on the lock's right side,
    # turning about its foot), and a ball, a ramrod and a priming flash that had no parts (PROPS; the Musket's flash is
    # the owner's part). Their bolt bones are empty. The Duelling Pistol's idle hides the left arm; its hand point is at
    # the muzzle, holding the ball, 0.71 s into the reload. The Bellmouth's left arm hangs from a "left_arm2" bone its
    # reload slides back.
    "duelling_pistol": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.7, 15.6)),
            ("gun_body", "gun_body2", ["main"], (8.0, 1.7, 15.6)),
            ("hammer", "gun_body", ["hammer"], (8.96, 3.6, 13.0)),
            ("bolt", "gun_body", [], (8.0, 3.77, 12.0)),
            ("ball", "gun_body", ["@ball"], (8.0, 3.47, 8.81)),
            ("ram", "gun_body", ["@ram"], (7.88, 1.9, 8.31)),
            ("flash", "gun_body", ["@flash"], (9.4, 4.5, 11.3)),
        ],
        "hands": {"right": (8.0, 1.7, 15.6), "left": (8.0, 4.6, 3.3)},
        "hand_pose": {"left": ("reload", "0.7083")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.28, -0.9592, 0.0384)},
        "muzzle": (8.0, 3.77, 3.0),
        "sight": (8.0, 4.95, 12.73),
    },
    "line_musket": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.9, 16.4)),
            ("gun_body", "gun_body2", ["main"], (8.0, 1.9, 16.4)),
            ("hammer", "gun_body", ["hammer"], (8.96, 3.5, 14.9)),
            ("bolt", "gun_body", [], (8.0, 3.92, 14.0)),
            ("ball", "gun_body", ["@ball"], (8.0, 2.92, 14.3)),
            ("ram", "gun_body", ["@ram"], (8.0, 2.92, 8.05)),
            ("flash", "gun_body", ["flash"], (9.0, 4.79, 13.04)),
        ],
        "hands": {"right": (8.0, 1.9, 16.4), "left": (8.0, 2.0, 8.0)},
        "arms": {"right": (-0.2762, -0.2276, 0.9338), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 3.92, 0.55),
        "sight": (8.0, 5.0, 14.74),
    },
    "bellmouth": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.9, 16.4)),
            ("gun_body", "gun_body2", ["main"], (8.0, 1.9, 16.4)),
            ("hammer", "gun_body", ["hammer"], (8.96, 3.6, 15.0)),
            ("bolt", "gun_body", [], (8.0, 3.66, 13.0)),
            ("ball", "gun_body", ["@ball"], (7.36, 3.07, 7.77)),
            ("ram", "gun_body", ["@ram"], (8.0, 2.66, 10.3)),
            ("flash", "gun_body", ["@flash"], (9.0, 4.9, 13.2)),
            ("left_arm2", "gun_body", [], (8.0, 1.4, 8.0)),
        ],
        "hands": {"right": (8.0, 1.9, 16.4), "left": (8.0, 1.4, 8.0)},
        "arm_parents": {"left": "left_arm2"},
        "arms": {"right": (-0.2762, -0.2276, 0.9338), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 3.66, 1.8),
        "sight": (8.0, 5.45, 12.41),
    },
}

# Props the animations move on bones that had no part ("@<name>" in a bone's parts): the rounds a reload carries in,
# a muzzle-loader's ball and ramrod and its priming flash. Each is one box drawn here into a free corner of the gun's
# atlas copy (the corner must be empty); the renderer shows it only while an animation moves it (GunRenderer).
#   kind "buckshot": a red paper hull with crimp lines and a brass head; "cartridge": a brass case with a lead tip;
#   "ball": a lead ball; "rod": an iron ramrod with a brass tip; "flash": a burst of priming fire.
# The Thunderpipe's 2 x 2 x 5 px shell rests at the breech it ends in (x 9, y 4.17, z 12.4..17.4) less the loop's last
# offset (render -0.278, -0.25, -7.515), so the loop slides it home. A muzzle-loader's ball rests where the reload's
# first hold puts it at the muzzle; its ramrod where the reload's farthest reach puts the rod's back end at the muzzle,
# in line with the bore (its strokes then drive it in); its flash at the pan beside the hammer.
PROPS = {
    "thunderpipe": {"shell": {"kind": "buckshot", "from": (8.278, 3.42, 19.9), "size": (2.0, 2.0, 5.0),
                              "texture_at": (112, 119)}},
    "longhorn_rifle": {"shell": {"kind": "cartridge", "from": (8.8, 2.7, 12.0), "size": (1.0, 1.0, 3.0),
                                 "texture_at": (119, 122)}},
    "duelling_pistol": {
        "ball": {"kind": "ball", "from": (7.5, 2.97, 8.31), "size": (1.0, 1.0, 1.0), "texture_at": (34, 61)},
        "ram": {"kind": "rod", "from": (7.63, 1.65, 5.31), "size": (0.5, 0.5, 6.0), "texture_at": (52, 50)},
        "flash": {"kind": "flash", "from": (8.9, 4.0, 10.8), "size": (1.0, 1.0, 1.0), "texture_at": (43, 58)},
    },
    "line_musket": {
        "ball": {"kind": "ball", "from": (7.5, 2.42, 13.8), "size": (1.0, 1.0, 1.0), "texture_at": (34, 61)},
        "ram": {"kind": "rod", "from": (7.75, 2.67, 2.05), "size": (0.5, 0.5, 12.0), "texture_at": (52, 50)},
    },
    "bellmouth": {
        "ball": {"kind": "ball", "from": (6.86, 2.57, 7.27), "size": (1.0, 1.0, 1.0), "texture_at": (34, 61)},
        "ram": {"kind": "rod", "from": (7.75, 2.41, 5.3), "size": (0.5, 0.5, 10.0), "texture_at": (52, 50)},
        "flash": {"kind": "flash", "from": (8.5, 4.4, 12.7), "size": (1.0, 1.0, 1.0), "texture_at": (43, 58)},
    },
}

# The sounds the animations name (sound_effects keys), per gun where they differ, and the gun's own shots.
# Each event: library file under Guns/sounds/ -> copied to assets/jugcraft/sounds/guns/.
EVENT_SOUNDS = {
    "gun_rustle": "item/gun_rustle/gun_rustle.ogg",
    "rustle": "item/gun_rustle/gun_rustle.ogg",
    "rack": "item/rack/rack.ogg",
    "bolt_release": "item/bolt_release/bolt_release.ogg",
    "bolt": "item/bolt/bolt.ogg",
    "bolt_pull": "item/bolt_pull/bolt_pull.ogg",
    "reload_mag_out": "item/mag_out/mag_out.ogg",
    "reload_mag_in": "item/mag_in/mag_in.ogg",
    "slap": "item/slap/slap.ogg",
    "reload_end": "item/reload_end/reload_end.ogg",
    "shell_in": "item/gun_sounds/insert.ogg",
    "dry_fire": "item/rusty_gnat/copper_jam.ogg",
    "lever": "item/lever/lever.ogg",
    "insert": "item/gun_sounds/insert.ogg",
    "metal": "item/gun_sounds/metal.ogg",
    "jam": "item/gun_sounds/metal.ogg",
}
# The shell-at-a-time guns' reload_loop names "reload_mag_in"; they push a shell or a round, so they play the insert.
EVENT_OVERRIDES = {"thunderpipe": {"reload_mag_in": "shell_in"}, "haymaker": {"reload_mag_in": "shell_in"},
                   "longhorn_rifle": {"reload_mag_in": "shell_in"}, "drover_rifle": {"reload_mag_in": "shell_in"},
                   "coach_gun": {"reload_mag_in": "shell_in"}}
SHOT_SOUNDS = {
    "rust_midge": "item/rusty_gnat/fire.ogg",
    "patchwork_carbine": "item/makeshift_rifle/fire.ogg",
    "thunderpipe": "item/boomstick/fire.ogg",
    "warden_pistol": "item/iron_pistol/fire.ogg",
    "riveter_smg": "item/greaser_smg/fire.ogg",
    "haymaker": "item/bruiser/fire.ogg",
    "longhorn_rifle": "item/heavy_rifle/fire.ogg",
    "drover_rifle": "item/cowboy/fire.ogg",
    "coach_gun": "item/brass_shotgun/fire.ogg",
    "duelling_pistol": "item/blackpowder/fire.ogg",
    "line_musket": "item/blackpowder/fire.ogg",
    "bellmouth": "item/blackpowder/fire.ogg",
}
SUBTITLES = {
    "fire": "Gun fires",
    "dry_fire": "Gun clicks empty",
    "gun_rustle": "Gun rustles",
    "rack": "Gun racks",
    "bolt_release": "Bolt releases",
    "bolt": "Bolt slides",
    "bolt_pull": "Bolt pulls",
    "reload_mag_out": "Magazine comes out",
    "reload_mag_in": "Magazine goes in",
    "slap": "Magazine slaps home",
    "reload_end": "Gun snaps shut",
    "shell_in": "Shell goes in",
    "lever": "Lever works",
    "insert": "Charge goes in",
    "metal": "Ramrod rings",
    "jam": "Ramrod rams home",
}


def items():
    return list(GUNS) + list(AMMO)


def sound_events():
    """Every sound event the guns register: guns.<event> for the shared ones and guns.<gun>.fire for each shot."""
    shared = sorted({name for name in EVENT_SOUNDS if name != "rustle"})
    return [f"guns.{name}" for name in shared] + [f"guns.{gun}.fire" for gun in GUNS]


def sound_file(path):
    """assets/jugcraft/sounds/guns/<flat name>.ogg for a library sound path."""
    return path.removeprefix("item/").removesuffix(".ogg").replace("/", "_")


SOUNDS = {}
for _name, _path in EVENT_SOUNDS.items():
    if _name != "rustle":
        SOUNDS[f"guns.{_name}"] = {"subtitle": f"subtitles.{MOD}.guns.{_name}",
                                   "sounds": [{"name": f"{MOD}:guns/{sound_file(_path)}"}]}
for _gun, _path in SHOT_SOUNDS.items():
    SOUNDS[f"guns.{_gun}.fire"] = {"subtitle": f"subtitles.{MOD}.guns.fire",
                                   "sounds": [{"name": f"{MOD}:guns/{sound_file(_path)}", "attenuation_distance": 64}]}


def write_all(write, assets, data, lang, condition):
    """Item definitions, the base models they draw with, names, tooltips and recipes (the GeckoLib files themselves
    are written by main())."""
    for gun, spec in GUNS.items():
        lang[f"item.{MOD}.{gun}"] = spec["display"]
        lang[f"tooltip.{MOD}.guns.{gun}"] = spec["tooltip"]
        # GeckoLib draws the gun through its special model renderer; the base model gives the display transforms
        # (the owner's, from item/<gun>.json) and the particle texture.
        write(assets / "models" / "item" / f"{gun}.json", base_model(gun))
        write(assets / "items" / f"{gun}.json", {"model": {
            "type": "minecraft:special", "base": f"{MOD}:item/{gun}", "model": {"type": "geckolib:geckolib"}}})
        pattern, key = RECIPES[gun]
        write(data / "recipe" / f"{gun}.json", {
            "fabric:load_conditions": condition("guns"), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{gun}"}})
    for ammo, (display, tooltip, (pattern, key, count)) in AMMO.items():
        lang[f"item.{MOD}.{ammo}"] = display
        lang[f"tooltip.{MOD}.guns.{ammo}"] = tooltip
        write(assets / "models" / "item" / f"{ammo}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{ammo}"}})
        write(assets / "items" / f"{ammo}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{ammo}"}})
        write(data / "recipe" / f"{ammo}.json", {
            "fabric:load_conditions": condition("guns"), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{ammo}", "count": count}})
    for name, text in SUBTITLES.items():
        lang[f"subtitles.{MOD}.guns.{name}"] = text
    lang[f"key.{MOD}.reload"] = "Reload gun"
    lang[f"key.{MOD}.inspect"] = "Inspect gun"
    lang[f"tooltip.{MOD}.guns.ammo"] = "Loaded: %s / %s"
    lang[f"tooltip.{MOD}.guns.stats"] = "Damage %s x %s, %s shots a second, range %s"
    lang[f"hud.{MOD}.guns.ammo"] = "%s / %s"
    lang[f"hud.{MOD}.guns.reloading"] = "Reloading"
    lang[f"message.{MOD}.guns.no_ammo"] = "No %s to load."
    lang[f"death.attack.{MOD}.bullet"] = "%1$s was shot by %2$s"
    lang[f"death.attack.{MOD}.bullet.item"] = "%1$s was shot by %2$s using %3$s"
    lang[f"death.attack.{MOD}.bullet.player"] = "%1$s was shot while fighting %2$s"
    # A bullet is a projectile: Projectile Protection guards against it.
    write(data / "damage_type" / "bullet.json",
          {"message_id": f"{MOD}.bullet", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"})
    write(data.parent / "minecraft" / "tags" / "damage_type" / "is_projectile.json",
          {"replace": False, "values": [f"{MOD}:bullet"]})
    # Guns fire faster than the half second a creature is shielded after a hit: each shot counts.
    write(data.parent / "minecraft" / "tags" / "damage_type" / "bypasses_cooldown.json",
          {"replace": False, "values": [f"{MOD}:bullet"]})


def draw_all(save):
    """The rounds' 16 x 16 icons, from their maps in tools/item_icons/ (docs/ITEM_ICONS.md). The guns themselves show
    their 3D model in every slot, as the owner's do."""
    import item_icons
    for ammo in AMMO:
        save(item_icons.draw(ammo), "item", ammo)


def base_model(gun):
    """The owner's display transforms for the gun (third person, ground, gui, fixed; first person is set by the gun
    renderer from the same numbers) with the gun's atlas as the particle texture."""
    source = json.loads((LIBRARY / "models" / "item" / f"{GUNS[gun]['source']}.json").read_text())
    return {"textures": {"particle": f"{MOD}:item/guns/{gun}"}, "display": source["display"]}


# ------------------------------------------------------------------ converting the parts

def load_part(gun, part):
    return json.loads((LIBRARY / "models" / "special" / GUNS[gun]["source"] / f"{part_file(part)}.json").read_text())


def part_file(part):
    """A bone's part entry names the owner's part file, optionally narrowed to some of its elements:
    "main@barrel" (the elements the owner named "barrel"), "main#8" (element 8), and "main-@barrel", "main-#8" (all
    but those). Several numbers are comma-separated: "main#8,9"."""
    return part.split("@")[0].split("#")[0].removesuffix("-")


def part_elements(gun, part):
    """The owner's elements a bone's part entry stands for, in the file's order."""
    elements = load_part(gun, part).get("elements", [])
    rest = part[len(part_file(part)):]
    if not rest:
        return elements
    exclude = rest.startswith("-")
    rest = rest.removeprefix("-")
    if rest.startswith("@"):
        keep = [e.get("name") == rest[1:] for e in elements]
    else:
        wanted = {int(n) for n in rest[1:].split(",")}
        keep = [i in wanted for i in range(len(elements))]
    return [e for e, k in zip(elements, keep) if k != exclude]


def geo_point(p):
    """Owner model pixels -> GeckoLib (Bedrock) model units: x mirrored about the centre line, z about the centre."""
    return [rnd(8.0 - p[0]), rnd(p[1]), rnd(p[2] - 8.0)]


FACES = ("north", "east", "south", "west", "up", "down")
# Vanilla FaceInfo: each face's four corners as (x, y, z) picks of (min, max), in UV order 0..3.
JAVA_CORNERS = {
    "down": [(0, 0, 1), (0, 0, 0), (1, 0, 0), (1, 0, 1)],
    "up": [(0, 1, 0), (0, 1, 1), (1, 1, 1), (1, 1, 0)],
    "north": [(1, 1, 0), (1, 0, 0), (0, 0, 0), (0, 1, 0)],
    "south": [(0, 1, 1), (0, 0, 1), (1, 0, 1), (1, 1, 1)],
    "west": [(0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)],
    "east": [(1, 1, 1), (1, 0, 1), (1, 0, 0), (1, 1, 0)],
}
# GeckoLib's VertexSet quads (cache VertexSet.quadWest() ...): the corners in the order it gives them UVs.
GECKO_CORNERS = {
    "west": [(0, 1, 1), (0, 1, 0), (0, 0, 0), (0, 0, 1)],
    "east": [(1, 1, 0), (1, 1, 1), (1, 0, 1), (1, 0, 0)],
    "north": [(0, 1, 0), (1, 1, 0), (1, 0, 0), (0, 0, 0)],
    "south": [(1, 1, 1), (0, 1, 1), (0, 0, 1), (1, 0, 1)],
    "up": [(0, 1, 1), (1, 1, 1), (1, 1, 0), (0, 1, 0)],
    "down": [(0, 0, 0), (1, 0, 0), (1, 0, 1), (0, 0, 1)],
}


def default_uv(face, lo, hi):
    """Vanilla's UVs for a face without "uv" (BlockElement.uvsByFace)."""
    return {
        "down": [lo[0], 16 - hi[2], hi[0], 16 - lo[2]], "up": [lo[0], lo[2], hi[0], hi[2]],
        "north": [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]], "south": [lo[0], 16 - hi[1], hi[0], 16 - lo[1]],
        "west": [lo[2], 16 - hi[1], hi[2], 16 - lo[1]], "east": [16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]],
    }[face]


def java_corner_uvs(face, uv, rotation):
    """{corner pick: (u, v)} in 0..16 units, as vanilla's BlockFaceUV gives them, rotation included."""
    out = {}
    for i, pick in enumerate(JAVA_CORNERS[face]):
        j = (i + rotation // 90) % 4
        out[pick] = (uv[0] if j in (0, 1) else uv[2], uv[1] if j in (0, 3) else uv[3])
    return out


def gecko_corner_uvs(face, u, v, us, vs, rotation, size_px):
    """{corner pick: (u, v)} in 0..16 units for a GeckoLib per-face UV (GeometryQuadUvs, not mirrored)."""
    w, h = size_px
    lo_u, hi_u, lo_v, hi_v = (u + us) / w, u / w, v / h, (v + vs) / h  # non-mirrored faces swap u and u + width
    uvs = {0: [lo_u, lo_v, hi_u, lo_v, hi_u, hi_v, lo_u, hi_v],
           90: [hi_u, lo_v, hi_u, hi_v, lo_u, hi_v, lo_u, lo_v],
           180: [hi_u, hi_v, lo_u, hi_v, lo_u, lo_v, hi_u, lo_v],
           270: [lo_u, hi_v, lo_u, lo_v, hi_u, lo_v, hi_u, hi_v]}[rotation]
    return {pick: (uvs[2 * i] * 16, uvs[2 * i + 1] * 16) for i, pick in enumerate(GECKO_CORNERS[face])}


def gecko_face(face, corners, size_px):
    """The GeckoLib per-face UV (uv, uv_size, uv_rotation) giving the same UV at each corner as vanilla does."""
    w, h = size_px
    us16 = sorted({c[0] for c in corners.values()})
    vs16 = sorted({c[1] for c in corners.values()})
    for rotation in (0, 90, 180, 270):
        for ua, ub in ((us16[0], us16[-1]), (us16[-1], us16[0])):
            for va, vb in ((vs16[0], vs16[-1]), (vs16[-1], vs16[0])):
                u, v = ua * w / 16, va * h / 16
                us, vs = (ub - ua) * w / 16, (vb - va) * h / 16
                got = gecko_corner_uvs(face, u, v, us, vs, rotation, size_px)
                if all(close(got[k], corners[k]) for k in corners):
                    out = {"uv": [rnd(u), rnd(v)], "uv_size": [rnd(us), rnd(vs)]}
                    if rotation:
                        out["uv_rotation"] = rotation
                    return out
    raise ValueError(f"no GeckoLib UV reproduces the {face} face {corners}")


def close(a, b, eps=1e-5):
    return all(abs(x - y) <= eps for x, y in zip(a, b))


def rnd(x):
    """Six decimals: the owner's models use up to five, so nothing they hold is lost."""
    x = round(x, 6) + 0.0
    return int(x) if x == int(x) else x


def element_cube(element, size_px):
    """One owner element -> one GeckoLib cube with the same corners, the same UV at each corner and the same turn."""
    lo = [min(a, b) for a, b in zip(element["from"], element["to"])]
    hi = [max(a, b) for a, b in zip(element["from"], element["to"])]
    size = [rnd(b - a) for a, b in zip(lo, hi)]
    cube = {"origin": [rnd(8.0 - hi[0]), rnd(lo[1]), rnd(lo[2] - 8.0)], "size": size}
    rotation = element.get("rotation")
    if rotation and rotation.get("angle"):
        if rotation.get("rescale"):
            raise ValueError("rescaled element rotations are not supported")
        angle = rotation["angle"]
        cube["pivot"] = geo_point(rotation.get("origin", [8, 8, 8]))
        # GeckoLib turns a cube by (-x, -y, z) of its "rotation" (GeometryCube.bake); vanilla turns by +angle.
        cube["rotation"] = {"x": [-angle, 0, 0], "y": [0, -angle, 0], "z": [0, 0, angle]}[rotation["axis"]]
        cube["rotation"] = [rnd(v) for v in cube["rotation"]]
    faces = {}
    for face in FACES:
        data = element.get("faces", {}).get(face)
        if data is None:
            continue
        uv = data.get("uv") or default_uv(face, lo, hi)
        corners = java_corner_uvs(face, uv, int(data.get("rotation", 0)) % 360)
        faces[face] = gecko_face(face, corners, size_px)
    cube["uv"] = faces
    return cube


def prop_dims(prop):
    """A prop's faces in whole texture pixels (a half-pixel rod still takes one)."""
    return tuple(max(1, int(round(v))) for v in prop["size"])


def prop_cube(gun, name):
    prop = PROPS[gun][name]
    x, y, z = prop["from"]
    sx, sy, sz = prop["size"]
    w, h, d = prop_dims(prop)
    tu, tv = prop["texture_at"]
    # One texture block per face (prop_block): the ends side by side, the sides below them, the top and bottom below.
    return {"origin": [rnd(8.0 - (x + sx)), rnd(y), rnd(z - 8.0)], "size": [sx, sy, sz], "uv": {
        "north": {"uv": [tu, tv], "uv_size": [w, h]}, "south": {"uv": [tu + w, tv], "uv_size": [w, h]},
        "east": {"uv": [tu, tv + h], "uv_size": [d, h]}, "west": {"uv": [tu, tv + h], "uv_size": [d, h]},
        "up": {"uv": [tu, tv + 2 * h], "uv_size": [w, d]}, "down": {"uv": [tu + w, tv + 2 * h], "uv_size": [w, d]}}}


def prop_block(prop):
    """The atlas block (width, height) a prop's faces take."""
    w, h, d = prop_dims(prop)
    return max(2 * w, d), 2 * h + d


def key_value(channel, time=None):
    """A channel's value: a constant ([x, y, z] or {"post": ...}), or its keyframe at `time` (a key of the file; the
    first when no time is given)."""
    if isinstance(channel, dict) and not {"post", "vector", "pre"} & channel.keys():
        # Keyframes: the one named, or the first of an idle that holds still.
        channel = channel[time if time is not None else min(channel, key=float)]
    if isinstance(channel, dict):
        channel = channel.get("post", channel.get("vector", channel.get("pre")))
    return [float(v) for v in channel]


def arm_offset(gun, side):
    """The arm's animated position, in the file's units, in the pose its hand point is given for: the idle (constant),
    or the "hand_pose" keyframe for an arm the idle hides."""
    anim, time = BUILDS[gun].get("hand_pose", {}).get(side, ("idle", None))
    return key_value(animations(gun)["animations"][anim]["bones"][f"{side}_arm"]["position"], time)


def arm_pivot(gun, side):
    """Owner-space rest pivot of an arm bone: the hand point less the pose's offset (GeckoLib moves a bone by
    (-x, y, z) of its "position", BoneSnapshot.translate)."""
    hx, hy, hz = BUILDS[gun]["hands"][side]
    ox, oy, oz = arm_offset(gun, side)
    return (hx + ox, hy - oy, hz - oz)


ARM_REACH = 10.0


def shoulder(gun, side):
    """Owner-space rest point of an arm's shoulder locator: ARM_REACH pixels from the arm bone's pivot along "arms"."""
    x, y, z = BUILDS[gun]["arms"][side]
    norm = math.sqrt(x * x + y * y + z * z)
    px, py, pz = arm_pivot(gun, side)
    return (px + ARM_REACH * x / norm, py + ARM_REACH * y / norm, pz + ARM_REACH * z / norm)


def build_geo(gun):
    build = BUILDS[gun]
    size_px = atlas_size(gun)
    bones = []
    for name, parent, parts, pivot in build["bones"]:
        bone = {"name": name, "pivot": geo_point(pivot)}
        if parent:
            bone["parent"] = parent
        cubes = []
        for part in parts:
            if part.startswith("@"):
                cubes.append(prop_cube(gun, part[1:]))
                continue
            for element in part_elements(gun, part):
                cubes.append(element_cube(element, size_px))
        if cubes:
            bone["cubes"] = cubes
        if name == "gun_body":
            bone["locators"] = {"sight": geo_point(build["sight"])}
        if name in ("barrels",) or (name == "gun_body" and not any(b[0] == "barrels" for b in build["bones"])):
            bone["locators"] = {**bone.get("locators", {}), "muzzle": geo_point(build["muzzle"])}
        bones.append(bone)
    for side in ("right", "left"):
        parent = build.get("arm_parents", {}).get(side, "gun_body")
        bones.append({"name": f"{side}_arm", "parent": parent, "pivot": geo_point(arm_pivot(gun, side)),
                      "locators": {f"{side}_shoulder": geo_point(shoulder(gun, side))}})
    return {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": f"geometry.{MOD}.{gun}", "texture_width": size_px[0],
                        "texture_height": size_px[1], "visible_bounds_width": 4, "visible_bounds_height": 3,
                        "visible_bounds_offset": [0, 0.5, 0]},
        "bones": bones}]}


def atlas_size(gun):
    from PIL import Image
    with Image.open(LIBRARY / "item" / f"{GUNS[gun]['source']}.png") as image:
        return image.size


def animations(gun):
    return json.loads((LIBRARY / "item" / f"{GUNS[gun]['source']}.animation.json").read_text())


# ------------------------------------------------------------------ checking: bake as GeckoLib does

def bake_cube(cube, size_px):
    """GeckoLib 5.5.7 GeometryCube.bake + VertexSet + GeometryQuadUvs, in owner model pixels: returns
    [(face, {owner-space corner (x, y, z): (u, v) in 0..16})] for the cube unturned, and its turn."""
    ox, oy, oz = cube["origin"]
    sx, sy, sz = cube["size"]
    # GeckoLib's render-space origin is (-(x + size x), y, z); back in owner pixels that is 8 + that, y, z + 8.
    lo = (8.0 - (ox + sx), oy, oz + 8.0)
    hi = (lo[0] + sx, lo[1] + sy, lo[2] + sz)
    out = []
    for face, data in cube.get("uv", {}).items():
        if zero_face(cube["size"], face):
            continue
        rotation = data.get("uv_rotation", 0)
        corners = gecko_corner_uvs(face, data["uv"][0], data["uv"][1], data["uv_size"][0], data["uv_size"][1],
                                   rotation, size_px)
        out.append((face, [(tuple((lo, hi)[s][k] for k, s in enumerate(pick)), uv) for pick, uv in corners.items()]))
    return out


def zero_face(size, face):
    """GeometryCube.isZeroSizeFace: a flat cube keeps only the two faces across its flat axis."""
    axis = {"west": 0, "east": 0, "up": 1, "down": 1, "north": 2, "south": 2}[face]
    for k in range(3):
        if size[k] == 0:
            return axis != k
    return False


def java_faces(element):
    lo = [min(a, b) for a, b in zip(element["from"], element["to"])]
    hi = [max(a, b) for a, b in zip(element["from"], element["to"])]
    out = []
    for face in FACES:
        data = element.get("faces", {}).get(face)
        if data is None:
            continue
        if zero_face([b - a for a, b in zip(lo, hi)], face):
            continue  # GeckoLib drops these edge-on faces, and they draw nothing in vanilla either
        uv = data.get("uv") or default_uv(face, lo, hi)
        corners = java_corner_uvs(face, uv, int(data.get("rotation", 0)) % 360)
        out.append((face, [(tuple((lo, hi)[s][k] for k, s in enumerate(pick)), uv) for pick, uv in corners.items()]))
    return out


def same_corners(got, exp):
    """Each corner of one face found at the same place, with the same UV, in the other (to 1e-5 px)."""
    return len(got) == len(exp) and all(any(close(p, q) and close(a, b) for q, b in exp) for p, a in got)


def check():
    """Every owner face must come back from the GeckoLib model at the same corners with the same UVs and the same turn
    about the same pivot; the arm bones and locators must be where BUILDS puts them. Returns problems."""
    problems = []
    for gun in GUNS:
        geo_path = ASSETS / "geckolib" / "models" / "item" / f"{gun}.geo.json"
        if not geo_path.exists():
            problems.append(f"{gun}: {geo_path.relative_to(ROOT)} is missing (run python3 tools/guns.py)")
            continue
        geo = json.loads(geo_path.read_text())
        if geo != build_geo(gun):
            problems.append(f"{gun}: the GeckoLib model is out of date (run python3 tools/guns.py)")
        size_px = atlas_size(gun)
        bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
        for name, parent, parts, pivot in BUILDS[gun]["bones"]:
            cubes = []
            want = []
            found = list(bones.get(name, {}).get("cubes", []))
            for part in parts:
                if part.startswith("@"):
                    found = found[1:]  # a prop, drawn here, not the owner's
                    continue
                elements = part_elements(gun, part)
                cubes += found[:len(elements)]
                found = found[len(elements):]
                want += elements
            cubes += found  # any left over are reported as a count mismatch
            if len(cubes) != len(want):
                problems.append(f"{gun}/{name}: {len(cubes)} cubes for {len(want)} owner elements")
                continue
            for i, (cube, element) in enumerate(zip(cubes, want)):
                got = bake_cube(cube, size_px)
                exp = java_faces(element)
                if sorted(f for f, _ in got) != sorted(f for f, _ in exp):
                    problems.append(f"{gun}/{name} element {i}: faces {sorted(f for f, _ in got)} != "
                                    f"{sorted(f for f, _ in exp)}")
                    continue
                for (face, g), (_, e) in zip(sorted(got), sorted(exp)):
                    if not same_corners(g, e):
                        problems.append(f"{gun}/{name} element {i} {face}: corners or UVs differ")
                rotation = element.get("rotation")
                if rotation and rotation.get("angle"):
                    turn = [0.0, 0.0, 0.0]
                    k = "xyz".index(rotation["axis"])
                    turn[k] = -rotation["angle"] if k < 2 else rotation["angle"]
                    pivot_back = (8.0 - cube["pivot"][0], cube["pivot"][1], cube["pivot"][2] + 8.0)
                    if not close(cube["rotation"], turn) or not close(pivot_back, rotation.get("origin", [8, 8, 8])):
                        problems.append(f"{gun}/{name} element {i}: turn differs")
                elif cube.get("rotation"):
                    problems.append(f"{gun}/{name} element {i}: turned but the owner's is not")
        for side in ("right", "left"):
            bone = bones.get(f"{side}_arm")
            if bone is None:
                problems.append(f"{gun}: no {side}_arm bone")
                continue
            ox, oy, oz = arm_offset(gun, side)
            px, py, pz = 8.0 - bone["pivot"][0], bone["pivot"][1], bone["pivot"][2] + 8.0
            if not close((px - ox, py + oy, pz + oz), BUILDS[gun]["hands"][side], 1e-3):
                problems.append(f"{gun}: the {side} hand is not where BUILDS puts it in its pose")
            sx, sy, sz = bone.get("locators", {}).get(f"{side}_shoulder", (8.0, 0.0, -8.0))
            reach = (8.0 - sx - px, sy - py, sz + 8.0 - pz)
            length = math.sqrt(sum(v * v for v in reach))
            if abs(length - ARM_REACH) > 1e-3 or reach[1] >= 0:
                problems.append(f"{gun}: the {side} shoulder locator is not {ARM_REACH} px below the hand")
        anim = ASSETS / "geckolib" / "animations" / "item" / f"{gun}.animation.json"
        source = LIBRARY / "item" / f"{GUNS[gun]['source']}.animation.json"
        if not anim.exists() or anim.read_bytes() != source.read_bytes():
            problems.append(f"{gun}: the animation is not the owner's file unchanged")
        for name, data in animations(gun)["animations"].items():
            for bone in data.get("bones", {}):
                if bone not in bones:
                    problems.append(f"{gun}: animation {name} moves {bone}, which the model lacks")
            for event in (data.get("sound_effects") or {}).values():
                if event["effect"] not in EVENT_SOUNDS:
                    problems.append(f"{gun}: animation {name} plays {event['effect']}, which has no sound")
    for name, path in {**EVENT_SOUNDS, **{f"{g}.fire": p for g, p in SHOT_SOUNDS.items()}}.items():
        target = ASSETS / "sounds" / "guns" / f"{sound_file(path)}.ogg"
        if not target.exists() or target.read_bytes() != (LIBRARY / "sounds" / path).read_bytes():
            problems.append(f"sound {name}: {target.relative_to(ROOT)} is not the library file unchanged")
    return problems


# ------------------------------------------------------------------ writing the GeckoLib files

def dump(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=1) + "\n")


def draw_props(atlas, gun):
    """Each prop's block of the atlas copy, in a corner the owner's atlas leaves empty."""
    for name, prop in PROPS[gun].items():
        tu, tv = prop["texture_at"]
        bw, bh = prop_block(prop)
        if any(atlas.getpixel((tu + x, tv + y))[3] for x in range(max(bw, 8)) for y in range(bh)):
            raise ValueError(f"{gun}: the atlas is not empty at {tu},{tv} for its {name}")
        {"buckshot": draw_buckshot, "cartridge": draw_cartridge}.get(prop["kind"], draw_plain)(atlas, prop)
    return atlas


def draw_buckshot(atlas, prop):
    """A 2 x 2 x 5 red paper hull with darker crimp lines and a brass head."""
    tu, tv = prop["texture_at"]
    red, red_dark, brass, brass_dark = (178, 44, 36, 255), (122, 28, 26, 255), (214, 170, 72, 255), (150, 112, 44, 255)
    for x in range(2):
        for y in range(2):
            atlas.putpixel((tu + x, tv + y), red_dark if (x + y) % 2 else red)      # north: the crimped end
            atlas.putpixel((tu + 2 + x, tv + y), brass if (x + y) % 2 else brass_dark)  # south: the brass head
    for x in range(5):
        for y in range(2):  # east and west: hull, brass at the back two pixels
            atlas.putpixel((tu + x, tv + 2 + y), (brass if y == 0 else brass_dark) if x >= 3 else
                           (red if y == 0 else red_dark))
    for z in range(5):
        for x in range(4):  # up (u 0-1) and down (u 2-3): hull, brass at the back
            atlas.putpixel((tu + x, tv + 4 + z), (brass if x % 2 == 0 else brass_dark) if z >= 3 else
                           (red if x % 2 == 0 else red_dark))
    return atlas


def draw_cartridge(atlas, prop):
    """A brass case with a lead tip at the front (north) end, the case's head at the back a darker ring."""
    tu, tv = prop["texture_at"]
    w, h, d = prop_dims(prop)
    lead, brass, brass_dark = (120, 124, 132, 255), (214, 170, 72, 255), (150, 112, 44, 255)
    for x in range(w):
        for y in range(h):
            atlas.putpixel((tu + x, tv + y), lead)               # north: the bullet's tip
            atlas.putpixel((tu + w + x, tv + y), brass_dark)     # south: the case head
    for z in range(d):  # the sides (u along the length) and the top and bottom (v along it): tip, case, head
        colour = lead if z == 0 else brass_dark if z == d - 1 else brass
        for y in range(h):
            atlas.putpixel((tu + z, tv + h + y), colour)
        for x in range(2 * w):
            atlas.putpixel((tu + x, tv + 2 * h + z), colour)
    return atlas


PLAIN = {
    # kind: (front end, body, back end), each a light and a dark tone checkered
    "ball": (((132, 136, 146, 255), (98, 102, 112, 255)),) * 3,
    "rod": (((214, 170, 72, 255), (150, 112, 44, 255)), ((122, 124, 132, 255), (86, 88, 96, 255)),
            ((122, 124, 132, 255), (86, 88, 96, 255))),
    "flash": (((255, 226, 110, 255), (246, 150, 52, 255)),) * 3,
}


def draw_plain(atlas, prop):
    """A ball, rod or flash: the front end's tone at the north face and the first pixel of the length, the back end's
    at the south face and the last, the body's between; light and dark checkered."""
    tu, tv = prop["texture_at"]
    w, h, d = prop_dims(prop)
    front, body, back = PLAIN[prop["kind"]]
    def tone(part, a, b):
        return part[(a + b) % 2]
    for x in range(w):
        for y in range(h):
            atlas.putpixel((tu + x, tv + y), tone(front, x, y))
            atlas.putpixel((tu + w + x, tv + y), tone(back, x, y))
    for z in range(d):
        part = front if z == 0 else back if z == d - 1 else body
        for y in range(h):
            atlas.putpixel((tu + z, tv + h + y), tone(part, z, y))
        for x in range(2 * w):
            atlas.putpixel((tu + x, tv + 2 * h + z), tone(part, x, z))
    return atlas


def write_files():
    from PIL import Image
    for gun, spec in GUNS.items():
        dump(ASSETS / "geckolib" / "models" / "item" / f"{gun}.geo.json", build_geo(gun))
        target = ASSETS / "geckolib" / "animations" / "item" / f"{gun}.animation.json"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "item" / f"{spec['source']}.animation.json", target)
        texture = ASSETS / "textures" / "item" / "guns" / f"{gun}.png"
        texture.parent.mkdir(parents=True, exist_ok=True)
        if gun in PROPS:
            with Image.open(LIBRARY / "item" / f"{spec['source']}.png") as image:
                draw_props(image.convert("RGBA"), gun).save(texture)
        else:
            shutil.copyfile(LIBRARY / "item" / f"{spec['source']}.png", texture)
    for path in sorted({*EVENT_SOUNDS.values(), *SHOT_SOUNDS.values()}):
        target = ASSETS / "sounds" / "guns" / f"{sound_file(path)}.ogg"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "sounds" / path, target)


def provenance():
    """Library path and SHA-256 of every owner file the guns use, for docs/features/guns.md."""
    rows = []
    for gun, spec in GUNS.items():
        src = spec["source"]
        paths = [f"models/item/{src}.json", f"item/{src}.png", f"item/{src}.animation.json"]
        for _, _, parts, _ in BUILDS[gun]["bones"]:
            paths += [f"models/special/{src}/{part_file(p)}.json" for p in parts if not p.startswith("@")]
        paths.append(f"sounds/{SHOT_SOUNDS[gun]}")
        rows += [(gun, p) for p in dict.fromkeys(paths)]  # a part on two bones (a spare magazine) counts once
    rows += [("shared", f"sounds/{p}") for p in sorted(set(EVENT_SOUNDS.values()))]
    return [(gun, path, hashlib.sha256((LIBRARY / path).read_bytes()).hexdigest()) for gun, path in rows]


if __name__ == "__main__":
    if "--provenance" in sys.argv:
        for gun, path, digest in provenance():
            print(f"| {gun} | `Guns/{path}` | `{digest[:16]}` |")
        sys.exit(0)
    if "--check" not in sys.argv:
        write_files()
    found = check()
    for problem in found:
        print("guns:", problem)
    print("guns: PASS" if not found else f"guns: {len(found)} problem(s)")
    sys.exit(1 if found else 0)
