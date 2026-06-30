package com.appmb.sdk.mbpayment.model

/**
 * Class representing the parameters required for payment.
 */
class MbPaymentParams {
  // Common fields
  internal var sku: String? = null
  internal var price: String? = null
  internal var orderId: String? = null
  internal var purchaseToken: String? = null

  private constructor()

  /**
   * Builder class for constructing `MbAuthParams` instances.
   */
  class Builder(
    private var sku: String? = null,
    private var price: String? = null,
    private var orderId: String? = null,
    private var purchaseToken: String? = null,
  ) {
    /**
     * Builds and returns an `MbPaymentParams` instance.
     * @return The constructed `MbPaymentParams` instance.
     */
    fun build() = MbPaymentParams().apply {
      sku = this@Builder.sku
      price = this@Builder.price
      purchaseToken = this@Builder.purchaseToken
      orderId = this@Builder.orderId
    }
  }

  companion object {

    /**
     * Builds an `MbPaymentParams` instance for validate game package
     *
     * @param sku The sku of the game package.
     * @param price The price of the game package.
     * @return The constructed `MbPaymentParams` instance.
     */
    fun buildForValidateGamePackage(
      sku: String,
      price: String,
    ) = Builder(
      sku = sku,
      price = price
    ).build()


    /**
     * Builds an `MbPaymentParams` instance for verify purchase
     *
     * @param sku The sku of the game package.
     * @param orderId The order id of the game package.
     * @param purchaseToken The purchase token of the game package to verify.
     * @return The constructed `MbPaymentParams` instance.
     */
    fun buildForVerifyPurchaseParams(
      sku: String,
      orderId: String,
      purchaseToken: String,
    ) = Builder(
      sku = sku,
      orderId = orderId,
      purchaseToken = purchaseToken
    ).build()
  }
}