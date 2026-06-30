package com.appmb.sdk.mbpayment.data.datasource

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BillingDataSource(context: Context) {
  private val billingClient = BillingClient.newBuilder(context)
    .enablePendingPurchases()
    .setListener(this::onPurchasesUpdated)
    .build()

  private val _purchaseUpdates = MutableSharedFlow<List<Purchase>>(replay = 1)

  private val _purchaseStatus = MutableSharedFlow<PurchaseStatus>(replay = 1)
  val purchaseStatus: SharedFlow<PurchaseStatus> = _purchaseStatus

  private val purchasedProduct = mutableListOf<Purchase>()

  private val productDetailsList = mutableListOf<ProductDetails>()

  suspend fun startConnection(): BillingResult =
    suspendCoroutine { cont ->
      billingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) =
          cont.resume(billingResult)

        override fun onBillingServiceDisconnected() = Unit
      })
    }

  suspend fun queryProducts(productIds: List<String>): List<ProductDetails> =
    suspendCoroutine { cont ->
      val productList = productIds.map { id ->
        QueryProductDetailsParams.Product.newBuilder()
          .setProductId(id)
          .setProductType(BillingClient.ProductType.INAPP)
          .build()
      }
      val params = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()
      billingClient.queryProductDetailsAsync(params) { result, list ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
          productDetailsList.apply {
            clear()
            addAll(list)
          }
          cont.resume(productDetailsList)
        } else {
          cont.resumeWithException(Exception("Query failed: ${'$'}{result.debugMessage}"))
        }
      }
    }

  fun launchPurchase(activity: Activity, productDetails: ProductDetails): BillingResult {
    val params = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(
        listOf(
          BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .build()
        )
      )
      .build()
    _purchaseStatus.tryEmit(PurchaseStatus.Idle)
    return billingClient.launchBillingFlow(activity, params)
  }

  private fun onPurchasesUpdated(
    billingResult: BillingResult,
    purchases: MutableList<Purchase>?,
  ) {
    when (billingResult.responseCode) {
      BillingClient.BillingResponseCode.OK -> {
        // Buy successfully
        purchases?.forEach { purchase ->
          handlePurchaseSuccess(purchase)
        }
      }

      BillingClient.BillingResponseCode.USER_CANCELED -> {
        _purchaseStatus.tryEmit(PurchaseStatus.UserCancelled)
      }

      BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
        _purchaseStatus.tryEmit(PurchaseStatus.BillingUnavailable)
      }

      BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
        _purchaseStatus.tryEmit(PurchaseStatus.ProductUnavailable)
      }

      else -> {
        // Error
        _purchaseStatus.tryEmit(PurchaseStatus.Error)
      }
    }
  }

  // Handle purchase success
  private fun handlePurchaseSuccess(purchase: Purchase) {
    // Always acknowledge first
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
      purchasedProduct.add(purchase)
      _purchaseUpdates.tryEmit(purchasedProduct)

      val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()
      billingClient.acknowledgePurchase(acknowledgeParams) { ackResult ->
        when (ackResult.responseCode) {
          BillingClient.BillingResponseCode.OK -> {
            Log.d(
              "BillingDataSource",
              "---inside acknowledge purchase--- Purchase acknowledged: ${purchase.products}"
            )
            // Now consume it
//            consumePurchase(purchase)

            val productDetail = productDetailsList.first { item ->
              item.productId == purchase.products.first()
            }
            val productName = productDetail.name
            val sku = productDetail.productId
            val purchaseToken = purchase.purchaseToken
            _purchaseStatus.tryEmit(
              PurchaseStatus.Success(
                productName = productName,
                sku = sku,
                purchaseToken = purchaseToken,
                orderId = purchase.orderId
              )
            )
          }
          BillingClient.BillingResponseCode.USER_CANCELED -> {
            Log.w("Billing", "---inside acknowledge purchase--- User cancelled the acknowledge")
            _purchaseStatus.tryEmit(PurchaseStatus.UserCancelled)
          }

          BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
            Log.w("Billing", "---inside acknowledge purchase--- User BILLING_UNAVAILABLE the acknowledge")
            _purchaseStatus.tryEmit(PurchaseStatus.BillingUnavailable)
          }

          BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
            Log.w("Billing", "---inside acknowledge purchase--- User ITEM_UNAVAILABLE the acknowledge")
            _purchaseStatus.tryEmit(PurchaseStatus.ProductUnavailable)
          }

          else -> {
            Log.w("Billing", "---inside acknowledge purchase--- Failed to acknowledge: ${ackResult.debugMessage}")
            _purchaseStatus.tryEmit(PurchaseStatus.Error)
          }

          /*else -> {
            Log.w(
              "BillingDataSource",
              "---inside acknowledge purchase--- Failed to acknowledge: ${ackResult.debugMessage}"
            )
            _purchaseStatus.tryEmit(PurchaseStatus.Error)
          }*/
        }
      }
    }
  }

  // Handle consume purchase to make it can be buy again instead of once time
 /*
 private fun consumePurchase(purchase: Purchase) {
    val consumeParams = ConsumeParams.newBuilder()
      .setPurchaseToken(purchase.purchaseToken)
      .build()

    billingClient.consumeAsync(consumeParams) { result, token ->
      when(result.responseCode) {
        BillingClient.BillingResponseCode.OK -> {
          Log.d("Billing", "---inside consume purchase--- Success with Purchase consumed: $token")
          val productDetail = productDetailsList.first { item ->
            item.productId == purchase.products.first()
          }
          val productName = productDetail.name
          val sku = productDetail.productId
          val purchaseToken = purchase.purchaseToken
          _purchaseStatus.tryEmit(
            PurchaseStatus.Success(
              productName = productName,
              sku = sku,
              purchaseToken = purchaseToken,
              orderId = purchase.orderId
            )
          )
        }
        BillingClient.BillingResponseCode.USER_CANCELED -> {
          Log.w("Billing", "---inside consume purchase--- User cancelled the consume")
          _purchaseStatus.tryEmit(PurchaseStatus.UserCancelled)
        }

        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
          Log.w("Billing", "---inside consume purchase--- User cancelled the consume")
          _purchaseStatus.tryEmit(PurchaseStatus.BillingUnavailable)
        }

        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
          Log.w("Billing", "---inside consume purchase--- User cancelled the consume")
          _purchaseStatus.tryEmit(PurchaseStatus.ProductUnavailable)
        }

        else -> {
          Log.w("Billing", "---inside consume purchase--- Failed to consume: ${result.debugMessage}")
          _purchaseStatus.tryEmit(PurchaseStatus.Error)
        }
      }
    }
  }
  */

  fun isReady(): Boolean = billingClient.isReady

  fun resetPurchaseStatus() {
    _purchaseStatus.tryEmit(PurchaseStatus.Idle)
  }
}
