"""Read-only material/structure impact report for a candidate and copied regions.

This does not infer ownership, approve excavation, or write any Minecraft files.
It compares block types, not inventories or every block-state property. A region
copy obtained while the server was running is only a planning reference.
"""
import argparse
from collections import Counter,defaultdict
import gzip
import json
from pathlib import Path
from western_city_survey import chunks,block,value


def impact(regions,plan):
    ox,oy,oz=plan['siteReferenceOrigin']
    by_chunk=defaultdict(list)
    for x,y,z,p in plan['cells']:by_chunk[((x+ox)//16,(z+oz)//16)].append((x,y,z,p))
    b=plan['bounds'];bounds=(b[0]+ox,b[2]+oz,b[3]+ox,b[5]+oz)
    counts=Counter();examples=defaultdict(list);found=set();conflicts=[]
    for cx,cz,root in chunks(regions,bounds):
        found.add((cx,cz));cells=by_chunk.get((cx,cz),[])
        sections={value(s,'Y'):s for s in value(root,'sections',(10,[]))[1]}
        modified=set()
        for x,y,z,p in cells:
            ax,ay,az=x+ox,y+oy,z+oz
            before=block(sections.get(ay//16,{}),ax,ay,az)
            after=plan['palette'][p].split('[',1)[0]
            if before==after:continue
            counts[before]+=1;modified.add((ax,ay,az))
            if len(examples[before])<8:examples[before].append([x,y,z])
        for entity in value(root,'block_entities',(10,[]))[1]:
            p=tuple(value(entity,k) for k in ('x','y','z'))
            if p in modified:conflicts.append({'id':value(entity,'id'),'world':p})
    return {'readOnly':True,'automaticApproval':False,'origin':[ox,oy,oz],
        'limitations':'Block-type comparison only. All ownership, entity regions, inventories, block-state changes, fluid boundaries and current stopped-world data still need review.',
        'missingChunks':[list(p) for p in sorted(set(by_chunk)-found)],
        'blockEntityConflicts':conflicts,
        'affectedBlockTypes':[{'block':k,'count':n,'exampleLocalPositions':examples[k]} for k,n in counts.most_common()]}


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--regions',type=Path,required=True);p.add_argument('--plan',type=Path,required=True)
    p.add_argument('--output',type=Path,required=True);a=p.parse_args()
    if a.regions.resolve() in a.output.resolve().parents:p.error('Write reports outside the copied region directory')
    with gzip.open(a.plan,'rt',encoding='utf-8') as stream:plan=json.load(stream)
    result=impact(a.regions,plan)
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8',newline='\n')
    print(f'{len(result["affectedBlockTypes"])} affected block types; {len(result["blockEntityConflicts"])} block-entity conflicts; {len(result["missingChunks"])} missing chunks. No world changed.')


if __name__=='__main__':main()
