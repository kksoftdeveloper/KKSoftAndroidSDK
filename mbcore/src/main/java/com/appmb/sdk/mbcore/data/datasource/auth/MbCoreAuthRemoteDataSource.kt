package com.appmb.sdk.mbcore.data.datasource.auth

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.network.api.auth.MbCoreAuthApi

class MbCoreAuthRemoteDataSource(
  private val mbCoreAuthApi: MbCoreAuthApi,
) : MbCoreAuthDataSource {
  override suspend fun refreshToken(mbRefreshTokenRequest: MbRefreshTokenRequest): Either<NetworkError, ResponseWrapper<MbRefreshTokenResponse>> =
    mbCoreAuthApi.refreshToken(request = mbRefreshTokenRequest)
}