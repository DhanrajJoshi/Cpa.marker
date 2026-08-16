# Text-to-Image Generator (Open-source)

Lightweight Text→Image web app with a FastAPI backend that proxies image generation requests to an external image-generation provider (example: OpenAI Images). Designed so API keys stay on the server (never in browser).

Features
- Prompt input
- Generate button with loading state
- Preview and download
- Gallery/history of generated images
- Aspect ratio / size and simple quality selection
- Backend reads API key from `.env` (do NOT commit `.env`)

Project structure
- frontend/ — static HTML/CSS/JS (index.html, style.css, app.js)
- backend/ — FastAPI app (main.py), requirements, .env.example
- generated/ — where generated images are stored (ignored by default)
- README.md, LICENSE, .gitignore

Quickstart (local)
1. Clone the repo.
2. Create a virtualenv and install backend deps:
   python -m venv .venv
   source .venv/bin/activate
   pip install -r backend/requirements.txt

3. Copy and edit backend/.env:
   cp backend/.env.example backend/.env
   # put your provider API key in backend/.env (OPENAI_API_KEY=...)

4. Run the backend:
   uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000

5. Open in browser:
   http://localhost:8000/frontend/index.html

Notes on providers & API keys
- The included backend uses an example OpenAI Images endpoint:
  POST to OPENAI_API_URL with JSON {"prompt": "...", "size":"512x512"}
  Many providers return base64 in data[0].b64_json or a public URL. Adjust backend as required for your provider.
- Keep your API key private: put it in backend/.env (which is in .gitignore).
- If you want to use another provider (Stability AI / Replicate / etc.), update backend/main.py request/response handling accordingly.

Security & production
- In production, do not allow frontend to talk directly to the external provider.
- Lock CORS origins to your frontend domain.
- Add rate-limiting and authentication to avoid misuse/charges.
- Consider storing images in cloud storage (S3) instead of server disk for scale.

License
This project is MIT-licensed — see LICENSE.
