package com.appmb.sdk.mbauth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpData(
  val otpVerifiedToken: String? = null,
  val expiresInSeconds: Int? = null
)
