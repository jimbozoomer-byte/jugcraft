# Scary creature asset provenance

- `src/client/java/.../client/scary/*Geometry.java`: generated cuboid geometry from this contributor's local prototype; Kook includes the contributor's Blockbench edits. Original implementation, MIT contribution.
- `art/scary-creatures/*.bbmodel`: editable original cuboid models with embedded generated atlases; local filesystem paths removed. The Kook skull atlas region was completely replaced with an original menacing skull with angular eye sockets, a nasal cavity and exposed teeth for this branch.
- `assets/jugcraft/textures/entity/scary/` and the four spawn-egg icons: programmatically drawn original texture atlases/icons, MIT contribution. No reference photograph/cartoon is embedded. No Mojang skull pixels are included in this version.
- `assets/jugcraft/sounds/scary/`: 26 newly synthesized clips using sine waves and seeded noise. Generator: `tools/scary_sounds.py` (Python standard library plus externally installed ffmpeg). No sampled audio, no runtime network calls; code and generated clips MIT contribution.
- Public audio deliberately differs from the private local prototype, whose downloaded Scooby-Doo TV/game clips did not have documented redistribution permission. Those files and source archives are excluded from this PR.
- Scooby-Doo character names and visual themes are reference inspiration; underlying characters are third-party intellectual property. This record claims original implementation/assets, not ownership of the characters or endorsement. Maintainer review of fan-content suitability is pending.

The public skull atlas is reproducible with `tools/scary_skull.py` (Pillow). It uses a 2048-pixel atlas with unchanged logical UV coordinates.

The four historical local generators are not copied because some extract proprietary textures/audio. The checked-in geometry and editable models are the authoritative public versions; do not regenerate this branch from the private prototype without reapplying these provenance changes.
