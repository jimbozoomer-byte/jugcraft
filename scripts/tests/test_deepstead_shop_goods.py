"""Keep the guild counter goods purchasable, exclusive and complete in generated data."""
import json
from pathlib import Path
import re
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import art_check
import deepstead_shop_goods as guild
import food_servings
import town_shops

RES = ROOT/"src/main/resources"
ART = RES/"assets/jugcraft"
IDS = {"jugcraft:"+row[0] for products in guild.STOCK.values() for row in products}

def read(path):
    return json.loads(path.read_text(encoding="utf-8"))

class GuildStock(unittest.TestCase):
    def test_display_assets_keep_their_model_and_material_details(self):
        for tool in guild.TOOLS:
            ref=food_servings.reference(read(ART/"items"/(tool+".json")))
            model=food_servings.model(ref)
            self.assertTrue(model.get("elements"),tool)
            self.assertIn("firstperson_righthand",model.get("display",{}),tool)
        for food in guild.FOODS:
            for prefix in ("guild_kitchen/", "food_serving/jugcraft/"):
                model=food_servings.model("jugcraft:block/"+prefix+food)
                faces={model["textures"][face["texture"][1:]]
                       for element in model["elements"] for face in element["faces"].values()}
                for view in ("top","side"):
                    self.assertIn("jugcraft:block/guild/"+food+"_"+view,faces)
        shapes=[]
        for state in range(3):
            model=read(ART/"models/block"/f"quarry_sample_cabinet_{state}.json")
            shapes.append([(e["from"],e["to"],e.get("rotation")) for e in model["elements"]])
        self.assertTrue(all(shapes[i]!=shapes[j] for i,j in ((0,1),(1,2),(2,0))))
        for state in (0,1):
            model=read(ART/"models/block"/f"copper_samovar_{state}.json")
            self.assertEqual(any(e.get("light_emission",0)>0 for e in model["elements"]),bool(state))

    def test_exclusive_stock_and_survival_sources(self):
        catalog=read(RES/"data/jugcraft/town/shops.json")["shops"]
        self.assertEqual(catalog,town_shops.data())
        self.assertEqual(town_shops.loop_errors(),[])
        self.assertEqual(len(IDS),20)
        self.assertEqual(IDS,set(read(RES/"data/jugcraft/tags/item/deepstead_shop_exclusives.json")["values"]))
        self.assertFalse(IDS.intersection(town_shops.tablet_outputs()))
        sales={key:[] for key in IDS}
        for shop,info in catalog.items():
            for side in ("sells","buys"):
                for offer in info[side]:
                    if offer["item"] in IDS:
                        self.assertEqual(side,"sells")
                        sales[offer["item"]].append(shop)
        for shop,stock in guild.STOCK.items():
            self.assertEqual(len(catalog[shop]["sells"]),10)
            for key,count,price,*_ in stock:
                item="jugcraft:"+key
                self.assertEqual(sales[item],[shop]);self.assertIn({"item":item,"price":price,"count":count},catalog[shop]["sells"])
        # Match complete resource IDs: prefixed placed-serving block IDs are allowed only to store the original stack.
        pattern=re.compile(b'"(?:'+b'|'.join(key.encode() for key in sorted(IDS))+b')"')
        for domain in (RES/"data").iterdir():
            for category in ("recipe","loot_table"):
                for path in (domain/category).rglob("*.json"):
                    refs=pattern.findall(path.read_bytes())
                    if not refs: continue
                    self.assertEqual(category,"loot_table",str(path))
                    key=path.stem
                    self.assertIn(key,guild.FURNISHINGS,str(path))
                    self.assertEqual(path,RES/"data/jugcraft/loot_table/blocks"/(key+".json"))
                    self.assertEqual(refs,[('"jugcraft:'+key+'"').encode()])

    def test_native_models_are_closed_and_their_textures_exist(self):
        models=set()
        for item in IDS:
            name=item.split(":")[1]
            ref=food_servings.reference(read(ART/"items"/(name+".json")))
            models.add(ref)
            if name in guild.FURNISHINGS:
                states=read(ART/"blockstates"/(name+".json"))["variants"]
                self.assertEqual(len(states),12);models.update(node["model"] for node in states.values())
        for name in guild.FOODS:
            models.add("jugcraft:block/food_serving/jugcraft/"+name)
        for ref in sorted(models):
            with self.subTest(model=ref):
                resolved=food_servings.model(ref)
                for tex in resolved["textures"].values():
                    if tex.startswith("#"):continue
                    ns,path=tex.split(":")
                    self.assertTrue(food_servings.read_asset(ns,"textures/"+path+".png"))
                els,lookup,_=art_check._resolve(read(ART/"models"/(ref.split(":")[1]+".json")),(ART,))
                if not els:continue
                faces,raw=art_check._model_faces(els,lookup)
                self.assertEqual(art_check._open_faces(els,faces),[])
                self.assertTrue(all(min(uv)>=-.001 and max(uv)<=16.001 for uv in raw))
                for i,j,gap,overlap,u,v,n,d in art_check._pairs(faces,art_check.N1_GAP):
                    if gap==0 and art_check._layered(els,faces[i],faces[j]):continue
                    self.assertFalse(art_check._differ(faces[i],faces[j],overlap,u,v,n,d),f"Overlapping faces {i}/{j}")
