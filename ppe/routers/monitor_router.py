# routers/monitor_router.py
from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
from ppe.services.webrtc_service import handle_offer

router = APIRouter(tags=["WebRTC Monitor"])

@router.get("/monitor", response_class=HTMLResponse)
async def monitor_page():
    return """
    <!doctype html>
    <html>
    <head>
    <meta charset="utf-8" />
    <title>WebRTC Monitor</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <style>
      :root{
        --bg:#ffffff; --fg:#111111; --muted:#666666; --line:#dddddd;
        --btn:#f8f8f8; --btn-h:#eeeeee; --btn-text:#111111;
        --pill:#f1f1f1; --pill-text:#111111;
      }
      *{box-sizing:border-box}
      html,body{height:100%}
      body{
        margin:0; background:var(--bg); color:var(--fg);
        font:14px/1.5 system-ui, -apple-system, Segoe UI, Roboto, Noto Sans, Helvetica, Arial;
        display:flex; align-items:flex-start; justify-content:center; padding:28px;
      }
      .wrap{width:min(980px, 100%);}
      .card{
        border:1px solid var(--line); border-radius:16px; overflow:hidden;
        background:#fff; box-shadow:0 4px 12px rgba(0,0,0,.08);
      }
      .head{
        padding:16px 18px; border-bottom:1px solid var(--line);
        display:flex; align-items:center; justify-content:space-between;
      }
      .title{font-weight:700; letter-spacing:.3px}
      .controls{display:flex; gap:8px; align-items:center; flex-wrap:wrap}
      .btn{
        appearance:none; border:1px solid var(--line); background:var(--btn); color:var(--btn-text);
        padding:9px 14px; border-radius:10px; cursor:pointer; transition:.15s ease;
        font-weight:600; letter-spacing:.2px;
      }
      .btn:hover{background:var(--btn-h)}
      .btn[disabled]{opacity:.45; cursor:not-allowed}
      .pill{
        background:var(--pill); color:var(--pill-text);
        border:1px solid var(--line); border-radius:999px; padding:6px 10px; font-weight:600;
      }
      .body{padding:18px}
      .video-wrap{
        background:#fafafa; border:1px solid var(--line); border-radius:12px; overflow:hidden;
        position:relative;
      }
      video{
        display:block; width:100%; height:auto; aspect-ratio:16/9; background:#000;
      }
      .hint{
        margin-top:10px; color:var(--muted);
      }
      .kbd{
        border:1px solid var(--line); padding:2px 6px; border-radius:6px; background:#f8f8f8; color:#111; font-weight:700;
      }
      .spinner{
        position:absolute; inset:0; display:flex; align-items:center; justify-content:center; pointer-events:none;
      }
      .ring{
        width:42px; height:42px; border:3px solid #e0e0e0; border-top-color:#111; border-radius:50%;
        animation:spin 1s linear infinite;
      }
      @keyframes spin { to{ transform:rotate(360deg); } }
    </style>
    </head>
    <body>
      <div class="wrap">
        <div class="card">
          <div class="head">
            <div class="title">WebRTC Monitor</div>
            <div class="controls">
              <button id="rtcStart" class="btn">Start WebRTC</button>
              <button id="rtcStop"  class="btn" disabled>Stop WebRTC</button>
              <span style="width:10px"></span>
              <button id="detStart" class="btn">Start Detect</button>
              <button id="detStop"  class="btn" disabled>Stop Detect</button>
              <span id="status" class="pill">idle</span>
            </div>
          </div>
          <div class="body">
            <div class="video-wrap">
              <video id="video" autoplay playsinline controls muted></video>
              <div id="spin" class="spinner" style="display:none"><div class="ring"></div></div>
            </div>
            <div class="hint">
              Press <span class="kbd">S</span> to start/stop detect &nbsp;·&nbsp;
              <span class="kbd">R</span> to start/stop WebRTC
            </div>
          </div>
        </div>
      </div>

    <script>
      let pc = null;
      const video = document.getElementById('video');
      const st    = document.getElementById('status');
      const spin  = document.getElementById('spin');
      const btnRS = document.getElementById('rtcStart');
      const btnRX = document.getElementById('rtcStop');
      const btnDS = document.getElementById('detStart');
      const btnDX = document.getElementById('detStop');

      function setStatus(s){ st.textContent = s; }
      function spinOn(on){ spin.style.display = on ? 'flex' : 'none'; }

      async function rtcStart(){
        if (pc) return;
        setStatus('rtc starting…'); spinOn(true);
        pc = new RTCPeerConnection({ iceServers: [{ urls: ['stun:stun.l.google.com:19302'] }] });

        pc.onconnectionstatechange = () => setStatus('rtc ' + pc.connectionState);
        pc.oniceconnectionstatechange = () => {
          if (['failed','disconnected','closed'].includes(pc.iceConnectionState)) spinOn(false);
        };
        pc.ontrack = (e) => { video.srcObject = e.streams[0]; spinOn(false); };
        pc.addTransceiver('video', { direction: 'recvonly' });

        try {
          const offer = await pc.createOffer();
          await pc.setLocalDescription(offer);
          const resp = await fetch('/offer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pc.localDescription)
          });
          const answer = await resp.json();
          await pc.setRemoteDescription(answer);

          btnRS.disabled = true; btnRX.disabled = false;
          btnDS.disabled = false; btnDX.disabled = true;
          setStatus('rtc connected');
        } catch (e){
          setStatus('rtc failed'); spinOn(false);
          console.error(e);
          rtcStop();
        }
      }

      function rtcStop(){
        if (!pc) return;
        pc.getSenders().forEach(s => s.track && s.track.stop());
        pc.getReceivers().forEach(r => r.track && r.track.stop());
        pc.close(); pc = null;
        video.srcObject = null;
        btnRS.disabled = false; btnRX.disabled = true;
        setStatus('rtc stopped');
        detStop();
      }

      async function detStart(){
        setStatus('detect starting…');
        try{
          const r = await fetch('/detect/start', { method: 'POST' });
          const j = await r.json();
          setStatus('detect started: ' + j.started);
          if (j.started){ btnDS.disabled = true; btnDX.disabled = false; }
        }catch(e){
          setStatus('detect failed'); console.error(e);
        }
      }

      async function detStop(){
        setStatus('detect stopping…');
        try{
          const r = await fetch('/detect/stop', { method: 'POST' });
          const j = await r.json();
          setStatus('detect stopped: ' + j.stopped);
          if (j.stopped){ btnDS.disabled = false; btnDX.disabled = true; }
        }catch(e){
          setStatus('detect failed'); console.error(e);
        }
      }

      btnRS.addEventListener('click', rtcStart);
      btnRX.addEventListener('click', rtcStop);
      btnDS.addEventListener('click', detStart);
      btnDX.addEventListener('click', detStop);

      window.addEventListener('beforeunload', () => { try { rtcStop(); } catch(e){} });

      // Shortcuts: R = WebRTC toggle, S = Detect toggle
      window.addEventListener('keydown', (e)=>{
        if (e.key.toLowerCase() === 'r') { pc ? rtcStop() : rtcStart(); }
        if (e.key.toLowerCase() === 's') { btnDS.disabled ? detStop() : detStart(); }
      });
    </script>
    </body>
    </html>
        """


@router.post("/offer")
async def offer(request: Request):
    return await handle_offer(request)
