package com.appmb.build_logic.convention

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


/**
 * Configure Compose-specific options
 */
internal fun Project.configureSdkCompose(
  libraryExtension: LibraryExtension,
) {
  libraryExtension.apply {
    buildFeatures {
      compose = true
    }

    composeOptions {
      kotlinCompilerExtensionVersion = libs.findVersion("androidxComposeCompiler").get().toString()
    }

    dependencies {
      val bom = libs.findLibrary("androidx-compose-bom").get()
      add("implementation", platform(bom))
      add("androidTestImplementation", platform(bom))
      add(
        "implementation",
        libs.findLibrary("androidx-ui-tooling-preview").get()
      )
      add(
        "debugImplementation",
        libs.findLibrary("androidx-ui-tooling").get()
      )
    }

    testOptions {
      unitTests {
        // For Robolectric
        isIncludeAndroidResources = true
      }
    }

  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      freeCompilerArgs.addAll(buildComposeMetricsParameters())
//      freeCompilerArgs.addAll(stabilityConfiguration())
    }
  }

}