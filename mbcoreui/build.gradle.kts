@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.com.appmb.android.sdk)
//  alias(libs.plugins.parcelize)
  alias(libs.plugins.kotlin.android)
  id("maven-publish")
  id("kotlin-parcelize")
}

apply(from = "$rootDir/build-logic/convention/src/main/kotlin/publish.gradle.kts")

android {
  namespace = "com.appmb.sdk.mbcoreui"

  buildTypes.forEach {
    it.buildConfigField("String", "LIBRARY_VERSION", "\"${project.findProperty("version")}\"")
  }
}

dependencies {
//  api(
//    group = emptyString,
//    name = LibsExt.designSystem.name(),
//    ext = LibsExt.designSystem.ext(),
//  )

  api(libs.androidx.appcompat)
  api(libs.material)

  api(libs.androidx.lifecycle.runtime.ktx)
  api(libs.androidx.activity.compose)
  api(platform(libs.androidx.compose.bom))
  api(libs.androidx.ui)
  api(libs.androidx.ui.graphics)
  api(libs.androidx.ui.tooling.preview)
  api(libs.androidx.material3)
  api(libs.androidx.ui.tooling)
  api(libs.koin.compose)
  // androidx-material3
  api(libs.androidx.material3)
//  androidx-pull-to-refresh
//  api(libs.androidx.pull.to.refresh)

  api(libs.navigation.compose)

  api(platform(libs.firebase.bom))
  api(libs.firebase.analytics) // Firebase Analytics
  api(libs.firebase.crashlytics) // Firebase Crashlytics
}
