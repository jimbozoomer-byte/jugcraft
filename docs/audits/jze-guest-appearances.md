# JZE town guests: appearance audit

Worker C, 11 November 2026. Read-only audit for the town-guest roster
([GuestRoster.java](../../src/main/java/io/github/jimbozoomer/jugcraft/town/guests/GuestRoster.java)); the
placeholders are set in
[GuestAppearances.java](../../src/main/java/io/github/jimbozoomer/jugcraft/town/guests/GuestAppearances.java).

## Findings

- No skin, texture or model for any roster entry exists in the repository. The townsfolk textures
  (`textures/entity/townsfolk/`, drawn by `tools/town_skins.py`) are original town cast skins, and custom NPC player
  skins (`NpcSkins`) are looked up by Minecraft account name, which is not used for guests.
- Codex searched the 48,060 supplied asset filenames for the roster's name tokens and found no match. That is
  filename evidence only; it does not show that no usable art exists elsewhere.
- No named skin assets were supplied. Every likeness is therefore pending reference art.

## Policy

- Each available guest wears a fixed placeholder from the town's civilian skins (`CustomNpcs.townSkins()`). Guard
  uniforms and the mayor, priest and banker are left out so that a guest is not mistaken for a guard or an official.
- Every appearance is `finalArt=false`. The guest menu and `/guests roster` say "placeholder appearance, reference
  pending". A placeholder is never presented as the character.
- Pending entries have no appearance and cannot be placed.

## Per guest

| Id | Asset status | Placeholder (town skin) | Pending |
| --- | --- | --- | --- |
| emiru | none found | townsfolk_1 | reference art |
| lauren_chen | none found | townsfolk_2 | reference art |
| brittany_venti | none found | townsfolk_3 | reference art |
| aoc | none found | townsfolk_4 | reference art |
| sarah_stock | none found | townsfolk_5 | reference art; optional non-gory zombie wedding dress (art) |
| pokimane | none found | florist | reference art |
| rooni | none found | none (cannot be placed) | identity and reference |
| power | none found | baker | reference art |
| makima | none found | innkeeper | reference art |
| reze | none found | shopkeeper_green | reference art |
| asa_mitaka | none found | shopkeeper_brown | reference art |
| himeno | none found | decorator_green | reference art |
| tokai_teio | none found | decorator_orange | reference art |
| agnes_tachyon | none found | townsfolk_1 | reference art |
| gold_ship | none found | townsfolk_2 | reference art |
| oguri_cap | none found | townsfolk_3 | reference art |
| special_week | none found | townsfolk_4 | reference art |
| rei_ayanami | none found | townsfolk_5 | reference art |
| asuka_langley | none found | florist | reference art |
| misato_katsuragi | none found | baker | reference art |
| frieren | none found | innkeeper | reference art |
| rem | none found | shopkeeper_green | reference art |
| megumin | none found | shopkeeper_brown | reference art |
| zero_two | none found | decorator_green | reference art |
| rin_ishtar | none found | decorator_orange | reference art; the owner chooses Rin Tohsaka or Ishtar |
| tifa_lockhart | none found | townsfolk_1 | reference art |
| yorha_2b | none found | townsfolk_2 | reference art |
| chun_li | none found | townsfolk_3 | reference art |
| ada_wong | none found | townsfolk_4 | reference art |
| lara_croft | none found | townsfolk_5 | reference art |
| kafka | none found | florist | reference art |
| miyabi | none found | baker | reference art |
| remielle | none found | none (cannot be placed) | identity and reference |
| nicole_demara | none found | innkeeper | reference art |
| guest_unnamed | none found | none (cannot be placed) | identity and reference |
| jakeey_kpop | none found | none (cannot be placed) | Jakeey's picks: identities and references |
