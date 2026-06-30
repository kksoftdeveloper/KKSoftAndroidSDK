package com.appmb.sdk.mbauth.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordDataModel(
  val success: Boolean? = false,
)
