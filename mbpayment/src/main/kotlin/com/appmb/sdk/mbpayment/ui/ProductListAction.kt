package com.appmb.sdk.mbpayment.ui

import android.app.Activity

sealed interface ProductListAction {
  data class BuyProduct(val activity: Activity, val productId: String, val price: String) : ProductListAction

  object ResetPurchaseStatusState : ProductListAction
  
  object RefreshProducts : ProductListAction
  
  object LoadMoreProducts : ProductListAction
}