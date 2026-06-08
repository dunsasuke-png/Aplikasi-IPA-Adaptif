# Update Algoritma Fuzzy Mamdani (Versi Terbaru)

## Ringkasan Perubahan

Sistem evaluasi adaptif menggunakan **Fuzzy Mamdani** dengan:

- 3 variabel input:
  - Ketepatan Jawaban (0–100)
  - Kecepatan Pengerjaan (0–1000 detik)
  - Tingkat Kesulitan Sebelumnya (0–100)
- 1 variabel output:
  - Tingkat Kesulitan Soal Selanjutnya (0–100)

Setiap sesi evaluasi terdiri dari **20 soal**.

---

## Variabel dan Membership Function

### 1. Ketepatan Jawaban

| Label | Parameter | Bentuk |
|---------|---------|---------|
| Rendah | [0, 0, 40, 50] | Trapesium |
| Sedang | [40, 60, 80] | Segitiga |
| Tinggi | [70, 80, 100, 100] | Trapesium |

### 2. Kecepatan Pengerjaan

| Label | Parameter | Bentuk |
|---------|---------|---------|
| Cepat | [0, 0, 360, 480] | Trapesium |
| Sedang | [360, 480, 780] | Segitiga |
| Lambat | [480, 780, 1000, 1000] | Trapesium |

### 3. Tingkat Kesulitan Sebelumnya

| Label | Parameter | Bentuk |
|---------|---------|---------|
| Mudah | [0, 0, 30, 40] | Trapesium |
| Sedang | [30, 50, 70] | Segitiga |
| Sulit | [60, 70, 100, 100] | Trapesium |

### 4. Output Tingkat Kesulitan

| Label | Parameter | Bentuk |
|---------|---------|---------|
| Mudah | [0, 0, 30, 40] | Trapesium |
| Sedang | [30, 50, 70] | Segitiga |
| Sulit | [60, 70, 100, 100] | Trapesium |

---

## Rule Base

### Rule Pre-test (9 Rule)

Digunakan ketika siswa belum memiliki riwayat pengerjaan.

| Ketepatan | Kecepatan | Output |
|------------|------------|----------|
| Tinggi | Cepat | Sulit |
| Tinggi | Sedang | Sulit |
| Tinggi | Lambat | Sedang |
| Sedang | Cepat | Sedang |
| Sedang | Sedang | Sedang |
| Sedang | Lambat | Mudah |
| Rendah | Cepat | Mudah |
| Rendah | Sedang | Mudah |
| Rendah | Lambat | Mudah |

### Rule Adaptif (27 Rule)

Menggunakan seluruh kombinasi:

3 Ketepatan × 3 Kecepatan × 3 Kesulitan Sebelumnya = 27 Rule

Kesulitan sebelumnya menjadi variabel aktif pada sesi lanjutan sehingga tingkat kesulitan soal dapat naik, turun, atau dipertahankan berdasarkan performa siswa.
No	Ketepatan  Jawaban (nilai)	Kecepatan Pengerjaan (waktu)	Tingkat Kesulitan Sebelumnya	Tingkat Kesulitan (Output)

1	Tinggi	Cepat	Mudah	Sedang
2	Tinggi	Sedang	Mudah	Sedang
3	Tinggi	Lambat	Mudah	Mudah
4	Sedang	Cepat	Mudah	Mudah
5	Sedang	Sedang	Mudah	Mudah
6	Sedang	Lambat	Mudah	Mudah
7	Rendah	Cepat	Mudah	Mudah
8	Rendah	Sedang	Mudah	Mudah
9	Rendah	Lambat	Mudah	Mudah
10	Tinggi	Cepat	Sedang	Sulit
11	Tinggi	Sedang	Sedang	Sulit
12	Tinggi	Lambat	Sedang	Sedang
13	Sedang	Cepat	Sedang	Sedang
14	Sedang	Sedang	Sedang	Sedang
15	Sedang	Lambat	Sedang	Mudah
16	Rendah	Cepat	Sedang	Mudah
17	Rendah	Sedang	Sedang	Mudah
18	Rendah	Lambat	Sedang	Mudah
19	Tinggi	Cepat	Sulit	Sulit
20	Tinggi	Sedang	Sulit	Sulit
21	Tinggi	Lambat	Sulit	Sulit
22	Sedang	Cepat	Sulit	Sulit
23	Sedang	Sedang	Sulit	Sedang
24	Sedang	Lambat	Sulit	Sedang
25	Rendah	Cepat	Sulit	Sedang
26	Rendah	Sedang	Sulit	Sedang
27	Rendah	Lambat	Sulit	Mudah

---

## Alur Perhitungan

### Tahap Pre-test

Input:
- Ketepatan = 60
- Kecepatan = 600 detik

Hasil fuzzifikasi:
- Ketepatan Sedang = 1
- Kecepatan Sedang = 0.6
- Kecepatan Lambat = 0.4

Rule aktif:
- R5 → Sedang (0.6)
- R6 → Mudah (0.4)

Agregasi:
- Mudah = 0.4
- Sedang = 0.6
- Sulit = 0

Defuzzifikasi:
- Metode Centroid (Center of Area)

Output crisp disimpan sebagai nilai "Tingkat Kesulitan Sebelumnya" untuk sesi berikutnya.

---

### Tahap Adaptif

Input:
- Ketepatan = 85
- Kecepatan = 300 detik
- Tingkat Kesulitan Sebelumnya = 35

Hasil fuzzifikasi:
- Ketepatan Tinggi = 1
- Kecepatan Cepat = 1
- Kesulitan Sebelumnya Mudah = 0.5
- Kesulitan Sebelumnya Sedang = 0.25

Rule aktif:
- R1 → Sedang
- R10 → Sulit

Agregasi:
- Mudah = 0
- Sedang = 0.5
- Sulit = 0.25

Defuzzifikasi:
- Metode Centroid (Center of Area)

Hasil akhir digunakan untuk menentukan tingkat kesulitan soal pada sesi berikutnya.

---

## Karakteristik Soal

### Mudah
- Pengenalan dasar melalui gambar
- Identifikasi nama tumbuhan
- Identifikasi manfaat umum tumbuhan

### Sedang
- Pemahaman fungsi tumbuhan yang lebih spesifik
- Penerapan dalam kehidupan sehari-hari

### Sulit
- Analisis manfaat tumbuhan
- Pemahaman hasil olahan tumbuhan
- Soal dengan tingkat kompleksitas lebih tinggi

---

## Catatan Implementasi

1. Sistem menggunakan metode Fuzzy Mamdani.
2. Operator inferensi menggunakan MIN.
3. Operator agregasi menggunakan MAX.
4. Defuzzifikasi menggunakan Centroid.
5. Membership function menggunakan kombinasi kurva trapesium dan segitiga dengan overlap.
6. Sesi pertama menggunakan 9 rule pre-test.
7. Sesi lanjutan menggunakan 27 rule adaptif.
