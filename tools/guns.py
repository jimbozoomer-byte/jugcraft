"""Guns (docs/features/guns.md): slice 1, the owner's Rust Midge, Patchwork Carbine and Thunderpipe; slice 2, the
iron set: the Warden Pistol, Riveter SMG and Haymaker; slice 3, the Longhorn Rifle, Drover Rifle and Coach Gun;
slice 4, the black powder guns: the Duelling Pistol, Line Musket and Bellmouth; slice 5, the attachments; slice 6,
the guns in use: muzzle flash, spent casings, the view narrowed while aiming and the two-handed hold seen from outside;
slice 7, the bayonets and the attachments drawn on shared textures, then the scopes (the owner's scope models, mounted
on the guns made to take them); slice 8, the hand guns: the Bulldog Pistol, Marshal Revolver and Sapper Revolver;
slice 8B, the service arms in steel: the Sentry Pistol, Garrison Rifle and Breacher; slice 8C, the heavy weapons: the
Trench Lobber, Thresher and Stoker; slice 8D, the energy weapons on Energy Cells: the Beam Pistol, Stormlock Rifle and
Linesman; slice 9A, the marksman rifles: the Picket Rifle, Ranger Rifle and Kestrel Rifle; slice 9B, the automatic
weapons: the Rattler Pistol, Bronco SMG and Squall Rifle.

The owner made these guns (inspired by Scorched Guns 2) and supplied, in the owner asset library:
  - a Blockbench Java model of every part (art/owner-library/originals/Blocks/Guns/models/special/<gun>/<part>.json),
  - a packed 128 x 128 texture atlas for each gun (.../Guns/item/<gun>.png),
  - Bedrock animations for each gun (.../Guns/item/<gun>.animation.json: draw, idle, shoot, aim_shoot, inspect and
    reload, or reload_start / reload_loop / reload_stop for a gun loaded a shell at a time),
  - the sounds (.../Guns/sounds/item/...),
  - casing art (.../Guns/item/<casing>.png) and muzzle flash frames
    (art/owner-library/originals/Blocks/Big Cannons and Mounted Guns/textures/muzzleflash*.png),
  - the attachments' item models (.../Guns/models/item/<attachment>.json), and the reticles and lens vignettes the
    scope models draw (.../Guns/effect/<name>.png, uploaded 8 October 2026).
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
BLOCKS = ROOT / "art" / "owner-library" / "originals" / "Blocks"
LIBRARY = BLOCKS / "Guns"
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
    # Slice 8: a hand cannon and two revolvers, each held in one hand. The Bulldog breaks open to load its one heavy
    # round; the Marshal loads through its gate a round at a time; the Sapper's cylinder swings out to load.
    "bulldog_pistol": {
        "display": "Bulldog Pistol",
        "source": "brawler",
        "tooltip": "A one-handed hand cannon that breaks open to load its one heavy round. Fires rifle rounds.",
        "damage": 11.0, "pellets": 1, "interval": 10, "auto": False, "capacity": 1,
        "reload": 45, "spread": (3.0, 1.0), "range": 56, "ammo": "rifle_round",
    },
    "marshal_revolver": {
        "display": "Marshal Revolver",
        "source": "longarm",
        "tooltip": "A long-barrelled revolver, loaded through its gate a round at a time. Fires light rounds.",
        "damage": 5.0, "pellets": 1, "interval": 8, "auto": False, "capacity": 6,
        "reload": (11, 25, 11), "spread": (2.0, 0.5), "range": 72, "ammo": "light_round",
    },
    "sapper_revolver": {
        "display": "Sapper Revolver",
        "source": "trenchur",
        "tooltip": "A short revolver whose cylinder swings out to load a round at a time. Fires light rounds.",
        "damage": 4.5, "pellets": 1, "interval": 6, "auto": False, "capacity": 6,
        "reload": (9, 12, 13), "spread": (2.5, 0.8), "range": 48, "ammo": "light_round",
    },
    # Slice 8B: the service arms, a step up in steel (the dieselpunk tier). The Sentry Pistol is held in one hand; the
    # Garrison Rifle fires for as long as the trigger is held; the Breacher is a pump shotgun fed from a box magazine.
    "sentry_pistol": {
        "display": "Sentry Pistol",
        "source": "mak_mkii",
        "tooltip": "A steel service pistol: eight hard-hitting shots from a magazine. Fires light rounds.",
        "damage": 5.0, "pellets": 1, "interval": 5, "auto": False, "capacity": 8,
        "reload": 47, "spread": (2.0, 0.6), "range": 64, "ammo": "light_round",
    },
    "garrison_rifle": {
        "display": "Garrison Rifle",
        "source": "stigg",
        "tooltip": "A steel assault rifle with a curved magazine, firing for as long as the trigger is held. Fires rifle rounds.",
        "damage": 4.0, "pellets": 1, "interval": 3, "auto": True, "capacity": 30,
        "reload": 53, "spread": (3.0, 0.6), "range": 80, "ammo": "rifle_round",
    },
    "breacher": {
        "display": "Breacher",
        "source": "combat_shotgun",
        "tooltip": "A pump shotgun fed from a box magazine, so quick to reload. Fires buckshot shells.",
        "damage": 3.0, "pellets": 8, "interval": 16, "auto": False, "capacity": 6,
        "reload": 52, "spread": (7.0, 5.0), "range": 28, "ammo": "buckshot_shell",
    },
    # Slice 8C: the heavy weapons, in steel. Each fires something other than a bullet, or fires it differently:
    #   shot     "grenade": each shot lobs a Grenade (the field chemistry branch's frag grenade) that bursts where it
    #            lands and never breaks blocks; "damage" is the burst at its centre, "range" how far a level shot
    #            carries and the spread how far the grenade strays. "flame": each shot is a burst of a short jet of
    #            flame that singes and sets alight every creature in it, never a block; "damage" is each burst's, on
    #            each creature in the jet, "range" its reach and the spread the jet's half width. Bullets otherwise.
    #   spin_up  ticks the trigger must be held, the barrels spinning up, before the first shot (the server keeps
    #            count); a gap in the holding spins them down.
    # Their ammunition is not one of AMMO's rounds: the Grenade, and blaze powder (OTHER_AMMO).
    "trench_lobber": {
        "display": "Trench Lobber",
        "source": "hammer_gl",
        "tooltip": "A pump-action grenade launcher fed from a box magazine. Each shot lobs a grenade that bursts where "
                   "it lands; it never breaks blocks. Fires grenades.",
        "damage": 16.0, "pellets": 1, "interval": 14, "auto": False, "capacity": 6,
        "reload": 53, "spread": (3.0, 1.0), "range": 24, "ammo": "grenade", "shot": "grenade",
    },
    "thresher": {
        "display": "Thresher",
        "source": "gattaler",
        "tooltip": "A drum-fed rotary gun. Hold the trigger: the barrels spin up, then it fires for as long as it is "
                   "held. Fires rifle rounds.",
        "damage": 3.0, "pellets": 1, "interval": 2, "auto": True, "capacity": 60,
        "reload": 78, "spread": (4.0, 2.0), "range": 64, "ammo": "rifle_round", "spin_up": 15,
    },
    "stoker": {
        "display": "Stoker",
        "source": "kiln_gun",
        "tooltip": "A flamethrower that burns blaze powder: a short jet that sets creatures alight and never a block. "
                   "Each blaze powder is four bursts.",
        "damage": 2.0, "pellets": 1, "interval": 4, "auto": True, "capacity": 32,
        "reload": 61, "spread": (10.0, 6.0), "range": 8, "ammo": "minecraft:blaze_powder", "shot": "flame",
    },
    # Slice 8D: the energy weapons, past steel (advanced circuits). Each runs on charge from the energy system: its
    # ammunition is the Energy Cell (CELL), which the Charging Station fills, and a reload draws each round's "charge"
    # (JE) from the cells in the inventory, leaving the cells to be charged again (JugcraftGuns.CHARGE).
    #   shot "beam": a straight beam to the first block in range, through every creature in its line; each takes
    #        "damage". "arc": a bolt that leaps to the creature nearest the aim within the spread (degrees off the
    #        look, from the hip and aimed) and range, then on to up to ARC_HOPS more, each the nearest within
    #        ARC_REACH blocks of the last, each taking ARC_SHARE of the damage before it.
    #   Both deal jugcraft:zap (DAMAGE_TYPES).
    "beam_pistol": {
        "display": "Beam Pistol",
        "source": "raygun",
        "tooltip": "A ray pistol held in one hand: its beam passes through every creature in its line. Breaks open to "
                   "load. Runs on Energy Cells.",
        "damage": 6.0, "pellets": 1, "interval": 8, "auto": False, "capacity": 8,
        "reload": 48, "spread": (1.5, 0.5), "range": 48, "ammo": "energy_cell", "shot": "beam", "charge": 400,
    },
    "stormlock_rifle": {
        "display": "Stormlock Rifle",
        "source": "teslock_rifle",
        "tooltip": "A coil rifle whose bolt leaps from its mark to two more creatures close by. Loaded a charge at a "
                   "time. Runs on Energy Cells.",
        "damage": 9.0, "pellets": 1, "interval": 14, "auto": False, "capacity": 5,
        "reload": (18, 17, 20), "spread": (2.0, 0.5), "range": 64, "ammo": "energy_cell", "shot": "arc", "charge": 750,
    },
    "linesman": {
        "display": "Linesman",
        "source": "arc_worker",
        "tooltip": "A short-range arc thrower. Hold the trigger: its arcs find the creatures in front of it and leap "
                   "between them. Loaded a cell at a time. Runs on Energy Cells.",
        "damage": 4.0, "pellets": 1, "interval": 6, "auto": True, "capacity": 6,
        "reload": (8, 13, 12), "spread": (15.0, 10.0), "range": 12, "ammo": "energy_cell", "shot": "arc", "charge": 250,
    },
    # Slice 9A: the marksman rifles, in steel. Semi-automatic, one shot each pull: the steadiest aimed and the farthest
    # reaching of the guns. The Picket Rifle looks through a peep sight; the Ranger Rifle hits hardest, its handle
    # lifted and its bolt drawn back to change a magazine; the Kestrel Rifle is loaded from the top with a clip of
    # eight and takes a scope.
    "picket_rifle": {
        "display": "Picket Rifle",
        "source": "m3_marksman",
        "tooltip": "A steel marksman's rifle with a peep sight: one steady shot each pull, true at long range. Fires rifle "
                   "rounds.",
        "damage": 8.0, "pellets": 1, "interval": 8, "auto": False, "capacity": 10,
        "reload": 43, "spread": (2.0, 0.1), "range": 128, "ammo": "rifle_round",
    },
    "ranger_rifle": {
        "display": "Ranger Rifle",
        "source": "mk43_rifle",
        "tooltip": "A heavy semi-automatic rifle: ten hard-hitting shots from a magazine. Fires rifle rounds.",
        "damage": 10.0, "pellets": 1, "interval": 10, "auto": False, "capacity": 10,
        "reload": 45, "spread": (2.5, 0.15), "range": 120, "ammo": "rifle_round",
    },
    "kestrel_rifle": {
        "display": "Kestrel Rifle",
        "source": "whistler",
        "tooltip": "A copper-bright rifle loaded from the top with a clip of eight. Takes a scope. Fires rifle rounds.",
        "damage": 9.0, "pellets": 1, "interval": 9, "auto": False, "capacity": 8,
        "reload": 55, "spread": (2.0, 0.15), "range": 128, "ammo": "rifle_round",
    },
    # Slice 9B: the automatic weapons, in steel: each fires for as long as the trigger is held. The Rattler Pistol and
    # the Bronco SMG are held in one hand, the left coming in only to change the magazine; the Squall Rifle is an air
    # rifle whose gas canister, on its left side, is changed to reload.
    "rattler_pistol": {
        "display": "Rattler Pistol",
        "source": "auvtomag",
        "tooltip": "A steel machine pistol, held in one hand, that fires for as long as the trigger is held. Takes a "
                   "scope. Fires light rounds.",
        "damage": 3.0, "pellets": 1, "interval": 3, "auto": True, "capacity": 20,
        "reload": 48, "spread": (4.0, 2.0), "range": 48, "ammo": "light_round",
    },
    "bronco_smg": {
        "display": "Bronco SMG",
        "source": "jr_wristbreaker",
        "tooltip": "A short submachine gun with a broad barrel shroud, fired in one hand: it bucks hard. Hold to fire. "
                   "Fires light rounds.",
        "damage": 3.5, "pellets": 1, "interval": 3, "auto": True, "capacity": 25,
        "reload": 53, "spread": (5.0, 2.5), "range": 40, "ammo": "light_round",
    },
    "squall_rifle": {
        "display": "Squall Rifle",
        "source": "gale",
        "tooltip": "An air rifle that fires for as long as the trigger is held, the gauge on its gas canister jumping with "
                   "each shot. Takes a scope. Fires light rounds.",
        "damage": 2.5, "pellets": 1, "interval": 2, "auto": True, "capacity": 40,
        "reload": 57, "spread": (3.0, 0.8), "range": 64, "ammo": "light_round",
    },
    # Slice 9C: the second energy weapons, on slice 8D's cells and shots. The Spikedriver is a rail pistol, held in one
    # hand: a heavy beam, slow and far-reaching. The Seam Cutter is a cutting laser carried at the hip: a short beam
    # for as long as the trigger is held. The Caisson Pistol's arc seeks wider than the Stormlock's, nearer.
    "spikedriver": {
        "display": "Spikedriver",
        "source": "railworker",
        "tooltip": "A rail pistol held in one hand: each heavy shot drives through every creature in its line. Its side "
                   "magazine is changed and its lever worked to reload. Runs on Energy Cells.",
        "damage": 12.0, "pellets": 1, "interval": 16, "auto": False, "capacity": 6,
        "reload": 67, "spread": (1.5, 0.3), "range": 64, "ammo": "energy_cell", "shot": "beam", "charge": 800,
    },
    "seam_cutter": {
        "display": "Seam Cutter",
        "source": "cr4k_mining_laser",
        "tooltip": "A cutting laser carried at the hip: for as long as the trigger is held, its short beam burns through "
                   "every creature in front of it. Its core is drawn out to change. Runs on Energy Cells.",
        "damage": 1.5, "pellets": 1, "interval": 2, "auto": True, "capacity": 60,
        "reload": 60, "spread": (2.0, 1.0), "range": 16, "ammo": "energy_cell", "shot": "beam", "charge": 100,
    },
    "caisson_pistol": {
        "display": "Caisson Pistol",
        "source": "hyperbaria",
        "tooltip": "A pressure pistol held in one hand: its bolt leaps from its mark to two more creatures close by. The "
                   "tall tank on top is changed to reload. Runs on Energy Cells.",
        "damage": 5.0, "pellets": 1, "interval": 8, "auto": False, "capacity": 10,
        "reload": 57, "spread": (6.0, 3.0), "range": 24, "ammo": "energy_cell", "shot": "arc", "charge": 300,
    },
}

# What a gun fires: bullets, or a slice 8C or 8D gun's "shot".
SHOTS = ("bullet", "grenade", "flame", "beam", "arc")
# An arc (slice 8D) leaps on from its first creature to at most this many more, each the nearest within this many
# blocks of the last that the shooter may strike and that it can reach in a straight line, each taking this share of
# the damage before it (JugcraftGuns.ARC_HOPS, ARC_REACH, ARC_SHARE).
ARC_HOPS = 2
ARC_REACH = 4.0
ARC_SHARE = 0.6
# The energy weapons' ammunition (slice 8D): the Energy Cell (guns/EnergyCellItem), a chargeable item like the powered
# tools (the jugcraft:energy charge, in JE; ToolUpgrades' capacity modules fit it), filled at the Charging Station. It
# holds energy, not a shot: a reload pools the cells' charge, takes each round's "charge" from them in inventory order
# and leaves the cells, to be charged again. The guns alone draw on it; the portable battery docs/MACHINE_ROADMAP.md
# plans is another thing. Its art is the owner's (Guns/item/energy_cell.png, a three-frame glow that CELL_ANIMATION
# runs; empty_cell.png when it is spent).
CELL = "energy_cell"
CELL_DISPLAY = "Energy Cell"
CELL_TOOLTIP = "Charge for the energy guns. Fill it at a Charging Station; a reload draws on it and leaves it."
CELL_CAPACITY = 10_000
CELL_ART = {"energy_cell": "energy_cell", "energy_cell_empty": "empty_cell"}
CELL_ANIMATION = {"animation": {"frametime": 6}}
# Two cells from copper cable, glass and redstone, capped in brass.
CELL_RECIPE = ([" C ", "GRG", " B "], {"C": "jugcraft:copper_cable", "G": "minecraft:glass_pane", "R": "minecraft:redstone",
                                       "B": "#c:ingots/brass"}, 2)
# Ammunition that is not one of AMMO's rounds (slice 8C), each with the rounds one item loads
# (JugcraftGuns.PER_ITEM): the field chemistry branch's Grenade (jugcraft:grenade), a grenade a round; and blaze
# powder, four bursts of the Stoker's flame. A reload that tops a gun up takes a whole item; what of it does not fit is
# lost (docs/features/guns.md, slice 8C).
OTHER_AMMO = {"grenade": 1, "minecraft:blaze_powder": 4}

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
    # Slice 8: iron and brass, a lever for the revolvers' lockwork, a wooden grip.
    "bulldog_pistol": (["IIB", " P "], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass", "P": "#minecraft:planks"}),
    "marshal_revolver": (["IIB", "BLP"], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass", "L": "minecraft:lever",
                                          "P": "#minecraft:planks"}),
    "sapper_revolver": (["IB", "LP"], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass", "L": "minecraft:lever",
                                       "P": "#minecraft:planks"}),
    # Slice 8B: steel (the coke oven and steel foundry), brass and a lever.
    "sentry_pistol": (["SSS", " LB"], {"S": "#c:ingots/steel", "L": "minecraft:lever", "B": "#c:ingots/brass"}),
    "garrison_rifle": (["SSS", "BLP"], {"S": "#c:ingots/steel", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                        "P": "#minecraft:planks"}),
    "breacher": (["SS ", "BLP"], {"S": "#c:ingots/steel", "L": "minecraft:lever", "B": "#c:ingots/brass",
                                  "P": "#minecraft:planks"}),
    # Slice 8C: more steel, and each a heavy part: the Lobber's wide tube, the Thresher's piston to turn its barrels,
    # the Stoker's igniter (flint and steel) and fuel tank (a bucket).
    "trench_lobber": (["SSS", "SBS", " LP"], {"S": "#c:ingots/steel", "B": "#c:ingots/brass", "L": "minecraft:lever",
                                             "P": "#minecraft:planks"}),
    "thresher": (["SSS", "SPS", "BLB"], {"S": "#c:ingots/steel", "P": "minecraft:piston", "B": "#c:ingots/brass",
                                        "L": "minecraft:lever"}),
    "stoker": (["SSF", "BLK"], {"S": "#c:ingots/steel", "F": "minecraft:flint_and_steel", "B": "#c:ingots/brass",
                                "L": "minecraft:lever", "K": "minecraft:bucket"}),
    # Slice 8D: steel and an advanced circuit each (the circuit assembler's), with the Beam Pistol's amethyst lens and
    # the arc guns' lightning rods for emitters and copper cable for their coils.
    "beam_pistol": (["SSM", " AB"], {"S": "#c:ingots/steel", "M": "minecraft:amethyst_shard", "A": "jugcraft:advanced_circuit",
                                    "B": "#c:ingots/brass"}),
    "stormlock_rifle": ([" SL", "CAS", "PB "], {"S": "#c:ingots/steel", "L": "minecraft:lightning_rod",
                                               "C": "jugcraft:copper_cable", "A": "jugcraft:advanced_circuit",
                                               "P": "#minecraft:planks", "B": "#c:ingots/brass"}),
    "linesman": (["LSS", "CAB", " CB"], {"L": "minecraft:lightning_rod", "S": "#c:ingots/steel", "C": "jugcraft:copper_cable",
                                        "A": "jugcraft:advanced_circuit", "B": "#c:ingots/brass"}),
    # Slice 9A: a long steel barrel and a lever for the trigger each; the Picket all steel, the Ranger with a wooden
    # stock under it, the Kestrel copper-bright.
    "picket_rifle": (["SSS", "SLB"], {"S": "#c:ingots/steel", "L": "minecraft:lever", "B": "#c:ingots/brass"}),
    "ranger_rifle": (["SSS", "BLP", " P "], {"S": "#c:ingots/steel", "B": "#c:ingots/brass", "L": "minecraft:lever",
                                            "P": "#minecraft:planks"}),
    "kestrel_rifle": (["SSS", "CLC"], {"S": "#c:ingots/steel", "C": "minecraft:copper_ingot", "L": "minecraft:lever"}),
    # Slice 9B: steel and a lever each; redstone for the machine pistol's and the SMG's sears, a piston for the air
    # rifle's pump.
    "rattler_pistol": (["SSS", "RLB"], {"S": "#c:ingots/steel", "R": "minecraft:redstone", "L": "minecraft:lever",
                                        "B": "#c:ingots/brass"}),
    "bronco_smg": (["SSB", "RLB"], {"S": "#c:ingots/steel", "B": "#c:ingots/brass", "R": "minecraft:redstone",
                                    "L": "minecraft:lever"}),
    "squall_rifle": (["SSS", "TLP"], {"S": "#c:ingots/steel", "T": "minecraft:piston", "L": "minecraft:lever",
                                      "P": "#minecraft:planks"}),
    # Slice 9C: steel and an advanced circuit each, as slice 8D's; copper cable for the rail pistol's rails, copper
    # ingots for the cutting laser's copper body and an amethyst shard for its lens, a lightning rod for the pressure
    # pistol's arc.
    "spikedriver": (["SSS", "CAB"], {"S": "#c:ingots/steel", "C": "jugcraft:copper_cable", "A": "jugcraft:advanced_circuit",
                                     "B": "#c:ingots/brass"}),
    "seam_cutter": (["CCM", "SAB"], {"C": "minecraft:copper_ingot", "M": "minecraft:amethyst_shard", "S": "#c:ingots/steel",
                                     "A": "jugcraft:advanced_circuit", "B": "#c:ingots/brass"}),
    "caisson_pistol": (["LSS", " AB"], {"L": "minecraft:lightning_rod", "S": "#c:ingots/steel",
                                       "A": "jugcraft:advanced_circuit", "B": "#c:ingots/brass"}),
}
# The switches beyond "guns" a gun's recipe needs (separate load conditions, all of which must hold): the energy weapons
# and their cells are useless without the Charging Station, so they need the machines too (as the leaf blower does).
RECIPE_SWITCHES = {gun: ("machines",) for gun, spec in GUNS.items() if spec["ammo"] == CELL}

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
# "muzzle" and "sight": the locators the shot's smoke and aiming down the sights use. A gun with no sights to aim
# down ("sight" None: the Thresher, carried at the hip with its back by the eye) is not slid over when aimed; aiming it
# only steadies it (its aimed spread) and narrows the view (ZOOM).
# "eye_relief" (optional): how much further from the eye the gun is held aimed than at the hip, in sixteenths of a
# block (client/guns/GunLooks.EYE_RELIEF). Aiming slides the sight onto the middle of the screen at the hip's depth; a
# gun whose moving parts slide back along the line of sight as it fires can reach the eye there.
# "mounts" (optional): the bone a slot's attachments ride, where it is not the bone holding the slot's standard part
# (effective_bones()): the Trench Lobber's leaf sight flaps with each shot, and a scope in its place should not.
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
    # Slice 8, the revolvers' set. All three are held in one hand: the idle hides the left arm, whose hand point is
    # given for a reload keyframe ("hand_pose"). Each left arm's direction keeps it coming up from below the screen
    # through every frame it shows (the reloads, and the revolvers' inspect), not only at that keyframe.
    # The Brawler: its hammer is the main part's 9th element, a flat plate riding the bolt bone that the shot drives
    # forward; its barrel tips about the hinge at the front of the lug beneath it to load, where it stands just clear of
    # the frame; the reload carries a cartridge in on the shell bone (PROPS), resting where the reload's offsets bring
    # its middle a pixel down the opened bore as it shrinks away (1.54 s). The left hand holds that round 1.17 s in,
    # 2.5 px back from it toward the shoulder, so the round shows past the fingers. The reload also names an
    # "extended_barrel" bone, which it never moves: it is empty, and a fitted Extended Barrel rides the barrel bone.
    "bulldog_pistol": {
        "bones": [
            ("gun_body", None, ["main-#9", "sights"], (8.0, 2.85, 15.0)),
            ("bolt", "gun_body", ["main#9"], (8.0, 4.13, 13.73)),
            ("barrel", "gun_body", ["stan_barrel"], (8.0, 4.3, 5.05)),
            ("extended_barrel", "barrel", [], (8.0, 4.3, 5.05)),
            ("shell", "gun_body", ["@shell"], (7.98, 9.8, 8.25)),
        ],
        "hands": {"right": (8.0, 2.85, 15.0), "left": (6.24, 7.42, 5.55)},
        "hand_pose": {"left": ("reload", "1.1667")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.6858, -0.3212, 0.653)},
        "muzzle": (8.0, 4.8, 3.71),
        "sight": (8.0, 6.17, 12.25),
    },
    # The Longarm loads through a gate: its cylinder (the main part's elements the owner named "magazine") turns a
    # chamber for each round on the cylinder_magazine bone, about its own axis. Its "magazine" bone is empty: the
    # closing animation swings it as if the cylinder swung out, which this one does not. Its hammer is the main
    # part's last element, turning about its foot. The left hand loads at the gate, on the right behind the cylinder.
    "marshal_revolver": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.45, 14.58)),
            ("gun_body", "gun_body2", ["main-#24,25,26,27,28,29,30"], (8.0, 2.45, 14.58)),
            ("cylinder_magazine", "gun_body", ["main#24,25,26,27,28,29"], (7.97735, 4.4, 10.76618)),
            ("magazine", "gun_body", [], (7.97735, 4.4, 10.76618)),
            ("hammer", "gun_body", ["main#30"], (8.0, 3.84, 13.75)),
            ("seal", "gun_body", [], (8.0, 4.4, 10.77)),
            ("shell", "gun_body", [], (8.0, 4.4, 10.77)),
        ],
        "hands": {"right": (8.0, 2.45, 14.58), "left": (9.5, 4.1, 12.2)},
        "hand_pose": {"left": ("reload_loop", "0.4167")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.3752, -0.911, 0.171)},
        "muzzle": (8.0, 4.78, 3.65),
        "sight": (8.0, 5.85, 12.3),
    },
    # The Trenchur's cylinder (its drum part) swings out to the left on the magazine bone, about the crane's hinge
    # below and left of it, to load a round at a time; its hammer turns about its foot. The left hand loads behind the
    # swung-out cylinder's outer chamber.
    "sapper_revolver": {
        "bones": [
            ("gun_body", None, ["main", "stan_barrel"], (8.0, 1.73, 14.73)),
            ("magazine", "gun_body", ["drum"], (7.0, 2.6, 11.6)),
            ("hammer", "gun_body", ["hammer"], (8.0, 3.42, 14.08)),
            ("bolt", "gun_body", [], (8.0, 3.83, 12.0)),
            ("seal", "gun_body", [], (8.0, 3.83, 11.6)),
            ("shell", "gun_body", [], (8.0, 3.83, 11.6)),
        ],
        "hands": {"right": (8.0, 1.73, 14.73), "left": (5.4, 3.8, 13.4)},
        "hand_pose": {"left": ("reload_loop", "0.2857")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.3078, -0.9328, -0.1877)},
        "muzzle": (8.0, 3.83, 5.24),
        "sight": (8.0, 5.6, 13.2),
    },
    # Slice 8B, the service arms. Each gun body turns about its grip; the shot moves the bolt part; the reload drops
    # the magazine and brings a new one in on magazine_2 (both carry the magazine part), each turning about the point
    # that keeps its top in the magazine well (the Mak's top, the Stigg's middle, the Combat Shotgun's top).
    # The Mak MkII is held in one hand; its idle hides the left arm.
    "sentry_pistol": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.02, 14.3)),
            ("gun_body", "gun_body2", ["main", "stan_barrel"], (8.0, 2.02, 14.3)),
            ("bolt", "gun_body", ["bolt"], (8.0, 5.9, 11.5)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 2.5, 14.45)),
            ("magazine_2", "gun_body", ["stan_mag"], (8.0, 2.5, 14.45)),
        ],
        "hands": {"right": (8.0, 2.02, 14.3), "left": (5.79, -2.31, 13.51)},
        "hand_pose": {"left": ("reload", "1.4583")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.9195, -0.24, 0.3112)},
        "muzzle": (8.0, 4.98, 8.6),
        "sight": (8.0, 6.15, 16.1),
    },
    # The Stigg's bolt is the receiver's two sides and the charging handle; its rear sight is the sights part, the front
    # post is on the barrel. Its receiver runs back under the line of sight to just short of the eye, and each shot slides
    # the bolt 2.6 px back and the gun 1.4 px: at the hip's depth, aimed, the bolt came past the eye and the shot filled
    # the screen (CI, 8 October 2026). Held 4 px further out aimed, the nearest of it stays 3.5 px from the eye.
    "garrison_rifle": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.64, 15.04)),
            ("gun_body", "gun_body2", ["main", "stan_barrel", "sights"], (8.0, 2.64, 15.04)),
            ("bolt", "gun_body", ["bolt"], (8.375, 5.6, 15.1)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 1.34, 10.05)),
            ("magazine_2", "gun_body", ["stan_mag"], (8.0, 1.34, 10.05)),
        ],
        "hands": {"right": (8.0, 2.64, 15.04), "left": (8.0, 2.5, 7.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.3, 0.55),
        "sight": (8.0, 6.82, 11.4),
        "eye_relief": 4.0,
    },
    # The Combat Shotgun's bolt part is its pump: the fore-end under the barrel and the handle beside the receiver that
    # rides with it. Its sights are a front post and a ring at the back.
    "breacher": {
        "bones": [
            ("gun_body2", None, [], (8.0, 1.8, 16.05)),
            ("gun_body", "gun_body2", ["main", "stan_barrel", "sights"], (8.0, 1.8, 16.05)),
            ("bolt", "gun_body", ["bolt"], (8.0, 3.5, 6.65)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 3.3, 11.61)),
            ("magazine_2", "gun_body", ["stan_mag"], (8.0, 3.3, 11.61)),
        ],
        "hands": {"right": (8.0, 1.8, 16.05), "left": (8.0, 2.3, 6.65)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.05, 3.98),
        "sight": (8.0, 6.24, 14.475),
    },
    # Slice 8C, the heavy weapons. The owner named the parts' pieces (the Hammer GL's "bolt", the Kiln Gun's "barrel"
    # group, the Gattaler's "Drum" group); each gun body turns about the grip in the right hand.
    # The Hammer GL's bolt is its pump: a sleeve on the rod under the barrel with a fore-grip angled down to the left,
    # which the shot slides back and rolls about the rod. Its leaf sight flips about its hinge with each shot; the
    # reload drops the magazine and seats it again (magazine_2 stays hidden, at scale 0, in the owner's reload).
    "trench_lobber": {
        "bones": [
            ("gun_body2", None, [], (8.0, 3.5, 16.9)),
            ("gun_body", "gun_body2", ["main@gun_body"], (8.0, 3.5, 16.9)),
            ("bolt", "gun_body", ["main@bolt"], (8.0, 3.08, 2.75)),
            ("sights", "gun_body", ["sights"], (8.0, 7.55, 9.51)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 6.4, 10.27)),
            ("magazine_2", "gun_body", [], (8.0, 6.4, 10.27)),
        ],
        "mounts": {"optic": "gun_body"},
        "hands": {"right": (8.0, 3.5, 16.9), "left": (6.3, 1.6, 2.7)},
        "arms": {"right": (-0.2722, -0.2158, 0.9377), "left": (-0.4794, -0.3003, 0.8246)},
        "muzzle": (8.0, 5.77, -2.55),
        "sight": (8.0, 8.6, 9.5),
    },
    # The Gattaler's barrels turn about their middle, spun from code (client/guns/GunRenderer: no animation moves
    # them). The right hand holds the rear grip low and the left the front plate's left side, by the barrels' root; the
    # reload ends with the carry handle on top worked forward like a lever (the owner's "grip"), about its feet on the
    # body's sides, and in between the left hand drops toward the drum on the left side as it comes off and goes back
    # (it turns about its middle). The owner's animations rest the left hand on the carry handle, but the owner's
    # display carries the gun at the hip with its back by the eye, and there the hand and its forearm filled the
    # screen (PR #278's in-game shots); from the front plate the same moves keep below the gun. For the same reason
    # it has no sights: sliding any point of it onto the middle of the screen brought the grip across the eye.
    "thresher": {
        "bones": [
            ("gun_body2", None, [], (8.0, 9.0, 16.4)),
            ("gun_body", "gun_body2", ["main-#2,3,4,5,15,17,18,19,20"], (8.0, 9.0, 16.4)),
            ("grip", "gun_body", ["main#2,3,4,5,15"], (8.0, 9.28, 10.88)),
            ("magazine", "gun_body", ["main#17,18,19,20"], (4.75, 4.16, 13.2)),
            ("barrels", "gun_body", ["barrels"], (8.0, 8.0, 3.25)),
        ],
        "hands": {"right": (8.0, 9.0, 16.4), "left": (5.5, 7.0, 9.8)},
        "arms": {"right": (-0.0946, -0.1812, 0.9789), "left": (0.2172, -0.6757, 0.7044)},
        "muzzle": (8.0, 8.0, -3.0),
        "sight": None,
    },
    # The Kiln Gun's "barrel" group (its two tubes and their collar) hinges up at the back for its shell-at-a-time
    # reload, which the Stoker does not use (it loads by the can). Its wide drum (the owner's cylinder_magazine) turns a
    # little about its middle to let the fuel can out and back. The owner's parts have no can, so the magazine bone
    # carries one (PROPS), inside the drum, out of sight until the reload pulls it. Each shot's jet of flame is the
    # "flame" bone: the shot slides it 5.5 px back to the nozzle at twice its size, then out and shrinking.
    "stoker": {
        "bones": [
            ("gun_body2", None, [], (8.0, 3.9, 7.7)),
            ("gun_body", "gun_body2", ["main-#22,23,24,25,26,27,28,29,30,31,32,33,34,35,36"], (8.0, 3.9, 7.7)),
            ("barrel", "gun_body", ["main#23,24,25,26,27,28,29,30,31,32,33,34,35,36"], (8.0, 4.7, 3.9)),
            ("cylinder_magazine", "gun_body", ["main#22"], (8.0, 8.575, 5.4)),
            ("magazine", "gun_body", ["@canister"], (8.0, 8.575, 5.4)),
            ("flame", "gun_body", ["@flame"], (8.0, 8.45, -13.6)),
            ("bolt", "gun_body", [], (8.0, 3.9, 7.7)),
        ],
        "hands": {"right": (8.0, 3.9, 7.7), "left": (8.0, 5.2, -2.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 8.45, -8.1),
        "sight": (8.0, 11.7, 5.0),
    },
    # Slice 8D, the energy weapons. The Raygun is held in one hand: its idle hides the left arm, whose hand point is
    # given for the reload's moment it takes the barrel. Its barrel (the main part's 4th to 9th elements: the bore, its
    # rings, the rod and the emitter's plates) breaks open upward about the hinge at the bottom of its back end, to
    # 140 degrees; its shell bone is empty (the owner's animations keep it at scale 0).
    "beam_pistol": {
        "bones": [
            ("gun_body", None, ["main-#3,4,5,6,7,8"], (8.0, 2.0, 14.3)),
            ("barrel", "gun_body", ["main#3,4,5,6,7,8"], (8.0, 3.45, 11.35)),
            ("shell", "gun_body", [], (8.0, 4.4, 11.35)),
        ],
        "hands": {"right": (8.0, 2.0, 14.3), "left": (8.0, 2.2, 8.6)},
        "hand_pose": {"left": ("reload", "0.2917")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.6341, -0.6949, 0.3391)},
        "muzzle": (8.0, 4.45, 3.8),
        "sight": (8.0, 7.0, 18.3),
        "eye_relief": 4.0,
    },
    # The Teslock's cylinder (the main part's 26th, 27th, 35th and 36th elements, the owner's "mag") turns about the
    # bore's line to take each charge, its latch (the 7th and 8th) swinging up off it first; the loop under the grip
    # (the 40th) is a lever, worked down and back to close it. Its bolt and shell bones are empty.
    "stormlock_rifle": {
        "bones": [
            ("gun_body", None, ["main-#6,7,25,26,34,35,39", "sights"], (8.0, 2.6, 15.0)),
            ("mag", "gun_body", ["main#25,26,34,35"], (7.95, 4.255, 10.37)),
            ("latch", "gun_body", ["main#6,7"], (8.5, 4.74, 5.86)),
            ("lever", "gun_body", ["main#39"], (7.95, 2.68, 12.37)),
            ("bolt", "gun_body", [], (7.95, 4.26, 12.4)),
            ("shell", "gun_body", [], (7.95, 4.26, 10.37)),
        ],
        "hands": {"right": (8.0, 1.9, 14.8), "left": (8.0, 2.4, 5.0)},
        "arms": {"right": (-0.2759, -0.2436, 0.9298), "left": (0.7169, -0.5901, 0.3712)},
        "muzzle": (7.95, 4.26, -4.61),
        "sight": (7.95, 5.6, 12.66),
    },
    # The Arc Worker's "Bolt" (the owner's capital B; the main part's 29th element, the block over its battery tubes)
    # slides back half a pixel to open and closes after; each loop of its reload carries a cell (PROPS) in from the
    # left hand and down into the lower tube's back end, shrinking away as it goes in.
    "linesman": {
        "bones": [
            ("gun_body", None, ["main-#28", "sights"], (8.0, 1.4, 15.7)),
            ("Bolt", "gun_body", ["main#28"], (8.0, 3.15, 10.75)),
            ("shell", "gun_body", ["@cell"], (8.48, 1.56, 12.6)),
        ],
        "hands": {"right": (8.0, 1.4, 15.7), "left": (8.0, 0.0, 6.6)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 4.34, 1.27),
        "sight": (8.0, 5.6, 13.8),
    },
    # Slice 9A, the marksman rifles; each gun body turns about the grip in the right hand. The M3 Marksman's bolt part is
    # its charging handle, on the right of the receiver, which each shot drives back; its reload swings the magazine
    # down and back out of the well, about its top. Its rear sight is a peep (the sights part's ring, on a rail); the
    # front post is on the barrel.
    "picket_rifle": {
        "bones": [
            ("gun_body", None, ["main", "stan_barrel", "sights"], (8.0, 2.09, 15.19)),
            ("bolt", "gun_body", ["bolt"], (9.28, 5.03, 11.34)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 3.02, 10.71)),
        ],
        "hands": {"right": (8.0, 2.09, 15.19), "left": (8.0, 2.8, 7.0)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.02, -0.04),
        "sight": (8.0, 7.03, 14.7),
        "eye_relief": 2.0,
    },
    # The MK43's handle (the main part's 31st to 34th elements: a bar along the right of the top cover, on a leg at its
    # front) rides its bolt bone, which is otherwise empty: each shot drives the handle back, and the reload first lifts
    # it about the foot of its leg, then draws it back. Its rear notch and front post are the main part's. Its "seal"
    # and "Flames" bones are empty: the charm that sways on each shot and the flames at the muzzle are not among the
    # owner's parts. magazine_2 stays hidden (scale 0) in the owner's reload.
    "ranger_rifle": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.09, 15.29)),
            ("gun_body", "gun_body2", ["main-#30,31,32,33", "stan_grip"], (8.0, 2.09, 15.29)),
            ("bolt", "gun_body", [], (8.75, 5.1, 11.38)),
            ("Handle", "bolt", ["main#30,31,32,33"], (8.75, 5.1, 11.38)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 2.83, 10.61)),
            ("magazine_2", "gun_body", [], (8.0, 2.83, 10.61)),
            ("seal", "gun_body", [], (8.0, 6.0, 15.0)),
            ("Flames", "gun_body", [], (8.0, 4.84, -0.1)),
            ("Flames2", "gun_body", [], (8.0, 4.84, -0.1)),
            ("Flames3", "gun_body", [], (8.0, 4.84, -0.1)),
            ("Flames4", "gun_body", [], (8.0, 4.84, -0.1)),
            ("Flames5", "gun_body", [], (8.0, 4.84, -0.1)),
            ("Flames6", "gun_body", [], (8.0, 4.84, -0.1)),
        ],
        "hands": {"right": (8.0, 2.09, 15.29), "left": (8.0, 1.75, 6.8)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 4.84, -0.1),
        "sight": (8.0, 6.1, 11.62),
        "eye_relief": 2.0,
    },
    # The Whistler's bolt (the main part's elements the owner named "bolt": the carrier along the right of the receiver
    # and its handle) slides back for the clip to go in from the top, turning a little about its own axis as it is
    # caught, and runs home after. Its rear sight is a peep (the sights part); the front post is the main part's.
    "kestrel_rifle": {
        "bones": [
            ("gun_body", None, ["main-@bolt", "stan_barrel", "sights", "stan_grip"], (8.0, 1.96, 14.77)),
            ("bolt", "gun_body", ["main@bolt"], (7.92, 5.33, 10.75)),
        ],
        "hands": {"right": (8.0, 1.96, 14.77), "left": (8.0, 1.95, 7.5)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.3, -3.45),
        "sight": (8.0, 6.38, 12.62),
        "eye_relief": 2.0,
    },
    # Slice 9B, the automatic weapons; each gun body turns about the grip in the right hand. The Auvtomag's slide (the
    # owner's receiver part) rides the bolt bone, which each shot drives back; its rear sight notch is on the slide and
    # its front post on the frame. Its sights part is empty, so its scopes and their rail (the owner's no_sights) ride
    # the slide with it. Its reload drops the magazine out of the grip (only its base plate shows) and the left hand
    # pushes the new one up; held in one hand, its idle hides the left arm.
    "rattler_pistol": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.12, 14.8)),
            ("gun_body", "gun_body2", ["main", "stan_barrel"], (8.0, 2.12, 14.8)),
            ("bolt", "gun_body", ["receiver", "sights"], (8.0, 5.2, 14.3)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 0.3, 15.6)),
            ("magazine_2", "gun_body", [], (8.0, 0.3, 15.6)),
        ],
        "hands": {"right": (8.0, 2.12, 14.8), "left": (8.0, -2.4, 15.8)},
        "hand_pose": {"left": ("reload", "1.6667")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.9195, -0.24, 0.3112)},
        "muzzle": (8.0, 4.52, 5.88),
        "sight": (8.0, 6.0, 15.38),
        "eye_relief": 4.0,
    },
    # The Jr Wristbreaker's bolt part is its two charging handles, either side of the receiver, which each shot drives
    # back. Its magazine goes in ahead of the grip; held in one hand, its idle hides the left arm, which comes in to
    # change the magazine. Its rear sight is a ring, its front post on the receiver. Its "seal" (a charm, as on the
    # revolvers) is not among the owner's parts: the bone is empty.
    "bronco_smg": {
        "bones": [
            ("gun_body", None, ["main", "stan_barrel"], (8.0, 2.48, 14.79)),
            ("bolt", "gun_body", ["bolt"], (8.0, 5.45, 11.59)),
            ("magazine", "gun_body", ["stan_mag"], (8.0, 3.25, 9.9)),
            ("seal", "gun_body", [], (8.0, 6.5, 14.0)),
        ],
        "hands": {"right": (8.0, 2.48, 14.79), "left": (8.0, -2.0, 9.9)},
        "hand_pose": {"left": ("reload", "1.25")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.2, 3.4),
        "sight": (8.0, 6.75, 14.14),
        "eye_relief": 4.0,
    },
    # The Gale's gas canister, on its left side (the main part's 19th, 21st, 22nd and 33rd elements), with the riser to
    # its gauge (the 23rd and 24th) and the gauge's dial and cap (the 20th and 27th), is the magazine its reload twists
    # off and carries away, about the middle the owner turned its elements about. The gauge's needle (the 26th) jumps
    # with each shot on the gauge bone, about the dial's middle; the owner's separate needle part is the same needle
    # unturned and is not used. The clamp at the canister's front (the 18th) is magazine2, swung aside for the canister
    # to come out. Its bolt bone is empty (only the draw names it, and holds it still).
    "squall_rifle": {
        "bones": [
            ("gun_body", None, ["main-#17,18,19,20,21,22,23,25,26,32", "sights", "stan_grip"], (8.0, 2.25, 15.14)),
            ("magazine", "gun_body", ["main#18,19,20,21,22,23,26,32"], (6.26, 3.65, 6.65)),
            ("gauge", "magazine", ["main#25"], (6.25, 5.65, 9.65)),
            ("magazine2", "gun_body", ["main#17"], (6.75, 4.15, 6.15)),
            ("bolt", "gun_body", [], (8.0, 5.2, 12.0)),
        ],
        "hands": {"right": (8.0, 2.25, 15.14), "left": (8.0, 1.25, 6.4)},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 5.2, -5.1),
        "sight": (8.0, 6.68, 13.39),
        "eye_relief": 3.0,
    },
    # Slice 9C, the second energy weapons. The Railworker is held in one hand: its idle hides the left arm, whose hand
    # point is given for the moment it holds the new magazine in, on the gun's left side, 1.25 s into the reload. The
    # frame standing out of its left side (the main part's 34th, 35th and 37th to 43rd elements) is its lever: the left
    # hand takes it and swings it back 145 degrees about its back end, and back, to charge the rails. The two rings
    # standing up at the back of its right side (the 2nd and 3rd) ride the bolt back with each shot, and the tip of its
    # muzzle (the 29th) recoils. gun_body2 and gun_body3 carry the whole gun as the lever is worked (empty, about the
    # grip). Its magazine, and the larger ones, are on its left side. It has no sights: aimed, it is looked over, along
    # the top of its back, and held 2 px further out (its back came within 2.6 px of the eye through the aimed shot).
    "spikedriver": {
        "bones": [
            ("gun_body3", None, [], (8.0, 1.68, 14.84)),
            ("gun_body2", "gun_body3", [], (8.0, 1.68, 14.84)),
            ("gun_body", "gun_body2", ["main-#1,2,28,33,34,36,37,38,39,40,41,42"], (8.0, 1.68, 14.84)),
            ("bolt", "gun_body", ["main#1,2"], (9.2, 5.3, 13.5)),
            ("lever", "gun_body", ["main#33,34,36,37,38,39,40,41,42"], (6.75, 4.3, 15.0)),
            ("tip", "gun_body", ["main#28"], (8.0, 3.7, 6.0)),
            ("magazine", "gun_body", ["stan_mag"], (6.42, 2.9, 9.25)),
            ("magazine_2", "gun_body", [], (6.42, 2.9, 9.25)),
        ],
        "hands": {"right": (8.0, 1.68, 14.84), "left": (5.4, 1.2, 9.2)},
        "hand_pose": {"left": ("reload", "1.25")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (8.0, 3.7, 5.75),
        "sight": (8.0, 6.05, 14.0),
        "eye_relief": 2.0,
    },
    # The CR4K Mining Laser is carried at the hip (the owner's third-person transform tilts it 72.75 degrees up off the
    # arm, as the Gattaler's), held as the Thresher is: the right hand on the slanted grip behind its copper body (the
    # main part's 3rd element), the left on its core, the brown cylinder standing out of its left side (the 1st and 44th
    # to 49th), which the reload draws out to the left about its inner end, drops away and brings back, as the left
    # hand lets go below. The handle above the grip (the 4th and 9th) is the owner's "grip", the carry handle the reload
    # flicks forward like a lever about its foot. The arm bones' idle turn is the owner's for this gun, not the other
    # guns', so each arm's way to the shoulder is turned to leave the screen as theirs do. Like the Thresher it has no
    # sights: its handle stands between the eye and its sight posts, so aiming only steadies it and narrows the view.
    "seam_cutter": {
        "bones": [
            ("gun_body", None, ["main-#0,3,8,43,44,45,46,47,48"], (8.0, 1.64, 16.72)),
            ("grip", "gun_body", ["main#3,8"], (8.0, 3.5, 16.6)),
            ("core", "gun_body", ["main#0,43,44,45,46,47,48"], (7.45, 3.6, 9.0)),
        ],
        "hands": {"right": (8.0, 1.64, 16.72), "left": (3.75, 3.6, 9.0)},
        "arms": {"right": (-0.2164, -0.21, 0.9535), "left": (0.3004, -0.5523, 0.7777)},
        "muzzle": (8.0, 2.45, 0.1),
        "sight": None,
    },
    # The Hyperbaria is held in one hand: its idle hides the left arm, whose hand point is given for the moment it holds
    # the bolt drawn back, 2.375 s into the reload. The tall tank on its top is the owner's "mag" (the main part's
    # elements so named), which the reload lifts off and tosses away to the left as a new one comes down into place;
    # its bolt is the owner's bolt part, along the top of its back. The small element named "bolt" at the foot of the
    # grip is a copy of the bolt's knob, a pommel, and stays with the body. gun_body2 (empty, about the grip) rolls the
    # gun as the bolt is worked, and holds it 0.7 px forward throughout. Its sight is the small ring at the left of the
    # tank's foot; aimed, its back stays 4.6 px from the eye.
    "caisson_pistol": {
        "bones": [
            ("gun_body2", None, [], (8.0, 2.48, 14.41)),
            ("gun_body", "gun_body2", ["main-@mag"], (8.0, 2.48, 14.41)),
            ("bolt", "gun_body", ["bolt"], (8.0, 5.39, 14.0)),
            ("mag", "gun_body", ["main@mag"], (8.0, 5.89, 10.95)),
        ],
        "hands": {"right": (8.0, 2.48, 14.41), "left": (7.2, 5.4, 17.7)},
        "hand_pose": {"left": ("reload", "2.375")},
        "arms": {"right": (-0.2762, -0.2762, 0.9206), "left": (0.717, -0.4911, 0.4947)},
        "muzzle": (7.95, 5.0, 3.7),
        "sight": (7.0, 6.14, 10.02),
    },
}

# Props the animations move on bones that had no part ("@<name>" in a bone's parts): the rounds a reload carries in,
# a muzzle-loader's ball and ramrod and its priming flash. Each is one box drawn here into a free corner of the gun's
# atlas copy (the corner must be empty); the renderer shows it only while an animation moves it (GunRenderer), but
# for a prop on a bone of another name (the Stoker's can, on its magazine bone), which rests out of sight.
#   kind "buckshot": a red paper hull with crimp lines and a brass head; "cartridge": a brass case with a lead tip;
#   "ball": a lead ball; "rod": an iron ramrod with a brass tip; "flash": a burst of priming fire; "canister": a brass
#   can with steel ends; "flame": the owner's pilot flame (Guns/item/spitfire_flame.png, its first frame) on the four
#   long faces, its tip forward, the ends left open (flame_faces()).
# The Stoker's flame rests 5.5 px ahead of the nozzle, its back end at the flame bone's pivot, so a shot's first
# offset (5.5 px back, at twice the size) puts it on the nozzle; its can rests inside the drum the reload turns.
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
    "bulldog_pistol": {"shell": {"kind": "cartridge", "from": (7.48, 9.3, 6.75), "size": (1.0, 1.0, 3.0),
                                 "texture_at": (56, 0)}},
    "bellmouth": {
        "ball": {"kind": "ball", "from": (6.86, 2.57, 7.27), "size": (1.0, 1.0, 1.0), "texture_at": (34, 61)},
        "ram": {"kind": "rod", "from": (7.75, 2.41, 5.3), "size": (0.5, 0.5, 10.0), "texture_at": (52, 50)},
        "flash": {"kind": "flash", "from": (8.5, 4.4, 12.7), "size": (1.0, 1.0, 1.0), "texture_at": (43, 58)},
    },
    "stoker": {
        "flame": {"kind": "flame", "from": (6.0, 6.45, -21.6), "size": (4.0, 4.0, 8.0), "texture_at": (120, 112)},
        "canister": {"kind": "canister", "from": (6.5, 7.075, 4.1), "size": (3.0, 3.0, 2.6), "texture_at": (120, 103)},
    },
    # Slice 8D: the Linesman's cell rests behind its lower tube's back end, its front end at the shell bone's pivot,
    # so the loop's last offset (half a pixel left, a third down and 2.7 px forward) puts it in the tube's mouth and
    # its shrinking draws it in.
    "linesman": {"cell": {"kind": "cell", "from": (7.73, 0.81, 12.6), "size": (1.5, 1.5, 3.0), "texture_at": (56, 0)}},
}
# The owner's pilot flame (Guns/item/spitfire_flame.png): three 8 x 8 frames, one above the other; the first frame's
# flame is 4 px wide (columns 2 to 5) and 8 tall, its tip at the top.
FLAME_SOURCE = LIBRARY / "item" / "spitfire_flame.png"

# ------------------------------------------------------------------ the attachments (slice 5)

# Each attachment is one item that fits every gun with a part of that kind among the owner's parts, drawn on the gun's
# own atlas (fits()); on each gun it shows as that gun's own part. Several of the owner's guns draw some parts on shared
# atlases (the Drover Rifle's, the Coach Gun's and the muzzle-loaders' grips, stocks and silencers); those wait for a
# later slice, as do the bayonets and the tactical grip.
#   slot     one attachment a slot: barrel, magazine, stock, grip (SLOTS)
#   parts    the owner's part file names for it, in the order tried (the guns name some differently)
#   replaces it takes the place of the slot's standard part (the barrel, the magazine, the Thunderpipe's pistol grip)
#   effects  multipliers on the gun's numbers: damage, range, hip_spread, aim_spread, capacity (rounds), reload (time),
#            kick (the view's jump) and volume (the shot's sound); the rest stay 1
#   model    the owner's item model for the attachment (Guns/models/item/<model>.json), drawn with...
#   texture  ...its one texture, copied to textures/item/guns/attachments/<texture>.png (ATTACHMENT_TEXTURES)
#   hides_flash  a can over the muzzle: a shot through it shows no muzzle flash (slice 6)
#   stab     a bayonet's stab, in half hearts (slice 7); 0 for the rest
#   mount    a scope (slice 7): the owner made no part of it for each gun, so its own item model is mounted on the guns
#            the owner made to take one (optic_part(); "parts" is unused)
#   zoom     a scope's: the field of view is multiplied by this at full aim, in place of the gun's own ZOOM
#   view     a scope's view through it, aimed (client/guns/GunScope): "reticle" and "vignette" fill the screen, the
#            gun put away; or a reflex sight's "dot" on the middle of the screen, over the gun
# The numbers are starting points for the owner.
ATTACHMENTS = {
    "silencer": {
        "display": "Silencer", "slot": "barrel", "parts": ["silencer"], "replaces": False,
        "effects": {"volume": 0.35, "damage": 0.95}, "model": "silencer", "texture": "muzzle_devices",
        "hides_flash": True,
        "tooltip": "A wool-packed can for the muzzle: a much quieter shot, a little weaker.",
    },
    "baffled_silencer": {
        "display": "Baffled Silencer", "slot": "barrel", "parts": ["advanced_silencer"], "replaces": False,
        "effects": {"volume": 0.2}, "model": "advanced_silencer", "texture": "baffled_silencer",
        "hides_flash": True,
        "tooltip": "Brass baffles in a long can: quieter still, and nothing lost.",
    },
    "muzzle_brake": {
        "display": "Muzzle Brake", "slot": "barrel", "parts": ["muzzle_brake"], "replaces": False,
        "effects": {"kick": 0.5, "aim_spread": 0.85}, "model": "muzzle_brake", "texture": "muzzle_devices",
        "tooltip": "Ports at the muzzle throw the blast aside: half the kick, steadier aimed.",
    },
    "extended_barrel": {
        "display": "Extended Barrel", "slot": "barrel", "parts": ["ext_barrel"], "replaces": True,
        "effects": {"range": 1.3, "hip_spread": 0.85, "aim_spread": 0.85}, "model": "extended_barrel",
        "texture": "extended_barrel",
        "tooltip": "A longer barrel: reaches farther and strays less.",
    },
    "extended_magazine": {
        "display": "Extended Magazine", "slot": "magazine", "parts": ["ext_mag"], "replaces": True,
        "effects": {"capacity": 1.5, "reload": 1.15}, "model": "extended_mag", "texture": "extended_magazine",
        "tooltip": "Half as many rounds again, a little slower to change.",
    },
    "speed_magazine": {
        "display": "Speed Magazine", "slot": "magazine", "parts": ["speed_mag"], "replaces": True,
        "effects": {"reload": 0.65}, "model": "speed_mag", "texture": "speed_magazine",
        "tooltip": "A sprung, flared magazine: changed in two thirds of the time.",
    },
    "light_stock": {
        "display": "Light Stock", "slot": "stock", "parts": ["light_stock", "stock_light"], "replaces": True,
        "effects": {"hip_spread": 0.85}, "model": "light_stock", "texture": "light_stock",
        "tooltip": "A skeleton stock: steadier from the hip.",
    },
    "weighted_stock": {
        "display": "Weighted Stock", "slot": "stock", "parts": ["heavy_stock", "stock_weighted"], "replaces": True,
        "effects": {"kick": 0.6, "aim_spread": 0.7}, "model": "weighted_stock", "texture": "weighted_stock",
        "tooltip": "A heavy stock: soaks up the kick and holds the aim.",
    },
    "wooden_stock": {
        "display": "Wooden Stock", "slot": "stock", "parts": ["wooden_stock", "stock_wooden"], "replaces": True,
        "effects": {"kick": 0.75, "hip_spread": 0.9, "aim_spread": 0.85}, "model": "wooden_stock",
        "texture": "wooden_stock",
        "tooltip": "A plain wooden stock: a little of everything.",
    },
    "light_grip": {
        "display": "Light Grip", "slot": "grip", "parts": ["light_grip", "grip_light"], "replaces": False,
        "effects": {"hip_spread": 0.8}, "model": "light_grip", "texture": "grips",
        "tooltip": "A short grip under the fore-end: steadier from the hip.",
    },
    "vertical_grip": {
        "display": "Vertical Grip", "slot": "grip", "parts": ["vertical_grip", "vert_grip", "grip_vertical"],
        "replaces": False, "effects": {"kick": 0.65}, "model": "vertical_grip", "texture": "grips",
        "tooltip": "A grip to pull the gun down by: less kick.",
    },
    # Slice 7: bayonets, under the barrel (so a gun has a bayonet or a grip). Each stabs (the stab key, V) for "stab"
    # damage; they change none of the gun's numbers. The owner's anthralite bayonet is Jugcraft's steel one.
    "iron_bayonet": {
        "display": "Iron Bayonet", "slot": "grip", "parts": ["iron_bayonet"], "replaces": False, "effects": {},
        "stab": 4.0, "model": "iron_bayonet", "texture": "iron_bayonet",
        "tooltip": "A blade under the muzzle. Stab with it up close.",
    },
    "steel_bayonet": {
        "display": "Steel Bayonet", "slot": "grip", "parts": ["anthralite_bayonet"], "replaces": False, "effects": {},
        "stab": 5.0, "model": "anthralite_bayonet", "texture": "steel_bayonet",
        "tooltip": "A steel blade under the muzzle, wrapped at the grip. Stab with it up close.",
    },
    "diamond_bayonet": {
        "display": "Diamond Bayonet", "slot": "grip", "parts": ["diamond_bayonet"], "replaces": False, "effects": {},
        "stab": 5.0, "model": "diamond_bayonet", "texture": "diamond_bayonet",
        "tooltip": "A diamond blade under the muzzle. Stab with it up close.",
    },
    "netherite_bayonet": {
        "display": "Netherite Bayonet", "slot": "grip", "parts": ["netherite_bayonet"], "replaces": False, "effects": {},
        "stab": 6.0, "model": "netherite_bayonet", "texture": "netherite_bayonet",
        "tooltip": "A netherite blade under the muzzle. Stab with it up close.",
    },
    # The scopes, on the optic slot: they take the place of the gun's iron sights (the owner's "sights" part; their
    # "no_sights" part, a rail on the Riveter, shows with the scope). A scope steadies the aim and narrows the view;
    # the two magnifying ones are clumsy from the hip.
    "long_scope": {
        "display": "Long Scope", "slot": "optic", "mount": True, "parts": [], "replaces": True,
        "effects": {"aim_spread": 0.5, "hip_spread": 1.25}, "model": "long_scope", "texture": "long_scope",
        "zoom": 0.3, "view": {"reticle": "long_scope_reticle2", "vignette": "scope_vignette"},
        "tooltip": "A long blackened-steel scope: a far closer view down the sights, clumsy from the hip.",
    },
    "medium_scope": {
        "display": "Medium Scope", "slot": "optic", "mount": True, "parts": [], "replaces": True,
        "effects": {"aim_spread": 0.7, "hip_spread": 1.1}, "model": "medium_scope", "texture": "medium_scope",
        "zoom": 0.5, "view": {"reticle": "long_scope_reticle2", "vignette": "scope_vignette"},
        "tooltip": "A short scope: a closer view down the sights, a little clumsy from the hip.",
    },
    "reflex_sight": {
        "display": "Reflex Sight", "slot": "optic", "mount": True, "parts": [], "replaces": True,
        "effects": {"aim_spread": 0.85}, "model": "reflex_sight", "texture": "reflex_sight",
        "zoom": 0.85, "view": {"dot": "red_dot_reticle"},
        "tooltip": "A glass window with a red dot on it: quick to aim through, no magnification to speak of.",
    },
}
# The order the effects are listed in (and GunAttachment's fields).
EFFECTS = ("damage", "range", "hip_spread", "aim_spread", "capacity", "reload", "kick", "volume")
# Each slot's standard parts: an attachment rides the bone that holds one (the gun body when the gun has none), and one
# that "replaces" takes its place.
SLOTS = {"barrel": ("stan_barrel", "barrel"), "magazine": ("stan_mag",), "stock": ("stan_grip",), "grip": (),
         "optic": ("sights",)}
# The reticles and lens vignettes the scopes' item models draw and their views show (the owner's, Guns/effect/<name>.png,
# copied to textures/item/guns/optics/<name>.png).
OPTIC_TEXTURES = ("long_scope_reticle2", "scope_vignette", "scope_vignette_circle", "red_dot_reticle")
# The attachments' textures: the owner's (Guns/item/<file>.png), copied under Jugcraft names.
ATTACHMENT_TEXTURES = {
    "muzzle_devices": "greaser_smg_barrels", "baffled_silencer": "advanced_silencer", "extended_barrel": "extended_barrel",
    "extended_magazine": "extended_mag", "speed_magazine": "carabine", "light_stock": "light_stock",
    "weighted_stock": "greaser_smg_stocks", "wooden_stock": "musket_stocks", "grips": "carabine_grips",
    "iron_bayonet": "iron_bayonet", "steel_bayonet": "anthralite_bayonet", "diamond_bayonet": "diamond_bayonet",
    "netherite_bayonet": "netherite_bayonet", "long_scope": "long_scope_texture", "medium_scope": "medium_scope",
    "reflex_sight": "relex_sight",
}
# Attachment recipes, from the same early metal and wood as the guns.
ATTACHMENT_RECIPES = {
    "silencer": (["IWI"], {"I": "minecraft:iron_ingot", "W": "#minecraft:wool"}),
    "baffled_silencer": (["BSB"], {"B": "#c:ingots/brass", "S": f"{MOD}:silencer"}),
    "muzzle_brake": (["NBN"], {"N": "minecraft:iron_nugget", "B": "#c:ingots/brass"}),
    "extended_barrel": (["IIB"], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass"}),
    "extended_magazine": (["I", "B", "I"], {"I": "minecraft:iron_ingot", "B": "#c:ingots/brass"}),
    "speed_magazine": (["B", "S", "B"], {"B": "#c:ingots/brass", "S": "minecraft:slime_ball"}),
    "light_stock": (["SSL"], {"S": "minecraft:stick", "L": "minecraft:leather"}),
    "weighted_stock": (["PPI"], {"P": "#minecraft:planks", "I": "minecraft:iron_ingot"}),
    "wooden_stock": (["PPL"], {"P": "#minecraft:planks", "L": "minecraft:leather"}),
    "light_grip": (["L", "S"], {"L": "minecraft:leather", "S": "minecraft:stick"}),
    "vertical_grip": (["I", "S", "L"], {"I": "minecraft:iron_ingot", "S": "minecraft:stick", "L": "minecraft:leather"}),
    # A blade over a ring that slips onto the muzzle.
    "iron_bayonet": (["I", "N"], {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"}),
    "steel_bayonet": (["S", "N"], {"S": "#c:ingots/steel", "N": "minecraft:iron_nugget"}),
    "diamond_bayonet": (["D", "N"], {"D": "minecraft:diamond", "N": "minecraft:iron_nugget"}),
    # Glass in a brass tube; the long scope is a spyglass in a brass mount; the reflex sight's dot is a glint of
    # redstone behind a pane on an iron foot.
    "long_scope": (["BSB"], {"B": "#c:ingots/brass", "S": "minecraft:spyglass"}),
    "medium_scope": (["BGB"], {"B": "#c:ingots/brass", "G": "minecraft:glass_pane"}),
    "reflex_sight": (["G", "R", "N"], {"G": "minecraft:glass_pane", "R": "minecraft:redstone", "N": "minecraft:iron_nugget"}),
}
# The Netherite Bayonet is a Diamond Bayonet upgraded at a smithing table, as netherite tools are.
NETHERITE_UPGRADES = {"netherite_bayonet": "diamond_bayonet"}


def attachment_part(gun, kind):
    """The part entry this gun shows the attachment with, or None. A scope's is its own item model mounted on the gun
    ("%<kind>", optic_part()), on a gun the owner made to take one (takes_optics()). Any other attachment's is the
    first of its "parts" the gun has, if every face draws on a texture in the library (the gun's own or, since slice 7,
    one the owner shares between guns, merged into the gun's atlas by atlas_layout())."""
    if ATTACHMENTS[kind].get("mount"):
        return f"%{kind}" if takes_optics(gun) else None
    for name in ATTACHMENTS[kind]["parts"]:
        if (LIBRARY / "models" / "special" / GUNS[gun]["source"] / f"{name}.json").exists():
            faces = [face for element in load_part(gun, name).get("elements", []) for face in element.get("faces", {}).values()]
            return name if faces and all(texture_file(face["_texture"]) for face in faces) else None
    return None


def fits(gun):
    """The attachments this gun takes, in ATTACHMENTS order (JugcraftGuns.ACCEPTS)."""
    return [kind for kind in ATTACHMENTS if attachment_part(gun, kind)]


def takes_optics(gun):
    """Whether the owner made the gun to take a scope: among its parts are iron sights and a "no_sights" stand-in (what
    shows under a scope: nothing, or a rail), and its bones carry the sights, so a scope can take their place."""
    folder = LIBRARY / "models" / "special" / GUNS[gun]["source"]
    return ((folder / "sights.json").exists() and (folder / "no_sights.json").exists()
            and any("sights" in parts for _, _, parts, _ in BUILDS[gun]["bones"]))


def attachment_parts(gun, kind):
    """The part entries the attachment's bone holds on this gun: its part, and with a scope the gun's "no_sights" part
    where it has one (the Riveter's rail)."""
    part = attachment_part(gun, kind)
    if ATTACHMENTS[kind].get("mount") and load_part(gun, "no_sights").get("elements"):
        return [part, "no_sights"]
    return [part]


# Where a scope stands on a gun, in owner space, where optic_mount()'s reading of the parts would not do.
MOUNTS = {}


def optic_mount(gun):
    """Where a scope's foot stands on the gun, in owner space: on the centre line, on top of the gun's "no_sights" rail
    where it has one, otherwise where the iron sights stand on the receiver; midway along either."""
    if gun in MOUNTS:
        return MOUNTS[gun]
    rail = part_elements(gun, "no_sights")
    elements = rail or part_elements(gun, "sights")
    ys = [v for e in elements for v in (e["from"][1], e["to"][1])]
    zs = [v for e in elements for v in (e["from"][2], e["to"][2])]
    return (8.0, max(ys) if rail else min(ys), (min(zs) + max(zs)) / 2.0)


def turned_corners(element):
    """The element's eight corners in owner space, turned as vanilla turns it (about its origin, right-handed)."""
    lo, hi = element["from"], element["to"]
    corners = [(x, y, z) for x in (lo[0], hi[0]) for y in (lo[1], hi[1]) for z in (lo[2], hi[2])]
    rotation = element.get("rotation")
    if not rotation or not rotation.get("angle"):
        return corners
    a = math.radians(rotation["angle"])
    c, s = math.cos(a), math.sin(a)
    ox, oy, oz = rotation.get("origin", [8, 8, 8])
    out = []
    for x, y, z in corners:
        x, y, z = x - ox, y - oy, z - oz
        x, y, z = {"x": (x, y * c - z * s, y * s + z * c), "y": (z * s + x * c, y, z * c - x * s),
                   "z": (x * c - y * s, x * s + y * c, z)}[rotation["axis"]]
        out.append((x + ox, y + oy, z + oz))
    return out


def optic_elements(kind):
    """The scope's item model elements that draw only on library textures: the body, not the lens planes that draw on
    its reticle and vignette (the scope's view shows those, GunScope; a gun's atlas has no room for them)."""
    data = json.loads((LIBRARY / "models" / "item" / f"{ATTACHMENTS[kind]['model']}.json").read_text())
    textures = data.get("textures", {})
    kept = []
    for element in data.get("elements", []):
        faces = list(element.get("faces", {}).values())
        for face in faces:
            face["_texture"] = face_texture(textures, face.get("texture", ""))
        if faces and all(texture_file(face["_texture"]) for face in faces):
            kept.append(element)
    return textures, kept


def optic_foot(kind):
    """The point of the scope that stands on a gun, in its own model's space: on the centre line at the bottom of its
    mount (its lowest corner), midway along the elements that reach down to it."""
    _, elements = optic_elements(kind)
    low = {id(e): min(p[1] for p in turned_corners(e)) for e in elements}
    bottom = min(low.values())
    feet = [e for e in elements if low[id(e)] <= bottom + 0.25]
    zs = [p[2] for e in feet for p in turned_corners(e)]
    return (8.0, bottom, (min(zs) + max(zs)) / 2.0)


def mount_offset(gun, kind):
    """How far the scope's model moves to stand on the gun."""
    return tuple(rnd(m - f) for m, f in zip(optic_mount(gun), optic_foot(kind)))


def optic_part(gun, kind):
    """The scope as a part of this gun: its body's elements (optic_elements()) moved so its foot stands on the gun's
    mount."""
    textures, elements = optic_elements(kind)
    d = mount_offset(gun, kind)
    moved = []
    for element in elements:
        element = dict(element, **{"from": [rnd(v + o) for v, o in zip(element["from"], d)],
                                   "to": [rnd(v + o) for v, o in zip(element["to"], d)]})
        if element.get("rotation"):
            origin = element["rotation"].get("origin", [8, 8, 8])
            element["rotation"] = dict(element["rotation"], origin=[rnd(v + o) for v, o in zip(origin, d)])
        moved.append(element)
    return {"textures": textures, "elements": moved}


def optic_sight(gun, kind):
    """Where aiming through the scope puts the middle of the screen (its "sight_<kind>" locator), in owner space: on
    the scope's line of sight (the owner's .scmeta camera height) at its eyepiece (the back of its body)."""
    meta = json.loads((LIBRARY / "models" / "item" / f"{ATTACHMENTS[kind]['model']}.scmeta").read_text())
    camera = meta["scguns:scope"]["camera"]
    d = mount_offset(gun, kind)
    back = max(p[2] for e in optic_elements(kind)[1] for p in turned_corners(e))
    return (rnd(camera[0] + d[0]), rnd(camera[1] + d[1]), rnd(back + d[2]))


def effective_bones(gun):
    """BUILDS' bones with the attachments added. Each slot the gun has attachments for gets, under each bone holding
    one of its standard parts (or under gun_body), an "att_<kind>" bone for each attachment, with that bone's pivot so
    it rides the barrel or magazine as the animations move it; where an attachment replaces the standard part, the part
    moves to a "std_<slot>" bone of its own. A slot on two bones (the Warden Pistol's magazine and the spare its reload
    brings in) gets a second set suffixed "_2". The renderer shows the fitted attachments' bones and hides the standard
    parts they replace (GunRenderer). A slot the gun's "mounts" names a bone for hangs its attachments there instead
    (the Trench Lobber's scopes ride its body, not the leaf sight that flaps)."""
    kinds = fits(gun)
    bones = BUILDS[gun]["bones"]
    anchors = {slot: [name for name, _, parts, _ in bones if set(parts) & set(standard)] or ["gun_body"]
               for slot, standard in SLOTS.items()}
    mounts = {slot: [BUILDS[gun].get("mounts", {})[slot]] if slot in BUILDS[gun].get("mounts", {}) else anchors[slot]
              for slot in SLOTS}
    replaced = {ATTACHMENTS[kind]["slot"] for kind in kinds if ATTACHMENTS[kind]["replaces"]}
    out = []
    for name, parent, parts, pivot in bones:
        children = []
        for slot, standard in SLOTS.items():
            if name in anchors[slot]:
                suffix = "" if anchors[slot].index(name) == 0 else f"_{anchors[slot].index(name) + 1}"
                own = [p for p in parts if p in standard]
                if own and slot in replaced:
                    parts = [p for p in parts if p not in standard]
                    children.append((f"std_{slot}{suffix}", name, own, pivot))
            if name in mounts[slot]:
                suffix = "" if mounts[slot].index(name) == 0 else f"_{mounts[slot].index(name) + 1}"
                children += [(f"att_{kind}{suffix}", name, attachment_parts(gun, kind), pivot)
                             for kind in kinds if ATTACHMENTS[kind]["slot"] == slot]
        out.append((name, parent, parts, pivot))
        out += children
    return out


def attachment_model(kind):
    """The owner's item model for the attachment, its textures renamed to the Jugcraft copies: its own, and a scope's
    reticle and lens vignette (OPTIC_TEXTURES)."""
    model = json.loads((LIBRARY / "models" / "item" / f"{ATTACHMENTS[kind]['model']}.json").read_text())
    texture = f"{MOD}:item/guns/attachments/{ATTACHMENTS[kind]['texture']}"
    effect = "scguns:effect/"
    model["textures"] = {key: f"{MOD}:item/guns/optics/{value.removeprefix(effect)}"
                         if value.startswith(effect) and key != "particle" else texture
                         for key, value in model["textures"].items()}
    return model


# ------------------------------------------------------------------ in use (slice 6)

# How far each gun narrows the view aimed down its sights: the field of view is multiplied by this at full aim
# (client/guns/GunLooks.LOOKS; client/GunFovMixin applies it). Pistols and shotguns a little, rifles more.
ZOOM = {
    "rust_midge": 0.9, "patchwork_carbine": 0.82, "thunderpipe": 0.92, "warden_pistol": 0.9, "riveter_smg": 0.88,
    "haymaker": 0.92, "longhorn_rifle": 0.75, "drover_rifle": 0.8, "coach_gun": 0.9, "duelling_pistol": 0.9,
    "line_musket": 0.82, "bellmouth": 0.92, "bulldog_pistol": 0.9, "marshal_revolver": 0.85, "sapper_revolver": 0.9,
    "sentry_pistol": 0.9, "garrison_rifle": 0.85, "breacher": 0.92,
    # The heavy weapons are fired from the hip as much as aimed: they narrow the view least.
    "trench_lobber": 0.9, "thresher": 0.95, "stoker": 0.95,
    # The energy weapons: the Stormlock is a rifle; the Linesman's arcs find their own way, close in.
    "beam_pistol": 0.9, "stormlock_rifle": 0.8, "linesman": 0.95,
    # The marksman rifles narrow it the most of any iron sights.
    "picket_rifle": 0.7, "ranger_rifle": 0.75, "kestrel_rifle": 0.7,
    # The automatic weapons (slice 9B): the hand guns a little, the air rifle more.
    "rattler_pistol": 0.9, "bronco_smg": 0.9, "squall_rifle": 0.85,
    # The second energy weapons (slice 9C): the rail pistol reaches furthest; the cutting laser's beam is short.
    "spikedriver": 0.85, "seam_cutter": 0.95, "caisson_pistol": 0.9,
}


def two_handed(gun):
    """Whether the gun is held in both hands. A one-handed gun's idle hides the left arm, so BUILDS gives it a
    "hand_pose"; seen from outside (client/guns/GunPose), a two-handed gun brings both arms up, a one-handed one the
    gun arm only."""
    return "hand_pose" not in BUILDS[gun]


def tilt(gun):
    """How far the owner's third-person transform tilts the gun up off the arm, in degrees (the x rotation of its
    "thirdperson_righthand"; 0 for most). Seen from outside, the holder's arms hang that much lower than raised along
    the look (client/guns/GunLooks.TILT, GunPose), so the gun still points where they look: the Gattaler's is made for
    an arm at the hip, and raised like a rifle it pointed at the sky (PR #278's in-game shots)."""
    return float(base_model(gun)["display"]["thirdperson_righthand"].get("rotation", [0, 0, 0])[0])


# The spent case a round leaves where the owner's animations eject one (their "eject_casing" particle cue, mostly at
# the start of shoot and aim_shoot; the Coach Gun's as it breaks open to reload). The owner's casing art
# (Guns/item/<file>.png) is copied to textures/particle/<round>_casing.png for the particle jugcraft:<round>_casing
# (JugcraftGuns.CASINGS). A paper cartridge leaves no case: its cue puffs smoke from the lock instead.
CASINGS = {"light_round": "small_copper_casing", "rifle_round": "large_brass_casing", "buckshot_shell": "shotgun_shell"}
# The animations' particle cues: GunAnimations ejects a casing at EJECT_CUE; the others mark points in a reload that
# the server's timing already covers, and show nothing.
EJECT_CUE = "eject_casing"
QUIET_CUES = ("loaded", "end_reload", "loop_end", "reload_end", "stop_mag_tracking")
# The muzzle flash: the owner's four flash frames, copied to textures/item/guns/flash/flash_<n>.png; each shot shows
# one, turned at random about the barrel. A silencer hides it ("hides_flash").
FLASH_FRAMES = ["muzzleflash", "muzzleflash2", "muzzleflash3", "muzzleflash4"]
FLASH_SOURCE = BLOCKS / "Big Cannons and Mounted Guns" / "textures"
# How big the flash is, across, in the gun model's pixels, by the round: black powder flares widest. A grenade's
# launch and a burst of flame (slice 8C) flash about as wide as buckshot; an energy weapon's discharge (slice 8D) as a
# light round's.
FLASH_SIZE = {"light_round": 5.0, "rifle_round": 7.0, "buckshot_shell": 8.0, "paper_cartridge": 10.0, "grenade": 9.0,
              "minecraft:blaze_powder": 8.0, "energy_cell": 6.0}
# The flash's tint, by the round, where it is not the owner's frames' own white-gold (client/guns/GunLooks
# FLASH_TINTS, multiplied into the frames): an energy weapon's discharge is cyan-white.
FLASH_TINT = {"energy_cell": 0x9FF4FF}


def barrel_front(gun, kind):
    """Where a barrel attachment's flash comes out, in owner space: on the gun's bore (its "muzzle" x and y) at the
    front of the attachment's part, so a muzzle brake or extended barrel moves the flash forward."""
    mx, my, _ = BUILDS[gun]["muzzle"]
    zs = [v for element in part_elements(gun, attachment_part(gun, kind)) for v in (element["from"][2], element["to"][2])]
    return (mx, my, round(min(zs), 4))


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
    # Slice 8C: the Trench Lobber's pump, worked in full after each shot and by halves around its reload.
    "pump": "item/gun_sounds/pump.ogg",
    "pump_half": "item/gun_sounds/pump_half.ogg",
    # Slice 9B: the Squall Rifle's canister knocking home, the ramrod's metal sound under its own name and subtitle.
    "clank": "item/gun_sounds/metal.ogg",
}
# The shell-at-a-time guns' reload_loop names "reload_mag_in"; they push a shell or a round, so they play the insert.
EVENT_OVERRIDES = {"thunderpipe": {"reload_mag_in": "shell_in"}, "haymaker": {"reload_mag_in": "shell_in"},
                   "longhorn_rifle": {"reload_mag_in": "shell_in"}, "drover_rifle": {"reload_mag_in": "shell_in"},
                   "coach_gun": {"reload_mag_in": "shell_in"},
                   # The energy weapons loaded a charge at a time (slice 8D) play the charge going in.
                   "stormlock_rifle": {"reload_mag_in": "insert"}, "linesman": {"reload_mag_in": "insert"},
                   # The Squall Rifle's reload names "metal" for its canister: it clanks, it is no ramrod (slice 9B).
                   "squall_rifle": {"metal": "clank"}}
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
    "bulldog_pistol": "item/heavier_rifle/fire.ogg",
    "marshal_revolver": "item/brass_revolver/fire.ogg",
    "sapper_revolver": "item/brass_pistol/fire.ogg",
    "sentry_pistol": "item/scrapper/fire.ogg",
    "garrison_rifle": "item/scorched_rifle/fire.ogg",
    "breacher": "item/combat_shotgun/fire.ogg",
    # Slice 8C. The library's machine gun and second heavy rifle shots carry another sound pack's copyright tag, so
    # the Thresher fires the new rifle shot: short and sharp, for ten shots a second. The Stoker's is the short burst
    # of the two flamethrower sounds.
    "trench_lobber": "item/grenade_launcher/fire.ogg",
    "thresher": "item/new_rifle/fire.ogg",
    "stoker": "item/flamethrower/fire_2.ogg",
    # Slice 8D: the library's ray gun shot, its shock shot for the Stormlock's bolt and its short laser shot for the
    # Linesman's quick arcs.
    "beam_pistol": "item/raygun/fire.ogg",
    "stormlock_rifle": "item/shock/fire.ogg",
    "linesman": "item/laser/fire.ogg",
    # Slice 9A: the library's sniper, old rifle and iron rifle shots. Its revolver shot carries the same sound pack's
    # copyright tags as the machine gun's, so it is not used either.
    "picket_rifle": "item/scorched_sniper/fire.ogg",
    "ranger_rifle": "item/old_rifle/fire.ogg",
    "kestrel_rifle": "item/iron_rifle/fire.ogg",
    # Slice 9B: the library's short iron rifle crack (its "enchanted" shot, with no ring after it), its second new rifle
    # shot and its air gun shot.
    "rattler_pistol": "item/iron_rifle/enchanted_fire.ogg",
    "bronco_smg": "item/new_rifle/fire_2.ogg",
    "squall_rifle": "item/airgun/fire.ogg",
    # Slice 9C: the library's rail shot, its second laser shot and its plasma shot.
    "spikedriver": "item/rail/fire.ogg",
    "seam_cutter": "item/laser/fire_2.ogg",
    "caisson_pistol": "item/plasma/fire.ogg",
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
    "pump": "Pump racks",
    "pump_half": "Pump slides",
    "clank": "Canister clanks",
}


def items():
    return list(GUNS) + list(AMMO) + [CELL] + list(ATTACHMENTS)


def switches(condition, gun):
    """A gun recipe's load conditions: the guns' switch and any its RECIPE_SWITCHES add, each its own condition (all
    must hold)."""
    return [c for switch in ("guns", *RECIPE_SWITCHES.get(gun, ())) for c in condition(switch)]


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


# The guns' damage types (data/jugcraft/damage_type/<name>.json): their effects, the vanilla tags they join and their
# death messages (plain, with the killer's named item, while fighting). Both come faster than the half second a
# creature is shielded after a hit, so each shot or burst counts (bypasses_cooldown). A bullet is a projectile
# (Projectile Protection guards against it). The Stoker's flame (slice 8C) is fire: fire protection, fire resistance
# and fireproof creatures shrug it off, it burns as fire does, and it pushes nothing back.
DAMAGE_TYPES = {
    "bullet": {"effects": None, "tags": ("is_projectile", "bypasses_cooldown"),
               "death": ("%1$s was shot by %2$s", "%1$s was shot by %2$s using %3$s",
                         "%1$s was shot while fighting %2$s")},
    "flame": {"effects": "burning", "tags": ("is_fire", "no_knockback", "bypasses_cooldown"),
              "death": ("%1$s was burnt to a crisp by %2$s", "%1$s was burnt to a crisp by %2$s using %3$s",
                        "%1$s was burnt to a crisp while fighting %2$s")},
    # The energy weapons' beams and arcs (slice 8D): no projectile and no fire, and they push nothing back, so an arc
    # gun's quick shots keep their mark in reach.
    "zap": {"effects": None, "tags": ("no_knockback", "bypasses_cooldown"),
            "death": ("%1$s was zapped by %2$s", "%1$s was zapped by %2$s using %3$s", "%1$s was zapped while fighting %2$s")},
}


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
            "fabric:load_conditions": switches(condition, gun), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{gun}"}})
    # Slice 8D: the Energy Cell. Charged, it glows (the owner's three frames, run by an .mcmeta); spent, it shows the
    # owner's empty cell: its jugcraft:energy charge is 0, the item's default, when spent.
    lang[f"item.{MOD}.{CELL}"] = CELL_DISPLAY
    lang[f"tooltip.{MOD}.guns.{CELL}"] = CELL_TOOLTIP
    for model in CELL_ART:
        write(assets / "models" / "item" / f"{model}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{model}"}})
    write(assets / "items" / f"{CELL}.json", {"model": {
        "type": "minecraft:condition", "property": "minecraft:has_component", "component": f"{MOD}:energy",
        "ignore_default": True, "on_true": {"type": "minecraft:model", "model": f"{MOD}:item/{CELL}"},
        "on_false": {"type": "minecraft:model", "model": f"{MOD}:item/{CELL}_empty"}}})
    pattern, key, count = CELL_RECIPE
    write(data / "recipe" / f"{CELL}.json", {
        "fabric:load_conditions": condition("guns") + condition("machines"), "type": "minecraft:crafting_shaped",
        "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{CELL}", "count": count}})
    lang[f"hud.{MOD}.guns.in_cells"] = "%s shots in cells"
    lang[f"message.{MOD}.guns.no_charge"] = "Your Energy Cells are spent. Fill them at a Charging Station."
    lang[f"tooltip.{MOD}.guns.charge"] = "Each round: %s JE from your Energy Cells"
    for ammo, (display, tooltip, (pattern, key, count)) in AMMO.items():
        lang[f"item.{MOD}.{ammo}"] = display
        lang[f"tooltip.{MOD}.guns.{ammo}"] = tooltip
        write(assets / "models" / "item" / f"{ammo}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{ammo}"}})
        write(assets / "items" / f"{ammo}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{ammo}"}})
        write(data / "recipe" / f"{ammo}.json", {
            "fabric:load_conditions": condition("guns"), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{ammo}", "count": count}})
    for kind, att in ATTACHMENTS.items():
        lang[f"item.{MOD}.{kind}"] = att["display"]
        lang[f"tooltip.{MOD}.guns.{kind}"] = att["tooltip"]
        write(assets / "models" / "item" / f"{kind}.json", attachment_model(kind))
        write(assets / "items" / f"{kind}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{kind}"}})
        if kind in NETHERITE_UPGRADES:
            write(data / "recipe" / f"{kind}.json", {
                "fabric:load_conditions": condition("guns"), "type": "minecraft:smithing_transform",
                "template": "minecraft:netherite_upgrade_smithing_template", "base": f"{MOD}:{NETHERITE_UPGRADES[kind]}",
                "addition": "minecraft:netherite_ingot", "result": {"id": f"{MOD}:{kind}"}})
            continue
        pattern, key = ATTACHMENT_RECIPES[kind]
        write(data / "recipe" / f"{kind}.json", {
            "fabric:load_conditions": condition("guns"), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{kind}"}})
    # A gun and an attachment it takes, in a crafting grid, give the gun with it fitted (any in the same slot stays in
    # the grid); a gun and shears take its last attachment off (GunAttachmentRecipe).
    for recipe in ("gun_attachment", "gun_attachment_removal"):
        write(data / "recipe" / f"{recipe}.json", {"fabric:load_conditions": condition("guns"), "type": f"{MOD}:{recipe}"})
    for slot in SLOTS:
        # The grip slot also takes the bayonets (slice 7): it is everything under the barrel.
        lang[f"tooltip.{MOD}.guns.slot.{slot}"] = "Under-barrel attachment" if slot == "grip" else f"{slot.capitalize()} attachment"
    lang[f"tooltip.{MOD}.guns.stab"] = "Stab: %s damage (%s)"
    lang[f"key.{MOD}.stab"] = "Stab with bayonet"
    for effect, text in {"damage": "Damage", "range": "Range", "hip_spread": "Spread from the hip",
                         "aim_spread": "Spread aimed", "capacity": "Rounds", "reload": "Reload time", "kick": "Kick",
                         "volume": "Shot sound"}.items():
        lang[f"tooltip.{MOD}.guns.effect.{effect}"] = f"{text} %s"
    lang[f"tooltip.{MOD}.guns.fits"] = "Fits: %s"
    lang[f"tooltip.{MOD}.guns.fitting"] = "Fit it with the gun in a crafting grid; the gun and shears take the last off."
    lang[f"tooltip.{MOD}.guns.fitted"] = "Fitted: %s"
    for name, text in SUBTITLES.items():
        lang[f"subtitles.{MOD}.guns.{name}"] = text
    # The spent casings' particles (JugcraftGuns.CASINGS); their textures are the owner's, copied by write_files().
    for ammo in CASINGS:
        write(assets / "particles" / f"{ammo}_casing.json", {"textures": [f"{MOD}:{ammo}_casing"]})
    lang[f"key.{MOD}.reload"] = "Reload gun"
    lang[f"key.{MOD}.inspect"] = "Inspect gun"
    lang[f"tooltip.{MOD}.guns.ammo"] = "Loaded: %s / %s"
    lang[f"tooltip.{MOD}.guns.stats"] = "Damage %s x %s, %s shots a second, range %s"
    lang[f"hud.{MOD}.guns.ammo"] = "%s / %s"
    lang[f"hud.{MOD}.guns.reloading"] = "Reloading"
    lang[f"message.{MOD}.guns.no_ammo"] = "No %s to load."
    for name, info in DAMAGE_TYPES.items():
        body = {"message_id": f"{MOD}.{name}", "exhaustion": 0.1, "scaling": "when_caused_by_living_non_player"}
        if info["effects"]:
            body["effects"] = info["effects"]
        write(data / "damage_type" / f"{name}.json", body)
        for suffix, text in zip(("", ".item", ".player"), info["death"]):
            lang[f"death.attack.{MOD}.{name}{suffix}"] = text
    # Each vanilla tag file holds every entry of the mod's: the field chemistry branch's damage types join some of the
    # same tags (thermite is fire too), so its entries are written here with the guns'.
    import field_chemistry
    tags = {tag: list(values) for tag, values in field_chemistry.damage_type_tags().items()}
    mine = {}
    for name, info in DAMAGE_TYPES.items():
        for tag in info["tags"]:
            mine.setdefault(tag, []).append(f"{MOD}:{name}")
    for tag, values in mine.items():
        write(data.parent / "minecraft" / "tags" / "damage_type" / f"{tag}.json",
              {"replace": False, "values": tags.get(tag, []) + values})


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

# Owner parts that stand off the gun they were made for, moved into place here, in pixels (the library's files are
# unchanged; the move is checked like the rest, face for face). The Whistler's light stock (slice 9A) stands 3.15 px
# behind and 0.9 px above its place: its wrist block and collars are its wooden and weighted stocks' own, which meet the
# grip, moved by just that, so fitted it floated behind the gun.
PART_SHIFTS = {("whistler", "light_stock"): (0.0, -0.9, -3.15)}


def load_part(gun, part):
    """The owner's part file, each face noting the texture it draws from ("_texture": a texture id), moved by its
    PART_SHIFTS entry if it has one. A scope's "%<kind>" is its item model mounted on the gun (optic_part())."""
    if part.startswith("%"):
        return optic_part(gun, part[1:])
    data = json.loads((LIBRARY / "models" / "special" / GUNS[gun]["source"] / f"{part_file(part)}.json").read_text())
    textures = data.get("textures", {})
    shift = PART_SHIFTS.get((GUNS[gun]["source"], part_file(part)))
    for element in data.get("elements", []):
        for face in element.get("faces", {}).values():
            face["_texture"] = face_texture(textures, face.get("texture", ""))
        if shift:
            element["from"] = [v + d for v, d in zip(element["from"], shift)]
            element["to"] = [v + d for v, d in zip(element["to"], shift)]
            if element.get("rotation"):
                element["rotation"]["origin"] = [v + d for v, d in zip(element["rotation"].get("origin", [8, 8, 8]), shift)]
    return data


def face_texture(textures, ref):
    """The texture id a face's "#key" names in its part file's textures (following "#key" to "#key"), or None."""
    seen = set()
    while ref.startswith("#"):
        key = ref[1:]
        if key in seen or key not in textures:
            return None
        seen.add(key)
        ref = textures[key]
    return ref or None


def texture_file(texture):
    """The library file a texture id ("scguns:item/<name>") names, or None if the library lacks it."""
    prefix = "scguns:item/"
    if texture and texture.startswith(prefix):
        path = LIBRARY / "item" / f"{texture[len(prefix):]}.png"
        return path if path.exists() else None
    return None


def own_texture(gun):
    return f"scguns:item/{GUNS[gun]['source']}"


def gun_textures(gun):
    """Every texture the gun's parts (its attachments' included) draw from, its own first."""
    found = [own_texture(gun)]
    for _, _, parts, _ in effective_bones(gun):
        for part in parts:
            if part.startswith("@"):
                continue
            for element in part_elements(gun, part):
                for face in element.get("faces", {}).values():
                    if face["_texture"] not in found:
                        found.append(face["_texture"])
    return found


def atlas_layout(gun):
    """The gun's atlas (slice 7): its own texture at the top left and, where its attachment parts draw on textures
    the owner shares between guns (bayonets, stocks, grips), those packed into room the own texture leaves free:
    blocks that are clear and that no face's UVs or prop reaches (own_footprint()), largest first, on an 8-pixel grid.
    A square atlas, grown from the own texture's size to 128 at most (tools/check_mod_data.py's texture rule) only
    when they do not fit.
    The textures its scopes draw on come after, piece by piece (they would not fit whole): each rect of pixels their
    faces use (scope_islands()), a pixel apart, into what room is left, largest first, the atlas growing to 128 if
    they need the room.
    Returns ((width, height), {texture id: placement}): a placement is (x, y, width, height) for a whole texture, or
    for a scope's texture {pixel rect: (x, y, width, height)}, the texture placed so that that rect lands where it was
    packed (face_placement())."""
    islands = scope_islands(gun)
    sizes = {texture: texture_size(texture) for texture in gun_textures(gun)}
    own = own_texture(gun)
    ow, oh = sizes[own]
    place = {own: (0, 0, ow, oh)}
    others = sorted((t for t in sizes if t != own and t not in islands), key=lambda t: (-sizes[t][0] * sizes[t][1], t))
    used = own_footprint(gun)
    side = max(ow, oh)
    while others:
        if side > 128:
            raise ValueError(f"{gun}: its shared textures do not fit a 128 x 128 atlas")
        taken = {}
        for texture in others:
            w, h = sizes[texture]
            spot = next(((x, y) for y in range(0, side - h + 1, 8) for x in range(0, side - w + 1, 8)
                         if not used[y:min(y + h, oh), x:min(x + w, ow)].any()
                         and all(x + w <= tx or tx + tw <= x or y + h <= ty or ty + th <= y
                                 for tx, ty, tw, th in taken.values())), None)
            if spot is None:
                break
            taken[texture] = (*spot, w, h)
        else:
            place.update(taken)
            break
        side *= 2
    while islands:
        try:
            place.update(pack_islands(gun, side, place, used, islands, sizes))
            break
        except ValueError:
            if side >= 128:
                raise
            side *= 2  # what is placed stays where it is in the bigger atlas
    return (side, side) if len(place) > 1 else (ow, oh), place


def texture_size(texture):
    """(width, height) of a library texture."""
    from PIL import Image
    with Image.open(texture_file(texture)) as image:
        return image.size


def island_rect(data, face, lo, hi, size):
    """The pixels (x0, y0, x1, y1) a face draws on its texture of this size: whole pixels, at least one each way."""
    u0, v0, u1, v1 = data.get("uv") or default_uv(face, lo, hi)
    x0, x1 = sorted((u0 * size[0] / 16, u1 * size[0] / 16))
    y0, y1 = sorted((v0 * size[1] / 16, v1 * size[1] / 16))
    x0, y0 = math.floor(x0 + 1e-6), math.floor(y0 + 1e-6)
    return x0, y0, max(math.ceil(x1 - 1e-6), x0 + 1), max(math.ceil(y1 - 1e-6), y0 + 1)


def scope_islands(gun):
    """{texture id: the pixel rects the gun's faces draw on it}, for the textures packed piece by piece: those its
    mounted scopes draw on, and any other shared texture as big as the largest atlas, which could never sit whole beside
    the gun's own (slice 8D: the Arc Worker's stocks draw on the Rust Midge's whole 128 px atlas)."""
    found = {}
    for _, _, parts, _ in effective_bones(gun):
        for part in parts:
            if part.startswith("@"):
                continue
            for element in part_elements(gun, part):
                lo = [min(a, b) for a, b in zip(element["from"], element["to"])]
                hi = [max(a, b) for a, b in zip(element["from"], element["to"])]
                for face, data in element.get("faces", {}).items():
                    texture = data["_texture"]
                    if not part.startswith("%") and (texture == own_texture(gun) or texture_size(texture) != (128, 128)):
                        continue
                    found.setdefault(texture, set()).add(island_rect(data, face, lo, hi, texture_size(texture)))
    return found


def pack_islands(gun, side, place, used, islands, sizes):
    """Packs each scope texture's rects into the side x side atlas where nothing is (the own texture's footprint and
    the whole textures placed), a pixel apart, largest first; returns their placements (atlas_layout())."""
    import numpy as np
    free = np.ones((side, side), bool)
    free[:used.shape[0], :used.shape[1]] &= ~used
    for texture, (x, y, w, h) in place.items():
        if texture != own_texture(gun):
            free[y:y + h, x:x + w] = False
    out = {}
    order = sorted(((t, r) for t, rects in islands.items() for r in rects),
                   key=lambda tr: (-(tr[1][2] - tr[1][0]) * (tr[1][3] - tr[1][1]), tr[0], tr[1]))
    for texture, (x0, y0, x1, y1) in order:
        w, h = x1 - x0 + 2, y1 - y0 + 2
        taken = np.zeros((side + 1, side + 1), np.int32)
        taken[1:, 1:] = (~free).astype(np.int32).cumsum(0).cumsum(1)
        sums = taken[h:, w:] - taken[:-h, w:] - taken[h:, :-w] + taken[:-h, :-w]
        spots = np.argwhere(sums == 0)
        if not len(spots):
            raise ValueError(f"{gun}: its scopes' textures do not fit its {side} x {side} atlas")
        y, x = (int(v) for v in spots[0])
        free[y:y + h, x:x + w] = False
        tw, th = sizes[texture]
        out.setdefault(texture, {})[(x0, y0, x1, y1)] = (x + 1 - x0, y + 1 - y0, tw, th)
    return out


def face_placement(place, data, face, lo, hi):
    """Where the texture a face draws on sits in the gun's atlas, for to_atlas(): its whole placement or, for a scope's
    texture packed piece by piece, the placement that lands this face's own pixels where they were packed."""
    placement = place[data["_texture"]]
    if isinstance(placement, dict):
        _, _, w, h = next(iter(placement.values()))
        return placement[island_rect(data, face, lo, hi, (w, h))]
    return placement


def own_footprint(gun):
    """Which pixels of the gun's own texture are in use: drawn on, under any face's UVs, or a prop's block."""
    import numpy as np
    from PIL import Image
    with Image.open(LIBRARY / "item" / f"{GUNS[gun]['source']}.png") as image:
        rgba = np.asarray(image.convert("RGBA"))
    used = rgba[:, :, 3] > 0
    height, width = used.shape
    for _, _, parts, _ in effective_bones(gun):
        for part in parts:
            if part.startswith("@"):
                continue
            for element in part_elements(gun, part):
                lo = [min(a, b) for a, b in zip(element["from"], element["to"])]
                hi = [max(a, b) for a, b in zip(element["from"], element["to"])]
                for face, data in element.get("faces", {}).items():
                    if data["_texture"] != own_texture(gun):
                        continue
                    u0, v0, u1, v1 = data.get("uv") or default_uv(face, lo, hi)
                    x0, x1 = sorted((u0 * width / 16, u1 * width / 16))
                    y0, y1 = sorted((v0 * height / 16, v1 * height / 16))
                    used[max(0, math.floor(y0)):math.ceil(y1) + 1, max(0, math.floor(x0)):math.ceil(x1) + 1] = True
    for prop in PROPS.get(gun, {}).values():
        tu, tv = prop["texture_at"]
        bw, bh = prop_block(prop)
        used[tv:tv + bh, tu:tu + max(bw, 8)] = True
    return used


def to_atlas(corners, placement, size):
    """Corner UVs in 0..16 of a face's own texture -> 0..16 of the gun's atlas, where that texture sits at
    placement (x, y, width, height) in an atlas of this size."""
    x, y, w, h = placement
    return {key: ((x + u * w / 16) * 16 / size[0], (y + v * h / 16) * 16 / size[1]) for key, (u, v) in corners.items()}


def compose_atlas(gun):
    """The gun's atlas as written: the owner's own texture (with any props drawn into it) and the shared textures
    its attachments draw on, placed by atlas_layout(); None when it is the owner's file unchanged."""
    from PIL import Image
    size, place = atlas_layout(gun)
    if gun not in PROPS and len(place) == 1:
        return None
    with Image.open(LIBRARY / "item" / f"{GUNS[gun]['source']}.png") as image:
        own = image.convert("RGBA")
    if gun in PROPS:
        own = draw_props(own, gun)
    if len(place) == 1:
        return own
    atlas = Image.new("RGBA", size, (0, 0, 0, 0))
    for texture, placement in place.items():
        if texture == own_texture(gun):
            atlas.paste(own, placement[:2])
            continue
        with Image.open(texture_file(texture)) as image:
            source = image.convert("RGBA")
        if isinstance(placement, dict):
            # A scope's texture, piece by piece: each rect where atlas_layout() packed it.
            for (x0, y0, x1, y1), (x, y, _, _) in placement.items():
                atlas.paste(source.crop((x0, y0, x1, y1)), (x + x0, y + y0))
        else:
            atlas.paste(source, placement[:2])
    return atlas


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


def element_cube(element, layout):
    """One owner element -> one GeckoLib cube with the same corners, the same UV at each corner (in the gun's atlas,
    layout = atlas_layout()) and the same turn."""
    size_px, place = layout
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
        corners = to_atlas(java_corner_uvs(face, uv, int(data.get("rotation", 0)) % 360),
                           face_placement(place, data, face, lo, hi), size_px)
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
    origin = [rnd(8.0 - (x + sx)), rnd(y), rnd(z - 8.0)]
    if prop["kind"] == "flame":
        return {"origin": origin, "size": [sx, sy, sz], "uv": flame_faces(tu, tv, w, h, d)}
    # One texture block per face (prop_block): the ends side by side, the sides below them, the top and bottom below.
    return {"origin": origin, "size": [sx, sy, sz], "uv": {
        "north": {"uv": [tu, tv], "uv_size": [w, h]}, "south": {"uv": [tu + w, tv], "uv_size": [w, h]},
        "east": {"uv": [tu, tv + h], "uv_size": [d, h]}, "west": {"uv": [tu, tv + h], "uv_size": [d, h]},
        "up": {"uv": [tu, tv + 2 * h], "uv_size": [w, d]}, "down": {"uv": [tu + w, tv + 2 * h], "uv_size": [w, d]}}}


def flame_faces(tu, tv, w, h, d):
    """A flame prop's four long faces, each its own block of the prop's corner (the same w x h x d block as
    prop_block), so the flame's tip can point forward on each: GeckoLib runs u from front to back on the west face but
    back to front on the east, and v from back to front on the top but front to back on the bottom (GECKO_CORNERS).
    The west face's block is first, then the east's below it; the top's and the bottom's side by side below those.
    The ends are left off: the flame is open at both."""
    return {"west": {"uv": [tu, tv], "uv_size": [d, h]}, "east": {"uv": [tu, tv + h], "uv_size": [d, h]},
            "up": {"uv": [tu, tv + 2 * h], "uv_size": [w, d]}, "down": {"uv": [tu + w, tv + 2 * h], "uv_size": [w, d]}}


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
    layout = atlas_layout(gun)
    size_px = layout[0]
    bones = []
    for name, parent, parts, pivot in effective_bones(gun):
        bone = {"name": name, "pivot": geo_point(pivot)}
        if parent:
            bone["parent"] = parent
        cubes = []
        for part in parts:
            if part.startswith("@"):
                cubes.append(prop_cube(gun, part[1:]))
                continue
            for element in part_elements(gun, part):
                cubes.append(element_cube(element, layout))
        if cubes:
            bone["cubes"] = cubes
        if name == "gun_body" and build["sight"] is not None:
            bone["locators"] = {"sight": geo_point(build["sight"])}
        if name in ("barrels",) or (name == "gun_body" and not any(b[0] == "barrels" for b in build["bones"])):
            bone["locators"] = {**bone.get("locators", {}), "muzzle": geo_point(build["muzzle"])}
        kind = name.removeprefix("att_")
        if name.startswith("att_") and kind in ATTACHMENTS and ATTACHMENTS[kind]["slot"] == "barrel":
            # The flash of a gun with this barrel attachment fitted comes from here (GunFlashLayer).
            bone["locators"] = {f"muzzle_{kind}": geo_point(barrel_front(gun, kind))}
        if name.startswith("att_") and kind in ATTACHMENTS and ATTACHMENTS[kind].get("mount"):
            # Aiming through the scope puts this in the middle of the screen (GunRenderer), in place of "sight".
            bone["locators"] = {f"sight_{kind}": geo_point(optic_sight(gun, kind))}
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


def java_faces(element, layout):
    """Each face of the owner's element: [(face, [(owner-space corner, (u, v) in 0..16 of the gun's atlas)])]."""
    size_px, place = layout
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
        corners = to_atlas(java_corner_uvs(face, uv, int(data.get("rotation", 0)) % 360),
                           face_placement(place, data, face, lo, hi), size_px)
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
        layout = atlas_layout(gun)
        size_px = layout[0]
        bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
        for name, parent, parts, pivot in effective_bones(gun):
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
                exp = java_faces(element, layout)
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
            for event in (data.get("particle_effects") or {}).values():
                if event["effect"] != EJECT_CUE and event["effect"] not in QUIET_CUES:
                    problems.append(f"{gun}: animation {name} cues particle {event['effect']}, which nothing shows")
        for kind in fits(gun):
            if ATTACHMENTS[kind]["slot"] == "barrel" and f"muzzle_{kind}" not in bones.get(f"att_{kind}", {}).get("locators", {}):
                problems.append(f"{gun}: the {kind} bone has no muzzle_{kind} locator for the flash")
            if ATTACHMENTS[kind].get("mount") and f"sight_{kind}" not in bones.get(f"att_{kind}", {}).get("locators", {}):
                problems.append(f"{gun}: the {kind} bone has no sight_{kind} locator to aim through")
        # The props' atlas corners must stay clear of every part's faces, the attachments' included.
        for prop_name, prop in PROPS.get(gun, {}).items():
            tu, tv = prop["texture_at"]
            bw, bh = prop_block(prop)
            for _, _, parts, _ in effective_bones(gun):
                for part in parts:
                    if part.startswith("@"):
                        continue
                    for element in part_elements(gun, part):
                        for face in element.get("faces", {}).values():
                            if "uv" not in face or face["_texture"] != own_texture(gun):
                                continue
                            u0, v0, u1, v1 = (c * s / 16.0 for c, s in zip(face["uv"], layout[1][own_texture(gun)][2:] * 2))
                            if min(u0, u1) < tu + bw and max(u0, u1) > tu and min(v0, v1) < tv + bh and max(v0, v1) > tv:
                                problems.append(f"{gun}: the {prop_name} prop's corner overlaps part {part}'s texture")
    from PIL import Image, ImageChops
    for gun, spec in GUNS.items():
        size, place = atlas_layout(gun)
        used = own_footprint(gun)
        ow, oh = place[own_texture(gun)][2:]
        # Each texture placed, whole or (a scope's) rect by rect, inside the atlas, off the own texture's pixels and
        # off every other.
        cover = [[0] * size[0] for _ in range(size[1])]
        for texture, placement in place.items():
            if texture == own_texture(gun):
                continue
            rects = ([(x + x0, y + y0, x1 - x0, y1 - y0) for (x0, y0, x1, y1), (x, y, _, _) in placement.items()]
                     if isinstance(placement, dict) else [placement])
            for x, y, w, h in rects:
                if x < 0 or y < 0 or x + w > size[0] or y + h > size[1]:
                    problems.append(f"{gun}: {texture} is packed outside its atlas")
                    continue
                if used[y:min(y + h, oh), x:min(x + w, ow)].any():
                    problems.append(f"{gun}: {texture} is packed over pixels the gun's own texture uses")
                for row in range(y, y + h):
                    for col in range(x, x + w):
                        cover[row][col] += 1
        if any(n > 1 for row in cover for n in row):
            problems.append(f"{gun}: textures are packed over each other in its atlas")
        if size[0] != size[1] or size[0] > 128:
            problems.append(f"{gun}: its atlas is {size}, not a square of 128 or less")
        texture = ASSETS / "textures" / "item" / "guns" / f"{gun}.png"
        atlas = compose_atlas(gun)
        if not texture.exists():
            continue  # reported with the model
        if atlas is None:
            if texture.read_bytes() != (LIBRARY / "item" / f"{spec['source']}.png").read_bytes():
                problems.append(f"{gun}: its atlas is not the owner's {spec['source']}.png unchanged")
        else:
            with Image.open(texture) as written:
                if written.size != atlas.size or ImageChops.difference(written.convert("RGBA"), atlas).getbbox():
                    problems.append(f"{gun}: its atlas is out of date (run python3 tools/guns.py)")
    for kind, att in ATTACHMENTS.items():
        if not any(kind in fits(gun) for gun in GUNS):
            problems.append(f"attachment {kind}: no gun takes it")
        if (kind not in ATTACHMENT_RECIPES and kind not in NETHERITE_UPGRADES) or att["texture"] not in ATTACHMENT_TEXTURES:
            problems.append(f"attachment {kind}: no recipe or no texture")
        source = json.loads((LIBRARY / "models" / "item" / f"{att['model']}.json").read_text())
        effect = "scguns:effect/"
        own = {v for k, v in source["textures"].items() if k != "particle" and not v.startswith(effect)}
        lenses = {v.removeprefix(effect) for v in source["textures"].values() if v.startswith(effect)}
        if len(own) != 1 or (lenses and not att.get("mount")) or not lenses <= set(OPTIC_TEXTURES):
            problems.append(f"attachment {kind}: the owner's model {att['model']} draws on more than its own texture "
                            "(and a scope's lens textures)")
        if att.get("mount"):
            view = att["view"]
            if not 0.1 <= att["zoom"] <= 1.0 or att["slot"] != "optic" or not set(view.values()) <= set(OPTIC_TEXTURES) \
                    or set(view) not in ({"reticle", "vignette"}, {"dot"}):
                problems.append(f"scope {kind}: its zoom, slot or view is not one the guns can show")
    for name, source in ATTACHMENT_TEXTURES.items():
        target = ASSETS / "textures" / "item" / "guns" / "attachments" / f"{name}.png"
        if not target.exists() or target.read_bytes() != (LIBRARY / "item" / f"{source}.png").read_bytes():
            problems.append(f"attachment texture {name}: not the library's {source}.png unchanged")
    for name in OPTIC_TEXTURES:
        target = ASSETS / "textures" / "item" / "guns" / "optics" / f"{name}.png"
        if not target.exists() or target.read_bytes() != (LIBRARY / "effect" / f"{name}.png").read_bytes():
            problems.append(f"scope texture {name}: not the library's effect/{name}.png unchanged")
    # Slice 6: the look in use.
    if set(ZOOM) != set(GUNS) or not all(0.5 <= z <= 1.0 for z in ZOOM.values()):
        problems.append("ZOOM must give every gun a zoom between 0.5 and 1")
    if not all(0.0 < build.get("eye_relief", 1.0) <= 8.0 for build in BUILDS.values()):
        problems.append("A gun's eye_relief must be more than 0 and at most 8")
    if not set(CASINGS) <= set(AMMO) or set(FLASH_SIZE) != set(AMMO) | set(OTHER_AMMO) | {CELL}:
        problems.append("CASINGS must name rounds, and FLASH_SIZE every round, the other ammunition and the cell")
    if not set(FLASH_TINT) <= set(FLASH_SIZE) or not all(0 <= tint <= 0xFFFFFF for tint in FLASH_TINT.values()):
        problems.append("FLASH_TINT must tint known ammunition, each an RGB colour")
    # Slice 8C: what each gun fires, and with what.
    for gun, spec in GUNS.items():
        if spec.get("shot", "bullet") not in SHOTS or spec["ammo"] not in set(AMMO) | set(OTHER_AMMO) | {CELL}:
            problems.append(f"{gun}: its shot or its ammunition is not one the guns know")
        # Slice 8D: a gun on Energy Cells draws a whole number of JE a round, and a cell fills its magazine at least once.
        charge = spec.get("charge", 0)
        if (spec["ammo"] == CELL) != (charge > 0) or charge and not (isinstance(charge, int) and
                                                                     charge * spec["capacity"] <= CELL_CAPACITY):
            problems.append(f"{gun}: a gun on Energy Cells, and only one, draws a whole JE charge a round, a magazine's "
                            "worth within one cell")
        if spec.get("shot") in ("beam", "arc") and spec["pellets"] != 1:
            problems.append(f"{gun}: a beam or an arc is one a shot")
        if spec.get("spin_up", 0) and not (0 < spec["spin_up"] <= 40 and spec["auto"]):
            problems.append(f"{gun}: a spin-up is for an automatic gun, and at most 40 ticks")
        if spec["capacity"] * max((ATTACHMENTS[k]["effects"].get("capacity", 1.0) for k in fits(gun)), default=1.0) > 64:
            problems.append(f"{gun}: its capacity, with a fitted magazine, is more than a gun holds (64)")
        if spec.get("shot") == "flame" and spec["pellets"] != 1:
            problems.append(f"{gun}: a flame is one jet a shot")
    if not all(isinstance(n, int) and n >= 1 for n in OTHER_AMMO.values()):
        problems.append("OTHER_AMMO: each item loads a whole number of rounds, at least one")
    for name, mounts in ((g, b.get("mounts", {})) for g, b in BUILDS.items()):
        if not set(mounts) <= set(SLOTS) or not set(mounts.values()) <= {bone for bone, _, _, _ in BUILDS[name]["bones"]}:
            problems.append(f"{name}: its mounts name a slot or a bone it lacks")
    for gun in GUNS:
        # The arms hang lower by the tilt in either hand (GunPose); a turn about another axis they could not take up.
        display = base_model(gun)["display"]
        turns = [display.get(f"thirdperson_{hand}", {}).get("rotation", [0, 0, 0]) for hand in ("righthand", "lefthand")]
        if turns[0] != turns[1] or any(turns[0][1:]):
            problems.append(f"{gun}: its third-person transforms turn it other than tilting it, the same in both hands")
    for ammo, casing in CASINGS.items():
        target = ASSETS / "textures" / "particle" / f"{ammo}_casing.png"
        if not target.exists() or target.read_bytes() != (LIBRARY / "item" / f"{casing}.png").read_bytes():
            problems.append(f"casing {ammo}: {target.relative_to(ROOT)} is not the library's {casing}.png unchanged")
    for n, frame in enumerate(FLASH_FRAMES):
        target = ASSETS / "textures" / "item" / "guns" / "flash" / f"flash_{n}.png"
        if not target.exists() or target.read_bytes() != (FLASH_SOURCE / f"{frame}.png").read_bytes():
            problems.append(f"flash {n}: {target.relative_to(ROOT)} is not the library's {frame}.png unchanged")
    for name, path in {**EVENT_SOUNDS, **{f"{g}.fire": p for g, p in SHOT_SOUNDS.items()}}.items():
        target = ASSETS / "sounds" / "guns" / f"{sound_file(path)}.ogg"
        if not target.exists() or target.read_bytes() != (LIBRARY / "sounds" / path).read_bytes():
            problems.append(f"sound {name}: {target.relative_to(ROOT)} is not the library file unchanged")
    # Slice 8D: the Energy Cell's art is the owner's, unchanged, the charged cell's frames run by our .mcmeta.
    for name, source in CELL_ART.items():
        target = ASSETS / "textures" / "item" / f"{name}.png"
        if not target.exists() or target.read_bytes() != (LIBRARY / "item" / f"{source}.png").read_bytes():
            problems.append(f"{name}: {target.relative_to(ROOT)} is not the library's {source}.png unchanged")
    meta = ASSETS / "textures" / "item" / f"{CELL}.png.mcmeta"
    if not meta.exists() or json.loads(meta.read_text()) != CELL_ANIMATION:
        problems.append(f"{meta.relative_to(ROOT)} must run the cell's frames (CELL_ANIMATION)")
    if not (0 < ARC_HOPS <= 4 and 0.0 < ARC_REACH <= 8.0 and 0.0 < ARC_SHARE < 1.0):
        problems.append("An arc leaps on at most four times, at most 8 blocks, each hop weaker")
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
        {"buckshot": draw_buckshot, "cartridge": draw_cartridge, "flame": draw_flame}.get(prop["kind"], draw_plain)(atlas, prop)
    return atlas


def draw_flame(atlas, prop):
    """The owner's pilot flame (FLAME_SOURCE's first frame, 4 x 8 px, tip at the top) laid along each long face of the
    prop, its tip at the front (flame_faces() places the four blocks; the prop is 4 x 4 x 8 px). Its clear pixels stay
    clear, so the flame keeps its shape."""
    from PIL import Image
    tu, tv = prop["texture_at"]
    w, h, d = prop_dims(prop)
    source = Image.open(FLAME_SOURCE).convert("RGBA")
    flame = [[source.getpixel((2 + col, row)) for col in range(4)] for row in range(8)]  # flame[row from tip][col]
    if (w, h, d) != (4, 4, 8):
        raise ValueError("a flame prop is 4 x 4 x 8 px, the owner's flame laid along it")
    for along in range(d):  # 0 at the front (the tip)
        for across in range(4):
            pixel = flame[along][across]
            atlas.putpixel((tu + along, tv + across), pixel)                  # west: u from the front
            atlas.putpixel((tu + d - 1 - along, tv + h + across), pixel)      # east: u from the back
            atlas.putpixel((tu + across, tv + 2 * h + d - 1 - along), pixel)  # top: v from the back
            atlas.putpixel((tu + w + across, tv + 2 * h + along), pixel)      # bottom: v from the front
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
    # A fuel can: steel ends, a brass body.
    "canister": (((150, 154, 162, 255), (104, 108, 116, 255)), ((214, 170, 72, 255), (150, 112, 44, 255)),
                 ((150, 154, 162, 255), (104, 108, 116, 255))),
    # An Energy Cell (slice 8D), in the owner's cell art's colours: a steel cap, the green glass, a copper cap.
    "cell": (((120, 111, 107, 255), (74, 70, 68, 255)), ((74, 144, 53, 255), (49, 86, 24, 255)),
             ((88, 72, 61, 255), (57, 50, 45, 255))),
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
        atlas = compose_atlas(gun)
        if atlas is not None:
            atlas.save(texture)
        else:
            shutil.copyfile(LIBRARY / "item" / f"{spec['source']}.png", texture)
    for name, source in ATTACHMENT_TEXTURES.items():
        target = ASSETS / "textures" / "item" / "guns" / "attachments" / f"{name}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "item" / f"{source}.png", target)
    for name in OPTIC_TEXTURES:
        target = ASSETS / "textures" / "item" / "guns" / "optics" / f"{name}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "effect" / f"{name}.png", target)
    for ammo, casing in CASINGS.items():
        target = ASSETS / "textures" / "particle" / f"{ammo}_casing.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "item" / f"{casing}.png", target)
    for n, frame in enumerate(FLASH_FRAMES):
        target = ASSETS / "textures" / "item" / "guns" / "flash" / f"flash_{n}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(FLASH_SOURCE / f"{frame}.png", target)
    for path in sorted({*EVENT_SOUNDS.values(), *SHOT_SOUNDS.values()}):
        target = ASSETS / "sounds" / "guns" / f"{sound_file(path)}.ogg"
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(LIBRARY / "sounds" / path, target)
    # The Energy Cell's art (slice 8D): the owner's, unchanged; the charged cell's three frames run by our .mcmeta.
    for name, source in CELL_ART.items():
        shutil.copyfile(LIBRARY / "item" / f"{source}.png", ASSETS / "textures" / "item" / f"{name}.png")
    dump(ASSETS / "textures" / "item" / f"{CELL}.png.mcmeta", CELL_ANIMATION)


def provenance():
    """Path (under the owner's Blocks folder) and SHA-256 of every owner file the guns use, for
    docs/features/guns.md."""
    rows = []
    for gun, spec in GUNS.items():
        src = spec["source"]
        paths = [f"models/item/{src}.json", f"item/{src}.png", f"item/{src}.animation.json"]
        for _, _, parts, _ in effective_bones(gun):
            paths += [f"models/special/{src}/{part_file(p)}.json" for p in parts if not p.startswith(("@", "%"))]
        paths.append(f"sounds/{SHOT_SOUNDS[gun]}")
        # The shared textures its attachments draw on, merged into its atlas (slice 7).
        paths += [str(texture_file(t).relative_to(LIBRARY)) for t in atlas_layout(gun)[1] if t != own_texture(gun)]
        rows += [(gun, p) for p in dict.fromkeys(paths)]  # a part on two bones (a spare magazine) counts once
        # A flame prop's pixels are the owner's pilot flame (slice 8C).
        if any(prop["kind"] == "flame" for prop in PROPS.get(gun, {}).values()):
            rows.append((gun, str(FLAME_SOURCE.relative_to(LIBRARY))))
    rows += [("shared", f"sounds/{p}") for p in sorted(set(EVENT_SOUNDS.values()))]
    rows += [(kind, f"models/item/{att['model']}.json") for kind, att in ATTACHMENTS.items()]
    # A scope's .scmeta gives the height of its line of sight (optic_sight()).
    rows += [(kind, f"models/item/{att['model']}.scmeta") for kind, att in ATTACHMENTS.items() if att.get("mount")]
    rows += [(name, f"item/{source}.png") for name, source in ATTACHMENT_TEXTURES.items()]
    rows += [(name, f"effect/{name}.png") for name in OPTIC_TEXTURES]
    rows += [(f"{ammo}_casing", f"item/{casing}.png") for ammo, casing in CASINGS.items()]
    rows += [(name, f"item/{source}.png") for name, source in CELL_ART.items()]
    rows = [(owner, LIBRARY / path) for owner, path in rows]
    rows += [(f"flash_{n}", FLASH_SOURCE / f"{frame}.png") for n, frame in enumerate(FLASH_FRAMES)]
    return [(owner, path.relative_to(BLOCKS).as_posix(), hashlib.sha256(path.read_bytes()).hexdigest())
            for owner, path in rows]


if __name__ == "__main__":
    if "--provenance" in sys.argv:
        for gun, path, digest in provenance():
            print(f"| {gun} | `{path}` | `{digest[:16]}` |")
        sys.exit(0)
    if "--check" not in sys.argv:
        write_files()
    found = check()
    for problem in found:
        print("guns:", problem)
    print("guns: PASS" if not found else f"guns: {len(found)} problem(s)")
    sys.exit(1 if found else 0)
