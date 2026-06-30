package com.appmb.sdk.mbauth.event

interface SignupAnalytics : AnalyticsProperties {

  companion object {
    @JvmStatic
    val phoneSignup: String
      get() = "phoneSignup"
    @JvmStatic
    val linkToPhoneAccount: String
      get() = "linkToPhoneAccount"
    @JvmStatic
    val linkToFacebookAccount: String
      get() = "linkToFacebookAccount"
    @JvmStatic
    val linkToGoogleAccount: String
      get() = "linkToGoogleAccount"
    @JvmStatic
    val linkToSocialAccount: String
      get() = "linkToSocialAccount"
    @JvmStatic
    val request: String
      get() = "request"
    @JvmStatic
    val success: String
      get() = "success"
    @JvmStatic
    val failure: String
      get() = "failure"
  }
}