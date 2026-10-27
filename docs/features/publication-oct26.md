# October 26 combined release preparation

Owner authorized publishing local Jimtafacts work, integrating all open pull requests,
and preparing matching server/client updates. Work performed with OpenAI Codex.

## Integrated scope

The integration branch preserves the original commit ancestry for PRs #448, #449,
#452-#455, #458-#459, #465-#468, #470, #472-#474, #476-#486, #489-#493.
This includes Jimtafacts/native computers and transport, aircraft repairs, Chompa,
Frostward College, starter quartz/silicon, companion jobs, seasonal wildlife,
Concordance winter/stone/workshops/pacts, crates, guns, trains, town traders,
Pumpkin King, and the Crown of Thunder arena/rite groundwork.

Crown of Thunder is part 1 only: the Storm Roc boss remains future work.
Jimtafacts adaptation limits remain in jimtafacts-remaining.md.

## Integration decisions

- Preserve starter heat upgrades and companion work together in the nine-slot workshop.
- Preserve full native computer controls while adopting the fitted machine screen style.
- Keep Crowfield/Pumpkin King and Crown of Thunder as separate lairs and generators.
- Merge shared registry, translation, tag and test manifests additively; regenerate data.
- Verify the embedded Cobalt runtime against its reviewed original content inventory;
  permit only Loom's exact generated metadata, not arbitrary nested libraries.

## Validation and deployment status

Combined validation is in progress. Earlier Jimtafacts results do not certify this
larger batch. Local evidence is under build/release-*-oct26.log.
No server restart or client channel promotion has occurred for this candidate.
The live world and player settings must be retained. Obtain owner confirmation,
warn connected players and wait 30 seconds before a live stop/restart. Promote
clients only after the matching server is verified healthy.
