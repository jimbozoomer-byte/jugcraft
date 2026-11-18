const {test}=require('node:test');
const assert=require('node:assert/strict');
const M=require('./model.js');
const catalog={schema:1,minecraft:'26.3',townSize:192,packFormat:[110,0],biomes:['minecraft:plains','minecraft:river','jugcraft:alpine_spawn'],structures:[{id:'minecraft:village_plains',biomes:['minecraft:plains']}],noiseSettings:{noise_router:{final_density:'minecraft:overworld/final_density',chunk_surface_level:'minecraft:overworld/chunk_surface_level'},aquifers:{surface_level:'minecraft:overworld/preliminary_surface_level'}},biomeSource:{type:'minecraft:multi_noise',preset:'minecraft:overworld'}};
test('default design saves and reopens without losing any samples',()=>{const d=M.createDesign();assert.deepEqual(M.validate(JSON.parse(JSON.stringify(d)),catalog),[]);assert.equal(d.grid.heights.length,4225);assert.equal(M.weight(d,0,0),1);assert.equal(M.weight(d,-1024,0),0);assert.equal(M.weight(d,-992,0),.5);assert.equal(M.weight(d,2048,0),0);});
test('bilinear terrain interpolation works on negative world coordinates',()=>{const d=M.createDesign();d.grid.heights.fill(80);d.grid.heights[32*65+32]=112;assert.equal(M.height(d,0,0),112);assert.equal(M.height(d,-16,-16),88);assert.equal(M.height(d,-10000,0),80);});
test('brushes are bounded, local and independently paint biomes',()=>{const d=M.createDesign();d.grid.heights.fill(80);M.paint(d,0,0,'raise',128,16,100,'minecraft:plains');assert.equal(M.height(d,0,0),96);assert.equal(M.height(d,128,0),80);M.paint(d,0,0,'biome',128,16,100,'jugcraft:alpine_spawn');assert.equal(d.biomes[M.index(d,0,0)],'jugcraft:alpine_spawn');assert.equal(M.height(d,0,0),96);M.paint(d,0,0,'river',128,16,100,'');assert.equal(M.height(d,0,0),59);assert.equal(d.biomes[M.index(d,0,0)],'minecraft:river');assert.match(M.validate(d,catalog).join(),/dry land/);});
test('bad imports return useful errors instead of throwing',()=>{for(const v of [null,{},[],{schema:2}, {...M.createDesign(),spawn:null,town:{x:'bad',z:0}}, {...M.createDesign(),structures:[null]}])assert.ok(M.validate(v,null,true).length);const d=M.createDesign();d.grid.heights[0]=NaN;assert.ok(M.validate(d,catalog).length);for(const c of [null,{}, {...catalog,structures:{}}, {...catalog,packFormat:'110'}])assert.ok(M.validateCatalog(c).length);});
test('export rejects missing mods, incompatible catalogs and structures without matching biome paint',()=>{const d=M.createDesign();d.biomes[200]='missing:biome';assert.match(M.validate(d,catalog,true).join(),/Missing installed biome/);d.biomes.fill('');d.structures.push({x:-504,z:8,id:'minecraft:village_plains'});assert.match(M.validate(d,catalog,true).join(),/permitted biome/);d.biomes[M.index(d,-504,8)]='minecraft:plains';assert.deepEqual(M.validate(d,catalog,true),[]);assert.ok(M.validate(d,{...catalog,minecraft:'1.21.1'},true).length);});
test('city snapping, footprints and repeated pins are checked',()=>{const d=M.createDesign();assert.deepEqual(M.snapTown(-501,201,192),{x:-496,z:208});d.town=M.snapTown(0,0,192);assert.match(M.validate(d,catalog).join(),/too close to spawn/);d.town=M.snapTown(320,320,192);assert.deepEqual(M.validate(d,catalog),[]);d.structures=[{x:-504,z:8,id:'minecraft:village_plains'},{x:-504,z:8,id:'minecraft:village_plains'}];assert.match(M.validate(d,catalog).join(),/duplicates/);});
test('an unfinished design with a submerged spawn can still be reopened for repair',()=>{const d=M.createDesign();d.grid.heights.fill(59);assert.deepEqual(M.validateShape(d),[]);assert.match(M.validate(d,catalog,true).join(),/dry land/);d.spawn=null;assert.ok(M.validateShape(d).length);});
test('square footprint corners cannot overlap each other or cover spawn',()=>{const d=M.createDesign();d.town={x:320,z:320};d.structures=[{x:168,z:168,id:'minecraft:village_plains'}];assert.match(M.validate(d,catalog).join(),/overlaps/);d.structures=[];d.spawn={x:230,z:230};assert.match(M.validate(d,catalog).join(),/too close to spawn/);});
test('pack compiler preserves the catalog and wires the world preset and density references',()=>{const d=M.createDesign(),before=M.copy(catalog);d.biomes.fill('jugcraft:alpine_spawn');d.town=M.snapTown(400,400,192);const files=M.packFiles(d,catalog),preset=JSON.parse(files['data/jugcraft/worldgen/world_preset/designed.json']);assert.deepEqual(catalog,before);const generator=preset.dimensions['minecraft:overworld'].generator;assert.equal(generator.biome_source.cells[0],1);assert.deepEqual(generator.biome_source.palette,['jugcraft:alpine_spawn']);assert.deepEqual(generator.biome_source.town,[400,0,400]);assert.equal(generator.settings,'jugcraft:designer');const noise=JSON.parse(files['data/jugcraft/worldgen/noise_settings/designer.json']);assert.equal(noise.noise_router.final_density,'jugcraft:designer/land');assert.equal(noise.aquifers.surface_level,'jugcraft:designer/surface');assert.deepEqual(JSON.parse(files['jugcraft-design.json']),d);});
test('negative coordinates export to the correct native start chunk',()=>{const d=M.createDesign();d.biomes.fill('minecraft:plains');d.structures=[{x:-504,z:-248,id:'minecraft:village_plains'}];const f=M.packFiles(d,catalog);assert.deepEqual(JSON.parse(f['data/jugcraft/worldgen/structure_set/designer/pin_0.json']).placement,{type:'jugcraft:design_pin',chunk_x:-32,chunk_z:-16});});
test('ZIP archive contains readable UTF-8 files and correct central directory offsets',()=>{const files={'pack.mcmeta':'{"name":"Vallée"}','data/test.json':'{}'},bytes=M.zip(files),v=new DataView(bytes.buffer);assert.equal(v.getUint32(0,true),0x04034b50);const end=bytes.length-22;assert.equal(v.getUint32(end,true),0x06054b50);assert.equal(v.getUint16(end+10,true),2);const central=v.getUint32(end+16,true);assert.equal(v.getUint32(central,true),0x02014b50);let offset=0;for(const [name,text]of Object.entries(files)){const length=v.getUint32(offset+18,true),nl=v.getUint16(offset+26,true);assert.equal(new TextDecoder().decode(bytes.slice(offset+30,offset+30+nl)),name);assert.equal(new TextDecoder().decode(bytes.slice(offset+30+nl,offset+30+nl+length)),text);offset+=30+nl+length;}assert.equal(offset,central);});
test('exported preset names only the vanilla dimensions, so the lairs come from the installed mod as in any world',()=>{const d=M.createDesign();d.biomes.fill('minecraft:plains');const files=M.packFiles(d,catalog),preset=JSON.parse(files['data/jugcraft/worldgen/world_preset/designed.json']);assert.deepEqual(Object.keys(preset.dimensions).sort(),['minecraft:overworld','minecraft:the_end','minecraft:the_nether']);assert.equal(preset.dimensions['minecraft:the_nether'].type,'minecraft:the_nether');assert.deepEqual(Object.keys(files).filter(p=>/^data\/[^/]+\/(dimension|dimension_type)\//.test(p)),[]);assert.deepEqual(Object.keys(JSON.parse(files['pack.mcmeta'])),['pack']);});

// ---- Spawn-relative climate bands (optional design block; mirrors ClimateProfile.CODEC, contract amendment 4) ----
const crypto=require('node:crypto');
const sha=b=>crypto.createHash('sha256').update(b).digest('hex');
const climateCatalog={...catalog,climate:{schema:1}};
function legacyFixture(){const d=M.createDesign();d.biomes.fill('jugcraft:alpine_spawn');d.biomes[M.index(d,-504,8)]='minecraft:plains';d.town=M.snapTown(400,400,192);d.structures=[{x:-504,z:8,id:'minecraft:village_plains'}];return d;}
const presetOf=files=>JSON.parse(files['data/jugcraft/worldgen/world_preset/designed.json']);
test('old designs without climate reopen, validate and save byte-for-byte unchanged',()=>{
  const text=JSON.stringify(legacyFixture(),null,2),d=JSON.parse(text);
  assert.deepEqual(M.validateShape(d),[]);assert.deepEqual(M.validate(d,catalog,true),[]);
  M.packFiles(d,catalog);assert.equal('climate' in d,false);assert.equal(JSON.stringify(M.copy(d),null,2),text);
});
test('old exports stay byte-identical to the pre-climate compiler, also with an older catalog',()=>{
  // Golden hashes produced by tools/world-designer/model.js at 8075c6d58b for this exact fixture.
  const files=M.packFiles(legacyFixture(),catalog);
  assert.equal(sha(M.zip(files)),'2037738c84e907769ecf6e84172051151db2d3429c950a598ca3ac35293e2b74');
  assert.equal(sha(files['data/jugcraft/worldgen/world_preset/designed.json']),'5bb4956330ba94c7e68a25a38952acbc41981632d186fb1569332033322d9b57');
  assert.equal('climate' in presetOf(files).dimensions['minecraft:overworld'].generator.biome_source,false);
  // A climate-capable catalog changes nothing for a design without climate.
  assert.equal(sha(M.zip(M.packFiles(legacyFixture(),climateCatalog))),'2037738c84e907769ecf6e84172051151db2d3429c950a598ca3ac35293e2b74');
});
test('a switched-off climate block exports no climate and leaves the generated preset identical',()=>{
  const off=legacyFixture();off.climate={...M.createClimate(),enabled:false,south_frigid:3};
  assert.deepEqual(M.validate(off,catalog,true),[]);
  const files=M.packFiles(off,catalog),plain=M.packFiles(legacyFixture(),catalog);
  assert.equal(files['data/jugcraft/worldgen/world_preset/designed.json'],plain['data/jugcraft/worldgen/world_preset/designed.json']);
  assert.deepEqual(Object.keys(files),Object.keys(plain));
  assert.deepEqual(JSON.parse(files['jugcraft-design.json']).climate,off.climate);
});
test('climate validation accepts and rejects exactly the eleven ClimateProfile rules',()=>{
  const base={schema:1,anchor:{mode:'fixed',x:0,z:0},north_frigid:2500,south_hot_start:2000,south_hot_end:4000,south_frigid:5500,transition:256,salt:0};
  const ok=p=>M.validateClimateProfile({...base,...p}).length===0;
  assert.ok(ok({}));const {salt,...noSalt}=base;assert.deepEqual(M.validateClimateProfile(noSalt),[]);
  // 1–3: schema, anchor mode and bounds.
  assert.ok(!ok({schema:2}));assert.ok(!ok({anchor:{mode:'climate_spawn',x:0,z:0}}));assert.ok(!ok({anchor:null}));
  assert.ok(ok({anchor:{mode:'fixed',x:-30000000,z:30000000}}));assert.ok(!ok({anchor:{mode:'fixed',x:30000001,z:0}}));assert.ok(!ok({anchor:{mode:'fixed',x:0,z:1.5}}));
  // 4–6: bounded integers.
  for(const f of ['north_frigid','south_hot_start','south_hot_end','south_frigid']) for(const v of [0,-1,1.5,'2500',null,NaN,1000001]) assert.ok(!ok({[f]:v}),`${f}=${v}`);
  assert.ok(ok({south_frigid:1000000}));assert.ok(ok({transition:0}));assert.ok(!ok({transition:-1}));assert.ok(!ok({transition:1025}));assert.ok(!ok({transition:2.5}));
  assert.ok(ok({salt:-2147483648}));assert.ok(ok({salt:2147483647}));assert.ok(!ok({salt:2147483648}));assert.ok(!ok({salt:0.5}));
  // 7: north_frigid > T.
  assert.ok(!ok({north_frigid:256}));assert.ok(ok({north_frigid:257}));assert.ok(ok({north_frigid:1,transition:0}));
  // 8: strict ordering.
  assert.ok(!ok({south_hot_end:2000}));assert.ok(!ok({south_frigid:4000,transition:0}));assert.ok(ok({south_hot_start:1,south_hot_end:2,south_frigid:3,transition:0}));
  // 9: 2*hot_start > T, including odd T (no rounding).
  assert.ok(!ok({south_hot_start:128}));assert.ok(ok({south_hot_start:129}));assert.ok(!ok({south_hot_start:127,transition:255,north_frigid:300}));assert.ok(ok({south_hot_start:128,transition:255}));
  // 10: hot_end - hot_start >= T; centred blends may touch.
  assert.ok(ok({south_hot_end:2256}));assert.ok(!ok({south_hot_end:2255}));
  // 11: 2*(frigid - hot_end) >= 3*T, i.e. hot_end + T/2 <= frigid - T.
  assert.ok(ok({south_frigid:4384}));assert.ok(!ok({south_frigid:4383}));assert.ok(ok({transition:255,south_frigid:4383}));assert.ok(!ok({transition:255,south_frigid:4382}));
  assert.ok(ok({north_frigid:1025,south_hot_start:513,south_hot_end:1537,south_frigid:3073,transition:1024}));
});
test('design climate blocks open for repair, report inline issues and block only invalid enabled exports',()=>{
  const d=legacyFixture();d.climate=M.createClimate();
  assert.deepEqual(M.validateShape(d),[]);assert.deepEqual(M.climateIssues(d.climate),[]);assert.deepEqual(M.validate(d,climateCatalog,true),[]);
  d.climate.south_hot_end=2100;
  assert.deepEqual(M.climateIssues(d.climate).map(i=>i.field),['south_hot_end']);
  assert.deepEqual(M.validateShape(d),[]);assert.match(M.validate(d,climateCatalog,true).join(),/Climate: Hot belt must be at least the transition width/);
  assert.throws(()=>M.packFiles(d,climateCatalog),/Hot belt/);
  d.climate.enabled=false;assert.deepEqual(M.validate(d,climateCatalog,true),[]);
  for(const climate of [[],'on',{...M.createClimate(),schema:2},{...M.createClimate(),enabled:'yes'},{...M.createClimate(),pools:{}},{...M.createClimate(),north_frigid:'2500'},{...M.createClimate(),transition:NaN},(({transition,...rest})=>rest)(M.createClimate())]) {
    assert.ok(M.validateShape({...d,climate}).length,JSON.stringify(climate));assert.ok(M.validate({...d,climate},climateCatalog,true).length);
  }
  assert.match(M.climateIssues({...M.createClimate(),pools:{}}).map(i=>i.message).join(),/pools/);
  assert.deepEqual(M.validateShape({...d,climate:null}),[]);
  const {salt,...noSalt}=M.createClimate();assert.deepEqual(M.validate({...d,climate:noSalt},climateCatalog,true),[]);
});
test('export writes the climate profile under the jugcraft:design source, anchored exactly at the spawn',()=>{
  const d=legacyFixture();d.climate={...M.createClimate(),salt:-7};d.spawn={x:-120,z:72};
  const before=M.copy(d),files=M.packFiles(d,climateCatalog),source=presetOf(files).dimensions['minecraft:overworld'].generator.biome_source;
  assert.deepEqual(d,before);assert.equal(source.type,'jugcraft:design');
  assert.deepEqual(Object.keys(source),['type','schema','base','grid','palette','cells','spawn','town','climate']);
  assert.deepEqual(source.climate,{schema:1,anchor:{mode:'fixed',x:-120,z:72},north_frigid:2500,south_hot_start:2000,south_hot_end:4000,south_frigid:5500,transition:256,salt:-7});
  assert.deepEqual(Object.keys(source.climate),['schema','anchor','north_frigid','south_hot_start','south_hot_end','south_frigid','transition','salt']);
  assert.deepEqual(source.spawn,[source.climate.anchor.x,0,source.climate.anchor.z]);assert.equal('pools' in source.climate,false);
  assert.deepEqual(M.validateClimateProfile(source.climate),[]);
  // Moving the spawn moves the anchor; nothing stores a second copy that could drift.
  d.spawn={x:256,z:-300};assert.deepEqual(presetOf(M.packFiles(d,climateCatalog)).dimensions['minecraft:overworld'].generator.biome_source.climate.anchor,{mode:'fixed',x:256,z:-300});
  assert.equal('anchor' in d.climate,false);
  const {salt,...noSalt}=M.createClimate();d.climate=noSalt;assert.equal(presetOf(M.packFiles(d,climateCatalog)).dimensions['minecraft:overworld'].generator.biome_source.climate.salt,0);
  const preset=presetOf(files);assert.deepEqual(preset.dimensions['minecraft:the_nether'],presetOf(M.packFiles(legacyFixture(),catalog)).dimensions['minecraft:the_nether']);
});
test('climate-enabled export is blocked unless the catalog reports climate support; other designs are unaffected',()=>{
  const d=legacyFixture();d.climate=M.createClimate();
  assert.match(M.validate(d,catalog,true).join(),/does not report climate-band support/);assert.throws(()=>M.packFiles(d,catalog),/climate-band support/);
  assert.match(M.validate(d,{...catalog,climate:{schema:2}},true).join(),/climate-band support/);
  assert.deepEqual(M.validate(d,climateCatalog,true),[]);assert.equal(M.catalogSupportsClimate(climateCatalog),true);assert.equal(M.catalogSupportsClimate(catalog),false);
  // Planning mode and drafting do not demand a catalog; older catalogs still load and export non-climate designs.
  assert.deepEqual(M.validate(d,null),[]);assert.deepEqual(M.validateCatalog(catalog),[]);assert.deepEqual(M.validateCatalog(climateCatalog),[]);
  d.climate.enabled=false;assert.doesNotThrow(()=>M.packFiles(d,catalog));
});
test('zones use the contract doubled-integer blends, with strict frigid limits and no blends at T=0',()=>{
  const c=M.createClimate(),z=s=>M.climateZoneAt(c,s);
  assert.deepEqual(z(0),{zone:'central'});assert.equal(z(-2500).zone,'frigid');assert.equal(z(-2500).strict,true);assert.equal(z(-1000000).zone,'frigid');
  assert.deepEqual(z(-2499).blend.to,'frigid');assert.equal(z(-2244).blend.progress,0);assert.equal(z(-2243).blend,undefined);
  assert.equal(z(1871).blend,undefined);assert.equal(z(1872).blend.to,'hot');assert.equal(z(2127).blend.to,'hot');assert.deepEqual(z(2128),{zone:'hot'});
  assert.equal(z(3872).blend.to,'temperate');assert.deepEqual(z(4128),{zone:'temperate'});assert.equal(z(5243).blend,undefined);
  assert.equal(z(5244).blend.to,'frigid');assert.equal(z(5499).blend.to,'frigid');assert.equal(z(5500).zone,'frigid');assert.equal(z(5500).strict,true);
  const odd={...c,transition:255},o=s=>M.climateZoneAt(odd,s);   // 2h-T <= 2s < 2h+T: blocks 1873..2127
  assert.equal(o(1872).blend,undefined);assert.equal(o(1873).blend.to,'hot');assert.equal(o(2127).blend.to,'hot');assert.equal(o(2128).blend,undefined);
  const sharp={...c,transition:0};for(const s of [-2500,-2499,1999,2000,3999,4000,5499,5500]) assert.equal(M.climateZoneAt(sharp,s).blend,undefined);
  assert.equal(M.climateZoneAt(sharp,-2499).zone,'central');assert.equal(M.climateZoneAt(sharp,1999).zone,'central');assert.equal(M.climateZoneAt(sharp,2000).zone,'hot');
  assert.equal(M.climateZoneAt(sharp,4000).zone,'temperate');assert.equal(M.climateZoneAt(sharp,5499).zone,'temperate');assert.equal(M.climateZoneAt(sharp,5500).zone,'frigid');
  for(const t of [0,1,255,256,1024]) {
    const p={...c,transition:t,south_frigid:6000},runs=M.climateSegments(p);
    for(let s=-9000;s<=9000;s++) {
      const r=runs.find(r=>r.s0<=s&&s<r.s1),k=M.climateZoneAt(p,s);
      assert.ok(r.zone?!k.blend&&k.zone===r.zone:k.blend&&new Set([k.blend.from,k.blend.to,r.north,r.south]).size===2,`T=${t} s=${s}`);
    }
  }
});
test('overview geometry: north (-Z) at the top, edges and labels measured from the spawn anchor, core outlined',()=>{
  const d=M.createDesign();d.spawn={x:-128,z:64};
  const o=M.climateOverview(d,M.createClimate(),{size:800});
  assert.equal(o.radius,8000);assert.equal(o.scale,.05);assert.deepEqual([o.anchor.px,o.anchor.py],[400,400]);
  const e=Object.fromEntries(o.edges.map(x=>[x.id,x]));
  assert.ok(Math.abs(e.north_frigid.y-(400-2500*.05))<.1);assert.equal(e.south_hot_start.y,500);assert.equal(e.south_hot_end.y,600);assert.equal(e.south_frigid.y,675);
  assert.deepEqual(o.edges.map(x=>x.distance),['2,500 N','2,000 S','4,000 S','5,500 S']);
  assert.deepEqual(o.edges.map(x=>x.z),[64-2500,2064,4064,5564]);assert.ok(o.edges.every(x=>x.visible));
  assert.deepEqual(o.edges.filter(x=>x.strict).map(x=>x.id),['north_frigid','south_frigid']);
  assert.ok(e.north_frigid.y<o.anchor.py&&o.anchor.py<e.south_hot_start.y);           // north above spawn, south below
  assert.deepEqual(o.regions.map(r=>r.zone||`${r.north}>${r.south}`),['frigid','frigid>central','central','central>hot','hot','hot>temperate','temperate','temperate>frigid','frigid']);
  assert.equal(o.regions[0].y0,0);assert.equal(o.regions.at(-1).y1,800);
  for(let i=1;i<o.regions.length;i++) assert.equal(o.regions[i].y0,o.regions[i-1].y1);
  assert.ok(Math.abs(o.regions[3].y1-o.regions[3].y0-256*.05)<1e-9);                    // transitions are T wide
  assert.deepEqual([o.core.left,o.core.top,o.core.right,o.core.bottom].map(v=>Math.round(v*100)/100),[355.2,345.6,457.6,448]);
  assert.ok(o.core.top<o.authored.top&&o.authored.bottom<o.core.bottom);
  assert.deepEqual(o.ticksZ.map(t=>t.label),['8,000 N','6,000 N','4,000 N','2,000 N','Spawn','2,000 S','4,000 S','6,000 S','8,000 S']);
  assert.deepEqual([o.ticksZ[0].y,o.ticksZ[0].z],[0,64-8000]);
  assert.deepEqual(o.ticksX.map(t=>t.label).slice(0,2),['8,000 W','6,000 W']);assert.equal(o.ticksX[0].px,0);assert.equal(o.ticksX.at(-1).label,'8,000 E');
  assert.deepEqual(M.overviewWorld(o,400,400),{x:-128,z:64});assert.deepEqual(M.overviewWorld(o,0,0),{x:-8128,z:-7936});
  // Far bands widen the view so both strict limits stay visible.
  const wide=M.climateOverview(d,{...M.createClimate(),south_frigid:20000},{size:800});assert.equal(wide.radius,23000);assert.ok(wide.edges.every(x=>x.visible));
  const zoomed=M.climateOverview(d,M.createClimate(),{size:800,radius:2000});assert.equal(zoomed.edges.find(x=>x.id==='south_frigid').visible,false);
});

test("Minecraft registry fixture stays identical to the real exporter",()=>{require("./export-fixture.cjs");});
