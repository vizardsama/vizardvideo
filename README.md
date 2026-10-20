# SpendTrace

Android app (Kotlin, Jetpack Compose, Room) yang baca notification bayaran dari app bank dan e-wallet,
parse jumlah dan merchant, auto-kategori, dan simpan dalam phone sahaja (tiada server).

## Cara build
1. Buka folder ini dalam Android Studio (Koala atau lebih baru), tunggu Gradle sync.
2. Run pada phone Android (minSdk 26).
3. Bila app dibuka, tekan butang untuk bagi Notification access, kemudian tick SpendTrace.

## Struktur
- `parser/` - `NotificationParser` dan `Categorizer`, pure Kotlin, ada unit test (`./gradlew :app:testDebugUnitTest`).
- `service/PaymentNotificationService` - NotificationListenerService, tapis ikut `SourceApps.packages`.
- `data/` - Room (`Transaction`, DAO). Duplicate dielak dengan unique `fingerprint`.
- `ui/` - skrin permission, ringkasan bulan ini ikut kategori, senarai transaksi (tap untuk tukar kategori / padam).

## Perlu disahkan
- Package name dalam `SourceApps.kt` adalah andaian, mungkin tak tepat. Semak package sebenar app anda
  (Settings > Apps, atau `adb shell pm list packages | grep -i <nama>`).
- Format notification berbeza antara bank. Parser perlu ditala guna contoh sebenar.
- Android dengan battery optimization agresif (Xiaomi, Oppo, Vivo, Samsung) boleh bunuh listener; letak app dalam "unrestricted".
