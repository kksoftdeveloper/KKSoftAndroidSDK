package com.appmb.sdk.mbauth.data.dto.request

import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest.Companion.PLATFORM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbResetPasswordRequest(
  @SerialName("deviceId") val deviceId: String? = null,
  @SerialName("type") val type: String? = "phone",
  @SerialName("phone") val phone: String?,
  @SerialName("password") val password: String?,
  @SerialName("platform") val platform: String? = PLATFORM,
  @SerialName("otpVerifiedToken") val otpVerifiedToken: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null,
  @SerialName("appVersion") val appVersion: String? = null,
)
