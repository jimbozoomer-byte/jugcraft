"""Build reproducible essence values from numeric reference seeds and actual local recipes.

No upstream code is used. Unknown items remain accepted at the explicitly labelled
one-essence fallback; they are never presented as historical EE values. Run after
recipe generation. Runtime adds the same fallback for optional registered items.
Use --affected-item for a recipe-yield edit: only lower existing component/consumer
prices, keeping unrelated ingredient prices and catalogue membership unchanged.
"""
import argparse
from collections import Counter
from fractions import Fraction
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]

def recipe_consumers(edges, roots):
    affected=set(roots)
    while True:
        consumers={out for _,inputs,out,_ in edges if any(options & affected for options in inputs)}
        added=consumers-affected
        if not added:break
        affected.update(added)
    return affected


def limit_recalculation(existing, records, edges, roots):
    """Lower existing prices affected by a recipe edit without enabling unrelated catalogue items."""
    if set(roots)-set(existing):raise ValueError('Affected items must already be catalogued')
    affected=recipe_consumers(edges,roots)
    result=dict(existing)
    for item in affected & existing.keys() & records.keys():
        before,after=existing[item],records[item]
        if Fraction(after['numerator'],after['denominator']) <= Fraction(before['numerator'],before['denominator']):
            result[item]=after
    return result


def build(jar, affected_items=None):
    seeds = json.loads((ROOT / 'tools/data/alchemy_value_seeds.json').read_text())
    tags, recipes = {}, {}
    def take(name, data):
        parts = name.split('/')
        if len(parts) < 4 or parts[0] != 'data' or not name.endswith('.json'): return
        if parts[2:4] == ['tags', 'item']:
            key=parts[1]+':'+ '/'.join(parts[4:])[:-5]
            tags[key] = ([] if data.get('replace') else tags.get(key,[]))+data.get('values', [])
        elif parts[2] == 'recipe': recipes[parts[1]+':'+ '/'.join(parts[3:])[:-5]] = data
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if n.startswith('data/') and n.endswith('.json') and ('/recipe/' in n or '/tags/item/' in n): take(n, json.loads(z.read(n)))
    for base in ('src/main/resources', 'src/main/generated'):
        for p in sorted((ROOT/base/'data').rglob('*.json')):
            if '/recipe/' in p.as_posix() or '/tags/item/' in p.as_posix(): take(p.relative_to(ROOT/base).as_posix(), json.loads(p.read_text()))
    def expand(x, seen=frozenset()):
        if isinstance(x, dict): x=x.get('item', x.get('id', '#'+x['tag'] if 'tag' in x else ''))
        if isinstance(x, list): return set().union(*(expand(i,seen) for i in x))
        if not isinstance(x,str) or not x: return set()
        if not x.startswith('#'): return {x}
        tag=x[1:]
        if tag in seen:return set()
        return set().union(*(expand(v,seen|{tag}) for v in tags.get(tag,[])))
    values={k:Fraction(v) for k,v in seeds['items'].items()}
    origins={k:'reference' for k in values}
    for tag,v in seeds['tags'].items():
        for item in expand('#'+tag): values[item]=Fraction(v); origins[item]='reference tag '+tag
    # Container values include the recoverable shell. Filling water does not create a cheap bucket.
    for item,price in {'bucket':768,'water_bucket':768,'powder_snow_bucket':769,'lava_bucket':832,
                       'milk_bucket':832,'cod_bucket':832,'salmon_bucket':832,'pufferfish_bucket':832,
                       'tropical_fish_bucket':832,'axolotl_bucket':1024,'tadpole_bucket':832}.items():
        values['minecraft:'+item]=Fraction(price);origins['minecraft:'+item]='local container accounting'
    # Natural rubber shares the existing one-essence material. A filled latex bottle
    # must include its recoverable shell, and pot output must not count returned bottles twice.
    for item, price in {'jugcraft:latex_bottle': 2, 'jugcraft:rubber': 1}.items():
        values[item] = Fraction(price); origins[item] = 'local natural rubber/container accounting'
    # Saved walnut aliases drop the unified blocks. They must cost the same,
    # even though recipes now output only the canonical IDs.
    walnut_aliases = {'walnut_log': 32, 'walnut_wood': 32, 'stripped_walnut_log': 32,
                      'stripped_walnut_wood': 32, 'walnut_planks': 8, 'walnut_stairs': 12,
                      'walnut_slab': 4, 'walnut_fence': Fraction(40, 3), 'walnut_fence_gate': 32,
                      'walnut_leaves': 1, 'walnut_sapling': 32}
    for name, price in walnut_aliases.items():
        values['jugcraft:ecology_'+name] = Fraction(price)
        origins['jugcraft:ecology_'+name] = 'local walnut compatibility accounting'
    # A consumed husk replaces one brown dye; keep the established dye value.
    values['jugcraft:walnut_husk'] = Fraction(64)
    origins['jugcraft:walnut_husk'] = 'local walnut dye accounting'
    # A Roc Feather breaks into four feathers. It must cost what they do, or forming
    # one and breaking it would create essence.
    values['jugcraft:roc_feather'] = 4*values['minecraft:feather']
    origins['jugcraft:roc_feather'] = 'local roc feather accounting'
    # Selected chemistry/production integration: include returned bottles and
    # account for the existing presses' two-sheets-per-stock yield. Fluid processing
    # has no general derivation below, so record these explicit local budgets.
    for item, price in {'glucose_syrup': Fraction(2), 'boric_acid': Fraction(1, 2),
                        'plastic_sheet': Fraction(1, 2), 'silicone': Fraction(9, 8)}.items():
        values['jugcraft:'+item] = price
        origins['jugcraft:'+item] = 'local chemistry/output/container accounting'
    # These ores drop existing valuable quartz, including up to four with Fortune III.
    # Do not assign a cheap fallback that can be transmuted and mined for profit.
    for ore in ('quartz_ore', 'deepslate_quartz_ore'):
        values['jugcraft:'+ore] = 4 * values['minecraft:quartz']
        origins['jugcraft:'+ore] = 'local quartz mining/Fortune accounting'
    for metal in ('iron', 'gold', 'copper'):
        ingot = 'minecraft:' + metal + '_ingot'
        if ingot in values:
            values['minecraft:'+metal+'_nugget'] = values[ingot] / 9
            origins['minecraft:'+metal+'_nugget'] = 'local metal compression accounting'
    # Fishing loot can be reclaimed into historical metals: one discarded tin
    # smelts/blasts into seven iron nuggets, and one goldfish crafts into a gold
    # nugget. Price these otherwise uncraftable roots at their recoverable metal,
    # rather than letting a one-essence fallback devalue nuggets below their
    # reversible nine-nuggets-per-ingot conversion. The recipe-count regression
    # requires this accounting to be reviewed if either salvage yield changes.
    for item, metal, nuggets in (('aquatic_tin_can', 'iron', 7), ('aquatic_goldfish', 'gold', 1)):
        if not (ROOT/'src/main/resources/assets/jugcraft/items'/(item+'.json')).is_file():
            continue
        values['jugcraft:'+item] = nuggets * values['minecraft:'+metal+'_ingot'] / 9
        origins['jugcraft:'+item] = f'local aquatic salvage accounting ({nuggets} {metal} nugget yield)'
    # Roots of decorative families the tablet makes (docs/features/tablet-decorations.md) that nothing crafts: mud is
    # 5, the contributor's price (a water bottle makes it from dirt, so water treatment drying it into clay is a farm); resin clumps and glow ink sacs are mob/creaking drops (provisional, for the owner). Their
    # crafted forms (packed mud, mud bricks, resin bricks, glow item frames) then come from the game's recipes.
    for item, price in {'mud': 5, 'resin_clump': 16, 'glow_ink_sac': 64}.items():
        values['minecraft:'+item] = Fraction(price); origins['minecraft:'+item] = 'local decorative accounting'
    # Manipulation essence is uncommon loot; at one essence, eight tablet-made tuff and one essence became eight
    # calcite worth 224 more. This is the least price that makes that recipe (and deepslate to tuff) gain nothing.
    values['jugcraft:nouveau_manipulation_essence'] = 8 * Fraction(32) - 8 * Fraction(4)
    origins['jugcraft:nouveau_manipulation_essence'] = 'local tuff/calcite conversion accounting'
    # Materials no recipe makes under the tablet's decorations, and values only a machine recipe sets (the builder reads
    # none): without these they fell to the one-essence fallback, a Landscapes plank was worth a quarter and steel one.
    for item, price in json.loads((ROOT / 'tools/data/alchemy_decorative_roots.json').read_text())['items'].items():
        values[item] = Fraction(price); origins[item] = 'local decorative accounting'
    # Keep the established one-essence crop budget before resolving decorative
    # recipes. Seed/crop cycles otherwise leave these roots unresolved, and the
    # alphabetical fallback loop can lock a crafted basket at 1 before its flax
    # receives a value. This preserves crop prices while deriving the whole prop.
    for crop in ('corn', 'flax', 'cranberries', 'onion', 'dried_herbs'):
        values['jugcraft:' + crop] = Fraction(1)
        origins['jugcraft:' + crop] = 'fallback (balance pending)'
    locked=set(values)
    edges=[]
    for rid,r in recipes.items():
        typ=r.get('type',''); ingredients=[]
        if typ in ('minecraft:crafting_shaped','jugcraft:botanical_armor_upgrade','jugcraft:botanical_water_shaped','jugcraft:botanical_water_crafting','jugcraft:packworks_tier_upgrade'):
            for c,n in Counter(''.join(r['pattern'])).items():
                if c!=' ':ingredients.extend([expand(r['key'][c])]*n)
        elif typ=='minecraft:crafting_shapeless': ingredients=[expand(x) for x in r['ingredients']]
        elif typ=='minecraft:smithing_transform': ingredients=[expand(r[k]) for k in ('template','base','addition') if k in r]
        elif typ in ('minecraft:smelting','minecraft:blasting','minecraft:smoking','minecraft:campfire_cooking','minecraft:stonecutting'): ingredients=[expand(r['ingredient'])]
        elif typ.startswith('jugcraft:') and isinstance(r.get('ingredients'),list) and isinstance(r.get('result'),dict) and not any(k in r for k in ('fluids','fluid_inputs','fluid','byproducts','secondary')):
            for entry in r['ingredients']:
                if isinstance(entry,dict) and 'ingredient' in entry:
                    ingredients.extend([expand(entry['ingredient'])]*entry.get('count',1))
                else:ingredients.append(expand(entry))
        else: continue
        out=r.get('result'); out={'id':out} if isinstance(out,str) else out
        if not isinstance(out,dict) or not out.get('id') or not ingredients or any(not x for x in ingredients):continue
        # Recipes with remaining containers require explicit net accounting, not a guessed ingredient cost.
        if any(any(s.endswith('_bucket') or s=='minecraft:honey_bottle' for s in opts) for opts in ingredients):continue
        edges.append((rid,ingredients,out['id'],out.get('count',r.get('count',1))))
    local_items={'jugcraft:'+p.stem for p in (ROOT/'src/main/resources/assets/jugcraft/items').glob('*.json')}
    target=ROOT/'src/main/resources/data/jugcraft/alchemy/values.json'
    if affected_items:
        existing=json.loads(target.read_text(encoding='utf-8'))['items']
        if set(affected_items)-set(existing):raise ValueError('Affected items must already be catalogued')
        affected=recipe_consumers(edges,affected_items)
        # Keep the current prices of all unrelated ingredients and do not enable uncatalogued items.
        values={k:Fraction(v['numerator'],v['denominator']) for k,v in existing.items()}
        origins={k:v['source'] for k,v in existing.items()}
        # A consumer walk can reach a seeded material through a recycling recipe.
        # That does not authorize revaluing the historical roots (e.g. iron/gold)
        # or their entire downstream economy. Preserve their existing anchors.
        locked=(set(values)-affected) | {k for k,v in existing.items() if v['source'].startswith('reference')}
        edges=[edge for edge in edges if edge[2] in affected and edge[2] in existing]
        local_items=set(values)
    # Root materials without a known historical value receive an explicit local base
    # assignment. Craftable products inherit actual ingredient costs, not that fallback.
    outputs={edge[2] for edge in edges}
    for item in local_items-outputs-set(values):values[item]=Fraction(1);origins[item]='fallback (balance pending)'
    def propagate():
        for _ in range(64):
            changed=False
            for rid,inputs,out,count in edges:
                costs=[min((values[i] for i in options if i in values),default=None) for options in inputs]
                if any(v is None for v in costs) or out in locked:continue
                # Keep fractions: forcing a cheap nugget/slab up to one creates value in crafting loops.
                v=(sum(costs) - (values["minecraft:glass_bottle"] if rid == "jugcraft:sugar_from_glucose_syrup" else 0))/count
                if v<=0 or v>1_000_000_000 or v.numerator>2**63-1 or v.denominator>2**63-1:continue
                if out not in values or v<values[out]:values[out]=v;origins[out]='recipe '+rid;changed=True
            if not changed:break
    propagate()
    # Decorative blocks no recipe makes (docs/features/tablet-decorations.md): each is worth its plain counterpart,
    # never less than what a recipe makes from it, so pricing them lowers nothing already derived. Weathered copper is
    # the copper it grew from; concrete is its powder set in water. Froglights, shroomlights and the bell are
    # provisional, for the owner.
    natural = {'pale_moss_block': 'moss_block', 'pale_moss_carpet': 'moss_carpet', 'wildflowers': 'yellow_dye',
               'red_mushroom_block': 'red_mushroom', 'brown_mushroom_block': 'brown_mushroom',
               'mushroom_stem': 'brown_mushroom', 'warped_wart_block': 'nether_wart_block',
               'stripped_bamboo_block': 'bamboo_block', 'rooted_dirt': 'dirt'}
    for form in ('cut_copper', 'cut_copper_slab', 'cut_copper_stairs', 'chiseled_copper', 'copper_grate', 'copper_door',
                 'copper_trapdoor', 'copper_bars', 'copper_chain', 'copper_lantern'):
        for stage in ('exposed', 'weathered', 'oxidized'):
            natural[stage+'_'+form] = form
    for colour in ('white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
                   'purple', 'blue', 'brown', 'green', 'red', 'black'):
        natural[colour+'_concrete'] = colour+'_concrete_powder'
    for item, base in natural.items():
        if 'minecraft:'+base in values:
            values['minecraft:'+item] = values['minecraft:'+base]; origins['minecraft:'+item] = 'local decorative accounting'
    for item, price in {'ochre_froglight': 64, 'verdant_froglight': 64, 'pearlescent_froglight': 64, 'shroomlight': 64,
                        'bell': 8192}.items():
        values['minecraft:'+item] = Fraction(price); origins['minecraft:'+item] = 'local decorative accounting'
    locked |= {k for k, s in origins.items() if s == 'local decorative accounting'}
    propagate()
    # Unseeded reversible families (ingot <-> block/nugget) need one base, not three
    # independent one-essence fallbacks. Anchor the unit and derive its other forms.
    while local_items-set(values):
        pending=local_items-set(values)
        root=min(pending,key=lambda s:(0 if s.endswith(('_ingot','_gem','_dust','_planks')) else 1 if not s.endswith(('_block','_nugget','_slab')) else 2,s))
        values[root]=Fraction(1);origins[root]='fallback (balance pending)';locked.add(root);propagate()
    # Independent rare-earth outputs must fit the input budget even when all
    # three rolls succeed. Allocate one quarter of the mixed feed to each
    # named unit: one Nd + two Ce + one Y spends the whole mixed-oxide value.
    # Do not price only the average drop: lucky batches must not mint essence.
    mixed_oxide = values.get('jugcraft:rare_earth_oxide', Fraction(1))
    for material in ('neodymium_oxide', 'cerium_oxide', 'yttrium_oxide'):
        item = 'jugcraft:' + material
        if item in local_items:
            values[item] = mixed_oxide / 4
            origins[item] = 'local rare-earth maximum coproduct allocation'
            locked.add(item)
    # Fluid-machine recipes are not derived above, so every zirconia form would keep
    # the one-essence fallback while finishing turns one fired blank into two inserts.
    # Price each form at its two-insert yield: finishing, firing, conditioning and the
    # short sand/concentrate routes then spend at least the value they return.
    insert = values.get('jugcraft:zirconia_insert', Fraction(1))
    for material in ('zirconia_precursor', 'zirconia_powder', 'zirconia_charge',
                     'zirconia_blank', 'fired_zirconia_blank'):
        item = 'jugcraft:' + material
        if item in local_items:
            values[item] = 2 * insert
            origins[item] = 'local zirconia finishing accounting'
            locked.add(item)
    propagate()
    # Every local item definition has a value, including uncraftable decorative assets.
    for key in local_items:
        if key not in values:values[key]=Fraction(1);origins[key]='fallback (balance pending)'
    # New botanical dye routes can lower brown dye below its historical seed.
    # Husk conversion must track that final value, not the pre-propagation 64.
    if not affected_items or 'jugcraft:walnut_husk' in affected:
        values['jugcraft:walnut_husk']=values['minecraft:brown_dye']
        origins['jugcraft:walnut_husk']='local walnut dye accounting (resolved brown dye)'
    propagate()
    records={k:{'numerator':v.numerator,'denominator':v.denominator,'source':origins[k]} for k,v in sorted(values.items())}
    if affected_items:
        records=limit_recalculation(existing,records,edges,affected_items)
        print(f'Scoped recalculation: {sum(records[k] != existing[k] for k in existing)} existing records updated; catalogue membership unchanged')
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_text(json.dumps({'schema':1,'fallback':1,'items':records},indent=2)+'\n')
    print(f'{len(records)} values: {Counter("fallback" if r["source"].startswith("fallback") else "recipe" if r["source"].startswith("recipe") else "reference" for r in records.values())}')
    return records

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--minecraft-jar',required=True)
    parser.add_argument('--affected-item',action='append',help='Limit reductions to this existing item and its recipe consumers; repeat for multiple items')
    args=parser.parse_args()
    build(args.minecraft_jar,args.affected_item)
