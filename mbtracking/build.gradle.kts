plugins {
  id("com.android.library")
  kotlin("android")
  id("maven-publish")
}

group = "com.appmb.sdk"
version = "1.0.0"

android {
  namespace = "com.appmb.sdk.mbtracking"
  compileSdk = 34

  defaultConfig {
    minSdk = 23
    targetSdk = 34
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
    debug {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {
  api(project(":mbcore"))
  implementation(libs.koin.android)
  api(platform(libs.firebase.bom))
  api(libs.firebase.analytics) // Firebase Analytics
  api(libs.firebase.crashlytics) // Firebase Crashlytics
  implementation(libs.play.services.ads.identifier)
  // Adjust
  api(libs.adjust.android)
  api(libs.adjust.android.huawei.referrer)
  api(libs.adjust.android.xiaomi.referrer)
  api(libs.adjust.android.samsung.referrer)
  api(libs.installreferrer)
  implementation(libs.tiktok.business.sdk)
  implementation(libs.facebook.login)
  implementation(libs.facebook.core)
}

publishing {
  publications {
    create<MavenPublication>("release") {
      afterEvaluate { from(components["release"]) }
      artifactId = "mbtracking"
    }
  }
}
