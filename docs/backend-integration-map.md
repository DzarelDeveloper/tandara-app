# Parent backend integration map

- Health: `GET /api/health`; success is `data.status == "ok"`.
- Login: `POST /api/auth/login` with JSON `username` and `password`.
- Token: HS256 JWT returned as `data.access_token`; requests use `Authorization: Bearer <token>`.
- Generic identity: `GET /api/auth/me`.
- Parent session: `GET /api/parent/session`; requires role `PARENT`, an active GuardianAccount/Guardian, and exactly one active GuardianStudent link.
- Parent assignment failures: `NO_ASSIGNED_STUDENT` and `MULTIPLE_STUDENT_CONFIGURATION` (HTTP 409).
- Admin ownership: Admin IT creates the Guardian, generated User account, and one GuardianStudent link through `/api/guardians`.
- Android configuration: `BuildConfig.TANDARA_API_BASE_URL`, supplied by Gradle property or environment variable `TANDARA_API_BASE_URL`.
- Android session: JWT in EncryptedSharedPreferences; non-sensitive identity/onboarding/theme remain in DataStore.

Attendance, reports, leave requests, and notifications remain mock-backed in Android during this phase.
