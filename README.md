# Neumorphic Launcher

Android launcher dengan gaya **Neumorphism (Soft UI)**, dibangun menggunakan **Kotlin** dan **Material Design 3**. Mendukung Android 8.0 (API 26) ke atas.

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-purple)
![Android](https://img.shields.io/badge/Android-API%2026%2B-green)
![Material3](https://img.shields.io/badge/Material-3-blue)

---

## Daftar Isi

1. [Fitur](#fitur)
2. [Persyaratan Sistem](#persyaratan-sistem)
3. [Cara Build](#cara-build)
4. [Struktur Proyek](#struktur-proyek)
5. [Cara Menjadikan Default Launcher](#cara-menjadikan-default-launcher)
6. [Panduan Penggunaan](#panduan-penggunaan)
7. [Kustomisasi](#kustomisasi)
8. [Troubleshooting](#troubleshooting)

---

## Fitur

### Fitur Utama

| Fitur | Deskripsi |
|-------|-----------|
| **Home Screen Multi-Page** | ViewPager2 untuk swipe antar halaman home, mendukung penempatan app shortcut |
| **App Drawer** | Daftar semua aplikasi terinstal dalam grid 4 kolom, dengan search bar real-time |
| **Search Aplikasi** | Filter aplikasi berdasarkan nama saat mengetik di search bar |
| **Dock Bawah** | 4-5 aplikasi favorit dengan ikon bulat Neumorphic di bagian bawah home |
| **Widget Jam & Tanggal** | Custom `NeumorphicClockView` dengan jam besar + tanggal, update setiap detik |
| **Pengaturan Tema** | Light / Dark / Auto mode, pilihan 5 warna aksen, slider corner radius & shadow intensity |
| **Swipe Gesture** | Swipe UP → buka App Drawer, Swipe DOWN → buka notification shade, Long press → Settings |
| **Gaya Neumorphism** | Custom view `NeumorphicCardView` dengan dual shadow (light + dark) untuk efek 3D lembut |

### Fitur Teknis

- **Kotlin** 1.9.10 dengan **ViewBinding** untuk type-safe view access
- **Coroutines** untuk operasi async (load aplikasi)
- **ListAdapter + DiffUtil** untuk RecyclerView yang efisien
- **Adaptive Icon** untuk Android 8.0+
- **Dark Mode** native via `values-night/`
- **State save/restore** via SharedPreferences

---

## Persyaratan Sistem

### Untuk Build

| Komponen | Versi Minimum |
|----------|---------------|
| Android Studio | Hedgehog (2023.1.1) atau lebih baru |
| Gradle | 8.2 |
| Android Gradle Plugin | 8.1.4 |
| JDK | 17 |
| Kotlin | 1.9.10 |
| Android SDK Platform | 34 (compileSdk) |
| Build Tools | 34.0.0 |

### Untuk Run (Target Device)

- **Android 8.0 (API 26)** atau lebih baru
- ~95% perangkat Android aktif saat ini didukung

---

## Cara Build

### Opsi 1: Build via Android Studio (Recommended)

1. **Clone atau extract** proyek ini ke direktori lokal.
2. Buka **Android Studio** → `File` → `Open` → pilih folder `NeumorphicLauncher`.
3. Tunggu Gradle sync selesai (mungkin butuh beberapa menit untuk download dependencies).
4. Hubungkan perangkat Android (USB debugging aktif) atau jalankan emulator.
5. Klik tombol **Run** ▶️ atau tekan `Shift+F10`.
6. APK akan ter-install di perangkat.

### Opsi 2: Build via Command Line

```bash
# 1. Pastikan ANDROID_SDK_ROOT sudah di-set
export ANDROID_SDK_ROOT=/path/to/android/sdk

# 2. Generate Gradle wrapper (jika belum ada gradlew)
gradle wrapper --gradle-version 8.2

# 3. Build debug APK
./gradlew assembleDebug

# 4. APK hasil build ada di:
# app/build/outputs/apk/debug/app-debug.apk

# 5. Install ke perangkat (USB debugging aktif)
adb install app/build/outputs/apk/debug/app-debug.apk

# 6. Build release APK (signed)
./gradlew assembleRelease
```

### Opsi 3: Build Release Signed APK

1. Generate keystore:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
   ```

2. Tambahkan konfigurasi signing di `app/build.gradle`:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file("path/to/my-release-key.jks")
               storePassword "your-password"
               keyAlias "my-alias"
               keyPassword "your-password"
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               minifyEnabled true
               proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
           }
       }
   }
   ```

3. Build:
   ```bash
   ./gradlew assembleRelease
   ```

---

## Struktur Proyek

```
NeumorphicLauncher/
├── build.gradle                          # Project-level Gradle config
├── settings.gradle                       # Project settings
├── gradle.properties                     # Gradle properties
├── gradle/wrapper/
│   └── gradle-wrapper.properties         # Gradle wrapper config
├── .gitignore
└── README.md
└── app/
    ├── build.gradle                      # App-level Gradle config
    ├── proguard-rules.pro                # ProGuard rules
    └── src/main/
        ├── AndroidManifest.xml           # Manifest (launcher intent filter)
        ├── java/com/neumorphic/launcher/
        │   ├── NeumorphicApp.kt          # Application class
        │   ├── MainActivity.kt           # Home screen
        │   ├── AppDrawerActivity.kt      # App drawer
        │   ├── SettingsActivity.kt       # Theme settings
        │   ├── adapter/
        │   │   ├── AppAdapter.kt         # Adapter untuk app drawer
        │   │   ├── AppGridAdapter.kt     # Adapter untuk home page grid
        │   │   ├── DockAdapter.kt        # Adapter untuk dock
        │   │   └── HomePagerAdapter.kt   # Adapter untuk ViewPager home
        │   ├── model/
        │   │   ├── AppInfo.kt            # Model data aplikasi
        │   │   ├── HomePage.kt           # Model halaman home
        │   │   └── ThemeConfig.kt        # Model konfigurasi tema
        │   ├── util/
        │   │   ├── AppLoader.kt          # Loader aplikasi terinstal
        │   │   ├── GestureHelper.kt      # Helper untuk gesture
        │   │   ├── NeumorphicHelper.kt   # Helper efek Neumorphic
        │   │   └── PreferenceManager.kt  # Manager SharedPreferences
        │   └── widget/
        │       ├── NeumorphicCardView.kt       # Custom view card Neumorphic
        │       ├── NeumorphicClockView.kt      # Custom view widget jam
        │       └── SwipeDetectRecyclerView.kt  # RecyclerView dengan swipe detection
        └── res/
            ├── layout/
            │   ├── activity_main.xml          # Layout home screen
            │   ├── activity_app_drawer.xml    # Layout app drawer
            │   ├── activity_settings.xml      # Layout settings
            │   ├── item_app.xml               # Layout item aplikasi
            │   ├── item_dock_app.xml          # Layout item dock
            │   └── item_home_page.xml         # Layout halaman home
            ├── drawable/
            │   ├── bg_neumorphic_card.xml         # BG convex (timbul)
            │   ├── bg_neumorphic_inset.xml         # BG inset (tenggelam)
            │   ├── bg_neumorphic_circle.xml        # BG bulat (dock)
            │   ├── bg_neumorphic_button_accent.xml # BG tombol accent
            │   ├── circle_purple.xml ... circle_orange.xml  # Color picker
            │   ├── ic_search.xml, ic_apps.xml, ic_arrow_back.xml, ic_settings.xml, ic_clock.xml
            │   └── ic_launcher_foreground.xml     # Adaptive icon foreground
            ├── mipmap-anydpi-v26/
            │   ├── ic_launcher.xml               # Adaptive launcher icon
            │   └── ic_launcher_round.xml
            ├── values/
            │   ├── colors.xml                    # Warna light mode
            │   ├── strings.xml                   # String resources (Bahasa Indonesia)
            │   ├── themes.xml                    # Tema (light)
            │   └── dimens.xml                    # Dimensi
            ├── values-night/
            │   ├── colors.xml                    # Warna dark mode
            │   └── themes.xml                    # Tema (dark)
            └── xml/
                └── backup_rules.xml              # Aturan backup data
```

---

## Cara Menjadikan Default Launcher

Setelah APK ter-install:

1. Buka **Settings** → **Apps** → **Default apps** → **Home app**.
2. Pilih **Neumorphic Launcher** dari daftar.
3. Atau, tekan tombol **Home** di perangkat — sistem akan memunculkan dialog pemilih launcher. Pilih **Neumorphic Launcher** → **Always**.

---

## Panduan Penggunaan

### Gesture di Home Screen

| Gesture | Aksi |
|---------|------|
| **Tap** pada clock | Buka aplikasi alarm/clock default |
| **Tap** pada search bar | Buka App Drawer dengan search aktif |
| **Tap** pada ikon dock | Buka aplikasi |
| **Swipe UP** | Buka App Drawer |
| **Swipe DOWN** | Buka notification shade |
| **Swipe LEFT/RIGHT** | Pindah halaman home |
| **Long press** di mana saja | Buka Settings |

### Gesture di App Drawer

| Gesture | Aksi |
|---------|------|
| **Tap** pada aplikasi | Buka aplikasi |
| **Long press** pada aplikasi | Tambahkan ke dock (jika belum penuh) |
| **Ketik di search bar** | Filter aplikasi real-time |
| **Tombol back / close** | Tutup App Drawer |

### Pengaturan Tema

Buka Settings (long press di home) untuk mengatur:

1. **Mode Tema**:
   - **Terang**: Light mode (background abu-abu terang)
   - **Gelap**: Dark mode (background gelap)
   - **Otomatis**: Mengikuti pengaturan sistem

2. **Warna Aksen**: 5 preset (ungu, biru, pink, hijau, oranye)

3. **Radius Sudut**: 12dp - 32dp (slider)

4. **Intensitas Bayangan**: 0% - 100% (slider)

Klik **Simpan** untuk menyimpan. Restart launcher (tombol home) untuk melihat perubahan tema penuh.

---

## Kustomisasi

### Mengubah Aplikasi Default di Dock

Saat ini dock berisi 4 aplikasi default:
- Phone (`com.android.dialer`)
- Chrome (`com.android.chrome`)
- Camera (`com.android.camera`)
- Messages (`com.google.android.apps.messaging`)

Untuk mengubahnya:
1. Buka **App Drawer**.
2. **Long press** pada aplikasi yang ingin ditambahkan ke dock.
3. Aplikasi akan otomatis ditambahkan (menggantikan yang terakhir jika dock penuh).

Daftar dock disimpan di SharedPreferences (`neumorphic_launcher_prefs`) dengan key `favorite_apps`.

### Menambahkan Halaman Home Baru

Edit `MainActivity.kt`, method `setupViewPager()`:

```kotlin
private fun setupViewPager() {
    binding.viewPager.apply {
        // Sebelumnya: adapter = HomePagerAdapter(this@MainActivity)
        // Sekarang: tambahkan data untuk multi-page
        val pages = listOf(
            listOf<AppInfo>(), // Halaman 1: kosong (default)
            listOf<AppInfo>()  // Halaman 2: bisa diisi app shortcut
        )
        val pagerAdapter = HomePagerAdapter()
        pagerAdapter.submitList(pages)
        adapter = pagerAdapter
    }
}
```

### Mengubah Warna Neumorphic

Edit `values/colors.xml` (light mode) atau `values-night/colors.xml` (dark mode):

```xml
<!-- Background utama — ubah untuk efek neumorphic yang berbeda -->
<color name="background_neu">#FFE0E5EC</color>

<!-- Light shadow — biasanya putih atau lebih terang dari bg -->
<color name="shadow_light_neu">#FFFFFFFF</color>

<!-- Dark shadow — lebih gelap dari bg -->
<color name="shadow_dark_neu">#FFA3B1C6</color>
```

**Tips**: Untuk efek neumorphic terbaik, gunakan background yang **grayscale** atau **low-saturation**. Hindari warna jenuh karena akan mengurangi ilusi 3D.

### Mengubah Layout Grid

Edit `activity_app_drawer.xml`, ubah `spanCount` pada GridLayoutManager:

```xml
<!-- Default 4 kolom. Ubah ke 5 atau 6 untuk perangkat tablet -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/appRecyclerView"
    app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
    app:spanCount="5"
    ... />
```

Atau ubah secara programatik di `AppDrawerActivity.kt`:

```kotlin
binding.appRecyclerView.layoutManager = GridLayoutManager(this, 5)
```

---

## Troubleshooting

### 1. Gradle Sync Failed: "SDK not found"

**Solusi**: Buat file `local.properties` di root proyek dengan isi:
```
sdk.dir=/path/to/your/Android/Sdk
```

### 2. Build Error: "Cannot resolve dependency"

**Solusi**:
- Pastikan ada koneksi internet saat Gradle sync pertama.
- Coba `File` → `Invalidate Caches & Restart`.
- Hapus folder `.gradle/` dan `build/`, lalu sync ulang.

### 3. Launcher tidak muncul di pilihan default

**Solusi**:
- Pastikan APK sudah ter-install dengan benar.
- Buka **Settings** → **Apps** → cari **Neumorphic Launcher** → pastikan tidak di-disable.
- Buka **Settings** → **Apps** → **Default apps** → **Home app** → pilih launcher.

### 4. Notification shade tidak terbuka saat swipe down

**Penyelasan**: Pada Android 11+ (API 30+), membuka notification shade secara programatik memerlukan permission sistem (`EXPAND_STATUS_BAR`) yang terbatas. Pada beberapa perangkat, ini hanya bisa dilakukan oleh aplikasi sistem.

**Workaround**: User dapat swipe down dari tepi atas layar secara native (gesture sistem Android).

### 5. App Drawer kosong / tidak menampilkan aplikasi

**Solusi**:
- Pada Android 11+ (API 30+), izin `QUERY_ALL_PACKAGES` diperlukan. Manifest sudah memasukkannya.
- Pastikan tidak ada aplikasi launcher sendiri yang ter-filter.
- Restart aplikasi setelah memberi izin.

### 6. Tema tidak berubah setelah disimpan

**Solusi**:
- Klik **Simpan** di Settings, lalu tekan tombol **Home** untuk restart launcher.
- Jika tetap tidak berubah, force-stop aplikasi dari Settings → Apps → Neumorphic Launcher → Force stop.

### 7. Ikon aplikasi terlihat buram

**Solusi**: Ikon yang dimuat adalah ikon asli dari aplikasi (dengan `loadIcon()`). Pada beberapa perangkat, ikon adaptive mungkin tidak ter-render dengan optimal. Untuk ikon yang lebih tajam, implementasi custom icon pack bisa ditambahkan.

---

## Roadmap (Pengembangan Selanjutnya)

Fitur yang bisa ditambahkan:

- [ ] **Drag-and-drop** app shortcut ke home page
- [ ] **Folder** untuk mengelompokkan aplikasi
- [ ] **Widget support** (Android app widget standar)
- [ ] **Icon pack** support (dari Apex/Nova icon pack)
- [ ] **Backup & restore** pengaturan
- [ ] **Animated transition** antar halaman
- [ ] **Custom grid size** (4x4, 5x5, 6x6)
- [ ] **Search web** dari search bar launcher
- [ ] **At-a-glance widget** (weather, calendar events)
- [ ] **Gestures kustom** (double tap, swipe edges)
- [ ] **Notification dot** pada ikon aplikasi
- [ ] **Predictive back** support (Android 14+)

---

## Lisensi

Proyek ini bebas digunakan, dimodifikasi, dan didistribusikan untuk keperluan pribadi maupun komersial.

## Kredit

- **Design**: Neumorphism / Soft UI design trend
- **Library**: AndroidX, Material Components for Android, Kotlin Coroutines
- **Target**: Android 8.0 (API 26) ke atas

---

**Dibuat dengan Kotlin** • **Didukung Material 3** • **Diilhami Neumorphism**
