package com.appmb.sdk.mbauth.event

interface OTPAnalytics : AnalyticsProperties {

  companion object {
    @JvmStatic
    val requestOTP: String
      get() = "requestOTP"

    @JvmStatic
    val verifyOTP: String
      get() = "verifyOTP"

    @JvmStatic
    val request: String
      get() = "request"

    @JvmStatic
    val success: String
      get() = "success"

    @JvmStatic
    val failure: String
      get() = "failure"

    @JvmStatic
    val message: String
      get() = "message"
  }
}