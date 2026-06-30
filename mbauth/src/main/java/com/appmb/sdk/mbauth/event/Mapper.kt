package com.appmb.sdk.mbauth.event

import com.appmb.sdk.mbauth.core.provider.LoginType.EMAIL
import com.appmb.sdk.mbauth.core.provider.LoginType.FACEBOOK
import com.appmb.sdk.mbauth.core.provider.LoginType.GOOGLE
import com.appmb.sdk.mbauth.core.provider.LoginType.GUEST
import com.appmb.sdk.mbauth.core.provider.LoginType.PHONE
import com.appmb.sdk.mbauth.model.MbAuthParams


fun MbAuthParams.toAnalyticsEventName(): String {
  return when (this.type) {
    EMAIL -> LoginAnalytics.emailLogin
    PHONE -> LoginAnalytics.phoneLogin
    GUEST -> LoginAnalytics.guestLogin
    GOOGLE -> LoginAnalytics.googleLogin
    FACEBOOK -> LoginAnalytics.facebookLogin
    null -> "Login Type is null"
  }
}

fun MbAuthParams.toAnalyticsProperties(): Map<String, Any?> {
  return mapOf(
    "type" to this.type?.name,
    "serverId" to (this.serverId),
    "osVersion" to (this.osVersion),
    "gameId" to (this.gameId),
    "appVersion" to (this.appVersion),
    "appPackageName" to (this.appPackageName),
    "email" to (this.email),
    "phone" to (this.phone),
    "password" to (this.password),
    "otp" to (this.otp),
    "otpType" to (this.otpType),
    "linkAccountType" to (this.linkAccountType),
    "googleAccount" to (this.googleAccount)
  )
}