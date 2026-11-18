plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.halimjr11.beacon.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    api(libs.bundles.coil)
    api(libs.androidx.paging.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.bundles.unit.test)
}
