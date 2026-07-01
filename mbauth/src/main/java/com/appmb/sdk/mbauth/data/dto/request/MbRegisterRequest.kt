package com.appmb.sdk.mbauth.data.dto.request

import com.appmb.sdk.mbauth.data.dto.request.MbLoginGuestRequest.Companion.PLATFORM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MbRegisterRequest(
  @SerialName("deviceId") val deviceId: String? = null,
  @SerialName("device") val device: String? = null,
  @SerialName("gameId") val gameId: Int? = null,
  @SerialName("type") val type: String? = "phone",
  @SerialName("phone") val phone: String?,
  @SerialName("password") val password: String?,
  @SerialName("platform") val platform: String? = PLATFORM,
  @SerialName("otpVerifiedToken") val otpVerifiedToken: String? = null,
  @SerialName("sdkVersion") val sdkVersion: String? = null,
  @SerialName("appVersion") val appVersion: String? = null,
  @SerialName("serverId") val serverId: Int? = null,
  @SerialName("adid") val adid: String? = null,
  @SerialName("fullName") val fullName: String? = null,
  @SerialName("dateOfBirth") val dateOfBirth: String? = null,
  @SerialName("gender") val gender: String? = null,
  @SerialName("address") val address: String? = null,
  @SerialName("consent") val consent: MbRegisterConsentRequest? = null,
  @SerialName("guardian") val guardian: MbRegisterGuardianRequest? = null,
  @SerialName("sign") val sign: String? = null
)

@Serializable
data class MbRegisterConsentRequest(
  @SerialName("legalAccepted") val legalAccepted: Boolean,
  @SerialName("selfRegistrationAgeConfirmed") val selfRegistrationAgeConfirmed: Boolean,
)

@Serializable
data class MbRegisterGuardianRequest(
  @SerialName("fullName") val fullName: String,
  @SerialName("dateOfBirth") val dateOfBirth: String,
  @SerialName("phone") val phone: String,
  @SerialName("address") val address: String? = null,
  @SerialName("otpVerifiedToken") val otpVerifiedToken: String? = null,
)
