"""Extract the approved face and selected individual hair meshes from the two packed Blender libraries."""
import json
import sys
from pathlib import Path
import bpy
import numpy as np

FAMILIES = {
    6: (5,), 14: (13,16), 21: (19,20), 24: (23,), 28: (26,), 45: (47,48),
    51: (49,), 62: (58,59,60,61,63), 43: (42,), 29: (33,35,38),
}
GAME_R6 = np.array([[0,-1,0],[0,0,-1],[1,0,0]])
GAME_R15 = np.array([[-1,0,0],[0,0,-1],[0,-1,0]])


def mesh_data(obj, matrix, axes):
    mesh=obj.data
    mesh.calc_loop_triangles()
    points=np.array([tuple(v.co) for v in mesh.vertices])
    p=points@matrix[:3,:3].T+matrix[:3,3]
    uv=np.array([tuple(u.uv) for u in mesh.uv_layers.active.data])
    uv[:,1]=1-uv[:,1]
    normal=np.array([tuple(n.vector) for n in mesh.corner_normals])@np.linalg.inv(matrix[:3,:3])@axes.T
    normal/=np.maximum(1e-12,np.linalg.norm(normal,axis=1,keepdims=True))
    return ((p@axes.T)[[v.vertex_index for v in mesh.loops]],uv,normal,
            np.array([p.loops[:] for p in mesh.loop_triangles]))


def pixels(obj):
    mat=obj.material_slots[0].material
    shader=next(n for n in mat.node_tree.nodes if n.type=='BSDF_PRINCIPLED')
    im=shader.inputs['Base Color'].links[0].from_node.image
    return im.packed_file.data


def run(r15,library,out):
    out.mkdir(parents=True,exist_ok=True)
    bpy.ops.wm.open_mainfile(filepath=str(r15),use_scripts=False)
    obj=bpy.data.objects['Head']
    m=np.asarray(obj.matrix_world)
    p,uv,n,f=mesh_data(obj,m,GAME_R15)
    p=p*6.1+[0,24+3*6.1,0]
    np.savez_compressed(out/'head.npz',p=p,uv=uv,n=n,f=f)
    (out/'head.png').write_bytes(pixels(obj))
    center=(p.min(0)+p.max(0))/2
    hair=bpy.data.objects['Hair']
    p,uv,n,f=mesh_data(hair,np.asarray(hair.matrix_world),GAME_R15)
    p=(p*6.1+[0,24+3*6.1,0]-center)/7.19
    np.savez_compressed(out/'hair0.npz',p=p,uv=uv,n=n,f=f)
    (out/'hair0.png').write_bytes(pixels(hair))
    bpy.ops.wm.open_mainfile(filepath=str(library),use_scripts=False)
    for face,pieces in FAMILIES.items():
        faceobj=bpy.data.objects[f'Handle{face}']
        rig=faceobj.find_armature()
        def data(obj):
            return mesh_data(obj,np.asarray(rig.matrix_world.inverted()@obj.matrix_world),GAME_R6)
        facepoints=data(faceobj)[0]
        for number in pieces:
            obj=bpy.data.objects[f'Handle{number}']
            assert obj.find_armature()==rig
            p,uv,n,f=data(obj)
            # Coordinates relative to the donor's actual face, in units of face width.
            lo=facepoints.min(0); hi=facepoints.max(0)
            center=(lo+hi)/2
            p=(p-center)/(hi[0]-lo[0])
            np.savez_compressed(out/f'hair{number}.npz',p=p,uv=uv,n=n,f=f)
            (out/f'hair{number}.png').write_bytes(pixels(obj))
            print(number,'relative bounds',np.round(p.min(0),2),np.round(p.max(0),2),'face span',(hi-lo).round(2))


if __name__=='__main__':
    run(*map(Path,sys.argv[sys.argv.index('--')+1:]))
