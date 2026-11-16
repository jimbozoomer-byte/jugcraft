"""Concordance Chunk E, E4 (docs/features/lair-loadouts.md): how long each lair keeper takes to bring down, alone, with
each of six loadouts, melee, magic and guns, worked out from the game's own numbers. It is a model, not a playtest: it
counts only the seconds spent hitting the keeper, never its waking, its unhurt phase changes, its movement or the
player's. It gives two figures for each keeper and loadout:

- **open**: every blow lands while the keeper's rule is answered (Vesperine's skulls down, the Cinder Tyrant quenched,
  the Crypt Lich exposed, the Iron Dreadnought's vents open, the Alpha Werewolf flushed, the Cydonian Sentinel's core
  open, the Pumpkin King bereft, the Headless Horseman revealed, the Yeti King stunned);
- **shut**: every blow lands while it is not.

A real fight is between the two. Run it to print the record's table (python3 tools/lair_loadouts.py);
tools/check_mod_data.py (check_lair_loadouts) holds the record's table to it and the loadouts' numbers to the game's.

Units: health and damage in half hearts, time in ticks (20 a second). One player, the server's default lairs settings.
"""
import alpha_werewolf as aw
import arms
import cinder_tyrant as ct
import concordance as co
import crypt_lich as cl
import cydonian_sentinel as cs
import gear
import guns
import hessian_horseman as hh
import iron_dreadnought as idn
import mire_hag as mh
import pumpkin_king as pk
import tatterlace as tt
import vesperine as vs
import yeti_king as yk

TICKS = 20
# Vanilla: a blow within HURT_IMMUNITY ticks of the last that counted deals only what it has over that one (an equal
# blow, nothing); bullets skip it (#minecraft:bypasses_cooldown holds jugcraft:bullet).
HURT_IMMUNITY = 10
# The longest fight worked out: past it a cell reads "> 30 min".
LONGEST = 30 * 60 * TICKS

# The silver dagger: agriculture/JugcraftAgriculture.java's sword(Werewolf.SILVER, 2.5F, -1.8F) on SILVER's 1.5 bonus
# (check_lair_loadouts holds these to the Java).
SILVER_BONUS = 1.5
DAGGER_DAMAGE = 2.5
DAGGER_SPEED = -1.8
# A sword's own 3 and the hand's 1 (the iron sword's 6 is 1 + 3 + iron's 2), at 1.6 attacks a second (4 - 2.4).
SWORD_DAMAGE = 3.0
SWORD_SPEED = -2.4
HAND = 1.0
ATTACKS = 4.0


def _metal(metal):
    """A metal's attack bonus (tools/gear.py GEAR_TIERS' tool tuple: durability, speed, attack bonus, enchantability)."""
    return gear.GEAR_TIERS[metal]["tool"][2]


def _melee(name, damage, speed, silver=False):
    return {"name": name, "kind": "melee", "damage": damage, "interval": TICKS / (ATTACKS + speed), "silver": silver}


def _gun(key):
    spec = guns.GUNS[key]
    return {"name": spec["display"], "kind": "gun", "damage": spec["damage"] * spec["pellets"], "interval": spec["interval"],
            "capacity": spec["capacity"], "reload": spec["reload"], "pierce": guns.PIERCING.get(key, 0.0)}


def _lance():
    """The Lance of Dawn, mastered, with no Spell Power gear: its beam's magnitude, its Focus and its cooldown; armour does
    not reduce it."""
    spell = co.INVOCATIONS["lance"]
    beam = co.COMPONENTS["lance_beam"]["operation"]
    return {"name": f"{spell['name']} alone", "kind": "magic", "damage": float(beam["magnitude"]), "interval": spell["cooldown"] * TICKS,
            "focus": spell["mastered_focus"], "pool": co.FOCUS_MAX, "regen": co.FOCUS_REGEN_TICKS}


# The loadouts, in the table's order: a steel sword and a steel battle axe (Arms), the silver dagger, the Lance of Dawn,
# and two steel guns, an automatic that pierces armour and a heavy semi-automatic.
LOADOUTS = [
    _melee("Steel Sword", HAND + SWORD_DAMAGE + _metal("steel"), SWORD_SPEED),
    _melee("Steel Battle Axe", HAND + arms.KINDS["battle_axe"]["damage"] + _metal("steel"), arms.KINDS["battle_axe"]["speed"]),
    _melee("Silver Dagger", HAND + SILVER_BONUS + DAGGER_DAMAGE, DAGGER_SPEED, silver=True),
    _lance(),
    _gun("garrison_rifle"),
    _gun("ranger_rifle"),
]


# Each keeper: its name, health, armour, and what it takes of a blow with its rule answered (open) and not (shut), for
# any blow and, where it differs, a silver one; the Cydonian Sentinel's open core lets no armour count; the Crypt Lich
# warded takes nothing; and the rule in a few words.
KEEPERS = [
    {"name": "Vesperine", "numbers": vs.VESPERINE, "taken": (1.0, vs.VESPERINE["GUARD"]),
     "rule": "her skulls guard her: strike them down"},
    {"name": "Madame Tatterlace", "numbers": tt.TATTERLACE, "taken": (1.0, 1.0), "rule": "keep your footing on her lace"},
    {"name": "The Yeti King", "numbers": yk.YETI_KING, "taken": (yk.YETI_KING["STUN_TAKEN"], 1.0),
     "rule": "lead his charge into an ice column"},
    {"name": "The Cinder Tyrant", "numbers": ct.CINDER_TYRANT, "taken": (ct.CINDER_TYRANT["QUENCH_TAKEN"], ct.CINDER_TYRANT["HOT_TAKEN"]),
     "rule": "quench him in a flooded trough"},
    {"name": "The Mire Hag", "numbers": mh.MIRE_HAG, "taken": (1.0, 1.0), "rule": "relight the lamps to find her"},
    {"name": "The Crypt Lich", "numbers": cl.CRYPT_LICH, "taken": (1.0, 0.0), "rule": "break his seals to expose him"},
    {"name": "The Iron Dreadnought", "numbers": idn.IRON_DREADNOUGHT, "taken": (1.0, idn.IRON_DREADNOUGHT["PLATED"]),
     "rule": "throw the lit pylon's breaker"},
    {"name": "The Alpha Werewolf", "numbers": aw.ALPHA_WEREWOLF, "taken": (1.0, aw.ALPHA_WEREWOLF["HIDE"]),
     "silver": (aw.ALPHA_WEREWOLF["FLUSHED_SILVER"], 1.0), "rule": "ring the bell to flush him; silver bites"},
    {"name": "The Cydonian Sentinel", "numbers": cs.SENTINEL, "taken": (cs.SENTINEL["CORE_MULTIPLIER"], 1.0),
     "bare_open": True, "rule": "strike its open core"},
    {"name": "The Pumpkin King", "numbers": pk.PUMPKIN_KING, "taken": (pk.PUMPKIN_KING["BEREFT"], 1.0),
     "rule": "pick his harvest clean to leave him bereft"},
    {"name": "The Headless Horseman", "numbers": hh.HESSIAN_HORSEMAN,
     "taken": (hh.HESSIAN_HORSEMAN["REVEALED"], hh.HESSIAN_HORSEMAN["HIDDEN"]), "rule": "relight the braziers he snuffs"},
]


def armour(damage, points):
    """Vanilla's armour (toughness 0, as every keeper's): what a blow of damage leaves through points of armour."""
    if points <= 0:
        return damage
    counted = min(max(points - damage / 2.0, points * 0.2), 20.0)
    return damage * (1.0 - counted / 25.0)


def ticks_to_kill(keeper, loadout, opened):
    """Ticks of hitting before the keeper falls; past LONGEST, infinity; None if no blow can hurt it (the Crypt Lich warded)."""
    numbers = keeper["numbers"]
    health = numbers["HEALTH"]
    index = 0 if opened else 1
    taken = (keeper.get("silver") or keeper["taken"])[index] if loadout.get("silver") else keeper["taken"][index]
    if taken <= 0:
        return None
    points = 0.0 if opened and keeper.get("bare_open") else numbers["ARMOR"]
    kind = loadout["kind"]
    if kind == "gun":
        points = max(0.0, points - loadout["pierce"])
    blow = loadout["damage"] * taken if kind == "magic" else armour(loadout["damage"] * taken, points)
    tick, last, focus, ammo = 0.0, None, loadout.get("pool", 0), loadout.get("capacity", 0)
    while tick <= LONGEST:
        if kind == "magic":
            # Focus comes back one a REGEN ticks up to the pool; a cast waits for its cost.
            if focus < loadout["focus"]:
                tick += loadout["regen"] * (loadout["focus"] - focus)
                focus = loadout["focus"]
            focus -= loadout["focus"]
            health -= blow
        elif kind == "gun":
            if ammo == 0:
                tick += loadout["reload"]
                ammo = loadout["capacity"]
            ammo -= 1
            health -= blow
        elif last is None or tick - last >= HURT_IMMUNITY:
            last = tick
            health -= blow
        if health <= 0:
            return tick
        if kind == "magic":
            regained = int(loadout["interval"] // loadout["regen"])
            focus = min(loadout["pool"], focus + regained)
        tick += loadout["interval"]
    return float("inf")


def seconds(ticks):
    if ticks is None:
        return "never"
    if ticks > LONGEST:
        return "> 30 min"
    total = round(ticks / TICKS)
    return f"{total} s" if total < 120 else f"{total // 60} min {total % 60:02d} s"


def rows():
    """The table: for each keeper, each loadout's open / shut time."""
    out = []
    for keeper in KEEPERS:
        out.append([keeper["name"], *[f"{seconds(ticks_to_kill(keeper, loadout, True))} / {seconds(ticks_to_kill(keeper, loadout, False))}"
                                      for loadout in LOADOUTS]])
    return out


def table():
    """The record's table, as markdown."""
    head = ["Keeper (open / shut)", *[loadout["name"] for loadout in LOADOUTS]]
    lines = ["| " + " | ".join(head) + " |", "|" + "---|" * len(head)]
    lines += ["| " + " | ".join(row) + " |" for row in rows()]
    return "\n".join(lines)


def rules():
    """The keepers' rules, as the record's second table: what each fight asks, and what it takes of a blow."""
    lines = ["| Keeper | Health | Armour | The rule | Takes, open / shut |", "|---|---|---|---|---|"]
    for keeper in KEEPERS:
        numbers = keeper["numbers"]
        taken = " / ".join(f"×{value:g}" for value in keeper["taken"])
        if keeper.get("silver"):
            taken += " (silver " + " / ".join(f"×{value:g}" for value in keeper["silver"]) + ")"
        if keeper.get("bare_open"):
            taken += ", no armour open"
        lines.append(f"| {keeper['name']} | {numbers['HEALTH']:g} | {numbers['ARMOR']:g} | {keeper['rule']} | {taken} |")
    return "\n".join(lines)


if __name__ == "__main__":
    print(rules())
    print()
    print(table())
