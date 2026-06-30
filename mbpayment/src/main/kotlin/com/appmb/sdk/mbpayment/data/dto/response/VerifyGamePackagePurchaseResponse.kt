package com.appmb.sdk.mbpayment.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyGamePackagePurchaseResponse(
  @SerialName("transactionCode") val transactionCode: String,
  @SerialName("point") val point: String,
)
