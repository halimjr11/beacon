import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/** Build-wide constants. Change here, every module follows. */
object BeaconBuild {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 24
    const val JVM = 17
    const val NAMESPACE_ROOT = "com.halimjr11.beacon"
}

val Project.catalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.lib(alias: String): Provider<out Dependency> = findLibrary(alias).get()
fun VersionCatalog.bundle(alias: String) = findBundle(alias).get()

/** Module name is the namespace suffix: `:core:network` -> `com.halimjr11.beacon.network` (names are unique). */
fun Project.namespaceSuffix(): String = name
