package com.appmb.sdk.mbcore.data.dto.response

import android.annotation.SuppressLint
import com.appmb.sdk.mbcore.model.MbAuthData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MbRefreshTokenResponse(
  @SerialName("accessToken") val accessToken: String? = null,
  @SerialName("refreshToken") val refreshToken: String? = null,
  @SerialName("expireDate") val expireDate: String? = null,
) {
  fun updateMbAuthData(mbAuthData: MbAuthData): MbAuthData = mbAuthData.copy(
    accessToken = this.accessToken,
    refreshToken = this.refreshToken,
    expireDate = this.expireDate,
  )
}
