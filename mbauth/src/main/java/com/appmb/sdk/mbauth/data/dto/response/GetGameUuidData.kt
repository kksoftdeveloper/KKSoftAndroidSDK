package com.appmb.sdk.mbauth.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGameUuidData(
  @SerialName("gameUId") val gameUuid: String? = null,
  @SerialName("characterId") val characterId: String? = null,
)