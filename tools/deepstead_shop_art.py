"""Rebuild the Stonewright and Copper Kettle wares and their existing item definitions."""
import json
import model_writer
import guild_kitchen
import guild_stonework
from guild_materials import write, paint
from deepstead_wares import ART, RES, ROOT
from deepstead_shop_goods import STONE, KETTLE, FURNISHINGS, FOODS, TOOLS, WOOD, LANG, SHOPS


def furniture():
    guild_stonework.masonry()
    guild_stonework.cabinets()
    guild_stonework.cutting_bench()
    for heat in (0,1):
        guild_kitchen.samovar(heat)
    guild_kitchen.basket()
    guild_kitchen.tankards()
    guild_kitchen.gong()
    for name in FURNISHINGS:
        count=3 if name=="quarry_sample_cabinet" else 2 if name=="copper_samovar" else 1
        def variant(i):
            return "jugcraft:block/"+name+(("_"+str(min(i,count-1))) if count>1 else "")
        states={f"facing={f},setting={i}":{"model":variant(i),"y":y} for i in range(3) for f,y in model_writer.FACING_Y.items()}
        write(ART/"blockstates"/(name+".json"),{"variants":states})
        item(name,variant(0))
        write(RES/"data/jugcraft/loot_table/blocks"/(name+".json"),{"type":"minecraft:block","pools":[{"rolls":1,
            "entries":[{"type":"minecraft:item","name":"jugcraft:"+name}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})

def item(name, model):
    write(ART/"items"/(name+".json"),{"model":{"type":"minecraft:model","model":model}})

def publish_data():
    import town_shops
    path=RES/"data/jugcraft/town/shops.json"
    current=json.loads(path.read_text())
    current["shops"]=town_shops.data()
    write(path,current)
    write(RES/"data/jugcraft/tags/item/deepstead_shop_exclusives.json",
        {"values":["jugcraft:"+row[0] for row in (*STONE,*KETTLE)]})
    for tool in ("axe","pickaxe"):
        path=RES/"data/minecraft/tags/block/mineable"/(tool+".json")
        tag=json.loads(path.read_text())
        new=["jugcraft:"+k for k in FURNISHINGS if (k in WOOD)==(tool=="axe")]
        tag["values"]=[k for k in tag["values"] if k not in new]
        anchor="jugcraft:town_commission_board" if tool=="axe" else "jugcraft:gallery_chimes"
        insert=tag["values"].index(anchor)+1
        tag["values"][insert:insert]=new
        write(path,tag)
    path=ART/"lang/en_us.json"
    language=json.loads(path.read_text(encoding="utf-8"))
    language.update(LANG)
    language.update({"shop.jugcraft."+key:name for key,name in SHOPS.items()})
    path.write_text(json.dumps(language,indent=2)+"\n",encoding="utf-8")
    path=ROOT/"tools/food_servings_catalog.json"
    catalog=json.loads(path.read_text(encoding="utf-8"))
    present={row["id"] for row in catalog}
    catalog.extend({"id":"jugcraft:"+key,"family":family} for key,family in FOODS.items() if "jugcraft:"+key not in present)
    write(path,catalog)
    import food_servings
    food_servings.generate(write,{"jugcraft:"+name for name in FOODS})

if __name__=="__main__":
    paint()
    furniture()
    guild_stonework.instruments()
    for name in TOOLS:
        item(name,"jugcraft:item/"+name)
    guild_kitchen.pasty()
    guild_kitchen.skewers()
    guild_kitchen.tart()
    guild_kitchen.hotpot()
    guild_kitchen.drinks()
    for name in FOODS:
        item(name,"jugcraft:block/guild_kitchen/"+name)
    publish_data()
