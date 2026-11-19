plugins {
    id("beacon.android.library")
    id("beacon.android.koin")
}

dependencies {
    // api: NetworkResult, Flow and PagingData are part of NewsRepository's public surface
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)
}
