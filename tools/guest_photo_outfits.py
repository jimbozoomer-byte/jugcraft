"""Photo-based fabric and skirt patterns for Emiru, Pokimane and Brittany Venti."""
import numpy as np
from PIL import Image
from guest_portrait_models import TAU, unit
from guest_skins import rgb

STYLES = ("gothic_lace", "satin_dress", "wrap_skort")
# The two head charts end at row 508. The arm and leg charts begin at 1050.
SKIRT = (785, 535, 1160, 480)
LETTERS = {
    "G": ("01110","11001","10000","10111","10001","11001","01110"),
    "R": ("11110","11001","11001","11110","11100","11010","11001"),
    "E": ("11111","11001","11000","11110","11000","11001","11111"),
    "N": ("11011","11001","11101","10101","10111","10011","11011"),
    "W": ("10001","10001","10101","10101","10101","11011","01010"),
    "I": ("111","010","010","010","010","010","111"),
    "C": ("01110","11001","11000","11000","11000","11001","01110"),
    "H": ("11011","10001","10001","11111","10001","10001","11011"),
}


def letterpress(field, angle, level, torso):
    # Project the print onto the front of the shirt. Equal UV increments span
    # unequal fabric widths around this sculpt, especially at the armholes.
    from scipy.interpolate import LinearNDInterpolator
    from guest_roblox_portraits import TORSO, SIZE
    vertices=np.asarray(torso["vertices"])
    uv=(vertices[:,3:5]*SIZE-np.asarray(TORSO[:2])-.5)/(np.asarray(TORSO[2:])-1)
    front=(abs(uv[:,0]-.5)<.30)&(uv[:,1]<.40)
    sample=(abs(angle)<1.36)&(level>3.55)&(level<6.04)
    across=LinearNDInterpolator(uv[front],vertices[front,0],fill_value=8)(
        angle[sample]/TAU+.5,level[sample]/24.1)
    word = "GREENWICH"
    advance = sum(len(LETTERS[c][0])+1 for c in word)-1
    column = (across/4.68+.5)*advance
    row = (level[sample]-3.85-.26*(across/2.34)**2)/1.38*7
    print_area=field[sample]
    offset = 0
    for char in word:
        glyph = np.array([[c=="1" for c in line] for line in LETTERS[char]])
        index = np.floor(column-offset).astype(int)
        baseline = np.floor(row).astype(int)
        active = (index>=0)&(index<glyph.shape[1])&(baseline>=0)&(baseline<7)
        print_area[active & glyph[baseline.clip(0,6),index.clip(0,glyph.shape[1]-1)]] = rgb("476156")
        offset += glyph.shape[1]+1
    field[sample]=print_area


def lace_pattern(a, y):
    # Four-petal rosettes on a diamond net. Offset alternate rows like a woven repeat.
    x = a*5.65/np.pi + .5*(np.floor(y/.91)%2)
    dx = (x+.5)%1-.5
    dy = (y/.91+.5)%1-.5
    phi = np.arctan2(dy,dx)
    petal = np.abs(np.hypot(dx,dy)-(.18+.081*np.cos(4*phi)))
    net = np.minimum(abs((x+y/.91+.5)%1-.5),abs((x-y/.91+.5)%1-.5))
    return (petal<.045)|(net<.026)


def paint_midriff(field, skin, angle, y, hem, waist):
    """Paint the exposed panel in torso UV space, from shirt hem to waistband."""
    exposed=(y>hem)&(y<waist)
    a=angle[exposed]
    top=np.broadcast_to(hem,y.shape)[exposed]
    depth=(y[exposed]-top)/(waist-top)
    front=np.maximum(np.cos(a),0)**2
    def wash(x,width,h,height):
        return np.exp(-((a-x)/width)**2-((depth-h)/height)**2)
    # The center furrow fades below the navel. The side planes turn towards
    # the waistband; everything around the back receives only the hem shadow.
    center=wash(.014,.095,.34,.40)
    obliques=np.exp(-((np.abs(a)-(.76-.36*depth))/.14)**2-((depth-.53)/.45)**2)
    light=wash(.29,.18,.29,.35)+wash(-.29,.18,.29,.35)
    cross=np.exp(-((depth-.40-.16*np.abs(a))/.075)**2-(a/.49)**4)
    navel=wash(.009,.076,.68,.092)
    under=wash(.012,.084,.766,.053)
    shade=front*(8*light-15*center-15*obliques-6*cross-43*navel+10*under)
    shade-=11*np.exp(-(depth/.083)**2)+4*np.exp(-((depth-1)/.09)**2)
    field[exposed]=np.asarray(skin)+shade[:,None]*np.array([.80,1.,.96])


def paint(atlas, style, dyes, skin, boxes, torso):
    from guest_roblox_portraits import put_atlas
    shell, inner, lower, trim, accent = [np.array(rgb(c),float) for c in dyes]
    skin = np.array(rgb(skin),float)
    for region,box in {**boxes,"skirt":SKIRT}.items():
        _,_,w,h = box
        yy,xx = np.mgrid[:h,:w]
        u,v = xx/(w-1), yy/(h-1)
        a=(u-.5)*TAU
        y=v*24.1
        side=np.abs(np.sin(a))
        front=np.cos(a)
        f=np.empty((h,w,3))
        f[:]=shell
        if region=="arm":
            f[:]=skin
            if style=="wrap_skort":
                f[y<4.66]=shell
                f[(y>4.48)&(y<4.69)] = shell*.86
            elif style=="satin_dress":
                # Two fine gold bracelets sit below the sleeve opening.
                for height in (10.80,11.08):
                    f[abs(y-height)<.068]=accent
        elif region=="leg":
            f[:]=skin
            under={"wrap_skort":14.30,"gothic_lace":13.58,"satin_dress":14.02}[style]
            knee=np.exp(-((y-16.82)/.86)**2)
            shin=np.exp(-((y-19.68)/2.4)**2)
            light=(4*shin-5*knee)*front.clip(0,1)-4*knee*side
            f+=light[...,None]*np.array([.7,1.,.94])
            f[y<under]=lower
            f[(y<under)&(y>under-.15)]=lower*.82
            if style=="gothic_lace":
                cuff=15.65+.10*np.cos(12*a)
                stocking=y>=cuff
                f[stocking]=rgb("f5eee3")
                rib=np.cos(34*a)>.78
                cable=(abs(np.sin(11*a+.69*np.sin(3.6*y)))<.19)|(abs(np.sin(11*a-.69*np.sin(3.6*y)))<.19)
                f[stocking&rib]=rgb("e8e1d7")
                f[stocking&cable]=rgb("dfd7cc")
                f[(y>=cuff)&(y<cuff+.74)]=rgb("e9e0d4")
                f[stocking&(y<cuff+.74)&(np.cos(24*a)>.30)]=rgb("f9f3e8")
            if style=="wrap_skort":
                f[y>21.74]=rgb("f2f0e6")
                f[(y>21.74)&(y<22.1)&(np.cos(30*a)>.35)]=rgb("d6d5cd")
        elif region=="torso":
            if style=="gothic_lace":
                neckline=3.91+.69*side-.39*np.exp(-((abs(a)-.46)/.28)**2)
                f[:]=skin
                bodice=y>neckline
                f[bodice]=inner
                sheen=4.6*np.cos(4*a-.18*y)+2.8*np.cos(9*a)
                f[bodice]+=sheen[bodice,None]
                f[bodice&lace_pattern(a,y)]=shell
                harness=(abs(y-(2.05+.99*side))<.16)|(abs(y-(2.08+.78*(1-side)))<.14)
                f[harness&(y<neckline)]=shell
                scallop=.12*np.cos(27*a)
                f[abs(y-neckline-scallop)<.20]=rgb("24212a")
                seam=5.46+.35*np.cos(4*a)
                f[(front>.40)&(abs(y-seam)<.08)]=shell
                for meridian in (.33,.72,1.22,2.08,2.72):
                    channel=abs(a)-meridian*(.89+.11*np.cos((y-5.6)*.94))
                    f[(y>seam)&(abs(channel)<.027)]=shell
                    f[(y>seam)&(abs(channel-.035)<.012)]=rgb("66555c")
                f[y>9.1]=lower
            elif style=="satin_dress":
                # The blouse gathers into a shaped underbust seam. Below it, paired
                # vertical stitch lines curve inwards along each corset channel.
                seam=5.81+.35*np.cos(3*a)
                f+=((6*np.cos(4*a)+3*np.sin(6.8*y+4*a))*np.clip((seam-y)/2,0,1))[...,None]
                for offset in (0,.52):
                    fold=y-(4.55+offset+.46*np.cos(3*a)-.52*side)
                    gather=(9*np.exp(-(fold/.14)**2)-6*np.exp(-((fold+.18)/.16)**2))*front.clip(0,1)
                    f+=gather[...,None]
                f[abs(y-seam)<.105]=inner
                spread=.83+.15*np.cos((y-5.7)*1.32)
                for meridian in (.29,.66,1.04,1.60,2.30):
                    dist=abs(a)-meridian*spread
                    f[(y>seam)&(abs(dist)<.036)]=inner
                    f[(y>seam)&(abs(dist-.04)<.015)]=trim
                f[(front>.83)&(abs(a)<.054)]=inner
                vneck=3.85-4.25*abs(a)
                f[(front>.85)&(y<vneck)]=skin
                f[y<1.62]=skin
                f[(front>.65)&(abs(y-vneck)<.10)&(y<3.94)]=trim
                for row in (4.03,4.76,5.49,6.22,6.95,7.68,8.41,9.14):
                    f[(a/.044)**2+((y-row)/.105)**2<1]=accent
            else:
                neck=1.89+.59*np.maximum(0,front)**2
                hem=7.25+.16*np.cos(2*a)
                drape=np.exp(-((y-hem+.48+.20*np.cos(3*a))/.16)**2)
                f-=3.8*drape[...,None]
                f[y<neck]=skin
                f[abs(y-neck)<.12]=shell*.85
                paint_midriff(f,skin,a,y,hem,9.67)
                f[abs(y-hem)<.10]=shell*.86
                letterpress(f,a,y,torso)
                f[y>=9.67]=lower
        elif style=="gothic_lace":
            f[:]=lower
            fold=4*np.cos(14*a+.3*v)+2*np.cos(28*a)
            f+=fold[...,None]
            if np.any(v>.70):
                scallop=.715+.034*np.cos(28*a)
                ivory=(v>scallop)&(v<.86+.015*np.cos(28*a))
                f[ivory]=rgb("eee7dc")
                f[ivory & lace_pattern(a,8*v)]=rgb("b6b0ad")
                f[abs(v-scallop)<.009]=rgb("514951")
            f[v>.975]=rgb("cfc6bf")
        elif style=="satin_dress":
            f[:]=shell
            f+=((6+9*v)*np.cos(9*a+.48*v)+2*np.cos(18*a+.25*v))[...,None]
            f[(v>.95)&(v<.962)]=inner
            f[v>.986]=trim
        else:
            f[:]=lower
            f+=((1.8*np.cos(6*a)+1.1*np.cos(12*a))*v)[...,None]
            f[(v<.079)]=lower*1.08
            f[(abs(v-.080)<.006)|(abs(v-.060)<.004)|(abs(v-.974)<.005)]=trim
            f[(u>.984)&(u<.991)]=trim
        grain=.9*np.cos(xx*.93+.24*yy)*np.sin(yy*1.37)
        put_atlas(atlas, Image.fromarray(np.uint8(np.clip(f+grain[:,:,None],0,255))), box)


def skirt(model_parts, rig, style):
    from guest_roblox_portraits import chart, envelope, part_add, surface
    count=96
    u=np.linspace(0,1,count+1)
    angle=(u-.5)*TAU
    start=9.08 if style!="wrap_skort" else 9.62
    end={"gothic_lace":14.05,"satin_dress":14.66,"wrap_skort":15.45}[style]
    under=np.concatenate([surface(model_parts[name]) for name in ("body","left_leg","right_leg")])
    # Fit every hem height against the torso and posed legs before adding fullness.
    heights=np.linspace(start,17,44)
    base_angles=np.linspace(-np.pi,np.pi,97)
    dist=[]
    for height in heights:
        outline=envelope(under,height,base_angles,.16,(0,-.13))
        dist.append(np.hypot(outline[:,0],outline[:,2]+.13))
    from scipy.interpolate import RegularGridInterpolator
    radius=RegularGridInterpolator((heights,base_angles),np.asarray(dist))
    def row(t):
        if style=="wrap_skort":
            seam=.82-.64*t
            theta=seam+u*(TAU+.17-.52*t**1.6)
            hem=end-.85*(1-u)**8-.79*u**10+.26*np.cos(theta)
        else:
            theta=angle
            hem=end+(.15 if style=="gothic_lace" else .26)*np.cos((14 if style=="gothic_lace" else 9)*theta)
        y=start+t*(hem-start)
        wrapped=(theta+np.pi)%TAU-np.pi
        fit=radius(np.column_stack((y,wrapped)))
        if style=="gothic_lace":
            fullness=.08+.90*t**1.4+.24*t*np.cos(14*theta)+.26*np.exp(-((t-.62)/.15)**2)
        elif style=="satin_dress":
            fullness=.045+.66*t**1.7+.16*t*np.cos(9*theta+.48*t)
        else:
            fullness=.055+.16*t+.13*u**9
        rad=fit+fullness
        return np.column_stack((rad*np.sin(theta),y,-.13-rad*np.cos(theta)))
    levels=np.unique(np.r_[np.linspace(0,1,24),.62,.69,.72,.84,.87,.975])
    rows=np.stack([row(t) for t in levels])
    normals=unit(np.cross(np.gradient(rows,axis=0),np.gradient(rows,axis=1)))
    uv=chart(np.stack(np.broadcast_arrays(u[None,:],levels[:,None]),axis=-1).reshape(-1,2),SKIRT)
    stride=len(u)
    faces=[[j,j+stride,j+stride+1,j+1] for i in range(len(levels)-1) for j in range(i*stride,i*stride+count)]
    part_add(model_parts["body"],rows.reshape(-1,3),uv,normals.reshape(-1,3),faces)
    if style=="wrap_skort":
        for t in (.15,.40,.66):
            p=row(t)[-4].copy()
            p[2]-=.055
            rig.orb("body","button",p,(.18,.18,.09),12)
            rig.orb("body","black",p+[0,0,-.07],(.06,.065,.035),8)
    elif style=="gothic_lace":
        # A separate upper flounce has a real rolled edge and casts a shadow on the lace.
        flounce=[]
        for t,extra in ((.38,.015),(.52,.18),(.67,.34),(.70,.27)):
            ring=row(t)[:-1].copy()
            ring[:,0]+=extra*np.sin(angle[:-1])
            ring[:,2]-=extra*np.cos(angle[:-1])
            ring[:,1]+=.12*np.cos(14*angle[:-1])
            flounce.append(ring)
        rig.grid("body","pants",flounce,caps=False)
