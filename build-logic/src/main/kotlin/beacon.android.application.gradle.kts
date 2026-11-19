// The :app shell: SDK levels, Java 17, JUnit 5, Compose.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") // @Serializable navigation keys live in :app
}

android {
    namespace = BeaconBuild.NAMESPACE_ROOT
    compileSdk {
        version = release(BeaconBuild.COMPILE_SDK)
    }
    defaultConfig {
        applicationId = BeaconBuild.NAMESPACE_ROOT
        minSdk = BeaconBuild.MIN_SDK
        targetSdk = BeaconBuild.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(BeaconBuild.JVM)
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
    "implementation"(platform(catalog.lib("androidx-compose-bom").get()))
    "implementation"(catalog.bundle("compose"))
    "testImplementation"(catalog.bundle("unit-test"))

    "androidTestImplementation"(catalog.lib("androidx-junit"))
    "androidTestImplementation"(catalog.lib("androidx-espresso-core"))
    "androidTestImplementation"(platform(catalog.lib("androidx-compose-bom").get()))
    "androidTestImplementation"(catalog.lib("androidx-compose-ui-test-junit4"))
    "debugImplementation"(catalog.lib("androidx-compose-ui-test-manifest"))
}
