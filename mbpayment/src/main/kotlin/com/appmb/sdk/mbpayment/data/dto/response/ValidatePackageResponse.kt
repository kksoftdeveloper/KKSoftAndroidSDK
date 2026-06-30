package com.appmb.sdk.mbpayment.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidatePackageResponse(
  @SerialName("sku") val sku: String,
  @SerialName("status") val status: String,
  @SerialName("price") val price: String,
  @SerialName("point") val point: String,
)
