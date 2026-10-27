"""Herobrine (docs/features/herobrine.md): a still, staring figure with glowing white eyes who appears to players at a
distance, now and then right behind them, and is gone as soon as anyone comes close, stares back or hits him. He never
attacks, changes the world or speaks.

Java: creatures/herobrine/Herobrine (the entity, who saw him, why he went), HerobrineEncounters (the entity type, the live
tick, placing him, operators' commands) and HerobrineDirector (the seam a schedule implements to decide who is due an
encounter and when); client/HerobrineRenderer draws him. His body is the game's own default player texture, read from the
installed game at run time, so no game asset is copied into Jugcraft; this module draws only his eyes: a clear 64x64 skin
layer with the four eye pixels white, drawn full-bright over that face.
"""
from PIL import Image

MOD = "jugcraft"
ENTITY = "herobrine"
NAME = "Herobrine"
# Keep in step with Herobrine and HerobrineEncounters (tools/check_mod_data.py checks them).
VANISH_DISTANCE = 12.0
STARE_TICKS = 40
DISTANT_TICKS = 600
CLOSE_TICKS = 60
CLOSE_STARE_TICKS = 12
SEEN_TICKS = 10
GRACE_TICKS = 1200
IDLE_TICKS = 1800
CLOSE_DARKNESS_TICKS = 60
# The server option (config/jugcraft.properties) that switches the scheduled encounters off; commands work either way.
OPTIONS = {"herobrine.haunting": "on"}
# His eyes: the two pixels of each eye on the face (the head's front is u 8-15, v 8-15 in the player skin layout).
EYES = ((9, 12), (10, 12), (13, 12), (14, 12))

FAILURES = {
    "target_gone": "%s is not in the Overworld",
    "target_unsafe": "%s is not calm enough for a close encounter",
    "already_haunted": "Herobrine is already out for %s",
    "no_visible_spot": "Herobrine found nowhere %s could see him",
}


def write_all(lang):
    lang[f"entity.{MOD}.{ENTITY}"] = NAME
    lang[f"commands.{MOD}.herobrine.status.on"] = "Herobrine's haunting is on: %s encounters under way, %s players online"
    lang[f"commands.{MOD}.herobrine.status.off"] = "Herobrine's haunting is off (herobrine.haunting=off)"
    lang[f"commands.{MOD}.herobrine.status.no_schedule"] = \
        "No schedule is installed: Herobrine appears only by /jugcraft herobrine sighting or scare"
    lang[f"commands.{MOD}.herobrine.sighting"] = "Herobrine is watching %s from a distance"
    lang[f"commands.{MOD}.herobrine.scare"] = "Herobrine stands behind %s"
    for failure, text in FAILURES.items():
        lang[f"commands.{MOD}.herobrine.failed.{failure}"] = text


def eyes_texture():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for x, y in EYES:
        img.putpixel((x, y), (255, 255, 255, 255))
    return img


def draw_all(save):
    save(eyes_texture(), "entity", f"{ENTITY}_eyes")
