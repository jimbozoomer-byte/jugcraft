"""Final owner of Art Batch 6 effect textures and the native strain pill atlas.

Effect maps use the same editable 16px/material system as the delivered items.
The 9px HUD source is separate because it follows vanilla heart/hunger footprints.
"""
from pathlib import Path
import csv
from PIL import Image
import arms_icons
from check_icon_maps import Map

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/jugcraft'
EFFECTS=tuple(row['effect_id'].split(':')[1] for row in csv.DictReader(
    (ROOT/'docs/plans/pharmacology-item-art/effect-icon-plan.csv').read_text(encoding='utf-8').splitlines()))
# dark, middle, highlight; keep in sync with StrainPillHud's forecast palette.
PILL_COLORS=((0x292b2c,0x434644,0x656962),(0xaaa98d,0xe2dfbc,0xfff3ce),
             (0x9f7430,0xe1b54f,0xffe58a),(0x9c4a25,0xdd833c,0xffc172),
             (0x852f36,0xca5559,0xff9891),(0x573576,0x9260ba,0xd19aed))

def render_effect(key):
    icon=Map(str(ROOT/'tools/item_icons'/('pharmacy_effect_'+key+'.txt')))
    if not icon.shape_ok():raise ValueError(icon.problems)
    colors=arms_icons.palette(icon.style());image=Image.new('RGBA',(16,16))
    for x,y,ch in Map.cells(icon.rows):image.putpixel((x,y),colors[ch])
    return image

def pill_atlas():
    rows=[line.strip() for line in (ROOT/'tools/hud_icons/strain_pill.txt').read_text(encoding='utf-8').splitlines() if not line.startswith('#')]
    assert len(rows)==9 and all(len(row)==9 and set(row)<=set('.ODMH') for row in rows)
    image=Image.new('RGBA',(54,54))
    for layer,colors in enumerate(PILL_COLORS):
        for columns in range(6):
            for y,row in enumerate(rows):
                for x,ch in enumerate(row):
                    if ch=='.':continue
                    if layer==0:color=0x171718 if ch=='O' else colors['DMH'.index(ch)]
                    elif ch=='O' or x-2>=columns:continue
                    else:color=colors['DMH'.index(ch)]
                    image.putpixel((columns*9+x,layer*9+y),((color>>16)&255,(color>>8)&255,color&255,255))
    return image

def generate_textures():
    effects=ASSETS/'textures/mob_effect';effects.mkdir(parents=True,exist_ok=True)
    for key in EFFECTS:render_effect(key).save(effects/(key+'.png'))
    gui=ASSETS/'textures/gui';gui.mkdir(parents=True,exist_ok=True)
    pill_atlas().save(gui/'strain_pills.png')

if __name__=='__main__':generate_textures()
