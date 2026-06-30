package com.appmb.sdk.mbcore.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbServer(
  @SerialName("serverId") val serverId: String? = null,
  @SerialName("serverName") val serverName: String? = null,
  @SerialName("serverClientId") val serverClientId: String? = null,
  @SerialName("status") val status: String? = SERVER_OFFLINE,
) {
  companion object {
    const val SERVER_GOOD = "ONLINE"
    const val SERVER_NEW = "NEW"
    const val SERVER_FULL = "FULL"
    const val SERVER_MAINTENANCE = "MAINTENANCE"
    const val SERVER_OFFLINE = "OFFLINE"
  }
}