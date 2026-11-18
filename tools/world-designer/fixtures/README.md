# Climate export regression

`climate-catalog.json` is a minimal test catalog for pinned Minecraft 26.3. Its
noise settings and pack format were read from that game's bundled Overworld
configuration, exactly as the in-game catalog exports them. It contains no
textures, models or executable code.

`../export-fixture.cjs` passes this catalog and a small authored design through
the real `model.js` exporter. Its `data/` files live in `src/gametest/resources`,
so Minecraft decodes the density functions, noise settings, biome tags and world
preset during real registry loading. The other exported files live in
`climate-export/`. Nothing here ships in the production mod.

`node tools/world-designer/export-fixture.cjs --write` regenerates all eight
files. The editor test suite checks them byte-for-byte. The
`ClimateExportClientGameTests` client selects the registered exported preset,
generates every belt, moves world spawn, saves and reopens it. This covers pack
content loading; it does not automate dragging a ZIP into the Data Packs UI.
