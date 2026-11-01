# Native acquisition repairs

Implemented with OpenAI Codex (GPT-6), at the owner's request to fix the six example acquisition issues identified in the companion PR review. This is not a blanket fix or exclusion for every unresolved upstream item.

- Livingwood and Dreamwood logs use the existing native axe-stripping block, retaining log axis and normal tool wear. This unlocks their existing derived recipes.
- All four supplied Terrasteel armor upgrades retain their shaped ingredients and preserve equipment components. The Water Rod retains its supplied shape and accepts only water potions.
- One present plus one dye changes its color; one ordinary present plus a tripwire hook makes the matching trapped present. Both preserve components, including stored contents and custom names, and reject extra inventories/materials.
- Eight otherwise orphaned compatibility decorations receive explicit native conversions: a base candle holder plus copper, ender pearl, soul soil or amethyst creates Cupric, Ender, Soul or Spectacle variants; a base sconce plus ender pearl, glow ink, green dye or blaze powder creates Ender, Glow, Green or Nether Brass variants. These are native design choices, not claims that the absent external recipes were restored unchanged.
- Ashen Basalt formed by falling ash can be collected with Silk Touch; ordinary breaking retains the basalt drop.
- The owner approved a new boss-gated Divine Pact recipe: one paper, two Arcane Crystals, one Nether Star and one Dragon Breath. This supplies the missing prerequisite for the existing Soul Binding Crystal ritual; the ritual itself is unchanged.

Original collection files are preserved. Source-shaped recipes come from `art/owner-library/originals/Edited-2026-10-29/Jugtania/data/botania/recipes/`. Present conversion behavior follows the supplied SUPPS recipe definitions. Native alternatives and the boss-gated recipe live in `tools/acquisition_repairs.py`, invoked by the canonical Supplementaries generator. No textures or external dependencies are added.

Acquisition auditing now follows the implemented custom shaped recipes, paid present conversions, native log stripping and Silk Touch collection. It retains checks for the other unresolved items, including the compatibility rods. No missing-item assertion is suppressed.

Validation: common Java compilation and 14 focused Python acquisition/generation and recipe-station regressions passed. All four native `AcquisitionRepairGameTests` passed in 1.301 seconds: present contents/name conservation and duplicate-input rejection; armor damage/name preservation and water-versus-poison matching; native stripping targets; all nine new ordinary recipe registrations; and normal versus Silk Touch basalt loot. The test build succeeded. No live server restart or deployment is included.

The refreshed native-registry acquisition audit reports 78 missing routes and 6 blocked chains (84 total), down from 146, and zero invalid ingredients. The other unresolved content remains visible; the all-items assertion is not claimed to pass.

Validation used `runGameTest -PciServerClasses=AcquisitionRepairGameTests` with the local scoped registry init script. Resource copying completed, but the first build was stopped during expensive resource hashing; prepared runtime resources were reused with `-x processResources`. Local Java/Kotlin compiler inputs excluded the resource directory, while runtime retained it. A test-fixture reference to the removed `Items.RED_DYE` constant was corrected to the native registry lookup before the passing run. No unrelated gameplay tests ran.
