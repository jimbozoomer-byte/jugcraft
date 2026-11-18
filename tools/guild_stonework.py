"""Masonry, the Stonewright's display furniture and four working survey instruments."""
from guild_materials import badge, box as b, mesh, octagon, turn


def block(name, parts, material):
    mesh("block/"+name,parts,material)


def masonry():
    block("guild_border_paver",[b(0,0,0,16,16,16,"course",up="paver",down="basalt")],"basalt")
    # Cut the face of the archstone forward in successively narrower beds.
    key=[b(0,0,2,16,16,16,"limestone"),b(0,0,.9,3,16,2,"basalt"),b(13,0,.9,16,16,2,"basalt")]
    for y,Y,x,X in ((0,4,5,11),(4,9,4,12),(9,14,3,13),(14,16,2,14)):
        key.append(b(x,y,.4,X,Y,2,"limestone"))
    key.extend([b(4.3,13.7,.08,11.7,14.4,.4,"brass"),
                badge(b(5.2,4.8,.08,10.8,13.1,.4,"limestone"),"north","archmark")])
    block("archmasters_keystone",key,"limestone")
    corbel=[b(4,1,12,12,14,15,"basalt"),b(2,14,1,14,16,15,"limestone"),
            b(2.6,13,1.6,13.4,14,14.4,"basalt"),b(3,12,2,13,13,14,"limestone")]
    for y,Y,x,z in ((2,4,5.5,10),(4,6,5,8),(6,8,4.6,6),(8,11,4,3),(11,12,4,2.5)):
        corbel.append(b(x,y,z,16-x,Y,13,"limestone"))
    for x,side in ((3.4,"west"),(12,"east")):
        cheek=b(x,7,3,x+.6,12,10,"limestone")
        corbel.append(badge(cheek,side,"scroll"))
    lip=b(3.8,9.2,2.7,12.2,11.75,3,"limestone")
    corbel.append(badge(lip,"north","scroll",(1,2,13,8)))
    for x in (4.1,10.7):
        corbel.append(b(x,11.75,2.1,x+1.2,12,3.8,"brass"))
    block("scrollwork_corbel",corbel,"limestone")
    benchmark=octagon(1,0,1,15,1.5,15,2,"basalt")+octagon(2,1.5,2,14,2.4,14,2,"limestone")
    benchmark+=octagon(3,2.4,3,13,3.3,13,2,"brass")
    face=b(4.25,3.3,4.25,11.75,3.6,11.75,"copper")
    benchmark+=[badge(face,"up","compass"),b(7.5,3.6,7.5,8.5,4.25,8.5,"iron")]
    for x,z in ((3,3),(12,3),(3,12),(12,12)):
        benchmark.append(b(x,2.4,z,x+1,2.8,z+1,"iron"))
    block("survey_benchmark",benchmark,"basalt")


def cabinets():
    for state in range(3):
        p=[b(.5,0,3,15.5,1.3,13,"oak"),b(1,1.3,4,15,2.5,12.5,"basalt"),b(1,2.5,11,15,13,12.5,"teal"),
           b(.5,13,3,15.5,14.3,13,"oak"),b(1,14.3,3.6,15,15,12.4,"brass"),
           b(2.6,2.5,4,13.4,3.2,11,"linen"),b(5,1.4,3.72,11,2.4,4,"brass")]
        badge(p[-1],"north",("sample_i","sample_ii","sample_iii")[state])
        for x in (1,13.5):
            p.extend([b(x,2.5,4,x+1.5,4,11,"oak"),b(x+.2,4,4.2,x+1.3,12.1,5.6,"oak"),
                      b(x,12.1,4,x+1.5,13,10.8,"brass")])
        if state==0:
            p+=octagon(4,3.2,5,12,4.4,10.5,1,"basalt")
            for x,z,h,ang in ((4.7,7,7.9,22.5),(7,7.8,11.4,0),(9.5,6.8,8.8,-22.5)):
                pivot=(x+1,4.3,z+1)
                for q in (b(x,4.3,z,x+1.9,h,z+1.8,"plum"),b(x+.4,h,z+.3,x+1.5,h+.9,z+1.45,"plum")):
                    p.append(turn(q,"z",ang,pivot) if ang else q)
        elif state==1:
            p+=octagon(3.5,3.2,5,12.5,5.2,10.4,1.5,"basalt")
            p+=octagon(4,5.2,5.7,10.7,7.3,10,1,"native_copper")
            p.extend([b(4.7,7.3,6.7,8.4,8.6,9,"native_copper"),b(10.3,5.2,6.8,12,6.5,9.1,"teal")])
        else:
            p+=octagon(4.3,3.2,6.2,11.7,4.8,10.5,1,"basalt")
            p.extend([b(4.5,4.8,7.9,11.5,8.8,10,"plum"),b(5.2,8.8,8,10.8,10,9.8,"basalt"),
                      b(4.5,4.8,6.4,5.8,8.8,7.9,"basalt"),b(10.2,4.8,6.4,11.5,8.8,7.9,"basalt"),
                      b(6,4.8,6.4,9.8,5.6,7.9,"sky"),b(6.6,5.6,7,7.5,7,7.8,"sky"),b(8.8,5.6,7,9.5,6.7,7.8,"sky")])
        block("quarry_sample_cabinet_"+str(state),p,"oak")


def cutting_bench():
    p=[b(.4,7,2,15.6,8.3,14,"oak"),b(0,8.3,1.7,16,9.6,14.3,"basalt"),
       b(2.6,2.3,7,13.4,3.6,9,"oak"),b(2.8,9.6,5.2,8.7,10.4,11.2,"iron")]
    for x in (1,12):
        p.extend([b(x,0,3,x+3,1.2,13,"oak"),b(x+.5,1.2,4,x+2.5,7,6.4,"oak"),b(x+.5,1.2,9.6,x+2.5,7,12,"oak")])
    # Profile of the wheel in the XY plane. An axle enters the bearing behind its centre.
    for y,Y,x,X in ((9.7,10.6,4,7.4),(10.6,11.7,3,8.4),(11.7,13.8,2.5,9),(13.8,14.9,3,8.4),(14.9,15.7,4.4,7)):
        band=b(x,y,6.5,X,Y,7.4,"iron")
        for face in ("north","south"):
            badge(band,face,"wheel",((x-2.5)*2.4,(15.7-Y)*2.5,(X-2.5)*2.4,(15.7-y)*2.5))
        p.append(band)
    for x,y in ((2.35,12.7),(3.3,14.6),(6.1,15.4),(8.5,13.8)):
        p.append(turn(b(x,y,6.55,x+.78,y+.65,7.3,"iron"),"z",22.5,(x+.4,y+.32,6.9)))
    p.extend([b(5.2,10.4,8.6,6.4,13.3,10,"oak"),b(5.15,12,6,6.55,13.35,8.6,"brass"),
              b(9.4,9.6,4.5,11,11.6,9.5,"iron"),b(11,9.6,5.1,13.2,10.9,8.8,"limestone"),
              b(13.2,9.6,4.6,14.4,11.5,9.4,"oak"),b(14.4,10.2,6.4,15.7,10.9,7.3,"iron"),
              b(15.5,9.2,6.1,16,12,7.6,"brass")])
    block("stonemasons_cutting_bench",p,"basalt")


def instruments():
    def instrument(name, parts, dominant, tilt, scale):
        hand={"rotation":[0,-90,25],"translation":[0,1,0],"scale":[.8]*3}
        display={"gui":{"rotation":[14,163,tilt],"translation":[0,0,0],"scale":[scale]*3},
                 "thirdperson_righthand":hand,"thirdperson_lefthand":hand,
                 "firstperson_righthand":{"rotation":[0,-65,13],"translation":[1,2,0],"scale":[.8]*3},
                 "firstperson_lefthand":{"rotation":[0,-65,13],"translation":[1,2,0],"scale":[.8]*3},
                 "fixed":{"rotation":[0,0,0],"scale":[.95]*3}}
        mesh("item/"+name,parts,dominant,display)
    hammer=[b(6.9,1,7.1,9.1,11.2,8.9,"oak"),b(6.6,.6,6.9,9.4,1.5,9.1,"brass"),
            b(6.65,2,6.9,9.35,5.8,9.1,"willow"),b(6.6,9.2,6.8,9.4,10.6,9.2,"brass"),
            b(4.4,10.6,6.3,10.8,13.9,9.7,"iron"),b(2.3,10.1,6,4.4,14.4,10,"iron"),
            b(10.8,11.4,6.8,12.7,13.9,9.2,"iron"),b(12.7,12.1,7.3,14.2,13.5,8.7,"iron"),
            b(14.2,12,7.6,15.2,12.9,8.4,"iron"),b(7,13.9,7.1,8.8,14.25,8.9,"oak")]
    for part in hammer[4:6]:
        for face in ("north","south","west"):
            badge(part,face,"forged")
    instrument("fault_sounding_hammer",hammer,"iron",-32,1.02)
    spirit=[b(1,6,6.3,15,7.2,9.7,"brass"),b(1,7.2,6.6,4.8,9.7,9.4,"oak"),
            b(11.2,7.2,6.6,15,9.7,9.4,"oak"),b(1,9.7,6.3,15,10.5,9.7,"brass")]
    vial=b(4.8,7.7,7.3,11.2,9.25,8.7,"teal")
    for side in ("north","south","up"):badge(vial,side,"vial")
    spirit.append(vial)
    for x in (1.6,13):spirit.append(b(x,7.5,6.3,x+1.3,8.9,6.6,"iron"))
    instrument("masons_spirit_level",spirit,"brass",23,1.05)
    rule=[]
    for x,y,z,X,Y,angle,pivot in [(2,3.6,7.6,13.3,5.4,0,(3,4.5,8)),(2,3.6,6.6,13.3,5.4,45,(3,4.5,7)),
                                   (2.5,10.5,5.6,11.1,12.3,0,(10.1,11.4,6))]:
        arm=b(x,y,z,X,Y,z+.98,"willow")
        for side in ("north","south"):badge(arm,side,"graduations")
        rule.append(turn(arm,"z",angle,pivot) if angle else arm)
    for x,y,z in ((3,4.5,6.3),(10,11.4,5.3)):
        rule.append(b(x-.55,y-.55,z,x+.55,y+.55,z+2.45,"brass"))
    instrument("folding_span_rule",rule,"willow",-12,1.04)
    meter=[]
    for y,Y,x,X in ((3,4,5,11),(4,5,3.8,12.2),(5,11,3,13),(11,12,3.8,12.2),(12,13,5,11)):
        meter.append(b(x,y,6.7,X,Y,9.6,"brass"))
    face=b(4.2,4.8,6.25,11.8,11.9,6.7,"iron")
    meter.extend([badge(face,"north","dial"),b(6.8,13,7.1,9.2,13.7,9.1,"iron"),
                  b(6.2,13.7,6.8,9.8,14.8,9.4,"teal"),b(7,14.8,7.2,9,15.3,9,"teal"),
                  b(6.3,1.6,7.2,7.1,3,8.6,"oak"),b(8.9,1.6,7.2,9.7,3,8.6,"oak"),b(6.3,.9,7.2,9.7,1.6,8.6,"oak")])
    instrument("lampwatch_photometer",meter,"brass",-12,1.0)

