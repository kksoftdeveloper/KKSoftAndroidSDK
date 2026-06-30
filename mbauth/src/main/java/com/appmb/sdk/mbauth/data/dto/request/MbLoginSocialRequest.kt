package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbLoginSocialRequest(
  @SerialName("type")
  val type: String? = null,
  @SerialName("deviceId")
  val deviceId: String? = null,
  @SerialName("token")
  val token: String? = null,
  @SerialName("gameId")
  val gameId: String? = null,
  @SerialName("serverId")
  val serverId: Int? = null,
  @SerialName("sign")
  val sign: String? = null,
  @SerialName("sdkVersion")
  val sdkVersion: String? = null,
  @SerialName("platform")
  val platform: String? = null,
  @SerialName("appVersion")
  val appVersion: String? = null,
  @SerialName("adid")
  val adid: String? = null
) {

  companion object {
    const val TYPE_GOOGLE = "google"
    const val TYPE_FACEBOOK = "facebook"
  }
}
