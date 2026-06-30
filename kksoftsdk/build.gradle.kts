@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.com.appmb.android.sdk)
  id("maven-publish")
  id("kotlin-parcelize")
}

group = "com.kksoft.sdk"
version = "1.0.0"

android {
  namespace = "com.kksoft.sdk"

  buildTypes.forEach {
    it.buildConfigField("String", "LIBRARY_VERSION", "\"${project.findProperty("version")}\"")
  }
}

dependencies {
  api(project(":mbcore"))
  api(project(":mbcoreui"))
  api(project(":mbauth"))
  api(project(":mbpayment"))
  api(project(":mbtracking"))
}

publishing {
  publications {
    create<MavenPublication>("release") {
      afterEvaluate { from(components["release"]) }
      artifactId = "kksoftsdk"
    }
  }
}
