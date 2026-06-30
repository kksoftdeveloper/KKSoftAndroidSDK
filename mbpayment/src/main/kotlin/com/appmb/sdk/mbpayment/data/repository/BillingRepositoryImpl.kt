package com.appmb.sdk.mbpayment.data.repository

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.appmb.sdk.mbauth.data.datasource.MbCommonLocalDataSource
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonLocalDataSource
import com.appmb.sdk.mbcoreui.utils.CommonUtils
import com.appmb.sdk.mbpayment.data.datasource.BillingDataSource
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.domain.BillingRepository
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BillingRepositoryImpl(
  private val billingDataSource: BillingDataSource,
  private val commonData: MbCoreCommonDataSource
) : BillingRepository {
  private val _products = MutableStateFlow<List<GoogleBillingProduct>>(emptyList())

  private val _purchaseStatus: StateFlow<PurchaseStatus> = billingDataSource.purchaseStatus
    .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(), PurchaseStatus.Idle)

  override suspend fun connect() {
    val result = billingDataSource.startConnection()
    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
      throw IllegalStateException("Billing setup failed: ${result.debugMessage}")
    }
  }

  override suspend fun loadProducts(productIds: List<String>): List<GoogleBillingProduct> {
    val details = billingDataSource.queryProducts(productIds)
    _products.value = details.map { productDetail ->
      val (points, pointUnit) = CommonUtils.extractNumberAndText(productDetail.description)
      Log.i("TAG:::","product-description: ${productDetail.description}")
      Log.i("TAG:::","product-name: ${productDetail.name}")
      GoogleBillingProduct(
        productId = productDetail.productId,
        name = productDetail.name,
        points = points.toString(),
        pointUnit = pointUnit,
        price = (productDetail.oneTimePurchaseOfferDetails?.priceAmountMicros?.div(1_000_000.0)
          ?: 0).toString(),
        formattedPrice = productDetail.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
        currency = productDetail.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "VND",
        supportedVNMarketPlace = productDetail.oneTimePurchaseOfferDetails?.priceCurrencyCode == "VND",
        supportedUser = commonData.isGuestUser() == false,
        productDetails = productDetail
      )
    }.run {
      sortedBy { it.points.toDoubleOrNull() ?: 0.0 }
    }
    return _products.value
  }

  override suspend fun launchPurchase(activity: Activity, productId: String): BillingResult {
    val details = _products.value.first { it.productId == productId }.productDetails
    details?.let {
      return billingDataSource.launchPurchase(activity, it)
    }
    throw IllegalStateException("Not found product details for id: $productId")
  }

  override fun observePurchasesStatus(): Flow<PurchaseStatus> = _purchaseStatus

  override fun isClientReady(): Boolean = billingDataSource.isReady()

  override fun resetPurchaseStatus() {
    billingDataSource.resetPurchaseStatus()
  }
}