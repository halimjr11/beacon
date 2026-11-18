# Product Requirements Document (PRD) — Beacon

## 1. Document Control & Overview
* **Product Name:** Beacon *(mercusuar: penunjuk arah di tengah lautan informasi, merepresentasikan aplikasi yang membantu pengguna menemukan berita dari sumber yang tepat)*
* **Package Name:** `com.halimjr11.beacon`
* **Tech Stack:** Kotlin, Jetpack Compose (Material 3), MVVM, Clean Architecture, Coroutines & Flow, Koin 4.2.2, Navigation 3, Paging 3, Retrofit + OkHttp + Gson, Coil 3, Android `WebView`.
* **Target Platform:** Android (Min SDK 24 / Android 7.0, Target SDK 37).
* **Data Source:** [NewsAPI](https://newsapi.org) v2 (`/v2/top-headlines/sources`, `/v2/top-headlines`).
* **Objective:** Aplikasi native untuk menjelajahi berita lewat alur **Kategori → Sumber Berita → Artikel → Detail Artikel (web view)**, dilengkapi pencarian sumber dan artikel, *endless scrolling*, serta penanganan kasus positif dan negatif yang menyeluruh.

### 1.1 Asumsi & Keputusan
| # | Keputusan | Alasan |
|---|---|---|
| A1 | Daftar sumber difilter `language=en` secara default (konstanta, mudah diubah). | Tanpa filter, hasil mencampur belasan bahasa. |
| A2 | Artikel diambil dari `top-headlines` dengan `sources=<id>`, bukan `everything`. | Sesuai alur "berita utama per sumber"; `q` tetap bisa dipakai untuk pencarian. |
| A3 | Detail artikel memakai `WebView` (bukan Custom Tabs). | Sesuai user story 4. |
| A4 | Pencarian artikel memanggil API (`q`) dengan debounce; pencarian sumber memfilter di sisi klien. | API sources tidak punya parameter pencarian. |
| A5 | Tidak ada database lokal pada iterasi ini. | Fokus pada paginasi, pencarian, dan penanganan error. |

### 1.2 Di Luar Cakupan (Out of Scope)
Bookmark/favorit, cache offline (Room), notifikasi, login, berbagi artikel, Custom Tabs, endpoint `/v2/everything`, mode gelap khusus per layar, Compose UI test dan instrumented test (**segera ditambahkan** setelah fitur stabil).

---

## 2. User Flow & Navigation Map
```text
[CategoriesScreen] (Start Destination, statis)
       │
       ├── Tap kategori (category)
       ▼
[SourcesScreen] (Search field + daftar sumber, endless scroll)
       │
       ├── Tap sumber (sourceId, sourceName)
       ▼
[ArticlesScreen] (Search field + daftar artikel, endless scroll)
       │
       ├── Tap artikel (url, title)
       ▼
[ArticleWebScreen] (WebView + progress + error/retry)
       │
       └── Tap "Open in browser" ──► Browser eksternal (fallback)
```

---

## 3. User Stories

| # | User story | Dicakup oleh |
|---|---|---|
| US1 | Menampilkan daftar kategori berita. | FR-01 |
| US2 | Menampilkan sumber berita saat kategori dipilih. | FR-02 |
| US3 | Menampilkan artikel saat sumber dipilih. | FR-03 |
| US4 | Menampilkan detail artikel di web view. | FR-04 |
| US5 | Pencarian sumber dan artikel. | FR-05 |
| US6 | *Endless scrolling* di sumber dan artikel. | FR-02, FR-03, FR-06 |
| US7 | Kasus positif dan negatif tercakup. | Semua FR, FR-07, [Test Plan](test_plan_and_qa.md) |

---

## 4. Functional Requirements & Acceptance Criteria (AC)

### FR-01: Daftar Kategori (US1)
* **Deskripsi:** Layar awal menampilkan tujuh kategori resmi NewsAPI: `business`, `entertainment`, `general`, `health`, `science`, `sports`, `technology`.
* **UI/UX Behavior:**
  * `TopAppBar` berjudul **"Beacon"**.
  * Daftar/grid kartu Material 3 dengan nama kategori (mis. *Business*, *Technology*). Urutan tetap seperti di atas.
  * Kategori bersifat **statis** (bukan hasil API), jadi tidak ada loading maupun error.
* **Acceptance Criteria:**
  * **AC 1.1 (Positive):** Tujuh kategori tampil, dapat diklik, dan membuka `SourcesScreen` untuk kategori itu.
  * **AC 1.2 (Offline-safe):** Layar tetap tampil penuh walau perangkat offline.

### FR-02: Sumber Berita per Kategori (US2, US6)
* **Deskripsi:** Menampilkan sumber berita untuk kategori terpilih (`/v2/top-headlines/sources?category=…&language=en`).
* **UI/UX Behavior:**
  * `TopAppBar` menampilkan nama kategori dan tombol *Back*.
  * Di bawahnya ada *search field* (FR-05).
  * Setiap item: **nama sumber**, **deskripsi** (maks. 2 baris), dan label **bahasa/negara**.
  * Endpoint sources **tidak punya paginasi**, sehingga daftar diambil sekali, disimpan di memori, lalu ditampilkan **20 item per halaman** secara bertahap (*endless scrolling* sisi klien).
* **Acceptance Criteria:**
  * **AC 2.1 (Positive):** Daftar sumber kategori tampil; tap membuka `ArticlesScreen` untuk sumber itu.
  * **AC 2.2 (Loading):** *Progress indicator* di tengah saat pemuatan awal.
  * **AC 2.3 (Negative - Initial Error):** Gagal memuat (offline, timeout, JSON rusak, 5xx) → layar error ramah + tombol **Retry**.
  * **AC 2.4 (Negative - Empty):** Tidak ada sumber untuk kategori → teks *"No sources found."*
  * **AC 2.5 (Endless Scroll):** Scroll mendekati akhir 20 item pertama menambah 20 item berikutnya tanpa memuat ulang API.
  * **AC 2.6 (Negative - API Key):** `apiKeyInvalid` / `apiKeyMissing` / `apiKeyDisabled` → pesan konfigurasi tanpa tombol Retry yang menyesatkan.
  * **AC 2.7 (Negative - Rate Limit):** `rateLimited` / `apiKeyExhausted` → pesan *"Daily request limit reached. Try again later."*
  * **AC 2.8 (Cache):** Kembali ke layar yang sama (atau memutar layar) tidak memanggil API lagi.

### FR-03: Artikel per Sumber (US3, US6)
* **Deskripsi:** Menampilkan artikel sumber terpilih (`/v2/top-headlines?sources=<id>&page=&pageSize=20`).
* **UI/UX Behavior:**
  * `TopAppBar` menampilkan nama sumber dan tombol *Back*; di bawahnya *search field* (FR-05).
  * Kartu artikel: **gambar** (`urlToImage`, placeholder bila kosong), **judul** (maks. 3 baris), **nama sumber**, **tanggal terbit** (`dd MMM yyyy`), dan **deskripsi** (maks. 2 baris).
  * **Endless scrolling:** halaman berikutnya (`page + 1`) dimuat otomatis; footer menampilkan loading atau tombol *Retry*.
* **Acceptance Criteria:**
  * **AC 3.1 (Positive):** Artikel sumber tampil sesuai urutan API; tap membuka `ArticleWebScreen`. Bila verifikasi panggilan nyata menunjukkan urutan terbalik (lihat API Spec §Endpoint 2), tiap halaman diurutkan di klien dari terbaru ke terlama.
  * **AC 3.2 (Loading):** *Progress indicator* saat pemuatan awal.
  * **AC 3.3 (Negative - Initial Error):** Gagal pada halaman 1 → layar error + **Retry**.
  * **AC 3.4 (Negative - Append Error):** Gagal pada halaman ke-2 dst → artikel sebelumnya tetap tampil, footer **"Failed to load more — Retry"**.
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

---

## 5. Non-Functional Requirements (NFR)
* **Architecture:** Strict separation of concerns (**Presentation** → **Domain** → **Data**). `:feature:*` hanya bergantung pada `:domain`, `:core:ui`, `:core:common`. Layer `:domain` tidak memakai class Android, Retrofit, maupun OkHttp.
* **Concurrency:** `viewModelScope`, `Dispatchers.IO` di repository, `debounce` + `flatMapLatest` untuk pencarian artikel.
* **Security:**
  * API key disimpan di `local.properties` (`NEWS_API_KEY`), masuk ke `BuildConfig`, dikirim lewat header `X-Api-Key`, tidak pernah di-commit atau dicatat di log.
  * Lalu lintas cleartext diblokir lewat network security config.
  * `WebView`: `allowFileAccess=false`, `allowContentAccess=false`, mixed content ditolak, hanya skema `http/https`.
  * R8 aktif pada build release dengan nama acak.
* **Performance & Quota:** Paket Developer NewsAPI dibatasi **100 request/hari**. Cache sumber per kategori di memori, `cachedIn(viewModelScope)`, debounce pencarian, dan tidak ada *auto-retry* saat `rateLimited`.
* **Compatibility:** Min SDK 24; tanpa `java.time` (butuh API 26) dan tanpa `SimpleDateFormat` (tidak thread-safe); tanggal diformat dengan parser manual yang stateless.
* **Accessibility:** Semua ikon berdeskripsi (`contentDescription`), target sentuh ≥ 48 dp, kontras warna memenuhi Material 3.
* **Testability:** Unit test (JUnit 5 `@ParameterizedTest`, MockK, Turbine, paging-testing) untuk parser error, mapper, `PagingSource`, repository, `ViewModel`, dan verifikasi graf Koin. Compose UI test dan instrumented test menyusul.

---

## 6. Risiko & Batasan
| Risiko | Mitigasi |
|---|---|
| Paket Developer: 100 request/hari, artikel tertunda 24 jam, **hanya untuk dev/testing**. | Cache + debounce; catat di README bahwa rilis publik butuh paket berbayar. |
| Sources tanpa paginasi dan tanpa pencarian server. | Paginasi dan filter di sisi klien (ADR-003). |
| URL artikel `http://` berbenturan dengan pemblokiran cleartext. | Upgrade ke `https://`, fallback ke browser (AC 4.4). |
| Perilaku API yang tidak terdokumentasi (batas hasil, urutan, `"[Removed]"`). | Ditandai *belum terverifikasi*; diuji dengan panggilan nyata sebelum dianggap final. |
| Situs berita memuat skrip berat/iklan di `WebView`. | Pengaturan `WebView` yang diperketat; fallback ke browser. |
