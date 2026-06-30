package com.appmb.sdk.mbpayment.data.datasource

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.request.ValidateGamePackageRequest
import com.appmb.sdk.mbpayment.data.dto.request.VerifyGamePackagePurchaseRequest
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse
import com.appmb.sdk.mbpayment.network.MbPaymentApi

class MbPaymentDataSource(
  private val mbPaymentApi: MbPaymentApi,
) {
  suspend fun getProductIds(gameId: String, serverId: String?, page: Int, size: Int): Either<NetworkError, ResponseWrapper<ProductListData>> {
    return mbPaymentApi.getProductIds(
      gameId = gameId,
      serverId = serverId,
      size = size,
      page = page
    )
  }

  suspend fun validateGamePackage(
    request: ValidateGamePackageRequest,
  ): Either<NetworkError, ResponseWrapper<ValidatePackageResponse>> {
    return mbPaymentApi.validateGamePackage(request)
  }

  suspend fun verifyGamePackagePurchase(
    request: VerifyGamePackagePurchaseRequest,
  ): Either<NetworkError, ResponseWrapper<VerifyGamePackagePurchaseResponse>> {
    return mbPaymentApi.verifyGamePackagePurchase(request)
  }
}