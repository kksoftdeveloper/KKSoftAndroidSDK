package com.appmb.sdk.mbauth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpData(
  val otpSent: Boolean? = null,
  val expiresInSeconds: Int? = null,
  val retryAfterSeconds: Int? = null,
)