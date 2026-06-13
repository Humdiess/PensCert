# PENS Cert

Aplikasi keamanan dokumen digital untuk lingkungan **Politeknik Elektronika Negeri Surabaya (PENS)**. PENS Cert menyediakan fitur verifikasi dokumen, tanda tangan digital, dan manajemen sertifikat secara aman dan terintegrasi.

## Fitur Utama

- **Verifikasi Dokumen** — Verifikasi keaslian dokumen melalui QR Code atau input ID manual
- **Tanda Tangan Digital** — Pengajuan tanda tangan digital ke dosen dengan alur multi-langkah
- **Manajemen Sertifikat** — Pembuatan, pencabutan, dan pengelolaan sertifikat digital
- **Panel Admin** — Approval/rejection pengajuan, generate QR, dan stamping PDF otomatis
- **Notifikasi** — Sistem notifikasi lokal untuk tracking status dokumen
- **QR Scanner** — Scanner QR modern dengan animasi premium dan kontrol flash

## Tech Stack

| Komponen | Teknologi |
|---|---|
| Language | Java |
| Platform | Android (minSdk 24) |
| UI | Material Design 3 + Custom Styles |
| Database | Supabase (PostgreSQL + REST API) |
| Storage | Supabase Storage |
| Networking | OkHttp 4.12 |
| Serialization | Gson 2.11 |
| QR Code | ZXing 3.5.3 + zxing-android-embedded 4.3 |
| PDF Processing | iTextG 5.5.10 |
| Build | Gradle 9.1 (Version Catalog) |

## Arsitektur

```
app/src/main/java/com/example/penscert/
├── LoginActivity.java            # Autentikasi (NRP/NIP + Role)
├── IntroActivity.java            # Onboarding slider
├── MainActivity.java             # Bottom nav + fragment host + QR FAB
├── HomeFragment.java             # Dashboard, statistik, recent activity
├── DocumentsFragment.java        # Daftar dokumen + filter tab + search
├── NotificationsFragment.java    # Pusat notifikasi
├── ProfileFragment.java          # Profil pengguna + logout
├── DocumentDetailActivity.java   # Detail dokumen + status badge
├── VerificationResultActivity.java # Hasil verifikasi QR/manual
├── VerifyInputActivity.java      # Input ID verifikasi manual
├── RequestSignatureActivity.java # Pengajuan TTD (3-step wizard)
├── SetQrPositionActivity.java    # Drag QR position pada PDF preview
├── MyRequestsActivity.java       # Daftar pengajuan milik user
├── AdminActivity.java            # Panel admin
├── AdminRequestsActivity.java    # Approval queue + QR stamping PDF
├── CreateCertificateActivity.java # Penerbitan sertifikat (3-step)
├── RevocationActivity.java       # Pencabutan dokumen
├── ScanQrActivity.java           # Custom QR scanner (portrait)
├── CaptureActivityPortrait.java  # ZXing capture helper
├── ScannerAnimations.java        # Utility animasi scanner
├── ScannerCornersView.java       # Custom view corner brackets QR
├── SupabaseHelper.java           # Client Supabase (CRUD + storage)
├── Certificate.java              # Model sertifikat
├── SignatureRequest.java         # Model pengajuan TTD
├── Notification.java             # Model notifikasi
└── NotificationHelper.java       # Helper notifikasi lokal (SharedPreferences)
```

## Alur Penggunaan

### Mahasiswa
1. Login dengan NRP → Pilih role Mahasiswa
2. Ajukan TTD → Isi identitas → Upload PDF → Pilih dosen → Kirim
3. Lihat status pengajuan di halaman Dokumen
4. Verifikasi dokumen dengan scan QR atau input ID

### Dosen / Admin
1. Login dengan NIP → Pilih role Dosen
2. Buka Panel Admin → Lihat antrean pengajuan
3. Setujui → QR Code di-stamp ke PDF otomatis + upload ulang
4. Tolak → Dokumen ditandai REJECTED
5. Cabut dokumen yang sudah VALID jika diperlukan

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

Aplikasi ini terhubung ke Supabase untuk database dan storage. Konfigurasi ada di `SupabaseHelper.java`:

- `SUPABASE_URL` — URL project Supabase
- `SUPABASE_KEY` — Service role key dari Supabase Dashboard → Settings → API

### Tabel `certificates`

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | uuid | Primary key |
| certificate_number | text | Nomor sertifikat |
| participant_name | text | Nama pemilik |
| participant_role | text | Role (Mahasiswa/Dosen) |
| event_name | text | Jenis dokumen/kegiatan |
| issued_at | timestamp | Tanggal penerbitan |
| status | text | VALID / PENDING / REJECTED / REVOKED |
| verification_status | text | Status verifikasi |
| pdf_url | text | URL file PDF |
| sha256_hash | text | Hash SHA-256 dokumen |
| rsa_signature | text | Tanda tangan digital |
| qr_token | text | Token QR Code |
| target_signer | text | Dosen tujuan TTD |
| subject_name | text | Mata kuliah (absen) |
| subject_date | text | Tanggal mata kuliah |

## Minimum Requirements

- Android 7.0 (API 24) atau lebih tinggi
- Koneksi internet aktif
- Kamera (untuk fitur QR Scanner)

## Lisensi

© 2024 PENS — Workshop Pemrograman 2
