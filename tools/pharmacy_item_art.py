"""Canonical pharmacy Art Batches 1–5 maps/models using the shared item-icon renderer."""
from pathlib import Path
from PIL import Image
from asset_png import save_rgba
import arms_icons
from check_icon_maps import Map
from pharmacy_art_catalog import BATCH2, BATCH3, BATCH4, BATCH5

ROOT=Path(__file__).resolve().parents[1]
BATCH1=('caffeine_drink','aspirin','paracetamol','rehydration_salts','activated_charcoal','stimulant',
       'penicillin','lidocaine','adrenaline','salbutamol','morphine','naloxone',
       'methylphenidate','amphetamine','modafinil','ketamine','fentanyl')
BATCHES={1:BATCH1, 2:BATCH2, 3:BATCH3, 4:BATCH4, 5:BATCH5}
ITEMS=tuple(key for batch in BATCHES.values() for key in batch)
DEVICES={'adrenaline','salbutamol','naloxone'}
SMALL={'aspirin','paracetamol','activated_charcoal','stimulant','methylphenidate','amphetamine','modafinil',
       'lsd','fentanyl_patch','thc','psilocybin','mescaline','dmt',
       'caffeine_xr','methylphenidate_xr','amphetamine_xr','modafinil_xr',
       'empty_pharmacy_capsule','botanical_capsule_shell'}

def render(key):
    icon=Map(str(ROOT/'tools/item_icons'/('pharmacy_'+key+'.txt')))
    if not icon.shape_ok(): raise ValueError(icon.problems)
    colors=arms_icons.palette(icon.style())
    image=Image.new('RGBA',(16,16),(0,0,0,0))
    for x,y,ch in Map.cells(icon.rows): image.putpixel((x,y),colors[ch])
    return image

def generate_textures():
    folder=ROOT/'src/main/resources/assets/jugcraft/textures/item/pharmacy'
    folder.mkdir(parents=True,exist_ok=True)
    for key in ITEMS: save_rgba(render(key), folder/(key+'.png'))
    # Run after older pharmacology alias writers and at the end of both canonical generators.
    import pharmacy_hud_art
    pharmacy_hud_art.generate_textures()

def write_all(write,assets):
    generate_textures()
    for key in ITEMS:
        scale=.5 if key in BATCH5 else .4 if key in SMALL else .58
        display={hand:{'rotation':[0,-90 if 'right' in hand else 90,25 if 'right' in hand else -25],
                       'translation':[1.13,3.2,1.13],'scale':[scale]*3}
                 for hand in ('firstperson_righthand','firstperson_lefthand')}
        model={'parent':'minecraft:item/generated','textures':{'layer0':'jugcraft:item/pharmacy/'+key},'display':display}
        write(assets/'models/item'/f'{key}.json',model)
        base={'type':'minecraft:model','model':'jugcraft:item/'+key}
        if key in DEVICES:
            held={'rotation':[0,-90,-40] if key=='adrenaline' else [0,-90,0],
                  'translation':[-1,1,-2] if key=='adrenaline' else [1,5,-2],'scale':[.58]*3}
            left={'rotation':[0,90,40] if key=='adrenaline' else [0,90,0],
                  'translation':held['translation'],'scale':held['scale']}
            write(assets/'models/item'/f'{key}_using.json',dict(model,display={'firstperson_righthand':held,'firstperson_lefthand':left}))
            base={'type':'minecraft:condition','property':'minecraft:using_item',
                  'on_true':{'type':'minecraft:model','model':'jugcraft:item/'+key+'_using'},'on_false':base}
        write(assets/'items'/f'{key}.json',{'model':base})

def language(lang):
    for key in DEVICES:
        text=lang.get('tooltip.jugcraft.'+key,'')
        suffix=' Single-use device; returns its empty glass cartridge.'
        if suffix.strip() not in text:lang['tooltip.jugcraft.'+key]=text+suffix

if __name__=='__main__': generate_textures()
