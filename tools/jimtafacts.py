"""Reproducible runtime copies of owner-supplied Jimtafacts assets; no source code execution."""
from pathlib import Path
import json
import shutil
import hashlib

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/owner-library/originals/Oct23/Jimtafacts'
RES = ROOT / 'src/main/resources'

# Native implementations only. Unimplemented collection entries stay in the intake catalog.
ACCESSORIES = {
    'crystal_heart': ('legs/charm', 'Adds five hearts while worn.', {'max_health': 10}),
    'power_glove': ('offhand/glove', 'Adds 3 melee attack damage.', {'attack_damage': 3}),
    'feral_claws': ('offhand/glove', 'Adds 1 attack speed.', {'attack_speed': 1}),
    'pocket_piston': ('offhand/glove', 'Adds 1 attack knockback.', {'attack_knockback': 1}),
    'digging_claws': ('offhand/glove', 'Doubles block-breaking speed; your tool still determines harvest eligibility.', {'block_break_speed': 1}),
    'charm_of_shrinking': ('legs/charm', 'Shrinks the wearer to half size.', {'scale': -.5}),
    'bunny_hoppers': ('feet/boots', 'Higher jumps, five extra safe-fall blocks and half fall damage.', {'jump_strength': .2, 'safe_fall_distance': 5, 'fall_damage_multiplier': -.5}),
    'steadfast_spikes': ('feet/boots', 'Strong knockback resistance while worn.', {'knockback_resistance': .8}),
    'flippers': ('feet/boots', 'Removes the underwater movement penalty.', {'water_movement_efficiency': 1}),
    'snorkel': ('head/hat', 'Breathe underwater while worn.', {}),
    'night_vision_goggles': ('head/hat', 'Night vision while worn; fades within twelve seconds after removal.', {}),
    'scarf_of_invisibility': ('chest/necklace', 'Invisibility while worn; armor remains visible.', {}),
    'panic_necklace': ('chest/necklace', 'Taking damage grants Speed II for five seconds. Ten-second cooldown.', {}),
    'cross_necklace': ('chest/necklace', 'Extends the brief invulnerability after taking a hit.', {}),
    'obsidian_skull': ('legs/charm', 'After fire hurts you, grants ten seconds of fire resistance. Thirty-second cooldown.', {}),
    'antidote_vessel': ('legs/charm', 'Clears poison and wither once every ten seconds.', {}),
    'universal_attractor': ('legs/charm', 'Draws nearby loose items toward you. Respects claims and pickup ownership.', {}),
    'fire_gauntlet': ('offhand/glove', 'Melee hits ignite permitted targets for four seconds.', {}),
    'vampiric_glove': ('offhand/glove', 'Melee hits heal 20% of damage, at most one heart per second.', {}),
    'withered_bracelet': ('hand/bracelet', 'Melee hits inflict Wither for three seconds. Five-second cooldown.', {}),
    'thorn_pendant': ('chest/necklace', 'Returns 2 damage to permitted melee attackers. Three-second cooldown.', {}),
    'flame_pendant': ('chest/necklace', 'Ignites permitted melee attackers for four seconds. Five-second cooldown.', {}),
    'shock_pendant': ('chest/necklace', 'Resists lightning and shocks permitted melee attackers for 3 damage. Five-second cooldown.', {}),
    'rooted_boots': ('feet/boots', 'Standing still on grass restores one food every ten seconds.', {}),
    'running_shoes': ('feet/boots', 'Twenty-five percent faster sprinting and one-block step height.', {}),
    'charm_of_sinking': ('legs/charm', 'Sink toward the bottom underwater; adds oxygen reserve.', {'oxygen_bonus': 2}),
}
FOOD = {'everlasting_beef': (3, .3), 'eternal_steak': (8, .8)}

def items():
    return ['jimta_' + name for name in sorted(set(ACCESSORIES) | set(FOOD))]

def slot_links(slot):
    return {'#jugcraft:jimtafacts/' + slot} if any(row[0] == slot for row in ACCESSORIES.values()) else set()

def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

def generate():
    lang_source = json.loads((SOURCE / 'assets/artifacts/lang/en_us.json').read_text(encoding='utf-8'))
    lang = {}
    copies = []
    catalog = {}
    for name in sorted(set(ACCESSORIES) | set(FOOD)):
        ident = 'jimta_' + name
        src = SOURCE / f'assets/artifacts/textures/item/{name}.png'
        dest = RES / f'assets/jugcraft/textures/item/jimtafacts/{name}.png'
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(src, dest)
        if src.with_suffix('.png.mcmeta').exists():
            shutil.copyfile(src.with_suffix('.png.mcmeta'), dest.with_suffix('.png.mcmeta'))
        copies.append({'source': src.relative_to(ROOT).as_posix(), 'runtime': dest.relative_to(ROOT).as_posix(),
                       'sha256': hashlib.sha256(src.read_bytes()).hexdigest(), 'changes': 'None; byte-identical texture.'})
        dump(RES / f'assets/jugcraft/models/item/{ident}.json', {'parent':'minecraft:item/generated', 'textures':{'layer0':f'jugcraft:item/jimtafacts/{name}'}})
        dump(RES / f'assets/jugcraft/items/{ident}.json', {'model':{'type':'minecraft:model','model':f'jugcraft:item/{ident}'}})
        lang[f'item.jugcraft.{ident}'] = lang_source[f'item.artifacts.{name}']
        if name in ACCESSORIES:
            slot, description, attributes = ACCESSORIES[name]
            catalog[name] = {'slot': slot, 'description': description, 'attributes': attributes}
        else:
            nutrition, saturation = FOOD[name]
            catalog[name] = {'slot': '', 'description': 'Reusable food. One-minute cooldown after eating.', 'nutrition': nutrition, 'saturation': saturation, 'attributes': {}}
        lang[f'tooltip.jugcraft.{ident}'] = catalog[name]['description']
    # Resource packs permit additive files for entity slots/tags. Use a nested tag to avoid replacing existing tags.
    slots = sorted({x[0] for x in ACCESSORIES.values()})
    dump(RES / 'data/trinkets/entities/jugcraft_jimtafacts.json', {'entities':['player'], 'slots':slots})
    for slot in slots:
        values = ['jugcraft:jimta_' + name for name, row in ACCESSORIES.items() if row[0] == slot]
        dump(RES / f'data/jugcraft/tags/item/jimtafacts/{slot}.json', {'replace': False, 'values': values})
        path = RES / f'data/trinkets/tags/item/{slot}.json'
        old = json.loads(path.read_text()) if path.exists() else {'replace':False, 'values':[]}
        entry = '#jugcraft:jimtafacts/' + slot
        if entry not in old['values']: old['values'].append(entry)
        dump(path, old)
    dump(RES / 'jugcraft/jimtafacts/accessories.json', catalog)
    # Native language files cannot be split. This import is repeated by the canonical generator.
    lang_path = RES / 'assets/jugcraft/lang/en_us.json'
    existing = json.loads(lang_path.read_text(encoding='utf-8')); existing.update(lang); dump(lang_path, existing)
    for kind, time in [('smelting',200),('smoking',100),('campfire_cooking',600)]:
        dump(RES / f'data/jugcraft/recipe/jimta_eternal_steak_{kind}.json', {
            'fabric:load_conditions':[{'condition':'jugcraft:feature_enabled','feature':'concordance'}],
            'type':'minecraft:'+kind, 'category':'food', 'ingredient':'jugcraft:jimta_everlasting_beef',
            'result': {'id':'jugcraft:jimta_eternal_steak'}, 'experience':0, 'cookingtime':time})
    dump(ROOT / 'art/owner-library/catalog/edited-2026-10-23-jimtafacts/runtime-copies.json', copies)
    return catalog

if __name__ == '__main__':
    print(f'Generated {len(generate())} native accessory/food resources from supplied textures.')
