# 2026-CSA-project

> *First-impressions of this repo — written as an outside reader seeing it fresh.*

---

## What this is

An AI-powered adaptive testing platform for AP Computer Science A students. Students log in with their school Google account, get AI-generated multiple-choice questions (powered by the Gemini API), and the difficulty adjusts after every answer. Teachers have a separate dashboard to monitor results. Honestly, that is a genuinely cool idea for a class project and the scope is ambitious.

---

## What works and looks solid

- **The frontend UI is polished.** The student login page, dashboard, and test pages have real CSS custom properties, responsive layouts, smooth transitions, difficulty-dot indicators, and skill-progress bars. Whoever did the CSS clearly put effort in. For a high-school class project this looks better than most.
- **The backend prompt engineering is thorough.** The Gemini prompt in `Main.java` is long, deliberate, and actually thought through — question variety rules, difficulty levels 1–5, anti-pattern rules like "no more than 25% output-prediction questions." Someone cared about question quality.
- **CORS, health check, and error handling exist.** The backend at least thought about non-2xx responses, missing API keys, and invalid JSON rather than just crashing.
- **The architecture folder shows design thinking.** Having a separate `architecture/` folder with model classes and an `AdaptiveEngine` suggests the team intended to separate concerns — that intent is good even if execution isn't there yet.

---

## What immediately stands out as concerning

### The backend is two different things at once

`Main.java` uses the raw JDK `com.sun.net.httpserver` API to build a server by hand. The `pom.xml` declares Spring Boot as the parent and lists Spring Boot Web, JDBC, and HikariCP as dependencies. These are two completely different ways to run a Java web server and they cannot both be "the backend" at the same time. It looks like two sub-teams started independently and nobody reconciled it. The `Application.java`, `ChatHandler.java`, and other Spring-style handler classes sitting next to `Main.java` reinforce this — it is unclear which one actually runs.

### The frontend API calls don't match any real backend endpoints

`integration.js` (the file gluing frontend pages to the backend) calls:
- `POST /login`
- `POST /startTest`
- `POST /nextQuestion`

The backend exposes:
- `POST /api/start-session`
- `POST /api/next-question`
- `POST /api/generate-question`
- `GET /api/health`

None of the frontend URLs match. If you click "Start Test" in the browser right now, every fetch will 404.

### Authentication is a stub with a real credential in it

`auth.js` has a real Google OAuth Client ID hardcoded in plain text. The backend endpoint it tries to call — `/api/auth/google` — does not exist anywhere in the Java source. When the backend is unreachable, the code silently falls back to saving the raw Google ID token in `sessionStorage` and moving on. That means authentication is effectively not enforced at all.

### The adaptive engine is three lines of math

`AdaptiveEngine.java` in the architecture folder:
```
if (correct) return min(max, current + 1);
return max(min, current - 1);
```

That is the entire adaptive algorithm. There is no item-response theory, no session history weighting, no topic tracking — just +1 or -1. It lives in `architecture/adaptive/` and is not imported anywhere in the actual backend. The backend just uses whatever difficulty number the frontend sends. The word "adaptive" in the product name is carrying a lot of weight right now.

### Hardcoded placeholder data is everywhere

The student dashboard shows "Jane Student", "Level 4 — Proficient", "62%", "Apr 14, 2026 — Passed", all hardcoded in the HTML. None of it comes from the API. A real user logging in would see Jane Student's fake data.

### Team coordination via a Markdown file

`frontend/student/WARNING.md` says:
> "Please don't make any changes without asking the student frontend team! Last time this happened, it reverted the changes and messed up works."

This is a sign that Git branching and PR reviews are not being used effectively. Conflict resolution through a warning text file is fragile — it will eventually fail.

### Documentation has personal machine paths in it

`backend/API.md` links to files as:
```
[Application.java](/Users/jeremyli/2026-CSA-project/backend/Application.java)
```
Those are absolute paths on one developer's Mac. They are dead links for everyone else.

### There are no tests

The `pom.xml` includes `spring-boot-starter-test` but there are zero test files in the project. A `test.java` file sits inside the `frontend/` folder (not a valid test location for either framework). The backend has no test coverage at all.

### The default Gemini model name doesn't exist

The documented default model is `gemini-3-flash-preview`. There is no Gemini 3 — the current model names are things like `gemini-1.5-flash` or `gemini-2.0-flash`. This will cause runtime failures unless an override environment variable is set.

---

## Overall feeling

This is a project where the *idea* and the *individual pieces* are actually pretty good — real Google OAuth scaffolding, a thoughtful AI prompt, a genuinely nice-looking UI, a domain model with `StudentProfile`, `Exam`, `TestSession`. But the pieces do not talk to each other yet. The frontend calls endpoints that don't exist on the backend. The backend has two competing implementations. The adaptive engine is in a folder that nothing imports. The auth flow silently bypasses itself.

It feels like multiple sub-teams built their parts in parallel without a shared integration meeting to agree on API contracts, file structure, or which framework to use — and now those parts need to be stitched together. The surface looks further along than the integration actually is.

The biggest single thing that would improve this repo: **one working end-to-end path** — student logs in → session starts → one question loads from Gemini → student answers → next question appears at the right difficulty. Everything else can be polished after that exists.
