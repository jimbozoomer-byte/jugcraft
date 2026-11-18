"""Deepstead's original block-model rig and three hand-paletted pixel skins.

Box coordinates use vanilla's 16-pixel unit, ground at y=24. The six-sided
unwrap is shared between the model baker and the pixel painter below.
"""
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/jugcraft"
ATLAS = 256
PIXELS_PER_UNIT = 2  # Keep vanilla box UVs. Paint two physical texels per model pixel.
parts = []
patches = {}
cursor = [1, 1, 0]


def part(name, parent="$mesh", at=(0, 0, 0), rot=(0, 0, 0)):
    node = dict(name=name, parent=parent, pose=[*at, *(math.radians(a) for a in rot)], cubes=[])
    parts.append(node)
    return node


def box(node, material, x, y, z, w, h, d):
    key = (material, w, h, d)
    if key not in patches:
        length = 2 * (w + d)
        if cursor[0] + length >= ATLAS:
            cursor[0], cursor[1], cursor[2] = 1, cursor[1] + cursor[2] + 1, 0
        if cursor[1] + d + h >= ATLAS:
            raise ValueError("Dwarf atlas overflow")
        patches[key] = cursor[:2]
        cursor[0] += length + 1
        cursor[2] = max(cursor[2], d + h)
    node["cubes"].append(dict(uv=patches[key], box=[x,y,z,w,h,d], mirror=False, inflate=[0,0,0]))


body = part("body", at=(0, 9, 0))
box(body, "coat", -5, 0, -3, 10, 5, 6)
box(body, "coat", -4.5, 4.4, -2.5, 9, 4, 5)
box(body, "belt", -4.65, 6.6, -3.1, 9, 2, 6)
box(body, "buckle", -1.5, 6.6, -3.66, 3, 2, 1)

head = part("head", "body", at=(0, 1, 0))
box(head, "face", -4, -8, -4, 8, 8, 8)
nose = part("nose_bridge", "head", (0,-4.15,-3.95), rot=(-18,0,0))
box(nose, "nose", -1,-.15,-1.12, 2, 2, 2)
tip = part("nose_point","nose_bridge",(0,1.68,-1.34),rot=(-9,0,-5))
box(tip, "nose_tip", -1.5,-.48,-.74, 3, 1, 2)
box(tip, "nose_tip", -.99,.32,-.56, 2, 1, 2)
for side in (-1, 1):
    box(head, "ear", side*4-.5,-4.7,.2, 1, 3, 2)
    brow = part("brow_"+str(side), "head", (side*2.2,-4.98,-4), rot=(0,0,-side*10))
    box(brow, "hair", -1.55,-.92,-.94, 3, 1, 2)
    cheek = part("sideburn_"+str(side), "head", (side*3.67,-3.85,-2.45), rot=(-8,0,side*4))
    box(cheek, "beard", -1,0,-1.5, 2, 4, 3)
    whisker = part("whisker_"+str(side), "head", (side*1.78,-1.7,-4.55), rot=(0,side*6,side*17))
    box(whisker, "whisker", -1.65,-.55,-1.06, 3, 2, 2)
beard = part("beard","head", (0,-.6,-3.15))
box(beard, "beard", -3.75,0,-1.5, 7, 3, 3)
smith_beard = part("smith_beard","beard")
box(smith_beard, "beard", -2.5,1.55,-1.75, 5, 4, 3)
box(smith_beard, "forge_ring", -1.5,4.9,-1.93, 3, 1, 3)
box(smith_beard, "beard", -1.5,5.75,-1.72, 3, 2, 2)
trader_beard = part("merchant_beard","beard")
box(trader_beard, "beard", -2.5,1.25,-1.75, 5, 3, 3)
watch_beard = part("watch_beard","beard")
box(watch_beard, "beard", -3.5,1.65,-1.85, 7, 3, 3)
box(watch_beard, "beard", -2.5,3.95,-1.55, 5, 2, 3)
for side in (-1, 1):
    lock = part("smith_lock_"+str(side), "smith_beard", (side*2.72,1.18,-.92), rot=(-7,0,side*9))
    box(lock, "beard", -1.42,0,-1.1, 3, 4, 2)
    box(lock, "beard", -.88,3.58,-.82, 2, 2, 2)
    plait = part("trader_plait_"+str(side), "merchant_beard", (side*2.65,.8,-.65), rot=(4,0,side*8))
    box(plait, "braid", -1.25,0,-1, 3, 4, 2)
    box(plait, "braid", -.91,3.43,-1.13, 2, 3, 2)
    box(plait, "clasp", -1,5.33,-1.37, 2, 1, 3)
    box(plait, "whisker", -.55,6.18,-.98, 1, 2, 2)

crest = part("miner_hair", "head")
box(crest, "cropped", -3.5,-7.55,2.4, 7, 4, 2)
ridge = part("ridge", "miner_hair", (0,-7.9,0), rot=(-12,0,0))
box(ridge, "hair", -1, -2.55, -2.5, 2, 3, 5)
box(ridge, "hair", -.82,-3.82,-.95, 1, 2, 3)
box(ridge, "cropped", -.35,-4.15,1.48, 1, 2, 2)
vest = part("work_apron","body")
box(vest, "apron", -3.5,1.45,-3.22, 7, 5, 1)
box(vest, "apron", -3.5,8.27,-2.98, 7, 3, 1)
for side in (-1,1):
    shoulder=part("apron_loop_"+str(side),"work_apron",(side*3.54,-.26,.08),rot=(0,0,side*9))
    box(shoulder,"leather",-.8,0,-3.29, 2, 2, 7)
    box(shoulder,"pocket",-.78,1.35,2.76, 2, 4, 1)
box(vest,"pocket",1.4,4,-3.61, 2, 2, 1)
box(vest,"forge_ring",3.02,5.83,-2.6, 1, 2, 2)

helmet = part("guard_helmet", "head")
box(helmet, "helm_plate", -4.5,-8.55,-4.35, 9, 3, 9)
box(helmet, "helm_plate", -3.5,-9.36,-3.85, 7, 2, 8)
box(helmet, "brass", -.5,-9.63,-3.95, 1, 1, 8)
box(helmet, "brass", -.5,-8.62,-4.58, 1, 3, 1)
box(helmet, "helm_band", -4.5,-6.29,-4.62, 9, 1, 9)
for side in (-1, 1):
    flange=part("cheek_plate_"+str(side),"guard_helmet",(side*4.14,-5.78,-.25),rot=(9,0,-side*7))
    box(flange,"helm_plate",-.58,0,-2.3, 1, 4, 5)
    box(flange,"brass",-.61,2.65,-2.35, 1, 1, 4)
    box(flange,"clasp",side*.27-.5,.4,-1.33, 1, 1, 1)
box(helmet,"iron",-3.5,-5.78,3.08, 7, 3, 2)
visor = part("visor", "guard_helmet", (0,-4.85,.15))
# A projecting grille clears the tip of the nose. The mouth of each slit stays dark
# behind the lit vertical bars. Side levers pin it to both cheek hinges.
box(visor,"visor", -3.5,-.05,-7.31, 7, 4, 1)
box(visor,"helm_band", -4,-.55,-7.49, 8, 1, 1)
box(visor,"iron", -3.5,3.08,-7.46, 7, 1, 1)
for x in (-3.55,-1.55,.45,2.45):
    box(visor,"visor_rib", x,.25,-7.59, 1, 3, 1)
for side in (-1,1):
    box(visor,"visor",side*3.78-.5,-.45,-7.14, 1, 1, 7)
    box(helmet,"brass",side*4.38-.48,-5.30,-.48, 1, 1, 2)

cap = part("merchant_cap","head")
box(cap, "felt", -4.5,-8.15,-4.06, 9, 2, 8)
crown=part("slouch","merchant_cap",(-.2,-8.0,.25),rot=(3,1,-11))
box(crown,"felt",-4.65,-1.8,-3.8, 9, 2, 8)
box(crown,"felt",-4.04,-2.49,-3, 7, 1, 6)
box(cap, "weave", -4.5,-6.7,-4.1, 9, 1, 8)
box(cap, "brass", -3.5,-6.8,-4.5, 2, 1, 1)
pin=part("cap_feather","merchant_cap",(-3.95,-6.85,-2.35),rot=(-21,0,-22))
box(pin,"quill",-.45,-4.3,-.48, 1, 5, 1)
box(pin,"feather",-.96,-4.62,-.24, 2, 3, 1)
box(pin,"feather",-.65,-5.47,-.10, 1, 2, 1)
bag = part("satchel","body",(-4.6,5.2,.5))
box(bag, "leather", -1.1,0,-1.9, 3, 4, 4)
box(bag, "pocket", -1.25,0,-2.15, 3, 2, 1)
box(bag, "brass", -.3,1,-2.35, 1, 1, 1)
sample = part("lamp_sample","satchel",(.2,2.65,-1.65))
box(sample,"clasp",-.6,0,-.74, 2, 1, 2)
box(sample,"jade",-.6,.9,-.74, 2, 2, 2)
box(sample,"brass",-.6,2.7,-.74, 2, 1, 2)
roll = part("map_case","satchel",(.4,.15,2.3),rot=(8,0,16))
box(roll,"binding",-.8,-1.85,-.6, 2, 6, 2)
box(roll,"paper",-.66,-2.25,-.5, 2, 1, 2)
box(roll,"belt",-.86,1,-.69, 2, 1, 2)
strap = part("strap","body",(0,2,-2.9),rot=(0,0,33))
box(strap, "pocket", -.8,-3,-.58, 2, 9, 1)
collar=part("merchant_mantle","body")
box(collar,"felt",-5.12,-.25,-3.16, 10, 2, 7)
box(collar,"felt",-.25,-.19,2.62, 5, 4, 1)
box(collar,"weave",-.28,3.47,2.74, 5, 1, 1)
box(collar,"jade",2.93,1.02,-3.7, 2, 2, 1)
tabard=part("tabard","body")
box(tabard, "mail", -5.06,.25,-3.08, 10, 5, 6)
box(tabard, "burgundy", -3.5,.08,-3.27, 7, 6, 7)
for side in (-1,1):
    box(tabard,"hem",side*1.85-1.5,7.95,-3.19, 3, 3, 1)
box(tabard,"burgundy",-3.5,7.98,2.12, 7, 3, 1)

for limb, sign in (("right",-1),("left",1)):
    leg = part(limb+"_leg", at=(sign*2.88,17.4,sign*.3),rot=(0,-sign*7,0))
    box(leg, "trousers", -2, -.6, -2.2, 4, 5, 4)
    box(leg, "boot", -2, 3.1, -2.55, 4, 3, 5)
    box(leg, "sole", -2.05, 5.6, -3.42, 4, 1, 6)
    box(leg, "boot", -2.03, 4.49, -3.36, 4, 2, 2)
    box(leg, "belt", -2.03, 2.96, -2.67, 4, 1, 5)
    toe=part(limb+"_toe_cap",limb+"_leg")
    box(toe,"bracer",-2.10,4.64,-3.54, 4, 1, 3)
    greave=part(limb+"_greave",limb+"_leg")
    box(greave, "burgundy", -2.08,-.2,-2.45, 4, 3, 5)
    box(greave, "iron", -1.55,2.15,-2.81, 3, 3, 1)
    box(greave, "clasp", -1.55,3.82,-2.96, 3, 1, 1)
    arm=part(limb+"_arm","body",(sign*6.28,1.05,0))
    box(arm,"bicep",-2.4,-.95,-2.45, 5, 4, 5)
    box(arm,"skin",-1.95,-1.54,-1.95, 4, 2, 4)
    box(arm,"skin",-1.94,1.65,-1.96, 4, 4, 4)
    box(arm,"glove",-2.05,5.7,-2.38, 4, 3, 5)
    box(arm,"glove",-sign*1.85-.5,6.22,-2.67, 1, 2, 2)
    cuff=part(limb+"_gauntlet",limb+"_arm")
    box(cuff,"steel",-2.53,3.55,-2.7, 5, 4, 5)
    box(cuff,"bracer",-2.53,3.83,-2.87, 5, 3, 1)
    box(cuff,"iron",-2.55,6.32,-2.81, 5, 1, 5)
    sleeve=part(limb+"_sleeve",limb+"_arm")
    box(sleeve,"coat",-2.6,-1.79,-2.62, 5, 5, 6)
    box(sleeve,"weave",-2.45,2.66,-2.7, 5, 1, 5)
    pauldron=part(limb+"_pauldron",limb+"_arm")
    box(pauldron,"mail",-2.58,-.8,-2.6, 5, 5, 5)
    box(pauldron,"brass",-2.95,-1.69,-2.88, 6, 2, 6)
    box(pauldron,"helm_plate",-2.55,-2.28,-2.51, 5, 1, 5)
    box(pauldron,"iron",sign*2.3-.58,-.26,-2.34, 1, 2, 5)

hammer=part("stone_maul","right_arm",(-.55,6.78,-1.24),rot=(-30,-18,18))
box(hammer,"haft",-.78,-1.85,-.72, 2, 9, 2)
box(hammer,"hammerstone",-3.95,4.42,-2.22, 8, 5, 5)
box(hammer,"steel",-4.57,4.84,-2.03, 1, 4, 4)
box(hammer,"steel",3.89,4.84,-2.03, 1, 4, 4)
for x in (-2.6,1.6):
    box(hammer,"forge_ring",x,4.33,-2.38, 1, 5, 5)
mace=part("guard_mace","right_arm",(-.6,7,-2.3),rot=(37,0,-20))
box(mace,"haft",-.65,-8,-.5, 1, 11, 1)
box(mace,"clasp",-1.15,-5,-1, 2, 1, 2)
box(mace,"steel",-1.65,-9,-1.5, 3, 4, 3)
box(mace,"iron",-2.1,-8.5,-1, 4, 3, 2)
box(mace,"clasp",-1.15,-9.5,-1, 2, 1, 2)
book=part("ledger","left_arm",(0,6.7,-1),rot=(-21,7,-11))
box(book,"book_cover",-2.53,-1.3,-3.63, 5, 1, 6)
box(book,"paper",-2.3,-.76,-3.37, 5, 2, 6)
box(book,"book_cover",-2.53,.94,-3.63, 5, 1, 6)
box(book,"clasp",1.43,-.55,-3.56, 1, 2, 1)
shield=part("buckler","left_arm",(1.8,3.8,-1))
box(shield,"burgundy",-.5,-2.8,-2.5, 2, 7, 5)
box(shield,"clasp",.1,-2.8,-2.7, 2, 1, 5)
box(shield,"clasp",.1,3,-2.7, 2, 1, 5)
box(shield,"steel",1,-1,-1.4, 2, 3, 3)

# The hallkeeper carries a brass carpenter's square, its two ruled arms joined at the heel.
square=part("masons_square","left_arm",(0,6.5,-2.1),rot=(-22,9,-12))
box(square,"brass",-2.2,-3.85,-2.25, 1, 7, 1)
box(square,"brass",-1.2,2.14,-2.25, 5, 1, 1)
for y in (-3,-1,1):
    box(square,"iron",-2.25,y,-2.34, 1, 1, 1)
box(square,"buckle",-2.27,1.91,-2.42, 2, 2, 1)
# A shallow four-sided ladle bowl and long handle read clearly against the keeper's apron.
ladle=part("copper_ladle","right_arm",(0,6.6,-1.9),rot=(36,-9,12))
box(ladle,"haft",-.55,-5.0,-.35, 1, 7, 1)
box(ladle,"brass",-1.5,-6.65,-1.28, 3, 1, 3)
for dx in (-1.82,1.16):
    box(ladle,"brass",dx,-7.18,-1.65, 1, 1, 4)
for dz in (-1.65,1.36):
    box(ladle,"brass",-.82,-7.18,dz, 2, 1, 1)
towel=part("bar_towel","body",(3.55,6.65,-3.15),rot=(0,-6,-7))
box(towel,"paper",-1,0,-.28, 2, 4, 1)
box(towel,"hem",-1,2.85,-.38, 2, 1, 1)

# Dark seam, shade, body colour, worn edge, glint. Separate hues keep steel, hide
# and beard legible next to one another in the amber light of the galleries.
STEEL = ("#26303a","#455364","#697b8c","#9aaab3","#c9d1cb")
BRONZE = ("#493731","#755036","#a87943","#d1a469","#f2d59b")
HIDE = ("#342732","#57362e","#835137","#ac744a","#c49363")
WOOL = ("#163b40","#245058","#336866","#4c8980","#81a693")
OXBLOOD = ("#34232f","#5d2a37","#873e49","#aa5860","#c47a6c")
PALETTES = {
    "coat":WOOL, "felt":WOOL, "weave":BRONZE, "belt":HIDE, "brass":BRONZE,
    "iron":STEEL, "steel":STEEL, "forge_ring":("#293239","#485451","#757967","#aaa487","#dbcf9d"),
    "helm_plate":("#302d3b","#4a414c","#6a5b62","#938183","#b5a398"),
    "visor":("#161e27","#25313a","#38434c","#596876","#87949b"),
    "visor_rib":("#202938","#34424d","#4b5a68","#738393","#96a2ac"),
    "helm_band":BRONZE, "clasp":BRONZE, "buckle":BRONZE, "bracer":STEEL,
    "pocket":HIDE,"leather":HIDE,"glove":HIDE,"boot":HIDE,"binding":HIDE,"book_cover":HIDE,
    "sole":("#282330","#3b333a","#514449","#665851","#877163"),
    "burgundy":OXBLOOD,"hem":OXBLOOD,
    "haft":("#402b31","#63412e","#8b603a","#b2834d","#ce9c64"),
    "paper":("#78644f","#a18b6c","#d0b994","#ead7aa","#f5e5bd"),
    "trousers":("#292733","#403c49","#585260","#716872","#8c807d"),
    "hammerstone":("#313a40","#505957","#717c74","#939c87","#bdc1a8"),
    "mail":("#252935","#41434f","#636575","#908b8c","#ada8a1"),
    "apron":("#312c31","#4f3632","#705044","#92654b","#ae8261"),
    "quill":BRONZE,"feather":("#403533","#78583e","#b0875b","#d4b183","#f3d9aa"),
    "jade":("#203b42","#32606a","#488f91","#74beb2","#b4e1c8"),
}
LOOKS = [
    dict(skin=("#76493f","#ad7151","#d2a173","#eac18b","#f7d9a4"),
         hair=("#272431","#3c3036","#51413e","#6a554b","#897361"),eye="#744732"),
    dict(skin=("#78453b","#a66348","#ca8d65","#e1aa79","#f3c797"),
         hair=("#412832","#66382f","#924c32","#b26b42","#d49761"),eye="#42736c"),
    dict(skin=("#805345","#af7c66","#d6a98b","#e8c3a1","#f4d5b4"),
         hair=("#424052","#646171","#918985","#b6aca0","#dbcfb9"),eye="#536b85"),
]


def portrait(tile, side, look):
    """A 16x16 painted head, continuous around the temples, with the gaze in the front UV."""
    d = ImageDraw.Draw(tile)
    skin, hair = look["skin"], look["hair"]
    if side == "front":
        d.polygon([(0,1),(5,0),(5,2),(2,4),(2,7),(0,8)],fill=skin[3])
        d.line([(6,2),(9,2),(11,3)],fill=skin[3])
        for x in (2,10):
            d.rectangle((x-1,7,x+3,10),fill=skin[1])
            d.line((x,7,x+3,7),fill=hair[0])
            d.rectangle((x,8,x+3,9),fill="#f6e6cb")
            pupil=x+(2 if x==2 else 1)
            d.line((pupil,8,pupil,9),fill=look["eye"])
            d.point((pupil,8),fill="#352c32")
            d.line((x,11,x+2,11),fill=skin[3])
            d.point((x-1 if x==2 else x+3,11),fill=skin[1])
        d.line((6,7,6,9),fill=skin[3])
        d.line((9,7,9,10),fill=skin[1])
        d.line((5,13,10,13),fill=hair[0])
    elif side in ("left","right","back"):
        if side=="back":
            d.polygon([(0,4),(3,5),(6,6),(12,5),(15,4),(15,15),(0,15)],fill=hair[1])
            for x in (1,5,10,13):
                d.line([(x,7),(x,10),(x+1,12)],fill=hair[2])
        else:
            back=15 if side=="left" else 0
            step=-1 if back==15 else 1
            d.polygon([(back,4),(back+step*5,5),(back+step*7,9),(back+step*6,15),(back,15)],fill=hair[1])


HAIR={"hair","beard","whisker","braid","cropped"}
SKIN={"skin","bicep","face","ear","nose","nose_tip"}
METAL={"brass","iron","steel","forge_ring","visor_rib","helm_plate","helm_band","bracer","clasp","buckle","visor","jade"}


def paint_face(image, mat, origin, size, face, look):
    w,h=(n*PIXELS_PER_UNIT for n in size)
    pal=look["skin"] if mat in SKIN else look["hair"] if mat in HAIR else PALETTES[mat]
    tile=Image.new("RGB",(w,h),pal[2])
    d=ImageDraw.Draw(tile)
    if mat=="book_cover" and face in ("up","down"):
        d.rectangle((0,0,w-1,h-1),fill=pal[1])
        d.rectangle((1,1,w-2,h-2),outline=pal[3])
        cx,cy=w//2,h//2
        d.line([(cx,cy-2),(cx+2,cy),(cx,cy+2),(cx-2,cy),(cx,cy-2)],fill=BRONZE[3])
    elif face=="down":
        d.rectangle((0,0,w-1,h-1),fill=pal[1])
    elif mat in HAIR:
        # Broad locks break into forks, with a single lit edge on each lock.
        for x in range(-4,w+3,4):
            bend=2 if mat in ("whisker","braid") else 1
            d.line([(x,0),(x,2),(x+bend,h//2),(x+bend,h-1)],fill=pal[1])
            d.line([(x+1,1),(x+1,2),(x+1+bend,h//2)],fill=pal[3])
        d.line((0,h-1,w//2,h-1),fill=pal[0])
    elif mat in METAL or mat=="hammerstone":
        d.polygon([(0,0),(w//2,0),(w//3,h//2),(0,h-2)],fill=pal[3])
        d.line((0,0,w-1,0),fill=pal[3 if face=="back" else 4])
        d.line((0,h-1,w-1,h-1),fill=pal[0])
        if w>=5 and h>=4:
            d.line((w-1,1,w-1,h-2),fill=pal[1])
        if mat=="visor_rib" and face=="front":
            d.line((0,1,0,h-2),fill=pal[3])
            d.line((w-1,1,w-1,h-2),fill=pal[1])
        elif mat=="bracer" and face=="front":
            for x in range(2,w-1,3):
                d.line([(x,1),(x,2),(x-1,h-2)],fill=pal[1])
        elif mat=="helm_band" and w>7:
            for x in range(2,w-1,5):
                d.point((x,0),fill=pal[1])
        elif mat=="buckle" and face=="front":
            d.rectangle((1,1,w-2,h-2),fill=HIDE[0])
            d.line((w//2,1,w//2,h//2),fill=pal[4])
        elif mat=="hammerstone" and w>5:
            d.line([(1,h-2),(3,h-3),(4,h-3)],fill=pal[1])
    elif mat=="mail":
        for y in range(0,h,3):
            for x in range((y%2)*2-2,w,4):
                d.line([(x,y),(x+1,y+1),(x+2,y)],fill=pal[3])
                d.point((x+1,y+2),fill=pal[0])
    elif mat in SKIN:
        if w>=3:
            d.polygon([(0,0),(w-2,0),(w//2,h//2),(0,h//2)],fill=pal[3])
        if h>2:
            d.line((w-1,h//2,w-1,h-1),fill=pal[1])
        if mat=="face":
            portrait(tile,face,look)
        elif mat=="bicep" and face in ("front","back","left","right"):
            d.line([(1,1),(w//2,1),(w//2+1,2)],fill=pal[3])
            d.line([(w-2,2),(w-2,h//2),(w-4,h-2)],fill=pal[1])
            d.line((1,h-1,w-3,h-1),fill=pal[1])
        elif mat=="ear" and face in ("right","left"):
            d.line((1,2,1,h-2),fill=pal[0])
        elif mat=="nose_tip" and face=="front":
            d.line((1,1,w-2,1),fill=pal[3])
            d.line((1,h-1,w-2,h-1),fill=pal[1])
    elif mat in ("coat","burgundy","felt","hem","trousers"):
        d.polygon([(0,0),(w//3,0),(w//3-1,h//2),(1,h-1),(0,h-1)],fill=pal[3])
        d.line([(w-2,0),(w-2,h//2),(w-3,h-1)],fill=pal[1])
        if mat=="hem" and w>=5 and h>=4:
            d.line([(0,h-3),(2,h-1),(4,h-3),(w-1,h-3)],fill=BRONZE[3])
    elif mat in ("leather","apron","pocket","glove","belt","binding","boot","sole"):
        d.line((0,0,w-1,0),fill=pal[3])
        d.line((0,1,0,h-1),fill=pal[3])
        d.line((0,h-1,w-1,h-1),fill=pal[0])
        if mat in ("pocket","apron","binding") and w>4 and h>3:
            for x in range(1,w-1,3):
                d.point((x,h-2),fill=pal[4])
        if mat=="apron" and face=="front" and h>=6:
            d.line([(w-4,1),(w-6,3),(w-6,4)],fill=pal[1])
    elif mat=="weave":
        for x in range(-2,w,4):
            d.line([(x,0),(x+2,h-1),(x+4,0)],fill=pal[4])
    elif mat in ("feather","quill","haft"):
        for x in range(0,w,3):
            d.line([(x,0),(x,h//2),(x+1,h-1)],fill=pal[3])
    elif mat=="paper":
        for y in range(1,h,2):
            d.line((1,y,w-1,y),fill=pal[1])
    image.paste(tile,tuple(n*PIXELS_PER_UNIT for n in origin))


def generate():
    rig=ASSETS/"town_models/deepstead_dwarf.json"
    rig.parent.mkdir(parents=True, exist_ok=True)
    rig.write_text(json.dumps(dict(textureSize=[ATLAS,ATLAS],parts=parts),separators=(",",":"))+"\n",encoding="utf8")
    path=ASSETS/"textures/entity/deepstead"
    path.mkdir(parents=True,exist_ok=True)
    for idx,look in enumerate(LOOKS):
        image=Image.new("RGBA",(ATLAS*PIXELS_PER_UNIT,ATLAS*PIXELS_PER_UNIT))
        for (material,w,h,d),(u,v) in patches.items():
            for name,dx,dy,fw,fh in (("up",d,0,w,d),("down",d+w,0,w,d),
                    ("right",0,d,d,h),("front",d,d,w,h),("left",d+w,d,d,h),("back",d+w+d,d,w,h)):
                paint_face(image,material,(u+dx,v+dy),(fw,fh),name,look)
        image.save(path/f"dwarf_{idx}.png",optimize=True)


if __name__=="__main__":
    generate()
