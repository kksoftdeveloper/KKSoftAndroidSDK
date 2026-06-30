package com.appmb.sdk.mbpayment.data.dto

sealed interface PurchaseStatus {
  object Idle : PurchaseStatus
  data class Success(
    val productName: String,
    val sku: String,
    val orderId: String? = null,
    val purchaseToken: String
  ) : PurchaseStatus

  object UserCancelled : PurchaseStatus
  object BillingUnavailable : PurchaseStatus
  object ProductUnavailable : PurchaseStatus
  object ProductUnavailableInGameServer : PurchaseStatus
  object Error : PurchaseStatus
}

fun PurchaseStatus.toMixpanelEvent(): String {
  return when (this) {
    is PurchaseStatus.Success -> "purchaseSuccess"
    PurchaseStatus.UserCancelled -> "purchaseUserCancelled"
    PurchaseStatus.BillingUnavailable -> "purchaseBillingUnavailable"
    PurchaseStatus.ProductUnavailable -> "purchaseProductUnavailable"
    PurchaseStatus.ProductUnavailableInGameServer -> "purchaseProductUnavailableInGameServer"
    PurchaseStatus.Error -> "purchaseError"
    PurchaseStatus.Idle -> "purchaseIdle"
  }
}