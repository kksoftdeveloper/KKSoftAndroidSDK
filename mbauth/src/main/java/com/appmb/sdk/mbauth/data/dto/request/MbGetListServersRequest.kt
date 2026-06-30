package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbGetListServersRequest(
  @SerialName("packageName") val packageName: String? = null,
  @SerialName("platform") val platform: String? = null,
  @SerialName("appVersion") val appVersion: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null,
)
