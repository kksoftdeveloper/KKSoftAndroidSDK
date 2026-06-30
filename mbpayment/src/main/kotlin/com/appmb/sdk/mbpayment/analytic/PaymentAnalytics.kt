package com.appmb.sdk.mbpayment.analytic

interface PaymentAnalytics {
  companion object {
    @JvmStatic
    val request: String
      get() = "request"

    @JvmStatic
    val success: String
      get() = "success"

    @JvmStatic
    val failure: String
      get() = "failure"

    @JvmStatic
    val getListProductIds: String
      get() = "getListProductIds"

    @JvmStatic
    val clickToBuyAProduct: String
      get() = "clickToBuyAProduct"

    @JvmStatic
    val purchaseValidation: String
      get() = "purchaseValidation"

    @JvmStatic
    val purchaseVerification: String
      get() = "purchaseVerification"
  }
}