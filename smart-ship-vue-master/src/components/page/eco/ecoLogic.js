let _started=false;
export function runEcoLogic(){
  if(_started) return;
  if(typeof window.Chart === 'undefined'){
    console.error('[eco] Chart.js not loaded');
  }
  _started=true;
  // expose handlers referenced by inline on* attributes in the ported markup
  // (function declarations below are hoisted, so they are already defined here)
  Object.assign(window, {closeDrawer, zoomMap, layoutMap, acknowledgeAlert, selectTrendZone, selectZone, openDrawer, updatePlayhead});
/* ---------------- CLOCK ---------------- */
function tickClock(){
  const el = document.getElementById('utcClock');
  if(!el) return;
  const now = new Date();
  el.textContent = now.toISOString().substr(11,8) + " UTC";
}
if(window.__ecoClock) clearInterval(window.__ecoClock);
window.__ecoClock = setInterval(tickClock,1000); tickClock();

/* ---------------- TAB SWITCH ---------------- */
var ECO_VIEWS = ['dashboard','analytics','activities','map','sensors','thermal'];

// activate a view's DOM (no routing side-effect)
function ecoActivateView(view){
  if(ECO_VIEWS.indexOf(view) < 0) view = 'dashboard';
  var btn = document.querySelector('nav.tabs button[data-view="'+view+'"]');
  document.querySelectorAll('nav.tabs button').forEach(function(b){ b.classList.remove('active'); });
  if(btn) btn.classList.add('active');
  document.querySelectorAll('.view').forEach(function(v){ v.classList.remove('active'); });
  var _view = document.getElementById('view-'+view);
  if(!_view) return;
  _view.classList.add('active');
  if(view === 'map'){ requestAnimationFrame(layoutMap); }
  // Chart.js canvases created while their tab was display:none have zero size;
  // resize them once the tab is actually visible.
  requestAnimationFrame(function(){
    if(!window.Chart || !window.Chart.getChart) return;
    _view.querySelectorAll('canvas').forEach(function(c){
      var inst = window.Chart.getChart(c);
      if(inst) inst.resize();
    });
  });
}
window.ecoActivateView = ecoActivateView;
// NOTE: tab-button clicks are handled by EcoApp.vue via event delegation on
// the stable .eco-root element (so they survive any v-html re-render) and it
// also syncs the Vue route. See EcoApp.vue mounted().

/* ---------------- DATA ---------------- */
const kpis = [
  {name:'PPE Compliance — Engine Room', score:83, delta:0, color:'var(--attention)'},
  {name:'Bridge Watch Level Compliance', score:85, delta:13, color:'var(--attention)'},
  {name:'Bridge Attendance While Underway [%]', score:87, delta:-4, color:'var(--attention)'},
  {name:'Engine Room Watch Performance', score:88, delta:5, color:'var(--attention)'},
  {name:'Gangway Watch Compliance', score:91, delta:5, color:'var(--routine)'},
  {name:'PPE Compliance', score:92, delta:-2, color:'var(--routine)'},
  {name:'Generator Space Watch Performance', score:92, delta:18, color:'var(--routine)'},
  {name:'Mooring Watch Compliance', score:92, delta:7, color:'var(--routine)'},
];

const kpiList = document.getElementById('kpiList');
kpis.forEach(k=>{
  const deltaClass = k.delta>0?'up':(k.delta<0?'down':'flat');
  const deltaTxt = k.delta>0? '▲ '+k.delta : (k.delta<0? '▼ '+Math.abs(k.delta) : '— 0');
  const prev = Math.max(0, Math.min(100, k.score - k.delta));
  const barColor = k.score>=90 ? 'var(--routine)' : (k.score>=75 ? 'var(--attention)' : 'var(--alert)');
  kpiList.insertAdjacentHTML('beforeend', `
    <div class="kpi-row">
      <div class="kpi-top">
        <span class="kpi-name">${k.name}</span>
        <span class="kpi-score-wrap">
          <span class="delta ${deltaClass}">${deltaTxt}</span>
          <span class="kpi-score">${k.score}</span>
        </span>
      </div>
      <div class="kpi-bullet">
        <div class="bullet-bands">
          <div class="band zone-red" style="width:75%"></div>
          <div class="band zone-amber" style="width:15%"></div>
          <div class="band zone-green" style="width:10%"></div>
        </div>
        <div class="bullet-bar" style="width:${k.score}%;background:${barColor};"></div>
        <div class="bullet-marker" style="left:${prev}%;" title="Previous period: ${prev}"></div>
      </div>
      <div class="kpi-foot">
        <span style="position:absolute;left:0;">0</span>
        <span style="position:absolute;left:75%;transform:translateX(-50%);">75</span>
        <span style="position:absolute;left:90%;transform:translateX(-50%);">90</span>
        <span style="position:absolute;right:0;">100</span>
      </div>
    </div>
  `);
});

const spotlights = [
  {time:'19:59:00, 21 Mar 2026', title:'Drill / Training', loc:'A103 — Under Way', sev:'attention'},
  {time:'09:13:19, 20 Mar 2026', title:'Snapback Risk', loc:'E103 — Moored', sev:'alert'},
];
const sevColorMap = {routine:'var(--routine)', attention:'var(--attention)', alert:'var(--alert)'};
const spotlightList = document.getElementById('spotlightList');
spotlights.forEach(s=>{
  spotlightList.insertAdjacentHTML('beforeend', `
    <div class="spot-card">
      <div class="spot-media">
        <div class="grain"></div>
        <div class="marker"></div>
        <div class="sev-pill" style="background:${sevColorMap[s.sev]};">${s.sev.toUpperCase()}</div>
        <div class="ts">0:30 / 0:30</div>
      </div>
      <div class="spot-body">
        <div class="spot-time">${s.time}</div>
        <div class="spot-title">${s.title}</div>
        <div class="spot-loc">${s.loc}</div>
      </div>
    </div>
  `);
});

const sevSnapshot = [
  {mod:'BRIDGE', routine:1120, attention:14, alert:1},
  {mod:'SAFETY', routine:640, attention:9, alert:0},
  {mod:'TECHNICAL', routine:410, attention:5, alert:1},
  {mod:'SECURITY', routine:280, attention:3, alert:1},
  {mod:'MARPOL', routine:96, attention:1, alert:0},
];
const sevStrip = document.getElementById('sevStrip');
sevSnapshot.forEach(s=>{
  const total = s.routine+s.attention+s.alert;
  sevStrip.insertAdjacentHTML('beforeend', `
    <div class="sev-tile">
      <div class="mod-label">${s.mod}</div>
      <div class="mod-count">${total.toLocaleString()}</div>
      <div class="mini-track">
        <div style="width:${s.routine/total*100}%;background:var(--routine);"></div>
        <div style="width:${s.attention/total*100}%;background:var(--attention);"></div>
        <div style="width:${s.alert/total*100}%;background:var(--alert);"></div>
      </div>
    </div>
  `);
});

/* ---------------- MODULE TREND CHART ---------------- */
const months = ['Dec','Jan','Feb','Mar'];
const moduleData = {
  Overall: [86,87,89,91],
  Bridge: [88,86,90,94],
  Safety: [86,88,90,94],
  Technical: [88,91,90,91],
  Security: [84,84,88,94],
  MARPOL: [90,92,95,100],
};
const moduleColors = {
  Overall:'#141B2E', Bridge:'#2E7BFF', Safety:'#1FA971', Technical:'#F0A020',
  Security:'#9B5DE5', MARPOL:'#12B3AA'
};
new Chart(document.getElementById('moduleTrendChart'), {
  type:'line',
  data:{
    labels:months,
    datasets: Object.keys(moduleData).map(k=>({
      label:k,
      data:moduleData[k],
      borderColor:moduleColors[k],
      backgroundColor:moduleColors[k],
      borderWidth: k==='Overall'?2:2.2,
      borderDash: k==='Overall'?[5,4]:[],
      pointRadius:0,
      tension:0.4,
    }))
  },
  options:{
    plugins:{legend:{display:false}},
    scales:{
      y:{min:80,max:100,grid:{color:'#EEF1F8'},ticks:{font:{family:'IBM Plex Mono',size:10}}},
      x:{grid:{display:false},ticks:{font:{family:'IBM Plex Mono',size:10}}}
    }
  }
});

/* ---------------- ANALYTICS ---------------- */
const scoreCardData = [
  {label:'Overall', score:91, delta:10, color:'#141B2E'},
  {label:'Bridge', score:84, delta:19, color:'#2E7BFF'},
  {label:'Safety', score:89, delta:3, color:'#1FA971'},
  {label:'Technical', score:100, delta:13, color:'#F0A020'},
  {label:'Security', score:78, delta:-2, color:'#9B5DE5'},
  {label:'MARPOL', score:96, delta:6, color:'#12B3AA'},
];
const scoreCards = document.getElementById('scoreCards');
scoreCardData.forEach((s,i)=>{
  scoreCards.insertAdjacentHTML('beforeend', `
    <div class="score-card ${i===0?'on':''}" style="--sc:${s.color}" data-label="${s.label}">
      <div class="lbl"><span class="dot"></span>${s.label}</div>
      <div class="val">${s.score}</div>
      <div class="trend" style="color:${s.delta>=0?'#1FA971':'#E5484D'};">${s.delta>=0?'▲':'▼'} ${Math.abs(s.delta)} vs prior period</div>
    </div>
  `);
});

let analyticsChart;
function renderAnalyticsChart(label){
  const ctx = document.getElementById('analyticsChart');
  const smooth = document.getElementById('smoothToggle').checked;
  const raw = moduleData[label] || moduleData.Overall;
  const data = smooth ? raw.map((v,i,a)=> i===0? v : Math.round((v+a[i-1])/2)) : raw;
  document.getElementById('chartTitle').textContent = label+' Performance — Trend';
  if(analyticsChart) analyticsChart.destroy();
  analyticsChart = new Chart(ctx, {
    type:'line',
    data:{ labels:months, datasets:[{
      label, data,
      borderColor: moduleColors[label] || '#141B2E',
      backgroundColor: (moduleColors[label] || '#141B2E')+'22',
      fill:true, tension:0.4, pointRadius:3,
      pointBackgroundColor: moduleColors[label] || '#141B2E',
    }]},
    options:{
      plugins:{legend:{display:false}},
      scales:{
        y:{min:60,max:100,grid:{color:'#EEF1F8'},ticks:{font:{family:'IBM Plex Mono',size:11}}},
        x:{grid:{display:false},ticks:{font:{family:'IBM Plex Mono',size:11}}}
      }
    }
  });
}
renderAnalyticsChart('Overall');
scoreCards.addEventListener('click', e=>{
  const card = e.target.closest('.score-card');
  if(!card) return;
  document.querySelectorAll('.score-card').forEach(c=>c.classList.remove('on'));
  card.classList.add('on');
  renderAnalyticsChart(card.dataset.label);
});
document.getElementById('smoothToggle').addEventListener('change', ()=>{
  const active = document.querySelector('.score-card.on');
  renderAnalyticsChart(active ? active.dataset.label : 'Overall');
});

/* ---------------- ANALYTICS ALARM LOG ---------------- */
const modColors = {Bridge:'#2E7BFF', Safety:'#1FA971', Technical:'#F0A020', Security:'#9B5DE5', MARPOL:'#12B3AA', Thermal:'#D9480F'};
const modBg = {Bridge:'#E7F0FF', Safety:'#E5F7EE', Technical:'#FCF0DC', Security:'#F3E9FE', MARPOL:'#E1F7F5', Thermal:'#FDEBDD'};
const analyticsAlarmLog = [
  {t:'25 Mar 07:14:52', mod:'MARPOL', desc:'Unauthorised Use of OWS in Restricted Zone — GPS-flagged position', where:'L103 · 5.316°N, 3.982°W', sev:'alert'},
  {t:'25 Mar 06:39:44', mod:'Bridge', desc:'Unattended Bridge while Underway', where:'L103 · Wheelhouse', sev:'alert'},
  {t:'25 Mar 06:28:05', mod:'Safety', desc:'Missing Helmet on Main Deck', where:'L103 · Main Deck', sev:'attention'},
  {t:'24 Mar 23:41:12', mod:'Security', desc:'Unattended Aft Mooring Station', where:'L103 · Mooring Station', sev:'alert'},
  {t:'24 Mar 22:18:30', mod:'MARPOL', desc:'Unauthorised Access — Man on Deck Near Railings (Night Hours) — CCTV', where:'L103 · Main Deck Railings', sev:'alert'},
  {t:'24 Mar 19:45:00', mod:'Bridge', desc:'Bridge Lighting at Night while Underway', where:'L103 · Wheelhouse', sev:'attention'},
  {t:'24 Mar 15:12:37', mod:'Technical', desc:'Inactivity in Main Engine Space', where:'L103 · Engine Room', sev:'attention'},
  {t:'21 Mar 19:59:00', mod:'Safety', desc:'Snapback Risk during Mooring Operation', where:'E103 · Mooring Deck', sev:'alert'},
  {t:'21 Mar 09:13:19', mod:'Security', desc:'Unattended Gangway Area (Port)', where:'E103 · Gangway', sev:'attention'},
  {t:'20 Mar 04:52:08', mod:'MARPOL', desc:'Activity at Enclosed Space Entrance', where:'L103 · Deck', sev:'attention'},
  {t:'19 Mar 22:07:51', mod:'Technical', desc:'Non-compliant Watch Level in Restricted Visibility', where:'L103 · Wheelhouse', sev:'alert'},
];
const LOG_MONTHS = {Jan:0,Feb:1,Mar:2,Apr:3,May:4,Jun:5,Jul:6,Aug:7,Sep:8,Oct:9,Nov:10,Dec:11};
function parseLogTime(str){
  // "25 Mar 06:39:44" or "25 Mar 06:39"
  const parts = str.split(' ');
  const day = parseInt(parts[0],10);
  const month = (LOG_MONTHS[parts[1]] != null ? LOG_MONTHS[parts[1]] : 2);
  const timeParts = (parts[2]||'00:00:00').split(':').map(n=>parseInt(n,10));
  return new Date(2026, month, day, timeParts[0]||0, timeParts[1]||0, timeParts[2]||0);
}
function renderAnalyticsAlarmLog(){
  const tbody = document.getElementById('analyticsAlarmRows');
  tbody.innerHTML = '';
  // Pull thermal-camera alarms into the same feed so any fire-risk anomaly shows up here too
  const thermalAsLog = (typeof thermAlarms !== 'undefined' ? thermAlarms : []).map(a=>({
    t:a.time+':00', mod:'Thermal', desc:a.desc+` — ${a.temp.toFixed(1)}°C`, where:'L103 · '+a.zone, sev:a.sev, acked:a.acked
  }));
  const combined = [...analyticsAlarmLog, ...thermalAsLog].sort((a,b)=> parseLogTime(b.t)-parseLogTime(a.t));
  combined.forEach(a=>{
    const isAlert = a.sev==='alert' && !a.acked;
    tbody.insertAdjacentHTML('beforeend', `
      <tr class="${isAlert?'blink-row':''}">
        <td class="mono-cell${isAlert?' blink-alert':''}" style="${isAlert?'color:var(--alert);font-weight:700;':''}">${a.t}</td>
        <td><span class="mod-tag" style="background:${modBg[a.mod]};color:${modColors[a.mod]};">${a.mod}</span></td>
        <td>${a.desc}</td>
        <td class="mono-cell">${a.where}</td>
        <td><span class="sev-dot-lbl"><span class="sev-dot${isAlert?' blink-alert':''}" style="background:${sevColorMap[a.sev]}"></span>${a.sev.charAt(0).toUpperCase()+a.sev.slice(1)}</span></td>
      </tr>
    `);
  });
  document.getElementById('alarmLogCount').textContent =
    combined.filter(a=>a.sev==='alert').length + ' active alert' + (combined.filter(a=>a.sev==='alert').length===1?'':'s');
}


/* ---------------- ACTIVITIES TABLE ---------------- */
const activities = [
  {sev:'alert', mod:'MARPOL', desc:'Unauthorised Use of OWS in Restricted Zone', comp:'5.316°N, 3.982°W', start:'25 Mar 07:14', dur:'2m 40s', status:'To Do', tags:['GPS-flagged'], acked:false},
  {sev:'alert', mod:'Bridge', desc:'Unattended Bridge while Underway', comp:'Wheelhouse', start:'25 Mar 06:39', dur:'5m 16s', status:'To Do', tags:[], acked:false},
  {sev:'attention', mod:'Safety', desc:'Missing Helmet on Main Deck', comp:'Deck', start:'25 Mar 06:28', dur:'6m 55s', status:'In Progress', tags:['Reviewed'], acked:false},
  {sev:'attention', mod:'Technical', desc:'Inactivity in Main Engine Space', comp:'Main Engine Room', start:'25 Mar 06:15', dur:'13m 59s', status:'To Do', tags:[], acked:false},
  {sev:'routine', mod:'Bridge', desc:'Bridge Watch — Routine Compliance', comp:'Wheelhouse', start:'25 Mar 06:06', dur:'6m 51s', status:'—', tags:[], acked:false},
  {sev:'attention', mod:'Security', desc:'Unattended Gangway Area (Port)', comp:'Gangway', start:'25 Mar 05:58', dur:'4m 15s', status:'Resolved', tags:['Reviewed'], acked:false},
  {sev:'alert', mod:'Security', desc:'Unattended Aft Mooring Station', comp:'Mooring Station', start:'24 Mar 23:41', dur:'9m 02s', status:'In Progress', tags:['Follow-up'], acked:false},
  {sev:'alert', mod:'MARPOL', desc:'Unauthorised Access — Man on Deck Near Railings (Night Hours)', comp:'Main Deck Railings', start:'24 Mar 22:18', dur:'4m 05s', status:'To Do', tags:['CCTV'], acked:false},
  {sev:'attention', mod:'Bridge', desc:'Bridge Lighting at Night while Underway', comp:'Wheelhouse', start:'24 Mar 19:45', dur:'23h 19m', status:'In Progress', tags:['Weather-related'], acked:false},
  {sev:'routine', mod:'Technical', desc:'Generator Space Watch — Routine', comp:'Engine Room', start:'24 Mar 14:20', dur:'8m 40s', status:'—', tags:[], acked:false},
  {sev:'attention', mod:'MARPOL', desc:'Activity at Enclosed Space Entrance', comp:'Deck', start:'24 Mar 11:02', dur:'3m 10s', status:'To Do', tags:[], acked:false},
];

const zoneCompMap = {'Main Engine':'Main Engine Room','Generator No.1':'Generator Room','Air Compressor Room':'Air Compressor Room','Purifier Room':'Purifier Room','Auxiliary Boiler':'Boiler Room'};
function renderActivities(){
  const modF = document.getElementById('moduleFilter').value;
  const compF = document.getElementById('compartmentFilter').value;
  const onSevs = [...document.querySelectorAll('.sev-chip.on')].map(c=>c.dataset.sev);
  const rows = document.getElementById('activityRows');
  rows.innerHTML='';

  const localItems = activities.map((a,i)=>({ ...a, _src:'local', _idx:i }));
  const thermItems = (typeof thermAlarms!=='undefined' ? thermAlarms : []).map((a,i)=>({
    sev:a.sev, mod:'Technical',
    desc:'[Thermal] '+a.desc+` (${a.temp.toFixed(1)}°C)`,
    comp: zoneCompMap[a.zone] || a.zone,
    start:a.time, dur:a.dur,
    status: a.acked ? 'Acknowledged' : a.status,
    tags:['Thermal'],
    acked:a.acked,
    _src:'thermal', _idx:i,
  }));

  [...localItems, ...thermItems]
    .filter(a=> onSevs.includes(a.sev))
    .filter(a=> modF==='all' || a.mod===modF)
    .filter(a=> compF==='all' || a.comp===compF)
    .forEach(a=>{
      const isAlert = a.sev==='alert' && !a.acked;
      const rowData = {sev:a.sev, mod:a.mod, desc:a.desc, comp:a.comp, start:a.start, dur:a.dur, status:a.status, tags:a.tags};
      rows.insertAdjacentHTML('beforeend', `
        <tr class="${isAlert?'blink-row':''}" onclick='openDrawer(${JSON.stringify(rowData).replace(/'/g,"&apos;")})'>
          <td><span class="sev-dot-lbl"><span class="sev-dot${isAlert?' blink-alert':''}" style="background:${sevColorMap[a.sev]}"></span>${a.sev.charAt(0).toUpperCase()+a.sev.slice(1)}</span></td>
          <td><span class="mod-tag" style="background:${modBg[a.mod]};color:${modColors[a.mod]};">${a.mod}</span></td>
          <td>${a.desc}</td>
          <td class="mono-cell">${a.comp}</td>
          <td class="mono-cell">${a.start}</td>
          <td class="mono-cell">${a.dur}</td>
          <td>${isAlert ? `<button class="ack-btn" onclick="event.stopPropagation(); acknowledgeAlert('${a._src}',${a._idx})">Ack</button>` : `<span class="status-pill">${a.status}</span>`}</td>
          <td>${a.tags.map(t=>`<span class="tag-pill">${t}</span>`).join('')}</td>
        </tr>
      `);
    });
}
function acknowledgeAlert(src, idx){
  if(src==='local'){ activities[idx].acked = true; }
  else if(src==='thermal'){ thermAlarms[idx].acked = true; }
  renderActivities();
  if(src==='thermal'){
    if(typeof renderThermAlarms==='function') renderThermAlarms();
    if(typeof renderAnalyticsAlarmLog==='function') renderAnalyticsAlarmLog();
  }
}
document.querySelectorAll('#view-activities .sev-chip').forEach(c=>{
  c.addEventListener('click', ()=>{ c.classList.toggle('on'); renderActivities(); });
});
document.getElementById('moduleFilter').addEventListener('change', renderActivities);
document.getElementById('compartmentFilter').addEventListener('change', renderActivities);

/* ---------------- DRAWER ---------------- */
function openDrawer(a){
  document.getElementById('drawerSev').textContent = a.sev.charAt(0).toUpperCase()+a.sev.slice(1);
  document.getElementById('drawerSev').style.background = sevColorMap[a.sev];
  document.getElementById('drawerSev').style.color = '#fff';
  document.getElementById('drawerSev').style.borderColor = 'transparent';
  document.getElementById('drawerTitle').textContent = a.desc;
  document.getElementById('drawerLoc').textContent = `${a.mod} — ${a.comp} · Vessel L103`;
  document.getElementById('overlay').classList.add('show');
  document.getElementById('drawer').classList.add('show');
}
function closeDrawer(){
  document.getElementById('overlay').classList.remove('show');
  document.getElementById('drawer').classList.remove('show');
}
document.getElementById('overlay').addEventListener('click', closeDrawer);
document.querySelectorAll('.wf-btn').forEach(b=>{
  b.addEventListener('click', ()=>{
    if(b.textContent.includes('Add Tag')) return;
    b.parentElement.querySelectorAll('.wf-btn').forEach(x=>{ if(!x.textContent.includes('Add Tag')) x.classList.remove('on'); });
    b.classList.add('on');
  });
});

/* ---------------- REAL MAP: lat/lon projection ---------------- */
const MAP_NATURAL_W = 2000, MAP_NATURAL_H = 1000; // self-contained generated world map
let mapZoom = 3.1;
const mapPoints = {
  abidjan:  {lat:51.92, lon:4.48},    // Rotterdam (origin)
  buchanan: {lat:40.71, lon:-74.00},  // New York (destination)
  vessel:   {lat:46.32, lon:-34.76},  // mid-Atlantic, open sea — well clear of any coastline
};
function lonToX(lon){ return (lon+180)/360*MAP_NATURAL_W; }
function latToY(lat){ return (90-lat)/180*MAP_NATURAL_H; }

function layoutMap(){
  const shell = document.getElementById('mapShell');
  const img = document.getElementById('mapImg');
  if(!shell || !img.complete || img.naturalWidth===0) return;
  const w = shell.clientWidth, h = shell.clientHeight;
  if(w===0||h===0) return;

  // Center the view on the vessel's current position
  const center = mapPoints.vessel;
  const cx = lonToX(center.lon)*mapZoom;
  const cy = latToY(center.lat)*mapZoom;

  img.style.width = (MAP_NATURAL_W*mapZoom)+'px';
  img.style.height = (MAP_NATURAL_H*mapZoom)+'px';
  const offsetX = w/2 - cx;
  const offsetY = h/2 - cy;
  img.style.transform = `translate(${offsetX}px, ${offsetY}px)`;

  function place(id, pt){
    const el = document.getElementById(id);
    if(!el) return;
    const x = offsetX + lonToX(pt.lon)*mapZoom;
    const y = offsetY + latToY(pt.lat)*mapZoom;
    el.style.left = x+'px';
    el.style.top = y+'px';
    return {x,y};
  }
  place('portAbidjan', mapPoints.abidjan);
  const aLbl = document.getElementById('portAbidjanLbl');
  const aPos = {x:offsetX+lonToX(mapPoints.abidjan.lon)*mapZoom, y:offsetY+latToY(mapPoints.abidjan.lat)*mapZoom};
  aLbl.style.left = (aPos.x+8)+'px'; aLbl.style.top = (aPos.y+6)+'px';

  place('portBuchanan', mapPoints.buchanan);
  const bLbl = document.getElementById('portBuchananLbl');
  const bPos = {x:offsetX+lonToX(mapPoints.buchanan.lon)*mapZoom, y:offsetY+latToY(mapPoints.buchanan.lat)*mapZoom};
  bLbl.style.left = (bPos.x+8)+'px'; bLbl.style.top = (bPos.y-16)+'px';

  const vPos = place('vesselDot', mapPoints.vessel);

  document.getElementById('routeLine').setAttribute('d', `M ${aPos.x} ${aPos.y} Q ${vPos.x} ${(aPos.y+bPos.y)/2 - 30} ${bPos.x} ${bPos.y}`);

  const tip = document.getElementById('mapTooltip');
  let tx = vPos.x + 22, ty = vPos.y - 90;
  if(tx + 220 > w) tx = vPos.x - 240;
  if(ty < 10) ty = vPos.y + 20;
  tip.style.left = tx+'px';
  tip.style.top = ty+'px';
}
function zoomMap(factor){
  mapZoom = Math.min(30, Math.max(2, mapZoom*factor));
  layoutMap();
}
window.addEventListener('resize', ()=>{
  if(document.getElementById('view-map').classList.contains('active')) layoutMap();
});

/* ---------------- TIMELINE DATE AXIS + SCRUBBER ---------------- */
const TL_START = Date.UTC(2026,2,18,0,0,0);
const TL_END   = Date.UTC(2026,2,25,0,0,0);
const timelineAxis = document.getElementById('timelineAxis');
for(let i=0;i<=7;i++){
  const t = new Date(TL_START + i*(TL_END-TL_START)/7);
  const lbl = t.toLocaleDateString('en-GB',{day:'2-digit',month:'short'}) + ' ' + String(t.getUTCHours()).padStart(2,'0')+':00';
  timelineAxis.insertAdjacentHTML('beforeend', `<span>${lbl}</span>`);
}
function updatePlayhead(val){
  const frac = val/1000;
  const t = new Date(TL_START + frac*(TL_END-TL_START));
  const readout = t.toLocaleDateString('en-GB',{day:'2-digit',month:'short',year:'numeric'}) + ', ' +
    String(t.getUTCHours()).padStart(2,'0')+':'+String(t.getUTCMinutes()).padStart(2,'0')+' UTC';
  document.getElementById('playheadReadout').textContent = readout;
  document.getElementById('timelinePlayhead').style.left = `calc(102px + (100% - 104px) * ${frac})`;
}
document.getElementById('timeSlider').addEventListener('input', e=>updatePlayhead(e.target.value));
updatePlayhead(820);

/* ---------------- MAP TIMELINE ---------------- */
function fillTimeline(id, color, count){
  const track = document.getElementById(id);
  let html='';
  for(let i=0;i<count;i++){
    const left = Math.random()*88;
    const w = 1.5+Math.random()*3;
    html += `<div class="timeline-block" style="left:${left}%;width:${w}%;background:${color};"></div>`;
  }
  track.innerHTML = html;
}
fillTimeline('tlAlert','var(--alert)',3);
fillTimeline('tlAttention','var(--attention)',10);
fillTimeline('tlRoutine','var(--routine)',26);
(function(){
  const track = document.getElementById('tlDayNight');
  track.innerHTML = `
    <div class="timeline-block" style="left:0%;width:25%;background:#1B2A4E;"></div>
    <div class="timeline-block" style="left:25%;width:25%;background:#F0A020;"></div>
    <div class="timeline-block" style="left:50%;width:25%;background:#1B2A4E;"></div>
    <div class="timeline-block" style="left:75%;width:25%;background:#F0A020;"></div>
  `;
})();

/* ---------------- SENSORS ---------------- */
const sensors = [
  {name:'PRTS-BRG-01', loc:'Bridge · Wheelhouse'},
  {name:'PRTS-BRG-02', loc:'Bridge · Wheelhouse Aft'},
  {name:'PRTS-ENG-01', loc:'Engine Room · Main Engine'},
  {name:'PRTS-GEN-01', loc:'Engine Room · Generator'},
  {name:'PRTS-ECR-01', loc:'Engine Room · ECR'},
  {name:'PRTS-SGR-01', loc:'Engine Room · Steering Gear'},
  {name:'PRTS-DCK-01', loc:'Deck · Main Deck Stbd'},
  {name:'PRTS-DCK-02', loc:'Deck · Main Deck Port'},
  {name:'PRTS-BCR-01', loc:'Deck · Ballast Control Rm'},
  {name:'PRTS-OWS-01', loc:'Engine Room · OWS'},
  {name:'PRTS-AMS-01', loc:'Deck · Aft Mooring Stn'},
  {name:'PRTS-AMS-02', loc:'Deck · Aft Mooring Stn'},
];
const sensorGrid = document.getElementById('sensorGrid');
sensors.forEach(s=>{
  sensorGrid.insertAdjacentHTML('beforeend', `
    <div class="sensor-card">
      <div class="sensor-thumb">
        <div class="grain"></div>
        <div class="live-badge"><span class="d"></span>LIVE</div>
      </div>
      <div class="sensor-body">
        <div class="sensor-name">${s.name}</div>
        <div class="sensor-loc">${s.loc}</div>
        <div class="sensor-status-row">
          <span class="status-ok">● Camera OK</span>
          <span>Edge: Up</span>
        </div>
      </div>
    </div>
  `);
});

/* ================= THERMAL CAMERA ANALYTICS ================= */
function tStatus(v,warn,alarm){ return v>=alarm?'alert':(v>=warn?'attention':'routine'); }

const zoneIcons = {
  engine: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="7" width="14" height="11" rx="1"/><path d="M17 10h3l1 2v4l-1 1h-3"/><path d="M7 7V4a1 1 0 011-1h1a1 1 0 011 1v3"/></svg>`,
  generator: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M9 9l6 6M15 9l-6 6"/></svg>`,
  purifier: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3c3 4 6 6.5 6 10a6 6 0 01-12 0c0-3.5 3-6 6-10z"/></svg>`,
  boiler: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 21V9a4 4 0 014-4h8a4 4 0 014 4v12"/><path d="M4 21h16M9 15h6"/></svg>`,
  compressor: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="10" width="12" height="8" rx="1"/><circle cx="18" cy="8" r="4"/><path d="M18 12v2M8 10V6"/></svg>`,
};

const thermalZones = [
  { id:'me', name:'Main Engine', kind:'engine', cam:'IR-CAM-01 — Main Engine (MAN B&W ME-GI)',
    points:[
      {name:'Cylinder Liner #2', value:72.4, warn:85, alarm:95},
      {name:'Main Bearing #3', value:96.8, warn:85, alarm:95},
      {name:'Turbocharger Casing', value:81.2, warn:80, alarm:100},
      {name:'Exhaust Manifold', value:74.0, warn:90, alarm:105},
      {name:'Jacket CW Inlet', value:54.3, warn:75, alarm:90},
    ]},
  { id:'g1', name:'Generator No.1', kind:'generator', cam:'IR-CAM-02 — Generator No.1',
    points:[
      {name:'Stator Winding', value:89.5, warn:80, alarm:100},
      {name:'Bearing DE', value:68.1, warn:80, alarm:95},
      {name:'Bearing NDE', value:66.4, warn:80, alarm:95},
      {name:'Exhaust Elbow', value:77.9, warn:90, alarm:105},
    ]},
  { id:'ac', name:'Air Compressor Room', kind:'compressor', cam:'IR-CAM-03 — Air Compressor (TANABE Type)',
    points:[
      {name:'Discharge Temp', value:68.4, warn:80, alarm:95},
      {name:'Stage 1 Outlet', value:74.1, warn:85, alarm:100},
      {name:'Stage 2 Outlet', value:79.6, warn:85, alarm:100},
      {name:'Air Receiver Temp', value:49.8, warn:70, alarm:90},
    ]},
  { id:'pu', name:'Purifier Room', kind:'purifier', cam:'IR-CAM-04 — Purifier (MITSUBISHI Self-Ejector Type)',
    points:[
      {name:'Bowl Casing', value:58.2, warn:75, alarm:90},
      {name:'Heater Outlet', value:91.4, warn:85, alarm:100},
      {name:'Discharge Line', value:52.0, warn:75, alarm:90},
    ]},
  { id:'bo', name:'Auxiliary Boiler', kind:'boiler', cam:'IR-CAM-05 — Vertical Boiler (Osaka Type)',
    points:[
      {name:'Furnace Front Plate', value:102.6, warn:90, alarm:100},
      {name:'Uptake Casing', value:88.3, warn:85, alarm:100},
      {name:'Feedwater Line', value:60.1, warn:80, alarm:95},
    ]},
];
thermalZones.forEach(z=>{
  z.points.forEach(p=> p.status = tStatus(p.value,p.warn,p.alarm));
  z.max = Math.max(...z.points.map(p=>p.value));
  z.avg = z.points.reduce((a,p)=>a+p.value,0)/z.points.length;
  z.min = Math.min(...z.points.map(p=>p.value));
  const order = {alert:2,attention:1,routine:0};
  z.status = z.points.reduce((s,p)=> order[p.status]>order[s]?p.status:s, 'routine');
});

const markerSlots = [
  {left:'22%',top:'24%',side:'l'},{left:'38%',top:'54%',side:'l'},{left:'50%',top:'16%',side:'l'},
  {left:'77%',top:'58%',side:'r'},{left:'15%',top:'66%',side:'l'},{left:'78%',top:'36%',side:'r'},
];
/* Marker positions matched to the actual engine-room reference image (percent of viewport) */
const sceneMarkerSlots = {
  compressor: [{left:'9%', top:'18%', side:'l'},{left:'15%',top:'16%', side:'l'},{left:'11%',top:'33%', side:'l'},{left:'21%',top:'22%', side:'l'}],
  generator:  [{left:'9%', top:'49%', side:'l'},{left:'6%', top:'51%', side:'l'},{left:'17%',top:'51%', side:'l'},{left:'13%',top:'46%', side:'l'}],
  engine:     [{left:'48%',top:'13%', side:'l'},{left:'52%',top:'42%', side:'l'},{left:'66%',top:'40%', side:'r'},{left:'41%',top:'8%',  side:'l'},{left:'30%',top:'52%', side:'l'}],
  boiler:     [{left:'84%',top:'34%', side:'r'},{left:'86%',top:'10%', side:'r'},{left:'79%',top:'20%', side:'l'}],
  purifier:   [{left:'78%',top:'62%', side:'l'},{left:'85%',top:'58%', side:'r'},{left:'92%',top:'82%', side:'r'}],
};
/* Room bounding boxes on the reference image, used to draw the active-zone highlight */
const zoneRoomBox = {
  compressor: {left:'1%',  top:'2%',  width:'24%', height:'42%'},
  generator:  {left:'1%',  top:'45%', width:'24%', height:'53%'},
  engine:     {left:'26%', top:'2%',  width:'47%', height:'64%'},
  boiler:     {left:'74%', top:'2%',  width:'25%', height:'42%'},
  purifier:   {left:'74%', top:'45%', width:'25%', height:'53%'},
};

let activeZoneId = 'me';

function renderZoneList(){
  const el = document.getElementById('zoneList');
  el.innerHTML = '';
  thermalZones.forEach(z=>{
    el.insertAdjacentHTML('beforeend', `
      <div class="zone-item ${z.id===activeZoneId?'active':''}" onclick="selectZone('${z.id}')">
        <div class="zone-icon ${z.status!=='routine'?'st-'+z.status:''}">${zoneIcons[z.kind]}</div>
        <div class="zone-info">
          <div class="zone-name">${z.name}</div>
          <div class="zone-meta"><span class="dot ${z.status}"></span>${z.points.length} points · ${z.status==='routine'?'Normal':z.status==='attention'?'Attention':'Alarm'}</div>
        </div>
        <div class="zone-temp ${z.status}${z.status==='alert'?' blink-alert':''}">${z.max.toFixed(1)}°</div>
      </div>
    `);
  });
}

function selectZone(id){
  activeZoneId = id;
  renderZoneList();
  renderViewport();
}

function renderViewport(){
  const z = thermalZones.find(zz=>zz.id===activeZoneId);
  const box = zoneRoomBox[z.kind];
  const hl = document.getElementById('roomHighlight');
  hl.style.left = box.left; hl.style.top = box.top; hl.style.width = box.width; hl.style.height = box.height;
  hl.className = 'room-highlight ' + z.status;
  document.getElementById('thermCamName').textContent = z.cam;
  document.getElementById('thermCamTime').textContent = new Date().toISOString().slice(0,19).replace('T',' ')+' UTC · Thermal Mode · 25fps';
  document.getElementById('thermMax').textContent = z.max.toFixed(1)+'°C';
  document.getElementById('thermAvg').textContent = z.avg.toFixed(1)+'°C';
  document.getElementById('thermMin').textContent = z.min.toFixed(1)+'°C';
  document.getElementById('thermMax').className = 'val '+(z.status==='alert'?'alert blink-alert':z.status==='attention'?'attention':'routine');

  const layer = document.getElementById('tpointLayer');
  layer.innerHTML = '';
  const slots = sceneMarkerSlots[z.kind] || markerSlots;
  z.points.forEach((p,i)=>{
    const slot = slots[i % slots.length];
    const diff = (p.value - p.warn).toFixed(1);
    layer.insertAdjacentHTML('beforeend', `
      <div class="tpoint" style="left:${slot.left};top:${slot.top};">
        <div class="tpoint-marker ${p.status}"></div>
        <div class="tpoint-label ${slot.side==='r'?'rt':''}">
          <div class="n">${p.name}</div>
          <div class="v ${p.status}${p.status==='alert'?' blink-alert':''}">${p.value.toFixed(1)}°C</div>
        </div>
      </div>
    `);
  });

  const ticker = document.getElementById('thermTicker');
  const worst = z.points.filter(p=>p.status!=='routine').sort((a,b)=> (b.value-b.warn)-(a.value-a.warn))[0];
  if(worst){
    ticker.style.display='flex';
    ticker.innerHTML = `<span class="tag${worst.status==='alert'?' blink-alert':''}">${worst.status.toUpperCase()}</span><span class="msg">${worst.name} — ${worst.value.toFixed(1)}°C exceeds ${worst.status==='alert'?'alarm':'warning'} threshold of ${(worst.status==='alert'?worst.alarm:worst.warn).toFixed(1)}°C</span>`;
  } else {
    ticker.style.display='none';
  }

  document.getElementById('pointsSub').textContent = z.name;
  const summ = document.getElementById('pointsSummary');
  summ.innerHTML = `
    <div class="psc"><div class="v alert">${z.max.toFixed(1)}°</div><div class="l">Highest</div></div>
    <div class="psc"><div class="v attention">${z.avg.toFixed(1)}°</div><div class="l">Average</div></div>
    <div class="psc"><div class="v routine">${z.min.toFixed(1)}°</div><div class="l">Lowest</div></div>
  `;
  const list = document.getElementById('pointRowList');
  list.innerHTML = '';
  z.points.forEach(p=>{
    const diff = p.value - p.warn;
    list.insertAdjacentHTML('beforeend', `
      <div class="point-row ${p.status!=='routine'?p.status:''}">
        <div class="pdot ${p.status}"></div>
        <div class="pinfo">
          <div class="pname">${p.name}</div>
          <div class="pmeta">Warn ${p.warn}°C · Alarm ${p.alarm}°C</div>
        </div>
        <div class="pval">
          <div class="t ${p.status}${p.status==='alert'?' blink-alert':''}">${p.value.toFixed(1)}°</div>
          <div class="d">${diff>=0?'+':''}${diff.toFixed(1)}°</div>
        </div>
      </div>
    `);
  });
}

/* ---- Sub-tab switching ---- */
document.querySelectorAll('#thermSubtabs button').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    document.querySelectorAll('#thermSubtabs button').forEach(b=>b.classList.remove('active'));
    btn.classList.add('active');
    document.querySelectorAll('.therm-panel').forEach(p=>p.classList.remove('active'));
    var _panel = document.getElementById('tp-'+btn.dataset.tp);
    _panel.classList.add('active');
    requestAnimationFrame(function(){
      if(!window.Chart || !window.Chart.getChart) return;
      _panel.querySelectorAll('canvas').forEach(function(c){
        var inst = window.Chart.getChart(c);
        if(inst) inst.resize();
      });
    });
  });
});

/* ---- Trends & Analysis ---- */
const pointColors = ['#2E7BFF','#1FA971','#F0A020','#9B5DE5','#12B3AA','#E5484D'];
let activeTrendZone = 'me';
function renderThermScoreCards(){
  const wrap = document.getElementById('thermScoreCards');
  wrap.innerHTML = '';
  thermalZones.forEach(z=>{
    wrap.insertAdjacentHTML('beforeend', `
      <div class="therm-score-card ${z.id===activeTrendZone?'on':''}" data-zid="${z.id}" onclick="selectTrendZone('${z.id}')">
        <div class="lbl"><span class="dot ${z.status}"></span>${z.name}</div>
        <div class="val">${z.max.toFixed(1)}°C</div>
        <div class="sub">${z.status==='routine'?'All points normal':(z.points.filter(p=>p.status!=='routine').length)+' point(s) flagged'}</div>
      </div>
    `);
  });
}
function genTrend(current, baseline){
  const pts=[]; for(let i=0;i<12;i++){ const t=i/11; const noise=(Math.sin(i*1.7)*1.2); pts.push(+(baseline + (current-baseline)*Math.pow(t,1.6) + noise).toFixed(1)); } return pts;
}
function selectTrendZone(id){
  activeTrendZone = id;
  renderThermScoreCards();
  renderThermChart();
}
let thermChart;
function renderThermChart(){
  const z = thermalZones.find(zz=>zz.id===activeTrendZone);
  document.getElementById('thermChartTitle').textContent = z.name+' — Temperature Trend (24h)';
  const hours = Array.from({length:12},(_,i)=> (i*2)+':00');
  const ctx = document.getElementById('thermTrendChart');
  const datasets = z.points.map((p,i)=>({
    label:p.name,
    data:genTrend(p.value, p.value - (8+Math.random()*10)),
    borderColor: pointColors[i%pointColors.length],
    backgroundColor:'transparent',
    borderWidth:2, tension:0.35, pointRadius:0,
  }));
  const worstWarn = Math.min(...z.points.map(p=>p.warn));
  const worstAlarm = Math.min(...z.points.map(p=>p.alarm));
  datasets.push({label:'Warning threshold', data:Array(12).fill(worstWarn), borderColor:'#F0A020', borderDash:[6,4], borderWidth:1.4, pointRadius:0});
  datasets.push({label:'Alarm threshold', data:Array(12).fill(worstAlarm), borderColor:'#E5484D', borderDash:[6,4], borderWidth:1.4, pointRadius:0});
  if(thermChart) thermChart.destroy();
  thermChart = new Chart(ctx, {
    type:'line',
    data:{ labels:hours, datasets },
    options:{
      plugins:{legend:{display:false}},
      scales:{
        y:{grid:{color:'#EEF1F8'},ticks:{font:{family:'IBM Plex Mono',size:10}}},
        x:{grid:{display:false},ticks:{font:{family:'IBM Plex Mono',size:10}}}
      }
    }
  });
  const legend = document.getElementById('thermChartLegend');
  legend.innerHTML = z.points.map((p,i)=>`<span><span style="display:inline-block;width:9px;height:9px;border-radius:50%;background:${pointColors[i%pointColors.length]};margin-right:5px;"></span>${p.name}</span>`).join('')
    + `<span><span style="display:inline-block;width:14px;height:2px;background:#F0A020;margin-right:5px;"></span>Warning</span>`
    + `<span><span style="display:inline-block;width:14px;height:2px;background:#E5484D;margin-right:5px;"></span>Alarm</span>`;
}

/* ---- Alarms table ---- */
function buildThermAlarms(){
  const rows = [];
  thermalZones.forEach(z=>{
    z.points.filter(p=>p.status!=='routine').forEach(p=>{
      rows.push({
        sev:p.status, zone:z.name,
        desc:(p.status==='alert'?'Over Temperature — ':'Approaching Threshold — ')+p.name,
        temp:p.value, threshold: p.status==='alert'?p.alarm:p.warn,
        time:'25 Mar '+(String(6+Math.floor(Math.random()*10)).padStart(2,'0'))+':'+String(Math.floor(Math.random()*60)).padStart(2,'0'),
        dur: Math.floor(Math.random()*40)+'m '+Math.floor(Math.random()*60)+'s',
        status: p.status==='alert' ? 'Active' : 'Acknowledged',
        acked:false,
      });
    });
  });
  return rows;
}
const thermAlarms = buildThermAlarms();
function renderThermAlarmFilters(){
  const sel = document.getElementById('thermZoneFilter');
  thermalZones.forEach(z=> sel.insertAdjacentHTML('beforeend', `<option value="${z.name}">${z.name}</option>`));
  const logSel = document.getElementById('logZoneFilter');
  thermalZones.forEach(z=> logSel.insertAdjacentHTML('beforeend', `<option value="${z.name}">${z.name}</option>`));
}
function renderThermAlarms(){
  document.getElementById('cntAttention').textContent = thermAlarms.filter(a=>a.sev==='attention').length;
  document.getElementById('cntAlert').textContent = thermAlarms.filter(a=>a.sev==='alert').length;
  const onSevs = [...document.querySelectorAll('#tp-alarms .sev-chip.on')].map(c=>c.dataset.tsev);
  const zoneF = document.getElementById('thermZoneFilter').value;
  const rows = document.getElementById('thermAlarmRows');
  rows.innerHTML = '';
  thermAlarms
    .map((a,idx)=>({...a, _idx:idx}))
    .filter(a=> onSevs.includes(a.sev))
    .filter(a=> zoneF==='all' || a.zone===zoneF)
    .forEach(a=>{
      const isAlert = a.sev==='alert' && !a.acked;
      rows.insertAdjacentHTML('beforeend', `
        <tr class="${isAlert?'blink-row':''}">
          <td><span class="sev-dot-lbl"><span class="sev-dot${isAlert?' blink-alert':''}" style="background:${sevColorMap[a.sev]}"></span>${a.sev.charAt(0).toUpperCase()+a.sev.slice(1)}</span></td>
          <td>${a.zone}</td>
          <td>${a.desc}</td>
          <td class="mono-cell${isAlert?' blink-alert':''}" style="color:${a.sev==='alert'?'var(--alert)':'var(--attention)'};font-weight:700;">${a.temp.toFixed(1)}°C</td>
          <td class="mono-cell">${a.threshold.toFixed(1)}°C</td>
          <td class="mono-cell">${a.time}</td>
          <td class="mono-cell">${a.dur}</td>
          <td>${isAlert ? `<button class="ack-btn" onclick="acknowledgeAlert('thermal',${a._idx})">Ack</button>` : `<span class="status-pill">${a.acked?'Acknowledged':a.status}</span>`}</td>
        </tr>
      `);
    });
}
document.querySelectorAll('#tp-alarms .sev-chip').forEach(c=> c.addEventListener('click', ()=>{ c.classList.toggle('on'); renderThermAlarms(); }));
document.getElementById('thermZoneFilter').addEventListener('change', renderThermAlarms);

/* ---- Logs table ---- */
function renderThermLogs(){
  const zoneF = document.getElementById('logZoneFilter').value;
  const rows = document.getElementById('thermLogRows');
  rows.innerHTML = '';
  const now = new Date();
  let entries = [];
  thermalZones.forEach(z=>{
    z.points.forEach(p=>{
      for(let i=0;i<3;i++){
        const t = new Date(now.getTime() - i*10*60000);
        entries.push({ time:t, zone:z.name, name:p.name, value:p.value + (Math.random()*2-1), status:p.status });
      }
    });
  });
  entries.sort((a,b)=> b.time-a.time);
  entries
    .filter(e=> zoneF==='all' || e.zone===zoneF)
    .slice(0,60)
    .forEach(e=>{
      rows.insertAdjacentHTML('beforeend', `
        <tr>
          <td class="mono-cell">${e.time.toISOString().slice(0,19).replace('T',' ')}</td>
          <td>${e.zone}</td>
          <td>${e.name}</td>
          <td class="mono-cell${e.status==='alert'?' blink-alert':''}" style="font-weight:700;color:${e.status==='alert'?'var(--alert)':e.status==='attention'?'var(--attention)':'var(--text)'};">${e.value.toFixed(1)}°C</td>
          <td><span class="sev-dot-lbl"><span class="sev-dot${e.status==='alert'?' blink-alert':''}" style="background:${sevColorMap[e.status]}"></span>${e.status.charAt(0).toUpperCase()+e.status.slice(1)}</span></td>
        </tr>
      `);
    });
}
document.getElementById('logZoneFilter').addEventListener('change', renderThermLogs);

/* ---- Init ---- */
renderZoneList();
renderViewport();
renderThermScoreCards();
renderThermChart();
renderThermAlarmFilters();
renderThermAlarms();
renderThermLogs();
renderAnalyticsAlarmLog();
renderActivities();
const totalAlert = thermalZones.reduce((s,z)=>s+z.points.filter(p=>p.status==='alert').length,0);
const totalAttn = thermalZones.reduce((s,z)=>s+z.points.filter(p=>p.status==='attention').length,0);
document.getElementById('thermSummary').textContent = `${thermalZones.length} zones · ${totalAlert} alert · ${totalAttn} attention`;

  if (typeof layoutMap === 'function') { try { layoutMap(); } catch (e) { /* map not visible yet */ } }
}
