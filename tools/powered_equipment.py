"""Owner-directed powered equipment descriptions, November 2, 2026."""
LANG = {
    'screen.jugcraft.charging_station.status.8': 'Make room for the recovered module.',
    'screen.jugcraft.charging_station.status.7': 'You need a matching module in your inventory.',
    'screen.jugcraft.charging_station.status.6': 'Discharge the item before removing capacity.',
    'screen.jugcraft.charging_station.status.5': 'Silk Touch and Fortune cannot be combined.',
    'screen.jugcraft.charging_station.status.4': 'This module is already at its limit.',
    'screen.jugcraft.charging_station.status.3': 'No compatible fitted module on this item.',
    'screen.jugcraft.charging_station.status.2': 'Module recovered.',
    'screen.jugcraft.charging_station.status.1': 'Module installed.',
    'screen.jugcraft.charging_station.status.0': 'Use + to fit a module and - to recover one.',
    'screen.jugcraft.charging_station.remove_capacity': 'Recover one %s from item %s. First discharge the item until its energy fits the smaller capacity.',
    'screen.jugcraft.charging_station.remove': 'Recover one fitted %s from item %s. Existing enchantments without a fitted module cannot be recovered.',
    'screen.jugcraft.charging_station.insert': 'Fit %s in item %s using a module from your cursor or inventory.',
    'screen.jugcraft.charging_station.empty': 'Place powered item',
    'screen.jugcraft.charging_station.buffer': 'Station: %s / %s JE | 512 JE/t shared',
    "item.jugcraft.leaf_blower.tooltip": "Hold use to blow; sneak to vacuum. Sprint+Sneak+use cycles strength; an offhand item filters collection.",
    "message.jugcraft.drill_mode.large": "5x5 area",
    "message.jugcraft.module.removed": "Recovered %s from %s",
    "message.jugcraft.module.too_charged": "Cannot remove %s from %s until charge fits its lower capacity",
    "tooltip.jugcraft.exosuit_helmet": "Night vision: 2 JE/t while active. Shift-use held helmet to toggle.",
    "tooltip.jugcraft.exosuit_chestplate": "Shield: 1,000 JE/point; thrust: 20 JE/t. Shield recovers after 3 seconds without damage. Shift-use held chestplate: adjust thrust.",
    "tooltip.jugcraft.exosuit_leggings": "+30% speed; 1 JE/t of boosted movement. No standby drain.",
    "tooltip.jugcraft.exosuit_boots": "Fall cushioning: 100 JE/heart prevented. Free full-block stepping while charged.",
    "tooltip.jugcraft.chainsaw": "Shift-use: tree / single-log / leaf-clearing mode. Sneak while cutting for one block.",
    "tooltip.jugcraft.module_fitting": "Open a charging station. Use + to fit a module from your inventory and - to recover it.",
    "tooltip.jugcraft.capacity_module": "Doubles capacity; at most two. Fit or remove in the charging station menu.",
    "tooltip.jugcraft.overclock_module": "+50% base mining speed and energy/block per level; at most two.",
    "screen.jugcraft.prospector.help": "Approximate regional signals. Distance is to a detected sample; advanced readings add a compass bearing.",
}
SCANNER_PAGES = [
 {"title":"Ore Scanner","icon":"jugcraft:jimta_ore_scanner","text":[
  "A powered upgrade in convenience over the Geo-Resonance Prospector: 250,000 JE, doubled by each of two Capacity Modules.",
  "Use for a loaded 3x3-chunk survey at 500 JE per completed scan. Distance bands describe the nearest detected sample, not an exact ore location. Shift-use to reopen the last saved survey for free.",
  "Use the survey screen filters, page controls and recent history. Coverage reports skipped terrain; no signal is not proof that no ore exists."]},
 {"title":"Advanced Ore Scanner","icon":"jugcraft:jimta_advanced_ore_scanner","text":[
  "The same battery capacity with better instrumentation: detailed 3x3 sampling costs 1,000 JE; wide 5x5 sampling costs 4,000 JE.",
  "Shift-use to switch mode. Detailed readings add eight-block distance bands, compass bearings and approximate depth bands. Distance refers to the nearest sampled resource.",
  "Sprint and shift-use to reopen the last saved survey without spending energy. Surveying unloaded terrain never generates or loads chunks."]}
]
