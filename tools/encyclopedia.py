"""Item-free Encyclopedia: canonical handbook/codex text and optional advancement-backed goals."""
import copy,re
import handbook,concordance

def gate(condition):
    if not condition: return ''
    research,state=condition if isinstance(condition,tuple) else ('first_light',condition)
    return 'jugcraft:'+concordance.research_advancement(research,state)

def clean(text):
    return text.replace('\\\\\n','\n').replace('**','').replace('\\','').replace('$(br2)','\n\n')

def build():
    entries=[]
    for ci,ch in enumerate(handbook.build()['chapters']):
        for pi,p in enumerate(ch['pages']):
            p=copy.deepcopy(p)
            entries.append(dict(id=f'tech/{ci}/{pi}',group='Technology',chapter=ch['title'],page=p,gate=''))
    import petro,industrial_forms
    for machine,recipes in {**petro.FLUID_RECIPES,**industrial_forms.FORM_RECIPES}.items():
        for recipe in recipes:
            def name(identifier):return identifier.split(':')[-1].replace('_',' ').title()
            parts=[f"{count} x {name(item)}" for item,count in recipe.get('items',[])]
            parts += [f"{amount} mB {name(fluid)}"+(' (retained; not consumed)' if i in recipe.get('retained_inputs',[]) else '') for i,(fluid,amount) in enumerate(recipe.get('fluids',[]))]
            outputs=[f"{count} x {name(item)}" for item,count in recipe.get('results',[])]
            outputs += [f"{row[1]} mB {name(row[0])}" for row in recipe.get('fluid_results',[])]
            from machine_consolidation import LEGACY, PRIMARY
            recommended = LEGACY.get(machine, machine)
            display = PRIMARY[recommended][0] if recommended in PRIMARY else name(machine)
            lines=['Machine: '+display,'Inputs: '+(', '.join(parts) or 'None'),'Outputs: '+(', '.join(outputs) or 'None'),f"Work: {recipe['ticks']} ticks ({recipe['ticks']/20:g} seconds)."]
            if recipe.get('tool'):lines.append('Reusable tool: '+name(recipe['tool']))
            if recipe.get('energy_per_tick'):lines.append(f"Power: {recipe['energy_per_tick']} JE/t before upgrades.")
            lines.append('Recipe conditions: '+', '.join(recipe.get('features',[]))+'. Installed-build reference; server datapacks may differ.')
            entries.append(dict(id=f"process/{machine}/{recipe['name']}",group='Technology',chapter='Processes',gate='',page=dict(title=name(recipe['name']),icon='jugcraft:'+machine,text=lines)))
    for (category,key),info in concordance.codex().items():
        for pi,p in enumerate(info['pages']):
            condition=p[3] if p[0]=='text' and len(p)>3 else info['condition']
            page=dict(title=p[1],icon=info['icon'],text=[clean(p[2])])
            if p[0]!='text':page['text'].append('Recipe: '+str(p[3])+'. See the Arcane Concordance codex for its live recipe view.')
            entries.append(dict(id=f'magic/{category}/{key}/{pi}',group='Magic',chapter=concordance.CATEGORIES[category]['name'],page=page,gate=gate(condition)))
    overviews=[
      ('welcome','Jugcraft Encyclopedia','minecraft:book',[
       'Build a home, choose a specialty, and connect your work to other players. Technology and magic each have their own starting paths. Trading can replace building an entire supply chain yourself.',
       'Technology and Magic contain the existing detailed guides. Search titles, chapters and page text. Routes show suggested learning order, not compulsory quest gates. Goals show your server-recorded progress; reading never completes a goal or unlocks research.',
       'Goals are personal, permanent advancement records. Obtaining goals accept crafting, trade or found items under their original rules; use/research goals require the recorded action. Nothing is consumed and no extra rewards are granted. Existing completion remains valid.',
       'Technology recipes reflect this installed build. Server datapacks may change recipes; consult a live machine when they differ. Locked magic details follow existing research milestones. Refresh goals after making progress.']),
      ('acid','Your first connected acid factory','jugcraft:chemical_reactor',[
       'Start with a reachable steel workshop, a power supply, pipes and tanks. You can buy the metals, catalysts or starter acid from another player. Normal machines do not require completing Encyclopedia goals.',
       'Sulfuric branch: oxygen + sulfur dust in the Chemical Oxidizer -> sulfur dioxide; sulfur dioxide + oxygen in the Chemical Infuser with a Vanadium Contact Bed -> sulfur trioxide. Pipe the gas and water into separate reactor feeds.',
       'Make the first bucket of sulfuric acid with the existing sulfur + water recipe, or trade for it. Empty ordinary process acid out before switching to water. Use the starter bucket on TOP of the reactor to fill its separate retained charge. It stays in the reactor, including when picked up.',
       '250 mB water + 250 mB sulfur trioxide -> 500 mB sulfuric acid per 5 seconds at 96 JE/t. The 1000 mB carrier is required but never consumed or counted as output. Full or incompatible output stops work before ingredients are spent.',
       'Hydrochloric branch: brine electrolysis supplies hydrogen and chlorine. The Infuser combines them into hydrogen chloride gas. The reactor takes 500 mB gas + 500 mB water -> 1000 mB hydrochloric acid per 5 seconds at 96 JE/t; no carrier needed.',
       'Sulfuric acid feeds existing ore recovery, fertilizer, electroplating, electronics and electrolyte recipes. Hydrochloric acid recovers one washed copper from one raw copper using 250 mB acid. Larger metal and polymer routes remain future work.',
       'Drain ordinary process tanks before moving a reactor; only the dedicated acid charge travels with its dropped item. These are fictional game quantities, not real manufacturing instructions.']),
      ('technology','Technology: choose your workshop','jugcraft:metal_press',[
       'Start with tools, obtainable metals and the pre-electric workshop. Ceramics, construction materials and mechanical power provide useful sideways paths. Steel and electricity are parallel capabilities, not a requirement to complete every craft.',
       'Power -> processing -> useful products: build generation, route cables, add storage, then connect the press, wire and circuit equipment. Follow actual recipes in Getting Started, Power and Processing; trade is always an alternative source of required materials.',
       'Choose oil, chemistry, agriculture, logistics, electronics or rocketry when their products help your project. The Routes view suggests connections. First electricity has a 2–4 active-hour design target, not a measured guarantee or a timer.']),
      ('magic','Magic: begin with discovery','minecraft:amethyst_shard',[
       'Find amethyst, glow lichen or glow berries. Sneak-use a luminous specimen to examine it; observing its glow needs darkness. Follow First Light, then study at the Lampwright\'s Bench and build an instrument.',
       'Research progresses through Encountered, Observed, Understood and Mastered. The server records your discoveries. Reading the guide does not unlock spells, and trading for supplies does not replace personal observations.',
       'Explore useful traditions: light and Ember, cultivation, alchemy, runes, ritual circles, binding, celestial work and the other existing Concordance practices. The detailed Magic pages reuse the codex\'s authored guidance and research conditions.',
       'Schools are at different stages of completion. Full Ice/Storm/Earth expansions, the full Ars Goetia pact system and interplanetary travel must not be inferred from materials or equipment already present. Consult the planned-content page.']),
      ('planned','Future content — not implemented here','minecraft:map',[
       'Stolas and the shared 72-spirit pact expansion remain planned. Full moon/planet travel remains planned. More magic school content, industrial metals/polymers, advanced chips and larger factory forms remain separate delivery work.',
       'The Encyclopedia itself does not add these systems. Existing entries document playable content; route arrows are suggestions unless an entry names an actual material or research prerequisite.'])]
    overviews.insert(2,('biogas','Farm waste to power: the digester factory','jugcraft:anaerobic_digester',[
        'Build the six-block-wide Anaerobic Digester, Biogas Cleaner, Digestate Separator and Bio-Generator. Steel, tanks, a motor and a basic circuit provide a reachable entry; no methane or digestate is needed to craft them. An existing generator or charged battery starts the line.',
        '16 farm items + 1000 mB water become 1000 mB raw 60/40 methane/CO2 biogas and 750 mB digestate. Clean the gas with 250 mB water; combine its 250 mB wash digestate with the main residue. The separator recovers 750 mB water and one fiber. Eight fibers craft Fertilizer. Collect every outlet: full tanks stop the line.',
        'Burn cleaned biogas in the Bio-Generator at 64 JE/t. Existing bioethanol also works. Reuse the Chemical Infuser and nickel bed to upgrade biogas with hydrogen into pure methane; collect its water or use its existing excess-water drain. Pure methane feeds the higher-output Gas Burning Generator.',
        'Existing pipes, gas cylinders, tanks, cables and conveyors handle the line. Search a machine name for exact port locations and recipes. This guide records game balance, not real industrial procedures.']))
    overviews.insert(2,('biorefinery','Crops to fuel and bauxite to aluminum','jugcraft:industrial_fermenter',[
        'Fuel route: Heated Mashing Station -> Industrial Fermenter -> Chemical Distillation Station -> existing Bio-Generator or compatible engines. Stillage feeds the Anaerobic Digester. Optional wet CO2 cleanup in the Biogas Cleaner supplies methane synthesis; fermentation keeps running with excess gas venting unless you select Keep gas.',
        'Aluminum route: Bauxite Digestion Vessel -> Aluminate Recovery Press -> Alumina Calciner -> Aluminum Reduction Cell. Recover 200 of 250 mB lye per bauxite, return the press and calciner water, and bind residue into game construction filler. Each bauxite still yields two ingots; electrolysis consumes coke. Copper and steel build the first machines.',
        'Search each machine for precise recipes and visible port positions. The earlier reactor recipes and aluminum consumers remain compatible. Fuel dehydration, larger plants and new aluminum alloys/products are later expansions.']))
    overviews.insert(2,('polymer_factory','From oil or crops to useful polymer parts','jugcraft:polymer_extruder',[
        'Monomer Preparation Unit: refinery gas or renewable bioethanol -> ethylene. Add chlorine for dichloride; crack it into vinyl chloride and recovered hydrogen chloride for existing acid absorption.',
        'Polymerization Kettle selects HDPE and LDPE with reusable inserts. Existing PVC and rubber supplies also work. The Molding Press keeps six molds; HDPE joins compatible rigid stock. The Extruder uses five reusable dies for sheets, film, profiles, hoses and insulation.',
        'Clean Polymer Reclaimer returns only matching stock from grade-marked offcuts or identified clean film/profiles, with paid work and losses. Both extrusion drawers must fit. Parts feed existing cables, valves, casings and plastic building blocks. For PTFE, follow the fluorochemistry guide: separate specialist powder, shaping and sintering.']))
    overviews.insert(2,('coastal_metals','Coastal magnesium, titanium and molded parts','jugcraft:magnesium_electrolysis_cell',[
        'Coastal Seawater Intake -> Brine Concentrator -> Mineral Conditioning Station -> Chloride Drying Oven -> Magnesium Electrolysis Cell. Intake water must be at sea level in a beach/ocean area beside an ocean, not just beneath a coastal machine. Lye and hydrochloric acid come from the existing starter factory.',
        'Raw Titanium + coke + chlorine -> Titanium Chlorination Vessel -> existing Chemical Distillation Station -> Titanium Reduction Retort with magnesium -> existing Arc Furnace. Re-electrolyze the returned dry salt to recover magnesium and chlorine with paid power. Treat spent mineral wash in the Conditioner.',
        'The Polymer Molding Press uses six earlier-steel reusable molds. Rigid pellets/PVC supply housings and panels, PVC supplies fittings, and rubber supplies insulation/gaskets/hoses. Molded products have additional construction recipes; old routes remain useful. Search each machine for exact recipes and locations.']))
    overviews.insert(2,('fluorochemistry','Snowy fluorite to finished PTFE','jugcraft:ptfe_sintering_oven',[
        'Fluorite veins occur only in snowy biomes, with deeper deposits and occasional high mountain exposures. Wash fluorite in the Mineral Conditioner. Concentrate starter sulfuric acid in the Acid Concentrator, then use the Chemical Dissolution Chamber to make hydrogen fluoride and calcium sulfate.',
        'Methane + chlorine -> chloroform in the Fluorocarbon Reactor. Chloroform + hydrogen fluoride -> chlorodifluoromethane. The Fluoromonomer Cracker makes TFE. Recover hydrogen chloride for existing acid absorption; every product outlet must have space.',
        'Use the PTFE insert in the Polymerization Kettle to turn TFE into powder. Press gasket/fitting/panel blanks, then sinter them. Alternatively prepare PTFE paste in the Conditioner and extrude liner blanks with a sheet die. Finished seals, fittings and liners build the larger two-lane Lined Fluorochemical Reactor. Only clean unsintered offcuts can be reclaimed.']))
    for key,title,icon,text in reversed(overviews):
        entries.insert(0,dict(id='guide/'+key,group='Home',chapter='Start here',gate='',page=dict(title=title,icon=icon,text=text)))
    return dict(version=1,entries=entries)

def write_content(write,assets,data,lang):
    lang['key.jugcraft.encyclopedia']='Open Jugcraft Encyclopedia'
    lang['screen.jugcraft.encyclopedia']='Jugcraft Encyclopedia'
    lang['screen.jugcraft.encyclopedia.search']='Search the Encyclopedia'
    lang['screen.jugcraft.encyclopedia.button']='Jugcraft guide'
    book=build();goals=[]
    acquire={"chemical_infuser":"Chemical Infuser", "chemical_oxidizer":"Chemical Oxidizer", "coal_gasifier":"Coal Gasifier", "gas_cleaning_rack":"Gas Cleaning and Separation Rack", "hydrochloric_acid_bucket":"Hydrochloric Acid"}
    import anaerobic
    import biorefinery
    import fluorochemistry
    acquire.update({name:info["display"] for name,info in fluorochemistry.FORMS.items()})
    import polymers
    acquire.update({name:info["display"] for name,info in polymers.FORMS.items()})
    import coastal_metals
    acquire.update({name:info["display"] for name,info in coastal_metals.FORMS.items()})
    acquire.update({name:info['display'] for name,info in biorefinery.FORMS.items()})
    acquire.update({name:info['display'] for name,info in anaerobic.FORMS.items()})
    for item,title in acquire.items():
        lang[f'advancements.jugcraft.encyclopedia_{item}.title']=title
        lang[f'advancements.jugcraft.encyclopedia_{item}.description']='Obtain '+title+' by crafting, trade or collection.'
        write(data/'jugcraft/advancement'/f'encyclopedia_{item}.json',dict(parent='jugcraft:root',
            display=dict(icon=dict(id='jugcraft:'+item),title=dict(translate=f'advancements.jugcraft.encyclopedia_{item}.title'),description=dict(translate=f'advancements.jugcraft.encyclopedia_{item}.description'),frame='task',show_toast=False,announce_to_chat=False,hidden=True),
            criteria=dict(obtain=dict(trigger='minecraft:inventory_changed',conditions=dict(items=[dict(items=['jugcraft:'+item])])))))
    # Existing server advancements remain the sole authority and save format. No new reward economy.
    chosen=['copper_ingot','tin_ingot','bronze_ingot','steel_ingot','coal_generator','battery_box','metal_press','wire_drawer','circuit_assembler','chemical_reactor','sulfuric_acid','fertilizer','electrolytic_separator','gas_burning_generator','chemical_infuser','chemical_oxidizer','coal_gasifier','gas_cleaning_rack','bioethanol','hydroponic_bay','rocket_workshop']
    chosen += ['encyclopedia_'+item for item in acquire]
    chosen += [concordance.research_advancement(r,s) for r,info in concordance.RESEARCH.items() for s in concordance.RESEARCH_STAGES if s in info['states']]
    import json
    for key in chosen:
        p=data/'jugcraft'/'advancement'/f'{key}.json'
        if not p.exists():continue
        a=json.loads(p.read_text());d=a.get('display',{})
        def text(v):return lang.get(v.get('translate',''),v.get('text','')) if isinstance(v,dict) else str(v)
        title=text(d.get('title',key));description=text(d.get('description',''))
        goals.append(dict(id='jugcraft:'+key,title=title,description=description,icon=d.get('icon',{}).get('id','minecraft:book'),group='Magic' if key.startswith('concordance') else 'Technology'))
    book['goals']=goals
    write(assets/'encyclopedia/en_us.json',book)
    write(data/'jugcraft/encyclopedia/quests.json',dict(advancements=sorted(set([g['id'] for g in goals]+[e['gate'] for e in book['entries'] if e['gate']]))))
