# How To Connect Frontend, Backend, APIs, and the Current Chat Demo

This document describes the current working setup for the `2026-CSA-project` frontend, the Spring Boot backend, and the public chat demo.

## Current Architecture

The current chat demo uses this flow:

`Browser -> Cloudflare Pages frontend -> public API domain -> Nginx -> Spring Boot -> Gemini API`

In production:

- Frontend root: `https://2026-csa-project.pages.dev/`
- Student entry: `https://2026-csa-project.pages.dev/student/`
- Chat demo: `https://2026-csa-project.pages.dev/chat/`
- Public backend API: `https://cs.andromedax.org/api/...`

On the backend server:

- Spring Boot listens on `http://127.0.0.1:8080`
- Nginx proxies `https://cs.andromedax.org/api/...` to `http://127.0.0.1:8080/api/...`

## Frontend Structure

Relevant frontend paths:

- `frontend/chat/index.html`
- `frontend/chat/app.js`
- `frontend/chat/styles.css`
- `frontend/student/index.html`
- `frontend/_redirects`

### Route behavior

The current Pages routing is defined in `frontend/_redirects`:

- `/` -> `/student/`
- `/student/` -> student sign-in page
- `/chat/` -> public chat demo

That means the default site entry is still the student portal, but the chat demo remains publicly reachable when the path is manually changed to `/chat/`.

## Current Chat Demo Behavior

The chat demo is a static frontend page under `frontend/chat/`.

Its API logic is in `frontend/chat/app.js`.

### API target selection

The page uses:

- `http://localhost:8080/api/gemini` when opened on `localhost` or `127.0.0.1`
- `https://cs.andromedax.org/api/gemini` in production

It also supports a manual override with a query string:

`/chat/?api=https://your-api-host/api/gemini`

### Request format

The frontend sends:

```json
{
  "message": "Hello"
}
```

### Success response

```json
{
  "reply": "..."
}
```

### Error response

```json
{
  "error": "..."
}
```

## Backend Structure

The current backend for the demo is:

- `backend/gemini-api`

This is the backend that should be used for the chat demo.

Do not rely on the older mixed backend code for the public chat route.

Relevant backend paths:

- `backend/gemini-api/pom.xml`
- `backend/gemini-api/src/main/resources/application.properties`
- `backend/gemini-api/src/main/java/org/andromedax/csa/geminiapi/controller/GeminiController.java`
- `backend/gemini-api/src/main/java/org/andromedax/csa/geminiapi/controller/HealthController.java`
- `backend/gemini-api/src/main/java/org/andromedax/csa/geminiapi/service/GeminiService.java`

## Backend API Contract

### Health check

`GET /api/health`

Response:

```json
{
  "status": "ok"
}
```

### Chat endpoint

`POST /api/gemini`

Request:

```json
{
  "message": "Say hello"
}
```

Success response:

```json
{
  "reply": "Hello..."
}
```

Error response:

```json
{
  "error": "..."
}
```

## Local Development

### Run the frontend locally

```bash
cd /Users/jeremyli/2026-CSA-project/frontend
python3 -m http.server 4173
```

Then open:

- `http://localhost:4173/student/`
- `http://localhost:4173/chat/`

### Run the backend locally

```bash
cd /Users/jeremyli/2026-CSA-project/backend/gemini-api
cp .env.example .env
```

Example `.env` values:

```bash
GEMINI_API_KEY=your_real_key
GEMINI_MODEL=gemini-2.5-flash
PORT=8080
CORS_ALLOWED_ORIGINS=https://2026-csa-project.pages.dev,http://localhost:4173
```

Start it:

```bash
set -a
source .env
set +a
java -jar target/gemini-api-0.0.1-SNAPSHOT.jar
```

Or with Maven during development:

```bash
mvn spring-boot:run
```

## Production Deployment

### Frontend deployment on Cloudflare Pages

Current recommended Pages settings:

- Root directory: `frontend`
- Build command: empty
- Build output directory: `.`

With that setup:

- `/student/` serves the student portal
- `/chat/` serves the chat demo
- `/` redirects to `/student/`

### Backend deployment on the server

Current deployment target:

- Repo path on server: `~/cs-api-repo/backend/gemini-api`

Recommended build approach on low-memory servers:

1. Build the jar on the local Mac
2. Upload the jar to the server
3. Restart the service on the server

Local build:

```bash
cd /Users/jeremyli/2026-CSA-project/backend/gemini-api
mvn -Dmaven.test.skip=true package
```

Upload:

```bash
scp -i /Users/jeremyli/.ssh/digitalocean \
  /Users/jeremyli/2026-CSA-project/backend/gemini-api/target/gemini-api-0.0.1-SNAPSHOT.jar \
  root@165.245.232.197:/root/cs-api-repo/backend/gemini-api/target/
```

### systemd

The backend should be managed by `systemd` so it keeps running after SSH disconnects.

The service should point to the new demo backend jar under:

`/root/cs-api-repo/backend/gemini-api/target/gemini-api-0.0.1-SNAPSHOT.jar`

### Nginx

Nginx should expose the backend like this:

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
    proxy_http_version 1.1;

    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

That makes the public API available at:

- `https://cs.andromedax.org/api/health`
- `https://cs.andromedax.org/api/gemini`

## Quick Verification Checklist

### Frontend

Check:

- `https://2026-csa-project.pages.dev/` redirects to `/student/`
- `https://2026-csa-project.pages.dev/student/` loads the student page
- `https://2026-csa-project.pages.dev/chat/` loads the chat demo

### Backend

Check locally on the server:

```bash
curl http://127.0.0.1:8080/api/health
curl -X POST http://127.0.0.1:8080/api/gemini \
  -H 'Content-Type: application/json' \
  -d '{"message":"Hello"}'
```

Check publicly:

```bash
curl https://cs.andromedax.org/api/health
curl -X POST https://cs.andromedax.org/api/gemini \
  -H 'Content-Type: application/json' \
  -d '{"message":"Hello"}'
```

## Common Failure Modes

### `/chat` shows the student login page

Cause:

- Cloudflare Pages root directory was set to `frontend/student` instead of `frontend`

Fix:

- Change Pages root directory to `frontend`
- Redeploy

### `/api/gemini` returns `Missing GEMINI_API_KEY environment variable.`

Cause:

- The runtime process did not actually receive `GEMINI_API_KEY`

Fix:

- Check `.env`
- Make sure `systemd` uses `EnvironmentFile=...`
- If running manually, `source .env` before `java -jar`

### `/api/gemini` returns `429`

Cause:

- Gemini upstream quota or rate limit

Fix:

- Check key, quota, billing, and model access

### Build gets killed on the server

Cause:

- The server has too little RAM for Maven test/build overhead

Fix:

- Build locally
- Upload the jar
- Restart only the runtime service on the server

## Current Recommended Source of Truth

For the chat demo integration, the current source of truth is:

- `frontend/chat/*`
- `frontend/_redirects`
- `backend/gemini-api/*`

If future work changes the API contract, update this file together with the frontend and backend code.
