import com.android.build.gradle.LibraryExtension
import com.appmb.build_logic.convention.configureSdkCompose
import com.appmb.build_logic.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidSdkConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply {
        apply("com.appmb.android.library")
//        apply("org.jetbrains.kotlin.plugin.compose")
      }
      extensions.configure<LibraryExtension> {
        defaultConfig {
        }
        testOptions.animationsDisabled = true
      }
      val extension = extensions.getByType<LibraryExtension>()
      configureSdkCompose(extension)
      dependencies {
//        add(
//          "api",
//          libs.findLibrary("mb-core-sdk").get()
//        )
        add("api", project(":mbcore"))
      }
    }
  }
}
