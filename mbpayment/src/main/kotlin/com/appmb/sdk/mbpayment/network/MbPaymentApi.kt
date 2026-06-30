package com.appmb.sdk.mbpayment.network

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.ProductListResponse
import com.appmb.sdk.mbpayment.data.dto.request.ValidateGamePackageRequest
import com.appmb.sdk.mbpayment.data.dto.request.VerifyGamePackagePurchaseRequest
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse

interface MbPaymentApi {
  suspend fun getProductIds(
    gameId: String,
    serverId: String?,
    size: Int = 10,
    page: Int = 0
  ): Either<NetworkError, ResponseWrapper<ProductListData>>

  suspend fun validateGamePackage(
    request: ValidateGamePackageRequest,
  ): Either<NetworkError, ResponseWrapper<ValidatePackageResponse>>

  suspend fun verifyGamePackagePurchase(
    request: VerifyGamePackagePurchaseRequest,
  ): Either<NetworkError, ResponseWrapper<VerifyGamePackagePurchaseResponse>>

  companion object {
    const val PRODUCT_IDS_PATH = "/sdk/api/v1/game-packages"
    const val VALIDATE_GAME_PACKAGE_PATH = "/sdk/api/v1/purchase/validate-package"
    const val VERIFY_GAME_PACKAGE_PURCHASE_PATH = "/sdk/api/v1/purchase/verify"
  }
}