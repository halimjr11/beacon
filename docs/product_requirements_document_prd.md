# Product Requirements Document (PRD) — Beacon

## 1. Document Control & Overview
* **Product Name:** Beacon *(mercusuar: penunjuk arah di tengah lautan informasi, merepresentasikan aplikasi yang membantu pengguna menemukan berita dari sumber yang tepat)*
* **Package Name:** `com.halimjr11.beacon`
* **Tech Stack:** Kotlin, Jetpack Compose (Material 3), MVVM, Clean Architecture, Coroutines & Flow, Koin 4.2.2, Navigation 3, Paging 3, Room (offline-first, `RemoteMediator`), Retrofit + OkHttp + Gson, Coil 3, Android `WebView`.
* **Target Platform:** Android (Min SDK 24 / Android 7.0, Target SDK 37).
* **Data Source:** [NewsAPI](https://newsapi.org) v2 (`/v2/top-headlines/sources`, `/v2/top-headlines`).
* **Objective:** Aplikasi native untuk menjelajahi berita lewat tiga tab (**Home** berisi highlight, **Genres**, **Sources**) yang bermuara ke **Daftar Artikel → Detail Artikel (web view)**, dilengkapi pencarian sumber dan artikel, *endless scrolling*, serta penanganan kasus positif dan negatif yang menyeluruh.

### 1.1 Asumsi & Keputusan
| # | Keputusan | Alasan |
|---|---|---|
| A1 | Daftar sumber difilter `language=en` secara default (konstanta, mudah diubah). | Tanpa filter, hasil mencampur belasan bahasa. |
| A2 | Artikel diambil dari `top-headlines` (parameter `sources=<id>` untuk sumber, `category=<x>` untuk genre, `country=us` untuk Home), bukan `everything`. | Sesuai alur "berita utama per sumber"; `q` tetap bisa dipakai untuk pencarian. |
| A3 | Detail artikel memakai `WebView` (bukan Custom Tabs). | Sesuai user story 4. |
| A4 | Pencarian artikel memanggil API (`q`) dengan debounce; pencarian sumber memfilter di sisi klien. | API sources tidak punya parameter pencarian. |
| A5 | **Offline-first:** Room adalah sumber kebenaran untuk sumber dan artikel; jaringan hanya mengisi Room lewat `RemoteMediator` (ADR-013). | Kuota 100 request/hari dan kebutuhan membaca daftar saat offline. |
| A7 | Home memakai satu feed `top-headlines?country=us`: 5 item teratas menjadi carousel highlight, sisanya daftar "Latest". Genre dan sumber memakai feed sendiri; semua feed memakai mekanisme cache yang sama (ADR-015). | Satu endpoint untuk Home hemat kuota; `sources` tidak boleh digabung `country`/`category`. |
| A6 | TTL cache: sumber **24 jam**, artikel **1 jam**; retensi artikel 7 hari. | Artikel NewsAPI Developer tertunda 24 jam, jadi TTL pendek tidak memberi data lebih baru. |

### 1.2 Di Luar Cakupan (Out of Scope)
Bookmark/favorit, sinkronisasi latar belakang (WorkManager), notifikasi, login, berbagi artikel, Custom Tabs, endpoint `/v2/everything`, mode gelap khusus per layar, Compose UI test dan instrumented test (**segera ditambahkan** setelah fitur stabil).

---

## 2. User Flow & Navigation Map
```text
Bottom navigation (Home | Genres | Sources); Back dari tab mana pun kembali ke Home

[HomeScreen] ── carousel highlight + daftar "Latest" (pull-to-refresh)
       │  tap artikel
       ▼
[GenresScreen] ── tap genre ──► [ArticlesScreen (genre)] ──┐
                                                          │ tap artikel (url, title)
[SourcesScreen] ── chip kategori + search ── tap sumber ──► [ArticlesScreen (sumber)] ──┤
                                                          ▼
                                                [ArticleWebScreen] (WebView + progress + error/retry)
                                                          │
                                                          └── "Open in browser" ──► Browser eksternal
```
Bottom bar hanya tampil di tiga tab; layar artikel dan detail menyembunyikannya.

---

## 3. User Stories

| # | User story | Dicakup oleh |
|---|---|---|
| US1 | Menampilkan genre berita di menu terpisah. | FR-01 |
| US2 | Menampilkan sumber berita saat kategori dipilih. | FR-02 |
| US3 | Menampilkan artikel saat sumber dipilih. | FR-03 |
| US4 | Menampilkan detail artikel di web view. | FR-04 |
| US5 | Pencarian sumber dan artikel. | FR-05 |
| US6 | *Endless scrolling* di sumber dan artikel. | FR-02, FR-03, FR-06 |
| US8 | Home menampilkan highlight dan headline terbaru. | FR-09 |
| US7 | Kasus positif dan negatif tercakup. | Semua FR, FR-07, [Test Plan](test_plan_and_qa.md) |

---

## 4. Functional Requirements & Acceptance Criteria (AC)

### FR-01: Genres (US1)
* **Deskripsi:** Tab **Genres** menampilkan tujuh kategori resmi NewsAPI: `business`, `entertainment`, `general`, `health`, `science`, `sports`, `technology`.
* **UI/UX Behavior:**
  * `TopAppBar` berjudul **"Genres"**; grid 2 kolom kartu dengan ikon dan nama. Urutan tetap seperti di atas.
  * Genre bersifat **statis** (bukan hasil API), jadi tidak ada loading maupun error.
  * Tap genre membuka `ArticlesScreen` berisi headline genre itu (`top-headlines?category=…`), dengan pencarian dan *endless scrolling* (FR-03, FR-05).
* **Acceptance Criteria:**
  * **AC 1.1 (Positive):** Tujuh genre tampil, dapat diklik, dan membuka daftar artikel genre itu dengan judul nama genre.
  * **AC 1.2 (Offline-safe):** Layar tetap tampil penuh walau perangkat offline.

### FR-02: Sumber Berita per Kategori (US2, US6)
* **Deskripsi:** Menampilkan sumber berita untuk kategori terpilih (`/v2/top-headlines/sources?category=…&language=en`).
* **UI/UX Behavior:**
  * Tab **Sources**: `TopAppBar` "Sources", *search field* (FR-05), lalu baris **chip kategori** (default General) yang memilih kategori sumber.
  * Setiap item: **nama sumber**, **deskripsi** (maks. 2 baris), dan label **bahasa/negara**.
  * Endpoint sources **tidak punya paginasi**, sehingga daftar diambil sekali per kategori, disimpan di Room (TTL 24 jam), lalu ditampilkan **20 item per halaman** secara bertahap dari Room (*endless scrolling* sisi klien).
* **Acceptance Criteria:**
  * **AC 2.1 (Positive):** Daftar sumber kategori tampil; tap membuka `ArticlesScreen` untuk sumber itu.
  * **AC 2.2 (Loading):** *Progress indicator* di tengah saat pemuatan awal.
  * **AC 2.3 (Negative - Initial Error):** Gagal memuat saat **cache kosong** (offline, timeout, JSON rusak, 5xx) → layar error ramah + tombol **Retry**. Bila cache ada, lihat AC 8.2.
  * **AC 2.4 (Negative - Empty):** Tidak ada sumber untuk kategori → teks *"No sources found."*
  * **AC 2.5 (Endless Scroll):** Scroll mendekati akhir 20 item pertama menambah 20 item berikutnya tanpa memuat ulang API.
  * **AC 2.6 (Negative - API Key):** `apiKeyInvalid` / `apiKeyMissing` / `apiKeyDisabled` → pesan konfigurasi tanpa tombol Retry yang menyesatkan.
  * **AC 2.7 (Negative - Rate Limit):** `rateLimited` / `apiKeyExhausted` → pesan *"Daily request limit reached. Try again later."*
  * **AC 2.8 (Cache):** Kembali ke layar yang sama, memutar layar, atau membuka ulang aplikasi dalam TTL tidak memanggil API lagi (data dari Room).

### FR-03: Artikel per Sumber (US3, US6)
* **Deskripsi:** Menampilkan artikel satu **sumber** (`/v2/top-headlines?sources=<id>&page=&pageSize=20`) atau satu **genre** (`?category=<x>&…`). Kedua daftar memakai layar yang sama.
* **UI/UX Behavior:**
  * `TopAppBar` menampilkan nama sumber dan tombol *Back*; di bawahnya *search field* (FR-05).
  * Kartu artikel: **gambar** (`urlToImage`, placeholder bila kosong), **judul** (maks. 3 baris), **nama sumber**, **tanggal terbit** (`dd MMM yyyy`), dan **deskripsi** (maks. 2 baris).
  * **Endless scrolling:** halaman berikutnya (`page + 1`) dimuat otomatis; footer menampilkan loading atau tombol *Retry*.
* **Acceptance Criteria:**
  * **AC 3.1 (Positive):** Artikel sumber tampil sesuai urutan API; tap membuka `ArticleWebScreen`. Bila verifikasi panggilan nyata menunjukkan urutan terbalik (lihat API Spec §Endpoint 2), tiap halaman diurutkan di klien dari terbaru ke terlama.
  * **AC 3.2 (Loading):** *Progress indicator* saat pemuatan awal.
  * **AC 3.3 (Negative - Initial Error):** Gagal pada halaman 1 saat **cache kosong** → layar error + **Retry**. Bila cache ada, lihat AC 8.2.
  * **AC 3.4 (Negative - Append Error):** Gagal pada halaman ke-2 dst → artikel di Room tetap tampil, footer **"Failed to load more — Retry"** (`rateLimited`: tanpa Retry).
  * **AC 3.5 (Negative - Empty):** Tidak ada artikel → *"No articles found."*
  * **AC 3.6 (Edge - Missing Fields):** `urlToImage`, `author`, `description`, atau `content` bernilai `null` tidak menyebabkan crash; gambar memakai placeholder.
  * **AC 3.7 (Edge - Unusable Item):** Artikel tanpa `url` atau dengan judul `"[Removed]"` tidak ditampilkan.
  * **AC 3.8 (End of List):** Paging berhenti bila `page × pageSize ≥ totalResults`, halaman kosong, atau API menolak halaman berikutnya karena batas hasil paket (*belum terverifikasi*); kondisi ini **bukan** error.
  * **AC 3.9 (Negative - Source):** `sourceDoesNotExist` → *"This source is no longer available."*

### FR-04: Detail Artikel di Web View (US4)
* **Deskripsi:** Membuka `url` artikel di dalam aplikasi menggunakan `WebView`.
* **UI/UX Behavior:**
  * `TopAppBar` dengan judul artikel (satu baris, *ellipsis*), tombol *Back*, dan aksi **"Open in browser"**.
  * `LinearProgressIndicator` di atas halaman selama pemuatan.
  * Navigasi dalam situs tetap di dalam `WebView`; tombol Back sistem mundur di riwayat web sebelum keluar layar.
* **Acceptance Criteria:**
  * **AC 4.1 (Positive):** Halaman termuat dan progress hilang setelah selesai.
  * **AC 4.2 (Negative - Load Error):** Gagal memuat halaman utama (offline, DNS, SSL) → layar error + **Retry** + **Open in browser**.
  * **AC 4.3 (Negative - Invalid URL):** `url` kosong atau bukan `http/https` → layar error tanpa membuat `WebView`.
  * **AC 4.4 (Security - http):** URL `http://` dicoba sebagai `https://`; bila gagal tampil error dengan **Open in browser**. Cleartext tetap diblokir.
  * **AC 4.5 (Security - Scheme):** Navigasi ke skema selain `http/https` (mis. `intent://`, `file://`) diblokir.
  * **AC 4.6 (Back):** Back sistem mundur di riwayat web bila tersedia; jika tidak, kembali ke daftar artikel.

### FR-05: Pencarian Sumber & Artikel (US5)
* **Deskripsi:** *Search field* di `SourcesScreen` dan `ArticlesScreen`.
* **Acceptance Criteria — Sumber (filter sisi klien):**
  * **AC 5.1 (Positive):** Mengetik memfilter sumber berdasarkan **nama atau deskripsi**, tidak peka huruf besar-kecil, instan, tanpa panggilan API.
  * **AC 5.2 (Negative - No Result):** Tidak ada yang cocok → *No sources match "…"*.
  * **AC 5.3:** Tombol hapus (×) mengosongkan query dan mengembalikan daftar penuh; query bertahan saat rotasi layar.
* **Acceptance Criteria — Artikel (pencarian API `q`):**
  * **AC 5.4 (Positive):** Setelah berhenti mengetik **500 ms** dan minimal **2 karakter**, daftar diganti hasil `top-headlines?sources=<id>&q=<query>`.
  * **AC 5.5 (Cancel):** Query baru membatalkan permintaan sebelumnya; hanya hasil terbaru yang tampil.
  * **AC 5.6 (Negative - No Result):** Tidak ada hasil → *No articles match "…"*.
  * **AC 5.7 (Negative - Error):** Gagal saat mencari → pesan error + **Retry**; teks pencarian tetap ada.
  * **AC 5.8:** Query kosong atau < 2 karakter (setelah `trim`) kembali ke daftar default tanpa memanggil API pencarian, termasuk saat pengguna menghapus teks dari ≥ 2 menjadi < 2 karakter (hasil pencarian lama tidak boleh tertinggal).

### FR-06: Endless Scrolling (US6)
* **Deskripsi:** Seluruh daftar panjang memakai Paging 3 dengan ukuran halaman 20.
* **Acceptance Criteria:**
  * **AC 6.1:** Posisi scroll tidak melompat saat halaman baru ditambahkan.
  * **AC 6.2:** Footer loading muncul saat halaman berikutnya dimuat; footer error + Retry bila gagal.
  * **AC 6.3:** Tidak ada item ganda yang membuat crash (kunci item aman terhadap duplikasi).

### FR-07: Penanganan Error & Umpan Balik (US7)
* **Deskripsi:** Kode error NewsAPI dibaca dari body (`{status:"error", code, message}`) dan dipetakan ke tipe error domain serta pesan yang ramah.

| Kondisi | Pesan pengguna | Aksi |
|---|---|---|
| Offline / timeout (`IOException`) | "No internet connection. Check your network and try again." | Retry |
| `apiKeyMissing` / `apiKeyInvalid` / `apiKeyDisabled` | "The API key is missing or invalid. Check the app configuration." | – |
| `rateLimited` / `apiKeyExhausted` | "Daily request limit reached. Try again later." | – |
| `sourceDoesNotExist` | "This source is no longer available." | Back |
| `parameterInvalid` / `parametersMissing` / `sourcesTooMany` | "Something went wrong. Please try again." | Retry |
| `unexpectedError` / 5xx | "The news service is having problems. Try again shortly." | Retry |
| JSON rusak / tak terduga | "Unexpected response from the server." | Retry |

* **Acceptance Criteria:**
  * **AC 7.1:** Setiap kondisi di atas menampilkan pesan dan aksi yang sesuai di semua layar.
  * **AC 7.2:** Pembatalan coroutine (mis. keluar layar) **tidak** diubah menjadi error.
  * **AC 7.3:** Pesan error tidak membocorkan API key atau URL permintaan.

### FR-08: Offline-First & Cache (US7)
* **Deskripsi:** Daftar sumber dan artikel dibaca dari Room; jaringan hanya menyegarkannya (ADR-013).
* **Acceptance Criteria:**
  * **AC 8.1 (TTL):** Membuka layar yang cache-nya masih segar (sumber < 24 jam, artikel < 1 jam per sumber dan query) **tidak** memanggil API. Cache yang kedaluwarsa disegarkan otomatis saat layar dibuka.
  * **AC 8.2 (Offline dengan Cache):** Offline atau gagal menyegarkan saat cache ada → data tersimpan tetap tampil dengan banner **"Offline, menampilkan data tersimpan"**; data lokal tidak dihapus oleh kegagalan.
  * **AC 8.3 (Cache Kosong):** Offline tanpa cache → layar error + **Retry** (AC 2.3, 3.3). Pencarian artikel yang belum pernah di-cache saat offline → pesan error + **Retry** (AC 5.7).
  * **AC 8.4 (Atomik):** Penyegaran mengganti data lama dan data baru dalam satu transaksi; pengguna tidak pernah melihat daftar setengah terhapus.
  * **AC 8.5 (Kuota):** `rateLimited` / `apiKeyExhausted` → cache tetap tampil dengan banner kuota, tanpa *auto-retry*.
  * **AC 8.6 (Urutan & Kunci):** Urutan artikel sama dengan urutan API; kunci item daftar stabil walau `url` sama muncul di halaman berbeda (AC 6.3).
  * **AC 8.7 (Retensi):** Artikel lebih tua dari 7 hari dan baris di atas batas total dibersihkan; pembersihan tidak mengganggu daftar yang sedang tampil.
  * **AC 8.8 (Gambar & WebView):** Gambar yang pernah dimuat tersedia offline (disk cache Coil). Membuka artikel saat offline tetap menampilkan error + Retry + Open in browser (AC 4.2).

### FR-09: Home & Highlight (US8)
* **Deskripsi:** Tab **Home** (start destination) menampilkan headline terbaru dari satu feed `top-headlines?country=us`.
* **UI/UX Behavior:**
  * `TopAppBar` **"Beacon"**; bagian **Highlights**: carousel horizontal 5 headline teratas (gambar penuh, judul 3 baris di atas scrim, nama sumber) dengan indikator halaman.
  * Bagian **Latest**: kartu artikel (FR-03) untuk item berikutnya, *endless scrolling*.
  * Tap carousel atau kartu membuka `ArticleWebScreen` langsung.
  * Tarik ke bawah (*pull-to-refresh*) menyegarkan feed (tindakan pengguna, menghabiskan kuota).
* **Acceptance Criteria:**
  * **AC 9.1 (Positive):** Carousel dan daftar tampil; urutan sama dengan API.
  * **AC 9.2 (Offline-first):** Cache masih segar (< 1 jam) tidak memanggil API; offline dengan cache menampilkan data + banner (AC 8.2); tanpa cache tampil error + Retry.
  * **AC 9.3 (Dedup):** Item yang sudah ada di carousel tidak muncul lagi di "Latest".
  * **AC 9.4 (Few items):** Kurang dari 5 headline tetap tampil tanpa crash; bagian "Latest" disembunyikan.

---

## 5. Non-Functional Requirements (NFR)
* **Architecture:** Strict separation of concerns (**Presentation** → **Domain** → **Data**). `:feature:*` hanya bergantung pada `:domain`, `:core:ui`, `:core:common`. Layer `:domain` tidak memakai class Android, Retrofit, maupun OkHttp.
* **Concurrency:** `viewModelScope`, `Dispatchers.IO` di repository, `debounce` + `flatMapLatest` untuk pencarian artikel.
* **Security:**
  * API key disimpan di `local.properties` (`NEWS_API_KEY`), masuk ke `BuildConfig`, dikirim lewat header `X-Api-Key`, tidak pernah di-commit atau dicatat di log.
  * Lalu lintas cleartext diblokir lewat network security config.
  * `WebView`: `allowFileAccess=false`, `allowContentAccess=false`, mixed content ditolak, hanya skema `http/https`.
  * R8 aktif pada build release dengan nama acak.
* **Performance & Quota:** Paket Developer NewsAPI dibatasi **100 request/hari**. Cache persisten di Room dengan TTL (sumber 24 jam, artikel 1 jam), `RemoteMediator`, `cachedIn(viewModelScope)`, debounce pencarian, dan tidak ada *auto-retry* saat `rateLimited`.
* **Compatibility:** Min SDK 24; tanpa `java.time` (butuh API 26) dan tanpa `SimpleDateFormat` (tidak thread-safe); tanggal diformat dengan parser manual yang stateless.
* **Accessibility:** Semua ikon berdeskripsi (`contentDescription`), target sentuh ≥ 48 dp, kontras warna memenuhi Material 3.
* **Testability:** Unit test (JUnit 5 `@ParameterizedTest`, MockK, Turbine, paging-testing) untuk parser error, mapper, `RemoteMediator`, repository, `ViewModel`, dan verifikasi graf Koin. Compose UI test dan instrumented test menyusul.

---

## 6. Risiko & Batasan
| Risiko | Mitigasi |
|---|---|
| Paket Developer: 100 request/hari, artikel tertunda 24 jam, **hanya untuk dev/testing**. | Cache Room + TTL + debounce; catat di README bahwa rilis publik butuh paket berbayar. |
| Sources tanpa paginasi dan tanpa pencarian server. | Fetch sekali per kategori ke Room; paginasi dan filter `LIKE` oleh Room (ADR-013). |
| URL artikel `http://` berbenturan dengan pemblokiran cleartext. | Upgrade ke `https://`, fallback ke browser (AC 4.4). |
| Perilaku API yang tidak terdokumentasi (batas hasil, urutan, `"[Removed]"`). | Ditandai *belum terverifikasi*; diuji dengan panggilan nyata sebelum dianggap final. |
| Situs berita memuat skrip berat/iklan di `WebView`. | Pengaturan `WebView` yang diperketat; fallback ke browser. |
| `RemoteMediator` kompleks (transaksi, kunci halaman, daftar melompat saat refresh). | TTL di `initialize()`, transaksi tunggal, test mediator dengan `TestPager` (ADR-013). |
| Data cache basi atau terlalu besar. | TTL, retensi 7 hari, batas jumlah baris, `fallbackToDestructiveMigration` untuk cache. |
