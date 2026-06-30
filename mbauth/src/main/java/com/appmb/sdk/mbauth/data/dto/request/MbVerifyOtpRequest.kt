package com.appmb.sdk.mbauth.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbVerifyOtpRequest(
  @SerialName("deviceId") val deviceId: String,
  @SerialName("phone") val phone: String,
  @SerialName("mode") val mode: String = "SMS",
  @SerialName("type") val type: String = OTP_TYPE_REGISTRATION,
  @SerialName("timestamp") val timestamp: Long,
  @SerialName("otp") val otp: String,
  @SerialName("sign") val sign: String,
) {
  companion object {
    const val OTP_TYPE_REGISTRATION = "REGISTRATION"
    const val OTP_TYPE_FORGOT_PASSWORD = "FORGOTPASSWORD"
  }
}
