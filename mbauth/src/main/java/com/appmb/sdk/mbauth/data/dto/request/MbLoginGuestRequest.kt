package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbLoginGuestRequest(
  @SerialName("sign")
  val sign: String? = null,
  @SerialName("type")
  val type: String? = TYPE_GUEST,
  @SerialName("deviceId")
  val deviceId: String? = null,
  @SerialName("platform")
  val platform: String? = PLATFORM,
  @SerialName("sdkVersion")
  val sdkVersion: String? = null,
  @SerialName("gameId")
  val gameId: String? = null,
  @SerialName("serverId")
  val serverId: Int? = null,
  @SerialName("appVersion")
  val appVersion: String? = null,
  @SerialName("adid")
  val adid: String? = null
) {
  companion object {
    const val TYPE_GUEST = "guest"
    const val PLATFORM = "android"
  }
}