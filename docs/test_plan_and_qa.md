# Test Plan & QA — Beacon

Dokumen ini memetakan setiap user story ke kasus uji **positif** dan **negatif** (US7). Status awal semua kasus: **Belum dijalankan** (belum ada implementasi).

**Legenda:** ✅ lulus · ❌ gagal · ⏭ tidak dijalankan · ⏳ belum dijalankan

---

## 1. Test Cases Manual (Emulator)

Prasyarat umum: emulator/perangkat Android 7.0+, `NEWS_API_KEY` valid di `local.properties` kecuali dinyatakan lain, internet aktif kecuali dinyatakan lain.

**Respons tiruan (mock):** kasus bertanda 🧩 (TC-10, 11, 15, 16, 17, 18, 44, 50) tidak bisa dipicu dengan API asli. Jalankan MockWebServer/server tiruan di host dan set `NEWS_API_BASE_URL=http://10.0.2.2:<port>/v2/` di `local.properties` (hanya build debug; emulator mengakses host lewat `10.0.2.2`; cleartext ke host ini diizinkan hanya oleh network security config debug). Skrip respons per kasus disimpan di `docs/mock/` saat implementasi.

### US1 — Kategori

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-01 | Positif | Buka aplikasi | Tujuh kategori tampil: Business, Entertainment, General, Health, Science, Sports, Technology | ⏳ |
| TC-02 | Positif | Tap "Technology" | `SourcesScreen` terbuka dengan judul Technology | ⏳ |
| TC-03 | Negatif | Aktifkan mode pesawat (dengan atau tanpa cache), buka aplikasi | Daftar kategori tetap tampil penuh (statis) | ⏳ |

### US2 — Sumber per Kategori

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-04 | Positif | Tap kategori | Loading lalu daftar sumber (nama, deskripsi, label bahasa/negara) | ⏳ |
| TC-05 | Positif | Kembali lalu masuk kategori yang sama | Daftar tampil dari Room tanpa panggilan API baru (TTL sumber 24 jam) | ⏳ |
| TC-06 | Positif | Putar layar | Daftar dan posisi scroll bertahan, tanpa panggilan API baru | ⏳ |
| TC-07 | Negatif | Mode pesawat, tap kategori yang belum pernah dimuat (tanpa cache) | Layar error "No internet connection…" + Retry | ⏳ |
| TC-08 | Negatif | Dari TC-07 matikan mode pesawat, tap Retry | Daftar sumber termuat | ⏳ |
| TC-09 | Negatif | `NEWS_API_KEY` kosong/salah, tap kategori | Pesan "API key is missing or invalid", tanpa Retry | ⏳ |
| TC-10 🧩 | Negatif | Kuota habis (`rateLimited`/`apiKeyExhausted`) | Pesan "Daily request limit reached…", tanpa Retry | ⏳ |
| TC-11 🧩 | Negatif | Kategori tanpa sumber (respons mock) | "No sources found." | ⏳ |

### US3 — Artikel per Sumber

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-12 | Positif | Tap sumber | Loading lalu kartu artikel (gambar, judul, sumber, tanggal, deskripsi) | ⏳ |
| TC-13 | Positif | Tap artikel | `ArticleWebScreen` terbuka | ⏳ |
| TC-14 | Negatif | Mode pesawat, tap sumber yang belum pernah dimuat (tanpa cache) | Layar error + Retry | ⏳ |
| TC-15 🧩 | Negatif | Sumber tidak ada lagi (`sourceDoesNotExist`) | "This source is no longer available." | ⏳ |
| TC-16 🧩 | Negatif | Sumber tanpa artikel | "No articles found." | ⏳ |
| TC-17 🧩 | Edge | Artikel dengan `urlToImage` null | Placeholder gambar, tidak crash | ⏳ |
| TC-18 🧩 | Edge | Artikel tanpa `url` / judul "[Removed]" | Tidak ditampilkan | ⏳ |

### US4 — Detail Artikel (WebView)

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-19 | Positif | Buka artikel `https://` | Progress bar lalu halaman termuat | ⏳ |
| TC-20 | Positif | Tap tautan di dalam halaman, tekan Back | Mundur di riwayat web; Back berikutnya kembali ke daftar | ⏳ |
| TC-21 | Negatif | Mode pesawat lalu buka artikel | Error + Retry + Open in browser | ⏳ |
| TC-22 | Negatif | Artikel dengan `url` kosong/tidak valid | Layar error tanpa WebView | ⏳ |
| TC-23 | Edge | Artikel `http://` | Dicoba sebagai `https://`; bila gagal error + Open in browser | ⏳ |
| TC-24 | Keamanan | Halaman mencoba membuka `intent://` / `file://` | Diblokir | ⏳ |
| TC-25 | Positif | Tap "Open in browser" | Browser eksternal membuka URL | ⏳ |

### US5 — Pencarian

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-26 | Positif | Sumber: ketik "bbc" | Daftar memfilter instan (nama/deskripsi), tanpa API | ⏳ |
| TC-27 | Positif | Sumber: ketik "BBC" huruf besar | Hasil sama (tidak peka huruf besar-kecil) | ⏳ |
| TC-28 | Negatif | Sumber: ketik "zzzz" | "No sources match "zzzz"" | ⏳ |
| TC-29 | Positif | Sumber: tap × | Query kosong, daftar penuh | ⏳ |
| TC-30 | Positif | Artikel: ketik "election" | Setelah ±500 ms daftar diganti hasil pencarian | ⏳ |
| TC-31 | Edge | Artikel: ketik 1 karakter | Tidak ada panggilan API; daftar default | ⏳ |
| TC-32 | Edge | Artikel: ketik cepat lalu ubah | Hanya hasil query terakhir tampil | ⏳ |
| TC-33 | Negatif | Artikel: query tanpa hasil | "No articles match "…"" | ⏳ |
| TC-34 | Negatif | Artikel: mode pesawat saat mencari | Pesan error + Retry; teks pencarian tetap | ⏳ |

### US6 — Endless Scrolling

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-35 | Positif | Sumber: scroll melewati item ke-20 | 20 item berikutnya muncul tanpa panggilan API | ⏳ |
| TC-36 | Positif | Artikel: scroll melewati item ke-20 | Halaman 2 dimuat otomatis, posisi scroll tidak melompat | ⏳ |
| TC-37 | Negatif | Artikel: mode pesawat saat scroll | Item sebelumnya tetap (dari Room); footer "Failed to load more — Retry" | ⏳ |
| TC-38 | Negatif | Dari TC-37 nyalakan internet, tap Retry | Halaman berikutnya masuk | ⏳ |
| TC-39 | Edge | Artikel: scroll sampai habis | Paging berhenti tanpa error (akhir daftar / batas hasil) | ⏳ |

### US7 — Lintas Fitur

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-40 | Negatif | Keluar layar saat permintaan berjalan | Tidak ada crash, tidak ada pesan error muncul | ⏳ |
| TC-41 | Keamanan | Periksa logcat saat memakai aplikasi | API key tidak tercatat | ⏳ |
| TC-42 | Rilis | Build release | R8 berhasil; layar utama berfungsi; `mapping.txt` memuat class teracak | ⏳ |
| TC-43 | Positif | Sumber: ketik "bbc", putar layar | Query "bbc" dan hasil filter bertahan (AC 5.3) | ⏳ |
| TC-44 🧩 | Edge | Artikel: respons mock berisi `url` sama di halaman 1 dan 2, scroll | Tidak crash; tidak ada kunci ganda (AC 6.3) | ⏳ |
| TC-45 | Keamanan | Picu error (offline, kunci salah, kuota) di tiap layar, baca pesan di layar dan logcat | Pesan tidak memuat API key maupun URL permintaan (AC 7.3) | ⏳ |

### US8 — Offline-First (FR-08, ADR-013)

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-46 | Positif | Online: buka kategori lalu sumber. Aktifkan mode pesawat, tutup aplikasi, buka lagi, masuk kategori yang sama | Daftar sumber tampil dari cache + banner "Offline, menampilkan data tersimpan"; tanpa layar error | ⏳ |
| TC-47 | Positif | Online: buka artikel sebuah sumber. Mode pesawat, keluar, masuk lagi | Artikel tampil dari cache + banner offline; gambar yang pernah dimuat tetap tampil | ⏳ |
| TC-48 | Edge | Cache sumber lebih tua dari 24 jam (majukan jam perangkat > 24 jam), online, buka kategori | Panggilan API baru; daftar diperbarui | ⏳ |
| TC-49 | Edge | Cache artikel lebih tua dari 1 jam (majukan jam > 1 jam), online, buka sumber | Panggilan API baru; daftar diganti tanpa duplikat | ⏳ |
| TC-50 🧩 | Negatif | Ada cache, respons mock `rateLimited` saat refresh | Cache tetap tampil + banner kuota; tidak ada *auto-retry* | ⏳ |
| TC-51 | Positif | Dari TC-46 matikan mode pesawat, tap Retry di banner | Banner hilang; daftar diperbarui tanpa melompat ke atas | ⏳ |

---

## 2. Matriks Unit Test (Rencana)

Format: JUnit 5, `@ParameterizedTest` dengan `argumentSet` (nama deskriptif), Turbine untuk Flow/StateFlow, `asSnapshot` untuk paging.

| Modul | Kelas test | Skenario utama |
|---|---|---|
| `:core:common` | `NewsFormattersTest` | Tanggal ISO → `dd MMM yyyy`; null/kosong/tidak valid; tanggal mustahil |
| `:core:common` | `NetworkResultTest` | `map` pada Success dan Error |
| `:core:network` | `ApiKeyInterceptorTest` | Header `X-Api-Key` dan `Accept` |
| `:core:network` | `ErrorParserTest` | Semua `code` → `NewsError`; body bukan JSON; body kosong; kode tak dikenal; fallback status HTTP |
| `:core:network` | `DtoParsingTest` | Field null, kunci tak dikenal, `{}` |
| `:domain` | `GetSourcesUseCaseTest`, `GetArticlesUseCaseTest` | Argumen diteruskan; flow diteruskan (Turbine) |
| `:data` | `MappersTest` | Sumber/artikel: field null, id kosong dibuang, `[Removed]`, tanpa `url`, tanggal |
| `:data` | `SourcesRemoteMediatorTest` | `initialize()` SKIP bila < 24 jam, LAUNCH bila lebih; REFRESH mengganti baris kategori dalam satu transaksi dan `endOfPaginationReached`; gagal tidak menghapus data; kategori berbeda terpisah |
| `:data` | `ArticlesRemoteMediatorTest` | TTL 1 jam; REFRESH hapus `(sourceId, queryKey)`; APPEND memakai `nextPage`; PREPEND berakhir; `totalResults`, halaman kosong, batas hasil; `IOException`, JSON rusak, cancellation; `queryKey` pencarian terpisah |
| `:data` | `CacheRetentionTest` | Artikel > 7 hari dan kelebihan baris dibuang; `remote_keys` tidak yatim |
| `:core:database` (androidTest, JUnit 4) | `ArticleDaoTest`, `SourceDaoTest` | Urutan `rowId`, `LIKE` tidak peka huruf besar-kecil, indeks unik `(sourceId, queryKey, url)`, hapus per kunci |
| `:data` | `NewsRepositoryImplTest` | Sukses; error HTTP per kode; offline; JSON rusak; cancellation di-*rethrow* |
| `:feature:sources` | `SourcesViewModelTest` | Query berubah menghasilkan flow baru; query dipertahankan |
| `:feature:articles` | `ArticlesViewModelTest` | Debounce 500 ms; < 2 karakter; `flatMapLatest` membatalkan; retry |
| `:core:common` | `NewsExceptionTest` | `toNewsError()`: `NewsException` → tipe asli; exception lain → `Unknown` |
| `:feature:articleweb` | `WebUrlPolicyTest` | `url` kosong/tidak valid/`file://`/`intent://` ditolak (AC 4.3, 4.5); `http://` di-upgrade ke `https://` (AC 4.4); `https://` tetap |
| `:feature:articles` | `ArticlesViewModelTest` (tambahan) | Query turun dari ≥ 2 ke < 2 karakter kembali ke daftar default (AC 5.8) |
| `:app` | `KoinModulesTest` | Graf DI lengkap |

Compose UI test dan instrumented test (JUnit 4) ditambahkan setelah fitur stabil.

---

## 3. Catatan Eksekusi & Defect Log

| Tanggal | Build | Penguji | Ringkasan | Defect |
|---|---|---|---|---|
| – | – | – | Belum ada eksekusi | – |

### Ringkasan (diisi setelah eksekusi)
| | Jumlah |
|---|---|
| Test case manual | 51 |
| Dijalankan / lulus | – |
| Unit test | – |
| Defect terbuka | – |
