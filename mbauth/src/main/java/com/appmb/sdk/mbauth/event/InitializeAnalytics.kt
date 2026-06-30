package com.appmb.sdk.mbauth.event

interface InitializeAnalytics : AnalyticsProperties {
  val eventName: String
    get() = "initAuthSDK"
}