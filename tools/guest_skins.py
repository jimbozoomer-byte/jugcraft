"""Named guest skins, using town_skins' editable player-UV painter.

Original pixel interpretations, not downloaded skins. No randomness/noise, no
resampling: classic 64x64 player UVs with opaque base and transparent outer hair.
Run this file to regenerate only guest_*.png; it never rewrites the town cast.
"""
from pathlib import Path
from PIL import Image, ImageDraw
from town_skins import Skin, OUT, faces, darker, lighter

def rgb(value):
    return tuple(bytes.fromhex(value))

# Hair, eyes, outfit, lower garment, trim, skin; one deliberate design per identity.
DESIGNS = {
    'emiru': ('34272c','715449','34313e','383343','bb718a','efd0bd','long','hoodie'),
    'lauren_chen': ('292329','533c30','ece4db','344257','b58d5a','e5bfa5','long','blazer'),
    'brittany_venti': ('34251f','574334','743d4d','32313e','c1966c','d7ac8c','curls','blazer'),
    'aoc': ('372821','533e30','f1e8d9','ddd4c7','b78949','c99473','long','blazer'),
    'sarah_stock': ('b99b63','627b88','eee5d6','e3d9c8','a08bb0','b1bb9e','long','bride'),
    'pokimane': ('5b3d30','614b35','c79bab','4d435a','efe4d5','e2b897','curls','hoodie'),
    'power': ('e3bb99','bd623a','e9e5dc','343649','253c61','f1cbb6','long','tie'),
    'makima': ('ae594b','c6a148','e9e5df','30313b','292b34','f0ceba','braid','tie'),
    'reze': ('51405f','648866','e8e3db','333344','27232f','edc8b9','bob','tie'),
    'asa_mitaka': ('282733','645348','e7e5df','263344','352b3a','efd1c2','long','uniform'),
    'himeno': ('263c3d','6c9978','30353d','272c35','ede5dc','e8c7af','bob','suit'),
    'tokai_teio': ('9a5b39','73538f','e9e4d8','e3ddd3','326f9f','f0cbb8','long','race'),
    'agnes_tachyon': ('9b6345','a55565','ece6d8','423344','ad3546','f0cdb7','bob','lab'),
    'gold_ship': ('c6c6d0','b64e62','9a3449','8c3044','ddd8c6','f1d5c5','long','race'),
    'oguri_cap': ('c5c4d1','617aa0','e8e4db','ddd7ce','3d5b88','edcebb','long','race'),
    'special_week': ('6e453e','8064a6','eae3d9','e3dace','715090','efcbb9','long','race'),
    'rei_ayanami': ('a5bfd0','a74b53','ebe9de','3f667a','943b42','eed4c7','bob','uniform'),
    'asuka_langley': ('b76d39','4d79aa','e8e4d8','34597a','a53936','f0c9ad','long','uniform'),
    'misato_katsuragi': ('383148','745071','9c3c45','37313f','d9bd62','edc3ae','long','jacket'),
    'frieren': ('dcdcd5','4a866d','eee9d8','33313e','c6a15d','f2d6c3','long','mage'),
    'rem': ('7dadd1','486fbb','33313d','302e3a','e5e3df','f0d3c4','bob','maid'),
    'megumin': ('4a3430','a34547','9c3337','69343a','d6b35c','efcbb8','bob','mage'),
    'zero_two': ('dd9baa','4f9391','ad3b42','a0363d','e5dfcf','f1cdbc','long','military'),
    'rin_ishtar': ('4b302b','487b9e','a5343c','302d39','e6dfd3','f0ccba','long','cross'),
    'tifa_lockhart': ('30282b','8b493e','e6e4df','333139','8f3338','edc2aa','long','fighter'),
    'yorha_2b': ('dcd9d3','737786','302e39','292733','b7b0a8','eed4c3','bob','android'),
    'chun_li': ('352b28','644733','315ca0','6e4e3b','dabf66','e8b998','bun','qipao'),
    'ada_wong': ('292831','5a4b42','9f303d','352e38','bd8d60','ebc3ac','bob','agent'),
    'lara_croft': ('594030','705039','5b9690','74563e','393439','d8a67d','braid','explorer'),
    'kafka': ('6c364d','a5668f','e1d8d0','4a3546','986582','e8c4b7','long','jacket'),
    'miyabi': ('30323e','a24748','334052','2e3444','548cb1','edcbb9','long','military'),
    'nicole_demara': ('de9bab','65966e','e6e1d5','383344','77a486','ebc3ad','long','jacket'),
}

def rect(s, part, face, x, y, w, h, colour):
    for j in range(y, y+h):
        for i in range(x, x+w):
            s.dot(part, face, i, j, colour)

def make(name, spec):
    hair, eyes, cloth, trousers, trim, skin = map(rgb, spec[:6])
    length, outfit = spec[6:]
    s = Skin(0, clean=True)
    for part in ('head','body','right_arm','left_arm','right_leg','left_leg'):
        s.fill(part, skin, folds=False)
    s.fill('body', cloth)
    for part in ('right_arm','left_arm'):
        s.fill(part, cloth, rows=(0,9))
        s.fill(part, trim, rows=(8,9))
    for part in ('right_leg','left_leg'):
        s.fill(part, trousers)
        s.fill(part, rgb('34313a'), rows=(9,12))
        rect(s,part,'front',0,10,4,1,rgb('514a50'))
    # Neck opening and fitted seams, with no anatomy drawn on clothes.
    rect(s,'body','front',3,0,2,2,skin)
    for x in (0,7):
        rect(s,'body','front',x,2,1,10,darker(cloth,.88))
    rect(s,'body','front',0,10,8,1,trim)
    rect(s,'body','back',0,10,8,1,trim)
    # Single-pixel-high eyes, ordinary Minecraft face proportions.
    for x in (1,5):
        rect(s,'head','front',x,4,2,1,rgb('f3eee4'))
    for x in (2,5): s.dot('head','front',x,4,eyes)
    rect(s,'head','front',3,6,2,1,darker(skin,.79))
    s.fill('head',hair,sides=['top','back'])
    s.fill('head',hair,sides=['left','right'],rows=(0,7 if length in ('long','curls') else 5))
    rect(s,'head','front',0,0,8,2,hair)
    for x in (0,7): rect(s,'head','front',x,2,1,4,hair)
    for x in (1,4,6): s.dot('hat','front',x,2,hair)
    s.fill('hat',hair,sides=['top','back'])
    s.fill('hat',hair,sides=['left','right'],rows=(0,7 if length in ('long','curls') else 5))
    for face in ('top','back','left','right'):
        for y in range(8 if face in ('top','back') else 5):
            for x in range(8):
                if (x+(y//2 if length=='curls' else 0))%3==0:
                    s.dot('hat',face,x,y,lighter(hair,1.10))
    if length in ('long','curls','braid'):
        for face in ('front','back'):
            for x in ([3,4] if length=='braid' else [0,1,6,7]):
                rect(s,'jacket',face,x,0,1,6 if length!='braid' else 9,hair)
                rect(s,'jacket',face,x,1,1,3,lighter(hair,1.12))
    # Clothes are drawn around every side, not a flat front decal.
    if outfit in ('tie','suit','uniform','lab','military'):
        if outfit=='suit': rect(s,'body','front',2,0,4,9,rgb('e7e2d9'))
        for x in (2,5): s.dot('body','front',x,1,rgb('f2ede4'))
        rect(s,'body','front',3,2,2,5,trim)
        s.dot('body','front',3,7,trim)
    if outfit in ('blazer','jacket','lab','military'):
        for x in (1,6):
            rect(s,'jacket','front',x,2,1,8,darker(cloth,.75))
        for y in (4,7,9): s.dot('jacket','front',5,y,rgb('cbb98b'))
        rect(s,'body','front',5,6,2,1,trim)
    if outfit=='hoodie':
        for x in (2,5): rect(s,'body','front',x,1,1,4,trim)
        rect(s,'body','front',2,7,4,2,darker(cloth,.8))
    if outfit in ('maid','bride'):
        rect(s,'jacket','front',2,2,4,10,rgb('eee8df'))
        for part in ('right_leg','left_leg'):
            s.fill(part,rgb('e9e2d9') if outfit=='bride' else cloth,rows=(0,6))
        for x in range(1,7): s.dot('hat','front',x,0,rgb('f2eee6'))
        if outfit=='bride':
            s.fill('hat',rgb('e8e0d1'),sides=['back'],alpha=150)
            for x in (2,5): s.dot('hat','front',x,1,trim)
    if outfit=='race':
        for part in ('body','right_arm','left_arm'):
            s.fill(part,trim,rows=(0,2))
        rect(s,'body','front',3,2,2,8,trim)
        for y in (3,6,8): s.dot('body','front',4,y,rgb('d9ba67'))
    if outfit=='mage':
        for part in ('body','right_arm','left_arm'):
            s.fill(part,trim,rows=(9,10))
        for part in ('right_leg','left_leg'): s.fill(part,cloth,rows=(0,7))
        rect(s,'body','front',3,2,2,7,trim)
    if outfit=='cross':
        rect(s,'body','front',3,2,1,4,trim); rect(s,'body','front',2,3,3,1,trim)
    if outfit in ('fighter','explorer','agent'):
        for part in ('right_arm','left_arm'):
            s.fill(part,skin,rows=(3,9),folds=False)
            s.fill(part,trim,rows=(9,11))
        for x in (1,6): rect(s,'body','front',x,0,1,10,rgb('3c3332'))
    if outfit=='qipao':
        rect(s,'body','front',5,0,1,10,trim)
        for y in (2,5): rect(s,'body','front',3,y,3,1,trim)
        for face in ('left','right'): rect(s,'hat',face,2,1,3,3,rgb('ece7dc'))
        for part in ('right_arm','left_arm'): s.fill(part,rgb('e0d8c9'),rows=(8,10))
    if outfit=='android':
        rect(s,'hat','front',0,3,8,2,rgb('302e37'))
        for x in (2,5): rect(s,'body','front',x,2,1,5,trim)
    if name=='himeno':
        rect(s,'hat','front',0,4,3,2,rgb('282b32'))
        for x in range(8): s.dot('hat','front',x,3,rgb('303139'))
    if name in ('power','zero_two'):
        for x in (1,6): rect(s,'hat','front',x,0,1,3,rgb('b23d47'))
    if name in ('tokai_teio','oguri_cap','special_week'):
        rect(s,'hat','front',3,0,2,3,rgb('eee5d6'))
    if name=='gold_ship':
        rect(s,'hat','front',2,0,4,2,rgb('5f536c'))
        rect(s,'hat','front',3,1,2,1,rgb('cdb56e'))
    if name=='rem':
        rect(s,'hat','front',4,1,3,4,hair)
        for x,y in ((1,1),(0,2),(2,2),(1,3)): s.dot('hat','front',x,y,rgb('d99fb5'))
    if name=='megumin':
        rect(s,'hat','front',0,0,8,2,rgb('604237'))
        for x in (2,5): s.dot('hat','front',x,1,trim)
    if name=='kafka':
        for x in (1,5): rect(s,'hat','front',x,0,2,1,rgb('4d293b'))
    if name=='nicole_demara':
        rect(s,'body','front',3,4,2,2,rgb('4c382f'))
    # Reserved unused UV island (0..7, 0..7) for closed head accessory geometry.
    accessory = (rgb('604237') if name=='megumin' else
                 rgb('b23d47') if name in ('power','zero_two') else
                 skin if name=='frieren' else hair)
    for y in range(8):
        for x in range(8):
            s.px[x,y] = (*accessory,255)
            s.px[24+x,y] = (*trim,255)
    return s

def write():
    OUT.mkdir(parents=True,exist_ok=True)
    for name,spec in DESIGNS.items(): make(name,spec).save(OUT/f'guest_{name}.png')

def preview(path):
    """Unfiltered front/back UV contact sheet for design review, not an in-game render."""
    sheet=Image.new('RGB',(8*160,4*200),(35,39,47)); draw=ImageDraw.Draw(sheet)
    for index,(name,spec) in enumerate(DESIGNS.items()):
        s=make(name,spec)
        x=(index%8)*160; y=(index//8)*200
        for column,face in enumerate(('front','back')):
            sprite=Image.new('RGBA',(16,32))
            for part,at in [('head',(4,0)),('body',(4,8)),('right_arm',(0,8)),('left_arm',(12,8)),('right_leg',(4,20)),('left_leg',(8,20))]:
                for layer in [part,{'head':'hat','body':'jacket','right_arm':'right_sleeve','left_arm':'left_sleeve','right_leg':'right_pants','left_leg':'left_pants'}[part]]:
                    u,v,w,h=faces(layer)[face]
                    sprite.alpha_composite(s.img.crop((u,v,u+w,v+h)),at)
            sprite=sprite.resize((64,128),Image.Resampling.NEAREST)
            sheet.paste(sprite,(x+column*76+8,y+35),sprite)
        draw.text((x+5,y+8),name,fill='white')
        draw.text((x+16,y+169),'front      back',fill=(180,185,195))
    path.parent.mkdir(parents=True,exist_ok=True); sheet.save(path)

if __name__=='__main__':
    write(); preview(Path('build/jze-guest-skins.png')); print(f'Wrote {len(DESIGNS)} guest skins')
