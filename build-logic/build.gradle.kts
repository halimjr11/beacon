plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Plugins the convention scripts apply must be on this build's classpath.
    implementation(libs.build.android.gradle.plugin)
    implementation(libs.build.kotlin.gradle.plugin)
    implementation(libs.build.compose.gradle.plugin)
    implementation(libs.build.kotlin.serialization.gradle.plugin)
    implementation(libs.build.ksp.gradle.plugin)
    implementation(libs.build.room.gradle.plugin)
}
