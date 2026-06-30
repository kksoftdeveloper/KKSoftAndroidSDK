package com.appmb.sdk.mbcore.domain.auth

import arrow.core.Either
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.error.NetworkError

interface MbCoreAuthRepository {

  /**
   * Refresh token
   *
   * @return A flow emitting the result of refresh token operation.
   */
  suspend fun refreshToken(): Either<NetworkError, MbRefreshTokenResponse?>
}