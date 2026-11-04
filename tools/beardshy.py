"""Beardshy's name and lines (Beardshy.java speaks them; generate_material_data.py writes them to the language file).

Beardshy is the shy, apologetic side of Beardson, the Retro Game Shop's owner (the owner's Jimboverse character): soft,
hesitant, quick to apologise, sure we are standing on stolen land, fond of internet sayings he half understands, and
given to explaining simple things a little too kindly. The counts must match TALK_LINES, STARTLED_LINES and HURT_LINES
in Beardshy.java (scripts/tests/test_beardshy.py checks them).
"""

ENTITY = "beardshy"
DISPLAY = "Beardshy"

# Said to a player who crouched to come close and talk.
TALK = [
    "Did you know we're, um... standing on stolen land? Like, right now. This block, even. Sorry.",
    "Before you mine that, should we, um, acknowledge whose land it is? Sorry, I know, I'm sorry.",
    "The big mountain is called Denali. If anyone calls it McKinley I'll... I'll be fine. I'm fine. Sorry.",
    "So, um, a villager is a person who lives in a village. That's why they're called villagers. Does that make sense?",
    "Emeralds are a kind of gem, and gems are shiny rocks, and you can trade them with villagers, who are... people. Sorry, you knew that.",
    "Hold my non-alcoholic beer.",
    "That escalated quickly... sorry. Was that me? I think that was me.",
    "Ah yes, the floor is made of floor. Sorry, that was mean. I'm so sorry.",
    "Bold of you to assume I have plans this weekend. Sorry.",
    "This guy mines. Sorry, is that how you say it? I think that's how you say it.",
    "Your base is so slay. Is slay a good word? I really think it's a good word.",
    "That pickaxe is lowkey giving... pickaxe. Sorry, I don't know what it's giving.",
    "It's free real estate! Well, it isn't free. It's stolen. Sorry.",
    "Shut up and take my emeralds. Sorry, I didn't mean shut up. Please keep talking.",
    "I for one welcome our new iron golem overlords. Sorry, golems.",
    "I'm blushing, aren't I. I know. It's a whole thing. Sorry.",
    "Sorry this sentence is getting so long, I just think it's important that, um... sorry. Sorry for the sorry.",
    "Please don't be mean to me... I'm just a shy little guy.",
]

# Said to a player who walked straight up to him (he startles and hurries off).
STARTLED = [
    "O-oh! S-sorry, I didn't see you there... sorry.",
    "Sorry! Sorry. I'll... I'll go stand over there.",
    "Eep. Sorry. I'm just a shy little guy...",
    "C-could you maybe crouch? Sorry. You're just very... tall.",
]

# Said to whoever hit him.
HURT = [
    "S-sorry! Was I in the way? I'm so sorry!",
    "Please don't be mean to me...",
    "Ow... no, that's fair, that's fair. Sorry.",
]


def lang(lang):
    """Adds his name and lines to {@code lang}."""
    lang[f"entity.jugcraft.{ENTITY}"] = DISPLAY
    lang[f"item.jugcraft.{ENTITY}_spawn_egg"] = DISPLAY + " Spawn Egg"
    for kind, lines in (("talk", TALK), ("startled", STARTLED), ("hurt", HURT)):
        for i, line in enumerate(lines):
            lang[f"message.jugcraft.{ENTITY}.{kind}.{i}"] = line


def egg_assets(write, assets):
    """His spawn egg's item definition and model (its icon is drawn by tools/beardshy_look.py)."""
    egg = f"{ENTITY}_spawn_egg"
    write(assets / "items" / f"{egg}.json", {"model": {"type": "minecraft:model", "model": f"jugcraft:item/{egg}"}})
    write(assets / "models" / "item" / f"{egg}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"jugcraft:item/{egg}"}})
