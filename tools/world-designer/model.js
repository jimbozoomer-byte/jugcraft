/* Jugcraft World Designer — original MIT code. Shared browser/Node model and pack compiler. */
(function (root) {
  'use strict';
  const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
  const copy = value => JSON.parse(JSON.stringify(value));
  const identifier = /^[a-z0-9_.-]+:[a-z0-9_./-]+$/;
  function createDesign() {
    const width = 65, step = 32, heights = [];
    for (let z = 0; z < width; z++) for (let x = 0; x < width; x++) {
      const dx = (x - 32) / 32, dz = (z - 32) / 32;
      const ridge = 53 * Math.exp(-((dx + .48) ** 2 * 24 + (dz - .22) ** 2 * 3));
      const hill = 26 * Math.exp(-((dx - .55) ** 2 * 13 + (dz + .4) ** 2 * 9));
      heights.push(Math.round(77 + ridge + hill + 3 * Math.sin(x / 6) * Math.sin(z / 8)));
    }
    return {schema: 1, name: 'The first valley', grid: {origin_x: -1024, origin_z: -1024, step, width, heights},
      biomes: Array(width * width).fill(''), spawn: {x: 0, z: 0}, town: null, structures: []};
  }
  function index(d, x, z) {
    const g = d.grid;
    return clamp(Math.round((z - g.origin_z) / g.step), 0, g.width - 1) * g.width + clamp(Math.round((x - g.origin_x) / g.step), 0, g.width - 1);
  }
  function height(d, x, z) {
    const g = d.grid, u = clamp((x - g.origin_x) / g.step, 0, g.width - 1), v = clamp((z - g.origin_z) / g.step, 0, g.width - 1);
    const ix = Math.min(Math.floor(u), g.width - 2), iz = Math.min(Math.floor(v), g.width - 2), tx = u - ix, tz = v - iz;
    return (g.heights[iz*g.width+ix]*(1-tx)+g.heights[iz*g.width+ix+1]*tx)*(1-tz)
      +(g.heights[(iz+1)*g.width+ix]*(1-tx)+g.heights[(iz+1)*g.width+ix+1]*tx)*tz;
  }
  function weight(d, x, z) {
    const g=d.grid, end=(g.width-1)*g.step;
    const t=clamp(Math.min(x-g.origin_x,z-g.origin_z,g.origin_x+end-x,g.origin_z+end-z)/(2*g.step),0,1);
    return t*t*(3-2*t);
  }
  function paint(d, x, z, tool, radius, strength, target, biome) {
    const g=d.grid, prior=tool==='smooth' ? g.heights.slice() : null;
    const minX=clamp(Math.floor((x-radius-g.origin_x)/g.step),0,g.width-1), maxX=clamp(Math.ceil((x+radius-g.origin_x)/g.step),0,g.width-1);
    const minZ=clamp(Math.floor((z-radius-g.origin_z)/g.step),0,g.width-1), maxZ=clamp(Math.ceil((z+radius-g.origin_z)/g.step),0,g.width-1);
    for(let iz=minZ; iz<=maxZ; iz++) for(let ix=minX; ix<=maxX; ix++) {
      const dist=Math.hypot(g.origin_x+ix*g.step-x,g.origin_z+iz*g.step-z);
      if(dist>radius) continue;
      const i=iz*g.width+ix, falloff=.5+.5*Math.cos(Math.PI*dist/radius), h=g.heights[i];
      if(tool==='biome') d.biomes[i]=biome;
      else if(tool==='erase') d.biomes[i]='';
      else if(tool==='raise'||tool==='lower') g.heights[i]=clamp(h+(tool==='raise'?1:-1)*strength*falloff,40,256);
      else if(tool==='flatten'||tool==='river') {
        g.heights[i]=h+((tool==='river'?59:target)-h)*Math.min(1,strength/8)*falloff;
        if(tool==='river'&&falloff>.3) d.biomes[i]='minecraft:river';
      } else if(tool==='smooth') {
        let sum=0,n=0;
        for(let dz=-1;dz<=1;dz++) for(let dx=-1;dx<=1;dx++) {sum+=prior[clamp(iz+dz,0,g.width-1)*g.width+clamp(ix+dx,0,g.width-1)];n++;}
        g.heights[i]=h+(sum/n-h)*falloff*Math.min(1,strength/8);
      }
      g.heights[i]=Math.round(g.heights[i]*100)/100;
    }
  }
  function snapTown(x,z,size) { const half=size/2; return {x:Math.floor((x-half+8)/16)*16+half,z:Math.floor((z-half+8)/16)*16+half}; }
  function validateCatalog(c) {
    const ids=a=>Array.isArray(a)&&a.length>0&&a.every(v=>typeof v==='string'&&identifier.test(v));
    if(!c||c.schema!==1||c.minecraft!=='26.3') return ['Load a catalog exported by Jugcraft on Minecraft 26.3 first.'];
    if(!ids(c.biomes)||!Array.isArray(c.structures)||c.structures.some(s=>!s||typeof s.id!=='string'||!identifier.test(s.id)||!Array.isArray(s.biomes)||s.biomes.some(b=>typeof b!=='string'||!identifier.test(b)))) return ['The catalog has invalid biome or structure records.'];
    if(!Number.isInteger(c.townSize)||c.townSize<16||c.townSize>1024||c.townSize%16!==0) return ['The catalog has an invalid city size.'];
    if(!Array.isArray(c.packFormat)||c.packFormat.length!==2||c.packFormat.some(n=>!Number.isInteger(n)||n<0)||!c.noiseSettings?.noise_router?.final_density||!c.noiseSettings.noise_router.chunk_surface_level||!c.biomeSource?.type) return ['The catalog is missing world-generation settings. Export a new catalog from the game.'];
    return [];
  }
  // Opening a saved work-in-progress must remain possible even when its placements need repair.
  function validateShape(d) {
    if(!d||d.schema!==1||typeof d.name!=='string'||d.name.length<1||d.name.length>80) return ['Invalid design name or version.'];
    const g=d.grid, point=p=>p&&Number.isInteger(p.x)&&Number.isInteger(p.z)&&Math.abs(p.x)<=2000000&&Math.abs(p.z)<=2000000;
    if(!g||!Number.isInteger(g.width)||g.width<5||g.width>129||![4,8,16,32,64,128].includes(g.step)||!Number.isInteger(g.origin_x)||!Number.isInteger(g.origin_z)||Math.abs(g.origin_x)>1000000||Math.abs(g.origin_z)>1000000) return ['Invalid grid bounds or resolution.'];
    if(!Array.isArray(g.heights)||g.heights.length!==g.width*g.width||g.heights.some(h=>!Number.isFinite(h)||h<40||h>256)||!Array.isArray(d.biomes)||d.biomes.length!==g.width*g.width||d.biomes.some(b=>typeof b!=='string'||b!==''&&!identifier.test(b))) return ['Invalid terrain or biome samples.'];
    if(!point(d.spawn)||(d.town!==null&&!point(d.town))||!Array.isArray(d.structures)||d.structures.length>32||d.structures.some(s=>!point(s)||typeof s.id!=='string'||!identifier.test(s.id))) return ['Invalid landmark records.'];
    const c=d.climate;
    if(c!==undefined&&c!==null&&(typeof c!=='object'||Array.isArray(c)||c.schema!==1||typeof c.enabled!=='boolean'||Object.keys(c).some(k=>!CLIMATE_KEYS.has(k))
      ||CLIMATE_FIELDS.some(f=>(f!=='salt'||c.salt!==undefined)&&!(typeof c[f]==='number'&&Number.isFinite(c[f]))))) return ['Invalid or unsupported climate settings.'];
    return [];
  }
  const catalogErrorsFor=catalog=>validateCatalog(catalog).length>0;
  function validate(d,catalog, exporting=false) {
    const errors=[];
    if(!d||d.schema!==1) return ['Unsupported design version.'];
    if(typeof d.name!=='string'||d.name.length<1||d.name.length>80) errors.push('Name must contain 1–80 characters.');
    const g=d.grid;
    if(!g||!Number.isInteger(g.width)||g.width<5||g.width>129||![4,8,16,32,64,128].includes(g.step)
      ||!Number.isInteger(g.origin_x)||!Number.isInteger(g.origin_z)||Math.abs(g.origin_x)>1000000||Math.abs(g.origin_z)>1000000) return [...errors,'Invalid grid bounds or resolution.'];
    if(!Array.isArray(g.heights)||g.heights.length!==g.width*g.width||g.heights.some(h=>!Number.isFinite(h)||h<40||h>256)) errors.push('Heights must be 40–256, with one value at each grid point.');
    if(!Array.isArray(d.biomes)||d.biomes.length!==g.width*g.width||d.biomes.some(b=>typeof b!=='string'||b!==''&&!identifier.test(b))) errors.push('Invalid biome paint data.');
    if(errors.length) return errors;
    const validPoint=p=>p&&Number.isInteger(p.x)&&Number.isInteger(p.z)&&weight(d,p.x,p.z)===1;
    if(!validPoint(d.spawn)) errors.push('Place spawn inside the design, away from the feathered border.');
    else if(height(d,d.spawn.x,d.spawn.z)<65) errors.push('Spawn must be on dry land (height 65 or above).');
    if(d.town!==null && !validPoint(d.town)) errors.push('Place the city inside the design.');
    if(!Array.isArray(d.structures)||d.structures.length>32) return [...errors,'A design supports at most 32 structure markers.'];
    const all=[];
    if(d.town && validPoint(d.town)) {
      const size=catalog?.townSize||192;
      const snapped=snapTown(d.town.x,d.town.z,size);
      if(snapped.x!==d.town.x||snapped.z!==d.town.z) errors.push('City marker must match the town grid; place it again with the City tool.');
      all.push({...d.town,r:size/2,label:'City'});
      if(height(d,d.town.x,d.town.z)<65) errors.push('City must stand above sea level.');
    }
    const seen=new Set();
    d.structures.forEach((s,i)=>{
      if(!validPoint(s)||typeof s.id!=='string'||!identifier.test(s.id)||((s.x%16)+16)%16!==8||((s.z%16)+16)%16!==8) {errors.push(`Structure ${i+1} needs a valid ID and a start-chunk center inside the design.`);return;}
      const key=`${s.id}:${s.x}:${s.z}`; if(seen.has(key)) errors.push(`Structure ${i+1} duplicates another marker.`); seen.add(key);
      all.push({...s,r:96,label:`Structure ${i+1}`});
      if(exporting) {
        const definition=Array.isArray(catalog?.structures)?catalog.structures.find(a=>a?.id===s.id):null;
        if(!definition) errors.push(`Missing installed structure: ${s.id}`);
        else {
          const painted=d.biomes[index(d,s.x,s.z)];
          if(!painted||!Array.isArray(definition.biomes)||!definition.biomes.includes(painted)) errors.push(`${s.id}: paint a permitted biome at its marker before export.`);
        }
      }
    });
    all.forEach((p,i)=>{
      for(const [dx,dz] of [[-p.r,-p.r],[p.r,p.r],[-p.r,p.r],[p.r,-p.r]]) if(weight(d,p.x+dx,p.z+dz)!==1) {errors.push(`${p.label} footprint reaches the feathered border.`);break;}
      if(validPoint(d.spawn)&&Math.abs(p.x-d.spawn.x)<p.r+24&&Math.abs(p.z-d.spawn.z)<p.r+24) errors.push(`${p.label} is too close to spawn.`);
      all.slice(i+1).forEach(q=>{if(Math.abs(p.x-q.x)<p.r+q.r&&Math.abs(p.z-q.z)<p.r+q.r) errors.push(`${p.label} overlaps ${q.label}.`);});
    });
    if(exporting) {
      const catalogErrors=validateCatalog(catalog);
      if(catalogErrors.length) errors.push(...catalogErrors);
      else {const available=new Set(catalog.biomes); for(const b of new Set(d.biomes.filter(Boolean))) if(!available.has(b)) errors.push(`Missing installed biome: ${b}`);}
      if(new Set(d.biomes.filter(Boolean)).size>512) errors.push('A design supports at most 512 painted biomes.');
    }
    if(d.climate!==undefined&&d.climate!==null) {
      // A switched-off block exports nothing, so only its structure must be sound; its values may be a draft.
      const issues=climateIssues(d.climate), on=d.climate.enabled===true;
      issues.filter(i=>on||!CLIMATE_FIELDS.includes(i.field)).forEach(i=>errors.push('Climate: '+i.message));
      if(on&&exporting&&!catalogErrorsFor(catalog)&&!catalogSupportsClimate(catalog)) errors.push('Climate: this mod catalog does not report climate-band support. An older Jugcraft build would ignore the bands and generate none; update Jugcraft and export a new catalog, or switch climate bands off.');
    }
    return [...new Set(errors)];
  }
  // Optional spawn-relative climate bands (design schema 1, climate schema 1). These rules mirror
  // ClimateProfile.CODEC exactly (contract amendment 4); change both together. Integer arithmetic only:
  // every "T/2" comparison is doubled, so odd transition widths never round differently from Java.
  const CLIMATE_FIELDS=['north_frigid','south_hot_start','south_hot_end','south_frigid','transition','salt'];
  const CLIMATE_KEYS=new Set(['schema','enabled',...CLIMATE_FIELDS]);
  const CLIMATE_LIMIT=1000000, CLIMATE_TRANSITION_MAX=1024, CLIMATE_ANCHOR_LIMIT=30000000, INT_MIN=-2147483648, INT_MAX=2147483647;
  const climateLabels={north_frigid:'North frigid limit',south_hot_start:'Hot belt start',south_hot_end:'Temperate belt start',south_frigid:'South frigid limit',transition:'Transition width',salt:'Variety salt'};
  const fmt=n=>{const [whole,fraction]=String(Math.abs(n)).split('.');return (n<0?'-':'')+whole.replace(/\B(?=(\d{3})+(?!\d))/g,',')+(fraction?'.'+fraction:'');};
  function createClimate() {return {schema:1,enabled:true,north_frigid:2500,south_hot_start:2000,south_hot_end:4000,south_frigid:5500,transition:256,salt:0};}
  // Rules 4–11 on the thresholds; returns [{field,message}] so the editor can mark the offending input.
  function climateRuleIssues(v) {
    const issues=[], add=(field,message)=>issues.push({field,message});
    for(const f of ['north_frigid','south_hot_start','south_hot_end','south_frigid']) {
      if(!Number.isInteger(v[f])) add(f,`${climateLabels[f]} must be a whole number of blocks.`);
      else if(v[f]<1||v[f]>CLIMATE_LIMIT) add(f,`${climateLabels[f]} must be 1–1,000,000 blocks from spawn.`);
    }
    if(!Number.isInteger(v.transition)) add('transition','Transition width must be a whole number of blocks.');
    else if(v.transition<0||v.transition>CLIMATE_TRANSITION_MAX) add('transition','Transition width must be 0–1,024 blocks.');
    if(v.salt!==undefined&&(!Number.isInteger(v.salt)||v.salt<INT_MIN||v.salt>INT_MAX)) add('salt','Variety salt must be a whole number from -2,147,483,648 to 2,147,483,647.');
    if(issues.some(i=>i.field!=='salt')) return issues;
    const {north_frigid:nf,south_hot_start:hs,south_hot_end:he,south_frigid:sf,transition:t}=v;
    if(nf<=t) add('north_frigid',`North frigid limit must be more than the transition width (${fmt(t)} blocks) north of spawn.`);
    if(hs>=he) add('south_hot_end','Temperate belt must start south of the hot belt start.');
    if(he>=sf) add('south_frigid','South frigid limit must be south of the temperate belt start.');
    if(2*hs<=t) add('south_hot_start',`Hot belt must start more than half the transition width (${fmt(t/2)} blocks) south of spawn.`);
    if(hs<he&&he-hs<t) add('south_hot_end',`Hot belt must be at least the transition width (${fmt(t)} blocks) wide.`);
    if(he<sf&&2*(sf-he)<3*t) add('south_frigid',`Temperate belt must be at least 1.5 × the transition width (${fmt(3*t/2)} blocks) wide.`);
    return issues;
  }
  // The editable design block: {schema, enabled, thresholds, transition, salt?}. No anchor (always the spawn) and no pools.
  function climateIssues(c) {
    if(c===undefined||c===null) return [];
    if(typeof c!=='object'||Array.isArray(c)) return [{field:'climate',message:'Climate settings must be an object.'}];
    const issues=[];
    if(c.schema!==1) issues.push({field:'schema',message:'Unsupported climate settings version.'});
    if(typeof c.enabled!=='boolean') issues.push({field:'enabled',message:'Climate on/off must be true or false.'});
    for(const k of Object.keys(c)) if(!CLIMATE_KEYS.has(k)) issues.push({field:'climate',message:k==='pools'
      ?'Custom climate pools are not supported by this editor; remove "pools" to use the datapack defaults.'
      :`Unknown climate setting: ${k.slice(0,40)}.`});
    return issues.length?issues:climateRuleIssues(c);
  }
  // Rules 1–11 on an exported ClimateProfile object.
  function validateClimateProfile(p) {
    if(!p||typeof p!=='object'||Array.isArray(p)) return ['Climate profile must be an object.'];
    const errors=[];
    if(p.schema!==1) errors.push('Climate profile schema must be 1.');
    const a=p.anchor, coordinate=v=>Number.isInteger(v)&&Math.abs(v)<=CLIMATE_ANCHOR_LIMIT;
    if(!a||typeof a!=='object'||a.mode!=='fixed') errors.push('Climate anchor mode must be "fixed".');
    else if(!coordinate(a.x)||!coordinate(a.z)) errors.push('Climate anchor X/Z must be whole numbers within 30,000,000.');
    return [...errors,...climateRuleIssues(p).map(i=>i.message)];
  }
  // The profile written into the jugcraft:design biome source, or null when climate is absent or off.
  // The anchor is exactly the design spawn: the server codec rejects any other value (amendment 3).
  function climateProfile(d) {
    const c=d&&d.climate;
    if(!c||c.enabled!==true) return null;
    return {schema:1,anchor:{mode:'fixed',x:d.spawn.x,z:d.spawn.z},north_frigid:c.north_frigid,south_hot_start:c.south_hot_start,
      south_hot_end:c.south_hot_end,south_frigid:c.south_frigid,transition:c.transition,salt:c.salt===undefined?0:c.salt};
  }
  const catalogSupportsClimate=catalog=>Boolean(catalog&&catalog.climate&&catalog.climate.schema===1);
  // Nominal zone and transition at an integer block offset s = blockZ - anchorZ (south positive), using the
  // contract's doubled-integer blend tests. The game picks one side of a transition per patch; this only reports
  // which two zones mix there and how far across the transition (0 = inner edge) the point lies.
  function climateZoneAt(c,s) {
    const t=c.transition, nf=c.north_frigid, hs=c.south_hot_start, he=c.south_hot_end, sf=c.south_frigid, n=-s;
    if(n>=nf) return {zone:'frigid',strict:true};
    if(t>0&&nf-t<=n) return {zone:'central',blend:{from:'central',to:'frigid',progress:(n-(nf-t))/t}};
    if(s>=sf) return {zone:'frigid',strict:true};
    if(t>0&&sf-t<=s) return {zone:'temperate',blend:{from:'temperate',to:'frigid',progress:(s-(sf-t))/t}};
    if(t>0&&2*he-t<=2*s&&2*s<2*he+t) return {zone:s>=he?'temperate':'hot',blend:{from:'hot',to:'temperate',progress:(2*s-(2*he-t))/(2*t)}};
    if(s>=he) return {zone:'temperate'};
    if(t>0&&2*hs-t<=2*s&&2*s<2*hs+t) return {zone:s>=hs?'hot':'central',blend:{from:'central',to:'hot',progress:(2*s-(2*hs-t))/(2*t)}};
    if(s>=hs) return {zone:'hot'};
    return {zone:'central'};
  }
  // North-to-south runs of pure zones and transitions as whole-block offsets [s0, s1): block s belongs to the run
  // with s0 <= s < s1, exactly as climateZoneAt classifies it (odd T included). Blends list their north/south zones.
  function climateSegments(c) {
    const t=c.transition, nf=c.north_frigid, hs=c.south_hot_start, he=c.south_hot_end, sf=c.south_frigid, lo=Math.floor(t/2), hi=Math.ceil(t/2);
    return [{zone:'frigid',strict:true,s0:-Infinity,s1:1-nf},{north:'frigid',south:'central',s0:1-nf,s1:1-nf+t},{zone:'central',s0:1-nf+t,s1:hs-lo},
      {north:'central',south:'hot',s0:hs-lo,s1:hs+hi},{zone:'hot',s0:hs+hi,s1:he-lo},{north:'hot',south:'temperate',s0:he-lo,s1:he+hi},
      {zone:'temperate',s0:he+hi,s1:sf-t},{north:'temperate',south:'frigid',s0:sf-t,s1:sf},{zone:'frigid',strict:true,s0:sf,s1:Infinity}].filter(r=>r.s1>r.s0);
  }
  function overviewRadius(c) {
    const far=Math.max(c&&Number.isInteger(c.north_frigid)?c.north_frigid:0,c&&Number.isInteger(c.south_frigid)?c.south_frigid:0);
    return Math.max(8000,Math.ceil(Math.min(far,CLIMATE_LIMIT)*1.15/1000)*1000);
  }
  const niceStep=radius=>[250,500,1000,2000,2500,5000,10000,20000,25000,50000,100000,200000,250000,500000].find(v=>v>=radius/4)||1000000;
  const direction=(v,negative,positive)=>v===0?'Spawn':`${fmt(Math.abs(v))} ${v<0?negative:positive}`;
  // Pure geometry for the climate overview: a square of `size` pixels centred on the anchor, covering ±radius
  // blocks. North (-Z) is at the top. Distances are measured from the anchor (the design spawn), never from 0,0.
  function climateOverview(d,c,options={}) {
    const radius=options.radius||overviewRadius(c), size=options.size||800, left=options.left||0, top=options.top||0, scale=size/(2*radius);
    const ax=d.spawn.x, az=d.spawn.z, sy=s=>top+(clamp(s,-radius,radius)+radius)*scale;
    const px=x=>left+(x-ax+radius)*scale, py=z=>top+(z-az+radius)*scale;
    const g=d.grid, extent=(g.width-1)*g.step, inset=2*g.step;
    const box=(x0,z0,x1,z1)=>({x0,z0,x1,z1,left:px(x0),top:py(z0),right:px(x1),bottom:py(z1)});
    // `s` is the threshold block; `y` is the line between zones (the north frigid block's south face, else the north face).
    const edge=(id,s,label,strict)=>({id,s,z:az+s,y:top+((id==='north_frigid'?s+1:s)+radius)*scale,label,strict,distance:direction(s,'N','S'),visible:Math.abs(s)<=radius});
    const step=niceStep(radius), ticks=[];
    for(let k=-Math.floor(radius/step);k<=Math.floor(radius/step);k++) ticks.push(k*step);
    return {radius,size,left,top,scale,step,anchor:{x:ax,z:az,px:px(ax),py:py(az)},
      regions:climateSegments(c).map(r=>({...r,y0:sy(r.s0),y1:sy(r.s1)})).filter(r=>r.y1>r.y0),
      edges:[edge('north_frigid',-c.north_frigid,'Strict frigid limit',true),edge('south_hot_start',c.south_hot_start,'Hot belt starts',false),
        edge('south_hot_end',c.south_hot_end,'Temperate belt starts',false),edge('south_frigid',c.south_frigid,'Strict frigid limit',true)],
      core:box(g.origin_x,g.origin_z,g.origin_x+extent,g.origin_z+extent),
      authored:box(g.origin_x+inset,g.origin_z+inset,g.origin_x+extent-inset,g.origin_z+extent-inset),
      ticksX:ticks.map(dx=>({dx,x:ax+dx,px:px(ax+dx),label:direction(dx,'W','E')})),
      ticksZ:ticks.map(s=>({s,z:az+s,y:py(az+s),label:direction(s,'N','S')}))};
  }
  function overviewWorld(o,x,y) {return {x:o.anchor.x+(x-o.left)/o.scale-o.radius,z:o.anchor.z+(y-o.top)/o.scale-o.radius};}
  function packFiles(design,catalog) {
    const errors=validate(design,catalog,true); if(errors.length) throw new Error(errors.join('\n'));
    const d=copy(design), palette=[...new Set(d.biomes.filter(Boolean))];
    if(!palette.length) palette.push(catalog.biomes.includes('minecraft:plains')?'minecraft:plains':catalog.biomes[0]);
    const cells=d.biomes.map(b=>b?palette.indexOf(b)+1:0), settings=copy(catalog.noiseSettings), router=settings.noise_router;
    const files={}, put=(path,obj)=>{files[path]=JSON.stringify(obj,null,2)+'\n';};
    const density={type:'jugcraft:design_density',grid:d.grid,input:router.final_density};
    const surface={type:'jugcraft:design_density',grid:d.grid,input:router.chunk_surface_level,surface:true};
    router.final_density='jugcraft:designer/land'; router.chunk_surface_level='jugcraft:designer/surface';
    if(settings.aquifers) settings.aquifers.surface_level='jugcraft:designer/surface';
    const source={type:'jugcraft:design',schema:1,base:catalog.biomeSource,grid:d.grid,palette,cells,spawn:[d.spawn.x,0,d.spawn.z]};
    if(d.town) source.town=[d.town.x,0,d.town.z];
    const climate=climateProfile(d);
    if(climate) {const climateErrors=validateClimateProfile(climate); if(climateErrors.length) throw new Error(climateErrors.join('\n')); source.climate=climate;}
    put('pack.mcmeta',{pack:{description:`Jugcraft World Designer: ${d.name}`,min_format:catalog.packFormat,max_format:catalog.packFormat}});
    put('data/jugcraft/worldgen/density_function/designer/land.json',density);
    put('data/jugcraft/worldgen/density_function/designer/surface.json',surface);
    put('data/jugcraft/worldgen/noise_settings/designer.json',settings);
    put('data/jugcraft/worldgen/world_preset/designed.json',{dimensions:{
      'minecraft:overworld':{type:'minecraft:overworld',generator:{type:'minecraft:noise',biome_source:source,settings:'jugcraft:designer'}},
      'minecraft:the_nether':{type:'minecraft:the_nether',generator:{type:'minecraft:noise',biome_source:{type:'minecraft:multi_noise',preset:'minecraft:nether'},settings:'minecraft:nether'}},
      'minecraft:the_end':{type:'minecraft:the_end',generator:{type:'minecraft:noise',biome_source:{type:'minecraft:the_end'},settings:'minecraft:end'}}}});
    put('data/minecraft/tags/worldgen/world_preset/normal.json',{replace:false,values:['jugcraft:designed']});
    d.structures.forEach((s,i)=>put(`data/jugcraft/worldgen/structure_set/designer/pin_${i}.json`,{
      structures:[{structure:s.id,weight:1}],placement:{type:'jugcraft:design_pin',chunk_x:Math.floor(s.x/16),chunk_z:Math.floor(s.z/16)}}));
    put('jugcraft-design.json',d);
    files['READ-ME.txt']='Requires Jugcraft with World Designer and the same Minecraft/mod collection used to export the catalog.\nCreate a NEW world; add this ZIP in Data Packs, enable it, then choose Jugcraft Designed in World Type. Keep structures enabled.\nThe painted surface sits above original deep caves; outer two grid cells blend to procedural terrain.\nStructure pins choose start chunks, not exact building corners; biome and terrain constraints still apply. Native random structures remain enabled. One Jugcraft walled city is supported.\nDo not replace this pack in an established world: already generated chunks are not regenerated.\n';
    return files;
  }
  // Small standards-based ZIP writer (stored entries). No downloaded ZIP library or network dependency.
  function zip(files) {
    const encode=new TextEncoder(), entries=[], parts=[]; let offset=0;
    const crc=bytes=>{let c=0xffffffff;for(const b of bytes){c^=b;for(let k=0;k<8;k++) c=(c>>>1)^((c&1)?0xedb88320:0);}return (c^0xffffffff)>>>0;};
    for(const [path,text] of Object.entries(files)) {
      const name=encode.encode(path), data=encode.encode(text), checksum=crc(data), header=new Uint8Array(30+name.length), view=new DataView(header.buffer);
      view.setUint32(0,0x04034b50,true);view.setUint16(4,20,true);view.setUint16(6,0x800,true);view.setUint16(12,33,true);
      view.setUint32(14,checksum,true);view.setUint32(18,data.length,true);view.setUint32(22,data.length,true);view.setUint16(26,name.length,true);header.set(name,30);
      entries.push({name,data,checksum,offset});parts.push(header,data);offset+=header.length+data.length;
    }
    const start=offset;
    for(const e of entries) {
      const header=new Uint8Array(46+e.name.length),v=new DataView(header.buffer);
      v.setUint32(0,0x02014b50,true);v.setUint16(4,20,true);v.setUint16(6,20,true);v.setUint16(8,0x800,true);v.setUint16(14,33,true);
      v.setUint32(16,e.checksum,true);v.setUint32(20,e.data.length,true);v.setUint32(24,e.data.length,true);v.setUint16(28,e.name.length,true);v.setUint32(42,e.offset,true);header.set(e.name,46);parts.push(header);offset+=header.length;
    }
    const end=new Uint8Array(22),v=new DataView(end.buffer);v.setUint32(0,0x06054b50,true);v.setUint16(8,entries.length,true);v.setUint16(10,entries.length,true);v.setUint32(12,offset-start,true);v.setUint32(16,start,true);parts.push(end);
    const result=new Uint8Array(offset+22);let cursor=0;for(const p of parts){result.set(p,cursor);cursor+=p.length;}return result;
  }
  const api={createDesign,copy,index,height,weight,paint,snapTown,validate,validateShape,validateCatalog,packFiles,zip,
    createClimate,climateIssues,climateProfile,validateClimateProfile,catalogSupportsClimate,climateZoneAt,climateSegments,climateOverview,overviewWorld,overviewRadius,climateLabels,formatBlocks:fmt};
  if(typeof module!=='undefined') module.exports=api;
  root.JugcraftDesign=api;
})(globalThis);
