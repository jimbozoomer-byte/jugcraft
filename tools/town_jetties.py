"""Author the two timber-jettied crowns above the existing Old Town lift landings."""
AIR = "minecraft:air"
TIMBER = "stripped_dark_oak_log"
PLASTER = "pale_oak_planks"


def build_jetty(name, roof, lift, baseline):
    """Local coordinates, including the baseline roof that the live survey will move."""
    x0, deck, z0, x1, ridge, z1 = roof
    gate = name == "west_gate_house"
    rise = 11 if gate else 16
    desired = {}

    def put(x, y, z, block):
        desired[x, y, z] = "minecraft:" + block

    def span(x, y, z, X, Z, block):
        for i in range(x, X + 1):
            for j in range(z, Z + 1):
                put(i, y, j, block)

    # The centre of the cap is the actual old roof, including chimney and roof openings.
    # Each ascending room projects on a different axis. The five- and six-block pitches
    # leave four clear wall courses below, and five below the top ceiling.
    rooms = [(x0, z0, x1, z1), (x0-1, z0, x1+1, z1)] if gate else [
        (x0, z0, x1, z1), (x0, z0, x1+1, z1), (x0, z0-1, x1+1, z1+1)]
    for y in range(deck, ridge + rise + 1):
        for x in range(x0, x1+1):
            for z in range(z0, z1+1):
                desired[x,y,z] = baseline(x,y-rise,z) if y >= deck+rise else AIR

    def pane(color, along_x):
        axis = "east=true,west=true" if along_x else "north=true,south=true"
        return color+"glass_pane["+axis+"]"

    last = (x0+1,z0+1,x1-1,z1-1)
    for floor, (w,n,e,s) in enumerate(rooms):
        y = deck+floor*5
        height = 6 if floor==len(rooms)-1 else 5
        span(w,y,n,e,s,"spruce_planks")
        for yy in range(y+1,y+height):
            span(w+1,yy,n+1,e-1,s-1,"air")

        # Align each projecting bay with a pair of joist ends. Dark corner posts and a continuous
        # bressummer carry the lighter infill; the copper drip edges only run beneath the windows.
        for along_x, edge, out in ((True,n,-1),(True,s,1),(False,w,-1),(False,e,1)):
            lo,hi = (w,e) if along_x else (n,s)
            projecting = edge not in ((last[1],last[3]) if along_x else (last[0],last[2]))
            middle = (lo+hi)//2
            posts = {lo,lo+3,hi-3,hi}
            if hi-lo >= 12:
                posts.add(middle)
            direction = ("north" if out<0 else "south") if along_x else ("west" if out<0 else "east")
            inward = {"north":"south","south":"north","west":"east","east":"west"}[direction]
            def wall(k, dy, b, offset=0):
                x,z = (k,edge+out*offset) if along_x else (edge+out*offset,k)
                put(x,y+dy,z,b)
            for k in range(lo,hi+1):
                axis="x" if along_x else "z"
                wall(k,0,TIMBER+"[axis="+axis+"]")
                for dy in range(1,height):
                    if k in posts:
                        mat=TIMBER
                    elif dy==height-1:
                        mat=TIMBER+"[axis="+axis+"]"
                    elif dy==1:
                        mat=PLASTER if gate or floor==0 else "white_terracotta"
                    elif lo<k<lo+3 and direction in ("west","south"):
                        # Herringbone braces in the solid end panels read from across the square.
                        mat=TIMBER if k-lo==dy-1 else "white_terracotta"
                    elif dy==height-2 and (k==lo+1 or k==hi-1):
                        mat="chiseled_tuff_bricks" if gate else "bricks"
                    else:
                        color=("light_gray_stained_" if gate else "yellow_stained_") if dy==height-2 else ""
                        mat=pane(color,along_x)
                    wall(k,dy,mat)
                if k in posts and lo<k<hi:
                    wall(k,0,"dark_oak_log[axis="+("z" if along_x else "x")+"]",1)
                    if projecting:
                        wall(k,-1,"dark_oak_stairs[facing="+inward+",half=top]")
                elif k==lo+1 or k==hi-1:
                    wall(k,1,"waxed_weathered_cut_copper_slab[type=top]" if gate else "spruce_slab[type=top]",1)
            if projecting:
                for k in [lo+1,hi-1]:
                    wall(k,-1,"spruce_stairs[facing="+inward+",half=top]")
            if direction==("east" if gate else "north"):
                for k in range(lo+1,lo+3):
                    wall(k,height-1,("waxed_weathered_cut_copper_stairs" if gate else "dark_oak_stairs")
                         +"[facing="+inward+"]",1)
                    wall(k,1,"waxed_cut_copper_slab[type=top]" if gate else "spruce_slab[type=top]",1)
                for k in (lo,lo+3):
                    wall(k,2,"dark_oak_trapdoor[half=bottom,open=true,facing="+direction+"]",1)
        # Extend the original flue straight through all floors, even where it now sits within a room.
        for yy in range(y,y+height):
            if gate:
                put(x0+1,yy,z1-2,"bricks")
            else:
                put(x0+2,yy,z0+1,"cobblestone")
        # Furniture stays along the edges. Each hall has an open area around the landing.
        if gate:
            span(w+2,y+1,n+1,w+3,n+1,"bookshelf")
            put(w+2,y+2,n+1,"lantern")
            for x in (e-4,e-3):
                put(x,y+1,n+2,"spruce_fence")
                put(x,y+2,n+2,"dark_oak_pressure_plate")
            put(e-3,y+1,n+4,"spruce_stairs[facing=south]")
            if floor:
                put(w+1,y+1,s-2,"cartography_table")
                put(w+1,y+2,s-2,"lantern")
                put(w+2,y+1,s-2,"lectern[facing=east]")
                put(w+2,y+1,s-4,"spruce_stairs[facing=north]")
        else:
            for x in (w+2,w+3,e-3,e-2):
                put(x,y+1,n+3,"dark_oak_stairs[facing=north]")
            for x in range(w+2,e-1):
                put(x,y+1,n+2,"spruce_fence" if x in (w+2,e-2) else "air")
                put(x,y+2,n+2,"spruce_trapdoor[half=bottom]")
            if floor==2:
                span(e-1,y+1,s-4,e-1,s-2,"bookshelf")
                put(e-1,y+2,s-3,"lantern")
                for x in (e-5,e-4):
                    put(x,y+1,s-4,"red_bed[facing=south,part=foot]")
                    put(x,y+1,s-3,"red_bed[facing=south,part=head]")
                for x in (e-6,e-3):
                    for z in (s-5,s-2):
                        for yy in range(y+1,y+4):
                            put(x,yy,z,"dark_oak_fence")
                    span(x,y+4,s-5,x,s-2,"dark_oak_slab[type=top]")
                span(e-5,y+4,s-5,e-4,s-5,"dark_oak_slab[type=top]")
                span(e-5,y+4,s-3,e-4,s-2,"red_wool")
        rug="cyan_carpet" if gate else "red_carpet"
        cx,cz=(w+e)//2,(n+s)//2
        span(cx-1,y+1,cz,cx+1,cz+1,rug)
        for x in (cx-2,cx+2):
            for z in range(n+1,s):
                if (x,z)!=lift:
                    put(x,y+height-1,z,TIMBER+"[axis=z]")
            put(x,y+height-1,cz,"iron_chain[axis=y]")
            put(x,y+height-2,cz,"lantern[hanging=true]")
        desired[lift[0],y,lift[1]]="jugcraft:counterweight_lift"
        last=(w,n,e,s)

    # A weather skirt bridges the wider walls into the preserved central gable. Its pitched
    # outer strip sits a half block below the old eaves and drains into the street.
    w,n,e,s=last
    cap=deck+rise
    for x in range(w-1,e+2):
        for z in range(n-1,s+2):
            if x0+1<=x<=x1-1 and z0+1<=z<=z1-1:
                continue
            outer=x in (w-1,e+1) or z in (n-1,s+1)
            if outer:
                direction="east" if x==w-1 else "west" if x==e+1 else "south" if z==n-1 else "north"
                put(x,cap,z,("waxed_weathered_cut_copper_stairs" if gate else "spruce_stairs")+"[facing="+direction+"]")
            else:
                put(x,cap,z,"dark_oak_planks")
                if not (x0<=x<=x1 and z0<=z<=z1):
                    put(x,cap+1,z,"waxed_weathered_cut_copper_slab" if gate else "spruce_slab")
    # Street landmarks: a braced gatehouse hoist with a suspended weight, and an inn's lantern sign.
    if gate:
        cx,cz=e,(n+s)//2
        for x in range(cx,cx+4):
            put(x,cap-1,cz,"stripped_spruce_log[axis=x]")
        put(cx+1,cap-2,cz,"spruce_stairs[facing=west,half=top]")
        put(cx+3,cap-1,cz,"waxed_cut_copper")
        for yy in range(cap-4,cap-1):
            put(cx+3,yy,cz,"iron_chain[axis=y]")
        put(cx+2,cap-2,cz,"grindstone[face=ceiling,facing=north]")
        put(cx+3,cap-5,cz,"polished_tuff")
    else:
        cx,cz=(w+e)//2,n
        for zz in range(cz-3,cz+1):
            put(cx,cap-1,zz,"dark_oak_log[axis=z]")
        put(cx,cap-2,cz-1,"spruce_stairs[facing=south,half=top]")
        for zz in (cz-2,cz-3):
            put(cx,cap-2,zz,"iron_chain[axis=y]")
            put(cx,cap-3,zz,"red_glazed_terracotta[facing=east]" if zz==cz-2 else "waxed_chiseled_copper")
        put(cx,cap-4,cz-3,"lantern[hanging=true]")

    # An inhabitable oriel breaks the line of the top floor. Its joists and stepped knees
    # can be read from below; a small pitched copper or timber gable covers the window seat.
    floor_y=cap-6
    mid=(w+e)//2 if gate else (n+s)//2
    def bay(k, depth, dy, state):
        x,z=(mid+k,n-depth) if gate else (e+depth,mid+k)
        put(x,floor_y+dy,z,state)
    axial="z" if gate else "x"
    inward="south" if gate else "west"
    for k in range(-2,3):
        for depth in (0,1,2):
            bay(k,depth,0,TIMBER+"[axis="+axial+"]" if abs(k)==2 else "spruce_planks")
            for dy in range(1,5):
                if abs(k)==2:
                    material=TIMBER if depth!=1 else PLASTER if dy==1 else pane("",not gate)
                elif depth==2:
                    if dy==1:
                        material="waxed_cut_copper" if gate else PLASTER
                    elif k==0 and dy<4:
                        material=TIMBER
                    else:
                        material=pane(("light_gray_stained_" if gate else "yellow_stained_") if dy==4 else "",gate)
                else:
                    material="air"
                bay(k,depth,dy,material)
            bay(k,depth,5,TIMBER+"[axis="+("x" if gate else "z")+"]")
        if abs(k)==2:
            bay(k,0,-2,"dark_oak_stairs[facing="+inward+",half=top]")
            bay(k,1,-1,"stripped_spruce_log[axis="+axial+"]")
            bay(k,2,-1,"spruce_stairs[facing="+inward+",half=top]")
        elif k==0:
            bay(k,1,1,"spruce_stairs[facing="+inward+"]")
    for k in range(-3,4):
        for depth in range(1,4):
            direction=("east" if k<0 else "west") if gate else ("south" if k<0 else "north")
            roof_y=5+(3-abs(k))//2
            metal="waxed_weathered_cut_copper" if gate else "dark_oak"
            piece=metal+"_slab[type=top]" if k==0 else metal+"_stairs[facing="+direction+"]"
            bay(k,depth,roof_y,piece)
    return rise, desired

