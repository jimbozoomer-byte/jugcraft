# JZE guest appearances — November 11 local implementation

The earlier filename audit found no supplied named-character skins among 48,060 asset filenames. This pass authors new Minecraft-style interpretations in the existing editable `tools/town_skins.py` UV system, through `tools/guest_skins.py`. It does not download or overwrite another creator's skin or any owner-library original.

## Implemented

32 separate 64×64 classic-player skins; opaque base faces, transparent hair/clothing layers, restrained palettes and single-row eyes. The existing town humanoid provides movement. Small closed model accessories add Uma/Miyabi ears, Power/Zero Two horns, Frieren ears and Megumin's stepped hat. These are stylized interpretations, not exact portraits or owner-approved final likenesses. Real-person clothing is a representative everyday outfit, not a claim about that person's identity, beliefs or endorsement. Sarah's nongraphic zombie bridal variant is the owner's requested fictional costume.

| Guest | Runtime texture | Hair / outfit treatment |
| --- | --- | --- |
| emiru | `guest_emiru.png` | long hair; hoodie details |
| lauren_chen | `guest_lauren_chen.png` | long hair; blazer details |
| brittany_venti | `guest_brittany_venti.png` | curls hair; blazer details |
| aoc | `guest_aoc.png` | long hair; blazer details |
| sarah_stock | `guest_sarah_stock.png` | long hair; bride details |
| pokimane | `guest_pokimane.png` | curls hair; hoodie details |
| power | `guest_power.png` | long hair; tie details |
| makima | `guest_makima.png` | braid hair; tie details |
| reze | `guest_reze.png` | bob hair; tie details |
| asa_mitaka | `guest_asa_mitaka.png` | long hair; uniform details |
| himeno | `guest_himeno.png` | bob hair; suit details |
| tokai_teio | `guest_tokai_teio.png` | long hair; race details |
| agnes_tachyon | `guest_agnes_tachyon.png` | bob hair; lab details |
| gold_ship | `guest_gold_ship.png` | long hair; race details |
| oguri_cap | `guest_oguri_cap.png` | long hair; race details |
| special_week | `guest_special_week.png` | long hair; race details |
| rei_ayanami | `guest_rei_ayanami.png` | bob hair; uniform details |
| asuka_langley | `guest_asuka_langley.png` | long hair; uniform details |
| misato_katsuragi | `guest_misato_katsuragi.png` | long hair; jacket details |
| frieren | `guest_frieren.png` | long hair; mage details |
| rem | `guest_rem.png` | bob hair; maid details |
| megumin | `guest_megumin.png` | bob hair; mage details |
| zero_two | `guest_zero_two.png` | long hair; military details |
| rin_ishtar | `guest_rin_ishtar.png` | long hair; cross details |
| tifa_lockhart | `guest_tifa_lockhart.png` | long hair; fighter details |
| yorha_2b | `guest_yorha_2b.png` | bob hair; android details |
| chun_li | `guest_chun_li.png` | bun hair; qipao details |
| ada_wong | `guest_ada_wong.png` | bob hair; agent details |
| lara_croft | `guest_lara_croft.png` | braid hair; explorer details |
| kafka | `guest_kafka.png` | long hair; jacket details |
| miyabi | `guest_miyabi.png` | long hair; military details |
| nicole_demara | `guest_nicole_demara.png` | long hair; jacket details |

Textures live in `src/main/resources/assets/jugcraft/textures/entity/townsfolk/`. Rebuild with `python tools/guest_skins.py`. The generator also writes `build/jze-guest-skins.png`, an explicitly labelled front/back UV preview, not runtime evidence.

## Reference direction

Owner's named roster is the design brief. Character pages consulted for the series' designs include [Chainsaw Man](https://www.chainsawman.dog/movie_reze/character/), [Uma Musume](https://umamusume.jp/character), [Agnes Tachyon](https://umamusume.jp/character/agnestachyon/), [Frieren](https://frieren-anime.jp/character/), [KonoSuba](https://konosuba.com/1st/character/) and [Zenless Zone Zero](https://zenless.hoyoverse.com/en-us/character). No assets from those websites are bundled. Rin/Ishtar uses the Rin outfit for the shared identity; changing the outfit must not create a second identity.

## Remaining reference holds

Rooni, Remielle, the unnamed guest and Jakeey's K-pop picks remain unavailable until identified. An appearance/race description does not establish a person's identity. The owner has been asked for names or reference links. Detailed visual acceptance of all newly drawn skins remains a local playtest step.

## Integration

`CustomNpcs.townSkins()` accepts the packaged guest skins. Saved canonical guests wearing their exact historical development stand-in migrate on their ordinary service tick; different operator-selected town skins, named skins and pending/resolved player skins are preserved. Neither migration nor appearance assignment changes blocks or identity/affection/marriage records.

`/guests city preview` lists clear loaded lawn sites; `/guests city place` rechecks permissions and places only never-placed identities. Existing/missing canonical identities are not silently relocated or duplicated. Only creative operators in the Overworld can use city placement. It requires a three-by-three clear grass margin, avoids resident spots and nearby entities, loads no chunks and writes no blocks. Crowded or owner-edited sites are skipped; `/guests place` remains available for individually selected locations. No live city placement was performed.

Tool attribution: OpenAI Codex, code-authored pixel assets and local Java integration. No new Claude worker was used for this appearance pass.

![Front/back pixel-skin design review (accessory geometry is visible in the runtime screenshots)](../images/jze_guest_skins.png)
