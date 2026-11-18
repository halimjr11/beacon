# API Specification — NewsAPI v2 Integration (Beacon)

> Sumber: dokumentasi resmi [newsapi.org](https://newsapi.org/docs). Hal yang **tidak** tertulis di dokumentasi ditandai *(belum terverifikasi)* dan harus diuji dengan panggilan nyata sebelum dianggap final.

## 1. Base Configuration & Authentication
* **Base URL:** `https://newsapi.org/v2/`
* **Authentication (header via `ApiKeyInterceptor`):**
  ```http
  X-Api-Key: <NEWS_API_KEY>
  Accept: application/json
  ```
  NewsAPI juga menerima `apiKey` sebagai query string atau header `Authorization: <key>` (tanpa kewajiban prefix `Bearer`, tanpa Base64). **Beacon memakai `X-Api-Key`** karena dokumentasi merekomendasikan header agar kunci tidak muncul di log.
* **Konfigurasi lokal:** `local.properties` berisi `NEWS_API_KEY=<kunci>` (tanpa tanda kutip; Gradle membungkusnya menjadi string `BuildConfig`). File ini di-ignore Git.
* **Kuota paket Developer:** 100 request/hari, artikel tertunda 24 jam, pencarian maksimal 1 bulan ke belakang, hanya untuk development/testing. Jumlah hasil tidak memengaruhi hitungan request: **setiap panggilan HTTP = 1 request**.

---

## 2. Endpoint Specifications

### Endpoint 1: Get Sources
* **Method & Path:** `GET /top-headlines/sources`
* **Query Parameters:**

  | Parameter | Tipe | Wajib | Keterangan |
  |---|---|---|---|
  | `category` | String | Tidak | `business`, `entertainment`, `general`, `health`, `science`, `sports`, `technology` |
  | `language` | String | Tidak | `ar, de, en, es, fr, he, it, nl, no, pt, ru, sv, ud, zh`. Beacon: `en` |
  | `country` | String | Tidak | Kode ISO 3166-1 (mis. `us`, `gb`, `id`). Beacon: tidak dipakai |

* **Tidak ada** `page`, `pageSize`, maupun `q`. Seluruh hasil dikembalikan sekaligus.
* **Response `200 OK`:**
  ```json
  {
    "status": "ok",
    "sources": [
      {
        "id": "bbc-news",
        "name": "BBC News",
        "description": "Use BBC News for up-to-the-minute news, breaking news, video, audio and feature stories.",
        "url": "http://www.bbc.co.uk/news",
        "category": "general",
        "language": "en",
        "country": "gb"
      }
    ]
  }
  ```
* **Mapping Rule → `NewsSource`:**
  * Buang item tanpa `id` atau dengan `name` kosong.
  * `description`, `category`, `language`, `country` yang `null` → `""`.
  * `country` ditampilkan sebagai kode huruf besar (`GB`), `language` sebagai kode huruf besar (`EN`).
* **Strategi Pemakaian (ADR-013):**
  * Diambil **sekali per kategori** oleh `SourcesRemoteMediator` dan disimpan di Room (`remote_keys` kunci = `sources:<category>:<language>`, TTL 24 jam).
  * Room `PagingSource` memberi **20 item per halaman**; pencarian memakai `LIKE` di Room.
  * Kegagalan **tidak** menghapus data lokal; Retry memanggil API lagi.

---

### Endpoint 2: Get Top Headlines (per Source, Paginated)
* **Method & Path:** `GET /top-headlines`
* **Query Parameters:**

  | Parameter | Tipe | Wajib | Keterangan |
  |---|---|---|---|
  | `sources` | String | **Ya (Beacon)** | Satu id sumber (mis. `bbc-news`). Dokumentasi: daftar dipisah koma; **tidak boleh** digabung `country`/`category`. |
  | `q` | String | Tidak | Kata kunci atau frasa pencarian. Boleh digabung dengan `sources`. |
  | `pageSize` | Int | Tidak | Default 20, maksimum 100. Beacon: `20`. |
  | `page` | Int | Tidak | Nomor halaman, mulai dari `1`. |

* **Response `200 OK`:**
  ```json
  {
    "status": "ok",
    "totalResults": 37,
    "articles": [
      {
        "source": { "id": "bbc-news", "name": "BBC News" },
        "author": "BBC News",
        "title": "Headline text",
        "description": "Short summary of the article.",
        "url": "https://www.bbc.co.uk/news/world-1234567",
        "urlToImage": "https://ichef.bbci.co.uk/news/1024/branded_news/abcd.jpg",
        "publishedAt": "2026-07-24T10:15:30Z",
        "content": "Truncated article text… [+1234 chars]"
      }
    ]
  }
  ```
  `content` dipotong 200 karakter (dokumentasi); penanda `[+N chars]` di akhir *(belum terverifikasi)*. Detail lengkap hanya lewat `url` (FR-04).
* **Pagination Logic (`ArticlesRemoteMediator`):**
  * `prevKey = if (page == 1) null else page - 1`
  * `nextKey = null` bila **salah satu** benar: `articles` kosong, `page * pageSize >= totalResults`, atau API menolak halaman berikutnya karena batas hasil paket (*kode error belum terverifikasi*, lihat §3).
  * Selain itu `nextKey = page + 1`.
* **Mapping Rule → `Article`:**
  * Buang artikel tanpa `url`, atau dengan `title == "[Removed]"` *(perilaku artikel yang dihapus, belum terverifikasi)* atau judul kosong.
  * `id` = `url` (stabil dan unik).
  * `sourceName` = `source.name`; `author`, `description`, `content` `null` → `""`; `urlToImage` `null`/kosong → `""` (UI memakai placeholder).
  * `publishedAt` (ISO-8601 UTC) → `dd MMM yyyy` (hanya 10 karakter pertama, tanpa pergeseran zona waktu); tidak valid → string asli.
  * Kunci item di daftar Compose **tidak boleh** memakai `url` saja karena bisa duplikat antarhalaman; pakai `url` + indeks atau tanpa key.
* **Urutan hasil:** dokumentasi menyebut *"earliest first"*, yang janggal untuk berita utama. **Verifikasi dengan panggilan nyata**; bila terbalik, urutkan di sisi klien per halaman (terbaru dulu, sesuai PRD AC 3.1).

---

### Endpoint 3 (Opsional, di luar cakupan): Everything
* `GET /everything` mendukung `q`, `sources`, `domains`, `from`, `to`, `language`, `sortBy`, `page`, `pageSize`. Tidak dipakai pada iterasi ini; halaman dokumentasinya belum diverifikasi.

---

## 3. Error Responses

* **Bentuk:**
  ```json
  { "status": "error", "code": "apiKeyInvalid", "message": "Your API key is invalid or incorrect." }
  ```
* **Aturan:** baca `code` dari **body**; dokumentasi tidak memetakan kode ke status HTTP, jadi jangan bergantung pada status. Bila body kosong atau bukan JSON, gunakan status HTTP sebagai cadangan.

| `code` | Arti | Tipe error domain | Aksi UI |
|---|---|---|---|
| `apiKeyMissing` | Kunci tidak dikirim | `InvalidApiKey` | Pesan konfigurasi |
| `apiKeyInvalid` | Kunci salah | `InvalidApiKey` | Pesan konfigurasi |
| `apiKeyDisabled` | Kunci dinonaktifkan | `InvalidApiKey` | Pesan konfigurasi |
| `apiKeyExhausted` | Kuota kunci habis | `RateLimited` | Pesan kuota, tanpa retry |
| `rateLimited` | Terlalu sering | `RateLimited` | Pesan kuota, tanpa retry |
| `sourceDoesNotExist` | Sumber tidak ada | `SourceNotFound` | Pesan + Back |
| `sourcesTooMany` | Terlalu banyak sumber | `BadRequest` | Pesan umum + Retry |
| `parameterInvalid` | Parameter tidak didukung | `BadRequest` | Pesan umum + Retry |
| `parametersMissing` | Parameter wajib hilang | `BadRequest` | Pesan umum + Retry |
| `unexpectedError` | Kesalahan server | `Server` | Retry |
| `maximumResultsReached` *(belum terverifikasi)* | Melewati batas hasil paket | — (akhir daftar, bukan error) | Hentikan paging |
| tidak dikenal | – | `Unknown` | Pesan umum + Retry |

* **Kegagalan non-HTTP:**

  | Kondisi | Tipe error domain |
  |---|---|
  | `IOException` (offline, timeout, DNS, SSL) | `Offline` |
  | `JsonParseException` / body tak terduga | `Parse` |
  | Pembatalan coroutine | **Tidak** dipetakan; selalu di-*rethrow* |

---

## 4. Contoh Retrofit Interface (referensi implementasi)
```kotlin
interface NewsApiService {

    @GET("top-headlines/sources")
    suspend fun getSources(
        @Query("category") category: String?,
        @Query("language") language: String? = "en"
    ): SourcesResponseDto

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("sources") sourceId: String,
        @Query("q") query: String?,      // null: Retrofit menghilangkan parameter
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): ArticlesResponseDto
}
```
* Semua DTO bertipe nullable dengan default `null` (Gson memakai constructor tanpa argumen) dan memakai `@SerializedName`. Aturan R8 DTO berada di `consumer-rules.pro` modul `:core:network`.

## 5. Daftar Kategori Statis
| Nilai API | Nama tampil |
|---|---|
| `business` | Business |
| `entertainment` | Entertainment |
| `general` | General |
| `health` | Health |
| `science` | Science |
| `sports` | Sports |
| `technology` | Technology |
