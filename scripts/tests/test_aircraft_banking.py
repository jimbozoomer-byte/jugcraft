"""Aircraft bank into their turns (docs/TODO.md, "Aircraft banking is still reversed").

Mirrors, in plain Python over the real supplied meshes (src/main/resources/assets/jugcraft/aircraft_models.json), the
arithmetic the client draws aircraft with: AircraftAttitude's bank, body turn, part turns and expression variables,
and AircraftRenderer's walk over the parts. The source-contract test fails if any mirrored Java expression changes,
so the mirror cannot silently drift. The server game test AircraftBankingGameTests runs the same checks against the
real Java code and flight model; this one runs without Minecraft. Neither is in-game visual confirmation.
"""
import json
import math
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODELS = json.loads((ROOT / 'src/main/resources/assets/jugcraft/aircraft_models.json').read_text(encoding='utf-8'))
CRAFT = json.loads((ROOT / 'src/main/resources/jugcraft/aircraft.json').read_text(encoding='utf-8'))['craft']
JAVA = ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/aircraft'
CLIENT = ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client'
YAWS = (0, 90, 180, -90, 37.5)
DEG = math.pi / 180
SIDE = re.compile(r'(^|_)(left|right)(_|$|\d)')


def _mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(4)) for j in range(4)] for i in range(4)]


def _matrix(rows):
    return [list(r) + [0.0] for r in rows] + [[0.0, 0.0, 0.0, 1.0]]


def _rx(d):
    c, s = math.cos(d * DEG), math.sin(d * DEG)
    return _matrix(((1, 0, 0), (0, c, -s), (0, s, c)))


def _ry(d):
    c, s = math.cos(d * DEG), math.sin(d * DEG)
    return _matrix(((c, 0, s), (0, 1, 0), (-s, 0, c)))


def _rz(d):
    c, s = math.cos(d * DEG), math.sin(d * DEG)
    return _matrix(((c, -s, 0), (s, c, 0), (0, 0, 1)))


def _translate(x, y, z):
    m = _matrix(((1, 0, 0), (0, 1, 0), (0, 0, 1)))
    m[0][3], m[1][3], m[2][3] = x, y, z
    return m


def _apply(m, v):
    return tuple(m[i][0] * v[0] + m[i][1] * v[1] + m[i][2] * v[2] + m[i][3] for i in range(3))


def _dot(a, b):
    return sum(x * y for x, y in zip(a, b))


# ---- mirrored from AircraftAttitude.java (see test_source_contract) ----
def bank(name, turn, power, sign=-1):
    aeroplane = name in ('biplane', 'bamboo_hopper')
    return sign * turn * power * (18 if aeroplane else 2 if name == 'warship' else 5)


def expression(raw, c, balloon_sign=-1):
    e = re.sub(r'\s+', '', raw.replace('variable.', ''))
    if not e:
        return 0.0
    sign = 1
    if e.startswith('-'):
        sign, e = -1, e[1:]
    parts = e.split('*')
    table = {'engine_rotation': 0.0, 'pressing_interpolated_x': c['steering'], 'pressing_interpolated_y': 0.0,
             'pressing_interpolated_z': 1.0, 'balloon_pitch': c['pitch'] * .2, 'balloon_roll': balloon_sign * c['steering'] * 2,
             'turret_cooldown': 1.0, '(1-turret_cooldown)': 0.0, 'pitch': 0.0, 'turret_pitch': 0.0, 'yaw': 0.0,
             'turret_yaw': 0.0, 'roll': c['bank'], 'chest': 0.0}
    if parts[0] in table:
        base = table[parts[0]]
    else:
        try:
            base = float(parts[0])
        except ValueError:
            base = 0.0
    if len(parts) > 1:
        base *= float(parts[1])
    return sign * base


def ground(name):
    return {'airship': .6875, 'cargo_airship': .6875, 'biplane': .5, 'gyrodyne': .25, 'warship': .78125}.get(name, 0.0)


def draw(name, yaw, steering, pitch, roll, balloon_sign=-1):
    """[(part path, world point)] where AircraftRenderer puts each vertex: body Ry(-yaw) Rx(pitch) Rz(bank), then parts."""
    c = {'steering': steering, 'pitch': pitch, 'bank': roll}
    out = []

    def animated(node, key):
        a = (node.get('animation') or {}).get(key) or {}
        return [expression(str(a[k]), c, balloon_sign) if k in a else 0.0 for k in 'xyz']

    def walk(nodes, parent, path):
        for n in nodes:
            p, r, o, rest = animated(n, 'position'), animated(n, 'rotation'), n['offset'], n['rotation']
            m = _mul(parent, _translate((o[0] + p[0]) / 16, (o[1] + p[1]) / 16, (o[2] + p[2]) / 16))
            m = _mul(_mul(_mul(m, _rz(rest[2] + r[2])), _ry(rest[1] + r[1])), _rx(rest[0] + r[0]))
            here = path + (n['name'],)
            for q in n['quads']:
                for v in q['vertices']:
                    out.append((here, _apply(m, (v[0] / 16, v[1] / 16, v[2] / 16))))
            walk(n['children'], m, here)

    body = _mul(_translate(0, ground(name), 0), _mul(_ry(-yaw), _mul(_rx(pitch), _rz(roll))))
    walk(MODELS[name], body, ())
    return out


# ---- Minecraft conventions: Vec3.directionFromRotation / Entity.calculateViewVector ----
def heading(yaw):
    return (-math.sin(yaw * DEG), 0.0, math.cos(yaw * DEG))


def right(yaw):
    h = heading(yaw)
    return (-h[2], 0.0, h[0])     # heading x up: facing +Z the right hand is -X


def tip(points, side):
    d = [_dot(p, side) for _, p in points]
    cut = max(d) - (max(d) - min(d)) * .1
    return [i for i, x in enumerate(d) if x >= cut]


def mean(points, which):
    return tuple(sum(points[i][1][k] for i in which) / len(which) for k in range(3))


def rise(before, after, which, axis=(0, 1, 0)):
    return _dot(mean(after, which), axis) - _dot(mean(before, which), axis)


def camera(view_yaw):
    """Vanilla camera basis (rotationYXZ(pi - yaw, -pitch, 0), pitch 0 here): no roll in any view."""
    m = _ry(180 - view_yaw)
    return _apply(m, (0, 1, 0)), _apply(m, (1, 0, 0))


def _contains(path, text):
    return re.sub(r'\s+', '', text) in re.sub(r'\s+', '', path.read_text(encoding='utf-8'))


class AircraftBankingTests(unittest.TestCase):
    def test_source_contract(self):
        attitude = JAVA / 'AircraftAttitude.java'
        for expected in ('return -turn * power * (spec.aeroplane() ? 18F : spec.name().equals("warship") ? 2F : 5F);',
                         'return -turn * 2;',
                         'case "balloon_roll" -> balloonRoll(c.steering());',
                         'case "roll" -> c.bodyBank();',
                         'case "pressing_interpolated_x" -> c.steering();',
                         'new Quaternionf().rotationY(-yaw * Mth.DEG_TO_RAD).rotateX(pitch * Mth.DEG_TO_RAD).rotateZ(bank * Mth.DEG_TO_RAD)',
                         'new Quaternionf().rotationZ((rest[2] + animated[2]) * Mth.DEG_TO_RAD)'
                         '.rotateY((rest[1] + animated[1]) * Mth.DEG_TO_RAD).rotateX((rest[0] + animated[0]) * Mth.DEG_TO_RAD)'):
            self.assertTrue(_contains(attitude, expected), expected)
        self.assertTrue(_contains(JAVA / 'Aircraft.java', 'return AircraftAttitude.bank(spec,animatedControl(1,partial),power());'))
        self.assertTrue(_contains(JAVA / 'Aircraft.java', 'setYRot(getYRot()-(float)(t*stat("yawSpeed",4)*.3*power));'))
        self.assertTrue(_contains(CLIENT / 'AircraftClient.java', '(o.keyLeft.isDown()?1:0)-(o.keyRight.isDown()?1:0)'))
        renderer = CLIENT / 'AircraftRenderer.java'
        for expected in ('pose.translate(0,s.spec.groundOffset(),0);applyBodyOrientation(pose,s.yaw,s.pitch,s.bank);',
                         'pose.rotate(AircraftAttitude.body(yaw,pitch,bank));',
                         'pose.translate((n.offset()[0]+p[0])/16,(n.offset()[1]+p[1])/16,(n.offset()[2]+p[2])/16);',
                         'pose.rotate(AircraftAttitude.part(n.rotation(),r));',
                         's.turn=a.animatedControl(1,delta);', 's.bank=a.bank(delta);'):
            self.assertTrue(_contains(renderer, expected), expected)

    def test_supplied_meshes_face_plus_z_with_port_on_plus_x(self):
        for name, craft in CRAFT.items():
            layouts, nose = craft['passengerPositions'], False
            for layout in layouts[1:]:
                for seat in layout[1:]:
                    self.assertGreaterEqual(layout[0]['z'], seat['z'], name)
                    nose |= layout[0]['z'] > seat['z']
            if len(layouts) == 1:
                for mount in craft['weaponMounts']:
                    for front in mount.get('FRONT', []):
                        self.assertGreater(front['z'], layouts[0][0]['z'], name)
                        nose = True
            self.assertTrue(nose, name)
            rest = draw(name, 0, 0, 0, 0)
            sums = {}
            for path, p in rest:
                for depth in range(len(path), 0, -1):
                    m = SIDE.search(path[depth - 1])
                    if m:
                        s = sums.setdefault(('/'.join(path[:depth]), m.group(2)), [0.0, 0])
                        s[0] += p[0]
                        s[1] += 1
                        break
            agree = disagree = 0
            for (_, side), (x, n) in sums.items():
                x /= n
                if abs(x) >= 1 / 32:
                    agree, disagree = (agree + 1, disagree) if (side == 'left') == (x > 0) else (agree, disagree + 1)
            self.assertGreaterEqual(agree, 4 * disagree, f'{name}: {agree} agree, {disagree} disagree')
            if name in ('bamboo_hopper', 'cargo_airship', 'warship'):
                self.assertGreater(agree, 0, name)
            rudders = [i for i, (path, _) in enumerate(rest) if any(n == 'rudder' or n.endswith('_rudder') for n in path)]
            if rudders:
                self.assertLess(mean(rest, rudders)[2], 0, name)

    def _inside_tip_drops(self, bank_sign, balloon_sign):
        """{(craft, yaw, key): (inside rise, outside rise, on-screen check per view)}."""
        results = {}
        for name in CRAFT:
            pitch = 0.0 if name in ('biplane', 'bamboo_hopper') else -4.0   # forward held: Aircraft.tick's xRot
            for yaw in YAWS:
                level = draw(name, yaw, 0, pitch, 0, balloon_sign)
                r = right(yaw)
                right_tip, left_tip = tip(level, r), tip(level, tuple(-x for x in r))
                for key in (-1, 1):          # AircraftClient: -1 is the right key, +1 the left key
                    # Aircraft.tick: setYRot(getYRot() - t*yawSpeed*.3*power): the right key raises the yaw.
                    turned = yaw - key * 1.5
                    self.assertGreater(-key * _dot(heading(turned), right(yaw)), 0)
                    banked = draw(name, yaw, key, pitch, bank(name, key, 1, bank_sign), balloon_sign)
                    inside, outside = (right_tip, left_tip) if key < 0 else (left_tip, right_tip)
                    views = []
                    swing = tuple(a - b for a, b in zip(heading(turned), heading(yaw)))
                    for view in (yaw, yaw + 180):      # first person and F5 behind; F5 in front
                        up, screen_right = camera(view)
                        same_side = (_dot(mean(level, inside), screen_right) > 0) == (_dot(swing, screen_right) > 0)
                        views.append(same_side and rise(level, banked, inside, up) < -.02)
                    results[name, yaw, key] = (rise(level, banked, inside), rise(level, banked, outside), views)
        return results

    def test_right_turn_lowers_right_wingtip_on_every_aircraft_and_view(self):
        for (name, yaw, key), (inside, outside, views) in self._inside_tip_drops(-1, -1).items():
            with self.subTest(craft=name, yaw=yaw, turn='right' if key < 0 else 'left'):
                self.assertLess(inside, -.02)
                self.assertGreater(outside, .02)
                self.assertTrue(all(views), views)

    def test_october_26_signs_were_reversed(self):
        """Negative control: 46bac6b349's bank and balloon_roll signs (released in 20261029.1) fail every craft."""
        for (name, yaw, key), (inside, outside, views) in self._inside_tip_drops(1, 1).items():
            with self.subTest(craft=name, yaw=yaw, turn='right' if key < 0 else 'left'):
                self.assertGreater(inside, .02)
                self.assertFalse(any(views))

    def test_warship_envelope_adds_to_the_body_bank(self):
        envelope = next(n for n in MODELS['warship'] if n['name'] == 'envelope')
        self.assertEqual('variable.balloon_roll', envelope['animation']['rotation']['z'])
        roll = bank('warship', -1, 1)
        for yaw in YAWS:
            level = draw('warship', yaw, 0, 0, 0)
            right_tip = tip(level, right(yaw))
            body_only = rise(level, draw('warship', yaw, 0, 0, roll), right_tip)
            full = rise(level, draw('warship', yaw, -1, 0, roll), right_tip)
            self.assertLess(body_only, -.02)
            self.assertLess(full, body_only - .02)

    def test_rudders_and_control_sticks_deflect_toward_the_turn(self):
        checked = 0
        for name in CRAFT:
            for yaw in YAWS:
                level = draw(name, yaw, 0, 0, 0)
                names = {n for path, _ in level for n in path}
                for key in (-1, 1):
                    steered = draw(name, yaw, key, 0, 0)
                    toward = tuple(-key * x for x in right(yaw))
                    for part in sorted(n for n in names if n == 'rudder' or n.endswith('_rudder') or n == 'controller'):
                        direction = tuple(-x for x in heading(yaw)) if 'rudder' in part else (0, 1, 0)
                        members = [i for i, (path, _) in enumerate(level) if part in path]
                        far = max(_dot(level[i][1], direction) for i in members)
                        end = [i for i in members if _dot(level[i][1], direction) >= far - 1 / 64]
                        moved = _dot(tuple(a - b for a, b in zip(mean(steered, end), mean(level, end))), toward)
                        self.assertGreater(moved, .005, f'{name} {part} yaw {yaw} key {key}')
                        checked += 1
        self.assertEqual(checked, 5 * 2 * (2 + 1 + 3))   # bamboo hopper 2 rudders, biplane 1; three sticks


if __name__ == '__main__':
    unittest.main()
