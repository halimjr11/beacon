// Android library with Jetpack Compose (Material 3).
plugins {
    id("beacon.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    "implementation"(platform(catalog.lib("androidx-compose-bom").get()))
    "implementation"(catalog.bundle("compose"))

    // Compose UI tests: createComposeRule + the manifest that hosts ComponentActivity.
    "androidTestImplementation"(platform(catalog.lib("androidx-compose-bom").get()))
    "androidTestImplementation"(catalog.lib("androidx-compose-ui-test-junit4"))
    "debugImplementation"(catalog.lib("androidx-compose-ui-test-manifest"))
}
