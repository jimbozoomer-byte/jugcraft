"""Author the eight guest character meshes in model pixels (negative Z is front).

Closed rings describe the cloth, limbs and head. Swept sections follow the hair
from its part to its tips. Vertices carry UVs and smooth normals into Minecraft.
"""
from dataclasses import dataclass
import json
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw
from guest_skins import DESIGNS, rgb

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/jugcraft"
TAU = math.tau
SIZE = 512
NAMES = ("chun_li", "tifa_lockhart", "yorha_2b", "ada_wong", "lara_croft",
         "kafka", "makima", "misato_katsuragi")

@dataclass(frozen=True)
class Cut:
    shoulder: float
    bust: float
    waist: float
    hips: float
    bust_depth: float
    seat: float
    thigh: float
    face: float

CUTS = dict(zip(NAMES, (
    Cut(3.68,3.88,2.31,4.44,2.77,3.16,2.63,3.65),
    Cut(3.47,4.04,2.37,3.87,3.16,2.92,2.11,3.52),
    Cut(3.33,3.66,2.44,4.04,2.71,3.07,2.13,3.48),
    Cut(3.32,3.41,2.28,3.78,2.64,2.89,1.98,3.45),
    Cut(3.74,3.68,2.60,3.74,2.79,2.85,2.11,3.55),
    Cut(3.47,3.76,2.32,4.02,2.89,2.98,2.07,3.59),
    Cut(3.38,3.98,2.30,4.19,3.01,3.09,2.19,3.57),
    Cut(3.56,3.77,2.50,3.82,2.81,2.84,2.04,3.60),
)))

def mix(a,b,t):
    return tuple(round(x+(y-x)*t) for x,y in zip(a,b))

def bezier(points,t):
    a,b,c,d=np.asarray(points,float)
    return a*(1-t)**3+3*b*t*(1-t)**2+3*c*t*t*(1-t)+d*t**3

def unit(a):
    return a / np.maximum(np.linalg.norm(a,axis=-1,keepdims=True),1e-12)

MATERIALS = ("skin","hair","strand","top","pants","coat","boot","trim","leather","metal",
             "gold","white","black","visor","brocade","lace","scarf","harvest","spring","lens",
             "rib","knit","lining","sole","button","glove","lining_dark","ribbon","jewel")
def rectangle(mat):
    if mat=="face": return 2,2,252,252
    if mat=="bodice": return 258,2,252,252
    i=MATERIALS.index(mat)
    return 2+(i%8)*64,258+(i//8)*63,60,59

class Rig:
    def __init__(self, guest):
        self.guest=guest
        self.parts={}
    def joint(self,name,parent="$mesh",at=(0,0,0)):
        self.parts[name]=dict(name=name,parent=parent,pose=list(at),vertices=[],faces=[])
        return name
    def grid(self,bone,mat,rings,uv=None,caps=True):
        # All rings have the same winding. The first vertex is repeated only at the UV seam.
        p=np.array(rings,float)
        rows,n,_=p.shape
        tangent=np.roll(p,-1,axis=1)-np.roll(p,1,axis=1)
        down=np.gradient(p,axis=0)
        normals=unit(np.cross(down,tangent))
        target=self.parts[bone]
        orig=len(target["vertices"]); ox,oy,w,h=rectangle(mat)
        def vert(pos,normal,u,v):
            target["vertices"].append([*(round(float(e),4) for e in pos),
                round((ox+.5+u*(w-1))/SIZE,6),round((oy+.5+v*(h-1))/SIZE,6),
                *(round(float(e),4) for e in normal)])
        for i in range(rows):
            for j in range(n+1):
                k=j%n
                u,v=uv(p[i,k],i,j,n) if uv else (j/n,i/(rows-1))
                vert(p[i,k],normals[i,k],max(0,min(1,u)),max(0,min(1,v)))
        for i in range(rows-1):
            for j in range(n):
                a=orig+i*(n+1)+j
                target["faces"].append([a,a+n+1,a+n+2,a+1])
        if caps:
            for row,sign in ((0,-1),(-1,1)):
                center=p[row].mean(axis=0)
                normal=unit(center-p[1 if row==0 else -2].mean(axis=0))
                base=len(target["vertices"])
                vert(center,normal,.5,.5)
                for k in range(n):
                    t=TAU*k/n
                    vert(p[row,k],normal,.5+.48*math.sin(t),.5-.48*math.cos(t))
                for k in range(n):
                    a,b=base+1+k,base+1+(k+1)%n
                    if sign<0:a,b=b,a
                    target["faces"].append([base,b,a,base])

    def loft(self,bone,mat,profile,at=(0,0,0),n=16,exp=1.0,warp=None,uv=None):
        rings=[]
        for i,(y,rx,front,back) in enumerate(profile):
            ring=[]
            for j in range(n):
                t=TAU*j/n
                sn,cs=math.sin(t),math.cos(t)
                dx=math.copysign(abs(sn)**exp,sn)
                dz=math.copysign(abs(cs)**exp,cs)
                x,z=rx*dx,-(front if cs>=0 else back)*dz
                pos=(x,y,z) if warp is None else warp(x,y,z,sn,cs,i)
                ring.append(np.asarray(pos)+at)
            rings.append(ring)
        self.grid(bone,mat,rings,uv=uv)

    def sweep(self,bone,mat,path,width,depth,*,guide=(1,0,0),steps=9,n=8,shape=None):
        rings=[]
        for i in range(steps+1):
            t=i/steps
            pos=bezier(path,t)
            tangent=unit(bezier(path,min(1,t+.002))-bezier(path,max(0,t-.002)))
            across=unit(np.array(guide,float)-np.dot(guide,tangent)*tangent)
            if np.linalg.norm(across)<.5:
                across=unit(np.cross(tangent,[0.,0.,1.]))
            front=unit(np.cross(across,tangent))
            amt=shape(t) if shape else 1
            rings.append([pos+amt*(width*math.sin(TAU*k/n)*across-
                             depth*math.cos(TAU*k/n)*front) for k in range(n)])
        self.grid(bone,mat,rings)

    def cord(self,bone,mat,points,radius):
        pts=np.asarray(points,float)
        for a,b in zip(pts,pts[1:]):
            self.sweep(bone,mat,[a,a+(b-a)/3,a+2*(b-a)/3,b],radius,radius,
                       guide=(0,0,1) if abs((b-a)[2])<np.linalg.norm(b-a)*.85 else (1,0,0),
                       steps=1,n=6)
    def orb(self,bone,mat,center,radii,n=12):
        rx,ry,rz=radii
        profile=[(ry*math.sin(a),max(.008,rx*math.cos(a)),max(.008,rz*math.cos(a)),
                  max(.008,rz*math.cos(a))) for a in np.linspace(-1.565,1.565,9)]
        self.loft(bone,mat,profile,center,n)
    def write(self):
        for path in (ASSETS/"town_models/guests",ASSETS/"textures/entity/town_guests"):
            path.mkdir(exist_ok=True,parents=True)
        for part in self.parts.values():
            if part["vertices"]:
                xyz=np.array(part["vertices"])[:,:3]
                part["bounds"]=[*xyz.min(axis=0).tolist(),*xyz.max(axis=0).tolist()]
        (ASSETS/"town_models/guests"/(self.guest+".json")).write_text(json.dumps(
            {"parts":list(self.parts.values())},separators=(",",":"))+"\n",encoding="utf8")
        atlas(self.guest).save(ASSETS/"textures/entity/town_guests"/("guest_"+self.guest+".png"))

def palette(g):
    hair,eye,top,lower,trim,skin,*_=DESIGNS[g]
    coat,boot,glove={
      "chun_li":("194a9d","f9f4de","212738"), "tifa_lockhart":("343240","8a2638","822338"),
      "yorha_2b":("24232e","302e3a","22222c"), "ada_wong":("942437","292731","e0ab97"),
      "lara_croft":("376d70","583e32","52382f"), "kafka":("492b43","703950","462d40"),
      "makima":("ece8e4","302c33","ecc4b3"), "misato_katsuragi":("a32d3e","6e3338","e6b9a5")}[g]
    m=dict(skin=skin,hair=hair,strand=hair,top=top,pants=lower,coat=coat,boot=boot,
           trim=trim,leather="422f32",metal="bec5d0",gold="e5b75e",white="f5ebe4",
           black="23212c",visor="272434",brocade=top,lace="302d3b",scarf="377079",
           harvest="c58344",spring="dca1b8",lens="674356",rib=coat,knit=coat,
           lining="d4cbc3",sole="352d32",button="d5c6aa",glove=glove,lining_dark="32303d",
           ribbon="fff2db",jewel="bc5654")
    if g=="misato_katsuragi":m["top"]="333044"
    if g=="chun_li":m["pants"]="614338"
    if g=="kafka":m["top"]="e9dce3"
    if g=="lara_croft":m["top"]="51968e"
    tones={k:rgb(v) for k,v in m.items()}
    tones["skin"]=mix(tones["skin"],(255,229,213),.26)
    return tones

def linecurve(points,n=18):
    return [tuple(bezier(points,t)) for t in np.linspace(0,1,n)]

def face_art(g):
    # Paint at twice the atlas resolution so the heavy upper lid and fine lower lashes stay clean.
    W=504
    skin=mix(rgb(DESIGNS[g][5]),(255,229,213),.26)
    iris=rgb({"chun_li":"955b30","tifa_lockhart":"a6413d","yorha_2b":"69798a",
              "ada_wong":"756043","lara_croft":"936839","kafka":"b06092",
              "makima":"f5b93f","misato_katsuragi":"835789","reze":"519856"}[g])
    y,x=np.mgrid[0:W,0:W]/2
    blush=np.exp(-((np.abs(x-126)-70)/22)**2-((y-162)/12)**2)*.23
    warm=np.array((226,132,134))
    base=np.array(skin)*(1-blush[:,:,None])+warm*blush[:,:,None]
    img=Image.fromarray(base.astype("uint8"));d=ImageDraw.Draw(img)
    def xy(pts):return [(round(a*2),round(b*2)) for a,b in pts]
    def line(pts,col,width):d.line(xy(pts),fill=col,width=max(1,round(2*width)),joint="curve")
    def oval(rect,col):d.ellipse(tuple(round(v*2) for v in rect),fill=col)
    span,opening,slant={
       "chun_li":(65,32,7),"tifa_lockhart":(68,39,3),"yorha_2b":(62,33,3),"ada_wong":(67,28,10),
       "lara_croft":(64,31,5),"kafka":(70,30,8),"makima":(68,39,1),
       "misato_katsuragi":(66,35,5),"reze":(69,32,6)}[g]
    span*=1.28
    opening*=1.13
    for s in (-1,1):
        cx=126+s*54
        def project(pts):
            return [(cx+s*a*span,132+b*opening-slant*a) for a,b in pts]
        upper=linecurve([(-.50,.11),(-.32,-.44),(.15,-.55),(.47,-.25)])
        lower=linecurve([(.47,-.25),(.40,.52),(-.15,.69),(-.50,.11)])
        if g=="makima":
            upper=linecurve([(-.51,-.01),(-.31,-.30),(.19,-.46),(.49,-.13)])
            lower=linecurve([(.49,-.13),(.34,.45),(-.18,.45),(-.51,-.01)])
        elif g in ("kafka","ada_wong"):
            upper=linecurve([(-.50,.12),(-.31,-.15),(.22,-.47),(.50,-.33)])
            lower=linecurve([(.50,-.33),(.27,.54),(-.26,.53),(-.50,.12)])
        outline=project(upper+lower)
        mask=Image.new("L",(W,W));ImageDraw.Draw(mask).polygon(xy(outline),fill=255)
        eye=Image.new("RGB",(W,W),(249,239,232));ed=ImageDraw.Draw(eye)
        def eo(rect,col):ed.ellipse(tuple(round(v*2) for v in rect),fill=col)
        radius=opening*(.63 if g=="makima" else .66)
        ix=cx-s*2.0; iy=126.5 if g=="makima" else 130
        eo([ix-radius-2,iy-radius-2,ix+radius+2,iy+radius+3],(53,36,40))
        for j in range(round(radius*2)+1):
            dy=j-radius
            dx=math.sqrt(max(0,radius*radius-dy*dy))
            col=mix(mix(iris,(41,31,48),.66),mix(iris,(255,208,154),.44),j/(2*radius))
            ed.line(xy([(ix-dx,iy+dy),(ix+dx,iy+dy)]),fill=col,width=3)
        if g=="makima":
            eo([ix-radius,iy-radius,ix+radius,iy+radius],(251,190,54))
            for frac in (.76,.43):
                a=radius*frac
                ed.ellipse(tuple(round(v*2) for v in (ix-a,iy-a,ix+a,iy+a)),outline=(191,67,39),width=5)
            eo([ix-2.3,iy-2.3,ix+2.3,iy+2.3],(169,61,35))
        else:
            eo([ix-3.1,iy-7,ix+3.1,iy+3],mix(iris,(25,23,35),.75))
            if g=="reze":
                for dx,dy in [(-8,4),(-6,7),(7,5)]:eo([ix+dx-1,iy+dy-1,ix+dx+1.4,iy+dy+1.4],(51,99,64))
            else:
                eo([ix-8,iy-9,ix-2,iy-4],(254,249,240))
                eo([ix+5,iy+6,ix+7.4,iy+8.4],(250,221,205))
        img.paste(eye,mask=mask)
        k=(39,29,41) if g!="makima" else (64,39,37)
        start,end=upper[0],upper[-1]
        cap=project(upper+linecurve([end,(.26,-.69 if g=="makima" else -.89),(-.24,-.38 if g=="makima" else -.57),start]))
        d.polygon(xy(cap),fill=k)
        for a in (.28,.41):
            pts=project([(a,-.38),(a+.10,-.38),(a+.20,-.66-(a-.2)),(a+.13,-.28),(a+.01,-.21)])
            d.polygon(xy(pts),fill=k)
        line(project(lower[:14]),mix(k,skin,.32),1.25)
        for a in (.19,.32,.41):
            line(project([(a,.44-(a-.17)*1.4),(a+.037,.53-(a-.17)*1.4)]),mix(k,skin,.4),.95)
        brow_pts=linecurve([(-.40,-.85),(-.16,-1.13),(.12,-1.15),(.37,-1.03)])
        line(project(brow_pts),mix(k,skin,.28),1.8)
    lip=mix(skin,(130,55,69),.60 if g in ("ada_wong","misato_katsuragi") else .43)
    line(linecurve([(125,160),(125,164),(121,165),(125,166)]),mix(skin,(156,92,89),.37),1.5)
    line([(126,165),(128,164)],mix(skin,(255,242,229),.72),1.25)
    mouth_y=186
    curve=[(117,mouth_y),(122,mouth_y+1),(128,mouth_y+1),(134,mouth_y-1 if g in ("kafka","chun_li","misato_katsuragi","reze") else mouth_y)]
    line(linecurve(curve),lip,2.3)
    line([(123,mouth_y+2.6),(128,mouth_y+2.8)],mix(skin,(217,137,143),.35),1.25)
    if g=="yorha_2b":oval((146,187,149,190),(101,73,78))
    return img.resize((252,252),Image.Resampling.LANCZOS)

def atlas(g):
    pal=palette(g)
    img=Image.new("RGB",(SIZE,SIZE),pal["skin"])
    img.paste(face_art(g),(2,2))
    img.paste(bodice_art(g),(258,2))
    for mat in MATERIALS:
        x,y,w,h=rectangle(mat);base=pal[mat]
        tile=Image.new("RGB",(w,h),base);d=ImageDraw.Draw(tile)
        if mat in ("hair","strand"):
            for xx in range(w):
                u=xx/(w-1)
                for yy in range(h):
                    t=yy/(h-1)
                    ridge=(u+.075+.042*math.sin(t*3))%1
                    sheen=math.exp(-((ridge-.16)/.095)**2)+.62*math.exp(-((ridge-.82)/.052)**2)
                    glint=.10*math.exp(-((t-.35+.08*math.sin(u*6))/.10)**2)
                    c=mix(base,(209,182,197),.28*sheen+glint)
                    d.point((xx,yy),fill=mix(c,(29,22,39),.19*(.5+.5*math.cos(4*math.pi*ridge))**3))
        elif mat in ("brocade","lace"):
            thread=pal["gold"] if mat=="brocade" else mix(base,pal["white"],.61)
            d.line((0,52,w,52),fill=thread,width=2)
            for a in range(-10,w,15):
                d.line([(a,50),(a+4,45),(a+9,47),(a+12,43),(a+8,40),(a+6,43)],fill=thread,width=1)
        elif mat in ("rib","knit"):
            for a in range(2,w,4):d.line((a,0,a,h),fill=mix(base,(41,29,41),.13))
        elif mat in ("gold","metal","lens"):
            for yy in range(h):
                d.line((0,yy,w,yy),fill=mix(base,(251,241,220),.39*math.exp(-((yy/h-.32)/.10)**2)))
        img.paste(tile,(x,y))
    return img


def face_profile(face):
    return [(-8.25,.55,.72,.73),(-7.98,2.18,2.17,2.27),(-7.34,3.22,2.95,3.13),
             (-6.34,face,3.26,3.45),(-5.18,face+.14,3.31,3.50),
             (-4.04,face+.11,3.26,3.38),(-3.17,face-.02,3.13,3.14),
             (-2.47,face-.48,2.93,2.76),(-1.93,face-1.18,2.52,2.19),
             (-1.49,face-2.09,1.96,1.54),(-1.25,.59,1.22,.85)]


def head(r,g):
    face=CUTS[g].face
    profile=face_profile(face)
    def head_uv(p,i,j,n):
        # UV stage uses front elevation. Side and back sample the skin on the atlas margin.
        if math.cos(TAU*j/n)<0:return (.01,.90)
        return (.5+p[0]/(2*(face+.14)),(p[1]+8.25)/7.0)
    def cheeks(x,y,z,s,c,i):
        if c>0:
            z-=.32*max(0,math.cos(s*math.pi/2))**2*math.exp(-((y+3.91)/.47)**2)
        return x,y,z
    r.loft("head","face",profile,n=40,exp=.82,uv=head_uv,warp=cheeks)
    r.loft("body","skin",[(-1.70,1.03,1.00,1.01),(-1.05,1.03,1.08,.98),
                         (-.08,1.15,1.13,1.08),(.68,1.50,1.32,1.31)])
    for s in (-1,1):
        r.orb("head","skin",(s*(face-.05),-4.04,.05),(.52,.83,.45))
        if g in ("chun_li","ada_wong","kafka","misato_katsuragi"):
            r.orb("head","gold" if g!="misato_katsuragi" else "metal",
                  (s*(face+.12),-3.55,-.09),(.18,.25,.20),8)

def lock(r,bone,path,width,depth,mat="strand",guide=(1,0,0)):
    def shape(t):
        return (.55+.60*math.sin(math.pi*min(1,t*1.05)))*(1-.97*t**2.65)
    r.sweep(bone,mat,path,width,depth,guide=guide,shape=shape,steps=10)

def hair(r,g):
    # A continuous scalp shell has a high front hairline and a low back edge.
    cap=[]
    skull=np.array(face_profile(CUTS[g].face))
    # Follow the actual skull beneath the bangs, including both temples.
    for step in range(15):
        row=[]
        for j in range(48):
            a=TAU*j/48; sn,cs=math.sin(a),math.cos(a)
            side=math.acos(cs)
            edge=float(np.interp(side,[0,.50,.90,1.27,1.68,2.28,math.pi],
                                [-6.17,-6.00,-5.37,-4.10,-3.25,-2.72,-2.58]))
            y=-8.87+(edge+8.87)*step/14
            sy=y+.32
            if sy<skull[0,0]:
                top=max(.02,(sy+8.55)/.30)
                rx=.91*top;rz=1.08*top
            else:
                rx=np.interp(sy,skull[:,0],skull[:,1])+.38
                rz=np.interp(sy,skull[:,0],skull[:,2 if cs>=0 else 3])+.37
            row.append((rx*math.copysign(abs(sn)**.82,sn),y,-rz*math.copysign(abs(cs)**.82,cs)))
        cap.append(row)
    r.grid("head","hair",cap,caps=False)
    # Each bang starts at a part and sweeps over the forehead. Tips are cut to frame an eye.
    parts={"chun_li":-.8,"tifa_lockhart":.1,"yorha_2b":-.95,"ada_wong":1.50,"lara_croft":.60,
           "kafka":.95,"makima":-.10,"misato_katsuragi":-.20}
    fringe={
        "chun_li":[(-3.27,-4.60),(-2.39,-5.26),(-1.12,-5.59),(.72,-5.53),(2.13,-5.48),(3.23,-4.62)],
        "tifa_lockhart":[(-3.33,-4.11),(-2.62,-4.85),(-1.45,-4.85),(.52,-5.56),(2.20,-5.36),(3.33,-4.19)],
        "yorha_2b":[(-3.28,-3.89),(-2.56,-4.59),(-1.60,-4.55),(-.29,-4.08),(1.03,-3.60),(2.13,-3.71),(3.09,-4.10)],
        "ada_wong":[(-3.0,-3.34),(-2.04,-3.25),(-.97,-3.67),(.03,-4.33),(1.59,-5.93),(3.10,-4.59)],
        "lara_croft":[(-3.4,-4.18),(-2.63,-4.72),(-1.35,-5.24),(.63,-5.99),(2.26,-5.43),(3.33,-4.11)],
        "kafka":[(-3.39,-4.03),(-2.39,-4.62),(-1.57,-4.62),(-.52,-5.09),(.61,-5.66),(2.08,-5.85),(3.20,-4.32)],
        "makima":[(-3.23,-4.93),(-2.56,-5.30),(-1.62,-5.23),(-.73,-5.00),(.12,-4.59),(1.09,-5.02),(2.09,-5.30),(3.00,-5.19)],
        "misato_katsuragi":[(-3.23,-4.04),(-2.20,-4.68),(-1.03,-4.52),(.01,-4.02),(1.36,-4.96),(2.52,-4.87),(3.32,-4.09)]}
    part=parts[g]
    for i,(tip,y) in enumerate(fringe[g]):
        s=-1 if tip<part else 1
        endz=-3.61+.068*tip*tip
        # Wide, flattened locks overlap like layers of cut hair. Their part, turn and fall
        # vary independently so the fringe reads as a haircut at profile and three-quarters.
        root=part+(tip-part)*.35
        sweep=s*(.32 if g=="makima" else .63)
        drape=(1.00,.73,1.13,.81,1.16,.78,.91,1.02)[i]
        def curl(t):
            return (.52+.58*math.sin(math.pi*(t*.92+.08)))*(1-.97*t**3.8)
        path=[(root,-8.60+.08*(i%2),-.98),
              (root+.55*(tip-root)+sweep,-8.33,-3.61),
              (tip+sweep*.88,y-.61,endz-.43),(tip-sweep*.28,y,endz-.18)]
        r.sweep("head","strand",path,drape*(.87 if g=="makima" else 1.05),.34,shape=curl,steps=13)
        if i%2==0:
            lock(r,"head",[(root+.11,-8.49,-1.46),(root+sweep*1.09,-8.04,-3.72),
                 (tip+sweep*.13,y-.42,endz-.53),(tip-sweep*.39,y+.23,endz-.22)],.24,.135)
    lengths={"chun_li":-2.53,"tifa_lockhart":-.20,"yorha_2b":-.84,"ada_wong":-.69,
             "lara_croft":-1.55,"kafka":1.63,"makima":2.15,"misato_katsuragi":.72}
    for s in (-1,1):
        y=lengths[g]+(.83 if g=="ada_wong" and s<0 else 0)
        inward=2.36 if g in ("ada_wong","yorha_2b") else 3.0
        for off in (0,.69):
            lock(r,"head",[(s*1.4,-8.36,.12+off),(s*4.56,-7.64,-1.05+off),
                     (s*4.10,y-.77,-2.03+off),(s*(inward-.31+.22*off),y-off,-2.43+off)],
                     .93,.58,guide=(.85,0,s*.65))
    length={"chun_li":-2.68,"tifa_lockhart":7.45,"yorha_2b":-.15,"ada_wong":-.32,
            "lara_croft":-2.0,"kafka":5.61,"makima":-2.15,"misato_katsuragi":5.45}[g]
    for k in range(9):
        a=(k-4)/4*1.45
        sn,cs=math.sin(a),math.cos(a)
        end=length-.99*abs(sn)+(k%3)*.22
        lock(r,"head",[(.38*part+.88*sn,-8.48,.77+cs),
              (4.72*sn,-7.66,4.39*cs+.31),(4.13*sn,end-1.83,3.69*cs+1.15),
              ((3.38 if length<0 else 4.29)*sn,end,(2.52 if length<0 else 3.24)*cs+1.19)],
              1.03,.53,guide=(cs,0,-sn))
    if g in ("makima","lara_croft"):
        b=r.joint("braid","head",(0,-3.06,3.48))
        end=8.3 if g=="makima" else 6.85
        for i in range(3):
            pts=[]
            for t in np.linspace(0,1,25):
                angle=TAU*(1.95*t+i/3)
                pts.append((.46*(1-.4*t)*math.sin(angle)+.46*t,t*end,.5*math.cos(angle)+1.28*t))
            for k in range(0,24,3):
                seg=pts[k:k+4]
                r.sweep(b,"strand",seg,.67-.24*k/24,.58-.16*k/24,steps=3,n=8)
        r.orb(b,"black",(.48,end,1.28),(.59,.30,.60))
        lock(r,b,[(.47,end,1.28),(.66,end+.5,1.34),(.77,end+1.08,1.56),(.58,end+1.45,1.69)],.56,.42)
    if g=="chun_li":
        for s in (-1,1):
            r.orb("head","white",(s*3.58,-6.99,1.08),(1.61,1.55,1.56),16)
            r.loft("head","gold",[(-.19,1.52,1.50,1.5),(.13,1.55,1.52,1.52)],(s*3.58,-6.67,1.08))
            for k in (0,1):
                lock(r,"head",[(s*3.93,-5.75,1.11+k*.40),(s*4.96,-4.63,1.63+k*.49),
                     (s*(3.44+k*.9),-2.47,2.37),(s*(4.24+k*.45),-1.14+k*.74,2.68)],.37,.12,"ribbon")
    if g=="yorha_2b":
        # The blindfold curves against the face, just beneath the bangs.
        def wrap(x,y,z,s,c,i):return (x,y+.36*s*s,z)
        r.loft("head","visor",[(-5.47,3.68,3.47,3.49),(-5.24,3.77,3.51,3.54),
               (-4.28,3.74,3.51,3.49),(-4.03,3.68,3.46,3.44)],n=32,warp=wrap)
        r.sweep("head","black",[(-3.84,-5.48,.22),(-3.65,-9.83,.24),
                (3.54,-9.85,.24),(3.85,-5.48,.22)],.21,.40,guide=(0,0,1),steps=22)
    if g=="kafka":
        for s in (-1,1):
            r.orb("head","metal",(s*1.51,-7.73,-3.02),(1.16,.75,.32))
            r.orb("head","lens",(s*1.51,-7.72,-3.22),(1.00,.60,.31))
        r.cord("head","gold",[(-.59,-7.64,-3.25),(0,-7.86,-3.40),(.59,-7.64,-3.25)],.10)


def bodice_art(g):
    p=palette(g);base=p["rib"] if g=="ada_wong" else p["top"]
    tile=Image.new("RGB",(252,252),base);d=ImageDraw.Draw(tile)
    seam=mix(base,(52,43,62),.20); glint=mix(base,(255,240,238),.18)
    def stitch(pts,color,width=2):
        d.line(linecurve(pts),fill=color,width=width,joint="curve")
    for s in (-1,1):
        def side(pts):return [(126+s*x,y) for x,y in pts]
        stitch(side([(103,27),(70,67),(95,117),(81,171)]),seam,2)
        stitch(side([(82,160),(61,179),(65,214),(60,234)]),glint,2)
    if g=="chun_li":
        gold=p["gold"]
        for s in (-1,1):
            def side(pts):return [(126+s*x,y) for x,y in pts]
            stitch(side([(3,30),(34,30),(64,46),(82,76)]),gold,5)
            stitch(side([(16,37),(46,39),(56,53),(59,61)]),mix(gold,p["white"],.54),2)
            for x,y in [(47,52),(64,66),(81,80)]:
                stitch(side([(x,y),(x+10,y-17),(x+21,y-4),(x+10,y+1)]),gold,3)
        for y in (22,46):
            d.line((116,y,136,y),fill=gold,width=4);d.ellipse((132,y-3,139,y+3),fill=gold)
    elif g in ("tifa_lockhart","lara_croft"):
        for s in (-1,1):
            def side(pts):return [(126+s*x,y) for x,y in pts]
            stitch(side([(84,0),(87,70),(92,123),(81,228)]),p["leather"],9 if g=="tifa_lockhart" else 12)
            x=126+s*87;d.rounded_rectangle((x-7,85,x+7,101),radius=3,outline=p["metal"],width=3)
        if g=="tifa_lockhart":
            stitch([(5,153),(59,162),(194,162),(247,153)],p["lining"],4)
    elif g=="ada_wong":
        for x in range(3,251,8):
            stitch([(x,0),(x-3,60),(x+1,125),(x,251)],mix(base,(41,26,39),.15),2)
        stitch([(234,0),(163,84),(88,135),(16,215)],p["leather"],10)
    elif g=="yorha_2b":
        flax=mix(p["white"],base,.28)
        for s in (-1,1):
            for y in (46,63,80):
                stitch([(126, y+27),(126+s*14,y+8),(126+s*42,y+14),(126+s*31,y-3)],flax,3)
                stitch([(126+s*29,y),(126+s*13,y-7),(126+s*10,y+6),(126+s*20,y+10)],flax,2)
    elif g=="makima":
        d.line((126,0,126,251),fill=seam,width=3);d.line((130,0,130,251),fill=glint,width=2)
        for y in (91,135,179,224):d.ellipse((127,y-2,132,y+3),fill=p["button"])
    elif g=="kafka":
        for s in (-1,1):
            for i in range(3):
                x=126+s*(52-i*6);y=119+i*23
                d.polygon([(x,y-4),(x+4,y),(x,y+4),(x-4,y)],fill=p["metal"])
    return tile

def body(r,g):
    c=CUTS[g]
    # Front and back projection are independent. Volume peaks at the bustline and
    # behind the hip rather than inflating the entire torso equally in every view.
    prof=[(-.30,1.38,1.17,1.21),(.20,2.09,1.40,1.32),(.83,c.shoulder-.26,1.61,1.55),
          (1.40,c.shoulder+.16,1.78,1.70),(2.19,c.bust-.18,2.04,1.76),
          (3.05,c.bust+.03,c.bust_depth*.94,1.86),(3.94,c.bust,c.bust_depth,1.86),
          (4.57,c.bust-.26,c.bust_depth*.94,1.76),(5.23,c.waist+.53,2.12,1.58),
          (6.11,c.waist+.13,1.65,1.49),(7.13,c.waist,1.48,1.47),
          (7.91,c.waist+.17,1.54,1.59),(8.83,c.hips-.75,1.79,2.10),
          (9.68,c.hips-.15,1.99,c.seat*.92),(10.56,c.hips,2.03,c.seat),
          (11.39,c.hips-.06,1.98,c.seat*.96),(12.04,c.hips-.30,1.79,c.seat*.82)]
    def shape(x,y,z,s,cos,i):
        vol=math.exp(-((y-3.77)/1.14)**2)*max(0,cos)**1.4
        return x,y,z+vol*(.60*math.exp(-(s/.17)**2)-.22*math.exp(-((abs(s)-.46)/.20)**2))
    def uv(pos,i,j,n):
        a=TAU*j/n
        return (.5+.48*math.sin(a) if math.cos(a)>=0 else .015,(pos[1]+.3)/9.5)
    for i,(a,b) in enumerate(zip(prof,prof[1:])):
        mat="bodice"
        if g in ("tifa_lockhart","lara_croft") and i<3:mat="skin"
        if g=="tifa_lockhart" and 8<=i<=11:mat="skin"
        if i>=12:mat="pants" if g not in ("chun_li","ada_wong","yorha_2b","misato_katsuragi") else "coat"
        if g=="kafka" and i>=12:mat="pants"
        # Adjacent rows have identical normals on their shared boundary.
        # Emit a full material run, keeping curvature across the rest of its length.
        if i==0:start=0;previous=mat
        if mat!=previous:
            r.loft("body",previous,prof[start:i+1],n=24,warp=shape,uv=uv if previous=="bodice" else None)
            start=i;previous=mat
    r.loft("body",previous,prof[start:],n=24,warp=shape,uv=uv if previous=="bodice" else None)
    if g in ("tifa_lockhart","lara_croft"):
        for s in (-1,1):
            r.sweep("body","white" if g=="tifa_lockhart" else "top",
                    [(s*2.29,2.62,-1.50),(s*2.64,.53,-1.33),(s*2.71,-.15,.57),(s*2.56,2.1,1.28)],
                    .60,.16)
    for s,arm,leg in ((-1,"right_arm","right_leg"),(1,"left_arm","left_leg")):
        ax=s*.49
        sleeveless=g in ("tifa_lockhart","chun_li","lara_croft")
        arm_mat="skin" if sleeveless else ("white" if g=="makima" else "coat")
        r.loft(arm,arm_mat,[(-1.89,.48,.62,.60),(-1.56,1.04,1.12,1.13),(-.86,1.28,1.30,1.34),
             (.18,1.36,1.40,1.39),(1.24,1.26,1.35,1.25),(2.66,1.07,1.16,1.18),
             (3.66,.92,1.01,1.09),(4.32,1.06,1.12,1.10),(5.36,1.12,1.17,1.12),
             (6.85,.83,.89,.95),(7.43,.77,.83,.87)],(ax,0,0))
        hand="skin" if g in ("chun_li","ada_wong","makima","misato_katsuragi") else "glove"
        r.loft(arm,hand,[(7.12,.74,.84,.84),(7.65,.90,.97,.91),(8.46,1.00,1.05,1.01),
                          (9.11,.85,.91,.85),(9.38,.51,.61,.61)],(ax,0,-.03),exp=.76)
        r.orb(arm,hand,(ax-s*.72,8.08,-.47),(.39,.69,.47),10)
        # Thigh width and rear profile stay on the moving leg bones when walking or sitting.
        lx=s*(.35 if g=="chun_li" else .02)
        legmat="pants" if g in ("makima","kafka","chun_li","yorha_2b","tifa_lockhart","ada_wong") else "skin"
        limb=[(-1.11,c.thigh*.69,1.31,1.65),(-.47,c.thigh*.93,1.67,2.18),
             (.62,c.thigh,1.98,2.39),(1.69,c.thigh*1.02,2.17 if g=="chun_li" else 1.90,2.24),
             (3.03,c.thigh*.94,2.02 if g=="chun_li" else 1.73,1.87),
             (4.38,c.thigh*.80,1.65,1.47),(5.61,c.thigh*.64,1.46,1.25),
             (6.37,c.thigh*.62,1.31,1.32),(7.55,c.thigh*.68,1.21,1.65),
             (8.32,c.thigh*.65,1.16,1.65),(9.71,c.thigh*.48,.93,1.17),
             (10.76,c.thigh*.42,.89,.91),(11.45,c.thigh*.43,.87,.90)]
        boot={"chun_li":5.65,"tifa_lockhart":8.35,"yorha_2b":3.70,"ada_wong":6.0,"lara_croft":7.85,
              "kafka":6.15,"makima":10.45,"misato_katsuragi":8.88}[g]
        cutoff=boot-.04
        full=np.array(limb)
        rim=[cutoff,*[float(np.interp(cutoff,full[:,0],full[:,i])) for i in (1,2,3)]]
        r.loft(leg,legmat,[v for v in limb if v[0]<cutoff]+[rim],(lx,0,0))
        rad=(1.68 if g=="chun_li" else (1.60 if g=="yorha_2b" else 1.29))
        r.loft(leg,"boot",[(boot,rad,1.61,1.65),(boot+.21,rad+.06,1.68,1.72),
                    ((boot+10.82)/2,rad*.89,1.42,1.48),(10.63,1.09,1.26,1.16),
                    (11.02,1.25,1.89,1.22),(11.43,1.37,2.69,1.33),(11.80,1.36,2.77,1.35)],(lx,0,0))
        r.loft(leg,"sole",[(11.70,1.37,2.76,1.35),(11.93,1.39,2.78,1.37),
                    (12.03,1.28,2.68,1.29)],(lx,0,0),n=16)
        if g=="chun_li":
            for k in range(4):
                y=6.37+.82*k
                for way in (-1,1):
                    r.cord(leg,"gold",[(lx-.74,y-way*.35,-1.37),(lx,y,-1.58),(lx+.74,y+way*.35,-1.37)],.073)
        if g=="tifa_lockhart":
            r.loft(arm,"glove",[(5.10,1.14,1.24,1.22),(5.44,1.32,1.40,1.35),
                      (6.96,1.29,1.37,1.27),(7.46,1.03,1.24,1.16)],(ax,0,0))
            for y in (5.49,6.68):
                r.loft(arm,"black",[(y,1.33,1.41,1.37),(y+.29,1.33,1.41,1.37)],(ax,0,0))
        if g in ("chun_li","yorha_2b"):
            r.loft(arm,"coat",[(-1.98,.69,.79,.75),(-1.64,1.22,1.35,1.30),
                  (-.64,1.70,1.72,1.67),(.42,1.67,1.70,1.66),(1.13,1.32,1.43,1.39)],(ax,0,0))
        if g=="chun_li":
            r.loft(arm,"black",[(5.97,1.23,1.33,1.25),(6.18,1.35,1.41,1.37),(7.32,1.32,1.40,1.35)],(ax,0,0))
            for k in range(5):
                a=k*TAU/5
                path=[(ax+l*math.sin(a),6.67,-l*math.cos(a)) for l in (1.15,1.36,1.59,1.86)]
                r.sweep(arm,"metal",path,.34,.35,guide=(0,1,0),steps=3,n=4,shape=lambda t:1-.98*t)
        if g=="lara_croft":
            r.loft(leg,"pants",[(-.47,2.03,1.95,2.21),(.69,2.20,2.04,2.44),(2.23,2.11,1.99,2.26)],(lx,0,0))
            r.loft(leg,"leather",[(2.78,2.03,1.91,2.01),(3.13,2.00,1.90,1.96)],(lx,0,0))
        if g=="makima":
            r.loft(leg,"pants",[(-.35,c.thigh+.06,1.84,2.24),(1.26,c.thigh+.09,2.00,2.40),
                 (3.56,2.03,1.82,1.89),(6.02,1.66,1.57,1.64),(8.62,1.60,1.47,1.73),
                 (10.68,1.55,1.43,1.43),(11.02,1.46,1.48,1.43)],(lx,0,0),exp=.86)


def garments(r,g):
    c=CUTS[g]
    def band(mat,y,bottom,width,front,back):
        r.loft("body",mat,[(y,width,front,back),(bottom,width+.03,front+.02,back+.03)],n=24)
    def ribbon(bone,mat,points,width,depth=.10,shape=None):
        r.sweep(bone,mat,points,width,depth,steps=10,n=8,shape=shape)
    def skirt(mat,bottom,flare,pleats,asym=0):
        rings=[(8.72,c.hips-.61,1.89,2.29),(9.59,c.hips+.02,2.11,c.seat+.09),
               (10.64,c.hips+.17,2.21,c.seat+.17),(11.88,c.hips+.21+.28*flare,2.40,c.seat+.25),
               (bottom-.29,c.hips+flare,2.50+.29*flare,c.seat+.35*flare),
               (bottom,c.hips+flare+.03,2.53+.29*flare,c.seat+.35*flare)]
        def pleat(x,y,z,s,cs,i):
            t=i/(len(rings)-1)
            a=math.atan2(s,cs)
            p=1+.061*t*math.cos(pleats*a) if pleats else 1
            return x*p,y+asym*t*s,z*p
        r.loft("body",mat,rings,n=48 if pleats else 24,warp=pleat)
    if g=="chun_li":
        band("gold",-.66,.21,1.40,1.29,1.28)
        band("white",6.21,7.57,2.60,1.80,1.70)
        for sign in (-1,1):
            ribbon("body","ribbon",[(sign*.3,6.9,1.71),(sign*1.78,5.65,2.13),
                   (sign*2.29,6.17,2.26),(sign*.68,7.12,2.06)],.36,.18)
        for f in (-1,1):
            z0=2.06 if f<0 else 2.83
            end=15.65 if f<0 else 16.50
            pts=[(0,8.81,f*1.84),(0,10.40,f*(z0+1.03)),(0,13.19,f*(z0+.30)),(0,end,f*z0)]
            ribbon("body","brocade",pts,1.91,.20,shape=lambda t:1+.16*math.sin(t*math.pi)-.25*t)
        for x in (-1.29,1.29):
            ribbon("body","gold",[(x,9.16,-1.81),(x*1.24,10.61,-2.62),
                   (x*1.28,13.24,-2.61),(x*.98,15.63,-2.09)],.10,.11)
    elif g in ("tifa_lockhart","yorha_2b","ada_wong","misato_katsuragi"):
        if g=="tifa_lockhart":
            skirt("pants",13.59,.46,12)
            band("leather",8.73,9.18,c.hips-.43,1.93,2.43)
            r.orb("body","metal",(-1.51,9.02,-1.83),(.47,.22,.11))
        elif g=="yorha_2b":
            skirt("lace",15.44,1.29,12,-.20)
            band("black",-.76,.50,1.35,1.28,1.22)
            for s in (-1,1):
                ribbon("body","lining",[(s*.65,14.96,-2.97),(s*1.4,14.43,-2.86),
                       (s*2.24,14.8,-2.65),(s*2.86,14.32,-2.54)],.10,.07)
        elif g=="ada_wong":
            skirt("rib",13.28,.29,0,-.76)
            band("rib",-1.01,.44,1.39,1.30,1.25)
            r.loft("left_leg","black",[(2.11,2.10,1.96,2.28),(2.49,2.08,1.95,2.24)])
            r.orb("left_leg","leather",(1.68,2.62,-.15),(.54,1.16,.75))
        else:
            skirt("black",13.21,.33,0)
            for s in (-1,1):
                ribbon("body","coat",[(s*2.68,.49,-1.12),(s*3.21,2.13,-1.89),
                       (s*3.27,4.34,-1.74),(s*2.09,6.67,-1.56)],1.01,.26)
                ribbon("body","black",[(s*1.34,.18,-1.33),(s*2.23,.93,-1.67),
                       (s*2.11,2.29,-2.24),(s*1.64,2.56,-2.40)],.41,.12)
            r.cord("body","metal",[(-1.09,.31,-1.32),(0,2.94,-2.67),(1.09,.31,-1.32)],.055)
            r.cord("body","metal",[(0,2.83,-2.76),(0,3.81,-2.90)],.11)
            r.cord("body","metal",[(-.37,3.15,-2.83),(.37,3.15,-2.83)],.09)
    elif g=="kafka":
        band("black",-.47,.15,1.26,1.17,1.14)
        for s in (-1,1):
            ribbon("body","coat",[(s*1.63,.45,-1.17),(s*3.83,1.89,-1.72),
                   (s*3.29,3.98,-1.90),(s*2.04,6.66,-1.54)],.66,.22)
            ribbon("body","coat",[(s*2.38,6.08,1.15),(s*3.13,9.48,2.87),
                   (s*4.45,13.02,2.99),(s*4.08,16.49,3.51)],1.48,.28,shape=lambda t:.78+.29*math.sin(math.pi*t))
            ribbon("body","trim",[(s*2.76,6.01,1.01),(s*4.33,10.31,2.22),
                   (s*5.02,14.09,2.63),(s*4.88,16.36,3.48)],.096,.11)
        band("black",8.04,8.52,c.waist+.54,1.76,1.98)
    elif g=="lara_croft":
        r.loft("body","leather",[(-.92,1.63,.75,.67),(-.53,2.22,1.13,1.24),
               (1.21,2.32,1.32,1.38),(3.45,2.20,1.24,1.30),(4.13,1.85,1.02,1.09)],(0,2.0,2.07),exp=.64)
        band("leather",8.62,9.22,c.hips-.32,1.99,2.64)
        r.orb("body","gold",(0,9.01,-2.01),(.68,.32,.13))
        for s,leg in ((1,"left_leg"),(-1,"right_leg")):
            r.orb(leg,"leather",(s*1.82,1.56,.07),(.64,1.64,.76))
            r.orb(leg,"button",(s*2.38,1.11,-.06),(.12,.19,.20))
        r.loft("left_arm","black",[(6.59,.97,1.08,1.03),(7.04,.93,1.02,1.03)],(.49,0,0))
        r.orb("left_arm","metal",(1.31,6.82,-.12),(.20,.34,.35))
    if g=="makima":
        for s in (-1,1):
            ribbon("body","white",[(s*1.07,-.43,-.88),(s*1.51,.20,-1.17),
                   (s*1.20,1.12,-1.76),(s*.85,1.68,-1.95)],.55,.14,shape=lambda t:1-.85*t)
        r.orb("body","black",(0,.94,-1.70),(.41,.39,.28))
        ribbon("body","black",[(0,1.03,-1.71),(0,2.83,-3.38),
               (0,4.61,-3.27),(0,6.46,-1.87)],.36,.12,
               shape=lambda t:.49+.73*math.sin(math.pi*t)**.7)
        band("black",8.53,8.96,c.hips-.52,1.86,2.27)
        r.orb("body","metal",(0,8.74,-1.87),(.42,.17,.09))


def seasons(r):
    winter=r.joint("winter_collar","body")
    r.loft(winter,"scarf",[(-.87,1.43,1.39,1.36),(-.61,1.63,1.53,1.48),
                            (.16,1.79,1.64,1.63),(.47,1.53,1.45,1.42)])
    r.sweep(winter,"scarf",[(-.48,.02,-1.42),(-.67,1.44,-2.02),
            (-2.18,2.87,-2.85),(-1.56,4.81,-2.65)],.46,.15)
    for s,side in ((-1,"right"),(1,"left")):
        cuff=r.joint("winter_"+side+"_arm",side+"_arm")
        r.loft(cuff,"scarf",[(5.04,1.22,1.32,1.25),(5.43,1.29,1.37,1.32),
                 (6.46,1.17,1.27,1.22)],(s*.49,0,0))
    for season,mat in (("harvest","harvest"),("spring","spring")):
        collar=r.joint(season+"_collar","body")
        r.loft(collar,mat,[(-.28,1.31,1.34,1.30),(.27,1.59,1.47,1.48)])
        pin=r.joint(season+"_pin","head")
        for i in range(5):
            a=TAU*i/5
            r.orb(pin,mat,(3.33+.39*math.sin(a),-6.21+.39*math.cos(a),-2.08),
                  (.24,.28,.13),8)
        r.orb(pin,"gold",(3.33,-6.21,-2.22),(.21,.22,.16),8)


def build(g):
    r=Rig(g)
    r.joint("head");r.joint("hat","head");r.joint("body")
    c=CUTS[g]
    for s,side in ((-1,"right"),(1,"left")):
        r.joint(side+"_arm",at=(s*4.55,2.0,0))
        r.joint(side+"_leg",at=(s*(2.17 if g=="chun_li" else c.hips*.50),12,0))
    head(r,g)
    hair(r,g)
    body(r,g)
    garments(r,g)
    seasons(r)
    return r


if __name__=="__main__":
    for guest in NAMES:
        build(guest).write()
