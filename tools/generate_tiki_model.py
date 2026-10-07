"""Author the block-native woven tiki torch and its optional light definition."""
from pathlib import Path
import json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
A=ROOT/'assets/peepo_companion'
def write(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,indent=2)+'\n')
def box(lo,hi,texture):
    return {'from':lo,'to':hi,'faces':{side:{'texture':'#'+texture} for side in ['up','down','north','south','east','west']}}
# Original pixel-art basket weave: alternating over/under reed strips and fine dark seams.
im=Image.new('RGB',(16,16))
for y in range(16):
    for x in range(16):
        band=(y//3)%2
        seam=(x+y+band*3)%8
        cross=(x-y)%8
        if seam==0:c=(126,103,55)
        elif seam==1:c=(226,207,145)
        elif cross<2:c=(164,140,82)
        else:
            grain=((x*7+y*3)%5)-2
            c=(194+grain,170+grain,108+grain)
        im.putpixel((x,y),c)
tex=A/'textures/block/tiki_weave.png';tex.parent.mkdir(parents=True,exist_ok=True);im.save(tex)
textures={'particle':'peepo_companion:block/tiki_weave','shaft':'minecraft:block/stripped_oak_log','weave':'peepo_companion:block/tiki_weave','binding':'minecraft:block/oak_planks','cap':'minecraft:block/polished_blackstone','wick':'minecraft:block/black_wool','flame':'minecraft:block/orange_wool','flame_tip':'minecraft:block/yellow_wool'}
full=[box([7.45,0,7.45],[8.55,6.5,8.55],'shaft'),box([7.2,5.5,7.2],[8.8,6.2,8.8],'binding')]
# Narrow open supports fan out from the shaft into the basket, leaving visible gaps.
for y in range(6,18,2):
    spread=.35+(y-6)*.18
    for sx in [-1,1]:
        for sz in [-1,1]:
            x,z=8+sx*spread,8+sz*spread
            full.append(box([x-.34,y,z-.34],[x+.34,y+2,z+.34],'shaft'))
full += [box([5.4,17.7,5.4],[10.6,18.25,10.6],'binding'),
    box([5.5,18,5],[10.5,28,11],'weave'),box([5,18,5.5],[11,28,10.5],'weave'),
    box([4.7,28,4.7],[11.3,28.6,11.3],'cap'),box([5.5,28.6,5.5],[10.5,29.15,10.5],'cap'),
    box([6.5,29.15,6.5],[9.5,29.65,9.5],'cap'),box([7.55,29.65,7.55],[8.45,30.2,8.45],'wick'),
    box([7.5,30.2,7.5],[8.5,30.9,8.5],'flame'),box([7.7,30.9,7.7],[8.3,31.5,8.3],'flame_tip'),
    box([7.85,31.5,7.85],[8.15,31.9,8.15],'flame_tip')]
# Clip at the block boundary so each half's geometry stays within its own cell.
for name,base in [('lower',0),('upper',16)]:
    elements=[]
    for e in full:
        lo,hi=e['from'].copy(),e['to'].copy()
        lo[1]=max(lo[1],base);hi[1]=min(hi[1],base+16)
        if lo[1]>=hi[1]:continue
        lo[1]-=base;hi[1]-=base
        elements.append({**e,'from':lo,'to':hi})
    write(A/f'models/block/tiki_torch_{name}.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements})
p=A/'models/item/tiki_torch.json';item=json.loads(p.read_text());item['textures']=textures;item['elements']=full;write(p,item)
write(A/'dynamiclights/item/tiki_torch.json',{'match':{'items':'peepo_companion:tiki_torch'},'luminance':14,'water_sensitive':True})
