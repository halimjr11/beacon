// Room 3 + KSP come from the convention plugin (schemas exported to ./schemas).
plugins {
    id("beacon.android.room")
    id("beacon.android.koin")
}

dependencies {
    // api: Koin Module type appears in databaseModule's public signature
    api(libs.bundles.koin.compose)
}
