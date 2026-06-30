package com.appmb.sdk.mbpayment.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyGamePackagePurchaseRequest(
  @SerialName("sku") val sku: String,
  @SerialName("orderId") val orderId: String,
  @SerialName("purchaseToken") val purchaseToken: String,
  @SerialName("gameId") val gameId: Int,
  @SerialName("serverId") val serverId: Int?,
  @SerialName("platform") val platform: String,
  @SerialName("appVersion") val appVersion: String,
  @SerialName("sdkVersion") val sdkVersion: String,
  @SerialName("sign") val sign: String,
)
