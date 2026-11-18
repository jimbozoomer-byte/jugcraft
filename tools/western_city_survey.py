"""Read-only terrain/build-evidence report from copied region files.

This is planning evidence, never ownership proof or permission to excavate.
Entity regions, claims and a stopped-world backup are separate requirements.
"""
from pathlib import Path
import argparse
import hashlib
import json
import math
import struct
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'scripts'))
from audit_candy_chunks import unpack, value
import nouveau_nbt


def chunks(folder, bounds):
    x0,z0,x1,z1=bounds
    for path in sorted(folder.glob('r.*.*.mca')):
        _,rx,rz,_=path.name.split('.'); rx=int(rx);rz=int(rz)
        with path.open('rb') as stream:
            header=stream.read(8192)
            if len(header)!=8192: raise ValueError(f'Truncated region: {path.name}')
            size=path.stat().st_size
            for slot in range(1024):
                cx,cz=rx*32+slot%32,rz*32+slot//32
                if cx*16>x1 or cx*16+15<x0 or cz*16>z1 or cz*16+15<z0: continue
                loc=int.from_bytes(header[slot*4:slot*4+4],'big')
                if not loc: continue
                sector,count=loc>>8,loc&255
                if sector<2 or not count or (sector+count)*4096>size: raise ValueError('Invalid region extent')
                stream.seek(sector*4096)
                length=struct.unpack('>I',stream.read(4))[0]
                if length<1 or length>count*4096-4: raise ValueError('Invalid chunk length')
                compression=stream.read(1)[0]
                root=nouveau_nbt.read(unpack(stream.read(length-1),compression))[2]
                yield cx,cz,root


def block(section,x,y,z):
    states=value(section,'block_states',{})
    palette=value(states,'palette',(10,[]))[1]
    if not palette: return 'minecraft:air'
    data=value(states,'data',[])
    bits=max(4,(len(palette)-1).bit_length()); per=64//bits
    i=(y&15)*256+(z&15)*16+(x&15)
    n=0 if not data else (data[i//per]>>((i%per)*bits))&((1<<bits)-1)
    entry=palette[n]
    if isinstance(entry,str): return entry.split('[',1)[0]
    result=value(entry,'Name',value(entry,'id',value(entry,'','unknown')))
    return result.split('[',1)[0] if isinstance(result,str) else 'unknown'


def report(folder,origin,bounds):
    ox,oy,oz=origin
    rows=[];entities=[];populated=[]
    for cx,cz,root in chunks(folder,bounds):
        sections={value(s,'Y'):s for s in value(root,'sections',(10,[]))[1]}
        # Vanilla chunks include one extra light-only section at either edge.
        active=[y for y,s in sections.items() if 'block_states' in s]
        if not active: continue
        minimum=min(active)*16
        heights=value(value(root,'Heightmaps',{}),'MOTION_BLOCKING_NO_LEAVES',[])
        if not heights: heights=value(value(root,'Heightmaps',{}),'WORLD_SURFACE',[])
        if not heights: continue
        # 37 longs = 9 bits (384-high world); 43 = 10 bits (tall worlds).
        candidates=[b for b in range(8,17) if math.ceil(256/(64//b))==len(heights)]
        if len(candidates)!=1: raise ValueError(f'Ambiguous height-map packing {len(heights)}')
        bits=candidates[0];per=64//bits
        for z in range(cz*16,cz*16+16,2):
            for x in range(cx*16,cx*16+16,2):
                if not(bounds[0]<=x<=bounds[2] and bounds[1]<=z<=bounds[3]):continue
                i=(z&15)*16+(x&15);y=((heights[i//per]>>((i%per)*bits))&((1<<bits)-1))+minimum-1
                rows.append([x-ox,y-oy,z-oz,block(sections.get(y//16,{}),x,y,z)])
        for e in value(root,'block_entities',(10,[]))[1]:
            x,y,z=(value(e,k) for k in ('x','y','z'))
            if bounds[0]<=x<=bounds[2] and bounds[1]<=z<=bounds[3]:
                entities.append({'id':value(e,'id'),'local':[x-ox,y-oy,z-oz]})
        populated.append({'chunk':[cx,cz],'inhabitedTicks':value(root,'InhabitedTime',0)})
    return {'readOnly':True,'origin':origin,'worldBounds':bounds,
        'warning':'Downloaded while server was running. Planning reference only, not a consistent backup. Terrain type and absent block entities do not prove land is unowned.',
        'regions':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(folder.glob('r.*.*.mca'))},
        'surface':rows,'blockEntities':entities,'chunks':populated}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--regions',type=Path,required=True);p.add_argument('--output',type=Path,required=True)
    p.add_argument('--origin',type=int,nargs=3,required=True)
    p.add_argument('--bounds',type=int,nargs=4,required=True)
    a=p.parse_args()
    if a.regions.resolve() in a.output.resolve().parents: p.error('Keep reports outside the copied region directory')
    r=report(a.regions,a.origin,a.bounds)
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(r,indent=2)+'\n',encoding='utf-8',newline='\n')
    ys=[s[1] for s in r['surface']]
    if not ys: raise ValueError('No readable height-map samples in the requested area')
    print(f'{len(ys)} terrain samples; height offsets {min(ys)}..{max(ys)}; {len(r["blockEntities"])} block entities. No world changed.')


if __name__=='__main__':main()
