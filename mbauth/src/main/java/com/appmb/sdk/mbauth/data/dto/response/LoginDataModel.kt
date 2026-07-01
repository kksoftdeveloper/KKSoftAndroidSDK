package com.appmb.sdk.mbauth.data.dto.response

import com.appmb.sdk.mbcore.model.MbAuthData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginDataModel(
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val expireDate: String? = null,
  @SerialName("gameUId") val gameUuid: String? = null,
  val userId: String? = null,
  val refreshExpireDate: String? = null,
  val isGuest: Boolean? = false,
  val serverId: String? = null,
  val userBlocked: Boolean? = false,
  val gameBlocked: Boolean? = false,
  val serverBlocked: Boolean? = false,
  val identityVerificationRequired: Boolean? = false
) {
  fun toEntity(): MbAuthData {
    return MbAuthData(
      accessToken = accessToken,
      refreshToken = refreshToken,
      expireDate = expireDate,
      gameUuid = gameUuid,
      userId = userId,
      refreshExpireDate = refreshExpireDate,
      isGuest = isGuest,
      serverId = serverId,
      userBlocked = userBlocked,
      gameBlocked = gameBlocked,
      serverBlocked = serverBlocked,
      identityVerificationRequired = identityVerificationRequired
    )
  }
}
