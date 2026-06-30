package com.appmb.sdk.mbauth.data.dto.response

import com.appmb.sdk.mbcore.model.MbAuthData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDataModel(
  val accessToken: String,
  val refreshToken: String,
  val expireDate: String,
  @SerialName("gameUId") val gameUuid: String? = null,
  val userId: String? = null,
  val isGuest: Boolean? = false,
  val serverId: String? = null,
  val userBlocked: Boolean? = false,
  val gameBlocked: Boolean? = false,
  val serverBlocked: Boolean? = false,
) {
  fun toEntity(): MbAuthData {
    return MbAuthData(
      accessToken = accessToken,
      refreshToken = refreshToken,
      expireDate = expireDate,
      gameUuid = gameUuid,
      userId = userId,
      isGuest = isGuest,
      serverId = serverId,
      userBlocked = serverBlocked,
      gameBlocked = gameBlocked,
      serverBlocked = serverBlocked
    )
  }
}
