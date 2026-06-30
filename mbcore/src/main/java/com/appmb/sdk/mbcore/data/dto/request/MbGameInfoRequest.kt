package com.appmb.sdk.mbcore.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MbGameInfoRequest(
  @SerialName("packageName") val packageName: String? = null,
  @SerialName("deviceId") val deviceId: String? = null,
  @SerialName("platform") val platform: String? = null,
  @SerialName("appVersion") val appVersion: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null,
  @SerialName("timestamp") val timestamp: Long? = null,
  @SerialName("sign") val sign: String? = null,
)

fun MbGameInfoRequest.toMap(): Map<String, Any?> = mapOf(
  "packageName" to packageName,
  "deviceId" to deviceId,
  "platform" to platform,
  "appVersion" to appVersion,
  "sdkVersion" to sdkVersion,
  "timestamp" to timestamp,
  "sign" to sign
)
