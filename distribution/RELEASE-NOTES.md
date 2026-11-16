JUGCRAFT November content update — release candidate

Changes prepared since 0.1.0-alpha.20261103.1:

- Five distinct rideable horse breeds: Icelandic, Mongolian, Shire, Giant Finnhorse and Friesian, with different travel, health and hauling strengths, ancestry records and crossbreeding.
- A tameable Pegasus with animated wings, Space to ascend and Left Ctrl to descend. Shires, Giant Finnhorses and saddled Pegasi support breed-sized cargo bags; breed horse armour is not fitted yet.
- Expanded pirate ships, small crews and ocean patrols, plus early firearms and Thanksgiving-era weapons.
- More Thanksgiving food, homestead and woodland crafts, toys, town details and festival decorations, with model refinements and conservative town placement that preserves owner additions.
- Guest friendship activities, errands, wardrobe and interface improvements; marriage eligibility remains limited to the reviewed adult roster.
- Better Peepo work orders, batching, Staff Posts, harvest behavior and social animations, alongside companion garden pacts.
- First-stage star-bred animal lineage and the Astrawand. Later gene-lab and astronomy proposals are not included as completed gameplay.
- Pharmacy progression, treatment effects and a strain HUD.
- Powered-equipment charging and handling improvements, scanner cancellation, chainsaw costs and drill-action fixes. Powered night vision now applies and bills consistently.
- Imbuement Chamber lapis-input fix. The Hydroponics Bay now clearly names its required Nutrient Solution and explains why it refuses plain water; pump/pipe and bucket transfers are covered by regressions.
- Villager-trading tooltip crash fix, native GUI tooltip handling and wrapping for long item descriptions on small screens.
- Aircraft control, banking, seating, muzzle alignment and compact controls-display fixes; walker cockpit, aiming and safe-dismount improvements.
- Better raid approach retries and guard lookout behavior around city walls.
- Unified encyclopedia route guidance, restored introductory chapters and clearer boss dossiers covering preparation, entry, rewards, reset and exits.
- Storm Roc and Glass Abbot encounter content; Cinder Tyrant heat/cooling mechanics; shared support credit for the migrated lair keepers. Storm Roc retains its separate living damage/rod-participant credit rule.
- Survival acquisition for additional armour sets, blank runes, Immolation and Drygmy's Blessing; prepared-component recipe rewards and simpler mineral-sand processing.
- Plastic routes from naphtha and scavenged plastic, and corrected copper/iron cable capacities.
- Seasonal quest and boss-bounty content, plus Beardshy.
- Reduced wild mushroom/fairy-ring ground coverage, repaired imported models and textures, storage-cell indicators, connected furnishing joins and distinct item names.
- Expanded decorative transmutation support. Its final output policy remains under economy review; this candidate must not be promoted until that review is resolved.
- Spawn-relative climate bands for future Overworld generation in the existing server world: arctic north beyond roughly 2,500 blocks; hot/tropical south beyond 2,000; temperate again beyond 4,000; frigid beyond 5,500, with blended transitions. Existing generated chunks and builds are preserved; Nether and End generation are unchanged.

Candidate status: not deployed. Full resource regeneration and data/model audits pass. The full local build and all 4,170 required server tests pass with optional integrations present and absent. Full rendered-client checks and the final corrected CI run remain pending, and two economy assertions remain unresolved. Exact evidence and limitations are recorded in docs/features/nov16-combined-release.md. Do not describe this candidate as a tested public release yet.

PR #612's drone tower, drop squads, plasma weapons, taller world and TV helpers are held for separate review and are not part of this candidate.

Minecraft 26.3, Fabric Loader 0.19.5 and Java 25 remain pinned. Updates preserve player settings, saves and extra mods; no new launcher profile or public Modrinth pack listing is required. Server installation requires a fresh backup and owner-approved restart with a full 30-second warning. Client updates are activated only after server verification.

Implementation includes the original contributors and Claude Code deliveries. Combined integration, fixes and validation: OpenAI Codex (GPT-6 family).
