# Gemini API

独立的 Spring Boot 后端，专门给前端 `/chat` demo 用。

## Endpoints

- `GET /api/health`
- `POST /api/gemini`

请求体：

```json
{
  "message": "Hello"
}
```

成功响应：

```json
{
  "reply": "..."
}
```

失败响应：

```json
{
  "error": "..."
}
```

## Run

```bash
cd /Users/jeremyli/2026-CSA-project/backend/gemini-api
cp .env.example .env
chmod +x run.sh
./run.sh
```

## Deploy notes

- 前端生产域名默认是 `https://2026-csa-project.pages.dev`
- 生产 API 目标域名是 `https://cs.andromedax.org`
- 建议服务器反向代理到本地 Spring Boot 端口
