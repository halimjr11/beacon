// Koin Annotations: @Module / @Single / @Factory / @KoinViewModel + the KSP compiler that generates
// the Koin modules. Apply next to an Android plugin of the module that declares DI definitions.
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    "implementation"(catalog.lib("koin-annotations"))
    "ksp"(catalog.lib("koin-ksp-compiler"))
}
