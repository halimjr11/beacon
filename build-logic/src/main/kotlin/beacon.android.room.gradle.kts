// Room 3 (KSP-only) with schema export. Apply to the module that owns the database.
plugins {
    id("beacon.android.library")
    id("com.google.devtools.ksp")
    id("androidx.room3")
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    "implementation"(catalog.lib("androidx-room-runtime"))
    "implementation"(catalog.lib("androidx-room-paging"))
    "ksp"(catalog.lib("androidx-room-compiler"))

    "androidTestImplementation"(catalog.lib("androidx-room-testing"))
    "androidTestImplementation"(catalog.lib("androidx-junit"))
}
