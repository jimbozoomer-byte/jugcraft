"""Fit the approved Reze head and imported Roblox hair to Jugcraft guests.

python tools/guest_roblox_portraits.py EXTRACTED_HEADS BODY_FIT_LIBRARY REVIEW_DIR [--next-sixteen]
"""
import argparse
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw
from scipy.ndimage import map_coordinates

import guest_body_fit as blockbench
import guest_portrait_models as old
import guest_roster_fittings as roster
from guest_portrait_models import Rig, TAU, unit

SIZE = 2048
# Each row chooses an approved body fit, source hair components, a hair ramp
# (shadow, body, reflected light), complexion, iris, brow angle and mouth lift.
LOOKS = {
    "chun_li": ("medium_full_power", [49], ("18141b","392d30","755d58"), "e2b59a","67432f", -16, .85),
    "tifa_lockhart": ("large_athletic", [13,16], ("141222","302a3a","706371"), "f0cbbd","963744", 2, 1.25),
    "yorha_2b": ("medium_full", [47,48], ("777a89","c5c9cf","f5f0e9"), "f1d4c8","879dac", 9, .25),
    "ada_wong": ("medium_medium", [23,42], ("131421","302b39","655568"), "e4bdab","5e5640", -10, .70),
    "lara_croft": ("large_compact_lean", [26,16], ("2e1b1c","59362a","a67350"), "d7a382","695537", -19, .28),
    "kafka": ("large_medium", [5,63], ("301224","63283e","ac5a7b"), "ebc7c0","9e547b", 13, 1.12),
    "makima": ("medium_full", [26], ("581c26","a84148","e28179"), "f0d0c2","edaf39", 4, .84),
    "misato_katsuragi": ("large_athletic", [19,20], ("211b37","443453","92708f"), "e9bba5","6a443c", -7, 1.45),
}
FACE=(6,6,756,756)
LOOKS.update(roster.LOOKS)
HAIR=[(774,6,616,502),(1401,6,632,502)]
LEGS=(800,1050,480,660)
ARMS=(1300,1050,360,660)
TORSO=(16,1310,740,700)
MATERIALS=(6,773,512,512)


def rgb(h):
    return np.frombuffer(bytes.fromhex(h),dtype=np.uint8).astype(float)


def ease(t):
    t=np.clip(t,0,1)
    return t*t*(3-2*t)


def blend(a,b,t):
    return a+(np.asarray(b)-a)*np.asarray(t)[...,None]


def put_atlas(atlas,im,box):
    x,y,w,h=box
    tile=np.asarray(im.convert("RGBA").resize((w,h),Image.Resampling.LANCZOS))
    atlas.paste(Image.fromarray(np.pad(tile,((2,2),(2,2),(0,0)),mode="edge")),(x-2,y-2))


def chart(uv,box):
    x,y,w,h=box
    return (np.asarray(uv)*[w-1,h-1]+[x+.5,y+.5])/SIZE


def face_art(source,g):
    if g=="reze":return source.convert("RGB")
    _,_,_,skin,iris,tilt,smile=LOOKS[g]
    original=np.asarray(source.convert("RGB"),dtype=float)
    h,w=original.shape[:2]
    assert h==w==1024
    yy,xx=np.mgrid[:h,:w]
    # The imported head uses a mirrored upper-lid island, a separate iris
    # island and a central mouth. Warp their poses independently on the real UV.
    sx=xx.astype(float);sy=yy.astype(float)
    lids=(xx>570)&(xx<925)&(yy>515)&(yy<736)
    brow=(xx>598)&(xx<845)&(yy>389)&(yy<499)
    mouth=(xx>395)&(xx<630)&(yy>802)&(yy<894)
    sy[lids]-=(tilt*.35*(xx[lids]-747)/135)
    sy[brow]-=tilt*(xx[brow]-718)/108
    sy[brow]+={"ada_wong":4,"chun_li":6,"tifa_lockhart":-7,"misato_katsuragi":-9}.get(g,0)
    sx[mouth]=512+(xx[mouth]-512)/(.76+.19*smile)
    sy[mouth]+=((np.abs(xx[mouth]-512)/84)**1.65)*(smile-.8)*19
    src=np.stack([map_coordinates(original[:,:,c],[sy,sx],order=1,mode="nearest") for c in range(3)],-1)
    base=original[5,5];skin=rgb(skin)
    distance=np.linalg.norm(src-base,axis=2)
    src+=(skin-base)*np.exp(-(distance/69)**2)[...,None]
    x=(xx-196)/61.7;y=(yy-484)/69.0
    r=(x*x+y*y)**.5; angle=np.arctan2(y,x)
    mask=(xx<375)&(yy>353)&(yy<611)
    # Remove the original green spill on the sclera around the iris.
    green=mask & (src[:,:,1]>src[:,:,0]+3)
    gray=np.mean(src,axis=2)
    src[green]=np.column_stack([gray[green],gray[green],gray[green]+1])
    lash=lids & (src[:,:,1]>src[:,:,0]+2)
    src[lash]=gray[lash,None]*np.array([.86,.76,.87])
    col=rgb(iris)
    color=blend(col*.48,col*1.22+4,ease((y+.60)/1.5))
    color+=3.3*np.sin(angle*26+.5*np.sin(angle*9))[...,None]*ease(r)[...,None]
    color=blend(color,col*.22,ease((r-.85)/.14))
    if g=="makima":
        color=blend(col*.88,col*1.14,ease((y+1)/2))
        for rad in (.28,.55,.86):
            color=blend(color,rgb("a64526"),1-ease((np.abs(r-rad)-.021)/.030))
        color=blend(color,rgb("ae4a25"),1-ease((r-.062)/.035))
    else:
        pupil=((x/.27)**2+(y/.34)**2)**.5
        color=blend(color,col*.15,1-ease((pupil-.86)/.16))
        if g=="power":
            cross=(np.minimum(abs(x),abs(y))<.105)&(r<.90)
            color=blend(color,rgb("a63d37"),cross)
        shine=np.exp(-(((x+.45)/.16)**2+((y+.37)/.102)**2)**2)
        color=blend(color,[255,247,235],shine*.94)
        if g in roster.SPECS:
            glint=np.exp(-(((x-.33)/.105)**2+((y-.50)/.070)**2)**2)
            color=blend(color,[253,229,209],.83*glint)
            crescent=np.exp(-((r-.76)/.14)**2)*ease((y-.20)/.55)
            color=blend(color,np.minimum(255,col*1.35+14),.57*crescent)
    src=blend(src,color,(1-ease((r-.976)/.044))*mask)
    # Character-specific mouth silhouettes on the approved face chart.
    src=blend(src,skin,ease((yy-810)/12)*(1-ease((yy-871)/12))*
              ease((xx-412)/14)*(1-ease((xx-608)/14)))
    result=Image.fromarray(np.uint8(np.clip(src,0,255)))
    d=ImageDraw.Draw(result)
    mouths={
        "chun_li":((454,850),(488,861),(547,858),(578,840)),
        "tifa_lockhart":((451,848),(483,863),(548,864),(576,848)),
        "yorha_2b":((479,854),(498,848),(534,847),(553,852)),
        "ada_wong":((455,851),(491,862),(552,855),(580,832)),
        "lara_croft":((463,851),(494,846),(529,856),(566,846)),
        "kafka":((447,848),(487,869),(541,860),(578,838)),
        "makima":((464,847),(497,856),(538,856),(569,844)),
        "misato_katsuragi":((445,845),(475,864),(548,866),(581,841))}
    curve=mouths.get(g,((461,849),(490,844+12*smile),(540,845+10*smile),(568,851-5*smile)))
    outline=old.linecurve(curve,28)
    ink=tuple(np.uint8(rgb("824b48") if g in ("kafka","ada_wong") else rgb("694442")))
    if g in ("misato_katsuragi","power","tokai_teio","special_week"):
        bottom=old.linecurve([curve[-1],(543,880),(480,880),curve[0]],28)
        d.polygon(outline+bottom,fill=ink)
        d.line(outline[5:-6],fill=(239,202,186),width=3)
        if g=="power":d.polygon([(468,852),(480,854),(475,864)],fill=(251,235,207))
    else:
        if g in ("emiru","lauren_chen","brittany_venti","aoc","sarah_stock","pokimane","himeno"):
            lip=tuple(np.uint8(.66*skin+.34*rgb("b85e6b")))
            d.line([(x,y+3) for x,y in outline[4:-4]],fill=lip,width=9)
        d.line(outline,fill=ink,width=5 if g in ("ada_wong","kafka") else 4)
    return result


def part_add(part,p,uv,n,faces):
    start=len(part["vertices"])
    vertices=np.column_stack((p.round(5),uv.round(7),unit(n).round(5)))
    part["vertices"].extend(vertices.tolist())
    faces=np.asarray(faces)
    if faces.shape[1]==3:faces=faces[:,[0,1,2,2]]
    part["faces"].extend((faces+start).tolist())


def add_import(part,points,uv,n,f):
    used,indices=np.unique(f,return_inverse=True)
    flat,ids=np.unique(np.column_stack((points[used].round(6),uv[used].round(7),n[used].round(6))),axis=0,return_inverse=True)
    part_add(part,flat[:,:3],flat[:,3:5],flat[:,5:],ids[indices.reshape(f.shape)])


def components(src,faces):
    from scipy.sparse import coo_matrix
    from scipy.sparse.csgraph import connected_components
    points,loops=np.unique(src.round(5),axis=0,return_inverse=True)
    triangles=loops[faces]
    edges=coo_matrix((np.ones(triangles.size),(triangles.ravel(),np.roll(triangles,1,axis=1).ravel())),
                     shape=(len(points),len(points)))
    _,component=connected_components(edges,directed=False)
    return component[loops]


def tuck_ada_fringe(src,posed,faces):
    labels=components(src,faces)
    # The donor has loose tip clusters that float across the open eye after fitting.
    eye=(posed[:,0]>-.37)&(posed[:,0]<-.08)&(posed[:,1]>.065)&(posed[:,1]<.27)&(posed[:,2]<-.43)
    tucked=[i for i in np.unique(labels[eye]) if posed[labels==i,1].min()>-.18]
    return faces[~np.isin(labels[faces[:,0]],tucked)]


def hair_chart(raw,im,ramp,g,num):
    h,w=im.height,im.width
    uv=raw["uv"]
    low=np.maximum([0,0],np.floor(uv.min(0)*[w,h]).astype(int)-2)
    high=np.minimum([w,h],np.ceil(uv.max(0)*[w,h]).astype(int)+2)
    local=(uv*[w,h]-low)/(high-low)
    pixels=np.asarray(im.convert("RGBA").crop((*low,*high))).copy()
    if num in (0,63): return Image.fromarray(pixels),local
    if g=="ada_wong" and num==23:
        # This low-poly underlayer supplies the dark mass behind the swept locks.
        pixels[:,:,:3]=rgb(ramp[1])*.71
        return Image.fromarray(pixels),local
    light=pixels[:,:,:3]@[.25,.57,.18]
    usable=light[pixels[:,:,3]>180]
    a,b=np.percentile(usable,[3,97])
    t=np.clip((light-a)/max(b-a,10),0,1)
    dark,mid,pale=map(rgb,ramp)
    color=blend(dark,mid,ease(t/.63))
    color=blend(color,pale,ease((t-.50)/.62)*.85)
    pixels[:,:,:3]=np.uint8(np.clip(color,0,255))
    return Image.fromarray(pixels),local


def hair_position(p,g,num):
    if g in roster.SPECS:return roster.hair_fit(p,g,num)
    q=p.copy()
    # Preserve the sculpted crown and retime the fall below the jaw.
    if g=="ada_wong":
        q[:,1]-=(.34 if num==23 else .58)*np.maximum(q[:,1]+.06,0)
        q[:,1]+=(.11 if num==23 else .32)*q[:,0]*ease((q[:,1]+.48)/.57)
        q[:,0]*=.84 if num==23 else .87
        if num==23:q[:,2]=q[:,2]*.81+.05;q[:,1]+=.06
        else:q[:,0]+=.10*ease((q[:,1]+.52)/.82)*(1-ease((q[:,2]+.43)/.27))
    elif g=="tifa_lockhart":
        if num==13:
            q[:,1]+=.83*np.maximum(q[:,1]-.06,0)
            q[:,0]*=1-.16*ease((q[:,1]-.28)/1.6)
            q[:,2]+=.15*ease((q[:,1]-.11)/1.0)
    elif g=="misato_katsuragi":
        q[:,0]*=.82
        if num==20:q[:,1]+=.25*np.maximum(q[:,1],0)
    elif g=="kafka" and num==5:
        q[:,0]*=.86
        q[:,1]+=.13*np.maximum(q[:,1],0)
    elif g=="kafka" and num==63:
        # Tip the sunglasses up onto the hairline.
        q[:,1]=q[:,1]*.54-.30
        q[:,2]=q[:,2]*.80-.094
    elif g=="chun_li":
        q[:,0]*=.82
        q[:,1]=q[:,1]*.74-.08
        bunch=ease((abs(q[:,0])-.44)/.25)
        q[:,0]*=1-.12*bunch
        q[:,1]=(1-.32*bunch)*q[:,1]+.32*bunch*(-.51)
    elif g=="yorha_2b":
        q[:,0]*=.87 if num==47 else 1
    elif g=="lara_croft":
        if num==26:
            q[:,1]+=.49*np.maximum(q[:,1]-.30,0)
            q[:,0]+=.11*ease((q[:,1]-.2)/1.3)
        else:
            q[:,1]-=.65*np.maximum(q[:,1]+.11,0)
            q[:,0]+=.17*ease((q[:,1]+.45)/.72)
            q[:,1]+=.28*q[:,0]*ease((q[:,1]+.53)/.55)
            q[:,2]-=.043
    return q


def add_head(model,atlas,sources,g):
    h=next(p for p in model["parts"] if p["name"]=="head")
    raw=np.load(sources/"head.npz")
    p,uv,n,f=[raw[k] for k in ("p","uv","n","f")]
    center=(p.min(0)+p.max(0))/2
    jaw={"chun_li":.98,"ada_wong":.92,"lara_croft":.96,"misato_katsuragi":.95,"makima":.98,**roster.JAW}.get(g,1.)
    if g=="reze":jaw=1.
    width=1-(1-jaw)*ease((p[:,1]-center[1]+.7)/3.6)
    p[:,0]*=width
    put_atlas(atlas,face_art(Image.open(sources/"head.png"),g),FACE)
    n=unit(n/[jaw,1,1])
    # The game lights the imported chin much harder than Blender's studio.
    # Soften normals on the facial plane while preserving the side of the jaw.
    front=ease((-p[:,2]-1.50)/1.5)*ease((-n[:,2]+.03)/.70)
    painted=unit(np.column_stack((n[:,0]*.55,n[:,1]*.18-.12,np.full(len(n),-.94))))
    n=unit(blend(n,painted,front*.72))
    add_import(h,p-h["pose"],chart(uv,FACE),n,f)
    for slot,num in enumerate(LOOKS[g][1]):
        mesh=np.load(sources/f"hair{num}.npz")
        image,uv=hair_chart(mesh,Image.open(sources/f"hair{num}.png"),LOOKS[g][2],g,num)
        put_atlas(atlas,image,HAIR[slot])
        src=mesh["p"];transform=lambda pts:hair_position(pts,g,num)
        p=transform(src)
        jac=np.stack([(transform(src+a)-transform(src-a))/.0002 for a in np.eye(3)*.0001],axis=-1)
        n=np.linalg.solve(jac.swapaxes(1,2),mesh["n"][:,:,None])[:,:,0]
        pos=center+p*7.19-h["pose"]
        faces=mesh["f"]
        uv=chart(uv,HAIR[slot])
        if g=="ada_wong" and num==42:
            faces=tuck_ada_fringe(src,p,faces)
        if g=="misato_katsuragi" and num==19:
            labels=components(src,faces)
            flyaway=np.unique(labels[src[:,1]<-.98])
            faces=faces[~np.isin(labels[faces[:,0]],flyaway)]
        if num==26:
            if g=="lara_croft":
                # Replace the donor's disconnected bangs and long front sidelocks.
                faces=faces[~np.all(src[faces,2]<-.46,axis=1)]
            tail=src[faces].mean(1)[:,1]>1.225
            pivot=center+transform(np.array([[0.,1.23,.53]]))[0]*7.19-h["pose"]
            braid=dict(name="braid",parent="head",pose=pivot.tolist(),vertices=[],faces=[])
            model["parts"].append(braid)
            add_import(braid,pos-pivot,uv,n,faces[tail])
            faces=faces[~tail]
        layer=dict(name="hair_base" if slot==0 else "eyewear" if num==63 else "hair_fringe",
                   parent="head",pose=[0,0,0],vertices=[],faces=[])
        model["parts"].append(layer)
        add_import(layer,pos,uv,n,faces)


def fit_body_textures(g,atlas):
    palette=old.palette(g)
    skin=rgb(LOOKS[g][3])
    tones={k:np.array(v,dtype=float) for k,v in palette.items()}
    bodice=np.asarray(old.bodice_art(g),float)
    for region,box in (("torso",TORSO),("arm",ARMS),("leg",LEGS)):
        w,h=box[2:];v,u=np.mgrid[0:h,0:w]
        theta=(u/(w-1)-.5)*TAU;y=v/(h-1)*24.1
        sin=np.sin(theta);c=np.cos(theta)
        front=ease((c-.07)/.37)
        def fill(color): return np.broadcast_to(color,(h,w,3)).copy()
        def edge(height):return ease((y-height+.036)/.072)
        def dye(color,mask):return blend(cloth,color,mask)
        if region=="torso":
            shirt=tones["top"] if g!="ada_wong" else rgb("962a40")
            cloth=fill(shirt)
            tx=np.clip(((.5+.48*sin)*251).astype(int),0,251)
            ty=np.clip((y/10.1*251).astype(int),0,251)
            cloth=dye(bodice[ty,tx],front)
            legs=tones["pants"] if g not in ("misato_katsuragi","yorha_2b","ada_wong") else tones["black"]
            neckline={"chun_li":1.4,"yorha_2b":1.45,"makima":1.82,"ada_wong":1.62,
                      "lara_croft":2.58,"tifa_lockhart":2.69,"misato_katsuragi":2.12,"kafka":2.27}[g]
            if g in ("tifa_lockhart","lara_croft"):
                neckline=neckline+1.18*np.maximum(c,0)**4-.53*abs(sin)
            cloth=dye(skin,1-edge(neckline))
            if g=="kafka":cloth=dye(tones["coat"],(1-front)*edge(2.23))
            if g=="misato_katsuragi":cloth=dye(tones["coat"],(1-front*.70)*edge(1.83)*(1-edge(8.12)))
            if g=="chun_li":cloth=dye(tones["white"],edge(7.53)*(1-edge(8.62)))
            # The underlayer, outer neckline and binding each have their own edge.
            # Angle runs continuously around the torso, including the back UV seam.
            side=abs(sin)
            chest=np.maximum(c,0)
            if g in ("tifa_lockhart","lara_croft"):
                binding=rgb("92384a" if g=="tifa_lockhart" else "293c42")
                neckline=3.08+1.22*chest**3-.64*side
                cloth=dye(binding,1-edge(neckline))
                cloth=dye(skin,1-edge(neckline-.45))
                strap=1-ease((abs(side-.76)-.052)/.06)
                cloth=dye(binding,strap*edge(1.48)*(1-edge(neckline)))
            elif g=="ada_wong":
                neckline=3.06+1.46*sin
                cloth=dye(tones["black"],1-edge(neckline))
                cloth=dye(skin,1-edge(neckline-.42))
            elif g=="yorha_2b":
                window=np.maximum(0,1-(abs(sin)/.58)**2)
                keyhole=edge(2.68)*(1-edge(2.68+2.03*window))*front
                cloth=dye(rgb("a18e98"),keyhole)
                lattice=np.minimum(abs((y+sin*2.8)%.72-.36),abs((y-sin*2.8)%.72-.36))
                cloth=dye(tones["lace"],keyhole*(1-ease((lattice-.036)/.045)))
            elif g=="kafka":
                opening=.45+.26*np.cos((y-4.3)*.60)
                inset=(1-ease((side-opening)/.08))*front*edge(2.21)
                cloth=dye(rgb("68304c"),inset)
                cloth=dye(skin,inset*(1-edge(3.35+.63*chest)))
                cloth=dye(tones["metal"],(1-ease(abs(side-opening)/.034))*front*edge(2.53))
            elif g=="misato_katsuragi":
                top=3.57+.44*chest
                cloth=dye(skin,front*(1-edge(top))*edge(1.42))
                cloth=dye(rgb("695575"),front*edge(top)*(1-edge(top+.43)))
            elif g=="chun_li":
                inset=ease((side-.52)/.25)*(1-edge(4.95-.67*side))*edge(1.56)
                cloth=dye(tones["white"],inset)
            if g in ("tifa_lockhart","lara_croft","kafka","makima","misato_katsuragi"):
                hems={"tifa_lockhart":6.20,"lara_croft":7.12,"kafka":6.72,"makima":7.16,"misato_katsuragi":6.55}
                hem=hems[g]+.18*side
                if g=="makima":
                    hem=hem+.88*chest*(1-side)
                cloth=dye(tones["lining_dark"],edge(hem-.26))
                cloth=dye(skin,edge(hem))
                if g in ("tifa_lockhart","kafka"):
                    hip_strap=9.55-1.46*side-.18*(1-chest)
                    cloth=dye(rgb("492c3a"),edge(hip_strap-.21)*(1-edge(hip_strap+.08)))
            cloth=dye(tones["coat"] if g=="chun_li" else legs,edge(9.65 if g!="tifa_lockhart" else 9.1))
        elif region=="arm":
            bare=g in ("chun_li","tifa_lockhart","lara_croft")
            cloth=fill(skin if bare else (tones["white"] if g=="makima" else tones["coat"]))
            if g in ("kafka","yorha_2b","tifa_lockhart","lara_croft"):
                cuff={"tifa_lockhart":8.0,"lara_croft":11.1,"kafka":10.95,"yorha_2b":9.3}[g]
                cloth=dye(tones["glove"],edge(cuff))
            else:
                cloth=dye(skin,edge(11.35))
            if g=="tifa_lockhart":cloth=dye(rgb("822d35"),edge(8.72)*(1-edge(11.20)))
            if g=="ada_wong":
                cloth=dye(skin,1-edge(7.52))
                cloth=dye(tones["black"],edge(7.52))
                cloth=dye(tones["rib"],edge(7.52)*(1-edge(7.83)))
        else:
            color={"tifa_lockhart":"25212d","chun_li":"62423b","ada_wong":"34252e","misato_katsuragi":"352d37",
                   "yorha_2b":"292733","lara_croft":"795c45","makima":"38323a","kafka":"403044"}[g]
            cloth=fill(rgb(color))
            # Shorts and stockings sit on each animated leg, so an open skirt or
            # a long stride reveals the same finished cloth as the standing pose.
            shorts={"chun_li":("244682",12.90),"tifa_lockhart":("302a39",13.30),
                    "lara_croft":("806447",13.82),"kafka":("4b3048",13.03),
                    "makima":("38323a",14.02),"ada_wong":("522936",12.68),
                    "misato_katsuragi":("332b3f",12.95),"yorha_2b":("34313b",13.20)}
            hosiery={"chun_li":("af8062",15.65),"tifa_lockhart":("39303c",15.52),
                     "yorha_2b":("38313e",15.83),"ada_wong":("604653",16.24),
                     "misato_katsuragi":("493c50",16.84),"kafka":("513346",15.05),
                     "makima":("685563",16.18)}
            cloth=dye(skin,edge(11.0))
            if g in hosiery:
                color,top=hosiery[g]
                cloth=dye(rgb(color),edge(top))
                cloth=dye(rgb(color)*.78,edge(top)*(1-edge(top+.25)))
            if g in shorts:
                color,length=shorts[g]
                hem=length+.31*np.cos(theta*2)
                cloth=dye(rgb(color),1-edge(hem))
                cloth=dye(rgb(color)*1.34,edge(hem-.24)*(1-edge(hem-.065)))
            boot={"tifa_lockhart":19.75,"chun_li":16.65,"ada_wong":18.15,"misato_katsuragi":20.25,
                  "yorha_2b":15.1,"lara_croft":18.71,"makima":22.5,"kafka":17.12}[g]
            bootcolor=tones["boot"] if g!="chun_li" else rgb("e6e2d9")
            cloth=dye(bootcolor,edge(boot))
            cloth=dye(bootcolor*.71,edge(boot+.04)*(1-edge(boot+.22)))
            cloth=dye(tones["sole"],edge(23.63))
            if g in ("chun_li","tifa_lockhart","lara_croft"):
                laces=1-ease(np.abs(np.abs((y-boot-.52)%1.0-.5)-.35*np.abs(sin))/.08)
                cloth=dye(tones["gold"] if g=="chun_li" else tones["leather"]*1.35,
                          laces*front*(1-ease((abs(sin)-.55)/.17))*edge(boot+.5)*(1-edge(22.83)))
        weave=(1.1*np.sin(u*1.93)*np.sin(v*2.14)+.65*np.cos(4*theta+.36*y))
        cloth=np.clip(cloth+weave[...,None],0,255).astype("uint8")
        put_atlas(atlas,Image.fromarray(cloth),box)


def clothe_base(model):
    for part in model["parts"]:
        if not part["vertices"]: continue
        bone=part["name"]
        box=TORSO if bone=="body" else (ARMS if bone.endswith("arm") else LEGS)
        v=np.asarray(part["vertices"]);world=v[:,:3]+part["pose"]
        x,y,z=world.T
        if bone=="body":
            cx=np.zeros_like(y);cz=np.interp(y,[.3,4.4,7.6,10.5,13.8],[-.45,-.70,-.66,-.39,-.20])
        else:
            side=1 if bone.startswith("left") else -1
            if bone.endswith("arm"):
                cx=side*np.interp(y,[1.7,3.3,6.9,10.7,12.9,14],[2.6,3.2,4.05,4.96,5.50,5.97])
                cz=np.interp(y,[2,7,10.8,14],[-.21,-.31,-.70,-1.26])
            else:
                cx=side*1.87;cz=-.11
        u=.5+np.arctan2(x-cx,-(z-cz))/TAU
        local=np.column_stack((u,np.clip(y/24.1,0,1)))
        v[:,3:5]=chart(local,box)
        # Seam-crossing triangles must sample the back of the garment on both edges.
        # The front motif is only on the torso. Use a narrow edge UV for those triangles.
        verts=v.tolist()
        fix={}
        for face in part["faces"]:
            ids=list(dict.fromkeys(face))
            if np.ptp(u[ids])>.55:
                for j,k in enumerate(face):
                    if k not in fix:
                        vv=list(verts[k]);vv[3]=chart([[.002,local[k,1]]],box)[0,0]
                        fix[k]=len(verts);verts.append(vv)
                    face[j]=fix[k]
        part["vertices"]=verts

def surface(part):
    v=np.array(part["vertices"])[:,:3]+part["pose"]
    faces=np.array(part["faces"])
    tris=np.concatenate([faces[:,:3],faces[faces[:,3]!=faces[:,2]][:,[0,2,3]]])
    return v[tris]


def section(tri,y):
    segments=[]
    for j in range(3):
        a,b=tri[:,j,:],tri[:,(j+1)%3,:]
        keep=(a[:,1]>y)!=(b[:,1]>y)
        a,b=a[keep],b[keep]
        if len(a):
            segments.append(a+(b-a)*((y-a[:,1])/(b[:,1]-a[:,1]))[:,None])
    return np.concatenate(segments)[:,[0,2]]


def standing_leg_rotation(bone):
    angle=np.deg2rad(4.0 if bone=="left_leg" else -4.0)
    return np.array([[np.cos(angle),np.sin(angle),0],[-np.sin(angle),np.cos(angle),0],[0,0,1]])


def envelope(tri,y,angles,margin,origin=None):
    from scipy.spatial import ConvexHull
    pts=section(tri,y)
    hull=ConvexHull(pts)
    eq=hull.equations
    center=(pts.max(0)+pts.min(0))/2 if origin is None else np.asarray(origin)
    direction=np.stack([np.sin(angles),-np.cos(angles)],axis=1)
    denom=direction@eq[:,:2].T
    radius=(-(eq[:,:2]@center+eq[:,2])/np.maximum(denom,1e-8)).min(axis=1)
    outline=center+direction*(radius+margin)[:,None]
    return np.column_stack((outline[:,0],np.full(len(outline),y),outline[:,1]))


def add_details(model,g,sources):
    if g in ("chun_li","yorha_2b"):
        model["parts"].append(dict(name="headwear",parent="head",pose=[0,0,0],vertices=[],faces=[]))
    parts={p["name"]:p for p in model["parts"]}
    accessories=Rig(g)
    for bone,part in parts.items():
        accessories.joint(bone,part["parent"],part["pose"])
    old.garments(accessories,g)
    old.seasons(accessories)
    # Drape the authored tailoring on the approved torso and moving thigh fits.
    dress=g in ("tifa_lockhart","yorha_2b","ada_wong","misato_katsuragi")
    belt=g in ("tifa_lockhart","lara_croft","makima","kafka")
    for bone,p in accessories.parts.items():
        if not p["vertices"]:continue
        v=np.array(p["vertices"]); x,y,z=v[:,:3].T.copy()
        if bone=="body":
            if dress or g in ("chun_li","lara_croft","makima"):
                # The original long panels and belts have fitted replacements below.
                keep=np.array(p["faces"])
                cutoff=7.9 if g in ("chun_li","lara_croft","makima") else 8.718
                keep=keep[~np.all(y[keep]>=cutoff,axis=1)]
                indices,inv=np.unique(keep,return_inverse=True)
                v=v[indices];x,y,z=x[indices],y[indices],z[indices]
                p["faces"]=inv.reshape(keep.shape).tolist()
            shift=np.interp(y,[-1,0,3.8,7.3,9.3,13],[1.85,1.9,1.3,1.0,.64,.35])
            v[:,0]=x*np.interp(y,[-.5,1,4,7.5,10,14],[.77,.84,.83,.95,1.03,1.04])
            v[:,1]=y+shift
            v[:,2]=z*np.interp(y,[-.5,1,3.7,7.4,10,15],[.86,.94,1.10,1.09,1.06,1.05])-.42
        elif bone in ("left_arm","right_arm"):
            v[:,0]+=(1 if bone.startswith("left") else -1)*1.10
        elif "collar" in bone:
            v[:,:3]+=[0,1.87,-.44]
        elif "pin" in bone:
            v[:,:3]+=[.05,.45,-.62]
        elif "winter_" in bone:
            v[:,:3]+=[1.12 if "_left" in bone else -1.12,.10,-.32]
        p["vertices"]=v.tolist()

    surfaces=[surface(parts["body"])]
    for bone in ("left_leg","right_leg"):
        pivot=np.array(parts[bone]["pose"])
        surfaces.append((surface(parts[bone])-pivot)@standing_leg_rotation(bone).T+pivot)
    torso=np.concatenate(surfaces)
    ring_angles=np.arange(48)*TAU/48
    def panel(mat,ys,width,offset,front=True,depth=.055):
        from scipy.spatial import ConvexHull
        rings=[]
        for y in ys:
            eq=ConvexHull(section(torso,y)).equations
            nz=eq[:,1]
            eq=eq[(nz<-.001) if front else (nz>.001)]
            t=(y-ys[0])/(ys[-1]-ys[0])
            a=np.arange(16)*TAU/16
            xs=offset(t)+width(t)*np.sin(a)
            bounds=-(xs[:,None]*eq[:,0]+eq[:,2])/eq[:,1]
            zz=bounds.max(1) if front else bounds.min(1)
            zz+=(-1 if front else 1)*(.08+depth*(1+np.cos(a)))
            rings.append(np.column_stack((xs,np.full(len(xs),y),zz)))
        accessories.grid("body",mat,rings)
    if g=="chun_li":
        for front,end in ((True,14.30),(False,15.65)):
            ys=np.linspace(9.1,end,18)
            span=lambda t:1.46+.30*np.sin(np.pi*t)-.28*t
            panel("brocade",ys,span,lambda t:0,front)
            for s in (-1,1):
                panel("gold",ys,lambda t:.065,lambda t:s*(span(t)-.09),front,.091)
    if g=="makima":
        panel("black",np.linspace(2.76,8.22,24),lambda t:.09+.29*np.sin(np.pi*t)**.42,lambda t:0)
        panel("black",np.linspace(2.35,2.91,5),lambda t:.28-.12*t,lambda t:0)
    if g=="misato_katsuragi":
        # Lay the cross on the jacket under the collar.
        panel("metal",np.linspace(4.1,5.18,9),lambda t:.085,lambda t:0)
        panel("metal",np.linspace(4.42,4.58,4),lambda t:.34,lambda t:0)
    if dress:
        end,flare,pleats,mat={
            "tifa_lockhart":(14.38,.34,12,"pants"),"ada_wong":(15.72,.28,0,"rib"),
            "misato_katsuragi":(13.90,.32,0,"black"),"yorha_2b":(15.55,1.14,12,"lace")}[g]
        side=np.sin(ring_angles)
        if g=="tifa_lockhart":
            hem=end-2.85*(.5+.5*np.cos(ring_angles-.70))**2
        elif g=="ada_wong":
            hem=end-3.25*np.maximum(side,0)**2
        elif g=="yorha_2b":
            hem=end-2.10*np.maximum(-side,0)**3
        else:
            hem=end-.72*(.5+.5*side)
        # Sample each column at its actual height, particularly the high side
        # of Ada's slit and the overlapping panel on Tifa's fighting shorts.
        heights=np.linspace(9.13,hem.max(),42)
        outlines=np.stack([envelope(torso,y,ring_angles,.25) for y in heights])
        rings=[]
        for t in np.r_[np.linspace(0,.94,22),.975,1]:
            y=9.13+(hem-9.13)*t
            r=np.column_stack([[np.interp(y[j],heights,outlines[:,j,k])
                                for j in range(len(ring_angles))] for k in range(3)])
            drape=flare*t*t
            if pleats:
                drape=drape+.13*t*np.cos(pleats*ring_angles)
            r[:,0]+=side*drape
            r[:,2]-=np.cos(ring_angles)*drape
            r[:,1]=y
            rings.append(r)
        accessories.grid("body",mat,rings,caps=False)
        if g in ("tifa_lockhart","yorha_2b"):
            binding=np.stack(rings[-2:]).copy()
            binding[:,:,0]+=.025*side
            binding[:,:,2]-=.025*np.cos(ring_angles)
            binding[0,:,1]=binding[1,:,1]-.12
            accessories.grid("body","lining_dark" if g=="tifa_lockhart" else "lining",binding,caps=False)
    if belt:
        rings=[envelope(torso,y,ring_angles,.22) for y in (9.18,9.62)]
        accessories.grid("body","leather" if g=="lara_croft" else "black",rings,caps=False)
        accessories.orb("body","gold" if g=="lara_croft" else "metal",(0,9.40,rings[0][0,2]-.04),(.38,.20,.095))
    if g in ("tifa_lockhart","yorha_2b","chun_li"):
        for bone in ("left_arm","right_arm"):
            arm=surface(parts[bone]);pivot=np.array(parts[bone]["pose"])
            if g in ("yorha_2b","chun_li"):
                ys=(2.46,2.67,3.12,3.71,4.34,4.55)
                layers=[envelope(arm,y,ring_angles,m)-pivot for y,m in zip(ys,(.035,.15,.39,.46,.23,.09))]
                accessories.grid(bone,"coat",layers)
            if g in ("chun_li","tifa_lockhart"):
                ys=(9.64,9.81,10.67,10.91)
                layers=[envelope(arm,y,ring_angles,m)-pivot for y,m in zip(ys,(.06,.22,.22,.08))]
                accessories.grid(bone,"black" if g=="chun_li" else "glove",layers)
                if g=="chun_li":
                    for j in range(0,len(ring_angles),8):
                        p=(layers[1][j]+layers[2][j])/2
                        d=np.array([np.sin(ring_angles[j]),0,-np.cos(ring_angles[j])])
                        accessories.sweep(bone,"metal",[p,p+.18*d,p+.38*d,p+.59*d],.2,.2,
                                          guide=(0,1,0),steps=3,n=4,shape=lambda t:1-.97*t)
    headpose=np.array(parts["head"]["pose"])
    if g=="yorha_2b":
        a=np.load(sources/"head.npz"); triangles=a["p"][a["f"]]
        # Fit her blindfold directly to the imported facial surface.
        bands=[envelope(triangles,y,ring_angles,.105)-headpose for y in (-2.54,-2.36,-1.18,-.69)]
        for j,band in enumerate(bands):
            band[:,1]-=.32*(1-np.cos(ring_angles))/2
        accessories.grid("headwear","visor",bands,caps=False)
    if g=="chun_li":
        a=np.load(sources/"head.npz"); triangles=a["p"][a["f"]]
        ys=np.linspace(-5.84,.17,50)
        rows=np.stack([envelope(triangles,y,ring_angles,.055) for y in ys])
        end=-4.64+4.04*(1-np.cos(ring_angles))*.5
        cap=[]
        for t in np.linspace(0,1,15):
            ring=[]
            for j,stop in enumerate(end):
                y=ys[0]+(stop-ys[0])*t
                ring.append([np.interp(y,ys,rows[:,j,k]) for k in range(3)])
            cap.append(np.array(ring)-headpose)
        accessories.grid("hair_base","hair",cap,caps=False)
        for s in (-1,1):
            center=np.array([s*4.48,-6.26,.48])-headpose
            accessories.orb("headwear","ribbon",center,(1.86,1.72,1.57),22)
            accessories.loft("headwear","gold",[(-.23,1.85,1.55,1.55),(.06,1.91,1.62,1.62)],center,24)
            for k in (-1,1):
                start=center+[s*.42,.98,.91]
                path=[start,start+[s*.53,.86,.60],start+[s*(.73+.37*k),2.71,.22],
                      start+[s*(.44+.58*k),3.93+.47*k,.31]]
                accessories.sweep("headwear","ribbon",path,.24,.075,steps=9,shape=lambda t:1-.33*t)
    for bone in ("left_leg","right_leg"):
        tri=surface(parts[bone]);pivot=np.array(parts[bone]["pose"])
        rings=[]
        for y,push in ((22.48,.045),(22.91,.22),(23.45,.62),(23.80,.72),(23.94,.68)):
            r=envelope(tri,y,ring_angles,.049)-pivot
            r[:,2]-=push*np.maximum(np.cos(ring_angles),0)**1.5
            rings.append(r)
        accessories.grid(bone,"boot",rings[:-1])
        accessories.grid(bone,"sole",rings[-2:])
    for bone,p in accessories.parts.items():
        if not p["faces"]:continue
        v=np.array(p["vertices"])
        part_add(parts[bone],v[:,:3],chart(v[:,3:5],MATERIALS),v[:,5:],p["faces"])


def source_reze(fitted):
    """Import the entire clothed avatar at its authored proportions and original UVs."""
    if fitted is None:
        raise ValueError("Reze requires the clothed avatar export")
    model=json.loads((fitted/"roblox-guest.json").read_text())
    charts=json.loads((fitted/"charts.json").read_text())
    with Image.open(fitted/"roblox-guest.png") as texture:
        atlas=texture.convert("RGBA")
    parts={p["name"]:p for p in model["parts"]}
    head=parts["head"]
    vertices=np.asarray(head["vertices"])
    faces=np.asarray(head["faces"])
    x,y,size,_=charts["Rig201Mtl"]
    uv=vertices[:,3:5]*SIZE
    on_hair=np.all((uv>=[x,y])&(uv<[x+size,y+size]),axis=1)
    hair_faces=on_hair[faces]
    if np.any(hair_faces.any(axis=1)!=hair_faces.all(axis=1)) or not hair_faces.any():
        raise ValueError("Invalid hair material on imported Reze")
    mask=hair_faces.all(axis=1)
    hair=dict(name="hair_base",parent="head",pose=[0,0,0])
    for bone,selection in ((hair,mask),(head,~mask)):
        used,indices=np.unique(faces[selection],return_inverse=True)
        bone["vertices"]=vertices[used].tolist()
        bone["faces"]=indices.reshape(-1,faces.shape[1]).tolist()
    model["parts"].append(hair)
    # Seasonal accessories have their own joints and an unused corner of the source atlas.
    box=(1500,1500,512,512)
    if atlas.getchannel("A").crop((1500,1500,2012,2012)).getbbox():
        raise ValueError("Seasonal tile overlaps the imported Reze artwork")
    roster.swatches(atlas,"reze",box)
    seasonal=Rig("reze")
    old.seasons(seasonal)
    for name,layer in seasonal.parts.items():
        v=np.array(layer["vertices"])
        if name.endswith("collar"): v[:,:3]+=[0,1.85,-.42]
        if name.endswith("_arm"): v[:,:3]+=[1.13 if "left" in name else -1.13,.1,-.3]
        if name.endswith("_pin"): v[:,:3]+=[.03,.45,-.60]
        part_add(parts[name],v[:,:3],chart(v[:,3:5],box),v[:,5:],layer["faces"])
    return model,atlas


def fit_head_to_stature(model, scale):
    """Scale the head and its attached braid, visor and pins around the neck pivot."""
    head_joints={"head"}
    for part in model["parts"]:
        if part["parent"] in head_joints:
            head_joints.add(part["name"])
            part["pose"]=(np.asarray(part["pose"])*scale).round(7).tolist()
        if part["name"] in head_joints and part["vertices"]:
            vertices=np.asarray(part["vertices"])
            vertices[:,:3]*=scale
            part["vertices"]=vertices.round(7).tolist()


def build(g,sources,forms,out,fitted=None):
    if g=="reze":
        model,atlas=source_reze(fitted)
    else:
        preset=LOOKS[g][0]
        model=json.loads((forms/preset/(preset+".json")).read_text())
        atlas=Image.new("RGBA",(SIZE,SIZE))
        if g in roster.SPECS:
            boxes=dict(torso=TORSO,arm=ARMS,leg=LEGS)
            roster.prepare_limbs(model,g)
            roster.swatches(atlas,g,MATERIALS)
            clothe_base(model)
            torso=next(p for p in model["parts"] if p["name"]=="body")
            roster.garment_textures(atlas,g,boxes,torso)
            add_head(model,atlas,sources,g)
            roster.add_costume(model,atlas,g,{**boxes,"details":MATERIALS})
            roster.stature(model,g)
        else:
            put_atlas(atlas,old.atlas(g),MATERIALS)
            fit_body_textures(g,atlas)
            clothe_base(model)
            add_head(model,atlas,sources,g)
            add_details(model,g,sources)
    if g in old.NAMES or g in roster.signature.GUESTS or g in ("himeno","emiru","pokimane","brittany_venti","sarah_stock","lauren_chen","aoc"):
        fit_head_to_stature(model,.89)
    elif g in roster.PILOT_UNITS or g in roster.RACERS or g in ("asa_mitaka","power"):
        fit_head_to_stature(model,.81)
    for part in model["parts"]:
        if not part["vertices"]:continue
        v=np.array(part["vertices"])
        if g not in roster.SPECS and part["name"] in ("left_leg","right_leg"):
            # A relaxed standing angle leaves the individual boots visible.
            rotation=standing_leg_rotation(part["name"])
            v[:,:3]=v[:,:3]@rotation.T
            v[:,5:]=v[:,5:]@rotation.T
            part["vertices"]=v.round(7).tolist()
        assert np.isfinite(v).all() and v[:,3:5].min()>=0 and v[:,3:5].max()<=1
        assert np.max(part["faces"])<len(v) and np.min(part["faces"])>=0
        part["bounds"]=[*v[:,:3].min(0).tolist(),*v[:,:3].max(0).tolist()]
    native=old.ASSETS/"town_models/guests"/(g+".json")
    tex=old.ASSETS/"textures/entity/town_guests"/("guest_"+g+".png")
    native.write_text(json.dumps(model,separators=(",",":"))+"\n",encoding="utf8")
    atlas.save(tex)
    bb=out/"blockbench"/(g+".bbmodel")
    bb.parent.mkdir(parents=True,exist_ok=True)
    blockbench.SIZE=SIZE
    blockbench.write_blockbench(model,atlas,bb,name=g.replace("_"," ").title(),texture_name=tex.name,
                               keep_sockets=True,hidden_prefixes=("spring_","harvest_","winter_"))
    print(g,sum(len(p["faces"]) for p in model["parts"]),flush=True)


if __name__=="__main__":
    cli=argparse.ArgumentParser(description=__doc__)
    for name in ("sources","forms","review"):cli.add_argument(name,type=Path)
    cli.add_argument("--only",nargs="+",choices=LOOKS)
    cli.add_argument("--next-sixteen",action="store_true")
    cli.add_argument("--fitted-avatar",type=Path,help="Directory containing the original clothed roblox-guest export")
    args=cli.parse_args()
    guests=args.only or (roster.NEXT_SIXTEEN if args.next_sixteen else old.NAMES)
    if "reze" in guests and args.fitted_avatar is None:cli.error("Reze requires --fitted-avatar DIRECTORY")
    for g in guests:build(g,args.sources,args.forms,args.review,args.fitted_avatar)
