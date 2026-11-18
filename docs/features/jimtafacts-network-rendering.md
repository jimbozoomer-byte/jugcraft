# Original digital-storage rendering

Local work, 23 October 2026. AI assistance: OpenAI Codex (GPT-6). No tests or
builds have been run for this addition; include it in the final combined batch.

`DigitalStorageRenderer` uses all ten supplied item/fluid cell chassis models in
drive bays, including their original texture coordinates and tier markings. The
source `DriveModel.getSlotOrigin` places the five rows and two columns at
`((9-col*8)/16, (13-row*3)/16, 1/16)`. The ME chest uses the source cell offset
`(5/16, 4/16, 0)`. Empty bays have no inserted chassis. Original five-face LED
geometry and green/cyan/amber/red status colors show empty, occupied, type-full
and capacity-full cells; unpowered cells use black LEDs. Actual content-count
changes produce the supplied cubic 200 ms pale-red activity blink.

The inscriber's moving inside faces reproduce `InscriberRenderer`'s six quads,
UV formulas and compression/decompression easing. A committed native workshop
operation starts one 16-tick (800 ms at normal tick rate) cycle. The displayed
press and middle input/result are the actual item identities. The charger shows
the inserted or finished crystal in the supplied location with the original
`sin(time) * 0.02` vertical motion at height `0.35`. The motion uses game time so
pausing freezes it. Reduced-motion mode settles the press and removes the bob
and status blink.

Only public appearance is synchronized: cell tier/type and status numbers, blink
timing, bare displayed item identities and operation timing. No cell inventory,
cell filter, program, custom item components, queued pattern or owner identity
is sent through rendering packets. Saved authoritative machine inventories are
unchanged. Render updates are checked at most every five ticks, with packets only
when appearance changes; the successful work animation is sent immediately.

The source remains at
`art/owner-library/originals/Oct23/Jimtafacts/applied energentics reborn by jimbo/`
with its retained notices. Trusted CFR/javap were used for static inspection only:
`DriveModel`, `DriveRenderer`, `CellLedRenderer`, `CellState`,
`ChestOrDriveRenderState`, `MEChestRenderer`, `InscriberRenderer`,
`ChargerRenderer`, and their render state classes. No supplied class was executed.
The helper `tools/jimtafacts_network_rendering.py` requests original meshes through
the existing resource adapter and copies the inside texture byte-for-byte. Its
texture digest is in `network-render-copies.json`; the main adapter records the
drive models in `network-runtime-copies.json`.

Prepared, unrun validation: `JimtafactsNetworkRenderingGameTests` checks public
packets cannot reveal stored items/filters/owners or populate inventories, and
strips custom data from displayed workshop items. The focused Python tests check
all ten mesh/UV definitions and the exact inside texture. The combined client
playtest must inspect all four facings, cell insertion/removal, empty/occupied/full
LEDs, moving inscriber faces and charger item alignment. This rendering work does
not claim broader AE API or storage-format compatibility.

## Held and inventory cell LEDs (13 November 2026)

Local work, AI assistance: Claude Opus 5.5. The supplied cell item definitions
tint the status LED layer with `ae2:storage_cell_state` and, on portable cells,
the screen layer with `ae2:portable_cell_color`. The runtime copies had dropped
both, so the LED showed its raw white sprite. `tools/jimtafacts_network.py` now
copies each supplied tint list, mapping those two types to the native
`jugcraft:jimta_storage_cell_state` and `jugcraft:jimta_portable_cell_color`
sources (`DigitalCellTints`, registered with the other storage client hooks).
`DigitalCellLeds` reads the saved cell contents, charge and dye. Its colours are
the supplied `CellState` values (black uncharged portable cell, green empty, blue
holding items, orange no room for another type, red full) and the supplied
`AEItems` portable default `0x80CAFF`, read through the dyed-colour component as
`AbstractPortableCell.getColor` does. Jugcraft cells charge no space per type, so
orange means all 63 types are used with room left; the drive and ME chest LEDs
use the same rule. Validation: `scripts/tests/test_jimtafacts_cell_tints.py`
(run) and `JimtafactsCellLedGameTests` (written, not run). The rendered colours
remain to be inspected in a client.

## Drive cell and Color Applicator geometry audit (13 November 2026)

Local work, AI assistance: Claude Opus 5.5. A November 12 face probe reported
left-out faces on the ten drive cell chassis and on both Color Applicator models.

- **Drive cells: intentional, unchanged.** The supplied chassis also leave out
  their east, west and south faces. At the supplied bay origins the sides lie flush
  against the side panels and middle strut (or the ME chest walls) and the back
  faces a bay closed by the separators and back panel, so none can be seen from
  outside. The runtime copies stay identical to the supplied models.
- **Color Applicator: confirmed, fixed.** The supplied models expose no left-out
  face. The runtime copies had been through the generic coplanar separation pass,
  which pushed boxes that rely on flush neighbours past those neighbours and opened
  see-through slits up to 0.2 px wide (24 faces on the plain model, 30 on the
  coloured one, measured on a 0.025 px grid). Both models now use the supplied
  elements, faces and UVs. The pass existed only for art_check Z1/N1 overlaps, all
  between faces buried inside opaque neighbours, so the generator instead leaves
  out those buried faces (the 45-degree hose elbows' tops and bottoms and the
  nozzle-side hose ends' fronts; `jimtafacts_model_repairs.BURIED`).

`scripts/tests/test_jimtafacts_storage_geometry.py` (run) flood-fills each drive
bay and the ME chest slot, checks the applicators against the supplied models,
proves each left-out face is buried and applies art_check's per-model rules. The
in-game look of both items remains to be inspected in a client.
