"""Full-size timber civic noticeboard. Nine clipped block models share a coherent 48px design."""
from copy import deepcopy

TEXTURES = {"wood":"minecraft:block/oak_planks", "post":"minecraft:block/stripped_oak_log",
    "trim":"minecraft:block/spruce_planks", "roof":"minecraft:block/bricks",
    "paper":"minecraft:block/white_concrete", "cream":"minecraft:block/sandstone_top",
    "blue":"minecraft:block/light_blue_concrete", "ink":"minecraft:block/light_gray_concrete",
    "pin":"minecraft:block/oxidized_copper", "redpin":"minecraft:block/redstone_block", "goldpin":"minecraft:block/gold_block",
    "particle":"minecraft:block/oak_planks"}
ELEMENTS=[]
def box(a,b,texture):
    ELEMENTS.append({"from":a,"to":b,"faces":{face:{"texture":"#"+texture,"uv":[0,0,16,16]} for face in ("north","south","east","west","up","down")}})
# Uprights, warm oak backing, rails and restrained roof cap. Everything has a physical support.
for x in (3,41): box([x,0,6],[x+4,10,10],"post")
box([2,12,6],[46,39,10],"wood")
for y in (10,39): box([0,y,5],[48,y+2,11],"trim")
for x in (0,46): box([x,12,5],[x+2,39,11],"post")
# Terracotta-coloured roof tiles, with an actual raised ridge and eaves rather than a floating slab.
box([0,41,2],[48,43,14],"trim")
box([0,43,2],[48,44,14],"roof")
box([0,44,4],[48,45,12],"roof")
box([0,45,6],[48,46,10],"roof")
# Layered, individually pinned papers; sheets deliberately avoid splitting across the 16px tile seams.
for index,(x,y,w,h) in enumerate([(4,25,9,12),(18,22,10,13),(34,26,10,12),(5,12,9,10),(20,11,9,9),(34,13,10,11)]):
    material=("paper","blue","cream","cream","paper","paper")[index]
    box([x,y,5.5],[x+w,y+h,5.75],material)
    # Short, pixel-crisp typeset rules make these read as posted notices, not miniature rolled scrolls.
    for line in range(4):
        ly=y+h-3-line*1.5
        width=w-2-(1 if line%2 else 0)
        box([x+1,ly,5.125],[x+1+width,ly+.375,5.375],"ink")
    box([x+w/2-.5,y+h-1,4.75],[x+w/2+.5,y+h,5.5],("pin","redpin","goldpin")[index%3])

ASSETS={}
for row in range(3):
    for col in range(3):
        clipped=[]
        for source in ELEMENTS:
            a=source["from"];b=source["to"]
            lo=[max(a[0],col*16),max(a[1],row*16),a[2]]
            hi=[min(b[0],col*16+16),min(b[1],row*16+16),b[2]]
            if hi[0]<=lo[0] or hi[1]<=lo[1]:continue
            e=deepcopy(source)
            e["from"]=[lo[0]-col*16,lo[1]-row*16,lo[2]]
            e["to"]=[hi[0]-col*16,hi[1]-row*16,hi[2]]
            clipped.append(e)
        part=row*3+col
        ASSETS[f"models/block/town_commission_board_{part}.json"]={"parent":"minecraft:block/block","textures":TEXTURES,"elements":clipped}
ASSETS["blockstates/town_commission_board.json"]={"variants":{
    f"facing={facing},part={part}":{"model":f"jugcraft:block/town_commission_board_{part}","y":angle}
    for facing,angle in (("north",0),("east",90),("south",180),("west",270)) for part in range(9)}}
# Full assembly miniature only for the inventory/held item, never for the placed world block.
mini=deepcopy(ELEMENTS)
for e in mini:
    for key in ("from","to"):e[key]=[e[key][0]/3,e[key][1]/3,(e[key][2]-8)/3+8]
ASSETS["models/block/town_commission_board.json"]={"parent":"minecraft:block/block","textures":TEXTURES,"elements":mini,
    "display":{"gui":{"rotation":[15,25,0],"translation":[0,0,0],"scale":[.9,.9,.9]}}}
ASSETS["items/town_commission_board.json"]={"model":{"type":"minecraft:model","model":"jugcraft:block/town_commission_board"}}
RECIPE={"type":"minecraft:crafting_shaped","pattern":["BBB","PPP","F F"],"key":{"B":"minecraft:brick","P":"minecraft:oak_planks","F":"minecraft:oak_fence"},"result":{"id":"jugcraft:town_commission_board","count":1},"fabric:load_conditions":[{"condition":"jugcraft:feature_enabled","feature":"town"}]}
def assets(assets_dir,write,lang):
    for path,value in ASSETS.items():write(assets_dir/path,value)
    lang["block.jugcraft.town_commission_board"]="City Commission Board"
    lang["message.jugcraft.commission.unavailable"]="This delivery or reward is unavailable. Check your items, progress and purse capacity."
