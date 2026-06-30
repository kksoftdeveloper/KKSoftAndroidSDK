package com.appmb.sdk.mbcore.di

object MbSdkQualifiers {
  object Common {
    const val BASE_URL = "BASE_URL"
  }

  object Auth {
    const val BASE_URL = Common.BASE_URL
    const val NETWORK_CLIENT = "NETWORK_CLIENT_AUTH"
  }

  object Payment {
    const val BASE_URL = Common.BASE_URL
    const val NETWORK_CLIENT = "NETWORK_CLIENT_PAYMENT"
  }

  object Event {
    const val BASE_URL = Common.BASE_URL
    const val NETWORK_CLIENT = "NETWORK_CLIENT_EVENT"
  }
}
