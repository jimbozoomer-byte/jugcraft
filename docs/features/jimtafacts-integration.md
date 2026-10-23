# Jimtafacts collection integration

Owner-approved scope, 23 October 2026: integrate all supplied Jimtafacts content into the single Jugcraft mod, retaining the edited assets and adapting platform-specific behavior. This is a multi-package port, not installation of the old binaries. AI assistance: OpenAI Codex (GPT-6).

## Source inventory and boundaries

The [verified collection](../../art/owner-library/catalog/edited-2026-10-23-jimtafacts/README.md) preserves 17,598 files, including 7,472 compiled classes. Artifacts, Applied Energentics and Computers classes use Java 25; the spellbook classes use Java 21; BuildCraft and IndustrialCraft contain much older Java 8/6 classes. Existing namespace labels remain in originals. No supplied executable code is run or added as a dependency. The source dependency JAR is not committed or shipped.

The branch starts from the planes/furniture/graves candidate `c459523ddac9fa1488c2802b140f41a4be20f441`. That preceding candidate and PR #461 remain independently testable. This collection has not been released, merged, uploaded to the live server or promoted to the client updater.

## Functional accessory slice

Stable IDs use `jugcraft:jimta_<source_name>` to avoid clashes with existing Concordance relics. The data and concise player descriptions live in `jugcraft/jimtafacts/accessories.json`; `tools/jimtafacts.py` is their source of truth.

- Health, attack, knockback, breaking speed, size, jump/fall and water-movement accessories use native attributes managed by Trinkets. Duplicate copies use the same modifier ID. There is no research gate; inventory-only and cosmetic copies give no abilities.
- Snorkel, goggles, invisibility scarf, antidote, panic necklace, obsidian skull and cross necklace use server-owned effects and cooldowns. Night vision naturally fades within 12 seconds after removal, preserving potion effects. Invisibility does not hide armor. Antidote removes poison/wither, not beneficial effects.
- Fire gauntlet, vampiric glove, withered bracelet and retaliation pendants act only after nonzero unblocked damage. Permission/PvP/party checks run before secondary effects; retaliation cannot recurse. Vampiric healing is capped at two health per second. The shock pendant uses direct damage, avoiding world-damaging lightning.
- Running shoes add sprint-only speed and step height, then remove their transient modifiers. Rooted boots require standing still on grass for ten seconds per food point. The sinking charm adds oxygen reserve and controlled downward water motion.
- Universal attractor scans at most 24 nearby drops every eight ticks, checks line of sight, pickup delay, reservation ownership and both endpoint claim permissions. It never opens containers or moves reserved loot.
- Everlasting Beef and Eternal Steak are reusable food with a one-minute item-type cooldown. Three vanilla cooking routes convert beef into steak with zero recipe XP.
- Existing native grave capture handles equipped items; personal/global keep-inventory exemptions remain authoritative.

Acquisition: a 20% chance of one item in new dungeon, mineshaft, desert/jungle temple, stronghold corridor, mansion, ancient-city and End City chest loot. Existing opened/generated loot is not rewritten. Eternal Steak comes from cooking Everlasting Beef. These are exploration rewards, not new ores or currencies. They support farming/exploration/workshop use and are not added to convenience transmutation outputs.

The slice has 28 registered items and uses the supplied icons byte-for-byte. **It is not full parity with every source accessory:** source durability/breakage, secondary harvest-tier/slip/plant-growth effects, client toggles, worn model geometry and animations still need integration. The other 21 source items (including the mimic egg) remain unregistered until their behavior is implemented. Do not label this as the entire Artifacts collection finished.

## Remaining integration packages

1. Accessory parity: remaining movement/loot/fishing/trading effects, umbrella, airborne movement/double jump, chorus rescue, portable warp, mimic/campsite spawning, source wearables and animations, original rarity/config tuning.
2. Spellbooks: map the supplied spell registry to existing magic schools; equipment, crafting, resources, mana/cooldowns, visual/audio effects, enemies/bosses, structures and progression. Preserve shared spell UI rather than add typing requirements.
3. Applied Energentics: network controller/power, cells/drives, terminals/search, item/fluid transfer, crafting jobs/CPUs, patterns, monitoring, security, spatial systems and supplied art. Persistence and no-duplication tests are prerequisites.
4. IndustrialCraft: catalogue legacy registry/config recipes, consolidate equivalent existing machines, adapt missing processing, electricity/storage, equipment, agriculture and nuclear content. Never create a parallel material or energy economy by blind import.
5. BuildCraft: legacy recipes and fluids, pipes/filters/gates, pumping/quarry/builders/blueprints, engines and robotics. Integrate claims, bounded work and existing transport APIs.
6. Computers: terminal UI, computers, networking/peripherals, turtle inventory/movement/actions and disk persistence. Supply a bounded offline runtime; do not execute source programs during intake or expose unrestricted host/network access.
7. Combined survival reachability, economy, client asset review, two-player claims, graves/keep-inventory, restart/save and performance validation; then package a local owner playtest before any release.

## Validation

- Source intake and repeated hash audit: passed for all 17,598 supplied files.
- Native accessory Java compilation: passed on the pinned toolchain.
- **32 focused server tests passed** (`accessories-final-tests.log`): Jimtafacts, existing Wayfaring equipment, graves and player rules. Includes real dungeon loot, all three cooking routes, no research prerequisite, carried/equipped distinction, sprint modifier removal, antidote and combat cooldowns, bounded vampiric healing, party protection, exclusion of player-owned spell damage from melee bonuses, reserved-drop exclusion and exactly-once grave recovery. The first run's grave assertion included starter items; clearing the mock inventory corrected the fixture.
- **Rendered client test passed** (`accessories-client.log`): all 28 stacks synchronized into the test gallery; screenshot inspected with all supplied icons visible and no missing-texture checkerboards. This checks inventory art, not the still-pending worn models.
- **Four asset/resource tests passed**: byte-identical texture provenance, resolvable modern item models, additive slot tags and idempotent regeneration preserving existing shared tags/language. Repository structure/documentation links passed.
- **Full resource/recipe audit passed** (`resource-audit-final.log`): 6,584 IDs, 22,190 block/item models, 3,729 quad parts and 176 combined models. The audit now includes the exact new registry IDs, additive equipment tags, metal-free food conversions and the existing Concordance recipe switch; no validation was removed.
- Git's staged blobs were verified against every original file: 17,597 byte-identical files, with only the documented third-party JAR intentionally excluded from Git. The local original remains preserved.
- Two-account dedicated-server gameplay, load/performance measurement, reconnect persistence and full collection parity remain untested. No hosted test or release readiness is claimed.
- No server restart or client updater promotion is authorized by this implementation checkpoint. Obtain the owner's confirmation before any later live restart and warn players for a full 30 seconds.
