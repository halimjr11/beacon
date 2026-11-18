// Android library with Jetpack Compose (Material 3).
plugins {
    id("beacon.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    "implementation"(platform(catalog.lib("androidx-compose-bom").get()))
    "implementation"(catalog.bundle("compose"))
}
