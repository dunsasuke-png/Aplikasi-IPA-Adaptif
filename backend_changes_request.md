# 📋 Permintaan Perubahan Backend — Sistem Materi Berbasis Tingkat

> **Dibuat:** 17 Mei 2026 (Diperbarui: 17 Mei 2026 — versi final)
> **Dari:** Tim Frontend Mobile Android
> **Prioritas:** 🔴 Tinggi

---

## 🎯 Ringkasan Singkat

Sistem Materi diubah dari **urutan angka 1–24** menjadi **field `tingkat`** (`mudah` / `sedang` / `sulit`), **sama persis dengan sistem Soal yang sudah berjalan**. Tanpa perubahan ini, materi dari level Sulit bisa muncul di halaman Mudah siswa.

---

## 🗄️ 1. Perubahan Database

### Tambah kolom `tingkat` di tabel `materi`

```sql
ALTER TABLE materi
ADD COLUMN tingkat ENUM('mudah', 'sedang', 'sulit') NOT NULL DEFAULT 'mudah';
```

### Migrasi data lama (jalankan sekali)

```sql
UPDATE materi SET tingkat = 'mudah'  WHERE urutan BETWEEN 1  AND 8;
UPDATE materi SET tingkat = 'sedang' WHERE urutan BETWEEN 9  AND 16;
UPDATE materi SET tingkat = 'sulit'  WHERE urutan >= 17;
```

> ✅ Field `urutan` tetap dipertahankan (tidak dihapus), hanya ditambah `tingkat`.

---

## 🔌 2. Perubahan Endpoint API

### A. `GET /api/guru/materi` — Tambah filter `?tingkat=`

Endpoint ini digunakan oleh **guru (untuk kelola materi) DAN siswa (untuk membaca materi)**. Keduanya perlu filter tingkat.

| Query Param | Tipe | Wajib | Nilai | Keterangan |
|---|---|---|---|---|
| `tingkat` | string | ❌ Opsional | `mudah` / `sedang` / `sulit` | Jika tidak dikirim → kembalikan semua |
| `page` | int | ❌ | — | Default: 1 |
| `limit` | int | ❌ | — | Default: 100 |

**Contoh request:**
```
GET /api/guru/materi?tingkat=mudah&limit=100
GET /api/guru/materi?tingkat=sedang
GET /api/guru/materi                          ← tanpa filter = semua materi
```

**Contoh response (struktur tidak berubah, tambah field `tingkat`):**
```json
{
  "success": true,
  "data": {
    "materi": [
      {
        "id": "abc123",
        "nama": "Pengenalan Tumbuhan Obat",
        "deskripsi": "-",
        "manfaat": "Berguna untuk kesehatan",
        "gambar_url": "https://cdn.example.com/foto.jpg",
        "video_url":  "https://cdn.example.com/video.mp4",
        "urutan": 1,
        "tingkat": "mudah",
        "guru_id": "guru-xyz",
        "created_at": "2026-05-17T09:00:00.000Z",
        "updated_at": "2026-05-17T09:00:00.000Z"
      }
    ],
    "pagination": {
      "total": 8,
      "page": 1,
      "limit": 100,
      "totalPages": 1
    }
  }
}
```

> ⚠️ **Wajib:** Field `"tingkat"` harus **selalu ada** di setiap objek materi pada response. Jika hilang/null, aplikasi tidak bisa memfilter dengan benar.

---

### B. `POST /api/guru/materi` — Tambah field `tingkat` di body

**Request body sebelumnya:**
```json
{
  "nama": "...",
  "deskripsi": "-",
  "manfaat": "...",
  "gambar_url": "https://...",
  "video_url": "https://...",
  "urutan": 3
}
```

**Request body sesudah:**
```json
{
  "nama": "...",
  "deskripsi": "-",
  "manfaat": "...",
  "gambar_url": "https://...",
  "video_url": "https://...",
  "urutan": 3,
  "tingkat": "mudah"
}
```

| Field | Tipe | Wajib | Nilai Valid |
|---|---|---|---|
| `tingkat` | string | ✅ | `mudah`, `sedang`, `sulit` |

---

### C. `PUT /api/guru/materi/:id` — Tambah field `tingkat` di body

```json
{
  "nama": "...",
  "manfaat": "...",
  "tingkat": "sedang"
}
```

| Field | Tipe | Wajib | Keterangan |
|---|---|---|---|
| `tingkat` | string | ❌ Opsional | Jika dikirim, update level materi |

---

## 📊 3. Ringkasan Perubahan

| No | Komponen | Jenis | Detail |
|---|---|---|---|
| 1 | Database `materi` | Tambah kolom | `tingkat ENUM('mudah','sedang','sulit') DEFAULT 'mudah'` |
| 2 | Database `materi` | Migrasi data | Set tingkat berdasarkan urutan lama |
| 3 | `GET /api/guru/materi` | Tambah param | `?tingkat=mudah\|sedang\|sulit` (opsional, dipakai guru & siswa) |
| 4 | `POST /api/guru/materi` | Tambah field body | `tingkat: string` (wajib) |
| 5 | `PUT /api/guru/materi/:id` | Tambah field body | `tingkat: string` (opsional) |
| 6 | Semua response materi | Tambah field | Sertakan `"tingkat"` di setiap objek materi |

---

## ✅ 4. Checklist Verifikasi

Setelah perubahan selesai, mohon centang satu per satu:

**Database:**
- [ ] Kolom `tingkat` berhasil ditambahkan di tabel `materi`
- [ ] Data lama sudah dimigrasi (1-8=mudah, 9-16=sedang, 17+=sulit)

**GET (Filter):**
- [ ] `GET /api/guru/materi` tanpa filter → kembalikan **semua** materi
- [ ] `GET /api/guru/materi?tingkat=mudah` → **hanya** materi mudah
- [ ] `GET /api/guru/materi?tingkat=sedang` → **hanya** materi sedang
- [ ] `GET /api/guru/materi?tingkat=sulit` → **hanya** materi sulit

**POST:**
- [ ] `POST` dengan `{ "tingkat": "sedang" }` → tersimpan dengan benar di DB

**PUT:**
- [ ] `PUT` dengan `{ "tingkat": "sulit" }` → tingkat berhasil diperbarui

**Response:**
- [ ] Setiap objek materi di response **selalu punya field `"tingkat"`** (tidak null, tidak missing)

---

## 🔗 5. Referensi — Sistem Soal (Sudah Berjalan ✅)

Cukup terapkan logika **yang sama persis** ke Materi:

```
# Yang sudah ada di Soal (tinggal salin ke Materi):
GET  /api/guru/soal?tingkat=mudah       → filter server-side ✅
POST /api/guru/soal { tingkat: "mudah"} → simpan dengan tingkat ✅
PUT  /api/guru/soal/:id { tingkat: ... }→ update tingkat ✅
Response soal selalu punya field tingkat ✅
```

---

## 📱 6. Catatan Khusus Mobile

Aplikasi mobile siswa memanggil **endpoint yang sama** (`/api/guru/materi`) dengan filter `?tingkat=` untuk mengambil materi sesuai level siswa. Contoh:

```
# Siswa membuka halaman Materi Mudah:
GET /api/guru/materi?tingkat=mudah

# Siswa pindah ke Materi Sedang:
GET /api/guru/materi?tingkat=sedang
```

Jika filter `?tingkat=` tidak didukung, **semua materi dari semua level akan muncul di halaman yang salah** (materi Sulit bisa muncul di halaman Mudah siswa).

---

> 📄 Dokumen ini dibuat oleh tim frontend pada **17 Mei 2026**.
> Pertanyaan → hubungi tim mobile Android.
