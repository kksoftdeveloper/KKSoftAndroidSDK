package com.appmb.sdk.mbpayment.data.repository

import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.ext.mapDataNull
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory
import com.appmb.sdk.mbcore.utils.sha256
import com.appmb.sdk.mbpayment.data.datasource.MbPaymentDataSource
import com.appmb.sdk.mbpayment.data.dto.ProductListData
import com.appmb.sdk.mbpayment.data.dto.request.ValidateGamePackageRequest
import com.appmb.sdk.mbpayment.data.dto.request.VerifyGamePackagePurchaseRequest
import com.appmb.sdk.mbpayment.data.dto.response.ValidatePackageResponse
import com.appmb.sdk.mbpayment.data.dto.response.VerifyGamePackagePurchaseResponse
import com.appmb.sdk.mbpayment.domain.MbPaymentRepository
import com.appmb.sdk.mbpayment.model.MbPaymentParams
import kotlinx.coroutines.flow.firstOrNull

class MbPaymentRepositoryImpl(
  private val paymentDataSource: MbPaymentDataSource,
  private val mbCoreCommonDataSource: MbCoreCommonDataSource,
  private val mbSdkConfig: MbSdkConfig,
) : MbPaymentRepository {

  override suspend fun getProductIds(
    size: Int,
    page: Int
  ): Either<NetworkError, ProductListData?> {
    return paymentDataSource.getProductIds(
      gameId = mbCoreCommonDataSource.getGameId(),
      serverId = mbCoreCommonDataSource.getServerId(),
      size = size,
      page = page
    ).mapDataNull()
  }

  override suspend fun validateGamePackage(
    mbPaymentParams: MbPaymentParams,
  ): Either<NetworkError, ValidatePackageResponse?> {
    if (mbPaymentParams.sku.isNullOrEmpty() || mbPaymentParams.price.isNullOrEmpty()) {
      return NetworkError.DataNullError.left()
    }
    val gameId = mbCoreCommonDataSource.getGameId()
    val serverId = mbCoreCommonDataSource.getServerId()
//      MbAuthSessionFactory.getSession().getSessionData().firstOrNull()?.serverId.orEmpty()
    val platform = mbCoreCommonDataSource.getPlatform()
    val appVersion = mbSdkConfig.getAppVersionName()
    val sdkVersion = mbSdkConfig.getPaymentSdkVersion()
    val secretKey = mbCoreCommonDataSource.getDeviceSecretId()
    // Sign format: sku|price|gameId|serverId|platform|appVersion|sdkVersion|secretKey
    val sign =
      "${mbPaymentParams.sku}|${mbPaymentParams.price}|$gameId|$serverId|$platform|$appVersion|$sdkVersion|$secretKey"
    val request = ValidateGamePackageRequest(
      sku = mbPaymentParams.sku.orEmpty(),
      price = mbPaymentParams.price.orEmpty(),
      gameId = gameId.toInt(),
      serverId = serverId?.toInt(),
      platform = platform,
      appVersion = appVersion.orEmpty(),
      sdkVersion = sdkVersion.orEmpty(),
      sign = sign.sha256()
    )
    return paymentDataSource.validateGamePackage(request).mapDataNull()
  }

  override suspend fun verifyGamePackagePurchase(
    mbPaymentParams: MbPaymentParams,
  ): Either<NetworkError, VerifyGamePackagePurchaseResponse?> {
    if (mbPaymentParams.sku.isNullOrEmpty() || mbPaymentParams.purchaseToken.isNullOrEmpty()) {
      return NetworkError.DataNullError.left()
    }
    val gameId = mbCoreCommonDataSource.getGameId()
    val serverId = mbCoreCommonDataSource.getServerId()
//      MbAuthSessionFactory.getSession().getSessionData().firstOrNull()?.serverId.orEmpty()
    val platform = mbCoreCommonDataSource.getPlatform()
    val appVersion = mbSdkConfig.getAppVersionName()
    val sdkVersion = mbSdkConfig.getPaymentSdkVersion()
    val secretKey = mbCoreCommonDataSource.getDeviceSecretId()
    // Sign format: sku|purchaseToken|gameId|serverId|platform|appVersion|sdkVersion|secretKey
    val sign =
      "${mbPaymentParams.sku}|${mbPaymentParams.purchaseToken}" +
          "|$gameId|$serverId|$platform|$appVersion|$sdkVersion|$secretKey"
    val request = VerifyGamePackagePurchaseRequest(
      sku = mbPaymentParams.sku.orEmpty(),
      orderId = mbPaymentParams.orderId.orEmpty(),
      purchaseToken = mbPaymentParams.purchaseToken.orEmpty(),
      gameId = gameId.toInt(),
      serverId = serverId?.toInt(),
      platform = platform,
      appVersion = appVersion.orEmpty(),
      sdkVersion = sdkVersion.orEmpty(),
      sign = sign.sha256()
    )
    return paymentDataSource.verifyGamePackagePurchase(request).mapDataNull()
  }
}