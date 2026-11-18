"""Countertop copperware, carved wood and the six dishes served at Bruni's shop."""
from PIL import Image, ImageDraw
from deepstead_wares import ART, save_image
from guild_materials import COLORS, EXTRA, badge, box as b, mesh, octagon as course, turn
from menu_meal_models import bowl, plate


def samovar(heat):
    p=[]
    for x,z,y,X,Z,Y,step,mat in [(3,3,0,13,13,1,1.5,"iron"),(4,4,1,12,12,3.3,1,"brass"),
            (5.4,5.4,3.3,10.6,10.6,4.4,1,"iron"),(4.2,4.2,4.4,11.8,11.8,5.3,1.3,"copper"),
            (3.4,3.4,5.3,12.6,12.6,7.4,1.6,"copper"),(3,3,7.4,13,13,9.9,2,"copper"),
            (3.6,3.6,9.9,12.4,12.4,10.6,1.7,"brass"),(4,4,10.6,12,12,11.4,1.5,"copper"),
            (5,5,11.4,11,11,12,1,"brass")]:
        p+=course(x,y,z,X,Y,Z,step,mat)
    p.extend([b(6.7,12,6.7,9.3,14.3,9.3,"iron"),b(6.2,14.3,6.2,9.8,15,9.8,"brass"),
              b(7.2,5.7,1.5,8.8,6.8,3.4,"brass"),b(7.2,4.7,1,8.8,6.1,1.5,"brass"),
              b(7.7,6.8,1.9,8.3,7.6,2.5,"iron"),b(6.7,7.6,1.7,9.3,8.2,2.7,"oak")])
    for lo,hi,L,R in ((.45,1.45,1.45,3.45),(14.55,15.55,12.55,14.55)):
        p.extend([b(L,6.4,7.25,R,7.2,8.75,"brass"),b(lo,6.4,7.25,hi,10.1,8.75,"oak"),
                  b(L,9.3,7.25,R,10.1,8.75,"brass")])
    for x in (5.8,7.6,9.4):
        vent=b(x,1.5,3.78,x+.9,2.9,4,"ember" if heat else "basalt")
        if heat:
            vent["light_emission"]=9
        p.append(vent)
    mesh("block/copper_samovar_"+str(heat),p,"copper")


def basket():
    p=course(.7,0,2.4,15.3,1,13.6,2,"oak")
    # Rim corners are lashed, the low front exposes the loaves on the checked napkin.
    for z in (2.4,12.6):
        p.extend([b(2.6,1,z,13.4,3.2,z+1,"willow"),b(2.5,3.2,z-.15,13.5,3.75,z+1.15,"oak")])
    for x in (.7,14.1):
        p.extend([b(x,1,4.4,x+1.2,3.6,11.6,"willow"),b(x-.15,3.6,4.25,x+1.35,4.2,11.75,"willow")])
    for x in (1.6,13.2):
        for z in (3.3,11.4):p.append(b(x,1,z,x+1.2,3.5,z+1.3,"willow"))
    p.extend([b(2,1,4,14,1.7,12,"linen"),b(3.2,3.75,2.25,6.1,4.05,4,"linen"),
              b(3.2,2.05,2,6.1,4.05,2.25,"linen")])
    for x,z,lenz in ((3,4.8,6.4),(8.3,4.1,7.4)):
        p+=course(x,1.7,z,x+4.4,3.9,z+lenz,.8,"crust")
        for i in range(3):
            Z=z+.55+i*(lenz-1.4)/3
            off=(i%2)*.8
            lobe=b(x+.35+off,3.88,Z,x+3.2+off,5.2,Z+1.75,"crust")
            badge(lobe,"up","braid",(2,2,10,10))
            p.append(turn(lobe,"y",22.5 if i%2 else -22.5,(x+2.2,4,Z+.8)))
    mesh("block/braided_bread_basket",p,"willow")


def tankards():
    p=course(2,0,3,14,1.1,13,2,"oak")+course(5,1.1,5,11,2,11,1,"brass")
    p.extend([b(7.25,2,7.25,8.75,14.1,8.75,"oak"),b(6.8,14.1,6.8,9.2,15,9.2,"brass")])
    # The pegs enter the open space inside each handle, underneath its upper return.
    def mug(x,y,z,mat,left):
        p.extend([b(x,y,z,x+3.2,y+.6,z+3.2,"iron"),b(x,y+.6,z,x+3.2,y+3.8,z+.55,mat),
                  b(x,y+.6,z+2.65,x+3.2,y+3.8,z+3.2,mat),b(x,y+.6,z+.55,x+.55,y+3.8,z+2.65,mat),
                  b(x+2.65,y+.6,z+.55,x+3.2,y+3.8,z+2.65,mat)])
        for Z in (z,z+2.68):
            p.append(b(x-.08,y+3.8,Z-.08,x+3.28,y+4.13,Z+.6,"iron"))
        for X in (x,x+2.68):
            p.append(b(X-.08,y+3.8,z+.52,X+.6,y+4.13,z+2.6,"iron"))
        H=x-1.6 if left else x+3.2
        end=H if left else H+1
        p.extend([b(H,y+.8,z+1.1,H+1.6,y+1.4,z+2,"brass"),b(H,y+2.8,z+1.1,H+1.6,y+3.45,z+2,"brass"),
                  b(end,y+1.4,z+1.1,end+.6,y+2.8,z+2,"brass")])
    mug(1.35,7,6.3,"teal",False)
    mug(11.45,9.3,6.5,"copper",True)
    mug(4.25,3.4,2.3,"plum",False)
    p.extend([b(4.85,9.2,7.55,7.25,9.8,8.2,"iron"),b(8.75,11.5,7.75,11.15,12.1,8.4,"iron"),
              b(7.8,5.6,3.8,8.5,6.2,7.25,"iron")])
    mesh("block/tankard_tree",p,"oak")


def gong():
    p=[]
    for x in (0,12.5):
        p.extend([b(x,0,4.6,x+3.5,1.3,11.4,"basalt"),b(x+.7,1.3,7,x+2.8,13.8,9,"oak"),
                  b(x+.4,2.8,6.75,x+3.1,3.6,9.25,"brass")])
    p.extend([b(0,13.8,6.6,16,15.4,9.4,"oak"),b(4.8,11.3,7.65,5.5,13.8,8.35,"iron"),
              b(10.5,11.3,7.65,11.2,13.8,8.35,"iron")])
    # UVs share a single circular hammered plate across its stepped silhouette.
    for y,Y,x,X in ((2.6,3.4,6,10),(3.4,4.45,4.8,11.2),(4.45,5.6,3.8,12.2),(5.6,9.8,3.15,12.85),
                      (9.8,10.9,3.8,12.2),(10.9,12,4.8,11.2),(12,12.8,6,10)):
        band=b(x,y,7.25,X,Y,8.75,"brass")
        for side in ("north","south"):
            badge(band,side,"gong",((x-3)*1.6,(12.8-Y)*1.55,(X-3)*1.6,(12.8-y)*1.55))
        p.append(band)
    p.extend([b(6.6,6.3,6.6,9.4,9.1,7.25,"copper"),b(7.2,6.9,6.2,8.8,8.5,6.6,"brass")])
    pivot=(13.8,2,6.1)
    for part in (b(13.5,1.3,5.8,14.1,6.7,6.4,"oak"),b(12.9,6.7,5.2,14.7,8.2,7,"linen")):
        p.append(turn(part,"z",22.5,pivot))
    mesh("block/shift_supper_gong",p,"brass")


def served(name,p,particle):
    # Paint a plan view of the serving and a separate side atlas in the same manner as
    # the town menu dishes. Turned pieces keep their own local top so their pattern turns too.
    ingredients = {"crust", "crumb", "onion", "mushroom", "gills", "pepper", "leaf", "custard", "berry", "amber", "rootcut"}
    keys = sorted({f["texture"][1:] for piece in p for f in piece["faces"].values()} & ingredients)
    tiles = {}
    for key in keys:
        with Image.open(ART/"textures/block/guild"/(key+".png")) as tile:
            tiles[key] = tile.convert("RGB")
    top,side = name+"_top",name+"_side"
    surface = Image.new("RGB",(16,16),tiles[keys[0]].getpixel((8,8)))
    sides = surface.copy()
    cells = {}
    for piece in p:
        face = piece["faces"].get("up",{})
        key = face.get("texture","")[1:]
        if key not in tiles or "rotation" in piece:
            continue
        x,y,z = piece["from"]
        X,Y,Z = piece["to"]
        for u in range(16):
            for v in range(16):
                if x <= u+.5 < X and z <= v+.5 < Z and Y >= cells.get((u,v),("",-1))[1]:
                    cells[u,v] = (key,Y)
    for (u,v),(key,Y) in cells.items():
        color = tiles[key].getpixel((u,v))
        palette = COLORS[EXTRA.get(key,key)].split()
        if cells.get((u-1,v)) != (key,Y) and cells.get((u,v-1)) != (key,Y):
            color = bytes.fromhex(palette[3])
        elif cells.get((u+1,v)) != (key,Y) and cells.get((u,v+1)) != (key,Y):
            color = bytes.fromhex(palette[1])
        surface.putpixel((u,v),tuple(color))
    # Hand-finished marks land on the food itself. The glaze, liner and exposed plate keep their own UVs.
    marks=ImageDraw.Draw(surface)
    if name=="ember_onion_pasty":
        for x in (6,9):
            marks.line((x-1,8,x,7),fill="#ab775c")
            marks.point((x,8),fill="#eab563")
    elif name=="golden_leek_tart":
        for x,z in ((5,6),(8,7),(6,9)):
            marks.rectangle((x,z,x+1,z+1),fill="#698747")
            marks.point((x,z),fill="#aadb74")
            marks.point((x+1,z+1),fill="#f3b679")
    elif name=="emberpepper_hotpot":
        for pts in (((4,5),(6,4),(7,4)),((8,11),(10,11),(11,10)),((6,8),(7,9))):
            marks.line(pts,fill="#f19645")
    elif name=="glowroot_steep":
        marks.line([(6,8),(7,9),(9,9)],fill="#e0af79")
        marks.line([(8,6),(10,7)],fill="#c68a47")
    bands = {}
    for i,key in enumerate(keys):
        first,last = (16*i)//len(keys),(16*(i+1))//len(keys)
        bands[key]=(first,last)
        sides.paste(tiles[key].crop((0,3,16,3+last-first)) if last-first<=13 else tiles[key],(0,first))
    save_image(surface,"block/guild",top)
    save_image(sides,"block/guild",side)
    for piece in p:
        x,y,z=piece["from"]
        X,Y,Z=piece["to"]
        for face,data in piece["faces"].items():
            key=data["texture"][1:]
            if key not in tiles:
                continue
            if face=="up" and "rotation" not in piece:
                badge(piece,face,top,(x,z,X,Z))
            elif face in ("north","south","east","west"):
                first,last=bands[key]
                u,U=(x,X) if face in ("north","south") else (z,Z)
                badge(piece,face,side,(u,first,U,first+min(Y-y,last-first)))
    # The block parent frames a full cube. Bring the food up to the centre of its inventory slot.
    gui={"rotation":[34,205,0],"translation":[0,1 if name=="caveberry_shrub" else 3.2,0],
         "scale":[1.04,1.04,1.04]}
    mesh("block/guild_kitchen/"+name,p,particle,{"gui":gui})


def pasty():
    p=plate()
    # Folded turnover: one continuous vaulted crust, a hand-pinched front lip and a cut slice.
    p.extend([b(4.1,1.5,5.7,10.1,3.15,10.25,"crust",east="onion"),
              b(4.75,3.15,6.1,10.1,4.35,9.7,"crust",east="onion"),
              b(5.4,4.35,6.65,9.9,5.03,9.15,"crust"),b(3.1,1.5,6.5,4.1,2.7,9.4,"crust"),
              b(3.6,2.7,6.85,4.75,3.5,9.1,"crust")])
    for x,z in ((3.5,6.4),(4.6,5.65),(6,5.38),(7.5,5.25),(9,5.35)):
        p.append(turn(b(x,2.35,z,x+1.4,3.05,z+.9,"crust"),"y",22.5,(x+.7,2.6,z+.45)))
    pivot=(11,1.5,4.95)
    slice_body=b(9.35,1.5,4.2,12.65,3.62,5.7,"crust")
    for side in ("north","south"):
        badge(slice_body,side,"filling")
    for piece in (slice_body,b(9.95,3.62,4.35,12.25,4.2,5.6,"crust")):
        p.append(turn(piece,"y",-22.5,pivot))
    p.append(b(8.1,1.5,10.9,9.1,1.85,11.65,"crumb"))
    served("ember_onion_pasty",p,"crust")


def skewers():
    p=plate("#woodplate","#woodside")
    for z,angle,offset in ((5.85,22.5,0),(9.65,-22.5,.7)):
        pivot=(8,2.5,z)
        def piece(q):
            p.append(turn(q,"y",angle,pivot))
        piece(b(2,2.5,z-.28,14,3.05,z+.28,"oak"))
        for n in range(3):
            x=3.6+offset+2.65*n
            length=1.2+(n%2)*.32
            if n==1:
                piece(b(x+.35,1.5,z-.5,x+1.5,2.95,z+.5,"mushroom"))
                piece(b(x-.35,2.95,z-1.18,x+2.2,3.65,z+1.18,"mushroom",down="gills"))
                piece(b(x+.05,3.65,z-.83,x+1.8,4.3,z+.83,"mushroom"))
            else:
                piece(b(x,2,z-.55,x+1,3.35,z+.55,"onion"))
                piece(b(x+.95,1.55,z-1.25,x+1.6,4,z+1.25,"mushroom",west="gills"))
                piece(b(x+1.6,1.86,z-.87,x+1.6+length*.55,3.67,z+.87,"mushroom"))
    p.append(turn(b(7.9,1.5,8,10.2,1.92,8.65,"leaf"),"y",-22.5,(9,1.5,8.3)))
    p.extend([b(5.5,1.5,7.55,7.3,1.93,8.7,"leaf"),b(6.8,1.5,8.4,8.1,2,9.3,"onion")])
    served("saltcap_skewers",p,"mushroom")


def tart():
    p=plate()
    # The missing near-right quarter exposes the filling; the slice sits across the gap.
    for x,z,X,Z in ((4.9,3.9,10,5.25),(3.95,5.25,11.7,8.15),(4,8.15,8,10.55),(5,10.55,8,11.7)):
        p.extend([b(x,1.5,z,X,2.65,Z,"crust"),b(x+.05,2.65,z+.05,X-.05,3.35,Z-.05,"custard")])
    for x,z,X,Z in ((5,3.9,10,4.5),(3.9,5.1,4.65,9.8),(4.55,9.8,5.3,11.25),(5.2,11,8,11.7),
                     (10,4.65,11,5.4),(11.1,5.4,11.75,8.1)):
        p.append(b(x,2.65,z,X,3.8,Z,"crust"))
    pivot=(10.15,1.5,10.25)
    for q in (b(8.8,1.5,9.05,11.45,2.6,11.55,"crust"),b(8.8,2.6,9.05,11.45,3.36,11.55,"custard"),
              b(10.8,3.36,9.4,11.45,3.73,11.4,"crust")):p.append(turn(q,"y",22.5,pivot))
    for x,z in ((5.5,5.5),(8,7),(5.8,9.1)):
        p.append(b(x,3.35,z,x+1.15,3.68,z+.8,"leaf"))
    served("golden_leek_tart",p,"crust")


def hotpot():
    p,fill=bowl(2,14,6,5.4,side="#porcelain",contents="pepper")
    for part in fill:p.append(b(*part["lo"],*part["hi"],"pepper"))
    p.extend([b(.7,3.4,6.4,2,4.3,9.6,"copper"),b(14,3.4,6.4,15.3,4.3,9.6,"copper")])
    for x,z,mat in ((4.3,6.4,"mushroom"),(6.1,8,"pepper"),(9.6,8.8,"mushroom"),(5.6,10,"leaf"),(8.6,5,"rootcut"),(4.1,8.25,"onion")):
        p+=course(x,5.4,z,x+1.9,6.02,z+1.6,.4,mat)
    pivot=(9.4,5.6,8)
    p.extend([turn(b(9.2,5.46,7.7,13.7,5.99,8.4,"oak"),"z",22.5,pivot),
              b(8.6,5.4,7.2,9.65,5.75,8.9,"oak")])
    served("emberpepper_hotpot",p,"pepper")


def drinks():
    p=[]
    for x,y,z,X,Y,Z,step,mat in [(4.3,0,4.3,11.7,.9,11.7,1.4,"berry"),(4,.9,4,12,5.7,12,1.4,"berry"),
            (4.6,5.7,4.6,11.4,7,11.4,1.2,"berry"),(5.6,7,5.6,10.4,8.1,10.4,.9,"berry"),
            (6.7,8.1,6.7,9.3,10.5,9.3,.5,"teal"),(6.2,10.5,6.2,9.8,11.1,9.8,.65,"teal"),
            (6.65,11.1,6.65,9.35,12.2,9.35,.5,"oak")]:p+=course(x,y,z,X,Y,Z,step,mat)
    p.extend([badge(b(5.8,1.7,3.73,10.2,5.3,4,"linen"),"north","label"),
              badge(b(5.8,1.7,12,10.2,5.3,12.27,"linen"),"south","label"),
              b(6.6,9.2,6.6,9.4,9.8,9.4,"willow"),b(6.2,8.3,6.15,7.15,9.5,6.7,"berry")])
    served("caveberry_shrub",p,"berry")
    # The same blue-rimmed glaze as the plates, on a smaller saucer. Retain the rim and lining UVs.
    p=plate()
    for part in p:
        for corner in ("from","to"):
            for axis in (0,2):
                part[corner][axis]=8+(part[corner][axis]-8)*.88
    for y,Y,x,X,mat in ((1.5,2.1,5.6,10.4,"teal"),(2.1,2.85,4.9,11.1,"porcelain"),(2.85,4.4,4.4,11.6,"porcelain")):
        p+=course(x,y,x,X,Y,X,1,mat)
    # Pooled amber sits below the lip, bounded by four side rails and the chamfer corners.
    p+=course(4.5,4.4,4.5,11.5,5.18,11.5,1,"amber")
    for x,z,X,Z in ((5.45,4.35,10.55,4.9),(5.45,11.1,10.55,11.65),(4.35,5.45,4.9,10.55),(11.1,5.45,11.65,10.55),
                     (4.9,4.9,5.45,5.45),(10.55,4.9,11.1,5.45),(4.9,10.55,5.45,11.1),(10.55,10.55,11.1,11.1)):
        p.append(b(x,4.4,z,X,5.65,Z,"teal"))
    p.extend([b(11.55,3,7.1,13.3,3.7,8.9,"teal"),b(13.3,3,7.1,14,5.35,8.9,"teal"),
              b(11.5,4.65,7.1,13.3,5.35,8.9,"teal"),b(6.8,5.18,6.75,8.4,5.5,7.7,"leaf")])
    p+=course(4,1.5,10.85,6.2,2.1,12.65,.38,"rootcut")
    served("glowroot_steep",p,"teal")

