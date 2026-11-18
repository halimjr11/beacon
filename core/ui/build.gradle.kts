plugins {
    id("beacon.android.library.compose")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    api(libs.bundles.compose)
    api(libs.bundles.coil)
    api(libs.androidx.paging.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
