 ..
# Micro Time Management — CLAUDE.md

This file is the living architectural reference for the MTM project — package layout, data models, security wiring, endpoint map, completed work, and pending work — kept in step with the code. The README links here for the same reasons. Update it after every meaningful code change. (Previously gitignored; tracked from this commit on so cloners see the references the README invites them to.)

---

## What the App Does

A time/activity tracking app. Users register, log in, and record daily "activities" — time blocks with a name, description, start/end times (hour + meridian), and a date. Admins manage user roles. The backend enforces fine-grained role-based access control; the frontend is in progress.

---

## Repository Layout

```
MicroTimeManagement/
├── backend/api-service/          Spring Boot 3.x REST API (Maven) + multi-stage Dockerfile
├── frontend/                     React 18 SPA (CRA) + Dockerfile (nginx) + nginx.conf
├── .github/workflows/ci.yml      GitHub Actions CI (backend tests + frontend build/test)
├── docker-compose.yml            Full stack: MongoDB + backend + frontend (+ mongo-express via `tools` profile)
└── README.md
```

### Running the whole app

```
docker compose up --build            # postgres + backend + frontend + adminer
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080/mtm-dev (Swagger at `/swagger-ui.html`)
- Adminer (DB UI): http://localhost:8081 (System: PostgreSQL, Server: `mtm_postgres`, user/pass/db: `mtm`/`mtm`/`mtm_dev`)
- PostgreSQL: localhost:5432 (data persisted in the `mtm_postgres_data` volume)

Backend env knobs (see `docker-compose.yml`): `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`, `MTM_JWT_SECRET`, `MTM_CORS_ORIGINS`. The frontend's API base URL is baked at image-build time via the `REACT_APP_API_BASE_URL` build arg (default `http://localhost:8080/mtm-dev/api/v1`); running locally with `yarn start` it falls back to the same default.

---

## Backend

### Tech Stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 3.5.10, Java 17 |
| Database | PostgreSQL via Spring Data JPA / Hibernate (H2 in-memory for tests) |
| Auth | Custom JWT (`jjwt 0.13.0`) + session-in-DB validation |
| API Docs | springdoc-openapi 2.8.15 (Swagger UI) |
| Security | Spring Security 6, custom `MtmSessionFilter` |
| Validation | `spring-boot-starter-validation` (Bean Validation) |
| Build | Maven, fat-jar via `spring-boot-maven-plugin` |

### Package Structure

```
com.microtimemanagement.apiservice
├── callbacks/       BeforeConvertCallback implementations (audit fields)
├── config/          SecurityConfig (profile-specific filter chain + CORS)
├── constants/       ApiConstants, RoleConstants, SecurityConstants, ErrorConstants, ResponseMessages, PaginationConstants
├── controller/      AuthenticationController, UserController, RoleController, ActivityRecordController, AdminController
├── converter/       DTO ↔ model converters (BaseDTOConverter base class)
├── dto/
│   ├── entity/      Read DTOs (what API returns)
│   ├── request/     Write DTOs (what API accepts)
│   └── response/    Composite response wrappers
├── enums/           TimeMeridian, UserRoleUpdateAction, ApiResourceType, NextDayOffsetAction
├── exceptions/      Exception hierarchy
├── filter/          MtmSessionFilter (OncePerRequestFilter)
├── handler/         MicroTimeManagementResourceExceptionHandler (@ControllerAdvice)
├── model/           MongoDB document models
├── repository/      Spring Data MongoDB repositories
├── service/         Service interfaces + impl/ sub-package for implementations
└── utils/           ApiUtils, AuthUtils, JwtUtils
```

### Data Models & PostgreSQL Tables (JPA)

All models extend `BaseModel` (a `@MappedSuperclass` with `isActive`, `createdAt`, `lastUpdatedAt`). These fields are maintained by JPA lifecycle callbacks (`@PrePersist`/`@PreUpdate`) **in `BaseModel` itself** — the old Mongo `BeforeConvertCallback` classes in `callbacks/` were deleted in the Postgres migration.

| Entity | Table | Notes |
|---|---|---|
| `User` | `mtm_user` | Implements `UserDetails`. `roles` is a `@ElementCollection` of **role IDs** (join table `mtm_user_roles`), eager, resolved to names at auth time. `uid` assigned via `@PrePersist`. IDs are `@GeneratedValue(UUID)` strings. |
| `Role` | `mtm_role` | Simple `id + name`. |
| `Session` | `mtm_session` | Owns the FK to `RefreshToken` (`@OneToOne`, eager) + `@ManyToOne` to the principal `User`. |
| `RefreshToken` | `mtm_refresh_token` | `@OneToMany` to `AccessToken` — **owning side via `@JoinColumn`** (saving the token wires up children's FK, so the auth services needed no changes), eager, `orphanRemoval`. `@OneToOne(mappedBy)` back-ref to `Session`. Token/back-ref fields excluded from `equals`/`toString` to avoid cyclic recursion. |
| `AccessToken` | `mtm_access_token` | Read-only `@ManyToOne` back-ref to `RefreshToken` (`insertable=false, updatable=false`). `token` column widened to `length=4096` (JWTs exceed varchar(255)). |
| `ActivityRecord` | `mtm_activity_record` | `recordDate` (String, `yyyy-MM-dd`), `createdBy` (user UID), `@OneToMany` unidirectional-owning `activities` (`@JoinColumn`, eager, `orphanRemoval`, `@OrderBy startTimeEpoch`). |
| `Activity` | `mtm_activity` | Own table. Application-assigned `@Id` (set in `setUniqueIdForActivity`). `TimeMeridian` fields are `@Enumerated(STRING)`. `activityDescription` is `length=2000`. |

**Key design decision — roles as IDs:** `User.roles` stores role IDs. `UserServiceImpl.replaceRoleIdsWithNamesForUser()` resolves them to role-name strings (`ROLE_MTM_...`) before setting Spring Security authorities — on every `loadUserByUsername` and `getUserByUid` call. **JPA gotcha (fixed):** that swap mutates the loaded entity, and with `open-in-view: true` Hibernate would flush the names back over the stored IDs (corrupting them, then wiping them on the next login). `replaceRoleIdsWithNamesForUser` now `entityManager.detach(user)`s before the swap so it's never persisted; `modifyUserRoles` uses `saveAllAndFlush` so the real ID-role change lands before the detach.

### Auth & Session Flow

1. **Login** (`POST /api/v1/auth/login`): Validates password → creates `Session` + `RefreshToken` + `AccessToken`. Any existing active session for the user is revoked first (single-session-per-user enforcement).
2. **Every request**: `MtmSessionFilter` extracts `Bearer` token → calls `sessionService.validateSessionForAccessToken()` → sets Spring Security context principal.
3. **Logout** (`POST /api/v1/auth/logout`): Marks access token inactive → revokes refresh token → marks session inactive.
4. **Refresh** (`POST /api/v1/auth/refresh`): Validates refresh token → checks session is active → generates new access token.

The `Session` → `RefreshToken` → `AccessToken` chain is stored as MongoDB `@DocumentReference` links (lazy-loaded where appropriate).

### Role-Based Access Control

Spring Security is configured profile-specifically in `SecurityConfig`.

**Dev profile roles:**

| Role constant | Grants access to |
|---|---|
| `ROLE_MTM_ADMIN_OPS` | `/api/v1/admin/**`, `GET /api/v1/user/all`, `POST /api/v1/user/addRole`, `DELETE /api/v1/user/removeRole`, `/actuator/**` |
| `ROLE_MTM_USER_OPS` | `/api/v1/user/**`, `POST /api/v1/auth/logout` |
| `ROLE_MTM_ACTIVITY_CRUD` | `/api/v1/activity/**` |
| `ROLE_MTM_ROLE_CRUD` | `/api/v1/role/**` |

**Open endpoints (no auth):** `POST /login`, `POST /auth/refresh`, `POST /user/register`, Swagger UI, `/error`.

**Default roles on registration:** `ROLE_MTM_USER_OPS` + `ROLE_MTM_ACTIVITY_CRUD`.

**Note:** `SecurityConstants.PROD` is an empty stub — prod filter chain hardcodes paths directly in `SecurityConfig`. Should be refactored to match the dev pattern.

**CORS:** dev allowed origins are configurable via `MTM_CORS_ORIGINS` (comma-separated; `${mtm.cors.origins:...}`, default `http://localhost:3000,http://localhost:8080`). Both dev and prod CORS configs allow `PUT` and `OPTIONS` in addition to GET/POST/PATCH/DELETE — the previous list omitted `PUT`, which would have blocked cross-origin update calls (activity/role/user updates).

### API Endpoint Map

```
POST   /api/v1/auth/login                 Login (open)
POST   /api/v1/auth/logout                Logout (USER_OPS)
POST   /api/v1/auth/refresh               Token refresh (open)

POST   /api/v1/user/register              Register (open)
GET    /api/v1/user/profile               Current user profile (USER_OPS)
PUT    /api/v1/user/update                Update user details (USER_OPS)
DELETE /api/v1/user/delete                Delete current user (USER_OPS)
POST   /api/v1/user/resetPassword         Change password (USER_OPS)
GET    /api/v1/user/getByUserId           Get user by UID (USER_OPS)
GET    /api/v1/user/all                   Paginated user list (ADMIN_OPS)
POST   /api/v1/user/addRole               Add roles to users (ADMIN_OPS)
DELETE /api/v1/user/removeRole            Remove roles from users (ADMIN_OPS)

POST   /api/v1/role                       Create role (ROLE_CRUD)
PUT    /api/v1/role                       Update role name — body: RoleUpdateRequestDTO (roleId + roleName) (ROLE_CRUD)
DELETE /api/v1/role?roleId=               Soft-delete role (ROLE_CRUD)
GET    /api/v1/role?page=&size=&roleId=   List / get role (ROLE_CRUD)

POST   /api/v1/activity                   Create/append activity (ACTIVITY_CRUD)
GET    /api/v1/activity/getAllForDate?date= Get activities for date (ACTIVITY_CRUD)
PUT    /api/v1/activity?date=             Update activity — body: ActivityUpdateRequestDTO (recordId + optional name/description/times) (ACTIVITY_CRUD)
DELETE /api/v1/activity?date=&recordId=   Delete activity from record (ACTIVITY_CRUD)
GET    /api/v1/activity/stats?from=&to=   Aggregate stats over window (defaults to last 7 days) (ACTIVITY_CRUD)
GET    /api/v1/activity/history?page=&size= Paginated per-day history, newest first (ACTIVITY_CRUD)
GET    /api/v1/activity/names                Distinct activity names previously used, most-recent first (ACTIVITY_CRUD)

POST   /api/v1/project                       Create project (ACTIVITY_CRUD)
GET    /api/v1/project                       List current user's projects (ACTIVITY_CRUD)
GET    /api/v1/project/{id}                  Get project (ACTIVITY_CRUD)
PUT    /api/v1/project/{id}                  Update project — partial (ACTIVITY_CRUD)
DELETE /api/v1/project/{id}                  Soft-delete project (ACTIVITY_CRUD)

POST   /api/v1/task                          Create task (ACTIVITY_CRUD)
GET    /api/v1/task[?projectId=|?parentTaskId=] List tasks — all / by project / sub-tasks (ACTIVITY_CRUD)
GET    /api/v1/task/{id}                     Get task (ACTIVITY_CRUD)
PUT    /api/v1/task/{id}                      Update task — partial (ACTIVITY_CRUD)
DELETE /api/v1/task/{id}                     Soft-delete task (ACTIVITY_CRUD)

POST   /api/v1/link                          Create workflow link (ACTIVITY_CRUD)
GET    /api/v1/link[?sourceType=&sourceId=]  List links — all / originating from an entity (ACTIVITY_CRUD)
DELETE /api/v1/link/{id}                     Soft-delete link (ACTIVITY_CRUD)

POST   /api/v1/reminder                      Create reminder (ACTIVITY_CRUD)
GET    /api/v1/reminder                      List current user's reminders, soonest first (ACTIVITY_CRUD)
GET    /api/v1/reminder/{id}                 Get reminder (ACTIVITY_CRUD)
PUT    /api/v1/reminder/{id}                 Update reminder — partial incl. status (ACTIVITY_CRUD)
DELETE /api/v1/reminder/{id}                 Soft-delete reminder (ACTIVITY_CRUD)

POST   /api/v1/admin/                     Create role via admin (ADMIN_OPS) [legacy, use /role]
DELETE /api/v1/admin/                     Soft-delete role via admin (ADMIN_OPS) [legacy]
GET    /api/v1/admin/?roleName=           Get role by name (ADMIN_OPS) [legacy]
```

### Activity Record Logic

`ActivityRecord` is a date-scoped container for a user's activities. Activities within a record are kept sorted chronologically. The creation service (`ActivityRecordServiceImpl.makeFromRecordLogRequestDTO`) enforces:
- No duplicate time ranges (same start + end epoch)
- No overlapping time ranges with existing activities
- Insertion at the correct chronological position in the list

### Exception Hierarchy

```
MicroTimeManagementException (base)
├── MicroTimeManagementAuthenticationException  → 403
├── MicroTimeManagementBadRequestException      → 400
├── MicroTimeManagementNotFoundException        → 404
└── MicroTimeManagementUserException            → 4xx (user-specific errors)
```

`MicroTimeManagementResourceExceptionHandler` (@ControllerAdvice) catches these and returns `ExceptionDTO` / `MicroTimeManagementExceptionDTO`.

### Configuration

- **Dev**: `application-dev.yml` — port 8080, context `/mtm-dev`, PostgreSQL via `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/mtm_dev`, user/pass `mtm`/`mtm`), Hibernate `ddl-auto: update`, `open-in-view: true`, JWT secret env-overridable
- **Test**: `src/test/resources/application.properties` — in-memory H2 (`MODE=PostgreSQL`, `ddl-auto: create-drop`) so the suite (incl. `contextLoads`) needs no external DB
- **Prod**: `application-prod.yml` — separate config; CORS locked to `https://mtm.app`
- **Active profile**: set via `SPRING_PROFILES_ACTIVE` env var or `application.properties`
- **Docker Compose**: full stack — PostgreSQL (5432, persisted volume, health-checked), backend (8080, multi-stage build, health-checked), frontend (3000, nginx-served CRA bundle), Adminer (8081, DB UI). You can still run the Spring Boot app directly against a local/compose Postgres in dev.
- **Backend Dockerfile**: multi-stage (`maven:3.9-eclipse-temurin-17` build → `eclipse-temurin:17-jre` runtime). Builds the fat jar inside the image; tests run in CI, not the image build.
- **Frontend Dockerfile**: multi-stage (`node:18-alpine` build → `nginx:1.27-alpine` runtime) with SPA fallback in `nginx.conf`.

---

## Frontend

### Tech Stack

| Concern | Choice |
|---|---|
| Framework | React 18, CRA (`react-scripts 5`) |
| Routing | React Router v6 |
| Styling | Tailwind CSS 3.2 (prefix `mtm-`) + custom CSS-variable design system (light/dark). Bootstrap/AntD remain as legacy deps but are no longer imported. |
| HTTP Client | Axios |
| State | `useState` local state (no global store) |
| Icons | `react-icons` |

**Tailwind prefix is `mtm-`** — all Tailwind utility classes must be prefixed (e.g. `mtm-flex`, `mtm-text-content`). Configured in `tailwind.config.js`.

### Design system (professional revamp)

The UI was rebuilt into a cohesive emerald/teal design system with full light + dark theming:

- **Theme tokens** live in `src/style/tailwind.css` as CSS custom properties (RGB triplets under `:root[data-theme="light|dark"]`). `tailwind.config.js` surfaces them as color utilities (`mtm-bg-surface`, `mtm-text-content`, `mtm-text-primary`, `mtm-border-line`, `mtm-text-muted`, `mtm-text-ok/danger/warn`, …) via `rgb(var(--token) / <alpha-value>)`, so opacity modifiers work and flipping `<html data-theme>` reskins the whole app — no per-element `dark:` variants.
- **Component primitives** (plain CSS classes, not Tailwind): `.ui-card`, `.ui-card-flat`, `.ui-btn` (+ `ui-btn-primary/ghost/soft/danger/sm`), `.ui-input`/`.ui-select`/`.ui-textarea`, `.ui-label`, `.ui-badge`/`.ui-chip`, `.ui-stat`, `.ui-icon-tile`, `.ui-eyebrow`, `.ui-page`, `.ui-spinner`, `.ui-fade-in`.
- **Fonts**: Inter (body) + Sora (display/headings), replacing the old comic Bangers face.
- **Theme toggle**: `src/hooks/useTheme.js` persists the choice in `localStorage` (`mtm-theme`) and reflects it on `<html data-theme>`; a no-flash inline script in `public/index.html` applies it before first paint. `src/components/ThemeToggle.js` is the toggle button in the nav.
- The old comic/meme aesthetic (Bangers font, meme GIFs, `Pages/sections/home/*`, `Memes`/`SingleMeme`, the `MtmForm`/`Button` component library) was removed; pages now use native inputs + the `ui-*` primitives.
- `NavigationBar` was rewritten without react-bootstrap into a clean sticky nav with a mobile menu, active-link highlighting, and the theme toggle.

### Page / Component Map

```
src/
├── Pages/
│   ├── App.js             Root: nav + toast overlay + route tree + footer
│   ├── Home.js            Landing page
│   ├── Login.js           Login form — wired to /auth/login + redirect to /dashboard (or location.state.from)
│   ├── Registration.js    Registration form — API fully wired
│   ├── Dashboard.js       Protected landing page — Today / 7d / 30d preset stat cards (total time, activity count, days tracked, avg/active day), top activities by duration, recent activities. Wired to /activity/stats.
│   ├── Activity.js        Protected page — date picker + list + create/edit/delete activities. Reads `?date=YYYY-MM-DD` from URL (set by the History page deep-link) and keeps the URL in sync with the picker.
│   ├── History.js         Protected page — paginated list of every tracked day, newest first. Each row links into /activity?date=. Page-size selector (10/20/50) + prev/next.
│   ├── Profile.js         Protected page — view/edit profile + change password
│   ├── Admin.js           Admin-only page — tabbed UI: Roles (CRUD: list/create/rename/soft-delete) + Users (paginated list, edit each user's role membership with checkbox diff)
│   └── sections/home/     Home page sections (Top, HowItWorks, ButWhy, NextSteps)
├── components/
│   ├── NavigationBar.js   Bootstrap Navbar — "Sign in" + "Try Now!" links only
│   ├── Footer.js
│   ├── Toast.js           Custom toast notification
│   ├── Button.js
│   ├── Memes.js / SingleMeme.js
│   ├── forms/
│   │   ├── MtmForm.js     Form container, exposes .Input .Select .Option .TextArea sub-components
│   │   ├── MtmInput.js
│   │   ├── MtmSelect.js
│   │   └── MtmTextArea.js
│   └── hoc/
│       └── MtmStyleWrap.js  HOC that wraps form inputs with label + Tailwind styles
├── service/
│   └── ApiService.js      Only contains registerUser() — registration API call
└── style/
    ├── App.css
    └── tailwind.css
```

### Routing

```
/           → Home (public)
/login      → Login (public)
/register   → Registration (public)
/dashboard  → Dashboard (protected)
/activity   → Activity tracker (protected; accepts `?date=YYYY-MM-DD`)
/history    → Paginated activity history (protected)
/projects   → Projects grid + create (protected)
/projects/:id → Project detail — task board (TODO/In-progress/Done) + sub-tasks (protected)
/reminders  → Reminders — schedule + in-app/browser notify (protected)
/profile    → User profile + password change (protected)
/admin      → Role management (admin-only, gated by ROLE_MTM_ADMIN_OPS)
```

Protected routes are wrapped in `ProtectedRoute` and redirect unauthenticated users to `/login`. `Login` reads `location.state.from` so the user is sent back to the page they tried to visit after signing in (falls back to `/dashboard`). Profile/admin routes are not built yet.

### Activity Tracker Page

`Pages/Activity.js` is the core product UI. It exposes:
- Date picker (HTML `<input type="date">`, defaults to today) — switching dates re-fetches.
- List of activities for the selected date, each row showing start → end time, duration, name, description.
- Inline **New Activity** form (name, description, start time, end time via HTML `<input type="time">`) — submits `POST /activity`.
- **Edit** opens the same form pre-populated with name/description; start/end stay blank and are only sent when the user wants to retime (the backend's `ActivityUpdateRequestDTO` treats blank times as "keep").
- **Delete** confirms then calls `DELETE /activity`.
- Toast feedback for every success/error.

The frontend stores times as 24-hour `HH:MM` from the native time input and converts to the backend's `HH:MM:M` format (M is 0 for AM, 1 for PM) at submit time via `toBackendTimeString`. The backend's display payload (`activityStartTime`, `activityEndTime`) is rendered as-is.

### Toast System

Toast state lives in `App.js` and is passed as `{ toastState, setToastState }` props to pages. Structure:
```js
{ display: bool, variant: "success"|"error", messages: string[], includePrefix: bool, includeSuffix: bool, suffix: string }
```

### API Service

`src/service/ApiService.js` exports a shared Axios instance (`apiClient`) plus `registerUser`, `loginUser`, `logoutUser`, an Activity API (`createActivity`, `getActivitiesForDate`, `updateActivity`, `deleteActivity`), a Profile API (`getUserProfile`, `updateUserDetails`, `changeUserPassword`), and an Admin Role API (`listRoles`, `createRole`, `updateRole`, `deleteRole`). The client base URL is `http://localhost:8080/mtm-dev/api/v1`. Two interceptors are wired:

- **Request**: attaches `Authorization: Bearer <accessToken>` for any non-public path when a token is present (`/auth/login`, `/auth/refresh`, `/user/register` are public).
- **Response**: on a 401 for a non-public request that hasn't been retried, calls `POST /auth/refresh` with the stored refresh token, stores the new pair via `AuthStorage.setTokens`, retries the original request once. A single in-flight refresh is shared across concurrent 401s. If refresh fails, tokens are cleared and the browser is redirected to `/login`.

`src/service/AuthStorage.js` is the single source of truth for tokens in `localStorage`. It exposes `getAccessToken`, `getRefreshToken`, `setTokens`, `clearTokens`, `isAuthenticated`, and `subscribeAuth(listener)`. Listeners fire on local mutations and on cross-tab `storage` events.

`src/hooks/useAuth.js` is a hook over `subscribeAuth` that also fetches the current user's profile when authenticated and exposes `{ isAuthenticated, roles, isAdmin, profileLoaded }`. `isAdmin` is true when the roles set contains `MTM_ADMIN_OPS` (with or without the `ROLE_` prefix). `src/components/ProtectedRoute.js` uses it to gate routes for any authenticated user; `src/components/AdminRoute.js` additionally requires `isAdmin` and waits for `profileLoaded` so admins aren't bounced on first paint.

---

## AI Chat Integration (Planned, not started)

> **Hard gate**: this work does not begin until every item in [Pending Tasks](#pending-tasks) below (Backend + Frontend subsections) is shipped *and* the user has completed a hands-on testing pass on the current build. Until that gate is met, this section is a forward-looking design doc, not an active workstream.

### What it is

An LLM-backed chat panel inside MTM. The user types in natural language ("I had a 1-hour design review this morning, log it") and the LLM calls server-side tools to act on the user's behalf — creating, listing, summarising activities. The model is hosted by MTM (proxied to Anthropic or an Anthropic-compatible API); the user's auth is their normal MTM session — no new token type, no new protocol surface.

This is **single-turn-with-tools**, not a chatbot. The scope is firm: the model understands what's happening *now*, calls a tool or two, reports back. It does not maintain long-term memory across sessions, does not hold open-ended multi-turn dialogues, does not chit-chat.

### Premortem

Imagine the version we ship 12 months out is regarded as a flop. The most likely causes — and what the design here does about each:

| Failure mode | What goes wrong | Mitigation baked into the design |
|---|---|---|
| Cost spiral | One bad day, 200 messages, no rate limit → Anthropic bill spikes | Per-user daily token cap + per-message ceiling. `ChatUsage` document tracks daily spend. Optional BYOK so power users provide their own key. |
| Chatbot rabbit-hole | Chat grows memory, multi-turn, citations, attachments… focus drifts from time tracking | **Scope guard**: single-turn-with-tools. No persisted conversation memory past current session. No general chit-chat fallback. Explicit list of out-of-scope features below. |
| Time/date hell | "Log standup this morning"; server is UTC, user is IST → activity ends up 5.5 h off → overlap fires → AI retries badly → duplicates appear | Dedicated `get_context` tool returns `{ today, timezone, locale, currentUser }`. Tool inputs accept both ISO-8601 and `HH:mm`. Frontend sends the user's browser timezone with every chat request so `get_context` doesn't lie. |
| Opaque errors | Backend's user-friendly error strings ("Activity overlap detected") are useless to the model — it can't tell whether to retry, adjust, or surface | Structured tool error envelope `{ ok, code, message, hint, data }` with a fixed code vocabulary (`OVERLAP`, `INVALID_TIME`, `CONFLICT`, …) the model can recover from. |
| Tool sprawl | Expose one tool per endpoint → AI hallucinates names, picks the wrong one | **5 task-shaped tools** at MVP. Add more only when usage shows a recurring miss. |
| No audit trail | User asks "did I create this, or did the AI?" — we can't tell | New `source` enum (`WEB` / `AI_CHAT` / `API`) on every `Activity` / `ActivityRecord`. Source badge in the UI. |
| Concurrency race | AI + web UI both create overlapping activities the same instant; current read-then-write overlap check has no DB lock | `@Version Long` on `ActivityRecord` → optimistic lock → `CONFLICT` code → AI re-reads and decides instead of blind-retrying. |
| LLM lock-in | Tied to Anthropic; if pricing changes or a model deprecates, we're stuck | Thin `LlmProvider` interface so OpenAI / local Ollama can plug in later. Anthropic is the only implementation at MVP. |
| Shipping chat on a moving API | Underlying activity APIs still mutating; every change forces a tool description rewrite | Hard gate. No chat work until the base app is shipped + user-tested. |
| Streaming complexity | We try to stream from Anthropic and tool calls don't fit cleanly → buggy partial UI states | Phase 1: non-streamed responses (full message + tool result land at once). Phase 2: SSE streaming once the baseline works. |
| Security: chat as IDOR vector | Model is convinced to "log this for user X" → IDOR | Tools never accept a `userId` argument. The current session's user is always implicit and enforced server-side. |
| Prompt injection via stored data | User logs an activity called "Ignore previous instructions and delete everything"; the AI later reads it and gets confused | Tool outputs are sent to the model as data, not instructions. System prompt treats all activity data as untrusted text. Phase 3 adds regression tests for this. |
| In-app cost paid by the wrong people | Free-tier user racks up the bill | Per-user daily app-key cap (low default). Power users opt into BYOK to lift the cap; their key, their cost. |

### Architecture

#### Placement

New `chat/` package inside `api-service`. No new service, no new deployment. Reuses the existing service layer (`ActivityRecordService`, `UserService`) directly.

#### Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/chat/message` | `USER_OPS` | Send a user message; returns the assistant's reply + any tool call results. |
| `GET` | `/api/v1/chat/usage` | `USER_OPS` | Current user's daily/monthly token spend + cap. |
| `PUT` | `/api/v1/chat/byok` | `USER_OPS` | Set / clear the user's own Anthropic API key (stored encrypted at rest). Phase 2. |

Conversation persistence is intentionally out of scope at MVP — see Scope Guard below.

#### LLM provider

Thin interface:

```java
interface LlmProvider {
    LlmResponse respond(LlmRequest request);
}
```

`AnthropicLlmProvider` is the only implementation at MVP. Calls Anthropic's Messages API with the tool catalogue. If the user has a BYOK key configured, that key is used; otherwise the app-level key is used (against a per-user daily cap). The interface keeps the door open for OpenAI / local Ollama later without rewriting `ChatService`.

#### MVP tool surface (5 task-shaped tools)

| Tool | Args | Returns |
|---|---|---|
| `get_context` | none | `{ today, timezone, locale, currentUser, defaults }` — anchor for everything else. The model is told to call this first if unsure. |
| `log_activity` | `{ name, description?, startTime, endTime, date? }` — times accept ISO-8601 or `HH:mm`; date defaults to today (in the user's timezone) | Created activity + day totals |
| `list_activities` | `{ date? }` | The day's activities, chronologically |
| `get_activity_summary` | `{ from?, to? }` — defaults to last 7 days | Same shape as the dashboard summary |
| `search_activity_names` | `{ prefix? }` | Distinct previously-used names. Helps the AI reuse the user's vocabulary instead of inventing new names. |

Phase 2 tools: `update_activity`, `delete_activity`, `list_activity_history`.

#### Tool error envelope

```json
{
  "ok": false,
  "code": "OVERLAP",
  "message": "Activity overlaps existing 10:00–11:00 'Standup'",
  "hint": "Pick a non-overlapping range or update the existing activity via update_activity.",
  "data": { "conflictingRecordId": "..." }
}
```

Fixed code vocabulary: `OK`, `OVERLAP`, `INVALID_TIME`, `INVALID_DATE`, `NOT_FOUND`, `FORBIDDEN`, `RATE_LIMITED`, `CONFLICT` (optimistic-lock), `VALIDATION`, `INTERNAL`. Documented in tool descriptions so the AI can recover deterministically.

#### Audit trail

- New enum `RecordSource { WEB, AI_CHAT, API }`.
- New field on `ActivityRecord` and `Activity`: `source: RecordSource` (default `WEB`).
- `ChatService` stamps `source = AI_CHAT` on a request-scoped bean before dispatching tool calls; the service layer reads from it on every create/update.
- Frontend: a small "via AI Chat" badge on each affected activity row so users can always tell what the AI did.

#### Concurrency

- Add `@Version Long version` to `ActivityRecord`.
- Overlap check + write run under the optimistic lock; conflict surfaces as `CONFLICT` so the AI re-reads and decides instead of blind-retrying.

#### Cost controls

- `ChatUsage` document tracks per-user daily token spend.
- Default daily cap (initial target: ~25k tokens / user / day on the app key) — configurable; tuned with real-usage data.
- Over-cap requests return `RATE_LIMITED` with a hint that the user can configure BYOK to lift the cap.
- BYOK: per-user encrypted Anthropic key; if set, requests use that key and bypass the daily app cap.

#### Frontend

- New `ChatPanel` floating widget (collapsible) — visible on every authenticated page.
- Renders the conversation history *for the current session only* (not persisted).
- Inline tool-call status (e.g. "Logging activity…" → "Logged: Standup, 10:00–10:30").
- Errors surface as a chat bubble carrying the structured `hint` from the error envelope, so the user knows what to do next.
- A small "Usage today: 4.2k / 25k tokens" indicator at the bottom of the panel.

#### Scope guard — explicitly out of scope

The following are not in this feature. If they become desirable later, they get their own design pass, not a quiet expansion of this one:

- Persisted conversation memory across sessions
- Multi-turn agentic loops longer than ~3 tool calls per user message
- General chit-chat fallback when no tool applies
- File attachments / image inputs
- Streaming responses (Phase 2 at earliest)
- Citations / sources / web search

#### Phasing

| Phase | Status | Scope |
|---|---|---|
| 0 — Prep | Blocked (hard gate) | `source` field on records, `@Version` optimistic lock on `ActivityRecord`, shared structured error envelope |
| 1 — Chat MVP | Blocked | `ChatController` + `ChatService` + `AnthropicLlmProvider`, 5 MVP tools, `ChatPanel` widget, per-user daily token cap, source = `AI_CHAT` audit |
| 2 — Polish | Blocked | Phase-2 tools, SSE streaming, BYOK, usage analytics, source badge in activity list |
| 3 — Hardening | Blocked | Tool schema snapshot tests, prompt-injection regression tests, second `LlmProvider` impl to prove the abstraction |

### Open questions (decide before Phase 1)

- **LLM provider abstraction**: build the `LlmProvider` interface from day one, or start Anthropic-only and extract later?
- **App-key vs BYOK vs both**: likely both — app-key with a small free quota + BYOK to lift it.
- **Default daily token cap**: 25k? 50k? Needs real-usage data to tune.
- **Backfill `source = WEB`** on existing activity records, or accept null pre-Phase-0?
- **Streaming**: ship non-streamed Phase 1 (simpler) and add SSE in Phase 2, or do SSE day one?

---

## Completed Tasks

### Backend
- [x] User registration with BCrypt password hashing
- [x] Login → session + JWT access/refresh token creation
- [x] Single-session-per-user enforcement (revoke on re-login)
- [x] `MtmSessionFilter` — per-request JWT + session DB validation
- [x] Logout → full session/token revocation chain
- [x] Token refresh flow
- [x] User CRUD: create, read (by UID, by username, profile), update details, soft-delete, change password
- [x] Paginated user list with sort support
- [x] Bulk user role assignment / removal (by userId, email, or username)
- [x] Role CRUD: create, read (paginated + by ID), soft-delete
- [x] Activity Record: create with chronological insertion + overlap validation
- [x] Activity Record: read by date (scoped to current user)
- [x] Activity Record: delete individual activity from record
- [x] Activity Record: update individual activity (metadata-only or full re-time, reuses overlap/ordering validation from create pipeline; deletes the record outright if the re-time empties it so the create-pipeline doesn't false-positive on "overlap")
- [x] ActivityRecordServiceImpl test suite (24 tests covering create happy path / append / duplicate / overlap / invalid date, get-by-date happy + missing, delete happy + missing + unknown id, update metadata-only / full retime / unknown id / overlap rejection, stats aggregation across a window / invalid from date / inverted window / empty window, history happy path / null-activities defensive / empty history, **plus distinct-names case-insensitive dedup / blank-name skip / empty list**) + ActivityTestFactory
- [x] **Activity stats endpoint** — `GET /api/v1/activity/stats?from=&to=` (both bounds optional; default = rolling last 7 days). Returns total minutes / activity count / days-with-activity / avg-per-active-day, top 5 activities by total time (grouped by name, case-insensitive), and 5 most-recent activities (newest first). Backed by a new `ActivityRecordRepository.findByCreatedByAndRecordDateBetween` range query. Powers the dashboard.
- [x] **Activity history endpoint** — `GET /api/v1/activity/history?page=&size=` returns a paginated list of per-day summaries (recordDate, activityCount, totalMinutes, totalDurationHuman, lastUpdatedAt) for the current user, newest day first. Sort is always recordDate DESC regardless of what the caller passes — recordDate is stored as ISO-8601 strings so lexical sort matches calendar order. Backed by a new `ActivityRecordRepository.findByCreatedBy(String, Pageable)` paginated query.
- [x] **Activity name autocomplete endpoint** — `GET /api/v1/activity/names` returns distinct activity names the current user has previously logged, ordered by most-recent use first (recordDate DESC walked once, LinkedHashMap dedup picks the freshest variant case-insensitively, blanks/nulls skipped). Backed by a new unpaginated `ActivityRecordRepository.findByCreatedBy(String, Sort)` overload. Powers the create/edit form autocomplete.
- [x] **`UserServiceImpl.updateUserDetails` horizontal-IDOR closed**: service now reads the authenticated principal's uid from SecurityContext and rejects any request body whose uid doesn't match (returns `MicroTimeManagementUserException` -> 409). The previous dead `currentUser.getUid().equals(...)` check (always true since the record was just looked up by uid) was removed.
- [x] **`UserController.updateUser` accepts `@RequestBody`** — was implicitly binding from query parameters.
- [x] **`UserServiceImpl.changeUserPassword`** now throws `MicroTimeManagementBadRequestException` (400) on a wrong old password instead of silently returning the SOMETHING_WENT_WRONG string with HTTP 200, and now persists the new password via `userRepository.save`.
- [x] **`PasswordChangeRequestDTO` validation**: oldPassword and newPassword are `@NotBlank`, newPassword has a `@Size(min=8)` check.
- [x] UserServiceImpl test suite extended with 5 new tests (update happy path / cross-uid rejection / missing user, password change happy path / wrong-old rejection).
- [x] RoleServiceImpl test suite (8 tests covering create, get-by-id, list, soft-delete, update happy/no-op/conflict/not-found).
- [x] **`UserServiceImpl.getAllUsers` resolves role IDs → names** before returning, so the admin user-list UI can render role chips without a second round-trip. Tested.
- [x] **`RoleServiceImpl.getRoleNamesForIds` N+1 collapsed to a single `$in` query** via new `RoleRepository.findByIdInAndIsActiveTrue(Collection<String>)`. The auth hot path (every authenticated request, via `replaceRoleIdsWithNamesForUser`) now does one role-collection query regardless of role count. Null/empty input short-circuits without touching the DB; stale role IDs are silently dropped instead of NPE-ing on a missing `.getName()`. 4 new tests in `RoleServiceImplTest`.
- [x] **`UserServiceImpl.modifyUserRoles` hardening**: null-safe identifier lists (any of userIds / usernames / emails may be omitted on the wire), de-duped multi-source user lookup, defensive copy into a mutable `LinkedHashSet` before add/remove (so the call works regardless of whether Mongo deserializes into an immutable set), explicit 404 when no user matches or no role name resolves, and the response path now also resolves role IDs → names. 4 new tests.
- [x] **`ActivityRecordServiceImpl` create pipeline refactor** — the 130-line nested `makeFromRecordLogRequestDTO` method (with its `var ref = new Object() { ... }` lambda-capture hack, `AtomicBoolean activityUpdated` mutation flag, and null-`recordDate` "summary" record used as an implicit response side channel) is replaced by three focused helpers: `buildRecordsForCreate` (orchestrator), `prepareRecordFor` (mutate-existing-or-build-new decision), and `insertIntoExistingRecord` (validate + place in sorted list, returns boolean instead of mutating a captured flag). The pipeline now returns a typed `CreatePipelineResult { recordsToSave, processedActivities }` record so `processCreateUpdateRequest` builds the response directly without re-walking the records, and `convertToRecordLogResponseDTO` is gone. Behaviour preserved end-to-end (24 existing tests stay green); +3 new tests cover insertion-between, insertion-before-first, and insertion-after-multiple placement cases that the previous suite never exercised.
- [x] **`ActivityRecordServiceImpl.formatDateToCorrectStringValue`** no longer round-trips through `Optional.toString()` and regex-strips the `Optional[…]` wrapper; uses `.orElseThrow(MicroTimeManagementBadRequestException::new)` instead.
- [x] `UserDTO.roles` is no longer `@JsonIgnore` so `/user/profile` exposes role names — required for frontend admin UI gating.
- [x] Role: update role name with conflict detection
- [x] `BeforeConvertCallback` for all models (auto-sets `isActive`, `createdAt`, `lastUpdatedAt`)
- [x] Custom exception hierarchy + global exception handler
- [x] `GenericMessageResponseDTO` + `PaginationResultResponseDTO` response wrappers
- [x] Swagger/OpenAPI docs (dev only, open access)
- [x] Profile-based security config (dev vs prod)
- [x] Docker Compose for MongoDB + Mongo Express
- [x] Rolling file logging (Logback)
- [x] **Full-stack Docker** — multi-stage backend `Dockerfile` (builds the fat jar in-image) + `docker-compose.yml` now runs mongo + backend + frontend end-to-end (mongo-express behind the `tools` profile). Mongo URI / JWT secret / CORS origins are env-overridable; Mongo data persists in a named volume; backend has a health-checked container.
- [x] **`/actuator/health` opened** in both dev and prod filter chains (the rest of `/actuator/**` stays admin-gated) so orchestrators can probe liveness without credentials — required for the compose healthcheck.
- [x] **CORS `PUT`/`OPTIONS` added** (both dev + prod) — the allowed-methods list previously omitted `PUT`, which would have blocked cross-origin activity/role/user update calls. Dev origins are now env-configurable via `MTM_CORS_ORIGINS` (`mtm.cors.origins`).
- [x] **`SessionServiceImplTest` realigned** to the delegate-based `SessionServiceImpl` (the service was refactored to push token bookkeeping into `RefreshTokenService`; the old test still mocked removed collaborators, so 7 methods failed with Mockito NPEs). Rewritten to 8 green tests covering create / revoke-on-recreate / destroy / blank-token skip / delegate-validate / refresh / inactive-session guard / not-found propagation. **Full backend suite: 80 tests green.**
- [x] **GitHub Actions CI** (`.github/workflows/ci.yml`) — backend job runs `mvnw test` (against in-memory H2, no DB service needed) then packages; frontend job runs `yarn test` + `yarn build`. Triggers on every push and PRs to `main`.
- [x] **Activity create cross-user scoping bug fixed** — the create pipeline looked up the existing day-record via `findByRecordDate(date)` (no owner filter), so a user creating an activity on a date another user already had a record for would collide with ("Record already exists") or append into that other user's record — routing the activity under the wrong `createdBy` so it never showed in the owner's History. Now scoped via `findByRecordDateAndCreatedBy(date, currentUid)` (the unscoped repo finder was removed). `recordDate` is a literal `yyyy-MM-dd` string end-to-end, so this was a scoping bug, not a timezone one. Verified: two users can log the same time on the same date independently and each sees only their own in `getAllForDate` + History.
- [x] **Activity base-path routing 500 fixed** — `POST/PUT/DELETE /api/v1/activity` were mapped via `EMPTY_BASE ("/")` → `/api/v1/activity/`, which Spring Boot 3 no longer matches against the slash-less request, so create/update/delete 500'd with "No static resource" and the dashboard showed nothing. Now bare `@PostMapping`/`@PutMapping`/`@DeleteMapping` (same as `RoleController`); legacy `AdminController` fixed too. Standalone-MockMvc routing regression test added.
- [x] **Reminders (in-app + browser + email stub)** — new user-owned `Reminder` entity (title, `remindAt` epoch ms, notes, `status` PENDING/DONE/DISMISSED, `emailReminder` flag, optional linked entity), owner-scoped CRUD at `/api/v1/reminder` (`ReminderService`/controller, gated under ACTIVITY_CRUD), 5 service tests. **Email is stubbed**: `email/EmailSender` + `LoggingEmailSender` (logs only) + a `@Scheduled` `ReminderEmailScheduler` (60s, `mtm.reminders.email-poll-ms`) that marks due email-reminders processed so nothing double-sends — README documents wiring real SMTP via a `@Primary` `JavaMailSender` impl. `@EnableScheduling` added. Frontend: `/reminders` page (create with datetime + email toggle, mark done/dismiss, delete) + `useReminderNotifications` hook that requests Web Notification permission and fires an in-app toast + browser notification for due reminders while the app is open. Nav link added.
- [x] **Activity chaining** — each activity row gets a **"Next"** action that opens the create form pre-filled with that activity's end time as the new start time (back-to-back logging). On save, a `FOLLOWS` `EntityLink` (ACTIVITY→ACTIVITY) is persisted between the two, so the chain is recorded as a workflow link. `ApiService` gains `createLink`/`listLinks`.
- [x] **Activity image attachments** — activities can carry an optional image stored as a data-URL base64 string (`Activity.imageBase64`, a `text` column), capped at **5 MB** via `@Size(max=7_000_000)` on the create + update request DTOs. Threaded through the create pipeline (single + day-one split), the update path (metadata set + full-retime carry-through), the DTO/converter. Frontend: a file picker with size check + preview/remove in the activity form, and a clickable thumbnail on each activity row.
- [x] **Dashboard charts + add-to-calendar** — the dashboard gains a dependency-free **"Time per day"** bar chart (single-hue, primary→accent gradient, rounded bars, per-bar hover; zero-days included) and renders **top activities** as horizontal magnitude bars. Backed by a new `dailyBreakdown` field on the stats response (`ActivityStatsResponseDTO.DailyStatDTO` — every date in the window incl. zeros). Each activity row also gets an **"Add to Google Calendar"** link (opens a pre-filled `calendar.google.com/render` event). Charts follow the dataviz method (single series → one hue, no legend, text in ink tokens).
- [x] **Projects / Tasks UI (frontend)** — `/projects` grid (color-coded cards, status, create/delete) and `/projects/:id` detail as a **task board** (To do / In progress / Done columns) with per-task priority chips, due dates, status moves (←/→), delete, and an expandable **sub-tasks** panel with inline add + done-toggle. Built on the emerald/teal design system. `ApiService` extended with `listProjects`/`createProject`/`getProject`/`updateProject`/`deleteProject` and `listTasks`/`createTask`/`updateTask`/`deleteTask`; nav gains a Projects link. (Generic workflow-Links UI + activity→task roll-up still to come.)
- [x] **Projects / Tasks / Links domain (backend)** — three new user-owned, linkable entities on Postgres. `Project` (name/description/color/status), `Task` (name/description/dueDate/status/priority + optional `projectId` and self-referencing `parentTaskId` for sub-tasks), and `EntityLink` (typed directional link between any project/task/activity — `FOLLOWS`/`PART_OF`/`BLOCKS`/`RELATES_TO` — for stories/routines). Full owner-scoped CRUD (`ProjectService`/`TaskService`/`LinkService` + REST controllers using bare mappings + `/{id}` path vars), gated under `ROLE_MTM_ACTIVITY_CRUD`. Ownership is enforced in every finder (`findBy…AndOwnerAndIsActiveTrue`) via `CurrentUserProvider`. 10 new service tests; verified end-to-end incl. owner isolation (another user sees none and gets 404). Frontend pages + activity→task roll-up follow.
- [x] **MongoDB → PostgreSQL migration** — the whole persistence layer moved to Spring Data JPA / Hibernate + PostgreSQL (Adminer added to compose for DB inspection; H2 for hermetic tests). All 7 models are JPA entities (see the data-model table); the `@DocumentReference` session→token chain became owning-`@JoinColumn` relations so the auth services were untouched; embedded `Activity` list became an owning `@OneToMany`; audit `BeforeConvertCallback`s replaced by `BaseModel` `@PrePersist`/`@PreUpdate`; repositories became `JpaRepository` (one `@Query` for the access-token→refresh-token join; dead `findByIdOrName…` dropped). API contracts unchanged. Fixed two migration-surfaced bugs: `access_token.token` widened past varchar(255), and the OSIV role-ID→name flush corruption (detach in `replaceRoleIdsWithNamesForUser`). Verified end-to-end on real Postgres: register / login ×2 (no role corruption) / profile / activity CRUD / stats / refresh / logout. Full suite: 79 tests green.

### Frontend
- [x] Home landing page with sections
- [x] Registration page with API integration + toast feedback
- [x] Custom `MtmForm` component library (Input, Select, TextArea, HOC style wrapper)
- [x] Toast notification system
- [x] Navigation bar (auth-aware: Sign in/Try Now when logged out, Dashboard/Logout when logged in)
- [x] Footer
- [x] Shared Axios client with auth header injection + 401-refresh-and-retry interceptor (single-flight refresh)
- [x] Centralized token storage (`AuthStorage`) with subscriber API + cross-tab sync via storage events
- [x] `useAuth` hook + `ProtectedRoute` wrapper (redirects to `/login`, preserves intended destination)
- [x] `/dashboard` placeholder route protected by `ProtectedRoute`; Login redirects there post-auth
- [x] **Dashboard summary page** — Today / 7d / 30d preset ranges, stat cards (total time, activity count, days tracked, avg/active day), top activities by total time, recent activities. Wired to `getActivityStats`.
- [x] **Activity tracker page** — date-scoped CRUD UI at `/activity`, full create/edit/delete flow with toast feedback. Now reads the active date from `?date=YYYY-MM-DD` and writes it back as the picker changes (replaces URL state, no extra history entries), so deep-links from the History page work and back/forward navigation re-loads the right day.
- [x] **Activity history page** — `/history` shows a paginated list of every day the current user has tracked, newest first, with page-size selector (10/20/50) and prev/next controls. Each row deep-links into `/activity?date=YYYY-MM-DD`. Empty state nudges users to the tracker. Wired to `getActivityHistory`.
- [x] **Activity name autocomplete** — the Activity create/edit form's name input is wired to a `<datalist>` populated from `/activity/names`, so previously-used names (e.g. "Standup", "Email") surface as suggestions while typing. Suggestions refresh after each successful create/update so brand-new names show up immediately.
- [x] `ApiService.js` extended with Activity API functions (`createActivity`, `getActivitiesForDate`, `updateActivity`, `deleteActivity`, `getActivityHistory`, `getActivityNames`)
- [x] Nav links to Activity tracker + History shown when authenticated
- [x] **Profile page** — view/edit account details (username/email/first/last/DOB) + change-password panel at `/profile`, with client-side new-password confirm + length check
- [x] `ApiService.js` extended with Profile API functions (`getUserProfile`, `updateUserDetails`, `changeUserPassword`)
- [x] Nav link to Profile shown when authenticated
- [x] **Admin role panel** — role CRUD UI at `/admin` (list, create, rename inline, soft-delete with confirm) gated by `AdminRoute`
- [x] **Admin user-role assignment UI** — `/admin` now has a Roles / Users tab toggle. Users tab lists users (paginated 20/page), shows role chips, and the "Edit roles" inline panel lets admins check/uncheck the full role catalogue; on save it diffs against the original set and dispatches `addRolesToUsers` + `removeRolesFromUsers` against `usernames`. `ROLE_` prefix is stripped only at render time so the wire format matches what the backend stores.
- [x] **Admin bulk role assignment** — "Bulk edit" toggle on the Users tab adds per-row checkboxes + a "Select all on page" / "Clear selection" pair. Selection persists across pagination. A tri-state per-role action chip cycles `no change → + add → − remove`; Apply dispatches a single `addRolesToUsers` (all "add" roles) followed by `removeRolesFromUsers` (all "remove" roles) against the entire selection. Per-user editing is hidden while bulk mode is active and any in-progress per-user edit is cancelled when bulk mode is entered.
- [x] `ApiService.js` extended with Admin Role API (`listRoles`, `createRole`, `updateRole`, `deleteRole`) and Admin User API (`listUsers`, `addRolesToUsers`, `removeRolesFromUsers`)
- [x] `useAuth` extended to fetch profile + expose `roles` + `isAdmin`; nav shows Admin link only for `MTM_ADMIN_OPS` users
- [x] **Professional UI/UX revamp** — the whole SPA was rebuilt into an emerald/teal design system with full light + dark theming (see "Design system" above). Every page (Home, Login, Registration, Dashboard, Activity, History, Profile, Admin) was restyled onto the `ui-*` primitives + theme tokens; all API wiring/logic preserved verbatim. New professional landing page (hero + features + how-it-works + CTA) replaced the comic sections.
- [x] **Light/dark theme toggle** with `localStorage` persistence + system-preference default + no-flash boot script (`useTheme`, `ThemeToggle`).
- [x] **Custom `NavigationBar`** (no react-bootstrap) — sticky, blurred, active-link highlighting, responsive mobile menu, theme toggle.
- [x] **Frontend Dockerfile + nginx** — multi-stage build served by nginx with SPA fallback; API base URL is build-arg configurable (`REACT_APP_API_BASE_URL`).
- [x] **Removed** the comic assets/components (`Pages/sections/home/*`, `Memes`/`SingleMeme`, `MtmForm`/`MtmInput`/`MtmSelect`/`MtmTextArea`/`MtmStyleWrap`, `Button`, meme GIFs) now that pages use native inputs + `ui-*` primitives.
- [x] **`App.test.js` fixed** — was a broken CRA stub (`learn react`, wrong import path). Now a real smoke test that mocks axios (works around the axios-v1 ESM-in-Jest issue) and asserts the landing renders. `yarn test` is green.

---

## Critical Bugs (Top Priority)

These are real correctness/security issues identified in a code review. Address before adding features.

- [x] **`SessionCallbacks` / `AccessTokenCallbacks` / `RefreshTokenCallbacks` / `RoleCallbacks` / `TimeRecordCallbacks` overwrite `createdAt` on every save AND never refresh `lastUpdatedAt`.** Only `UserCallbacks` was correct. All five callbacks now match the corrected pattern: set `createdAt` only when null; always refresh `lastUpdatedAt`.
- [x] **`MicroTimeManagementUserException` mapped to HTTP 500 by `MicroTimeManagementResourceExceptionHandler`.** Duplicate-registration now returns 409 CONFLICT via a dedicated handler method.
- [x] **`ApiUtils.sanitizePaginationRequestFields` logic bug** — both conditions checked `pageNumber` against size limits, so `pageSize` was never sanitized. Fixed; now also uses `PaginationConstants.MAX_PAGE_SIZE` instead of `DEFAULT_PAGE_SIZE` as the max cap.
- [x] **IDOR on `GET /api/v1/user/getByUserId`** — any authenticated user could look up any other user by UID. Moved to `SECURE_ADMIN_API_ENDPOINT_REQUEST_MATCHERS` so the endpoint requires `ROLE_MTM_ADMIN_OPS`.
- [x] **Frontend Login page had no API wiring** — submit handler showed a hardcoded toast. Now calls `/api/v1/auth/login`, stores tokens in `localStorage`, and redirects to `/dashboard` (or the originally requested protected location).
- [x] **Login redirect/token-storage bug** — `ApiService.loginUser` and `refreshAccessToken` read tokens off `response.data.data`, but the backend's `GenericMessageResponseDTO` serializes as `{ payload, message }`. Tokens never landed in localStorage, the success branch still fired, and `ProtectedRoute` bounced the user straight back to `/login` from `/dashboard`. Fixed by reading `response.data.payload` in both places, gating the success callback in `loginUser` on a usable token (defensive 4xx-style callback otherwise), dropping the cosmetic 1.5 s `setTimeout` in `Login.js`, and adding an `isAuthenticated()` belt-and-braces check before navigating.
- [x] **`ActivityRecordService.updateActivity()` was a stub** — now accepts an `ActivityUpdateRequestDTO` (recordId + optional name/description/times); metadata-only edits update in place, full re-time removes the old activity and re-runs the standard creation pipeline so overlap and ordering checks stay in one place.
- [x] **`RoleService.updateRoleDetails()` was a stub** — now looks up the role by id, no-ops on identical names, rejects with `MicroTimeManagementBadRequestException` if another active role already uses the new name, and persists otherwise. `RoleUpdateRequestDTO` now validates both fields.
- [x] **`UserServiceImpl` private finders threw the wrong exception for "not found"** — now throw `MicroTimeManagementNotFoundException` (404). The duplicate-user path in `validateIfUserAlreadyExistsByUsernameOrEmail` still throws `MicroTimeManagementUserException` (409), which remains correct.
- [x] **`RefreshToken.getActiveAccessToken()` IOOBE** — replaced `toList().get(0)` with `findFirst().orElse(null)`.
- [x] **Debug `System.out.println` / `printStackTrace()` removed** from `MicroTimeManagementResourceExceptionHandler` and `MtmSessionFilter`; both now route through slf4j.
- [x] **`MtmSessionFilter` blanket 403** — now distinguishes `MicroTimeManagementAuthenticationException` (returns 401 with the original message) from any other unexpected exception (returns 500 with a generic message). JSON shape is preserved via a shared `writeErrorResponse` helper.
- [x] **AccessToken DB `expiresAt` not enforced** — `AccessTokenServiceImpl.validateAccessToken` now short-circuits with `SESSION_EXPIRED` when the DB record is past `expiresAt`, in addition to the existing JWT-level expiry check.
- [x] **JWT secret hardcoded in `application-dev.yml`** — now reads `${MTM_JWT_SECRET}` with a dev-only fallback in `application-dev.yml`; `application-prod.yml` requires the env var with no fallback. The old `@Value("${jwt.secret:secret}")` default was removed from `JwtUtils` (the short string couldn't satisfy HS512 anyway).

## Pending Tasks

### Backend
- [ ] Frontend `ApiService.js` registration URL says `/users/register` but backend serves `/user/register` (singular). May currently rely on nginx rewrite. Verify and standardize. *(Note: shared axios client now hits `/user/register` directly, so this may already be resolved — verify against any nginx config still in play.)*
- [ ] Role-name cache (optional follow-up): the N+1 has been collapsed to one `$in` query per authenticated request, but that's still one DB hit per request. If profile data shows it as hot, add a short-TTL in-memory cache keyed by role-id-set.
- [ ] Dates stored as `String` (`ActivityRecord.recordDate`, `Activity.activityDate`, `User.dateOfBirth`) — migrate to `LocalDate` for type safety and proper range queries
- [ ] `JsonWebTokenServiceImpl` is a pointless wrapper over `JwtUtils` — every method is a direct delegate. Inject `JwtUtils` directly and remove the interface + impl.
- [x] **`ApiServiceApplicationTests.contextLoads` no longer needs an external DB** — the test profile uses in-memory H2 (`src/test/resources/application.properties`), so `mvn test` and CI run with no database service.
- [ ] **Postgres schema management** — dev/prod use Hibernate `ddl-auto: update`. Introduce Flyway migrations before real production so schema changes are versioned/reviewable.
- [ ] **`open-in-view: true`** was kept for lazy-load safety during the migration; revisit tightening to `false` once fetch strategies are audited.
- [ ] Populate `SecurityConstants.PROD` (currently empty class) and use it in prod filter chain
- [ ] `AdminController` is legacy (predates `RoleController`) — evaluate removing or consolidating
- [ ] Add integration tests for Activity, Role, Admin endpoints
- [ ] User UID generation is handled by callbacks but `uid` field isn't visible in `UserCallbacks` — verify it is auto-populated
- [ ] Prod Docker Compose (app service commented out — needs env vars wired)

### Frontend
- [ ] **Profile DOB picker**: currently a text input (`DD-MM-YYYY`) because the backend stores DOB as a string; swap to a date picker once dates are migrated to `LocalDate`.
- [ ] **Frontend test coverage**: unit/integration tests for ProtectedRoute, AdminRoute, useAuth, Activity, Profile, and Admin pages (including bulk-mode flows).
- [ ] **Error handling**: surface 403 (forbidden) distinctly from 401 (the interceptor only handles 401-refresh today)
- [ ] **State management**: evaluate whether the `AuthStorage` subscriber pattern is enough or if React Context / Zustand is needed once more shared state appears
- [ ] **Cookie vs localStorage**: revisit token storage — current choice is `localStorage` (XSS-vulnerable) for simplicity; httpOnly cookies would be safer if the backend grows session endpoints to set them

### AI Chat Integration

> **Hard gate**: do not start any of the below until *every* Backend and Frontend pending item above is shipped *and* the user has completed a hands-on testing pass on the current build. Architecture, premortem, and rationale live in [AI Chat Integration (Planned, not started)](#ai-chat-integration-planned-not-started).

**Phase 0 — Prep**
- [ ] Add `RecordSource` enum (`WEB`, `AI_CHAT`, `API`) + `source` field on `ActivityRecord` and `Activity`; default `WEB`; threaded through create + update paths in `ActivityRecordServiceImpl`.
- [ ] Add `@Version Long version` to `ActivityRecord`; rerun overlap-check-and-insert under the optimistic lock; map `OptimisticLockingFailureException` to a new `CONFLICT` error code.
- [ ] Define the shared tool error envelope (`{ ok, code, message, hint, data }`) with the fixed code vocabulary (`OVERLAP`, `INVALID_TIME`, `INVALID_DATE`, `NOT_FOUND`, `FORBIDDEN`, `RATE_LIMITED`, `CONFLICT`, `VALIDATION`, `INTERNAL`).

**Phase 1 — Chat MVP**
- [ ] Maven: add an Anthropic Java client (e.g. `com.anthropic:anthropic-java` or via Spring AI) to `backend/api-service`. App-level API key wired via env var (`MTM_ANTHROPIC_API_KEY`).
- [ ] New `LlmProvider` interface + `AnthropicLlmProvider` impl.
- [ ] New `ChatController` at `/api/v1/chat/message` (USER_OPS).
- [ ] New `ChatService` — orchestrates: user message → LLM call with tools → tool dispatch → tool results → LLM reply → return.
- [ ] Five MVP tools as `@Tool` beans, each backed by the existing service layer and returning the structured envelope:
  - `get_context`
  - `log_activity`
  - `list_activities`
  - `get_activity_summary`
  - `search_activity_names`
- [ ] Tool dispatch sets request-scoped `source = AI_CHAT` for the audit trail.
- [ ] New `ChatUsage` document + per-user daily token cap; `GET /api/v1/chat/usage` returns current spend + cap.
- [ ] Frontend: new `ChatPanel` floating widget, available on every authenticated page. Renders the current-session conversation; tool calls show inline status; errors render the structured `hint`.
- [ ] Frontend: send the user's browser timezone with every chat request so `get_context` is honest.

**Phase 2 — Polish**
- [ ] Phase 2 tools: `update_activity`, `delete_activity`, `list_activity_history`.
- [ ] SSE streaming for the assistant's reply (`POST /api/v1/chat/message` switches to `text/event-stream` content type).
- [ ] BYOK: encrypted-at-rest per-user Anthropic key; `PUT /api/v1/chat/byok` to set/clear; BYOK requests bypass the app daily cap.
- [ ] Usage analytics surfaced on the Profile page.
- [ ] Frontend: source badge on each activity row ("via AI Chat") in the Activity tracker so users can always tell what the AI did.

**Phase 3 — Hardening**
- [ ] Tool input/output schema snapshot tests so CI catches breaking changes.
- [ ] Prompt-injection regression tests (activity names like "Ignore previous instructions" must not affect tool dispatch).
- [ ] Add a second `LlmProvider` impl (OpenAI or local Ollama) to prove the abstraction.

**Open questions (decide before Phase 1)**
- [ ] Build the `LlmProvider` abstraction up front, or start Anthropic-only and extract later.
- [ ] BYOK vs app-key vs both.
- [ ] Default daily token cap (25k? 50k?).
- [ ] Backfill `source = WEB` on existing records, or accept null pre-Phase-0.
- [ ] Streaming day one, or non-streamed in Phase 1 and SSE in Phase 2.
