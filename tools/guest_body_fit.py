"""Clothed guest fitting rig. Coordinates are Minecraft model pixels, with negative Z forward."""
import argparse
import base64
import io
import json
import uuid
import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw
from guest_portrait_models import Rig, rectangle, SIZE, TAU, unit


def cloth_section(r, bone, material, rows, pivot=(0,0,0), square=.68, sides=24):
    """Each row gives world height, x centre, radius and the front and rear Z coordinates."""
    rings=[]
    for y,x,radius,front,rear in rows:
        ring=[]
        for j in range(sides):
            angle=j*TAU/sides
            s,c=math.sin(angle),math.cos(angle)
            px=x+radius*math.copysign(abs(s)**square,s)
            z=(front+rear)/2-(rear-front)/2*math.copysign(abs(c)**square,c)
            ring.append(np.array((px,y,z))-pivot)
        rings.append(ring)
    r.grid(bone,material,rings)


def hand(r,bone,side,pivot):
    # A closed concave outline cut through a deep palm. The opening faces the viewer
    # and remains open underneath. A side elevation sees the broad bevelled back.
    outline=np.array([(-.89,.37),(-.89,-.24),(-.78,-.70),(-.55,-.98),(-.20,-1.11),
        (.19,-1.16),(.54,-1.00),(.79,-.72),(.94,-.28),(.98,.25),(.96,.86),
        (.88,1.33),(.70,1.57),(.53,1.52),(.42,1.26),(.38,.86),(.34,.43),
        (.26,.13),(-.30,.14),(-.38,.50),(-.42,.92),(-.53,1.12),(-.68,1.08),
        (-.80,.88)],float)
    # Counterclockwise in y-down screen coordinates. Offset both convex and concave
    # corners along their angle bisectors to round the lips without filling the grip.
    tangent=unit(np.roll(outline,-1,axis=0)-outline)
    inward=np.c_[-tangent[:,1],tangent[:,0]]
    mids=unit(inward+np.roll(inward,1,axis=0))
    inset=mids/np.maximum((mids*inward).sum(axis=1,keepdims=True),.31)
    def smooth_loop(points):
        following=np.roll(points,-1,axis=0)
        return np.stack((.75*points+.25*following,.25*points+.75*following),axis=1).reshape(-1,2)
    # Each side of the palm rolls into a broad flat face. Give the cap its own
    # consistent normal, independent of how the concave opening is triangulated.
    palm_basis=np.array([[side,.12*side,0],[0,1.075,0],[0,-.30,1]])
    normal_basis=np.linalg.inv(palm_basis).T
    points=[];faces=[];normals=[]
    bevel_angles=(-90,-67.5,-45,-22.5,0,0,22.5,45,67.5,90)
    for ring,angle in enumerate(bevel_angles):
        a=math.radians(angle)
        depth=(-.63 if ring < len(bevel_angles)//2 else .63)+.24*math.sin(a)
        bevel=.105*(1-math.cos(a))
        p=outline+inset*bevel
        curve=smooth_loop(p)
        tangent=unit(np.roll(curve,-1,axis=0)-np.roll(curve,1,axis=0))
        for (x,y),(tx,ty) in zip(curve,tangent):
            points.append((side*(6.83+x+.12*y),11.57+y*1.075,depth-.30*y-.62))
            normals.append(unit(normal_basis@[math.cos(a)*ty,-math.cos(a)*tx,math.sin(a)]))
    n=len(curve)
    last=(len(bevel_angles)-1)*n
    for ring in range(len(bevel_angles)-1):
        for j in range(n):
            a=ring*n+j;b=ring*n+(j+1)%n
            faces.append([a,b,b+n,a+n])
    def cross(a,b,c):return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    face=smooth_loop(outline+inset*.105)
    remaining=list(range(n))
    while len(remaining)>2:
        for at,k in enumerate(remaining):
            prev,nxt=remaining[at-1],remaining[(at+1)%len(remaining)]
            a,b,c=face[[prev,k,nxt]]
            if cross(a,b,c)<.000001:continue
            inside=any(min(cross(a,b,face[t]),cross(b,c,face[t]),cross(c,a,face[t]))>=0
                       for t in remaining if t not in (prev,k,nxt))
            if inside:continue
            faces.append([nxt,k,prev,prev])
            faces.append([prev+last,k+last,nxt+last,nxt+last])
            remaining.pop(at)
            break
        else:raise ValueError("Palm outline intersects itself")
    p=np.array(points)-pivot
    if side<0:faces=[list(reversed(q)) for q in faces]
    part=r.parts[bone];offset=len(part["vertices"])
    x,y,w,h=rectangle("skin")
    for pos,normal in zip(p,normals):
        part["vertices"].append([*np.round(pos,4),round((x+w*.5)/SIZE,6),round((y+h*.5)/SIZE,6),*np.round(normal,5)])
    part["faces"].extend([[offset+i for i in q] for q in faces])


def rounded_profile(rows):
    """Round the corners of a longitudinal cut while retaining its end rings."""
    source=np.asarray(rows,dtype=float)
    inset=np.empty((2*len(source),source.shape[1]))
    inset[0],inset[-1]=source[0],source[-1]
    inset[1:-1:2]=.75*source[:-1]+.25*source[1:]
    inset[2:-1:2]=.25*source[:-1]+.75*source[1:]
    return inset


def sculpt_arm(r,bone,side,pivot):
    # The rings follow the shoulder-to-wrist centreline. A level cut through a
    # bent elbow used to leave a corner along the back and a ledge at the wrist.
    rows=rounded_profile(rounded_profile([
        # y, distance from centre, rearward displacement, half width and depth
        (.60,3.13,.12,.12,.18),(.67,3.18,.12,.43,.55),(.86,3.24,.12,.72,.84),
        (1.16,3.34,.13,.91,1.05),(1.64,3.49,.16,.97,1.09),(2.23,3.67,.25,.92,1.04),
        (3.12,4.00,.48,.84,.99),(4.18,4.39,.88,.77,.93),(4.92,4.67,1.12,.73,.90),
        (5.44,4.88,1.25,.70,.88),(5.80,5.04,1.26,.71,.89),(6.20,5.20,1.18,.74,.94),
        (6.83,5.44,.98,.76,.98),(7.52,5.69,.67,.73,.90),(8.30,5.98,.32,.66,.79),
        (9.04,6.23,.02,.59,.66),(9.62,6.43,-.24,.54,.56),(9.86,6.53,-.35,.50,.50),
        (10.05,6.59,-.40,.43,.44),(10.32,6.66,-.43,.425,.43),(10.55,6.74,-.47,.44,.46)]))
    sample_y=np.r_[np.linspace(rows[0,0],1.55,8,endpoint=False),
                   np.linspace(1.55,9.6,30,endpoint=False),np.linspace(9.6,rows[-1,0],7)]
    rows=np.column_stack([sample_y,*[np.interp(sample_y,rows[:,0],column) for column in rows[:,1:].T]])
    centers=np.column_stack((side*rows[:,1],rows[:,0],rows[:,2]))
    forward=unit(np.gradient(centers,axis=0))
    across=unit(np.cross(forward,[0,0,1]))
    depth=unit(np.cross(across,forward))
    a=np.arange(24)*TAU/24
    s,c=np.sin(a),np.cos(a)
    rings=[]
    for i,(y,_,_,width,thickness) in enumerate(rows):
        # A round deltoid and forearm ease into the softly squared wrist in the reference.
        edge=.96-.22*float(np.clip((y-7.8)/2.5,0,1))
        x=width*np.sign(s)*np.abs(s)**edge
        z=-thickness*np.sign(c)*np.abs(c)**edge
        rings.append(centers[i]-pivot+x[:,None]*across[i]+z[:,None]*depth[i])
    r.grid(bone,"skin",rings)
    hand(r,bone,side,pivot)


def fitted_top(r):
    # One continuous cloth surface from neckline to hem. Broad off-centre volumes
    # give the bust a round profile while the fabric spans the middle of the chest.
    cut=rounded_profile([
        (.70,2.17,-1.06,1.18),(1.03,2.79,-1.49,1.39),(1.50,3.22,-2.16,1.53),
        (2.04,3.47,-2.78,1.66),(2.64,3.65,-3.01,1.73),(3.25,3.74,-3.10,1.78),
        (3.83,3.70,-3.19,1.78),(4.31,3.56,-3.23,1.80),(4.77,3.31,-3.12,1.81),
        (5.24,3.02,-3.22,1.80),(5.77,2.73,-2.93,1.73),(6.22,2.53,-2.52,1.62),
        (6.67,2.39,-2.09,1.53),(7.11,2.33,-1.77,1.49),(7.57,2.43,-1.61,1.57),
        (8.03,2.68,-1.82,1.71)])
    rings=[]
    for y,width,front,rear in cut:
        # Longer lower arcs let the shirt drape from the chest over the lower
        # ribs. The upper arc and the point of maximum projection stay in place.
        panel_height=2.90 if y>3.48 else 2.0
        drape=max(0,min(1,(6.65-y)/1.60))
        drape=drape*drape*(3-2*drape)
        ring=[]
        for j in range(48):
            angle=TAU*j/48
            s,c=math.sin(angle),math.cos(angle)
            x=width*math.copysign(abs(s)**.90,s)
            z=(front+rear)/2-(rear-front)/2*math.copysign(abs(c)**.90,c)
            if c>0:
                front_z=z
                for center in (-1.52,1.52):
                    dome=1-((x-center)/1.98)**2-((y-3.48)/panel_height)**2
                    if dome<=0:
                        continue
                    surface=-1.38-3.04*math.sqrt(dome)
                    # Blend the cloth between the two curved panels and the rib cage.
                    rounding=max(0,1-abs(front_z-surface)/.80)
                    front_z=min(front_z,surface)-.20*rounding*rounding
                z+=(front_z-z)*min(1,c/.70)*drape
            ring.append((x,y,z))
        rings.append(ring)
    r.grid("body","top",rings)


def body(r):
    # Height and front/rear extents were read independently from the supplied front
    # and profile views. The back of the hip and the forward knee peak at different heights.
    def torso(mat,rows):
        cloth_section(r,"body",mat,[(y,0,w,f,b) for y,w,f,b in rows])
    torso("skin",rounded_profile([(-.95,.80,-.78,.87),(-.40,.89,-.85,.96),
                  (.28,1.00,-.96,1.04),(.48,1.77,-1.02,1.10),(.64,2.55,-.99,1.13),
                  (.82,3.03,-.94,1.14),(1.10,3.31,-.99,1.19),(1.68,3.12,-1.09,1.18)]))
    fitted_top(r)
    torso("rib",[(7.83,2.61,-1.77,1.68),(8.02,2.71,-1.86,1.75),(8.14,2.73,-1.87,1.76)])
    # The seat lining continues from the waistband. Its curved underside stays
    # inside the trouser legs at rest and covers the hip opening during a step.
    torso("pants",[(8.07,2.65,-1.77,1.69),(8.47,2.75,-1.85,1.81),(8.89,3.06,-2.02,2.04),
          *rounded_profile([(9.42,3.55,-2.21,2.42),(9.60,3.67,-2.25,2.54),(10.0,4.08,-2.44,2.98),
          (10.62,4.31,-2.62,3.26),(10.95,4.10,-2.62,3.09),
          (11.63,3.86,-2.69,3.21),(12.20,3.31,-2.63,3.20),
          (12.77,2.53,-2.40,3.06),(13.24,1.75,-1.95,2.77),
          (13.64,1.03,-1.35,2.24),(13.93,.48,-.62,1.52),
          (14.07,.13,.06,.76)])])
    torso("black",[(8.67,2.99,-2.00,2.03),(8.88,3.11,-2.07,2.11),(9.03,3.22,-2.10,2.18)])
    for side,name in ((-1,"right"),(1,"left")):
        arm=name+"_arm";leg=name+"_leg"
        pa=np.array(r.parts[arm]["pose"]);pl=np.array(r.parts[leg]["pose"])
        def sections(bone,mat,rows,pivot,square):
            cloth_section(r,bone,mat,[(y,side*x,w,f,b) for y,x,w,f,b in rows],pivot,square)
        sculpt_arm(r,arm,side,pa)
        # Long, bevelled rectangular shins share the trouser profile with the thighs.
        # They have broad flat hems rather than separate ankle and shoe volumes.
        rows=[(10.18,2.03,1.81,-2.00,2.72),(10.61,2.23,2.13,-2.52,3.34),
              (11.28,2.52,2.43,-2.96,3.71),(12.05,2.69,2.60,-3.31,3.88),
              (12.83,2.82,2.71,-3.67,3.88),(13.51,2.89,2.72,-3.82,3.79),
              (14.19,2.94,2.64,-3.78,3.60),(14.83,2.98,2.47,-3.64,3.24),
              (15.39,3.00,2.26,-3.39,2.85),(15.79,3.02,2.11,-3.20,2.43),
              (16.12,3.04,2.02,-3.11,2.20),(16.43,3.07,1.97,-3.04,2.08),
              (17.13,3.12,1.96,-3.00,2.15),(19.13,3.29,1.97,-3.15,2.23),
              (21.40,3.53,2.02,-3.23,2.37),(23.47,3.76,2.12,-3.21,2.58),
              (23.86,3.78,2.11,-3.18,2.57),(24.0,3.78,2.04,-3.11,2.52)]
        # Split the single waist ellipse into two cuffs. Both upper loops share the
        # centre seam at the waistband. Each loop opens into its own articulated leg.
        rings=[]
        top=np.array([(9.37,3.52,-2.21,2.41),(9.60,3.67,-2.25,2.54),
                      (10.00,4.08,-2.44,2.98),(10.62,4.54,-2.71,3.39),
                      (11.28,4.92,-2.97,3.67),(12.05,5.21,-3.29,3.85),
                      (12.88,5.52,-3.66,3.88)])
        lower=rounded_profile(rows)
        heights=np.unique(np.r_[top[:,0],lower[lower[:,0]>top[2,0],0]])
        def power(v,exp):return math.copysign(abs(v)**exp,v)
        for y in heights:
            x,w,front,rear=[np.interp(y,lower[:,0],lower[:,i]) for i in range(1,5)]
            width,upper_front,upper_rear=[np.interp(y,top[:,0],top[:,i]) for i in range(1,4)]
            blend=np.clip((y-9.64)/3.24,0,1)
            blend=blend*blend*(3-2*blend)
            # Round the thigh all the way around. Ease back to the wide, bevelled
            # lower-leg shape through the knee so the trouser hem still sits flat.
            to_shin=float(np.clip((y-14.1)/3.9,0,1))
            to_shin=to_shin*to_shin*(3-2*to_shin)
            section_power=.94+(.41-.94)*to_shin
            # Keep the seat continuous farther down the inseam. The cloth gently
            # separates above the knees, with each half still on its own leg.
            release=float(np.clip((y-12.60)/4.0,0,1))
            seam_fill=blend*(1-release*release*(3-2*release))
            # Tuck the moving top edge under the lining at the hip pivot.
            tuck=1-.22*float(np.clip((10.55-y)/1.18,0,1))**2
            ring=[]
            for j in range(32):
                if j<=24:
                    t=math.pi*j/24
                    sx=width*power(math.sin(t),.68)
                    sz=(upper_front+upper_rear)/2-(upper_rear-upper_front)/2*power(math.cos(t),.68)
                    theta=-.53+(math.pi+1.06)*j/24
                else:
                    sx=0
                    sz=upper_rear+(upper_front-upper_rear)*(j-24)/8
                    theta=math.pi+.53+(math.pi-1.06)*(j-24)/8
                dx=x+w*power(math.sin(theta),section_power)
                dz=(front+rear)/2-(rear-front)/2*power(math.cos(theta),section_power)
                px=(1-blend)*sx+blend*dx
                pz=(1-blend)*sz+blend*dz
                medial=float(np.clip((.30-math.sin(theta))/.78,0,1))
                fill=seam_fill*medial**2*(3-2*medial)*(.91-.09*math.cos(theta))
                seam_fraction=(sz-upper_front)/(upper_rear-upper_front)
                px+=fill*(sx*(x+w)/width-px)
                pz+=fill*(front+seam_fraction*(rear-front)-pz)
                ring.append(np.array([side*px*tuck,y,pz*tuck])-pl)
            # Mirror the right-hand cut while retaining the outward winding.
            rings.append(ring if side>0 else ring[::-1])
        first=len(r.parts[leg]["vertices"])
        r.grid(leg,"pants",rings,caps=False)
        # Average shading across the fly and centre-back seams until the legs diverge.
        for row,y in enumerate(heights):
            weight=float(np.clip((15.5-y)/3.4,0,1))
            weight=weight*weight*(3-2*weight)
            if weight==0:break
            for k in range(33):
                j=k%32 if side>0 else 31-k%32
                front_distance=min(j,32-j)
                rear_distance=abs(j-24)
                distance=min(front_distance,rear_distance)
                blend=weight*max(0,1-distance/3.5)**2
                if blend:
                    v=r.parts[leg]["vertices"][first+33*row+k]
                    normal=np.array(v[5:8])
                    desired=unit(np.array([0,normal[1]*.45,-1 if front_distance<rear_distance else 1]))
                    v[5:8]=list(unit(normal*(1-blend)+desired*blend).round(5))
        # A curved seam and slightly raised waistband keep the two trouser pieces legible.
        sections(leg,"lining",[(23.78,3.78,2.13,-3.21,2.61),(23.93,3.78,2.12,-3.19,2.59),
                               (24.0,3.78,2.05,-3.13,2.53)],pl,.41)


SKULL=[(-9.05,.60,.72,.76),(-8.81,2.12,2.12,2.31),(-8.20,3.63,3.15,3.46),
       (-7.31,4.26,3.53,3.94),(-6.16,4.54,3.73,4.14),(-4.80,4.65,3.79,4.17),
       (-3.70,4.75,3.81,4.09),(-2.91,4.73,3.83,3.98),(-2.27,4.55,3.79,3.81),
       (-1.77,4.17,3.67,3.60),(-1.34,3.52,3.45,3.40),(-1.02,2.63,3.10,3.01),
       (-.85,1.67,2.64,2.45),(-.81,.78,1.70,1.57)]


def head(r):
    r.loft("head","face",rounded_profile(SKULL),n=56,exp=.72,
           uv=lambda p,i,j,n:(.5+p[0]/9.82 if math.cos(TAU*j/n)>=0 else .012,(p[1]+9.05)/8.49))
    # A continuous cut fringe with a long, off-centre lock. The outer contour is defined
    # from brow to jaw and sampled around both sides, all the way to the low tied back.
    fringe=[(-180,-1.20),(-144,-.96),(-113,-.56),(-91,-1.01),(-73,-.70),
            (-62,-2.85),(-50,-4.15),(-41,-5.43),(-35,-5.64),(-25,-5.98),
            (-16,-6.43),(-10,-6.49),(-7,-6.46),(-2,-6.33),(3,-6.25),
            (6,-6.17),(8,-6.21),(13,-6.32),(18,-6.41),(25,-6.35),
            (30,-6.19),(39,-5.84),(46,-5.43),(55,-4.91),(64,-3.20),
            (75,-1.01),(84,-.54),(99,-1.32),(118,-.96),(145,-1.14),(180,-1.20)]
    # Round the edge of the fringe where the locks turn around the cheek.
    angles,hems=rounded_profile(sorted(fringe)).T
    # One continuous crown-to-temple profile avoids a shelf above the fringe.
    shell=rounded_profile([(-10.30,.09,.09,.09),(-10.21,1.17,1.13,1.17),
        (-9.94,2.31,2.04,2.21),(-9.44,3.48,2.91,3.22),(-8.70,4.47,3.68,3.92),
        (-7.74,5.12,4.17,4.47),(-6.47,5.43,4.40,4.71),(-4.85,5.55,4.44,4.75),
        (-3.53,5.46,4.35,4.64),(-2.42,5.22,4.12,4.42),(-1.53,4.79,3.82,4.10),
        (-.89,4.24,3.49,3.66),(-.51,3.72,3.15,3.26)])
    rings=[]
    for row in range(22):
        v=row/21
        ring=[]
        for j in range(96):
            a=TAU*j/96
            signed=(math.degrees(a)+180)%360-180
            hem=np.interp(signed,angles,hems)
            y=shell[0,0]+(hem-shell[0,0])*v
            xrad=np.interp(y,shell[:,0],shell[:,1])
            zrad=np.interp(y,shell[:,0],shell[:,2 if math.cos(a)>=0 else 3])
            twist=.12*math.sin(v*math.pi)*(1+.5*math.cos(a))-.14*(1-v)
            a+=twist;sn,cs=math.sin(a),math.cos(a)
            groove=(.065*math.cos(a*11+1.2*math.sin(4*v))+.025*math.cos(a*21-v))*math.sin(math.pi*v)
            ring.append(((xrad+groove)*math.copysign(abs(sn)**.82,sn),y,
                        -(zrad+groove)*math.copysign(abs(cs)**.82,cs)))
        rings.append(ring)
    r.grid("head","hair",rings,caps=False)
    def wisp(path,w,d):
        r.sweep("head","strand",path,w,d,guide=(1,0,0),steps=20,n=12,
                shape=lambda t:(.46+.67*math.sin(math.pi*t**.80))*(1-.987*t**3))

    # Curved locks follow the crown surface all the way to the hairline.
    # Angle and height control the part, width is measured across the scalp.
    def crown_lock(path,width,depth,offset):
        t=np.linspace(0,1,26)
        weights=np.column_stack(((1-t)**3,3*t*(1-t)**2,3*t*t*(1-t),t**3))
        angle,y=(weights@path).T
        angle=np.radians(angle)
        sn,cs=np.sin(angle),np.cos(angle)
        rx=np.interp(y,shell[:,0],shell[:,1])
        rz=np.where(cs>=0,np.interp(y,shell[:,0],shell[:,2]),np.interp(y,shell[:,0],shell[:,3]))
        radial=unit(np.column_stack((sn/rx,np.zeros_like(sn),-cs/rz)))
        center=np.column_stack((rx*np.sign(sn)*abs(sn)**.82,y,-rz*np.sign(cs)*abs(cs)**.82))
        center+=radial*(offset*(.40+.60*np.sin(t*math.pi)))[:,None]
        along=unit(np.gradient(center,axis=0))
        across=unit(np.cross(radial,along))
        outward=unit(np.cross(along,across))
        section=np.arange(14)*TAU/14
        leaf=(.10*(1-t)+np.sin(math.pi*(.025+.973*t))**.85)*(1-.40*t**1.6)
        rings=[c+size*(width*np.sin(section)[:,None]*a+depth*np.cos(section)[:,None]*o)
               for c,size,a,o in zip(center,leaf,across,outward)]
        r.grid("head","strand",rings)

    # Split the long centre bang just inside the right iris, then sweep the
    # shorter leaves toward the temples. The cheek tips fall at different heights.
    crown_lock([(-19,-10.17),(-16,-8.45),(-13,-6.20),(-3,-4.05)],.52,.14,.18)
    crown_lock([(-4,-10.18),(-17,-7.13),(18,-4.40),(9,-2.24)],.77,.21,.37)
    crown_lock([(12,-10.15),(16,-8.89),(-2,-6.08),(7,-4.58)],.53,.16,.13)
    crown_lock([(-26,-10.07),(-22,-7.92),(-45,-6.52),(-65,-5.28)],1.00,.18,.16)
    crown_lock([(-37,-9.89),(-39,-8.27),(-56,-6.21),(-75,-4.84)],.56,.16,.16)
    crown_lock([(21,-10.07),(18,-8.15),(39,-6.01),(58,-5.16)],.91,.19,.19)
    crown_lock([(30,-9.90),(42,-8.71),(50,-6.59),(68,-4.82)],.49,.16,.16)
    crown_lock([(-34,-9.87),(-24,-8.01),(-40,-6.52),(-62,-5.64)],.31,.13,.30)
    crown_lock([(29,-9.87),(16,-8.56),(32,-6.57),(48,-5.63)],.34,.13,.29)
    for s in (-1,1):
        crown_lock([(s*42,-9.82),(s*56,-8.78),(s*72,-5.35),(s*56,-2.84)],.64,.23,.18)
        crown_lock([(s*61,-9.62),(s*78,-8.17),(s*91,-3.41),(s*79,-1.24)],.75,.19,.12)
        crown_lock([(s*102,-9.73),(s*108,-7.32),(s*118,-2.48),(s*158,-.75)],.73,.20,.09)
        crown_lock([(s*131,-9.68),(s*141,-6.83),(s*147,-3.37),(s*172,-1.03)],.66,.18,.12)
        late=.34 if s<0 else 0
        wisp([(s*4.05,-7.51,-2.53),(s*5.28,-5.05,-3.39),
              (s*4.73,-1.89,-3.16),(s*(3.12+late),-.91+late,-3.21)],.52,.20)
        wisp([(s*4.82,-6.70,-1.61),(s*5.90,-3.68,-1.70),
              (s*5.01,-.94,-2.45),(s*(3.77-late),.65+late,-2.71)],.49,.18)
        wisp([(s*4.65,-5.50,.02),(s*6.25,-1.84,-.18),
              (s*4.03,.16,-1.43),(s*(2.12+late),1.20-late,-2.16)],.55,.22)
    wisp([(-3.14,-8.89,-1.67),(-5.52,-7.03,-1.95),
          (-5.20,-4.63,-1.53),(-6.27,-3.70,-1.67)],.36,.15)
    wisp([(-4.68,-6.25,-2.36),(-5.36,-3.97,-2.44),
          (-4.84,-1.48,-2.73),(-3.17,-.42,-3.06)],.21,.12)
    wisp([(4.58,-5.62,-2.22),(5.57,-3.34,-2.75),
          (4.41,-.34,-2.38),(2.47,.75,-2.68)],.24,.13)
    r.orb("head","black",(0,-1.76,3.97),(1.20,.70,1.10))
    r.orb("head","hair",(.30,-1.30,4.67),(1.71,1.53,1.02),20)
    for s in (-1,1):
        wisp([(s*.29,-1.89,4.64),(s*2.64,-1.94,6.19),
              (s*1.98,.70,5.58),(s*.62,.76,4.77)],.63,.32)
        wisp([(s*.34,-1.14,4.87),(s*1.88,-.35,5.94),
              (s*1.52,1.05,5.77),(s*1.77,1.36,5.16)],.52,.25)


EYE_STYLES=("almond","wide","half_lidded")


def paint_face(style,skin):
    """Paint the eyes and small smile onto the curved front of the head."""
    # Coordinates run from the inner to the outer corner of one eye. Mirror the
    # lid shape, but light both irises from the same side.
    if style=="almond":
        upper=[(-38,146),(-23,123),(12,113),(36,130)]
        lower=[(36,130),(32,151),(-6,164),(-38,146)]
        cx,cy,rx,ry=1,139,19,21
        wing=(44,120); lash=5.8; glint=3.5
    elif style=="wide":
        upper=[(-33,147),(-35,108),(19,99),(34,130)]
        lower=[(34,130),(36,166),(-20,174),(-33,147)]
        cx,cy,rx,ry=0,139,23,28
        wing=(48,115); lash=6.2; glint=5.0
    elif style=="half_lidded":
        upper=[(-38,139),(-19,133),(13,131),(36,129)]
        lower=[(36,129),(27,156),(-7,163),(-38,139)]
        cx,cy,rx,ry=-4,136,19,21
        wing=(44,123); lash=6.4; glint=2.8
    else:
        raise ValueError(f"Unknown eye style: {style}")
    t=np.linspace(0,1,40)
    weights=np.stack(((1-t)**3,3*t*(1-t)**2,3*t**2*(1-t),t**3),axis=1)
    lid=weights@upper
    rim=weights@lower
    scale=4
    yy,xx=np.mgrid[:252,:252]
    cheek=np.exp(-((abs(xx-126)-73)/27)**2-((yy-174)/13)**2)
    face_pixels=np.array(skin)+cheek[:,:,None]*[2,-14,-8]
    if style=="wide":
        # A light mauve wash follows the outer lid and fades before the brow.
        eye_x=abs(xx-126)-52
        shadow_y=np.interp(eye_x,lid[:,0],lid[:,1])+3
        shadow=np.exp(-((eye_x-19)/23)**2-((yy-shadow_y)/7.2)**2)
        face_pixels+=shadow[:,:,None]*[-14,-23,-16]
    face=Image.fromarray(face_pixels.round().astype("uint8")).resize((252*scale,252*scale),Image.Resampling.BICUBIC)
    ink=(45,35,53)
    for side in (-1,1):
        center=126+side*52
        def xy(path):
            return [(round((center+side*x)*scale),round((y+6)*scale)) for x,y in path]
        if style=="wide":
            brow=weights@[(-25,98),(-12,92),(9,83),(34,95)]
            thickness=1.85*np.sin(np.pi*(.04+.95*t))**.75*(1.12-.38*t)
            edge=brow.copy()
            edge[:,1]+=2*thickness
            ImageDraw.Draw(face).polygon(xy(np.vstack((brow,edge[::-1]))),fill=(155,127,140))
        opening=Image.new("L",face.size)
        ImageDraw.Draw(opening).polygon(xy(np.vstack((lid,rim))),fill=255)
        eye=Image.new("RGB",face.size,(249,247,237))
        draw=ImageDraw.Draw(eye)
        iris_x=center+side*cx
        def ellipse(x,y,a,b,tone):
            y+=6
            draw.ellipse(tuple(round(v*scale) for v in (x-a,y-b,x+a,y+b)),fill=tone)
        ellipse(iris_x,cy,rx,ry,(42,73,64))
        ellipse(iris_x,cy+1.8,rx-1.8,ry-1.8,(85,146,108))
        ellipse(iris_x,cy+ry*.46,rx*.76,ry*.47,(155,205,142))
        ellipse(iris_x,cy-.9,rx*.48,ry*.68,(42,74,60))
        ellipse(iris_x-rx*.31,cy-ry*.31,glint,glint,(254,251,234))
        face.paste(eye,mask=opening)
        outer=lid.copy()
        outer[:,1]-=lash*(np.sin(np.pi*t*.81)**.72)
        draw=ImageDraw.Draw(face)
        if style=="wide":
            flick=weights@[lid[-1],(38,128),(44,118),wing]
            return_edge=weights@[wing,(43,115),(38,122),outer[-1]]
            draw.polygon(xy(np.vstack((lid,flick,return_edge,outer[::-1]))),fill=ink)
        else:
            draw.polygon(xy(np.vstack((lid,wing,outer[::-1]))),fill=ink)
        rim_tip=rim[:16].copy()
        rim_tip[:,1]+=1.8*(1-np.linspace(0,1,len(rim_tip)))
        draw.polygon(xy(np.vstack((rim[:16],rim_tip[::-1]))),fill=(78,61,66))
    # Two shallow lobes meet at a tiny lift. Taper the corners so the gesture
    # reads as a smile even with nearest-neighbour sampling in the game.
    left=weights@[(107,192),(111,197),(121,198),(126,194)]
    right=weights@[(126,194),(131,197),(140,196),(144,190)]
    smile=np.vstack((left,right[1:]))
    width=1.45*(.20+.80*np.sin(np.linspace(0,math.pi,len(smile)))**.45)
    upper=smile.copy()
    lower=smile.copy()
    upper[:,1]-=width
    lower[:,1]+=width
    ImageDraw.Draw(face).polygon([tuple(point) for point in np.vstack((upper,lower[::-1]))*scale],fill=(105,66,74))
    return face.resize((252,252),Image.Resampling.LANCZOS)


def texture(eye_style="wide"):
    skin=(245,215,205)
    tex=Image.new("RGB",(SIZE,SIZE),skin)
    colors={"skin":skin,"top":(41,38,47),"rib":(48,43,52),"pants":(225,216,223),
            "lining":(208,199,210),"black":(47,41,53),"hair":(59,50,76),"strand":(69,58,86)}
    for material,color in colors.items():
        x,y,w,h=rectangle(material)
        tile=np.zeros((h,w,3),dtype=np.uint8)
        for iy in range(h):
            for ix in range(w):
                u=ix/(w-1);v=iy/(h-1)
                shade=1.
                sheen=0.
                if material=="hair":
                    phase=TAU*(u*8+.20*math.sin(TAU*(u+v*.55)))
                    shade=.88+.095*math.cos(phase)+.035*math.cos(3*phase+v*5)-.09*v
                    sheen=.35*math.exp(-((v-.31-.07*math.cos(phase))/.19)**2)
                elif material=="strand":
                    # Light runs along each leaf with a short satin glint high on the curve.
                    phase=TAU*(u+.07*math.sin(4*v))
                    ridge=.5+.5*math.cos(phase)
                    # Several unequal strokes curve down the wide side of each lock.
                    # Pull them together near the tip to follow the taper.
                    sweep=.043*math.sin(5*v)+.025*v
                    strokes=0.
                    for lane,breadth,intensity in ((-.13,.045,.57),(.03,.025,.71),(.145,.019,.40)):
                        dist=(u-sweep-lane*(1-.5*v)+.5)%1-.5
                        strokes+=intensity*math.exp(-(dist/breadth)**2)
                    shade=.70+.21*ridge+.07*math.cos(5*phase+v*4)-.09*v
                    glint=math.exp(-((v-.34-.055*math.cos(3*phase))/.21)**2)
                    sheen=ridge*(.18+.49*glint)+strokes*(.23+.86*glint)
                tile[iy,ix]=np.clip(np.array(color)*shade+sheen*np.array([38,31,51]),0,255)
        tex.paste(Image.fromarray(tile),(x,y))
    tex.paste(paint_face(eye_style,skin),(2,2))
    return tex


def fitting_joints(r):
    r.joint("head");r.joint("hat","head");r.joint("body")
    for s,name in ((-1,"right"),(1,"left")):
        r.joint(name+"_arm",at=(s*4.55,2,0))
        r.joint(name+"_leg",at=(s*2.24,11.02,0))
    # Compatible with the town renderer's seasonal visibility switches.
    for name,parent in (("winter_collar","body"),("winter_left_arm","left_arm"),("winter_right_arm","right_arm"),
                        ("harvest_pin","head"),("harvest_collar","body"),("spring_pin","head"),("spring_collar","body")):
        r.joint(name,parent)


def build():
    r=Rig("guest_fit")
    fitting_joints(r)
    head(r)
    # Apply the width change to the complete head, including the tied hair.
    # Texture space stays attached to the face and normals follow the new curvature.
    vertices=np.asarray(r.parts["head"]["vertices"])
    head_width=.85
    vertices[:,0]=(vertices[:,0]*head_width).round(4)
    vertices[:,5:8]=unit(vertices[:,5:8]/[head_width,1,1]).round(5)
    r.parts["head"]["vertices"]=vertices.tolist()
    body(r)
    for p in r.parts.values():
        if p["vertices"]:
            v=np.asarray(p["vertices"])
            p["bounds"]=[*v[:,:3].min(axis=0),*v[:,:3].max(axis=0)]
    return {"parts":list(r.parts.values())}


def write_blockbench(model, pixels, output, *, name="Guest body fit", texture_name="guest_fit.png",
                     keep_sockets=False,hidden_prefixes=()):
    def ident(key):
        return str(uuid.uuid5(uuid.NAMESPACE_URL,"jugcraft:guest_fit/"+key))
    elements=[];groups=[]
    texture_bytes=io.BytesIO()
    pixels.save(texture_bytes,format="PNG")
    origins={"$mesh":np.array([0,24,0])}
    for part in model["parts"]:
        bone=part["name"]
        origins[bone]=origins[part["parent"]]+np.array(part["pose"])*[1,-1,-1]
        if not part["vertices"]:continue
        px,py,pz=part["pose"]
        origin=origins[bone].tolist() if keep_sockets else [px,24-py,-pz]
        vertices={};uv={}
        for i,(x,y,z,u,v,*_) in enumerate(part["vertices"]):
            key=format(i,"x")
            vertices[key]=[x,-y,-z]
            uv[key]=[round(u*SIZE,4),round(v*SIZE,4)]
        faces={}
        for i,quad in enumerate(part["faces"]):
            keys=list(dict.fromkeys(format(n,"x") for n in quad))
            faces[format(i,"x")]={"vertices":keys,"uv":{k:uv[k] for k in keys},"texture":0}
        elements.append({"type":"mesh","name":bone,"uuid":ident(bone+"/mesh"),
                         "origin":origin,"rotation":[0,0,0],"vertices":vertices,"faces":faces,
                         "color":0,"visibility":not bone.startswith(hidden_prefixes),"locked":False})
        groups.append({"name":bone,"uuid":ident(bone),"origin":origin,"rotation":[0,0,0],
                       "children":[ident(bone+"/mesh")],"export":True,"visibility":True,"isOpen":False})
    if keep_sockets:
        by_name={g["name"]:g for g in groups}
        groups=[]
        origins={"$mesh":np.array([0,24,0])}
        for part in model["parts"]:
            bone,parent=part["name"],part["parent"]
            origin=origins[parent]+np.array(part["pose"])*[1,-1,-1]
            origins[bone]=origin
            if bone not in by_name:
                by_name[bone]={"name":bone,"uuid":ident(bone),"rotation":[0,0,0],
                               "children":[],"export":True,"visibility":True,"isOpen":False}
            group=by_name[bone]
            group["origin"]=origin.tolist()
            if parent=="$mesh":
                groups.append(group)
            else:
                by_name[parent]["children"].append(group)
    texture_data=base64.b64encode(texture_bytes.getvalue()).decode("ascii")
    doc={"meta":{"format_version":"4.10","model_format":"free","box_uv":False},
         "name":name,"resolution":{"width":SIZE,"height":SIZE},"elements":elements,
         "outliner":groups,"textures":[{"id":"0","name":texture_name,"uuid":ident("atlas"),
         "width":SIZE,"height":SIZE,"uv_width":SIZE,"uv_height":SIZE,"mode":"bitmap",
         "internal":True,"saved":True,"source":"data:image/png;base64,"+texture_data}]}
    output.write_text(json.dumps(doc,separators=(",",":"))+"\n",encoding="utf8")


if __name__=="__main__":
    cli=argparse.ArgumentParser(description=__doc__)
    cli.add_argument("output",type=Path)
    cli.add_argument("--eyes",choices=EYE_STYLES,default="wide")
    args=cli.parse_args()
    folder=args.output
    folder.mkdir(parents=True,exist_ok=True)
    model=build()
    pixels=texture(args.eyes)
    (folder/"guest_fit.json").write_text(json.dumps(model,separators=(",",":"))+"\n",encoding="utf8")
    pixels.save(folder/"guest_fit.png")
    write_blockbench(model,pixels,folder/"guest-body-fit.bbmodel")
