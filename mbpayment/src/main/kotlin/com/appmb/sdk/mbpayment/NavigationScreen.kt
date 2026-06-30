package com.appmb.sdk.mbpayment

import kotlinx.serialization.Serializable

@Serializable
object BillingScreen {
  const val NAME = "BillingScreen"
}

@Serializable
data class PopupResult(
  val productName: String? = null,
  val purchaseToken: String? = null,
) : java.io.Serializable