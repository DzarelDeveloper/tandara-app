# Tandara Parent — Backend Integration Plan

Audit date: 2026-09-28 (Asia/Jakarta)

This document records the exported Android project's current baseline and a future integration plan. It does not define a final backend contract, connect the app to a server, add WebSocket behavior, or add FCM.

## 1. Current Android architecture

- Single Android application module (`:app`) written in Kotlin.
- UI is 100% Jetpack Compose; the XML files are resources, themes, launcher assets, and backup rules, not View layouts.
- Navigation uses Navigation Compose with one `NavHost` in `TandaraNavHost.kt`.
- The code is a small layered/MVVM-style architecture: Compose screens -> `ViewModel`/`StateFlow` -> domain repository interfaces -> manually constructed mock repository implementations.
- `DefaultAppContainer` is a manual service locator. Hilt/Koin is not present.
- Domain models are in `domain/model`; repository contracts are in `domain/repository`; mock implementations and unused remote scaffolding are in `data`.
- Preferences/session flags use Preferences DataStore. Room is declared but has no entities, DAOs, or database. Coil is declared but image loading currently uses packaged resources.
- Retrofit, OkHttp, and Moshi scaffolding exists, but `ApiClient` and its API interfaces are not injected into or called by the active repositories. The live app remains local/mock-only.

### Package/configuration identity

- Kotlin source package: `id.tandara.parent`
- Android namespace: `id.tandara.parent`
- Application ID: `id.tandara.parent`
- App label/root project: `Tandara Parent` / `TandaraParent`

The source package, namespace, and application ID were aligned during Android foundation cleanup. Existing installations under an earlier application ID remain separate Android applications.

### Project topology

```text
app/src/main/java/id/tandara/parent/
├── MainActivity.kt                 Compose host and persisted theme resolution
├── TandaraApplication.kt          Manual container owner
├── core/
│   ├── common/                    Container, dates, phone helpers, notification permission check
│   ├── designsystem/              Semantic colors, Material schemes, typography, shapes
│   ├── navigation/                Route declarations and NavHost
│   └── network/                   Result wrapper and currently unused URL configuration
├── data/
│   ├── remote/                    Unused Retrofit client, interfaces, and DTOs
│   ├── repository/                Active local/mock repositories and future token stub
│   └── session/                   Preferences DataStore session/onboarding/theme flags
├── domain/
│   ├── model/                     Parent, one Student, attendance, leave, notification models
│   └── repository/                Repository contracts
└── ui/
    ├── auth/                      Login screen/state/ViewModel
    ├── components/                Shared Compose components; several are unused
    ├── home/                      Dashboard screen/state/ViewModel
    ├── onboarding/                Welcome and three-page onboarding
    ├── permission/                Leave form plus static leave history
    ├── reports/                   Attendance history/summary
    ├── settings/                  Settings and five sub-screens
    └── splash/                    Session/onboarding restoration router
```

There are no `font`, `raw`, or `assets` resource directories. Two logo JPEGs are byte-identical. Generated/template residue includes sample backup XML, placeholder unit tests, timestamp-suffixed images, and unused component/dependency scaffolding.

## 2. Current screens

| Screen/file | Route | Purpose | Current source/state | Backend data eventually required |
|---|---|---|---|---|
| `SplashScreen.kt` | `splash` | Restore first-run/session state | DataStore flows plus 750 ms display | Local session/token validity decision |
| `WelcomeScreen.kt` | `welcome` | First-install entry | Static Compose copy/illustration | None |
| `OnboardingScreen.kt` | `onboarding` | Three-page feature introduction | Static Compose copy/illustrations | None |
| `LoginScreen.kt` | `auth/login` | Parent login | `LoginViewModel`; hardcoded local credentials | Auth login and server error model |
| `HomeScreen.kt` | `home` | Assigned student and today's attendance dashboard | Mock repositories plus some hardcoded display fallbacks | Parent dashboard, assigned student, today's attendance, notification summary |
| `ReportsScreen.kt` | `reports` | Monthly summary and attendance records | Mock repository; hardcoded month navigation/fallback counts | Paginated/filterable attendance history and report/download capability |
| `PermissionScreen.kt` | `permission` | Leave form and request history | Mock assigned student; submission returns backend-unavailable; history is hardcoded in UI | Leave list, submission, attachment policy/upload, statuses |
| `SettingsScreen.kt` | `settings` | Account, appearance, notification preferences, help, logout | Parent DataStore session; theme persisted; two notification toggles are local `remember` state | Profile summary and eventual server/device notification preferences |
| `ParentProfileScreen.kt` | `settings/profile` | Read-only parent identity | Session parent plus hardcoded username, school, status | Parent profile: name, username, phone, role, school, account status |
| `PrivacyScreen.kt` | `settings/privacy` | Privacy/security information | Static copy | Prefer versioned/legal content source if required |
| `FaqScreen.kt` | `settings/faq` | Searchable FAQ | Static in-file FAQ list and local Compose state | Optional CMS/static packaged content |
| `AboutScreen.kt` | `settings/about` | Product/team/version information | Static copy; external portfolio URL | Build-derived version and approved links/copy |
| `ChangePasswordScreen.kt` | `settings/change-password` | Password form | Local Compose state only; no repository call | Authenticated password-change API if school policy permits |
| Notification center sheet | no route; opened from Home | Notification list/filter | Ignores supplied repository notifications and renders a separate static list | Notifications, unread count, mark-one/read-all |
| Attendance detail sheet | no route; inside Reports | Record detail | Selected mock attendance record | Attendance record detail (if not included in history payload) |
| Student detail sheet | no route; inside Home | Read-only assigned student detail | Mock assigned student plus static school | Assigned student detail |

Major baseline issues: Home still hardcodes `Bapak Budi` and several attendance times despite having state; Reports month controls only replace labels; the notification sheet ignores its `notifications` parameter; leave history is not loaded from its repository; Change Password is declared but Settings opens an informational dialog instead, making the route unreachable.

## 3. Current mock data sources

### Runtime preview mocks that should later be replaced by backend data

- `data/session/SessionManager.kt`: `081234567890`, `parent123`, `Budi Santoso`, role, and local authenticated-session fields.
- `data/repository/AuthRepositoryImpl.kt`: local credential comparison and `mock_parent_1`.
- `data/repository/ParentRepositoryImpl.kt`: assigned student Alya Putri, NIS/NISN `00192381`, class, avatar, and three notifications.
- `data/repository/AttendanceRepositoryImpl.kt`: fixed September 2026 attendance, totals, and an unused Rizky branch.
- `ui/components/NotificationCenterSheet.kt`: four static notifications; currently overrides/ignores repository input.
- `ui/permission/PermissionScreen.kt`: three static leave-history cards.
- `ui/home/HomeScreen.kt`, `ui/reports/ReportsScreen.kt`, `ui/settings/SettingsScreen.kt`, `ui/settings/ParentProfileScreen.kt`, and `ui/components/ChildComponents.kt`: fallback parent/student names, school, times, username, account status, and summary values.

### Safe static preview/onboarding content

- `WelcomeScreen.kt` and `OnboardingScreen.kt`: explanatory illustrations and demo attendance/leave copy.
- FAQ, About, Privacy, empty/loading/disconnected states, and reusable components.
- Packaged demo avatar and brand/launcher images.

Keep explicit preview fixtures separate from production repositories during integration so missing API data cannot silently become plausible fake data.

## 4. Current navigation

```text
Splash
├── first install ──> Welcome ──> Onboarding ──> Login ──> Home
│                         └────────────────────> Login
├── returning, logged out ─────────────────────> Login
└── authenticated ─────────────────────────────> Home

Home <── bottom navigation ──> Reports
  │                           Permission
  │                           Settings
  │                              ├── Profile
  │                              ├── Privacy
  │                              ├── FAQ
  │                              └── About
  ├── Notification sheet
  └── Student detail sheet

Declared but unreachable: Settings/Change Password
Reports -> attendance detail sheet
```

All declared routes have destinations and there are no duplicate route strings. There is no dedicated Notifications or Attendance Detail route because those experiences are modal sheets. The bottom-navigation `popUpTo(findStartDestination())` targets the graph's Splash destination, which is removed after launch; this should be device-tested and replaced with an explicit stable main-graph strategy if tab back stacks accumulate. Logout clears the navigation stack and goes to Login. Home implements double-back-to-exit; root Welcome/Login exit directly.

## 5. First-run and session behavior

- `has_completed_onboarding`, authentication, parent display fields, appearance, and one notification preference are stored in Preferences DataStore (`tandara_session_prefs`).
- First install routes Splash -> Welcome -> Onboarding -> Login.
- Returning logged-out routes Splash -> Login.
- Returning locally authenticated routes Splash -> Home.
- Splash reads onboarding and session state before navigation, so Login should not flash during restoration.
- Logout removes only authentication/name/phone/role and preserves onboarding and preferences, so it does not restart onboarding.
- Current authentication is a boolean local mock, not a real token-backed or server-validated session.

## 6. Theme architecture

- Modes are `light`, `dark`, and `system`; DataStore default is `light` as required.
- `MainActivity` resolves `system` with `isSystemInDarkTheme()` and supplies `TandaraTheme`.
- The design system defines light/dark semantic colors, Material color schemes, typography, and shapes.
- Most screens use semantic tokens. Remaining direct colors exist in the logo artwork, selected controls/white-on-primary content, one Home button, date pickers, settings switch, and the password-strength amber. Some may be intentional contrast colors, but they bypass semantic tokens.
- No centralized spacing scale exists; spacing is repeated with literal `dp` values.

## 7. Parent/student state model

Target invariant:

```text
ParentSession
├── parent
└── student
```

The visible UX has no child selector, carousel, search, arbitrary ID field, “Pilih Anak,” or “Ganti Anak.” Profile identity is read-only and separate from Privacy.

However, legacy multi-child-shaped abstractions remain: `ParentRepository.getLinkedStudents(): List<Student>`, `HomeUiState.linkedStudents`, `ReportsUiState.linkedStudents`, `selectedStudent` aliases, and Permission taking the first linked student. Replace these during integration with one nullable/required assigned-student value resolved from the authenticated parent. The client must never use this UX invariant as authorization.

## 8. Proposed network layer

Do not activate the existing API interfaces until the actual FastAPI contract is checked. When integration begins:

1. Keep Retrofit + OkHttp + Moshi only if they match team standards; remove unused Firebase AI, App Check, Room, Coil, Camera/Location catalog entries, and other dependencies only after usage verification.
2. Replace mutable `NetworkConfig` constants with a single build-variant-provided `BuildConfig.API_BASE_URL`; do not put a random LAN address in source.
3. Inject API services into repositories via the existing manual container initially, or adopt one DI framework in a separate deliberate change.
4. Add an authenticated OkHttp interceptor, normalized API error envelope, timeouts, and redacted debug-only logging.
5. Map DTOs to domain models; do not expose DTOs to UI state.
6. Make backend ownership resolution authoritative. Avoid caller-editable/arbitrary student IDs wherever the backend can infer the assigned student from JWT.

The current Retrofit endpoints are speculative `api/v1/...` paths and must not be treated as the backend contract.

## 9. Proposed authentication flow

```text
Login credentials
  -> POST login
  -> validate response
  -> store token in encrypted, non-backed-up storage
  -> fetch/derive ParentSession(parent, assignedStudent)
  -> Home

Cold launch
  -> read token securely
  -> validate/refresh according to backend contract
  -> Home or Login without rendering an intermediate Login screen

Logout
  -> revoke server session if supported
  -> close WebSocket
  -> clear token and identity cache
  -> preserve onboarding/theme
  -> Login
```

Never store a password. Preferences DataStore alone is not appropriate for bearer-token protection. Define backup exclusions before storing tokens or sensitive cached data.

## 10. Proposed API mapping (provisional)

Exact paths and payloads require validation against the real backend.

| Android concern | Expected backend capability |
|---|---|
| Login/Splash session | `POST /api/auth/login`; token validation/refresh/logout if available |
| Parent profile/Settings | `GET /api/parent/profile` |
| Home aggregate | `GET /api/parent/dashboard` |
| Assigned student | `GET /api/parent/students` then enforce exactly one in product handling, or preferably an ownership-derived singular endpoint |
| Student detail | `GET /api/parent/students/{student_id}` |
| Today card | `GET /api/parent/students/{student_id}/attendance/today` |
| Reports/history | `GET /api/parent/students/{student_id}/attendance` |
| Notification sheet | `GET /api/parent/notifications` |
| Badge count | `GET /api/parent/notifications/unread-count` |
| Read one/all | `PATCH /api/parent/notifications/{id}/read`; `PATCH /api/parent/notifications/read-all` |
| Leave history/form | `GET /api/parent/leave-requests`; `POST /api/parent/leave-requests` |
| Foreground realtime | `WS /ws/parent?token=<JWT>` or a safer negotiated/auth-header mechanism supported by the backend |

Even when an endpoint contains a student ID, the server must verify that the authenticated parent owns the requested student. Android is not an authorization boundary.

## 11. Proposed WebSocket architecture

No WebSocket client currently exists. Place it alongside the network data source, expose a typed event stream from a parent/realtime repository, collect it in an application/session-scoped coordinator or ViewModel, and reduce events into screen state:

```text
OkHttp WebSocket data source
  -> typed realtime repository (Flow)
  -> session-scoped connection/retry coordinator
  -> ViewModel state refresh/reduction
  -> Home / Reports / Leave / Notifications UI
```

Support `STUDENT_CHECK_IN`, `STUDENT_CHECK_OUT`, `ATTENDANCE_CORRECTED`, `LEAVE_APPROVED`, and `LEAVE_REJECTED` only after the server event schema is confirmed. Define authentication, exponential backoff with jitter, network/lifecycle awareness, deduplication/event IDs, ordering, token expiry, and reconciliation via REST after reconnect. Do not rely on WebSocket delivery as the persistent source of truth.

## 12. Proposed notification architecture

1. Persistent backend notification records are the source of truth.
2. REST supplies list, unread count, and read mutations.
3. Foreground WebSocket events trigger local UI updates and/or refetch.
4. FCM is a later background-delivery phase only; its payload should lead back to persistent server data.

The current notification center is an appropriate surface, but it must consume its supplied state, expose read actions, and handle loading/empty/error states before integration is complete. The current settings toggles are not persisted consistently and should not imply server push behavior.

## 13. Development base URL and HTTP strategy

- Android device `localhost` refers to that device, not the development laptop.
- A physical device will use an environment-specific laptop LAN address such as `http://192.168.x.x:8000`, supplied outside source control through the centralized build configuration.
- Emulator development may use `10.0.2.2`, also through the same build configuration.
- Production must use HTTPS.
- The manifest has no cleartext opt-in or network security configuration. With the current target SDK, HTTP is blocked by default. If LAN HTTP is temporarily required, add a debug-only, narrowly scoped network security policy for the known development host rather than globally enabling cleartext.
- Current source contains active-looking hardcoded HTTP examples (`10.0.2.2` and `192.168.10.10`) even though networking is unused. Remove/replace them when the build-variant strategy is introduced.

## 14. Security considerations

- No real API key, JWT, access token, or production password was found.
- `parent123` is a plainly labeled mock password; keep it out of production variants and screenshots/docs intended as real credentials.
- `.env.example`, README, metadata, Firebase AI dependency, App Check dependencies, Google Services plugin, and secrets plugin are AI Studio artifacts. The app does not call Gemini and should not need a Gemini key.
- `googleServices.missing.passthrough=true` and the Firebase scaffolding can hide an incomplete service setup; remove only after the dependency cleanup decision.
- The generated release signing block reads passwords from environment variables, which is appropriate in principle, but the default keystore path and release process must be reviewed. The debug block contains standard debug-keystore credentials and references a missing ignored `debug.keystore`.
- Empty/sample backup rules plus `allowBackup=true` do not explicitly exclude session DataStore. Define least-privilege backup behavior before real tokens or personal data exist.
- Static Privacy copy claims TLS 1.3, encrypted credentials, revocable tokens, and regulatory posture that the current mock app does not implement. Legal/security copy must match deployed reality.
- HTTP logging is set to headers; authentication/cookie headers still require explicit redaction and logging should be debug-only.
- Only `INTERNET` is requested. There is no camera, location, storage, or notification permission in the manifest. The notification permission helper currently checks a permission that is not declared and is unused by UI.

## 15. Readiness findings and cleanup candidates

- UI foundation: broadly reusable Compose tokens/components and coherent screens, but several screens duplicate hardcoded display data and bypass repository state.
- Design-system placeholders/unused definitions: `BackendDisconnectedCard`, `LoadingState`, `EmptyState`, `TandaraCard`, base `StatusBadge`, attendance/leave badge helpers, and `SectionHeader` have no external call sites.
- Network scaffolding is unused and speculative; `ApiClient` has no consumer.
- `NotificationTokenRepository` and FCM DTO/API methods are premature Stage 7 scaffolding, not an FCM implementation.
- Room, Coil, Firebase AI, Firebase App Check, KSP Room compiler, and Google Services/secrets plugins appear unused by app code. Version-catalog Camera, Location, Accompanist, Firebase Auth/Firestore, and credentials entries are also unused/commented.
- Logo JPEGs `ic_tandara_logo.jpg` and `ic_tandara_logo_1790257730907.jpg` are exact duplicates.
- `DateUtils.getIndonesianGreeting` has different boundaries (04:00 morning; 18:00 still afternoon) from the correct Home greeting helper and should not become a second source of truth.
- About/Settings version strings (`2.0.0`, `3.0`) conflict with Gradle `versionName = 1.0`.
- No obsolete school names were found; current static school identity is consistently SMK Taman Harapan.

## 16. Migration phases

### Phase 1 — Android networking foundation

- Confirm Retrofit/OkHttp/Moshi against team standards.
- Add build-variant `API_BASE_URL`, DTO/domain mapping, API error model, debug-only redacted logging, and scoped debug HTTP policy if required.
- Remove or isolate unused AI Studio/Firebase and speculative network artifacts after verification.

### Phase 2 — Authentication

- Parent login, secure token storage, validated session restore, and logout/revocation.
- Preserve onboarding/theme on logout and avoid Login flash during restoration.

### Phase 3 — Parent core data

- Read-only profile, exactly one assigned student, dashboard, today's attendance, and attendance history.
- Remove legacy list/selected-student abstractions and all runtime fake-data fallbacks.

### Phase 4 — Leave requests

- List, submit, attachment handling, validation, and status reconciliation.

### Phase 5 — Notifications

- Persistent list, unread count, mark-one/read-all, loading/empty/error states.

### Phase 6 — Realtime

- Parent WebSocket, authenticated lifecycle, reconnect/reconciliation, typed events, and UI state updates.

### Phase 7 — Background push

- FCM dependency/configuration, device registration, token lifecycle, notification channels, permission UX, and deep links.

## 17. Baseline build/device validation

The export is missing `gradlew`, `gradlew.bat`, and `gradle-wrapper.jar`; only `gradle-wrapper.properties` exists. The configured distribution is Gradle 9.3.1. The current machine also has no Java runtime or global Gradle. Therefore `./gradlew --version`, `assembleDebug`, `test`, and `lint` all stop immediately with “No such file or directory.” No APK exists and compilation compatibility cannot yet be verified.

Configured values (not execution-verified): AGP 9.1.1, Kotlin/Compose compiler plugin 2.2.10, Compose BOM 2024.09.00, Java source/target 11, compile SDK 36.1, target SDK 36, minimum SDK 26.

`adb devices` runs successfully but reports no connected device. Device install was not attempted.

Before Phase 1, restore a complete trusted Gradle Wrapper, install a JDK compatible with AGP/Gradle, then run `./gradlew --version`, `./gradlew assembleDebug`, `./gradlew test`, and `./gradlew lint`. Treat any resulting compile/lint failures as new evidence rather than assuming this source currently builds.
