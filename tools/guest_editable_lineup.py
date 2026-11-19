"""Build a packed Blender review scene from the Minecraft model/atlas pairs.

blender --background --factory-startup --disable-autoexec --python tools/guest_editable_lineup.py -- OUTPUT.blend
"""
import json
import math
import sys
from pathlib import Path

import bpy
import numpy as np
from mathutils import Quaternion

ASSETS=Path(__file__).resolve().parents[1]/"src/main/resources/assets/jugcraft"
NAMES={"chun_li":"Chun-Li","tifa_lockhart":"Tifa Lockhart","yorha_2b":"YoRHa 2B","ada_wong":"Ada Wong",
       "lara_croft":"Lara Croft","kafka":"Kafka","makima":"Makima","misato_katsuragi":"Misato Katsuragi"}


def axes(v):
    return v[..., [0,2,1]]*[1,1,-1]


def add_character(guest,name,column,row_width):
    coll=bpy.data.collections.new(name)
    bpy.context.scene.collection.children.link(coll)
    data=json.loads((ASSETS/"town_models/guests"/(guest+".json")).read_text())["parts"]
    arm=bpy.data.armatures.new(name+" joints")
    rig=bpy.data.objects.new(name,arm)
    coll.objects.link(rig)
    rig.location.x=(column%8-(row_width-1)/2)*1.15
    rig.location.z=-(column//8)*2.63
    arm.display_type="STICK"
    bpy.context.view_layer.objects.active=rig
    rig.select_set(True)
    bpy.ops.object.mode_set(mode="EDIT")
    origins={"$mesh":np.array([0.,0.,1.5])}
    for part in data:
        bone=part["name"]
        origin=origins[part["parent"]]+axes(np.asarray(part["pose"]))/16
        origins[bone]=origin
        joint=arm.edit_bones.new(bone)
        joint.head=origin
        joint.tail=origin+[0,0,-.45 if bone.endswith(("_arm","_leg")) else .13]
        if part["parent"]!="$mesh":joint.parent=arm.edit_bones[part["parent"]]
    bpy.ops.object.mode_set(mode="OBJECT")
    rig.select_set(False)
    mat=bpy.data.materials.new(name+" atlas")
    mat.use_nodes=True
    nt=mat.node_tree
    image=bpy.data.images.load(str(ASSETS/"textures/entity/town_guests"/("guest_"+guest+".png")))
    image.pack()
    image.filepath="//"+image.name
    tex=nt.nodes.new("ShaderNodeTexImage")
    tex.image=image
    tex.interpolation="Closest"
    shader=nt.nodes.get("Principled BSDF")
    nt.links.new(tex.outputs["Color"],shader.inputs["Base Color"])
    nt.links.new(tex.outputs["Alpha"],shader.inputs["Alpha"])
    shader.inputs["Roughness"].default_value=.8
    for part in data:
        if not part["vertices"]:continue
        bone=part["name"]
        v=np.asarray(part["vertices"])
        faces=[list(dict.fromkeys(q)) for q in part["faces"]]
        mesh=bpy.data.meshes.new(name+" / "+bone)
        mesh.from_pydata(axes(v[:,:3])/16+origins[bone],[],faces,shade_flat=False)
        mesh.update()
        uv=mesh.uv_layers.new(name="Atlas")
        indices=np.empty(len(mesh.loops),dtype=np.int32)
        mesh.loops.foreach_get("vertex_index",indices)
        coords=v[indices,3:5].copy()
        coords[:,1]=1-coords[:,1]
        uv.data.foreach_set("uv",coords.astype("float32").ravel())
        mesh.normals_split_custom_set_from_vertices(axes(v[:,5:]).tolist())
        obj=bpy.data.objects.new(mesh.name,mesh)
        coll.objects.link(obj)
        obj.parent=rig
        group=obj.vertex_groups.new(name=bone)
        group.add(list(range(len(v))),1,"REPLACE")
        mod=obj.modifiers.new("Town guest rig","ARMATURE")
        mod.object=rig
        mesh.materials.append(mat)
        if bone.startswith(("winter_","harvest_","spring_")):
            obj.hide_set(True)
            obj.hide_render=True
    return rig


bpy.ops.wm.read_factory_settings(use_empty=True)
output,*requested=sys.argv[sys.argv.index("--")+1:]
names=requested or list(NAMES)
for i,guest in enumerate(names):
    name=NAMES.get(guest,"AOC" if guest=="aoc" else guest.replace("_"," ").title())
    add_character(guest,name,i,min(8,len(names)))
bpy.context.view_layer.objects.active=None
for screen in bpy.data.screens:
    for area in screen.areas:
        if area.type=="VIEW_3D":
            sp=area.spaces.active
            sp.overlay.show_overlays=False
            sp.shading.type="MATERIAL"
            sp.region_3d.view_rotation=Quaternion((1,0,0),math.pi/2)
            sp.region_3d.view_perspective="ORTHO"
            last_row=(len(names)-1)//8
            sp.region_3d.view_distance=4.7 if len(names)<=2 else 8.1+1.45*last_row
            sp.region_3d.view_location=(0,0,1.01-1.315*last_row)
bpy.context.preferences.filepaths.save_version=0
bpy.ops.wm.save_as_mainfile(filepath=str(Path(output).resolve()),compress=True)
