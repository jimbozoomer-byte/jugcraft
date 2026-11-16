"""Prepare (never install) a 26.3 existing-world climate migration.

Reads downloaded/stopped-world metadata and writes a separate review bundle.
Only Overworld generator.biome_source changes. Region files are never opened.
Install only with the matching Jugcraft climate build, a fresh backup, and the
server stopped; verify the original hash again immediately before replacement.
"""
import argparse
import copy
import hashlib
import json
from pathlib import Path

from nouveau_nbt import read, write


def compound(parent, key):
    tag, value = parent[key]
    if tag != 10:
        raise ValueError(f"{key} must be an NBT compound")
    return value


def migrate(raw, town_raw):
    root = read(raw)
    if root[0] != 10 or root[2].get('DataVersion') != (3, 5023):
        raise ValueError('Expected Minecraft 26.3 world_gen_settings.dat (DataVersion 5023)')
    town = compound(read(town_raw)[2], 'data')
    kind, origin = town['origin']
    if kind != 11 or len(origin) != 3 or any(abs(n) > 29_990_000 for n in (origin[0], origin[2])):
        raise ValueError('Expected a valid saved town origin, not a guessed anchor')
    x, _, z = origin
    result = copy.deepcopy(root)
    settings = compound(result[2], 'data')
    dimensions = compound(settings, 'dimensions')
    overworld = compound(dimensions, 'minecraft:overworld')
    generator = compound(overworld, 'generator')
    if overworld.get('type') != (8, 'minecraft:overworld') or generator.get('type') != (8, 'minecraft:noise'):
        raise ValueError('Only the existing noise Overworld can be migrated')
    source = compound(generator, 'biome_source')
    profile = {'schema': (3, 1), 'anchor': (10, {'mode': (8, 'fixed'), 'x': (3, x), 'z': (3, z)}),
               'north_frigid': (3, 2500), 'south_hot_start': (3, 2000), 'south_hot_end': (3, 4000),
               'south_frigid': (3, 5500), 'transition': (3, 256)}
    if source.get('type') == (8, 'jugcraft:climate'):
        if source.get('climate') != (10, profile):
            raise ValueError('Existing climate profile differs; refusing to move its fixed anchor or rules')
        base = compound(source, 'base')
        if base.get('type') != (8, 'minecraft:multi_noise') or base.get('preset') != (8, 'minecraft:overworld'):
            raise ValueError('Existing climate wrapper has an unexpected base source')
        return raw, {'status': 'already_migrated', 'anchor': {'x': x, 'z': z}}
    if source.get('type') != (8, 'minecraft:multi_noise') or source.get('preset') != (8, 'minecraft:overworld'):
        raise ValueError('Unexpected biome source; refusing to replace an authored/custom source')
    generator['biome_source'] = (10, {'type': (8, 'jugcraft:climate'), 'base': (10, source), 'climate': (10, profile)})
    encoded = write(result)
    # Prove the entire decoded tree, including seed, settings and other dimensions,
    # is unchanged after removing exactly the wrapper we just added.
    unwrapped = copy.deepcopy(read(encoded))
    restored = compound(compound(compound(compound(unwrapped[2], 'data'), 'dimensions'), 'minecraft:overworld'), 'generator')
    restored['biome_source'] = restored['biome_source'][1]['base']
    if unwrapped != root:
        raise AssertionError('Migration changed data outside the biome-source wrapper')
    return encoded, {'status': 'prepared', 'anchor': {'x': x, 'z': z},
                     'boundaries_z': {'north_frigid': z-2500, 'hot_start': z+2000,
                                      'temperate_start': z+4000, 'south_frigid': z+5500}}


def sha(raw):
    return hashlib.sha256(raw).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--world-gen', required=True, type=Path)
    parser.add_argument('--town', required=True, type=Path)
    parser.add_argument('--out', required=True, type=Path, help='New, separate output directory; never the world directory')
    args = parser.parse_args()
    source, town = args.world_gen.resolve(), args.town.resolve()
    out = args.out.resolve()
    if out == source.parent or out in source.parents or out == town.parent:
        parser.error('Output must be a separate preparation directory')
    raw, town_raw = source.read_bytes(), town.read_bytes()
    migrated, report = migrate(raw, town_raw)
    # Refuse to overwrite earlier evidence or inputs.
    out.mkdir(parents=True, exist_ok=False)
    (out / 'world_gen_settings.dat').write_bytes(migrated)
    (out / 'world_gen_settings.original.dat').write_bytes(raw)
    report.update({'source_sha256': sha(raw), 'prepared_sha256': sha(migrated), 'town_sha256': sha(town_raw),
                   'target': 'world/data/minecraft/world_gen_settings.dat',
                   'installed': False, 'requires': 'Matching climate-capable Jugcraft JAR; stopped server; fresh backup; source hash recheck',
                   'preserves': ['all existing chunk files', 'seed', 'noise settings', 'Nether', 'End', 'other dimensions', 'spawn', 'town', 'players']})
    (out / 'migration.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    main()
