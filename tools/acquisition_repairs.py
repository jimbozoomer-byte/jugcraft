"""Owner-approved native acquisition repairs; keep source archives unchanged."""
from newest_building import RES, write, read
from menagerie_data import loot

def generate(writer=None):
    writer = writer or globals()['write']
    write = writer
    recipes=RES/'data/jugcraft/recipe/acquisition'
    write(recipes/'present_conversion.json', {'type':'jugcraft:supps_present_conversion'})
    # Native alternatives for decorations whose original external ingredients are absent.
    variants={'candle_holder_cupric':('candle_holder','minecraft:copper_ingot'),
              'candle_holder_ender':('candle_holder','minecraft:ender_pearl'),
              'candle_holder_soul':('candle_holder','minecraft:soul_soil'),
              'candle_holder_spectacle':('candle_holder','minecraft:amethyst_shard'),
              'sconce_ender':('sconce','minecraft:ender_pearl'),
              'sconce_glow':('sconce','minecraft:glow_ink_sac'),
              'sconce_green':('sconce','minecraft:green_dye'),
              'sconce_nether_brass':('sconce','minecraft:blaze_powder')}
    for name,(base,material) in variants.items():
        write(recipes/(name+'.json'),{'type':'minecraft:crafting_shapeless','ingredients':['jugcraft:supps_'+base,material], 'result':{'id':'jugcraft:supps_'+name,'count':1}})
    write(recipes/'divine_pact.json',{'type':'minecraft:crafting_shaped','pattern':[' P ','CNC',' D '], 'key':{'P':'minecraft:paper','C':'jugcraft:forbidden_arcane_crystal','N':'minecraft:nether_star','D':'minecraft:dragon_breath'},'result':{'id':'jugcraft:forbidden_divine_pact','count':1}})
    silk={'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}
    write(RES/'data/jugcraft/loot_table/blocks/supps_ashen_basalt.json',loot({'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':'jugcraft:supps_ashen_basalt','conditions':[silk]},{'type':'minecraft:item','name':'minecraft:basalt'}]}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]}))
    from native_acquisition_completion import generate as complete
    complete(writer)

if __name__=='__main__':generate()
