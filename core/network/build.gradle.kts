import java.util.Properties

plugins {
    id("beacon.android.library")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
// local.properties holds raw values (no quotes); wrap them as BuildConfig strings.
fun String.asBuildConfigString() = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    defaultConfig {
        buildConfigField("String", "NEWS_API_KEY", (localProperties.getProperty("NEWS_API_KEY") ?: "").asBuildConfigString())
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        // Test base URL override is debug-only (QA mock server, see test plan).
        debug {
            val base = localProperties.getProperty("NEWS_API_BASE_URL") ?: "https://newsapi.org/v2/"
            buildConfigField("String", "BASE_URL", base.asBuildConfigString())
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://newsapi.org/v2/\"")
        }
    }
}

dependencies {
    api(project(":core:common"))
    api(libs.bundles.koin.compose)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.bundles.networking)
}
