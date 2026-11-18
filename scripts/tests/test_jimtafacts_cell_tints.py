"""November 13: storage cell item definitions keep the supplied status LED and portable screen tints.

The supplied AE2 item definitions tint layer 1 (the LED) with ae2:storage_cell_state and, on portable cells, layer 2
(the screen) with ae2:portable_cell_color. The runtime copies must carry the same tint lists with the native sources
(jugcraft:jimta_storage_cell_state, jugcraft:jimta_portable_cell_color), the client must register those sources, and
the colours in DigitalCellLeds must be the ones in the supplied classes (CellState, AEItems), read here from their
constant pools as data. Run with: python scripts/tests/test_jimtafacts_cell_tints.py
"""
import json
import re
import struct
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SUPPLIED = ROOT / 'art/owner-library/originals/Oct23/Jimtafacts/applied energentics reborn by jimbo'
SOURCE_ITEMS = SUPPLIED / 'assets/ae2/items'
SOURCE_MODELS = SUPPLIED / 'assets/ae2/models'
JUG = ROOT / 'src/main/resources/assets/jugcraft'
LEDS = ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/jimtafacts/network/DigitalCellLeds.java'
TINTS = ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client/jimtafacts/DigitalCellTints.java'
STORAGE_CLIENT = ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client/jimtafacts/DigitalStorageClient.java'
NATIVE = {'ae2:storage_cell_state': 'jugcraft:jimta_storage_cell_state',
          'ae2:portable_cell_color': 'jugcraft:jimta_portable_cell_color'}
CELLS = ([f'{kind}_storage_cell_{size}k' for kind in ('item', 'fluid') for size in (1, 4, 16, 64, 256)]
         + [f'portable_{kind}_cell_{size}k' for kind in ('item', 'fluid') for size in (1, 4, 16, 64, 256)])


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def supplied_cell_definitions():
    """{name: supplied tint list} for every supplied item definition using a cell tint type."""
    found = {}
    for path in sorted(SOURCE_ITEMS.glob('*.json')):
        tints = read(path)['model'].get('tints') or []
        if any(t['type'] in NATIVE for t in tints):
            found[path.stem] = tints
    return found


def constant_pool(path):
    """(integer constants, utf8 strings) of a class file's constant pool, in pool order. Reads bytes only."""
    data = path.read_bytes()
    if data[:4] != b'\xca\xfe\xba\xbe':
        raise ValueError(f'{path} is not a class file')
    count = struct.unpack('>H', data[8:10])[0]
    ints, strings, index, pos = [], [], 1, 10
    while index < count:
        tag = data[pos]
        pos += 1
        if tag == 1:
            length = struct.unpack('>H', data[pos:pos + 2])[0]
            strings.append(data[pos + 2:pos + 2 + length].decode('utf-8', 'replace'))
            pos += 2 + length
        elif tag == 3:
            ints.append(struct.unpack('>i', data[pos:pos + 4])[0])
            pos += 4
        elif tag == 4:
            pos += 4
        elif tag in (5, 6):
            pos += 8
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            pos += 4
        elif tag == 15:
            pos += 3
        else:
            raise ValueError(f'{path}: unknown constant tag {tag}')
        index += 1
    return ints, strings


class CellTintDefinitions(unittest.TestCase):
    def test_supplied_cell_definitions_are_the_twenty_cells(self):
        self.assertEqual(sorted(supplied_cell_definitions()), sorted(CELLS))

    def test_every_cell_definition_carries_the_supplied_tints(self):
        for name, tints in supplied_cell_definitions().items():
            with self.subTest(name):
                model = read(JUG / f'items/jimta_{name}.json')['model']
                self.assertEqual(model['type'], 'minecraft:model')
                self.assertEqual(model['model'], f'jugcraft:jimtafacts/ae2/item/{name}')
                expected = [dict(t, type=NATIVE.get(t['type'], t['type'])) for t in tints]
                self.assertEqual(model.get('tints'), expected)
                self.assertEqual(model['tints'][1], {'type': 'jugcraft:jimta_storage_cell_state'})
                if name.startswith('portable_'):
                    self.assertEqual(model['tints'][2], {'type': 'jugcraft:jimta_portable_cell_color'})

    def test_tinted_layers_are_the_supplied_led_and_screen(self):
        for name, tints in supplied_cell_definitions().items():
            with self.subTest(name):
                runtime = read(JUG / f'models/jimtafacts/ae2/item/{name}.json')
                source = read(SOURCE_MODELS / f'item/{name}.json')
                self.assertEqual(runtime['parent'], 'minecraft:item/generated')
                self.assertEqual(runtime['textures'],
                                 {k: v.replace('ae2:', 'jugcraft:jimtafacts/ae2/') for k, v in source['textures'].items()})
                for index in range(len(tints)):
                    self.assertIn(f'layer{index}', runtime['textures'])
                led = runtime['textures']['layer1']
                self.assertTrue(led.endswith('/portable_cell_led' if name.startswith('portable_') else '/storage_cell_led'), led)
                if name.startswith('portable_'):
                    self.assertTrue(runtime['textures']['layer2'].endswith('/portable_cell_screen'))

    def test_native_cell_tints_appear_only_on_cells(self):
        for path in sorted((JUG / 'items').glob('jimta_*.json')):
            text = path.read_text(encoding='utf-8')
            if 'jimta_storage_cell_state' in text or 'jimta_portable_cell_color' in text:
                self.assertIn(path.stem.removeprefix('jimta_'), CELLS, path.name)
            self.assertNotIn('ae2:', text, path.name)

    def test_generator_writes_the_same_tints(self):
        sys.path.insert(0, str(ROOT / 'tools'))
        import jimtafacts_network as network
        for name in network.ITEMS + ['fluix_glass_cable']:
            with self.subTest(name):
                written = read(JUG / f'items/jimta_{name}.json')['model']
                tints = network.item_tints(name)
                if name in CELLS:
                    self.assertEqual(tints, {'tints': written['tints']})
                else:
                    self.assertEqual(tints, {})

    def test_client_registers_the_native_sources(self):
        tints = TINTS.read_text(encoding='utf-8')
        self.assertIn('ItemTintSources.ID_MAPPER.put(Jugcraft.id("jimta_storage_cell_state")', tints)
        self.assertIn('ItemTintSources.ID_MAPPER.put(Jugcraft.id("jimta_portable_cell_color")', tints)
        self.assertIn('DigitalCellTints.register();', STORAGE_CLIENT.read_text(encoding='utf-8'))

    def test_colours_are_the_supplied_values(self):
        java = LEDS.read_text(encoding='utf-8')
        colours = [int(v, 16) for v in re.search(r'COLORS = \{([^}]*)\}', java).group(1).replace('0x', '').split(',')]
        portable = int(re.search(r'PORTABLE_DEFAULT_COLOR = 0x([0-9A-Fa-f]+);', java).group(1), 16)
        state_ints, state_names = constant_pool(SUPPLIED / 'appeng/api/storage/cells/CellState.class')
        for name in ('ABSENT', 'EMPTY', 'NOT_EMPTY', 'TYPES_FULL', 'FULL', 'stateColor'):
            self.assertIn(name, state_names)
        # ABSENT is constructed with iconst_0; the other four colours are the pool's integers, in enum order.
        self.assertEqual(state_ints, [0x00FF00, 0x00AAFF, 0xFFAA00, 0xFF0000])
        self.assertEqual(colours, [0] + state_ints)
        items_ints, _ = constant_pool(SUPPLIED / 'appeng/core/definitions/AEItems.class')
        self.assertIn(portable, items_ints)
        self.assertEqual(portable, 0x80CAFF)
        _, tint_names = constant_pool(SUPPLIED / 'appeng/client/item/StorageCellStateTintSource.class')
        self.assertIn('storage_cell_state', tint_names)
        _, portable_names = constant_pool(SUPPLIED / 'appeng/client/item/PortableCellColorTintSource.class')
        self.assertIn('getColor', portable_names)


if __name__ == '__main__':
    unittest.main()
