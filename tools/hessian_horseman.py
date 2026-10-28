"""The Headless Horseman in his lair (docs/features/hessian-horseman.md; the plan in docs/branches/BOSSES.md, "The Headless
Horseman in his lair: the plan"): the numbers the Java (src/main/java/.../lair/horseman, weapons/DreadBoon.java) and the
data (tools/hessian_horseman_data.py) share. tools/check_mod_data.py checks the Java against them, and where his fight
looks for the hollow's parts against tools/church_bridge.py (his hollow, part 1: docs/features/church-bridge.md).

He is not the Headless Horseman summoned at midnight in the Halloween season (agriculture/HeadlessHorseman.java,
docs/features/halloween-nights.md), who rides the Overworld as he always has: this is the same rider at rest out of the
season, in the hollow where the legend buried him, fought as a lair boss. His id is hessian_horseman; both are named the
Headless Horseman.

Units: health and damage in half hearts (vanilla's points), times in ticks (20 a second), distances in blocks; his
reaches are measured from his middle. Damage and health are before the server's lairs.boss_damage and
lairs.boss_health scaling (Vesperine's settings, which every lair boss shares: 0.25 to 4, 1 by default).
"""

# HessianHorsemanEntity: he, his pacing and his rule.
HESSIAN_HORSEMAN = {
    "HEALTH": 420.0, "ARMOR": 8.0,
    "LEASH": 24.0,               # farther than this from the hollow's middle, he is set back in it
    "PARTY_STEP": 0.5, "PARTY_MAX": 2.5,
    "WAKE_TICKS": 50,            # waking: his horse rears, his lantern bursts into flame, and he laughs; unhurt
    "ABANDON_TICKS": 200, "GLOBAL_COOLDOWN": 16,
    "SPEED": 0.24,               # trotting after his foe: a little slower than a sprinting player (0.28)
    "KEEP_DISTANCE": 3.0,
    "STEP": 1.6,                 # his horse steps over the road's split rails (a fence's height) and the fallen trunks
    "WAKES_AT": 0.5,             # at this share of his health, the Hollow Wakes
    "LAST_AT": 0.25,             # below this share, the Last Ride
    # The Hollow Wakes: he rides back to the bridge's foot (RIDE_BACK_TICKS), lifts his lantern high (WAKES_TICKS,
    # unhurt), every brazier gutters, and WAKES_SNUFFED of them go dark, a snuffing like any other.
    "RIDE_BACK_TICKS": 30, "WAKES_TICKS": 40, "WAKES_SNUFFED": 2,
    # His rule, the Dark Road. Every SNUFF_EVERY ticks (CHARGE_EVERY in the Headless Charge, LAST_EVERY in the Last Ride)
    # he wheels and lifts his lantern (SNUFF_TICKS) and snuffs SNUFFED braziers (LAST_SNUFFED in the Last Ride): the one
    # nearest the player he hunts, and on round the ring toward whichever of its neighbours is nearer them. Any player
    # lights a dark brazier by using it. While HIDDEN_DARK or more are dark he is hidden: every blow does HIDDEN times its
    # damage. Every brazier of a snuffing lit again within RELIGHT_TICKS, he is revealed: his horse rears (REVEAL_STAGGER)
    # and for REVEALED_TICKS every blow does REVEALED times its damage.
    "SNUFF_EVERY": 480, "CHARGE_EVERY": 360, "LAST_EVERY": 300,
    "SNUFF_TICKS": 24, "SNUFFED": 2, "LAST_SNUFFED": 3,
    "RELIGHT_TICKS": 200,
    "HIDDEN_DARK": 4, "HIDDEN": 0.5,
    "REVEAL_STAGGER": 40, "REVEALED_TICKS": 160, "REVEALED": 1.5,
    # Sabre Pass: a gallop along a line from him through his foe's place, PASS_LENGTH long (never off the hollow's floor
    # nor onto the bridge), over its active ticks: PASS_DAMAGE to each player he rides through (within PASS_HALF_WIDTH of
    # the line), thrown aside (PASS_THROW).
    "PASS_DAMAGE": 9.0, "PASS_LENGTH": 16.0, "PASS_HALF_WIDTH": 1.2, "PASS_THROW": 0.9,
    # Pumpkin Toss: a flaming pumpkin from his saddle onto a ring of embers where his foe stood (at most TOSS_RANGE
    # away), flying TOSS_FLIGHT ticks: TOSS_DAMAGE within TOSS_SPLASH of where it lands, and set alight TOSS_FIRE_SECONDS.
    "TOSS_DAMAGE": 7.0, "TOSS_SPLASH": 3.0, "TOSS_FIRE_SECONDS": 3, "TOSS_RANGE": 20.0, "TOSS_FLIGHT": 12,
    # Rearing Stomp: his horse rears and comes down: STOMP_DAMAGE within STOMP_REACH.
    "STOMP_DAMAGE": 6.0, "STOMP_REACH": 3.5,
    # Head Hurl: the blazing jack-o'-lantern from his saddle, at the farthest player he can see (anyone on the bridge
    # first), onto a pillar of flame where they stood (at most HURL_RANGE away), flying HURL_FLIGHT ticks: HURL_DAMAGE
    # within HURL_SPLASH, and set alight HURL_FIRE_SECONDS.
    "HURL_DAMAGE": 12.0, "HURL_SPLASH": 2.5, "HURL_FIRE_SECONDS": 4, "HURL_RANGE": 30.0, "HURL_FLIGHT": 16,
    # Phantom Riders: blue fire runs along RIDERS lines across the hollow through his foe's place, crossing there at
    # right angles, each a chord of the hollow; a ghostly rider gallops down each over the attack's active ticks:
    # RIDER_DAMAGE to each player it rides through (within RIDER_HALF_WIDTH).
    "RIDERS": 2, "RIDER_DAMAGE": 6.0, "RIDER_HALF_WIDTH": 1.2,
    "EXPERIENCE": 350,
}
# His attacks (HessianHorsemanEntity.Attack): wind-up, active and recovery ticks, cooldown, the range he uses it from
# (blocks), and in which phases ("ride": the Ride; "charge": the Headless Charge and the Last Ride). The wind-up is what
# players read; the strike lands as it ends. The Sabre Pass's active ticks are his gallop, the Pumpkin Toss's and the Head
# Hurl's their flight, and the Phantom Riders' the riders' gallop.
ATTACKS = {
    "SABRE_PASS": (20, 20, 14, 100, 4, 16, True, True),
    "PUMPKIN_TOSS": (20, 12, 10, 90, 5, 20, True, True),
    "REARING_STOMP": (16, 1, 14, 60, 0, 3.5, True, True),
    "HEAD_HURL": (30, 16, 14, 200, 6, 30, False, True),
    "PHANTOM_RIDERS": (30, 24, 12, 260, 0, 40, False, True),
}
# The attacks he can still use on a foe who stands on the bridge, which his horse balks at: he throws.
THROWN = ("PUMPKIN_TOSS", "HEAD_HURL")
# His phantom riders (PhantomRiderEntity): ghosts of his troop in blue fire, galloping down their lines. They cannot be
# hurt, drop nothing, give no experience and are never saved; RIDERS at most, gone with him.
PHANTOM = {"MAX": 2}

# His loot, rolled for each player paid for his fall (HorsemanLoot, through lair/LairRewards): the Horseman's Lantern (his
# old drop, a lantern block), a chance of one of his two trophies (the Arms VII variants his bosses/hessian_horseman table
# holds; one is certain on a player's first win), and the chance of each of the rest. The lair opens only out of the
# Halloween season, so no event candy is added.
TROPHIES = ("hollowflail", "hessian_sabre")
TROPHY_CHANCE = 0.15
ALWAYS = ("horseman_lantern",)
CHANCES = {"hessians_spur": 0.25, "hessians_tricorn": 0.2}
ADVANCEMENT = {"key": "laid_to_rest", "title": "Laid to Rest", "icon": "jugcraft:hessian_sabre",
               "description": "Light the braziers he snuffs, and lay the Headless Horseman to rest by the Church Bridge",
               "frame": "challenge"}
# The Hessian's Spur: in the offhand, the horse its holder rides gallops SPUR_SPEED faster at night (a movement-speed
# modifier on the horse, added and taken away every SPUR_CHECK ticks; never saved).
SPUR = {"SPUR_SPEED": 0.2, "SPUR_CHECK": 10}

ENTITIES = {
    "hessian_horseman": "The Headless Horseman",
    "phantom_rider": "Phantom Rider",
    "hessian_pumpkin": "The Horseman's Pumpkin",
    "hessian_head": "The Horseman's Lantern Head",
}
# The entities GeckoLib draws, and the model each is. Each glows somewhere (his lantern and the embers at his collar, the
# riders' blue fire, the pumpkins' fire), so each has a glowmask and a glow layer.
GECKO = {"hessian_horseman": "hessian_horseman", "phantom_rider": "phantom_rider", "hessian_pumpkin": "hessian_pumpkin",
         "hessian_head": "hessian_head"}
GLOWING = ("hessian_horseman", "phantom_rider", "hessian_pumpkin", "hessian_head")
ITEMS = {"hessians_spur": "Hessian's Spur"}
COSTUMES = {"hessians_tricorn": "Hessian's Tricorn"}

MESSAGES = {
    "wakes": "His horse rears out of the dark, his lantern bursts into flame, and the Headless Horseman laughs",
    "snuff": "He rides the Dark Road: the %s and the %s braziers go dark. Light them again!",
    "snuff_three": "He rides the Dark Road: the %s, the %s and the %s braziers go dark. Light them again!",
    "dark_bar": "The Headless Horseman: hidden in the dark",
    "relit": "Brazier lit: %s of %s",
    "revealed": "Every brazier burns again: the Headless Horseman is revealed!",
    "revealed_bar": "The Headless Horseman: revealed!",
    "hollow_wakes": "The Hollow Wakes: he lifts his lantern high, and every brazier gutters",
    "charge_bar": "The Headless Horseman: the Headless Charge",
    "last": "The Last Ride: the dark closes in faster",
    "last_bar": "The Headless Horseman: the Last Ride",
    "balks": "His horse balks at the bridge",
    "reset": "The Headless Horseman rides back to the bridge's foot; the braziers burn again, and his lantern goes dark",
    "defeated": "His lantern rolls from the saddle and goes out; his horse rears and fades into the mist",
    "loot": "The Headless Horseman is laid to rest: his spoils are yours",
}


def items():
    """Every item the Horseman's lair fight adds (the Hollowflail and the Hessian Sabre are Arms VII variants, listed
    there; the Horseman's Lantern is the summoned Horseman's, agriculture's)."""
    return list(ITEMS) + list(COSTUMES)
