package com.appmb.sdk.mbcore.network.api.auth

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.ResponseWrapper
import com.appmb.sdk.mbcore.data.dto.request.MbRefreshTokenRequest
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError

interface MbCoreAuthApi {
  suspend fun refreshToken(request: MbRefreshTokenRequest): Either<NetworkError, ResponseWrapper<MbRefreshTokenResponse>>

  companion object {
    const val REFRESH_TOKEN_PATH = "/sdk/api/v1/auth/refresh-token"
  }
}