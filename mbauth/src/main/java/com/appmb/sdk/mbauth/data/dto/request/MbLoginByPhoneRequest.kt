package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbLoginByPhoneRequest(
  @SerialName("appVersion")
  val appVersion: String? = null,
  @SerialName("deviceId")
  val deviceId: String? = null,
  @SerialName("gameId")
  val gameId: String? = null,
  @SerialName("serverId")
  val serverId: Int? = null,
  @SerialName("password")
  val password: String? = null,
  @SerialName("phone")
  val phone: String?,
  @SerialName("platform")
  val platform: String? = null,
  @SerialName("sdkVersion")
  val sdkVersion: String? = null,
  @SerialName("sign")
  val sign: String? = null,
  @SerialName("type")
  val type: String? = TYPE_PHONE,
  @SerialName("adid")
  val adid: String? = null
) {
  companion object {
    const val TYPE_PHONE = "phone"
  }
}

fun MbLoginByPhoneRequest.toMap(): Map<String, Any?> {
  return mapOf(
    "appVersion" to (appVersion),
    "deviceId" to (deviceId),
    "gameId" to (gameId),
    "serverId" to (serverId),
    "password" to (password),
    "phone" to (phone),
    "platform" to (platform),
    "sdkVersion" to (sdkVersion),
    "sign" to (sign),
    "type" to type,
  )
}