dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        // Same catalog as the main build: one place for every version.
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "build-logic"
