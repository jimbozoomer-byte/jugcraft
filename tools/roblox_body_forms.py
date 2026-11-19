"""Create a wardrobe mannequin library from the owner's rigged Roblox avatar.

Run in Blender background mode on the rigged fitting copy, with the output
directory after --. The origin, body geometry and pivots belong to that source.
"""
import argparse
import json
import sys
from pathlib import Path

import bpy
import numpy as np
from mathutils import Vector

# Index zero always opens the exact imported body. Legs and waist describe
# independent changes to the standard cut and lean fit respectively.
FITS = (
    ("small_medium", "Small", "Medium", "Standard", "Standard"),
    ("small_small", "Small", "Small", "Standard", "Standard"),
    ("small_athletic", "Small", "Athletic", "Standard", "Standard"),
    ("small_full", "Small", "Full", "Standard", "Standard"),
    ("medium_medium", "Medium", "Medium", "Standard", "Standard"),
    ("medium_athletic", "Medium", "Athletic", "Standard", "Standard"),
    ("medium_full", "Medium", "Full", "Standard", "Standard"),
    ("medium_full_power", "Medium", "Full", "Power", "Standard"),
    ("large_medium", "Large", "Medium", "Standard", "Standard"),
    ("large_athletic_thick", "Large", "Athletic", "Thick", "Standard"),
    ("large_full_thick", "Large", "Full", "Thick", "Standard"),
    ("large_compact_lean", "Large", "Compact", "Lean", "Lean"),
    ("large_athletic", "Large", "Athletic", "Standard", "Standard"),
)
BODY = ("UpperTorso", "LowerTorso", *(
    side + limb for side in ("Left", "Right")
    for limb in ("UpperArm", "LowerArm", "Hand", "UpperLeg", "LowerLeg", "Foot")
))
RGBA = (210, 176, 150, 255)


def smooth(value):
    t = np.clip(value, 0, 1)
    return t*t*(3-2*t)


def bell(z, peak, breadth):
    return np.exp(-np.square((z-peak)/breadth))


def fit_positions(world, part, fit):
    """Offsets in source coordinates (+Y front, +Z up), never bone scale."""
    _, bust, seat, leg, waist = fit
    if part not in ("UpperTorso", "LowerTorso") and not part.endswith("Leg"):
        return world.copy()
    x, y, z = world.T
    result = world.copy()
    dx = np.zeros_like(x)
    dy = np.zeros_like(y)
    flank = np.tanh(x/.25)
    # Anchor neckline, sternum and shoulder sockets. Bring the front panel away
    # from the rib cage in two shallow arcs without widening the whole back.
    if part == "UpperTorso":
        amount = {"Small":0., "Medium":.115, "Large":.25}[bust]
        chest = bell(z, 6.245, .244) * smooth((6.69-z)/.12)
        front = smooth((y+.018)/.38)
        panels = .74+.26*bell(np.abs(x), .205, .145)
        dy += amount * chest * front * panels
        dx += .24 * amount * chest * flank * smooth((y+.12)/.37)
        if waist == "Lean":
            cinch = bell(z,5.72,.212)
            dx -= .047 * cinch * flank
            dy -= .095 * cinch * (y-.10)
    # The same field traverses hip and thigh pieces, including their articulated
    # overlap. The athletic profile peaks higher and releases below the seat.
    configs = {"Medium":(0.,0.,4.85,.33), "Small":(-.060,-.101,4.84,.36),
               "Athletic":(.033,.125,4.997,.259), "Full":(.076,.238,4.845,.369),
               "Compact":(-.025,-.070,4.88,.33)}
    width, projection, peak, spread = configs[seat]
    hip = bell(z,peak,spread)
    if seat in ("Small","Compact"):
        # Reduce the full rear profile through the upper leg. Pinching a single
        # narrow ring makes the lower thigh project farther than the seat.
        hip = smooth((z-3.56)/.77)*smooth((5.55-z)/.46)
    rear = smooth((.125-y)/.423)
    dx += width * hip * flank * (.48+.52*rear)
    dy -= projection * hip * rear
    if seat == "Athletic":
        dy += .036 * bell(z,4.58,.185) * rear
    strength = {"Standard":0., "Thick":.110, "Power":.157, "Lean":-.043}[leg]
    thigh = bell(z,4.54,.313)
    # Outer- and front-facing volume leaves room for the hands and the inseam.
    outer = smooth((np.abs(x)-.14)/.47)
    dx += strength * thigh * flank * outer
    dy += strength * thigh * (1.04*smooth((y+.014)/.38)-.73*rear)
    if leg == "Power":
        dx += .032*bell(z,4.34,.21)*flank*outer
        dy += .066*bell(z,4.40,.235)*smooth((y+.01)/.38)
    result[:,0] += dx
    result[:,1] += dy
    return result


def positions(obj):
    coordinates = np.empty(len(obj.data.vertices)*3, dtype=np.float32)
    obj.data.vertices.foreach_get("co",coordinates)
    return coordinates.reshape(-1,3)


def transform(points, matrix):
    m = np.asarray(matrix)
    return points @ m[:3,:3].T + m[:3,3]


def select_fit(rig, index):
    rig["Body fit"] = index
    rig.update_tag()
    bpy.context.view_layer.update()


def to_guest_world(xyz):
    x,y,z = xyz
    return np.array([-x*6.1, 24-(z-3)*6.1, -y*6.1])


def guest_bone(name):
    if name.endswith("Torso"):
        return "body"
    side = "left" if name.startswith("Left") else "right"
    return side + ("_arm" if name.endswith(("Arm","Hand")) else "_leg")


def joint(name,parent="$mesh",pose=(0,0,0)):
    return dict(name=name,parent=parent,pose=list(pose),vertices=[],faces=[])


def game_mesh(rig, objects):
    parts = {"body":joint("body")}
    for name, source in (("head","Head"),("left_arm","LeftUpperArm"),("right_arm","RightUpperArm"),
                         ("left_leg","LeftUpperLeg"),("right_leg","RightUpperLeg")):
        pivot = to_guest_world(rig.matrix_world @ rig.data.bones[source].head_local)
        parts[name] = joint(name,pose=pivot.round(5))
    for name,parent in (("hat","head"),("winter_collar","body"),("winter_left_arm","left_arm"),
                         ("winter_right_arm","right_arm"),("harvest_pin","head"),("harvest_collar","body"),
                         ("spring_pin","head"),("spring_collar","body")):
        parts[name] = joint(name,parent)
    deps = bpy.context.evaluated_depsgraph_get()
    for slot,obj in enumerate(objects):
        part = parts[guest_bone(obj.name)]
        evaluated = obj.evaluated_get(deps)
        mesh = evaluated.to_mesh()
        mesh.calc_loop_triangles()
        normals = evaluated.matrix_world.to_3x3().inverted().transposed()
        uv = mesh.uv_layers.active.data
        origin = part["pose"]
        lookup = {}
        for tri in mesh.loop_triangles:
            face = []
            for li in tri.loops:
                if li not in lookup:
                    loop = mesh.loops[li]
                    world = evaluated.matrix_world @ mesh.vertices[loop.vertex_index].co
                    vertex = (to_guest_world(world)-origin).round(5).tolist()
                    u,v = uv[li].uv
                    vertex += [round((128*(slot%4)+2+124*u)/512,7),
                               round((128*(slot//4)+2+124*(1-v))/512,7)]
                    nx,ny,nz = (normals @ mesh.corner_normals[li].vector).normalized()
                    vertex += [round(-nx,5),round(-nz,5),round(-ny,5)]
                    lookup[li] = len(part["vertices"])
                    part["vertices"].append(vertex)
                face.append(lookup[li])
            part["faces"].append(face+[face[-1]])
        evaluated.to_mesh_clear()
    for p in parts.values():
        if p["vertices"]:
            points = np.asarray(p["vertices"])[:,:3]
            p["bounds"] = [*points.min(axis=0),*points.max(axis=0)]
    return {"parts":list(parts.values())}


def make_library(out):
    out.mkdir(exist_ok=True,parents=True)
    scene = bpy.context.scene
    scene.frame_set(1)
    rig = next(o for o in scene.objects if o.type=="ARMATURE")
    objects = [bpy.data.objects[name] for name in BODY]
    assert len(rig.data.bones)==16 and all(o.find_armature()==rig for o in objects)
    assert all(o.type=="MESH" and o.data.uv_layers.active and not o.data.shape_keys for o in objects)
    # Operate only in the separate process that writes this fitting library.
    for obj in list(scene.objects):
        if obj != rig and obj.name not in BODY:
            bpy.data.objects.remove(obj,do_unlink=True)
    mat = bpy.data.materials.new("Matte fitting clay")
    rgb = np.array(RGBA[:3])/255
    mat.diffuse_color = (*np.power((rgb+.055)/1.055,2.4),1.)
    mat.use_nodes = True
    shader = mat.node_tree.nodes.get("Principled BSDF")
    shader.inputs["Base Color"].default_value = mat.diffuse_color
    shader.inputs["Roughness"].default_value = .92
    rig["Body fit"] = 0
    rig.id_properties_ui("Body fit").update(min=0,max=len(FITS)-1,
        description="Choose a fitting. The Fit index text lists the numbered silhouettes.")
    rig.name = "Wardrobe mannequin"
    coordinates = {}
    for obj in objects:
        obj.data.materials.clear()
        obj.data.materials.append(mat)
        for poly in obj.data.polygons:
            poly.material_index=0
        local = positions(obj)
        world = transform(local,obj.matrix_world)
        coordinates[obj.name] = [local.copy()]
        for index,fit in enumerate(FITS[1:],1):
            changed = fit_positions(world,obj.name,fit)
            if np.array_equal(world,changed):
                coordinates[obj.name].append(local)
                continue
            coords = transform(changed,obj.matrix_world.inverted()).astype(np.float32)
            coordinates[obj.name].append(coords)
            if obj.data.shape_keys is None:
                obj.shape_key_add(name="Basis")
            block = obj.shape_key_add(name=fit[0])
            block.data.foreach_set("co", coords.ravel())
            driver = block.driver_add("value").driver
            var = driver.variables.new()
            var.name = "fit_index"
            var.targets[0].id = rig
            var.targets[0].data_path = '["Body fit"]'
            driver.expression = f"fit_index == {index}"
    # Discard unlinked outfit data and character textures from this master.
    bpy.data.orphans_purge(do_recursive=True)
    assert not any(im.users and im.source=="FILE" for im in bpy.data.images)
    listing = bpy.data.texts.new("Fit index")
    listing.write("Select Wardrobe mannequin > Object Properties > Custom Properties > Body fit.\n"
                  "The head joint is available for a separate head. Frame 1 is the rest fitting pose.\n\n")
    entries=[]
    for index,(key,bust,seat,legs,waist) in enumerate(FITS):
        label = f"{bust} bust · {seat.lower()} seat"
        if legs!="Standard":label += f" · {legs.lower()} legs"
        if waist!="Standard":label += f" · {waist.lower()} waist"
        listing.write(f"{index:02d}    {label}\n")
        entries.append(dict(index=index,id=key,bust=bust,seat=seat,legs=legs,waist=waist,label=label))
    manifest = dict(reference=FITS[0][0],source="Approved Roblox fitting rig",color=RGBA,joints=16,presets=entries)
    (out/"fits.json").write_text(json.dumps(manifest,indent=2),encoding="utf8")
    select_fit(rig,0)
    bpy.ops.object.select_all(action="DESELECT")
    rig.select_set(True)
    bpy.context.view_layer.objects.active=rig
    for screen in bpy.data.screens:
        for area in screen.areas:
            if area.type=="VIEW_3D":
                space=area.spaces.active
                space.region_3d.view_location=(0,0,4.99)
                space.region_3d.view_distance=7.8
                space.shading.color_type="MATERIAL"
            if area.type=="PROPERTIES":
                area.spaces.active.context="OBJECT"
    bpy.context.preferences.filepaths.save_version=0
    bpy.ops.wm.save_as_mainfile(filepath=str(out/"wardrobe-mannequin.blend"),compress=True)
    # Validate the evaluated selector, then freeze each silhouette for interchange.
    for index,entry in enumerate(entries):
        select_fit(rig,index)
        deps=bpy.context.evaluated_depsgraph_get()
        for o in objects:
            v=positions(o.evaluated_get(deps))
            np.testing.assert_allclose(v,coordinates[o.name][index],atol=2.0e-6)
        folder=out/entry["id"]
        folder.mkdir(exist_ok=True)
        model=game_mesh(rig,objects)
        filename=folder/(entry["id"]+".json")
        filename.write_text(json.dumps(model,separators=(",",":")),encoding="utf8")
    # Interchange files carry a baked rest shape, the skeleton and the source walk.
    for o in objects:
        if o.data.shape_keys is not None:
            o.shape_key_clear()
    for index,entry in enumerate(entries):
        for o in objects:
            o.data.vertices.foreach_set("co",coordinates[o.name][index].ravel())
            o.data.update()
        filepath=out/entry["id"]/(entry["id"]+".glb")
        bpy.ops.export_scene.gltf(filepath=str(filepath),export_format="GLB",export_morph=False,
                                  export_animations=True,export_skins=True,export_extras=False)
    print(f"Built {len(entries)} Roblox body fittings on {len(objects)} source mesh parts.")


if __name__=="__main__":
    cli=argparse.ArgumentParser(description=__doc__)
    cli.add_argument("output",type=Path)
    make_library(cli.parse_args(sys.argv[sys.argv.index("--")+1:]).output)
