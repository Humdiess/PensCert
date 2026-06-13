# Release Notes — PENS Cert

---

## v1.0.0 — Initial Release

**Release Date:** June 2026
**Version Code:** 1
**Target SDK:** Android 16 (API 36)

---

### ✨ New Features

#### Home — Dashboard
- **Time-based Greeting** — Sapaan otomatis sesuai waktu: Pagi, Siang, Sore, Malam
- **Quick Search Bar** — Cari dokumen berdasarkan ID langsung dari halaman utama
- **Real-time Statistics** — Kartu statistik: Total Dokumen, Tervalidasi, Tertunda
- **Verified Percentage** — Persentase dokumen yang berhasil diverifikasi
- **Last Activity Info** — Informasi waktu verifikasi terakhir
- **Security Tips Carousel** — Tips keamanan berputar otomatis setiap 5 detik
- **Panduan Penggunaan** — Dialog panduan lengkap cara menggunakan aplikasi
- **Achievement System** — Gamifikasi 6 level berdasarkan jumlah dokumen terverifikasi:
  - 🎯 Mulai Perjalananmu (0 dokumen)
  - 🏆 Pemula Digital (1–4 dokumen)
  - 🏅 Aktif Verifikasi (5–9 dokumen)
  - ⭐ Verifier Handal (10–19 dokumen)
  - 💎 Ahli Verifikasi (20–49 dokumen)
  - 👑 Master Dokumen (50+ dokumen)
- **Pending Shortcut Card** — Shortcut cepat ke daftar dokumen tertunda dengan badge count

#### Documents
- **Tab Filter** — Semua, Menunggu, Tervalidasi, Ditolak
- **Sort Toggle** — Urutkan berdasarkan terbaru/terlama
- **Document Count** — Tampilan jumlah dokumen yang sedang ditampilkan
- **Search Bar** — Pencarian dokumen berdasarkan nama/ID

#### Notifications
- **Filter Chips** — 4 filter: Semua, Disetujui, Ditolak, Permintaan
- **Active Chip Indicator** — Visual feedback chip aktif (warna primer) vs nonaktif
- **Clear Old Notifications** — Hapus notifikasi lama sekaligus
- **Empty State** — Ilustrasi dan pesan saat tidak ada notifikasi

#### Profile
- **User Info Card** — Nama, NRP/NIP, role dengan avatar
- **Security Status** — Status keamanan akun (Aktif + checklist)
- **Share App** — Bagikan aplikasi via native Android share sheet
- **Help & Support** — Dialog bantuan dengan kontak support (email, WhatsApp, GitHub)
- **Clear Cache** — Bersihkan cache aplikasi tanpa menghapus data akun
- **Logout** — Konfirmasi dialog sebelum keluar

#### QR Scanner
- **Custom Scanner UI** — Animasi scan line, corner brackets, flash toggle
- **Portrait-only Mode** — Scanner terkunci dalam orientasi portrait
- **Auto-detect QR** — Deteksi QR otomatis dari kamera
- **Manual Input Fallback** — Input ID manual jika QR tidak terbaca

#### Admin / Dosen
- **Admin Panel** — Portal admin untuk manajemen pengajuan
- **Approval Queue** — Daftar pengajuan menunggu persetujuan
- **QR Stamping** — QR Code otomatis di-stamp ke PDF saat disetujui
- **Document Revocation** — Cabut dokumen yang sudah VALID
- **Certificate Issuance** — Penerbitan sertifikat digital 3 langkah

#### Authentication
- **Dual-role Login** — Login sebagai Mahasiswa (NRP) atau Dosen (NIP)
- **Onboarding Slider** — Intro 3 halaman sebelum login
- **Keyboard-aware Forms** — Form tidak tertutup keyboard saat mengetik

---

### 🛠️ Technical Details

| Item | Detail |
|---|---|
| Language | Java 11 |
| Min SDK | Android 7.0 (API 24) |
| Target SDK | Android 16 (API 36) |
| Build | Gradle 9.1 + Version Catalog |
| Database | Supabase (PostgreSQL REST API) |
| Storage | Supabase Storage |
| QR Engine | ZXing Core 3.5.3 + Embedded 4.3.0 |
| PDF | iTextG 5.5.10 |
| HTTP | OkHttp 4.12.0 |
| UI | Material Design 3 (1.14.0) |

### 📱 Supported Devices
- Semua perangkat Android 7.0 (API 24) ke atas
- Memerlukan kamera untuk fitur QR Scanner
- Dioptimalkan untuk layout portrait

---

### 🐛 Known Issues
- Notifikasi hanya tersimpan lokal (SharedPreferences), belum push notification
- Belum ada fitur offline mode, koneksi internet wajib untuk verifikasi
- Dark mode belum diimplementasi sepenuhnya

---

### 📋 Changelog Summary

```
[ADDED] Home: Time-based greeting, achievement system, quick search
[ADDED] Home: Security tips carousel, panduan dialog, verified stats
[ADDED] Home: Pending shortcut card with badge count
[ADDED] Documents: Sort toggle (terbaru/terlama), document count
[ADDED] Notifications: 4 filter chips (Semua/Disetujui/Ditolak/Permintaan)
[ADDED] Notifications: Clear old notifications button
[ADDED] Profile: Share App via native share sheet
[ADDED] Profile: Help & Support dialog with contact info
[ADDED] Profile: Clear Cache without deleting account data
[ADDED] Profile: Security status card
[ADDED] Login: adjustResize for keyboard-aware forms
[FIXED] FAB QR z-order: rendered on top of bottom navbar
[FIXED] FAB positioning: sejajar (level) with navbar
[FIXED] Home bottom content cutoff behind navbar
[FIXED] AndroidManifest exported attributes for Android 12+
[FIXED] SupabaseHelper: RSA verification, PDF download path, query ordering
[UPDATED] Deprecated API calls migrated to modern equivalents
[UPDATED] NIM references changed to NRP throughout
```

---

© 2024–2026 PENS — Workshop Pemrograman 2
