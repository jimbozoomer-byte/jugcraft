"""Fitted clothing, headpieces and hair ornaments for the last seven guest rigs."""
import numpy as np
from PIL import Image

from guest_photo_outfits import paint_midriff
from guest_portrait_models import TAU
from guest_skins import rgb

GUESTS=("frieren","rem","megumin","zero_two","rin_ishtar","miyabi","nicole_demara")
SHOES=dict(zip(GUESTS,("604c46","36303c","63473c","785057","4c3641","353d49","514254")))


def paint(atlas,guest,dyes,skin,boxes):
    shell,lining,lower,trim,accent=[np.array(rgb(x)) for x in dyes]
    for region,(ox,oy,width,height) in boxes.items():
        row,col=np.mgrid[:height,:width]
        a=(col/(width-1)-.5)*TAU
        y=row*24.1/(height-1)
        side=np.abs(np.sin(a))
        front=np.cos(a)>0
        cloth=np.empty((height,width,3),float)
        cloth[:]=shell
        if region=="leg":
            cloth[:]=lower if guest in ("zero_two","miyabi","nicole_demara") else rgb("443d4c")
            if guest=="rem": cloth[:]=rgb("efe9ec")
            top={"frieren":18.7,"megumin":17.3,"zero_two":17.8,"miyabi":19.8,"nicole_demara":18.9}.get(guest,21.8)
            cloth[y>=top]=rgb(SHOES[guest])
            cloth[(y>=top)&(y<top+.21)]=trim
            if guest in ("miyabi","nicole_demara","megumin"):
                cloth[(y>top+.52)&(y<top+.75)]=lining
                cloth[front&(abs(a-.46)<.11)&(abs(y-top-.63)<.18)]=accent
            if guest=="nicole_demara":
                cloth[(y>13.55)&(y<18.90)]=rgb("665363")
                cloth[(y>=13.35)&(y<=13.55)]=trim
        elif region=="arm":
            cuff=10.52 if guest=="nicole_demara" else 11.72
            if guest=="rem":
                cloth[:]=lining
                cloth[(y>4.38)&(y<cuff-.7)]=rgb("38303e")
            cloth[(y>cuff-.46)&(y<cuff)]=trim
            cloth[y>cuff]=skin
            if guest in ("zero_two","miyabi"):
                cloth[(y>3.6)&(y<3.85)&(side>.46)]=accent
                cloth[(y>4.0)&(y<4.18)&(side>.46)]=trim
            if guest=="nicole_demara":
                cloth[(y>4.15)&(y<5.92)&(side>.55)]=accent
        else:
            cloth[y<1.78]=skin
            cloth[y>9.55]=lower
            if guest in ("frieren","megumin"):
                piping=abs(a)-(.38+.10*np.sin(y*.45))
                cloth[front&(y>2.2)&(y<9.20)&(abs(piping)<.055)]=trim
            elif guest=="rem":
                bib=front&(abs(a)<.49+.12*np.cos(y*.46))&(y>2.54)&(y<9.36)
                cloth[bib]=lining
                cloth[front&(abs(abs(a)-(.50+.12*np.cos(y*.46)))<.066)&(y>2.54)&(y<9.36)]=trim
                cloth[abs(y-9.28)<.28]=lining
            elif guest=="rin_ishtar":
                cloth[(y>1.8)&(y<2.62)]=lining
                cloth[front&(((abs(a)<.075)&(y>3.14)&(y<5.56))|((abs(a)<.34)&(abs(y-4.02)<.13)))]=trim
                cloth[(abs(y-8.9)<.22)]=lining
            elif guest=="zero_two":
                edge=.66-.032*(y-3)
                cloth[front&(abs(abs(a)-edge)<.058)&(y>2.58)&(y<9.30)]=lining
                for level in (3.94,5.29,6.64,7.99):
                    cloth[front&(((abs(a)-.43)/.064)**2+((y-level)/.14)**2<1)]=accent
            elif guest=="miyabi":
                diag=3.72+2.62*np.sin(a)
                cloth[front&(y>1.87)&(y<diag)]=lining
                cloth[front&(abs(y-diag)<.18)]=trim
                cloth[(y>8.38)&(y<9.23)]=rgb("262936")
            else:
                opening=front&(abs(a)<.59+.045*y)
                cloth[opening&(y>1.77)&(y<7.31)]=lining
                cloth[front&(abs(abs(a)-(.59+.045*y))<.072)&(y>2)&(y<7.30)]=trim
                hem=7.23+.11*np.cos(2*a)
                paint_midriff(cloth,skin,a,y,hem,9.36)
                cloth[(y>9.36)&(y<9.77)]=lining
            if guest in ("zero_two","frieren","megumin"):
                cloth[abs(y-9.23)<.23]=lining
        weave=1.5*np.cos(a*4+.16*y)+np.sin(col*1.63)*np.cos(row*1.08)
        atlas.paste(Image.fromarray(np.uint8(np.clip(cloth+weave[:,:,None],0,255))),(ox,oy))


def accessories(g,rig,parts,ring,hp,hair_front,crown):
    from guest_roblox_portraits import envelope,surface
    theta=np.arange(64)*TAU/64
    def strip(bone,mat,rows):
        # Close the narrow edges of open cloth panels without capping across the wearer.
        inset=np.array([.993,1,.993])
        rig.grid(bone,mat,[np.vstack((row,row[::-1]*inset)) for row in rows],caps=False)

    if g in ("frieren","rem","megumin","zero_two","rin_ishtar","miyabi"):
        hem={"frieren":17.8,"rem":17.3,"megumin":17.7,"zero_two":14.6,"rin_ishtar":15.4,"miyabi":17.5}[g]
        under=np.concatenate([surface(parts[b]) for b in ("body","left_leg","right_leg")])
        rows=[]
        for t in np.linspace(0,1,20):
            y=9.18+(hem-9.18)*t
            flare=(.12+.64*t**1.55+.18*t*np.cos(12*theta))
            if g=="miyabi":flare+=.20*t
            row=envelope(under,y,theta,.14,(0,-.13))
            row[:,0]+=flare*np.sin(theta)
            row[:,2]-=flare*np.cos(theta)
            row[:,1]+=.16*t**2*np.cos(12*theta)
            rows.append(row)
        if g=="miyabi":
            strip("body","coat",[row[8:57] for row in rows])
            strip("body","trim",[row[8:57]*[1.01,1,1.01] for row in rows[-2:]])
        else:
            rig.grid("body","coat" if g=="zero_two" else "pants",rows,caps=False)
            rig.grid("body","lining" if g=="rin_ishtar" else "trim",np.asarray(rows[-2:])*[1.008,1,1.008],caps=False)
        if g=="rem":
            apron=[]
            for i,row in enumerate(rows):
                half=7+round(5*i/(len(rows)-1))
                # Keep the hem scallops on the lower panel and center the bib above it.
                sample=np.linspace(-half,half,25)
                k=np.floor(sample).astype(int);frac=(sample-k)[:,None]
                panel=((1-frac)*row[k]+frac*row[k+1])*[1.025,1,1.025]
                panel[:,1]-=.30*(i/(len(rows)-1))**4*(1+.7*np.cos(5*np.linspace(-np.pi,np.pi,25)))
                apron.append(panel)
            strip("body","white",apron)
            strip("body","lace",np.asarray(apron[-2:])*[1.008,1,1.008])
    if g in ("frieren","megumin"):
        mantle=[]
        for t in np.linspace(0,1,12):
            row=ring(1.92+t*2.5,.11+t*.36)
            a=np.arange(len(row))*TAU/len(row)
            row[:,1]+=t*t*(.7+1.8*(1-np.cos(a)))
            row[:,0]*=1+.18*t
            row[:,2]+=t*.28
            mantle.append(row)
        rig.grid("body","white" if g=="frieren" else "lining",mantle,caps=False)
        rig.grid("body","trim",np.asarray(mantle[-2:])*[1.01,1,1.01],caps=False)
        rig.orb("body","jewel",mantle[-2][0]+[0,-.20,-.13],(.24,.32,.14))
    if g in ("zero_two","nicole_demara","miyabi","megumin"):
        belt=[ring(h,.13) for h in (8.97,9.54)]
        rig.grid("body","leather",belt,caps=False)
        fastener=belt[0][0]+[0,.27,-.12]
        rig.orb("body","metal",fastener,(.42,.26,.09))
        rig.orb("body","lining_dark",fastener+[0,0,-.08],(.22,.12,.055))
    if g in ("miyabi","nicole_demara"):
        hip=ring(9.65,.20)[9 if g=="nicole_demara" else -9]
        rig.loft("body","leather",[(0,.52,.27,.30),(.16,.66,.31,.33),(1.04,.58,.31,.31)],hip,12,exp=.46)
        rig.orb("body","jewel",hip+[0,.31,-.29],(.16,.13,.08))

    def hair_ties(length,color):
        for s in (-1,1):
            start=hp(s*4.02,-4.35,.57)
            rig.orb("headwear",color,start,(.74,.55,.65))
            for k in (-1,0,1):
                bend=1.45*np.sign(k) if g=="nicole_demara" else k*.62
                path=[start+[k*.36,0,.10],start+[s*(2.0+k*.36),length*.23,.82],
                      start+[s*(1.28+bend),length*.78,1.28],start+[s*(.8+k*.67),length-.68*abs(k),.72]]
                rig.sweep("headwear","strand" if k==1 else "hair",path,.85,.64,n=12,steps=15,
                          shape=lambda t:.10+(1-t)**.52)
            if g=="rin_ishtar":
                for direction in (-1,1):
                    rig.sweep("headwear","black",[start+[0,0,-.26],start+[s*.54,direction*.24,-.61],
                        start+[s*1.12,direction*1.01,-.64],start+[s*1.33,direction*1.34,-.43]],.44,.20)
    if g in ("frieren","rin_ishtar","nicole_demara"):
        hair_ties({"frieren":13.9,"rin_ishtar":12.3,"nicole_demara":8.3}[g],"trim" if g!="rin_ishtar" else "black")
    if g=="frieren":
        for s in (-1,1):
            path=[hp(s*3.39,-2.65,-1.98),hp(s*4.30,-2.63,-2.26),
                  hp(s*5.57,-3.37,-2.27),hp(s*6.48,-3.80,-2.12)]
            rig.sweep("headwear","skin",path,.67,.35,guide=(0,1,0),steps=12,shape=lambda t:1.02-.98*t)
            rig.cord("headwear","gold",[hp(s*4.80,-2.78,-2.44),hp(s*4.80,-1.94,-2.44)],.085)
            rig.orb("headwear","jewel",hp(s*4.80,-1.65,-2.44),(.26,.39,.22))
    if g in ("miyabi","zero_two"):
        for s in (-1,1):
            tall=g=="miyabi"
            extent=3.75 if tall else 1.72
            base=hp(s*(2.39 if tall else 1.68),crown+.76,-.26 if tall else -1.63)
            def ear(x,y,z,si,co,i):
                return x+s*(extent-y)*.26,y,z
            scale=1.03 if tall else .52
            rig.loft("headwear","hair" if tall else "coat",[(0,.04,.06,.06),
                (.34*extent,.48*scale,.36*scale,.39*scale),(.76*extent,.98*scale,.56*scale,.57*scale),
                (extent,1.03*scale,.61*scale,.60*scale)],base-[0,extent,0],16,warp=ear)
            if tall:
                rig.loft("headwear","lining",[(extent*.22,.04,.04,.04),(extent*.66,.47,.11,.10),
                    (extent*.85,.65,.12,.11)],base-[0,extent,.41],14,warp=ear)
    if g=="rem":
        for i in range(-5,6):
            a=i/5*1.16
            x=np.sin(a)*4.52
            y=crown+1.24+(1-np.cos(a))*4.48
            z=hair_front(x,y)
            pos=hp(x,y,(z if z is not None else -2.67)-.19)
            rig.orb("headwear","white",pos,(.52,.58,.36),12)
            rig.orb("headwear","lace",pos+[0,.30,-.11],(.28,.31,.23),10)
        for dx,dy in ((.53,.61),(.53,-.61)):
            x,y=-2.20,-4.3
            z=hair_front(x,y)
            if z is not None:
                rig.cord("headwear","jewel",[hp(x-dx,y-dy,z-.19),hp(x+dx,y+dy,z-.19)],.10)
    if g=="megumin":
        center=hp(0,crown+1.27,.04)
        def tilt(x,y,z,si,co,i):
            return x*1.23+max(-y,0)**1.67*.23,y+.13*x+.21*co,z*1.14
        rig.loft("headwear","lining",[(-.29,5.54,4.74,4.79),(-.10,5.69,4.88,4.91),(.07,5.56,4.75,4.78)],center,40,warp=tilt)
        rig.loft("headwear","trim",[(-.24,5.64,4.85,4.87),(-.13,5.70,4.90,4.93)],center,40,warp=tilt)
        rig.loft("headwear","lining",[(-5.62,.09,.10,.10),(-4.67,.70,.64,.69),
            (-3.14,1.55,1.48,1.54),(-1.40,2.65,2.21,2.40),(-.19,3.29,2.83,2.88)],center,36,warp=tilt)
        rig.loft("headwear","trim",[(-1.26,2.79,2.38,2.51),(-.92,2.99,2.57,2.67)],center,36,warp=tilt)
        for s in (-1,1):
            cx=s*1.48
            rig.cord("headwear","jewel",[center+[cx-.36,-1.57,-1.99],center+[cx,-1.91,-2.01],
                center+[cx+.36,-1.56,-2.08],center+[cx,-1.26,-2.19],center+[cx-.36,-1.57,-1.99]],.09)
