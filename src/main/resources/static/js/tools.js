/**
 * ToolBase - All tool definitions and logic
 * Each tool = { id, name, icon, cat, desc, html(), run(), keywords }
 */
const TOOLS = [];

function def(id, name, icon, cat, desc, html, run, keywords) {
  TOOLS.push({ id, name, icon, cat, desc, html, run, keywords: keywords || '' });
}

// ===== TEXT PROCESSING =====
def('json-formatter', 'JSON Formatter', '{ }', 'text',
  'Format, validate, and compress JSON data',
  () => `<textarea id="in" placeholder="Paste JSON data here..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="T('json-formatter')._fmt('  ')">✨ Format (2 spaces)</button>
<button onclick="T('json-formatter')._fmt('\t')">TAB indent</button>
<button onclick="T('json-formatter')._cmp()">🗜️ Compress</button>
<button onclick="T('json-formatter')._val()">✅ Validate</button>
<button onclick="copyOut()">📋 Copy</button></div><div id="status"></div>`,
  function() {
    this._fmt = (i) => { try { const v=JSON.parse(inp()); outp(JSON.stringify(v,null,i)); st('Formatted ✓'); } catch(e){ st('✗ '+e.message,'err'); } };
    this._cmp = () => { try { const v=JSON.parse(inp()); outp(JSON.stringify(v)); st('Compressed ✓'); } catch(e){ st('✗ '+e.message,'err'); } };
    this._val = () => { try { JSON.parse(inp()); st('✅ Valid JSON'); } catch(e){ st('✗ Invalid JSON','err'); } };
  });

def('base64-encode', 'Base64 Encode/Decode', '🔐', 'text',
  'Encode text to Base64 or decode Base64 to text',
  () => `<div class="tab-row"><button class="tab-active" id="b64e" onclick="T('base64-encode')._mode('encode')">Encode</button>
<button id="b64d" onclick="T('base64-encode')._mode('decode')">Decode</button></div>
<textarea id="in" placeholder="Enter text..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="T('base64-encode')._run()">▶ Convert</button>
<button onclick="swapInpOut()">🔄 Swap</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() {
    this._mode = (m) => { this.m=m; document.querySelectorAll('#b64e,#b64d').forEach(b=>b.className=''); document.getElementById('b64'+m[0]).className='tab-active'; document.getElementById('in').placeholder=m==='encode'?'Enter text to encode...':'Enter Base64 string...'; };
    this.m='encode'; this._run=()=>{try{this.m==='encode'?outp(btoa(inp())):outp(atob(inp()));st('Done ✓')}catch(e){st('✗ Failed','err')}};
  });

def('url-encode', 'URL Encode/Decode', '🔗', 'text',
  'URL-encode or URL-decode strings',
  () => `<textarea id="in" placeholder="Enter URL or text..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="T('url-encode')._run('encode')">🔐 Encode</button>
<button onclick="T('url-encode')._run('decode')">🔓 Decode</button>
<button onclick="swapInpOut()">🔄 Swap</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() { this._run=(m)=>{try{outp(m==='encode'?encodeURIComponent(inp()):decodeURIComponent(inp()));st('Done ✓')}catch(e){st('✗ Failed','err')}}; });

def('regex-tester', 'Regex Tester', '🔍', 'text',
  'Test regular expressions with real-time highlighting',
  () => `<div class="fld"><label>Regular Expression</label><input id="pat" placeholder="e.g. \\\\d{3,}" oninput="T('regex-tester')._test()"></div>
<div class="chk-row"><label><input type="checkbox" id="rg" checked onchange="T('regex-tester')._test()"> Global</label>
<label><input type="checkbox" id="ri" onchange="T('regex-tester')._test()"> Ignore case</label>
<label><input type="checkbox" id="rm" onchange="T('regex-tester')._test()"> Multiline</label></div>
<div class="fld"><label>Test Text</label><textarea id="in" placeholder="Text to test against..." oninput="T('regex-tester')._test()">Hello World 123\ntest@email.com\n2024-01-15</textarea></div>
<div id="re-result" class="result-box">Results appear here</div>`,
  function() {
    this._test = () => {
      const p = E('pat'), t = E('in').value, r = E('re-result');
      if (!p) { r.innerHTML = '<span class=dim>Enter a regex pattern</span>'; return; }
      try {
        let f = 'g'; if (E('ri').checked) f += 'i'; if (E('rm').checked) f += 'm';
        const re = new RegExp(p, f), matches = [...t.matchAll(re)], seen = new Set();
        let html = `<div>${matches.length} match${matches.length!==1?'es':''}</div>`;
        matches.forEach((m, i) => { if (!seen.has(m.index)) { seen.add(m.index); html += `<div class=match>#${i+1}: <code>${escHtml(m[0])}</code></div>`; } });
        r.innerHTML = html;
      } catch(e) { r.innerHTML = `<span class=err>${escHtml(e.message)}</span>`; }
    };
  });

def('uuid-generator', 'UUID Generator', '🆔', 'text',
  'Generate UUID v4 identifiers, bulk mode available',
  () => `<div class="fld"><label>Count</label><input id="cnt" type="number" value="5" min="1" max="100"></div>
<div class="btn-row"><button onclick="T('uuid-generator')._gen(false)">🔄 Generate</button>
<button onclick="T('uuid-generator')._gen(true)">🔠 Uppercase</button>
<button onclick="T('uuid-generator')._cpy()">📋 Copy All</button></div>
<div id="ulist" class="uuid-list"></div>`,
  function() {
    this._gen = (up) => {
      const n = parseInt(E('cnt').value)||5, list = E('ulist');
      list.innerHTML = '';
      for (let i = 0; i < n; i++) {
        const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,c=>{const r=Math.random()*16|0;return(c==='x'?r:r&0x3|0x8).toString(16)});
        const d = document.createElement('div'); d.className = 'uuid-item';
        d.innerHTML = `<span>${up?uuid.toUpperCase():uuid}</span><span class=dim>📋</span>`;
        d.onclick = () => { navigator.clipboard.writeText(d.firstChild.textContent).then(()=>{d.innerHTML='<span style=color:#4ade80>Copied ✓</span>';setTimeout(()=>{d.innerHTML=`<span>${up?uuid.toUpperCase():uuid}</span><span class=dim>📋</span>`},1000)}) };
        list.appendChild(d);
      }
    };
    this._cpy = () => { let t=''; E('ulist').querySelectorAll('.uuid-item').forEach(e=>t+=e.firstChild.textContent+'\n'); navigator.clipboard.writeText(t.trim()); st('All copied!'); };
  });

def('hash-generator', 'Hash Generator', '#️⃣', 'text',
  'Calculate MD5, SHA1, SHA256, SHA512 hashes',
  () => `<textarea id="in" placeholder="Enter text to hash..." oninput="T('hash-generator')._calc()" spellcheck="false"></textarea>
<div class="hash-grid"><div class=hcard><div class=hl>MD5</div><div class=hv id=hmd5>—</div></div>
<div class=hcard><div class=hl>SHA-1</div><div class=hv id=hsha1>—</div></div>
<div class=hcard><div class=hl>SHA-256</div><div class=hv id=hsha256>—</div></div>
<div class=hcard><div class=hl>SHA-512</div><div class=hv id=hsha512>—</div></div></div>`,
  function() {
    this._calc = () => {
      const t = inp(); if (!t) { ['md5','sha1','sha256','sha512'].forEach(k=>{E('h'+k).textContent='—';}); return; }
      if (typeof CryptoJS === 'undefined') { document.head.appendChild(Object.assign(document.createElement('script'),{src:'https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.2.0/crypto-js.min.js',onload:()=>{this._calc()}})); return; }
      E('hmd5').textContent = CryptoJS.MD5(t).toString();
      E('hsha1').textContent = CryptoJS.SHA1(t).toString();
      E('hsha256').textContent = CryptoJS.SHA256(t).toString();
      E('hsha512').textContent = CryptoJS.SHA512(t).toString();
      st('Auto-calculated');
    };
  });

def('word-counter', 'Word Counter', '📊', 'text',
  'Count words, characters, sentences, and paragraphs',
  () => `<textarea id="in" placeholder="Paste or type text here..." oninput="T('word-counter')._count()" spellcheck="false"></textarea>
<div class="stat-row"><div class=scard><div class=sv id=wc1>0</div><div class=sl>Words</div></div>
<div class=scard><div class=sv id=wc2>0</div><div class=sl>Characters</div></div>
<div class=scard><div class=sv id=wc3>0</div><div class=sl>Chars (no space)</div></div>
<div class=scard><div class=sv id=wc4>0</div><div class=sl>Sentences</div></div></div>`,
  function() {
    this._count = () => {
      const t = inp();
      E('wc1').textContent = t ? t.trim().split(/\s+/).length : 0;
      E('wc2').textContent = t.length;
      E('wc3').textContent = t.replace(/\s/g,'').length;
      E('wc4').textContent = t ? t.split(/[.!?]+/).filter(s=>s.trim()).length : 0;
    };
  });

def('case-converter', 'Case Converter', 'Aa', 'text',
  'Convert text to UPPER CASE, lower case, Title Case, and more',
  () => `<textarea id="in" placeholder="Enter text to convert..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="outp(inp().toUpperCase())">UPPER CASE</button>
<button onclick="outp(inp().toLowerCase())">lower case</button>
<button onclick="outp(inp().replace(/\\w\\S*/g,w=>w[0].toUpperCase()+w.slice(1).toLowerCase()))">Title Case</button>
<button onclick="outp(inp().replace(/(^\w|\\.\\s*\\w)/g,w=>w.toUpperCase()))">Sentence case</button>
<button onclick="outp(inp().split('').join(' '))">S p a c e d</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() {});

def('html-encoder', 'HTML Entity Encoder', '&lt;', 'text',
  'Encode or decode HTML entities like &amp; &lt; &gt;',
  () => `<div class="tab-row"><button class="tab-active" id="htmle" onclick="T('html-encoder')._mode('encode')">Encode</button>
<button id="htmld" onclick="T('html-encoder')._mode('decode')">Decode</button></div>
<textarea id="in" placeholder="Enter HTML..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="T('html-encoder')._run()">▶ Convert</button>
<button onclick="swapInpOut()">🔄 Swap</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() {
    this.m='encode';
    this._mode=(m)=>{this.m=m;document.querySelectorAll('#htmle,#htmld').forEach(b=>b.className='');document.getElementById('html'+m[0]).className='tab-active';};
    this._run=()=>{outp(this.m==='encode'?inp().replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'):inp().replace(/&amp;/g,'&').replace(/&lt;/g,'<').replace(/&gt;/g,'>').replace(/&quot;/g,'"'));st('Done ✓');};
  });

def('slug-generator', 'Slug Generator', '🔗', 'text',
  'Generate SEO-friendly URL slugs from text',
  () => `<textarea id="in" placeholder="Enter text to slugify..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="outp(inp().toLowerCase().trim().replace(/[^\\w\\s-]/g,'').replace(/[\\s_]+/g,'-').replace(/-+/g,'-').replace(/^-+|-+$/g,''))">▶ Generate Slug</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() {});

// ===== CONVERSION =====
def('timestamp-converter', 'Timestamp Converter', '⏰', 'convert',
  'Convert Unix timestamps to dates and vice versa',
  () => `<div class="current-time"><div class=dim style="font-size:12px">Current Time</div>
<div id="nowTs" class=big-num></div><div id="nowDt" class=dim></div></div>
<div class="converter-box"><div class="fld"><label>Unix Timestamp → Date</label><input id="tsInp" placeholder="Enter Unix timestamp (seconds)"></div>
<button onclick="T('timestamp-converter')._ts2dt()">Convert</button>
<div class=result-box><div class=hl>Date & Time</div><div id="tsRes" class=hv>Enter a timestamp and click Convert</div></div></div>
<div class="converter-box" style="margin-top:12px"><div class="fld"><label>Date → Unix Timestamp</label><input id="dtInp" type="datetime-local"></div>
<button onclick="T('timestamp-converter')._dt2ts()">Convert</button>
<div class=result-box><div class=hl>Timestamp (seconds)</div><div id="dtRes" class=hv>Select a date and click Convert</div></div></div>`,
  function() {
    this._ts2dt = () => { const v = E('tsInp').value; if(!v) return; const ts = parseInt(v); if(isNaN(ts)){E('tsRes').textContent='✗ Invalid';return} E('tsRes').textContent=new Date(ts*1000).toLocaleString(); };
    this._dt2ts = () => { const v = E('dtInp').value; if(!v) return; E('dtRes').textContent=Math.floor(new Date(v).getTime()/1000); };
    this._tick = () => { const n = Math.floor(Date.now()/1000); E('nowTs').textContent = n; E('nowDt').textContent = new Date(n*1000).toLocaleString(); };
    this._tick(); setInterval(()=>this._tick(), 1000);
  });

def('number-base', 'Number Base Converter', '🔢', 'convert',
  'Convert between binary, octal, decimal, and hexadecimal',
  () => `<div class="fld"><label>Input Number</label><input id="nbInp" value="255" placeholder="Enter number"></div>
<div class="fld"><label>Input Base</label><select id="nbFrom"><option value=2>Binary (2)</option><option value=8>Octal (8)</option><option value=10 selected>Decimal (10)</option><option value=16>Hex (16)</option></select></div>
<button onclick="T('number-base')._conv()">Convert</button>
<div class="converter-box"><div class=result-box><div style=display:grid;grid-template-columns:1fr 1fr;gap:8px>
<div><div class=hl>Binary</div><div class=hv id=nb2>—</div></div>
<div><div class=hl>Octal</div><div class=hv id=nb8>—</div></div>
<div><div class=hl>Decimal</div><div class=hv id=nb10>—</div></div>
<div><div class=hl>Hexadecimal</div><div class=hv id=nb16>—</div></div>
</div></div></div>`,
  function() {
    this._conv = () => {
      try {
        const v = parseInt(E('nbInp').value, parseInt(E('nbFrom').value));
        if (isNaN(v)) { st('✗ Invalid number','err'); return; }
        E('nb2').textContent = v.toString(2);
        E('nb8').textContent = v.toString(8);
        E('nb10').textContent = v.toString(10);
        E('nb16').textContent = v.toString(16).toUpperCase();
        st('Converted ✓');
      } catch(e) { st('✗ Error','err'); }
    };
  });

def('color-converter', 'Color Converter', '🎨', 'convert',
  'Convert between HEX, RGB, and HSL color formats',
  () => `<div class="fld"><label>Color (HEX, RGB, or HSL)</label><input id="clrInp" value="#3b82f6" placeholder="#ff0000 or rgb(255,0,0)"></div>
<button onclick="T('color-converter')._conv()">Convert</button>
<div class="converter-box"><div id="clrPreview" style="height:60px;border-radius:8px;margin-bottom:12px"></div>
<div class=result-box><div style=display:grid;grid-template-columns:1fr 1fr;gap:8px>
<div><div class=hl>HEX</div><div class=hv id=clrHex>—</div></div>
<div><div class=hl>RGB</div><div class=hv id=clrRgb>—</div></div>
<div><div class=hl>HSL</div><div class=hv id=clrHsl>—</div></div>
</div></div></div>`,
  function() {
    this._conv = () => {
      const v = E('clrInp').value.trim();
      let r, g, b;
      if (v.startsWith('#')) {
        const h = v.replace('#',''); r=parseInt(h.slice(0,2),16); g=parseInt(h.slice(2,4),16); b=parseInt(h.slice(4,6),16);
      } else if (v.startsWith('rgb')) {
        const m = v.match(/(\d+)/g); if(m){r=+m[0];g=+m[1];b=+m[2];}
      } else if (v.startsWith('hsl')) {
        const m = v.match(/([\d.]+)/g); if(m){const[h,s,l]=[+m[0],+m[1],+m[2]]; const rgb=hslToRgb(h/360,s/100,l/100); r=rgb[0];g=rgb[1];b=rgb[2];}
      }
      if (r===undefined||isNaN(r)) { st('✗ Invalid color','err'); return; }
      const hex = '#'+[r,g,b].map(x=>x.toString(16).padStart(2,'0')).join('');
      const rgb = `rgb(${r},${g},${b})`;
      const [rr,gg,bb] = [r/255,g/255,b/255]; const mx=Math.max(rr,gg,bb),mn=Math.min(rr,gg,bb); const l=(mx+mn)/2;
      const d=mx-mn; const hh=d===0?0:mx===rr?((gg-bb)/d)%6:mx===gg?((bb-rr)/d+2):((rr-gg)/d+4); const s=d===0?0:d/(1-Math.abs(2*l-1));
      const hsl = `hsl(${Math.round(hh*60)},${Math.round(s*100)}%,${Math.round(l*100)}%)`;
      E('clrHex').textContent = hex; E('clrRgb').textContent = rgb; E('clrHsl').textContent = hsl;
      E('clrPreview').style.background = rgb; st('Converted ✓');
    };
  });

function hslToRgb(h,s,l){let c=(1-Math.abs(2*l-1))*s,x=c*(1-Math.abs((h*6)%2-1)),m=l-c/2,[r,g,b]=h<1/6?[c,x,0]:h<2/6?[x,c,0]:h<3/6?[0,c,x]:h<4/6?[0,x,c]:h<5/6?[x,0,c]:[c,0,x];return[Math.round((r+m)*255),Math.round((g+m)*255),Math.round((b+m)*255)];}

// ===== SECURITY =====
def('password-generator', 'Password Generator', '🔑', 'security',
  'Generate secure random passwords with custom options',
  () => `<div class="fld"><label>Length</label><input id="pwLen" type="number" value="16" min="4" max="128"></div>
<div class="chk-row"><label><input type="checkbox" id="pwUp" checked> A-Z</label>
<label><input type="checkbox" id="pwLow" checked> a-z</label>
<label><input type="checkbox" id="pwNum" checked> 0-9</label>
<label><input type="checkbox" id="pwSym" checked> !@#$%</label></div>
<button onclick="T('password-generator')._gen()">🔄 Generate</button>
<div class="result-box"><div class=hv id="pwResult" style="font-size:20px;word-break:break-all">Click Generate</div></div>
<div class="btn-row"><button onclick="T('password-generator')._gen()">🔄 Regenerate</button>
<button onclick="copyElem('pwResult')">📋 Copy</button></div>`,
  function() {
    this._gen = () => {
      const len = parseInt(E('pwLen').value)||16;
      let chars = '';
      if (E('pwUp').checked) chars += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
      if (E('pwLow').checked) chars += 'abcdefghijklmnopqrstuvwxyz';
      if (E('pwNum').checked) chars += '0123456789';
      if (E('pwSym').checked) chars += '!@#$%^&*()_+-=[]{}|;:,.<>?';
      if (!chars) { E('pwResult').textContent = 'Select at least one character set'; return; }
      let pw = '';
      for (let i = 0; i < len; i++) pw += chars[Math.floor(Math.random() * chars.length)];
      E('pwResult').textContent = pw;
      // Strength indicator
      const s = len >= 20 ? 'Very Strong' : len >= 12 ? 'Strong' : len >= 8 ? 'Medium' : 'Weak';
      E('pwResult').style.color = len >= 12 ? '#4ade80' : len >= 8 ? '#fb923c' : '#f87171';
      document.querySelector('#pwResult').insertAdjacentHTML('afterend', `<div style=font-size:12px;color:#64748b;margin-top:4px>Strength: ${s} (${len} chars)</div>`);
    };
  });

def('hmac-generator', 'HMAC Generator', '🔏', 'security',
  'Generate HMAC-SHA256/SHA1/MD5 signatures',
  () => `<div class="fld"><label>Secret Key</label><input id="hmacKey" placeholder="Your secret key"></div>
<textarea id="in" placeholder="Message to sign..." spellcheck="false"></textarea>
<div class="btn-row"><button onclick="T('hmac-generator')._gen('SHA256')">SHA256</button>
<button onclick="T('hmac-generator')._gen('SHA1')">SHA1</button>
<button onclick="T('hmac-generator')._gen('MD5')">MD5</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() {
    this._gen = (a) => {
      if(typeof CryptoJS==='undefined'){document.head.appendChild(Object.assign(document.createElement('script'),{src:'https://cdnjs.cloudflare.com/ajax/libs/crypto-js/4.2.0/crypto-js.min.js',onload:()=>this._gen(a)}));return;}
      const k = E('hmacKey').value||'key', msg = inp();
      outp(CryptoJS.HmacSHA256(msg,k).toString().substring(0,32)+'...');
      st(`HMAC-${a} generated`);
    };
  });

// ===== WEB DEV =====
def('css-minifier', 'CSS Minifier', '🎨', 'webdev',
  'Minify CSS by removing whitespace and comments',
  () => `<textarea id="in" placeholder="Paste your CSS here..." spellcheck="false" style="min-height:150px"></textarea>
<div class="btn-row"><button onclick="T('css-minifier')._min()">🗜️ Minify</button>
<button onclick="copyOut()">📋 Copy</button></div>`,
  function() { this._min = () => { outp(inp().replace(/\/\*[\s\S]*?\*\//g,'').replace(/\s+/g,' ').replace(/\s*([{}:;,])\s*/g,'$1').trim()); st('Minified ✓ Size: '+outp().length+' chars'); }; });

def('lorem-ipsum', 'Lorem Ipsum Generator', '📝', 'webdev',
  'Generate Lorem Ipsum placeholder text',
  () => `<div class="fld"><label>Paragraphs</label><input id="lip" type="number" value="3" min="1" max="20"></div>
<button onclick="T('lorem-ipsum')._gen()">Generate</button>
<textarea id="out" readonly style="min-height:200px;margin-top:12px"></textarea>
<div class="btn-row"><button onclick="copyOut()">📋 Copy</button></div>`,
  function() {
    this._gen = () => {
      const n = parseInt(E('lip').value)||3;
      const words = ['lorem','ipsum','dolor','sit','amet','consectetur','adipiscing','elit','sed','do','eiusmod','tempor','incididunt','ut','labore','et','dolore','magna','aliqua','enim','ad','minim','veniam','quis','nostrud','exercitation','ullamco','laboris','nisi','aliquip','ex','ea','commodo','consequat','duis','aute','irure','dolor','in','reprehenderit','voluptate','velit','esse','cillum','eu','fugiat','nulla','pariatur','excepteur','sint','occaecat','cupidatat','non','proident','sunt','culpa','qui','officia','deserunt','mollit','anim','est','laborum'];
      let result = '';
      for (let p = 0; p < n; p++) {
        let sentenceCount = 3 + Math.floor(Math.random() * 5);
        let para = '';
        for (let s = 0; s < sentenceCount; s++) {
          let wordCount = 5 + Math.floor(Math.random() * 10);
          let sentence = '';
          for (let w = 0; w < wordCount; w++) {
            sentence += words[Math.floor(Math.random() * words.length)] + ' ';
          }
          para += sentence.trim().charAt(0).toUpperCase() + sentence.slice(1).trim() + '. ';
        }
        result += para.trim() + '\n\n';
      }
      E('out').value = result.trim();
      st(`Generated ${n} paragraph${n!==1?'s':''}`);
    };
  });

def('qr-code', 'QR Code Generator', '📱', 'webdev',
  'Generate QR codes from text or URLs',
  () => `<div class="fld"><label>Content</label><textarea id="in" placeholder="Text or URL for QR code..." spellcheck="false">https://toolbase.io</textarea></div>
<div class="btn-row"><button onclick="T('qr-code')._gen()">Generate QR Code</button>
<button onclick="T('qr-code')._dl()">⬇️ Download PNG</button></div>
<div id="qrContainer" class="qr-display"><div class=dim>Click Generate</div></div>`,
  function() {
    this._gen = () => {
      const t = inp(); if (!t) { E('qrContainer').innerHTML = '<div class=dim>Enter content</div>'; return; }
      const src = '/tools/qrcode/gen?text='+encodeURIComponent(t)+'&size=300';
      this._src = src;
      E('qrContainer').innerHTML = `<img src="${src}" alt="QR Code" style="max-width:300px;border-radius:8px"><div style=font-size:11px;color:#475569;margin-top:6px;word-break:break-all>${escHtml(t)}</div>`;
    };
    this._dl = () => { if(this._src){const a=document.createElement('a');a.href=this._src;a.download='qrcode.png';a.click()} };
  });

// ===== HELPERS =====
function inp() { return (document.getElementById('in')||{value:''}).value; }
function outp(v) { const o=document.getElementById('out'); if(!o) return ''; if(v!==undefined) o.value=v; return o.value; }
function st(msg, type) { const s=document.getElementById('status'); if(!s) return; s.textContent=msg; s.style.color=type==='err'?'#f87171':'#4ade80'; }
function escHtml(s) { const d=document.createElement('div'); d.textContent=s; return d.innerHTML; }
function copyOut() { const o=document.getElementById('out'); if(o&&o.value){navigator.clipboard.writeText(o.value).then(()=>st('Copied ✓'))} }
function copyElem(id) { const e=document.getElementById(id); if(e){navigator.clipboard.writeText(e.textContent).then(()=>st('Copied ✓'))} }
function swapInpOut() { const i=document.getElementById('in'),o=document.getElementById('out'); if(i&&o){const t=i.value;i.value=o.value;o.value=t} }
function T(id) { return TOOLS.find(t=>t.id===id); }
function E(id) { return document.getElementById(id); }

// Register timestamp tick
document.addEventListener('DOMContentLoaded', () => {
  const ts = TOOLS.find(t=>t.id==='timestamp-converter');
  if (ts && ts.run) ts.run();
});
