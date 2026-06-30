package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbLinkSocialAccountRequest(
  // This token is social token
  @SerialName("token") val token: String? = null,
  @SerialName("type") val type: String? = null,
  @SerialName("deviceId") val deviceId: String? = null,
  @SerialName("sign") val sign: String? = null,
  @SerialName("gameId") val gameId: Int? = null,
  @SerialName("serverId") val serverId: Int? = null,
  @SerialName("platform") val platform: String = "android",
  @SerialName("appVersion") val appVersion: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null
)