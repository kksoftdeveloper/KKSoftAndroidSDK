package com.appmb.sdk.mbcore.data.repo

import arrow.core.Either
import arrow.core.left
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.datasource.auth.MbCoreAuthDataSource
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository

import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.ext.mapDataNull
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import com.appmb.sdk.mbcore.utils.sha256
import kotlinx.coroutines.flow.firstOrNull
import android.util.Log

class MbCoreAuthRepositoryImpl(
  private val mbCoreAuthDataSource: MbCoreAuthDataSource,
  private val mbCommonDataSource: MbCoreCommonDataSource,
  private val mbSdkConfig: MbSdkConfig,
) : MbCoreAuthRepository {

  override suspend fun refreshToken(): Either<NetworkError, MbRefreshTokenResponse?> {
    val sessionData = getSession().getSessionData().firstOrNull() ?: return NetworkError.DataNullError.left()
    val refreshToken = sessionData.refreshToken
    val deviceId = mbCommonDataSource.getDeviceId()
    val deviceSecretId = mbCommonDataSource.getDeviceSecretId()

    val sign =
      "$deviceId|${mbCommonDataSource.getPlatform()}|${mbSdkConfig.getAuthSdkVersion()}|" +
          "${mbSdkConfig.getAppVersionName()}|$refreshToken|$deviceSecretId"

    val request = MbRefreshTokenRequest(
      deviceId = deviceId,
      platform = mbCommonDataSource.getPlatform(),
      sdkVersion = mbSdkConfig.getAuthSdkVersion(),
      appVersion = mbSdkConfig.getAppVersionName(),
      gameId = mbCommonDataSource.getGameId(),
      refreshToken = refreshToken,
      // Sign format - deviceId|platform|sdkVersion|appVersion|refreshToken|secretKey
      sign = sign.sha256(),
    )
    return mbCoreAuthDataSource.refreshToken(request).mapDataNull()
  }
}