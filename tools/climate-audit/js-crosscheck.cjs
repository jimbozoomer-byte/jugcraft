'use strict';
// Climate audit (worker D): the World Designer's climate validator and zone geometry against the audit oracle's
// vectors (written by ClimateBandsPropertyCheck from CONTRACT.md amendment 4). Run with node; not a game test.
//   node tools/climate-audit/js-crosscheck.cjs tools/world-designer/model.js vectors.json
const fs = require('fs');
const path = require('path');
const model = require(path.resolve(process.argv[2]));
const vectors = JSON.parse(fs.readFileSync(process.argv[3], 'utf8'));
let failures = 0, profiles = 0, zones = 0;
const fail = message => { if (failures++ < 20) console.log('FAIL: ' + message); };
const key = set => [...set].sort().join('+');
for (const v of vectors.profiles) {
  const p = {schema: 1, anchor: {mode: 'fixed', x: v.anchor[0], z: v.anchor[1]}, north_frigid: v.north_frigid, south_hot_start: v.south_hot_start,
    south_hot_end: v.south_hot_end, south_frigid: v.south_frigid, transition: v.transition, salt: v.salt};
  const errors = model.validateClimateProfile(p);
  profiles++;
  if ((errors.length === 0) !== v.valid) fail(`validity differs for ${JSON.stringify(p)}: oracle ${v.valid}, editor ${JSON.stringify(errors)}`);
  if (!v.valid || !v.zones) continue;
  const segments = model.climateSegments(p);
  for (const [s, ...expected] of v.zones) {
    zones++;
    const at = model.climateZoneAt(p, s);
    const got = at.blend ? new Set([at.blend.from, at.blend.to]) : new Set([at.zone]);
    if (key(got) !== key(expected)) fail(`climateZoneAt(${s}) is ${key(got)}, oracle ${key(expected)} for ${JSON.stringify(p)}`);
    const segment = segments.find(r => r.s0 <= s && s < r.s1);
    const seg = !segment ? new Set() : segment.zone ? new Set([segment.zone]) : new Set([segment.north, segment.south]);
    if (key(seg) !== key(expected)) fail(`climateSegments puts s ${s} in ${key(seg)}, oracle ${key(expected)} for ${JSON.stringify(p)}`);
  }
}
// Amendment 4 rule 3 and the "integers, no rounding" rule at the editor's export boundary.
for (const [bad, why] of [[{x: 30000001, z: 0}, 'anchor beyond 30,000,000'], [{x: 0.5, z: 0}, 'fractional anchor']]) {
  const p = {schema: 1, anchor: {mode: 'fixed', ...bad}, north_frigid: 2500, south_hot_start: 2000, south_hot_end: 4000, south_frigid: 5500, transition: 256, salt: 0};
  if (model.validateClimateProfile(p).length === 0) fail('editor accepts ' + why);
}
const fractional = {schema: 1, anchor: {mode: 'fixed', x: 0, z: 0}, north_frigid: 2500.5, south_hot_start: 2000, south_hot_end: 4000, south_frigid: 5500, transition: 256, salt: 0};
if (model.validateClimateProfile(fractional).length === 0) fail('editor accepts a fractional threshold');
console.log(`editor cross-check: ${profiles} profiles, ${zones} zone points, ${failures} failures`);
process.exit(failures ? 1 : 0);
