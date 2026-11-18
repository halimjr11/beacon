plugins {
    id("beacon.android.library")
}

dependencies {
    // api: DispatcherProvider and the Koin Module type appear in public signatures
    api(libs.kotlinx.coroutines.core)
    api(libs.bundles.koin.compose)
}
