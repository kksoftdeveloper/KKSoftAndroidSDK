package com.appmb.build_logic.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import java.util.Properties

private const val SDK_DEVICE_SECRET_ID = "iZbR051Ox6NBcTYryGiP8AJ7W5TuEdyIdwxPxg9bL6KW"
private const val SDK_MIXPANEL_TOKEN = "ddb99903ca5b060afe6440ada8a121ee"

internal fun Project.configureEnvironment(
    commonExtension: CommonExtension<*, *, *, *>,
) {
    val localPropertiesFile = rootProject.file("local.properties")
    val localProperties = Properties().apply {
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { load(it) }
        }
    }

    val configuredBaseUrl = localProperties["base.url"]?.toString()?.trim().orEmpty()

    // Environment detection
    val isStaging = localProperties
        .getProperty("staging")
        ?.equals("true", ignoreCase = true)
        ?: false
    val isProduction = !isStaging

    // Environment URLs
    val baseUrl = configuredBaseUrl.ifBlank {
        if (isStaging) "https://api-staging.kksoft.vn" else "https://api.kksoft.vn"
    }

    commonExtension.apply {
        buildTypes.forEach {
            it.buildConfigField("Boolean", "IS_PRODUCTION", isProduction.toString())
            it.buildConfigField("Boolean", "IS_STAGING", isStaging.toString())
            it.buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
            it.buildConfigField("String", "AUTH_BASE_URL", "\"$baseUrl\"")
            it.buildConfigField("String", "PAYMENT_BASE_URL", "\"$baseUrl\"")
            it.buildConfigField("String", "EVENT_BASE_URL", "\"$baseUrl\"")
            
            it.buildConfigField("String", "ANALYTICS_EVENT_TOKEN", "\"$SDK_MIXPANEL_TOKEN\"")
            it.buildConfigField("String", "DEVICE_SECRET_ID", "\"$SDK_DEVICE_SECRET_ID\"")
        }
    }
}
