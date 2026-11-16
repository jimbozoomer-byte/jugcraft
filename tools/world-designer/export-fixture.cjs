// Test-only exported world. The catalog's noise settings/pack format come from the pinned 26.3 game.
// Minecraft loads these exact exporter files as part of the game-test mod, before registries freeze.
// node tools/world-designer/export-fixture.cjs --write regenerates the fixtures; without it checks for drift.
const fs=require('node:fs'),path=require('node:path'),assert=require('node:assert/strict');
const M=require('./model.js'),catalog=require('./fixtures/climate-catalog.json');
const root=path.resolve(__dirname,'../..'),d=M.createDesign();
d.name='Climate export regression';
d.grid={origin_x:192,origin_z:-320,step:32,width:9,heights:Array(81).fill(80)};
d.biomes=Array(81).fill(''); d.spawn={x:320,z:-192}; d.climate=M.createClimate();
const files=M.packFiles(d,catalog),write=process.argv.includes('--write');
for(const [name,content] of Object.entries(files)) {
  const target=name.startsWith('data/') ? path.join(root,'src/gametest/resources',name)
    : path.join(__dirname,'fixtures/climate-export',name);
  if(write){fs.mkdirSync(path.dirname(target),{recursive:true});fs.writeFileSync(target,content);}
  else assert.equal(fs.readFileSync(target,'utf8'),content,`Export fixture drift: ${name}`);
}
console.log(`${Object.keys(files).length} real export fixture files ${write?'generated':'verified'}`);
