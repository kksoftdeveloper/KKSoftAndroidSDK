package com.appmb.build_logic.convention

/**
 * This is shared between :app and :benchmarks module to provide configurations type safety.
 */
enum class AndroidBuildType(val applicationIdSuffix: String? = null) {
  DEBUG(".debug"),
  RELEASE,
}
