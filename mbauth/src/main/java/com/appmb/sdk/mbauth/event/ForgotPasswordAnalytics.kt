package com.appmb.sdk.mbauth.event

interface ForgotPasswordAnalytics : AnalyticsProperties {
  val eventName: String
    get() = "forgotPassword"
  val requestOTP: String
    get() = "forgotPasswordRequestOTP"
  val verifyOTP: String
    get() = "forgotPasswordVerifyOTP"
}