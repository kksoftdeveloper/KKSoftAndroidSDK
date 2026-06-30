package com.appmb.sdk.mbpayment.domain

import android.app.Activity
import com.android.billingclient.api.BillingResult
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
  suspend fun connect()
  suspend fun loadProducts(productIds: List<String>): List<GoogleBillingProduct>
  suspend fun launchPurchase(activity: Activity, productId: String): BillingResult
  fun observePurchasesStatus(): Flow<PurchaseStatus>
  fun isClientReady(): Boolean
  fun resetPurchaseStatus()
}