# Authoring Jugcraft town guests

When the owner asks to add a waifu, named guest or new guest wardrobe, start from the fitted Roblox rig and the approved nine-character Minecraft lineup. The local **Jugcraft Guest Modeling Kit** contains the original references, all 32 model recipes and editable models, 13 body fits, packed Blender sources and the step-by-step handbook. On the owner's workstation open `%USERPROFILE%\Documents\Codex\Deliverables\Jugcraft\Guest Modeling Kit\START-HERE.md`. The portable copy is `Jugcraft-Guest-Modeling-Kit.zip` beside that folder. The handbook links to the photo studies, original Reze mesh, approved lineup, exact build commands and game captures.

## Add a guest

1. Choose a stable roster ID and a reference outfit. For an existing guest, reuse its ID, edition and saved-data relationships. For a new guest, check `GuestRoster.ALL`, `GuestAppearances.CHOSEN`, `GuestPortraitModel.GUESTS` and the group sets in `GuestBody` and `tools/guest_skins.py`. Preserve the historical placeholder mapping.
2. Choose a fit from `inputs/forms/fits.json` in the kit and one or two donor hair pieces from `inputs/heads`. Use `tools/guest_roster_fittings.py` for the new guest's `SPECS`, `DYES`, `FITTINGS`, jaw factor and `hair_fit` settings. Check the head-scale dispatch in `tools/guest_roblox_portraits.py`. The first eight are authored in that entry point. Reze uses its original clothed source export.
3. Paint fitted garments on the approved body surface. Use geometry for free hems, sleeves with volume, hair ornaments and distinctive accessories. `tools/guest_signature_outfits.py` has seven worked examples. `tools/guest_photo_outfits.py` covers lace, satin, wrap construction and shading exposed midriffs between the shirt and waistband. Compare front, back and profile against the selected outfit.
4. From a full checkout, run the generator with the kit's data paths and one explicit ID. It writes the mesh and 2048² atlas into the checkout and an editable model into the review directory. Use the actual checkout paths and guest ID.

```powershell
$kit = Join-Path $env:USERPROFILE 'Documents/Codex/Deliverables/Jugcraft/Guest Modeling Kit'
& "$kit/Rebuild-Guest.ps1" my_guest -Project (Get-Location).Path
python tools/guest_skins.py
python -B -m unittest discover -s scripts/tests -p "test_guest*.py"
.\gradlew.bat runClientGameTest "-PclientTests=GuestClientGameTests"
```

5. Inspect the actual Minecraft front and profile at the approved zoom. Check seasonal layers, strides, head turns and held items where the design changes them. Confirm the outfit and face read at walking distance. Save the named previews, fit, donor IDs and verified revision in the local kit. Keep the artist's source library and personal reference photos there. Review the runtime resource pair and fallback skins in the release checkout.
6. Follow the checkout's release instructions. The live restart needs owner confirmation and a full 30-second player warning.

The client loads `src/main/resources/assets/jugcraft/town_models/guests/<id>.json` and `textures/entity/town_guests/guest_<id>.png` under the same assets directory. The 64² fallback and seasonal variants live in `textures/entity/townsfolk/`. For the registration history and older fallback work, see the [guest appearance audit](audits/jze-guest-appearances.md).
