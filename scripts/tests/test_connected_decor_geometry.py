"""Connected tables and fountains in the states the game produces, and the Christmas trees' left-out faces.

A sibling of test_original_geometry_holes.py (12 November): the same art_check O1 probe (covers, opacity, a grid just
outside each left-out face), applied where that test cannot reach.

- OwnerHomeBlock.joined sets each cardinal and each diagonal connection from its own neighbour cell, so all 256
  patterns of the eight neighbours occur. The supplied cozyhome blockstates assume the source's rule instead
  (AbstractHorizontalConnectingBlock.checkDiagonalNeighbor: a diagonal needs both of its cardinals), and their
  inner_corner_piece fill bared its sides and doubled the table top when a lone diagonal neighbour set it. Every
  pattern must now compose exactly the source state's parts and leave no left-out face open but the reviewed ones.
- Christmas tree bulb caps and the inner branch ring segment (rotated and zero-angle boxes, which the audit's O1 skips).
They need the full resources (models, blockstates, textures).
"""
from copy import deepcopy
from functools import lru_cache
from itertools import product
import json
from pathlib import Path
import sys
import unittest

import numpy as np

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import art_check
import newest_model_finishing as finishing
import newest_multipart_art as multipart

ASSETS = art_check.ASSETS
CARDINAL = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}
DIAGONAL = {'north_east': ('north', 'east'), 'north_west': ('north', 'west'),
            'south_east': ('south', 'east'), 'south_west': ('south', 'west')}
NEIGHBOURS = [CARDINAL[a] for a in CARDINAL] + [tuple(map(sum, zip(CARDINAL[a], CARDINAL[b]))) for a, b in DIAGONAL.values()]
FAMILIES = {name: sorted(p.stem for p in (ASSETS / 'blockstates').glob('homeworks_*_' + name + '.json'))
            for name in ('table', 'fountain')}
_POCKET = ("faces the pedestal pocket under the bowl beside a corner_piece: closed from outside by that piece's notch "
           "walls (elements 0 and 1), the base and the bowl; ray-checked with the ground below, never seen")
_NOTCH = ("the corner's pedestal box reaches over the concave corner's notch, which is open air walled by the "
          "corner_piece's notch walls; these faces lie on its block seam or over the base notch in that air")
# Left-out faces the probe still reaches in some reachable pattern: {(family, piece, element, face): reason}.
REVIEWED_OPEN = {
    ('fountain', 'corner', 3, 'east'): _NOTCH, ('fountain', 'corner', 3, 'south'): _NOTCH,
    ('fountain', 'corner', 3, 'down'): _NOTCH,
    ('fountain', 'double', 5, 'south'): _POCKET, ('fountain', 'middle', 1, 'east'): _POCKET,
    ('fountain', 'middle', 1, 'west'): _POCKET, ('fountain', 'side', 1, 'east'): _POCKET,
    ('fountain', 'side', 1, 'south'): _POCKET, ('fountain', 'side', 1, 'west'): _POCKET,
    ('fountain', 'inner_corner_piece', 0, 'north'): _POCKET, ('fountain', 'inner_corner_piece', 0, 'west'): _POCKET,
}
TREES = {relative: faces for relative, faces in finishing.CLOSE_FACES.items() if relative.startswith('holidayworks/')}


def read(path):
    return json.loads(Path(path).read_text(encoding='utf-8'))


def joined(cells, x, z):
    """OwnerHomeBlock.joined on this base: every connection from its own neighbour cell."""
    state = {side: str((x + dx, z + dz) in cells).lower() for side, (dx, dz) in CARDINAL.items()}
    for corner, (a, b) in DIAGONAL.items():
        dx, dz = (CARDINAL[a][0] + CARDINAL[b][0], CARDINAL[a][1] + CARDINAL[b][1])
        state[corner] = str((x + dx, z + dz) in cells).lower()
    return state


def source_state(state):
    """The same neighbours under cozyhome's rule: a diagonal also needs both of its cardinal connections."""
    return {key: ('true' if value == 'true' and all(state[side] == 'true' for side in DIAGONAL.get(key, ())) else
                  'false' if key in DIAGONAL else value) for key, value in state.items()}


def patterns():
    for present in product((False, True), repeat=8):
        yield frozenset([(0, 0)] + [cell for cell, on in zip(NEIGHBOURS, present) if on])


@lru_cache(maxsize=None)
def parts(blockstate):
    return tuple(json.dumps(p, sort_keys=True) for p in read(ASSETS / 'blockstates' / (blockstate + '.json'))['multipart']
                 if 'contains' not in p.get('when', {}))


def applied(blockstate, state):
    out = []
    for text in parts(blockstate):
        part = json.loads(text)
        if all(state[key] in str(value).split('|') for key, value in part.get('when', {}).items()):
            out.append(text)
    return out


@lru_cache(maxsize=None)
def piece(apply_text):
    """(turned elements, opaque indices, turned faces) of one applied model, as newest_multipart_art composes them."""
    apply = json.loads(apply_text)
    elements = multipart.geometry(apply)
    path = ASSETS / 'models' / (apply['model'].split(':', 1)[1] + '.json')
    _, lookup, _ = art_check._resolve(read(path), (ASSETS,))
    faces, _ = art_check._model_faces(elements, lookup)
    return elements, frozenset(multipart.opaque_elements(path)), faces


def compose(blockstate, cells):
    """[(cell, model, y, element index, from, to, faces, opaque)] and the drawn panels of a neighbourhood."""
    boxes, panels = [], []
    for cell in sorted(cells):
        for text in applied(blockstate, joined(cells, *cell)):
            apply = json.loads(text)['apply']
            elements, opaque, faces = piece(json.dumps(apply, sort_keys=True))
            shift = np.array([16 * cell[0], 0, 16 * cell[1]], float)
            for i, e in enumerate(elements):
                boxes.append((cell, apply['model'], apply.get('y', 0), i, np.array(e['from'], float) + shift,
                              np.array(e['to'], float) + shift, set(e.get('faces', {})), i in opaque))
            panels += [(cell, apply['model'], f, shift) for f in faces]
    return boxes, panels


def exposed(boxes, panels, grid=6):
    """Left-out faces of the centre cell's opaque boxes (seams included) that no box or opaque panel covers."""
    found = set()
    for k, (cell, model, y, i, lo, hi, faces, opaque) in enumerate(boxes):
        if cell != (0, 0) or not opaque or len(faces) == 6 or np.any(hi - lo <= 1e-6):
            continue
        for face, normal in art_check._NORMAL.items():
            if face in faces:
                continue
            axis, sign = art_check._AXIS[face], sum(normal)
            plane = hi[axis] if sign > 0 else lo[axis]
            others = [a for a in range(3) if a != axis]
            for a, b in product(range(grid), repeat=2):
                p = np.zeros(3)
                p[axis] = plane + sign * .01
                p[others[0]] = lo[others[0]] + (a + .5) / grid * (hi[others[0]] - lo[others[0]])
                p[others[1]] = lo[others[1]] + (b + .5) / grid * (hi[others[1]] - lo[others[1]])
                if any(j != k and np.all(blo - 1e-6 <= p) and np.all(p <= bhi + 1e-6)
                       for j, (_, _, _, _, blo, bhi, _, _) in enumerate(boxes)):
                    continue
                on = p.copy()
                on[axis] = plane
                if panel_covers(panels, on, normal, (cell, model, i)):
                    continue
                turned = face
                for _ in range(y // 90):
                    turned = {'east': 'north', 'south': 'east', 'west': 'south', 'north': 'west'}.get(turned, turned)
                found.add((model.rsplit('/', 1)[1], i, turned))
                break
    return found


def panel_covers(panels, point, normal, own):
    for cell, model, face, shift in panels:
        if (cell, model, face.label[0]) == own or art_check._dot(face.normal, normal) < 1 - 1e-6:
            continue
        local = tuple(point - shift)
        if abs(art_check._dot(art_check._sub(local, face.pts[0]), normal)) > 1e-6:
            continue
        lo, hi = np.min(face.pts, axis=0), np.max(face.pts, axis=0)
        if np.all(lo - 1e-6 <= local) and np.all(local <= hi + 1e-6):
            pixel = face.texel(local)
            if pixel is not None and pixel[3] == 255:
                return True
    return False


def signature(blockstate):
    """What decides a family's probe: its parts and, per applied model, the parents and opaque elements."""
    out = []
    for text in parts(blockstate):
        apply = json.loads(text)['apply']
        path = ASSETS / 'models' / (apply['model'].split(':', 1)[1] + '.json')
        out.append((json.loads(text).get('when'), apply.get('y', 0), read(path).get('parent'),
                    sorted(multipart.opaque_elements(path))))
    return json.dumps(out, sort_keys=True)


class ConnectedJoinTests(unittest.TestCase):
    def test_every_reachable_pattern_composes_the_source_state(self):
        self.assertEqual((15, 18), (len(FAMILIES['table']), len(FAMILIES['fountain'])))
        for blockstate in FAMILIES['table'] + FAMILIES['fountain']:
            with self.subTest(blockstate=blockstate):
                wrong = [sorted(c) for c in patterns()
                         if applied(blockstate, joined(c, 0, 0)) != applied(blockstate, source_state(joined(c, 0, 0)))]
                self.assertEqual([], wrong[:3], f'{len(wrong)} of 256 neighbour patterns add parts the source never does')

    def test_reachable_joins_leave_no_open_faces(self):
        """Probe one family per distinct signature (parts, parents, opaque elements) over all 256 patterns."""
        groups = {}
        for family, names in FAMILIES.items():
            for name in names:
                groups.setdefault((family, signature(name)), name)
        self.assertEqual(4, len(groups), 'tables: 11 woods, bamboo/undead, glass/iron shelf; fountains: one')
        open_by_family = {family: set() for family in FAMILIES}
        for (family, _), blockstate in sorted(groups.items(), key=lambda kv: kv[1]):
            seen = set()
            for cells in patterns():
                seen |= exposed(*compose(blockstate, cells))
            material = blockstate.removeprefix('homeworks_').removesuffix('_' + family)
            open_by_family[family] |= {(family, model.removeprefix(material + '_' + family).removeprefix('_') or 'single',
                                        i, face) for model, i, face in seen}
        for family, found in open_by_family.items():
            with self.subTest(family=family):
                self.assertEqual({key for key in REVIEWED_OPEN if key[0] == family}, found,
                                 'open faces must be exactly the reviewed ones')

    def test_reviewed_openings_are_explained(self):
        for key, reason in REVIEWED_OPEN.items():
            self.assertGreater(len(reason), 60, key)


def unturned(elements):
    elements = deepcopy(elements)
    for element in elements:
        rotation = element.get('rotation')
        if rotation is not None and abs(rotation.get('angle', 0)) < 1e-9:
            del element['rotation']
    return elements


def open_rotated_and_flat(elements, lookup):
    """O1 for every box, rotated ones in their own frame (covers: any element in its local box, as art_check)."""
    elements = unturned(elements)
    faces, _ = art_check._model_faces(elements, lookup)
    by = {}
    for f in faces:
        by.setdefault(f.label[0], []).append(f)
    frames = []
    for e in elements:
        turn, _ = art_check._turner(e.get('rotation'))
        origin = np.array(turn((0, 0, 0)))
        matrix = np.column_stack([np.array(turn(p)) - origin for p in ((1, 0, 0), (0, 1, 0), (0, 0, 1))])
        frames.append((np.linalg.inv(matrix), origin, turn))
    found = set()
    for i, e in enumerate(elements):
        f, t = e['from'], e['to']
        present = set(e.get('faces', {}))
        if not present or len(present) == 6 or any(t[k] - f[k] <= 1e-6 for k in range(3)) or not art_check._opaque(by.get(i, [])):
            continue
        turn = frames[i][2]
        for face, normal in art_check._NORMAL.items():
            if face in present:
                continue
            axis, sign = art_check._AXIS[face], sum(normal)
            plane = t[axis] if sign > 0 else f[axis]
            if 'rotation' not in e and (abs(plane) < 1e-6 or abs(plane - 16) < 1e-6 or (plane < 0 if sign < 0 else plane > 16)):
                continue  # art_check's seam rule for unturned boxes
            others = [k for k in range(3) if k != axis]
            for a, b in product(range(4), repeat=2):
                p = [0.0, 0.0, 0.0]
                p[axis] = plane + sign * .01
                p[others[0]] = f[others[0]] + (a + .5) / 4 * (t[others[0]] - f[others[0]])
                p[others[1]] = f[others[1]] + (b + .5) / 4 * (t[others[1]] - f[others[1]])
                world = np.array(turn(p))
                if not any(j != i and np.all(np.array(o['from']) - 1e-6 <= (inv @ (world - origin)))
                           and np.all((inv @ (world - origin)) <= np.array(o['to']) + 1e-6)
                           for j, (o, (inv, origin, _)) in enumerate(zip(elements, frames))):
                    found.add((i, face))
                    break
    return found


class ChristmasTreeTests(unittest.TestCase):
    def mesh(self, relative):
        path = ASSETS / 'models/newest' / (relative + '.json')
        model = read(path)
        if relative.startswith('holidayworks/block/parent/'):
            # The parents carry no textures; resolve them through a real decorated child (red), as placed.
            child = 'red_' + Path(relative).stem
            model = read(ASSETS / 'models/newest/holidayworks/block/christmas' / (child + '.json'))
        elements, lookup, used = art_check._resolve(model, (ASSETS,))
        drawn = {lookup('#' + key) for key in used if not key == 'particle'} - {None}
        self.assertTrue(drawn and all(p.exists() for p in drawn), relative)
        return elements, lookup

    def test_bulb_caps_and_inner_ring_are_closed(self):
        self.assertEqual(6, len(TREES))
        for relative, faces in TREES.items():
            with self.subTest(model=relative):
                elements, lookup = self.mesh(relative)
                self.assertEqual(set(), open_rotated_and_flat(elements, lookup))
                self.assertTrue(all(face in elements[i]['faces'] for i, face in faces))

    def test_caps_copy_their_own_opposite_face(self):
        opposite = dict(north='south', south='north', east='west', west='east')
        for relative, faces in TREES.items():
            elements, _ = self.mesh(relative)
            for i, face in faces:
                with self.subTest(model=relative, element=i):
                    self.assertIn(face, elements[i]['faces'])
                    spec = elements[i]['faces'][face]
                    if opposite[face] in elements[i]['faces']:
                        self.assertEqual(elements[i]['faces'][opposite[face]], spec)
                    else:  # the ring segment: a window of its own foliage faces
                        self.assertEqual('#3', spec['texture'])

    def test_every_tree_block_draws_these_meshes(self):
        sections = ('base', 'bottom', 'middle', 'top')
        for colour in ('blue', 'colorful', 'green', 'purple', 'red', 'yellow'):
            for section in sections:
                model = read(ASSETS / f'models/newest/holidayworks/block/christmas/{colour}_decorated_christmas_tree_{section}.json')
                self.assertEqual(f'jugcraft:newest/holidayworks/block/parent/decorated_christmas_tree_{section}', model['parent'])


if __name__ == '__main__':
    unittest.main()
