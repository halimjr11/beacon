# Test Plan & QA — Beacon

Dokumen ini memetakan setiap user story ke kasus uji **positif** dan **negatif** (US7). Status awal semua kasus: **Belum dijalankan** (belum ada implementasi).

**Legenda:** ✅ lulus · ❌ gagal · ⏭ tidak dijalankan · ⏳ belum dijalankan

---

## 1. Test Cases Manual (Emulator)

Prasyarat umum: emulator/perangkat Android 7.0+, `NEWS_API_KEY` valid di `local.properties` kecuali dinyatakan lain, internet aktif kecuali dinyatakan lain.

**Respons tiruan (mock):** kasus bertanda 🧩 (TC-10, 11, 15, 16, 17, 18, 44, 50, 60) tidak bisa dipicu dengan API asli. Jalankan MockWebServer/server tiruan di host dan set `NEWS_API_BASE_URL=http://10.0.2.2:<port>/v2/` di `local.properties` (hanya build debug; emulator mengakses host lewat `10.0.2.2`; cleartext ke host ini diizinkan hanya oleh network security config debug). Server tiruan ada di `docs/mock/mock_server.py` (`python3 docs/mock/mock_server.py`, port 8099; mode lewat `GET /_mode/ok|ratelimited|badkey|error|empty|few`). Bila emulator tidak bisa menjangkau `10.0.2.2` (mis. izin jaringan lokal macOS), pakai `adb reverse tcp:8099 tcp:8099` dan `NEWS_API_BASE_URL=http://localhost:8099/v2/`; config debug mengizinkan cleartext ke `10.0.2.2`, `localhost`, dan `127.0.0.1`.

### US1 — Genres

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-01 | Positif | Buka tab Genres | Tujuh genre tampil: Business, Entertainment, General, Health, Science, Sports, Technology | ✅ |
| TC-02 | Positif | Tap "Technology" | Daftar sumber Technology terbuka (judul Technology, hanya sumber genre itu, tanpa chip kategori) | ✅ |
| TC-02b | Positif | Di daftar sumber genre tap chip "Top headlines"; tekan Back dua kali | Headline genre tampil; Back kembali ke daftar sumber, lalu ke Genres | ⏳ |
| TC-02c | Positif | Di daftar sumber genre tap sebuah sumber | Daftar artikel sumber itu terbuka | ✅ |
| TC-03 | Negatif | Aktifkan mode pesawat (dengan atau tanpa cache), buka tab Genres | Tujuh genre tetap tampil penuh (statis) | ⏳ |

### US2 — Sumber per Kategori

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-04 | Positif | Buka tab Sources (chip General aktif) | Loading lalu daftar sumber (nama, deskripsi, label bahasa/negara) | ✅ |
| TC-05 | Positif | Pindah ke tab lain lalu kembali ke tab Sources dengan chip yang sama | Daftar tampil dari Room tanpa panggilan API baru (TTL sumber 24 jam) | ⏳ |
| TC-06 | Positif | Putar layar | Daftar dan posisi scroll bertahan, tanpa panggilan API baru | ⏳ |
| TC-07 | Negatif | Mode pesawat, pilih chip kategori yang belum pernah dimuat (tanpa cache) | Layar error "No internet connection…" + Retry | ⏳ |
| TC-08 | Negatif | Dari TC-07 matikan mode pesawat, tap Retry | Daftar sumber termuat | ⏳ |
| TC-09 | Negatif | `NEWS_API_KEY` kosong/salah, buka tab Sources | Pesan "API key is missing or invalid", tanpa Retry | ⏳ |
| TC-10 🧩 | Negatif | Kuota habis, tanpa cache, (`rateLimited`/`apiKeyExhausted`) | Pesan "Daily request limit reached…", tanpa Retry | ⏳ |
| TC-11 🧩 | Negatif | Kategori tanpa sumber (respons mock) | "No sources found." | ⏳ |

### US3 — Artikel per Sumber

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-12 | Positif | Tap sumber | Loading lalu kartu artikel (gambar, judul, sumber, tanggal, deskripsi) | ✅ |
| TC-13 | Positif | Tap artikel | `ArticleWebScreen` terbuka langsung (tanpa layar ringkasan) | ✅ |
| TC-14 | Negatif | Mode pesawat, tap sumber yang belum pernah dimuat (tanpa cache) | Layar error + Retry | ✅ |
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
| TC-26 | Positif | Sumber: ketik "bbc" | Daftar memfilter instan (nama/deskripsi), tanpa API | ✅ |
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
| TC-35 | Positif | Sumber: scroll melewati item ke-20 | 20 item berikutnya muncul tanpa panggilan API | ✅ |
| TC-36 | Positif | Artikel: scroll melewati item ke-20 | Halaman 2 dimuat otomatis, posisi scroll tidak melompat | ✅ |
| TC-37 | Negatif | Artikel: mode pesawat saat scroll | Item sebelumnya tetap (dari Room); footer "Failed to load more — Retry" | ⏳ |
| TC-38 | Negatif | Dari TC-37 nyalakan internet, tap Retry | Halaman berikutnya masuk | ⏳ |
| TC-39 | Edge | Artikel: scroll sampai habis | Paging berhenti tanpa error (akhir daftar / batas hasil) | ⏳ |

### US9 — Home & Navigasi (FR-09)

| ID | Tipe | Langkah | Hasil yang diharapkan | Status |
|---|---|---|---|---|
| TC-52 | Positif | Buka aplikasi | Home tampil: carousel 5 headline dengan indikator, lalu bagian Latest | ✅ |
| TC-53 | Positif | Geser carousel, tap salah satu | Indikator mengikuti; `ArticleWebScreen` terbuka untuk artikel itu | ⏳ |
| TC-54 | Edge | Bandingkan carousel dengan Latest | Tidak ada artikel ganda (AC 9.3) | ⏳ |
| TC-55 | Positif | Scroll Home sampai akhir bagian Latest | Halaman berikutnya dimuat otomatis; footer loading/Retry sesuai AC 6.2 | ⏳ |
| TC-56 | Positif | Tarik ke bawah di Home | Indikator refresh; data diperbarui; posisi tidak rusak | ⏳ |
| TC-57 | Positif | Online buka Home, mode pesawat, tutup lalu buka aplikasi | Home tampil dari cache + banner offline (AC 8.2) | ⏳ |
| TC-58 | Navigasi | Dari Genres atau Sources tekan Back | Kembali ke Home; Back sekali lagi keluar aplikasi | ⏳ |
| TC-59 | Navigasi | Buka layar artikel atau detail | Bottom bar tersembunyi; muncul lagi saat kembali ke tab | ⏳ |
| TC-61 | Positif | Biarkan Home terbuka 10 detik | Carousel berpindah sendiri tiap ± 4 detik, kembali ke item pertama setelah item ke-5 (AC 9.5) | ✅ |
| TC-62 | Edge | Sentuh dan geser carousel, lepas | Auto-scroll berhenti saat disentuh dan lanjut ± 4 detik setelah dilepas; tidak berjalan saat aplikasi di latar belakang | ⏳ |
| TC-60 🧩 | Edge | Home dengan respons mock 3 headline | Carousel 3 item, bagian Latest tidak tampil, tanpa crash (AC 9.4) | ⏳ |

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
| TC-50 🧩 | Negatif | Ada cache, respons mock `rateLimited` saat refresh | Cache tetap tampil + banner kuota; tidak ada *auto-retry* | ✅ |
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
| `:data` | `ArticlesRemoteMediatorTest` | Feed Home/genre/sumber → parameter dan kunci yang benar; TTL 1 jam; REFRESH hapus `(sourceId, queryKey)`; APPEND memakai `nextPage`; PREPEND berakhir; `totalResults`, halaman kosong, batas hasil; `IOException`, JSON rusak, cancellation; `queryKey` pencarian terpisah |
| `:data` | `CacheRetentionTest` | Artikel > 7 hari dan kelebihan baris dibuang; `remote_keys` tidak yatim |
| `:core:database` (androidTest, JUnit 4) | `ArticleDaoTest`, `SourceDaoTest` | Urutan `rowId`, `LIKE` tidak peka huruf besar-kecil, indeks unik `(sourceId, queryKey, url)`, hapus per kunci |
| `:data` | `NewsRepositoryImplTest` | Sukses; error HTTP per kode; offline; JSON rusak; cancellation di-*rethrow* |
| `:feature:sources` | `SourcesViewModelTest` | Default General; query di-`trim` dan teks field dipertahankan; ganti chip meminta daftar baru |
| `:feature:home` | `HomeViewModelTest` *(menyusul)* | Meminta feed `Headlines` |
| `:feature:articles` | `ArticlesViewModelTest` | Daftar awal langsung; debounce 500 ms; < 2 karakter kembali ke default; hanya query terakhir diminta |
| `:core:common` | `NewsExceptionTest` | `toNewsError()`: `NewsException` → tipe asli; exception lain → `Unknown` |
| `:feature:articleweb` | `WebUrlPolicyTest` | `url` kosong/tidak valid/`file://`/`intent://` ditolak (AC 4.3, 4.5); `http://` di-upgrade ke `https://` (AC 4.4); `https://` tetap |
| `:feature:articles` | `ArticlesViewModelTest` (tambahan) | Query turun dari ≥ 2 ke < 2 karakter kembali ke daftar default (AC 5.8) |
| `:app` | `KoinModulesTest` | Graf DI lengkap (`verify`) |

### Unit test yang sudah ada (JVM, JUnit 5, 153 tes)
Semua pengujian berparameter memakai `@ParameterizedTest` + `argumentSet` (nama kasus deskriptif): `NewsFormattersTest`, `ErrorParserTest`, `SafeApiCallTest` (semua jenis kegagalan → `NewsError`, pembatalan di-*rethrow*), `FeedParamsTest`, `CacheKeysTest`, `MappersTest`, `WebUrlPolicyTest`, `NewsErrorUiTest`, plus `ArticlesViewModelTest` (7 kombinasi teks pencarian, waktu virtual), `SourcesViewModelTest` (trim query, tiap kategori), kedua `RemoteMediator`, `CacheRetentionTest`, `HomeViewModelTest`, `UseCasesTest`, dan `KoinModulesTest`.

### Instrumented test (`src/androidTest`, JUnit 4 + AndroidX Test + Espresso + Compose test, 134 tes)
Dijalankan dengan `./gradlew :<modul>:connectedDebugAndroidTest` di emulator/perangkat (APK uji memasang dan mencopot app, jadi pasang ulang APK debug sesudahnya). Berparameter memakai `@RunWith(Parameterized::class)`.

| Modul | Kelas | Cakupan |
|---|---|---|
| `:core:database` (24) | `SourceDaoSearchTest` (10 pola LIKE: huruf besar-kecil, nama/deskripsi, wildcard di-escape), `SourceDaoWriteTest`, `ArticleDaoTest` (urutan `rowId`, indeks unik, feed dan query terpisah, hapus per kunci, yatim, `trimTo`), `RemoteKeyDaoTest`, `RemoteKeyRetentionTest` | Query Room asli di SQLite perangkat |
| `:core:ui` (31) | `FullScreenErrorTest` (8 jenis error), `PagingFooterTest`, `CacheBannerTest`, `PagingStateLayoutTest` (7 keadaan offline-first), `ArticleCardTest`, `SearchFieldTest` | Komponen bersama dan pesan FR-07 |
| `:feature:home` (11) | `HomeScreenTest` (3/5/6/20 headline), `HomeLatestAndCarouselTest` (tanpa duplikat, auto-scroll 4 detik, kembali ke item pertama) | AC 9.1–9.5 |
| `:feature:categories` (8) | `GenresScreenTest` (7 genre), `GenresTitleTest` | US1 |
| `:feature:sources` (34) | `SourcesScreenTest` (chip, filter, pesan kosong/tanpa hasil, klik), `CategorySourcesScreenTest` (7 genre × 4: hanya sumber genre itu, klik sumber, back + Top headlines, pencarian dalam genre) | US2, US5 |
| `:feature:articles` (9) | `ArticlesScreenTest`, `ArticlesSearchTest` (5 teks: minimal 2 karakter, trim) | US3, US5 |
| `:feature:articleweb` (7) | `ArticleWebInvalidUrlTest` (7 URL tidak valid) | AC 4.3, 4.5 |
| `:app` (17) | `GenreNavigationTest` (7 genre × 2: buka daftar sumber, bottom bar tersembunyi, "Top headlines", Espresso Back), `TabsTest` | Navigasi nyata |

Belum ada instrumented test untuk `WebView` yang memuat halaman, `RemoteMediator` bersama Room asli, dan alur penuh dengan jaringan tiruan.

---

## 3. Catatan Eksekusi & Defect Log

| Tanggal | Build | Penguji | Ringkasan | Defect |
|---|---|---|---|---|
| 2026-11-18 | debug, emulator Pixel_10a | Claude (sesi pengembangan) | Smoke test dengan server tiruan: TC-01, 02, 04, 12, 13, 50, 52 lulus (screenshot dicek). Dengan NewsAPI asli: Home, Genres→feed Sports, Sources (+ pencarian lokal "news"), artikel→WebView (ESPN, WSJ), rotasi; semuanya berfungsi. Juga di perangkat: TC-14 (offline tanpa cache → error + Retry, pulih setelah online), TC-26, TC-35 (daftar sumber melewati 20 item), TC-36 (halaman 3 artikel termuat saat scroll). Belum dijalankan: sisa kasus. | D-01, D-02 (diperbaiki, lihat bawah); verifikasi API asli, lihat API Spec §0 |

**D-02:** dengan API asli, Home sempat menampilkan "No headlines found." sekitar satu detik sebelum daftar muncul: mediator melaporkan selesai sebelum Room mengirim ulang baris. Diperbaiki di `PagingStateLayout` (daftar kosong baru dipercaya setelah 400 ms; selama itu loading).

**D-01:** pembukaan pertama memanggil API 3 kali (halaman 1-3) karena `initialLoadSize` default Paging = 3 halaman. Diperbaiki: `initialLoadSize = 20`, `prefetchDistance = 5`. Sisa perilaku yang wajar: `RemoteMediator` mengambil satu halaman lebih awal begitu data Room habis, jadi pembukaan pertama = 2 request.

### Ringkasan (diisi setelah eksekusi)
| | Jumlah |
|---|---|
| Test case manual | 64 |
| Dijalankan / lulus | – |
| Unit test | – |
| Defect terbuka | – |
