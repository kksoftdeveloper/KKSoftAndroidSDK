package com.appmb.sdk.mbpayment.ui

import com.appmb.sdk.mbpayment.model.PurchaseResult

sealed interface ProductListEvent {

  data class Error(val code: Int) : ProductListEvent

  object TokenExpired : ProductListEvent

  object PurchasedIdle : ProductListEvent

  object ClosedProductList : ProductListEvent

  data class PurchasedSuccess(
    val productName: String,
    val sku: String,
    val orderId: String? = null,
    val purchaseToken: String
  ) : ProductListEvent

  object PurchasedFailure : ProductListEvent

  object PurchasedUserCancel : ProductListEvent
  object PurchasedUnavailableProduct : ProductListEvent

  object PurchasedUnavailableInSelectedServer : ProductListEvent
  object PurchasedUnavailableBilling : ProductListEvent

  data class PurchasedError(val code: Int) : ProductListEvent
}

fun ProductListEvent.toPurchaseResult(): PurchaseResult? {
  return when (this) {
    is ProductListEvent.PurchasedSuccess -> PurchaseResult.PurchasedSuccess(productName)
    ProductListEvent.PurchasedFailure -> PurchaseResult.PurchasedFailure
    ProductListEvent.PurchasedUserCancel -> PurchaseResult.PurchasedUserCancel
    ProductListEvent.PurchasedUnavailableProduct -> PurchaseResult.PurchasedError
    ProductListEvent.PurchasedUnavailableInSelectedServer -> PurchaseResult.PurchasedUnavailableInSelectedServer
    ProductListEvent.PurchasedUnavailableBilling -> PurchaseResult.PurchasedError
    is ProductListEvent.Error -> PurchaseResult.PurchasedError
    ProductListEvent.PurchasedIdle -> PurchaseResult.PurchasedError
    ProductListEvent.ClosedProductList -> PurchaseResult.ClosedProductList
    is ProductListEvent.PurchasedError -> PurchaseResult.PurchasedError
    else -> null
  }
}