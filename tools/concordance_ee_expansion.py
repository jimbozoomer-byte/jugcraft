"""Connected alchemy behavior and remaining owner UI/state art. No new item registry or currency."""
GUI = ('alchchest','book_texture','collector1','collector2','collector3','condenser','condenser_mk2','eternal_density','mercurial_eye','nei','portaldevice')
ACTIVE = {'gem_of_eternal_density':'dense_gem_on','void_ring':'rings/void_on','mind_stone':'rings/mind_stone_on'}
OWNER_FILES = {**{f'textures/gui/alchemy/{name}.png':f'textures/gui/{name}.png' for name in GUI},
               **{f'textures/item/alchemy/{name}_active.png':f'textures/item/{source}.png' for name,source in ACTIVE.items()}}
TOOLTIPS = {
 'philosophers_stone':'Use: essence tablet. Sneak-use air: crafting. Use terrain: transform. Sneak-use terrain: cycle 1x1 / 3x3 / 5x5. Costs at least 8 essence per block.',
 'gem_of_eternal_density':'Use to set nine allowed material identities, output tier and automation. Runs in inventory or a carried bag/chest. Empty filters consume nothing.',
 'void_ring':'Use: confirm dissolving the other-hand material. Sneak-use: configure filtered automatic dissolution. Empty filters consume nothing.',
 'mind_stone':'Use stores 100 XP; sneak-use retrieves 100. On a pedestal, collects nearby XP into this physical stone. The lit icon indicates stored XP.',
 'transmutation_tablet':'Dissolve materials into essence; open Learned patterns to learn, search, favorite and form items without retaining a sample.',
 'mercurial_eye':'Sneak-use or use in air: choose a learned building material. Use a surface: build a 3x3 face for formation price + 8 essence per placed block. Without a selected pattern, uses offhand blocks.',
 'dark_matter_pedestal':'Twelve artifacts: repair, support, grow/harvest, repel, gather into adjacent storage, defend, collect XP, or accelerate adjacent furnaces/condensers. Uses its buffer.',
 'alchemical_chest':'54 party-access slots with an opening lid. Activated artifacts can repair, consolidate and collect matching drops while the chest is closed. Redstone pauses.',
 'repair_talisman':'Activated while held/worn: repairs equipment. Activated inside a carried bag or owned chest: repairs stored gear. 1 essence per point, up to 4 each second.',
 'black_hole_band':'Activated while held/worn: attracts drops. Inside storage: collects matching stored item types. On a pedestal: inserts nearby drops into adjacent inventories.',
}
for i in range(1,7):TOOLTIPS[f'klein_star_{i}']='Stores essence. Equipment spends your wallet first, then carried stars in inventory order. Use to store/withdraw; nested stars are not accessed.'
for i in range(1,4):TOOLTIPS[f'collector_mk{i}']='Collects daylight essence. Charge mode exports to neighbors/stars. Fuel mode upgrades coal to a chosen final alchemical fuel; inputs above/sides, output below.'
LANG={
 'key.jugcraft.gem_abilities':'Gem armor abilities',
 'message.jugcraft.stone.charge':'Stone area: %s x %s',
 'screen.jugcraft.alchemy.patterns':'Patterns',
 'screen.jugcraft.alchemy.gem_controls':'Gem armor',
 **{f'screen.jugcraft.pattern.{k}':v for k,v in {
 'title':'Learned transmutation','build':'Mercurial building palette','search':'Search patterns...',
 'learn':'Learn','make':'Form one','choose':'Use pattern','previous':'<','next':'>','favorite':'Favorite',
 'unfavorite':'Unfavorite','favorites':'Favorites','all':'All patterns','balance':'Essence: %s','sample':'Sample','target':'Target','preview':'Preview',
 'output':'Output','cost':'Price per item: %s','page':'%s / %s','learn_help':'Learn once; keep sample','area':'3 x 3 / up to nine blocks',
 'make_help':'Forms one learned item for the displayed price. Only your actual learned, eligible materials are available.',
 'build_help':'Select a learned building block. Use the eye on a surface to place up to nine blocks, each at the displayed price. Protected or occupied positions are skipped.',
 }.items()},
 **{f'screen.jugcraft.automation.{k}':v for k,v in {
 'start':'Start automatic','stop':'Stop automatic','target_0':'Output: Iron','target_1':'Output: Gold','target_2':'Output: Diamond',
 'clear':'Clear filters','filters':'Allowed input materials','hint':'Copy a held stack into a filter.',
 'help':'Only these material types may be consumed. Click a filter with an empty cursor to remove it. Your stack is kept. Modified items and hotbar equipment are excluded.',
 'output_help':'Forms at most one output per pulse at its normal price, using your shared essence balance. Existing essence may be spent. A full output stops input consumption.',
 }.items()},
 **{f'screen.jugcraft.gem.{k}':v for k,v in {
 'title':'Gem armor abilities','action_0':'Helmet: toggle night vision','action_1':'Chest: repel nearby enemies','action_2':'Leggings: forward dash','action_3':'Boots: toggle flight',
 'help_0':'Wear an enabled Gem helmet. Night vision costs 4 essence each renewal. Toggle again to stop renewing it.',
 'help_1':'Wear an enabled Gem chestplate. Repels nearby hostile enemies for 16 essence each.',
 'help_2':'Wear enabled Gem leggings and stand on solid ground. Dash forward for 64 essence; blocked paths are refused.',
 'help_3':'Wear enabled Gem boots. Flight costs 16 essence per second. Double-jump to fly once enabled; toggle again to turn it off.',
 }.items()},
 **{f'screen.jugcraft.workshop.fuel_{i}':v for i,v in enumerate(('Mode: Charge','Stop: Alchemical Coal','Stop: Mobius Fuel','Stop: Aeternalis Fuel'))},
 'screen.jugcraft.workshop.fuel_input':'Fuel to upgrade',
 'screen.jugcraft.workshop.fuel_help':'One fuel step per second: coal -> Alchemical (1,024 essence), -> Mobius (4,096), -> Aeternalis (16,384). Target fuel is never upgraded further. Charge mode exports essence instead.',
}
def tags(tags): pass
def write_all(write,assets,data,lang,condition,self_drop):
    lang.update(LANG)
    for name,text in TOOLTIPS.items():lang[f'tooltip.jugcraft.{name}']=text
    for name in ACTIVE:
        write(assets/f'models/item/{name}_active.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'jugcraft:item/alchemy/{name}_active'}})
        write(assets/f'items/{name}.json',{'model':{'type':'minecraft:condition','property':'minecraft:has_component','component':'jugcraft:alchemy_active',
              'on_false':{'type':'minecraft:model','model':f'jugcraft:item/{name}'},'on_true':{'type':'minecraft:model','model':f'jugcraft:item/{name}_active'}}})
    # Actual player help is replaced together with mechanics; older feature records remain historical evidence.
    bodies=[
     'Understand Assay. The tablet, table and stone share essence. Dissolving learns eligible items. Open Learned patterns, insert a sample and Learn to keep it permanently. Samples are retained.',
     'Dissolving rounds down; forming costs 125%, rounded up. Learned patterns persist through death and reconnect. Search or favorite them. Modified, stored and excluded items remain protected.',
     'Collector Charge mode supplies neighbors and stars. Fuel mode upgrades coal to the selected stopping tier for 1,024 / 4,096 / 16,384 essence per step. Furnaces and condensers accept one Watch bonus each second.',
     'Bags have independent 54-slot inventories. Active repair talismans repair stored gear. Black Hole Bands collect drops matching a stored item type. Open storage pauses its automation. No nested containers.',
     'Use Density Gem or sneak-use Void Ring to configure allowed materials, then Start automatic. Empty filters do nothing. Converters never consume modified items; loose converters skip your hotbar. Density forms iron, gold or diamond.',
     'Mind Stone stores actual XP and collects orbs on a pedestal. Twelve pedestal artifacts support farming, defense and workshops. Open Gem abilities for armor powers. Carried stars power equipment after your wallet empties.',
    ]
    for i,text in enumerate(bodies):lang[f'screen.jugcraft.alchemy_storage.manual.body_{i}']=text
def codex():
    return {('equivalence','connected_alchemy'):{'name':'Connected Alchemy','x':18,'y':0,'icon':'jugcraft:transmutation_tablet','condition':None,'description':'Learn patterns; automate a living workshop',
        'pages':[
         ('text','Learn Once','The tablet’s **Learned patterns** button opens your saved collection. Learning retains the sample. Dissolving also learns eligible materials. Browse icons, search, favorite, and **Form one** at the normal price without carrying a sample. Knowledge is personal and survives death. Server catalogue changes still control eligibility.'),
         ('text','Automatic Storage','Configure a **Density Gem** or sneak-use a **Void Ring**. Copy up to nine allowed input types into its filter and start it. Only plain matching material is consumed. Density forms its selected iron/gold/diamond target; Void deposits essence. A carried bag or owned chest also supports active **Repair Talisman** and **Black Hole Band**. Seed storage with a desired item to filter pickup. Storage menus pause their own background writer.'),
         ('text','Collector Fuels','Choose **Charge**, **Alchemical Coal**, **Mobius Fuel** or **Aeternalis Fuel** in a collector. Each step consumes one previous fuel and **1,024 / 4,096 / 16,384 essence**. Intermediate fuels remain in output and can continue until the chosen stop. Clear output space is required. Charge mode retains the existing neighbor/star export behavior.'),
         ('text','Pedestal Workshop','Original six effects remain. **Harvest Goddess now replants ripe crops**. **Black Hole Band** transfers drops into adjacent inventories. **Mind Stone** stores nearby XP, preserving merged orb counts. **Archangel**, **Ignition**, **Zero** defend against monsters. **Watch** gives adjacent furnaces/condensers one extra paid work pulse per second, shared across all Watches. All effects use the pedestal buffer and owner authority.'),
         ('text','Stone and Eye','Stone: use terrain to transmute; sneak-use terrain changes area (1x1, 3x3, 5x5); sneak-use air opens crafting. Each changed block costs at least 8 essence. Eye: open its learned building palette, select a block, then use a surface for a **3x3 face**. Each placed block costs formation price plus 8. Claims, collision, reach and loaded terrain apply.'),
         ('text','Stars and Gem Armor','Equipment spends your **wallet first**, then carried Klein Stars. No nested-star access. Gem controls are in the tablet and Manual, with an optional Controls keybind. Helmet toggles night vision; chest repels enemies; leggings dash; boots toggle paid flight. Flight shares the existing lease and ends on removal or insufficient power. Ordinary wards remain.'),
        ]}}
