package com.appmb.sdk.mbauth.event

import com.appmb.sdk.mbcore.BuildConfig

interface AnalyticsProperties {
  companion object {
    val platform: String
      get() = "Android"
    val token: String
      get() = BuildConfig.ANALYTICS_EVENT_TOKEN
    val request: String
      get() = "request"
    val success: String
      get() = "success"
    val failure: String
      get() = "failure"
    val logout: String
      get() = "logout"
    val getLatestAuthSession: String
      get() = "getLatestAuthSession"
  }
}
