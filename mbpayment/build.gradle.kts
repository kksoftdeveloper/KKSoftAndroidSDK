@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.com.appmb.android.sdk)
//  alias(libs.plugins.parcelize)
  alias(libs.plugins.kotlin.android)
  id("maven-publish")
  id("kotlin-parcelize")
}

group = "com.appmb.sdk"
version = "1.0.0"

publishing {
  publications {
    create<MavenPublication>("release") {
      afterEvaluate { from(components["release"]) }
      artifactId = "mbpayment"
    }
  }
  repositories {
    maven {
      name = "AndroidSDK"
      url = uri("https://maven.pkg.github.com/knmobileapp/android-Game-SDK")
    }
  }
}

android {
  namespace = "com.appmb.sdk.mbpayment"

  defaultConfig {
    minSdk = 23
  }

  buildTypes.forEach {
    it.buildConfigField("String", "LIBRARY_VERSION", "\"${project.findProperty("version")}\"")
  }

  testOptions {
    unitTests {
      isReturnDefaultValues = true // Suppress "not mocked" warnings for Android classes
    }
  }
}

dependencies {
//  implementation(libs.androidx.appcompat)
  implementation(libs.google.billing)
  implementation(libs.google.play.services.base)
  implementation("androidx.compose.material:material:1.4.0-beta01")
  implementation(project(":mbcoreui"))
  implementation(project(":mbauth"))
  implementation(project(":mbtracking"))

  // Test dependencies
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlinx.coroutines.test)
}
