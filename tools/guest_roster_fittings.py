"""Costume cuts, hair fittings and palettes for the detailed town guests."""
import math
import numpy as np
from PIL import Image
from guest_portrait_models import Rig, TAU, rectangle, seasons, unit
from guest_skins import rgb
import guest_photo_outfits as photo
import guest_signature_outfits as signature

# Hair shadow, midtone and glint. Each cut references objects in the owner library.
# Width, sleeve ease and stature describe the clothed silhouette in town.
SPECS={
    # id               hair     ramp                         skin     iris      brow smile   outfit       width sleeve stature
    "emiru":           ([13,48],"17151a 32292b 665455","edd0c1","825a42",  2,1.0,"gothic_lace", .97,.98,1.01),
    "lauren_chen":     ([13,16],"191823 362c33 706064","dfb99f","664a39", -5,.75,"mariner", .90,.95,1.01),
    "brittany_venti":  ([5,16],"19181a 302823 61524a","d3a783","76553e",  3,.92,"wrap_skort",1.04,1.00,1.045),
    "aoc":             ([5],   "201a20 413032 795944","c39372","714b38", -9,1.18,"wrap",.99,1.0,1.025),
    "sarah_stock":     ([13,16],"766447 bb9a63 e7d7a0","efc9b1","658599", 2,1.14,"knit",.98,1.01,1.01),
    "pokimane":        ([5,48],"342323 674937 c4966d","deb793","83563a", 1,1.10,"satin_dress",1.00,.98,1.02),
    "power":           ([13,16],"95675e d9a795 f9d5bb","efcbb9","e8ab44",-16,1.6,"uniform",1.00,1.05,1.025),
    "reze":            ([0],   "211c2b 51405f 89719b","edc8b9","729e69", 4,.95,"source",1.,1.,1.),
    "asa_mitaka":      ([13,48],"18191f 32303c 696777","efcfc4","545749", 3,.23,"uniform",.98,.96,1.045),
    "himeno":          ([42,48],"18252a 344241 70827c","e9c5ad","74ab93",10,1.20,"halter",1.03,.98,1.055),
    "tokai_teio":      ([59,16],"573323 9e623f dba471","f1cbb8","876396",-6,1.45,"race",.95,1.0,150/160),
    "agnes_tachyon":   ([42,61],"533327 946044 c99576","f0cdb7","a94f5d",12,.83,"lab",.98,1.13,159/160),
    "gold_ship":       ([13,48],"717080 b9b8c7 e9e7ef","f1d5c5","b85873",-12,1.45,"naval",1.07,1.06,170/160),
    "oguri_cap":       ([5,16],"767789 c2c2d1 efecf0","edcebb","657eaa",5,.35,"race",1.03,1.01,167/160),
    "special_week":    ([47,16],"4c3236 7d5350 b17f70","efcbb9","8665a2",1,1.30,"race",.98,1.07,158/160),
    "rei_ayanami":     ([47,61],"66869d a8c5d7 e5eff1","efdad2","b24956",6,.14,"plugsuit",1.00,1.02,1.055)
}
NEXT_SIXTEEN=tuple(SPECS)
SPECS["asuka_langley"]=([13,16],"713422 b96134 e6a261","efd0bc","3d78b0",-12,.72,"plugsuit",1.02,1.04,1.075)
SPECS.update({
    "frieren": ([47,61],"878a95 cccfd0 f3f2eb","efd6c7","639783",4,.52,"mage",.95,1.05,.98),
    "rem": ([42,61],"516e9b 91bce1 d7e9f7","efd3c5","497aac",2,.88,"steward",.98,1.04,1.01),
    "megumin": ([23,42],"37282d 573c39 93675e","efcbbb","b4474f",-9,1.23,"expedition",.97,1.04,1.00),
    "zero_two": ([13,48],"a16b82 e1a6b7 f8d6db","f0cabc","50a49a",-8,1.39,"commander",1.,1.02,1.055),
    "rin_ishtar": ([47,16],"25202b 493841 735461","efcbbc","588cb2",-11,.65,"gem_mage",.98,1.00,1.015),
    "miyabi": ([13,48],"171d30 364054 656f86","edcbb9","a95055",-12,.32,"section_chief",1.,1.07,1.045),
    "nicole_demara": ([42,16],"975c7a dd92af fad2d9","eec5b5","74a479",0,1.35,"broker",1.,1.06,1.035),
})
PILOT_UNITS={"rei_ayanami":"00","asuka_langley":"02"}
RACERS=("tokai_teio","agnes_tachyon","gold_ship","oguri_cap","special_week")
# Shell, underlayer, lower garment, binding and accent. The palette is shared by the
# painted garment panels, shaped collars and independently editable accessories.
DYES={
 "emiru":("19171e","51464d","28232b","b6adad","bfc0c5"), "lauren_chen":("344762","e8e3d4","e1d6c6","ab7954","e9c995"),
 "brittany_venti":("e8e1cb","e2d8c1","89907a","69715d","645344"), "aoc":("6a344b","8b485e","663247","b78e69","e0b87a"),
 "sarah_stock":("46746a","314c46","353745","795d48","dcb27b"), "pokimane":("42444b","262830","3c3d43","62646a","b5a083"),
 "power":("2e425f","eee7db","353644","d3ac7c","b04e4b"),"reze":("efebe0","f4efe7","50434d","846951","b3a782"),
 "asa_mitaka":("4a505b","e5ddcd","34343d","93654c","cfb78c"),"himeno":("332f3d","565063","343342","292630","d5c7ba"),
 "tokai_teio":("f4efe2","31568b","e9e0cb","3c76b1","d99447"),"agnes_tachyon":("ece8d8","c5a266","493d41","863c49","c19056"),
 "gold_ship":("9b3747","f0e4c9","913442","4d3940","d7ad68"),"oguri_cap":("e8e4dc","3a4d80","e5e1d9","526895","c3a467"),
 "special_week":("f0e5d8","735287","ece0da","8d68a2","cd9aaa"),"rei_ayanami":("e1e8e8","354454","b3c7d2","ad453f","60d4ba"),
 "asuka_langley":("c63932","3c3039","942e32","df7940","78d0a0")}
DYES.update({
    "frieren":("f1ede2","dedbd6","e4e0dc","b69a59","a94255"),
    "rem":("292534","f2eee7","302c3d","dad2db","b185a9"),
    "megumin":("a13b38","422e34","8e3638","d6ae5a","de654e"),
    "zero_two":("af3749","733346","3d3447","e8e0d2","deb56c"),
    "rin_ishtar":("a43541","762d3a","393440","e2d9c9","a5d9dc"),
    "miyabi":("304052","d8e1e1","283140","6caaa8","bd614c"),
    "nicole_demara":("deddd4","383341","423846","96bc80","d89eb6"),
})
JAW=(.94,.91,.97,.93,.95,.97,.96,.98,.92,.91,.96,.95,.95,.94,.98,.93,.95,
     .94,.95,.92,.96,.93,.93,.96)
# Art-directed game fittings. Named entries keep a new guest from silently inheriting a mannequin.
FITTINGS={
    "emiru":"small_medium", "lauren_chen":"small_small", "brittany_venti":"large_medium",
    "aoc":"medium_medium", "sarah_stock":"medium_athletic", "pokimane":"medium_medium",
    "power":"medium_athletic", "reze":"source", "asa_mitaka":"small_medium", "himeno":"large_full_thick",
    "tokai_teio":"small_athletic", "agnes_tachyon":"small_medium", "gold_ship":"medium_athletic",
    "oguri_cap":"medium_athletic", "special_week":"small_athletic",
    **dict.fromkeys(PILOT_UNITS,"medium_medium"),
    "frieren":"small_small","rem":"medium_medium","megumin":"small_athletic",
    "zero_two":"medium_athletic","rin_ishtar":"small_medium","miyabi":"medium_athletic",
    "nicole_demara":"large_medium"}
LOOKS={g:(FITTINGS[g],sp[0],sp[1].split(),sp[2],sp[3],sp[4],sp[5]) for g,sp in SPECS.items()}
JAW=dict(zip(SPECS,JAW))
def smooth(x):
    t=np.clip(x,0,1)
    return t*t*(3-2*t)

def hair_fit(points,g,num):
    p=points.copy()
    if num==0:return p
    # Width about the temples, length below the chin, forward swing.
    shape={"emiru":(.87,1.51,.06),"lauren_chen":(.86,.96,.03),"brittany_venti":(.86,.54,.09),
           "aoc":(.80,.80,.025),"sarah_stock":(.88,.91,.05),"pokimane":(.90,1.18,.09),
           "power":(.92,1.17,.04),"asa_mitaka":(.82,.74,.04),"himeno":(.85,.56,.0),
           "tokai_teio":(.83,.74,.04),"agnes_tachyon":(.88,.77,.0),"gold_ship":(.89,1.35,.04),
           "oguri_cap":(.90,1.20,.06),"special_week":(.90,.86,.03),"rei_ayanami":(.86,.71,.015),
           "asuka_langley":(.95,1.78,.04),"frieren":(.87,.68,.05),"rem":(.87,.49,.03),
           "megumin":(.88,.70,.02),"zero_two":(.87,1.55,.06),"rin_ishtar":(.85,.80,.04),
           "miyabi":(.84,1.32,.05),"nicole_demara":(.90,.67,.03)}
    w,length,swing=shape[g]
    p[:,0]*=w
    p[:,1]+=(length-1)*np.maximum(p[:,1]+.02,0)
    p[:,2]+=swing*smooth((p[:,1]-.10)/1.0)
    if num==48 and g in ("emiru","asa_mitaka","gold_ship"):
        p[:,1]-=.095*smooth((p[:,1]+.45)/.55)
    if num==16 and g in ("power","lauren_chen","brittany_venti","tokai_teio","special_week"):
        p[:,0]+=.12*smooth((p[:,1]+.55)/.77)
        p[:,1]+=.23*p[:,0]*smooth((p[:,1]+.40)/.55)
    if g=="asuka_langley" and num==16:
        p[:,1]-=.53*np.maximum(0,points[:,1]+.06)
        p[:,0]+=.14*smooth((points[:,1]+.55)/.65)
    if g in ("brittany_venti","pokimane") and num==5:
        amp=smooth((p[:,1]+.4)/.8)
        wave=(p[:,1]+.23)*(10 if g=="brittany_venti" else 7)+2.3*p[:,0]
        p[:,0]+=.095*amp*np.sin(wave)
        p[:,2]+=.075*amp*np.cos(wave)
    if g=="brittany_venti" and num==5:
        # Sweep the long donor sections up into the tie behind the crown.
        fall=smooth((points[:,1]+.28)/1.46)
        p[:,0]*=1-.68*fall
        p[:,2]+=fall*(.59-.45*p[:,2])
        p[:,1]-=.38*np.maximum(0,points[:,1]+.24)
    if g=="emiru" and num==48:
        # The front fringe clears the lashes while the back falls past the waist.
        p[:,1]-=.75*np.maximum(0,points[:,1]+.08)
        p[:,0]+=.16*smooth((points[:,1]+.48)/.68)
    if g=="brittany_venti" and num==16:
        p[:,1]-=.33*np.maximum(0,points[:,1]+.04)
        p[:,0]+=.13*smooth((points[:,1]+.54)/.55)
    if g=="brittany_venti" and num in (5,16):
        front=smooth((-points[:,2]-.16)/.25)*smooth((points[:,1]+.58)/.44)
        distance=p[:,0]+.045
        p[:,1]-=.34*front*np.exp(-(distance/.38)**2)
        p[:,0]+=.15*front*np.tanh(8*distance)
    if g=="himeno" and num==48:
        p[:,0]-=.20*smooth((p[:,1]+.52)/.52)
    if g=="rem" and num==61:
        fringe=smooth((p[:,1]+.66)/.62)*smooth((-p[:,2]-.12)/.27)
        p[:,0]+=.24*fringe
        p[:,1]+=.27*fringe*smooth((p[:,0]+.29)/.44)
    if g in ("agnes_tachyon","rei_ayanami") and num==61:
        p[:,2]-=.045
        p[:,1]-=.145*smooth((p[:,1]+.62)/.8)
    p[:,0]+=points[:,0]*(1-w)*.90*(1-smooth((p[:,1]+.20)/.55))
    if num in (48,16,42) and g not in ("himeno","sarah_stock"):
        fringe=smooth((p[:,1]+.36)/.43)*(1-smooth((abs(p[:,0])-.33)/.19))*(1-smooth((p[:,2]+.45)/.22))
        p[:,1]-=.285*fringe
        p[:,0]+=.078*np.tanh(7*p[:,0])*fringe
    return p

def swatches(atlas,g,box):
    base,shirt,lower,trim,accent=DYES[g]
    skin=SPECS[g][2]; hair=SPECS[g][1].split()
    colors=dict(skin=skin,hair=hair[1],strand=hair[2],top=shirt,pants=lower,coat=base,boot="52444b",
        trim=trim,leather="674c43",metal="b8bdc7",gold=accent,white="f7efe1",black="292832",
        visor="252730",brocade=base,lace="eee7e3",scarf="56828a",harvest="b57745",spring="de9bad",
        lens="799caa",rib=base,knit=base,lining=shirt,sole="35303b",button=accent,glove=skin,
        lining_dark="45414e",ribbon=trim,jewel=accent)
    shoes={"emiru":"24212b","pokimane":"373439","brittany_venti":"ecebe5","lauren_chen":"775948","sarah_stock":"725446","himeno":"302d39"}
    colors["boot"]=signature.SHOES.get(g,shoes.get(g,colors["boot"]))
    if g in PILOT_UNITS:
        colors.update(boot=lower, sole=shirt, glove=shirt, metal="8b9aa3", visor=shirt)
    if g=="brittany_venti":colors.update(sole="d2d1c9",button="675545",ribbon="b9b4a3")
    if g=="pokimane":colors.update(sole="28262d",metal="b39a74")
    tex=Image.new("RGB",(512,512),rgb(base))
    for k,color in colors.items():
        x,y,w,h=rectangle(k); shade=np.asarray(rgb(color))
        yy,xx=np.mgrid[:h,:w]
        texture=(2.3*np.cos((xx+.23*yy)*1.3)+1.6*np.sin(yy*1.42)) if k in ("knit","rib") else np.zeros((h,w))
        if k in ("hair","strand"):
            texture=11*np.cos((xx/w+.035*np.sin(yy/h*3))*math.tau*4)+3*np.cos(xx*1.5)
        if k in ("metal","gold","button","jewel"): texture=22*np.exp(-((yy/h-.28)/.16)**2)-3
        tex.paste(Image.fromarray(np.uint8(np.clip(shade+texture[:,:,None],0,255))),(x,y))
    atlas.paste(tex,box[:2])

def prepare_limbs(model,g):
    sleeve=SPECS[g][8]
    for part in model['parts']:
        bone=part['name']
        if not part['vertices'] or bone=='body':continue
        v=np.array(part['vertices']); p=v[:,:3]+part['pose']; x,y,z=p.T
        side=1 if bone.startswith("left") else -1
        if bone.endswith("arm") and SPECS[g][6] not in photo.STYLES:
            cx=side*np.interp(y,[1.7,3.3,6.9,10.7,12.9,14],[2.6,3.2,4.05,4.96,5.50,5.97])
            cz=np.interp(y,[2,7,10.8,14],[-.21,-.31,-.70,-1.26])
            factor=1+(sleeve-1)*(1-smooth((y-10.7)/1.8))
            x[:]=cx+(x-cx)*factor;z[:]=cz+(z-cz)*factor
            v[:,5]/=factor;v[:,7]/=factor
        v[:,:3]=p-part['pose'];v[:,5:]=unit(v[:,5:])
        if bone.endswith("leg"):
            # Fit the skirt to the standing stance that will actually render.
            a=np.deg2rad(4.0)*side
            rotation=np.array([[np.cos(a),np.sin(a),0],[-np.sin(a),np.cos(a),0],[0,0,1]])
            v[:,:3]=v[:,:3]@rotation.T
            v[:,5:]=v[:,5:]@rotation.T
        part['vertices']=v.round(7).tolist()

def pilot_textures(atlas, guest, boxes):
    """Continuous pressure suit fabric, ceramic armor, seals and unit markings."""
    shell, liner, panel, trim, light = map(rgb, DYES[guest])
    digits = {"0": ("111", "101", "101", "101", "111"),
              "2": ("111", "001", "111", "100", "111")}
    for region, (x, y, width, height) in boxes.items():
        row, col = np.mgrid[:height, :width]
        level = row * 24.1 / (height - 1)
        angle = (col / (width - 1) - .5) * TAU
        front = np.cos(angle)
        side = np.abs(np.sin(angle))
        color = np.zeros((height, width, 3), dtype=float)
        color[:] = shell
        if region == "torso":
            yoke = 3.24 + .92 * (1 - side)
            color[level < yoke] = liner
            shoulder_plate = (front > -.28) & (side > .34) & (level > 2.0 + .6*side) & (level < yoke)
            color[shoulder_plate] = trim if guest == "asuka_langley" else panel
            color[np.abs(level - yoke) < .10] = trim
            for rib in (6.40, 7.24, 8.08):
                edge = rib - .68 * side
                color[(np.abs(level-edge) < .15) & (side > .45)] = panel
            # Offset seams on the side and rear keep the front unit plate readable.
            channel = .73 + .10 * np.cos((level - 5) * .69)
            color[(side > channel) & (level > yoke)] = panel
            color[(np.abs(side - channel) < .035) & (level > yoke)] = liner
            belt = 9.05 + .30 * np.cos(2 * angle)
            color[np.abs(level - belt) < .40] = liner
            color[np.abs(level - belt + .28) < .08] = panel
            color[(front < -.35) & (level < 8.45) & (level > 2.75)] = panel
            for vent in (4.6, 5.25, 5.9):
                color[(front < -.75) & (np.abs(level - vent) < .13)] = liner
            # A small horizontal unit plate uses the same angular UV as the torso.
            chest = (front > .95) & (level > 4.18) & (level < 5.60)
            number_ink = liner
            for index, digit in enumerate(PILOT_UNITS[guest]):
                for v, line in enumerate(digits[digit]):
                    for u, bit in enumerate(line):
                        if bit == "1":
                            x0 = -.25 + index * .28 + u * .073
                            y0 = 4.37 + v * .21
                            color[chest & (angle >= x0) & (angle < x0 + .079)
                                  & (level >= y0) & (level < y0 + .216)] = number_ink
        elif region == "arm":
            color[level < 4.3] = panel
            elbow = np.abs(level - 7.05) + .68 * np.maximum(0,front)
            color[elbow < 1.38] = liner
            color[(level >= 7.95) & (level < 8.3)] = trim
            color[(level > 7.95) & (level < 10.9) & (side > .65)] = panel
            color[level >= 11.23] = liner
            color[(level > 10.92) & (level < 11.22)] = trim
            color[(level > 8.85) & (level < 9.22) & (front > .55)] = light
        else:
            thigh = .47 + .28 * np.cos((level-12) * .74)
            color[(side > thigh) & (level < 17)] = panel
            color[(np.abs(side - thigh) < .045) & (level > 12.40) & (level < 15.55)] = liner
            knee = 16.32 + .51 * (1 - front)
            color[np.abs(level - knee) < 1.13] = liner
            color[(front > .34) & (np.abs(level - knee) < .77*front)] = shell
            shin = .60 + .13 * np.cos((level-18.15)*1.02)
            color[(level >= 17.72) & (side > shin)] = panel
            color[(level >= 18.02) & (level < 21.73) & (np.abs(side-shin)<.055)] = liner
            color[level >= 21.73] = panel
            color[np.abs(level - 21.73) < .15] = trim
            color[level > 23.55] = liner
        grain = 1.1 * np.sin(col * .73) * np.sin(row * .82)
        atlas.paste(Image.fromarray(np.uint8(np.clip(color + grain[:, :, None], 0, 255))), (x, y))


def garment_textures(atlas,g,boxes,torso):
    shell,inner,lower,trim,accent=map(rgb,DYES[g])
    kind=SPECS[g][6];skin=rgb(SPECS[g][2])
    if g in signature.GUESTS:
        signature.paint(atlas,g,DYES[g],skin,boxes)
        return
    if kind in photo.STYLES:
        photo.paint(atlas,kind,DYES[g],SPECS[g][2],boxes,torso)
        return
    if g in PILOT_UNITS:
        pilot_textures(atlas,g,boxes)
        return
    boot_dyes={"tokai_teio":"e9ded2","oguri_cap":"333e5b","special_week":"74516f","gold_ship":"513949","agnes_tachyon":"584543"}
    boot_dyes.update(emiru="c7b5c6",pokimane="dfc6c0",sarah_stock="7c5b4a",lauren_chen="725748",himeno="423d49")
    shoes=rgb(boot_dyes.get(g,"6a5249" if kind=="pinafore" else "393641"))
    for region,box in boxes.items():
        x0,y0,w,h=box
        yy,xx=np.mgrid[:h,:w]
        y=yy*24.1/(h-1);a=(xx/(w-1)-.5)*math.tau
        sn=np.sin(a);front=np.cos(a)>.01
        cloth=np.zeros((h,w,3),float)
        def dye(c,mask):
            cloth[mask]=c
        if region=="arm":
            cloth[:]=shell
            if kind in ("pinafore","varsity"):cloth[:]=inner
            cuff={"lab":12.45,"mariner":8.9,"wrap":7.4,"vest":2.82,"halter":2.48,"tennis":6.65}.get(kind,11.32)
            dye(trim,(y>cuff-.49)&(y<cuff-.27))
            dye(skin,y>cuff)
            if kind in ("race","naval","varsity","tennis"):
                dye(accent,(y>5.6)&(y<5.92));dye(trim,(y>6.12)&(y<6.38))
            if g in RACERS:
                cap_height={"tokai_teio":3.95,"agnes_tachyon":0,"gold_ship":4.35,"oguri_cap":4.65,"special_week":4.05}[g]
                dye(inner,y<cap_height)
                dye(accent,abs(y-cap_height)<.10)
            if g=="power":
                dye(inner,(y>8.18)&(y<9.12))
                dye(skin,y>=9.12)
            if kind=="mariner":
                dye(inner,(y>4.1)&(y<cuff)&((y-4.1)%.92<.32))
        elif region=="leg":
            cloth[:]=lower
            if kind=="pinafore":cloth[:]=rgb("393745")
            if kind=="wrap":cloth[:]=rgb("52444c")
            if kind in ("varsity","tennis","halter"):
                cloth[:]=rgb("4b4354" if kind!="tennis" else "ece2dc")
                if kind=="halter":
                    hem=13.12+.27*np.cos(2*a)
                    dye(skin,y<15.65)
                    dye(rgb("382e40"),(y>=15.65)&(y<16.02))
                    dye(lower,y<hem)
                    dye(rgb("817285"),(y>hem-.22)&(y<hem-.08))
            if kind in ("mariner","knit","vest") or g=="power":
                dye(np.asarray(lower)*.74,(abs(sn)>.966)&(y>10.7))
                pocket=11.4+.98*np.maximum(0,abs(sn)-.4)
                dye(np.asarray(lower)*1.35,front&(abs(sn)>.55)&(abs(y-pocket)<.09))
                back_seam=12.45+.36*np.cos(4*a)
                dye(np.asarray(lower)*.84,~front&(abs(y-back_seam)<.07)&(abs(sn)<.65))
                dye(np.asarray(lower)*1.09,front&(abs(sn)<.035)&(y>13.8)&(y<20.1))
            if g=="asa_mitaka":cloth[:]=rgb("3c3945")
            boots=17.5 if g in RACERS else {"halter":18.2,"varsity":21.6,"tennis":21.6,"knit":19.2}.get(kind,21.0)
            dye(shoes,y>boots);dye(accent if g in RACERS else rgb("6e6265"),(y>boots+.18)&(y<boots+.35))
            dye(rgb("292834"),y>23.62)
            if g in RACERS or kind in ("varsity","tennis"):
                laces=(np.abs(np.abs((y-boots-.42)%.85-.425)-.32*np.abs(sn))<.055)
                dye(rgb("d7cfc7"),laces&front&(abs(sn)<.44)&(y>boots+.67)&(y<23.1))
        else:
            cloth[:]=shell
            if kind in ("blazer","suit","lab","uniform"):
                opening=np.interp(y,[1.3,2.5,5.9,8.7,11.8],[.28,.56,.16,.08,.015])
                dye(inner,front&(abs(sn)<opening))
                dye(trim,front&(abs(abs(sn)-opening)<.036)&(y<8.78))
                for h1 in (6.9,8.3,9.7):
                    dye(accent,front&((sn-.21)**2/.036**2+(y-h1)**2/.12**2<1))
                dye(accent,front&(abs(sn+.62)<.20)&(abs(y-7.15-.22*sn)<.09))
                if g in ("power","himeno","agnes_tachyon"):
                    dye(trim,front&(abs(sn)<np.interp(y,[2,2.6,3,6.6,7.0],[.040,.12,.066,.14,.001]))&(y>2.15)&(y<7))
            if kind=="pinafore":
                dye(inner,(y<3.6)&(abs(sn)<.71))
                dye(trim,front&(abs(sn)<np.clip(.4-(y-2.65)*.18,0,.44))&(y>2.55)&(y<4.68))
            if kind=="blouse":
                dye(trim,front&(abs(sn)<.037)&(y>2.25)&(y<5.1))
                for h1 in (4.8,6.2,7.6):dye(accent,front&(abs(sn)<.045)&(abs(y-h1)<.10))
            if kind=="hoodie":
                dye(inner,front&(y>7.62+.85*abs(sn))&(y<9.65)&(abs(sn)<.69))
                dye(trim,front&(abs(abs(sn)-.25)<.028)&(y>2.3)&(y<5.70+.55*sn))
                dye(accent,front&(abs(abs(sn)-.25)<.035)&(abs(y-5.7-.55*sn)<.15))
            if g in RACERS and kind!="lab":
                dye(inner,(abs(sn)>.65)&(y>2.15))
                if g in ("tokai_teio","gold_ship"):
                    dye(trim,(abs(y-(5.8+2.5*sn))<.32)&front)
                    dye(accent,(abs(y-(5.23+2.5*sn))<.09)&front)
                elif g=="oguri_cap":
                    dye(inner,(y<3.50+1.25*(1-abs(sn)))&(y>1.60))
                    dye(accent,abs(y-(3.50+1.25*(1-abs(sn))))<.11)
                else:
                    dye(trim,front&(abs(abs(sn)-.50)<.10)&(y>2.1))
                    dye(accent,front&(abs(abs(sn)-.64)<.035)&(y>2.1))
                for h1 in (3.6,4.55,7.45,8.40):
                    dye(accent,front&(abs(abs(sn)-.37)<.047)&(abs(y-h1)<.13))
            dye(skin,(y<1.60))
            if kind!="lab":
                dye(trim,(y>9.5)&(y<9.75))
            if kind in ("blouse","blazer","suit","uniform","race"):
                dye(lower,y>=9.76)
            if kind in ("varsity","tennis"):
                zipline=.03 if kind=="varsity" else .016
                dye(inner,front&(abs(sn)<zipline)&(y>2.0)&(y<8.9))
                dye(inner,front&(abs(y-(3.0+.64*abs(sn)))<.16))
                dye(trim,(y>8.75)&(y<9.40))
                mx=(sn-.50)/.14
                my=(5.0-y)/.44
                if g=="emiru":
                    motif=(mx**2+my**2-.77)**3<mx**2*my**3
                else:
                    motif=(mx**2+my**2<1)&((mx-.44)**2+(my-.20)**2>.65)
                dye(accent,front&motif)
            if kind=="mariner":
                dye(inner,(y>3.1)&(y<8.65)&((y-3.1)%.97<.32))
            if kind in ("knit","mariner"):
                rib=(abs(np.sin(45*a))>.88)&(y>2.54)&(y<9.10)
                cloth[rib]*=.94
            if kind=="knit":
                line=3.02+.30*np.cos(a)
                dye(inner,(y<line)&(y>2.15))
                dye(trim,np.abs(y-line)<.10)
            if kind=="wrap":
                diagonal=4.9+2.8*sn
                dye(inner,front&(y<diagonal)&(y>2.12))
                dye(accent,front&(abs(y-diagonal)<.095))
            if kind=="vest":
                vneck=3.85-2.2*abs(sn)
                dye(inner,front&(y<vneck)&(y>1.65))
                for pos in (5.,6.4,7.8):
                    dye(accent,front&((abs(sn)-.32)**2/.061**2+(y-pos)**2/.17**2<1))
            if kind in ("knit","mariner","vest","wrap"):
                dye(lower,y>=9.48)
                dye(trim,(y>=9.20)&(y<9.58))
            if kind=="knit":
                hem=7.22+.12*np.cos(2*a)
                dye(inner,(y>hem-.22)&(y<=hem))
                photo.paint_midriff(cloth,skin,a,y,hem,9.20)
            if kind=="halter":
                neckline=np.where(front,2.5+1.5*abs(sn)**1.4,3.76)
                dye(skin,y<neckline)
                straps=front&(abs(abs(sn)-np.clip(.25+(y-1.35)*.18,.24,.65))<.068)
                dye(shell,straps&(y>=1.30)&(y<neckline))
                dye(accent,abs(y-neckline)<.07)
                overlap=neckline+.68+.24*np.sin(2*a)
                dye(rgb("80647e"),(y>=neckline)&(y<overlap))
                dye(rgb("c1a4b1"),abs(y-overlap)<.085)
                fold=5.40+.33*np.sin(a*2)+.21*sn
                dye(inner,front&(abs(y-fold)<.07)&(abs(sn)<.71))
                dye(inner,(y>6.68)&(y<7.06))
                dye(skin,(y>=7.06)&(y<9.12))
                hip_band=9.58-1.48*abs(sn)-.15*np.maximum(0,-np.cos(a))
                dye(rgb("422e43"),abs(y-hip_band)<.16)
                dye(lower,y>=9.12)
                dye(trim,(y>=9.12)&(y<9.55))
            if kind=="pinafore":
                dye(inner,front&(abs(sn)<.62)&(y<4.25-1.75*abs(sn)))
                dye(trim,front&(abs(sn)<.10)&(y>2.7)&(y<4.65))
        if g in RACERS and kind!="lab":
            if region=="torso":
                # Contrasting race bib, visible to timekeepers on both sides of the circuit.
                center=np.arcsin(sn)
                bib=(abs(center)<.31)&(y>5.77)&(y<7.64)
                dye(rgb("eee8d9"),bib)
                numbers={"tokai_teio":("110","001","010","100","111"),
                         "gold_ship":("111","001","011","001","111"),
                         "oguri_cap":("101","101","111","001","001"),
                         "special_week":("111","100","111","001","111")}
                for j,row in enumerate(numbers[g]):
                    for i,bit in enumerate(row):
                        if bit=="1":
                            left=-.172+i*.115; top=6.03+j*.255
                            dye(rgb("302e3b"),bib&(center>=left)&(center<left+.117)&(y>=top)&(y<top+.258))
            if region=="leg":
                dye(inner,(abs(sn)>.75)&(y<17.45))
                dye(accent,(abs(abs(sn)-.75)<.048)&(y<17.45))
        light=1.8*np.cos(a*4+.2*y)+.8*np.sin(xx*1.87+yy*.31)*np.sin(yy*1.62)
        if kind in ("knit","mariner") and region!="leg":
            light+=1.8*np.cos(xx*.67+.28*np.sin(yy*.34))
        img=Image.fromarray(np.uint8(np.clip(cloth+light[:,:,None],0,255)))
        atlas.paste(img,(x0,y0))

def outer_surface(tri,x,y):
    a,b,c=tri[:,0],tri[:,1]-tri[:,0],tri[:,2]-tri[:,0]
    dx,dy=x-a[:,0],y-a[:,1]
    den=b[:,0]*c[:,1]-b[:,1]*c[:,0]
    valid=abs(den)>1e-7
    div=np.where(valid,den,1)
    u=(dx*c[:,1]-dy*c[:,0])/div
    v=(b[:,0]*dy-b[:,1]*dx)/div
    inside=valid&(u>=0)&(v>=0)&(u+v<=1)
    return (a[:,2]+b[:,2]*u+c[:,2]*v)[inside].min() if inside.any() else None


def add_costume(model,atlas,g,charts):
    """Build fitted layers on the same moving joints as the original guest models."""
    from guest_roblox_portraits import chart, part_add, surface, envelope
    style=SPECS[g][6]; garment_width=SPECS[g][7]
    parts={p["name"]:p for p in model["parts"]}
    for name,parent in [("headwear","head")]+([("horse_tail","body")] if g in RACERS else []):
        p=dict(name=name,parent=parent,pose=[0,0,0],vertices=[],faces=[])
        parts[name]=p;model["parts"].append(p)
    r=Rig(g)
    for name,p in parts.items():r.joint(name,p["parent"],p["pose"])
    seasons(r)
    for name,p in r.parts.items():
        if not p["vertices"]:continue
        v=np.array(p["vertices"])
        if "collar" in name:v[:,:3]+=[0,1.85,-.42]
        if "_arm" in name:v[:,:3]+=[1.13 if "left" in name else -1.13,.10,-.3]
        if "_pin" in name:v[:,:3]+=[.03,.45,-.60]
        p["vertices"]=v.tolist()
    # Keep the imported triangles and normals. Fitted clothing is painted on
    # their UV chart; belts and trim follow sections of that same moving surface.
    angles=np.arange(48)*TAU/48
    torso=surface(parts["body"])

    def ring(y,plus=0):
        return envelope(torso,y,angles,plus)

    def chest_point(x,y,clearance):
        z=outer_surface(torso,x,y)
        if z is None:
            # Shoulder trim can reach the arm seam just beyond the torso.
            profile=ring(y)
            edge=profile[np.argmax(profile[:,0]*np.sign(x))]
            return [edge[0],y,edge[2]-clearance]
        return [x,y,z-clearance]

    def band(mat,y0,y1,extra=.07):
        r.grid('body',mat,[ring(y0,extra),ring(y1,extra)],caps=False)
    def ribbon(bone,mat,path,w,d=.09):
        if bone=="body":
            path=[chest_point(x,y,d) for x,y,_ in path]
        r.sweep(bone,mat,path,w,d,steps=10,n=8,shape=lambda t:.98-.23*t)
    def badge(bone,mat,p,rx,ry,depth=.09):r.orb(bone,mat,p,(rx,ry,depth),12)
    if style in photo.STYLES:
        photo.skirt(parts,r,style)
        if style=="gothic_lace":
            r.loft("body","black",[(.95,1.12,1.03,1.05),(1.17,1.15,1.06,1.08)],(0,0,-.32),n=28)
            chain=[ring(y,.11)[i] for y,i in ((2.68,-5),(2.91,-3),(3.07,0),(2.91,3),(2.68,5))]
            r.cord("body","metal",chain,.052)
            badge("body","jewel",chain[2]+[0,.14,0],.16,.19,.07)
        if style=="satin_dress":
            for sign in (-1,1):
                points=[ring(y,.10)[i*sign] for y,i in ((1.94,4),(2.20,6),(3.35,4),(2.56,1))]
                points=np.array(points)
                r.grid("body","lining",[np.vstack((points[0],points[1],points[1]+[0,0,.04],points[0]+[0,0,.04])),
                    np.vstack((points[3],points[2],points[2]+[0,0,.04],points[3]+[0,0,.04]))],caps=False)
        if style=="wrap_skort":
            for bone in ("left_leg","right_leg"):
                tri=surface(parts[bone]);pivot=np.asarray(parts[bone]["pose"])
                for y in (22.78,23.06):
                    sole=envelope(tri,y,angles,.09)-pivot
                    eye=np.array([sole[i] for i in (-3,0,3)])
                    eye[:,2]-=.45
                    r.cord(bone,"white",eye,.069)
    if style == "plugsuit":
        r.loft("body","visor",[(.42,1.08,1.05,1.06),(.73,1.32,1.14,1.18),
                               (1.51,1.38,1.22,1.23),(1.89,1.26,1.12,1.12)],(0,0,-.28),n=32)
        r.loft("body","trim",[(1.29,1.39,1.24,1.25),(1.46,1.39,1.24,1.25)],(0,0,-.28),n=32)
        for sign in (-1,1):
            # Each chest connector follows the upper yoke and has an inset indicator.
            connector=np.array(chest_point(sign*2.14,3.42,.11))
            badge("body","visor",connector,.56,.45,.22)
            badge("body","jewel",connector+[0,-.01,-.19],.32,.22,.09)
            for material, offset, width in (("visor",0,.31),("trim",.04,.10)):
                ribbon("body",material,[(sign*2.77,2.83,-.85-offset),(sign*2.82,3.33,-1.22-offset),
                                       (sign*2.59,3.95,-1.54-offset),(sign*2.13,4.35,-1.81-offset)],width)
        # The back connector pack and rigid vents sit wholly on the body joint.
        pack_z=max(ring(y)[:,2].max() for y in (2.72,4.30,5.92))-.19
        r.loft("body","visor",[(2.26,1.01,.28,.29),(2.72,1.69,.51,.52),(5.92,1.56,.51,.58),
                               (7.40,1.08,.32,.40)],(0,0,pack_z),n=20,exp=.57)
        for y in (3.49,4.30,5.11):
            r.loft("body","metal",[(y,1.04,.12,.19),(y+.22,1.06,.12,.19)],(0,0,pack_z+.52),n=18,exp=.55)
        for bone in ("left_arm","right_arm","left_leg","right_leg"):
            tri=surface(parts[bone])
            pivot=np.asarray(parts[bone]["pose"])
            levels=((2.62,.05),(2.89,.25),(3.52,.29),(4.05,.17),(4.25,.05)) if bone.endswith("arm") else (
                    (15.70,.045),(16.02,.17),(16.54,.24),(17.07,.16),(17.35,.045))
            rows=[envelope(tri,y,angles,clearance)-pivot for y,clearance in levels]
            r.grid(bone,"coat" if bone.endswith("arm") else "boot",rows,caps=False)
            if bone.endswith("leg"):
                guard=[]
                for y,thickness in ((17.22,.09),(17.66,.19),(18.51,.24),(20.26,.19),(21.60,.085)):
                    row=envelope(tri,y,angles,.045)-pivot
                    row[:,2]-=thickness*np.maximum(0,np.cos(angles))**2
                    guard.append(row)
                r.grid(bone,"boot",guard,caps=False)
            edge=np.stack(rows[-2:])
            edge[:,:,[0,2]]*=1.004
            r.grid(bone,"visor",edge,caps=False)
    if style in ("pinafore","varsity","tennis","lab","wrap") or g=="asa_mitaka":
        end={"varsity":15.7,"tennis":15.4,"pinafore":17.85,"lab":17.7,"wrap":16.72,"uniform":16.28}[style]
        flare={"varsity":1.50,"tennis":1.36,"pinafore":1.19,"lab":.48,"wrap":.16,"uniform":.10}[style]
        ys=[*np.arange(9.35,end-.18,.54),end-.14,end];skirt=[]
        under=np.concatenate([surface(parts[k]) for k in ("body","left_leg","right_leg")])
        direction=np.column_stack((np.sin(angles),-np.cos(angles)))
        for y in ys:
            t=(y-9.35)/(end-9.35)
            rad=(3.32+(flare+.56)*t+.19*np.sin(t*np.pi))*garment_width
            pleat=(.31 if style in ("pinafore","tennis","varsity","race") else .12)*t*np.cos(angles*12)
            depth=1.93+flare*t*.69+.46*t+pleat
            designed=1/np.sqrt((direction[:,0]/(rad+pleat))**2+(direction[:,1]/depth)**2)
            origin=(0,-.14)
            fitted=envelope(under,y,angles,.21,origin)[:,[0,2]]-origin
            shape=(np.maximum(designed,np.linalg.norm(fitted,axis=1))+.04)[:,None]*direction
            sweep=(.64*np.sin(angles-.30)*t**2 if style=="wrap" else (.10+.08*t)*t*np.cos(angles*10))
            skirt.append(np.c_[shape[:,0],y+sweep,shape[:,1]-.14])
        r.grid('body','pants' if style!="lab" else 'coat',skirt,caps=False)
        bottom=np.stack(skirt[-2:]);bottom[:,:,[0,2]]*=1.007
        r.grid('body','trim',bottom,caps=False)
        if style=="wrap":
            # A bias-cut overlap crosses the skirt and meets the gold waist clasp.
            seam=[]
            for i,row in enumerate(skirt):
                angle=.18+.89*i/(len(skirt)-1)
                idx=angle*len(angles)/TAU
                k=int(idx)
                p=(1-idx%1)*row[k]+(idx%1)*row[k+1]
                seam.append(p+[0,0,-.048])
            r.cord("body","trim",seam,.052)
        if style=="lab":
            # Cut the pockets out of the same cloth rows, then offset them along the apron.
            for s in (-1,1):
                pocket=[]
                ids=np.arange(2,8) if s==1 else np.arange(-7,-1)
                for y in (12.24,12.75,13.29,13.85):
                    row=skirt[np.argmin(abs(np.asarray(ys)-y))][ids].copy()
                    row[:,2]-=.13
                    pocket.append(np.concatenate([row,row[::-1]+[0,0,.065]]))
                r.grid("body","lace",pocket,caps=False)
                r.grid("body","leather",[pocket[0]-[0,.09,.02],pocket[0]+[0,.04,-.02]],caps=False)
        if style in ("varsity","tennis"):
            r.grid("body","lining",np.array(skirt[-4:-2])*[1.014,1,1.014],caps=False)
    if g=="asa_mitaka":
        # The archive pass hangs from the jacket lapel on its own brass clip.
        pin=np.asarray(chest_point(-1.71,3.48,.13))
        badge("body","gold",pin,.13,.13,.09)
        r.loft("body","leather",[(0,.26,.06,.06),(.08,.34,.07,.07),(.86,.34,.07,.07)],
               pin+[0,.06,-.09],n=12,exp=.36)
        r.loft("body","white",[(0,.25,.05,.04),(.49,.25,.05,.04)],pin+[0,.24,-.16],n=12,exp=.36)
    if style in ("lab","uniform"):
        for s in (-1,1):
            ribbon("body","lining" if style=="lab" else "coat",
                   [(s*.73,1.80,-1.23),(s*2.49,2.51,-1.66),(s*1.42,4.27,-2.02),(s*.28,6.82,-2.12)],.36,.12)
            ribbon("body","gold" if style=="lab" else "lining_dark",[(s*.98,2.0,-1.52),(s*1.86,3.1,-1.90),
                   (s*.76,5.64,-2.11),(s*.28,6.6,-2.11)],.055,.059)
        if g in ("himeno","power","agnes_tachyon"):
            ribbon("body","trim",[(0,2.15,-1.67),(0,3.44,-2.13),(0,5.5,-2.21),(0,7.38,-2.12)],.27,.12)
    if style in ("varsity","tennis","knit","mariner","wrap","vest"):
        band("rib" if style in ("knit","varsity") else "trim",8.96,9.34,.10)
        front=ring(9.31,.13)[0]
        if style in ("knit","wrap","vest","mariner"):
            badge("body","gold",front,.40,.21,.10)
            badge("body","leather",front+[0,0,-.10],.22,.105,.036)
        if style=="varsity":
            for y in (3.25,4.65,6.05,7.45,8.58):
                badge("body","metal",ring(y,.095)[0],.11,.11,.06)
        if style in ("mariner","knit","wrap"):
            collar=[ring(h,.09) for h in (2.35,2.65,2.84)]
            for row in collar:row[:,1]+=.36*np.maximum(0,np.cos(angles))
            r.grid("body","lining" if style=="knit" else "trim",collar,caps=False)
        if style=="vest":
            for s in (-1,1):
                ribbon("body","lining",[(s*.68,1.92,-1.50),(s*2.20,2.59,-1.62),
                       (s*1.32,4.13,-1.93),(s*.18,6.37,-1.94)],.24,.075)
    if style=="halter":
        body_surface=surface(parts["body"])
        fitted=[envelope(body_surface,y,angles,.04) for y in (9.14,9.45)]
        r.grid("body","leather",fitted,caps=False)
        center=fitted[0][0]+[0,.14,-.08]
        badge("body","metal",center,.34,.18,.07)
        badge("body","black",center+[0,0,-.08],.18,.08,.03)
        r.loft("body","black",[(.76,1.02,1.01,1.04),(1.12,1.06,1.06,1.08)],(0,0,-.32),n=24)
    if style in ("race","naval"):
        band("trim",8.89,9.34,.14);band("gold",9.26,9.40,.18)
        for s in (-1,1):
            ribbon("body","trim",[(s*.24,2.01,-1.49),(s*1.09,2.55,-1.82),(s*2.02,2.54,-1.68),(s*2.28,3.29,-1.58)],.21)
        if g=="gold_ship":
            r.loft("body","trim",[(1.40,1.10,1.04,1.02),(2.06,1.54,1.30,1.29)],(0,0,-.19),24)
    if style in ("pinafore","blouse"):
        for s in (-1,1):
            ribbon("body","trim",[(s*.09,3.23,-1.98),(s*.78,2.97,-2.04),(s*.95,2.81,-2.0),(s*.93,3.54,-1.99)],.22,.10)
            ribbon("body","trim",[(s*.05,3.26,-1.98),(s*.20,3.65,-2.06),(s*.31,4.01,-2.13),(s*.40,4.46,-2.14)],.10)
    # Boots have raised toes and separate soles. They follow the same leg as the trousers.
    for name in ("right_leg","left_leg"):
        tri=surface(parts[name]);pivot=np.array(parts[name]["pose"]);loops=[]
        for y,push in ((22.45,.04),(23.04,.24),(23.57,.64),(23.85,.69),(23.94,.64)):
            q=envelope(tri,y,angles,.066)-pivot;q[:,2]-=push*np.maximum(np.cos(angles),0)**1.6;loops.append(q)
        r.grid(name,"boot",loops[:-1]);r.grid(name,"sole",loops[-2:])
    # Accessories below are in the head's rest frame, including the existing head pivot.
    pivot=np.array(parts["head"]["pose"])
    def hp(x,y,z):return np.array([x,y,z])-pivot
    if g!="reze":
        tri=surface(parts["head"])
        levels=np.linspace(tri[:,:,1].min()+.04,tri[:,:,1].max()-.12,36)
        outlines=np.stack([envelope(tri,y,angles,.067) for y in levels])
        end=-4.42+4.4*((1-np.cos(angles))/2)**.76+.42*np.sin(angles)**2
        rows=[]
        for t in np.linspace(0,1,15):
            row=[]
            for j,stop in enumerate(end):
                y=levels[0]+(stop-levels[0])*t
                row.append([np.interp(y,levels,outlines[:,j,k]) for k in range(3)])
            rows.append(np.array(row)-pivot)
        r.grid("hair_base","hair",rows,caps=False)
    hair=np.concatenate([surface(p)+pivot for name,p in parts.items() if name in ("hair_fringe","hair_base")])
    crown=float(np.quantile(hair[:,:,1],.012))
    def hair_front(x,y):
        return outer_surface(hair,x,y)
    if g in signature.GUESTS:
        signature.accessories(g,r,parts,ring,hp,hair_front,crown)
    def curl(s,x,y,z,length,phase,rad):
        # At NPC scale ten sides keep the highlight smooth around each narrow lock.
        circle=np.arange(10)*TAU/10
        rings=[]
        for t in np.linspace(0,1,24):
            a=phase+t*TAU*1.57
            center=hp(s*(x+rad*np.cos(a)),y+length*t,z+rad*np.sin(a))
            scale=.26*np.sin(t*2.91+.17)**.72+.025
            rings.append(center+np.c_[scale*np.sin(circle),np.zeros(len(circle)),-.60*scale*np.cos(circle)])
        r.grid("headwear","hair",rings)
    if g in PILOT_UNITS:
        for sign in (-1,1):
            x=sign*2.94
            socket=outer_surface(hair[:,:,[0,2,1]],x,-.58)
            base=hp(x,(socket if socket is not None else crown+.8)+.16,-.58)
            def lean(x,y,z,sine,cosine,index):
                return x+sign*(.13-.25*y), .58*y+.26*sign*x+.23*z, 1.78*z+.25*y
            r.loft("headwear","visor",[(-1.12,.17,.22,.25),(-.61,.55,.56,.71),
                                       (.20,.72,.62,.78),(.54,.58,.52,.62)],base,n=16,warp=lean)
            r.loft("headwear","coat",[(-1.31,.12,.18,.21),(-.84,.45,.52,.56),
                                      (-.26,.66,.63,.76),(.04,.70,.63,.77)],base,n=16,warp=lean)
    if g=="asuka_langley":
        for sign in (-1,1):
            origin=hp(sign*2.80,crown+.87,.24)
            for strand,spread in (("hair",0.),("strand",.43),("hair",-.33)):
                path=[origin+[sign*spread,0,0],origin+[sign*(1.78+spread),2.10,.58],
                      origin+[sign*(2.11+spread),6.03,2.33],origin+[sign*(1.29+spread),9.38,2.86]]
                r.sweep("hair_base",strand,path,.52,.34,steps=15,
                        shape=lambda t:(.45+.70*np.sin(np.pi*t))*(1-.87*t))
    if g=="emiru":
        for sign in (-1,1):
            for j in range(3):
                path=[hp(sign*(2.35+.25*j),-5.06,-2.78+.42*j),hp(sign*(4.02+.16*j),-1.22,-2.85+.30*j),
                      hp(sign*(3.40+.28*j),6.03,-2.20+.28*j),hp(sign*(2.90+.37*j),11.10+.72*j,-1.95+.27*j)]
                r.sweep("hair_base","hair" if j!=1 else "strand",path,.43 if j!=1 else .11,.19,steps=18,
                        shape=lambda t:(.63+.59*np.sin(np.pi*t))*(1-.95*t**2))
    if g=="pokimane":
        for sign in (-1,1):
            for i in range(4):
                curl(sign,3.0+.25*i,-3.58+.22*i,-1.60+i*.70,8.84+.64*i,i*1.37,.70)
    if g=="brittany_venti":
        # Draw the curls up behind the crown, leaving two fine spirals at the temples.
        tie=hp(0,crown+1.25,2.75)
        r.orb("hair_base","hair",tie,(1.82,1.22,1.05),20)
        circle=np.arange(12)*TAU/12
        for i in range(22):
            a=TAU*i/22
            anchor=tie+np.array([1.10*np.sin(a),.1, .60*np.cos(a)])
            rings=[]
            for t in np.linspace(0,1,35):
                spread=1.0+2.05*np.sin(np.pi*t*.86)
                phase=i*1.63+t*TAU*2.8
                center=anchor+[(spread-1)*np.sin(a)+.44*np.cos(phase)+1.38*t,t*(9.0+1.5*np.cos(3*a)),
                                1.35+1.15*np.sin(np.pi*t)+spread*.55*np.cos(a)+.33*np.sin(phase)]
                width=(.56+.13*np.cos(i))*(.58+.67*np.sin(t*2.96))*(1-.83*t**3)
                rings.append(center+np.c_[width*np.sin(circle),np.zeros(len(circle)),-.72*width*np.cos(circle)])
            r.grid("hair_base","hair" if i%5 else "strand",rings)
        for sign in (-1,1):
            curl(sign,3.80,-4.60,-2.32,6.12,sign*.8,.52)
    if g=="himeno":
        badge("headwear","black",hp(-1.48,-1.55,-3.94),1.01,.81,.15)
        ribbon("headwear","black",[hp(2.77,-3.94,-2.99),hp(1.03,-3.12,-4.23),
               hp(-.64,-2.18,-4.19),hp(-1.98,-1.58,-3.79)],.105)
    if g=="sarah_stock":
        # A pair of tortoiseshell barrettes lies directly on the imported fringe.
        for y in (-4.62,-4.26):
            points=[]
            for t in np.linspace(0,1,4):
                x=1.85+.90*t
                z=hair_front(x,y-.25*t)
                if z is not None:points.append(hp(x,y-.25*t,z-.13))
            if len(points)==4:ribbon("headwear","gold",points,.085,.086)
    if g=="power":
        for s in (-1,1):
            r.loft("headwear","jewel",[(0,.075,.08,.10),(.42,.31,.32,.36),(1.04,.50,.49,.50),
                   (1.77,.56,.54,.57)],hp(s*2.29,-9.11,-.51),n=18,warp=lambda x,y,z,si,co,i:(x+s*.18*(1.8-y),y,z-.22*y))
    if g=="asa_mitaka":
        for s in (-1,1):
            r.orb("headwear","trim",hp(s*3.02,-.11,1.74),(.48,.52,.40))
            ribbon("headwear","hair",[hp(s*3,-.12,1.77),hp(s*3.32,1.0,2.32),
                   hp(s*3.71,2.41,2.64),hp(s*3.92,3.56,2.68)],.53,.37)
    if g in RACERS:
        for s in (-1,1):
            socket=outer_surface(hair[:,:,[0,2,1]],s*2.14,.08)
            a=hp(s*2.14,(socket if socket is not None else -7.08)-2.04,.08)
            lean=.20 if g=="gold_ship" else .32
            def ear(x,y,z,si,co,i):return x+s*(2.9-y)*lean,y,z-.12*(2.9-y)
            rows=[(0,.05,.08,.07),(.47,.35,.37,.42),(1.20,.59,.54,.56),
                  (2.15,.68,.56,.63),(2.85,.48,.43,.52),(3.1,.39,.40,.40)]
            r.loft("headwear","hair",rows,a,n=16,warp=ear)
            r.loft("headwear","lining_dark",[(.42,.045,.05,.045),(1.09,.26,.15,.10),(2.01,.36,.19,.12),(2.42,.18,.14,.12)],
                   a+[0,0,-.42],n=12,warp=ear)
        if g in ("tokai_teio","special_week"):
            s=-1 if g=="special_week" else 1
            for k in (-1,1):
                ribbon("headwear","trim",[hp(s*2.92,-6.42,-.7),hp(s*(3.36+.3*k),-6.65+k*.53,-.77),
                       hp(s*3.93,-6.62+k*.51,-.78),hp(s*3.92,-6.43+k*.72,-.74)],.30,.15)
            badge("headwear","gold",hp(s*2.96,-6.46,-.88),.27,.27,.13)
        if g=="gold_ship":
            cap=hp(0,crown+.12,-.28)
            r.loft("headwear","leather",[(-.72,2.25,1.85,1.82),(-.25,2.83,2.13,2.26),(.22,2.90,2.22,2.21)],cap,n=32)
            r.loft("headwear","gold",[(-.13,2.87,2.19,2.24),(.10,2.92,2.23,2.26)],cap,n=32)
            for s in (-1,1):
                r.orb("headwear","leather",hp(s*3.65,-3.08,.10),(.60,1.19,1.0))
                r.orb("headwear","gold",hp(s*4.14,-3.10,.08),(.21,.76,.65))
        if g=="oguri_cap":
            for k in range(-3,4):
                x=k*.77;y=-4.90+.09*abs(k)
                z=hair_front(x,y)
                if z is not None:badge("headwear","gold" if k%2==0 else "trim",hp(x,y,z-.17),.47,.34,.16)
        if g in ("tokai_teio","special_week"):
            rows=[]
            for t in np.linspace(0,1,25):
                y=crown+.54+t*(-3.2-crown-.54)
                cx=-.23+.28*np.sin(t*np.pi*1.2)
                half=.035+(.47 if g=="special_week" else .30)*np.sin(np.pi*t)**.67
                z=hair_front(cx,y)
                if z is None:continue
                rows.append(np.array([hp(cx+half*np.sin(a),y,z-.14-.10*np.cos(a)) for a in angles]))
            if len(rows)>1:r.grid("headwear","white",rows)
        parts["horse_tail"]["pose"]=[0,9.77,1.43]
        for i in range(5):
            t=(i-2)*.27
            path=[(t*.5,0,.1),(t*1.55,-1.69,3.18),(t*1.88,4.55,3.90),(t*.80+.31,8.10-.44*abs(i-2),3.13+t*.31)]
            r.sweep("horse_tail","hair" if i!=3 else "strand",path,.46,.40,steps=13,shape=lambda a:.12+.85*np.sin(a*2.99+.08)**.66)
        if g=="tokai_teio":
            ribbon("headwear","hair",[hp(0,-5.8,2.66),hp(.25,-7.45,5.19),hp(1.48,-.50,5.35),hp(.77,2.22,4.56)],1.0,.74)
    for name,p in r.parts.items():
        if p['vertices']:
            v=np.asarray(p['vertices'])
            part_add(parts[name],v[:,:3],chart(v[:,3:5],charts['details']),v[:,5:],p['faces'])


def stature(model,g):
    # Scale the finished rig about the soles, including the child joints and their geometry.
    factor=SPECS[g][9]
    for part in model["parts"]:
        pose=np.array(part["pose"],float)*factor
        if part["parent"]=="$mesh":pose[1]+=24*(1-factor)
        part["pose"]=pose.round(6).tolist()
        if part["vertices"]:
            verts=np.array(part["vertices"])
            verts[:,:3]*=factor
            part["vertices"]=verts.round(7).tolist()
