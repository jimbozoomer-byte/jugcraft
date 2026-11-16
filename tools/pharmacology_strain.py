"""Owner-approved decaying strain rules; presentation data, no real pharmacology instructions."""
LANG = {
    'strain.jugcraft.stable': 'Stable', 'strain.jugcraft.strained': 'Strained',
    'strain.jugcraft.overloaded': 'Overloaded', 'strain.jugcraft.severe': 'Severe', 'strain.jugcraft.critical': 'Critical',
    'hud.jugcraft.strain_pills': '%s · %s',
    'hud.jugcraft.strain_pills_next': '+%s → %s',
    'hud.jugcraft.strain': 'Strain %s · %s · Falling %s/min',
    'hud.jugcraft.strain_next': 'Next dose: +%s → %s · %s',
    'hud.jugcraft.strain_combination': 'Includes +%s combination strain',
    'tooltip.jugcraft.strain_forecast': 'Next dose: +%s strain → %s · %s',
    'tooltip.jugcraft.drug.timing': '%s s benefit · %s s onset · +%s strain (safe threshold %s)',
    'tooltip.jugcraft.drug.redosing': 'One formulation per family. Replacing pays full strain and restarts onset. Repeats last 75%, then 50%; duration never banks.',
    'tooltip.jugcraft.drug.no_redose': 'Replacing pays full new strain, restarts onset and retains family tolerance.',
    'message.jugcraft.drug.incompatible': 'Overlapping alcohol, opioids or Ketamine adds 25 combination strain per new dose.',
    'message.jugcraft.drug.strain_full': '100 is the safe threshold. Higher strain is allowed with increasing penalties.',
    'message.jugcraft.drug.wait': 'This formulation cannot be used in your current state.',
    'message.jugcraft.drug.status': '%s: %s %s:%s · Strain %s (safe %s)',
    'message.jugcraft.drug.ready': '%s: ready · Strain %s (safe %s)',
}


def write_all(lang):
    lang.update(LANG)


def handbook_pages():
    return [{'title': 'Strain and Redosing', 'icon': 'jugcraft:caffeine_drink', 'text': [
        '100 strain is a safe threshold, not a consumption cap. Every new dose immediately adds its full strain cost, including replacements. Strain falls by 6 points per online minute during onset or an active dose, and by 12 per minute when no doses remain active. Long benefits can outlast their strain.',
        'Above 100 through 125: Strained, activity food cost +15% and firearm spread +10%. Above 125 through 150: Overloaded, movement/mining -10%, food cost +25% and spread +20%.',
        'Above 150 through 200: Severe, movement -20%, mining -25%, attack speed -15%, food cost +40% and spread +35%. Above 200: Critical, movement -30%, mining -40%, attack speed -25%, food cost +60% and spread +50%. Matching drug and strain penalties use the stronger value rather than multiplying.',
        'A new formulation replaces its family, ends the old benefit and restarts its own onset. Earlier strain is not refunded. Repeated family doses last 75%, then 50%; remaining duration never accumulates. Recovery records do not block use. Active stimulant replacement still causes Jitters.',
        'A new dose overlapping another alcohol, opioid or Ketamine family costs 25 extra strain, once per new dose even when two such families overlap. Ended doses and replacing the same family alone do not create this surcharge.',
        'Ten horizontal pills above the hunger bar show strain. Each pill holds 10 points: ivory fills through 100, then amber/orange/red paints over that row through 200, followed by violet through 300. Decay removes the newest fill first. Above 100 the readout shows exact strain and severity, including values above 300. Air bubbles and mounted health keep their vanilla positions; the pill row moves above them when needed.',
        'While strain is above zero, holding a formulation adds a patterned preview and next-cost readout, including combination strain. At zero the empty row remains for three seconds, then hides, even while a drug is held or benefits remain active. A held drug at zero does not bring it back; the tooltip still gives the next-dose forecast and predicted severity. Visual Off and Reduced Motion do not change penalties.',
        'Milk, Charcoal, Naloxone and death do not erase strain or tolerance. Death ends benefits. Logout pauses drug, tolerance and strain clocks together. Existing saved reservations seed the new decaying pool once; they are never recharged on subsequent loads.'
    ]}]
