import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.plugin.devel.tasks.ValidatePlugins

plugins {
  `kotlin-dsl`
//  `kotlin-dsl-precompiled-script-plugins`
  `java-gradle-plugin`
}

group = "com.appmb.buildlogic"

repositories {
  google()
  mavenCentral()
  gradlePluginPortal()
}

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.android.gradlePlugin)
//  compileOnly(libs.android.gradlePlugin)
//  compileOnly(libs.android.tools.common)
//  compileOnly(libs.kotlin.gradlePlugin)
}

tasks.named<ValidatePlugins>("validatePlugins") {
  enableStricterValidation.set(true)
  failOnWarning.set(true)
}

gradlePlugin {
  plugins {
    register("androidApplicationCompose") {
      id = "com.appmb.android.application.compose"
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "com.appmb.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = "com.appmb.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidSdk") {
      id = "com.appmb.android.sdk"
      implementationClass = "AndroidSdkConventionPlugin"
    }
  }
}
