package com.appmb.sdk.mbpayment.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class PurchaseResult : Parcelable {

  @Parcelize
  data class PurchasedSuccess(val productName: String) : PurchaseResult()

  @Parcelize
  object PurchasedFailure : PurchaseResult()

  @Parcelize
  object PurchasedUserCancel : PurchaseResult()

  @Parcelize
  object ClosedProductList : PurchaseResult()

  @Parcelize
  object PurchasedError : PurchaseResult()

  @Parcelize
  object PurchasedUnavailableInSelectedServer : PurchaseResult()

  @Parcelize
  object PurchasedUserNotAuthenticated : PurchaseResult()
}