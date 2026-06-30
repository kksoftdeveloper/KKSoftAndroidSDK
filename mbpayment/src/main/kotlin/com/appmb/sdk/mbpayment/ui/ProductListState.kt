package com.appmb.sdk.mbpayment.ui

import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct

data class ProductListState(
  val isLoading: Boolean = false,
  val isGoogleBillingAvailable: Boolean = false,
  val products: List<GoogleBillingProduct> = emptyList(),
  val purchasedStatus: PurchaseStatus = PurchaseStatus.Idle,
  val error: Int? = null,
  val isRefreshing: Boolean = false,
  val isLoadingMore: Boolean = false,
  val hasMoreProducts: Boolean = true,
  val currentPage: Int = 0,
  val pageSize: Int = 10
)
