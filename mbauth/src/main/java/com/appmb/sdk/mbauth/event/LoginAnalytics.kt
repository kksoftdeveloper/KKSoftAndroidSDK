package com.appmb.sdk.mbauth.event

interface LoginAnalytics {
  companion object {
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
    val guestLogin: String
      get() = "guestLogin"

    @JvmStatic
    val phoneLogin: String
      get() = "phoneLogin"

    @JvmStatic
    val facebookLogin: String
      get() = "facebookLogin"

    @JvmStatic
    val googleLogin: String
      get() = "googleLogin"

    @JvmStatic
    val emailLogin: String
      get() = "emailLogin"
  }
}
