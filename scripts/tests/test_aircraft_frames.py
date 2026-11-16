"""Aircraft seats, muzzles, turrets and the hot-air balloon's lean agree with the drawn models (November 13 batch).

Each check reads the Java source, recognises the transform it uses (the form before the fix or after it; anything else
fails, so this mirror cannot drift), mirrors it in plain Python, and compares it with where the client draws the
supplied meshes (src/main/resources/assets/jugcraft/aircraft_models.json) by AircraftRenderer's arithmetic, mirrored in
test_aircraft_banking.py. The negative-control tests keep the defects found on November 13 demonstrable after the fix.

Conventions:
- The renderer turns the body by JOML Ry(-yaw) Rx(pitch) Rz(bank) about the model origin lifted by the ground offset,
  and each supplied group by Rz Ry Rx of its rest plus animated angles (AircraftAttitude). The owner-supplied original
  (Immersive Aircraft's VehicleEntity.getVehicleTransform, read from the supplied class file) places seats and muzzles
  through the same Ry(-yaw) Rx(pitch) Rz(roll) matrix.
- Minecraft's Vec3.xRot(a) is mirrored as Mojang's mapped source gives it: y' = y cos a + z sin a, z' = z cos a - y sin a,
  that is Rx(-a); Vec3.yRot(a) is Ry(+a). No Minecraft jar is in this environment: the server game test
  AircraftFrameGameTests checks this against Vec3.directionFromRotation at run time.
- A positive aim yaw is to the pilot's right and a negative aim pitch is up, like an entity's rotation; the supplied
  RotationalManager computes yaw = -atan2(x, z) and pitch = -atan2(y, horizontal) in the craft's frame, and the
  supplied WarshipEntity sets turret_yaw = -yaw and turret_pitch = -pitch.

The server game test AircraftFrameGameTests checks the same things with the real flight and firing code. Neither is
in-game visual confirmation.
"""
import math
import re
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import test_aircraft_banking as banking  # noqa: E402

ROOT = banking.ROOT
MODELS = banking.MODELS
CRAFT = banking.CRAFT
JAVA = banking.JAVA
CLIENT = banking.CLIENT
DEG = banking.DEG
YAWS = banking.YAWS
_mul, _rx, _ry, _rz, _translate, _apply, _dot = (banking._mul, banking._rx, banking._ry, banking._rz, banking._translate,
                                                 banking._apply, banking._dot)
IDENTITY = _translate(0, 0, 0)


def _flat(text):
    return re.sub(r'\s+', '', text)


def _source(path):
    return _flat(path.read_text(encoding='utf-8'))


def _sub(a, b):
    return tuple(x - y for x, y in zip(a, b))


def _add(a, b):
    return tuple(x + y for x, y in zip(a, b))


def _scale(a, k):
    return tuple(x * k for x in a)


def _length(a):
    return math.sqrt(_dot(a, a))


def _unit(a):
    n = _length(a)
    return tuple(x / n for x in a)


def _angle(a, b):
    return math.degrees(math.acos(max(-1.0, min(1.0, _dot(_unit(a), _unit(b))))))


def _rotate(m, v):
    """A direction through a matrix (no translation)."""
    return tuple(m[i][0] * v[0] + m[i][1] * v[1] + m[i][2] * v[2] for i in range(3))


def _transpose3(m):
    out = _translate(0, 0, 0)
    for i in range(3):
        for j in range(3):
            out[i][j] = m[j][i]
    return out


# ---- Minecraft's Vec3 rotations (Mojang's mapped source; checked at run time by AircraftFrameGameTests) ----
def vec3_xrot(v, a):
    c, s = math.cos(a), math.sin(a)
    return (v[0], v[1] * c + v[2] * s, v[2] * c - v[1] * s)


def vec3_yrot(v, a):
    c, s = math.cos(a), math.sin(a)
    return (v[0] * c + v[2] * s, v[1], v[2] * c - v[0] * s)


def view_vector(pitch, yaw):
    """Entity.calculateViewVector / Vec3.directionFromRotation."""
    f, g = pitch * DEG, -yaw * DEG
    return (math.sin(g) * math.cos(f), -math.sin(f), math.cos(g) * math.cos(f))


# ---- the renderer's frames ----
def body(yaw, pitch, bank):
    return _mul(_ry(-yaw), _mul(_rx(pitch), _rz(bank)))


def drawn(name, yaw, pitch, bank, point):
    """Where AircraftRenderer draws a point of the model (blocks from its origin), relative to the entity."""
    return _apply(_mul(_translate(0, banking.ground(name), 0), body(yaw, pitch, bank)), point)


def node_frames(nodes, base, variables):
    """{path: matrix} for every group of a supplied model, walked as AircraftRenderer walks it."""
    out = {}

    def animated(node, key):
        a = (node.get('animation') or {}).get(key) or {}
        return [expression(str(a[k]), variables) if k in a else 0.0 for k in 'xyz']

    def walk(children, parent, path):
        for n in children:
            p, r, o, rest = animated(n, 'position'), animated(n, 'rotation'), n['offset'], n['rotation']
            m = _mul(parent, _translate((o[0] + p[0]) / 16, (o[1] + p[1]) / 16, (o[2] + p[2]) / 16))
            m = _mul(_mul(_mul(m, _rz(rest[2] + r[2])), _ry(rest[1] + r[1])), _rx(rest[0] + r[0]))
            here = path + (n['name'],)
            out.setdefault(here, m)
            walk(n['children'], m, here)

    walk(nodes, base, ())
    return out


def vertices(nodes, base, variables, inside=None):
    """(local point, world point) of the vertices of groups whose path contains {@code inside} (every group if None)."""
    frames = node_frames(nodes, base, variables)
    out = []

    def walk(children, path):
        for n in children:
            here = path + (n['name'],)
            if inside is None or inside in here:
                for q in n['quads']:
                    for v in q['vertices']:
                        out.append((tuple(c / 16 for c in v[:3]), _apply(frames[here], (v[0] / 16, v[1] / 16, v[2] / 16))))
            walk(n['children'], here)

    walk(nodes, ())
    return out


# ---- the expression variables the aim nodes read, from the current AircraftAttitude.expression ----
def aim_mapping():
    """{variable: (sign, 'aimYaw' | 'aimPitch')} from the switch in AircraftAttitude.expression."""
    text = _source(JAVA / 'AircraftAttitude.java')
    mapping = {}
    for labels, expr in re.findall(r'case((?:"[a-z_]+",?)+)->([^;]+);', text):
        m = re.fullmatch(r'(-?)c\.(aimYaw|aimPitch)\(\)', expr)
        if m:
            for label in re.findall(r'"([a-z_]+)"', labels):
                mapping[label] = (-1 if m.group(1) else 1, m.group(2))
    for needed in ('yaw', 'pitch', 'turret_yaw', 'turret_pitch'):
        if needed not in mapping:
            raise AssertionError(f'AircraftAttitude.expression has no recognised case for {needed}')
    return mapping


def expression(raw, variables):
    e = re.sub(r'\s+', '', raw.replace('variable.', ''))
    if not e:
        return 0.0
    sign = 1
    if e.startswith('-'):
        sign, e = -1, e[1:]
    parts = e.split('*')
    if parts[0] in variables:
        base = variables[parts[0]]
    else:
        try:
            base = float(parts[0])
        except ValueError:
            base = 0.0
    if len(parts) > 1:
        base *= float(parts[1])
    return sign * base


def aim_variables(aim_yaw, aim_pitch, bank=0.0, mapping=None):
    mapping = mapping or aim_mapping()
    values = {'aimYaw': aim_yaw, 'aimPitch': aim_pitch}
    out = {k: sign * values[v] for k, (sign, v) in mapping.items()}
    out.update({'roll': bank, 'turret_cooldown': 1.0, '(1-turret_cooldown)': 0.0})
    return out


# ---- the seat and muzzle transforms, recognised from the current Aircraft.java ----
OLD_SEAT = 'returnspec.seat(getPassengers().indexOf(passenger),getPassengers().size()).xRot(getXRot()*Mth.DEG_TO_RAD).yRot(-getYRot()*Mth.DEG_TO_RAD).add(0,spec.groundOffset(),0);'
NEW_SEAT = 'returnAircraftAttitude.place(spec,getYRot(),getXRot(),bank(1),seat.x,seat.y,seat.z);'
OLD_MUZZLE = 'Vec3muzzle=position().add(0,spec.groundOffset(),0).add(newVec3(m.get("x").getAsDouble(),m.get("y").getAsDouble(),m.get("z").getAsDouble()).xRot(getXRot()*Mth.DEG_TO_RAD).yRot(-getYRot()*Mth.DEG_TO_RAD));'
NEW_MUZZLE = 'Vec3muzzle=position().add(AircraftAttitude.place(spec,yaw,pitch,bank,m.get("x").getAsDouble(),m.get("y").getAsDouble(),m.get("z").getAsDouble()));'
NEW_PLACE = ('publicstaticVec3place(AircraftSpecspec,floatyaw,floatpitch,floatbank,doublex,doubley,doublez){returnturn(yaw,pitch,bank,x,y,z).add(0,spec.groundOffset(),0);}',
             'publicstaticVec3turn(floatyaw,floatpitch,floatbank,doublex,doubley,doublez){Vector3fv=body(yaw,pitch,bank).transform(newVector3f((float)x,(float)y,(float)z));returnnewVec3(v.x,v.y,v.z);}')


def old_place(name, yaw, pitch, bank, point):
    """Vec3.xRot(pitch).yRot(-yaw), lifted by the ground offset; no bank."""
    p = vec3_yrot(vec3_xrot(point, pitch * DEG), -yaw * DEG)
    return (p[0], p[1] + banking.ground(name), p[2])


def new_place(name, yaw, pitch, bank, point):
    return drawn(name, yaw, pitch, bank, point)


def current(path, old, new, old_mirror, new_mirror, extra=()):
    text = _source(path)
    if new in text and all(e in _source(JAVA / 'AircraftAttitude.java') for e in extra):
        return new_mirror
    if old in text:
        return old_mirror
    raise AssertionError(f'{path.name}: neither the known form before the November 13 fix nor after it')


def seat_mirror():
    return current(JAVA / 'Aircraft.java', OLD_SEAT, NEW_SEAT, old_place, new_place, NEW_PLACE)


def muzzle_mirror():
    return current(JAVA / 'Aircraft.java', OLD_MUZZLE, NEW_MUZZLE, old_place, new_place, NEW_PLACE)


def craft_bank(name):
    return banking.bank(name, -1, 1)   # a right turn at full power


# ---- the turret aim the server sends, recognised from the current Aircraft.java ----
OLD_AIM = ('entityData.set(AIM_YAW,pilot==null?0F:Mth.wrapDegrees(pilot.getYRot()-getYRot()));'
           'entityData.set(AIM_PITCH,pilot==null?0F:Mth.clamp(pilot.getXRot()-getXRot(),-80,80));')
NEW_AIM = ('float[]aim=pilot==null?newfloat[2]:AircraftAttitude.aim(getYRot(),getXRot(),bank(1),pilot.getLookAngle());'
           'entityData.set(AIM_YAW,Mth.wrapDegrees(aim[0]));entityData.set(AIM_PITCH,Mth.clamp(aim[1],-80,80));')
NEW_AIM_BODY = ('Vector3flocal=body(yaw,pitch,bank).conjugate().transform(newVector3f((float)look.x,(float)look.y,(float)look.z));'
                'returnnewfloat[]{(float)Math.toDegrees(-Math.atan2(local.x,local.z)),(float)Math.toDegrees(-Math.atan2(local.y,Math.sqrt(local.x*local.x+local.z*local.z)))};')


def old_aim(yaw, pitch, bank, look_pitch, look_yaw):
    return ((look_yaw - yaw + 180) % 360 - 180, max(-80, min(80, look_pitch - pitch)))


def new_aim(yaw, pitch, bank, look_pitch, look_yaw):
    local = _rotate(_transpose3(body(yaw, pitch, bank)), view_vector(look_pitch, look_yaw))
    return (-math.degrees(math.atan2(local[0], local[2])),
            max(-80, min(80, -math.degrees(math.atan2(local[1], math.hypot(local[0], local[2]))))))


def aim_mirror():
    return current(JAVA / 'Aircraft.java', OLD_AIM, NEW_AIM, old_aim, new_aim, (NEW_AIM_BODY,))


# ---- where shots leave, recognised from the current Aircraft.java ----
OLD_ROUND = 'round.setPos(muzzle.add(direction));'
NEW_ROUND = ('varbore=AircraftAttitude.cannonBore(aimYaw(),aimPitch());'
             'round.setPos(muzzle.add(AircraftAttitude.turn(yaw,pitch,bank,bore.x,bore.y,bore.z)).add(direction.scale(AircraftAttitude.CANNON_BARREL)));')
NEW_BORE = ('privatestaticfinalfloat[]CANNON_YAW={0,9,-5.5F},CANNON_PITCH={0,3,.5F},CANNON_ROLL={0,.5F,-.5F};',
            'publicstaticfinaldoubleCANNON_BARREL=1;',
            'Vector3froll=newVector3f(CANNON_ROLL[0],CANNON_ROLL[1],CANNON_ROLL[2]);'
            'newQuaternionf().rotationX(aimPitch*Mth.DEG_TO_RAD).transform(roll);'
            'roll.add(CANNON_PITCH[0],CANNON_PITCH[1],CANNON_PITCH[2]);'
            'newQuaternionf().rotationY(-aimYaw*Mth.DEG_TO_RAD).transform(roll);'
            'returnroll.add(CANNON_YAW[0],CANNON_YAW[1],CANNON_YAW[2]).div(16);')
OLD_BOLT = 'Vec3from=muzzle.add(direction.scale(1.25));'
NEW_BOLT = 'Vec3from=muzzle.add(AircraftAttitude.turn(yaw,pitch,bank,0,AircraftAttitude.CROSSBOW_BOLT,0)).add(direction.scale(1.25));'
NEW_BOLT_HEIGHT = ('publicstaticfinaldoubleCROSSBOW_BOLT=5/16.0;',)


def old_round(name, yaw, pitch, bank, aim, mount, direction):
    return _add(old_place(name, yaw, pitch, bank, mount), direction)


def new_round(name, yaw, pitch, bank, aim, mount, direction):
    roll = _apply(_rx(aim[1]), (0, .5, -.5))
    bore = _add(_apply(_ry(-aim[0]), _add(roll, (0, 3, .5))), (0, 9, -5.5))
    bore = _scale(bore, 1 / 16)
    return _add(_add(new_place(name, yaw, pitch, bank, mount), _rotate(body(yaw, pitch, bank), bore)), direction)


def round_mirror():
    return current(JAVA / 'Aircraft.java', OLD_ROUND, NEW_ROUND, old_round, new_round, NEW_BORE + NEW_PLACE)


def old_bolt(name, yaw, pitch, bank, mount, direction):
    return _add(old_place(name, yaw, pitch, bank, mount), _scale(direction, 1.25))


def new_bolt(name, yaw, pitch, bank, mount, direction):
    lift = _rotate(body(yaw, pitch, bank), (0, 5 / 16, 0))
    return _add(_add(new_place(name, yaw, pitch, bank, mount), lift), _scale(direction, 1.25))


def bolt_mirror():
    return current(JAVA / 'Aircraft.java', OLD_BOLT, NEW_BOLT, old_bolt, new_bolt, NEW_BOLT_HEIGHT + NEW_PLACE)


# ---- the cannon's shot direction, recognised from the current Aircraft.java (November 13 follow-up) ----
# Aircraft.tick clamps the synced aim pitch to +-80 degrees in the body's frame; the drawn barrel and the bore read the
# clamped aim. Before the follow-up the round flew along the pilot's look; after it, along the drawn barrel.
OLD_SHOT = 'Vec3direction=pilot.getLookAngle();varround=newAircraftBullet(level,pilot);'
NEW_SHOT = ('varbarrel=AircraftAttitude.cannonDirection(aimYaw(),aimPitch());'
            'Vec3direction=AircraftAttitude.turn(yaw,pitch,bank,barrel.x,barrel.y,barrel.z);varround=newAircraftBullet(level,pilot);')
NEW_SHOT_BODY = ('publicstaticVector3fcannonDirection(floataimYaw,floataimPitch){returnnewQuaternionf().rotationY(-aimYaw*Mth.DEG_TO_RAD)'
                 '.rotateX(aimPitch*Mth.DEG_TO_RAD).transform(newVector3f(0,0,1));}',)
AIM_CLAMP = 'entityData.set(AIM_PITCH,Mth.clamp(aim[1],-80,80));'
CROSSBOW_LOOK = ('Vec3direction=pilot.getLookAngle();Vec3from=muzzle.add(AircraftAttitude.turn(yaw,pitch,bank,0,AircraftAttitude.CROSSBOW_BOLT,0)).add(direction.scale(1.25));',
                 'arrow.shoot(direction.x,direction.y+.1,direction.z,3,0);')
FIRES = 'if(!bomb&&!crossbow&&!weapon.equals("rotary_cannon"))continue;'


def old_shot(yaw, pitch, bank, aim, look):
    return look


def new_shot(yaw, pitch, bank, aim, look):
    return _rotate(body(yaw, pitch, bank), _rotate(_mul(_ry(-aim[0]), _rx(aim[1])), (0, 0, 1)))


def shot_mirror():
    return current(JAVA / 'Aircraft.java', OLD_SHOT, NEW_SHOT, old_shot, new_shot, NEW_SHOT_BODY)


# Body-frame aim pitches (negative is up) around the +-80 clamp, and the attitudes and aim yaws they are tried at.
LIMIT_PITCHES = (-89, -85, -81, -80.5, -79.5, -79, -60, 60, 79, 79.5, 80.5, 81, 85, 89)
LIMIT_ATTITUDES = tuple((yaw, pitch, bank) for yaw in (0, 37.5, -90) for pitch in (-18, 0, 18) for bank in (-18, 0, 18))
LIMIT_AIM_YAWS = (-120, -30, 0, 45, 160)


def limit_cases():
    """(yaw, pitch, bank, aim yaw, body pitch, look pitch, look yaw): a look at a given aim in the body's frame, as the
    pilot's own pitch and yaw (vanilla's view vector convention)."""
    for yaw, pitch, bank in LIMIT_ATTITUDES:
        for aim_yaw in LIMIT_AIM_YAWS:
            for aim_pitch in LIMIT_PITCHES:
                world = _rotate(body(yaw, pitch, bank), view_vector(aim_pitch, aim_yaw))
                look_pitch = -math.degrees(math.asin(max(-1.0, min(1.0, world[1]))))
                look_yaw = math.degrees(math.atan2(-world[0], world[2]))
                yield yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw


def drawn_barrel(name, yaw, pitch, bank, aim):
    """The drawn rotary cannon's or telescope's barrel direction and muzzle, for a weapon at the model origin."""
    frames = node_frames(MODELS[name], body(yaw, pitch, bank), aim_variables(aim[0], aim[1], bank))
    roll = frames[('yaw', 'pitch', 'roll')]
    return _unit(_rotate(roll, (0, 0, 1))), _apply(roll, (0, 0, 1))


def flight_pitches(name):
    return (-18.0, 18.0)


# ---- the hot-air balloon's envelope, recognised from the current HotAirBalloonRenderer ----
THROAT = 3.25
ENVELOPE_TOP = THROAT + 11.0
OLD_BALLOON = ('pose.rotateDegrees(Axis.YP,180.0F-state.yaw);',
               'pose.translate(0.0F,(float)HotAirBalloon.THROAT,0.0F);pose.rotateDegrees(Axis.XP,state.swayX);'
               'pose.rotateDegrees(Axis.ZP,state.swayZ);pose.translate(0.0F,(float)-HotAirBalloon.THROAT,0.0F);',
               'state.swayX=0.8F*Mth.sin(now*0.031F)+(float)Mth.clamp(motion.z*20.0,-3.0,3.0);',
               'state.swayZ=0.8F*Mth.sin(now*0.023F+1.7F)-(float)Mth.clamp(motion.x*20.0,-3.0,3.0);')
NEW_BALLOON = ('pose.rotate(BalloonAttitude.basket(state.yaw));',
               'pose.translate(0.0F,(float)HotAirBalloon.THROAT,0.0F);pose.rotate(BalloonAttitude.envelope(state.yaw,state.swayX,state.swayZ));'
               'pose.translate(0.0F,(float)-HotAirBalloon.THROAT,0.0F);',
               'float[]lean=BalloonAttitude.lean(motion.x,motion.z);',
               'state.swayX=0.8F*Mth.sin(now*0.031F)+lean[0];', 'state.swayZ=0.8F*Mth.sin(now*0.023F+1.7F)+lean[1];')
NEW_BALLOON_ATTITUDE = ('returnnewfloat[]{(float)Mth.clamp(motionZ*LEAN,-MOST,MOST),(float)-Mth.clamp(motionX*LEAN,-MOST,MOST)};',
                        'returnnewQuaternionf().rotationY((180-yaw)*Mth.DEG_TO_RAD);',
                        'returnnewQuaternionf().rotationX(swayX*Mth.DEG_TO_RAD).rotateZ(swayZ*Mth.DEG_TO_RAD).rotateY((180-yaw)*Mth.DEG_TO_RAD);',
                        'publicstaticfinalfloatLEAN=20,MOST=3;')


def _lean(mx, mz):
    return (max(-3, min(3, mz * 20)), -max(-3, min(3, mx * 20)))


def old_envelope_top(yaw, mx, mz):
    sx, sz = _lean(mx, mz)
    m = _mul(_ry(180 - yaw), _mul(_translate(0, THROAT, 0), _mul(_rx(sx), _mul(_rz(sz), _translate(0, -THROAT, 0)))))
    return _apply(m, (0, ENVELOPE_TOP, 0))


def new_envelope_top(yaw, mx, mz):
    sx, sz = _lean(mx, mz)
    m = _mul(_translate(0, THROAT, 0), _mul(_rx(sx), _mul(_rz(sz), _mul(_ry(180 - yaw), _translate(0, -THROAT, 0)))))
    return _apply(m, (0, ENVELOPE_TOP, 0))


def balloon_mirror():
    renderer = _source(CLIENT / 'HotAirBalloonRenderer.java')
    if all(f in renderer for f in NEW_BALLOON):
        attitude = _source(JAVA / 'BalloonAttitude.java')
        if all(f in attitude for f in NEW_BALLOON_ATTITUDE):
            return new_envelope_top
    if all(f in renderer for f in OLD_BALLOON):
        return old_envelope_top
    raise AssertionError('HotAirBalloonRenderer: neither the known form before the November 13 fix nor after it')


DRIFTS = ((.15, 0), (-.15, 0), (0, .15), (0, -.15), (.1, -.1), (.04, .02))
BALLOON_YAWS = (0, 90, 180, -90, 37.5)


def balloon_errors(mirror):
    """[(yaw, drift, angle between the envelope top's lean and the drift, in degrees)]."""
    out = []
    for yaw in BALLOON_YAWS:
        rest = mirror(yaw, 0, 0)
        for mx, mz in DRIFTS:
            moved = _sub(mirror(yaw, mx, mz), rest)
            out.append((yaw, (mx, mz), _angle((moved[0], 0, moved[2]), (mx, 0, mz))))
    return out


class AircraftFrameTests(unittest.TestCase):
    # ---- (a) seats and muzzles ----
    def _seat_errors(self, mirror):
        worst = {}
        for name, craft in CRAFT.items():
            for layout in craft['passengerPositions']:
                for seat in layout:
                    point = (seat['x'], seat['y'], seat['z'])
                    for yaw in YAWS:
                        for pitch in flight_pitches(name):
                            for bank in (0.0, craft_bank(name), -craft_bank(name)):
                                error = _length(_sub(mirror(name, yaw, pitch, bank, point), drawn(name, yaw, pitch, bank, point)))
                                worst[name] = max(worst.get(name, 0.0), error)
        return worst

    def test_seats_sit_where_the_model_is_drawn(self):
        worst = self._seat_errors(seat_mirror())
        self.assertTrue(all(e < 1e-6 for e in worst.values()), f'seat attachment off the drawn seat, worst per craft (blocks): {worst}')

    def test_mounts_are_where_the_model_is_drawn(self):
        mirror, worst = muzzle_mirror(), {}
        for name, craft in CRAFT.items():
            for group in craft['weaponMounts']:
                for kind in ('ROTATING', 'FRONT', 'DROP'):
                    for m in group.get(kind, []):
                        point = (m['x'], m['y'], m['z'])
                        for yaw in YAWS:
                            for pitch in flight_pitches(name):
                                for bank in (0.0, craft_bank(name)):
                                    error = _length(_sub(mirror(name, yaw, pitch, bank, point), drawn(name, yaw, pitch, bank, point)))
                                    worst[name] = max(worst.get(name, 0.0), error)
        self.assertTrue(all(e < 1e-6 for e in worst.values()), f'weapon mount off the drawn mount, worst per craft (blocks): {worst}')

    def test_vec3_xrot_turns_against_the_renderers_pitch(self):
        """Negative control for the seats and mounts before November 13: Vec3.xRot(a) is Rx(-a), so at 18 degrees of
        pitch the old placement misses the drawn seat by up to 0.3 blocks (biplane) and the bamboo hopper's front
        weapon mounts by about a block."""
        self.assertLess(_length(_sub(vec3_yrot(vec3_xrot((0, 0, 1), -20 * DEG), -30 * DEG), view_vector(20, 30))), 1e-9)
        worst = self._seat_errors(old_place)
        self.assertGreater(worst['biplane'], .25, worst)
        self.assertGreater(worst['bamboo_hopper'], .55, worst)
        hopper_front = (-5.0, 1.5, 1.5)
        self.assertGreater(_length(_sub(old_place('bamboo_hopper', 0, 18, 0, hopper_front), drawn('bamboo_hopper', 0, 18, 0, hopper_front))), .9)

    # ---- (b) turrets ----
    def _barrel(self, name, aim_yaw, aim_pitch, mapping=None):
        """The drawn barrel direction of a weapon model in its mount's frame, and its muzzle point."""
        variables = aim_variables(aim_yaw, aim_pitch, 0.0, mapping)
        frames = node_frames(MODELS[name], IDENTITY, variables)
        roll = frames[('yaw', 'pitch', 'roll')]
        return _unit(_rotate(roll, (0, 0, 1))), _apply(roll, (0, 0, 1))

    def _turret_errors(self, mapping=None):
        out = []
        for name in ('rotary_cannon', 'telescope'):
            for aim_yaw in (-60, -20, 20, 60):
                for aim_pitch in (-30, 0, 30):
                    direction, _ = self._barrel(name, aim_yaw, aim_pitch, mapping)
                    out.append((name, aim_yaw, aim_pitch, _angle(direction, view_vector(aim_pitch, aim_yaw))))
        return out

    def test_rotary_cannon_and_telescope_point_where_the_pilot_looks(self):
        # Their barrels and the telescope's wide objective lie along the roll node's +Z.
        cannon = vertices(MODELS['rotary_cannon'], IDENTITY, aim_variables(0, 0), 'roll')
        self.assertGreater(max(local[2] for local, _ in cannon), .99)
        bad = [e for e in self._turret_errors() if e[3] > .5]
        self.assertFalse(bad, f'(weapon, aim yaw, aim pitch, degrees off the look): {bad}')

    def _warship_turret(self, aim_yaw, aim_pitch, mapping=None):
        variables = aim_variables(aim_yaw, aim_pitch, 0.0, mapping)
        frames = node_frames(MODELS['warship'], IDENTITY, variables)
        bow = next(m for path, m in frames.items() if path[-1] == 'cross_bow')
        return _unit(_rotate(bow, (0, 0, -1)))

    def test_warship_crossbow_turns_and_tilts_toward_the_look(self):
        # The gondola is turned half round (rest Rz180 Rx180), so the turret's crossbow points along its local -Z.
        level = self._warship_turret(0, 0)
        self.assertGreater(level[2], .99)
        for aim_yaw, aim_pitch in ((40, 0), (-40, 0), (0, -25), (0, 25), (30, -20)):
            d = self._warship_turret(aim_yaw, aim_pitch)
            look = view_vector(aim_pitch, aim_yaw)
            with self.subTest(aim_yaw=aim_yaw, aim_pitch=aim_pitch):
                if aim_yaw:
                    self.assertGreater(d[0] * look[0], 0, f'turns toward the look: barrel {d}, look {look}')
                if aim_pitch:
                    self.assertGreater(d[1] * look[1], 0, f'tilts toward the look: barrel {d}, look {look}')

    def test_old_aim_signs_mirror_the_cannon_and_tilt_the_warship_turret_away(self):
        """Negative control: before November 13, "yaw" read -aimYaw and "turret_pitch" +aimPitch."""
        old = {'yaw': (-1, 'aimYaw'), 'pitch': (1, 'aimPitch'), 'turret_yaw': (-1, 'aimYaw'), 'turret_pitch': (1, 'aimPitch')}
        self.assertTrue(all(e[3] > 30 for e in self._turret_errors(old) if e[1]), self._turret_errors(old))
        d = self._warship_turret(0, -25, old)
        self.assertLess(d[1], 0, 'looking up tilted the warship crossbow down')

    def _aimed_barrel_error(self, aim, name, yaw, pitch, bank, look_pitch, look_yaw):
        aim_yaw, aim_pitch = aim(yaw, pitch, bank, look_pitch, look_yaw)
        local, _ = self._barrel(name, aim_yaw, aim_pitch)
        return _angle(_rotate(body(yaw, pitch, bank), local), view_vector(look_pitch, look_yaw))

    def test_turret_aim_follows_the_look_when_the_craft_is_pitched_and_banked(self):
        aim, bad = aim_mirror(), []
        for yaw in YAWS:
            for pitch in (-18.0, 18.0):
                for bank in (-18.0, 0.0, 18.0):
                    for look_yaw, look_pitch in ((70, 0), (-70, 10), (30, -30), (150, 20)):
                        for weapon in ('rotary_cannon', 'telescope'):
                            e = self._aimed_barrel_error(aim, weapon, yaw, pitch, bank, look_pitch, yaw + look_yaw)
                            if e > .5:
                                bad.append((weapon, yaw, pitch, bank, look_yaw, look_pitch, round(e, 1)))
        self.assertFalse(bad[:8], f'{len(bad)} aims miss the look by over half a degree, e.g. {bad[:8]}')

    def test_relative_angles_miss_the_look_off_the_nose(self):
        """Negative control: subtracting the craft's yaw and pitch from the pilot's misses by up to the craft's pitch."""
        e = self._aimed_barrel_error(old_aim, 'rotary_cannon', 0, -18, 0, -18, 90)
        self.assertGreater(e, 15)

    # ---- shots leave the drawn muzzles ----
    def test_cannon_rounds_leave_the_drawn_barrels(self):
        mirror, aim, bad = round_mirror(), aim_mirror(), []
        for name, craft in CRAFT.items():
            for group in craft['weaponMounts']:
                for m in group.get('ROTATING', []):
                    mount = (m['x'], m['y'], m['z'])
                    for yaw in YAWS:
                        for pitch in flight_pitches(name):
                            bank = craft_bank(name)
                            look_pitch, look_yaw = pitch - 10, yaw + 35
                            aim_yaw, aim_pitch = aim(yaw, pitch, bank, look_pitch, look_yaw)
                            direction = shot_mirror()(yaw, pitch, bank, (aim_yaw, aim_pitch), view_vector(look_pitch, look_yaw))
                            spawn = mirror(name, yaw, pitch, bank, (aim_yaw, aim_pitch), mount, direction)
                            base = _mul(_mul(_translate(0, banking.ground(name), 0), body(yaw, pitch, bank)), _translate(*mount))
                            frames = node_frames(MODELS['rotary_cannon'], base, aim_variables(aim_yaw, aim_pitch, bank))
                            tip = _apply(frames[('yaw', 'pitch', 'roll')], (0, 0, 1))
                            if _length(_sub(spawn, tip)) > .02:
                                bad.append((name, yaw, pitch, round(_length(_sub(spawn, tip)), 2)))
        self.assertFalse(bad, f'(craft, yaw, pitch, blocks from the drawn barrels\' muzzle): {bad[:10]}')

    def test_crossbow_bolts_leave_along_the_drawn_stock(self):
        mirror, bad = bolt_mirror(), []
        stock = [p for p in vertices(MODELS['heavy_crossbow'], IDENTITY, {}) if abs(p[0][0]) <= 2 / 16]
        rest_height = max(local[1] for local, _ in stock)            # the stock's top, where a bolt lies: 5 px
        front = max(local[2] for local, _ in vertices(MODELS['heavy_crossbow'], IDENTITY, {}))
        for name, craft in CRAFT.items():
            for group in craft['weaponMounts']:
                for m in group.get('FRONT', []):
                    mount = (m['x'], m['y'], m['z'])
                    for yaw in YAWS:
                        for pitch in flight_pitches(name):
                            bank = craft_bank(name)
                            frame = _mul(_mul(_translate(0, banking.ground(name), 0), body(yaw, pitch, bank)), _translate(*mount))
                            forward = _rotate(frame, (0, 0, 1))
                            direction = view_vector(pitch, yaw)    # the pilot looks along the nose
                            spawn = mirror(name, yaw, pitch, bank, mount, direction)
                            local = _rotate(_transpose3(frame), _sub(spawn, _apply(frame, (0, 0, 0))))
                            off = math.hypot(local[0], local[1] - rest_height)
                            if off > .02 or local[2] <= front or _angle(direction, forward) > .5:
                                bad.append((name, yaw, pitch, round(off, 2), round(local[2], 2)))
        self.assertFalse(bad, f'(craft, yaw, pitch, blocks off the drawn bolt line, blocks ahead): {bad[:10]}')

    def test_old_rounds_left_below_the_drawn_cannon(self):
        """Negative control: before November 13 a round left from the mount's foot, about 0.8 blocks below the barrels."""
        mount, direction = (0, .9, -1.1875), (0, 0, 1)
        spawn = old_round('biplane', 0, 0, 0, (0, 0), mount, direction)
        tip = drawn('biplane', 0, 0, 0, _add(mount, (0, 12.5 / 16, 1 - 5.5 / 16)))
        self.assertGreater(_length(_sub(spawn, tip)), .75)

    def test_bombs_drop_from_below_the_drawn_bay(self):
        muzzle, bad = muzzle_mirror(), []
        bay = [local for local, _ in vertices(MODELS['bomb_bay'], IDENTITY, {})]
        for name, craft in CRAFT.items():
            for group in craft['weaponMounts']:
                for m in group.get('DROP', []):
                    mount = (m['x'], m['y'], m['z'])
                    for yaw in YAWS:
                        for pitch in flight_pitches(name):
                            frame = _mul(_mul(_translate(0, banking.ground(name), 0), body(yaw, pitch, 0)), _translate(*mount))
                            drawn_bay = [_apply(frame, p) for p in bay]
                            shell = _add(muzzle(name, yaw, pitch, 0, mount), (0, -.8, 0))   # Aircraft.fire: muzzle.add(0,-.8,0)
                            lowest = min(p[1] for p in drawn_bay)
                            xs, zs = [p[0] for p in drawn_bay], [p[2] for p in drawn_bay]
                            if not (shell[1] < lowest - .05 and min(xs) <= shell[0] <= max(xs) and min(zs) <= shell[2] <= max(zs)):
                                bad.append((name, yaw, pitch, round(shell[1] - lowest, 2)))
        self.assertFalse(bad, f'(craft, yaw, pitch, height above the drawn bay\'s bottom): {bad[:10]}')

    # ---- the +-80 degree aim clamp (November 13 follow-up) ----
    def _limit_disagreements(self, shot):
        """{body pitch: worst angle in degrees between the drawn cannon barrel and the round's direction}, and the same
        for the round's start against the drawn muzzle (blocks)."""
        aim, angles, starts = aim_mirror(), {}, {}
        for yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw in limit_cases():
            look = view_vector(look_pitch, look_yaw)
            clamped = aim(yaw, pitch, bank, look_pitch, look_yaw)
            barrel, muzzle = drawn_barrel('rotary_cannon', yaw, pitch, bank, clamped)
            direction = shot(yaw, pitch, bank, clamped, look)
            start = new_round('biplane', yaw, pitch, bank, clamped, (0, 0, 0), direction)
            start = _sub(start, (0, banking.ground('biplane'), 0))
            angles[aim_pitch] = max(angles.get(aim_pitch, 0.0), _angle(barrel, direction))
            starts[aim_pitch] = max(starts.get(aim_pitch, 0.0), _length(_sub(start, muzzle)))
        return angles, starts

    def test_aim_clamp_is_where_the_finding_says(self):
        self.assertIn(AIM_CLAMP, _source(JAVA / 'Aircraft.java'))
        aim = aim_mirror()
        for yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw in limit_cases():
            got = aim(yaw, pitch, bank, look_pitch, look_yaw)
            self.assertAlmostEqual(got[1], max(-80, min(80, aim_pitch)), places=6)

    def test_cannon_rounds_fly_along_the_drawn_barrel_at_the_aim_limit(self):
        angles, _ = self._limit_disagreements(shot_mirror())
        bad = {k: round(v, 2) for k, v in angles.items() if v > .01}
        self.assertFalse(bad, f'degrees between the drawn barrel and the round, by body-frame aim pitch: {bad}')

    def test_cannon_rounds_start_at_the_drawn_muzzle_at_the_aim_limit(self):
        _, starts = self._limit_disagreements(shot_mirror())
        bad = {k: round(v, 3) for k, v in starts.items() if v > 1e-6}
        self.assertFalse(bad, f'blocks between the drawn muzzle and the start of the round, by body-frame aim pitch: {bad}')

    def test_cannon_rounds_follow_the_look_within_the_aim_limit(self):
        """The clamp only matters past 80 degrees: inside it the drawn barrel, and so the round, is the pilot's look."""
        aim, shot = aim_mirror(), shot_mirror()
        for yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw in limit_cases():
            look = view_vector(look_pitch, look_yaw)
            direction = shot(yaw, pitch, bank, aim(yaw, pitch, bank, look_pitch, look_yaw), look)
            off = _angle(direction, look)
            if abs(aim_pitch) < 80:
                self.assertLess(off, .01, (yaw, pitch, bank, aim_yaw, aim_pitch))
            else:
                self.assertLess(off, abs(aim_pitch) - 80 + .01, (yaw, pitch, bank, aim_yaw, aim_pitch))

    def test_old_rounds_left_the_drawn_barrel_past_the_aim_limit(self):
        """Negative control: firing along the look, the round left the drawn barrel by up to the look's excess over 80."""
        angles, starts = self._limit_disagreements(old_shot)
        self.assertLess(max(v for k, v in angles.items() if abs(k) < 80), .01)
        self.assertGreater(angles[89], 8.9)
        self.assertGreater(angles[-81], .99)
        self.assertGreater(starts[89], .15)

    def test_telescope_draws_the_clamped_aim_and_fires_nothing(self):
        """The telescope fires nothing (Aircraft.fire skips it); its drawn objective follows the same clamped aim."""
        self.assertIn(FIRES, _source(JAVA / 'Aircraft.java'))
        aim = aim_mirror()
        for yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw in limit_cases():
            clamped = aim(yaw, pitch, bank, look_pitch, look_yaw)
            telescope, _ = drawn_barrel('telescope', yaw, pitch, bank, clamped)
            cannon, _ = drawn_barrel('rotary_cannon', yaw, pitch, bank, clamped)
            self.assertLess(_angle(telescope, cannon), .01)

    def test_crossbow_bolts_follow_the_pilots_look_by_design(self):
        """Pinned, not a defect: the feature record says weapons follow the pilot's aim, and the supplied crossbow has no
        aim joint, so the drawn crossbow is fixed and the bolt flies along the look, lifted 0.1, at any pitch. The
        angle between the drawn crossbow and the bolt is therefore the look's angle off the nose."""
        source = _source(JAVA / 'Aircraft.java')
        for line in CROSSBOW_LOOK:
            self.assertIn(line, source)
        self.assertFalse(any('yaw' in str(a) or 'pitch' in str(a) for n in MODELS['heavy_crossbow'] for a in (n.get('animation') or {}).values()))
        for yaw, pitch, bank, aim_yaw, aim_pitch, look_pitch, look_yaw in limit_cases():
            look = view_vector(look_pitch, look_yaw)
            bolt = _unit((look[0], look[1] + .1, look[2]))
            nose = _rotate(body(yaw, pitch, bank), (0, 0, 1))
            self.assertAlmostEqual(_angle(nose, bolt), _angle(nose, _add(look, (0, .1, 0))), places=6)

    # ---- (c) the hot-air balloon ----
    def test_balloon_envelope_leans_the_way_it_drifts_however_it_was_placed(self):
        bad = [e for e in balloon_errors(balloon_mirror()) if e[2] > 1]
        self.assertFalse(bad, f'(placement yaw, drift, degrees between the lean and the drift): {bad}')

    def test_old_balloon_lean_turned_with_the_basket(self):
        """Negative control: before November 13 the lean, worked out in the world's frame, was turned by the basket's
        facing: it followed the drift only when placed at yaw 180, trailed it at 0 and went sideways at 90."""
        errors = {(yaw, drift): angle for yaw, drift, angle in balloon_errors(old_envelope_top)}
        self.assertLess(errors[180, (.15, 0)], 1)
        self.assertGreater(errors[0, (.15, 0)], 179)
        self.assertGreater(errors[90, (.15, 0)], 89)


if __name__ == '__main__':
    unittest.main()
