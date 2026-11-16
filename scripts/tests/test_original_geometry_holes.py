"""Original-model holes that the art audit's O1 probe never reached.

tools/art_check._open_faces probes only elements without a "rotation" key. Blockbench writes
{"angle": 0, ...} on most of the owner's imported elements, so their left-out faces went unchecked
although such a box is not turned at all. These tests run the same probe (art_check._open_faces,
with its covers, opacity and 0/16 seam rules) on the repaired original models with zero-angle
rotations normalised away, in the states where the holes showed, and keep the reviewed intentional
openings explicit, each with its reason. They need the full resources (models and textures).
"""
import copy
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import art_check
import newest_multipart_art as multipart

MODELS = art_check.ASSETS / 'models'
TIERS = ('backpack', 'copper_backpack', 'iron_backpack', 'gold_backpack', 'diamond_backpack', 'netherite_backpack')
# Repaired on 12 November 2026: each was an exposed, unrotated opaque box face.
REPAIRED = {
    **{'newest/packworks/block/' + tier + '.json': "carry handle's top bar showed a see-through slot from below"
       for tier in TIERS},
    'newest/supps/block/rope_up.json': 'upper rope end was hollow where no knot covers it',
    'newest/supps/block/rope_down.json': 'lower rope end was hollow where no knot covers it',
    'newest/supps/block/rope_connection.json': 'side rope end was hollow at corners and wall-to-hanging joins',
}
_HANDLE = ("open U-shaped lunch-basket handle: this box is the handle's outer skin and the next element is "
           "its exact reversed partner (the inward skin). Its north/south faces would fill the handle's "
           "opening, as reviewed for the standing baskets on 1 November")
# Intentional openings this probe finds in the original collection: {model: (left-out faces, reason)}.
INTENTIONAL_OPEN = {
    'newest/supps/block/lunch_basket_hanging.json': ({(2, 'north'), (2, 'south')}, _HANDLE),
    'newest/supps/block/lunch_basket_hanging_open.json': ({(2, 'north'), (2, 'south')}, _HANDLE),
    'newest/supps/block/lunch_basket_hanging_dyed.json': ({(3, 'north'), (3, 'south')}, _HANDLE),
    'newest/supps/block/lunch_basket_hanging_open_dyed.json': ({(3, 'north'), (3, 'south')}, _HANDLE),
}


def unturned(elements):
    """The same boxes without zero-angle rotations, which turn nothing, so O1 probes them."""
    elements = copy.deepcopy(elements)
    for element in elements:
        rotation = element.get('rotation')
        if rotation is not None and abs(rotation.get('angle', 0)) < 1e-9:
            del element['rotation']
    return elements


def holes(elements, lookup):
    faces, _ = art_check._model_faces(elements, lookup)
    return set(art_check._open_faces(elements, faces))


class OriginalGeometryHoleTests(unittest.TestCase):
    def model(self, relative):
        path = MODELS / relative
        elements, lookup, _ = art_check._resolve(json.loads(path.read_text(encoding='utf-8')), (art_check.ASSETS,))
        textures = {lookup(face.get('texture', '')) for element in elements for face in element['faces'].values()}
        # Without real textures every box reads as see-through and the probe would pass vacuously.
        self.assertTrue(textures and all(t is not None and t.exists() for t in textures), relative)
        return unturned(elements), lookup

    def test_repaired_original_models_have_no_exposed_left_out_faces(self):
        for relative, was in REPAIRED.items():
            with self.subTest(model=relative, was=was):
                self.assertEqual(set(), holes(*self.model(relative)))

    def test_rope_joins_without_a_knot_are_closed(self):
        """The native knot only comes from shears, so ropes join without it (OwnerSupplementRopes)."""
        parts = json.loads((art_check.ASSETS / 'blockstates/supps_rope.json').read_text(encoding='utf-8'))['multipart']
        _, lookup = self.model('newest/supps/block/rope_knot.json')
        states = ({'north'}, {'north', 'down'}, {'north', 'east'}, {'north', 'east', 'down'},
                  {'up', 'down'}, {'up', 'north'}, {'north', 'south'}, {'up', 'north', 'south', 'down'})
        for connected in states:
            with self.subTest(connected=sorted(connected)):
                combined = []
                for part in parts:
                    (key, value), = part['when'].items()
                    if value is True and key in connected:
                        combined += unturned(multipart.geometry(part['apply']))
                self.assertTrue(combined)
                self.assertEqual(set(), holes(combined, lookup))

    def test_intentional_openings_are_exact_and_explained(self):
        for relative, (faces, reason) in INTENTIONAL_OPEN.items():
            with self.subTest(model=relative):
                self.assertGreater(len(reason), 40)
                elements, lookup = self.model(relative)
                self.assertEqual(faces, holes(elements, lookup), 'an entry may cover only its reviewed faces')
                (index,) = {i for i, _ in faces}
                outer, inner = elements[index], elements[index + 1]
                self.assertEqual((outer['from'], outer['to']), (inner['to'], inner['from']))
                # The two skins together draw every side of the ribbon but the opening's.
                self.assertEqual({'north', 'south'}, set(art_check._NORMAL) - set(outer['faces']) - set(inner['faces']))


if __name__ == '__main__':
    unittest.main()
