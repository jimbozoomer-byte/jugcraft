"""Read-only dry-space/fluid boundary audit of a candidate against copied regions.

This flags obvious open-water ingress, not all fluid behavior or safe installation
ordering. It never writes world data and cannot approve a live excavation.
"""
import argparse
from collections import Counter
import gzip
import json
from pathlib import Path
from western_city_survey import chunks, block, value

DIRECTIONS=((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))
AIR={'minecraft:air','minecraft:cave_air','minecraft:void_air'}
FLUID={'minecraft:water','minecraft:lava'}


def audit(regions,plan):
    ox,oy,oz=plan['siteReferenceOrigin'];b=plan['bounds']
    sections={}
    for cx,cz,root in chunks(regions,(ox+b[0]-1,oz+b[2]-1,ox+b[3]+1,oz+b[5]+1)):
        sections[cx,cz]={value(s,'Y'):s for s in value(root,'sections',(10,[]))[1]}
    intended={(x,y,z):plan['palette'][p].split('[',1)[0] for x,y,z,p in plan['cells']}
    missing=set();counts=Counter();examples=[];wet_excavation=0
    def existing(x,y,z):
        ax,ay,az=x+ox,y+oy,z+oz;chunk=(ax//16,az//16)
        if chunk not in sections:
            missing.add(chunk);return 'unavailable'
        return block(sections[chunk].get(ay//16,{}),ax,ay,az)
    for (x,y,z),after in intended.items():
        if after not in AIR:continue
        if existing(x,y,z) in FLUID:wet_excavation+=1
        for dx,dy,dz in DIRECTIONS:
            q=(x+dx,y+dy,z+dz)
            if q in intended:continue
            source=existing(*q)
            if source in FLUID:
                counts[source]+=1
                if len(examples)<100:examples.append({'airLocal':[x,y,z],'outsideFluidLocal':list(q),'fluid':source})
    return {'readOnly':True,'automaticApproval':False,'wetExcavationCells':wet_excavation,
        'outsideFluidFaces':sum(counts.values()),'byFluid':dict(counts),'examples':examples,
        'missingChunks':[list(q) for q in sorted(missing)],
        'limitations':'Direct six-neighbor AIR/fluid adjacency only. Waterlogged blocks, porous boundary blocks, connected caves, fluid ticks, transient construction order and ownership require further review.'}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--regions',required=True,type=Path);p.add_argument('--plan',required=True,type=Path)
    p.add_argument('--output',required=True,type=Path);a=p.parse_args()
    if a.regions.resolve() in a.output.resolve().parents:p.error('Report must be outside reference directory')
    with gzip.open(a.plan,'rt',encoding='utf-8') as stream:plan=json.load(stream)
    result=audit(a.regions,plan)
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8',newline='\n')
    print(f"{result['wetExcavationCells']} wet excavation cells; {result['outsideFluidFaces']} open boundary faces; {len(result['missingChunks'])} missing chunks. Read-only planning check.")


if __name__=='__main__':main()
