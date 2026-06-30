@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.com.appmb.android.library)
  id("kotlin-parcelize")
  id("maven-publish")
}

apply(from = "../build-logic/convention/src/main/kotlin/publish.gradle.kts")

android {
  namespace = "com.appmb.sdk.mbcore"

  buildTypes.forEach {
    it.buildConfigField("String", "PLATFORM", "\"ANDROID\"")
  }
}

dependencies {
  api(libs.ktor.core)
  api(libs.ktor.client.okhttp)
  api(libs.ktor.client.negotiation)
  api(libs.ktor.client.json)
  api(libs.ktor.client.logging)
  api(libs.androidx.datastore.preferences)
  api(libs.ktor.client.auth)
  api(libs.koin.core)
  api(libs.koin.android)
  api(libs.arrow.kt.core)
  api(libs.mixpanel.android)
  api(libs.bolts.android)
  api(libs.gson)

  api(platform(libs.firebase.bom))
  api(libs.firebase.analytics) // Firebase Analytics
  api(libs.firebase.crashlytics) // Firebase Crashlytics
}
