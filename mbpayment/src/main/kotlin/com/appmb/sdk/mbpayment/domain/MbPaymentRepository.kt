package com.appmb.sdk.mbpayment.domain

import arrow.core.Either
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse
import com.appmb.sdk.mbpayment.model.MbPaymentParams

interface MbPaymentRepository {

  suspend fun getProductIds(
    size: Int = 10,
    page: Int = 0
  ): Either<NetworkError, ProductListData?>

  suspend fun validateGamePackage(
    mbPaymentParams: MbPaymentParams,
  ): Either<NetworkError, ValidatePackageResponse?>

  suspend fun verifyGamePackagePurchase(
    mbPaymentParams: MbPaymentParams,
  ): Either<NetworkError, VerifyGamePackagePurchaseResponse?>
}