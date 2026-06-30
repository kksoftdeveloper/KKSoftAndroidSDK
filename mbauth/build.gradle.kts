@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.com.appmb.android.sdk)
//  alias(libs.plugins.parcelize)
  id("kotlin-parcelize")
  id("maven-publish")
}
apply(from = "$rootDir/build-logic/convention/src/main/kotlin/publish.gradle.kts")

android {
  namespace = "com.appmb.sdk.mbauth"

  buildTypes.forEach {
    it.buildConfigField("String", "LIBRARY_VERSION", "\"${project.findProperty("version")}\"")
  }
}


dependencies {
//  implementation(
//    group = emptyString,
//    name = LibsExt.designSystem.name(),
//    ext = LibsExt.designSystem.ext(),
//  )
  implementation(libs.androidx.credential)
  implementation(libs.androidx.credential.service.auth)
  implementation(libs.androidx.identity.googleid)
  implementation(libs.androidx.work)
  implementation(libs.facebook.login)
//  implementation(libs.mb.core.ui.sdk)
  implementation(project(":mbcoreui"))
  implementation(project(":mbcore"))
  implementation(project(":mbtracking"))
  implementation(libs.android.google.signin)
  implementation(libs.mixpanel.android)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.analytics) // Firebase Analytics
  implementation(libs.firebase.crashlytics) // Firebase Crashlytics

  // Test dependencies
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlinx.coroutines.test)
}
