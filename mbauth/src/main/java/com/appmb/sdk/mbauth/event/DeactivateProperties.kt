package com.appmb.sdk.mbauth.event

interface DeactivateProperties : AnalyticsProperties {
  val deactivateAccount: String
    get() = "deactivateAccount"
}