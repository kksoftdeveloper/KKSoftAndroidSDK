package com.appmb.sdk.mbcore.data.datasource.auth

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError

interface MbCoreAuthDataSource {

  /**
   * Send a request to refresh accessToken
   *
   * @param mbRefreshTokenRequest The refresh token params.
   * @return A flow which emits the result of the refresh accessToken operation.
   */
  suspend fun refreshToken(
    mbRefreshTokenRequest: MbRefreshTokenRequest,
  ): Either<NetworkError, ResponseWrapper<MbRefreshTokenResponse>>
}