"""Existing native Supplementaries growth, rewards and special crafting.

These are audit edges, not new recipes. Ordinary vanilla survival terrain and
encounters are available; season, randomness and exact enchantment components
remain documented conditions rather than simulated mechanics.
"""

JAVA = 'src/main/java/io/github/jimbozoomer/jugcraft/menagerie/'


def extend(root, edge, evidence, items):
    def item(name):
        return 'jugcraft:supps_' + name

    def found(name, source, note):
        name = item(name)
        if name in items:
            evidence[name].append({'kind': 'native_acquisition', 'source': JAVA + source, 'note': note})

    def route(source, kind, needs, output):
        edge(JAVA + source, kind, needs, [item(output)])

    for color in 'white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split():
        route('OwnerPresentRecipe.java', 'dye_present', [item('present'), 'minecraft:' + color + '_dye'], 'present_' + color)
        route('OwnerPresentRecipe.java', 'trap_present', [item('present_' + color), 'minecraft:tripwire_hook'], 'trapped_present_' + color)
    route('OwnerSupplementTerrain.java', 'ash_basalt_silk_touch', ['minecraft:basalt', item('ash'), '#minecraft:pickaxes', 'minecraft:enchanted_book'], 'ashen_basalt')
    found('wild_flax' , 'OwnerSupplementFlora.java',
          'The registered wild-flax feature places plants on sandy riverbanks in its biome tag; Silk Touch retains the wild plant. OwnerWildFlax also permits bounded bonemeal spreading.')
    found('flax_seeds', 'OwnerSupplementFlora.java',
          'Breaking naturally generated wild flax without a preserving tool drops flax seeds through blocks/supps_wild_flax; these seeds plant OwnerSupplementFlax.')
    route('OwnerSupplementFlax.java', 'plant_grow_and_harvest_mature_flax_lower_half',
          [item('flax_seeds'), 'minecraft:dirt', '#minecraft:hoes'], 'flax')

    found('barnacles', 'OwnerBarnaclePatch.java',
          'Registered ocean and beach features grow barnacles on submerged supports. Their native block loot returns the item only with Silk Touch.')
    for name, note in (
            ('music_disc_heave_ho', 'Six-percent bonus pool in vanilla shipwreck treasure chests.'),
            ('blast_armor_trim_smithing_template', 'Fifteen-percent bonus pool in vanilla desert pyramid chests.'),
            ('dragon_banner_pattern', 'Ten-percent bonus pool in vanilla End City treasure chests.')):
        found(name, 'OwnerSupplementTreasures.java', note)
    for name, victim in (('enderman_head', 'Enderman'), ('spider_head', 'Spider or Cave Spider')):
        found(name, 'OwnerMobHeads.java',
              'A ' + victim + ' killed by a charged creeper has this item added to the vanilla charged-creeper loot table.')

    for name in ('globe', 'globe_sepia'):
        route('OwnerGlobes.java', 'wandering_trader_emerald_purchase', ['minecraft:emerald'], name)
    route('OwnerGlobes.java', 'apply_antique_ink_to_globe_then_collect',
          [item('globe'), item('antique_ink')], 'globe_sepia')
    route('OwnerGlobes.java', 'wash_sepia_globe_with_soap_then_collect',
          [item('globe_sepia'), item('soap')], 'globe')

    # Both serializers consume actual ingredients; neither is a free conversion
    # merely because its recipe JSON has no ordinary result/ingredient fields.
    route('OwnerRopeArrow.java', 'craft_arrow_with_one_or_more_paid_ropes',
          ['minecraft:arrow', item('rope')], 'rope_arrow')
    # A stick is one concrete permitted hidden item: no inventories, block entity
    # payloads, or recursively nested suspicious blocks are accepted by Hide.
    route('OwnerHiddenBricks.java', 'craft_gravel_bricks_with_hidden_paid_item',
          [item('gravel_bricks'), 'minecraft:stick'], 'suspicious_gravel_bricks')
    route('OwnerSupplementaries.java', 'hoe_gravel_then_collect_with_silk_touch',
          ['minecraft:gravel', '#minecraft:hoes', 'minecraft:enchanted_book'], 'raked_gravel')

    # Compatibility rods still have no implemented conversion.
    return {}
