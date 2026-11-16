"""Shared native world-only drug filter and local display labels; no gameplay values."""


def write_all(write, assets, lang):
    lang.update({
        "screen.jugcraft.dream_study.card": "%s · %s",
        "screen.jugcraft.dream_study.input_card": "Input %s: %s",
        "screen.jugcraft.dream_study.clue_tooltip": "%s Source: %s > %s > %s. Trace: %s > %s > %s.",
        "hud.jugcraft.dream.mode_1": "Memory study", "hud.jugcraft.dream.mode_2": "Pattern study",
        "hud.jugcraft.dream.mode_3": "Deep study", "hud.jugcraft.dream.mode_4": "Resonance",
        "hud.jugcraft.dream.no_folio": "%s · Carry a Dream Folio",
        "hud.jugcraft.dream.no_site": "%s · No eligible recorded plinth within %s blocks",
        "hud.jugcraft.dream.target": "%s %s: %s blocks · %s · %s",
        "hud.jugcraft.dream.recorded_only": "Recorded, loaded sites only · 96-block range",
        "hud.jugcraft.dream.study": "%s visible recorded site(s) · Use plinth with Folio + Study Canvas",
        "hud.jugcraft.echo.body": "%s Body: %s blocks · %s · %s",
        "hud.jugcraft.echo.return": "Body vulnerable · Sneak to return · Range %s blocks",
        "hud.jugcraft.echo.at_body": "Echo View · At your body",
        "hud.jugcraft.echo.directly": "directly",
        "hud.jugcraft.echo.level": "same height",
        "hud.jugcraft.echo.above": "%s blocks above",
        "hud.jugcraft.echo.below": "%s blocks below",
        "hud.jugcraft.echo.ahead": "ahead", "hud.jugcraft.echo.ahead_right": "ahead-right",
        "hud.jugcraft.echo.right": "right", "hud.jugcraft.echo.behind_right": "behind-right",
        "hud.jugcraft.echo.behind": "behind", "hud.jugcraft.echo.behind_left": "behind-left",
        "hud.jugcraft.echo.left": "left", "hud.jugcraft.echo.ahead_left": "ahead-left",
        "screen.jugcraft.drug_visuals.title": "Drug visuals",
        "screen.jugcraft.drug_visuals.enabled": "Enable drug visuals",
        "screen.jugcraft.drug_visuals.enabled.tooltip": "Changes only how effects look. Drug benefits, strain, movement and firearm penalties still apply.",
        "screen.jugcraft.drug_visuals.intensity": "Overall intensity (%)",
        "screen.jugcraft.drug_visuals.intensity.tooltip": "0 turns drug visuals off. Minecraft's Distortion Effects setting also limits the overall strength.",
        "screen.jugcraft.drug_visuals.blur": "Edge blur (%)",
        "screen.jugcraft.drug_visuals.blur.tooltip": "Softens the edges of the world. The center and interface stay clear.",
        "screen.jugcraft.drug_visuals.motion": "Visual motion (%)",
        "screen.jugcraft.drug_visuals.motion.tooltip": "Peripheral sway, patterns, dream wisps, afterimages, warning breathing and Jitters. Reduced motion makes patterns static and removes wisps/afterimages. Mouse direction and aiming are unchanged.",
        "screen.jugcraft.drug_visuals.double_vision": "Double vision (%)",
        "screen.jugcraft.drug_visuals.double_vision.tooltip": "Faint doubled outlines and LSD/DMT directional afterimages at the edges. 0 removes both.",
    })
    write(assets / "post_effect/drug_alcohol.json", {
        "targets": {"jugcraft:drug_swap": {}},
        "passes": [
            {"vertex_shader": "minecraft:core/screenquad", "fragment_shader": "jugcraft:post/drug_alcohol",
             "inputs": [{"sampler_name": "In", "target": "minecraft:main", "bilinear": True}],
             "output": "jugcraft:drug_swap",
             "uniforms": {"JugcraftDrugVisuals": [
                 {"name": "Effect", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "Look", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "Clarity", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "Warning", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "Calm", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "Dream", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "TrailA", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
                 {"name": "TrailB", "type": "vec4", "value": [0.0, 0.0, 0.0, 0.0]},
             ]}},
            {"vertex_shader": "minecraft:core/screenquad", "fragment_shader": "minecraft:post/blit",
             "inputs": [{"sampler_name": "In", "target": "jugcraft:drug_swap"}],
             "output": "minecraft:main",
             "uniforms": {"BlitConfig": [{"name": "ColorModulate", "type": "vec4", "value": [1.0, 1.0, 1.0, 1.0]}]}},
        ],
    })


def handbook_pages():
    return [{'title': 'Drug Display Settings', 'icon': 'jugcraft:caffeine_drink', 'text': [
        'Open Jugcraft display settings through Mod Menu, or bind its action in Controls. Drug visuals has an On/Off switch and intensity, blur, sway and double-vision sliders. Minecraft Distortion Effects also limits everything. Reduced motion makes warning rims static and removes sway and visual Jitters. These choices never change drug benefits, strain, hunger or firearm rules.',
        'Caffeine has a subtle warm contrast lift. Methylphenidate has cool focus contrast; Modafinil has a restrained neutral clarity treatment. Amphetamine and Stimulant have a warmer peripheral tunnel, and Adrenaline has a stronger initial rush that settles. Drug Jitters move only peripheral image samples; Stimulant Fatigue gently desaturates the view.',
        'Above 100 strain, an amber peripheral warning grows through the strain bands toward a red critical rim. At higher strain, edge blur can accompany it. The center, crosshair and interface remain readable. The strain HUD still names the band when visual effects are off. Milk ends dose visuals but does not erase this strain warning.',
        'THC adds a gentle warm botanical tint and light edge softness. Morphine and Fentanyl have muted color and softened edges; Fentanyl is stronger, while its patch stays lighter. Naloxone ends opioid visuals together with the opioid benefit.',
        'Ketamine has a cooler, lightly separated peripheral image during the actual Echo View. Reduced motion removes its peripheral breathing. Its body-direction HUD remains readable with visual effects off: it shows the distance, direction and height back to your vulnerable body. Sneak or open a menu to return.',
        'Psilocybin adds gentle organic peripheral breathing and violet warmth. Its wisps mark only recorded, visible study plinths. Mescaline has a restrained crystalline rim and a slow resonance pulse; its persistent compass uses only the nearest recorded, loaded plinth within 96 blocks.',
        'Study and navigation text remain readable with drug visuals off. Carry a Dream Folio and physically record a plinth; Psilocybin then reveals its Memory lesson. Three numbered clue cards show the source and required input, highlight the current source step and describe the rule/trace in narrated tooltips. Mirror reverses the input order; Reverie advances each symbol through Sun, Wave, Leaf, Star, Sun. A successful lesson still pays one Study Canvas for one imprint.',
        'LSD adds a six-fold geometric lattice and up to three faint directional afterimages when you turn. DMT adds deeper eight-fold geometry and slow peripheral Clockwork arches, gears and schematic pointed figures. These glimpses preview the Clockwork theme; they do not open a portal or provide trading. Actual travel and elven trades remain a later content batch.',
        'Afterimages use the recent view direction to offset the currently visible world; they do not retain past world frames. They settle within six view ticks when turning stops, and reset on a scene change, menu or sudden jump. Reduced Motion, zero motion or zero double vision removes them while static patterns and real study information remain useful.',
        'Reduced Motion or zero motion removes moving dream wisps and peripheral breathing. Lower motion reduces wisp count; the shared magic particle budget also applies. No Folio or no eligible site is explained by the dream HUD. It never finds undiscovered plinths or loads chunks.',
        'Alcohol, focus, calm, dream, Jitters and strain combine in one bounded world filter. Opening an interface suspends the filter. Cosmetic effects never turn the camera, change input, flash or reveal hidden blocks.'
    ]}]
