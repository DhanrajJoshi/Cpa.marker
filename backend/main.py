from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from fastapi.responses import RedirectResponse
import os, base64, uuid, datetime, asyncio, json
import httpx
from dotenv import load_dotenv
import aiofiles

load_dotenv()

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
# Default endpoint for OpenAI Images generative example. You may change via OPENAI_API_URL env var.
OPENAI_API_URL = os.getenv("OPENAI_API_URL", "https://api.openai.com/v1/images/generations")

GENERATED_DIR = os.path.join(os.getcwd(), "generated")
os.makedirs(GENERATED_DIR, exist_ok=True)

app = FastAPI(title="Text-to-Image Generator (backend)")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # in production lock this down
    allow_methods=["*"],
    allow_headers=["*"],
)

# Serve frontend and generated files
app.mount("/frontend", StaticFiles(directory=os.path.join(os.getcwd(), "frontend")), name="frontend")
app.mount("/generated", StaticFiles(directory=GENERATED_DIR), name="generated")

class GenRequest(BaseModel):
    prompt: str
    size: str = "512x512"
    quality: str = "standard"

@app.get("/")
def root():
    return RedirectResponse(url="/frontend/index.html")

@app.post("/generate")
async def generate(req: GenRequest):
    """
    Proxies the generation request to an external image API (example: OpenAI Images).
    Requires OPENAI_API_KEY set in environment (.env).
    Returns {"url": "/generated/<file>.png", "filename": "..."}
    """
    if not OPENAI_API_KEY:
        raise HTTPException(status_code=500, detail="OPENAI_API_KEY not configured on server")

    payload = {
        "prompt": req.prompt,
        "size": req.size
    }

    # Add any model/quality mapping if needed
    if req.quality == "high":
        # Example: some providers accept additional params; adapt per provider.
        payload["quality"] = "high"

    headers = {
        "Authorization": f"Bearer {OPENAI_API_KEY}",
        "Content-Type": "application/json"
    }

    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            resp = await client.post(OPENAI_API_URL, json=payload, headers=headers)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed contacting image API: {e}")

    if resp.status_code != 200:
        # Bubble up errors for easier debugging
        raise HTTPException(status_code=500, detail=f"Image API error: {resp.status_code} - {resp.text}")

    data = resp.json()

    # Many image APIs (e.g., OpenAI) return base64 in data[0].b64_json
    b64 = None
    try:
        b64 = data["data"][0].get("b64_json") or data["data"][0].get("b64")
    except Exception:
        pass

    if not b64:
        # If provider instead returns a URL, try to fetch it
        try:
            url = data["data"][0].get("url")
            if url:
                # fetch binary
                async with httpx.AsyncClient(timeout=60.0) as client:
                    r2 = await client.get(url)
                    if r2.status_code == 200:
                        content = r2.content
                        fname = f"{datetime.datetime.utcnow():%Y%m%d%H%M%S}_{uuid.uuid4().hex[:8]}.png"
                        out_path = os.path.join(GENERATED_DIR, fname)
                        async with aiofiles.open(out_path, "wb") as f:
                            await f.write(content)
                        return {"url": f"/generated/{fname}", "filename": fname}
        except Exception:
            pass
        raise HTTPException(status_code=500, detail=f"No image data returned from provider: {json.dumps(data)[:400]}")

    try:
        img_bytes = base64.b64decode(b64)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to decode image data: {e}")

    fname = f"{datetime.datetime.utcnow():%Y%m%d%H%M%S}_{uuid.uuid4().hex[:8]}.png"
    out_path = os.path.join(GENERATED_DIR, fname)
    async with aiofiles.open(out_path, "wb") as f:
        await f.write(img_bytes)

    return {"url": f"/generated/{fname}", "filename": fname}

@app.get("/gallery")
def gallery():
    """
    Returns list of generated images (filename, url, created timestamp)
    """
    items = []
    for fn in sorted(os.listdir(GENERATED_DIR), reverse=True):
        if fn.startswith("."): continue
        path = os.path.join(GENERATED_DIR, fn)
        stat = os.stat(path)
        items.append({
            "filename": fn,
            "url": f"/generated/{fn}",
            "created": datetime.datetime.utcfromtimestamp(stat.st_mtime).isoformat()+"Z"
        })
    return items
