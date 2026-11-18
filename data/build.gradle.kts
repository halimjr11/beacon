plugins {
    id("beacon.android.library")
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":domain"))

    api(libs.bundles.koin.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit.core)

    testImplementation(libs.androidx.paging.testing)
}
