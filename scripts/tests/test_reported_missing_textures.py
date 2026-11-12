"""November 12 batch: the owner's reported purple-and-black families must render their supplied art.

For every in-scope item or block this resolves the item definition (and blockstate), every model in
each parent chain and every texture the drawn model binds. Each texture must exist, be a well-formed
PNG with sane frame geometry, be stitched into the block atlas, be byte-identical to its catalogued
supplied original, and not be a known placeholder. Each model must reference the same textures as
the supplied original model it was converted from.

Anything without a supplied original must be listed in AWAITING_ORIGINAL with the exact file that
is needed; it never passes silently. Run with: python scripts/tests/test_reported_missing_textures.py
"""
import hashlib
import json
import struct
import subprocess
import unittest
import zlib
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets'
JUG = ASSETS / 'jugcraft'
ATLAS = ASSETS / 'minecraft/atlases/blocks.json'
LANG = JUG / 'lang/en_us.json'
JIMTAFACTS = ROOT / 'art/owner-library/originals/Oct23/Jimtafacts'
MANIFEST = ROOT / 'art/owner-library/catalog/edited-2026-10-23-jimtafacts/manifest.json'
# Runtime prefix -> (supplied namespace, supplied assets folder relative to the Jimtafacts collection).
SOURCES = {
    'jimtafacts/ae2/': ('ae2', 'applied energentics reborn by jimbo/assets/ae2'),
    'jimtafacts/spellbooks/': ('irons_spellbooks', 'assets/irons_spellbooks'),
}

COLOURS = ('white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown '
           'green red black').split()
_QUARTZ = ['quartz_block', 'quartz_stairs', 'quartz_slab', 'quartz_wall',
           'quartz_bricks', 'quartz_brick_stairs', 'quartz_brick_slab', 'quartz_brick_wall',
           'quartz_pillar', 'quartz_pillar_stairs', 'quartz_pillar_slab', 'quartz_pillar_wall']
for _style in ('chiseled_quartz', 'cut_quartz', 'smooth_quartz'):
    _QUARTZ += [_style + suffix for suffix in ('_block', '_stairs', '_slab', '_wall')]

# Reported family -> registry paths (all jugcraft:jimta_*). Display names come from the lang file.
SCOPE = {
    'Certus Quartz (crystals, budding "ore", blocks)':
        ['certus_quartz_crystal', 'charged_certus_quartz_crystal', 'certus_quartz_dust']
        + ['certus_quartz_' + tool for tool in
           ('axe', 'hoe', 'shovel', 'pickaxe', 'sword', 'wrench', 'cutting_knife')]
        + _QUARTZ
        + [grade + '_budding_quartz' for grade in ('damaged', 'chipped', 'flawed', 'flawless')]
        + [size + '_quartz_bud' for size in ('small', 'medium', 'large')]
        + ['quartz_cluster', 'quartz_glass', 'quartz_vibrant_glass', 'quartz_fixture'],
    'Inscriber presses': ['logic_processor_press', 'calculation_processor_press',
                          'engineering_processor_press', 'silicon_press', 'name_press', 'inscriber'],
    'Paintballs': [c + kind for c in COLOURS for kind in ('_paint_ball', '_lumen_paint_ball')],
    'Quantum entanglement': ['quantum_ring', 'quantum_link', 'quantum_entangled_singularity'],
    'Storage cells': [f'{kind}_{size}k' for kind in ('item_storage_cell', 'fluid_storage_cell',
                                                     'portable_item_cell', 'portable_fluid_cell',
                                                     'cell_component')
                      for size in (1, 4, 16, 64, 256)]
        + [f'spatial_storage_cell_{n}' for n in (2, 16, 128)]
        + [f'spatial_cell_component_{n}' for n in (2, 16, 128)]
        + ['creative_storage_cell', 'view_cell', 'item_cell_housing', 'fluid_cell_housing'],
    'Charged Staff': ['charged_staff'],
    'Color Applicator': ['color_applicator'],
    'Matter Cannon': ['matter_cannon', 'matter_ball'],
    'Printed silicon, cores and cards':
        ['printed_silicon', 'printed_logic_processor', 'printed_calculation_processor',
         'printed_engineering_processor', 'logic_processor', 'calculation_processor',
         'engineering_processor', 'formation_core', 'annihilation_core']
        + [card + '_card' for card in ('basic', 'advanced', 'speed', 'capacity', 'fuzzy', 'inverter',
                                       'redstone', 'crafting', 'equal_distribution', 'energy', 'void',
                                       'memory')],
    'Portal Frame': ['spellbooks_portal_frame', 'spellbooks_pocket_dimension_portal_frame'],
    'Blaze Instruction Manual and related spell books':
        ['spellbooks_' + book + '_spell_book' for book in
         ('blaze', 'copper', 'iron', 'gold', 'diamond', 'netherite', 'rotten', 'dragonskin', 'druidic',
          'villager', 'ice', 'cursed_doll', 'evoker', 'legendary', 'necronomicon', 'wimpy')],
}

# Known placeholders: never accepted as an item's own art unless the item is AWAITING_ORIGINAL.
PLACEHOLDER_TEXTURES = {
    # The supplied Iron's Spellbooks source uses this temporary book for books it never drew.
    'jugcraft:jimtafacts/spellbooks/item/temp_spellbook',
    'jugcraft:newest/jugtania/item/placeholder',
    'minecraft:missingno',
}

# Reported content with no supplied original anywhere in art/owner-library. Each entry names the
# exact original that would be needed. Remove an entry once that original is imported.
AWAITING_ORIGINAL = {
    'certus_quartz_ore': {
        'family': 'Certus Quartz (crystals, budding "ore", blocks)',
        'registered': False,
        'candidate_ids': ('certus_quartz_ore', 'deepslate_certus_quartz_ore', 'charged_certus_quartz_ore',
                          'quartz_ore', 'charged_quartz_ore'),
        'library_names': ('certus_quartz_ore.png', 'charged_certus_quartz_ore.png', 'quartz_ore.png',
                          'charged_quartz_ore.png', 'deepslate_certus_quartz_ore.png'),
        'needed': 'A Certus Quartz Ore block texture (plus a charged variant if wanted), e.g. the '
                  'AE2 1.7-1.12 era appliedenergistics2 textures/blocks/quartz_ore.png and '
                  'charged_quartz_ore.png. The supplied modern AE source has no ore: certus grows from '
                  'the four budding grades, which are in scope and render. No such block is registered.',
    },
    'spellbooks_legendary_spell_book': {
        'family': 'Blaze Instruction Manual and related spell books',
        'registered': True,
        'renders': 'jugcraft:jimtafacts/spellbooks/item/temp_spellbook',
        'library_names': ('legendary_spell_book.png',),
        'needed': 'assets/irons_spellbooks/textures/item/legendary_spell_book.png (16x16 icon); '
                  'textures/item/spell_book_models/legendary_spell_book.png (64x64) for a held 3D book. '
                  'The supplied model itself points at temp_spellbook.',
    },
    'spellbooks_wimpy_spell_book': {
        'family': 'Blaze Instruction Manual and related spell books',
        'registered': True,
        'renders': 'jugcraft:jimtafacts/spellbooks/item/temp_spellbook',
        'library_names': ('wimpy_spell_book.png',),
        'needed': 'assets/irons_spellbooks/textures/item/wimpy_spell_book.png (16x16 icon); '
                  'textures/item/spell_book_models/wimpy_spell_book.png (64x64) for a held 3D book. '
                  'The supplied model itself points at temp_spellbook.',
    },
}

# Texture variables that the vanilla templates draw. The child chain must bind every one.
VANILLA_TEMPLATES = {
    'block/block': (), 'block/cube_all': ('all',), 'block/cube_column': ('end', 'side'),
    'block/stairs': ('bottom', 'top', 'side'), 'block/inner_stairs': ('bottom', 'top', 'side'),
    'block/outer_stairs': ('bottom', 'top', 'side'), 'block/slab': ('bottom', 'top', 'side'),
    'block/slab_top': ('bottom', 'top', 'side'), 'block/template_wall_post': ('wall',),
    'block/template_wall_side': ('wall',), 'block/template_wall_side_tall': ('wall',),
    'block/wall_inventory': ('wall',), 'block/cross': ('cross',),
    'block/orientable': ('top', 'front', 'side'), 'item/generated': ('layer0',),
    'item/handheld': ('layer0',),
}


def split(ref):
    namespace, _, path = ref.rpartition(':')
    return namespace or 'minecraft', path


def qualified(ref):
    if isinstance(ref, dict):  # a material object: {"sprite": ..., "force_translucent": ...}
        ref = ref.get('sprite', '')
    return ref if ref.startswith('#') or ':' in ref else 'minecraft:' + ref


def same_textures(test, expected, actual, message):
    """Supplied vs runtime texture maps. A runtime-only particle may reuse one of the model's own faces."""
    expected = {k: source_to_runtime(v) if not v.startswith('#') else v
                for k, v in ((k, qualified(v)) for k, v in expected.items())}
    actual = {k: qualified(v) for k, v in actual.items()}
    if 'particle' in actual and 'particle' not in expected:
        particle = actual.pop('particle')
        test.assertTrue(particle in expected.values() or particle[1:] in expected,
                        f'{message}: added particle {particle} is not one of the supplied textures')
    test.assertEqual(expected, actual, message)


def runtime_to_source(ref):
    """jugcraft:jimtafacts/ae2/x -> (ae2:x, collection-relative folder); None for Jugcraft-made files."""
    namespace, path = split(ref)
    if namespace == 'jugcraft':
        for prefix, (source_namespace, folder) in SOURCES.items():
            if path.startswith(prefix):
                return source_namespace + ':' + path[len(prefix):], folder
    return None


def source_to_runtime(ref):
    namespace, path = split(ref)
    for prefix, (source_namespace, _) in SOURCES.items():
        if namespace == source_namespace:
            return 'jugcraft:' + prefix + path
    return ref if namespace != 'minecraft' else 'minecraft:' + path


@lru_cache(maxsize=None)
def runtime_model(ref):
    namespace, path = split(ref)
    if namespace != 'jugcraft':
        return None
    file = JUG / 'models' / (path + '.json')
    return json.loads(file.read_text(encoding='utf-8')) if file.is_file() else 'missing'


def supplied_file(relative):
    """A catalogued supplied file, or None if the collection has none. Never silently absent."""
    if relative not in manifest():
        return None
    file = JIMTAFACTS / relative
    if not file.is_file():
        raise AssertionError(f'catalogued original {relative} is not checked out; '
                             'check out art/owner-library/originals/Oct23/Jimtafacts')
    return file


@lru_cache(maxsize=None)
def source_model(ref):
    namespace, path = split(ref)
    folder = next((f for n, f in SOURCES.values() if n == namespace), None)
    if folder is None:
        return None
    file = supplied_file(f'{folder}/models/{path}.json')
    return json.loads(file.read_text(encoding='utf-8')) if file else 'missing'


def resolve(ref, loader):
    """(chain, textures, elements, texture_size, vanilla template) of a model, following parents."""
    chain, textures, elements, size, template = [], {}, None, None, None
    while ref:
        namespace, path = split(ref)
        model = loader(ref)
        if model is None:
            template = path if namespace == 'minecraft' else ref
            chain.append('minecraft:' + path if namespace == 'minecraft' else ref)
            break
        if model == 'missing':
            raise AssertionError(f'missing model {ref} (chain {chain})')
        if ref in chain:
            raise AssertionError(f'parent loop at {ref}')
        chain.append(ref)
        textures = dict(model.get('textures', {}), **textures)
        if elements is None and 'elements' in model:
            elements = model['elements']
        if size is None and 'texture_size' in model:
            size = tuple(model['texture_size'])
        ref = model.get('parent')
    return chain, textures, elements, size, template


def drawn_textures(textures, elements, template):
    """Texture variables the game draws -> resolved texture IDs."""
    variables = {face['texture'] for element in elements or ()
                 for face in element.get('faces', {}).values() if 'texture' in face}
    variables.update('#' + key for key in textures if key == 'particle' or key.startswith('layer'))
    if elements is None:  # the vanilla template supplies the geometry, so it draws its own variables
        if template is not None and template not in ('item/generated', 'item/handheld', 'block/block'):
            variables.update('#' + key for key in textures)
        variables.update('#' + key for key in VANILLA_TEMPLATES.get(template, ()))
    resolved = {}
    for variable in variables:
        value, seen = variable, set()
        while isinstance(value, str) and value.startswith('#') and value not in seen:
            seen.add(value)
            value = textures.get(value[1:])
            value = qualified(value) if isinstance(value, dict) else value
        resolved[variable] = value
    return resolved


def model_refs(node, out):
    """Every model reference in an item definition (vanilla and supplied custom types) or blockstate."""
    if isinstance(node, dict):
        for key, value in node.items():
            if isinstance(value, str) and key in ('model', 'base', 'base_model', 'colored_model',
                                                   'uncolored_model'):
                out.append(value)
            else:
                model_refs(value, out)
    elif isinstance(node, list):
        for value in node:
            model_refs(value, out)
    return out


def png(data):
    """Validate a PNG (signature, CRCs, IHDR, inflated size) and decode 8-bit-or-less pixels to RGBA."""
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError('not a PNG signature')
    position, chunks = 8, []
    while not chunks or chunks[-1][0] != b'IEND':
        if position + 12 > len(data):
            raise ValueError('truncated PNG: no IEND chunk')
        length, kind = struct.unpack('>I4s', data[position:position + 8])
        if position + 12 + length > len(data):
            raise ValueError(f'truncated {kind!r} chunk')
        body = data[position + 8:position + 8 + length]
        crc, = struct.unpack('>I', data[position + 8 + length:position + 12 + length])
        if zlib.crc32(kind + body) & 0xFFFFFFFF != crc:
            raise ValueError(f'bad {kind!r} chunk CRC')
        chunks.append((kind, body))
        position += 12 + length
    if position != len(data):
        raise ValueError('data after IEND')
    if chunks[0][0] != b'IHDR':
        raise ValueError('IHDR is not the first chunk')
    width, height, depth, colour, _, _, interlace = struct.unpack('>IIBBBBB', chunks[0][1])
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colour]
    raw = zlib.decompress(b''.join(body for kind, body in chunks if kind == b'IDAT'))
    if interlace or depth > 8:
        return width, height, None
    stride = (width * channels * depth + 7) // 8
    if len(raw) != height * (stride + 1):
        raise ValueError('inflated image size does not match IHDR')
    palette = next((body for kind, body in chunks if kind == b'PLTE'), b'')
    alpha = next((body for kind, body in chunks if kind == b'tRNS'), b'')
    unit, previous, pixels = max(1, channels * depth // 8), bytearray(stride), []
    for row in range(height):
        kind, line = raw[row * (stride + 1)], bytearray(raw[row * (stride + 1) + 1:(row + 1) * (stride + 1)])
        for i in range(stride):
            left = line[i - unit] if i >= unit else 0
            up, corner = previous[i], previous[i - unit] if i >= unit else 0
            if kind == 1: line[i] = (line[i] + left) & 255
            elif kind == 2: line[i] = (line[i] + up) & 255
            elif kind == 3: line[i] = (line[i] + (left + up) // 2) & 255
            elif kind == 4:
                p = left + up - corner
                pa, pb, pc = abs(p - left), abs(p - up), abs(p - corner)
                line[i] = (line[i] + (left if pa <= pb and pa <= pc else up if pb <= pc else corner)) & 255
        previous = line
        samples = list(line) if depth == 8 else [
            (byte >> (8 - depth * (k + 1))) & ((1 << depth) - 1)
            for byte in line for k in range(8 // depth)]
        for x in range(width):
            s = samples[x * channels:(x + 1) * channels]
            if colour == 3:
                r, g, b = palette[s[0] * 3:s[0] * 3 + 3]
                pixels.append((r, g, b, alpha[s[0]] if s[0] < len(alpha) else 255))
            elif colour == 6: pixels.append(tuple(s))
            elif colour == 2: pixels.append((*s, 255))
            elif colour == 4: pixels.append((s[0], s[0], s[0], s[1]))
            else:
                v = s[0] * 255 // ((1 << depth) - 1)
                pixels.append((v, v, v, 255))
    return width, height, pixels


def frames(width, height, meta):
    """Frame size and count from a texture's animation sidecar, following vanilla's defaults."""
    animation = meta.get('animation') if meta else None
    if animation is None:
        return (width, height), 1, None
    frame_w = animation.get('width', width if 'height' in animation else min(width, height))
    frame_h = animation.get('height', height if 'width' in animation else min(width, height))
    if width % frame_w or height % frame_h:
        raise ValueError(f'{width}x{height} is not a whole number of {frame_w}x{frame_h} frames')
    count = (width // frame_w) * (height // frame_h)
    indices = [f['index'] if isinstance(f, dict) else f for f in animation.get('frames', [])]
    if any(not 0 <= i < count for i in indices):
        raise ValueError(f'frame index out of range 0..{count - 1}: {indices}')
    if animation.get('frametime', 1) < 1 or any(isinstance(f, dict) and f.get('time', 1) < 1
                                                 for f in animation.get('frames', [])):
        raise ValueError('frame time below one tick')
    return (frame_w, frame_h), count, indices


@lru_cache(maxsize=None)
def manifest():
    return {entry['path']: entry for entry in json.loads(MANIFEST.read_text(encoding='utf-8'))['files']}


@lru_cache(maxsize=None)
def atlas():
    return json.loads(ATLAS.read_text(encoding='utf-8'))['sources']


def stitched(texture):
    namespace, path = split(texture)
    if path.startswith(('block/', 'item/')):
        return True  # vanilla's own block and item directory sources
    for source in atlas():
        if source['type'] == 'minecraft:single' and source.get('sprite', source.get('resource')) == texture:
            return True
        prefix = source.get('prefix', '')
        if (source['type'] == 'minecraft:directory' and path.startswith(prefix)
                and (ASSETS / namespace / 'textures' / source['source'] / (path[len(prefix):] + '.png')).is_file()):
            return True
    return False


@lru_cache(maxsize=None)
def library_basenames():
    """File names anywhere in art/owner-library (git index first, so sparse checkouts still see all)."""
    try:
        listing = subprocess.run(['git', '-C', str(ROOT), 'ls-files', '-z', 'art/owner-library'],
                                 capture_output=True, check=True).stdout.decode('utf-8').split('\0')
        if len(listing) > 1000:
            return frozenset(Path(p).name.lower() for p in listing if p)
    except (OSError, subprocess.CalledProcessError):
        pass
    return frozenset(p.name.lower() for p in (ROOT / 'art/owner-library').rglob('*') if p.is_file())


def audit(name):
    """Resolve one in-scope ID. Returns its row; raises AssertionError on a broken reference."""
    registry = 'jimta_' + name
    item_file, state_file = JUG / 'items' / (registry + '.json'), JUG / 'blockstates' / (registry + '.json')
    roots = []
    if item_file.is_file():
        roots += model_refs(json.loads(item_file.read_text(encoding='utf-8')), [])
    if state_file.is_file():
        roots += model_refs(json.loads(state_file.read_text(encoding='utf-8')), [])
    if not roots:
        raise AssertionError(f'{registry}: no item definition or blockstate model')
    used = {}
    for root in dict.fromkeys(roots):
        if split(root)[0] == 'minecraft':
            raise AssertionError(f'{registry}: root model {root} is vanilla, not the supplied art')
        chain, textures, elements, size, template = resolve(root, runtime_model)
        drawn = drawn_textures(textures, elements, template)
        if not drawn:
            raise AssertionError(f'{registry}: {root} draws no texture')
        for variable, texture in drawn.items():
            if not isinstance(texture, str) or not texture or texture.startswith('#'):
                raise AssertionError(f'{registry}: {root} leaves {variable} unbound (chain {chain})')
            used.setdefault(qualified(texture), (root, size, template))
    return registry, item_file.is_file(), state_file.is_file(), used


def check_texture(test, registry, texture, size, template):
    """Existence, PNG validity, frame geometry, atlas stitching and supplied-original identity."""
    namespace, path = split(texture)
    test.assertNotEqual(namespace, 'minecraft', f'{registry} draws vanilla {texture}, not supplied art')
    file = ASSETS / namespace / 'textures' / (path + '.png')
    test.assertTrue(file.is_file(), f'{registry}: missing texture file {file.relative_to(ROOT)}')
    data = file.read_bytes()
    width, height, pixels = png(data)
    sidecar = file.with_name(file.name + '.mcmeta')
    meta = json.loads(sidecar.read_text(encoding='utf-8')) if sidecar.is_file() else None
    (frame_w, frame_h), count, _ = frames(width, height, meta)
    test.assertIn(frame_w, (16, 32, 64, 128, 256), f'{registry}: {texture} frame width {frame_w}')
    if template in ('item/generated', 'item/handheld'):
        test.assertEqual(frame_w, frame_h, f'{registry}: flat icon {texture} has non-square frames')
    if size:
        test.assertEqual(frame_w * size[1], frame_h * size[0],
                         f'{registry}: {texture} {frame_w}x{frame_h} frame vs model texture_size {size}')
    if meta is None:
        test.assertTrue(width == height or size, f'{registry}: {texture} is {width}x{height} with no sidecar')
    test.assertTrue(stitched(texture), f'{registry}: {texture} is not stitched into the block atlas')
    if pixels is not None:
        visible = [p for p in pixels if p[3]]
        test.assertTrue(visible, f'{registry}: {texture} is fully transparent')
        test.assertFalse(all(p[:3] in ((248, 0, 248), (255, 0, 255), (0, 0, 0)) for p in visible)
                         and any(p[:3] != (0, 0, 0) for p in visible),
                         f'{registry}: {texture} is a magenta/black missing-texture checkerboard')
    source = runtime_to_source(texture)
    test.assertIsNotNone(source, f'{registry}: {texture} has no supplied original (not a Jimtafacts copy)')
    source_ref, folder = source
    relative = f'{folder}/textures/{split(source_ref)[1]}.png'
    entry = manifest().get(relative)
    test.assertIsNotNone(entry, f'{registry}: no catalog entry for supplied {relative}')
    test.assertEqual(entry['sha256'], hashlib.sha256(data).hexdigest(),
                     f'{registry}: {texture} differs from supplied {relative}')
    test.assertEqual(supplied_file(relative).read_bytes(), data, f'{registry}: {texture} differs from {relative}')
    supplied = supplied_file(relative + '.mcmeta')
    if supplied:
        test.assertIsNotNone(meta, f'{registry}: {texture} lost its supplied animation sidecar')
        test.assertEqual(json.loads(supplied.read_text(encoding='utf-8')), meta,
                         f'{registry}: {texture} animation differs from the supplied sidecar')
    else:
        test.assertIsNone(meta, f'{registry}: {texture} has an animation sidecar the original lacks')
    return width, height, count


def source_roots(name):
    """The supplied item models for an ID, expanding NeoForge separate_transforms perspectives."""
    namespace, folder = (SOURCES['jimtafacts/spellbooks/'] if name.startswith('spellbooks_')
                         else SOURCES['jimtafacts/ae2/'])
    base = name[len('spellbooks_'):] if name.startswith('spellbooks_') else name
    definition = supplied_file(f'{folder}/items/{base}.json')
    if definition:
        return {'': model_refs(json.loads(definition.read_text(encoding='utf-8')), [])}
    model = source_model(f'{namespace}:item/{base}')
    if not isinstance(model, dict):
        return {}
    if model.get('loader') == 'neoforge:separate_transforms':
        return {'fallback': model['base'], **model.get('perspectives', {})}
    return {'': [f'{namespace}:item/{base}']}


class ReportedMissingTexturesTests(unittest.TestCase):
    def test_scope_ids_are_real_displayed_content(self):
        lang = json.loads(LANG.read_text(encoding='utf-8'))
        for family, names in SCOPE.items():
            for name in names:
                with self.subTest(family=family, id=name):
                    self.assertTrue(f'item.jugcraft.jimta_{name}' in lang or f'block.jugcraft.jimta_{name}' in lang,
                                    f'jimta_{name} has no display name')

    def test_every_reported_item_resolves_to_supplied_art(self):
        for family, names in SCOPE.items():
            for name in names:
                with self.subTest(family=family, id=name):
                    registry, _, _, used = audit(name)
                    placeholders = {t for t in used if t in PLACEHOLDER_TEXTURES}
                    if name in AWAITING_ORIGINAL:
                        self.assertEqual(placeholders, {AWAITING_ORIGINAL[name]['renders']},
                                         f'{registry} is awaiting an original but no longer shows its placeholder;'
                                         ' remove it from AWAITING_ORIGINAL')
                    else:
                        self.assertFalse(placeholders, f'{registry} renders placeholder {placeholders}')
                    for texture, (root, size, template) in used.items():
                        check_texture(self, registry, texture, size, template)

    def test_runtime_models_reference_the_same_textures_as_the_supplied_models(self):
        for family, names in SCOPE.items():
            for name in names:
                with self.subTest(family=family, id=name):
                    registry, _, _, used = audit(name)
                    runtime_roots = set(model_refs(json.loads((JUG / 'items' / (registry + '.json')).read_text(
                        encoding='utf-8')), [])) if (JUG / 'items' / (registry + '.json')).is_file() else set()
                    supplied = source_roots(name)
                    if not supplied:
                        self.assertNotIn(name, [n for n in AWAITING_ORIGINAL if AWAITING_ORIGINAL[n]['registered']])
                        continue  # block-only content: compared through its blockstate below
                    for perspective, refs in supplied.items():
                        if isinstance(refs, dict):  # an inline separate_transforms perspective
                            inline = resolve(refs['parent'], source_model)[1] if refs.get('parent') else {}
                            matches = [r for r in runtime_roots
                                       if (perspective == 'fallback' and '/perspective/' not in r)
                                       or r.endswith('_' + perspective)]
                            self.assertEqual(len(matches), 1, f'{registry}: no runtime {perspective} model')
                            same_textures(self, dict(inline, **refs.get('textures', {})),
                                          resolve(matches[0], runtime_model)[1],
                                          f'{registry} {perspective} textures differ from the supplied perspective')
                            continue
                        for ref in refs:
                            runtime_ref = source_to_runtime(ref)
                            self.assertIn(runtime_ref, runtime_roots, f'{registry}: supplied model {ref} not used')
                            same_textures(self, resolve(ref, source_model)[1], resolve(runtime_ref, runtime_model)[1],
                                          f'{registry}: {runtime_ref} textures differ from supplied {ref}')

    def test_block_models_match_their_supplied_blockstates(self):
        for family, names in SCOPE.items():
            for name in names:
                state = JUG / 'blockstates' / f'jimta_{name}.json'
                if not state.is_file():
                    continue
                with self.subTest(family=family, id=name):
                    namespace, folder = (SOURCES['jimtafacts/spellbooks/'] if name.startswith('spellbooks_')
                                         else SOURCES['jimtafacts/ae2/'])
                    base = name[len('spellbooks_'):] if name.startswith('spellbooks_') else name
                    supplied = supplied_file(f'{folder}/blockstates/{base}.json')
                    if not supplied:
                        continue  # the supplied collection has no blockstate for this ID
                    runtime = set(model_refs(json.loads(state.read_text(encoding='utf-8')), []))
                    for ref in set(model_refs(json.loads(supplied.read_text(encoding='utf-8')), [])):
                        runtime_ref = source_to_runtime(ref)
                        if source_model(ref) == 'missing':
                            continue  # supplied blockstate names a code-baked model; Jugcraft authors those
                        self.assertIn(runtime_ref, runtime, f'jimta_{name}: supplied {ref} is not placed')
                        same_textures(self, resolve(ref, source_model)[1], resolve(runtime_ref, runtime_model)[1],
                                      f'jimta_{name}: {runtime_ref} textures differ from supplied {ref}')

    def test_awaiting_originals_are_still_absent_and_documented(self):
        names = library_basenames()
        self.assertGreater(len(names), 1000, 'owner library listing unavailable')
        for name, entry in AWAITING_ORIGINAL.items():
            with self.subTest(id=name):
                self.assertIn(entry['family'], SCOPE)
                if entry['registered']:
                    self.assertIn(name, SCOPE[entry['family']])
                self.assertGreater(len(entry['needed']), 40)
                supplied = [n for n in entry['library_names'] if n.lower() in names]
                self.assertFalse(supplied, f'{name}: an original now exists ({supplied}); import it and '
                                           'remove the AWAITING_ORIGINAL entry')
                if not entry['registered']:
                    for candidate in entry['candidate_ids']:
                        for folder in ('items', 'blockstates'):
                            self.assertFalse((JUG / folder / f'jimta_{candidate}.json').is_file(),
                                             f'jimta_{candidate} is registered without supplied art; add it to SCOPE')

    def test_png_reader_rejects_damaged_files(self):
        data = (JUG / 'textures/jimtafacts/ae2/item/certus_quartz_crystal.png').read_bytes()
        self.assertEqual(png(data)[:2], (16, 16))
        with self.assertRaises(ValueError):
            png(data[:40] + bytes([data[40] ^ 1]) + data[41:])
        with self.assertRaises(ValueError):
            png(b'GIF89a' + data[6:])
        with self.assertRaises(ValueError):
            png(data + b'x')
        with self.assertRaises(ValueError):
            png(data[:-20])
        with self.assertRaises(ValueError):
            frames(16, 48, {'animation': {'frames': [0, 3]}})


if __name__ == '__main__':
    unittest.main()
