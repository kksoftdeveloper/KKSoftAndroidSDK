package com.appmb.sdk.mbpayment.domain

class FetchListProductsUseCase(
  private val billingRepository: BillingRepository,
) {
  suspend operator fun invoke(skus: List<String>) =
    billingRepository.loadProducts(skus)
}