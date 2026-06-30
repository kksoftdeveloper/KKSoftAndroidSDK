package com.appmb.sdk.mbauth.event

interface LogoutAnalytics {
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
    val message: String
      get() = "message"

    @JvmStatic
    val eventName: String
      get() = "logout"
  }
}
