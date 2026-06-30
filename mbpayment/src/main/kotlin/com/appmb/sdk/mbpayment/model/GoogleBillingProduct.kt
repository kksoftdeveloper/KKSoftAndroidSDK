package com.appmb.sdk.mbpayment.model

import com.android.billingclient.api.ProductDetails

data class GoogleBillingProduct(
  val productId: String,
  val name: String,
  val points: String, // Amount of points (60 coins,....)
  val pointUnit: String, // Unit in-game: gems, coins,.....
  val price: String, // Price of product
  val formattedPrice: String,
  val supportedVNMarketPlace: Boolean = true,
  val supportedUser: Boolean = true, // If product is supported for user
  val currency: String,
  val purchaseToken: String? = null,
  val productDetails: ProductDetails? = null,
)