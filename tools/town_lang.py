"""The walled town's English text (tools/town_assets.py writes it into the lang file): names, screens, messages, and
what townsfolk say in each theme. Every line is original."""

THEME_NAMES = {
    "spring": "Spring", "summer": "Summer", "autumn": "Autumn", "winter": "Winter",
    "halloween": "Halloween", "harvest": "the Harvest Feast", "december": "December",
}

# What each kind of townsperson says, three lines per theme (the line shown changes with time and person).
LINES = {
    "townsfolk": {
        "spring": ["The tulips came up early this year. Lovely, aren't they?", "Fresh air and new leaves. The whole town smells of blossom.",
                   "Mind the puddles by the fountain, the spring rains have been generous."],
        "summer": ["Warm days and long evenings. Best time to sit by the fountain.", "The market's busy in summer. Have you seen the flower stall?",
                   "Keep to the shade at midday, friend."],
        "autumn": ["The leaves are turning gold outside the walls. Worth a walk.", "Pumpkins in every window box! Autumn's my favourite.",
                   "Bring your harvest to the General Store; they pay fair Jugs."],
        "winter": ["Cold enough to freeze the fountain! Well, nearly.", "Wrap up warm. The wind comes straight down the main street.",
                   "The snowman in the square has better posture than me."],
        "halloween": ["Did you see the soul lanterns? Gives me the shivers.", "I'm going as a pumpkin this year. Again.",
                      "The pile of jack o'lanterns in the square glows all night!"],
        "harvest": ["The Harvest Feast! The table in the square is groaning with food.", "Give thanks and share the pie, that's what my gran said.",
                    "Hay bales everywhere. Mind you don't sneeze."],
        "december": ["Have you seen the great tree in the square? The baubles glow!", "Red and green banners on every house. Merry December!",
                     "I've wrapped my presents in wool. Is that normal?"],
    },
    "guard": {
        "spring": ["All quiet at the gate. Just bees.", "Keep your blade sheathed in town, traveller.", "Nothing gets past these walls."],
        "summer": ["Long watch in the sun. Still, nothing gets past me.", "Monsters don't like the lamps. Neither do I, in this heat.",
                   "Move along, the square's that way."],
        "autumn": ["Dark comes early now. I'll be watching.", "Seen a creeper skulking by the east gate. Not anymore.",
                   "The town's safe. You have my word."],
        "winter": ["Frozen solid on the wall walk. Still standing.", "Snow hides footprints. Not from me.", "Stay inside the walls after dark."],
        "halloween": ["Skeletons in costume are still skeletons. I check.", "Boo yourself.", "The decorations are fake. The zombies aren't."],
        "harvest": ["Even guards get a slice of pie at the Feast.", "Guarding the pie table is a serious duty.", "All quiet, and well fed."],
        "december": ["A guard's December: lanterns, snow and no surprises.", "Somebody keeps putting tinsel on my helmet.",
                     "The tree in the square is under my protection."],
    },
    "decorator": {
        "spring": ["Out with the old, in with the tulips!", "Pink and white for the stalls this spring. What do you think?",
                   "Every window box gets fresh flowers. Every single one."],
        "summer": ["Yellow banners for summer. Sunny, like me.", "The flower bed in the square was my idea.", "Lanterns stay, everything else goes bright."],
        "autumn": ["Pumpkins, hay and orange banners. Perfect.", "I've carried more pumpkins this week than a farmer.",
                   "Autumn's easy: if it's orange, it goes up."],
        "winter": ["Snow blocks for the planters, spruce for the windows.", "The snowman's coming along nicely.", "White and pale blue. Very wintry."],
        "halloween": ["Soul lanterns on every post. Spooky!", "Black and orange stripes for the stalls. Mwahaha.", "Cobwebs? Those are deliberate."],
        "harvest": ["The feast table's set, hay's stacked, pumpkins polished.", "Brown and gold for the Harvest Feast.",
                    "Carrying a whole cake across the square is harder than it looks."],
        "december": ["That tree took me all morning. Worth it.", "Red and white awnings, like peppermint.", "Presents under the tree are just wool. Shh."],
    },
    "priest": {
        "spring": ["Welcome to the Church of Notch, the Maker of our block-built world.", "The bells ring for every season.", "Peace be with you, traveller."],
        "summer": ["We honour Notch by creating, exploring and sharing what we build.", "Rest awhile in the cool of the nave.", "Peace be with you."],
        "autumn": ["We thank Notch for the earth and this harvest.", "The days grow short; the candles burn longer.", "Peace be with you."],
        "winter": ["The Church of Notch keeps its doors open even in winter.", "Warm yourself by the candles.", "Peace be with you."],
        "halloween": ["Fear not the lanterns. They are only pumpkins.", "All travellers are welcome in the Church of Notch.", "Peace be with you, and with your costume."],
        "harvest": ["Share what you have; that is the feast.", "Together we give thanks to Notch, the Maker.", "Peace be with you."],
        "december": ["The longest nights bring the brightest lights.", "May the blessing of Notch follow all who pass these gates.", "Peace be with you."],
    },
    "mayor": {
        "spring": ["Welcome to our town! Mind the flower boxes.", "The town council approves of spring.", "Our walls keep the town as it ever was."],
        "summer": ["A fine summer for trade. Visit the market!", "The Jug Teller in the bank keeps your Jugs safe.",
                   "Our shops buy fair and sell fair. I insist."],
        "autumn": ["Harvest time! The General Store is buying.", "The council has approved more pumpkins.", "Enjoy our town, and spend your Jugs."],
        "winter": ["The town stands firm, winter or no.", "The snowman has been elected to the council.", "Stay warm, visitor."],
        "halloween": ["The council regrets nothing about the decorations.", "Trick or treat? The mayor has neither.", "Happy Halloween!"],
        "harvest": ["Welcome to the Harvest Feast!", "Eat, drink and buy something at the stalls.", "A bountiful year for the town."],
        "december": ["Season's greetings from the town council!", "The great tree was approved unanimously.", "Merry December, visitor!"],
    },
}

# What residents say about their day (town/TownRoutine.java TALK names which exist): three lines for each job and part of
# the day. Everyone shares the dawn greetings; the watch and the innkeepers, awake all night, also have night lines.
ROUTINE_LINES = {
    "townsfolk": {
        "morning": ["Morning! The bakery's chimney is already smoking. Best sign of a good day.",
                    "Up with the sun. Well, a bit after the sun. The sun is very keen.",
                    "Off to work. If you see a porter with a cabbage crate, give them room."],
    },
    "gardener": {
        "work": ["Every planter in this town gets watered by noon. Every. Single. One.",
                 "Mind the tulips. They've had a difficult week.",
                 "Weeds are just flowers nobody asked for. I ask them to leave."],
        "evening": ["Soil under my nails and a seat by the fountain. That's a good day.",
                    "I talk to the plants all day, so forgive me if I talk to you like a fern.",
                    "Tomorrow the hedges by the square get a trim. They know what they did."],
    },
    "porter": {
        "work": ["Cabbages to that stall, carrots to this one. Don't ask why. Nobody knows why.",
                 "Lift with your legs, friend. My back learned that the hard way.",
                 "These crates don't stack themselves. I've watched. For hours."],
        "evening": ["My arms are done for the day. My mouth's still working, though.",
                    "I carried forty crates today. Ask me how many I dropped. Go on. None!",
                    "Evenings are for sitting. Carrying is for tomorrow."],
    },
    "sweeper": {
        "work": ["A clean street is a happy street. A dusty street is my fault.",
                 "You'd be amazed what people drop. Yesterday: a whole cake. Still warm.",
                 "Sweep left, sweep right, wave at the guards. That's the job."],
        "evening": ["Broom's in the shed, feet are up. Mostly.",
                    "The main street's spotless. Give it till morning.",
                    "I can hear the street calling. It's saying 'leaves'. I'm ignoring it."],
    },
    "lamplighter": {
        "work": ["By day I polish the lanterns. By night I make sure they're doing their job.",
                 "A smudged lantern is a sad lantern. Not on my street.",
                 "I've named every lamp post in this town. That one's Gerald."],
        "evening": ["Dusk! My favourite time. Every lamp round the square, checked twice.",
                    "Monsters hate a well-lit town. So do the moths, oddly.",
                    "Lantern by lantern, the square comes alive."],
    },
    "busker": {
        "work": ["Requests? I know three songs. Two of them are the same song.",
                 "A Jug in the hat is lovely. A clap is nearly as good.",
                 "The fountain has excellent acoustics. Or the pigeons are being polite."],
        "evening": ["The tavern crowd sings louder than I play. I don't mind.",
                    "One more tune, then another, then probably another.",
                    "Clap along! Or don't. The fiddle carries on regardless."],
    },
    "baker": {
        "work": ["Up before the roosters, kneading since dawn. The bread doesn't make itself.",
                 "Smell that? That's the smell of a town that won't go hungry.",
                 "Fresh loaves for the taverns, the stalls and the guards. The guards eat the most."],
        "evening": ["Flour everywhere. In my hair, in my boots. I'll find some in my bed, I expect.",
                    "Tomorrow's dough is resting. So am I.",
                    "A good loaf needs patience. So does a good evening."],
    },
    "mayor": {
        "work": ["The town hall's door is always open. Well, during working hours.",
                 "Today's agenda: the fountain, the fountain's budget, and why the fountain needs a budget.",
                 "Our shops open at seven and shut at six. I signed the order myself."],
        "evening": ["An evening by the fountain is the best council meeting there is.",
                    "Citizens! The town is thriving! Also, someone left a cake on the main street.",
                    "I've shaken every hand in the square tonight. Some of them twice."],
    },
    "priest": {
        "work": ["The Church of Notch is open to all who build, explore and share.",
                 "Notch made the world one block at a time. We can be patient with ours.",
                 "Sit awhile. The nave is quiet, and the candles don't mind company."],
        "evening": ["Evening prayers at sunset. All are welcome, even creepers. Especially reformed ones.",
                    "May your torches never run out and your pickaxes never break.",
                    "The bells ring the day out. Rest well, traveller."],
    },
    "shopkeeper": {
        "work": ["Come in, come in! Everything's priced fair, in honest Jugs.",
                 "We're open seven till six. After that I'm someone else entirely.",
                 "Browse all you like. I'll pretend I'm not watching."],
        "evening": ["Shop's shut! I'm a private citizen until seven tomorrow.",
                    "Don't talk to me about prices. It's my evening off.",
                    "If it's urgent, the taverns sell the essentials all night."],
    },
    "florist": {
        "work": ["Every flower on this stall was grown with love. And a bit of bone meal.",
                 "Tulips for a friend, poppies for a hero, cornflowers for no reason at all.",
                 "Smell the alliums. No, really. Do it. Lovely, aren't they?"],
        "evening": ["The stall's shut, but the flowers never stop being lovely.",
                    "I've watered the pots, swept the petals and counted the bees.",
                    "Come by in the morning. The tulips will be awake by then."],
    },
    "vendor": {
        "work": ["Fresh from the farms this morning! Well, yesterday afternoon.",
                 "Cabbages! Carrots! Conversation! Two of those are for sale.",
                 "A good stall is a busy stall. A busy stall is a tired vendor."],
        "evening": ["Stall's packed away, crates are stacked. Now for a sit-down.",
                    "I talked to two hundred customers today. Now I'm talking to you. For free.",
                    "Tomorrow I'm selling onions. I can feel it."],
    },
    "decorator": {
        "work": ["Every lantern straight, every banner pressed. It's a calling.",
                 "I measure the bunting twice and hang it once.",
                 "If you move a flower pot, I'll know. I always know."],
        "evening": ["The square looks lovely tonight. You're welcome.",
                    "I've redone the planters three times today. They're finally right.",
                    "Tomorrow: ribbons. Possibly too many ribbons."],
    },
    "innkeeper": {
        "work": ["Welcome to the tavern! Our door's open every hour of the day.",
                 "Torches, bread, a bed to sleep in. Whatever the hour, we've got it.",
                 "The shops shut at six. We don't."],
        "evening": ["Busy night! Find a spot by the door, the singing's about to start.",
                    "Another round for the porters, and a quiet corner for the baker.",
                    "Evening, friend. Essentials are on the counter; gossip is free."],
        "night": ["Mm? Oh! Still open. Always open. What can I get you?",
                  "Shh, the town's asleep. Torches? Bread? A bed?",
                  "Night shift at the tavern. Just me, the fire and you."],
    },
    "watch": {
        "work": ["Gate's quiet. I like it quiet.",
                 "Shops shut at six. After that it's us and the innkeepers awake.",
                 "Keep your blade sheathed in the square, friend."],
        "evening": ["Lamps are lit and the gates are watched. Enjoy your evening.",
                    "Hear that? Tavern singing. Means all's well.",
                    "I'll be here all night. Somebody has to be."],
        "night": ["Quiet night. The whole town's abed but us and the innkeepers.",
                  "Keep your voice down, citizen. Folk are sleeping.",
                  "Need supplies at this hour? The taverns are open. Try not to wake anyone."],
    },
}

SHOP_NAMES = {"general": "General Store", "seasonal": "Seasonal Stall", "curios": "Curiosities", "florist": "Florist"}


def lang():
    out = {
        "block.jugcraft.atm": "Jug Teller",
        "entity.jugcraft.townsfolk": "Townsperson",
        "message.jugcraft.town.protected": "The town is protected: you can't change it here",
        "message.jugcraft.town.decorating": "The townsfolk are decorating for %s",
        "message.jugcraft.town.welcome": "Welcome to the town! Here are %s Jugs to start you off. Spend them at the market.",
        "message.jugcraft.townsfolk.says": "%s: %s",
        "message.jugcraft.townsfolk.banker": "You hold %s Jugs. Use a Jug Teller to send Jugs to another player.",
        "message.jugcraft.shop.out_of_season": "That isn't on sale this time of year",
        "message.jugcraft.shop.hours": "%s: open %s to %s",
        "message.jugcraft.shop.closed": "Sorry, the %s is closed. We open at %s and shut at %s. The taverns sell essentials all night.",
        "message.jugcraft.shop.closed_now": "The shop has closed for the day. It opens again at %s.",
        "message.jugcraft.townsfolk.asleep": "%s is fast asleep. Come back in the morning; the taverns stay open all night.",
        "screen.jugcraft.shop.essentials": "Tavern Essentials",
        "message.jugcraft.shop.too_few_jugs": "That costs %s Jugs",
        "message.jugcraft.shop.too_few_items": "You need %s %s to sell",
        "message.jugcraft.shop.purse_full": "Your Jugs can't hold any more",
        "message.jugcraft.shop.bought": "Bought %s %s for %s Jugs",
        "message.jugcraft.shop.sold": "Sold %s %s for %s Jugs",
        "message.jugcraft.atm.choose": "Choose who to send to and how many Jugs",
        "message.jugcraft.atm.offline": "%s is not online",
        "message.jugcraft.atm.refused": "The Jug Teller can't send that",
        "message.jugcraft.atm.sent": "Sent %s Jugs to %s",
        "message.jugcraft.atm.received": "You received %s Jugs from %s",
        "screen.jugcraft.jugs": "Jugs: %s",
        "screen.jugcraft.price": "%s Jugs",
        "screen.jugcraft.shop.buy": "Buy",
        "screen.jugcraft.shop.sell": "Sell",
        "screen.jugcraft.shop.buy_one": "Buy",
        "screen.jugcraft.shop.sell_one": "Sell",
        "screen.jugcraft.shop.empty": "Nothing here today. Come back another season!",
        "screen.jugcraft.atm.to": "Send to:",
        "screen.jugcraft.atm.nobody": "No one else is online",
        "screen.jugcraft.atm.recipient": "To: %s",
        "screen.jugcraft.atm.amount": "Amount: %s",
        "screen.jugcraft.atm.clear": "Clear",
        "screen.jugcraft.atm.all": "All",
        "screen.jugcraft.atm.send": "Send",
        "command.jugcraft.town.none": "There is no town in this world",
        "command.jugcraft.town.info": "The town's middle is at %s %s %s; it is decorated for %s",
        "command.jugcraft.town.overworld": "The town can only be placed in the Overworld",
        "command.jugcraft.town.exists": "This world already has a town",
        "command.jugcraft.town.placed": "The town will be built round you as its chunks load",
        "command.jugcraft.town.no_theme": "No theme called %s",
        "command.jugcraft.town.theme": "The town is now decorated for %s",
        "command.jugcraft.jugs.balance": "You hold %s Jugs",
        "command.jugcraft.jugs.too_few": "They don't have that many Jugs",
        "command.jugcraft.jugs.changed": "%s now holds %s Jugs",
    }
    for theme, name in THEME_NAMES.items():
        out[f"theme.jugcraft.{theme}"] = name
    for shop, name in SHOP_NAMES.items():
        out[f"shop.jugcraft.{shop}"] = name
    for role, themes in LINES.items():
        for theme, lines in themes.items():
            for i, line in enumerate(lines):
                out[f"message.jugcraft.townsfolk.{role}.{theme}.{i}"] = line
    for job, parts in ROUTINE_LINES.items():
        for part, lines in parts.items():
            if len(lines) != 3:
                raise ValueError(f"routine lines {job}.{part} need three lines")
            for i, line in enumerate(lines):
                out[f"message.jugcraft.townsfolk.routine.{job}.{part}.{i}"] = line
    out.update({
        "filled_map.jugcraft.town": "Town Map - 1 Church / 2 Bank / 3 Hall / 4 Store",
        "message.jugcraft.town.map_hint": "The Church of Notch is north of the square; the bank is west and the town hall east. Look for the bright shop signs.",
        "message.jugcraft.town.map_given": "Here is your town map. North is up: 1 Church, 2 Bank, 3 Town Hall, 4 General Store. The market surrounds the square.",
        "message.jugcraft.town.have_map": "You already carry our town map. North is up: 1 Church, 2 Bank, 3 Hall, 4 Store.",
        "message.jugcraft.town.map_full": "Make one empty inventory slot and ask me again for a free map.",
    })
    return out
