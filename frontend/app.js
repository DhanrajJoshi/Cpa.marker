const promptEl = document.getElementById('prompt');
const sizeEl = document.getElementById('size');
const qualityEl = document.getElementById('quality');
const generateBtn = document.getElementById('generate');
const loader = document.getElementById('loader');
const errorEl = document.getElementById('error');

const previewImg = document.getElementById('previewImage');
const previewPlaceholder = document.getElementById('previewPlaceholder');
const downloadBtn = document.getElementById('downloadBtn');
const copyBtn = document.getElementById('copyUrl');

const galleryGrid = document.getElementById('galleryGrid');

async function fetchGallery(){
  try{
    const res = await fetch('/gallery');
    if(!res.ok) throw new Error('Could not load gallery');
    const list = await res.json();
    renderGallery(list);
  }catch(e){
    console.warn(e);
  }
}

function renderGallery(list){
  galleryGrid.innerHTML = '';
  list.forEach(item=>{
    const div = document.createElement('div');
    div.className = 'gallery-item';
    const img = document.createElement('img');
    img.src = item.url;
    img.alt = item.filename;
    img.loading = 'lazy';
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = item.created;
    div.appendChild(img);
    div.appendChild(meta);
    div.addEventListener('click', ()=> setPreview(item.url));
    galleryGrid.appendChild(div);
  });
}

function setPreview(url){
  previewImg.src = url;
  previewImg.classList.remove('hidden');
  previewPlaceholder.classList.add('hidden');
  downloadBtn.href = url;
  downloadBtn.classList.remove('hidden');
  copyBtn.classList.remove('hidden');
}

function setLoading(on){
  if(on){
    loader.classList.remove('hidden');
    generateBtn.disabled = true;
  }else{
    loader.classList.add('hidden');
    generateBtn.disabled = false;
  }
}

generateBtn.addEventListener('click', async ()=>{
  errorEl.classList.add('hidden');
  const prompt = promptEl.value.trim();
  if(!prompt){
    errorEl.textContent = 'Please enter a prompt.';
    errorEl.classList.remove('hidden');
    return;
  }
  const payload = {
    prompt,
    size: sizeEl.value,
    quality: qualityEl.value
  };
  setLoading(true);
  try{
    const res = await fetch('/generate', {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){
      const err = await res.json().catch(()=>null);
      throw new Error(err?.detail || 'Generation failed');
    }
    const data = await res.json();
    setPreview(data.url);
    // refresh gallery
    fetchGallery();
  }catch(e){
    console.error(e);
    errorEl.textContent = e.message || 'Error';
    errorEl.classList.remove('hidden');
  }finally{
    setLoading(false);
  }
});

copyBtn.addEventListener('click', async ()=>{
  try{
    await navigator.clipboard.writeText(previewImg.src);
    copyBtn.textContent = 'Copied!';
    setTimeout(()=>copyBtn.textContent = 'Copy URL', 1200);
  }catch(e){}
});

window.addEventListener('load', ()=> {
  fetchGallery();
});
