plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.halimjr11.beacon.sources"
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
    // :feature:* may depend only on :domain, :core:ui, :core:common (never :data / :core:network)
    api(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":domain"))

    api(libs.bundles.koin.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.androidx.paging.testing)
}
