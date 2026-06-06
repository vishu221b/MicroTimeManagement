# Micro Time Management — CLAUDE.md

This file is the living architectural reference for the MTM project — package layout, data models, security wiring, endpoint map, completed work, and pending work — kept in step with the code. The README links here for the same reasons. Update it after every meaningful code change. (Previously gitignored; tracked from this commit on so cloners see the references the README invites them to.)

---

## What the App Does

A time/activity tracking app. Users register, log in, and record daily "activities" — time blocks with a name, description, start/end times (hour + meridian), and a date. Admins manage user roles. The backend enforces fine-grained role-based access control; the frontend is in progress.

---

## Repository Layout

```
MicroTimeManagement/
├── backend/api-service/          Spring Boot 3.x REST API (Maven)
├── frontend/                     React 18 SPA (CRA)
├── docker-compose.yml            MongoDB + Mongo Express only (app service commented out)
└── README.md
```

---

## Backend

### Tech Stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 3.5.10, Java 17 |
| Database | MongoDB via Spring Data MongoDB |
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

### Data Models & MongoDB Collections

All models extend `BaseModel` (`isActive: Boolean`, `createdAt: Date`, `lastUpdatedAt: Date`). These fields are auto-set by `BeforeConvertCallback` classes in `callbacks/`.

| Model | Collection | Notes |
|---|---|---|
| `User` | `mtm_user` | Implements `UserDetails`. `roles` field stores **role IDs** (resolved to names at auth time). `uid` is a separate unique identifier used externally. |
| `Role` | `mtm_role` | Simple `id + name`. |
| `Session` | `session` | Links a `User` to a `RefreshToken` via `@DocumentReference`. |
| `RefreshToken` | `refresh_token` | Has a list of `AccessToken` references + back-reference to `Session`. |
| `AccessToken` | `access_token` | Has a back-reference to `RefreshToken`. |
| `ActivityRecord` | `micro_activity_record` | `recordDate` (String, `yyyy-MM-dd`), `createdBy` (user UID), `activities` (embedded list). |
| `Activity` | embedded in `ActivityRecord` | Not a top-level collection. Stores epoch times + human-readable hour/minute values + meridian. |

**Key design decision — roles as IDs:** `User.roles` stores role document IDs. `UserServiceImpl.replaceRoleIdsWithNamesForUser()` resolves them to role name strings (`ROLE_MTM_...`) before setting Spring Security authorities. This happens on every `loadUserByUsername` call (login + every authenticated request). Consider caching if performance becomes an issue.

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

- **Dev**: `application-dev.yml` — port 8080, context `/mtm-dev`, MongoDB `localhost:27017/mtm_dev`, JWT secret hardcoded (must be moved to env var before production)
- **Prod**: `application-prod.yml` — separate config; CORS locked to `https://mtm.app`
- **Active profile**: set via `SPRING_PROFILES_ACTIVE` env var or `application.properties`
- **Docker Compose**: spins up MongoDB (27017) + Mongo Express (8081). App service is commented out — run the Spring Boot app directly in dev.

---

## Frontend

### Tech Stack

| Concern | Choice |
|---|---|
| Framework | React 18, CRA (`react-scripts 5`) |
| Routing | React Router v6 |
| Styling | Tailwind CSS 3.2 (prefix `mtm-`) + Bootstrap 5 + Ant Design 5 |
| HTTP Client | Axios |
| State | `useState` local state (no global store) |
| Icons | `react-icons` |

**Tailwind prefix is `mtm-`** — all Tailwind utility classes must be prefixed (e.g. `mtm-flex`, `mtm-text-white`). Configured in `tailwind.config.js`.

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
- [ ] **Pre-existing flaky tests**: `SessionServiceImplTest` has 7 failing/erroring methods due to Mockito stubbing returning `null` (NPE on `validSessionTokenDTO.getIsValidSession()`). Likely mismatch between test setup arguments and actual service call signatures.
- [ ] **`ApiServiceApplicationTests.contextLoads` requires MongoDB** — fails with `Connection refused` to `localhost:27017` if MongoDB isn't running. Add an `application-test.yml` with an embedded MongoDB (Flapdoodle) or skip the test when no Mongo is available.
- [ ] Populate `SecurityConstants.PROD` (currently empty class) and use it in prod filter chain
- [ ] `AdminController` is legacy (predates `RoleController`) — evaluate removing or consolidating
- [ ] Add integration tests for Activity, Role, Admin endpoints
- [ ] User UID generation is handled by callbacks but `uid` field isn't visible in `UserCallbacks` — verify it is auto-populated
- [ ] Prod Docker Compose (app service commented out — needs env vars wired)

### Frontend
- [ ] **Profile DOB picker**: currently a text input (`DD-MM-YYYY`) because the backend stores DOB as a string; swap to a date picker once dates are migrated to `LocalDate`.
- [ ] **Frontend test coverage**: unit/integration tests for ProtectedRoute, AdminRoute, useAuth, Activity, Profile, and Admin pages (including bulk-mode flows).
- [ ] **Profile page**: show current user details, allow updates
- [ ] **Admin panel**: role management UI (only visible to `ROLE_MTM_ADMIN_OPS` users)
- [ ] **ApiService.js**: add functions for remaining backend endpoints (activity CRUD, user profile, role admin, etc.)
- [ ] **Error handling**: surface 403 (forbidden) distinctly from 401 (the interceptor only handles 401-refresh today)
- [ ] **State management**: evaluate whether the `AuthStorage` subscriber pattern is enough or if React Context / Zustand is needed once more shared state appears
- [ ] **Cookie vs localStorage**: revisit token storage — current choice is `localStorage` (XSS-vulnerable) for simplicity; httpOnly cookies would be safer if the backend grows session endpoints to set them
