"""November 13: the drive cell models' open sides stay sealed in their bays; the color applicator has no slits.

Worker B's November 12 audit reported left-out south and east faces on the ten drive cell chassis and exposed faces on
color_applicator and color_applicator_colored.

* Drive cells: the supplied models leave out the east, west and south faces too. The runtime copies must stay identical
  to them, and every left-out face must be unreachable from outside the drive (and the ME chest) once the chassis sits
  at the supplied bay origin: the sides are flush against the housing walls and the back faces into a closed bay.
* Color applicator: the supplied models leave no face exposed. Their art_check Z1/N1 overlaps are all between faces
  buried inside opaque neighbours. The runtime copies must be the supplied geometry and UVs with only the audited buried
  faces of jimtafacts_model_repairs.BURIED left out, expose no left-out face (the old separation pass opened slits up to
  0.2 px wide) and pass art_check's per-model rules.

The flood fill and coverage scans are pure Python. The opacity and art_check rule checks need numpy and Pillow (as in
CI) and are skipped without them. Run with: python scripts/tests/test_jimtafacts_storage_geometry.py
"""
import json
import math
import re
import sys
import unittest
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
SUPPLIED = ROOT / 'art/owner-library/originals/Oct23/Jimtafacts/applied energentics reborn by jimbo/assets/ae2/models'
RUNTIME = ROOT / 'src/main/resources/assets/jugcraft/models/jimtafacts/ae2'
RENDERER = ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client/jimtafacts/DigitalStorageRenderer.java'
DRIVE_CELLS = [f'block/drive_{size}k_{kind}_cell' for size in (1, 4, 16, 64, 256) for kind in ('item', 'fluid')]
APPLICATORS = ['item/color_applicator', 'item/color_applicator_colored']
FACES = {'west': (0, -1), 'east': (0, 1), 'down': (1, -1), 'up': (1, 1), 'north': (2, -1), 'south': (2, 1)}
AXES = {'x': 0, 'y': 1, 'z': 2}


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def runtime_texture(ref):
    return ref.replace('ae2:', 'jugcraft:jimtafacts/ae2/') if isinstance(ref, str) else ref


def turn(element):
    """(to_world, to_local) point maps for an element's rotation (Minecraft's right-handed axis rotation)."""
    rotation = element.get('rotation')
    if not rotation or abs(rotation.get('angle', 0)) < 1e-9:
        return (lambda p: list(p)), (lambda p: list(p))
    if rotation.get('rescale'):
        raise AssertionError('rescaled rotations are not modelled by this test')
    axis, origin, angle = AXES[rotation['axis']], rotation['origin'], math.radians(rotation['angle'])
    i, j = {0: (1, 2), 1: (2, 0), 2: (0, 1)}[axis]
    c, s = math.cos(angle), math.sin(angle)

    def rotate(p, sign):
        q = [p[k] - origin[k] for k in range(3)]
        r = list(q)
        r[i], r[j] = q[i] * c - sign * q[j] * s, sign * q[i] * s + q[j] * c
        return [r[k] + origin[k] for k in range(3)]
    return (lambda p: rotate(p, 1)), (lambda p: rotate(p, -1))


def inside(element, local):
    return all(element['from'][k] - 1e-6 <= local[k] <= element['to'][k] + 1e-6 for k in range(3))


def face_points(element, face, step, offset):
    """Points on a grid over an element's face plane, `offset` px outside it, in the element's own coordinates."""
    low, high = element['from'], element['to']
    axis, sign = FACES[face]
    a, b = [k for k in range(3) if k != axis]
    na, nb = max(1, round((high[a] - low[a]) / step)), max(1, round((high[b] - low[b]) / step))
    for i in range(na):
        for j in range(nb):
            p = [0.0] * 3
            p[axis] = (high[axis] if sign > 0 else low[axis]) + sign * offset
            p[a] = low[a] + (i + .5) / na * (high[a] - low[a])
            p[b] = low[b] + (j + .5) / nb * (high[b] - low[b])
            yield p


def left_out(element):
    present = set(element.get('faces', {}))
    solid = all(element['to'][k] - element['from'][k] > 1e-6 for k in range(3))
    return [f for f in FACES if f not in present] if present and solid else []


def exposed(elements, covers=None, step=0.05, offset=0.005):
    """{(index, face): uncovered fraction} of each left-out face: a point just outside it in no covering element."""
    maps = [turn(e) for e in elements]
    covers = range(len(elements)) if covers is None else covers
    result = {}
    for n, element in enumerate(elements):
        for face in left_out(element):
            points = list(face_points(element, face, step, offset))
            free = sum(1 for p in points if not any(
                m != n and inside(elements[m], maps[m][1](maps[n][0](p))) for m in covers))
            if free:
                result[(n, face)] = round(free / len(points), 4)
    return result


def buried(elements, n, face, covers, step=0.05, offset=0.005):
    """Fraction of a face whose points just in front of it lie in none of the covering elements."""
    maps = [turn(e) for e in elements]
    points = list(face_points(elements[n], face, step, offset))
    free = sum(1 for p in points if not any(
        m != n and inside(elements[m], maps[m][1](maps[n][0](p))) for m in covers))
    return free / len(points)


def solid_voxels(element, resolution=0.5, low=-1.0, size=36):
    """Voxels (of a grid from `low`, `size` cells a side) whose centre lies inside an element."""
    world, local = turn(element)
    corners = [world([element['to' if (c >> k) & 1 else 'from'][k] for k in range(3)]) for c in range(8)]
    ranges = [range(max(0, int((min(p[k] for p in corners) - low) // resolution)),
                    min(size, int((max(p[k] for p in corners) - low) // resolution) + 1)) for k in range(3)]
    centre = [low + (i + .5) * resolution for i in range(size)]
    return {(x, y, z) for x in ranges[0] for y in ranges[1] for z in ranges[2]
            if inside(element, local((centre[x], centre[y], centre[z])))}


def reachable_from_outside(solid, resolution=0.5, low=-1.0, size=36):
    """Flood fill (6-connected) of the empty voxels reachable from outside the block; returns a point predicate."""
    seen, todo = {(0, 0, 0)}, deque([(0, 0, 0)])
    while todo:
        x, y, z = todo.popleft()
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            nxt = (x + dx, y + dy, z + dz)
            if all(0 <= v < size for v in nxt) and nxt not in seen and nxt not in solid:
                seen.add(nxt)
                todo.append(nxt)
    return lambda p: tuple(int((p[k] - low) // resolution) for k in range(3)) in seen


def moved(element, offset):
    copy = json.loads(json.dumps(element))
    copy['from'] = [copy['from'][k] + offset[k] for k in range(3)]
    copy['to'] = [copy['to'][k] + offset[k] for k in range(3)]
    if copy.get('rotation'):
        copy['rotation']['origin'] = [copy['rotation']['origin'][k] + offset[k] for k in range(3)]
    return copy


class DriveCellBays(unittest.TestCase):
    # Supplied DriveModel.getSlotOrigin: ((9 - col * 8) / 16, (13 - row * 3) / 16, 1 / 16); MEChestRenderer: (5/16, 4/16, 0).
    DRIVE_SLOTS = [(9 - slot % 2 * 8, 13 - slot // 2 * 3, 1) for slot in range(10)]
    CHEST_SLOT = (5, 4, 0)

    def test_runtime_chassis_are_the_supplied_models(self):
        for ref in DRIVE_CELLS:
            with self.subTest(ref):
                supplied, runtime = read(SUPPLIED / f'{ref}.json'), read(RUNTIME / f'{ref}.json')
                self.assertEqual(runtime['elements'], supplied['elements'])
                self.assertEqual(runtime['textures'], {k: runtime_texture(v) for k, v in supplied['textures'].items()})
                self.assertEqual(len(runtime['elements']), 1)
                chassis = runtime['elements'][0]
                self.assertEqual((chassis['from'], chassis['to']), ([0, 0, 0], [6, 2, 2]))
                self.assertEqual(left_out(chassis), ['west', 'east', 'south'])

    def test_renderer_uses_the_supplied_bay_origins(self):
        java = RENDERER.read_text(encoding='utf-8')
        self.assertIn('pose.translate((9 - slot % 2 * 8) / 16.0, (13 - slot / 2 * 3) / 16.0, .0625);', java)
        self.assertIn('else pose.translate(.3125, .25, 0);', java)

    def check_sealed(self, base, slots):
        housing = set()
        for element in read(RUNTIME / f'block/{base}.json')['elements']:
            housing |= solid_voxels(element)
        chassis = read(RUNTIME / f'{DRIVE_CELLS[0]}.json')['elements'][0]  # all ten share this box (test above)
        for slot in slots:
            with self.subTest(base=base, slot=slot):
                cell = moved(chassis, slot)
                outside = reachable_from_outside(housing | solid_voxels(cell))
                self.assertTrue(all(outside(p) for p in face_points(cell, 'north', 0.25, 0.01)),
                                'the drawn front faces the open bay')
                for face in left_out(cell):
                    open_points = [p for p in face_points(cell, face, 0.25, 0.01) if outside(p)]
                    self.assertEqual(open_points, [], f'{face} face reachable from outside')

    def test_open_sides_are_sealed_in_every_drive_bay(self):
        self.check_sealed('drive_base', self.DRIVE_SLOTS)

    def test_open_sides_are_sealed_in_the_me_chest(self):
        self.check_sealed('me_chest_base', [self.CHEST_SLOT])


def art_check_or_skip(case):
    try:
        import art_check
    except ImportError as missing:  # numpy and Pillow are installed in CI
        case.skipTest(f'art_check needs numpy and Pillow: {missing}')
    return art_check


def opaque_elements(art_check, model):
    elements, lookup, _ = art_check._resolve(model, (art_check.ASSETS,))
    faces, _ = art_check._model_faces(elements, lookup)
    by_element = {}
    for face in faces:
        by_element.setdefault(face.label[0], []).append(face)
    return [n for n in range(len(elements)) if art_check._opaque(by_element.get(n, []))]


def supplied_with_runtime_textures(ref):
    model = read(SUPPLIED / f'{ref}.json')
    model['textures'] = {k: runtime_texture(v) for k, v in model['textures'].items()}
    return model


class ColorApplicatorGeometry(unittest.TestCase):
    def test_runtime_is_the_supplied_geometry_less_the_audited_buried_faces(self):
        from jimtafacts_model_repairs import BURIED, REPAIRED, CLOSED
        for ref in APPLICATORS:
            with self.subTest(ref):
                key = 'jimtafacts/ae2/' + ref
                self.assertNotIn(key, REPAIRED | CLOSED)
                supplied, runtime = read(SUPPLIED / f'{ref}.json'), read(RUNTIME / f'{ref}.json')
                self.assertEqual(runtime['textures'], {k: runtime_texture(v) for k, v in supplied['textures'].items()})
                self.assertEqual(len(runtime['elements']), len(supplied['elements']))
                dropped = {}
                for n, (before, after) in enumerate(zip(supplied['elements'], runtime['elements'])):
                    for key_name in ('from', 'to', 'rotation'):
                        self.assertEqual(after.get(key_name), before.get(key_name), (n, key_name))
                    missing = [f for f in before['faces'] if f not in after['faces']]
                    if missing:
                        dropped[n] = tuple(sorted(missing))
                    self.assertFalse(set(after['faces']) - set(before['faces']), n)
                    for face, data in after['faces'].items():
                        source = dict(before['faces'][face])
                        if not source['texture'].startswith('#'):
                            source['texture'] = '#' + source['texture']
                        if ref == 'item/color_applicator':
                            source.pop('tintindex', None)  # the uncolored model is drawn untinted
                        self.assertEqual(data, source, (n, face))
                self.assertEqual(dropped, {n: tuple(sorted(f)) for n, f in BURIED[key].items()})

    def test_no_left_out_face_is_exposed(self):
        for ref in APPLICATORS:
            with self.subTest(ref):
                self.assertEqual(exposed(read(SUPPLIED / f'{ref}.json')['elements']), {}, 'supplied model')
                self.assertEqual(exposed(read(RUNTIME / f'{ref}.json')['elements']), {})

    def test_left_out_faces_rely_on_no_new_see_through_covers(self):
        art_check = art_check_or_skip(self)
        for ref in APPLICATORS:
            with self.subTest(ref):
                supplied, runtime = supplied_with_runtime_textures(ref), read(RUNTIME / f'{ref}.json')
                expected = exposed(supplied['elements'], opaque_elements(art_check, supplied))
                self.assertEqual(exposed(runtime['elements'], opaque_elements(art_check, runtime)), expected)

    def test_left_out_faces_are_buried_in_opaque_neighbours(self):
        art_check = art_check_or_skip(self)
        from jimtafacts_model_repairs import BURIED
        for ref in APPLICATORS:
            supplied = supplied_with_runtime_textures(ref)
            covers = opaque_elements(art_check, supplied)
            for n, faces in BURIED['jimtafacts/ae2/' + ref].items():
                for face in faces:
                    with self.subTest(ref=ref, element=n, face=face):
                        self.assertEqual(buried(supplied['elements'], n, face, covers), 0)

    def test_art_check_model_rules_pass(self):
        art_check = art_check_or_skip(self)
        for ref in APPLICATORS:
            with self.subTest(ref):
                elements, lookup, _ = art_check._resolve(read(RUNTIME / f'{ref}.json'), (art_check.ASSETS,))
                faces, raw = art_check._model_faces(elements, lookup)
                self.assertEqual([uv for uv in raw if min(uv) < -1e-3 or max(uv) > 16 + 1e-3], [], 'U1')
                flicker = []
                for i, j, gap, overlap, u, v, n, d in art_check._pairs(faces, art_check.N1_GAP):
                    if gap == 0 and art_check._layered(elements, faces[i], faces[j]):
                        continue
                    if art_check._differ(faces[i], faces[j], overlap, u, v, n, d):
                        flicker.append((faces[i].label, faces[j].label, gap))
                self.assertEqual(flicker, [], 'Z1/N1')
                self.assertEqual(art_check._open_faces(elements, faces), [], 'O1')


if __name__ == '__main__':
    unittest.main()
