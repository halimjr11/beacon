// Base for every Android library module: SDK levels, Java 17, JUnit 5, shared test dependencies.
plugins {
    id("com.android.library")
}

android {
    namespace = "${BeaconBuild.NAMESPACE_ROOT}.${namespaceSuffix()}"
    compileSdk {
        version = release(BeaconBuild.COMPILE_SDK)
    }
    defaultConfig {
        minSdk = BeaconBuild.MIN_SDK
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(BeaconBuild.JVM)
}

// Local unit tests run on JUnit 5; instrumented tests (androidTest) stay on JUnit 4.
tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
    "testImplementation"(catalog.bundle("unit-test"))
}
