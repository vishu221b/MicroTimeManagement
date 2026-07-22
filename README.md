<div align="center">

# Micro Time Management (MTM)

**A self-hosted, role-aware time tracker for the people who keep meaning to start one.**

[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/status-active%20development-blue)](#roadmap)

</div>

---

## Table of contents

- [What it is](#what-it-is)
- [Project journey](#project-journey)
- [Features](#features)
- [Architecture at a glance](#architecture-at-a-glance)
- [Tech stack](#tech-stack)
- [Project layout](#project-layout)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Clone](#1-clone)
  - [2. Run MongoDB](#2-run-mongodb)
  - [3. Start the backend](#3-start-the-backend)
  - [4. Start the frontend](#4-start-the-frontend)
- [Configuration](#configuration)
- [API surface](#api-surface)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Authors](#authors)

---

## What it is

Micro Time Management is a full-stack web app for tracking how you actually spend your day. You log in, pick a date, and add **activities** — time blocks with a name, description, and start/end times. The backend rejects overlapping blocks, keeps them in chronological order, and scopes everything to the signed-in user. Admins can manage roles from a separate panel.

The project started as a side-project during a Master's degree and is now being finished as a portfolio piece. It is deliberately built around **a real auth stack, a real role model, and real test coverage** rather than a toy backend, so it doubles as a reference for anyone wiring up Spring Security + JWT sessions against MongoDB.

## Project journey

The bones of this app — the data model, the auth flow, RBAC, the activity logic, the React shell, and most of the early controllers and services — went in back in late 2022 during my Master's. After that the repo sat quiet for a while.

I picked it back up in early 2026 and spent a couple of months getting the foundation back in shape before adding anything new. That stretch was all solo: refactoring the auth flow, isolating the service boundaries, fixing RBAC end-to-end, building out role CRUD with pagination, getting a working user profile + update endpoint in, wiring up the login and registration screens, and giving the test suite some real display names plus a few more supporting tests.

Once the base felt solid again, I brought [Claude](https://www.anthropic.com/claude) in as a co-author to finish the rest. Pair-programming with an AI is becoming a normal part of how software gets built, and I wanted to grow into that workflow rather than work around it. I'm still the one driving the product and reviewing every change; Claude just lets me move at a pace I actually enjoy. The auth hardening, the activity tracker UI, the profile page, the admin panel, and most of the service-layer test coverage came out of that pairing.

The result is an app that's mine in design and direction, built with a workflow I'd happily reach for on the next thing too.

## Features

### For everyone
- 🔐 **Registration & login** — BCrypt-hashed passwords, JWT access (5h) + opaque refresh (7d) tokens, automatic refresh on `401`.
- 📅 **Daily activity tracker** — create, edit, delete activities per date. Overlapping time ranges and duplicates are rejected at the service layer; insertions stay chronologically sorted.
- 👤 **Profile management** — update name, email, username, date of birth, and change password with current-password verification.
- 🚪 **Logout & session revocation** — server-side session is invalidated on logout (and on re-login: single active session per user).

### For admins (`ROLE_MTM_ADMIN_OPS`)
- 🛡️ **Role management** — create, rename (with name-conflict detection), and soft-delete roles from a dedicated admin panel.
- 👥 **Bulk user/role operations** — backend endpoints for adding/removing roles to users in bulk (UI coming soon).
- 🔎 **Paginated user listing** — sortable, scopable, locked behind `ROLE_MTM_ADMIN_OPS`.

### Under the hood
- 🧪 **Service-layer test coverage** for activities, users, roles, auth, and pagination utilities.
- 🪵 **Rolling-file structured logging** via Logback.
- 📓 **Swagger UI** auto-generated in the dev profile.
- 🔁 **Single-flight 401 refresh** — concurrent expired requests share one refresh round-trip in the Axios client.
- 🗂 **Cross-tab auth sync** in the frontend via `storage` events.

## Architecture at a glance

```
                 ┌──────────────────────────────────────┐
                 │             React SPA                │
                 │  Pages: Home · Login · Register      │
                 │         Dashboard · Activity ·       │
                 │         Profile · Admin              │
                 │  ProtectedRoute / AdminRoute         │
                 │  Axios apiClient (+401 refresh)      │
                 └──────────────────┬───────────────────┘
                                    │ HTTPS / JSON
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 3.5 API                      │
│                                                             │
│  MtmSessionFilter ──► AuthN/AuthZ ──► Controllers ──► Svcs  │
│        │                                       │            │
│        ▼                                       ▼            │
│  JWT validate + session DB check        Domain logic        │
│                                                             │
└────────────────────────────┬────────────────────────────────┘
                             │ Spring Data MongoDB
                             ▼
            ┌───────────────────────────────────────┐
            │              MongoDB                  │
            │  users · roles · sessions · tokens    │
            │  micro_activity_record                │
            └───────────────────────────────────────┘
```

### Request lifecycle

1. Client hits an endpoint with `Authorization: Bearer <jwt>`.
2. `MtmSessionFilter` parses the JWT, calls `AuthenticationAndAuthorizationService.validateCurrentUserSessionForAccessToken`, and looks up the access token + session in MongoDB.
3. Valid → the resolved `User` becomes the Spring Security principal for the rest of the request. Expired/invalid → `401`. Unexpected exception → `500` (deliberately distinct from auth failures).
4. Controllers route to services, which return either DTOs or a `GenericMessageResponseDTO` envelope.

## Tech stack

### Backend

| Concern | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.10 |
| Persistence | Spring Data MongoDB |
| Security | Spring Security 6 + custom `MtmSessionFilter` |
| Auth tokens | `jjwt 0.13.0` (HS512) + DB-backed sessions / refresh tokens |
| Validation | `spring-boot-starter-validation` (Bean Validation) |
| API docs | springdoc-openapi 2.8.15 (Swagger UI) |
| Build | Maven, fat-jar via `spring-boot-maven-plugin` |
| Logging | Logback (rolling file) |
| Testing | JUnit 5, Mockito, AssertJ |

### Frontend

| Concern | Choice |
|---|---|
| Framework | React 18 (Create React App) |
| Routing | React Router v6 |
| Styling | Tailwind CSS 3 (prefix `mtm-`) + a custom CSS-variable design system (emerald/teal, light + dark) |
| Fonts | Inter (body) + Sora (display) |
| HTTP | Axios (shared client with request + response interceptors) |
| State | `useState` + a focused `useAuth` hook + a tiny pub/sub on top of `localStorage` |
| Theme | `useTheme` hook — persisted light/dark toggle with no-flash boot |
| Icons | `react-icons` |

### Infrastructure

| Concern | Choice |
|---|---|
| Full stack | Docker Compose (MongoDB + backend + frontend; Mongo Express behind the `tools` profile) |
| Images | Multi-stage `Dockerfile`s — backend (Maven → JRE), frontend (Node → nginx) |
| CI | GitHub Actions (`.github/workflows/ci.yml`) — backend tests on a `mongo:7` service + frontend build/test |
| Profiles | `dev` (port 8080, context `/mtm-dev`) · `prod` (env-var driven, locked CORS) |

## Project layout

```
MicroTimeManagement/
├── backend/api-service/                Spring Boot REST API
│   ├── src/main/java/.../apiservice/
│   │   ├── callbacks/                  BeforeConvertCallback (audit fields)
│   │   ├── config/                     Profile-aware Spring Security
│   │   ├── constants/                  Centralised error / role / pagination constants
│   │   ├── controller/                 Auth · User · Role · Activity · Admin
│   │   ├── converter/                  Model ↔ DTO
│   │   ├── dto/{entity,request,response}
│   │   ├── enums/
│   │   ├── exceptions/                 Custom hierarchy + @ControllerAdvice
│   │   ├── filter/                     MtmSessionFilter (per-request session validation)
│   │   ├── handler/                    Global exception handler
│   │   ├── model/                      MongoDB documents
│   │   ├── repository/                 Spring Data interfaces
│   │   ├── service/{,impl}             Business logic
│   │   └── utils/                      JWT + API utilities
│   └── src/test/java/.../apiservice/   Service-layer tests + test factories
├── frontend/                           React 18 SPA
│   └── src/
│       ├── Pages/                      Home · Login · Registration · Dashboard ·
│       │                               Activity · Profile · Admin
│       ├── components/                 ProtectedRoute · AdminRoute · NavigationBar ·
│       │                               ThemeToggle · Toast · Footer
│       ├── hooks/                      useAuth (auth + profile/roles) · useTheme (light/dark)
│       ├── service/                    AuthStorage (token store + pub/sub),
│       │                               ApiService (shared axios + endpoint wrappers)
│       └── style/                      Tailwind + CSS-variable design system (light/dark)
│   ├── Dockerfile                      Multi-stage build → nginx
│   └── nginx.conf                      SPA fallback
├── .github/workflows/ci.yml            GitHub Actions CI
├── docker-compose.yml                  Full stack: MongoDB + backend + frontend (+ mongo-express via `tools`)
└── README.md                           You are here.
```

## Getting started

### Prerequisites

- **Java 17+** (Temurin / Adoptium recommended)
- **Node.js 18+** and **npm 9+**
- **Docker** & **Docker Compose** (for MongoDB locally)
- **Maven** is bundled via `./mvnw`; no global install needed

### 1. Clone

```bash
git clone git@github.com:vishu221b/MicroTimeManagement.git
cd MicroTimeManagement
```

### Option A — run the whole app with Docker (recommended)

```bash
docker compose up --build
```

That builds and starts everything:

| Service | Port | What it is |
|---|---|---|
| `mtm_frontend` | `3000` | React SPA (nginx) at <http://localhost:3000> |
| `mtm_backend` | `8080` | Spring Boot API at <http://localhost:8080/mtm-dev> |
| `mtm_mongo` | `27017` | MongoDB (data persisted in the `mtm_mongo_data` volume) |

Add the DB UI when you need it:

```bash
docker compose --profile tools up   # also starts mongo-express on :8081
```

- Swagger UI: <http://localhost:8080/mtm-dev/swagger-ui/index.html>
- Health endpoint (open): <http://localhost:8080/mtm-dev/actuator/health>

Overridable env: `MTM_JWT_SECRET`, `MTM_CORS_ORIGINS`, `SPRING_DATA_MONGODB_URI` (backend) and the `REACT_APP_API_BASE_URL` build arg (frontend). Defaults work out of the box on `localhost`.

### Option B — local dev (hot reload + breakpoints)

Run just MongoDB in Docker, then the apps directly.

```bash
docker compose up -d mtm_mongo        # MongoDB on :27017
```

```bash
cd backend/api-service && ./mvnw spring-boot:run   # API on :8080/mtm-dev
```

```bash
cd frontend && npm install && npm start            # SPA on :3000
```

The dev server runs on **<http://localhost:3000>** and talks to the backend at `http://localhost:8080/mtm-dev/api/v1`.

## Configuration

The dev profile (`application-dev.yml`) ships with sensible defaults so you can `mvn spring-boot:run` after `docker compose up`. The prod profile (`application-prod.yml`) requires every secret to come from the environment.

| Variable | Scope | Default | Purpose |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | both | `dev` | Switches between dev and prod config |
| `MTM_JWT_SECRET` | dev (optional) / prod (**required**) | bundled dev fallback | Symmetric key for HS512 JWT signing |
| `MTM_MONGO_USERNAME` | prod | — | Mongo Atlas user |
| `MTM_MONGO_PASSWORD` | prod | — | Mongo Atlas password |
| `MONGO_STORE_CLUSTER` | prod | — | Atlas cluster hostname segment |
| `MONGO_STORE_DATABASE` | prod | — | Atlas database name |
| `LOG_FILE_PROPERTIES` | prod | — | Path for rolling log file |

> The dev JWT secret has been left in the repo intentionally so the app boots out of the box. **Override it in any environment you don't fully control.**

## API surface

A trimmed map of the v1 API — full schemas live in the Swagger UI.

### Auth

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | public | Username/email + password → access + refresh tokens |
| `POST` | `/api/v1/auth/refresh` | public | Refresh token → new access token |
| `POST` | `/api/v1/auth/logout` | `USER_OPS` | Revoke current session |

### User

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/user/register` | public | Create new account (default roles: `USER_OPS`, `ACTIVITY_CRUD`) |
| `GET` | `/api/v1/user/profile` | `USER_OPS` | Current user, including roles |
| `PUT` | `/api/v1/user/update` | `USER_OPS` | Update own first/last name, email, username, DOB |
| `POST` | `/api/v1/user/resetPassword` | `USER_OPS` | Verify old password, set new password (≥ 8 chars) |
| `DELETE` | `/api/v1/user/delete` | `USER_OPS` | Soft-delete current account |
| `GET` | `/api/v1/user/getByUserId` | `ADMIN_OPS` | Look up any user by UID |
| `GET` | `/api/v1/user/all` | `ADMIN_OPS` | Paginated, sortable user listing |
| `POST` | `/api/v1/user/addRole` | `ADMIN_OPS` | Bulk add roles by uid/email/username |
| `DELETE` | `/api/v1/user/removeRole` | `ADMIN_OPS` | Bulk remove roles |

### Activity

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/activity` | `ACTIVITY_CRUD` | Create / append activity (overlap-validated) |
| `GET` | `/api/v1/activity/getAllForDate?date=` | `ACTIVITY_CRUD` | Activities for a date, scoped to current user |
| `PUT` | `/api/v1/activity?date=` | `ACTIVITY_CRUD` | Update an activity (metadata-only or full re-time) |
| `DELETE` | `/api/v1/activity?date=&recordId=` | `ACTIVITY_CRUD` | Remove a single activity from a day's record |

### Role

| Method | Path | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/v1/role` | `ROLE_CRUD` | Create role |
| `GET` | `/api/v1/role` | `ROLE_CRUD` | List or get by id (paginated) |
| `PUT` | `/api/v1/role` | `ROLE_CRUD` | Rename role (conflict-checked) |
| `DELETE` | `/api/v1/role?roleId=` | `ROLE_CRUD` | Soft-delete role |

## Testing

### Backend

```bash
cd backend/api-service
./mvnw test                                # full suite
./mvnw test -Dtest=ActivityRecordServiceImplTest   # one class
```

Service-layer coverage today (selected highlights):

| Suite | Notes |
|---|---|
| `ActivityRecordServiceImplTest` | 14 cases — create / append / overlap / duplicate / invalid date / get / delete / update (metadata + retime + overlap rejection) |
| `UserServiceImplTest` | Registration, lookup, profile fetch, update (with cross-uid IDOR rejection), password change happy + wrong-old-password rejection |
| `RoleServiceImplTest` | 8 cases — create / get / list / soft-delete / rename happy / no-op / conflict / not-found |
| `AuthenticationAndAuthorizationServiceImplTest` | Login happy + bad password |
| `ApiUtilsTest` | Pagination sanitisation edge cases |

> A small number of `SessionServiceImplTest` cases and `ApiServiceApplicationTests.contextLoads` are known-flaky (Mockito stubbing mismatch + Mongo dependency). They are tracked in `CLAUDE.md` and excluded from the green-bar definition.

### Frontend

```bash
cd frontend
npm run build         # production bundle (also runs ESLint)
npm test              # Jest in watch mode (test coverage is the next milestone)
```

## Roadmap

### Shipped
- ✅ Auth flow with single-flight refresh, protected + admin routes
- ✅ Activity tracker CRUD UI
- ✅ Profile + password change UI
- ✅ Admin role management UI

### Next up
- 📊 Dashboard with recent-activity summary
- 🧪 React Testing Library coverage for pages + hooks
- 👥 User → Role assignment UI in the admin panel
- 📆 Migrate string dates to `LocalDate` and swap the DOB text input for a date picker
- ⚡ Bulk role lookup (eliminate the per-request N+1 in `UserServiceImpl.replaceRoleIdsWithNamesForUser`)
- 🐳 Wire the prod Docker Compose app service with env-var-driven config

A complete pending list lives in `CLAUDE.md`.

## Contributing

This is a personal portfolio project, but PRs and issues are welcome.

1. Fork → branch (`feat/<thing>`).
2. **Backend changes ship with tests.** Service-layer logic is TDD-friendly — write the test first.
3. Run `./mvnw test` and `npm run build` before pushing.
4. Open a PR against `main` with a description of *what* changed and *why*.

The development conventions (package layout, exception hierarchy, response wrappers, callback contracts, etc.) are documented in `CLAUDE.md` and updated alongside every feature.

## Authors

- [Vishal Dogra](https://github.com/vishu221b) — design, product direction, original build, review of every change.
- [Claude (Anthropic)](https://www.anthropic.com/claude) — co-author. Paired on the later milestones: auth hardening, the activity tracker UI, the profile + password flows, the admin role panel, and most of the service-layer test coverage.

If this project taught you something, or you spot a bug, please open an issue — feedback is the entire point of shipping it publicly.
