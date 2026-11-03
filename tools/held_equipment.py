"""Native hand poses for equipment whose authored grip differs from a vanilla sword.

Only the four hand display contexts are replaced. Original meshes, sprite pixels,
animation, item selectors and GUI/ground/frame poses remain the author's work.
Run directly for a bounded refresh, or through generate_material_data's final pass.
"""
from pathlib import Path
import json
import math

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/jugcraft"
HAND_CONTEXTS = {f"{view}_{hand}hand" for view in ("firstperson", "thirdperson") for hand in ("right", "left")}
SWORD_GRIP = (-4.5, -4.5, 0)
VANILLA = {
    "thirdperson": ((0, -90, 55), (0, 4, .5), .85),
    "firstperson": ((0, -90, 25), (1.13, 3.2, 1.13), .68),
}


def rotate(point, angles):
    """Minecraft ItemTransform's Quaternionf.rotationXYZ: Rx * Ry * Rz."""
    x, y, z = point
    rx, ry, rz = map(math.radians, angles)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    return (x, y, z)


def hand_pose(grip, third, first, mirror_aim=False, first_offset=(0, 0, 0)):
    """Place the authored grip at vanilla's hand contact, in model pixels from (8,8,8).

    Sprites keep vanilla's readable plane in either hand. Centred 3D tools mirror
    their aim across the player; Minecraft itself negates left-hand X and Y/Z angles.
    A small first-person carry offset can bring a long blade/readout into view.
    """
    display = {}
    for view, (angles, size) in (("thirdperson", third), ("firstperson", first)):
        base_angles, base_translation, base_size = VANILLA[view]
        contact = rotate(tuple(v * base_size for v in SWORD_GRIP), base_angles)
        offset = rotate(tuple(v * size for v in grip), angles)
        delta = tuple(a - b for a, b in zip(contact, offset))
        for left in (False, True):
            carry = first_offset if view == "firstperson" else (0, 0, 0)
            translation = [base_translation[i] + carry[i] + (-delta[i] if left and i == 0 and not mirror_aim else delta[i]) for i in range(3)]
            rotation = list(angles) if not left or mirror_aim else [angles[0], -angles[1], -angles[2]]
            display[f"{view}_{'left' if left else 'right'}hand"] = {
                "rotation": rotation, "translation": [round(v, 5) for v in translation], "scale": [size] * 3}
    return display


def sprite(grip, turn=0, first_offset=(0, 0, 0)):
    # Grip is measured on a 16-pixel version of the original sprite, x right/y down.
    return hand_pose((grip[0] - 8, 8 - grip[1], 0),
                     ((0, -90, 55 + turn), .85), ((0, -90, 25 + turn), .68), first_offset=first_offset)


def profiles():
    result = {}
    def item(name, pose): result[f"models/item/{name}.json"] = pose
    # Upright meshes have their working end on +Y, not +Z. Each rear grip is distinct.
    for name, grip in (("mining_drill", (0, -4.25, 1.25)), ("chainsaw", (0, -5.75, 0)),
                       ("leaf_blower", (0, -4.25, 1.25))):
        item(name, hand_pose(grip, ((0, 0, 0), .55), ((-20, -25, 15), .5), mirror_aim=True))
    # Both katana sprites run from a lower-left blade tip to an upper-right hilt.
    for name in ("power_katana", "ronin_katana"):
        item(name, sprite((13.375, 2.875), 180, first_offset=(-1, .5, 0)))
    for name in ("industrial_drill", "diamond_drill", "iridium_drill", "industrial_chainsaw"):
        item("jimta_" + name, sprite((6, 12)))
    # These imported sprites point upper-left, opposite vanilla's upper-right tool art.
    for name in ("electric_hoe", "electric_wrench", "electric_treetap"):
        item("jimta_" + name, sprite((12.5, 12.5), -90))
    # The manual versions have different art: an upright wrench and a horizontal tap.
    item("jimta_industrial_wrench", sprite((8, 12.5), -45))
    item("jimta_hand_treetap", sprite((5.5, 9.5), 45))
    item("jimta_mining_laser", sprite((12, 12.5), -90))
    # Readouts are held by their lower casing, upright instead of flat or by a corner.
    for name in ("crop_analyzer", "ore_scanner", "advanced_ore_scanner", "obscurator"):
        grip = (5.5, -6, 0) if name == "obscurator" else (0, -3.5, 0)
        carry = (0, 0, 0) if name == "obscurator" else (-1, 2, 0)
        item("jimta_" + name, hand_pose(grip, ((90, 0, 0), .55), ((0, -90, 0), .55), first_offset=carry))
    # Imported AE meshes point down -Z and have a slanted grip, unlike the upright tools.
    grip = rotate((0, -2.5, 1), (-22.5, 0, 0))
    grip = (grip[0], grip[1] - 3, grip[2] + 2)
    pose = hand_pose(grip, ((90, 0, 0), .6), ((0, -15, 0), .8), mirror_aim=True)
    for name in ("color_applicator", "color_applicator_colored", "matter_cannon", "network_tool"):
        result[f"models/jimtafacts/ae2/item/{name}.json"] = pose
    return result


def generate(assets=ASSETS):
    """Idempotent final compatibility pass after shared and imported model generation."""
    changed = []
    for relative, hands in profiles().items():
        path = assets / relative
        model = json.loads(path.read_text(encoding="utf-8"))
        old = json.loads(json.dumps(model))
        model.setdefault("display", {}).update(hands)
        if model != old:
            path.write_text(json.dumps(model, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        if model != old:
            changed.append(relative)
    return changed


if __name__ == "__main__":
    print(f"Updated {len(generate())} equipment hand models")
