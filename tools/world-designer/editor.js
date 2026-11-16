/* Offline editor. Catalog content and imported files are treated as data, never executable HTML. */
(function () {
  'use strict';
  const M=JugcraftDesign, $=id=>document.getElementById(id), canvas=$('map'), ctx=canvas.getContext('2d');
  const preview=document.createElement('canvas'); preview.width=preview.height=512;
  const demo={biomes:['minecraft:plains','minecraft:forest','minecraft:taiga','minecraft:river','minecraft:meadow','minecraft:snowy_plains','minecraft:desert'],
    structures:[{id:'minecraft:village_plains',biomes:['minecraft:plains','minecraft:meadow']}]};
  let design=M.createDesign(), catalog=null, tool='raise', view='terrain', zoom=1, pan={x:0,y:0}, pointer=null, stroke=null, space=false, dirty=false, frame=false, toastTimer;
  let history=[], future=[], overviewZoom=1, climatePending={};
  const labels={raise:'Raise terrain',lower:'Lower terrain',flatten:'Level terrain',smooth:'Smooth terrain',river:'Carve river',biome:'Paint biome',erase:'Clear biome paint',pan:'Move map',spawn:'Place spawn',town:'Place walled city',structure:'Place structure'};
  const pretty=id=>id.split(':').pop().replaceAll('_',' ').replace(/\b\w/g,c=>c.toUpperCase());
  const content=()=>catalog||demo;
  function notify(message){$('toast').textContent=message;$('toast').classList.add('show');clearTimeout(toastTimer);toastTimer=setTimeout(()=>$('toast').classList.remove('show'),4500);}
  function remember(){history.push(M.copy(design));if(history.length>40)history.shift();future=[];dirty=true;updateUndo();}
  function updateUndo(){$('undo').disabled=!history.length;$('redo').disabled=!future.length;}
  function restore(from,to){if(!from.length)return;to.push(M.copy(design));design=from.pop();dirty=true;$('name').value=design.name;updateUndo();refresh();}
  function transform(){const extent=(design.grid.width-1)*design.grid.step, s=Math.min((canvas.clientWidth-90)/extent,(canvas.clientHeight-190)/extent)*zoom;return {s,x:canvas.clientWidth/2+pan.x,y:canvas.clientHeight/2+pan.y-4,extent};}
  function screen(x,z){const t=transform();return {x:t.x+(x-design.grid.origin_x-t.extent/2)*t.s,y:t.y+(z-design.grid.origin_z-t.extent/2)*t.s};}
  function world(x,y){const t=transform();return {x:(x-t.x)/t.s+design.grid.origin_x+t.extent/2,z:(y-t.y)/t.s+design.grid.origin_z+t.extent/2};}
  function location(e){const b=canvas.getBoundingClientRect();return {x:e.clientX-b.left,y:e.clientY-b.top};}
  function biomeColor(id){if(!id)return [97,132,79];if(/river|ocean/.test(id))return [63,128,150];if(/snow|frozen|ice/.test(id))return [184,210,209];if(/desert|badland/.test(id))return [206,173,110];if(/swamp|marsh/.test(id))return [99,133,100];let h=0;for(const c of id)h=(h*31+c.charCodeAt(0))|0;return [68+Math.abs(h%49),113+Math.abs((h>>5)%47),65+Math.abs((h>>10)%56)];}
  function buildPreview(){
    const g=design.grid, n=preview.width, extent=(g.width-1)*g.step, image=preview.getContext('2d').createImageData(n,n), contours=$('contours').checked;
    for(let py=0;py<n;py++)for(let px=0;px<n;px++){
      const x=g.origin_x+px/(n-1)*extent,z=g.origin_z+py/(n-1)*extent,h=M.height(design,x,z),hE=M.height(design,x+g.step,z),hS=M.height(design,x,z+g.step);
      let color=h<63?[43+(h-40),91+(h-40)*1.3,108+(h-40)*1.6]:h>155?[178,188,160]:[84+(h-64)*.55,116+(h-64)*.54,67+(h-64)*.62];
      if(view==='biomes')color=biomeColor(design.biomes[M.index(design,x,z)]);
      if(view==='height'){const t=(h-40)/216;color=[51+t*190,93+t*153,101+t*123];}
      const shade=Math.max(.58,Math.min(1.3,1+(hE-h+hS-h)*.015));
      const line=contours&&Math.floor(h/8)!==Math.floor(M.height(design,x+extent/n,z+extent/n)/8);
      const edge=M.weight(design,x,z), alpha=.45+.55*edge, i=(py*n+px)*4;
      for(let c=0;c<3;c++)image.data[i+c]=Math.round(color[c]*shade*(line?.68:1)*alpha);
      image.data[i+3]=255;
    }
    preview.getContext('2d').putImageData(image,0,0);requestDraw();
  }
  function requestDraw(){if(!frame){frame=true;requestAnimationFrame(()=>{frame=false;draw();});}}
  function draw(){
    const w=canvas.clientWidth,h=canvas.clientHeight,ratio=window.devicePixelRatio||1;
    if(canvas.width!==Math.round(w*ratio)||canvas.height!==Math.round(h*ratio)){canvas.width=Math.round(w*ratio);canvas.height=Math.round(h*ratio);}
    ctx.setTransform(ratio,0,0,ratio,0,0);ctx.clearRect(0,0,w,h);
    if(view==='climate'){drawClimate(w,h);return;}
    const t=transform(),g=design.grid,start=screen(g.origin_x,g.origin_z),size=t.extent*t.s;
    ctx.fillStyle='#12251f';ctx.fillRect(0,0,w,h);ctx.strokeStyle='#20372b';ctx.lineWidth=1;
    for(let x=0;x<w;x+=32){ctx.beginPath();ctx.moveTo(x,0);ctx.lineTo(x,h);ctx.stroke();}for(let y=0;y<h;y+=32){ctx.beginPath();ctx.moveTo(0,y);ctx.lineTo(w,y);ctx.stroke();}
    ctx.shadowBlur=35;ctx.shadowColor='#020d0acc';ctx.drawImage(preview,start.x,start.y,size,size);ctx.shadowBlur=0;ctx.strokeStyle='#8aa171';ctx.strokeRect(start.x,start.y,size,size);
    const inset=2*g.step*t.s;ctx.setLineDash([4,6]);ctx.strokeStyle='#d9e5bb66';ctx.strokeRect(start.x+inset,start.y+inset,size-2*inset,size-2*inset);ctx.setLineDash([]);
    function pin(p,label,symbol,color,radius){if(!p)return;const at=screen(p.x,p.z);if(radius){ctx.fillStyle=color+'22';ctx.strokeStyle=color+'88';ctx.lineWidth=1;ctx.strokeRect(at.x-radius*t.s,at.y-radius*t.s,radius*2*t.s,radius*2*t.s);ctx.fillRect(at.x-radius*t.s,at.y-radius*t.s,radius*2*t.s,radius*2*t.s);}
      ctx.fillStyle='#1a2a21';ctx.strokeStyle=color;ctx.lineWidth=2;ctx.beginPath();ctx.arc(at.x,at.y,12,0,Math.PI*2);ctx.fill();ctx.stroke();ctx.textAlign='center';ctx.textBaseline='middle';ctx.fillStyle=color;ctx.font='bold 15px Segoe UI';ctx.fillText(symbol,at.x,at.y-1);
      ctx.font='11px Segoe UI';const width=ctx.measureText(label).width+14;ctx.fillStyle='#17291fe8';ctx.fillRect(at.x-width/2,at.y+17,width,21);ctx.fillStyle='#e9f0d8';ctx.fillText(label,at.x,at.y+27);
    }
    design.structures.forEach((s,i)=>pin(s,pretty(s.id),'⌂','#edd7a0',96));pin(design.town,'Walled city','▥','#f2c986',(content().townSize||192)/2);pin(design.spawn,'Spawn','⚑','#d6edac',0);
    if(pointer&&tool!=='pan'&&!space){const p=world(pointer.x,pointer.y);ctx.strokeStyle='#eff8d9';ctx.lineWidth=1;ctx.setLineDash([5,4]);ctx.beginPath();ctx.arc(pointer.x,pointer.y,['spawn','town','structure'].includes(tool)?14:Number($('radius').value)*t.s,0,Math.PI*2);ctx.stroke();ctx.setLineDash([]);$('cursor').textContent=`X ${Math.round(p.x).toLocaleString()}  ·  Z ${Math.round(p.z).toLocaleString()}  ·  Height ${Math.round(M.height(design,p.x,p.z))}`;}
    const scale=128*t.s>45?128:256;$('scale').style.width=`${Math.max(50,scale*t.s)}px`;$('scale').textContent=`${scale} blocks`;
  }
  // ---- Climate bands: settings panel and the overview, which reaches far beyond the 65 × 65 terrain grid ----
  const climateFields=['north_frigid','south_hot_start','south_hot_end','south_frigid','transition','salt'], blocks=M.formatBlocks;
  const zoneColor={frigid:'#d5e6e8',central:'#5f8b58',hot:'#dcab5f',temperate:'#2e6b57'}, zoneInk={frigid:'#1b2c2c',central:'#eef5e4',hot:'#2d2212',temperate:'#e6f2e9'};
  const zoneName={frigid:'Frigid',central:'Central',hot:'Hot',temperate:'Temperate'}, zoneHint={frigid:'frigid biome pools only · painted cells still win',central:'normal biome mix, unchanged',hot:'desert · savanna · jungle pools',temperate:'forest · taiga · grassland pools'};
  const climateOn=()=>Boolean(design.climate&&design.climate.enabled===true), climateValues=()=>design.climate||M.createClimate();
  function syncClimate(){
    const c=design.climate,on=climateOn(),values=climateValues(),issues=on?M.climateIssues(c):[],bad=new Set(issues.map(i=>i.field));
    $('climate-enabled').checked=on;$('climate-state').textContent=on?'On':'Off';
    for(const f of climateFields){const input=$('climate-'+f),v=values[f]===undefined&&f==='salt'?0:values[f];if(!(f in climatePending))input.value=Number.isFinite(v)?String(v):'';input.disabled=!on;input.setAttribute('aria-invalid',String(on&&(bad.has(f)||f in climatePending)));}
    const messages=[...Object.values(climatePending),...issues.map(i=>i.message)];
    if(on&&catalog&&!M.catalogSupportsClimate(catalog))messages.push('This mod catalog does not report climate-band support, so export is blocked: an older Jugcraft build would ignore the bands. Update Jugcraft and export a new catalog.');
    $('climate-issues').replaceChildren(...[...new Set(messages)].map(m=>{const li=document.createElement('li');li.textContent=m;return li;}));
    $('climate-note').textContent=`Blocks north/south of spawn (X ${design.spawn.x} · Z ${design.spawn.z}); north is −Z. Belts run east and west without end. Painted cells keep their biome; climate changes unpainted and outside biomes only, never terrain. “Strict” frigid means frigid biomes only: as in vanilla, frozen and deep frozen oceans rain in places and their open water does not fully freeze, and cold desert or Siberian taiga are cold but dry.`;
  }
  function climateChanged(){syncClimate();validation();caption();requestDraw();}
  function setView(next){view=next;document.querySelectorAll('[data-view]').forEach(a=>{const on=a.dataset.view===view;a.classList.toggle('selected',on);a.setAttribute('aria-pressed',String(on));});$('active-tool').textContent=view==='climate'?'Climate overview · scroll to zoom':labels[tool];canvas.style.cursor=view==='climate'?'default':tool==='pan'?'grab':'crosshair';caption();if(view==='climate')requestDraw();else buildPreview();}
  function caption(){const extent=(design.grid.width-1)*design.grid.step,climate=view==='climate';
    $('map-size').textContent=climate?`Climate overview · ±${blocks(overview().radius)} blocks`:`${extent.toLocaleString()} × ${extent.toLocaleString()} blocks`;
    $('map-sub').textContent=climate?(climateOn()?'From spawn · north is −Z · belts run east–west without end':'Climate bands are off · this preview exports nothing'):'Surface plan · procedural world beyond the border';}
  function overview(){const w=canvas.clientWidth,h=canvas.clientHeight,c=climateValues(),gutterLeft=82,gutterRight=182,size=Math.max(140,Math.min(w-gutterLeft-gutterRight-16,h-218));
    return M.climateOverview(design,c,{radius:Math.max(500,Math.round(M.overviewRadius(c)*overviewZoom/500)*500),size,left:Math.round(gutterLeft+Math.max(0,(w-gutterLeft-gutterRight-size)/2)),top:Math.round(Math.max(96,(h-size)/2-14))});}
  function line(x0,y0,x1,y1){ctx.beginPath();ctx.moveTo(x0,y0);ctx.lineTo(x1,y1);ctx.stroke();}
  function drawClimate(w,h){
    const c=climateValues(),on=climateOn(),o=overview(),{left,top,size}=o,font=(px,bold)=>`${bold?'bold ':''}${px}px Segoe UI, sans-serif`;
    caption();ctx.fillStyle='#12251f';ctx.fillRect(0,0,w,h);ctx.textBaseline='middle';
    if(M.climateIssues({...c,enabled:true}).length){ctx.fillStyle='#e9f0d8';ctx.textAlign='center';ctx.font=font(13);ctx.fillText('Fix the climate settings in the details panel to preview the belts.',w/2,h/2);$('cursor').textContent='Climate settings need attention';return;}
    ctx.save();ctx.beginPath();ctx.rect(left,top,size,size);ctx.clip();
    for(const r of o.regions){if(r.zone)ctx.fillStyle=zoneColor[r.zone];else{const g=ctx.createLinearGradient(0,r.y0,0,r.y1);g.addColorStop(0,zoneColor[r.north]);g.addColorStop(1,zoneColor[r.south]);ctx.fillStyle=g;}ctx.fillRect(left,r.y0,size,Math.max(.75,r.y1-r.y0));}
    ctx.strokeStyle='#ffffff38';ctx.lineWidth=1;    // hatched transitions: the game chooses one side per natural patch
    for(const r of o.regions)if(!r.zone&&r.y1-r.y0>3){const t=r.y1-r.y0;ctx.save();ctx.beginPath();ctx.rect(left,r.y0,size,t);ctx.clip();for(let x=left-t;x<left+size;x+=7)line(x,r.y1,x+t,r.y0);ctx.restore();}
    ctx.strokeStyle='#0d1d1726';for(const t of o.ticksX)line(t.px,top,t.px,top+size);for(const t of o.ticksZ)line(left,t.y,left+size,t.y);
    ctx.textAlign='left';
    for(const r of o.regions)if(r.zone&&r.y1-r.y0>18){const mid=(r.y0+r.y1)/2;ctx.fillStyle=zoneInk[r.zone];ctx.font=font(11,true);const name=zoneName[r.zone]+(r.strict?' · strict':'');if(r.y1-r.y0>36){ctx.fillText(name,left+12,mid-7);ctx.font=font(10);ctx.fillText(zoneHint[r.zone],left+12,mid+8);}else ctx.fillText(name,left+12,mid);}
    const k=o.core,a=o.authored,px=x=>left+(x-o.anchor.x+o.radius)*o.scale,py=z=>top+(z-o.anchor.z+o.radius)*o.scale;
    ctx.strokeStyle='#f7fbef';ctx.lineWidth=1.5;ctx.strokeRect(k.left,k.top,k.right-k.left,k.bottom-k.top);ctx.setLineDash([3,3]);ctx.strokeStyle='#f7fbef99';ctx.lineWidth=1;ctx.strokeRect(a.left,a.top,a.right-a.left,a.bottom-a.top);ctx.setLineDash([]);
    const extent=(design.grid.width-1)*design.grid.step,coreLabel=`${blocks(extent)}² design`;ctx.font=font(10);const lw=ctx.measureText(coreLabel).width+10;ctx.fillStyle='#17291fe8';ctx.fillRect(k.left,k.top-17,lw,15);ctx.fillStyle='#e9f0d8';ctx.fillText(coreLabel,k.left+5,k.top-9.5);
    if(design.town){ctx.fillStyle='#f2c986';ctx.fillRect(px(design.town.x)-3,py(design.town.z)-3,6,6);}
    ctx.fillStyle='#1a2a21';ctx.strokeStyle='#d6edac';ctx.lineWidth=2;ctx.beginPath();ctx.arc(o.anchor.px,o.anchor.py,6,0,Math.PI*2);ctx.fill();ctx.stroke();
    if(!on){ctx.fillStyle='#12251fb3';ctx.fillRect(left,top,size,size);const note='Climate bands are off · enable them in the details panel';ctx.font=font(12,true);const nw=Math.min(size-16,ctx.measureText(note).width+24);ctx.fillStyle='#17291ff2';ctx.fillRect(left+(size-nw)/2,top+14,nw,28);ctx.fillStyle='#e9f0d8';ctx.textAlign='center';ctx.fillText(note,left+size/2,top+28,nw-16);}
    ctx.restore();
    ctx.strokeStyle='#8aa171';ctx.lineWidth=1;ctx.strokeRect(left,top,size,size);
    // Axes: distances from spawn. North (−Z) is the top edge, west the left.
    const sparse=o.step*o.scale<64;ctx.font=font(10);ctx.fillStyle='#c3d3c1';
    ctx.textAlign='right';o.ticksZ.forEach((t,i)=>{if(sparse&&i%2)return;ctx.fillText(t.label,left-9,t.y);});
    ctx.textAlign='center';o.ticksX.forEach((t,i)=>{if(sparse&&i%2)return;ctx.fillText(t.label,t.px,top-10);});
    ctx.font=font(10,true);ctx.fillStyle='#e9f0d8';ctx.textAlign='center';ctx.fillText('↑ NORTH · −Z',left+size/2,top-28);ctx.fillText('↓ SOUTH · +Z',left+size/2,top+size+15);
    // Threshold lines and their labels (pushed apart so close belts stay readable).
    const marks=o.edges.filter(e=>e.visible).sort((p,q)=>p.y-q.y).map(e=>({e,ly:e.y}));let last=-Infinity;
    for(const m of marks){m.ly=Math.max(m.ly,last+30,top+8);last=m.ly;}let limit=top+size-8;for(let i=marks.length-1;i>=0;i--){marks[i].ly=Math.min(marks[i].ly,limit);limit=marks[i].ly-30;}
    ctx.globalAlpha=on?1:.55;
    for(const {e,ly} of marks){ctx.strokeStyle=e.strict?'#f06a5d':'#1d2a22cc';ctx.lineWidth=e.strict?2:1.25;ctx.setLineDash(e.strict?[]:[6,4]);line(left,e.y,left+size+8,e.y);ctx.setLineDash([]);ctx.strokeStyle='#8fa594';ctx.lineWidth=1;line(left+size+8,e.y,left+size+16,ly);
      ctx.textAlign='left';ctx.font=font(11,true);ctx.fillStyle=e.strict?'#f59a8f':'#e9f0d8';ctx.fillText(e.label,left+size+20,ly-7);ctx.font=font(10);ctx.fillStyle='#b4c7b9';ctx.fillText(`${e.distance} · Z ${blocks(e.z)}`,left+size+20,ly+7);}
    ctx.globalAlpha=1;
    if(marks.length<o.edges.length){ctx.textAlign='left';ctx.font=font(10);ctx.fillStyle='#f59a8f';ctx.fillText('More belts lie beyond this view · Fit map',left+size+20,top+size+16);}
    const scale=o.step*o.scale;$('scale').style.width=`${Math.max(30,scale)}px`;$('scale').textContent=`${blocks(o.step)} blocks`;
    if(pointer&&pointer.x>=left&&pointer.x<=left+size&&pointer.y>=top&&pointer.y<=top+size){
      const p=M.overviewWorld(o,pointer.x,pointer.y),bx=Math.floor(p.x),bz=Math.floor(p.z),s=bz-design.spawn.z,z=M.climateZoneAt(c,s);
      const where=s===0?'level with spawn':`${blocks(Math.abs(s))} ${s<0?'N':'S'} of spawn`,zone=z.blend?`${zoneName[z.blend.from]} → ${zoneName[z.blend.to]} transition, ${Math.round(z.blend.progress*100)}% across`:zoneName[z.zone]+(z.strict?' (strict)':'');
      $('cursor').textContent=`X ${blocks(bx)}  ·  Z ${blocks(bz)}  ·  ${where}  ·  ${on?'':'Off · '}${zone}`;
    } else $('cursor').textContent='Move across the overview to inspect a belt';
  }
  function chooseTool(next){if(view==='climate'&&next!=='pan')setView('terrain');tool=next;document.querySelectorAll('[data-tool]').forEach(b=>{const on=b.dataset.tool===tool;b.classList.toggle('selected',on);b.setAttribute('aria-pressed',String(on));});$('active-tool').textContent=labels[tool];canvas.style.cursor=tool==='pan'?'grab':'crosshair';requestDraw();}
  function fillChoices(){
    const old=$('biome').value,filter=$('biome-filter').value.trim().toLowerCase();$('biome').replaceChildren();
    for(const id of content().biomes.filter(id=>id.toLowerCase().includes(filter))){const option=new Option(`${pretty(id)}${id.startsWith('minecraft:')?'':' · '+id.split(':')[0]}`,id);$('biome').add(option);}
    if([...$('biome').options].some(o=>o.value===old))$('biome').value=old;else $('biome').selectedIndex=0;
    $('biome-id').textContent=$('biome').value||'No matching biomes';
  }
  function updateCatalog(){fillChoices();const old=$('structure').value;$('structure').replaceChildren();for(const s of content().structures)$('structure').add(new Option(`${pretty(s.id)} · ${s.id.split(':')[0]}`,s.id));if([...$('structure').options].some(o=>o.value===old))$('structure').value=old;
    $('catalog-state').textContent=catalog?'Mod catalog connected':'Planning mode';$('catalog-dot').classList.toggle('ready',Boolean(catalog));$('catalog-detail').textContent=catalog?`${catalog.biomes.length} biomes · ${catalog.structures.length} structures · Minecraft ${catalog.minecraft}`:'Load your mod catalog to paint installed biomes and export a playable world.';structureDetail();refresh();
  }
  function structureDetail(){const s=content().structures.find(a=>a.id===$('structure').value);$('structure-detail').textContent=s?`Paint near the marker with: ${s.biomes.slice(0,4).map(pretty).join(', ')}${s.biomes.length>4?'…':''}. Native terrain rules still apply.`:'No registered structures in this catalog.';}
  function loadCatalog(value){const errors=M.validateCatalog(value);if(errors.length)throw new Error(errors.join(' '));catalog=value;updateCatalog();notify('Installed biomes and structures are ready.');}
  function landmarks(){const list=$('landmarks');list.replaceChildren();const items=[{p:design.spawn,label:'Spawn',kind:'spawn'}];if(design.town)items.push({p:design.town,label:'Walled city',kind:'town'});design.structures.forEach((p,i)=>items.push({p,label:pretty(p.id),kind:i}));$('landmark-count').textContent=items.length;
    items.forEach(item=>{const row=document.createElement('div');row.className='landmark';const button=document.createElement('button');button.className='locate';button.textContent=item.label;const sub=document.createElement('small');sub.textContent=`X ${item.p.x} · Z ${item.p.z}`;button.append(sub);button.onclick=()=>{const at=screen(item.p.x,item.p.z);pan.x+=canvas.clientWidth/2-at.x;pan.y+=canvas.clientHeight/2-at.y;requestDraw();};row.append(button);
      if(item.kind!=='spawn'){const remove=document.createElement('button');remove.textContent='×';remove.className='remove';remove.setAttribute('aria-label',`Remove ${item.label}`);remove.onclick=()=>{remember();if(item.kind==='town')design.town=null;else design.structures.splice(item.kind,1);refresh();};row.append(remove);}list.append(row);
    });
  }
  function validation(){const errors=M.validate(design,catalog,true);$('validation-title').textContent=errors.length?'Before you export':'Ready to export';$('validation').replaceChildren();errors.slice(0,6).forEach(error=>{const li=document.createElement('li');li.textContent=error;$('validation').append(li);});if(errors.length>6){const li=document.createElement('li');li.textContent=`And ${errors.length-6} more issue(s). Fix the above first.`;$('validation').append(li);}return errors;}
  function refresh(){climatePending={};landmarks();validation();syncClimate();buildPreview();caption();}
  function place(kind,x,z){if(!Number.isFinite(x)||!Number.isFinite(z)){notify('Enter valid X and Z coordinates.');return;}let p={x:Math.round(x),z:Math.round(z)};if(kind==='town')p=M.snapTown(p.x,p.z,content().townSize||192);if(kind==='structure')p={x:Math.floor(x/16)*16+8,z:Math.floor(z/16)*16+8,id:$('structure').value};if(M.weight(design,p.x,p.z)!==1){notify('Place landmarks inside the dotted border.');return;}if(kind==='structure'&&(!p.id||design.structures.length>=32)){notify('Select a structure; a design supports up to 32 markers.');return;}
    remember();if(kind==='structure')design.structures.push(p);else design[kind]=p;refresh();notify(kind==='town'?'Walled city placed. Its builder will level the footprint.':kind==='structure'?'Structure start chunk pinned. Check its biome requirements.':'Spawn moved.');
  }
  function dab(p){if(M.weight(design,p.x,p.z)===0)return;if(tool==='biome'&&!$('biome').value)return;M.paint(design,p.x,p.z,tool,Number($('radius').value),Number($('strength').value),Number($('target').value),$('biome').value);}
  canvas.addEventListener('pointerdown',e=>{if(e.button!==0&&e.button!==1)return;e.preventDefault();canvas.focus();if(view==='climate')return;pointer=location(e);canvas.setPointerCapture(e.pointerId);const p=world(pointer.x,pointer.y);if(tool==='pan'||space||e.button===1){stroke={mode:'pan',last:pointer};return;}if(['spawn','town','structure'].includes(tool)){place(tool,p.x,p.z);return;}remember();stroke={mode:'paint',last:p};dab(p);buildPreview();});
  canvas.addEventListener('pointermove',e=>{pointer=location(e);if(stroke?.mode==='pan'){pan.x+=pointer.x-stroke.last.x;pan.y+=pointer.y-stroke.last.y;stroke.last=pointer;}else if(stroke?.mode==='paint'){const p=world(pointer.x,pointer.y),dist=Math.hypot(p.x-stroke.last.x,p.z-stroke.last.z),count=Math.min(256,Math.ceil(dist/Math.max(8,design.grid.step/2)));for(let i=1;i<=count;i++)dab({x:stroke.last.x+(p.x-stroke.last.x)*i/count,z:stroke.last.z+(p.z-stroke.last.z)*i/count});stroke.last=p;buildPreview();}requestDraw();});
  function endStroke(){if(stroke?.mode==='paint')validation();stroke=null;}
  canvas.addEventListener('pointerup',endStroke);canvas.addEventListener('pointercancel',endStroke);canvas.addEventListener('lostpointercapture',endStroke);canvas.addEventListener('pointerleave',()=>{if(!stroke)pointer=null;requestDraw();});
  canvas.addEventListener('wheel',e=>{e.preventDefault();if(view==='climate'){overviewZoom=Math.max(.125,Math.min(8,overviewZoom*Math.exp(e.deltaY*.001)));requestDraw();return;}const p=location(e),before=world(p.x,p.y);zoom=Math.max(.5,Math.min(8,zoom*Math.exp(-e.deltaY*.001)));const after=screen(before.x,before.z);pan.x+=p.x-after.x;pan.y+=p.y-after.y;requestDraw();},{passive:false});
  document.querySelectorAll('[data-tool]').forEach(b=>b.onclick=()=>chooseTool(b.dataset.tool));document.querySelectorAll('[data-view]').forEach(b=>b.onclick=()=>setView(b.dataset.view));
  for(const id of ['radius','strength','target'])$(id).oninput=()=>{$(id+'-value').textContent=$(id).value+(id==='radius'?' blocks':'');requestDraw();};
  $('undo').onclick=()=>restore(history,future);$('redo').onclick=()=>restore(future,history);$('fit').onclick=()=>{zoom=1;pan={x:0,y:0};overviewZoom=1;requestDraw();};$('contours').onchange=buildPreview;
  $('biome-filter').oninput=fillChoices;$('biome').onchange=()=>{$('biome-id').textContent=$('biome').value;chooseTool('biome');};$('structure').onchange=()=>{structureDetail();chooseTool('structure');};
  $('place-coordinates').onclick=()=>place($('coordinate-kind').value,Number($('coordinate-x').value),Number($('coordinate-z').value));
  $('name').onchange=()=>{remember();design.name=$('name').value.trim()||'Untitled world';$('name').value=design.name;validation();};
  document.addEventListener('keydown',e=>{if(/INPUT|SELECT|TEXTAREA/.test(e.target.tagName)||$('guide').open)return;if(e.code==='Space'){space=true;e.preventDefault();}if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='z'){e.preventDefault();if(e.shiftKey)restore(future,history);else restore(history,future);}if(e.key==='Escape'){stroke=null;space=false;chooseTool('pan');}});document.addEventListener('keyup',e=>{if(e.code==='Space')space=false;});window.addEventListener('blur',()=>{space=false;endStroke();});
  function download(name,data,type){const url=URL.createObjectURL(new Blob([data],{type})),a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),10000);}
  const fileName=()=>design.name.toLowerCase().replace(/[^a-z0-9]+/g,'-').replace(/^-|-$/g,'')||'jugcraft-world';
  $('save').onclick=()=>{download(fileName()+'.jugcraft.json',JSON.stringify(design,null,2),'application/json');dirty=false;notify('Design saved. Keep this file to edit it later.');};
  $('export').onclick=()=>{const errors=validation();if(errors.length){notify(errors[0]);return;}try{download(fileName()+'.zip',M.zip(M.packFiles(design,catalog)),'application/zip');notify('World pack exported. Add it when creating a new world.');}catch(e){notify(e.message);}};
  let importKind='design';function pick(kind){importKind=kind;$('file').value='';$('file').click();}$('open').onclick=()=>pick('design');$('catalog').onclick=()=>pick('catalog');$('file').onchange=async()=>{const file=$('file').files[0];if(!file)return;try{if(file.size>8*1024*1024)throw new Error('Choose a JSON file smaller than 8 MB.');const value=JSON.parse(await file.text());if(importKind==='catalog')loadCatalog(value);else{const errors=M.validateShape(value);if(errors.length)throw new Error(errors[0]);remember();design=M.copy(value);$('name').value=design.name;zoom=1;pan={x:0,y:0};refresh();notify('Design opened.');}}catch(e){notify('Could not open file: '+e.message);}};
  $('climate-enabled').onchange=()=>{remember();const on=$('climate-enabled').checked;if(!design.climate)design.climate=M.createClimate();design.climate.enabled=on;climatePending={};climateChanged();if(on&&view!=='climate')notify('Climate bands on. Choose Climate above the map to see the belts.');};
  for(const f of climateFields)$('climate-'+f).onchange=()=>{if(!climateOn())return;const text=$('climate-'+f).value.trim(),n=Number(text);
    if(text===''||!Number.isSafeInteger(n)){climatePending[f]=`${M.climateLabels[f]}: enter a whole number. The design keeps ${blocks(design.climate[f]??0)}.`;syncClimate();return;}
    delete climatePending[f];if(design.climate[f]!==n){remember();design.climate[f]=n;}climateChanged();};
  $('climate-view').onclick=()=>{setView('climate');canvas.scrollIntoView({block:'nearest'});};
  $('help').onclick=()=>$('guide').showModal();document.querySelectorAll('.close').forEach(b=>b.onclick=()=>$('guide').close());window.addEventListener('beforeunload',e=>{if(dirty){e.preventDefault();e.returnValue='';}});
  new ResizeObserver(requestDraw).observe(canvas);chooseTool('raise');
  if(globalThis.JUGCRAFT_CATALOG){try{loadCatalog(globalThis.JUGCRAFT_CATALOG);}catch(e){notify('Catalog could not load: '+e.message);updateCatalog();}}else updateCatalog();
})();
