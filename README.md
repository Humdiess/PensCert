# PENS Cert

<div align="center">

**Aplikasi Keamanan Dokumen Digital**
*Politeknik Elektronika Negeri Surabaya*

![Android](https://img.shields.io/badge/Android-7.0%2B-green)
![Java](https://img.shields.io/badge/Java-11-orange)
![Gradle](https://img.shields.io/badge/Gradle-9.1-blue)
![License](https://img.shields.io/badge/License-PENS-yellow)

</div>

---

PENS Cert adalah aplikasi keamanan dokumen digital untuk lingkungan **Politeknik Elektronika Negeri Surabaya (PENS)**. Aplikasi ini menyediakan fitur verifikasi dokumen, tanda tangan digital, dan manajemen sertifikat secara aman dan terintegrasi.

## Fitur Utama

### Verifikasi & Keamanan
- **QR Code Scanner** — Scan QR dengan animasi premium, kontrol flash, dan deteksi otomatis
- **Verifikasi Manual** — Verifikasi dokumen via input ID atau search bar
- **Quick Search** — Cari dokumen berdasarkan ID langsung dari dashboard
- **Hash SHA-256** — Integritas dokumen dijamin dengan cryptographic hash

### Manajemen Dokumen
- **Tanda Tangan Digital** — Pengajuan TTD ke dosen dengan wizard 3 langkah
- **Panel Admin (Dosen)** — Approval/rejection pengajuan + QR stamping PDF otomatis
- **Penerbitan Sertifikat** — Pembuatan sertifikat digital 3 langkah
- **Pencabutan Dokumen** — Revocation untuk dokumen yang sudah tidak berlaku
- **Filter & Sort** — Filter dokumen berdasarkan status + sort terbaru/terlama

### Dashboard & Analitik
- **Statistik Real-time** — Total dokumen, tervalidasi, dan tertunda
- **Achievement System** — Gamifikasi 6 level: 🎯🏆🏅⭐💎👑
- **Persentase Verifikasi** — Statistik persentase dokumen terverifikasi
- **Tips Keamanan** — Tips rotating tentang keamanan dokumen digital
- **Time-based Greeting** — Sapaan otomatis: Pagi/Siang/Sore/Malam

### UX & Navigasi
- **Bottom Navigation** — Home, Dokumen, Notifikasi, Profil + FAB QR di tengah
- **Edge-to-Edge Display** — Tampilan modern tanpa system bar gap
- **Notification Filter** — Filter notifikasi: Semua, Disetujui, Ditolak, Permintaan
- **Share App** — Bagikan aplikasi via native share sheet
- **Bantuan & Dukungan** — Pusat bantuan dengan kontak support
- **Clear Cache** — Bersihkan cache tanpa menghapus data akun
- **Keyboard-aware Login** — Form login auto-scroll saat keyboard muncul

## Tech Stack

| Komponen | Teknologi | Versi |
|---|---|---|
| Language | Java | 11 |
| Platform | Android | minSdk 24, targetSdk 36 |
| UI Framework | Material Design 3 | 1.14.0 |
| Activity | AndroidX Activity | 1.13.0 |
| Layout | ConstraintLayout | 2.2.1 |
| Database | Supabase (PostgreSQL REST) | — |
| Storage | Supabase Storage | — |
| Networking | OkHttp | 4.12.0 |
| Serialization | Gson | 2.11.0 |
| QR Code | ZXing Core + Embedded | 3.5.3 / 4.3.0 |
| PDF Processing | iTextG | 5.5.10 |
| Build Tool | Gradle + Version Catalog | 9.1 |

## Arsitektur

```
app/src/main/java/com/example/penscert/
│
├── 📱 UI Layer
│   ├── LoginActivity.java              # Autentikasi (NRP/NIP + Role)
│   ├── IntroActivity.java              # Onboarding slider (3 halaman)
│   ├── MainActivity.java               # Bottom nav + fragment host + QR FAB
│   ├── HomeFragment.java               # Dashboard + statistik + achievement
│   ├── DocumentsFragment.java          # Daftar dokumen + filter + sort + search
│   ├── NotificationsFragment.java      # Notifikasi + filter chips + clear
│   ├── ProfileFragment.java            # Profil + share + bantuan + cache
│   ├── DocumentDetailActivity.java     # Detail dokumen + status badge
│   ├── VerificationResultActivity.java # Hasil verifikasi QR / manual
│   ├── VerifyInputActivity.java        # Input ID verifikasi manual
│   ├── RequestSignatureActivity.java   # Pengajuan TTD (3-step wizard)
│   ├── SetQrPositionActivity.java      # Drag QR position pada PDF preview
│   ├── MyRequestsActivity.java         # Daftar pengajuan milik user
│   ├── AdminActivity.java              # Panel admin / dosen
│   ├── AdminRequestsActivity.java      # Approval queue + QR stamping PDF
│   ├── CreateCertificateActivity.java  # Penerbitan sertifikat (3-step)
│   ├── RevocationActivity.java         # Pencabutan dokumen
│   └── ScanQrActivity.java             # Custom QR scanner (portrait)
│
├── 🛠️ Utility Layer
│   ├── SupabaseHelper.java             # Client Supabase (CRUD + storage)
│   ├── NotificationHelper.java         # Helper notifikasi lokal (SharedPreferences)
│   ├── CaptureActivityPortrait.java    # ZXing capture helper
│   ├── ScannerAnimations.java          # Utility animasi scanner
│   └── ScannerCornersView.java         # Custom view corner brackets QR
│
└── 📦 Model Layer
    ├── Certificate.java                # Model sertifikat
    ├── SignatureRequest.java           # Model pengajuan TTD
    └── Notification.java               # Model notifikasi
```

## Alur Penggunaan

### Mahasiswa
1. Login dengan **NRP** → Pilih role Mahasiswa
2. Dashboard menampilkan statistik, achievement, dan tips keamanan
3. **Ajukan TTD** → Isi identitas → Upload PDF → Pilih dosen → Kirim
4. Pantau status di tab **Dokumen** (filter: Semua/Menunggu/Tervalidasi/Ditolak)
5. **Verifikasi dokumen** via tombol QR di navbar atau search bar

### Dosen / Admin
1. Login dengan **NIP** → Pilih role Dosen
2. Buka **Portal Dosen** → Lihat antrean pengajuan
3. **Setujui** → QR Code di-stamp ke PDF otomatis + upload ulang
4. **Tolak** → Dokumen ditandai REJECTED
5. **Cabut** dokumen yang sudah VALID jika diperlukan

## Build & Run

```bash
# Clone repository
git clone <repo-url>
cd PensCert

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

Buka project di **Android Studio** dan jalankan langsung ke emulator atau device.

## Konfigurasi Supabase

Aplikasi terhubung ke Supabase untuk database dan storage. Konfigurasi ada di `SupabaseHelper.java`:

| Variabel | Keterangan |
|---|---|
| `SUPABASE_URL` | URL project Supabase |
| `SUPABASE_KEY` | Service role key dari Dashboard → Settings → API |

### Skema Tabel `certificates`

| Kolom | Tipe | Keterangan |
|---|---|---|
| `id` | uuid | Primary key |
| `certificate_number` | text | Nomor sertifikat |
| `participant_name` | text | Nama pemilik |
| `participant_role` | text | Role (Mahasiswa/Dosen) |
| `event_name` | text | Jenis dokumen/kegiatan |
| `issued_at` | timestamp | Tanggal penerbitan |
| `status` | text | VALID / PENDING / REJECTED / REVOKED |
| `verification_status` | text | Status verifikasi |
| `pdf_url` | text | URL file PDF di Supabase Storage |
| `sha256_hash` | text | Hash SHA-256 dokumen |
| `rsa_signature` | text | Tanda tangan digital RSA |
| `qr_token` | text | Token QR Code |
| `target_signer` | text | Dosen tujuan TTD |
| `subject_name` | text | Mata kuliah |
| `subject_date` | text | Tanggal mata kuliah |

## Requirements

- **Android** 7.0 (API 24) atau lebih tinggi
- **Koneksi internet** aktif (untuk Supabase)
- **Kamera** (untuk fitur QR Scanner)
- **Penyimpanan** (untuk upload/download PDF)

## Lisensi

© 2024–2026 PENS — Workshop Pemrograman 2
