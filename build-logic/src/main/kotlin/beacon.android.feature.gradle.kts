// Feature module: screens + ViewModels. May depend only on :domain, :core:ui, :core:common.
plugins {
    id("beacon.android.library.compose")
}

val forbidden = setOf(":data", ":core:network", ":core:database")

dependencies {
    "api"(project(":core:common"))
    "implementation"(project(":core:ui"))
    "implementation"(project(":domain"))

    "api"(catalog.bundle("koin-compose"))
    "implementation"(catalog.lib("androidx-lifecycle-runtime-ktx"))

    "testImplementation"(catalog.lib("androidx-paging-testing"))
}

// Fail the build when a feature reaches into the data layer (dependency matrix, architecture doc §1.B).
configurations.matching { it.name in setOf("api", "implementation") }.configureEach {
    dependencies.withType<ProjectDependency>().configureEach {
        check(path !in forbidden) {
            "${project.path} must not depend on $path (features use only :domain, :core:ui, :core:common)"
        }
    }
}
