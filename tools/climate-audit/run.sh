#!/bin/sh
# Climate audit property checks (worker D). Compiled and run with a bare JDK (21+) outside Gradle, against the climate
# core's pure classes: no Minecraft, Fabric or DFU jars. Not a Gradle, game or Minecraft test.
#   tools/climate-audit/run.sh [repo root] [work dir]
# Writes the oracle vectors to <work dir>/vectors.json, then cross-checks the World Designer's validator and zones
# against them with node, if node is installed.
set -eu
ROOT=${1:-$(cd "$(dirname "$0")/../.." && pwd)}
WORK=${2:-$(mktemp -d)}
CORE="$ROOT/src/main/java/io/github/jimbozoomer/jugcraft/world/climate"
ORACLE="$ROOT/src/gametest/java/io/github/jimbozoomer/jugcraft/test/ClimateAuditGeometry.java"
mkdir -p "$WORK/stub/io/github/jimbozoomer/jugcraft/world/climate" "$WORK/classes"
# ClimateBands has one ClimateProfile overload; the real ClimateProfile needs DFU and Minecraft, so a one-line stub
# with the same accessor stands in. Every class under test is the core's own source, unchanged.
cat > "$WORK/stub/io/github/jimbozoomer/jugcraft/world/climate/ClimateProfile.java" <<'JAVA'
package io.github.jimbozoomer.jugcraft.world.climate;
public record ClimateProfile(ClimateGeometry geometry) { }
JAVA
javac -d "$WORK/classes" "$WORK/stub/io/github/jimbozoomer/jugcraft/world/climate/ClimateProfile.java" \
	"$CORE/ClimateAnchor.java" "$CORE/ClimateBands.java" "$CORE/ClimateGeometry.java" "$CORE/ClimateNoise.java" \
	"$CORE/ClimateRules.java" "$CORE/ClimateSelection.java" "$CORE/ClimateZone.java" "$ORACLE" "$ROOT/tools/climate-audit/ClimateBandsPropertyCheck.java"
java -cp "$WORK/classes" io.github.jimbozoomer.jugcraft.test.ClimateBandsPropertyCheck "$WORK/vectors.json"
if command -v node >/dev/null 2>&1 && [ -f "$ROOT/tools/world-designer/model.js" ]; then
	node "$ROOT/tools/climate-audit/js-crosscheck.cjs" "$ROOT/tools/world-designer/model.js" "$WORK/vectors.json"
fi
