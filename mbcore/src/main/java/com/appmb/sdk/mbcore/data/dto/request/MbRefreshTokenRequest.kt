package com.appmb.sdk.mbcore.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbRefreshTokenRequest(
  @SerialName("deviceId") val deviceId: String? = null,
  @SerialName("platform") val platform: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null,
  @SerialName("appVersion") val appVersion: String? = null,
  @SerialName("gameId") val gameId: String? = null,
  @SerialName("serverId") val serverId: Int? = null,
  @SerialName("refreshToken") val refreshToken: String? = null,
  @SerialName("sign") val sign: String? = null,
)
