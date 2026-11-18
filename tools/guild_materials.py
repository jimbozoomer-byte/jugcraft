"""Materials and closed model assembly for the two Deepstead guild shops."""
import json
from PIL import Image, ImageDraw
import model_writer
from deepstead_wares import ART, element, save_image


def box(x,y,z,X,Y,Z,mat, **faces):
    part = element([x,y,z],[X,Y,Z],mat)
    if mat=="oak":
        if Y-y>max(X-x,Z-z):
            faces={"north":"oak_upright","south":"oak_upright","west":"oak_upright","east":"oak_upright",
                   "up":"endgrain","down":"endgrain",**faces}
        elif X-x>max(Y-y,Z-z):
            faces={"west":"endgrain","east":"endgrain",**faces}
    for side, name in faces.items():
        part["faces"][side] = {"texture":"#"+name}
    return part


def octagon(x,y,z,X,Y,Z,step,mat, **faces):
    # An octagonal course with common world-space UVs across all three strips.
    return [box(x+step,y,z,X-step,Y,z+step,mat,**faces),
            box(x,y,z+step,X,Y,Z-step,mat,**faces),
            box(x+step,y,Z-step,X-step,Y,Z,mat,**faces)]


def turn(part, axis, degrees, pivot):
    part["rotation"] = {"axis":axis,"angle":degrees,"origin":list(pivot)}
    return part


def badge(part, side, key, uv=(0,0,16,16)):
    part["faces"][side]={"texture":"#"+key,"uv":list(uv)}
    return part


def write(path, obj):
    if path.exists() and json.loads(path.read_text(encoding="utf-8")) == obj:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+"\n",encoding="utf-8")


SHARED = dict(brass="deepstead_hammered_brass",sky="deepstead_geode_lit",plate="meal_glazed_plate_top",
              plate_side="meal_glazed_side",woodplate="meal_wood_plate_top",woodside="meal_wood_side")


def mesh(name, parts, particle, display=None):
    keys = {f["texture"][1:] for p in parts for f in p["faces"].values()} | {particle}
    refs = {k:"jugcraft:block/"+SHARED.get(k,"guild/"+k) for k in sorted(keys)}
    model_writer.finish_closed(parts)
    result = {"parent":"minecraft:block/block","textures":{"particle":refs[particle],**refs},"elements":parts}
    if display:
        result["display"] = display
    write(ART/"models"/(name+".json"),result)


# Shadow, shade, ground, light and glint. Each surface is painted at 16 px.
COLORS={
    "basalt":"33313b 42434b 55565b 696d70 87908e", "limestone":"675b57 88796b ad9e88 cbbba2 ddd0b7",
    "oak":"39292d 503530 765043 9b704e b3895d", "iron":"33333f 46515c 697680 939c9c bfc7bb",
    "copper":"573c42 86513e b77048 cf935b e8b577", "teal":"203d41 305758 447877 70a29a a6c8b3",
    "linen":"78514b a06c54 c0a477 d3bd92 e4d0a2", "willow":"593c39 79523a a17244 c39358 ddba78",
    # Baking palettes from pie_crust.png and cornbread.png. Onion flesh and mushroom gills
    # from onion.png and brown_mushroom_colony_stage2.png in the owner's farming collection.
    "crust":"77561f ab775c d88b50 d59f4b eab563", "crumb":"824a00 ad6918 d49c00 e1aa2b ffca38",
    "onion":"6d4a20 8b6941 ac732f dea766 e9c396", "mushroom":"65554a 725643 916d55 bb8967 ccae9b",
    # tomato, cabbage_leaf, glow_berry_custard, sweet_berry_cookie and apple_cider.
    "pepper":"63203a 941e33 c0341f cf4d3b ff8073", "leaf":"3c713e 4d8c3b 7ab244 aadb74 c7ff87",
    "custard":"92441a d57219 f3b679 fadfad f3da99", "berry":"352939 45283c 691f39 99394f c16b78",
    "amber":"92441a 9c4414 b86633 c68a47 e0af79", "plum":"42354e 604b70 846691 af91b0 d6bdcf",
    "ember":"41343c 773e3d a94e34 cf753b e3ab5c",
}
EXTRA=dict(rootcut="amber",native_copper="copper",paver="basalt",course="basalt",scroll="limestone",
           compass="limestone",dial="limestone",gong="copper",graduations="linen",label="linen",vial="leaf",
           archmark="limestone",wheel="iron",forged="iron",porcelain="teal",gills="mushroom",braid="crust",
           filling="onion",sample_i="linen",sample_ii="linen",sample_iii="linen",endgrain="oak",oak_upright="oak")


def tile(key,palette):
    c=["#"+v for v in COLORS[palette].split()]
    im=Image.new("RGB",(16,16),c[2])
    d=ImageDraw.Draw(im)
    def line(pts,i,w=1):
        d.line(pts,fill=c[i],width=w)
    def rect(bounds,i):
        d.rectangle(bounds,fill=c[i])
    def stroke(pts):
        line([(x,y+1) for x,y in pts],1)
        line(pts,3)
    if key.startswith("sample_"):
        count=len(key.split("_")[1])
        for column in range(count):
            x=7-(count-1)*2+column*4
            line([(x,4),(x,11)],0,2)
        line([(2,2),(13,2)],3);line([(2,13),(13,13)],1)
    elif key=="filling":
        d.rectangle((0,0,15,15),fill="#bb8967")
        d.polygon([(2,4),(5,2),(12,2),(14,5),(14,11),(11,13),(3,13),(1,10)],fill="#6d4a20")
        for x,y in ((3,6),(8,5),(6,10),(11,9)):
            d.arc((x-1,y-2,x+2,y+1),20,300,fill="#e9c396")
            d.point((x+1,y),fill="#ac732f")
    elif key=="archmark":
        for pts,k in [([(1,1),(14,1),(10,13),(5,13),(1,1)],1), ([(2,2),(13,2),(9,12),(6,12)],4),
                      ([(8,3),(11,6),(8,10),(5,6),(8,3)],0)]:
            line(pts,k)
        d.polygon([(8,4),(10,6),(8,9),(6,6)],fill="#a86748")
        d.line([(8,4),(6,6),(8,7)],fill="#deaf72")
    elif key=="wheel":
        d.ellipse((0,0,15,15), fill=c[1],outline=c[4])
        for i in (2,4):
            d.arc((i,i,15-i,15-i),180,315,fill=c[3])
            d.arc((i,i,15-i,15-i),5,140,fill=c[0])
        for x,y in ((3,2),(11,2),(14,6),(14,10),(10,14),(5,14),(1,9),(1,5)):rect((x,y,x+1,y),0)
        d.ellipse((6,6,9,9),fill="#bd9966")
    elif key=="forged":
        d.polygon([(0,0),(15,0),(15,4),(10,6),(1,4)],fill=c[3])
        d.polygon([(0,12),(8,10),(15,11),(15,15),(0,15)],fill=c[1])
        line([(1,2),(7,2),(11,3)],4);line([(4,8),(6,7),(8,7)],1);line([(11,9),(13,8)],3)
    elif key=="porcelain":
        d.rectangle((0,0,15,15),fill="#476366")
        for y,i in ((0,4),(1,1),(2,3),(6,1),(9,3),(11,0),(14,1)):
            line([(0,y),(15,y)],i)
        for x in (0,6,12):
            d.line([(x,3),(x+2,5),(x+4,3)],fill="#caa476")
    elif key=="gills":
        for x in (0,3,6,9,12,15):
            line([(7,10),(x,3)],1);line([(8,11),(x+1,15)],3)
        line([(0,2),(15,2)],4)
    elif key=="braid":
        for x,y in ((0,8),(6,3),(10,12)):
            line([(x-2,y+1),(x+2,y-2),(x+4,y-2)],1,2)
            line([(x-2,y),(x+2,y-3),(x+4,y-3)],4)
    elif key in ("oak","oak_upright"):
        for pts in ([(-2,1),(4,1),(6,2),(11,2),(13,1),(18,1)],
                    [(-2,5),(2,5),(4,4),(8,4)],[(9,5),(12,4),(17,4)],
                    [(-1,10),(3,10),(5,8),(11,8),(14,10),(17,10)],
                    [(-1,13),(4,13),(7,12),(11,12)],[(11,14),(13,13),(16,13)]):
            stroke(pts)
        line([(4,7),(7,6),(10,6),(12,7),(10,8),(7,8),(4,7)],1)
        line([(6,7),(9,7)],0)
        if key=="oak_upright":
            im=im.transpose(Image.Transpose.ROTATE_90)
    elif key=="endgrain":
        for i in (1,4,7):
            line([(i,15),(i,6+i//2),(i+2,3+i//2),(7,i),(14-i//2,i)],1)
            line([(i+1,15),(i+1,7+i//2),(i+3,4+i//2)],3)
    elif key in ("basalt","limestone"):
        for pts,k in (([(0,0),(5,0),(3,2),(0,3)],3), ([(8,2),(13,2),(15,4),(12,5),(7,4)],1),
                      ([(2,6),(5,5),(8,6),(6,7),(1,8)],3), ([(0,12),(3,10),(7,10),(5,12)],1),
                      ([(9,11),(13,9),(15,10),(13,12),(10,13)],3), ([(3,15),(6,13),(9,13),(7,15)],3)):
            d.polygon(pts,fill=c[k])
        for pts in ([(0,4),(3,3),(5,3)],[(8,8),(10,7),(12,7)],[(11,15),(14,14),(15,14)]):
            stroke(pts)
    elif key in ("copper","iron","teal","berry","amber"):
        for x in (0,8):
            d.polygon([(x,0),(x+3,0),(x+2,6),(x,8)],fill=c[3])
            d.polygon([(x+3,7),(x+6,5),(x+6,13),(x+3,15)],fill=c[1])
        for x,y in ((1,-1),(7,4),(13,10),(3,12)):
            line([(x,y),(x,y+3),(x-1,y+5)],3);line([(x+2,y+1),(x+2,y+5)],1)
        rect((1,1,2,2),4)
    elif key=="willow":
        for y in range(0,16,4):
            for x in range(-4,17,8):
                u=x+(4 if y%8 else 0)
                rect((u,y,u+5,y+2),1);line([(u+1,y),(u+4,y)],3);line([(u+6,y),(u+6,y+3)],4)
    elif key=="linen":
        for i in (2,3,11):
            line([(i,0),(i,15)],1);line([(0,i),(15,i)],1)
        for x in (2,3,11):
            for y in (2,3,11):rect((x,y,x,y),0)
    elif key in ("crust","crumb","mushroom"):
        for x,y,n in ((1,2,4),(8,0,3),(10,7,4),(4,8,2),(0,13,3),(8,14,3)):
            line([(x,y+1),(x+1,y),(x+n,y)],3);rect((x+1,y+2,x+2,y+2),1)
        if key=="crust":
            for x,y in ((2,4),(8,10)):
                line([(x,y+3),(x+3,y),(x+4,y)],1);line([(x+1,y+3),(x+4,y)],4)
    elif key in ("onion","custard","pepper","leaf","rootcut"):
        for x,y in ((2,4),(9,8),(1,13),(13,1)):
            d.arc((x-2,y-2,x+3,y+2),60,340,fill=c[1]);d.arc((x-1,y-1,x+2,y+1),100,300,fill=c[4])
        if key=="custard":
            for x,y in ((5,5),(10,7),(6,10),(10,11),(3,8),(12,4)):
                d.ellipse((x-1,y-1,x+1,y+1),outline="#758648");d.point((x,y),fill="#c9ca77")
    elif key in ("plum","native_copper","ember"):
        for pts,i in [([(0,0),(7,1),(4,6),(0,8)],3), ([(9,3),(15,2),(13,10),(8,11)],1),
                      ([(3,9),(8,6),(6,14),(1,15)],3)]:
            d.polygon(pts,fill=c[i]);line(pts[:2],4)
        if key=="native_copper":
            for x,y in ((1,2),(9,6),(5,12)):d.polygon([(x,y),(x+3,y-1),(x+4,y+2),(x+1,y+3)],fill="#789181")
    elif key=="paver":
        for y in (2,12):
            line([(0,y+1),(15,y+1)],0);d.line((0,y,15,y),fill="#b89a65")
        for x in (-8,0,8,16):
            for dy,color in ((1,"#403839"),(0,"#b68d55")):
                d.line([(x-4,8+dy),(x,5+dy),(x+4,8+dy),(x,11+dy)],fill=color)
            d.line([(x+1,6),(x+3,8)],fill="#d0b784")
    elif key=="course":
        for y,split in ((2,5),(10,12)):
            line([(0,y),(15,y)],0);line([(0,y+1),(15,y+1)],3);line([(split,y+1),(split,y+7)],1)
        for x,y in ((1,6),(8,5),(6,14),(13,1)):
            line([(x,y),(x+2,y),(x+3,y-1)],3)
    elif key=="scroll":
        line([(2,13),(6,12),(10,10),(13,7),(13,4),(11,2),(7,2),(5,4),(5,7),(7,9),(10,8),(10,5),(8,5)],0,2)
        line([(2,12),(6,11),(10,9),(12,7),(12,4),(10,3),(7,3),(6,5)],4)
    elif key in ("compass","dial","gong"):
        ground,ink={"compass":("#726041","#dbbd7d"),"dial":("#d9c89f","#443e43"),"gong":("#ad7745","#e0b97b")}[key]
        d.rectangle((0,0,15,15),fill=ground)
        d.ellipse((1,1,14,14),outline=c[0]);d.ellipse((2,2,13,13),outline=ink)
        if key=="gong":
            for i in (3,5):
                d.arc((i,i,15-i,15-i),172,302,fill=c[4]);d.arc((i,i,15-i,15-i),8,136,fill=c[1])
        elif key=="compass":
            d.line((2,8,13,8),fill="#bbaa84");d.line((8,2,8,13),fill="#bbaa84")
            d.polygon([(8,2),(10,8),(8,7),(6,8)],fill="#e3c786")
            d.polygon([(8,13),(6,8),(8,9),(10,8)],fill="#4a4341")
        else:
            for pts in [((4,4),(5,5)),((11,4),(10,5)),((3,7),(4,7)),((11,7),(12,7)),((7,3),(7,4))]:
                d.line(pts,fill=ink)
            d.line([(5,11),(8,8),(10,4)],fill="#9e4d37",width=2);d.point((8,8),fill=c[0])
    elif key=="graduations":
        for x in range(0,16,2):line([(x,1),(x,5 if x%4 else 9)],0)
        line([(0,14),(15,14)],3)
    elif key=="label":
        d.line((0,1,15,1),fill="#8b694d");d.line((0,14,15,14),fill="#8b694d")
        d.line([(4,6),(8,5),(11,2)],fill="#557749",width=2)
        for x,y in ((4,8),(8,7),(7,11)):
            d.ellipse((x-1,y-1,x+2,y+2),fill="#884561");d.point((x,y-1),fill="#bd7c82")
    elif key=="vial":
        rect((0,0,15,2),1);rect((0,12,15,15),1);rect((5,4,10,9),4)
        for x in (3,12):line([(x,0),(x,15)],0)
    else:
        raise ValueError(key)
    save_image(im,"block/guild",key)


def paint():
    for name in COLORS:
        tile(name,name)
    for name,palette in EXTRA.items():
        tile(name,palette)

