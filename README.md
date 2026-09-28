# Tandara Parent

Aplikasi Android untuk orang tua dalam ekosistem presensi sekolah Tandara.

Tandara Parent memungkinkan orang tua memantau informasi kehadiran siswa yang ditugaskan kepada mereka dari perangkat Android. Project ini merupakan prototype/demo untuk Developer Day 2026, bukan layanan publik yang siap produksi.

![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4)
![Min SDK](https://img.shields.io/badge/minSdk-26-3B82F6)
![Developer Day 2026](https://img.shields.io/badge/Developer%20Day-2026-0F172A)
![Prototype](https://img.shields.io/badge/Status-Prototype-FF8A00)

## Tentang Tandara Parent

Tandara adalah ekosistem presensi sekolah yang berfokus pada penggunaan sistem face recognition untuk pencatatan kehadiran siswa. Aplikasi ini dirancang sebagai antarmuka Android bagi orang tua untuk melihat status kehadiran anak yang telah ditugaskan kepada mereka.

Alur konseptualnya adalah:

Student
↓
Face Recognition
↓
Tandara Backend
↓
Attendance Data
↓
Tandara Parent

Saat ini, aplikasi Android masih menggunakan data lokal/mock untuk preview UI dan alur login/session. Integrasi dengan backend nyata belum diaktifkan di repo ini.

## Aturan Produk

Tandara Parent mengikuti prinsip berikut:

- Satu akun orang tua = satu siswa yang ditugaskan.
- Orang tua tidak memilih siswa secara manual dari daftar yang tidak terikat.
- Tidak ada switcher siswa atau pemilihan anak yang arbitrer di UI.
- Identitas siswa pada akhirnya harus ditentukan dari akun parent yang terautentikasi dan otorisasi backend.
- Backend tetap menjadi otoritas utama untuk kontrol akses.

Repo Android saat ini tidak mengimplementasikan penguatan otorisasi yang bersifat real-time di sisi client. Prinsip tersebut dipertahankan sebagai aturan desain untuk integrasi mendatang.

## Fitur

### Fitur saat ini / prototype

Aplikasi yang ada saat ini mencakup beberapa layar dan alur UI sebagai prototype demo:

- Splash screen
- Welcome screen
- Onboarding
- Login UI orang tua
- Dashboard Home
- Ringkasan kehadiran
- Riwayat/rekap kehadiran
- UI permohonan izin/cuti
- UI notifikasi
- Profil orang tua
- Settings
- Tema terang / gelap / mengikuti sistem
- Greeting dan format tanggal dalam Bahasa Indonesia
- Persistensi tingkat onboarding, tema, dan status sesi menggunakan DataStore

Catatan penting: data kehadiran dan sesi login saat ini masih dapat bersumber dari mock/local state. Fitur yang terlihat belum otomatis merepresentasikan sistem backend produksi.

### Fitur yang direncanakan setelah integrasi backend

Fitur berikut merupakan target integrasi mendatang dan belum menjadi sumber data aktif di repo saat ini:

- Autentikasi orang tua yang benar-benar terhubung ke backend
- Pengambilan data profil parent dari server
- Data siswa yang ditugaskan sesuai akun authenticated parent
- Rekap kehadiran yang real-time dan berbasis server
- Riwayat izin yang tersimpan di backend
- Notifikasi yang diambil dari sumber data server
- WebSocket untuk pembaruan realtime attendance
- Integrasi push notification di masa depan

## Alur Aplikasi

### Alur first install

```mermaid
flowchart LR
    A[Splash] --> B[Welcome]
    B --> C[Onboarding]
    C --> D[Login]
    D --> E[Home]
```

### Alur saat kembali membuka aplikasi

```mermaid
flowchart LR
    A[Splash] --> B[Login]
```

### Alur ketika sudah terautentikasi secara lokal

```mermaid
flowchart LR
    A[Splash] --> B[Home]
```

### Navigasi utama

```mermaid
flowchart TB
    H[Home] --> R[Reports]
    H --> P[Permission]
    H --> S[Settings]
```

Route utama yang terdefinisi di aplikasi saat ini adalah: Home, Reports, Permission, Settings, serta submenu profile/privacy/FAQ/about. Tidak ada route tambahan yang dibuat untuk fitur backend nyata.

## Preview

Tidak ada screenshot produk yang ditemukan di repository ini yang layak dipakai sebagai preview aplikasi. Untuk saat ini, placeholder disimpan agar tidak ada link rusak:

<!-- Tambahkan screenshot aplikasi di docs/screenshots/ -->
<!-- Layout yang disarankan: Home | Reports | Permission | Settings -->

## Tech Stack

### Aktif

- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- StateFlow
- Preferences DataStore
- Manual AppContainer / lightweight service locator

### Disiapkan / scaffolding

- Retrofit
- OkHttp
- Moshi
- Room (dideklarasikan, namun belum aktif digunakan sebagai database yang benar-benar terhubung)
- Coil (tersedia, tetapi tampaknya belum menjadi sumber gambar utama di runtime)
- Firebase AI / App Check scaffolding yang masih belum dihubungkan ke fitur yang aktif

Catatan: Retrofit, OkHttp, dan Moshi ada di project untuk integrasi backend masa depan, tetapi kode aktif saat ini tetap mengandalkan repo lokal/mock. Mereka belum menjadi transport layer yang sebenarnya untuk aplikasi yang berjalan.

## Arsitektur

Arsitektur saat ini bersifat ringan dan cocok untuk prototype UI:

```mermaid
flowchart TD
    A[Compose UI] --> B[ViewModel / StateFlow]
    B --> C[Repository Interface]
    C --> D[Mock Repository]
```

Interaksi app saat ini dilakukan melalui `DefaultAppContainer` yang menginisialisasi repository dan `SessionManager` secara manual. Tidak ada dependency injection framework seperti Hilt/Koin dalam repo ini.

### Target Integration

```mermaid
flowchart TD
    A[Compose UI] --> B[ViewModel]
    B --> C[Repository]
    C --> D[Retrofit / WebSocket]
    D --> E[Tandara FastAPI Backend]
```

Diagram di bawah ini merupakan target integrasi yang masih direncanakan; bukan representasi dari koneksi yang sudah aktif di aplikasi saat ini.

## Ekosistem Tandara

Repository ini hanya mencakup aplikasi Android parent. Sistem Tandara yang lebih besar terdiri dari komponen yang terpisah:

```text
Tandara
├── Admin IT Web Dashboard
├── Guru/Piket Web Dashboard
├── Face Recognition Attendance
├── FastAPI Backend
└── Tandara Parent Android App
```

Dashboard web, backend, dan sistem absensi face recognition adalah proyek yang terpisah dari aplikasi ini. Tandara Parent hanya bertindak sebagai client Android untuk orang tua/wali.

## Arsitektur Demo

Developer Day 2026 berorientasi pada demo lokal-first. Salah satu skenario yang dimaksud adalah:

```text
Laptop Server
├── FastAPI
├── Database
├── Face Recognition
├── DroidCam / Camera
└── Web Dashboard
       │
       └── Local Network
            ├── Dashboard Client
            └── Android Parent App
```

Pada demo lokal, ponsel Android dan server laptop harus dapat dijangkau dalam jaringan lokal yang sama. Ini penting untuk uji integrasi demo, namun repo yang ada belum mengeksekusi koneksi backend yang nyata.

## Persyaratan Development

Untuk menjalankan project ini secara lokal, diperlukan:

- JDK yang kompatibel dengan Android Gradle Plugin yang dipakai project
- Android SDK
- Platform Android sesuai dengan `compileSdk` yang didefinisikan project
- ADB
- Emulator Android atau perangkat fisik
- Android Studio atau VS Code dengan tooling Android yang sesuai

Project ini menggunakan Gradle Wrapper:

```bash
./gradlew
```

Konfigurasi saat ini memakai JDK 17, Gradle Wrapper 9.3.1, Android Gradle Plugin 9.1.1, dan Android platform `android-36.1`. Tidak diperlukan instalasi Gradle global.

## Menjalankan Project

Persiapan pertama kali memeriksa toolchain yang sudah terpasang. Script tidak memasang paket atau mengubah konfigurasi sistem secara otomatis:

```bash
./scripts/setup-android.sh
```

Pemeriksaan environment tanpa build atau perubahan device:

```bash
./scripts/check-android.sh
```

Untuk pengembangan harian, hubungkan ponsel melalui USB, aktifkan USB debugging, setujui prompt otorisasi, lalu jalankan:

```bash
./scripts/run-android.sh
```

Script akan membangun APK debug, memverifikasi application ID, meng-install atau memperbarui aplikasi tanpa uninstall, lalu membukanya. Jika ada beberapa device, pilih serial secara eksplisit dengan `--device <serial>`. Gunakan `--no-build` untuk meng-install APK debug yang sudah ada, `--logs` untuk mengikuti log aplikasi, atau `--check` untuk menjalankan pemeriksaan environment saja.

Identitas aplikasi Android adalah `id.tandara.parent`. Perubahan application ID membuat Android memperlakukan instalasi lama `com.aistudio.tandara.kztw` sebagai aplikasi terpisah; launcher tidak menghapusnya.

## Build APK

Build debug dijalankan dengan `./gradlew assembleDebug`; APK dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`. APK bersifat output lokal dan tidak perlu dikomit. Tidak ada konfigurasi release signing untuk workflow ini.

## Status Integrasi Backend

Status saat ini sesuai dengan audit kode yang ada:

| Komponen | Status |
|---|---|
| Android UI | Implemented |
| Local preferences | Implemented |
| Mock repository | Implemented |
| REST backend | Not Connected |
| Realtime WebSocket | Not Connected |
| FCM | Not Implemented |

Status ini mengacu pada repository saat ini: backend belum terhubung, autentikasi parent nyata belum aktif, REST tidak dihubungkan, dan realtime belum ditambahkan.

## Integrasi Backend yang Direncanakan

Konsep integrasi yang diharapkan adalah:

```mermaid
flowchart TD
    A[Tandara Parent Android] --> B[REST]
    A --> C[WebSocket]
    B --> D[FastAPI]
    C --> D
    D --> E[Tandara Database]
```

Saat ini, semua kontrak API dan skema WebSocket tetap harus diverifikasi terhadap backend nyata sebelum dapat dipakai di aplikasi. Dokumen yang relevan dapat dilihat di `docs/PARENT_BACKEND_INTEGRATION_PLAN.md`.

## Keamanan & Privasi

Prinsip yang penting untuk dipegang:

- Akses parent harus diotorisasi oleh backend.
- Client Android tidak boleh dipercaya sebagai otoritas keputusan akses siswa.
- Satu akun parent mewakili satu siswa yang ditugaskan.
- Proses face recognition adalah bagian dari sistem absensi sekolah, bukan fitur di aplikasi parent Android.
- Tandara Parent tidak memerlukan permission kamera untuk face recognition.
- Jangan menyimpan API key, token, password, atau secret di repo.
- Token autentikasi di masa depan harus disimpan dengan mekanisme penyimpanan yang aman dan sesuai kebutuhan produk.

Repo ini tidak mengimplementasikan mekanisme keamanan produksi yang nyata. Prinsip di atas lebih menggambarkan desain yang benar untuk integrasi mendatang.

## Tema

Aplikasi menyediakan dukungan tema terang, gelap, dan mengikuti sistem. Default yang terlihat pada code adalah mode `light` (Terang), dan `MainActivity` memetakan setting appearance menjadi `light`, `dark`, atau `system`.

## Struktur Project

Struktur utama source Android adalah:

```text
app/src/main/java/id/tandara/parent/
├── MainActivity.kt
├── TandaraApplication.kt
├── core/
│   ├── common/          Utility, app container, permission, helper
│   ├── designsystem/    Semantic color, theme, style token
│   ├── navigation/      Routes dan NavHost Compose
│   └── network/         Wrapper hasil API, konfigurasi jaringan yang belum aktif
├── data/
│   ├── remote/          Retrofit client, DTO, service scaffolding
│   ├── repository/      Mock repository aktif dan local service
│   └── session/         Preferences DataStore session/theme/onboarding
├── domain/
│   ├── model/           Model domain parent, student, attendance, leave
│   └── repository/      Interface repository
└── ui/
    ├── auth/            Login screen dan ViewModel
    ├── components/      Shared UI component
    ├── home/            Dashboard utama
    ├── onboarding/      Welcome & onboarding flow
    ├── permission/      Form izin/cuti
    ├── reports/         Riwayat dan ringkasan presensi
    ├── settings/        Pengaturan, profile, FAQ, privasi, about
    └── splash/          Router awal untuk onboarding/session
```

## Roadmap

- [x] Fondasi UI Android parent
- [x] First-run onboarding
- [x] Sistem tema
- [x] Dashboard mock parent
- [x] Stabilisasi build/toolchain
- [x] Identitas Android dan USB dev launcher
- [ ] Autentikasi parent nyata
- [ ] Integrasi REST backend
- [ ] Integrasi realtime attendance
- [ ] Integrasi leave request
- [ ] Notifikasi persisten
- [ ] WebSocket realtime
- [ ] Push notification background

Checklist di atas mencerminkan status yang dapat didukung oleh kode dan dokumen yang ada. Belum ada tanggal rilis yang ditetapkan.

## Tim

Team: Kena Scan

Anggota yang tercantum dalam project:

- Muhammad Dzarel Alghifari — Team Leader & Lead Developer
- Azzam — Hardware & Technical Support
- Labib — Testing & Documentation

## Developer Day 2026

Tandara merupakan project mahasiswa yang dikembangkan dalam konteks Developer Day 2026. Fokus project ini adalah prototyping ekosistem presensi sekolah berbasis face recognition dan aplikasi Android untuk orang tua.

## Tandara di Web

Struktur publik yang direncanakan adalah:

- tandara.id → landing page proyek resmi
- app.tandara.id → halaman showcase/download untuk Tandara Parent

Domain tersebut tidak dikonfirmasi sebagai situs yang aktif dalam repo ini, dan tidak ada bukti bahwa app.tandara.id saat ini mendistribusikan APK. Dalam konteks Developer Day, sistem absensi dapat tetap bersifat local-first sementara website digunakan untuk presentasi proyek.

## Lisensi

Belum ada file LICENSE yang ditemukan di repository ini. Lisensi proyek belum ditentukan di repo saat ini.

## Catatan Akhir

Proyek ini adalah prototype Android yang fokus pada pengalaman orang tua dalam ekosistem Tandara. UI, alur, tema, dan session state sudah dibangun dengan pendekatan Compose dan DataStore, tetapi integrasi backend, autentikasi nyata, dan data live belum aktif. README ini disusun berdasarkan audit kode yang sebenarnya, bukan asumsi dari backend yang belum ada.
# Android ↔ Local Backend Development

The laptop and physical Android phone must be connected to the same trusted LAN. Determine the laptop's LAN IPv4 address (for example with `ip -4 addr`; do not use `localhost` or `127.0.0.1` on the phone).

Start Tandara with deliberate LAN binding:

```bash
cd /home/dzarel/Projects/Tandara
./start-tandara.sh --lan --no-browser
```

This binds FastAPI to `0.0.0.0:8000` for the local network; it does not configure public internet exposure. Configure and launch Android with the current laptop address:

```bash
./scripts/run-android.sh --clean --api-url http://LAPTOP_LAN_IP:8000/
```

Alternatively set `TANDARA_API_BASE_URL` in the environment or pass it as the Gradle property `-PTANDARA_API_BASE_URL=...`. Debug builds allow local cleartext HTTP; release builds do not opt into cleartext. Log in with a Parent username/password provisioned by Admin IT. Never place a real password or JWT in project files.

The Phase 1 integration uses real parent identity and the one assigned student to validate the session. Attendance, reports, leave, and notifications are still the next migration phase and remain mock-backed in Android.
